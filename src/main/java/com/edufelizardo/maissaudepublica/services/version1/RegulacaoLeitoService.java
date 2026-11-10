package com.edufelizardo.maissaudepublica.services.version1;

import com.edufelizardo.maissaudepublica.config.ContextoAuditoria;
import com.edufelizardo.maissaudepublica.config.UsuarioAutenticado;
import com.edufelizardo.maissaudepublica.exceptions.ResourceBadRequestException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceConflictException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceForbiddenException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceNotFoundException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceUnprocessableEntityException;
import com.edufelizardo.maissaudepublica.models.Atendimento;
import com.edufelizardo.maissaudepublica.models.EventoSolicitacaoLeito;
import com.edufelizardo.maissaudepublica.models.Internacao;
import com.edufelizardo.maissaudepublica.models.Leito;
import com.edufelizardo.maissaudepublica.models.Paciente;
import com.edufelizardo.maissaudepublica.models.Profissional;
import com.edufelizardo.maissaudepublica.models.SolicitacaoLeito;
import com.edufelizardo.maissaudepublica.models.UnidadeDeSaude;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.AdmissaoRegulacaoLeitoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.InternacaoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.MotivoSolicitacaoLeitoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.ReservaLeitoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.SolicitacaoLeitoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.SolicitacaoLeitoDtos;
import com.edufelizardo.maissaudepublica.models.enuns.CaraterInternacao;
import com.edufelizardo.maissaudepublica.models.enuns.SexoLeito;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoLeito;
import com.edufelizardo.maissaudepublica.models.enuns.StatusInternacao;
import com.edufelizardo.maissaudepublica.models.enuns.StatusSolicitacaoLeito;
import com.edufelizardo.maissaudepublica.models.enuns.TipoEventoSolicitacaoLeito;
import com.edufelizardo.maissaudepublica.repositories.AtendimentoRepository;
import com.edufelizardo.maissaudepublica.repositories.EventoSolicitacaoLeitoRepository;
import com.edufelizardo.maissaudepublica.repositories.InternacaoRepository;
import com.edufelizardo.maissaudepublica.repositories.LeitoRepository;
import com.edufelizardo.maissaudepublica.repositories.PacienteRepository;
import com.edufelizardo.maissaudepublica.repositories.ProfissionalRepository;
import com.edufelizardo.maissaudepublica.repositories.SolicitacaoLeitoRepository;
import com.edufelizardo.maissaudepublica.repositories.UnidadeDeSaudeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

/**
 * Central de regulação de leitos entre unidades (ADR-0184): o pedido de leito da origem, a fila por prioridade, as vagas
 * compatíveis de outras unidades, a reserva do leito com prazo (ou a vaga zero, com justificativa) e a admissão ou a recusa no
 * destino. A reserva vencida volta o pedido para a fila e o leito para livre, sem rotina: quem consulta ou age vê o vencimento.
 */
@Service
public class RegulacaoLeitoService {

    public static final String SOLICITAR = "INTERNACAO.SOLICITAR_VAGA";
    public static final String REGULAR = "LEITO.REGULAR";
    static final int HORAS_PADRAO = 6;
    private static final Set<StatusSolicitacaoLeito> EM_ANDAMENTO = Set.of(StatusSolicitacaoLeito.SOLICITADA, StatusSolicitacaoLeito.RESERVADA);
    private static final Comparator<SolicitacaoLeito> ORDEM_DA_FILA = Comparator
            .comparing(SolicitacaoLeito::getPrioridade).thenComparing(SolicitacaoLeito::getSolicitadaEm);

    @Autowired
    private SolicitacaoLeitoRepository solicitacaoRepository;

    @Autowired
    private EventoSolicitacaoLeitoRepository eventoRepository;

    @Autowired
    private LeitoRepository leitoRepository;

    @Autowired
    private LeitoService leitoService;

    @Autowired
    private InternacaoService internacaoService;

    @Autowired
    private InternacaoRepository internacaoRepository;

    @Autowired
    private PacienteRepository pacienteRepository;

    @Autowired
    private AtendimentoRepository atendimentoRepository;

    @Autowired
    private UnidadeDeSaudeRepository unidadeRepository;

    @Autowired
    private ProfissionalRepository profissionalRepository;

    @Autowired
    private ControleDeAcesso controleDeAcesso;

    // ── Pedido ─────────────────────────────────────────────────────────────────────────────────────

    /** A origem pede um leito: um pedido em andamento por paciente (409). */
    @Transactional
    public SolicitacaoLeitoDtos.Solicitacao solicitar(SolicitacaoLeitoRequestDto dto) {
        UnidadeDeSaude origem = unidade(dto.getUnidadeOrigemId());
        controleDeAcesso.exigir(SOLICITAR, origem);
        Paciente paciente = pacienteRepository.findById(dto.getPacienteId()).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar um paciente com o id " + dto.getPacienteId() + " em nossos registros."));
        ContextoAuditoria.paciente(paciente.getUuid());
        if (!paciente.isAtivo()) {
            throw new ResourceUnprocessableEntityException("O paciente está inativo; reative o cadastro antes de pedir leito.");
        }
        if (solicitacaoRepository.existsByPaciente_UuidAndStatusIn(paciente.getUuid(), EM_ANDAMENTO)) {
            throw new ResourceConflictException("O paciente já tem um pedido de leito em andamento.");
        }
        Internacao naOrigem = null;
        if (dto.getInternacaoOrigemId() != null) {
            naOrigem = internacaoRepository.findById(dto.getInternacaoOrigemId()).orElseThrow(() -> new ResourceNotFoundException(
                    "Não foi possível encontrar uma internação com o id " + dto.getInternacaoOrigemId() + " em nossos registros."));
            if (!naOrigem.getPaciente().getUuid().equals(paciente.getUuid()) || !naOrigem.getUnidade().getUuid().equals(origem.getUuid())) {
                throw new ResourceBadRequestException("A internação informada é de outro paciente ou de outra unidade.");
            }
            if (naOrigem.getStatus() != StatusInternacao.INTERNADO) {
                throw new ResourceUnprocessableEntityException("A internação informada já foi encerrada.");
            }
        }
        Atendimento atendimento = null;
        if (dto.getAtendimentoId() != null) {
            atendimento = atendimentoRepository.findById(dto.getAtendimentoId()).orElseThrow(() -> new ResourceNotFoundException(
                    "Não foi possível encontrar um atendimento com o id " + dto.getAtendimentoId() + " em nossos registros."));
            if (!atendimento.getPaciente().getUuid().equals(paciente.getUuid())) {
                throw new ResourceBadRequestException("O atendimento informado é de outro paciente.");
            }
        }
        Profissional quem = profissional(dto.getProfissionalMatricula());
        SolicitacaoLeito s = new SolicitacaoLeito();
        s.setPaciente(paciente);
        s.setUnidadeOrigem(origem);
        s.setInternacaoOrigem(naOrigem);
        s.setAtendimento(atendimento);
        s.setOcorrenciaUrgenciaId(dto.getOcorrenciaUrgenciaId());
        s.setSolicitadoPor(quem);
        s.setCid(dto.getCid().trim().toUpperCase(Locale.ROOT).replace(".", ""));
        s.setQuadroClinico(dto.getQuadroClinico().trim());
        s.setTipoLeito(dto.getTipoLeito());
        s.setPrioridade(dto.getPrioridade());
        s.setStatus(StatusSolicitacaoLeito.SOLICITADA);
        s.setSolicitadaEm(Instant.now());
        s.setRegistradoPorCpf(UsuarioAutenticado.cpf());
        s = solicitacaoRepository.save(s);
        registrar(s, TipoEventoSolicitacaoLeito.SOLICITACAO, dto.getTipoLeito() + " · " + dto.getPrioridade() + " · CID " + s.getCid(), quem);
        return detalhe(s);
    }

    /** A origem desiste, até a admissão, com motivo. A reserva, se houver, é desfeita. */
    @Transactional
    public SolicitacaoLeitoDtos.Solicitacao cancelar(UUID uuid, MotivoSolicitacaoLeitoRequestDto dto) {
        SolicitacaoLeito s = travar(uuid);
        controleDeAcesso.exigir(SOLICITAR, s.getUnidadeOrigem());
        exigirEmAndamento(s, "cancelar");
        Profissional quem = profissional(dto.getProfissionalMatricula());
        desfazerReserva(s);
        s.setStatus(StatusSolicitacaoLeito.CANCELADA);
        s.setMotivoEncerramento(dto.getMotivo().trim());
        registrar(s, TipoEventoSolicitacaoLeito.CANCELAMENTO, dto.getMotivo().trim(), quem);
        return detalhe(solicitacaoRepository.save(s));
    }

    // ── Central ────────────────────────────────────────────────────────────────────────────────────

    /**
     * Os pedidos em andamento das origens no escopo do regulador, com a posição na fila (só os que aguardam): prioridade e depois a
     * hora do pedido. A reserva vencida aparece de volta na fila.
     */
    @Transactional(readOnly = true)
    public List<SolicitacaoLeitoDtos.Solicitacao> fila() {
        Instant agora = Instant.now();
        List<SolicitacaoLeito> todas = solicitacaoRepository.findByStatusInOrderBySolicitadaEmAsc(EM_ANDAMENTO);
        List<SolicitacaoLeito> visiveis = controleDeAcesso.filtrar(todas.stream(), s -> Stream.of(s.getUnidadeOrigem()), REGULAR)
                .sorted(Comparator.comparing((SolicitacaoLeito s) -> aguardando(s, agora) ? 0 : 1).thenComparing(ORDEM_DA_FILA)).toList();
        int[] posicao = {0};
        return visiveis.stream().map(s -> resumo(s, aguardando(s, agora) ? ++posicao[0] : null, agora)).toList();
    }

    /** Os leitos livres de outras unidades que servem ao pedido: tipo, sexo da enfermaria e faixa etária. */
    @Transactional(readOnly = true)
    public List<SolicitacaoLeitoDtos.Vaga> vagas(UUID uuid) {
        SolicitacaoLeito s = buscar(uuid);
        controleDeAcesso.exigir(REGULAR, s.getUnidadeOrigem());
        Instant agora = Instant.now();
        return controleDeAcesso.filtrar(leitoRepository.findAllByOrderByIdentificacaoAsc().stream(), l -> Stream.of(l.getUnidade()), REGULAR)
                .filter(l -> compativel(l, s, agora))
                .sorted(Comparator.comparing((Leito l) -> l.getUnidade().getNome()).thenComparing(Leito::getIdentificacao))
                .map(SolicitacaoLeitoDtos.Vaga::from).toList();
    }

    /**
     * Reserva um leito livre compatível de outra unidade, por um prazo (padrão 6 horas). Sem leito, é vaga zero: unidade de destino
     * e justificativa obrigatórias (400).
     */
    @Transactional
    public SolicitacaoLeitoDtos.Solicitacao reservar(UUID uuid, ReservaLeitoRequestDto dto) {
        SolicitacaoLeito s = travar(uuid);
        controleDeAcesso.exigir(REGULAR, s.getUnidadeOrigem());
        Instant agora = Instant.now();
        expirarSeVencida(s, agora);
        if (s.getStatus() != StatusSolicitacaoLeito.SOLICITADA) {
            throw new ResourceUnprocessableEntityException("Só pedido aguardando vaga pode ser reservado; este está " + s.getStatus() + ".");
        }
        Profissional regulador = profissional(dto.getProfissionalMatricula());
        String transporte = texto(dto.getTransporte());
        if (dto.getLeitoId() == null) {
            if (dto.getUnidadeDestinoId() == null || texto(dto.getJustificativaVagaZero()) == null) {
                throw new ResourceBadRequestException("Sem leito livre, é vaga zero: informe a unidade de destino e a justificativa.");
            }
            UnidadeDeSaude destino = unidade(dto.getUnidadeDestinoId());
            controleDeAcesso.exigir(REGULAR, destino);
            exigirOutraUnidade(s, destino);
            s.setStatus(StatusSolicitacaoLeito.RESERVADA);
            s.setVagaZero(true);
            s.setUnidadeDestino(destino);
            s.setJustificativaVagaZero(dto.getJustificativaVagaZero().trim());
            s.setTransporte(transporte);
            registrar(s, TipoEventoSolicitacaoLeito.VAGA_ZERO, "Para " + destino.getNome() + ": " + s.getJustificativaVagaZero(), regulador);
            return detalhe(solicitacaoRepository.save(s));
        }
        Leito leito = leitoService.travar(dto.getLeitoId());
        controleDeAcesso.exigir(REGULAR, leito.getUnidade());
        leitoService.liberarReservaVencida(leito);
        exigirOutraUnidade(s, leito.getUnidade());
        if (!compativel(leito, s, agora)) {
            throw new ResourceUnprocessableEntityException("O leito " + leito.getIdentificacao() + " não serve ao pedido: precisa estar livre e ser "
                    + s.getTipoLeito() + ", com sexo da enfermaria e faixa etária compatíveis com o paciente.");
        }
        int horas = dto.getHoras() != null ? dto.getHoras() : HORAS_PADRAO;
        Instant ate = agora.plus(Duration.ofHours(horas));
        leito.setSituacao(SituacaoLeito.RESERVADO);
        leito.setReservadoAte(ate);
        leito.setReservaSolicitacaoId(s.getUuid());
        leitoRepository.save(leito);
        s.setStatus(StatusSolicitacaoLeito.RESERVADA);
        s.setVagaZero(false);
        s.setLeitoReservado(leito);
        s.setUnidadeDestino(leito.getUnidade());
        s.setReservadoAte(ate);
        s.setTransporte(transporte);
        registrar(s, TipoEventoSolicitacaoLeito.RESERVA, "Leito " + leito.getIdentificacao() + " em " + leito.getUnidade().getNome() + " por "
                + horas + "h" + (transporte != null ? " · " + transporte : ""), regulador);
        return detalhe(solicitacaoRepository.save(s));
    }

    /** O regulador nega o pedido, com motivo, enquanto aguarda vaga. */
    @Transactional
    public SolicitacaoLeitoDtos.Solicitacao negar(UUID uuid, MotivoSolicitacaoLeitoRequestDto dto) {
        SolicitacaoLeito s = travar(uuid);
        controleDeAcesso.exigir(REGULAR, s.getUnidadeOrigem());
        expirarSeVencida(s, Instant.now());
        if (s.getStatus() != StatusSolicitacaoLeito.SOLICITADA) {
            throw new ResourceUnprocessableEntityException("Só pedido aguardando vaga pode ser negado; desfaça a reserva antes.");
        }
        Profissional quem = profissional(dto.getProfissionalMatricula());
        s.setStatus(StatusSolicitacaoLeito.NEGADA);
        s.setMotivoEncerramento(dto.getMotivo().trim());
        registrar(s, TipoEventoSolicitacaoLeito.NEGACAO, dto.getMotivo().trim(), quem);
        return detalhe(solicitacaoRepository.save(s));
    }

    // ── Destino ────────────────────────────────────────────────────────────────────────────────────

    /** Os pedidos com reserva válida (ou vaga zero) para as unidades do escopo de quem interna. */
    @Transactional(readOnly = true)
    public List<SolicitacaoLeitoDtos.Solicitacao> aReceber() {
        Instant agora = Instant.now();
        return controleDeAcesso.filtrar(solicitacaoRepository.findByStatusInOrderBySolicitadaEmAsc(Set.of(StatusSolicitacaoLeito.RESERVADA)).stream()
                                .filter(s -> !s.reservaVencida(agora)),
                        s -> Stream.of(s.getUnidadeDestino()), InternacaoService.GERENCIAR)
                .sorted(ORDEM_DA_FILA)
                .map(s -> resumo(s, null, agora)).toList();
    }

    /**
     * O destino admite: cria a internação no leito reservado (na vaga zero, num leito livre da unidade). Se o paciente estava
     * internado na origem, ela fecha com alta por transferência. Reserva vencida: 422.
     */
    @Transactional
    public SolicitacaoLeitoDtos.Solicitacao admitir(UUID uuid, AdmissaoRegulacaoLeitoRequestDto dto) {
        SolicitacaoLeito s = travar(uuid);
        controleDeAcesso.exigir(InternacaoService.GERENCIAR, s.getUnidadeDestino());
        Instant agora = Instant.now();
        if (s.getStatus() != StatusSolicitacaoLeito.RESERVADA || s.reservaVencida(agora)) {
            expirarSeVencida(s, agora);
            throw new ResourceUnprocessableEntityException("O pedido não tem reserva válida para admitir; peça nova reserva à central.");
        }
        Leito leito;
        if (s.isVagaZero()) {
            if (dto.getLeitoId() == null) {
                throw new ResourceBadRequestException("Na vaga zero, informe o leito livre da unidade onde o paciente fica.");
            }
            leito = leitoService.travar(dto.getLeitoId());
            if (!leito.getUnidade().getUuid().equals(s.getUnidadeDestino().getUuid())) {
                throw new ResourceUnprocessableEntityException("O leito precisa ser da unidade de destino.");
            }
            leitoService.liberarReservaVencida(leito);
        } else {
            leito = leitoService.travar(s.getLeitoReservado().getUuid());
        }
        InternacaoRequestDto internacao = new InternacaoRequestDto();
        internacao.setPacienteId(s.getPaciente().getUuid());
        internacao.setLeitoId(leito.getUuid());
        internacao.setMedicoMatricula(dto.getMedicoMatricula());
        internacao.setCid(s.getCid());
        internacao.setMotivo("Regulada pela central de leitos: " + s.getQuadroClinico());
        internacao.setCarater(CaraterInternacao.URGENCIA);
        internacao.setPrevisaoAlta(dto.getPrevisaoAlta());
        internacao.setJustificativaFaixaEtaria(dto.getJustificativaFaixaEtaria());
        Internacao nova = internacaoService.internarRegulado(internacao, leito, s.getUuid(), s.getInternacaoOrigem());
        s.setStatus(StatusSolicitacaoLeito.ADMITIDA);
        s.setInternacaoDestino(nova);
        registrar(s, TipoEventoSolicitacaoLeito.ADMISSAO, "Leito " + leito.getIdentificacao() + " em " + leito.getUnidade().getNome(),
                nova.getMedicoResponsavel());
        return detalhe(solicitacaoRepository.save(s));
    }

    /** O destino recusa, com motivo: o pedido volta à fila na posição original e o leito volta a livre. */
    @Transactional
    public SolicitacaoLeitoDtos.Solicitacao recusar(UUID uuid, MotivoSolicitacaoLeitoRequestDto dto) {
        SolicitacaoLeito s = travar(uuid);
        controleDeAcesso.exigir(InternacaoService.GERENCIAR, s.getUnidadeDestino());
        if (s.getStatus() != StatusSolicitacaoLeito.RESERVADA) {
            throw new ResourceUnprocessableEntityException("Só pedido com reserva pode ser recusado no destino.");
        }
        Profissional quem = profissional(dto.getProfissionalMatricula());
        String destino = s.getUnidadeDestino().getNome();
        desfazerReserva(s);
        s.setStatus(StatusSolicitacaoLeito.SOLICITADA);
        registrar(s, TipoEventoSolicitacaoLeito.RECUSA_DESTINO, destino + ": " + dto.getMotivo().trim(), quem);
        return detalhe(solicitacaoRepository.save(s));
    }

    // ── Leitura ────────────────────────────────────────────────────────────────────────────────────

    /** O detalhe, com os eventos: para a origem, a central e o destino. */
    @Transactional(readOnly = true)
    public SolicitacaoLeitoDtos.Solicitacao buscarPorId(UUID uuid) {
        SolicitacaoLeito s = buscar(uuid);
        controleDeAcesso.filtrar(Stream.of(s), x -> Stream.concat(Stream.of(x.getUnidadeOrigem()), Stream.ofNullable(x.getUnidadeDestino())),
                        SOLICITAR, REGULAR, InternacaoService.GERENCIAR).findAny()
                .orElseThrow(() -> new ResourceForbiddenException("Este pedido de leito é de unidades fora do seu acesso."));
        return detalhe(s);
    }

    /** Os pedidos do paciente, do mais recente ao mais antigo, para a origem que o atende. */
    @Transactional(readOnly = true)
    public List<SolicitacaoLeitoDtos.Solicitacao> doPaciente(UUID pacienteId) {
        Instant agora = Instant.now();
        return controleDeAcesso.filtrar(solicitacaoRepository.findByPaciente_UuidOrderBySolicitadaEmDesc(pacienteId).stream(),
                        s -> Stream.concat(Stream.of(s.getUnidadeOrigem()), Stream.ofNullable(s.getUnidadeDestino())),
                        SOLICITAR, REGULAR, InternacaoService.GERENCIAR)
                .map(s -> resumo(s, null, agora)).toList();
    }

    // ── Apoio ──────────────────────────────────────────────────────────────────────────────────────

    /** Leito ativo, livre agora (ou de reserva vencida), de outra unidade, do tipo pedido, com sexo e faixa etária do paciente. */
    private boolean compativel(Leito l, SolicitacaoLeito s, Instant agora) {
        if (!l.isAtivo() || l.situacaoEm(agora) != SituacaoLeito.LIVRE || l.getUnidade().getUuid().equals(s.getUnidadeOrigem().getUuid())
                || l.getTipo() != s.getTipoLeito()) {
            return false;
        }
        if (l.getSexo() != SexoLeito.MISTO) {
            try {
                InternacaoService.exigirSexo(l, s.getPaciente());
            } catch (ResourceUnprocessableEntityException e) {
                return false;
            }
        }
        try {
            return InternacaoService.conferirFaixaEtaria(l, s.getPaciente(), null) == null;
        } catch (ResourceUnprocessableEntityException e) {
            return false;
        }
    }

    private static boolean aguardando(SolicitacaoLeito s, Instant agora) {
        return s.getStatus() == StatusSolicitacaoLeito.SOLICITADA || s.reservaVencida(agora);
    }

    /** Reserva vencida: o pedido volta para a fila e o leito para livre, com o evento. */
    private void expirarSeVencida(SolicitacaoLeito s, Instant agora) {
        if (!s.reservaVencida(agora)) {
            return;
        }
        String leito = s.getLeitoReservado() != null ? s.getLeitoReservado().getIdentificacao() : "";
        desfazerReserva(s);
        s.setStatus(StatusSolicitacaoLeito.SOLICITADA);
        registrar(s, TipoEventoSolicitacaoLeito.EXPIRACAO, "Reserva do leito " + leito + " venceu sem admissão.", null);
        solicitacaoRepository.save(s);
    }

    private void desfazerReserva(SolicitacaoLeito s) {
        if (s.getLeitoReservado() != null) {
            Leito l = leitoService.travar(s.getLeitoReservado().getUuid());
            if (l.getSituacao() == SituacaoLeito.RESERVADO && s.getUuid().equals(l.getReservaSolicitacaoId())) {
                l.setSituacao(SituacaoLeito.LIVRE);
                l.setReservadoAte(null);
                l.setReservaSolicitacaoId(null);
                leitoRepository.save(l);
            }
        }
        s.setLeitoReservado(null);
        s.setUnidadeDestino(null);
        s.setReservadoAte(null);
        s.setVagaZero(false);
        s.setJustificativaVagaZero(null);
    }

    private static void exigirOutraUnidade(SolicitacaoLeito s, UnidadeDeSaude destino) {
        if (destino.getUuid().equals(s.getUnidadeOrigem().getUuid())) {
            throw new ResourceUnprocessableEntityException("O destino precisa ser outra unidade; na própria, interne direto.");
        }
    }

    private static void exigirEmAndamento(SolicitacaoLeito s, String acao) {
        if (!EM_ANDAMENTO.contains(s.getStatus())) {
            throw new ResourceUnprocessableEntityException("Não é possível " + acao + " um pedido " + s.getStatus() + ".");
        }
    }

    private SolicitacaoLeitoDtos.Solicitacao resumo(SolicitacaoLeito s, Integer posicao, Instant agora) {
        SolicitacaoLeitoDtos.Solicitacao d = SolicitacaoLeitoDtos.Solicitacao.from(s, posicao, List.of());
        if (s.reservaVencida(agora)) {
            // A reserva venceu sem ninguém agir ainda: mostra como aguardando vaga.
            return new SolicitacaoLeitoDtos.Solicitacao(d.uuid(), d.pacienteId(), d.pacienteNome(), d.unidadeOrigemId(), d.unidadeOrigemNome(),
                    d.internacaoOrigemId(), d.atendimentoId(), d.ocorrenciaUrgenciaId(), d.solicitadoPorMatricula(), d.solicitadoPorNome(), d.cid(),
                    d.quadroClinico(), d.tipoLeito(), d.prioridade(), StatusSolicitacaoLeito.SOLICITADA, d.solicitadaEm(), posicao, null, null, null,
                    null, null, false, null, d.transporte(), d.motivoEncerramento(), d.internacaoDestinoId(), List.of());
        }
        return d;
    }

    private SolicitacaoLeitoDtos.Solicitacao detalhe(SolicitacaoLeito s) {
        ContextoAuditoria.paciente(s.getPaciente().getUuid());
        ContextoAuditoria.registro(s.getUuid());
        Integer posicao = null;
        if (aguardando(s, Instant.now())) {
            List<SolicitacaoLeito> fila = solicitacaoRepository.findByStatusInOrderBySolicitadaEmAsc(EM_ANDAMENTO).stream()
                    .filter(x -> aguardando(x, Instant.now())).sorted(ORDEM_DA_FILA).toList();
            Map<UUID, Integer> pos = new HashMap<>();
            for (int i = 0; i < fila.size(); i++) {
                pos.put(fila.get(i).getUuid(), i + 1);
            }
            posicao = pos.get(s.getUuid());
        }
        return SolicitacaoLeitoDtos.Solicitacao.from(s, posicao, eventoRepository.findBySolicitacao_UuidOrderByOcorridoEmAsc(s.getUuid()));
    }

    private void registrar(SolicitacaoLeito s, TipoEventoSolicitacaoLeito tipo, String texto, Profissional quem) {
        EventoSolicitacaoLeito e = new EventoSolicitacaoLeito();
        e.setSolicitacao(s);
        e.setTipo(tipo);
        e.setTexto(texto != null && texto.length() > 1000 ? texto.substring(0, 1000) : texto);
        e.setProfissional(quem);
        e.setOcorridoEm(Instant.now());
        e.setRegistradoPorCpf(UsuarioAutenticado.cpf());
        eventoRepository.save(e);
    }

    private SolicitacaoLeito travar(UUID uuid) {
        SolicitacaoLeito s = solicitacaoRepository.findByIdParaAtualizar(uuid).orElseThrow(() -> naoEncontrado(uuid));
        ContextoAuditoria.paciente(s.getPaciente().getUuid());
        return s;
    }

    private SolicitacaoLeito buscar(UUID uuid) {
        return solicitacaoRepository.findById(uuid).orElseThrow(() -> naoEncontrado(uuid));
    }

    private UnidadeDeSaude unidade(UUID uuid) {
        return unidadeRepository.findById(uuid).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar uma unidade de saúde com o id " + uuid + " em nossos registros."));
    }

    private Profissional profissional(String matricula) {
        return profissionalRepository.findByMatricula(matricula.trim()).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar um profissional com a matrícula " + matricula.trim() + " em nossos registros."));
    }

    private static String texto(String t) {
        return t == null || t.isBlank() ? null : t.trim();
    }

    private static ResourceNotFoundException naoEncontrado(UUID uuid) {
        return new ResourceNotFoundException("Não foi possível encontrar um pedido de leito com o id " + uuid + " em nossos registros.");
    }
}
