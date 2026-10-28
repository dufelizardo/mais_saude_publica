package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.models.Paciente;
import com.edufelizardo.maissaudepublica.models.UnidadeDeSaude;
import com.edufelizardo.maissaudepublica.models.enuns.Sexo;
import com.edufelizardo.maissaudepublica.models.enuns.TipoUnidadeDeSaude;
import com.edufelizardo.maissaudepublica.repositories.PacienteRepository;
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
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Saúde Mental, fatia SM5 (ADR-0153): a RAPS pelos perfis e capacidades das unidades, com os casos e as intervenções dos
 * últimos 90 dias; o componente no catálogo do modelo operacional; e o acolhimento noturno do CAPS (configuração, entrada,
 * vagas, permanência máxima, prorrogação e saída).
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class RapsControllerTest {

    private static final String PREFIXO = "Raps Teste ";

    @Autowired private MockMvc mockMvc;
    @Autowired private JdbcTemplate jdbc;
    @Autowired private UnidadeDeSaudeRepository unidadeDeSaudeRepository;
    @Autowired private PacienteRepository pacienteRepository;

    private final LocalDate hoje = LocalDate.now();
    private UnidadeDeSaude caps;
    private UnidadeDeSaude upa;
    private int pacientes;

    @BeforeEach
    void seed() {
        limpar();
        caps = unidade("CAPS III", TipoUnidadeDeSaude.CAPS);
        upa = unidade("UPA", TipoUnidadeDeSaude.UPA);
        capacidade(caps, "CAPS_III");
        capacidade(caps, "ACOLHIMENTO_NOTURNO");
        jdbc.update("insert into tb_unidade_perfil_operacional (uuid, unidade_id, perfil_operacional_id, inicio) select gen_random_uuid(), ?, uuid, ? "
                + "from tb_perfil_operacional where codigo = 'URGENCIA'", upa.getUuid(), hoje.minusDays(1));
    }

    @AfterEach
    void limpar() {
        String unidades = "(select uuid from tb_unidade_de_saude where nome like '" + PREFIXO + "%')";
        String casos = "(select uuid from tb_acompanhamento_saude_mental where unidade_id in " + unidades + ")";
        jdbc.update("delete from tb_permanencia_acolhimento_noturno where unidade_id in " + unidades);
        jdbc.update("delete from tb_configuracao_acolhimento_noturno where unidade_id in " + unidades);
        jdbc.update("delete from tb_intervencao_saude_mental where acompanhamento_id in " + casos);
        jdbc.update("delete from tb_acompanhamento_saude_mental where unidade_id in " + unidades);
        jdbc.update("delete from tb_acompanhamento_programatico where unidade_id in " + unidades);
        jdbc.update("delete from tb_unidade_capacidade_assistencial where unidade_id in " + unidades);
        jdbc.update("delete from tb_unidade_perfil_operacional where unidade_id in " + unidades);
        jdbc.update("delete from tb_paciente where nome like '" + PREFIXO + "%'");
        jdbc.update("delete from tb_unidade_de_saude where nome like '" + PREFIXO + "%'");
    }

    private UnidadeDeSaude unidade(String nome, TipoUnidadeDeSaude tipo) {
        UnidadeDeSaude u = new UnidadeDeSaude();
        u.setNome(PREFIXO + nome);
        u.setTipo(tipo);
        u.setAtivo(true);
        return unidadeDeSaudeRepository.save(u);
    }

    private void capacidade(UnidadeDeSaude u, String codigo) {
        jdbc.update("insert into tb_unidade_capacidade_assistencial (uuid, unidade_id, capacidade_assistencial_id, inicio) select gen_random_uuid(), ?, uuid, ? "
                + "from tb_capacidade_assistencial where codigo = ?", u.getUuid(), hoje.minusDays(1), codigo);
    }

    /** Um paciente com caso de saúde mental vigente na unidade (e uma intervenção realizada há 10 dias). */
    private Paciente comCaso(UnidadeDeSaude u) {
        pacientes++;
        Paciente p = new Paciente();
        p.setNome(PREFIXO + "Paciente " + pacientes);
        p.setCpf(String.format("900222%05d", pacientes));
        p.setSexo(Sexo.MASCULINO);
        p.setAtivo(true);
        p = pacienteRepository.save(p);
        String programa = jdbc.queryForObject("insert into tb_acompanhamento_programatico (uuid, paciente_id, programa_id, unidade_id, inicio, situacao) "
                + "select gen_random_uuid(), ?, uuid, ?, ?, 'ATIVO' from tb_programa where codigo = 'SAUDE_MENTAL' returning cast(uuid as varchar)", String.class,
                p.getUuid(), u.getUuid(), hoje.minusDays(30));
        String caso = jdbc.queryForObject("insert into tb_acompanhamento_saude_mental (uuid, paciente_id, unidade_id, acompanhamento_programatico_id, inicio) "
                + "values (gen_random_uuid(), ?, ?, cast(? as uuid), ?) returning cast(uuid as varchar)", String.class, p.getUuid(), u.getUuid(), programa,
                hoje.minusDays(30));
        jdbc.update("insert into tb_intervencao_saude_mental (uuid, acompanhamento_id, tipo_id, realizada_em, resumo, situacao) select gen_random_uuid(), "
                + "cast(? as uuid), uuid, now() - interval '10 days', 'Oficina de música', 'REALIZADA' from tb_tipo_intervencao_saude_mental "
                + "where codigo = 'OFICINA'", caso);
        return p;
    }

    private String enviar(MockHttpServletRequestBuilder r, String corpo, int esperado) throws Exception {
        return mockMvc.perform(r.contentType(MediaType.APPLICATION_JSON).content(corpo)).andExpect(status().is(esperado)).andReturn().getResponse()
                .getContentAsString(StandardCharsets.UTF_8);
    }

    private String ler(String url) throws Exception {
        return mockMvc.perform(get(url)).andExpect(status().isOk()).andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
    }

    private static Map<String, Object> ponto(String painel, String componente, String unidadeId) {
        List<Map<String, Object>> p = JsonPath.read(painel, "$.componentes[?(@.componente == '" + componente + "')].unidades[?(@.unidadeId == '"
                + unidadeId + "')]");
        assertThat(p).hasSize(1);
        return p.get(0);
    }

    @Test
    void rapsPelosPerfisECapacidades() throws Exception {
        comCaso(caps);
        String painel = ler("/api/v1/saude-mental/raps");
        Map<String, Object> noCaps = ponto(painel, "ATENCAO_PSICOSSOCIAL_ESPECIALIZADA", caps.getUuid().toString());
        assertThat((List<String>) noCaps.get("capacidades")).containsExactly("Acolhimento noturno", "CAPS III");
        assertThat(noCaps).containsEntry("casosEmAcompanhamento", 1);
        assertThat((List<String>) JsonPath.read(noCaps, "$.intervencoes90Dias[*].chave")).containsExactly("Oficina");
        // A UPA entra pelo perfil, sem capacidade a listar.
        assertThat((List<String>) ponto(painel, "URGENCIA_EMERGENCIA", upa.getUuid().toString()).get("capacidades")).isEmpty();
        assertThat((List<String>) JsonPath.read(painel, "$.componentes[*].componente")).hasSize(7);

        // O componente no catálogo do modelo operacional; o ajuste sem ele mantém o que está.
        String catalogo = ler("/api/v1/capacidade-assistencial/");
        assertThat((List<String>) JsonPath.read(catalogo, "$[?(@.codigo == 'UNIDADE_ACOLHIMENTO_ADULTO')].componenteRaps")).containsExactly("RESIDENCIAL_TRANSITORIO");
    }

    @Test
    void acolhimentoNoturnoComVagasEPermanencia() throws Exception {
        Paciente a = comCaso(caps);
        Paciente b = comCaso(caps);
        Paciente semCaso = comCaso(upa);
        String entrada = "{\"unidadeId\": \"%s\", \"pacienteId\": \"%s\", \"saidaPrevista\": \"%s\"%s}";
        String url = "/api/v1/saude-mental/acolhimento-noturno";

        // Sem configuração, e sem a capacidade na unidade.
        enviar(post(url), entrada.formatted(caps.getUuid(), a.getUuid(), hoje.plusDays(3), ""), 422);
        enviar(put(url + "/configuracao?unidadeId=" + upa.getUuid()), "{\"vagas\": 2, \"permanenciaMaximaDias\": 14}", 422);
        String config = enviar(put(url + "/configuracao?unidadeId=" + caps.getUuid()), "{\"vagas\": 1, \"permanenciaMaximaDias\": 14}", 200);
        assertThat((Integer) JsonPath.read(config, "$.vagas")).isEqualTo(1);

        // O caso precisa ser da unidade; além do máximo, só com justificativa; a saída prevista não antes da entrada.
        enviar(post(url), entrada.formatted(caps.getUuid(), semCaso.getUuid(), hoje.plusDays(3), ""), 422);
        enviar(post(url), entrada.formatted(caps.getUuid(), a.getUuid(), hoje.plusDays(20), ""), 422);
        enviar(post(url), entrada.formatted(caps.getUuid(), a.getUuid(), hoje.minusDays(1), ""), 400);
        String p = enviar(post(url), entrada.formatted(caps.getUuid(), a.getUuid(), hoje.plusDays(20), ", \"justificativa\": \"Sem retaguarda familiar\""), 201);
        String permanencia = JsonPath.read(p, "$.uuid");
        assertThat((Boolean) JsonPath.read(p, "$.alemDoMaximo")).isTrue();

        // Já acolhido, 409; sem vaga, 422; as vagas não ficam abaixo da ocupação.
        enviar(post(url), entrada.formatted(caps.getUuid(), a.getUuid(), hoje.plusDays(3), ""), 409);
        enviar(post(url), entrada.formatted(caps.getUuid(), b.getUuid(), hoje.plusDays(3), ""), 422);
        enviar(put(url + "/configuracao?unidadeId=" + caps.getUuid()), "{\"vagas\": 2, \"permanenciaMaximaDias\": 14}", 200);
        String painel = ler(url + "?unidadeId=" + caps.getUuid());
        assertThat((Integer) JsonPath.read(painel, "$.ocupadas")).isEqualTo(1);
        assertThat((Boolean) JsonPath.read(painel, "$.temAcolhimentoNoturno")).isTrue();

        // Prorrogar dentro do máximo; sair com o motivo (sem motivo, 400); encerrada, não muda.
        p = enviar(post(url + "/" + permanencia + "/prorrogacao"), "{\"saidaPrevista\": \"%s\"}".formatted(hoje.plusDays(5)), 200);
        assertThat((Boolean) JsonPath.read(p, "$.alemDoMaximo")).isFalse();
        enviar(post(url + "/" + permanencia + "/saida"), "{\"motivo\": \"\"}", 400);
        p = enviar(post(url + "/" + permanencia + "/saida"), "{\"motivo\": \"Estabilizado, segue no CAPS\"}", 200);
        assertThat((String) JsonPath.read(p, "$.saida")).isEqualTo(hoje.toString());
        enviar(post(url + "/" + permanencia + "/saida"), "{\"motivo\": \"De novo\"}", 422);
        painel = ler(url + "?unidadeId=" + caps.getUuid());
        assertThat((Integer) JsonPath.read(painel, "$.ocupadas")).isZero();
        assertThat((List<String>) JsonPath.read(painel, "$.saidasRecentes[*].uuid")).containsExactly(permanencia);
        assertThat((List<String>) JsonPath.read(ler("/api/v1/saude-mental/paciente/" + a.getUuid() + "/acolhimento-noturno"), "$[*].uuid"))
                .containsExactly(permanencia);
    }
}
