package com.edufelizardo.maissaudepublica.services.version1;

import com.edufelizardo.maissaudepublica.config.ContextoAuditoria;
import com.edufelizardo.maissaudepublica.config.UsuarioAutenticado;
import com.edufelizardo.maissaudepublica.exceptions.ResourceBadRequestException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceNotFoundException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceUnprocessableEntityException;
import com.edufelizardo.maissaudepublica.models.ItemRequisicaoEstoque;
import com.edufelizardo.maissaudepublica.models.ItemTransferenciaEstoque;
import com.edufelizardo.maissaudepublica.models.LoteMaterial;
import com.edufelizardo.maissaudepublica.models.MaterialEstoque;
import com.edufelizardo.maissaudepublica.models.MovimentacaoEstoque;
import com.edufelizardo.maissaudepublica.models.Profissional;
import com.edufelizardo.maissaudepublica.models.RequisicaoEstoque;
import com.edufelizardo.maissaudepublica.models.TransferenciaEstoque;
import com.edufelizardo.maissaudepublica.models.UnidadeDeSaude;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.OperacaoEstoqueRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.CentralEstoqueDtos;
import com.edufelizardo.maissaudepublica.models.enuns.MotivoDivergenciaEstoque;
import com.edufelizardo.maissaudepublica.models.enuns.MotivoPerdaEstoque;
import com.edufelizardo.maissaudepublica.models.enuns.PrioridadeRequisicaoEstoque;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoRequisicaoEstoque;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoTransferenciaEstoque;
import com.edufelizardo.maissaudepublica.models.enuns.TipoMovimentacaoEstoque;
import com.edufelizardo.maissaudepublica.repositories.ItemRequisicaoEstoqueRepository;
import com.edufelizardo.maissaudepublica.repositories.ItemTransferenciaEstoqueRepository;
import com.edufelizardo.maissaudepublica.repositories.LoteMaterialRepository;
import com.edufelizardo.maissaudepublica.repositories.MaterialEstoqueRepository;
import com.edufelizardo.maissaudepublica.repositories.MovimentacaoEstoqueRepository;
import com.edufelizardo.maissaudepublica.repositories.RequisicaoEstoqueRepository;
import com.edufelizardo.maissaudepublica.repositories.TransferenciaEstoqueRepository;
import com.edufelizardo.maissaudepublica.repositories.UnidadeCapacidadeAssistencialRepository;
import com.edufelizardo.maissaudepublica.repositories.UnidadeDeSaudeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Almoxarifado central (ADR-0189): a unidade com a capacidade {@value #CAPACIDADE_CENTRAL} atende as requisições das unidades; o
 * atendimento vira uma transferência em duas etapas (envio pelos lotes que vencem primeiro e recebimento conferido, com a divergência
 * voltando à origem ou baixada como perda). A transferência também pode ser avulsa, entre duas unidades quaisquer.
 */
@Service
public class CentralEstoqueService {

    public static final String CAPACIDADE_CENTRAL = "CENTRAL_ABASTECIMENTO";
    public static final String REQUISITAR = "ESTOQUE.REQUISITAR";
    public static final String ATENDER = "ESTOQUE.ATENDER";
    private static final String[] VER_UNIDADE = {EstoqueService.CONSULTAR, EstoqueService.MOVIMENTAR, EstoqueService.GERENCIAR, REQUISITAR, ATENDER};

    @Autowired private RequisicaoEstoqueRepository requisicaoRepository;
    @Autowired private ItemRequisicaoEstoqueRepository itemRequisicaoRepository;
    @Autowired private TransferenciaEstoqueRepository transferenciaRepository;
    @Autowired private ItemTransferenciaEstoqueRepository itemTransferenciaRepository;
    @Autowired private LoteMaterialRepository loteRepository;
    @Autowired private MaterialEstoqueRepository materialRepository;
    @Autowired private MovimentacaoEstoqueRepository movimentacaoRepository;
    @Autowired private UnidadeDeSaudeRepository unidadeRepository;
    @Autowired private UnidadeCapacidadeAssistencialRepository capacidadeRepository;
    @Autowired private EstoqueService estoqueService;
    @Autowired private ControleDeAcesso controleDeAcesso;

    // ── Centrais ──────────────────────────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<CentralEstoqueDtos.Central> centrais() {
        return capacidadeRepository.findVigentesComCapacidade().stream()
                .filter(a -> CAPACIDADE_CENTRAL.equals(a.getCapacidade().getCodigo()))
                .map(a -> a.getUnidade()).filter(UnidadeDeSaude::isAtivo).distinct()
                .map(u -> new CentralEstoqueDtos.Central(u.getUuid(), u.getNome()))
                .sorted(Comparator.comparing(CentralEstoqueDtos.Central::nome, String.CASE_INSENSITIVE_ORDER)).toList();
    }

    // ── Requisição ────────────────────────────────────────────────────────────────────────────────

    /** Rascunho da requisição da unidade à central (400 sem itens ou com material repetido; 422 se não for central ou for a própria). */
    @Transactional
    public CentralEstoqueDtos.Requisicao criarRequisicao(OperacaoEstoqueRequestDto dto) {
        if (dto.getUnidadeId() == null || dto.getCentralId() == null) {
            throw new ResourceBadRequestException("Informe a unidade e a central.");
        }
        UnidadeDeSaude u = unidade(dto.getUnidadeId());
        controleDeAcesso.exigir(REQUISITAR, u);
        RequisicaoEstoque r = new RequisicaoEstoque();
        r.setUnidade(u);
        r.setSituacao(SituacaoRequisicaoEstoque.RASCUNHO);
        r.setSolicitante(estoqueService.profissionalPorMatricula(dto.getProfissionalMatricula()));
        r.setCriadaEm(Instant.now());
        r.setRegistradoPorCpf(UsuarioAutenticado.cpf());
        preencher(r, dto);
        r = requisicaoRepository.save(r);
        trocarItens(r, dto);
        return requisicao(r);
    }

    /** Muda o rascunho: central, prioridade, observação e itens. */
    @Transactional
    public CentralEstoqueDtos.Requisicao atualizarRequisicao(UUID uuid, OperacaoEstoqueRequestDto dto) {
        RequisicaoEstoque r = travarRequisicao(uuid);
        controleDeAcesso.exigir(REQUISITAR, r.getUnidade());
        exigirSituacao(r, SituacaoRequisicaoEstoque.RASCUNHO);
        if (dto.getCentralId() == null) {
            throw new ResourceBadRequestException("Informe a central.");
        }
        preencher(r, dto);
        r = requisicaoRepository.save(r);
        itemRequisicaoRepository.deleteAll(itemRequisicaoRepository.findByRequisicao_Uuid(r.getUuid()));
        itemRequisicaoRepository.flush();
        trocarItens(r, dto);
        return requisicao(r);
    }

    @Transactional
    public CentralEstoqueDtos.Requisicao enviarRequisicao(UUID uuid, OperacaoEstoqueRequestDto dto) {
        RequisicaoEstoque r = travarRequisicao(uuid);
        controleDeAcesso.exigir(REQUISITAR, r.getUnidade());
        exigirSituacao(r, SituacaoRequisicaoEstoque.RASCUNHO);
        r.setSituacao(SituacaoRequisicaoEstoque.ENVIADA);
        r.setEnviadaEm(Instant.now());
        return requisicao(requisicaoRepository.save(r));
    }

    /** A unidade cancela o rascunho ou a enviada ainda não atendida, com motivo. */
    @Transactional
    public CentralEstoqueDtos.Requisicao cancelarRequisicao(UUID uuid, OperacaoEstoqueRequestDto dto) {
        RequisicaoEstoque r = travarRequisicao(uuid);
        controleDeAcesso.exigir(REQUISITAR, r.getUnidade());
        exigirSituacao(r, SituacaoRequisicaoEstoque.RASCUNHO, SituacaoRequisicaoEstoque.ENVIADA);
        r.setMotivo(exigirMotivo(dto));
        r.setSituacao(SituacaoRequisicaoEstoque.CANCELADA);
        r.setDecididaEm(Instant.now());
        return requisicao(requisicaoRepository.save(r));
    }

    /** A central nega a requisição enviada, com motivo. */
    @Transactional
    public CentralEstoqueDtos.Requisicao negarRequisicao(UUID uuid, OperacaoEstoqueRequestDto dto) {
        RequisicaoEstoque r = travarRequisicao(uuid);
        controleDeAcesso.exigir(ATENDER, r.getCentral());
        exigirSituacao(r, SituacaoRequisicaoEstoque.ENVIADA);
        r.setMotivo(exigirMotivo(dto));
        r.setSituacao(SituacaoRequisicaoEstoque.NEGADA);
        r.setDecididaEm(Instant.now());
        r.setAtendente(estoqueService.profissionalPorMatricula(dto.getProfissionalMatricula()));
        return requisicao(requisicaoRepository.save(r));
    }

    /**
     * A central atende: de zero até o pedido em cada item (400 acima), sem passar do próprio saldo dentro da validade (422). Sai pelos
     * lotes que vencem primeiro e vira uma transferência em trânsito para a unidade.
     */
    @Transactional
    public CentralEstoqueDtos.Requisicao atenderRequisicao(UUID uuid, OperacaoEstoqueRequestDto dto) {
        RequisicaoEstoque r = travarRequisicao(uuid);
        controleDeAcesso.exigir(ATENDER, r.getCentral());
        exigirSituacao(r, SituacaoRequisicaoEstoque.ENVIADA);
        Map<UUID, Integer> atendido = new HashMap<>();
        for (OperacaoEstoqueRequestDto.Item i : dto.getItens()) {
            if (i.getMaterialId() == null || i.getQuantidade() == null) {
                throw new ResourceBadRequestException("Cada item do atendimento pede o material e a quantidade.");
            }
            atendido.put(i.getMaterialId(), i.getQuantidade());
        }
        List<ItemRequisicaoEstoque> itens = itemRequisicaoRepository.findByRequisicao_Uuid(r.getUuid());
        for (ItemRequisicaoEstoque i : itens) {
            int q = atendido.getOrDefault(i.getMaterial().getUuid(), 0);
            if (q > i.getQuantidadePedida()) {
                throw new ResourceBadRequestException("O atendimento de " + i.getMaterial().getNome() + " passa do pedido (" + i.getQuantidadePedida() + ").");
            }
            i.setQuantidadeAtendida(q);
        }
        if (itens.stream().allMatch(i -> i.getQuantidadeAtendida() == 0)) {
            throw new ResourceUnprocessableEntityException("Atenda ao menos um item, ou negue a requisição com o motivo.");
        }
        Profissional quem = estoqueService.profissionalPorMatricula(dto.getProfissionalMatricula());
        TransferenciaEstoque t = novaTransferencia(r.getCentral(), r.getUnidade(), r.getUuid(), texto(dto.getObservacao()), quem);
        LocalDate hoje = LocalDate.now();
        for (ItemRequisicaoEstoque i : itens) {
            if (i.getQuantidadeAtendida() == 0) {
                continue;
            }
            List<LoteMaterial> lotes = loteRepository.findDoMaterialNaUnidadeParaAtualizar(i.getMaterial().getUuid(), r.getCentral().getUuid()).stream()
                    .filter(l -> l.getQuantidade() > 0 && !l.vencido(hoje)).toList();
            int disponivel = lotes.stream().mapToInt(LoteMaterial::getQuantidade).sum();
            if (disponivel < i.getQuantidadeAtendida()) {
                throw new ResourceUnprocessableEntityException("A central tem " + disponivel + " de " + i.getMaterial().getNome()
                        + " dentro da validade; não dá para atender " + i.getQuantidadeAtendida() + ".");
            }
            UUID emergenciaId = r.getEmergencia() != null ? r.getEmergencia().getUuid() : null;
            estoqueService.exigirForaDaReserva(i.getMaterial(), r.getCentral(), lotes, i.getQuantidadeAtendida(), emergenciaId, hoje);
            int falta = i.getQuantidadeAtendida();
            for (LoteMaterial l : lotes) {
                if (falta == 0) {
                    break;
                }
                int tirar = Math.min(falta, l.getQuantidade());
                enviar(t, l, tirar, quem, "Requisição de " + r.getUnidade().getNome());
                falta -= tirar;
            }
            if (emergenciaId != null) {
                estoqueService.abaterReserva(r.getCentral().getUuid(), i.getMaterial().getUuid(), emergenciaId, i.getQuantidadeAtendida());
            }
            itemRequisicaoRepository.save(i);
        }
        r.setSituacao(SituacaoRequisicaoEstoque.ATENDIDA);
        r.setDecididaEm(Instant.now());
        r.setAtendente(quem);
        r.setTransferenciaId(t.getUuid());
        return requisicao(requisicaoRepository.save(r));
    }

    @Transactional(readOnly = true)
    public List<CentralEstoqueDtos.Requisicao> requisicoes(UUID unidadeId, UUID centralId, SituacaoRequisicaoEstoque situacao) {
        controleDeAcesso.exigirAlguma(VER_UNIDADE);
        Optional<Set<UUID>> daUnidade = controleDeAcesso.unidadesVisiveis(VER_UNIDADE);
        Optional<Set<UUID>> daCentral = controleDeAcesso.unidadesVisiveis(ATENDER);
        List<RequisicaoEstoque> lista = requisicaoRepository.findTodas().stream()
                .filter(r -> unidadeId == null || r.getUnidade().getUuid().equals(unidadeId))
                .filter(r -> centralId == null || r.getCentral().getUuid().equals(centralId))
                .filter(r -> situacao == null || r.getSituacao() == situacao)
                // A central não vê o rascunho da unidade.
                .filter(r -> visivel(daUnidade, r.getUnidade())
                        || (r.getSituacao() != SituacaoRequisicaoEstoque.RASCUNHO && visivel(daCentral, r.getCentral())))
                // A requisição urgente da emergência ainda por atender vem no alto da fila (ADR-0191).
                .sorted(Comparator.comparing((RequisicaoEstoque r) -> !(r.getEmergencia() != null && r.getSituacao() == SituacaoRequisicaoEstoque.ENVIADA)))
                .toList();
        Map<UUID, List<ItemRequisicaoEstoque>> itens = lista.isEmpty() ? Map.of()
                : itemRequisicaoRepository.findByRequisicao_UuidIn(lista.stream().map(RequisicaoEstoque::getUuid).toList()).stream()
                .collect(Collectors.groupingBy(i -> i.getRequisicao().getUuid()));
        return lista.stream().map(r -> resumo(r, itens.getOrDefault(r.getUuid(), List.of()))).toList();
    }

    /** O detalhe, com o saldo e o consumo dos últimos 30 dias da unidade e o saldo da central de cada item. */
    @Transactional(readOnly = true)
    public CentralEstoqueDtos.Requisicao buscarRequisicao(UUID uuid) {
        RequisicaoEstoque r = requisicaoRepository.findById(uuid).orElseThrow(() -> naoEncontrada("a requisição", uuid));
        boolean unidade = visivel(controleDeAcesso.unidadesVisiveis(VER_UNIDADE), r.getUnidade());
        boolean central = r.getSituacao() != SituacaoRequisicaoEstoque.RASCUNHO && visivel(controleDeAcesso.unidadesVisiveis(ATENDER), r.getCentral());
        if (!unidade && !central) {
            controleDeAcesso.exigirVisivel(r.getUnidade(), VER_UNIDADE);
        }
        return requisicao(r);
    }

    // ── Transferência ─────────────────────────────────────────────────────────────────────────────

    /** Transferência avulsa entre duas unidades, por lote (dentro da validade e com saldo, 422). */
    @Transactional
    public CentralEstoqueDtos.Transferencia transferir(OperacaoEstoqueRequestDto dto) {
        if (dto.getUnidadeId() == null || dto.getDestinoId() == null) {
            throw new ResourceBadRequestException("Informe a origem e o destino.");
        }
        if (dto.getUnidadeId().equals(dto.getDestinoId())) {
            throw new ResourceUnprocessableEntityException("A origem e o destino são a mesma unidade.");
        }
        if (dto.getItens() == null || dto.getItens().isEmpty()) {
            throw new ResourceBadRequestException("Informe ao menos um lote.");
        }
        UnidadeDeSaude origem = unidade(dto.getUnidadeId());
        UnidadeDeSaude destino = unidade(dto.getDestinoId());
        controleDeAcesso.exigir(EstoqueService.MOVIMENTAR, origem);
        Profissional quem = estoqueService.profissionalPorMatricula(dto.getProfissionalMatricula());
        TransferenciaEstoque t = novaTransferencia(origem, destino, null, texto(dto.getObservacao()), quem);
        LocalDate hoje = LocalDate.now();
        Map<UUID, Integer> porMaterial = new HashMap<>();
        Map<UUID, MaterialEstoque> materiais = new HashMap<>();
        for (OperacaoEstoqueRequestDto.Item i : dto.getItens()) {
            if (i.getLoteId() == null || i.getQuantidade() == null || i.getQuantidade() < 1) {
                throw new ResourceBadRequestException("Cada item pede o lote e a quantidade.");
            }
            LoteMaterial l = loteRepository.findByIdParaAtualizar(i.getLoteId()).orElseThrow(() -> naoEncontrada("o lote", i.getLoteId()));
            if (!l.getUnidade().getUuid().equals(origem.getUuid())) {
                throw new ResourceUnprocessableEntityException("O lote " + l.getNumeroLote() + " não é da unidade de origem.");
            }
            if (l.vencido(hoje)) {
                throw new ResourceUnprocessableEntityException("O lote " + l.getNumeroLote() + " está vencido; registre como perda.");
            }
            if (l.getQuantidade() < i.getQuantidade()) {
                throw new ResourceUnprocessableEntityException("O lote tem " + l.getQuantidade() + "; não dá para transferir " + i.getQuantidade() + ".");
            }
            enviar(t, l, i.getQuantidade(), quem, "Transferência para " + destino.getNome());
            porMaterial.merge(l.getMaterial().getUuid(), i.getQuantidade(), Integer::sum);
            materiais.put(l.getMaterial().getUuid(), l.getMaterial());
        }
        // Depois do envio o saldo já caiu: o que sobrou dentro da validade precisa cobrir o reservado (a avulsa não usa a reserva).
        for (Map.Entry<UUID, Integer> e : porMaterial.entrySet()) {
            MaterialEstoque m = materiais.get(e.getKey());
            int reservado = estoqueService.reservado(origem.getUuid(), m.getUuid(), null);
            int sobra = loteRepository.findDoMaterialNaUnidadeParaAtualizar(m.getUuid(), origem.getUuid()).stream()
                    .filter(l -> !l.vencido(hoje)).mapToInt(LoteMaterial::getQuantidade).sum();
            if (sobra < reservado) {
                throw new ResourceUnprocessableEntityException(reservado + " de " + m.getNome() + " estão reservados para emergência na origem; ficam livres "
                        + Math.max(0, sobra + e.getValue() - reservado) + ", não dá para transferir " + e.getValue() + ".");
            }
        }
        return transferencia(t);
    }

    /**
     * O destino confere item a item (todos, 400 faltando): o que chegou entra no mesmo lote; a diferença pede o motivo (400). Falta e
     * outro voltam ao lote de origem; avaria entra e sai como perda no destino.
     */
    @Transactional
    public CentralEstoqueDtos.Transferencia receber(UUID uuid, OperacaoEstoqueRequestDto dto) {
        TransferenciaEstoque t = transferenciaRepository.findByIdParaAtualizar(uuid).orElseThrow(() -> naoEncontrada("a transferência", uuid));
        controleDeAcesso.exigir(EstoqueService.MOVIMENTAR, t.getDestino());
        if (t.getSituacao() != SituacaoTransferenciaEstoque.EM_TRANSITO) {
            throw new ResourceUnprocessableEntityException("A transferência já foi recebida.");
        }
        Map<UUID, OperacaoEstoqueRequestDto.Item> conferidos = dto.getItens().stream().filter(i -> i.getItemId() != null)
                .collect(Collectors.toMap(OperacaoEstoqueRequestDto.Item::getItemId, Function.identity(), (a, b) -> b));
        List<ItemTransferenciaEstoque> itens = itemTransferenciaRepository.findByTransferencia_Uuid(t.getUuid());
        Profissional quem = estoqueService.profissionalPorMatricula(dto.getProfissionalMatricula());
        String ref = "Transferência de " + t.getOrigem().getNome();
        for (ItemTransferenciaEstoque i : itens) {
            OperacaoEstoqueRequestDto.Item c = conferidos.get(i.getUuid());
            if (c == null || c.getQuantidade() == null) {
                throw new ResourceBadRequestException("Confira todos os itens: falta " + i.getMaterial().getNome() + (i.getNumeroLote() != null ? " lote " + i.getNumeroLote() : "") + ".");
            }
            int recebido = c.getQuantidade();
            if (recebido > i.getQuantidadeEnviada()) {
                throw new ResourceBadRequestException("Chegou mais que o enviado de " + i.getMaterial().getNome() + ".");
            }
            int diferenca = i.getQuantidadeEnviada() - recebido;
            if (diferenca > 0 && c.getMotivoDivergencia() == null) {
                throw new ResourceBadRequestException("Informe o motivo da diferença em " + i.getMaterial().getNome() + ".");
            }
            i.setQuantidadeRecebida(recebido);
            i.setMotivoDivergencia(diferenca > 0 ? c.getMotivoDivergencia() : null);
            i.setObservacao(texto(c.getObservacao()));
            boolean avaria = diferenca > 0 && c.getMotivoDivergencia() == MotivoDivergenciaEstoque.AVARIA;
            int entra = recebido + (avaria ? diferenca : 0);
            if (entra > 0) {
                LoteMaterial destino = estoqueService.loteDeEntrada(i.getMaterial(), t.getDestino(), i.getNumeroLote(), i.getValidade(), i.getCustoUnitario());
                i.setLoteDestino(destino);
                lancar(destino, TipoMovimentacaoEstoque.TRANSFERENCIA_ENTRADA, entra, quem, t, ref, false);
                if (avaria) {
                    MovimentacaoEstoque perda = estoqueService.lancar(destino, TipoMovimentacaoEstoque.PERDA, -diferenca, quem, false);
                    perda.setMotivoPerda(MotivoPerdaEstoque.AVARIA);
                    perda.setJustificativa("Avaria no transporte. " + ref);
                    perda.setTransferenciaId(t.getUuid());
                    estoqueService.salvar(perda);
                }
            }
            if (diferenca > 0 && !avaria) {
                LoteMaterial origem = loteRepository.findByIdParaAtualizar(i.getLoteOrigem().getUuid()).orElseThrow();
                lancar(origem, TipoMovimentacaoEstoque.TRANSFERENCIA_ESTORNO, diferenca, quem, t,
                        "Não chegou a " + t.getDestino().getNome() + " (" + c.getMotivoDivergencia() + ")", true);
            }
            itemTransferenciaRepository.save(i);
        }
        t.setSituacao(SituacaoTransferenciaEstoque.RECEBIDA);
        t.setRecebidaEm(Instant.now());
        t.setRecebidoPor(quem);
        transferenciaRepository.save(t);
        if (t.getRequisicaoId() != null) {
            requisicaoRepository.findByIdParaAtualizar(t.getRequisicaoId()).ifPresent(r -> {
                r.setSituacao(SituacaoRequisicaoEstoque.RECEBIDA);
                r.setRecebidaEm(t.getRecebidaEm());
                requisicaoRepository.save(r);
            });
        }
        return transferencia(t);
    }

    @Transactional(readOnly = true)
    public List<CentralEstoqueDtos.Transferencia> transferencias(UUID unidadeId, SituacaoTransferenciaEstoque situacao) {
        controleDeAcesso.exigirAlguma(VER_UNIDADE);
        Optional<Set<UUID>> visiveis = controleDeAcesso.unidadesVisiveis(VER_UNIDADE);
        List<TransferenciaEstoque> lista = transferenciaRepository.findTodas().stream()
                .filter(t -> unidadeId == null || t.getOrigem().getUuid().equals(unidadeId) || t.getDestino().getUuid().equals(unidadeId))
                .filter(t -> situacao == null || t.getSituacao() == situacao)
                .filter(t -> visivel(visiveis, t.getOrigem()) || visivel(visiveis, t.getDestino())).toList();
        Map<UUID, List<ItemTransferenciaEstoque>> itens = lista.isEmpty() ? Map.of()
                : itemTransferenciaRepository.findByTransferencia_UuidIn(lista.stream().map(TransferenciaEstoque::getUuid).toList()).stream()
                .collect(Collectors.groupingBy(i -> i.getTransferencia().getUuid()));
        return lista.stream().map(t -> transferencia(t, itens.getOrDefault(t.getUuid(), List.of()))).toList();
    }

    @Transactional(readOnly = true)
    public CentralEstoqueDtos.Transferencia buscarTransferencia(UUID uuid) {
        TransferenciaEstoque t = transferenciaRepository.findById(uuid).orElseThrow(() -> naoEncontrada("a transferência", uuid));
        Optional<Set<UUID>> visiveis = controleDeAcesso.unidadesVisiveis(VER_UNIDADE);
        if (!visivel(visiveis, t.getOrigem()) && !visivel(visiveis, t.getDestino())) {
            controleDeAcesso.exigirVisivel(t.getDestino(), VER_UNIDADE);
        }
        return transferencia(t);
    }

    // ── Emergências (ES3, ADR-0191) ───────────────────────────────────────────────────────────────

    /**
     * A requisição urgente que a coordenação da emergência gera para a mobilização de insumo, já enviada à central (não é central, 422;
     * a própria unidade, 422; material inativo, 422). A central atende pelo fluxo de sempre e pode usar a reserva da emergência.
     */
    RequisicaoEstoque requisicaoDaEmergencia(com.edufelizardo.maissaudepublica.models.Emergencia em, UnidadeDeSaude unidade, UnidadeDeSaude central,
                                             MaterialEstoque material, int quantidade, String observacao) {
        if (!material.isAtivo()) {
            throw new ResourceUnprocessableEntityException("O material " + material.getNome() + " está inativo no catálogo.");
        }
        RequisicaoEstoque r = new RequisicaoEstoque();
        r.setUnidade(unidade);
        r.setSituacao(SituacaoRequisicaoEstoque.ENVIADA);
        r.setSolicitante(estoqueService.profissionalPorMatricula(null));
        r.setCriadaEm(Instant.now());
        r.setEnviadaEm(r.getCriadaEm());
        r.setRegistradoPorCpf(UsuarioAutenticado.cpf());
        r.setEmergencia(em);
        OperacaoEstoqueRequestDto dto = new OperacaoEstoqueRequestDto();
        dto.setCentralId(central.getUuid());
        dto.setPrioridade(PrioridadeRequisicaoEstoque.URGENTE);
        dto.setObservacao(observacao);
        preencher(r, dto);
        r = requisicaoRepository.save(r);
        ItemRequisicaoEstoque item = new ItemRequisicaoEstoque();
        item.setRequisicao(r);
        item.setMaterial(material);
        item.setQuantidadePedida(quantidade);
        itemRequisicaoRepository.save(item);
        return r;
    }

    /** A mobilização cancelada pela coordenação leva junto a requisição ainda não atendida (atendida, 422). */
    void cancelarDaEmergencia(RequisicaoEstoque r, String motivo) {
        RequisicaoEstoque atual = travarRequisicao(r.getUuid());
        if (atual.getSituacao() != SituacaoRequisicaoEstoque.ENVIADA) {
            throw new ResourceUnprocessableEntityException("A requisição já está " + atual.getSituacao() + ": a mobilização segue o estoque.");
        }
        atual.setSituacao(SituacaoRequisicaoEstoque.CANCELADA);
        atual.setMotivo(motivo);
        atual.setDecididaEm(Instant.now());
        requisicaoRepository.save(atual);
    }

    TransferenciaEstoque transferenciaPorId(UUID uuid) {
        return transferenciaRepository.findById(uuid).orElseThrow(() -> naoEncontrada("a transferência de estoque", uuid));
    }

    // ── Apoio ─────────────────────────────────────────────────────────────────────────────────────

    private void preencher(RequisicaoEstoque r, OperacaoEstoqueRequestDto dto) {
        UnidadeDeSaude central = unidade(dto.getCentralId());
        if (!capacidadeRepository.existsByUnidade_UuidAndCapacidade_CodigoAndFimIsNull(central.getUuid(), CAPACIDADE_CENTRAL)) {
            throw new ResourceUnprocessableEntityException(central.getNome() + " não é central de abastecimento (capacidade " + CAPACIDADE_CENTRAL + ").");
        }
        if (central.getUuid().equals(r.getUnidade().getUuid())) {
            throw new ResourceUnprocessableEntityException("A unidade não pede à própria central.");
        }
        r.setCentral(central);
        r.setPrioridade(dto.getPrioridade() != null ? dto.getPrioridade() : PrioridadeRequisicaoEstoque.NORMAL);
        r.setObservacao(texto(dto.getObservacao()));
    }

    private void trocarItens(RequisicaoEstoque r, OperacaoEstoqueRequestDto dto) {
        if (dto.getItens() == null || dto.getItens().isEmpty()) {
            throw new ResourceBadRequestException("A requisição precisa de ao menos um material.");
        }
        Set<UUID> vistos = new HashSet<>();
        for (OperacaoEstoqueRequestDto.Item i : dto.getItens()) {
            if (i.getMaterialId() == null || i.getQuantidade() == null || i.getQuantidade() < 1) {
                throw new ResourceBadRequestException("Cada item pede o material e a quantidade (ao menos 1).");
            }
            if (!vistos.add(i.getMaterialId())) {
                throw new ResourceBadRequestException("O mesmo material aparece duas vezes; some as quantidades.");
            }
            MaterialEstoque m = materialRepository.findById(i.getMaterialId()).orElseThrow(() -> naoEncontrada("o material", i.getMaterialId()));
            if (!m.isAtivo()) {
                throw new ResourceUnprocessableEntityException("O material " + m.getNome() + " está inativo no catálogo.");
            }
            ItemRequisicaoEstoque item = new ItemRequisicaoEstoque();
            item.setRequisicao(r);
            item.setMaterial(m);
            item.setQuantidadePedida(i.getQuantidade());
            itemRequisicaoRepository.save(item);
        }
    }

    private TransferenciaEstoque novaTransferencia(UnidadeDeSaude origem, UnidadeDeSaude destino, UUID requisicaoId, String observacao, Profissional quem) {
        TransferenciaEstoque t = new TransferenciaEstoque();
        t.setOrigem(origem);
        t.setDestino(destino);
        t.setRequisicaoId(requisicaoId);
        t.setSituacao(SituacaoTransferenciaEstoque.EM_TRANSITO);
        t.setObservacao(observacao);
        t.setEnviadaEm(Instant.now());
        t.setEnviadoPor(quem);
        t.setRegistradoPorCpf(UsuarioAutenticado.cpf());
        return transferenciaRepository.save(t);
    }

    private void enviar(TransferenciaEstoque t, LoteMaterial l, int quantidade, Profissional quem, String ref) {
        lancar(l, TipoMovimentacaoEstoque.TRANSFERENCIA_SAIDA, -quantidade, quem, t, ref, false);
        ItemTransferenciaEstoque i = new ItemTransferenciaEstoque();
        i.setTransferencia(t);
        i.setMaterial(l.getMaterial());
        i.setLoteOrigem(l);
        i.setNumeroLote(l.getNumeroLote());
        i.setValidade(l.getValidade());
        i.setCustoUnitario(l.getCustoUnitario());
        i.setQuantidadeEnviada(quantidade);
        itemTransferenciaRepository.save(i);
    }

    private void lancar(LoteMaterial l, TipoMovimentacaoEstoque tipo, int variacao, Profissional quem, TransferenciaEstoque t, String ref, boolean livre) {
        MovimentacaoEstoque m = estoqueService.lancar(l, tipo, variacao, quem, livre);
        m.setTransferenciaId(t.getUuid());
        m.setJustificativa(ref.length() > 500 ? ref.substring(0, 500) : ref);
        estoqueService.salvar(m);
    }

    private CentralEstoqueDtos.Requisicao requisicao(RequisicaoEstoque r) {
        ContextoAuditoria.registro(r.getUuid());
        return resumo(r, itemRequisicaoRepository.findByRequisicao_Uuid(r.getUuid()), true);
    }

    private CentralEstoqueDtos.Requisicao resumo(RequisicaoEstoque r, List<ItemRequisicaoEstoque> itens) {
        return resumo(r, itens, false);
    }

    private CentralEstoqueDtos.Requisicao resumo(RequisicaoEstoque r, List<ItemRequisicaoEstoque> itens, boolean comSaldos) {
        Map<UUID, Integer> saldoUnidade = comSaldos ? saldos(r.getUnidade().getUuid()) : Map.of();
        Map<UUID, Integer> saldoCentral = comSaldos ? saldos(r.getCentral().getUuid()) : Map.of();
        Map<UUID, Integer> consumo = comSaldos ? consumo30Dias(r.getUnidade().getUuid()) : Map.of();
        boolean parcial = r.getSituacao() == SituacaoRequisicaoEstoque.ATENDIDA || r.getSituacao() == SituacaoRequisicaoEstoque.RECEBIDA
                ? itens.stream().anyMatch(i -> i.getQuantidadeAtendida() == null || i.getQuantidadeAtendida() < i.getQuantidadePedida()) : false;
        List<CentralEstoqueDtos.ItemRequisicao> lista = itens.stream()
                .sorted(Comparator.comparing(i -> i.getMaterial().getNome(), String.CASE_INSENSITIVE_ORDER))
                .map(i -> new CentralEstoqueDtos.ItemRequisicao(i.getUuid(), i.getMaterial().getUuid(), i.getMaterial().getCodigo(), i.getMaterial().getNome(),
                        i.getMaterial().getUnidadeMedida(), i.getQuantidadePedida(), i.getQuantidadeAtendida(),
                        saldoUnidade.getOrDefault(i.getMaterial().getUuid(), 0), consumo.getOrDefault(i.getMaterial().getUuid(), 0),
                        saldoCentral.getOrDefault(i.getMaterial().getUuid(), 0))).toList();
        return new CentralEstoqueDtos.Requisicao(r.getUuid(), r.getUnidade().getUuid(), r.getUnidade().getNome(), r.getCentral().getUuid(), r.getCentral().getNome(),
                r.getPrioridade(), r.getSituacao(), parcial, r.getObservacao(),
                r.getSolicitante() != null ? r.getSolicitante().getNome() : "Coordenação da emergência", r.getCriadaEm(), r.getEnviadaEm(), r.getDecididaEm(),
                r.getAtendente() != null ? r.getAtendente().getNome() : null, r.getMotivo(), r.getTransferenciaId(), r.getRecebidaEm(), lista,
                r.getEmergencia() != null ? r.getEmergencia().getUuid() : null, r.getEmergencia() != null ? r.getEmergencia().getNome() : null);
    }

    /** Saldo dentro da validade por material na unidade. */
    private Map<UUID, Integer> saldos(UUID unidadeId) {
        LocalDate hoje = LocalDate.now();
        return loteRepository.findDaUnidade(unidadeId).stream().filter(l -> !l.vencido(hoje))
                .collect(Collectors.groupingBy(l -> l.getMaterial().getUuid(), Collectors.summingInt(LoteMaterial::getQuantidade)));
    }

    private Map<UUID, Integer> consumo30Dias(UUID unidadeId) {
        return movimentacaoRepository.findDesde(Instant.now().minus(Duration.ofDays(30))).stream()
                .filter(m -> m.getTipo() == TipoMovimentacaoEstoque.SAIDA_CONSUMO && m.getLote().getUnidade().getUuid().equals(unidadeId))
                .collect(Collectors.groupingBy(m -> m.getLote().getMaterial().getUuid(), Collectors.summingInt(m -> -m.getQuantidade())));
    }

    private CentralEstoqueDtos.Transferencia transferencia(TransferenciaEstoque t) {
        ContextoAuditoria.registro(t.getUuid());
        return transferencia(t, itemTransferenciaRepository.findByTransferencia_Uuid(t.getUuid()));
    }

    private static CentralEstoqueDtos.Transferencia transferencia(TransferenciaEstoque t, List<ItemTransferenciaEstoque> itens) {
        List<CentralEstoqueDtos.ItemTransferencia> lista = itens.stream()
                .sorted(Comparator.comparing((ItemTransferenciaEstoque i) -> i.getMaterial().getNome(), String.CASE_INSENSITIVE_ORDER)
                        .thenComparing(i -> i.getValidade(), Comparator.nullsLast(Comparator.naturalOrder())))
                .map(i -> new CentralEstoqueDtos.ItemTransferencia(i.getUuid(), i.getMaterial().getUuid(), i.getMaterial().getCodigo(), i.getMaterial().getNome(),
                        i.getLoteOrigem().getUuid(), i.getNumeroLote(), i.getValidade(), i.getQuantidadeEnviada(), i.getQuantidadeRecebida(),
                        i.getMotivoDivergencia(), i.getObservacao())).toList();
        boolean divergencia = itens.stream().anyMatch(i -> i.getQuantidadeRecebida() != null && i.getQuantidadeRecebida() < i.getQuantidadeEnviada());
        return new CentralEstoqueDtos.Transferencia(t.getUuid(), t.getOrigem().getUuid(), t.getOrigem().getNome(), t.getDestino().getUuid(), t.getDestino().getNome(),
                t.getRequisicaoId(), t.getSituacao(), t.getObservacao(), t.getEnviadaEm(), t.getEnviadoPor().getNome(), t.getRecebidaEm(),
                t.getRecebidoPor() != null ? t.getRecebidoPor().getNome() : null, divergencia, lista);
    }

    private RequisicaoEstoque travarRequisicao(UUID uuid) {
        return requisicaoRepository.findByIdParaAtualizar(uuid).orElseThrow(() -> naoEncontrada("a requisição", uuid));
    }

    private static void exigirSituacao(RequisicaoEstoque r, SituacaoRequisicaoEstoque... permitidas) {
        if (Stream.of(permitidas).noneMatch(s -> s == r.getSituacao())) {
            throw new ResourceUnprocessableEntityException("A requisição está " + r.getSituacao() + ".");
        }
    }

    private static String exigirMotivo(OperacaoEstoqueRequestDto dto) {
        String m = texto(dto.getMotivo());
        if (m == null) {
            throw new ResourceBadRequestException("Informe o motivo.");
        }
        return m;
    }

    private UnidadeDeSaude unidade(UUID id) {
        return unidadeRepository.findById(id).orElseThrow(() -> naoEncontrada("a unidade de saúde", id));
    }

    private static boolean visivel(Optional<Set<UUID>> visiveis, UnidadeDeSaude u) {
        return u != null && visiveis.map(s -> s.contains(u.getUuid())).orElse(true);
    }

    private static String texto(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }

    private static ResourceNotFoundException naoEncontrada(String o, UUID uuid) {
        return new ResourceNotFoundException("Não foi possível encontrar " + o + " " + uuid + " em nossos registros.");
    }

}
