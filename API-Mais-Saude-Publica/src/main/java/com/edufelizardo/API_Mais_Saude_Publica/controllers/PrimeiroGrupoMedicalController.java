package com.edufelizardo.API_Mais_Saude_Publica.controllers;

import com.edufelizardo.API_Mais_Saude_Publica.dtos.PrimeiroGrupoMedicalDto;
import com.edufelizardo.API_Mais_Saude_Publica.dtos.responses.PrimeiroGrupoMedicalResponseDto;
import com.edufelizardo.API_Mais_Saude_Publica.services.PrimeiroGrupoMedicalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/primeiro-grupo-medical")
public class PrimeiroGrupoMedicalController {
    @Autowired
    private PrimeiroGrupoMedicalService saudeService;
    @GetMapping
    public ResponseEntity<List<PrimeiroGrupoMedicalResponseDto>> getAllMinisterioSaude(){
        List<PrimeiroGrupoMedicalResponseDto> saudeResponseDtos = saudeService.getAllPrimeiroGrupoMedical();
        return ResponseEntity.ok(saudeResponseDtos);
    }
    @PostMapping
    public ResponseEntity<PrimeiroGrupoMedicalResponseDto> createMinisterioSaude(@RequestBody PrimeiroGrupoMedicalDto dto){
        PrimeiroGrupoMedicalResponseDto responseDto = saudeService.createPrimeiroGrupoMedical(dto);
        return new ResponseEntity<>(responseDto, HttpStatus.CREATED);
    }
    @PutMapping("/dados/{nomeInstitucional}")
    public ResponseEntity<PrimeiroGrupoMedicalResponseDto> updateMinisterioSaude(@PathVariable String nomeInstitucional,
                                                                                 @RequestBody PrimeiroGrupoMedicalDto dto){
        PrimeiroGrupoMedicalResponseDto responseDto = saudeService.updatePrimeiroGrupoMedical(nomeInstitucional, dto);
        if (responseDto != null) {
            return ResponseEntity.ok(responseDto);
        }
        return ResponseEntity.noContent().build();
    }
    @PutMapping("/{nomeInstitucional}")
    public ResponseEntity<PrimeiroGrupoMedicalResponseDto> updateMinisterioSaudenomeInstitucional(@PathVariable String nomeInstitucional,
                                                                                                  @RequestBody PrimeiroGrupoMedicalDto dto){
        PrimeiroGrupoMedicalResponseDto responseDto = saudeService.updatePrimeiroGrupoMedicalNome(nomeInstitucional, dto);
        if (responseDto != null) {
            return ResponseEntity.ok(responseDto);
        }
        return ResponseEntity.noContent().build();
    }
}
