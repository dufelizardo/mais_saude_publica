package com.edufelizardo.API_Mais_Saude_Publica.services;

import com.edufelizardo.API_Mais_Saude_Publica.dtos.SegundoDepartOrganizacionalTGMDto;
import com.edufelizardo.API_Mais_Saude_Publica.dtos.responses.SegundoDepartOrganizacionalTGMResponseDto;
import com.edufelizardo.API_Mais_Saude_Publica.model.localizacao.Endereco;
import com.edufelizardo.API_Mais_Saude_Publica.model.primeiro_departamento_operacoes.PrimeiroDepartOrganizacionalTGM;
import com.edufelizardo.API_Mais_Saude_Publica.model.segundo_departamento_operacoes.SegundoDepartOrganizacionalTGM;
import com.edufelizardo.API_Mais_Saude_Publica.repositories.PrimeiroDepartOrganizacionalTGMRepository;
import com.edufelizardo.API_Mais_Saude_Publica.repositories.SegundoDepartOrganizacionalTGMRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class SegundoDepartOrganizacionalTGMService {
    @Autowired
    private PrimeiroDepartOrganizacionalTGMRepository primeiroDepartOrganizacionalTGMRepository;
    @Autowired
    private SegundoDepartOrganizacionalTGMRepository segundoDepartOrganizacionalTGMRepository;

    public List<SegundoDepartOrganizacionalTGMResponseDto> getAllSegundoDepartOrganizacionalTGM() {
        return segundoDepartOrganizacionalTGMRepository.findAll()
                .stream()
                .map(SegundoDepartOrganizacionalTGMResponseDto::fromSegundoDepartOrganizacionalTGMResponse)
                .collect(Collectors.toList());
    }
    public List<SegundoDepartOrganizacionalTGMResponseDto> getSegundoDepartOrganizacionalTGMByNome(String nome) {
        return segundoDepartOrganizacionalTGMRepository.findByNome(nome)
                .stream()
                .map(SegundoDepartOrganizacionalTGMResponseDto::fromSegundoDepartOrganizacionalTGMResponse)
                .collect(Collectors.toList());
    }
    public SegundoDepartOrganizacionalTGMResponseDto createSegundoDepartOrganizacionalTGM(SegundoDepartOrganizacionalTGMDto organizacionalTGMDto) {
        PrimeiroDepartOrganizacionalTGM departOrganizacionalTGM = primeiroDepartOrganizacionalTGMRepository.findByNome(organizacionalTGMDto.primeiroDepartamentoOrganizacionalTGM())
                .orElseThrow(()->new EntityNotFoundException("Primeiro Departamento Organizacional não encontrado."));
        SegundoDepartOrganizacionalTGM tgm = new SegundoDepartOrganizacionalTGM(organizacionalTGMDto, departOrganizacionalTGM);
        tgm = segundoDepartOrganizacionalTGMRepository.save(tgm);
        return SegundoDepartOrganizacionalTGMResponseDto.fromSegundoDepartOrganizacionalTGMResponse(tgm);
    }
    public SegundoDepartOrganizacionalTGMResponseDto updateSegundoDepartOrganizacionalTGM(String nome, SegundoDepartOrganizacionalTGMDto dto) {
        Optional<SegundoDepartOrganizacionalTGM> organizacionalTGM = segundoDepartOrganizacionalTGMRepository.findByNome(nome);
        Endereco endereco = new Endereco(dto.endereco());

        PrimeiroDepartOrganizacionalTGM primeiroDepartOrganizacionalTGM = primeiroDepartOrganizacionalTGMRepository.findByNome(dto.primeiroDepartamentoOrganizacionalTGM())
                .orElseThrow(()->new EntityNotFoundException("Primeiro Departamento Organizacional não encontrado."));
        if (organizacionalTGM.isPresent()) {
            SegundoDepartOrganizacionalTGM segundoDepartOrganizacionalTGM = organizacionalTGM.get();
            segundoDepartOrganizacionalTGM.setPrimeiroDepartOrganizacionalTGM(primeiroDepartOrganizacionalTGM);
            segundoDepartOrganizacionalTGM.setNome(dto.nome());
            segundoDepartOrganizacionalTGM.setEndereco(endereco);
            segundoDepartOrganizacionalTGM.setTelefones(dto.telefones());
            segundoDepartOrganizacionalTGM.setEmail(dto.email());
            segundoDepartOrganizacionalTGM.setHorarioFuncionamento(dto.horarioFuncionamento());
            segundoDepartOrganizacionalTGM.setHorarioAtendimento(dto.horarioAtendimento());
            segundoDepartOrganizacionalTGM = segundoDepartOrganizacionalTGMRepository.save(segundoDepartOrganizacionalTGM);
            return SegundoDepartOrganizacionalTGMResponseDto.fromSegundoDepartOrganizacionalTGMResponse(segundoDepartOrganizacionalTGM);
        }
        return null;
    }
}
