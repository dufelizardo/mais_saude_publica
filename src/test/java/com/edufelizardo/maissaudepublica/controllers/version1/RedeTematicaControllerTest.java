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

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

/**
 * Gestão da Rede, GR2 (ADR-0197): o catálogo das redes temáticas, o painel com os pontos e a lacuna por município, a referência estrutural
 * com as regras, a troca que encerra a vigente, a mais específica cobrindo a solicitante, e a Regulação marcando a autorização fora da
 * referência, com o percentual no painel.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class RedeTematicaControllerTest {

    private static final String PREFIXO = "RedeTem Teste ";
    private static final String URL = "/api/v1/rede/";
    private static final String REG = "/api/v1/solicitacao-regulacao/";
    private static final YearMonth MES = YearMonth.now().plusMonths(1);

    @Autowired private MockMvc mockMvc;
    @Autowired private JdbcTemplate jdbc;
    @Autowired private UnidadeDeSaudeRepository unidadeRepository;
    @Autowired private PacienteRepository pacienteRepository;
    @Autowired private ProfissionalRepository profissionalRepository;

    private UnidadeDeSaude municipio;
    private UnidadeDeSaude municipio2;
    private UnidadeDeSaude ubs;
    private UnidadeDeSaude ubs2;
    private UnidadeDeSaude fora;
    private UnidadeDeSaude policlinica;
    private UnidadeDeSaude hospital;
    private String procedimento;
    private String endodontia;
    private int pacientes;

    @BeforeEach
    void seed() {
        limpar();
        municipio = unidade("Município", TipoUnidadeDeSaude.MUNICIPAL, null);
        municipio2 = unidade("Município 2", TipoUnidadeDeSaude.MUNICIPAL, null);
        ubs = unidade("UBS", TipoUnidadeDeSaude.UBS, municipio);
        ubs2 = unidade("UBS 2", TipoUnidadeDeSaude.UBS, municipio);
        fora = unidade("Fora", TipoUnidadeDeSaude.UBS, municipio2);
        policlinica = unidade("Policlínica", TipoUnidadeDeSaude.POLICLINICA, municipio);
        hospital = unidade("Hospital", TipoUnidadeDeSaude.HOSPITAL, municipio);
        CapacidadesDeTeste.habilitar(jdbc, policlinica.getUuid(), "CONSULTA_ESPECIALIZADA");
        CapacidadesDeTeste.habilitar(jdbc, fora.getUuid(), "ACOMPANHAMENTO_LONGITUDINAL");
        procedimento = UUID.randomUUID().toString();
        jdbc.update("insert into tb_procedimento_regulado (uuid, nome, tipo, ativo) values (cast(? as uuid), ?, 'CONSULTA_ESPECIALIZADA', true)",
                procedimento, PREFIXO + "Cardiologia");
        endodontia = UUID.randomUUID().toString();
        jdbc.update("insert into tb_procedimento_regulado (uuid, nome, tipo, ativo, capacidade_exigida_id) select cast(? as uuid), ?, 'PROCEDIMENTO', true, uuid "
                + "from tb_capacidade_assistencial where codigo = 'ODONTO_ENDODONTIA'", endodontia, PREFIXO + "Endodontia");
        for (String m : List.of("RT-SOLICITANTE", "RT-REGULADOR")) {
            Profissional p = new Profissional();
            p.setMatricula(m);
            p.setCpf(m.equals("RT-REGULADOR") ? "90097000001" : "90097000002");
            p.setNome(PREFIXO + m);
            p.setAtivo(true);
            profissionalRepository.save(p);
        }
        pacientes = 0;
    }

    @AfterEach
    void limpar() {
        String unidades = "(select uuid from tb_unidade_de_saude where nome like '" + PREFIXO + "%')";
        String solicitacoes = "(select uuid from tb_solicitacao_regulacao where unidade_solicitante_id in " + unidades + ")";
        jdbc.update("delete from tb_evento_regulacao where solicitacao_id in " + solicitacoes);
        jdbc.update("delete from tb_solicitacao_regulacao where unidade_solicitante_id in " + unidades);
        jdbc.update("delete from tb_referencia_rede where origem_id in " + unidades);
        jdbc.update("delete from tb_componente_rede_item where componente = 'DC_ESPECIALIZADA' and capacidade_id in "
                + "(select uuid from tb_capacidade_assistencial where codigo = 'ODONTO_ENDODONTIA')");
        CapacidadesDeTeste.limpar(jdbc, PREFIXO);
        jdbc.update("delete from tb_procedimento_regulado where nome like '" + PREFIXO + "%'");
        jdbc.update("delete from tb_paciente where nome like '" + PREFIXO + "%'");
        jdbc.update("delete from tb_profissional where matricula like 'RT-%'");
        jdbc.update("update tb_unidade_de_saude set unidade_superior_id = null where nome like ?", PREFIXO + "%");
        jdbc.update("delete from tb_unidade_de_saude where nome like '" + PREFIXO + "%'");
    }

    private UnidadeDeSaude unidade(String nome, TipoUnidadeDeSaude tipo, UnidadeDeSaude superior) {
        UnidadeDeSaude u = new UnidadeDeSaude();
        u.setNome(PREFIXO + nome);
        u.setTipo(tipo);
        u.setAtivo(true);
        u.setUnidadeSuperior(superior);
        return unidadeRepository.save(u);
    }

    private String enviar(MockHttpServletRequestBuilder r, String corpo, int esperado) throws Exception {
        var resposta = mockMvc.perform(r.contentType(MediaType.APPLICATION_JSON).content(corpo)).andReturn().getResponse();
        assertThat(resposta.getStatus()).as(resposta.getContentAsString(StandardCharsets.UTF_8)).isEqualTo(esperado);
        return resposta.getContentAsString(StandardCharsets.UTF_8);
    }

    private String ler(String url) throws Exception {
        return enviar(get(url), "", 200);
    }

    private String referencia(UnidadeDeSaude origem, String proc, UnidadeDeSaude executante, LocalDate inicio, int esperado) throws Exception {
        return enviar(post(URL + "referencia"), "{\"origemId\": \"%s\", \"procedimentoId\": \"%s\", \"executanteId\": \"%s\"%s}"
                .formatted(origem.getUuid(), proc, executante.getUuid(), inicio == null ? "" : ", \"inicio\": \"" + inicio + "\""), esperado);
    }

    private String sugerida(UnidadeDeSaude solicitante) throws Exception {
        return JsonPath.read(ler(URL + "referencia/sugestao?procedimentoId=" + procedimento + "&solicitanteId=" + solicitante.getUuid()), "$.executanteNome");
    }

    /** Solicita a partir da unidade e autoriza na executante; devolve o id da solicitação. */
    private String autorizar(UnidadeDeSaude solicitante, UnidadeDeSaude executante) throws Exception {
        Paciente p = new Paciente();
        p.setNome(PREFIXO + "Paciente " + (++pacientes));
        p.setCpf(String.format("9097%07d", pacientes));
        p.setAtivo(true);
        p = pacienteRepository.save(p);
        enviar(post(REG), ("{\"pacienteId\": \"%s\", \"procedimentoId\": \"%s\", \"unidadeSolicitanteId\": \"%s\", \"profissionalMatricula\": \"RT-SOLICITANTE\", "
                + "\"cid\": \"I10\", \"justificativa\": \"Hipertensão resistente.\", \"prioridade\": \"VERDE\"}").formatted(p.getUuid(), procedimento, solicitante.getUuid()), 201);
        String id = jdbc.queryForObject("select cast(uuid as varchar) from tb_solicitacao_regulacao where paciente_id = ?", String.class, p.getUuid());
        enviar(post(REG + id + "/autorizacao"), "{\"profissionalMatricula\": \"RT-REGULADOR\", \"unidadeExecutanteId\": \"%s\", \"dataHoraPrevista\": \"%s\"}"
                .formatted(executante.getUuid(), MES.atDay(10).atTime(9, 0)), 200);
        return id;
    }

    @Test
    void catalogoEPainelDaRede() throws Exception {
        String catalogo = ler(URL + "tematica");
        assertThat((List<String>) JsonPath.read(catalogo, "$[*].codigo")).containsExactly("URGENCIA_EMERGENCIA", "CEGONHA_ALYNE", "PESSOA_COM_DEFICIENCIA", "DOENCAS_CRONICAS");
        assertThat((List<String>) JsonPath.read(catalogo, "$[?(@.codigo == 'CEGONHA_ALYNE')].componentes[?(@.codigo == 'RC_PARTO_NASCIMENTO')].itens[*].codigo"))
                .contains("OBSTETRICIA");

        String endo = jdbc.queryForObject("select cast(uuid as varchar) from tb_capacidade_assistencial where codigo = 'ODONTO_ENDODONTIA'", String.class);
        String perfil = jdbc.queryForObject("select cast(uuid as varchar) from tb_perfil_operacional order by codigo limit 1", String.class);
        enviar(post(URL + "tematica/componente/DC_ESPECIALIZADA/item"), "{}", 400);
        enviar(post(URL + "tematica/componente/DC_ESPECIALIZADA/item"), "{\"capacidadeId\": \"%s\", \"perfilId\": \"%s\"}".formatted(endo, perfil), 400);
        String item = enviar(post(URL + "tematica/componente/DC_ESPECIALIZADA/item"), "{\"capacidadeId\": \"%s\"}".formatted(endo), 201);
        enviar(post(URL + "tematica/componente/DC_ESPECIALIZADA/item"), "{\"capacidadeId\": \"%s\"}".formatted(endo), 409);
        enviar(delete(URL + "tematica/item/" + JsonPath.read(item, "$.uuid")), "", 204);

        // A policlínica é ponto da atenção especializada; o Município 2 tem ponto na atenção básica, mas não na especializada.
        String painel = ler(URL + "tematica/DOENCAS_CRONICAS");
        assertThat((List<String>) JsonPath.read(painel, "$.componentes[?(@.codigo == 'DC_ESPECIALIZADA')].pontos[*].unidadeNome")).contains(PREFIXO + "Policlínica");
        assertThat((List<String>) JsonPath.read(painel, "$.componentes[?(@.codigo == 'DC_ATENCAO_BASICA')].pontos[*].unidadeNome")).contains(PREFIXO + "Fora");
        assertThat((List<String>) JsonPath.read(painel, "$.lacunas")).contains(PREFIXO + "Município 2: sem ponto de atenção especializada ambulatorial e hospitalar.");
    }

    @Test
    void referenciaEAutorizacaoForaDela() throws Exception {
        // As regras: a capacidade exigida, a mesma unidade, a repetida.
        referencia(municipio, endodontia, policlinica, null, 422);
        referencia(policlinica, procedimento, policlinica, null, 400);
        String daEsfera = referencia(municipio, procedimento, policlinica, null, 201);
        referencia(municipio, procedimento, policlinica, null, 409);
        String daUbs = referencia(ubs, procedimento, hospital, null, 201);

        // A mais específica: a UBS tem a própria; a UBS 2 usa a do município; fora do município, nenhuma.
        assertThat(sugerida(ubs)).isEqualTo(PREFIXO + "Hospital");
        assertThat(sugerida(ubs2)).isEqualTo(PREFIXO + "Policlínica");
        assertThat(sugerida(fora)).isNull();

        // A troca começa depois da vigente e a encerra na véspera; hoje ainda vale a anterior.
        referencia(municipio, procedimento, hospital, LocalDate.now(), 422);
        String nova = referencia(municipio, procedimento, hospital, LocalDate.now().plusDays(1), 201);
        String historico = ler(URL + "referencia?procedimentoId=" + procedimento);
        assertThat((List<String>) JsonPath.read(historico, "$.referencias[?(@.uuid == '" + JsonPath.read(daEsfera, "$.uuid") + "')].fim"))
                .containsExactly(LocalDate.now().toString());
        assertThat(sugerida(ubs2)).isEqualTo(PREFIXO + "Policlínica");

        // A Regulação marca a autorização: dentro, fora e sem referência.
        String dentro = autorizar(ubs2, policlinica);
        String foraDaRef = autorizar(ubs, policlinica);
        String semRef = autorizar(fora, policlinica);
        assertThat((Boolean) JsonPath.read(ler(REG + dentro), "$.foraDaReferencia")).isFalse();
        assertThat((Boolean) JsonPath.read(ler(REG + foraDaRef), "$.foraDaReferencia")).isTrue();
        assertThat((Object) JsonPath.read(ler(REG + semRef), "$.foraDaReferencia")).isNull();
        assertThat(jdbc.queryForObject("select count(*) from tb_evento_regulacao where solicitacao_id = cast(? as uuid) and texto like '%Fora da referência%'",
                Integer.class, foraDaRef)).isEqualTo(1);

        String painel = ler(URL + "referencia?procedimentoId=" + procedimento);
        assertThat((Integer) JsonPath.read(painel, "$.indicadores[0].autorizacoes")).isEqualTo(2);
        assertThat((Integer) JsonPath.read(painel, "$.indicadores[0].foraDaReferencia")).isEqualTo(1);
        assertThat((Double) JsonPath.read(painel, "$.indicadores[0].percentualFora")).isEqualTo(50.0);

        // Encerrar: a que ainda não começou, não; a da UBS, sim, uma vez.
        enviar(post(URL + "referencia/" + JsonPath.read(nova, "$.uuid") + "/encerramento"), "", 422);
        String encerrada = enviar(post(URL + "referencia/" + JsonPath.read(daUbs, "$.uuid") + "/encerramento"), "", 200);
        assertThat((String) JsonPath.read(encerrada, "$.fim")).isEqualTo(LocalDate.now().toString());
        enviar(post(URL + "referencia/" + JsonPath.read(daUbs, "$.uuid") + "/encerramento"), "", 422);
    }
}
