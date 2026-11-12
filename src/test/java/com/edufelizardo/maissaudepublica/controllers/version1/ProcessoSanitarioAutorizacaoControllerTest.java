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
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

/**
 * Processo sanitário com login e autorização ligados (ADR-0200): o fiscal lavra o auto assinando com a própria matrícula e não julga; a
 * coordenação julga; o gestor gerencia e lê as interdições, mas não julga; a verificação pública não pede login.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = {"app.security.enabled=true", "app.security.authorization.enabled=true"})
class ProcessoSanitarioAutorizacaoControllerTest {

    private static final String PREFIXO = "ProcAut Teste ";
    private static final String API = "/api/v1/vigilancia-sanitaria/";
    private static final String FISCAL = "94933867401";
    private static final String COORDENACAO = "94933867402";
    private static final String GESTOR = "94933867403";
    private static final String RECEPCAO = "94933867404";
    private static final List<String> CPFS = List.of(FISCAL, COORDENACAO, GESTOR, RECEPCAO);

    @Autowired private MockMvc mockMvc;
    @Autowired private JdbcTemplate jdbc;
    @Autowired private JwtService jwtService;
    @Autowired private UnidadeDeSaudeRepository unidadeDeSaudeRepository;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private AtribuicaoAcessoRepository atribuicaoRepository;
    @Autowired private PapelRepository papelRepository;
    @Autowired private ProfissionalRepository profissionalRepository;

    private UnidadeDeSaude unidade;

    @BeforeEach
    void seed() {
        limpar();
        unidade = new UnidadeDeSaude();
        unidade.setNome(PREFIXO + "Vigilância Sanitária");
        unidade.setTipo(TipoUnidadeDeSaude.UBS);
        unidade.setAtivo(true);
        unidade = unidadeDeSaudeRepository.save(unidade);
        Profissional p = new Profissional();
        p.setMatricula("PROCA-FISC");
        p.setCpf(FISCAL);
        p.setNome(PREFIXO + "Fiscal");
        p.setAtivo(true);
        profissionalRepository.save(p);
        atribuir(FISCAL, "FISCAL_SANITARIO");
        atribuir(COORDENACAO, "COORDENACAO_VISA");
        atribuir(GESTOR, "GESTOR");
        atribuir(RECEPCAO, "RECEPCAO");
    }

    @AfterEach
    void limpar() {
        VigilanciaSanitariaControllerTest.limparDados(jdbc, PREFIXO, "PROCA-%");
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

    private MvcResult como(String cpf, MockHttpServletRequestBuilder r) throws Exception {
        return mockMvc.perform(r.header("Authorization", "Bearer " + jwtService.gerarToken(cpf, PREFIXO + cpf, false))).andReturn();
    }

    private int status(String cpf, MockHttpServletRequestBuilder r) throws Exception {
        return como(cpf, r).getResponse().getStatus();
    }

    private static MockHttpServletRequestBuilder json(MockHttpServletRequestBuilder r, String corpo) {
        return r.contentType(MediaType.APPLICATION_JSON).content(corpo);
    }

    private static String corpo(MvcResult r) throws Exception {
        return r.getResponse().getContentAsString(StandardCharsets.UTF_8);
    }

    @Test
    void processoPorPapel() throws Exception {
        LocalDate hoje = LocalDate.now();
        String corpoEstab = VigilanciaSanitariaControllerTest.corpoEstabelecimento(VigilanciaSanitariaControllerTest.cnpj(), "Bar", "5611201:MEDIO")
                .replace(VigilanciaSanitariaControllerTest.PREFIXO, PREFIXO);
        String bar = JsonPath.read(corpo(como(FISCAL, json(post(API + "estabelecimento"), corpoEstab))), "$.uuid");
        String insp = corpo(como(FISCAL, json(post(API + "inspecao"), "{\"estabelecimentoId\": \"" + bar + "\", \"motivo\": \"PROGRAMADA\", \"dataInspecao\": \""
                + hoje.minusDays(40) + "\", \"resultado\": \"INSATISFATORIA\", \"relatorio\": \"x\", \"fiscais\": [\"PROCA-FISC\"], \"irregularidades\": "
                + "[{\"descricao\": \"Sem alvará\", \"gravidade\": \"GRAVE\"}]}")));
        String irreg = JsonPath.read(insp, "$.irregularidades[0].uuid");
        String auto = "{\"estabelecimentoId\": \"" + bar + "\", \"irregularidades\": [\"" + irreg + "\"], \"dispositivoLegal\": \"art. 10\", "
                + "\"descricao\": \"x\", \"dataLavratura\": \"" + hoje.minusDays(40) + "\", \"fiscais\": [\"PROCA-FISC\"]}";

        // Só o fiscal lavra, e só com a própria matrícula: a coordenação não é o fiscal informado.
        assertThat(status(RECEPCAO, json(post(API + "processo"), auto))).isEqualTo(403);
        assertThat(status(GESTOR, json(post(API + "processo"), auto))).isEqualTo(403);
        assertThat(status(COORDENACAO, json(post(API + "processo"), auto))).isEqualTo(403);
        MvcResult r = como(FISCAL, json(post(API + "processo"), auto));
        assertThat(r.getResponse().getStatus()).isEqualTo(201);
        String processo = JsonPath.read(corpo(r), "$.uuid");
        String codigo = JsonPath.read(corpo(r), "$.termos[0].codigo");

        // A ciência pelo fiscal; a defesa e o julgamento, não.
        assertThat(status(FISCAL, json(post(API + "processo/" + processo + "/ciencia"), "{\"forma\": \"PESSOAL\", \"data\": \"" + hoje.minusDays(39) + "\"}")))
                .isEqualTo(200);
        String decisao = "{\"resultado\": \"PENALIDADE\", \"fundamentacao\": \"x\", \"penalidades\": [{\"tipo\": \"ADVERTENCIA\"}]}";
        assertThat(status(FISCAL, json(post(API + "processo/" + processo + "/decisao"), decisao))).isEqualTo(403);
        assertThat(status(GESTOR, json(post(API + "processo/" + processo + "/decisao"), decisao))).isEqualTo(403);
        assertThat(status(COORDENACAO, json(post(API + "processo/" + processo + "/decisao"), decisao))).isEqualTo(200);

        // As medidas: o fiscal interdita, a coordenação desinterdita; o gestor lê as interdições pela Sala de Situação.
        r = como(FISCAL, json(post(API + "medida"), "{\"estabelecimentoId\": \"" + bar + "\", \"tipo\": \"INTERDICAO_CAUTELAR\", \"descricao\": \"x\"}"));
        assertThat(r.getResponse().getStatus()).isEqualTo(201);
        String medida = JsonPath.read(corpo(r), "$.uuid");
        assertThat(status(GESTOR, get(API + "interdicoes"))).isEqualTo(200);
        assertThat(status(RECEPCAO, get(API + "interdicoes"))).isEqualTo(403);
        assertThat(status(FISCAL, json(post(API + "medida/" + medida + "/encerramento"), "{\"motivo\": \"x\"}"))).isEqualTo(403);
        assertThat(status(COORDENACAO, json(post(API + "medida/" + medida + "/encerramento"), "{\"motivo\": \"Regularizado.\"}"))).isEqualTo(200);

        // A verificação pública, sem login; o processo, não.
        assertThat(mockMvc.perform(get("/api/v1/publico/visa/verificacao/" + codigo)).andReturn().getResponse().getStatus()).isEqualTo(200);
        assertThat(mockMvc.perform(get(API + "processo/" + processo)).andReturn().getResponse().getStatus()).isEqualTo(401);
        assertThat(status(RECEPCAO, get(API + "processos"))).isEqualTo(403);
    }
}
