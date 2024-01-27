package com.edufelizardo.API_Mais_Saude_Publica.services;

import com.edufelizardo.API_Mais_Saude_Publica.dtos.PrimeiroDepartOrganizacionalTGMDto;
import com.edufelizardo.API_Mais_Saude_Publica.dtos.responses.PrimeiroDepartOrganizacionalTGMResponseDto;
import com.edufelizardo.API_Mais_Saude_Publica.model.TerceiroGrupoMedical;
import com.edufelizardo.API_Mais_Saude_Publica.model.localizacao.Endereco;
import com.edufelizardo.API_Mais_Saude_Publica.model.primeiro_departamento_operacoes.PrimeiroDepartOrganizacionalTGM;
import com.edufelizardo.API_Mais_Saude_Publica.repositories.PrimeiroDepartOrganizacionalTGMRepository;
import com.edufelizardo.API_Mais_Saude_Publica.repositories.TerceiroGrupoMedicalRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PrimeiroDepartOrganizacionalTGMService {
    @Autowired
    private TerceiroGrupoMedicalRepository terceiroGrupoMedicalRepository;
    @Autowired
    private PrimeiroDepartOrganizacionalTGMRepository tgmRepository;
    public List<PrimeiroDepartOrganizacionalTGMResponseDto> getAllPrimeiroDepartOrganizacionalTGM() {
        return tgmRepository.findAll()
                .stream()
                .map(PrimeiroDepartOrganizacionalTGMResponseDto::fromPrimeiroDepartOrganizacionalTGMResponse)
                .collect(Collectors.toList());
    }

    public List<PrimeiroDepartOrganizacionalTGMResponseDto> getPrimeiroDepartOrganizacionalTGMByNome(String nome) {
        return tgmRepository.findByNome(nome)
                .stream()
                .map(PrimeiroDepartOrganizacionalTGMResponseDto::fromPrimeiroDepartOrganizacionalTGMResponse)
                .collect(Collectors.toList());
    }
    public PrimeiroDepartOrganizacionalTGMResponseDto createPrimeiroDepartOrganizacionalTGM(PrimeiroDepartOrganizacionalTGMDto organizacionalTGMDto) {
        TerceiroGrupoMedical medical = terceiroGrupoMedicalRepository.findByNome(organizacionalTGMDto.terceiroGrupoMedical())
                .orElseThrow(()->new EntityNotFoundException("Treceiro Grupo Medical não encontrado."));
        PrimeiroDepartOrganizacionalTGM tgm = new PrimeiroDepartOrganizacionalTGM(organizacionalTGMDto, medical);
        tgm = tgmRepository.save(tgm);
        return PrimeiroDepartOrganizacionalTGMResponseDto.fromPrimeiroDepartOrganizacionalTGMResponse(tgm);
    }
    public PrimeiroDepartOrganizacionalTGMResponseDto updatePrimeiroDepartOrganizacionalTGM(String nome, PrimeiroDepartOrganizacionalTGMDto dto) {
        Optional<PrimeiroDepartOrganizacionalTGM> organizacionalTGM = tgmRepository.findByNome(nome);
        Endereco endereco = new Endereco(dto.endereco());

        TerceiroGrupoMedical grupoMedical = terceiroGrupoMedicalRepository.findByNome(dto.terceiroGrupoMedical())
                .orElseThrow(()->new EntityNotFoundException("Terceiro Grupo Medical naõ encontrado."));

        if (organizacionalTGM.isPresent()) {
            PrimeiroDepartOrganizacionalTGM departOrganizacionalTGM = organizacionalTGM.get();
            departOrganizacionalTGM.setTerceiroGrupoMedical(grupoMedical);
            departOrganizacionalTGM.setNome(dto.nome());
            departOrganizacionalTGM.setCompostoPelaPrefeituraRegional(dto.CompostoPelaPrefeituraRegional());
            departOrganizacionalTGM.setEndereco(endereco);
            departOrganizacionalTGM.setTelefones(dto.telefones());
            departOrganizacionalTGM.setEmail(dto.email());
            departOrganizacionalTGM.setHorarioFuncionamento(dto.horarioFuncionamento());
            departOrganizacionalTGM.setHorarioAtendimento(dto.horarioAtendimento());
            departOrganizacionalTGM = tgmRepository.save(departOrganizacionalTGM);
            return PrimeiroDepartOrganizacionalTGMResponseDto.fromPrimeiroDepartOrganizacionalTGMResponse(departOrganizacionalTGM);
        }
        return null;
    }
}
