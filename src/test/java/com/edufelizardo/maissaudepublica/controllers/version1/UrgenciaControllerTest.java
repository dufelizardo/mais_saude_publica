package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.models.Paciente;
import com.edufelizardo.maissaudepublica.models.Profissional;
import com.edufelizardo.maissaudepublica.models.UnidadeDeSaude;
import com.edufelizardo.maissaudepublica.models.enuns.TipoUnidadeDeSaude;
import com.edufelizardo.maissaudepublica.repositories.PacienteRepository;
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
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Regulação de urgência (ADR-0176): o ciclo do chamado ao recebimento no destino, as regras da decisão e do deslocamento,
 * o painel em ordem, o prontuário e o acionamento do SAMU pela crise em saúde mental.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UrgenciaControllerTest {

    private static final String PREFIXO = "Urgencia Teste ";
    private static final String URL = "/api/v1/urgencia/";

    @Autowired private MockMvc mockMvc;
    @Autowired private JdbcTemplate jdbc;
    @Autowired private UnidadeDeSaudeRepository unidadeRepository;
    @Autowired private PacienteRepository pacienteRepository;
    @Autowired private ProfissionalRepository profissionalRepository;

    private UnidadeDeSaude central;
    private UnidadeDeSaude semCapacidade;
    private UnidadeDeSaude upa;
    private UnidadeDeSaude hospital;
    private UnidadeDeSaude ubs;
    private Paciente paciente;

    @BeforeEach
    void seed() {
        limpar();
        central = unidade("Central 192", TipoUnidadeDeSaude.UPA);
        semCapacidade = unidade("Sem capacidade", TipoUnidadeDeSaude.HOSPITAL);
        upa = unidade("UPA", TipoUnidadeDeSaude.UPA);
        hospital = unidade("Hospital", TipoUnidadeDeSaude.HOSPITAL);
        ubs = unidade("UBS", TipoUnidadeDeSaude.UBS);
        jdbc.update("insert into tb_unidade_capacidade_assistencial (uuid, unidade_id, capacidade_assistencial_id, inicio) select gen_random_uuid(), ?, uuid, ? "
                + "from tb_capacidade_assistencial where codigo = 'REGULACAO_MEDICA_URGENCIA'", central.getUuid(), LocalDate.now().minusDays(1));
        for (UnidadeDeSaude u : List.of(upa, hospital)) {
            jdbc.update("insert into tb_unidade_perfil_operacional (uuid, unidade_id, perfil_operacional_id, inicio) select gen_random_uuid(), ?, uuid, ? "
                    + "from tb_perfil_operacional where codigo = 'URGENCIA'", u.getUuid(), LocalDate.now().minusDays(1));
        }
        paciente = paciente("Paciente", "90076100001");
        profissional("UR-REGULADOR", "90076100009");
        profissional("UR-TARM", "90076100008");
    }

    @AfterEach
    void limpar() {
        String unidades = "(select uuid from tb_unidade_de_saude where nome like '" + PREFIXO + "%')";
        String ocorrencias = "(select uuid from tb_ocorrencia_urgencia where central_id in " + unidades + ")";
        jdbc.update("delete from tb_evento_urgencia where ocorrencia_id in " + ocorrencias);
        jdbc.update("delete from tb_ocorrencia_urgencia where central_id in " + unidades);
        jdbc.update("delete from tb_etapa_crise where crise_id in (select uuid from tb_evento_crise where unidade_id in " + unidades + ")");
        jdbc.update("delete from tb_evento_crise where unidade_id in " + unidades);
        jdbc.update("delete from tb_unidade_capacidade_assistencial where unidade_id in " + unidades);
        jdbc.update("delete from tb_unidade_perfil_operacional where unidade_id in " + unidades);
        jdbc.update("delete from tb_paciente where nome like '" + PREFIXO + "%'");
        jdbc.update("delete from tb_profissional where matricula like 'UR-%'");
        jdbc.update("delete from tb_unidade_de_saude where nome like '" + PREFIXO + "%'");
    }

    private UnidadeDeSaude unidade(String nome, TipoUnidadeDeSaude tipo) {
        UnidadeDeSaude u = new UnidadeDeSaude();
        u.setNome(PREFIXO + nome);
        u.setTipo(tipo);
        u.setAtivo(true);
        return unidadeRepository.save(u);
    }

    private Paciente paciente(String nome, String cpf) {
        Paciente p = new Paciente();
        p.setNome(PREFIXO + nome);
        p.setCpf(cpf);
        p.setAtivo(true);
        return pacienteRepository.save(p);
    }

    private Profissional profissional(String matricula, String cpf) {
        Profissional p = new Profissional();
        p.setMatricula(matricula);
        p.setCpf(cpf);
        p.setNome(PREFIXO + matricula);
        p.setAtivo(true);
        return profissionalRepository.save(p);
    }

    private String enviar(MockHttpServletRequestBuilder r, String corpo, int esperado) throws Exception {
        var resposta = mockMvc.perform(r.contentType(MediaType.APPLICATION_JSON).content(corpo)).andReturn().getResponse();
        assertThat(resposta.getStatus()).as(resposta.getContentAsString()).isEqualTo(esperado);
        return resposta.getContentAsString();
    }

    private String ler(String url) throws Exception {
        return mockMvc.perform(get(url)).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
    }

    private String chamado(UnidadeDeSaude c, int esperado) throws Exception {
        String r = enviar(post(URL + "ocorrencia"), ("{\"centralId\": \"%s\", \"origem\": \"CHAMADO_192\", \"solicitanteNome\": \"Vizinha\", "
                + "\"solicitanteTelefone\": \"11999990000\", \"descricaoPaciente\": \"Homem, cerca de 60 anos\", \"localTipo\": \"VIA_PUBLICA\", "
                + "\"endereco\": \"Rua A, 100\", \"queixa\": \"Caído na calçada, não responde.\", \"atendenteMatricula\": \"UR-TARM\"}")
                .formatted(c.getUuid()), esperado);
        return esperado == 201 ? JsonPath.read(r, "$.uuid") : r;
    }

    private String regular(String id, String corpo, int esperado) throws Exception {
        return enviar(post(URL + "ocorrencia/" + id + "/regulacao"), "{\"profissionalMatricula\": \"UR-REGULADOR\", \"conduta\": \"Avaliar no local.\", " + corpo + "}", esperado);
    }

    private String passo(String id, String tipo, int esperado) throws Exception {
        return enviar(post(URL + "ocorrencia/" + id + "/deslocamento"), "{\"tipo\": \"" + tipo + "\"}", esperado);
    }

    @Test
    void doChamadoAoRecebimentoNoDestino() throws Exception {
        String id = chamado(central, 201);
        String aberta = ler(URL + "ocorrencia/" + id);
        assertThat((String) JsonPath.read(aberta, "$.situacao")).isEqualTo("RECEBIDA");
        assertThat((String) JsonPath.read(aberta, "$.protocolo")).matches("\\d{4}-\\d{6}");
        assertThat((String) JsonPath.read(aberta, "$.queixa")).contains("não responde");

        regular(id, "\"prioridade\": \"VERMELHO\", \"decisao\": \"ENVIO_DE_RECURSO\", \"tipoRecurso\": \"USA\", \"recursoIdentificacao\": \"USA-01\"", 200);
        passo(id, "CHEGADA_AO_LOCAL", 422);
        String enviada = passo(id, "ENVIO_DE_RECURSO", 200);
        assertThat((String) JsonPath.read(enviada, "$.situacao")).isEqualTo("EM_ATENDIMENTO");
        passo(id, "ENVIO_DE_RECURSO", 409);
        passo(id, "CHEGADA_AO_LOCAL", 200);
        passo(id, "SAIDA_DO_LOCAL", 200);
        passo(id, "CHEGADA_AO_DESTINO", 422);

        String destino = "{\"profissionalMatricula\": \"UR-REGULADOR\", \"destinoId\": \"%s\", \"vagaZero\": true%s}";
        enviar(post(URL + "ocorrencia/" + id + "/destino"), destino.formatted(ubs.getUuid(), ", \"justificativaVagaZero\": \"Sem vaga\""), 422);
        enviar(post(URL + "ocorrencia/" + id + "/destino"), destino.formatted(hospital.getUuid(), ""), 400);
        enviar(post(URL + "ocorrencia/" + id + "/destino"), destino.formatted(hospital.getUuid(), ", \"justificativaVagaZero\": \"Sala vermelha cheia, risco de morte.\""), 200);
        String destinos = ler(URL + "destinos");
        assertThat((List<Integer>) JsonPath.read(destinos, "$[?(@.unidadeId == '" + hospital.getUuid() + "')].aCaminho")).containsExactly(1);

        enviar(post(URL + "ocorrencia/" + id + "/paciente"), "{\"pacienteId\": \"" + paciente.getUuid() + "\"}", 200);
        enviar(post(URL + "ocorrencia/" + id + "/paciente"), "{\"pacienteId\": \"" + paciente.getUuid() + "\"}", 409);
        String recebida = enviar(post(URL + "ocorrencia/" + id + "/recebimento"), "{\"observacao\": \"Sala vermelha.\"}", 200);
        assertThat((String) JsonPath.read(recebida, "$.situacao")).isEqualTo("CONCLUIDA");
        assertThat((String) JsonPath.read(recebida, "$.desfecho")).isEqualTo("RECEBIDO_NO_DESTINO");
        assertThat((Boolean) JsonPath.read(recebida, "$.vagaZero")).isTrue();
        assertThat((List<String>) JsonPath.read(recebida, "$.eventos[*].tipo")).containsExactly("RECEBIMENTO", "REGULACAO", "ENVIO_DE_RECURSO",
                "CHEGADA_AO_LOCAL", "SAIDA_DO_LOCAL", "DESTINO", "VINCULO_DO_PACIENTE", "CHEGADA_AO_DESTINO", "RECEBIMENTO_NO_DESTINO");
        assertThat((Integer) JsonPath.read(recebida, "$.minutosAteRegulacao")).isZero();
        assertThat((Integer) JsonPath.read(recebida, "$.minutosTotal")).isNotNull();

        String prontuario = ler("/api/v1/prontuario/" + paciente.getUuid());
        assertThat((List<String>) JsonPath.read(prontuario, "$.ocorrenciasUrgencia[*].uuid")).containsExactly(id);
    }

    @Test
    void regrasDaAberturaEDaDecisao() throws Exception {
        chamado(semCapacidade, 422);
        enviar(post(URL + "ocorrencia"), "{\"centralId\": \"%s\", \"origem\": \"CHAMADO_192\", \"queixa\": \"Dor no peito.\"}".formatted(central.getUuid()), 400);
        enviar(post(URL + "ocorrencia"), "{\"centralId\": \"%s\", \"origem\": \"UNIDADE_DE_SAUDE\", \"queixa\": \"Dor no peito.\"}".formatted(central.getUuid()), 400);
        enviar(post(URL + "ocorrencia"), "{\"centralId\": \"%s\", \"origem\": \"CRISE_SAUDE_MENTAL\", \"queixa\": \"Crise.\"}".formatted(central.getUuid()), 400);

        String id = chamado(central, 201);
        regular(id, "\"prioridade\": \"AMARELO\", \"decisao\": \"ENVIO_DE_RECURSO\"", 400);
        regular(id, "\"prioridade\": \"AMARELO\", \"decisao\": \"ENCAMINHAMENTO\", \"tipoRecurso\": \"USB\", \"destinoId\": \"" + upa.getUuid() + "\"", 400);
        regular(id, "\"prioridade\": \"AMARELO\", \"decisao\": \"TRANSFERENCIA\", \"destinoId\": \"" + upa.getUuid() + "\"", 422);
        regular(id, "\"prioridade\": \"AMARELO\", \"decisao\": \"ORIENTACAO\", \"destinoId\": \"" + upa.getUuid() + "\"", 400);
        String orientada = regular(id, "\"prioridade\": \"VERDE\", \"decisao\": \"ORIENTACAO\"", 200);
        assertThat((String) JsonPath.read(orientada, "$.situacao")).isEqualTo("CONCLUIDA");
        assertThat((String) JsonPath.read(orientada, "$.desfecho")).isEqualTo("ORIENTADO");
        regular(id, "\"prioridade\": \"VERDE\", \"decisao\": \"ORIENTACAO\"", 422);

        String transf = JsonPath.read(enviar(post(URL + "ocorrencia"), ("{\"centralId\": \"%s\", \"origem\": \"UNIDADE_DE_SAUDE\", \"unidadeSolicitanteId\": \"%s\", "
                + "\"queixa\": \"IAM, precisa de hemodinâmica.\"}").formatted(central.getUuid(), upa.getUuid()), 201), "$.uuid");
        regular(transf, "\"prioridade\": \"VERMELHO\", \"decisao\": \"TRANSFERENCIA\", \"destinoId\": \"" + upa.getUuid() + "\"", 422);
        String regulada = regular(transf, "\"prioridade\": \"VERMELHO\", \"decisao\": \"TRANSFERENCIA\", \"tipoRecurso\": \"USA\", \"destinoId\": \"" + hospital.getUuid() + "\"", 200);
        assertThat((String) JsonPath.read(regulada, "$.destinoNome")).isEqualTo(PREFIXO + "Hospital");
        enviar(post(URL + "ocorrencia/" + transf + "/recebimento"), "{}", 422);
        enviar(post(URL + "ocorrencia/" + transf + "/reclassificacao"), "{\"profissionalMatricula\": \"UR-REGULADOR\", \"prioridade\": \"VERMELHO\", \"motivo\": \"x\"}", 422);
        enviar(post(URL + "ocorrencia/" + transf + "/reclassificacao"), "{\"profissionalMatricula\": \"UR-REGULADOR\", \"prioridade\": \"AMARELO\", \"motivo\": \"Estável.\"}", 200);
        passo(transf, "ENVIO_DE_RECURSO", 200);
        enviar(post(URL + "ocorrencia/" + transf + "/cancelamento"), "{\"motivo\": \"Duplicada\"}", 422);
        String encerrar = "{\"profissionalMatricula\": \"UR-REGULADOR\", \"desfecho\": \"%s\", \"observacao\": \"Fim.\"}";
        enviar(post(URL + "ocorrencia/" + transf + "/encerramento"), encerrar.formatted("ATENDIDO_NO_LOCAL"), 422);
        enviar(post(URL + "ocorrencia/" + transf + "/encerramento"), encerrar.formatted("RECEBIDO_NO_DESTINO"), 422);
        enviar(post(URL + "ocorrencia/" + transf + "/encerramento"), encerrar.formatted("DESISTENCIA"), 200);

        String trote = chamado(central, 201);
        enviar(post(URL + "ocorrencia/" + trote + "/encerramento"), encerrar.formatted("RECUSA"), 422);
        enviar(post(URL + "ocorrencia/" + trote + "/encerramento"), encerrar.formatted("SEM_OCORRENCIA"), 200);
        String duplicada = chamado(central, 201);
        String cancelada = enviar(post(URL + "ocorrencia/" + duplicada + "/cancelamento"), "{\"motivo\": \"Duplicada\"}", 200);
        assertThat((String) JsonPath.read(cancelada, "$.situacao")).isEqualTo("CANCELADA");
    }

    @Test
    void painelPoeAguardandoPrimeiroEDepoisAGravidade() throws Exception {
        String amarela = chamado(central, 201);
        regular(amarela, "\"prioridade\": \"AMARELO\", \"decisao\": \"ENVIO_DE_RECURSO\", \"tipoRecurso\": \"USB\"", 200);
        String vermelha = chamado(central, 201);
        regular(vermelha, "\"prioridade\": \"VERMELHO\", \"decisao\": \"ENVIO_DE_RECURSO\", \"tipoRecurso\": \"USA\"", 200);
        String aguardando = chamado(central, 201);
        String painel = ler(URL + "painel?centralId=" + central.getUuid());
        assertThat((List<String>) JsonPath.read(painel, "$.abertas[*].uuid")).containsExactly(aguardando, vermelha, amarela);
        assertThat((Integer) JsonPath.read(painel, "$.aguardandoRegulacao")).isEqualTo(1);
        assertThat((Integer) JsonPath.read(painel, "$.porPrioridade.VERMELHO")).isEqualTo(1);
        assertThat((Integer) JsonPath.read(painel, "$.recebidasHoje")).isEqualTo(3);
        assertThat((Double) JsonPath.read(painel, "$.tempoMedioAteRegulacaoMinutos")).isZero();
        assertThat((List<String>) JsonPath.read(ler(URL + "centrais"), "$[*].nome")).contains(PREFIXO + "Central 192");
        assertThat((List<String>) JsonPath.read(ler(URL + "ocorrencia?centralId=" + central.getUuid() + "&situacao=REGULADA"), "$[*].uuid"))
                .containsExactlyInAnyOrder(vermelha, amarela);
    }

    @Test
    void crisePodeAcionarOSamu() throws Exception {
        String crise = JsonPath.read(enviar(post("/api/v1/saude-mental/crise"), ("{\"pacienteId\": \"%s\", \"unidadeId\": \"%s\", \"tipo\": \"AGITACAO_PSICOMOTORA\", "
                + "\"descricao\": \"Agitação intensa.\", \"localOcorrencia\": \"Recepção da UBS\"}").formatted(paciente.getUuid(), ubs.getUuid()), 201), "$.uuid");
        String corpo = "{\"centralId\": \"%s\", \"motivo\": \"Agitação com risco de queda; precisa de remoção.\"}";
        enviar(post("/api/v1/saude-mental/crise/" + crise + "/acionamento-samu"), corpo.formatted(semCapacidade.getUuid()), 422);
        String o = enviar(post("/api/v1/saude-mental/crise/" + crise + "/acionamento-samu"), corpo.formatted(central.getUuid()), 201);
        assertThat((String) JsonPath.read(o, "$.origem")).isEqualTo("CRISE_SAUDE_MENTAL");
        assertThat((String) JsonPath.read(o, "$.pacienteId")).isEqualTo(paciente.getUuid().toString());
        assertThat((String) JsonPath.read(o, "$.unidadeSolicitanteId")).isEqualTo(ubs.getUuid().toString());
        assertThat((String) JsonPath.read(o, "$.endereco")).isEqualTo("Recepção da UBS");
        enviar(post("/api/v1/saude-mental/crise/" + crise + "/acionamento-samu"), corpo.formatted(central.getUuid()), 409);
        String protocolo = JsonPath.read(o, "$.protocolo");
        assertThat(jdbc.queryForObject("select descricao from tb_etapa_crise where crise_id = cast(? as uuid) and etapa = 'ACIONAMENTO_DA_REDE'",
                String.class, crise)).contains(protocolo);
        // A transferência da unidade da crise para a urgência.
        String id = JsonPath.read(o, "$.uuid");
        regular(id, "\"prioridade\": \"AMARELO\", \"decisao\": \"TRANSFERENCIA\", \"tipoRecurso\": \"USB\", \"destinoId\": \"" + upa.getUuid() + "\"", 200);
    }
}
