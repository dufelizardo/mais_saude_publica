package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.models.Paciente;
import com.edufelizardo.maissaudepublica.models.Profissional;
import com.edufelizardo.maissaudepublica.models.UnidadeDeSaude;
import com.edufelizardo.maissaudepublica.models.enuns.Sexo;
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

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

/**
 * Itens internos da Imunização, I6 (ADR-0206): o remanejamento entre salas de vacina (envio, recusa, cancelamento, recebimento na faixa
 * e fora dela, as regras do lote e do destino), o alerta de ESAVI por lote (graves em 30 dias e desvio de qualidade, o número
 * normalizado), a suspensão na rede (lotes sob suspeita, a dose barrada no estoque e no lote digitado, a entrada já suspensa, a avaliação
 * da unidade bloqueada, liberar e inutilizar) e a revogação da contraindicação pelo CRIE.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ImunizacaoRedeControllerTest {

    static final String PREFIXO = "ImuRede Teste ";
    private static final String MATRICULA = "P-IMU6-TEC";
    private static final String URL = "/api/v1/";

    @Autowired private MockMvc mockMvc;
    @Autowired private JdbcTemplate jdbc;
    @Autowired private UnidadeDeSaudeRepository unidadeDeSaudeRepository;
    @Autowired private PacienteRepository pacienteRepository;
    @Autowired private ProfissionalRepository profissionalRepository;

    private final LocalDate hoje = LocalDate.now();
    private UnidadeDeSaude origem;
    private UnidadeDeSaude destino;
    private UnidadeDeSaude semSala;
    private int pacientes;

    @BeforeEach
    void seed() {
        limpar();
        origem = unidade("Origem");
        destino = unidade("Destino");
        semSala = unidade("Sem sala");
        for (UnidadeDeSaude u : List.of(origem, destino)) {
            jdbc.update("insert into tb_unidade_capacidade_assistencial (uuid, unidade_id, capacidade_assistencial_id, inicio) select gen_random_uuid(), ?, uuid, ? "
                    + "from tb_capacidade_assistencial where codigo = 'SALA_VACINA'", u.getUuid(), hoje.minusDays(1));
        }
        Profissional tec = new Profissional();
        tec.setMatricula(MATRICULA);
        tec.setCpf("90055500001");
        tec.setNome(PREFIXO + "Técnica");
        tec.setConselhoClasse("COREN");
        tec.setAtivo(true);
        profissionalRepository.save(tec);
    }

    @AfterEach
    void limpar() {
        limparDados(jdbc, PREFIXO, MATRICULA);
    }

    static void limparDados(JdbcTemplate jdbc, String prefixo, String matricula) {
        String unidades = "(select uuid from tb_unidade_de_saude where nome like '" + prefixo + "%')";
        String lotes = "(select uuid from tb_lote_imunobiologico where unidade_id in " + unidades + ")";
        String pacientesSql = "(select uuid from tb_paciente where nome like '" + prefixo + "%')";
        String notificacoes = "(select uuid from tb_notificacao where paciente_id in " + pacientesSql + ")";
        jdbc.update("delete from tb_movimentacao_imunobiologico where lote_id in " + lotes);
        jdbc.update("delete from tb_remanejamento_imunobiologico where unidade_destino_id in " + unidades + " or lote_origem_id in " + lotes);
        jdbc.update("delete from tb_contraindicacao_vacinal where paciente_id in " + pacientesSql);
        jdbc.update("delete from tb_notificacao_dose where notificacao_id in " + notificacoes);
        jdbc.update("delete from tb_classificacao_caso where notificacao_id in " + notificacoes);
        jdbc.update("delete from tb_notificacao where paciente_id in " + pacientesSql);
        jdbc.update("update tb_registro_vacinacao set retificacao_de_id = null where paciente_id in " + pacientesSql);
        jdbc.update("delete from tb_registro_vacinacao where paciente_id in " + pacientesSql);
        jdbc.update("delete from tb_frasco_aberto where lote_id in " + lotes);
        jdbc.update("delete from tb_lote_imunobiologico where unidade_id in " + unidades);
        jdbc.update("delete from tb_suspensao_lote_rede where numero_lote like 'I6%'");
        jdbc.update("delete from tb_leitura_temperatura where camara_id in (select uuid from tb_camara_refrigeracao where unidade_id in " + unidades + ")");
        jdbc.update("delete from tb_camara_refrigeracao where unidade_id in " + unidades);
        jdbc.update("delete from tb_unidade_capacidade_assistencial where unidade_id in " + unidades);
        jdbc.update("delete from tb_paciente where nome like '" + prefixo + "%'");
        jdbc.update("delete from tb_profissional where matricula = '" + matricula + "'");
        jdbc.update("delete from tb_unidade_de_saude where nome like '" + prefixo + "%'");
    }

    private UnidadeDeSaude unidade(String nome) {
        UnidadeDeSaude u = new UnidadeDeSaude();
        u.setNome(PREFIXO + nome);
        u.setTipo(TipoUnidadeDeSaude.UBS);
        u.setAtivo(true);
        return unidadeDeSaudeRepository.save(u);
    }

    private Paciente paciente() {
        pacientes++;
        Paciente p = new Paciente();
        p.setNome(PREFIXO + "Criança " + pacientes);
        p.setCpf(String.format("900555%05d", pacientes));
        p.setSexo(Sexo.FEMININO);
        p.setDataNascimento(hoje.minusMonths(5));
        p.setAtivo(true);
        return pacienteRepository.save(p);
    }

    private String imuno(String codigo) {
        return jdbc.queryForObject("select cast(uuid as varchar) from tb_imunobiologico where codigo = ?", String.class, codigo);
    }

    private String enviar(MockHttpServletRequestBuilder r, String corpo, int esperado) throws Exception {
        var resposta = mockMvc.perform(r.contentType(MediaType.APPLICATION_JSON).content(corpo)).andReturn().getResponse();
        String texto = resposta.getContentAsString(StandardCharsets.UTF_8);
        assertThat(resposta.getStatus()).as(texto).isEqualTo(esperado);
        return texto;
    }

    private String ler(String url) throws Exception {
        var resposta = mockMvc.perform(get(url)).andReturn().getResponse();
        assertThat(resposta.getStatus()).as(resposta.getContentAsString()).isEqualTo(200);
        return resposta.getContentAsString(StandardCharsets.UTF_8);
    }

    private String entrada(UnidadeDeSaude u, String lote, int doses) throws Exception {
        String r = enviar(post(URL + "sala-de-vacina/entrada"), ("{\"unidadeId\": \"%s\", \"imunobiologicoId\": \"%s\", \"numeroLote\": \"%s\", "
                + "\"fabricante\": \"Butantan\", \"validade\": \"%s\", \"doses\": %d}").formatted(u.getUuid(), imuno("PENTA"), lote, hoje.plusDays(200), doses), 201);
        return JsonPath.read(r, "$.uuid");
    }

    private Object lote(UnidadeDeSaude u, String loteId, String campo) throws Exception {
        List<Object> v = JsonPath.read(ler(URL + "sala-de-vacina/estoque?unidadeId=" + u.getUuid()), "$.lotes[?(@.uuid == '" + loteId + "')]." + campo);
        return v.isEmpty() ? null : v.get(0);
    }

    private String remanejar(String loteId, UnidadeDeSaude para, int doses, String extra, int esperado) throws Exception {
        return enviar(post(URL + "sala-de-vacina/remanejamento"), ("{\"loteId\": \"%s\", \"unidadeDestinoId\": \"%s\", \"doses\": %d, \"motivo\": "
                + "\"EXCESSO\"%s}").formatted(loteId, para.getUuid(), doses, extra), esperado);
    }

    private String transcrita(Paciente p, String lote) throws Exception {
        return transcrita(p, lote, 10);
    }

    private String transcrita(Paciente p, String lote, int diasAtras) throws Exception {
        String r = enviar(post(URL + "vacinacao"), ("{\"pacienteId\": \"%s\", \"imunobiologicoId\": \"%s\", \"dose\": \"PRIMEIRA\", \"unidadeId\": \"%s\", "
                + "\"origem\": \"TRANSCRITA\", \"aplicadaEm\": \"%s\", \"lote\": \"%s\"}").formatted(p.getUuid(), imuno("PENTA"), semSala.getUuid(),
                hoje.minusDays(diasAtras).atTime(LocalTime.of(9, 0)), lote), 201);
        return JsonPath.read(r, "$.uuid");
    }

    private String esavi(Paciente p, String dose, String gravidade) throws Exception {
        String agravo = jdbc.queryForObject("select cast(uuid as varchar) from tb_agravo where codigo = 'ESAVI'", String.class);
        String r = enviar(post(URL + "vigilancia/notificacao"), ("{\"pacienteId\": \"%s\", \"agravoId\": \"%s\", \"unidadeId\": \"%s\", \"origem\": "
                + "\"VACINACAO\", \"respostas\": {\"manifestacoes\": [\"Febre\"], \"hospitalizado\": true}, \"gravidade\": \"%s\", \"doseIds\": [\"%s\"], "
                + "\"dataPrimeirosSintomas\": \"%s\"}").formatted(p.getUuid(), agravo, semSala.getUuid(), gravidade, dose, hoje.minusDays(9)), 201);
        return JsonPath.read(r, "$.uuid");
    }

    private String aplicada(Paciente p, UnidadeDeSaude u, String extra) {
        return ("{\"pacienteId\": \"%s\", \"imunobiologicoId\": \"%s\", \"dose\": \"PRIMEIRA\", \"unidadeId\": \"%s\", \"origem\": \"APLICADA\", "
                + "\"profissionalMatricula\": \"%s\"%s}").formatted(p.getUuid(), imuno("PENTA"), u.getUuid(), MATRICULA, extra);
    }

    @Test
    void remanejamentoEntreSalas() throws Exception {
        String lote = entrada(origem, "I6RM01", 20);
        // As recusas do envio.
        remanejar(lote, origem, 2, "", 400);
        remanejar(lote, semSala, 2, "", 422);
        remanejar(lote, destino, 21, "", 422);
        enviar(post(URL + "sala-de-vacina/remanejamento"), "{\"loteId\": \"" + lote + "\", \"unidadeDestinoId\": \"" + destino.getUuid()
                + "\", \"doses\": 2, \"motivo\": \"OUTRO\"}", 400);

        // Enviado: as doses saem; recusado: voltam.
        String r = remanejar(lote, destino, 8, ", \"observacao\": \"Sobra da campanha\"", 201);
        String id = JsonPath.read(r, "$.uuid");
        assertThat((String) JsonPath.read(r, "$.situacao")).isEqualTo("ENVIADO");
        assertThat(lote(origem, lote, "doses")).isEqualTo(12);
        enviar(post(URL + "remanejamento-imunobiologico/" + id + "/recusa"), "{}", 400);
        r = enviar(post(URL + "remanejamento-imunobiologico/" + id + "/recusa"), "{\"motivo\": \"Câmara cheia\"}", 200);
        assertThat((String) JsonPath.read(r, "$.situacao")).isEqualTo("RECUSADO");
        assertThat(lote(origem, lote, "doses")).isEqualTo(20);
        enviar(post(URL + "remanejamento-imunobiologico/" + id + "/recusa"), "{\"motivo\": \"x\"}", 422);

        // Cancelado pela origem: voltam.
        id = JsonPath.read(remanejar(lote, destino, 5, "", 201), "$.uuid");
        r = enviar(post(URL + "remanejamento-imunobiologico/" + id + "/cancelamento"), "", 200);
        assertThat((String) JsonPath.read(r, "$.situacao")).isEqualTo("CANCELADO");
        assertThat(lote(origem, lote, "doses")).isEqualTo(20);

        // Recebido na faixa: o lote nasce no destino, disponível.
        id = JsonPath.read(remanejar(lote, destino, 6, "", 201), "$.uuid");
        enviar(post(URL + "remanejamento-imunobiologico/" + id + "/recebimento"), "{}", 400);
        r = enviar(post(URL + "remanejamento-imunobiologico/" + id + "/recebimento"), "{\"temperaturaChegada\": 5.0}", 200);
        String noDestino = JsonPath.read(r, "$.loteDestinoId");
        assertThat((Boolean) JsonPath.read(r, "$.chegouForaDaFaixa")).isFalse();
        assertThat(lote(destino, noDestino, "doses")).isEqualTo(6);
        assertThat(lote(destino, noDestino, "situacao")).isEqualTo("DISPONIVEL");
        assertThat(lote(destino, noDestino, "numeroLote")).isEqualTo("I6RM01");
        enviar(post(URL + "remanejamento-imunobiologico/" + id + "/cancelamento"), "", 422);

        // Recebido fora da faixa: o lote fica sob suspeita pela excursão e não sai mais.
        id = JsonPath.read(remanejar(lote, destino, 2, "", 201), "$.uuid");
        r = enviar(post(URL + "remanejamento-imunobiologico/" + id + "/recebimento"), "{\"temperaturaChegada\": 10.5}", 200);
        assertThat((Boolean) JsonPath.read(r, "$.chegouForaDaFaixa")).isTrue();
        assertThat(lote(destino, noDestino, "doses")).isEqualTo(8);
        assertThat(lote(destino, noDestino, "situacao")).isEqualTo("SOB_SUSPEITA");
        assertThat(lote(destino, noDestino, "motivoSuspeita")).isEqualTo("EXCURSAO_TEMPERATURA");
        remanejar(noDestino, origem, 1, "", 422);
        assertThat(lote(origem, lote, "doses")).isEqualTo(12);

        // O livro e a lista das duas unidades.
        List<String> tipos = JsonPath.read(ler(URL + "lote-imunobiologico/" + lote + "/movimentacoes"), "$[*].tipo");
        assertThat(tipos).contains("REMANEJAMENTO_SAIDA", "ESTORNO_REMANEJAMENTO");
        assertThat((List<String>) JsonPath.read(ler(URL + "lote-imunobiologico/" + noDestino + "/movimentacoes"), "$[*].tipo"))
                .containsOnly("REMANEJAMENTO_ENTRADA");
        assertThat((List<Object>) JsonPath.read(ler(URL + "sala-de-vacina/remanejamentos?unidadeId=" + origem.getUuid()), "$[*]")).hasSize(4);
        assertThat((List<String>) JsonPath.read(ler(URL + "sala-de-vacina/remanejamentos?unidadeId=" + destino.getUuid()), "$[?(@.situacao == 'RECEBIDO')].uuid"))
                .hasSize(2);
    }

    @Test
    void alertaESuspensaoDoLoteNaRede() throws Exception {
        String loteOrigem = entrada(origem, "I6 AL77", 10);
        String loteDestino = entrada(destino, "i6al77", 10);
        // Dois ESAVI graves com o lote digitado de outro jeito; um não grave não conta.
        Paciente a = paciente();
        Paciente b = paciente();
        Paciente c = paciente();
        esavi(a, transcrita(a, "i6 al 77"), "GRAVE");
        String nb = esavi(b, transcrita(b, "I6AL77"), "GRAVE");
        esavi(c, transcrita(c, "I6AL77"), "NAO_GRAVE");
        // Um ESAVI com desvio de qualidade (A2) de outro lote.
        Paciente d = paciente();
        String nd = esavi(d, transcrita(d, "I6Q2X"), "NAO_GRAVE");
        enviar(post(URL + "vigilancia/notificacao/" + nd + "/encerramento"), "{\"evolucao\": \"CURA\", \"causalidade\": \"A2_DESVIO_QUALIDADE\", "
                + "\"conduta\": \"MANTER\"}", 200);

        String alertas = ler(URL + "imunizacao/alertas-lote");
        assertThat((List<Integer>) JsonPath.read(alertas, "$[?(@.numeroLote == 'I6AL77')].gravesEm30Dias")).containsExactly(2);
        assertThat((List<Integer>) JsonPath.read(alertas, "$[?(@.numeroLote == 'I6AL77')].salasComEstoque")).containsExactly(2);
        assertThat((List<Integer>) JsonPath.read(alertas, "$[?(@.numeroLote == 'I6AL77')].dosesEmEstoque")).containsExactly(20);
        assertThat((List<Boolean>) JsonPath.read(alertas, "$[?(@.numeroLote == 'I6Q2X')].desvioDeQualidade")).containsExactly(true);
        assertThat(alertas).doesNotContain("Criança");

        // A suspensão na rede: os dois lotes sob suspeita pelo ESAVI; uma vigente por lote.
        enviar(post(URL + "imunizacao/suspensao-lote"), "{\"imunobiologicoId\": \"" + imuno("PENTA") + "\", \"numeroLote\": \"I6AL77\"}", 400);
        String s = enviar(post(URL + "imunizacao/suspensao-lote"), "{\"imunobiologicoId\": \"" + imuno("PENTA") + "\", \"numeroLote\": \"i6 al77\", "
                + "\"motivo\": \"Dois ESAVI graves na rede.\"}", 201);
        String suspensao = JsonPath.read(s, "$.uuid");
        assertThat((String) JsonPath.read(s, "$.numeroLote")).isEqualTo("I6AL77");
        assertThat((String) JsonPath.read(s, "$.sinal")).contains("2 ESAVI graves");
        assertThat((Integer) JsonPath.read(s, "$.lotesAlcancados")).isEqualTo(2);
        enviar(post(URL + "imunizacao/suspensao-lote"), "{\"imunobiologicoId\": \"" + imuno("PENTA") + "\", \"numeroLote\": \"I6AL77\", \"motivo\": \"x\"}", 409);
        assertThat(lote(origem, loteOrigem, "situacao")).isEqualTo("SOB_SUSPEITA");
        assertThat(lote(origem, loteOrigem, "motivoSuspeita")).isEqualTo("ESAVI_LOTE");
        assertThat(lote(destino, loteDestino, "suspensoNaRede")).isEqualTo(true);

        // A dose barrada: do estoque e do lote digitado; o outro lote digitado passa.
        String barrada = enviar(post(URL + "vacinacao"), aplicada(paciente(), origem, ", \"loteEstoqueId\": \"" + loteOrigem + "\""), 422);
        assertThat(barrada).contains("alerta de ESAVI");
        enviar(post(URL + "vacinacao"), aplicada(paciente(), semSala, ", \"lote\": \"i6-al77\""), 201);
        barrada = enviar(post(URL + "vacinacao"), aplicada(paciente(), semSala, ", \"lote\": \"I6 AL 77\""), 422);
        assertThat(barrada).contains("suspenso na rede");
        // A unidade não avalia o lote suspenso pela rede; ele também não sai em remanejamento.
        enviar(post(URL + "lote-imunobiologico/" + loteOrigem + "/avaliacao"), "{\"decisao\": \"LIBERAR\", \"parecer\": \"ok\"}", 422);
        remanejar(loteOrigem, destino, 1, "", 422);

        // A suspensão sem sinal (comunicado do fabricante) alcança a entrada posterior.
        String z = enviar(post(URL + "imunizacao/suspensao-lote"), "{\"imunobiologicoId\": \"" + imuno("PENTA") + "\", \"numeroLote\": \"I6ZZ9\", "
                + "\"motivo\": \"Comunicado de desvio de qualidade do fabricante.\"}", 201);
        assertThat((String) JsonPath.read(z, "$.sinal")).startsWith("Sem sinal");
        assertThat((Integer) JsonPath.read(z, "$.lotesAlcancados")).isZero();
        String novo = entrada(origem, "i6 zz9", 4);
        assertThat(lote(origem, novo, "situacao")).isEqualTo("SOB_SUSPEITA");
        assertThat(lote(origem, novo, "motivoSuspeita")).isEqualTo("ESAVI_LOTE");

        // Liberar: os lotes voltam; a decisão é uma só.
        String url = URL + "imunizacao/suspensao-lote/" + suspensao + "/decisao";
        enviar(post(url), "{\"decisao\": \"TALVEZ\", \"parecer\": \"x\"}", 400);
        s = enviar(post(url), "{\"decisao\": \"LIBERAR\", \"parecer\": \"Investigação sem relação com o lote.\"}", 200);
        assertThat((String) JsonPath.read(s, "$.situacao")).isEqualTo("LIBERADA");
        assertThat(lote(origem, loteOrigem, "situacao")).isEqualTo("DISPONIVEL");
        assertThat(lote(destino, loteDestino, "aplicavel")).isEqualTo(true);
        enviar(post(url), "{\"decisao\": \"LIBERAR\", \"parecer\": \"x\"}", 422);
        enviar(post(URL + "vacinacao"), aplicada(paciente(), origem, ", \"loteEstoqueId\": \"" + loteOrigem + "\""), 201);

        // Inutilizar: o saldo sai como perda pelo ESAVI do lote.
        String zid = JsonPath.read(z, "$.uuid");
        enviar(post(URL + "imunizacao/suspensao-lote/" + zid + "/decisao"), "{\"decisao\": \"INUTILIZAR\", \"parecer\": \"Recolhimento do fabricante.\"}", 200);
        List<String> motivos = JsonPath.read(ler(URL + "lote-imunobiologico/" + novo + "/movimentacoes"), "$[?(@.tipo == 'PERDA_FISICA')].motivoPerda");
        assertThat(motivos).containsExactly("ESAVI_LOTE");
        assertThat(lote(origem, novo, "doses")).isNull();
        List<String> situacoes = JsonPath.read(ler(URL + "imunizacao/suspensoes-lote"), "$[?(@.numeroLote =~ /I6.*/)].situacao");
        assertThat(situacoes).containsExactlyInAnyOrder("LIBERADA", "INUTILIZADA");
        assertThat(nb).isNotNull();
    }

    @Test
    void revogacaoDaContraindicacaoPeloCrie() throws Exception {
        Paciente p = paciente();
        String n = esavi(p, transcrita(p, "I6CR1", 70), "GRAVE");
        enviar(post(URL + "vigilancia/notificacao/" + n + "/encerramento"), "{\"evolucao\": \"CURA\", \"causalidade\": \"A1_REACAO_PRODUTO\", "
                + "\"conduta\": \"CONTRAINDICAR\"}", 200);
        String caderneta = ler(URL + "paciente/" + p.getUuid() + "/vacinacao");
        String contra = ((List<String>) JsonPath.read(caderneta, "$.contraindicacoes[*].uuid")).get(0);
        String dose = ("{\"pacienteId\": \"%s\", \"imunobiologicoId\": \"%s\", \"dose\": \"SEGUNDA\", \"unidadeId\": \"%s\", \"origem\": \"APLICADA\", "
                + "\"profissionalMatricula\": \"%s\", \"lote\": \"I6OK1\"}").formatted(p.getUuid(), imuno("PENTA"), semSala.getUuid(), MATRICULA);
        enviar(post(URL + "vacinacao"), dose, 422);

        String url = URL + "contraindicacao-vacinal/" + contra + "/revogacao";
        enviar(post(url), "{\"parecer\": \" \"}", 400);
        caderneta = enviar(post(url), "{\"parecer\": \"Reavaliada no CRIE: reação local, sem contraindicação.\"}", 200);
        assertThat((List<String>) JsonPath.read(caderneta, "$.contraindicacoes[*].parecerRevogacao")).containsExactly("Reavaliada no CRIE: reação local, sem contraindicação.");
        assertThat((List<Object>) JsonPath.read(caderneta, "$.contraindicacoes[*].revogadaEm")).doesNotContainNull();
        enviar(post(url), "{\"parecer\": \"de novo\"}", 422);
        // Revogada, a dose entra sem justificativa.
        enviar(post(URL + "vacinacao"), dose, 201);
    }
}
