package com.edufelizardo.maissaudepublica.services.version1;

import com.edufelizardo.maissaudepublica.config.ContextoAuditoria;
import com.edufelizardo.maissaudepublica.config.UsuarioAutenticado;
import com.edufelizardo.maissaudepublica.exceptions.ResourceBadRequestException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceConflictException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceNotFoundException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceUnprocessableEntityException;
import com.edufelizardo.maissaudepublica.models.Agendamento;
import com.edufelizardo.maissaudepublica.models.EmbarqueViagem;
import com.edufelizardo.maissaudepublica.models.Endereco;
import com.edufelizardo.maissaudepublica.models.Internacao;
import com.edufelizardo.maissaudepublica.models.Paciente;
import com.edufelizardo.maissaudepublica.models.Profissional;
import com.edufelizardo.maissaudepublica.models.SolicitacaoRegulacao;
import com.edufelizardo.maissaudepublica.models.SolicitacaoTransporte;
import com.edufelizardo.maissaudepublica.models.UnidadeDeSaude;
import com.edufelizardo.maissaudepublica.models.Veiculo;
import com.edufelizardo.maissaudepublica.models.ViagemTransporte;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.SolicitacaoTransporteRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.ViagemTransporteRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.TransporteEletivoDtos;
import com.edufelizardo.maissaudepublica.models.enuns.MotivoTransporte;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoEmbarque;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoSolicitacaoTransporte;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoVeiculo;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoViagem;
import com.edufelizardo.maissaudepublica.models.enuns.StatusAgendamento;
import com.edufelizardo.maissaudepublica.models.enuns.StatusInternacao;
import com.edufelizardo.maissaudepublica.models.enuns.StatusSolicitacaoRegulacao;
import com.edufelizardo.maissaudepublica.repositories.AgendamentoRepository;
import com.edufelizardo.maissaudepublica.repositories.InternacaoRepository;
import com.edufelizardo.maissaudepublica.repositories.PacienteRepository;
import com.edufelizardo.maissaudepublica.repositories.ProfissionalRepository;
import com.edufelizardo.maissaudepublica.repositories.SolicitacaoRegulacaoRepository;
import com.edufelizardo.maissaudepublica.repositories.SolicitacaoTransporteRepository;
import com.edufelizardo.maissaudepublica.repositories.UnidadeDeSaudeRepository;
import com.edufelizardo.maissaudepublica.repositories.VeiculoRepository;
import com.edufelizardo.maissaudepublica.repositories.ViagemTransporteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Transporte, fatia T2 (ADR-0190): a solicitação de transporte eletivo (consulta, exame, hemodiálise, terapia seriada, alta e TFD), a
 * autorização do TFD pela comissão, a viagem com o veículo, o condutor e os embarques, e a quilometragem. A frota e o "em serviço" são
 * da T1 ({@link TransporteService}).
 */
@Service
public class TransporteEletivoService {

    public static final String SOLICITAR = "TRANSPORTE.SOLICITAR";
    public static final String TFD_AUTORIZAR = "TFD.AUTORIZAR";
    private static final String CONSULTAR = TransporteService.CONSULTAR;
    private static final String GERENCIAR = TransporteService.GERENCIAR;
    private static final int RECORRENCIA_MAXIMA_DIAS = 90;
    private static final Duration VIAGEM_MAXIMA = Duration.ofHours(72);
    private static final ZoneId ZONA = ZoneId.systemDefault();
    private static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("dd/MM HH:mm");

    @Autowired private SolicitacaoTransporteRepository solicitacaoRepository;
    @Autowired private ViagemTransporteRepository viagemRepository;
    @Autowired private VeiculoRepository veiculoRepository;
    @Autowired private PacienteRepository pacienteRepository;
    @Autowired private UnidadeDeSaudeRepository unidadeRepository;
    @Autowired private AgendamentoRepository agendamentoRepository;
    @Autowired private SolicitacaoRegulacaoRepository regulacaoRepository;
    @Autowired private InternacaoRepository internacaoRepository;
    @Autowired private ProfissionalRepository profissionalRepository;
    @Autowired private TransporteService transporteService;
    @Autowired private FrotaEmergenciaService frotaEmergenciaService;
    @Autowired private ControleDeAcesso controleDeAcesso;

    // ── Solicitação ───────────────────────────────────────────────────────────────────────────────

    @Transactional
    public TransporteEletivoDtos.Solicitacao solicitar(SolicitacaoTransporteRequestDto dto) {
        if (dto.getDiasDaSemana() != null && !dto.getDiasDaSemana().isEmpty() || dto.getRecorrenteAte() != null) {
            throw new ResourceBadRequestException("Para repetir em dias da semana, use a solicitação recorrente.");
        }
        SolicitacaoTransporte s = montar(dto);
        exigirSemDuplicada(s);
        solicitacaoRepository.save(s);
        ContextoAuditoria.registro(s.getUuid());
        ContextoAuditoria.detalhe("Transporte " + s.getMotivo() + " para " + s.getDataHora().format(HORA) + ".");
        return dto(s, verLaudo(), null);
    }

    /** Hemodiálise e terapia seriada: uma solicitação por data nos dias da semana até a data final (até 90 dias). */
    @Transactional
    public List<TransporteEletivoDtos.Solicitacao> solicitarRecorrente(SolicitacaoTransporteRequestDto dto) {
        if (dto.getDiasDaSemana() == null || dto.getDiasDaSemana().isEmpty() || dto.getRecorrenteAte() == null) {
            throw new ResourceBadRequestException("Informe os dias da semana e a data final da recorrência.");
        }
        if (dto.getMotivo() == null || !dto.getMotivo().recorrente()) {
            throw new ResourceBadRequestException("A recorrência é para hemodiálise e terapia seriada.");
        }
        if (dto.getAgendamentoId() != null || dto.getSolicitacaoRegulacaoId() != null || dto.getInternacaoId() != null) {
            throw new ResourceBadRequestException("A recorrência não se liga a um agendamento, a uma regulação ou a uma internação.");
        }
        SolicitacaoTransporte modelo = montar(dto);
        LocalDate inicio = modelo.getDataHora().toLocalDate();
        if (dto.getRecorrenteAte().isBefore(inicio)) {
            throw new ResourceBadRequestException("A data final vem depois da primeira sessão.");
        }
        if (dto.getRecorrenteAte().isAfter(inicio.plusDays(RECORRENCIA_MAXIMA_DIAS))) {
            throw new ResourceBadRequestException("A recorrência vai até " + RECORRENCIA_MAXIMA_DIAS + " dias; renove depois.");
        }
        Set<DayOfWeek> dias = EnumSet.copyOf(dto.getDiasDaSemana());
        UUID recorrencia = UUID.randomUUID();
        List<SolicitacaoTransporte> geradas = new ArrayList<>();
        for (LocalDate d = inicio; !d.isAfter(dto.getRecorrenteAte()); d = d.plusDays(1)) {
            if (!dias.contains(d.getDayOfWeek())) {
                continue;
            }
            SolicitacaoTransporte s = copia(modelo, d.atTime(modelo.getDataHora().toLocalTime()));
            s.setRecorrenciaId(recorrencia);
            exigirSemDuplicada(s);
            geradas.add(s);
        }
        if (geradas.isEmpty()) {
            throw new ResourceBadRequestException("Nenhuma data cai nos dias da semana escolhidos.");
        }
        solicitacaoRepository.saveAll(geradas);
        ContextoAuditoria.registro(recorrencia);
        ContextoAuditoria.detalhe(geradas.size() + " sessões de " + modelo.getMotivo() + " até " + dto.getRecorrenteAte() + ".");
        boolean laudo = verLaudo();
        return geradas.stream().map(s -> dto(s, laudo, null)).toList();
    }

    /** Cancela, com motivo; na recorrente, pode cancelar também as próximas que ainda não estão numa viagem. */
    @Transactional
    public List<TransporteEletivoDtos.Solicitacao> cancelar(UUID uuid, ViagemTransporteRequestDto.Motivo dto) {
        SolicitacaoTransporte s = travarSolicitacao(uuid);
        controleDeAcesso.exigir(SOLICITAR, s.getUnidade());
        if (!s.getSituacao().cancelavel()) {
            throw new ResourceUnprocessableEntityException(s.getSituacao() == SituacaoSolicitacaoTransporte.PROGRAMADA
                    ? "A solicitação está numa viagem; retire-a da viagem antes de cancelar."
                    : "A solicitação está " + s.getSituacao() + "; não cancela mais.");
        }
        List<SolicitacaoTransporte> alvo = new ArrayList<>(List.of(s));
        if (Boolean.TRUE.equals(dto.getRecorrencia()) && s.getRecorrenciaId() != null) {
            solicitacaoRepository.findByRecorrenciaIdOrderByDataHora(s.getRecorrenciaId()).stream()
                    .filter(x -> x.getDataHora().isAfter(s.getDataHora()) && x.getSituacao().cancelavel()).forEach(alvo::add);
        }
        for (SolicitacaoTransporte x : alvo) {
            decidir(x, SituacaoSolicitacaoTransporte.CANCELADA, dto.getMotivo().trim());
        }
        ContextoAuditoria.paciente(s.getPaciente().getUuid());
        ContextoAuditoria.registro(s.getUuid());
        ContextoAuditoria.detalhe(alvo.size() == 1 ? "Transporte cancelado." : alvo.size() + " transportes da recorrência cancelados.");
        boolean laudo = verLaudo();
        return alvo.stream().map(x -> dto(x, laudo, null)).toList();
    }

    /** A comissão de TFD autoriza: a solicitação vai para a fila das viagens. */
    @Transactional
    public TransporteEletivoDtos.Solicitacao autorizar(UUID uuid) {
        SolicitacaoTransporte s = aguardandoComissao(uuid);
        decidir(s, SituacaoSolicitacaoTransporte.AUTORIZADA, null);
        ContextoAuditoria.detalhe("TFD autorizado.");
        return dto(s, true, null);
    }

    @Transactional
    public TransporteEletivoDtos.Solicitacao negar(UUID uuid, ViagemTransporteRequestDto.Motivo dto) {
        SolicitacaoTransporte s = aguardandoComissao(uuid);
        decidir(s, SituacaoSolicitacaoTransporte.NEGADA, dto.getMotivo().trim());
        ContextoAuditoria.detalhe("TFD negado.");
        return dto(s, true, null);
    }

    /** A fila e o histórico: da unidade que pede, ou da rede para quem programa, consulta a frota ou autoriza o TFD. */
    @Transactional(readOnly = true)
    public List<TransporteEletivoDtos.Solicitacao> listar(UUID unidadeId, UUID pacienteId, SituacaoSolicitacaoTransporte situacao,
                                                          MotivoTransporte motivo, LocalDate de, LocalDate ate) {
        controleDeAcesso.exigirAlguma(SOLICITAR, CONSULTAR, GERENCIAR, TFD_AUTORIZAR);
        List<SolicitacaoTransporte> base;
        if (pacienteId != null) {
            base = solicitacaoRepository.findDoPaciente(pacienteId);
            ContextoAuditoria.paciente(pacienteId);
        } else {
            LocalDate inicio = de != null ? de : LocalDate.now(ZONA).minusDays(7);
            LocalDate fim = ate != null ? ate : inicio.plusDays(67);
            if (fim.isBefore(inicio) || fim.isAfter(inicio.plusDays(120))) {
                throw new ResourceBadRequestException("O período vai de um dia a 120 dias.");
            }
            base = solicitacaoRepository.findNoPeriodo(inicio.atStartOfDay(), fim.plusDays(1).atStartOfDay());
        }
        Stream<SolicitacaoTransporte> visiveis = controleDeAcesso.temAlguma(CONSULTAR, GERENCIAR, TFD_AUTORIZAR)
                ? base.stream()
                : controleDeAcesso.filtrar(base.stream(), s -> Stream.of(s.getUnidade()), SOLICITAR);
        List<SolicitacaoTransporte> lista = visiveis
                .filter(s -> unidadeId == null || s.getUnidade().getUuid().equals(unidadeId))
                .filter(s -> situacao == null || s.getSituacao() == situacao)
                .filter(s -> motivo == null || s.getMotivo() == motivo).toList();
        Map<UUID, UUID> viagens = viagensDe(lista);
        boolean laudo = verLaudo();
        return lista.stream().map(s -> dto(s, laudo, viagens.get(s.getUuid()))).toList();
    }

    @Transactional(readOnly = true)
    public TransporteEletivoDtos.Solicitacao buscar(UUID uuid) {
        SolicitacaoTransporte s = solicitacaoRepository.findById(uuid).orElseThrow(() -> solicitacaoNaoEncontrada(uuid));
        controleDeAcesso.exigirAlguma(SOLICITAR, CONSULTAR, GERENCIAR, TFD_AUTORIZAR);
        if (!controleDeAcesso.temAlguma(CONSULTAR, GERENCIAR, TFD_AUTORIZAR)) {
            controleDeAcesso.exigirVisivel(s.getUnidade(), SOLICITAR);
        }
        ContextoAuditoria.paciente(s.getPaciente().getUuid());
        return dto(s, verLaudo(), viagensDe(List.of(s)).get(s.getUuid()));
    }

    // ── Viagem ────────────────────────────────────────────────────────────────────────────────────

    @Transactional
    public TransporteEletivoDtos.Viagem programar(ViagemTransporteRequestDto dto) {
        Veiculo v = veiculoRepository.findByIdParaAtualizar(dto.getVeiculoId()).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar o veículo " + dto.getVeiculoId() + " em nossos registros."));
        controleDeAcesso.exigir(GERENCIAR, v.getUnidade());
        if (v.getTipo().doSamu()) {
            throw new ResourceUnprocessableEntityException("A viatura " + v.getPrefixo() + " é do SAMU (" + v.getTipo()
                    + "); o transporte eletivo usa ambulância de transporte, van, micro-ônibus ou carro.");
        }
        if (v.getSituacao() == SituacaoVeiculo.INATIVO || v.getSituacao() == SituacaoVeiculo.MANUTENCAO) {
            throw new ResourceUnprocessableEntityException("O veículo " + v.getPrefixo() + " está " + v.getSituacao() + ".");
        }
        frotaEmergenciaService.exigirNaoCedido(v);
        if (!dto.getRetornoPrevisto().isAfter(dto.getSaidaPrevista())) {
            throw new ResourceBadRequestException("O retorno previsto vem depois da saída.");
        }
        if (Duration.between(dto.getSaidaPrevista(), dto.getRetornoPrevisto()).compareTo(VIAGEM_MAXIMA) > 0) {
            throw new ResourceBadRequestException("A viagem vai até 72 horas da saída ao retorno.");
        }
        Profissional condutor = condutor(dto.getCondutorMatricula());
        ViagemTransporte viagem = new ViagemTransporte();
        viagem.setVeiculo(v);
        viagem.setCondutor(condutor);
        viagem.setSaidaPrevista(dto.getSaidaPrevista());
        viagem.setRetornoPrevisto(dto.getRetornoPrevisto());
        viagem.setSituacao(SituacaoViagem.PROGRAMADA);
        viagem.setObservacao(texto(dto.getObservacao()));
        viagem.setRegistradoPorCpf(UsuarioAutenticado.cpf());
        exigirSemConflito(viagem);
        List<SolicitacaoTransporte> novas = solicitacoesParaEmbarque(dto.getSolicitacoes(), viagem, Set.of());
        exigirCompatibilidade(v, novas);
        for (SolicitacaoTransporte s : novas) {
            s.setSituacao(SituacaoSolicitacaoTransporte.PROGRAMADA);
            viagem.getEmbarques().add(new EmbarqueViagem(null, viagem, s, SituacaoEmbarque.PREVISTO));
        }
        String destino = texto(dto.getDestino());
        viagem.setDestino(destino != null ? destino : destinoResumo(novas));
        viagemRepository.save(viagem);
        ContextoAuditoria.unidade(v.getUnidade());
        ContextoAuditoria.registro(viagem.getUuid());
        ContextoAuditoria.detalhe("Viagem de " + v.getPrefixo() + " com " + novas.size() + " paciente(s).");
        return dto(viagem);
    }

    /** Troca os embarques de uma viagem programada; quem sai volta para a fila. */
    @Transactional
    public TransporteEletivoDtos.Viagem embarques(UUID uuid, ViagemTransporteRequestDto.Embarques dto) {
        ViagemTransporte viagem = travarViagem(uuid);
        controleDeAcesso.exigir(GERENCIAR, viagem.getVeiculo().getUnidade());
        exigirSituacao(viagem, SituacaoViagem.PROGRAMADA, "Os embarques mudam só antes da saída.");
        Set<UUID> pedidos = new HashSet<>();
        for (UUID id : dto.getSolicitacoes()) {
            if (!pedidos.add(id)) {
                throw new ResourceBadRequestException("A solicitação " + id + " aparece duas vezes.");
            }
        }
        Set<UUID> atuais = viagem.getEmbarques().stream().map(e -> e.getSolicitacao().getUuid()).collect(Collectors.toSet());
        List<SolicitacaoTransporte> novas = solicitacoesParaEmbarque(dto.getSolicitacoes().stream().filter(id -> !atuais.contains(id)).toList(),
                viagem, Set.of());
        List<SolicitacaoTransporte> ficam = viagem.getEmbarques().stream().map(EmbarqueViagem::getSolicitacao)
                .filter(s -> pedidos.contains(s.getUuid())).toList();
        List<SolicitacaoTransporte> todas = new ArrayList<>(ficam);
        todas.addAll(novas);
        exigirCompatibilidade(viagem.getVeiculo(), todas);
        viagem.getEmbarques().removeIf(e -> {
            if (pedidos.contains(e.getSolicitacao().getUuid())) {
                return false;
            }
            voltarParaFila(e.getSolicitacao());
            return true;
        });
        for (SolicitacaoTransporte s : novas) {
            s.setSituacao(SituacaoSolicitacaoTransporte.PROGRAMADA);
            viagem.getEmbarques().add(new EmbarqueViagem(null, viagem, s, SituacaoEmbarque.PREVISTO));
        }
        viagemRepository.save(viagem);
        ContextoAuditoria.registro(viagem.getUuid());
        ContextoAuditoria.detalhe("Embarques da viagem de " + viagem.getVeiculo().getPrefixo() + ": " + todas.size() + ".");
        return dto(viagem);
    }

    /** Sai: a quilometragem do painel (não volta) e o veículo em serviço. */
    @Transactional
    public TransporteEletivoDtos.Viagem sair(UUID uuid, ViagemTransporteRequestDto.Quilometragem dto) {
        ViagemTransporte viagem = travarViagem(uuid);
        controleDeAcesso.exigir(GERENCIAR, viagem.getVeiculo().getUnidade());
        exigirSituacao(viagem, SituacaoViagem.PROGRAMADA, "A viagem já saiu ou terminou.");
        Veiculo v = veiculoRepository.findByIdParaAtualizar(viagem.getVeiculo().getUuid()).orElseThrow();
        if (v.getSituacao() != SituacaoVeiculo.DISPONIVEL) {
            throw new ResourceUnprocessableEntityException("O veículo " + v.getPrefixo() + " não está disponível (" + v.getSituacao() + ").");
        }
        if (v.getQuilometragem() != null && dto.getQuilometragem() < v.getQuilometragem()) {
            throw new ResourceUnprocessableEntityException("A quilometragem não pode voltar: o veículo está em " + v.getQuilometragem() + ".");
        }
        transporteService.colocarEmServico(v, "Saída da viagem com " + viagem.getEmbarques().size() + " paciente(s).");
        viagem.setKmSaida(dto.getQuilometragem());
        viagem.setSaidaEm(Instant.now());
        viagem.setSituacao(SituacaoViagem.EM_ANDAMENTO);
        viagemRepository.save(viagem);
        ContextoAuditoria.registro(viagem.getUuid());
        ContextoAuditoria.detalhe("Saída de " + v.getPrefixo() + " com " + dto.getQuilometragem() + " km.");
        return dto(viagem);
    }

    /** Quem embarcou e quem faltou; corrigível até a chegada. */
    @Transactional
    public TransporteEletivoDtos.Viagem presencas(UUID uuid, ViagemTransporteRequestDto.Presencas dto) {
        ViagemTransporte viagem = travarViagem(uuid);
        controleDeAcesso.exigir(GERENCIAR, viagem.getVeiculo().getUnidade());
        exigirSituacao(viagem, SituacaoViagem.EM_ANDAMENTO, "O embarque se registra com a viagem em andamento.");
        Map<UUID, EmbarqueViagem> porSolicitacao = viagem.getEmbarques().stream()
                .collect(Collectors.toMap(e -> e.getSolicitacao().getUuid(), e -> e));
        for (ViagemTransporteRequestDto.Presenca p : dto.getItens()) {
            EmbarqueViagem e = porSolicitacao.get(p.getSolicitacaoId());
            if (e == null) {
                throw new ResourceUnprocessableEntityException("A solicitação " + p.getSolicitacaoId() + " não está nesta viagem.");
            }
            SolicitacaoTransporte s = e.getSolicitacao();
            if (Boolean.TRUE.equals(p.getEmbarcou())) {
                e.setSituacao(SituacaoEmbarque.EMBARCOU);
                s.setSituacao(SituacaoSolicitacaoTransporte.PROGRAMADA);
                s.setMotivoSituacao(null);
            } else {
                e.setSituacao(SituacaoEmbarque.FALTOU);
                s.setSituacao(SituacaoSolicitacaoTransporte.FALTOU);
                s.setMotivoSituacao(texto(p.getMotivo()));
            }
        }
        viagemRepository.save(viagem);
        ContextoAuditoria.registro(viagem.getUuid());
        return dto(viagem);
    }

    /** Chega: todos com embarque registrado, a quilometragem atualiza o veículo e ele fica disponível. */
    @Transactional
    public TransporteEletivoDtos.Viagem chegar(UUID uuid, ViagemTransporteRequestDto.Quilometragem dto) {
        ViagemTransporte viagem = travarViagem(uuid);
        controleDeAcesso.exigir(GERENCIAR, viagem.getVeiculo().getUnidade());
        exigirSituacao(viagem, SituacaoViagem.EM_ANDAMENTO, "A viagem não está em andamento.");
        long semRegistro = viagem.getEmbarques().stream().filter(e -> e.getSituacao() == SituacaoEmbarque.PREVISTO).count();
        if (semRegistro > 0) {
            throw new ResourceUnprocessableEntityException("Registre o embarque de todos antes da chegada (" + semRegistro + " sem registro).");
        }
        if (dto.getQuilometragem() < viagem.getKmSaida()) {
            throw new ResourceUnprocessableEntityException("A quilometragem de chegada vem depois da de saída (" + viagem.getKmSaida() + ").");
        }
        Veiculo v = veiculoRepository.findByIdParaAtualizar(viagem.getVeiculo().getUuid()).orElseThrow();
        if (v.getQuilometragem() == null || dto.getQuilometragem() > v.getQuilometragem()) {
            v.setQuilometragem(dto.getQuilometragem());
            veiculoRepository.save(v);
        }
        transporteService.liberar(v, "Chegada da viagem.");
        for (EmbarqueViagem e : viagem.getEmbarques()) {
            if (e.getSituacao() == SituacaoEmbarque.EMBARCOU) {
                e.getSolicitacao().setSituacao(SituacaoSolicitacaoTransporte.REALIZADA);
            }
        }
        viagem.setKmChegada(dto.getQuilometragem());
        viagem.setChegadaEm(Instant.now());
        viagem.setSituacao(SituacaoViagem.CONCLUIDA);
        viagemRepository.save(viagem);
        ContextoAuditoria.registro(viagem.getUuid());
        ContextoAuditoria.detalhe("Chegada de " + v.getPrefixo() + ": " + (dto.getQuilometragem() - viagem.getKmSaida()) + " km rodados.");
        return dto(viagem);
    }

    /** Cancela antes da saída; as solicitações voltam para a fila. */
    @Transactional
    public TransporteEletivoDtos.Viagem cancelarViagem(UUID uuid, ViagemTransporteRequestDto.Motivo dto) {
        ViagemTransporte viagem = travarViagem(uuid);
        controleDeAcesso.exigir(GERENCIAR, viagem.getVeiculo().getUnidade());
        exigirSituacao(viagem, SituacaoViagem.PROGRAMADA, "Só a viagem que ainda não saiu se cancela.");
        viagem.getEmbarques().forEach(e -> voltarParaFila(e.getSolicitacao()));
        viagem.setSituacao(SituacaoViagem.CANCELADA);
        viagem.setMotivoCancelamento(dto.getMotivo().trim());
        viagemRepository.save(viagem);
        ContextoAuditoria.registro(viagem.getUuid());
        ContextoAuditoria.detalhe("Viagem cancelada.");
        return dto(viagem);
    }

    /** A agenda: as viagens que cruzam o dia (padrão: hoje) e os próximos dias, até 14. */
    @Transactional(readOnly = true)
    public List<TransporteEletivoDtos.Viagem> viagens(LocalDate data, Integer dias, UUID veiculoId, UUID unidadeId) {
        controleDeAcesso.exigirAlguma(CONSULTAR, GERENCIAR);
        LocalDate inicio = data != null ? data : LocalDate.now(ZONA);
        int n = dias == null ? 1 : Math.max(1, Math.min(dias, 14));
        Instant de = inicio.atStartOfDay(ZONA).toInstant();
        Instant ate = inicio.plusDays(n).atStartOfDay(ZONA).toInstant();
        return controleDeAcesso.filtrar(viagemRepository.findNoPeriodo(de, ate).stream(), x -> Stream.of(x.getVeiculo().getUnidade()), CONSULTAR, GERENCIAR)
                .filter(x -> veiculoId == null || x.getVeiculo().getUuid().equals(veiculoId))
                .filter(x -> unidadeId == null || x.getVeiculo().getUnidade().getUuid().equals(unidadeId))
                .map(this::dto).toList();
    }

    @Transactional(readOnly = true)
    public TransporteEletivoDtos.Viagem viagem(UUID uuid) {
        ViagemTransporte viagem = viagemRepository.findById(uuid).orElseThrow(() -> viagemNaoEncontrada(uuid));
        controleDeAcesso.exigirVisivel(viagem.getVeiculo().getUnidade(), CONSULTAR, GERENCIAR);
        return dto(viagem);
    }

    /** Quilômetros das viagens concluídas por veículo, com a chegada no período (padrão: os últimos 30 dias). */
    @Transactional(readOnly = true)
    public List<TransporteEletivoDtos.Quilometragem> quilometragem(LocalDate de, LocalDate ate, UUID unidadeId) {
        controleDeAcesso.exigirAlguma(CONSULTAR, GERENCIAR);
        LocalDate fim = ate != null ? ate : LocalDate.now(ZONA);
        LocalDate inicio = de != null ? de : fim.minusDays(30);
        if (fim.isBefore(inicio) || fim.isAfter(inicio.plusDays(366))) {
            throw new ResourceBadRequestException("O período vai de um dia a um ano.");
        }
        Instant i = inicio.atStartOfDay(ZONA).toInstant();
        Instant f = fim.plusDays(1).atStartOfDay(ZONA).toInstant();
        Map<UUID, List<ViagemTransporte>> porVeiculo = new LinkedHashMap<>();
        controleDeAcesso.filtrar(viagemRepository.findConcluidasComChegadaEntre(i, f).stream(), x -> Stream.of(x.getVeiculo().getUnidade()),
                        CONSULTAR, GERENCIAR)
                .filter(x -> unidadeId == null || x.getVeiculo().getUnidade().getUuid().equals(unidadeId))
                .forEach(x -> porVeiculo.computeIfAbsent(x.getVeiculo().getUuid(), k -> new ArrayList<>()).add(x));
        return porVeiculo.values().stream().map(lista -> {
            Veiculo v = lista.get(0).getVeiculo();
            int km = lista.stream().mapToInt(x -> x.getKmChegada() - x.getKmSaida()).sum();
            int pacientes = (int) lista.stream().flatMap(x -> x.getEmbarques().stream()).filter(e -> e.getSituacao() == SituacaoEmbarque.EMBARCOU).count();
            return new TransporteEletivoDtos.Quilometragem(v.getUuid(), v.getPrefixo(), v.getTipo(), v.getUnidade().getNome(), lista.size(), pacientes, km);
        }).sorted(Comparator.comparing(TransporteEletivoDtos.Quilometragem::prefixo)).toList();
    }

    // ── Regras da solicitação ─────────────────────────────────────────────────────────────────────

    private SolicitacaoTransporte montar(SolicitacaoTransporteRequestDto dto) {
        UnidadeDeSaude origem = unidade(dto.getUnidadeId());
        controleDeAcesso.exigir(SOLICITAR, origem);
        Paciente paciente = pacienteRepository.findById(dto.getPacienteId()).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar o paciente " + dto.getPacienteId() + " em nossos registros."));
        long vinculos = Stream.of(dto.getAgendamentoId(), dto.getSolicitacaoRegulacaoId(), dto.getInternacaoId()).filter(x -> x != null).count();
        if (vinculos > 1) {
            throw new ResourceBadRequestException("Ligue a solicitação a um só: agendamento, regulação ou internação.");
        }
        SolicitacaoTransporte s = new SolicitacaoTransporte();
        s.setPaciente(paciente);
        s.setUnidade(origem);
        s.setMotivo(dto.getMotivo());
        s.setTrecho(dto.getTrecho());
        s.setDataHora(dto.getDataHora());
        s.setDestinoUnidade(dto.getDestinoUnidadeId() != null ? unidade(dto.getDestinoUnidadeId()) : null);
        String embarque = texto(dto.getEnderecoEmbarque());
        if (dto.getAgendamentoId() != null) {
            Agendamento a = agendamentoRepository.findById(dto.getAgendamentoId()).orElseThrow(() -> new ResourceNotFoundException(
                    "Não foi possível encontrar o agendamento " + dto.getAgendamentoId() + " em nossos registros."));
            exigirMesmoPaciente(a.getPaciente(), paciente, "O agendamento");
            if (a.getStatus() != StatusAgendamento.AGENDADO && a.getStatus() != StatusAgendamento.CONFIRMADO) {
                throw new ResourceUnprocessableEntityException("O agendamento está " + a.getStatus() + ".");
            }
            s.setAgendamento(a);
            s.setDataHora(a.getDataHora());
            s.setDestinoUnidade(a.getUnidade());
        } else if (dto.getSolicitacaoRegulacaoId() != null) {
            SolicitacaoRegulacao r = regulacaoRepository.findById(dto.getSolicitacaoRegulacaoId()).orElseThrow(() -> new ResourceNotFoundException(
                    "Não foi possível encontrar a solicitação de regulação " + dto.getSolicitacaoRegulacaoId() + " em nossos registros."));
            exigirMesmoPaciente(r.getPaciente(), paciente, "A solicitação regulada");
            if (r.getStatus() != StatusSolicitacaoRegulacao.AUTORIZADA && r.getStatus() != StatusSolicitacaoRegulacao.AGENDADA) {
                throw new ResourceUnprocessableEntityException("A solicitação regulada está " + r.getStatus() + "; o transporte é da autorizada ou agendada.");
            }
            LocalDateTime quando = r.getAgendamento() != null ? r.getAgendamento().getDataHora() : r.getDataHoraPrevista();
            if (quando == null || r.getUnidadeExecutante() == null) {
                throw new ResourceUnprocessableEntityException("A solicitação regulada ainda não tem a executante e a data.");
            }
            s.setSolicitacaoRegulacao(r);
            s.setDataHora(quando);
            s.setDestinoUnidade(r.getUnidadeExecutante());
        } else if (dto.getInternacaoId() != null) {
            Internacao i = internacaoRepository.findById(dto.getInternacaoId()).orElseThrow(() -> new ResourceNotFoundException(
                    "Não foi possível encontrar a internação " + dto.getInternacaoId() + " em nossos registros."));
            exigirMesmoPaciente(i.getPaciente(), paciente, "A internação");
            if (dto.getMotivo() != MotivoTransporte.ALTA_HOSPITALAR) {
                throw new ResourceBadRequestException("A internação se liga ao transporte da alta hospitalar.");
            }
            if (i.getStatus() != StatusInternacao.ALTA && i.getPrevisaoAlta() == null) {
                throw new ResourceUnprocessableEntityException("A internação ainda não tem alta nem previsão de alta.");
            }
            s.setInternacao(i);
            if (embarque == null) {
                embarque = i.getUnidade().getNome() + enderecoSufixo(i.getUnidade().getEndereco());
            }
        }
        if (s.getDataHora() == null) {
            throw new ResourceBadRequestException("Informe a data e a hora do compromisso.");
        }
        if (s.getDataHora().toLocalDate().isBefore(LocalDate.now(ZONA))) {
            throw new ResourceUnprocessableEntityException("A data do compromisso já passou.");
        }
        if (embarque == null) {
            embarque = endereco(paciente.getEndereco());
        }
        if (embarque == null) {
            throw new ResourceBadRequestException("Informe o endereço de embarque; o paciente está sem endereço no cadastro.");
        }
        s.setEnderecoEmbarque(embarque);
        String destino = texto(dto.getDestino());
        if (destino == null && s.getDestinoUnidade() != null) {
            destino = s.getDestinoUnidade().getNome() + enderecoSufixo(s.getDestinoUnidade().getEndereco());
        }
        if (destino == null && s.getMotivo() == MotivoTransporte.ALTA_HOSPITALAR) {
            destino = endereco(paciente.getEndereco());
        }
        if (destino == null) {
            throw new ResourceBadRequestException("Informe o destino.");
        }
        s.setDestino(destino);
        String municipio = texto(dto.getDestinoMunicipio());
        if (municipio == null && s.getDestinoUnidade() != null) {
            municipio = texto(s.getDestinoUnidade().getMunicipio());
            if (municipio == null && s.getDestinoUnidade().getEndereco() != null) {
                municipio = texto(s.getDestinoUnidade().getEndereco().getCidade());
            }
        }
        s.setDestinoMunicipio(municipio);
        s.setMaca(Boolean.TRUE.equals(dto.getMaca()));
        s.setCadeiraDeRodas(Boolean.TRUE.equals(dto.getCadeiraDeRodas()));
        s.setOxigenio(Boolean.TRUE.equals(dto.getOxigenio()));
        s.setAcompanhanteNome(texto(dto.getAcompanhanteNome()));
        s.setAcompanhanteDocumento(texto(dto.getAcompanhanteDocumento()));
        if (s.getAcompanhanteNome() == null) {
            boolean menor = paciente.getDataNascimento() != null
                    && Period.between(paciente.getDataNascimento(), s.getDataHora().toLocalDate()).getYears() < 18;
            if (menor) {
                throw new ResourceBadRequestException("O paciente é menor de 18 anos: informe o acompanhante.");
            }
            if (s.isMaca()) {
                throw new ResourceBadRequestException("Quem vai de maca precisa de acompanhante.");
            }
        }
        s.setObservacao(texto(dto.getObservacao()));
        s.setSituacao(SituacaoSolicitacaoTransporte.SOLICITADA);
        if (s.getMotivo() == MotivoTransporte.TFD) {
            exigirLaudoTfd(s, dto, origem, paciente);
            s.setSituacao(SituacaoSolicitacaoTransporte.AGUARDANDO_AUTORIZACAO);
        }
        s.setSolicitadoEm(Instant.now());
        s.setSolicitadoPorCpf(UsuarioAutenticado.cpf());
        ContextoAuditoria.paciente(paciente.getUuid());
        ContextoAuditoria.unidade(origem);
        return s;
    }

    /** TFD (Portaria SAS 55/1999): laudo com CID, justificativa e quem pede, para outro município. */
    private void exigirLaudoTfd(SolicitacaoTransporte s, SolicitacaoTransporteRequestDto dto, UnidadeDeSaude origem, Paciente paciente) {
        String cid = texto(dto.getCid());
        String justificativa = texto(dto.getJustificativa());
        if (cid == null || justificativa == null || texto(dto.getProfissionalMatricula()) == null) {
            throw new ResourceBadRequestException("O TFD pede o laudo: CID, justificativa e a matrícula de quem pede.");
        }
        if (s.getDestinoMunicipio() == null) {
            throw new ResourceBadRequestException("Informe o município de destino do TFD.");
        }
        String municipioOrigem = texto(origem.getMunicipio());
        if (municipioOrigem == null && origem.getEndereco() != null) {
            municipioOrigem = texto(origem.getEndereco().getCidade());
        }
        if (municipioOrigem == null && paciente.getEndereco() != null) {
            municipioOrigem = texto(paciente.getEndereco().getCidade());
        }
        if (municipioOrigem != null && normal(municipioOrigem).equals(normal(s.getDestinoMunicipio()))) {
            throw new ResourceUnprocessableEntityException("O TFD é para outro município; " + s.getDestinoMunicipio() + " é o mesmo da origem.");
        }
        Profissional p = profissionalRepository.findByMatricula(dto.getProfissionalMatricula().trim()).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar um profissional com a matrícula " + dto.getProfissionalMatricula().trim() + " em nossos registros."));
        s.setCid(cid.toUpperCase(Locale.ROOT));
        s.setJustificativa(justificativa);
        s.setProfissional(p);
    }

    private void exigirSemDuplicada(SolicitacaoTransporte s) {
        boolean existe = solicitacaoRepository.findByPaciente_UuidAndDataHora(s.getPaciente().getUuid(), s.getDataHora()).stream()
                .anyMatch(x -> x.getSituacao() != SituacaoSolicitacaoTransporte.CANCELADA && x.getSituacao() != SituacaoSolicitacaoTransporte.NEGADA);
        if (existe) {
            throw new ResourceConflictException("O paciente já tem transporte pedido para " + s.getDataHora().format(HORA) + ".");
        }
    }

    private SolicitacaoTransporte aguardandoComissao(UUID uuid) {
        SolicitacaoTransporte s = travarSolicitacao(uuid);
        controleDeAcesso.exigir(TFD_AUTORIZAR, s.getUnidade());
        if (s.getSituacao() != SituacaoSolicitacaoTransporte.AGUARDANDO_AUTORIZACAO) {
            throw new ResourceUnprocessableEntityException("A solicitação não está aguardando a comissão de TFD (" + s.getSituacao() + ").");
        }
        ContextoAuditoria.paciente(s.getPaciente().getUuid());
        ContextoAuditoria.registro(s.getUuid());
        return s;
    }

    private void decidir(SolicitacaoTransporte s, SituacaoSolicitacaoTransporte situacao, String motivo) {
        s.setSituacao(situacao);
        s.setMotivoSituacao(motivo);
        s.setDecididoEm(Instant.now());
        s.setDecididoPorCpf(UsuarioAutenticado.cpf());
        solicitacaoRepository.save(s);
    }

    private static void exigirMesmoPaciente(Paciente do_, Paciente paciente, String oQue) {
        if (do_ == null || !do_.getUuid().equals(paciente.getUuid())) {
            throw new ResourceUnprocessableEntityException(oQue + " é de outro paciente.");
        }
    }

    private static SolicitacaoTransporte copia(SolicitacaoTransporte m, LocalDateTime quando) {
        SolicitacaoTransporte s = new SolicitacaoTransporte();
        s.setPaciente(m.getPaciente());
        s.setUnidade(m.getUnidade());
        s.setMotivo(m.getMotivo());
        s.setTrecho(m.getTrecho());
        s.setDataHora(quando);
        s.setEnderecoEmbarque(m.getEnderecoEmbarque());
        s.setDestinoUnidade(m.getDestinoUnidade());
        s.setDestino(m.getDestino());
        s.setDestinoMunicipio(m.getDestinoMunicipio());
        s.setMaca(m.isMaca());
        s.setCadeiraDeRodas(m.isCadeiraDeRodas());
        s.setOxigenio(m.isOxigenio());
        s.setAcompanhanteNome(m.getAcompanhanteNome());
        s.setAcompanhanteDocumento(m.getAcompanhanteDocumento());
        s.setObservacao(m.getObservacao());
        s.setSituacao(m.getSituacao());
        s.setSolicitadoEm(m.getSolicitadoEm());
        s.setSolicitadoPorCpf(m.getSolicitadoPorCpf());
        return s;
    }

    // ── Regras da viagem ──────────────────────────────────────────────────────────────────────────

    /** As solicitações que entram: na fila (o TFD autorizado), sem repetir e com o compromisso dentro da janela da viagem. */
    private List<SolicitacaoTransporte> solicitacoesParaEmbarque(List<UUID> ids, ViagemTransporte viagem, Set<UUID> ignorar) {
        Set<UUID> vistos = new HashSet<>();
        List<SolicitacaoTransporte> lista = new ArrayList<>();
        for (UUID id : ids) {
            if (ignorar.contains(id)) {
                continue;
            }
            if (!vistos.add(id)) {
                throw new ResourceBadRequestException("A solicitação " + id + " aparece duas vezes.");
            }
            SolicitacaoTransporte s = travarSolicitacao(id);
            String quem = s.getPaciente().getNome();
            switch (s.getSituacao()) {
                case SOLICITADA, AUTORIZADA -> {
                }
                case PROGRAMADA -> throw new ResourceConflictException("O transporte de " + quem + " já está noutra viagem.");
                case AGUARDANDO_AUTORIZACAO -> throw new ResourceUnprocessableEntityException("O TFD de " + quem + " ainda não foi autorizado.");
                default -> throw new ResourceUnprocessableEntityException("O transporte de " + quem + " está " + s.getSituacao() + ".");
            }
            Instant compromisso = s.getDataHora().atZone(ZONA).toInstant();
            if (compromisso.isBefore(viagem.getSaidaPrevista()) || compromisso.isAfter(viagem.getRetornoPrevisto())) {
                throw new ResourceUnprocessableEntityException("O compromisso de " + quem + " (" + s.getDataHora().format(HORA)
                        + ") fica fora da janela da viagem.");
            }
            lista.add(s);
        }
        return lista;
    }

    /** Lugares (paciente e acompanhante), maca e cadeira de rodas. */
    private static void exigirCompatibilidade(Veiculo v, List<SolicitacaoTransporte> todas) {
        int lugares = todas.stream().mapToInt(SolicitacaoTransporte::lugares).sum();
        if (v.getLugares() != null && lugares > v.getLugares()) {
            throw new ResourceUnprocessableEntityException("O veículo " + v.getPrefixo() + " tem " + v.getLugares() + " lugares; os embarques pedem "
                    + lugares + " (pacientes e acompanhantes).");
        }
        if (!v.isMaca() && todas.stream().anyMatch(SolicitacaoTransporte::isMaca)) {
            throw new ResourceUnprocessableEntityException("O veículo " + v.getPrefixo() + " não leva maca.");
        }
        if (!v.isCadeiraDeRodas() && todas.stream().anyMatch(SolicitacaoTransporte::isCadeiraDeRodas)) {
            throw new ResourceUnprocessableEntityException("O veículo " + v.getPrefixo() + " não tem acesso para cadeira de rodas.");
        }
    }

    /** O veículo ou o condutor noutra viagem aberta que cruza a janela: 409. */
    private void exigirSemConflito(ViagemTransporte viagem) {
        for (ViagemTransporte x : viagemRepository.findNoPeriodo(viagem.getSaidaPrevista(), viagem.getRetornoPrevisto())) {
            if (!x.getSituacao().aberta() || x.getUuid().equals(viagem.getUuid())) {
                continue;
            }
            if (x.getVeiculo().getUuid().equals(viagem.getVeiculo().getUuid())) {
                throw new ResourceConflictException("O veículo " + viagem.getVeiculo().getPrefixo() + " já tem viagem nesse horário.");
            }
            if (x.getCondutor().getUuid().equals(viagem.getCondutor().getUuid())) {
                throw new ResourceConflictException(viagem.getCondutor().getNome() + " já conduz outra viagem nesse horário.");
            }
        }
    }

    private Profissional condutor(String matricula) {
        String m = matricula.trim();
        Profissional p = profissionalRepository.findByMatricula(m).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar um profissional com a matrícula " + m + " em nossos registros."));
        if (!p.isAtivo()) {
            throw new ResourceUnprocessableEntityException(p.getNome() + " está inativo.");
        }
        return p;
    }

    private static void voltarParaFila(SolicitacaoTransporte s) {
        s.setSituacao(s.getMotivo() == MotivoTransporte.TFD ? SituacaoSolicitacaoTransporte.AUTORIZADA : SituacaoSolicitacaoTransporte.SOLICITADA);
        s.setMotivoSituacao(null);
    }

    private static void exigirSituacao(ViagemTransporte viagem, SituacaoViagem esperada, String mensagem) {
        if (viagem.getSituacao() != esperada) {
            throw new ResourceUnprocessableEntityException(mensagem + " A viagem está " + viagem.getSituacao() + ".");
        }
    }

    private static String destinoResumo(List<SolicitacaoTransporte> lista) {
        String d = lista.stream().map(SolicitacaoTransporte::getDestino).distinct().collect(Collectors.joining("; "));
        return d.length() > 500 ? d.substring(0, 497) + "..." : d;
    }

    // ── Apoio ─────────────────────────────────────────────────────────────────────────────────────

    /** O laudo do TFD aparece para quem pede ou autoriza; a frota vê a logística. */
    private boolean verLaudo() {
        return controleDeAcesso.temAlguma(SOLICITAR, TFD_AUTORIZAR);
    }

    private Map<UUID, UUID> viagensDe(List<SolicitacaoTransporte> lista) {
        Map<UUID, UUID> mapa = new HashMap<>();
        for (SolicitacaoTransporte s : lista) {
            if (s.getSituacao() == SituacaoSolicitacaoTransporte.PROGRAMADA || s.getSituacao() == SituacaoSolicitacaoTransporte.REALIZADA
                    || s.getSituacao() == SituacaoSolicitacaoTransporte.FALTOU) {
                viagemRepository.findDaSolicitacao(s.getUuid()).stream().filter(v -> v.getSituacao() != SituacaoViagem.CANCELADA).findFirst()
                        .ifPresent(v -> mapa.put(s.getUuid(), v.getUuid()));
            }
        }
        return mapa;
    }

    private TransporteEletivoDtos.Solicitacao dto(SolicitacaoTransporte s, boolean laudo, UUID viagemId) {
        Paciente p = s.getPaciente();
        return new TransporteEletivoDtos.Solicitacao(s.getUuid(), p.getUuid(), p.getNome(), p.getCartaoSus(), s.getUnidade().getUuid(),
                s.getUnidade().getNome(), s.getMotivo(), s.getAgendamento() != null ? s.getAgendamento().getUuid() : null,
                s.getSolicitacaoRegulacao() != null ? s.getSolicitacaoRegulacao().getUuid() : null,
                s.getInternacao() != null ? s.getInternacao().getUuid() : null, s.getDataHora(), s.getEnderecoEmbarque(),
                s.getDestinoUnidade() != null ? s.getDestinoUnidade().getUuid() : null, s.getDestino(), s.getDestinoMunicipio(), s.getTrecho(),
                s.isMaca(), s.isCadeiraDeRodas(), s.isOxigenio(), s.getAcompanhanteNome(), s.getAcompanhanteDocumento(), s.lugares(),
                laudo ? s.getCid() : null, laudo ? s.getJustificativa() : null, s.getProfissional() != null ? s.getProfissional().getNome() : null,
                s.getSituacao(), s.getMotivoSituacao(), s.getRecorrenciaId(), s.getObservacao(), s.getSolicitadoEm(), viagemId);
    }

    private TransporteEletivoDtos.Viagem dto(ViagemTransporte x) {
        Veiculo v = x.getVeiculo();
        List<TransporteEletivoDtos.Embarque> embarques = x.getEmbarques().stream()
                .sorted(Comparator.comparing(e -> e.getSolicitacao().getDataHora()))
                .map(e -> {
                    SolicitacaoTransporte s = e.getSolicitacao();
                    return new TransporteEletivoDtos.Embarque(s.getUuid(), s.getPaciente().getUuid(), s.getPaciente().getNome(), s.getMotivo(),
                            s.getDataHora(), s.getEnderecoEmbarque(), s.getDestino(), s.getTrecho(), s.isMaca(), s.isCadeiraDeRodas(), s.isOxigenio(),
                            s.getAcompanhanteNome(), e.getSituacao());
                }).toList();
        int ocupados = x.getEmbarques().stream().mapToInt(e -> e.getSolicitacao().lugares()).sum();
        Integer km = x.getKmSaida() != null && x.getKmChegada() != null ? x.getKmChegada() - x.getKmSaida() : null;
        return new TransporteEletivoDtos.Viagem(x.getUuid(), v.getUuid(), v.getPrefixo(), v.getTipo(), v.getLugares(), v.getUnidade().getUuid(),
                v.getUnidade().getNome(), x.getCondutor().getUuid(), x.getCondutor().getNome(), x.getSaidaPrevista(), x.getRetornoPrevisto(),
                x.getDestino(), x.getSituacao(), ocupados, x.getKmSaida(), x.getKmChegada(), km, x.getSaidaEm(), x.getChegadaEm(),
                x.getMotivoCancelamento(), x.getObservacao(), embarques);
    }

    private SolicitacaoTransporte travarSolicitacao(UUID uuid) {
        return solicitacaoRepository.findByIdParaAtualizar(uuid).orElseThrow(() -> solicitacaoNaoEncontrada(uuid));
    }

    private ViagemTransporte travarViagem(UUID uuid) {
        return viagemRepository.findByIdParaAtualizar(uuid).orElseThrow(() -> viagemNaoEncontrada(uuid));
    }

    private UnidadeDeSaude unidade(UUID id) {
        return unidadeRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar uma unidade de saúde com o id " + id + " em nossos registros."));
    }

    private static ResourceNotFoundException solicitacaoNaoEncontrada(UUID uuid) {
        return new ResourceNotFoundException("Não foi possível encontrar a solicitação de transporte " + uuid + " em nossos registros.");
    }

    private static ResourceNotFoundException viagemNaoEncontrada(UUID uuid) {
        return new ResourceNotFoundException("Não foi possível encontrar a viagem " + uuid + " em nossos registros.");
    }

    private static String endereco(Endereco e) {
        if (e == null || texto(e.getLogradouro()) == null) {
            return null;
        }
        StringBuilder b = new StringBuilder(e.getLogradouro().trim());
        if (texto(e.getNumeroLogradouro()) != null) b.append(", ").append(e.getNumeroLogradouro().trim());
        if (texto(e.getComplemento()) != null) b.append(" - ").append(e.getComplemento().trim());
        if (texto(e.getBairro()) != null) b.append(" - ").append(e.getBairro().trim());
        if (texto(e.getCidade()) != null) b.append(" - ").append(e.getCidade().trim());
        if (texto(e.getEstado()) != null) b.append("/").append(e.getEstado().trim());
        String s = b.toString();
        return s.length() > 500 ? s.substring(0, 500) : s;
    }

    private static String enderecoSufixo(Endereco e) {
        String s = endereco(e);
        return s == null ? "" : " (" + (s.length() > 300 ? s.substring(0, 300) : s) + ")";
    }

    private static String normal(String s) {
        return Normalizer.normalize(s, Normalizer.Form.NFD).replaceAll("\\p{M}", "").trim().toLowerCase(Locale.ROOT);
    }

    private static String texto(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }
}
