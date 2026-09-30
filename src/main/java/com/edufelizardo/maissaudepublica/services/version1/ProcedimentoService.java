package com.edufelizardo.maissaudepublica.services.version1;

import com.edufelizardo.maissaudepublica.config.ContextoAuditoria;
import com.edufelizardo.maissaudepublica.models.enuns.StatusProcedimento;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.StatusProcedimentoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.RetificacaoProcedimentoRequestDto;
import com.edufelizardo.maissaudepublica.exceptions.ResourceUnprocessableEntityException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceBadRequestException;
import com.edufelizardo.maissaudepublica.config.UsuarioAutenticado;
import org.springframework.transaction.annotation.Transactional;
import java.util.Map;
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
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * CRUD do Procedimento (Assistência — ver ADR-0039/ADR-0044). {@code profissionalMatricula} é
 * resolvido para o {@link Profissional} real na fronteira da API, mesmo padrão do
 * {@code ConsultaService} (ver ADR-0034/ADR-0043).
 */
@Service
public class ProcedimentoService {

    @Autowired
    private ProcedimentoRepository procedimentoRepository;

    @Autowired
    private ControleDeAcesso controleDeAcesso;

    @Autowired
    private ConsultaRepository consultaRepository;

    @Autowired
    private ProfissionalRepository profissionalRepository;

    @Transactional
    public ProcedimentoResponseDto criar(ProcedimentoRequestDto dto) {
        Consulta consulta = buscarConsultaPorId(dto.getConsultaId());
        controleDeAcesso.exigir("PROCEDIMENTO.REGISTRAR", consulta.getAtendimento().getUnidade());
        ContextoAuditoria.paciente(consulta.getAtendimento().getPaciente().getUuid());
        if (consultaRepository.existsByRetificacaoDe_Uuid(consulta.getUuid())) {
            throw new ResourceUnprocessableEntityException("Esta consulta foi retificada: registre o procedimento na versão vigente.");
        }
        Profissional profissional = buscarProfissionalPorMatricula(dto.getProfissionalMatricula());

        Procedimento procedimento = new Procedimento(consulta, profissional, dto.getTipo(), dto.getDescricao(),
                dto.getDataRealizacao(), dto.getStatus());
        procedimento.setRegistradoEm(Instant.now());
        procedimento.setRegistradoPorCpf(UsuarioAutenticado.cpf());
        procedimento = procedimentoRepository.save(procedimento);
        ContextoAuditoria.registro(procedimento.getUuid());
        return ProcedimentoResponseDto.fromProcedimento(procedimento);
    }

    /**
     * Retificação (ADR-0062): grava uma nova versão ligada à anterior, que continua no prontuário. Só a
     * versão vigente pode ser retificada, e o procedimento continua no mesmo consulta.
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
        controleDeAcesso.exigir("PROCEDIMENTO.REGISTRAR", original.getConsulta().getAtendimento().getUnidade());
        ContextoAuditoria.paciente(original.getConsulta().getAtendimento().getPaciente().getUuid());
        controleDeAcesso.exigirAutoriaOuSupervisao(original.getRegistradoPorCpf(), original.getConsulta().getAtendimento().getUnidade());
        if (!original.getConsulta().getUuid().equals(dto.getConsultaId())) {
            throw new ResourceBadRequestException("A retificação precisa manter o mesmo consulta do registro original.");
        }
        Profissional profissional = buscarProfissionalPorMatricula(dto.getProfissionalMatricula());

        Procedimento procedimento = new Procedimento(original.getConsulta(), profissional, dto.getTipo(), dto.getDescricao(),
                dto.getDataRealizacao(), dto.getStatus());
        procedimento.setRetificacaoDe(original);
        procedimento.setMotivoRetificacao(dto.getMotivoRetificacao().trim());
        procedimento.setRegistradoEm(Instant.now());
        procedimento.setRegistradoPorCpf(UsuarioAutenticado.cpf());
        procedimento = procedimentoRepository.save(procedimento);
        ContextoAuditoria.registro(procedimento.getUuid());
        return ProcedimentoResponseDto.fromProcedimento(procedimento, null);
    }

    /**
     * Desfecho de um procedimento agendado (ADR-0062): acontece uma vez, de AGENDADO para REALIZADO
     * (com a data em que foi feito) ou CANCELADO (com justificativa). A data prevista fica guardada.
     */
    @Transactional
    public ProcedimentoResponseDto alterarStatus(UUID uuid, StatusProcedimentoRequestDto dto) {
        Procedimento procedimento = procedimentoRepository.findByIdParaRetificar(uuid)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Não foi possível encontrar um procedimento com o id " + uuid + " em nossos registros."));
        controleDeAcesso.exigir("PROCEDIMENTO.REGISTRAR", procedimento.getConsulta().getAtendimento().getUnidade());
        ContextoAuditoria.paciente(procedimento.getConsulta().getAtendimento().getPaciente().getUuid());
        if (sucessores().containsKey(uuid)) {
            throw new ResourceUnprocessableEntityException(
                    "Este procedimento foi retificado: altere o status da versão vigente.");
        }
        if (procedimento.getStatus() != StatusProcedimento.AGENDADO) {
            throw new ResourceUnprocessableEntityException("Só um procedimento agendado muda de status; este está "
                    + procedimento.getStatus() + ". Para corrigir, use a retificação.");
        }
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
        return ProcedimentoResponseDto.fromProcedimento(procedimentoRepository.save(procedimento), null);
    }

    /** Versão que corrige cada registro retificado (registro → sucessor). */
    private Map<UUID, UUID> sucessores() {
        return procedimentoRepository.findParesDeRetificacao().stream()
                .collect(Collectors.toMap(par -> (UUID) par[0], par -> (UUID) par[1]));
    }

    public List<ProcedimentoResponseDto> listar() {
        Map<UUID, UUID> sucessores = sucessores();
        return controleDeAcesso.filtrar(procedimentoRepository.findAll().stream(),
                        r -> Stream.of(r.getConsulta().getAtendimento().getUnidade()), "PRONTUARIO.CONSULTAR", "PROCEDIMENTO.REGISTRAR")
                .map(r -> ProcedimentoResponseDto.fromProcedimento(r, sucessores.get(r.getUuid())))
                .collect(Collectors.toList());
    }

    public ProcedimentoResponseDto buscarPorId(UUID uuid) {
        Procedimento registro = buscarEntidadePorId(uuid);
        controleDeAcesso.exigirVisivel(registro.getConsulta().getAtendimento().getUnidade(), "PRONTUARIO.CONSULTAR",
                "PROCEDIMENTO.REGISTRAR");
        ContextoAuditoria.paciente(registro.getConsulta().getAtendimento().getPaciente().getUuid());
        return ProcedimentoResponseDto.fromProcedimento(registro, sucessores().get(uuid));
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
