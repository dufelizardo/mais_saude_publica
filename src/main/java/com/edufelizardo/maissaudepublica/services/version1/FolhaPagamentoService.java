package com.edufelizardo.maissaudepublica.services.version1;

import com.edufelizardo.maissaudepublica.exceptions.ResourceConflictException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceNotFoundException;
import com.edufelizardo.maissaudepublica.models.FolhaPagamento;
import com.edufelizardo.maissaudepublica.models.Profissional;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.FolhaPagamentoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.FolhaPagamentoResponseDto;
import com.edufelizardo.maissaudepublica.repositories.FolhaPagamentoRepository;
import com.edufelizardo.maissaudepublica.repositories.ProfissionalRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class FolhaPagamentoService {

    @Autowired
    private FolhaPagamentoRepository folhaPagamentoRepository;

    @Autowired
    private ProfissionalRepository profissionalRepository;

    public FolhaPagamentoResponseDto criar(FolhaPagamentoRequestDto dto) {
        Profissional profissional = profissionalRepository.findByMatricula(dto.getMatriculaProfissional())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Não foi possível encontrar um profissional com a matrícula " + dto.getMatriculaProfissional() + " em nossos registros."));

        if (folhaPagamentoRepository.existsByProfissional_MatriculaAndCompetencia(dto.getMatriculaProfissional(), dto.getCompetencia())) {
            throw new ResourceConflictException(
                    "Já existe uma folha de pagamento para o profissional de matrícula " + dto.getMatriculaProfissional()
                            + " na competência " + dto.getCompetencia() + ".");
        }

        FolhaPagamento folhaPagamento = new FolhaPagamento(profissional, dto.getCompetencia(), dto.getProventos(),
                dto.getDescontos(), dto.getEncargos(), dto.getTotal());
        folhaPagamento = folhaPagamentoRepository.save(folhaPagamento);
        return FolhaPagamentoResponseDto.fromFolhaPagamento(folhaPagamento);
    }

    /**
     * Sem 404 em lista vazia, diferente de {@link #listarHistorico}: "ninguém processado ainda
     * nesta competência" é um estado normal de partida da tela, não um erro (a tela existe
     * justamente pra ir preenchendo aos poucos).
     */
    public List<FolhaPagamentoResponseDto> listarPorCompetencia(String competencia) {
        return folhaPagamentoRepository.findByCompetenciaOrderByProfissional_NomeAsc(competencia)
                .stream()
                .map(FolhaPagamentoResponseDto::fromFolhaPagamento)
                .collect(Collectors.toList());
    }

    public List<FolhaPagamentoResponseDto> listarHistorico(String matricula) {
        List<FolhaPagamentoResponseDto> historico = folhaPagamentoRepository.findByProfissional_MatriculaOrderByCompetenciaDesc(matricula)
                .stream()
                .map(FolhaPagamentoResponseDto::fromFolhaPagamento)
                .collect(Collectors.toList());
        if (historico.isEmpty()) {
            throw new ResourceNotFoundException(
                    "Não foi possível encontrar folhas de pagamento para o profissional de matrícula " + matricula + " em nossos registros.");
        }
        return historico;
    }
}
