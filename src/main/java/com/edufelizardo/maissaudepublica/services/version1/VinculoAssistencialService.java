package com.edufelizardo.maissaudepublica.services.version1;

import com.edufelizardo.maissaudepublica.config.ContextoAuditoria;
import com.edufelizardo.maissaudepublica.config.UsuarioAutenticado;
import com.edufelizardo.maissaudepublica.exceptions.ResourceBadRequestException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceNotFoundException;
import com.edufelizardo.maissaudepublica.exceptions.VinculoAssistencialAusenteException;
import com.edufelizardo.maissaudepublica.models.AcessoJustificado;
import com.edufelizardo.maissaudepublica.models.Agendamento;
import com.edufelizardo.maissaudepublica.models.Atendimento;
import com.edufelizardo.maissaudepublica.models.Internacao;
import com.edufelizardo.maissaudepublica.models.Paciente;
import com.edufelizardo.maissaudepublica.models.SolicitacaoRegulacao;
import com.edufelizardo.maissaudepublica.models.UnidadeDeSaude;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.AcessoJustificadoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.AcessoJustificadoResponseDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.AcessoProntuarioDto;
import com.edufelizardo.maissaudepublica.models.enuns.StatusAgendamento;
import com.edufelizardo.maissaudepublica.models.enuns.StatusAtendimento;
import com.edufelizardo.maissaudepublica.models.enuns.StatusInternacao;
import com.edufelizardo.maissaudepublica.models.enuns.StatusSolicitacaoRegulacao;
import com.edufelizardo.maissaudepublica.repositories.AcessoJustificadoRepository;
import com.edufelizardo.maissaudepublica.repositories.AgendamentoRepository;
import com.edufelizardo.maissaudepublica.repositories.AtendimentoRepository;
import com.edufelizardo.maissaudepublica.repositories.InternacaoRepository;
import com.edufelizardo.maissaudepublica.repositories.PacienteRepository;
import com.edufelizardo.maissaudepublica.repositories.SolicitacaoRegulacaoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Prontuário por vínculo assistencial (ADR-0076). Além de {@code PRONTUARIO.CONSULTAR}, abrir o histórico
 * completo de um paciente exige um vínculo com ele:
 * <ol>
 *   <li>atendimento em aberto, ou recente, numa unidade do escopo de quem lê;</li>
 *   <li>ser o profissional de um atendimento recente ou de um agendamento próximo do paciente;</li>
 *   <li>uma solicitação de regulação em curso, ou realizada há pouco, em que a unidade do escopo é a solicitante
 *   ou a executante — referência e contrarreferência (ADR-0089);</li>
 *   <li>uma internação em curso, ou com alta há pouco: a equipe da unidade onde o paciente está internado e o médico
 *   responsável (ADR-0100);</li>
 *   <li>ou um acesso justificado ainda válido, declarado por quem lê.</li>
 * </ol>
 * {@value #SEM_VINCULO} dispensa o vínculo (auditoria clínica, regulação) e não entra em papel padrão.
 *
 * <p>Só age com a autorização ligada ({@link ControleDeAcesso#ativo()}) e com o toggle próprio
 * {@code app.security.prontuario-por-vinculo.enabled}; senão, o prontuário segue aberto à rede (ADR-0067).
 */
@Service
public class VinculoAssistencialService {

    public static final String CONSULTAR = "PRONTUARIO.CONSULTAR";
    public static final String SEM_VINCULO = "PRONTUARIO.CONSULTAR_SEM_VINCULO";

    @Value("${app.security.prontuario-por-vinculo.enabled:false}")
    private boolean ligado;

    @Value("${app.security.prontuario-por-vinculo.janela-dias:30}")
    private int janelaDias;

    @Value("${app.security.prontuario-por-vinculo.acesso-justificado-horas:4}")
    private int horasAcessoJustificado;

    @Value("${app.security.prontuario-por-vinculo.justificativa-minimo:20}")
    private int minimoJustificativa;

    @Autowired
    private ControleDeAcesso controleDeAcesso;

    @Autowired
    private AutorizacaoService autorizacaoService;

    @Autowired
    private AtendimentoRepository atendimentoRepository;

    @Autowired
    private AgendamentoRepository agendamentoRepository;

    @Autowired
    private PacienteRepository pacienteRepository;

    @Autowired
    private AcessoJustificadoRepository acessoJustificadoRepository;

    @Autowired
    private SolicitacaoRegulacaoRepository solicitacaoRegulacaoRepository;

    @Autowired
    private InternacaoRepository internacaoRepository;

    public boolean ativo() {
        return ligado && controleDeAcesso.ativo();
    }

    /**
     * Confere o vínculo de quem está logado com o paciente e diz em que ele se baseou; sem vínculo, 403 com
     * o código {@value VinculoAssistencialAusenteException#CODIGO}. A base vai para o detalhe da auditoria.
     */
    @Transactional(readOnly = true)
    public AcessoProntuarioDto exigirVinculo(UUID pacienteId) {
        if (!ativo()) {
            return AcessoProntuarioDto.livre();
        }
        String cpf = UsuarioAutenticado.cpf();
        if (cpf != null && autorizacaoService.tem(cpf, SEM_VINCULO)) {
            return registrar(new AcessoProntuarioDto("PERMISSAO_AMPLA", "Sem vínculo, por " + SEM_VINCULO, null));
        }
        Optional<String> vinculo = cpf == null ? Optional.empty() : vinculo(cpf, pacienteId);
        if (vinculo.isPresent()) {
            return registrar(new AcessoProntuarioDto("VINCULO", vinculo.get(), null));
        }
        Optional<AcessoJustificado> justificado = cpf == null ? Optional.empty()
                : acessoJustificadoRepository.findFirstByUsuarioCpfAndPaciente_UuidAndExpiraEmAfterOrderByExpiraEmDesc(
                        cpf, pacienteId, Instant.now());
        if (justificado.isPresent()) {
            AcessoJustificado a = justificado.get();
            return registrar(new AcessoProntuarioDto("JUSTIFICADO",
                    "Acesso justificado (" + a.getMotivo() + ") " + a.getUuid(), a.getExpiraEm()));
        }
        throw new VinculoAssistencialAusenteException("Você não tem vínculo assistencial com este paciente: nenhum "
                + "atendimento recente na sua unidade, nem atendimento ou agendamento seu com ele. Para abrir o "
                + "prontuário, registre um acesso justificado.");
    }

    /** Registra o acesso justificado, válido por algumas horas, só para este paciente e este usuário. */
    @Transactional
    public AcessoJustificadoResponseDto justificar(UUID pacienteId, AcessoJustificadoRequestDto dto) {
        Paciente paciente = pacienteRepository.findById(pacienteId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Não foi possível encontrar um paciente com o id " + pacienteId + " em nossos registros."));
        String texto = dto.getJustificativa() == null ? "" : dto.getJustificativa().trim();
        if (texto.length() < minimoJustificativa) {
            throw new ResourceBadRequestException("Descreva a justificativa com pelo menos " + minimoJustificativa + " caracteres.");
        }
        Instant agora = Instant.now();
        AcessoJustificado a = new AcessoJustificado();
        a.setUsuarioCpf(UsuarioAutenticado.cpf() != null ? UsuarioAutenticado.cpf() : "anonimo");
        a.setPaciente(paciente);
        a.setMotivo(dto.getMotivo());
        a.setJustificativa(texto);
        a.setConcedidoEm(agora);
        a.setExpiraEm(agora.plus(Duration.ofHours(horasAcessoJustificado)));
        a = acessoJustificadoRepository.save(a);

        ContextoAuditoria.paciente(pacienteId);
        ContextoAuditoria.registro(a.getUuid());
        // O motivo, sem o texto: a trilha não guarda conteúdo (ADR-0070).
        ContextoAuditoria.detalhe("Motivo " + a.getMotivo() + ", válido até " + a.getExpiraEm());
        return AcessoJustificadoResponseDto.fromAcessoJustificado(a);
    }

    private Optional<String> vinculo(String cpf, UUID pacienteId) {
        LocalDateTime agora = LocalDateTime.now();
        LocalDateTime limite = agora.minusDays(janelaDias);
        List<Atendimento> atendimentos = atendimentoRepository.findByPacienteUuid(pacienteId);

        // 1. Atendimento em aberto, ou recente, numa unidade do escopo.
        Optional<Set<UUID>> visiveis = controleDeAcesso.unidadesVisiveis(CONSULTAR);
        for (Atendimento a : atendimentos) {
            boolean vigente = a.getStatus() != StatusAtendimento.CONCLUIDO
                    || (a.getDataHora() != null && !a.getDataHora().isBefore(limite));
            boolean naUnidade = a.getUnidade() != null
                    && visiveis.map(ids -> ids.contains(a.getUnidade().getUuid())).orElse(true);
            if (vigente && naUnidade) {
                return Optional.of("Atendimento na unidade " + a.getUnidade().getNome());
            }
        }

        // 2. Profissional de um atendimento recente ou de um agendamento próximo.
        for (Atendimento a : atendimentos) {
            if (a.getProfissional() != null && cpf.equals(a.getProfissional().getCpf())
                    && a.getDataHora() != null && !a.getDataHora().isBefore(limite)) {
                return Optional.of("Profissional do atendimento " + a.getUuid());
            }
        }
        LocalDateTime ate = agora.plusDays(janelaDias);
        for (Agendamento g : agendamentoRepository.findByPaciente_UuidOrderByDataHoraAsc(pacienteId)) {
            if (g.getStatus() != StatusAgendamento.CANCELADO && g.getProfissional() != null
                    && cpf.equals(g.getProfissional().getCpf()) && g.getDataHora() != null
                    && !g.getDataHora().isBefore(limite) && !g.getDataHora().isAfter(ate)) {
                return Optional.of("Profissional do agendamento " + g.getUuid());
            }
        }

        // 4. Regulação: a unidade solicitante e a executante, enquanto o encaminhamento está em curso ou
        // foi realizado dentro da janela (ADR-0089).
        Instant limiteRealizacao = Instant.now().minus(Duration.ofDays(janelaDias));
        for (SolicitacaoRegulacao s : solicitacaoRegulacaoRepository.findByPaciente_Uuid(pacienteId)) {
            boolean emCurso = StatusSolicitacaoRegulacao.EM_ABERTO.contains(s.getStatus());
            boolean realizadaHaPouco = s.getStatus() == StatusSolicitacaoRegulacao.REALIZADA
                    && s.getConcluidoEm() != null && s.getConcluidoEm().isAfter(limiteRealizacao);
            if (!emCurso && !realizadaHaPouco) {
                continue;
            }
            if (visivel(s.getUnidadeExecutante(), visiveis)) {
                return Optional.of("Regulação: unidade executante " + s.getUnidadeExecutante().getNome());
            }
            if (visivel(s.getUnidadeSolicitante(), visiveis)) {
                return Optional.of("Regulação: unidade solicitante " + s.getUnidadeSolicitante().getNome());
            }
        }

        // 5. Internação em curso, ou com alta dentro da janela: a equipe da unidade e o médico responsável (ADR-0100).
        for (Internacao i : internacaoRepository.findByPaciente_UuidOrderByAdmitidaEmDesc(pacienteId)) {
            boolean vigente = i.getStatus() == StatusInternacao.INTERNADO
                    || (i.getAltaEm() != null && i.getAltaEm().isAfter(limiteRealizacao));
            if (!vigente) {
                continue;
            }
            if (visivel(i.getUnidade(), visiveis)) {
                return Optional.of("Internação na unidade " + i.getUnidade().getNome());
            }
            if (i.getMedicoResponsavel() != null && cpf.equals(i.getMedicoResponsavel().getCpf())) {
                return Optional.of("Médico responsável pela internação " + i.getUuid());
            }
        }
        return Optional.empty();
    }

    private static boolean visivel(UnidadeDeSaude unidade, Optional<Set<UUID>> visiveis) {
        return unidade != null && visiveis.map(ids -> ids.contains(unidade.getUuid())).orElse(true);
    }

    private static AcessoProntuarioDto registrar(AcessoProntuarioDto acesso) {
        ContextoAuditoria.detalhe(acesso.getDescricao());
        return acesso;
    }
}
