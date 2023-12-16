package com.edufelizardo.API_Mais_Saude_Publica.controllers;

import com.edufelizardo.API_Mais_Saude_Publica.dtos.MinisterioSaudeDto;
import com.edufelizardo.API_Mais_Saude_Publica.dtos.responses.MinisterioSaudeResponseDto;
import com.edufelizardo.API_Mais_Saude_Publica.services.MinisterioSaudeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/miniterio-da-saude")
public class MinisteriaSaudeController {
    @Autowired
    private MinisterioSaudeService saudeService;
    @GetMapping
    public ResponseEntity<List<MinisterioSaudeResponseDto>> getAllMinisterioSaude(){
        List<MinisterioSaudeResponseDto> saudeResponseDtos = saudeService.getAllMinisterioSaude();
        return ResponseEntity.ok(saudeResponseDtos);
    }
    @PostMapping
    public ResponseEntity<MinisterioSaudeResponseDto> createMinisterioSaude(@RequestBody MinisterioSaudeDto dto){
        MinisterioSaudeResponseDto responseDto = saudeService.createMinisterioSaude(dto);
        return new ResponseEntity<>(responseDto, HttpStatus.CREATED);
    }
    @PutMapping("/{name}")
    public ResponseEntity<MinisterioSaudeResponseDto> updateMinisterioSaude(@PathVariable String nomeInstitucional,
                                                                            @RequestBody MinisterioSaudeDto dto){
        MinisterioSaudeResponseDto responseDto = saudeService.updateMinisterioSaude(nomeInstitucional, dto);
        if (responseDto != null) {
            return ResponseEntity.ok(responseDto);
        }
        return ResponseEntity.notFound().build();
    }
    @PutMapping("/{nomeInstitucional}")
    public ResponseEntity<MinisterioSaudeResponseDto> updateMinisterioSaudenomeInstitucional(@PathVariable String nomeInstitucional,
                                                                                             @RequestBody MinisterioSaudeDto dto){
        MinisterioSaudeResponseDto responseDto = saudeService.updateMinisterioSaudeNome(nomeInstitucional, dto);
        if (responseDto != null) {
            return ResponseEntity.ok(responseDto);
        }
        return ResponseEntity.notFound().build();
    }
}
