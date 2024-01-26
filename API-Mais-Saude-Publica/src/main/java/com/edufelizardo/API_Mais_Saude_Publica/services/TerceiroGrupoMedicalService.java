package com.edufelizardo.API_Mais_Saude_Publica.services;

import com.edufelizardo.API_Mais_Saude_Publica.dtos.TerceiroGrupoMedicalDto;
import com.edufelizardo.API_Mais_Saude_Publica.dtos.responses.TerceiroGrupoMedicalResponseDto;
import com.edufelizardo.API_Mais_Saude_Publica.model.SegundoGrupoMedical;
import com.edufelizardo.API_Mais_Saude_Publica.model.TerceiroGrupoMedical;
import com.edufelizardo.API_Mais_Saude_Publica.model.localizacao.Endereco;
import com.edufelizardo.API_Mais_Saude_Publica.repositories.SegundoGrupoMedicalRepository;
import com.edufelizardo.API_Mais_Saude_Publica.repositories.TerceiroGrupoMedicalRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TerceiroGrupoMedicalService {
    @Autowired
    private TerceiroGrupoMedicalRepository terceiroGrupoMedicalRepository;
    @Autowired
    private SegundoGrupoMedicalRepository segundoGrupoMedicalRepository;
    public List<TerceiroGrupoMedicalResponseDto> getAllTerceiroGrupoMedical() {
        return terceiroGrupoMedicalRepository.findAll()
                .stream()
                .map(TerceiroGrupoMedicalResponseDto::fromTerceiroGrupoMedicalResponse)
                .collect(Collectors.toList());
    }
    public List<TerceiroGrupoMedicalResponseDto> getTerceiroGrupoMedicalByNome(String nome) {
        return terceiroGrupoMedicalRepository.findByNome(nome)
                .stream()
                .map(TerceiroGrupoMedicalResponseDto::fromTerceiroGrupoMedicalResponse)
                .collect(Collectors.toList());
    }
    public TerceiroGrupoMedicalResponseDto createTerceiroGrupoMedical(TerceiroGrupoMedicalDto grupoMedicalDto) {
        SegundoGrupoMedical medical = segundoGrupoMedicalRepository.findByNome(grupoMedicalDto.segundoGrupoMedical())
                .orElseThrow(()-> new EntityNotFoundException("Segundo Grupo Medical não encontrado."));
        TerceiroGrupoMedical terceiroGrupoMedical = new TerceiroGrupoMedical(grupoMedicalDto, medical);
        terceiroGrupoMedical = terceiroGrupoMedicalRepository.save(terceiroGrupoMedical);
        return TerceiroGrupoMedicalResponseDto.fromTerceiroGrupoMedicalResponse(terceiroGrupoMedical);
    }
    public TerceiroGrupoMedicalResponseDto updateTerceiroGrupoMedical(String nome, TerceiroGrupoMedicalDto dto) {
        Optional<TerceiroGrupoMedical> terceiroGrupoMedical = terceiroGrupoMedicalRepository.findByNome(nome);
        Endereco endereco = new Endereco(dto.endereco());

        SegundoGrupoMedical grupoMedical = segundoGrupoMedicalRepository.findByNome(dto.segundoGrupoMedical())
                .orElseThrow(()->new EntityNotFoundException("Segundo Grupo Medical não encontrado."));

        if (terceiroGrupoMedical.isPresent()) {
            TerceiroGrupoMedical medical = terceiroGrupoMedical.get();
            medical.setSegundoGrupoMedical(grupoMedical);
            medical.setNome(dto.nome());
            medical.setEndereco(endereco);
            medical.setTelefones(dto.telefones());
            medical.setEmail(dto.email());
            medical.setHorarioFuncionamento(dto.horarioFuncionamento());
            medical.setHorarioAtendimento(dto.horarioAtendimento());
            medical = terceiroGrupoMedicalRepository.save(medical);
            return TerceiroGrupoMedicalResponseDto.fromTerceiroGrupoMedicalResponse(medical);
        }
        return null;
    }
}
