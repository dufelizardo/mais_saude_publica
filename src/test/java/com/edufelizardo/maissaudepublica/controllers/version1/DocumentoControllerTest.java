package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.models.Paciente;
import com.edufelizardo.maissaudepublica.models.UnidadeDeSaude;
import com.edufelizardo.maissaudepublica.models.enuns.Sexo;
import com.edufelizardo.maissaudepublica.models.enuns.TipoUnidadeDeSaude;
import com.edufelizardo.maissaudepublica.repositories.PacienteRepository;
import com.edufelizardo.maissaudepublica.repositories.TipoDocumentoRepository;
import com.edufelizardo.maissaudepublica.repositories.UnidadeDeSaudeRepository;
import com.edufelizardo.maissaudepublica.services.version1.ArmazenamentoDeArquivos;
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
import org.springframework.test.web.servlet.ResultActions;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Documentos, fatia D1 (ADR-0172): anexar, versões, retirada, conferência pelo conteúdo, hash e catálogo. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class DocumentoControllerTest {

    private static final String PREFIXO = "Documento Teste ";
    private static final String CODIGO = "TESTE_D1_";
    private static final byte[] PDF = "%PDF-1.4 exame".getBytes(StandardCharsets.US_ASCII);
    private static final byte[] PNG = {(byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A, 1, 2};

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private UnidadeDeSaudeRepository unidadeDeSaudeRepository;

    @Autowired
    private PacienteRepository pacienteRepository;

    @Autowired
    private TipoDocumentoRepository tipoRepository;

    @Autowired
    private ArmazenamentoDeArquivos armazenamento;

    private UUID paciente;
    private UUID unidade;
    private UUID tipo;

    @BeforeEach
    void seed() {
        limpar();
        UnidadeDeSaude u = new UnidadeDeSaude();
        u.setNome(PREFIXO + "UBS");
        u.setTipo(TipoUnidadeDeSaude.UBS);
        u.setAtivo(true);
        unidade = unidadeDeSaudeRepository.save(u).getUuid();
        Paciente p = new Paciente();
        p.setNome(PREFIXO + "Ana");
        p.setCpf("93200000172");
        p.setDataNascimento(LocalDate.of(1980, 1, 1));
        p.setSexo(Sexo.FEMININO);
        p.setAtivo(true);
        paciente = pacienteRepository.save(p).getUuid();
        tipo = tipoRepository.findByCodigo("LAUDO").orElseThrow().getUuid();
    }

    @AfterEach
    void limpar() {
        String docs = "(select d.uuid from tb_documento d join tb_unidade_de_saude u on u.uuid = d.unidade_id where u.nome like ?)";
        jdbc.update("delete from tb_versao_documento where documento_id in " + docs, PREFIXO + "%");
        jdbc.update("delete from tb_documento where uuid in " + docs, PREFIXO + "%");
        jdbc.update("delete from tb_tipo_documento where codigo like ?", CODIGO + "%");
        jdbc.update("delete from tb_evento_auditoria where paciente_id in (select uuid from tb_paciente where nome like ?)", PREFIXO + "%");
        jdbc.update("delete from tb_paciente where nome like ?", PREFIXO + "%");
        jdbc.update("delete from tb_unidade_de_saude where nome like ?", PREFIXO + "%");
    }

    private MockMultipartFile arquivo(String nome, String declarado, byte[] conteudo) {
        return new MockMultipartFile("arquivo", nome, declarado, conteudo);
    }

    private ResultActions anexar(MockMultipartFile arquivo, String titulo, String data) throws Exception {
        var r = multipart("/api/v1/paciente/" + paciente + "/documento").file(arquivo)
                .param("tipoId", tipo.toString()).param("unidadeId", unidade.toString()).param("titulo", titulo);
        return mockMvc.perform(data == null ? r : r.param("dataDocumento", data));
    }

    private String idDoDocumento(ResultActions r) throws Exception {
        return JsonPath.read(r.andExpect(status().isOk()).andReturn().getResponse().getContentAsString(), "$.documentos[0].uuid");
    }

    @Test
    void anexaEAbreOArquivoComoAnexo() throws Exception {
        String corpo = anexar(arquivo("exame.pdf", "application/pdf", PDF), "Hemograma", null).andExpect(status().isOk())
                .andExpect(jsonPath("$.podeAbrir").value(true))
                .andExpect(jsonPath("$.documentos[0].titulo").value("Hemograma"))
                .andExpect(jsonPath("$.documentos[0].versoes[0].numero").value(1))
                .andExpect(jsonPath("$.documentos[0].versoes[0].tipoConteudo").value("application/pdf"))
                .andReturn().getResponse().getContentAsString();
        String versao = JsonPath.read(corpo, "$.documentos[0].versoes[0].uuid");

        mockMvc.perform(get("/api/v1/documento/versao/" + versao + "/arquivo"))
                .andExpect(status().isOk())
                .andExpect(content().bytes(PDF))
                .andExpect(header().string("Content-Disposition", containsString("attachment")))
                .andExpect(header().string("X-Content-Type-Options", "nosniff"));
    }

    @Test
    void tipoVemDoConteudoENaoDaExtensao() throws Exception {
        anexar(arquivo("foto.pdf", "application/pdf", PNG), "Foto", null).andExpect(status().isOk())
                .andExpect(jsonPath("$.documentos[0].versoes[0].tipoConteudo").value("image/png"));
        anexar(arquivo("pagina.pdf", "application/pdf", "<html><script>".getBytes()), "Falso", null).andExpect(status().isBadRequest());
        anexar(arquivo("vazio.pdf", "application/pdf", new byte[0]), "Vazio", null).andExpect(status().isBadRequest());
    }

    @Test
    void nomeDoArquivoPerdeOCaminho() throws Exception {
        anexar(arquivo("../../etc/passwd.pdf", "application/pdf", PDF), "Nome", null)
                .andExpect(jsonPath("$.documentos[0].versoes[0].nomeArquivo").value("passwd.pdf"));
    }

    @Test
    void validacoes() throws Exception {
        anexar(arquivo("a.pdf", "application/pdf", PDF), "   ", null).andExpect(status().isBadRequest());
        anexar(arquivo("a.pdf", "application/pdf", PDF), "Futuro", LocalDate.now().plusDays(1).toString()).andExpect(status().isBadRequest());
        byte[] grande = new byte[10 * 1024 * 1024 + 1];
        System.arraycopy(PDF, 0, grande, 0, PDF.length);
        anexar(arquivo("g.pdf", "application/pdf", grande), "Grande", null).andExpect(status().isBadRequest());
        mockMvc.perform(multipart("/api/v1/paciente/" + UUID.randomUUID() + "/documento").file(arquivo("a.pdf", "application/pdf", PDF))
                .param("tipoId", tipo.toString()).param("unidadeId", unidade.toString()).param("titulo", "X")).andExpect(status().isNotFound());
    }

    @Test
    void tipoInativoE422() throws Exception {
        String r = mockMvc.perform(post("/api/v1/tipo-documento").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"codigo\": \"%sinativo\", \"nome\": \"Inativo\", \"ativo\": false}".formatted(CODIGO)))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.codigo").value(CODIGO + "INATIVO"))
                .andReturn().getResponse().getContentAsString();
        tipo = UUID.fromString(JsonPath.read(r, "$.uuid"));
        anexar(arquivo("a.pdf", "application/pdf", PDF), "X", null).andExpect(status().isUnprocessableEntity());
    }

    @Test
    void catalogoTrazACargaERecusaCodigoRepetido() throws Exception {
        mockMvc.perform(get("/api/v1/tipo-documento")).andExpect(status().isOk())
                .andExpect(jsonPath("$[*].codigo", org.hamcrest.Matchers.hasItems("EXAME_EXTERNO", "RECEITA", "TERMO_CONSENTIMENTO")));
        String corpo = "{\"codigo\": \"LAUDO\", \"nome\": \"Outro laudo\"}";
        mockMvc.perform(post("/api/v1/tipo-documento").contentType(MediaType.APPLICATION_JSON).content(corpo)).andExpect(status().isConflict());
        String r = mockMvc.perform(post("/api/v1/tipo-documento").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"codigo\": \"%sA\", \"nome\": \"A\"}".formatted(CODIGO))).andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        mockMvc.perform(patch("/api/v1/tipo-documento/" + JsonPath.read(r, "$.uuid")).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"codigo\": \"%sA\", \"nome\": \"Novo nome\", \"ativo\": false}".formatted(CODIGO)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.nome").value("Novo nome"));
    }

    @Test
    void novaVersaoGuardaAAnterior() throws Exception {
        String id = idDoDocumento(anexar(arquivo("v1.pdf", "application/pdf", PDF), "Laudo", null));
        mockMvc.perform(multipart("/api/v1/documento/" + id + "/versao").file(arquivo("v2.png", "image/png", PNG)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.documentos[0].versoes.length()").value(2))
                .andExpect(jsonPath("$.documentos[0].versoes[0].numero").value(2))
                .andExpect(jsonPath("$.documentos[0].versoes[1].nomeArquivo").value("v1.pdf"));
    }

    @Test
    void retirarTiraDaListaEBloqueiaAbrir() throws Exception {
        String corpo = anexar(arquivo("v1.pdf", "application/pdf", PDF), "Errado", null).andReturn().getResponse().getContentAsString();
        String id = JsonPath.read(corpo, "$.documentos[0].uuid");
        String versao = JsonPath.read(corpo, "$.documentos[0].versoes[0].uuid");

        mockMvc.perform(post("/api/v1/documento/" + id + "/retirada").contentType(MediaType.APPLICATION_JSON).content("{\"motivo\": \"Paciente errado\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.documentos.length()").value(0))
                .andExpect(jsonPath("$.retirados[0].motivoRetirada").value("Paciente errado"));
        mockMvc.perform(post("/api/v1/documento/" + id + "/retirada").contentType(MediaType.APPLICATION_JSON).content("{\"motivo\": \"De novo\"}"))
                .andExpect(status().isUnprocessableEntity());
        mockMvc.perform(get("/api/v1/documento/versao/" + versao + "/arquivo")).andExpect(status().isUnprocessableEntity());
        mockMvc.perform(multipart("/api/v1/documento/" + id + "/versao").file(arquivo("v2.pdf", "application/pdf", PDF))).andExpect(status().isUnprocessableEntity());
    }

    @Test
    void arquivoAlteradoForaDoSistemaEDenunciado() throws Exception {
        String corpo = anexar(arquivo("v1.pdf", "application/pdf", PDF), "Laudo", null).andReturn().getResponse().getContentAsString();
        String versao = JsonPath.read(corpo, "$.documentos[0].versoes[0].uuid");
        List<String> chaves = jdbc.queryForList("select chave from tb_versao_documento where uuid = ?::uuid", String.class, versao);
        armazenamento.gravar(chaves.get(0), "%PDF-1.4 adulterado".getBytes(StandardCharsets.US_ASCII));

        mockMvc.perform(get("/api/v1/documento/versao/" + versao + "/arquivo")).andExpect(status().isConflict());
    }

    @Test
    void leituraEntraNaTrilhaDoPaciente() throws Exception {
        anexar(arquivo("v1.pdf", "application/pdf", PDF), "Laudo", null).andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/paciente/" + paciente + "/documentos")).andExpect(status().isOk());
        Integer leituras = jdbc.queryForObject("select count(*) from tb_evento_auditoria where paciente_id = ? and acao = 'LEITURA'", Integer.class, paciente);
        assertThat(leituras).isPositive();
    }
}
