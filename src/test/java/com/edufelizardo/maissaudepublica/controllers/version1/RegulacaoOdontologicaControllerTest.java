package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.models.Atendimento;
import com.edufelizardo.maissaudepublica.models.Paciente;
import com.edufelizardo.maissaudepublica.models.Profissional;
import com.edufelizardo.maissaudepublica.models.UnidadeDeSaude;
import com.edufelizardo.maissaudepublica.models.enuns.StatusAtendimento;
import com.edufelizardo.maissaudepublica.models.enuns.TipoAtendimento;
import com.edufelizardo.maissaudepublica.models.enuns.TipoUnidadeDeSaude;
import com.edufelizardo.maissaudepublica.repositories.AtendimentoRepository;
import com.edufelizardo.maissaudepublica.repositories.EspecialidadeRepository;
import com.edufelizardo.maissaudepublica.repositories.PacienteRepository;
import com.edufelizardo.maissaudepublica.repositories.ProfissionalRepository;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Encaminhamento odontológico pela Regulação, fatia SB4a (ADR-0138): o item regulado exige a capacidade da executante
 * (o CEO é a unidade com as capacidades odontológicas), a solicitação feita a partir do item do plano, a volta do item
 * a planejado na negativa e a contrarreferência no plano.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class RegulacaoOdontologicaControllerTest {

    private static final String PREFIXO = "RegOdonto Teste ";
    private static final String URL = "/api/v1/";
    private static final String REGULACAO = URL + "solicitacao-regulacao/";

    @Autowired private MockMvc mockMvc;
    @Autowired private JdbcTemplate jdbc;
    @Autowired private UnidadeDeSaudeRepository unidadeDeSaudeRepository;
    @Autowired private PacienteRepository pacienteRepository;
    @Autowired private ProfissionalRepository profissionalRepository;
    @Autowired private AtendimentoRepository atendimentoRepository;
    @Autowired private EspecialidadeRepository especialidadeRepository;

    private UnidadeDeSaude ubs;
    private UnidadeDeSaude ceo;
    private UnidadeDeSaude policlinica;
    private Paciente paciente;
    private Paciente outro;
    private Atendimento atendimento;

    @BeforeEach
    void seed() {
        limpar();
        ubs = unidade("UBS", TipoUnidadeDeSaude.UBS);
        ceo = unidade("CEO", TipoUnidadeDeSaude.CENTRO_ESPECIALIDADES);
        policlinica = unidade("Policlínica", TipoUnidadeDeSaude.POLICLINICA);
        jdbc.update("insert into tb_unidade_capacidade_assistencial (uuid, unidade_id, capacidade_assistencial_id, inicio) select gen_random_uuid(), ?, uuid, ? "
                + "from tb_capacidade_assistencial where codigo = 'ODONTO_ENDODONTIA'", ceo.getUuid(), LocalDate.now().minusDays(1));
        paciente = paciente("Paciente", "90011100001");
        outro = paciente("Outro", "90011100002");
        Profissional dentista = profissional("RO-DENTISTA", "90011100009");
        profissional("RO-REGULADOR", "90011100008");
        profissional("RO-ENDO", "90011100007");
        atendimento = new Atendimento(paciente, dentista, ubs, null, null, TipoAtendimento.CONSULTA, StatusAtendimento.EM_ANDAMENTO, LocalDateTime.now());
        atendimento.setEspecialidade(especialidadeRepository.findAll().stream().filter(e -> e.getCodigo().equals("ODONTOLOGIA")).findFirst().orElseThrow());
        atendimento = atendimentoRepository.save(atendimento);
    }

    @AfterEach
    void limpar() {
        String pacientes = "(select uuid from tb_paciente where nome like '" + PREFIXO + "%')";
        String planos = "(select uuid from tb_plano_tratamento where paciente_id in " + pacientes + ")";
        String solicitacoes = "(select uuid from tb_solicitacao_regulacao where paciente_id in " + pacientes + ")";
        jdbc.update("delete from tb_item_plano_tratamento_face where item_id in (select uuid from tb_item_plano_tratamento where plano_id in " + planos + ")");
        jdbc.update("delete from tb_item_plano_tratamento where plano_id in " + planos);
        jdbc.update("delete from tb_plano_tratamento where paciente_id in " + pacientes);
        jdbc.update("delete from tb_evento_regulacao where solicitacao_id in " + solicitacoes);
        jdbc.update("delete from tb_solicitacao_regulacao where paciente_id in " + pacientes);
        jdbc.update("delete from tb_agendamento where paciente_id in " + pacientes);
        jdbc.update("delete from tb_atendimento where paciente_id in " + pacientes);
        jdbc.update("delete from tb_procedimento_regulado where nome like '" + PREFIXO + "%'");
        jdbc.update("delete from tb_unidade_capacidade_assistencial where unidade_id in (select uuid from tb_unidade_de_saude where nome like '" + PREFIXO + "%')");
        jdbc.update("delete from tb_paciente where nome like '" + PREFIXO + "%'");
        jdbc.update("delete from tb_profissional where matricula like 'RO-%'");
        jdbc.update("delete from tb_unidade_de_saude where nome like '" + PREFIXO + "%'");
    }

    private UnidadeDeSaude unidade(String nome, TipoUnidadeDeSaude tipo) {
        UnidadeDeSaude u = new UnidadeDeSaude();
        u.setNome(PREFIXO + nome);
        u.setTipo(tipo);
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

    private Profissional profissional(String matricula, String cpf) {
        Profissional p = new Profissional();
        p.setMatricula(matricula);
        p.setCpf(cpf);
        p.setNome(PREFIXO + matricula);
        p.setAtivo(true);
        return profissionalRepository.save(p);
    }

    private String enviar(MockHttpServletRequestBuilder r, String corpo, int esperado) throws Exception {
        return mockMvc.perform(r.contentType(MediaType.APPLICATION_JSON).content(corpo)).andExpect(status().is(esperado))
                .andReturn().getResponse().getContentAsString();
    }

    private String ler(String url) throws Exception {
        return mockMvc.perform(get(url)).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
    }

    private String regulado(String nome) {
        return jdbc.queryForObject("select cast(uuid as varchar) from tb_procedimento_regulado where nome = ?", String.class, nome);
    }

    private String solicitar(Paciente p, String procedimento, String item, int esperado) throws Exception {
        enviar(post(REGULACAO), ("{\"pacienteId\": \"%s\", \"procedimentoId\": \"%s\", \"unidadeSolicitanteId\": \"%s\", \"profissionalMatricula\": "
                + "\"RO-DENTISTA\", \"cid\": \"K04.0\", \"justificativa\": \"Pulpite irreversível no 46.\", \"prioridade\": \"AMARELO\"%s}")
                .formatted(p.getUuid(), regulado(procedimento), ubs.getUuid(), item == null ? "" : ", \"itemPlanoTratamentoId\": \"" + item + "\""), esperado);
        return esperado != 201 ? null : jdbc.queryForObject("select cast(uuid as varchar) from tb_solicitacao_regulacao where paciente_id = ? "
                + "order by solicitado_em desc limit 1", String.class, p.getUuid());
    }

    private String autorizacao(UnidadeDeSaude executante, String executor) {
        return ("{\"profissionalMatricula\": \"RO-REGULADOR\", \"unidadeExecutanteId\": \"%s\", \"dataHoraPrevista\": \"%s\"%s}").formatted(
                executante.getUuid(), LocalDateTime.now().plusDays(3).truncatedTo(ChronoUnit.MINUTES),
                executor == null ? "" : ", \"profissionalExecutanteMatricula\": \"" + executor + "\"");
    }

    private String planoComPulpotomia() throws Exception {
        String item = jdbc.queryForObject("select cast(uuid as varchar) from tb_procedimento_catalogo where codigo = 'PULPOTOMIA'", String.class);
        String r = enviar(post(URL + "plano-tratamento"), ("{\"atendimentoId\": \"%s\", \"profissionalMatricula\": \"RO-DENTISTA\", "
                + "\"prioridade\": \"PRIORITARIA\", \"itens\": [{\"procedimentoCatalogoId\": \"%s\", \"dente\": 46}]}").formatted(atendimento.getUuid(), item), 201);
        return JsonPath.read(r, "$.itens[0].uuid");
    }

    private String itemNoPlano(String campo) throws Exception {
        return JsonPath.read(ler(URL + "paciente/" + paciente.getUuid() + "/planos-tratamento"), "$[0].itens[0]." + campo);
    }

    @Test
    void catalogoOdontologicoComCapacidadeExigida() throws Exception {
        String lista = ler(URL + "procedimento-regulado/");
        assertThat((List<String>) JsonPath.read(lista, "$[?(@.nome == 'Endodontia (CEO)')].capacidadeExigidaNome")).containsExactly("Endodontia");
        assertThat((List<String>) JsonPath.read(lista, "$[?(@.nome == 'Prótese dentária')].tipo")).containsExactly("CONSULTA_ESPECIALIZADA");
        String capacidade = jdbc.queryForObject("select cast(uuid as varchar) from tb_capacidade_assistencial where codigo = 'ODONTO_PERIODONTIA'", String.class);
        enviar(post(URL + "procedimento-regulado/"), "{\"nome\": \"%sX\", \"tipo\": \"PROCEDIMENTO\", \"capacidadeExigidaId\": \"%s\"}"
                .formatted(PREFIXO, UUID.randomUUID()), 404);
        enviar(post(URL + "procedimento-regulado/"), "{\"nome\": \"%sRaspagem subgengival\", \"tipo\": \"PROCEDIMENTO\", \"capacidadeExigidaId\": \"%s\"}"
                .formatted(PREFIXO, capacidade), 201);
        assertThat((List<String>) JsonPath.read(ler(URL + "procedimento-regulado/"), "$[?(@.nome == '" + PREFIXO + "Raspagem subgengival')].capacidadeExigidaNome"))
                .containsExactly("Periodontia");
    }

    @Test
    void executanteSemACapacidadeNaoRecebe() throws Exception {
        String s = solicitar(paciente, "Endodontia (CEO)", null, 201);
        enviar(post(REGULACAO + s + "/autorizacao"), autorizacao(policlinica, null), 422);
        enviar(post(REGULACAO + s + "/autorizacao"), autorizacao(ceo, null), 200);
    }

    @Test
    void encaminharDoPlanoNegativaEContrarreferencia() throws Exception {
        String item = planoComPulpotomia();
        // O item é deste paciente; o de outro não entra.
        solicitar(outro, "Endodontia (CEO)", item, 422);
        String s = solicitar(paciente, "Endodontia (CEO)", item, 201);
        assertThat(itemNoPlano("situacao")).isEqualTo("ENCAMINHADO");
        assertThat(itemNoPlano("regulacaoProcedimentoNome")).isEqualTo("Endodontia (CEO)");
        assertThat(itemNoPlano("regulacaoStatus")).isEqualTo("SOLICITADA");
        // Encaminhado não se encaminha de novo.
        solicitar(paciente, "Periodontia (CEO)", item, 422);

        // A negativa devolve o item a planejado, com o motivo.
        enviar(post(REGULACAO + s + "/negativa"), "{\"profissionalMatricula\": \"RO-REGULADOR\", \"motivo\": \"Tratar na APS\"}", 200);
        assertThat(itemNoPlano("situacao")).isEqualTo("PLANEJADO");
        assertThat(itemNoPlano("motivoSituacao")).isEqualTo("Regulação negada: Tratar na APS");

        // Encaminhado de novo, autorizado no CEO, agendado e realizado: a contrarreferência aparece no plano.
        String s2 = solicitar(paciente, "Endodontia (CEO)", item, 201);
        enviar(post(REGULACAO + s2 + "/autorizacao"), autorizacao(ceo, "RO-ENDO"), 200);
        enviar(post(REGULACAO + s2 + "/realizacao"), "{\"profissionalMatricula\": \"RO-ENDO\", \"contrarreferencia\": \"Canal tratado no 46; restaurar na UBS.\"}", 200);
        assertThat(itemNoPlano("regulacaoStatus")).isEqualTo("REALIZADA");
        assertThat(itemNoPlano("regulacaoExecutanteNome")).isEqualTo(PREFIXO + "CEO");
        assertThat(itemNoPlano("contrarreferencia")).isEqualTo("Canal tratado no 46; restaurar na UBS.");
        assertThat(itemNoPlano("situacao")).isEqualTo("ENCAMINHADO");
    }
}
