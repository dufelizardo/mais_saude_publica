package com.edufelizardo.API_Mais_Saude_Publica.controllers;

import com.edufelizardo.API_Mais_Saude_Publica.dtos.SegundoDepartOrganizacionalTGMDto;
import com.edufelizardo.API_Mais_Saude_Publica.dtos.responses.SegundoDepartOrganizacionalTGMResponseDto;
import com.edufelizardo.API_Mais_Saude_Publica.services.SegundoDepartOrganizacionalTGMService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/segundo-departamento-organizacional-tgms")
public class SegundoDepartOrganizacionalTGMController {
    @Autowired
    private SegundoDepartOrganizacionalTGMService tgmService;
    @GetMapping
    public ResponseEntity<List<SegundoDepartOrganizacionalTGMResponseDto>> getAllSegundoDepartOrganizacionalTGMResponse() {
        List<SegundoDepartOrganizacionalTGMResponseDto> dtoList = tgmService.getAllSegundoDepartOrganizacionalTGM();
        return ResponseEntity.ok(dtoList);
    }
    @GetMapping("/{nome}")
    public ResponseEntity<List<SegundoDepartOrganizacionalTGMResponseDto>> getSegundoDepartOrganizacionalTGMResponseByNome(@PathVariable String nome) {
        List<SegundoDepartOrganizacionalTGMResponseDto> dtoList = tgmService.getSegundoDepartOrganizacionalTGMByNome(nome);
        if (!dtoList.isEmpty()) {
            return ResponseEntity.ok(dtoList);
        }
        return ResponseEntity.noContent().build();
    }
    @PostMapping
    public ResponseEntity<SegundoDepartOrganizacionalTGMResponseDto> createSegundoDepartOrganizacionalTGMResponse(@RequestBody SegundoDepartOrganizacionalTGMDto dto) {
        SegundoDepartOrganizacionalTGMResponseDto responseDto = tgmService.createSegundoDepartOrganizacionalTGM(dto);
        return new ResponseEntity<>(responseDto, HttpStatus.CREATED);
    }
    @PutMapping("/{nome}")
    public ResponseEntity<SegundoDepartOrganizacionalTGMResponseDto> updateSegundoDepartOrganizacionalTGMResponse(@PathVariable String nome,
                                                                                                                  @RequestBody SegundoDepartOrganizacionalTGMDto dto) {
        SegundoDepartOrganizacionalTGMResponseDto responseDto = tgmService.updateSegundoDepartOrganizacionalTGM(nome, dto);
        if (responseDto != null) {
            return  ResponseEntity.ok(responseDto);
        }
        return ResponseEntity.noContent().build();
    }
}
