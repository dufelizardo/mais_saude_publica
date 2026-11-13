package com.edufelizardo.maissaudepublica.services.version1;

import com.edufelizardo.maissaudepublica.config.ContextoAuditoria;
import com.edufelizardo.maissaudepublica.config.UsuarioAutenticado;
import com.edufelizardo.maissaudepublica.exceptions.ResourceBadRequestException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceNotFoundException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceUnprocessableEntityException;
import com.edufelizardo.maissaudepublica.models.IncidenteSeguranca;
import com.edufelizardo.maissaudepublica.models.Paciente;
import com.edufelizardo.maissaudepublica.models.Profissional;
import com.edufelizardo.maissaudepublica.models.UnidadeDeSaude;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.ClassificacaoIncidenteRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.ConclusaoIncidenteRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.IncidenteSegurancaRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.NotivisaRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.IncidenteSegurancaDtos;
import com.edufelizardo.maissaudepublica.models.enuns.FatorContribuinte;
import com.edufelizardo.maissaudepublica.models.enuns.GrauDano;
import com.edufelizardo.maissaudepublica.models.enuns.NaturezaIncidente;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoIncidente;
import com.edufelizardo.maissaudepublica.repositories.IncidenteSegurancaRepository;
import com.edufelizardo.maissaudepublica.repositories.PacienteRepository;
import com.edufelizardo.maissaudepublica.repositories.ProfissionalRepository;
import com.edufelizardo.maissaudepublica.repositories.UnidadeDeSaudeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Segurança do paciente, fatia Q1 da Qualidade (ADR-0203, desenho na ADR-0202), pela RDC ANVISA 36/2013 e pelo PNSP (Portaria MS
 * 529/2013): a notificação interna de incidente por qualquer profissional, anônima ou identificada (cultura não punitiva); a classificação
 * e a análise pelo Núcleo de Segurança do Paciente, com a causa raiz no dano grave, no óbito e no never event; o registro da notificação do
 * evento adverso ao SNVS (Notivisa) com o prazo.
 */
@Service
public class IncidenteSegurancaService {

    public static final String NOTIFICAR = "QUALIDADE.NOTIFICAR";
    public static final String ANALISAR = "QUALIDADE.ANALISAR";
    /** Óbito e never event: 72 horas da ocorrência (RDC 36/2013, art. 10). */
    static final Duration PRAZO_GRAVISSIMO = Duration.ofHours(72);
    /** Os demais eventos adversos: até o 15º dia útil do mês seguinte. */
    static final int DIA_UTIL_MENSAL = 15;
    private static final ZoneId ZONA = ZoneId.of("America/Sao_Paulo");

    @Autowired private IncidenteSegurancaRepository repository;
    @Autowired private UnidadeDeSaudeRepository unidadeRepository;
    @Autowired private PacienteRepository pacienteRepository;
    @Autowired private ProfissionalRepository profissionalRepository;
    @Autowired private ControleDeAcesso controleDeAcesso;

    /**
     * Notifica: a unidade do acesso (403); o paciente, se informado, existe (404); a ocorrência não é futura (400). Na anônima, quem
     * notificou não é guardado. Devolve só o protocolo.
     */
    @Transactional
    public IncidenteSegurancaDtos.Protocolo notificar(IncidenteSegurancaRequestDto dto) {
        UnidadeDeSaude unidade = unidadeRepository.findById(dto.getUnidadeId()).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar uma unidade de saúde com o id " + dto.getUnidadeId() + " em nossos registros."));
        controleDeAcesso.exigir(NOTIFICAR, unidade);
        Paciente paciente = dto.getPacienteId() == null ? null : pacienteRepository.findById(dto.getPacienteId()).orElseThrow(() ->
                new ResourceNotFoundException("Não foi possível encontrar um paciente com o id " + dto.getPacienteId() + " em nossos registros."));
        if (dto.getOcorridoEm().isAfter(Instant.now())) {
            throw new ResourceBadRequestException("A ocorrência não pode ser no futuro.");
        }
        boolean anonima = Boolean.TRUE.equals(dto.getAnonima());
        IncidenteSeguranca i = new IncidenteSeguranca();
        i.setNumero(numero(LocalDate.now(ZONA).getYear()));
        i.setUnidade(unidade);
        i.setPaciente(paciente);
        i.setOcorridoEm(dto.getOcorridoEm());
        i.setLocal(dto.getLocal().trim());
        i.setTipo(dto.getTipo());
        i.setDanoPercebido(dto.getDanoPercebido());
        i.setDescricao(dto.getDescricao().trim());
        i.setAcaoImediata(limpo(dto.getAcaoImediata()));
        i.setAnonima(anonima);
        i.setNotificadoPorCpf(anonima ? null : UsuarioAutenticado.cpf());
        i.setSituacao(SituacaoIncidente.RECEBIDA);
        i.setRegistradoEm(Instant.now());
        repository.save(i);
        ContextoAuditoria.unidade(unidade);
        ContextoAuditoria.registro(i.getUuid());
        if (paciente != null) {
            ContextoAuditoria.paciente(paciente.getUuid());
        }
        ContextoAuditoria.detalhe("Incidente de segurança " + i.getNumero() + (anonima ? " (anônimo)." : "."));
        return new IncidenteSegurancaDtos.Protocolo(i.getUuid(), i.getNumero(), i.getRegistradoEm(), anonima);
    }

    /** Os incidentes das unidades em que se analisa; unidadeId e situacao filtram. */
    @Transactional(readOnly = true)
    public List<IncidenteSegurancaDtos.Incidente> incidentes(UUID unidadeId, SituacaoIncidente situacao) {
        controleDeAcesso.exigirAlguma(ANALISAR);
        Optional<Set<UUID>> visiveis = controleDeAcesso.unidadesVisiveis(ANALISAR);
        return repository.findTodos().stream()
                .filter(i -> visiveis.isEmpty() || visiveis.get().contains(i.getUnidade().getUuid()))
                .filter(i -> unidadeId == null || i.getUnidade().getUuid().equals(unidadeId))
                .filter(i -> situacao == null || i.getSituacao() == situacao).map(this::dto).toList();
    }

    @Transactional(readOnly = true)
    public IncidenteSegurancaDtos.Incidente incidente(UUID id) {
        IncidenteSeguranca i = paraAnalisar(id);
        return dto(i);
    }

    /** Classifica (recebido ou em análise; 422 nos outros) e põe em análise. */
    @Transactional
    public IncidenteSegurancaDtos.Incidente classificar(UUID id, ClassificacaoIncidenteRequestDto dto) {
        IncidenteSeguranca i = paraAnalisar(id);
        aberto(i);
        i.setNatureza(dto.getNatureza());
        i.setDano(dto.getDano());
        if (dto.getTipo() != null) {
            i.setTipo(dto.getTipo());
        }
        i.setNeverEvent(Boolean.TRUE.equals(dto.getNeverEvent()));
        if (dto.getNatureza() != NaturezaIncidente.EVENTO_ADVERSO && dto.getDano() != GrauDano.NENHUM) {
            throw new ResourceBadRequestException("Só o evento adverso tem dano; nas outras naturezas, o dano é nenhum.");
        }
        if (dto.getNatureza() == NaturezaIncidente.EVENTO_ADVERSO && dto.getDano() == GrauDano.NENHUM) {
            throw new ResourceBadRequestException("O evento adverso tem dano ao paciente; sem dano, é incidente sem dano.");
        }
        i.setSituacao(SituacaoIncidente.EM_ANALISE);
        return salvar(i, "Incidente classificado: " + dto.getNatureza() + ", dano " + dto.getDano() + ".");
    }

    /** Conclui a análise do classificado (422): a causa raiz no dano grave, no óbito e no never event (400). */
    @Transactional
    public IncidenteSegurancaDtos.Incidente concluir(UUID id, ConclusaoIncidenteRequestDto dto) {
        IncidenteSeguranca i = paraAnalisar(id);
        if (i.getSituacao() != SituacaoIncidente.EM_ANALISE) {
            throw new ResourceUnprocessableEntityException("Classifique o incidente antes de concluir a análise.");
        }
        if (exigeCausaRaiz(i) && limpo(dto.getCausaRaiz()) == null) {
            throw new ResourceBadRequestException("Dano grave, óbito ou never event pedem a análise de causa raiz.");
        }
        i.setFatores(dto.getFatores().stream().distinct().map(Enum::name).collect(Collectors.joining(";")));
        i.setCausaRaiz(limpo(dto.getCausaRaiz()));
        i.setLicoes(limpo(dto.getLicoes()));
        i.setSituacao(SituacaoIncidente.CONCLUIDA);
        i.setAnalisadoEm(Instant.now());
        i.setAnalisadoPorCpf(UsuarioAutenticado.cpf());
        return salvar(i, "Análise do incidente concluída.");
    }

    /** Descarta o que não é incidente (duplicado, fora do escopo), com o motivo; só aberto (422). */
    @Transactional
    public IncidenteSegurancaDtos.Incidente descartar(UUID id, String motivo) {
        IncidenteSeguranca i = paraAnalisar(id);
        aberto(i);
        i.setSituacao(SituacaoIncidente.DESCARTADA);
        i.setMotivoDescarte(motivo.trim());
        i.setAnalisadoEm(Instant.now());
        i.setAnalisadoPorCpf(UsuarioAutenticado.cpf());
        return salvar(i, "Incidente descartado.");
    }

    /** Registra a notificação ao SNVS: só do evento adverso (422), uma vez (409 se já houver), não futura (400). */
    @Transactional
    public IncidenteSegurancaDtos.Incidente registrarNotivisa(UUID id, NotivisaRequestDto dto) {
        IncidenteSeguranca i = paraAnalisar(id);
        if (i.getNatureza() != NaturezaIncidente.EVENTO_ADVERSO) {
            throw new ResourceUnprocessableEntityException("Vai ao Notivisa o evento adverso; classifique antes.");
        }
        if (i.getNumeroNotivisa() != null) {
            throw new com.edufelizardo.maissaudepublica.exceptions.ResourceConflictException("A notificação ao Notivisa já foi registrada: "
                    + i.getNumeroNotivisa() + ".");
        }
        Instant em = dto.getNotificadoEm() != null ? dto.getNotificadoEm() : Instant.now();
        if (em.isAfter(Instant.now())) {
            throw new ResourceBadRequestException("A notificação não pode ser no futuro.");
        }
        i.setNumeroNotivisa(dto.getNumero().trim());
        i.setNotivisaEm(em);
        return salvar(i, "Notificação ao Notivisa " + i.getNumeroNotivisa() + ".");
    }

    /** O painel do núcleo, só com totais: nas unidades em que se analisa ou se consultam indicadores; de e ate pela ocorrência. */
    @Transactional(readOnly = true)
    public IncidenteSegurancaDtos.Painel painel(UUID unidadeId, LocalDate de, LocalDate ate) {
        controleDeAcesso.exigirAlguma(ANALISAR, "INDICADOR.CONSULTAR");
        Optional<Set<UUID>> visiveis = controleDeAcesso.unidadesVisiveis(ANALISAR, "INDICADOR.CONSULTAR");
        Instant agora = Instant.now();
        List<IncidenteSeguranca> lista = repository.findTodos().stream()
                .filter(i -> visiveis.isEmpty() || visiveis.get().contains(i.getUnidade().getUuid()))
                .filter(i -> unidadeId == null || i.getUnidade().getUuid().equals(unidadeId))
                .filter(i -> de == null || !dia(i.getOcorridoEm()).isBefore(de))
                .filter(i -> ate == null || !dia(i.getOcorridoEm()).isAfter(ate))
                .filter(i -> i.getSituacao() != SituacaoIncidente.DESCARTADA).toList();
        List<IncidenteSeguranca> adversos = lista.stream().filter(i -> i.getNatureza() == NaturezaIncidente.EVENTO_ADVERSO).toList();
        List<IncidenteSeguranca> pendentes = adversos.stream().filter(i -> i.getNumeroNotivisa() == null).toList();
        return new IncidenteSegurancaDtos.Painel(lista.size(), lista.stream().filter(i -> i.getSituacao() != SituacaoIncidente.CONCLUIDA).count(),
                adversos.size(), pendentes.size(), pendentes.stream().filter(i -> prazoNotivisa(i).isBefore(agora)).count(),
                contar(lista, i -> i.getTipo().name()), contar(lista.stream().filter(i -> i.getDano() != null).toList(), i -> i.getDano().name()),
                contar(lista.stream().filter(i -> i.getNatureza() != null).toList(), i -> i.getNatureza().name()));
    }

    // ── Regras ─────────────────────────────────────────────────────────────────────────────────────

    static boolean exigeCausaRaiz(IncidenteSeguranca i) {
        return i.isNeverEvent() || i.getDano() == GrauDano.GRAVE || i.getDano() == GrauDano.OBITO;
    }

    /** O prazo do Notivisa: 72 horas da ocorrência no óbito e no never event; nos demais, o fim do 15º dia útil do mês seguinte. */
    static Instant prazoNotivisa(IncidenteSeguranca i) {
        if (i.getDano() == GrauDano.OBITO || i.isNeverEvent()) {
            return i.getOcorridoEm().plus(PRAZO_GRAVISSIMO);
        }
        LocalDate d = YearMonth.from(dia(i.getOcorridoEm())).plusMonths(1).atDay(1);
        int uteis = 0;
        while (true) {
            if (d.getDayOfWeek() != DayOfWeek.SATURDAY && d.getDayOfWeek() != DayOfWeek.SUNDAY && ++uteis == DIA_UTIL_MENSAL) {
                break;
            }
            d = d.plusDays(1);
        }
        return d.plusDays(1).atStartOfDay(ZONA).toInstant();
    }

    // ── Apoio ──────────────────────────────────────────────────────────────────────────────────────

    private IncidenteSeguranca paraAnalisar(UUID id) {
        IncidenteSeguranca i = repository.findById(id).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar o incidente " + id + " em nossos registros."));
        controleDeAcesso.exigir(ANALISAR, i.getUnidade());
        ContextoAuditoria.registro(i.getUuid());
        if (i.getPaciente() != null) {
            ContextoAuditoria.paciente(i.getPaciente().getUuid());
        }
        return i;
    }

    private static void aberto(IncidenteSeguranca i) {
        if (i.getSituacao() != SituacaoIncidente.RECEBIDA && i.getSituacao() != SituacaoIncidente.EM_ANALISE) {
            throw new ResourceUnprocessableEntityException("A análise deste incidente já foi encerrada.");
        }
    }

    private IncidenteSegurancaDtos.Incidente salvar(IncidenteSeguranca i, String detalhe) {
        repository.save(i);
        ContextoAuditoria.detalhe(detalhe);
        return dto(i);
    }

    private String numero(int ano) {
        String prefixo = ano + "/";
        return prefixo + String.format(Locale.ROOT, "%06d", repository.countByNumeroStartingWith(prefixo) + 1);
    }

    private static List<IncidenteSegurancaDtos.Contagem> contar(List<IncidenteSeguranca> lista, Function<IncidenteSeguranca, String> chave) {
        Map<String, Long> m = new TreeMap<>(lista.stream().collect(Collectors.groupingBy(chave, Collectors.counting())));
        return m.entrySet().stream().map(e -> new IncidenteSegurancaDtos.Contagem(e.getKey(), e.getValue())).toList();
    }

    static LocalDate dia(Instant i) {
        return LocalDate.ofInstant(i, ZONA);
    }

    private static String limpo(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }

    private IncidenteSegurancaDtos.Incidente dto(IncidenteSeguranca i) {
        boolean adverso = i.getNatureza() == NaturezaIncidente.EVENTO_ADVERSO;
        Instant prazo = adverso ? prazoNotivisa(i) : null;
        String notificador = i.getNotificadoPorCpf() == null ? null
                : profissionalRepository.findByCpfAndAtivoTrue(i.getNotificadoPorCpf()).map(Profissional::getNome).orElse("Usuário " + i.getNotificadoPorCpf());
        Paciente p = i.getPaciente();
        List<FatorContribuinte> fatores = i.getFatores() == null ? List.of()
                : Arrays.stream(i.getFatores().split(";")).map(FatorContribuinte::valueOf).toList();
        return new IncidenteSegurancaDtos.Incidente(i.getUuid(), i.getNumero(), i.getUnidade().getUuid(), i.getUnidade().getNome(),
                p != null ? p.getUuid() : null, p != null ? p.getNome() : null, i.getOcorridoEm(), i.getLocal(), i.getTipo(), i.getDanoPercebido(),
                i.getDescricao(), i.getAcaoImediata(), i.isAnonima(), notificador, i.getSituacao(), i.getNatureza(), i.getDano(), i.isNeverEvent(), fatores,
                i.getCausaRaiz(), i.getLicoes(), i.getMotivoDescarte(), i.getAnalisadoEm(), exigeCausaRaiz(i), adverso, prazo, i.getNumeroNotivisa(),
                i.getNotivisaEm(), adverso && i.getNumeroNotivisa() == null && prazo.isBefore(Instant.now()), i.getRegistradoEm());
    }
}
