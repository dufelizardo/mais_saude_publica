package com.edufelizardo.maissaudepublica.services.version1;

import com.edufelizardo.maissaudepublica.config.ContextoAuditoria;
import com.edufelizardo.maissaudepublica.config.UsuarioAutenticado;
import com.edufelizardo.maissaudepublica.exceptions.ResourceBadRequestException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceConflictException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceForbiddenException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceNotFoundException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceUnprocessableEntityException;
import com.edufelizardo.maissaudepublica.models.CertidaoFornecedor;
import com.edufelizardo.maissaudepublica.models.CotacaoCompra;
import com.edufelizardo.maissaudepublica.models.EstoqueMinimo;
import com.edufelizardo.maissaudepublica.models.Fornecedor;
import com.edufelizardo.maissaudepublica.models.ItemSolicitacaoCompra;
import com.edufelizardo.maissaudepublica.models.LoteMaterial;
import com.edufelizardo.maissaudepublica.models.MaterialEstoque;
import com.edufelizardo.maissaudepublica.models.Medicamento;
import com.edufelizardo.maissaudepublica.models.SancaoFornecedor;
import com.edufelizardo.maissaudepublica.models.Setor;
import com.edufelizardo.maissaudepublica.models.SolicitacaoCompra;
import com.edufelizardo.maissaudepublica.models.UnidadeDeSaude;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.AnaliseSolicitacaoCompraRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.CertidaoFornecedorRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.ConclusaoPesquisaRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.CotacaoCompraRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.FornecedorRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.SancaoFornecedorRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.SolicitacaoCompraRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.ComprasDtos;
import com.edufelizardo.maissaudepublica.models.enuns.FonteCotacao;
import com.edufelizardo.maissaudepublica.models.enuns.PrioridadeCompra;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoSolicitacaoCompra;
import com.edufelizardo.maissaudepublica.models.enuns.TipoCertidaoFornecedor;
import com.edufelizardo.maissaudepublica.models.enuns.TipoItemCompra;
import com.edufelizardo.maissaudepublica.models.enuns.TipoSancaoFornecedor;
import com.edufelizardo.maissaudepublica.repositories.CertidaoFornecedorRepository;
import com.edufelizardo.maissaudepublica.repositories.CotacaoCompraRepository;
import com.edufelizardo.maissaudepublica.repositories.EstoqueMinimoRepository;
import com.edufelizardo.maissaudepublica.repositories.FornecedorRepository;
import com.edufelizardo.maissaudepublica.repositories.ItemSolicitacaoCompraRepository;
import com.edufelizardo.maissaudepublica.repositories.LoteMaterialRepository;
import com.edufelizardo.maissaudepublica.repositories.MaterialEstoqueRepository;
import com.edufelizardo.maissaudepublica.repositories.MedicamentoRepository;
import com.edufelizardo.maissaudepublica.repositories.SancaoFornecedorRepository;
import com.edufelizardo.maissaudepublica.repositories.SetorRepository;
import com.edufelizardo.maissaudepublica.repositories.SolicitacaoCompraRepository;
import com.edufelizardo.maissaudepublica.repositories.UnidadeDeSaudeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Compras, fatia CP1 (ADR-0212): o fornecedor com as certidões e as sanções, a solicitação de compra (DFD) da unidade e a pesquisa de
 * preços. A unidade pede (COMPRAS.SOLICITAR, no escopo da unidade); o setor de compras, central, analisa, cota e mantém os fornecedores
 * (COMPRAS.GERENCIAR, na rede inteira).
 */
@Service
public class ComprasService {

    public static final String SOLICITAR = "COMPRAS.SOLICITAR";
    public static final String GERENCIAR = "COMPRAS.GERENCIAR";
    static final int MINIMO_DE_COTACOES = 3;
    private static final ZoneId ZONA = ZoneId.of("America/Sao_Paulo");
    private static final Set<SituacaoSolicitacaoCompra> EDITAVEIS = EnumSet.of(SituacaoSolicitacaoCompra.RASCUNHO, SituacaoSolicitacaoCompra.DEVOLVIDA);
    private static final Set<SituacaoSolicitacaoCompra> EM_COTACAO = EnumSet.of(SituacaoSolicitacaoCompra.APROVADA, SituacaoSolicitacaoCompra.EM_COTACAO);
    private static final Set<SituacaoSolicitacaoCompra> FECHADAS = EnumSet.of(SituacaoSolicitacaoCompra.REJEITADA, SituacaoSolicitacaoCompra.COTADA,
            SituacaoSolicitacaoCompra.CANCELADA);
    private static final Set<TipoSancaoFornecedor> IMPEDEM = EnumSet.of(TipoSancaoFornecedor.IMPEDIMENTO, TipoSancaoFornecedor.INIDONEIDADE);

    @Autowired private FornecedorRepository fornecedorRepository;
    @Autowired private CertidaoFornecedorRepository certidaoRepository;
    @Autowired private SancaoFornecedorRepository sancaoRepository;
    @Autowired private SolicitacaoCompraRepository solicitacaoRepository;
    @Autowired private ItemSolicitacaoCompraRepository itemRepository;
    @Autowired private CotacaoCompraRepository cotacaoRepository;
    @Autowired private UnidadeDeSaudeRepository unidadeRepository;
    @Autowired private SetorRepository setorRepository;
    @Autowired private MaterialEstoqueRepository materialRepository;
    @Autowired private MedicamentoRepository medicamentoRepository;
    @Autowired private LoteMaterialRepository loteRepository;
    @Autowired private EstoqueMinimoRepository minimoRepository;
    @Autowired private ControleDeAcesso controleDeAcesso;

    // ── Fornecedor ─────────────────────────────────────────────────────────────────────────────────

    /** Quem pede e quem compra leem; o CPF de pessoa física sai mascarado para quem só pede. {@code busca} olha nome e documento. */
    @Transactional(readOnly = true)
    public List<ComprasDtos.Fornecedor> fornecedores(String busca, Boolean ativos) {
        controleDeAcesso.exigirAlguma(SOLICITAR, GERENCIAR);
        String termo = busca == null ? "" : busca.trim().toLowerCase(Locale.ROOT);
        String digitos = termo.replaceAll("\\D", "");
        Map<UUID, List<CertidaoFornecedor>> certidoes = certidaoRepository.findTodas().stream().collect(Collectors.groupingBy(c -> c.getFornecedor().getUuid()));
        Map<UUID, List<SancaoFornecedor>> sancoes = sancaoRepository.findTodas().stream().collect(Collectors.groupingBy(s -> s.getFornecedor().getUuid()));
        return fornecedorRepository.findAllByOrderByRazaoSocialAsc().stream()
                .filter(f -> ativos == null || f.isAtivo() == ativos)
                .filter(f -> termo.isEmpty() || f.getRazaoSocial().toLowerCase(Locale.ROOT).contains(termo)
                        || (f.getNomeFantasia() != null && f.getNomeFantasia().toLowerCase(Locale.ROOT).contains(termo))
                        || (!digitos.isEmpty() && f.getDocumento().contains(digitos)))
                .map(f -> dto(f, certidoes.getOrDefault(f.getUuid(), List.of()), sancoes.getOrDefault(f.getUuid(), List.of()))).toList();
    }

    @Transactional(readOnly = true)
    public ComprasDtos.Fornecedor fornecedor(UUID id) {
        controleDeAcesso.exigirAlguma(SOLICITAR, GERENCIAR);
        return dto(fornecedorOu404(id));
    }

    /** CPF ou CNPJ válido (400) e único (409); nasce ativo. */
    @Transactional
    public ComprasDtos.Fornecedor cadastrarFornecedor(FornecedorRequestDto dto) {
        controleDeAcesso.exigirAlguma(GERENCIAR);
        String documento = VigilanciaSanitariaService.documento(dto.getDocumento());
        if (fornecedorRepository.findByDocumento(documento).isPresent()) {
            throw new ResourceConflictException("Já existe fornecedor com este " + (documento.length() == 14 ? "CNPJ." : "CPF."));
        }
        Fornecedor f = new Fornecedor();
        f.setDocumento(documento);
        f.setPessoaJuridica(documento.length() == 14);
        preencher(f, dto);
        f.setAtivo(true);
        f.setRegistradoEm(Instant.now());
        f.setRegistradoPorCpf(UsuarioAutenticado.cpf());
        fornecedorRepository.save(f);
        ContextoAuditoria.registro(f.getUuid());
        return dto(f);
    }

    /** O documento não muda (422); o resto, sim, inclusive desativar. */
    @Transactional
    public ComprasDtos.Fornecedor atualizarFornecedor(UUID id, FornecedorRequestDto dto) {
        controleDeAcesso.exigirAlguma(GERENCIAR);
        Fornecedor f = fornecedorOu404(id);
        if (!dto.getDocumento().replaceAll("\\D", "").equals(f.getDocumento())) {
            throw new ResourceUnprocessableEntityException("O CPF ou o CNPJ não muda; cadastre outro fornecedor.");
        }
        preencher(f, dto);
        if (dto.getAtivo() != null) {
            f.setAtivo(dto.getAtivo());
        }
        fornecedorRepository.save(f);
        ContextoAuditoria.registro(f.getUuid());
        return dto(f);
    }

    /** Emitida não futura e validade a partir da emissão (400). A mais nova de cada tipo é a que vale. */
    @Transactional
    public ComprasDtos.Fornecedor registrarCertidao(UUID id, CertidaoFornecedorRequestDto dto) {
        controleDeAcesso.exigirAlguma(GERENCIAR);
        Fornecedor f = fornecedorOu404(id);
        if (dto.getEmitidaEm().isAfter(hoje())) {
            throw new ResourceBadRequestException("A emissão não pode ser no futuro.");
        }
        if (dto.getValidaAte().isBefore(dto.getEmitidaEm())) {
            throw new ResourceBadRequestException("A validade não pode ser antes da emissão.");
        }
        if (!f.isPessoaJuridica() && dto.getTipo() == TipoCertidaoFornecedor.FGTS) {
            throw new ResourceBadRequestException("O certificado do FGTS é da pessoa jurídica.");
        }
        CertidaoFornecedor c = new CertidaoFornecedor(null, f, dto.getTipo(), limpo(dto.getNumero()), dto.getEmitidaEm(), dto.getValidaAte(),
                Instant.now(), UsuarioAutenticado.cpf());
        certidaoRepository.save(c);
        ContextoAuditoria.registro(f.getUuid());
        ContextoAuditoria.detalhe("Certidão " + dto.getTipo() + " válida até " + dto.getValidaAte());
        return dto(f);
    }

    /**
     * Sanção (art. 156): a advertência e a multa não têm fim (400 se vier); a multa pede o valor (400). O impedimento vai até 3 anos e a
     * inidoneidade de 3 a 6 (400), os dois com o fim.
     */
    @Transactional
    public ComprasDtos.Fornecedor registrarSancao(UUID id, SancaoFornecedorRequestDto dto) {
        controleDeAcesso.exigirAlguma(GERENCIAR);
        Fornecedor f = fornecedorOu404(id);
        boolean impede = IMPEDEM.contains(dto.getTipo());
        if (impede != (dto.getFim() != null)) {
            throw new ResourceBadRequestException("O impedimento e a inidoneidade pedem o fim; a advertência e a multa, não.");
        }
        if ((dto.getTipo() == TipoSancaoFornecedor.MULTA) != (dto.getValor() != null)) {
            throw new ResourceBadRequestException("A multa pede o valor, e só ela.");
        }
        if (impede) {
            if (!dto.getFim().isAfter(dto.getInicio())) {
                throw new ResourceBadRequestException("O fim é depois do início.");
            }
            LocalDate tres = dto.getInicio().plusYears(3);
            if (dto.getTipo() == TipoSancaoFornecedor.IMPEDIMENTO && dto.getFim().isAfter(tres)) {
                throw new ResourceBadRequestException("O impedimento de licitar e contratar vai até 3 anos (art. 156, § 4º).");
            }
            if (dto.getTipo() == TipoSancaoFornecedor.INIDONEIDADE && (dto.getFim().isBefore(tres) || dto.getFim().isAfter(dto.getInicio().plusYears(6)))) {
                throw new ResourceBadRequestException("A declaração de inidoneidade vai de 3 a 6 anos (art. 156, § 5º).");
            }
        }
        SancaoFornecedor s = new SancaoFornecedor(null, f, dto.getTipo(), dto.getInicio(), dto.getFim(), dto.getValor(), dto.getMotivo().trim(),
                limpo(dto.getProcesso()), null, null, Instant.now(), UsuarioAutenticado.cpf());
        sancaoRepository.save(s);
        ContextoAuditoria.registro(f.getUuid());
        ContextoAuditoria.detalhe("Sanção " + dto.getTipo());
        return dto(f);
    }

    /** Revoga a sanção (reabilitação, art. 163, ou decisão judicial), com o motivo; a revogada fica no histórico (422 se já revogada). */
    @Transactional
    public ComprasDtos.Fornecedor revogarSancao(UUID sancaoId, String motivo) {
        controleDeAcesso.exigirAlguma(GERENCIAR);
        SancaoFornecedor s = sancaoRepository.findById(sancaoId).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar a sanção " + sancaoId + " em nossos registros."));
        if (s.getRevogadaEm() != null) {
            throw new ResourceUnprocessableEntityException("A sanção já foi revogada.");
        }
        s.setRevogadaEm(hoje());
        s.setMotivoRevogacao(motivo.trim());
        sancaoRepository.save(s);
        ContextoAuditoria.registro(s.getFornecedor().getUuid());
        ContextoAuditoria.detalhe("Sanção " + s.getTipo() + " revogada");
        return dto(s.getFornecedor());
    }

    // ── Solicitação de compra ──────────────────────────────────────────────────────────────────────

    /**
     * As solicitações que a pessoa vê: as das unidades em que pede e, para quem compra, as de toda a rede que já saíram do rascunho.
     * {@code unidadeId} e {@code situacao} filtram.
     */
    @Transactional(readOnly = true)
    public List<ComprasDtos.Solicitacao> solicitacoes(UUID unidadeId, SituacaoSolicitacaoCompra situacao) {
        controleDeAcesso.exigirAlguma(SOLICITAR, GERENCIAR);
        Optional<Set<UUID>> pede = controleDeAcesso.unidadesVisiveis(SOLICITAR);
        boolean compra = controleDeAcesso.temAlguma(GERENCIAR);
        List<SolicitacaoCompra> lista = solicitacaoRepository.findTodas().stream()
                .filter(s -> visivel(s, pede, compra))
                .filter(s -> unidadeId == null || s.getUnidade().getUuid().equals(unidadeId))
                .filter(s -> situacao == null || s.getSituacao() == situacao).toList();
        return dtos(lista);
    }

    @Transactional(readOnly = true)
    public ComprasDtos.Solicitacao solicitacao(UUID id) {
        SolicitacaoCompra s = solicitacaoOu404(id);
        controleDeAcesso.exigirAlguma(SOLICITAR, GERENCIAR);
        if (!visivel(s, controleDeAcesso.unidadesVisiveis(SOLICITAR), controleDeAcesso.temAlguma(GERENCIAR))) {
            throw new ResourceForbiddenException("Sem acesso a esta solicitação de compra.");
        }
        return dto(s);
    }

    /** Cria em rascunho, na unidade em que se pede (403); o setor é da unidade (422); os itens conferidos (400/404). */
    @Transactional
    public ComprasDtos.Solicitacao criar(SolicitacaoCompraRequestDto dto) {
        UnidadeDeSaude unidade = unidadeRepository.findById(dto.getUnidadeId()).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar uma unidade de saúde com o id " + dto.getUnidadeId() + " em nossos registros."));
        controleDeAcesso.exigir(SOLICITAR, unidade);
        SolicitacaoCompra s = new SolicitacaoCompra();
        s.setNumero(numero());
        s.setUnidade(unidade);
        s.setSituacao(SituacaoSolicitacaoCompra.RASCUNHO);
        s.setRegistradoEm(Instant.now());
        s.setRegistradoPorCpf(UsuarioAutenticado.cpf());
        preencher(s, dto);
        solicitacaoRepository.save(s);
        gravarItens(s, dto.getItens());
        ContextoAuditoria.registro(s.getUuid());
        return dto(s);
    }

    /** Edita o rascunho ou a devolvida (422 nas outras): troca os itens; a unidade não muda (422). */
    @Transactional
    public ComprasDtos.Solicitacao atualizar(UUID id, SolicitacaoCompraRequestDto dto) {
        SolicitacaoCompra s = paraPedir(id);
        if (!EDITAVEIS.contains(s.getSituacao())) {
            throw new ResourceUnprocessableEntityException("Só o rascunho e a devolvida se editam.");
        }
        if (!s.getUnidade().getUuid().equals(dto.getUnidadeId())) {
            throw new ResourceUnprocessableEntityException("A unidade da solicitação não muda.");
        }
        preencher(s, dto);
        itemRepository.deleteAll(itemRepository.findDasSolicitacoes(List.of(s.getUuid())));
        itemRepository.flush();
        gravarItens(s, dto.getItens());
        solicitacaoRepository.save(s);
        return dto(s);
    }

    /** Envia o rascunho ou a devolvida para a análise do setor de compras (422 nas outras). */
    @Transactional
    public ComprasDtos.Solicitacao enviar(UUID id) {
        SolicitacaoCompra s = paraPedir(id);
        if (!EDITAVEIS.contains(s.getSituacao())) {
            throw new ResourceUnprocessableEntityException("Só o rascunho e a devolvida se enviam.");
        }
        s.setSituacao(SituacaoSolicitacaoCompra.ENVIADA);
        s.setEnviadaEm(Instant.now());
        s.setMotivo(null);
        solicitacaoRepository.save(s);
        ContextoAuditoria.detalhe("Enviada");
        return dto(s);
    }

    /** Analisa a enviada (422 nas outras): aprovar; devolver ou rejeitar pedem o motivo (400). */
    @Transactional
    public ComprasDtos.Solicitacao analisar(UUID id, AnaliseSolicitacaoCompraRequestDto dto) {
        controleDeAcesso.exigirAlguma(GERENCIAR);
        SolicitacaoCompra s = solicitacaoOu404(id);
        ContextoAuditoria.registro(s.getUuid());
        if (s.getSituacao() != SituacaoSolicitacaoCompra.ENVIADA) {
            throw new ResourceUnprocessableEntityException("Só a solicitação enviada se analisa.");
        }
        String motivo = limpo(dto.getMotivo());
        switch (dto.getDecisao()) {
            case APROVAR -> s.setSituacao(SituacaoSolicitacaoCompra.APROVADA);
            case DEVOLVER, REJEITAR -> {
                if (motivo == null) {
                    throw new ResourceBadRequestException("Devolver ou rejeitar pede o motivo.");
                }
                s.setSituacao(dto.getDecisao() == com.edufelizardo.maissaudepublica.models.enuns.DecisaoSolicitacaoCompra.DEVOLVER
                        ? SituacaoSolicitacaoCompra.DEVOLVIDA : SituacaoSolicitacaoCompra.REJEITADA);
            }
        }
        s.setMotivo(motivo);
        s.setAnalisadaEm(Instant.now());
        s.setAnalisadaPorCpf(UsuarioAutenticado.cpf());
        solicitacaoRepository.save(s);
        ContextoAuditoria.detalhe(dto.getDecisao().name());
        return dto(s);
    }

    /**
     * Cancela com o motivo: quem pede, enquanto não aprovada; quem compra, até a pesquisa concluir. Rejeitada, cotada ou cancelada, 422.
     */
    @Transactional
    public ComprasDtos.Solicitacao cancelar(UUID id, String motivo) {
        SolicitacaoCompra s = solicitacaoOu404(id);
        ContextoAuditoria.registro(s.getUuid());
        if (FECHADAS.contains(s.getSituacao())) {
            throw new ResourceUnprocessableEntityException("A solicitação já está encerrada.");
        }
        if (EM_COTACAO.contains(s.getSituacao())) {
            controleDeAcesso.exigirAlguma(GERENCIAR);
        } else if (!controleDeAcesso.temAlguma(GERENCIAR) || s.getSituacao() == SituacaoSolicitacaoCompra.RASCUNHO) {
            controleDeAcesso.exigir(SOLICITAR, s.getUnidade());
        }
        s.setSituacao(SituacaoSolicitacaoCompra.CANCELADA);
        s.setMotivo(motivo.trim());
        solicitacaoRepository.save(s);
        ContextoAuditoria.detalhe("Cancelada");
        return dto(s);
    }

    /** Os materiais abaixo do mínimo na unidade (ES1), fora de solicitação em andamento; repõe até o dobro do mínimo. */
    @Transactional(readOnly = true)
    public List<ComprasDtos.Sugestao> sugestao(UUID unidadeId) {
        UnidadeDeSaude unidade = unidadeRepository.findById(unidadeId).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar uma unidade de saúde com o id " + unidadeId + " em nossos registros."));
        controleDeAcesso.exigir(SOLICITAR, unidade);
        Map<UUID, Integer> saldo = loteRepository.findDaUnidade(unidadeId).stream()
                .collect(Collectors.groupingBy(l -> l.getMaterial().getUuid(), Collectors.summingInt(LoteMaterial::getQuantidade)));
        List<UUID> andamento = solicitacaoRepository.findTodas().stream()
                .filter(s -> s.getUnidade().getUuid().equals(unidadeId) && !FECHADAS.contains(s.getSituacao()))
                .map(SolicitacaoCompra::getUuid).toList();
        Set<UUID> jaPedidos = andamento.isEmpty() ? Set.of() : itemRepository.findDasSolicitacoes(andamento).stream()
                .filter(i -> i.getMaterial() != null).map(i -> i.getMaterial().getUuid()).collect(Collectors.toSet());
        List<ComprasDtos.Sugestao> lista = new ArrayList<>();
        for (EstoqueMinimo e : minimoRepository.findTodos()) {
            MaterialEstoque m = e.getMaterial();
            int total = saldo.getOrDefault(m.getUuid(), 0);
            if (!e.getUnidade().getUuid().equals(unidadeId) || !m.isAtivo() || total >= e.getQuantidadeMinima() || jaPedidos.contains(m.getUuid())) {
                continue;
            }
            lista.add(new ComprasDtos.Sugestao(m.getUuid(), m.getCodigo(), m.getNome(), m.getUnidadeMedida().name(), total, e.getQuantidadeMinima(),
                    e.getQuantidadeMinima() * 2 - total));
        }
        lista.sort(Comparator.comparing(ComprasDtos.Sugestao::nome));
        return lista;
    }

    // ── Pesquisa de preços ─────────────────────────────────────────────────────────────────────────

    /**
     * A cotação de um item da solicitação aprovada ou em cotação (422 nas outras). O fornecedor é obrigatório na fonte fornecedor, e só
     * nela (400), ativo e sem impedimento (422); as outras fontes pedem a referência (400). Data não futura e de até um ano (400).
     */
    @Transactional
    public ComprasDtos.Solicitacao cotar(UUID itemId, CotacaoCompraRequestDto dto) {
        controleDeAcesso.exigirAlguma(GERENCIAR);
        ItemSolicitacaoCompra item = itemRepository.findById(itemId).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar o item " + itemId + " em nossos registros."));
        SolicitacaoCompra s = item.getSolicitacao();
        ContextoAuditoria.registro(s.getUuid());
        if (!EM_COTACAO.contains(s.getSituacao())) {
            throw new ResourceUnprocessableEntityException("Só a solicitação aprovada recebe cotações.");
        }
        boolean doFornecedor = dto.getFonte() == FonteCotacao.FORNECEDOR;
        if (doFornecedor != (dto.getFornecedorId() != null)) {
            throw new ResourceBadRequestException("A fonte fornecedor pede o fornecedor, e só ela.");
        }
        String referencia = limpo(dto.getReferencia());
        if (!doFornecedor && referencia == null) {
            throw new ResourceBadRequestException("Informe a referência: o contrato, o site ou a fonte do preço.");
        }
        LocalDate hoje = hoje();
        if (dto.getCotadoEm().isAfter(hoje) || dto.getCotadoEm().isBefore(hoje.minusYears(1))) {
            throw new ResourceBadRequestException("A cotação é de até um ano atrás e não pode ser no futuro.");
        }
        Fornecedor fornecedor = null;
        if (doFornecedor) {
            fornecedor = fornecedorOu404(dto.getFornecedorId());
            if (!fornecedor.isAtivo()) {
                throw new ResourceUnprocessableEntityException("O fornecedor está inativo.");
            }
            if (impedido(sancaoRepository.findByFornecedor_UuidOrderByInicioDesc(fornecedor.getUuid()), hoje)) {
                throw new ResourceUnprocessableEntityException("O fornecedor está impedido de licitar e contratar.");
            }
        }
        cotacaoRepository.save(new CotacaoCompra(null, item, dto.getFonte(), fornecedor, referencia, dto.getPrecoUnitario(), dto.getCotadoEm(),
                Instant.now(), UsuarioAutenticado.cpf()));
        s.setSituacao(SituacaoSolicitacaoCompra.EM_COTACAO);
        solicitacaoRepository.save(s);
        ContextoAuditoria.detalhe("Cotação do item " + item.getOrdem());
        return dto(s);
    }

    /** Remove a cotação lançada errada, enquanto a pesquisa não conclui (422). */
    @Transactional
    public ComprasDtos.Solicitacao removerCotacao(UUID cotacaoId) {
        controleDeAcesso.exigirAlguma(GERENCIAR);
        CotacaoCompra c = cotacaoRepository.findById(cotacaoId).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar a cotação " + cotacaoId + " em nossos registros."));
        SolicitacaoCompra s = c.getItem().getSolicitacao();
        ContextoAuditoria.registro(s.getUuid());
        if (!EM_COTACAO.contains(s.getSituacao())) {
            throw new ResourceUnprocessableEntityException("A pesquisa já foi concluída.");
        }
        cotacaoRepository.delete(c);
        return dto(s);
    }

    /**
     * Conclui a pesquisa (422 fora da cotação): todo item tem ao menos uma cotação (422); com menos de 3, a justificativa (422). O preço de
     * referência de cada item fica fixado na mediana.
     */
    @Transactional
    public ComprasDtos.Solicitacao concluirPesquisa(UUID id, ConclusaoPesquisaRequestDto dto) {
        controleDeAcesso.exigirAlguma(GERENCIAR);
        SolicitacaoCompra s = solicitacaoOu404(id);
        ContextoAuditoria.registro(s.getUuid());
        if (s.getSituacao() != SituacaoSolicitacaoCompra.EM_COTACAO) {
            throw new ResourceUnprocessableEntityException("Só a solicitação em cotação conclui a pesquisa.");
        }
        Map<UUID, String> justificativas = new HashMap<>();
        if (dto != null && dto.getJustificativas() != null) {
            dto.getJustificativas().forEach(j -> justificativas.put(j.getItemId(), j.getJustificativa().trim()));
        }
        List<ItemSolicitacaoCompra> itens = itemRepository.findDasSolicitacoes(List.of(s.getUuid()));
        Map<UUID, List<CotacaoCompra>> cotacoes = cotacaoRepository.findDosItens(itens.stream().map(ItemSolicitacaoCompra::getUuid).toList()).stream()
                .collect(Collectors.groupingBy(c -> c.getItem().getUuid()));
        for (ItemSolicitacaoCompra i : itens) {
            List<CotacaoCompra> doItem = cotacoes.getOrDefault(i.getUuid(), List.of());
            if (doItem.isEmpty()) {
                throw new ResourceUnprocessableEntityException("O item " + i.getOrdem() + " (" + i.getDescricao() + ") está sem cotação.");
            }
            String justificativa = justificativas.get(i.getUuid());
            if (doItem.size() < MINIMO_DE_COTACOES && justificativa == null) {
                throw new ResourceUnprocessableEntityException("O item " + i.getOrdem() + " (" + i.getDescricao() + ") tem menos de "
                        + MINIMO_DE_COTACOES + " cotações; justifique.");
            }
            i.setJustificativaCotacao(doItem.size() < MINIMO_DE_COTACOES ? justificativa : null);
            i.setPrecoReferencia(mediana(doItem.stream().map(CotacaoCompra::getPrecoUnitario).toList()));
            itemRepository.save(i);
        }
        s.setSituacao(SituacaoSolicitacaoCompra.COTADA);
        s.setPesquisaConcluidaEm(Instant.now());
        solicitacaoRepository.save(s);
        ContextoAuditoria.detalhe("Pesquisa de preços concluída");
        return dto(s);
    }

    // ── Apoio ──────────────────────────────────────────────────────────────────────────────────────

    /** A mediana, com 4 casas; com quantidade par, a média dos dois do meio. */
    static BigDecimal mediana(List<BigDecimal> precos) {
        if (precos.isEmpty()) {
            return null;
        }
        List<BigDecimal> ordenados = precos.stream().sorted().toList();
        int n = ordenados.size();
        BigDecimal m = n % 2 == 1 ? ordenados.get(n / 2) : ordenados.get(n / 2 - 1).add(ordenados.get(n / 2)).divide(BigDecimal.valueOf(2), 4, RoundingMode.HALF_UP);
        return m.setScale(4, RoundingMode.HALF_UP);
    }

    static boolean impedido(List<SancaoFornecedor> sancoes, LocalDate hoje) {
        return sancoes.stream().anyMatch(s -> IMPEDEM.contains(s.getTipo()) && vigente(s, hoje));
    }

    private static boolean vigente(SancaoFornecedor s, LocalDate hoje) {
        return s.getRevogadaEm() == null && !s.getInicio().isAfter(hoje) && (s.getFim() == null || !s.getFim().isBefore(hoje));
    }

    private static boolean visivel(SolicitacaoCompra s, Optional<Set<UUID>> pede, boolean compra) {
        boolean naUnidade = pede.isEmpty() || pede.get().contains(s.getUnidade().getUuid());
        return naUnidade || (compra && s.getSituacao() != SituacaoSolicitacaoCompra.RASCUNHO);
    }

    private SolicitacaoCompra paraPedir(UUID id) {
        SolicitacaoCompra s = solicitacaoOu404(id);
        controleDeAcesso.exigir(SOLICITAR, s.getUnidade());
        ContextoAuditoria.registro(s.getUuid());
        return s;
    }

    private void preencher(SolicitacaoCompra s, SolicitacaoCompraRequestDto dto) {
        Setor setor = null;
        if (dto.getSetorId() != null) {
            setor = setorRepository.findById(dto.getSetorId()).orElseThrow(() -> new ResourceNotFoundException(
                    "Não foi possível encontrar o setor " + dto.getSetorId() + " em nossos registros."));
            if (setor.getUnidade() == null || !setor.getUnidade().getUuid().equals(s.getUnidade().getUuid())) {
                throw new ResourceUnprocessableEntityException("O setor é de outra unidade.");
            }
        }
        s.setSetor(setor);
        s.setJustificativa(dto.getJustificativa().trim());
        s.setPrioridade(dto.getPrioridade() != null ? dto.getPrioridade() : PrioridadeCompra.NORMAL);
    }

    /**
     * O material pede o material ativo do catálogo e herda a unidade de medida; o medicamento pede o medicamento ativo; o serviço pede a
     * descrição e a unidade de medida (400). O mesmo material ou medicamento não se repete (400).
     */
    private void gravarItens(SolicitacaoCompra s, List<SolicitacaoCompraRequestDto.Item> itens) {
        Set<UUID> vistos = new HashSet<>();
        int ordem = 0;
        for (SolicitacaoCompraRequestDto.Item dto : itens) {
            ordem++;
            boolean material = dto.getTipo() == TipoItemCompra.MATERIAL;
            boolean medicamento = dto.getTipo() == TipoItemCompra.MEDICAMENTO;
            if (material != (dto.getMaterialId() != null) || medicamento != (dto.getMedicamentoId() != null)) {
                throw new ResourceBadRequestException("Item " + ordem + ": o material pede o material do catálogo, o medicamento pede o medicamento, e só eles.");
            }
            ItemSolicitacaoCompra i = new ItemSolicitacaoCompra();
            i.setSolicitacao(s);
            i.setOrdem(ordem);
            i.setTipo(dto.getTipo());
            i.setQuantidade(dto.getQuantidade());
            String descricao = limpo(dto.getDescricao());
            String medida = limpo(dto.getUnidadeMedida());
            if (material) {
                MaterialEstoque m = materialRepository.findById(dto.getMaterialId()).orElseThrow(() -> new ResourceNotFoundException(
                        "Não foi possível encontrar o material " + dto.getMaterialId() + " em nossos registros."));
                if (!m.isAtivo()) {
                    throw new ResourceUnprocessableEntityException("O material " + m.getNome() + " está inativo.");
                }
                i.setMaterial(m);
                descricao = descricao != null ? m.getNome() + " — " + descricao : m.getNome();
                medida = m.getUnidadeMedida().name();
            } else if (medicamento) {
                Medicamento m = medicamentoRepository.findById(dto.getMedicamentoId()).orElseThrow(() -> new ResourceNotFoundException(
                        "Não foi possível encontrar o medicamento " + dto.getMedicamentoId() + " em nossos registros."));
                if (!m.isAtivo()) {
                    throw new ResourceUnprocessableEntityException("O medicamento " + m.getNome() + " está inativo.");
                }
                i.setMedicamento(m);
                String nome = m.getNome() + (m.getApresentacao() != null ? " " + m.getApresentacao() : "");
                descricao = descricao != null ? nome + " — " + descricao : nome;
                medida = medida != null ? medida : "UNIDADE";
            } else if (descricao == null || medida == null) {
                throw new ResourceBadRequestException("Item " + ordem + ": o serviço pede a descrição e a unidade de medida.");
            }
            UUID chave = material ? dto.getMaterialId() : dto.getMedicamentoId();
            if (chave != null && !vistos.add(chave)) {
                throw new ResourceBadRequestException("Item " + ordem + ": o mesmo produto já está na solicitação.");
            }
            i.setDescricao(descricao.length() > 500 ? descricao.substring(0, 500) : descricao);
            i.setUnidadeMedida(medida.toUpperCase(Locale.ROOT));
            itemRepository.save(i);
        }
    }

    private void preencher(Fornecedor f, FornecedorRequestDto dto) {
        f.setRazaoSocial(dto.getRazaoSocial().trim());
        f.setNomeFantasia(limpo(dto.getNomeFantasia()));
        f.setEmail(limpo(dto.getEmail()));
        f.setTelefone(limpo(dto.getTelefone()));
        f.setEndereco(limpo(dto.getEndereco()));
    }

    private Fornecedor fornecedorOu404(UUID id) {
        return fornecedorRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar o fornecedor " + id + " em nossos registros."));
    }

    private SolicitacaoCompra solicitacaoOu404(UUID id) {
        return solicitacaoRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar a solicitação de compra " + id + " em nossos registros."));
    }

    private ComprasDtos.Fornecedor dto(Fornecedor f) {
        return dto(f, certidaoRepository.findByFornecedor_UuidOrderByValidaAteDesc(f.getUuid()), sancaoRepository.findByFornecedor_UuidOrderByInicioDesc(f.getUuid()));
    }

    private ComprasDtos.Fornecedor dto(Fornecedor f, List<CertidaoFornecedor> certidoes, List<SancaoFornecedor> sancoes) {
        LocalDate hoje = hoje();
        Set<TipoCertidaoFornecedor> validas = certidoes.stream().filter(c -> !c.getValidaAte().isBefore(hoje)).map(CertidaoFornecedor::getTipo)
                .collect(Collectors.toSet());
        List<TipoCertidaoFornecedor> pendentes = java.util.Arrays.stream(TipoCertidaoFornecedor.values())
                .filter(t -> f.isPessoaJuridica() || t != TipoCertidaoFornecedor.FGTS).filter(t -> !validas.contains(t)).toList();
        boolean impedido = impedido(sancoes, hoje);
        String documento = f.isPessoaJuridica() || controleDeAcesso.temAlguma(GERENCIAR) ? f.getDocumento()
                : "***." + f.getDocumento().substring(3, 6) + "." + f.getDocumento().substring(6, 9) + "-**";
        return new ComprasDtos.Fornecedor(f.getUuid(), documento, f.isPessoaJuridica(), f.getRazaoSocial(), f.getNomeFantasia(), f.getEmail(),
                f.getTelefone(), f.getEndereco(), f.isAtivo(), impedido, f.isAtivo() && !impedido && pendentes.isEmpty(), pendentes,
                certidoes.stream().sorted(Comparator.comparing(CertidaoFornecedor::getValidaAte).reversed())
                        .map(c -> new ComprasDtos.Certidao(c.getUuid(), c.getTipo(), c.getNumero(), c.getEmitidaEm(), c.getValidaAte(), c.getValidaAte().isBefore(hoje)))
                        .toList(),
                sancoes.stream().sorted(Comparator.comparing(SancaoFornecedor::getInicio).reversed())
                        .map(s -> new ComprasDtos.Sancao(s.getUuid(), s.getTipo(), s.getInicio(), s.getFim(), s.getValor(), s.getMotivo(), s.getProcesso(),
                                vigente(s, hoje), s.getRevogadaEm(), s.getMotivoRevogacao())).toList());
    }

    private ComprasDtos.Solicitacao dto(SolicitacaoCompra s) {
        return dtos(List.of(s)).get(0);
    }

    private List<ComprasDtos.Solicitacao> dtos(List<SolicitacaoCompra> lista) {
        if (lista.isEmpty()) {
            return List.of();
        }
        Map<UUID, List<ItemSolicitacaoCompra>> itens = itemRepository.findDasSolicitacoes(lista.stream().map(SolicitacaoCompra::getUuid).toList()).stream()
                .collect(Collectors.groupingBy(i -> i.getSolicitacao().getUuid()));
        List<UUID> idsItens = itens.values().stream().flatMap(List::stream).map(ItemSolicitacaoCompra::getUuid).toList();
        Map<UUID, List<CotacaoCompra>> cotacoes = idsItens.isEmpty() ? Map.of() : cotacaoRepository.findDosItens(idsItens).stream()
                .collect(Collectors.groupingBy(c -> c.getItem().getUuid()));
        return lista.stream().map(s -> {
            List<ComprasDtos.Item> doPedido = itens.getOrDefault(s.getUuid(), List.of()).stream()
                    .sorted(Comparator.comparingInt(ItemSolicitacaoCompra::getOrdem)).map(i -> item(i, cotacoes.getOrDefault(i.getUuid(), List.of()))).toList();
            BigDecimal total = doPedido.stream().map(ComprasDtos.Item::valorEstimado).filter(java.util.Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add);
            boolean temValor = doPedido.stream().anyMatch(i -> i.valorEstimado() != null);
            return new ComprasDtos.Solicitacao(s.getUuid(), s.getNumero(), s.getUnidade().getUuid(), s.getUnidade().getNome(),
                    s.getSetor() != null ? s.getSetor().getUuid() : null, s.getSetor() != null ? s.getSetor().getNome() : null, s.getJustificativa(),
                    s.getPrioridade(), s.getSituacao(), s.getMotivo(), doPedido, temValor ? total.setScale(2, RoundingMode.HALF_UP) : null, s.getRegistradoEm(),
                    s.getEnviadaEm(), s.getAnalisadaEm(), s.getPesquisaConcluidaEm());
        }).toList();
    }

    private static ComprasDtos.Item item(ItemSolicitacaoCompra i, List<CotacaoCompra> cotacoes) {
        BigDecimal mediana = mediana(cotacoes.stream().map(CotacaoCompra::getPrecoUnitario).toList());
        BigDecimal preco = i.getPrecoReferencia() != null ? i.getPrecoReferencia() : mediana;
        return new ComprasDtos.Item(i.getUuid(), i.getOrdem(), i.getTipo(), i.getMaterial() != null ? i.getMaterial().getUuid() : null,
                i.getMedicamento() != null ? i.getMedicamento().getUuid() : null, i.getDescricao(), i.getUnidadeMedida(), i.getQuantidade(),
                cotacoes.stream().map(c -> new ComprasDtos.Cotacao(c.getUuid(), c.getFonte(), c.getFornecedor() != null ? c.getFornecedor().getUuid() : null,
                        c.getFornecedor() != null ? c.getFornecedor().getRazaoSocial() : null, c.getReferencia(), c.getPrecoUnitario(), c.getCotadoEm())).toList(),
                mediana, i.getPrecoReferencia(), preco == null ? null : preco.multiply(BigDecimal.valueOf(i.getQuantidade())).setScale(2, RoundingMode.HALF_UP),
                i.getJustificativaCotacao());
    }

    private String numero() {
        String prefixo = hoje().getYear() + "/";
        return prefixo + String.format(Locale.ROOT, "%06d", solicitacaoRepository.countByNumeroStartingWith(prefixo) + 1);
    }

    private static String limpo(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }

    private static LocalDate hoje() {
        return LocalDate.now(ZONA);
    }
}
