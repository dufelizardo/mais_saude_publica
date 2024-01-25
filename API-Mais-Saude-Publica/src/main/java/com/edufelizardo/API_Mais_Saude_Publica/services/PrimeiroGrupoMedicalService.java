package com.edufelizardo.API_Mais_Saude_Publica.services;

import com.edufelizardo.API_Mais_Saude_Publica.dtos.PrimeiroGrupoMedicalDto;
import com.edufelizardo.API_Mais_Saude_Publica.dtos.responses.PrimeiroGrupoMedicalResponseDto;
import com.edufelizardo.API_Mais_Saude_Publica.model.PrimeiroGrupoMedical;
import com.edufelizardo.API_Mais_Saude_Publica.model.localizacao.Endereco;
import com.edufelizardo.API_Mais_Saude_Publica.repositories.PrimeiroGrupoMedicalRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PrimeiroGrupoMedicalService {
    @Autowired
    private PrimeiroGrupoMedicalRepository medicalRepository;

    public List<PrimeiroGrupoMedicalResponseDto> getAllPrimeiroGrupoMedical(){
        return  medicalRepository.findAll()
                .stream()
                .map(PrimeiroGrupoMedicalResponseDto::fromPrimeiroGrupoMedical)
                .collect(Collectors.toList());
    }

    public PrimeiroGrupoMedicalResponseDto createPrimeiroGrupoMedical(PrimeiroGrupoMedicalDto dto){
        PrimeiroGrupoMedical ministerioSaude = new PrimeiroGrupoMedical(dto);
        ministerioSaude = medicalRepository.save(ministerioSaude);
        return PrimeiroGrupoMedicalResponseDto.fromPrimeiroGrupoMedical(ministerioSaude);
    }
    public PrimeiroGrupoMedicalResponseDto updatePrimeiroGrupoMedical(String nomeInstitucional, PrimeiroGrupoMedicalDto dto){
        Optional<PrimeiroGrupoMedical> saudeOptional = medicalRepository.findByNomeInstitucional(nomeInstitucional);
        Endereco endereco = new Endereco(dto.endereco());
        if (saudeOptional.isPresent()){
            PrimeiroGrupoMedical saude = saudeOptional.get();
            saude.setEndereco(endereco);
            saude.setTelefones(dto.telefones());
            saude.setEmail(dto.email());
            saude.setHorarioFuncionamento(dto.horarioFuncionamento());
            saude.setHorarioAtendimento(dto.horarioAtendimento());
            saude = medicalRepository.save(saude);
            return PrimeiroGrupoMedicalResponseDto.fromPrimeiroGrupoMedical(saude);
        }
        return null;
    }

    public PrimeiroGrupoMedicalResponseDto updatePrimeiroGrupoMedicalNome(String nomeInstitucional, PrimeiroGrupoMedicalDto dto){
        Optional<PrimeiroGrupoMedical> saudeOptional = medicalRepository.findByNomeInstitucional(nomeInstitucional);
        if (saudeOptional.isPresent()){
            PrimeiroGrupoMedical saude = saudeOptional.get();
            saude.setNomeInstitucional(dto.nomeInstitucional());
            saude = medicalRepository.save(saude);
            return PrimeiroGrupoMedicalResponseDto.fromPrimeiroGrupoMedical(saude);
        }
        return null;
    }
}
