package com.edufelizardo.API_Mais_Saude_Publica.model.segundo_departamento_operacoes;

import com.edufelizardo.API_Mais_Saude_Publica.dtos.PrimeiroDepartOrganizacionalTGMDto;
import com.edufelizardo.API_Mais_Saude_Publica.dtos.SegundoDepartOrganizacionalTGMDto;
import com.edufelizardo.API_Mais_Saude_Publica.dtos.conversors.SegundoGrupoMedicalConverterDto;
import com.edufelizardo.API_Mais_Saude_Publica.model.localizacao.Endereco;
import com.edufelizardo.API_Mais_Saude_Publica.model.primeiro_departamento_operacoes.PrimeiroDepartOrganizacionalTGM;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.DayOfWeek;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "tb_Segundo_Depart_Op_TGM")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class SegundoDepartOrganizacionalTGM implements Serializable {
    @Serial
    private static final long serialVersionUID = -6399745948496779123L;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @ManyToOne
    @JoinColumn(name = "primeiro_Departamento_Organizacional_TGM")
    private PrimeiroDepartOrganizacionalTGM primeiroDepartOrganizacionalTGM;

    private String nome;

    @Embedded
    private Endereco endereco;

    @ElementCollection
    @CollectionTable(name = "tb_seg_depsrt_oper_tgm_telefones")
    private Set<String> Telefones;

    private String email;

    @ElementCollection
    @CollectionTable(name = "facility_horario_funcionamento_sdotgm")
    @MapKeyColumn(name = "day_of_week")
    @Column(name = "horario_funcionamento")
    private Map<DayOfWeek, String> horarioFuncionamento;

    @ElementCollection
    @CollectionTable(name = "facility_horario_atendimento_sdotgm")
    @MapKeyColumn(name = "day_of_week")
    @Column(name = "horario_atendimento")
    private Map<DayOfWeek, String> horarioAtendimento;

    public SegundoDepartOrganizacionalTGM(SegundoDepartOrganizacionalTGMDto dto, PrimeiroDepartOrganizacionalTGM tgm) {
        this.primeiroDepartOrganizacionalTGM = tgm;
        this.nome = dto.nome();
        this.endereco = new Endereco(dto.endereco());
        Telefones = dto.telefones();
        this.email = dto.email();
        this.horarioFuncionamento = dto.horarioFuncionamento();
        this.horarioAtendimento = dto.horarioAtendimento();
    }
}
