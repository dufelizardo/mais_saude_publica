package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.models.Lote;
import com.edufelizardo.maissaudepublica.models.Medicamento;
import com.edufelizardo.maissaudepublica.models.MovimentacaoFarmacia;
import com.edufelizardo.maissaudepublica.models.Paciente;
import com.edufelizardo.maissaudepublica.models.Profissional;
import com.edufelizardo.maissaudepublica.models.UnidadeDeSaude;
import com.edufelizardo.maissaudepublica.models.enuns.TipoMovimentacaoFarmacia;
import com.edufelizardo.maissaudepublica.repositories.AdministracaoMedicamentoRepository;
import com.edufelizardo.maissaudepublica.repositories.AtendimentoRepository;
import com.edufelizardo.maissaudepublica.repositories.ConsultaRepository;
import com.edufelizardo.maissaudepublica.repositories.LoteRepository;
import com.edufelizardo.maissaudepublica.repositories.MovimentacaoFarmaciaRepository;
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
 * Testes da administração de medicamento (ADR-0064): checagem administrado/não administrado, baixa do
 * lote pelo livro da Farmácia, validações de lote e prescrição, retificação com estorno, prontuário e
 * resumo do atendimento. SEM {@code @Transactional} na classe; limpeza manual no {@code @AfterEach}.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AdministracaoMedicamentoControllerTest {

    private static final String ADMINISTRACAO_URL = "/api/v1/administracao-medicamento/";
    private static final String ATENDIMENTO_URL = "/api/v1/atendimento/";
    private static final String CONSULTA_URL = "/api/v1/consulta/";
    private static final String LOTE_URL = "/api/v1/lote/";
    private static final String MEDICAMENTO_URL = "/api/v1/medicamento/";
    private static final String UNIDADE_SAUDE_URL = "/api/v1/unidade-saude/";
    private static final String FEDERAL_URL = "/api/v1/federal/";
    private static final String ESTADUAL_URL = "/api/v1/estadual/";
    private static final String MUNICIPAL_URL = "/api/v1/municipal/";
    private static final String PROFISSIONAL_URL = "/api/v1/profissional/";
    private static final String PACIENTE_URL = "/api/v1/paciente/";
    private static final String PREFIXO_NOME_TESTE = "Administracao Teste - ";
    private static final String PREFIXO_CPF_TESTE = "77755544";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AdministracaoMedicamentoRepository administracaoRepository;

    @Autowired
    private AtendimentoRepository atendimentoRepository;

    @Autowired
    private ConsultaRepository consultaRepository;

    @Autowired
    private LoteRepository loteRepository;

    @Autowired
    private MovimentacaoFarmaciaRepository movimentacaoFarmaciaRepository;

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
        // Ordem das FKs: livro → administrações (versões que corrigem primeiro) → consultas → atendimentos → lotes.
        movimentacaoFarmaciaRepository.deleteAll();
        administracaoRepository.deleteAll(administracaoRepository.findAll().stream().filter(x -> x.getRetificacaoDe() != null).toList());
        administracaoRepository.deleteAll();
        consultaRepository.deleteAll();
        atendimentoRepository.deleteAll();
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

    private record Cenario(UUID atendimentoId, UUID consultaId, UUID medicamentoId, UUID unidadeId, UUID loteId,
                           UUID pacienteId, String matricula) {
    }

    private UUID criarLote(UUID medicamentoId, UUID unidadeId, String numeroLote, String validade, int quantidade)
            throws Exception {
        mockMvc.perform(post(LOTE_URL).contentType(MediaType.APPLICATION_JSON).content("""
                        { "medicamentoId": "%s", "unidadeId": "%s", "numeroLote": "%s", "validade": "%s", "quantidade": %d }
                        """.formatted(medicamentoId, unidadeId, numeroLote, validade, quantidade)))
                .andExpect(status().isCreated());
        return loteRepository.findByMedicamentoUuid(medicamentoId).stream()
                .filter(l -> l.getNumeroLote().equals(numeroLote) && l.getUnidade().getUuid().equals(unidadeId))
                .findFirst()
                .orElseThrow()
                .getUuid();
    }

    private UUID criarAtendimento(UUID pacienteId, String matricula, UUID unidadeId) throws Exception {
        long antes = atendimentoRepository.count();
        mockMvc.perform(post(ATENDIMENTO_URL).contentType(MediaType.APPLICATION_JSON).content("""
                        { "pacienteId": "%s", "profissionalMatricula": "%s", "unidadeId": "%s", "tipo": "URGENCIA",
                          "status": "EM_ANDAMENTO", "dataHora": "2026-03-01T08:00:00" }
                        """.formatted(pacienteId, matricula, unidadeId)))
                .andExpect(status().isCreated());
        assertThat(atendimentoRepository.count()).isEqualTo(antes + 1);
        return atendimentoRepository.findAll().stream()
                .filter(a -> a.getPaciente().getUuid().equals(pacienteId))
                .map(a -> a.getUuid())
                .filter(id -> consultaRepository.findByAtendimentoUuid(id).isEmpty())
                .findFirst()
                .orElseThrow();
    }

    private UUID criarConsulta(UUID atendimentoId, String matricula) throws Exception {
        mockMvc.perform(post(CONSULTA_URL).contentType(MediaType.APPLICATION_JSON).content("""
                        { "atendimentoId": "%s", "profissionalMatricula": "%s", "dataHora": "2026-03-01T08:20:00",
                          "tipoConsulta": "URGENCIA", "queixaPrincipal": "Dor", "receituario": "Dipirona 1 g IM agora" }
                        """.formatted(atendimentoId, matricula)))
                .andExpect(status().isCreated());
        return consultaRepository.findByAtendimentoUuid(atendimentoId).get(0).getUuid();
    }

    /** Paciente com atendimento e consulta (a prescrição) numa UBS que tem um lote com {@code saldo} unidades. */
    private Cenario criarCenario(String sufixo, int saldo) throws Exception {
        UUID unidadeId = criarUnidadeSaudeUbs(sufixo);
        UUID medicamentoId = criarMedicamentoEBuscarUuid(sufixo);
        String matricula = criarProfissionalEBuscarMatricula(PREFIXO_CPF_TESTE + sufixo, "Enfermeira " + sufixo);
        UUID pacienteId = criarPacienteEBuscarUuid(PREFIXO_CPF_TESTE + sufixo, "Paciente " + sufixo);
        UUID loteId = criarLote(medicamentoId, unidadeId, "ADM" + sufixo, "2030-01-01", saldo);
        UUID atendimentoId = criarAtendimento(pacienteId, matricula, unidadeId);
        UUID consultaId = criarConsulta(atendimentoId, matricula);
        return new Cenario(atendimentoId, consultaId, medicamentoId, unidadeId, loteId, pacienteId, matricula);
    }

    private String administrado(Cenario c, UUID loteId, Integer quantidade) {
        return """
                { "atendimentoId": "%s", "consultaId": "%s", "medicamentoId": "%s", "situacao": "ADMINISTRADO",
                  "loteId": %s, "dose": "1 g", "via": "INTRAMUSCULAR", "quantidade": %s,
                  "dataHora": "2026-03-01T08:40:00", "profissionalMatricula": "%s" }
                """.formatted(c.atendimentoId(), c.consultaId(), c.medicamentoId(),
                loteId == null ? "null" : "\"" + loteId + "\"", quantidade, c.matricula());
    }

    private String naoAdministrado(Cenario c, String motivo, String observacao) {
        return """
                { "atendimentoId": "%s", "consultaId": "%s", "medicamentoId": "%s", "situacao": "NAO_ADMINISTRADO",
                  "motivoNaoAdministracao": "%s", "observacao": %s, "dataHora": "2026-03-01T08:40:00",
                  "profissionalMatricula": "%s" }
                """.formatted(c.atendimentoId(), c.consultaId(), c.medicamentoId(), motivo,
                observacao == null ? "null" : "\"" + observacao + "\"", c.matricula());
    }

    private static String comMotivo(String corpo, String motivo) {
        return corpo.replaceFirst("\\{", "{ \"motivoRetificacao\": \"" + motivo + "\",");
    }

    private int saldo(UUID loteId) {
        return loteRepository.findById(loteId).orElseThrow().getQuantidade();
    }

    private List<TipoMovimentacaoFarmacia> tiposNoLivro(UUID loteId) {
        return movimentacaoFarmaciaRepository.findByLote_UuidOrderByRegistradoEmAsc(loteId).stream()
                .map(MovimentacaoFarmacia::getTipo)
                .toList();
    }

    private UUID unicaAdministracao(Cenario c) {
        return administracaoRepository.findByAtendimentoUuid(c.atendimentoId()).get(0).getUuid();
    }

    @Test
    void deveAdministrarBaixandoOLoteDaUnidadePeloLivro() throws Exception {
        Cenario c = criarCenario("01", 100);

        mockMvc.perform(post(ADMINISTRACAO_URL).contentType(MediaType.APPLICATION_JSON).content(administrado(c, c.loteId(), 2)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Administração registrada com sucesso!"));

        assertThat(saldo(c.loteId())).isEqualTo(98);
        assertThat(tiposNoLivro(c.loteId())).containsExactly(TipoMovimentacaoFarmacia.ENTRADA, TipoMovimentacaoFarmacia.ADMINISTRACAO);
        UUID id = unicaAdministracao(c);
        mockMvc.perform(get(ADMINISTRACAO_URL + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.situacao").value("ADMINISTRADO"))
                .andExpect(jsonPath("$.numeroLote").value("ADM01"))
                .andExpect(jsonPath("$.via").value("INTRAMUSCULAR"))
                .andExpect(jsonPath("$.quantidade").value(2))
                .andExpect(jsonPath("$.consultaUuid").value(c.consultaId().toString()))
                .andExpect(jsonPath("$.registradoEm").isNotEmpty());
        mockMvc.perform(get("/api/v1/movimentacao-farmacia/lote/" + c.loteId()))
                .andExpect(jsonPath("$[1].tipo").value("ADMINISTRACAO"))
                .andExpect(jsonPath("$[1].quantidade").value(-2))
                .andExpect(jsonPath("$[1].administracaoId").value(id.toString()));
    }

    @Test
    void deveRegistrarNaoAdministradoSemMexerNoEstoque() throws Exception {
        Cenario c = criarCenario("02", 100);

        mockMvc.perform(post(ADMINISTRACAO_URL).contentType(MediaType.APPLICATION_JSON)
                        .content(naoAdministrado(c, "RECUSA_DO_PACIENTE", null)))
                .andExpect(status().isCreated());

        assertThat(saldo(c.loteId())).isEqualTo(100);
        mockMvc.perform(get(ADMINISTRACAO_URL + unicaAdministracao(c)))
                .andExpect(jsonPath("$.situacao").value("NAO_ADMINISTRADO"))
                .andExpect(jsonPath("$.motivoNaoAdministracao").value("RECUSA_DO_PACIENTE"))
                .andExpect(jsonPath("$.loteUuid").doesNotExist());
    }

    @Test
    void deveExigirObservacaoComMotivoOutroEDadosCompletosQuandoAdministrado() throws Exception {
        Cenario c = criarCenario("03", 100);

        mockMvc.perform(post(ADMINISTRACAO_URL).contentType(MediaType.APPLICATION_JSON)
                        .content(naoAdministrado(c, "OUTRO", null)))
                .andExpect(status().isBadRequest());
        mockMvc.perform(post(ADMINISTRACAO_URL).contentType(MediaType.APPLICATION_JSON).content(administrado(c, null, 2)))
                .andExpect(status().isBadRequest());

        assertThat(administracaoRepository.count()).isZero();
        assertThat(saldo(c.loteId())).isEqualTo(100);
    }

    @Test
    void deveRecusarLoteDeOutraUnidadeLoteVencidoESaldoInsuficiente() throws Exception {
        Cenario c = criarCenario("04", 1);
        UUID outraUnidade = criarUnidadeSaudeUbs("04B");
        UUID loteDeFora = criarLote(c.medicamentoId(), outraUnidade, "FORA04", "2030-01-01", 50);
        UUID loteVencido = criarLote(c.medicamentoId(), c.unidadeId(), "VENC04", "2020-01-01", 50);

        mockMvc.perform(post(ADMINISTRACAO_URL).contentType(MediaType.APPLICATION_JSON).content(administrado(c, loteDeFora, 1)))
                .andExpect(status().isUnprocessableEntity());
        mockMvc.perform(post(ADMINISTRACAO_URL).contentType(MediaType.APPLICATION_JSON).content(administrado(c, loteVencido, 1)))
                .andExpect(status().isUnprocessableEntity());
        mockMvc.perform(post(ADMINISTRACAO_URL).contentType(MediaType.APPLICATION_JSON).content(administrado(c, c.loteId(), 5)))
                .andExpect(status().isUnprocessableEntity());

        assertThat(administracaoRepository.count()).isZero();
        assertThat(saldo(c.loteId())).isEqualTo(1);
        assertThat(saldo(loteDeFora)).isEqualTo(50);
    }

    @Test
    void deveRecusarPrescricaoDeOutroAtendimento() throws Exception {
        Cenario c = criarCenario("05", 100);
        UUID outroAtendimento = criarAtendimento(c.pacienteId(), c.matricula(), c.unidadeId());
        String corpo = administrado(c, c.loteId(), 1).replace(c.atendimentoId().toString(), outroAtendimento.toString());

        mockMvc.perform(post(ADMINISTRACAO_URL).contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isBadRequest());
        assertThat(saldo(c.loteId())).isEqualTo(100);
    }

    @Test
    void deveRetificarEstornandoABaixaAnteriorAntesDaNova() throws Exception {
        Cenario c = criarCenario("06", 100);
        mockMvc.perform(post(ADMINISTRACAO_URL).contentType(MediaType.APPLICATION_JSON).content(administrado(c, c.loteId(), 2)))
                .andExpect(status().isCreated());
        UUID original = unicaAdministracao(c);

        mockMvc.perform(post(ADMINISTRACAO_URL + original + "/retificacao").contentType(MediaType.APPLICATION_JSON)
                        .content(comMotivo(administrado(c, c.loteId(), 3), "Foram três ampolas")))
                .andExpect(status().isCreated());

        assertThat(saldo(c.loteId())).isEqualTo(97);
        assertThat(tiposNoLivro(c.loteId())).containsExactly(TipoMovimentacaoFarmacia.ENTRADA,
                TipoMovimentacaoFarmacia.ADMINISTRACAO, TipoMovimentacaoFarmacia.ADMINISTRACAO_ESTORNO,
                TipoMovimentacaoFarmacia.ADMINISTRACAO);
        mockMvc.perform(get(ADMINISTRACAO_URL + original))
                .andExpect(jsonPath("$.retificado").value(true));

        // A versão já retificada não é retificada de novo.
        mockMvc.perform(post(ADMINISTRACAO_URL + original + "/retificacao").contentType(MediaType.APPLICATION_JSON)
                        .content(comMotivo(administrado(c, c.loteId(), 1), "Outra correção")))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void deveRetificarParaNaoAdministradoDevolvendoOEstoque() throws Exception {
        Cenario c = criarCenario("07", 100);
        mockMvc.perform(post(ADMINISTRACAO_URL).contentType(MediaType.APPLICATION_JSON).content(administrado(c, c.loteId(), 2)))
                .andExpect(status().isCreated());
        UUID original = unicaAdministracao(c);

        mockMvc.perform(post(ADMINISTRACAO_URL + original + "/retificacao").contentType(MediaType.APPLICATION_JSON)
                        .content(comMotivo(naoAdministrado(c, "RECUSA_DO_PACIENTE", null), "Paciente recusou; registrado errado")))
                .andExpect(status().isCreated());

        assertThat(saldo(c.loteId())).isEqualTo(100);
    }

    @Test
    void deveAparecerNoProntuarioENoResumoDoAtendimento() throws Exception {
        Cenario c = criarCenario("08", 100);
        mockMvc.perform(get(ADMINISTRACAO_URL + "atendimento/" + c.atendimentoId()))
                .andExpect(status().isNotFound());

        mockMvc.perform(post(ADMINISTRACAO_URL).contentType(MediaType.APPLICATION_JSON).content(administrado(c, c.loteId(), 1)))
                .andExpect(status().isCreated());

        mockMvc.perform(get(ADMINISTRACAO_URL + "atendimento/" + c.atendimentoId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
        mockMvc.perform(get("/api/v1/prontuario/" + c.pacienteId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.atendimentos[0].administracoes.length()").value(1))
                .andExpect(jsonPath("$.atendimentos[0].administracoes[0].dose").value("1 g"));
        mockMvc.perform(get(ATENDIMENTO_URL + c.atendimentoId()))
                .andExpect(jsonPath("$.totalAdministracoes").value(1));
    }
}
