package com.edufelizardo.API_Mais_Saude_Publica.model;

import com.edufelizardo.API_Mais_Saude_Publica.dtos.SegundoGrupoMedicalDto;
import com.edufelizardo.API_Mais_Saude_Publica.model.localizacao.Endereco;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.DayOfWeek;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
@Entity
@Table(name = "tb_Segundo_Grupo_Medical")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class SegundoGrupoMedical implements Serializable {
    @Serial
    private static final long serialVersionUID = -2812362626857335166L;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @ManyToOne
    @JoinColumn(name = "primeiro_grupo_medical_id")
    private PrimeiroGrupoMedical primeiroGrupoMedical;

    private String nome;

    @Embedded
    private Endereco endereco;

    @ElementCollection
    @CollectionTable(name = "tb_segundo_grupo_medical_telefones")
    private Set<String> Telefones;

    private String email;

    @ElementCollection
    @CollectionTable(name = "facility_horario_funcionamento_sgm")
    @MapKeyColumn(name = "day_of_week")
    @Column(name = "horario_funcionamento")
    private Map<DayOfWeek, String> horarioFuncionamento;

    @ElementCollection
    @CollectionTable(name = "facility_horario_atendimento_smg")
    @MapKeyColumn(name = "day_of_week")
    @Column(name = "horario_atendimento")
    private Map<DayOfWeek, String> horarioAtendimento;

    public SegundoGrupoMedical(SegundoGrupoMedicalDto dto, PrimeiroGrupoMedical medical) {
        this.primeiroGrupoMedical = medical;
        this.nome = dto.nome();
        this.endereco = new Endereco(dto.endereco());
        Telefones = dto.telefones();
        this.email = dto.email();
        this.horarioFuncionamento = dto.horarioFuncionamento();
        this.horarioAtendimento = dto.horarioAtendimento();
    }
}
