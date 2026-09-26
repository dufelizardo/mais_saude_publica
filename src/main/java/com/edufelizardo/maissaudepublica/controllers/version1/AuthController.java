package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.controllers.version1.examples.ApiErrorResponsesMutacao;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.LoginRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.LoginResponseDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.SecurityStatusResponseDto;
import com.edufelizardo.maissaudepublica.services.version1.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/api/v1/auth/")
@Tag(name = "Auth", description = "Login e status do toggle de segurança (ver docs/adr/0055-primeira-implementacao-de-login-usuario-jwt-e-toggle-por-ambiente.md).")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping(value = "login", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Autentica por CPF ou matrícula funcional", tags = "Auth")
    @ApiResponse(responseCode = "200", description = "Success:", content = {
            @Content(mediaType = "application/json", schema = @Schema(implementation = LoginResponseDto.class))
    })
    @ApiErrorResponsesMutacao
    public ResponseEntity<LoginResponseDto> login(@Valid @RequestBody LoginRequestDto dto) {
        return ResponseEntity.ok(authService.login(dto));
    }

    @GetMapping(value = "status", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Informa se o toggle de segurança está ligado neste ambiente",
            description = "Endpoint sempre público — é a fonte única de verdade que o frontend consulta "
                    + "para decidir se deve ou não exigir login (ver ADR-0055).",
            tags = "Auth")
    @ApiResponse(responseCode = "200", description = "Success:", content = {
            @Content(mediaType = "application/json", schema = @Schema(implementation = SecurityStatusResponseDto.class))
    })
    public ResponseEntity<SecurityStatusResponseDto> status() {
        return ResponseEntity.ok(new SecurityStatusResponseDto(authService.isSecurityEnabled()));
    }
}
