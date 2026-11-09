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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Transporte com a segurança ligada (ADR-0178): o coordenador de frota cadastra só na unidade dele; o atendente da central consulta e
 * não cadastra; a recepção não vê a frota.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = {"app.security.enabled=true", "app.security.authorization.enabled=true"})
class TransporteAutorizacaoControllerTest {

    private static final String PREFIXO = "TransporteAut Teste ";
    private static final String URL = "/api/v1/transporte/";
    private static final String FROTA = "94901867700";
    private static final String TARM = "94901867701";
    private static final String RECEPCAO = "94901867702";
    private static final List<String> CPFS = List.of(FROTA, TARM, RECEPCAO);

    @Autowired private MockMvc mockMvc;
    @Autowired private JdbcTemplate jdbc;
    @Autowired private JwtService jwtService;
    @Autowired private UnidadeDeSaudeRepository unidadeRepository;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private AtribuicaoAcessoRepository atribuicaoRepository;
    @Autowired private PapelRepository papelRepository;

    private UnidadeDeSaude garagem;
    private UnidadeDeSaude outra;

    @BeforeEach
    void seed() {
        limpar();
        garagem = unidade("Garagem");
        outra = unidade("Outra");
        atribuir(FROTA, "COORDENADOR_DE_FROTA", garagem);
        atribuir(TARM, "ATENDENTE_REGULACAO_URGENCIA", garagem);
        atribuir(RECEPCAO, "RECEPCAO", garagem);
    }

    @AfterEach
    void limpar() {
        String unidades = "(select uuid from tb_unidade_de_saude where nome like '" + PREFIXO + "%')";
        jdbc.update("delete from tb_situacao_veiculo where veiculo_id in (select uuid from tb_veiculo where unidade_id in " + unidades + ")");
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
        return r.header("Authorization", "Bearer " + jwtService.gerarToken(cpf, PREFIXO + cpf, false));
    }

    private MockHttpServletRequestBuilder van(UnidadeDeSaude u, String placa) {
        return post(URL + "veiculo").contentType(MediaType.APPLICATION_JSON)
                .content("{\"placa\": \"%s\", \"prefixo\": \"TA-%s\", \"tipo\": \"VAN_SANITARIA\", \"unidadeId\": \"%s\"}".formatted(placa, placa, u.getUuid()));
    }

    @Test
    void cadaPapelNoSeuLugar() throws Exception {
        mockMvc.perform(get(URL + "veiculo")).andExpect(status().isUnauthorized());
        mockMvc.perform(como(FROTA, van(outra, "TAA1A11"))).andExpect(status().isForbidden());
        mockMvc.perform(como(TARM, van(garagem, "TAA1A12"))).andExpect(status().isForbidden());
        mockMvc.perform(como(FROTA, van(garagem, "TAA1A13"))).andExpect(status().isCreated());
        mockMvc.perform(como(TARM, get(URL + "veiculo"))).andExpect(status().isOk());
        mockMvc.perform(como(RECEPCAO, get(URL + "veiculo"))).andExpect(status().isForbidden());
    }
}
