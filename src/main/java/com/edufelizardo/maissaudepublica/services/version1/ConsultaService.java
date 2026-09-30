package com.edufelizardo.maissaudepublica.services.version1;

import com.edufelizardo.maissaudepublica.config.ContextoAuditoria;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.RetificacaoConsultaRequestDto;
import com.edufelizardo.maissaudepublica.exceptions.ResourceUnprocessableEntityException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceBadRequestException;
import com.edufelizardo.maissaudepublica.config.UsuarioAutenticado;
import org.springframework.transaction.annotation.Transactional;
import java.util.Map;
import java.time.Instant;
import com.edufelizardo.maissaudepublica.exceptions.ResourceNotFoundException;
import com.edufelizardo.maissaudepublica.models.Atendimento;
import com.edufelizardo.maissaudepublica.models.Consulta;
import com.edufelizardo.maissaudepublica.models.Profissional;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.ConsultaRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.ConsultaResponseDto;
import com.edufelizardo.maissaudepublica.repositories.AtendimentoRepository;
import com.edufelizardo.maissaudepublica.repositories.ConsultaRepository;
import com.edufelizardo.maissaudepublica.repositories.ProfissionalRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * CRUD da Consulta (Assistência — ver ADR-0039/ADR-0043). {@code profissionalMatricula} é
 * resolvido para o {@link Profissional} real na fronteira da API, mesmo padrão do
 * {@code AtendimentoService} (ver ADR-0034/ADR-0041).
 */
@Service
public class ConsultaService {

    @Autowired
    private ConsultaRepository consultaRepository;

    @Autowired
    private ControleDeAcesso controleDeAcesso;

    @Autowired
    private AtendimentoRepository atendimentoRepository;

    @Autowired
    private ProfissionalRepository profissionalRepository;

    @Transactional
    public ConsultaResponseDto criar(ConsultaRequestDto dto) {
        Atendimento atendimento = buscarAtendimentoPorId(dto.getAtendimentoId());
        controleDeAcesso.exigir("CONSULTA.REGISTRAR", atendimento.getUnidade());
        ContextoAuditoria.paciente(atendimento.getPaciente().getUuid());
        Profissional profissional = buscarProfissionalPorMatricula(dto.getProfissionalMatricula());

        Consulta consulta = new Consulta(atendimento, profissional, dto.getDataHora(), dto.getTipoConsulta(),
                dto.getQueixaPrincipal(), dto.getDiagnostico(), dto.getReceituario(),
                dto.getExamesSolicitados(), dto.getRetorno());
        consulta.setRegistradoEm(Instant.now());
        consulta.setRegistradoPorCpf(UsuarioAutenticado.cpf());
        consulta = consultaRepository.save(consulta);
        ContextoAuditoria.registro(consulta.getUuid());
        return ConsultaResponseDto.fromConsulta(consulta);
    }

    /**
     * Retificação (ADR-0062): grava uma nova versão ligada à anterior, que continua no prontuário. Só a
     * versão vigente pode ser retificada, e a consulta continua no mesmo atendimento.
     */
    @Transactional
    public ConsultaResponseDto retificar(UUID uuid, RetificacaoConsultaRequestDto dto) {
        Consulta original = consultaRepository.findByIdParaRetificar(uuid)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Não foi possível encontrar uma consulta com o id " + uuid + " em nossos registros."));
        UUID vigente = sucessores().get(uuid);
        if (vigente != null) {
            throw new ResourceUnprocessableEntityException("Esta consulta já foi retificada pela versão " + vigente
                    + ": retifique a versão vigente.");
        }
        controleDeAcesso.exigir("CONSULTA.REGISTRAR", original.getAtendimento().getUnidade());
        ContextoAuditoria.paciente(original.getAtendimento().getPaciente().getUuid());
        controleDeAcesso.exigirAutoriaOuSupervisao(original.getRegistradoPorCpf(), original.getAtendimento().getUnidade());
        if (!original.getAtendimento().getUuid().equals(dto.getAtendimentoId())) {
            throw new ResourceBadRequestException("A retificação precisa manter o mesmo atendimento do registro original.");
        }
        Profissional profissional = buscarProfissionalPorMatricula(dto.getProfissionalMatricula());

        Consulta consulta = new Consulta(original.getAtendimento(), profissional, dto.getDataHora(), dto.getTipoConsulta(),
                dto.getQueixaPrincipal(), dto.getDiagnostico(), dto.getReceituario(),
                dto.getExamesSolicitados(), dto.getRetorno());
        consulta.setRetificacaoDe(original);
        consulta.setMotivoRetificacao(dto.getMotivoRetificacao().trim());
        consulta.setRegistradoEm(Instant.now());
        consulta.setRegistradoPorCpf(UsuarioAutenticado.cpf());
        consulta = consultaRepository.save(consulta);
        ContextoAuditoria.registro(consulta.getUuid());
        return ConsultaResponseDto.fromConsulta(consulta, null);
    }

    /** Versão que corrige cada registro retificado (registro → sucessor). */
    private Map<UUID, UUID> sucessores() {
        return consultaRepository.findParesDeRetificacao().stream()
                .collect(Collectors.toMap(par -> (UUID) par[0], par -> (UUID) par[1]));
    }

    public List<ConsultaResponseDto> listar() {
        Map<UUID, UUID> sucessores = sucessores();
        return controleDeAcesso.filtrar(consultaRepository.findAll().stream(), r -> Stream.of(r.getAtendimento().getUnidade()),
                        "PRONTUARIO.CONSULTAR", "CONSULTA.REGISTRAR")
                .map(r -> ConsultaResponseDto.fromConsulta(r, sucessores.get(r.getUuid())))
                .collect(Collectors.toList());
    }

    public ConsultaResponseDto buscarPorId(UUID uuid) {
        Consulta registro = buscarEntidadePorId(uuid);
        controleDeAcesso.exigirVisivel(registro.getAtendimento().getUnidade(), "PRONTUARIO.CONSULTAR", "CONSULTA.REGISTRAR");
        ContextoAuditoria.paciente(registro.getAtendimento().getPaciente().getUuid());
        return ConsultaResponseDto.fromConsulta(registro, sucessores().get(uuid));
    }

    private Consulta buscarEntidadePorId(UUID uuid) {
        return consultaRepository.findById(uuid)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Não foi possível encontrar uma consulta com o id " + uuid + " em nossos registros."));
    }

    private Atendimento buscarAtendimentoPorId(UUID uuid) {
        return atendimentoRepository.findById(uuid)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Não foi possível encontrar um atendimento com o id " + uuid + " em nossos registros."));
    }

    private Profissional buscarProfissionalPorMatricula(String matricula) {
        return profissionalRepository.findByMatricula(matricula)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Não foi possível encontrar um profissional com a matrícula " + matricula + " em nossos registros."));
    }
}
