package com.edufelizardo.maissaudepublica.services.version1;

import com.edufelizardo.maissaudepublica.exceptions.ResourceNotFoundException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceUnprocessableEntityException;
import com.edufelizardo.maissaudepublica.models.Lote;
import com.edufelizardo.maissaudepublica.models.Medicamento;
import com.edufelizardo.maissaudepublica.models.Profissional;
import com.edufelizardo.maissaudepublica.models.UnidadeDeSaude;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.LoteAtualizacaoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.LoteRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.LoteResponseDto;
import com.edufelizardo.maissaudepublica.models.enuns.TipoMovimentacaoFarmacia;
import com.edufelizardo.maissaudepublica.repositories.LoteRepository;
import com.edufelizardo.maissaudepublica.repositories.MedicamentoRepository;
import com.edufelizardo.maissaudepublica.repositories.ProfissionalRepository;
import com.edufelizardo.maissaudepublica.repositories.UnidadeDeSaudeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * CRUD do Lote (Farmácia, segunda fatia do domínio — ver MAPA-DE-DOMINIOS.md #9, ADR-0050).
 * {@code medicamentoId}/{@code unidadeId} são FKs diretas por uuid, mesmo padrão do
 * {@code AtendimentoService} (ver ADR-0041).
 */
@Service
public class LoteService {

    @Autowired
    private LoteRepository loteRepository;

    @Autowired
    private ControleDeAcesso controleDeAcesso;

    @Autowired
    private MedicamentoRepository medicamentoRepository;

    @Autowired
    private UnidadeDeSaudeRepository unidadeDeSaudeRepository;

    @Autowired
    private ProfissionalRepository profissionalRepository;

    @Autowired
    private MovimentacaoFarmaciaService movimentacaoFarmaciaService;

    /** Resultado da entrada de lote: {@code loteNovo} é falso quando a remessa já tinha lote na unidade. */
    public record EntradaDeLote(LoteResponseDto lote, boolean loteNovo) {
    }

    /**
     * Registra a entrada de uma remessa numa unidade. Uma remessa (medicamento, número e validade) tem
     * um único lote por unidade (ADR-0060): se ele já existe, a quantidade entra nele como novo
     * lançamento de ENTRADA; se não, o lote nasce com saldo zero e recebe a ENTRADA (ADR-0057).
     */
    @Transactional
    public EntradaDeLote criar(LoteRequestDto dto) {
        Medicamento medicamento = buscarMedicamentoPorId(dto.getMedicamentoId());
        UnidadeDeSaude unidade = travarUnidade(dto.getUnidadeId());
        controleDeAcesso.exigir("FARMACIA.GERENCIAR_ESTOQUE", unidade);
        Profissional responsavel = buscarProfissionalSeInformado(dto.getProfissionalMatricula());

        UUID existenteId = loteRepository.findIdsDaRemessaNaUnidade(medicamento.getUuid(), unidade.getUuid(),
                dto.getNumeroLote(), dto.getValidade()).stream().findFirst().orElse(null);
        if (existenteId != null) {
            Lote lote = loteRepository.findByIdParaMovimentar(existenteId).orElseThrow();
            if (dto.getQuantidade() > 0) {
                movimentacaoFarmaciaService.lancar(lote, TipoMovimentacaoFarmacia.ENTRADA, dto.getQuantidade(),
                        responsavel, null, null, null);
            }
            return new EntradaDeLote(LoteResponseDto.fromLote(lote), false);
        }

        Lote lote = loteRepository.save(new Lote(medicamento, unidade, dto.getNumeroLote(), dto.getValidade(), 0));
        movimentacaoFarmaciaService.lancar(lote, TipoMovimentacaoFarmacia.ENTRADA, dto.getQuantidade(), responsavel,
                null, null, null);
        return new EntradaDeLote(LoteResponseDto.fromLote(lote), true);
    }

    /**
     * Só número do lote e validade — ver {@link LoteAtualizacaoRequestDto}. A correção não pode
     * transformar o lote em outro lote já existente da mesma remessa na unidade (ADR-0060).
     */
    @Transactional
    public LoteResponseDto atualizar(UUID uuid, LoteAtualizacaoRequestDto dto) {
        Lote atual = buscarEntidadePorId(uuid);
        controleDeAcesso.exigir("FARMACIA.GERENCIAR_ESTOQUE", atual.getUnidade());
        travarUnidade(atual.getUnidade().getUuid());
        Lote lote = loteRepository.findByIdParaMovimentar(uuid).orElseThrow();
        if (lote.getLoteIncorporador() != null) {
            throw new ResourceUnprocessableEntityException(
                    "O lote " + lote.getNumeroLote() + " foi incorporado a outro lote e não pode mais ser corrigido.");
        }
        boolean outroLoteDaMesmaRemessa = loteRepository.findIdsDaRemessaNaUnidade(lote.getMedicamento().getUuid(),
                        lote.getUnidade().getUuid(), dto.getNumeroLote(), dto.getValidade())
                .stream().anyMatch(id -> !id.equals(uuid));
        if (outroLoteDaMesmaRemessa) {
            throw new ResourceUnprocessableEntityException(
                    "Já existe nesta unidade um lote " + dto.getNumeroLote() + " com validade " + dto.getValidade()
                            + " deste medicamento. Registre as quantidades nele em vez de corrigir este lote para a mesma remessa.");
        }
        lote.setNumeroLote(dto.getNumeroLote());
        lote.setValidade(dto.getValidade());
        lote = loteRepository.save(lote);
        return LoteResponseDto.fromLote(lote);
    }

    private Profissional buscarProfissionalSeInformado(String matricula) {
        if (matricula == null || matricula.isBlank()) {
            return null;
        }
        return profissionalRepository.findByMatricula(matricula)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Não foi possível encontrar um profissional com a matrícula " + matricula + " em nossos registros."));
    }

    /** Só lotes ativos — os incorporados a outro lote (ADR-0060) continuam acessíveis pelo id. */
    public List<LoteResponseDto> listar() {
        return controleDeAcesso.filtrar(loteRepository.findByLoteIncorporadorIsNull().stream(), l -> Stream.of(l.getUnidade()),
                        "FARMACIA.CONSULTAR", "FARMACIA.DISPENSAR", "FARMACIA.GERENCIAR_ESTOQUE", "MEDICACAO.ADMINISTRAR")
                .map(LoteResponseDto::fromLote)
                .collect(Collectors.toList());
    }

    public LoteResponseDto buscarPorId(UUID uuid) {
        Lote lote = buscarEntidadePorId(uuid);
        controleDeAcesso.exigirVisivel(lote.getUnidade(), "FARMACIA.CONSULTAR", "FARMACIA.DISPENSAR", "FARMACIA.GERENCIAR_ESTOQUE", "MEDICACAO.ADMINISTRAR");
        return LoteResponseDto.fromLote(lote);
    }

    private Lote buscarEntidadePorId(UUID uuid) {
        return loteRepository.findById(uuid)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Não foi possível encontrar um lote com o id " + uuid + " em nossos registros."));
    }

    private Medicamento buscarMedicamentoPorId(UUID uuid) {
        return medicamentoRepository.findById(uuid)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Não foi possível encontrar um medicamento com o id " + uuid + " em nossos registros."));
    }

    /** Trava a unidade antes de procurar a remessa nela (ADR-0060). */
    private UnidadeDeSaude travarUnidade(UUID uuid) {
        return unidadeDeSaudeRepository.findByIdParaMovimentarEstoque(uuid)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Não foi possível encontrar uma unidade de saúde com o id " + uuid + " em nossos registros."));
    }
}
