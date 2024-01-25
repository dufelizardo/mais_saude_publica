package com.edufelizardo.API_Mais_Saude_Publica.controllers;

import com.edufelizardo.API_Mais_Saude_Publica.dtos.SegundoGrupoMedicalDto;
import com.edufelizardo.API_Mais_Saude_Publica.dtos.responses.SegundoGrupoMedicalResponseDto;
import com.edufelizardo.API_Mais_Saude_Publica.services.SegundoGrupoMedicalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/segundo-grupo-medicals")
public class SegundoGrupoMedicalController {
    @Autowired
    private SegundoGrupoMedicalService medicalService;

    @GetMapping
    public ResponseEntity<List<SegundoGrupoMedicalResponseDto>> getAllSegundoGrupoMedical(){
        List<SegundoGrupoMedicalResponseDto> dtoList = medicalService.getAllSegundoGrupoMedical();
        return ResponseEntity.ok(dtoList);
    }

    @GetMapping("/{nome}")
    public ResponseEntity<List<SegundoGrupoMedicalResponseDto>> getSegundoGrupoMedicalByNome(@PathVariable String nome){
        List<SegundoGrupoMedicalResponseDto> dtoList = medicalService.getSegundoGrupoMedicalByNome(nome);
        if (!dtoList.isEmpty()) {
            return ResponseEntity.ok(dtoList);
        }
        return ResponseEntity.noContent().build();
    }

    @PostMapping
    public ResponseEntity<SegundoGrupoMedicalResponseDto> createSegundoGrupoMedical(@RequestBody SegundoGrupoMedicalDto dto){
        SegundoGrupoMedicalResponseDto responseDto = medicalService.createSegundoGrupoMedical(dto);
        return new ResponseEntity<>(responseDto, HttpStatus.CREATED);
    }

    @PutMapping("/{nome}")
    public ResponseEntity<SegundoGrupoMedicalResponseDto> updateSegundoGrupoMedical(@PathVariable String nome,
                                                                                    @RequestBody SegundoGrupoMedicalDto dto){
        SegundoGrupoMedicalResponseDto responseDto = medicalService.updateSegundoGrupoMedical(nome, dto);
        if (responseDto != null){
            return ResponseEntity.ok(responseDto);
        }
        return ResponseEntity.noContent().build();
    }
}
