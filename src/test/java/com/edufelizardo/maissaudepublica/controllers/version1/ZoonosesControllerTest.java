package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.models.Paciente;
import com.edufelizardo.maissaudepublica.models.UnidadeDeSaude;
import com.edufelizardo.maissaudepublica.models.enuns.TipoUnidadeDeSaude;
import com.edufelizardo.maissaudepublica.repositories.AgravoRepository;
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
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Vigilância, fatia V4b (ADR-0163): o animal de interesse, a observação do agressor ligada ao atendimento antirrábico
 * (10 dias, desfecho), os exames (raiva e leishmaniose, com o desfecho do cão) e a vacinação antirrábica animal.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ZoonosesControllerTest {

    private static final String PREFIXO = "Zoonose Teste ";
    private static final String CODIGO = "TSTV4B";

    @Autowired private MockMvc mockMvc;
    @Autowired private JdbcTemplate jdbc;
    @Autowired private UnidadeDeSaudeRepository unidadeRepository;
    @Autowired private PacienteRepository pacienteRepository;
    @Autowired private AgravoRepository agravoRepository;

    private UnidadeDeSaude ubs;
    private String area;
    private String microarea;
    private String imovel;
    private final LocalDate hoje = LocalDate.now();

    @BeforeEach
    void seed() {
        limpar();
        UUID segmento = UUID.randomUUID();
        UUID territorio = UUID.randomUUID();
        UUID a = UUID.randomUUID();
        UUID m = UUID.randomUUID();
        UUID im = UUID.randomUUID();
        jdbc.update("insert into tb_segmento_territorial (uuid, codigo, nome, tipo) values (?, ?, ?, 'DISTRITO_SANITARIO')", segmento, CODIGO + "SEG", PREFIXO + "Distrito");
        jdbc.update("insert into tb_territorio (uuid, segmento_id, nome, finalidade, ativo) values (?, ?, ?, 'VIGILANCIA', true)", territorio, segmento, PREFIXO + "Território");
        jdbc.update("insert into tb_area (uuid, territorio_id, codigo, nome) values (?, ?, '01', ?)", a, territorio, PREFIXO + "Área Sul");
        jdbc.update("insert into tb_microarea (uuid, area_id, codigo) values (?, ?, '03')", m, a);
        jdbc.update("insert into tb_imovel (uuid, microarea_id, tipo, ativo, logradouro, numero_logradouro) values (?, ?, 'DOMICILIO', true, 'Rua dos Cães', '7')", im, m);
        area = a.toString();
        microarea = m.toString();
        imovel = im.toString();
        ubs = new UnidadeDeSaude();
        ubs.setNome(PREFIXO + "UBS");
        ubs.setTipo(TipoUnidadeDeSaude.UBS);
        ubs.setAtivo(true);
        ubs = unidadeRepository.save(ubs);
    }

    @AfterEach
    void limpar() {
        String microareas = "(select uuid from tb_microarea where area_id in (select a.uuid from tb_area a join tb_territorio t on t.uuid = a.territorio_id "
                + "join tb_segmento_territorial s on s.uuid = t.segmento_id where s.codigo like '" + CODIGO + "%'))";
        String animais = "(select uuid from tb_animal_zoonoses where nome like '" + PREFIXO + "%')";
        jdbc.update("delete from tb_exame_animal where animal_id in " + animais);
        jdbc.update("delete from tb_observacao_animal where animal_id in " + animais);
        jdbc.update("delete from tb_animal_zoonoses where uuid in " + animais);
        jdbc.update("delete from tb_vacinacao_antirrabica_animal where microarea_id in " + microareas);
        VigilanciaControllerTest.limparDados(jdbc, PREFIXO, CODIGO);
        TerritorioControllerTest.limparDados(jdbc, PREFIXO, CODIGO);
    }

    private String enviar(MockHttpServletRequestBuilder r, String corpo, int esperado) throws Exception {
        return mockMvc.perform(r.contentType(MediaType.APPLICATION_JSON).content(corpo)).andExpect(status().is(esperado)).andReturn().getResponse()
                .getContentAsString(StandardCharsets.UTF_8);
    }

    private String ler(String url) throws Exception {
        return mockMvc.perform(get(url)).andExpect(status().isOk()).andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
    }

    private String animal(String especie, String nome) throws Exception {
        return JsonPath.read(enviar(post("/api/v1/zoonoses/animal"), "{\"especie\": \"" + especie + "\", \"nome\": \"" + PREFIXO + nome
                + "\", \"tutorNome\": \"Dona Rita\", \"tutorTelefone\": \"(19) 99999-0000\", \"imovelId\": \"" + imovel + "\"}", 201), "$.uuid");
    }

    private String notificar(String codigo) throws Exception {
        Paciente p = new Paciente();
        p.setNome(PREFIXO + codigo);
        p.setCpf(codigo.equals("SARAMPO") ? "93980000002" : "93980000001");
        p.setAtivo(true);
        p = pacienteRepository.save(p);
        String agravo = agravoRepository.findByCodigo(codigo).orElseThrow().getUuid().toString();
        return JsonPath.read(enviar(post("/api/v1/vigilancia/notificacao"), "{\"pacienteId\": \"%s\", \"agravoId\": \"%s\", \"unidadeId\": \"%s\", \"origem\": \"ATENDIMENTO\"}"
                .formatted(p.getUuid(), agravo, ubs.getUuid()), 201), "$.uuid");
    }

    @Test
    void animalEObservacaoDoAgressor() throws Exception {
        // O imóvel ou o endereço; com o imóvel, a microárea.
        enviar(post("/api/v1/zoonoses/animal"), "{\"especie\": \"CAO\", \"nome\": \"" + PREFIXO + "Sem lugar\"}", 400);
        String rex = animal("CAO", "Rex");
        String morcego = animal("MORCEGO", "Morcego");
        String r = ler("/api/v1/zoonoses/animais?busca=rex");
        assertThat((List<String>) JsonPath.read(r, "$[*].uuid")).containsExactly(rex);
        assertThat((String) JsonPath.read(r, "$[0].microareaCodigo")).isEqualTo("03");
        assertThat((String) JsonPath.read(r, "$[0].endereco")).startsWith("Rua dos Cães");

        String antirrabico = notificar("ATENDIMENTO_ANTIRRABICO");
        String sarampo = notificar("SARAMPO");
        String obs = "{\"animalId\": \"%s\", \"dataAgressao\": \"%s\"%s}";
        // Só cão ou gato; agressão futura (400); início antes da agressão (400); outro agravo (422).
        enviar(post("/api/v1/zoonoses/observacao"), obs.formatted(morcego, hoje, ""), 422);
        enviar(post("/api/v1/zoonoses/observacao"), obs.formatted(rex, hoje.plusDays(1), ""), 400);
        enviar(post("/api/v1/zoonoses/observacao"), obs.formatted(rex, hoje, ", \"inicio\": \"" + hoje.minusDays(1) + "\""), 400);
        enviar(post("/api/v1/zoonoses/observacao"), obs.formatted(rex, hoje, ", \"notificacaoId\": \"" + sarampo + "\""), 422);
        // Agressão há 4 dias: o fim previsto é a agressão + 10.
        r = enviar(post("/api/v1/zoonoses/observacao"), obs.formatted(rex, hoje.minusDays(4), ", \"notificacaoId\": \"" + antirrabico + "\""), 201);
        String id = JsonPath.read(r, "$.uuid");
        assertThat((String) JsonPath.read(r, "$.fimPrevisto")).isEqualTo(hoje.plusDays(6).toString());
        enviar(post("/api/v1/zoonoses/observacao"), obs.formatted(rex, hoje, ""), 409);
        assertThat((List<String>) JsonPath.read(ler("/api/v1/zoonoses/observacoes?notificacaoId=" + antirrabico), "$[*].uuid")).containsExactly(id);
        assertThat((Boolean) JsonPath.read(ler("/api/v1/zoonoses/animais?busca=rex"), "$[0].emObservacao")).isTrue();

        // Sadio antes dos 10 dias, 422; morto pode, e encerra; de novo, 422.
        enviar(post("/api/v1/zoonoses/observacao/" + id + "/desfecho"), "{\"desfecho\": \"SADIO\"}", 422);
        r = enviar(post("/api/v1/zoonoses/observacao/" + id + "/desfecho"), "{\"desfecho\": \"SUSPEITO_RAIVA\", \"observacao\": \"Agressivo e salivando\"}", 200);
        assertThat((String) JsonPath.read(r, "$.desfecho")).isEqualTo("SUSPEITO_RAIVA");
        enviar(post("/api/v1/zoonoses/observacao/" + id + "/desfecho"), "{\"desfecho\": \"MORTO\"}", 422);

        // Agressão antiga: a observação aberta já está vencida e o sadio vale.
        String mia = animal("GATO", "Mia");
        r = enviar(post("/api/v1/zoonoses/observacao"), obs.formatted(mia, hoje.minusDays(12), ", \"inicio\": \"" + hoje.minusDays(11) + "\""), 201);
        assertThat((Boolean) JsonPath.read(r, "$.vencida")).isTrue();
        String idMia = JsonPath.read(r, "$.uuid");
        enviar(post("/api/v1/zoonoses/observacao/" + idMia + "/desfecho"), "{\"desfecho\": \"SADIO\"}", 200);
        assertThat((List<String>) JsonPath.read(ler("/api/v1/zoonoses/observacoes?abertas=true"), "$[*].uuid")).doesNotContain(id, idMia);
    }

    @Test
    void examesEVacinacao() throws Exception {
        String rex = animal("CAO", "Rex");
        String bob = animal("CAO", "Bob");
        // A coleta de raiva ligada à observação de outro animal, 422.
        String obsBob = JsonPath.read(enviar(post("/api/v1/zoonoses/observacao"), "{\"animalId\": \"" + bob + "\", \"dataAgressao\": \"" + hoje + "\"}", 201), "$.uuid");
        enviar(post("/api/v1/zoonoses/exame"), "{\"animalId\": \"" + rex + "\", \"tipo\": \"RAIVA_IMUNOFLUORESCENCIA\", \"observacaoId\": \"" + obsBob + "\"}", 422);
        enviar(post("/api/v1/zoonoses/exame"), "{\"animalId\": \"" + rex + "\", \"tipo\": \"LEISHMANIOSE_TR_DPP\", \"dataColeta\": \"" + hoje.plusDays(1) + "\"}", 400);
        String raiva = JsonPath.read(enviar(post("/api/v1/zoonoses/exame"), "{\"animalId\": \"" + bob + "\", \"tipo\": \"RAIVA_IMUNOFLUORESCENCIA\", \"observacaoId\": \"" + obsBob + "\"}", 201), "$.uuid");
        String leish = JsonPath.read(enviar(post("/api/v1/zoonoses/exame"), "{\"animalId\": \"" + rex + "\", \"tipo\": \"LEISHMANIOSE_TR_DPP\"}", 201), "$.uuid");
        assertThat((List<String>) JsonPath.read(ler("/api/v1/zoonoses/exames?pendentes=true"), "$[*].uuid")).contains(raiva, leish);

        // O desfecho do cão só na leishmaniose reagente.
        enviar(post("/api/v1/zoonoses/exame/" + raiva + "/resultado"), "{\"resultado\": \"REAGENTE\", \"desfechoLeishmaniose\": \"EUTANASIA\"}", 422);
        enviar(post("/api/v1/zoonoses/exame/" + leish + "/resultado"), "{\"resultado\": \"NAO_REAGENTE\", \"desfechoLeishmaniose\": \"EUTANASIA\"}", 422);
        String r = enviar(post("/api/v1/zoonoses/exame/" + leish + "/resultado"), "{\"resultado\": \"REAGENTE\", \"desfechoLeishmaniose\": \"TRATAMENTO_VETERINARIO\"}", 200);
        assertThat((String) JsonPath.read(r, "$.desfechoLeishmaniose")).isEqualTo("TRATAMENTO_VETERINARIO");
        enviar(post("/api/v1/zoonoses/exame/" + raiva + "/resultado"), "{\"resultado\": \"NAO_REAGENTE\"}", 200);
        assertThat((List<String>) JsonPath.read(ler("/api/v1/zoonoses/exames?pendentes=true"), "$[*].uuid")).doesNotContain(raiva, leish);

        // A vacinação do posto: ao menos um animal; os totais por área.
        String vac = "{\"microareaId\": \"" + microarea + "\", \"posto\": \"Escola Municipal\", \"responsavel\": \"Equipe de zoonoses\"%s}";
        enviar(post("/api/v1/zoonoses/vacinacao-antirrabica"), vac.formatted(""), 400);
        enviar(post("/api/v1/zoonoses/vacinacao-antirrabica"), vac.formatted(", \"caes\": 40, \"gatos\": 12"), 201);
        r = enviar(post("/api/v1/zoonoses/vacinacao-antirrabica"), vac.formatted(", \"caes\": 25, \"gatos\": 8"), 201);
        assertThat((List<Integer>) JsonPath.read(r, "$.areas[?(@.areaId == '" + area + "')].caes")).containsExactly(65);
        assertThat((List<Integer>) JsonPath.read(r, "$.areas[?(@.areaId == '" + area + "')].gatos")).containsExactly(20);
        assertThat((Integer) JsonPath.read(r, "$.ano")).isEqualTo(hoje.getYear());
    }
}
