package com.edufelizardo.maissaudepublica.models;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/** Fornecedor da rede (ADR-0212): CPF ou CNPJ único; as certidões e as sanções ficam em histórico, e a situação é derivada delas. */
@Entity
@Table(name = "TB_FORNECEDOR")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class Fornecedor implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    /** CPF (11) ou CNPJ (14), só os dígitos. */
    @Column(nullable = false, unique = true, length = 14, updatable = false)
    private String documento;

    @Column(nullable = false, updatable = false)
    private boolean pessoaJuridica;

    @Column(nullable = false, length = 200)
    private String razaoSocial;

    @Column(length = 200)
    private String nomeFantasia;

    @Column(length = 150)
    private String email;

    @Column(length = 30)
    private String telefone;

    @Column(length = 500)
    private String endereco;

    @Column(nullable = false)
    private boolean ativo;

    @Column(nullable = false, updatable = false)
    private Instant registradoEm;

    @Column(updatable = false)
    private String registradoPorCpf;
}
