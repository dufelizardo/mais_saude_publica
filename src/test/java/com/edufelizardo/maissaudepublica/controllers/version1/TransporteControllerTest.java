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
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

/**
 * Transporte, T1 (ADR-0178): a frota (placa, prefixo, lotação da viatura do SAMU), a situação com histórico, o plantão com a tripulação
 * mínima e os conflitos, e a viatura empenhada pela regulação de urgência, em serviço no envio e livre no fim.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class TransporteControllerTest {

    private static final String PREFIXO = "Transporte Teste ";
    private static final String URL = "/api/v1/transporte/";
    private static final String URG = "/api/v1/urgencia/";

    @Autowired private MockMvc mockMvc;
    @Autowired private JdbcTemplate jdbc;
    @Autowired private UnidadeDeSaudeRepository unidadeRepository;
    @Autowired private ProfissionalRepository profissionalRepository;

    private UnidadeDeSaude base;
    private UnidadeDeSaude ubs;

    @BeforeEach
    void seed() {
        limpar();
        base = unidade("Base SAMU", TipoUnidadeDeSaude.UPA);
        ubs = unidade("UBS", TipoUnidadeDeSaude.UBS);
        jdbc.update("insert into tb_unidade_perfil_operacional (uuid, unidade_id, perfil_operacional_id, inicio) select gen_random_uuid(), ?, uuid, ? "
                + "from tb_perfil_operacional where codigo = 'ATENCAO_MOVEL'", base.getUuid(), LocalDate.now().minusDays(1));
        jdbc.update("insert into tb_unidade_capacidade_assistencial (uuid, unidade_id, capacidade_assistencial_id, inicio) select gen_random_uuid(), ?, uuid, ? "
                + "from tb_capacidade_assistencial where codigo = 'REGULACAO_MEDICA_URGENCIA'", base.getUuid(), LocalDate.now().minusDays(1));
        for (String m : List.of("TR-CONDUTOR", "TR-TECNICO", "TR-ENFERMEIRO", "TR-MEDICO", "TR-CONDUTOR2", "TR-TECNICO2")) {
            Profissional p = new Profissional();
            p.setMatricula(m);
            p.setCpf("9007" + String.format("%07d", Math.abs(m.hashCode()) % 10_000_000));
            p.setNome(PREFIXO + m);
            p.setAtivo(true);
            profissionalRepository.save(p);
        }
    }

    @AfterEach
    void limpar() {
        String unidades = "(select uuid from tb_unidade_de_saude where nome like '" + PREFIXO + "%')";
        String veiculos = "(select uuid from tb_veiculo where unidade_id in " + unidades + ")";
        jdbc.update("delete from tb_evento_urgencia where ocorrencia_id in (select uuid from tb_ocorrencia_urgencia where central_id in " + unidades + ")");
        jdbc.update("delete from tb_ocorrencia_urgencia where central_id in " + unidades);
        jdbc.update("delete from tb_tripulante_plantao where plantao_id in (select uuid from tb_plantao_viatura where veiculo_id in " + veiculos + ")");
        jdbc.update("delete from tb_plantao_viatura where veiculo_id in " + veiculos);
        jdbc.update("delete from tb_situacao_veiculo where veiculo_id in " + veiculos);
        jdbc.update("delete from tb_veiculo where unidade_id in " + unidades);
        jdbc.update("delete from tb_unidade_capacidade_assistencial where unidade_id in " + unidades);
        jdbc.update("delete from tb_unidade_perfil_operacional where unidade_id in " + unidades);
        jdbc.update("delete from tb_profissional where matricula like 'TR-%'");
        jdbc.update("delete from tb_unidade_de_saude where nome like '" + PREFIXO + "%'");
    }

    private UnidadeDeSaude unidade(String nome, TipoUnidadeDeSaude tipo) {
        UnidadeDeSaude u = new UnidadeDeSaude();
        u.setNome(PREFIXO + nome);
        u.setTipo(tipo);
        u.setAtivo(true);
        return unidadeRepository.save(u);
    }

    private String enviar(MockHttpServletRequestBuilder r, String corpo, int esperado) throws Exception {
        var resposta = mockMvc.perform(r.contentType(MediaType.APPLICATION_JSON).content(corpo)).andReturn().getResponse();
        assertThat(resposta.getStatus()).as(resposta.getContentAsString()).isEqualTo(esperado);
        return resposta.getContentAsString();
    }

    private String ler(String url) throws Exception {
        var resposta = mockMvc.perform(get(url)).andReturn().getResponse();
        assertThat(resposta.getStatus()).as(resposta.getContentAsString()).isEqualTo(200);
        return resposta.getContentAsString();
    }

    private String veiculo(String placa, String prefixo, String tipo, UnidadeDeSaude u, int esperado) throws Exception {
        String r = enviar(post(URL + "veiculo"), "{\"placa\": \"%s\", \"prefixo\": \"%s\", \"tipo\": \"%s\", \"unidadeId\": \"%s\", \"maca\": true, \"quilometragem\": 1000}"
                .formatted(placa, prefixo, tipo, u.getUuid()), esperado);
        return esperado == 201 ? JsonPath.read(r, "$.uuid") : r;
    }

    private String plantao(String veiculo, Instant inicio, Instant fim, String tripulacao, int esperado) throws Exception {
        return enviar(post(URL + "plantao"), "{\"veiculoId\": \"%s\", \"inicio\": \"%s\", \"fim\": \"%s\", \"tripulacao\": [%s]}"
                .formatted(veiculo, inicio, fim, tripulacao), esperado);
    }

    private static String t(String matricula, String funcao) {
        return "{\"matricula\": \"%s\", \"funcao\": \"%s\"}".formatted(matricula, funcao);
    }

    @Test
    void frotaESituacao() throws Exception {
        veiculo("ABC12", "TR-X", "USB", base, 400);
        veiculo("TRA1B23", "TR-USB-01", "USB", ubs, 422);
        String usb = veiculo("tra-1b23", "tr-usb-01", "USB", base, 201);
        veiculo("TRA1B23", "TR-USB-02", "USB", base, 409);
        veiculo("TRB2C34", "TR-USB-01", "USB", base, 409);
        String van = veiculo("TRC3456", "TR-VAN-01", "VAN_SANITARIA", ubs, 201);
        String lido = ler(URL + "veiculo/" + usb);
        assertThat((String) JsonPath.read(lido, "$.placa")).isEqualTo("TRA1B23");
        assertThat((String) JsonPath.read(lido, "$.prefixo")).isEqualTo("TR-USB-01");
        assertThat((String) JsonPath.read(lido, "$.situacao")).isEqualTo("DISPONIVEL");

        String situacao = "{\"situacao\": \"%s\", \"motivo\": \"%s\"%s}";
        enviar(post(URL + "veiculo/" + van + "/situacao"), situacao.formatted("EM_SERVICO", "x", ""), 400);
        enviar(post(URL + "veiculo/" + van + "/situacao"), situacao.formatted("DISPONIVEL", "x", ""), 422);
        enviar(post(URL + "veiculo/" + van + "/situacao"), situacao.formatted("MANUTENCAO", "Troca de pneus.", ", \"quilometragem\": 900"), 422);
        String manut = enviar(post(URL + "veiculo/" + van + "/situacao"), situacao.formatted("MANUTENCAO", "Troca de pneus.", ", \"quilometragem\": 1200"), 200);
        assertThat((List<String>) JsonPath.read(manut, "$.historico[*].situacao")).containsExactly("MANUTENCAO", "DISPONIVEL");
        assertThat((Integer) JsonPath.read(manut, "$.quilometragem")).isEqualTo(1200);
        enviar(put(URL + "veiculo/" + van), "{\"placa\": \"TRC3456\", \"prefixo\": \"TR-VAN-01\", \"tipo\": \"VAN_SANITARIA\", \"unidadeId\": \"%s\", \"quilometragem\": 100}"
                .formatted(ubs.getUuid()), 422);
        assertThat((List<String>) JsonPath.read(ler(URL + "veiculo?unidadeId=" + base.getUuid()), "$[*].prefixo")).containsExactly("TR-USB-01");
    }

    @Test
    void plantaoComTripulacaoMinimaEConflitos() throws Exception {
        String usb = veiculo("TRD4E56", "TR-USB-11", "USB", base, 201);
        String usa = veiculo("TRE5F67", "TR-USA-11", "USA", base, 201);
        Instant inicio = Instant.now().truncatedTo(ChronoUnit.MINUTES).minus(1, ChronoUnit.HOURS);
        Instant fim = inicio.plus(12, ChronoUnit.HOURS);
        plantao(usb, inicio, inicio.plus(25, ChronoUnit.HOURS), t("TR-CONDUTOR", "CONDUTOR") + "," + t("TR-TECNICO", "TECNICO_ENFERMAGEM"), 400);
        plantao(usb, fim, inicio, t("TR-CONDUTOR", "CONDUTOR"), 400);
        plantao(usb, inicio, fim, t("TR-CONDUTOR", "CONDUTOR"), 422);
        plantao(usa, inicio, fim, t("TR-CONDUTOR2", "CONDUTOR") + "," + t("TR-ENFERMEIRO", "ENFERMEIRO"), 422);
        plantao(usb, inicio, fim, t("TR-CONDUTOR", "CONDUTOR") + "," + t("TR-CONDUTOR", "TECNICO_ENFERMAGEM"), 400);
        String p = plantao(usb, inicio, fim, t("TR-CONDUTOR", "CONDUTOR") + "," + t("TR-TECNICO", "TECNICO_ENFERMAGEM"), 201);
        assertThat((Boolean) JsonPath.read(p, "$.vigente")).isTrue();
        plantao(usb, inicio.plus(2, ChronoUnit.HOURS), fim, t("TR-CONDUTOR2", "CONDUTOR") + "," + t("TR-TECNICO2", "TECNICO_ENFERMAGEM"), 409);
        plantao(usa, inicio, fim, t("TR-CONDUTOR", "CONDUTOR") + "," + t("TR-ENFERMEIRO", "ENFERMEIRO") + "," + t("TR-MEDICO", "MEDICO"), 409);
        plantao(usa, inicio, fim, t("TR-CONDUTOR2", "CONDUTOR") + "," + t("TR-ENFERMEIRO", "ENFERMEIRO") + "," + t("TR-MEDICO", "MEDICO"), 201);
        assertThat((List<String>) JsonPath.read(ler(URL + "plantao?unidadeId=" + base.getUuid()), "$[*].prefixo"))
                .containsExactlyInAnyOrder("TR-USB-11", "TR-USA-11");
        String encerrado = enviar(post(URL + "plantao/" + JsonPath.read(p, "$.uuid") + "/encerramento"), "", 200);
        assertThat((Boolean) JsonPath.read(encerrado, "$.vigente")).isFalse();
    }

    @Test
    void viaturaEmpenhadaPeloSamu() throws Exception {
        String usb = veiculo("TRF6G78", "TR-USB-21", "USB", base, 201);
        String usa = veiculo("TRG7H89", "TR-USA-21", "USA", base, 201);
        Instant inicio = Instant.now().minus(1, ChronoUnit.HOURS);
        String chamado = "{\"centralId\": \"%s\", \"origem\": \"CHAMADO_192\", \"solicitanteTelefone\": \"11900001111\", \"queixa\": \"Queda de moto.\"}"
                .formatted(base.getUuid());
        String id = JsonPath.read(enviar(post(URG + "ocorrencia"), chamado, 201), "$.uuid");
        String regular = "{\"profissionalMatricula\": \"TR-MEDICO\", \"prioridade\": \"VERMELHO\", \"decisao\": \"ENVIO_DE_RECURSO\", \"conduta\": \"Avaliar.\"%s}";
        // Sem plantão: 422.
        enviar(post(URG + "ocorrencia/" + id + "/regulacao"), regular.formatted(", \"veiculoId\": \"" + usb + "\""), 422);
        plantao(usb, inicio, inicio.plus(12, ChronoUnit.HOURS), t("TR-CONDUTOR", "CONDUTOR") + "," + t("TR-TECNICO", "TECNICO_ENFERMAGEM"), 201);
        plantao(usa, inicio, inicio.plus(12, ChronoUnit.HOURS),
                t("TR-CONDUTOR2", "CONDUTOR") + "," + t("TR-ENFERMEIRO", "ENFERMEIRO") + "," + t("TR-MEDICO", "MEDICO"), 201);
        assertThat((List<String>) JsonPath.read(ler(URG + "painel?centralId=" + base.getUuid()), "$.viaturas[*].prefixo"))
                .containsExactly("TR-USA-21", "TR-USB-21");
        // Tipo pedido diferente da viatura: 422; orientação com viatura: 400.
        enviar(post(URG + "ocorrencia/" + id + "/regulacao"), regular.formatted(", \"tipoRecurso\": \"USA\", \"veiculoId\": \"" + usb + "\""), 422);
        enviar(post(URG + "ocorrencia/" + id + "/regulacao"),
                "{\"profissionalMatricula\": \"TR-MEDICO\", \"prioridade\": \"VERDE\", \"decisao\": \"ORIENTACAO\", \"conduta\": \"x\", \"veiculoId\": \"%s\"}".formatted(usb), 400);
        String regulada = enviar(post(URG + "ocorrencia/" + id + "/regulacao"), regular.formatted(", \"veiculoId\": \"" + usb + "\""), 200);
        assertThat((String) JsonPath.read(regulada, "$.tipoRecurso")).isEqualTo("USB");
        assertThat((String) JsonPath.read(regulada, "$.recursoIdentificacao")).isEqualTo("TR-USB-21");
        assertThat((List<String>) JsonPath.read(ler(URG + "painel?centralId=" + base.getUuid()), "$.viaturas[*].prefixo")).containsExactly("TR-USA-21");

        String outro = JsonPath.read(enviar(post(URG + "ocorrencia"), chamado, 201), "$.uuid");
        enviar(post(URG + "ocorrencia/" + outro + "/regulacao"), regular.formatted(", \"veiculoId\": \"" + usb + "\""), 409);

        enviar(post(URG + "ocorrencia/" + id + "/deslocamento"), "{\"tipo\": \"ENVIO_DE_RECURSO\"}", 200);
        assertThat((String) JsonPath.read(ler(URL + "veiculo/" + usb), "$.situacao")).isEqualTo("EM_SERVICO");
        enviar(post(URL + "veiculo/" + usb + "/situacao"), "{\"situacao\": \"MANUTENCAO\", \"motivo\": \"x\"}", 422);
        enviar(post(URG + "ocorrencia/" + id + "/deslocamento"), "{\"tipo\": \"CHEGADA_AO_LOCAL\"}", 200);
        enviar(post(URG + "ocorrencia/" + id + "/encerramento"), "{\"profissionalMatricula\": \"TR-MEDICO\", \"desfecho\": \"ATENDIDO_NO_LOCAL\", \"observacao\": \"Escoriações.\"}", 200);
        String livre = ler(URL + "veiculo/" + usb);
        assertThat((String) JsonPath.read(livre, "$.situacao")).isEqualTo("DISPONIVEL");
        assertThat((List<String>) JsonPath.read(livre, "$.historico[*].situacao")).containsExactly("DISPONIVEL", "EM_SERVICO", "DISPONIVEL");
    }
}
