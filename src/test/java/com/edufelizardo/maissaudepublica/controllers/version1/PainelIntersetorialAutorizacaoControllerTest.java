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
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

/**
 * O painel da rede intersetorial com login e autorização ligados (ADR-0211): de INTERSETORIAL.GERENCIAR (gestor e
 * assistente social); a recepção e o psicólogo, que leem o catálogo, não veem o painel nem a lista de cobrança.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = {"app.security.enabled=true", "app.security.authorization.enabled=true"})
class PainelIntersetorialAutorizacaoControllerTest {

    private static final String PREFIXO = "PainelIntersetAut Teste ";
    private static final String GESTOR = "94901867410";
    private static final String ASSISTENTE_SOCIAL = "94901867411";
    private static final String RECEPCAO = "94901867412";
    private static final String PSICOLOGO = "94901867413";
    private static final List<String> CPFS = List.of(GESTOR, ASSISTENTE_SOCIAL, RECEPCAO, PSICOLOGO);

    @Autowired private MockMvc mockMvc;
    @Autowired private JdbcTemplate jdbc;
    @Autowired private JwtService jwtService;
    @Autowired private UnidadeDeSaudeRepository unidadeDeSaudeRepository;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private AtribuicaoAcessoRepository atribuicaoRepository;
    @Autowired private PapelRepository papelRepository;

    private UnidadeDeSaude unidade;
    private UUID instituicao;

    @BeforeEach
    void seed() {
        limpar();
        unidade = new UnidadeDeSaude();
        unidade.setNome(PREFIXO + "UBS");
        unidade.setTipo(TipoUnidadeDeSaude.UBS);
        unidade.setAtivo(true);
        unidade = unidadeDeSaudeRepository.save(unidade);
        atribuir(GESTOR, "GESTOR");
        atribuir(ASSISTENTE_SOCIAL, "ASSISTENTE_SOCIAL");
        atribuir(RECEPCAO, "RECEPCAO");
        atribuir(PSICOLOGO, "PSICOLOGO");
        instituicao = jdbc.queryForObject("insert into tb_instituicao_externa (uuid, nome, setor, tipo, ativa, atualizada_em) "
                + "values (gen_random_uuid(), ?, 'ASSISTENCIA_SOCIAL', 'CRAS', true, now()) returning uuid", UUID.class, PREFIXO + "CRAS");
    }

    @AfterEach
    void limpar() {
        jdbc.update("delete from tb_instituicao_externa where nome like ?", PREFIXO + "%");
        for (String cpf : CPFS) {
            atribuicaoRepository.deleteAll(atribuicaoRepository.findByUsuarioCpf(cpf));
            usuarioRepository.findByCpf(cpf).ifPresent(usuarioRepository::delete);
            jdbc.update("delete from tb_evento_auditoria where usuario_cpf = ?", cpf);
        }
        jdbc.update("delete from tb_unidade_de_saude where nome like ?", PREFIXO + "%");
    }

    private void atribuir(String cpf, String papel) {
        Usuario usuario = usuarioRepository.save(new Usuario(cpf, PREFIXO + cpf, "hash-irrelevante"));
        AtribuicaoAcesso a = new AtribuicaoAcesso();
        a.setUsuario(usuario);
        a.setPapel(papelRepository.findByCodigo(papel).orElseThrow());
        a.setUnidade(unidade);
        a.setConcedidoEm(Instant.now());
        atribuicaoRepository.save(a);
    }

    private int status(String cpf, MockHttpServletRequestBuilder r) throws Exception {
        return mockMvc.perform(r.header("Authorization", "Bearer " + jwtService.gerarToken(cpf, PREFIXO + cpf, false)))
                .andReturn().getResponse().getStatus();
    }

    @Test
    void painelECobrancaSoDeQuemGerenciaARede() throws Exception {
        String painel = "/api/v1/intersetorial/painel";
        String cobranca = "/api/v1/intersetorial/instituicao/" + instituicao + "/sem-retorno";
        for (String cpf : List.of(GESTOR, ASSISTENTE_SOCIAL)) {
            assertThat(status(cpf, get(painel))).as("painel " + cpf).isEqualTo(200);
            assertThat(status(cpf, get(cobranca))).as("cobrança " + cpf).isEqualTo(200);
        }
        for (String cpf : List.of(RECEPCAO, PSICOLOGO)) {
            assertThat(status(cpf, get(painel))).as("painel " + cpf).isEqualTo(403);
            assertThat(status(cpf, get(cobranca))).as("cobrança " + cpf).isEqualTo(403);
            // O catálogo continua aberto a quem está logado.
            assertThat(status(cpf, get("/api/v1/instituicoes-externas"))).as("catálogo " + cpf).isEqualTo(200);
        }
        assertThat(mockMvc.perform(get(painel)).andReturn().getResponse().getStatus()).isEqualTo(401);
    }
}
