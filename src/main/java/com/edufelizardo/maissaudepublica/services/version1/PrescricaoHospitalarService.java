package com.edufelizardo.maissaudepublica.services.version1;

import com.edufelizardo.maissaudepublica.config.ContextoAuditoria;
import com.edufelizardo.maissaudepublica.config.UsuarioAutenticado;
import com.edufelizardo.maissaudepublica.exceptions.ResourceBadRequestException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceConflictException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceNotFoundException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceUnprocessableEntityException;
import com.edufelizardo.maissaudepublica.models.ChecagemPrescricao;
import com.edufelizardo.maissaudepublica.models.HorarioPrescricao;
import com.edufelizardo.maissaudepublica.models.Internacao;
import com.edufelizardo.maissaudepublica.models.ItemPrescricao;
import com.edufelizardo.maissaudepublica.models.Lote;
import com.edufelizardo.maissaudepublica.models.Medicamento;
import com.edufelizardo.maissaudepublica.models.PrescricaoHospitalar;
import com.edufelizardo.maissaudepublica.models.Profissional;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.AcaoPrescricaoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.ItemPrescricaoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.PrescricaoHospitalarRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.PrescricaoDtos;
import com.edufelizardo.maissaudepublica.models.enuns.ResultadoChecagem;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoHorarioPrescricao;
import com.edufelizardo.maissaudepublica.models.enuns.StatusInternacao;
import com.edufelizardo.maissaudepublica.models.enuns.TipoDieta;
import com.edufelizardo.maissaudepublica.models.enuns.TipoItemPrescricao;
import com.edufelizardo.maissaudepublica.models.enuns.TipoMovimentacaoFarmacia;
import com.edufelizardo.maissaudepublica.repositories.ChecagemPrescricaoRepository;
import com.edufelizardo.maissaudepublica.repositories.HorarioPrescricaoRepository;
import com.edufelizardo.maissaudepublica.repositories.InternacaoRepository;
import com.edufelizardo.maissaudepublica.repositories.ItemPrescricaoRepository;
import com.edufelizardo.maissaudepublica.repositories.LoteRepository;
import com.edufelizardo.maissaudepublica.repositories.MedicamentoRepository;
import com.edufelizardo.maissaudepublica.repositories.PrescricaoHospitalarRepository;
import com.edufelizardo.maissaudepublica.repositories.ProfissionalRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Prescrição hospitalar diária, aprazamento e checagem (ADR-0187). O médico prescreve por 24 horas (a nova encerra a anterior no seu
 * início; renovar copia os itens); os horários saem do primeiro horário e do intervalo, ou das refeições na dieta oral; a enfermagem
 * checa (a nutrição, só a dieta), com a baixa do lote no livro da Farmácia quando informado (ADR-0064). A checagem não é editada:
 * corrigir é retificar (ADR-0062), com o estorno da baixa anterior.
 */
@Service
public class PrescricaoHospitalarService {

    public static final String PRESCREVER = "PRESCRICAO.HOSPITALAR";
    public static final String CHECAR = "PRESCRICAO.CHECAR";
    public static final String CHECAR_DIETA = "DIETA.CHECAR";
    static final Duration VIGENCIA = Duration.ofHours(24);
    static final Duration ATRASO = Duration.ofHours(1);
    /** Horários das refeições da dieta oral: desjejum, colação, almoço, lanche, jantar e ceia. */
    static final List<LocalTime> REFEICOES = List.of(LocalTime.of(7, 0), LocalTime.of(9, 30), LocalTime.of(12, 0), LocalTime.of(15, 0),
            LocalTime.of(18, 0), LocalTime.of(21, 0));
    private static final String[] VER = {"INTERNACAO.CONSULTAR", "INTERNACAO.GERENCIAR", "INTERNACAO.ALTA", PRESCREVER, CHECAR, CHECAR_DIETA};
    /** No posto, quem cuida do paciente vê todos os itens; quem só consulta a internação ou só checa dieta, não. */
    private static final String[] VER_TUDO = {"INTERNACAO.GERENCIAR", "INTERNACAO.ALTA", PRESCREVER, CHECAR};

    @Autowired
    private PrescricaoHospitalarRepository prescricaoRepository;

    @Autowired
    private ItemPrescricaoRepository itemRepository;

    @Autowired
    private HorarioPrescricaoRepository horarioRepository;

    @Autowired
    private ChecagemPrescricaoRepository checagemRepository;

    @Autowired
    private InternacaoRepository internacaoRepository;

    @Autowired
    private MedicamentoRepository medicamentoRepository;

    @Autowired
    private LoteRepository loteRepository;

    @Autowired
    private ProfissionalRepository profissionalRepository;

    @Autowired
    private MovimentacaoFarmaciaService movimentacaoFarmaciaService;

    @Autowired
    private ControleDeAcesso controleDeAcesso;

    // ── Prescrição ─────────────────────────────────────────────────────────────────────────────────────────────────────

    /** Prescreve por 24 horas. Sem itens, 400; internação com alta, 422; outra prescrição começando no mesmo início ou depois, 409. */
    @Transactional
    public PrescricaoDtos.Prescricao prescrever(PrescricaoHospitalarRequestDto dto) {
        if (dto.getInternacaoId() == null) {
            throw new ResourceBadRequestException("Informe a internação.");
        }
        if (dto.getItens() == null || dto.getItens().isEmpty()) {
            throw new ResourceBadRequestException("A prescrição precisa de ao menos um item.");
        }
        Internacao internacao = internacaoRepository.findById(dto.getInternacaoId()).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar uma internação com o id " + dto.getInternacaoId() + " em nossos registros."));
        Instant inicio = dto.getInicio() != null ? dto.getInicio().truncatedTo(ChronoUnit.MINUTES) : proximaHoraCheia();
        PrescricaoHospitalar p = nova(internacao, dto.getMedicoMatricula(), inicio, dto.getObservacao(), null);
        int ordem = 1;
        for (ItemPrescricaoRequestDto i : dto.getItens()) {
            criarItem(p, i, ordem++);
        }
        return detalhe(p);
    }

    /** Renova: a do dia seguinte, com os itens não suspensos da anterior (os horários deslocados), para o médico revisar. */
    @Transactional
    public PrescricaoDtos.Prescricao renovar(UUID prescricaoId, PrescricaoHospitalarRequestDto dto) {
        PrescricaoHospitalar anterior = prescricaoRepository.findById(prescricaoId).orElseThrow(() -> naoEncontrada(prescricaoId));
        Instant agora = Instant.now();
        Instant inicio = dto.getInicio() != null ? dto.getInicio().truncatedTo(ChronoUnit.MINUTES)
                : anterior.getFim().isAfter(agora.minus(Duration.ofHours(2))) ? anterior.getFim() : proximaHoraCheia();
        List<ItemPrescricao> itens = itemRepository.findByPrescricao_UuidOrderByOrdemAsc(anterior.getUuid()).stream()
                .filter(i -> i.getSuspensoEm() == null).toList();
        if (itens.isEmpty()) {
            throw new ResourceUnprocessableEntityException("A prescrição anterior não tem itens ativos para renovar.");
        }
        PrescricaoHospitalar p = nova(anterior.getInternacao(), dto.getMedicoMatricula(), inicio,
                dto.getObservacao() != null ? dto.getObservacao() : anterior.getObservacao(), anterior.getUuid());
        Duration deslocamento = Duration.between(anterior.getInicio(), inicio);
        int ordem = 1;
        for (ItemPrescricao a : itens) {
            ItemPrescricaoRequestDto c = new ItemPrescricaoRequestDto();
            c.setTipo(a.getTipo());
            c.setMedicamentoId(a.getMedicamento() != null ? a.getMedicamento().getUuid() : null);
            c.setDose(a.getDose());
            c.setVia(a.getVia());
            c.setTipoDieta(a.getTipoDieta());
            c.setDescricao(a.getDescricao());
            c.setIntervaloHoras(a.getIntervaloHoras());
            c.setPrimeiroHorario(a.getPrimeiroHorario() != null ? a.getPrimeiroHorario().plus(deslocamento) : null);
            c.setSeNecessario(a.isSeNecessario());
            c.setCondicao(a.getCondicao());
            criarItem(p, c, ordem++);
        }
        return detalhe(p);
    }

    /** Suspende o item, com motivo; os horários pendentes ficam cancelados. */
    @Transactional
    public PrescricaoDtos.Prescricao suspenderItem(UUID itemId, AcaoPrescricaoRequestDto dto) {
        ItemPrescricao item = itemRepository.findByIdParaAtualizar(itemId).orElseThrow(() -> itemNaoEncontrado(itemId));
        PrescricaoHospitalar p = item.getPrescricao();
        ContextoAuditoria.paciente(p.getInternacao().getPaciente().getUuid());
        controleDeAcesso.exigir(PRESCREVER, p.getInternacao().getUnidade());
        if (item.getSuspensoEm() != null) {
            throw new ResourceUnprocessableEntityException("O item já está suspenso.");
        }
        String motivo = exigirTexto(dto.getMotivo(), "Informe o motivo da suspensão.");
        item.setSuspensoEm(Instant.now());
        item.setMotivoSuspensao(motivo);
        item.setSuspensoPor(profissional(dto.getProfissionalMatricula()));
        itemRepository.save(item);
        cancelar(horarioRepository.findByItem_UuidAndSituacao(item.getUuid(), SituacaoHorarioPrescricao.PENDENTE), null);
        return detalhe(p);
    }

    @Transactional(readOnly = true)
    public List<PrescricaoDtos.Prescricao> listar(UUID internacaoId) {
        Internacao internacao = internacaoRepository.findById(internacaoId).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar uma internação com o id " + internacaoId + " em nossos registros."));
        controleDeAcesso.exigirVisivel(internacao.getUnidade(), VER);
        ContextoAuditoria.paciente(internacao.getPaciente().getUuid());
        ContextoAuditoria.registro(internacao.getUuid());
        List<PrescricaoHospitalar> prescricoes = prescricaoRepository.findByInternacao_UuidOrderByInicioDesc(internacaoId);
        return montar(prescricoes);
    }

    @Transactional(readOnly = true)
    public PrescricaoDtos.Prescricao buscarPorId(UUID uuid) {
        PrescricaoHospitalar p = prescricaoRepository.findById(uuid).orElseThrow(() -> naoEncontrada(uuid));
        controleDeAcesso.exigirVisivel(p.getInternacao().getUnidade(), VER);
        return detalhe(p);
    }

    /** Na alta (ADR-0098): a vigente se encerra e os horários pendentes ficam cancelados. */
    @Transactional
    public void encerrarNaAlta(Internacao internacao, Instant alta) {
        for (PrescricaoHospitalar p : prescricaoRepository.findByInternacao_UuidOrderByInicioDesc(internacao.getUuid())) {
            if (p.getEncerradaEm() == null && p.getFim().isAfter(alta)) {
                p.setEncerradaEm(alta.isBefore(p.getInicio()) ? p.getInicio() : alta);
                prescricaoRepository.save(p);
            }
            cancelar(horarioRepository.findByItem_Prescricao_UuidAndSituacao(p.getUuid(), SituacaoHorarioPrescricao.PENDENTE), null);
        }
    }

    // ── Posto de enfermagem e checagem ─────────────────────────────────────────────────────────────────────────────────

    /**
     * Os horários das internações em curso entre dois instantes (padrão: 6 horas antes e 6 depois; até 48 horas), por unidade e
     * setor. Quem só checa dieta vê só a dieta.
     */
    @Transactional(readOnly = true)
    public List<PrescricaoDtos.HorarioDoPosto> posto(UUID unidadeId, UUID setorId, Instant de, Instant ate) {
        controleDeAcesso.exigirAlguma(VER);
        Instant agora = Instant.now();
        Instant inicio = de != null ? de : agora.minus(Duration.ofHours(6));
        Instant fim = ate != null ? ate : agora.plus(Duration.ofHours(6));
        if (!fim.isAfter(inicio) || Duration.between(inicio, fim).compareTo(Duration.ofHours(48)) > 0) {
            throw new ResourceBadRequestException("O período vai de 1 minuto a 48 horas.");
        }
        Optional<Set<UUID>> tudo = controleDeAcesso.unidadesVisiveis(VER_TUDO);
        Optional<Set<UUID>> dieta = controleDeAcesso.unidadesVisiveis(CHECAR_DIETA);
        List<HorarioPrescricao> horarios = horarioRepository.findDoPosto(StatusInternacao.INTERNADO, inicio, fim).stream()
                .filter(h -> h.getSituacao() != SituacaoHorarioPrescricao.CANCELADO)
                .filter(h -> {
                    Internacao n = h.getItem().getPrescricao().getInternacao();
                    return (unidadeId == null || n.getUnidade().getUuid().equals(unidadeId))
                            && (setorId == null || n.getLeito().getSetor().getUuid().equals(setorId));
                })
                .filter(h -> {
                    UUID u = h.getItem().getPrescricao().getInternacao().getUnidade().getUuid();
                    boolean veTudo = tudo.isEmpty() || tudo.get().contains(u);
                    boolean veDieta = h.getItem().getTipo() == TipoItemPrescricao.DIETA && (dieta.isEmpty() || dieta.get().contains(u));
                    return veTudo || veDieta;
                }).toList();
        Map<UUID, ChecagemPrescricao> checagens = horarios.isEmpty() ? Map.of()
                : checagemRepository.findByHorario_UuidInAndRetificadaFalse(horarios.stream().map(HorarioPrescricao::getUuid).toList()).stream()
                .collect(Collectors.toMap(c -> c.getHorario().getUuid(), Function.identity(), (a, b) -> a));
        return horarios.stream().map(h -> {
            ItemPrescricao i = h.getItem();
            Internacao n = i.getPrescricao().getInternacao();
            ChecagemPrescricao c = checagens.get(h.getUuid());
            return new PrescricaoDtos.HorarioDoPosto(h.getUuid(), i.getUuid(), i.getPrescricao().getUuid(), n.getUuid(), n.getPaciente().getNome(),
                    n.getLeito().getIdentificacao(), n.getLeito().getSetor().getNome(), n.getUnidade().getUuid(), n.getUnidade().getNome(), i.getTipo(),
                    PrescricaoDtos.resumo(i), h.getPrevistoPara(), h.getSituacao(), atrasado(h, agora), c != null ? PrescricaoDtos.Checagem.from(c) : null);
        }).toList();
    }

    /** Reapraza o horário pendente para outro dentro da vigência, com motivo. */
    @Transactional
    public PrescricaoDtos.Prescricao reaprazar(UUID horarioId, AcaoPrescricaoRequestDto dto) {
        HorarioPrescricao h = horarioRepository.findByIdParaAtualizar(horarioId).orElseThrow(() -> horarioNaoEncontrado(horarioId));
        ItemPrescricao item = h.getItem();
        PrescricaoHospitalar p = item.getPrescricao();
        exigirChecagem(item);
        if (h.getSituacao() != SituacaoHorarioPrescricao.PENDENTE) {
            throw new ResourceUnprocessableEntityException("Só o horário pendente pode ser reaprazado; este está " + h.getSituacao() + ".");
        }
        if (dto.getPrevistoPara() == null) {
            throw new ResourceBadRequestException("Informe o novo horário.");
        }
        String motivo = exigirTexto(dto.getMotivo(), "Informe o motivo do reaprazamento.");
        Instant novo = dto.getPrevistoPara().truncatedTo(ChronoUnit.MINUTES);
        if (novo.isBefore(p.getInicio()) || !novo.isBefore(p.fimEfetivo())) {
            throw new ResourceUnprocessableEntityException("O novo horário precisa estar dentro da vigência da prescrição.");
        }
        if (h.getAprazadoOriginalmente() == null) {
            h.setAprazadoOriginalmente(h.getPrevistoPara());
        }
        h.setPrevistoPara(novo);
        h.setMotivoReaprazamento(motivo);
        horarioRepository.save(h);
        return detalhe(p);
    }

    /** Checa o horário aprazado: feito ou não feito. Horário já checado ou cancelado, 422. */
    @Transactional
    public PrescricaoDtos.Prescricao checar(UUID horarioId, AcaoPrescricaoRequestDto dto) {
        HorarioPrescricao h = horarioRepository.findByIdParaAtualizar(horarioId).orElseThrow(() -> horarioNaoEncontrado(horarioId));
        ItemPrescricao item = h.getItem();
        exigirChecagem(item);
        if (h.getSituacao() != SituacaoHorarioPrescricao.PENDENTE) {
            throw new ResourceUnprocessableEntityException("Este horário já foi checado ou cancelado (" + h.getSituacao() + ").");
        }
        ChecagemPrescricao c = registrarChecagem(item, h, dto, null);
        h.setSituacao(c.getResultado() == ResultadoChecagem.FEITO ? SituacaoHorarioPrescricao.FEITO : SituacaoHorarioPrescricao.NAO_FEITO);
        horarioRepository.save(h);
        return detalhe(item.getPrescricao());
    }

    /** Checa o item "se necessário", sem horário, com a prescrição vigente. */
    @Transactional
    public PrescricaoDtos.Prescricao checarSeNecessario(UUID itemId, AcaoPrescricaoRequestDto dto) {
        ItemPrescricao item = itemRepository.findByIdParaAtualizar(itemId).orElseThrow(() -> itemNaoEncontrado(itemId));
        exigirChecagem(item);
        if (!item.isSeNecessario()) {
            throw new ResourceUnprocessableEntityException("Este item tem horários aprazados: cheque pelo horário.");
        }
        if (!item.getPrescricao().vigenteEm(Instant.now())) {
            throw new ResourceUnprocessableEntityException("A prescrição não está vigente.");
        }
        registrarChecagem(item, null, dto, null);
        return detalhe(item.getPrescricao());
    }

    /** Retifica a checagem (com motivo): a nova a substitui, e a baixa do lote da anterior é estornada. */
    @Transactional
    public PrescricaoDtos.Prescricao retificar(UUID checagemId, AcaoPrescricaoRequestDto dto) {
        ChecagemPrescricao original = checagemRepository.findByIdParaAtualizar(checagemId).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar uma checagem com o id " + checagemId + " em nossos registros."));
        ItemPrescricao item = original.getItem();
        exigirChecagem(item);
        if (original.isRetificada()) {
            throw new ResourceUnprocessableEntityException("Esta checagem já foi retificada; retifique a versão mais nova.");
        }
        String motivo = exigirTexto(dto.getMotivoRetificacao(), "Informe o motivo da retificação.");
        original.setRetificada(true);
        checagemRepository.save(original);
        if (original.getLote() != null && original.getResultado() == ResultadoChecagem.FEITO) {
            Lote lote = loteRepository.findByIdParaMovimentar(original.getLote().getUuid()).orElseThrow();
            movimentacaoFarmaciaService.lancar(lote, TipoMovimentacaoFarmacia.ADMINISTRACAO_ESTORNO, original.getQuantidade(),
                    profissional(dto.getProfissionalMatricula()), null, "Checagem da prescrição hospitalar retificada: " + motivo, null, null, null);
        }
        ChecagemPrescricao nova = registrarChecagem(item, original.getHorario(), dto, original);
        nova.setMotivoRetificacao(motivo);
        checagemRepository.save(nova);
        if (original.getHorario() != null) {
            HorarioPrescricao h = horarioRepository.findByIdParaAtualizar(original.getHorario().getUuid()).orElseThrow();
            h.setSituacao(nova.getResultado() == ResultadoChecagem.FEITO ? SituacaoHorarioPrescricao.FEITO : SituacaoHorarioPrescricao.NAO_FEITO);
            horarioRepository.save(h);
        }
        return detalhe(item.getPrescricao());
    }

    // ── Apoio ──────────────────────────────────────────────────────────────────────────────────────────────────────────

    private PrescricaoHospitalar nova(Internacao internacao, String medicoMatricula, Instant inicio, String observacao, UUID renovadaDe) {
        ContextoAuditoria.paciente(internacao.getPaciente().getUuid());
        controleDeAcesso.exigir(PRESCREVER, internacao.getUnidade());
        if (internacao.getStatus() != StatusInternacao.INTERNADO) {
            throw new ResourceUnprocessableEntityException("A internação já teve alta: não recebe prescrição.");
        }
        Instant agora = Instant.now();
        if (inicio.isBefore(agora.minus(Duration.ofHours(2))) || inicio.isAfter(agora.plus(VIGENCIA))) {
            throw new ResourceUnprocessableEntityException("O início vai de 2 horas atrás até 24 horas à frente.");
        }
        List<PrescricaoHospitalar> existentes = prescricaoRepository.findDaInternacaoParaAtualizar(internacao.getUuid());
        for (PrescricaoHospitalar e : existentes) {
            if (e.fimEfetivo().isAfter(inicio)) {
                if (!e.getInicio().isBefore(inicio)) {
                    throw new ResourceConflictException("Já há prescrição desta internação começando neste horário ou depois.");
                }
                e.setEncerradaEm(inicio);
                prescricaoRepository.save(e);
                cancelar(horarioRepository.findByItem_Prescricao_UuidAndSituacao(e.getUuid(), SituacaoHorarioPrescricao.PENDENTE), inicio);
            }
        }
        PrescricaoHospitalar p = new PrescricaoHospitalar();
        p.setInternacao(internacao);
        p.setMedico(profissional(medicoMatricula));
        p.setInicio(inicio);
        p.setFim(inicio.plus(VIGENCIA));
        p.setObservacao(texto(observacao));
        p.setRenovadaDe(renovadaDe);
        p.setRegistradaEm(agora);
        p.setRegistradaPorCpf(UsuarioAutenticado.cpf());
        p = prescricaoRepository.save(p);
        ContextoAuditoria.registro(p.getUuid());
        return p;
    }

    private void criarItem(PrescricaoHospitalar p, ItemPrescricaoRequestDto dto, int ordem) {
        ItemPrescricao i = new ItemPrescricao();
        i.setPrescricao(p);
        i.setOrdem(ordem);
        i.setTipo(dto.getTipo());
        i.setDescricao(texto(dto.getDescricao()));
        i.setIntervaloHoras(dto.getIntervaloHoras());
        String rotulo = "Item " + ordem + ": ";
        switch (dto.getTipo()) {
            case MEDICAMENTO -> {
                if (dto.getMedicamentoId() == null || texto(dto.getDose()) == null || dto.getVia() == null) {
                    throw new ResourceBadRequestException(rotulo + "o medicamento pede o medicamento, a dose e a via.");
                }
                Medicamento m = medicamentoRepository.findById(dto.getMedicamentoId()).orElseThrow(() -> new ResourceNotFoundException(
                        "Não foi possível encontrar um medicamento com o id " + dto.getMedicamentoId() + " em nossos registros."));
                if (!m.isAtivo()) {
                    throw new ResourceUnprocessableEntityException(rotulo + "o medicamento " + m.getNome() + " está inativo.");
                }
                i.setMedicamento(m);
                i.setDose(dto.getDose().trim());
                i.setVia(dto.getVia());
                if (dto.isSeNecessario()) {
                    i.setSeNecessario(true);
                    i.setCondicao(exigirTexto(dto.getCondicao(), rotulo + "o \"se necessário\" pede a condição de uso."));
                    i.setIntervaloHoras(dto.getIntervaloHoras());
                }
            }
            case DIETA -> {
                if (dto.getTipoDieta() == null) {
                    throw new ResourceBadRequestException(rotulo + "informe o tipo de dieta.");
                }
                i.setTipoDieta(dto.getTipoDieta());
                if (dto.getTipoDieta().oral() || dto.getTipoDieta() == TipoDieta.ZERO) {
                    i.setIntervaloHoras(null);
                }
            }
            case CUIDADO -> {
                if (i.getDescricao() == null) {
                    throw new ResourceBadRequestException(rotulo + "descreva o cuidado.");
                }
            }
        }
        Instant primeiro = dto.getPrimeiroHorario() != null ? dto.getPrimeiroHorario().truncatedTo(ChronoUnit.MINUTES) : null;
        if (primeiro != null && (primeiro.isBefore(p.getInicio()) || !primeiro.isBefore(p.getFim()))) {
            throw new ResourceBadRequestException(rotulo + "o primeiro horário precisa estar dentro da vigência da prescrição.");
        }
        i.setPrimeiroHorario(primeiro);
        i = itemRepository.save(i);
        for (Instant t : aprazar(i, p)) {
            HorarioPrescricao h = new HorarioPrescricao();
            h.setItem(i);
            h.setPrevistoPara(t);
            h.setSituacao(SituacaoHorarioPrescricao.PENDENTE);
            horarioRepository.save(h);
        }
    }

    /**
     * Os horários do item na vigência: do primeiro horário (ou do início) em diante, de intervalo em intervalo; sem intervalo, um só
     * (dose única do medicamento). Dieta oral: as refeições; zero: nenhum. Cuidado sem intervalo e "se necessário": nenhum.
     */
    static List<Instant> aprazar(ItemPrescricao i, PrescricaoHospitalar p) {
        List<Instant> horarios = new ArrayList<>();
        if (i.isSeNecessario()) {
            return horarios;
        }
        if (i.getTipo() == TipoItemPrescricao.DIETA && (i.getTipoDieta() == TipoDieta.ZERO || i.getTipoDieta().oral())) {
            if (i.getTipoDieta() == TipoDieta.ZERO) {
                return horarios;
            }
            ZoneId zona = ZoneId.systemDefault();
            LocalDate dia = p.getInicio().atZone(zona).toLocalDate();
            for (int d = 0; d <= 1; d++) {
                for (LocalTime r : REFEICOES) {
                    Instant t = dia.plusDays(d).atTime(r).atZone(zona).toInstant();
                    if (!t.isBefore(p.getInicio()) && t.isBefore(p.getFim())) {
                        horarios.add(t);
                    }
                }
            }
            return horarios;
        }
        Instant primeiro = i.getPrimeiroHorario() != null ? i.getPrimeiroHorario() : p.getInicio();
        if (i.getIntervaloHoras() == null) {
            if (i.getTipo() == TipoItemPrescricao.MEDICAMENTO) {
                horarios.add(primeiro);
            }
            return horarios;
        }
        for (Instant t = primeiro; t.isBefore(p.getFim()); t = t.plus(Duration.ofHours(i.getIntervaloHoras()))) {
            horarios.add(t);
        }
        return horarios;
    }

    private ChecagemPrescricao registrarChecagem(ItemPrescricao item, HorarioPrescricao h, AcaoPrescricaoRequestDto dto, ChecagemPrescricao retificacaoDe) {
        if (dto.getResultado() == null) {
            throw new ResourceBadRequestException("Informe se foi feito ou não.");
        }
        Instant agora = Instant.now();
        Instant realizado = dto.getRealizadoEm() != null ? dto.getRealizadoEm() : (retificacaoDe != null ? retificacaoDe.getRealizadoEm() : agora);
        if (realizado.isAfter(agora.plus(Duration.ofMinutes(5)))) {
            throw new ResourceUnprocessableEntityException("A hora da checagem não pode estar no futuro.");
        }
        if (item.getSuspensoEm() != null && retificacaoDe == null) {
            throw new ResourceUnprocessableEntityException("O item foi suspenso pelo médico.");
        }
        Internacao internacao = item.getPrescricao().getInternacao();
        ChecagemPrescricao c = new ChecagemPrescricao();
        c.setItem(item);
        c.setHorario(h);
        c.setResultado(dto.getResultado());
        c.setObservacao(texto(dto.getObservacao()));
        c.setRealizadoEm(realizado);
        c.setProfissional(profissional(dto.getProfissionalMatricula()));
        c.setRetificacaoDe(retificacaoDe);
        c.setRegistradoEm(agora);
        c.setRegistradoPorCpf(UsuarioAutenticado.cpf());
        boolean feito = dto.getResultado() == ResultadoChecagem.FEITO;
        switch (item.getTipo()) {
            case MEDICAMENTO -> {
                if (!feito) {
                    if (dto.getMotivoNaoAdministracao() == null) {
                        throw new ResourceBadRequestException("Informe o motivo de não ter administrado.");
                    }
                    c.setMotivoNaoAdministracao(dto.getMotivoNaoAdministracao());
                } else if (dto.getLoteId() != null) {
                    Lote lote = loteRepository.findByIdParaMovimentar(dto.getLoteId()).orElseThrow(() -> new ResourceNotFoundException(
                            "Não foi possível encontrar um lote com o id " + dto.getLoteId() + " em nossos registros."));
                    if (!lote.getMedicamento().getUuid().equals(item.getMedicamento().getUuid())) {
                        throw new ResourceUnprocessableEntityException("O lote é de outro medicamento.");
                    }
                    if (!lote.getUnidade().getUuid().equals(internacao.getUnidade().getUuid())) {
                        throw new ResourceUnprocessableEntityException("O lote precisa ser do estoque da unidade da internação.");
                    }
                    int quantidade = dto.getQuantidade() != null ? dto.getQuantidade() : 1;
                    c.setLote(lote);
                    c.setQuantidade(quantidade);
                    movimentacaoFarmaciaService.lancar(lote, TipoMovimentacaoFarmacia.ADMINISTRACAO, -quantidade, c.getProfissional(), null,
                            "Prescrição hospitalar: " + internacao.getPaciente().getNome() + " · " + PrescricaoDtos.resumo(item), null, null, null);
                }
            }
            case DIETA -> {
                if (feito) {
                    if (dto.getAceitacao() == null) {
                        throw new ResourceBadRequestException("Informe a aceitação da dieta.");
                    }
                    c.setAceitacao(dto.getAceitacao());
                } else if (c.getObservacao() == null) {
                    throw new ResourceBadRequestException("Informe por que a dieta não foi ofertada.");
                }
            }
            case CUIDADO -> {
                if (!feito && c.getObservacao() == null) {
                    throw new ResourceBadRequestException("Informe por que o cuidado não foi feito.");
                }
            }
        }
        return checagemRepository.save(c);
    }

    /** Medicamento e cuidado: enfermagem; dieta: enfermagem ou nutrição. */
    private void exigirChecagem(ItemPrescricao item) {
        Internacao internacao = item.getPrescricao().getInternacao();
        ContextoAuditoria.paciente(internacao.getPaciente().getUuid());
        if (item.getTipo() == TipoItemPrescricao.DIETA) {
            controleDeAcesso.exigirAlguma(CHECAR, CHECAR_DIETA);
            controleDeAcesso.exigirVisivel(internacao.getUnidade(), CHECAR, CHECAR_DIETA);
        } else {
            controleDeAcesso.exigir(CHECAR, internacao.getUnidade());
        }
        if (internacao.getStatus() != StatusInternacao.INTERNADO) {
            throw new ResourceUnprocessableEntityException("A internação já teve alta.");
        }
    }

    /** Cancela os horários pendentes (a partir de um instante, se informado). */
    private void cancelar(List<HorarioPrescricao> pendentes, Instant aPartirDe) {
        for (HorarioPrescricao h : pendentes) {
            if (aPartirDe == null || !h.getPrevistoPara().isBefore(aPartirDe)) {
                h.setSituacao(SituacaoHorarioPrescricao.CANCELADO);
                horarioRepository.save(h);
            }
        }
    }

    private static boolean atrasado(HorarioPrescricao h, Instant agora) {
        return h.getSituacao() == SituacaoHorarioPrescricao.PENDENTE && h.getPrevistoPara().isBefore(agora.minus(ATRASO));
    }

    private PrescricaoDtos.Prescricao detalhe(PrescricaoHospitalar p) {
        ContextoAuditoria.paciente(p.getInternacao().getPaciente().getUuid());
        ContextoAuditoria.registro(p.getUuid());
        return montar(List.of(p)).get(0);
    }

    private List<PrescricaoDtos.Prescricao> montar(List<PrescricaoHospitalar> prescricoes) {
        if (prescricoes.isEmpty()) {
            return List.of();
        }
        Instant agora = Instant.now();
        List<ItemPrescricao> itens = itemRepository.findByPrescricao_UuidInOrderByOrdemAsc(prescricoes.stream().map(PrescricaoHospitalar::getUuid).toList());
        List<UUID> itemIds = itens.stream().map(ItemPrescricao::getUuid).toList();
        Map<UUID, List<HorarioPrescricao>> horarios = itemIds.isEmpty() ? Map.of()
                : horarioRepository.findByItem_UuidInOrderByPrevistoParaAsc(itemIds).stream().collect(Collectors.groupingBy(h -> h.getItem().getUuid()));
        Map<UUID, List<ChecagemPrescricao>> checagens = itemIds.isEmpty() ? Map.of()
                : checagemRepository.findByItem_UuidInOrderByRealizadoEmAscRegistradoEmAsc(itemIds).stream().collect(Collectors.groupingBy(c -> c.getItem().getUuid()));
        Map<UUID, List<PrescricaoDtos.Item>> porPrescricao = itens.stream().collect(Collectors.groupingBy(i -> i.getPrescricao().getUuid(),
                Collectors.mapping(i -> {
                    List<ChecagemPrescricao> doItem = checagens.getOrDefault(i.getUuid(), List.of());
                    Map<UUID, ChecagemPrescricao> vigentePorHorario = doItem.stream().filter(c -> !c.isRetificada() && c.getHorario() != null)
                            .collect(Collectors.toMap(c -> c.getHorario().getUuid(), Function.identity(), (a, b) -> b));
                    List<PrescricaoDtos.Horario> hs = horarios.getOrDefault(i.getUuid(), List.of()).stream()
                            .sorted(Comparator.comparing(HorarioPrescricao::getPrevistoPara))
                            .map(h -> PrescricaoDtos.Horario.from(h, atrasado(h, agora), vigentePorHorario.containsKey(h.getUuid())
                                    ? PrescricaoDtos.Checagem.from(vigentePorHorario.get(h.getUuid())) : null)).toList();
                    return PrescricaoDtos.Item.from(i, hs, doItem.stream().map(PrescricaoDtos.Checagem::from).toList());
                }, Collectors.toList())));
        return prescricoes.stream().map(p -> PrescricaoDtos.Prescricao.from(p, agora,
                porPrescricao.getOrDefault(p.getUuid(), List.of()).stream().sorted(Comparator.comparingInt(PrescricaoDtos.Item::ordem)).toList())).toList();
    }

    private static Instant proximaHoraCheia() {
        return Instant.now().truncatedTo(ChronoUnit.HOURS).plus(Duration.ofHours(1));
    }

    private Profissional profissional(String matricula) {
        if (matricula == null || matricula.isBlank()) {
            throw new ResourceBadRequestException("Informe a matrícula.");
        }
        return profissionalRepository.findByMatricula(matricula.trim()).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar um profissional com a matrícula " + matricula.trim() + " em nossos registros."));
    }

    private static String exigirTexto(String s, String mensagem) {
        String t = texto(s);
        if (t == null) {
            throw new ResourceBadRequestException(mensagem);
        }
        return t;
    }

    private static String texto(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }

    private static ResourceNotFoundException naoEncontrada(UUID uuid) {
        return new ResourceNotFoundException("Não foi possível encontrar uma prescrição hospitalar com o id " + uuid + " em nossos registros.");
    }

    private static ResourceNotFoundException itemNaoEncontrado(UUID uuid) {
        return new ResourceNotFoundException("Não foi possível encontrar um item de prescrição com o id " + uuid + " em nossos registros.");
    }

    private static ResourceNotFoundException horarioNaoEncontrado(UUID uuid) {
        return new ResourceNotFoundException("Não foi possível encontrar um horário de prescrição com o id " + uuid + " em nossos registros.");
    }
}
