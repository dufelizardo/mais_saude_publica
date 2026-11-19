package com.edufelizardo.maissaudepublica.services.version1;

import com.edufelizardo.maissaudepublica.config.ContextoAuditoria;
import com.edufelizardo.maissaudepublica.config.UsuarioAutenticado;
import com.edufelizardo.maissaudepublica.exceptions.ResourceBadRequestException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceConflictException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceForbiddenException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceNotFoundException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceUnprocessableEntityException;
import com.edufelizardo.maissaudepublica.models.AditivoContratual;
import com.edufelizardo.maissaudepublica.models.Contratacao;
import com.edufelizardo.maissaudepublica.models.Fornecedor;
import com.edufelizardo.maissaudepublica.models.InstrumentoContratual;
import com.edufelizardo.maissaudepublica.models.ItemContratacao;
import com.edufelizardo.maissaudepublica.models.ItemInstrumento;
import com.edufelizardo.maissaudepublica.models.ItemSolicitacaoCompra;
import com.edufelizardo.maissaudepublica.models.Profissional;
import com.edufelizardo.maissaudepublica.models.SolicitacaoCompra;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.AditivoContratualRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.ContratacaoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.HomologacaoContratacaoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.ResultadoItemContratacaoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.ComprasDtos;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.ContratosDtos;
import com.edufelizardo.maissaudepublica.models.enuns.ModalidadeContratacao;
import com.edufelizardo.maissaudepublica.models.enuns.ResultadoItemContratacao;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoContratacao;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoSolicitacaoCompra;
import com.edufelizardo.maissaudepublica.models.enuns.TipoAditivoContratual;
import com.edufelizardo.maissaudepublica.models.enuns.TipoInstrumentoContratual;
import com.edufelizardo.maissaudepublica.models.enuns.TipoItemCompra;
import com.edufelizardo.maissaudepublica.repositories.AditivoContratualRepository;
import com.edufelizardo.maissaudepublica.repositories.ContratacaoRepository;
import com.edufelizardo.maissaudepublica.repositories.FornecedorRepository;
import com.edufelizardo.maissaudepublica.repositories.InstrumentoContratualRepository;
import com.edufelizardo.maissaudepublica.repositories.ItemContratacaoRepository;
import com.edufelizardo.maissaudepublica.repositories.ItemInstrumentoRepository;
import com.edufelizardo.maissaudepublica.repositories.ItemSolicitacaoCompraRepository;
import com.edufelizardo.maissaudepublica.repositories.ProfissionalRepository;
import com.edufelizardo.maissaudepublica.repositories.SolicitacaoCompraRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Compras, fatia CP2 (ADR-0213): a contratação reúne as solicitações cotadas, registra o resultado por item e, na homologação, gera a ata
 * de registro de preços ou o contrato de cada fornecedor vencedor, com o gestor e o fiscal; os aditivos mudam prazo, quantidade e preço
 * dentro dos limites da Lei 14.133/2021. O setor de compras conduz (COMPRAS.GERENCIAR); o gestor e o fiscal leem os próprios contratos.
 */
@Service
public class ContratosService {

    static final int DIAS_ALERTA_VIGENCIA = 90;
    static final BigDecimal LIMITE_ADITIVO = new BigDecimal("25");
    private static final ZoneId ZONA = ZoneId.of("America/Sao_Paulo");

    @Autowired private ContratacaoRepository contratacaoRepository;
    @Autowired private ItemContratacaoRepository itemContratacaoRepository;
    @Autowired private InstrumentoContratualRepository instrumentoRepository;
    @Autowired private ItemInstrumentoRepository itemInstrumentoRepository;
    @Autowired private AditivoContratualRepository aditivoRepository;
    @Autowired private SolicitacaoCompraRepository solicitacaoRepository;
    @Autowired private ItemSolicitacaoCompraRepository itemSolicitacaoRepository;
    @Autowired private FornecedorRepository fornecedorRepository;
    @Autowired private ProfissionalRepository profissionalRepository;
    @Autowired private ComprasService comprasService;
    @Autowired private ControleDeAcesso controleDeAcesso;
    @Autowired private com.edufelizardo.maissaudepublica.repositories.OrdemFornecimentoRepository ordemRepository;

    // ── Contratação ────────────────────────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<ContratosDtos.Contratacao> contratacoes(SituacaoContratacao situacao) {
        controleDeAcesso.exigirAlguma(ComprasService.GERENCIAR);
        return contratacaoRepository.findTodas().stream().filter(c -> situacao == null || c.getSituacao() == situacao).map(this::dto).toList();
    }

    @Transactional(readOnly = true)
    public ContratosDtos.Contratacao contratacao(UUID id) {
        controleDeAcesso.exigirAlguma(ComprasService.GERENCIAR);
        return dto(contratacaoOu404(id));
    }

    /**
     * Abre a contratação: processo único (409); dispensa e inexigibilidade pedem o fundamento, a adesão pede a ata de origem (400); as
     * solicitações estão cotadas (422) e fora de outra contratação em andamento (409). Os itens iguais somam a quantidade, e o preço de
     * referência é a média ponderada pela quantidade.
     */
    @Transactional
    public ContratosDtos.Contratacao abrir(ContratacaoRequestDto dto) {
        controleDeAcesso.exigirAlguma(ComprasService.GERENCIAR);
        String processo = dto.getNumeroProcesso().trim();
        if (contratacaoRepository.existsByNumeroProcesso(processo)) {
            throw new ResourceConflictException("Já existe contratação com o processo " + processo + ".");
        }
        boolean pedeFundamento = dto.getModalidade() == ModalidadeContratacao.DISPENSA || dto.getModalidade() == ModalidadeContratacao.INEXIGIBILIDADE;
        if (pedeFundamento && limpo(dto.getFundamentoLegal()) == null) {
            throw new ResourceBadRequestException("A dispensa e a inexigibilidade pedem o fundamento legal (arts. 74 e 75).");
        }
        if (dto.getModalidade() == ModalidadeContratacao.ADESAO_ATA && limpo(dto.getAtaOrigem()) == null) {
            throw new ResourceBadRequestException("A adesão pede a ata de origem: o órgão gerenciador e o número.");
        }
        List<SolicitacaoCompra> solicitacoes = new ArrayList<>();
        for (UUID id : new HashSet<>(dto.getSolicitacaoIds())) {
            SolicitacaoCompra s = solicitacaoRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(
                    "Não foi possível encontrar a solicitação de compra " + id + " em nossos registros."));
            if (s.getSituacao() != SituacaoSolicitacaoCompra.COTADA) {
                throw new ResourceUnprocessableEntityException("A solicitação " + s.getNumero() + " não está cotada.");
            }
            if (s.getContratacao() != null && s.getContratacao().getSituacao() == SituacaoContratacao.EM_ANDAMENTO) {
                throw new ResourceConflictException("A solicitação " + s.getNumero() + " já está na contratação " + s.getContratacao().getNumeroProcesso() + ".");
            }
            solicitacoes.add(s);
        }
        Contratacao c = new Contratacao();
        c.setNumeroProcesso(processo);
        c.setModalidade(dto.getModalidade());
        c.setFundamentoLegal(pedeFundamento ? limpo(dto.getFundamentoLegal()) : null);
        c.setAtaOrigem(dto.getModalidade() == ModalidadeContratacao.ADESAO_ATA ? limpo(dto.getAtaOrigem()) : null);
        c.setObjeto(dto.getObjeto().trim());
        c.setSituacao(SituacaoContratacao.EM_ANDAMENTO);
        c.setRegistradoEm(Instant.now());
        c.setRegistradoPorCpf(UsuarioAutenticado.cpf());
        contratacaoRepository.save(c);

        Map<String, List<ItemSolicitacaoCompra>> grupos = new LinkedHashMap<>();
        itemSolicitacaoRepository.findDasSolicitacoes(solicitacoes.stream().map(SolicitacaoCompra::getUuid).toList())
                .forEach(i -> grupos.computeIfAbsent(chave(i), k -> new ArrayList<>()).add(i));
        int ordem = 0;
        for (List<ItemSolicitacaoCompra> grupo : grupos.values()) {
            ItemSolicitacaoCompra primeiro = grupo.get(0);
            int quantidade = grupo.stream().mapToInt(ItemSolicitacaoCompra::getQuantidade).sum();
            BigDecimal ponderado = grupo.stream().filter(i -> i.getPrecoReferencia() != null)
                    .map(i -> i.getPrecoReferencia().multiply(BigDecimal.valueOf(i.getQuantidade()))).reduce(BigDecimal.ZERO, BigDecimal::add);
            ItemContratacao item = new ItemContratacao();
            item.setContratacao(c);
            item.setOrdem(++ordem);
            item.setTipo(primeiro.getTipo());
            item.setMaterial(primeiro.getMaterial());
            item.setMedicamento(primeiro.getMedicamento());
            item.setDescricao(primeiro.getMaterial() != null ? primeiro.getMaterial().getNome() : primeiro.getDescricao());
            item.setUnidadeMedida(primeiro.getUnidadeMedida());
            item.setQuantidade(quantidade);
            item.setPrecoReferencia(ponderado.divide(BigDecimal.valueOf(quantidade), 4, RoundingMode.HALF_UP));
            item.setResultado(ResultadoItemContratacao.PENDENTE);
            itemContratacaoRepository.save(item);
            grupo.forEach(i -> i.setItemContratacao(item));
            itemSolicitacaoRepository.saveAll(grupo);
        }
        solicitacoes.forEach(s -> s.setContratacao(c));
        solicitacaoRepository.saveAll(solicitacoes);
        ContextoAuditoria.registro(c.getUuid());
        return dto(c);
    }

    /**
     * O resultado do item, com a contratação em andamento (422). Adjudicado pede o fornecedor e o preço, fracassado e deserto não (400); o
     * fornecedor está regular (422); acima da referência, a justificativa (422).
     */
    @Transactional
    public ContratosDtos.Contratacao resultado(UUID itemId, ResultadoItemContratacaoRequestDto dto) {
        controleDeAcesso.exigirAlguma(ComprasService.GERENCIAR);
        ItemContratacao item = itemContratacaoRepository.findById(itemId).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar o item " + itemId + " em nossos registros."));
        Contratacao c = item.getContratacao();
        ContextoAuditoria.registro(c.getUuid());
        emAndamento(c);
        boolean adjudicado = dto.getResultado() == ResultadoItemContratacao.ADJUDICADO;
        if (dto.getResultado() == ResultadoItemContratacao.PENDENTE) {
            throw new ResourceBadRequestException("Informe adjudicado, fracassado ou deserto.");
        }
        if (adjudicado != (dto.getFornecedorId() != null && dto.getPrecoUnitario() != null)) {
            throw new ResourceBadRequestException("O item adjudicado pede o fornecedor e o preço; o fracassado e o deserto, não.");
        }
        item.setResultado(dto.getResultado());
        item.setFornecedor(null);
        item.setPrecoUnitario(null);
        item.setJustificativaPreco(null);
        if (adjudicado) {
            Fornecedor f = fornecedorOu404(dto.getFornecedorId());
            exigirRegular(f);
            String justificativa = limpo(dto.getJustificativaPreco());
            if (item.getPrecoReferencia() != null && dto.getPrecoUnitario().compareTo(item.getPrecoReferencia()) > 0 && justificativa == null) {
                throw new ResourceUnprocessableEntityException("O preço passa da referência (" + item.getPrecoReferencia().stripTrailingZeros().toPlainString()
                        + "); justifique.");
            }
            item.setFornecedor(f);
            item.setPrecoUnitario(dto.getPrecoUnitario());
            item.setJustificativaPreco(justificativa);
        }
        itemContratacaoRepository.save(item);
        ContextoAuditoria.detalhe("Item " + item.getOrdem() + ": " + dto.getResultado());
        return dto(c);
    }

    /**
     * Homologa (422 fora do andamento; com item pendente, 422): um instrumento por fornecedor vencedor, nem mais nem menos (400); número
     * único (409); vigência dentro do limite do tipo (400); gestor e fiscal diferentes (422); fornecedor ainda regular (422). As
     * solicitações passam a contratadas.
     */
    @Transactional
    public ContratosDtos.Contratacao homologar(UUID id, HomologacaoContratacaoRequestDto dto) {
        controleDeAcesso.exigirAlguma(ComprasService.GERENCIAR);
        Contratacao c = contratacaoOu404(id);
        ContextoAuditoria.registro(c.getUuid());
        emAndamento(c);
        List<ItemContratacao> itens = itemContratacaoRepository.findDaContratacao(c.getUuid());
        if (itens.stream().anyMatch(i -> i.getResultado() == ResultadoItemContratacao.PENDENTE)) {
            throw new ResourceUnprocessableEntityException("Há item sem resultado.");
        }
        Map<UUID, List<ItemContratacao>> porFornecedor = itens.stream().filter(i -> i.getResultado() == ResultadoItemContratacao.ADJUDICADO)
                .collect(Collectors.groupingBy(i -> i.getFornecedor().getUuid(), LinkedHashMap::new, Collectors.toList()));
        List<HomologacaoContratacaoRequestDto.Instrumento> pedidos = dto.getInstrumentos() == null ? List.of() : dto.getInstrumentos();
        Set<UUID> informados = pedidos.stream().map(HomologacaoContratacaoRequestDto.Instrumento::getFornecedorId).collect(Collectors.toSet());
        if (informados.size() != pedidos.size() || !informados.equals(porFornecedor.keySet())) {
            throw new ResourceBadRequestException("Informe uma ata ou um contrato para cada fornecedor vencedor, e só para eles.");
        }
        Set<String> numeros = new HashSet<>();
        for (HomologacaoContratacaoRequestDto.Instrumento p : pedidos) {
            String numero = p.getNumero().trim();
            if (!numeros.add(numero.toLowerCase(Locale.ROOT)) || instrumentoRepository.existsByNumero(numero)) {
                throw new ResourceConflictException("Já existe ata ou contrato com o número " + numero + ".");
            }
            List<ItemContratacao> doFornecedor = porFornecedor.get(p.getFornecedorId());
            boolean continuo = Boolean.TRUE.equals(p.getServicoContinuo());
            validarVigencia(p.getTipo(), continuo, p.getInicio(), p.getFim(), doFornecedor);
            Profissional gestor = profissional(p.getGestorMatricula());
            Profissional fiscal = profissional(p.getFiscalMatricula());
            if (gestor.getUuid().equals(fiscal.getUuid())) {
                throw new ResourceUnprocessableEntityException("O gestor e o fiscal são pessoas diferentes (segregação de funções, art. 7º).");
            }
            Fornecedor f = doFornecedor.get(0).getFornecedor();
            exigirRegular(f);
            InstrumentoContratual ins = new InstrumentoContratual();
            ins.setContratacao(c);
            ins.setTipo(p.getTipo());
            ins.setNumero(numero);
            ins.setFornecedor(f);
            ins.setInicio(p.getInicio());
            ins.setFim(p.getFim());
            ins.setServicoContinuo(continuo);
            ins.setGestor(gestor);
            ins.setFiscal(fiscal);
            ins.setValorInicial(doFornecedor.stream().map(i -> i.getPrecoUnitario().multiply(BigDecimal.valueOf(i.getQuantidade())))
                    .reduce(BigDecimal.ZERO, BigDecimal::add).setScale(2, RoundingMode.HALF_UP));
            ins.setRegistradoEm(Instant.now());
            ins.setRegistradoPorCpf(UsuarioAutenticado.cpf());
            instrumentoRepository.save(ins);
            for (ItemContratacao i : doFornecedor) {
                itemInstrumentoRepository.save(new ItemInstrumento(null, ins, i, i.getQuantidade(), i.getQuantidade(), i.getQuantidade(), i.getPrecoUnitario()));
            }
        }
        c.setSituacao(SituacaoContratacao.HOMOLOGADA);
        c.setHomologadaEm(hoje());
        contratacaoRepository.save(c);
        List<SolicitacaoCompra> solicitacoes = solicitacoesDa(c);
        solicitacoes.forEach(s -> s.setSituacao(SituacaoSolicitacaoCompra.CONTRATADA));
        solicitacaoRepository.saveAll(solicitacoes);
        ContextoAuditoria.detalhe("Homologada com " + pedidos.size() + " instrumento(s)");
        return dto(c);
    }

    /** Cancela a contratação em andamento (422), com o motivo; as solicitações voltam a cotadas, livres para outra contratação. */
    @Transactional
    public ContratosDtos.Contratacao cancelar(UUID id, String motivo) {
        controleDeAcesso.exigirAlguma(ComprasService.GERENCIAR);
        Contratacao c = contratacaoOu404(id);
        ContextoAuditoria.registro(c.getUuid());
        emAndamento(c);
        List<SolicitacaoCompra> solicitacoes = solicitacoesDa(c);
        List<ItemSolicitacaoCompra> itens = itemSolicitacaoRepository.findDasSolicitacoes(solicitacoes.stream().map(SolicitacaoCompra::getUuid).toList());
        itens.forEach(i -> i.setItemContratacao(null));
        itemSolicitacaoRepository.saveAll(itens);
        solicitacoes.forEach(s -> s.setContratacao(null));
        solicitacaoRepository.saveAll(solicitacoes);
        c.setSituacao(SituacaoContratacao.CANCELADA);
        c.setMotivoCancelamento(motivo.trim());
        contratacaoRepository.save(c);
        return dto(c);
    }

    // ── Atas e contratos ───────────────────────────────────────────────────────────────────────────

    /** Para quem compra, todos; {@code comAlerta} deixa só os que têm alerta. */
    @Transactional(readOnly = true)
    public List<ContratosDtos.Instrumento> instrumentos(TipoInstrumentoContratual tipo, Boolean comAlerta) {
        controleDeAcesso.exigirAlguma(ComprasService.GERENCIAR);
        List<InstrumentoContratual> lista = instrumentoRepository.findTodos().stream().filter(i -> tipo == null || i.getTipo() == tipo).toList();
        return dtos(lista).stream().filter(i -> !Boolean.TRUE.equals(comAlerta) || !i.alertas().isEmpty()).toList();
    }

    /** As atas e os contratos em que a pessoa logada é o gestor ou o fiscal. */
    @Transactional(readOnly = true)
    public List<ContratosDtos.Instrumento> meus() {
        String cpf = UsuarioAutenticado.cpf();
        if (cpf == null) {
            return List.of();
        }
        return dtos(instrumentoRepository.findTodos().stream().filter(i -> designado(i, cpf)).toList());
    }

    /** Quem compra ou quem foi designado gestor ou fiscal (403 aos outros). */
    @Transactional(readOnly = true)
    public ContratosDtos.Instrumento instrumento(UUID id) {
        InstrumentoContratual i = instrumentoOu404(id);
        String cpf = UsuarioAutenticado.cpf();
        if (!controleDeAcesso.temAlguma(ComprasService.GERENCIAR) && (cpf == null || !designado(i, cpf))) {
            throw new ResourceForbiddenException("Sem acesso a esta ata ou contrato.");
        }
        return dtos(List.of(i)).get(0);
    }

    /**
     * O aditivo, assinado até hoje e dentro da vigência (400/422):
     * <ul>
     *   <li>prazo: novo fim depois do atual (400); a ata prorroga uma vez, até 2 anos do início (art. 84); o serviço contínuo, até 10
     *   anos (art. 107) (422);</li>
     *   <li>acréscimo e supressão: só no contrato (422); a soma de cada um até 25% do valor inicial (art. 125, 422); a supressão não
     *   passa do saldo (422);</li>
     *   <li>reequilíbrio: o novo preço do item (400).</li>
     * </ul>
     */
    @Transactional
    public ContratosDtos.Instrumento aditar(UUID id, AditivoContratualRequestDto dto) {
        controleDeAcesso.exigirAlguma(ComprasService.GERENCIAR);
        InstrumentoContratual ins = instrumentoOu404(id);
        ContextoAuditoria.registro(ins.getUuid());
        LocalDate hoje = hoje();
        if (dto.getAssinadoEm().isAfter(hoje)) {
            throw new ResourceBadRequestException("A assinatura não pode ser no futuro.");
        }
        if (dto.getAssinadoEm().isAfter(ins.getFim()) || ins.getFim().isBefore(hoje)) {
            throw new ResourceUnprocessableEntityException("O aditivo é assinado dentro da vigência.");
        }
        List<AditivoContratual> anteriores = aditivoRepository.findDosInstrumentos(List.of(ins.getUuid()));
        boolean ata = ins.getTipo() == TipoInstrumentoContratual.ATA_REGISTRO_PRECOS;
        AditivoContratual a = new AditivoContratual();
        a.setInstrumento(ins);
        a.setNumero(anteriores.size() + 1);
        a.setTipo(dto.getTipo());
        a.setJustificativa(dto.getJustificativa().trim());
        a.setAssinadoEm(dto.getAssinadoEm());
        a.setRegistradoEm(Instant.now());
        a.setRegistradoPorCpf(UsuarioAutenticado.cpf());
        switch (dto.getTipo()) {
            case PRAZO -> {
                if (dto.getNovoFim() == null || !dto.getNovoFim().isAfter(ins.getFim())) {
                    throw new ResourceBadRequestException("A prorrogação pede o novo fim, depois do atual.");
                }
                if (ata && anteriores.stream().anyMatch(x -> x.getTipo() == TipoAditivoContratual.PRAZO)) {
                    throw new ResourceUnprocessableEntityException("A ata de registro de preços se prorroga uma vez só (art. 84).");
                }
                if (ata && dto.getNovoFim().isAfter(ins.getInicio().plusYears(2))) {
                    throw new ResourceUnprocessableEntityException("A ata vai até 2 anos do início, com a prorrogação (art. 84).");
                }
                if (!ata && ins.isServicoContinuo() && dto.getNovoFim().isAfter(ins.getInicio().plusYears(10))) {
                    throw new ResourceUnprocessableEntityException("O serviço contínuo vai até 10 anos (art. 107).");
                }
                a.setFimAnterior(ins.getFim());
                a.setNovoFim(dto.getNovoFim());
                ins.setFim(dto.getNovoFim());
                instrumentoRepository.save(ins);
            }
            case ACRESCIMO, SUPRESSAO -> {
                if (ata) {
                    throw new ResourceUnprocessableEntityException("A ata de registro de preços não aceita acréscimo nem supressão de quantidade.");
                }
                ItemInstrumento item = itemDo(ins, dto.getItemId());
                if (dto.getQuantidade() == null) {
                    throw new ResourceBadRequestException("Informe a quantidade.");
                }
                boolean acrescimo = dto.getTipo() == TipoAditivoContratual.ACRESCIMO;
                if (!acrescimo && dto.getQuantidade() > item.getSaldo()) {
                    throw new ResourceUnprocessableEntityException("A supressão não passa do saldo do item (" + item.getSaldo() + ").");
                }
                BigDecimal ja = anteriores.stream().filter(x -> x.getTipo() == dto.getTipo()).map(ContratosService::valorDoAditivo).reduce(BigDecimal.ZERO, BigDecimal::add);
                BigDecimal agora = item.getPrecoUnitario().multiply(BigDecimal.valueOf(dto.getQuantidade()));
                if (percentual(ja.add(agora), ins.getValorInicial()).compareTo(LIMITE_ADITIVO) > 0) {
                    throw new ResourceUnprocessableEntityException((acrescimo ? "Os acréscimos" : "As supressões") + " passam de 25% do valor inicial (art. 125).");
                }
                a.setItemInstrumento(item);
                a.setQuantidade(dto.getQuantidade());
                a.setPrecoAnterior(item.getPrecoUnitario());
                int sinal = acrescimo ? 1 : -1;
                item.setQuantidade(item.getQuantidade() + sinal * dto.getQuantidade());
                item.setSaldo(item.getSaldo() + sinal * dto.getQuantidade());
                itemInstrumentoRepository.save(item);
            }
            case REEQUILIBRIO -> {
                ItemInstrumento item = itemDo(ins, dto.getItemId());
                if (dto.getNovoPreco() == null) {
                    throw new ResourceBadRequestException("O reequilíbrio pede o novo preço.");
                }
                a.setItemInstrumento(item);
                a.setPrecoAnterior(item.getPrecoUnitario());
                a.setNovoPreco(dto.getNovoPreco());
                item.setPrecoUnitario(dto.getNovoPreco());
                itemInstrumentoRepository.save(item);
            }
        }
        aditivoRepository.save(a);
        ContextoAuditoria.detalhe("Aditivo " + a.getNumero() + ": " + dto.getTipo());
        return dtos(List.of(ins)).get(0);
    }

    // ── Apoio ──────────────────────────────────────────────────────────────────────────────────────

    /**
     * A ata vai até 1 ano (art. 84) e não é de serviço contínuo; o contrato de serviço contínuo, até 5 anos (art. 106), e só com serviços;
     * os demais, até 1 ano (400). O fim é depois do início (400).
     */
    private static void validarVigencia(TipoInstrumentoContratual tipo, boolean continuo, LocalDate inicio, LocalDate fim, List<ItemContratacao> itens) {
        if (!fim.isAfter(inicio)) {
            throw new ResourceBadRequestException("O fim da vigência é depois do início.");
        }
        if (continuo && (tipo == TipoInstrumentoContratual.ATA_REGISTRO_PRECOS || itens.stream().anyMatch(i -> i.getTipo() != TipoItemCompra.SERVICO))) {
            throw new ResourceBadRequestException("Serviço contínuo é contrato só de serviços.");
        }
        int anos = continuo ? 5 : 1;
        if (fim.isAfter(inicio.plusYears(anos))) {
            throw new ResourceBadRequestException(tipo == TipoInstrumentoContratual.ATA_REGISTRO_PRECOS ? "A ata vai até 1 ano (art. 84)."
                    : continuo ? "O serviço contínuo vai até 5 anos (art. 106)." : "O contrato vai até 1 ano; serviço contínuo, até 5.");
        }
    }

    private void exigirRegular(Fornecedor f) {
        ComprasDtos.Fornecedor s = comprasService.situacao(f);
        if (!s.regular()) {
            throw new ResourceUnprocessableEntityException("O fornecedor " + f.getRazaoSocial() + " não está regular: "
                    + (!s.ativo() ? "inativo" : s.impedido() ? "impedido de licitar e contratar" : "certidão pendente (" + s.certidoesPendentes() + ")") + ".");
        }
    }

    private static boolean designado(InstrumentoContratual i, String cpf) {
        return cpf.equals(i.getGestor().getCpf()) || cpf.equals(i.getFiscal().getCpf());
    }

    private static BigDecimal valorDoAditivo(AditivoContratual a) {
        return a.getPrecoAnterior().multiply(BigDecimal.valueOf(a.getQuantidade()));
    }

    private static BigDecimal percentual(BigDecimal parte, BigDecimal total) {
        return total.signum() == 0 ? BigDecimal.ZERO : parte.multiply(BigDecimal.valueOf(100)).divide(total, 2, RoundingMode.HALF_UP);
    }

    private ItemInstrumento itemDo(InstrumentoContratual ins, UUID itemId) {
        if (itemId == null) {
            throw new ResourceBadRequestException("Informe o item.");
        }
        return itemInstrumentoRepository.findDosInstrumentos(List.of(ins.getUuid())).stream().filter(i -> i.getUuid().equals(itemId)).findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("O item " + itemId + " não é desta ata ou contrato."));
    }

    private static String chave(ItemSolicitacaoCompra i) {
        return switch (i.getTipo()) {
            case MATERIAL -> "M:" + i.getMaterial().getUuid();
            case MEDICAMENTO -> "F:" + i.getMedicamento().getUuid();
            case SERVICO -> "S:" + i.getDescricao().trim().toLowerCase(Locale.ROOT) + "|" + i.getUnidadeMedida();
        };
    }

    private List<SolicitacaoCompra> solicitacoesDa(Contratacao c) {
        return solicitacaoRepository.findTodas().stream().filter(s -> s.getContratacao() != null && s.getContratacao().getUuid().equals(c.getUuid())).toList();
    }

    private static void emAndamento(Contratacao c) {
        if (c.getSituacao() != SituacaoContratacao.EM_ANDAMENTO) {
            throw new ResourceUnprocessableEntityException("A contratação já foi " + (c.getSituacao() == SituacaoContratacao.HOMOLOGADA ? "homologada." : "cancelada."));
        }
    }

    private Profissional profissional(String matricula) {
        String m = matricula.trim();
        return profissionalRepository.findByMatricula(m).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar o profissional de matrícula " + m + " em nossos registros."));
    }

    private Fornecedor fornecedorOu404(UUID id) {
        return fornecedorRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar o fornecedor " + id + " em nossos registros."));
    }

    private Contratacao contratacaoOu404(UUID id) {
        return contratacaoRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar a contratação " + id + " em nossos registros."));
    }

    private InstrumentoContratual instrumentoOu404(UUID id) {
        return instrumentoRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar a ata ou o contrato " + id + " em nossos registros."));
    }

    private ContratosDtos.Contratacao dto(Contratacao c) {
        List<ContratosDtos.ItemContratacao> itens = itemContratacaoRepository.findDaContratacao(c.getUuid()).stream()
                .map(i -> new ContratosDtos.ItemContratacao(i.getUuid(), i.getOrdem(), i.getTipo(), i.getDescricao(), i.getUnidadeMedida(), i.getQuantidade(),
                        i.getPrecoReferencia(), i.getResultado(), i.getFornecedor() != null ? i.getFornecedor().getUuid() : null,
                        i.getFornecedor() != null ? i.getFornecedor().getRazaoSocial() : null, i.getPrecoUnitario(), i.getJustificativaPreco(),
                        i.getPrecoUnitario() == null ? null : i.getPrecoUnitario().multiply(BigDecimal.valueOf(i.getQuantidade())).setScale(2, RoundingMode.HALF_UP)))
                .toList();
        BigDecimal referencia = itens.stream().filter(i -> i.precoReferencia() != null).map(i -> i.precoReferencia().multiply(BigDecimal.valueOf(i.quantidade())))
                .reduce(BigDecimal.ZERO, BigDecimal::add).setScale(2, RoundingMode.HALF_UP);
        BigDecimal adjudicado = itens.stream().map(ContratosDtos.ItemContratacao::valor).filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
        List<ContratosDtos.SolicitacaoResumo> solicitacoes = c.getSituacao() == SituacaoContratacao.CANCELADA ? List.of()
                : solicitacoesDa(c).stream().map(s -> new ContratosDtos.SolicitacaoResumo(s.getUuid(), s.getNumero(), s.getUnidade().getNome())).toList();
        List<ContratosDtos.InstrumentoResumo> instrumentos = instrumentoRepository.findTodos().stream().filter(i -> i.getContratacao().getUuid().equals(c.getUuid()))
                .map(i -> new ContratosDtos.InstrumentoResumo(i.getUuid(), i.getTipo(), i.getNumero(), i.getFornecedor().getRazaoSocial())).toList();
        return new ContratosDtos.Contratacao(c.getUuid(), c.getNumeroProcesso(), c.getModalidade(), c.getFundamentoLegal(), c.getAtaOrigem(), c.getObjeto(),
                c.getSituacao(), c.getHomologadaEm(), c.getMotivoCancelamento(), solicitacoes, itens, referencia, adjudicado, instrumentos);
    }

    private List<ContratosDtos.Instrumento> dtos(List<InstrumentoContratual> lista) {
        if (lista.isEmpty()) {
            return List.of();
        }
        List<UUID> ids = lista.stream().map(InstrumentoContratual::getUuid).toList();
        Map<UUID, List<ItemInstrumento>> itens = itemInstrumentoRepository.findDosInstrumentos(ids).stream().collect(Collectors.groupingBy(i -> i.getInstrumento().getUuid()));
        Map<UUID, List<AditivoContratual>> aditivos = aditivoRepository.findDosInstrumentos(ids).stream().collect(Collectors.groupingBy(a -> a.getInstrumento().getUuid()));
        Map<UUID, ComprasDtos.Fornecedor> situacoes = new HashMap<>();
        Map<UUID, Long> ordensAbertas = ordemRepository.findTodas().stream().filter(o -> o.getSituacao() == com.edufelizardo.maissaudepublica.models.enuns.SituacaoOrdemFornecimento.EMITIDA
                || o.getSituacao() == com.edufelizardo.maissaudepublica.models.enuns.SituacaoOrdemFornecimento.PARCIAL)
                .collect(Collectors.groupingBy(o -> o.getInstrumento().getUuid(), Collectors.counting()));
        LocalDate hoje = hoje();
        return lista.stream().map(ins -> {
            List<ItemInstrumento> doIns = itens.getOrDefault(ins.getUuid(), List.of()).stream()
                    .sorted((x, y) -> Integer.compare(x.getItemContratacao().getOrdem(), y.getItemContratacao().getOrdem())).toList();
            List<AditivoContratual> adts = aditivos.getOrDefault(ins.getUuid(), List.of());
            Map<UUID, String> descricoes = doIns.stream().collect(Collectors.toMap(ItemInstrumento::getUuid, i -> i.getItemContratacao().getDescricao()));
            long dias = ChronoUnit.DAYS.between(hoje, ins.getFim());
            boolean vigente = !ins.getInicio().isAfter(hoje) && dias >= 0;
            List<ContratosDtos.ItemInstrumento> itensDto = doIns.stream().map(i -> {
                ItemContratacao ic = i.getItemContratacao();
                return new ContratosDtos.ItemInstrumento(i.getUuid(), ic.getTipo(), ic.getMaterial() != null ? ic.getMaterial().getUuid() : null,
                        ic.getMedicamento() != null ? ic.getMedicamento().getUuid() : null, ic.getDescricao(), ic.getUnidadeMedida(), i.getQuantidadeInicial(),
                        i.getQuantidade(), i.getSaldo(), i.getPrecoUnitario(), i.getQuantidade() > 0 && i.getSaldo() * 5 < i.getQuantidade());
            }).toList();
            List<String> alertas = new ArrayList<>();
            if (dias < 0) {
                alertas.add("Vigência encerrada em " + ins.getFim());
            } else if (dias <= DIAS_ALERTA_VIGENCIA) {
                alertas.add("Vigência termina em " + dias + " dia(s)");
            }
            itensDto.stream().filter(ContratosDtos.ItemInstrumento::saldoBaixo).forEach(i -> alertas.add("Saldo abaixo de 20%: " + i.descricao()));
            if (dias >= 0) {
                ComprasDtos.Fornecedor s = situacoes.computeIfAbsent(ins.getFornecedor().getUuid(), k -> comprasService.situacao(ins.getFornecedor()));
                if (s.impedido()) {
                    alertas.add("Fornecedor impedido de licitar e contratar");
                } else if (!s.certidoesPendentes().isEmpty()) {
                    alertas.add("Fornecedor com certidão pendente: " + s.certidoesPendentes().stream().map(Enum::name).collect(Collectors.joining(", ")));
                }
            }
            BigDecimal atual = doIns.stream().map(i -> i.getPrecoUnitario().multiply(BigDecimal.valueOf(i.getQuantidade()))).reduce(BigDecimal.ZERO, BigDecimal::add)
                    .setScale(2, RoundingMode.HALF_UP);
            BigDecimal acrescido = percentual(adts.stream().filter(a -> a.getTipo() == TipoAditivoContratual.ACRESCIMO).map(ContratosService::valorDoAditivo)
                    .reduce(BigDecimal.ZERO, BigDecimal::add), ins.getValorInicial());
            BigDecimal suprimido = percentual(adts.stream().filter(a -> a.getTipo() == TipoAditivoContratual.SUPRESSAO).map(ContratosService::valorDoAditivo)
                    .reduce(BigDecimal.ZERO, BigDecimal::add), ins.getValorInicial());
            List<ContratosDtos.Aditivo> aditivosDto = adts.stream().map(a -> new ContratosDtos.Aditivo(a.getUuid(), a.getNumero(), a.getTipo(), a.getFimAnterior(),
                    a.getNovoFim(), a.getItemInstrumento() != null ? a.getItemInstrumento().getUuid() : null,
                    a.getItemInstrumento() != null ? descricoes.get(a.getItemInstrumento().getUuid()) : null, a.getQuantidade(), a.getPrecoAnterior(),
                    a.getNovoPreco(), a.getJustificativa(), a.getAssinadoEm())).toList();
            return new ContratosDtos.Instrumento(ins.getUuid(), ins.getTipo(), ins.getNumero(), ins.getContratacao().getUuid(),
                    ins.getContratacao().getNumeroProcesso(), ins.getFornecedor().getUuid(), ins.getFornecedor().getRazaoSocial(), ins.getInicio(), ins.getFim(),
                    ins.isServicoContinuo(), vigente, dias, ins.getValorInicial(), atual, acrescido, suprimido, ins.getGestor().getMatricula(),
                    ins.getGestor().getNome(), ins.getFiscal().getMatricula(), ins.getFiscal().getNome(), itensDto, aditivosDto, alertas,
                    ordensAbertas.getOrDefault(ins.getUuid(), 0L));
        }).toList();
    }

    private static String limpo(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }

    private static LocalDate hoje() {
        return LocalDate.now(ZONA);
    }
}
