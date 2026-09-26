package com.edufelizardo.maissaudepublica.models;

import com.edufelizardo.maissaudepublica.models.dtos.version1.request.MedicamentoRequestDto;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;

/**
 * Catálogo de medicamentos — primeira entidade do domínio Farmácia (ver MAPA-DE-DOMINIOS.md #9,
 * ADR-0049). Raiz do domínio, sem FK — mesmo papel estrutural de {@link Paciente} na Assistência.
 * Sem {@code Lote} (validade/quantidade) nem distinção de medicamento controlado (Portaria 344/98)
 * nesta fase — ver ADR-0049 pelas razões (YAGNI: nenhum consumidor real, como Dispensação, existe
 * ainda).
 */
@Entity
@Table(name = "TB_MEDICAMENTO")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class Medicamento implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @NotBlank
    private String nome;

    /**
     * Só preenchido quando difere do {@code nome} comercial — para genéricos, costuma ser igual.
     */
    private String principioAtivo;

    /**
     * Forma farmacêutica + dosagem, texto livre (ex.: "Comprimido 500mg") — convenção usada por
     * catálogos de medicamentos como o RENAME.
     */
    private String apresentacao;

    private String codigo;

    private boolean ativo;

    public Medicamento(MedicamentoRequestDto dto) {
        this.nome = dto.getNome();
        this.principioAtivo = dto.getPrincipioAtivo();
        this.apresentacao = dto.getApresentacao();
        this.codigo = dto.getCodigo();
        this.ativo = dto.getAtivo();
    }
}
