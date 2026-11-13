package com.edufelizardo.maissaudepublica.services.version1;

import com.edufelizardo.maissaudepublica.config.ContextoAuditoria;
import com.edufelizardo.maissaudepublica.config.UsuarioAutenticado;
import com.edufelizardo.maissaudepublica.exceptions.ResourceConflictException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceNotFoundException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceUnprocessableEntityException;
import com.edufelizardo.maissaudepublica.models.CessaoVeiculo;
import com.edufelizardo.maissaudepublica.models.Emergencia;
import com.edufelizardo.maissaudepublica.models.Mobilizacao;
import com.edufelizardo.maissaudepublica.models.UnidadeDeSaude;
import com.edufelizardo.maissaudepublica.models.Veiculo;
import com.edufelizardo.maissaudepublica.models.ViagemTransporte;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.CessaoVeiculoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.FrotaEmergenciaDtos;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoEmergencia;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoMobilizacao;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoVeiculo;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoViagem;
import com.edufelizardo.maissaudepublica.models.enuns.TipoMobilizacao;
import com.edufelizardo.maissaudepublica.models.enuns.TipoVeiculo;
import com.edufelizardo.maissaudepublica.repositories.CessaoVeiculoRepository;
import com.edufelizardo.maissaudepublica.repositories.MobilizacaoRepository;
import com.edufelizardo.maissaudepublica.repositories.VeiculoRepository;
import com.edufelizardo.maissaudepublica.repositories.ViagemTransporteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Os veículos nas emergências, fatia T3 (ADR-0192): a coordenação pede o veículo pela mobilização (E3) e a frota cede, com
 * {@code TRANSPORTE.GERENCIAR} na unidade do veículo. Cedido, o veículo fica em serviço pelo mecanismo da T1 ({@link TransporteService});
 * devolvido, volta a disponível com a quilometragem. A lotação não muda.
 */
@Service
public class FrotaEmergenciaService {

    private static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("dd/MM HH:mm");

    @Autowired private CessaoVeiculoRepository cessaoRepository;
    @Autowired private MobilizacaoRepository mobilizacaoRepository;
    @Autowired private VeiculoRepository veiculoRepository;
    @Autowired private ViagemTransporteRepository viagemRepository;
    @Autowired private TransporteService transporteService;
    @Autowired private ControleDeAcesso controleDeAcesso;

    /** Os pedidos de veículo das emergências ativas: os abertos e os que ainda têm veículo cedido, com as cessões. */
    @Transactional(readOnly = true)
    public List<FrotaEmergenciaDtos.Pedido> pedidos() {
        controleDeAcesso.exigirAlguma(TransporteService.CONSULTAR, TransporteService.GERENCIAR);
        List<Mobilizacao> mobs = mobilizacaoRepository.findDeVeiculo().stream()
                .filter(m -> m.getEmergencia().getSituacao() == SituacaoEmergencia.ATIVA || m.getEmergencia().getSituacao() == SituacaoEmergencia.EM_DESMOBILIZACAO)
                .toList();
        Map<UUID, List<FrotaEmergenciaDtos.Cessao>> cessoes = cessoes(mobs.stream().map(Mobilizacao::getUuid).toList());
        return mobs.stream()
                .filter(m -> aberta(m) || cessoes.getOrDefault(m.getUuid(), List.of()).stream().anyMatch(c -> c.devolvidaEm() == null))
                .map(m -> pedido(m, cessoes.getOrDefault(m.getUuid(), List.of()))).toList();
    }

    /**
     * Cede o veículo ao pedido: a mobilização de veículo (422), aberta (422), da emergência ativa (422), sem passar da quantidade pedida
     * (422). O veículo já cedido, 409; inativo, em manutenção ou em serviço, 422. Fica em serviço; as viagens eletivas programadas dele
     * voltam como aviso.
     */
    @Transactional
    public FrotaEmergenciaDtos.CessaoFeita ceder(UUID mobilizacaoId, CessaoVeiculoRequestDto dto) {
        Veiculo v = veiculoRepository.findByIdParaAtualizar(dto.getVeiculoId()).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar o veículo " + dto.getVeiculoId() + " em nossos registros."));
        controleDeAcesso.exigir(TransporteService.GERENCIAR, v.getUnidade());
        Mobilizacao m = mobilizacaoRepository.findById(mobilizacaoId).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar a mobilização " + mobilizacaoId + " em nossos registros."));
        if (m.getTipo() != TipoMobilizacao.VEICULO) {
            throw new ResourceUnprocessableEntityException("A mobilização não é de veículo.");
        }
        if (m.getEmergencia().getSituacao() != SituacaoEmergencia.ATIVA) {
            throw new ResourceUnprocessableEntityException("A emergência " + m.getEmergencia().getNome() + " não está ativa.");
        }
        if (!aberta(m)) {
            throw new ResourceUnprocessableEntityException("O pedido já foi " + (m.getSituacao() == SituacaoMobilizacao.CONCLUIDA ? "concluído." : "cancelado."));
        }
        cessaoRepository.findAbertaDoVeiculo(v.getUuid()).ifPresent(c -> {
            throw new ResourceConflictException("O veículo " + v.getPrefixo() + " já está cedido à emergência " + c.getMobilizacao().getEmergencia().getNome() + ".");
        });
        long abertas = cessaoRepository.findDasMobilizacoes(List.of(m.getUuid())).stream().filter(CessaoVeiculo::aberta).count();
        if (m.getQuantidade() != null && abertas >= m.getQuantidade()) {
            throw new ResourceUnprocessableEntityException("O pedido já tem os " + m.getQuantidade() + " veículo(s) que pediu.");
        }
        if (v.getSituacao() != SituacaoVeiculo.DISPONIVEL) {
            throw new ResourceUnprocessableEntityException("O veículo " + v.getPrefixo() + " não está disponível (" + v.getSituacao() + ").");
        }
        transporteService.colocarEmServico(v, "Cedido à emergência " + m.getEmergencia().getNome() + ".");
        CessaoVeiculo c = cessaoRepository.save(new CessaoVeiculo(null, m, v, Instant.now(), null, v.getQuilometragem(), null,
                texto(dto.getObservacao()), UsuarioAutenticado.cpf(), null));
        if (m.getSituacao() == SituacaoMobilizacao.SOLICITADA) {
            m.setSituacao(SituacaoMobilizacao.EM_ANDAMENTO);
            mobilizacaoRepository.save(m);
        }
        ContextoAuditoria.unidade(v.getUnidade());
        ContextoAuditoria.registro(c.getUuid());
        ContextoAuditoria.detalhe("Veículo " + v.getPrefixo() + " cedido à emergência " + m.getEmergencia().getNome() + ".");
        return new FrotaEmergenciaDtos.CessaoFeita(cessao(c), avisos(v, m));
    }

    /** Devolve o veículo (já devolvido, 422): a quilometragem não volta (422) e atualiza o veículo, que volta a disponível. */
    @Transactional
    public FrotaEmergenciaDtos.Cessao devolver(UUID cessaoId, CessaoVeiculoRequestDto.Devolucao dto) {
        CessaoVeiculo c = cessaoRepository.findById(cessaoId).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar a cessão " + cessaoId + " em nossos registros."));
        controleDeAcesso.exigir(TransporteService.GERENCIAR, c.getVeiculo().getUnidade());
        if (!c.aberta()) {
            throw new ResourceUnprocessableEntityException("O veículo já foi devolvido.");
        }
        fechar(c, dto.getQuilometragem(), texto(dto.getObservacao()));
        ContextoAuditoria.unidade(c.getVeiculo().getUnidade());
        ContextoAuditoria.registro(c.getUuid());
        ContextoAuditoria.detalhe("Veículo " + c.getVeiculo().getPrefixo() + " devolvido pela emergência.");
        return cessao(c);
    }

    // ── Para a coordenação da emergência ──────────────────────────────────────────────────────────

    /** Concluir, cancelar ou desmobilizar devolve os veículos ainda cedidos ao pedido, sem a quilometragem; devolve quantos. */
    int devolverDaMobilizacao(Mobilizacao m, String motivo) {
        int n = 0;
        for (CessaoVeiculo c : cessaoRepository.findDasMobilizacoes(List.of(m.getUuid()))) {
            if (c.aberta()) {
                fechar(c, null, motivo.length() > 500 ? motivo.substring(0, 500) : motivo);
                n++;
            }
        }
        return n;
    }

    /** As cessões de cada mobilização. */
    Map<UUID, List<FrotaEmergenciaDtos.Cessao>> cessoes(Collection<UUID> mobilizacaoIds) {
        if (mobilizacaoIds.isEmpty()) {
            return Map.of();
        }
        return cessaoRepository.findDasMobilizacoes(mobilizacaoIds).stream().map(FrotaEmergenciaService::cessao)
                .collect(Collectors.groupingBy(FrotaEmergenciaDtos.Cessao::mobilizacaoId));
    }

    /** Os cedidos à emergência, a frota das unidades afetadas e os disponíveis da rede por tipo. */
    FrotaEmergenciaDtos.Frota frota(Emergencia em, List<Mobilizacao> mobilizacoes, Collection<UnidadeDeSaude> afetadas) {
        List<FrotaEmergenciaDtos.Cessao> cedidos = cessoes(mobilizacoes.stream().filter(m -> m.getTipo() == TipoMobilizacao.VEICULO)
                .map(Mobilizacao::getUuid).toList()).values().stream().flatMap(List::stream)
                .sorted(Comparator.comparing((FrotaEmergenciaDtos.Cessao c) -> c.devolvidaEm() != null).thenComparing(FrotaEmergenciaDtos.Cessao::cedidaEm))
                .toList();
        Map<UUID, String> cedidoA = cessaoRepository.findAbertas().stream()
                .collect(Collectors.toMap(c -> c.getVeiculo().getUuid(), c -> c.getMobilizacao().getEmergencia().getNome(), (a, b) -> a));
        Set<UUID> idsAfetadas = afetadas.stream().map(UnidadeDeSaude::getUuid).collect(Collectors.toSet());
        List<Veiculo> todos = veiculoRepository.findTodos();
        List<FrotaEmergenciaDtos.Veiculo> naArea = todos.stream().filter(v -> idsAfetadas.contains(v.getUnidade().getUuid()))
                .sorted(Comparator.comparing((Veiculo v) -> v.getUnidade().getNome(), String.CASE_INSENSITIVE_ORDER).thenComparing(Veiculo::getPrefixo))
                .map(v -> new FrotaEmergenciaDtos.Veiculo(v.getUuid(), v.getPrefixo(), v.getTipo(), v.getUnidade().getNome(), v.getSituacao(),
                        cedidoA.get(v.getUuid()))).toList();
        Map<TipoVeiculo, Integer> porTipo = new EnumMap<>(TipoVeiculo.class);
        todos.stream().filter(v -> v.getSituacao() == SituacaoVeiculo.DISPONIVEL).forEach(v -> porTipo.merge(v.getTipo(), 1, Integer::sum));
        return new FrotaEmergenciaDtos.Frota(cedidos, naArea, porTipo.entrySet().stream()
                .map(e -> new FrotaEmergenciaDtos.Disponiveis(e.getKey(), e.getValue())).toList());
    }

    /** Para o relatório pós-evento: {veículos distintos, dias de uso (uma casa), quilômetros (nulo sem registro)}. */
    Object[] totais(Emergencia em, List<Mobilizacao> mobilizacoes) {
        List<UUID> ids = mobilizacoes.stream().filter(m -> m.getTipo() == TipoMobilizacao.VEICULO).map(Mobilizacao::getUuid).toList();
        if (ids.isEmpty()) {
            return new Object[]{0, 0.0, null};
        }
        List<CessaoVeiculo> lista = cessaoRepository.findDasMobilizacoes(ids);
        Instant agora = Instant.now();
        long minutos = lista.stream().mapToLong(c -> Duration.between(c.getCedidaEm(), c.getDevolvidaEm() != null ? c.getDevolvidaEm() : agora).toMinutes()).sum();
        List<CessaoVeiculo> comKm = lista.stream().filter(c -> c.getKmInicio() != null && c.getKmFim() != null).toList();
        Integer km = comKm.isEmpty() ? null : comKm.stream().mapToInt(c -> c.getKmFim() - c.getKmInicio()).sum();
        double dias = BigDecimal.valueOf(minutos / 1440.0).setScale(1, RoundingMode.HALF_UP).doubleValue();
        return new Object[]{(int) lista.stream().map(c -> c.getVeiculo().getUuid()).distinct().count(), dias, km};
    }

    /** O veículo cedido a uma emergência não entra em viagem eletiva nova (409). */
    void exigirNaoCedido(Veiculo v) {
        cessaoRepository.findAbertaDoVeiculo(v.getUuid()).ifPresent(c -> {
            throw new ResourceConflictException("O veículo " + v.getPrefixo() + " está cedido à emergência " + c.getMobilizacao().getEmergencia().getNome()
                    + "; a frota devolve antes de programar viagem.");
        });
    }

    // ── Apoio ─────────────────────────────────────────────────────────────────────────────────────

    private void fechar(CessaoVeiculo c, Integer km, String observacao) {
        Veiculo v = veiculoRepository.findByIdParaAtualizar(c.getVeiculo().getUuid()).orElseThrow();
        if (km != null) {
            if (c.getKmInicio() != null && km < c.getKmInicio()) {
                throw new ResourceUnprocessableEntityException("A quilometragem não pode voltar: na cessão estava em " + c.getKmInicio() + ".");
            }
            if (v.getQuilometragem() != null && km < v.getQuilometragem()) {
                throw new ResourceUnprocessableEntityException("A quilometragem não pode voltar: o veículo está em " + v.getQuilometragem() + ".");
            }
            v.setQuilometragem(km);
            veiculoRepository.save(v);
            c.setKmFim(km);
        }
        c.setDevolvidaEm(Instant.now());
        c.setDevolvidaPorCpf(UsuarioAutenticado.cpf());
        if (observacao != null) {
            c.setObservacao(observacao);
        }
        cessaoRepository.save(c);
        transporteService.liberar(v, "Devolvido pela emergência " + c.getMobilizacao().getEmergencia().getNome() + ".");
    }

    /** As viagens eletivas programadas do veículo nos próximos 30 dias (ou até o fim do pedido), para a frota reprogramar. */
    private List<String> avisos(Veiculo v, Mobilizacao m) {
        Instant agora = Instant.now();
        Instant ate = agora.plus(Duration.ofDays(30));
        if (m.getFim() != null) {
            Instant fim = m.getFim().plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant();
            ate = fim.isBefore(ate) ? fim : ate;
        }
        return viagemRepository.findNoPeriodo(agora, ate).stream()
                .filter(x -> x.getSituacao() == SituacaoViagem.PROGRAMADA && x.getVeiculo().getUuid().equals(v.getUuid()))
                .sorted(Comparator.comparing(ViagemTransporte::getSaidaPrevista))
                .map(x -> "A viagem de " + x.getSaidaPrevista().atZone(ZoneId.systemDefault()).format(HORA) + " para " + x.getDestino()
                        + " usa o " + v.getPrefixo() + ": reprograme com outro veículo.").toList();
    }

    private FrotaEmergenciaDtos.Pedido pedido(Mobilizacao m, List<FrotaEmergenciaDtos.Cessao> cessoes) {
        long abertas = cessoes.stream().filter(c -> c.devolvidaEm() == null).count();
        Integer faltam = m.getQuantidade() == null ? null : (int) Math.max(0, m.getQuantidade() - abertas);
        return new FrotaEmergenciaDtos.Pedido(m.getUuid(), m.getEmergencia().getUuid(), m.getEmergencia().getNome(), m.getDestino().getUuid(),
                m.getDestino().getNome(), m.getRecurso(), m.getQuantidade(), m.getResponsavel(), m.getInicio(), m.getFim(), m.getSituacao(), cessoes, faltam);
    }

    private static FrotaEmergenciaDtos.Cessao cessao(CessaoVeiculo c) {
        Veiculo v = c.getVeiculo();
        Mobilizacao m = c.getMobilizacao();
        return new FrotaEmergenciaDtos.Cessao(c.getUuid(), m.getUuid(), m.getEmergencia().getUuid(), m.getEmergencia().getNome(), v.getUuid(),
                v.getPrefixo(), v.getPlaca(), v.getTipo(), v.getUnidade().getNome(), c.getCedidaEm(), c.getDevolvidaEm(), c.getKmInicio(), c.getKmFim(),
                c.getObservacao());
    }

    private static boolean aberta(Mobilizacao m) {
        return m.getSituacao() == SituacaoMobilizacao.SOLICITADA || m.getSituacao() == SituacaoMobilizacao.EM_ANDAMENTO;
    }

    private static String texto(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }
}
