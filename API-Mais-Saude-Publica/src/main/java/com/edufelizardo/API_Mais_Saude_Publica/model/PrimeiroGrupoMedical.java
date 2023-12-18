package com.edufelizardo.API_Mais_Saude_Publica.model;

//import com.edufelizardo.API_Mais_Saude_Publica.dtos.MinisterioSaudeDto;

import com.edufelizardo.API_Mais_Saude_Publica.dtos.PrimeiroGrupoMedicalDto;
import com.edufelizardo.API_Mais_Saude_Publica.model.localizacao.Endereco;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.DayOfWeek;
import java.util.Map;
import java.util.UUID;

/**
 * @author Eduardo Felizardo Cândido
 * @version 0.00.00
 * @apiNote Mais Saúde Pública 12/2023
 */
@Entity
@Table(name = "tb_Primeiro_Grupo_Medical")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class PrimeiroGrupoMedical implements Serializable {
    @Serial
    private static final long serialVersionUID = -7237337837935448377L;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;
    private String nomeInstitucional;
    @Embedded
    private Endereco endereco;
    @ElementCollection
    @CollectionTable(name = "facility_horario_funcionamento")
    @MapKeyColumn(name = "day_of_week")
    @Column(name = "horario_funcionamento")
    private Map<DayOfWeek, String> horarioFuncionamento;
    @ElementCollection
    @CollectionTable(name = "facility_horario_atendimento")
    @MapKeyColumn(name = "day_of_week")
    @Column(name = "horario_atendimento")
    private Map<DayOfWeek, String> horarioAtendimento;

    public PrimeiroGrupoMedical(PrimeiroGrupoMedicalDto dto) {
        this.nomeInstitucional = dto.nomeInstitucional();
        this.endereco = new Endereco(dto.endereco());
        this.horarioFuncionamento = dto.horarioFuncionamento();
        this.horarioAtendimento = dto.horarioAtendimento();
    }
}
