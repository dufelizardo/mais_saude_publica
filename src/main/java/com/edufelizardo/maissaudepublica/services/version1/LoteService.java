package com.edufelizardo.maissaudepublica.services.version1;

import com.edufelizardo.maissaudepublica.exceptions.ResourceNotFoundException;
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
    private MedicamentoRepository medicamentoRepository;

    @Autowired
    private UnidadeDeSaudeRepository unidadeDeSaudeRepository;

    @Autowired
    private ProfissionalRepository profissionalRepository;

    @Autowired
    private MovimentacaoFarmaciaService movimentacaoFarmaciaService;

    /** O lote nasce com saldo zero e a quantidade recebida entra como lançamento de ENTRADA (ADR-0057). */
    @Transactional
    public LoteResponseDto criar(LoteRequestDto dto) {
        Medicamento medicamento = buscarMedicamentoPorId(dto.getMedicamentoId());
        UnidadeDeSaude unidade = buscarUnidadePorId(dto.getUnidadeId());
        Profissional responsavel = buscarProfissionalSeInformado(dto.getProfissionalMatricula());

        Lote lote = loteRepository.save(new Lote(medicamento, unidade, dto.getNumeroLote(), dto.getValidade(), 0));
        movimentacaoFarmaciaService.lancar(lote, TipoMovimentacaoFarmacia.ENTRADA, dto.getQuantidade(), responsavel,
                null, null, null);
        return LoteResponseDto.fromLote(lote);
    }

    /** Só número do lote e validade — ver {@link LoteAtualizacaoRequestDto}. */
    @Transactional
    public LoteResponseDto atualizar(UUID uuid, LoteAtualizacaoRequestDto dto) {
        Lote lote = buscarEntidadePorId(uuid);
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

    public List<LoteResponseDto> listar() {
        return loteRepository.findAll()
                .stream()
                .map(LoteResponseDto::fromLote)
                .collect(Collectors.toList());
    }

    public LoteResponseDto buscarPorId(UUID uuid) {
        return LoteResponseDto.fromLote(buscarEntidadePorId(uuid));
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

    private UnidadeDeSaude buscarUnidadePorId(UUID uuid) {
        return unidadeDeSaudeRepository.findById(uuid)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Não foi possível encontrar uma unidade de saúde com o id " + uuid + " em nossos registros."));
    }
}
