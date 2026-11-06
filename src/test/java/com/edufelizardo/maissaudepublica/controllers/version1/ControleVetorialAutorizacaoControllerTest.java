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
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

/**
 * Controle de vetores com login e autorização ligados (ADR-0162): o agente de endemias vistoria e não planeja ação; a
 * vigilância ambiental planeja; a vigilância epidemiológica e o gestor leem os índices; a recepção, não.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = {"app.security.enabled=true", "app.security.authorization.enabled=true"})
class ControleVetorialAutorizacaoControllerTest {

    private static final String PREFIXO = "VetorAut Teste ";
    private static final String CODIGO = "TSTV4A";
    private static final String AGENTE = "94911867401";
    private static final String AMBIENTAL = "94911867402";
    private static final String EPIDEMIOLOGICA = "94911867403";
    private static final String GESTOR = "94911867404";
    private static final String RECEPCAO = "94911867405";
    private static final List<String> CPFS = List.of(AGENTE, AMBIENTAL, EPIDEMIOLOGICA, GESTOR, RECEPCAO);

    @Autowired private MockMvc mockMvc;
    @Autowired private JdbcTemplate jdbc;
    @Autowired private JwtService jwtService;
    @Autowired private UnidadeDeSaudeRepository unidadeDeSaudeRepository;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private AtribuicaoAcessoRepository atribuicaoRepository;
    @Autowired private PapelRepository papelRepository;
    @Autowired private ProfissionalRepository profissionalRepository;

    private UnidadeDeSaude unidade;
    private String microarea;
    private String imovel;

    @BeforeEach
    void seed() {
        limpar();
        unidade = new UnidadeDeSaude();
        unidade.setNome(PREFIXO + "Unidade de Zoonoses");
        unidade.setTipo(TipoUnidadeDeSaude.UBS);
        unidade.setAtivo(true);
        unidade = unidadeDeSaudeRepository.save(unidade);
        UUID segmento = UUID.randomUUID();
        UUID territorio = UUID.randomUUID();
        UUID area = UUID.randomUUID();
        UUID m = UUID.randomUUID();
        UUID im = UUID.randomUUID();
        jdbc.update("insert into tb_segmento_territorial (uuid, codigo, nome, tipo) values (?, ?, ?, 'DISTRITO_SANITARIO')", segmento, CODIGO + "SEG", PREFIXO + "Distrito");
        jdbc.update("insert into tb_territorio (uuid, segmento_id, nome, finalidade, ativo) values (?, ?, ?, 'VIGILANCIA', true)", territorio, segmento, PREFIXO + "Território");
        jdbc.update("insert into tb_area (uuid, territorio_id, codigo, nome) values (?, ?, '01', ?)", area, territorio, PREFIXO + "Área");
        jdbc.update("insert into tb_microarea (uuid, area_id, codigo) values (?, ?, '01')", m, area);
        jdbc.update("insert into tb_imovel (uuid, microarea_id, tipo, ativo, logradouro) values (?, ?, 'DOMICILIO', true, 'Rua A')", im, m);
        microarea = m.toString();
        imovel = im.toString();
        Profissional p = new Profissional();
        p.setMatricula("VETA-ACE");
        p.setCpf("90166600002");
        p.setNome(PREFIXO + "Agente");
        p.setAtivo(true);
        profissionalRepository.save(p);
        atribuir(AGENTE, "AGENTE_DE_ENDEMIAS");
        atribuir(AMBIENTAL, "VIGILANCIA_AMBIENTAL");
        atribuir(EPIDEMIOLOGICA, "VIGILANCIA_EPIDEMIOLOGICA");
        atribuir(GESTOR, "GESTOR");
        atribuir(RECEPCAO, "RECEPCAO");
    }

    @AfterEach
    void limpar() {
        String microareas = "(select uuid from tb_microarea where area_id in (select a.uuid from tb_area a join tb_territorio t on t.uuid = a.territorio_id "
                + "join tb_segmento_territorial s on s.uuid = t.segmento_id where s.codigo like '" + CODIGO + "%'))";
        String vistorias = "(select uuid from tb_vistoria_vetorial where imovel_id in (select uuid from tb_imovel where microarea_id in " + microareas + "))";
        jdbc.update("delete from tb_foco_vetorial where vistoria_id in " + vistorias);
        jdbc.update("delete from tb_vistoria_vetorial where uuid in " + vistorias);
        for (UUID a : jdbc.queryForList("select acao_id from tb_acao_controle_vetorial_microarea where microarea_id in " + microareas, UUID.class)) {
            jdbc.update("delete from tb_acao_controle_vetorial_microarea where acao_id = ?", a);
            jdbc.update("delete from tb_acao_controle_vetorial where uuid = ?", a);
        }
        jdbc.update("delete from tb_animal_zoonoses where nome = 'Totó' and endereco = 'Rua B, 10'");
        jdbc.update("delete from tb_amostra_agua where ponto_id in (select uuid from tb_ponto_coleta_agua where nome = 'VetorAut Poço')");
        jdbc.update("delete from tb_ponto_coleta_agua where nome = 'VetorAut Poço'");
        TerritorioControllerTest.limparDados(jdbc, PREFIXO, CODIGO);
        for (String cpf : CPFS) {
            atribuicaoRepository.deleteAll(atribuicaoRepository.findByUsuarioCpf(cpf));
            usuarioRepository.findByCpf(cpf).ifPresent(usuarioRepository::delete);
            jdbc.update("delete from tb_evento_auditoria where usuario_cpf = ?", cpf);
        }
        jdbc.update("delete from tb_profissional where matricula like 'VETA-%'");
        jdbc.update("delete from tb_unidade_de_saude where nome like '" + PREFIXO + "%'");
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

    private MockHttpServletRequestBuilder como(String cpf, MockHttpServletRequestBuilder r) {
        return r.header("Authorization", "Bearer " + jwtService.gerarToken(cpf, PREFIXO + cpf, false));
    }

    private int status(String cpf, MockHttpServletRequestBuilder r) throws Exception {
        return mockMvc.perform(r.header("Authorization", "Bearer " + jwtService.gerarToken(cpf, PREFIXO + cpf, false))).andReturn().getResponse().getStatus();
    }

    private static MockHttpServletRequestBuilder json(MockHttpServletRequestBuilder r, String corpo) {
        return r.contentType(MediaType.APPLICATION_JSON).content(corpo);
    }

    @Test
    void controleVetorialPorPapel() throws Exception {
        String vistoria = "{\"imovelId\": \"" + imovel + "\", \"agenteMatricula\": \"VETA-ACE\", \"tipo\": \"ROTINA\", \"resultado\": \"INSPECIONADA\", \"depositosB\": 2}";
        // A vistoria é do agente; a recepção e a vigilância epidemiológica não vistoriam.
        assertThat(status(RECEPCAO, json(post("/api/v1/vetores/vistoria"), vistoria))).isEqualTo(403);
        assertThat(status(EPIDEMIOLOGICA, json(post("/api/v1/vetores/vistoria"), vistoria))).isEqualTo(403);
        assertThat(status(AGENTE, json(post("/api/v1/vetores/vistoria"), vistoria))).isEqualTo(201);
        // A ação de controle é da coordenação (vigilância ambiental e gestor); o agente não planeja.
        String acao = "{\"tipo\": \"NEBULIZACAO\", \"microareaIds\": [\"" + microarea + "\"], \"dataPrevista\": \"" + LocalDate.now() + "\", \"responsavel\": \"Zoonoses\"}";
        assertThat(status(AGENTE, json(post("/api/v1/vetores/acao"), acao))).isEqualTo(403);
        assertThat(status(AMBIENTAL, json(post("/api/v1/vetores/acao"), acao))).isEqualTo(201);
        // Os índices: a vigilância epidemiológica e o gestor leem; a recepção, não.
        assertThat(status(EPIDEMIOLOGICA, get("/api/v1/vetores/indices"))).isEqualTo(200);
        assertThat(status(GESTOR, get("/api/v1/vetores/indices"))).isEqualTo(200);
        assertThat(status(RECEPCAO, get("/api/v1/vetores/indices"))).isEqualTo(403);
        // Os casos sem bloqueio mostram o lugar do caso: coordenação e investigação; o agente, não.
        assertThat(status(AMBIENTAL, get("/api/v1/vetores/bloqueios-pendentes"))).isEqualTo(200);
        assertThat(status(EPIDEMIOLOGICA, get("/api/v1/vetores/bloqueios-pendentes"))).isEqualTo(200);
        assertThat(status(AGENTE, get("/api/v1/vetores/bloqueios-pendentes"))).isEqualTo(403);
    }

    @Test
    void zoonosesPorPapel() throws Exception {
        String animal = "{\"especie\": \"CAO\", \"nome\": \"Totó\", \"endereco\": \"Rua B, 10\"}";
        // O animal é registro do agente; a recepção não registra nem lê (o tutor é dado pessoal).
        assertThat(status(RECEPCAO, json(post("/api/v1/zoonoses/animal"), animal))).isEqualTo(403);
        assertThat(status(AGENTE, json(post("/api/v1/zoonoses/animal"), animal))).isEqualTo(201);
        assertThat(status(RECEPCAO, get("/api/v1/zoonoses/animais"))).isEqualTo(403);
        assertThat(status(EPIDEMIOLOGICA, get("/api/v1/zoonoses/observacoes"))).isEqualTo(200);
        // A vacinação antirrábica agregada: o gestor lê; a recepção, não.
        assertThat(status(GESTOR, get("/api/v1/zoonoses/vacinacao-antirrabica"))).isEqualTo(200);
        assertThat(status(RECEPCAO, get("/api/v1/zoonoses/vacinacao-antirrabica"))).isEqualTo(403);
    }

    @Test
    void vigilanciaAmbientalPorPapel() throws Exception {
        String ponto = "{\"nome\": \"VetorAut Poço\", \"tipo\": \"REDE\", \"local\": \"Praça\"}";
        // O ponto é da coordenação; o agente não cadastra, mas registra a amostra (ADR-0164).
        assertThat(status(AGENTE, json(post("/api/v1/ambiental/ponto-agua"), ponto))).isEqualTo(403);
        String r = mockMvc.perform(como(AMBIENTAL, json(post("/api/v1/ambiental/ponto-agua"), ponto))).andReturn().getResponse()
                .getContentAsString(java.nio.charset.StandardCharsets.UTF_8);
        String id = com.jayway.jsonpath.JsonPath.read(r, "$.uuid");
        String amostra = "{\"pontoId\": \"" + id + "\", \"turbidez\": 1}";
        assertThat(status(RECEPCAO, json(post("/api/v1/ambiental/amostra-agua"), amostra))).isEqualTo(403);
        assertThat(status(AGENTE, json(post("/api/v1/ambiental/amostra-agua"), amostra))).isEqualTo(201);
        // A leitura agregada: o gestor e a vigilância epidemiológica; a recepção, não.
        assertThat(status(GESTOR, get("/api/v1/ambiental/amostras-agua"))).isEqualTo(200);
        assertThat(status(EPIDEMIOLOGICA, get("/api/v1/ambiental/riscos"))).isEqualTo(200);
        assertThat(status(RECEPCAO, get("/api/v1/ambiental/riscos"))).isEqualTo(403);
    }
}
