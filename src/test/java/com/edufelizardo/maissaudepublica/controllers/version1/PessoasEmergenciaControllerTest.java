package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.models.Endereco;
import com.edufelizardo.maissaudepublica.models.Paciente;
import com.edufelizardo.maissaudepublica.models.Profissional;
import com.edufelizardo.maissaudepublica.models.UnidadeDeSaude;
import com.edufelizardo.maissaudepublica.models.enuns.TipoUnidadeDeSaude;
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

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

/**
 * Emergências, fatia E4a (ADR-0193): os vulneráveis da área afetada (microárea marcada e imóvel dentro do polígono; idade, gestante,
 * programas não sensíveis, diálise; o caso de saúde mental fora), os deslocados (destinos, 409, realocação com histórico, retorno,
 * encerramento, transporte do mesmo paciente), o encaminhamento intersetorial, o aviso do prontuário, a pendência e o relatório.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PessoasEmergenciaControllerTest {

    private static final String PREFIXO = "PessEmerg Teste ";
    private static final String POLIGONO = "{\"type\": \"Polygon\", \"coordinates\": [[[-46.70, -23.60], [-46.60, -23.60], [-46.60, -23.50], [-46.70, -23.50], [-46.70, -23.60]]]}";

    @Autowired private MockMvc mockMvc;
    @Autowired private JdbcTemplate jdbc;
    @Autowired private UnidadeDeSaudeRepository unidadeRepository;
    @Autowired private PacienteRepository pacienteRepository;
    @Autowired private ProfissionalRepository profissionalRepository;

    private final LocalDate hoje = LocalDate.now();
    private UnidadeDeSaude ubs;
    private UnidadeDeSaude hospital;
    private String m1;
    private Paciente idoso;
    private Paciente gestante;
    private Paciente crianca;
    private Paciente adulto;
    private Paciente mental;
    private Paciente dialise;
    private Paciente longe;
    private String abrigo;
    private String cras;

    @BeforeEach
    void seed() {
        limpar();
        ubs = unidade("UBS");
        hospital = unidade("Hospital");
        Profissional acs = new Profissional();
        acs.setMatricula("PEM-ACS");
        acs.setCpf("90199900001");
        acs.setNome(PREFIXO + "Agente");
        acs.setAtivo(true);
        acs = profissionalRepository.save(acs);
        String segmento = jdbc.queryForObject("insert into tb_segmento_territorial (uuid, codigo, nome, tipo) values (gen_random_uuid(), 'PEM-SEG', "
                + "'Segmento PEM', 'DISTRITO_SANITARIO') returning cast(uuid as varchar)", String.class);
        String territorio = jdbc.queryForObject("insert into tb_territorio (uuid, segmento_id, nome, finalidade, ativo) values (gen_random_uuid(), "
                + "cast(? as uuid), ?, 'ATENCAO_PRIMARIA', true) returning cast(uuid as varchar)", String.class, segmento, PREFIXO + "Território");
        String area = jdbc.queryForObject("insert into tb_area (uuid, territorio_id, codigo, nome) values (gen_random_uuid(), cast(? as uuid), 'PEM-AR', "
                + "'Área Norte') returning cast(uuid as varchar)", String.class, territorio);
        m1 = jdbc.queryForObject("insert into tb_microarea (uuid, area_id, codigo, nome) values (gen_random_uuid(), cast(? as uuid), 'PEM-01', 'Beira-rio') "
                + "returning cast(uuid as varchar)", String.class, area);
        String m2 = jdbc.queryForObject("insert into tb_microarea (uuid, area_id, codigo, nome) values (gen_random_uuid(), cast(? as uuid), 'PEM-02', 'Morro') "
                + "returning cast(uuid as varchar)", String.class, area);
        String equipe = jdbc.queryForObject("insert into tb_equipe (uuid, unidade_id, nome, tipo, ativa) values (gen_random_uuid(), ?, ?, 'ESF', true) "
                + "returning cast(uuid as varchar)", String.class, ubs.getUuid(), PREFIXO + "ESF");
        String membro = jdbc.queryForObject("insert into tb_membro_equipe (uuid, equipe_id, profissional_id, funcao, inicio) values (gen_random_uuid(), "
                + "cast(? as uuid), ?, 'ACS', ?) returning cast(uuid as varchar)", String.class, equipe, acs.getUuid(), hoje.minusDays(30));
        jdbc.update("insert into tb_atribuicao_microarea (uuid, membro_id, microarea_id, inicio) values (gen_random_uuid(), cast(? as uuid), cast(? as uuid), ?)",
                membro, m1, hoje.minusDays(30));

        idoso = paciente("Idoso", 70);
        gestante = paciente("Gestante", 25);
        crianca = paciente("Crianca", 3);
        adulto = paciente("Adulto", 40);
        mental = paciente("Mental", 35);
        dialise = paciente("Dialise", 50);
        longe = paciente("Longe", 80);
        // Microárea marcada, sem coordenada; microárea fora da lista, dentro e fora do polígono.
        domicilio(m1, null, null, idoso, gestante, crianca, adulto, mental);
        domicilio(m2, -23.55, -46.65, dialise);
        domicilio(m2, -22.90, -43.20, longe);
        acompanhamento(gestante, "PRE_NATAL");
        acompanhamento(mental, "SAUDE_MENTAL");
        acompanhamento(adulto, "HIPERDIA");
        abrigo = instituicao("Abrigo Ginasio", "ACOLHIMENTO_INSTITUCIONAL");
        cras = instituicao("CRAS Norte", "CRAS");
    }

    @AfterEach
    void limpar() {
        String emergencias = "(select e.uuid from tb_emergencia e join tb_evento_emergencial v on v.uuid = e.evento_id where v.descricao like '" + PREFIXO + "%')";
        String pacientes = "(select uuid from tb_paciente where nome like '" + PREFIXO + "%')";
        String unidades = "(select uuid from tb_unidade_de_saude where nome like '" + PREFIXO + "%')";
        String microareas = "(select uuid from tb_microarea where codigo like 'PEM-%')";
        String relatorios = "(select uuid from tb_relatorio_pos_evento where emergencia_id in " + emergencias + ")";
        jdbc.update("delete from tb_encaminhamento_intersetorial where paciente_id in " + pacientes);
        jdbc.update("delete from tb_paciente_deslocado where paciente_id in " + pacientes);
        jdbc.update("delete from tb_recomendacao_relatorio where relatorio_id in " + relatorios);
        jdbc.update("delete from tb_relatorio_pos_evento where emergencia_id in " + emergencias);
        jdbc.update("delete from tb_desmobilizacao where emergencia_id in " + emergencias);
        jdbc.update("delete from tb_emergencia_microarea where emergencia_id in " + emergencias);
        jdbc.update("delete from tb_mudanca_nivel_emergencia where emergencia_id in " + emergencias);
        jdbc.update("delete from tb_emergencia where uuid in " + emergencias);
        jdbc.update("delete from tb_evento_emergencial where descricao like '" + PREFIXO + "%'");
        jdbc.update("delete from tb_solicitacao_transporte where paciente_id in " + pacientes);
        jdbc.update("delete from tb_acompanhamento_programatico where paciente_id in " + pacientes);
        jdbc.update("delete from tb_membro_nucleo where paciente_id in " + pacientes);
        jdbc.update("delete from tb_nucleo_familiar where responsavel_id in " + pacientes);
        jdbc.update("delete from tb_domicilio where imovel_id in (select uuid from tb_imovel where microarea_id in " + microareas + ")");
        jdbc.update("delete from tb_imovel where microarea_id in " + microareas);
        jdbc.update("delete from tb_atribuicao_microarea where microarea_id in " + microareas);
        jdbc.update("delete from tb_membro_equipe where equipe_id in (select uuid from tb_equipe where unidade_id in " + unidades + ")");
        jdbc.update("delete from tb_equipe where unidade_id in " + unidades);
        jdbc.update("delete from tb_microarea where codigo like 'PEM-%'");
        jdbc.update("delete from tb_area where codigo = 'PEM-AR'");
        jdbc.update("delete from tb_territorio where nome = '" + PREFIXO + "Território'");
        jdbc.update("delete from tb_segmento_territorial where codigo = 'PEM-SEG'");
        jdbc.update("delete from tb_instituicao_externa where nome like '" + PREFIXO + "%'");
        jdbc.update("delete from tb_paciente where nome like '" + PREFIXO + "%'");
        jdbc.update("delete from tb_profissional where matricula = 'PEM-ACS'");
        jdbc.update("delete from tb_unidade_de_saude where nome like '" + PREFIXO + "%'");
    }

    private UnidadeDeSaude unidade(String nome) {
        UnidadeDeSaude u = new UnidadeDeSaude();
        u.setNome(PREFIXO + nome);
        u.setTipo(TipoUnidadeDeSaude.UBS);
        u.setMunicipio("Cidade Alfa");
        u.setAtivo(true);
        return unidadeRepository.save(u);
    }

    private Paciente paciente(String nome, int idade) {
        Paciente p = new Paciente();
        p.setNome(PREFIXO + nome);
        p.setCpf("9019" + String.format("%07d", Math.abs(nome.hashCode()) % 10_000_000));
        p.setDataNascimento(hoje.minusYears(idade).minusDays(10));
        p.setAtivo(true);
        Endereco e = new Endereco();
        e.setLogradouro("Rua do Rio");
        e.setNumeroLogradouro("10");
        e.setCidade("Cidade Alfa");
        e.setEstado("SP");
        p.setEndereco(e);
        return pacienteRepository.save(p);
    }

    private void domicilio(String microarea, Double lat, Double lng, Paciente... moradores) {
        String imovel = jdbc.queryForObject("insert into tb_imovel (uuid, microarea_id, tipo, ativo, logradouro, numero_logradouro, latitude, longitude) values "
                + "(gen_random_uuid(), cast(? as uuid), 'DOMICILIO', true, 'Rua do Rio', '10', ?, ?) returning cast(uuid as varchar)", String.class, microarea, lat, lng);
        String dom = jdbc.queryForObject("insert into tb_domicilio (uuid, imovel_id, ativo) values (gen_random_uuid(), cast(? as uuid), true) "
                + "returning cast(uuid as varchar)", String.class, imovel);
        String nucleo = jdbc.queryForObject("insert into tb_nucleo_familiar (uuid, domicilio_id, responsavel_id, inicio) values (gen_random_uuid(), "
                + "cast(? as uuid), ?, ?) returning cast(uuid as varchar)", String.class, dom, moradores[0].getUuid(), hoje.minusYears(1));
        for (Paciente p : moradores) {
            jdbc.update("insert into tb_membro_nucleo (uuid, nucleo_id, paciente_id, parentesco, inicio) values (gen_random_uuid(), cast(? as uuid), ?, "
                    + "'FILHO', ?)", nucleo, p.getUuid(), hoje.minusYears(1));
        }
    }

    private void acompanhamento(Paciente p, String programa) {
        jdbc.update("insert into tb_acompanhamento_programatico (uuid, paciente_id, programa_id, unidade_id, inicio, situacao) "
                + "select gen_random_uuid(), ?, uuid, ?, ?, 'ATIVO' from tb_programa where codigo = ?", p.getUuid(), ubs.getUuid(), hoje.minusDays(20), programa);
    }

    private String instituicao(String nome, String tipo) {
        return jdbc.queryForObject("insert into tb_instituicao_externa (uuid, nome, setor, tipo, ativa, atualizada_em) values (gen_random_uuid(), ?, "
                + "'ASSISTENCIA_SOCIAL', ?, true, now()) returning cast(uuid as varchar)", String.class, PREFIXO + nome, tipo);
    }

    private String enviar(MockHttpServletRequestBuilder r, String corpo, int esperado) throws Exception {
        if (corpo != null) {
            r = r.contentType(MediaType.APPLICATION_JSON).content(corpo);
        }
        var resposta = mockMvc.perform(r).andReturn().getResponse();
        assertThat(resposta.getStatus()).as(resposta.getContentAsString(StandardCharsets.UTF_8)).isEqualTo(esperado);
        return resposta.getContentAsString(StandardCharsets.UTF_8);
    }

    private String emergencia() throws Exception {
        String tipo = jdbc.queryForObject("select cast(uuid as varchar) from tb_tipo_emergencia where codigo = 'INUNDACAO'", String.class);
        String nivel = jdbc.queryForObject("select cast(uuid as varchar) from tb_nivel_emergencia where ordem = 1", String.class);
        String evento = JsonPath.read(enviar(post("/api/v1/evento-emergencial"), "{\"tipoId\": \"" + tipo + "\", \"fonte\": \"DEFESA_CIVIL\", "
                + "\"descricao\": \"" + PREFIXO + "Cheia\"}", 201), "$.uuid");
        String em = JsonPath.read(enviar(post("/api/v1/evento-emergencial/" + evento + "/emergencia"), "{\"nome\": \"Cheia Pessoas\", \"nivelId\": \"" + nivel
                + "\", \"justificativa\": \"Rio acima da cota\"}", 201), "$.uuid");
        enviar(put("/api/v1/emergencia/" + em + "/territorio"), "{\"geometria\": \"" + POLIGONO.replace("\"", "\\\"") + "\", \"microareaIds\": [\""
                + m1 + "\"]}", 200);
        return em;
    }

    private String deslocar(String em, Paciente p, String destino, int esperado) throws Exception {
        return enviar(post("/api/v1/emergencia/" + em + "/deslocado"), "{\"pacienteId\": \"%s\", %s}".formatted(p.getUuid(), destino), esperado);
    }

    private String idDe(String lista, Paciente p, String situacao) {
        return JsonPath.parse(lista).read("$[?(@.pacienteId == '" + p.getUuid() + "' && @.situacao == '" + situacao + "')].uuid", List.class).get(0).toString();
    }

    @Test
    void vulneraveisDaAreaAfetada() throws Exception {
        String em = emergencia();
        String r = enviar(get("/api/v1/emergencia/" + em + "/vulneraveis"), null, 200);
        // Na área: os cinco da microárea marcada e o da diálise dentro do polígono; o de longe fica fora. Sem transporte, a diálise ainda
        // não marca.
        assertThat((Integer) JsonPath.read(r, "$.pessoasNaArea")).isEqualTo(6);
        assertThat((List<String>) JsonPath.read(r, "$.lista[*].nome")).containsExactlyInAnyOrder(PREFIXO + "Idoso", PREFIXO + "Gestante",
                PREFIXO + "Crianca", PREFIXO + "Adulto");
        List<Map<String, Object>> gl = JsonPath.parse(r).read("$.lista[?(@.nome == '" + PREFIXO + "Gestante')]");
        Map<String, Object> g = gl.get(0);
        assertThat((List<String>) g.get("marcadores")).containsExactly("GESTANTE", "PROGRAMA");
        assertThat((List<String>) g.get("programas")).containsExactly("Pré-natal");
        assertThat(g.get("acsNome")).isEqualTo(PREFIXO + "Agente");
        // O caso de saúde mental (programa sensível) não aparece nem marca; com todos, entra sem marcador.
        r = enviar(get("/api/v1/emergencia/" + em + "/vulneraveis?todos=true"), null, 200);
        assertThat(JsonPath.parse(r).read("$.lista[?(@.nome == '" + PREFIXO + "Mental')].marcadores[*]", List.class)).isEmpty();
        assertThat(JsonPath.parse(r).read("$.lista[?(@.nome == '" + PREFIXO + "Mental')].programas[*]", List.class)).isEmpty();
        r = enviar(get("/api/v1/emergencia/" + em + "/vulneraveis?marcador=IDOSO"), null, 200);
        assertThat((List<String>) JsonPath.read(r, "$.lista[*].nome")).containsExactly(PREFIXO + "Idoso");
        assertThat(enviar(get("/api/v1/emergencia/" + em + "/vulneraveis.csv"), null, 200)).contains(PREFIXO + "Crianca").contains("CRIANCA");

        // A diálise vem do transporte recorrente (T2).
        String amanha = LocalDate.now(ZoneId.systemDefault()).plusDays(1) + "T08:00";
        enviar(post("/api/v1/transporte/solicitacao"), ("{\"pacienteId\": \"%s\", \"unidadeId\": \"%s\", \"motivo\": \"HEMODIALISE\", \"trecho\": \"IDA_E_VOLTA\", "
                + "\"dataHora\": \"%s\", \"destinoUnidadeId\": \"%s\"}").formatted(dialise.getUuid(), ubs.getUuid(), amanha, hospital.getUuid()), 201);
        r = enviar(get("/api/v1/emergencia/" + em + "/vulneraveis?marcador=DIALISE_TERAPIA"), null, 200);
        assertThat((List<String>) JsonPath.read(r, "$.lista[*].nome")).containsExactly(PREFIXO + "Dialise");
    }

    @Test
    void deslocadosAbrigosEncaminhamentoERelatorio() throws Exception {
        String em = emergencia();
        String amanha = LocalDate.now(ZoneId.systemDefault()).plusDays(1) + "T08:00";
        String transporteDialise = JsonPath.read(enviar(post("/api/v1/transporte/solicitacao"), ("{\"pacienteId\": \"%s\", \"unidadeId\": \"%s\", "
                + "\"motivo\": \"HEMODIALISE\", \"trecho\": \"IDA_E_VOLTA\", \"dataHora\": \"%s\", \"destinoUnidadeId\": \"%s\"}")
                .formatted(dialise.getUuid(), ubs.getUuid(), amanha, hospital.getUuid()), 201), "$.uuid");

        // Os destinos pedem o que precisam; o mesmo paciente duas vezes, 409; voltar para casa não é destino.
        deslocar(em, idoso, "\"destinoTipo\": \"ABRIGO\"", 400);
        deslocar(em, idoso, "\"destinoTipo\": \"DOMICILIO\", \"destinoDescricao\": \"Casa\"", 400);
        String l = deslocar(em, idoso, "\"destinoTipo\": \"ABRIGO\", \"destinoInstituicaoId\": \"" + abrigo + "\", \"necessidades\": \"Fralda geriátrica\"", 201);
        deslocar(em, idoso, "\"destinoTipo\": \"ABRIGO\", \"destinoDescricao\": \"Outro\"", 409);
        deslocar(em, gestante, "\"destinoTipo\": \"CASA_PARENTE\"", 400);
        deslocar(em, gestante, "\"destinoTipo\": \"CASA_PARENTE\", \"destinoDescricao\": \"Rua Alta, 200\"", 201);
        enviar(post("/api/v1/emergencia/" + em + "/deslocado"), "{\"pacienteId\": \"%s\", \"destinoTipo\": \"UNIDADE_SAUDE\", \"destinoUnidadeId\": \"%s\", "
                .formatted(crianca.getUuid(), hospital.getUuid()) + "\"solicitacaoTransporteId\": \"" + transporteDialise + "\"}", 422);
        l = deslocar(em, dialise, "\"destinoTipo\": \"UNIDADE_SAUDE\", \"destinoUnidadeId\": \"" + hospital.getUuid() + "\", \"solicitacaoTransporteId\": \""
                + transporteDialise + "\"", 201);
        assertThat(JsonPath.parse(l).read("$[?(@.pacienteId == '" + dialise.getUuid() + "')].solicitacaoTransporteId", List.class)).containsExactly(transporteDialise);

        // Os vulneráveis mostram quem está fora; a emergência mostra a ocupação sem nomes.
        String v = enviar(get("/api/v1/emergencia/" + em + "/vulneraveis?marcador=IDOSO"), null, 200);
        assertThat((String) JsonPath.read(v, "$.lista[0].deslocadoPara")).isEqualTo(PREFIXO + "Abrigo Ginasio");
        String d = enviar(get("/api/v1/emergencia/" + em), null, 200);
        assertThat((Integer) JsonPath.read(d, "$.deslocadosEmAberto")).isEqualTo(3);
        assertThat((List<String>) JsonPath.read(d, "$.abrigos[*].destino")).contains(PREFIXO + "Abrigo Ginasio", "Rua Alta, 200");

        // Realocar guarda o caminho; o antigo não se mexe mais.
        String antigo = idDe(l, idoso, "DESLOCADO");
        l = enviar(post("/api/v1/emergencia/" + em + "/deslocado/" + antigo + "/realocacao"), "{\"destinoTipo\": \"UNIDADE_SAUDE\", \"destinoUnidadeId\": \""
                + hospital.getUuid() + "\"}", 201);
        String novo = idDe(l, idoso, "DESLOCADO");
        assertThat(JsonPath.parse(l).read("$[?(@.uuid == '" + novo + "')].anteriorId", List.class)).containsExactly(antigo);
        assertThat(JsonPath.parse(l).read("$[?(@.uuid == '" + novo + "')].origem", List.class)).containsExactly(PREFIXO + "Abrigo Ginasio");
        enviar(post("/api/v1/emergencia/" + em + "/deslocado/" + antigo + "/realocacao"), "{\"destinoTipo\": \"OUTRO\", \"destinoDescricao\": \"X\"}", 422);
        String prontuario = enviar(get("/api/v1/emergencias/deslocamentos?pacienteId=" + idoso.getUuid()), null, 200);
        assertThat((List<String>) JsonPath.read(prontuario, "$[*].destino")).containsExactly(PREFIXO + "Hospital");

        // Retorno e encerramento.
        String g = idDe(l, gestante, "DESLOCADO");
        enviar(post("/api/v1/emergencia/" + em + "/deslocado/" + g + "/retorno"), "{}", 200);
        enviar(post("/api/v1/emergencia/" + em + "/deslocado/" + g + "/retorno"), "{}", 422);
        enviar(post("/api/v1/emergencia/" + em + "/deslocado/" + novo + "/encerramento"), "{}", 400);

        // Encaminhamento ao CRAS (ADR-0154): duplicado, 409; recebido e concluído.
        String dl = idDe(l, dialise, "DESLOCADO");
        String enc = "{\"unidadeId\": \"%s\", \"instituicaoId\": \"%s\", \"motivo\": \"Aluguel social\", \"retornoEsperadoEm\": \"%s\"}"
                .formatted(ubs.getUuid(), cras, hoje.plusDays(10));
        l = enviar(post("/api/v1/emergencia/" + em + "/deslocado/" + dl + "/encaminhamento"), enc, 201);
        enviar(post("/api/v1/emergencia/" + em + "/deslocado/" + dl + "/encaminhamento"), enc, 409);
        String e = JsonPath.parse(l).read("$[?(@.uuid == '" + dl + "')].encaminhamentos[0].uuid", List.class).get(0).toString();
        enviar(post("/api/v1/emergencia/" + em + "/deslocado/" + dl + "/encaminhamento/" + e + "/recebimento"), "{}", 200);
        l = enviar(post("/api/v1/emergencia/" + em + "/deslocado/" + dl + "/encaminhamento/" + e + "/retorno"),
                "{\"desfecho\": \"CONCLUIDO\", \"retorno\": \"Benefício concedido.\"}", 200);
        assertThat(JsonPath.parse(l).read("$[?(@.uuid == '" + dl + "')].encaminhamentos[0].situacao", List.class)).containsExactly("CONCLUIDO");

        // Desmobilização: a pendência dos que seguem fora; não entra deslocado novo; o retorno ainda vale; o relatório conta.
        d = enviar(post("/api/v1/emergencia/" + em + "/desmobilizacao"), "{\"responsavel\": \"Secretário\", \"justificativa\": \"Águas baixando\"}", 200);
        assertThat((List<String>) JsonPath.read(d, "$.pendencias")).anyMatch(p -> p.contains("2 paciente(s) deslocado(s)"));
        deslocar(em, adulto, "\"destinoTipo\": \"OUTRO\", \"destinoDescricao\": \"Escola\"", 422);
        enviar(post("/api/v1/emergencia/" + em + "/deslocado/" + novo + "/encerramento"), "{\"motivo\": \"Mudou-se para outra cidade\"}", 200);
        String rel = enviar(get("/api/v1/emergencia/" + em + "/relatorio"), null, 200);
        assertThat((Integer) JsonPath.read(rel, "$.indicadores.pacientesDeslocados")).isEqualTo(3);
        assertThat((Integer) JsonPath.read(rel, "$.indicadores.deslocadosRetornaram")).isEqualTo(1);
        assertThat((Integer) JsonPath.read(rel, "$.indicadores.deslocadosAindaFora")).isEqualTo(1);
        assertThat((Integer) JsonPath.read(rel, "$.indicadores.abrigosUsados")).isEqualTo(1);
        assertThat((Integer) JsonPath.read(rel, "$.indicadores.encaminhamentosIntersetoriais")).isEqualTo(1);
        assertThat(UUID.fromString(novo)).isNotNull();
    }
}
