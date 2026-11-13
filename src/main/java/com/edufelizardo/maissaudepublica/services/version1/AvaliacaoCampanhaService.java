package com.edufelizardo.maissaudepublica.services.version1;

import com.edufelizardo.maissaudepublica.config.ContextoAuditoria;
import com.edufelizardo.maissaudepublica.config.UsuarioAutenticado;
import com.edufelizardo.maissaudepublica.exceptions.ResourceBadRequestException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceNotFoundException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceUnprocessableEntityException;
import com.edufelizardo.maissaudepublica.models.AcaoEducativa;
import com.edufelizardo.maissaudepublica.models.AcaoEducativaMaterial;
import com.edufelizardo.maissaudepublica.models.AcaoProgramatica;
import com.edufelizardo.maissaudepublica.models.AcompanhamentoProgramatico;
import com.edufelizardo.maissaudepublica.models.AvaliacaoCampanha;
import com.edufelizardo.maissaudepublica.models.Campanha;
import com.edufelizardo.maissaudepublica.models.CampanhaComunicacao;
import com.edufelizardo.maissaudepublica.models.CanalComunicacao;
import com.edufelizardo.maissaudepublica.models.Comunicado;
import com.edufelizardo.maissaudepublica.models.Microarea;
import com.edufelizardo.maissaudepublica.models.Publicacao;
import com.edufelizardo.maissaudepublica.models.Territorio;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.AvaliacaoCampanhaRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.AvaliacaoCampanhaDto;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoAcaoProgramatica;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoCampanhaComunicacao;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoMetaComunicacao;
import com.edufelizardo.maissaudepublica.models.enuns.TipoCanalComunicacao;
import com.edufelizardo.maissaudepublica.repositories.AcaoEducativaRepository;
import com.edufelizardo.maissaudepublica.repositories.AcompanhamentoProgramaticoRepository;
import com.edufelizardo.maissaudepublica.repositories.AvaliacaoCampanhaRepository;
import com.edufelizardo.maissaudepublica.repositories.CampanhaComunicacaoRepository;
import com.edufelizardo.maissaudepublica.repositories.ComunicadoRepository;
import com.edufelizardo.maissaudepublica.repositories.MembroNucleoRepository;
import com.edufelizardo.maissaudepublica.repositories.MicroareaRepository;
import com.edufelizardo.maissaudepublica.repositories.PublicacaoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * Avaliação da comunicação e da educação, fatia C6 (ADR-0196): reúne por campanha o alcance dos canais, as ações educativas da C3, a
 * cobertura das microáreas dos territórios, as metas e os indicadores de resultado associados; registra o julgamento da equipe na campanha
 * encerrada, com os números congelados; e entrega ao #19 os números do período para os relatórios.
 */
@Service
public class AvaliacaoCampanhaService {

    private static final String PUBLICAR = CampanhaComunicacaoService.PUBLICAR;
    private static final long MAXIMO_DIAS = 366;

    @Autowired private CampanhaComunicacaoRepository campanhaRepository;
    @Autowired private AvaliacaoCampanhaRepository avaliacaoRepository;
    @Autowired private ComunicadoRepository comunicadoRepository;
    @Autowired private PublicacaoRepository publicacaoRepository;
    @Autowired private AcaoEducativaRepository educativaRepository;
    @Autowired private MicroareaRepository microareaRepository;
    @Autowired private MembroNucleoRepository membroRepository;
    @Autowired private AcompanhamentoProgramaticoRepository acompanhamentoRepository;
    @Autowired private CampanhaService campanhaOperacionalService;
    @Autowired private EscopoDosIndicadores escopo;
    @Autowired private ControleDeAcesso controleDeAcesso;

    @Transactional(readOnly = true)
    public AvaliacaoCampanhaDto apuracao(UUID campanhaId) {
        CampanhaComunicacao c = campanha(campanhaId);
        if (c.getUnidade() != null) {
            controleDeAcesso.exigirVisivel(c.getUnidade(), PUBLICAR);
        } else {
            controleDeAcesso.exigirAlguma(PUBLICAR);
        }
        return apurar(c);
    }

    /** Só na campanha encerrada (422). Registrar de novo substitui o julgamento e congela os números outra vez. */
    @Transactional
    public AvaliacaoCampanhaDto registrar(UUID campanhaId, AvaliacaoCampanhaRequestDto dto) {
        CampanhaComunicacao c = campanha(campanhaId);
        if (c.getUnidade() != null) {
            controleDeAcesso.exigir(PUBLICAR, c.getUnidade());
        } else {
            controleDeAcesso.exigirAlguma(PUBLICAR);
        }
        if (c.getSituacao() != SituacaoCampanhaComunicacao.ENCERRADA) {
            throw new ResourceUnprocessableEntityException("A avaliação é da campanha encerrada.");
        }
        AvaliacaoCampanhaDto atual = apurar(c);
        Instant agora = Instant.now();
        AvaliacaoCampanha a = avaliacaoRepository.findByCampanha_Uuid(campanhaId).orElseGet(() -> {
            AvaliacaoCampanha n = new AvaliacaoCampanha();
            n.setCampanha(c);
            n.setRegistradaEm(agora);
            n.setRegistradaPorCpf(UsuarioAutenticado.cpf());
            return n;
        });
        a.setPublicoAtingido(dto.getPublicoAtingido());
        a.setResultados(texto(dto.getResultados()));
        a.setConclusoes(dto.getConclusoes().trim());
        a.setRecomendacoes(texto(dto.getRecomendacoes()));
        a.setPessoasAlcancadas(atual.acoes().pessoas());
        a.setAcoesRealizadas(atual.acoes().realizadas());
        a.setEnviados(atual.enviados());
        a.setCoberturaPercentual(atual.cobertura());
        a.setSituacaoMetaPessoas(atual.metaPessoas().situacao());
        a.setSituacaoMetaAcoes(atual.metaAcoes().situacao());
        a.setAtualizadaEm(agora);
        a.setAtualizadaPorCpf(UsuarioAutenticado.cpf());
        avaliacaoRepository.save(a);
        ContextoAuditoria.unidade(c.getUnidade());
        ContextoAuditoria.registro(c.getUuid());
        ContextoAuditoria.detalhe("Avaliação da campanha " + c.getNome() + ": público atingido " + dto.getPublicoAtingido() + ".");
        return apurar(c);
    }

    // ── Para os relatórios (#19) ─────────────────────────────────────────────────────────────────────

    /**
     * As campanhas cujo período cruza o filtro. Com unidade no filtro, as da unidade (ou abaixo dela); as da rede, sem unidade, só entram
     * sem filtro. Os números são os da campanha inteira.
     */
    @Transactional(readOnly = true)
    public AvaliacaoCampanhaDto.NoPeriodo campanhasNoPeriodo(LocalDate de, LocalDate ate, UUID unidadeId) {
        LocalDate[] p = periodo(de, ate);
        EscopoDosIndicadores.Escopo e = escopo.de(unidadeId);
        List<AvaliacaoCampanhaDto> lista = campanhaRepository.findAllByOrderByInicioDesc().stream()
                .filter(c -> !c.getInicio().isAfter(p[1]) && !c.getFim().isBefore(p[0]))
                .filter(c -> c.getUnidade() == null ? e.unidade() == null : e.contem(c.getUnidade().getUuid()))
                .map(this::apurar).toList();
        return new AvaliacaoCampanhaDto.NoPeriodo(p[0], p[1], e.abrangencia(), lista);
    }

    /** As ações educativas realizadas no período, nas unidades do escopo, por tema, por unidade e os materiais. */
    @Transactional(readOnly = true)
    public AvaliacaoCampanhaDto.EducacaoNoPeriodo educacaoNoPeriodo(LocalDate de, LocalDate ate, UUID unidadeId) {
        LocalDate[] p = periodo(de, ate);
        EscopoDosIndicadores.Escopo e = escopo.de(unidadeId);
        Map<String, int[]> temas = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        Map<String, int[]> unidades = new TreeMap<>();
        Map<String, Integer> materiais = new TreeMap<>();
        int acoes = 0;
        int pessoas = 0;
        for (AcaoEducativa x : educativaRepository.findTodas()) {
            AcaoProgramatica a = x.getAcao();
            if (a.getSituacao() != SituacaoAcaoProgramatica.REALIZADA || a.getRealizadaEm() == null || a.getRealizadaEm().isBefore(p[0])
                    || a.getRealizadaEm().isAfter(p[1]) || a.getUnidade() == null || !e.contem(a.getUnidade().getUuid())) {
                continue;
            }
            int publico = Objects.requireNonNullElse(EducacaoEmSaudeService.publico(a), 0);
            acoes++;
            pessoas += publico;
            int[] t = temas.computeIfAbsent(x.getTema(), k -> new int[6]);
            t[0]++;
            t[1] += publico;
            t[x.getParticipacao() == null ? 5 : switch (x.getParticipacao()) {
                case ALTA -> 2;
                case MEDIA -> 3;
                case BAIXA -> 4;
            }]++;
            int[] u = unidades.computeIfAbsent(a.getUnidade().getNome(), k -> new int[2]);
            u[0]++;
            u[1] += publico;
            for (AcaoEducativaMaterial m : x.getMateriais()) {
                materiais.merge(m.getMaterial().getTitulo(), m.getQuantidade(), Integer::sum);
            }
        }
        return new AvaliacaoCampanhaDto.EducacaoNoPeriodo(p[0], p[1], e.abrangencia(), acoes, pessoas,
                temas.entrySet().stream().map(t -> new AvaliacaoCampanhaDto.Tema(t.getKey(), t.getValue()[0], t.getValue()[1], t.getValue()[2],
                        t.getValue()[3], t.getValue()[4], t.getValue()[5])).toList(),
                unidades.entrySet().stream().map(u -> new AvaliacaoCampanhaDto.Unidade(u.getKey(), u.getValue()[0], u.getValue()[1])).toList(),
                materiais.entrySet().stream().map(m -> new AvaliacaoCampanhaDto.Material(m.getKey(), m.getValue())).toList());
    }

    // ── Apuração ────────────────────────────────────────────────────────────────────────────────────

    private AvaliacaoCampanhaDto apurar(CampanhaComunicacao c) {
        // Canais: os planejados e os que tiveram publicação.
        Map<UUID, CanalComunicacao> canais = new LinkedHashMap<>();
        c.getCanais().stream().sorted(Comparator.comparing(CanalComunicacao::getNome)).forEach(k -> canais.put(k.getUuid(), k));
        Map<UUID, int[]> numeros = new HashMap<>();
        for (Comunicado k : comunicadoRepository.findByCampanha_UuidOrderByInicioAsc(c.getUuid())) {
            for (Publicacao p : publicacaoRepository.findDoComunicado(k.getUuid())) {
                canais.putIfAbsent(p.getCanal().getUuid(), p.getCanal());
                int[] n = numeros.computeIfAbsent(p.getCanal().getUuid(), x -> new int[3]);
                n[0]++;
                n[1] += p.getEnviados();
                n[2] += p.getFalhas();
            }
        }
        UUID melhor = numeros.entrySet().stream()
                .filter(x -> canais.get(x.getKey()).getTipo() != TipoCanalComunicacao.PORTAL && x.getValue()[1] > 0)
                .max(Comparator.comparingInt(x -> x.getValue()[1])).map(Map.Entry::getKey).orElse(null);
        int enviados = 0;
        List<AvaliacaoCampanhaDto.Canal> listaCanais = new ArrayList<>();
        for (CanalComunicacao k : canais.values()) {
            int[] n = numeros.getOrDefault(k.getUuid(), new int[3]);
            boolean portal = k.getTipo() == TipoCanalComunicacao.PORTAL;
            enviados += n[1];
            listaCanais.add(new AvaliacaoCampanhaDto.Canal(k.getNome(), k.getTipo(), n[0], n[1], n[2], portal ? null : percentual(n[1], n[1] + n[2]),
                    portal, k.getUuid().equals(melhor)));
        }

        // Ações educativas.
        List<AcaoEducativa> educativas = educativaRepository.findDaCampanha(c.getUuid());
        int previstas = 0, realizadas = 0, naoRealizadas = 0, canceladas = 0, pessoas = 0, alta = 0, media = 0, baixa = 0, sem = 0;
        Map<String, Integer> materiais = new TreeMap<>();
        List<AcaoEducativa> feitas = new ArrayList<>();
        for (AcaoEducativa e : educativas) {
            switch (e.getAcao().getSituacao()) {
                case PENDENTE -> previstas++;
                case NAO_REALIZADA -> naoRealizadas++;
                case CANCELADA -> canceladas++;
                case REALIZADA -> {
                    realizadas++;
                    feitas.add(e);
                    pessoas += Objects.requireNonNullElse(EducacaoEmSaudeService.publico(e.getAcao()), 0);
                    if (e.getParticipacao() == null) {
                        sem++;
                    } else {
                        switch (e.getParticipacao()) {
                            case ALTA -> alta++;
                            case MEDIA -> media++;
                            case BAIXA -> baixa++;
                        }
                    }
                    for (AcaoEducativaMaterial m : e.getMateriais()) {
                        materiais.merge(m.getMaterial().getTitulo(), m.getQuantidade(), Integer::sum);
                    }
                }
            }
        }
        AvaliacaoCampanhaDto.Acoes acoes = new AvaliacaoCampanhaDto.Acoes(previstas, realizadas, naoRealizadas, canceladas, pessoas, alta, media, baixa, sem,
                materiais.entrySet().stream().map(m -> new AvaliacaoCampanhaDto.Material(m.getKey(), m.getValue())).toList());

        // Cobertura por território: os alvo da campanha ou, sem alvo, os das microáreas das ações que não caíram.
        Map<UUID, Territorio> alvo = new LinkedHashMap<>();
        c.getTerritorios().forEach(t -> alvo.put(t.getUuid(), t));
        if (alvo.isEmpty()) {
            educativas.stream().filter(e -> e.getAcao().getSituacao() == SituacaoAcaoProgramatica.PENDENTE || e.getAcao().getSituacao() == SituacaoAcaoProgramatica.REALIZADA)
                    .flatMap(e -> e.getMicroareas().stream()).forEach(m -> alvo.putIfAbsent(m.getArea().getTerritorio().getUuid(), m.getArea().getTerritorio()));
        }
        List<AvaliacaoCampanhaDto.Territorio> territorios = new ArrayList<>();
        BigDecimal cobertura = null;
        if (!alvo.isEmpty()) {
            Map<UUID, Long> moradores = new HashMap<>();
            for (Object[] l : membroRepository.contarVigentesPorMicroarea()) {
                moradores.put((UUID) l[0], ((Number) l[1]).longValue());
            }
            Map<UUID, Set<UUID>> microareasDo = new HashMap<>();
            for (Microarea m : microareaRepository.findDosTerritorios(alvo.keySet())) {
                microareasDo.computeIfAbsent(m.getArea().getTerritorio().getUuid(), k -> new HashSet<>()).add(m.getUuid());
            }
            int total = 0, comAcaoTotal = 0;
            for (Territorio t : alvo.values()) {
                Set<UUID> doTerritorio = microareasDo.getOrDefault(t.getUuid(), Set.of());
                Set<UUID> comAcao = new HashSet<>();
                int alcancadas = 0;
                for (AcaoEducativa e : feitas) {
                    Set<UUID> daAcao = new HashSet<>();
                    e.getMicroareas().forEach(m -> daAcao.add(m.getUuid()));
                    daAcao.retainAll(doTerritorio);
                    if (!daAcao.isEmpty()) {
                        comAcao.addAll(daAcao);
                        alcancadas += Objects.requireNonNullElse(EducacaoEmSaudeService.publico(e.getAcao()), 0);
                    }
                }
                total += doTerritorio.size();
                comAcaoTotal += comAcao.size();
                territorios.add(new AvaliacaoCampanhaDto.Territorio(t.getUuid(), t.getNome(), doTerritorio.size(), comAcao.size(),
                        percentual(comAcao.size(), doTerritorio.size()), doTerritorio.stream().mapToLong(m -> moradores.getOrDefault(m, 0L)).sum(), alcancadas, false));
            }
            cobertura = percentual(comAcaoTotal, total);
            territorios.sort(Comparator.comparing(AvaliacaoCampanhaDto.Territorio::cobertura, Comparator.nullsLast(Comparator.naturalOrder()))
                    .thenComparing(AvaliacaoCampanhaDto.Territorio::nome));
            if (territorios.size() > 1 && territorios.get(0).cobertura() != null) {
                AvaliacaoCampanhaDto.Territorio t = territorios.get(0);
                territorios.set(0, new AvaliacaoCampanhaDto.Territorio(t.uuid(), t.nome(), t.microareas(), t.microareasComAcao(), t.cobertura(),
                        t.moradores(), t.pessoas(), true));
            }
        }
        int semMicroarea = 0, pessoasSemMicroarea = 0;
        for (AcaoEducativa e : feitas) {
            if (e.getMicroareas().isEmpty()) {
                semMicroarea++;
                pessoasSemMicroarea += Objects.requireNonNullElse(EducacaoEmSaudeService.publico(e.getAcao()), 0);
            }
        }

        return new AvaliacaoCampanhaDto(c.getUuid(), c.getNome(), c.getSituacao(), c.getInicio(), c.getFim(), listaCanais, enviados, acoes, territorios,
                new AvaliacaoCampanhaDto.SemTerritorio(semMicroarea, pessoasSemMicroarea), cobertura,
                meta(c.getMetaPessoas(), pessoas, c.getSituacao()), meta(c.getMetaAcoes(), realizadas, c.getSituacao()), efetividade(c),
                avaliacaoRepository.findByCampanha_Uuid(c.getUuid()).map(AvaliacaoCampanhaService::registrada).orElse(null));
    }

    /** Abaixo só depois de a campanha terminar; antes, em andamento. */
    private static AvaliacaoCampanhaDto.Meta meta(Integer meta, int valor, SituacaoCampanhaComunicacao situacao) {
        if (meta == null) {
            return new AvaliacaoCampanhaDto.Meta(null, valor, null, SituacaoMetaComunicacao.SEM_META);
        }
        SituacaoMetaComunicacao s;
        if (valor >= meta) {
            s = SituacaoMetaComunicacao.ATINGIDA;
        } else if (situacao == SituacaoCampanhaComunicacao.ENCERRADA || situacao == SituacaoCampanhaComunicacao.CANCELADA) {
            s = SituacaoMetaComunicacao.ABAIXO;
        } else {
            s = SituacaoMetaComunicacao.EM_ANDAMENTO;
        }
        return new AvaliacaoCampanhaDto.Meta(meta, valor, percentual(valor, meta), s);
    }

    /**
     * A campanha operacional ligada, com o andamento da meta dela; o programa ligado, com as inscrições novas no período da campanha (até
     * hoje) e no período anterior de mesma duração, na unidade da campanha ou na rede.
     */
    private AvaliacaoCampanhaDto.Efetividade efetividade(CampanhaComunicacao c) {
        Campanha op = c.getCampanhaOperacional();
        String opNome = null;
        Integer opMeta = null, opRealizado = null;
        BigDecimal opPercentual = null;
        if (op != null) {
            opNome = op.getNome();
            opMeta = op.getMetaQuantidade();
            opRealizado = campanhaOperacionalService.realizado(op);
            opPercentual = opMeta == null ? null : percentual(opRealizado, opMeta);
        }
        if (c.getPrograma() == null) {
            return op == null ? null : new AvaliacaoCampanhaDto.Efetividade(opNome, opMeta, opRealizado, opPercentual, null, null, null, null, null, null, null);
        }
        LocalDate de = c.getInicio();
        LocalDate hoje = LocalDate.now();
        LocalDate ate = c.getFim().isAfter(hoje) ? hoje : c.getFim();
        if (ate.isBefore(de)) {
            ate = de;
        }
        long dias = ChronoUnit.DAYS.between(de, ate) + 1;
        LocalDate antesAte = de.minusDays(1);
        LocalDate antesDe = de.minusDays(dias);
        UUID unidade = c.getUnidade() == null ? null : c.getUnidade().getUuid();
        long novos = 0, anteriores = 0;
        for (AcompanhamentoProgramatico a : acompanhamentoRepository.findTodosDoPrograma(c.getPrograma().getUuid())) {
            if (unidade != null && (a.getUnidade() == null || !unidade.equals(a.getUnidade().getUuid()))) {
                continue;
            }
            LocalDate i = a.getInicio();
            if (!i.isBefore(de) && !i.isAfter(ate)) {
                novos++;
            } else if (!i.isBefore(antesDe) && !i.isAfter(antesAte)) {
                anteriores++;
            }
        }
        return new AvaliacaoCampanhaDto.Efetividade(opNome, opMeta, opRealizado, opPercentual, c.getPrograma().getNome(), de, ate, novos,
                antesDe, antesAte, anteriores);
    }

    private static AvaliacaoCampanhaDto.Registrada registrada(AvaliacaoCampanha a) {
        return new AvaliacaoCampanhaDto.Registrada(a.getPublicoAtingido(), a.getResultados(), a.getConclusoes(), a.getRecomendacoes(),
                a.getPessoasAlcancadas(), a.getAcoesRealizadas(), a.getEnviados(), a.getCoberturaPercentual(), a.getSituacaoMetaPessoas(),
                a.getSituacaoMetaAcoes(), a.getRegistradaEm(), a.getAtualizadaEm());
    }

    // ── Apoio ───────────────────────────────────────────────────────────────────────────────────────

    private static BigDecimal percentual(long parte, long todo) {
        if (todo <= 0) {
            return null;
        }
        return BigDecimal.valueOf(parte * 100L).divide(BigDecimal.valueOf(todo), 1, RoundingMode.HALF_UP);
    }

    private static LocalDate[] periodo(LocalDate de, LocalDate ate) {
        LocalDate fim = ate == null ? LocalDate.now() : ate;
        LocalDate inicio = de == null ? fim.withDayOfMonth(1) : de;
        if (inicio.isAfter(fim)) {
            throw new ResourceBadRequestException("O início do período não pode ser depois do fim.");
        }
        if (ChronoUnit.DAYS.between(inicio, fim) + 1 > MAXIMO_DIAS) {
            throw new ResourceBadRequestException("O período vai até um ano.");
        }
        return new LocalDate[]{inicio, fim};
    }

    private CampanhaComunicacao campanha(UUID uuid) {
        return campanhaRepository.findById(uuid).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar a campanha " + uuid + " em nossos registros."));
    }

    private static String texto(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }
}
