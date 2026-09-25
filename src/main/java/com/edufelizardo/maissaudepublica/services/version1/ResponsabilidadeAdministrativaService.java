package com.edufelizardo.maissaudepublica.services.version1;

import com.edufelizardo.maissaudepublica.exceptions.ResourceConflictException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceNotFoundException;
import com.edufelizardo.maissaudepublica.models.Profissional;
import com.edufelizardo.maissaudepublica.models.ResponsabilidadeAdministrativa;
import com.edufelizardo.maissaudepublica.models.Setor;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.ResponsabilidadeAdministrativaRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.ResponsabilidadeAdministrativaResponseDto;
import com.edufelizardo.maissaudepublica.repositories.ProfissionalRepository;
import com.edufelizardo.maissaudepublica.repositories.ResponsabilidadeAdministrativaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ResponsabilidadeAdministrativaService {

    @Autowired
    private ResponsabilidadeAdministrativaRepository responsabilidadeAdministrativaRepository;

    @Autowired
    private ProfissionalRepository profissionalRepository;

    @Autowired
    private SetorService setorService;

    public ResponsabilidadeAdministrativaResponseDto criar(ResponsabilidadeAdministrativaRequestDto dto) {
        Profissional profissional = profissionalRepository.findByMatricula(dto.getMatriculaProfissional())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Não foi possível encontrar um profissional com a matrícula " + dto.getMatriculaProfissional() + " em nossos registros."));
        Setor setor = setorService.buscarEntidadePorId(dto.getSetorId());

        ResponsabilidadeAdministrativa responsabilidade = new ResponsabilidadeAdministrativa(
                profissional, setor, dto.getTipo(), dto.getDescricao(), dto.getDataInicio());
        responsabilidade = responsabilidadeAdministrativaRepository.save(responsabilidade);
        return ResponsabilidadeAdministrativaResponseDto.fromResponsabilidadeAdministrativa(responsabilidade);
    }

    /**
     * "Encerrar" é uma operação de negócio explícita, não um PATCH genérico -- a responsabilidade
     * não é apagada nem alterada destrutivamente, ela ganha um fim (ver ADR-0035). Recusa encerrar
     * uma responsabilidade que já tem dataFim (409) -- não é um "atualizar dataFim", é uma ação que
     * só acontece uma vez.
     */
    public ResponsabilidadeAdministrativaResponseDto encerrar(UUID uuid, LocalDate dataFim) {
        ResponsabilidadeAdministrativa responsabilidade = responsabilidadeAdministrativaRepository.findById(uuid)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Não foi possível encontrar uma responsabilidade administrativa com o id " + uuid + " em nossos registros."));

        if (responsabilidade.getDataFim() != null) {
            throw new ResourceConflictException(
                    "A responsabilidade administrativa de id " + uuid + " já foi encerrada em " + responsabilidade.getDataFim() + ".");
        }

        responsabilidade.setDataFim(dataFim);
        responsabilidade = responsabilidadeAdministrativaRepository.save(responsabilidade);
        return ResponsabilidadeAdministrativaResponseDto.fromResponsabilidadeAdministrativa(responsabilidade);
    }

    public List<ResponsabilidadeAdministrativaResponseDto> listarHistorico(String matricula) {
        List<ResponsabilidadeAdministrativaResponseDto> historico = responsabilidadeAdministrativaRepository
                .findByProfissional_MatriculaOrderByDataInicioDesc(matricula)
                .stream()
                .map(ResponsabilidadeAdministrativaResponseDto::fromResponsabilidadeAdministrativa)
                .collect(Collectors.toList());
        if (historico.isEmpty()) {
            throw new ResourceNotFoundException(
                    "Não foi possível encontrar responsabilidades administrativas para o profissional de matrícula " + matricula + " em nossos registros.");
        }
        return historico;
    }
}
