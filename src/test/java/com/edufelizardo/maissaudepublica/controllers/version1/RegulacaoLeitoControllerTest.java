package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.models.Leito;
import com.edufelizardo.maissaudepublica.models.Paciente;
import com.edufelizardo.maissaudepublica.models.Profissional;
import com.edufelizardo.maissaudepublica.models.Setor;
import com.edufelizardo.maissaudepublica.models.UnidadeDeSaude;
import com.edufelizardo.maissaudepublica.models.enuns.FaixaEtariaLeito;
import com.edufelizardo.maissaudepublica.models.enuns.Sexo;
import com.edufelizardo.maissaudepublica.models.enuns.SexoLeito;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoLeito;
import com.edufelizardo.maissaudepublica.models.enuns.TipoLeito;
import com.edufelizardo.maissaudepublica.models.enuns.TipoSetor;
import com.edufelizardo.maissaudepublica.models.enuns.TipoUnidadeDeSaude;
import com.edufelizardo.maissaudepublica.repositories.InternacaoRepository;
import com.edufelizardo.maissaudepublica.repositories.LeitoRepository;
import com.edufelizardo.maissaudepublica.repositories.PacienteRepository;
import com.edufelizardo.maissaudepublica.repositories.ProfissionalRepository;
import com.edufelizardo.maissaudepublica.repositories.SetorRepository;
import com.edufelizardo.maissaudepublica.repositories.UnidadeDeSaudeRepository;
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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Central de regulação de leitos entre unidades (ADR-0184): pedido, fila, vagas compatíveis, reserva com prazo, vaga zero,
 * recusa, expiração, admissão com transferência da origem e a faixa etária do leito, com login e autorização desligados. O
 * escopo fica em {@link RegulacaoLeitoAutorizacaoControllerTest}.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class RegulacaoLeitoControllerTest {

    private static final String URL = "/api/v1/regulacao-leito/";
    private static final String PREFIXO = "RegLeito Teste ";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private UnidadeDeSaudeRepository unidadeDeSaudeRepository;

    @Autowired
    private SetorRepository setorRepository;

    @Autowired
    private LeitoRepository leitoRepository;

    @Autowired
    private PacienteRepository pacienteRepository;

    @Autowired
    private ProfissionalRepository profissionalRepository;

    @Autowired
    private InternacaoRepository internacaoRepository;

    private UnidadeDeSaude upa;
    private UnidadeDeSaude hospital;
    private Setor enfermaria;
    private Setor observacao;
    private Paciente maria;

    @BeforeEach
    void seed() {
        limpar();
        upa = unidade("UPA", TipoUnidadeDeSaude.UPA);
        hospital = unidade("Hospital", TipoUnidadeDeSaude.HOSPITAL);
        enfermaria = setorRepository.save(new Setor(hospital, PREFIXO + "Clínica médica", "RL-CM", TipoSetor.ASSISTENCIAL, true, null));
        observacao = setorRepository.save(new Setor(upa, PREFIXO + "Observação", "RL-OBS", TipoSetor.ASSISTENCIAL, true, null));
        Profissional medico = new Profissional();
        medico.setMatricula("RL-MED");
        medico.setCpf("80246802464");
        medico.setNome(PREFIXO + "Médico");
        medico.setAtivo(true);
        profissionalRepository.save(medico);
        maria = paciente("Maria", Sexo.FEMININO, LocalDate.of(1980, 3, 10));
    }

    @AfterEach
    void limpar() {
        List<UUID> unidades = unidadeDeSaudeRepository.findAll().stream()
                .filter(u -> u.getNome() != null && u.getNome().startsWith(PREFIXO)).map(UnidadeDeSaude::getUuid).toList();
        for (UUID u : unidades) {
            jdbc.update("delete from tb_evento_solicitacao_leito where solicitacao_id in (select uuid from tb_solicitacao_leito where unidade_origem_id = ?)", u);
            jdbc.update("delete from tb_solicitacao_leito where unidade_origem_id = ?", u);
        }
        for (UUID u : unidades) {
            jdbc.update("delete from tb_evento_leito where leito_id in (select uuid from tb_leito where unidade_id = ?)", u);
            jdbc.update("delete from tb_internacao where unidade_id = ?", u);
            jdbc.update("delete from tb_leito where unidade_id = ?", u);
            jdbc.update("delete from tb_setor where unidade_id = ?", u);
        }
        unidadeDeSaudeRepository.deleteAllById(unidades);
        pacienteRepository.deleteAll(pacienteRepository.findAll().stream()
                .filter(p -> p.getNome() != null && p.getNome().startsWith(PREFIXO)).toList());
        profissionalRepository.findByMatricula("RL-MED").ifPresent(profissionalRepository::delete);
    }

    // ── Ciclo ──────────────────────────────────────────────────────────────────────────────────

    @Test
    void pedidoReservaEAdmissaoComVagasCompativeis() throws Exception {
        UUID misto = leito(hospital, enfermaria, "CM-01", TipoLeito.CLINICO, SexoLeito.MISTO, FaixaEtariaLeito.ADULTO);
        UUID masculino = leito(hospital, enfermaria, "CM-02", TipoLeito.CLINICO, SexoLeito.MASCULINO, FaixaEtariaLeito.ADULTO);
        UUID pediatrico = leito(hospital, enfermaria, "CM-03", TipoLeito.CLINICO, SexoLeito.MISTO, FaixaEtariaLeito.PEDIATRICO);
        UUID daOrigem = leito(upa, observacao, "OBS-01", TipoLeito.CLINICO, SexoLeito.MISTO, FaixaEtariaLeito.ADULTO);
        UUID uti = leito(hospital, enfermaria, "UTI-01", TipoLeito.UTI_ADULTO, SexoLeito.MISTO, FaixaEtariaLeito.ADULTO);

        UUID pedido = pedir(maria, "CLINICO", "VERMELHO", null);
        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(corpoPedido(maria, "CLINICO", "AMARELO", null)))
                .andExpect(status().isConflict());
        mockMvc.perform(get(URL + "fila"))
                .andExpect(jsonPath("$[?(@.uuid == '%s')].posicaoNaFila".formatted(pedido)).value(org.hamcrest.Matchers.contains(1)));
        mockMvc.perform(get(URL + pedido + "/vagas"))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].leitoId").value(misto.toString()));

        reservar(pedido, "{\"profissionalMatricula\": \"RL-MED\", \"leitoId\": \"%s\"}".formatted(masculino)).andExpect(status().isUnprocessableEntity());
        reservar(pedido, "{\"profissionalMatricula\": \"RL-MED\", \"leitoId\": \"%s\"}".formatted(daOrigem)).andExpect(status().isUnprocessableEntity());
        reservar(pedido, "{\"profissionalMatricula\": \"RL-MED\", \"leitoId\": \"%s\", \"horas\": 4, \"transporte\": \"USA 01\"}".formatted(misto))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("RESERVADA"))
                .andExpect(jsonPath("$.leitoReservadoIdentificacao").value("CM-01"))
                .andExpect(jsonPath("$.reservadoAte", notNullValue()));
        assertThat(leitoRepository.findById(misto).orElseThrow().getSituacao()).isEqualTo(SituacaoLeito.RESERVADO);
        mockMvc.perform(get("/api/v1/leito/?unidadeId=" + hospital.getUuid()))
                .andExpect(jsonPath("$[?(@.uuid == '%s')].situacao".formatted(misto)).value(org.hamcrest.Matchers.contains("RESERVADO")))
                .andExpect(jsonPath("$[?(@.uuid == '%s')].reservaSolicitacaoId".formatted(misto)).value(org.hamcrest.Matchers.contains(pedido.toString())));
        mockMvc.perform(get(URL + "a-receber")).andExpect(jsonPath("$[?(@.uuid == '%s')]".formatted(pedido), hasSize(1)));
        // Ninguém interna direto no leito reservado.
        mockMvc.perform(post("/api/v1/internacao/").contentType(MediaType.APPLICATION_JSON).content(corpoInternacao(maria, misto, null)))
                .andExpect(status().isUnprocessableEntity());

        mockMvc.perform(post(URL + pedido + "/admissao").contentType(MediaType.APPLICATION_JSON).content("{\"medicoMatricula\": \"RL-MED\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ADMITIDA"))
                .andExpect(jsonPath("$.internacaoDestinoId", notNullValue()))
                .andExpect(jsonPath("$.eventos[*].tipo").value(org.hamcrest.Matchers.contains("SOLICITACAO", "RESERVA", "ADMISSAO")));
        Leito ocupado = leitoRepository.findById(misto).orElseThrow();
        assertThat(ocupado.getSituacao()).isEqualTo(SituacaoLeito.OCUPADO);
        assertThat(ocupado.getReservaSolicitacaoId()).isNull();
        assertThat(pediatrico).isNotNull();
        assertThat(uti).isNotNull();
    }

    @Test
    void recusaVoltaAFilaEReservaVencidaLiberaOLeito() throws Exception {
        UUID leito = leito(hospital, enfermaria, "CM-10", TipoLeito.CLINICO, SexoLeito.FEMININO, FaixaEtariaLeito.ADULTO);
        UUID pedido = pedir(maria, "CLINICO", "AMARELO", null);
        reservar(pedido, "{\"profissionalMatricula\": \"RL-MED\", \"leitoId\": \"%s\"}".formatted(leito)).andExpect(status().isOk());
        mockMvc.perform(post(URL + pedido + "/recusa").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"profissionalMatricula\": \"RL-MED\", \"motivo\": \"Sem equipe de nefrologia hoje.\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SOLICITADA"))
                .andExpect(jsonPath("$.posicaoNaFila").value(1));
        assertThat(leitoRepository.findById(leito).orElseThrow().getSituacao()).isEqualTo(SituacaoLeito.LIVRE);

        reservar(pedido, "{\"profissionalMatricula\": \"RL-MED\", \"leitoId\": \"%s\"}".formatted(leito)).andExpect(status().isOk());
        jdbc.update("update tb_leito set reservado_ate = now() - interval '1 hour' where uuid = ?", leito);
        jdbc.update("update tb_solicitacao_leito set reservado_ate = now() - interval '1 hour' where uuid = ?", pedido);
        mockMvc.perform(get(URL + "fila"))
                .andExpect(jsonPath("$[?(@.uuid == '%s')].status".formatted(pedido)).value(org.hamcrest.Matchers.contains("SOLICITADA")));
        mockMvc.perform(get(URL + pedido + "/vagas")).andExpect(jsonPath("$[0].leitoId").value(leito.toString()));
        mockMvc.perform(get(URL + "a-receber")).andExpect(jsonPath("$[?(@.uuid == '%s')]".formatted(pedido), hasSize(0)));
        mockMvc.perform(post(URL + pedido + "/admissao").contentType(MediaType.APPLICATION_JSON).content("{\"medicoMatricula\": \"RL-MED\"}"))
                .andExpect(status().isUnprocessableEntity());
        reservar(pedido, "{\"profissionalMatricula\": \"RL-MED\", \"leitoId\": \"%s\"}".formatted(leito))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.eventos[*].tipo").value(org.hamcrest.Matchers.contains("SOLICITACAO", "RESERVA", "RECUSA_DESTINO", "RESERVA",
                        "EXPIRACAO", "RESERVA")));
    }

    @Test
    void vagaZeroPedeJustificativaEOLeitoNaAdmissao() throws Exception {
        UUID leito = leito(hospital, enfermaria, "CM-20", TipoLeito.CLINICO, SexoLeito.MISTO, FaixaEtariaLeito.ADULTO);
        UUID pedido = pedir(maria, "UTI_ADULTO", "VERMELHO", null);
        reservar(pedido, "{\"profissionalMatricula\": \"RL-MED\", \"unidadeDestinoId\": \"%s\"}".formatted(hospital.getUuid()))
                .andExpect(status().isBadRequest());
        reservar(pedido, "{\"profissionalMatricula\": \"RL-MED\", \"unidadeDestinoId\": \"%s\", \"justificativaVagaZero\": \"Choque séptico, sem UTI livre na rede.\"}"
                .formatted(hospital.getUuid()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.vagaZero").value(true))
                .andExpect(jsonPath("$.unidadeDestinoNome").value(PREFIXO + "Hospital"));
        mockMvc.perform(post(URL + pedido + "/admissao").contentType(MediaType.APPLICATION_JSON).content("{\"medicoMatricula\": \"RL-MED\"}"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(post(URL + pedido + "/admissao").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"medicoMatricula\": \"RL-MED\", \"leitoId\": \"%s\"}".formatted(leito)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ADMITIDA"));
    }

    @Test
    void admissaoNoDestinoFechaAInternacaoDaOrigemPorTransferencia() throws Exception {
        UUID obs = leito(upa, observacao, "OBS-30", TipoLeito.OBSERVACAO, SexoLeito.MISTO, FaixaEtariaLeito.ADULTO);
        UUID uti = leito(hospital, enfermaria, "UTI-30", TipoLeito.UTI_ADULTO, SexoLeito.MISTO, FaixaEtariaLeito.ADULTO);
        String internacao = JsonPath.read(mockMvc.perform(post("/api/v1/internacao/").contentType(MediaType.APPLICATION_JSON)
                .content(corpoInternacao(maria, obs, null))).andExpect(status().isCreated()).andReturn().getResponse().getContentAsString(), "$.uuid");
        UUID pedido = pedir(maria, "UTI_ADULTO", "VERMELHO", UUID.fromString(internacao));
        reservar(pedido, "{\"profissionalMatricula\": \"RL-MED\", \"leitoId\": \"%s\"}".formatted(uti)).andExpect(status().isOk());
        mockMvc.perform(post(URL + pedido + "/admissao").contentType(MediaType.APPLICATION_JSON).content("{\"medicoMatricula\": \"RL-MED\"}"))
                .andExpect(status().isOk());
        var origem = internacaoRepository.findById(UUID.fromString(internacao)).orElseThrow();
        assertThat(origem.getTipoAlta().name()).isEqualTo("TRANSFERENCIA");
        assertThat(leitoRepository.findById(obs).orElseThrow().getSituacao()).isEqualTo(SituacaoLeito.HIGIENIZACAO);
        assertThat(leitoRepository.findById(uti).orElseThrow().getSituacao()).isEqualTo(SituacaoLeito.OCUPADO);
    }

    @Test
    void faixaEtariaDoLeitoNaVagaENaInternacao() throws Exception {
        Paciente crianca = paciente("Pedro", Sexo.MASCULINO, LocalDate.now().minusYears(5));
        UUID adulto = leito(hospital, enfermaria, "CM-40", TipoLeito.CLINICO, SexoLeito.MISTO, FaixaEtariaLeito.ADULTO);
        UUID pediatrico = leito(hospital, enfermaria, "CM-41", TipoLeito.CLINICO, SexoLeito.MISTO, FaixaEtariaLeito.PEDIATRICO);
        UUID pedido = pedir(crianca, "CLINICO", "VERDE", null);
        mockMvc.perform(get(URL + pedido + "/vagas"))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].leitoId").value(pediatrico.toString()));
        mockMvc.perform(post("/api/v1/internacao/").contentType(MediaType.APPLICATION_JSON).content(corpoInternacao(crianca, adulto, null)))
                .andExpect(status().isUnprocessableEntity());
        mockMvc.perform(post("/api/v1/internacao/").contentType(MediaType.APPLICATION_JSON)
                        .content(corpoInternacao(crianca, adulto, "Ala pediátrica em reforma; acompanhante presente.")))
                .andExpect(status().isCreated());
    }

    @Test
    void negarECancelar() throws Exception {
        UUID pedido = pedir(maria, "CLINICO", "AZUL", null);
        String motivo = "{\"profissionalMatricula\": \"RL-MED\", \"motivo\": \"Quadro resolvido na própria unidade.\"}";
        mockMvc.perform(post(URL + pedido + "/negacao").contentType(MediaType.APPLICATION_JSON).content(motivo))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("NEGADA"));
        mockMvc.perform(post(URL + pedido + "/cancelamento").contentType(MediaType.APPLICATION_JSON).content(motivo))
                .andExpect(status().isUnprocessableEntity());
        UUID outro = pedir(maria, "CLINICO", "AZUL", null);
        mockMvc.perform(post(URL + outro + "/cancelamento").contentType(MediaType.APPLICATION_JSON).content(motivo))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("CANCELADA"));
        mockMvc.perform(get(URL + "?pacienteId=" + maria.getUuid())).andExpect(jsonPath("$", hasSize(2)));
    }

    // ── Apoio ──────────────────────────────────────────────────────────────────────────────────

    private UnidadeDeSaude unidade(String nome, TipoUnidadeDeSaude tipo) {
        UnidadeDeSaude u = new UnidadeDeSaude();
        u.setNome(PREFIXO + nome);
        u.setTipo(tipo);
        u.setAtivo(true);
        return unidadeDeSaudeRepository.save(u);
    }

    private Paciente paciente(String nome, Sexo sexo, LocalDate nascimento) {
        Paciente p = new Paciente();
        p.setNome(PREFIXO + nome);
        p.setSexo(sexo);
        p.setDataNascimento(nascimento);
        p.setAtivo(true);
        return pacienteRepository.save(p);
    }

    private UUID leito(UnidadeDeSaude unidade, Setor setor, String identificacao, TipoLeito tipo, SexoLeito sexo, FaixaEtariaLeito faixa) {
        Leito l = new Leito();
        l.setUnidade(unidade);
        l.setSetor(setor);
        l.setIdentificacao(identificacao);
        l.setTipo(tipo);
        l.setSexo(sexo);
        l.setFaixaEtaria(faixa);
        l.setSituacao(SituacaoLeito.LIVRE);
        l.setAtivo(true);
        return leitoRepository.save(l).getUuid();
    }

    private String corpoPedido(Paciente p, String tipo, String prioridade, UUID internacaoOrigem) {
        return """
                {"pacienteId": "%s", "unidadeOrigemId": "%s", "profissionalMatricula": "RL-MED", "cid": "A41.9",
                 "quadroClinico": "Hipotensa, taquicárdica, lactato alto.", "tipoLeito": "%s", "prioridade": "%s"%s}
                """.formatted(p.getUuid(), upa.getUuid(), tipo, prioridade,
                internacaoOrigem != null ? ", \"internacaoOrigemId\": \"" + internacaoOrigem + "\"" : "");
    }

    private UUID pedir(Paciente p, String tipo, String prioridade, UUID internacaoOrigem) throws Exception {
        String corpo = mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(corpoPedido(p, tipo, prioridade, internacaoOrigem)))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return UUID.fromString(JsonPath.read(corpo, "$.uuid"));
    }

    private ResultActions reservar(UUID pedido, String corpo) throws Exception {
        return mockMvc.perform(post(URL + pedido + "/reserva").contentType(MediaType.APPLICATION_JSON).content(corpo));
    }

    private String corpoInternacao(Paciente p, UUID leito, String justificativa) {
        return """
                {"pacienteId": "%s", "leitoId": "%s", "medicoMatricula": "RL-MED", "cid": "a41.9", "motivo": "Sepse.", "carater": "URGENCIA"%s}
                """.formatted(p.getUuid(), leito, justificativa != null ? ", \"justificativaFaixaEtaria\": \"" + justificativa + "\"" : "");
    }
}
