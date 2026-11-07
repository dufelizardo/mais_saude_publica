package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.models.AtribuicaoAcesso;
import com.edufelizardo.maissaudepublica.models.Paciente;
import com.edufelizardo.maissaudepublica.models.UnidadeDeSaude;
import com.edufelizardo.maissaudepublica.models.Usuario;
import com.edufelizardo.maissaudepublica.models.enuns.Sexo;
import com.edufelizardo.maissaudepublica.models.enuns.TipoUnidadeDeSaude;
import com.edufelizardo.maissaudepublica.repositories.AtribuicaoAcessoRepository;
import com.edufelizardo.maissaudepublica.repositories.PacienteRepository;
import com.edufelizardo.maissaudepublica.repositories.PapelRepository;
import com.edufelizardo.maissaudepublica.repositories.TipoDocumentoRepository;
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
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.AbstractMockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Documentos com login e autorização ligados (ADR-0172): a recepção anexa e vê só o que anexou, sem abrir; o médico abre; outra unidade não anexa. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = {"app.security.enabled=true", "app.security.authorization.enabled=true"})
class DocumentoAutorizacaoControllerTest {

    private static final String PREFIXO = "DocAut Teste ";
    private static final String RECEPCAO = "94501867400";
    private static final String MEDICO = "94501867401";
    private static final String RECEPCAO_OUTRA = "94501867402";
    private static final String AGENTE = "94501867403";
    private static final List<String> CPFS = List.of(RECEPCAO, MEDICO, RECEPCAO_OUTRA, AGENTE);

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
    private PacienteRepository pacienteRepository;

    @Autowired
    private TipoDocumentoRepository tipoRepository;

    private UnidadeDeSaude ubs;
    private UUID paciente;

    @BeforeEach
    void seed() {
        limpar();
        ubs = unidade("UBS");
        UnidadeDeSaude outra = unidade("Outra");
        Paciente p = new Paciente();
        p.setNome(PREFIXO + "Ana");
        p.setCpf("93200000173");
        p.setDataNascimento(LocalDate.of(1980, 1, 1));
        p.setSexo(Sexo.FEMININO);
        p.setAtivo(true);
        paciente = pacienteRepository.save(p).getUuid();
        atribuir(RECEPCAO, "RECEPCAO", ubs);
        atribuir(MEDICO, "MEDICO", ubs);
        atribuir(RECEPCAO_OUTRA, "RECEPCAO", outra);
        atribuir(AGENTE, "AGENTE_DE_ENDEMIAS", ubs);
    }

    @AfterEach
    void limpar() {
        jdbc.update("delete from tb_versao_documento where documento_id in (select uuid from tb_documento where dono_id in (select uuid from tb_paciente where nome like ?))", PREFIXO + "%");
        jdbc.update("delete from tb_documento where dono_id in (select uuid from tb_paciente where nome like ?)", PREFIXO + "%");
        jdbc.update("delete from tb_evento_auditoria where paciente_id in (select uuid from tb_paciente where nome like ?)", PREFIXO + "%");
        for (String cpf : CPFS) {
            atribuicaoRepository.deleteAll(atribuicaoRepository.findByUsuarioCpf(cpf));
            usuarioRepository.findByCpf(cpf).ifPresent(usuarioRepository::delete);
            jdbc.update("delete from tb_evento_auditoria where usuario_cpf = ?", cpf);
        }
        jdbc.update("delete from tb_paciente where nome like ?", PREFIXO + "%");
        List<UUID> unidades = unidadeDeSaudeRepository.findAll().stream()
                .filter(u -> u.getNome() != null && u.getNome().startsWith(PREFIXO)).map(UnidadeDeSaude::getUuid).toList();
        unidadeDeSaudeRepository.deleteAllById(unidades);
    }

    private UnidadeDeSaude unidade(String nome) {
        UnidadeDeSaude u = new UnidadeDeSaude();
        u.setNome(PREFIXO + nome);
        u.setTipo(TipoUnidadeDeSaude.UBS);
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

    private <B extends AbstractMockHttpServletRequestBuilder<B>> B como(String cpf, B r) {
        return r.header("Authorization", "Bearer " + jwtService.gerarToken(cpf, PREFIXO + cpf, false));
    }

    private MockMultipartHttpServletRequestBuilder anexo(String titulo) {
        return multipart("/api/v1/paciente/" + paciente + "/documento")
                .file(new MockMultipartFile("arquivo", "a.pdf", "application/pdf", "%PDF-1.4".getBytes(StandardCharsets.US_ASCII)))
                .param("tipoId", tipoRepository.findByCodigo("DOCUMENTO_PESSOAL").orElseThrow().getUuid().toString())
                .param("unidadeId", ubs.getUuid().toString()).param("titulo", titulo);
    }

    @Test
    void recepcaoAnexaEVeSoOQueAnexouSemAbrir() throws Exception {
        String url = "/api/v1/paciente/" + paciente + "/documentos";
        mockMvc.perform(get(url)).andExpect(status().isUnauthorized());
        mockMvc.perform(como(AGENTE, anexo("Sem permissão"))).andExpect(status().isForbidden());
        mockMvc.perform(como(RECEPCAO_OUTRA, anexo("Outra unidade"))).andExpect(status().isForbidden());

        mockMvc.perform(como(MEDICO, anexo("Do médico"))).andExpect(status().isOk());
        String r = mockMvc.perform(como(RECEPCAO, anexo("RG"))).andExpect(status().isOk())
                .andExpect(jsonPath("$.podeAbrir").value(false))
                .andExpect(jsonPath("$.documentos.length()").value(1))
                .andExpect(jsonPath("$.documentos[0].titulo").value("RG"))
                .andReturn().getResponse().getContentAsString();
        String versao = JsonPath.read(r, "$.documentos[0].versoes[0].uuid");

        mockMvc.perform(como(RECEPCAO, get("/api/v1/documento/versao/" + versao + "/arquivo"))).andExpect(status().isForbidden());
        mockMvc.perform(como(MEDICO, get("/api/v1/documento/versao/" + versao + "/arquivo"))).andExpect(status().isOk());
        mockMvc.perform(como(MEDICO, get(url))).andExpect(status().isOk())
                .andExpect(jsonPath("$.podeAbrir").value(true))
                .andExpect(jsonPath("$.documentos.length()").value(2));
    }
}
