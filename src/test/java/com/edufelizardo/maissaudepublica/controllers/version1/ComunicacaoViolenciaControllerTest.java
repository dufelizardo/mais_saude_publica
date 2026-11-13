package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.models.Paciente;
import com.edufelizardo.maissaudepublica.models.UnidadeDeSaude;
import com.edufelizardo.maissaudepublica.models.enuns.Sexo;
import com.edufelizardo.maissaudepublica.models.enuns.TipoUnidadeDeSaude;
import com.edufelizardo.maissaudepublica.repositories.AgravoRepository;
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
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

/**
 * A comunicação obrigatória da violência notificada, fatia V3c (ADR-0201): Conselho Tutelar para menor de 18 anos (inclusive a lesão
 * autoprovocada), conselho de direitos, Ministério Público ou polícia para a pessoa idosa, polícia para a mulher; sem obrigação para o
 * homem adulto e a lesão autoprovocada do adulto; o tipo aceito, a feita no prazo, a cancelada, a vencida e o relatório.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ComunicacaoViolenciaControllerTest {

    static final String PREFIXO = "Violencia Teste ";
    private static final String API = "/api/v1/vigilancia/";

    @Autowired private MockMvc mockMvc;
    @Autowired private JdbcTemplate jdbc;
    @Autowired private UnidadeDeSaudeRepository unidadeDeSaudeRepository;
    @Autowired private PacienteRepository pacienteRepository;
    @Autowired private AgravoRepository agravoRepository;

    private final LocalDate hoje = LocalDate.now();
    private UnidadeDeSaude ubs;
    private String violencia;
    private String dengue;

    @BeforeEach
    void seed() {
        limpar();
        UnidadeDeSaude u = new UnidadeDeSaude();
        u.setNome(PREFIXO + "UBS");
        u.setTipo(TipoUnidadeDeSaude.UBS);
        u.setAtivo(true);
        ubs = unidadeDeSaudeRepository.save(u);
        violencia = agravoRepository.findByCodigo("VIOLENCIA_INTERPESSOAL_AUTOPROVOCADA").orElseThrow().getUuid().toString();
        dengue = agravoRepository.findByCodigo("DENGUE").orElseThrow().getUuid().toString();
    }

    @AfterEach
    void limpar() {
        limparDados(jdbc, PREFIXO);
    }

    static void limparDados(JdbcTemplate jdbc, String prefixo) {
        String unidades = "(select uuid from tb_unidade_de_saude where nome like '" + prefixo + "%')";
        jdbc.update("delete from tb_encaminhamento_intersetorial where unidade_id in " + unidades);
        jdbc.update("delete from tb_instituicao_externa where nome like ?", prefixo + "%");
        VigilanciaControllerTest.limparDados(jdbc, prefixo, "TSTV3C");
    }

    static String instituicao(JdbcTemplate jdbc, String prefixo, String nome, String setor, String tipo) {
        UUID id = UUID.randomUUID();
        jdbc.update("insert into tb_instituicao_externa (ativa, atualizada_em, uuid, setor, tipo, nome) values (true, now(), ?, ?, ?, ?)",
                id, setor, tipo, prefixo + nome);
        return id.toString();
    }

    private Paciente paciente(String nome, String cpf, int idade, Sexo sexo) {
        Paciente p = new Paciente();
        p.setNome(PREFIXO + nome);
        p.setCpf(cpf);
        p.setDataNascimento(hoje.minusYears(idade).minusDays(10));
        p.setSexo(sexo);
        p.setAtivo(true);
        return pacienteRepository.save(p);
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

    private String notificar(Paciente p, String tipo, boolean autoprovocada) throws Exception {
        return JsonPath.read(enviar(post(API + "notificacao"), "{\"pacienteId\": \"" + p.getUuid() + "\", \"agravoId\": \"" + violencia + "\", \"unidadeId\": \""
                + ubs.getUuid() + "\", \"origem\": \"ATENDIMENTO\", \"respostas\": {\"tipoViolencia\": \"" + tipo + "\", \"lesaoAutoprovocada\": "
                + autoprovocada + "}}", 201), "$.uuid");
    }

    @Test
    void criancaVaiAoConselhoTutelar() throws Exception {
        String ct = instituicao(jdbc, PREFIXO, "Conselho Tutelar", "GARANTIA_DE_DIREITOS", "CONSELHO_TUTELAR");
        String cras = instituicao(jdbc, PREFIXO, "CRAS", "ASSISTENCIA_SOCIAL", "CRAS");
        String n = notificar(paciente("Ana", "90044400001", 10, Sexo.FEMININO), "Física", false);
        String r = ler(API + "notificacao/" + n + "/comunicacao");
        assertThat((Boolean) JsonPath.read(r, "$.obrigatoria")).isTrue();
        assertThat((String) JsonPath.read(r, "$.destino")).isEqualTo("Conselho Tutelar");
        assertThat((String) JsonPath.read(r, "$.situacao")).isEqualTo("PENDENTE");
        assertThat((List<String>) JsonPath.read(ler(API + "comunicacoes-pendentes"), "$[*].notificacaoId")).contains(n);

        // Tipo errado, 422; o Conselho Tutelar cumpre no prazo, com o motivo mínimo.
        enviar(post(API + "notificacao/" + n + "/comunicacao"), "{\"instituicaoId\": \"" + cras + "\"}", 422);
        r = enviar(post(API + "notificacao/" + n + "/comunicacao"), "{\"instituicaoId\": \"" + ct + "\"}", 201);
        assertThat((String) JsonPath.read(r, "$.situacao")).isEqualTo("FEITA");
        assertThat((Boolean) JsonPath.read(r, "$.noPrazo")).isTrue();
        assertThat((String) JsonPath.read(r, "$.encaminhamentos[0].motivo")).startsWith("Comunicação obrigatória de violência (ECA, arts. 13 e 245): física");
        assertThat((String) JsonPath.read(r, "$.encaminhamentos[0].retornoEsperadoEm")).isEqualTo(hoje.plusDays(30).toString());
        assertThat((List<String>) JsonPath.read(ler(API + "comunicacoes-pendentes"), "$[*].notificacaoId")).doesNotContain(n);
        enviar(post(API + "notificacao/" + n + "/comunicacao"), "{\"instituicaoId\": \"" + ct + "\"}", 409);

        // Cancelada, volta a pendente; e vencida depois das 24 horas.
        String enc = JsonPath.read(r, "$.encaminhamentos[0].uuid");
        r = enviar(post(API + "notificacao/" + n + "/comunicacao/" + enc + "/cancelamento"), "{\"motivo\": \"Instituição errada.\"}", 200);
        assertThat((String) JsonPath.read(r, "$.situacao")).isEqualTo("PENDENTE");
        jdbc.update("update tb_notificacao set registrada_em = now() - interval '2 days' where uuid = ?", UUID.fromString(n));
        assertThat((String) JsonPath.read(ler(API + "notificacao/" + n + "/comunicacao"), "$.situacao")).isEqualTo("VENCIDA");
        assertThat((List<Boolean>) JsonPath.read(ler(API + "comunicacoes-pendentes"), "$[?(@.notificacaoId == '" + n + "')].vencida")).containsExactly(true);
        r = enviar(post(API + "notificacao/" + n + "/comunicacao"), "{\"instituicaoId\": \"" + ct + "\"}", 201);
        assertThat((Boolean) JsonPath.read(r, "$.noPrazo")).isFalse();

        // A lesão autoprovocada do adolescente também vai ao Conselho Tutelar.
        String auto = notificar(paciente("Bia", "90044400002", 15, Sexo.FEMININO), "Autoprovocada", true);
        assertThat((String) JsonPath.read(ler(API + "notificacao/" + auto + "/comunicacao"), "$.destino")).isEqualTo("Conselho Tutelar");

        // O relatório Vigilância do #19.
        String rel = ler("/api/v1/relatorios/VIGILANCIA?de=" + hoje.minusDays(7) + "&ate=" + hoje);
        assertThat((List<String>) JsonPath.read(rel, "$.secoes[*].titulo")).contains("Comunicações obrigatórias da violência");
        assertThat((List<String>) JsonPath.read(rel, "$.secoes[?(@.titulo == 'Comunicações obrigatórias da violência')].linhas[*][0]")).contains("Conselho Tutelar");
    }

    @Test
    void idosaMulherESemObrigacao() throws Exception {
        String mp = instituicao(jdbc, PREFIXO, "Promotoria", "GARANTIA_DE_DIREITOS", "MINISTERIO_PUBLICO");
        String conselho = instituicao(jdbc, PREFIXO, "Conselho da Pessoa Idosa", "GARANTIA_DE_DIREITOS", "CONSELHO_DE_DIREITOS");
        String delegacia = instituicao(jdbc, PREFIXO, "Delegacia da Mulher", "SEGURANCA_PUBLICA", "SEGURANCA_PUBLICA");

        String idosa = notificar(paciente("Dona Rosa", "90044400003", 72, Sexo.FEMININO), "Negligência ou abandono", false);
        String r = ler(API + "notificacao/" + idosa + "/comunicacao");
        assertThat((List<String>) JsonPath.read(r, "$.tiposAceitos")).containsExactly("CONSELHO_DE_DIREITOS", "MINISTERIO_PUBLICO", "SEGURANCA_PUBLICA");
        r = enviar(post(API + "notificacao/" + idosa + "/comunicacao"), "{\"instituicaoId\": \"" + mp + "\"}", 201);
        assertThat((String) JsonPath.read(r, "$.situacao")).isEqualTo("FEITA");

        String mulher = notificar(paciente("Carla", "90044400004", 30, Sexo.FEMININO), "Física", false);
        assertThat((String) JsonPath.read(ler(API + "notificacao/" + mulher + "/comunicacao"), "$.destino")).isEqualTo("Autoridade policial");
        enviar(post(API + "notificacao/" + mulher + "/comunicacao"), "{\"instituicaoId\": \"" + conselho + "\"}", 422);
        enviar(post(API + "notificacao/" + mulher + "/comunicacao"), "{\"instituicaoId\": \"" + delegacia + "\", \"enviadoEm\": \"" + hoje.minusDays(3) + "\"}", 400);
        enviar(post(API + "notificacao/" + mulher + "/comunicacao"), "{\"instituicaoId\": \"" + delegacia + "\"}", 201);

        // Sem obrigação: o homem adulto, a lesão autoprovocada da mulher adulta, outro agravo.
        Paciente joao = paciente("João", "90044400005", 30, Sexo.MASCULINO);
        String homem = notificar(joao, "Física", false);
        r = ler(API + "notificacao/" + homem + "/comunicacao");
        assertThat((Boolean) JsonPath.read(r, "$.obrigatoria")).isFalse();
        assertThat((String) JsonPath.read(r, "$.situacao")).isEqualTo("NAO_SE_APLICA");
        enviar(post(API + "notificacao/" + homem + "/comunicacao"), "{\"instituicaoId\": \"" + delegacia + "\"}", 422);
        String autoAdulta = notificar(paciente("Lia", "90044400006", 35, Sexo.FEMININO), "Autoprovocada", true);
        assertThat((Boolean) JsonPath.read(ler(API + "notificacao/" + autoAdulta + "/comunicacao"), "$.obrigatoria")).isFalse();
        String d = JsonPath.read(enviar(post(API + "notificacao"), "{\"pacienteId\": \"" + paciente("Téo", "90044400007", 8, Sexo.MASCULINO).getUuid()
                + "\", \"agravoId\": \"" + dengue + "\", \"unidadeId\": \"" + ubs.getUuid() + "\", \"origem\": \"ATENDIMENTO\", \"respostas\": {\"febre\": true}}", 201), "$.uuid");
        assertThat((Boolean) JsonPath.read(ler(API + "notificacao/" + d + "/comunicacao"), "$.obrigatoria")).isFalse();
    }
}
