package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.models.AtribuicaoAcesso;
import com.edufelizardo.maissaudepublica.models.Paciente;
import com.edufelizardo.maissaudepublica.models.Profissional;
import com.edufelizardo.maissaudepublica.models.UnidadeDeSaude;
import com.edufelizardo.maissaudepublica.models.Usuario;
import com.edufelizardo.maissaudepublica.models.enuns.TipoUnidadeDeSaude;
import com.edufelizardo.maissaudepublica.repositories.AtribuicaoAcessoRepository;
import com.edufelizardo.maissaudepublica.repositories.PacienteRepository;
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
import java.time.ZoneId;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Transporte eletivo com a segurança ligada (ADR-0190): a recepção pede na sua unidade e não autoriza o TFD; a comissão de TFD autoriza e
 * lê o laudo; o coordenador de frota programa a viagem do veículo da garagem dele e não vê o laudo; a recepção não vê a agenda de viagens.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = {"app.security.enabled=true", "app.security.authorization.enabled=true"})
class TransporteEletivoAutorizacaoControllerTest {

    private static final String PREFIXO = "TransporteEletivoAut Teste ";
    private static final String URL = "/api/v1/transporte/";
    private static final String RECEPCAO = "94901867710";
    private static final String COMISSAO = "94901867711";
    private static final String FROTA = "94901867712";
    private static final List<String> CPFS = List.of(RECEPCAO, COMISSAO, FROTA);

    @Autowired private MockMvc mockMvc;
    @Autowired private JdbcTemplate jdbc;
    @Autowired private JwtService jwtService;
    @Autowired private UnidadeDeSaudeRepository unidadeRepository;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private AtribuicaoAcessoRepository atribuicaoRepository;
    @Autowired private PapelRepository papelRepository;
    @Autowired private PacienteRepository pacienteRepository;
    @Autowired private ProfissionalRepository profissionalRepository;

    private UnidadeDeSaude ubs;
    private UnidadeDeSaude outra;
    private UnidadeDeSaude garagem;
    private Paciente paciente;

    @BeforeEach
    void seed() {
        limpar();
        ubs = unidade("UBS", "Cidade Alfa");
        outra = unidade("Outra", "Cidade Alfa");
        garagem = unidade("Garagem", "Cidade Alfa");
        atribuir(RECEPCAO, "RECEPCAO", ubs);
        atribuir(COMISSAO, "COMISSAO_TFD", null);
        atribuir(FROTA, "COORDENADOR_DE_FROTA", garagem);
        Paciente p = new Paciente();
        p.setNome(PREFIXO + "Paciente");
        p.setCpf("90180000001");
        p.setDataNascimento(LocalDate.now().minusYears(40));
        p.setAtivo(true);
        paciente = pacienteRepository.save(p);
        for (String m : List.of("TEA-MEDICO", "TEA-COND")) {
            Profissional prof = new Profissional();
            prof.setMatricula(m);
            prof.setCpf("9018" + String.format("%07d", Math.abs(m.hashCode()) % 10_000_000));
            prof.setNome(PREFIXO + m);
            prof.setAtivo(true);
            profissionalRepository.save(prof);
        }
    }

    @AfterEach
    void limpar() {
        String unidades = "(select uuid from tb_unidade_de_saude where nome like '" + PREFIXO + "%')";
        String veiculos = "(select uuid from tb_veiculo where unidade_id in " + unidades + ")";
        jdbc.update("delete from tb_embarque_viagem where viagem_id in (select uuid from tb_viagem_transporte where veiculo_id in " + veiculos + ")");
        jdbc.update("delete from tb_viagem_transporte where veiculo_id in " + veiculos);
        jdbc.update("delete from tb_solicitacao_transporte where unidade_id in " + unidades);
        jdbc.update("delete from tb_situacao_veiculo where veiculo_id in " + veiculos);
        jdbc.update("delete from tb_veiculo where unidade_id in " + unidades);
        for (String cpf : CPFS) {
            atribuicaoRepository.deleteAll(atribuicaoRepository.findByUsuarioCpf(cpf));
            usuarioRepository.findByCpf(cpf).ifPresent(usuarioRepository::delete);
            jdbc.update("delete from tb_evento_auditoria where usuario_cpf = ?", cpf);
        }
        jdbc.update("delete from tb_paciente where nome like ?", PREFIXO + "%");
        jdbc.update("delete from tb_profissional where matricula like 'TEA-%'");
        jdbc.update("delete from tb_unidade_de_saude where nome like ?", PREFIXO + "%");
    }

    private UnidadeDeSaude unidade(String nome, String municipio) {
        UnidadeDeSaude u = new UnidadeDeSaude();
        u.setNome(PREFIXO + nome);
        u.setTipo(TipoUnidadeDeSaude.UBS);
        u.setMunicipio(municipio);
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
        return r.header("Authorization", "Bearer " + jwtService.gerarToken(cpf, PREFIXO + cpf, false))
                .contentType(MediaType.APPLICATION_JSON);
    }

    private String tfd(UnidadeDeSaude u) {
        return ("{\"pacienteId\": \"%s\", \"unidadeId\": \"%s\", \"motivo\": \"TFD\", \"trecho\": \"IDA_E_VOLTA\", \"dataHora\": \"%sT10:00\", "
                + "\"enderecoEmbarque\": \"Rua A, 1\", \"destino\": \"Hospital de Beta\", \"destinoMunicipio\": \"Cidade Beta\", \"cid\": \"C50\", "
                + "\"justificativa\": \"Radioterapia\", \"profissionalMatricula\": \"TEA-MEDICO\"}")
                .formatted(paciente.getUuid(), u.getUuid(), LocalDate.now(ZoneId.systemDefault()).plusDays(1));
    }

    @Test
    void cadaPapelNoSeuLugar() throws Exception {
        mockMvc.perform(get(URL + "solicitacao")).andExpect(status().isUnauthorized());
        // A recepção pede na sua unidade, não noutra; o coordenador de frota não pede.
        mockMvc.perform(como(RECEPCAO, post(URL + "solicitacao")).content(tfd(outra))).andExpect(status().isForbidden());
        mockMvc.perform(como(FROTA, post(URL + "solicitacao")).content(tfd(garagem))).andExpect(status().isForbidden());
        String r = mockMvc.perform(como(RECEPCAO, post(URL + "solicitacao")).content(tfd(ubs))).andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        String id = JsonPath.read(r, "$.uuid");

        // A recepção não autoriza; a comissão autoriza e lê o laudo; a frota vê a fila sem o laudo.
        mockMvc.perform(como(RECEPCAO, post(URL + "solicitacao/" + id + "/autorizacao"))).andExpect(status().isForbidden());
        mockMvc.perform(como(COMISSAO, get(URL + "solicitacao/" + id))).andExpect(status().isOk());
        r = mockMvc.perform(como(COMISSAO, post(URL + "solicitacao/" + id + "/autorizacao"))).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        assertThat((String) JsonPath.read(r, "$.cid")).isEqualTo("C50");
        r = mockMvc.perform(como(FROTA, get(URL + "solicitacao?situacao=AUTORIZADA"))).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        assertThat((List<String>) JsonPath.read(r, "$[*].uuid")).contains(id);
        assertThat((List<Object>) JsonPath.read(r, "$[?(@.uuid == '" + id + "')].cid")).containsOnlyNulls();

        // A frota programa a viagem do veículo da garagem; a recepção não vê a agenda.
        r = mockMvc.perform(como(FROTA, post(URL + "veiculo")).content(("{\"placa\": \"TEA1A11\", \"prefixo\": \"TEA-VAN\", \"tipo\": \"VAN_SANITARIA\", "
                + "\"unidadeId\": \"%s\", \"lugares\": 8}").formatted(garagem.getUuid()))).andExpect(status().isCreated()).andReturn().getResponse()
                .getContentAsString();
        String van = JsonPath.read(r, "$.uuid");
        Instant saida = LocalDate.now(ZoneId.systemDefault()).plusDays(1).atTime(6, 0).atZone(ZoneId.systemDefault()).toInstant();
        String viagem = "{\"veiculoId\": \"%s\", \"condutorMatricula\": \"TEA-COND\", \"saidaPrevista\": \"%s\", \"retornoPrevisto\": \"%s\", \"solicitacoes\": [\"%s\"]}"
                .formatted(van, saida, saida.plusSeconds(12 * 3600), id);
        mockMvc.perform(como(RECEPCAO, post(URL + "viagem")).content(viagem)).andExpect(status().isForbidden());
        mockMvc.perform(como(FROTA, post(URL + "viagem")).content(viagem)).andExpect(status().isCreated());
        mockMvc.perform(como(RECEPCAO, get(URL + "viagem"))).andExpect(status().isForbidden());
        mockMvc.perform(como(COMISSAO, get(URL + "viagem"))).andExpect(status().isOk());
    }
}
