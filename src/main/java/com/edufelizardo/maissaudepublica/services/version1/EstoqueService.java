package com.edufelizardo.maissaudepublica.services.version1;

import com.edufelizardo.maissaudepublica.config.ContextoAuditoria;
import com.edufelizardo.maissaudepublica.config.UsuarioAutenticado;
import com.edufelizardo.maissaudepublica.exceptions.ResourceBadRequestException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceConflictException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceNotFoundException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceUnprocessableEntityException;
import com.edufelizardo.maissaudepublica.models.EstoqueMinimo;
import com.edufelizardo.maissaudepublica.models.LoteMaterial;
import com.edufelizardo.maissaudepublica.models.MaterialEstoque;
import com.edufelizardo.maissaudepublica.models.MovimentacaoEstoque;
import com.edufelizardo.maissaudepublica.models.Profissional;
import com.edufelizardo.maissaudepublica.models.Setor;
import com.edufelizardo.maissaudepublica.models.UnidadeDeSaude;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.AjusteEstoqueRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.EntradaEstoqueRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.EstoqueMinimoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.MaterialEstoqueRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.PerdaEstoqueRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.SaidaEstoqueRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.EstoqueDtos;
import com.edufelizardo.maissaudepublica.models.enuns.CategoriaMaterial;
import com.edufelizardo.maissaudepublica.models.enuns.OrigemEntradaEstoque;
import com.edufelizardo.maissaudepublica.models.enuns.TipoMovimentacaoEstoque;
import com.edufelizardo.maissaudepublica.repositories.EstoqueMinimoRepository;
import com.edufelizardo.maissaudepublica.repositories.LoteMaterialRepository;
import com.edufelizardo.maissaudepublica.repositories.MaterialEstoqueRepository;
import com.edufelizardo.maissaudepublica.repositories.MovimentacaoEstoqueRepository;
import com.edufelizardo.maissaudepublica.repositories.ProfissionalRepository;
import com.edufelizardo.maissaudepublica.repositories.SetorRepository;
import com.edufelizardo.maissaudepublica.repositories.UnidadeDeSaudeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Estoque e Almoxarifado, fatia ES1 (ADR-0177): o catálogo de materiais, o estoque de cada unidade em lotes, o livro de movimentação
 * (entrada, saída por consumo, perda, ajuste) e os alertas de mínimo e validade. Medicamento fica na Farmácia e vacina na Sala de
 * Vacina, com regras próprias; o padrão é o mesmo (lote, livro imutável, saldo que nunca fica negativo).
 */
@Service
public class EstoqueService {

    public static final String CONSULTAR = "ESTOQUE.CONSULTAR";
    public static final String MOVIMENTAR = "ESTOQUE.MOVIMENTAR";
    public static final String GERENCIAR = "ESTOQUE.GERENCIAR";
    /** Lotes que vencem até este número de dias entram nos alertas. */
    static final int JANELA_DE_VENCIMENTO = 90;

    @Autowired private MaterialEstoqueRepository materialRepository;
    @Autowired private LoteMaterialRepository loteRepository;
    @Autowired private MovimentacaoEstoqueRepository movimentacaoRepository;
    @Autowired private EstoqueMinimoRepository minimoRepository;
    @Autowired private UnidadeDeSaudeRepository unidadeRepository;
    @Autowired private SetorRepository setorRepository;
    @Autowired private ProfissionalRepository profissionalRepository;
    @Autowired private ControleDeAcesso controleDeAcesso;
    @Autowired private com.edufelizardo.maissaudepublica.repositories.ConsumoSemSaldoRepository consumoSemSaldoRepository;

    // ── Catálogo ──────────────────────────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<EstoqueDtos.Material> materiais(Boolean ativos) {
        controleDeAcesso.exigirAlguma(CONSULTAR, MOVIMENTAR, GERENCIAR);
        return materialRepository.findAllByOrderByNomeAsc().stream()
                .filter(m -> ativos == null || !ativos || m.isAtivo())
                .map(this::material).toList();
    }

    @Transactional
    public EstoqueDtos.Material criarMaterial(MaterialEstoqueRequestDto dto) {
        controleDeAcesso.exigirAlguma(GERENCIAR);
        String codigo = dto.getCodigo().trim().toUpperCase(Locale.ROOT);
        String nome = dto.getNome().trim();
        if (materialRepository.existsByCodigoIgnoreCase(codigo)) {
            throw new ResourceConflictException("Já existe um material com o código " + codigo + ".");
        }
        if (materialRepository.existsByNomeIgnoreCase(nome)) {
            throw new ResourceConflictException("Já existe um material chamado " + nome + ".");
        }
        MaterialEstoque m = new MaterialEstoque(null, codigo, nome, texto(dto.getCatmat()), dto.getCategoria(), dto.getUnidadeMedida(),
                dto.getControlaLote() == null || dto.getControlaLote(), texto(dto.getDescricao()), dto.getAtivo() == null || dto.getAtivo());
        materialRepository.save(m);
        ContextoAuditoria.registro(m.getUuid());
        return material(m);
    }

    @Transactional
    public EstoqueDtos.Material atualizarMaterial(UUID uuid, MaterialEstoqueRequestDto dto) {
        controleDeAcesso.exigirAlguma(GERENCIAR);
        MaterialEstoque m = materialPorId(uuid);
        String codigo = dto.getCodigo().trim().toUpperCase(Locale.ROOT);
        String nome = dto.getNome().trim();
        if (materialRepository.existsByCodigoIgnoreCaseAndUuidNot(codigo, uuid)) {
            throw new ResourceConflictException("Já existe um material com o código " + codigo + ".");
        }
        if (materialRepository.existsByNomeIgnoreCaseAndUuidNot(nome, uuid)) {
            throw new ResourceConflictException("Já existe um material chamado " + nome + ".");
        }
        boolean controla = dto.getControlaLote() == null ? m.isControlaLote() : dto.getControlaLote();
        if (controla != m.isControlaLote() && loteRepository.existsByMaterial_Uuid(uuid)) {
            throw new ResourceUnprocessableEntityException("O controle de lote não muda depois da primeira entrada do material.");
        }
        m.setCodigo(codigo);
        m.setNome(nome);
        m.setCatmat(texto(dto.getCatmat()));
        m.setCategoria(dto.getCategoria());
        m.setUnidadeMedida(dto.getUnidadeMedida());
        m.setControlaLote(controla);
        m.setDescricao(texto(dto.getDescricao()));
        if (dto.getAtivo() != null) {
            m.setAtivo(dto.getAtivo());
        }
        materialRepository.save(m);
        ContextoAuditoria.registro(m.getUuid());
        return material(m);
    }

    @Transactional
    public EstoqueDtos.ItemDeSaldo definirMinimo(EstoqueMinimoRequestDto dto) {
        UnidadeDeSaude u = unidade(dto.getUnidadeId());
        controleDeAcesso.exigir(GERENCIAR, u);
        MaterialEstoque m = materialPorId(dto.getMaterialId());
        EstoqueMinimo e = minimoRepository.findByMaterial_UuidAndUnidade_Uuid(m.getUuid(), u.getUuid())
                .orElseGet(() -> new EstoqueMinimo(null, m, u, 0, null, null));
        e.setQuantidadeMinima(dto.getQuantidadeMinima());
        e.setAtualizadoEm(Instant.now());
        e.setAtualizadoPorCpf(UsuarioAutenticado.cpf());
        minimoRepository.save(e);
        ContextoAuditoria.detalhe("Mínimo de " + m.getCodigo() + ": " + dto.getQuantidadeMinima() + ".");
        return item(m, loteRepository.findDaUnidade(u.getUuid()).stream().filter(l -> l.getMaterial().getUuid().equals(m.getUuid())).toList(),
                e.getQuantidadeMinima(), LocalDate.now());
    }

    // ── Movimentação ──────────────────────────────────────────────────────────────────────────────

    /** Entra no lote do mesmo número (sem controle de lote, no saldo único) ou abre um lote novo. */
    @Transactional
    public EstoqueDtos.Movimentacao entrada(EntradaEstoqueRequestDto dto) {
        UnidadeDeSaude u = unidade(dto.getUnidadeId());
        controleDeAcesso.exigir(MOVIMENTAR, u);
        MaterialEstoque m = materialPorId(dto.getMaterialId());
        if (!m.isAtivo()) {
            throw new ResourceUnprocessableEntityException("O material " + m.getNome() + " está inativo no catálogo.");
        }
        if (dto.getOrigem() == OrigemEntradaEstoque.NOTA_FISCAL && texto(dto.getDocumento()) == null) {
            throw new ResourceBadRequestException("Informe o número da nota fiscal.");
        }
        String numero = texto(dto.getNumeroLote());
        if (m.isControlaLote() && (numero == null || dto.getValidade() == null)) {
            throw new ResourceBadRequestException(m.getNome() + " controla lote: informe o lote e a validade.");
        }
        if (!m.isControlaLote() && (numero != null || dto.getValidade() != null)) {
            throw new ResourceBadRequestException(m.getNome() + " não controla lote: deixe o lote e a validade em branco.");
        }
        if (dto.getValidade() != null && dto.getValidade().isBefore(LocalDate.now())) {
            throw new ResourceUnprocessableEntityException("O lote já está vencido; não entra no estoque.");
        }
        List<LoteMaterial> existentes = loteRepository.findDoMaterialNaUnidadeParaAtualizar(m.getUuid(), u.getUuid());
        LoteMaterial lote = existentes.stream().filter(l -> Objects.equals(normalizar(l.getNumeroLote()), normalizar(numero))).findFirst().orElse(null);
        if (lote != null && !Objects.equals(lote.getValidade(), dto.getValidade())) {
            throw new ResourceUnprocessableEntityException("O lote " + numero + " já está no estoque com a validade " + lote.getValidade() + ".");
        }
        if (lote == null) {
            lote = loteRepository.save(new LoteMaterial(null, m, u, numero, dto.getValidade(), 0, null, Instant.now()));
        }
        if (dto.getCustoUnitario() != null) {
            lote.setCustoUnitario(dto.getCustoUnitario());
        }
        TipoMovimentacaoEstoque tipo = dto.getOrigem() == OrigemEntradaEstoque.SALDO_INICIAL ? TipoMovimentacaoEstoque.SALDO_INICIAL
                : TipoMovimentacaoEstoque.ENTRADA;
        MovimentacaoEstoque mov = lancar(lote, tipo, dto.getQuantidade(), profissional(dto.getProfissionalMatricula()));
        mov.setOrigem(dto.getOrigem());
        mov.setDocumento(texto(dto.getDocumento()));
        mov.setFornecedor(texto(dto.getFornecedor()));
        return movimentacao(movimentacaoRepository.save(mov));
    }

    /** Sem lote escolhido, sai dos lotes que vencem primeiro (FEFO), podendo usar mais de um. Lote vencido não sai por consumo. */
    @Transactional
    public List<EstoqueDtos.Movimentacao> saida(SaidaEstoqueRequestDto dto) {
        UnidadeDeSaude u = unidade(dto.getUnidadeId());
        controleDeAcesso.exigir(MOVIMENTAR, u);
        MaterialEstoque m = materialPorId(dto.getMaterialId());
        Setor setor = null;
        if (dto.getSetorId() != null) {
            setor = setorRepository.findById(dto.getSetorId()).orElseThrow(() -> new ResourceNotFoundException(
                    "Não foi possível encontrar o setor " + dto.getSetorId() + " em nossos registros."));
            if (setor.getUnidade() == null || !setor.getUnidade().getUuid().equals(u.getUuid())) {
                throw new ResourceUnprocessableEntityException("O setor " + setor.getNome() + " não é desta unidade.");
            }
        }
        LocalDate hoje = LocalDate.now();
        List<LoteMaterial> lotes = loteRepository.findDoMaterialNaUnidadeParaAtualizar(m.getUuid(), u.getUuid());
        List<LoteMaterial> usar;
        if (dto.getLoteId() != null) {
            LoteMaterial escolhido = lotes.stream().filter(l -> l.getUuid().equals(dto.getLoteId())).findFirst()
                    .orElseThrow(() -> new ResourceUnprocessableEntityException("O lote não é deste material nesta unidade."));
            if (escolhido.vencido(hoje)) {
                throw new ResourceUnprocessableEntityException("O lote " + escolhido.getNumeroLote() + " está vencido; registre como perda.");
            }
            if (escolhido.getQuantidade() < dto.getQuantidade()) {
                throw new ResourceUnprocessableEntityException("O lote tem " + escolhido.getQuantidade() + "; não dá para tirar " + dto.getQuantidade() + ".");
            }
            usar = List.of(escolhido);
        } else {
            usar = lotes.stream().filter(l -> l.getQuantidade() > 0 && !l.vencido(hoje)).toList();
            int disponivel = usar.stream().mapToInt(LoteMaterial::getQuantidade).sum();
            if (disponivel < dto.getQuantidade()) {
                throw new ResourceUnprocessableEntityException("Há " + disponivel + " de " + m.getNome() + " dentro da validade; não dá para tirar "
                        + dto.getQuantidade() + ".");
            }
        }
        Profissional quem = profissional(dto.getProfissionalMatricula());
        int falta = dto.getQuantidade();
        List<EstoqueDtos.Movimentacao> feitas = new ArrayList<>();
        for (LoteMaterial l : usar) {
            if (falta == 0) {
                break;
            }
            int tirar = Math.min(falta, l.getQuantidade());
            MovimentacaoEstoque mov = lancar(l, TipoMovimentacaoEstoque.SAIDA_CONSUMO, -tirar, quem);
            mov.setSetor(setor);
            mov.setJustificativa(texto(dto.getJustificativa()));
            feitas.add(movimentacao(movimentacaoRepository.save(mov)));
            falta -= tirar;
        }
        return feitas;
    }

    /**
     * Consumo lançado pelo sistema (ADR-0183): os insumos da coleta e da realização de exame. Sai pelo FEFO até onde houver saldo
     * dentro da validade, sem exigir ESTOQUE.MOVIMENTAR de quem coleta; o que faltar vira alerta de consumo sem saldo, porque a
     * coleta já aconteceu. Devolve a quantidade que faltou.
     */
    @Transactional
    public int consumirPeloSistema(UnidadeDeSaude u, MaterialEstoque m, int quantidade, Profissional quem, String origem) {
        LocalDate hoje = LocalDate.now();
        List<LoteMaterial> usar = loteRepository.findDoMaterialNaUnidadeParaAtualizar(m.getUuid(), u.getUuid()).stream()
                .filter(l -> l.getQuantidade() > 0 && !l.vencido(hoje)).toList();
        int falta = quantidade;
        for (LoteMaterial l : usar) {
            if (falta == 0) {
                break;
            }
            int tirar = Math.min(falta, l.getQuantidade());
            MovimentacaoEstoque mov = lancar(l, TipoMovimentacaoEstoque.SAIDA_CONSUMO, -tirar, quem);
            mov.setJustificativa(origem.length() > 500 ? origem.substring(0, 500) : origem);
            movimentacaoRepository.save(mov);
            falta -= tirar;
        }
        if (falta > 0) {
            com.edufelizardo.maissaudepublica.models.ConsumoSemSaldo c = new com.edufelizardo.maissaudepublica.models.ConsumoSemSaldo();
            c.setUnidade(u);
            c.setMaterial(m);
            c.setQuantidadeFaltante(falta);
            c.setOrigem(origem.length() > 300 ? origem.substring(0, 300) : origem);
            c.setProfissional(quem);
            c.setOcorridoEm(Instant.now());
            consumoSemSaldoRepository.save(c);
        }
        return falta;
    }

    /** O almoxarife conferiu o consumo sem saldo (acertou a entrada ou o inventário): sai do alerta. Já conferido: 422. */
    @Transactional
    public void conferirConsumoSemSaldo(UUID uuid) {
        com.edufelizardo.maissaudepublica.models.ConsumoSemSaldo c = consumoSemSaldoRepository.findById(uuid).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar o consumo sem saldo " + uuid + " em nossos registros."));
        controleDeAcesso.exigir(MOVIMENTAR, c.getUnidade());
        if (c.getConferidoEm() != null) {
            throw new ResourceUnprocessableEntityException("Este consumo sem saldo já foi conferido.");
        }
        c.setConferidoEm(Instant.now());
        c.setConferidoPorCpf(UsuarioAutenticado.cpf());
        consumoSemSaldoRepository.save(c);
    }

    @Transactional
    public EstoqueDtos.Movimentacao perda(PerdaEstoqueRequestDto dto) {
        LoteMaterial l = loteParaMovimentar(dto.getLoteId());
        if (l.getQuantidade() < dto.getQuantidade()) {
            throw new ResourceUnprocessableEntityException("O lote tem " + l.getQuantidade() + "; a perda não pode ser de " + dto.getQuantidade() + ".");
        }
        MovimentacaoEstoque mov = lancar(l, TipoMovimentacaoEstoque.PERDA, -dto.getQuantidade(), profissional(dto.getProfissionalMatricula()));
        mov.setMotivoPerda(dto.getMotivo());
        mov.setJustificativa(texto(dto.getJustificativa()));
        return movimentacao(movimentacaoRepository.save(mov));
    }

    @Transactional
    public EstoqueDtos.Movimentacao ajuste(AjusteEstoqueRequestDto dto) {
        LoteMaterial l = loteParaMovimentar(dto.getLoteId());
        int diferenca = dto.getQuantidadeContada() - l.getQuantidade();
        if (diferenca == 0) {
            throw new ResourceUnprocessableEntityException("A contagem confere com o saldo; não há o que ajustar.");
        }
        MovimentacaoEstoque mov = lancar(l, TipoMovimentacaoEstoque.AJUSTE_INVENTARIO, diferenca, profissional(dto.getProfissionalMatricula()));
        mov.setJustificativa(dto.getJustificativa().trim());
        return movimentacao(movimentacaoRepository.save(mov));
    }

    // ── Leitura ───────────────────────────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public EstoqueDtos.Saldo saldo(UUID unidadeId, CategoriaMaterial categoria) {
        UnidadeDeSaude u = unidade(unidadeId);
        controleDeAcesso.exigirVisivel(u, CONSULTAR, MOVIMENTAR, GERENCIAR);
        LocalDate hoje = LocalDate.now();
        Map<UUID, List<LoteMaterial>> porMaterial = loteRepository.findDaUnidade(u.getUuid()).stream()
                .collect(Collectors.groupingBy(l -> l.getMaterial().getUuid()));
        Map<UUID, Integer> minimos = minimoRepository.findTodos().stream().filter(e -> e.getUnidade().getUuid().equals(u.getUuid()))
                .collect(Collectors.toMap(e -> e.getMaterial().getUuid(), EstoqueMinimo::getQuantidadeMinima));
        List<EstoqueDtos.ItemDeSaldo> itens = materialRepository.findAllByOrderByNomeAsc().stream()
                .filter(m -> porMaterial.containsKey(m.getUuid()) || minimos.containsKey(m.getUuid()))
                .filter(m -> categoria == null || m.getCategoria() == categoria)
                .map(m -> item(m, porMaterial.getOrDefault(m.getUuid(), List.of()), minimos.get(m.getUuid()), hoje)).toList();
        List<EstoqueDtos.Vencimento> vencimentos = itens.stream()
                .flatMap(i -> i.lotes().stream().filter(l -> l.diasParaVencer() != null && l.diasParaVencer() <= JANELA_DE_VENCIMENTO)
                        .map(l -> new EstoqueDtos.Vencimento(l.uuid(), i.materialId(), i.nome(), l.numeroLote(), l.validade(), l.quantidade(), l.diasParaVencer())))
                .sorted(Comparator.comparing(EstoqueDtos.Vencimento::validade)).toList();
        BigDecimal total = itens.stream().map(EstoqueDtos.ItemDeSaldo::valor).filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add);
        return new EstoqueDtos.Saldo(u.getUuid(), u.getNome(), itens, (int) itens.stream().filter(EstoqueDtos.ItemDeSaldo::abaixoDoMinimo).count(),
                (int) vencimentos.stream().filter(v -> v.diasParaVencer() >= 0).count(), (int) vencimentos.stream().filter(v -> v.diasParaVencer() < 0).count(),
                total, vencimentos, consumoSemSaldoRepository.findByUnidade_UuidAndConferidoEmIsNullOrderByOcorridoEmDesc(u.getUuid()).stream()
                        .map(c -> new EstoqueDtos.ConsumoSemSaldo(c.getUuid(), c.getMaterial().getUuid(), c.getMaterial().getCodigo(), c.getMaterial().getNome(),
                                c.getQuantidadeFaltante(), c.getOrigem(), c.getProfissional() != null ? c.getProfissional().getNome() : null, c.getOcorridoEm()))
                        .toList());
    }

    /** O livro, mais recente primeiro: dos últimos dias (padrão 30, até 365). */
    @Transactional(readOnly = true)
    public List<EstoqueDtos.Movimentacao> movimentacoes(UUID unidadeId, UUID materialId, TipoMovimentacaoEstoque tipo, Integer dias) {
        int d = dias == null ? 30 : Math.max(1, Math.min(dias, 365));
        Stream<MovimentacaoEstoque> base = movimentacaoRepository.findDesde(Instant.now().minus(Duration.ofDays(d))).stream();
        if (unidadeId != null) {
            controleDeAcesso.exigirVisivel(unidade(unidadeId), CONSULTAR, MOVIMENTAR, GERENCIAR);
            base = base.filter(m -> m.getLote().getUnidade().getUuid().equals(unidadeId));
        } else {
            base = controleDeAcesso.filtrar(base, m -> Stream.of(m.getLote().getUnidade()), CONSULTAR, MOVIMENTAR, GERENCIAR);
        }
        return base.filter(m -> materialId == null || m.getLote().getMaterial().getUuid().equals(materialId))
                .filter(m -> tipo == null || m.getTipo() == tipo)
                .map(this::movimentacao).toList();
    }

    // ── Apoio ─────────────────────────────────────────────────────────────────────────────────────

    private MovimentacaoEstoque lancar(LoteMaterial l, TipoMovimentacaoEstoque tipo, int variacao, Profissional quem) {
        int saldo = l.getQuantidade() + variacao;
        if (saldo < 0) {
            throw new ResourceUnprocessableEntityException("O saldo do lote ficaria negativo.");
        }
        l.setQuantidade(saldo);
        loteRepository.save(l);
        ContextoAuditoria.unidade(l.getUnidade());
        ContextoAuditoria.detalhe(tipo + " de " + Math.abs(variacao) + " " + l.getMaterial().getCodigo() + ".");
        MovimentacaoEstoque mov = new MovimentacaoEstoque();
        mov.setLote(l);
        mov.setTipo(tipo);
        mov.setQuantidade(variacao);
        mov.setSaldoApos(saldo);
        mov.setProfissional(quem);
        mov.setRegistradoEm(Instant.now());
        mov.setRegistradoPorCpf(UsuarioAutenticado.cpf());
        return mov;
    }

    private LoteMaterial loteParaMovimentar(UUID loteId) {
        LoteMaterial l = loteRepository.findByIdParaAtualizar(loteId).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar o lote " + loteId + " em nossos registros."));
        controleDeAcesso.exigir(MOVIMENTAR, l.getUnidade());
        return l;
    }

    private EstoqueDtos.ItemDeSaldo item(MaterialEstoque m, List<LoteMaterial> lotes, Integer minimo, LocalDate hoje) {
        List<EstoqueDtos.Lote> comSaldo = lotes.stream().filter(l -> l.getQuantidade() > 0)
                .sorted(Comparator.comparing(LoteMaterial::getValidade, Comparator.nullsLast(Comparator.naturalOrder())))
                .map(l -> new EstoqueDtos.Lote(l.getUuid(), l.getNumeroLote(), l.getValidade(), l.getQuantidade(), l.getCustoUnitario(), l.vencido(hoje),
                        l.getValidade() == null ? null : ChronoUnit.DAYS.between(hoje, l.getValidade()))).toList();
        int total = comSaldo.stream().mapToInt(EstoqueDtos.Lote::quantidade).sum();
        BigDecimal valor = comSaldo.stream().filter(l -> l.custoUnitario() != null)
                .map(l -> l.custoUnitario().multiply(BigDecimal.valueOf(l.quantidade()))).reduce(null, (a, b) -> a == null ? b : a.add(b));
        LocalDate proxima = comSaldo.stream().map(EstoqueDtos.Lote::validade).filter(Objects::nonNull).min(Comparator.naturalOrder()).orElse(null);
        return new EstoqueDtos.ItemDeSaldo(m.getUuid(), m.getCodigo(), m.getNome(), m.getCategoria(), m.getUnidadeMedida(), m.isControlaLote(), total,
                minimo, minimo != null && total < minimo, proxima, valor, comSaldo);
    }

    private EstoqueDtos.Material material(MaterialEstoque m) {
        return new EstoqueDtos.Material(m.getUuid(), m.getCodigo(), m.getNome(), m.getCatmat(), m.getCategoria(), m.getUnidadeMedida(), m.isControlaLote(),
                m.getDescricao(), m.isAtivo(), loteRepository.existsByMaterial_Uuid(m.getUuid()));
    }

    private EstoqueDtos.Movimentacao movimentacao(MovimentacaoEstoque m) {
        LoteMaterial l = m.getLote();
        return new EstoqueDtos.Movimentacao(m.getUuid(), m.getTipo(), l.getMaterial().getUuid(), l.getMaterial().getCodigo(), l.getMaterial().getNome(),
                l.getUnidade().getUuid(), l.getUnidade().getNome(), l.getUuid(), l.getNumeroLote(), l.getValidade(), m.getQuantidade(), m.getSaldoApos(),
                m.getOrigem(), m.getDocumento(), m.getFornecedor(), m.getSetor() != null ? m.getSetor().getNome() : null, m.getMotivoPerda(),
                m.getJustificativa(), m.getProfissional() != null ? m.getProfissional().getNome() : null, m.getRegistradoEm());
    }

    /** Pela matrícula informada; sem ela, o profissional do login, se houver. */
    private Profissional profissional(String matricula) {
        if (texto(matricula) != null) {
            return profissionalRepository.findByMatricula(matricula.trim()).orElseThrow(() -> new ResourceNotFoundException(
                    "Não foi possível encontrar um profissional com a matrícula " + matricula.trim() + " em nossos registros."));
        }
        String cpf = UsuarioAutenticado.cpf();
        return cpf == null ? null : profissionalRepository.findByCpfAndAtivoTrue(cpf).orElse(null);
    }

    private MaterialEstoque materialPorId(UUID id) {
        return materialRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar o material " + id + " em nossos registros."));
    }

    private UnidadeDeSaude unidade(UUID id) {
        if (id == null) {
            throw new ResourceBadRequestException("Informe a unidade.");
        }
        return unidadeRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar uma unidade de saúde com o id " + id + " em nossos registros."));
    }

    private static String normalizar(String lote) {
        return lote == null ? null : lote.trim().toUpperCase(Locale.ROOT);
    }

    private static String texto(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }
}
