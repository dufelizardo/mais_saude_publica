package com.edufelizardo.API_Mais_Saude_Publica.controllers;

import com.edufelizardo.API_Mais_Saude_Publica.dtos.PrimeiroDepartOrganizacionalTGMDto;
import com.edufelizardo.API_Mais_Saude_Publica.dtos.responses.PrimeiroDepartOrganizacionalTGMResponseDto;
import com.edufelizardo.API_Mais_Saude_Publica.services.PrimeiroDepartOrganizacionalTGMService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/primeiro-departamento-organizacional-tgms")
public class PrimeiroDepartOrganizacionalTGMController {
    @Autowired
    private PrimeiroDepartOrganizacionalTGMService tgmService;
    @GetMapping
    public ResponseEntity<List<PrimeiroDepartOrganizacionalTGMResponseDto>> getAllPrimeiroDepartOrganizacionalTGM() {
        List<PrimeiroDepartOrganizacionalTGMResponseDto> dtoList = tgmService.getAllPrimeiroDepartOrganizacionalTGM();
        return ResponseEntity.ok(dtoList);
    }
    @GetMapping("/{nome}")
    public ResponseEntity<List<PrimeiroDepartOrganizacionalTGMResponseDto>> getPrimeiroDepartOrganizacionalTGMByNome(@PathVariable String nome) {
        List<PrimeiroDepartOrganizacionalTGMResponseDto> dtoList = tgmService.getPrimeiroDepartOrganizacionalTGMByNome(nome);
        if (!dtoList.isEmpty()) {
            return ResponseEntity.ok(dtoList);
        }
        return ResponseEntity.noContent().build();
    }
    @PostMapping
    public ResponseEntity<PrimeiroDepartOrganizacionalTGMResponseDto> createPrimeiroDepartOrganizacionalTGM(@RequestBody PrimeiroDepartOrganizacionalTGMDto dto) {
        PrimeiroDepartOrganizacionalTGMResponseDto responseDto = tgmService.createPrimeiroDepartOrganizacionalTGM(dto);
        return new ResponseEntity<>(responseDto, HttpStatus.CREATED);
    }
    @PutMapping("/{nome}")
    public ResponseEntity<PrimeiroDepartOrganizacionalTGMResponseDto> updatePrimeiroDepartOrganizacionalTGM(@PathVariable String nome,
                                                                                                            @RequestBody PrimeiroDepartOrganizacionalTGMDto dto) {
        PrimeiroDepartOrganizacionalTGMResponseDto responseDto = tgmService.updatePrimeiroDepartOrganizacionalTGM(nome, dto);
        if (responseDto != null) {
            return  ResponseEntity.ok(responseDto);
        }
        return ResponseEntity.noContent().build();
    }
}
