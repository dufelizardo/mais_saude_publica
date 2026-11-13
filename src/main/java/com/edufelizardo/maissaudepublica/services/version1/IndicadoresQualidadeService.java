package com.edufelizardo.maissaudepublica.services.version1;

import com.edufelizardo.maissaudepublica.config.ContextoAuditoria;
import com.edufelizardo.maissaudepublica.config.UsuarioAutenticado;
import com.edufelizardo.maissaudepublica.exceptions.ResourceBadRequestException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceConflictException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceNotFoundException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceUnprocessableEntityException;
import com.edufelizardo.maissaudepublica.models.AuditoriaProtocolo;
import com.edufelizardo.maissaudepublica.models.ChecklistProtocolo;
import com.edufelizardo.maissaudepublica.models.IncidenteSeguranca;
import com.edufelizardo.maissaudepublica.models.Internacao;
import com.edufelizardo.maissaudepublica.models.MetaQualidade;
import com.edufelizardo.maissaudepublica.models.NaoConformidade;
import com.edufelizardo.maissaudepublica.models.Setor;
import com.edufelizardo.maissaudepublica.models.UnidadeDeSaude;
import com.edufelizardo.maissaudepublica.models.VersaoChecklistProtocolo;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.AuditoriaProtocoloRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.ChecklistProtocoloRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.MetaQualidadeRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.IndicadoresQualidadeDtos;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.RelatorioDto;
import com.edufelizardo.maissaudepublica.models.enuns.GrauDano;
import com.edufelizardo.maissaudepublica.models.enuns.IndicadorQualidade;
import com.edufelizardo.maissaudepublica.models.enuns.NaturezaIncidente;
import com.edufelizardo.maissaudepublica.models.enuns.RespostaRoteiro;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoIncidente;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoNaoConformidade;
import com.edufelizardo.maissaudepublica.models.enuns.StatusAgendamento;
import com.edufelizardo.maissaudepublica.models.enuns.StatusInternacao;
import com.edufelizardo.maissaudepublica.models.enuns.TipoIncidente;
import com.edufelizardo.maissaudepublica.models.enuns.TipoRelatorio;
import com.edufelizardo.maissaudepublica.repositories.AgendamentoRepository;
import com.edufelizardo.maissaudepublica.repositories.AtendimentoRepository;
import com.edufelizardo.maissaudepublica.repositories.AuditoriaProtocoloRepository;
import com.edufelizardo.maissaudepublica.repositories.ChecklistProtocoloRepository;
import com.edufelizardo.maissaudepublica.repositories.ConsultaRepository;
import com.edufelizardo.maissaudepublica.repositories.IncidenteSegurancaRepository;
import com.edufelizardo.maissaudepublica.repositories.InternacaoRepository;
import com.edufelizardo.maissaudepublica.repositories.MetaQualidadeRepository;
import com.edufelizardo.maissaudepublica.repositories.NaoConformidadeRepository;
import com.edufelizardo.maissaudepublica.repositories.SetorRepository;
import com.edufelizardo.maissaudepublica.repositories.UnidadeDeSaudeRepository;
import com.edufelizardo.maissaudepublica.repositories.VersaoChecklistProtocoloRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Indicadores de qualidade com metas e auditoria de adesão aos protocolos, fatia Q3 da Qualidade (ADR-0205, desenho na ADR-0202). Os
 * indicadores são calculados dos registros da rede (internações, incidentes da Q1, atendimentos, agenda e auditorias), sem tabela; a meta
 * por unidade é histórico de vigência; sem denominador o indicador fica "sem base", nunca zero.
 */
@Service
public class IndicadoresQualidadeService {

    static final String GERIR = NaoConformidadeService.GERIR;
    static final String ANALISAR = IncidenteSegurancaService.ANALISAR;
    static final String INDICADOR = "INDICADOR.CONSULTAR";
    static final int MESES_DA_SERIE = 12;
    static final int DIAS_REINTERNACAO = 30;
    private static final long ESPERA_MAXIMA_VALIDA = 24 * 60;
    private static final ZoneId ZONA = ZoneId.of("America/Sao_Paulo");
    private static final ObjectMapper JSON = new ObjectMapper();
    private static final BigDecimal MIL = BigDecimal.valueOf(1000);
    private static final BigDecimal CEM = BigDecimal.valueOf(100);

    /** O resultado de um indicador: o valor (nulo sem base), o numerador e o denominador. */
    record Valor(BigDecimal valor, BigDecimal numerador, BigDecimal denominador) {
    }

    @Autowired private InternacaoRepository internacaoRepository;
    @Autowired private IncidenteSegurancaRepository incidenteRepository;
    @Autowired private AtendimentoRepository atendimentoRepository;
    @Autowired private ConsultaRepository consultaRepository;
    @Autowired private AgendamentoRepository agendamentoRepository;
    @Autowired private AuditoriaProtocoloRepository auditoriaRepository;
    @Autowired private ChecklistProtocoloRepository checklistRepository;
    @Autowired private VersaoChecklistProtocoloRepository versaoRepository;
    @Autowired private MetaQualidadeRepository metaRepository;
    @Autowired private NaoConformidadeRepository naoConformidadeRepository;
    @Autowired private UnidadeDeSaudeRepository unidadeRepository;
    @Autowired private SetorRepository setorRepository;
    @Autowired private ControleDeAcesso controleDeAcesso;
    @Autowired private EscopoDosIndicadores escopo;

    // ── Painel ─────────────────────────────────────────────────────────────────────────────────────

    /**
     * O painel da competência (o mês corrente por padrão), na unidade ou nas unidades do acesso: cada indicador com o valor, a meta da
     * unidade e a série dos 12 meses. As metas só valem no painel da unidade.
     */
    @Transactional(readOnly = true)
    public IndicadoresQualidadeDtos.Painel painel(UUID unidadeId, YearMonth competencia) {
        controleDeAcesso.exigirAlguma(GERIR, INDICADOR);
        YearMonth mes = competencia != null ? competencia : YearMonth.now(ZONA);
        UnidadeDeSaude unidade = null;
        Predicate<UUID> naUnidade;
        if (unidadeId != null) {
            unidade = unidadeOu404(unidadeId);
            controleDeAcesso.exigirVisivel(unidade, GERIR, INDICADOR);
            naUnidade = unidadeId::equals;
        } else {
            Optional<Set<UUID>> visiveis = controleDeAcesso.unidadesVisiveis(GERIR, INDICADOR);
            naUnidade = u -> visiveis.isEmpty() || visiveis.get().contains(u);
        }
        List<MetaQualidade> metas = unidade == null ? List.of() : metaRepository.findTodas().stream()
                .filter(m -> m.getUnidade().getUuid().equals(unidadeId)).toList();
        List<IndicadoresQualidadeDtos.Linha> linhas = new ArrayList<>();
        for (IndicadorQualidade ind : IndicadorQualidade.values()) {
            if (ind == IndicadorQualidade.ADESAO_PROTOCOLO) {
                for (ChecklistProtocolo c : checklistRepository.findAllByOrderByNomeAsc()) {
                    if (c.isAtivo()) {
                        linhas.add(linha(ind, c, naUnidade, mes, metas));
                    }
                }
            } else {
                linhas.add(linha(ind, null, naUnidade, mes, metas));
            }
        }
        return new IndicadoresQualidadeDtos.Painel(unidadeId, unidade != null ? unidade.getNome() : "Unidades do acesso", mes.toString(), linhas);
    }

    private IndicadoresQualidadeDtos.Linha linha(IndicadorQualidade ind, ChecklistProtocolo c, Predicate<UUID> naUnidade, YearMonth mes,
                                                List<MetaQualidade> metas) {
        UUID checklistId = c != null ? c.getUuid() : null;
        Valor v = calcular(ind, checklistId, naUnidade, mes.atDay(1), mes.atEndOfMonth());
        BigDecimal meta = metaVigente(metas, ind, checklistId, mes).map(MetaQualidade::getValor).orElse(null);
        List<IndicadoresQualidadeDtos.Ponto> serie = new ArrayList<>();
        for (int i = MESES_DA_SERIE - 1; i >= 0; i--) {
            YearMonth m = mes.minusMonths(i);
            serie.add(new IndicadoresQualidadeDtos.Ponto(m.toString(), i == 0 ? v.valor() : calcular(ind, checklistId, naUnidade, m.atDay(1), m.atEndOfMonth()).valor()));
        }
        String nome = c != null ? ind.nome() + ": " + c.getNome() : ind.nome();
        return new IndicadoresQualidadeDtos.Linha(ind, checklistId, nome, ind.medida(), ind.menorMelhor(), v.valor(), v.numerador(), v.denominador(), meta,
                situacao(ind, v.valor(), meta), serie);
    }

    static String situacao(IndicadorQualidade ind, BigDecimal valor, BigDecimal meta) {
        if (valor == null) {
            return "SEM_BASE";
        }
        if (meta == null) {
            return "SEM_META";
        }
        return fora(ind, valor, meta) ? "FORA" : "DENTRO";
    }

    static boolean fora(IndicadorQualidade ind, BigDecimal valor, BigDecimal meta) {
        return ind.menorMelhor() ? valor.compareTo(meta) > 0 : valor.compareTo(meta) < 0;
    }

    static Optional<MetaQualidade> metaVigente(List<MetaQualidade> metas, IndicadorQualidade ind, UUID checklistId, YearMonth mes) {
        LocalDate inicio = mes.atDay(1);
        LocalDate fim = mes.atEndOfMonth();
        return metas.stream().filter(m -> m.getIndicador() == ind && java.util.Objects.equals(m.getChecklist() != null ? m.getChecklist().getUuid() : null, checklistId))
                .filter(m -> !m.getVigenteDesde().isAfter(fim) && (m.getVigenteAte() == null || !m.getVigenteAte().isBefore(inicio)))
                .findFirst();
    }

    // ── Cálculo ────────────────────────────────────────────────────────────────────────────────────

    /** O indicador no intervalo, nas unidades do predicado. */
    Valor calcular(IndicadorQualidade ind, UUID checklistId, Predicate<UUID> naUnidade, LocalDate de, LocalDate ate) {
        return switch (ind) {
            case TAXA_QUEDAS -> porMilPacientesDia(incidentes(naUnidade, de, ate, i -> i.getTipo() == TipoIncidente.QUEDA), naUnidade, de, ate);
            case INCIDENCIA_LESAO_PRESSAO -> porMilPacientesDia(incidentes(naUnidade, de, ate, i -> i.getTipo() == TipoIncidente.LESAO_POR_PRESSAO), naUnidade, de, ate);
            case EVENTOS_ADVERSOS_GRAVES -> porMilPacientesDia(incidentes(naUnidade, de, ate, i -> i.getNatureza() == NaturezaIncidente.EVENTO_ADVERSO
                    && (i.getDano() == GrauDano.GRAVE || i.getDano() == GrauDano.OBITO)), naUnidade, de, ate);
            case REINTERNACAO_30_DIAS -> reinternacao(naUnidade, de, ate);
            case TEMPO_ATE_MEDICO -> tempoAteMedico(naUnidade, de, ate);
            case ABSENTEISMO -> absenteismo(naUnidade, de, ate);
            case ADESAO_PROTOCOLO -> adesao(checklistId, naUnidade, de, ate);
        };
    }

    private long incidentes(Predicate<UUID> naUnidade, LocalDate de, LocalDate ate, Predicate<IncidenteSeguranca> filtro) {
        return incidenteRepository.findTodos().stream()
                .filter(i -> i.getSituacao() != SituacaoIncidente.DESCARTADA && naUnidade.test(i.getUnidade().getUuid()))
                .filter(i -> dentro(LocalDate.ofInstant(i.getOcorridoEm(), ZONA), de, ate)).filter(filtro).count();
    }

    private Valor porMilPacientesDia(long eventos, Predicate<UUID> naUnidade, LocalDate de, LocalDate ate) {
        long pacientesDia = pacientesDia(naUnidade, de, ate);
        BigDecimal num = BigDecimal.valueOf(eventos);
        BigDecimal den = BigDecimal.valueOf(pacientesDia);
        return new Valor(pacientesDia == 0 ? null : num.multiply(MIL).divide(den, 2, RoundingMode.HALF_UP), num, den);
    }

    /**
     * Pacientes-dia: os dias de internação dentro do intervalo, da admissão à alta (ou ao fim do intervalo); a internação que entra e sai no
     * mesmo dia conta um.
     */
    long pacientesDia(Predicate<UUID> naUnidade, LocalDate de, LocalDate ate) {
        long total = 0;
        for (Internacao i : internacaoRepository.findQueTocam(de.atStartOfDay(ZONA).toInstant(), ate.plusDays(1).atStartOfDay(ZONA).toInstant())) {
            if (!naUnidade.test(i.getUnidade().getUuid())) {
                continue;
            }
            LocalDate admissao = LocalDate.ofInstant(i.getAdmitidaEm(), ZONA);
            LocalDate alta = i.getAltaEm() != null ? LocalDate.ofInstant(i.getAltaEm(), ZONA) : null;
            LocalDate inicio = admissao.isBefore(de) ? de : admissao;
            LocalDate fim = alta == null || alta.isAfter(ate) ? ate.plusDays(1) : alta;
            long dias = ChronoUnit.DAYS.between(inicio, fim);
            total += dias > 0 ? dias : (alta != null && alta.equals(admissao) && dentro(admissao, de, ate) ? 1 : 0);
        }
        return total;
    }

    /** As altas do intervalo com nova internação do mesmo paciente, em qualquer unidade da rede, em até 30 dias. */
    private Valor reinternacao(Predicate<UUID> naUnidade, LocalDate de, LocalDate ate) {
        List<Internacao> todas = internacaoRepository.findQueTocam(de.atStartOfDay(ZONA).toInstant(),
                ate.plusDays(DIAS_REINTERNACAO + 1).atStartOfDay(ZONA).toInstant());
        Map<UUID, List<Internacao>> porPaciente = todas.stream().collect(Collectors.groupingBy(i -> i.getPaciente().getUuid()));
        long altas = 0;
        long voltaram = 0;
        for (Internacao i : todas) {
            if (i.getStatus() != StatusInternacao.ALTA || i.getAltaEm() == null || !naUnidade.test(i.getUnidade().getUuid())
                    || !dentro(LocalDate.ofInstant(i.getAltaEm(), ZONA), de, ate)) {
                continue;
            }
            altas++;
            Instant limite = i.getAltaEm().plus(Duration.ofDays(DIAS_REINTERNACAO));
            boolean voltou = porPaciente.get(i.getPaciente().getUuid()).stream().anyMatch(o -> o != i && o.getAdmitidaEm().isAfter(i.getAltaEm())
                    && !o.getAdmitidaEm().isAfter(limite));
            if (voltou) {
                voltaram++;
            }
        }
        return percentual(voltaram, altas);
    }

    /** A mediana, em minutos, da chegada à primeira consulta dos que chegaram no intervalo (0 a 24 horas, como na situação operacional). */
    private Valor tempoAteMedico(Predicate<UUID> naUnidade, LocalDate de, LocalDate ate) {
        Map<UUID, LocalDateTime> chegadas = new HashMap<>();
        for (Object[] l : atendimentoRepository.findComChegadaNoIntervalo(de.atStartOfDay(), ate.plusDays(1).atStartOfDay())) {
            if (l[1] != null && naUnidade.test((UUID) l[1])) {
                chegadas.put((UUID) l[0], (LocalDateTime) l[2]);
            }
        }
        List<Long> minutos = new ArrayList<>();
        if (!chegadas.isEmpty()) {
            for (Object[] l : consultaRepository.findPrimeiraPorAtendimento(chegadas.keySet())) {
                long m = Duration.between(chegadas.get((UUID) l[0]), (LocalDateTime) l[1]).toMinutes();
                if (m >= 0 && m <= ESPERA_MAXIMA_VALIDA) {
                    minutos.add(m);
                }
            }
        }
        if (minutos.isEmpty()) {
            return new Valor(null, null, BigDecimal.ZERO);
        }
        minutos.sort(null);
        int n = minutos.size();
        BigDecimal mediana = n % 2 == 1 ? BigDecimal.valueOf(minutos.get(n / 2)).setScale(1)
                : BigDecimal.valueOf(minutos.get(n / 2 - 1) + minutos.get(n / 2)).divide(BigDecimal.valueOf(2), 1, RoundingMode.HALF_UP);
        return new Valor(mediana, null, BigDecimal.valueOf(n));
    }

    /** Faltas sobre realizados mais faltas, como na produção assistencial (BI1). */
    private Valor absenteismo(Predicate<UUID> naUnidade, LocalDate de, LocalDate ate) {
        long realizados = 0;
        long faltas = 0;
        for (Object[] l : agendamentoRepository.findParaProducao(de.atStartOfDay(), ate.plusDays(1).atStartOfDay())) {
            if (!naUnidade.test((UUID) l[0])) {
                continue;
            }
            if (l[2] == StatusAgendamento.REALIZADO) {
                realizados++;
            } else if (l[2] == StatusAgendamento.FALTOU) {
                faltas++;
            }
        }
        return percentual(faltas, realizados + faltas);
    }

    private Valor adesao(UUID checklistId, Predicate<UUID> naUnidade, LocalDate de, LocalDate ate) {
        long conformes = 0;
        long aplicaveis = 0;
        for (AuditoriaProtocolo a : auditoriaRepository.findNoPeriodo(de, ate)) {
            if (naUnidade.test(a.getUnidade().getUuid()) && a.getVersao().getChecklist().getUuid().equals(checklistId)) {
                conformes += a.getConformes();
                aplicaveis += a.getAplicaveis();
            }
        }
        return percentual(conformes, aplicaveis);
    }

    private static Valor percentual(long numerador, long denominador) {
        BigDecimal num = BigDecimal.valueOf(numerador);
        BigDecimal den = BigDecimal.valueOf(denominador);
        return new Valor(denominador == 0 ? null : num.multiply(CEM).divide(den, 1, RoundingMode.HALF_UP), num, den);
    }

    private static boolean dentro(LocalDate d, LocalDate de, LocalDate ate) {
        return !d.isBefore(de) && !d.isAfter(ate);
    }

    // ── Metas ──────────────────────────────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<IndicadoresQualidadeDtos.Meta> metas(UUID unidadeId) {
        controleDeAcesso.exigirAlguma(GERIR, INDICADOR);
        Optional<Set<UUID>> visiveis = controleDeAcesso.unidadesVisiveis(GERIR, INDICADOR);
        return metaRepository.findTodas().stream()
                .filter(m -> visiveis.isEmpty() || visiveis.get().contains(m.getUnidade().getUuid()))
                .filter(m -> unidadeId == null || m.getUnidade().getUuid().equals(unidadeId)).map(IndicadoresQualidadeService::dto).toList();
    }

    /**
     * A meta da unidade (403 fora do acesso): na adesão, o protocolo, e só nela (400); a vigência começa no primeiro dia do mês informado
     * (o corrente por padrão) e encerra a anterior na véspera; a anterior que começa no mesmo mês ou depois, 422.
     */
    @Transactional
    public IndicadoresQualidadeDtos.Meta definirMeta(MetaQualidadeRequestDto dto) {
        UnidadeDeSaude unidade = unidadeOu404(dto.getUnidadeId());
        controleDeAcesso.exigir(GERIR, unidade);
        boolean adesao = dto.getIndicador() == IndicadorQualidade.ADESAO_PROTOCOLO;
        if (adesao != (dto.getChecklistId() != null)) {
            throw new ResourceBadRequestException("A meta de adesão pede o protocolo, e só ela.");
        }
        ChecklistProtocolo checklist = dto.getChecklistId() == null ? null : checklistOu404(dto.getChecklistId());
        LocalDate desde = (dto.getVigenteDesde() != null ? YearMonth.from(dto.getVigenteDesde()) : YearMonth.now(ZONA)).atDay(1);
        UUID cid = checklist != null ? checklist.getUuid() : null;
        for (MetaQualidade m : metaRepository.findTodas()) {
            if (m.getUnidade().getUuid().equals(unidade.getUuid()) && m.getIndicador() == dto.getIndicador()
                    && java.util.Objects.equals(m.getChecklist() != null ? m.getChecklist().getUuid() : null, cid) && m.getVigenteAte() == null) {
                if (!m.getVigenteDesde().isBefore(desde)) {
                    throw new ResourceUnprocessableEntityException("Já há meta vigente a partir de " + m.getVigenteDesde() + ".");
                }
                m.setVigenteAte(desde.minusDays(1));
                metaRepository.save(m);
            }
        }
        MetaQualidade m = new MetaQualidade(null, unidade, dto.getIndicador(), checklist, dto.getValor(), desde, null, Instant.now(), UsuarioAutenticado.cpf());
        metaRepository.save(m);
        ContextoAuditoria.unidade(unidade);
        ContextoAuditoria.registro(m.getUuid());
        ContextoAuditoria.detalhe("Meta de " + dto.getIndicador().nome() + ": " + dto.getValor() + " desde " + desde + ".");
        return dto(m);
    }

    // ── Checklists ─────────────────────────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<IndicadoresQualidadeDtos.Checklist> checklists() {
        controleDeAcesso.exigirAlguma(GERIR, ANALISAR, INDICADOR);
        return checklistRepository.findAllByOrderByNomeAsc().stream().map(this::dto).toList();
    }

    /** O checklist com a primeira versão: código e nome (400), código único (409), itens sem código repetido (400). */
    @Transactional
    public IndicadoresQualidadeDtos.Checklist criarChecklist(ChecklistProtocoloRequestDto dto) {
        controleDeAcesso.exigirAlguma(GERIR);
        String codigo = dto.getCodigo() == null ? "" : dto.getCodigo().trim().toUpperCase(Locale.ROOT);
        String nome = dto.getNome() == null ? "" : dto.getNome().trim();
        if (codigo.isEmpty() || nome.isEmpty()) {
            throw new ResourceBadRequestException("Informe o código e o nome do checklist.");
        }
        if (checklistRepository.findByCodigo(codigo).isPresent()) {
            throw new ResourceConflictException("Já existe o checklist " + codigo + ".");
        }
        ChecklistProtocolo c = checklistRepository.save(new ChecklistProtocolo(null, codigo, nome, true, Instant.now()));
        versao(c, 1, dto.getItens());
        ContextoAuditoria.registro(c.getUuid());
        return dto(c);
    }

    @Transactional
    public IndicadoresQualidadeDtos.Checklist novaVersaoChecklist(UUID id, ChecklistProtocoloRequestDto dto) {
        controleDeAcesso.exigirAlguma(GERIR);
        ChecklistProtocolo c = checklistOu404(id);
        int ultima = versaoRepository.findFirstByChecklist_UuidOrderByVersaoDesc(id).map(VersaoChecklistProtocolo::getVersao).orElse(0);
        if (dto.getNome() != null && !dto.getNome().isBlank()) {
            c.setNome(dto.getNome().trim());
        }
        versao(c, ultima + 1, dto.getItens());
        ContextoAuditoria.registro(c.getUuid());
        return dto(c);
    }

    private void versao(ChecklistProtocolo c, int numero, List<ChecklistProtocoloRequestDto.Item> itens) {
        Set<String> codigos = new HashSet<>();
        List<IndicadoresQualidadeDtos.ItemChecklist> lista = new ArrayList<>();
        for (ChecklistProtocoloRequestDto.Item i : itens) {
            String cod = i.getCodigo().trim().toUpperCase(Locale.ROOT);
            if (!codigos.add(cod)) {
                throw new ResourceBadRequestException("O item " + cod + " está repetido.");
            }
            lista.add(new IndicadoresQualidadeDtos.ItemChecklist(cod, i.getTexto().trim()));
        }
        try {
            versaoRepository.save(new VersaoChecklistProtocolo(null, c, numero, JSON.writeValueAsString(Map.of("itens", lista)), Instant.now(), UsuarioAutenticado.cpf()));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    static List<IndicadoresQualidadeDtos.ItemChecklist> itens(VersaoChecklistProtocolo v) {
        try {
            Map<String, List<IndicadoresQualidadeDtos.ItemChecklist>> m = JSON.readValue(v.getDefinicao(), new TypeReference<>() {
            });
            return m.getOrDefault("itens", List.of());
        } catch (Exception e) {
            throw new IllegalStateException("Checklist com definição inválida: " + v.getUuid(), e);
        }
    }

    // ── Auditorias de protocolo ────────────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<IndicadoresQualidadeDtos.Auditoria> auditorias(UUID unidadeId, LocalDate de, LocalDate ate) {
        controleDeAcesso.exigirAlguma(GERIR, ANALISAR);
        Optional<Set<UUID>> visiveis = controleDeAcesso.unidadesVisiveis(GERIR, ANALISAR);
        LocalDate fim = ate != null ? ate : LocalDate.now(ZONA);
        LocalDate inicio = de != null ? de : fim.minusMonths(3);
        return auditoriaRepository.findNoPeriodo(inicio, fim).stream()
                .filter(a -> visiveis.isEmpty() || visiveis.get().contains(a.getUnidade().getUuid()))
                .filter(a -> unidadeId == null || a.getUnidade().getUuid().equals(unidadeId)).map(IndicadoresQualidadeService::dto).toList();
    }

    /**
     * A auditoria: quem gere ou analisa na unidade (403); o setor da unidade (422); a data não futura (400); todos os itens da versão
     * respondidos, sem item estranho (400), e ao menos um aplicável (400). A adesão sai dos totais.
     */
    @Transactional
    public IndicadoresQualidadeDtos.Auditoria registrarAuditoria(AuditoriaProtocoloRequestDto dto) {
        UnidadeDeSaude unidade = unidadeOu404(dto.getUnidadeId());
        controleDeAcesso.exigirVisivel(unidade, GERIR, ANALISAR);
        VersaoChecklistProtocolo versao = versaoRepository.findById(dto.getVersaoId()).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar a versão de checklist " + dto.getVersaoId() + " em nossos registros."));
        Setor setor = null;
        if (dto.getSetorId() != null) {
            setor = setorRepository.findById(dto.getSetorId()).orElseThrow(() -> new ResourceNotFoundException(
                    "Não foi possível encontrar o setor " + dto.getSetorId() + " em nossos registros."));
            if (setor.getUnidade() == null || !setor.getUnidade().getUuid().equals(unidade.getUuid())) {
                throw new ResourceUnprocessableEntityException("O setor é de outra unidade.");
            }
        }
        LocalDate data = dto.getDataAuditoria() != null ? dto.getDataAuditoria() : LocalDate.now(ZONA);
        if (data.isAfter(LocalDate.now(ZONA))) {
            throw new ResourceBadRequestException("A auditoria não pode ser de data futura.");
        }
        Map<String, RespostaRoteiro> respostas = new TreeMap<>();
        dto.getRespostas().forEach((k, v) -> respostas.put(k.trim().toUpperCase(Locale.ROOT), v));
        Set<String> codigos = itens(versao).stream().map(IndicadoresQualidadeDtos.ItemChecklist::codigo).collect(Collectors.toCollection(LinkedHashSet::new));
        for (String c : codigos) {
            if (respostas.get(c) == null) {
                throw new ResourceBadRequestException("Responda o item " + c + " do checklist.");
            }
        }
        for (String c : respostas.keySet()) {
            if (!codigos.contains(c)) {
                throw new ResourceBadRequestException("O item " + c + " não é do checklist.");
            }
        }
        int conformes = (int) respostas.values().stream().filter(r -> r == RespostaRoteiro.CONFORME).count();
        int aplicaveis = (int) respostas.values().stream().filter(r -> r != RespostaRoteiro.NAO_SE_APLICA).count();
        if (aplicaveis == 0) {
            throw new ResourceBadRequestException("A auditoria precisa de ao menos um item aplicável.");
        }
        AuditoriaProtocolo a;
        try {
            a = new AuditoriaProtocolo(null, unidade, setor, versao, data, JSON.writeValueAsString(respostas), conformes, aplicaveis,
                    dto.getObservacao() == null || dto.getObservacao().isBlank() ? null : dto.getObservacao().trim(), UsuarioAutenticado.cpf(), Instant.now());
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
        auditoriaRepository.save(a);
        ContextoAuditoria.unidade(unidade);
        ContextoAuditoria.registro(a.getUuid());
        ContextoAuditoria.detalhe("Auditoria de " + versao.getChecklist().getNome() + ": " + conformes + " de " + aplicaveis + ".");
        return dto(a);
    }

    // ── Sugestões para a Q2 ────────────────────────────────────────────────────────────────────────

    /** O indicador fora da meta no mês fechado anterior, por meta vigente nas unidades do predicado. */
    List<Object[]> foraDaMeta(Predicate<UUID> naUnidade) {
        YearMonth mes = YearMonth.now(ZONA).minusMonths(1);
        List<MetaQualidade> metas = metaRepository.findTodas().stream().filter(m -> naUnidade.test(m.getUnidade().getUuid())).toList();
        List<Object[]> lista = new ArrayList<>();
        for (MetaQualidade m : metas) {
            if (metaVigente(List.of(m), m.getIndicador(), m.getChecklist() != null ? m.getChecklist().getUuid() : null, mes).isEmpty()) {
                continue;
            }
            UUID u = m.getUnidade().getUuid();
            Valor v = calcular(m.getIndicador(), m.getChecklist() != null ? m.getChecklist().getUuid() : null, u::equals, mes.atDay(1), mes.atEndOfMonth());
            if (v.valor() != null && fora(m.getIndicador(), v.valor(), m.getValor())) {
                lista.add(new Object[]{m, mes, v.valor()});
            }
        }
        return lista;
    }

    // ── Relatório no #19 ───────────────────────────────────────────────────────────────────────────

    /** O relatório da qualidade no catálogo da BI4: os indicadores do período, os incidentes, as não conformidades e a adesão. */
    @Transactional(readOnly = true)
    public RelatorioDto relatorio(TipoRelatorio tipo, LocalDate de, LocalDate ate, UUID unidadeId) {
        EscopoDosIndicadores.Escopo e = escopo.de(unidadeId);
        LocalDate fim = ate == null ? LocalDate.now(ZONA) : ate;
        LocalDate inicio = de == null ? fim.withDayOfMonth(1) : de;
        if (inicio.isAfter(fim)) {
            throw new ResourceBadRequestException("O início do período não pode ser depois do fim.");
        }
        if (inicio.plusYears(1).isBefore(fim)) {
            throw new ResourceBadRequestException("O período vai até um ano.");
        }
        Predicate<UUID> naUnidade = u -> e.unidades() == null || e.contem(u);
        List<MetaQualidade> metas = unidadeId == null ? List.of() : metaRepository.findTodas().stream().filter(m -> m.getUnidade().getUuid().equals(unidadeId)).toList();
        YearMonth mesFim = YearMonth.from(fim);
        List<List<String>> indicadores = new ArrayList<>();
        for (IndicadorQualidade ind : IndicadorQualidade.values()) {
            List<ChecklistProtocolo> cs = ind == IndicadorQualidade.ADESAO_PROTOCOLO ? checklistRepository.findAllByOrderByNomeAsc() : java.util.Collections.singletonList(null);
            for (ChecklistProtocolo c : cs) {
                UUID cid = c != null ? c.getUuid() : null;
                Valor v = calcular(ind, cid, naUnidade, inicio, fim);
                BigDecimal meta = metaVigente(metas, ind, cid, mesFim).map(MetaQualidade::getValor).orElse(null);
                indicadores.add(List.of(c != null ? ind.nome() + ": " + c.getNome() : ind.nome(), ind.medida(), v.valor() == null ? "sem base" : v.valor().toPlainString(),
                        meta == null ? "" : meta.toPlainString(), situacao(ind, v.valor(), meta)));
            }
        }
        List<IncidenteSeguranca> incidentes = incidenteRepository.findTodos().stream()
                .filter(i -> i.getSituacao() != SituacaoIncidente.DESCARTADA && naUnidade.test(i.getUnidade().getUuid())
                        && dentro(LocalDate.ofInstant(i.getOcorridoEm(), ZONA), inicio, fim)).toList();
        Map<String, Long> porTipo = new TreeMap<>(incidentes.stream().collect(Collectors.groupingBy(i -> i.getTipo().name(), Collectors.counting())));
        List<NaoConformidade> ncs = naoConformidadeRepository.findTodas().stream()
                .filter(n -> naUnidade.test(n.getUnidade().getUuid()) && dentro(n.getIdentificadaEm(), inicio, fim)).toList();
        Map<String, Long> porOrigem = new TreeMap<>(ncs.stream().collect(Collectors.groupingBy(n -> n.getOrigem().name() + "|" + n.getSituacao().name(), Collectors.counting())));
        List<AuditoriaProtocolo> auditorias = auditoriaRepository.findNoPeriodo(inicio, fim).stream().filter(a -> naUnidade.test(a.getUnidade().getUuid())).toList();
        Map<String, long[]> porProtocolo = new TreeMap<>();
        auditorias.forEach(a -> {
            long[] t = porProtocolo.computeIfAbsent(a.getVersao().getChecklist().getNome(), k -> new long[3]);
            t[0]++;
            t[1] += a.getConformes();
            t[2] += a.getAplicaveis();
        });
        List<RelatorioDto.Secao> secoes = new ArrayList<>();
        secoes.add(new RelatorioDto.Secao("Indicadores do período", List.of("Indicador", "Medida", "Valor", "Meta", "Situação"), indicadores));
        secoes.add(new RelatorioDto.Secao("Incidentes de segurança por tipo", List.of("Tipo", "Incidentes"),
                porTipo.entrySet().stream().map(x -> List.of(x.getKey(), String.valueOf(x.getValue()))).toList()));
        secoes.add(new RelatorioDto.Secao("Não conformidades por origem e situação", List.of("Origem", "Situação", "Quantidade"),
                porOrigem.entrySet().stream().map(x -> List.of(x.getKey().split("\\|")[0], x.getKey().split("\\|")[1], String.valueOf(x.getValue()))).toList()));
        secoes.add(new RelatorioDto.Secao("Adesão aos protocolos", List.of("Protocolo", "Auditorias", "Adesão (%)"),
                porProtocolo.entrySet().stream().map(x -> List.of(x.getKey(), String.valueOf(x.getValue()[0]), x.getValue()[2] == 0 ? ""
                        : BigDecimal.valueOf(x.getValue()[1] * 100).divide(BigDecimal.valueOf(x.getValue()[2]), 1, RoundingMode.HALF_UP).toPlainString())).toList()));
        long abertas = ncs.stream().filter(n -> n.getSituacao() != SituacaoNaoConformidade.ENCERRADA && n.getSituacao() != SituacaoNaoConformidade.CANCELADA).count();
        List<RelatorioDto.Indicador> resumo = List.of(
                new RelatorioDto.Indicador("Incidentes notificados", String.valueOf(incidentes.size())),
                new RelatorioDto.Indicador("Eventos adversos", String.valueOf(incidentes.stream().filter(i -> i.getNatureza() == NaturezaIncidente.EVENTO_ADVERSO).count())),
                new RelatorioDto.Indicador("Não conformidades em aberto", String.valueOf(abertas)),
                new RelatorioDto.Indicador("Auditorias de protocolo", String.valueOf(auditorias.size())));
        return new RelatorioDto(tipo.name(), tipo.getNome(), tipo.getCategoria(), inicio, fim, e.abrangencia(), resumo, secoes,
                List.of("Os indicadores saem dos registros da rede; sem base, o indicador não é zero. As metas valem no relatório da unidade, pela "
                        + "competência do fim do período. Só totais, sem pessoas."));
    }

    // ── Apoio ──────────────────────────────────────────────────────────────────────────────────────

    private UnidadeDeSaude unidadeOu404(UUID id) {
        return unidadeRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar uma unidade de saúde com o id " + id + " em nossos registros."));
    }

    ChecklistProtocolo checklistOu404(UUID id) {
        return checklistRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar o checklist " + id + " em nossos registros."));
    }

    private static IndicadoresQualidadeDtos.Meta dto(MetaQualidade m) {
        ChecklistProtocolo c = m.getChecklist();
        return new IndicadoresQualidadeDtos.Meta(m.getUuid(), m.getUnidade().getUuid(), m.getUnidade().getNome(), m.getIndicador(), m.getIndicador().nome(),
                c != null ? c.getUuid() : null, c != null ? c.getNome() : null, m.getValor(), m.getVigenteDesde(), m.getVigenteAte());
    }

    private IndicadoresQualidadeDtos.Checklist dto(ChecklistProtocolo c) {
        VersaoChecklistProtocolo v = versaoRepository.findFirstByChecklist_UuidOrderByVersaoDesc(c.getUuid()).orElse(null);
        return new IndicadoresQualidadeDtos.Checklist(c.getUuid(), c.getCodigo(), c.getNome(), c.isAtivo(), v != null ? v.getUuid() : null,
                v != null ? v.getVersao() : 0, v != null ? itens(v) : List.of());
    }

    private static IndicadoresQualidadeDtos.Auditoria dto(AuditoriaProtocolo a) {
        Map<String, RespostaRoteiro> respostas;
        try {
            respostas = JSON.readValue(a.getRespostas(), new TypeReference<TreeMap<String, RespostaRoteiro>>() {
            });
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
        return new IndicadoresQualidadeDtos.Auditoria(a.getUuid(), a.getUnidade().getUuid(), a.getUnidade().getNome(), a.getSetor() != null ? a.getSetor().getNome() : null,
                a.getVersao().getChecklist().getUuid(), a.getVersao().getChecklist().getNome(), a.getVersao().getVersao(), a.getDataAuditoria(), a.getConformes(),
                a.getAplicaveis(), BigDecimal.valueOf(a.getConformes() * 100L).divide(BigDecimal.valueOf(a.getAplicaveis()), 1, RoundingMode.HALF_UP),
                a.getObservacao(), respostas, a.getRegistradoEm());
    }

    static Set<IndicadorQualidade> todos() {
        return EnumSet.allOf(IndicadorQualidade.class);
    }
}
