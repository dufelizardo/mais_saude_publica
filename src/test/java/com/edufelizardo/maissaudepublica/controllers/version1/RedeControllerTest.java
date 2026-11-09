package com.edufelizardo.maissaudepublica.controllers.version1;

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

import java.time.YearMonth;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

/**
 * Gestão da Rede, GR1 (ADR-0179): a oferta e as cotas da PPI com os limites, e a Regulação consumindo a cota mais específica da solicitante,
 * a extracota justificada, a devolução no cancelamento, o mês sem oferta sem controle e a cópia da competência.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class RedeControllerTest {

    private static final String PREFIXO = "Rede Teste ";
    private static final String URL = "/api/v1/rede/";
    private static final String REG = "/api/v1/solicitacao-regulacao/";
    private static final YearMonth MES = YearMonth.now().plusMonths(1);

    @Autowired private MockMvc mockMvc;
    @Autowired private JdbcTemplate jdbc;
    @Autowired private UnidadeDeSaudeRepository unidadeRepository;
    @Autowired private PacienteRepository pacienteRepository;
    @Autowired private ProfissionalRepository profissionalRepository;

    private UnidadeDeSaude municipio;
    private UnidadeDeSaude ubs;
    private UnidadeDeSaude ubs2;
    private UnidadeDeSaude fora;
    private UnidadeDeSaude policlinica;
    private String procedimento;
    private String endodontia;
    private int pacientes;

    @BeforeEach
    void seed() {
        limpar();
        municipio = unidade("Município", TipoUnidadeDeSaude.MUNICIPAL, null);
        ubs = unidade("UBS", TipoUnidadeDeSaude.UBS, municipio);
        ubs2 = unidade("UBS 2", TipoUnidadeDeSaude.UBS, municipio);
        fora = unidade("Fora", TipoUnidadeDeSaude.UBS, null);
        policlinica = unidade("Policlínica", TipoUnidadeDeSaude.POLICLINICA, municipio);
        procedimento = UUID.randomUUID().toString();
        jdbc.update("insert into tb_procedimento_regulado (uuid, nome, tipo, ativo) values (cast(? as uuid), ?, 'CONSULTA_ESPECIALIZADA', true)",
                procedimento, PREFIXO + "Cardiologia");
        endodontia = UUID.randomUUID().toString();
        jdbc.update("insert into tb_procedimento_regulado (uuid, nome, tipo, ativo, capacidade_exigida_id) select cast(? as uuid), ?, 'PROCEDIMENTO', true, uuid "
                + "from tb_capacidade_assistencial where codigo = 'ODONTO_ENDODONTIA'", endodontia, PREFIXO + "Endodontia");
        for (String m : List.of("RD-SOLICITANTE", "RD-REGULADOR")) {
            Profissional p = new Profissional();
            p.setMatricula(m);
            p.setCpf(m.equals("RD-REGULADOR") ? "90079000001" : "90079000002");
            p.setNome(PREFIXO + m);
            p.setAtivo(true);
            profissionalRepository.save(p);
        }
        pacientes = 0;
    }

    @AfterEach
    void limpar() {
        String unidades = "(select uuid from tb_unidade_de_saude where nome like '" + PREFIXO + "%')";
        String solicitacoes = "(select uuid from tb_solicitacao_regulacao where unidade_solicitante_id in " + unidades + ")";
        jdbc.update("delete from tb_evento_regulacao where solicitacao_id in " + solicitacoes);
        jdbc.update("delete from tb_solicitacao_regulacao where unidade_solicitante_id in " + unidades);
        jdbc.update("delete from tb_cota_ppi where oferta_id in (select uuid from tb_oferta_assistencial where unidade_executante_id in " + unidades + ")");
        jdbc.update("delete from tb_oferta_assistencial where unidade_executante_id in " + unidades);
        jdbc.update("delete from tb_procedimento_regulado where nome like '" + PREFIXO + "%'");
        jdbc.update("delete from tb_paciente where nome like '" + PREFIXO + "%'");
        jdbc.update("delete from tb_profissional where matricula like 'RD-%'");
        jdbc.update("update tb_unidade_de_saude set unidade_superior_id = null where nome like ?", PREFIXO + "%");
        jdbc.update("delete from tb_unidade_de_saude where nome like '" + PREFIXO + "%'");
    }

    private UnidadeDeSaude unidade(String nome, TipoUnidadeDeSaude tipo, UnidadeDeSaude superior) {
        UnidadeDeSaude u = new UnidadeDeSaude();
        u.setNome(PREFIXO + nome);
        u.setTipo(tipo);
        u.setAtivo(true);
        u.setUnidadeSuperior(superior);
        return unidadeRepository.save(u);
    }

    private String enviar(MockHttpServletRequestBuilder r, String corpo, int esperado) throws Exception {
        var resposta = mockMvc.perform(r.contentType(MediaType.APPLICATION_JSON).content(corpo)).andReturn().getResponse();
        assertThat(resposta.getStatus()).as(resposta.getContentAsString()).isEqualTo(esperado);
        return resposta.getContentAsString();
    }

    private String ler(String url) throws Exception {
        var resposta = mockMvc.perform(get(url)).andReturn().getResponse();
        assertThat(resposta.getStatus()).as(resposta.getContentAsString()).isEqualTo(200);
        return resposta.getContentAsString();
    }

    private String oferta(String proc, UnidadeDeSaude executante, YearMonth mes, int qtd, int esperado) throws Exception {
        String r = enviar(post(URL + "oferta"), "{\"executanteId\": \"%s\", \"procedimentoId\": \"%s\", \"competencia\": \"%s\", \"quantidade\": %d}"
                .formatted(executante.getUuid(), proc, mes, qtd), esperado);
        return esperado == 201 ? JsonPath.read(r, "$.uuid") : r;
    }

    private String cota(String oferta, UnidadeDeSaude beneficiaria, int qtd, int esperado) throws Exception {
        return enviar(post(URL + "oferta/" + oferta + "/cota"), "{\"beneficiariaId\": \"%s\", \"quantidade\": %d}".formatted(beneficiaria.getUuid(), qtd), esperado);
    }

    /** Solicita a partir da unidade e tenta autorizar na policlínica no mês; devolve o id da solicitação. */
    private String autorizar(UnidadeDeSaude solicitante, YearMonth mes, String extra, int esperado) throws Exception {
        Paciente p = new Paciente();
        p.setNome(PREFIXO + "Paciente " + (++pacientes));
        p.setCpf(String.format("9008%07d", pacientes));
        p.setAtivo(true);
        p = pacienteRepository.save(p);
        enviar(post(REG), ("{\"pacienteId\": \"%s\", \"procedimentoId\": \"%s\", \"unidadeSolicitanteId\": \"%s\", \"profissionalMatricula\": \"RD-SOLICITANTE\", "
                + "\"cid\": \"I10\", \"justificativa\": \"Hipertensão resistente.\", \"prioridade\": \"VERDE\"}").formatted(p.getUuid(), procedimento, solicitante.getUuid()), 201);
        String id = jdbc.queryForObject("select cast(uuid as varchar) from tb_solicitacao_regulacao where paciente_id = ?", String.class, p.getUuid());
        enviar(post(REG + id + "/autorizacao"), "{\"profissionalMatricula\": \"RD-REGULADOR\", \"unidadeExecutanteId\": \"%s\", \"dataHoraPrevista\": \"%s\"%s}"
                .formatted(policlinica.getUuid(), mes.atDay(10).atTime(9, 0), extra), esperado);
        return id;
    }

    @Test
    void ofertaECotasComLimites() throws Exception {
        String oferta = oferta(procedimento, policlinica, MES, 5, 201);
        oferta(procedimento, policlinica, MES, 3, 409);
        oferta(endodontia, policlinica, MES, 3, 422);
        cota(oferta, ubs, 3, 201);
        cota(oferta, ubs, 1, 409);
        cota(oferta, municipio, 3, 422);
        String painel = cota(oferta, municipio, 2, 201);
        assertThat((Integer) JsonPath.read(painel, "$.pactuado")).isEqualTo(5);
        assertThat((Integer) JsonPath.read(painel, "$.semPactuar")).isZero();
        enviar(put(URL + "oferta/" + oferta), "{\"quantidade\": 4}", 422);
        enviar(put(URL + "oferta/" + oferta), "{\"quantidade\": 8, \"observacao\": \"Mutirão.\"}", 200);
    }

    @Test
    void regulacaoConsomeACotaMaisEspecifica() throws Exception {
        String oferta = oferta(procedimento, policlinica, MES, 10, 201);
        cota(oferta, ubs, 2, 201);
        String comMunicipio = cota(oferta, municipio, 3, 201);
        String cotaUbs = JsonPath.read(comMunicipio, "$.cotas[?(@.beneficiariaId == '" + ubs.getUuid() + "')].uuid").toString().replaceAll("[\\[\\]\"]", "");
        String saldo = ler(URL + "ppi/saldo?procedimentoId=" + procedimento + "&executanteId=" + policlinica.getUuid() + "&solicitanteId=" + ubs.getUuid()
                + "&competencia=" + MES);
        assertThat((Integer) JsonPath.read(saldo, "$.saldo")).isEqualTo(2);
        assertThat((String) JsonPath.read(saldo, "$.beneficiariaNome")).isEqualTo(PREFIXO + "UBS");

        String primeira = autorizar(ubs, MES, "", 200);
        autorizar(ubs, MES, "", 200);
        // A cota da UBS acabou; a do município não cobre a UBS, que tem a própria.
        autorizar(ubs, MES, "", 422);
        autorizar(ubs, MES, ", \"extraCota\": true", 422);
        String extra = autorizar(ubs, MES, ", \"extraCota\": true, \"justificativaExtraCota\": \"Risco cardiovascular alto.\"", 200);
        assertThat((Boolean) JsonPath.read(ler(REG + extra), "$.extraCota")).isTrue();
        // A UBS 2 não tem cota própria: usa a do município.
        String daUbs2 = autorizar(ubs2, MES, "", 200);
        assertThat((String) JsonPath.read(ler(REG + daUbs2), "$.cotaBeneficiariaNome")).isEqualTo(PREFIXO + "Município");
        // Fora do município, sem cota: só extracota.
        autorizar(fora, MES, "", 422);
        // No mês sem oferta, nada muda.
        autorizar(fora, MES.plusMonths(1), "", 200);

        String painel = ler(URL + "ppi?competencia=" + MES);
        assertThat((List<Integer>) JsonPath.read(painel, "$.ofertas[?(@.uuid == '" + oferta + "')].utilizado")).containsExactly(4);
        assertThat((List<Integer>) JsonPath.read(painel, "$.ofertas[?(@.uuid == '" + oferta + "')].extracotas")).containsExactly(1);

        enviar(put(URL + "cota/" + cotaUbs), "{\"quantidade\": 1}", 422);
        enviar(delete(URL + "cota/" + cotaUbs), "", 422);
        // O cancelamento devolve a vaga.
        enviar(post(REG + primeira + "/cancelamento"), "{\"profissionalMatricula\": \"RD-REGULADOR\", \"motivo\": \"Paciente mudou de cidade.\"}", 200);
        String depois = ler(URL + "ppi/saldo?procedimentoId=" + procedimento + "&executanteId=" + policlinica.getUuid() + "&solicitanteId=" + ubs.getUuid()
                + "&competencia=" + MES);
        assertThat((Integer) JsonPath.read(depois, "$.saldo")).isEqualTo(1);
    }

    @Test
    void copiaACompetencia() throws Exception {
        String oferta = oferta(procedimento, policlinica, MES, 6, 201);
        cota(oferta, ubs, 4, 201);
        enviar(post(URL + "ppi/copia"), "{\"origem\": \"%s\", \"destino\": \"%s\"}".formatted(MES, MES.minusMonths(1)), 400);
        String copia = enviar(post(URL + "ppi/copia"), "{\"origem\": \"%s\", \"destino\": \"%s\"}".formatted(MES, MES.plusMonths(1)), 200);
        assertThat((Integer) JsonPath.read(copia, "$.ofertasCopiadas")).isGreaterThanOrEqualTo(1);
        String painel = ler(URL + "ppi?competencia=" + MES.plusMonths(1) + "&executanteId=" + policlinica.getUuid());
        assertThat((List<Integer>) JsonPath.read(painel, "$.ofertas[*].quantidade")).containsExactly(6);
        assertThat((List<Integer>) JsonPath.read(painel, "$.ofertas[0].cotas[*].quantidade")).containsExactly(4);
        String outra = enviar(post(URL + "ppi/copia"), "{\"origem\": \"%s\", \"destino\": \"%s\"}".formatted(MES, MES.plusMonths(1)), 200);
        assertThat((Integer) JsonPath.read(outra, "$.ofertasJaExistentes")).isGreaterThanOrEqualTo(1);
    }
}
