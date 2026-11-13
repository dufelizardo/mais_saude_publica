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
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

/**
 * A investigação do trabalho, fatia V5b (ADR-0208): a visita, o nexo, a medida com prazo e verificação, a comunicação à vigilância
 * sanitária sem os dados do trabalhador, o encaminhamento intersetorial, o risco ocupacional, a medida vencida no painel, a semana da Sala
 * de Situação e o relatório Vigilância sem as de nexo descartado.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class InvestigacaoTrabalhoControllerTest {

    static final String PREFIXO = "InvTrab Teste ";
    private static final String API = "/api/v1/vigilancia/";

    @Autowired private MockMvc mockMvc;
    @Autowired private JdbcTemplate jdbc;
    @Autowired private UnidadeDeSaudeRepository unidadeDeSaudeRepository;
    @Autowired private PacienteRepository pacienteRepository;
    @Autowired private AgravoRepository agravoRepository;

    private final LocalDate hoje = LocalDate.now();
    private UnidadeDeSaude ubs;

    @BeforeEach
    void seed() {
        limpar();
        UnidadeDeSaude u = new UnidadeDeSaude();
        u.setNome(PREFIXO + "UBS");
        u.setTipo(TipoUnidadeDeSaude.UBS);
        u.setAtivo(true);
        ubs = unidadeDeSaudeRepository.save(u);
    }

    @AfterEach
    void limpar() {
        jdbc.update("delete from tb_risco_ocupacional where empregador_nome like ?", PREFIXO + "%");
        ComunicacaoViolenciaControllerTest.limparDados(jdbc, PREFIXO);
    }

    private Paciente paciente(String nome, String cpf, int idade) {
        Paciente p = new Paciente();
        p.setNome(PREFIXO + nome);
        p.setCpf(cpf);
        p.setDataNascimento(hoje.minusYears(idade).minusDays(10));
        p.setSexo(Sexo.MASCULINO);
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

    /** O acidente grave de ontem, com o empregador da construção e o pedreiro (CBO 7152-10). */
    private String acidente(Paciente p, boolean obito) throws Exception {
        String agravo = agravoRepository.findByCodigo("ACIDENTE_TRABALHO_GRAVE").orElseThrow().getUuid().toString();
        return JsonPath.read(enviar(post(API + "notificacao"), "{\"pacienteId\": \"" + p.getUuid() + "\", \"agravoId\": \"" + agravo + "\", \"unidadeId\": \""
                + ubs.getUuid() + "\", \"origem\": \"ATENDIMENTO\", \"dataPrimeirosSintomas\": \"" + hoje.minusDays(1) + "\", \"trabalho\": {\"ocupacaoCbo\": "
                + "\"715210\", \"ocupacaoDescricao\": \"Pedreiro\", \"situacaoTrabalho\": \"EMPREGADO_REGISTRADO\", \"empregadorNome\": \"" + PREFIXO
                + "Construtora\", \"empregadorCnae\": \"4120400\", \"tipoAcidente\": \"TIPICO\", \"obito\": " + obito + "}}", 201), "$.uuid");
    }

    @Test
    void cicloDaInvestigacao() throws Exception {
        Paciente p = paciente("Pedreiro", "90088800001", 40);
        String n = acidente(p, false);
        String inv = API + "notificacao/" + n + "/trabalho/investigacao";

        // Sem a investigação, o nexo e a medida são 422; a dengue sem bloco, 422; a visita no futuro, 400.
        enviar(post(inv + "/nexo"), "{\"nexo\": \"CONFIRMADO\", \"justificativa\": \"Queda de andaime.\"}", 422);
        String dengue = agravoRepository.findByCodigo("DENGUE").orElseThrow().getUuid().toString();
        String nd = JsonPath.read(enviar(post(API + "notificacao"), "{\"pacienteId\": \"" + p.getUuid() + "\", \"agravoId\": \"" + dengue + "\", \"unidadeId\": \""
                + ubs.getUuid() + "\", \"origem\": \"ATENDIMENTO\", \"respostas\": {\"febre\": true}}", 201), "$.uuid");
        enviar(put(API + "notificacao/" + nd + "/trabalho/investigacao"), "{\"visitaEm\": \"" + hoje + "\"}", 422);
        enviar(put(inv), "{\"visitaEm\": \"" + hoje.plusDays(1) + "\"}", 400);

        String r = enviar(put(inv), "{\"visitaEm\": \"" + hoje + "\", \"localVisitado\": \"Obra da Rua A\", \"achados\": \"Andaime sem guarda-corpo.\"}", 200);
        assertThat((String) JsonPath.read(r, "$.situacao")).isEqualTo("EM_INVESTIGACAO");
        assertThat((String) JsonPath.read(r, "$.trabalho.investigacao.nexo")).isEqualTo("EM_ANALISE");

        enviar(post(inv + "/nexo"), "{\"nexo\": \"EM_ANALISE\", \"justificativa\": \"x\"}", 400);
        r = enviar(post(inv + "/nexo"), "{\"nexo\": \"CONFIRMADO\", \"justificativa\": \"Queda de andaime sem proteção.\"}", 200);
        assertThat((String) JsonPath.read(r, "$.trabalho.investigacao.nexo")).isEqualTo("CONFIRMADO");

        // A medida: prazo no passado, 400; verificada não cumprida pede a observação; já verificada, 422.
        enviar(post(inv + "/medida"), "{\"tipo\": \"PROTECAO_COLETIVA\", \"descricao\": \"Guarda-corpo\", \"prazo\": \"" + hoje.minusDays(1) + "\"}", 400);
        r = enviar(post(inv + "/medida"), "{\"tipo\": \"PROTECAO_COLETIVA\", \"descricao\": \"Instalar guarda-corpo no andaime.\", \"prazo\": \""
                + hoje.plusDays(15) + "\"}", 201);
        String medida = JsonPath.read(r, "$.trabalho.investigacao.medidas[0].uuid");
        assertThat((String) JsonPath.read(r, "$.trabalho.investigacao.medidas[0].situacao")).isEqualTo("RECOMENDADA");
        enviar(post(API + "medida-trabalho/" + medida + "/verificacao"), "{\"situacao\": \"NAO_CUMPRIDA\"}", 400);
        r = enviar(post(API + "medida-trabalho/" + medida + "/verificacao"), "{\"situacao\": \"CUMPRIDA\"}", 200);
        assertThat((String) JsonPath.read(r, "$.trabalho.investigacao.medidas[0].situacao")).isEqualTo("CUMPRIDA");
        enviar(post(API + "medida-trabalho/" + medida + "/verificacao"), "{\"situacao\": \"CUMPRIDA\"}", 422);

        // A vigilância sanitária: a denúncia do canal Vigilância contra o empregador, sem o nome do trabalhador; a segunda, 409.
        r = enviar(post(inv + "/comunicacao-visa"), "{\"descricao\": \"Verificar a proteção contra quedas na obra.\"}", 201);
        String protocolo = JsonPath.read(r, "$.trabalho.investigacao.denunciaProtocolo");
        Map<String, Object> d = jdbc.queryForMap("select canal, descricao, estabelecimento_descricao, denunciante_nome from tb_denuncia_sanitaria where protocolo = ?",
                protocolo);
        assertThat(d.get("canal")).isEqualTo("VIGILANCIA");
        assertThat((String) d.get("estabelecimento_descricao")).isEqualTo(PREFIXO + "Construtora");
        assertThat((String) d.get("descricao")).startsWith("Investigação de Acidente de trabalho grave").doesNotContain("Pedreiro");
        enviar(post(inv + "/comunicacao-visa"), "{\"descricao\": \"De novo.\"}", 409);

        // O encaminhamento à Superintendência do Trabalho.
        String sret = ComunicacaoViolenciaControllerTest.instituicao(jdbc, PREFIXO, "Superintendência Regional do Trabalho", "TRABALHO_E_RENDA",
                "SERVICO_DE_EMPREGO");
        r = enviar(post(inv + "/encaminhamento"), "{\"instituicaoId\": \"" + sret + "\", \"motivo\": \"Fiscalização da obra.\"}", 201);
        assertThat((String) JsonPath.read(r, "$.trabalho.investigacao.encaminhamentos[0].instituicaoNome")).isEqualTo(PREFIXO + "Superintendência Regional do Trabalho");
        assertThat((String) JsonPath.read(r, "$.trabalho.investigacao.encaminhamentos[0].retornoEsperadoEm")).isEqualTo(hoje.plusDays(30).toString());
        enviar(post(inv + "/encaminhamento"), "{\"instituicaoId\": \"" + sret + "\", \"motivo\": \"De novo.\"}", 409);

        // O risco pela investigação herda o empregador; sem empregador e sem notificação, 400; controlado uma vez.
        r = enviar(post(API + "saude-trabalhador/risco"), "{\"tipo\": \"ACIDENTE\", \"agente\": \"Trabalho em altura\", \"trabalhadoresExpostos\": 12, "
                + "\"notificacaoId\": \"" + n + "\"}", 201);
        String risco = JsonPath.read(r, "$.uuid");
        assertThat((String) JsonPath.read(r, "$.empregadorNome")).isEqualTo(PREFIXO + "Construtora");
        enviar(post(API + "saude-trabalhador/risco"), "{\"tipo\": \"QUIMICO\", \"agente\": \"Sílica\"}", 400);
        assertThat((List<String>) JsonPath.read(ler(API + "notificacao/" + n), "$.trabalho.investigacao.riscos[*].agente")).containsExactly("Trabalho em altura");
        assertThat((List<String>) JsonPath.read(ler(API + "saude-trabalhador/riscos?situacao=ATIVO"), "$[*].uuid")).contains(risco);
        r = enviar(post(API + "risco-ocupacional/" + risco + "/controle"), "{\"motivo\": \"Guarda-corpo instalado.\"}", 200);
        assertThat((String) JsonPath.read(r, "$.situacao")).isEqualTo("CONTROLADO");
        enviar(post(API + "risco-ocupacional/" + risco + "/controle"), "{\"motivo\": \"De novo.\"}", 422);
    }

    @Test
    void painelSemanaERelatorio() throws Exception {
        String confirmado = acidente(paciente("Servente", "90088800002", 17), true);
        String descartado = acidente(paciente("Pintor", "90088800003", 35), false);
        for (String n : List.of(confirmado, descartado)) {
            enviar(put(API + "notificacao/" + n + "/trabalho/investigacao"), "{\"visitaEm\": \"" + hoje + "\"}", 200);
        }
        enviar(post(API + "notificacao/" + confirmado + "/trabalho/investigacao/nexo"), "{\"nexo\": \"CONFIRMADO\", \"justificativa\": \"Queda.\"}", 200);
        enviar(post(API + "notificacao/" + descartado + "/trabalho/investigacao/nexo"), "{\"nexo\": \"DESCARTADO\", \"justificativa\": \"Queda em casa.\"}", 200);
        enviar(post(API + "notificacao/" + confirmado + "/trabalho/investigacao/medida"), "{\"tipo\": \"CAPACITACAO\", \"descricao\": \"Treinar a equipe.\", "
                + "\"prazo\": \"" + hoje + "\"}", 201);
        jdbc.update("update tb_medida_trabalho set prazo = ? where investigacao_id in (select uuid from tb_investigacao_trabalho where notificacao_id = ?::uuid)",
                hoje.minusDays(1), confirmado);

        String painel = ler(API + "saude-trabalhador/painel?unidadeId=" + ubs.getUuid());
        assertThat((List<String>) JsonPath.read(painel, "$.medidasVencidas[*].notificacaoId")).containsExactly(confirmado);

        String semana = ler(API + "saude-trabalhador/semana");
        assertThat((List<String>) JsonPath.read(semana, "$.alertas[?(@.unidadeNome == '" + PREFIXO + "UBS')].empregadorNome")).hasSize(2);
        assertThat(semana).doesNotContain(PREFIXO + "Servente");
        assertThat((Integer) JsonPath.read(semana, "$.medidasVencidas")).isGreaterThanOrEqualTo(1);

        // O relatório da unidade: só a de nexo confirmado conta; óbito e menor de 18 anos; construção e grande grupo 7.
        String rel = ler("/api/v1/relatorios/VIGILANCIA?de=" + hoje.minusDays(7) + "&ate=" + hoje + "&unidadeId=" + ubs.getUuid());
        assertThat((List<String>) JsonPath.read(rel, "$.resumo[?(@.rotulo == 'Relacionadas ao trabalho')].valor")).containsExactly("1");
        assertThat((List<String>) JsonPath.read(rel, "$.resumo[?(@.rotulo == 'Nexo do trabalho investigado (%)')].valor")).containsExactly("100,0");
        assertThat((List<String>) JsonPath.read(rel, "$.resumo[?(@.rotulo == 'Acidentes de trabalho fatais')].valor")).containsExactly("1");
        assertThat((List<String>) JsonPath.read(rel, "$.resumo[?(@.rotulo == 'Trabalho com menores de 18 anos')].valor")).containsExactly("1");
        assertThat((List<List<String>>) JsonPath.read(rel, "$.secoes[?(@.titulo == 'Saúde do trabalhador por seção do CNAE')].linhas[*]"))
                .containsExactly(List.of("F · Construção", "1"));
        assertThat((List<List<String>>) JsonPath.read(rel, "$.secoes[?(@.titulo == 'Saúde do trabalhador por grande grupo da CBO')].linhas[*]"))
                .containsExactly(List.of("7 · Trabalhadores da produção de bens e serviços industriais", "1"));
    }
}
