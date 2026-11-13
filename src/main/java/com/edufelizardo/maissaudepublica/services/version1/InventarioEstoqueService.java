package com.edufelizardo.maissaudepublica.services.version1;

import com.edufelizardo.maissaudepublica.config.ContextoAuditoria;
import com.edufelizardo.maissaudepublica.config.UsuarioAutenticado;
import com.edufelizardo.maissaudepublica.exceptions.ResourceBadRequestException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceConflictException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceNotFoundException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceUnprocessableEntityException;
import com.edufelizardo.maissaudepublica.models.InventarioEstoque;
import com.edufelizardo.maissaudepublica.models.ItemInventarioEstoque;
import com.edufelizardo.maissaudepublica.models.LoteMaterial;
import com.edufelizardo.maissaudepublica.models.MovimentacaoEstoque;
import com.edufelizardo.maissaudepublica.models.Profissional;
import com.edufelizardo.maissaudepublica.models.UnidadeDeSaude;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.OperacaoEstoqueRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.CentralEstoqueDtos;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoInventarioEstoque;
import com.edufelizardo.maissaudepublica.models.enuns.TipoMovimentacaoEstoque;
import com.edufelizardo.maissaudepublica.repositories.InventarioEstoqueRepository;
import com.edufelizardo.maissaudepublica.repositories.ItemInventarioEstoqueRepository;
import com.edufelizardo.maissaudepublica.repositories.LoteMaterialRepository;
import com.edufelizardo.maissaudepublica.repositories.UnidadeDeSaudeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Inventário da unidade com fechamento (ADR-0189): a abertura congela os lotes com saldo e o saldo do sistema e bloqueia a movimentação
 * da unidade (o consumo lançado pelo sistema segue); a contagem é por lote; o fechamento, com todos contados, lança os ajustes no livro.
 */
@Service
public class InventarioEstoqueService {

    @Autowired private InventarioEstoqueRepository inventarioRepository;
    @Autowired private ItemInventarioEstoqueRepository itemRepository;
    @Autowired private LoteMaterialRepository loteRepository;
    @Autowired private UnidadeDeSaudeRepository unidadeRepository;
    @Autowired private EstoqueService estoqueService;
    @Autowired private ControleDeAcesso controleDeAcesso;

    /** Abre o inventário: um aberto por unidade (409); sem lote com saldo, 422. */
    @Transactional
    public CentralEstoqueDtos.Inventario abrir(OperacaoEstoqueRequestDto dto) {
        if (dto.getUnidadeId() == null) {
            throw new ResourceBadRequestException("Informe a unidade.");
        }
        UnidadeDeSaude u = unidadeRepository.findById(dto.getUnidadeId()).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar a unidade de saúde " + dto.getUnidadeId() + " em nossos registros."));
        controleDeAcesso.exigir(EstoqueService.MOVIMENTAR, u);
        if (inventarioRepository.existsByUnidade_UuidAndSituacao(u.getUuid(), SituacaoInventarioEstoque.ABERTO)) {
            throw new ResourceConflictException("A unidade já tem um inventário aberto.");
        }
        List<LoteMaterial> lotes = loteRepository.findDaUnidade(u.getUuid()).stream().filter(l -> l.getQuantidade() > 0).toList();
        if (lotes.isEmpty()) {
            throw new ResourceUnprocessableEntityException("A unidade não tem lote com saldo para inventariar.");
        }
        InventarioEstoque i = new InventarioEstoque();
        i.setUnidade(u);
        i.setSituacao(SituacaoInventarioEstoque.ABERTO);
        i.setAbertoEm(Instant.now());
        i.setAbertoPor(estoqueService.profissionalPorMatricula(dto.getProfissionalMatricula()));
        i.setObservacao(texto(dto.getObservacao()));
        i.setRegistradoPorCpf(UsuarioAutenticado.cpf());
        InventarioEstoque salvo = inventarioRepository.save(i);
        for (LoteMaterial l : lotes) {
            ItemInventarioEstoque item = new ItemInventarioEstoque();
            item.setInventario(salvo);
            item.setLote(l);
            item.setSaldoSistema(l.getQuantidade());
            itemRepository.save(item);
        }
        return detalhe(salvo);
    }

    /** Registra a contagem de um ou mais lotes (pode corrigir a anterior enquanto aberto). */
    @Transactional
    public CentralEstoqueDtos.Inventario contar(UUID uuid, OperacaoEstoqueRequestDto dto) {
        InventarioEstoque i = travarAberto(uuid);
        Profissional quem = estoqueService.profissionalPorMatricula(dto.getProfissionalMatricula());
        Map<UUID, ItemInventarioEstoque> itens = itemRepository.findByInventario_Uuid(i.getUuid()).stream()
                .collect(Collectors.toMap(ItemInventarioEstoque::getUuid, Function.identity()));
        if (dto.getItens() == null || dto.getItens().isEmpty()) {
            throw new ResourceBadRequestException("Informe ao menos uma contagem.");
        }
        Instant agora = Instant.now();
        for (OperacaoEstoqueRequestDto.Item c : dto.getItens()) {
            ItemInventarioEstoque item = c.getItemId() != null ? itens.get(c.getItemId()) : null;
            if (item == null || c.getQuantidade() == null) {
                throw new ResourceBadRequestException("Cada contagem pede o item do inventário e a quantidade.");
            }
            item.setQuantidadeContada(c.getQuantidade());
            item.setContadoEm(agora);
            item.setContadoPor(quem);
            itemRepository.save(item);
        }
        return detalhe(i);
    }

    /** Fecha: com todos os lotes contados (422), lança o ajuste de cada diferença no livro. */
    @Transactional
    public CentralEstoqueDtos.Inventario fechar(UUID uuid, OperacaoEstoqueRequestDto dto) {
        InventarioEstoque i = travarAberto(uuid);
        List<ItemInventarioEstoque> itens = itemRepository.findByInventario_Uuid(i.getUuid());
        long faltam = itens.stream().filter(x -> x.getQuantidadeContada() == null).count();
        if (faltam > 0) {
            throw new ResourceUnprocessableEntityException("Faltam " + faltam + " lote(s) sem contagem.");
        }
        Profissional quem = estoqueService.profissionalPorMatricula(dto.getProfissionalMatricula());
        String ref = "Inventário de " + i.getAbertoEm().toString().substring(0, 10) + " (" + i.getUuid().toString().substring(0, 8) + ")";
        for (ItemInventarioEstoque x : itens) {
            int diferenca = x.getQuantidadeContada() - x.getSaldoSistema();
            if (diferenca == 0) {
                continue;
            }
            LoteMaterial l = loteRepository.findByIdParaAtualizar(x.getLote().getUuid()).orElseThrow();
            int variacao = Math.max(diferenca, -l.getQuantidade());
            if (variacao == 0) {
                continue;
            }
            MovimentacaoEstoque m = estoqueService.lancar(l, TipoMovimentacaoEstoque.AJUSTE_INVENTARIO, variacao, quem, true);
            m.setJustificativa(ref + ": contado " + x.getQuantidadeContada() + ", sistema " + x.getSaldoSistema() + ".");
            estoqueService.salvar(m);
        }
        i.setSituacao(SituacaoInventarioEstoque.FECHADO);
        i.setEncerradoEm(Instant.now());
        i.setEncerradoPor(quem);
        return detalhe(inventarioRepository.save(i));
    }

    /** Cancela o inventário aberto, com motivo; nada é ajustado e a movimentação volta. */
    @Transactional
    public CentralEstoqueDtos.Inventario cancelar(UUID uuid, OperacaoEstoqueRequestDto dto) {
        InventarioEstoque i = travarAberto(uuid);
        String motivo = texto(dto.getMotivo());
        if (motivo == null) {
            throw new ResourceBadRequestException("Informe o motivo.");
        }
        i.setSituacao(SituacaoInventarioEstoque.CANCELADO);
        i.setEncerradoEm(Instant.now());
        i.setEncerradoPor(estoqueService.profissionalPorMatricula(dto.getProfissionalMatricula()));
        i.setObservacao(motivo);
        return detalhe(inventarioRepository.save(i));
    }

    @Transactional(readOnly = true)
    public List<CentralEstoqueDtos.Inventario> listar(UUID unidadeId) {
        UnidadeDeSaude u = unidadeRepository.findById(unidadeId).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar a unidade de saúde " + unidadeId + " em nossos registros."));
        controleDeAcesso.exigirVisivel(u, EstoqueService.CONSULTAR, EstoqueService.MOVIMENTAR, EstoqueService.GERENCIAR);
        return inventarioRepository.findByUnidade_UuidOrderByAbertoEmDesc(unidadeId).stream().map(i -> dto(i, itemRepository.findByInventario_Uuid(i.getUuid()), false)).toList();
    }

    @Transactional(readOnly = true)
    public CentralEstoqueDtos.Inventario buscar(UUID uuid) {
        InventarioEstoque i = inventarioRepository.findById(uuid).orElseThrow(() -> naoEncontrado(uuid));
        controleDeAcesso.exigirVisivel(i.getUnidade(), EstoqueService.CONSULTAR, EstoqueService.MOVIMENTAR, EstoqueService.GERENCIAR);
        return detalhe(i);
    }

    private InventarioEstoque travarAberto(UUID uuid) {
        InventarioEstoque i = inventarioRepository.findByIdParaAtualizar(uuid).orElseThrow(() -> naoEncontrado(uuid));
        controleDeAcesso.exigir(EstoqueService.MOVIMENTAR, i.getUnidade());
        if (i.getSituacao() != SituacaoInventarioEstoque.ABERTO) {
            throw new ResourceUnprocessableEntityException("O inventário está " + i.getSituacao() + ".");
        }
        return i;
    }

    private CentralEstoqueDtos.Inventario detalhe(InventarioEstoque i) {
        ContextoAuditoria.unidade(i.getUnidade());
        ContextoAuditoria.registro(i.getUuid());
        return dto(i, itemRepository.findByInventario_Uuid(i.getUuid()), true);
    }

    private static CentralEstoqueDtos.Inventario dto(InventarioEstoque i, List<ItemInventarioEstoque> itens, boolean comItens) {
        int contados = (int) itens.stream().filter(x -> x.getQuantidadeContada() != null).count();
        int comDiferenca = (int) itens.stream().filter(x -> x.getQuantidadeContada() != null && !x.getQuantidadeContada().equals(x.getSaldoSistema())).count();
        List<CentralEstoqueDtos.ItemInventario> lista = !comItens ? List.of() : itens.stream()
                .sorted(Comparator.comparing((ItemInventarioEstoque x) -> x.getLote().getMaterial().getNome(), String.CASE_INSENSITIVE_ORDER)
                        .thenComparing(x -> x.getLote().getValidade(), Comparator.nullsLast(Comparator.naturalOrder())))
                .map(x -> new CentralEstoqueDtos.ItemInventario(x.getUuid(), x.getLote().getUuid(), x.getLote().getMaterial().getUuid(),
                        x.getLote().getMaterial().getCodigo(), x.getLote().getMaterial().getNome(), x.getLote().getNumeroLote(), x.getLote().getValidade(),
                        x.getSaldoSistema(), x.getQuantidadeContada(), x.getQuantidadeContada() != null ? x.getQuantidadeContada() - x.getSaldoSistema() : null,
                        x.getContadoPor() != null ? x.getContadoPor().getNome() : null)).toList();
        return new CentralEstoqueDtos.Inventario(i.getUuid(), i.getUnidade().getUuid(), i.getUnidade().getNome(), i.getSituacao(), i.getAbertoEm(),
                i.getAbertoPor().getNome(), i.getEncerradoEm(), i.getEncerradoPor() != null ? i.getEncerradoPor().getNome() : null, i.getObservacao(),
                itens.size(), contados, comDiferenca, lista);
    }

    private static String texto(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }

    private static ResourceNotFoundException naoEncontrado(UUID uuid) {
        return new ResourceNotFoundException("Não foi possível encontrar o inventário " + uuid + " em nossos registros.");
    }
}
