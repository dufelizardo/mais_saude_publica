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

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Estoque e frota nas emergências com a segurança ligada (ADR-0191 e ADR-0192): a coordenação pede o veículo e vê as reservas, mas não
 * reserva estoque nem cede veículo; o almoxarife reserva na sua unidade e não noutra; o coordenador de frota cede o veículo da garagem
 * dele; o almoxarife não vê os pedidos de veículo.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = {"app.security.enabled=true", "app.security.authorization.enabled=true"})
class EstoqueFrotaEmergenciaAutorizacaoControllerTest {

    private static final String PREFIXO = "EstFrotaAut Teste ";
    private static final String GESTOR = "94901867720";
    private static final String COORD = "94901867721";
    private static final String ALMOX = "94901867722";
    private static final String FROTA = "94901867723";
    private static final List<String> CPFS = List.of(GESTOR, COORD, ALMOX, FROTA);

    @Autowired private MockMvc mockMvc;
    @Autowired private JdbcTemplate jdbc;
    @Autowired private JwtService jwtService;
    @Autowired private UnidadeDeSaudeRepository unidadeRepository;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private AtribuicaoAcessoRepository atribuicaoRepository;
    @Autowired private PapelRepository papelRepository;

    private UnidadeDeSaude ubs;
    private UnidadeDeSaude outra;
    private UnidadeDeSaude garagem;

    @BeforeEach
    void seed() {
        limpar();
        ubs = unidade("UBS");
        outra = unidade("Outra");
        garagem = unidade("Garagem");
        atribuir(GESTOR, "GESTOR", null);
        atribuir(COORD, "COORDENADOR_DE_ENFERMAGEM", null);
        atribuir(ALMOX, "ALMOXARIFE", ubs);
        atribuir(FROTA, "COORDENADOR_DE_FROTA", garagem);
    }

    @AfterEach
    void limpar() {
        String emergencias = "(select e.uuid from tb_emergencia e join tb_evento_emergencial v on v.uuid = e.evento_id where v.descricao like '" + PREFIXO + "%')";
        String unidades = "(select uuid from tb_unidade_de_saude where nome like '" + PREFIXO + "%')";
        String veiculos = "(select uuid from tb_veiculo where unidade_id in " + unidades + ")";
        String materiais = "(select uuid from tb_material_estoque where nome like '" + PREFIXO + "%')";
        jdbc.update("delete from tb_cessao_veiculo where veiculo_id in " + veiculos);
        jdbc.update("delete from tb_mobilizacao where emergencia_id in " + emergencias);
        jdbc.update("delete from tb_reserva_estoque where material_id in " + materiais);
        jdbc.update("delete from tb_lote_material where material_id in " + materiais);
        jdbc.update("delete from tb_material_estoque where nome like '" + PREFIXO + "%'");
        jdbc.update("delete from tb_mudanca_nivel_emergencia where emergencia_id in " + emergencias);
        jdbc.update("delete from tb_emergencia where uuid in " + emergencias);
        jdbc.update("delete from tb_evento_emergencial where descricao like '" + PREFIXO + "%'");
        jdbc.update("delete from tb_situacao_veiculo where veiculo_id in " + veiculos);
        jdbc.update("delete from tb_veiculo where unidade_id in " + unidades);
        for (String cpf : CPFS) {
            atribuicaoRepository.deleteAll(atribuicaoRepository.findByUsuarioCpf(cpf));
            usuarioRepository.findByCpf(cpf).ifPresent(usuarioRepository::delete);
            jdbc.update("delete from tb_evento_auditoria where usuario_cpf = ?", cpf);
        }
        jdbc.update("delete from tb_unidade_de_saude where nome like ?", PREFIXO + "%");
    }

    private UnidadeDeSaude unidade(String nome) {
        UnidadeDeSaude u = new UnidadeDeSaude();
        u.setNome(PREFIXO + nome);
        u.setTipo(TipoUnidadeDeSaude.UBS);
        u.setAtivo(true);
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

    private MockHttpServletRequestBuilder como(String cpf, MockHttpServletRequestBuilder r) {
        return r.header("Authorization", "Bearer " + jwtService.gerarToken(cpf, PREFIXO + cpf, false)).contentType(MediaType.APPLICATION_JSON);
    }

    private String corpo(org.springframework.test.web.servlet.ResultActions r) throws Exception {
        return r.andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
    }

    @Test
    void cadaPapelNoSeuLugar() throws Exception {
        String tipo = jdbc.queryForObject("select cast(uuid as varchar) from tb_tipo_emergencia where codigo = 'INUNDACAO'", String.class);
        String nivel = jdbc.queryForObject("select cast(uuid as varchar) from tb_nivel_emergencia where ordem = 1", String.class);
        String evento = JsonPath.read(corpo(mockMvc.perform(como(GESTOR, post("/api/v1/evento-emergencial")).content("{\"tipoId\": \"" + tipo
                + "\", \"fonte\": \"DEFESA_CIVIL\", \"descricao\": \"" + PREFIXO + "Enchente\"}")).andExpect(status().isCreated())), "$.uuid");
        String em = JsonPath.read(corpo(mockMvc.perform(como(GESTOR, post("/api/v1/evento-emergencial/" + evento + "/emergencia")).content(
                "{\"nome\": \"Enchente Aut\", \"nivelId\": \"" + nivel + "\", \"justificativa\": \"J\"}")).andExpect(status().isCreated())), "$.uuid");
        String material = JsonPath.read(corpo(mockMvc.perform(como(GESTOR, post("/api/v1/estoque/material")).content("{\"codigo\": \"EFA-KIT\", \"nome\": \""
                + PREFIXO + "Kit\", \"categoria\": \"MEDICO_HOSPITALAR\", \"unidadeMedida\": \"UNIDADE\", \"controlaLote\": false}")).andExpect(status().isCreated())), "$.uuid");
        for (UnidadeDeSaude u : List.of(ubs, outra)) {
            jdbc.update("insert into tb_lote_material (uuid, material_id, unidade_id, quantidade, criado_em) values (?, cast(? as uuid), ?, 50, now())",
                    UUID.randomUUID(), material, u.getUuid());
        }

        // Reserva: a coordenação não reserva; o almoxarife reserva na sua unidade e não noutra.
        String reserva = "{\"emergenciaId\": \"%s\", \"unidadeId\": \"%s\", \"materialId\": \"%s\", \"quantidade\": 10, \"justificativa\": \"Kits\"}";
        mockMvc.perform(como(COORD, post("/api/v1/estoque/reserva")).content(reserva.formatted(em, ubs.getUuid(), material))).andExpect(status().isForbidden());
        mockMvc.perform(como(ALMOX, post("/api/v1/estoque/reserva")).content(reserva.formatted(em, outra.getUuid(), material))).andExpect(status().isForbidden());
        mockMvc.perform(como(ALMOX, post("/api/v1/estoque/reserva")).content(reserva.formatted(em, ubs.getUuid(), material))).andExpect(status().isCreated());
        mockMvc.perform(como(COORD, get("/api/v1/estoque/reserva?emergenciaId=" + em))).andExpect(status().isOk());

        // Veículo: a coordenação pede e não cede; a frota cede o da garagem; o almoxarife não vê os pedidos.
        String d = corpo(mockMvc.perform(como(COORD, post("/api/v1/emergencia/" + em + "/mobilizacao")).content("{\"tipo\": \"VEICULO\", \"destinoId\": \""
                + ubs.getUuid() + "\", \"recurso\": \"Uma van\"}")).andExpect(status().isCreated()));
        String mob = JsonPath.parse(d).read("$.mobilizacoes[0].uuid", String.class);
        String van = JsonPath.read(corpo(mockMvc.perform(como(FROTA, post("/api/v1/transporte/veiculo")).content(("{\"placa\": \"EFA1A11\", \"prefixo\": \"EFA-VAN\", "
                + "\"tipo\": \"VAN_SANITARIA\", \"unidadeId\": \"%s\", \"lugares\": 8}").formatted(garagem.getUuid()))).andExpect(status().isCreated())), "$.uuid");
        String cessao = "{\"veiculoId\": \"" + van + "\"}";
        mockMvc.perform(como(COORD, post("/api/v1/transporte/emergencia/mobilizacao/" + mob + "/cessao")).content(cessao)).andExpect(status().isForbidden());
        mockMvc.perform(como(ALMOX, get("/api/v1/transporte/emergencia/pedidos"))).andExpect(status().isForbidden());
        mockMvc.perform(como(FROTA, get("/api/v1/transporte/emergencia/pedidos"))).andExpect(status().isOk());
        mockMvc.perform(como(FROTA, post("/api/v1/transporte/emergencia/mobilizacao/" + mob + "/cessao")).content(cessao)).andExpect(status().isCreated());
    }
}
