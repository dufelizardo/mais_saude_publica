package com.edufelizardo.maissaudepublica.services.version1;

import com.edufelizardo.maissaudepublica.exceptions.ResourceBadRequestException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceForbiddenException;
import com.edufelizardo.maissaudepublica.models.CotaPpi;
import com.edufelizardo.maissaudepublica.models.EventoRegulacao;
import com.edufelizardo.maissaudepublica.models.OfertaAssistencial;
import com.edufelizardo.maissaudepublica.models.ReferenciaRede;
import com.edufelizardo.maissaudepublica.models.SolicitacaoRegulacao;
import com.edufelizardo.maissaudepublica.models.UnidadeDeSaude;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.CoberturaRedeDto;
import com.edufelizardo.maissaudepublica.models.enuns.RedeTematica;
import com.edufelizardo.maissaudepublica.models.enuns.StatusSolicitacaoRegulacao;
import com.edufelizardo.maissaudepublica.models.enuns.TipoEventoRegulacao;
import com.edufelizardo.maissaudepublica.models.enuns.TipoUnidadeDeSaude;
import com.edufelizardo.maissaudepublica.repositories.AdscricaoRepository;
import com.edufelizardo.maissaudepublica.repositories.CotaPpiRepository;
import com.edufelizardo.maissaudepublica.repositories.EventoRegulacaoRepository;
import com.edufelizardo.maissaudepublica.repositories.OfertaAssistencialRepository;
import com.edufelizardo.maissaudepublica.repositories.ReferenciaRedeRepository;
import com.edufelizardo.maissaudepublica.repositories.SolicitacaoRegulacaoRepository;
import com.edufelizardo.maissaudepublica.repositories.UnidadeDeSaudeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Gestão da Rede, fatia GR3 (ADR-0198): a cobertura da rede e os indicadores de pactuação. Não há cadastro novo: o serviço junta a oferta e
 * as cotas da PPI (GR1), as referências (GR2), a fila e as autorizações da Regulação e a população adscrita às equipes (Território), por
 * procedimento e por origem. A tela usa o acesso de rede; o relatório do #19, o escopo dos indicadores.
 */
@Service
public class CoberturaRedeService {

    private static final String CONSULTAR = RedeService.CONSULTAR;
    private static final String GERENCIAR = RedeService.GERENCIAR;
    private static final long MAXIMO_DIAS = 366;
    private static final Set<StatusSolicitacaoRegulacao> AGUARDANDO = EnumSet.of(StatusSolicitacaoRegulacao.SOLICITADA, StatusSolicitacaoRegulacao.DEVOLVIDA);
    private static final ZoneId ZONA = ZoneId.systemDefault();

    @Autowired private OfertaAssistencialRepository ofertaRepository;
    @Autowired private CotaPpiRepository cotaRepository;
    @Autowired private SolicitacaoRegulacaoRepository solicitacaoRepository;
    @Autowired private EventoRegulacaoRepository eventoRepository;
    @Autowired private AdscricaoRepository adscricaoRepository;
    @Autowired private UnidadeDeSaudeRepository unidadeRepository;
    @Autowired private ReferenciaRedeRepository referenciaRepository;
    @Autowired private RedeTematicaService redeTematicaService;
    @Autowired private EscopoDosIndicadores escopo;
    @Autowired private ControleDeAcesso controleDeAcesso;

    /** Na tela da rede: a competência (o mês atual sem ela), as unidades do acesso. */
    @Transactional(readOnly = true)
    public CoberturaRedeDto painel(YearMonth competencia, UUID procedimentoId) {
        controleDeAcesso.exigirAlguma(CONSULTAR, GERENCIAR);
        Optional<Set<UUID>> visiveis = controleDeAcesso.unidadesVisiveis(CONSULTAR, GERENCIAR);
        if (visiveis.isPresent() && visiveis.get().isEmpty()) {
            throw new ResourceForbiddenException("Seu acesso não inclui a gestão da rede em nenhuma unidade.");
        }
        YearMonth mes = competencia != null ? competencia : YearMonth.now();
        return calcular(mes.atDay(1), mes.atEndOfMonth(), procedimentoId, u -> visiveis.isEmpty() || visiveis.get().contains(u.getUuid()),
                visiveis.isEmpty() ? "Rede municipal" : "Unidades do seu acesso");
    }

    /** Para o relatório do #19: o período (até um ano) e o escopo dos indicadores. */
    @Transactional(readOnly = true)
    public CoberturaRedeDto paraRelatorio(LocalDate de, LocalDate ate, UUID unidadeId) {
        LocalDate fim = ate == null ? LocalDate.now() : ate;
        LocalDate inicio = de == null ? fim.withDayOfMonth(1) : de;
        if (inicio.isAfter(fim)) {
            throw new ResourceBadRequestException("O início do período não pode ser depois do fim.");
        }
        if (ChronoUnit.DAYS.between(inicio, fim) + 1 > MAXIMO_DIAS) {
            throw new ResourceBadRequestException("O período vai até um ano.");
        }
        EscopoDosIndicadores.Escopo e = escopo.de(unidadeId);
        return calcular(inicio, fim, null, u -> e.unidades() == null || e.contem(u.getUuid()), e.abrangencia());
    }

    // ── Cálculo ──────────────────────────────────────────────────────────────────────────────────

    private CoberturaRedeDto calcular(LocalDate de, LocalDate ate, UUID procedimentoId, Predicate<UnidadeDeSaude> visivel, String abrangencia) {
        LocalDate hoje = LocalDate.now();
        boolean fechada = ate.isBefore(hoje.withDayOfMonth(1));
        LocalDateTime inicio = de.atStartOfDay();
        LocalDateTime fim = ate.plusDays(1).atStartOfDay();

        // As ofertas das competências do período, visíveis pela executante ou por alguma beneficiária.
        List<OfertaAssistencial> todas = new ArrayList<>();
        for (YearMonth m = YearMonth.from(de); !m.isAfter(YearMonth.from(ate)); m = m.plusMonths(1)) {
            todas.addAll(ofertaRepository.findDaCompetencia(m.atDay(1)));
        }
        todas.removeIf(o -> procedimentoId != null && !o.getProcedimento().getUuid().equals(procedimentoId));
        Map<UUID, List<CotaPpi>> cotasDaOferta = todas.isEmpty() ? Map.of() : cotaRepository.findDasOfertas(todas.stream().map(OfertaAssistencial::getUuid).toList())
                .stream().collect(Collectors.groupingBy(c -> c.getOferta().getUuid()));
        List<OfertaAssistencial> ofertas = todas.stream().filter(o -> visivel.test(o.getExecutante())
                || cotasDaOferta.getOrDefault(o.getUuid(), List.of()).stream().anyMatch(c -> visivel.test(c.getBeneficiaria()))).toList();
        Map<UUID, List<OfertaAssistencial>> porProcedimento = ofertas.stream()
                .sorted(Comparator.comparing(o -> o.getProcedimento().getNome(), String.CASE_INSENSITIVE_ORDER))
                .collect(Collectors.groupingBy(o -> o.getProcedimento().getUuid(), LinkedHashMap::new, Collectors.toList()));

        List<CoberturaRedeDto.Lacuna> lacunas = new ArrayList<>();
        for (RedeTematica r : RedeTematica.values()) {
            redeTematicaService.montar(r, u -> u.isAtivo() && visivel.test(u), abrangencia).lacunas()
                    .forEach(l -> lacunas.add(new CoberturaRedeDto.Lacuna(r.rotulo(), l)));
        }
        if (porProcedimento.isEmpty()) {
            return new CoberturaRedeDto(de, ate, fechada, abrangencia, new CoberturaRedeDto.Resumo(0, 0, 0, 0, null, null, null, null, 0, lacunas.size()),
                    List.of(), List.of(), lacunas);
        }

        List<SolicitacaoRegulacao> solicitacoes = solicitacaoRepository.findDosProcedimentos(porProcedimento.keySet());
        Map<UUID, List<EventoRegulacao>> autorizacoes = eventoRepository.findDoTipoNosProcedimentos(TipoEventoRegulacao.AUTORIZACAO, porProcedimento.keySet(),
                inicio.atZone(ZONA).toInstant(), fim.atZone(ZONA).toInstant()).stream()
                .collect(Collectors.groupingBy(e -> e.getSolicitacao().getProcedimento().getUuid()));

        // A população adscrita: a da unidade e, na esfera, a das unidades abaixo dela.
        Map<UUID, Long> adscritos = new HashMap<>();
        for (Object[] l : adscricaoRepository.contarVigentesPorUnidade()) {
            adscritos.put((UUID) l[0], ((Number) l[1]).longValue());
        }
        List<UnidadeDeSaude> unidades = unidadeRepository.findAll();
        Map<UUID, UnidadeDeSaude> porId = new HashMap<>();
        Map<UUID, List<UUID>> acimaDe = new HashMap<>();
        Map<UUID, Long> populacao = new HashMap<>();
        for (UnidadeDeSaude u : unidades) {
            porId.put(u.getUuid(), u);
            List<UUID> acima = RedeService.acima(u);
            acimaDe.put(u.getUuid(), acima);
            long n = adscritos.getOrDefault(u.getUuid(), 0L);
            if (n > 0) {
                acima.forEach(a -> populacao.merge(a, n, Long::sum));
            }
        }

        List<CoberturaRedeDto.Procedimento> procedimentos = new ArrayList<>();
        List<String> alertas = new ArrayList<>();
        int ofertadoTotal = 0, pactuadoTotal = 0, utilizadoTotal = 0, extrasTotal = 0, avaliadasTotal = 0, foraTotal = 0, esgotadas = 0;
        for (Map.Entry<UUID, List<OfertaAssistencial>> e : porProcedimento.entrySet()) {
            UUID p = e.getKey();
            String nome = e.getValue().get(0).getProcedimento().getNome();
            Set<String> ofertadas = e.getValue().stream().map(o -> chave(o.getExecutante().getUuid(), YearMonth.from(o.getCompetencia()))).collect(Collectors.toSet());
            List<CotaPpi> cotas = e.getValue().stream().flatMap(o -> cotasDaOferta.getOrDefault(o.getUuid(), List.of()).stream()).toList();
            int ofertado = e.getValue().stream().mapToInt(OfertaAssistencial::getQuantidade).sum();
            int pactuado = cotas.stream().mapToInt(CotaPpi::getQuantidade).sum();
            List<SolicitacaoRegulacao> doProcedimento = solicitacoes.stream().filter(s -> s.getProcedimento().getUuid().equals(p)).toList();
            List<SolicitacaoRegulacao> autorizadasNoPeriodo = doProcedimento.stream()
                    .filter(s -> RedeService.CONSOMEM.contains(s.getStatus()) && s.getDataHoraPrevista() != null
                            && !s.getDataHoraPrevista().isBefore(inicio) && s.getDataHoraPrevista().isBefore(fim)).toList();
            // O que consumiu a oferta: na cota ou extracota, na executante e no mês ofertados (a regra da GR1).
            List<SolicitacaoRegulacao> usados = autorizadasNoPeriodo.stream().filter(s -> (s.getCota() != null || s.isExtraCota()) && s.getUnidadeExecutante() != null
                    && ofertadas.contains(chave(s.getUnidadeExecutante().getUuid(), YearMonth.from(s.getDataHoraPrevista())))).toList();
            int utilizado = usados.size();
            int extras = (int) usados.stream().filter(SolicitacaoRegulacao::isExtraCota).count();
            List<SolicitacaoRegulacao> avaliadas = autorizadasNoPeriodo.stream().filter(s -> s.getForaDaReferencia() != null).toList();
            int fora = (int) avaliadas.stream().filter(s -> Boolean.TRUE.equals(s.getForaDaReferencia())).count();
            List<SolicitacaoRegulacao> fila = doProcedimento.stream().filter(s -> AGUARDANDO.contains(s.getStatus())).toList();
            Instant agora = Instant.now();
            BigDecimal esperaFila = mediana(fila.stream().map(s -> dias(s.getSolicitadoEm(), agora)).toList());
            BigDecimal esperaAutorizacao = mediana(autorizacoes.getOrDefault(p, List.of()).stream()
                    .map(ev -> dias(ev.getSolicitacao().getSolicitadoEm(), ev.getOcorridoEm())).toList());

            // As origens: a beneficiária mais específica que cobre a solicitante ou, sem cota, a própria solicitante.
            Map<UUID, List<CotaPpi>> cotasDaBeneficiaria = cotas.stream().collect(Collectors.groupingBy(c -> c.getBeneficiaria().getUuid()));
            Map<UUID, int[]> numeros = new LinkedHashMap<>();
            cotasDaBeneficiaria.keySet().forEach(b -> numeros.put(b, new int[4]));
            for (SolicitacaoRegulacao s : doProcedimento) {
                boolean solicitada = !s.getSolicitadoEm().isBefore(inicio.atZone(ZONA).toInstant()) && s.getSolicitadoEm().isBefore(fim.atZone(ZONA).toInstant());
                boolean autorizada = autorizadasNoPeriodo.contains(s);
                boolean aguardando = AGUARDANDO.contains(s.getStatus());
                if (!solicitada && !autorizada && !aguardando) {
                    continue;
                }
                UUID solicitante = s.getUnidadeSolicitante().getUuid();
                UUID origem = acimaDe.getOrDefault(solicitante, List.of(solicitante)).stream().filter(cotasDaBeneficiaria::containsKey).findFirst().orElse(solicitante);
                int[] n = numeros.computeIfAbsent(origem, k -> new int[4]);
                n[0] += solicitada ? 1 : 0;
                n[1] += autorizada ? 1 : 0;
                n[2] += autorizada && s.isExtraCota() ? 1 : 0;
                n[3] += aguardando ? 1 : 0;
            }
            boolean temReferencia = referenciaRepository.findDoProcedimento(p).stream().anyMatch(r -> r.vigenteEm(hoje));
            List<CoberturaRedeDto.Origem> origens = new ArrayList<>();
            for (Map.Entry<UUID, int[]> o : numeros.entrySet()) {
                UnidadeDeSaude u = porId.get(o.getKey());
                if (u == null || !visivel.test(u)) {
                    continue;
                }
                int[] n = o.getValue();
                List<CotaPpi> daOrigem = cotasDaBeneficiaria.get(u.getUuid());
                Integer cota = daOrigem == null ? null : daOrigem.stream().mapToInt(CotaPpi::getQuantidade).sum();
                int usadosDaCota = (int) usados.stream().filter(s -> s.getCota() != null && s.getCota().getBeneficiaria().getUuid().equals(u.getUuid())).count();
                Integer saldo = cota == null ? null : Math.max(0, cota - usadosDaCota);
                long pop = populacao.getOrDefault(u.getUuid(), 0L);
                BigDecimal porMil = cota == null || pop == 0 ? null
                        : BigDecimal.valueOf(cota * 1000L).divide(BigDecimal.valueOf(pop), 1, RoundingMode.HALF_UP);
                ReferenciaRede referencia = redeTematicaService.vigentePara(u, p, hoje);
                List<String> avisos = new ArrayList<>();
                if (cota != null && saldo == 0 && n[3] > 0) {
                    avisos.add("Cota esgotada com fila");
                    esgotadas++;
                }
                if (fechada && cota != null && usadosDaCota * 2 < cota) {
                    avisos.add("Cota ociosa");
                }
                if (cota == null && (n[0] > 0 || n[3] > 0)) {
                    avisos.add("Demanda sem cota");
                }
                if (temReferencia && referencia == null) {
                    avisos.add("Sem referência");
                }
                avisos.forEach(a -> alertas.add(nome + ": " + u.getNome() + " · " + a.toLowerCase(Locale.ROOT) + "."));
                origens.add(new CoberturaRedeDto.Origem(u.getUuid(), u.getNome(), u.getTipo() != null ? u.getTipo().name() : null, pop, cota, porMil,
                        n[0], n[1], n[2], n[3], saldo, referencia != null, avisos));
            }
            // A unidade de atenção básica sem referência num procedimento que tem referência na rede.
            if (temReferencia) {
                Set<UUID> listadas = origens.stream().map(CoberturaRedeDto.Origem::unidadeId).collect(Collectors.toSet());
                unidades.stream().filter(u -> u.isAtivo() && u.getTipo() == TipoUnidadeDeSaude.UBS && visivel.test(u) && !listadas.contains(u.getUuid()))
                        .filter(u -> redeTematicaService.vigentePara(u, p, hoje) == null)
                        .sorted(Comparator.comparing(UnidadeDeSaude::getNome, String.CASE_INSENSITIVE_ORDER))
                        .forEach(u -> alertas.add(nome + ": " + u.getNome() + " · sem referência."));
            }
            origens.sort(Comparator.comparing((CoberturaRedeDto.Origem o) -> o.alertas().isEmpty()).thenComparing(CoberturaRedeDto.Origem::unidadeNome, String.CASE_INSENSITIVE_ORDER));
            procedimentos.add(new CoberturaRedeDto.Procedimento(p, nome, ofertado, pactuado, utilizado, extras, percentual(pactuado, ofertado),
                    percentual(utilizado - extras, pactuado), percentual(extras, utilizado), percentual(fora, avaliadas.size()), fila.size(), esperaFila,
                    esperaAutorizacao, origens));
            ofertadoTotal += ofertado;
            pactuadoTotal += pactuado;
            utilizadoTotal += utilizado;
            extrasTotal += extras;
            avaliadasTotal += avaliadas.size();
            foraTotal += fora;
        }
        return new CoberturaRedeDto(de, ate, fechada, abrangencia, new CoberturaRedeDto.Resumo(ofertadoTotal, pactuadoTotal, utilizadoTotal, extrasTotal,
                percentual(pactuadoTotal, ofertadoTotal), percentual(utilizadoTotal - extrasTotal, pactuadoTotal), percentual(extrasTotal, utilizadoTotal),
                percentual(foraTotal, avaliadasTotal), esgotadas, lacunas.size()), procedimentos, alertas, lacunas);
    }

    // ── Apoio ────────────────────────────────────────────────────────────────────────────────────

    private static String chave(UUID executante, YearMonth mes) {
        return executante + "|" + mes;
    }

    private static double dias(Instant de, Instant ate) {
        return Duration.between(de, ate).toMinutes() / 1440.0;
    }

    static BigDecimal mediana(List<Double> valores) {
        if (valores.isEmpty()) {
            return null;
        }
        List<Double> v = new ArrayList<>(valores);
        Collections.sort(v);
        int n = v.size();
        double m = n % 2 == 1 ? v.get(n / 2) : (v.get(n / 2 - 1) + v.get(n / 2)) / 2;
        return BigDecimal.valueOf(m).setScale(1, RoundingMode.HALF_UP);
    }

    private static BigDecimal percentual(long parte, long todo) {
        if (todo <= 0) {
            return null;
        }
        return BigDecimal.valueOf(parte * 100L).divide(BigDecimal.valueOf(todo), 1, RoundingMode.HALF_UP);
    }
}
