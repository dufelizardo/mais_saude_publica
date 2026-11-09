package com.edufelizardo.maissaudepublica.models;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

/** Versão do documento (ADR-0172): o arquivo enviado, fora do banco, com o hash que denuncia alteração. Só inclusão. */
@Entity
@Table(name = "TB_VERSAO_DOCUMENTO", uniqueConstraints = @UniqueConstraint(name = "uk_versao_documento", columnNames = {"documento_id", "numero"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class VersaoDocumento implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "documento_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Documento documento;

    @Column(nullable = false, updatable = false)
    private int numero;

    @Column(nullable = false, length = 255, updatable = false)
    private String nomeArquivo;

    @Column(nullable = false, length = 60, updatable = false)
    private String tipoConteudo;

    @Column(nullable = false, updatable = false)
    private long tamanho;

    @Column(nullable = false, length = 64, updatable = false)
    private String sha256;

    /** Onde o arquivo está no armazenamento. Não vem do usuário. */
    @Column(nullable = false, length = 60, updatable = false)
    private String chave;

    @Column(nullable = false, updatable = false)
    private LocalDateTime enviadoEm;

    @Column(updatable = false)
    private String enviadoPorCpf;
}
