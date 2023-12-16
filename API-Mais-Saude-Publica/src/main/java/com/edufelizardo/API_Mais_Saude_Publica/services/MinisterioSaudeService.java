package com.edufelizardo.API_Mais_Saude_Publica.services;

import com.edufelizardo.API_Mais_Saude_Publica.dtos.MinisterioSaudeDto;
import com.edufelizardo.API_Mais_Saude_Publica.dtos.responses.MinisterioSaudeResponseDto;
import com.edufelizardo.API_Mais_Saude_Publica.model.MinisterioSaude;
import com.edufelizardo.API_Mais_Saude_Publica.model.localizacao.Endereco;
import com.edufelizardo.API_Mais_Saude_Publica.repositories.MinisterioSaudeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class MinisterioSaudeService {
    @Autowired
    private MinisterioSaudeRepository ministerioSaudeRepository;

    public List<MinisterioSaudeResponseDto> getAllMinisterioSaude(){
        return  ministerioSaudeRepository.findAll()
                .stream()
                .map(MinisterioSaudeResponseDto::fromMinisterioSaude)
                .collect(Collectors.toList());
    }

    public MinisterioSaudeResponseDto createMinisterioSaude(MinisterioSaudeDto dto){
        MinisterioSaude ministerioSaude = new MinisterioSaude(dto);
        ministerioSaude = ministerioSaudeRepository.save(ministerioSaude);
        return MinisterioSaudeResponseDto.fromMinisterioSaude(ministerioSaude);
    }
    public MinisterioSaudeResponseDto updateMinisterioSaude(String nomeInstitucional, MinisterioSaudeDto dto){
        Optional<MinisterioSaude> saudeOptional = ministerioSaudeRepository.findByNomeInstitucional(nomeInstitucional);
        Endereco endereco = new Endereco(dto.endereco());
        if (saudeOptional.isPresent()){
            MinisterioSaude saude = saudeOptional.get();
            saude.setEndereco(endereco);
            saude.setHorarioFuncionamento(dto.horarioFuncionamento());
            saude.setHorarioAtendimento(dto.horarioAtendimento());
            saude = ministerioSaudeRepository.save(saude);
            return MinisterioSaudeResponseDto.fromMinisterioSaude(saude);
        }
        return null;
    }

    public MinisterioSaudeResponseDto updateMinisterioSaudeNome(String nomeInstitucional, MinisterioSaudeDto dto){
        Optional<MinisterioSaude> saudeOptional = ministerioSaudeRepository.findByNomeInstitucional(nomeInstitucional);
        if (saudeOptional.isPresent()){
            MinisterioSaude saude = saudeOptional.get();
            saude.setNomeInstitucional(dto.nomeInstitucional());
            saude = ministerioSaudeRepository.save(saude);
            return MinisterioSaudeResponseDto.fromMinisterioSaude(saude);
        }
        return null;
    }
}
