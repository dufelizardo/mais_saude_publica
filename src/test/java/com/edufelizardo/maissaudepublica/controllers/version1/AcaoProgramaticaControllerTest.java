package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.models.Agendamento;
import com.edufelizardo.maissaudepublica.models.Atendimento;
import com.edufelizardo.maissaudepublica.models.CategoriaPrograma;
import com.edufelizardo.maissaudepublica.models.Paciente;
import com.edufelizardo.maissaudepublica.models.ParticipacaoPrograma;
import com.edufelizardo.maissaudepublica.models.Profissional;
import com.edufelizardo.maissaudepublica.models.Programa;
import com.edufelizardo.maissaudepublica.models.UnidadeDeSaude;
import com.edufelizardo.maissaudepublica.models.enuns.AbrangenciaPrograma;
import com.edufelizardo.maissaudepublica.models.enuns.PeriodicidadeAcompanhamento;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoPrograma;
import com.edufelizardo.maissaudepublica.models.enuns.StatusAtendimento;
import com.edufelizardo.maissaudepublica.models.enuns.TipoAtendimento;
import com.edufelizardo.maissaudepublica.models.enuns.TipoUnidadeDeSaude;
import com.edufelizardo.maissaudepublica.repositories.AgendamentoRepository;
import com.edufelizardo.maissaudepublica.repositories.AtendimentoRepository;
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
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Ações programáticas, fatia P4 (ADR-0132): ação individual pelo acompanhamento e coletiva com os participantes, as regras
 * de realização, não realizada e cancelada, a busca ativa (uma pendente, resultado, sugestão de quem mudou), o programa na
 * marcação da Agenda e a ação pronta para confirmar quando o atendimento da marcação é concluído.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AcaoProgramaticaControllerTest {

    private static final String PREFIXO = "AcaoProg Teste ";
    private static final String CODIGO = "TESTE_AP_";
    private static final String URL = "/api/v1/";

    @Autowired private MockMvc mockMvc;
    @Autowired private JdbcTemplate jdbc;
    @Autowired private UnidadeDeSaudeRepository unidadeDeSaudeRepository;
    @Autowired private PacienteRepository pacienteRepository;
    @Autowired private ProgramaRepository programaRepository;
    @Autowired private CategoriaProgramaRepository categoriaRepository;
    @Autowired private ParticipacaoProgramaRepository participacaoRepository;
    @Autowired private ProfissionalRepository profissionalRepository;
    @Autowired private AgendamentoRepository agendamentoRepository;
    @Autowired private AtendimentoRepository atendimentoRepository;

    private UnidadeDeSaude ubs;
    private Programa hiperdia;
    private Paciente joao;
    private Paciente maria;
    private Paciente pedro;
    private Profissional medico;

    @BeforeEach
    void seed() {
        limpar();
        ubs = new UnidadeDeSaude();
        ubs.setNome(PREFIXO + "UBS");
        ubs.setTipo(TipoUnidadeDeSaude.UBS);
        ubs.setAtivo(true);
        ubs = unidadeDeSaudeRepository.save(ubs);
        CategoriaPrograma cronicos = categoriaRepository.findByNomeIgnoreCase("Crônicos").orElseThrow();
        Programa p = new Programa();
        p.setCodigo(CODIGO + "HD");
        p.setNome(PREFIXO + "HiperDia");
        p.setCategoria(cronicos);
        p.setAbrangencia(AbrangenciaPrograma.MUNICIPAL);
        p.setSituacao(SituacaoPrograma.ATIVO);
        p.setPeriodicidade(PeriodicidadeAcompanhamento.MENSAL);
        hiperdia = programaRepository.save(p);
        participacaoRepository.save(new ParticipacaoPrograma(null, hiperdia, ubs, null, LocalDate.now().minusYears(1), null, null, null));
        joao = paciente("João", "90044400001");
        maria = paciente("Maria", "90044400002");
        pedro = paciente("Pedro", "90044400003");
        Profissional m = new Profissional();
        m.setMatricula("AP-MEDICO");
        m.setCpf("90044400009");
        m.setNome(PREFIXO + "Médico sem lotação");
        m.setAtivo(true);
        medico = profissionalRepository.save(m);
    }

    @AfterEach
    void limpar() {
        String programas = "(select uuid from tb_programa where codigo like '" + CODIGO + "%')";
        String pacientes = "(select uuid from tb_paciente where nome like '" + PREFIXO + "%')";
        jdbc.update("delete from tb_acao_programatica_participante where acao_id in (select uuid from tb_acao_programatica where programa_id in "
                + programas + ")");
        jdbc.update("delete from tb_acao_programatica where programa_id in " + programas);
        jdbc.update("delete from tb_atendimento where paciente_id in " + pacientes);
        jdbc.update("delete from tb_agendamento where paciente_id in " + pacientes);
        jdbc.update("delete from tb_evento_acompanhamento where acompanhamento_id in (select uuid from tb_acompanhamento_programatico where programa_id in "
                + programas + ")");
        jdbc.update("delete from tb_acompanhamento_programatico where programa_id in " + programas);
        jdbc.update("delete from tb_participacao_programa where programa_id in " + programas);
        jdbc.update("delete from tb_programa where codigo like '" + CODIGO + "%'");
        jdbc.update("delete from tb_paciente where nome like '" + PREFIXO + "%'");
        jdbc.update("delete from tb_profissional where matricula = 'AP-MEDICO'");
        jdbc.update("delete from tb_unidade_de_saude where nome like '" + PREFIXO + "%'");
    }

    private Paciente paciente(String nome, String cpf) {
        Paciente p = new Paciente();
        p.setNome(PREFIXO + nome);
        p.setCpf(cpf);
        p.setDataNascimento(LocalDate.now().minusYears(50));
        p.setAtivo(true);
        return pacienteRepository.save(p);
    }

    private String enviar(MockHttpServletRequestBuilder r, String corpo, int esperado) throws Exception {
        return mockMvc.perform(r.contentType(MediaType.APPLICATION_JSON).content(corpo)).andExpect(status().is(esperado))
                .andReturn().getResponse().getContentAsString();
    }

    private String ler(String url) throws Exception {
        return mockMvc.perform(get(url)).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
    }

    private String inscrever(Paciente p) throws Exception {
        String r = enviar(post(URL + "programa/" + hiperdia.getUuid() + "/acompanhamento"),
                "{\"pacienteId\": \"%s\", \"unidadeId\": \"%s\"}".formatted(p.getUuid(), ubs.getUuid()), 201);
        return JsonPath.read(r, "$.uuid");
    }

    private String acao(String extra, int esperado) throws Exception {
        return enviar(post(URL + "acao-programatica"), "{\"programaId\": \"%s\", \"dataPrevista\": \"%s\"%s}"
                .formatted(hiperdia.getUuid(), LocalDate.now().minusDays(1), extra), esperado);
    }

    @Test
    void acaoIndividualRealizadaENaoRealizada() throws Exception {
        String acJoao = inscrever(joao);
        acao(", \"tipo\": \"CONSULTA_PROGRAMADA\"", 400);
        acao(", \"tipo\": \"CONSULTA_PROGRAMADA\", \"acompanhamentoId\": \"%s\", \"responsavelMatricula\": \"AP-MEDICO\"".formatted(acJoao), 422);
        String r = acao(", \"tipo\": \"CONSULTA_PROGRAMADA\", \"acompanhamentoId\": \"%s\"".formatted(acJoao), 201);
        String consulta = JsonPath.read(r, "$.uuid");
        assertThat(JsonPath.<String>read(r, "$.situacao")).isEqualTo("PENDENTE");
        assertThat(JsonPath.<Boolean>read(r, "$.vencida")).isTrue();
        String lista = ler(URL + "acoes-programaticas?unidadeId=" + ubs.getUuid());
        assertThat(JsonPath.<Integer>read(lista, "$.vencidas")).isEqualTo(1);
        assertThat((List<String>) JsonPath.read(lista, "$.lista[*].uuid")).contains(consulta);

        enviar(post(URL + "acao-programatica/" + consulta + "/realizacao"), "{\"realizadaEm\": \"%s\"}".formatted(LocalDate.now().plusDays(1)), 400);
        enviar(post(URL + "acao-programatica/" + consulta + "/realizacao"), "{\"resultadoBusca\": \"NAO_ENCONTRADO\"}", 400);
        r = enviar(post(URL + "acao-programatica/" + consulta + "/realizacao"), "{\"observacao\": \"PA 130x85\"}", 200);
        assertThat(JsonPath.<String>read(r, "$.situacao")).isEqualTo("REALIZADA");
        assertThat(JsonPath.<String>read(r, "$.realizadaEm")).isEqualTo(LocalDate.now().toString());
        enviar(post(URL + "acao-programatica/" + consulta + "/realizacao"), "{}", 422);

        String afericao = JsonPath.read(acao(", \"tipo\": \"AFERICAO\", \"acompanhamentoId\": \"%s\"".formatted(acJoao), 201), "$.uuid");
        enviar(post(URL + "acao-programatica/" + afericao + "/situacao"), "{\"situacao\": \"NAO_REALIZADA\"}", 400);
        enviar(post(URL + "acao-programatica/" + afericao + "/situacao"), "{\"situacao\": \"REALIZADA\", \"motivo\": \"x\"}", 400);
        r = enviar(post(URL + "acao-programatica/" + afericao + "/situacao"), "{\"situacao\": \"NAO_REALIZADA\", \"motivo\": \"Faltou\"}", 200);
        assertThat(JsonPath.<String>read(r, "$.motivo")).isEqualTo("Faltou");
        assertThat((List<?>) JsonPath.read(ler(URL + "paciente/" + joao.getUuid() + "/acoes-programaticas"), "$")).hasSize(2);
    }

    @Test
    void buscaAtivaEAcaoColetiva() throws Exception {
        String acJoao = inscrever(joao);
        inscrever(maria);
        String r = enviar(post(URL + "acompanhamento/" + acJoao + "/busca-ativa"), "{}", 201);
        String busca = JsonPath.read(r, "$.uuid");
        assertThat(JsonPath.<String>read(r, "$.tipo")).isEqualTo("BUSCA_ATIVA");
        assertThat(JsonPath.<String>read(r, "$.dataPrevista")).isEqualTo(LocalDate.now().toString());
        enviar(post(URL + "acompanhamento/" + acJoao + "/busca-ativa"), "{}", 409);
        enviar(post(URL + "acao-programatica/" + busca + "/realizacao"), "{}", 400);
        r = enviar(post(URL + "acao-programatica/" + busca + "/realizacao"), "{\"resultadoBusca\": \"MUDOU_DE_ENDERECO\"}", 200);
        assertThat(JsonPath.<String>read(r, "$.sugestao")).contains("encerrar o acompanhamento");

        // Coletiva: grupo com participantes acompanhados no programa.
        acao(", \"tipo\": \"BUSCA_ATIVA\", \"unidadeId\": \"%s\"".formatted(ubs.getUuid()), 400);
        acao(", \"tipo\": \"GRUPO\"", 400);
        acao(", \"tipo\": \"GRUPO\", \"unidadeId\": \"%s\", \"participantesIds\": [\"%s\", \"%s\"]".formatted(ubs.getUuid(), joao.getUuid(), pedro.getUuid()), 422);
        r = acao(", \"tipo\": \"GRUPO\", \"unidadeId\": \"%s\", \"local\": \"Salão\", \"participantesIds\": [\"%s\"]".formatted(ubs.getUuid(), joao.getUuid()), 201);
        assertThat(JsonPath.<Boolean>read(r, "$.coletiva")).isTrue();
        String grupo = JsonPath.read(r, "$.uuid");
        r = enviar(post(URL + "acao-programatica/" + grupo + "/realizacao"), "{\"participantesIds\": [\"%s\", \"%s\"]}".formatted(joao.getUuid(), maria.getUuid()), 200);
        assertThat((List<?>) JsonPath.read(r, "$.participantes")).hasSize(2);
    }

    @Test
    void programaNaMarcacaoEAcaoProntaParaConfirmar() throws Exception {
        String acJoao = inscrever(joao);
        String consulta = JsonPath.read(acao(", \"tipo\": \"CONSULTA_PROGRAMADA\", \"acompanhamentoId\": \"%s\"".formatted(acJoao), 201), "$.uuid");
        String marcacao = "{\"pacienteId\": \"%s\", \"profissionalMatricula\": \"AP-MEDICO\", \"dataHora\": \"%s\", \"status\": \"AGENDADO\", "
                + "\"tipo\": \"CONSULTA\", \"programaId\": \"%s\"%s}";
        String quando = LocalDateTime.now().plusDays(2).withNano(0).toString();
        // Quem não é acompanhado no programa não é marcado nele.
        enviar(post(URL + "agendamento/"), marcacao.formatted(pedro.getUuid(), quando, hiperdia.getUuid(), ""), 422);
        enviar(post(URL + "agendamento/"), marcacao.formatted(joao.getUuid(), quando, hiperdia.getUuid(),
                ", \"acaoProgramaticaId\": \"" + consulta + "\""), 201);
        UUID agendamentoId = agendamentoRepository.findByPaciente_UuidOrderByDataHoraAsc(joao.getUuid()).get(0).getUuid();
        String r = ler(URL + "agendamento/" + agendamentoId);
        assertThat(JsonPath.<String>read(r, "$.programaNome")).isEqualTo(PREFIXO + "HiperDia");

        Agendamento agendamento = agendamentoRepository.findById(agendamentoId).orElseThrow();
        Atendimento at = atendimentoRepository.save(new Atendimento(joao, medico, ubs, null, agendamento, TipoAtendimento.CONSULTA,
                StatusAtendimento.CONCLUIDO, LocalDateTime.now()));
        String lista = ler(URL + "acoes-programaticas?unidadeId=" + ubs.getUuid());
        assertThat(JsonPath.<Integer>read(lista, "$.prontasParaConfirmar")).isEqualTo(1);
        assertThat(JsonPath.<String>read(lista, "$.lista[0].atendimentoSugeridoId")).isEqualTo(at.getUuid().toString());
        assertThat(JsonPath.<String>read(lista, "$.lista[0].agendamentoId")).isEqualTo(agendamentoId.toString());

        // Confirmar com o atendimento de outro paciente não vale; com o sugerido, sim.
        Atendimento outro = atendimentoRepository.save(new Atendimento(maria, medico, ubs, null, null, TipoAtendimento.CONSULTA,
                StatusAtendimento.CONCLUIDO, LocalDateTime.now()));
        enviar(post(URL + "acao-programatica/" + consulta + "/realizacao"), "{\"atendimentoId\": \"%s\"}".formatted(outro.getUuid()), 422);
        r = enviar(post(URL + "acao-programatica/" + consulta + "/realizacao"), "{\"atendimentoId\": \"%s\"}".formatted(at.getUuid()), 200);
        assertThat(JsonPath.<String>read(r, "$.atendimentoId")).isEqualTo(at.getUuid().toString());
        assertThat(JsonPath.<Boolean>read(r, "$.prontaParaConfirmar")).isFalse();
    }
}
