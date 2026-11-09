package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.models.ConteudoEducativo;
import com.edufelizardo.maissaudepublica.models.Profissional;
import com.edufelizardo.maissaudepublica.models.Treinamento;
import com.edufelizardo.maissaudepublica.models.enuns.PublicoComunicacao;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoConteudo;
import com.edufelizardo.maissaudepublica.repositories.CompetenciaRepository;
import com.edufelizardo.maissaudepublica.repositories.ConteudoEducativoRepository;
import com.edufelizardo.maissaudepublica.repositories.ProfissionalRepository;
import com.edufelizardo.maissaudepublica.repositories.TreinamentoRepository;
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
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDate;
import java.util.UUID;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Educação permanente, fatia C4 (ADR-0173): catálogo de competências, ligações do treinamento e competências do profissional. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class EducacaoPermanenteControllerTest {

    private static final String URL = "/api/v1/educacao-permanente/";
    private static final String PREFIXO = "EduPerm Teste ";
    private static final String CODIGO = "TESTE_C4_";
    private static final String MATRICULA = "TESTE-C4-0001";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private TreinamentoRepository treinamentoRepository;

    @Autowired
    private CompetenciaRepository competenciaRepository;

    @Autowired
    private ConteudoEducativoRepository conteudoRepository;

    @Autowired
    private ProfissionalRepository profissionalRepository;

    private UUID treinamento;
    private UUID biosseguranca;
    private UUID revisado;
    private UUID rascunho;

    @BeforeEach
    void seed() {
        limpar();
        treinamento = treinamento("NR-32", 12);
        biosseguranca = competenciaRepository.findByCodigo("BIOSSEGURANCA").orElseThrow().getUuid();
        revisado = conteudo("Higiene das mãos", 1, SituacaoConteudo.SUBSTITUIDO, 30, null);
        conteudo("Higiene das mãos", 2, SituacaoConteudo.PUBLICADO, 30, revisado);
        conteudo("Higiene das mãos", 3, SituacaoConteudo.RASCUNHO, 30, revisado);
        rascunho = conteudo("Sem revisão", 1, SituacaoConteudo.EM_REVISAO, 30, null);
    }

    @AfterEach
    void limpar() {
        String treinos = "(select uuid from tb_treinamento where nome like '" + PREFIXO + "%')";
        jdbc.update("delete from tb_treinamento_competencia where treinamento_id in " + treinos + " or competencia_id in (select uuid from tb_competencia where codigo like ?)", CODIGO + "%");
        jdbc.update("delete from tb_treinamento_conteudo where treinamento_id in " + treinos);
        jdbc.update("delete from tb_participacao_treinamento where treinamento_id in " + treinos);
        jdbc.update("delete from tb_treinamento where nome like ?", PREFIXO + "%");
        jdbc.update("delete from tb_competencia where codigo like ?", CODIGO + "%");
        jdbc.update("delete from tb_conteudo_educativo where titulo like ?", PREFIXO + "%");
        profissionalRepository.findByMatricula(MATRICULA).ifPresent(profissionalRepository::delete);
    }

    private UUID treinamento(String nome, Integer validadeMeses) {
        Treinamento t = new Treinamento();
        t.setNome(PREFIXO + nome);
        t.setCargaHoraria(8);
        t.setValidadeMeses(validadeMeses);
        t.setObrigatorio(true);
        return treinamentoRepository.save(t).getUuid();
    }

    private UUID conteudo(String titulo, int versao, SituacaoConteudo situacao, int diasDeValidade, UUID grupo) {
        ConteudoEducativo c = new ConteudoEducativo();
        UUID g = grupo != null ? grupo : UUID.randomUUID();
        c.setGrupoId(g);
        c.setVersao(versao);
        c.setTitulo(PREFIXO + titulo);
        c.setResumo("Resumo");
        c.setTexto("Texto");
        c.setTema("Biossegurança");
        c.setPublico(PublicoComunicacao.PROFISSIONAIS);
        c.setValidade(LocalDate.now().plusDays(diasDeValidade));
        c.setSituacao(situacao);
        c.setAutorCpf("00000000000");
        conteudoRepository.save(c);
        return g;
    }

    private ResultActions ligar(UUID treino, String competencias, String conteudos) throws Exception {
        return mockMvc.perform(put(URL + "treinamento/" + treino).contentType(MediaType.APPLICATION_JSON)
                .content("{\"competenciaIds\": [" + competencias + "], \"conteudoGrupoIds\": [" + conteudos + "]}"));
    }

    private static String q(UUID id) {
        return "\"" + id + "\"";
    }

    @Test
    void catalogoTrazACargaERecusaCodigoRepetido() throws Exception {
        mockMvc.perform(get(URL + "competencia")).andExpect(status().isOk())
                .andExpect(jsonPath("$[*].codigo", hasItems("BIOSSEGURANCA", "SUPORTE_BASICO_VIDA", "GESTAO_DO_CUIDADO")));
        mockMvc.perform(post(URL + "competencia").contentType(MediaType.APPLICATION_JSON).content("{\"codigo\": \"biosseguranca\", \"nome\": \"X\"}"))
                .andExpect(status().isConflict());
        String r = mockMvc.perform(post(URL + "competencia").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"codigo\": \"%sa\", \"nome\": \"Nova\", \"descricao\": \"  \"}".formatted(CODIGO)))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.codigo").value(CODIGO + "A")).andExpect(jsonPath("$.ativo").value(true))
                .andExpect(jsonPath("$.descricao").doesNotExist())
                .andReturn().getResponse().getContentAsString();
        mockMvc.perform(patch(URL + "competencia/" + JsonPath.read(r, "$.uuid")).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"codigo\": \"%sA\", \"nome\": \"Outro nome\", \"ativo\": false}".formatted(CODIGO)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.nome").value("Outro nome")).andExpect(jsonPath("$.ativo").value(false));
        mockMvc.perform(post(URL + "competencia").contentType(MediaType.APPLICATION_JSON).content("{\"codigo\": \"\", \"nome\": \"\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void ligaCompetenciasEAVersaoEmVigorDoConteudo() throws Exception {
        ligar(treinamento, q(biosseguranca) + ", " + q(biosseguranca), q(revisado)).andExpect(status().isOk())
                .andExpect(jsonPath("$.competencias.length()").value(1))
                .andExpect(jsonPath("$.competencias[0].codigo").value("BIOSSEGURANCA"))
                .andExpect(jsonPath("$.conteudos.length()").value(1))
                .andExpect(jsonPath("$.conteudos[0].versao").value(2))
                .andExpect(jsonPath("$.conteudos[0].situacao").value("PUBLICADO"));
        mockMvc.perform(get(URL + "treinamento")).andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.treinamentoId == '" + treinamento + "')].conteudos[0].titulo").value(hasItem(PREFIXO + "Higiene das mãos")));
        mockMvc.perform(get(URL + "uso-dos-conteudos")).andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.grupoId == '" + revisado + "')].treinamentos[0]").value(hasItem(PREFIXO + "NR-32")));
        ligar(treinamento, "", "").andExpect(status().isOk()).andExpect(jsonPath("$.competencias.length()").value(0));
    }

    @Test
    void conteudoSemRevisaoNaoEntra() throws Exception {
        ligar(treinamento, "", q(rascunho)).andExpect(status().isUnprocessableEntity());
        ligar(treinamento, "", q(UUID.randomUUID())).andExpect(status().isNotFound());
        mockMvc.perform(get(URL + "conteudo")).andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.grupoId == '" + revisado + "')].versao").value(hasItem(2)))
                .andExpect(jsonPath("$[?(@.grupoId == '" + rascunho + "')]").isEmpty());
    }

    @Test
    void conteudoVencidoApareceMarcado() throws Exception {
        UUID vencido = conteudo("Vencido", 1, SituacaoConteudo.PUBLICADO, -1, null);
        ligar(treinamento, "", q(vencido)).andExpect(status().isOk())
                .andExpect(jsonPath("$.conteudos[0].vencido").value(true))
                .andExpect(jsonPath("$.conteudos[0].situacao").value("EXPIRADO"));
    }

    @Test
    void competenciaInativaSoFicaSeJaEstavaLigada() throws Exception {
        String r = mockMvc.perform(post(URL + "competencia").contentType(MediaType.APPLICATION_JSON)
                .content("{\"codigo\": \"%sINATIVA\", \"nome\": \"Inativa\"}".formatted(CODIGO))).andReturn().getResponse().getContentAsString();
        UUID inativa = UUID.fromString(JsonPath.read(r, "$.uuid"));
        ligar(treinamento, q(inativa), "").andExpect(status().isOk());
        mockMvc.perform(patch(URL + "competencia/" + inativa).contentType(MediaType.APPLICATION_JSON)
                .content("{\"codigo\": \"%sINATIVA\", \"nome\": \"Inativa\", \"ativo\": false}".formatted(CODIGO))).andExpect(status().isOk());
        ligar(treinamento, q(inativa), "").andExpect(status().isOk());
        UUID outro = treinamento("Outro", null);
        ligar(outro, q(inativa), "").andExpect(status().isUnprocessableEntity());
        ligar(outro, q(UUID.randomUUID()), "").andExpect(status().isNotFound());
        ligar(UUID.randomUUID(), "", "").andExpect(status().isNotFound());
    }

    @Test
    void competenciasDoProfissionalVemDasParticipacoes() throws Exception {
        Profissional p = new Profissional();
        p.setMatricula(MATRICULA);
        p.setCpf("00000000000");
        p.setNome(PREFIXO + "Profissional");
        p.setAtivo(true);
        profissionalRepository.save(p);
        UUID suporte = competenciaRepository.findByCodigo("SUPORTE_BASICO_VIDA").orElseThrow().getUuid();
        UUID reciclagem = treinamento("Reciclagem", null);
        ligar(treinamento, q(biosseguranca) + ", " + q(suporte), "").andExpect(status().isOk());
        ligar(reciclagem, q(biosseguranca), "").andExpect(status().isOk());
        participar(treinamento, LocalDate.now().minusYears(2));

        mockMvc.perform(get(URL + "profissional/" + MATRICULA + "/competencias")).andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[?(@.codigo == 'BIOSSEGURANCA')].vencida").value(hasItem(true)));

        participar(reciclagem, LocalDate.now().minusDays(3));
        mockMvc.perform(get(URL + "profissional/" + MATRICULA + "/competencias")).andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.codigo == 'BIOSSEGURANCA')].vencida").value(hasItem(false)))
                .andExpect(jsonPath("$[?(@.codigo == 'BIOSSEGURANCA')].treinamentoNome").value(hasItem(PREFIXO + "Reciclagem")))
                .andExpect(jsonPath("$[?(@.codigo == 'SUPORTE_BASICO_VIDA')].vencida").value(hasItem(true)));
        mockMvc.perform(get(URL + "profissional/NAO-EXISTE/competencias")).andExpect(status().isNotFound());
    }

    private void participar(UUID treino, LocalDate conclusao) throws Exception {
        mockMvc.perform(post("/api/v1/participacao-treinamento/").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"matriculaProfissional\": \"%s\", \"treinamentoId\": \"%s\", \"dataConclusao\": \"%s\"}".formatted(MATRICULA, treino, conclusao)))
                .andExpect(status().is2xxSuccessful());
    }
}
