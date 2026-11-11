package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Prescrição hospitalar (ADR-0187). Na nova: a internação e os itens. Na renovação: só o médico e o início (os itens vêm da
 * anterior). Sem início, a próxima hora cheia (ou o fim da anterior, na renovação).
 */
@Data
public class PrescricaoHospitalarRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private UUID internacaoId;

    @NotBlank(message = "Informe a matrícula do médico.")
    private String medicoMatricula;

    private Instant inicio;

    @Size(max = 1000, message = "A observação vai até 1000 caracteres.")
    private String observacao;

    @Valid
    private List<ItemPrescricaoRequestDto> itens = new ArrayList<>();
}
