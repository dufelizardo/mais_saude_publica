package com.edufelizardo.maissaudepublica.services.version1;

import com.edufelizardo.maissaudepublica.config.ContextoAuditoria;
import com.edufelizardo.maissaudepublica.config.UsuarioAutenticado;
import com.edufelizardo.maissaudepublica.exceptions.ResourceBadRequestException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceConflictException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceNotFoundException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceUnprocessableEntityException;
import com.edufelizardo.maissaudepublica.models.AcaoProgramatica;
import com.edufelizardo.maissaudepublica.models.AcompanhamentoProgramatico;
import com.edufelizardo.maissaudepublica.models.Agendamento;
import com.edufelizardo.maissaudepublica.models.Atendimento;
import com.edufelizardo.maissaudepublica.models.Campanha;
import com.edufelizardo.maissaudepublica.models.Equipe;
import com.edufelizardo.maissaudepublica.models.LocalCampanha;
import com.edufelizardo.maissaudepublica.models.Paciente;
import com.edufelizardo.maissaudepublica.models.Profissional;
import com.edufelizardo.maissaudepublica.models.Programa;
import com.edufelizardo.maissaudepublica.models.UnidadeDeSaude;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.AcaoProgramaticaRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.BuscaAtivaRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.RealizacaoAcaoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.SituacaoAcaoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.AcaoProgramaticaDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.AcoesProgramaticasDto;
import com.edufelizardo.maissaudepublica.models.enuns.ResultadoBuscaAtiva;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoAcaoProgramatica;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoCampanha;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoPrograma;
import com.edufelizardo.maissaudepublica.models.enuns.StatusAtendimento;
import com.edufelizardo.maissaudepublica.models.enuns.TipoAcaoProgramatica;
import com.edufelizardo.maissaudepublica.repositories.AcaoProgramaticaRepository;
import com.edufelizardo.maissaudepublica.repositories.AcompanhamentoProgramaticoRepository;
import com.edufelizardo.maissaudepublica.repositories.AdscricaoRepository;
import com.edufelizardo.maissaudepublica.repositories.AtendimentoRepository;
import com.edufelizardo.maissaudepublica.repositories.AtribuicaoMicroareaRepository;
import com.edufelizardo.maissaudepublica.repositories.CampanhaRepository;
import com.edufelizardo.maissaudepublica.repositories.EquipeRepository;
import com.edufelizardo.maissaudepublica.repositories.LocalCampanhaRepository;
import com.edufelizardo.maissaudepublica.repositories.PacienteRepository;
import com.edufelizardo.maissaudepublica.repositories.ProgramaRepository;
import com.edufelizardo.maissaudepublica.repositories.UnidadeDeSaudeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

/**
 * Ações programáticas, fatia P4 (ADR-0132, desenho na ADR-0109): a ação prevista num programa, individual (do
 * acompanhamento) ou coletiva (com os participantes), a busca ativa do faltoso ou atrasado, a ligação com a marcação da
 * Agenda e a confirmação pelo profissional, com o atendimento que a cumpriu. A rede aponta o que está pronto para
 * confirmar, mas não confirma sozinha.
 */
@Service
public class AcaoProgramaticaService {

    private static final String ACOMPANHAR = AcompanhamentoService.ACOMPANHAR;
    /** Ações que só existem para uma pessoa. */
    private static final Set<TipoAcaoProgramatica> SO_INDIVIDUAIS = EnumSet.of(TipoAcaoProgramatica.CONSULTA_PROGRAMADA,
            TipoAcaoProgramatica.BUSCA_ATIVA, TipoAcaoProgramatica.VISITA_DOMICILIAR);

    @Autowired
    private AcaoProgramaticaRepository acaoRepository;

    @Autowired
    private AcompanhamentoProgramaticoRepository acompanhamentoRepository;

    @Autowired
    private ProgramaRepository programaRepository;

    @Autowired
    private UnidadeDeSaudeRepository unidadeDeSaudeRepository;

    @Autowired
    private EquipeRepository equipeRepository;

    @Autowired
    private PacienteRepository pacienteRepository;

    @Autowired
    private AtendimentoRepository atendimentoRepository;

    @Autowired
    private AdscricaoRepository adscricaoRepository;

    @Autowired
    private AtribuicaoMicroareaRepository atribuicaoRepository;

    @Autowired
    private AcompanhamentoService acompanhamentoService;

    @Autowired
    private CampanhaRepository campanhaRepository;

    @Autowired
    private LocalCampanhaRepository localCampanhaRepository;

    @Autowired
    private VinculoAssistencialService vinculoService;

    @Autowired
    private ControleDeAcesso controleDeAcesso;

    // ── Escrita ────────────────────────────────────────────────────────────────────────────────────

    /**
     * Prevê a ação: individual pelo acompanhamento vigente; coletiva pela unidade, com participantes acompanhados no programa;
     * ou da campanha, num local dela (ADR-0133).
     */
    @Transactional
    public AcaoProgramaticaDto prever(AcaoProgramaticaRequestDto dto) {
        if ((dto.getProgramaId() == null) == (dto.getCampanhaId() == null)) {
            throw new ResourceBadRequestException("A ação é do programa ou da campanha: informe um dos dois.");
        }
        if (dto.getCampanhaId() != null) {
            return preverDaCampanha(dto);
        }
        Programa programa = programaRepository.findById(dto.getProgramaId()).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar o programa " + dto.getProgramaId() + " em nossos registros."));
        if (programa.getSituacao() != SituacaoPrograma.ATIVO) {
            throw new ResourceUnprocessableEntityException("O programa " + programa.getNome() + " não está ativo.");
        }
        AcaoProgramatica a = new AcaoProgramatica();
        a.setPrograma(programa);
        a.setTipo(dto.getTipo());
        if (dto.getAcompanhamentoId() != null) {
            AcompanhamentoProgramatico ac = acompanhamentoRepository.findById(dto.getAcompanhamentoId()).orElseThrow(() -> new ResourceNotFoundException(
                    "Não foi possível encontrar o acompanhamento " + dto.getAcompanhamentoId() + " em nossos registros."));
            if (!ac.getPrograma().getUuid().equals(programa.getUuid())) {
                throw new ResourceUnprocessableEntityException("O acompanhamento é de outro programa (" + ac.getPrograma().getNome() + ").");
            }
            if (!ac.vigente()) {
                throw new ResourceUnprocessableEntityException("O acompanhamento já foi encerrado.");
            }
            controleDeAcesso.exigir(ACOMPANHAR, ac.getUnidade());
            ContextoAuditoria.paciente(ac.getPaciente().getUuid());
            a.setAcompanhamento(ac);
            a.setPaciente(ac.getPaciente());
            a.setUnidade(ac.getUnidade());
            a.setEquipe(ac.getEquipe());
        } else {
            if (SO_INDIVIDUAIS.contains(dto.getTipo())) {
                throw new ResourceBadRequestException("Consulta programada, visita domiciliar e busca ativa são de um paciente: informe o acompanhamento.");
            }
            if (dto.getUnidadeId() == null) {
                throw new ResourceBadRequestException("A ação coletiva pede a unidade que realiza.");
            }
            UnidadeDeSaude unidade = unidadeDeSaudeRepository.findById(dto.getUnidadeId()).orElseThrow(() -> new ResourceNotFoundException(
                    "Não foi possível encontrar uma unidade de saúde com o id " + dto.getUnidadeId() + " em nossos registros."));
            controleDeAcesso.exigir(ACOMPANHAR, unidade);
            a.setUnidade(unidade);
            a.setEquipe(equipe(dto.getEquipeId(), unidade));
            a.setLocal(texto(dto.getLocal()));
            a.setParticipantes(participantes(programa, dto.getParticipantesIds()));
        }
        a.setDescricao(texto(dto.getDescricao()));
        a.setResponsavel(acompanhamentoService.profissionalResponsavel(dto.getResponsavelMatricula(), a.getUnidade()));
        a.setDataPrevista(dto.getDataPrevista());
        a.setSituacao(SituacaoAcaoProgramatica.PENDENTE);
        a.setCriadaEm(Instant.now());
        a.setRegistradoPorCpf(UsuarioAutenticado.cpf());
        a = acaoRepository.save(a);
        ContextoAuditoria.registro(a.getUuid());
        ContextoAuditoria.detalhe("Ação " + a.getTipo() + " do programa " + programa.getCodigo() + (a.getPaciente() == null ? " (coletiva)." : "."));
        return dto(a, LocalDate.now());
    }

    /** Ação da campanha: coletiva, num local dela, no período, com a campanha não cancelada nem encerrada (ADR-0133). */
    private AcaoProgramaticaDto preverDaCampanha(AcaoProgramaticaRequestDto dto) {
        Campanha campanha = campanhaRepository.findById(dto.getCampanhaId()).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar a campanha " + dto.getCampanhaId() + " em nossos registros."));
        LocalDate hoje = LocalDate.now();
        SituacaoCampanha situacao = campanha.situacao(hoje);
        if (situacao == SituacaoCampanha.CANCELADA || situacao == SituacaoCampanha.ENCERRADA) {
            throw new ResourceUnprocessableEntityException("A campanha " + campanha.getNome() + " está " + situacao.name().toLowerCase() + ".");
        }
        if (dto.getAcompanhamentoId() != null || SO_INDIVIDUAIS.contains(dto.getTipo())) {
            throw new ResourceBadRequestException("A ação da campanha é coletiva: vacinação, exame, aferição, grupo, orientação, mobilização ou outra.");
        }
        if (dto.getLocalCampanhaId() == null) {
            throw new ResourceBadRequestException("Informe o local da campanha onde a ação acontece.");
        }
        LocalCampanha local = localCampanhaRepository.findById(dto.getLocalCampanhaId()).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar o local " + dto.getLocalCampanhaId() + " em nossos registros."));
        if (!local.getCampanha().getUuid().equals(campanha.getUuid())) {
            throw new ResourceUnprocessableEntityException("O local é de outra campanha.");
        }
        if (dto.getDataPrevista().isBefore(campanha.getInicio()) || dto.getDataPrevista().isAfter(campanha.getFim())) {
            throw new ResourceUnprocessableEntityException("A data precisa estar no período da campanha (" + campanha.getInicio() + " a "
                    + campanha.getFim() + ").");
        }
        controleDeAcesso.exigir(ACOMPANHAR, local.getUnidade());
        AcaoProgramatica a = new AcaoProgramatica();
        a.setCampanha(campanha);
        a.setLocalCampanha(local);
        a.setTipo(dto.getTipo());
        a.setUnidade(local.getUnidade());
        a.setLocal(local.isTemporario() ? local.getNome() : local.getUnidade().getNome());
        a.setDescricao(texto(dto.getDescricao()));
        a.setResponsavel(acompanhamentoService.profissionalResponsavel(dto.getResponsavelMatricula(), local.getUnidade()));
        a.setDataPrevista(dto.getDataPrevista());
        a.setSituacao(SituacaoAcaoProgramatica.PENDENTE);
        a.setCriadaEm(Instant.now());
        a.setRegistradoPorCpf(UsuarioAutenticado.cpf());
        a = acaoRepository.save(a);
        ContextoAuditoria.registro(a.getUuid());
        ContextoAuditoria.detalhe("Ação " + a.getTipo() + " da campanha " + campanha.getNome() + ".");
        return dto(a, hoje);
    }

    /**
     * Busca ativa do acompanhado (faltoso, atrasado ou não): uma pendente por acompanhamento (409). Sem responsável, o ACS
     * da microárea onde a pessoa está adscrita, se houver e for lotado na unidade.
     */
    @Transactional
    public AcaoProgramaticaDto buscaAtiva(UUID acompanhamentoId, BuscaAtivaRequestDto dto) {
        AcompanhamentoProgramatico ac = acompanhamentoRepository.findById(acompanhamentoId).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar o acompanhamento " + acompanhamentoId + " em nossos registros."));
        controleDeAcesso.exigir(ACOMPANHAR, ac.getUnidade());
        ContextoAuditoria.paciente(ac.getPaciente().getUuid());
        if (!ac.vigente()) {
            throw new ResourceUnprocessableEntityException("O acompanhamento já foi encerrado.");
        }
        if (acaoRepository.existsByAcompanhamento_UuidAndTipoAndSituacao(ac.getUuid(), TipoAcaoProgramatica.BUSCA_ATIVA, SituacaoAcaoProgramatica.PENDENTE)) {
            throw new ResourceConflictException("Já há uma busca ativa pendente para " + ac.getPaciente().getNome() + " neste programa.");
        }
        Profissional responsavel = texto(dto.getResponsavelMatricula()) != null
                ? acompanhamentoService.profissionalResponsavel(dto.getResponsavelMatricula(), ac.getUnidade())
                : acsDoPaciente(ac.getPaciente(), ac.getUnidade()).orElse(null);
        AcaoProgramatica a = new AcaoProgramatica();
        a.setPrograma(ac.getPrograma());
        a.setTipo(TipoAcaoProgramatica.BUSCA_ATIVA);
        a.setAcompanhamento(ac);
        a.setPaciente(ac.getPaciente());
        a.setUnidade(ac.getUnidade());
        a.setEquipe(ac.getEquipe());
        a.setDescricao(texto(dto.getDescricao()) != null ? texto(dto.getDescricao())
                : "Busca ativa: " + ac.getSituacao().name().toLowerCase() + " no programa " + ac.getPrograma().getNome() + ".");
        a.setResponsavel(responsavel);
        a.setDataPrevista(dto.getDataPrevista() != null ? dto.getDataPrevista() : LocalDate.now());
        a.setSituacao(SituacaoAcaoProgramatica.PENDENTE);
        a.setCriadaEm(Instant.now());
        a.setRegistradoPorCpf(UsuarioAutenticado.cpf());
        a = acaoRepository.save(a);
        ContextoAuditoria.registro(a.getUuid());
        ContextoAuditoria.detalhe("Busca ativa no programa " + ac.getPrograma().getCodigo() + ".");
        return dto(a, LocalDate.now());
    }

    /** Confirma a realização: a data (até hoje), o atendimento que a cumpriu e, na busca ativa, o resultado. */
    @Transactional
    public AcaoProgramaticaDto realizar(UUID acaoId, RealizacaoAcaoRequestDto dto) {
        AcaoProgramatica a = pendente(acaoId);
        LocalDate dia = dto.getRealizadaEm() != null ? dto.getRealizadaEm() : LocalDate.now();
        if (dia.isAfter(LocalDate.now())) {
            throw new ResourceBadRequestException("A realização não pode ser no futuro.");
        }
        if (a.getTipo() == TipoAcaoProgramatica.BUSCA_ATIVA && dto.getResultadoBusca() == null) {
            throw new ResourceBadRequestException("A busca ativa pede o resultado.");
        }
        if (a.getTipo() != TipoAcaoProgramatica.BUSCA_ATIVA && dto.getResultadoBusca() != null) {
            throw new ResourceBadRequestException("O resultado é só da busca ativa.");
        }
        if (dto.getAtendimentoId() != null) {
            if (a.getPaciente() == null) {
                throw new ResourceBadRequestException("A ação coletiva não se liga a um atendimento.");
            }
            Atendimento at = atendimentoRepository.findById(dto.getAtendimentoId()).orElseThrow(() -> new ResourceNotFoundException(
                    "Não foi possível encontrar o atendimento " + dto.getAtendimentoId() + " em nossos registros."));
            if (!at.getPaciente().getUuid().equals(a.getPaciente().getUuid())) {
                throw new ResourceUnprocessableEntityException("O atendimento é de outro paciente.");
            }
            a.setAtendimento(at);
        }
        if (a.getPaciente() == null && a.getPrograma() != null && dto.getParticipantesIds() != null) {
            a.setParticipantes(participantes(a.getPrograma(), dto.getParticipantesIds()));
        }
        if (dto.getQuantidadeAtendida() != null && a.getPaciente() != null) {
            throw new ResourceBadRequestException("A quantidade atendida é da ação coletiva.");
        }
        a.setQuantidadeAtendida(dto.getQuantidadeAtendida());
        a.setRealizadaEm(dia);
        a.setResultadoBusca(dto.getResultadoBusca());
        a.setObservacao(texto(dto.getObservacao()));
        a.setSituacao(SituacaoAcaoProgramatica.REALIZADA);
        acaoRepository.save(a);
        ContextoAuditoria.detalhe("Realizada" + (dto.getResultadoBusca() != null ? ": " + dto.getResultadoBusca() : "") + ".");
        return dto(a, LocalDate.now());
    }

    /** Não realizada ou cancelada, com o motivo. */
    @Transactional
    public AcaoProgramaticaDto mudarSituacao(UUID acaoId, SituacaoAcaoRequestDto dto) {
        if (dto.getSituacao() != SituacaoAcaoProgramatica.NAO_REALIZADA && dto.getSituacao() != SituacaoAcaoProgramatica.CANCELADA) {
            throw new ResourceBadRequestException("Aqui a situação é NAO_REALIZADA ou CANCELADA; para realizar, use a realização.");
        }
        AcaoProgramatica a = pendente(acaoId);
        a.setSituacao(dto.getSituacao());
        a.setMotivo(dto.getMotivo().trim());
        acaoRepository.save(a);
        ContextoAuditoria.detalhe(dto.getSituacao() + ": " + a.getMotivo());
        return dto(a, LocalDate.now());
    }

    /**
     * Liga a ação pendente à marcação feita a partir dela (pela Agenda): do mesmo paciente e programa. Chamado pela
     * marcação, que já conferiu o acesso à agenda.
     */
    @Transactional
    void vincularAgendamento(UUID acaoId, Agendamento agendamento) {
        AcaoProgramatica a = acaoRepository.findById(acaoId).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar a ação programática " + acaoId + " em nossos registros."));
        if (a.getSituacao() != SituacaoAcaoProgramatica.PENDENTE || a.getPaciente() == null) {
            throw new ResourceUnprocessableEntityException("Só a ação individual pendente pode ser agendada.");
        }
        if (!a.getPaciente().getUuid().equals(agendamento.getPaciente().getUuid())) {
            throw new ResourceUnprocessableEntityException("A ação programática é de outro paciente.");
        }
        if (agendamento.getPrograma() == null || !agendamento.getPrograma().getUuid().equals(a.getPrograma().getUuid())) {
            throw new ResourceUnprocessableEntityException("A marcação precisa ser do programa da ação (" + a.getPrograma().getNome() + ").");
        }
        a.setAgendamento(agendamento);
        acaoRepository.save(a);
    }

    // ── Leitura ────────────────────────────────────────────────────────────────────────────────────

    /**
     * As ações do escopo no período (padrão: a semana, de segunda a domingo), com filtros; as vencidas de antes do período
     * também entram, porque continuam pendentes. Programa sensível só para quem acompanha na unidade.
     */
    @Transactional(readOnly = true)
    public AcoesProgramaticasDto acoes(UUID programaId, UUID campanhaId, UUID unidadeId, UUID equipeId, String responsavelMatricula, LocalDate de,
                                       LocalDate ate, SituacaoAcaoProgramatica situacao) {
        LocalDate hoje = LocalDate.now();
        LocalDate inicio = de != null ? de : hoje.minusDays(hoje.getDayOfWeek().getValue() - 1L);
        LocalDate fim = ate != null ? ate : inicio.plusDays(6);
        if (fim.isBefore(inicio)) {
            throw new ResourceBadRequestException("O fim do período precisa ser depois do início.");
        }
        String responsavel = texto(responsavelMatricula);
        List<AcaoProgramatica> escopo = controleDeAcesso.filtrar(acaoRepository.findTodas().stream(), a -> Stream.of(a.getUnidade()), ACOMPANHAR)
                .filter(this::podeVer)
                .filter(a -> programaId == null || (a.getPrograma() != null && a.getPrograma().getUuid().equals(programaId)))
                .filter(a -> campanhaId == null || (a.getCampanha() != null && a.getCampanha().getUuid().equals(campanhaId)))
                .filter(a -> unidadeId == null || a.getUnidade().getUuid().equals(unidadeId))
                .filter(a -> equipeId == null || (a.getEquipe() != null && a.getEquipe().getUuid().equals(equipeId)))
                .filter(a -> responsavel == null || (a.getResponsavel() != null && responsavel.equals(a.getResponsavel().getMatricula())))
                .toList();
        List<AcaoProgramaticaDto> todas = escopo.stream().map(a -> dto(a, hoje)).toList();
        LocalDate inicioDoMes = hoje.withDayOfMonth(1);
        List<AcaoProgramaticaDto> lista = todas.stream()
                .filter(d -> (!d.getDataPrevista().isBefore(inicio) && !d.getDataPrevista().isAfter(fim)) || d.isVencida())
                .filter(d -> situacao == null || d.getSituacao() == situacao)
                .sorted(Comparator.comparing((AcaoProgramaticaDto d) -> !d.isProntaParaConfirmar()).thenComparing(d -> !d.isVencida())
                        .thenComparing(AcaoProgramaticaDto::getDataPrevista))
                .toList();
        return new AcoesProgramaticasDto(inicio, fim,
                todas.stream().filter(d -> d.getSituacao() == SituacaoAcaoProgramatica.PENDENTE
                        && !d.getDataPrevista().isBefore(inicio) && !d.getDataPrevista().isAfter(fim)).count(),
                todas.stream().filter(AcaoProgramaticaDto::isVencida).count(),
                todas.stream().filter(d -> d.getSituacao() == SituacaoAcaoProgramatica.REALIZADA && d.getRealizadaEm() != null
                        && !d.getRealizadaEm().isBefore(inicioDoMes)).count(),
                todas.stream().filter(AcaoProgramaticaDto::isProntaParaConfirmar).count(), lista);
    }

    /** As ações do paciente, com as regras do prontuário (vínculo e auditoria de leitura) e o sigilo do programa. */
    @Transactional(readOnly = true)
    public List<AcaoProgramaticaDto> doPaciente(UUID pacienteId) {
        if (!pacienteRepository.existsById(pacienteId)) {
            throw new ResourceNotFoundException("Não foi possível encontrar um paciente com o id " + pacienteId + " em nossos registros.");
        }
        ContextoAuditoria.paciente(pacienteId);
        vinculoService.exigirVinculo(pacienteId);
        LocalDate hoje = LocalDate.now();
        return acaoRepository.findByPaciente_UuidOrderByDataPrevistaDesc(pacienteId).stream().filter(this::podeVer).map(a -> dto(a, hoje)).toList();
    }

    // ── Apoio ──────────────────────────────────────────────────────────────────────────────────────

    private AcaoProgramatica pendente(UUID acaoId) {
        AcaoProgramatica a = acaoRepository.findById(acaoId).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar a ação programática " + acaoId + " em nossos registros."));
        controleDeAcesso.exigir(ACOMPANHAR, a.getUnidade());
        ContextoAuditoria.registro(a.getUuid());
        if (a.getPaciente() != null) {
            ContextoAuditoria.paciente(a.getPaciente().getUuid());
        }
        if (a.getSituacao() != SituacaoAcaoProgramatica.PENDENTE) {
            throw new ResourceUnprocessableEntityException("A ação já está " + a.getSituacao().name().toLowerCase().replace('_', ' ') + " e não muda mais.");
        }
        return a;
    }

    /** Programa sensível: a mesma regra do acompanhamento (quem acompanha na unidade). A ação da campanha não é sigilosa. */
    private boolean podeVer(AcaoProgramatica a) {
        return a.getPrograma() == null || acompanhamentoService.podeVer(a.getPrograma(), a.getUnidade());
    }

    private Set<Paciente> participantes(Programa programa, List<UUID> ids) {
        Set<Paciente> lista = new HashSet<>();
        for (UUID id : ids == null ? List.<UUID>of() : ids) {
            Paciente p = pacienteRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(
                    "Não foi possível encontrar um paciente com o id " + id + " em nossos registros."));
            if (!acompanhamentoRepository.existsByPaciente_UuidAndPrograma_UuidAndFimIsNull(p.getUuid(), programa.getUuid())) {
                throw new ResourceUnprocessableEntityException(p.getNome() + " não é acompanhado no programa " + programa.getNome() + ".");
            }
            lista.add(p);
        }
        return lista;
    }

    private Equipe equipe(UUID equipeId, UnidadeDeSaude unidade) {
        if (equipeId == null) {
            return null;
        }
        Equipe e = equipeRepository.findById(equipeId).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar uma equipe com o id " + equipeId + " em nossos registros."));
        if (!e.getUnidade().getUuid().equals(unidade.getUuid())) {
            throw new ResourceUnprocessableEntityException("A equipe " + e.getNome() + " não é da " + unidade.getNome() + ".");
        }
        return e;
    }

    /** O ACS da microárea de adscrição do paciente, se lotado na unidade do acompanhamento (Território, ADRs 0125 e 0128). */
    private Optional<Profissional> acsDoPaciente(Paciente paciente, UnidadeDeSaude unidade) {
        return adscricaoRepository.findByPaciente_UuidAndFimIsNull(paciente.getUuid())
                .filter(ad -> ad.getMicroarea() != null)
                .flatMap(ad -> atribuicaoRepository.findByMicroarea_UuidAndFimIsNull(ad.getMicroarea().getUuid()))
                .map(at -> at.getMembro().getProfissional())
                .filter(p -> {
                    try {
                        acompanhamentoService.profissionalResponsavel(p.getMatricula(), unidade);
                        return true;
                    } catch (ResourceUnprocessableEntityException e) {
                        return false;
                    }
                });
    }

    private AcaoProgramaticaDto dto(AcaoProgramatica a, LocalDate hoje) {
        boolean pendente = a.getSituacao() == SituacaoAcaoProgramatica.PENDENTE;
        // Pronta para confirmar: a marcação feita a partir da ação já virou atendimento concluído.
        Atendimento sugerido = !pendente || a.getAgendamento() == null ? null
                : atendimentoRepository.findByAgendamento_Uuid(a.getAgendamento().getUuid()).stream()
                .filter(at -> at.getStatus() == StatusAtendimento.CONCLUIDO).findFirst().orElse(null);
        String sugestao = a.getResultadoBusca() == ResultadoBuscaAtiva.MUDOU_DE_ENDERECO && a.getAcompanhamento() != null && a.getAcompanhamento().vigente()
                ? "Mudou de endereço: avalie encerrar o acompanhamento (transferência)." : null;
        Programa p = a.getPrograma();
        return new AcaoProgramaticaDto(a.getUuid(), p != null ? p.getUuid() : null, p != null ? p.getCodigo() : null,
                p != null ? p.getNome() : a.getCampanha().getNome(), p != null && p.isSensivel(), a.getTipo(), a.getPaciente() == null,
                a.getAcompanhamento() != null ? a.getAcompanhamento().getUuid() : null,
                a.getPaciente() != null ? a.getPaciente().getUuid() : null, a.getPaciente() != null ? a.getPaciente().getNome() : null,
                a.getUnidade().getUuid(), a.getUnidade().getNome(), a.getEquipe() != null ? a.getEquipe().getUuid() : null,
                a.getEquipe() != null ? a.getEquipe().getNome() : null, a.getLocal(), a.getDescricao(),
                a.getResponsavel() != null ? a.getResponsavel().getMatricula() : null, a.getResponsavel() != null ? a.getResponsavel().getNome() : null,
                a.getDataPrevista(), a.getSituacao(), pendente && a.getDataPrevista().isBefore(hoje), a.getRealizadaEm(),
                a.getAtendimento() != null ? a.getAtendimento().getUuid() : null, a.getAgendamento() != null ? a.getAgendamento().getUuid() : null,
                a.getAgendamento() != null ? a.getAgendamento().getDataHora() : null, sugerido != null, sugerido != null ? sugerido.getUuid() : null,
                a.getMotivo(), a.getResultadoBusca(), sugestao, a.getObservacao(),
                a.getParticipantes().stream().sorted(Comparator.comparing(Paciente::getNome, String.CASE_INSENSITIVE_ORDER))
                        .map(x -> new AcaoProgramaticaDto.Participante(x.getUuid(), x.getNome())).toList(),
                a.getCriadaEm(), a.getCampanha() != null ? a.getCampanha().getUuid() : null, a.getCampanha() != null ? a.getCampanha().getNome() : null,
                a.getLocalCampanha() != null ? a.getLocalCampanha().getUuid() : null, a.getLocal(), a.getQuantidadeAtendida());
    }

    private static String texto(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }
}
