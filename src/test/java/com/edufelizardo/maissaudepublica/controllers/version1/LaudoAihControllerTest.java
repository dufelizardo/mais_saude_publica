package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.models.Leito;
import com.edufelizardo.maissaudepublica.models.Paciente;
import com.edufelizardo.maissaudepublica.models.ProcedimentoCatalogo;
import com.edufelizardo.maissaudepublica.models.Profissional;
import com.edufelizardo.maissaudepublica.models.Setor;
import com.edufelizardo.maissaudepublica.models.UnidadeDeSaude;
import com.edufelizardo.maissaudepublica.models.enuns.AlcanceProcedimento;
import com.edufelizardo.maissaudepublica.models.enuns.FaixaEtariaLeito;
import com.edufelizardo.maissaudepublica.models.enuns.Sexo;
import com.edufelizardo.maissaudepublica.models.enuns.SexoLeito;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoLeito;
import com.edufelizardo.maissaudepublica.models.enuns.TipoLeito;
import com.edufelizardo.maissaudepublica.models.enuns.TipoSetor;
import com.edufelizardo.maissaudepublica.models.enuns.TipoUnidadeDeSaude;
import com.edufelizardo.maissaudepublica.repositories.LeitoRepository;
import com.edufelizardo.maissaudepublica.repositories.PacienteRepository;
import com.edufelizardo.maissaudepublica.repositories.ProcedimentoCatalogoRepository;
import com.edufelizardo.maissaudepublica.repositories.ProfissionalRepository;
import com.edufelizardo.maissaudepublica.repositories.SetorRepository;
import com.edufelizardo.maissaudepublica.repositories.UnidadeDeSaudeRepository;
import com.edufelizardo.maissaudepublica.services.version1.LaudoAihService;
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

import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Laudo para solicitação de AIH e autorização interna (ADR-0186), com login e autorização desligados. O escopo e a regra de quem
 * solicitou não autorizar pelo usuário logado ficam em {@link LaudoAihAutorizacaoControllerTest}. As faixas são da rede inteira:
 * o teste apaga todas antes e depois.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class LaudoAihControllerTest {

    private static final String AIH = "/api/v1/laudo-aih/";
    private static final String PREFIXO = "LaudoAih Teste ";
    private static final int ANO = LocalDate.now().getYear();

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private UnidadeDeSaudeRepository unidadeDeSaudeRepository;

    @Autowired
    private SetorRepository setorRepository;

    @Autowired
    private LeitoRepository leitoRepository;

    @Autowired
    private PacienteRepository pacienteRepository;

    @Autowired
    private ProfissionalRepository profissionalRepository;

    @Autowired
    private ProcedimentoCatalogoRepository procedimentoRepository;

    private UnidadeDeSaude hospital;
    private Setor clinica;
    private UUID procedimento;
    private int leitos;

    @BeforeEach
    void seed() {
        limpar();
        hospital = new UnidadeDeSaude();
        hospital.setNome(PREFIXO + "Hospital");
        hospital.setTipo(TipoUnidadeDeSaude.HOSPITAL);
        hospital.setAtivo(true);
        hospital = unidadeDeSaudeRepository.save(hospital);
        clinica = setorRepository.save(new Setor(hospital, PREFIXO + "Clínica médica", "LA-CM", TipoSetor.ASSISTENCIAL, true, null));
        profissional("LA-MED", "28374655647", "Médica assistente");
        profissional("LA-AUT", "39485766758", "Médico autorizador");
        ProcedimentoCatalogo p = new ProcedimentoCatalogo();
        p.setCodigo("LA-PROC");
        p.setNome(PREFIXO + "Tratamento de pneumonia");
        p.setSigtap("0303140151");
        p.setAlcance(AlcanceProcedimento.NENHUM);
        p.setAtivo(true);
        procedimento = procedimentoRepository.save(p).getUuid();
        leitos = 0;
    }

    @AfterEach
    void limpar() {
        jdbc.update("delete from tb_faixa_aih");
        List<UUID> unidades = unidadeDeSaudeRepository.findAll().stream()
                .filter(u -> u.getNome() != null && u.getNome().startsWith(PREFIXO)).map(UnidadeDeSaude::getUuid).toList();
        for (UUID u : unidades) {
            jdbc.update("delete from tb_evento_laudo_aih where laudo_id in (select uuid from tb_laudo_aih where unidade_id = ?)", u);
            jdbc.update("delete from tb_laudo_aih where unidade_id = ?", u);
            jdbc.update("delete from tb_evento_internacao_agendada where agendamento_id in (select uuid from tb_internacao_agendada where unidade_id = ?)", u);
            jdbc.update("delete from tb_internacao_agendada where unidade_id = ?", u);
            jdbc.update("delete from tb_evento_leito where leito_id in (select uuid from tb_leito where unidade_id = ?)", u);
            jdbc.update("delete from tb_internacao where unidade_id = ?", u);
            jdbc.update("delete from tb_leito where unidade_id = ?", u);
            jdbc.update("delete from tb_setor where unidade_id = ?", u);
        }
        unidadeDeSaudeRepository.deleteAllById(unidades);
        pacienteRepository.deleteAll(pacienteRepository.findAll().stream()
                .filter(p -> p.getNome() != null && p.getNome().startsWith(PREFIXO)).toList());
        for (String m : List.of("LA-MED", "LA-AUT")) {
            profissionalRepository.findByMatricula(m).ifPresent(profissionalRepository::delete);
        }
        procedimentoRepository.findAll().stream().filter(p -> "LA-PROC".equals(p.getCodigo())).forEach(procedimentoRepository::delete);
    }

    @Test
    void digitoVerificadorEhORestoDaDivisaoPorOnze() {
        assertThat(LaudoAihService.numeroComDigito(123456789012L)).isEqualTo("1234567890127");
        assertThat(LaudoAihService.numeroComDigito(10L)).isEqualTo("0000000000100");
        assertThat(LaudoAihService.numeroComDigito(1L)).isEqualTo("0000000000011");
    }

    @Test
    void solicitaDevolveCorrigeEAutorizaComONumeroDaFaixa() throws Exception {
        UUID internacao = internar("Ana");
        String corpo = laudo("internacaoId", internacao);
        UUID laudo = criar(corpo);
        mockMvc.perform(post(AIH).contentType(MediaType.APPLICATION_JSON).content(corpo)).andExpect(status().isConflict());
        mockMvc.perform(post(AIH).contentType(MediaType.APPLICATION_JSON).content(laudo("internacaoId", null))).andExpect(status().isBadRequest());

        decisao(laudo, "devolucao", "LA-AUT", null).andExpect(status().isBadRequest());
        decisao(laudo, "devolucao", "LA-AUT", "Descreva os resultados da gasometria.")
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("DEVOLVIDO"));
        mockMvc.perform(put(AIH + laudo).contentType(MediaType.APPLICATION_JSON).content(corpo.replace("Raio X com consolidação.", "Gasometria com hipoxemia.")))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("SOLICITADO"))
                .andExpect(jsonPath("$.resultadosExames").value("Gasometria com hipoxemia."));
        mockMvc.perform(get(AIH + "fila")).andExpect(jsonPath("$[?(@.uuid == '%s')].carater".formatted(laudo)).value(org.hamcrest.Matchers.contains("URGENCIA")));

        decisao(laudo, "autorizacao", "LA-MED", null).andExpect(status().isUnprocessableEntity());
        decisao(laudo, "autorizacao", "LA-AUT", null).andExpect(status().isUnprocessableEntity());

        faixa(1000, 1001).andExpect(status().isCreated())
                .andExpect(jsonPath("$.primeiroNumero").value(LaudoAihService.numeroComDigito(1000)))
                .andExpect(jsonPath("$.restantes").value(2));
        faixa(990, 1000).andExpect(status().isConflict());
        faixa(2000, 1999).andExpect(status().isBadRequest());

        String numero = LaudoAihService.numeroComDigito(1000);
        decisao(laudo, "autorizacao", "LA-AUT", null).andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("AUTORIZADO"))
                .andExpect(jsonPath("$.numeroAih").value(numero))
                .andExpect(jsonPath("$.eventos[*].tipo").value(org.hamcrest.Matchers.contains("SOLICITACAO", "DEVOLUCAO", "CORRECAO", "AUTORIZACAO")));
        mockMvc.perform(get("/api/v1/internacao/" + internacao)).andExpect(jsonPath("$.numeroAih").value(numero));
        decisao(laudo, "negacao", "LA-AUT", "Tarde demais.").andExpect(status().isUnprocessableEntity());
        mockMvc.perform(get(AIH + laudo + "/laudo.pdf")).andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_PDF));

        UUID segundo = criar(laudo("internacaoId", internar("Bia")));
        decisao(segundo, "autorizacao", "LA-AUT", null).andExpect(jsonPath("$.numeroAih").value(LaudoAihService.numeroComDigito(1001)));
        UUID terceiro = criar(laudo("internacaoId", internar("Caio")));
        decisao(terceiro, "autorizacao", "LA-AUT", null).andExpect(status().isUnprocessableEntity());
        mockMvc.perform(get(AIH + "faixas")).andExpect(jsonPath("$[0].esgotada").value(true));
    }

    @Test
    void negaECancelaComMotivo() throws Exception {
        UUID negado = criar(laudo("internacaoId", internar("Davi")));
        decisao(negado, "negacao", "LA-AUT", "  ").andExpect(status().isBadRequest());
        decisao(negado, "negacao", "LA-AUT", "Quadro sem critério de internação.").andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("NEGADO")).andExpect(jsonPath("$.autorizadorNome").value(PREFIXO + "Médico autorizador"));
        decisao(negado, "cancelamento", "LA-MED", "Engano.").andExpect(status().isUnprocessableEntity());

        UUID internacao = internar("Eva");
        UUID cancelado = criar(laudo("internacaoId", internacao));
        decisao(cancelado, "cancelamento", "LA-MED", "Laudo preenchido no paciente errado.").andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELADO"));
        criar(laudo("internacaoId", internacao));
        mockMvc.perform(get(AIH + "?internacaoId=" + internacao)).andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void laudoDaEletivaPassaParaAInternacaoNaAdmissao() throws Exception {
        faixa(5000, 5009).andExpect(status().isCreated());
        Paciente p = paciente("Fábio");
        UUID leito = leito();
        String corpo = mockMvc.perform(post("/api/v1/internacao-eletiva/").contentType(MediaType.APPLICATION_JSON).content("""
                        {"pacienteId": "%s", "unidadeId": "%s", "dataPrevista": "%s", "tipoLeito": "CLINICO", "medicoMatricula": "LA-MED",
                         "cid": "K80.2", "procedimento": "Colecistectomia."}
                        """.formatted(p.getUuid(), hospital.getUuid(), LocalDate.now())))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        UUID agendamento = UUID.fromString(JsonPath.read(corpo, "$.uuid"));
        UUID laudo = criar(laudo("agendamentoId", agendamento));
        mockMvc.perform(get(AIH + laudo)).andExpect(jsonPath("$.carater").value("ELETIVA")).andExpect(jsonPath("$.internacaoId").isEmpty());
        decisao(laudo, "autorizacao", "LA-AUT", null).andExpect(status().isOk());

        String admitido = mockMvc.perform(post("/api/v1/internacao-eletiva/" + agendamento + "/admissao").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"profissionalMatricula\": \"LA-MED\", \"leitoId\": \"%s\"}".formatted(leito)))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        UUID internacao = UUID.fromString(JsonPath.read(admitido, "$.internacaoId"));
        mockMvc.perform(get(AIH + laudo)).andExpect(jsonPath("$.internacaoId").value(internacao.toString()))
                .andExpect(jsonPath("$.eventos[*].tipo").value(org.hamcrest.Matchers.contains("SOLICITACAO", "AUTORIZACAO", "VINCULO_INTERNACAO")));
        mockMvc.perform(get("/api/v1/internacao/" + internacao)).andExpect(jsonPath("$.numeroAih").value(LaudoAihService.numeroComDigito(5000)));
        mockMvc.perform(post(AIH).contentType(MediaType.APPLICATION_JSON).content(laudo("internacaoId", internacao))).andExpect(status().isConflict());
    }

    @Test
    void urgenciaSemLaudoDepoisDe48HorasEhPendencia() throws Exception {
        UUID antiga = internar("Gil");
        UUID recente = internar("Hugo");
        jdbc.update("update tb_internacao set admitida_em = ? where uuid = ?", Timestamp.from(Instant.now().minus(Duration.ofHours(50))), antiga);
        mockMvc.perform(get(AIH + "pendencias"))
                .andExpect(jsonPath("$[?(@.internacaoId == '%s')].horasSemLaudo".formatted(antiga)).value(org.hamcrest.Matchers.contains(50)))
                .andExpect(jsonPath("$[?(@.internacaoId == '%s')]".formatted(recente)).isEmpty());
        criar(laudo("internacaoId", antiga));
        mockMvc.perform(get(AIH + "pendencias")).andExpect(jsonPath("$[?(@.internacaoId == '%s')]".formatted(antiga)).isEmpty());
    }

    // ── Apoio ──────────────────────────────────────────────────────────────────────────────────

    private void profissional(String matricula, String cpf, String nome) {
        Profissional p = new Profissional();
        p.setMatricula(matricula);
        p.setCpf(cpf);
        p.setNome(PREFIXO + nome);
        p.setAtivo(true);
        profissionalRepository.save(p);
    }

    private Paciente paciente(String nome) {
        Paciente p = new Paciente();
        p.setNome(PREFIXO + nome);
        p.setSexo(Sexo.FEMININO);
        p.setDataNascimento(LocalDate.of(1970, 1, 1));
        p.setAtivo(true);
        return pacienteRepository.save(p);
    }

    private UUID leito() {
        Leito l = new Leito();
        l.setUnidade(hospital);
        l.setSetor(clinica);
        l.setIdentificacao("LA-" + (++leitos));
        l.setTipo(TipoLeito.CLINICO);
        l.setSexo(SexoLeito.MISTO);
        l.setFaixaEtaria(FaixaEtariaLeito.ADULTO);
        l.setSituacao(SituacaoLeito.LIVRE);
        l.setAtivo(true);
        return leitoRepository.save(l).getUuid();
    }

    private UUID internar(String nome) throws Exception {
        String corpo = mockMvc.perform(post("/api/v1/internacao/").contentType(MediaType.APPLICATION_JSON).content("""
                {"pacienteId": "%s", "leitoId": "%s", "medicoMatricula": "LA-MED", "cid": "J18.9", "motivo": "Pneumonia.", "carater": "URGENCIA"}
                """.formatted(paciente(nome).getUuid(), leito()))).andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return UUID.fromString(JsonPath.read(corpo, "$.uuid"));
    }

    private String laudo(String campo, UUID id) {
        return """
                {"%s": %s, "profissionalMatricula": "LA-MED", "sinaisSintomas": "Febre, tosse produtiva e dispneia.",
                 "condicoesJustificativas": "Saturação de 88 por cento em ar ambiente.", "resultadosExames": "Raio X com consolidação.",
                 "diagnosticoInicial": "Pneumonia comunitária grave.", "cidPrincipal": "J18.9", "cidSecundario": "", "procedimentoId": "%s",
                 "clinica": "CLINICA_MEDICA"}
                """.formatted(campo, id != null ? "\"" + id + "\"" : "null", procedimento);
    }

    private UUID criar(String corpo) throws Exception {
        String r = mockMvc.perform(post(AIH).contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return UUID.fromString(JsonPath.read(r, "$.uuid"));
    }

    private ResultActions decisao(UUID laudo, String rota, String matricula, String motivo) throws Exception {
        return mockMvc.perform(post(AIH + laudo + "/" + rota).contentType(MediaType.APPLICATION_JSON).content("""
                {"profissionalMatricula": "%s"%s}
                """.formatted(matricula, motivo != null ? ", \"motivo\": \"" + motivo + "\"" : "")));
    }

    private ResultActions faixa(long inicio, long fim) throws Exception {
        return mockMvc.perform(post(AIH + "faixas").contentType(MediaType.APPLICATION_JSON).content("""
                {"ano": %d, "inicio": %d, "fim": %d}
                """.formatted(ANO, inicio, fim)));
    }
}
