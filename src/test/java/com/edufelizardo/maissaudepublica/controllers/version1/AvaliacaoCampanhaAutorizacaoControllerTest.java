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
import java.time.LocalDate;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Avaliação da campanha com a segurança ligada (ADR-0196): o gestor (COMUNICACAO.PUBLICAR) apura e avalia a campanha da sua unidade e não a
 * de outra; o médico não apura nem avalia; sem login, 401.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = {"app.security.enabled=true", "app.security.authorization.enabled=true"})
class AvaliacaoCampanhaAutorizacaoControllerTest {

    private static final String PREFIXO = "AvalCampAut Teste ";
    private static final String GESTOR = "94601867700";
    private static final String MEDICO = "94601867701";
    private static final List<String> CPFS = List.of(GESTOR, MEDICO);

    @Autowired private MockMvc mockMvc;
    @Autowired private JdbcTemplate jdbc;
    @Autowired private JwtService jwtService;
    @Autowired private UnidadeDeSaudeRepository unidadeRepository;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private AtribuicaoAcessoRepository atribuicaoRepository;
    @Autowired private PapelRepository papelRepository;

    private String daUbs;
    private String daOutra;

    @BeforeEach
    void seed() {
        limpar();
        UnidadeDeSaude ubs = unidade("UBS");
        UnidadeDeSaude outra = unidade("Outra");
        atribuir(GESTOR, "GESTOR", ubs);
        atribuir(MEDICO, "MEDICO", ubs);
        daUbs = campanha(ubs);
        daOutra = campanha(outra);
    }

    @AfterEach
    void limpar() {
        String campanhas = "(select uuid from tb_campanha_comunicacao where nome like '" + PREFIXO + "%')";
        jdbc.update("delete from tb_avaliacao_campanha where campanha_id in " + campanhas);
        jdbc.update("delete from tb_campanha_comunicacao where nome like '" + PREFIXO + "%'");
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

    private String campanha(UnidadeDeSaude u) {
        return jdbc.queryForObject("insert into tb_campanha_comunicacao (uuid, nome, objetivo, inicio, fim, situacao, publico_alvo, unidade_id, registrada_em) values "
                + "(gen_random_uuid(), ?, 'Informar', ?, ?, 'ENCERRADA', 'POPULACAO_GERAL', ?, now()) returning cast(uuid as varchar)", String.class,
                PREFIXO + u.getNome(), LocalDate.now().minusDays(20), LocalDate.now().minusDays(1), u.getUuid());
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
    void gestorAvaliaNaPropriaUnidade() throws Exception {
        String corpo = "{\"publicoAtingido\": \"SIM\", \"conclusoes\": \"Boa adesão.\"}";
        String url = "/api/v1/comunicacao/campanha/%s/avaliacao";
        mockMvc.perform(get(url.formatted(daUbs))).andExpect(status().isUnauthorized());
        mockMvc.perform(como(MEDICO, get(url.formatted(daUbs)))).andExpect(status().isForbidden());
        mockMvc.perform(como(MEDICO, post(url.formatted(daUbs))).content(corpo)).andExpect(status().isForbidden());
        mockMvc.perform(como(GESTOR, get(url.formatted(daOutra)))).andExpect(status().isForbidden());
        mockMvc.perform(como(GESTOR, post(url.formatted(daOutra))).content(corpo)).andExpect(status().isForbidden());
        mockMvc.perform(como(GESTOR, get(url.formatted(daUbs)))).andExpect(status().isOk());
        mockMvc.perform(como(GESTOR, post(url.formatted(daUbs))).content(corpo)).andExpect(status().isOk());
    }
}
