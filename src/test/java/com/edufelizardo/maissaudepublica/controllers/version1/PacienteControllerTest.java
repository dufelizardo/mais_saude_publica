package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.models.Paciente;
import com.edufelizardo.maissaudepublica.repositories.PacienteRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Testes do PacienteController (Assistência — ver ADR-0039/ADR-0040). Mesmo padrão de engenharia
 * de teste dos demais controllers: SEM {@code @Transactional} na classe, limpeza manual via
 * {@code @AfterEach} por prefixo de CPF.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PacienteControllerTest {

    private static final String BASE_URL = "/api/v1/paciente/";
    private static final String PREFIXO_CPF_TESTE = "77766655";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PacienteRepository pacienteRepository;

    @AfterEach
    void limparDadosDeTeste() {
        List<Paciente> criadosNoTeste = pacienteRepository.findAll().stream()
                .filter(p -> p.getCpf() != null && p.getCpf().startsWith(PREFIXO_CPF_TESTE))
                .toList();
        pacienteRepository.deleteAll(criadosNoTeste);
    }

    private String corpoPaciente(String cpf, String nome, String cartaoSus, boolean ativo) {
        return """
                {
                  "nome": "%s",
                  "cpf": "%s",
                  "cartaoSus": "%s",
                  "dataNascimento": "1990-05-10",
                  "sexo": "FEMININO",
                  "endereco": {
                    "cep": "01310-100",
                    "logradouro": "Avenida Paulista",
                    "numeroLogradouro": "1000",
                    "bairro": "Bela Vista",
                    "cidade": "São Paulo",
                    "estado": "SP"
                  },
                  "telefones": ["011-2063-7185"],
                  "email": "paciente@exemplo.com",
                  "ativo": %s
                }
                """.formatted(nome, cpf, cartaoSus, ativo);
    }

    private java.util.UUID criarPaciente(String cpf, String nome, String cartaoSus) throws Exception {
        MvcResult result = mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpoPaciente(cpf, nome, cartaoSus, true)))
                .andExpect(status().isCreated())
                .andReturn();

        Paciente criado = pacienteRepository.findByCpf(cpf).stream()
                .filter(p -> p.getNome().equals(nome))
                .findFirst()
                .orElseThrow();
        return criado.getUuid();
    }

    @Test
    void deveCriarComSucesso() throws Exception {
        String cpf = PREFIXO_CPF_TESTE + "01";
        String nome = "Paciente Criação";

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpoPaciente(cpf, nome, "700000000000001", true)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Paciente criado com sucesso!"))
                .andExpect(jsonPath("$.details").value(containsString(nome)));
    }

    @Test
    void deveRetornarBadRequestAoCriarSemEnderecoOuTelefone() throws Exception {
        String cpf = PREFIXO_CPF_TESTE + "02";
        String body = """
                {
                  "nome": "Paciente Sem Endereço",
                  "cpf": "%s",
                  "dataNascimento": "1990-05-10",
                  "sexo": "FEMININO",
                  "ativo": true
                }
                """.formatted(cpf);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details").value("Erro de Validação"));
    }

    @Test
    void deveCriarComSucesso_CpfRepetido_NaoEhConflito() throws Exception {
        // CPF não é único (mesmo raciocínio da ADR-0017 aplicado ao Paciente) — duas fichas com o
        // mesmo CPF sempre devem dar 201.
        String cpf = PREFIXO_CPF_TESTE + "03";
        criarPaciente(cpf, "Paciente Ficha Um", "700000000000002");
        criarPaciente(cpf, "Paciente Ficha Dois", "700000000000003");

        assertThat(pacienteRepository.findByCpf(cpf)).hasSize(2);
    }

    @Test
    void devePermitirCriarSemCartaoSus() throws Exception {
        String cpf = PREFIXO_CPF_TESTE + "04";
        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpoPaciente(cpf, "Paciente Sem CNS", "", true)))
                .andExpect(status().isCreated());
    }

    @Test
    void deveBuscarPorId() throws Exception {
        String cpf = PREFIXO_CPF_TESTE + "05";
        String nome = "Paciente Busca Por Id";
        java.util.UUID uuid = criarPaciente(cpf, nome, "700000000000004");

        mockMvc.perform(get(BASE_URL + uuid))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cpf").value(cpf))
                .andExpect(jsonPath("$.nome").value(nome))
                .andExpect(jsonPath("$.endereco.cidade").value("São Paulo"))
                .andExpect(jsonPath("$.telefones[0]").value("011-2063-7185"));
    }

    @Test
    void deveRetornarNotFoundAoBuscarIdInexistente() throws Exception {
        mockMvc.perform(get(BASE_URL + java.util.UUID.randomUUID()))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveBuscarPorCpf() throws Exception {
        String cpf = PREFIXO_CPF_TESTE + "06";
        String nome = "Paciente Busca Por Cpf";
        criarPaciente(cpf, nome, "700000000000005");

        mockMvc.perform(get(BASE_URL + "cpf/" + cpf))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].cpf").value(cpf))
                .andExpect(jsonPath("$[0].nome").value(nome));
    }

    @Test
    void deveRetornarNotFoundAoBuscarCpfInexistente() throws Exception {
        mockMvc.perform(get(BASE_URL + "cpf/00000000000-nao-existe"))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveBuscarPorCartaoSus() throws Exception {
        String cpf = PREFIXO_CPF_TESTE + "07";
        String nome = "Paciente Busca Por CNS";
        String cartaoSus = "700000000000006";
        criarPaciente(cpf, nome, cartaoSus);

        mockMvc.perform(get(BASE_URL + "cartao-sus/" + cartaoSus))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].cartaoSus").value(cartaoSus))
                .andExpect(jsonPath("$[0].nome").value(nome));
    }

    @Test
    void deveAtualizarDadosCadastrais() throws Exception {
        String cpf = PREFIXO_CPF_TESTE + "08";
        java.util.UUID uuid = criarPaciente(cpf, "Paciente Atualização", "700000000000007");

        String body = """
                {
                  "nome": "Paciente Atualizado",
                  "cpf": "%s",
                  "cartaoSus": "700000000000007",
                  "dataNascimento": "1990-05-10",
                  "sexo": "FEMININO",
                  "endereco": {
                    "cep": "02012-040",
                    "logradouro": "Rua Padre Marchetti",
                    "numeroLogradouro": "557",
                    "bairro": "Ipiranga",
                    "cidade": "Campinas",
                    "estado": "SP"
                  },
                  "telefones": ["019-9999-8888"],
                  "email": "novo-contato@exemplo.com",
                  "ativo": true
                }
                """.formatted(cpf);

        mockMvc.perform(patch(BASE_URL + uuid)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk());

        mockMvc.perform(get(BASE_URL + uuid))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Paciente Atualizado"))
                .andExpect(jsonPath("$.email").value("novo-contato@exemplo.com"))
                .andExpect(jsonPath("$.endereco.cidade").value("Campinas"));
    }

    @Test
    void deveInativarViaAtualizacao() throws Exception {
        String cpf = PREFIXO_CPF_TESTE + "09";
        java.util.UUID uuid = criarPaciente(cpf, "Paciente Inativar", "700000000000008");

        mockMvc.perform(patch(BASE_URL + uuid)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpoPaciente(cpf, "Paciente Inativar", "700000000000008", false)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.details").value(containsString("Ativo: false")));

        Paciente inativado = pacienteRepository.findById(uuid).orElseThrow();
        assertThat(inativado.isAtivo()).isFalse();
    }
}
