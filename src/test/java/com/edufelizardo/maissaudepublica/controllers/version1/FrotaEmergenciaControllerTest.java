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
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

/**
 * Transporte nas emergências, fatia T3 (ADR-0192): a coordenação pede o veículo, a frota cede (com os avisos das viagens a reprogramar,
 * o veículo em serviço e os limites de quantidade, situação e cessão repetida), devolve com a quilometragem; concluir o pedido e a
 * desmobilização devolvem o que restou; o relatório conta os veículos e os quilômetros.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class FrotaEmergenciaControllerTest {

    private static final String PREFIXO = "FrotaEmerg Teste ";
    private static final String TR = "/api/v1/transporte/";
    private static final ZoneId ZONA = ZoneId.systemDefault();

    @Autowired private MockMvc mockMvc;
    @Autowired private JdbcTemplate jdbc;
    @Autowired private UnidadeDeSaudeRepository unidadeRepository;
    @Autowired private PacienteRepository pacienteRepository;
    @Autowired private ProfissionalRepository profissionalRepository;

    private String inundacao;
    private String nivel1;
    private UnidadeDeSaude garagem;
    private UnidadeDeSaude ubs;
    private UnidadeDeSaude hospital;
    private Paciente paciente;

    @BeforeEach
    void seed() {
        limpar();
        inundacao = jdbc.queryForObject("select cast(uuid as varchar) from tb_tipo_emergencia where codigo = 'INUNDACAO'", String.class);
        nivel1 = jdbc.queryForObject("select cast(uuid as varchar) from tb_nivel_emergencia where ordem = 1", String.class);
        garagem = unidade("Garagem");
        ubs = unidade("UBS");
        hospital = unidade("Hospital");
        Paciente p = new Paciente();
        p.setNome(PREFIXO + "Paciente");
        p.setCpf("90190000001");
        p.setDataNascimento(LocalDate.now().minusYears(50));
        p.setAtivo(true);
        Endereco e = new Endereco();
        e.setLogradouro("Rua das Flores");
        e.setNumeroLogradouro("10");
        e.setCidade("Cidade Alfa");
        e.setEstado("SP");
        p.setEndereco(e);
        paciente = pacienteRepository.save(p);
        Profissional c = new Profissional();
        c.setMatricula("FE-COND");
        c.setCpf("90190000099");
        c.setNome(PREFIXO + "Condutor");
        c.setAtivo(true);
        profissionalRepository.save(c);
    }

    @AfterEach
    void limpar() {
        String emergencias = "(select e.uuid from tb_emergencia e join tb_evento_emergencial v on v.uuid = e.evento_id where v.descricao like '" + PREFIXO + "%')";
        String unidades = "(select uuid from tb_unidade_de_saude where nome like '" + PREFIXO + "%')";
        String veiculos = "(select uuid from tb_veiculo where unidade_id in " + unidades + ")";
        String relatorios = "(select uuid from tb_relatorio_pos_evento where emergencia_id in " + emergencias + ")";
        jdbc.update("delete from tb_recomendacao_relatorio where relatorio_id in " + relatorios);
        jdbc.update("delete from tb_relatorio_pos_evento where emergencia_id in " + emergencias);
        jdbc.update("delete from tb_desmobilizacao where emergencia_id in " + emergencias);
        jdbc.update("delete from tb_cessao_veiculo where veiculo_id in " + veiculos);
        jdbc.update("delete from tb_mobilizacao where emergencia_id in " + emergencias);
        jdbc.update("delete from tb_mudanca_nivel_emergencia where emergencia_id in " + emergencias);
        jdbc.update("delete from tb_emergencia where uuid in " + emergencias);
        jdbc.update("delete from tb_evento_emergencial where descricao like '" + PREFIXO + "%'");
        jdbc.update("delete from tb_embarque_viagem where viagem_id in (select uuid from tb_viagem_transporte where veiculo_id in " + veiculos + ")");
        jdbc.update("delete from tb_viagem_transporte where veiculo_id in " + veiculos);
        jdbc.update("delete from tb_solicitacao_transporte where unidade_id in " + unidades);
        jdbc.update("delete from tb_situacao_veiculo where veiculo_id in " + veiculos);
        jdbc.update("delete from tb_veiculo where unidade_id in " + unidades);
        jdbc.update("delete from tb_paciente where nome like ?", PREFIXO + "%");
        jdbc.update("delete from tb_profissional where matricula like 'FE-%'");
        jdbc.update("delete from tb_unidade_de_saude where nome like ?", PREFIXO + "%");
    }

    private UnidadeDeSaude unidade(String nome) {
        UnidadeDeSaude u = new UnidadeDeSaude();
        u.setNome(PREFIXO + nome);
        u.setTipo(TipoUnidadeDeSaude.UBS);
        u.setMunicipio("Cidade Alfa");
        u.setAtivo(true);
        return unidadeRepository.save(u);
    }

    private String enviar(MockHttpServletRequestBuilder r, String corpo, int esperado) throws Exception {
        if (corpo != null) {
            r = r.contentType(MediaType.APPLICATION_JSON).content(corpo);
        }
        var resposta = mockMvc.perform(r).andReturn().getResponse();
        assertThat(resposta.getStatus()).as(resposta.getContentAsString(StandardCharsets.UTF_8)).isEqualTo(esperado);
        return resposta.getContentAsString(StandardCharsets.UTF_8);
    }

    private String emergencia(String nome) throws Exception {
        String evento = JsonPath.read(enviar(post("/api/v1/evento-emergencial"), "{\"tipoId\": \"" + inundacao + "\", \"fonte\": \"DEFESA_CIVIL\", "
                + "\"descricao\": \"" + PREFIXO + nome + "\"}", 201), "$.uuid");
        return JsonPath.read(enviar(post("/api/v1/evento-emergencial/" + evento + "/emergencia"), "{\"nome\": \"" + nome + "\", \"nivelId\": \"" + nivel1
                + "\", \"justificativa\": \"Cota de alerta\"}", 201), "$.uuid");
    }

    private String veiculo(String placa, String prefixo, String tipo) throws Exception {
        return JsonPath.read(enviar(post(TR + "veiculo"), ("{\"placa\": \"%s\", \"prefixo\": \"%s\", \"tipo\": \"%s\", \"unidadeId\": \"%s\", \"lugares\": 6, "
                + "\"quilometragem\": 5000}").formatted(placa, prefixo, tipo, garagem.getUuid()), 201), "$.uuid");
    }

    /** O pedido de veículo; devolve o id da mobilização. */
    private String pedir(String em, String recurso, Integer quantidade) throws Exception {
        String d = enviar(post("/api/v1/emergencia/" + em + "/mobilizacao"), "{\"tipo\": \"VEICULO\", \"destinoId\": \"%s\", \"recurso\": \"%s\"%s}"
                .formatted(ubs.getUuid(), recurso, quantidade == null ? "" : ", \"quantidade\": " + quantidade), 201);
        return JsonPath.parse(d).read("$.mobilizacoes[?(@.recurso == '" + recurso + "')].uuid", List.class).get(0).toString();
    }

    private String ceder(String mob, String veiculo, int esperado) throws Exception {
        return enviar(post(TR + "emergencia/mobilizacao/" + mob + "/cessao"), "{\"veiculoId\": \"%s\"}".formatted(veiculo), esperado);
    }

    private String situacao(String veiculo) {
        return jdbc.queryForObject("select situacao from tb_veiculo where uuid = cast(? as uuid)", String.class, veiculo);
    }

    private String viagem(String veiculo, int dias, int esperado) throws Exception {
        LocalDate amanha = LocalDate.now(ZONA).plusDays(dias);
        String s = JsonPath.read(enviar(post(TR + "solicitacao"), ("{\"pacienteId\": \"%s\", \"unidadeId\": \"%s\", \"motivo\": \"CONSULTA\", \"trecho\": \"IDA_E_VOLTA\", "
                + "\"dataHora\": \"%sT09:00\", \"destinoUnidadeId\": \"%s\"}").formatted(paciente.getUuid(), ubs.getUuid(), amanha, hospital.getUuid()), 201), "$.uuid");
        Instant saida = amanha.atTime(7, 0).atZone(ZONA).toInstant();
        String r = enviar(post(TR + "viagem"), "{\"veiculoId\": \"%s\", \"condutorMatricula\": \"FE-COND\", \"saidaPrevista\": \"%s\", \"retornoPrevisto\": \"%s\", \"solicitacoes\": [\"%s\"]}"
                .formatted(veiculo, saida, saida.plusSeconds(6 * 3600), s), esperado);
        if (esperado != 201) {
            enviar(post(TR + "solicitacao/" + s + "/cancelamento"), "{\"motivo\": \"Teste\"}", 200);
        }
        return r;
    }

    @Test
    void frotaCedeEDevolve() throws Exception {
        String em = emergencia("Enchente Frota");
        String van = veiculo("FEM1A11", "FE-VAN", "VAN_SANITARIA");
        String amb = veiculo("FEM1A12", "FE-AMB", "AMBULANCIA_TRANSPORTE");
        String oficina = veiculo("FEM1A13", "FE-OFI", "CARRO_ADMINISTRATIVO");
        enviar(post(TR + "veiculo/" + oficina + "/situacao"), "{\"situacao\": \"MANUTENCAO\", \"motivo\": \"Freio\"}", 200);
        viagem(van, 1, 201);

        String pedido = pedir(em, "Uma van para remoções", 1);
        String insumo = JsonPath.parse(enviar(post("/api/v1/emergencia/" + em + "/mobilizacao"), "{\"tipo\": \"INSUMO\", \"destinoId\": \"%s\", \"recurso\": \"Lonas\"}"
                .formatted(ubs.getUuid()), 201)).read("$.mobilizacoes[?(@.recurso == 'Lonas')].uuid", List.class).get(0).toString();
        assertThat((List<String>) JsonPath.read(enviar(get(TR + "emergencia/pedidos"), null, 200), "$[*].mobilizacaoId")).contains(pedido).doesNotContain(insumo);

        // Não serve: a mobilização de insumo e o veículo em manutenção.
        ceder(insumo, van, 422);
        ceder(pedido, oficina, 422);
        // Cedida: em serviço, o pedido anda e volta o aviso da viagem programada.
        String r = ceder(pedido, van, 201);
        assertThat((List<String>) JsonPath.read(r, "$.avisos")).hasSize(1);
        String cessao = JsonPath.read(r, "$.cessao.uuid");
        assertThat((Integer) JsonPath.read(r, "$.cessao.kmInicio")).isEqualTo(5000);
        assertThat(situacao(van)).isEqualTo("EM_SERVICO");
        String d = enviar(get("/api/v1/emergencia/" + em), null, 200);
        assertThat(JsonPath.parse(d).read("$.mobilizacoes[?(@.uuid == '" + pedido + "')].situacao", List.class)).containsExactly("EM_ANDAMENTO");
        assertThat((List<String>) JsonPath.read(d, "$.frota.cedidos[*].prefixo")).containsExactly("FE-VAN");

        // A quantidade pedida já foi atendida; a van já cedida não vai para outro pedido; nem para viagem nova.
        ceder(pedido, amb, 422);
        String outro = pedir(em, "Mais uma ambulância", null);
        ceder(outro, van, 409);
        viagem(van, 2, 409);

        // A devolução com a quilometragem: não volta; o veículo fica disponível e com a nova.
        enviar(post(TR + "emergencia/cessao/" + cessao + "/devolucao"), "{\"quilometragem\": 4999}", 422);
        enviar(post(TR + "emergencia/cessao/" + cessao + "/devolucao"), "{\"quilometragem\": 5120}", 200);
        enviar(post(TR + "emergencia/cessao/" + cessao + "/devolucao"), "{}", 422);
        assertThat(situacao(van)).isEqualTo("DISPONIVEL");
        assertThat(jdbc.queryForObject("select quilometragem from tb_veiculo where uuid = cast(? as uuid)", Integer.class, van)).isEqualTo(5120);

        // Concluir o pedido devolve o que ainda estava cedido.
        ceder(outro, amb, 201);
        enviar(post("/api/v1/emergencia/" + em + "/mobilizacao/" + outro + "/situacao"), "{\"situacao\": \"CONCLUIDA\"}", 200);
        assertThat(situacao(amb)).isEqualTo("DISPONIVEL");

        // A desmobilização devolve também.
        String terceiro = pedir(em, "Van de novo", 2);
        ceder(terceiro, van, 201);
        enviar(post("/api/v1/emergencia/" + em + "/desmobilizacao"), "{\"responsavel\": \"Secretário\", \"justificativa\": \"Águas baixando\"}", 200);
        assertThat(situacao(van)).isEqualTo("DISPONIVEL");
        assertThat(jdbc.queryForObject("select count(*) from tb_cessao_veiculo where veiculo_id in (cast(? as uuid), cast(? as uuid)) and devolvida_em is null",
                Integer.class, van, amb)).isZero();
        // Fora da emergência ativa, não se cede.
        ceder(terceiro, amb, 422);

        String rel = enviar(get("/api/v1/emergencia/" + em + "/relatorio"), null, 200);
        assertThat((Integer) JsonPath.read(rel, "$.indicadores.veiculosCedidos")).isEqualTo(2);
        assertThat((Integer) JsonPath.read(rel, "$.indicadores.kmVeiculos")).isEqualTo(120);
    }
}
