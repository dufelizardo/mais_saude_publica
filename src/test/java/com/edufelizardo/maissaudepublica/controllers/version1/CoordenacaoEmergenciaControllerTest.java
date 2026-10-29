package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.models.Profissional;
import com.edufelizardo.maissaudepublica.models.UnidadeDeSaude;
import com.edufelizardo.maissaudepublica.models.enuns.TipoUnidadeDeSaude;
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

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Emergências, fatia E3 (ADR-0158): as ações (geradas do plano sem repetir, avulsas, atrasadas, situações), a mobilização
 * (a exceção da Escala para o profissional mobilizado e o fim dela; a ligação com a transferência da Farmácia) e a
 * capacidade emergencial (cadastro, limite, ajuste e desativação no encerramento).
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CoordenacaoEmergenciaControllerTest {

    private static final String PREFIXO = "Coord Teste ";

    @Autowired private MockMvc mockMvc;
    @Autowired private JdbcTemplate jdbc;
    @Autowired private UnidadeDeSaudeRepository unidadeDeSaudeRepository;
    @Autowired private ProfissionalRepository profissionalRepository;

    private String inundacao;
    private String nivel1;
    private UnidadeDeSaude hospital;
    private UnidadeDeSaude ubs;
    private final LocalDate hoje = LocalDate.now();

    @BeforeEach
    void seed() {
        limpar();
        inundacao = jdbc.queryForObject("select cast(uuid as varchar) from tb_tipo_emergencia where codigo = 'INUNDACAO'", String.class);
        nivel1 = jdbc.queryForObject("select cast(uuid as varchar) from tb_nivel_emergencia where ordem = 1", String.class);
        hospital = unidade("Hospital");
        ubs = unidade("UBS Norte");
    }

    @AfterEach
    void limpar() {
        String emergencias = "(select e.uuid from tb_emergencia e join tb_evento_emergencial v on v.uuid = e.evento_id where v.descricao like '" + PREFIXO + "%')";
        String unidades = "(select uuid from tb_unidade_de_saude where nome like '" + PREFIXO + "%')";
        jdbc.update("delete from tb_turno_escala where unidade_id in " + unidades);
        jdbc.update("delete from tb_mobilizacao where emergencia_id in " + emergencias);
        jdbc.update("delete from tb_transferencia_farmacia where unidade_destino_id in " + unidades);
        jdbc.update("delete from tb_lote where unidade_id in " + unidades);
        jdbc.update("delete from tb_medicamento where nome like '" + PREFIXO + "%'");
        jdbc.update("delete from tb_ativacao_capacidade where emergencia_id in " + emergencias);
        jdbc.update("delete from tb_capacidade_emergencial where unidade_id in " + unidades);
        jdbc.update("delete from tb_acao_emergencial where emergencia_id in " + emergencias);
        jdbc.update("delete from tb_registro_ativacao_plano where ativacao_id in (select uuid from tb_ativacao_plano where emergencia_id in " + emergencias + ")");
        jdbc.update("delete from tb_ativacao_plano where emergencia_id in " + emergencias);
        jdbc.update("delete from tb_mudanca_nivel_emergencia where emergencia_id in " + emergencias);
        jdbc.update("delete from tb_emergencia where uuid in " + emergencias);
        jdbc.update("delete from tb_evento_emergencial where descricao like '" + PREFIXO + "%'");
        String planos = "(select uuid from tb_plano_contingencia where codigo like 'CRD_%')";
        jdbc.update("delete from tb_acao_plano where plano_id in " + planos);
        jdbc.update("delete from tb_plano_contingencia_nivel where plano_id in " + planos);
        jdbc.update("delete from tb_plano_contingencia where codigo like 'CRD_%'");
        jdbc.update("delete from tb_profissional where matricula like 'CRD-%'");
        jdbc.update("delete from tb_unidade_de_saude where nome like '" + PREFIXO + "%'");
    }

    private UnidadeDeSaude unidade(String nome) {
        UnidadeDeSaude u = new UnidadeDeSaude();
        u.setNome(PREFIXO + nome);
        u.setTipo(TipoUnidadeDeSaude.UBS);
        u.setAtivo(true);
        return unidadeDeSaudeRepository.save(u);
    }

    private String enviar(MockHttpServletRequestBuilder r, String corpo, int esperado) throws Exception {
        return mockMvc.perform(r.contentType(MediaType.APPLICATION_JSON).content(corpo)).andExpect(status().is(esperado)).andReturn().getResponse()
                .getContentAsString(StandardCharsets.UTF_8);
    }

    private String emergencia() throws Exception {
        String evento = JsonPath.read(enviar(post("/api/v1/evento-emergencial"), "{\"tipoId\": \"" + inundacao + "\", \"fonte\": \"DEFESA_CIVIL\", "
                + "\"descricao\": \"" + PREFIXO + "Rio transbordou\"}", 201), "$.uuid");
        return JsonPath.read(enviar(post("/api/v1/evento-emergencial/" + evento + "/emergencia"), "{\"nome\": \"Enchente Coord\", \"nivelId\": \"" + nivel1
                + "\", \"justificativa\": \"Cota de alerta\"}", 201), "$.uuid");
    }

    @Test
    void acoesDoPlanoEAvulsas() throws Exception {
        String em = emergencia();
        String plano = JsonPath.read(enviar(post("/api/v1/emergencias/plano"), "{\"codigo\": \"CRD_ENCHENTE\", \"nome\": \"Plano\", \"tipoId\": \"" + inundacao
                + "\", \"objetivo\": \"Responder\", \"vigenciaInicio\": \"" + hoje.minusDays(1) + "\", \"nivelIds\": [\"" + nivel1 + "\"], \"acoes\": ["
                + "{\"nivelId\": \"" + nivel1 + "\", \"descricao\": \"Abrir o COE\", \"responsavel\": \"Vigilância\", \"prazoHoras\": 2},"
                + "{\"nivelId\": \"" + nivel1 + "\", \"descricao\": \"Avisar as UBS\", \"responsavel\": \"Atenção Básica\"}]}", 201), "$.uuid");
        enviar(post("/api/v1/emergencias/plano/" + plano + "/publicacao"), "", 200);
        // Sem plano ativo, nada a gerar.
        enviar(post("/api/v1/emergencia/" + em + "/acoes-do-plano"), "", 422);
        enviar(post("/api/v1/emergencia/" + em + "/plano-ativo"), "{\"planoId\": \"" + plano + "\", \"autoridade\": \"Secretário\", \"justificativa\": \"J\"}", 201);
        String r = enviar(post("/api/v1/emergencia/" + em + "/acoes-do-plano"), "", 201);
        assertThat((List<String>) JsonPath.read(r, "$.acoes[*].descricao")).containsExactlyInAnyOrder("Abrir o COE", "Avisar as UBS");
        assertThat((List<String>) JsonPath.read(r, "$.acoes[*].planoCodigo")).containsOnly("CRD_ENCHENTE");
        List<Map<String, Object>> coe = JsonPath.read(r, "$.acoes[?(@.descricao == 'Abrir o COE')]");
        assertThat(coe.get(0).get("prazo")).isNotNull();
        enviar(post("/api/v1/emergencia/" + em + "/acoes-do-plano"), "", 422);

        // Avulsa já atrasada: vai para o alto e conta na lista.
        r = enviar(post("/api/v1/emergencia/" + em + "/acao"), "{\"descricao\": \"Abrir abrigo\", \"responsavel\": \"Assistência social\", \"prioridade\": "
                + "\"URGENTE\", \"prazo\": \"" + Instant.now().minusSeconds(3600) + "\", \"unidadeId\": \"" + ubs.getUuid() + "\"}", 201);
        assertThat((String) JsonPath.read(r, "$.acoes[0].descricao")).isEqualTo("Abrir abrigo");
        assertThat((Boolean) JsonPath.read(r, "$.acoes[0].atrasada")).isTrue();
        assertThat((Integer) JsonPath.read(r, "$.acoesAtrasadas")).isEqualTo(1);
        List<Integer> naLista = JsonPath.read(mockMvc.perform(get("/api/v1/emergencias?situacao=ATIVA")).andReturn().getResponse()
                .getContentAsString(StandardCharsets.UTF_8), "$[?(@.uuid == '" + em + "')].acoesAtrasadas");
        assertThat(naLista).containsExactly(1);

        String acao = JsonPath.read(r, "$.acoes[0].uuid");
        String base = "/api/v1/emergencia/" + em + "/acao/" + acao + "/situacao";
        enviar(post(base), "{\"situacao\": \"CANCELADA\"}", 400);
        r = enviar(post(base), "{\"situacao\": \"EM_EXECUCAO\", \"observacao\": \"Ginásio liberado\"}", 200);
        assertThat((String) JsonPath.read(r, "$.acoes[0].iniciadaEm")).isNotNull();
        r = enviar(post(base), "{\"situacao\": \"CONCLUIDA\"}", 200);
        assertThat((Integer) JsonPath.read(r, "$.acoesAtrasadas")).isZero();
        enviar(post(base), "{\"situacao\": \"EM_EXECUCAO\"}", 422);
    }

    private String turno(LocalDate dia) {
        return "{\"unidadeId\": \"" + ubs.getUuid() + "\", \"profissionalMatricula\": \"CRD-ENF\", \"tipo\": \"MANHA\", \"data\": \"" + dia
                + "\", \"inicio\": \"07:00\", \"fim\": \"13:00\"}";
    }

    @Test
    void mobilizacaoAutorizaTurnoSemLotacao() throws Exception {
        Profissional p = new Profissional();
        p.setMatricula("CRD-ENF");
        p.setCpf("90055500001");
        p.setNome(PREFIXO + "Enfermeira");
        p.setAtivo(true);
        profissionalRepository.save(p);
        String em = emergencia();
        // Sem lotação na UBS, a Escala recusa.
        enviar(post("/api/v1/escala/turno"), turno(hoje.plusDays(1)), 422);

        String r = enviar(post("/api/v1/emergencia/" + em + "/mobilizacao"), "{\"tipo\": \"PROFISSIONAL\", \"profissionalMatricula\": \"CRD-ENF\", "
                + "\"origemId\": \"" + hospital.getUuid() + "\", \"destinoId\": \"" + ubs.getUuid() + "\", \"fim\": \"" + hoje.plusDays(10) + "\"}", 201);
        assertThat((String) JsonPath.read(r, "$.mobilizacoes[0].situacao")).isEqualTo("EM_ANDAMENTO");
        assertThat((Boolean) JsonPath.read(r, "$.mobilizacoes[0].autorizaEscala")).isTrue();
        enviar(post("/api/v1/emergencia/" + em + "/mobilizacao"), "{\"tipo\": \"PROFISSIONAL\", \"profissionalMatricula\": \"CRD-ENF\", \"destinoId\": \""
                + ubs.getUuid() + "\"}", 409);
        // Mobilizada, o turno entra, marcado; fora do período, não.
        String t = enviar(post("/api/v1/escala/turno"), turno(hoje.plusDays(1)), 201);
        assertThat((String) JsonPath.read(t, "$.mobilizacaoEmergencia")).isEqualTo("Enchente Coord");
        enviar(post("/api/v1/escala/turno"), turno(hoje.plusDays(20)), 422);

        // Concluída a mobilização, a exceção acaba para turnos novos.
        String mob = JsonPath.read(r, "$.mobilizacoes[0].uuid");
        r = enviar(post("/api/v1/emergencia/" + em + "/mobilizacao/" + mob + "/situacao"), "{\"situacao\": \"CONCLUIDA\"}", 200);
        assertThat((Boolean) JsonPath.read(r, "$.mobilizacoes[0].autorizaEscala")).isFalse();
        enviar(post("/api/v1/escala/turno"), turno(hoje.plusDays(2)), 422);

        // Mobilizada de novo, a emergência encerrada também acaba a exceção.
        enviar(post("/api/v1/emergencia/" + em + "/mobilizacao"), "{\"tipo\": \"PROFISSIONAL\", \"profissionalMatricula\": \"CRD-ENF\", \"destinoId\": \""
                + ubs.getUuid() + "\"}", 201);
        enviar(post("/api/v1/escala/turno"), turno(hoje.plusDays(3)), 201);
        enviar(post("/api/v1/emergencia/" + em + "/encerramento"), "{\"motivo\": \"Águas baixaram\"}", 200);
        enviar(post("/api/v1/escala/turno"), turno(hoje.plusDays(4)), 422);
    }

    @Test
    void medicamentoSegueATransferencia() throws Exception {
        String em = emergencia();
        enviar(post("/api/v1/emergencia/" + em + "/mobilizacao"), "{\"tipo\": \"INSUMO\", \"destinoId\": \"" + ubs.getUuid() + "\"}", 400);
        String r = enviar(post("/api/v1/emergencia/" + em + "/mobilizacao"), "{\"tipo\": \"MEDICAMENTO\", \"recurso\": \"Soro de reidratação oral\", "
                + "\"quantidade\": 500, \"destinoId\": \"" + ubs.getUuid() + "\"}", 201);
        assertThat((String) JsonPath.read(r, "$.mobilizacoes[0].situacao")).isEqualTo("SOLICITADA");
        String mob = JsonPath.read(r, "$.mobilizacoes[0].uuid");
        String medicamento = jdbc.queryForObject("insert into tb_medicamento (uuid, nome, ativo) values (gen_random_uuid(), ?, true) returning cast(uuid as varchar)",
                String.class, PREFIXO + "Soro");
        String lote = jdbc.queryForObject("insert into tb_lote (uuid, numero_lote, quantidade, validade, medicamento_id, unidade_id) values (gen_random_uuid(), "
                + "'L1', 1000, ?, cast(? as uuid), ?) returning cast(uuid as varchar)", String.class, hoje.plusYears(1), medicamento, hospital.getUuid());
        String profissional = jdbc.queryForObject("insert into tb_profissional (uuid, matricula, cpf, nome, ativo) values (gen_random_uuid(), 'CRD-FAR', "
                + "'90055500002', ?, true) returning cast(uuid as varchar)", String.class, PREFIXO + "Farmacêutico");
        String paraOutra = jdbc.queryForObject("insert into tb_transferencia_farmacia (uuid, quantidade, registrado_em, lote_origem_id, profissional_id, "
                + "unidade_destino_id, status) values (gen_random_uuid(), 500, now(), cast(? as uuid), cast(? as uuid), ?, 'EM_TRANSITO') returning cast(uuid as varchar)",
                String.class, lote, profissional, hospital.getUuid());
        String transferencia = jdbc.queryForObject("insert into tb_transferencia_farmacia (uuid, quantidade, registrado_em, lote_origem_id, profissional_id, "
                + "unidade_destino_id, status) values (gen_random_uuid(), 500, now(), cast(? as uuid), cast(? as uuid), ?, 'EM_TRANSITO') returning cast(uuid as varchar)",
                String.class, lote, profissional, ubs.getUuid());
        String ligar = "/api/v1/emergencia/" + em + "/mobilizacao/" + mob + "/transferencia";
        enviar(put(ligar), "{\"transferenciaId\": \"" + paraOutra + "\"}", 422);
        r = enviar(put(ligar), "{\"transferenciaId\": \"" + transferencia + "\"}", 200);
        assertThat((String) JsonPath.read(r, "$.mobilizacoes[0].situacao")).isEqualTo("EM_ANDAMENTO");
        enviar(post("/api/v1/emergencia/" + em + "/mobilizacao/" + mob + "/situacao"), "{\"situacao\": \"CONCLUIDA\"}", 422);
        jdbc.update("update tb_transferencia_farmacia set status = 'RECEBIDA' where uuid = cast(? as uuid)", transferencia);
        r = mockMvc.perform(get("/api/v1/emergencia/" + em)).andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        assertThat((String) JsonPath.read(r, "$.mobilizacoes[0].situacao")).isEqualTo("CONCLUIDA");
        assertThat((String) JsonPath.read(r, "$.mobilizacoes[0].transferenciaStatus")).isEqualTo("RECEBIDA");
    }

    @Test
    void capacidadeEmergencial() throws Exception {
        String corpo = "{\"unidadeId\": \"" + hospital.getUuid() + "\", \"recurso\": \"LEITOS\", \"normal\": 120, \"emergencial\": 30}";
        String c = JsonPath.read(enviar(post("/api/v1/emergencias/capacidade"), corpo, 201), "$.uuid");
        enviar(post("/api/v1/emergencias/capacidade"), corpo, 409);
        String em = emergencia();
        String r = enviar(post("/api/v1/emergencia/" + em + "/capacidade-ativa"), "{\"capacidadeId\": \"" + c + "\", \"quantidade\": 10, \"justificativa\": "
                + "\"Ala de observação\"}", 201);
        assertThat((Integer) JsonPath.read(r, "$.capacidades[0].quantidade")).isEqualTo(10);
        enviar(post("/api/v1/emergencia/" + em + "/capacidade-ativa"), "{\"capacidadeId\": \"" + c + "\", \"quantidade\": 25, \"justificativa\": \"Mais\"}", 422);
        enviar(put("/api/v1/emergencias/capacidade/" + c), corpo.replace("\"emergencial\": 30", "\"emergencial\": 5"), 422);
        List<Integer> ativada = JsonPath.read(mockMvc.perform(get("/api/v1/emergencias/capacidades?unidadeId=" + hospital.getUuid())).andReturn().getResponse()
                .getContentAsString(StandardCharsets.UTF_8), "$[*].ativadaAgora");
        assertThat(ativada).containsExactly(10);

        r = enviar(post("/api/v1/emergencia/" + em + "/encerramento"), "{\"motivo\": \"Fim\"}", 200);
        assertThat((String) JsonPath.read(r, "$.capacidades[0].situacao")).isEqualTo("DESATIVADA");
        enviar(post("/api/v1/emergencia/" + em + "/capacidade-ativa"), "{\"capacidadeId\": \"" + c + "\", \"quantidade\": 1, \"justificativa\": \"J\"}", 422);
    }
}
