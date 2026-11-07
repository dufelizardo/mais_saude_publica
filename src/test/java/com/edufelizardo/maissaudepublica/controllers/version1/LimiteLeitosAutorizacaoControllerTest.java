package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.models.AtribuicaoAcesso;
import com.edufelizardo.maissaudepublica.models.UnidadeDeSaude;
import com.edufelizardo.maissaudepublica.models.Usuario;
import com.edufelizardo.maissaudepublica.models.enuns.TipoUnidadeDeSaude;
import com.edufelizardo.maissaudepublica.repositories.AtribuicaoAcessoRepository;
import com.edufelizardo.maissaudepublica.repositories.CapacidadeAssistencialRepository;
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

/** Limite de leitos com login e autorização ligados (ADR-0171): a gestão altera na sua unidade; quem só cadastra leitos lê, e não altera. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = {"app.security.enabled=true", "app.security.authorization.enabled=true"})
class LimiteLeitosAutorizacaoControllerTest {

    private static final String PREFIXO = "LimiteAut Teste ";
    private static final String GESTOR = "94501867390";
    private static final String GESTOR_OUTRA = "94501867391";
    private static final String NIR = "94501867392";
    private static final List<String> CPFS = List.of(GESTOR, GESTOR_OUTRA, NIR);

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UnidadeDeSaudeRepository unidadeDeSaudeRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private AtribuicaoAcessoRepository atribuicaoRepository;

    @Autowired
    private PapelRepository papelRepository;

    @Autowired
    private CapacidadeAssistencialRepository capacidadeRepository;

    private UnidadeDeSaude hospital;

    @BeforeEach
    void seed() {
        limpar();
        hospital = unidade("Hospital");
        UnidadeDeSaude outra = unidade("Outra");
        CapacidadesDeTeste.habilitar(jdbc, hospital.getUuid(), "UTI");
        atribuir(GESTOR, "GESTOR", hospital);
        atribuir(GESTOR_OUTRA, "GESTOR", outra);
        atribuir(NIR, "GESTOR_DE_LEITOS", hospital);
    }

    @AfterEach
    void limpar() {
        jdbc.update("delete from tb_limite_leitos where unidade_id in (select uuid from tb_unidade_de_saude where nome like ?)", PREFIXO + "%");
        CapacidadesDeTeste.limpar(jdbc, PREFIXO);
        for (String cpf : CPFS) {
            atribuicaoRepository.deleteAll(atribuicaoRepository.findByUsuarioCpf(cpf));
            usuarioRepository.findByCpf(cpf).ifPresent(usuarioRepository::delete);
            jdbc.update("delete from tb_evento_auditoria where usuario_cpf = ?", cpf);
        }
        List<UUID> unidades = unidadeDeSaudeRepository.findAll().stream()
                .filter(u -> u.getNome() != null && u.getNome().startsWith(PREFIXO)).map(UnidadeDeSaude::getUuid).toList();
        unidadeDeSaudeRepository.deleteAllById(unidades);
    }

    private UnidadeDeSaude unidade(String nome) {
        UnidadeDeSaude u = new UnidadeDeSaude();
        u.setNome(PREFIXO + nome);
        u.setTipo(TipoUnidadeDeSaude.HOSPITAL);
        u.setAtivo(true);
        return unidadeDeSaudeRepository.save(u);
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
        return r.header("Authorization", "Bearer " + jwtService.gerarToken(cpf, PREFIXO + cpf, false));
    }

    @Test
    void gestorAlteraNaSuaUnidadeEQuemCadastraLeitoSoLe() throws Exception {
        String url = "/api/v1/unidade-saude/id/" + hospital.getUuid() + "/limites-de-leitos";
        String corpo = "{\"capacidadeId\": \"%s\", \"quantidade\": 10, \"motivo\": \"Habilitação\"}"
                .formatted(capacidadeRepository.findByCodigo("UTI").orElseThrow().getUuid());

        mockMvc.perform(como(NIR, post(url)).contentType(MediaType.APPLICATION_JSON).content(corpo)).andExpect(status().isForbidden());
        mockMvc.perform(como(GESTOR_OUTRA, post(url)).contentType(MediaType.APPLICATION_JSON).content(corpo)).andExpect(status().isForbidden());
        mockMvc.perform(como(GESTOR, post(url)).contentType(MediaType.APPLICATION_JSON).content(corpo)).andExpect(status().isOk());
        mockMvc.perform(como(NIR, get(url))).andExpect(status().isOk());
        mockMvc.perform(get(url)).andExpect(status().isUnauthorized());
    }
}
