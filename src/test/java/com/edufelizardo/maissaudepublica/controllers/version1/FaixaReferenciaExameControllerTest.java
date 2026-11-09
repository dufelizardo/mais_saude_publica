package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.models.ExameLaboratorial;
import com.edufelizardo.maissaudepublica.models.ItemPedidoExame;
import com.edufelizardo.maissaudepublica.models.Paciente;
import com.edufelizardo.maissaudepublica.models.PedidoExame;
import com.edufelizardo.maissaudepublica.models.Profissional;
import com.edufelizardo.maissaudepublica.models.UnidadeDeSaude;
import com.edufelizardo.maissaudepublica.models.enuns.MaterialExame;
import com.edufelizardo.maissaudepublica.models.enuns.Sexo;
import com.edufelizardo.maissaudepublica.models.enuns.TipoResultadoExame;
import com.edufelizardo.maissaudepublica.models.enuns.TipoUnidadeDeSaude;
import com.edufelizardo.maissaudepublica.repositories.ExameLaboratorialRepository;
import com.edufelizardo.maissaudepublica.repositories.ItemPedidoExameRepository;
import com.edufelizardo.maissaudepublica.repositories.PacienteRepository;
import com.edufelizardo.maissaudepublica.repositories.PedidoExameRepository;
import com.edufelizardo.maissaudepublica.repositories.ProfissionalRepository;
import com.edufelizardo.maissaudepublica.repositories.UnidadeDeSaudeRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.contains;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Faixas de referência por sexo e idade, valores críticos e a evolução do exame (ADR-0180), com login e autorização
 * desligados. O escopo fica em {@link LaboratorioAutorizacaoControllerTest}.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class FaixaReferenciaExameControllerTest {

    private static final String PEDIDO = "/api/v1/pedido-exame/";
    private static final String CATALOGO = "/api/v1/exame-laboratorial/";
    private static final String PREFIXO = "Faixa Teste ";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private ExameLaboratorialRepository exameRepository;

    @Autowired
    private PedidoExameRepository pedidoRepository;

    @Autowired
    private ItemPedidoExameRepository itemRepository;

    @Autowired
    private PacienteRepository pacienteRepository;

    @Autowired
    private ProfissionalRepository profissionalRepository;

    @Autowired
    private UnidadeDeSaudeRepository unidadeDeSaudeRepository;

    private UnidadeDeSaude ubs;
    private UnidadeDeSaude laboratorio;
    private ExameLaboratorial hemoglobina;

    @BeforeEach
    void seed() {
        limpar();
        ubs = unidade("UBS", TipoUnidadeDeSaude.UBS);
        laboratorio = unidade("Laboratório", TipoUnidadeDeSaude.LABORATORIO);
        profissional("FX-MED", "15938264070");
        profissional("FX-TEC", "26049375181");
        profissional("FX-RT", "37150486292");
        hemoglobina = new ExameLaboratorial();
        hemoglobina.setNome(PREFIXO + "Hemoglobina");
        hemoglobina.setMaterial(MaterialExame.SANGUE);
        hemoglobina.setTipoResultado(TipoResultadoExame.NUMERICO);
        hemoglobina.setUnidadeMedida("g/dL");
        hemoglobina.setReferenciaMinima(new BigDecimal("12"));
        hemoglobina.setReferenciaMaxima(new BigDecimal("17"));
        hemoglobina.setAtivo(true);
        hemoglobina = exameRepository.save(hemoglobina);
    }

    @AfterEach
    void limpar() {
        List<UUID> pacientes = pacienteRepository.findAll().stream()
                .filter(p -> p.getNome() != null && p.getNome().startsWith(PREFIXO)).map(Paciente::getUuid).toList();
        for (UUID p : pacientes) {
            for (PedidoExame pedido : pedidoRepository.findByPaciente_UuidOrderBySolicitadoEmDesc(p)) {
                UUID id = pedido.getUuid();
                jdbc.update("update tb_item_pedido_exame set resultado_atual_id = null, amostra_id = null where pedido_id = ?", id);
                jdbc.update("delete from tb_resultado_exame where item_id in (select uuid from tb_item_pedido_exame where pedido_id = ?)", id);
                jdbc.update("delete from tb_evento_exame where pedido_id = ?", id);
                jdbc.update("delete from tb_item_pedido_exame where pedido_id = ?", id);
                jdbc.update("delete from tb_amostra_exame where pedido_id = ?", id);
                jdbc.update("delete from tb_pedido_exame where uuid = ?", id);
            }
        }
        pacienteRepository.deleteAllById(pacientes);
        jdbc.update("delete from tb_faixa_referencia_exame where exame_id in (select uuid from tb_exame_laboratorial where nome like ?)", PREFIXO + "%");
        exameRepository.deleteAll(exameRepository.findAll().stream().filter(e -> e.getNome().startsWith(PREFIXO)).toList());
        profissionalRepository.deleteAll(profissionalRepository.findAll().stream()
                .filter(p -> p.getMatricula() != null && p.getMatricula().startsWith("FX-")).toList());
        unidadeDeSaudeRepository.deleteAll(unidadeDeSaudeRepository.findAll().stream()
                .filter(u -> u.getNome() != null && u.getNome().startsWith(PREFIXO)).toList());
    }

    // ── Apoio ──────────────────────────────────────────────────────────────────────────────────

    private UnidadeDeSaude unidade(String nome, TipoUnidadeDeSaude tipo) {
        UnidadeDeSaude u = new UnidadeDeSaude();
        u.setNome(PREFIXO + nome);
        u.setTipo(tipo);
        u.setAtivo(true);
        return unidadeDeSaudeRepository.save(u);
    }

    private void profissional(String matricula, String cpf) {
        Profissional p = new Profissional();
        p.setMatricula(matricula);
        p.setCpf(cpf);
        p.setNome(PREFIXO + matricula);
        p.setAtivo(true);
        profissionalRepository.save(p);
    }

    private Paciente paciente(String nome, Sexo sexo, LocalDate nascimento) {
        Paciente p = new Paciente();
        p.setNome(PREFIXO + nome);
        p.setSexo(sexo);
        p.setDataNascimento(nascimento);
        p.setAtivo(true);
        return pacienteRepository.save(p);
    }

    private ResultActions faixa(String corpo) throws Exception {
        return mockMvc.perform(post(CATALOGO + hemoglobina.getUuid() + "/faixa").contentType(MediaType.APPLICATION_JSON).content(corpo));
    }

    /** Mulher adulta (12 a 59 anos), homem adulto (12 anos em diante) e criança de qualquer sexo (6 meses a 11 anos). */
    private void faixasPadrao() throws Exception {
        faixa("""
                {"sexo": "FEMININO", "idadeMinima": 12, "unidadeIdadeMinima": "ANOS", "idadeMaxima": 60, "unidadeIdadeMaxima": "ANOS",
                 "referenciaMinima": 12, "referenciaMaxima": 15.5, "criticoMinimo": 7, "criticoMaximo": 20}
                """).andExpect(status().isCreated());
        faixa("""
                {"sexo": "MASCULINO", "idadeMinima": 12, "unidadeIdadeMinima": "ANOS",
                 "referenciaMinima": 13.5, "referenciaMaxima": 17.5, "criticoMinimo": 7, "criticoMaximo": 20}
                """).andExpect(status().isCreated());
        faixa("""
                {"idadeMinima": 6, "unidadeIdadeMinima": "MESES", "idadeMaxima": 12, "unidadeIdadeMaxima": "ANOS",
                 "referenciaMinima": 11, "referenciaMaxima": 14, "descricao": "Infantil"}
                """).andExpect(status().isCreated());
    }

    /** Pede, coleta hoje e registra o valor; devolve o item. */
    private UUID resultado(Paciente paciente, String valor) throws Exception {
        mockMvc.perform(post(PEDIDO).contentType(MediaType.APPLICATION_JSON).content("""
                {"pacienteId": "%s", "unidadeSolicitanteId": "%s", "profissionalMatricula": "FX-MED", "exameIds": ["%s"],
                 "indicacaoClinica": "Astenia e palidez há 3 semanas.", "prioridade": "ROTINA"}
                """.formatted(paciente.getUuid(), ubs.getUuid(), hemoglobina.getUuid()))).andExpect(status().isCreated());
        // O pedido ainda sem coleta (o recém-criado), sem depender da ordem dos horários.
        PedidoExame pedido = pedidoRepository.findByPaciente_UuidOrderBySolicitadoEmDesc(paciente.getUuid()).stream()
                .filter(p -> itemRepository.findByPedido_Uuid(p.getUuid()).stream().allMatch(i -> i.getAmostra() == null))
                .findFirst().orElseThrow();
        mockMvc.perform(post(PEDIDO + pedido.getUuid() + "/coleta").contentType(MediaType.APPLICATION_JSON).content("""
                {"profissionalMatricula": "FX-TEC", "unidadeColetaId": "%s", "laboratorioId": "%s"}
                """.formatted(ubs.getUuid(), laboratorio.getUuid()))).andExpect(status().isOk());
        UUID item = itemRepository.findByPedido_Uuid(pedido.getUuid()).stream().map(ItemPedidoExame::getUuid).findFirst().orElseThrow();
        acao(item, "resultado", "{\"profissionalMatricula\": \"FX-TEC\", \"valorNumerico\": " + valor + "}").andExpect(status().isOk());
        return item;
    }

    private ResultActions acao(UUID item, String rota, String corpo) throws Exception {
        return mockMvc.perform(post(PEDIDO + "item/" + item + "/" + rota).contentType(MediaType.APPLICATION_JSON).content(corpo));
    }

    private ResultActions doItem(UUID item) throws Exception {
        UUID pedido = itemRepository.findById(item).orElseThrow().getPedido().getUuid();
        return mockMvc.perform(get(PEDIDO + pedido));
    }

    // ── Catálogo ───────────────────────────────────────────────────────────────────────────────

    @Test
    void catalogoIncluiFaixasComRotuloEOrdenaPelaIdade() throws Exception {
        faixasPadrao();
        mockMvc.perform(get(CATALOGO + hemoglobina.getUuid()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.faixas.length()").value(3))
                .andExpect(jsonPath("$.faixas[0].rotulo").value("Infantil (Ambos os sexos, 6 meses a menos de 12 anos)"))
                .andExpect(jsonPath("$.faixas[1].rotulo").value("Feminino, 12 a 59 anos"))
                .andExpect(jsonPath("$.faixas[2].rotulo").value("Masculino, a partir de 12 anos"));
        mockMvc.perform(get(CATALOGO))
                .andExpect(jsonPath("$[?(@.uuid == '%s')].faixas.length()".formatted(hemoglobina.getUuid())).value(contains(3)));
    }

    @Test
    void faixaRecusaSobreposicaoDoMesmoSexoSexoIgnoradoELimitesIncoerentes() throws Exception {
        faixasPadrao();
        // Mesmo sexo com idades em comum (59 anos cai nas duas): 409. Outro sexo nas mesmas idades: aceito.
        faixa("""
                {"sexo": "FEMININO", "idadeMinima": 50, "unidadeIdadeMinima": "ANOS", "referenciaMinima": 11.5}
                """).andExpect(status().isConflict());
        faixa("""
                {"sexo": "FEMININO", "idadeMinima": 60, "unidadeIdadeMinima": "ANOS", "referenciaMinima": 11.5}
                """).andExpect(status().isCreated());
        faixa("""
                {"sexo": "IGNORADO", "idadeMinima": 0, "unidadeIdadeMinima": "DIAS", "referenciaMinima": 10}
                """).andExpect(status().isBadRequest());
        faixa("""
                {"idadeMinima": 0, "unidadeIdadeMinima": "DIAS", "idadeMaxima": 29, "referenciaMinima": 14}
                """).andExpect(status().isBadRequest());
        faixa("""
                {"idadeMinima": 2, "unidadeIdadeMinima": "MESES", "idadeMaxima": 30, "unidadeIdadeMaxima": "DIAS", "referenciaMinima": 14}
                """).andExpect(status().isBadRequest());
        faixa("""
                {"idadeMinima": 0, "unidadeIdadeMinima": "DIAS", "idadeMaxima": 29, "unidadeIdadeMaxima": "DIAS"}
                """).andExpect(status().isBadRequest());
        faixa("""
                {"idadeMinima": 0, "unidadeIdadeMinima": "DIAS", "idadeMaxima": 29, "unidadeIdadeMaxima": "DIAS",
                 "referenciaMinima": 14, "referenciaMaxima": 22, "criticoMinimo": 15}
                """).andExpect(status().isBadRequest());
        faixa("""
                {"idadeMinima": 0, "unidadeIdadeMinima": "DIAS", "idadeMaxima": 29, "unidadeIdadeMaxima": "DIAS",
                 "referenciaMinima": 14, "referenciaMaxima": 22, "criticoMaximo": 21}
                """).andExpect(status().isBadRequest());
        // O catálogo também confere os críticos da faixa geral.
        mockMvc.perform(patch(CATALOGO + hemoglobina.getUuid()).contentType(MediaType.APPLICATION_JSON).content("""
                {"nome": "%s", "material": "SANGUE", "tipoResultado": "NUMERICO", "referenciaMinima": 12, "referenciaMaxima": 17,
                 "criticoMinimo": 13}
                """.formatted(hemoglobina.getNome()))).andExpect(status().isBadRequest());
    }

    @Test
    void faixaSoEmExameNumericoEExameComFaixaNaoViraTexto() throws Exception {
        ExameLaboratorial urina = new ExameLaboratorial();
        urina.setNome(PREFIXO + "Urina");
        urina.setMaterial(MaterialExame.URINA);
        urina.setTipoResultado(TipoResultadoExame.TEXTO);
        urina.setAtivo(true);
        urina = exameRepository.save(urina);
        mockMvc.perform(post(CATALOGO + urina.getUuid() + "/faixa").contentType(MediaType.APPLICATION_JSON).content("""
                {"idadeMinima": 0, "unidadeIdadeMinima": "DIAS", "referenciaMinima": 1}
                """)).andExpect(status().isUnprocessableEntity());
        faixasPadrao();
        mockMvc.perform(patch(CATALOGO + hemoglobina.getUuid()).contentType(MediaType.APPLICATION_JSON).content("""
                {"nome": "%s", "material": "SANGUE", "tipoResultado": "TEXTO"}
                """.formatted(hemoglobina.getNome()))).andExpect(status().isUnprocessableEntity());
    }

    @Test
    void faixaEditaERemoveSemMudarResultadoJaDado() throws Exception {
        faixasPadrao();
        Paciente maria = paciente("Maria", Sexo.FEMININO, LocalDate.now().minusYears(30));
        UUID item = resultado(maria, "11.8");
        doItem(item).andExpect(jsonPath("$.itens[0].resultado.interpretacao").value("ABAIXO"))
                .andExpect(jsonPath("$.itens[0].resultado.faixaDescricao").value("Feminino, 12 a 59 anos"));

        String feminina = mockMvc.perform(get(CATALOGO + hemoglobina.getUuid())).andReturn().getResponse().getContentAsString();
        String faixaId = com.jayway.jsonpath.JsonPath.<List<String>>read(feminina, "$.faixas[?(@.sexo == 'FEMININO')].uuid").get(0);
        mockMvc.perform(put(CATALOGO + "faixa/" + faixaId).contentType(MediaType.APPLICATION_JSON).content("""
                {"sexo": "FEMININO", "idadeMinima": 12, "unidadeIdadeMinima": "ANOS", "idadeMaxima": 60, "unidadeIdadeMaxima": "ANOS",
                 "referenciaMinima": 11.5, "referenciaMaxima": 15.5}
                """)).andExpect(status().isOk())
                .andExpect(jsonPath("$.faixas[?(@.uuid == '%s')].referenciaMinima".formatted(faixaId)).value(contains(11.5)));
        mockMvc.perform(delete(CATALOGO + "faixa/" + faixaId)).andExpect(status().isOk())
                .andExpect(jsonPath("$.faixas.length()").value(2));
        mockMvc.perform(delete(CATALOGO + "faixa/" + faixaId)).andExpect(status().isNotFound());

        // O resultado guardou a faixa da época.
        doItem(item).andExpect(jsonPath("$.itens[0].resultado.referenciaMinima").value(12.0))
                .andExpect(jsonPath("$.itens[0].resultado.faixaDescricao").value("Feminino, 12 a 59 anos"));
    }

    // ── Escolha da faixa no resultado ──────────────────────────────────────────────────────────

    @Test
    void resultadoUsaAFaixaDoSexoEDaIdadeNaDataDaColeta() throws Exception {
        faixasPadrao();
        // 13,0 é normal para mulher (12 a 15,5) e baixo para homem (13,5 a 17,5).
        UUID mulher = resultado(paciente("Ana", Sexo.FEMININO, LocalDate.now().minusYears(40)), "13.0");
        UUID homem = resultado(paciente("João", Sexo.MASCULINO, LocalDate.now().minusYears(40)), "13.0");
        doItem(mulher).andExpect(jsonPath("$.itens[0].resultado.interpretacao").value("NORMAL"))
                .andExpect(jsonPath("$.itens[0].resultado.referenciaMinima").value(12.0))
                .andExpect(jsonPath("$.itens[0].resultado.referenciaMaxima").value(15.5));
        doItem(homem).andExpect(jsonPath("$.itens[0].resultado.interpretacao").value("ABAIXO"))
                .andExpect(jsonPath("$.itens[0].resultado.faixaDescricao").value("Masculino, a partir de 12 anos"));
    }

    @Test
    void idadeNoLimiteDaFaixaContaPeloCalendario() throws Exception {
        faixasPadrao();
        // Faz 12 anos hoje: já é adulta. Faz 12 amanhã: ainda é infantil.
        UUID aniversario = resultado(paciente("Bia", Sexo.FEMININO, LocalDate.now().minusYears(12)), "11.5");
        UUID vespera = resultado(paciente("Clara", Sexo.FEMININO, LocalDate.now().minusYears(12).plusDays(1)), "11.5");
        doItem(aniversario).andExpect(jsonPath("$.itens[0].resultado.faixaDescricao").value("Feminino, 12 a 59 anos"))
                .andExpect(jsonPath("$.itens[0].resultado.interpretacao").value("ABAIXO"));
        doItem(vespera).andExpect(jsonPath("$.itens[0].resultado.faixaDescricao")
                        .value("Infantil (Ambos os sexos, 6 meses a menos de 12 anos)"))
                .andExpect(jsonPath("$.itens[0].resultado.interpretacao").value("NORMAL"));
    }

    @Test
    void sexoIgnoradoUsaSoFaixaSemSexoESemNascimentoUsaAFaixaGeral() throws Exception {
        faixasPadrao();
        // Adulto de sexo ignorado: nenhuma faixa sem sexo cobre 30 anos; vale a faixa geral (12 a 17).
        UUID ignorado = resultado(paciente("Sem sexo", Sexo.IGNORADO, LocalDate.now().minusYears(30)), "16.0");
        UUID semNascimento = resultado(paciente("Sem nascimento", Sexo.FEMININO, null), "16.0");
        UUID crianca = resultado(paciente("Criança", Sexo.IGNORADO, LocalDate.now().minusYears(5)), "16.0");
        doItem(ignorado).andExpect(jsonPath("$.itens[0].resultado.faixaDescricao").value("Faixa geral"))
                .andExpect(jsonPath("$.itens[0].resultado.interpretacao").value("NORMAL"));
        doItem(semNascimento).andExpect(jsonPath("$.itens[0].resultado.faixaDescricao").value("Faixa geral"));
        doItem(crianca).andExpect(jsonPath("$.itens[0].resultado.faixaDescricao")
                        .value("Infantil (Ambos os sexos, 6 meses a menos de 12 anos)"))
                .andExpect(jsonPath("$.itens[0].resultado.interpretacao").value("ACIMA"));
    }

    @Test
    void valorAlemDoCriticoEhCritico() throws Exception {
        faixasPadrao();
        Paciente ana = paciente("Ana", Sexo.FEMININO, LocalDate.now().minusYears(30));
        doItem(resultado(ana, "6.5")).andExpect(jsonPath("$.itens[0].resultado.interpretacao").value("CRITICO_BAIXO"))
                .andExpect(jsonPath("$.itens[0].resultado.criticoMinimo").value(7.0));
        doItem(resultado(ana, "21")).andExpect(jsonPath("$.itens[0].resultado.interpretacao").value("CRITICO_ALTO"));
        doItem(resultado(ana, "7")).andExpect(jsonPath("$.itens[0].resultado.interpretacao").value("ABAIXO"));
    }

    // ── Evolução ───────────────────────────────────────────────────────────────────────────────

    @Test
    void evolucaoTrazSoLiberadosEmOrdemDeColetaComARetificacaoMarcada() throws Exception {
        faixasPadrao();
        Paciente ana = paciente("Ana", Sexo.FEMININO, LocalDate.now().minusYears(30));
        UUID primeiro = resultado(ana, "10.2");
        UUID segundo = resultado(ana, "12.4");
        UUID naoLiberado = resultado(ana, "14.0");
        // A primeira coleta foi há dois meses.
        jdbc.update("update tb_amostra_exame set coletada_em = ? where uuid = (select amostra_id from tb_item_pedido_exame where uuid = ?)",
                LocalDateTime.now().minusMonths(2), primeiro);
        acao(primeiro, "liberacao", "{\"profissionalMatricula\": \"FX-RT\"}").andExpect(status().isOk());
        acao(segundo, "liberacao", "{\"profissionalMatricula\": \"FX-RT\"}").andExpect(status().isOk());
        acao(segundo, "retificacao", "{\"profissionalMatricula\": \"FX-RT\", \"valorNumerico\": 12.8, \"motivo\": \"Erro de digitação\"}")
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/prontuario/" + ana.getUuid() + "/exame/" + hemoglobina.getUuid() + "/evolucao"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.exameNome").value(PREFIXO + "Hemoglobina"))
                .andExpect(jsonPath("$.unidadeMedida").value("g/dL"))
                .andExpect(jsonPath("$.pontos.length()").value(2))
                .andExpect(jsonPath("$.pontos[0].itemId").value(primeiro.toString()))
                .andExpect(jsonPath("$.pontos[0].valor").value(10.2))
                .andExpect(jsonPath("$.pontos[0].interpretacao").value("ABAIXO"))
                .andExpect(jsonPath("$.pontos[0].retificado").value(false))
                .andExpect(jsonPath("$.pontos[1].valor").value(12.8))
                .andExpect(jsonPath("$.pontos[1].retificado").value(true))
                .andExpect(jsonPath("$.pontos[1].faixaDescricao").value("Feminino, 12 a 59 anos"))
                .andExpect(jsonPath("$.pontos[?(@.itemId == '%s')]".formatted(naoLiberado)).isEmpty());
    }

    @Test
    void evolucaoDePacienteOuExameInexistenteDa404() throws Exception {
        Paciente ana = paciente("Ana", Sexo.FEMININO, LocalDate.now().minusYears(30));
        mockMvc.perform(get("/api/v1/prontuario/" + UUID.randomUUID() + "/exame/" + hemoglobina.getUuid() + "/evolucao"))
                .andExpect(status().isNotFound());
        mockMvc.perform(get("/api/v1/prontuario/" + ana.getUuid() + "/exame/" + UUID.randomUUID() + "/evolucao"))
                .andExpect(status().isNotFound());
        mockMvc.perform(get("/api/v1/prontuario/" + ana.getUuid() + "/exame/" + hemoglobina.getUuid() + "/evolucao"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pontos.length()").value(0));
    }
}
