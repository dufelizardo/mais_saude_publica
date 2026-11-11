package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.models.Profissional;
import com.edufelizardo.maissaudepublica.models.UnidadeDeSaude;
import com.edufelizardo.maissaudepublica.models.enuns.TipoUnidadeDeSaude;
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
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

/**
 * Estoque nas emergências, fatia ES3 (ADR-0191): o insumo mobilizado pela central vira a requisição urgente e a mobilização segue o
 * estoque; a reserva barra as saídas comuns e é usada pela requisição e pelo consumo da emergência; a transferência avulsa ligada; a
 * desmobilização libera as reservas e o relatório conta o consumo.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class EstoqueEmergenciaControllerTest {

    private static final String PREFIXO = "EstEmerg Teste ";
    private static final String EST = "/api/v1/estoque/";

    @Autowired private MockMvc mockMvc;
    @Autowired private JdbcTemplate jdbc;
    @Autowired private UnidadeDeSaudeRepository unidadeRepository;
    @Autowired private com.edufelizardo.maissaudepublica.repositories.ProfissionalRepository profissionalRepository;

    private String inundacao;
    private String nivel1;
    private UnidadeDeSaude central;
    private UnidadeDeSaude ubs;
    private UnidadeDeSaude deposito;

    @BeforeEach
    void seed() {
        limpar();
        inundacao = jdbc.queryForObject("select cast(uuid as varchar) from tb_tipo_emergencia where codigo = 'INUNDACAO'", String.class);
        nivel1 = jdbc.queryForObject("select cast(uuid as varchar) from tb_nivel_emergencia where ordem = 1", String.class);
        central = unidade("Central");
        Profissional p = new Profissional();
        p.setMatricula("EEM-ALM");
        p.setCpf("91122233355");
        p.setNome(PREFIXO + "Almoxarife");
        p.setAtivo(true);
        profissionalRepository.save(p);
        ubs = unidade("UBS");
        deposito = unidade("Deposito");
        jdbc.update("insert into tb_unidade_capacidade_assistencial (uuid, unidade_id, capacidade_assistencial_id, inicio) select gen_random_uuid(), ?, uuid, ? "
                + "from tb_capacidade_assistencial where codigo = 'CENTRAL_ABASTECIMENTO'", central.getUuid(), LocalDate.now().minusDays(1));
    }

    @AfterEach
    void limpar() {
        String emergencias = "(select e.uuid from tb_emergencia e join tb_evento_emergencial v on v.uuid = e.evento_id where v.descricao like '" + PREFIXO + "%')";
        String unidades = "(select uuid from tb_unidade_de_saude where nome like '" + PREFIXO + "%')";
        String materiais = "(select uuid from tb_material_estoque where nome like '" + PREFIXO + "%')";
        String relatorios = "(select uuid from tb_relatorio_pos_evento where emergencia_id in " + emergencias + ")";
        jdbc.update("delete from tb_recomendacao_relatorio where relatorio_id in " + relatorios);
        jdbc.update("delete from tb_relatorio_pos_evento where emergencia_id in " + emergencias);
        jdbc.update("delete from tb_desmobilizacao where emergencia_id in " + emergencias);
        jdbc.update("delete from tb_mobilizacao where emergencia_id in " + emergencias);
        jdbc.update("delete from tb_reserva_estoque where material_id in " + materiais);
        jdbc.update("delete from tb_item_transferencia_estoque where transferencia_id in (select uuid from tb_transferencia_estoque where origem_id in " + unidades + ")");
        jdbc.update("delete from tb_transferencia_estoque where origem_id in " + unidades);
        jdbc.update("delete from tb_item_requisicao_estoque where requisicao_id in (select uuid from tb_requisicao_estoque where unidade_id in " + unidades + ")");
        jdbc.update("delete from tb_requisicao_estoque where unidade_id in " + unidades);
        jdbc.update("delete from tb_movimentacao_estoque where lote_id in (select uuid from tb_lote_material where material_id in " + materiais + ")");
        jdbc.update("delete from tb_lote_material where material_id in " + materiais);
        jdbc.update("delete from tb_material_estoque where nome like '" + PREFIXO + "%'");
        jdbc.update("delete from tb_mudanca_nivel_emergencia where emergencia_id in " + emergencias);
        jdbc.update("delete from tb_emergencia where uuid in " + emergencias);
        jdbc.update("delete from tb_evento_emergencial where descricao like '" + PREFIXO + "%'");
        jdbc.update("delete from tb_unidade_capacidade_assistencial where unidade_id in " + unidades);
        jdbc.update("delete from tb_profissional where matricula = 'EEM-ALM'");
        jdbc.update("delete from tb_unidade_de_saude where nome like '" + PREFIXO + "%'");
    }

    private UnidadeDeSaude unidade(String nome) {
        UnidadeDeSaude u = new UnidadeDeSaude();
        u.setNome(PREFIXO + nome);
        u.setTipo(TipoUnidadeDeSaude.UBS);
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

    private String emergencia() throws Exception {
        String evento = JsonPath.read(enviar(post("/api/v1/evento-emergencial"), "{\"tipoId\": \"" + inundacao + "\", \"fonte\": \"DEFESA_CIVIL\", "
                + "\"descricao\": \"" + PREFIXO + "Rio transbordou\"}", 201), "$.uuid");
        return JsonPath.read(enviar(post("/api/v1/evento-emergencial/" + evento + "/emergencia"), "{\"nome\": \"Enchente Estoque\", \"nivelId\": \"" + nivel1
                + "\", \"justificativa\": \"Cota de alerta\"}", 201), "$.uuid");
    }

    private String material(String codigo, String nome) throws Exception {
        return JsonPath.read(enviar(post(EST + "material"), ("{\"codigo\": \"%s\", \"nome\": \"%s%s\", \"categoria\": \"MEDICO_HOSPITALAR\", "
                + "\"unidadeMedida\": \"UNIDADE\", \"controlaLote\": true}").formatted(codigo, PREFIXO, nome), 201), "$.uuid");
    }

    private void entrada(UnidadeDeSaude u, String material, int qtd, String lote) throws Exception {
        enviar(post(EST + "entrada"), ("{\"unidadeId\": \"%s\", \"materialId\": \"%s\", \"quantidade\": %d, \"origem\": \"DOACAO\", \"numeroLote\": \"%s\", "
                + "\"validade\": \"%s\", \"custoUnitario\": 2.50}").formatted(u.getUuid(), material, qtd, lote, LocalDate.now().plusDays(200)), 201);
    }

    private String reservar(String em, UnidadeDeSaude u, String material, int qtd, int esperado) throws Exception {
        return enviar(post(EST + "reserva"), "{\"emergenciaId\": \"%s\", \"unidadeId\": \"%s\", \"materialId\": \"%s\", \"quantidade\": %d, \"justificativa\": \"Kits da enchente\"}"
                .formatted(em, u.getUuid(), material, qtd), esperado);
    }

    private String saida(UnidadeDeSaude u, String material, int qtd, String emergencia, int esperado) throws Exception {
        return enviar(post(EST + "saida"), "{\"unidadeId\": \"%s\", \"materialId\": \"%s\", \"quantidade\": %d%s}"
                .formatted(u.getUuid(), material, qtd, emergencia == null ? "" : ", \"emergenciaId\": \"" + emergencia + "\""), esperado);
    }

    private int item(UnidadeDeSaude u, String material, String campo) throws Exception {
        List<Integer> v = JsonPath.read(enviar(get(EST + "saldo?unidadeId=" + u.getUuid()), null, 200), "$.itens[?(@.materialId == '" + material + "')]." + campo);
        return v.get(0);
    }

    private String mobilizacao(String detalhe, String material) {
        return JsonPath.parse(detalhe).read("$.mobilizacoes[?(@.materialId == '" + material + "')].uuid", List.class).get(0).toString();
    }

    @Test
    void insumoPelaCentralUsaAReservaDaEmergencia() throws Exception {
        String em = emergencia();
        String kit = material("EEM-KIT", "Kit hidratacao");
        entrada(central, kit, 100, "K1");

        // A central reserva 60 para a emergência: o disponível cai e a reserva não passa do livre.
        String reserva = JsonPath.read(reservar(em, central, kit, 60, 201), "$.uuid");
        assertThat(item(central, kit, "reservado")).isEqualTo(60);
        assertThat(item(central, kit, "disponivel")).isEqualTo(40);
        reservar(em, central, kit, 41, 422);

        // A coordenação mobiliza 70 kits da central para a UBS: nasce a requisição urgente, já enviada e ligada.
        enviar(post("/api/v1/emergencia/" + em + "/mobilizacao"), "{\"tipo\": \"MEDICAMENTO\", \"destinoId\": \"%s\", \"recurso\": \"X\", \"materialId\": \"%s\", \"quantidade\": 1}"
                .formatted(ubs.getUuid(), kit), 422);
        enviar(post("/api/v1/emergencia/" + em + "/mobilizacao"), "{\"tipo\": \"INSUMO\", \"origemId\": \"%s\", \"destinoId\": \"%s\", \"materialId\": \"%s\"}"
                .formatted(central.getUuid(), ubs.getUuid(), kit), 400);
        String d = enviar(post("/api/v1/emergencia/" + em + "/mobilizacao"), ("{\"tipo\": \"INSUMO\", \"origemId\": \"%s\", \"destinoId\": \"%s\", \"materialId\": \"%s\", "
                + "\"quantidade\": 70, \"responsavel\": \"Almoxarifado\"}").formatted(central.getUuid(), ubs.getUuid(), kit), 201);
        String mob = mobilizacao(d, kit);
        String req = JsonPath.parse(d).read("$.mobilizacoes[?(@.uuid == '" + mob + "')].requisicaoEstoqueId", List.class).get(0).toString();
        assertThat(JsonPath.parse(d).read("$.mobilizacoes[?(@.uuid == '" + mob + "')].situacao", List.class)).containsExactly("SOLICITADA");
        assertThat(JsonPath.parse(d).read("$.mobilizacoes[?(@.uuid == '" + mob + "')].recurso", List.class)).containsExactly(PREFIXO + "Kit hidratacao");
        String r = enviar(get(EST + "requisicao/" + req), null, 200);
        assertThat((String) JsonPath.read(r, "$.situacao")).isEqualTo("ENVIADA");
        assertThat((String) JsonPath.read(r, "$.prioridade")).isEqualTo("URGENTE");
        assertThat((String) JsonPath.read(r, "$.emergenciaId")).isEqualTo(em);
        // A situação segue o estoque: não muda à mão.
        enviar(post("/api/v1/emergencia/" + em + "/mobilizacao/" + mob + "/situacao"), "{\"situacao\": \"CONCLUIDA\"}", 422);

        // Uma requisição comum de 50 não passa: só 40 estão livres.
        String comum = JsonPath.read(enviar(post(EST + "requisicao"), "{\"profissionalMatricula\": \"EEM-ALM\", \"unidadeId\": \"%s\", \"centralId\": \"%s\", \"itens\": [{\"materialId\": \"%s\", \"quantidade\": 50}]}"
                .formatted(deposito.getUuid(), central.getUuid(), kit), 201), "$.uuid");
        enviar(post(EST + "requisicao/" + comum + "/envio"), "{\"profissionalMatricula\": \"EEM-ALM\"}", 200);
        // A da emergência vem no alto da fila da central.
        assertThat((String) JsonPath.read(enviar(get(EST + "requisicao?centralId=" + central.getUuid()), null, 200), "$[0].uuid")).isEqualTo(req);
        enviar(post(EST + "requisicao/" + comum + "/atendimento"), "{\"profissionalMatricula\": \"EEM-ALM\", \"itens\": [{\"materialId\": \"%s\", \"quantidade\": 50}]}".formatted(kit), 422);

        // A requisição da emergência usa a reserva: 70 saem, a reserva fica consumida e a mobilização anda.
        String atendida = enviar(post(EST + "requisicao/" + req + "/atendimento"), "{\"profissionalMatricula\": \"EEM-ALM\", \"itens\": [{\"materialId\": \"%s\", \"quantidade\": 70}]}".formatted(kit), 200);
        assertThat(jdbc.queryForObject("select situacao from tb_reserva_estoque where uuid = cast(? as uuid)", String.class, reserva)).isEqualTo("CONSUMIDA");
        assertThat(item(central, kit, "reservado")).isZero();
        d = enviar(get("/api/v1/emergencia/" + em), null, 200);
        assertThat(JsonPath.parse(d).read("$.mobilizacoes[?(@.uuid == '" + mob + "')].situacao", List.class)).containsExactly("EM_ANDAMENTO");
        String transferencia = JsonPath.read(atendida, "$.transferenciaId");
        String itemT = JsonPath.read(enviar(get(EST + "transferencia/" + transferencia), null, 200), "$.itens[0].uuid");
        enviar(post(EST + "transferencia/" + transferencia + "/recebimento"), "{\"profissionalMatricula\": \"EEM-ALM\", \"itens\": [{\"itemId\": \"%s\", \"quantidade\": 70}]}".formatted(itemT), 200);
        d = enviar(get("/api/v1/emergencia/" + em), null, 200);
        assertThat(JsonPath.parse(d).read("$.mobilizacoes[?(@.uuid == '" + mob + "')].situacao", List.class)).containsExactly("CONCLUIDA");
        // Sem reserva, a comum agora passa no que sobrou.
        enviar(post(EST + "requisicao/" + comum + "/atendimento"), "{\"profissionalMatricula\": \"EEM-ALM\", \"itens\": [{\"materialId\": \"%s\", \"quantidade\": 30}]}".formatted(kit), 200);

        // Cancelar a mobilização ainda não atendida cancela a requisição junto.
        d = enviar(post("/api/v1/emergencia/" + em + "/mobilizacao"), ("{\"tipo\": \"INSUMO\", \"origemId\": \"%s\", \"destinoId\": \"%s\", \"materialId\": \"%s\", "
                + "\"quantidade\": 5, \"recurso\": \"Segundo lote\"}").formatted(central.getUuid(), deposito.getUuid(), kit), 201);
        String segunda = JsonPath.parse(d).read("$.mobilizacoes[?(@.recurso == 'Segundo lote')].uuid", List.class).get(0).toString();
        String req2 = JsonPath.parse(d).read("$.mobilizacoes[?(@.uuid == '" + segunda + "')].requisicaoEstoqueId", List.class).get(0).toString();
        enviar(post("/api/v1/emergencia/" + em + "/mobilizacao/" + segunda + "/situacao"), "{\"situacao\": \"CANCELADA\"}", 400);
        d = enviar(post("/api/v1/emergencia/" + em + "/mobilizacao/" + segunda + "/situacao"), "{\"situacao\": \"CANCELADA\", \"motivo\": \"Chegou doação\"}", 200);
        assertThat(JsonPath.parse(d).read("$.mobilizacoes[?(@.uuid == '" + segunda + "')].situacao", List.class)).containsExactly("CANCELADA");
        assertThat((String) JsonPath.read(enviar(get(EST + "requisicao/" + req2), null, 200), "$.situacao")).isEqualTo("CANCELADA");
    }

    @Test
    void consumoExtraordinarioTransferenciaLigadaEDesmobilizacao() throws Exception {
        String em = emergencia();
        String soro = material("EEM-SORO", "Soro oral");
        entrada(ubs, soro, 20, "S1");
        entrada(deposito, soro, 10, "S2");

        // Reserva na UBS: a saída comum e a perda de lote válido não tiram do reservado.
        String reserva = JsonPath.read(reservar(em, ubs, soro, 15, 201), "$.uuid");
        saida(ubs, soro, 10, null, 422);
        saida(ubs, soro, 5, null, 201);
        String lote = jdbc.queryForObject("select cast(uuid as varchar) from tb_lote_material where unidade_id = ? and numero_lote = 'S1'", String.class, ubs.getUuid());
        enviar(post(EST + "perda"), "{\"loteId\": \"%s\", \"quantidade\": 1, \"motivo\": \"AVARIA\"}".formatted(lote), 422);

        // O consumo da emergência usa a reserva e fica marcado.
        saida(ubs, soro, 4, em, 201);
        assertThat(jdbc.queryForObject("select consumida from tb_reserva_estoque where uuid = cast(? as uuid)", Integer.class, reserva)).isEqualTo(4);
        assertThat(item(ubs, soro, "reservado")).isEqualTo(11);
        String d = enviar(get("/api/v1/emergencia/" + em), null, 200);
        assertThat((Integer) JsonPath.read(d, "$.estoque.quantidadeConsumida")).isEqualTo(4);
        assertThat(((Number) JsonPath.read(d, "$.estoque.valorConsumido")).doubleValue()).isEqualTo(10.0);
        assertThat((Integer) JsonPath.read(d, "$.estoque.reservas[0].restante")).isEqualTo(11);
        assertThat((List<String>) JsonPath.read(enviar(get(EST + "reserva?emergenciaId=" + em), null, 200), "$[*].uuid")).containsExactly(reserva);

        // Insumo sem origem central fica solicitado e se liga a uma transferência avulsa para o mesmo destino.
        d = enviar(post("/api/v1/emergencia/" + em + "/mobilizacao"), "{\"tipo\": \"INSUMO\", \"origemId\": \"%s\", \"destinoId\": \"%s\", \"materialId\": \"%s\", \"quantidade\": 6}"
                .formatted(deposito.getUuid(), ubs.getUuid(), soro), 201);
        String mob = mobilizacao(d, soro);
        assertThat(JsonPath.parse(d).read("$.mobilizacoes[?(@.uuid == '" + mob + "')].requisicaoEstoqueId", List.class)).containsOnlyNulls();
        String loteDeposito = jdbc.queryForObject("select cast(uuid as varchar) from tb_lote_material where unidade_id = ? and numero_lote = 'S2'", String.class,
                deposito.getUuid());
        String paraCentral = JsonPath.read(enviar(post(EST + "transferencia"), "{\"profissionalMatricula\": \"EEM-ALM\", \"unidadeId\": \"%s\", \"destinoId\": \"%s\", \"itens\": [{\"loteId\": \"%s\", \"quantidade\": 1}]}"
                .formatted(deposito.getUuid(), central.getUuid(), loteDeposito), 201), "$.uuid");
        String paraUbs = JsonPath.read(enviar(post(EST + "transferencia"), "{\"profissionalMatricula\": \"EEM-ALM\", \"unidadeId\": \"%s\", \"destinoId\": \"%s\", \"itens\": [{\"loteId\": \"%s\", \"quantidade\": 6}]}"
                .formatted(deposito.getUuid(), ubs.getUuid(), loteDeposito), 201), "$.uuid");
        enviar(put("/api/v1/emergencia/" + em + "/mobilizacao/" + mob + "/transferencia"), "{\"transferenciaId\": \"%s\"}".formatted(paraCentral), 422);
        d = enviar(put("/api/v1/emergencia/" + em + "/mobilizacao/" + mob + "/transferencia"), "{\"transferenciaId\": \"%s\"}".formatted(paraUbs), 200);
        assertThat(JsonPath.parse(d).read("$.mobilizacoes[?(@.uuid == '" + mob + "')].situacao", List.class)).containsExactly("EM_ANDAMENTO");
        assertThat(JsonPath.parse(d).read("$.mobilizacoes[?(@.uuid == '" + mob + "')].transferenciaEstoqueSituacao", List.class)).containsExactly("EM_TRANSITO");

        // A desmobilização libera a reserva; a ligada segue o estoque; o relatório conta o consumo.
        enviar(post("/api/v1/emergencia/" + em + "/desmobilizacao"), "{\"responsavel\": \"Secretário\", \"justificativa\": \"Águas baixando\"}", 200);
        assertThat(jdbc.queryForObject("select situacao from tb_reserva_estoque where uuid = cast(? as uuid)", String.class, reserva)).isEqualTo("LIBERADA");
        assertThat(item(ubs, soro, "reservado")).isZero();
        assertThat(jdbc.queryForObject("select situacao from tb_mobilizacao where uuid = cast(? as uuid)", String.class, mob)).isEqualTo("SOLICITADA");
        String rel = enviar(get("/api/v1/emergencia/" + em + "/relatorio"), null, 200);
        assertThat((Integer) JsonPath.read(rel, "$.indicadores.insumosConsumidos")).isEqualTo(4);
        assertThat(((Number) JsonPath.read(rel, "$.indicadores.valorInsumos")).doubleValue()).isEqualTo(10.0);
        // Em desmobilização ainda se consome pela emergência; reservar, não.
        saida(ubs, soro, 1, em, 201);
        reservar(em, ubs, soro, 1, 422);
        enviar(post(EST + "reserva/" + reserva + "/liberacao"), "{\"motivo\": \"De novo\"}", 422);
    }
}
