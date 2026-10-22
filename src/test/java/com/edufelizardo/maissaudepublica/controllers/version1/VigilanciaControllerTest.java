package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.models.Internacao;
import com.edufelizardo.maissaudepublica.models.Leito;
import com.edufelizardo.maissaudepublica.models.Paciente;
import com.edufelizardo.maissaudepublica.models.Profissional;
import com.edufelizardo.maissaudepublica.models.Setor;
import com.edufelizardo.maissaudepublica.models.UnidadeDeSaude;
import com.edufelizardo.maissaudepublica.models.enuns.CaraterInternacao;
import com.edufelizardo.maissaudepublica.models.enuns.SexoLeito;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoLeito;
import com.edufelizardo.maissaudepublica.models.enuns.StatusInternacao;
import com.edufelizardo.maissaudepublica.models.enuns.TipoLeito;
import com.edufelizardo.maissaudepublica.models.enuns.TipoSetor;
import com.edufelizardo.maissaudepublica.models.enuns.TipoUnidadeDeSaude;
import com.edufelizardo.maissaudepublica.repositories.AgravoRepository;
import com.edufelizardo.maissaudepublica.repositories.InternacaoRepository;
import com.edufelizardo.maissaudepublica.repositories.LeitoRepository;
import com.edufelizardo.maissaudepublica.repositories.PacienteRepository;
import com.edufelizardo.maissaudepublica.repositories.ProfissionalRepository;
import com.edufelizardo.maissaudepublica.repositories.SetorRepository;
import com.edufelizardo.maissaudepublica.repositories.UnidadeDeSaudeRepository;
import com.edufelizardo.maissaudepublica.services.version1.SemanaEpidemiologica;
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

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Vigilância epidemiológica, fatia V1 (ADR-0124): ficha versionada e validação das respostas; o ciclo notificar,
 * investigar, classificar e encerrar; a notificação negativa; as sugestões a partir da internação e da crise, e a ponte
 * com a crise da SM3.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class VigilanciaControllerTest {

    static final String PREFIXO = "Vigilancia Teste ";
    private static final String CODIGO = "TESTE_V1_";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private UnidadeDeSaudeRepository unidadeDeSaudeRepository;

    @Autowired
    private PacienteRepository pacienteRepository;

    @Autowired
    private AgravoRepository agravoRepository;

    @Autowired
    private ProfissionalRepository profissionalRepository;

    @Autowired
    private SetorRepository setorRepository;

    @Autowired
    private LeitoRepository leitoRepository;

    @Autowired
    private InternacaoRepository internacaoRepository;

    private UnidadeDeSaude ubs;
    private UnidadeDeSaude outra;
    private Paciente maria;
    private Paciente jose;
    private String dengue;
    private String violencia;

    @BeforeEach
    void seed() {
        limpar();
        ubs = unidade("UBS");
        outra = unidade("UBS Sul");
        maria = paciente("Maria", "90011100011");
        jose = paciente("José", "90011100012");
        dengue = agravoRepository.findByCodigo("DENGUE").orElseThrow().getUuid().toString();
        violencia = agravoRepository.findByCodigo("VIOLENCIA_INTERPESSOAL_AUTOPROVOCADA").orElseThrow().getUuid().toString();
    }

    @AfterEach
    void limpar() {
        limparDados(jdbc, PREFIXO, CODIGO);
    }

    /** Apaga o que os testes de vigilância criaram, pelas unidades e pelos agravos do prefixo. */
    static void limparDados(JdbcTemplate jdbc, String prefixo, String codigo) {
        limparSurtos(jdbc, prefixo, codigo);
        String unidades = "(select uuid from tb_unidade_de_saude where nome like '" + prefixo + "%')";
        String notificacoes = "(select uuid from tb_notificacao where unidade_id in " + unidades + ")";
        // Contatos (ADR-0130): os do caso índice e os que viraram caso.
        jdbc.update("delete from tb_contato_epidemiologico where notificacao_id in " + notificacoes + " or notificacao_gerada_id in " + notificacoes);
        jdbc.update("delete from tb_classificacao_caso where notificacao_id in " + notificacoes);
        jdbc.update("delete from tb_investigacao_exame where investigacao_id in (select uuid from tb_investigacao_epidemiologica where notificacao_id in "
                + notificacoes + ")");
        jdbc.update("delete from tb_investigacao_epidemiologica where notificacao_id in " + notificacoes);
        jdbc.update("delete from tb_notificacao where unidade_id in " + unidades);
        jdbc.update("delete from tb_notificacao_negativa where unidade_id in " + unidades);
        jdbc.update("delete from tb_etapa_crise where crise_id in (select uuid from tb_evento_crise where unidade_id in " + unidades + ")");
        jdbc.update("delete from tb_evento_crise where unidade_id in " + unidades);
        jdbc.update("delete from tb_evento_leito where leito_id in (select uuid from tb_leito where unidade_id in " + unidades + ")");
        jdbc.update("delete from tb_internacao where unidade_id in " + unidades);
        jdbc.update("delete from tb_leito where unidade_id in " + unidades);
        jdbc.update("delete from tb_setor where unidade_id in " + unidades);
        String agravos = "(select uuid from tb_agravo where codigo like '" + codigo + "%')";
        jdbc.update("delete from tb_ficha_agravo where agravo_id in " + agravos);
        jdbc.update("delete from tb_agravo_cid where agravo_id in " + agravos);
        jdbc.update("delete from tb_agravo_criterio where agravo_id in " + agravos);
        jdbc.update("delete from tb_agravo where codigo like '" + codigo + "%'");
        jdbc.update("delete from tb_paciente where nome like '" + prefixo + "%'");
        jdbc.update("delete from tb_profissional where nome like '" + prefixo + "%'");
        jdbc.update("delete from tb_unidade_de_saude where nome like '" + prefixo + "%'");
    }

    /** Surtos e medidas (ADR-0131) das unidades e dos agravos do teste; antes do território (as microáreas do surto). */
    static void limparSurtos(JdbcTemplate jdbc, String prefixo, String codigo) {
        String unidades = "(select uuid from tb_unidade_de_saude where nome like '" + prefixo + "%')";
        String surtos = "(select uuid from tb_surto where unidade_id in " + unidades + " or agravo_id in (select uuid from tb_agravo where codigo like '"
                + codigo + "%'))";
        jdbc.update("delete from tb_medida_controle where surto_id in " + surtos + " or notificacao_id in (select uuid from tb_notificacao where unidade_id in "
                + unidades + ")");
        jdbc.update("update tb_notificacao set surto_id = null where surto_id in " + surtos);
        jdbc.update("delete from tb_surto_microarea where surto_id in " + surtos);
        jdbc.update("delete from tb_surto where uuid in " + surtos);
    }

    private UnidadeDeSaude unidade(String nome) {
        UnidadeDeSaude u = new UnidadeDeSaude();
        u.setNome(PREFIXO + nome);
        u.setTipo(TipoUnidadeDeSaude.UBS);
        u.setAtivo(true);
        return unidadeDeSaudeRepository.save(u);
    }

    private Paciente paciente(String nome, String cpf) {
        Paciente p = new Paciente();
        p.setNome(PREFIXO + nome);
        p.setCpf(cpf);
        p.setAtivo(true);
        return pacienteRepository.save(p);
    }

    private ResultActions postar(String url, String corpo) throws Exception {
        return mockMvc.perform(post("/api/v1/vigilancia/" + url).contentType(MediaType.APPLICATION_JSON).content(corpo));
    }

    private ResultActions notificar(Paciente p, String agravo, String extra) throws Exception {
        return postar("notificacao", "{\"pacienteId\": \"%s\", \"agravoId\": \"%s\", \"unidadeId\": \"%s\", \"origem\": \"ATENDIMENTO\"%s}"
                .formatted(p.getUuid(), agravo, ubs.getUuid(), extra));
    }

    @Test
    void fichaVersionadaEValidacaoDasRespostas() throws Exception {
        String r = postar("agravo", """
                {"codigo": "%sLOCAL", "nome": "Agravo local de teste", "cids": ["U99"], "periodicidade": "SEMANAL",
                 "prazoEncerramentoDias": 30, "criterios": ["CLINICO"]}""".formatted(CODIGO)).andExpect(status().isCreated())
                .andExpect(jsonPath("$.fichaVersao").doesNotExist()).andReturn().getResponse().getContentAsString();
        String agravo = JsonPath.read(r, "$.uuid");
        postar("agravo", "{\"codigo\": \"%sLOCAL\", \"nome\": \"x\", \"periodicidade\": \"SEMANAL\", \"prazoEncerramentoDias\": 30, \"criterios\": [\"CLINICO\"]}"
                .formatted(CODIGO)).andExpect(status().isConflict());
        notificar(maria, agravo, ", \"respostas\": {\"febre\": true}").andExpect(status().isBadRequest());
        postar("agravo/" + agravo + "/ficha", """
                {"campos": [{"chave": "febre", "rotulo": "Febre", "tipo": "SIM_NAO", "obrigatorio": true},
                            {"chave": "local", "rotulo": "Local", "tipo": "OPCAO", "opcoes": ["Casa", "Escola"]}]}""")
                .andExpect(status().isCreated()).andExpect(jsonPath("$.fichaVersao").value(1));
        notificar(maria, agravo, "").andExpect(status().isBadRequest());
        notificar(maria, agravo, ", \"respostas\": {\"febre\": \"sim\"}").andExpect(status().isBadRequest());
        notificar(maria, agravo, ", \"respostas\": {\"febre\": true, \"local\": \"Praia\"}").andExpect(status().isBadRequest());
        notificar(maria, agravo, ", \"respostas\": {\"febre\": true, \"outro\": 1}").andExpect(status().isBadRequest());
        String n = notificar(maria, agravo, ", \"respostas\": {\"febre\": true, \"local\": \"Escola\"}").andExpect(status().isCreated())
                .andExpect(jsonPath("$.fichaVersao").value(1))
                .andExpect(jsonPath("$.situacao").value("NOTIFICADA"))
                .andExpect(jsonPath("$.prazoEncerramento").value(LocalDate.now().plusDays(30).toString()))
                .andReturn().getResponse().getContentAsString();
        postar("agravo/" + agravo + "/ficha", "{\"campos\": [{\"chave\": \"tosse\", \"rotulo\": \"Tosse\", \"tipo\": \"SIM_NAO\", \"obrigatorio\": true}]}")
                .andExpect(status().isCreated()).andExpect(jsonPath("$.fichaVersao").value(2));
        // A notificação antiga continua na versão 1, com a ficha dela.
        mockMvc.perform(get("/api/v1/vigilancia/notificacao/" + JsonPath.read(n, "$.uuid"))).andExpect(status().isOk())
                .andExpect(jsonPath("$.fichaVersao").value(1))
                .andExpect(jsonPath("$.ficha[0].chave").value("febre"))
                .andExpect(jsonPath("$.respostas.local").value("Escola"));
        notificar(maria, agravo, ", \"respostas\": {\"tosse\": true}").andExpect(status().isCreated())
                .andExpect(jsonPath("$.fichaVersao").value(2))
                .andExpect(jsonPath("$.aviso", containsString("outra notificação")));
    }

    @Test
    void cicloNotificarInvestigarClassificarEncerrar() throws Exception {
        String r = notificar(maria, dengue, ", \"dataPrimeirosSintomas\": \"%s\", \"respostas\": {\"febre\": true, \"sinaisAlarme\": [\"letargia\"]}"
                .formatted(LocalDate.now().minusDays(3))).andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        String n = "notificacao/" + JsonPath.read(r, "$.uuid");
        postar(n + "/investigacao", "{\"exposicao\": \"Vizinho com dengue.\", \"localProvavelInfeccao\": \"Bairro Centro\"}")
                .andExpect(status().isOk()).andExpect(jsonPath("$.situacao").value("EM_INVESTIGACAO"))
                .andExpect(jsonPath("$.investigacao.exposicao").value("Vizinho com dengue."));
        postar(n + "/classificacao", "{\"classificacao\": \"CONFIRMADO\"}").andExpect(status().isBadRequest());
        postar(n + "/classificacao", "{\"classificacao\": \"CONFIRMADO\", \"criterio\": \"CLINICO\"}").andExpect(status().isUnprocessableEntity());
        postar(n + "/classificacao", "{\"classificacao\": \"SUSPEITO\"}").andExpect(status().isCreated());
        postar(n + "/encerramento", "{\"evolucao\": \"CURA\"}").andExpect(status().isUnprocessableEntity());
        postar(n + "/classificacao", "{\"classificacao\": \"CONFIRMADO\", \"criterio\": \"LABORATORIAL\", \"justificativa\": \"NS1 reagente.\"}")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.classificacoes.length()").value(2))
                .andExpect(jsonPath("$.classificacaoAtual").value("CONFIRMADO"));
        postar(n + "/encerramento", "{\"evolucao\": \"CURA\"}").andExpect(status().isOk())
                .andExpect(jsonPath("$.situacao").value("ENCERRADA"))
                .andExpect(jsonPath("$.classificacaoFinal").value("CONFIRMADO"));
        postar(n + "/classificacao", "{\"classificacao\": \"DESCARTADO\", \"criterio\": \"LABORATORIAL\"}").andExpect(status().isUnprocessableEntity());
        mockMvc.perform(get("/api/v1/vigilancia/boletim").param("unidadeId", ubs.getUuid().toString())).andExpect(status().isOk())
                .andExpect(jsonPath("$.linhas[0].agravoCodigo").value("DENGUE"))
                .andExpect(jsonPath("$.linhas[0].confirmadas").value(1));
        // Prazo vencido: a data vem do passado direto no banco.
        String outra = notificar(jose, dengue, ", \"respostas\": {\"febre\": false}").andReturn().getResponse().getContentAsString();
        jdbc.update("update tb_notificacao set prazo_encerramento = ? where uuid = ?::uuid", LocalDate.now().minusDays(1), JsonPath.read(outra, "$.uuid"));
        mockMvc.perform(get("/api/v1/vigilancia/notificacoes").param("unidadeId", ubs.getUuid().toString()).param("atrasadas", "true"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.encerramentoAtrasado").value(1)).andExpect(jsonPath("$.lista.length()").value(1));
    }

    @Test
    void notificacaoNegativaSemanal() throws Exception {
        SemanaEpidemiologica anterior = SemanaEpidemiologica.de(LocalDate.now()).anterior();
        postar("notificacao-negativa", "{\"unidadeId\": \"%s\"}".formatted(outra.getUuid())).andExpect(status().isCreated())
                .andExpect(jsonPath("$.semana").value(anterior.semana()))
                .andExpect(jsonPath("$.inicio").value(anterior.inicio().toString()));
        postar("notificacao-negativa", "{\"unidadeId\": \"%s\"}".formatted(outra.getUuid())).andExpect(status().isConflict());
        notificar(maria, dengue, ", \"respostas\": {\"febre\": true}").andExpect(status().isCreated());
        SemanaEpidemiologica atual = SemanaEpidemiologica.de(LocalDate.now());
        postar("notificacao-negativa", "{\"unidadeId\": \"%s\", \"ano\": %d, \"semana\": %d}".formatted(ubs.getUuid(), atual.ano(), atual.semana()))
                .andExpect(status().isUnprocessableEntity());
        postar("notificacao-negativa", "{\"unidadeId\": \"%s\", \"ano\": %d, \"semana\": 60}".formatted(ubs.getUuid(), atual.ano()))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get("/api/v1/vigilancia/semanas").param("unidadeId", outra.getUuid().toString())).andExpect(status().isOk())
                .andExpect(jsonPath("$.declaradas.length()").value(1))
                .andExpect(jsonPath("$.pendentes.length()").value(7));
    }

    @Test
    void sugestoesDaInternacaoEDaCriseComAPonte() throws Exception {
        Profissional medico = new Profissional();
        medico.setMatricula("VIG-MED");
        medico.setCpf("90011100099");
        medico.setNome(PREFIXO + "Médico");
        medico.setAtivo(true);
        medico = profissionalRepository.save(medico);
        Setor setor = setorRepository.save(new Setor(ubs, PREFIXO + "Clínica", "VIG-CL", TipoSetor.ASSISTENCIAL, true, null));
        Leito leito = new Leito();
        leito.setUnidade(ubs);
        leito.setSetor(setor);
        leito.setIdentificacao("VG 01");
        leito.setTipo(TipoLeito.CLINICO);
        leito.setSexo(SexoLeito.MISTO);
        leito.setSituacao(SituacaoLeito.OCUPADO);
        leito.setAtivo(true);
        leito = leitoRepository.save(leito);
        Internacao i = new Internacao();
        i.setPaciente(jose);
        i.setUnidade(ubs);
        i.setLeito(leito);
        i.setMedicoResponsavel(medico);
        i.setCid("A90");
        i.setMotivo("Dengue com sinais de alarme.");
        i.setCarater(CaraterInternacao.URGENCIA);
        i.setAdmitidaEm(Instant.now().minus(Duration.ofDays(2)));
        i.setStatus(StatusInternacao.INTERNADO);
        internacaoRepository.save(i);

        String crise = mockMvc.perform(post("/api/v1/saude-mental/crise").contentType(MediaType.APPLICATION_JSON).content("""
                        {"pacienteId": "%s", "unidadeId": "%s", "tipo": "TENTATIVA_DE_SUICIDIO", "descricao": "Ingestão de medicamentos."}"""
                        .formatted(maria.getUuid(), ubs.getUuid())))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        String criseId = JsonPath.read(crise, "$.uuid");

        mockMvc.perform(get("/api/v1/vigilancia/sugestoes").param("unidadeId", ubs.getUuid().toString())).andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[?(@.origem == 'INTERNACAO')].agravoCodigo").value("DENGUE"))
                .andExpect(jsonPath("$[?(@.origem == 'SAUDE_MENTAL')].referenciaId").value(criseId));

        postar("notificacao", """
                {"pacienteId": "%s", "agravoId": "%s", "unidadeId": "%s", "origem": "SAUDE_MENTAL", "criseId": "%s",
                 "respostas": {"tipoViolencia": "Autoprovocada", "lesaoAutoprovocada": true}}"""
                .formatted(maria.getUuid(), violencia, ubs.getUuid(), criseId)).andExpect(status().isCreated())
                .andExpect(jsonPath("$.criseId").value(criseId));
        mockMvc.perform(get("/api/v1/saude-mental/crise/" + criseId)).andExpect(status().isOk())
                .andExpect(jsonPath("$.notificacaoPendente").value(false));
        postar("notificacao", "{\"pacienteId\": \"%s\", \"agravoId\": \"%s\", \"unidadeId\": \"%s\", \"origem\": \"SAUDE_MENTAL\", \"criseId\": \"%s\"}"
                .formatted(jose.getUuid(), violencia, ubs.getUuid(), criseId)).andExpect(status().isUnprocessableEntity());
        mockMvc.perform(get("/api/v1/vigilancia/sugestoes").param("unidadeId", ubs.getUuid().toString())).andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].origem").value("INTERNACAO"));
    }
}
