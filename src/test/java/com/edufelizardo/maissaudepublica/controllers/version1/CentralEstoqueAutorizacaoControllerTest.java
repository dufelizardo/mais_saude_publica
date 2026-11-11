package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.models.AtribuicaoAcesso;
import com.edufelizardo.maissaudepublica.models.Profissional;
import com.edufelizardo.maissaudepublica.models.UnidadeDeSaude;
import com.edufelizardo.maissaudepublica.models.Usuario;
import com.edufelizardo.maissaudepublica.models.enuns.TipoUnidadeDeSaude;
import com.edufelizardo.maissaudepublica.repositories.AtribuicaoAcessoRepository;
import com.edufelizardo.maissaudepublica.repositories.PapelRepository;
import com.edufelizardo.maissaudepublica.repositories.ProfissionalRepository;
import com.edufelizardo.maissaudepublica.repositories.UnidadeDeSaudeRepository;
import com.edufelizardo.maissaudepublica.repositories.UsuarioRepository;
import com.edufelizardo.maissaudepublica.services.version1.JwtService;
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
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Escopo do almoxarifado central (ADR-0189), com a segurança ligada: o almoxarife da UBS pede e recebe; o almoxarife da central atende,
 * mas não pede pela UBS; a central não vê o rascunho; o almoxarife de outra unidade recebe 403.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = {"app.security.enabled=true", "app.security.authorization.enabled=true"})
class CentralEstoqueAutorizacaoControllerTest {

    private static final String PREFIXO = "EstCentralAut Teste ";
    private static final String DA_UBS = "92233344455";
    private static final String DA_CENTRAL = "93344455566";
    private static final String DE_FORA = "94455566677";
    private static final String GESTOR = "95566677788";
    private static final List<String> CPFS = List.of(DA_UBS, DA_CENTRAL, DE_FORA, GESTOR);

    @Autowired private MockMvc mockMvc;
    @Autowired private JwtService jwtService;
    @Autowired private JdbcTemplate jdbc;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private PapelRepository papelRepository;
    @Autowired private AtribuicaoAcessoRepository atribuicaoRepository;
    @Autowired private UnidadeDeSaudeRepository unidadeRepository;
    @Autowired private ProfissionalRepository profissionalRepository;

    private UnidadeDeSaude central;
    private UnidadeDeSaude ubs;

    @BeforeEach
    void seed() {
        limpar();
        central = unidade("Central");
        ubs = unidade("UBS");
        UnidadeDeSaude outra = unidade("Outra");
        jdbc.update("insert into tb_unidade_capacidade_assistencial (uuid, unidade_id, capacidade_assistencial_id, inicio) select gen_random_uuid(), ?, uuid, ? "
                + "from tb_capacidade_assistencial where codigo = 'CENTRAL_ABASTECIMENTO'", central.getUuid(), LocalDate.now().minusDays(1));
        usuario(DA_UBS, "ALMOXARIFE", ubs);
        usuario(DA_CENTRAL, "ALMOXARIFE", central);
        usuario(DE_FORA, "ALMOXARIFE", outra);
        usuario(GESTOR, "GESTOR", null);
        for (String[] p : new String[][]{{"ECA-UBS", DA_UBS}, {"ECA-CEN", DA_CENTRAL}}) {
            Profissional prof = new Profissional();
            prof.setMatricula(p[0]);
            prof.setCpf(p[1]);
            prof.setNome(PREFIXO + p[0]);
            prof.setAtivo(true);
            profissionalRepository.save(prof);
        }
    }

    @AfterEach
    void limpar() {
        String unidades = "(select uuid from tb_unidade_de_saude where nome like '" + PREFIXO + "%')";
        String materiais = "(select uuid from tb_material_estoque where nome like '" + PREFIXO + "%')";
        jdbc.update("delete from tb_item_transferencia_estoque where transferencia_id in (select uuid from tb_transferencia_estoque where origem_id in " + unidades + ")");
        jdbc.update("delete from tb_transferencia_estoque where origem_id in " + unidades);
        jdbc.update("delete from tb_item_requisicao_estoque where requisicao_id in (select uuid from tb_requisicao_estoque where unidade_id in " + unidades + ")");
        jdbc.update("delete from tb_requisicao_estoque where unidade_id in " + unidades);
        jdbc.update("delete from tb_movimentacao_estoque where lote_id in (select uuid from tb_lote_material where material_id in " + materiais + ")");
        jdbc.update("delete from tb_lote_material where material_id in " + materiais);
        jdbc.update("delete from tb_material_estoque where nome like '" + PREFIXO + "%'");
        jdbc.update("delete from tb_unidade_capacidade_assistencial where unidade_id in " + unidades);
        for (String cpf : CPFS) {
            atribuicaoRepository.deleteAll(atribuicaoRepository.findByUsuarioCpf(cpf));
            usuarioRepository.findByCpf(cpf).ifPresent(usuarioRepository::delete);
        }
        jdbc.update("delete from tb_profissional where matricula like 'ECA-%'");
        jdbc.update("delete from tb_unidade_de_saude where nome like '" + PREFIXO + "%'");
    }

    @Test
    void ubsPedeCentralAtendeEOutraNaoVe() throws Exception {
        String material = JsonPath.read(mockMvc.perform(como(GESTOR, json(post("/api/v1/estoque/material"), ("{\"codigo\": \"ECA-LUVA\", \"nome\": \"%sLuva\", "
                + "\"categoria\": \"MEDICO_HOSPITALAR\", \"unidadeMedida\": \"UNIDADE\", \"controlaLote\": false}").formatted(PREFIXO))))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString(), "$.uuid");
        mockMvc.perform(como(DA_CENTRAL, json(post("/api/v1/estoque/entrada"), "{\"unidadeId\": \"%s\", \"materialId\": \"%s\", \"quantidade\": 20, \"origem\": \"DOACAO\"}"
                .formatted(central.getUuid(), material)))).andExpect(status().isCreated());

        String corpo = "{\"profissionalMatricula\": \"ECA-UBS\", \"unidadeId\": \"%s\", \"centralId\": \"%s\", \"itens\": [{\"materialId\": \"%s\", \"quantidade\": 5}]}"
                .formatted(ubs.getUuid(), central.getUuid(), material);
        mockMvc.perform(como(DA_CENTRAL, json(post("/api/v1/estoque/requisicao"), corpo))).andExpect(status().isForbidden());
        mockMvc.perform(como(DE_FORA, json(post("/api/v1/estoque/requisicao"), corpo))).andExpect(status().isForbidden());
        String req = JsonPath.read(mockMvc.perform(como(DA_UBS, json(post("/api/v1/estoque/requisicao"), corpo)))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString(), "$.uuid");
        mockMvc.perform(como(DA_CENTRAL, get("/api/v1/estoque/requisicao?centralId=" + central.getUuid()))).andExpect(jsonPath("$").isEmpty());
        mockMvc.perform(como(DA_UBS, json(post("/api/v1/estoque/requisicao/" + req + "/envio"), "{\"profissionalMatricula\": \"ECA-UBS\"}"))).andExpect(status().isOk());
        mockMvc.perform(como(DA_CENTRAL, get("/api/v1/estoque/requisicao?centralId=" + central.getUuid()))).andExpect(jsonPath("$[0].uuid").value(req));

        String atender = "{\"profissionalMatricula\": \"ECA-CEN\", \"itens\": [{\"materialId\": \"%s\", \"quantidade\": 5}]}".formatted(material);
        mockMvc.perform(como(DA_UBS, json(post("/api/v1/estoque/requisicao/" + req + "/atendimento"), atender))).andExpect(status().isForbidden());
        String atendida = mockMvc.perform(como(DA_CENTRAL, json(post("/api/v1/estoque/requisicao/" + req + "/atendimento"), atender)))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        String transferencia = JsonPath.read(atendida, "$.transferenciaId");
        mockMvc.perform(como(DE_FORA, get("/api/v1/estoque/transferencia/" + transferencia))).andExpect(status().isForbidden());
        String item = JsonPath.read(mockMvc.perform(como(DA_UBS, get("/api/v1/estoque/transferencia/" + transferencia))).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(), "$.itens[0].uuid");
        String receber = "{\"profissionalMatricula\": \"ECA-UBS\", \"itens\": [{\"itemId\": \"%s\", \"quantidade\": 5}]}".formatted(item);
        mockMvc.perform(como(DA_CENTRAL, json(post("/api/v1/estoque/transferencia/" + transferencia + "/recebimento"), receber))).andExpect(status().isForbidden());
        mockMvc.perform(como(DA_UBS, json(post("/api/v1/estoque/transferencia/" + transferencia + "/recebimento"), receber))).andExpect(status().isOk());
    }

    private UnidadeDeSaude unidade(String nome) {
        UnidadeDeSaude u = new UnidadeDeSaude();
        u.setNome(PREFIXO + nome);
        u.setTipo(TipoUnidadeDeSaude.UBS);
        u.setAtivo(true);
        return unidadeRepository.save(u);
    }

    private void usuario(String cpf, String papel, UnidadeDeSaude unidade) {
        Usuario usuario = usuarioRepository.findByCpf(cpf).orElseGet(() -> usuarioRepository.save(new Usuario(cpf, PREFIXO + cpf, "hash-irrelevante")));
        AtribuicaoAcesso a = new AtribuicaoAcesso();
        a.setUsuario(usuario);
        a.setPapel(papelRepository.findByCodigo(papel).orElseThrow());
        a.setUnidade(unidade);
        a.setConcedidoEm(Instant.now());
        atribuicaoRepository.save(a);
    }

    private MockHttpServletRequestBuilder como(String cpf, MockHttpServletRequestBuilder r) {
        return r.header("Authorization", "Bearer " + jwtService.gerarToken(cpf, PREFIXO + cpf, false));
    }

    private MockHttpServletRequestBuilder json(MockHttpServletRequestBuilder r, String corpo) {
        return r.contentType(MediaType.APPLICATION_JSON).content(corpo);
    }
}
