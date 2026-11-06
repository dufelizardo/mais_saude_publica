package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.models.Paciente;
import com.edufelizardo.maissaudepublica.models.Profissional;
import com.edufelizardo.maissaudepublica.models.UnidadeDeSaude;
import com.edufelizardo.maissaudepublica.models.enuns.TipoUnidadeDeSaude;
import com.edufelizardo.maissaudepublica.repositories.AgravoRepository;
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
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Vigilância, fatia V4a (ADR-0162): a vistoria do agente de endemias (regras de depósito e foco, pendência de revisita),
 * os focos (laboratório e tratamento), os índices do ciclo (IIP, Breteau e classificação) e as ações de controle, com os
 * casos de arbovirose ainda sem bloqueio.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ControleVetorialControllerTest {

    private static final String PREFIXO = "Vetor Teste ";
    private static final String CODIGO = "TSTV4";

    @Autowired private MockMvc mockMvc;
    @Autowired private JdbcTemplate jdbc;
    @Autowired private ProfissionalRepository profissionalRepository;
    @Autowired private UnidadeDeSaudeRepository unidadeRepository;
    @Autowired private PacienteRepository pacienteRepository;
    @Autowired private AgravoRepository agravoRepository;

    private String area;
    private String microarea;
    private final String[] imoveis = new String[4];
    private UnidadeDeSaude ubs;
    private final LocalDate hoje = LocalDate.now();

    @BeforeEach
    void seed() {
        limpar();
        UUID segmento = UUID.randomUUID();
        UUID territorio = UUID.randomUUID();
        UUID a = UUID.randomUUID();
        UUID m = UUID.randomUUID();
        jdbc.update("insert into tb_segmento_territorial (uuid, codigo, nome, tipo) values (?, ?, ?, 'DISTRITO_SANITARIO')", segmento, CODIGO + "SEG", PREFIXO + "Distrito");
        jdbc.update("insert into tb_territorio (uuid, segmento_id, nome, finalidade, ativo) values (?, ?, ?, 'VIGILANCIA', true)", territorio, segmento, PREFIXO + "Território");
        jdbc.update("insert into tb_area (uuid, territorio_id, codigo, nome) values (?, ?, '01', ?)", a, territorio, PREFIXO + "Área Norte");
        jdbc.update("insert into tb_microarea (uuid, area_id, codigo) values (?, ?, '01')", m, a);
        for (int i = 0; i < imoveis.length; i++) {
            UUID im = UUID.randomUUID();
            jdbc.update("insert into tb_imovel (uuid, microarea_id, tipo, ativo, logradouro, numero_logradouro, bairro) values (?, ?, 'DOMICILIO', true, ?, ?, 'Centro')",
                    im, m, "Rua das Flores", String.valueOf(10 + i));
            imoveis[i] = im.toString();
        }
        area = a.toString();
        microarea = m.toString();
        Profissional p = new Profissional();
        p.setMatricula("VET-ACE");
        p.setCpf("90166600001");
        p.setNome(PREFIXO + "Agente");
        p.setAtivo(true);
        profissionalRepository.save(p);
        ubs = new UnidadeDeSaude();
        ubs.setNome(PREFIXO + "UBS");
        ubs.setTipo(TipoUnidadeDeSaude.UBS);
        ubs.setAtivo(true);
        ubs = unidadeRepository.save(ubs);
    }

    @AfterEach
    void limpar() {
        String microareas = "(select uuid from tb_microarea where area_id in (select a.uuid from tb_area a join tb_territorio t on t.uuid = a.territorio_id "
                + "join tb_segmento_territorial s on s.uuid = t.segmento_id where s.codigo like '" + CODIGO + "%'))";
        String vistorias = "(select uuid from tb_vistoria_vetorial where imovel_id in (select uuid from tb_imovel where microarea_id in " + microareas + "))";
        jdbc.update("delete from tb_foco_vetorial where vistoria_id in " + vistorias);
        jdbc.update("delete from tb_vistoria_vetorial where uuid in " + vistorias);
        List<UUID> acoes = jdbc.queryForList("select acao_id from tb_acao_controle_vetorial_microarea where microarea_id in " + microareas, UUID.class);
        for (UUID a : acoes) {
            jdbc.update("delete from tb_acao_controle_vetorial_microarea where acao_id = ?", a);
            jdbc.update("delete from tb_acao_controle_vetorial where uuid = ?", a);
        }
        VigilanciaControllerTest.limparDados(jdbc, PREFIXO, CODIGO);
        TerritorioControllerTest.limparDados(jdbc, PREFIXO, CODIGO);
        jdbc.update("delete from tb_profissional where matricula like 'VET-%'");
    }

    private String enviar(MockHttpServletRequestBuilder r, String corpo, int esperado) throws Exception {
        return mockMvc.perform(r.contentType(MediaType.APPLICATION_JSON).content(corpo)).andExpect(status().is(esperado)).andReturn().getResponse()
                .getContentAsString(StandardCharsets.UTF_8);
    }

    private String ler(String url, int esperado) throws Exception {
        return mockMvc.perform(get(url)).andExpect(status().is(esperado)).andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
    }

    private String vistoria(int imovel, String resultado, String extra) throws Exception {
        return "{\"imovelId\": \"" + imoveis[imovel] + "\", \"agenteMatricula\": \"VET-ACE\", \"tipo\": \"ROTINA\", \"resultado\": \"" + resultado + "\"" + extra + "}";
    }

    @Test
    void vistoriasFocosEIndices() throws Exception {
        String url = "/api/v1/vetores/vistoria";
        // Imóveis da área, sem família; sem área nem microárea, 400.
        ler("/api/v1/vetores/imoveis", 400);
        String im = ler("/api/v1/vetores/imoveis?areaId=" + area + "&busca=flores", 200);
        assertThat((List<String>) JsonPath.read(im, "$[*].endereco")).hasSize(4).allMatch(e -> e.startsWith("Rua das Flores"));

        // As regras: data futura (400), fechado com depósito (422), foco acima dos depósitos do tipo (422), larvicida sem nome (400).
        enviar(post(url), vistoria(0, "INSPECIONADA", ", \"data\": \"" + hoje.plusDays(1) + "\""), 400);
        enviar(post(url), vistoria(0, "FECHADA", ", \"depositosB\": 2"), 422);
        enviar(post(url), vistoria(0, "INSPECIONADA", ", \"depositosB\": 1, \"focos\": [{\"tipoDeposito\": \"B\", \"fase\": \"LARVA\"}, "
                + "{\"tipoDeposito\": \"B\", \"fase\": \"PUPA\"}]"), 422);
        enviar(post(url), vistoria(0, "INSPECIONADA", ", \"larvicidaGramas\": 2.5"), 400);
        enviar(post(url), vistoria(0, "INSPECIONADA", "").replace("VET-ACE", "VET-NAO"), 404);

        // Imóvel 0: dois focos (um vai dar negativo no laboratório); imóvel 1: um foco; imóvel 2: sem foco; imóvel 3: fechado.
        String r = enviar(post(url), vistoria(0, "INSPECIONADA", ", \"depositosA2\": 3, \"depositosB\": 4, \"depositosTratados\": 2, \"larvicida\": \"Pyriproxyfen\", "
                + "\"larvicidaGramas\": 1.5, \"focos\": [{\"tipoDeposito\": \"A2\", \"fase\": \"LARVA\", \"especieSuspeita\": \"AEDES_AEGYPTI\", \"amostraColetada\": true}, "
                + "{\"tipoDeposito\": \"B\", \"fase\": \"LARVA_E_PUPA\", \"amostraColetada\": true}]"), 201);
        assertThat((Integer) JsonPath.read(r, "$.depositosInspecionados")).isEqualTo(7);
        assertThat((List<String>) JsonPath.read(r, "$.focos[*].situacao")).containsOnly("ATIVO");
        String negativo = JsonPath.read(r, "$.focos[?(@.tipoDeposito == 'B')].uuid").toString().replaceAll("[\\[\\]\"]", "");
        enviar(post(url), vistoria(1, "INSPECIONADA", ", \"depositosD1\": 2, \"focos\": [{\"tipoDeposito\": \"D1\", \"fase\": \"PUPA\"}]"), 201);
        enviar(post(url), vistoria(2, "INSPECIONADA", ", \"depositosC\": 1"), 201);
        enviar(post(url), vistoria(3, "FECHADA", ""), 201);

        // Pendência de revisita: o imóvel 3.
        assertThat((List<String>) JsonPath.read(ler("/api/v1/vetores/pendencias?areaId=" + area, 200), "$[*].uuid")).containsExactly(imoveis[3]);

        // Índices: 3 inspecionados, 2 positivos (IIP 66,7), 3 depósitos positivos (IB 100), 1 pendente.
        String ind = ler("/api/v1/vetores/indices", 200);
        String linha = "$.areas[?(@.areaId == '" + area + "')]";
        assertThat((List<Integer>) JsonPath.read(ind, linha + ".imoveisInspecionados")).containsExactly(3);
        assertThat((List<Integer>) JsonPath.read(ind, linha + ".imoveisPositivos")).containsExactly(2);
        assertThat((List<Double>) JsonPath.read(ind, linha + ".iip")).containsExactly(66.7);
        assertThat((List<Double>) JsonPath.read(ind, linha + ".ib")).containsExactly(100.0);
        assertThat((List<Integer>) JsonPath.read(ind, linha + ".imoveisPendentes")).containsExactly(1);
        assertThat((List<String>) JsonPath.read(ind, linha + ".classificacao")).containsExactly("RISCO");
        ler("/api/v1/vetores/indices?ciclo=7", 400);

        // O laboratório dá negativo para um foco: deixa de contar (IB 66,7); foco sem amostra, 422.
        String f = enviar(post("/api/v1/vetores/foco/" + negativo + "/laboratorio"), "{\"resultado\": \"NEGATIVO\"}", 200);
        assertThat((Boolean) JsonPath.read(f, "$.contaNosIndices")).isFalse();
        String semAmostra = JsonPath.read(ler("/api/v1/vetores/focos?areaId=" + area, 200), "$[?(@.tipoDeposito == 'D1')].uuid").toString().replaceAll("[\\[\\]\"]", "");
        enviar(post("/api/v1/vetores/foco/" + semAmostra + "/laboratorio"), "{\"resultado\": \"AEDES_AEGYPTI\"}", 422);
        assertThat((List<Double>) JsonPath.read(ler("/api/v1/vetores/indices", 200), linha + ".ib")).containsExactly(66.7);

        // Tratar o foco; de novo, 422; voltar a ativo, 400.
        enviar(post("/api/v1/vetores/foco/" + semAmostra + "/situacao"), "{\"situacao\": \"ATIVO\"}", 400);
        enviar(post("/api/v1/vetores/foco/" + semAmostra + "/situacao"), "{\"situacao\": \"ELIMINADO\", \"observacao\": \"Pneu recolhido\"}", 200);
        enviar(post("/api/v1/vetores/foco/" + semAmostra + "/situacao"), "{\"situacao\": \"TRATADO\"}", 422);
        assertThat((List<String>) JsonPath.read(ler("/api/v1/vetores/focos?situacao=ATIVO&areaId=" + area, 200), "$[*].tipoDeposito"))
                .containsExactlyInAnyOrder("A2", "B");

        // A revisita recupera a pendência.
        enviar(post(url), vistoria(3, "INSPECIONADA", ", \"depositosA1\": 1"), 201);
        assertThat((List<String>) JsonPath.read(ler("/api/v1/vetores/pendencias?areaId=" + area, 200), "$")).isEmpty();
    }

    @Test
    void acoesDeControleEBloqueios() throws Exception {
        // Um caso de dengue notificado sem bloqueio aparece; sarampo, não.
        Paciente p = new Paciente();
        p.setNome(PREFIXO + "Maria");
        p.setCpf("93990000001");
        p.setAtivo(true);
        p = pacienteRepository.save(p);
        String dengue = agravoRepository.findByCodigo("DENGUE").orElseThrow().getUuid().toString();
        String sarampo = agravoRepository.findByCodigo("SARAMPO").orElseThrow().getUuid().toString();
        String caso = JsonPath.read(enviar(post("/api/v1/vigilancia/notificacao"), "{\"pacienteId\": \"%s\", \"agravoId\": \"%s\", \"unidadeId\": \"%s\", \"origem\": \"ATENDIMENTO\", \"respostas\": {\"febre\": true}}"
                .formatted(p.getUuid(), dengue, ubs.getUuid()), 201), "$.uuid");
        String outro = JsonPath.read(enviar(post("/api/v1/vigilancia/notificacao"), "{\"pacienteId\": \"%s\", \"agravoId\": \"%s\", \"unidadeId\": \"%s\", \"origem\": \"ATENDIMENTO\"}"
                .formatted(p.getUuid(), sarampo, ubs.getUuid()), 201), "$.uuid");
        String pend = ler("/api/v1/vetores/bloqueios-pendentes", 200);
        assertThat((List<String>) JsonPath.read(pend, "$[*].notificacaoId")).contains(caso).doesNotContain(outro);
        assertThat(pend).doesNotContain(PREFIXO + "Maria");

        String acao = "{\"tipo\": \"BLOQUEIO_TRANSMISSAO\", \"microareaIds\": [\"" + microarea + "\"], \"dataPrevista\": \"" + hoje + "\", \"responsavel\": \"Equipe de endemias\"";
        enviar(post("/api/v1/vetores/acao"), acao + ", \"microareaIds\": []}", 400);
        enviar(post("/api/v1/vetores/acao"), acao + ", \"notificacaoId\": \"" + outro + "\"}", 422);
        String r = enviar(post("/api/v1/vetores/acao"), acao + ", \"notificacaoId\": \"" + caso + "\"}", 201);
        String id = JsonPath.read(r, "$.uuid");
        assertThat((String) JsonPath.read(r, "$.agravoNome")).containsIgnoringCase("dengue");
        enviar(post("/api/v1/vetores/acao"), acao + ", \"notificacaoId\": \"" + caso + "\"}", 409);
        assertThat((List<String>) JsonPath.read(ler("/api/v1/vetores/bloqueios-pendentes", 200), "$[*].notificacaoId")).doesNotContain(caso);

        // A vistoria do bloqueio entra na ação; realizada, não muda; cancelar a realizada, 422.
        enviar(post("/api/v1/vetores/vistoria"), vistoria(0, "INSPECIONADA", ", \"acaoId\": \"" + id + "\"").replace("ROTINA", "BLOQUEIO"), 201);
        enviar(post("/api/v1/vetores/acao/" + id + "/realizacao"), "{\"data\": \"" + hoje.plusDays(1) + "\"}", 400);
        r = enviar(post("/api/v1/vetores/acao/" + id + "/realizacao"), "{\"imoveisTrabalhados\": 42, \"resultado\": \"Raio de 150 m\"}", 200);
        assertThat((String) JsonPath.read(r, "$.situacao")).isEqualTo("REALIZADA");
        assertThat((Integer) JsonPath.read(r, "$.vistorias")).isEqualTo(1);
        enviar(post("/api/v1/vetores/acao/" + id + "/cancelamento"), "{\"motivo\": \"x\"}", 422);

        // Ação cancelada não recebe vistoria.
        String mutirao = JsonPath.read(enviar(post("/api/v1/vetores/acao"), acao.replace("BLOQUEIO_TRANSMISSAO", "MUTIRAO_LIMPEZA") + "}", 201), "$.uuid");
        enviar(post("/api/v1/vetores/acao/" + mutirao + "/cancelamento"), "{\"motivo\": \"Chuva forte\"}", 200);
        enviar(post("/api/v1/vetores/vistoria"), vistoria(1, "INSPECIONADA", ", \"acaoId\": \"" + mutirao + "\""), 422);
        assertThat((List<String>) JsonPath.read(ler("/api/v1/vetores/acoes?situacao=CANCELADA", 200), "$[*].uuid")).contains(mutirao);
    }
}
