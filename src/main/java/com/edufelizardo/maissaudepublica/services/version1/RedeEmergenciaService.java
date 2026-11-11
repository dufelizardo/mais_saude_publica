package com.edufelizardo.maissaudepublica.services.version1;

import com.edufelizardo.maissaudepublica.config.ContextoAuditoria;
import com.edufelizardo.maissaudepublica.exceptions.ResourceBadRequestException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceConflictException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceNotFoundException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceUnprocessableEntityException;
import com.edufelizardo.maissaudepublica.models.Agendamento;
import com.edufelizardo.maissaudepublica.models.AvaliacaoImpacto;
import com.edufelizardo.maissaudepublica.models.Emergencia;
import com.edufelizardo.maissaudepublica.models.Lote;
import com.edufelizardo.maissaudepublica.models.LoteMaterial;
import com.edufelizardo.maissaudepublica.models.SolicitacaoRegulacao;
import com.edufelizardo.maissaudepublica.models.UnidadeDeSaude;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.RedeEmergenciaDtos;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoEmergencia;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoOperacional;
import com.edufelizardo.maissaudepublica.models.enuns.StatusAgendamento;
import com.edufelizardo.maissaudepublica.models.enuns.StatusSolicitacaoRegulacao;
import com.edufelizardo.maissaudepublica.repositories.AgendamentoRepository;
import com.edufelizardo.maissaudepublica.repositories.AvaliacaoImpactoRepository;
import com.edufelizardo.maissaudepublica.repositories.EmergenciaRepository;
import com.edufelizardo.maissaudepublica.repositories.LoteMaterialRepository;
import com.edufelizardo.maissaudepublica.repositories.LoteRepository;
import com.edufelizardo.maissaudepublica.repositories.SolicitacaoRegulacaoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * A rede na emergência, fatia E4b (ADR-0194): onde tem o medicamento (Farmácia) ou o material (Estoque) na rede, os compromissos nas
 * unidades afetadas fora de operação (para a Agenda e a Regulação redirecionarem) e a prioridade da emergência na Regulação. A coordenação
 * aponta; o dono executa.
 */
@Service
public class RedeEmergenciaService {

    private static final Set<StatusSolicitacaoRegulacao> ABERTAS = Set.of(StatusSolicitacaoRegulacao.SOLICITADA, StatusSolicitacaoRegulacao.DEVOLVIDA,
            StatusSolicitacaoRegulacao.AUTORIZADA, StatusSolicitacaoRegulacao.AGENDADA);

    @Autowired private EmergenciaRepository emergenciaRepository;
    @Autowired private AvaliacaoImpactoRepository avaliacaoRepository;
    @Autowired private LoteRepository loteRepository;
    @Autowired private LoteMaterialRepository loteMaterialRepository;
    @Autowired private AgendamentoRepository agendamentoRepository;
    @Autowired private SolicitacaoRegulacaoRepository regulacaoRepository;
    @Autowired private EstoqueService estoqueService;
    @Autowired private PessoasEmergenciaService pessoasService;
    @Autowired private ControleDeAcesso controleDeAcesso;

    /** Onde tem: por unidade com saldo dentro da validade, as afetadas marcadas. Pede o medicamento ou o material, um só (400). */
    @Transactional(readOnly = true)
    public List<RedeEmergenciaDtos.Disponibilidade> disponibilidade(UUID emergenciaId, UUID medicamentoId, UUID materialId) {
        controleDeAcesso.exigirAlguma(PessoasEmergenciaService.COORDENAR);
        if ((medicamentoId == null) == (materialId == null)) {
            throw new ResourceBadRequestException("Informe o medicamento ou o material, um só.");
        }
        Set<UUID> afetadas = afetadas(emergencia(emergenciaId)).stream().map(UnidadeDeSaude::getUuid).collect(Collectors.toSet());
        LocalDate hoje = LocalDate.now();
        Map<UUID, UnidadeDeSaude> unidades = new HashMap<>();
        Map<UUID, Integer> saldo = new HashMap<>();
        Map<UUID, LocalDate> validade = new HashMap<>();
        if (medicamentoId != null) {
            for (Lote l : loteRepository.findByMedicamentoUuid(medicamentoId)) {
                if (l.getLoteIncorporador() != null || l.getQuantidade() == null || l.getQuantidade() <= 0
                        || (l.getValidade() != null && l.getValidade().isBefore(hoje))) {
                    continue;
                }
                acumular(l.getUnidade(), l.getQuantidade(), l.getValidade(), unidades, saldo, validade);
            }
        } else {
            for (LoteMaterial l : loteMaterialRepository.findTodos().stream().filter(x -> x.getMaterial().getUuid().equals(materialId)).toList()) {
                if (l.getQuantidade() <= 0 || l.vencido(hoje)) {
                    continue;
                }
                acumular(l.getUnidade(), l.getQuantidade(), l.getValidade(), unidades, saldo, validade);
            }
        }
        return unidades.values().stream().map(u -> {
                    int s = saldo.get(u.getUuid());
                    int reservado = materialId != null ? estoqueService.reservado(u.getUuid(), materialId, null) : 0;
                    return new RedeEmergenciaDtos.Disponibilidade(u.getUuid(), u.getNome(), u.getSituacaoOperacional(), afetadas.contains(u.getUuid()), s,
                            reservado, Math.max(0, s - reservado), validade.get(u.getUuid()));
                })
                .sorted(Comparator.comparing(RedeEmergenciaDtos.Disponibilidade::afetada).thenComparing(d -> -d.livre()))
                .toList();
    }

    /**
     * Os compromissos dos próximos 30 dias nas unidades afetadas fora de operação (agendamentos agendados ou confirmados e solicitações
     * reguladas autorizadas ou agendadas com a executante afetada) e as regulações que a emergência priorizou.
     */
    @Transactional(readOnly = true)
    public RedeEmergenciaDtos.Rede rede(UUID emergenciaId) {
        exigirAcesso();
        Emergencia em = emergencia(emergenciaId);
        Map<UUID, UnidadeDeSaude> fora = afetadas(em).stream().filter(u -> u.getSituacaoOperacional() != SituacaoOperacional.EM_OPERACAO)
                .collect(Collectors.toMap(UnidadeDeSaude::getUuid, u -> u, (a, b) -> a));
        LocalDateTime agora = LocalDateTime.now();
        LocalDateTime ate = agora.plusDays(30);
        List<RedeEmergenciaDtos.Compromisso> compromissos = new ArrayList<>();
        if (!fora.isEmpty()) {
            for (Agendamento a : agendamentoRepository.findAll()) {
                if (a.getUnidade() == null || !fora.containsKey(a.getUnidade().getUuid()) || a.getDataHora().isBefore(agora) || a.getDataHora().isAfter(ate)
                        || (a.getStatus() != StatusAgendamento.AGENDADO && a.getStatus() != StatusAgendamento.CONFIRMADO)) {
                    continue;
                }
                compromissos.add(new RedeEmergenciaDtos.Compromisso("AGENDAMENTO", a.getUuid(), a.getPaciente().getUuid(), a.getPaciente().getNome(),
                        a.getUnidade().getUuid(), a.getUnidade().getNome(), a.getDataHora(),
                        a.getEspecialidade() != null ? a.getEspecialidade().getNome() : a.getTipo().name(), a.getStatus().name()));
            }
            for (SolicitacaoRegulacao s : regulacaoRepository.findAll()) {
                LocalDateTime quando = s.getAgendamento() != null ? s.getAgendamento().getDataHora() : s.getDataHoraPrevista();
                if (s.getUnidadeExecutante() == null || !fora.containsKey(s.getUnidadeExecutante().getUuid()) || quando == null || quando.isBefore(agora)
                        || quando.isAfter(ate) || (s.getStatus() != StatusSolicitacaoRegulacao.AUTORIZADA && s.getStatus() != StatusSolicitacaoRegulacao.AGENDADA)) {
                    continue;
                }
                compromissos.add(new RedeEmergenciaDtos.Compromisso("REGULACAO", s.getUuid(), s.getPaciente().getUuid(), s.getPaciente().getNome(),
                        s.getUnidadeExecutante().getUuid(), s.getUnidadeExecutante().getNome(), quando, s.getProcedimento().getNome(), s.getStatus().name()));
            }
        }
        compromissos.sort(Comparator.comparing(RedeEmergenciaDtos.Compromisso::dataHora));
        List<RedeEmergenciaDtos.Regulacao> priorizadas = regulacaoRepository.findAll().stream()
                .filter(s -> s.getEmergencia() != null && s.getEmergencia().getUuid().equals(em.getUuid()))
                .sorted(Comparator.comparing(SolicitacaoRegulacao::getSolicitadoEm)).map(s -> regulacao(s, true)).toList();
        ContextoAuditoria.registro(em.getUuid());
        return new RedeEmergenciaDtos.Rede(compromissos, priorizadas);
    }

    /** As solicitações reguladas em aberto do paciente, para a coordenação escolher qual priorizar. */
    @Transactional(readOnly = true)
    public List<RedeEmergenciaDtos.Regulacao> regulacoesDoPaciente(UUID emergenciaId, UUID pacienteId) {
        exigirAcesso();
        Emergencia em = emergencia(emergenciaId);
        return regulacaoRepository.findByPaciente_Uuid(pacienteId).stream().filter(s -> ABERTAS.contains(s.getStatus()))
                .sorted(Comparator.comparing(SolicitacaoRegulacao::getSolicitadoEm))
                .map(s -> regulacao(s, s.getEmergencia() != null && s.getEmergencia().getUuid().equals(em.getUuid()))).toList();
    }

    /**
     * Marca a solicitação regulada com a emergência ativa (422): em aberto (422), do paciente deslocado ou morador da área afetada (422), sem a
     * marca de outra emergência aberta (409). Sobe na fila dentro da mesma cor; quem decide é o regulador.
     */
    @Transactional
    public RedeEmergenciaDtos.Rede priorizar(UUID emergenciaId, UUID solicitacaoId) {
        exigirAcesso();
        Emergencia em = emergencia(emergenciaId);
        if (em.getSituacao() != SituacaoEmergencia.ATIVA) {
            throw new ResourceUnprocessableEntityException("A emergência não está ativa: a prioridade nova entra só durante a resposta.");
        }
        SolicitacaoRegulacao s = regulacaoRepository.findByIdParaAtualizar(solicitacaoId).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar a solicitação " + solicitacaoId + " em nossos registros."));
        if (!ABERTAS.contains(s.getStatus())) {
            throw new ResourceUnprocessableEntityException("A solicitação já está " + s.getStatus().name().toLowerCase() + ".");
        }
        if (s.daEmergenciaAberta()) {
            if (s.getEmergencia().getUuid().equals(em.getUuid())) {
                throw new ResourceConflictException("A solicitação já está priorizada por esta emergência.");
            }
            throw new ResourceConflictException("A solicitação já está priorizada pela emergência " + s.getEmergencia().getNome() + ".");
        }
        if (!pessoasService.pertence(em, s.getPaciente().getUuid())) {
            throw new ResourceUnprocessableEntityException("O paciente não está deslocado nem mora na área afetada desta emergência.");
        }
        s.setEmergencia(em);
        regulacaoRepository.save(s);
        ContextoAuditoria.registro(em.getUuid());
        ContextoAuditoria.detalhe("Regulação " + s.getUuid() + " priorizada pela emergência.");
        return rede(emergenciaId);
    }

    /** Tira a marca (de outra emergência ou sem marca, 422). Vale também na desmobilização. */
    @Transactional
    public RedeEmergenciaDtos.Rede retirar(UUID emergenciaId, UUID solicitacaoId) {
        exigirAcesso();
        Emergencia em = emergencia(emergenciaId);
        SolicitacaoRegulacao s = regulacaoRepository.findByIdParaAtualizar(solicitacaoId).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar a solicitação " + solicitacaoId + " em nossos registros."));
        if (s.getEmergencia() == null || !s.getEmergencia().getUuid().equals(em.getUuid())) {
            throw new ResourceUnprocessableEntityException("A solicitação não está priorizada por esta emergência.");
        }
        s.setEmergencia(null);
        regulacaoRepository.save(s);
        ContextoAuditoria.registro(em.getUuid());
        ContextoAuditoria.detalhe("Prioridade da emergência retirada da regulação " + s.getUuid() + ".");
        return rede(emergenciaId);
    }

    /** Para o relatório pós-evento: quantas solicitações a emergência priorizou. */
    int priorizadas(Emergencia em) {
        return (int) regulacaoRepository.findAll().stream().filter(s -> s.getEmergencia() != null && s.getEmergencia().getUuid().equals(em.getUuid())).count();
    }

    // ── Apoio ─────────────────────────────────────────────────────────────────────────────────────

    private static void acumular(UnidadeDeSaude u, int quantidade, LocalDate validade, Map<UUID, UnidadeDeSaude> unidades, Map<UUID, Integer> saldo,
                                 Map<UUID, LocalDate> proxima) {
        unidades.put(u.getUuid(), u);
        saldo.merge(u.getUuid(), quantidade, Integer::sum);
        if (validade != null) {
            proxima.merge(u.getUuid(), validade, (a, b) -> a.isBefore(b) ? a : b);
        }
    }

    private List<UnidadeDeSaude> afetadas(Emergencia em) {
        AvaliacaoImpacto ultima = avaliacaoRepository.findDaEmergencia(em.getUuid()).stream().findFirst().orElse(null);
        return ultima == null ? List.of() : new ArrayList<>(ultima.getUnidadesAfetadas());
    }

    private static RedeEmergenciaDtos.Regulacao regulacao(SolicitacaoRegulacao s, boolean priorizada) {
        LocalDateTime quando = s.getAgendamento() != null ? s.getAgendamento().getDataHora() : s.getDataHoraPrevista();
        return new RedeEmergenciaDtos.Regulacao(s.getUuid(), s.getPaciente().getUuid(), s.getPaciente().getNome(), s.getProcedimento().getNome(),
                s.getPrioridade(), s.getStatus().name(), s.getUnidadeSolicitante().getNome(),
                s.getUnidadeExecutante() != null ? s.getUnidadeExecutante().getNome() : null, quando, priorizada);
    }

    private void exigirAcesso() {
        controleDeAcesso.exigirAlguma(PessoasEmergenciaService.COORDENAR);
        controleDeAcesso.exigirAlguma(PessoasEmergenciaService.PACIENTE);
    }

    private Emergencia emergencia(UUID id) {
        return emergenciaRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar a emergência " + id + " em nossos registros."));
    }
}
