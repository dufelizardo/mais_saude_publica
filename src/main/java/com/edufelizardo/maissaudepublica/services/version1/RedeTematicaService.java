package com.edufelizardo.maissaudepublica.services.version1;

import com.edufelizardo.maissaudepublica.config.ContextoAuditoria;
import com.edufelizardo.maissaudepublica.config.UsuarioAutenticado;
import com.edufelizardo.maissaudepublica.exceptions.ResourceBadRequestException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceConflictException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceForbiddenException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceNotFoundException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceUnprocessableEntityException;
import com.edufelizardo.maissaudepublica.models.CapacidadeAssistencial;
import com.edufelizardo.maissaudepublica.models.ComponenteRedeItem;
import com.edufelizardo.maissaudepublica.models.PerfilOperacional;
import com.edufelizardo.maissaudepublica.models.ProcedimentoRegulado;
import com.edufelizardo.maissaudepublica.models.ReferenciaRede;
import com.edufelizardo.maissaudepublica.models.SolicitacaoRegulacao;
import com.edufelizardo.maissaudepublica.models.UnidadeCapacidadeAssistencial;
import com.edufelizardo.maissaudepublica.models.UnidadeDeSaude;
import com.edufelizardo.maissaudepublica.models.UnidadePerfilOperacional;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.ItemComponenteRedeRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.ReferenciaRedeRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.RedeTematicaDtos;
import com.edufelizardo.maissaudepublica.models.enuns.ComponenteRedeTematica;
import com.edufelizardo.maissaudepublica.models.enuns.RedeTematica;
import com.edufelizardo.maissaudepublica.models.enuns.TipoUnidadeDeSaude;
import com.edufelizardo.maissaudepublica.repositories.CapacidadeAssistencialRepository;
import com.edufelizardo.maissaudepublica.repositories.ComponenteRedeItemRepository;
import com.edufelizardo.maissaudepublica.repositories.PerfilOperacionalRepository;
import com.edufelizardo.maissaudepublica.repositories.ReferenciaRedeRepository;
import com.edufelizardo.maissaudepublica.repositories.SolicitacaoRegulacaoRepository;
import com.edufelizardo.maissaudepublica.repositories.UnidadeCapacidadeAssistencialRepository;
import com.edufelizardo.maissaudepublica.repositories.UnidadeDeSaudeRepository;
import com.edufelizardo.maissaudepublica.repositories.UnidadePerfilOperacionalRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * Gestão da Rede, fatia GR2 (ADR-0197): as redes temáticas no padrão da RAPS (o componente é dado do modelo operacional, e o painel mostra os
 * pontos e as lacunas) e a referência estrutural, para onde cada origem encaminha cada procedimento regulado. A Regulação recebe a referência
 * como sugestão e marca a autorização que foi para outra executante; a decisão do caso continua com o regulador.
 */
@Service
public class RedeTematicaService {

    private static final String CONSULTAR = RedeService.CONSULTAR;
    private static final String GERENCIAR = RedeService.GERENCIAR;
    private static final Duration JANELA = Duration.ofDays(90);

    @Autowired private ComponenteRedeItemRepository itemRepository;
    @Autowired private ReferenciaRedeRepository referenciaRepository;
    @Autowired private PerfilOperacionalRepository perfilRepository;
    @Autowired private CapacidadeAssistencialRepository capacidadeRepository;
    @Autowired private UnidadePerfilOperacionalRepository unidadePerfilRepository;
    @Autowired private UnidadeCapacidadeAssistencialRepository unidadeCapacidadeRepository;
    @Autowired private UnidadeDeSaudeRepository unidadeRepository;
    @Autowired private SolicitacaoRegulacaoRepository solicitacaoRepository;
    @Autowired private ProcedimentoReguladoService procedimentoService;
    @Autowired private ControleDeAcesso controleDeAcesso;

    // ── Catálogo ─────────────────────────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<RedeTematicaDtos.RedeDoCatalogo> catalogo() {
        controleDeAcesso.exigirAlguma(CONSULTAR, GERENCIAR);
        Map<ComponenteRedeTematica, List<RedeTematicaDtos.Item>> itens = new EnumMap<>(ComponenteRedeTematica.class);
        for (ComponenteRedeItem i : itemRepository.findTodos()) {
            itens.computeIfAbsent(i.getComponente(), k -> new ArrayList<>()).add(item(i));
        }
        List<RedeTematicaDtos.RedeDoCatalogo> redes = new ArrayList<>();
        for (RedeTematica r : RedeTematica.values()) {
            redes.add(new RedeTematicaDtos.RedeDoCatalogo(r, r.rotulo(), Arrays.stream(ComponenteRedeTematica.values()).filter(c -> c.rede() == r)
                    .map(c -> new RedeTematicaDtos.ComponenteDoCatalogo(c, c.rotulo(), itens.getOrDefault(c, List.of()).stream()
                            .sorted(Comparator.comparing(RedeTematicaDtos.Item::nome, String.CASE_INSENSITIVE_ORDER)).toList())).toList()));
        }
        return redes;
    }

    /** Liga um perfil ou uma capacidade ao componente. O catálogo é da rede inteira: só quem gerencia a rede toda (403). */
    @Transactional
    public RedeTematicaDtos.Item ligar(ComponenteRedeTematica componente, ItemComponenteRedeRequestDto dto) {
        exigirRedeInteira();
        if ((dto.getPerfilId() == null) == (dto.getCapacidadeId() == null)) {
            throw new ResourceBadRequestException("Informe o perfil ou a capacidade, um dos dois.");
        }
        ComponenteRedeItem i = new ComponenteRedeItem();
        i.setComponente(componente);
        if (dto.getPerfilId() != null) {
            PerfilOperacional p = perfilRepository.findById(dto.getPerfilId()).orElseThrow(() -> naoEncontrado("o perfil", dto.getPerfilId()));
            if (itemRepository.existsByComponenteAndPerfil_Uuid(componente, p.getUuid())) {
                throw new ResourceConflictException("O perfil " + p.getNome() + " já está em " + componente.rotulo() + ".");
            }
            i.setPerfil(p);
        } else {
            CapacidadeAssistencial c = capacidadeRepository.findById(dto.getCapacidadeId()).orElseThrow(() -> naoEncontrado("a capacidade", dto.getCapacidadeId()));
            if (itemRepository.existsByComponenteAndCapacidade_Uuid(componente, c.getUuid())) {
                throw new ResourceConflictException("A capacidade " + c.getNome() + " já está em " + componente.rotulo() + ".");
            }
            i.setCapacidade(c);
        }
        itemRepository.save(i);
        ContextoAuditoria.registro(i.getUuid());
        ContextoAuditoria.detalhe(componente.rede().rotulo() + ", " + componente.rotulo() + ": " + item(i).nome() + ".");
        return item(i);
    }

    @Transactional
    public void desligar(UUID itemId) {
        exigirRedeInteira();
        ComponenteRedeItem i = itemRepository.findById(itemId).orElseThrow(() -> naoEncontrado("o item", itemId));
        ContextoAuditoria.registro(i.getUuid());
        ContextoAuditoria.detalhe("Saiu de " + i.getComponente().rotulo() + ": " + item(i).nome() + ".");
        itemRepository.delete(i);
    }

    // ── Painel ───────────────────────────────────────────────────────────────────────────────────

    /** Por componente, as unidades ativas do acesso com o perfil ou a capacidade ligada a ele; e as lacunas. */
    @Transactional(readOnly = true)
    public RedeTematicaDtos.Painel painel(RedeTematica rede) {
        controleDeAcesso.exigirAlguma(CONSULTAR, GERENCIAR);
        Optional<Set<UUID>> visiveis = controleDeAcesso.unidadesVisiveis(CONSULTAR, GERENCIAR);
        if (visiveis.isPresent() && visiveis.get().isEmpty()) {
            throw new ResourceForbiddenException("Seu acesso não inclui a gestão da rede em nenhuma unidade.");
        }
        Predicate<UnidadeDeSaude> naAbrangencia = u -> u.isAtivo() && (visiveis.isEmpty() || visiveis.get().contains(u.getUuid()));
        Map<UUID, Set<ComponenteRedeTematica>> porPerfil = new HashMap<>();
        Map<UUID, Set<ComponenteRedeTematica>> porCapacidade = new HashMap<>();
        for (ComponenteRedeItem i : itemRepository.findTodos()) {
            if (i.getComponente().rede() != rede) {
                continue;
            }
            if (i.getPerfil() != null) {
                porPerfil.computeIfAbsent(i.getPerfil().getUuid(), k -> EnumSet.noneOf(ComponenteRedeTematica.class)).add(i.getComponente());
            } else {
                porCapacidade.computeIfAbsent(i.getCapacidade().getUuid(), k -> EnumSet.noneOf(ComponenteRedeTematica.class)).add(i.getComponente());
            }
        }
        Map<ComponenteRedeTematica, Map<UUID, Set<String>>> pontos = new EnumMap<>(ComponenteRedeTematica.class);
        Map<UUID, UnidadeDeSaude> unidades = new HashMap<>();
        for (UnidadePerfilOperacional p : unidadePerfilRepository.findVigentesComPerfil()) {
            for (ComponenteRedeTematica c : porPerfil.getOrDefault(p.getPerfil().getUuid(), Set.of())) {
                if (naAbrangencia.test(p.getUnidade())) {
                    unidades.put(p.getUnidade().getUuid(), p.getUnidade());
                    pontos.computeIfAbsent(c, k -> new LinkedHashMap<>()).computeIfAbsent(p.getUnidade().getUuid(), k -> new TreeSet<>()).add(p.getPerfil().getNome());
                }
            }
        }
        for (UnidadeCapacidadeAssistencial x : unidadeCapacidadeRepository.findVigentesComCapacidade()) {
            for (ComponenteRedeTematica c : porCapacidade.getOrDefault(x.getCapacidade().getUuid(), Set.of())) {
                if (naAbrangencia.test(x.getUnidade())) {
                    unidades.put(x.getUnidade().getUuid(), x.getUnidade());
                    pontos.computeIfAbsent(c, k -> new LinkedHashMap<>()).computeIfAbsent(x.getUnidade().getUuid(), k -> new TreeSet<>()).add(x.getCapacidade().getNome());
                }
            }
        }
        List<ComponenteRedeTematica> daRede = Arrays.stream(ComponenteRedeTematica.values()).filter(c -> c.rede() == rede).toList();
        List<RedeTematicaDtos.Componente> componentes = new ArrayList<>();
        List<String> lacunas = new ArrayList<>();
        for (ComponenteRedeTematica c : daRede) {
            Map<UUID, Set<String>> doComponente = pontos.getOrDefault(c, Map.of());
            if (doComponente.isEmpty()) {
                lacunas.add("Nenhum ponto de " + c.rotulo().toLowerCase(Locale.ROOT) + ".");
            }
            componentes.add(new RedeTematicaDtos.Componente(c, c.rotulo(), doComponente.entrySet().stream().map(e -> {
                UnidadeDeSaude u = unidades.get(e.getKey());
                return new RedeTematicaDtos.Ponto(u.getUuid(), u.getNome(), u.getTipo() != null ? u.getTipo().name() : null, List.copyOf(e.getValue()));
            }).sorted(Comparator.comparing(RedeTematicaDtos.Ponto::unidadeNome, String.CASE_INSENSITIVE_ORDER)).toList()));
        }
        lacunas.addAll(lacunasPorMunicipio(daRede, pontos, unidades, naAbrangencia));
        return new RedeTematicaDtos.Painel(rede, rede.rotulo(), visiveis.isEmpty() ? "Rede municipal" : "Unidades do seu acesso", componentes, lacunas);
    }

    /**
     * Com mais de um município no acesso, o componente sem ponto em cada um deles (pela unidade superior e pela supervisão regional). O
     * componente sem ponto nenhum já está nas lacunas da rede.
     */
    private List<String> lacunasPorMunicipio(List<ComponenteRedeTematica> daRede, Map<ComponenteRedeTematica, Map<UUID, Set<String>>> pontos,
                                             Map<UUID, UnidadeDeSaude> unidades, Predicate<UnidadeDeSaude> naAbrangencia) {
        List<UnidadeDeSaude> municipios = unidadeRepository.findAll().stream()
                .filter(u -> u.getTipo() == TipoUnidadeDeSaude.MUNICIPAL && naAbrangencia.test(u))
                .sorted(Comparator.comparing(UnidadeDeSaude::getNome, String.CASE_INSENSITIVE_ORDER)).toList();
        if (municipios.size() < 2) {
            return List.of();
        }
        Map<UUID, List<UUID>> acimaDe = new HashMap<>();
        unidades.values().forEach(u -> acimaDe.put(u.getUuid(), RedeService.acima(u)));
        List<String> lacunas = new ArrayList<>();
        for (UnidadeDeSaude m : municipios) {
            for (ComponenteRedeTematica c : daRede) {
                Map<UUID, Set<String>> doComponente = pontos.getOrDefault(c, Map.of());
                if (!doComponente.isEmpty() && doComponente.keySet().stream().noneMatch(u -> acimaDe.get(u).contains(m.getUuid()))) {
                    lacunas.add(m.getNome() + ": sem ponto de " + c.rotulo().toLowerCase(Locale.ROOT) + ".");
                }
            }
        }
        return lacunas;
    }

    // ── Referência ───────────────────────────────────────────────────────────────────────────────

    /**
     * Registra a referência da origem para o procedimento. Com uma aberta, a nova precisa começar depois dela (422) e a encerra na véspera; a
     * mesma executante da aberta é conflito (409). A executante precisa da capacidade exigida (422), estar ativa (422) e não ser a origem (400).
     */
    @Transactional
    public RedeTematicaDtos.Referencia registrar(ReferenciaRedeRequestDto dto) {
        UnidadeDeSaude origem = unidade(dto.getOrigemId());
        controleDeAcesso.exigir(GERENCIAR, origem);
        UnidadeDeSaude executante = unidade(dto.getExecutanteId());
        if (origem.getUuid().equals(executante.getUuid())) {
            throw new ResourceBadRequestException("A origem e a executante são a mesma unidade.");
        }
        ProcedimentoRegulado procedimento = procedimentoService.buscar(dto.getProcedimentoId());
        if (!procedimento.isAtivo()) {
            throw new ResourceUnprocessableEntityException("O procedimento " + procedimento.getNome() + " está fora de uso no catálogo da regulação.");
        }
        if (!executante.isAtivo()) {
            throw new ResourceUnprocessableEntityException("A unidade " + executante.getNome() + " está inativa.");
        }
        var exigida = procedimento.getCapacidadeExigida();
        if (exigida != null && !unidadeCapacidadeRepository.existsByUnidade_UuidAndCapacidade_UuidAndFimIsNull(executante.getUuid(), exigida.getUuid())) {
            throw new ResourceUnprocessableEntityException(executante.getNome() + " não tem a capacidade " + exigida.getNome() + ", que "
                    + procedimento.getNome() + " exige (modelo operacional).");
        }
        LocalDate inicio = dto.getInicio() != null ? dto.getInicio() : LocalDate.now();
        Optional<ReferenciaRede> aberta = referenciaRepository.findAbertaParaAtualizar(origem.getUuid(), procedimento.getUuid());
        if (aberta.isPresent()) {
            ReferenciaRede a = aberta.get();
            if (a.getExecutante().getUuid().equals(executante.getUuid())) {
                throw new ResourceConflictException(executante.getNome() + " já é a referência de " + origem.getNome() + " para " + procedimento.getNome() + ".");
            }
            if (!inicio.isAfter(a.getInicio())) {
                throw new ResourceUnprocessableEntityException("A referência vigente começou em " + a.getInicio() + "; a nova precisa começar depois.");
            }
            a.setFim(inicio.minusDays(1));
            a.setEncerradaEm(Instant.now());
            a.setEncerradaPorCpf(UsuarioAutenticado.cpf());
            referenciaRepository.saveAndFlush(a);
        }
        ReferenciaRede r = referenciaRepository.save(new ReferenciaRede(null, origem, procedimento, executante, inicio, null, texto(dto.getObservacao()),
                Instant.now(), UsuarioAutenticado.cpf(), null, null));
        ContextoAuditoria.unidade(origem);
        ContextoAuditoria.registro(r.getUuid());
        ContextoAuditoria.detalhe("Referência de " + origem.getNome() + " para " + procedimento.getNome() + ": " + executante.getNome() + ".");
        return dto(r);
    }

    /** Encerra a aberta hoje, sem substituir; a que ainda não começou não se encerra (422). */
    @Transactional
    public RedeTematicaDtos.Referencia encerrar(UUID uuid) {
        ReferenciaRede r = referenciaRepository.findById(uuid).orElseThrow(() -> naoEncontrado("a referência", uuid));
        controleDeAcesso.exigir(GERENCIAR, r.getOrigem());
        LocalDate hoje = LocalDate.now();
        if (r.getFim() != null) {
            throw new ResourceUnprocessableEntityException("Esta referência já foi encerrada.");
        }
        if (r.getInicio().isAfter(hoje)) {
            throw new ResourceUnprocessableEntityException("A referência começa em " + r.getInicio() + "; registre outra no lugar dela.");
        }
        r.setFim(hoje);
        r.setEncerradaEm(Instant.now());
        r.setEncerradaPorCpf(UsuarioAutenticado.cpf());
        referenciaRepository.save(r);
        ContextoAuditoria.unidade(r.getOrigem());
        ContextoAuditoria.registro(r.getUuid());
        ContextoAuditoria.detalhe("Referência encerrada.");
        return dto(r);
    }

    /**
     * As referências cuja origem ou executante está no acesso, vigentes primeiro, e nos últimos 90 dias as autorizações que tinham
     * referência, por procedimento, com quantas foram para outra executante.
     */
    @Transactional(readOnly = true)
    public RedeTematicaDtos.PainelReferencias referencias(UUID procedimentoId) {
        controleDeAcesso.exigirAlguma(CONSULTAR, GERENCIAR);
        LocalDate hoje = LocalDate.now();
        List<RedeTematicaDtos.Referencia> lista = controleDeAcesso.filtrar(referenciaRepository.findTodas().stream()
                        .filter(r -> procedimentoId == null || r.getProcedimento().getUuid().equals(procedimentoId)),
                        r -> Stream.of(r.getOrigem(), r.getExecutante()), CONSULTAR, GERENCIAR)
                .map(this::dto)
                .sorted(Comparator.comparing(RedeTematicaDtos.Referencia::procedimentoNome, String.CASE_INSENSITIVE_ORDER)
                        .thenComparing(RedeTematicaDtos.Referencia::origemNome, String.CASE_INSENSITIVE_ORDER)
                        .thenComparing(RedeTematicaDtos.Referencia::vigente, Comparator.reverseOrder())
                        .thenComparing(RedeTematicaDtos.Referencia::inicio, Comparator.reverseOrder())).toList();
        Map<UUID, int[]> contagem = new LinkedHashMap<>();
        Map<UUID, String> nomes = new HashMap<>();
        List<SolicitacaoRegulacao> avaliadas = controleDeAcesso.filtrar(solicitacaoRepository.findAvaliadasPelaReferenciaDesde(Instant.now().minus(JANELA),
                                RedeService.CONSOMEM).stream().filter(s -> procedimentoId == null || s.getProcedimento().getUuid().equals(procedimentoId)),
                        s -> Stream.of(s.getUnidadeSolicitante(), s.getUnidadeExecutante()), CONSULTAR, GERENCIAR).toList();
        for (SolicitacaoRegulacao s : avaliadas) {
            nomes.put(s.getProcedimento().getUuid(), s.getProcedimento().getNome());
            int[] n = contagem.computeIfAbsent(s.getProcedimento().getUuid(), k -> new int[2]);
            n[0]++;
            if (Boolean.TRUE.equals(s.getForaDaReferencia())) {
                n[1]++;
            }
        }
        List<RedeTematicaDtos.Indicador> indicadores = contagem.entrySet().stream()
                .map(e -> new RedeTematicaDtos.Indicador(e.getKey(), nomes.get(e.getKey()), e.getValue()[0], e.getValue()[1],
                        BigDecimal.valueOf(e.getValue()[1] * 100L).divide(BigDecimal.valueOf(e.getValue()[0]), 1, RoundingMode.HALF_UP)))
                .sorted(Comparator.comparing(RedeTematicaDtos.Indicador::percentualFora).reversed()
                        .thenComparing(RedeTematicaDtos.Indicador::procedimentoNome, String.CASE_INSENSITIVE_ORDER)).toList();
        return new RedeTematicaDtos.PainelReferencias(lista, indicadores);
    }

    /** Para a gaveta da Regulação: a referência vigente hoje que cobre a solicitante. */
    @Transactional(readOnly = true)
    public RedeTematicaDtos.Sugestao sugestao(UUID procedimentoId, UUID solicitanteId) {
        controleDeAcesso.exigirAlguma(CONSULTAR, GERENCIAR, SolicitacaoRegulacaoService.REGULAR);
        ReferenciaRede r = vigentePara(unidade(solicitanteId), procedimentoId, LocalDate.now());
        return r == null ? new RedeTematicaDtos.Sugestao(null, null, null, null)
                : new RedeTematicaDtos.Sugestao(r.getUuid(), r.getExecutante().getUuid(), r.getExecutante().getNome(), r.getOrigem().getNome());
    }

    // ── Para a Regulação ─────────────────────────────────────────────────────────────────────────

    /** A referência mais específica vigente no dia: a da unidade e, sem ela, a da esfera mais próxima acima. */
    ReferenciaRede vigentePara(UnidadeDeSaude solicitante, UUID procedimentoId, LocalDate dia) {
        Map<UUID, ReferenciaRede> porOrigem = new HashMap<>();
        for (ReferenciaRede r : referenciaRepository.findDoProcedimento(procedimentoId)) {
            if (r.vigenteEm(dia)) {
                porOrigem.put(r.getOrigem().getUuid(), r);
            }
        }
        if (porOrigem.isEmpty()) {
            return null;
        }
        return RedeService.acima(solicitante).stream().map(porOrigem::get).filter(Objects::nonNull).findFirst().orElse(null);
    }

    // ── Apoio ────────────────────────────────────────────────────────────────────────────────────

    private void exigirRedeInteira() {
        controleDeAcesso.exigirAlguma(GERENCIAR);
        if (controleDeAcesso.unidadesVisiveis(GERENCIAR).isPresent()) {
            throw new ResourceForbiddenException("O catálogo das redes temáticas vale para a rede inteira; só quem gerencia a rede toda o altera.");
        }
    }

    private static RedeTematicaDtos.Item item(ComponenteRedeItem i) {
        if (i.getPerfil() != null) {
            return new RedeTematicaDtos.Item(i.getUuid(), "PERFIL", i.getPerfil().getUuid(), i.getPerfil().getCodigo(), i.getPerfil().getNome());
        }
        return new RedeTematicaDtos.Item(i.getUuid(), "CAPACIDADE", i.getCapacidade().getUuid(), i.getCapacidade().getCodigo(), i.getCapacidade().getNome());
    }

    private RedeTematicaDtos.Referencia dto(ReferenciaRede r) {
        return new RedeTematicaDtos.Referencia(r.getUuid(), r.getOrigem().getUuid(), r.getOrigem().getNome(),
                r.getOrigem().getTipo() != null ? r.getOrigem().getTipo().name() : null, r.getProcedimento().getUuid(), r.getProcedimento().getNome(),
                r.getExecutante().getUuid(), r.getExecutante().getNome(), r.getInicio(), r.getFim(), r.vigenteEm(LocalDate.now()), r.getObservacao());
    }

    private UnidadeDeSaude unidade(UUID id) {
        if (id == null) {
            throw new ResourceBadRequestException("Informe a unidade.");
        }
        return unidadeRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar uma unidade de saúde com o id " + id + " em nossos registros."));
    }

    private static ResourceNotFoundException naoEncontrado(String o, UUID id) {
        return new ResourceNotFoundException("Não foi possível encontrar " + o + " " + id + " em nossos registros.");
    }

    private static String texto(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }
}
