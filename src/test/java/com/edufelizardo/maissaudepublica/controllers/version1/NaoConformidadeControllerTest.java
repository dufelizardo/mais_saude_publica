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
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

/**
 * Não conformidades e planos de ação, fatia Q2 da Qualidade (ADR-0204): as regras da origem, o ciclo PDCA (análise, ações 5W2H,
 * verificação não eficaz que abre o ciclo seguinte e a eficaz que encerra), o cancelamento, as sugestões do incidente e da VISA e o painel.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class NaoConformidadeControllerTest {

    static final String PREFIXO = "NaoConf Teste ";
    private static final String API = "/api/v1/qualidade/";

    @Autowired private MockMvc mockMvc;
    @Autowired private JdbcTemplate jdbc;
    @Autowired private UnidadeDeSaudeRepository unidadeDeSaudeRepository;
    @Autowired private ProfissionalRepository profissionalRepository;

    private final LocalDate hoje = LocalDate.now(java.time.ZoneId.of("America/Sao_Paulo"));
    private UnidadeDeSaude hospital;
    private UnidadeDeSaude outra;

    @BeforeEach
    void seed() {
        limpar();
        hospital = unidade("Hospital");
        outra = unidade("Outra");
        Profissional p = new Profissional();
        p.setMatricula("NCT-RESP");
        p.setCpf("90088800001");
        p.setNome(PREFIXO + "Responsável");
        p.setAtivo(true);
        profissionalRepository.save(p);
    }

    @AfterEach
    void limpar() {
        limparDados(jdbc, PREFIXO, "NCT-%");
    }

    static void limparDados(JdbcTemplate jdbc, String prefixo, String matriculas) {
        String unidades = "(select uuid from tb_unidade_de_saude where nome like '" + prefixo + "%')";
        String ncs = "(select uuid from tb_nao_conformidade where unidade_id in " + unidades + ")";
        jdbc.update("delete from tb_verificacao_eficacia where nao_conformidade_id in " + ncs);
        jdbc.update("delete from tb_acao_qualidade where nao_conformidade_id in " + ncs);
        jdbc.update("delete from tb_nao_conformidade where unidade_id in " + unidades);
        jdbc.update("delete from tb_setor where unidade_id in " + unidades);
        VigilanciaSanitariaControllerTest.limparDados(jdbc, prefixo, matriculas);
        IncidenteSegurancaControllerTest.limparDados(jdbc, prefixo);
    }

    private UnidadeDeSaude unidade(String nome) {
        UnidadeDeSaude u = new UnidadeDeSaude();
        u.setNome(PREFIXO + nome);
        u.setTipo(TipoUnidadeDeSaude.HOSPITAL);
        u.setAtivo(true);
        return unidadeDeSaudeRepository.save(u);
    }

    private String enviar(MockHttpServletRequestBuilder r, String corpo, int esperado) throws Exception {
        var resposta = mockMvc.perform(r.contentType(MediaType.APPLICATION_JSON).content(corpo)).andReturn().getResponse();
        String texto = resposta.getContentAsString(StandardCharsets.UTF_8);
        assertThat(resposta.getStatus()).as(texto).isEqualTo(esperado);
        return texto;
    }

    private String ler(String url) throws Exception {
        var resposta = mockMvc.perform(get(url)).andReturn().getResponse();
        assertThat(resposta.getStatus()).isEqualTo(200);
        return resposta.getContentAsString(StandardCharsets.UTF_8);
    }

    private String incidente(UnidadeDeSaude u) throws Exception {
        return JsonPath.read(enviar(post(API + "incidente"), "{\"unidadeId\": \"" + u.getUuid() + "\", \"ocorridoEm\": \"" + Instant.now().minus(Duration.ofDays(2))
                + "\", \"local\": \"Leito 3\", \"tipo\": \"QUEDA\", \"descricao\": \"Queda do leito.\"}", 201), "$.uuid");
    }

    private String nc(String extra) throws Exception {
        return enviar(post(API + "nao-conformidade"), "{\"unidadeId\": \"" + hospital.getUuid() + "\", \"descricao\": \"" + PREFIXO + "Falha.\", \"gravidade\": "
                + "\"MAIOR\", \"responsavelMatricula\": \"NCT-RESP\"" + extra + "}", 201);
    }

    private String acao(String nc, String oQue, LocalDate prazo) throws Exception {
        String r = enviar(post(API + "nao-conformidade/" + nc + "/acao"), "{\"descricao\": \"" + oQue + "\", \"justificativa\": \"Eliminar a causa.\", "
                + "\"responsavelMatricula\": \"NCT-RESP\", \"prazo\": \"" + prazo + "\", \"custo\": 150.5}", 201);
        List<String> ids = JsonPath.read(r, "$.acoes[?(@.oQue == '" + oQue + "')].uuid");
        return ids.get(ids.size() - 1);
    }

    @Test
    void regrasDaOrigem() throws Exception {
        String url = API + "nao-conformidade";
        String base = "{\"unidadeId\": \"" + hospital.getUuid() + "\", \"descricao\": \"x\", \"gravidade\": \"MENOR\", \"responsavelMatricula\": \"NCT-RESP\", ";
        String inc = incidente(hospital);
        String incOutra = incidente(outra);
        UUID setorOutra = UUID.randomUUID();
        jdbc.update("insert into tb_setor (uuid, unidade_id, nome, codigo, tipo, ativo) values (?, ?, ?, 'NCT-S1', 'ASSISTENCIAL', true)", setorOutra, outra.getUuid(),
                PREFIXO + "Setor alheio");
        enviar(post(url), base + "\"origem\": \"INDICADOR\"}", 400);
        enviar(post(url), base + "\"origem\": \"INCIDENTE\"}", 400);
        enviar(post(url), base + "\"origem\": \"AUDITORIA_INTERNA\", \"incidenteId\": \"" + inc + "\"}", 400);
        enviar(post(url), base + "\"origem\": \"INCIDENTE\", \"incidenteId\": \"" + incOutra + "\"}", 422);
        enviar(post(url), base + "\"origem\": \"OUTRA\", \"setorId\": \"" + setorOutra + "\"}", 422);
        enviar(post(url), base.replace("NCT-RESP", "NCT-NINGUEM") + "\"origem\": \"OUTRA\"}", 404);
        enviar(post(url), base + "\"origem\": \"OUTRA\", \"identificadaEm\": \"" + hoje.plusDays(1) + "\"}", 400);
        String r = enviar(post(url), base + "\"origem\": \"INCIDENTE\", \"incidenteId\": \"" + inc + "\"}", 201);
        assertThat((String) JsonPath.read(r, "$.situacao")).isEqualTo("ABERTA");
        assertThat((String) JsonPath.read(r, "$.numero")).matches("\\d{4}/\\d{6}");
        enviar(post(url), base + "\"origem\": \"INCIDENTE\", \"incidenteId\": \"" + inc + "\"}", 409);

        // Cancelada, libera o vínculo e leva as ações abertas.
        String id = JsonPath.read(r, "$.uuid");
        acao(id, "Treinar a equipe", hoje.plusDays(10));
        r = enviar(post(url + "/" + id + "/cancelamento"), "{\"motivo\": \"Registrada em duplicidade.\"}", 200);
        assertThat((List<String>) JsonPath.read(r, "$.acoes[*].situacao")).containsOnly("CANCELADA");
        enviar(post(url + "/" + id + "/cancelamento"), "{\"motivo\": \"x\"}", 422);
        enviar(post(url), base + "\"origem\": \"INCIDENTE\", \"incidenteId\": \"" + inc + "\"}", 201);
    }

    @Test
    void cicloPdca() throws Exception {
        String r = nc(", \"origem\": \"AUDITORIA_INTERNA\", \"correcaoImediata\": \"Recolhido o material vencido.\", \"identificadaEm\": \"" + hoje.minusDays(5) + "\"");
        String id = JsonPath.read(r, "$.uuid");
        String url = API + "nao-conformidade/" + id;
        enviar(post(url + "/verificacao"), "{\"eficaz\": true}", 422);
        r = enviar(post(url + "/analise"), "{\"analiseCausa\": \"Sem rotina de conferência da validade.\", \"categorias\": [\"METODO\", \"MAO_DE_OBRA\", \"METODO\"]}", 200);
        assertThat((List<String>) JsonPath.read(r, "$.categoriasCausa")).containsExactly("METODO", "MAO_DE_OBRA");

        // O plano do ciclo 1.
        enviar(post(url + "/acao"), "{\"descricao\": \"x\", \"justificativa\": \"y\", \"responsavelMatricula\": \"NCT-RESP\", \"prazo\": \"" + hoje.minusDays(6) + "\"}", 400);
        String rotina = acao(id, "Criar rotina semanal", hoje.plusDays(15));
        String painel = acao(id, "Afixar painel de validade", hoje.minusDays(1));
        r = ler(url);
        assertThat((String) JsonPath.read(r, "$.situacao")).isEqualTo("EM_TRATAMENTO");
        assertThat((Integer) JsonPath.read(r, "$.acoesAtrasadas")).isEqualTo(1);
        enviar(post(API + "acao/" + rotina + "/andamento"), "{\"situacao\": \"PENDENTE\"}", 400);
        enviar(post(API + "acao/" + rotina + "/andamento"), "{\"situacao\": \"CONCLUIDA\"}", 400);
        r = enviar(post(API + "acao/" + rotina + "/andamento"), "{\"situacao\": \"EM_ANDAMENTO\"}", 200);
        assertThat((String) JsonPath.read(r, "$.situacao")).isEqualTo("EM_ANDAMENTO");
        enviar(post(API + "acao/" + rotina + "/andamento"), "{\"situacao\": \"CONCLUIDA\", \"evidencia\": \"Planilha de conferência em uso.\"}", 200);
        r = enviar(post(API + "acao/" + painel + "/cancelamento"), "{\"motivo\": \"Substituída pela rotina.\"}", 200);
        assertThat((String) JsonPath.read(r, "$.situacao")).isEqualTo("EM_VERIFICACAO");
        assertThat((String) JsonPath.read(r, "$.verificacaoPrevista")).isEqualTo(hoje.plusDays(30).toString());
        enviar(post(url + "/acao"), "{\"descricao\": \"x\", \"justificativa\": \"y\", \"responsavelMatricula\": \"NCT-RESP\", \"prazo\": \"" + hoje + "\"}", 422);

        // Não eficaz: ciclo 2.
        enviar(post(url + "/verificacao"), "{\"eficaz\": false}", 400);
        r = enviar(post(url + "/verificacao"), "{\"eficaz\": false, \"observacao\": \"Ainda houve material vencido na farmácia.\"}", 200);
        assertThat((String) JsonPath.read(r, "$.situacao")).isEqualTo("EM_TRATAMENTO");
        assertThat((Integer) JsonPath.read(r, "$.ciclo")).isEqualTo(2);
        enviar(post(API + "acao/" + rotina + "/andamento"), "{\"situacao\": \"EM_ANDAMENTO\"}", 422);
        String auditoria = acao(id, "Auditoria mensal da validade", hoje.plusDays(20));
        r = ler(url);
        assertThat((List<Integer>) JsonPath.read(r, "$.acoes[?(@.uuid == '" + auditoria + "')].ciclo")).containsExactly(2);
        enviar(post(API + "acao/" + auditoria + "/andamento"), "{\"situacao\": \"CONCLUIDA\", \"evidencia\": \"Relatório da primeira auditoria.\", "
                + "\"concluidaEm\": \"" + hoje.plusDays(1) + "\"}", 400);
        enviar(post(API + "acao/" + auditoria + "/andamento"), "{\"situacao\": \"CONCLUIDA\", \"evidencia\": \"Relatório da primeira auditoria.\"}", 200);
        r = enviar(post(url + "/verificacao"), "{\"eficaz\": true, \"observacao\": \"Dois meses sem vencidos.\"}", 200);
        assertThat((String) JsonPath.read(r, "$.situacao")).isEqualTo("ENCERRADA");
        assertThat((List<Boolean>) JsonPath.read(r, "$.verificacoes[*].eficaz")).containsExactly(false, true);
        enviar(post(url + "/analise"), "{\"analiseCausa\": \"x\"}", 422);

        String p = ler(API + "nao-conformidades/painel?unidadeId=" + hospital.getUuid());
        assertThat((Integer) JsonPath.read(p, "$.verificadas")).isEqualTo(1);
        assertThat((Double) JsonPath.read(p, "$.eficaciaNaPrimeira")).isEqualTo(0.0);
        assertThat((List<Integer>) JsonPath.read(p, "$.porSituacao[?(@.codigo == 'ENCERRADA')].quantidade")).containsExactly(1);
    }

    @Test
    void sugestoesDoIncidenteEDaVisa() throws Exception {
        // O incidente concluído com dano grave.
        String inc = incidente(hospital);
        enviar(post(API + "incidente/" + inc + "/classificacao"), "{\"natureza\": \"EVENTO_ADVERSO\", \"dano\": \"GRAVE\"}", 200);
        enviar(post(API + "incidente/" + inc + "/conclusao"), "{\"fatores\": [\"AMBIENTE\"], \"causaRaiz\": \"Grade abaixada.\"}", 200);

        // A irregularidade da VISA na unidade pública da rede.
        String visa = "/api/v1/vigilancia-sanitaria/";
        String corpo = VigilanciaSanitariaControllerTest.corpoEstabelecimento(VigilanciaSanitariaControllerTest.cnpj(), "Hospital da rede", "8610101:ALTO")
                .replace(VigilanciaSanitariaControllerTest.PREFIXO, PREFIXO).replace("\"atividades\"", "\"unidadeId\": \"" + hospital.getUuid() + "\", \"atividades\"");
        String estab = JsonPath.read(enviar(post(visa + "estabelecimento"), corpo, 201), "$.uuid");
        String r = enviar(post(visa + "inspecao"), "{\"estabelecimentoId\": \"" + estab + "\", \"motivo\": \"PROGRAMADA\", \"resultado\": \"COM_RESSALVAS\", "
                + "\"relatorio\": \"x\", \"fiscais\": [\"NCT-RESP\"], \"irregularidades\": [{\"descricao\": \"Lavatório sem sabão\", \"gravidade\": \"LEVE\"}]}", 201);
        String irreg = JsonPath.read(r, "$.irregularidades[0].uuid");

        String s = ler(API + "nao-conformidades/sugestoes");
        assertThat((List<String>) JsonPath.read(s, "$[*].referenciaId")).contains(inc, irreg);
        r = nc(", \"origem\": \"INSPECAO_SANITARIA\", \"irregularidadeId\": \"" + irreg + "\"");
        assertThat((String) JsonPath.read(r, "$.irregularidadeDescricao")).isEqualTo("Lavatório sem sabão");
        nc(", \"origem\": \"INCIDENTE\", \"incidenteId\": \"" + inc + "\"");
        s = ler(API + "nao-conformidades/sugestoes");
        assertThat((List<String>) JsonPath.read(s, "$[*].referenciaId")).doesNotContain(inc, irreg);

        // A irregularidade de estabelecimento que não é unidade da rede.
        String privado = JsonPath.read(enviar(post(visa + "estabelecimento"), VigilanciaSanitariaControllerTest.corpoEstabelecimento(
                VigilanciaSanitariaControllerTest.cnpj(), "Clínica", "8630503:MEDIO").replace(VigilanciaSanitariaControllerTest.PREFIXO, PREFIXO), 201), "$.uuid");
        r = enviar(post(visa + "inspecao"), "{\"estabelecimentoId\": \"" + privado + "\", \"motivo\": \"PROGRAMADA\", \"resultado\": \"COM_RESSALVAS\", "
                + "\"relatorio\": \"x\", \"fiscais\": [\"NCT-RESP\"], \"irregularidades\": [{\"descricao\": \"Sem PGRSS\", \"gravidade\": \"GRAVE\"}]}", 201);
        String irregPrivada = JsonPath.read(r, "$.irregularidades[0].uuid");
        assertThat((List<String>) JsonPath.read(ler(API + "nao-conformidades/sugestoes"), "$[*].referenciaId")).doesNotContain(irregPrivada);
        enviar(post(API + "nao-conformidade"), "{\"unidadeId\": \"" + hospital.getUuid() + "\", \"descricao\": \"x\", \"gravidade\": \"MAIOR\", "
                + "\"responsavelMatricula\": \"NCT-RESP\", \"origem\": \"INSPECAO_SANITARIA\", \"irregularidadeId\": \"" + irregPrivada + "\"}", 422);
    }
}
