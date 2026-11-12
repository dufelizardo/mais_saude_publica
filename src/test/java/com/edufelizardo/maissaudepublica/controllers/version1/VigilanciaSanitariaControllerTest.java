package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.models.Profissional;
import com.edufelizardo.maissaudepublica.repositories.ProfissionalRepository;
import com.edufelizardo.maissaudepublica.repositories.TipoDocumentoRepository;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Vigilância Sanitária, fatia V3a (ADR-0199): o estabelecimento com as atividades e o risco, a licença e a dispensa, a denúncia, o
 * roteiro versionado, a inspeção com as irregularidades e o retorno, as pendências, o termo em PDF e os documentos (D2 parcial).
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class VigilanciaSanitariaControllerTest {

    static final String PREFIXO = "Visa Teste ";
    private static final String API = "/api/v1/vigilancia-sanitaria/";
    private static final byte[] PDF = "%PDF-1.4 foto".getBytes(StandardCharsets.US_ASCII);
    private static final AtomicLong SEQUENCIA = new AtomicLong(System.nanoTime() % 1_000_000_000L);

    @Autowired private MockMvc mockMvc;
    @Autowired private JdbcTemplate jdbc;
    @Autowired private ProfissionalRepository profissionalRepository;
    @Autowired private TipoDocumentoRepository tipoRepository;

    private final LocalDate hoje = LocalDate.now();

    @BeforeEach
    void seed() {
        limpar();
        Profissional p = new Profissional();
        p.setMatricula("VISA-FISC");
        p.setCpf("90177700001");
        p.setNome(PREFIXO + "Fiscal");
        p.setAtivo(true);
        profissionalRepository.save(p);
    }

    @AfterEach
    void limpar() {
        limparDados(jdbc, PREFIXO, "VISA-%");
    }

    /** Apaga o que os testes da VISA criam: estabelecimentos pelo prefixo, os roteiros TSTVISA e os profissionais pela matrícula. */
    static void limparDados(JdbcTemplate jdbc, String prefixo, String matriculas) {
        String estabs = "(select uuid from tb_estabelecimento_regulado where razao_social like '" + prefixo + "%')";
        String inspecoes = "(select uuid from tb_inspecao_sanitaria where estabelecimento_id in " + estabs + ")";
        String processos = "(select uuid from tb_processo_sanitario where estabelecimento_id in " + estabs + ")";
        String docs = "(select uuid from tb_documento where dono_id in " + estabs + " or dono_id in " + inspecoes + " or dono_id in " + processos + ")";
        jdbc.update("delete from tb_termo_sanitario where estabelecimento_id in " + estabs);
        jdbc.update("delete from tb_versao_documento where documento_id in " + docs);
        jdbc.update("delete from tb_documento where uuid in " + docs);
        jdbc.update("delete from tb_medida_sanitaria where estabelecimento_id in " + estabs);
        jdbc.update("delete from tb_penalidade_sanitaria where processo_id in " + processos);
        jdbc.update("delete from tb_processo_sanitario_fiscal where processo_id in " + processos);
        jdbc.update("update tb_irregularidade_sanitaria set processo_id = null where estabelecimento_id in " + estabs);
        jdbc.update("delete from tb_processo_sanitario where estabelecimento_id in " + estabs);
        jdbc.update("delete from tb_versao_documento where documento_id in " + docs);
        jdbc.update("delete from tb_documento where uuid in " + docs);
        jdbc.update("delete from tb_licenca_sanitaria where estabelecimento_id in " + estabs);
        jdbc.update("delete from tb_irregularidade_sanitaria where estabelecimento_id in " + estabs);
        jdbc.update("delete from tb_inspecao_sanitaria_fiscal where inspecao_id in " + inspecoes);
        jdbc.update("update tb_inspecao_sanitaria set inspecao_origem_id = null where uuid in " + inspecoes);
        jdbc.update("delete from tb_inspecao_sanitaria where estabelecimento_id in " + estabs);
        jdbc.update("delete from tb_denuncia_sanitaria where descricao like ?", prefixo + "%");
        jdbc.update("delete from tb_atividade_estabelecimento where estabelecimento_id in " + estabs);
        jdbc.update("delete from tb_estabelecimento_regulado where razao_social like ?", prefixo + "%");
        jdbc.update("delete from tb_versao_roteiro_inspecao where roteiro_id in (select uuid from tb_roteiro_inspecao where codigo like 'TSTVISA%')");
        jdbc.update("delete from tb_roteiro_inspecao where codigo like 'TSTVISA%'");
        jdbc.update("delete from tb_profissional where matricula like ?", matriculas);
    }

    /** Um CNPJ válido e diferente a cada chamada. */
    static String cnpj() {
        String base = String.format("%08d0001", SEQUENCIA.incrementAndGet() % 100_000_000L);
        int[] pesos = {6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
        StringBuilder d = new StringBuilder(base);
        for (int n = 12; n <= 13; n++) {
            int soma = 0;
            for (int i = 0; i < n; i++) {
                soma += (d.charAt(i) - '0') * pesos[i + 13 - n];
            }
            int resto = soma % 11;
            d.append(resto < 2 ? 0 : 11 - resto);
        }
        return d.toString();
    }

    private String enviar(MockHttpServletRequestBuilder r, String corpo, int esperado) throws Exception {
        var resposta = mockMvc.perform(r.contentType(MediaType.APPLICATION_JSON).content(corpo)).andReturn().getResponse();
        String texto = resposta.getContentAsString(StandardCharsets.UTF_8);
        assertThat(resposta.getStatus()).as(texto).isEqualTo(esperado);
        return texto;
    }

    private String ler(String url) throws Exception {
        return mockMvc.perform(get(url)).andExpect(status().isOk()).andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
    }

    static String corpoEstabelecimento(String documento, String nome, String... atividades) {
        String lista = java.util.Arrays.stream(atividades).map(a -> {
            String[] p = a.split(":");
            return "{\"cnae\": \"" + p[0] + "\", \"descricao\": \"Atividade " + p[0] + "\", \"risco\": \"" + p[1] + "\"}";
        }).collect(Collectors.joining(", "));
        return "{\"documento\": \"" + documento + "\", \"razaoSocial\": \"" + PREFIXO + nome + "\", \"endereco\": \"Rua do Comércio, 10\", "
                + "\"atividades\": [" + lista + "]}";
    }

    private String estabelecimento(String nome, String... atividades) throws Exception {
        return JsonPath.read(enviar(post(API + "estabelecimento"), corpoEstabelecimento(cnpj(), nome, atividades), 201), "$.uuid");
    }

    private String roteiroAlimentos() throws Exception {
        List<String> ids = JsonPath.read(ler(API + "roteiros"), "$[?(@.codigo == 'ALIMENTOS_BOAS_PRATICAS')].versaoId");
        return ids.get(0);
    }

    /** As respostas do roteiro de alimentos: tudo conforme, menos os itens informados. */
    private static String respostas(String... naoConformes) {
        List<String> nc = List.of(naoConformes);
        return "{" + List.of("A1", "A2", "A3", "A4", "A5", "A6", "A7", "A8").stream()
                .map(c -> "\"" + c + "\": \"" + (nc.contains(c) ? "NAO_CONFORME" : "CONFORME") + "\"").collect(Collectors.joining(", ")) + "}";
    }

    @Test
    void estabelecimentoLicencaEDispensa() throws Exception {
        String url = API + "estabelecimento";
        enviar(post(url), corpoEstabelecimento("11222333000180", "X", "5611201:ALTO"), 400);
        enviar(post(url), corpoEstabelecimento(cnpj(), "X", "5611201:ALTO", "5611-2/01:BAIXO"), 400);
        String doc = cnpj();
        String r = enviar(post(url), corpoEstabelecimento(doc, "Restaurante", "4721102:BAIXO", "5611-2/01:ALTO"), 201);
        assertThat((String) JsonPath.read(r, "$.risco")).isEqualTo("ALTO");
        assertThat((List<String>) JsonPath.read(r, "$.atividades[*].cnae")).containsExactly("4721102", "5611201");
        String restaurante = JsonPath.read(r, "$.uuid");
        enviar(post(url), corpoEstabelecimento(doc, "Outro", "5611201:ALTO"), 409);

        // Risco III sem inspeção satisfatória.
        String licenca = API + "estabelecimento/" + restaurante + "/licenca";
        enviar(post(licenca), "{}", 422);
        String inspecao = enviar(post(API + "inspecao"), "{\"estabelecimentoId\": \"" + restaurante + "\", \"motivo\": \"LICENCIAMENTO\", \"roteiroVersaoId\": \""
                + roteiroAlimentos() + "\", \"respostas\": " + respostas() + ", \"resultado\": \"SATISFATORIA\", \"relatorio\": \"Tudo em ordem.\", "
                + "\"fiscais\": [\"VISA-FISC\"]}", 201);
        // Sem login não há assinatura, mas o conteúdo já tem o hash.
        assertThat((Boolean) JsonPath.read(inspecao, "$.assinada")).isFalse();
        assertThat((String) JsonPath.read(inspecao, "$.hashConteudo")).hasSize(64);
        assertThat((String) JsonPath.read(inspecao, "$.fiscais[0].nome")).isEqualTo(PREFIXO + "Fiscal");
        r = enviar(post(licenca), "{\"observacao\": \"Primeira licença.\"}", 201);
        assertThat((String) JsonPath.read(r, "$.numero")).matches(hoje.getYear() + "/\\d{6}");
        assertThat((String) JsonPath.read(r, "$.validade")).isEqualTo(hoje.plusYears(1).toString());
        assertThat((String) JsonPath.read(r, "$.situacao")).isEqualTo("VIGENTE");
        assertThat((String) JsonPath.read(r, "$.inspecaoId")).isEqualTo(JsonPath.read(inspecao, "$.uuid"));
        enviar(post(licenca), "{}", 409);

        // Risco I: a dispensa, sem validade e sem repetir.
        String banca = estabelecimento("Banca", "4729699:BAIXO");
        String dispensa = API + "estabelecimento/" + banca + "/licenca";
        enviar(post(dispensa), "{\"validade\": \"" + hoje.plusYears(1) + "\"}", 400);
        r = enviar(post(dispensa), "{}", 201);
        assertThat((Boolean) JsonPath.read(r, "$.dispensa")).isTrue();
        assertThat((String) JsonPath.read(r, "$.situacao")).isEqualTo("DISPENSADA");
        enviar(post(dispensa), "{}", 409);

        // Risco II: sem licença e com a licença vencida, nas pendências.
        String padaria = estabelecimento("Padaria", "1091102:MEDIO");
        String mercado = estabelecimento("Mercado", "4711302:MEDIO");
        r = enviar(post(API + "estabelecimento/" + mercado + "/licenca"), "{\"emitidaEm\": \"" + hoje.minusMonths(13) + "\", \"validade\": \""
                + hoje.minusMonths(1) + "\"}", 201);
        assertThat((String) JsonPath.read(r, "$.situacao")).isEqualTo("VENCIDA");
        String p = ler(API + "pendencias");
        assertThat((List<String>) JsonPath.read(p, "$.licencas[?(@.estabelecimentoId == '" + padaria + "')].motivo")).containsExactly("SEM_LICENCA");
        assertThat((List<String>) JsonPath.read(p, "$.licencas[?(@.estabelecimentoId == '" + mercado + "')].motivo")).containsExactly("VENCIDA");
        assertThat((List<String>) JsonPath.read(p, "$.licencas[?(@.estabelecimentoId == '" + restaurante + "')]")).isEmpty();
        assertThat((List<String>) JsonPath.read(p, "$.licencas[?(@.estabelecimentoId == '" + banca + "')]")).isEmpty();

        // A alteração mantém o CNAE que fica e troca o risco; inativo não recebe licença.
        r = enviar(put(url + "/" + mercado), corpoEstabelecimento(cnpj(), "Mercado", "4711302:BAIXO").replace("}]}", "}], \"ativo\": false}"), 200);
        assertThat((String) JsonPath.read(r, "$.risco")).isEqualTo("BAIXO");
        assertThat((Boolean) JsonPath.read(r, "$.ativo")).isFalse();
        enviar(post(API + "estabelecimento/" + mercado + "/licenca"), "{}", 422);
        assertThat((List<String>) JsonPath.read(ler(API + "estabelecimentos?busca=padaria"), "$[*].uuid")).containsExactly(padaria);
        assertThat((Integer) JsonPath.read(ler(API + "estabelecimento/" + restaurante), "$.licencas.length()")).isEqualTo(1);
    }

    @Test
    void denunciaInspecaoERetorno() throws Exception {
        String loja = estabelecimento("Lanchonete", "5611203:MEDIO");
        String url = API + "denuncia";
        enviar(post(url), "{\"canal\": \"TELEFONE\", \"descricao\": \"" + PREFIXO + "Baratas\", \"anonima\": true}", 400);
        enviar(post(url), "{\"canal\": \"TELEFONE\", \"descricao\": \"" + PREFIXO + "Baratas\", \"estabelecimentoDescricao\": \"Lanchonete da esquina\", "
                + "\"anonima\": true, \"denuncianteNome\": \"Maria\"}", 400);
        enviar(post(url), "{\"canal\": \"TELEFONE\", \"descricao\": \"" + PREFIXO + "Baratas\", \"estabelecimentoDescricao\": \"Lanchonete da esquina\"}", 400);
        String r = enviar(post(url), "{\"canal\": \"OUVIDORIA\", \"descricao\": \"" + PREFIXO + "Baratas na cozinha\", \"estabelecimentoDescricao\": "
                + "\"Lanchonete da esquina\", \"denuncianteNome\": \"Maria\", \"denuncianteContato\": \"(19) 99999-0000\"}", 201);
        String denuncia = JsonPath.read(r, "$.uuid");
        assertThat((String) JsonPath.read(r, "$.protocolo")).matches(hoje.getYear() + "/\\d{6}");
        assertThat((String) JsonPath.read(r, "$.situacao")).isEqualTo("RECEBIDA");
        enviar(post(url + "/" + denuncia + "/conclusao"), "{\"situacao\": \"PROCEDENTE\", \"conclusao\": \"x\"}", 422);
        enviar(post(url + "/" + denuncia + "/conclusao"), "{\"situacao\": \"EM_APURACAO\", \"conclusao\": \"x\"}", 400);

        // As regras da inspeção.
        String rot = roteiroAlimentos();
        String base = "\"estabelecimentoId\": \"" + loja + "\", \"relatorio\": \"Cozinha com pragas.\", \"fiscais\": [\"VISA-FISC\"], ";
        String insp = API + "inspecao";
        enviar(post(insp), "{" + base + "\"motivo\": \"DENUNCIA\", \"resultado\": \"COM_RESSALVAS\"}", 400);
        enviar(post(insp), "{" + base + "\"motivo\": \"PROGRAMADA\", \"denunciaId\": \"" + denuncia + "\", \"resultado\": \"COM_RESSALVAS\"}", 400);
        enviar(post(insp), "{" + base + "\"motivo\": \"PROGRAMADA\", \"resultado\": \"COM_RESSALVAS\", \"respostas\": {\"A1\": \"CONFORME\"}}", 400);
        enviar(post(insp), "{" + base + "\"motivo\": \"PROGRAMADA\", \"resultado\": \"COM_RESSALVAS\", \"roteiroVersaoId\": \"" + rot
                + "\", \"respostas\": {\"A1\": \"CONFORME\"}}", 400);
        enviar(post(insp), "{" + base + "\"motivo\": \"PROGRAMADA\", \"resultado\": \"INSATISFATORIA\", \"roteiroVersaoId\": \"" + rot
                + "\", \"respostas\": " + respostas("A3") + "}", 400);
        enviar(post(insp), "{" + base + "\"motivo\": \"PROGRAMADA\", \"resultado\": \"SATISFATORIA\", \"irregularidades\": [{\"descricao\": \"x\", "
                + "\"gravidade\": \"LEVE\"}]}", 400);
        enviar(post(insp), "{" + base + "\"motivo\": \"PROGRAMADA\", \"resultado\": \"COM_RESSALVAS\", \"irregularidades\": [{\"descricao\": \"x\", "
                + "\"gravidade\": \"LEVE\", \"prazoCorrecao\": \"" + hoje.minusDays(1) + "\"}]}", 400);
        enviar(post(insp), "{\"estabelecimentoId\": \"" + loja + "\", \"relatorio\": \"x\", \"fiscais\": [\"VISA-NINGUEM\"], \"motivo\": \"PROGRAMADA\", "
                + "\"resultado\": \"SATISFATORIA\"}", 404);

        // A inspeção da denúncia: o item A3 não conforme e uma irregularidade fora do roteiro, já com o prazo vencido.
        r = enviar(post(insp), "{" + base + "\"motivo\": \"DENUNCIA\", \"denunciaId\": \"" + denuncia + "\", \"dataInspecao\": \"" + hoje.minusDays(20)
                + "\", \"resultado\": \"INSATISFATORIA\", \"roteiroVersaoId\": \"" + rot + "\", \"respostas\": " + respostas("A3") + ", \"irregularidades\": ["
                + "{\"itemCodigo\": \"a3\", \"descricao\": \"Baratas na área de preparo\", \"enquadramento\": \"RDC 216/2004, item 4.3\", \"gravidade\": \"GRAVISSIMA\", "
                + "\"prazoCorrecao\": \"" + hoje.plusDays(10) + "\"}, {\"descricao\": \"Lixeira sem tampa\", \"gravidade\": \"LEVE\", \"prazoCorrecao\": \""
                + hoje.minusDays(5) + "\"}]}", 201);
        String primeira = JsonPath.read(r, "$.uuid");
        assertThat((String) JsonPath.read(r, "$.denunciaProtocolo")).matches("\\d{4}/\\d{6}");
        assertThat((String) JsonPath.read(r, "$.respostas.A3")).isEqualTo("NAO_CONFORME");
        List<String> irregs = JsonPath.read(r, "$.irregularidades[*].uuid");
        assertThat(irregs).hasSize(2);
        assertThat((List<String>) JsonPath.read(r, "$.irregularidades[*].itemCodigo")).contains("A3");
        r = ler(API + "denuncias");
        assertThat((List<String>) JsonPath.read(r, "$[?(@.uuid == '" + denuncia + "')].situacao")).containsExactly("EM_APURACAO");
        assertThat((List<String>) JsonPath.read(r, "$[?(@.uuid == '" + denuncia + "')].estabelecimentoId")).containsExactly(loja);
        assertThat((List<String>) JsonPath.read(r, "$[?(@.uuid == '" + denuncia + "')].denuncianteNome")).containsExactly("Maria");
        assertThat((List<String>) JsonPath.read(ler(API + "pendencias"), "$.prazosVencidos[*].descricao")).contains("Lixeira sem tampa");
        assertThat((Integer) JsonPath.read(ler(API + "estabelecimentos?busca=lanchonete"), "$[0].irregularidadesAbertas")).isEqualTo(2);

        // Gravíssima aberta impede a licença; a denúncia procedente fecha uma vez só.
        enviar(post(API + "estabelecimento/" + loja + "/licenca"), "{}", 422);
        r = enviar(post(url + "/" + denuncia + "/conclusao"), "{\"situacao\": \"PROCEDENTE\", \"conclusao\": \"Constatado na inspeção.\"}", 200);
        assertThat((Integer) JsonPath.read(r, "$.inspecoes")).isEqualTo(1);
        enviar(post(url + "/" + denuncia + "/conclusao"), "{\"situacao\": \"IMPROCEDENTE\", \"conclusao\": \"x\"}", 422);

        // O retorno confirma as correções.
        enviar(post(insp), "{" + base + "\"motivo\": \"RETORNO\", \"resultado\": \"SATISFATORIA\"}", 400);
        enviar(post(insp), "{" + base + "\"motivo\": \"PROGRAMADA\", \"resultado\": \"SATISFATORIA\", \"corrigidas\": [\"" + irregs.get(0) + "\"]}", 400);
        r = enviar(post(insp), "{" + base + "\"motivo\": \"RETORNO\", \"inspecaoOrigemId\": \"" + primeira + "\", \"resultado\": \"SATISFATORIA\", "
                + "\"corrigidas\": [\"" + irregs.get(0) + "\", \"" + irregs.get(1) + "\"]}", 201);
        assertThat((List<String>) JsonPath.read(r, "$.corrigidas")).containsExactlyInAnyOrderElementsOf(irregs);
        String detalhe = ler(API + "estabelecimento/" + loja);
        assertThat((List<String>) JsonPath.read(detalhe, "$.irregularidades[*].situacao")).containsOnly("CORRIGIDA");
        assertThat((Integer) JsonPath.read(detalhe, "$.estabelecimento.irregularidadesAbertas")).isEqualTo(0);
        assertThat((Integer) JsonPath.read(detalhe, "$.inspecoes.length()")).isEqualTo(2);
        enviar(post(insp), "{" + base + "\"motivo\": \"RETORNO\", \"inspecaoOrigemId\": \"" + primeira + "\", \"resultado\": \"SATISFATORIA\", "
                + "\"corrigidas\": [\"" + irregs.get(0) + "\"]}", 422);
        enviar(post(API + "estabelecimento/" + loja + "/licenca"), "{}", 201);

        // O termo em PDF.
        byte[] pdf = mockMvc.perform(get(API + "inspecao/" + primeira + "/termo.pdf")).andExpect(status().isOk()).andReturn().getResponse().getContentAsByteArray();
        assertThat(new String(pdf, 0, 5, StandardCharsets.US_ASCII)).isEqualTo("%PDF-");
    }

    @Test
    void documentosDaVisa() throws Exception {
        String loja = estabelecimento("Farmácia", "4771701:MEDIO");
        String tipo = tipoRepository.findByCodigo("OUTRO").orElseThrow().getUuid().toString();
        MockMultipartFile arquivo = new MockMultipartFile("arquivo", "planta.pdf", "application/pdf", PDF);
        String r = mockMvc.perform(multipart(API + "estabelecimento/" + loja + "/documento").file(arquivo).param("tipoId", tipo).param("titulo", "Planta baixa"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        assertThat((List<String>) JsonPath.read(r, "$[*].titulo")).containsExactly("Planta baixa");
        String documento = JsonPath.read(r, "$[0].uuid");
        String versao = JsonPath.read(r, "$[0].versoes[0].uuid");
        mockMvc.perform(get(API + "documento/versao/" + versao + "/arquivo")).andExpect(status().isOk());
        // O documento da VISA não abre pelo caminho do paciente, e vice-versa.
        mockMvc.perform(get("/api/v1/documento/versao/" + versao + "/arquivo")).andExpect(status().isNotFound());
        mockMvc.perform(post("/api/v1/documento/" + documento + "/retirada").contentType(MediaType.APPLICATION_JSON).content("{\"motivo\": \"x\"}"))
                .andExpect(status().isNotFound());
        mockMvc.perform(multipart(API + "estabelecimento/" + loja + "/documento").file(new MockMultipartFile("arquivo", "a.txt", "text/plain", new byte[]{1, 2}))
                .param("tipoId", tipo).param("titulo", "x")).andExpect(status().isBadRequest());
        assertThat((Integer) JsonPath.read(ler(API + "estabelecimento/" + loja), "$.documentos.length()")).isEqualTo(1);
        mockMvc.perform(post(API + "documento/" + documento + "/retirada").contentType(MediaType.APPLICATION_JSON).content("{\"motivo\": \"Enviado errado.\"}"))
                .andExpect(status().isNoContent());
        assertThat((Integer) JsonPath.read(ler(API + "estabelecimento/" + loja + "/documentos"), "$.length()")).isEqualTo(0);
        mockMvc.perform(get(API + "documento/versao/" + versao + "/arquivo")).andExpect(status().isUnprocessableEntity());
    }

    @Test
    void roteirosVersionados() throws Exception {
        String url = API + "roteiro";
        String itens = "\"itens\": [{\"codigo\": \"r1\", \"texto\": \"Licença à vista\", \"gravidade\": \"LEVE\"}]";
        enviar(post(url), "{" + itens + "}", 400);
        enviar(post(url), "{\"codigo\": \"TSTVISA_X\", \"nome\": \"Teste\", \"itens\": [{\"codigo\": \"R1\", \"texto\": \"a\", \"gravidade\": \"LEVE\"}, "
                + "{\"codigo\": \"r1\", \"texto\": \"b\", \"gravidade\": \"LEVE\"}]}", 400);
        String r = enviar(post(url), "{\"codigo\": \"tstvisa_salao\", \"nome\": \"Salões de beleza\", " + itens + "}", 201);
        assertThat((String) JsonPath.read(r, "$.codigo")).isEqualTo("TSTVISA_SALAO");
        assertThat((Integer) JsonPath.read(r, "$.versao")).isEqualTo(1);
        assertThat((String) JsonPath.read(r, "$.itens[0].codigo")).isEqualTo("R1");
        String id = JsonPath.read(r, "$.uuid");
        String v1 = JsonPath.read(r, "$.versaoId");
        enviar(post(url), "{\"codigo\": \"TSTVISA_SALAO\", \"nome\": \"x\", " + itens + "}", 409);
        r = enviar(post(url + "/" + id + "/versao"), "{\"itens\": [{\"codigo\": \"R1\", \"texto\": \"Licença à vista\", \"gravidade\": \"LEVE\"}, "
                + "{\"codigo\": \"R2\", \"texto\": \"Esterilização dos instrumentos\", \"gravidade\": \"GRAVE\"}]}", 201);
        assertThat((Integer) JsonPath.read(r, "$.versao")).isEqualTo(2);
        assertThat((String) JsonPath.read(r, "$.versaoId")).isNotEqualTo(v1);
        assertThat((Integer) JsonPath.read(r, "$.itens.length()")).isEqualTo(2);
        assertThat((List<Integer>) JsonPath.read(ler(API + "roteiros"), "$[?(@.codigo == 'FARMACIA_DROGARIA')].versao")).containsExactly(1);
    }
}
