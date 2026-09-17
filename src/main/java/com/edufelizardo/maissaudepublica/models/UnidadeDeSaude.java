package com.edufelizardo.maissaudepublica.models;

import com.edufelizardo.maissaudepublica.models.dtos.version1.request.EstadualRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.FederalRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.MunicipalRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.RegionalRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.UnidadeSaudeRequestDto;
import com.edufelizardo.maissaudepublica.models.enuns.TipoUnidadeDeSaude;
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
    @Enumerated(EnumType.STRING)
    private TipoUnidadeDeSaude tipo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unidade_superior_id", referencedColumnName = "uuid", nullable = true)
    private UnidadeDeSaude unidadeSuperior;

    /**
     * Vínculo lateral, não-hierárquico, de supervisão técnica — só preenchido para Unidade de
     * Saúde (UBS/HOSPITAL), sempre apontando para uma unidade REGIONAL. Não substitui
     * {@code unidadeSuperior} (que continua sendo Municipal, ver ADR-0009/ADR-0013).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supervisao_regional_id", referencedColumnName = "uuid", nullable = true)
    private UnidadeDeSaude supervisaoRegional;
    private String regiao;
    private String municipio;
    @Column(name = "estado_administracao")
    private String estado;

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

    /**
     * CPF do responsável, informado no cadastro da unidade mesmo quando o {@link Profissional}
     * correspondente ainda não existe (vínculo fraco resolvido por reconciliação, ver ADR-0014).
     */
    private String responsavelCpf;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "responsavel_id", referencedColumnName = "uuid", nullable = true)
    private Profissional responsavel;

    public UnidadeDeSaude(FederalRequestDto dto) {
        this.ativo = true;
        this.nome = dto.getNome();
        this.tipo = dto.getTipo();
        this.endereco = new Endereco(dto.getEndereco());
        this.saudeTelefones = dto.getTelefones();
        this.email = dto.getEmail();
        this.horarioFuncionamento = dto.getHorarioFuncionamento();
        this.horarioAtendimento = dto.getHorarioAtendimento();
    }

    public UnidadeDeSaude(EstadualRequestDto dto) {
        this.ativo = true;
        this.nome = dto.getNome();
        this.tipo = dto.getTipo();
        this.estado = dto.getEstado();
        this.endereco = new Endereco(dto.getEndereco());
        this.saudeTelefones = dto.getTelefones();
        this.email = dto.getEmail();
        this.horarioFuncionamento = dto.getHorarioFuncionamento();
        this.horarioAtendimento = dto.getHorarioAtendimento();
    }

    public UnidadeDeSaude(MunicipalRequestDto dto) {
        this.ativo = true;
        this.nome = dto.getNome();
        this.tipo = dto.getTipo();
        this.municipio = dto.getMunicipio();
        this.endereco = new Endereco(dto.getEndereco());
        this.saudeTelefones = dto.getTelefones();
        this.email = dto.getEmail();
        this.horarioFuncionamento = dto.getHorarioFuncionamento();
        this.horarioAtendimento = dto.getHorarioAtendimento();
    }

    public UnidadeDeSaude(RegionalRequestDto dto) {
        this.ativo = true;
        this.nome = dto.getNome();
        this.tipo = dto.getTipo();
        this.regiao = dto.getRegiao();
        this.endereco = new Endereco(dto.getEndereco());
        this.saudeTelefones = dto.getTelefones();
        this.email = dto.getEmail();
        this.horarioFuncionamento = dto.getHorarioFuncionamento();
        this.horarioAtendimento = dto.getHorarioAtendimento();
    }

    public UnidadeDeSaude(UnidadeSaudeRequestDto dto) {
        this.ativo = true;
        this.nome = dto.getNome();
        this.tipo = dto.getTipo();
        this.responsavelCpf = dto.getResponsavelCpf();
        this.endereco = new Endereco(dto.getEndereco());
        this.saudeTelefones = dto.getTelefones();
        this.email = dto.getEmail();
        this.horarioFuncionamento = dto.getHorarioFuncionamento();
        this.horarioAtendimento = dto.getHorarioAtendimento();
    }
}
