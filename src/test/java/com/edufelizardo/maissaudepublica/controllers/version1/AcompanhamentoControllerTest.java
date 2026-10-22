package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.models.CategoriaPrograma;
import com.edufelizardo.maissaudepublica.models.Paciente;
import com.edufelizardo.maissaudepublica.models.ParticipacaoPrograma;
import com.edufelizardo.maissaudepublica.models.Profissional;
import com.edufelizardo.maissaudepublica.models.Programa;
import com.edufelizardo.maissaudepublica.models.UnidadeDeSaude;
import com.edufelizardo.maissaudepublica.models.enuns.AbrangenciaPrograma;
import com.edufelizardo.maissaudepublica.models.enuns.PeriodicidadeAcompanhamento;
import com.edufelizardo.maissaudepublica.models.enuns.Sexo;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoPrograma;
import com.edufelizardo.maissaudepublica.models.enuns.TipoUnidadeDeSaude;
import com.edufelizardo.maissaudepublica.repositories.CategoriaProgramaRepository;
import com.edufelizardo.maissaudepublica.repositories.PacienteRepository;
import com.edufelizardo.maissaudepublica.repositories.ParticipacaoProgramaRepository;
import com.edufelizardo.maissaudepublica.repositories.ProfissionalRepository;
import com.edufelizardo.maissaudepublica.repositories.ProgramaRepository;
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
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Acompanhamento programático, fatia P2 (ADR-0119): inscrição com as regras (unidade que oferece, um vigente, programa
 * ativo, aviso de público), faltoso, saída com reinscrição, atrasado pela periodicidade, os programas do paciente, os
 * inscritos do programa e as contagens no catálogo.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AcompanhamentoControllerTest {

    private static final String PREFIXO = "Acompanhamento Teste ";
    private static final String CODIGO = "TESTE_AC_";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private UnidadeDeSaudeRepository unidadeDeSaudeRepository;

    @Autowired
    private PacienteRepository pacienteRepository;

    @Autowired
    private ProgramaRepository programaRepository;

    @Autowired
    private CategoriaProgramaRepository categoriaRepository;

    @Autowired
    private ParticipacaoProgramaRepository participacaoRepository;

    @Autowired
    private ProfissionalRepository profissionalRepository;

    private UnidadeDeSaude ubs;
    private UnidadeDeSaude outra;
    private Programa hiperdia;
    private Paciente adulto;
    private Paciente adolescente;

    @BeforeEach
    void seed() {
        limpar();
        ubs = unidade("UBS Vila Esperança");
        outra = unidade("UBS Centro");
        hiperdia = programa("HD", PeriodicidadeAcompanhamento.MENSAL, SituacaoPrograma.ATIVO);
        participacaoRepository.save(new ParticipacaoPrograma(null, hiperdia, ubs, null, LocalDate.now().minusYears(1), null, null, null));
        adulto = paciente("João", LocalDate.now().minusYears(45), "90022233344");
        adolescente = paciente("Ana", LocalDate.now().minusYears(15), "90022233355");
        Profissional p = new Profissional();
        p.setMatricula("AC-SEMLOTACAO");
        p.setCpf("90022233366");
        p.setNome(PREFIXO + "Sem lotação");
        p.setAtivo(true);
        profissionalRepository.save(p);
    }

    @AfterEach
    void limpar() {
        jdbc.update("delete from tb_evento_acompanhamento where acompanhamento_id in (select a.uuid from tb_acompanhamento_programatico a "
                + "join tb_programa p on p.uuid = a.programa_id where p.codigo like ?)", CODIGO + "%");
        jdbc.update("delete from tb_acompanhamento_programatico where programa_id in (select uuid from tb_programa where codigo like ?)", CODIGO + "%");
        jdbc.update("delete from tb_participacao_programa where programa_id in (select uuid from tb_programa where codigo like ?)", CODIGO + "%");
        jdbc.update("delete from tb_programa where codigo like ?", CODIGO + "%");
        pacienteRepository.deleteAll(pacienteRepository.findAll().stream().filter(x -> x.getNome() != null && x.getNome().startsWith(PREFIXO)).toList());
        unidadeDeSaudeRepository.deleteAllById(unidadeDeSaudeRepository.findAll().stream()
                .filter(u -> u.getNome() != null && u.getNome().startsWith(PREFIXO)).map(UnidadeDeSaude::getUuid).toList());
        profissionalRepository.findByMatricula("AC-SEMLOTACAO").ifPresent(profissionalRepository::delete);
    }

    private UnidadeDeSaude unidade(String nome) {
        UnidadeDeSaude u = new UnidadeDeSaude();
        u.setNome(PREFIXO + nome);
        u.setTipo(TipoUnidadeDeSaude.UBS);
        u.setAtivo(true);
        return unidadeDeSaudeRepository.save(u);
    }

    private Programa programa(String sufixo, PeriodicidadeAcompanhamento periodicidade, SituacaoPrograma situacao) {
        CategoriaPrograma cronicos = categoriaRepository.findByNomeIgnoreCase("Crônicos").orElseThrow();
        Programa p = new Programa();
        p.setCodigo(CODIGO + sufixo);
        p.setNome(PREFIXO + sufixo);
        p.setCategoria(cronicos);
        p.setAbrangencia(AbrangenciaPrograma.MUNICIPAL);
        p.setSituacao(situacao);
        p.setPeriodicidade(periodicidade);
        p.setIdadeMinima(18);
        return programaRepository.save(p);
    }

    private Paciente paciente(String nome, LocalDate nascimento, String cpf) {
        Paciente p = new Paciente();
        p.setNome(PREFIXO + nome);
        p.setCpf(cpf);
        p.setDataNascimento(nascimento);
        p.setSexo(Sexo.MASCULINO);
        p.setAtivo(true);
        return pacienteRepository.save(p);
    }

    private ResultActions inscrever(Programa programa, Paciente paciente, UnidadeDeSaude unidade, String extra) throws Exception {
        return mockMvc.perform(post("/api/v1/programa/" + programa.getUuid() + "/acompanhamento").contentType(MediaType.APPLICATION_JSON)
                .content("{\"pacienteId\": \"%s\", \"unidadeId\": \"%s\"%s}".formatted(paciente.getUuid(), unidade.getUuid(), extra)));
    }

    @Test
    void inscricaoComAsRegrasEAvisoDePublico() throws Exception {
        inscrever(hiperdia, adulto, ubs, "").andExpect(status().isCreated())
                .andExpect(jsonPath("$.situacao").value("ATIVO"))
                .andExpect(jsonPath("$.pacienteIdade").value(45))
                .andExpect(jsonPath("$.avisoPublico").doesNotExist())
                .andExpect(jsonPath("$.eventos.length()").value(1));
        inscrever(hiperdia, adulto, ubs, "").andExpect(status().isConflict());
        inscrever(hiperdia, adolescente, outra, "").andExpect(status().isUnprocessableEntity());
        inscrever(hiperdia, adolescente, ubs, ", \"inicio\": \"%s\"".formatted(LocalDate.now().plusDays(1))).andExpect(status().isBadRequest());
        inscrever(hiperdia, adolescente, ubs, ", \"profissionalMatricula\": \"AC-SEMLOTACAO\"").andExpect(status().isUnprocessableEntity());
        inscrever(hiperdia, adolescente, ubs, "").andExpect(status().isCreated())
                .andExpect(jsonPath("$.avisoPublico", containsString("15 anos")));

        Programa suspenso = programa("SUSP", PeriodicidadeAcompanhamento.MENSAL, SituacaoPrograma.SUSPENSO);
        participacaoRepository.save(new ParticipacaoPrograma(null, suspenso, ubs, null, LocalDate.now(), null, null, null));
        inscrever(suspenso, adulto, ubs, "").andExpect(status().isUnprocessableEntity());
    }

    @Test
    void faltosoSaidaEReinscricaoComoHistorico() throws Exception {
        String id = JsonPath.read(inscrever(hiperdia, adulto, ubs, "").andReturn().getResponse().getContentAsString(), "$.uuid");
        String situacao = "/api/v1/acompanhamento/" + id + "/situacao";
        mockMvc.perform(post(situacao).contentType(MediaType.APPLICATION_JSON).content("{\"situacao\": \"FALTOSO\", \"motivo\": \"Faltou a 3 consultas\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.situacao").value("FALTOSO"));
        mockMvc.perform(post(situacao).contentType(MediaType.APPLICATION_JSON).content("{\"situacao\": \"FALTOSO\", \"motivo\": \"De novo\"}"))
                .andExpect(status().isUnprocessableEntity());
        mockMvc.perform(post(situacao).contentType(MediaType.APPLICATION_JSON).content("{\"situacao\": \"CONCLUIDO\", \"motivo\": \"x\"}"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/api/v1/programa/" + hiperdia.getUuid() + "/acompanhamentos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.inscritos").value(1))
                .andExpect(jsonPath("$.faltosos").value(1));

        String encerrar = "/api/v1/acompanhamento/" + id + "/encerramento";
        mockMvc.perform(post(encerrar).contentType(MediaType.APPLICATION_JSON).content("{\"motivoSaida\": \"ALTA\"}")).andExpect(status().isBadRequest());
        mockMvc.perform(post(encerrar).contentType(MediaType.APPLICATION_JSON).content("{\"motivoSaida\": \"ALTA\", \"observacao\": \"Metas atingidas\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.situacao").value("CONCLUIDO"))
                .andExpect(jsonPath("$.eventos.length()").value(3));
        mockMvc.perform(post(encerrar).contentType(MediaType.APPLICATION_JSON).content("{\"motivoSaida\": \"OBITO\", \"observacao\": \"x\"}"))
                .andExpect(status().isUnprocessableEntity());

        inscrever(hiperdia, adulto, ubs, "").andExpect(status().isCreated());
        mockMvc.perform(get("/api/v1/paciente/" + adulto.getUuid() + "/programas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.vigentes.length()").value(1))
                .andExpect(jsonPath("$.encerrados[0].motivoSaida").value("ALTA"))
                .andExpect(jsonPath("$.restritos").value(0))
                .andExpect(jsonPath("$.acesso.base").value("LIVRE"));
    }

    @Test
    void atrasadoPelaPeriodicidadeEContagensNoCatalogo() throws Exception {
        inscrever(hiperdia, adulto, ubs, ", \"inicio\": \"%s\"".formatted(LocalDate.now().minusDays(40))).andExpect(status().isCreated())
                .andExpect(jsonPath("$.atrasado").value(true));
        Programa semPeriodo = programa("SP", PeriodicidadeAcompanhamento.SEM_PERIODICIDADE, SituacaoPrograma.ATIVO);
        participacaoRepository.save(new ParticipacaoPrograma(null, semPeriodo, ubs, null, LocalDate.now().minusYears(1), null, null, null));
        inscrever(semPeriodo, adulto, ubs, ", \"inicio\": \"%s\"".formatted(LocalDate.now().minusDays(400))).andExpect(status().isCreated())
                .andExpect(jsonPath("$.atrasado").value(false));

        mockMvc.perform(get("/api/v1/programa/"))
                .andExpect(jsonPath("$[?(@.codigo == 'TESTE_AC_HD')].inscritos", hasItem(1)))
                .andExpect(jsonPath("$[?(@.codigo == 'TESTE_AC_HD')].atrasados", hasItem(1)));
        mockMvc.perform(get("/api/v1/programa/").param("unidadeId", outra.getUuid().toString()))
                .andExpect(jsonPath("$.length()").value(0));
        mockMvc.perform(get("/api/v1/acompanhamento/resumo")).andExpect(status().isOk())
                .andExpect(jsonPath("$.pacientes").isNumber());
        List<?> lista = JsonPath.read(mockMvc.perform(get("/api/v1/programa/" + hiperdia.getUuid() + "/acompanhamentos")
                .param("situacao", "FALTOSO")).andReturn().getResponse().getContentAsString(), "$.lista");
        org.assertj.core.api.Assertions.assertThat(lista).isEmpty();
    }

    @Test
    void pacienteOuAcompanhamentoInexistente() throws Exception {
        mockMvc.perform(get("/api/v1/paciente/" + UUID.randomUUID() + "/programas")).andExpect(status().isNotFound());
        mockMvc.perform(post("/api/v1/acompanhamento/" + UUID.randomUUID() + "/situacao").contentType(MediaType.APPLICATION_JSON)
                .content("{\"situacao\": \"FALTOSO\", \"motivo\": \"x\"}")).andExpect(status().isNotFound());
    }
}
