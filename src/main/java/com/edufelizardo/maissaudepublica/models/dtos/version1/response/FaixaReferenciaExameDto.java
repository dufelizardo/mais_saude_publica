package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import com.edufelizardo.maissaudepublica.models.FaixaReferenciaExame;
import com.edufelizardo.maissaudepublica.models.enuns.Sexo;
import com.edufelizardo.maissaudepublica.models.enuns.UnidadeIdade;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.UUID;

/** Faixa de referência por sexo e idade (ADR-0180), com o rótulo que vai para o resultado. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class FaixaReferenciaExameDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private UUID uuid;
    private Sexo sexo;
    private int idadeMinima;
    private UnidadeIdade unidadeIdadeMinima;
    private Integer idadeMaxima;
    private UnidadeIdade unidadeIdadeMaxima;
    private BigDecimal referenciaMinima;
    private BigDecimal referenciaMaxima;
    private BigDecimal criticoMinimo;
    private BigDecimal criticoMaximo;
    private String descricao;
    /** "Feminino, 12 a 59 anos", "Recém-nascido (Ambos os sexos, 0 a 28 dias)" etc. */
    private String rotulo;

    public static FaixaReferenciaExameDto fromFaixa(FaixaReferenciaExame f) {
        return new FaixaReferenciaExameDto(f.getUuid(), f.getSexo(), f.getIdadeMinima(), f.getUnidadeIdadeMinima(), f.getIdadeMaxima(),
                f.getUnidadeIdadeMaxima(), f.getReferenciaMinima(), f.getReferenciaMaxima(), f.getCriticoMinimo(), f.getCriticoMaximo(),
                f.getDescricao(), rotulo(f));
    }

    /** O rótulo da faixa: descrição (se houver), sexo e idades. */
    public static String rotulo(FaixaReferenciaExame f) {
        String sexo = f.getSexo() == Sexo.MASCULINO ? "Masculino" : f.getSexo() == Sexo.FEMININO ? "Feminino" : "Ambos os sexos";
        String idade;
        if (f.getIdadeMaxima() == null) {
            idade = "a partir de " + idade(f.getIdadeMinima(), f.getUnidadeIdadeMinima());
        } else if (f.getUnidadeIdadeMinima() == f.getUnidadeIdadeMaxima() && f.getIdadeMaxima() - 1 > f.getIdadeMinima()) {
            // Idade completa: "12 a 59 anos" vai até a véspera dos 60.
            idade = f.getIdadeMinima() + " a " + idade(f.getIdadeMaxima() - 1, f.getUnidadeIdadeMaxima());
        } else {
            idade = idade(f.getIdadeMinima(), f.getUnidadeIdadeMinima()) + " a menos de " + idade(f.getIdadeMaxima(), f.getUnidadeIdadeMaxima());
        }
        String base = sexo + ", " + idade;
        return f.getDescricao() == null ? base : f.getDescricao() + " (" + base + ")";
    }

    private static String idade(int valor, UnidadeIdade unidade) {
        return valor + switch (unidade) {
            case DIAS -> valor == 1 ? " dia" : " dias";
            case MESES -> valor == 1 ? " mês" : " meses";
            case ANOS -> valor == 1 ? " ano" : " anos";
        };
    }
}
