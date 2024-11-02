package com.edufelizardo.maissaudepublica.models;

import com.edufelizardo.maissaudepublica.models.dtos.version1.request.HierarquicoUmRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.HierarquicoZeroRequestDto;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.DayOfWeek;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "TB_UNIDADE_DE_SAUDE", uniqueConstraints = {
        @UniqueConstraint(columnNames = "nome")})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class UnidadeDeSaude implements Serializable {
    @Serial
    private static final long serialVersionUID = 3410068205127200223L;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @NotBlank
    @Column(unique = true)
    private String nome;
    private int tipo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unidade_superior_id", referencedColumnName = "uuid", nullable = true)
    private UnidadeDeSaude unidadeSuperior;
    private String regiao;
    private String municipio;
    private String estados;

    @Embedded
    private Endereco endereco;

    @ElementCollection
    @CollectionTable(name = "TB_TELEFONES_SAUDE")
    private Set<String> saudeTelefones;
    private String email;

    @ElementCollection
    @CollectionTable(name = "TB_HORARIO_FUNIONAMENTO")
    @MapKeyColumn(name = "DAY_OF_WEEK")
    @Column(name = "HORARIO_FUNCIONAMENTO")
    private Map<DayOfWeek, String> horarioFuncionamento;

    @ElementCollection
    @CollectionTable(name = "TB_HORARIO_ATENDIMENTO")
    @MapKeyColumn(name = "DAY_OF_WEEK")
    @Column(name = "HORARIO_ATENDIMENTO")
    private Map<DayOfWeek, String> horarioAtendimento;
    private boolean ativo;

    public UnidadeDeSaude(HierarquicoZeroRequestDto dto) {
        this.ativo = true;
        this.nome = dto.getNome();
        this.tipo = dto.getTipo();
        this.endereco = new Endereco(dto.getEndereco());
        this.saudeTelefones = dto.getTelefones();
        this.email = dto.getEmail();
        this.horarioFuncionamento = dto.getHorarioFuncionamento();
        this.horarioAtendimento = dto.getHorarioAtendimento();
    }

    public UnidadeDeSaude(HierarquicoUmRequestDto dto) {
        this.ativo = true;
        this.nome = dto.getNome();
        this.tipo = dto.getTipo();
        this.municipio = dto.getMunicipio();
        this.estados = dto.getEstados();
        this.endereco = new Endereco(dto.getEndereco());
        this.saudeTelefones = dto.getTelefones();
        this.email = dto.getEmail();
        this.horarioFuncionamento = dto.getHorarioFuncionamento();
        this.horarioAtendimento = dto.getHorarioAtendimento();
    }
}
