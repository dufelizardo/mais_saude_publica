package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.models.Paciente;
import com.edufelizardo.maissaudepublica.models.Profissional;
import com.edufelizardo.maissaudepublica.models.UnidadeDeSaude;
import com.edufelizardo.maissaudepublica.models.enuns.Sexo;
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
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Imunização, fatia I2 (ADR-0148): a sala de vacina como capacidade da unidade, a entrada do lote, a baixa automática da
 * dose aplicada (com o frasco multidose aberto e reusado), o estorno na anulação e na retificação, a perda técnica do
 * frasco vencido, a perda física e o ajuste; a cadeia de frio com a excursão que põe o lote sob suspeita e a avaliação
 * da coordenação. A unidade sem a capacidade segue como na I1 (lote em texto).
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SalaDeVacinaControllerTest {

    private static final String PREFIXO = "SalaVac Teste ";
    private static final String MATRICULA = "P-SALAVAC";
    private static final String URL = "/api/v1/";

    @Autowired private MockMvc mockMvc;
    @Autowired private JdbcTemplate jdbc;
    @Autowired private UnidadeDeSaudeRepository unidadeDeSaudeRepository;
    @Autowired private PacienteRepository pacienteRepository;
    @Autowired private ProfissionalRepository profissionalRepository;

    private final LocalDate hoje = LocalDate.now();
    private UnidadeDeSaude sala;
    private UnidadeDeSaude semSala;
    private int pacientes;

    @BeforeEach
    void seed() {
        limpar();
        sala = unidade("UBS com sala");
        semSala = unidade("UBS sem sala");
        jdbc.update("insert into tb_unidade_capacidade_assistencial (uuid, unidade_id, capacidade_assistencial_id, inicio) select gen_random_uuid(), ?, uuid, ? "
                + "from tb_capacidade_assistencial where codigo = 'SALA_VACINA'", sala.getUuid(), hoje.minusDays(1));
        Profissional tec = new Profissional();
        tec.setMatricula(MATRICULA);
        tec.setCpf("90077700001");
        tec.setNome(PREFIXO + "Técnica");
        tec.setConselhoClasse("COREN");
        tec.setAtivo(true);
        profissionalRepository.save(tec);
    }

    @AfterEach
    void limpar() {
        String unidades = "(select uuid from tb_unidade_de_saude where nome like '" + PREFIXO + "%')";
        String lotes = "(select uuid from tb_lote_imunobiologico where unidade_id in " + unidades + ")";
        String pacientesSql = "(select uuid from tb_paciente where nome like '" + PREFIXO + "%')";
        jdbc.update("delete from tb_movimentacao_imunobiologico where lote_id in " + lotes);
        jdbc.update("update tb_registro_vacinacao set retificacao_de_id = null where paciente_id in " + pacientesSql);
        jdbc.update("delete from tb_registro_vacinacao where paciente_id in " + pacientesSql);
        jdbc.update("delete from tb_frasco_aberto where lote_id in " + lotes);
        jdbc.update("delete from tb_lote_imunobiologico where unidade_id in " + unidades);
        jdbc.update("delete from tb_leitura_temperatura where camara_id in (select uuid from tb_camara_refrigeracao where unidade_id in " + unidades + ")");
        jdbc.update("delete from tb_camara_refrigeracao where unidade_id in " + unidades);
        jdbc.update("delete from tb_unidade_capacidade_assistencial where unidade_id in " + unidades);
        jdbc.update("delete from tb_paciente where nome like '" + PREFIXO + "%'");
        jdbc.update("delete from tb_profissional where matricula = '" + MATRICULA + "'");
        jdbc.update("delete from tb_unidade_de_saude where nome like '" + PREFIXO + "%'");
    }

    private UnidadeDeSaude unidade(String nome) {
        UnidadeDeSaude u = new UnidadeDeSaude();
        u.setNome(PREFIXO + nome);
        u.setTipo(TipoUnidadeDeSaude.UBS);
        u.setAtivo(true);
        return unidadeDeSaudeRepository.save(u);
    }

    private Paciente paciente(LocalDate nascimento) {
        pacientes++;
        Paciente p = new Paciente();
        p.setNome(PREFIXO + "Criança " + pacientes);
        p.setCpf(String.format("900777%05d", pacientes));
        p.setSexo(Sexo.MASCULINO);
        p.setDataNascimento(nascimento);
        p.setAtivo(true);
        return pacienteRepository.save(p);
    }

    private String imuno(String codigo) {
        return jdbc.queryForObject("select cast(uuid as varchar) from tb_imunobiologico where codigo = ?", String.class, codigo);
    }

    private String entrada(UnidadeDeSaude u, String codigo, String lote, LocalDate validade, int doses, String extra) {
        return ("{\"unidadeId\": \"%s\", \"imunobiologicoId\": \"%s\", \"numeroLote\": \"%s\", \"fabricante\": \"Butantan\", \"validade\": \"%s\", "
                + "\"doses\": %d%s}").formatted(u.getUuid(), imuno(codigo), lote, validade, doses, extra);
    }

    private String dose(Paciente p, UnidadeDeSaude u, String codigo, String doseVacinal, String extra) {
        return ("{\"pacienteId\": \"%s\", \"imunobiologicoId\": \"%s\", \"dose\": \"%s\", \"unidadeId\": \"%s\", \"origem\": \"APLICADA\", "
                + "\"profissionalMatricula\": \"%s\"%s}").formatted(p.getUuid(), imuno(codigo), doseVacinal, u.getUuid(), MATRICULA, extra);
    }

    private String enviar(MockHttpServletRequestBuilder r, String corpo, int esperado) throws Exception {
        return mockMvc.perform(r.contentType(MediaType.APPLICATION_JSON).content(corpo)).andExpect(status().is(esperado)).andReturn().getResponse()
                .getContentAsString(StandardCharsets.UTF_8);
    }

    private String ler(String url) throws Exception {
        return mockMvc.perform(get(url)).andExpect(status().isOk()).andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
    }

    private int doses(String lote) throws Exception {
        List<Integer> d = JsonPath.read(ler(URL + "sala-de-vacina/estoque?unidadeId=" + sala.getUuid()), "$.lotes[?(@.uuid == '" + lote + "')].doses");
        return d.isEmpty() ? 0 : d.get(0);
    }

    private List<String> tipos(String lote) throws Exception {
        return JsonPath.read(ler(URL + "lote-imunobiologico/" + lote + "/movimentacoes"), "$[*].tipo");
    }

    @Test
    void entradaBaixaMultidoseEstornoEPerdaTecnica() throws Exception {
        // Entrada: cria o lote, soma o mesmo número; validade divergente, lote vencido e unidade sem sala de vacina, 422.
        String bcg = JsonPath.read(enviar(post(URL + "sala-de-vacina/entrada"), entrada(sala, "BCG", "bcg-1", hoje.plusDays(60), 20, ""), 201), "$.uuid");
        String somado = enviar(post(URL + "sala-de-vacina/entrada"), entrada(sala, "BCG", "BCG-1", hoje.plusDays(60), 10, ""), 201);
        assertThat((String) JsonPath.read(somado, "$.uuid")).isEqualTo(bcg);
        assertThat((Integer) JsonPath.read(somado, "$.doses")).isEqualTo(30);
        assertThat((String) JsonPath.read(somado, "$.numeroLote")).isEqualTo("BCG-1");
        enviar(post(URL + "sala-de-vacina/entrada"), entrada(sala, "BCG", "BCG-1", hoje.plusDays(90), 10, ""), 422);
        enviar(post(URL + "sala-de-vacina/entrada"), entrada(sala, "BCG", "BCG-2", hoje.minusDays(1), 10, ""), 422);
        enviar(post(URL + "sala-de-vacina/entrada"), entrada(semSala, "BCG", "BCG-1", hoje.plusDays(60), 10, ""), 422);
        String penta = JsonPath.read(enviar(post(URL + "sala-de-vacina/entrada"), entrada(sala, "PENTA", "PT-1", hoje.plusDays(20), 5, ""), 201), "$.uuid");

        // A dose aplicada na unidade com sala exige o lote do estoque (400); lote de outra vacina, 422.
        Paciente a = paciente(hoje.minusDays(5));
        enviar(post(URL + "vacinacao"), dose(a, sala, "BCG", "UNICA", ", \"lote\": \"L-TEXTO\""), 400);
        enviar(post(URL + "vacinacao"), dose(a, sala, "BCG", "UNICA", ", \"loteEstoqueId\": \"" + penta + "\""), 422);
        String r1 = enviar(post(URL + "vacinacao"), dose(a, sala, "BCG", "UNICA", ", \"loteEstoqueId\": \"" + bcg + "\""), 201);
        assertThat((String) JsonPath.read(r1, "$.lote")).isEqualTo("BCG-1");
        assertThat((String) JsonPath.read(r1, "$.fabricante")).isEqualTo("Butantan");
        String frasco = JsonPath.read(r1, "$.frascoId");
        assertThat(frasco).isNotNull();

        // O segundo paciente usa o mesmo frasco aberto.
        Paciente b = paciente(hoje.minusDays(3));
        String r2 = enviar(post(URL + "vacinacao"), dose(b, sala, "BCG", "UNICA", ", \"loteEstoqueId\": \"" + bcg + "\""), 201);
        assertThat((String) JsonPath.read(r2, "$.frascoId")).isEqualTo(frasco);
        String estoque = ler(URL + "sala-de-vacina/estoque?unidadeId=" + sala.getUuid());
        assertThat((Boolean) JsonPath.read(estoque, "$.temSalaDeVacina")).isTrue();
        assertThat((List<Integer>) JsonPath.read(estoque, "$.frascos[*].dosesRestantes")).containsExactly(8);
        assertThat((List<Integer>) JsonPath.read(estoque, "$.porVacina[?(@.sigla == 'BCG')].doses")).containsExactly(28);
        assertThat((List<Boolean>) JsonPath.read(estoque, "$.lotes[?(@.uuid == '" + penta + "')].venceEm30Dias")).containsExactly(true);

        // A anulação devolve a dose ao lote e ao frasco.
        enviar(post(URL + "vacinacao/" + JsonPath.read(r2, "$.uuid") + "/anulacao"), "{\"motivo\": \"Paciente errado\"}", 200);
        assertThat(doses(bcg)).isEqualTo(29);
        estoque = ler(URL + "sala-de-vacina/estoque?unidadeId=" + sala.getUuid());
        assertThat((List<Integer>) JsonPath.read(estoque, "$.frascos[*].dosesRestantes")).containsExactly(9);

        // A retificação que mantém o lote não lança nada; a que troca de lote estorna o anterior e baixa o novo.
        String outro = JsonPath.read(enviar(post(URL + "sala-de-vacina/entrada"), entrada(sala, "BCG", "BCG-9", hoje.plusDays(60), 10, ""), 201), "$.uuid");
        String mesmo = enviar(post(URL + "vacinacao/" + JsonPath.read(r1, "$.uuid") + "/retificacao"),
                dose(a, sala, "BCG", "UNICA", ", \"loteEstoqueId\": \"" + bcg + "\", \"motivoRetificacao\": \"Local de aplicação\", \"localAplicacao\": \"DELTOIDE_DIREITO\""), 201);
        assertThat((String) JsonPath.read(mesmo, "$.frascoId")).isEqualTo(frasco);
        assertThat(tipos(bcg)).containsExactly("ENTRADA", "ENTRADA", "APLICACAO", "APLICACAO", "ESTORNO_APLICACAO");
        enviar(post(URL + "vacinacao/" + JsonPath.read(mesmo, "$.uuid") + "/retificacao"),
                dose(a, sala, "BCG", "UNICA", ", \"loteEstoqueId\": \"" + outro + "\", \"motivoRetificacao\": \"Lote errado\""), 201);
        assertThat(doses(bcg)).isEqualTo(30);
        assertThat(doses(outro)).isEqualTo(9);

        // O frasco vencido se encerra na próxima consulta: o que sobrou é perda técnica.
        jdbc.update("update tb_frasco_aberto set valido_ate = now() - interval '1 hour' where lote_id in (cast(? as uuid), cast(? as uuid))", bcg, outro);
        estoque = ler(URL + "sala-de-vacina/estoque?unidadeId=" + sala.getUuid());
        assertThat((List<Object>) JsonPath.read(estoque, "$.frascos")).isEmpty();
        assertThat(doses(bcg)).isEqualTo(20);
        assertThat(doses(outro)).isEqualTo(0);
        assertThat((Integer) JsonPath.read(estoque, "$.perdaTecnicaMes")).isEqualTo(19);
        assertThat((Integer) JsonPath.read(estoque, "$.aplicadasMes")).isEqualTo(1);
        assertThat(tipos(bcg)).last().isEqualTo("PERDA_TECNICA");

        // Unidose: sem frasco. Perda física, até o saldo; a do frasco vencido é do sistema (400). Ajuste de inventário.
        Paciente c = paciente(hoje.minusMonths(2).minusDays(3));
        String r3 = enviar(post(URL + "vacinacao"), dose(c, sala, "PENTA", "PRIMEIRA", ", \"loteEstoqueId\": \"" + penta + "\""), 201);
        assertThat((String) JsonPath.read(r3, "$.frascoId")).isNull();
        enviar(post(URL + "lote-imunobiologico/" + penta + "/perda"), "{\"doses\": 1, \"motivo\": \"QUEBRA\"}", 200);
        enviar(post(URL + "lote-imunobiologico/" + penta + "/perda"), "{\"doses\": 9, \"motivo\": \"QUEBRA\"}", 422);
        enviar(post(URL + "lote-imunobiologico/" + penta + "/perda"), "{\"doses\": 1, \"motivo\": \"FRASCO_ABERTO_VENCIDO\"}", 400);
        enviar(post(URL + "lote-imunobiologico/" + penta + "/ajuste"), "{\"saldo\": 3, \"justificativa\": \"Contagem\"}", 422);
        String ajustado = enviar(post(URL + "lote-imunobiologico/" + penta + "/ajuste"), "{\"saldo\": 2, \"justificativa\": \"Contagem mensal\"}", 200);
        assertThat((Integer) JsonPath.read(ajustado, "$.doses")).isEqualTo(2);
        assertThat(tipos(penta)).containsExactly("ENTRADA", "APLICACAO", "PERDA_FISICA", "AJUSTE_INVENTARIO");

        // A unidade sem sala de vacina segue com o lote em texto; o lote do estoque ali, 422.
        Paciente d = paciente(hoje.minusDays(2));
        enviar(post(URL + "vacinacao"), dose(d, semSala, "BCG", "UNICA", ", \"loteEstoqueId\": \"" + bcg + "\""), 422);
        String r4 = enviar(post(URL + "vacinacao"), dose(d, semSala, "BCG", "UNICA", ", \"lote\": \"L-TEXTO\""), 201);
        assertThat((String) JsonPath.read(r4, "$.loteEstoqueId")).isNull();
        assertThat((Boolean) JsonPath.read(ler(URL + "sala-de-vacina/estoque?unidadeId=" + semSala.getUuid()), "$.temSalaDeVacina")).isFalse();
    }

    @Test
    void cadeiaDeFrioExcursaoEAvaliacao() throws Exception {
        // A câmara: faixa padrão de 2 a 8 °C; nome repetido 409; faixa invertida 400; sem sala de vacina 422.
        String camara = enviar(post(URL + "camara-refrigeracao"), "{\"unidadeId\": \"%s\", \"nome\": \"Câmara 1\"}".formatted(sala.getUuid()), 201);
        String camaraId = JsonPath.read(camara, "$.uuid");
        assertThat(JsonPath.read(camara, "$.temperaturaMaxima").toString()).isEqualTo("8.0");
        enviar(post(URL + "camara-refrigeracao"), "{\"unidadeId\": \"%s\", \"nome\": \"câmara 1\"}".formatted(sala.getUuid()), 409);
        enviar(post(URL + "camara-refrigeracao"), "{\"unidadeId\": \"%s\", \"nome\": \"Câmara 2\", \"temperaturaMinima\": 8, \"temperaturaMaxima\": 2}"
                .formatted(sala.getUuid()), 400);
        enviar(post(URL + "camara-refrigeracao"), "{\"unidadeId\": \"%s\", \"nome\": \"Câmara 1\"}".formatted(semSala.getUuid()), 422);
        String vip = JsonPath.read(enviar(post(URL + "sala-de-vacina/entrada"),
                entrada(sala, "VIP", "VIP-1", hoje.plusDays(90), 10, ", \"camaraId\": \"" + camaraId + "\""), 201), "$.uuid");

        // Leitura na faixa; fora de ordem 400; ontem sem leitura, duas pendências.
        String ok = enviar(post(URL + "camara-refrigeracao/" + camaraId + "/leitura"),
                "{\"temperaturaAtual\": 5, \"temperaturaMinima\": 3.5, \"temperaturaMaxima\": 7}", 201);
        assertThat((Boolean) JsonPath.read(ok, "$.excursao")).isFalse();
        enviar(post(URL + "camara-refrigeracao/" + camaraId + "/leitura"), "{\"temperaturaAtual\": 9, \"temperaturaMinima\": 3, \"temperaturaMaxima\": 7}", 400);
        String ontem = ler(URL + "sala-de-vacina/cadeia-de-frio?unidadeId=" + sala.getUuid() + "&data=" + hoje.minusDays(1));
        assertThat((List<String>) JsonPath.read(ontem, "$.pendencias")).containsExactly("Câmara 1: falta a leitura da manhã.",
                "Câmara 1: falta a leitura da tarde.");

        // Excursão: o lote da câmara fica sob suspeita e não se aplica.
        String exc = enviar(post(URL + "camara-refrigeracao/" + camaraId + "/leitura"),
                "{\"temperaturaAtual\": 6, \"temperaturaMinima\": 4, \"temperaturaMaxima\": 10.5, \"observacao\": \"Porta aberta\"}", 201);
        assertThat((Boolean) JsonPath.read(exc, "$.excursao")).isTrue();
        assertThat((Integer) JsonPath.read(exc, "$.lotesPostosSobSuspeita")).isEqualTo(1);
        Paciente p = paciente(hoje.minusMonths(2).minusDays(3));
        enviar(post(URL + "vacinacao"), dose(p, sala, "VIP", "PRIMEIRA", ", \"loteEstoqueId\": \"" + vip + "\""), 422);
        String painel = ler(URL + "sala-de-vacina/cadeia-de-frio?unidadeId=" + sala.getUuid());
        assertThat((List<String>) JsonPath.read(painel, "$.lotesSobSuspeita[*].numeroLote")).containsExactly("VIP-1");
        assertThat((List<Integer>) JsonPath.read(painel, "$.camaras[*].lotesSobSuspeita")).containsExactly(1);
        assertThat((List<Integer>) JsonPath.read(painel, "$.leituras[*].lotesPostosSobSuspeita")).hasSize(2);

        // Avaliação: decisão inválida 400; liberar volta a aplicar; avaliar o que não está sob suspeita, 422.
        enviar(post(URL + "lote-imunobiologico/" + vip + "/avaliacao"), "{\"decisao\": \"TALVEZ\", \"parecer\": \"x\"}", 400);
        String liberado = enviar(post(URL + "lote-imunobiologico/" + vip + "/avaliacao"),
                "{\"decisao\": \"liberar\", \"parecer\": \"Fabricante: estável até 12 °C por 24 h.\"}", 200);
        assertThat((String) JsonPath.read(liberado, "$.situacao")).isEqualTo("DISPONIVEL");
        enviar(post(URL + "lote-imunobiologico/" + vip + "/avaliacao"), "{\"decisao\": \"LIBERAR\", \"parecer\": \"x\"}", 422);
        enviar(post(URL + "vacinacao"), dose(p, sala, "VIP", "PRIMEIRA", ", \"loteEstoqueId\": \"" + vip + "\""), 201);

        // Nova excursão e inutilização: o saldo inteiro sai como perda física; o frasco aberto se encerra.
        enviar(post(URL + "camara-refrigeracao/" + camaraId + "/leitura"), "{\"temperaturaAtual\": 1, \"temperaturaMinima\": -1, \"temperaturaMaxima\": 5}", 201);
        String inutilizado = enviar(post(URL + "lote-imunobiologico/" + vip + "/avaliacao"),
                "{\"decisao\": \"INUTILIZAR\", \"parecer\": \"Congelamento: vacina inativada não tolera.\"}", 200);
        assertThat((String) JsonPath.read(inutilizado, "$.situacao")).isEqualTo("INUTILIZADO");
        assertThat((Integer) JsonPath.read(inutilizado, "$.doses")).isZero();
        List<String> motivos = JsonPath.read(ler(URL + "lote-imunobiologico/" + vip + "/movimentacoes"), "$[*].motivoPerda");
        assertThat(motivos).last().isEqualTo("EXCURSAO_TEMPERATURA");
        String estoque = ler(URL + "sala-de-vacina/estoque?unidadeId=" + sala.getUuid());
        assertThat((List<Object>) JsonPath.read(estoque, "$.frascos")).isEmpty();
        assertThat((Integer) JsonPath.read(estoque, "$.perdaFisicaMes")).isEqualTo(9);

        // A câmara desativada não recebe leitura (422) nem gera pendência.
        enviar(put(URL + "camara-refrigeracao/" + camaraId), "{\"unidadeId\": \"%s\", \"nome\": \"Câmara 1\", \"ativa\": false}".formatted(sala.getUuid()), 200);
        enviar(post(URL + "camara-refrigeracao/" + camaraId + "/leitura"), "{\"temperaturaAtual\": 5, \"temperaturaMinima\": 4, \"temperaturaMaxima\": 6}", 422);
        ontem = ler(URL + "sala-de-vacina/cadeia-de-frio?unidadeId=" + sala.getUuid() + "&data=" + hoje.minusDays(1));
        assertThat((List<String>) JsonPath.read(ontem, "$.pendencias")).isEmpty();
    }
}
