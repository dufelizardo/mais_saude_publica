package com.edufelizardo.API_Mais_Saude_Publica.services;

import com.edufelizardo.API_Mais_Saude_Publica.dtos.SegundoGrupoMedicalDto;
import com.edufelizardo.API_Mais_Saude_Publica.dtos.responses.SegundoGrupoMedicalResponseDto;
import com.edufelizardo.API_Mais_Saude_Publica.model.PrimeiroGrupoMedical;
import com.edufelizardo.API_Mais_Saude_Publica.model.SegundoGrupoMedical;
import com.edufelizardo.API_Mais_Saude_Publica.model.localizacao.Endereco;
import com.edufelizardo.API_Mais_Saude_Publica.repositories.PrimeiroGrupoMedicalRepository;
import com.edufelizardo.API_Mais_Saude_Publica.repositories.SegundoGrupoMedicalRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
@Service
public class SegundoGrupoMedicalService {
    @Autowired
    private SegundoGrupoMedicalRepository segundoGrupoMedicalRepository;
    @Autowired
    private PrimeiroGrupoMedicalRepository primeiroGrupoMedicalRepository;

    public List<SegundoGrupoMedicalResponseDto> getAllSegundoGrupoMedical() {
        return segundoGrupoMedicalRepository.findAll()
                .stream()
                .map(SegundoGrupoMedicalResponseDto::fromSegundoGrupoMedicalResponse)
                .collect(Collectors.toList());
    }

    public List<SegundoGrupoMedicalResponseDto> getSegundoGrupoMedicalByNome(String nome) {
        return segundoGrupoMedicalRepository.findByNome(nome)
                .stream()
                .map(SegundoGrupoMedicalResponseDto::fromSegundoGrupoMedicalResponse)
                .collect(Collectors.toList());
    }

    public SegundoGrupoMedicalResponseDto createSegundoGrupoMedical(SegundoGrupoMedicalDto grupoMedicalDto) {
        PrimeiroGrupoMedical medical = primeiroGrupoMedicalRepository.findByNomeInstitucional(grupoMedicalDto.primeiroGrupoMedical())
                .orElseThrow(() -> new EntityNotFoundException("Primeiro Grupo Medico não encontrado"));

        SegundoGrupoMedical segundoGrupoMedical = new SegundoGrupoMedical(grupoMedicalDto, medical);
        segundoGrupoMedical = segundoGrupoMedicalRepository.save(segundoGrupoMedical);
        return SegundoGrupoMedicalResponseDto.fromSegundoGrupoMedicalResponse(segundoGrupoMedical);
    }

    public SegundoGrupoMedicalResponseDto updateSegundoGrupoMedical(String nome, SegundoGrupoMedicalDto dto){

        Optional<SegundoGrupoMedical> segundoGrupoMedical = segundoGrupoMedicalRepository.findByNome(nome);
        Endereco endereco = new Endereco(dto.endereco());

        PrimeiroGrupoMedical pMedical = primeiroGrupoMedicalRepository.findByNomeInstitucional(dto.primeiroGrupoMedical())
                .orElseThrow(() -> new EntityNotFoundException("Primeiro Grupo Medico não encontrado"));

        if (segundoGrupoMedical.isPresent()){
            SegundoGrupoMedical medical = segundoGrupoMedical.get();
            medical.setPrimeiroGrupoMedical(pMedical);
            medical.setNome(dto.nome());
            medical.setEndereco(endereco);
            medical.setTelefones(dto.telefones());
            medical.setEmail(dto.email());
            medical.setHorarioFuncionamento(dto.horarioFuncionamento());
            medical.setHorarioAtendimento(dto.horarioAtendimento());
            medical = segundoGrupoMedicalRepository.save(medical);
            return SegundoGrupoMedicalResponseDto.fromSegundoGrupoMedicalResponse(medical);
        }
        return null;
    }
}
