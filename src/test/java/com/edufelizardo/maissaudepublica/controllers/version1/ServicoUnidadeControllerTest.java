package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.models.Leito;
import com.edufelizardo.maissaudepublica.models.Setor;
import com.edufelizardo.maissaudepublica.models.UnidadeDeSaude;
import com.edufelizardo.maissaudepublica.models.enuns.SexoLeito;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoLeito;
import com.edufelizardo.maissaudepublica.models.enuns.TipoLeito;
import com.edufelizardo.maissaudepublica.models.enuns.TipoSetor;
import com.edufelizardo.maissaudepublica.models.enuns.TipoUnidadeDeSaude;
import com.edufelizardo.maissaudepublica.repositories.CapacidadeAssistencialRepository;
import com.edufelizardo.maissaudepublica.repositories.LeitoRepository;
import com.edufelizardo.maissaudepublica.repositories.PerfilOperacionalRepository;
import com.edufelizardo.maissaudepublica.repositories.SetorRepository;
import com.edufelizardo.maissaudepublica.repositories.TipoServicoRepository;
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
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItems;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Serviços da unidade, fatia MO2 (ADR-0165): o catálogo, a capacidade exigida pelo tipo, os setores de um serviço, os leitos
 * que vêm deles e o encerramento como histórico.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ServicoUnidadeControllerTest {

    private static final String UNIDADE = "/api/v1/unidade-saude/id/";
    private static final String TIPO = "/api/v1/tipo-servico/";
    private static final String PREFIXO = "Servico Teste ";
    private static final String CODIGO = "TESTE_MO2_";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private UnidadeDeSaudeRepository unidadeDeSaudeRepository;

    @Autowired
    private SetorRepository setorRepository;

    @Autowired
    private LeitoRepository leitoRepository;

    @Autowired
    private TipoServicoRepository tipoRepository;

    @Autowired
    private PerfilOperacionalRepository perfilRepository;

    @Autowired
    private CapacidadeAssistencialRepository capacidadeRepository;

    private UnidadeDeSaude hospital;

    @BeforeEach
    void seed() throws Exception {
        limpar();
        hospital = unidade("Hospital", TipoUnidadeDeSaude.HOSPITAL);
        comCapacidade(hospital, "INTERNACAO", "UTI");
    }

    @AfterEach
    void limpar() {
        List<UUID> unidades = unidadeDeSaudeRepository.findAll().stream()
                .filter(u -> u.getNome() != null && u.getNome().startsWith(PREFIXO)).map(UnidadeDeSaude::getUuid).toList();
        for (UUID u : unidades) {
            jdbc.update("delete from tb_servico_setor where servico_id in (select uuid from tb_servico_unidade where unidade_id = ?)", u);
            jdbc.update("delete from tb_servico_unidade where unidade_id = ?", u);
            jdbc.update("delete from tb_leito where unidade_id = ?", u);
            jdbc.update("delete from tb_setor where unidade_id = ?", u);
            jdbc.update("delete from tb_unidade_capacidade_assistencial where unidade_id = ?", u);
            jdbc.update("delete from tb_unidade_perfil_operacional where unidade_id = ?", u);
        }
        unidadeDeSaudeRepository.deleteAllById(unidades);
        jdbc.update("delete from tb_tipo_servico where codigo like ?", CODIGO + "%");
    }

    private UnidadeDeSaude unidade(String nome, TipoUnidadeDeSaude tipo) {
        UnidadeDeSaude u = new UnidadeDeSaude();
        u.setNome(PREFIXO + nome);
        u.setTipo(tipo);
        u.setAtivo(true);
        return unidadeDeSaudeRepository.save(u);
    }

    private void comCapacidade(UnidadeDeSaude u, String perfil, String capacidade) throws Exception {
        mockMvc.perform(post(UNIDADE + u.getUuid() + "/modelo-operacional/perfil").contentType(MediaType.APPLICATION_JSON)
                .content("{\"perfilId\": \"%s\"}".formatted(perfilRepository.findByCodigo(perfil).orElseThrow().getUuid())))
                .andExpect(status().isOk());
        mockMvc.perform(post(UNIDADE + u.getUuid() + "/modelo-operacional/capacidade").contentType(MediaType.APPLICATION_JSON)
                .content("{\"capacidadeId\": \"%s\"}".formatted(capacidadeRepository.findByCodigo(capacidade).orElseThrow().getUuid())))
                .andExpect(status().isOk());
    }

    private Setor setor(UnidadeDeSaude u, String nome) {
        Setor s = new Setor();
        s.setUnidade(u);
        s.setNome(nome);
        s.setCodigo(nome.replace(" ", "_").toUpperCase());
        s.setTipo(TipoSetor.ASSISTENCIAL);
        s.setAtivo(true);
        return setorRepository.save(s);
    }

    private void leito(UnidadeDeSaude u, Setor setor, String identificacao, SituacaoLeito situacao) {
        Leito l = new Leito();
        l.setUnidade(u);
        l.setSetor(setor);
        l.setIdentificacao(identificacao);
        l.setTipo(TipoLeito.values()[0]);
        l.setSexo(SexoLeito.values()[0]);
        l.setSituacao(situacao);
        l.setAtivo(true);
        leitoRepository.save(l);
    }

    private UUID tipo(String codigo) {
        return tipoRepository.findByCodigo(codigo).orElseThrow().getUuid();
    }

    private ResultActions incluir(UUID unidade, UUID tipo, String nome, String... setores) throws Exception {
        String ids = String.join("\", \"", setores);
        return mockMvc.perform(post(UNIDADE + unidade + "/servico").contentType(MediaType.APPLICATION_JSON)
                .content("{\"tipoId\": \"%s\", \"nome\": \"%s\", \"vinteQuatroHoras\": true, \"setorIds\": [%s]}"
                        .formatted(tipo, nome, setores.length == 0 ? "" : "\"" + ids + "\"")));
    }

    private String idDoServico(ResultActions r) throws Exception {
        return JsonPath.read(r.andReturn().getResponse().getContentAsString(), "$.servicos[0].uuid");
    }

    @Test
    void cargaInicialTrazOsTiposComACapacidadeExigida() throws Exception {
        mockMvc.perform(get(TIPO))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.codigo == 'UTI')].capacidadeExigidaId").isNotEmpty())
                .andExpect(jsonPath("$[*].codigo", hasItems("PRONTO_SOCORRO", "CENTRO_CIRURGICO", "SALA_DE_VACINA", "FARMACIA")));
    }

    @Test
    void tipoSemCapacidadeExigidaNaUnidadeE422() throws Exception {
        incluir(hospital.getUuid(), tipo("CENTRO_CIRURGICO"), "Centro cirúrgico")
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void tipoSemCapacidadeNoCatalogoEntraEmQualquerUnidade() throws Exception {
        incluir(hospital.getUuid(), tipo("SALA_DE_VACINA"), "Sala de vacina").andExpect(status().isOk());
    }

    @Test
    void nivelDeGestaoNaoTemServico() throws Exception {
        UnidadeDeSaude regional = unidade("Regional", TipoUnidadeDeSaude.REGIONAL);
        incluir(regional.getUuid(), tipo("SALA_DE_VACINA"), "Sala de vacina").andExpect(status().isUnprocessableEntity());
    }

    @Test
    void osLeitosDoServicoVemDosSetores() throws Exception {
        Setor uti = setor(hospital, "UTI 1");
        Setor uti2 = setor(hospital, "UTI 2");
        Setor outro = setor(hospital, "Clínica");
        leito(hospital, uti, "UTI-1", SituacaoLeito.LIVRE);
        leito(hospital, uti, "UTI-2", SituacaoLeito.OCUPADO);
        leito(hospital, uti2, "UTI-3", SituacaoLeito.HIGIENIZACAO);
        leito(hospital, outro, "CL-1", SituacaoLeito.LIVRE);

        incluir(hospital.getUuid(), tipo("UTI"), "UTI Adulto", uti.getUuid().toString(), uti2.getUuid().toString())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.servicos[0].tipoCodigo").value("UTI"))
                .andExpect(jsonPath("$.servicos[0].setores.length()").value(2))
                .andExpect(jsonPath("$.servicos[0].leitos").value(3))
                .andExpect(jsonPath("$.servicos[0].leitosLivres").value(1))
                .andExpect(jsonPath("$.servicos[0].leitosOcupados").value(1))
                .andExpect(jsonPath("$.setoresLivres.length()").value(1))
                .andExpect(jsonPath("$.setoresLivres[0].nome").value("Clínica"));
    }

    @Test
    void setorEmOutroServicoVigenteE409ESetorDeOutraUnidadeE422() throws Exception {
        Setor uti = setor(hospital, "UTI");
        incluir(hospital.getUuid(), tipo("UTI"), "UTI Adulto", uti.getUuid().toString()).andExpect(status().isOk());
        incluir(hospital.getUuid(), tipo("UTI"), "UTI Pediátrica", uti.getUuid().toString()).andExpect(status().isConflict());

        Setor alheio = setor(unidade("Outro", TipoUnidadeDeSaude.HOSPITAL), "UTI");
        incluir(hospital.getUuid(), tipo("UTI"), "UTI Pediátrica", alheio.getUuid().toString()).andExpect(status().isUnprocessableEntity());
    }

    @Test
    void nomeRepetidoEntreOsVigentesE409() throws Exception {
        incluir(hospital.getUuid(), tipo("UTI"), "UTI Adulto").andExpect(status().isOk());
        incluir(hospital.getUuid(), tipo("UTI"), "uti adulto").andExpect(status().isConflict());
    }

    @Test
    void encerrarGuardaOHistoricoELiberaOSetor() throws Exception {
        Setor uti = setor(hospital, "UTI");
        String id = idDoServico(incluir(hospital.getUuid(), tipo("UTI"), "UTI Adulto", uti.getUuid().toString()));

        mockMvc.perform(post(UNIDADE + hospital.getUuid() + "/servico/" + id + "/encerramento").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"motivo\": \"Reforma da ala\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.servicos.length()").value(0))
                .andExpect(jsonPath("$.encerrados[0].motivoFim").value("Reforma da ala"))
                .andExpect(jsonPath("$.setoresLivres[0].nome").value("UTI"));

        mockMvc.perform(post(UNIDADE + hospital.getUuid() + "/servico/" + id + "/encerramento").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"motivo\": \"De novo\"}"))
                .andExpect(status().isUnprocessableEntity());
        mockMvc.perform(patch(UNIDADE + hospital.getUuid() + "/servico/" + id).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\": \"Outro nome\"}"))
                .andExpect(status().isUnprocessableEntity());
        incluir(hospital.getUuid(), tipo("UTI"), "UTI Adulto", uti.getUuid().toString()).andExpect(status().isOk());
    }

    @Test
    void fimAntesDoInicioOuNoFuturoE400() throws Exception {
        String id = idDoServico(incluir(hospital.getUuid(), tipo("UTI"), "UTI Adulto"));
        mockMvc.perform(post(UNIDADE + hospital.getUuid() + "/servico/" + id + "/encerramento").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"motivo\": \"x\", \"fim\": \"%s\"}".formatted(LocalDate.now().plusDays(1))))
                .andExpect(status().isBadRequest());
        mockMvc.perform(post(UNIDADE + hospital.getUuid() + "/servico/" + id + "/encerramento").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"motivo\": \"x\", \"fim\": \"%s\"}".formatted(LocalDate.now().minusDays(1))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void ajustarTrocaOsSetoresEOHorario() throws Exception {
        Setor a = setor(hospital, "A");
        Setor b = setor(hospital, "B");
        String id = idDoServico(incluir(hospital.getUuid(), tipo("UTI"), "UTI Adulto", a.getUuid().toString()));

        mockMvc.perform(patch(UNIDADE + hospital.getUuid() + "/servico/" + id).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\": \"UTI Geral\", \"vinteQuatroHoras\": false, \"horario\": \"7h às 19h\", \"setorIds\": [\"%s\"]}".formatted(b.getUuid())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.servicos[0].nome").value("UTI Geral"))
                .andExpect(jsonPath("$.servicos[0].horario").value("7h às 19h"))
                .andExpect(jsonPath("$.servicos[0].setores[0].nome").value("B"))
                .andExpect(jsonPath("$.setoresLivres[0].nome").value("A"));
    }

    @Test
    void capacidadeNaoEncerraComServicoVigenteQueAExige() throws Exception {
        String id = idDoServico(incluir(hospital.getUuid(), tipo("UTI"), "UTI Adulto"));
        String associacao = JsonPath.read(mockMvc.perform(get(UNIDADE + hospital.getUuid() + "/modelo-operacional"))
                .andReturn().getResponse().getContentAsString(), "$.capacidades[0].uuid");
        String url = UNIDADE + hospital.getUuid() + "/modelo-operacional/capacidade/" + associacao + "/encerramento";

        mockMvc.perform(post(url).contentType(MediaType.APPLICATION_JSON).content("{\"motivo\": \"Fim da habilitação\"}"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("UTI Adulto")));

        mockMvc.perform(post(UNIDADE + hospital.getUuid() + "/servico/" + id + "/encerramento").contentType(MediaType.APPLICATION_JSON)
                .content("{\"motivo\": \"Fim da habilitação\"}")).andExpect(status().isOk());
        mockMvc.perform(post(url).contentType(MediaType.APPLICATION_JSON).content("{\"motivo\": \"Fim da habilitação\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void catalogoCriaEditaEProtegeACapacidadeDeTipoEmUso() throws Exception {
        String corpo = "{\"codigo\": \"%sbanco_leite\", \"nome\": \"Banco de leite\", \"capacidadeExigidaId\": %s}";
        String r = mockMvc.perform(post(TIPO).contentType(MediaType.APPLICATION_JSON).content(corpo.formatted(CODIGO, "null")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.codigo").value(CODIGO + "BANCO_LEITE"))
                .andExpect(jsonPath("$.ativo").value(true))
                .andReturn().getResponse().getContentAsString();
        String id = JsonPath.read(r, "$.uuid");
        mockMvc.perform(post(TIPO).contentType(MediaType.APPLICATION_JSON).content(corpo.formatted(CODIGO, "null"))).andExpect(status().isConflict());

        incluir(hospital.getUuid(), UUID.fromString(id), "Banco de leite").andExpect(status().isOk());
        String capacidade = "\"" + capacidadeRepository.findByCodigo("UTI").orElseThrow().getUuid() + "\"";
        mockMvc.perform(patch(TIPO + id).contentType(MediaType.APPLICATION_JSON).content(corpo.formatted(CODIGO, capacidade)))
                .andExpect(status().isUnprocessableEntity());
        assertThat(tipoRepository.findByCodigo(CODIGO + "BANCO_LEITE").orElseThrow().getCapacidadeExigida()).isNull();
    }

    @Test
    void tipoInativoNaoEntraEmServicoNovo() throws Exception {
        String r = mockMvc.perform(post(TIPO).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"codigo\": \"%sINATIVO\", \"nome\": \"Inativo\", \"ativo\": false}".formatted(CODIGO)))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        incluir(hospital.getUuid(), UUID.fromString(JsonPath.read(r, "$.uuid")), "Inativo").andExpect(status().isUnprocessableEntity());
    }
}
