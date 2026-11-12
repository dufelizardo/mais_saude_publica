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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

/**
 * Gestão da Rede, GR3 (ADR-0198): a cobertura da rede com os percentuais da pactuação, a população adscrita da unidade e da esfera, a cota
 * por mil, a fila, os alertas (cota esgotada com fila, demanda sem cota, sem referência, cota ociosa só na competência fechada) e o
 * relatório do #19.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CoberturaRedeControllerTest {

    private static final String PREFIXO = "CobRede Teste ";
    private static final String URL = "/api/v1/rede/";
    private static final String REG = "/api/v1/solicitacao-regulacao/";
    private static final YearMonth MES = YearMonth.now().plusMonths(1);
    private static final YearMonth PASSADO = YearMonth.now().minusMonths(2);

    @Autowired private MockMvc mockMvc;
    @Autowired private JdbcTemplate jdbc;
    @Autowired private UnidadeDeSaudeRepository unidadeRepository;
    @Autowired private PacienteRepository pacienteRepository;
    @Autowired private ProfissionalRepository profissionalRepository;

    private UnidadeDeSaude municipio;
    private UnidadeDeSaude ubs;
    private UnidadeDeSaude ubs2;
    private UnidadeDeSaude fora;
    private UnidadeDeSaude policlinica;
    private String procedimento;
    private int pacientes;

    @BeforeEach
    void seed() {
        limpar();
        municipio = unidade("Município", TipoUnidadeDeSaude.MUNICIPAL, null);
        UnidadeDeSaude municipio2 = unidade("Município 2", TipoUnidadeDeSaude.MUNICIPAL, null);
        ubs = unidade("UBS", TipoUnidadeDeSaude.UBS, municipio);
        ubs2 = unidade("UBS 2", TipoUnidadeDeSaude.UBS, municipio);
        fora = unidade("Fora", TipoUnidadeDeSaude.UBS, municipio2);
        unidade("UBS 3", TipoUnidadeDeSaude.UBS, municipio2);
        policlinica = unidade("Policlínica", TipoUnidadeDeSaude.POLICLINICA, municipio);
        procedimento = UUID.randomUUID().toString();
        jdbc.update("insert into tb_procedimento_regulado (uuid, nome, tipo, ativo) values (cast(? as uuid), ?, 'CONSULTA_ESPECIALIZADA', true)",
                procedimento, PREFIXO + "Cardiologia");
        for (String m : List.of("CR-SOLICITANTE", "CR-REGULADOR")) {
            Profissional p = new Profissional();
            p.setMatricula(m);
            p.setCpf(m.equals("CR-REGULADOR") ? "90098000001" : "90098000002");
            p.setNome(PREFIXO + m);
            p.setAtivo(true);
            profissionalRepository.save(p);
        }
        pacientes = 0;
        adscrever(ubs, 4);
        adscrever(ubs2, 6);
    }

    @AfterEach
    void limpar() {
        String unidades = "(select uuid from tb_unidade_de_saude where nome like '" + PREFIXO + "%')";
        String solicitacoes = "(select uuid from tb_solicitacao_regulacao where unidade_solicitante_id in " + unidades + ")";
        jdbc.update("delete from tb_evento_regulacao where solicitacao_id in " + solicitacoes);
        jdbc.update("delete from tb_solicitacao_regulacao where unidade_solicitante_id in " + unidades);
        jdbc.update("delete from tb_referencia_rede where origem_id in " + unidades);
        jdbc.update("delete from tb_cota_ppi where oferta_id in (select uuid from tb_oferta_assistencial where unidade_executante_id in " + unidades + ")");
        jdbc.update("delete from tb_oferta_assistencial where unidade_executante_id in " + unidades);
        jdbc.update("delete from tb_adscricao where equipe_id in (select uuid from tb_equipe where unidade_id in " + unidades + ")");
        jdbc.update("delete from tb_equipe where unidade_id in " + unidades);
        jdbc.update("delete from tb_procedimento_regulado where nome like '" + PREFIXO + "%'");
        jdbc.update("delete from tb_paciente where nome like '" + PREFIXO + "%'");
        jdbc.update("delete from tb_profissional where matricula like 'CR-%'");
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

    private Paciente paciente() {
        Paciente p = new Paciente();
        p.setNome(PREFIXO + "Paciente " + (++pacientes));
        p.setCpf(String.format("9098%07d", pacientes));
        p.setAtivo(true);
        return pacienteRepository.save(p);
    }

    private void adscrever(UnidadeDeSaude u, int quantos) {
        String equipe = jdbc.queryForObject("insert into tb_equipe (uuid, unidade_id, nome, tipo, ativa) values (gen_random_uuid(), ?, ?, 'ESF', true) "
                + "returning cast(uuid as varchar)", String.class, u.getUuid(), PREFIXO + "Equipe " + u.getNome());
        for (int i = 0; i < quantos; i++) {
            jdbc.update("insert into tb_adscricao (uuid, paciente_id, equipe_id, origem, inicio) values (gen_random_uuid(), ?, cast(? as uuid), 'MANUAL', ?)",
                    paciente().getUuid(), equipe, LocalDate.now().minusDays(30));
        }
    }

    private String enviar(MockHttpServletRequestBuilder r, String corpo, int esperado) throws Exception {
        var resposta = mockMvc.perform(r.contentType(MediaType.APPLICATION_JSON).content(corpo)).andReturn().getResponse();
        assertThat(resposta.getStatus()).as(resposta.getContentAsString(StandardCharsets.UTF_8)).isEqualTo(esperado);
        return resposta.getContentAsString(StandardCharsets.UTF_8);
    }

    private String ler(String url) throws Exception {
        return enviar(get(url), "", 200);
    }

    private String oferta(YearMonth mes, int qtd) throws Exception {
        return JsonPath.read(enviar(post(URL + "oferta"), "{\"executanteId\": \"%s\", \"procedimentoId\": \"%s\", \"competencia\": \"%s\", \"quantidade\": %d}"
                .formatted(policlinica.getUuid(), procedimento, mes, qtd), 201), "$.uuid");
    }

    private void cota(String oferta, UnidadeDeSaude beneficiaria, int qtd) throws Exception {
        enviar(post(URL + "oferta/" + oferta + "/cota"), "{\"beneficiariaId\": \"%s\", \"quantidade\": %d}".formatted(beneficiaria.getUuid(), qtd), 201);
    }

    private String solicitar(UnidadeDeSaude solicitante) throws Exception {
        Paciente p = paciente();
        enviar(post(REG), ("{\"pacienteId\": \"%s\", \"procedimentoId\": \"%s\", \"unidadeSolicitanteId\": \"%s\", \"profissionalMatricula\": \"CR-SOLICITANTE\", "
                + "\"cid\": \"I10\", \"justificativa\": \"Hipertensão resistente.\", \"prioridade\": \"VERDE\"}").formatted(p.getUuid(), procedimento, solicitante.getUuid()), 201);
        return jdbc.queryForObject("select cast(uuid as varchar) from tb_solicitacao_regulacao where paciente_id = ?", String.class, p.getUuid());
    }

    private void autorizar(UnidadeDeSaude solicitante, String extra) throws Exception {
        enviar(post(REG + solicitar(solicitante) + "/autorizacao"), "{\"profissionalMatricula\": \"CR-REGULADOR\", \"unidadeExecutanteId\": \"%s\", \"dataHoraPrevista\": \"%s\"%s}"
                .formatted(policlinica.getUuid(), MES.atDay(10).atTime(9, 0), extra), 200);
    }

    private Object origem(String json, UnidadeDeSaude u, String campo) {
        List<Object> v = JsonPath.read(json, "$.procedimentos[0].origens[?(@.unidadeId == '" + u.getUuid() + "')]." + campo);
        assertThat(v).as(u.getNome() + " " + campo).hasSize(1);
        return v.get(0);
    }

    @Test
    void coberturaDaCompetencia() throws Exception {
        String oferta = oferta(MES, 10);
        cota(oferta, ubs, 2);
        cota(oferta, municipio, 3);
        enviar(post(URL + "referencia"), "{\"origemId\": \"%s\", \"procedimentoId\": \"%s\", \"executanteId\": \"%s\"}"
                .formatted(municipio.getUuid(), procedimento, policlinica.getUuid()), 201);
        autorizar(ubs, "");
        autorizar(ubs, "");
        autorizar(ubs, ", \"extraCota\": true, \"justificativaExtraCota\": \"Risco alto.\"");
        autorizar(ubs2, "");
        solicitar(ubs);
        solicitar(fora);

        String r = ler(URL + "cobertura?competencia=" + MES + "&procedimentoId=" + procedimento);
        assertThat((Boolean) JsonPath.read(r, "$.fechada")).isFalse();
        assertThat((Integer) JsonPath.read(r, "$.procedimentos[0].ofertado")).isEqualTo(10);
        assertThat((Integer) JsonPath.read(r, "$.procedimentos[0].pactuado")).isEqualTo(5);
        assertThat((Integer) JsonPath.read(r, "$.procedimentos[0].utilizado")).isEqualTo(4);
        assertThat((Integer) JsonPath.read(r, "$.procedimentos[0].extracotas")).isEqualTo(1);
        assertThat((Double) JsonPath.read(r, "$.procedimentos[0].percentualPactuado")).isEqualTo(50.0);
        assertThat((Double) JsonPath.read(r, "$.procedimentos[0].percentualCotaUsada")).isEqualTo(60.0);
        assertThat((Double) JsonPath.read(r, "$.procedimentos[0].percentualExtracota")).isEqualTo(25.0);
        assertThat((Double) JsonPath.read(r, "$.procedimentos[0].percentualForaDaReferencia")).isEqualTo(0.0);
        assertThat((Integer) JsonPath.read(r, "$.procedimentos[0].naFila")).isEqualTo(2);
        assertThat((Object) JsonPath.read(r, "$.procedimentos[0].esperaNaFilaDias")).isNotNull();

        // A UBS: a própria cota esgotada, com fila; a população é a das suas equipes.
        assertThat(origem(r, ubs, "populacaoAdscrita")).isEqualTo(4);
        assertThat(origem(r, ubs, "cotaPorMil")).isEqualTo(500.0);
        assertThat(origem(r, ubs, "autorizadas")).isEqualTo(3);
        assertThat(origem(r, ubs, "aguardando")).isEqualTo(1);
        assertThat(origem(r, ubs, "saldo")).isEqualTo(0);
        assertThat((List<String>) origem(r, ubs, "alertas")).containsExactly("Cota esgotada com fila");
        // O município cobre a UBS 2 e soma a população das unidades abaixo.
        assertThat(origem(r, municipio, "populacaoAdscrita")).isEqualTo(10);
        assertThat(origem(r, municipio, "cotaPorMil")).isEqualTo(300.0);
        assertThat(origem(r, municipio, "saldo")).isEqualTo(2);
        assertThat(origem(r, municipio, "comReferencia")).isEqualTo(true);
        // Fora do município: demanda sem cota e sem referência; a UBS 3, sem demanda, entra nos alertas.
        assertThat((List<String>) origem(r, fora, "alertas")).containsExactly("Demanda sem cota", "Sem referência");
        assertThat((List<String>) JsonPath.read(r, "$.alertas")).contains(PREFIXO + "Cardiologia: " + PREFIXO + "UBS 3 · sem referência.");
        assertThat((Integer) JsonPath.read(r, "$.resumo.cotasEsgotadasComFila")).isEqualTo(1);

        // O relatório do #19, do dia de hoje até o fim da competência, pelo município: a espera até a autorização entra.
        String rel = ler("/api/v1/relatorios/PACTUACAO_REDE?de=" + LocalDate.now() + "&ate=" + MES.atEndOfMonth() + "&unidadeId=" + municipio.getUuid());
        assertThat((String) JsonPath.read(rel, "$.categoria")).isEqualTo("Gestão da rede");
        List<List<String>> linhas = JsonPath.read(rel, "$.secoes[0].linhas[?(@[0] == '" + PREFIXO + "Cardiologia')]");
        assertThat(linhas).hasSize(1);
        assertThat(linhas.get(0).get(1)).isEqualTo("10");
        assertThat(linhas.get(0).get(11)).isNotEmpty();
        enviar(get("/api/v1/relatorios/PACTUACAO_REDE/exportacao?unidadeId=" + municipio.getUuid()), "", 200);
    }

    @Test
    void cotaOciosaSoNaCompetenciaFechada() throws Exception {
        cota(oferta(PASSADO, 4), ubs2, 2);
        String passado = ler(URL + "cobertura?competencia=" + PASSADO + "&procedimentoId=" + procedimento);
        assertThat((Boolean) JsonPath.read(passado, "$.fechada")).isTrue();
        assertThat((List<String>) origem(passado, ubs2, "alertas")).containsExactly("Cota ociosa");

        cota(oferta(MES, 4), ubs2, 2);
        String aberta = ler(URL + "cobertura?competencia=" + MES + "&procedimentoId=" + procedimento);
        assertThat((List<String>) origem(aberta, ubs2, "alertas")).isEmpty();
    }
}
