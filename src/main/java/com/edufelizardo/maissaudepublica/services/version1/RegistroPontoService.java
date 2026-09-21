package com.edufelizardo.maissaudepublica.services.version1;

import com.edufelizardo.maissaudepublica.exceptions.ResourceConflictException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceNotFoundException;
import com.edufelizardo.maissaudepublica.models.Profissional;
import com.edufelizardo.maissaudepublica.models.RegistroPonto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.RegistroPontoCorrecaoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.RegistroPontoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.RegistroPontoResponseDto;
import com.edufelizardo.maissaudepublica.repositories.ProfissionalRepository;
import com.edufelizardo.maissaudepublica.repositories.RegistroPontoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class RegistroPontoService {

    @Autowired
    private RegistroPontoRepository registroPontoRepository;

    @Autowired
    private ProfissionalRepository profissionalRepository;

    public RegistroPontoResponseDto criar(RegistroPontoRequestDto dto) {
        Profissional profissional = profissionalRepository.findByMatricula(dto.getMatriculaProfissional())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Não foi possível encontrar um profissional com a matrícula " + dto.getMatriculaProfissional() + " em nossos registros."));

        RegistroPonto registroPonto = new RegistroPonto(profissional, dto.getDataHora(), dto.getTipo(), dto.getOrigem());
        registroPonto = registroPontoRepository.save(registroPonto);
        return RegistroPontoResponseDto.fromRegistroPonto(registroPonto);
    }

    public RegistroPontoResponseDto solicitarCorrecao(UUID uuid, RegistroPontoCorrecaoRequestDto dto) {
        RegistroPonto registroPonto = buscarEntidadePorId(uuid);

        if (registroPonto.getDataHoraProposta() != null) {
            throw new ResourceConflictException(
                    "O registro de ponto de id " + uuid + " já tem uma correção pendente de aprovação.");
        }

        registroPonto.setDataHoraProposta(dto.getDataHoraProposta());
        registroPonto.setTipoProposto(dto.getTipoProposto());
        registroPonto.setJustificativaCorrecao(dto.getJustificativa());
        registroPonto = registroPontoRepository.save(registroPonto);
        return RegistroPontoResponseDto.fromRegistroPonto(registroPonto);
    }

    public RegistroPontoResponseDto aprovarCorrecao(UUID uuid) {
        RegistroPonto registroPonto = buscarEntidadePorId(uuid);

        if (registroPonto.getDataHoraProposta() == null) {
            throw new ResourceConflictException(
                    "O registro de ponto de id " + uuid + " não tem nenhuma correção pendente de aprovação.");
        }

        registroPonto.setDataHora(registroPonto.getDataHoraProposta());
        registroPonto.setTipo(registroPonto.getTipoProposto());
        limparProposta(registroPonto);
        registroPonto = registroPontoRepository.save(registroPonto);
        return RegistroPontoResponseDto.fromRegistroPonto(registroPonto);
    }

    public RegistroPontoResponseDto rejeitarCorrecao(UUID uuid) {
        RegistroPonto registroPonto = buscarEntidadePorId(uuid);

        if (registroPonto.getDataHoraProposta() == null) {
            throw new ResourceConflictException(
                    "O registro de ponto de id " + uuid + " não tem nenhuma correção pendente de aprovação.");
        }

        limparProposta(registroPonto);
        registroPonto = registroPontoRepository.save(registroPonto);
        return RegistroPontoResponseDto.fromRegistroPonto(registroPonto);
    }

    private void limparProposta(RegistroPonto registroPonto) {
        registroPonto.setDataHoraProposta(null);
        registroPonto.setTipoProposto(null);
        registroPonto.setJustificativaCorrecao(null);
    }

    private RegistroPonto buscarEntidadePorId(UUID uuid) {
        return registroPontoRepository.findById(uuid)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Não foi possível encontrar um registro de ponto com o id " + uuid + " em nossos registros."));
    }

    public List<RegistroPontoResponseDto> listarHistorico(String matricula, LocalDate dataInicio, LocalDate dataFim) {
        List<RegistroPonto> registros = (dataInicio != null && dataFim != null)
                ? registroPontoRepository.findByProfissional_MatriculaAndDataHoraBetweenOrderByDataHoraDesc(
                        matricula, dataInicio.atStartOfDay(), dataFim.atTime(LocalTime.MAX))
                : registroPontoRepository.findByProfissional_MatriculaOrderByDataHoraDesc(matricula);

        List<RegistroPontoResponseDto> historico = registros.stream()
                .map(RegistroPontoResponseDto::fromRegistroPonto)
                .collect(Collectors.toList());
        if (historico.isEmpty()) {
            throw new ResourceNotFoundException(
                    "Não foi possível encontrar registros de ponto para o profissional de matrícula " + matricula + " em nossos registros.");
        }
        return historico;
    }
}
