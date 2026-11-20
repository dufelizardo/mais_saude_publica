package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.models.Endereco;
import com.edufelizardo.maissaudepublica.models.Paciente;
import com.edufelizardo.maissaudepublica.models.UnidadeDeSaude;
import com.edufelizardo.maissaudepublica.models.enuns.TipoUnidadeDeSaude;
import com.edufelizardo.maissaudepublica.repositories.PacienteRepository;
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
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

/**
 * Financeiro, fatia F3 (ADR-0220): o centro geral de cada unidade e o do setor; a apuração do mês encerrado com a folha pela lotação, o consumo
 * do Estoque no setor, a saída sem custo e a depreciação do bem; refazer a aberta, fechar e não mudar mais; os indicadores com o custo por
 * atendimento e a média do tipo de unidade. F4 (ADR-0221): o rateio em degraus, o critério e a área, o recurso ao custo direto, o não rateado e a
 * unidade mista.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CustosControllerTest {

    private static final String PREFIXO = "Custos Teste ";
    private static final String API = "/api/v1/financeiro/";

    @Autowired private MockMvc mockMvc;
    @Autowired private JdbcTemplate jdbc;
    @Autowired private UnidadeDeSaudeRepository unidadeRepository;
    @Autowired private PacienteRepository pacienteRepository;

    private final YearMonth mes = YearMonth.now(ZoneId.of("America/Sao_Paulo")).minusMonths(1);
    private UnidadeDeSaude ubs;
    private UnidadeDeSaude ubs2;
    private UUID setor;

    @BeforeEach
    void seed() {
        limpar();
        ubs = unidade("UBS Centro");
        ubs2 = unidade("UBS Norte");
        setor = UUID.randomUUID();
        jdbc.update("insert into tb_setor (uuid, unidade_id, nome, codigo, tipo, ativo) values (?, ?, ?, 'CUS-ENF', 'ASSISTENCIAL', true)",
                setor, ubs.getUuid(), PREFIXO + "Enfermagem");
    }

    @AfterEach
    void limpar() {
        String apuracao = "(select uuid from tb_apuracao_custo where competencia = '" + mes + "')";
        jdbc.update("delete from tb_linha_apuracao_custo where apuracao_id in " + apuracao);
        jdbc.update("delete from tb_rateio_apuracao_custo where apuracao_id in " + apuracao);
        jdbc.update("delete from tb_custo_centro_apuracao where apuracao_id in " + apuracao);
        jdbc.update("delete from tb_producao_apuracao_custo where apuracao_id in " + apuracao);
        jdbc.update("delete from tb_apuracao_custo where uuid in " + apuracao);
        String profissionais = "(select uuid from tb_profissional where matricula like 'CUS-%')";
        jdbc.update("delete from tb_atendimento where profissional_id in " + profissionais);
        jdbc.update("delete from tb_leito where identificacao like 'CUS-%'");
        jdbc.update("delete from tb_paciente where nome like ?", PREFIXO + "%");
        jdbc.update("delete from tb_folha_pagamento where profissional_id in " + profissionais);
        jdbc.update("delete from tb_lotacao where profissional_id in " + profissionais);
        jdbc.update("delete from tb_profissional where matricula like 'CUS-%'");
        jdbc.update("delete from tb_cargo where nome like ?", PREFIXO + "%");
        jdbc.update("delete from tb_categoria_salarial where nome like ?", PREFIXO + "%");
        ComprasControllerTest.limparDados(jdbc, PREFIXO);
    }

    private UnidadeDeSaude unidade(String nome) {
        UnidadeDeSaude u = new UnidadeDeSaude();
        u.setNome(PREFIXO + nome);
        u.setTipo(TipoUnidadeDeSaude.UBS);
        u.setAtivo(true);
        return unidadeRepository.save(u);
    }

    private String enviar(MockHttpServletRequestBuilder r, String corpo, int esperado) throws Exception {
        var resposta = mockMvc.perform(r.contentType(MediaType.APPLICATION_JSON).content(corpo)).andReturn().getResponse();
        String texto = resposta.getContentAsString(StandardCharsets.UTF_8);
        assertThat(resposta.getStatus()).as(texto).isEqualTo(esperado);
        return texto;
    }

    private String ler(String url, int esperado) throws Exception {
        var resposta = mockMvc.perform(get(url)).andReturn().getResponse();
        String texto = resposta.getContentAsString(StandardCharsets.UTF_8);
        assertThat(resposta.getStatus()).as(texto).isEqualTo(esperado);
        return texto;
    }

    private static double numero(String json, String caminho) {
        Object v = JsonPath.read(json, caminho);
        return v instanceof List<?> l ? ((Number) l.get(0)).doubleValue() : ((Number) v).doubleValue();
    }

    @Test
    void centrosDeCusto() throws Exception {
        String lista = ler(API + "centros-custo", 200);
        String geral = "$[?(@.unidadeId == '" + ubs.getUuid() + "' && @.geral == true)]";
        assertThat((List<?>) JsonPath.read(lista, geral)).hasSize(1);
        assertThat(JsonPath.read(lista, geral + ".tipo").toString()).contains("PRODUTIVO");
        String geralId = ((List<?>) JsonPath.read(lista, geral + ".uuid")).get(0).toString();

        enviar(post(API + "centro-custo"), "{\"setorId\": \"" + UUID.randomUUID() + "\"}", 404);
        enviar(post(API + "centro-custo"), "{}", 400);
        String c = enviar(post(API + "centro-custo"), "{\"setorId\": \"" + setor + "\"}", 201);
        assertThat((String) JsonPath.read(c, "$.nome")).isEqualTo(PREFIXO + "Enfermagem");
        assertThat((String) JsonPath.read(c, "$.tipo")).isEqualTo("PRODUTIVO");
        assertThat((String) JsonPath.read(c, "$.codigo")).startsWith("CC-");
        enviar(post(API + "centro-custo"), "{\"setorId\": \"" + setor + "\"}", 409);

        enviar(put(API + "centro-custo/" + geralId), "{\"nome\": \"Geral\", \"tipo\": \"PRODUTIVO\", \"ativo\": false}", 422);
        enviar(put(API + "centro-custo/" + geralId), "{\"nome\": \"Geral\", \"tipo\": \"PRODUTIVO\", \"ativo\": true, \"criterioRateio\": \"AREA\"}", 422);
        enviar(put(API + "centro-custo/" + geralId), "{\"nome\": \"Geral\", \"tipo\": \"PRODUTIVO\", \"ativo\": true, \"areaM2\": -1}", 400);
        assertThat(JsonPath.read(c, "$.criterioRateio") == null).isTrue();
        String alt = enviar(put(API + "centro-custo/" + JsonPath.read(c, "$.uuid")), "{\"nome\": \"Sala de vacina\", \"tipo\": \"APOIO\", \"ativo\": true}", 200);
        assertThat((String) JsonPath.read(alt, "$.tipo")).isEqualTo("APOIO");
        assertThat((String) JsonPath.read(alt, "$.criterioRateio")).isEqualTo("CUSTO_DIRETO");
    }

    @Test
    void apuracaoDoMes() throws Exception {
        LocalDate dia = mes.atDay(15);
        String centro = JsonPath.read(enviar(post(API + "centro-custo"), "{\"setorId\": \"" + setor + "\"}", 201), "$.uuid");

        // Pessoal: a folha de 3.000 pela lotação vigente na UBS Centro.
        UUID cat = UUID.randomUUID(), cargo = UUID.randomUUID(), prof = UUID.randomUUID();
        jdbc.update("insert into tb_categoria_salarial (uuid, nome) values (?, ?)", cat, PREFIXO + "Categoria");
        jdbc.update("insert into tb_cargo (uuid, nome, categoria_salarial_id) values (?, ?, ?)", cargo, PREFIXO + "Enfermeiro", cat);
        jdbc.update("insert into tb_profissional (uuid, matricula, cpf, nome, ativo) values (?, 'CUS-1', '90033420001', ?, true)", prof, PREFIXO + "Enfermeira");
        jdbc.update("insert into tb_lotacao (uuid, profissional_id, unidade_id, cargo_id, data_inicio) values (?, ?, ?, ?, ?)",
                UUID.randomUUID(), prof, ubs.getUuid(), cargo, mes.atDay(1).minusYears(1));
        jdbc.update("insert into tb_folha_pagamento (uuid, profissional_id, competencia, proventos, descontos, encargos, total) values (?, ?, ?, 2500, 300, 800, 3000)",
                UUID.randomUUID(), prof, String.format("%02d/%d", mes.getMonthValue(), mes.getYear()));

        // Material: 10 a R$ 2,50 no setor (vai ao centro do setor); uma saída de lote sem custo.
        UUID mat = UUID.randomUUID(), lote = UUID.randomUUID(), semCusto = UUID.randomUUID();
        jdbc.update("insert into tb_material_estoque (uuid, codigo, nome, categoria, unidade_medida, ativo, controla_lote) values (?, 'CUS-LUVA', ?, "
                + "'EPI', 'CAIXA', true, false)", mat, PREFIXO + "Luva");
        Timestamp quando = Timestamp.valueOf(dia.atTime(12, 0));
        for (Object[] l : new Object[][]{{lote, 2.5}, {semCusto, null}}) {
            jdbc.update("insert into tb_lote_material (uuid, material_id, unidade_id, quantidade, custo_unitario, criado_em) values (?, ?, ?, 100, ?, ?)",
                    l[0], mat, ubs.getUuid(), l[1], quando);
            jdbc.update("insert into tb_movimentacao_estoque (uuid, lote_id, tipo, quantidade, saldo_apos, setor_id, registrado_em) values (?, ?, "
                    + "'SAIDA_CONSUMO', -10, 90, ?, ?)", UUID.randomUUID(), l[0], setor, quando);
        }

        // Depreciação: o computador de 6.000 na UBS Norte, tombado há dois anos (5 anos, residual de 10%: 90 por mês).
        jdbc.update("insert into tb_bem_patrimonial (uuid, unidade_id, descricao, classe, origem, situacao, numero_tombamento, valor_aquisicao, "
                + "data_aquisicao, registrado_em) values (?, ?, ?, 'INFORMATICA', 'COMPRA', 'EM_USO', 'CUS-0001', 6000, ?, now())",
                UUID.randomUUID(), ubs2.getUuid(), PREFIXO + "Computador", mes.atDay(1).minusYears(2));

        // Produção: dois atendimentos concluídos na UBS Centro.
        Paciente p = new Paciente();
        p.setNome(PREFIXO + "Paciente");
        p.setCpf("90033420011");
        p.setDataNascimento(dia.minusYears(30));
        p.setAtivo(true);
        Endereco e = new Endereco();
        e.setLogradouro("Rua A");
        e.setNumeroLogradouro("1");
        e.setCidade("Cidade");
        e.setEstado("SP");
        p.setEndereco(e);
        p = pacienteRepository.save(p);
        for (int i = 0; i < 2; i++) {
            jdbc.update("insert into tb_atendimento (uuid, data_hora, paciente_id, profissional_id, unidade_id, status) values (?, ?, ?, ?, ?, 'CONCLUIDO')",
                    UUID.randomUUID(), Timestamp.valueOf(dia.atTime(9 + i, 0)), p.getUuid(), prof, ubs.getUuid());
        }

        enviar(post(API + "apuracao-custo"), "{\"competencia\": \"" + YearMonth.now(ZoneId.of("America/Sao_Paulo")) + "\"}", 422);
        enviar(post(API + "apuracao-custo"), "{\"competencia\": \"2026-13\"}", 400);
        ler(API + "apuracao-custo?competencia=1990-01", 404);
        String a = enviar(post(API + "apuracao-custo"), "{\"competencia\": \"" + mes + "\"}", 200);
        assertThat((String) JsonPath.read(a, "$.situacao")).isEqualTo("ABERTA");
        assertThat(numero(a, "$.saidasSemCusto")).isGreaterThanOrEqualTo(1.0);
        String daUbs = "$.centros[?(@.unidadeId == '" + ubs.getUuid() + "' && @.setorNome == null)]";
        assertThat(numero(a, daUbs + ".valores.PESSOAL")).isEqualTo(3000.0);
        assertThat(numero(a, "$.centros[?(@.centroId == '" + centro + "')].valores.MATERIAL_CONSUMO")).isEqualTo(25.0);
        assertThat(numero(a, "$.centros[?(@.unidadeId == '" + ubs2.getUuid() + "')].valores.DEPRECIACAO")).isEqualTo(90.0);

        // Refazer a aberta dá o mesmo; a lista mostra a competência.
        String b = enviar(post(API + "apuracao-custo"), "{\"competencia\": \"" + mes + "\"}", 200);
        assertThat(numero(b, daUbs + ".valores.PESSOAL")).isEqualTo(3000.0);
        assertThat(JsonPath.read(ler(API + "apuracoes-custo", 200), "$[?(@.competencia == '" + mes + "')].situacao").toString()).contains("ABERTA");

        // Indicadores: 3.025 na UBS Centro por 2 atendimentos.
        String ind = ler(API + "indicadores-custo?competencia=" + mes, 200);
        String linha = "$.unidades[?(@.unidadeId == '" + ubs.getUuid() + "')]";
        assertThat(numero(ind, linha + ".total")).isEqualTo(3025.0);
        assertThat(numero(ind, linha + ".atendimentos")).isEqualTo(2.0);
        assertThat(numero(ind, linha + ".custoPorAtendimento")).isEqualTo(1512.5);
        assertThat(JsonPath.read(ind, "$.unidades[?(@.unidadeId == '" + ubs2.getUuid() + "')].custoPorAtendimento").toString()).contains("null");

        // Fechada, não muda.
        String id = JsonPath.read(a, "$.uuid");
        assertThat((String) JsonPath.read(enviar(post(API + "apuracao-custo/" + id + "/fechamento"), "", 200), "$.situacao")).isEqualTo("FECHADA");
        enviar(post(API + "apuracao-custo/" + id + "/fechamento"), "", 422);
        enviar(post(API + "apuracao-custo"), "{\"competencia\": \"" + mes + "\"}", 422);
    }

    private UUID setor(String codigo, String nome, String tipo) {
        UUID id = UUID.randomUUID();
        jdbc.update("insert into tb_setor (uuid, unidade_id, nome, codigo, tipo, ativo) values (?, ?, ?, ?, ?, true)", id, ubs.getUuid(), PREFIXO + nome, codigo, tipo);
        return id;
    }

    /** Um profissional lotado no setor (ou no geral), com a jornada e a folha da competência. */
    private void lotado(String matricula, UUID cargo, UUID setorId, int horas, int folha) {
        UUID prof = UUID.randomUUID();
        jdbc.update("insert into tb_profissional (uuid, matricula, cpf, nome, ativo) values (?, ?, '90033420002', ?, true)", prof, matricula, PREFIXO + matricula);
        jdbc.update("insert into tb_lotacao (uuid, profissional_id, unidade_id, cargo_id, setor_id, jornada_semanal_horas, data_inicio) values (?, ?, ?, ?, ?, ?, ?)",
                UUID.randomUUID(), prof, ubs.getUuid(), cargo, setorId, horas, mes.atDay(1).minusYears(1));
        jdbc.update("insert into tb_folha_pagamento (uuid, profissional_id, competencia, proventos, descontos, encargos, total) values (?, ?, ?, ?, 0, 0, ?)",
                UUID.randomUUID(), prof, String.format("%02d/%d", mes.getMonthValue(), mes.getYear()), folha, folha);
    }

    /**
     * Na UBS Centro: a administração (1.000 de folha, 40 h) rateia por pessoas para o geral (10 h) e a enfermagem (30 h): 250 e 750. Depois a
     * limpeza (100 de material), por área, para o geral (100 m²) e a enfermagem (300 m²): 25 e 75; a administração, que já rateou, não recebe.
     * A enfermagem tem leito: o custo dela vai ao paciente-dia e o resto ao atendimento. Na UBS Norte, o apoio sem destino fica não rateado.
     */
    @Test
    void rateioEmDegraus() throws Exception {
        UUID adm = setor("CUS-ADM", "Administração", "ADMINISTRATIVO");
        UUID limp = setor("CUS-LIM", "Limpeza", "APOIO");
        String cAdm = JsonPath.read(enviar(post(API + "centro-custo"), "{\"setorId\": \"" + adm + "\"}", 201), "$.uuid");
        String cLimp = JsonPath.read(enviar(post(API + "centro-custo"), "{\"setorId\": \"" + limp + "\", \"criterioRateio\": \"AREA\"}", 201), "$.uuid");
        String cEnf = JsonPath.read(enviar(post(API + "centro-custo"), "{\"setorId\": \"" + setor + "\", \"areaM2\": 300}", 201), "$.uuid");
        String lista = ler(API + "centros-custo", 200);
        assertThat(JsonPath.read(lista, "$[?(@.uuid == '" + cAdm + "')].criterioRateio").toString()).contains("PESSOAS");
        String geralId = ((List<?>) JsonPath.read(lista, "$[?(@.unidadeId == '" + ubs.getUuid() + "' && @.geral == true)].uuid")).get(0).toString();
        enviar(put(API + "centro-custo/" + geralId), "{\"nome\": \"Geral\", \"tipo\": \"PRODUTIVO\", \"ativo\": true, \"areaM2\": 100}", 200);
        String geralNorte = ((List<?>) JsonPath.read(lista, "$[?(@.unidadeId == '" + ubs2.getUuid() + "' && @.geral == true)].uuid")).get(0).toString();
        enviar(put(API + "centro-custo/" + geralNorte), "{\"nome\": \"Geral\", \"tipo\": \"APOIO\", \"ativo\": true}", 200);

        UUID cat = UUID.randomUUID(), cargo = UUID.randomUUID();
        jdbc.update("insert into tb_categoria_salarial (uuid, nome) values (?, ?)", cat, PREFIXO + "Categoria");
        jdbc.update("insert into tb_cargo (uuid, nome, categoria_salarial_id) values (?, ?, ?)", cargo, PREFIXO + "Cargo", cat);
        lotado("CUS-ADM", cargo, adm, 40, 1000);
        lotado("CUS-ENF", cargo, setor, 30, 2000);
        lotado("CUS-GER", cargo, null, 10, 3000);

        UUID mat = UUID.randomUUID(), lote = UUID.randomUUID();
        jdbc.update("insert into tb_material_estoque (uuid, codigo, nome, categoria, unidade_medida, ativo, controla_lote) values (?, 'CUS-SAB', ?, "
                + "'EPI', 'CAIXA', true, false)", mat, PREFIXO + "Sabão");
        Timestamp quando = Timestamp.valueOf(mes.atDay(10).atTime(12, 0));
        jdbc.update("insert into tb_lote_material (uuid, material_id, unidade_id, quantidade, custo_unitario, criado_em) values (?, ?, ?, 100, 10, ?)",
                lote, mat, ubs.getUuid(), quando);
        jdbc.update("insert into tb_movimentacao_estoque (uuid, lote_id, tipo, quantidade, saldo_apos, setor_id, registrado_em) values (?, ?, "
                + "'SAIDA_CONSUMO', -10, 90, ?, ?)", UUID.randomUUID(), lote, limp, quando);
        jdbc.update("insert into tb_leito (uuid, unidade_id, setor_id, identificacao, sexo, situacao, tipo, faixa_etaria, ativo) values (?, ?, ?, 'CUS-L1', 'MISTO', "
                + "'LIVRE', 'OBSERVACAO', 'ADULTO', true)", UUID.randomUUID(), ubs.getUuid(), setor);
        // UBS Norte: o geral de apoio com a depreciação do computador, sem nenhum produtivo.
        jdbc.update("insert into tb_bem_patrimonial (uuid, unidade_id, descricao, classe, origem, situacao, numero_tombamento, valor_aquisicao, "
                + "data_aquisicao, registrado_em) values (?, ?, ?, 'INFORMATICA', 'COMPRA', 'EM_USO', 'CUS-0002', 6000, ?, now())",
                UUID.randomUUID(), ubs2.getUuid(), PREFIXO + "Computador", mes.atDay(1).minusYears(2));

        String a = enviar(post(API + "apuracao-custo"), "{\"competencia\": \"" + mes + "\"}", 200);
        String ce = "$.centros[?(@.centroId == '%s')].";
        assertThat(numero(a, ce.formatted(cAdm) + "total")).isEqualTo(1000.0);
        assertThat(numero(a, ce.formatted(cAdm) + "enviado")).isEqualTo(1000.0);
        assertThat(numero(a, ce.formatted(cAdm) + "custoFinal")).isEqualTo(0.0);
        assertThat(numero(a, ce.formatted(cLimp) + "recebido")).isEqualTo(0.0);
        assertThat(numero(a, ce.formatted(cLimp) + "enviado")).isEqualTo(100.0);
        assertThat(numero(a, ce.formatted(geralId) + "recebido")).isEqualTo(275.0);
        assertThat(numero(a, ce.formatted(geralId) + "custoFinal")).isEqualTo(3275.0);
        assertThat(numero(a, ce.formatted(cEnf) + "recebido")).isEqualTo(825.0);
        assertThat(numero(a, ce.formatted(cEnf) + "custoFinal")).isEqualTo(2825.0);
        assertThat(JsonPath.read(a, ce.formatted(cEnf) + "internacao").toString()).contains("true");
        assertThat(numero(a, "$.rateios[?(@.origemId == '" + cAdm + "' && @.destinoId == '" + cEnf + "')].valor")).isEqualTo(750.0);
        assertThat(JsonPath.read(a, "$.rateios[?(@.origemId == '" + cAdm + "')].criterio").toString()).contains("PESSOAS").doesNotContain("CUSTO_DIRETO");
        assertThat(numero(a, "$.rateios[?(@.origemId == '" + cLimp + "' && @.destinoId == '" + geralId + "')].valor")).isEqualTo(25.0);
        assertThat((List<?>) JsonPath.read(a, "$.rateios[?(@.destinoId == '" + cAdm + "')]")).isEmpty();
        assertThat(numero(a, "$.naoRateado")).isGreaterThanOrEqualTo(90.0);
        assertThat(numero(a, ce.formatted(geralNorte) + "custoFinal")).isEqualTo(90.0);
        assertThat(numero(a, ce.formatted(geralNorte) + "enviado")).isEqualTo(0.0);

        // Unidade mista: 6.100 na UBS Centro; a enfermagem (2.825) ao paciente-dia, sem censo nulo; os 3.275 restantes por 2 atendimentos.
        Paciente p = new Paciente();
        p.setNome(PREFIXO + "Paciente Rateio");
        p.setCpf("90033420012");
        p.setDataNascimento(mes.atDay(1).minusYears(30));
        p.setAtivo(true);
        Endereco e = new Endereco();
        e.setLogradouro("Rua A");
        e.setNumeroLogradouro("1");
        e.setCidade("Cidade");
        e.setEstado("SP");
        p.setEndereco(e);
        p = pacienteRepository.save(p);
        UUID prof = jdbc.queryForObject("select uuid from tb_profissional where matricula = 'CUS-ENF'", UUID.class);
        for (int i = 0; i < 2; i++) {
            jdbc.update("insert into tb_atendimento (uuid, data_hora, paciente_id, profissional_id, unidade_id, status) values (?, ?, ?, ?, ?, 'CONCLUIDO')",
                    UUID.randomUUID(), Timestamp.valueOf(mes.atDay(5).atTime(9 + i, 0)), p.getUuid(), prof, ubs.getUuid());
        }
        enviar(post(API + "apuracao-custo"), "{\"competencia\": \"" + mes + "\"}", 200);
        String ind = ler(API + "indicadores-custo?competencia=" + mes, 200);
        String linha = "$.unidades[?(@.unidadeId == '" + ubs.getUuid() + "')]";
        assertThat(numero(ind, linha + ".total")).isEqualTo(6100.0);
        assertThat(numero(ind, linha + ".custoPorAtendimento")).isEqualTo(1637.5);
        assertThat(JsonPath.read(ind, linha + ".custoPorPacienteDia").toString()).contains("null");
    }

    /** Sem base no critério (ninguém lotado nos destinos), o rateio vai pelo custo direto. */
    @Test
    void rateioSemBaseVaiPeloCustoDireto() throws Exception {
        UUID adm = setor("CUS-AD2", "Administração 2", "ADMINISTRATIVO");
        String cAdm = JsonPath.read(enviar(post(API + "centro-custo"), "{\"setorId\": \"" + adm + "\"}", 201), "$.uuid");
        String cEnf = JsonPath.read(enviar(post(API + "centro-custo"), "{\"setorId\": \"" + setor + "\"}", 201), "$.uuid");
        UUID cat = UUID.randomUUID(), cargo = UUID.randomUUID();
        jdbc.update("insert into tb_categoria_salarial (uuid, nome) values (?, ?)", cat, PREFIXO + "Categoria");
        jdbc.update("insert into tb_cargo (uuid, nome, categoria_salarial_id) values (?, ?, ?)", cargo, PREFIXO + "Cargo", cat);
        lotado("CUS-AD2", cargo, adm, 40, 900);
        // O custo direto: 100 de material na enfermagem e 200 no geral (pelo consumo sem setor).
        UUID mat = UUID.randomUUID(), lote = UUID.randomUUID();
        jdbc.update("insert into tb_material_estoque (uuid, codigo, nome, categoria, unidade_medida, ativo, controla_lote) values (?, 'CUS-GAZ', ?, "
                + "'EPI', 'CAIXA', true, false)", mat, PREFIXO + "Gaze");
        Timestamp quando = Timestamp.valueOf(mes.atDay(10).atTime(12, 0));
        jdbc.update("insert into tb_lote_material (uuid, material_id, unidade_id, quantidade, custo_unitario, criado_em) values (?, ?, ?, 100, 10, ?)",
                lote, mat, ubs.getUuid(), quando);
        for (Object[] m : new Object[][]{{setor, -10}, {null, -20}}) {
            jdbc.update("insert into tb_movimentacao_estoque (uuid, lote_id, tipo, quantidade, saldo_apos, setor_id, registrado_em) values (?, ?, "
                    + "'SAIDA_CONSUMO', ?, 70, ?, ?)", UUID.randomUUID(), lote, m[1], m[0], quando);
        }
        String a = enviar(post(API + "apuracao-custo"), "{\"competencia\": \"" + mes + "\"}", 200);
        assertThat(JsonPath.read(a, "$.rateios[?(@.origemId == '" + cAdm + "')].criterio").toString()).contains("CUSTO_DIRETO").doesNotContain("PESSOAS");
        assertThat(numero(a, "$.rateios[?(@.origemId == '" + cAdm + "' && @.destinoId == '" + cEnf + "')].valor")).isEqualTo(300.0);
        assertThat(numero(a, "$.centros[?(@.centroId == '" + cAdm + "')].custoFinal")).isEqualTo(0.0);
    }
}
