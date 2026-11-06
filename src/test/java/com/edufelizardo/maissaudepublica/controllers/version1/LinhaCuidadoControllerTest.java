package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.models.*;
import com.edufelizardo.maissaudepublica.models.enuns.*;
import com.edufelizardo.maissaudepublica.repositories.*;
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
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Programas, fatia P5 (ADR-0152): o catálogo de linhas com as etapas (carga inicial, cadastro, recusas e ajuste); o
 * percurso com cada tipo de etapa cumprido pelo registro do domínio, o prazo do início e da anterior, a quantidade, a
 * dispensa e o encerramento; e o painel com a sugestão de abrir a linha.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class LinhaCuidadoControllerTest {

    private static final String PREFIXO = "Linha Teste ";
    private static final String CODIGO = "LT_P5_";

    @Autowired private MockMvc mockMvc;
    @Autowired private JdbcTemplate jdbc;
    @Autowired private UnidadeDeSaudeRepository unidadeDeSaudeRepository;
    @Autowired private PacienteRepository pacienteRepository;
    @Autowired private ProfissionalRepository profissionalRepository;
    @Autowired private AtendimentoRepository atendimentoRepository;
    @Autowired private ExameLaboratorialRepository exameRepository;
    @Autowired private PedidoExameRepository pedidoRepository;
    @Autowired private ItemPedidoExameRepository itemRepository;
    @Autowired private ResultadoExameRepository resultadoRepository;
    @Autowired private ProcedimentoReguladoRepository procedimentoRepository;
    @Autowired private SolicitacaoRegulacaoRepository solicitacaoRepository;
    @Autowired private SetorRepository setorRepository;
    @Autowired private LeitoRepository leitoRepository;
    @Autowired private InternacaoRepository internacaoRepository;

    private final LocalDate hoje = LocalDate.now();
    private UnidadeDeSaude ubs;
    private Profissional medico;
    private Paciente paciente;

    @BeforeEach
    void seed() {
        limpar();
        ubs = new UnidadeDeSaude();
        ubs.setNome(PREFIXO + "UBS");
        ubs.setTipo(TipoUnidadeDeSaude.UBS);
        ubs.setAtivo(true);
        ubs = unidadeDeSaudeRepository.save(ubs);
        medico = new Profissional();
        medico.setMatricula("LT-P5-MED");
        medico.setCpf("90044400001");
        medico.setNome(PREFIXO + "Médica");
        medico.setAtivo(true);
        medico = profissionalRepository.save(medico);
        Paciente p = new Paciente();
        p.setNome(PREFIXO + "Paciente");
        p.setCpf("90044400002");
        p.setSexo(Sexo.FEMININO);
        p.setDataNascimento(LocalDate.of(1995, 3, 10));
        p.setAtivo(true);
        paciente = pacienteRepository.save(p);
    }

    @AfterEach
    void limpar() {
        String unidades = "(select uuid from tb_unidade_de_saude where nome like '" + PREFIXO + "%')";
        String pacientes = "(select uuid from tb_paciente where nome like '" + PREFIXO + "%')";
        String linhas = "(select uuid from tb_linha_cuidado where codigo like '" + CODIGO + "%')";
        jdbc.update("delete from tb_registro_etapa_percurso where percurso_id in (select uuid from tb_percurso_linha_cuidado where paciente_id in " + pacientes + ")");
        jdbc.update("delete from tb_percurso_linha_cuidado where paciente_id in " + pacientes);
        jdbc.update("delete from tb_etapa_linha_cuidado where linha_id in " + linhas);
        jdbc.update("delete from tb_linha_cuidado where codigo like '" + CODIGO + "%'");
        jdbc.update("delete from tb_acompanhamento_programatico where paciente_id in " + pacientes);
        jdbc.update("delete from tb_registro_vacinacao where paciente_id in " + pacientes);
        jdbc.update("update tb_item_pedido_exame set resultado_atual_id = null where pedido_id in (select uuid from tb_pedido_exame where paciente_id in " + pacientes + ")");
        jdbc.update("delete from tb_resultado_exame where item_id in (select i.uuid from tb_item_pedido_exame i join tb_pedido_exame p on p.uuid = i.pedido_id "
                + "where p.paciente_id in " + pacientes + ")");
        jdbc.update("delete from tb_item_pedido_exame where pedido_id in (select uuid from tb_pedido_exame where paciente_id in " + pacientes + ")");
        jdbc.update("delete from tb_pedido_exame where paciente_id in " + pacientes);
        jdbc.update("delete from tb_exame_laboratorial where nome like '" + PREFIXO + "%'");
        jdbc.update("delete from tb_solicitacao_regulacao where paciente_id in " + pacientes);
        jdbc.update("delete from tb_procedimento_regulado where nome like '" + PREFIXO + "%'");
        jdbc.update("delete from tb_internacao where unidade_id in " + unidades);
        jdbc.update("delete from tb_leito where unidade_id in " + unidades);
        jdbc.update("delete from tb_setor where unidade_id in " + unidades);
        jdbc.update("delete from tb_atendimento where paciente_id in " + pacientes);
        jdbc.update("delete from tb_paciente where nome like '" + PREFIXO + "%'");
        jdbc.update("delete from tb_profissional where matricula = 'LT-P5-MED'");
        jdbc.update("delete from tb_unidade_de_saude where nome like '" + PREFIXO + "%'");
    }

    private String enviar(MockHttpServletRequestBuilder r, String corpo, int esperado) throws Exception {
        return mockMvc.perform(r.contentType(MediaType.APPLICATION_JSON).content(corpo)).andExpect(status().is(esperado)).andReturn().getResponse()
                .getContentAsString(StandardCharsets.UTF_8);
    }

    private String ler(String url) throws Exception {
        return mockMvc.perform(get(url)).andExpect(status().isOk()).andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
    }

    private String id(String sql) {
        return jdbc.queryForObject(sql, String.class);
    }

    private static Map<String, Object> etapa(String percurso, int ordem) {
        List<Map<String, Object>> e = JsonPath.read(percurso, "$.etapas[?(@.ordem == " + ordem + ")]");
        assertThat(e).hasSize(1);
        return e.get(0);
    }

    private Instant dia(int diasAtras) {
        return hoje.minusDays(diasAtras).atTime(LocalTime.NOON).atZone(ZoneId.systemDefault()).toInstant();
    }

    /** Uma linha com um exemplo de cada tipo de etapa; devolve o JSON criado. */
    private String linhaCompleta(ExameLaboratorial exame, ProcedimentoRegulado procedimento) throws Exception {
        String enfermagem = id("select cast(uuid as varchar) from tb_especialidade where codigo = 'ENFERMAGEM'");
        String bcg = id("select cast(uuid as varchar) from tb_imunobiologico where codigo = 'BCG'");
        return enviar(post("/api/v1/linha-de-cuidado"), """
                {"codigo": "%sCOMPLETA", "nome": "%sCompleta", "etapas": [
                  {"nome": "Consulta de enfermagem", "tipo": "ATENDIMENTO", "especialidadeId": "%s", "prazoDias": 30},
                  {"nome": "Exame", "tipo": "EXAME", "exameId": "%s", "prazoDias": 60},
                  {"nome": "Consulta especializada", "tipo": "REGULACAO", "procedimentoId": "%s", "prazoDias": 90},
                  {"nome": "BCG", "tipo": "VACINA", "imunobiologicoId": "%s", "prazoDias": 10},
                  {"nome": "Parto", "tipo": "INTERNACAO", "cids": "o80, O81.0", "prazoDias": 300},
                  {"nome": "Duas visitas", "tipo": "MANUAL", "quantidade": 2, "prazoDias": 15, "prazoDesde": "ETAPA_ANTERIOR"},
                  {"nome": "Orientação", "tipo": "MANUAL", "obrigatoria": false}
                ]}""".formatted(CODIGO, PREFIXO, enfermagem, exame.getUuid(), procedimento.getUuid(), bcg), 201);
    }

    private ExameLaboratorial exame() {
        ExameLaboratorial e = new ExameLaboratorial();
        e.setNome(PREFIXO + "Hemograma");
        e.setMaterial(MaterialExame.SANGUE);
        e.setTipoResultado(TipoResultadoExame.TEXTO);
        e.setAtivo(true);
        return exameRepository.save(e);
    }

    private ProcedimentoRegulado procedimento() {
        ProcedimentoRegulado p = new ProcedimentoRegulado();
        p.setNome(PREFIXO + "Mastologia");
        p.setTipo(TipoProcedimentoRegulado.CONSULTA_ESPECIALIZADA);
        p.setAtivo(true);
        return procedimentoRepository.save(p);
    }

    @Test
    void catalogoCadastroRecusasEAjuste() throws Exception {
        String todas = ler("/api/v1/linhas-de-cuidado");
        assertThat((List<Integer>) JsonPath.read(todas, "$[?(@.codigo == 'GESTANTE')].etapas.length()")).containsExactly(6);
        assertThat((List<String>) JsonPath.read(todas, "$[?(@.codigo == 'GESTANTE')].programaNome")).containsExactly("Pré-natal");

        String criada = linhaCompleta(exame(), procedimento());
        String linha = JsonPath.read(criada, "$.uuid");
        assertThat((String) JsonPath.read(criada, "$.etapas[4].cids")).isEqualTo("O80,O810");
        assertThat((String) JsonPath.read(criada, "$.etapas[5].criterio")).contains("2 × Registro do profissional", "da etapa anterior");

        // Recusas: código repetido, exame sem o exame, primeira etapa contada da anterior.
        enviar(post("/api/v1/linha-de-cuidado"), "{\"codigo\": \"%sCOMPLETA\", \"nome\": \"Outra\", \"etapas\": [{\"nome\": \"X\", \"tipo\": \"MANUAL\"}]}"
                .formatted(CODIGO), 409);
        enviar(post("/api/v1/linha-de-cuidado"), "{\"codigo\": \"%sB\", \"nome\": \"B\", \"etapas\": [{\"nome\": \"X\", \"tipo\": \"EXAME\"}]}"
                .formatted(CODIGO), 400);
        enviar(post("/api/v1/linha-de-cuidado"), "{\"codigo\": \"%sC\", \"nome\": \"C\", \"etapas\": [{\"nome\": \"X\", \"tipo\": \"MANUAL\", "
                + "\"prazoDesde\": \"ETAPA_ANTERIOR\"}]}".formatted(CODIGO), 400);

        // Ajuste: a etapa que não vem é desativada; a mantida guarda o uuid.
        String primeira = JsonPath.read(criada, "$.etapas[0].uuid");
        String ajustada = enviar(put("/api/v1/linha-de-cuidado/" + linha), """
                {"codigo": "%sCOMPLETA", "nome": "%sCompleta", "etapas": [
                  {"uuid": "%s", "nome": "Consulta", "tipo": "ATENDIMENTO", "prazoDias": 20},
                  {"nome": "Visita", "tipo": "MANUAL"}
                ]}""".formatted(CODIGO, PREFIXO, primeira), 200);
        assertThat((List<String>) JsonPath.read(ajustada, "$.etapas[*].nome")).containsExactly("Consulta", "Visita");
        assertThat((String) JsonPath.read(ajustada, "$.etapas[0].uuid")).isEqualTo(primeira);
        assertThat(jdbc.queryForObject("select count(*) from tb_etapa_linha_cuidado where linha_id = cast(? as uuid) and not ativa", Integer.class, linha))
                .isEqualTo(6);
    }

    @Test
    void percursoCumpridoPelosRegistrosDosDominios() throws Exception {
        ExameLaboratorial exame = exame();
        ProcedimentoRegulado procedimento = procedimento();
        String linha = JsonPath.read(linhaCompleta(exame, procedimento), "$.uuid");
        LocalDate inicio = hoje.minusDays(40);

        // Antes do início não conta; a consulta de enfermagem depois dele cumpre a primeira etapa.
        Especialidade enfermagem = new Especialidade();
        enfermagem.setUuid(java.util.UUID.fromString(id("select cast(uuid as varchar) from tb_especialidade where codigo = 'ENFERMAGEM'")));
        Atendimento antes = new Atendimento(paciente, medico, ubs, null, null, TipoAtendimento.CONSULTA, StatusAtendimento.CONCLUIDO,
                LocalDateTime.now().minusDays(50));
        antes.setEspecialidade(enfermagem);
        atendimentoRepository.save(antes);
        Atendimento depois = new Atendimento(paciente, medico, ubs, null, null, TipoAtendimento.CONSULTA, StatusAtendimento.CONCLUIDO,
                LocalDateTime.now().minusDays(35));
        depois.setEspecialidade(enfermagem);
        atendimentoRepository.save(depois);
        // O exame liberado.
        PedidoExame pedido = new PedidoExame();
        pedido.setPaciente(paciente);
        pedido.setUnidadeSolicitante(ubs);
        pedido.setProfissionalSolicitante(medico);
        pedido.setIndicacaoClinica("Rotina");
        pedido.setPrioridade(PrioridadeExame.ROTINA);
        pedido.setSolicitadoEm(dia(25));
        pedido = pedidoRepository.save(pedido);
        ItemPedidoExame item = new ItemPedidoExame();
        item.setPedido(pedido);
        item.setExame(exame);
        item.setStatus(StatusItemExame.LIBERADO);
        item = itemRepository.save(item);
        ResultadoExame resultado = new ResultadoExame();
        resultado.setItem(item);
        resultado.setAnalisadoPor(medico);
        resultado.setValorTexto("Normal");
        resultado.setRegistradoEm(dia(21));
        resultado.setLiberadoEm(dia(20));
        resultado = resultadoRepository.save(resultado);
        item.setResultadoAtual(resultado);
        itemRepository.save(item);
        // A regulação agendada: anda, mas não cumpre.
        SolicitacaoRegulacao s = new SolicitacaoRegulacao();
        s.setPaciente(paciente);
        s.setProcedimento(procedimento);
        s.setUnidadeSolicitante(ubs);
        s.setProfissionalSolicitante(medico);
        s.setCid("N63");
        s.setJustificativa("Nódulo");
        s.setSolicitadoEm(dia(30));
        s.setStatus(StatusSolicitacaoRegulacao.AGENDADA);
        s.setPrioridade(PrioridadeRegulacao.AMARELO);
        s.setDataHoraPrevista(LocalDateTime.now().plusDays(5));
        s = solicitacaoRepository.save(s);

        String abrir = "{\"pacienteId\": \"%s\", \"linhaId\": \"%s\", \"inicio\": \"%s\", \"unidadeId\": \"%s\"}";
        enviar(post("/api/v1/percurso-linha-cuidado"), abrir.formatted(paciente.getUuid(), linha, hoje.plusDays(1), ubs.getUuid()), 400);
        String p = enviar(post("/api/v1/percurso-linha-cuidado"), abrir.formatted(paciente.getUuid(), linha, inicio, ubs.getUuid()), 201);
        String percurso = JsonPath.read(p, "$.uuid");
        enviar(post("/api/v1/percurso-linha-cuidado"), abrir.formatted(paciente.getUuid(), linha, inicio, ubs.getUuid()), 409);

        assertThat(etapa(p, 1)).containsEntry("situacao", "CUMPRIDA").containsEntry("realizadas", 1);
        assertThat(etapa(p, 2)).containsEntry("situacao", "CUMPRIDA").containsEntry("cumpridaEm", hoje.minusDays(20).toString());
        assertThat(etapa(p, 3)).containsEntry("situacao", "PENDENTE");
        assertThat((String) etapa(p, 3).get("evidencia")).contains("agendado para");
        assertThat(etapa(p, 4)).containsEntry("situacao", "ATRASADA").containsEntry("prazo", inicio.plusDays(10).toString());
        assertThat(etapa(p, 5)).containsEntry("situacao", "PENDENTE");
        assertThat(etapa(p, 6)).containsEntry("situacao", "AGUARDANDO");
        assertThat((Boolean) JsonPath.read(p, "$.atrasado")).isTrue();
        assertThat((String) JsonPath.read(p, "$.etapaAtual")).isEqualTo("Consulta especializada");

        // A alta com o CID do parto cumpre a internação; a etapa seguinte conta o prazo da alta.
        Setor setor = setorRepository.save(new Setor(ubs, PREFIXO + "Obstetrícia", "LT-OB", TipoSetor.ASSISTENCIAL, true, null));
        Leito leito = new Leito();
        leito.setUnidade(ubs);
        leito.setSetor(setor);
        leito.setIdentificacao("OB 01");
        leito.setTipo(TipoLeito.CLINICO);
        leito.setSexo(SexoLeito.MISTO);
        leito.setSituacao(SituacaoLeito.LIVRE);
        leito.setAtivo(true);
        leito = leitoRepository.save(leito);
        Internacao i = new Internacao();
        i.setPaciente(paciente);
        i.setUnidade(ubs);
        i.setLeito(leito);
        i.setMedicoResponsavel(medico);
        i.setCid("O80.0");
        i.setMotivo("Trabalho de parto");
        i.setCarater(CaraterInternacao.URGENCIA);
        i.setAdmitidaEm(dia(4));
        i.setStatus(StatusInternacao.ALTA);
        i.setAltaEm(dia(2));
        internacaoRepository.save(i);
        p = ler("/api/v1/percurso-linha-cuidado/" + percurso);
        assertThat(etapa(p, 5)).containsEntry("situacao", "CUMPRIDA");
        assertThat(etapa(p, 6)).containsEntry("situacao", "PENDENTE").containsEntry("prazo", hoje.minusDays(2).plusDays(15).toString());

        // A manual: duas vezes para cumprir; a de domínio não se registra à mão; a dispensa pede o motivo.
        String etapa6 = (String) etapa(p, 6).get("etapaId");
        String etapa1 = (String) etapa(p, 1).get("etapaId");
        String etapa4 = (String) etapa(p, 4).get("etapaId");
        String url = "/api/v1/percurso-linha-cuidado/" + percurso + "/etapa";
        enviar(post(url), "{\"etapaId\": \"%s\", \"tipo\": \"CUMPRIDA\"}".formatted(etapa1), 422);
        p = enviar(post(url), "{\"etapaId\": \"%s\", \"tipo\": \"CUMPRIDA\", \"observacao\": \"Primeira visita\"}".formatted(etapa6), 200);
        assertThat(etapa(p, 6)).containsEntry("situacao", "PENDENTE").containsEntry("realizadas", 1);
        p = enviar(post(url), "{\"etapaId\": \"%s\", \"tipo\": \"CUMPRIDA\"}".formatted(etapa6), 200);
        assertThat(etapa(p, 6)).containsEntry("situacao", "CUMPRIDA");
        enviar(post(url), "{\"etapaId\": \"%s\", \"tipo\": \"DISPENSADA\"}".formatted(etapa4), 400);
        p = enviar(post(url), "{\"etapaId\": \"%s\", \"tipo\": \"DISPENSADA\", \"observacao\": \"Vacinada em outro município\"}".formatted(etapa4), 200);
        assertThat(etapa(p, 4)).containsEntry("situacao", "DISPENSADA").containsEntry("observacao", "Vacinada em outro município");
        assertThat((Boolean) JsonPath.read(p, "$.atrasado")).isFalse();

        // Concluir pede as obrigatórias feitas; a regulação realizada cumpre a última; a opcional não segura.
        String fim = "/api/v1/percurso-linha-cuidado/" + percurso + "/encerramento";
        enviar(post(fim), "{\"situacao\": \"CONCLUIDO\"}", 422);
        enviar(post(fim), "{\"situacao\": \"INTERROMPIDO\"}", 400);
        s.setStatus(StatusSolicitacaoRegulacao.REALIZADA);
        s.setConcluidoEm(dia(1));
        solicitacaoRepository.save(s);
        p = enviar(post(fim), "{\"situacao\": \"CONCLUIDO\"}", 200);
        assertThat((String) JsonPath.read(p, "$.situacao")).isEqualTo("CONCLUIDO");
        assertThat(etapa(p, 7)).containsEntry("situacao", "PENDENTE");
        enviar(post(url), "{\"etapaId\": \"%s\", \"tipo\": \"CUMPRIDA\"}".formatted(etapa6), 422);

        String doPaciente = ler("/api/v1/paciente/" + paciente.getUuid() + "/linhas-de-cuidado");
        assertThat((List<String>) JsonPath.read(doPaciente, "$[*].uuid")).containsExactly(percurso);
    }

    @Test
    void painelComSugestaoEAtrasados() throws Exception {
        String preNatal = id("select cast(uuid as varchar) from tb_programa where codigo = 'PRE_NATAL'");
        String gestante = id("select cast(uuid as varchar) from tb_linha_cuidado where codigo = 'GESTANTE'");
        String acompanhamento = jdbc.queryForObject("insert into tb_acompanhamento_programatico (uuid, paciente_id, programa_id, unidade_id, inicio, situacao) "
                + "values (gen_random_uuid(), ?, cast(? as uuid), ?, ?, 'ATIVO') returning cast(uuid as varchar)", String.class, paciente.getUuid(), preNatal,
                ubs.getUuid(), hoje.minusDays(10));

        String painel = ler("/api/v1/linhas-de-cuidado/painel?unidadeId=" + ubs.getUuid());
        assertThat((List<String>) JsonPath.read(painel, "$.sugestoes[?(@.linhaId == '" + gestante + "')].acompanhamentoId")).containsExactly(acompanhamento);

        // O acompanhamento precisa ser do programa da linha.
        String outraLinha = id("select cast(uuid as varchar) from tb_linha_cuidado where codigo = 'PRIMEIRA_SEMANA_CRIANCA'");
        String abrir = "{\"pacienteId\": \"%s\", \"linhaId\": \"%s\", \"inicio\": \"%s\", \"unidadeId\": \"%s\", \"acompanhamentoId\": \"%s\"}";
        enviar(post("/api/v1/percurso-linha-cuidado"), abrir.formatted(paciente.getUuid(), outraLinha, hoje.minusDays(3), ubs.getUuid(), acompanhamento), 422);

        // Aberta a linha da gestante há 100 dias, sem consulta: a captação está atrasada, e a sugestão some.
        String p = enviar(post("/api/v1/percurso-linha-cuidado"), abrir.formatted(paciente.getUuid(), gestante, hoje.minusDays(100), ubs.getUuid(),
                acompanhamento), 201);
        assertThat((Boolean) JsonPath.read(p, "$.atrasado")).isTrue();
        assertThat((String) JsonPath.read(p, "$.etapaAtual")).startsWith("1ª consulta");
        assertThat(etapa(p, 6)).containsEntry("situacao", "AGUARDANDO");
        painel = ler("/api/v1/linhas-de-cuidado/painel?unidadeId=" + ubs.getUuid() + "&atrasados=true");
        assertThat((List<String>) JsonPath.read(painel, "$.percursos[*].pacienteNome")).containsExactly(PREFIXO + "Paciente");
        assertThat((Integer) JsonPath.read(painel, "$.atrasados")).isEqualTo(1);
        assertThat((List<?>) JsonPath.read(painel, "$.sugestoes[?(@.linhaId == '" + gestante + "')]")).isEmpty();
    }
}
