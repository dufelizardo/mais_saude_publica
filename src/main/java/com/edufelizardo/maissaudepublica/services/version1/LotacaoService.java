package com.edufelizardo.maissaudepublica.services.version1;

import com.edufelizardo.maissaudepublica.exceptions.ResourceNotFoundException;
import com.edufelizardo.maissaudepublica.models.Cargo;
import com.edufelizardo.maissaudepublica.models.Lotacao;
import com.edufelizardo.maissaudepublica.models.Profissional;
import com.edufelizardo.maissaudepublica.models.UnidadeDeSaude;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.LotacaoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.LotacaoResponseDto;
import com.edufelizardo.maissaudepublica.repositories.CargoRepository;
import com.edufelizardo.maissaudepublica.repositories.LotacaoRepository;
import com.edufelizardo.maissaudepublica.repositories.ProfissionalRepository;
import com.edufelizardo.maissaudepublica.repositories.UnidadeDeSaudeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Ver docs/rh/MODELO-RH.md seção 2.4. Um único endpoint de criação cobre admissão, transferência e
 * mudança de cargo — se já existir uma lotação vigente pro profissional, ela é fechada
 * automaticamente antes da nova ser criada, garantindo no máximo uma vigente por vez.
 */
@Service
public class LotacaoService {

    @Autowired
    private LotacaoRepository lotacaoRepository;

    @Autowired
    private ProfissionalRepository profissionalRepository;

    @Autowired
    private UnidadeDeSaudeRepository unidadeDeSaudeRepository;

    @Autowired
    private CargoService cargoService;

    @Transactional
    public LotacaoResponseDto criar(LotacaoRequestDto dto) {
        Profissional profissional = profissionalRepository.findByMatricula(dto.getMatriculaProfissional())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Não foi possível encontrar um profissional com a matrícula " + dto.getMatriculaProfissional() + " em nossos registros."));
        UnidadeDeSaude unidade = unidadeDeSaudeRepository.findById(dto.getUnidadeId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Não foi possível encontrar uma unidade de saúde com o id " + dto.getUnidadeId() + " em nossos registros."));
        Cargo cargo = cargoService.buscarEntidadePorId(dto.getCargoId());

        lotacaoRepository.findByProfissional_MatriculaAndDataFimIsNull(dto.getMatriculaProfissional())
                .ifPresent(vigente -> {
                    vigente.setDataFim(dto.getDataInicio().minusDays(1));
                    lotacaoRepository.save(vigente);
                });

        Lotacao lotacao = new Lotacao(profissional, unidade, cargo, dto.getJornadaSemanalHoras(),
                dto.getDataInicio(), dto.getMotivo());
        lotacao = lotacaoRepository.save(lotacao);
        return LotacaoResponseDto.fromLotacao(lotacao);
    }

    public List<LotacaoResponseDto> listarHistorico(String matricula) {
        List<LotacaoResponseDto> historico = lotacaoRepository.findByProfissional_MatriculaOrderByDataInicioDesc(matricula)
                .stream()
                .map(LotacaoResponseDto::fromLotacao)
                .collect(Collectors.toList());
        if (historico.isEmpty()) {
            throw new ResourceNotFoundException(
                    "Não foi possível encontrar lotações para o profissional de matrícula " + matricula + " em nossos registros.");
        }
        return historico;
    }

    public LotacaoResponseDto buscarVigente(String matricula) {
        return lotacaoRepository.findByProfissional_MatriculaAndDataFimIsNull(matricula)
                .map(LotacaoResponseDto::fromLotacao)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Não foi possível encontrar uma lotação vigente para o profissional de matrícula " + matricula + " em nossos registros."));
    }
}
