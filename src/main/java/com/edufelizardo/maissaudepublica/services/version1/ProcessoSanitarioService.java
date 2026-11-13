package com.edufelizardo.maissaudepublica.services.version1;

import com.edufelizardo.maissaudepublica.config.ContextoAuditoria;
import com.edufelizardo.maissaudepublica.config.UsuarioAutenticado;
import com.edufelizardo.maissaudepublica.exceptions.ResourceBadRequestException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceForbiddenException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceNotFoundException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceUnprocessableEntityException;
import com.edufelizardo.maissaudepublica.models.DenunciaSanitaria;
import com.edufelizardo.maissaudepublica.models.EstabelecimentoRegulado;
import com.edufelizardo.maissaudepublica.models.InspecaoSanitaria;
import com.edufelizardo.maissaudepublica.models.IrregularidadeSanitaria;
import com.edufelizardo.maissaudepublica.models.LicencaSanitaria;
import com.edufelizardo.maissaudepublica.models.MedidaSanitaria;
import com.edufelizardo.maissaudepublica.models.PenalidadeSanitaria;
import com.edufelizardo.maissaudepublica.models.ProcessoSanitario;
import com.edufelizardo.maissaudepublica.models.Profissional;
import com.edufelizardo.maissaudepublica.models.TermoSanitario;
import com.edufelizardo.maissaudepublica.models.VersaoDocumento;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.AutoInfracaoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.CienciaRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.DecisaoSanitariaRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.EncerramentoMedidaRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.JulgamentoRecursoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.MedidaSanitariaRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.ProtocoloSanitarioRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.ProcessoSanitarioDtos;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.RelatorioDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.VigilanciaSanitariaDtos;
import com.edufelizardo.maissaudepublica.models.enuns.AgravanteSanitaria;
import com.edufelizardo.maissaudepublica.models.enuns.AtenuanteSanitaria;
import com.edufelizardo.maissaudepublica.models.enuns.DonoDocumento;
import com.edufelizardo.maissaudepublica.models.enuns.FaseProcessoSanitario;
import com.edufelizardo.maissaudepublica.models.enuns.GravidadeIrregularidade;
import com.edufelizardo.maissaudepublica.models.enuns.ResultadoDecisaoSanitaria;
import com.edufelizardo.maissaudepublica.models.enuns.ResultadoRecursoSanitario;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoIrregularidade;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoLicencaSanitaria;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoMedidaSanitaria;
import com.edufelizardo.maissaudepublica.models.enuns.TipoMedidaSanitaria;
import com.edufelizardo.maissaudepublica.models.enuns.TipoPenalidadeSanitaria;
import com.edufelizardo.maissaudepublica.models.enuns.TipoRelatorio;
import com.edufelizardo.maissaudepublica.models.enuns.TipoTermoSanitario;
import com.edufelizardo.maissaudepublica.repositories.DenunciaSanitariaRepository;
import com.edufelizardo.maissaudepublica.repositories.EstabelecimentoReguladoRepository;
import com.edufelizardo.maissaudepublica.repositories.InspecaoSanitariaRepository;
import com.edufelizardo.maissaudepublica.repositories.IrregularidadeSanitariaRepository;
import com.edufelizardo.maissaudepublica.repositories.LicencaSanitariaRepository;
import com.edufelizardo.maissaudepublica.repositories.MedidaSanitariaRepository;
import com.edufelizardo.maissaudepublica.repositories.PenalidadeSanitariaRepository;
import com.edufelizardo.maissaudepublica.repositories.ProcessoSanitarioRepository;
import com.edufelizardo.maissaudepublica.repositories.ProfissionalRepository;
import com.edufelizardo.maissaudepublica.repositories.TermoSanitarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.text.NumberFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Processo administrativo sanitário e medidas, fatia V3b da Vigilância (ADR-0200, desenho na ADR-0110): o auto de infração abre o
 * processo (Lei 6.437/1977: ciência, defesa em 15 dias, decisão, recurso em 15 dias com efeito suspensivo, encerramento); as
 * penalidades do art. 2º valem no encerramento; a interdição cautelar (até 90 dias) e a apreensão são imediatas; cada ato gera o termo
 * em PDF com o código da verificação pública.
 */
@Service
public class ProcessoSanitarioService {

    public static final String JULGAR = "VISA.JULGAR";
    static final String[] LEITURA = {VigilanciaSanitariaService.INSPECIONAR, VigilanciaSanitariaService.GERENCIAR, JULGAR};
    /** Defesa (art. 22) e recurso (art. 30): 15 dias da ciência. */
    static final int PRAZO_DIAS = 15;
    /** Interdição cautelar: até 90 dias (art. 23, §4º). */
    static final int CAUTELAR_MAXIMO_DIAS = 90;
    static final int DIAS_DECISAO_PARADA = 30;
    static final int DIAS_CAUTELAR_VENCENDO = 10;
    private static final Set<TipoPenalidadeSanitaria> DA_LICENCA = EnumSet.of(TipoPenalidadeSanitaria.SUSPENSAO_LICENCA, TipoPenalidadeSanitaria.CANCELAMENTO_LICENCA);
    private static final Set<TipoPenalidadeSanitaria> INTERDICOES = EnumSet.of(TipoPenalidadeSanitaria.INTERDICAO_PARCIAL, TipoPenalidadeSanitaria.INTERDICAO_TOTAL);
    private static final String LETRAS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final SecureRandom SORTEIO = new SecureRandom();
    private static final DateTimeFormatter DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DATA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm").withZone(ZoneId.of("America/Sao_Paulo"));

    @Autowired private ProcessoSanitarioRepository processoRepository;
    @Autowired private PenalidadeSanitariaRepository penalidadeRepository;
    @Autowired private MedidaSanitariaRepository medidaRepository;
    @Autowired private TermoSanitarioRepository termoRepository;
    @Autowired private EstabelecimentoReguladoRepository estabelecimentoRepository;
    @Autowired private InspecaoSanitariaRepository inspecaoRepository;
    @Autowired private IrregularidadeSanitariaRepository irregularidadeRepository;
    @Autowired private LicencaSanitariaRepository licencaRepository;
    @Autowired private ProfissionalRepository profissionalRepository;
    @Autowired private DocumentoService documentoService;
    @Autowired private ControleDeAcesso controleDeAcesso;
    @Autowired private EscopoDosIndicadores escopo;
    @Autowired private DenunciaSanitariaRepository denunciaRepository;

    @Value("${app.security.recuperacao-senha.url-frontend:}")
    private String frontendUrl;

    // ── Processos ──────────────────────────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<ProcessoSanitarioDtos.Processo> processos(UUID estabelecimentoId, FaseProcessoSanitario fase) {
        controleDeAcesso.exigirAlguma(LEITURA);
        return processoRepository.findTodos().stream()
                .filter(p -> estabelecimentoId == null || p.getEstabelecimento().getUuid().equals(estabelecimentoId))
                .filter(p -> fase == null || p.getFase() == fase).map(this::dto).toList();
    }

    @Transactional(readOnly = true)
    public ProcessoSanitarioDtos.Processo processo(UUID id) {
        controleDeAcesso.exigirAlguma(LEITURA);
        ProcessoSanitario p = processoOu404(id);
        ContextoAuditoria.registro(p.getUuid());
        return dto(p);
    }

    /**
     * Lavra o auto de infração e abre o processo. As irregularidades são abertas e do estabelecimento (422) e passam a autuadas; a
     * inspeção, do mesmo estabelecimento (422); a lavratura não é futura nem anterior à inspeção (400); a gravidade nunca fica abaixo da
     * maior irregularidade (400). Os fiscais pela matrícula (404); com login, o logado está entre eles (403) e assina.
     */
    @Transactional
    public ProcessoSanitarioDtos.Processo lavrar(AutoInfracaoRequestDto dto) {
        controleDeAcesso.exigirAlguma(VigilanciaSanitariaService.INSPECIONAR);
        EstabelecimentoRegulado e = estabelecimentoOu404(dto.getEstabelecimentoId());
        LocalDate data = dto.getDataLavratura() != null ? dto.getDataLavratura() : LocalDate.now();
        naoFutura(data, "A lavratura");
        InspecaoSanitaria inspecao = null;
        if (dto.getInspecaoId() != null) {
            inspecao = inspecaoRepository.findById(dto.getInspecaoId()).orElseThrow(() -> new ResourceNotFoundException(
                    "Não foi possível encontrar a inspeção " + dto.getInspecaoId() + " em nossos registros."));
            if (!inspecao.getEstabelecimento().getUuid().equals(e.getUuid())) {
                throw new ResourceUnprocessableEntityException("A inspeção é de outro estabelecimento.");
            }
            if (data.isBefore(inspecao.getDataInspecao())) {
                throw new ResourceBadRequestException("O auto não pode ser lavrado antes da inspeção.");
            }
        }
        List<IrregularidadeSanitaria> irregularidades = new ArrayList<>();
        for (UUID rid : new LinkedHashSet<>(dto.getIrregularidades())) {
            IrregularidadeSanitaria r = irregularidadeRepository.findById(rid).orElseThrow(() -> new ResourceNotFoundException(
                    "Não foi possível encontrar a irregularidade " + rid + " em nossos registros."));
            if (!r.getEstabelecimento().getUuid().equals(e.getUuid()) || r.getSituacao() != SituacaoIrregularidade.ABERTA) {
                throw new ResourceUnprocessableEntityException("Só se autua irregularidade aberta deste estabelecimento.");
            }
            irregularidades.add(r);
        }
        GravidadeIrregularidade maior = irregularidades.stream().map(IrregularidadeSanitaria::getGravidade).max(Comparator.naturalOrder()).orElseThrow();
        GravidadeIrregularidade gravidade = dto.getGravidade() != null ? dto.getGravidade() : maior;
        if (gravidade.compareTo(maior) < 0) {
            throw new ResourceBadRequestException("A gravidade do auto não fica abaixo da maior irregularidade (" + maior + ").");
        }
        Set<Profissional> fiscais = fiscais(dto.getFiscais(), "O auto é assinado por quem está logado: inclua a sua própria matrícula entre os fiscais.");

        ProcessoSanitario p = new ProcessoSanitario();
        p.setNumero(numero(data.getYear(), processoRepository::countByNumeroStartingWith));
        p.setEstabelecimento(e);
        p.setInspecao(inspecao);
        p.setDispositivoLegal(dto.getDispositivoLegal().trim());
        p.setDescricao(dto.getDescricao().trim());
        p.setGravidade(gravidade);
        p.setFase(FaseProcessoSanitario.AGUARDANDO_CIENCIA);
        p.setDataLavratura(data);
        p.setFiscais(fiscais);
        p.setLavradoEm(Instant.now());
        p.setLavradoPorCpf(UsuarioAutenticado.cpf());
        processoRepository.save(p);
        for (IrregularidadeSanitaria r : irregularidades) {
            r.setSituacao(SituacaoIrregularidade.AUTUADA);
            r.setProcesso(p);
            irregularidadeRepository.save(r);
        }
        p.setHashAuto(AssinaturaResultado.sha256(String.join("\n", "processo=" + p.getUuid(), "numero=" + p.getNumero(),
                "estabelecimento=" + e.getDocumento(), "data=" + data, "dispositivo=" + p.getDispositivoLegal(), "descricao=" + p.getDescricao(),
                "gravidade=" + gravidade, "fiscais=" + fiscais.stream().map(Profissional::getMatricula).sorted().collect(Collectors.joining(",")),
                "irregularidades=" + irregularidades.stream().map(r -> r.getUuid().toString()).sorted().collect(Collectors.joining(",")))));
        processoRepository.save(p);
        emitirAuto(p, irregularidades);
        ContextoAuditoria.registro(p.getUuid());
        ContextoAuditoria.detalhe("Auto de infração " + p.getNumero() + " (" + gravidade + ").");
        return dto(p);
    }

    /** A ciência do auto (abre a defesa) ou da decisão (abre o recurso); fora dessas fases, 422. A data não é futura nem anterior ao ato (400). */
    @Transactional
    public ProcessoSanitarioDtos.Processo ciencia(UUID id, CienciaRequestDto dto) {
        controleDeAcesso.exigirAlguma(VigilanciaSanitariaService.INSPECIONAR, VigilanciaSanitariaService.GERENCIAR);
        ProcessoSanitario p = processoOu404(id);
        LocalDate data = dto.getData() != null ? dto.getData() : LocalDate.now();
        naoFutura(data, "A ciência");
        if (p.getFase() == FaseProcessoSanitario.AGUARDANDO_CIENCIA) {
            naoAntes(data, p.getDataLavratura(), "A ciência não pode ser antes da lavratura.");
            p.setCienciaAutoEm(data);
            p.setFormaCienciaAuto(dto.getForma());
            p.setFase(FaseProcessoSanitario.EM_DEFESA);
        } else if (p.getFase() == FaseProcessoSanitario.DECIDIDO && p.getCienciaDecisaoEm() == null) {
            naoAntes(data, p.getDataDecisao(), "A ciência não pode ser antes da decisão.");
            p.setCienciaDecisaoEm(data);
            p.setFormaCienciaDecisao(dto.getForma());
        } else {
            throw new ResourceUnprocessableEntityException("Não há ciência a registrar nesta fase do processo.");
        }
        return salvar(p, "Ciência registrada (" + dto.getForma() + ").");
    }

    /** A defesa, só na fase de defesa (422). Depois dos 15 dias é recebida como intempestiva, enquanto não houver decisão. */
    @Transactional
    public ProcessoSanitarioDtos.Processo defesa(UUID id, ProtocoloSanitarioRequestDto dto) {
        controleDeAcesso.exigirAlguma(VigilanciaSanitariaService.GERENCIAR);
        ProcessoSanitario p = processoOu404(id);
        if (p.getFase() != FaseProcessoSanitario.EM_DEFESA) {
            throw new ResourceUnprocessableEntityException("A defesa só é recebida depois da ciência do auto e antes da decisão.");
        }
        LocalDate data = dto.getData() != null ? dto.getData() : LocalDate.now();
        naoFutura(data, "A defesa");
        naoAntes(data, p.getCienciaAutoEm(), "A defesa não pode ser antes da ciência do auto.");
        p.setDefesaEm(data);
        p.setDefesaTexto(dto.getTexto().trim());
        p.setDefesaIntempestiva(data.isAfter(prazoDefesa(p)));
        p.setFase(FaseProcessoSanitario.AGUARDANDO_DECISAO);
        return salvar(p, Boolean.TRUE.equals(p.getDefesaIntempestiva()) ? "Defesa intempestiva." : "Defesa no prazo.");
    }

    /**
     * A decisão da autoridade julgadora: com a defesa, ou sem ela depois do prazo (revelia); antes disso, 422. O arquivamento encerra o
     * processo e devolve as irregularidades às abertas; a penalidade espera a ciência e o prazo de recurso. Penalidades: ao menos uma, sem
     * repetir, a multa com valor, a suspensão com a data final, sem interdição parcial e total juntas nem suspensão e cancelamento juntos
     * (400); suspender ou cancelar pede licença vigente (422).
     */
    @Transactional
    public ProcessoSanitarioDtos.Processo decidir(UUID id, DecisaoSanitariaRequestDto dto) {
        controleDeAcesso.exigirAlguma(JULGAR);
        ProcessoSanitario p = processoOu404(id);
        LocalDate data = dto.getData() != null ? dto.getData() : LocalDate.now();
        naoFutura(data, "A decisão");
        boolean revelia = p.getFase() == FaseProcessoSanitario.EM_DEFESA && data.isAfter(prazoDefesa(p));
        if (p.getFase() != FaseProcessoSanitario.AGUARDANDO_DECISAO && !revelia) {
            throw new ResourceUnprocessableEntityException(p.getFase() == FaseProcessoSanitario.EM_DEFESA
                    ? "O prazo de defesa corre até " + prazoDefesa(p) + "." : "O processo não está aguardando decisão.");
        }
        naoAntes(data, p.getDefesaEm() != null ? p.getDefesaEm() : p.getCienciaAutoEm(), "A decisão não pode ser antes da defesa ou da ciência.");
        List<DecisaoSanitariaRequestDto.Penalidade> penalidades = dto.getPenalidades() == null ? List.of() : dto.getPenalidades();
        if (dto.getResultado() == ResultadoDecisaoSanitaria.ARQUIVAMENTO && !penalidades.isEmpty()) {
            throw new ResourceBadRequestException("O arquivamento não leva penalidade.");
        }
        if (dto.getResultado() == ResultadoDecisaoSanitaria.PENALIDADE) {
            validarPenalidades(p, penalidades, data);
        }
        p.setDecisaoResultado(dto.getResultado());
        p.setDecisaoFundamentacao(dto.getFundamentacao().trim());
        p.setAtenuantes(juntar(dto.getAtenuantes()));
        p.setAgravantes(juntar(dto.getAgravantes()));
        p.setDataDecisao(data);
        p.setDecididoPorCpf(UsuarioAutenticado.cpf());
        for (DecisaoSanitariaRequestDto.Penalidade x : penalidades) {
            penalidadeRepository.save(new PenalidadeSanitaria(null, p, x.getTipo(), x.getTipo() == TipoPenalidadeSanitaria.MULTA ? x.getValorMulta() : null,
                    x.getTipo() == TipoPenalidadeSanitaria.SUSPENSAO_LICENCA ? x.getSuspensaoAte() : null, limpo(x.getDescricao()), false));
        }
        if (dto.getResultado() == ResultadoDecisaoSanitaria.ARQUIVAMENTO) {
            p.setFase(FaseProcessoSanitario.ENCERRADO);
            p.setEncerradoEm(data);
            reabrirIrregularidades(p);
        } else {
            p.setFase(FaseProcessoSanitario.DECIDIDO);
        }
        processoRepository.save(p);
        emitirDecisao(p, TipoTermoSanitario.DECISAO);
        return salvar(p, "Decisão: " + dto.getResultado() + (penalidades.isEmpty() ? "." : ", " + penalidades.size() + " penalidade(s)."));
    }

    private void validarPenalidades(ProcessoSanitario p, List<DecisaoSanitariaRequestDto.Penalidade> penalidades, LocalDate data) {
        if (penalidades.isEmpty()) {
            throw new ResourceBadRequestException("Informe ao menos uma penalidade.");
        }
        Set<TipoPenalidadeSanitaria> tipos = new HashSet<>();
        for (DecisaoSanitariaRequestDto.Penalidade x : penalidades) {
            if (!tipos.add(x.getTipo())) {
                throw new ResourceBadRequestException("A penalidade " + x.getTipo() + " está repetida.");
            }
            if (x.getTipo() == TipoPenalidadeSanitaria.MULTA && x.getValorMulta() == null) {
                throw new ResourceBadRequestException("A multa precisa do valor.");
            }
            if (x.getTipo() == TipoPenalidadeSanitaria.SUSPENSAO_LICENCA && (x.getSuspensaoAte() == null || !x.getSuspensaoAte().isAfter(data))) {
                throw new ResourceBadRequestException("A suspensão da licença precisa da data final, depois da decisão.");
            }
        }
        if (tipos.containsAll(INTERDICOES) || tipos.containsAll(DA_LICENCA)) {
            throw new ResourceBadRequestException("Interdição parcial e total, ou suspensão e cancelamento da licença, não vão juntas.");
        }
        if (tipos.stream().anyMatch(DA_LICENCA::contains) && licencaVigente(p.getEstabelecimento(), data) == null) {
            throw new ResourceUnprocessableEntityException("O estabelecimento não tem licença vigente para suspender ou cancelar.");
        }
    }

    /** O recurso, depois da ciência da decisão (422) e dentro dos 15 dias (422: o intempestivo não é conhecido). */
    @Transactional
    public ProcessoSanitarioDtos.Processo recurso(UUID id, ProtocoloSanitarioRequestDto dto) {
        controleDeAcesso.exigirAlguma(VigilanciaSanitariaService.GERENCIAR);
        ProcessoSanitario p = processoOu404(id);
        if (p.getFase() != FaseProcessoSanitario.DECIDIDO) {
            throw new ResourceUnprocessableEntityException("Só cabe recurso da decisão que aplicou penalidade, antes do encerramento.");
        }
        if (p.getCienciaDecisaoEm() == null) {
            throw new ResourceUnprocessableEntityException("Registre antes a ciência da decisão.");
        }
        LocalDate data = dto.getData() != null ? dto.getData() : LocalDate.now();
        naoFutura(data, "O recurso");
        naoAntes(data, p.getCienciaDecisaoEm(), "O recurso não pode ser antes da ciência da decisão.");
        if (data.isAfter(prazoRecurso(p))) {
            throw new ResourceUnprocessableEntityException("O prazo de recurso acabou em " + prazoRecurso(p) + ": o recurso intempestivo não é conhecido.");
        }
        p.setRecursoEm(data);
        p.setRecursoTexto(dto.getTexto().trim());
        p.setFase(FaseProcessoSanitario.EM_RECURSO);
        return salvar(p, "Recurso protocolado.");
    }

    /**
     * Julga o recurso (só em recurso, 422) e encerra: provido revoga as penalidades e reabre as irregularidades; parcialmente provido
     * mantém as informadas (ao menos uma e não todas, do processo; 400); improvido mantém todas. As mantidas valem a partir daqui.
     */
    @Transactional
    public ProcessoSanitarioDtos.Processo julgarRecurso(UUID id, JulgamentoRecursoRequestDto dto) {
        controleDeAcesso.exigirAlguma(JULGAR);
        ProcessoSanitario p = processoOu404(id);
        if (p.getFase() != FaseProcessoSanitario.EM_RECURSO) {
            throw new ResourceUnprocessableEntityException("O processo não está em recurso.");
        }
        LocalDate data = dto.getData() != null ? dto.getData() : LocalDate.now();
        naoFutura(data, "O julgamento");
        naoAntes(data, p.getRecursoEm(), "O julgamento não pode ser antes do recurso.");
        List<PenalidadeSanitaria> penalidades = penalidadeRepository.findByProcesso_Uuid(p.getUuid());
        Set<UUID> mantidas = dto.getPenalidadesMantidas() == null ? Set.of() : new HashSet<>(dto.getPenalidadesMantidas());
        switch (dto.getResultado()) {
            case PROVIDO -> penalidades.forEach(x -> x.setRevogada(true));
            case IMPROVIDO -> {
            }
            case PARCIALMENTE_PROVIDO -> {
                Set<UUID> ids = penalidades.stream().map(PenalidadeSanitaria::getUuid).collect(Collectors.toSet());
                if (mantidas.isEmpty() || mantidas.size() >= ids.size() || !ids.containsAll(mantidas)) {
                    throw new ResourceBadRequestException("No parcialmente provido, informe as penalidades que ficam: ao menos uma e não todas.");
                }
                penalidades.forEach(x -> x.setRevogada(!mantidas.contains(x.getUuid())));
            }
        }
        penalidadeRepository.saveAll(penalidades);
        p.setRecursoResultado(dto.getResultado());
        p.setRecursoFundamentacao(dto.getFundamentacao().trim());
        p.setDataJulgamentoRecurso(data);
        p.setRecursoJulgadoPorCpf(UsuarioAutenticado.cpf());
        if (dto.getResultado() == ResultadoRecursoSanitario.PROVIDO) {
            reabrirIrregularidades(p);
        }
        encerrarComEfeitos(p, data);
        emitirDecisao(p, TipoTermoSanitario.DECISAO_RECURSO);
        return salvar(p, "Recurso " + dto.getResultado() + "; processo encerrado.");
    }

    /** Encerra a decisão sem recurso depois do prazo (422 antes da ciência ou do fim do prazo); as penalidades passam a valer. */
    @Transactional
    public ProcessoSanitarioDtos.Processo encerrar(UUID id) {
        controleDeAcesso.exigirAlguma(VigilanciaSanitariaService.GERENCIAR);
        ProcessoSanitario p = processoOu404(id);
        if (p.getFase() != FaseProcessoSanitario.DECIDIDO || p.getCienciaDecisaoEm() == null) {
            throw new ResourceUnprocessableEntityException("Só se encerra a decisão com ciência registrada e sem recurso.");
        }
        LocalDate hoje = LocalDate.now();
        if (!hoje.isAfter(prazoRecurso(p))) {
            throw new ResourceUnprocessableEntityException("O prazo de recurso corre até " + prazoRecurso(p) + ".");
        }
        encerrarComEfeitos(p, hoje);
        return salvar(p, "Processo encerrado sem recurso.");
    }

    /** As penalidades mantidas passam a valer: a licença suspensa ou cassada e a interdição com o termo. */
    private void encerrarComEfeitos(ProcessoSanitario p, LocalDate data) {
        p.setFase(FaseProcessoSanitario.ENCERRADO);
        p.setEncerradoEm(data);
        processoRepository.save(p);
        for (PenalidadeSanitaria x : penalidadeRepository.findByProcesso_Uuid(p.getUuid())) {
            if (x.isRevogada()) {
                continue;
            }
            switch (x.getTipo()) {
                case SUSPENSAO_LICENCA, CANCELAMENTO_LICENCA -> {
                    LicencaSanitaria l = licencaVigente(p.getEstabelecimento(), data);
                    if (l != null) {
                        boolean suspensao = x.getTipo() == TipoPenalidadeSanitaria.SUSPENSAO_LICENCA;
                        l.setSituacao(suspensao ? SituacaoLicencaSanitaria.SUSPENSA : SituacaoLicencaSanitaria.CASSADA);
                        l.setSuspensaAte(suspensao ? x.getSuspensaoAte() : null);
                        licencaRepository.save(l);
                    }
                }
                case INTERDICAO_PARCIAL, INTERDICAO_TOTAL -> {
                    MedidaSanitaria m = novaMedida(p.getEstabelecimento(), TipoMedidaSanitaria.INTERDICAO, x.getTipo() == TipoPenalidadeSanitaria.INTERDICAO_TOTAL,
                            "Interdição pela decisão do processo " + p.getNumero() + (x.getDescricao() != null ? ": " + x.getDescricao() : "."), data);
                    m.setProcesso(p);
                    medidaRepository.save(m);
                    emitirMedida(m, TipoTermoSanitario.INTERDICAO);
                }
                default -> {
                }
            }
        }
    }

    private void reabrirIrregularidades(ProcessoSanitario p) {
        for (IrregularidadeSanitaria r : irregularidadeRepository.findByProcesso_UuidOrderByRegistradoEmAsc(p.getUuid())) {
            if (r.getSituacao() == SituacaoIrregularidade.AUTUADA) {
                r.setSituacao(SituacaoIrregularidade.ABERTA);
                irregularidadeRepository.save(r);
            }
        }
    }

    // ── Medidas ────────────────────────────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<ProcessoSanitarioDtos.Medida> medidas(UUID estabelecimentoId, SituacaoMedidaSanitaria situacao) {
        controleDeAcesso.exigirAlguma(LEITURA);
        return medidaRepository.findTodas().stream()
                .filter(m -> estabelecimentoId == null || m.getEstabelecimento().getUuid().equals(estabelecimentoId))
                .filter(m -> situacao == null || m.getSituacao() == situacao).map(this::dto).toList();
    }

    /**
     * A interdição cautelar (fim previsto até 90 dias depois do início, 90 por padrão; 400) ou a apreensão (produto e quantidade, 400). A
     * interdição definitiva vem da decisão (400). Estabelecimento ativo (422); a inspeção do mesmo estabelecimento (422); início não futuro.
     */
    @Transactional
    public ProcessoSanitarioDtos.Medida registrarMedida(MedidaSanitariaRequestDto dto) {
        controleDeAcesso.exigirAlguma(VigilanciaSanitariaService.INSPECIONAR);
        if (dto.getTipo() == TipoMedidaSanitaria.INTERDICAO) {
            throw new ResourceBadRequestException("A interdição definitiva vem da decisão do processo; aqui, a cautelar ou a apreensão.");
        }
        EstabelecimentoRegulado e = estabelecimentoOu404(dto.getEstabelecimentoId());
        if (!e.isAtivo()) {
            throw new ResourceUnprocessableEntityException("O estabelecimento está inativo.");
        }
        LocalDate inicio = dto.getInicio() != null ? dto.getInicio() : LocalDate.now();
        naoFutura(inicio, "O início da medida");
        boolean cautelar = dto.getTipo() == TipoMedidaSanitaria.INTERDICAO_CAUTELAR;
        MedidaSanitaria m = novaMedida(e, dto.getTipo(), !cautelar || dto.getTotal() == null || dto.getTotal(), dto.getDescricao().trim(), inicio);
        if (cautelar) {
            LocalDate fim = dto.getFimPrevisto() != null ? dto.getFimPrevisto() : inicio.plusDays(CAUTELAR_MAXIMO_DIAS);
            if (!fim.isAfter(inicio) || fim.isAfter(inicio.plusDays(CAUTELAR_MAXIMO_DIAS))) {
                throw new ResourceBadRequestException("A interdição cautelar vai até 90 dias (art. 23, §4º, da Lei 6.437/1977).");
            }
            m.setFimPrevisto(fim);
        } else {
            if (limpo(dto.getProduto()) == null || limpo(dto.getQuantidade()) == null) {
                throw new ResourceBadRequestException("A apreensão pede o produto e a quantidade.");
            }
            m.setTotal(false);
            m.setProduto(dto.getProduto().trim());
            m.setLote(limpo(dto.getLote()));
            m.setQuantidade(dto.getQuantidade().trim());
            m.setLocalGuarda(limpo(dto.getLocalGuarda()));
        }
        if (dto.getInspecaoId() != null) {
            InspecaoSanitaria i = inspecaoRepository.findById(dto.getInspecaoId()).orElseThrow(() -> new ResourceNotFoundException(
                    "Não foi possível encontrar a inspeção " + dto.getInspecaoId() + " em nossos registros."));
            if (!i.getEstabelecimento().getUuid().equals(e.getUuid())) {
                throw new ResourceUnprocessableEntityException("A inspeção é de outro estabelecimento.");
            }
            m.setInspecao(i);
        }
        medidaRepository.save(m);
        emitirMedida(m, cautelar ? TipoTermoSanitario.INTERDICAO : TipoTermoSanitario.APREENSAO);
        ContextoAuditoria.registro(m.getUuid());
        ContextoAuditoria.detalhe((cautelar ? "Interdição cautelar " : "Apreensão ") + m.getNumero() + ".");
        return dto(m);
    }

    /** Encerra a medida vigente (422): a desinterdição, com o termo, ou a destinação do apreendido. Data não futura nem antes do início (400). */
    @Transactional
    public ProcessoSanitarioDtos.Medida encerrarMedida(UUID id, EncerramentoMedidaRequestDto dto) {
        controleDeAcesso.exigirAlguma(VigilanciaSanitariaService.GERENCIAR);
        MedidaSanitaria m = medidaRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar a medida " + id + " em nossos registros."));
        if (m.getSituacao() != SituacaoMedidaSanitaria.VIGENTE) {
            throw new ResourceUnprocessableEntityException("A medida já foi encerrada.");
        }
        LocalDate data = dto.getData() != null ? dto.getData() : LocalDate.now();
        naoFutura(data, "O encerramento");
        naoAntes(data, m.getInicio(), "O encerramento não pode ser antes do início da medida.");
        m.setSituacao(SituacaoMedidaSanitaria.ENCERRADA);
        m.setEncerradaEm(data);
        m.setMotivoEncerramento(dto.getMotivo().trim());
        m.setEncerradaPorCpf(UsuarioAutenticado.cpf());
        medidaRepository.save(m);
        if (m.getTipo() != TipoMedidaSanitaria.APREENSAO) {
            emitirMedida(m, TipoTermoSanitario.DESINTERDICAO);
        }
        ContextoAuditoria.registro(m.getUuid());
        ContextoAuditoria.detalhe(m.getTipo() == TipoMedidaSanitaria.APREENSAO ? "Destinação do apreendido." : "Desinterdição.");
        return dto(m);
    }

    /** As interdições vigentes, para a Sala de Situação. */
    @Transactional(readOnly = true)
    public List<ProcessoSanitarioDtos.Interdicao> interdicoes() {
        controleDeAcesso.exigirAlguma(VigilanciaSanitariaService.INSPECIONAR, VigilanciaSanitariaService.GERENCIAR, JULGAR, "INDICADOR.CONSULTAR");
        return medidaRepository.findTodas().stream().filter(m -> m.getSituacao() == SituacaoMedidaSanitaria.VIGENTE && m.getTipo() != TipoMedidaSanitaria.APREENSAO)
                .map(m -> new ProcessoSanitarioDtos.Interdicao(m.getUuid(), m.getNumero(), VigilanciaSanitariaService.nome(m.getEstabelecimento()), m.getTipo(),
                        m.isTotal(), m.getInicio(), m.getFimPrevisto())).toList();
    }

    // ── Verificação pública ────────────────────────────────────────────────────────────────────────

    /** O termo pelo código impresso, sem login: tipo, número, data, estabelecimento e se o arquivo confere com o hash. Código desconhecido, 404. */
    @Transactional(readOnly = true)
    public ProcessoSanitarioDtos.Verificacao verificar(String codigo) {
        String c = codigo == null ? "" : codigo.trim().toUpperCase(Locale.ROOT);
        TermoSanitario t = termoRepository.findByCodigo(c).orElseThrow(() -> new ResourceNotFoundException("Nenhum termo foi emitido com o código " + c + "."));
        EstabelecimentoRegulado e = t.getEstabelecimento();
        return new ProcessoSanitarioDtos.Verificacao(t.getCodigo(), t.getTipo(), t.getNumero(), t.getEmitidoEm(), e.getRazaoSocial(), mascarar(e.getDocumento()),
                documentoService.integro(t.getDocumento().getUuid(), t.getHashPdf()), t.getHashPdf());
    }

    /** A licença pelo número, sem login, como o alvará afixado no estabelecimento. Número desconhecido, 404. */
    @Transactional(readOnly = true)
    public ProcessoSanitarioDtos.VerificacaoLicenca verificarLicenca(String numero) {
        String n = numero == null ? "" : numero.trim();
        LicencaSanitaria l = licencaRepository.findByNumero(n).orElseThrow(() -> new ResourceNotFoundException("Nenhuma licença sanitária tem o número " + n + "."));
        EstabelecimentoRegulado e = l.getEstabelecimento();
        return new ProcessoSanitarioDtos.VerificacaoLicenca(l.getNumero(), e.getRazaoSocial(), mascarar(e.getDocumento()), l.isDispensa(), l.getEmitidaEm(),
                l.getValidade(), VigilanciaSanitariaService.situacao(l, LocalDate.now()));
    }

    /** O CNPJ é público; o CPF do estabelecimento pessoa física sai mascarado. */
    static String mascarar(String documento) {
        if (documento.length() == 14) {
            return documento.substring(0, 2) + "." + documento.substring(2, 5) + "." + documento.substring(5, 8) + "/" + documento.substring(8, 12) + "-"
                    + documento.substring(12);
        }
        return "***." + documento.substring(3, 6) + "." + documento.substring(6, 9) + "-**";
    }

    // ── Pendências ─────────────────────────────────────────────────────────────────────────────────

    List<ProcessoSanitarioDtos.PendenciaProcesso> pendenciasProcessos() {
        LocalDate hoje = LocalDate.now();
        List<ProcessoSanitarioDtos.PendenciaProcesso> lista = new ArrayList<>();
        for (ProcessoSanitario p : processoRepository.findTodos()) {
            String nome = VigilanciaSanitariaService.nome(p.getEstabelecimento());
            String motivo = null;
            LocalDate desde = null;
            switch (p.getFase()) {
                case AGUARDANDO_CIENCIA -> {
                    motivo = "CIENCIA_DO_AUTO";
                    desde = p.getDataLavratura();
                }
                case EM_DEFESA -> {
                    if (prazoDefesa(p).isBefore(hoje)) {
                        motivo = "REVELIA";
                        desde = prazoDefesa(p);
                    }
                }
                case AGUARDANDO_DECISAO -> {
                    if (p.getDefesaEm().isBefore(hoje.minusDays(DIAS_DECISAO_PARADA))) {
                        motivo = "DECISAO_PARADA";
                        desde = p.getDefesaEm();
                    }
                }
                case DECIDIDO -> {
                    if (p.getCienciaDecisaoEm() == null) {
                        motivo = "CIENCIA_DA_DECISAO";
                        desde = p.getDataDecisao();
                    } else if (prazoRecurso(p).isBefore(hoje)) {
                        motivo = "A_ENCERRAR";
                        desde = prazoRecurso(p);
                    }
                }
                default -> {
                }
            }
            if (motivo != null) {
                lista.add(new ProcessoSanitarioDtos.PendenciaProcesso(p.getUuid(), p.getNumero(), nome, motivo, desde));
            }
        }
        return lista;
    }

    List<ProcessoSanitarioDtos.PendenciaMedida> pendenciasMedidas() {
        LocalDate hoje = LocalDate.now();
        return medidaRepository.findTodas().stream()
                .filter(m -> m.getSituacao() == SituacaoMedidaSanitaria.VIGENTE && m.getTipo() == TipoMedidaSanitaria.INTERDICAO_CAUTELAR
                        && !m.getFimPrevisto().isAfter(hoje.plusDays(DIAS_CAUTELAR_VENCENDO)))
                .map(m -> new ProcessoSanitarioDtos.PendenciaMedida(m.getUuid(), m.getNumero(), VigilanciaSanitariaService.nome(m.getEstabelecimento()),
                        m.getFimPrevisto().isBefore(hoje) ? "CAUTELAR_VENCIDA" : "CAUTELAR_VENCENDO", m.getFimPrevisto())).toList();
    }

    // ── Relatório no #19 ───────────────────────────────────────────────────────────────────────────

    /**
     * O relatório da vigilância sanitária (ADR-0200), no catálogo da BI4: o período de até um ano (o mês corrente por padrão) e a
     * permissão dos indicadores. A VISA é municipal: os números são do município, sem recorte por unidade.
     */
    @Transactional(readOnly = true)
    public RelatorioDto relatorio(TipoRelatorio tipo, LocalDate de, LocalDate ate, UUID unidadeId) {
        escopo.de(unidadeId);
        LocalDate fim = ate == null ? LocalDate.now() : ate;
        LocalDate inicio = de == null ? fim.withDayOfMonth(1) : de;
        if (inicio.isAfter(fim)) {
            throw new ResourceBadRequestException("O início do período não pode ser depois do fim.");
        }
        if (inicio.plusYears(1).isBefore(fim)) {
            throw new ResourceBadRequestException("O período vai até um ano.");
        }
        LocalDate hoje = LocalDate.now();
        ZoneId zona = ZoneId.of("America/Sao_Paulo");
        List<InspecaoSanitaria> inspecoes = inspecaoRepository.findTodas().stream().filter(i -> doPeriodo(i.getDataInspecao(), inicio, fim)).toList();
        Map<List<String>, Long> porMotivo = new TreeMap<>(Comparator.comparing((List<String> l) -> l.get(0)).thenComparing(l -> l.get(1)));
        inspecoes.forEach(i -> porMotivo.merge(List.of(VigilanciaSanitariaService.rotulo(i.getMotivo().name()),
                VigilanciaSanitariaService.rotulo(i.getResultado().name())), 1L, Long::sum));
        Map<UUID, LicencaSanitaria> atuais = new LinkedHashMap<>();
        licencaRepository.findTodas().forEach(l -> atuais.putIfAbsent(l.getEstabelecimento().getUuid(), l));
        Map<String, Long> situacoes = contar(atuais.values().stream().map(l -> VigilanciaSanitariaService.situacao(l, hoje)).toList());
        List<DenunciaSanitaria> denuncias = denunciaRepository.findTodas().stream()
                .filter(d -> doPeriodo(LocalDate.ofInstant(d.getRecebidaEm(), zona), inicio, fim)).toList();
        Map<String, Long> porSituacaoDenuncia = contar(denuncias.stream().map(d -> d.getSituacao().name()).toList());
        List<ProcessoSanitario> processos = processoRepository.findTodos();
        List<ProcessoSanitario> autos = processos.stream().filter(p -> doPeriodo(p.getDataLavratura(), inicio, fim)).toList();
        Map<String, Long> porGravidade = contar(autos.stream().map(p -> p.getGravidade().name()).toList());
        Map<String, Long> porFase = contar(processos.stream().filter(p -> p.getFase() != FaseProcessoSanitario.ENCERRADO).map(p -> p.getFase().name()).toList());
        long prazosVencidos = pendenciasProcessos().stream().filter(x -> !x.motivo().startsWith("CIENCIA")).count();
        List<PenalidadeSanitaria> penalidades = penalidadeRepository.findTodas().stream()
                .filter(x -> vale(x) && doPeriodo(x.getProcesso().getEncerradoEm(), inicio, fim)).toList();
        Map<String, List<PenalidadeSanitaria>> porTipo = new TreeMap<>(penalidades.stream().collect(Collectors.groupingBy(x -> VigilanciaSanitariaService.rotulo(
                x.getTipo().name()))));
        NumberFormat real = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("pt-BR"));
        List<MedidaSanitaria> interdicoes = medidaRepository.findTodas().stream()
                .filter(m -> m.getSituacao() == SituacaoMedidaSanitaria.VIGENTE && m.getTipo() != TipoMedidaSanitaria.APREENSAO).toList();

        List<RelatorioDto.Secao> secoes = new ArrayList<>();
        secoes.add(new RelatorioDto.Secao("Inspeções por motivo e resultado", List.of("Motivo", "Resultado", "Inspeções"),
                porMotivo.entrySet().stream().map(x -> List.of(x.getKey().get(0), x.getKey().get(1), String.valueOf(x.getValue()))).toList()));
        secoes.add(tabela("Licenças (a situação atual de cada estabelecimento)", "Situação", "Estabelecimentos", situacoes));
        secoes.add(tabela("Denúncias recebidas no período", "Situação", "Denúncias", porSituacaoDenuncia));
        secoes.add(tabela("Autos de infração por gravidade", "Gravidade", "Autos", porGravidade));
        secoes.add(tabela("Processos em andamento por fase", "Fase", "Processos", porFase));
        secoes.add(new RelatorioDto.Secao("Penalidades que passaram a valer no período", List.of("Penalidade", "Quantidade", "Multas"),
                porTipo.entrySet().stream().map(x -> List.of(x.getKey(), String.valueOf(x.getValue().size()), real.format(soma(x.getValue())))).toList()));
        secoes.add(new RelatorioDto.Secao("Interdições vigentes", List.of("Medida", "Estabelecimento", "Tipo", "Desde", "Até"),
                interdicoes.stream().map(m -> List.of(m.getNumero(), VigilanciaSanitariaService.nome(m.getEstabelecimento()),
                        VigilanciaSanitariaService.rotulo(m.getTipo().name()) + (m.isTotal() ? " total" : " parcial"), DATA.format(m.getInicio()),
                        m.getFimPrevisto() != null ? DATA.format(m.getFimPrevisto()) : "")).toList()));
        List<RelatorioDto.Indicador> resumo = List.of(
                new RelatorioDto.Indicador("Inspeções", String.valueOf(inspecoes.size())),
                new RelatorioDto.Indicador("Denúncias recebidas", String.valueOf(denuncias.size())),
                new RelatorioDto.Indicador("Autos de infração", String.valueOf(autos.size())),
                new RelatorioDto.Indicador("Processos com prazo vencido", String.valueOf(prazosVencidos)),
                new RelatorioDto.Indicador("Multas aplicadas", real.format(soma(penalidades))),
                new RelatorioDto.Indicador("Interdições vigentes", String.valueOf(interdicoes.size())));
        return new RelatorioDto(tipo.name(), tipo.getNome(), tipo.getCategoria(), inicio, fim, "Município", resumo, secoes,
                List.of("A VISA é municipal: os números não se recortam por unidade. Licenças e interdições são as de hoje; as penalidades contam quando o "
                        + "processo se encerra (o recurso tem efeito suspensivo). A multa é registrada aqui; a cobrança é da Fazenda."));
    }

    private static Map<String, Long> contar(List<String> codigos) {
        Map<String, Long> m = new TreeMap<>();
        codigos.forEach(c -> m.merge(VigilanciaSanitariaService.rotulo(c), 1L, Long::sum));
        return m;
    }

    private static RelatorioDto.Secao tabela(String titulo, String coluna, String quantidade, Map<String, Long> valores) {
        return new RelatorioDto.Secao(titulo, List.of(coluna, quantidade), valores.entrySet().stream().map(x -> List.of(x.getKey(), String.valueOf(x.getValue()))).toList());
    }

    private static BigDecimal soma(List<PenalidadeSanitaria> lista) {
        return lista.stream().map(PenalidadeSanitaria::getValorMulta).filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // ── Termos ─────────────────────────────────────────────────────────────────────────────────────

    private void emitirAuto(ProcessoSanitario p, List<IrregularidadeSanitaria> irregularidades) {
        EstabelecimentoRegulado e = p.getEstabelecimento();
        List<TermoSanitarioPdf.Bloco> blocos = new ArrayList<>();
        blocos.add(blocoEstabelecimento(e));
        blocos.add(new TermoSanitarioPdf.Bloco("Infração", List.of(
                par("Lavrado em", DATA.format(p.getDataLavratura())),
                par("Dispositivo legal infringido", p.getDispositivoLegal()),
                par("Gravidade", VigilanciaSanitariaService.rotulo(p.getGravidade().name())),
                par("Fiscais", fiscais(p))), List.of(p.getDescricao())));
        blocos.add(new TermoSanitarioPdf.Bloco("Irregularidades autuadas", List.of(), irregularidades.stream()
                .map(r -> "• " + r.getDescricao() + " (" + VigilanciaSanitariaService.rotulo(r.getGravidade().name()) + ")"
                        + (r.getEnquadramento() != null ? ". " + r.getEnquadramento() + "." : ".")).toList()));
        blocos.add(new TermoSanitarioPdf.Bloco("Defesa", List.of(), List.of("O autuado tem 15 (quinze) dias, contados da ciência deste auto, para apresentar "
                + "defesa ou impugnação à autoridade sanitária (art. 22 da Lei 6.437/1977).")));
        emitir(TipoTermoSanitario.AUTO_INFRACAO, p.getNumero(), e, p, null, "Auto de infração sanitária nº " + p.getNumero(), blocos);
    }

    private void emitirDecisao(ProcessoSanitario p, TipoTermoSanitario tipo) {
        List<TermoSanitarioPdf.Bloco> blocos = new ArrayList<>();
        blocos.add(blocoEstabelecimento(p.getEstabelecimento()));
        List<String[]> pares = new ArrayList<>(List.of(par("Processo", p.getNumero()), par("Auto lavrado em", DATA.format(p.getDataLavratura())),
                par("Defesa", p.getDefesaEm() == null ? "não apresentada (revelia)" : DATA.format(p.getDefesaEm())
                        + (Boolean.TRUE.equals(p.getDefesaIntempestiva()) ? " (intempestiva)" : ""))));
        boolean recurso = tipo == TipoTermoSanitario.DECISAO_RECURSO;
        if (recurso) {
            pares.add(par("Recurso", DATA.format(p.getRecursoEm())));
            pares.add(par("Julgamento", DATA.format(p.getDataJulgamentoRecurso()) + " · " + VigilanciaSanitariaService.rotulo(p.getRecursoResultado().name())));
        } else {
            pares.add(par("Decisão", DATA.format(p.getDataDecisao()) + " · " + VigilanciaSanitariaService.rotulo(p.getDecisaoResultado().name())));
            pares.add(par("Atenuantes (art. 7º)", rotulos(p.getAtenuantes())));
            pares.add(par("Agravantes (art. 8º)", rotulos(p.getAgravantes())));
        }
        blocos.add(new TermoSanitarioPdf.Bloco(recurso ? "Julgamento do recurso" : "Decisão", pares,
                List.of(recurso ? p.getRecursoFundamentacao() : p.getDecisaoFundamentacao())));
        List<PenalidadeSanitaria> penalidades = penalidadeRepository.findByProcesso_Uuid(p.getUuid());
        if (!penalidades.isEmpty()) {
            blocos.add(new TermoSanitarioPdf.Bloco("Penalidades", List.of(), penalidades.stream().map(x -> "• " + penalidade(x)
                    + (x.isRevogada() ? " (revogada no recurso)" : "")).toList()));
        }
        if (!recurso && p.getDecisaoResultado() == ResultadoDecisaoSanitaria.PENALIDADE) {
            blocos.add(new TermoSanitarioPdf.Bloco("Recurso", List.of(), List.of("Cabe recurso em 15 (quinze) dias, contados da ciência desta decisão, com "
                    + "efeito suspensivo (art. 30 da Lei 6.437/1977). Sem recurso, as penalidades valem depois do prazo.")));
        }
        emitir(tipo, p.getNumero(), p.getEstabelecimento(), p, null, (recurso ? "Julgamento do recurso" : "Decisão") + " do processo sanitário nº "
                + p.getNumero(), blocos);
    }

    private void emitirMedida(MedidaSanitaria m, TipoTermoSanitario tipo) {
        List<TermoSanitarioPdf.Bloco> blocos = new ArrayList<>();
        blocos.add(blocoEstabelecimento(m.getEstabelecimento()));
        List<String[]> pares = new ArrayList<>(List.of(par("Medida", m.getNumero()), par("Tipo", VigilanciaSanitariaService.rotulo(m.getTipo().name())
                + (m.getTipo() == TipoMedidaSanitaria.APREENSAO ? "" : m.isTotal() ? " total" : " parcial")), par("Início", DATA.format(m.getInicio()))));
        if (m.getFimPrevisto() != null) {
            pares.add(par("Até", DATA.format(m.getFimPrevisto())));
        }
        if (m.getProduto() != null) {
            pares.add(par("Produto", m.getProduto() + (m.getLote() != null ? ", lote " + m.getLote() : "")));
            pares.add(par("Quantidade", m.getQuantidade()));
            pares.add(par("Local de guarda", m.getLocalGuarda()));
        }
        if (m.getProcesso() != null) {
            pares.add(par("Processo", m.getProcesso().getNumero()));
        }
        if (tipo == TipoTermoSanitario.DESINTERDICAO) {
            pares.add(par("Desinterditado em", DATA.format(m.getEncerradaEm())));
        }
        blocos.add(new TermoSanitarioPdf.Bloco(tipo == TipoTermoSanitario.DESINTERDICAO ? "Desinterdição" : "Medida", pares,
                tipo == TipoTermoSanitario.DESINTERDICAO ? List.of(m.getMotivoEncerramento()) : List.of(m.getDescricao())));
        String titulo = switch (tipo) {
            case APREENSAO -> "Termo de apreensão nº ";
            case DESINTERDICAO -> "Termo de desinterdição nº ";
            default -> "Termo de interdição nº ";
        };
        emitir(tipo, m.getNumero(), m.getEstabelecimento(), m.getProcesso(), m, titulo + m.getNumero(), blocos);
    }

    private void emitir(TipoTermoSanitario tipo, String numero, EstabelecimentoRegulado e, ProcessoSanitario p, MedidaSanitaria m, String titulo,
                        List<TermoSanitarioPdf.Bloco> blocos) {
        String codigo = novoCodigo();
        String endereco = (frontendUrl == null || frontendUrl.isBlank() ? "" : frontendUrl.replaceAll("/+$", "")) + "/verificar-visa?codigo=" + codigo;
        String cpf = UsuarioAutenticado.cpf();
        String assinante = cpf == null ? null : profissionalRepository.findByCpfAndAtivoTrue(cpf).map(Profissional::getNome).orElse(null);
        Instant agora = Instant.now();
        String assinatura = assinante != null
                ? "Emitido e assinado eletronicamente por " + assinante + " em " + DATA_HORA.format(agora) + " (assinatura eletrônica avançada, Lei 14.063/2020)."
                : "Emitido em " + DATA_HORA.format(agora) + (cpf == null ? " sem login." : ".");
        byte[] pdf = TermoSanitarioPdf.gerar(titulo, "Vigilância Sanitária municipal · Lei 6.437/1977", blocos, assinatura, codigo, endereco);
        DonoDocumento dono = p != null ? DonoDocumento.PROCESSO_SANITARIO : DonoDocumento.ESTABELECIMENTO;
        VersaoDocumento v = documentoService.guardarGerado(dono, p != null ? p.getUuid() : e.getUuid(), "TERMO_SANITARIO", titulo, pdf,
                tipo.name().toLowerCase(Locale.ROOT).replace('_', '-') + "-" + numero.replace('/', '-') + ".pdf");
        termoRepository.save(new TermoSanitario(null, codigo, tipo, numero, e, p, m, v.getDocumento(), v.getSha256(), agora, cpf));
    }

    private static TermoSanitarioPdf.Bloco blocoEstabelecimento(EstabelecimentoRegulado e) {
        return new TermoSanitarioPdf.Bloco("Estabelecimento", List.of(
                par("Razão social", e.getRazaoSocial()),
                par("Nome fantasia", e.getNomeFantasia()),
                par(e.getDocumento().length() == 14 ? "CNPJ" : "CPF", formatado(e.getDocumento())),
                par("Endereço", e.getEndereco()),
                par("Responsável legal", e.getResponsavelLegal())), List.of());
    }

    private static String formatado(String d) {
        return d.length() == 14 ? mascarar(d) : d.substring(0, 3) + "." + d.substring(3, 6) + "." + d.substring(6, 9) + "-" + d.substring(9);
    }

    private String novoCodigo() {
        for (int tentativa = 0; tentativa < 20; tentativa++) {
            StringBuilder sb = new StringBuilder();
            for (int k = 0; k < 12; k++) {
                if (k > 0 && k % 4 == 0) {
                    sb.append('-');
                }
                sb.append(LETRAS.charAt(SORTEIO.nextInt(LETRAS.length())));
            }
            if (!termoRepository.existsByCodigo(sb.toString())) {
                return sb.toString();
            }
        }
        throw new IllegalStateException("Não foi possível gerar um código de verificação livre.");
    }

    // ── Apoio ──────────────────────────────────────────────────────────────────────────────────────

    static LocalDate prazoDefesa(ProcessoSanitario p) {
        return p.getCienciaAutoEm() == null ? null : p.getCienciaAutoEm().plusDays(PRAZO_DIAS);
    }

    static LocalDate prazoRecurso(ProcessoSanitario p) {
        return p.getCienciaDecisaoEm() == null ? null : p.getCienciaDecisaoEm().plusDays(PRAZO_DIAS);
    }

    /** A licença que vale na data (não dispensa, vigente pela situação calculada). */
    private LicencaSanitaria licencaVigente(EstabelecimentoRegulado e, LocalDate data) {
        List<LicencaSanitaria> h = licencaRepository.findByEstabelecimento_UuidOrderByEmitidaEmDescRegistradoEmDesc(e.getUuid());
        if (h.isEmpty() || h.get(0).isDispensa() || !"VIGENTE".equals(VigilanciaSanitariaService.situacao(h.get(0), data))) {
            return null;
        }
        return h.get(0);
    }

    private Set<Profissional> fiscais(List<String> matriculas, String mensagem403) {
        Set<Profissional> fiscais = new LinkedHashSet<>();
        for (String m : matriculas) {
            String mat = m.trim();
            fiscais.add(profissionalRepository.findByMatricula(mat).orElseThrow(() -> new ResourceNotFoundException(
                    "Não foi possível encontrar o profissional de matrícula " + mat + " em nossos registros.")));
        }
        String cpf = UsuarioAutenticado.cpf();
        if (cpf != null && fiscais.stream().noneMatch(p -> cpf.equals(p.getCpf()))) {
            throw new ResourceForbiddenException(mensagem403);
        }
        return fiscais;
    }

    private MedidaSanitaria novaMedida(EstabelecimentoRegulado e, TipoMedidaSanitaria tipo, boolean total, String descricao, LocalDate inicio) {
        MedidaSanitaria m = new MedidaSanitaria();
        m.setNumero(numero(inicio.getYear(), medidaRepository::countByNumeroStartingWith));
        m.setEstabelecimento(e);
        m.setTipo(tipo);
        m.setTotal(total);
        m.setDescricao(descricao);
        m.setInicio(inicio);
        m.setSituacao(SituacaoMedidaSanitaria.VIGENTE);
        m.setRegistradoEm(Instant.now());
        m.setRegistradoPorCpf(UsuarioAutenticado.cpf());
        return m;
    }

    private ProcessoSanitarioDtos.Processo salvar(ProcessoSanitario p, String detalhe) {
        processoRepository.save(p);
        ContextoAuditoria.registro(p.getUuid());
        ContextoAuditoria.detalhe(detalhe);
        return dto(p);
    }

    private static String numero(int ano, Function<String, Long> contar) {
        String prefixo = ano + "/";
        return prefixo + String.format(Locale.ROOT, "%06d", contar.apply(prefixo) + 1);
    }

    private static void naoFutura(LocalDate data, String oQue) {
        if (data.isAfter(LocalDate.now())) {
            throw new ResourceBadRequestException(oQue + " não pode ser de data futura.");
        }
    }

    private static void naoAntes(LocalDate data, LocalDate limite, String mensagem) {
        if (limite != null && data.isBefore(limite)) {
            throw new ResourceBadRequestException(mensagem);
        }
    }

    private static String juntar(List<? extends Enum<?>> valores) {
        return valores == null || valores.isEmpty() ? null : valores.stream().distinct().map(Enum::name).collect(Collectors.joining(";"));
    }

    static <E extends Enum<E>> List<E> separar(String texto, Class<E> tipo) {
        return texto == null || texto.isBlank() ? List.of() : Arrays.stream(texto.split(";")).map(s -> Enum.valueOf(tipo, s)).toList();
    }

    private static String rotulos(String texto) {
        return texto == null || texto.isBlank() ? "nenhuma" : Arrays.stream(texto.split(";")).map(VigilanciaSanitariaService::rotulo).collect(Collectors.joining("; "));
    }

    static String penalidade(PenalidadeSanitaria x) {
        String s = VigilanciaSanitariaService.rotulo(x.getTipo().name());
        if (x.getValorMulta() != null) {
            s += " de " + NumberFormat.getCurrencyInstance(Locale.forLanguageTag("pt-BR")).format(x.getValorMulta());
        }
        if (x.getSuspensaoAte() != null) {
            s += " até " + DATA.format(x.getSuspensaoAte());
        }
        return x.getDescricao() != null ? s + ": " + x.getDescricao() : s;
    }

    private static String fiscais(ProcessoSanitario p) {
        return p.getFiscais().stream().map(f -> f.getNome() + " (matrícula " + f.getMatricula() + ")").collect(Collectors.joining("; "));
    }

    private static String[] par(String rotulo, String valor) {
        return new String[]{rotulo, valor};
    }

    private static String limpo(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }

    private EstabelecimentoRegulado estabelecimentoOu404(UUID id) {
        return estabelecimentoRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar o estabelecimento " + id + " em nossos registros."));
    }

    private ProcessoSanitario processoOu404(UUID id) {
        return processoRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar o processo sanitário " + id + " em nossos registros."));
    }

    private List<ProcessoSanitarioDtos.Termo> termos(List<TermoSanitario> lista) {
        return lista.stream().map(t -> {
            UUID versao = documentoService.doDono(t.getDocumento().getDonoTipo(), t.getDocumento().getDonoId()).stream()
                    .filter(d -> d.getUuid().equals(t.getDocumento().getUuid())).findFirst()
                    .map(d -> d.getVersoes().isEmpty() ? null : d.getVersoes().get(0).getUuid()).orElse(null);
            return new ProcessoSanitarioDtos.Termo(t.getUuid(), t.getCodigo(), t.getTipo(), t.getNumero(), t.getEmitidoEm(), versao);
        }).toList();
    }

    private ProcessoSanitarioDtos.Processo dto(ProcessoSanitario p) {
        LocalDate hoje = LocalDate.now();
        EstabelecimentoRegulado e = p.getEstabelecimento();
        boolean revelia = p.getFase() == FaseProcessoSanitario.EM_DEFESA && prazoDefesa(p).isBefore(hoje)
                || p.getDefesaEm() == null && p.getDataDecisao() != null;
        return new ProcessoSanitarioDtos.Processo(p.getUuid(), p.getNumero(), e.getUuid(), VigilanciaSanitariaService.nome(e), e.getDocumento(),
                p.getInspecao() != null ? p.getInspecao().getUuid() : null, p.getDispositivoLegal(), p.getDescricao(), p.getGravidade(), p.getFase(),
                p.getDataLavratura(), p.getFiscais().stream().map(f -> new VigilanciaSanitariaDtos.Fiscal(f.getMatricula(), f.getNome())).toList(),
                irregularidadeRepository.findByProcesso_UuidOrderByRegistradoEmAsc(p.getUuid()).stream().map(r -> VigilanciaSanitariaService.dto(r, hoje)).toList(),
                p.getCienciaAutoEm(), p.getFormaCienciaAuto(), prazoDefesa(p), p.getDefesaEm(), p.getDefesaTexto(), p.getDefesaIntempestiva(), revelia,
                p.getDecisaoResultado(), p.getDataDecisao(), p.getDecisaoFundamentacao(), separar(p.getAtenuantes(), AtenuanteSanitaria.class),
                separar(p.getAgravantes(), AgravanteSanitaria.class), p.getCienciaDecisaoEm(), p.getFormaCienciaDecisao(), prazoRecurso(p), p.getRecursoEm(),
                p.getRecursoTexto(), p.getRecursoResultado(), p.getDataJulgamentoRecurso(), p.getRecursoFundamentacao(), p.getEncerradoEm(),
                penalidadeRepository.findByProcesso_Uuid(p.getUuid()).stream().map(x -> new ProcessoSanitarioDtos.Penalidade(x.getUuid(), x.getTipo(),
                        x.getValorMulta(), x.getSuspensaoAte(), x.getDescricao(), x.isRevogada())).toList(),
                termos(termoRepository.findDoProcesso(p.getUuid()).stream().filter(t -> t.getMedida() == null).toList()),
                medidaRepository.findByProcesso_UuidOrderByInicioAsc(p.getUuid()).stream().map(this::dto).toList());
    }

    private ProcessoSanitarioDtos.Medida dto(MedidaSanitaria m) {
        LocalDate hoje = LocalDate.now();
        ProcessoSanitario p = m.getProcesso();
        return new ProcessoSanitarioDtos.Medida(m.getUuid(), m.getNumero(), m.getEstabelecimento().getUuid(), VigilanciaSanitariaService.nome(m.getEstabelecimento()),
                p != null ? p.getUuid() : null, p != null ? p.getNumero() : null, m.getTipo(), m.isTotal(), m.getDescricao(), m.getProduto(), m.getLote(),
                m.getQuantidade(), m.getLocalGuarda(), m.getInicio(), m.getFimPrevisto(), m.getSituacao(), m.getEncerradaEm(), m.getMotivoEncerramento(),
                m.getSituacao() == SituacaoMedidaSanitaria.VIGENTE && m.getFimPrevisto() != null && m.getFimPrevisto().isBefore(hoje),
                termos(termoRepository.findDaMedida(m.getUuid())));
    }

    /** Usado pelo relatório: as penalidades mantidas, para somar as multas. */
    static boolean vale(PenalidadeSanitaria x) {
        return !x.isRevogada() && x.getProcesso().getFase() == FaseProcessoSanitario.ENCERRADO;
    }

    static boolean doPeriodo(LocalDate d, LocalDate de, LocalDate ate) {
        return d != null && !d.isBefore(de) && !d.isAfter(ate);
    }
}
