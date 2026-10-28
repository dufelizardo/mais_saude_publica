package com.edufelizardo.maissaudepublica.services.version1;

import com.edufelizardo.maissaudepublica.config.ContextoAuditoria;
import com.edufelizardo.maissaudepublica.config.UsuarioAutenticado;
import com.edufelizardo.maissaudepublica.exceptions.ResourceBadRequestException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceConflictException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceNotFoundException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceUnprocessableEntityException;
import com.edufelizardo.maissaudepublica.models.Adscricao;
import com.edufelizardo.maissaudepublica.models.Atendimento;
import com.edufelizardo.maissaudepublica.models.Campanha;
import com.edufelizardo.maissaudepublica.models.Equipe;
import com.edufelizardo.maissaudepublica.models.EsquemaVacinal;
import com.edufelizardo.maissaudepublica.models.Imunobiologico;
import com.edufelizardo.maissaudepublica.models.Paciente;
import com.edufelizardo.maissaudepublica.models.Profissional;
import com.edufelizardo.maissaudepublica.models.RegistroVacinacao;
import com.edufelizardo.maissaudepublica.models.UnidadeDeSaude;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.AnulacaoVacinacaoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.RegistroVacinacaoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.AtrasadoVacinacaoDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.CadernetaVacinacaoDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.ContagemProducaoDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.DoseCalendarioDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.EsquemaVacinalDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.RegistroVacinacaoDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.VacinacaoDoDiaDto;
import com.edufelizardo.maissaudepublica.models.enuns.DoseVacinal;
import com.edufelizardo.maissaudepublica.models.enuns.EstrategiaVacinacao;
import com.edufelizardo.maissaudepublica.models.enuns.OrigemRegistroVacinacao;
import com.edufelizardo.maissaudepublica.models.enuns.PublicoVacinal;
import com.edufelizardo.maissaudepublica.models.enuns.Sexo;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoDoseCalendario;
import com.edufelizardo.maissaudepublica.models.enuns.TipoCampanha;
import com.edufelizardo.maissaudepublica.repositories.AdscricaoRepository;
import com.edufelizardo.maissaudepublica.repositories.AtendimentoRepository;
import com.edufelizardo.maissaudepublica.repositories.CampanhaRepository;
import com.edufelizardo.maissaudepublica.repositories.EquipeRepository;
import com.edufelizardo.maissaudepublica.repositories.EsquemaVacinalRepository;
import com.edufelizardo.maissaudepublica.repositories.ImunobiologicoRepository;
import com.edufelizardo.maissaudepublica.repositories.PacienteRepository;
import com.edufelizardo.maissaudepublica.repositories.ProfissionalRepository;
import com.edufelizardo.maissaudepublica.repositories.RegistroVacinacaoRepository;
import com.edufelizardo.maissaudepublica.repositories.UnidadeDeSaudeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Vacinação, fatia I1 da Imunização (ADR-0147): o registro da dose (aplicada na unidade, ou transcrita da caderneta), a
 * retificação e a anulação (ADR-0062), a caderneta do paciente com a situação vacinal pelo calendário, a sala de vacina
 * do dia e os adscritos com dose atrasada. A caderneta é dado de saúde: segue as regras do prontuário (ADR-0076).
 */
@Service
public class VacinacaoService {

    public static final String REGISTRAR = "VACINACAO.REGISTRAR";
    private static final String CONSULTAR_PRONTUARIO = "PRONTUARIO.CONSULTAR";

    @Autowired private RegistroVacinacaoRepository repository;
    @Autowired private ImunobiologicoRepository imunobiologicoRepository;
    @Autowired private EsquemaVacinalRepository esquemaRepository;
    @Autowired private PacienteRepository pacienteRepository;
    @Autowired private ProfissionalRepository profissionalRepository;
    @Autowired private UnidadeDeSaudeRepository unidadeRepository;
    @Autowired private CampanhaRepository campanhaRepository;
    @Autowired private AtendimentoRepository atendimentoRepository;
    @Autowired private EquipeRepository equipeRepository;
    @Autowired private AdscricaoRepository adscricaoRepository;
    @Autowired private ControleDeAcesso controleDeAcesso;
    @Autowired private VinculoAssistencialService vinculoService;

    // ── Registro ───────────────────────────────────────────────────────────────────────────────

    @Transactional
    public RegistroVacinacaoDto registrar(RegistroVacinacaoRequestDto dto) {
        RegistroVacinacao r = novo(dto, null);
        r = repository.save(r);
        ContextoAuditoria.registro(r.getUuid());
        return dto(r);
    }

    /** Retificação (ADR-0062): nova versão ligada à anterior, do mesmo paciente; só a vigente se retifica. */
    @Transactional
    public RegistroVacinacaoDto retificar(UUID uuid, RegistroVacinacaoRequestDto dto) {
        RegistroVacinacao original = vigente(uuid);
        controleDeAcesso.exigirAutoriaOuSupervisao(original.getRegistradoPorCpf(), original.getUnidade());
        if (!original.getPaciente().getUuid().equals(dto.getPacienteId())) {
            throw new ResourceBadRequestException("A retificação precisa manter o mesmo paciente; o registro do paciente errado se anula.");
        }
        if (dto.getMotivoRetificacao() == null || dto.getMotivoRetificacao().isBlank()) {
            throw new ResourceBadRequestException("Informe o motivo da retificação.");
        }
        RegistroVacinacao r = novo(dto, original);
        r.setRetificacaoDe(original);
        r.setMotivoRetificacao(dto.getMotivoRetificacao().trim());
        r = repository.save(r);
        ContextoAuditoria.registro(r.getUuid());
        return dto(r);
    }

    /** Registrado por engano: sai da caderneta, com o motivo; o registro continua na trilha. */
    @Transactional
    public RegistroVacinacaoDto anular(UUID uuid, AnulacaoVacinacaoRequestDto dto) {
        RegistroVacinacao r = vigente(uuid);
        controleDeAcesso.exigir(REGISTRAR, r.getUnidade());
        controleDeAcesso.exigirAutoriaOuSupervisao(r.getRegistradoPorCpf(), r.getUnidade());
        ContextoAuditoria.paciente(r.getPaciente().getUuid());
        r.setAnulada(true);
        r.setMotivoAnulacao(dto.getMotivo().trim());
        r.setAnuladaEm(Instant.now());
        r.setAnuladaPorCpf(UsuarioAutenticado.cpf());
        ContextoAuditoria.registro(r.getUuid());
        return dto(repository.save(r));
    }

    // ── Caderneta ──────────────────────────────────────────────────────────────────────────────

    /** As doses vigentes, o calendário com a situação e as recomendações conforme a situação vacinal. */
    @Transactional(readOnly = true)
    public CadernetaVacinacaoDto caderneta(UUID pacienteId) {
        Paciente p = paciente(pacienteId);
        controleDeAcesso.exigirAlguma(CONSULTAR_PRONTUARIO, REGISTRAR);
        vinculoService.exigirVinculo(pacienteId);
        ContextoAuditoria.paciente(pacienteId);
        List<RegistroVacinacao> doses = repository.findVigentesDoPaciente(pacienteId);
        List<RegistroVacinacaoDto> dosesDto = doses.stream().map(VacinacaoService::dto).toList();
        LocalDate hoje = LocalDate.now();
        if (p.getDataNascimento() == null) {
            return new CadernetaVacinacaoDto(p.getUuid(), p.getNome(), null, null, false, false, 0, 0, List.of(), List.of(), dosesDto);
        }
        List<EsquemaVacinal> esquemas = esquemaRepository.findTodos();
        List<DoseCalendarioDto> calendario = CalendarioVacinal.situacao(p.getDataNascimento(), hoje, esquemas, doses);
        long atrasadas = calendario.stream().filter(d -> d.getSituacao() == SituacaoDoseCalendario.ATRASADA).count();
        long proximas = calendario.stream().filter(d -> d.getSituacao() == SituacaoDoseCalendario.PROXIMA).count();
        long idade = CalendarioVacinal.idadeMeses(p.getDataNascimento(), hoje);
        List<EsquemaVacinalDto> recomendacoes = esquemas.stream()
                .filter(e -> !e.isContaNaSituacao() && e.vigenteEm(hoje))
                .filter(e -> (e.getIdadeMinimaMeses() == null || idade >= e.getIdadeMinimaMeses())
                        && (e.getIdadeMaximaMeses() == null || idade <= e.getIdadeMaximaMeses()))
                .filter(e -> e.getPublico() != PublicoVacinal.GESTANTE || (p.getSexo() == Sexo.FEMININO && idade <= 600))
                .map(ImunizacaoCatalogoService::dto).toList();
        return new CadernetaVacinacaoDto(p.getUuid(), p.getNome(), p.getDataNascimento(), (int) idade, true, atrasadas == 0, atrasadas, proximas,
                calendario, recomendacoes, dosesDto);
    }

    // ── Sala de vacina ─────────────────────────────────────────────────────────────────────────

    /** As doses aplicadas na unidade no dia (padrão: hoje) e a contagem por imunobiológico. */
    @Transactional(readOnly = true)
    public VacinacaoDoDiaDto doDia(UUID unidadeId, LocalDate data) {
        UnidadeDeSaude u = unidade(unidadeId);
        controleDeAcesso.exigir(REGISTRAR, u);
        LocalDate dia = data == null ? LocalDate.now() : data;
        List<RegistroVacinacao> lista = repository.findAplicadasDaUnidade(unidadeId, dia.atStartOfDay(), dia.plusDays(1).atStartOfDay());
        Map<String, long[]> contagem = new TreeMap<>();
        Map<String, String> nomes = new HashMap<>();
        for (RegistroVacinacao r : lista) {
            contagem.computeIfAbsent(r.getImunobiologico().getSigla(), k -> new long[1])[0]++;
            nomes.put(r.getImunobiologico().getSigla(), r.getImunobiologico().getNome());
        }
        List<ContagemProducaoDto> porImuno = contagem.entrySet().stream()
                .map(x -> new ContagemProducaoDto(x.getKey(), nomes.get(x.getKey()), x.getValue()[0]))
                .sorted(Comparator.comparingLong(ContagemProducaoDto::getQuantidade).reversed()).toList();
        return new VacinacaoDoDiaDto(u.getUuid(), u.getNome(), dia, lista.size(), porImuno, lista.stream().map(VacinacaoService::dto).toList());
    }

    /** Os adscritos às equipes da unidade com alguma dose atrasada, os de atraso mais antigo primeiro (busca ativa). */
    @Transactional(readOnly = true)
    public List<AtrasadoVacinacaoDto> atrasados(UUID unidadeId) {
        UnidadeDeSaude u = unidade(unidadeId);
        controleDeAcesso.exigir(REGISTRAR, u);
        Map<UUID, Adscricao> adscritos = new LinkedHashMap<>();
        for (Equipe e : equipeRepository.findAllByOrderByNomeAsc()) {
            if (e.getUnidade() == null || !e.getUnidade().getUuid().equals(unidadeId)) {
                continue;
            }
            for (Adscricao a : adscricaoRepository.findByEquipe_UuidAndFimIsNull(e.getUuid())) {
                Paciente p = a.getPaciente();
                if (p.isAtivo() && p.getDataNascimento() != null) {
                    adscritos.putIfAbsent(p.getUuid(), a);
                }
            }
        }
        if (adscritos.isEmpty()) {
            return List.of();
        }
        Map<UUID, List<RegistroVacinacao>> doses = repository.findVigentesDosPacientes(adscritos.keySet()).stream()
                .collect(Collectors.groupingBy(r -> r.getPaciente().getUuid()));
        List<EsquemaVacinal> esquemas = esquemaRepository.findTodos();
        LocalDate hoje = LocalDate.now();
        List<AtrasadoVacinacaoDto> lista = new ArrayList<>();
        for (Adscricao a : adscritos.values()) {
            Paciente p = a.getPaciente();
            List<DoseCalendarioDto> atrasadas = CalendarioVacinal.situacao(p.getDataNascimento(), hoje, esquemas,
                            doses.getOrDefault(p.getUuid(), List.of())).stream()
                    .filter(d -> d.getSituacao() == SituacaoDoseCalendario.ATRASADA)
                    .sorted(Comparator.comparing(DoseCalendarioDto::getDataPrevista)).toList();
            if (atrasadas.isEmpty()) {
                continue;
            }
            lista.add(new AtrasadoVacinacaoDto(p.getUuid(), p.getNome(), p.getDataNascimento(), (int) CalendarioVacinal.idadeMeses(p.getDataNascimento(), hoje),
                    a.getEquipe().getNome(), a.getMicroarea() != null ? a.getMicroarea().getNome() : null,
                    atrasadas.stream().map(d -> d.getSigla() + ", " + d.getDoseRotulo().toLowerCase(Locale.ROOT)).toList(),
                    atrasadas.get(0).getDataPrevista()));
        }
        lista.sort(Comparator.comparing(AtrasadoVacinacaoDto::getAtrasadaDesde).thenComparing(AtrasadoVacinacaoDto::getPacienteNome));
        return lista;
    }

    // ── Apoio ──────────────────────────────────────────────────────────────────────────────────

    /**
     * Monta a dose com as regras: a aplicada pede quem aplicou e o lote; a transcrita pede a data; não há dose no futuro
     * nem antes do nascimento; a mesma dose vigente não se repete (409); fora da idade ou do intervalo do calendário, só
     * com a justificativa (422).
     */
    private RegistroVacinacao novo(RegistroVacinacaoRequestDto dto, RegistroVacinacao substitui) {
        Paciente paciente = paciente(dto.getPacienteId());
        UnidadeDeSaude unidade = unidade(dto.getUnidadeId());
        controleDeAcesso.exigir(REGISTRAR, unidade);
        ContextoAuditoria.paciente(paciente.getUuid());
        Imunobiologico imuno = imunobiologicoRepository.findById(dto.getImunobiologicoId()).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar o imunobiológico " + dto.getImunobiologicoId() + " em nossos registros."));
        if (!imuno.isAtivo() && dto.getOrigem() == OrigemRegistroVacinacao.APLICADA) {
            throw new ResourceUnprocessableEntityException("O imunobiológico " + imuno.getNome() + " está inativo no catálogo.");
        }
        boolean aplicada = dto.getOrigem() == OrigemRegistroVacinacao.APLICADA;
        LocalDateTime quando = dto.getAplicadaEm();
        if (quando == null) {
            if (!aplicada) {
                throw new ResourceBadRequestException("Informe a data da dose transcrita da caderneta.");
            }
            quando = LocalDateTime.now();
        }
        if (quando.isAfter(LocalDateTime.now().plusMinutes(5))) {
            throw new ResourceBadRequestException("A dose não pode ser no futuro.");
        }
        if (paciente.getDataNascimento() != null && quando.toLocalDate().isBefore(paciente.getDataNascimento())) {
            throw new ResourceBadRequestException("A dose não pode ser antes do nascimento do paciente.");
        }
        Profissional profissional = null;
        if (dto.getProfissionalMatricula() != null && !dto.getProfissionalMatricula().isBlank()) {
            profissional = profissionalRepository.findByMatricula(dto.getProfissionalMatricula().trim()).orElseThrow(() ->
                    new ResourceNotFoundException("Não foi possível encontrar um profissional com a matrícula " + dto.getProfissionalMatricula()
                            + " em nossos registros."));
        }
        if (aplicada && profissional == null) {
            throw new ResourceBadRequestException("Informe quem aplicou a dose.");
        }
        if (aplicada && (dto.getLote() == null || dto.getLote().isBlank())) {
            throw new ResourceBadRequestException("Informe o lote da dose aplicada.");
        }
        Campanha campanha = null;
        if (dto.getCampanhaId() != null) {
            campanha = campanhaRepository.findById(dto.getCampanhaId()).orElseThrow(() -> new ResourceNotFoundException(
                    "Não foi possível encontrar a campanha " + dto.getCampanhaId() + " em nossos registros."));
            if (campanha.getTipo() != TipoCampanha.VACINACAO) {
                throw new ResourceUnprocessableEntityException("A campanha " + campanha.getNome() + " não é de vacinação.");
            }
        }
        Atendimento atendimento = null;
        if (dto.getAtendimentoId() != null) {
            atendimento = atendimentoRepository.findById(dto.getAtendimentoId()).orElseThrow(() -> new ResourceNotFoundException(
                    "Não foi possível encontrar o atendimento " + dto.getAtendimentoId() + " em nossos registros."));
            if (!atendimento.getPaciente().getUuid().equals(paciente.getUuid())) {
                throw new ResourceUnprocessableEntityException("O atendimento é de outro paciente.");
            }
        }
        List<RegistroVacinacao> vigentes = repository.findVigentesDoPaciente(paciente.getUuid()).stream()
                .filter(r -> substitui == null || !r.getUuid().equals(substitui.getUuid())).toList();
        LocalDate hoje = quando.toLocalDate();
        // A mesma vacina e dose pode estar em mais de um público (hepatite B ao nascer e no adulto, influenza da criança, da
        // gestante e do idoso): vale o esquema em cuja faixa de idade o paciente está; sem nenhum, o primeiro do calendário.
        Long idadeNaDose = paciente.getDataNascimento() != null ? CalendarioVacinal.idadeMeses(paciente.getDataNascimento(), hoje) : null;
        EsquemaVacinal esquema = esquemaRepository.findTodos().stream()
                .filter(e -> e.getImunobiologico().getUuid().equals(imuno.getUuid()) && e.getDose() == dto.getDose() && e.vigenteEm(hoje))
                .min(Comparator.comparing((EsquemaVacinal e) -> !cabe(e, idadeNaDose)).thenComparing(e -> !e.isContaNaSituacao())
                        .thenComparingInt(EsquemaVacinal::getOrdem))
                .orElse(null);
        exigirSemRepeticao(dto.getDose(), imuno, esquema, vigentes, quando);
        String justificativa = dto.getJustificativa() == null || dto.getJustificativa().isBlank() ? null : dto.getJustificativa().trim();
        if (aplicada && esquema != null && paciente.getDataNascimento() != null) {
            String fora = foraDoEsquema(esquema, paciente.getDataNascimento(), quando, imuno, vigentes);
            if (fora != null && justificativa == null) {
                throw new ResourceUnprocessableEntityException("Dose fora do calendário (" + fora + "): informe a justificativa para registrar.");
            }
        }
        RegistroVacinacao r = new RegistroVacinacao();
        r.setPaciente(paciente);
        r.setImunobiologico(imuno);
        r.setEsquema(esquema);
        r.setDose(dto.getDose());
        r.setAplicadaEm(quando);
        r.setUnidade(unidade);
        r.setProfissional(profissional);
        r.setOrigem(dto.getOrigem());
        r.setLocalOrigem(aplicada ? null : texto(dto.getLocalOrigem()));
        r.setLote(texto(dto.getLote()));
        r.setFabricante(texto(dto.getFabricante()));
        r.setVia(dto.getVia() != null ? dto.getVia() : aplicada ? imuno.getVia() : null);
        r.setLocalAplicacao(dto.getLocalAplicacao());
        r.setEstrategia(dto.getEstrategia() != null ? dto.getEstrategia() : campanha != null ? EstrategiaVacinacao.CAMPANHA : EstrategiaVacinacao.ROTINA);
        r.setCampanha(campanha);
        r.setGestante(Boolean.TRUE.equals(dto.getGestante()));
        r.setJustificativa(justificativa);
        r.setAtendimento(atendimento);
        r.setObservacao(texto(dto.getObservacao()));
        r.setRegistradoEm(Instant.now());
        r.setRegistradoPorCpf(UsuarioAutenticado.cpf());
        return r;
    }

    /**
     * A mesma dose vigente não se repete (409). A anual, uma por ano; a adicional e a que se repete no calendário (o dT
     * a cada 10 anos) podem se repetir.
     */
    private static void exigirSemRepeticao(DoseVacinal dose, Imunobiologico imuno, EsquemaVacinal esquema, List<RegistroVacinacao> vigentes,
                                           LocalDateTime quando) {
        if (dose == DoseVacinal.ADICIONAL || (esquema != null && esquema.getRepeticaoAnos() != null)) {
            return;
        }
        boolean repetida = vigentes.stream().anyMatch(r -> r.getImunobiologico().getUuid().equals(imuno.getUuid()) && r.getDose() == dose
                && (dose != DoseVacinal.ANUAL || r.getAplicadaEm().getYear() == quando.getYear()));
        if (repetida) {
            throw new ResourceConflictException("O paciente já tem " + imuno.getSigla() + ", " + dose.rotulo().toLowerCase(Locale.ROOT)
                    + (dose == DoseVacinal.ANUAL ? " neste ano" : "") + ". Para corrigir, retifique o registro.");
        }
    }

    private static boolean cabe(EsquemaVacinal e, Long idade) {
        return idade != null && (e.getIdadeMinimaMeses() == null || idade >= e.getIdadeMinimaMeses())
                && (e.getIdadeMaximaMeses() == null || idade <= e.getIdadeMaximaMeses());
    }

    /** Fora da idade mínima ou máxima, ou antes do intervalo mínimo desde a dose anterior do mesmo imunobiológico. */
    static String foraDoEsquema(EsquemaVacinal e, LocalDate nascimento, LocalDateTime quando, Imunobiologico imuno, List<RegistroVacinacao> vigentes) {
        long idade = CalendarioVacinal.idadeMeses(nascimento, quando.toLocalDate());
        if (e.getIdadeMinimaMeses() != null && idade < e.getIdadeMinimaMeses()) {
            return "idade de " + idade + " meses, abaixo da mínima de " + e.getIdadeMinimaMeses();
        }
        if (e.getIdadeMaximaMeses() != null && idade > e.getIdadeMaximaMeses()) {
            return "idade de " + idade + " meses, acima da máxima de " + e.getIdadeMaximaMeses();
        }
        if (e.getIntervaloMinimoDias() != null) {
            Optional<RegistroVacinacao> anterior = vigentes.stream()
                    .filter(r -> r.getImunobiologico().getUuid().equals(imuno.getUuid()) && r.getAplicadaEm().isBefore(quando))
                    .max(Comparator.comparing(RegistroVacinacao::getAplicadaEm));
            if (anterior.isPresent()) {
                long dias = ChronoUnit.DAYS.between(anterior.get().getAplicadaEm().toLocalDate(), quando.toLocalDate());
                if (dias < e.getIntervaloMinimoDias()) {
                    return dias + " dias desde a dose anterior, abaixo do intervalo mínimo de " + e.getIntervaloMinimoDias();
                }
            }
        }
        return null;
    }

    private RegistroVacinacao vigente(UUID uuid) {
        RegistroVacinacao r = repository.findById(uuid).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar o registro de vacinação " + uuid + " em nossos registros."));
        if (r.isAnulada()) {
            throw new ResourceUnprocessableEntityException("Este registro foi anulado.");
        }
        if (repository.existsByRetificacaoDe_Uuid(uuid)) {
            throw new ResourceUnprocessableEntityException("Este registro já foi retificado: use a versão vigente.");
        }
        return r;
    }

    private Paciente paciente(UUID id) {
        return pacienteRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar um paciente com o id " + id + " em nossos registros."));
    }

    private UnidadeDeSaude unidade(UUID id) {
        return unidadeRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar uma unidade de saúde com o id " + id + " em nossos registros."));
    }

    private static String texto(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }

    static RegistroVacinacaoDto dto(RegistroVacinacao r) {
        Imunobiologico i = r.getImunobiologico();
        Profissional p = r.getProfissional();
        return new RegistroVacinacaoDto(r.getUuid(), r.getPaciente().getUuid(), r.getPaciente().getNome(), i.getUuid(), i.getNome(), i.getSigla(),
                r.getDose(), r.getDose().rotulo(), r.getAplicadaEm(), r.getUnidade().getUuid(), r.getUnidade().getNome(),
                p != null ? p.getMatricula() : null, p != null ? p.getNome() : null, r.getOrigem(), r.getLocalOrigem(), r.getLote(), r.getFabricante(),
                r.getVia(), r.getLocalAplicacao(), r.getEstrategia(), r.getCampanha() != null ? r.getCampanha().getUuid() : null,
                r.getCampanha() != null ? r.getCampanha().getNome() : null, r.isGestante(), r.getJustificativa(), r.getObservacao(),
                r.getRetificacaoDe() != null ? r.getRetificacaoDe().getUuid() : null, r.getMotivoRetificacao(), r.getRegistradoEm());
    }
}
