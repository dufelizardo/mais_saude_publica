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
import java.time.ZoneId;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

/**
 * Entregas com login e autorização ligados (ADR-0214): o gestor da secretaria emite a ordem; o fiscal designado (com papel de recepção)
 * recebe, faz o definitivo, que dá entrada no Estoque sem ESTOQUE.MOVIMENTAR, e registra ocorrência, mas não emite ordem nem vê o painel;
 * o gestor do contrato que não é o fiscal acompanha e não recebe; outro profissional não vê a ordem; sem login, 401.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = {"app.security.enabled=true", "app.security.authorization.enabled=true"})
class EntregasAutorizacaoControllerTest {

    private static final String PREFIXO = "EntregasAut Teste ";
    private static final String API = "/api/v1/compras/";
    private static final String COMPRAS = "94999867401";
    private static final String FISCAL = "94999867402";
    private static final String GESTOR_CONTRATO = "94999867403";
    private static final String OUTRO = "94999867404";
    private static final List<String> CPFS = List.of(COMPRAS, FISCAL, GESTOR_CONTRATO, OUTRO);

    @Autowired private MockMvc mockMvc;
    @Autowired private JdbcTemplate jdbc;
    @Autowired private JwtService jwtService;
    @Autowired private UnidadeDeSaudeRepository unidadeDeSaudeRepository;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private AtribuicaoAcessoRepository atribuicaoRepository;
    @Autowired private PapelRepository papelRepository;
    @Autowired private ProfissionalRepository profissionalRepository;

    private final LocalDate hoje = LocalDate.now(ZoneId.of("America/Sao_Paulo"));
    private UnidadeDeSaude ubs;

    @BeforeEach
    void seed() {
        limpar();
        UnidadeDeSaude u = new UnidadeDeSaude();
        u.setNome(PREFIXO + "UBS");
        u.setTipo(TipoUnidadeDeSaude.UBS);
        u.setAtivo(true);
        ubs = unidadeDeSaudeRepository.save(u);
        profissional("ENA-FISCAL", FISCAL);
        profissional("ENA-GESTOR", GESTOR_CONTRATO);
        atribuir(COMPRAS, "GESTOR");
        atribuir(FISCAL, "RECEPCAO");
        atribuir(GESTOR_CONTRATO, "RECEPCAO");
        atribuir(OUTRO, "RECEPCAO");
    }

    @AfterEach
    void limpar() {
        for (String cpf : CPFS) {
            atribuicaoRepository.deleteAll(atribuicaoRepository.findByUsuarioCpf(cpf));
            usuarioRepository.findByCpf(cpf).ifPresent(usuarioRepository::delete);
            jdbc.update("delete from tb_evento_auditoria where usuario_cpf = ?", cpf);
        }
        ComprasControllerTest.limparDados(jdbc, PREFIXO);
        jdbc.update("delete from tb_profissional where matricula like 'ENA-%'");
    }

    private void profissional(String matricula, String cpf) {
        Profissional p = new Profissional();
        p.setMatricula(matricula);
        p.setCpf(cpf);
        p.setNome(PREFIXO + matricula);
        p.setAtivo(true);
        profissionalRepository.save(p);
    }

    private void atribuir(String cpf, String papel) {
        Usuario usuario = usuarioRepository.save(new Usuario(cpf, PREFIXO + cpf, "hash-irrelevante"));
        AtribuicaoAcesso a = new AtribuicaoAcesso();
        a.setUsuario(usuario);
        a.setPapel(papelRepository.findByCodigo(papel).orElseThrow());
        a.setUnidade(ubs);
        a.setConcedidoEm(Instant.now());
        atribuicaoRepository.save(a);
    }

    private MockHttpServletRequestBuilder token(String cpf, MockHttpServletRequestBuilder r) {
        return r.header("Authorization", "Bearer " + jwtService.gerarToken(cpf, PREFIXO + cpf, false));
    }

    private MvcResult como(String cpf, MockHttpServletRequestBuilder r, String corpo) throws Exception {
        return mockMvc.perform(token(cpf, r).contentType(MediaType.APPLICATION_JSON).content(corpo)).andReturn();
    }

    private int codigo(String cpf, MockHttpServletRequestBuilder r, String corpo) throws Exception {
        return como(cpf, r, corpo).getResponse().getStatus();
    }

    private static String corpo(MvcResult r) throws Exception {
        return r.getResponse().getContentAsString(StandardCharsets.UTF_8);
    }

    @Test
    void fiscalRecebe() throws Exception {
        String[] k = EntregasControllerTest.contrato(mockMvc, jdbc, PREFIXO, ubs, hoje, "906100000001", "ENA-GESTOR", "ENA-FISCAL", r -> token(COMPRAS, r));
        String ordem = "{\"unidadeId\": \"" + ubs.getUuid() + "\", \"itens\": [{\"itemInstrumentoId\": \"" + k[2] + "\", \"quantidade\": 10}]}";
        assertThat(codigo(FISCAL, post(API + "instrumento/" + k[0] + "/ordem"), ordem)).isEqualTo(403);
        MvcResult r = como(COMPRAS, post(API + "instrumento/" + k[0] + "/ordem"), ordem);
        assertThat(r.getResponse().getStatus()).as(corpo(r)).isEqualTo(201);
        String ordemId = JsonPath.read(corpo(r), "$.uuid");
        String item = JsonPath.read(corpo(r), "$.itens[0].uuid");

        // Quem vê a ordem.
        assertThat(codigo(FISCAL, get(API + "ordem/" + ordemId), "")).isEqualTo(200);
        assertThat(codigo(GESTOR_CONTRATO, get(API + "instrumento/" + k[0] + "/ordens"), "")).isEqualTo(200);
        assertThat(codigo(OUTRO, get(API + "ordem/" + ordemId), "")).isEqualTo(403);

        // O fiscal recebe; o gestor do contrato e o outro, não.
        String rec = "{\"notaFiscal\": \"NF 1\", \"notaEmitidaEm\": \"" + hoje + "\", \"itens\": [{\"itemOrdemId\": \"" + item + "\", \"quantidade\": 10, "
                + "\"numeroLote\": \"L9\", \"validade\": \"" + hoje.plusYears(1) + "\"}]}";
        assertThat(codigo(GESTOR_CONTRATO, post(API + "ordem/" + ordemId + "/recebimento"), rec)).isEqualTo(403);
        assertThat(codigo(OUTRO, post(API + "ordem/" + ordemId + "/recebimento"), rec)).isEqualTo(403);
        r = como(FISCAL, post(API + "ordem/" + ordemId + "/recebimento"), rec);
        assertThat(r.getResponse().getStatus()).as(corpo(r)).isEqualTo(201);
        String recebimento = JsonPath.read(corpo(r), "$.recebimentos[0].uuid");
        String itemRec = JsonPath.read(corpo(r), "$.recebimentos[0].itens[0].uuid");
        r = como(FISCAL, post(API + "recebimento/" + recebimento + "/definitivo"), "{\"itens\": [{\"itemRecebimentoId\": \"" + itemRec + "\", \"quantidadeAceita\": 10}]}");
        assertThat(r.getResponse().getStatus()).as(corpo(r)).isEqualTo(200);
        assertThat((String) JsonPath.read(corpo(r), "$.situacao")).isEqualTo("RECEBIDA");
        assertThat(jdbc.queryForObject("select count(*) from tb_movimentacao_estoque where fornecedor_id = cast(? as uuid)", Integer.class, k[1])).isEqualTo(1);

        assertThat(codigo(FISCAL, post(API + "ordem/" + ordemId + "/ocorrencia"), "{\"tipo\": \"OUTRA\", \"descricao\": \"Entrega fora do horário.\"}")).isEqualTo(201);
        assertThat(codigo(FISCAL, get(API + "painel"), "")).isEqualTo(403);
        assertThat(codigo(FISCAL, get(API + "ocorrencias"), "")).isEqualTo(403);
        assertThat(codigo(COMPRAS, get(API + "painel"), "")).isEqualTo(200);
        assertThat(mockMvc.perform(get(API + "ordem/" + ordemId)).andReturn().getResponse().getStatus()).isEqualTo(401);
    }
}
