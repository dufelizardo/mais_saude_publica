package com.edufelizardo.maissaudepublica.models;

import com.edufelizardo.maissaudepublica.models.enuns.TipoEquipe;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/** Equipe de saúde de uma unidade (ADR-0103): tipo, INE, microáreas, coordenação, reunião e, na eMulti, as equipes apoiadas. */
@Entity
@Table(name = "TB_EQUIPE", uniqueConstraints = @UniqueConstraint(name = "uk_equipe_nome_por_unidade", columnNames = {"unidade_id", "nome"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "apoiadas")
@EqualsAndHashCode(exclude = "apoiadas")
public class Equipe implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unidade_id", referencedColumnName = "uuid", nullable = false, updatable = false)
    private UnidadeDeSaude unidade;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false)
    private TipoEquipe tipo;

    /** Como a rede chama a equipe, ex.: "SF-12". Único na unidade. */
    @Column(nullable = false, length = 80)
    private String nome;

    /** Identificador Nacional de Equipe (CNES), 10 dígitos; opcional e único. */
    @Column(length = 10, unique = true)
    private String ine;

    @Column(nullable = false)
    private boolean ativa;

    /** Códigos das microáreas atendidas, separados por vírgula (ex.: "14, 17, 18"). */
    @Column(length = 300)
    private String microareas;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coordenador_id", referencedColumnName = "uuid")
    private Profissional coordenador;

    @Enumerated(EnumType.STRING)
    private DayOfWeek reuniaoDia;

    private LocalTime reuniaoInicio;

    private LocalTime reuniaoFim;

    @Column(length = 100)
    private String reuniaoLocal;

    /** Na eMulti, as equipes de Saúde da Família e de Atenção Primária que ela apoia. */
    @ManyToMany
    @JoinTable(name = "TB_EQUIPE_APOIO", joinColumns = @JoinColumn(name = "equipe_id"), inverseJoinColumns = @JoinColumn(name = "apoiada_id"))
    private Set<Equipe> apoiadas = new HashSet<>();
}
