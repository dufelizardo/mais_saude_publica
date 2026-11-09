package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.models.AtribuicaoAcesso;
import com.edufelizardo.maissaudepublica.models.UnidadeDeSaude;
import com.edufelizardo.maissaudepublica.models.Usuario;
import com.edufelizardo.maissaudepublica.models.enuns.TipoUnidadeDeSaude;
import com.edufelizardo.maissaudepublica.repositories.AtribuicaoAcessoRepository;
import com.edufelizardo.maissaudepublica.repositories.PapelRepository;
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
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Estoque com a segurança ligada (ADR-0177): o gestor mantém o catálogo e o mínimo; o almoxarife movimenta só na unidade dele e não
 * cadastra material; a coordenação de enfermagem consulta e não movimenta; o ACS não vê o estoque.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = {"app.security.enabled=true", "app.security.authorization.enabled=true"})
class EstoqueAutorizacaoControllerTest {

    private static final String PREFIXO = "EstoqueAut Teste ";
    private static final String URL = "/api/v1/estoque/";
    private static final String GESTOR = "94801867700";
    private static final String ALMOXARIFE = "94801867701";
    private static final String COORDENACAO = "94801867702";
    private static final String ACS = "94801867703";
    private static final List<String> CPFS = List.of(GESTOR, ALMOXARIFE, COORDENACAO, ACS);

    @Autowired private MockMvc mockMvc;
    @Autowired private JdbcTemplate jdbc;
    @Autowired private JwtService jwtService;
    @Autowired private UnidadeDeSaudeRepository unidadeRepository;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private AtribuicaoAcessoRepository atribuicaoRepository;
    @Autowired private PapelRepository papelRepository;

    private UnidadeDeSaude ubs;
    private UnidadeDeSaude outra;

    @BeforeEach
    void seed() {
        limpar();
        UnidadeDeSaude municipio = unidade("Município", TipoUnidadeDeSaude.MUNICIPAL, null);
        ubs = unidade("UBS", TipoUnidadeDeSaude.UBS, municipio);
        outra = unidade("Outra", TipoUnidadeDeSaude.UBS, municipio);
        atribuir(GESTOR, "GESTOR", municipio);
        atribuir(ALMOXARIFE, "ALMOXARIFE", ubs);
        atribuir(COORDENACAO, "COORDENADOR_DE_ENFERMAGEM", ubs);
        atribuir(ACS, "AGENTE_COMUNITARIO_SAUDE", ubs);
    }

    @AfterEach
    void limpar() {
        String materiais = "(select uuid from tb_material_estoque where nome like '" + PREFIXO + "%')";
        jdbc.update("delete from tb_movimentacao_estoque where lote_id in (select uuid from tb_lote_material where material_id in " + materiais + ")");
        jdbc.update("delete from tb_lote_material where material_id in " + materiais);
        jdbc.update("delete from tb_estoque_minimo where material_id in " + materiais);
        jdbc.update("delete from tb_material_estoque where nome like '" + PREFIXO + "%'");
        for (String cpf : CPFS) {
            atribuicaoRepository.deleteAll(atribuicaoRepository.findByUsuarioCpf(cpf));
            usuarioRepository.findByCpf(cpf).ifPresent(usuarioRepository::delete);
            jdbc.update("delete from tb_evento_auditoria where usuario_cpf = ?", cpf);
        }
        jdbc.update("update tb_unidade_de_saude set unidade_superior_id = null where nome like ?", PREFIXO + "%");
        jdbc.update("delete from tb_unidade_de_saude where nome like ?", PREFIXO + "%");
    }

    private UnidadeDeSaude unidade(String nome, TipoUnidadeDeSaude tipo, UnidadeDeSaude superior) {
        UnidadeDeSaude u = new UnidadeDeSaude();
        u.setNome(PREFIXO + nome);
        u.setTipo(tipo);
        u.setAtivo(true);
        u.setUnidadeSuperior(superior);
        return unidadeRepository.save(u);
    }

    private void atribuir(String cpf, String papel, UnidadeDeSaude unidade) {
        Usuario usuario = usuarioRepository.save(new Usuario(cpf, PREFIXO + cpf, "hash-irrelevante"));
        AtribuicaoAcesso a = new AtribuicaoAcesso();
        a.setUsuario(usuario);
        a.setPapel(papelRepository.findByCodigo(papel).orElseThrow());
        a.setUnidade(unidade);
        a.setConcedidoEm(Instant.now());
        atribuicaoRepository.save(a);
    }

    private MockHttpServletRequestBuilder como(String cpf, MockHttpServletRequestBuilder r, String corpo) {
        r.header("Authorization", "Bearer " + jwtService.gerarToken(cpf, PREFIXO + cpf, false));
        return corpo == null ? r : r.contentType(MediaType.APPLICATION_JSON).content(corpo);
    }

    @Test
    void cadaPapelNoSeuLugar() throws Exception {
        String novo = "{\"codigo\": \"ESA-ALCOOL\", \"nome\": \"%sÁlcool 70%%\", \"categoria\": \"LIMPEZA_HIGIENE\", \"unidadeMedida\": \"LITRO\", \"controlaLote\": false}"
                .formatted(PREFIXO);
        mockMvc.perform(post(URL + "material").contentType(MediaType.APPLICATION_JSON).content(novo)).andExpect(status().isUnauthorized());
        mockMvc.perform(como(ALMOXARIFE, post(URL + "material"), novo)).andExpect(status().isForbidden());
        String material = JsonPath.read(mockMvc.perform(como(GESTOR, post(URL + "material"), novo)).andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString(), "$.uuid");
        mockMvc.perform(como(GESTOR, put(URL + "minimo"), "{\"unidadeId\": \"%s\", \"materialId\": \"%s\", \"quantidadeMinima\": 5}".formatted(ubs.getUuid(), material)))
                .andExpect(status().isOk());

        String entrada = "{\"unidadeId\": \"%s\", \"materialId\": \"%s\", \"quantidade\": 10, \"origem\": \"SALDO_INICIAL\"}";
        mockMvc.perform(como(ALMOXARIFE, post(URL + "entrada"), entrada.formatted(outra.getUuid(), material))).andExpect(status().isForbidden());
        mockMvc.perform(como(COORDENACAO, post(URL + "entrada"), entrada.formatted(ubs.getUuid(), material))).andExpect(status().isForbidden());
        mockMvc.perform(como(ALMOXARIFE, post(URL + "entrada"), entrada.formatted(ubs.getUuid(), material))).andExpect(status().isCreated());

        mockMvc.perform(como(COORDENACAO, get(URL + "saldo?unidadeId=" + ubs.getUuid()), null)).andExpect(status().isOk());
        mockMvc.perform(como(COORDENACAO, get(URL + "saldo?unidadeId=" + outra.getUuid()), null)).andExpect(status().isForbidden());
        mockMvc.perform(como(ACS, get(URL + "saldo?unidadeId=" + ubs.getUuid()), null)).andExpect(status().isForbidden());
        mockMvc.perform(como(GESTOR, get(URL + "saldo?unidadeId=" + outra.getUuid()), null)).andExpect(status().isOk());
    }
}
