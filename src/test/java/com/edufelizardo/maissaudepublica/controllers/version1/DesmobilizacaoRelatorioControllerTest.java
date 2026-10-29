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

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Emergências, fatia E5 (ADR-0159): a desmobilização (liberações de uma vez, bloqueios, pendências, fim da exceção da
 * Escala, encerramento) e o relatório pós-evento (rascunho, números calculados, revisão do plano pela recomendação,
 * finalização e CSV).
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class DesmobilizacaoRelatorioControllerTest {

    private static final String PREFIXO = "Desmob Teste ";

    @Autowired private MockMvc mockMvc;
    @Autowired private JdbcTemplate jdbc;
    @Autowired private UnidadeDeSaudeRepository unidadeDeSaudeRepository;
    @Autowired private ProfissionalRepository profissionalRepository;

    private String inundacao;
    private String nivel1;
    private String nivel2;
    private UnidadeDeSaude ubs;
    private final LocalDate hoje = LocalDate.now();

    @BeforeEach
    void seed() {
        limpar();
        inundacao = jdbc.queryForObject("select cast(uuid as varchar) from tb_tipo_emergencia where codigo = 'INUNDACAO'", String.class);
        nivel1 = jdbc.queryForObject("select cast(uuid as varchar) from tb_nivel_emergencia where ordem = 1", String.class);
        nivel2 = jdbc.queryForObject("select cast(uuid as varchar) from tb_nivel_emergencia where ordem = 2", String.class);
        ubs = new UnidadeDeSaude();
        ubs.setNome(PREFIXO + "UBS");
        ubs.setTipo(TipoUnidadeDeSaude.UBS);
        ubs.setAtivo(true);
        ubs = unidadeDeSaudeRepository.save(ubs);
        Profissional p = new Profissional();
        p.setMatricula("DSM-ENF");
        p.setCpf("90066600001");
        p.setNome(PREFIXO + "Enfermeiro");
        p.setAtivo(true);
        profissionalRepository.save(p);
    }

    @AfterEach
    void limpar() {
        String emergencias = "(select e.uuid from tb_emergencia e join tb_evento_emergencial v on v.uuid = e.evento_id where v.descricao like '" + PREFIXO + "%')";
        String unidades = "(select uuid from tb_unidade_de_saude where nome like '" + PREFIXO + "%')";
        String relatorios = "(select uuid from tb_relatorio_pos_evento where emergencia_id in " + emergencias + ")";
        String planos = "(select uuid from tb_plano_contingencia where codigo like 'DSM_%')";
        jdbc.update("delete from tb_recomendacao_relatorio where relatorio_id in " + relatorios);
        jdbc.update("delete from tb_relatorio_pos_evento where emergencia_id in " + emergencias);
        jdbc.update("delete from tb_desmobilizacao where emergencia_id in " + emergencias);
        jdbc.update("delete from tb_turno_escala where unidade_id in " + unidades);
        jdbc.update("delete from tb_mobilizacao where emergencia_id in " + emergencias);
        jdbc.update("delete from tb_ativacao_capacidade where emergencia_id in " + emergencias);
        jdbc.update("delete from tb_capacidade_emergencial where unidade_id in " + unidades);
        jdbc.update("delete from tb_acao_emergencial where emergencia_id in " + emergencias);
        jdbc.update("delete from tb_registro_ativacao_plano where ativacao_id in (select uuid from tb_ativacao_plano where emergencia_id in " + emergencias + ")");
        jdbc.update("delete from tb_ativacao_plano where emergencia_id in " + emergencias);
        jdbc.update("delete from tb_mudanca_nivel_emergencia where emergencia_id in " + emergencias);
        jdbc.update("delete from tb_emergencia where uuid in " + emergencias);
        jdbc.update("delete from tb_evento_emergencial where descricao like '" + PREFIXO + "%'");
        jdbc.update("delete from tb_acao_plano where plano_id in " + planos);
        jdbc.update("delete from tb_plano_contingencia_nivel where plano_id in " + planos);
        jdbc.update("delete from tb_plano_contingencia where codigo like 'DSM_%'");
        jdbc.update("delete from tb_profissional where matricula like 'DSM-%'");
        jdbc.update("delete from tb_unidade_de_saude where nome like '" + PREFIXO + "%'");
    }

    private String enviar(MockHttpServletRequestBuilder r, String corpo, int esperado) throws Exception {
        return mockMvc.perform(r.contentType(MediaType.APPLICATION_JSON).content(corpo)).andExpect(status().is(esperado)).andReturn().getResponse()
                .getContentAsString(StandardCharsets.UTF_8);
    }

    private String ler(String url) throws Exception {
        return mockMvc.perform(get(url)).andExpect(status().isOk()).andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
    }

    /** A emergência em resposta: plano ativo, capacidade ativada, profissional mobilizado e uma ação aberta. */
    private String emResposta() throws Exception {
        String evento = JsonPath.read(enviar(post("/api/v1/evento-emergencial"), "{\"tipoId\": \"" + inundacao + "\", \"fonte\": \"DEFESA_CIVIL\", "
                + "\"descricao\": \"" + PREFIXO + "Rio transbordou\"}", 201), "$.uuid");
        String em = JsonPath.read(enviar(post("/api/v1/evento-emergencial/" + evento + "/emergencia"), "{\"nome\": \"Enchente Desmob\", \"nivelId\": \""
                + nivel1 + "\", \"justificativa\": \"Cota de alerta\"}", 201), "$.uuid");
        String plano = JsonPath.read(enviar(post("/api/v1/emergencias/plano"), "{\"codigo\": \"DSM_ENCHENTE\", \"nome\": \"Plano\", \"tipoId\": \"" + inundacao
                + "\", \"objetivo\": \"Responder\", \"vigenciaInicio\": \"" + hoje.minusDays(1) + "\", \"nivelIds\": [\"" + nivel1 + "\", \"" + nivel2
                + "\"], \"acoes\": [{\"nivelId\": \"" + nivel1 + "\", \"descricao\": \"Abrir o COE\", \"responsavel\": \"Vigilância\", \"prazoHoras\": 48}]}",
                201), "$.uuid");
        enviar(post("/api/v1/emergencias/plano/" + plano + "/publicacao"), "", 200);
        enviar(post("/api/v1/emergencia/" + em + "/plano-ativo"), "{\"planoId\": \"" + plano + "\", \"autoridade\": \"Secretário\", \"justificativa\": \"J\"}", 201);
        enviar(post("/api/v1/emergencia/" + em + "/nivel"), "{\"nivelId\": \"" + nivel2 + "\", \"justificativa\": \"Agravou\"}", 200);
        String cap = JsonPath.read(enviar(post("/api/v1/emergencias/capacidade"), "{\"unidadeId\": \"" + ubs.getUuid() + "\", \"recurso\": \"LEITOS\", "
                + "\"normal\": 10, \"emergencial\": 5}", 201), "$.uuid");
        enviar(post("/api/v1/emergencia/" + em + "/capacidade-ativa"), "{\"capacidadeId\": \"" + cap + "\", \"quantidade\": 3, \"justificativa\": \"Ala\"}", 201);
        enviar(post("/api/v1/emergencia/" + em + "/mobilizacao"), "{\"tipo\": \"PROFISSIONAL\", \"profissionalMatricula\": \"DSM-ENF\", \"destinoId\": \""
                + ubs.getUuid() + "\"}", 201);
        enviar(post("/api/v1/emergencia/" + em + "/acao"), "{\"descricao\": \"Abrir abrigo\", \"responsavel\": \"Assistência\", \"prioridade\": \"ALTA\"}", 201);
        return em;
    }

    private String turno(LocalDate dia) {
        return "{\"unidadeId\": \"" + ubs.getUuid() + "\", \"profissionalMatricula\": \"DSM-ENF\", \"tipo\": \"MANHA\", \"data\": \"" + dia
                + "\", \"inicio\": \"07:00\", \"fim\": \"13:00\"}";
    }

    @Test
    void desmobilizacaoLiberaEBloqueia() throws Exception {
        String em = emResposta();
        enviar(post("/api/v1/escala/turno"), turno(hoje.plusDays(1)), 201);
        String base = "/api/v1/emergencia/" + em;
        String r = enviar(post(base + "/desmobilizacao"), "{\"responsavel\": \"Secretário de Saúde\", \"justificativa\": \"Águas baixando\"}", 200);
        assertThat((String) JsonPath.read(r, "$.situacao")).isEqualTo("EM_DESMOBILIZACAO");
        assertThat((Integer) JsonPath.read(r, "$.desmobilizacao.mobilizacoesConcluidas")).isEqualTo(1);
        assertThat((Integer) JsonPath.read(r, "$.desmobilizacao.profissionaisLiberados")).isEqualTo(1);
        assertThat((Integer) JsonPath.read(r, "$.desmobilizacao.capacidadesDesativadas")).isEqualTo(1);
        assertThat((Integer) JsonPath.read(r, "$.desmobilizacao.planosDesativados")).isEqualTo(1);
        assertThat((List<String>) JsonPath.read(r, "$.planos[*].situacao")).containsOnly("DESATIVADA");
        assertThat((List<String>) JsonPath.read(r, "$.capacidades[*].situacao")).containsOnly("DESATIVADA");
        assertThat((List<String>) JsonPath.read(r, "$.mobilizacoes[*].situacao")).containsOnly("CONCLUIDA");
        assertThat((List<String>) JsonPath.read(r, "$.pendencias")).anyMatch(p -> p.contains("1 ação(ões) em aberto"));
        // A exceção da Escala acabou para turnos novos.
        enviar(post("/api/v1/escala/turno"), turno(hoje.plusDays(2)), 422);
        // Nada novo entra; o nível não muda; a desmobilização não recomeça.
        enviar(post(base + "/acao"), "{\"descricao\": \"Outra\", \"responsavel\": \"X\", \"prioridade\": \"BAIXA\"}", 422);
        enviar(post(base + "/mobilizacao"), "{\"tipo\": \"INSUMO\", \"recurso\": \"Kits\", \"destinoId\": \"" + ubs.getUuid() + "\"}", 422);
        enviar(post(base + "/nivel"), "{\"nivelId\": \"" + nivel1 + "\", \"justificativa\": \"J\"}", 422);
        enviar(post(base + "/desmobilizacao"), "{\"responsavel\": \"X\", \"justificativa\": \"De novo\"}", 422);
        // Com ação aberta, não encerra; cancela as restantes e encerra.
        enviar(post(base + "/encerramento"), "{\"motivo\": \"Fim\"}", 422);
        r = enviar(post(base + "/acoes-restantes/cancelamento"), "{\"motivo\": \"Desnecessária após a cheia\"}", 200);
        assertThat((List<String>) JsonPath.read(r, "$.acoes[*].situacao")).containsOnly("CANCELADA");
        assertThat((List<String>) JsonPath.read(r, "$.pendencias")).isEmpty();
        enviar(post(base + "/acoes-restantes/cancelamento"), "{\"motivo\": \"De novo\"}", 422);
        r = enviar(post(base + "/encerramento"), "{\"motivo\": \"Rede normalizada\"}", 200);
        assertThat((String) JsonPath.read(r, "$.situacao")).isEqualTo("ENCERRADA");
        assertThat((List<String>) JsonPath.read(ler("/api/v1/emergencias?situacao=EM_DESMOBILIZACAO"), "$[*].uuid")).doesNotContain(em);
    }

    @Test
    void encerrarDiretoTambemLibera() throws Exception {
        String em = emResposta();
        String r = enviar(post("/api/v1/emergencia/" + em + "/encerramento"), "{\"motivo\": \"Evento curto\"}", 200);
        assertThat((List<String>) JsonPath.read(r, "$.mobilizacoes[*].situacao")).containsOnly("CONCLUIDA");
        assertThat((List<String>) JsonPath.read(r, "$.planos[*].situacao")).containsOnly("DESATIVADA");
    }

    @Test
    void relatorioPosEvento() throws Exception {
        String em = emResposta();
        String rel = "/api/v1/emergencia/" + em + "/relatorio";
        String corpo = "{\"resumo\": \"Cheia de três dias\", \"problemas\": \"Abrigo tardio\", \"licoes\": \"Antecipar o abrigo\", \"recomendacoes\": ["
                + "{\"descricao\": \"Abrir abrigo no nível 1\", \"planoCodigo\": \"dsm_enchente\"}, {\"descricao\": \"Treinar a equipe\"}]}";
        enviar(put(rel), corpo, 422);
        String a = JsonPath.read(ler("/api/v1/emergencia/" + em), "$.acoes[0].uuid");
        enviar(post("/api/v1/emergencia/" + em + "/acao/" + a + "/situacao"), "{\"situacao\": \"CONCLUIDA\"}", 200);
        enviar(post("/api/v1/emergencia/" + em + "/desmobilizacao"), "{\"responsavel\": \"Secretário\", \"justificativa\": \"Controle\"}", 200);

        // Sem rascunho, só os números.
        String r = ler(rel);
        assertThat((Object) JsonPath.read(r, "$.uuid")).isNull();
        assertThat((String) JsonPath.read(r, "$.indicadores.nivelMaximo")).isEqualTo("Nível 2 · Resposta ampliada");
        assertThat((Integer) JsonPath.read(r, "$.indicadores.mudancasDeNivel")).isEqualTo(1);
        assertThat((Integer) JsonPath.read(r, "$.indicadores.acoesConcluidas")).isEqualTo(1);
        assertThat((Integer) JsonPath.read(r, "$.indicadores.profissionaisMobilizados")).isEqualTo(1);
        assertThat((Integer) JsonPath.read(r, "$.indicadores.capacidadeAtivada")).isEqualTo(3);
        assertThat((List<String>) JsonPath.read(r, "$.planosUsados[*].codigo")).containsExactly("DSM_ENCHENTE");

        enviar(put(rel), corpo.replace("dsm_enchente", "OUTRO_PLANO"), 422);
        r = enviar(put(rel), corpo, 200);
        assertThat((String) JsonPath.read(r, "$.situacao")).isEqualTo("RASCUNHO");
        assertThat((List<String>) JsonPath.read(r, "$.recomendacoes[*].planoCodigo")).containsExactly("DSM_ENCHENTE", null);
        enviar(post(rel + "/finalizacao"), "", 422);

        // A recomendação gera a revisão do plano (v2 em rascunho); de novo, 409.
        String rec = JsonPath.read(r, "$.recomendacoes[0].uuid");
        r = enviar(post(rel + "/recomendacao/" + rec + "/revisao-plano"), "", 200);
        assertThat((Integer) JsonPath.read(r, "$.recomendacoes[0].planoRevisaoVersao")).isEqualTo(2);
        enviar(post(rel + "/recomendacao/" + rec + "/revisao-plano"), "", 409);
        String sem = JsonPath.read(r, "$.recomendacoes[1].uuid");
        enviar(post(rel + "/recomendacao/" + sem + "/revisao-plano"), "", 422);
        // Ajustar o texto mantém a revisão ligada.
        r = enviar(put(rel), corpo.replace("Cheia de três dias", "Cheia de três dias na região norte"), 200);
        assertThat((Integer) JsonPath.read(r, "$.recomendacoes[0].planoRevisaoVersao")).isEqualTo(2);

        enviar(post("/api/v1/emergencia/" + em + "/encerramento"), "{\"motivo\": \"Rede normalizada\"}", 200);
        r = enviar(post(rel + "/finalizacao"), "", 200);
        assertThat((String) JsonPath.read(r, "$.situacao")).isEqualTo("FINALIZADO");
        enviar(put(rel), corpo, 422);
        enviar(post(rel + "/finalizacao"), "", 422);
        String csv = mockMvc.perform(get(rel + ".csv")).andExpect(status().isOk()).andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        assertThat(csv).contains("Nível máximo;Nível 2 · Resposta ampliada").contains("Capacidade emergencial ativada;3");
    }
}
