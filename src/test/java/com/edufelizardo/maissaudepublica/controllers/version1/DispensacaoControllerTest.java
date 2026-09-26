package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.models.Lote;
import com.edufelizardo.maissaudepublica.models.Medicamento;
import com.edufelizardo.maissaudepublica.models.Paciente;
import com.edufelizardo.maissaudepublica.models.Profissional;
import com.edufelizardo.maissaudepublica.models.UnidadeDeSaude;
import com.edufelizardo.maissaudepublica.repositories.DispensacaoRepository;
import com.edufelizardo.maissaudepublica.repositories.LoteRepository;
import com.edufelizardo.maissaudepublica.repositories.MedicamentoRepository;
import com.edufelizardo.maissaudepublica.repositories.PacienteRepository;
import com.edufelizardo.maissaudepublica.repositories.ProfissionalRepository;
import com.edufelizardo.maissaudepublica.repositories.UnidadeDeSaudeRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Testes do DispensacaoController (Farmácia — ver MAPA-DE-DOMINIOS.md #9, ADR-0051). Só
 * criação/leitura (sem PATCH, decisão da ADR-0051). Mesmo padrão de engenharia de teste dos demais
 * controllers: SEM {@code @Transactional} na classe, limpeza manual via {@code @AfterEach}
 * incondicional para Dispensacao/Lote (único teste que os cria).
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class DispensacaoControllerTest {

    private static final String DISPENSACAO_URL = "/api/v1/dispensacao/";
    private static final String LOTE_URL = "/api/v1/lote/";
    private static final String MEDICAMENTO_URL = "/api/v1/medicamento/";
    private static final String UNIDADE_SAUDE_URL = "/api/v1/unidade-saude/";
    private static final String FEDERAL_URL = "/api/v1/federal/";
    private static final String ESTADUAL_URL = "/api/v1/estadual/";
    private static final String MUNICIPAL_URL = "/api/v1/municipal/";
    private static final String PROFISSIONAL_URL = "/api/v1/profissional/";
    private static final String PACIENTE_URL = "/api/v1/paciente/";
    private static final String PREFIXO_NOME_TESTE = "Dispensacao Teste - ";
    private static final String PREFIXO_CPF_TESTE = "77733322";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private DispensacaoRepository dispensacaoRepository;

    @Autowired
    private LoteRepository loteRepository;

    @Autowired
    private MedicamentoRepository medicamentoRepository;

    @Autowired
    private UnidadeDeSaudeRepository unidadeDeSaudeRepository;

    @Autowired
    private ProfissionalRepository profissionalRepository;

    @Autowired
    private PacienteRepository pacienteRepository;

    @AfterEach
    void limparDadosDeTeste() {
        // Dispensacao/Lote não têm campo de teste próprio para filtrar sem navegar associações LAZY
        // fora de transação. Este é o único teste que os cria, então apagar tudo é seguro.
        // Dispensacao sai primeiro: referencia Lote, que seria bloqueado pela FK.
        dispensacaoRepository.deleteAll();
        loteRepository.deleteAll();

        List<Medicamento> medicamentos = medicamentoRepository.findAll().stream()
                .filter(m -> m.getNome() != null && m.getNome().startsWith(PREFIXO_NOME_TESTE))
                .toList();
        medicamentoRepository.deleteAll(medicamentos);

        List<Paciente> pacientes = pacienteRepository.findAll().stream()
                .filter(p -> p.getCpf() != null && p.getCpf().startsWith(PREFIXO_CPF_TESTE))
                .toList();
        pacienteRepository.deleteAll(pacientes);

        List<Profissional> profissionais = profissionalRepository.findAll().stream()
                .filter(p -> p.getCpf() != null && p.getCpf().startsWith(PREFIXO_CPF_TESTE))
                .toList();
        profissionalRepository.deleteAll(profissionais);

        List<UnidadeDeSaude> unidades = unidadeDeSaudeRepository.findAll().stream()
                .filter(u -> u.getNome() != null && u.getNome().startsWith(PREFIXO_NOME_TESTE))
                .toList();
        unidadeDeSaudeRepository.deleteAll(unidades);
    }

    private UUID criarUnidadeSaudeUbs(String sufixo) throws Exception {
        String nomeFederal = PREFIXO_NOME_TESTE + "Federal " + sufixo;
        String nomeEstadual = PREFIXO_NOME_TESTE + "Estadual " + sufixo;
        String nomeMunicipal = PREFIXO_NOME_TESTE + "Municipal " + sufixo;
        String nomeUnidade = PREFIXO_NOME_TESTE + "UBS " + sufixo;

        mockMvc.perform(post(FEDERAL_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nome": "%s",
                                  "tipo": "FEDERAL",
                                  "endereco": {
                                    "cep": "70058-900",
                                    "logradouro": "Esplanada dos Ministérios",
                                    "numeroLogradouro": "Bloco G",
                                    "bairro": "Zona Cívico-Administrativa",
                                    "cidade": "Brasília",
                                    "estado": "DF"
                                  },
                                  "email": "contato@saude.gov.br"
                                }
                                """.formatted(nomeFederal)))
                .andExpect(status().isCreated());

        mockMvc.perform(post(ESTADUAL_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nome": "%s",
                                  "tipo": "ESTADUAL",
                                  "administracaoSuperior": "%s",
                                  "estado": "SP",
                                  "endereco": {
                                    "cep": "01037-000",
                                    "logradouro": "Rua Conselheiro Crispiniano",
                                    "numeroLogradouro": "20",
                                    "bairro": "Centro",
                                    "cidade": "São Paulo",
                                    "estado": "SP"
                                  },
                                  "email": "contato@saude.sp.gov.br"
                                }
                                """.formatted(nomeEstadual, nomeFederal)))
                .andExpect(status().isCreated());

        mockMvc.perform(post(MUNICIPAL_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nome": "%s",
                                  "tipo": "MUNICIPAL",
                                  "administracaoSuperior": "%s",
                                  "municipio": "São Paulo",
                                  "endereco": {
                                    "cep": "02012-040",
                                    "logradouro": "Rua Padre Marchetti",
                                    "numeroLogradouro": "557",
                                    "bairro": "Ipiranga",
                                    "cidade": "São Paulo",
                                    "estado": "SP"
                                  },
                                  "email": "contato@prefeitura.sp.gov.br"
                                }
                                """.formatted(nomeMunicipal, nomeEstadual)))
                .andExpect(status().isCreated());

        mockMvc.perform(post(UNIDADE_SAUDE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nome": "%s",
                                  "tipo": "UBS",
                                  "administracaoSuperior": "%s",
                                  "endereco": {
                                    "cep": "02012-040",
                                    "logradouro": "Rua Padre Marchetti",
                                    "numeroLogradouro": "557",
                                    "bairro": "Ipiranga",
                                    "cidade": "São Paulo",
                                    "estado": "SP"
                                  },
                                  "email": "contato@ubs.sp.gov.br"
                                }
                                """.formatted(nomeUnidade, nomeMunicipal)))
                .andExpect(status().isCreated());

        return unidadeDeSaudeRepository.findByNome(nomeUnidade).orElseThrow().getUuid();
    }

    private UUID criarMedicamentoEBuscarUuid(String sufixo) throws Exception {
        String nome = PREFIXO_NOME_TESTE + "Medicamento " + sufixo;
        mockMvc.perform(post(MEDICAMENTO_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nome": "%s",
                                  "principioAtivo": "Dipirona Sódica",
                                  "apresentacao": "Comprimido 500mg",
                                  "codigo": "COD-%s",
                                  "ativo": true
                                }
                                """.formatted(nome, sufixo)))
                .andExpect(status().isCreated());

        return medicamentoRepository.findAll().stream()
                .filter(m -> nome.equals(m.getNome()))
                .findFirst()
                .orElseThrow()
                .getUuid();
    }

    private String criarProfissionalEBuscarMatricula(String cpf, String nome) throws Exception {
        mockMvc.perform(post(PROFISSIONAL_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "cpf": "%s",
                                  "nome": "%s",
                                  "endereco": {
                                    "cep": "01310-100",
                                    "logradouro": "Avenida Paulista",
                                    "numeroLogradouro": "1000",
                                    "bairro": "Bela Vista",
                                    "cidade": "São Paulo",
                                    "estado": "SP"
                                  },
                                  "telefones": ["011-2063-7185"],
                                  "email": "profissional@saude.sp.gov.br"
                                }
                                """.formatted(cpf, nome)))
                .andExpect(status().isCreated());

        return profissionalRepository.findByCpfAndAtivoTrue(cpf).orElseThrow().getMatricula();
    }

    private UUID criarPacienteEBuscarUuid(String cpf, String nome) throws Exception {
        mockMvc.perform(post(PACIENTE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nome": "%s",
                                  "cpf": "%s",
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
                                  "ativo": true
                                }
                                """.formatted(nome, cpf)))
                .andExpect(status().isCreated());

        return pacienteRepository.findByCpf(cpf).stream().findFirst().orElseThrow().getUuid();
    }

    private record DispensacaoSeed(UUID loteId, UUID pacienteId, String profissionalMatricula) {
    }

    private DispensacaoSeed criarDependencias(String sufixo, int quantidadeLote) throws Exception {
        UUID medicamentoId = criarMedicamentoEBuscarUuid(sufixo);
        UUID unidadeId = criarUnidadeSaudeUbs(sufixo);
        String matricula = criarProfissionalEBuscarMatricula(PREFIXO_CPF_TESTE + sufixo, "Profissional " + sufixo);
        UUID pacienteId = criarPacienteEBuscarUuid(PREFIXO_CPF_TESTE + sufixo, "Paciente " + sufixo);

        mockMvc.perform(post(LOTE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "medicamentoId": "%s",
                                  "unidadeId": "%s",
                                  "numeroLote": "L%s",
                                  "validade": "2027-01-01",
                                  "quantidade": %d
                                }
                                """.formatted(medicamentoId, unidadeId, sufixo, quantidadeLote)))
                .andExpect(status().isCreated());

        UUID loteId = loteRepository.findAll().stream()
                .filter(l -> l.getMedicamento().getUuid().equals(medicamentoId))
                .findFirst()
                .orElseThrow()
                .getUuid();

        return new DispensacaoSeed(loteId, pacienteId, matricula);
    }

    private String corpoDispensacao(UUID loteId, UUID pacienteId, String profissionalMatricula, int quantidade) {
        return """
                {
                  "loteId": "%s",
                  "pacienteId": "%s",
                  "profissionalMatricula": "%s",
                  "quantidade": %d,
                  "dataHora": "2026-01-01T14:00:00"
                }
                """.formatted(loteId, pacienteId, profissionalMatricula, quantidade);
    }

    @Test
    void deveCriarComSucessoEDecrementarLote() throws Exception {
        DispensacaoSeed seed = criarDependencias("01", 100);

        mockMvc.perform(post(DISPENSACAO_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpoDispensacao(seed.loteId(), seed.pacienteId(), seed.profissionalMatricula(), 30)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Dispensação registrada com sucesso!"));

        Lote lote = loteRepository.findById(seed.loteId()).orElseThrow();
        assertThat(lote.getQuantidade()).isEqualTo(70);
    }

    @Test
    void deveRetornarBadRequestAoCriarSemCamposObrigatorios() throws Exception {
        mockMvc.perform(post(DISPENSACAO_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details").value("Erro de Validação"));
    }

    @Test
    void deveRetornarNotFoundQuandoLoteNaoExiste() throws Exception {
        UUID unidadeId = criarUnidadeSaudeUbs("02");
        String matricula = criarProfissionalEBuscarMatricula(PREFIXO_CPF_TESTE + "02", "Profissional Dois");
        UUID pacienteId = criarPacienteEBuscarUuid(PREFIXO_CPF_TESTE + "02", "Paciente Dois");

        mockMvc.perform(post(DISPENSACAO_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpoDispensacao(UUID.randomUUID(), pacienteId, matricula, 10)))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveRetornarUnprocessableEntityQuandoEstoqueInsuficiente() throws Exception {
        DispensacaoSeed seed = criarDependencias("03", 10);

        mockMvc.perform(post(DISPENSACAO_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpoDispensacao(seed.loteId(), seed.pacienteId(), seed.profissionalMatricula(), 50)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.details").value("Unprocessable Entity"));

        Lote lote = loteRepository.findById(seed.loteId()).orElseThrow();
        assertThat(lote.getQuantidade()).isEqualTo(10);
    }

    @Test
    void deveBuscarPorId() throws Exception {
        DispensacaoSeed seed = criarDependencias("04", 100);

        mockMvc.perform(post(DISPENSACAO_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpoDispensacao(seed.loteId(), seed.pacienteId(), seed.profissionalMatricula(), 20)))
                .andExpect(status().isCreated());

        UUID uuid = dispensacaoRepository.findAll().stream()
                .filter(d -> d.getLote().getUuid().equals(seed.loteId()))
                .findFirst()
                .orElseThrow()
                .getUuid();

        mockMvc.perform(get(DISPENSACAO_URL + uuid))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantidade").value(20))
                .andExpect(jsonPath("$.consultaUuid").value(org.hamcrest.Matchers.nullValue()));
    }

    @Test
    void deveRetornarNotFoundAoBuscarIdInexistente() throws Exception {
        mockMvc.perform(get(DISPENSACAO_URL + UUID.randomUUID()))
                .andExpect(status().isNotFound());
    }
}
