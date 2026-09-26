package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class LoginResponseDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private String token;
    private Instant expiraEm;
    private String nome;
    private String cpf;
}
