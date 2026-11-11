package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.models.Profissional;
import com.edufelizardo.maissaudepublica.models.UnidadeDeSaude;
import com.edufelizardo.maissaudepublica.models.enuns.TipoUnidadeDeSaude;
import com.edufelizardo.maissaudepublica.repositories.ProfissionalRepository;
import com.edufelizardo.maissaudepublica.repositories.UnidadeDeSaudeRepository;
import com.jayway.jsonpath.JsonPath;
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
import org.springframework.test.web.servlet.ResultActions;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Ficha clínica do atendimento pré-hospitalar (ADR-0188), com login e autorização desligados. O escopo fica em
 * {@link FichaAphAutorizacaoControllerTest}.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class FichaAphControllerTest {

    private static final String URGENCIA = "/api/v1/urgencia/";
    private static final String APH = "/api/v1/aph/";
    private static final String PREFIXO = "FichaAph Teste ";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private UnidadeDeSaudeRepository unidadeRepository;

    @Autowired
    private ProfissionalRepository profissionalRepository;

    private UnidadeDeSaude central;

    @BeforeEach
    void seed() {
        limpar();
        central = new UnidadeDeSaude();
        central.setNome(PREFIXO + "Central 192");
        central.setTipo(TipoUnidadeDeSaude.UPA);
        central.setAtivo(true);
        central = unidadeRepository.save(central);
        jdbc.update("insert into tb_unidade_capacidade_assistencial (uuid, unidade_id, capacidade_assistencial_id, inicio) select gen_random_uuid(), ?, uuid, ? "
                + "from tb_capacidade_assistencial where codigo = 'REGULACAO_MEDICA_URGENCIA'", central.getUuid(), LocalDate.now().minusDays(1));
        for (String[] p : new String[][]{{"FA-TARM", "90088100001"}, {"FA-REG", "90088100002"}, {"FA-MED", "90088100003"}, {"FA-ENF", "90088100004"}}) {
            Profissional prof = new Profissional();
            prof.setMatricula(p[0]);
            prof.setCpf(p[1]);
            prof.setNome(PREFIXO + p[0]);
            prof.setAtivo(true);
            profissionalRepository.save(prof);
        }
    }

    @AfterEach
    void limpar() {
        String unidades = "(select uuid from tb_unidade_de_saude where nome like '" + PREFIXO + "%')";
        String ocorrencias = "(select uuid from tb_ocorrencia_urgencia where central_id in " + unidades + ")";
        String fichas = "(select uuid from tb_ficha_aph where ocorrencia_id in " + ocorrencias + ")";
        jdbc.update("delete from tb_adendo_ficha_aph where ficha_id in " + fichas);
        jdbc.update("delete from tb_medicamento_aph where ficha_id in " + fichas);
        jdbc.update("delete from tb_sinais_vitais_aph where ficha_id in " + fichas);
        jdbc.update("delete from tb_ficha_aph_procedimento where ficha_id in " + fichas);
        jdbc.update("delete from tb_ficha_aph where ocorrencia_id in " + ocorrencias);
        jdbc.update("delete from tb_evento_urgencia where ocorrencia_id in " + ocorrencias);
        jdbc.update("delete from tb_ocorrencia_urgencia where central_id in " + unidades);
        jdbc.update("delete from tb_unidade_capacidade_assistencial where unidade_id in " + unidades);
        jdbc.update("delete from tb_profissional where matricula like 'FA-%'");
        jdbc.update("delete from tb_unidade_de_saude where nome like '" + PREFIXO + "%'");
    }

    @Test
    void fichaSoComRecursoEnviadoEAtendimentosDaEquipe() throws Exception {
        String semRecurso = chamado();
        ficha(semRecurso, "{\"natureza\": \"CLINICA\"}").andExpect(status().isUnprocessableEntity());
        String comRecurso = comRecurso();
        mockMvc.perform(get(APH + "ocorrencia/" + comRecurso + "/ficha")).andExpect(status().isNotFound());
        mockMvc.perform(get(APH + "atendimentos?centralId=" + central.getUuid()))
                .andExpect(jsonPath("$[*].ocorrenciaId").value(org.hamcrest.Matchers.contains(comRecurso)))
                .andExpect(jsonPath("$[0].recurso").value("USA USA-01"))
                .andExpect(jsonPath("$[0].fichaId").isEmpty());
        ficha(comRecurso, """
                {"natureza": "TRAUMA", "mecanismoTrauma": "ACIDENTE_MOTO", "situacaoEncontrada": "Motociclista caído, consciente.",
                 "hemorragiaExsanguinante": false, "glasgowOcular": 4, "glasgowVerbal": 5, "glasgowMotor": 6,
                 "procedimentos": ["COLAR_CERVICAL", "PRANCHA_IMOBILIZACAO", "OUTRO"]}
                """).andExpect(status().isBadRequest());
        ficha(comRecurso, "{\"glasgowOcular\": 5}").andExpect(status().isBadRequest());
        ficha(comRecurso, """
                {"natureza": "TRAUMA", "mecanismoTrauma": "ACIDENTE_MOTO", "situacaoEncontrada": "Motociclista caído, consciente.",
                 "hemorragiaExsanguinante": false, "glasgowOcular": 4, "glasgowVerbal": 5, "glasgowMotor": 6,
                 "procedimentos": ["COLAR_CERVICAL", "PRANCHA_IMOBILIZACAO"], "cid": "S82.2"}
                """).andExpect(status().isOk())
                .andExpect(jsonPath("$.glasgowTotal").value(15))
                .andExpect(jsonPath("$.cid").value("S822"))
                .andExpect(jsonPath("$.procedimentos", hasSize(2)))
                .andExpect(jsonPath("$.finalizada").value(false));
        mockMvc.perform(get(APH + "atendimentos")).andExpect(jsonPath("$[?(@.ocorrenciaId == '%s')].fichaId".formatted(comRecurso)).isNotEmpty());
    }

    @Test
    void sinaisEmSerieMedicamentosFinalizacaoEAdendo() throws Exception {
        String ocorrencia = comRecurso();
        String r = ficha(ocorrencia, "{\"natureza\": \"CLINICA\", \"desfecho\": \"REMOVIDO_AO_DESTINO\"}").andReturn().getResponse().getContentAsString();
        String id = JsonPath.read(r, "$.uuid");
        acao(id, "sinais-vitais", "{\"profissionalMatricula\": \"FA-ENF\"}").andExpect(status().isBadRequest());
        acao(id, "sinais-vitais", "{\"profissionalMatricula\": \"FA-ENF\", \"saturacao\": 120}").andExpect(status().isBadRequest());
        acao(id, "sinais-vitais", "{\"profissionalMatricula\": \"FA-ENF\", \"saturacao\": 90, \"em\": \"%s\"}".formatted(Instant.now().plus(Duration.ofHours(1))))
                .andExpect(status().isUnprocessableEntity());
        acao(id, "sinais-vitais", "{\"profissionalMatricula\": \"FA-ENF\", \"paSistolica\": 90, \"paDiastolica\": 60, \"frequenciaCardiaca\": 120, \"saturacao\": 89, \"em\": \"%s\"}"
                .formatted(Instant.now().minus(Duration.ofMinutes(20)))).andExpect(status().isOk());
        acao(id, "sinais-vitais", "{\"profissionalMatricula\": \"FA-ENF\", \"paSistolica\": 110, \"paDiastolica\": 70, \"saturacao\": 96, \"temperatura\": 36.8, \"dor\": 3, \"glasgow\": 15}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sinaisVitais", hasSize(2)))
                .andExpect(jsonPath("$.sinaisVitais[0].frequenciaCardiaca").value(120));
        acao(id, "medicamentos", "{\"profissionalMatricula\": \"FA-MED\", \"nome\": \"Soro fisiológico 0,9%\"}").andExpect(status().isBadRequest());
        acao(id, "medicamentos", "{\"profissionalMatricula\": \"FA-MED\", \"nome\": \"Soro fisiológico 0,9%\", \"dose\": \"500 ml\", \"via\": \"INTRAVENOSA\"}")
                .andExpect(status().isOk()).andExpect(jsonPath("$.medicamentos", hasSize(1)));

        acao(id, "adendo", "{\"profissionalMatricula\": \"FA-MED\", \"texto\": \"Cedo demais.\"}").andExpect(status().isUnprocessableEntity());
        acao(id, "finalizacao", "{\"profissionalMatricula\": \"FA-MED\"}").andExpect(status().isUnprocessableEntity());
        ficha(ocorrencia, "{\"natureza\": \"CLINICA\", \"desfecho\": \"REMOVIDO_AO_DESTINO\", \"recebidoPor\": \"Enf. Clara\", \"entregueEm\": \"%s\"}"
                .formatted(Instant.now())).andExpect(status().isOk());
        acao(id, "finalizacao", "{\"profissionalMatricula\": \"FA-MED\"}").andExpect(status().isOk())
                .andExpect(jsonPath("$.finalizada").value(true)).andExpect(jsonPath("$.finalizadaPorNome").value(PREFIXO + "FA-MED"));
        ficha(ocorrencia, "{\"natureza\": \"TRAUMA\"}").andExpect(status().isUnprocessableEntity());
        acao(id, "sinais-vitais", "{\"profissionalMatricula\": \"FA-ENF\", \"saturacao\": 95}").andExpect(status().isUnprocessableEntity());
        acao(id, "adendo", "{\"profissionalMatricula\": \"FA-MED\"}").andExpect(status().isBadRequest());
        acao(id, "adendo", "{\"profissionalMatricula\": \"FA-MED\", \"texto\": \"Paciente informou alergia a dipirona depois da entrega.\"}")
                .andExpect(status().isOk()).andExpect(jsonPath("$.adendos", hasSize(1)));
        mockMvc.perform(get(APH + "ficha/" + id + "/ficha.pdf")).andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_PDF));
    }

    @Test
    void obitoERecusaPedemOsDadosParaFinalizar() throws Exception {
        String obito = comRecurso();
        String id = JsonPath.read(ficha(obito, "{\"natureza\": \"CLINICA\", \"desfecho\": \"OBITO_NO_LOCAL\"}").andReturn().getResponse().getContentAsString(), "$.uuid");
        acao(id, "finalizacao", "{\"profissionalMatricula\": \"FA-MED\"}").andExpect(status().isUnprocessableEntity());
        ficha(obito, "{\"natureza\": \"CLINICA\", \"desfecho\": \"OBITO_NO_LOCAL\", \"obitoEm\": \"%s\", \"obitoConstatadoPorMatricula\": \"FA-MED\"}"
                .formatted(Instant.now().minus(Duration.ofMinutes(5)))).andExpect(status().isOk())
                .andExpect(jsonPath("$.obitoConstatadoPorNome").value(PREFIXO + "FA-MED"));
        acao(id, "finalizacao", "{\"profissionalMatricula\": \"FA-MED\"}").andExpect(status().isOk());

        String recusa = comRecurso();
        String id2 = JsonPath.read(ficha(recusa, "{\"natureza\": \"CLINICA\", \"desfecho\": \"RECUSA_DE_REMOCAO\"}").andReturn().getResponse().getContentAsString(), "$.uuid");
        acao(id2, "finalizacao", "{\"profissionalMatricula\": \"FA-MED\"}").andExpect(status().isUnprocessableEntity());
        ficha(recusa, "{\"natureza\": \"CLINICA\", \"desfecho\": \"RECUSA_DE_REMOCAO\", \"recusaNome\": \"O próprio paciente\"}").andExpect(status().isOk());
        acao(id2, "finalizacao", "{\"profissionalMatricula\": \"FA-MED\"}").andExpect(status().isOk());
    }

    // ── Apoio ──────────────────────────────────────────────────────────────────────────────────

    private String chamado() throws Exception {
        String r = mockMvc.perform(post(URGENCIA + "ocorrencia").contentType(MediaType.APPLICATION_JSON).content(("{\"centralId\": \"%s\", \"origem\": \"CHAMADO_192\", "
                        + "\"solicitanteNome\": \"Vizinha\", \"solicitanteTelefone\": \"11999990000\", \"descricaoPaciente\": \"Homem, cerca de 40 anos\", "
                        + "\"localTipo\": \"VIA_PUBLICA\", \"endereco\": \"Rua A, 100\", \"queixa\": \"Queda de moto.\", \"atendenteMatricula\": \"FA-TARM\"}")
                        .formatted(central.getUuid())))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return JsonPath.read(r, "$.uuid");
    }

    private String comRecurso() throws Exception {
        String id = chamado();
        mockMvc.perform(post(URGENCIA + "ocorrencia/" + id + "/regulacao").contentType(MediaType.APPLICATION_JSON).content(
                        "{\"profissionalMatricula\": \"FA-REG\", \"conduta\": \"Avaliar no local.\", \"prioridade\": \"VERMELHO\", \"decisao\": \"ENVIO_DE_RECURSO\", "
                                + "\"tipoRecurso\": \"USA\", \"recursoIdentificacao\": \"USA-01\"}"))
                .andExpect(status().isOk());
        return id;
    }

    private ResultActions ficha(String ocorrencia, String corpo) throws Exception {
        return mockMvc.perform(put(APH + "ocorrencia/" + ocorrencia + "/ficha").contentType(MediaType.APPLICATION_JSON).content(corpo));
    }

    private ResultActions acao(String ficha, String rota, String corpo) throws Exception {
        return mockMvc.perform(post(APH + "ficha/" + ficha + "/" + rota).contentType(MediaType.APPLICATION_JSON).content(corpo));
    }
}
