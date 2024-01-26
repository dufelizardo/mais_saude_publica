package com.edufelizardo.API_Mais_Saude_Publica.controllers;

import com.edufelizardo.API_Mais_Saude_Publica.dtos.TerceiroGrupoMedicalDto;
import com.edufelizardo.API_Mais_Saude_Publica.dtos.responses.TerceiroGrupoMedicalResponseDto;
import com.edufelizardo.API_Mais_Saude_Publica.services.TerceiroGrupoMedicalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/terceiro-grupo-medicals")
public class TerceiroGrupoMedicalController {
    @Autowired
    private TerceiroGrupoMedicalService medicalService;

    @GetMapping
    public ResponseEntity<List<TerceiroGrupoMedicalResponseDto>> getAllTerceiroGrupoMedical() {
        List<TerceiroGrupoMedicalResponseDto> dtoList = medicalService.getAllTerceiroGrupoMedical();
        return ResponseEntity.ok(dtoList);
    }
    @GetMapping("/{nome}")
    public ResponseEntity<List<TerceiroGrupoMedicalResponseDto>> getTerceiroGrupoMedicalByNome(@PathVariable String nome) {
        List<TerceiroGrupoMedicalResponseDto> dtoList = medicalService.getTerceiroGrupoMedicalByNome(nome);
        if (!dtoList.isEmpty()) {
            return ResponseEntity.ok(dtoList);
        }
        return ResponseEntity.noContent().build();
    }
    @PostMapping
    public ResponseEntity<TerceiroGrupoMedicalResponseDto> createTerceiroGrupoMedical(@RequestBody TerceiroGrupoMedicalDto dto) {
        TerceiroGrupoMedicalResponseDto responseDto = medicalService.createTerceiroGrupoMedical(dto);
        return new ResponseEntity<>(responseDto, HttpStatus.CREATED);
    }
    @PutMapping("/{nome}")
    public ResponseEntity<TerceiroGrupoMedicalResponseDto> upadateTerceiroGrupoMedical(@PathVariable String nome,
                                                                                       @RequestBody TerceiroGrupoMedicalDto dto) {
        TerceiroGrupoMedicalResponseDto responseDto = medicalService.updateTerceiroGrupoMedical(nome, dto);
        if (responseDto != null) {
            return ResponseEntity.ok(responseDto);
        }
        return ResponseEntity.noContent().build();
    }
}
