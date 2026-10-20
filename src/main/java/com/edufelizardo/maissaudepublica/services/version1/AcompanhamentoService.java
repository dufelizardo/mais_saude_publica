package com.edufelizardo.maissaudepublica.services.version1;

import com.edufelizardo.maissaudepublica.config.ContextoAuditoria;
import com.edufelizardo.maissaudepublica.config.UsuarioAutenticado;
import com.edufelizardo.maissaudepublica.exceptions.ResourceBadRequestException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceConflictException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceNotFoundException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceUnprocessableEntityException;
import com.edufelizardo.maissaudepublica.models.AcompanhamentoProgramatico;
import com.edufelizardo.maissaudepublica.models.Adscricao;
import com.edufelizardo.maissaudepublica.models.Equipe;
import com.edufelizardo.maissaudepublica.models.EventoAcompanhamento;
import com.edufelizardo.maissaudepublica.models.Paciente;
import com.edufelizardo.maissaudepublica.models.Profissional;
import com.edufelizardo.maissaudepublica.models.Programa;
import com.edufelizardo.maissaudepublica.models.UnidadeDeSaude;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.AcompanhamentoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.EncerramentoAcompanhamentoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.SituacaoAcompanhamentoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.AcessoProntuarioDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.AcompanhamentoDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.AcompanhamentosDoPacienteDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.AdscricaoDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.InscritosProgramaDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.ResumoAcompanhamentosDto;
import com.edufelizardo.maissaudepublica.models.enuns.MotivoSaidaAcompanhamento;
import com.edufelizardo.maissaudepublica.models.enuns.Sexo;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoAcompanhamento;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoPrograma;
import com.edufelizardo.maissaudepublica.repositories.AcompanhamentoProgramaticoRepository;
import com.edufelizardo.maissaudepublica.repositories.AcompanhamentoSaudeMentalRepository;
import com.edufelizardo.maissaudepublica.repositories.AtendimentoRepository;
import com.edufelizardo.maissaudepublica.repositories.EquipeRepository;
import com.edufelizardo.maissaudepublica.repositories.EventoAcompanhamentoRepository;
import com.edufelizardo.maissaudepublica.repositories.LotacaoRepository;
import com.edufelizardo.maissaudepublica.repositories.PacienteRepository;
import com.edufelizardo.maissaudepublica.repositories.ParticipacaoProgramaRepository;
import com.edufelizardo.maissaudepublica.repositories.ProfissionalRepository;
import com.edufelizardo.maissaudepublica.repositories.ProgramaRepository;
import com.edufelizardo.maissaudepublica.repositories.UnidadeDeSaudeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Acompanhamento programático (ADR-0119, fatia P2 da ADR-0109): inscrever o paciente num programa, marcar faltoso,
 * encerrar, e ler os programas do paciente e os inscritos de um programa. Estar num programa é dado de saúde: a leitura
 * do paciente segue o prontuário (vínculo da ADR-0076 e auditoria de leitura), e programa sensível só aparece para quem
 * acompanha na unidade responsável.
 */
@Service
public class AcompanhamentoService {

    public static final String ACOMPANHAR = "PROGRAMA.ACOMPANHAR";
    static final String PRONTUARIO = "PRONTUARIO.CONSULTAR";

    @Autowired
    private AcompanhamentoProgramaticoRepository acompanhamentoRepository;

    @Autowired
    private EventoAcompanhamentoRepository eventoRepository;

    @Autowired
    private ProgramaRepository programaRepository;

    @Autowired
    private ParticipacaoProgramaRepository participacaoRepository;

    @Autowired
    private PacienteRepository pacienteRepository;

    @Autowired
    private UnidadeDeSaudeRepository unidadeDeSaudeRepository;

    @Autowired
    private EquipeRepository equipeRepository;

    @Autowired
    private ProfissionalRepository profissionalRepository;

    @Autowired
    private LotacaoRepository lotacaoRepository;

    @Autowired
    private AtendimentoRepository atendimentoRepository;

    @Autowired
    private AcompanhamentoSaudeMentalRepository casoSaudeMentalRepository;

    @Autowired
    private ControleDeAcesso controleDeAcesso;

    @Autowired
    private AutorizacaoService autorizacaoService;

    @Autowired
    private VinculoAssistencialService vinculoService;

    // ── Escrita ────────────────────────────────────────────────────────────────────────────────────

    /**
     * Inscreve o paciente. A unidade responsável precisa oferecer o programa; um acompanhamento vigente por paciente e
     * programa. Fora do público-alvo só avisa: quem decide é o profissional.
     */
    @Transactional
    public AcompanhamentoDto inscrever(UUID programaId, AcompanhamentoRequestDto dto) {
        Programa programa = programa(programaId);
        UnidadeDeSaude unidade = unidadeDeSaudeRepository.findById(dto.getUnidadeId()).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar uma unidade de saúde com o id " + dto.getUnidadeId() + " em nossos registros."));
        controleDeAcesso.exigir(ACOMPANHAR, unidade);
        Paciente paciente = pacienteRepository.findById(dto.getPacienteId()).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar um paciente com o id " + dto.getPacienteId() + " em nossos registros."));
        ContextoAuditoria.paciente(paciente.getUuid());
        if (programa.getSituacao() != SituacaoPrograma.ATIVO) {
            throw new ResourceUnprocessableEntityException("O programa " + programa.getNome() + " está "
                    + programa.getSituacao().name().toLowerCase() + " e não recebe inscrições.");
        }
        if (!paciente.isAtivo()) {
            throw new ResourceUnprocessableEntityException("O cadastro do paciente está inativo.");
        }
        boolean oferece = participacaoRepository.findDoPrograma(programa.getUuid()).stream()
                .anyMatch(x -> x.vigente() && x.getUnidade().getUuid().equals(unidade.getUuid()));
        if (!oferece) {
            throw new ResourceUnprocessableEntityException(unidade.getNome() + " não oferece o programa " + programa.getNome()
                    + "; inclua a unidade no programa antes.");
        }
        if (acompanhamentoRepository.existsByPaciente_UuidAndPrograma_UuidAndFimIsNull(paciente.getUuid(), programa.getUuid())) {
            throw new ResourceConflictException("O paciente já é acompanhado no programa " + programa.getNome() + ".");
        }
        Equipe equipe = null;
        if (dto.getEquipeId() != null) {
            equipe = equipeRepository.findById(dto.getEquipeId()).orElseThrow(() -> new ResourceNotFoundException(
                    "Não foi possível encontrar uma equipe com o id " + dto.getEquipeId() + " em nossos registros."));
            if (!equipe.getUnidade().getUuid().equals(unidade.getUuid())) {
                throw new ResourceUnprocessableEntityException("A equipe " + equipe.getNome() + " não é da " + unidade.getNome() + ".");
            }
        }
        Profissional profissional = profissionalResponsavel(dto.getProfissionalMatricula(), unidade);
        LocalDate inicio = dto.getInicio() != null ? dto.getInicio() : LocalDate.now();
        if (inicio.isAfter(LocalDate.now())) {
            throw new ResourceBadRequestException("O início não pode ser no futuro.");
        }
        AcompanhamentoProgramatico a = acompanhamentoRepository.save(new AcompanhamentoProgramatico(null, paciente, programa, unidade,
                equipe, profissional, inicio, null, SituacaoAcompanhamento.ATIVO, null, null, UsuarioAutenticado.cpf()));
        evento(a, null, SituacaoAcompanhamento.ATIVO, "Inscrição no programa.");
        ContextoAuditoria.registro(a.getUuid());
        ContextoAuditoria.detalhe("Inscrição no programa " + programa.getCodigo() + ".");
        return dto(a, ultimos(List.of(a)), eventoRepository.findDosAcompanhamentos(List.of(a.getUuid())));
    }

    /** Marca faltoso ou volta a ativo, com motivo. Encerrar é pelo encerramento. */
    @Transactional
    public AcompanhamentoDto mudarSituacao(UUID acompanhamentoId, SituacaoAcompanhamentoRequestDto dto) {
        AcompanhamentoProgramatico a = acompanhamento(acompanhamentoId);
        controleDeAcesso.exigir(ACOMPANHAR, a.getUnidade());
        ContextoAuditoria.paciente(a.getPaciente().getUuid());
        if (!a.vigente()) {
            throw new ResourceUnprocessableEntityException("Este acompanhamento já foi encerrado.");
        }
        if (!dto.getSituacao().vigente()) {
            throw new ResourceBadRequestException("Aqui a situação é ATIVO ou FALTOSO; para encerrar, use o encerramento.");
        }
        if (dto.getSituacao() == a.getSituacao()) {
            throw new ResourceUnprocessableEntityException("O acompanhamento já está " + a.getSituacao().name().toLowerCase() + ".");
        }
        SituacaoAcompanhamento anterior = a.getSituacao();
        a.setSituacao(dto.getSituacao());
        acompanhamentoRepository.save(a);
        evento(a, anterior, dto.getSituacao(), dto.getMotivo().trim());
        ContextoAuditoria.detalhe(anterior + " → " + dto.getSituacao() + ".");
        return dto(a, ultimos(List.of(a)), eventoRepository.findDosAcompanhamentos(List.of(a.getUuid())));
    }

    /** Saída do programa: alta conclui; transferência, óbito, abandono… encerram. O registro fica no histórico. */
    @Transactional
    public AcompanhamentoDto encerrar(UUID acompanhamentoId, EncerramentoAcompanhamentoRequestDto dto) {
        AcompanhamentoProgramatico a = acompanhamento(acompanhamentoId);
        controleDeAcesso.exigir(ACOMPANHAR, a.getUnidade());
        return encerrar(a, dto);
    }

    /**
     * A saída do programa pedida pelo caso em saúde mental, que já conferiu o acesso (ADR-0120). Na transferência do
     * cuidado (ADR-0122), quem aceita no destino não tem a permissão na unidade de origem.
     */
    @Transactional
    AcompanhamentoDto encerrarPelaSaudeMental(UUID acompanhamentoId, EncerramentoAcompanhamentoRequestDto dto) {
        return encerrar(acompanhamento(acompanhamentoId), dto);
    }

    private AcompanhamentoDto encerrar(AcompanhamentoProgramatico a, EncerramentoAcompanhamentoRequestDto dto) {
        ContextoAuditoria.paciente(a.getPaciente().getUuid());
        if (!a.vigente()) {
            throw new ResourceUnprocessableEntityException("Este acompanhamento já foi encerrado.");
        }
        // O caso em saúde mental é quem encerra o seu acompanhamento do programa (ADR-0120).
        if (casoSaudeMentalRepository.existsByAcompanhamentoProgramatico_UuidAndFimIsNull(a.getUuid())) {
            throw new ResourceUnprocessableEntityException("Este acompanhamento tem um caso em saúde mental em aberto; "
                    + "registre a saída pela tela de Saúde Mental.");
        }
        LocalDate fim = dto.getFim() != null ? dto.getFim() : LocalDate.now();
        if (fim.isBefore(a.getInicio()) || fim.isAfter(LocalDate.now())) {
            throw new ResourceBadRequestException("A saída precisa ser entre o início (" + a.getInicio() + ") e hoje.");
        }
        SituacaoAcompanhamento anterior = a.getSituacao();
        SituacaoAcompanhamento nova = dto.getMotivoSaida() == MotivoSaidaAcompanhamento.ALTA ? SituacaoAcompanhamento.CONCLUIDO
                : SituacaoAcompanhamento.ENCERRADO;
        a.setFim(fim);
        a.setSituacao(nova);
        a.setMotivoSaida(dto.getMotivoSaida());
        a.setObservacaoSaida(dto.getObservacao().trim());
        acompanhamentoRepository.save(a);
        evento(a, anterior, nova, dto.getMotivoSaida() + ": " + dto.getObservacao().trim());
        ContextoAuditoria.detalhe("Saída do programa " + a.getPrograma().getCodigo() + " (" + dto.getMotivoSaida() + ").");
        return dto(a, ultimos(List.of(a)), eventoRepository.findDosAcompanhamentos(List.of(a.getUuid())));
    }

    // ── Leitura ────────────────────────────────────────────────────────────────────────────────────

    /**
     * Os programas do paciente, com as regras do prontuário: vínculo assistencial (ou acesso justificado) e auditoria de
     * leitura. Programa sensível só aparece para quem tem {@value #ACOMPANHAR} na unidade responsável; os outros contam
     * em {@code restritos}.
     */
    @Transactional(readOnly = true)
    public AcompanhamentosDoPacienteDto doPaciente(UUID pacienteId) {
        if (!pacienteRepository.existsById(pacienteId)) {
            throw new ResourceNotFoundException("Não foi possível encontrar um paciente com o id " + pacienteId + " em nossos registros.");
        }
        ContextoAuditoria.paciente(pacienteId);
        AcessoProntuarioDto acesso = vinculoService.exigirVinculo(pacienteId);
        List<AcompanhamentoProgramatico> todos = acompanhamentoRepository.findDoPaciente(pacienteId);
        List<AcompanhamentoProgramatico> visiveis = todos.stream().filter(this::podeVer).toList();
        int restritos = todos.size() - visiveis.size();
        Map<String, LocalDateTime> ultimos = ultimos(visiveis);
        Map<UUID, List<EventoAcompanhamento>> eventos = eventoRepository.findDosAcompanhamentos(visiveis.stream()
                .map(AcompanhamentoProgramatico::getUuid).toList()).stream().collect(Collectors.groupingBy(e -> e.getAcompanhamento().getUuid()));
        List<AcompanhamentoDto> dtos = visiveis.stream().map(a -> dto(a, ultimos, eventos.getOrDefault(a.getUuid(), List.of()))).toList();
        return new AcompanhamentosDoPacienteDto(pacienteId, acesso,
                dtos.stream().filter(d -> d.getFim() == null).sorted(Comparator.comparing(AcompanhamentoDto::getProgramaNome)).toList(),
                dtos.stream().filter(d -> d.getFim() != null).sorted(Comparator.comparing(AcompanhamentoDto::getFim).reversed()).toList(),
                restritos);
    }

    /** Os inscritos vigentes de um programa nas unidades do escopo de quem lê; faltosos e atrasados primeiro. */
    @Transactional(readOnly = true)
    public InscritosProgramaDto inscritos(UUID programaId, UUID unidadeId, SituacaoAcompanhamento situacao) {
        Programa programa = programa(programaId);
        ContextoAuditoria.registro(programa.getUuid());
        List<AcompanhamentoProgramatico> vigentes = controleDeAcesso.filtrar(acompanhamentoRepository.findVigentesDoPrograma(programaId).stream(),
                        a -> java.util.stream.Stream.of(a.getUnidade()), ACOMPANHAR)
                .filter(a -> unidadeId == null || a.getUnidade().getUuid().equals(unidadeId))
                .toList();
        Map<String, LocalDateTime> ultimos = ultimos(vigentes);
        List<AcompanhamentoDto> todos = vigentes.stream().map(a -> dto(a, ultimos, List.of())).toList();
        List<AcompanhamentoDto> lista = todos.stream()
                .filter(d -> situacao == null || d.getSituacao() == situacao)
                .sorted(Comparator.comparing((AcompanhamentoDto d) -> d.getSituacao() != SituacaoAcompanhamento.FALTOSO)
                        .thenComparing(d -> !d.isAtrasado())
                        .thenComparing(AcompanhamentoDto::getPacienteNome, String.CASE_INSENSITIVE_ORDER))
                .toList();
        return new InscritosProgramaDto(programaId, todos.size(),
                todos.stream().filter(d -> d.getSituacao() == SituacaoAcompanhamento.FALTOSO).count(),
                todos.stream().filter(AcompanhamentoDto::isAtrasado).count(), lista);
    }

    /** Agregado da rede para a tela de Pacientes: não identifica ninguém. */
    @Transactional(readOnly = true)
    public ResumoAcompanhamentosDto resumo() {
        List<AcompanhamentoProgramatico> vigentes = acompanhamentoRepository.findVigentes();
        return new ResumoAcompanhamentosDto(vigentes.stream().map(a -> a.getPaciente().getUuid()).distinct().count(), vigentes.size(),
                vigentes.stream().filter(a -> a.getSituacao() == SituacaoAcompanhamento.FALTOSO).count());
    }

    /**
     * Inscritos, faltosos e atrasados por programa, para os cartões do catálogo (só números). Com unidade ou equipe, conta
     * só os acompanhamentos dela.
     */
    @Transactional(readOnly = true)
    public Map<UUID, long[]> contagensPorPrograma(UUID unidadeId, UUID equipeId) {
        List<AcompanhamentoProgramatico> vigentes = acompanhamentoRepository.findVigentes().stream()
                .filter(a -> unidadeId == null || a.getUnidade().getUuid().equals(unidadeId))
                .filter(a -> equipeId == null || (a.getEquipe() != null && a.getEquipe().getUuid().equals(equipeId)))
                .toList();
        Map<String, LocalDateTime> ultimos = ultimos(vigentes);
        Map<UUID, long[]> contagens = new HashMap<>();
        for (AcompanhamentoProgramatico a : vigentes) {
            long[] c = contagens.computeIfAbsent(a.getPrograma().getUuid(), k -> new long[3]);
            c[0]++;
            if (a.getSituacao() == SituacaoAcompanhamento.FALTOSO) {
                c[1]++;
            }
            if (atrasado(a, ultimos)) {
                c[2]++;
            }
        }
        return contagens;
    }

    // ── Apoio ──────────────────────────────────────────────────────────────────────────────────────

    /** Programa sensível: só quem acompanha na unidade responsável (com a autorização ligada). */
    private boolean podeVer(AcompanhamentoProgramatico a) {
        if (!a.getPrograma().isSensivel() || !controleDeAcesso.ativo()) {
            return true;
        }
        String cpf = UsuarioAutenticado.cpf();
        return cpf != null && autorizacaoService.tem(cpf, ACOMPANHAR, a.getUnidade().getUuid());
    }

    private Profissional profissionalResponsavel(String matricula, UnidadeDeSaude unidade) {
        if (matricula == null || matricula.isBlank()) {
            return null;
        }
        String m = matricula.trim();
        Profissional p = profissionalRepository.findByMatricula(m).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar um profissional com a matrícula " + m + " em nossos registros."));
        boolean lotado = lotacaoRepository.findByProfissional_MatriculaOrderByDataInicioDesc(m).stream()
                .anyMatch(l -> l.getDataFim() == null && l.getUnidade() != null && l.getUnidade().getUuid().equals(unidade.getUuid()));
        if (!p.isAtivo() || !lotado) {
            throw new ResourceUnprocessableEntityException(p.getNome() + " não tem lotação vigente na " + unidade.getNome()
                    + " e não pode ser o responsável.");
        }
        return p;
    }

    private void evento(AcompanhamentoProgramatico a, SituacaoAcompanhamento anterior, SituacaoAcompanhamento nova, String motivo) {
        eventoRepository.save(new EventoAcompanhamento(null, a, Instant.now(), anterior, nova,
                motivo.length() > 500 ? motivo.substring(0, 500) : motivo, UsuarioAutenticado.cpf()));
    }

    /** Último atendimento realizado por paciente e unidade, chave "paciente|unidade". */
    private Map<String, LocalDateTime> ultimos(Collection<AcompanhamentoProgramatico> acompanhamentos) {
        List<UUID> pacientes = acompanhamentos.stream().map(a -> a.getPaciente().getUuid()).distinct().toList();
        if (pacientes.isEmpty()) {
            return Map.of();
        }
        Map<String, LocalDateTime> m = new HashMap<>();
        for (Object[] linha : atendimentoRepository.findUltimosRealizados(pacientes)) {
            m.put(linha[0] + "|" + linha[1], (LocalDateTime) linha[2]);
        }
        return m;
    }

    /** Sem atendimento na unidade responsável há mais tempo que a periodicidade (contando do início, se nunca veio). */
    private static boolean atrasado(AcompanhamentoProgramatico a, Map<String, LocalDateTime> ultimos) {
        Integer dias = a.getPrograma().getPeriodicidade().dias();
        if (!a.vigente() || dias == null) {
            return false;
        }
        LocalDateTime ultimo = ultimos.get(a.getPaciente().getUuid() + "|" + a.getUnidade().getUuid());
        LocalDate base = ultimo != null ? ultimo.toLocalDate() : a.getInicio();
        return base.plusDays(dias).isBefore(LocalDate.now());
    }

    /** Por que o paciente está fora do público-alvo (idade e sexo); gestante e condições o cadastro não diz. */
    static String avisoPublico(Programa p, Paciente paciente) {
        Integer idade = paciente.getDataNascimento() == null ? null : Period.between(paciente.getDataNascimento(), LocalDate.now()).getYears();
        String publico = ProgramaService.publico(p, false).toLowerCase();
        if (idade != null && p.getIdadeMinima() != null && idade < p.getIdadeMinima()) {
            return idade + " anos; o programa é para " + publico + ".";
        }
        if (idade != null && p.getIdadeMaxima() != null && idade > p.getIdadeMaxima()) {
            return idade + " anos; o programa é para " + publico + ".";
        }
        if (p.getPublicoSexo() != null && paciente.getSexo() != null && paciente.getSexo() != Sexo.IGNORADO
                && paciente.getSexo() != p.getPublicoSexo()) {
            return "Sexo " + paciente.getSexo().name().toLowerCase() + "; o programa é para " + publico + ".";
        }
        return null;
    }

    private AcompanhamentoDto dto(AcompanhamentoProgramatico a, Map<String, LocalDateTime> ultimos, List<EventoAcompanhamento> eventos) {
        Paciente pac = a.getPaciente();
        Programa p = a.getPrograma();
        LocalDateTime ultimo = ultimos.get(pac.getUuid() + "|" + a.getUnidade().getUuid());
        return new AcompanhamentoDto(a.getUuid(), pac.getUuid(), pac.getNome(),
                pac.getDataNascimento() == null ? null : Period.between(pac.getDataNascimento(), LocalDate.now()).getYears(),
                p.getUuid(), p.getCodigo(), p.getNome(), p.isSensivel(), a.getUnidade().getUuid(), a.getUnidade().getNome(),
                a.getEquipe() != null ? a.getEquipe().getUuid() : null, a.getEquipe() != null ? a.getEquipe().getNome() : null,
                a.getProfissional() != null ? a.getProfissional().getMatricula() : null,
                a.getProfissional() != null ? a.getProfissional().getNome() : null,
                a.getInicio(), a.getFim(), a.getSituacao(), a.getMotivoSaida(), a.getObservacaoSaida(),
                ultimo != null ? ultimo.toLocalDate() : null, atrasado(a, ultimos), a.vigente() ? avisoPublico(p, pac) : null,
                eventos.stream().filter(e -> Objects.equals(e.getAcompanhamento().getUuid(), a.getUuid()))
                        .map(e -> new AcompanhamentoDto.Evento(e.getOcorridoEm(), e.getSituacaoAnterior(), e.getSituacao(), e.getMotivo(),
                                e.getRegistradoPorCpf())).toList());
    }

    private AcompanhamentoProgramatico acompanhamento(UUID uuid) {
        return acompanhamentoRepository.findById(uuid).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar o acompanhamento " + uuid + " em nossos registros."));
    }

    // ── Elegíveis (ADR-0128) ───────────────────────────────────────────────────────────────────────

    @Autowired
    private AdscricaoService adscricaoService;

    @Autowired
    private EscopoTerritorial escopo;

    /**
     * Os adscritos da equipe (ou de uma microárea dela) que cabem no público do programa e não estão inscritos nele. Avalia
     * idade e sexo; gestação e condição clínica ficam como critérios não avaliados (não há registro estruturado delas).
     */
    @Transactional(readOnly = true)
    public AdscricaoDto.Elegiveis elegiveis(UUID programaId, UUID equipeId, UUID microareaId) {
        Programa programa = programa(programaId);
        Equipe equipe = equipeRepository.findById(equipeId).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar uma equipe com o id " + equipeId + " em nossos registros."));
        controleDeAcesso.exigir(ACOMPANHAR, equipe.getUnidade());
        // Escopo territorial (ADR-0129): só a própria equipe, e o ACS só a sua microárea.
        escopo.exigir(equipe);
        java.util.Optional<EscopoTerritorial.Alcance> alcance = escopo.alcance();
        ContextoAuditoria.registro(programa.getUuid());
        List<String> avaliados = new ArrayList<>();
        if (programa.getIdadeMinima() != null || programa.getIdadeMaxima() != null) {
            avaliados.add("idade " + (programa.getIdadeMinima() != null ? "de " + programa.getIdadeMinima() + " " : "")
                    + (programa.getIdadeMaxima() != null ? "até " + programa.getIdadeMaxima() + " " : "") + "anos");
        }
        if (programa.getPublicoSexo() != null) {
            avaliados.add("sexo " + programa.getPublicoSexo());
        }
        List<String> naoAvaliados = new ArrayList<>();
        if (programa.isGestante()) {
            naoAvaliados.add("gestação");
        }
        if (!programa.getCondicoes().isEmpty()) {
            naoAvaliados.add("condição (" + String.join(", ", programa.getCondicoes()) + ")");
        }
        boolean porIdade = programa.getIdadeMinima() != null || programa.getIdadeMaxima() != null;
        LocalDate hoje = LocalDate.now();
        int semNascimento = 0;
        List<AdscricaoDto.Elegivel> lista = new ArrayList<>();
        for (Adscricao a : adscricaoService.vigentesDaEquipe(equipe.getUuid())) {
            if (microareaId != null && (a.getMicroarea() == null || !a.getMicroarea().getUuid().equals(microareaId))) {
                continue;
            }
            if (alcance.isPresent() && !EscopoTerritorial.alcanca(alcance.get(), a)) {
                continue;
            }
            Paciente p = a.getPaciente();
            if (!p.isAtivo() || (programa.getPublicoSexo() != null && programa.getPublicoSexo() != p.getSexo())) {
                continue;
            }
            Integer idade = p.getDataNascimento() == null ? null : Period.between(p.getDataNascimento(), hoje).getYears();
            if (porIdade) {
                if (idade == null) {
                    semNascimento++;
                    continue;
                }
                if ((programa.getIdadeMinima() != null && idade < programa.getIdadeMinima())
                        || (programa.getIdadeMaxima() != null && idade > programa.getIdadeMaxima())) {
                    continue;
                }
            }
            if (acompanhamentoRepository.existsByPaciente_UuidAndPrograma_UuidAndFimIsNull(p.getUuid(), programa.getUuid())) {
                continue;
            }
            lista.add(new AdscricaoDto.Elegivel(p.getUuid(), p.getNome(), idade, p.getSexo(),
                    a.getMicroarea() != null ? a.getMicroarea().getCodigo() : null, adscricaoService.acsDaMicroarea(a.getMicroarea())));
        }
        lista.sort(Comparator.comparing((AdscricaoDto.Elegivel e) -> Objects.toString(e.getMicroareaCodigo(), "~"))
                .thenComparing(e -> e.getNome(), String.CASE_INSENSITIVE_ORDER));
        return new AdscricaoDto.Elegiveis(programa.getUuid(), equipe.getUuid(), microareaId,
                avaliados, naoAvaliados, semNascimento, lista);
    }

    private Programa programa(UUID uuid) {
        return programaRepository.findById(uuid).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar um programa com o id " + uuid + " em nossos registros."));
    }
}
