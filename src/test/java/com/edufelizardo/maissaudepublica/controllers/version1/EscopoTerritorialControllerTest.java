package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.models.Adscricao;
import com.edufelizardo.maissaudepublica.models.Area;
import com.edufelizardo.maissaudepublica.models.AtribuicaoAcesso;
import com.edufelizardo.maissaudepublica.models.AtribuicaoMicroarea;
import com.edufelizardo.maissaudepublica.models.Endereco;
import com.edufelizardo.maissaudepublica.models.Equipe;
import com.edufelizardo.maissaudepublica.models.Imovel;
import com.edufelizardo.maissaudepublica.models.MembroEquipe;
import com.edufelizardo.maissaudepublica.models.Microarea;
import com.edufelizardo.maissaudepublica.models.Paciente;
import com.edufelizardo.maissaudepublica.models.Profissional;
import com.edufelizardo.maissaudepublica.models.ResponsabilidadeTerritorial;
import com.edufelizardo.maissaudepublica.models.SegmentoTerritorial;
import com.edufelizardo.maissaudepublica.models.Territorio;
import com.edufelizardo.maissaudepublica.models.UnidadeDeSaude;
import com.edufelizardo.maissaudepublica.models.Usuario;
import com.edufelizardo.maissaudepublica.models.enuns.FinalidadeTerritorio;
import com.edufelizardo.maissaudepublica.models.enuns.FuncaoEquipe;
import com.edufelizardo.maissaudepublica.models.enuns.OrigemAdscricao;
import com.edufelizardo.maissaudepublica.models.enuns.TipoEquipe;
import com.edufelizardo.maissaudepublica.models.enuns.TipoImovel;
import com.edufelizardo.maissaudepublica.models.enuns.TipoSegmentoTerritorial;
import com.edufelizardo.maissaudepublica.models.enuns.TipoUnidadeDeSaude;
import com.edufelizardo.maissaudepublica.repositories.AdscricaoRepository;
import com.edufelizardo.maissaudepublica.repositories.AreaRepository;
import com.edufelizardo.maissaudepublica.repositories.AtribuicaoAcessoRepository;
import com.edufelizardo.maissaudepublica.repositories.AtribuicaoMicroareaRepository;
import com.edufelizardo.maissaudepublica.repositories.EquipeRepository;
import com.edufelizardo.maissaudepublica.repositories.ImovelRepository;
import com.edufelizardo.maissaudepublica.repositories.MembroEquipeRepository;
import com.edufelizardo.maissaudepublica.repositories.MicroareaRepository;
import com.edufelizardo.maissaudepublica.repositories.PacienteRepository;
import com.edufelizardo.maissaudepublica.repositories.PapelRepository;
import com.edufelizardo.maissaudepublica.repositories.ProfissionalRepository;
import com.edufelizardo.maissaudepublica.repositories.ResponsabilidadeTerritorialRepository;
import com.edufelizardo.maissaudepublica.repositories.SegmentoTerritorialRepository;
import com.edufelizardo.maissaudepublica.repositories.TerritorioRepository;
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

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Escopo territorial (ADR-0129), com login, autorização, escopo e prontuário por vínculo ligados: o ACS vê e cadastra só
 * na sua microárea; o enfermeiro da equipe, na área inteira; quem cadastra sem equipe não cadastra; o gestor não tem
 * restrição territorial; e a adscrição à equipe conta como vínculo para o prontuário.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = {"app.security.enabled=true", "app.security.authorization.enabled=true",
        "app.security.escopo-territorial.enabled=true", "app.security.prontuario-por-vinculo.enabled=true"})
class EscopoTerritorialControllerTest {

    private static final String PREFIXO = "Escopo Teste ";
    private static final String CODIGO = "TSTF4";
    private static final String ACS = "94701867390";
    private static final String ENF = "94701867391";
    private static final String ENF_SEM = "94701867392";
    private static final String GESTOR = "94701867393";
    private static final String MED = "94701867394";
    private static final String MED_FORA = "94701867395";
    private static final List<String> CPFS = List.of(ACS, ENF, ENF_SEM, GESTOR, MED, MED_FORA);

    @Autowired private MockMvc mockMvc;
    @Autowired private JdbcTemplate jdbc;
    @Autowired private JwtService jwtService;
    @Autowired private UnidadeDeSaudeRepository unidadeRepository;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private AtribuicaoAcessoRepository atribuicaoAcessoRepository;
    @Autowired private PapelRepository papelRepository;
    @Autowired private ProfissionalRepository profissionalRepository;
    @Autowired private PacienteRepository pacienteRepository;
    @Autowired private SegmentoTerritorialRepository segmentoRepository;
    @Autowired private TerritorioRepository territorioRepository;
    @Autowired private AreaRepository areaRepository;
    @Autowired private MicroareaRepository microareaRepository;
    @Autowired private EquipeRepository equipeRepository;
    @Autowired private MembroEquipeRepository membroRepository;
    @Autowired private ResponsabilidadeTerritorialRepository responsabilidadeRepository;
    @Autowired private AtribuicaoMicroareaRepository atribuicaoRepository;
    @Autowired private ImovelRepository imovelRepository;
    @Autowired private AdscricaoRepository adscricaoRepository;

    private Area area;
    private Microarea m07;
    private Microarea m08;
    private Imovel no07;
    private Imovel no08;
    private Equipe equipe;
    private Paciente paciente;

    @BeforeEach
    void seed() {
        limpar();
        UnidadeDeSaude ubs = new UnidadeDeSaude();
        ubs.setNome(PREFIXO + "UBS");
        ubs.setTipo(TipoUnidadeDeSaude.UBS);
        ubs.setAtivo(true);
        ubs = unidadeRepository.save(ubs);
        for (String[] u : new String[][]{{ACS, "AGENTE_COMUNITARIO_SAUDE"}, {ENF, "ENFERMEIRO"}, {ENF_SEM, "ENFERMEIRO"}, {GESTOR, "GESTOR"},
                {MED, "MEDICO"}, {MED_FORA, "MEDICO"}}) {
            atribuir(u[0], u[1], ubs);
        }
        SegmentoTerritorial s = segmentoRepository.save(new SegmentoTerritorial(null, CODIGO, "D", TipoSegmentoTerritorial.OUTRO, ubs, null));
        Territorio t = territorioRepository.save(new Territorio(null, s, "APS", FinalidadeTerritorio.ATENCAO_PRIMARIA, true));
        area = areaRepository.save(new Area(null, t, "01", null, "A", null));
        m07 = microareaRepository.save(new Microarea(null, area, "07", null, null, null));
        m08 = microareaRepository.save(new Microarea(null, area, "08", null, null, null));
        equipe = new Equipe();
        equipe.setUnidade(ubs);
        equipe.setTipo(TipoEquipe.ESF);
        equipe.setNome(PREFIXO + "SF");
        equipe.setAtiva(true);
        equipe = equipeRepository.save(equipe);
        LocalDate desde = LocalDate.now().minusMonths(1);
        responsabilidadeRepository.save(new ResponsabilidadeTerritorial(null, area, equipe, desde, null, null, null));
        MembroEquipe acs = membroRepository.save(new MembroEquipe(null, equipe, profissional(ACS), FuncaoEquipe.ACS, desde, null, null, null));
        membroRepository.save(new MembroEquipe(null, equipe, profissional(ENF), FuncaoEquipe.ENFERMEIRO, desde, null, null, null));
        membroRepository.save(new MembroEquipe(null, equipe, profissional(MED), FuncaoEquipe.MEDICO, desde, null, null, null));
        profissional(ENF_SEM);
        profissional(MED_FORA);
        atribuicaoRepository.save(new AtribuicaoMicroarea(null, m07, acs, desde, null, null, null));
        no07 = imovelRepository.save(imovel(m07, "1"));
        no08 = imovelRepository.save(imovel(m08, "2"));
        Paciente p = new Paciente();
        p.setNome(PREFIXO + "Paciente");
        p.setCpf("93800000001");
        p.setAtivo(true);
        paciente = pacienteRepository.save(p);
        adscricaoRepository.save(new Adscricao(null, paciente, equipe, m07, OrigemAdscricao.TERRITORIO, null, desde, null, null, null));
    }

    @AfterEach
    void limpar() {
        for (String cpf : CPFS) {
            atribuicaoAcessoRepository.deleteAll(atribuicaoAcessoRepository.findByUsuarioCpf(cpf));
            usuarioRepository.findByCpf(cpf).ifPresent(usuarioRepository::delete);
            jdbc.update("delete from tb_evento_auditoria where usuario_cpf = ?", cpf);
        }
        jdbc.update("delete from tb_adscricao where paciente_id in (select uuid from tb_paciente where nome like ?)", PREFIXO + "%");
        TerritorioControllerTest.limparDados(jdbc, PREFIXO, CODIGO);
        jdbc.update("delete from tb_evento_auditoria where paciente_id in (select uuid from tb_paciente where nome like ?)", PREFIXO + "%");
        jdbc.update("delete from tb_paciente where nome like ?", PREFIXO + "%");
        jdbc.update("delete from tb_membro_equipe where equipe_id in (select e.uuid from tb_equipe e join tb_unidade_de_saude u on u.uuid = e.unidade_id where u.nome like ?)", PREFIXO + "%");
        jdbc.update("delete from tb_equipe where unidade_id in (select uuid from tb_unidade_de_saude where nome like ?)", PREFIXO + "%");
        jdbc.update("delete from tb_profissional where matricula like ?", "ESC-%");
        jdbc.update("delete from tb_unidade_de_saude where nome like ?", PREFIXO + "%");
    }

    private void atribuir(String cpf, String papel, UnidadeDeSaude unidade) {
        Usuario usuario = usuarioRepository.save(new Usuario(cpf, PREFIXO + cpf, "hash-irrelevante"));
        AtribuicaoAcesso a = new AtribuicaoAcesso();
        a.setUsuario(usuario);
        a.setPapel(papelRepository.findByCodigo(papel).orElseThrow());
        a.setUnidade(unidade);
        a.setConcedidoEm(Instant.now());
        atribuicaoAcessoRepository.save(a);
    }

    private Profissional profissional(String cpf) {
        Profissional p = new Profissional();
        p.setMatricula("ESC-" + cpf.substring(8));
        p.setCpf(cpf);
        p.setNome(PREFIXO + cpf);
        p.setAtivo(true);
        return profissionalRepository.save(p);
    }

    private static Imovel imovel(Microarea m, String numero) {
        return new Imovel(null, m, TipoImovel.DOMICILIO, new Endereco("13000-000", "Rua A", numero, null, "Centro", "Campinas", "SP", null),
                null, null, true, null);
    }

    private MockHttpServletRequestBuilder como(String cpf, MockHttpServletRequestBuilder r) {
        return r.header("Authorization", "Bearer " + jwtService.gerarToken(cpf, PREFIXO + cpf, false));
    }

    private String ler(String cpf, String url, int esperado) throws Exception {
        return mockMvc.perform(como(cpf, get(url))).andExpect(status().is(esperado)).andReturn().getResponse().getContentAsString();
    }

    private String novoImovel(String cpf, Microarea m, int esperado) throws Exception {
        String corpo = "{\"microareaId\": \"%s\", \"tipo\": \"DOMICILIO\", \"endereco\": %s}".formatted(m.getUuid(),
                CadastroTerritorialControllerTest.endereco("Rua B", "9"));
        return mockMvc.perform(como(cpf, post("/api/v1/territorio/imovel")).contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().is(esperado)).andReturn().getResponse().getContentAsString();
    }

    @Test
    void acsNaMicroareaEquipeNaAreaGestorSemRestricao() throws Exception {
        String imoveis = "/api/v1/territorio/area/" + area.getUuid() + "/imoveis";
        assertThat((List<String>) JsonPath.read(ler(ACS, imoveis, 200), "$[*].microareaCodigo")).containsExactly("07");
        ler(ACS, "/api/v1/territorio/imovel/" + no07.getUuid(), 200);
        ler(ACS, "/api/v1/territorio/imovel/" + no08.getUuid(), 403);
        novoImovel(ACS, m08, 403);
        novoImovel(ACS, m07, 201);

        assertThat((List<String>) JsonPath.read(ler(ENF, imoveis, 200), "$[*].microareaCodigo")).contains("07", "08");
        novoImovel(ENF, m08, 201);
        novoImovel(ENF_SEM, m07, 403);
        ler(ENF_SEM, imoveis, 403);
        assertThat((List<?>) JsonPath.read(ler(GESTOR, imoveis, 200), "$")).hasSize(4);

        String escopoAcs = ler(ACS, "/api/v1/territorio/meu-escopo", 200);
        assertThat(JsonPath.<Boolean>read(escopoAcs, "$.restrito")).isTrue();
        assertThat((List<String>) JsonPath.read(escopoAcs, "$.microareas[*].codigo")).containsExactly("07");
        assertThat(JsonPath.<Boolean>read(ler(GESTOR, "/api/v1/territorio/meu-escopo", 200), "$.restrito")).isFalse();
        assertThat((List<String>) JsonPath.read(ler(ENF, "/api/v1/territorio/meu-escopo", 200), "$.areas[*].codigo")).containsExactly("01");
    }

    @Test
    void adscricaoAEquipeEVinculoParaOProntuario() throws Exception {
        ler(MED, "/api/v1/prontuario/" + paciente.getUuid(), 200);
        // O médico da rede que não é da equipe não tem vínculo.
        ler(MED_FORA, "/api/v1/prontuario/" + paciente.getUuid(), 403);
    }
}
