package com.edufelizardo.maissaudepublica.services.version1;

import com.edufelizardo.maissaudepublica.config.ContextoAuditoria;
import com.edufelizardo.maissaudepublica.models.Atendimento;
import com.edufelizardo.maissaudepublica.models.ComplementoOdontologico;
import com.edufelizardo.maissaudepublica.models.ProcedimentoCatalogo;
import com.edufelizardo.maissaudepublica.models.enuns.AlcanceProcedimento;
import com.edufelizardo.maissaudepublica.models.enuns.FaceDente;
import com.edufelizardo.maissaudepublica.models.enuns.StatusProcedimento;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.StatusProcedimentoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.RetificacaoProcedimentoRequestDto;
import com.edufelizardo.maissaudepublica.exceptions.ResourceUnprocessableEntityException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceBadRequestException;
import com.edufelizardo.maissaudepublica.config.UsuarioAutenticado;
import com.edufelizardo.maissaudepublica.repositories.AtendimentoRepository;
import com.edufelizardo.maissaudepublica.repositories.ComplementoOdontologicoRepository;
import org.springframework.transaction.annotation.Transactional;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.time.Instant;
import com.edufelizardo.maissaudepublica.exceptions.ResourceNotFoundException;
import com.edufelizardo.maissaudepublica.models.Consulta;
import com.edufelizardo.maissaudepublica.models.Procedimento;
import com.edufelizardo.maissaudepublica.models.Profissional;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.ProcedimentoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.ProcedimentoResponseDto;
import com.edufelizardo.maissaudepublica.repositories.ConsultaRepository;
import com.edufelizardo.maissaudepublica.repositories.ProcedimentoRepository;
import com.edufelizardo.maissaudepublica.repositories.ProfissionalRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * CRUD do Procedimento (Assistência — ver ADR-0039/ADR-0044). {@code profissionalMatricula} é
 * resolvido para o {@link Profissional} real na fronteira da API, mesmo padrão do
 * {@code ConsultaService} (ver ADR-0034/ADR-0043).
 *
 * <p>Com a ADR-0136, o procedimento fica no atendimento (a consulta é opcional), aponta para o catálogo geral e, quando
 * odontológico, leva o complemento (dente e faces): realizado, grava os registros do odontograma.
 */
@Service
public class ProcedimentoService {

    private static final String REGISTRAR = "PROCEDIMENTO.REGISTRAR";

    @Autowired
    private ProcedimentoRepository procedimentoRepository;

    @Autowired
    private ControleDeAcesso controleDeAcesso;

    @Autowired
    private ConsultaRepository consultaRepository;

    @Autowired
    private AtendimentoRepository atendimentoRepository;

    @Autowired
    private ProfissionalRepository profissionalRepository;

    @Autowired
    private ProcedimentoCatalogoService catalogoService;

    @Autowired
    private ComplementoOdontologicoRepository complementoRepository;

    @Autowired
    private OdontogramaService odontogramaService;

    @Transactional
    public ProcedimentoResponseDto criar(ProcedimentoRequestDto dto) {
        Consulta consulta = dto.getConsultaId() != null ? buscarConsultaPorId(dto.getConsultaId()) : null;
        Atendimento atendimento = atendimentoDoRegistro(consulta, dto.getAtendimentoId());
        controleDeAcesso.exigir(REGISTRAR, atendimento.getUnidade());
        ContextoAuditoria.paciente(atendimento.getPaciente().getUuid());
        if (consulta != null && consultaRepository.existsByRetificacaoDe_Uuid(consulta.getUuid())) {
            throw new ResourceUnprocessableEntityException("Esta consulta foi retificada: registre o procedimento na versão vigente.");
        }
        ProcedimentoCatalogo item = catalogoService.ativo(dto.getProcedimentoCatalogoId());
        exigirDentista(item, atendimento);
        Set<FaceDente> faces = validarComplemento(item, atendimento, dto.getDente(), dto.getFaces());
        Profissional profissional = buscarProfissionalPorMatricula(dto.getProfissionalMatricula());

        Procedimento procedimento = new Procedimento(atendimento, consulta, profissional, tipo(item, dto.getTipo()), dto.getDescricao(),
                dto.getDataRealizacao(), dto.getStatus());
        procedimento.setCatalogo(item);
        procedimento.setRegistradoEm(Instant.now());
        procedimento.setRegistradoPorCpf(UsuarioAutenticado.cpf());
        procedimento = procedimentoRepository.save(procedimento);
        ComplementoOdontologico complemento = salvarComplemento(procedimento, dto.getDente(), faces);
        odontogramaService.gravarDoProcedimento(procedimento, complemento);
        ContextoAuditoria.registro(procedimento.getUuid());
        return ProcedimentoResponseDto.fromProcedimento(procedimento, null, complemento);
    }

    /**
     * Retificação (ADR-0062): grava uma nova versão ligada à anterior, que continua no prontuário. Só a
     * versão vigente pode ser retificada, e o procedimento continua no mesmo consulta (ou no mesmo atendimento).
     * Os registros do odontograma da versão anterior são anulados, e a nova grava os seus (ADR-0136).
     */
    @Transactional
    public ProcedimentoResponseDto retificar(UUID uuid, RetificacaoProcedimentoRequestDto dto) {
        Procedimento original = procedimentoRepository.findByIdParaRetificar(uuid)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Não foi possível encontrar um procedimento com o id " + uuid + " em nossos registros."));
        UUID vigente = sucessores().get(uuid);
        if (vigente != null) {
            throw new ResourceUnprocessableEntityException("Este procedimento já foi retificado pela versão " + vigente
                    + ": retifique a versão vigente.");
        }
        Atendimento atendimento = original.getAtendimento();
        controleDeAcesso.exigir(REGISTRAR, atendimento.getUnidade());
        ContextoAuditoria.paciente(atendimento.getPaciente().getUuid());
        controleDeAcesso.exigirAutoriaOuSupervisao(original.getRegistradoPorCpf(), atendimento.getUnidade());
        UUID consultaOriginal = original.getConsulta() != null ? original.getConsulta().getUuid() : null;
        if (!Objects.equals(consultaOriginal, dto.getConsultaId())
                || (dto.getAtendimentoId() != null && !atendimento.getUuid().equals(dto.getAtendimentoId()))) {
            throw new ResourceBadRequestException("A retificação precisa manter o mesmo consulta do registro original.");
        }
        exigirDentista(original.getCatalogo(), atendimento);
        ProcedimentoCatalogo item = catalogoService.ativo(dto.getProcedimentoCatalogoId());
        exigirDentista(item, atendimento);
        Set<FaceDente> faces = validarComplemento(item, atendimento, dto.getDente(), dto.getFaces());
        Profissional profissional = buscarProfissionalPorMatricula(dto.getProfissionalMatricula());

        Procedimento procedimento = new Procedimento(atendimento, original.getConsulta(), profissional, tipo(item, dto.getTipo()),
                dto.getDescricao(), dto.getDataRealizacao(), dto.getStatus());
        procedimento.setCatalogo(item);
        procedimento.setRetificacaoDe(original);
        procedimento.setMotivoRetificacao(dto.getMotivoRetificacao().trim());
        procedimento.setRegistradoEm(Instant.now());
        procedimento.setRegistradoPorCpf(UsuarioAutenticado.cpf());
        procedimento = procedimentoRepository.save(procedimento);
        ComplementoOdontologico complemento = salvarComplemento(procedimento, dto.getDente(), faces);
        odontogramaService.anularDoProcedimento(original, "Procedimento retificado: " + procedimento.getMotivoRetificacao());
        odontogramaService.gravarDoProcedimento(procedimento, complemento);
        ContextoAuditoria.registro(procedimento.getUuid());
        return ProcedimentoResponseDto.fromProcedimento(procedimento, null, complemento);
    }

    /**
     * Desfecho de um procedimento agendado (ADR-0062): acontece uma vez, de AGENDADO para REALIZADO
     * (com a data em que foi feito) ou CANCELADO (com justificativa). A data prevista fica guardada.
     * Realizado, o procedimento odontológico grava o odontograma (ADR-0136).
     */
    @Transactional
    public ProcedimentoResponseDto alterarStatus(UUID uuid, StatusProcedimentoRequestDto dto) {
        Procedimento procedimento = procedimentoRepository.findByIdParaRetificar(uuid)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Não foi possível encontrar um procedimento com o id " + uuid + " em nossos registros."));
        controleDeAcesso.exigir(REGISTRAR, procedimento.getAtendimento().getUnidade());
        ContextoAuditoria.paciente(procedimento.getAtendimento().getPaciente().getUuid());
        if (sucessores().containsKey(uuid)) {
            throw new ResourceUnprocessableEntityException(
                    "Este procedimento foi retificado: altere o status da versão vigente.");
        }
        if (procedimento.getStatus() != StatusProcedimento.AGENDADO) {
            throw new ResourceUnprocessableEntityException("Só um procedimento agendado muda de status; este está "
                    + procedimento.getStatus() + ". Para corrigir, use a retificação.");
        }
        exigirDentista(procedimento.getCatalogo(), procedimento.getAtendimento());
        String justificativa = dto.getJustificativa() == null || dto.getJustificativa().isBlank()
                ? null : dto.getJustificativa().trim();
        switch (dto.getStatus()) {
            case REALIZADO -> {
                if (dto.getDataRealizacao() == null) {
                    throw new ResourceBadRequestException("Informe a dataRealizacao do procedimento realizado.");
                }
            }
            case CANCELADO -> {
                if (justificativa == null) {
                    throw new ResourceBadRequestException("Informe a justificativa do cancelamento.");
                }
            }
            default -> throw new ResourceBadRequestException("O novo status precisa ser REALIZADO ou CANCELADO.");
        }
        Profissional profissional = buscarProfissionalPorMatricula(dto.getProfissionalMatricula());

        procedimento.setDataPrevista(procedimento.getDataRealizacao());
        if (dto.getStatus() == StatusProcedimento.REALIZADO) {
            procedimento.setDataRealizacao(dto.getDataRealizacao());
        }
        procedimento.setStatus(dto.getStatus());
        procedimento.setProfissionalStatus(profissional);
        procedimento.setJustificativaStatus(justificativa);
        procedimento.setStatusAlteradoEm(Instant.now());
        procedimento.setStatusAlteradoPorCpf(UsuarioAutenticado.cpf());
        procedimento = procedimentoRepository.save(procedimento);
        ComplementoOdontologico complemento = complementoRepository.findByProcedimento_Uuid(uuid).orElse(null);
        odontogramaService.gravarDoProcedimento(procedimento, complemento);
        return ProcedimentoResponseDto.fromProcedimento(procedimento, null, complemento);
    }

    /** Versão que corrige cada registro retificado (registro → sucessor). */
    private Map<UUID, UUID> sucessores() {
        return procedimentoRepository.findParesDeRetificacao().stream()
                .collect(Collectors.toMap(par -> (UUID) par[0], par -> (UUID) par[1]));
    }

    public List<ProcedimentoResponseDto> listar() {
        Map<UUID, UUID> sucessores = sucessores();
        Map<UUID, ComplementoOdontologico> complementos = complementoRepository.findAll().stream()
                .collect(Collectors.toMap(c -> c.getProcedimento().getUuid(), Function.identity()));
        return controleDeAcesso.filtrar(procedimentoRepository.findAll().stream(),
                        r -> Stream.of(r.getAtendimento().getUnidade()), "PRONTUARIO.CONSULTAR", REGISTRAR)
                .map(r -> ProcedimentoResponseDto.fromProcedimento(r, sucessores.get(r.getUuid()), complementos.get(r.getUuid())))
                .collect(Collectors.toList());
    }

    public ProcedimentoResponseDto buscarPorId(UUID uuid) {
        Procedimento registro = buscarEntidadePorId(uuid);
        controleDeAcesso.exigirVisivel(registro.getAtendimento().getUnidade(), "PRONTUARIO.CONSULTAR", REGISTRAR);
        ContextoAuditoria.paciente(registro.getAtendimento().getPaciente().getUuid());
        return ProcedimentoResponseDto.fromProcedimento(registro, sucessores().get(uuid),
                complementoRepository.findByProcedimento_Uuid(uuid).orElse(null));
    }

    // ── Atendimento, catálogo e complemento (ADR-0136) ─────────────────────────────────────────────

    /** Na consulta, o atendimento dela; sem consulta, o atendimento informado. Nenhum dos dois: 400. */
    private Atendimento atendimentoDoRegistro(Consulta consulta, UUID atendimentoId) {
        if (consulta != null) {
            if (atendimentoId != null && !consulta.getAtendimento().getUuid().equals(atendimentoId)) {
                throw new ResourceBadRequestException("A consulta " + consulta.getUuid() + " é de outro atendimento.");
            }
            return consulta.getAtendimento();
        }
        if (atendimentoId == null) {
            throw new ResourceBadRequestException("Informe a consulta ou o atendimento do procedimento.");
        }
        return atendimentoRepository.findById(atendimentoId).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar um atendimento com o id " + atendimentoId + " em nossos registros."));
    }

    /** Com o item do catálogo, vale o nome dele; sem, o tipo em texto livre é obrigatório (400). */
    private static String tipo(ProcedimentoCatalogo item, String tipo) {
        if (item != null) {
            return item.getNome();
        }
        if (tipo == null || tipo.isBlank()) {
            throw new ResourceBadRequestException("Informe o procedimento do catálogo ou o tipo em texto livre.");
        }
        return tipo.trim();
    }

    /** O procedimento que exige dentista (exodontia, restauração) pede ODONTOLOGIA.REGISTRAR (Lei 11.889/2008). */
    private void exigirDentista(ProcedimentoCatalogo item, Atendimento atendimento) {
        if (item != null && item.isExigeDentista()) {
            controleDeAcesso.exigir(AvaliacaoOdontologicaService.REGISTRAR, atendimento.getUnidade());
        }
    }

    /**
     * O complemento odontológico segue o item do catálogo: nada (sem dente nem face), o dente inteiro ou o dente com as
     * faces, com as regras do odontograma (FDI, 400; face do dente, 422). O procedimento odontológico só entra em
     * atendimento de especialidade odontológica (422).
     */
    private static Set<FaceDente> validarComplemento(ProcedimentoCatalogo item, Atendimento atendimento, Integer dente, List<FaceDente> lista) {
        Set<FaceDente> faces = lista == null ? new LinkedHashSet<>() : new LinkedHashSet<>(lista.stream().filter(Objects::nonNull).toList());
        if (item != null && item.odontologico() && (atendimento.getEspecialidade() == null || !atendimento.getEspecialidade().isOdontologica())) {
            throw new ResourceUnprocessableEntityException("O procedimento " + item.getNome()
                    + " é odontológico: registre-o num atendimento de especialidade odontológica.");
        }
        AlcanceProcedimento alcance = item != null ? item.getAlcance() : AlcanceProcedimento.NENHUM;
        if (alcance == AlcanceProcedimento.NENHUM) {
            if (dente != null || !faces.isEmpty()) {
                throw new ResourceUnprocessableEntityException(item == null
                        ? "Para registrar o dente e a face, escolha o procedimento do catálogo."
                        : "O procedimento " + item.getNome() + " não leva dente nem face.");
            }
            return faces;
        }
        if (dente == null) {
            throw new ResourceBadRequestException("Informe o dente (FDI) do procedimento " + item.getNome() + ".");
        }
        OdontogramaService.exigirDente(dente);
        if (alcance == AlcanceProcedimento.DENTE && !faces.isEmpty()) {
            throw new ResourceUnprocessableEntityException("O procedimento " + item.getNome() + " é do dente inteiro: não informe a face.");
        }
        if (alcance == AlcanceProcedimento.FACE && faces.isEmpty()) {
            throw new ResourceBadRequestException("Informe a face (ou as faces) do procedimento " + item.getNome() + ".");
        }
        faces.forEach(f -> OdontogramaService.exigirFace(dente, f));
        return faces;
    }

    private ComplementoOdontologico salvarComplemento(Procedimento procedimento, Integer dente, Set<FaceDente> faces) {
        if (dente == null) {
            return null;
        }
        return complementoRepository.save(new ComplementoOdontologico(null, procedimento, dente, faces));
    }

    private Procedimento buscarEntidadePorId(UUID uuid) {
        return procedimentoRepository.findById(uuid)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Não foi possível encontrar um procedimento com o id " + uuid + " em nossos registros."));
    }

    private Consulta buscarConsultaPorId(UUID uuid) {
        return consultaRepository.findById(uuid)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Não foi possível encontrar uma consulta com o id " + uuid + " em nossos registros."));
    }

    private Profissional buscarProfissionalPorMatricula(String matricula) {
        return profissionalRepository.findByMatricula(matricula)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Não foi possível encontrar um profissional com a matrícula " + matricula + " em nossos registros."));
    }
}
