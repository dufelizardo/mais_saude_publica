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
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Escopo da ficha do APH (ADR-0188), com a segurança ligada: a equipe da central preenche; o regulador consulta mas não preenche; quem
 * só acompanha a urgência (o gestor) não vê o dado clínico; a equipe de outra central recebe 403.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = {"app.security.enabled=true", "app.security.authorization.enabled=true"})
class FichaAphAutorizacaoControllerTest {

    private static final String PREFIXO = "FichaAphAut Teste ";
    private static final String TARM = "90099100001";
    private static final String REGULADOR = "90099100002";
    private static final String EQUIPE = "90099100003";
    private static final String EQUIPE_OUTRA = "90099100004";
    private static final String GESTOR = "90099100005";
    private static final List<String> CPFS = List.of(TARM, REGULADOR, EQUIPE, EQUIPE_OUTRA, GESTOR);

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PapelRepository papelRepository;

    @Autowired
    private AtribuicaoAcessoRepository atribuicaoRepository;

    @Autowired
    private UnidadeDeSaudeRepository unidadeRepository;

    @Autowired
    private ProfissionalRepository profissionalRepository;

    private UnidadeDeSaude central;

    @BeforeEach
    void seed() {
        limpar();
        central = unidade("Central 192");
        UnidadeDeSaude outra = unidade("Outra central");
        for (UnidadeDeSaude u : List.of(central, outra)) {
            jdbc.update("insert into tb_unidade_capacidade_assistencial (uuid, unidade_id, capacidade_assistencial_id, inicio) select gen_random_uuid(), ?, uuid, ? "
                    + "from tb_capacidade_assistencial where codigo = 'REGULACAO_MEDICA_URGENCIA'", u.getUuid(), LocalDate.now().minusDays(1));
        }
        usuario(TARM, "ATENDENTE_REGULACAO_URGENCIA", central);
        usuario(REGULADOR, "MEDICO_REGULADOR_URGENCIA", central);
        usuario(EQUIPE, "EQUIPE_INTERVENCAO_SAMU", central);
        usuario(EQUIPE_OUTRA, "EQUIPE_INTERVENCAO_SAMU", outra);
        usuario(GESTOR, "GESTOR", central);
        for (String[] p : new String[][]{{"FAA-TARM", TARM}, {"FAA-REG", REGULADOR}, {"FAA-EQP", EQUIPE}}) {
            Profissional prof = new Profissional();
            prof.setMatricula(p[0]);
            prof.setCpf(p[1]);
            prof.setNome(PREFIXO + p[0]);
            prof.setAtivo(true);
            profissionalRepository.save(prof);
        }
    }

    @AfterEach
    void limpar() {
        String unidades = "(select uuid from tb_unidade_de_saude where nome like '" + PREFIXO + "%')";
        String ocorrencias = "(select uuid from tb_ocorrencia_urgencia where central_id in " + unidades + ")";
        String fichas = "(select uuid from tb_ficha_aph where ocorrencia_id in " + ocorrencias + ")";
        jdbc.update("delete from tb_sinais_vitais_aph where ficha_id in " + fichas);
        jdbc.update("delete from tb_ficha_aph_procedimento where ficha_id in " + fichas);
        jdbc.update("delete from tb_ficha_aph where ocorrencia_id in " + ocorrencias);
        jdbc.update("delete from tb_evento_urgencia where ocorrencia_id in " + ocorrencias);
        jdbc.update("delete from tb_ocorrencia_urgencia where central_id in " + unidades);
        jdbc.update("delete from tb_unidade_capacidade_assistencial where unidade_id in " + unidades);
        for (String cpf : CPFS) {
            atribuicaoRepository.deleteAll(atribuicaoRepository.findByUsuarioCpf(cpf));
            usuarioRepository.findByCpf(cpf).ifPresent(usuarioRepository::delete);
        }
        jdbc.update("delete from tb_profissional where matricula like 'FAA-%'");
        jdbc.update("delete from tb_unidade_de_saude where nome like '" + PREFIXO + "%'");
    }

    @Test
    void equipeDaCentralPreencheReguladorConsultaEGestorNaoVe() throws Exception {
        String r = mockMvc.perform(como(TARM, json(post("/api/v1/urgencia/ocorrencia"), ("{\"centralId\": \"%s\", \"origem\": \"CHAMADO_192\", "
                        + "\"solicitanteNome\": \"Vizinha\", \"solicitanteTelefone\": \"11999990000\", \"descricaoPaciente\": \"Mulher, 30 anos\", "
                        + "\"localTipo\": \"VIA_PUBLICA\", \"endereco\": \"Rua B, 20\", \"queixa\": \"Desmaio.\", \"atendenteMatricula\": \"FAA-TARM\"}")
                        .formatted(central.getUuid()))))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        String ocorrencia = JsonPath.read(r, "$.uuid");
        mockMvc.perform(como(REGULADOR, json(post("/api/v1/urgencia/ocorrencia/" + ocorrencia + "/regulacao"),
                "{\"profissionalMatricula\": \"FAA-REG\", \"conduta\": \"Avaliar.\", \"prioridade\": \"AMARELO\", \"decisao\": \"ENVIO_DE_RECURSO\", \"tipoRecurso\": \"USB\"}")))
                .andExpect(status().isOk());

        String corpo = "{\"natureza\": \"CLINICA\", \"situacaoEncontrada\": \"Consciente, pálida.\"}";
        mockMvc.perform(como(REGULADOR, json(put("/api/v1/aph/ocorrencia/" + ocorrencia + "/ficha"), corpo))).andExpect(status().isForbidden());
        mockMvc.perform(como(EQUIPE_OUTRA, json(put("/api/v1/aph/ocorrencia/" + ocorrencia + "/ficha"), corpo))).andExpect(status().isForbidden());
        String f = mockMvc.perform(como(EQUIPE, json(put("/api/v1/aph/ocorrencia/" + ocorrencia + "/ficha"), corpo)))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        String ficha = JsonPath.read(f, "$.uuid");
        mockMvc.perform(como(EQUIPE, json(post("/api/v1/aph/ficha/" + ficha + "/sinais-vitais"), "{\"profissionalMatricula\": \"FAA-EQP\", \"saturacao\": 97}")))
                .andExpect(status().isOk());

        mockMvc.perform(como(REGULADOR, get("/api/v1/aph/ocorrencia/" + ocorrencia + "/ficha"))).andExpect(status().isOk());
        mockMvc.perform(como(GESTOR, get("/api/v1/aph/ocorrencia/" + ocorrencia + "/ficha"))).andExpect(status().isForbidden());
        mockMvc.perform(como(EQUIPE_OUTRA, get("/api/v1/aph/ocorrencia/" + ocorrencia + "/ficha"))).andExpect(status().isForbidden());
        mockMvc.perform(como(EQUIPE_OUTRA, get("/api/v1/aph/atendimentos?centralId=" + central.getUuid())))
                .andExpect(status().isOk()).andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$").isEmpty());
    }

    private UnidadeDeSaude unidade(String nome) {
        UnidadeDeSaude u = new UnidadeDeSaude();
        u.setNome(PREFIXO + nome);
        u.setTipo(TipoUnidadeDeSaude.UPA);
        u.setAtivo(true);
        return unidadeRepository.save(u);
    }

    private void usuario(String cpf, String papel, UnidadeDeSaude unidade) {
        Usuario usuario = usuarioRepository.findByCpf(cpf).orElseGet(() -> usuarioRepository.save(new Usuario(cpf, PREFIXO + cpf, "hash-irrelevante")));
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

    private MockHttpServletRequestBuilder json(MockHttpServletRequestBuilder r, String corpo) {
        return r.contentType(MediaType.APPLICATION_JSON).content(corpo);
    }
}
