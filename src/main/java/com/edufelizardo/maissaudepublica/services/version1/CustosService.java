package com.edufelizardo.maissaudepublica.services.version1;

import com.edufelizardo.maissaudepublica.config.UsuarioAutenticado;
import com.edufelizardo.maissaudepublica.exceptions.ResourceBadRequestException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceConflictException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceNotFoundException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceUnprocessableEntityException;
import com.edufelizardo.maissaudepublica.models.*;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.CustosRequestDtos;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.CustosDtos;
import com.edufelizardo.maissaudepublica.models.enuns.*;
import com.edufelizardo.maissaudepublica.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.time.*;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Financeiro, fatias F3 (ADR-0220) e F4 (ADR-0221): os centros de custo por unidade e por setor, no padrão do Programa Nacional de Gestão de Custos, e a apuração
 * mensal por competência, só com o que a plataforma já registra: a folha do RH pela lotação, o consumo do Estoque e da Farmácia, as liquidações
 * de serviço, a manutenção interna, a depreciação dos bens e as diárias do TFD. Aberta, a apuração é refeita; fechada, vira fotografia. Os
 * indicadores dão o custo por atendimento e por paciente-dia, contra a média das unidades do mesmo tipo. A F4 rateia, em degraus, o custo dos
 * centros administrativos e de apoio para os produtivos da mesma unidade, e separa a internação do ambulatório na unidade mista.
 */
@Service
public class CustosService {

    public static final String CONSULTAR = FinanceiroService.CONSULTAR;
    public static final String GERENCIAR = FinanceiroService.GERENCIAR;
    public static final String INDICADOR = "INDICADOR.CONSULTAR";
    private static final ZoneId ZONA = ZoneId.of("America/Sao_Paulo");
    /** Chave da trava consultiva que serializa a criação dos centros (o código em sequência e o geral único por unidade). */
    private static final long TRAVA_CENTROS = 2_026_112_001L;
    /** Acima da média do tipo de unidade a partir de 30%. */
    private static final BigDecimal LIMIAR = new BigDecimal("1.30");

    @Autowired private CentroCustoRepository centroRepository;
    @Autowired private ApuracaoCustoRepository apuracaoRepository;
    @Autowired private LinhaApuracaoCustoRepository linhaRepository;
    @Autowired private ProducaoApuracaoCustoRepository producaoRepository;
    @Autowired private CustoCentroApuracaoRepository custoCentroRepository;
    @Autowired private RateioApuracaoCustoRepository rateioRepository;
    @Autowired private UnidadeDeSaudeRepository unidadeRepository;
    @Autowired private SetorRepository setorRepository;
    @Autowired private ControleDeAcesso controleDeAcesso;
    @Autowired private JdbcTemplate jdbc;

    // ── Centros de custo ───────────────────────────────────────────────────────────────────────────

    /** Os centros, com o geral de cada unidade ativa criado na primeira leitura (ADR-0209: gerado ao ler, sem agendador). */
    @Transactional
    public List<CustosDtos.Centro> centros() {
        controleDeAcesso.exigirAlguma(CONSULTAR, GERENCIAR);
        garantirGerais();
        return centroRepository.findTodos().stream().map(CustosService::dto).toList();
    }

    /** O centro de um setor (404), um por setor (409), com o nome e o tipo do setor quando não vierem. */
    @Transactional
    public CustosDtos.Centro criar(CustosRequestDtos.NovoCentro dto) {
        controleDeAcesso.exigirAlguma(GERENCIAR);
        Setor s = setorRepository.findById(dto.setorId()).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar o setor " + dto.setorId() + " em nossos registros."));
        geral(s.getUnidade());
        if (centroRepository.existsBySetor_Uuid(s.getUuid())) {
            throw new ResourceConflictException("O setor " + s.getNome() + " já tem centro de custo.");
        }
        String nome = limpo(dto.nome());
        CentroCusto c = novo(s.getUnidade(), s, nome != null ? nome : s.getNome(), dto.tipo() != null ? dto.tipo() : tipo(s.getTipo()));
        rateio(c, dto.criterioRateio(), dto.areaM2());
        return dto(centroRepository.save(c));
    }

    /**
     * O nome, o tipo, se está ativo, o critério e a área (404); o geral não se desativa (422), porque recebe o custo sem setor; o produtivo não
     * tem critério de rateio (422).
     */
    @Transactional
    public CustosDtos.Centro atualizar(UUID id, CustosRequestDtos.AtualizacaoCentro dto) {
        controleDeAcesso.exigirAlguma(GERENCIAR);
        CentroCusto c = centroRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar o centro de custo " + id + " em nossos registros."));
        if (c.getSetor() == null && !dto.ativo()) {
            throw new ResourceUnprocessableEntityException("O centro geral da unidade recebe o custo sem setor e não se desativa.");
        }
        c.setNome(dto.nome().trim());
        c.setTipo(dto.tipo());
        c.setAtivo(dto.ativo());
        rateio(c, dto.criterioRateio(), dto.areaM2());
        return dto(c);
    }

    // ── Apuração ───────────────────────────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<CustosDtos.ApuracaoResumo> apuracoes() {
        controleDeAcesso.exigirAlguma(CONSULTAR, GERENCIAR);
        Map<UUID, BigDecimal> totais = linhaRepository.findTotais().stream().collect(Collectors.toMap(l -> (UUID) l[0], l -> (BigDecimal) l[1]));
        return apuracaoRepository.findAllByOrderByCompetenciaDesc().stream().map(a -> new CustosDtos.ApuracaoResumo(a.getUuid(), a.getCompetencia(),
                a.getSituacao(), totais.getOrDefault(a.getUuid(), BigDecimal.ZERO), a.getApuradaEm(), a.getFechadaEm())).toList();
    }

    @Transactional(readOnly = true)
    public CustosDtos.Apuracao apuracao(String competencia) {
        controleDeAcesso.exigirAlguma(CONSULTAR, GERENCIAR);
        return dto(buscar(competencia));
    }

    /**
     * Apura a competência já encerrada (400 no formato, 422 no mês corrente ou futuro): cria, ou refaz a aberta (422 se fechada). Cada custo vai
     * ao centro do setor de onde saiu, quando o setor tem centro ativo, ou ao geral da unidade; depois, o rateio em degraus (ADR-0221).
     */
    @Transactional
    public CustosDtos.Apuracao apurar(String competencia) {
        controleDeAcesso.exigirAlguma(GERENCIAR);
        YearMonth mes = mes(competencia);
        if (!mes.isBefore(YearMonth.now(ZONA))) {
            throw new ResourceUnprocessableEntityException("A competência " + competencia + " ainda não terminou; a apuração é do mês encerrado.");
        }
        ApuracaoCusto a = apuracaoRepository.findByCompetencia(mes.toString()).orElse(null);
        if (a != null && a.getSituacao() == SituacaoApuracaoCusto.FECHADA) {
            throw new ResourceUnprocessableEntityException("A apuração de " + competencia + " está fechada e não muda.");
        }
        if (a == null) {
            a = new ApuracaoCusto();
            a.setCompetencia(mes.toString());
            a.setSituacao(SituacaoApuracaoCusto.ABERTA);
        } else {
            linhaRepository.apagarDaApuracao(a.getUuid());
            producaoRepository.apagarDaApuracao(a.getUuid());
            custoCentroRepository.apagarDaApuracao(a.getUuid());
            rateioRepository.apagarDaApuracao(a.getUuid());
        }
        garantirGerais();
        Apurador ap = new Apurador(centroRepository.findTodos());
        pessoal(ap, mes);
        material(ap, mes);
        medicamentos(ap, mes);
        servicos(ap, mes);
        manutencao(ap, mes);
        depreciacao(ap, mes);
        diarias(ap, mes);
        outras(ap, mes);
        a.setNaoAlocado(ap.naoAlocado.setScale(2, RoundingMode.HALF_UP));
        a.setSaidasSemCusto(ap.semCusto);
        Rateador r = ratear(ap, mes);
        a.setNaoRateado(r.naoRateado);
        a.setApuradaEm(Instant.now());
        a.setApuradaPorCpf(UsuarioAutenticado.cpf());
        ApuracaoCusto salva = apuracaoRepository.save(a);
        for (Map.Entry<UUID, EnumMap<CategoriaCusto, BigDecimal>> c : ap.valores.entrySet()) {
            for (Map.Entry<CategoriaCusto, BigDecimal> v : c.getValue().entrySet()) {
                BigDecimal valor = v.getValue().setScale(2, RoundingMode.HALF_UP);
                if (valor.signum() != 0) {
                    LinhaApuracaoCusto l = new LinhaApuracaoCusto();
                    l.setApuracao(salva);
                    l.setCentro(ap.centros.get(c.getKey()));
                    l.setCategoria(v.getKey());
                    l.setValor(valor);
                    linhaRepository.save(l);
                }
            }
        }
        r.salvar(salva, custoCentroRepository, rateioRepository);
        producao(salva, mes);
        linhaRepository.flush();
        return dto(salva);
    }

    /** Fecha a apuração aberta (404, 422): daí em diante é fotografia, sem os lançamentos atrasados. */
    @Transactional
    public CustosDtos.Apuracao fechar(UUID id) {
        controleDeAcesso.exigirAlguma(GERENCIAR);
        ApuracaoCusto a = apuracaoRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar a apuração " + id + " em nossos registros."));
        if (a.getSituacao() == SituacaoApuracaoCusto.FECHADA) {
            throw new ResourceUnprocessableEntityException("A apuração de " + a.getCompetencia() + " já está fechada.");
        }
        a.setSituacao(SituacaoApuracaoCusto.FECHADA);
        a.setFechadaEm(Instant.now());
        a.setFechadaPorCpf(UsuarioAutenticado.cpf());
        return dto(a);
    }

    // ── Indicadores ────────────────────────────────────────────────────────────────────────────────

    /**
     * O custo por unidade da competência apurada (404 sem apuração), com o custo por atendimento e por paciente-dia e a comparação com a média do
     * tipo de unidade. Quem só tem INDICADOR.CONSULTAR vê as unidades do acesso; a média é sempre da rede.
     */
    @Transactional(readOnly = true)
    public CustosDtos.Indicadores indicadores(String competencia) {
        controleDeAcesso.exigirAlguma(CONSULTAR, GERENCIAR, INDICADOR);
        ApuracaoCusto a = buscar(competencia);
        Map<UUID, EnumMap<CategoriaCusto, BigDecimal>> porUnidade = new HashMap<>();
        Map<UUID, UnidadeDeSaude> unidades = new HashMap<>();
        for (LinhaApuracaoCusto l : linhaRepository.findDaApuracao(a.getUuid())) {
            UnidadeDeSaude u = l.getCentro().getUnidade();
            unidades.put(u.getUuid(), u);
            porUnidade.computeIfAbsent(u.getUuid(), k -> new EnumMap<>(CategoriaCusto.class)).merge(l.getCategoria(), l.getValor(), BigDecimal::add);
        }
        Map<UUID, BigDecimal> internacao = new HashMap<>();
        for (CustoCentroApuracao c : custoCentroRepository.findDaApuracao(a.getUuid())) {
            if (c.isInternacao()) {
                internacao.merge(c.getCentro().getUnidade().getUuid(), c.getDireto().add(c.getRecebido()).subtract(c.getEnviado()), BigDecimal::add);
            }
        }
        Map<UUID, ProducaoApuracaoCusto> producao = new HashMap<>();
        for (ProducaoApuracaoCusto p : producaoRepository.findDaApuracao(a.getUuid())) {
            producao.put(p.getUnidade().getUuid(), p);
        }
        List<Object[]> base = new ArrayList<>();
        for (UUID id : porUnidade.keySet()) {
            UnidadeDeSaude u = unidades.get(id);
            BigDecimal total = porUnidade.get(id).values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
            ProducaoApuracaoCusto p = producao.get(id);
            int atend = p == null ? 0 : p.getAtendimentos();
            int pd = p == null ? 0 : p.getPacientesDia();
            BigDecimal deInternacao = internacao.get(id);
            if (deInternacao == null) {
                base.add(new Object[]{u, total, atend, pd, dividir(total, atend), dividir(total, pd)});
            } else {
                // A unidade mista (ADR-0221): a internação vai ao paciente-dia; o resto, ao atendimento.
                base.add(new Object[]{u, total, atend, pd, dividir(total.subtract(deInternacao), atend), dividir(deInternacao, pd)});
            }
        }
        Map<TipoUnidadeDeSaude, BigDecimal> medias = base.stream().filter(x -> x[4] != null && ((UnidadeDeSaude) x[0]).getTipo() != null)
                .collect(Collectors.groupingBy(x -> ((UnidadeDeSaude) x[0]).getTipo(), Collectors.collectingAndThen(Collectors.toList(), l -> dividir(
                        l.stream().map(x -> (BigDecimal) x[4]).reduce(BigDecimal.ZERO, BigDecimal::add), l.size()))));
        Optional<Set<UUID>> visiveis = controleDeAcesso.temAlguma(CONSULTAR, GERENCIAR) ? Optional.empty() : controleDeAcesso.unidadesVisiveis(INDICADOR);
        List<CustosDtos.IndicadorUnidade> lista = base.stream()
                .filter(x -> visiveis.isEmpty() || visiveis.get().contains(((UnidadeDeSaude) x[0]).getUuid()))
                .map(x -> {
                    UnidadeDeSaude u = (UnidadeDeSaude) x[0];
                    BigDecimal porAtend = (BigDecimal) x[4];
                    BigDecimal media = u.getTipo() == null ? null : medias.get(u.getTipo());
                    boolean acima = porAtend != null && media != null && media.signum() > 0 && porAtend.compareTo(media.multiply(LIMIAR)) > 0;
                    return new CustosDtos.IndicadorUnidade(u.getUuid(), u.getNome(), u.getTipo(), (BigDecimal) x[1], ordenado(porUnidade.get(u.getUuid())),
                            (int) x[2], (int) x[3], porAtend, (BigDecimal) x[5], media, acima);
                })
                .sorted(Comparator.comparing(CustosDtos.IndicadorUnidade::total).reversed())
                .toList();
        return new CustosDtos.Indicadores(a.getCompetencia(), a.getSituacao(), lista);
    }

    // ── As fontes da apuração ──────────────────────────────────────────────────────────────────────

    /** A folha da competência (MM/AAAA no RH) pela lotação vigente no mês, no setor da lotação (ADR-0221); sem lotação, não alocado. */
    private void pessoal(Apurador ap, YearMonth mes) {
        String rh = String.format("%02d/%d", mes.getMonthValue(), mes.getYear());
        jdbc.query("select f.total, l.unidade_id, l.setor_id from tb_folha_pagamento f left join lateral (select x.unidade_id, x.setor_id from tb_lotacao x "
                        + "where x.profissional_id = f.profissional_id and x.data_inicio <= ? and (x.data_fim is null or x.data_fim >= ?) "
                        + "order by x.data_inicio desc limit 1) l on true where f.competencia in (?, ?)",
                rs -> {
                    ap.lancar((UUID) rs.getObject(2), (UUID) rs.getObject(3), CategoriaCusto.PESSOAL, rs.getBigDecimal(1));
                }, mes.atEndOfMonth(), mes.atDay(1), rh, mes.toString());
    }

    /** As saídas de consumo do Estoque no mês, pelo custo do lote, no setor de destino. */
    private void material(Apurador ap, YearMonth mes) {
        jdbc.query("select l.unidade_id, m.setor_id, -m.quantidade, l.custo_unitario from tb_movimentacao_estoque m join tb_lote_material l on l.uuid = m.lote_id "
                        + "where m.tipo = 'SAIDA_CONSUMO' and m.registrado_em >= ? and m.registrado_em < ?",
                rs -> {
                    BigDecimal custo = rs.getBigDecimal(4);
                    if (custo == null) {
                        ap.semCusto++;
                    } else {
                        ap.lancar((UUID) rs.getObject(1), (UUID) rs.getObject(2), CategoriaCusto.MATERIAL_CONSUMO, custo.multiply(BigDecimal.valueOf(rs.getInt(3))));
                    }
                }, inicio(mes), inicio(mes.plusMonths(1)));
    }

    /**
     * As dispensações e administrações da Farmácia no mês (menos os estornos), pelo preço do contrato do lote recebido de Compras: mesmo
     * medicamento, mesmo número de lote e mesma unidade. O lote sem recebimento fica sem custo.
     */
    private void medicamentos(Apurador ap, YearMonth mes) {
        jdbc.query("select l.unidade_id, -m.quantidade, (select ii.preco_unitario from tb_item_recebimento_compra ir "
                        + "join tb_item_ordem_fornecimento io on io.uuid = ir.item_ordem_id join tb_ordem_fornecimento o on o.uuid = io.ordem_id "
                        + "join tb_item_instrumento ii on ii.uuid = io.item_instrumento_id join tb_item_contratacao ic on ic.uuid = ii.item_contratacao_id "
                        + "where ic.medicamento_id = l.medicamento_id and ir.numero_lote = l.numero_lote and o.unidade_id = l.unidade_id "
                        + "order by o.emitida_em desc limit 1) from tb_movimentacao_farmacia m join tb_lote l on l.uuid = m.lote_id "
                        + "where m.tipo in ('DISPENSACAO', 'ADMINISTRACAO', 'ADMINISTRACAO_ESTORNO') and m.registrado_em >= ? and m.registrado_em < ?",
                rs -> {
                    BigDecimal preco = rs.getBigDecimal(3);
                    if (preco == null) {
                        ap.semCusto++;
                    } else {
                        ap.lancar((UUID) rs.getObject(1), null, CategoriaCusto.MEDICAMENTOS, preco.multiply(BigDecimal.valueOf(rs.getInt(2))));
                    }
                }, inicio(mes), inicio(mes.plusMonths(1)));
    }

    /**
     * As liquidações de serviço no mês: do recebimento, a parte de serviço da ordem de fornecimento (o material entra pelo consumo e o bem pela
     * depreciação); da ordem de serviço por contrato, inteira, no setor do bem.
     */
    private void servicos(Apurador ap, YearMonth mes) {
        String valor = "(select coalesce(sum(io.quantidade * ii.preco_unitario), 0) from tb_item_ordem_fornecimento io join tb_item_instrumento ii "
                + "on ii.uuid = io.item_instrumento_id join tb_item_contratacao ic on ic.uuid = ii.item_contratacao_id where io.ordem_id = o.uuid%s)";
        jdbc.query("select o.unidade_id, li.valor_bruto, " + valor.formatted(" and ic.tipo = 'SERVICO'") + ", " + valor.formatted("")
                        + " from tb_liquidacao li join tb_recebimento_compra r on r.uuid = li.recebimento_id join tb_ordem_fornecimento o on o.uuid = r.ordem_id "
                        + "where li.origem = 'RECEBIMENTO' and li.liquidada_em between ? and ?",
                rs -> {
                    BigDecimal servico = rs.getBigDecimal(3);
                    BigDecimal total = rs.getBigDecimal(4);
                    if (servico.signum() > 0 && total.signum() > 0) {
                        ap.lancar((UUID) rs.getObject(1), null, CategoriaCusto.SERVICOS_TERCEIROS,
                                rs.getBigDecimal(2).multiply(servico).divide(total, 6, RoundingMode.HALF_UP));
                    }
                }, mes.atDay(1), mes.atEndOfMonth());
        jdbc.query("select b.unidade_id, b.setor_id, li.valor_bruto from tb_liquidacao li join tb_ordem_servico_manutencao os on os.uuid = li.ordem_servico_id "
                        + "join tb_bem_patrimonial b on b.uuid = os.bem_id where li.origem = 'ORDEM_SERVICO' and li.liquidada_em between ? and ?",
                rs -> {
                    ap.lancar((UUID) rs.getObject(1), (UUID) rs.getObject(2), CategoriaCusto.SERVICOS_TERCEIROS, rs.getBigDecimal(3));
                }, mes.atDay(1), mes.atEndOfMonth());
    }

    /** As ordens de serviço internas concluídas no mês: a mão de obra e as peças compradas (a peça do Estoque já entrou no consumo). */
    private void manutencao(Apurador ap, YearMonth mes) {
        jdbc.query("select b.unidade_id, b.setor_id, coalesce(os.custo_mao_de_obra, 0) + coalesce((select sum(p.quantidade * p.custo_unitario) "
                        + "from tb_peca_ordem_servico p where p.ordem_id = os.uuid and p.material_id is null), 0) from tb_ordem_servico_manutencao os "
                        + "join tb_bem_patrimonial b on b.uuid = os.bem_id where os.situacao = 'CONCLUIDA' and coalesce(os.execucao, 'INTERNA') = 'INTERNA' "
                        + "and os.concluida_em between ? and ?",
                rs -> {
                    ap.lancar((UUID) rs.getObject(1), (UUID) rs.getObject(2), CategoriaCusto.MANUTENCAO, rs.getBigDecimal(3));
                }, mes.atDay(1), mes.atEndOfMonth());
    }

    /** A parcela do mês dos bens tombados, pela mesma conta do valor contábil (ADR-0217); o bem baixado antes do mês fica de fora. */
    private void depreciacao(Apurador ap, YearMonth mes) {
        LocalDate fimAnterior = mes.atDay(1).minusDays(1);
        jdbc.query("select b.unidade_id, b.setor_id, b.classe, b.valor_aquisicao, b.data_aquisicao from tb_bem_patrimonial b where b.numero_tombamento "
                        + "is not null and b.valor_aquisicao is not null and b.classe is not null and b.data_aquisicao <= ? and not exists (select 1 from "
                        + "tb_baixa_bem x where x.bem_id = b.uuid and x.situacao = 'CONFIRMADA' and x.data_baixa < ?)",
                rs -> {
                    BemPatrimonial b = new BemPatrimonial();
                    b.setClasse(ClasseBem.valueOf(rs.getString(3)));
                    b.setValorAquisicao(rs.getBigDecimal(4));
                    b.setDataAquisicao(rs.getObject(5, LocalDate.class));
                    BigDecimal antes = PatrimonioService.valorContabil(b, fimAnterior.isBefore(b.getDataAquisicao()) ? b.getDataAquisicao() : fimAnterior);
                    BigDecimal depois = PatrimonioService.valorContabil(b, mes.atEndOfMonth());
                    if (antes != null && depois != null) {
                        ap.lancar((UUID) rs.getObject(1), (UUID) rs.getObject(2), CategoriaCusto.DEPRECIACAO, antes.subtract(depois));
                    }
                }, mes.atEndOfMonth(), mes.atDay(1));
    }

    /** As diárias do TFD liquidadas no mês, na unidade que pediu o transporte. */
    private void diarias(Apurador ap, YearMonth mes) {
        jdbc.query("select s.unidade_id, li.valor_bruto from tb_liquidacao li join tb_diaria_tfd d on d.uuid = li.diaria_id join tb_solicitacao_transporte s "
                        + "on s.uuid = d.solicitacao_id where li.origem = 'DIARIA_TFD' and li.liquidada_em between ? and ?",
                rs -> {
                    ap.lancar((UUID) rs.getObject(1), null, CategoriaCusto.DIARIAS_TFD, rs.getBigDecimal(2));
                }, mes.atDay(1), mes.atEndOfMonth());
    }

    /** As liquidações avulsas do mês, no centro informado; a antiga, sem centro, fica não alocada. */
    private void outras(Apurador ap, YearMonth mes) {
        jdbc.query("select li.centro_custo_id, li.valor_bruto from tb_liquidacao li where li.origem = 'AVULSA' and li.liquidada_em between ? and ?",
                rs -> {
                    ap.noCentro((UUID) rs.getObject(1), CategoriaCusto.OUTRAS, rs.getBigDecimal(2));
                }, mes.atDay(1), mes.atEndOfMonth());
    }

    /** Os atendimentos concluídos e os pacientes-dia do censo vigente, por unidade. */
    private void producao(ApuracaoCusto a, YearMonth mes) {
        Map<UUID, int[]> p = new HashMap<>();
        jdbc.query("select unidade_id, count(*) from tb_atendimento where status = 'CONCLUIDO' and data_hora >= ? and data_hora < ? group by unidade_id",
                rs -> {
                    p.computeIfAbsent((UUID) rs.getObject(1), k -> new int[2])[0] = rs.getInt(2);
                }, Timestamp.valueOf(mes.atDay(1).atStartOfDay()), Timestamp.valueOf(mes.plusMonths(1).atDay(1).atStartOfDay()));
        jdbc.query("select unidade_id, sum(paciente_dia) from tb_censo_diario where not reaberto and data between ? and ? group by unidade_id",
                rs -> {
                    p.computeIfAbsent((UUID) rs.getObject(1), k -> new int[2])[1] = rs.getInt(2);
                }, mes.atDay(1), mes.atEndOfMonth());
        Map<UUID, UnidadeDeSaude> unidades = unidadeRepository.findAllById(p.keySet()).stream().collect(Collectors.toMap(UnidadeDeSaude::getUuid, Function.identity()));
        p.forEach((id, v) -> {
            ProducaoApuracaoCusto x = new ProducaoApuracaoCusto();
            x.setApuracao(a);
            x.setUnidade(unidades.get(id));
            x.setAtendimentos(v[0]);
            x.setPacientesDia(v[1]);
            producaoRepository.save(x);
        });
    }

    /** O acumulador da apuração: o centro do setor (ativo) ou o geral da unidade; o que não acha centro vai para o não alocado. */
    private static final class Apurador {
        final Map<UUID, CentroCusto> centros = new HashMap<>();
        final Map<UUID, UUID> porSetor = new HashMap<>();
        final Map<UUID, UUID> geral = new HashMap<>();
        final Map<UUID, EnumMap<CategoriaCusto, BigDecimal>> valores = new LinkedHashMap<>();
        BigDecimal naoAlocado = BigDecimal.ZERO;
        int semCusto;

        Apurador(List<CentroCusto> lista) {
            for (CentroCusto c : lista) {
                centros.put(c.getUuid(), c);
                if (c.getSetor() == null) {
                    geral.put(c.getUnidade().getUuid(), c.getUuid());
                } else if (c.isAtivo()) {
                    porSetor.put(c.getSetor().getUuid(), c.getUuid());
                }
            }
        }

        void lancar(UUID unidadeId, UUID setorId, CategoriaCusto categoria, BigDecimal valor) {
            noCentro(centroDe(unidadeId, setorId), categoria, valor);
        }

        UUID centroDe(UUID unidadeId, UUID setorId) {
            return setorId != null && porSetor.containsKey(setorId) ? porSetor.get(setorId) : unidadeId == null ? null : geral.get(unidadeId);
        }

        void noCentro(UUID centro, CategoriaCusto categoria, BigDecimal valor) {
            if (valor == null || valor.signum() == 0) {
                return;
            }
            if (centro == null || !centros.containsKey(centro)) {
                naoAlocado = naoAlocado.add(valor);
                return;
            }
            valores.computeIfAbsent(centro, k -> new EnumMap<>(CategoriaCusto.class)).merge(categoria, valor, BigDecimal::add);
        }
    }

    // ── O rateio em degraus (ADR-0221) ───────────────────────────────────────────────────────────────

    /**
     * O rateio do PNGC em cada unidade: os administrativos, depois os de apoio, na ordem do código, mandam o que têm (o direto e o recebido) aos
     * centros ativos da unidade que ainda não ratearam e aos produtivos, pela base do critério; base zero, pelo custo direto; sem destino, o custo
     * fica e é contado como não rateado. Quem já rateou não recebe de volta.
     */
    private Rateador ratear(Apurador ap, YearMonth mes) {
        Map<UUID, BigDecimal> horas = new HashMap<>();
        jdbc.query("select unidade_id, setor_id, coalesce(jornada_semanal_horas, 40) from tb_lotacao where data_inicio <= ? and (data_fim is null or data_fim >= ?)",
                rs -> {
                    UUID c = ap.centroDe((UUID) rs.getObject(1), (UUID) rs.getObject(2));
                    if (c != null) {
                        horas.merge(c, BigDecimal.valueOf(rs.getInt(3)), BigDecimal::add);
                    }
                }, mes.atEndOfMonth(), mes.atDay(1));
        Map<UUID, BigDecimal> requisicoes = new HashMap<>();
        jdbc.query("select l.unidade_id, m.setor_id, count(*) from tb_movimentacao_estoque m join tb_lote_material l on l.uuid = m.lote_id "
                        + "where m.tipo = 'SAIDA_CONSUMO' and m.registrado_em >= ? and m.registrado_em < ? group by l.unidade_id, m.setor_id",
                rs -> {
                    UUID c = ap.centroDe((UUID) rs.getObject(1), (UUID) rs.getObject(2));
                    if (c != null) {
                        requisicoes.merge(c, BigDecimal.valueOf(rs.getLong(3)), BigDecimal::add);
                    }
                }, inicio(mes), inicio(mes.plusMonths(1)));
        Set<UUID> setoresComLeito = new HashSet<>(jdbc.queryForList("select distinct setor_id from tb_leito where ativo and setor_id is not null", UUID.class));
        Rateador r = new Rateador();
        Map<UUID, List<CentroCusto>> porUnidade = new HashMap<>();
        for (CentroCusto c : ap.centros.values()) {
            BigDecimal direto = ap.valores.getOrDefault(c.getUuid(), new EnumMap<>(CategoriaCusto.class)).values().stream()
                    .map(v -> v.setScale(2, RoundingMode.HALF_UP)).reduce(BigDecimal.ZERO, BigDecimal::add);
            if (c.isAtivo() || direto.signum() != 0) {
                r.direto.put(c.getUuid(), direto);
                r.centros.put(c.getUuid(), c);
                porUnidade.computeIfAbsent(c.getUnidade().getUuid(), k -> new ArrayList<>()).add(c);
            }
        }
        for (List<CentroCusto> lista : porUnidade.values()) {
            List<CentroCusto> sequencia = lista.stream().filter(c -> c.getTipo() != TipoCentroCusto.PRODUTIVO)
                    .sorted(Comparator.comparing((CentroCusto c) -> c.getTipo() == TipoCentroCusto.ADMINISTRATIVO ? 0 : 1).thenComparing(CentroCusto::getCodigo))
                    .toList();
            Set<UUID> rateados = new HashSet<>();
            int ordem = 0;
            for (CentroCusto origem : sequencia) {
                ordem++;
                rateados.add(origem.getUuid());
                BigDecimal valor = r.saldo(origem.getUuid());
                if (valor.signum() <= 0) {
                    continue;
                }
                List<CentroCusto> destinos = lista.stream().filter(d -> d.isAtivo() && !rateados.contains(d.getUuid()))
                        .sorted(Comparator.comparing(CentroCusto::getCodigo)).toList();
                CriterioRateio criterio = criterio(origem);
                Map<UUID, BigDecimal> base = base(criterio, destinos, r.direto, horas, requisicoes);
                if (soma(base).signum() == 0 && criterio != CriterioRateio.CUSTO_DIRETO) {
                    criterio = CriterioRateio.CUSTO_DIRETO;
                    base = base(criterio, destinos, r.direto, horas, requisicoes);
                }
                Map<UUID, BigDecimal> pesos = base;
                BigDecimal total = soma(pesos);
                if (total.signum() == 0) {
                    continue;
                }
                List<UUID> comBase = destinos.stream().map(CentroCusto::getUuid).filter(id -> pesos.get(id).signum() > 0).toList();
                BigDecimal distribuido = BigDecimal.ZERO;
                for (int i = 0; i < comBase.size(); i++) {
                    UUID d = comBase.get(i);
                    BigDecimal parte = i == comBase.size() - 1 ? valor.subtract(distribuido)
                            : valor.multiply(pesos.get(d)).divide(total, 2, RoundingMode.HALF_UP);
                    distribuido = distribuido.add(parte);
                    r.mandar(origem, r.centros.get(d), ordem, criterio, parte);
                }
            }
            for (CentroCusto c : sequencia) {
                r.naoRateado = r.naoRateado.add(r.saldo(c.getUuid()));
            }
            for (CentroCusto c : lista) {
                if (c.getTipo() == TipoCentroCusto.PRODUTIVO && c.getSetor() != null && setoresComLeito.contains(c.getSetor().getUuid())) {
                    r.internacao.add(c.getUuid());
                }
            }
        }
        return r;
    }

    private static Map<UUID, BigDecimal> base(CriterioRateio criterio, List<CentroCusto> destinos, Map<UUID, BigDecimal> direto, Map<UUID, BigDecimal> horas,
                                              Map<UUID, BigDecimal> requisicoes) {
        Map<UUID, BigDecimal> m = new LinkedHashMap<>();
        for (CentroCusto d : destinos) {
            BigDecimal v = switch (criterio) {
                case PESSOAS -> horas.get(d.getUuid());
                case CUSTO_DIRETO -> direto.get(d.getUuid());
                case AREA -> d.getAreaM2();
                case REQUISICOES -> requisicoes.get(d.getUuid());
            };
            m.put(d.getUuid(), v == null || v.signum() < 0 ? BigDecimal.ZERO : v);
        }
        return m;
    }

    private static BigDecimal soma(Map<UUID, BigDecimal> m) {
        return m.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /** O acumulador do rateio: o direto, o recebido e o enviado de cada centro, as linhas e o não rateado. */
    private static final class Rateador {
        final Map<UUID, CentroCusto> centros = new HashMap<>();
        final Map<UUID, BigDecimal> direto = new HashMap<>();
        final Map<UUID, BigDecimal> recebido = new HashMap<>();
        final Map<UUID, BigDecimal> enviado = new HashMap<>();
        final Set<UUID> internacao = new HashSet<>();
        final List<Object[]> linhas = new ArrayList<>();
        BigDecimal naoRateado = BigDecimal.ZERO;

        BigDecimal saldo(UUID c) {
            return direto.getOrDefault(c, BigDecimal.ZERO).add(recebido.getOrDefault(c, BigDecimal.ZERO)).subtract(enviado.getOrDefault(c, BigDecimal.ZERO));
        }

        void mandar(CentroCusto origem, CentroCusto destino, int ordem, CriterioRateio criterio, BigDecimal valor) {
            if (valor.signum() == 0) {
                return;
            }
            enviado.merge(origem.getUuid(), valor, BigDecimal::add);
            recebido.merge(destino.getUuid(), valor, BigDecimal::add);
            linhas.add(new Object[]{origem, destino, ordem, criterio, valor});
        }

        void salvar(ApuracaoCusto a, CustoCentroApuracaoRepository custos, RateioApuracaoCustoRepository rateios) {
            for (CentroCusto c : centros.values()) {
                BigDecimal d = direto.getOrDefault(c.getUuid(), BigDecimal.ZERO);
                BigDecimal rec = recebido.getOrDefault(c.getUuid(), BigDecimal.ZERO);
                BigDecimal env = enviado.getOrDefault(c.getUuid(), BigDecimal.ZERO);
                if (d.signum() != 0 || rec.signum() != 0 || env.signum() != 0) {
                    custos.save(new CustoCentroApuracao(null, a, c, d, rec, env, internacao.contains(c.getUuid())));
                }
            }
            for (Object[] l : linhas) {
                rateios.save(new RateioApuracaoCusto(null, a, (CentroCusto) l[0], (CentroCusto) l[1], (int) l[2], (CriterioRateio) l[3], (BigDecimal) l[4]));
            }
        }
    }

    // ── Auxiliares ─────────────────────────────────────────────────────────────────────────────────

    /**
     * O centro geral de cada unidade ativa que ainda não tem. A trava da transação serializa as leituras simultâneas (a tela pede os centros
     * e a apuração juntas), que de outro modo criariam o mesmo código.
     */
    private void garantirGerais() {
        travarCentros();
        Set<UUID> com = centroRepository.findTodos().stream().filter(c -> c.getSetor() == null).map(c -> c.getUnidade().getUuid()).collect(Collectors.toSet());
        for (UnidadeDeSaude u : unidadeRepository.findAll()) {
            if (u.isAtivo() && !com.contains(u.getUuid())) {
                centroRepository.saveAndFlush(novo(u, null, "Geral", tipo(u.getTipo())));
            }
        }
    }

    /** O geral da unidade, criado se faltar (mesmo para a inativa que ganha centro de setor). */
    CentroCusto geral(UnidadeDeSaude u) {
        travarCentros();
        return centroRepository.findByUnidade_UuidAndSetorIsNull(u.getUuid()).orElseGet(() -> centroRepository.saveAndFlush(novo(u, null, "Geral", tipo(u.getTipo()))));
    }

    /** O centro ativo para a liquidação avulsa (404, 422). */
    CentroCusto ativo(UUID id) {
        CentroCusto c = centroRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar o centro de custo " + id + " em nossos registros."));
        if (!c.isAtivo()) {
            throw new ResourceUnprocessableEntityException("O centro de custo " + c.getCodigo() + " está inativo.");
        }
        return c;
    }

    private void travarCentros() {
        jdbc.execute("select pg_advisory_xact_lock(" + TRAVA_CENTROS + ")");
    }

    private CentroCusto novo(UnidadeDeSaude u, Setor s, String nome, TipoCentroCusto tipo) {
        String maior = centroRepository.findMaiorCodigo();
        long n = maior == null ? 1 : Long.parseLong(maior.substring(3)) + 1;
        CentroCusto c = new CentroCusto();
        c.setUnidade(u);
        c.setSetor(s);
        c.setCodigo("CC-" + String.format("%05d", n));
        c.setNome(nome.length() > 150 ? nome.substring(0, 150) : nome);
        c.setTipo(tipo);
        c.setAtivo(true);
        c.setRegistradoEm(Instant.now());
        c.setRegistradoPorCpf(UsuarioAutenticado.cpf());
        return c;
    }

    /** A gestão (federal, estadual, municipal, regional) é administrativa; o laboratório, apoio; o resto atende. */
    private static TipoCentroCusto tipo(TipoUnidadeDeSaude t) {
        if (t == null) {
            return TipoCentroCusto.PRODUTIVO;
        }
        return switch (t) {
            case FEDERAL, ESTADUAL, MUNICIPAL, REGIONAL -> TipoCentroCusto.ADMINISTRATIVO;
            case LABORATORIO -> TipoCentroCusto.APOIO;
            default -> TipoCentroCusto.PRODUTIVO;
        };
    }

    private static TipoCentroCusto tipo(TipoSetor t) {
        if (t == null) {
            return TipoCentroCusto.PRODUTIVO;
        }
        return switch (t) {
            case ASSISTENCIAL -> TipoCentroCusto.PRODUTIVO;
            case APOIO, TECNICO -> TipoCentroCusto.APOIO;
            case ADMINISTRATIVO -> TipoCentroCusto.ADMINISTRATIVO;
        };
    }

    private ApuracaoCusto buscar(String competencia) {
        YearMonth mes = mes(competencia);
        return apuracaoRepository.findByCompetencia(mes.toString()).orElseThrow(() -> new ResourceNotFoundException(
                "A competência " + competencia + " ainda não foi apurada."));
    }

    private static YearMonth mes(String competencia) {
        if (competencia == null || !competencia.matches("\\d{4}-(0[1-9]|1[0-2])")) {
            throw new ResourceBadRequestException("A competência é AAAA-MM.");
        }
        return YearMonth.parse(competencia);
    }

    private static Timestamp inicio(YearMonth mes) {
        return Timestamp.from(mes.atDay(1).atStartOfDay(ZONA).toInstant());
    }

    private static BigDecimal dividir(BigDecimal valor, int por) {
        return por <= 0 ? null : valor.divide(BigDecimal.valueOf(por), 2, RoundingMode.HALF_UP);
    }

    private static Map<CategoriaCusto, BigDecimal> ordenado(Map<CategoriaCusto, BigDecimal> m) {
        Map<CategoriaCusto, BigDecimal> r = new LinkedHashMap<>();
        for (CategoriaCusto c : CategoriaCusto.values()) {
            if (m != null && m.containsKey(c)) {
                r.put(c, m.get(c));
            }
        }
        return r;
    }

    private CustosDtos.Apuracao dto(ApuracaoCusto a) {
        Map<UUID, EnumMap<CategoriaCusto, BigDecimal>> porCentro = new LinkedHashMap<>();
        Map<UUID, CentroCusto> centros = new HashMap<>();
        EnumMap<CategoriaCusto, BigDecimal> porCategoria = new EnumMap<>(CategoriaCusto.class);
        for (LinhaApuracaoCusto l : linhaRepository.findDaApuracao(a.getUuid())) {
            centros.put(l.getCentro().getUuid(), l.getCentro());
            porCentro.computeIfAbsent(l.getCentro().getUuid(), k -> new EnumMap<>(CategoriaCusto.class)).merge(l.getCategoria(), l.getValor(), BigDecimal::add);
            porCategoria.merge(l.getCategoria(), l.getValor(), BigDecimal::add);
        }
        Map<UUID, CustoCentroApuracao> resumo = new HashMap<>();
        for (CustoCentroApuracao c : custoCentroRepository.findDaApuracao(a.getUuid())) {
            resumo.put(c.getCentro().getUuid(), c);
            centros.putIfAbsent(c.getCentro().getUuid(), c.getCentro());
            porCentro.putIfAbsent(c.getCentro().getUuid(), new EnumMap<>(CategoriaCusto.class));
        }
        List<CustosDtos.LinhaCentro> linhas = porCentro.entrySet().stream().map(e -> {
                    CentroCusto c = centros.get(e.getKey());
                    CustoCentroApuracao r = resumo.get(e.getKey());
                    BigDecimal direto = e.getValue().values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
                    BigDecimal recebido = r == null ? BigDecimal.ZERO : r.getRecebido();
                    BigDecimal enviado = r == null ? BigDecimal.ZERO : r.getEnviado();
                    return new CustosDtos.LinhaCentro(c.getUuid(), c.getCodigo(), c.getNome(), c.getTipo(), c.getUnidade().getUuid(), c.getUnidade().getNome(),
                            c.getSetor() == null ? null : c.getSetor().getNome(), ordenado(e.getValue()), direto, recebido, enviado,
                            direto.add(recebido).subtract(enviado), r != null && r.isInternacao());
                })
                .sorted(Comparator.comparing(CustosDtos.LinhaCentro::unidadeNome).thenComparing(CustosDtos.LinhaCentro::codigo))
                .toList();
        List<CustosDtos.Rateio> rateios = rateioRepository.findDaApuracao(a.getUuid()).stream().map(r -> new CustosDtos.Rateio(r.getOrdem(),
                r.getOrigem().getUnidade().getNome(), r.getOrigem().getUuid(), r.getOrigem().getCodigo(), r.getOrigem().getNome(), r.getDestino().getUuid(),
                r.getDestino().getCodigo(), r.getDestino().getNome(), r.getCriterio(), r.getValor()))
                .sorted(Comparator.comparing(CustosDtos.Rateio::unidadeNome).thenComparingInt(CustosDtos.Rateio::ordem).thenComparing(CustosDtos.Rateio::destinoCodigo))
                .toList();
        BigDecimal total = porCategoria.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        return new CustosDtos.Apuracao(a.getUuid(), a.getCompetencia(), a.getSituacao(), total, a.getNaoAlocado(), a.getSaidasSemCusto(), a.getNaoRateado(),
                ordenado(porCategoria), linhas, rateios, a.getApuradaEm(), a.getFechadaEm());
    }

    private static CustosDtos.Centro dto(CentroCusto c) {
        return new CustosDtos.Centro(c.getUuid(), c.getCodigo(), c.getNome(), c.getTipo(), c.getUnidade().getUuid(), c.getUnidade().getNome(),
                c.getSetor() == null ? null : c.getSetor().getUuid(), c.getSetor() == null ? null : c.getSetor().getNome(), c.getSetor() == null, c.isAtivo(),
                criterio(c), c.getAreaM2());
    }

    /** O critério e a área do centro; o produtivo não rateia (422). */
    private static void rateio(CentroCusto c, CriterioRateio criterio, BigDecimal areaM2) {
        if (criterio != null && c.getTipo() == TipoCentroCusto.PRODUTIVO) {
            throw new ResourceUnprocessableEntityException("O centro produtivo recebe o rateio e não tem critério.");
        }
        c.setCriterioRateio(c.getTipo() == TipoCentroCusto.PRODUTIVO ? null : criterio);
        c.setAreaM2(areaM2);
    }

    /** O critério aplicado: o informado ou o padrão do tipo (pessoas no administrativo, custo direto no apoio); nulo no produtivo. */
    static CriterioRateio criterio(CentroCusto c) {
        return switch (c.getTipo()) {
            case PRODUTIVO -> null;
            case ADMINISTRATIVO -> c.getCriterioRateio() != null ? c.getCriterioRateio() : CriterioRateio.PESSOAS;
            case APOIO -> c.getCriterioRateio() != null ? c.getCriterioRateio() : CriterioRateio.CUSTO_DIRETO;
        };
    }

    private static String limpo(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }
}
