package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.models.Internacao;
import com.edufelizardo.maissaudepublica.models.Leito;
import com.edufelizardo.maissaudepublica.models.Paciente;
import com.edufelizardo.maissaudepublica.models.Profissional;
import com.edufelizardo.maissaudepublica.models.Setor;
import com.edufelizardo.maissaudepublica.models.UnidadeDeSaude;
import com.edufelizardo.maissaudepublica.models.enuns.CaraterInternacao;
import com.edufelizardo.maissaudepublica.models.enuns.Sexo;
import com.edufelizardo.maissaudepublica.models.enuns.SexoLeito;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoLeito;
import com.edufelizardo.maissaudepublica.models.enuns.StatusInternacao;
import com.edufelizardo.maissaudepublica.models.enuns.TipoAlta;
import com.edufelizardo.maissaudepublica.models.enuns.TipoLeito;
import com.edufelizardo.maissaudepublica.models.enuns.TipoSetor;
import com.edufelizardo.maissaudepublica.models.enuns.TipoUnidadeDeSaude;
import com.edufelizardo.maissaudepublica.repositories.InternacaoRepository;
import com.edufelizardo.maissaudepublica.repositories.LeitoRepository;
import com.edufelizardo.maissaudepublica.repositories.PacienteRepository;
import com.edufelizardo.maissaudepublica.repositories.ProfissionalRepository;
import com.edufelizardo.maissaudepublica.repositories.SetorRepository;
import com.edufelizardo.maissaudepublica.repositories.UnidadeDeSaudeRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Duration;
import java.time.Instant;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Internação psiquiátrica (ADR-0123, na Internação da ADR-0098): modalidade, ordem judicial na compulsória e a
 * comunicação da involuntária ao Ministério Público em 72 horas da admissão e da alta, com a pendência à vista.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class InternacaoPsiquiatricaControllerTest {

    private static final String PREFIXO = "IntPsiq Teste ";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private UnidadeDeSaudeRepository unidadeDeSaudeRepository;

    @Autowired
    private PacienteRepository pacienteRepository;

    @Autowired
    private ProfissionalRepository profissionalRepository;

    @Autowired
    private SetorRepository setorRepository;

    @Autowired
    private LeitoRepository leitoRepository;

    @Autowired
    private InternacaoRepository internacaoRepository;

    private Internacao internacao;

    @BeforeEach
    void seed() {
        limpar();
        UnidadeDeSaude hospital = new UnidadeDeSaude();
        hospital.setNome(PREFIXO + "Hospital");
        hospital.setTipo(TipoUnidadeDeSaude.UBS);
        hospital.setAtivo(true);
        hospital = unidadeDeSaudeRepository.save(hospital);
        Profissional medico = new Profissional();
        medico.setMatricula("IPQ-MED");
        medico.setCpf("90099900001");
        medico.setNome(PREFIXO + "Médico");
        medico.setAtivo(true);
        medico = profissionalRepository.save(medico);
        Setor setor = setorRepository.save(new Setor(hospital, PREFIXO + "Psiquiatria", "IPQ-PS", TipoSetor.ASSISTENCIAL, true, null));
        Leito leito = new Leito();
        leito.setUnidade(hospital);
        leito.setSetor(setor);
        leito.setIdentificacao("PQ 01");
        leito.setTipo(TipoLeito.CLINICO);
        leito.setSexo(SexoLeito.MISTO);
        leito.setSituacao(SituacaoLeito.OCUPADO);
        leito.setAtivo(true);
        leito = leitoRepository.save(leito);
        Paciente paciente = new Paciente();
        paciente.setNome(PREFIXO + "Paciente");
        paciente.setSexo(Sexo.MASCULINO);
        paciente.setAtivo(true);
        paciente = pacienteRepository.save(paciente);
        Internacao i = new Internacao();
        i.setPaciente(paciente);
        i.setUnidade(hospital);
        i.setLeito(leito);
        i.setMedicoResponsavel(medico);
        i.setCid("F20");
        i.setMotivo("Episódio psicótico com risco a si.");
        i.setCarater(CaraterInternacao.URGENCIA);
        i.setAdmitidaEm(Instant.now().minus(Duration.ofHours(80)));
        i.setStatus(StatusInternacao.INTERNADO);
        internacao = internacaoRepository.save(i);
    }

    @AfterEach
    void limpar() {
        String unidades = "(select uuid from tb_unidade_de_saude where nome like '" + PREFIXO + "%')";
        jdbc.update("delete from tb_comunicacao_ministerio_publico where internacao_id in (select uuid from tb_internacao where unidade_id in " + unidades + ")");
        jdbc.update("delete from tb_evento_leito where leito_id in (select uuid from tb_leito where unidade_id in " + unidades + ")");
        jdbc.update("delete from tb_internacao where unidade_id in " + unidades);
        jdbc.update("delete from tb_leito where unidade_id in " + unidades);
        jdbc.update("delete from tb_setor where unidade_id in " + unidades);
        jdbc.update("delete from tb_paciente where nome like '" + PREFIXO + "%'");
        jdbc.update("delete from tb_profissional where matricula = 'IPQ-MED'");
        jdbc.update("delete from tb_unidade_de_saude where nome like '" + PREFIXO + "%'");
    }

    @Test
    void involuntariaComunicadaAoMinisterioPublicoNaAdmissaoENaAlta() throws Exception {
        String base = "/api/v1/internacao/" + internacao.getUuid();
        mockMvc.perform(post(base + "/modalidade").contentType(MediaType.APPLICATION_JSON).content("{\"modalidade\": \"COMPULSORIA\"}"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(post(base + "/comunicacao-mp").contentType(MediaType.APPLICATION_JSON).content("{\"momento\": \"ADMISSAO\"}"))
                .andExpect(status().isUnprocessableEntity());
        // Admitida há 80 horas, involuntária sem comunicação: pendente e vencida.
        mockMvc.perform(post(base + "/modalidade").contentType(MediaType.APPLICATION_JSON).content("{\"modalidade\": \"INVOLUNTARIA\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.modalidade").value("INVOLUNTARIA"))
                .andExpect(jsonPath("$.comunicacaoMpPendente").value("ADMISSAO"))
                .andExpect(jsonPath("$.comunicacaoMpVencida").value(true));
        mockMvc.perform(get("/api/v1/internacao/").param("comunicacaoMpPendente", "true")).andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.uuid == '%s')]".formatted(internacao.getUuid())).isNotEmpty());
        mockMvc.perform(post(base + "/comunicacao-mp").contentType(MediaType.APPLICATION_JSON).content("{\"momento\": \"ALTA\"}"))
                .andExpect(status().isUnprocessableEntity());
        mockMvc.perform(post(base + "/comunicacao-mp").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"momento\": \"ADMISSAO\", \"responsavelMatricula\": \"IPQ-MED\", \"protocolo\": \"MP-2026-001\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.comunicacoesMp.length()").value(1))
                .andExpect(jsonPath("$.comunicacaoMpPendente").doesNotExist());
        mockMvc.perform(post(base + "/comunicacao-mp").contentType(MediaType.APPLICATION_JSON).content("{\"momento\": \"ADMISSAO\"}"))
                .andExpect(status().isUnprocessableEntity());

        // A alta não é bloqueada; a comunicação da alta fica pendente, no prazo.
        mockMvc.perform(post(base + "/alta").contentType(MediaType.APPLICATION_JSON).content("""
                        {"tipoAlta": "%s", "sumario": "Remissão dos sintomas; segue no CAPS.", "medicoMatricula": "IPQ-MED"}"""
                        .formatted(TipoAlta.values()[0])))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.comunicacaoMpPendente").value("ALTA"))
                .andExpect(jsonPath("$.comunicacaoMpVencida").value(false));
        mockMvc.perform(post(base + "/comunicacao-mp").contentType(MediaType.APPLICATION_JSON).content("{\"momento\": \"ALTA\", \"responsavelMatricula\": \"IPQ-MED\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.comunicacoesMp.length()").value(2))
                .andExpect(jsonPath("$.comunicacaoMpPendente").doesNotExist());
    }

    @Test
    void compulsoriaComOrdemJudicial() throws Exception {
        mockMvc.perform(post("/api/v1/internacao/" + internacao.getUuid() + "/modalidade").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"modalidade\": \"COMPULSORIA\", \"ordemJudicial\": \"0001234-56.2026.8.26.0100\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ordemJudicial").value("0001234-56.2026.8.26.0100"))
                .andExpect(jsonPath("$.comunicacaoMpPendente").doesNotExist());
    }
}
