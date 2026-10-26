package com.edufelizardo.maissaudepublica.services.version1;

import com.edufelizardo.maissaudepublica.config.ContextoAuditoria;
import com.edufelizardo.maissaudepublica.config.UsuarioAutenticado;
import com.edufelizardo.maissaudepublica.exceptions.ResourceBadRequestException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceConflictException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceNotFoundException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceUnprocessableEntityException;
import com.edufelizardo.maissaudepublica.models.AcaoProgramatica;
import com.edufelizardo.maissaudepublica.models.AcaoSaudeBucal;
import com.edufelizardo.maissaudepublica.models.AcompanhamentoProgramatico;
import com.edufelizardo.maissaudepublica.models.Adscricao;
import com.edufelizardo.maissaudepublica.models.Equipe;
import com.edufelizardo.maissaudepublica.models.ExameLaboratorial;
import com.edufelizardo.maissaudepublica.models.MetaProgramatica;
import com.edufelizardo.maissaudepublica.models.Paciente;
import com.edufelizardo.maissaudepublica.models.PlanoTratamento;
import com.edufelizardo.maissaudepublica.models.Programa;
import com.edufelizardo.maissaudepublica.models.UnidadeDeSaude;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.MetaProgramaticaRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.IndicadorApuradoDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.MetaProgramaticaDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.SerieIndicadorDto;
import com.edufelizardo.maissaudepublica.models.enuns.AtividadeSaudeBucalColetiva;
import com.edufelizardo.maissaudepublica.models.enuns.IndicadorProgramatico;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoAcaoProgramatica;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoMetaProgramatica;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoPlanoTratamento;
import com.edufelizardo.maissaudepublica.models.enuns.TipoValorIndicador;
import com.edufelizardo.maissaudepublica.repositories.AcaoProgramaticaRepository;
import com.edufelizardo.maissaudepublica.repositories.AcaoSaudeBucalRepository;
import com.edufelizardo.maissaudepublica.repositories.AcompanhamentoProgramaticoRepository;
import com.edufelizardo.maissaudepublica.repositories.AtendimentoRepository;
import com.edufelizardo.maissaudepublica.repositories.EquipeRepository;
import com.edufelizardo.maissaudepublica.repositories.ExameLaboratorialRepository;
import com.edufelizardo.maissaudepublica.repositories.MetaProgramaticaRepository;
import com.edufelizardo.maissaudepublica.repositories.PlanoTratamentoRepository;
import com.edufelizardo.maissaudepublica.repositories.ProgramaRepository;
import com.edufelizardo.maissaudepublica.repositories.ResultadoExameRepository;
import com.edufelizardo.maissaudepublica.repositories.TratamentoProteticoRepository;
import com.edufelizardo.maissaudepublica.repositories.TriagemRepository;
import com.edufelizardo.maissaudepublica.repositories.UnidadeDeSaudeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Metas e indicadores programáticos, fatia P6 (ADR-0141, desenho na ADR-0109): o conjunto fixo de indicadores
 * calculados a partir do que o sistema registra (acompanhamento, atendimento, triagem, exame, ação programática, plano e
 * prótese odontológicos), as metas por período e a evolução mensal. Só números: nenhum paciente aparece.
 */
@Service
public class IndicadorProgramaticoService {

    private static final Pattern PA = Pattern.compile("^\\s*(\\d{2,3})\\s*[/xX]\\s*(\\d{2,3})\\s*$");
    private static final String GERENCIAR = "PROGRAMA.GERENCIAR";
    private static final ZoneId FUSO = ZoneId.systemDefault();

    @Autowired private ProgramaRepository programaRepository;
    @Autowired private MetaProgramaticaRepository metaRepository;
    @Autowired private UnidadeDeSaudeRepository unidadeRepository;
    @Autowired private ExameLaboratorialRepository exameRepository;
    @Autowired private AcompanhamentoProgramaticoRepository acompanhamentoRepository;
    @Autowired private AtendimentoRepository atendimentoRepository;
    @Autowired private TriagemRepository triagemRepository;
    @Autowired private ResultadoExameRepository resultadoRepository;
    @Autowired private AcaoProgramaticaRepository acaoRepository;
    @Autowired private AcaoSaudeBucalRepository acaoSaudeBucalRepository;
    @Autowired private PlanoTratamentoRepository planoRepository;
    @Autowired private TratamentoProteticoRepository proteseRepository;
    @Autowired private EquipeRepository equipeRepository;
    @Autowired private AdscricaoService adscricaoService;
    @Autowired private ControleDeAcesso controleDeAcesso;

    // ── Indicadores ────────────────────────────────────────────────────────────────────────────────

    /**
     * Os indicadores do programa no período (padrão: o mês corrente até hoje), na rede ou na unidade. Cada um traz a meta
     * da mesma unidade (ou da rede) que cobre o fim do período, e a situação em relação a ela.
     */
    @Transactional(readOnly = true)
    public List<IndicadorApuradoDto> indicadores(UUID programaId, LocalDate de, LocalDate ate, UUID unidadeId) {
        Programa programa = programaLegivel(programaId);
        LocalDate fim = ate != null ? ate : LocalDate.now();
        LocalDate inicio = de != null ? de : fim.withDayOfMonth(1);
        exigirPeriodo(inicio, fim);
        UnidadeDeSaude unidade = unidade(unidadeId);
        List<MetaProgramatica> metas = metaRepository.findDoPrograma(programaId);
        List<IndicadorApuradoDto> lista = new ArrayList<>();
        for (IndicadorProgramatico i : IndicadorProgramatico.values()) {
            if (!i.aplicaA(programa.getCodigo())) {
                continue;
            }
            MetaProgramatica meta = metaVigente(metas, i, unidade, fim);
            lista.add(dto(i, apurar(i, programa, unidade, inicio, fim, meta), inicio, fim, meta));
        }
        return lista;
    }

    /**
     * Para os indicadores da rede (BI3, ADR-0146): os indicadores do programa numa unidade (nula = a rede), com a meta da
     * unidade e, sem ela, a da rede. Mesma apuração do detalhe do programa; não confere acesso — quem chama já conferiu o
     * escopo e o programa sensível.
     */
    @Transactional(readOnly = true)
    public List<IndicadorApuradoDto> apurarParaRede(Programa programa, UnidadeDeSaude unidade, LocalDate inicio, LocalDate fim) {
        List<MetaProgramatica> metas = metaRepository.findDoPrograma(programa.getUuid());
        List<IndicadorApuradoDto> lista = new ArrayList<>();
        for (IndicadorProgramatico i : IndicadorProgramatico.values()) {
            if (!i.aplicaA(programa.getCodigo())) {
                continue;
            }
            MetaProgramatica meta = metaVigente(metas, i, unidade, fim);
            if (meta == null && unidade != null) {
                meta = metaVigente(metas, i, null, fim);
            }
            lista.add(dto(i, apurar(i, programa, unidade, inicio, fim, meta), inicio, fim, meta));
        }
        return lista;
    }

    /** A evolução mensal do indicador nos últimos meses (padrão 12), com a meta vigente de cada mês para o exame. */
    @Transactional(readOnly = true)
    public SerieIndicadorDto serie(UUID programaId, IndicadorProgramatico indicador, UUID unidadeId, Integer meses) {
        Programa programa = programaLegivel(programaId);
        if (!indicador.aplicaA(programa.getCodigo())) {
            throw new ResourceUnprocessableEntityException("O indicador " + indicador.nome() + " não se aplica ao programa " + programa.getNome() + ".");
        }
        int n = meses == null ? 12 : meses;
        if (n < 1 || n > 24) {
            throw new ResourceBadRequestException("A série vai de 1 a 24 meses.");
        }
        UnidadeDeSaude unidade = unidade(unidadeId);
        List<MetaProgramatica> metas = metaRepository.findDoPrograma(programaId);
        if (indicador == IndicadorProgramatico.COBERTURA) {
            return new SerieIndicadorDto(indicador, indicador.nome(), false, "A cobertura é apurada só hoje: a adscrição do passado não é reconstruída.", List.of());
        }
        List<SerieIndicadorDto.Ponto> pontos = new ArrayList<>();
        YearMonth atual = YearMonth.now();
        for (int k = n - 1; k >= 0; k--) {
            YearMonth m = atual.minusMonths(k);
            LocalDate inicio = m.atDay(1);
            LocalDate fim = k == 0 ? LocalDate.now() : m.atEndOfMonth();
            Apuracao a = apurar(indicador, programa, unidade, inicio, fim, metaVigente(metas, indicador, unidade, fim));
            pontos.add(new SerieIndicadorDto.Ponto(m.toString(), a.numerador, a.denominador, a.aplicavel ? valor(indicador, a) : null));
        }
        boolean algum = pontos.stream().anyMatch(p -> p.getValor() != null);
        return new SerieIndicadorDto(indicador, indicador.nome(), algum || indicador != IndicadorProgramatico.EXAME_NO_ALVO,
                algum || indicador != IndicadorProgramatico.EXAME_NO_ALVO ? null : "Cadastre a meta com o exame e o limite.", pontos);
    }

    /** O relatório do período em CSV (separador ;), para abrir na planilha. */
    @Transactional(readOnly = true)
    public String csv(UUID programaId, LocalDate de, LocalDate ate, UUID unidadeId) {
        Programa programa = programaRepository.findById(programaId).orElseThrow(() -> naoEncontrado(programaId));
        List<IndicadorApuradoDto> lista = indicadores(programaId, de, ate, unidadeId);
        StringBuilder sb = new StringBuilder("Programa;Indicador;De;Até;Numerador;Denominador;Valor;Tipo;Meta;Situação;Observação\n");
        for (IndicadorApuradoDto i : lista) {
            sb.append(String.join(";", programa.getNome(), i.getNome(), i.getDe().toString(), i.getAte().toString(), String.valueOf(i.getNumerador()),
                    i.getDenominador() == null ? "" : String.valueOf(i.getDenominador()), i.getValor() == null ? "" : i.getValor().toPlainString(),
                    i.getTipo().name(), i.getAlvo() == null ? "" : i.getAlvo().toPlainString(), i.getSituacao() == null ? "" : i.getSituacao().name(),
                    i.getMotivo() == null ? "" : i.getMotivo().replace(';', ','))).append('\n');
        }
        return sb.toString();
    }

    // ── Metas ──────────────────────────────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<MetaProgramaticaDto> metas(UUID programaId) {
        programaLegivel(programaId);
        return metaRepository.findDoPrograma(programaId).stream().map(IndicadorProgramaticoService::dto).toList();
    }

    /**
     * Cadastra a meta (PROGRAMA.GERENCIAR): o indicador do programa (422), o período válido (400), o alvo até 100 no
     * percentual (400), o exame e o limite no "exame no alvo" (400). Uma por indicador e unidade em cada período: períodos que se cruzam dão 409.
     */
    @Transactional
    public MetaProgramaticaDto criarMeta(UUID programaId, MetaProgramaticaRequestDto dto) {
        Programa programa = programaRepository.findById(programaId).orElseThrow(() -> naoEncontrado(programaId));
        controleDeAcesso.exigirAlguma(GERENCIAR);
        MetaProgramatica m = new MetaProgramatica();
        m.setPrograma(programa);
        m.setIndicador(dto.getIndicador());
        m.setUnidade(unidade(dto.getUnidadeId()));
        preencher(m, programa, dto);
        exigirSemSobreposicao(m, dto.getInicio(), dto.getFim());
        m.setCriadaEm(Instant.now());
        m.setRegistradoPorCpf(UsuarioAutenticado.cpf());
        metaRepository.save(m);
        ContextoAuditoria.registro(m.getUuid());
        return dto(m);
    }

    /** Ajusta o alvo, o período e o exame da meta; o indicador e a unidade não mudam. */
    @Transactional
    public MetaProgramaticaDto atualizarMeta(UUID metaId, MetaProgramaticaRequestDto dto) {
        MetaProgramatica m = metaRepository.findById(metaId).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar a meta " + metaId + " em nossos registros."));
        controleDeAcesso.exigirAlguma(GERENCIAR);
        if (dto.getIndicador() != m.getIndicador() || !Objects.equals(dto.getUnidadeId(), id(m.getUnidade()))) {
            throw new ResourceUnprocessableEntityException("O indicador e a unidade da meta não mudam: cadastre outra meta.");
        }
        exigirSemSobreposicao(m, dto.getInicio(), dto.getFim());
        preencher(m, m.getPrograma(), dto);
        metaRepository.save(m);
        ContextoAuditoria.registro(m.getUuid());
        return dto(m);
    }

    /** A meta vigente tem que ser uma só: duas do mesmo indicador e unidade não podem ter períodos que se cruzam (409). */
    private void exigirSemSobreposicao(MetaProgramatica m, LocalDate inicio, LocalDate fim) {
        if (inicio == null || fim == null) {
            return;
        }
        boolean cruza = metaRepository.findDoPrograma(m.getPrograma().getUuid()).stream().anyMatch(x -> !Objects.equals(x.getUuid(), m.getUuid())
                && x.getIndicador() == m.getIndicador() && Objects.equals(id(x.getUnidade()), id(m.getUnidade()))
                && !x.getInicio().isAfter(fim) && !inicio.isAfter(x.getFim()));
        if (cruza) {
            throw new ResourceConflictException("Já existe meta de " + m.getIndicador().nome() + " em período que se cruza com este para "
                    + nome(m.getUnidade()) + ".");
        }
    }

    @Transactional
    public void excluirMeta(UUID metaId) {
        MetaProgramatica m = metaRepository.findById(metaId).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar a meta " + metaId + " em nossos registros."));
        controleDeAcesso.exigirAlguma(GERENCIAR);
        ContextoAuditoria.registro(m.getUuid());
        metaRepository.delete(m);
    }

    private void preencher(MetaProgramatica m, Programa programa, MetaProgramaticaRequestDto dto) {
        if (!dto.getIndicador().aplicaA(programa.getCodigo())) {
            throw new ResourceUnprocessableEntityException("O indicador " + dto.getIndicador().nome() + " não se aplica ao programa " + programa.getNome() + ".");
        }
        exigirPeriodo(dto.getInicio(), dto.getFim());
        if (dto.getIndicador().tipo() == TipoValorIndicador.PERCENTUAL && dto.getAlvo().compareTo(BigDecimal.valueOf(100)) > 0) {
            throw new ResourceBadRequestException("O alvo de um indicador percentual vai até 100.");
        }
        ExameLaboratorial exame = null;
        if (dto.getIndicador().precisaExame()) {
            if (dto.getExameId() == null || (dto.getLimiteMinimo() == null && dto.getLimiteMaximo() == null)) {
                throw new ResourceBadRequestException("O indicador " + dto.getIndicador().nome() + " pede o exame e o limite (mínimo, máximo ou os dois).");
            }
            exame = exameRepository.findById(dto.getExameId()).orElseThrow(() -> new ResourceNotFoundException(
                    "Não foi possível encontrar o exame " + dto.getExameId() + " em nossos registros."));
        }
        m.setAlvo(dto.getAlvo());
        m.setInicio(dto.getInicio());
        m.setFim(dto.getFim());
        m.setExame(exame);
        m.setLimiteMinimo(exame != null ? dto.getLimiteMinimo() : null);
        m.setLimiteMaximo(exame != null ? dto.getLimiteMaximo() : null);
    }

    // ── Apuração ───────────────────────────────────────────────────────────────────────────────────

    private record Apuracao(boolean aplicavel, String motivo, long numerador, Long denominador) {
        static Apuracao de(long numerador, long denominador) {
            return new Apuracao(true, null, numerador, denominador);
        }

        static Apuracao contagem(long n) {
            return new Apuracao(true, null, n, null);
        }

        static Apuracao naoSeAplica(String motivo) {
            return new Apuracao(false, motivo, 0, null);
        }
    }

    private Apuracao apurar(IndicadorProgramatico i, Programa p, UnidadeDeSaude u, LocalDate de, LocalDate ate, MetaProgramatica meta) {
        return switch (i) {
            case ACOMPANHADOS_EM_DIA -> emDia(p, u, ate);
            case COBERTURA -> cobertura(p, u);
            case ACOES_REALIZADAS -> acoes(p, u, de, ate);
            case PA_CONTROLADA -> paControlada(p, u, de, ate);
            case EXAME_NO_ALVO -> exameNoAlvo(p, u, de, ate, meta);
            case PRE_NATAL_6_CONSULTAS -> preNatal(p, u, ate);
            case TRATAMENTO_ODONTOLOGICO_CONCLUIDO -> tratamentoConcluido(u, de, ate);
            case PROTESES_ENTREGUES -> Apuracao.contagem(proteseRepository.findEntreguesNoIntervalo(de, ate).stream()
                    .filter(t -> u == null || t.getUnidade().getUuid().equals(u.getUuid())).count());
            case ESCOVACAO_SUPERVISIONADA -> escovacao(p, u, de, ate);
        };
    }

    /** Os acompanhamentos vigentes na data (começaram até ela e não tinham terminado), na unidade responsável. */
    private List<AcompanhamentoProgramatico> vigentesEm(Programa p, UnidadeDeSaude u, LocalDate dia) {
        return acompanhamentoRepository.findTodosDoPrograma(p.getUuid()).stream()
                .filter(a -> !a.getInicio().isAfter(dia) && (a.getFim() == null || a.getFim().isAfter(dia)))
                .filter(a -> u == null || a.getUnidade().getUuid().equals(u.getUuid()))
                .toList();
    }

    private static Set<UUID> pacientes(Collection<AcompanhamentoProgramatico> lista) {
        Set<UUID> s = new HashSet<>();
        lista.forEach(a -> s.add(a.getPaciente().getUuid()));
        return s;
    }

    private Apuracao emDia(Programa p, UnidadeDeSaude u, LocalDate ate) {
        Integer dias = p.getPeriodicidade().dias();
        if (dias == null) {
            return Apuracao.naoSeAplica("O programa não tem periodicidade de acompanhamento.");
        }
        List<AcompanhamentoProgramatico> vigentes = vigentesEm(p, u, ate);
        if (vigentes.isEmpty()) {
            return Apuracao.de(0, 0);
        }
        Set<UUID> comAtendimento = new HashSet<>();
        for (Object[] l : atendimentoRepository.findRealizadosNoIntervalo(pacientes(vigentes), ate.minusDays(dias).atStartOfDay(), ate.plusDays(1).atStartOfDay())) {
            comAtendimento.add((UUID) l[0]);
        }
        return Apuracao.de(vigentes.stream().filter(a -> comAtendimento.contains(a.getPaciente().getUuid())).count(), vigentes.size());
    }

    private Apuracao cobertura(Programa p, UnidadeDeSaude u) {
        if (p.isGestante() || !p.getCondicoes().isEmpty()) {
            return Apuracao.naoSeAplica("O público é por condição ou gestação, que o cadastro não diz: não há denominador.");
        }
        Set<UUID> elegiveis = new HashSet<>();
        LocalDate hoje = LocalDate.now();
        for (Equipe e : equipeRepository.findAllByOrderByNomeAsc()) {
            if (u != null && !e.getUnidade().getUuid().equals(u.getUuid())) {
                continue;
            }
            for (Adscricao a : adscricaoService.vigentesDaEquipe(e.getUuid())) {
                Paciente pac = a.getPaciente();
                if (pac.isAtivo() && elegivel(p, pac, hoje)) {
                    elegiveis.add(pac.getUuid());
                }
            }
        }
        if (elegiveis.isEmpty()) {
            return Apuracao.naoSeAplica("Nenhum elegível adscrito às equipes" + (u != null ? " da unidade" : "") + ".");
        }
        Set<UUID> acompanhados = pacientes(vigentesEm(p, u, hoje));
        return Apuracao.de(elegiveis.stream().filter(acompanhados::contains).count(), elegiveis.size());
    }

    private static boolean elegivel(Programa p, Paciente pac, LocalDate hoje) {
        if (p.getPublicoSexo() != null && p.getPublicoSexo() != pac.getSexo()) {
            return false;
        }
        if (p.getIdadeMinima() == null && p.getIdadeMaxima() == null) {
            return true;
        }
        if (pac.getDataNascimento() == null) {
            return false;
        }
        int idade = Period.between(pac.getDataNascimento(), hoje).getYears();
        return (p.getIdadeMinima() == null || idade >= p.getIdadeMinima()) && (p.getIdadeMaxima() == null || idade <= p.getIdadeMaxima());
    }

    private Apuracao acoes(Programa p, UnidadeDeSaude u, LocalDate de, LocalDate ate) {
        List<AcaoProgramatica> previstas = acaoRepository.findDoProgramaNoIntervalo(p.getUuid(), de, ate).stream()
                .filter(a -> a.getSituacao() != SituacaoAcaoProgramatica.CANCELADA)
                .filter(a -> u == null || a.getUnidade().getUuid().equals(u.getUuid()))
                .toList();
        return Apuracao.de(previstas.stream().filter(a -> a.getSituacao() == SituacaoAcaoProgramatica.REALIZADA).count(), previstas.size());
    }

    private Apuracao paControlada(Programa p, UnidadeDeSaude u, LocalDate de, LocalDate ate) {
        Set<UUID> inscritos = pacientes(vigentesEm(p, u, ate));
        if (inscritos.isEmpty()) {
            return Apuracao.de(0, 0);
        }
        Map<UUID, Object[]> ultima = new HashMap<>();
        for (Object[] l : triagemRepository.findPressoesNoIntervalo(inscritos, de.atStartOfDay(), ate.plusDays(1).atStartOfDay())) {
            if (pressao((String) l[2]) == null) {
                continue;
            }
            Object[] atual = ultima.get((UUID) l[0]);
            if (atual == null || ((LocalDateTime) l[1]).isAfter((LocalDateTime) atual[1])) {
                ultima.put((UUID) l[0], l);
            }
        }
        long controlados = ultima.values().stream().map(l -> pressao((String) l[2])).filter(pa -> pa[0] < 140 && pa[1] < 90).count();
        return Apuracao.de(controlados, ultima.size());
    }

    /** "130/85" (ou 130x85) em sistólica e diastólica; fora do formato, nulo. */
    static int[] pressao(String texto) {
        if (texto == null) {
            return null;
        }
        Matcher m = PA.matcher(texto);
        return m.matches() ? new int[]{Integer.parseInt(m.group(1)), Integer.parseInt(m.group(2))} : null;
    }

    private Apuracao exameNoAlvo(Programa p, UnidadeDeSaude u, LocalDate de, LocalDate ate, MetaProgramatica meta) {
        if (meta == null || meta.getExame() == null) {
            return Apuracao.naoSeAplica("Cadastre a meta com o exame e o limite.");
        }
        Set<UUID> inscritos = pacientes(vigentesEm(p, u, ate));
        if (inscritos.isEmpty()) {
            return Apuracao.de(0, 0);
        }
        Map<UUID, Object[]> ultimo = new HashMap<>();
        for (Object[] l : resultadoRepository.findValoresNoIntervalo(meta.getExame().getUuid(), inscritos, inicioDo(de), inicioDo(ate.plusDays(1)))) {
            Object[] atual = ultimo.get((UUID) l[0]);
            if (atual == null || ((Instant) l[1]).isAfter((Instant) atual[1])) {
                ultimo.put((UUID) l[0], l);
            }
        }
        long noAlvo = ultimo.values().stream().map(l -> (BigDecimal) l[2])
                .filter(v -> (meta.getLimiteMinimo() == null || v.compareTo(meta.getLimiteMinimo()) >= 0)
                        && (meta.getLimiteMaximo() == null || v.compareTo(meta.getLimiteMaximo()) <= 0))
                .count();
        return Apuracao.de(noAlvo, ultimo.size());
    }

    private Apuracao preNatal(Programa p, UnidadeDeSaude u, LocalDate ate) {
        List<AcompanhamentoProgramatico> gestantes = vigentesEm(p, u, ate);
        if (gestantes.isEmpty()) {
            return Apuracao.de(0, 0);
        }
        LocalDate maisAntigo = gestantes.stream().map(AcompanhamentoProgramatico::getInicio).min(LocalDate::compareTo).orElse(ate);
        Map<UUID, List<LocalDateTime>> atendimentos = new HashMap<>();
        for (Object[] l : atendimentoRepository.findRealizadosNoIntervalo(pacientes(gestantes), maisAntigo.atStartOfDay(), ate.plusDays(1).atStartOfDay())) {
            atendimentos.computeIfAbsent((UUID) l[0], k -> new ArrayList<>()).add((LocalDateTime) l[2]);
        }
        long com6 = gestantes.stream().filter(a -> atendimentos.getOrDefault(a.getPaciente().getUuid(), List.of()).stream()
                .filter(d -> !d.toLocalDate().isBefore(a.getInicio())).count() >= 6).count();
        return Apuracao.de(com6, gestantes.size());
    }

    private Apuracao tratamentoConcluido(UnidadeDeSaude u, LocalDate de, LocalDate ate) {
        List<PlanoTratamento> abertos = planoRepository.findAbertosNoIntervalo(inicioDo(de), inicioDo(ate.plusDays(1))).stream()
                .filter(x -> u == null || x.getUnidade().getUuid().equals(u.getUuid()))
                .toList();
        Instant limite = inicioDo(ate.plusDays(1));
        return Apuracao.de(abertos.stream().filter(x -> x.getSituacao() == SituacaoPlanoTratamento.CONCLUIDO && x.getEncerradoEm().isBefore(limite)).count(),
                abertos.size());
    }

    private Apuracao escovacao(Programa p, UnidadeDeSaude u, LocalDate de, LocalDate ate) {
        long pessoas = 0;
        for (AcaoSaudeBucal d : acaoSaudeBucalRepository.findRealizadasNoIntervalo(de, ate)) {
            AcaoProgramatica a = d.getAcao();
            if (a.getPrograma() == null || !a.getPrograma().getUuid().equals(p.getUuid())
                    || !d.getAtividades().contains(AtividadeSaudeBucalColetiva.ESCOVACAO_SUPERVISIONADA)
                    || (u != null && !a.getUnidade().getUuid().equals(u.getUuid()))) {
                continue;
            }
            pessoas += a.getQuantidadeAtendida() != null ? a.getQuantidadeAtendida() : a.getParticipantes().size();
        }
        return Apuracao.contagem(pessoas);
    }

    // ── Apoio ──────────────────────────────────────────────────────────────────────────────────────

    /** Programa sensível: contagens pequenas identificam, então só quem acompanha (PROGRAMA.ACOMPANHAR). */
    private Programa programaLegivel(UUID programaId) {
        Programa p = programaRepository.findById(programaId).orElseThrow(() -> naoEncontrado(programaId));
        if (p.isSensivel()) {
            controleDeAcesso.exigirAlguma(AcompanhamentoService.ACOMPANHAR);
        }
        return p;
    }

    private static MetaProgramatica metaVigente(List<MetaProgramatica> metas, IndicadorProgramatico i, UnidadeDeSaude u, LocalDate dia) {
        return metas.stream()
                .filter(m -> m.getIndicador() == i && Objects.equals(id(m.getUnidade()), id(u)) && !m.getInicio().isAfter(dia) && !m.getFim().isBefore(dia))
                .findFirst().orElse(null);
    }

    private static IndicadorApuradoDto dto(IndicadorProgramatico i, Apuracao a, LocalDate de, LocalDate ate, MetaProgramatica meta) {
        BigDecimal valor = a.aplicavel ? valor(i, a) : null;
        SituacaoMetaProgramatica situacao = null;
        if (meta != null) {
            if (valor == null) {
                situacao = SituacaoMetaProgramatica.SEM_DADOS;
            } else if (valor.compareTo(meta.getAlvo()) >= 0) {
                situacao = SituacaoMetaProgramatica.ATINGIDA;
            } else if (valor.compareTo(meta.getAlvo().multiply(BigDecimal.valueOf(0.8))) >= 0) {
                situacao = SituacaoMetaProgramatica.EM_RISCO;
            } else {
                situacao = SituacaoMetaProgramatica.ABAIXO;
            }
        }
        return new IndicadorApuradoDto(i, i.nome(), i.descricao(), i.tipo(), a.aplicavel, a.motivo, a.numerador, a.denominador, valor, de, ate,
                meta != null ? meta.getUuid() : null, meta != null ? meta.getAlvo() : null, situacao);
    }

    /** O percentual com duas casas (sem denominador, nulo) ou a contagem. */
    private static BigDecimal valor(IndicadorProgramatico i, Apuracao a) {
        if (i.tipo() == TipoValorIndicador.QUANTIDADE) {
            return BigDecimal.valueOf(a.numerador);
        }
        if (a.denominador == null || a.denominador == 0) {
            return null;
        }
        return BigDecimal.valueOf(a.numerador * 100L).divide(BigDecimal.valueOf(a.denominador), 2, RoundingMode.HALF_UP);
    }

    static MetaProgramaticaDto dto(MetaProgramatica m) {
        return new MetaProgramaticaDto(m.getUuid(), m.getPrograma().getUuid(), m.getIndicador(), m.getIndicador().nome(), id(m.getUnidade()),
                m.getUnidade() != null ? m.getUnidade().getNome() : null, m.getAlvo(), m.getInicio(), m.getFim(),
                m.getExame() != null ? m.getExame().getUuid() : null, m.getExame() != null ? m.getExame().getNome() : null,
                m.getLimiteMinimo(), m.getLimiteMaximo());
    }

    private UnidadeDeSaude unidade(UUID unidadeId) {
        return unidadeId == null ? null : unidadeRepository.findById(unidadeId).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar uma unidade de saúde com o id " + unidadeId + " em nossos registros."));
    }

    private static void exigirPeriodo(LocalDate de, LocalDate ate) {
        if (de == null || ate == null || de.isAfter(ate)) {
            throw new ResourceBadRequestException("O período precisa ter o início antes do fim.");
        }
    }

    private static Instant inicioDo(LocalDate dia) {
        return dia.atStartOfDay(FUSO).toInstant();
    }

    private static UUID id(UnidadeDeSaude u) {
        return u != null ? u.getUuid() : null;
    }

    private static String nome(UnidadeDeSaude u) {
        return u != null ? u.getNome() : "a rede";
    }

    private static ResourceNotFoundException naoEncontrado(UUID programaId) {
        return new ResourceNotFoundException("Não foi possível encontrar o programa " + programaId + " em nossos registros.");
    }

}
