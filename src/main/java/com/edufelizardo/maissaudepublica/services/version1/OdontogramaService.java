package com.edufelizardo.maissaudepublica.services.version1;

import com.edufelizardo.maissaudepublica.config.ContextoAuditoria;
import com.edufelizardo.maissaudepublica.config.UsuarioAutenticado;
import com.edufelizardo.maissaudepublica.exceptions.ResourceBadRequestException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceConflictException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceNotFoundException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceUnprocessableEntityException;
import com.edufelizardo.maissaudepublica.models.AvaliacaoOdontologica;
import com.edufelizardo.maissaudepublica.models.ComplementoOdontologico;
import com.edufelizardo.maissaudepublica.models.CondicaoOdontologica;
import com.edufelizardo.maissaudepublica.models.Procedimento;
import com.edufelizardo.maissaudepublica.models.RegistroOdontograma;
import com.edufelizardo.maissaudepublica.models.UnidadeDeSaude;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.CondicaoOdontologicaRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.MarcacoesOdontogramaRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.CondicaoOdontologicaDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.OdontogramaDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.RegistroOdontogramaDto;
import com.edufelizardo.maissaudepublica.models.enuns.AlcanceCondicaoOdontologica;
import com.edufelizardo.maissaudepublica.models.enuns.FaceDente;
import com.edufelizardo.maissaudepublica.models.enuns.StatusProcedimento;
import com.edufelizardo.maissaudepublica.repositories.AvaliacaoOdontologicaRepository;
import com.edufelizardo.maissaudepublica.repositories.CondicaoOdontologicaRepository;
import com.edufelizardo.maissaudepublica.repositories.PacienteRepository;
import com.edufelizardo.maissaudepublica.repositories.RegistroOdontogramaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

/**
 * Odontograma com histórico, fatia SB2 (ADR-0135, desenho na ADR-0111): o catálogo de condições, os registros por dente
 * (FDI) e face feitos na avaliação odontológica, a anulação da marcação errada e a projeção do odontograma numa data.
 * Registros, não estado: a situação do dente muda com o tempo, e o histórico responde "como estava antes".
 */
@Service
public class OdontogramaService {

    private static final String REGISTRAR = AvaliacaoOdontologicaService.REGISTRAR;

    @Autowired
    private CondicaoOdontologicaRepository condicaoRepository;

    @Autowired
    private RegistroOdontogramaRepository registroRepository;

    @Autowired
    private AvaliacaoOdontologicaRepository avaliacaoRepository;

    @Autowired
    private PacienteRepository pacienteRepository;

    @Autowired
    private ControleDeAcesso controleDeAcesso;

    @Autowired
    private VinculoAssistencialService vinculoService;

    // ── Catálogo de condições ──────────────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<CondicaoOdontologicaDto> condicoes() {
        return condicaoRepository.findAllByOrderByNomeAsc().stream().map(OdontogramaService::dto).toList();
    }

    @Transactional
    public CondicaoOdontologicaDto criarCondicao(CondicaoOdontologicaRequestDto dto) {
        if (condicaoRepository.existsByCodigo(dto.getCodigo())) {
            throw new ResourceConflictException("Já existe uma condição com o código " + dto.getCodigo() + ".");
        }
        CondicaoOdontologica c = new CondicaoOdontologica(null, dto.getCodigo(), dto.getNome().trim(), dto.getAlcance(), dto.getCor().toUpperCase(),
                dto.getSigla().trim().toUpperCase(), !Boolean.FALSE.equals(dto.getAtiva()));
        c = condicaoRepository.save(c);
        ContextoAuditoria.registro(c.getUuid());
        return dto(c);
    }

    @Transactional
    public CondicaoOdontologicaDto atualizarCondicao(UUID uuid, CondicaoOdontologicaRequestDto dto) {
        CondicaoOdontologica c = condicaoRepository.findById(uuid).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar a condição " + uuid + " em nossos registros."));
        if (!c.getCodigo().equals(dto.getCodigo())) {
            throw new ResourceUnprocessableEntityException("O código da condição não muda (" + c.getCodigo() + ").");
        }
        c.setNome(dto.getNome().trim());
        c.setAlcance(dto.getAlcance());
        c.setCor(dto.getCor().toUpperCase());
        c.setSigla(dto.getSigla().trim().toUpperCase());
        c.setAtiva(!Boolean.FALSE.equals(dto.getAtiva()));
        condicaoRepository.save(c);
        ContextoAuditoria.registro(c.getUuid());
        return dto(c);
    }

    // ── Registros ──────────────────────────────────────────────────────────────────────────────────

    /**
     * Marcações feitas na avaliação odontológica vigente (não retificada, 422): cada uma num dente FDI válido (400), na
     * face que o dente tem (422) e com a condição ativa e do alcance certo (422). A data, sem informar, é a da avaliação.
     */
    @Transactional
    public OdontogramaDto marcar(UUID avaliacaoId, MarcacoesOdontogramaRequestDto dto) {
        AvaliacaoOdontologica a = avaliacaoRepository.findById(avaliacaoId).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar a avaliação odontológica " + avaliacaoId + " em nossos registros."));
        controleDeAcesso.exigir(REGISTRAR, a.getAtendimento().getUnidade());
        ContextoAuditoria.paciente(a.getAtendimento().getPaciente().getUuid());
        ContextoAuditoria.registro(a.getUuid());
        if (avaliacaoRepository.existsByRetificacaoDe_Uuid(a.getUuid())) {
            throw new ResourceUnprocessableEntityException("A avaliação foi retificada: marque o odontograma na versão vigente.");
        }
        LocalDate data = dto.getData() != null ? dto.getData() : a.getDataHora().toLocalDate();
        if (data.isAfter(LocalDate.now())) {
            throw new ResourceBadRequestException("A data do odontograma não pode ser no futuro.");
        }
        Instant agora = Instant.now();
        for (MarcacoesOdontogramaRequestDto.Marcacao m : dto.getMarcacoes()) {
            exigirDente(m.getDente());
            CondicaoOdontologica c = condicaoRepository.findById(m.getCondicaoId()).orElseThrow(() -> new ResourceNotFoundException(
                    "Não foi possível encontrar a condição " + m.getCondicaoId() + " em nossos registros."));
            if (!c.isAtiva()) {
                throw new ResourceUnprocessableEntityException("A condição " + c.getNome() + " está inativa.");
            }
            exigirAlcance(c, m.getFace());
            if (m.getFace() != null) {
                exigirFace(m.getDente(), m.getFace());
            }
            RegistroOdontograma r = new RegistroOdontograma();
            r.setPaciente(a.getAtendimento().getPaciente());
            r.setDente(m.getDente());
            r.setFace(m.getFace());
            r.setCondicao(c);
            r.setData(data);
            r.setAvaliacao(a);
            r.setObservacao(m.getObservacao() == null || m.getObservacao().isBlank() ? null : m.getObservacao().trim());
            r.setRegistradoEm(agora);
            r.setRegistradoPorCpf(UsuarioAutenticado.cpf());
            registroRepository.save(r);
        }
        ContextoAuditoria.detalhe(dto.getMarcacoes().size() + " marcação(ões) no odontograma.");
        return projecao(a.getAtendimento().getPaciente().getUuid(), LocalDate.now());
    }

    /**
     * Anula a marcação errada, com motivo (400); ela fica no histórico e sai da projeção. Já anulada: 422. O registro
     * gravado por um procedimento (ADR-0136) não se anula aqui: corrige-se retificando o procedimento (422).
     */
    @Transactional
    public RegistroOdontogramaDto anular(UUID registroId, String motivo) {
        RegistroOdontograma r = registroRepository.findById(registroId).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar o registro do odontograma " + registroId + " em nossos registros."));
        UnidadeDeSaude unidade = r.getAvaliacao() != null ? r.getAvaliacao().getAtendimento().getUnidade() : r.getProcedimento().getAtendimento().getUnidade();
        controleDeAcesso.exigir(REGISTRAR, unidade);
        ContextoAuditoria.paciente(r.getPaciente().getUuid());
        ContextoAuditoria.registro(r.getUuid());
        controleDeAcesso.exigirAutoriaOuSupervisao(r.getRegistradoPorCpf(), unidade);
        if (r.getProcedimento() != null) {
            throw new ResourceUnprocessableEntityException("Este registro vem do procedimento " + r.getProcedimento().getTipo()
                    + ": para corrigir, retifique o procedimento.");
        }
        if (r.getAnuladoEm() != null) {
            throw new ResourceUnprocessableEntityException("Este registro já foi anulado.");
        }
        if (motivo == null || motivo.isBlank()) {
            throw new ResourceBadRequestException("Informe o motivo da anulação.");
        }
        r.setAnuladoEm(Instant.now());
        r.setMotivoAnulacao(motivo.trim());
        r.setAnuladoPorCpf(UsuarioAutenticado.cpf());
        registroRepository.save(r);
        ContextoAuditoria.detalhe("Anulado: " + r.getMotivoAnulacao());
        return dto(r);
    }

    // ── Registros do procedimento (ADR-0136) ───────────────────────────────────────────────────────

    /**
     * O procedimento realizado grava a condição do item do catálogo: um registro por face, ou um do dente inteiro, na
     * data da realização e com o procedimento como origem. Agendado, cancelado ou sem condição: nada.
     */
    @Transactional
    public void gravarDoProcedimento(Procedimento p, ComplementoOdontologico complemento) {
        if (complemento == null || p.getStatus() != StatusProcedimento.REALIZADO || p.getCatalogo() == null
                || p.getCatalogo().getCondicaoResultante() == null) {
            return;
        }
        List<FaceDente> faces = complemento.getFaces().isEmpty() ? java.util.Collections.singletonList(null)
                : complemento.getFaces().stream().sorted().toList();
        Instant agora = Instant.now();
        for (FaceDente face : faces) {
            RegistroOdontograma r = new RegistroOdontograma();
            r.setPaciente(p.getAtendimento().getPaciente());
            r.setDente(complemento.getDente());
            r.setFace(face);
            r.setCondicao(p.getCatalogo().getCondicaoResultante());
            r.setData(p.getDataRealizacao().toLocalDate());
            r.setProcedimento(p);
            r.setRegistradoEm(agora);
            r.setRegistradoPorCpf(UsuarioAutenticado.cpf());
            registroRepository.save(r);
        }
    }

    /** Anula os registros que o procedimento gravou (na retificação dele); ficam no histórico. */
    @Transactional
    public void anularDoProcedimento(Procedimento p, String motivo) {
        Instant agora = Instant.now();
        for (RegistroOdontograma r : registroRepository.findByProcedimento_UuidAndAnuladoEmIsNull(p.getUuid())) {
            r.setAnuladoEm(agora);
            r.setMotivoAnulacao(motivo.length() > 500 ? motivo.substring(0, 500) : motivo);
            r.setAnuladoPorCpf(UsuarioAutenticado.cpf());
            registroRepository.save(r);
        }
    }

    // ── Leitura ────────────────────────────────────────────────────────────────────────────────────

    /** O odontograma do paciente numa data (padrão: hoje), com as regras do prontuário (vínculo e leitura auditada). */
    @Transactional(readOnly = true)
    public OdontogramaDto odontograma(UUID pacienteId, LocalDate data) {
        exigirLeitura(pacienteId);
        return projecao(pacienteId, data != null ? data : LocalDate.now());
    }

    /** Todos os registros de um dente, inclusive os anulados, do mais antigo ao mais recente. */
    @Transactional(readOnly = true)
    public List<RegistroOdontogramaDto> historicoDoDente(UUID pacienteId, int dente) {
        exigirLeitura(pacienteId);
        exigirDente(dente);
        return registroRepository.findDoPaciente(pacienteId).stream().filter(r -> r.getDente() == dente).map(OdontogramaService::dto).toList();
    }

    // ── Apoio ──────────────────────────────────────────────────────────────────────────────────────

    private void exigirLeitura(UUID pacienteId) {
        if (!pacienteRepository.existsById(pacienteId)) {
            throw new ResourceNotFoundException("Não foi possível encontrar um paciente com o id " + pacienteId + " em nossos registros.");
        }
        ContextoAuditoria.paciente(pacienteId);
        vinculoService.exigirVinculo(pacienteId);
    }

    /**
     * A projeção: os registros não anulados até a data, em ordem. O registro do dente inteiro vale por cima das faces
     * registradas antes dele (o dente extraído não tem mais a cárie da oclusal).
     */
    private OdontogramaDto projecao(UUID pacienteId, LocalDate data) {
        List<RegistroOdontograma> todos = registroRepository.findDoPaciente(pacienteId).stream().filter(r -> !r.getData().isAfter(data)).toList();
        Map<Integer, RegistroOdontograma> doDente = new TreeMap<>();
        Map<Integer, Map<FaceDente, RegistroOdontograma>> dasFaces = new TreeMap<>();
        for (RegistroOdontograma r : todos) {
            if (r.getAnuladoEm() != null) {
                continue;
            }
            if (r.getFace() == null) {
                doDente.put(r.getDente(), r);
                dasFaces.remove(r.getDente());
            } else {
                dasFaces.computeIfAbsent(r.getDente(), k -> new EnumMap<>(FaceDente.class)).put(r.getFace(), r);
            }
        }
        TreeMap<Integer, OdontogramaDto.Dente> dentes = new TreeMap<>();
        doDente.forEach((d, r) -> dentes.put(d, new OdontogramaDto.Dente(d, dto(r), new ArrayList<>())));
        dasFaces.forEach((d, faces) -> {
            OdontogramaDto.Dente dente = dentes.computeIfAbsent(d, k -> new OdontogramaDto.Dente(k, null, new ArrayList<>()));
            faces.forEach((f, r) -> dente.getFaces().add(new OdontogramaDto.Face(f, dto(r))));
        });
        return new OdontogramaDto(pacienteId, data, List.copyOf(dentes.values()), todos.size());
    }

    /** FDI (ISO 3950): permanentes nos quadrantes 1 a 4 (dentes 1 a 8); decíduos nos quadrantes 5 a 8 (dentes 1 a 5). */
    static void exigirDente(Integer dente) {
        int q = dente == null ? 0 : dente / 10;
        int n = dente == null ? 0 : dente % 10;
        boolean permanente = q >= 1 && q <= 4 && n >= 1 && n <= 8;
        boolean deciduo = q >= 5 && q <= 8 && n >= 1 && n <= 5;
        if (!permanente && !deciduo) {
            throw new ResourceBadRequestException("Dente " + dente + " fora da notação FDI (permanentes 11 a 48, decíduos 51 a 85).");
        }
    }

    /** Oclusal só nos posteriores (pré-molares e molares; nos decíduos, os molares); incisal só nos anteriores. */
    static void exigirFace(int dente, FaceDente face) {
        int n = dente % 10;
        boolean anterior = n <= 3;
        if (face == FaceDente.OCLUSAL && anterior) {
            throw new ResourceUnprocessableEntityException("O dente " + dente + " é anterior: use a face incisal, e não a oclusal.");
        }
        if (face == FaceDente.INCISAL && !anterior) {
            throw new ResourceUnprocessableEntityException("O dente " + dente + " é posterior: use a face oclusal, e não a incisal.");
        }
    }

    private static void exigirAlcance(CondicaoOdontologica c, FaceDente face) {
        if (face == null && c.getAlcance() == AlcanceCondicaoOdontologica.FACE) {
            throw new ResourceUnprocessableEntityException("A condição " + c.getNome() + " se marca numa face: informe a face.");
        }
        if (face != null && c.getAlcance() == AlcanceCondicaoOdontologica.DENTE) {
            throw new ResourceUnprocessableEntityException("A condição " + c.getNome() + " é do dente inteiro: não informe a face.");
        }
    }

    static CondicaoOdontologicaDto dto(CondicaoOdontologica c) {
        return new CondicaoOdontologicaDto(c.getUuid(), c.getCodigo(), c.getNome(), c.getAlcance(), c.getCor(), c.getSigla(), c.isAtiva());
    }

    static RegistroOdontogramaDto dto(RegistroOdontograma r) {
        CondicaoOdontologica c = r.getCondicao();
        AvaliacaoOdontologica a = r.getAvaliacao();
        Procedimento p = r.getProcedimento();
        UUID atendimento = a != null ? a.getAtendimento().getUuid() : p != null ? p.getAtendimento().getUuid() : null;
        String profissional = a != null ? a.getProfissional().getNome() : p != null ? p.getProfissional().getNome() : null;
        return new RegistroOdontogramaDto(r.getUuid(), r.getDente(), r.getFace(), c.getUuid(), c.getCodigo(), c.getNome(), c.getCor(), c.getSigla(),
                r.getData(), a != null ? a.getUuid() : null, atendimento, profissional, r.getObservacao(), r.getAnuladoEm() != null, r.getAnuladoEm(),
                r.getMotivoAnulacao(), r.getRegistradoEm(), p != null ? p.getUuid() : null, p != null ? p.getTipo() : null);
    }
}
