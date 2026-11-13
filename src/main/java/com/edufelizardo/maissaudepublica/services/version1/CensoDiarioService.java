package com.edufelizardo.maissaudepublica.services.version1;

import com.edufelizardo.maissaudepublica.config.ContextoAuditoria;
import com.edufelizardo.maissaudepublica.config.UsuarioAutenticado;
import com.edufelizardo.maissaudepublica.exceptions.ResourceBadRequestException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceConflictException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceNotFoundException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceUnprocessableEntityException;
import com.edufelizardo.maissaudepublica.models.CensoDiario;
import com.edufelizardo.maissaudepublica.models.CensoDiarioPaciente;
import com.edufelizardo.maissaudepublica.models.EventoLeito;
import com.edufelizardo.maissaudepublica.models.Internacao;
import com.edufelizardo.maissaudepublica.models.Leito;
import com.edufelizardo.maissaudepublica.models.UnidadeDeSaude;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.EletivaCensoDtos;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoLeito;
import com.edufelizardo.maissaudepublica.models.enuns.TipoAlta;
import com.edufelizardo.maissaudepublica.models.enuns.TipoEventoLeito;
import com.edufelizardo.maissaudepublica.repositories.CensoDiarioPacienteRepository;
import com.edufelizardo.maissaudepublica.repositories.CensoDiarioRepository;
import com.edufelizardo.maissaudepublica.repositories.EventoLeitoRepository;
import com.edufelizardo.maissaudepublica.repositories.InternacaoRepository;
import com.edufelizardo.maissaudepublica.repositories.LeitoRepository;
import com.edufelizardo.maissaudepublica.repositories.UnidadeDeSaudeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

/**
 * Censo hospitalar diário (ADR-0185): os números de um dia já terminado, calculados das internações e dos eventos de leito, fechados
 * e guardados com o censo nominal. Um censo vigente por unidade e dia (409); corrigir é reabrir com motivo e fechar outra versão.
 * Os leitos (existentes e bloqueados) são os da hora do fechamento: o histórico de cadastro do leito não é guardado.
 */
@Service
public class CensoDiarioService {

    public static final String FECHAR = "CENSO.FECHAR";
    static final int DIAS_DE_PENDENCIA = 30;

    @Autowired
    private CensoDiarioRepository censoRepository;

    @Autowired
    private CensoDiarioPacienteRepository pacienteRepository;

    @Autowired
    private InternacaoRepository internacaoRepository;

    @Autowired
    private EventoLeitoRepository eventoLeitoRepository;

    @Autowired
    private LeitoRepository leitoRepository;

    @Autowired
    private UnidadeDeSaudeRepository unidadeRepository;

    @Autowired
    private ControleDeAcesso controleDeAcesso;

    /** O censo vigente do dia, se fechado; senão, a prévia calculada (sem uuid). */
    @Transactional(readOnly = true)
    public EletivaCensoDtos.Censo doDia(UUID unidadeId, LocalDate data) {
        UnidadeDeSaude u = unidade(unidadeId);
        controleDeAcesso.exigirVisivel(u, Stream.concat(Stream.of(LeitoService.VER), Stream.of(FECHAR)).toArray(String[]::new));
        exigirDiaTerminado(data);
        return censoRepository.findByUnidade_UuidAndDataAndReabertoFalse(u.getUuid(), data)
                .map(c -> EletivaCensoDtos.Censo.from(c, pacienteRepository.findByCenso_UuidOrderByLeitoIdentificacaoAsc(c.getUuid())))
                .orElseGet(() -> previa(calcular(u, data)));
    }

    /** Fecha o censo do dia (só dia terminado, 422; já fechado, 409). */
    @Transactional
    public EletivaCensoDtos.Censo fechar(UUID unidadeId, LocalDate data) {
        UnidadeDeSaude u = unidade(unidadeId);
        controleDeAcesso.exigir(FECHAR, u);
        exigirDiaTerminado(data);
        if (censoRepository.findByUnidade_UuidAndDataAndReabertoFalse(u.getUuid(), data).isPresent()) {
            throw new ResourceConflictException("O censo deste dia já está fechado; para corrigir, reabra com o motivo.");
        }
        Calculo calc = calcular(u, data);
        CensoDiario c = calc.censo;
        c.setVersao(censoRepository.findByUnidade_UuidAndDataOrderByVersaoDesc(u.getUuid(), data).stream().findFirst()
                .map(x -> x.getVersao() + 1).orElse(1));
        c.setFechadoEm(Instant.now());
        c.setFechadoPorCpf(UsuarioAutenticado.cpf());
        c = censoRepository.save(c);
        for (CensoDiarioPaciente p : calc.pacientes) {
            p.setCenso(c);
        }
        pacienteRepository.saveAll(calc.pacientes);
        ContextoAuditoria.registro(c.getUuid());
        return EletivaCensoDtos.Censo.from(c, calc.pacientes);
    }

    /** Reabre o censo vigente do dia, com motivo (400 sem ele; 422 se não houver censo fechado). A versão reaberta fica guardada. */
    @Transactional
    public EletivaCensoDtos.Censo reabrir(UUID unidadeId, LocalDate data, String motivo) {
        UnidadeDeSaude u = unidade(unidadeId);
        controleDeAcesso.exigir(FECHAR, u);
        if (motivo == null || motivo.isBlank()) {
            throw new ResourceBadRequestException("Informe o motivo da reabertura.");
        }
        CensoDiario c = censoRepository.findByUnidade_UuidAndDataAndReabertoFalse(u.getUuid(), data).orElseThrow(() ->
                new ResourceUnprocessableEntityException("Não há censo fechado neste dia para reabrir."));
        c.setReaberto(true);
        c.setReabertoEm(Instant.now());
        c.setReabertoPorCpf(UsuarioAutenticado.cpf());
        c.setMotivoReabertura(motivo.trim());
        censoRepository.saveAndFlush(c);
        ContextoAuditoria.registro(c.getUuid());
        return previa(calcular(u, data));
    }

    /** As versões do dia, da mais nova para a mais velha (a reaberta fica com o motivo). */
    @Transactional(readOnly = true)
    public List<EletivaCensoDtos.Censo> versoes(UUID unidadeId, LocalDate data) {
        UnidadeDeSaude u = unidade(unidadeId);
        controleDeAcesso.exigirVisivel(u, Stream.concat(Stream.of(LeitoService.VER), Stream.of(FECHAR)).toArray(String[]::new));
        return censoRepository.findByUnidade_UuidAndDataOrderByVersaoDesc(u.getUuid(), data).stream()
                .map(c -> EletivaCensoDtos.Censo.from(c, List.of())).toList();
    }

    /** Os dias terminados dos últimos 30, sem censo fechado, das unidades com leito no escopo. */
    @Transactional(readOnly = true)
    public List<EletivaCensoDtos.Pendencia> pendencias(UUID unidadeId) {
        List<UnidadeDeSaude> unidades = controleDeAcesso.filtrar(leitoRepository.findAllByOrderByIdentificacaoAsc().stream()
                                .filter(Leito::isAtivo).map(Leito::getUnidade)
                                .filter(u -> unidadeId == null || u.getUuid().equals(unidadeId)),
                        u -> Stream.of(u), FECHAR)
                .collect(java.util.stream.Collectors.toMap(UnidadeDeSaude::getUuid, u -> u, (a, b) -> a, java.util.LinkedHashMap::new))
                .values().stream().toList();
        LocalDate ontem = LocalDate.now().minusDays(1);
        List<EletivaCensoDtos.Pendencia> pendentes = new ArrayList<>();
        for (UnidadeDeSaude u : unidades) {
            Set<LocalDate> fechados = new HashSet<>();
            censoRepository.findByUnidade_UuidAndDataBetweenOrderByDataDescVersaoDesc(u.getUuid(), ontem.minusDays(DIAS_DE_PENDENCIA - 1), ontem)
                    .stream().filter(c -> !c.isReaberto()).forEach(c -> fechados.add(c.getData()));
            for (int i = 0; i < DIAS_DE_PENDENCIA; i++) {
                LocalDate d = ontem.minusDays(i);
                if (!fechados.contains(d)) {
                    pendentes.add(new EletivaCensoDtos.Pendencia(u.getUuid(), u.getNome(), d));
                }
            }
        }
        return pendentes.stream().sorted(Comparator.comparing(EletivaCensoDtos.Pendencia::data).reversed()
                .thenComparing(EletivaCensoDtos.Pendencia::unidadeNome)).toList();
    }

    // ── Cálculo ────────────────────────────────────────────────────────────────────────────────────

    private record Calculo(CensoDiario censo, List<CensoDiarioPaciente> pacientes) {
    }

    /**
     * Do dia D (fuso do servidor): internados às 00h e às 24h, admissões, altas por tipo e trocas de leito no dia. Paciente-dia são
     * os internados às 24h mais os que entraram e saíram no mesmo dia. Taxa de ocupação: paciente-dia sobre leitos operacionais.
     * Média de permanência: dos que tiveram alta no dia.
     */
    private Calculo calcular(UnidadeDeSaude u, LocalDate data) {
        ZoneId zona = ZoneId.systemDefault();
        Instant inicio = data.atStartOfDay(zona).toInstant();
        Instant fim = data.plusDays(1).atStartOfDay(zona).toInstant();
        List<Internacao> internacoes = internacaoRepository.findDaUnidadeNoPeriodo(u.getUuid(), inicio, fim);
        int noInicio = 0;
        int admissoes = 0;
        int mesmoDia = 0;
        int[] altas = new int[TipoAlta.values().length];
        List<Internacao> noFim = new ArrayList<>();
        List<Long> permanencias = new ArrayList<>();
        for (Internacao i : internacoes) {
            boolean admitidaNoDia = !i.getAdmitidaEm().isBefore(inicio);
            boolean saiuNoDia = i.getAltaEm() != null && i.getAltaEm().isBefore(fim);
            if (!admitidaNoDia) {
                noInicio++;
            } else {
                admissoes++;
            }
            if (saiuNoDia) {
                altas[i.getTipoAlta().ordinal()]++;
                permanencias.add(Duration.between(i.getAdmitidaEm(), i.getAltaEm()).toHours());
                if (admitidaNoDia) {
                    mesmoDia++;
                }
            } else {
                noFim.add(i);
            }
        }
        List<EventoLeito> trocas = eventoLeitoRepository.findByLeito_Unidade_UuidAndTipoAndOcorridoEmBetween(u.getUuid(), TipoEventoLeito.TROCA_DE_LEITO,
                inicio, fim.minusNanos(1));
        // A troca grava um evento no leito de origem e outro no de destino.
        long trocasInternas = trocas.stream().map(e -> (e.getInternacao() != null ? e.getInternacao().getUuid() : null) + "@"
                + e.getOcorridoEm().getEpochSecond()).distinct().count();
        List<Leito> leitos = leitoRepository.findByUnidade_UuidOrderByIdentificacaoAsc(u.getUuid()).stream().filter(Leito::isAtivo).toList();
        int bloqueados = (int) leitos.stream().filter(l -> l.getSituacao() == SituacaoLeito.BLOQUEADO).count();
        int operacionais = leitos.size() - bloqueados;
        int pacienteDia = noFim.size() + mesmoDia;

        CensoDiario c = new CensoDiario();
        c.setUnidade(u);
        c.setData(data);
        c.setLeitosExistentes(leitos.size());
        c.setLeitosBloqueados(bloqueados);
        c.setLeitosOperacionais(operacionais);
        c.setInternadosInicio(noInicio);
        c.setAdmissoes(admissoes);
        c.setAltasMelhorado(altas[TipoAlta.MELHORADO.ordinal()]);
        c.setAltasAPedido(altas[TipoAlta.A_PEDIDO.ordinal()]);
        c.setAltasTransferencia(altas[TipoAlta.TRANSFERENCIA.ordinal()]);
        c.setEvasoes(altas[TipoAlta.EVASAO.ordinal()]);
        c.setObitos(altas[TipoAlta.OBITO.ordinal()]);
        c.setTransferenciasInternas((int) trocasInternas);
        c.setInternadosFim(noFim.size());
        c.setPacienteDia(pacienteDia);
        c.setTaxaOcupacao(operacionais == 0 ? null
                : BigDecimal.valueOf(pacienteDia * 100.0 / operacionais).setScale(1, RoundingMode.HALF_UP));
        c.setMediaPermanencia(permanencias.isEmpty() ? null
                : BigDecimal.valueOf(permanencias.stream().mapToLong(Long::longValue).average().orElse(0) / 24.0).setScale(1, RoundingMode.HALF_UP));
        List<CensoDiarioPaciente> nominal = noFim.stream()
                .sorted(Comparator.comparing((Internacao i) -> i.getLeito().getIdentificacao()))
                .map(i -> {
                    CensoDiarioPaciente p = new CensoDiarioPaciente();
                    p.setInternacao(i);
                    String nome = i.getPaciente().getNome();
                    p.setPacienteNome(nome.length() > 300 ? nome.substring(0, 300) : nome);
                    p.setLeitoIdentificacao(i.getLeito().getIdentificacao());
                    String setor = i.getLeito().getSetor().getNome();
                    p.setSetorNome(setor.length() > 200 ? setor.substring(0, 200) : setor);
                    p.setDiasInternado((int) Duration.between(i.getAdmitidaEm(), fim).toDays());
                    return p;
                }).toList();
        return new Calculo(c, new ArrayList<>(nominal));
    }

    private static EletivaCensoDtos.Censo previa(Calculo calc) {
        CensoDiario c = calc.censo;
        return new EletivaCensoDtos.Censo(null, c.getUnidade().getUuid(), c.getUnidade().getNome(), c.getData(), null, false, c.getLeitosExistentes(),
                c.getLeitosBloqueados(), c.getLeitosOperacionais(), c.getInternadosInicio(), c.getAdmissoes(), c.getAltasMelhorado(), c.getAltasAPedido(),
                c.getAltasTransferencia(), c.getEvasoes(), c.getObitos(), c.getTransferenciasInternas(), c.getInternadosFim(), c.getPacienteDia(),
                c.getTaxaOcupacao(), c.getMediaPermanencia(), null, false, null,
                calc.pacientes.stream().map(p -> new EletivaCensoDtos.PacienteDoCenso(p.getInternacao().getUuid(), p.getPacienteNome(),
                        p.getLeitoIdentificacao(), p.getSetorNome(), p.getDiasInternado())).toList());
    }

    private static void exigirDiaTerminado(LocalDate data) {
        if (data == null || !data.isBefore(LocalDate.now())) {
            throw new ResourceUnprocessableEntityException("O censo é de um dia já terminado: escolha até ontem.");
        }
    }

    private UnidadeDeSaude unidade(UUID id) {
        if (id == null) {
            throw new ResourceBadRequestException("Informe a unidade.");
        }
        return unidadeRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar uma unidade de saúde com o id " + id + " em nossos registros."));
    }
}
