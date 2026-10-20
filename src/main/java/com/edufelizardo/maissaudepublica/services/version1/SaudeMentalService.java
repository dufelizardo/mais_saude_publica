package com.edufelizardo.maissaudepublica.services.version1;

import com.edufelizardo.maissaudepublica.config.ContextoAuditoria;
import com.edufelizardo.maissaudepublica.config.UsuarioAutenticado;
import com.edufelizardo.maissaudepublica.exceptions.ResourceBadRequestException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceConflictException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceForbiddenException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceNotFoundException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceUnprocessableEntityException;
import com.edufelizardo.maissaudepublica.models.AcolhimentoSaudeMental;
import com.edufelizardo.maissaudepublica.models.AcompanhamentoProgramatico;
import com.edufelizardo.maissaudepublica.models.AcompanhamentoSaudeMental;
import com.edufelizardo.maissaudepublica.models.Atendimento;
import com.edufelizardo.maissaudepublica.models.AvaliacaoSaudeMental;
import com.edufelizardo.maissaudepublica.models.Equipe;
import com.edufelizardo.maissaudepublica.models.MembroEquipe;
import com.edufelizardo.maissaudepublica.models.Paciente;
import com.edufelizardo.maissaudepublica.models.Profissional;
import com.edufelizardo.maissaudepublica.models.ProjetoTerapeuticoSingular;
import com.edufelizardo.maissaudepublica.models.Programa;
import com.edufelizardo.maissaudepublica.models.ReferenciaSaudeMental;
import com.edufelizardo.maissaudepublica.models.UnidadeDeSaude;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.AcolhimentoSaudeMentalRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.AcompanhamentoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.AcompanhamentoSaudeMentalRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.AvaliacaoSaudeMentalRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.EncerramentoAcompanhamentoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.ReferenciaSaudeMentalRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.AcessoProntuarioDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.AcolhimentoSaudeMentalDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.AcolhimentosSaudeMentalDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.AcompanhamentoDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.AcompanhamentoSaudeMentalDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.AvaliacaoSaudeMentalDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.CasosSaudeMentalDto;
import com.edufelizardo.maissaudepublica.models.enuns.DesfechoAcolhimentoSaudeMental;
import com.edufelizardo.maissaudepublica.models.enuns.MotivoSaidaAcompanhamento;
import com.edufelizardo.maissaudepublica.models.enuns.RiscoInicialSaudeMental;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoAcompanhamento;
import com.edufelizardo.maissaudepublica.repositories.AcolhimentoSaudeMentalRepository;
import com.edufelizardo.maissaudepublica.repositories.AcompanhamentoProgramaticoRepository;
import com.edufelizardo.maissaudepublica.repositories.AcompanhamentoSaudeMentalRepository;
import com.edufelizardo.maissaudepublica.repositories.AtendimentoRepository;
import com.edufelizardo.maissaudepublica.repositories.AvaliacaoSaudeMentalRepository;
import com.edufelizardo.maissaudepublica.repositories.EquipeRepository;
import com.edufelizardo.maissaudepublica.repositories.MembroEquipeRepository;
import com.edufelizardo.maissaudepublica.repositories.PacienteRepository;
import com.edufelizardo.maissaudepublica.repositories.ProfissionalRepository;
import com.edufelizardo.maissaudepublica.repositories.ProgramaRepository;
import com.edufelizardo.maissaudepublica.repositories.ReferenciaSaudeMentalRepository;
import com.edufelizardo.maissaudepublica.repositories.UnidadeDeSaudeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.Period;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Saúde Mental, fatia SM1 (ADR-0120, desenho na ADR-0112): acolhimento, caso longitudinal ligado ao programa Saúde
 * Mental, equipe e técnico de referência e avaliação.
 *
 * <p>Sigilo no grau mais alto (LGPD, art. 11). Ler o caso pede vínculo assistencial (ADR-0076), e ser da equipe de
 * referência conta como vínculo. O conteúdo clínico (acolhimentos e avaliações) só vai para a equipe de referência
 * vigente, para quem registrou algo no caso e para o acesso justificado ou a permissão ampla; os demais veem que o caso
 * existe e quem é a referência. Com a autorização desligada, tudo passa, como no resto do sistema (ADR-0067).
 */
@Service
public class SaudeMentalService {

    public static final String ACOLHER = "SAUDE_MENTAL.ACOLHER";
    public static final String ACOMPANHAR = "SAUDE_MENTAL.ACOMPANHAR";
    public static final String PROGRAMA_SAUDE_MENTAL = "SAUDE_MENTAL";

    @Autowired
    private AcompanhamentoSaudeMentalRepository casoRepository;

    @Autowired
    private ReferenciaSaudeMentalRepository referenciaRepository;

    @Autowired
    private AcolhimentoSaudeMentalRepository acolhimentoRepository;

    @Autowired
    private AvaliacaoSaudeMentalRepository avaliacaoRepository;

    @Autowired
    private AcompanhamentoProgramaticoRepository programaticoRepository;

    @Autowired
    private ProgramaRepository programaRepository;

    @Autowired
    private PacienteRepository pacienteRepository;

    @Autowired
    private UnidadeDeSaudeRepository unidadeDeSaudeRepository;

    @Autowired
    private EquipeRepository equipeRepository;

    @Autowired
    private MembroEquipeRepository membroRepository;

    @Autowired
    private ProfissionalRepository profissionalRepository;

    @Autowired
    private AtendimentoRepository atendimentoRepository;

    @Autowired
    private AcompanhamentoService acompanhamentoService;

    @Autowired
    private ControleDeAcesso controleDeAcesso;

    @Autowired
    private VinculoAssistencialService vinculoService;

    @Autowired
    private TerapeuticoLeitura terapeutico;

    @Autowired
    private RedeLeitura rede;

    // ── Escrita ────────────────────────────────────────────────────────────────────────────────────

    /**
     * Registra o acolhimento. Com desfecho que acompanha: se o paciente já tem caso vigente, o acolhimento entra nele;
     * senão, o caso abre junto, com a equipe e o técnico de referência.
     */
    @Transactional
    public AcolhimentoSaudeMentalDto acolher(AcolhimentoSaudeMentalRequestDto dto) {
        UnidadeDeSaude unidade = unidade(dto.getUnidadeId());
        controleDeAcesso.exigir(ACOLHER, unidade);
        Paciente paciente = paciente(dto.getPacienteId());
        ContextoAuditoria.paciente(paciente.getUuid());
        if (!paciente.isAtivo()) {
            throw new ResourceUnprocessableEntityException("O cadastro do paciente está inativo.");
        }
        Atendimento atendimento = null;
        if (dto.getAtendimentoId() != null) {
            atendimento = atendimentoRepository.findById(dto.getAtendimentoId()).orElseThrow(() -> new ResourceNotFoundException(
                    "Não foi possível encontrar o atendimento " + dto.getAtendimentoId() + " em nossos registros."));
            if (atendimento.getPaciente() == null || !atendimento.getPaciente().getUuid().equals(paciente.getUuid())) {
                throw new ResourceUnprocessableEntityException("O atendimento informado é de outro paciente.");
            }
        }
        String destino = texto(dto.getEncaminhamentoDestino());
        if (dto.getDesfecho() == DesfechoAcolhimentoSaudeMental.ENCAMINHAMENTO && destino == null) {
            throw new ResourceBadRequestException("No encaminhamento, informe para onde.");
        }
        AcompanhamentoSaudeMental caso = null;
        String aviso = null;
        if (dto.getDesfecho().abreAcompanhamento()) {
            Optional<AcompanhamentoSaudeMental> vigente = casoRepository.findVigenteDoPaciente(paciente.getUuid());
            if (vigente.isPresent()) {
                caso = vigente.get();
                aviso = "O paciente já é acompanhado na " + caso.getUnidade().getNome() + "; o acolhimento entrou nesse caso.";
            } else {
                if (dto.getEquipeId() == null || texto(dto.getTecnicoMatricula()) == null) {
                    throw new ResourceBadRequestException("Para acompanhar, informe a equipe e o técnico de referência.");
                }
                caso = abrirCaso(paciente, unidade, dto.getEquipeId(), dto.getTecnicoMatricula(), LocalDate.now(), null);
            }
        }
        if (dto.getRiscoInicial() == RiscoInicialSaudeMental.ALTO) {
            String risco = "Risco alto: conduza pela rede de urgência. O registro da crise chega na próxima fatia (SM3).";
            aviso = aviso == null ? risco : aviso + " " + risco;
        }
        AcolhimentoSaudeMental a = acolhimentoRepository.save(new AcolhimentoSaudeMental(null, paciente, unidade, profissionalDoUsuario(),
                atendimento, caso, Instant.now(), dto.getMotivoProcura().trim(), texto(dto.getNecessidadeImediata()),
                texto(dto.getVulnerabilidade()), dto.getRiscoInicial(), dto.getDesfecho(),
                dto.getDesfecho() == DesfechoAcolhimentoSaudeMental.ENCAMINHAMENTO ? destino : null, UsuarioAutenticado.cpf()));
        ContextoAuditoria.registro(a.getUuid());
        ContextoAuditoria.detalhe("Acolhimento em saúde mental, desfecho " + dto.getDesfecho() + ".");
        AcolhimentoSaudeMentalDto resposta = acolhimentoDto(a, true);
        resposta.setAviso(aviso);
        return resposta;
    }

    /** Abre o caso sem acolhimento novo. Um caso vigente por paciente (409). */
    @Transactional
    public AcompanhamentoSaudeMentalDto abrir(AcompanhamentoSaudeMentalRequestDto dto) {
        UnidadeDeSaude unidade = unidade(dto.getUnidadeId());
        controleDeAcesso.exigir(ACOMPANHAR, unidade);
        Paciente paciente = paciente(dto.getPacienteId());
        ContextoAuditoria.paciente(paciente.getUuid());
        if (!paciente.isAtivo()) {
            throw new ResourceUnprocessableEntityException("O cadastro do paciente está inativo.");
        }
        casoRepository.findVigenteDoPaciente(paciente.getUuid()).ifPresent(c -> {
            throw new ResourceConflictException("O paciente já é acompanhado em saúde mental na " + c.getUnidade().getNome() + ".");
        });
        LocalDate inicio = dto.getInicio() != null ? dto.getInicio() : LocalDate.now();
        if (inicio.isAfter(LocalDate.now())) {
            throw new ResourceBadRequestException("O início não pode ser no futuro.");
        }
        AcompanhamentoSaudeMental caso = abrirCaso(paciente, unidade, dto.getEquipeId(), dto.getTecnicoMatricula(), inicio, null);
        return detalhe(caso, null, true);
    }

    /** Troca a equipe ou o técnico de referência: a vigente fecha hoje e a nova abre, com o motivo. */
    @Transactional
    public AcompanhamentoSaudeMentalDto trocarReferencia(UUID casoId, ReferenciaSaudeMentalRequestDto dto) {
        AcompanhamentoSaudeMental caso = casoParaEscrever(casoId);
        Equipe equipe = equipeDaUnidade(dto.getEquipeId(), caso.getUnidade());
        Profissional tecnico = tecnicoDaEquipe(dto.getTecnicoMatricula(), equipe);
        List<ReferenciaSaudeMental> vigentes = referenciaRepository.findDosAcompanhamentos(List.of(caso.getUuid())).stream()
                .filter(ReferenciaSaudeMental::vigente).toList();
        if (vigentes.stream().anyMatch(r -> r.getEquipe().getUuid().equals(equipe.getUuid()) && r.getTecnico().getUuid().equals(tecnico.getUuid()))) {
            throw new ResourceUnprocessableEntityException("Essa já é a referência do caso.");
        }
        for (ReferenciaSaudeMental r : vigentes) {
            r.setFim(LocalDate.now());
            referenciaRepository.save(r);
        }
        referenciaRepository.save(new ReferenciaSaudeMental(null, caso, equipe, tecnico, LocalDate.now(), null, dto.getMotivo().trim(),
                UsuarioAutenticado.cpf()));
        ContextoAuditoria.detalhe("Referência: " + equipe.getNome() + ", " + tecnico.getMatricula() + ".");
        return detalhe(caso, null, true);
    }

    @Transactional
    public AcompanhamentoSaudeMentalDto avaliar(UUID casoId, AvaliacaoSaudeMentalRequestDto dto) {
        AcompanhamentoSaudeMental caso = casoParaEscrever(casoId);
        AvaliacaoSaudeMental a = avaliacaoRepository.save(new AvaliacaoSaudeMental(null, caso, profissionalDoUsuario(), Instant.now(),
                dto.getContextoHistoria().trim(), texto(dto.getAspectosPsicossociais()), texto(dto.getFatoresRisco()),
                texto(dto.getFatoresProtecao()), dto.getNecessidades().trim(),
                texto(dto.getCid()) == null ? null : texto(dto.getCid()).toUpperCase(), UsuarioAutenticado.cpf()));
        ContextoAuditoria.registro(a.getUuid());
        ContextoAuditoria.detalhe("Avaliação em saúde mental.");
        return detalhe(caso, null, true);
    }

    /** Encerra o caso, fecha a referência e registra a saída do programa Saúde Mental, com o mesmo motivo. */
    @Transactional
    public AcompanhamentoSaudeMentalDto encerrar(UUID casoId, EncerramentoAcompanhamentoRequestDto dto) {
        AcompanhamentoSaudeMental caso = casoParaEscrever(casoId);
        if (rede.transferenciaPendente(caso.getUuid()) != null) {
            throw new ResourceUnprocessableEntityException("O caso tem uma transferência do cuidado pendente; cancele-a antes de encerrar.");
        }
        encerrarCaso(caso, dto);
        return detalhe(caso, null, true);
    }

    /** Encerra o caso, a referência e o acompanhamento do programa, sem conferir o acesso (quem chama já conferiu). */
    void encerrarCaso(AcompanhamentoSaudeMental caso, EncerramentoAcompanhamentoRequestDto dto) {
        LocalDate fim = dto.getFim() != null ? dto.getFim() : LocalDate.now();
        if (fim.isBefore(caso.getInicio()) || fim.isAfter(LocalDate.now())) {
            throw new ResourceBadRequestException("A saída precisa ser entre o início (" + caso.getInicio() + ") e hoje.");
        }
        caso.setFim(fim);
        caso.setMotivoSaida(dto.getMotivoSaida());
        caso.setObservacaoSaida(dto.getObservacao().trim());
        casoRepository.saveAndFlush(caso);
        for (ReferenciaSaudeMental r : referenciaRepository.findDosAcompanhamentos(List.of(caso.getUuid()))) {
            if (r.vigente()) {
                r.setFim(fim);
                referenciaRepository.save(r);
            }
        }
        AcompanhamentoProgramatico programatico = caso.getAcompanhamentoProgramatico();
        if (programatico.vigente()) {
            LocalDate fimPrograma = fim.isBefore(programatico.getInicio()) ? programatico.getInicio() : fim;
            acompanhamentoService.encerrarPelaSaudeMental(programatico.getUuid(),
                    new EncerramentoAcompanhamentoRequestDto(dto.getMotivoSaida(), dto.getObservacao(), fimPrograma));
        }
        ContextoAuditoria.registro(caso.getUuid());
        ContextoAuditoria.detalhe("Saída da saúde mental (" + dto.getMotivoSaida() + ").");
    }

    // ── Leitura ────────────────────────────────────────────────────────────────────────────────────

    /** O caso, com vínculo assistencial; o conteúdo clínico só para a referência, os autores e o acesso justificado. */
    @Transactional(readOnly = true)
    public AcompanhamentoSaudeMentalDto caso(UUID casoId) {
        AcompanhamentoSaudeMental caso = buscarCaso(casoId);
        ContextoAuditoria.paciente(caso.getPaciente().getUuid());
        ContextoAuditoria.registro(caso.getUuid());
        AcessoProntuarioDto acesso = vinculoService.exigirVinculo(caso.getPaciente().getUuid());
        boolean liberado = liberado(caso, acesso);
        ContextoAuditoria.detalhe((acesso.getDescricao() != null ? acesso.getDescricao() + "; " : "")
                + (liberado ? "conteúdo liberado." : "só o resumo do caso."));
        return detalhe(caso, acesso, liberado);
    }

    /** Os acolhimentos do paciente, com vínculo; conteúdo só onde estiver liberado. */
    @Transactional(readOnly = true)
    public AcolhimentosSaudeMentalDto acolhimentosDoPaciente(UUID pacienteId) {
        if (!pacienteRepository.existsById(pacienteId)) {
            throw new ResourceNotFoundException("Não foi possível encontrar um paciente com o id " + pacienteId + " em nossos registros.");
        }
        ContextoAuditoria.paciente(pacienteId);
        AcessoProntuarioDto acesso = vinculoService.exigirVinculo(pacienteId);
        Map<UUID, Boolean> porCaso = new HashMap<>();
        List<AcolhimentoSaudeMentalDto> lista = acolhimentoRepository.findDoPaciente(pacienteId).stream().map(a -> {
            boolean liberado = a.getAcompanhamento() != null
                    ? porCaso.computeIfAbsent(a.getAcompanhamento().getUuid(), k -> liberado(a.getAcompanhamento(), acesso))
                    : liberadoSemCaso(a, acesso);
            return acolhimentoDto(a, liberado);
        }).toList();
        return new AcolhimentosSaudeMentalDto(pacienteId, acesso, lista);
    }

    /**
     * Os casos vigentes nas unidades do escopo, sem conteúdo clínico. {@code escopo}: TECNICO (sou o técnico de
     * referência) ou EQUIPE (sou de uma equipe de referência); sem escopo, todos os do meu acesso. {@code pendencia}:
     * SEM_AVALIACAO, SEM_PTS ou PTS_A_REVISAR (ADR-0121).
     */
    @Transactional(readOnly = true)
    public CasosSaudeMentalDto casos(UUID unidadeId, UUID pacienteId, String escopo, SituacaoAcompanhamento situacao, String pendencia) {
        List<AcompanhamentoSaudeMental> vigentes = controleDeAcesso.filtrar(casoRepository.findVigentes().stream(),
                        c -> java.util.stream.Stream.of(c.getUnidade()), ACOMPANHAR)
                .filter(c -> unidadeId == null || c.getUnidade().getUuid().equals(unidadeId))
                .filter(c -> pacienteId == null || c.getPaciente().getUuid().equals(pacienteId))
                .toList();
        List<UUID> ids = vigentes.stream().map(AcompanhamentoSaudeMental::getUuid).toList();
        Map<UUID, ReferenciaSaudeMental> referencias = vigentesPorCaso(ids);
        Map<UUID, Instant> ultimas = ultimasAvaliacoes(ids);
        Map<UUID, ProjetoTerapeuticoSingular> pts = terapeutico.ptsVigentes(ids);
        String cpf = UsuarioAutenticado.cpf();
        Set<UUID> minhasEquipes = cpf == null ? Set.of() : minhasEquipes(cpf);
        List<AcompanhamentoSaudeMentalDto> todos = vigentes.stream()
                .filter(c -> {
                    if (cpf == null || escopo == null || escopo.isBlank()) {
                        return true;
                    }
                    ReferenciaSaudeMental r = referencias.get(c.getUuid());
                    if ("TECNICO".equalsIgnoreCase(escopo)) {
                        return r != null && cpf.equals(r.getTecnico().getCpf());
                    }
                    return r != null && minhasEquipes.contains(r.getEquipe().getUuid());
                })
                .map(c -> resumo(c, referencias.get(c.getUuid()), ultimas.get(c.getUuid()), pts.get(c.getUuid())))
                .toList();
        List<AcompanhamentoSaudeMentalDto> lista = todos.stream()
                .filter(d -> situacao == null || d.getSituacao() == situacao)
                .filter(d -> pendencia == null || pendencia.isBlank()
                        || ("SEM_AVALIACAO".equalsIgnoreCase(pendencia) && d.getUltimaAvaliacaoEm() == null)
                        || ("SEM_PTS".equalsIgnoreCase(pendencia) && d.getPtsVersao() == null)
                        || ("PTS_A_REVISAR".equalsIgnoreCase(pendencia) && d.isPtsRevisaoVencida()))
                .sorted(Comparator.comparing((AcompanhamentoSaudeMentalDto d) -> d.getSituacao() != SituacaoAcompanhamento.FALTOSO)
                        .thenComparing(d -> d.getUltimaAvaliacaoEm() != null)
                        .thenComparing(AcompanhamentoSaudeMentalDto::getPacienteNome, String.CASE_INSENSITIVE_ORDER))
                .toList();
        return new CasosSaudeMentalDto(todos.size(),
                todos.stream().filter(d -> d.getSituacao() == SituacaoAcompanhamento.FALTOSO).count(),
                todos.stream().filter(d -> d.getUltimaAvaliacaoEm() == null).count(),
                todos.stream().filter(d -> d.getPtsVersao() == null).count(),
                todos.stream().filter(AcompanhamentoSaudeMentalDto::isPtsRevisaoVencida).count(), lista);
    }

    // ── Apoio ──────────────────────────────────────────────────────────────────────────────────────

    /**
     * Abre o caso e a primeira referência. Reaproveita o acompanhamento vigente do programa Saúde Mental na mesma
     * unidade; sem ele, inscreve (a unidade precisa oferecer o programa, ADR-0119).
     */
    AcompanhamentoSaudeMental abrirCaso(Paciente paciente, UnidadeDeSaude unidade, UUID equipeId, String matricula, LocalDate inicio,
                                        AcompanhamentoSaudeMental casoAnterior) {
        controleDeAcesso.exigir(ACOMPANHAR, unidade);
        Equipe equipe = equipeDaUnidade(equipeId, unidade);
        Profissional tecnico = tecnicoDaEquipe(matricula, equipe);
        Programa programa = programaRepository.findByCodigo(PROGRAMA_SAUDE_MENTAL).orElseThrow(() -> new ResourceUnprocessableEntityException(
                "O programa Saúde Mental (" + PROGRAMA_SAUDE_MENTAL + ") não está no catálogo."));
        AcompanhamentoProgramatico programatico = programaticoRepository.findDoPaciente(paciente.getUuid()).stream()
                .filter(a -> a.vigente() && a.getPrograma().getUuid().equals(programa.getUuid()))
                .findFirst().orElse(null);
        if (programatico != null && !programatico.getUnidade().getUuid().equals(unidade.getUuid())) {
            throw new ResourceUnprocessableEntityException("O paciente é acompanhado no programa Saúde Mental na "
                    + programatico.getUnidade().getNome() + "; abra o caso lá ou registre a saída do programa antes.");
        }
        if (programatico == null) {
            AcompanhamentoDto inscrito = acompanhamentoService.inscrever(programa.getUuid(),
                    new AcompanhamentoRequestDto(paciente.getUuid(), unidade.getUuid(), equipe.getUuid(), null, inicio));
            programatico = programaticoRepository.findById(inscrito.getUuid()).orElseThrow();
        }
        AcompanhamentoSaudeMental caso = casoRepository.save(new AcompanhamentoSaudeMental(null, paciente, unidade, programatico, inicio,
                null, null, null, UsuarioAutenticado.cpf(), casoAnterior));
        referenciaRepository.save(new ReferenciaSaudeMental(null, caso, equipe, tecnico, inicio, null, null, UsuarioAutenticado.cpf()));
        ContextoAuditoria.registro(caso.getUuid());
        return caso;
    }

    /** Para escrever no caso: vigente, a permissão na unidade e o conteúdo liberado (a referência ou o acesso justificado). */
    AcompanhamentoSaudeMental casoParaEscrever(UUID casoId) {
        AcompanhamentoSaudeMental caso = buscarCaso(casoId);
        controleDeAcesso.exigir(ACOMPANHAR, caso.getUnidade());
        ContextoAuditoria.paciente(caso.getPaciente().getUuid());
        ContextoAuditoria.registro(caso.getUuid());
        if (!caso.vigente()) {
            throw new ResourceUnprocessableEntityException("Este caso já foi encerrado.");
        }
        if (!liberado(caso, null)) {
            throw new ResourceForbiddenException("Só a equipe de referência do caso, ou quem registrar um acesso justificado, "
                    + "registra neste caso.");
        }
        return caso;
    }

    /** Para ler o conteúdo do caso: vínculo assistencial e conteúdo liberado; sem ele, 403 (ADR-0121). */
    AcompanhamentoSaudeMental casoParaLer(UUID casoId) {
        AcompanhamentoSaudeMental caso = buscarCaso(casoId);
        ContextoAuditoria.paciente(caso.getPaciente().getUuid());
        ContextoAuditoria.registro(caso.getUuid());
        AcessoProntuarioDto acesso = vinculoService.exigirVinculo(caso.getPaciente().getUuid());
        if (!liberado(caso, acesso)) {
            throw new ResourceForbiddenException("O conteúdo deste caso é da equipe de referência; registre um acesso justificado.");
        }
        return caso;
    }

    /** O conteúdo clínico do caso: a referência vigente, quem registrou algo nele, o acesso justificado ou a permissão ampla. */
    private boolean liberado(AcompanhamentoSaudeMental caso, AcessoProntuarioDto acesso) {
        if (!controleDeAcesso.ativo()) {
            return true;
        }
        String cpf = UsuarioAutenticado.cpf();
        if (cpf == null) {
            return false;
        }
        if (acesso != null && ("JUSTIFICADO".equals(acesso.getBase()) || "PERMISSAO_AMPLA".equals(acesso.getBase()))) {
            return true;
        }
        if (cpf.equals(caso.getRegistradoPorCpf())) {
            return true;
        }
        ReferenciaSaudeMental r = vigentesPorCaso(List.of(caso.getUuid())).get(caso.getUuid());
        if (r != null && (cpf.equals(r.getTecnico().getCpf()) || minhasEquipes(cpf).contains(r.getEquipe().getUuid()))) {
            return true;
        }
        boolean autor = acolhimentoRepository.findDoPaciente(caso.getPaciente().getUuid()).stream()
                .anyMatch(a -> a.getAcompanhamento() != null && a.getAcompanhamento().getUuid().equals(caso.getUuid())
                        && cpf.equals(a.getRegistradoPorCpf()))
                || avaliacaoRepository.findDoAcompanhamento(caso.getUuid()).stream().anyMatch(a -> cpf.equals(a.getRegistradoPorCpf()))
                || terapeutico.autor(caso.getUuid(), cpf)
                // A equipe parceira do cuidado compartilhado e a referência do caso aberto por transferência (ADR-0122).
                || rede.parceiro(caso.getUuid(), cpf)
                || rede.referenciaDoCasoSeguinte(caso.getUuid(), cpf);
        return autor || vinculoService.acessoJustificadoVigente(cpf, caso.getPaciente().getUuid());
    }

    /** Acolhimento que não abriu caso: quem registrou, quem acolhe ou acompanha na unidade, e o acesso justificado. */
    private boolean liberadoSemCaso(AcolhimentoSaudeMental a, AcessoProntuarioDto acesso) {
        if (!controleDeAcesso.ativo()) {
            return true;
        }
        String cpf = UsuarioAutenticado.cpf();
        if (cpf == null) {
            return false;
        }
        if (cpf.equals(a.getRegistradoPorCpf()) || "JUSTIFICADO".equals(acesso.getBase()) || "PERMISSAO_AMPLA".equals(acesso.getBase())) {
            return true;
        }
        Optional<Set<UUID>> visiveis = controleDeAcesso.unidadesVisiveis(ACOLHER, ACOMPANHAR);
        return visiveis.map(ids -> ids.contains(a.getUnidade().getUuid())).orElse(true)
                || vinculoService.acessoJustificadoVigente(cpf, a.getPaciente().getUuid());
    }

    private Set<UUID> minhasEquipes(String cpf) {
        return profissionalRepository.findByCpf(cpf).stream()
                .flatMap(p -> membroRepository.findByProfissional_UuidAndFimIsNull(p.getUuid()).stream())
                .map(m -> m.getEquipe().getUuid())
                .collect(Collectors.toSet());
    }

    private Map<UUID, ReferenciaSaudeMental> vigentesPorCaso(Collection<UUID> casos) {
        if (casos.isEmpty()) {
            return Map.of();
        }
        Map<UUID, ReferenciaSaudeMental> m = new HashMap<>();
        for (ReferenciaSaudeMental r : referenciaRepository.findDosAcompanhamentos(casos)) {
            if (r.vigente()) {
                m.put(r.getAcompanhamento().getUuid(), r);
            }
        }
        return m;
    }

    private Map<UUID, Instant> ultimasAvaliacoes(Collection<UUID> casos) {
        if (casos.isEmpty()) {
            return Map.of();
        }
        Map<UUID, Instant> m = new HashMap<>();
        for (Object[] linha : avaliacaoRepository.findUltimas(casos)) {
            m.put((UUID) linha[0], (Instant) linha[1]);
        }
        return m;
    }

    private Equipe equipeDaUnidade(UUID equipeId, UnidadeDeSaude unidade) {
        Equipe equipe = equipeRepository.findById(equipeId).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar uma equipe com o id " + equipeId + " em nossos registros."));
        if (!equipe.getUnidade().getUuid().equals(unidade.getUuid())) {
            throw new ResourceUnprocessableEntityException("A equipe " + equipe.getNome() + " não é da " + unidade.getNome() + ".");
        }
        if (!equipe.isAtiva()) {
            throw new ResourceUnprocessableEntityException("A equipe " + equipe.getNome() + " está inativa.");
        }
        return equipe;
    }

    /** O técnico de referência precisa ser membro vigente da equipe de referência. */
    private Profissional tecnicoDaEquipe(String matricula, Equipe equipe) {
        String m = matricula.trim();
        Profissional p = profissionalRepository.findByMatricula(m).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar um profissional com a matrícula " + m + " em nossos registros."));
        boolean membro = membroRepository.findByEquipe_UuidInAndFimIsNull(List.of(equipe.getUuid())).stream()
                .map(MembroEquipe::getProfissional).anyMatch(x -> x.getUuid().equals(p.getUuid()));
        if (!p.isAtivo() || !membro) {
            throw new ResourceUnprocessableEntityException(p.getNome() + " não é membro vigente da equipe " + equipe.getNome()
                    + " e não pode ser o técnico de referência.");
        }
        return p;
    }

    /** A referência vigente do caso (ADR-0122: matriciamento e transferência). */
    ReferenciaSaudeMental referenciaVigente(AcompanhamentoSaudeMental caso) {
        return vigentesPorCaso(List.of(caso.getUuid())).get(caso.getUuid());
    }

    /** O caso, sem conferir o acesso (quem chama confere). */
    AcompanhamentoSaudeMental casoSemAcesso(UUID casoId) {
        return buscarCaso(casoId);
    }

    /** O caso com o conteúdo, depois de uma escrita (o casoParaEscrever já conferiu o acesso). */
    AcompanhamentoSaudeMentalDto detalheLiberado(AcompanhamentoSaudeMental caso) {
        return detalhe(caso, null, true);
    }

    /** O profissional é o técnico ou membro vigente da equipe de referência do caso (responsável no PTS, ADR-0121). */
    boolean daReferencia(AcompanhamentoSaudeMental caso, Profissional p) {
        ReferenciaSaudeMental r = vigentesPorCaso(List.of(caso.getUuid())).get(caso.getUuid());
        if (r == null) {
            return false;
        }
        return r.getTecnico().getUuid().equals(p.getUuid())
                || membroRepository.findByEquipe_UuidInAndFimIsNull(List.of(r.getEquipe().getUuid())).stream()
                .anyMatch(m -> m.getProfissional().getUuid().equals(p.getUuid()));
    }

    Profissional profissionalDoUsuario() {
        String cpf = UsuarioAutenticado.cpf();
        return cpf == null ? null : profissionalRepository.findByCpfAndAtivoTrue(cpf).orElse(null);
    }

    private AcompanhamentoSaudeMentalDto detalhe(AcompanhamentoSaudeMental caso, AcessoProntuarioDto acesso, boolean liberado) {
        List<ReferenciaSaudeMental> referencias = referenciaRepository.findDosAcompanhamentos(List.of(caso.getUuid()));
        ReferenciaSaudeMental vigente = referencias.stream().filter(ReferenciaSaudeMental::vigente).findFirst()
                .orElse(referencias.isEmpty() ? null : referencias.get(0));
        List<AvaliacaoSaudeMental> avaliacoes = avaliacaoRepository.findDoAcompanhamento(caso.getUuid());
        AcompanhamentoSaudeMentalDto d = resumo(caso, vigente, avaliacoes.isEmpty() ? null : avaliacoes.get(0).getRealizadaEm(),
                terapeutico.ptsVigentes(List.of(caso.getUuid())).get(caso.getUuid()));
        d.setConteudoLiberado(liberado);
        d.setAcesso(acesso);
        d.setReferencias(referencias.stream().map(r -> new AcompanhamentoSaudeMentalDto.Referencia(r.getEquipe().getUuid(),
                r.getEquipe().getNome(), r.getTecnico().getMatricula(), r.getTecnico().getNome(), r.getInicio(), r.getFim(),
                r.getMotivo())).toList());
        if (liberado) {
            d.setAcolhimentos(acolhimentoRepository.findDoPaciente(caso.getPaciente().getUuid()).stream()
                    .filter(a -> a.getAcompanhamento() != null && a.getAcompanhamento().getUuid().equals(caso.getUuid()))
                    .map(a -> acolhimentoDto(a, true)).toList());
            d.setAvaliacoes(avaliacoes.stream().map(a -> new AvaliacaoSaudeMentalDto(a.getUuid(), a.getRealizadaEm(),
                    a.getProfissional() != null ? a.getProfissional().getNome() : null, a.getContextoHistoria(),
                    a.getAspectosPsicossociais(), a.getFatoresRisco(), a.getFatoresProtecao(), a.getNecessidades(), a.getCid())).toList());
            d.setPts(terapeutico.versoes(caso.getUuid()));
            d.setIntervencoes(terapeutico.intervencoes(caso.getUuid()));
            d.setFaltasSeguidas(TerapeuticoLeitura.faltasSeguidas(d.getIntervencoes()));
            d.setRedeApoio(terapeutico.redeApoio(caso.getUuid()));
            d.setEncaminhamentos(rede.encaminhamentos(caso.getUuid()));
            d.setCompartilhamentos(rede.compartilhamentos(caso.getUuid()));
            d.setTransferencias(rede.transferencias(caso.getUuid()));
        }
        return d;
    }

    private static AcompanhamentoSaudeMentalDto resumo(AcompanhamentoSaudeMental c, ReferenciaSaudeMental r, Instant ultimaAvaliacao,
                                                       ProjetoTerapeuticoSingular pts) {
        Paciente pac = c.getPaciente();
        AcompanhamentoProgramatico programatico = c.getAcompanhamentoProgramatico();
        SituacaoAcompanhamento situacao = c.vigente() ? programatico.getSituacao()
                : (c.getMotivoSaida() == MotivoSaidaAcompanhamento.ALTA
                ? SituacaoAcompanhamento.CONCLUIDO : SituacaoAcompanhamento.ENCERRADO);
        return new AcompanhamentoSaudeMentalDto(c.getUuid(), pac.getUuid(), pac.getNome(),
                pac.getDataNascimento() == null ? null : Period.between(pac.getDataNascimento(), LocalDate.now()).getYears(),
                c.getUnidade().getUuid(), c.getUnidade().getNome(), programatico.getUuid(), situacao, c.getInicio(), c.getFim(),
                c.getMotivoSaida(), c.getObservacaoSaida(),
                r != null ? r.getEquipe().getUuid() : null, r != null ? r.getEquipe().getNome() : null,
                r != null ? r.getTecnico().getMatricula() : null, r != null ? r.getTecnico().getNome() : null,
                ultimaAvaliacao, false, null, List.of(), List.of(), List.of(),
                pts != null ? pts.getVersao() : null, pts != null ? pts.getRevisarEm() : null, TerapeuticoLeitura.revisaoVencida(pts), 0,
                List.of(), List.of(), List.of(), List.of(), List.of(), List.of(),
                c.getCasoAnterior() != null ? c.getCasoAnterior().getUuid() : null);
    }

    private static AcolhimentoSaudeMentalDto acolhimentoDto(AcolhimentoSaudeMental a, boolean liberado) {
        if (!liberado) {
            return new AcolhimentoSaudeMentalDto(a.getUuid(), a.getPaciente().getUuid(), a.getUnidade().getUuid(), a.getUnidade().getNome(),
                    a.getRealizadoEm(), false, null, null, null, null, null, null, null, null,
                    a.getAcompanhamento() != null ? a.getAcompanhamento().getUuid() : null, null);
        }
        return new AcolhimentoSaudeMentalDto(a.getUuid(), a.getPaciente().getUuid(), a.getUnidade().getUuid(), a.getUnidade().getNome(),
                a.getRealizadoEm(), true, a.getProfissional() != null ? a.getProfissional().getNome() : null,
                a.getAtendimento() != null ? a.getAtendimento().getUuid() : null, a.getMotivoProcura(), a.getNecessidadeImediata(),
                a.getVulnerabilidade(), a.getRiscoInicial(), a.getDesfecho(), a.getEncaminhamentoDestino(),
                a.getAcompanhamento() != null ? a.getAcompanhamento().getUuid() : null, null);
    }

    private static String texto(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }

    private AcompanhamentoSaudeMental buscarCaso(UUID uuid) {
        return casoRepository.findById(uuid).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar o caso em saúde mental " + uuid + " em nossos registros."));
    }

    private Paciente paciente(UUID uuid) {
        return pacienteRepository.findById(uuid).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar um paciente com o id " + uuid + " em nossos registros."));
    }

    private UnidadeDeSaude unidade(UUID uuid) {
        return unidadeDeSaudeRepository.findById(uuid).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar uma unidade de saúde com o id " + uuid + " em nossos registros."));
    }
}
