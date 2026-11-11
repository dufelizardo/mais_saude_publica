package com.edufelizardo.maissaudepublica.models.dtos.version1.request;

import com.edufelizardo.maissaudepublica.models.enuns.DesfechoAph;
import com.edufelizardo.maissaudepublica.models.enuns.MecanismoTrauma;
import com.edufelizardo.maissaudepublica.models.enuns.NaturezaAph;
import com.edufelizardo.maissaudepublica.models.enuns.ProcedimentoAph;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;

/** Ficha do APH (ADR-0188): cena, avaliação primária, procedimentos, impressão diagnóstica, desfecho e entrega. Tudo opcional até finalizar. */
@Data
public class FichaAphRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private NaturezaAph natureza;

    private MecanismoTrauma mecanismoTrauma;

    @Size(max = 2000, message = "A situação encontrada vai até 2000 caracteres.")
    private String situacaoEncontrada;

    private Boolean hemorragiaExsanguinante;

    @Size(max = 500, message = "A via aérea vai até 500 caracteres.")
    private String viaAerea;

    @Size(max = 500, message = "A respiração vai até 500 caracteres.")
    private String respiracao;

    @Size(max = 500, message = "A circulação vai até 500 caracteres.")
    private String circulacao;

    @Min(value = 1, message = "Glasgow ocular vai de 1 a 4.")
    @Max(value = 4, message = "Glasgow ocular vai de 1 a 4.")
    private Integer glasgowOcular;

    @Min(value = 1, message = "Glasgow verbal vai de 1 a 5.")
    @Max(value = 5, message = "Glasgow verbal vai de 1 a 5.")
    private Integer glasgowVerbal;

    @Min(value = 1, message = "Glasgow motor vai de 1 a 6.")
    @Max(value = 6, message = "Glasgow motor vai de 1 a 6.")
    private Integer glasgowMotor;

    @Size(max = 200, message = "As pupilas vão até 200 caracteres.")
    private String pupilas;

    @Size(max = 500, message = "A exposição vai até 500 caracteres.")
    private String exposicao;

    private Set<ProcedimentoAph> procedimentos = new LinkedHashSet<>();

    @Size(max = 500, message = "O outro procedimento vai até 500 caracteres.")
    private String procedimentoOutro;

    @Size(max = 1000, message = "A impressão diagnóstica vai até 1000 caracteres.")
    private String impressaoDiagnostica;

    @Pattern(regexp = "^\\s*([A-Za-z][0-9]{2}(\\.?[0-9A-Za-z]{1,2})?)?\\s*$", message = "CID-10 inválido (ex.: S06.0).")
    private String cid;

    private DesfechoAph desfecho;

    @Size(max = 200, message = "O nome de quem recusou vai até 200 caracteres.")
    private String recusaNome;

    private Instant obitoEm;

    private String obitoConstatadoPorMatricula;

    @Size(max = 200, message = "Quem recebeu vai até 200 caracteres.")
    private String recebidoPor;

    private Instant entregueEm;
}
