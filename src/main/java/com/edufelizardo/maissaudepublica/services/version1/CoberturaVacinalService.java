package com.edufelizardo.maissaudepublica.services.version1;

import com.edufelizardo.maissaudepublica.exceptions.ResourceBadRequestException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceForbiddenException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceNotFoundException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceUnprocessableEntityException;
import com.edufelizardo.maissaudepublica.models.Adscricao;
import com.edufelizardo.maissaudepublica.models.Campanha;
import com.edufelizardo.maissaudepublica.models.Equipe;
import com.edufelizardo.maissaudepublica.models.EsquemaVacinal;
import com.edufelizardo.maissaudepublica.models.Microarea;
import com.edufelizardo.maissaudepublica.models.Paciente;
import com.edufelizardo.maissaudepublica.models.RegistroVacinacao;
import com.edufelizardo.maissaudepublica.models.UnidadeDeSaude;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.CoberturaVacinalDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.ContagemProducaoDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.VacinacaoNaRedeDto;
import com.edufelizardo.maissaudepublica.models.enuns.ClasseAbandonoVacinal;
import com.edufelizardo.maissaudepublica.models.enuns.DoseVacinal;
import com.edufelizardo.maissaudepublica.models.enuns.EstrategiaVacinacao;
import com.edufelizardo.maissaudepublica.models.enuns.PublicoVacinal;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoCampanha;
import com.edufelizardo.maissaudepublica.models.enuns.TipoCampanha;
import com.edufelizardo.maissaudepublica.repositories.AdscricaoRepository;
import com.edufelizardo.maissaudepublica.repositories.CampanhaRepository;
import com.edufelizardo.maissaudepublica.repositories.EquipeRepository;
import com.edufelizardo.maissaudepublica.repositories.EsquemaVacinalRepository;
import com.edufelizardo.maissaudepublica.repositories.MicroareaRepository;
import com.edufelizardo.maissaudepublica.repositories.RegistroVacinacaoRepository;
import com.edufelizardo.maissaudepublica.repositories.UnidadeDeSaudeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Cobertura vacinal, fatia I3 da Imunização (ADR-0149). Sai do cadastro, não de estimativa (ADR-0128): para cada dose
 * do calendário que mede a cobertura, a coorte são as crianças adscritas de 1 a 12 meses depois da idade recomendada, e
 * vacinada é a que tem a dose vigente (aplicada ou transcrita) até a data. Com isso, a homogeneidade entre vacinas (as
 * vacinas na meta), entre territórios (os recortes na meta) e a taxa de abandono das vacinas de várias doses (na mesma
 * coorte, quem tomou a 1ª dose e não chegou à de referência). Só números.
 */
@Service
public class CoberturaVacinalService {

    /** A leitura da unidade: quem vacina, quem gerencia a imunização ou quem consulta os indicadores. */
    static final String[] LEITURA = {VacinacaoService.REGISTRAR, ImunizacaoCatalogoService.GERENCIAR, EscopoDosIndicadores.CONSULTAR};
    static final int JANELA_COORTE_MESES = 12;
    private static final long MAXIMO_DIAS = 366;
    private static final Map<EstrategiaVacinacao, String> ESTRATEGIAS = new EnumMap<>(Map.of(
            EstrategiaVacinacao.ROTINA, "Rotina", EstrategiaVacinacao.CAMPANHA, "Campanha", EstrategiaVacinacao.BLOQUEIO, "Bloqueio",
            EstrategiaVacinacao.INTENSIFICACAO, "Intensificação", EstrategiaVacinacao.ESPECIAL, "Especial (CRIE)"));

    @Autowired private AdscricaoRepository adscricaoRepository;
    @Autowired private RegistroVacinacaoRepository vacinacaoRepository;
    @Autowired private EsquemaVacinalRepository esquemaRepository;
    @Autowired private UnidadeDeSaudeRepository unidadeRepository;
    @Autowired private EquipeRepository equipeRepository;
    @Autowired private MicroareaRepository microareaRepository;
    @Autowired private CampanhaRepository campanhaRepository;
    @Autowired private ControleDeAcesso controleDeAcesso;
    @Autowired private EscopoDosIndicadores escopoDosIndicadores;

    /** Uma criança adscrita: onde está, o nascimento e as doses vigentes até a data (imunobiológico e dose). */
    record Crianca(UUID unidadeId, String unidadeNome, UUID equipeId, String equipeNome, UUID microareaId, String microareaNome,
                   LocalDate nascimento, Set<String> doses) {
    }

    /** A contagem de uma vacina numa população; sem 1ª dose quando a vacina não tem abandono. */
    record Contagem(int coorte, int vacinados, Integer comPrimeira, Integer completaram) {

        Double cobertura() {
            return coorte == 0 ? null : arredondar(100.0 * vacinados / coorte);
        }

        boolean atingida(EsquemaVacinal e) {
            return coorte > 0 && e.getMetaCobertura() != null && 100.0 * vacinados / coorte >= e.getMetaCobertura().doubleValue();
        }

        Double abandono() {
            return comPrimeira == null || comPrimeira == 0 ? null : arredondar(100.0 * (comPrimeira - completaram) / comPrimeira);
        }
    }

    /** As doses que medem a cobertura na data, e as vacinas com 1ª dose na criança (as que têm abandono). */
    record Calendario(List<EsquemaVacinal> medidores, Set<UUID> comPrimeiraDose) {

        boolean temAbandono(EsquemaVacinal e) {
            return e.getDose() != DoseVacinal.PRIMEIRA && e.getDose() != DoseVacinal.UNICA && comPrimeiraDose.contains(e.getImunobiologico().getUuid());
        }

        int idadeMaximaMeses() {
            return medidores.stream().mapToInt(e -> e.getIdadeRecomendadaMeses() + JANELA_COORTE_MESES).max().orElse(0);
        }
    }

    // ── Cobertura ──────────────────────────────────────────────────────────────────────────────

    /**
     * A cobertura da abrangência: a microárea (com a equipe), a equipe (por microárea), a unidade (por equipe) ou, sem
     * filtro, as unidades do acesso (por unidade). A data de referência é hoje por padrão; a adscrição é a vigente.
     */
    @Transactional(readOnly = true)
    public CoberturaVacinalDto cobertura(UUID unidadeId, UUID equipeId, UUID microareaId, LocalDate data) {
        LocalDate dia = data == null ? LocalDate.now() : data;
        if (dia.isAfter(LocalDate.now())) {
            throw new ResourceBadRequestException("A data de referência não pode ser no futuro.");
        }
        if (microareaId != null && equipeId == null) {
            throw new ResourceBadRequestException("Para a microárea, informe também a equipe.");
        }
        String nivel;
        String abrangencia;
        String recorteRotulo;
        Predicate<Adscricao> filtro;
        Function<Crianca, Recorte> recorte;
        UnidadeDeSaude unidade = null;
        if (equipeId != null) {
            Equipe equipe = equipeRepository.findById(equipeId).orElseThrow(() -> new ResourceNotFoundException(
                    "Não foi possível encontrar a equipe " + equipeId + " em nossos registros."));
            unidade = equipe.getUnidade();
            if (unidade == null) {
                throw new ResourceUnprocessableEntityException("A equipe " + equipe.getNome() + " não está numa unidade.");
            }
            if (unidadeId != null && !unidadeId.equals(unidade.getUuid())) {
                throw new ResourceBadRequestException("A equipe " + equipe.getNome() + " é de outra unidade.");
            }
            controleDeAcesso.exigirVisivel(unidade, LEITURA);
            if (microareaId != null) {
                Microarea m = microareaRepository.findById(microareaId).orElseThrow(() -> new ResourceNotFoundException(
                        "Não foi possível encontrar a microárea " + microareaId + " em nossos registros."));
                nivel = "MICROAREA";
                abrangencia = equipe.getNome() + " · " + nomeDaMicroarea(m);
                recorteRotulo = null;
                filtro = a -> a.getEquipe().getUuid().equals(equipeId) && a.getMicroarea() != null && a.getMicroarea().getUuid().equals(microareaId);
                recorte = null;
            } else {
                nivel = "EQUIPE";
                abrangencia = equipe.getNome();
                recorteRotulo = "Microárea";
                filtro = a -> a.getEquipe().getUuid().equals(equipeId);
                recorte = c -> new Recorte(c.microareaId(), c.microareaNome() != null ? c.microareaNome() : "Sem microárea");
            }
        } else if (unidadeId != null) {
            unidade = unidadeRepository.findById(unidadeId).orElseThrow(() -> new ResourceNotFoundException(
                    "Não foi possível encontrar a unidade " + unidadeId + " em nossos registros."));
            controleDeAcesso.exigirVisivel(unidade, LEITURA);
            nivel = "UNIDADE";
            abrangencia = unidade.getNome();
            recorteRotulo = "Equipe";
            filtro = a -> a.getEquipe().getUnidade().getUuid().equals(unidadeId);
            recorte = c -> new Recorte(c.equipeId(), c.equipeNome());
        } else {
            Optional<Set<UUID>> visiveis = controleDeAcesso.unidadesVisiveis(LEITURA);
            if (visiveis.isPresent() && visiveis.get().isEmpty()) {
                throw new ResourceForbiddenException("Seu acesso não inclui a cobertura vacinal em nenhuma unidade.");
            }
            nivel = "REDE";
            abrangencia = visiveis.isEmpty() ? "Rede municipal" : "Unidades do seu acesso";
            recorteRotulo = "Unidade";
            filtro = a -> visiveis.isEmpty() || visiveis.get().contains(a.getEquipe().getUnidade().getUuid());
            recorte = c -> new Recorte(c.unidadeId(), c.unidadeNome());
        }
        Calendario cal = calendario(dia);
        List<Crianca> criancas = criancas(filtro, cal, dia);
        Resultado r = calcular(cal, criancas, dia, recorte);
        return new CoberturaVacinalDto(nivel, abrangencia, unidade != null ? unidade.getUuid() : null, equipeId, microareaId, dia,
                r.homogeneidade(), r.naMeta(), r.avaliadas(), r.abandonoAlto(), r.vacinas(), recorteRotulo, r.recortes());
    }

    // ── Sala de Situação ───────────────────────────────────────────────────────────────────────

    /**
     * A vacinação na Sala de Situação (ADR-0149), com a abrangência dos indicadores (ADR-0142): as doses aplicadas no
     * período por estratégia e por vacina, a cobertura de hoje com a homogeneidade e o abandono, e as campanhas de
     * vacinação do período com o andamento da meta (da campanha inteira).
     */
    @Transactional(readOnly = true)
    public VacinacaoNaRedeDto naRede(LocalDate de, LocalDate ate, UUID unidadeId) {
        LocalDate fim = ate == null ? LocalDate.now() : ate;
        LocalDate inicio = de == null ? fim.withDayOfMonth(1) : de;
        if (inicio.isAfter(fim)) {
            throw new ResourceBadRequestException("O início do período não pode ser depois do fim.");
        }
        if (ChronoUnit.DAYS.between(inicio, fim) + 1 > MAXIMO_DIAS) {
            throw new ResourceBadRequestException("O período vai até um ano.");
        }
        EscopoDosIndicadores.Escopo escopo = escopoDosIndicadores.de(unidadeId);
        List<RegistroVacinacao> doses = vacinacaoRepository.findAplicadasEntre(inicio.atStartOfDay(), fim.plusDays(1).atStartOfDay()).stream()
                .filter(r -> escopo.contem(r.getUnidade().getUuid())).toList();
        Map<EstrategiaVacinacao, Long> porEstrategia = doses.stream()
                .collect(Collectors.groupingBy(RegistroVacinacao::getEstrategia, () -> new EnumMap<>(EstrategiaVacinacao.class), Collectors.counting()));
        Map<String, Long> porSigla = doses.stream().collect(Collectors.groupingBy(r -> r.getImunobiologico().getSigla(), Collectors.counting()));
        Map<String, String> nomes = new HashMap<>();
        doses.forEach(r -> nomes.put(r.getImunobiologico().getSigla(), r.getImunobiologico().getNome()));

        LocalDate hoje = LocalDate.now();
        Calendario cal = calendario(hoje);
        Resultado r = calcular(cal, criancas(a -> escopo.contem(a.getEquipe().getUnidade().getUuid()), cal, hoje), hoje, null);

        List<VacinacaoNaRedeDto.Campanha> campanhas = campanhaRepository.findTodas().stream()
                .filter(c -> c.getTipo() == TipoCampanha.VACINACAO && c.getCanceladaEm() == null
                        && !c.getInicio().isAfter(fim) && !c.getFim().isBefore(inicio))
                .sorted(Comparator.comparing(Campanha::getInicio))
                .map(c -> new VacinacaoNaRedeDto.Campanha(c.getUuid(), c.getNome(), c.getInicio(), c.getFim(), c.situacao(hoje),
                        c.getMetaQuantidade(), (int) vacinacaoRepository.countPessoasDaCampanha(c.getUuid())))
                .toList();
        return new VacinacaoNaRedeDto(escopo.abrangencia(), inicio, fim, doses.size(),
                porEstrategia.entrySet().stream().map(x -> new ContagemProducaoDto(x.getKey().name(), ESTRATEGIAS.get(x.getKey()), x.getValue()))
                        .sorted(Comparator.comparingLong(ContagemProducaoDto::getQuantidade).reversed()).toList(),
                porSigla.entrySet().stream().map(x -> new ContagemProducaoDto(x.getKey(), nomes.get(x.getKey()), x.getValue()))
                        .sorted(Comparator.comparingLong(ContagemProducaoDto::getQuantidade).reversed().thenComparing(ContagemProducaoDto::getChave))
                        .limit(10).toList(),
                r.homogeneidade(), r.naMeta(), r.avaliadas(), r.abandonoAlto(), r.vacinas(), campanhas);
    }

    // ── Cálculo ────────────────────────────────────────────────────────────────────────────────

    private record Recorte(UUID id, String nome) {
    }

    private record Resultado(List<CoberturaVacinalDto.Vacina> vacinas, Double homogeneidade, int naMeta, int avaliadas, int abandonoAlto,
                             List<CoberturaVacinalDto.Recorte> recortes) {
    }

    private Calendario calendario(LocalDate dia) {
        List<EsquemaVacinal> todos = esquemaRepository.findTodos();
        List<EsquemaVacinal> medidores = todos.stream()
                .filter(e -> e.isMedeCobertura() && e.vigenteEm(dia) && e.getIdadeRecomendadaMeses() != null)
                .sorted(Comparator.comparingInt(EsquemaVacinal::getOrdem)).toList();
        Set<UUID> comPrimeira = todos.stream()
                .filter(e -> e.getDose() == DoseVacinal.PRIMEIRA && e.getPublico() == PublicoVacinal.CRIANCA && e.vigenteEm(dia))
                .map(e -> e.getImunobiologico().getUuid()).collect(Collectors.toSet());
        return new Calendario(medidores, comPrimeira);
    }

    /** As crianças adscritas da abrangência na idade de alguma coorte, com as doses vigentes até a data. */
    private List<Crianca> criancas(Predicate<Adscricao> filtro, Calendario cal, LocalDate dia) {
        int idadeMaxima = cal.idadeMaximaMeses();
        Map<UUID, Adscricao> porPaciente = new LinkedHashMap<>();
        for (Adscricao a : adscricaoRepository.findVigentesCompletas()) {
            Paciente p = a.getPaciente();
            if (p.isAtivo() && p.getDataNascimento() != null && !p.getDataNascimento().isAfter(dia)
                    && CalendarioVacinal.idadeMeses(p.getDataNascimento(), dia) <= idadeMaxima && filtro.test(a)) {
                porPaciente.putIfAbsent(p.getUuid(), a);
            }
        }
        if (porPaciente.isEmpty()) {
            return List.of();
        }
        Map<UUID, Set<String>> doses = new HashMap<>();
        for (RegistroVacinacao r : vacinacaoRepository.findVigentesDosPacientes(porPaciente.keySet())) {
            if (!r.getAplicadaEm().toLocalDate().isAfter(dia)) {
                doses.computeIfAbsent(r.getPaciente().getUuid(), k -> new HashSet<>()).add(chave(r.getImunobiologico().getUuid(), r.getDose()));
            }
        }
        return porPaciente.values().stream().map(a -> {
            Equipe e = a.getEquipe();
            Microarea m = a.getMicroarea();
            return new Crianca(e.getUnidade().getUuid(), e.getUnidade().getNome(), e.getUuid(), e.getNome(), m != null ? m.getUuid() : null,
                    m != null ? nomeDaMicroarea(m) : null, a.getPaciente().getDataNascimento(), doses.getOrDefault(a.getPaciente().getUuid(), Set.of()));
        }).toList();
    }

    private static Resultado calcular(Calendario cal, List<Crianca> criancas, LocalDate dia, Function<Crianca, Recorte> recorte) {
        Map<Recorte, List<Crianca>> grupos = new LinkedHashMap<>();
        if (recorte != null) {
            criancas.forEach(c -> grupos.computeIfAbsent(recorte.apply(c), k -> new ArrayList<>()).add(c));
        }
        List<CoberturaVacinalDto.Recorte> recortes = new ArrayList<>();
        Map<UUID, int[]> territorios = new HashMap<>();
        for (Map.Entry<Recorte, List<Crianca>> g : grupos.entrySet()) {
            List<CoberturaVacinalDto.Item> itens = new ArrayList<>();
            int naMeta = 0;
            int avaliadas = 0;
            for (EsquemaVacinal e : cal.medidores()) {
                Contagem c = contar(e, g.getValue(), dia, false);
                boolean atingida = c.atingida(e);
                itens.add(new CoberturaVacinalDto.Item(e.getUuid(), c.coorte(), c.vacinados(), c.cobertura(), atingida));
                if (c.coorte() > 0) {
                    avaliadas++;
                    naMeta += atingida ? 1 : 0;
                    int[] t = territorios.computeIfAbsent(e.getUuid(), k -> new int[2]);
                    t[0]++;
                    t[1] += atingida ? 1 : 0;
                }
            }
            recortes.add(new CoberturaVacinalDto.Recorte(g.getKey().id(), g.getKey().nome(), percentual(naMeta, avaliadas), naMeta, avaliadas, itens));
        }
        recortes.sort(Comparator.comparing((CoberturaVacinalDto.Recorte x) -> x.getId() == null).thenComparing(CoberturaVacinalDto.Recorte::getNome,
                String.CASE_INSENSITIVE_ORDER));

        List<CoberturaVacinalDto.Vacina> vacinas = new ArrayList<>();
        int naMeta = 0;
        int avaliadas = 0;
        int abandonoAlto = 0;
        for (EsquemaVacinal e : cal.medidores()) {
            Contagem c = contar(e, criancas, dia, cal.temAbandono(e));
            boolean atingida = c.atingida(e);
            Double abandono = c.abandono();
            ClasseAbandonoVacinal classe = abandono == null ? null : ClasseAbandonoVacinal.de(abandono);
            int[] t = territorios.get(e.getUuid());
            vacinas.add(new CoberturaVacinalDto.Vacina(e.getUuid(), e.getImunobiologico().getUuid(), e.getImunobiologico().getSigla(),
                    e.getImunobiologico().getNome(), e.getDose(), e.getDose().rotulo(), e.getIdadeRecomendadaMeses(), e.getMetaCobertura(),
                    c.coorte(), c.vacinados(), c.cobertura(), atingida, t == null ? null : percentual(t[1], t[0]), c.comPrimeira(), abandono, classe));
            if (c.coorte() > 0) {
                avaliadas++;
                naMeta += atingida ? 1 : 0;
            }
            abandonoAlto += classe == ClasseAbandonoVacinal.ALTA ? 1 : 0;
        }
        return new Resultado(vacinas, percentual(naMeta, avaliadas), naMeta, avaliadas, abandonoAlto, recortes);
    }

    /** A coorte da dose (de 1 a 12 meses depois da idade recomendada), as vacinadas e, com abandono, as com a 1ª dose. */
    static Contagem contar(EsquemaVacinal e, Collection<Crianca> criancas, LocalDate dia, boolean comAbandono) {
        int recomendada = e.getIdadeRecomendadaMeses();
        String dose = chave(e.getImunobiologico().getUuid(), e.getDose());
        String primeira = chave(e.getImunobiologico().getUuid(), DoseVacinal.PRIMEIRA);
        int coorte = 0;
        int vacinados = 0;
        int comPrimeira = 0;
        int completaram = 0;
        for (Crianca c : criancas) {
            long idade = CalendarioVacinal.idadeMeses(c.nascimento(), dia);
            if (idade < recomendada + 1 || idade > recomendada + JANELA_COORTE_MESES) {
                continue;
            }
            coorte++;
            boolean tem = c.doses().contains(dose);
            vacinados += tem ? 1 : 0;
            if (comAbandono && c.doses().contains(primeira)) {
                comPrimeira++;
                completaram += tem ? 1 : 0;
            }
        }
        return new Contagem(coorte, vacinados, comAbandono ? comPrimeira : null, comAbandono ? completaram : null);
    }

    /** O nome da microárea; sem nome, "Microárea" e o código. */
    static String nomeDaMicroarea(Microarea m) {
        return m.getNome() != null && !m.getNome().isBlank() ? m.getNome() : "Microárea " + m.getCodigo();
    }

    static String chave(UUID imunobiologicoId, DoseVacinal dose) {
        return imunobiologicoId + ":" + dose.name();
    }

    private static Double percentual(int parte, int total) {
        return total == 0 ? null : arredondar(100.0 * parte / total);
    }

    static double arredondar(double v) {
        return Math.round(v * 10) / 10.0;
    }
}
