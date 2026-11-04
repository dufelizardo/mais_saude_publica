package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import com.edufelizardo.maissaudepublica.models.enuns.ResultadoVistoriaVetorial;
import com.edufelizardo.maissaudepublica.models.enuns.TipoImovel;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.UUID;

/** O imóvel para o agente de endemias (ADR-0162): endereço, local e a última vistoria; sem família nem morador. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class ImovelVetorialDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private UUID uuid;
    private TipoImovel tipo;
    private String endereco;
    private UUID microareaId;
    private String microareaCodigo;
    private UUID areaId;
    private String areaNome;
    private Double latitude;
    private Double longitude;
    private LocalDate ultimaVistoria;
    private ResultadoVistoriaVetorial ultimoResultado;
    /** A última vistoria do ciclo atual ficou fechada ou recusada. */
    private boolean pendente;
}
