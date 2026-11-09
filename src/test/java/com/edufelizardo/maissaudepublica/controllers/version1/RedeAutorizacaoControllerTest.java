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
import java.time.YearMonth;
import java.util.List;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Gestão da Rede com a segurança ligada (ADR-0179): o gestor do município cadastra a oferta da policlínica dele; o de outra unidade não; o
 * médico regulador consulta o saldo; o ACS não vê a PPI.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = {"app.security.enabled=true", "app.security.authorization.enabled=true"})
class RedeAutorizacaoControllerTest {

    private static final String PREFIXO = "RedeAut Teste ";
    private static final String GESTOR = "95001867700";
    private static final String GESTOR_FORA = "95001867701";
    private static final String REGULADOR = "95001867702";
    private static final String ACS = "95001867703";
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
        jdbc.update("delete from tb_cota_ppi where oferta_id in (select uuid from tb_oferta_assistencial where unidade_executante_id in " + unidades + ")");
        jdbc.update("delete from tb_oferta_assistencial where unidade_executante_id in " + unidades);
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
        return r.header("Authorization", "Bearer " + jwtService.gerarToken(cpf, PREFIXO + cpf, false));
    }

    @Test
    void cadaPapelNoSeuLugar() throws Exception {
        YearMonth mes = YearMonth.now().plusMonths(1);
        String oferta = "{\"executanteId\": \"%s\", \"procedimentoId\": \"%s\", \"competencia\": \"%s\", \"quantidade\": 5}"
                .formatted(policlinica.getUuid(), procedimento, mes);
        mockMvc.perform(post("/api/v1/rede/oferta").contentType(MediaType.APPLICATION_JSON).content(oferta)).andExpect(status().isUnauthorized());
        mockMvc.perform(como(GESTOR_FORA, post("/api/v1/rede/oferta")).contentType(MediaType.APPLICATION_JSON).content(oferta)).andExpect(status().isForbidden());
        mockMvc.perform(como(GESTOR, post("/api/v1/rede/oferta")).contentType(MediaType.APPLICATION_JSON).content(oferta)).andExpect(status().isCreated());
        String saldo = "/api/v1/rede/ppi/saldo?procedimentoId=%s&executanteId=%s&solicitanteId=%s&competencia=%s"
                .formatted(procedimento, policlinica.getUuid(), ubs.getUuid(), mes);
        mockMvc.perform(como(REGULADOR, get(saldo))).andExpect(status().isOk());
        mockMvc.perform(como(ACS, get("/api/v1/rede/ppi"))).andExpect(status().isForbidden());
    }
}
