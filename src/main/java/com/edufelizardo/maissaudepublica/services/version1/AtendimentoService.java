package com.edufelizardo.maissaudepublica.services.version1;

import com.edufelizardo.maissaudepublica.config.ContextoAuditoria;
import com.edufelizardo.maissaudepublica.repositories.AdministracaoMedicamentoRepository;
import java.util.HashMap;
import java.util.Map;
import org.springframework.transaction.annotation.Transactional;
import com.edufelizardo.maissaudepublica.exceptions.ResourceUnprocessableEntityException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceBadRequestException;
import com.edufelizardo.maissaudepublica.models.enuns.StatusAgendamento;
import com.edufelizardo.maissaudepublica.models.enuns.ClassificacaoRisco;
import com.edufelizardo.maissaudepublica.repositories.EvolucaoEnfermagemRepository;
import com.edufelizardo.maissaudepublica.repositories.ProcedimentoRepository;
import com.edufelizardo.maissaudepublica.repositories.ConsultaRepository;
import com.edufelizardo.maissaudepublica.repositories.TriagemRepository;
import com.edufelizardo.maissaudepublica.exceptions.ResourceNotFoundException;
import com.edufelizardo.maissaudepublica.models.Agendamento;
import com.edufelizardo.maissaudepublica.models.Atendimento;
import com.edufelizardo.maissaudepublica.models.Paciente;
import com.edufelizardo.maissaudepublica.models.Profissional;
import com.edufelizardo.maissaudepublica.models.Setor;
import com.edufelizardo.maissaudepublica.models.UnidadeDeSaude;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.AtendimentoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.AtendimentoResponseDto;
import com.edufelizardo.maissaudepublica.repositories.AgendamentoRepository;
import com.edufelizardo.maissaudepublica.repositories.AtendimentoRepository;
import com.edufelizardo.maissaudepublica.repositories.PacienteRepository;
import com.edufelizardo.maissaudepublica.repositories.ProfissionalRepository;
import com.edufelizardo.maissaudepublica.repositories.SetorRepository;
import com.edufelizardo.maissaudepublica.repositories.UnidadeDeSaudeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * CRUD do Atendimento (Assistência — ver ADR-0039/ADR-0041). {@code profissionalMatricula} é
 * resolvido para o {@link Profissional} real na fronteira da API, mesmo padrão do
 * {@code SetorService} para {@code responsavel} (ver ADR-0034).
 */
@Service
public class AtendimentoService {

    @Autowired
    private AtendimentoRepository atendimentoRepository;

    @Autowired
    private ControleDeAcesso controleDeAcesso;

    @Autowired
    private PacienteRepository pacienteRepository;

    @Autowired
    private ProfissionalRepository profissionalRepository;

    @Autowired
    private UnidadeDeSaudeRepository unidadeDeSaudeRepository;

    @Autowired
    private SetorRepository setorRepository;

    @Autowired
    private AgendamentoRepository agendamentoRepository;

    @Autowired
    private TriagemRepository triagemRepository;

    @Autowired
    private ConsultaRepository consultaRepository;

    @Autowired
    private ProcedimentoRepository procedimentoRepository;

    @Autowired
    private EvolucaoEnfermagemRepository evolucaoEnfermagemRepository;

    @Autowired
    private AdministracaoMedicamentoRepository administracaoMedicamentoRepository;

    /**
     * Um atendimento aberto a partir de um agendamento marca o agendamento como REALIZADO na mesma
     * transação (ADR-0062) — não há mais duas chamadas que possam falhar pela metade.
     */
    @Transactional
    public AtendimentoResponseDto criar(AtendimentoRequestDto dto) {
        Paciente paciente = buscarPacientePorId(dto.getPacienteId());
        Profissional profissional = buscarProfissionalPorMatricula(dto.getProfissionalMatricula());
        UnidadeDeSaude unidade = buscarUnidadePorId(dto.getUnidadeId());
        controleDeAcesso.exigir("ATENDIMENTO.GERENCIAR", unidade);
        ContextoAuditoria.paciente(paciente.getUuid());
        Setor setor = buscarSetorSeInformado(dto.getSetorId());
        Agendamento agendamento = buscarAgendamentoSeInformado(dto.getAgendamentoId());
        realizarAgendamento(agendamento, paciente);

        Atendimento atendimento = new Atendimento(paciente, profissional, unidade, setor, agendamento,
                dto.getTipo(), dto.getStatus(), dto.getDataHora());
        atendimento = atendimentoRepository.save(atendimento);
        ContextoAuditoria.registro(atendimento.getUuid());
        return AtendimentoResponseDto.fromAtendimento(atendimento);
    }

    /**
     * O paciente não muda depois que o atendimento tem registro clínico (ADR-0062): isso levaria
     * triagem, consulta e evolução para o prontuário de outra pessoa. Vincular um novo agendamento o
     * marca como REALIZADO, como na criação.
     */
    @Transactional
    public AtendimentoResponseDto atualizar(UUID uuid, AtendimentoRequestDto dto) {
        Atendimento atendimento = buscarEntidadePorId(uuid);
        controleDeAcesso.exigir("ATENDIMENTO.GERENCIAR", atendimento.getUnidade());
        ContextoAuditoria.paciente(atendimento.getPaciente().getUuid());
        if (!atendimento.getPaciente().getUuid().equals(dto.getPacienteId()) && temRegistroClinico(uuid)) {
            throw new ResourceUnprocessableEntityException(
                    "Este atendimento já tem registro clínico: o paciente não pode ser trocado.");
        }
        Paciente paciente = buscarPacientePorId(dto.getPacienteId());
        Profissional profissional = buscarProfissionalPorMatricula(dto.getProfissionalMatricula());
        UnidadeDeSaude unidade = buscarUnidadePorId(dto.getUnidadeId());
        controleDeAcesso.exigir("ATENDIMENTO.GERENCIAR", unidade);
        Setor setor = buscarSetorSeInformado(dto.getSetorId());
        Agendamento agendamento = buscarAgendamentoSeInformado(dto.getAgendamentoId());
        UUID agendamentoAtual = atendimento.getAgendamento() != null ? atendimento.getAgendamento().getUuid() : null;
        if (agendamento != null && !agendamento.getUuid().equals(agendamentoAtual)) {
            realizarAgendamento(agendamento, paciente);
        }

        atendimento.setPaciente(paciente);
        atendimento.setProfissional(profissional);
        atendimento.setUnidade(unidade);
        atendimento.setSetor(setor);
        atendimento.setAgendamento(agendamento);
        atendimento.setTipo(dto.getTipo());
        atendimento.setStatus(dto.getStatus());
        atendimento.setDataHora(dto.getDataHora());
        atendimento = atendimentoRepository.save(atendimento);
        ContextoAuditoria.registro(atendimento.getUuid());
        return AtendimentoResponseDto.fromAtendimento(atendimento);
    }

    /** Com o resumo clínico de cada atendimento, calculado em consultas agrupadas (ADR-0062). */
    @Transactional(readOnly = true)
    public List<AtendimentoResponseDto> listar() {
        ResumoClinico resumo = resumoClinico();
        return controleDeAcesso.filtrar(atendimentoRepository.findAll().stream(), a -> Stream.of(a.getUnidade()),
                        "ATENDIMENTO.GERENCIAR", "PRONTUARIO.CONSULTAR")
                .map(a -> resumo.aplicar(AtendimentoResponseDto.fromAtendimento(a)))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public AtendimentoResponseDto buscarPorId(UUID uuid) {
        Atendimento atendimento = buscarEntidadePorId(uuid);
        controleDeAcesso.exigirVisivel(atendimento.getUnidade(), "ATENDIMENTO.GERENCIAR", "PRONTUARIO.CONSULTAR");
        ContextoAuditoria.paciente(atendimento.getPaciente().getUuid());
        return resumoClinico().aplicar(AtendimentoResponseDto.fromAtendimento(atendimento));
    }

    /** Contagem de registros vigentes e risco da triagem mais recente, por atendimento. */
    private record ResumoClinico(Map<UUID, ClassificacaoRisco> risco, Map<UUID, Long> triagens,
                                 Map<UUID, Long> consultas, Map<UUID, Long> procedimentos, Map<UUID, Long> evolucoes,
                                 Map<UUID, Long> administracoes) {
        AtendimentoResponseDto aplicar(AtendimentoResponseDto dto) {
            UUID id = dto.getUuid();
            dto.setClassificacaoRiscoAtual(risco.get(id));
            dto.setTotalTriagens(triagens.getOrDefault(id, 0L));
            dto.setTotalConsultas(consultas.getOrDefault(id, 0L));
            dto.setTotalProcedimentos(procedimentos.getOrDefault(id, 0L));
            dto.setTotalEvolucoes(evolucoes.getOrDefault(id, 0L));
            dto.setTotalAdministracoes(administracoes.getOrDefault(id, 0L));
            return dto;
        }
    }

    private ResumoClinico resumoClinico() {
        Map<UUID, ClassificacaoRisco> risco = new HashMap<>();
        for (Object[] linha : triagemRepository.riscosVigentesDoMaisRecente()) {
            risco.putIfAbsent((UUID) linha[0], (ClassificacaoRisco) linha[1]);
        }
        return new ResumoClinico(risco, contagens(triagemRepository.contarVigentesPorAtendimento()),
                contagens(consultaRepository.contarVigentesPorAtendimento()),
                contagens(procedimentoRepository.contarVigentesPorAtendimento()),
                contagens(evolucaoEnfermagemRepository.contarVigentesPorAtendimento()),
                contagens(administracaoMedicamentoRepository.contarVigentesPorAtendimento()));
    }

    private static Map<UUID, Long> contagens(List<Object[]> linhas) {
        return linhas.stream().collect(Collectors.toMap(l -> (UUID) l[0], l -> (Long) l[1]));
    }

    private boolean temRegistroClinico(UUID atendimentoId) {
        return !triagemRepository.findByAtendimentoUuid(atendimentoId).isEmpty()
                || !consultaRepository.findByAtendimentoUuid(atendimentoId).isEmpty()
                || !evolucaoEnfermagemRepository.findByAtendimentoUuid(atendimentoId).isEmpty()
                || !administracaoMedicamentoRepository.findByAtendimentoUuid(atendimentoId).isEmpty();
    }

    /** O agendamento precisa ser do mesmo paciente e ainda estar em aberto; vira REALIZADO. */
    private void realizarAgendamento(Agendamento agendamento, Paciente paciente) {
        if (agendamento == null) {
            return;
        }
        if (!agendamento.getPaciente().getUuid().equals(paciente.getUuid())) {
            throw new ResourceBadRequestException("O agendamento informado é de outro paciente.");
        }
        if (agendamento.getStatus() == StatusAgendamento.CANCELADO || agendamento.getStatus() == StatusAgendamento.REALIZADO) {
            throw new ResourceUnprocessableEntityException("O agendamento está " + agendamento.getStatus()
                    + ": só um agendamento em aberto pode dar origem a um atendimento.");
        }
        agendamento.setStatus(StatusAgendamento.REALIZADO);
        agendamentoRepository.save(agendamento);
    }

    private Atendimento buscarEntidadePorId(UUID uuid) {
        return atendimentoRepository.findById(uuid)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Não foi possível encontrar um atendimento com o id " + uuid + " em nossos registros."));
    }

    private Paciente buscarPacientePorId(UUID uuid) {
        return pacienteRepository.findById(uuid)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Não foi possível encontrar um paciente com o id " + uuid + " em nossos registros."));
    }

    private Profissional buscarProfissionalPorMatricula(String matricula) {
        return profissionalRepository.findByMatricula(matricula)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Não foi possível encontrar um profissional com a matrícula " + matricula + " em nossos registros."));
    }

    private UnidadeDeSaude buscarUnidadePorId(UUID uuid) {
        return unidadeDeSaudeRepository.findById(uuid)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Não foi possível encontrar uma unidade de saúde com o id " + uuid + " em nossos registros."));
    }

    private Setor buscarSetorSeInformado(UUID setorId) {
        if (setorId == null) {
            return null;
        }
        return setorRepository.findById(setorId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Não foi possível encontrar um setor com o id " + setorId + " em nossos registros."));
    }

    private Agendamento buscarAgendamentoSeInformado(UUID agendamentoId) {
        if (agendamentoId == null) {
            return null;
        }
        return agendamentoRepository.findById(agendamentoId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Não foi possível encontrar um agendamento com o id " + agendamentoId + " em nossos registros."));
    }
}
