package com.edufelizardo.maissaudepublica.controllers.version1;

import com.edufelizardo.maissaudepublica.models.Agendamento;
import com.edufelizardo.maissaudepublica.models.Endereco;
import com.edufelizardo.maissaudepublica.models.Paciente;
import com.edufelizardo.maissaudepublica.models.Profissional;
import com.edufelizardo.maissaudepublica.models.UnidadeDeSaude;
import com.edufelizardo.maissaudepublica.models.enuns.StatusAgendamento;
import com.edufelizardo.maissaudepublica.models.enuns.TipoAgendamento;
import com.edufelizardo.maissaudepublica.models.enuns.TipoUnidadeDeSaude;
import com.edufelizardo.maissaudepublica.repositories.AgendamentoRepository;
import com.edufelizardo.maissaudepublica.repositories.PacienteRepository;
import com.edufelizardo.maissaudepublica.repositories.ProfissionalRepository;
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
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

/**
 * Transporte, T2 (ADR-0190): a solicitação (acompanhante, destino, data, duplicada, vínculo com o agendamento), o TFD com laudo e
 * comissão, a recorrência da hemodiálise, e a viagem do programar à chegada (veículo do SAMU, lugares, maca, janela, conflitos, troca de
 * embarques, saída, falta, chegada com a quilometragem) e a quilometragem por veículo.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class TransporteEletivoControllerTest {

    private static final String PREFIXO = "TransporteEletivo Teste ";
    private static final String URL = "/api/v1/transporte/";
    private static final ZoneId ZONA = ZoneId.systemDefault();

    @Autowired private MockMvc mockMvc;
    @Autowired private JdbcTemplate jdbc;
    @Autowired private UnidadeDeSaudeRepository unidadeRepository;
    @Autowired private ProfissionalRepository profissionalRepository;
    @Autowired private PacienteRepository pacienteRepository;
    @Autowired private AgendamentoRepository agendamentoRepository;

    private UnidadeDeSaude garagem;
    private UnidadeDeSaude ubs;
    private UnidadeDeSaude hospital;
    private UnidadeDeSaude base;
    private Paciente adulto;
    private Paciente adulto2;
    private Paciente adulto3;
    private Paciente crianca;
    private Profissional medico;
    private LocalDate amanha;

    @BeforeEach
    void seed() {
        limpar();
        amanha = LocalDate.now(ZONA).plusDays(1);
        garagem = unidade("Garagem", "Cidade Alfa");
        ubs = unidade("UBS", "Cidade Alfa");
        hospital = unidade("Hospital", "Cidade Beta");
        base = unidade("Base SAMU", "Cidade Alfa");
        jdbc.update("insert into tb_unidade_perfil_operacional (uuid, unidade_id, perfil_operacional_id, inicio) select gen_random_uuid(), ?, uuid, ? "
                + "from tb_perfil_operacional where codigo = 'ATENCAO_MOVEL'", base.getUuid(), LocalDate.now().minusDays(1));
        adulto = paciente("Adulto", "90170000001", LocalDate.now().minusYears(60));
        adulto2 = paciente("Adulto Dois", "90170000002", LocalDate.now().minusYears(45));
        adulto3 = paciente("Adulto Tres", "90170000003", LocalDate.now().minusYears(30));
        crianca = paciente("Crianca", "90170000004", LocalDate.now().minusYears(8));
        for (String m : List.of("TE-COND1", "TE-COND2", "TE-MEDICO")) {
            Profissional p = new Profissional();
            p.setMatricula(m);
            p.setCpf("9017" + String.format("%07d", Math.abs(m.hashCode()) % 10_000_000));
            p.setNome(PREFIXO + m);
            p.setAtivo(true);
            Profissional salvo = profissionalRepository.save(p);
            if (m.equals("TE-MEDICO")) {
                medico = salvo;
            }
        }
    }

    @AfterEach
    void limpar() {
        String unidades = "(select uuid from tb_unidade_de_saude where nome like '" + PREFIXO + "%')";
        String veiculos = "(select uuid from tb_veiculo where unidade_id in " + unidades + ")";
        jdbc.update("delete from tb_embarque_viagem where viagem_id in (select uuid from tb_viagem_transporte where veiculo_id in " + veiculos + ")");
        jdbc.update("delete from tb_viagem_transporte where veiculo_id in " + veiculos);
        jdbc.update("delete from tb_solicitacao_transporte where unidade_id in " + unidades);
        jdbc.update("delete from tb_agendamento where unidade_id in " + unidades);
        jdbc.update("delete from tb_situacao_veiculo where veiculo_id in " + veiculos);
        jdbc.update("delete from tb_veiculo where unidade_id in " + unidades);
        jdbc.update("delete from tb_unidade_perfil_operacional where unidade_id in " + unidades);
        jdbc.update("delete from tb_paciente where nome like ?", PREFIXO + "%");
        jdbc.update("delete from tb_profissional where matricula like 'TE-%'");
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

    private Paciente paciente(String nome, String cpf, LocalDate nascimento) {
        Paciente p = new Paciente();
        p.setNome(PREFIXO + nome);
        p.setCpf(cpf);
        p.setDataNascimento(nascimento);
        p.setAtivo(true);
        Endereco e = new Endereco();
        e.setLogradouro("Rua das Flores");
        e.setNumeroLogradouro("10");
        e.setCidade("Cidade Alfa");
        e.setEstado("SP");
        p.setEndereco(e);
        return pacienteRepository.save(p);
    }

    private String enviar(MockHttpServletRequestBuilder r, String corpo, int esperado) throws Exception {
        var resposta = mockMvc.perform(r.contentType(MediaType.APPLICATION_JSON).content(corpo)).andReturn().getResponse();
        assertThat(resposta.getStatus()).as(resposta.getContentAsString()).isEqualTo(esperado);
        return resposta.getContentAsString();
    }

    private String ler(String url) throws Exception {
        var resposta = mockMvc.perform(get(url)).andReturn().getResponse();
        assertThat(resposta.getStatus()).as(resposta.getContentAsString()).isEqualTo(200);
        return resposta.getContentAsString();
    }

    /** Uma solicitação simples; {@code extra} entra no corpo (com vírgula na frente). */
    private String pedir(Paciente p, String motivo, LocalDateTime quando, String extra, int esperado) throws Exception {
        String r = enviar(post(URL + "solicitacao"), ("{\"pacienteId\": \"%s\", \"unidadeId\": \"%s\", \"motivo\": \"%s\", \"trecho\": \"IDA_E_VOLTA\", "
                + "\"dataHora\": \"%s\", \"destinoUnidadeId\": \"%s\"%s}").formatted(p.getUuid(), ubs.getUuid(), motivo, quando, hospital.getUuid(), extra),
                esperado);
        return esperado == 201 ? JsonPath.read(r, "$.uuid") : r;
    }

    private String veiculo(String placa, String prefixo, String tipo, UnidadeDeSaude u, int lugares, boolean maca, boolean cadeira) throws Exception {
        String r = enviar(post(URL + "veiculo"), ("{\"placa\": \"%s\", \"prefixo\": \"%s\", \"tipo\": \"%s\", \"unidadeId\": \"%s\", \"lugares\": %d, "
                + "\"maca\": %s, \"cadeiraDeRodas\": %s, \"quilometragem\": 5000}").formatted(placa, prefixo, tipo, u.getUuid(), lugares, maca, cadeira), 201);
        return JsonPath.read(r, "$.uuid");
    }

    private String viagem(String veiculo, String condutor, LocalTime saida, LocalTime retorno, List<String> solicitacoes, int esperado) throws Exception {
        String ids = String.join(", ", solicitacoes.stream().map(s -> "\"" + s + "\"").toList());
        String r = enviar(post(URL + "viagem"), "{\"veiculoId\": \"%s\", \"condutorMatricula\": \"%s\", \"saidaPrevista\": \"%s\", \"retornoPrevisto\": \"%s\", \"solicitacoes\": [%s]}"
                .formatted(veiculo, condutor, instante(saida), instante(retorno), ids), esperado);
        return esperado == 201 ? JsonPath.read(r, "$.uuid") : r;
    }

    private Instant instante(LocalTime hora) {
        return amanha.atTime(hora).atZone(ZONA).toInstant();
    }

    private String situacao(String solicitacao) throws Exception {
        return JsonPath.read(ler(URL + "solicitacao/" + solicitacao), "$.situacao");
    }

    @Test
    void solicitacaoComAcompanhanteDestinoEVinculo() throws Exception {
        LocalDateTime nove = amanha.atTime(9, 0);
        // Menor de 18 e maca pedem acompanhante.
        pedir(crianca, "CONSULTA", nove, "", 400);
        pedir(adulto, "CONSULTA", nove, ", \"maca\": true", 400);
        // Sem destino nem unidade de destino.
        enviar(post(URL + "solicitacao"), "{\"pacienteId\": \"%s\", \"unidadeId\": \"%s\", \"motivo\": \"EXAME\", \"trecho\": \"IDA\", \"dataHora\": \"%s\"}"
                .formatted(adulto.getUuid(), ubs.getUuid(), nove), 400);
        // No passado.
        pedir(adulto, "CONSULTA", LocalDate.now(ZONA).minusDays(2).atTime(9, 0), "", 422);
        // Ligada a dois vínculos de uma vez.
        pedir(adulto, "CONSULTA", nove, ", \"agendamentoId\": \"%s\", \"internacaoId\": \"%s\"".formatted(java.util.UUID.randomUUID(),
                java.util.UUID.randomUUID()), 400);

        String id = pedir(crianca, "CONSULTA", nove, ", \"acompanhanteNome\": \"Mae da Crianca\"", 201);
        String r = ler(URL + "solicitacao/" + id);
        assertThat((String) JsonPath.read(r, "$.situacao")).isEqualTo("SOLICITADA");
        assertThat((String) JsonPath.read(r, "$.enderecoEmbarque")).startsWith("Rua das Flores, 10");
        assertThat((String) JsonPath.read(r, "$.destino")).startsWith(PREFIXO + "Hospital");
        assertThat((String) JsonPath.read(r, "$.destinoMunicipio")).isEqualTo("Cidade Beta");
        assertThat((Integer) JsonPath.read(r, "$.lugares")).isEqualTo(2);
        // O mesmo paciente na mesma hora.
        pedir(crianca, "EXAME", nove, ", \"acompanhanteNome\": \"Pai\"", 409);

        // Com o agendamento, a data e o destino vêm dele; de outro paciente, 422.
        Agendamento a = new Agendamento();
        a.setPaciente(adulto);
        a.setProfissional(medico);
        a.setUnidade(hospital);
        a.setDataHora(amanha.atTime(10, 30));
        a.setStatus(StatusAgendamento.AGENDADO);
        a.setTipo(TipoAgendamento.CONSULTA);
        a = agendamentoRepository.save(a);
        enviar(post(URL + "solicitacao"), "{\"pacienteId\": \"%s\", \"unidadeId\": \"%s\", \"motivo\": \"CONSULTA\", \"trecho\": \"IDA\", \"agendamentoId\": \"%s\"}"
                .formatted(adulto2.getUuid(), ubs.getUuid(), a.getUuid()), 422);
        r = enviar(post(URL + "solicitacao"), "{\"pacienteId\": \"%s\", \"unidadeId\": \"%s\", \"motivo\": \"CONSULTA\", \"trecho\": \"IDA\", \"agendamentoId\": \"%s\"}"
                .formatted(adulto.getUuid(), ubs.getUuid(), a.getUuid()), 201);
        assertThat((String) JsonPath.read(r, "$.dataHora")).startsWith(amanha + "T10:30");
        assertThat((String) JsonPath.read(r, "$.destinoUnidadeId")).isEqualTo(hospital.getUuid().toString());

        // A fila da unidade e do paciente.
        assertThat((List<?>) JsonPath.read(ler(URL + "solicitacao?unidadeId=" + ubs.getUuid()), "$")).hasSize(2);
        assertThat((List<?>) JsonPath.read(ler(URL + "solicitacao?pacienteId=" + adulto.getUuid()), "$")).hasSize(1);
    }

    @Test
    void tfdComLaudoEComissao() throws Exception {
        LocalDateTime oito = amanha.atTime(8, 0);
        // Sem laudo.
        pedir(adulto, "TFD", oito, "", 400);
        // Para o mesmo município da origem.
        enviar(post(URL + "solicitacao"), ("{\"pacienteId\": \"%s\", \"unidadeId\": \"%s\", \"motivo\": \"TFD\", \"trecho\": \"IDA_E_VOLTA\", \"dataHora\": \"%s\", "
                + "\"destino\": \"Hospital de Alfa\", \"destinoMunicipio\": \"cidade alfa\", \"cid\": \"C50\", \"justificativa\": \"Radioterapia\", "
                + "\"profissionalMatricula\": \"TE-MEDICO\"}").formatted(adulto.getUuid(), ubs.getUuid(), oito), 422);
        String laudo = ", \"cid\": \"c50\", \"justificativa\": \"Radioterapia sem oferta no município\", \"profissionalMatricula\": \"TE-MEDICO\"";
        String tfd = pedir(adulto, "TFD", oito, laudo, 201);
        String r = ler(URL + "solicitacao/" + tfd);
        assertThat((String) JsonPath.read(r, "$.situacao")).isEqualTo("AGUARDANDO_AUTORIZACAO");
        assertThat((String) JsonPath.read(r, "$.cid")).isEqualTo("C50");
        assertThat((String) JsonPath.read(r, "$.profissionalNome")).isEqualTo(PREFIXO + "TE-MEDICO");

        // Negar pede motivo; autorizar duas vezes, 422.
        enviar(post(URL + "solicitacao/" + tfd + "/negacao"), "{}", 400);
        enviar(post(URL + "solicitacao/" + tfd + "/autorizacao"), "", 200);
        enviar(post(URL + "solicitacao/" + tfd + "/autorizacao"), "", 422);
        assertThat(situacao(tfd)).isEqualTo("AUTORIZADA");

        String outro = pedir(adulto2, "TFD", oito, laudo, 201);
        r = enviar(post(URL + "solicitacao/" + outro + "/negacao"), "{\"motivo\": \"Há oferta na região\"}", 200);
        assertThat((String) JsonPath.read(r, "$.situacao")).isEqualTo("NEGADA");
        assertThat((String) JsonPath.read(r, "$.motivoSituacao")).isEqualTo("Há oferta na região");
    }

    @Test
    void hemodialiseRecorrenteECancelamentoDasProximas() throws Exception {
        LocalDate segunda = LocalDate.now(ZONA).with(TemporalAdjusters.next(DayOfWeek.MONDAY));
        String corpo = ("{\"pacienteId\": \"%s\", \"unidadeId\": \"%s\", \"motivo\": \"%s\", \"trecho\": \"IDA_E_VOLTA\", \"dataHora\": \"%s\", "
                + "\"destino\": \"Clínica de hemodiálise\", \"diasDaSemana\": [\"MONDAY\", \"WEDNESDAY\", \"FRIDAY\"], \"recorrenteAte\": \"%s\"}");
        // Consulta não é recorrente; mais de 90 dias; a simples não aceita os dias da semana.
        enviar(post(URL + "solicitacao/recorrente"), corpo.formatted(adulto.getUuid(), ubs.getUuid(), "CONSULTA", segunda.atTime(6, 0), segunda.plusDays(13)), 400);
        enviar(post(URL + "solicitacao/recorrente"), corpo.formatted(adulto.getUuid(), ubs.getUuid(), "HEMODIALISE", segunda.atTime(6, 0), segunda.plusDays(91)), 400);
        enviar(post(URL + "solicitacao"), corpo.formatted(adulto.getUuid(), ubs.getUuid(), "HEMODIALISE", segunda.atTime(6, 0), segunda.plusDays(13)), 400);

        String r = enviar(post(URL + "solicitacao/recorrente"), corpo.formatted(adulto.getUuid(), ubs.getUuid(), "HEMODIALISE", segunda.atTime(6, 0),
                segunda.plusDays(13)), 201);
        List<String> ids = JsonPath.read(r, "$[*].uuid");
        assertThat(ids).hasSize(6);
        List<String> datas = JsonPath.read(r, "$[*].dataHora");
        assertThat(datas.get(1)).startsWith(segunda.plusDays(2) + "T06:00");
        String recorrencia = JsonPath.read(r, "$[0].recorrenciaId");
        assertThat((List<String>) JsonPath.read(r, "$[*].recorrenciaId")).containsOnly(recorrencia);

        // Cancela a segunda sessão e as próximas: a primeira fica.
        enviar(post(URL + "solicitacao/" + ids.get(1) + "/cancelamento"), "{\"motivo\": \"Transplante\"}", 200);
        assertThat(situacao(ids.get(2))).isEqualTo("SOLICITADA");
        r = enviar(post(URL + "solicitacao/" + ids.get(2) + "/cancelamento"), "{\"motivo\": \"Mudou de clínica\", \"recorrencia\": true}", 200);
        assertThat((List<?>) JsonPath.read(r, "$")).hasSize(4);
        assertThat(situacao(ids.get(0))).isEqualTo("SOLICITADA");
        assertThat(situacao(ids.get(5))).isEqualTo("CANCELADA");
        enviar(post(URL + "solicitacao/" + ids.get(5) + "/cancelamento"), "{\"motivo\": \"De novo\"}", 422);
    }

    @Test
    void viagemDoProgramarAChegada() throws Exception {
        String van = veiculo("TEV1A11", "TE-VAN-1", "VAN_SANITARIA", garagem, 3, false, true);
        String van2 = veiculo("TEV1A12", "TE-VAN-2", "VAN_SANITARIA", garagem, 8, true, true);
        String usb = veiculo("TEV1A13", "TE-USB-1", "USB", base, 2, true, false);

        String s1 = pedir(adulto, "CONSULTA", amanha.atTime(9, 0), ", \"cadeiraDeRodas\": true", 201);
        String s2 = pedir(adulto2, "EXAME", amanha.atTime(10, 0), ", \"acompanhanteNome\": \"Esposa\"", 201);
        String s3 = pedir(adulto3, "CONSULTA", amanha.atTime(9, 30), ", \"acompanhanteNome\": \"Irmao\", \"maca\": true", 201);
        String tarde = pedir(crianca, "CONSULTA", amanha.atTime(16, 0), ", \"acompanhanteNome\": \"Mae\"", 201);
        String tfd = pedir(adulto3, "TFD", amanha.atTime(11, 0), ", \"cid\": \"N18\", \"justificativa\": \"Nefrologia\", \"profissionalMatricula\": \"TE-MEDICO\"", 201);

        LocalTime sete = LocalTime.of(7, 0);
        LocalTime treze = LocalTime.of(13, 0);
        // Viatura do SAMU, TFD sem autorização, maca sem maca, lugares, fora da janela, janela invertida.
        viagem(usb, "TE-COND1", sete, treze, List.of(s1), 422);
        viagem(van, "TE-COND1", sete, treze, List.of(tfd), 422);
        viagem(van, "TE-COND1", sete, treze, List.of(s3), 422);
        viagem(van, "TE-COND1", sete, treze, List.of(s1, s2, tarde), 422);
        viagem(van, "TE-COND1", sete, treze, List.of(tarde), 422);
        viagem(van, "TE-COND1", treze, sete, List.of(s1), 400);

        String v1 = viagem(van, "TE-COND1", sete, treze, List.of(s1, s2), 201);
        String r = ler(URL + "viagem/" + v1);
        assertThat((Integer) JsonPath.read(r, "$.lugaresOcupados")).isEqualTo(3);
        assertThat(situacao(s1)).isEqualTo("PROGRAMADA");
        assertThat((String) JsonPath.read(ler(URL + "solicitacao/" + s1), "$.viagemId")).isEqualTo(v1);
        // A mesma solicitação noutra viagem; o veículo e o condutor no mesmo horário.
        viagem(van2, "TE-COND2", sete, treze, List.of(s1), 409);
        viagem(van, "TE-COND2", LocalTime.of(12, 0), LocalTime.of(18, 0), List.of(tarde), 409);
        viagem(van2, "TE-COND1", LocalTime.of(12, 0), LocalTime.of(18, 0), List.of(tarde), 409);

        // Troca os embarques: s2 sai e volta para a fila.
        r = enviar(put(URL + "viagem/" + v1 + "/embarques"), "{\"solicitacoes\": [\"%s\"]}".formatted(s1), 200);
        assertThat((List<?>) JsonPath.read(r, "$.embarques")).hasSize(1);
        assertThat(situacao(s2)).isEqualTo("SOLICITADA");
        enviar(put(URL + "viagem/" + v1 + "/embarques"), "{\"solicitacoes\": [\"%s\", \"%s\"]}".formatted(s1, s2), 200);

        // Embarque e chegada antes da saída; quilometragem que volta.
        enviar(post(URL + "viagem/" + v1 + "/chegada"), "{\"quilometragem\": 5100}", 422);
        enviar(post(URL + "viagem/" + v1 + "/saida"), "{\"quilometragem\": 4999}", 422);
        enviar(post(URL + "viagem/" + v1 + "/saida"), "{\"quilometragem\": 5010}", 200);
        assertThat((String) JsonPath.read(ler(URL + "veiculo/" + van), "$.situacao")).isEqualTo("EM_SERVICO");
        enviar(put(URL + "viagem/" + v1 + "/embarques"), "{\"solicitacoes\": [\"%s\"]}".formatted(s1), 422);
        enviar(post(URL + "viagem/" + v1 + "/cancelamento"), "{\"motivo\": \"Chuva\"}", 422);

        // Chegada sem todos os embarques registrados; s2 faltou.
        enviar(post(URL + "viagem/" + v1 + "/embarque"), "{\"itens\": [{\"solicitacaoId\": \"%s\", \"embarcou\": true}]}".formatted(s1), 200);
        enviar(post(URL + "viagem/" + v1 + "/chegada"), "{\"quilometragem\": 5100}", 422);
        enviar(post(URL + "viagem/" + v1 + "/embarque"), "{\"itens\": [{\"solicitacaoId\": \"%s\", \"embarcou\": false, \"motivo\": \"Não estava em casa\"}]}"
                .formatted(s2), 200);
        enviar(post(URL + "viagem/" + v1 + "/embarque"), "{\"itens\": [{\"solicitacaoId\": \"%s\", \"embarcou\": true}]}".formatted(tarde), 422);
        enviar(post(URL + "viagem/" + v1 + "/chegada"), "{\"quilometragem\": 5009}", 422);
        r = enviar(post(URL + "viagem/" + v1 + "/chegada"), "{\"quilometragem\": 5087}", 200);
        assertThat((String) JsonPath.read(r, "$.situacao")).isEqualTo("CONCLUIDA");
        assertThat((Integer) JsonPath.read(r, "$.kmPercorridos")).isEqualTo(77);
        assertThat(situacao(s1)).isEqualTo("REALIZADA");
        assertThat(situacao(s2)).isEqualTo("FALTOU");
        r = ler(URL + "veiculo/" + van);
        assertThat((String) JsonPath.read(r, "$.situacao")).isEqualTo("DISPONIVEL");
        assertThat((Integer) JsonPath.read(r, "$.quilometragem")).isEqualTo(5087);

        // Com maca: a van 2 leva; cancelar devolve à fila. O TFD autorizado entra.
        enviar(post(URL + "solicitacao/" + tfd + "/autorizacao"), "", 200);
        String v2 = viagem(van2, "TE-COND2", sete, treze, List.of(s3, tfd), 201);
        r = enviar(post(URL + "viagem/" + v2 + "/cancelamento"), "{\"motivo\": \"Veículo com pane\"}", 200);
        assertThat((String) JsonPath.read(r, "$.situacao")).isEqualTo("CANCELADA");
        assertThat(situacao(s3)).isEqualTo("SOLICITADA");
        assertThat(situacao(tfd)).isEqualTo("AUTORIZADA");

        // A agenda do dia e a quilometragem.
        assertThat((List<?>) JsonPath.read(ler(URL + "viagem?data=" + amanha + "&unidadeId=" + garagem.getUuid()), "$")).hasSize(2);
        r = ler(URL + "quilometragem?de=" + LocalDate.now(ZONA) + "&ate=" + amanha.plusDays(1) + "&unidadeId=" + garagem.getUuid());
        assertThat((List<?>) JsonPath.read(r, "$")).hasSize(1);
        assertThat((Integer) JsonPath.read(r, "$[0].km")).isEqualTo(77);
        assertThat((Integer) JsonPath.read(r, "$[0].pacientes")).isEqualTo(1);
    }
}
