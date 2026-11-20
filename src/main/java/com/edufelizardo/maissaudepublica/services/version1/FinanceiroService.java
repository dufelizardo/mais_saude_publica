package com.edufelizardo.maissaudepublica.services.version1;

import com.edufelizardo.maissaudepublica.config.ContextoAuditoria;
import com.edufelizardo.maissaudepublica.config.UsuarioAutenticado;
import com.edufelizardo.maissaudepublica.exceptions.ResourceBadRequestException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceConflictException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceNotFoundException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceUnprocessableEntityException;
import com.edufelizardo.maissaudepublica.models.*;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.*;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.ComprasDtos;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.FinanceiroDtos;
import com.edufelizardo.maissaudepublica.models.enuns.*;
import com.edufelizardo.maissaudepublica.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Financeiro, fatia F1 (ADR-0218): a dotação do Fundo Municipal de Saúde com as suplementações e as anulações, e o empenho com o número do
 * SIAFIC, o reforço e a anulação. A plataforma acompanha a execução; o lançamento contábil é do SIAFIC (Decreto 10.540/2020). A ordem de
 * fornecimento de Compras aponta o empenho e consome o saldo dele.
 */
@Service
public class FinanceiroService {

    public static final String CONSULTAR = "FINANCEIRO.CONSULTAR";
    public static final String GERENCIAR = "FINANCEIRO.GERENCIAR";
    private static final ZoneId ZONA = ZoneId.of("America/Sao_Paulo");

    @Autowired private DotacaoOrcamentariaRepository dotacaoRepository;
    @Autowired private AlteracaoDotacaoRepository alteracaoRepository;
    @Autowired private EmpenhoRepository empenhoRepository;
    @Autowired private MovimentoEmpenhoRepository movimentoRepository;
    @Autowired private OrdemFornecimentoRepository ordemRepository;
    @Autowired private ItemOrdemFornecimentoRepository itemOrdemRepository;
    @Autowired private FornecedorRepository fornecedorRepository;
    @Autowired private InstrumentoContratualRepository instrumentoRepository;
    @Autowired private ComprasService comprasService;
    @Autowired private ControleDeAcesso controleDeAcesso;

    // ── Dotação ────────────────────────────────────────────────────────────────────────────────────

    /** As dotações, do exercício pedido ou de todos, com o autorizado, o empenhado e o disponível. */
    @Transactional(readOnly = true)
    public List<FinanceiroDtos.Dotacao> dotacoes(Integer exercicio) {
        controleDeAcesso.exigirAlguma(CONSULTAR, GERENCIAR);
        return dtosDotacao(dotacaoRepository.findTodas().stream().filter(d -> exercicio == null || d.getExercicio() == exercicio).toList());
    }

    /**
     * A dotação do exercício anterior, do atual ou do seguinte (400), única pelo programa de trabalho, pela natureza e pela fonte (409).
     */
    @Transactional
    public FinanceiroDtos.Dotacao criarDotacao(DotacaoOrcamentariaRequestDto dto) {
        controleDeAcesso.exigirAlguma(GERENCIAR);
        int ano = hoje().getYear();
        if (dto.getExercicio() < ano - 1 || dto.getExercicio() > ano + 1) {
            throw new ResourceBadRequestException("O exercício vai de " + (ano - 1) + " a " + (ano + 1) + ".");
        }
        String programa = dto.getProgramaTrabalho().trim();
        String natureza = dto.getNaturezaDespesa().trim();
        if (dotacaoRepository.existsByExercicioAndProgramaTrabalhoAndNaturezaDespesaAndFonte(dto.getExercicio(), programa, natureza, dto.getFonte())) {
            throw new ResourceConflictException("Já existe a dotação " + programa + " · " + natureza + " nesta fonte no exercício " + dto.getExercicio() + ".");
        }
        DotacaoOrcamentaria d = new DotacaoOrcamentaria();
        d.setExercicio(dto.getExercicio());
        d.setProgramaTrabalho(programa);
        d.setDescricao(dto.getDescricao().trim());
        d.setNaturezaDespesa(natureza);
        d.setFonte(dto.getFonte());
        d.setFonteCodigo(limpo(dto.getFonteCodigo()));
        d.setValorInicial(dto.getValorInicial());
        d.setRegistradoEm(Instant.now());
        d.setRegistradoPorCpf(UsuarioAutenticado.cpf());
        dotacaoRepository.save(d);
        ContextoAuditoria.registro(d.getUuid());
        return dtosDotacao(List.of(d)).get(0);
    }

    /** A suplementação ou a anulação, com o ato e a data do exercício, não futura (400); a anulação vai até o disponível (422). */
    @Transactional
    public FinanceiroDtos.Dotacao alterarDotacao(UUID dotacaoId, AlteracaoDotacaoRequestDto dto) {
        controleDeAcesso.exigirAlguma(GERENCIAR);
        DotacaoOrcamentaria d = dotacaoOu404(dotacaoId);
        ContextoAuditoria.registro(d.getUuid());
        exigirData(dto.getData(), d.getExercicio());
        if (dto.getTipo() == TipoAlteracaoDotacao.ANULACAO) {
            BigDecimal disponivel = dtosDotacao(List.of(d)).get(0).disponivel();
            if (dto.getValor().compareTo(disponivel) > 0) {
                throw new ResourceUnprocessableEntityException("A dotação tem " + moeda(disponivel) + " disponível; a anulação passa disso.");
            }
        }
        AlteracaoDotacao a = new AlteracaoDotacao();
        a.setDotacao(d);
        a.setTipo(dto.getTipo());
        a.setValor(dto.getValor());
        a.setAto(dto.getAto().trim());
        a.setData(dto.getData());
        a.setMotivo(limpo(dto.getMotivo()));
        a.setRegistradoEm(Instant.now());
        a.setRegistradoPorCpf(UsuarioAutenticado.cpf());
        alteracaoRepository.save(a);
        return dtosDotacao(List.of(d)).get(0);
    }

    // ── Empenho ────────────────────────────────────────────────────────────────────────────────────

    /** Os empenhos, com os filtros opcionais de exercício, dotação e fornecedor. */
    @Transactional(readOnly = true)
    public List<FinanceiroDtos.Empenho> empenhos(Integer exercicio, UUID dotacaoId, UUID fornecedorId) {
        controleDeAcesso.exigirAlguma(CONSULTAR, GERENCIAR);
        return dtosEmpenho(empenhoRepository.findTodos().stream()
                .filter(e -> exercicio == null || e.getExercicio() == exercicio)
                .filter(e -> dotacaoId == null || e.getDotacao().getUuid().equals(dotacaoId))
                .filter(e -> fornecedorId == null || e.getFornecedor().getUuid().equals(fornecedorId))
                .toList());
    }

    @Transactional(readOnly = true)
    public FinanceiroDtos.Empenho empenho(UUID id) {
        controleDeAcesso.exigirAlguma(CONSULTAR, GERENCIAR);
        return dtosEmpenho(List.of(empenhoOu404(id))).get(0);
    }

    /**
     * Os empenhos com saldo que servem à ata ou ao contrato: do mesmo fornecedor e sem outro instrumento. Para quem emite a ordem de
     * fornecimento (Compras) e para o Financeiro.
     */
    @Transactional(readOnly = true)
    public List<FinanceiroDtos.Empenho> disponiveisPara(UUID instrumentoId) {
        controleDeAcesso.exigirAlguma(ComprasService.GERENCIAR, CONSULTAR, GERENCIAR);
        InstrumentoContratual ins = instrumentoRepository.findById(instrumentoId).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar a ata ou o contrato " + instrumentoId + " em nossos registros."));
        return dtosEmpenho(empenhoRepository.findTodos().stream()
                .filter(e -> e.getFornecedor().getUuid().equals(ins.getFornecedor().getUuid()))
                .filter(e -> e.getInstrumento() == null || e.getInstrumento().getUuid().equals(ins.getUuid()))
                .toList()).stream().filter(e -> e.saldo().signum() > 0).toList();
    }

    /**
     * O empenho lançado no SIAFIC: número único no exercício (409); data do exercício da dotação e não futura (400); fornecedor regular
     * (422); a ata ou o contrato, se houver, do mesmo fornecedor (422); o valor dentro do disponível da dotação (422).
     */
    @Transactional
    public FinanceiroDtos.Empenho empenhar(EmpenhoRequestDto dto) {
        controleDeAcesso.exigirAlguma(GERENCIAR);
        DotacaoOrcamentaria d = dotacaoOu404(dto.getDotacaoId());
        exigirData(dto.getData(), d.getExercicio());
        String numero = dto.getNumero().trim();
        if (empenhoRepository.existsByExercicioAndNumero(d.getExercicio(), numero)) {
            throw new ResourceConflictException("O empenho " + numero + " já está registrado no exercício " + d.getExercicio() + ".");
        }
        Fornecedor f = fornecedorRepository.findById(dto.getFornecedorId()).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar o fornecedor " + dto.getFornecedorId() + " em nossos registros."));
        ComprasDtos.Fornecedor situacao = comprasService.situacao(f);
        if (!situacao.ativo() || situacao.impedido()) {
            throw new ResourceUnprocessableEntityException("O fornecedor " + f.getRazaoSocial() + " está " + (!situacao.ativo() ? "inativo." : "impedido de contratar."));
        }
        InstrumentoContratual ins = null;
        if (dto.getInstrumentoId() != null) {
            ins = instrumentoRepository.findById(dto.getInstrumentoId()).orElseThrow(() -> new ResourceNotFoundException(
                    "Não foi possível encontrar a ata ou o contrato " + dto.getInstrumentoId() + " em nossos registros."));
            if (!ins.getFornecedor().getUuid().equals(f.getUuid())) {
                throw new ResourceUnprocessableEntityException("A ata ou o contrato " + ins.getNumero() + " é de outro fornecedor.");
            }
        }
        exigirDisponivel(d, dto.getValor());
        Empenho e = new Empenho();
        e.setNumero(numero);
        e.setExercicio(d.getExercicio());
        e.setTipo(dto.getTipo());
        e.setDotacao(d);
        e.setFornecedor(f);
        e.setInstrumento(ins);
        e.setValor(dto.getValor());
        e.setData(dto.getData());
        e.setHistorico(limpo(dto.getHistorico()));
        e.setRegistradoEm(Instant.now());
        e.setRegistradoPorCpf(UsuarioAutenticado.cpf());
        empenhoRepository.save(e);
        ContextoAuditoria.registro(e.getUuid());
        ContextoAuditoria.detalhe("Empenho " + numero + " de " + moeda(dto.getValor()));
        return dtosEmpenho(List.of(e)).get(0);
    }

    /** O reforço (dentro do disponível da dotação, 422) ou a anulação (até o saldo do empenho, 422), com a data do exercício (400). */
    @Transactional
    public FinanceiroDtos.Empenho movimentar(UUID empenhoId, MovimentoEmpenhoRequestDto dto) {
        controleDeAcesso.exigirAlguma(GERENCIAR);
        Empenho e = empenhoOu404(empenhoId);
        ContextoAuditoria.registro(e.getUuid());
        exigirData(dto.getData(), e.getExercicio());
        if (dto.getTipo() == TipoMovimentoEmpenho.REFORCO) {
            exigirDisponivel(e.getDotacao(), dto.getValor());
        } else {
            BigDecimal saldo = dtosEmpenho(List.of(e)).get(0).saldo();
            if (dto.getValor().compareTo(saldo) > 0) {
                throw new ResourceUnprocessableEntityException("O empenho tem saldo de " + moeda(saldo) + "; a anulação passa disso.");
            }
        }
        MovimentoEmpenho m = new MovimentoEmpenho();
        m.setEmpenho(e);
        m.setTipo(dto.getTipo());
        m.setValor(dto.getValor());
        m.setData(dto.getData());
        m.setMotivo(dto.getMotivo().trim());
        m.setRegistradoEm(Instant.now());
        m.setRegistradoPorCpf(UsuarioAutenticado.cpf());
        movimentoRepository.save(m);
        return dtosEmpenho(List.of(e)).get(0);
    }

    /**
     * O empenho da ordem de fornecimento (ADR-0218): do fornecedor da ata ou do contrato (422), sem outro instrumento (422) e com saldo para
     * o valor da ordem (422). Chamado pela emissão da ordem, que já conferiu quem compra.
     */
    Empenho comprometer(UUID empenhoId, InstrumentoContratual ins, BigDecimal valor) {
        Empenho e = empenhoOu404(empenhoId);
        if (!e.getFornecedor().getUuid().equals(ins.getFornecedor().getUuid())) {
            throw new ResourceUnprocessableEntityException("O empenho " + e.getNumero() + " é de outro fornecedor.");
        }
        if (e.getInstrumento() != null && !e.getInstrumento().getUuid().equals(ins.getUuid())) {
            throw new ResourceUnprocessableEntityException("O empenho " + e.getNumero() + " é de outra ata ou contrato.");
        }
        BigDecimal saldo = dtosEmpenho(List.of(e)).get(0).saldo();
        if (valor.compareTo(saldo) > 0) {
            throw new ResourceUnprocessableEntityException("O empenho " + e.getNumero() + " tem saldo de " + moeda(saldo) + "; a ordem vale " + moeda(valor) + ".");
        }
        return e;
    }

    // ── Auxiliares ─────────────────────────────────────────────────────────────────────────────────

    private void exigirDisponivel(DotacaoOrcamentaria d, BigDecimal valor) {
        BigDecimal disponivel = dtosDotacao(List.of(d)).get(0).disponivel();
        if (valor.compareTo(disponivel) > 0) {
            throw new ResourceUnprocessableEntityException("A dotação " + d.getProgramaTrabalho() + " · " + d.getNaturezaDespesa() + " tem " + moeda(disponivel) + " disponível.");
        }
    }

    private void exigirData(LocalDate data, int exercicio) {
        if (data.isAfter(hoje())) {
            throw new ResourceBadRequestException("A data não pode ser futura.");
        }
        if (data.getYear() != exercicio) {
            throw new ResourceBadRequestException("A data é do exercício " + exercicio + ".");
        }
    }

    private List<FinanceiroDtos.Dotacao> dtosDotacao(List<DotacaoOrcamentaria> dotacoes) {
        if (dotacoes.isEmpty()) {
            return List.of();
        }
        Set<UUID> ids = dotacoes.stream().map(DotacaoOrcamentaria::getUuid).collect(Collectors.toSet());
        Map<UUID, List<AlteracaoDotacao>> alteracoes = alteracaoRepository.findDasDotacoes(ids).stream().collect(Collectors.groupingBy(a -> a.getDotacao().getUuid()));
        List<Empenho> empenhos = empenhoRepository.findTodos().stream().filter(e -> ids.contains(e.getDotacao().getUuid())).toList();
        Map<UUID, BigDecimal> atual = valoresAtuais(empenhos);
        Map<UUID, BigDecimal> empenhado = new HashMap<>();
        empenhos.forEach(e -> empenhado.merge(e.getDotacao().getUuid(), atual.get(e.getUuid()), BigDecimal::add));
        return dotacoes.stream().map(d -> {
            List<AlteracaoDotacao> l = alteracoes.getOrDefault(d.getUuid(), List.of());
            BigDecimal sup = soma(l.stream().filter(a -> a.getTipo() == TipoAlteracaoDotacao.SUPLEMENTACAO).map(AlteracaoDotacao::getValor).toList());
            BigDecimal anul = soma(l.stream().filter(a -> a.getTipo() == TipoAlteracaoDotacao.ANULACAO).map(AlteracaoDotacao::getValor).toList());
            BigDecimal autorizado = d.getValorInicial().add(sup).subtract(anul);
            BigDecimal emp = empenhado.getOrDefault(d.getUuid(), BigDecimal.ZERO);
            return new FinanceiroDtos.Dotacao(d.getUuid(), d.getExercicio(), d.getProgramaTrabalho(), d.getDescricao(), d.getNaturezaDespesa(), d.getFonte(),
                    d.getFonteCodigo(), d.getValorInicial(), sup, anul, autorizado, emp, autorizado.subtract(emp),
                    l.stream().map(a -> new FinanceiroDtos.Alteracao(a.getUuid(), a.getTipo(), a.getValor(), a.getAto(), a.getData(), a.getMotivo())).toList());
        }).toList();
    }

    /** O valor atual de cada empenho: o inicial mais os reforços menos as anulações. */
    Map<UUID, BigDecimal> valoresAtuais(List<Empenho> empenhos) {
        Map<UUID, BigDecimal> atual = new HashMap<>();
        empenhos.forEach(e -> atual.put(e.getUuid(), e.getValor()));
        if (!empenhos.isEmpty()) {
            for (MovimentoEmpenho m : movimentoRepository.findDosEmpenhos(atual.keySet())) {
                atual.merge(m.getEmpenho().getUuid(), m.getTipo() == TipoMovimentoEmpenho.REFORCO ? m.getValor() : m.getValor().negate(), BigDecimal::add);
            }
        }
        return atual;
    }

    private List<FinanceiroDtos.Empenho> dtosEmpenho(List<Empenho> empenhos) {
        if (empenhos.isEmpty()) {
            return List.of();
        }
        Set<UUID> ids = empenhos.stream().map(Empenho::getUuid).collect(Collectors.toSet());
        Map<UUID, List<MovimentoEmpenho>> movimentos = movimentoRepository.findDosEmpenhos(ids).stream().collect(Collectors.groupingBy(m -> m.getEmpenho().getUuid()));
        List<OrdemFornecimento> ordens = ordemRepository.findDosEmpenhos(ids);
        Map<UUID, List<ItemOrdemFornecimento>> itens = ordens.isEmpty() ? Map.of()
                : itemOrdemRepository.findDasOrdens(ordens.stream().map(OrdemFornecimento::getUuid).toList()).stream()
                .collect(Collectors.groupingBy(i -> i.getOrdem().getUuid()));
        Map<UUID, List<FinanceiroDtos.OrdemDoEmpenho>> porEmpenho = new HashMap<>();
        for (OrdemFornecimento o : ordens) {
            boolean cancelada = o.getSituacao() == SituacaoOrdemFornecimento.CANCELADA;
            BigDecimal valor = soma(itens.getOrDefault(o.getUuid(), List.of()).stream()
                    .map(i -> i.getItemInstrumento().getPrecoUnitario().multiply(BigDecimal.valueOf(cancelada ? i.getQuantidadeAceita() : i.getQuantidade()))).toList());
            porEmpenho.computeIfAbsent(o.getEmpenhoRef().getUuid(), k -> new ArrayList<>()).add(new FinanceiroDtos.OrdemDoEmpenho(o.getUuid(), o.getNumero(), o.getSituacao(), valor));
        }
        return empenhos.stream().map(e -> {
            List<MovimentoEmpenho> l = movimentos.getOrDefault(e.getUuid(), List.of());
            BigDecimal reforco = soma(l.stream().filter(m -> m.getTipo() == TipoMovimentoEmpenho.REFORCO).map(MovimentoEmpenho::getValor).toList());
            BigDecimal anulado = soma(l.stream().filter(m -> m.getTipo() == TipoMovimentoEmpenho.ANULACAO).map(MovimentoEmpenho::getValor).toList());
            BigDecimal atual = e.getValor().add(reforco).subtract(anulado);
            List<FinanceiroDtos.OrdemDoEmpenho> os = porEmpenho.getOrDefault(e.getUuid(), List.of());
            BigDecimal comprometido = soma(os.stream().map(FinanceiroDtos.OrdemDoEmpenho::valor).toList());
            DotacaoOrcamentaria d = e.getDotacao();
            return new FinanceiroDtos.Empenho(e.getUuid(), e.getNumero(), e.getExercicio(), e.getTipo(), d.getUuid(),
                    d.getProgramaTrabalho() + " · " + d.getNaturezaDespesa(), e.getFornecedor().getUuid(), e.getFornecedor().getRazaoSocial(),
                    e.getInstrumento() != null ? e.getInstrumento().getUuid() : null, e.getInstrumento() != null ? e.getInstrumento().getNumero() : null,
                    e.getValor(), reforco, anulado, atual, comprometido, atual.subtract(comprometido), e.getData(), e.getHistorico(),
                    l.stream().map(m -> new FinanceiroDtos.Movimento(m.getUuid(), m.getTipo(), m.getValor(), m.getData(), m.getMotivo())).toList(), os);
        }).toList();
    }

    private static BigDecimal soma(List<BigDecimal> valores) {
        return valores.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private static String moeda(BigDecimal v) {
        return java.text.NumberFormat.getCurrencyInstance(Locale.forLanguageTag("pt-BR")).format(v);
    }

    private DotacaoOrcamentaria dotacaoOu404(UUID id) {
        return dotacaoRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar a dotação " + id + " em nossos registros."));
    }

    private Empenho empenhoOu404(UUID id) {
        return empenhoRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar o empenho " + id + " em nossos registros."));
    }

    private static String limpo(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }

    private static LocalDate hoje() {
        return LocalDate.now(ZONA);
    }
}
