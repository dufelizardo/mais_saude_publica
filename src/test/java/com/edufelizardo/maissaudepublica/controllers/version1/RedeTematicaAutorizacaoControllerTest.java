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
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Redes temáticas e referência com a segurança ligada (ADR-0197): o gestor do município registra a referência das suas unidades e não a de
 * outra; o gestor de uma unidade não altera o catálogo, que é da rede inteira; o médico regulador consulta a sugestão e não registra; o ACS
 * não vê os painéis; sem login, 401.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = {"app.security.enabled=true", "app.security.authorization.enabled=true"})
class RedeTematicaAutorizacaoControllerTest {

    private static final String PREFIXO = "RedeTemAut Teste ";
    private static final String GESTOR = "95101867700";
    private static final String GESTOR_FORA = "95101867701";
    private static final String REGULADOR = "95101867702";
    private static final String ACS = "95101867703";
    private static final List<String> CPFS = List.of(GESTOR, GESTOR_FORA, REGULADOR, ACS);

    @Autowired private MockMvc mockMvc;
    @Autowired private JdbcTemplate jdbc;
    @Autowired private JwtService jwtService;
    @Autowired private UnidadeDeSaudeRepository unidadeRepository;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private AtribuicaoAcessoRepository atribuicaoRepository;
    @Autowired private PapelRepository papelRepository;

    private UnidadeDeSaude policlinica;
    private UnidadeDeSaude ubs;
    private String procedimento;

    @BeforeEach
    void seed() {
        limpar();
        UnidadeDeSaude municipio = unidade("Município", TipoUnidadeDeSaude.MUNICIPAL, null);
        policlinica = unidade("Policlínica", TipoUnidadeDeSaude.POLICLINICA, municipio);
        ubs = unidade("UBS", TipoUnidadeDeSaude.UBS, municipio);
        UnidadeDeSaude outra = unidade("Outra", TipoUnidadeDeSaude.UBS, null);
        procedimento = UUID.randomUUID().toString();
        jdbc.update("insert into tb_procedimento_regulado (uuid, nome, tipo, ativo) values (cast(? as uuid), ?, 'CONSULTA_ESPECIALIZADA', true)",
                procedimento, PREFIXO + "Neurologia");
        atribuir(GESTOR, "GESTOR", municipio);
        atribuir(GESTOR_FORA, "GESTOR", outra);
        atribuir(REGULADOR, "MEDICO_REGULADOR", municipio);
        atribuir(ACS, "AGENTE_COMUNITARIO_SAUDE", ubs);
    }

    @AfterEach
    void limpar() {
        String unidades = "(select uuid from tb_unidade_de_saude where nome like '" + PREFIXO + "%')";
        jdbc.update("delete from tb_referencia_rede where origem_id in " + unidades);
        jdbc.update("delete from tb_procedimento_regulado where nome like ?", PREFIXO + "%");
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

    private MockHttpServletRequestBuilder como(String cpf, MockHttpServletRequestBuilder r) {
        return r.header("Authorization", "Bearer " + jwtService.gerarToken(cpf, PREFIXO + cpf, false)).contentType(MediaType.APPLICATION_JSON);
    }

    @Test
    void cadaPapelNoSeuLugar() throws Exception {
        String referencia = "{\"origemId\": \"%s\", \"procedimentoId\": \"%s\", \"executanteId\": \"%s\"}".formatted(ubs.getUuid(), procedimento, policlinica.getUuid());
        String sugestao = "/api/v1/rede/referencia/sugestao?procedimentoId=%s&solicitanteId=%s".formatted(procedimento, ubs.getUuid());
        String item = "{\"capacidadeId\": \"%s\"}".formatted(jdbc.queryForObject(
                "select cast(uuid as varchar) from tb_capacidade_assistencial where codigo = 'ODONTO_ENDODONTIA'", String.class));

        mockMvc.perform(post("/api/v1/rede/referencia").contentType(MediaType.APPLICATION_JSON).content(referencia)).andExpect(status().isUnauthorized());
        mockMvc.perform(como(GESTOR_FORA, post("/api/v1/rede/referencia")).content(referencia)).andExpect(status().isForbidden());
        mockMvc.perform(como(REGULADOR, post("/api/v1/rede/referencia")).content(referencia)).andExpect(status().isForbidden());
        mockMvc.perform(como(GESTOR, post("/api/v1/rede/referencia")).content(referencia)).andExpect(status().isCreated());
        mockMvc.perform(como(REGULADOR, get(sugestao))).andExpect(status().isOk());
        mockMvc.perform(como(GESTOR, get("/api/v1/rede/tematica/URGENCIA_EMERGENCIA"))).andExpect(status().isOk());
        mockMvc.perform(como(GESTOR, post("/api/v1/rede/tematica/componente/DC_ESPECIALIZADA/item")).content(item)).andExpect(status().isForbidden());
        mockMvc.perform(como(ACS, get("/api/v1/rede/tematica/URGENCIA_EMERGENCIA"))).andExpect(status().isForbidden());
        mockMvc.perform(como(ACS, get("/api/v1/rede/referencia"))).andExpect(status().isForbidden());
    }
}
