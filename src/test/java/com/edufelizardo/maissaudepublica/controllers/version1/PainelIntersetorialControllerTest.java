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
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Painel da rede intersetorial, IS2 (ADR-0211): as contas do período e da situação de hoje, os filtros, a lista de
 * cobrança e a ausência de qualquer dado do paciente.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PainelIntersetorialControllerTest {

    private static final String PREFIXO = "PainelInterset Teste ";
    private static final String MOTIVO = "Motivo sigiloso do painel";

    @Autowired private MockMvc mockMvc;
    @Autowired private JdbcTemplate jdbc;
    @Autowired private UnidadeDeSaudeRepository unidadeDeSaudeRepository;
    @Autowired private PacienteRepository pacienteRepository;

    private final LocalDate hoje = LocalDate.now();
    private UnidadeDeSaude unidade;
    private Paciente paciente;
    private UUID creas;
    private UUID escola;
    private UUID atrasadoHa150;
    private UUID atrasadoHa5;
    private UUID atrasadoHa3;

    @BeforeEach
    void seed() {
        limpar();
        unidade = new UnidadeDeSaude();
        unidade.setNome(PREFIXO + "CAPS");
        unidade.setTipo(TipoUnidadeDeSaude.CAPS);
        unidade.setAtivo(true);
        unidade = unidadeDeSaudeRepository.save(unidade);
        paciente = new Paciente();
        paciente.setNome(PREFIXO + "Paciente");
        paciente.setCpf("90055500001");
        paciente.setSexo(Sexo.FEMININO);
        paciente.setAtivo(true);
        paciente = pacienteRepository.save(paciente);
        creas = instituicao("CREAS", "ASSISTENCIA_SOCIAL", "CREAS");
        escola = instituicao("Escola", "EDUCACAO", "ESCOLA");

        atrasadoHa3 = encaminhamento(creas, "SAUDE_MENTAL", 10, -3, "ENVIADO", null, null);
        atrasadoHa5 = encaminhamento(creas, "SAUDE_MENTAL", 20, -5, "RECEBIDO", 18, null);
        encaminhamento(creas, "SAUDE_MENTAL", 30, -15, "CONCLUIDO", null, 20);
        encaminhamento(creas, "VIGILANCIA", 40, -25, "RECUSADO", null, 36);
        // Fora dos 90 dias, mas ainda em aberto: entra em "em aberto" e "sem retorno", não em "enviados".
        atrasadoHa150 = encaminhamento(creas, "SAUDE_MENTAL", 200, -150, "ENVIADO", null, null);
        encaminhamento(escola, "SAUDE_MENTAL", 5, 10, "ENVIADO", null, null);
        encaminhamento(escola, "EMERGENCIAS", 50, -30, "CANCELADO", null, null);
    }

    @AfterEach
    void limpar() {
        jdbc.update("delete from tb_encaminhamento_intersetorial where unidade_id in (select uuid from tb_unidade_de_saude where nome like ?)", PREFIXO + "%");
        jdbc.update("delete from tb_instituicao_externa where nome like ?", PREFIXO + "%");
        jdbc.update("delete from tb_paciente where nome like ?", PREFIXO + "%");
        jdbc.update("delete from tb_unidade_de_saude where nome like ?", PREFIXO + "%");
    }

    private UUID instituicao(String nome, String setor, String tipo) {
        return jdbc.queryForObject("insert into tb_instituicao_externa (uuid, nome, setor, tipo, telefone, ativa, atualizada_em) "
                + "values (gen_random_uuid(), ?, ?, ?, '(11) 3333-0000', true, now()) returning uuid", UUID.class, PREFIXO + nome, setor, tipo);
    }

    /** Enviado há {@code enviadoHa} dias, com o retorno esperado em {@code esperadoEm} dias a partir de hoje (negativo: no passado). */
    private UUID encaminhamento(UUID instituicao, String origem, int enviadoHa, int esperadoEm, String situacao, Integer recebidoHa, Integer retornoHa) {
        return jdbc.queryForObject("insert into tb_encaminhamento_intersetorial (uuid, paciente_id, instituicao_id, dominio_origem, origem_id, unidade_id, "
                        + "motivo, enviado_em, retorno_esperado_em, situacao, recebido_em, retorno_em, retorno, motivo_cancelamento, registrado_em) "
                        + "values (gen_random_uuid(), ?, ?, ?, gen_random_uuid(), ?, ?, ?, ?, ?, ?, ?, ?, ?, now()) returning uuid", UUID.class,
                paciente.getUuid(), instituicao, origem, unidade.getUuid(), MOTIVO, hoje.minusDays(enviadoHa), hoje.plusDays(esperadoEm), situacao,
                recebidoHa == null ? null : hoje.minusDays(recebidoHa), retornoHa == null ? null : hoje.minusDays(retornoHa),
                retornoHa == null ? null : "Retorno da instituição", "CANCELADO".equals(situacao) ? "Paciente mudou de cidade" : null);
    }

    private String ler(String url) throws Exception {
        return mockMvc.perform(get(url)).andExpect(status().isOk()).andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
    }

    private int codigo(String url) throws Exception {
        return mockMvc.perform(get(url)).andReturn().getResponse().getStatus();
    }

    @Test
    void contasDoPeriodoEDaSituacaoDeHoje() throws Exception {
        String r = ler("/api/v1/intersetorial/painel?unidade=" + unidade.getUuid());
        assertThat((String) JsonPath.read(r, "$.inicio")).isEqualTo(hoje.minusDays(89).toString());
        assertThat((String) JsonPath.read(r, "$.fim")).isEqualTo(hoje.toString());
        assertThat((Integer) JsonPath.read(r, "$.enviados")).isEqualTo(6);
        assertThat((Integer) JsonPath.read(r, "$.emAberto")).isEqualTo(4);
        assertThat((Integer) JsonPath.read(r, "$.semRetorno")).isEqualTo(3);
        assertThat((Integer) JsonPath.read(r, "$.concluidos")).isEqualTo(1);
        assertThat((Integer) JsonPath.read(r, "$.recusados")).isEqualTo(1);
        assertThat((Integer) JsonPath.read(r, "$.cancelados")).isEqualTo(1);
        assertThat((Double) JsonPath.read(r, "$.taxaResolucao")).isEqualTo(50.0);
        // Concluído em 10 dias e recusado em 4: média de 7.
        assertThat((Double) JsonPath.read(r, "$.tempoMedioRetornoDias")).isEqualTo(7.0);

        // A instituição com mais casos sem retorno vem primeiro.
        assertThat((String) JsonPath.read(r, "$.instituicoes[0].instituicaoId")).isEqualTo(creas.toString());
        assertThat((Integer) JsonPath.read(r, "$.instituicoes[0].enviados")).isEqualTo(4);
        assertThat((Integer) JsonPath.read(r, "$.instituicoes[0].emAberto")).isEqualTo(3);
        assertThat((Integer) JsonPath.read(r, "$.instituicoes[0].semRetorno")).isEqualTo(3);
        assertThat((Integer) JsonPath.read(r, "$.instituicoes[0].enviadosPorOrigem.SAUDE_MENTAL")).isEqualTo(3);
        assertThat((Integer) JsonPath.read(r, "$.instituicoes[0].enviadosPorOrigem.VIGILANCIA")).isEqualTo(1);
        assertThat((String) JsonPath.read(r, "$.instituicoes[1].instituicaoId")).isEqualTo(escola.toString());
        assertThat((Integer) JsonPath.read(r, "$.instituicoes[1].semRetorno")).isZero();
        assertThat((Object) JsonPath.read(r, "$.instituicoes[1].taxaResolucao")).isNull();
        assertThat((Object) JsonPath.read(r, "$.instituicoes[1].tempoMedioRetornoDias")).isNull();

        // Nenhum dado do paciente nem o motivo.
        assertThat(r).doesNotContain(paciente.getNome(), paciente.getCpf(), paciente.getUuid().toString(), MOTIVO);
    }

    @Test
    void filtrosPorSetorOrigemEPeriodo() throws Exception {
        String base = "/api/v1/intersetorial/painel?unidade=" + unidade.getUuid();
        String educacao = ler(base + "&setor=EDUCACAO");
        assertThat((List<?>) JsonPath.read(educacao, "$.instituicoes")).hasSize(1);
        assertThat((Integer) JsonPath.read(educacao, "$.enviados")).isEqualTo(2);
        assertThat((Integer) JsonPath.read(educacao, "$.semRetorno")).isZero();

        String vigilancia = ler(base + "&origem=VIGILANCIA");
        assertThat((Integer) JsonPath.read(vigilancia, "$.enviados")).isEqualTo(1);
        assertThat((Integer) JsonPath.read(vigilancia, "$.recusados")).isEqualTo(1);
        assertThat((Double) JsonPath.read(vigilancia, "$.taxaResolucao")).isEqualTo(0.0);

        // Só os últimos 15 dias: os enviados há 5 e há 10 dias; os em aberto continuam todos.
        String quinzena = ler(base + "&inicio=" + hoje.minusDays(15) + "&fim=" + hoje);
        assertThat((Integer) JsonPath.read(quinzena, "$.enviados")).isEqualTo(2);
        assertThat((Integer) JsonPath.read(quinzena, "$.emAberto")).isEqualTo(4);

        assertThat(codigo(base + "&inicio=" + hoje + "&fim=" + hoje.minusDays(1))).isEqualTo(400);
        assertThat(codigo(base + "&inicio=" + hoje.minusDays(400) + "&fim=" + hoje)).isEqualTo(400);
    }

    @Test
    void listaDeCobrancaSemPaciente() throws Exception {
        String r = ler("/api/v1/intersetorial/instituicao/" + creas + "/sem-retorno");
        assertThat((String) JsonPath.read(r, "$.instituicao.nome")).isEqualTo(PREFIXO + "CREAS");
        assertThat((List<String>) JsonPath.read(r, "$.semRetorno[*].encaminhamentoId"))
                .containsExactly(atrasadoHa150.toString(), atrasadoHa5.toString(), atrasadoHa3.toString());
        assertThat((Integer) JsonPath.read(r, "$.semRetorno[0].diasDeAtraso")).isEqualTo(150);
        assertThat((String) JsonPath.read(r, "$.semRetorno[0].numero")).isEqualTo(atrasadoHa150.toString().substring(0, 8).toUpperCase());
        assertThat((String) JsonPath.read(r, "$.semRetorno[1].situacao")).isEqualTo("RECEBIDO");
        assertThat((String) JsonPath.read(r, "$.semRetorno[1].unidadeNome")).isEqualTo(PREFIXO + "CAPS");
        assertThat(r).doesNotContain(paciente.getNome(), paciente.getCpf(), paciente.getUuid().toString(), MOTIVO);

        String vazia = ler("/api/v1/intersetorial/instituicao/" + escola + "/sem-retorno");
        assertThat((List<?>) JsonPath.read(vazia, "$.semRetorno")).isEmpty();
        assertThat(codigo("/api/v1/intersetorial/instituicao/" + UUID.randomUUID() + "/sem-retorno")).isEqualTo(404);
    }
}
