package com.edufelizardo.maissaudepublica.services.version1;

import com.edufelizardo.maissaudepublica.config.ContextoAuditoria;
import com.edufelizardo.maissaudepublica.config.UsuarioAutenticado;
import com.edufelizardo.maissaudepublica.exceptions.ResourceBadRequestException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceConflictException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceNotFoundException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceUnprocessableEntityException;
import com.edufelizardo.maissaudepublica.models.AcaoControleVetorial;
import com.edufelizardo.maissaudepublica.models.Endereco;
import com.edufelizardo.maissaudepublica.models.FocoVetorial;
import com.edufelizardo.maissaudepublica.models.Imovel;
import com.edufelizardo.maissaudepublica.models.Microarea;
import com.edufelizardo.maissaudepublica.models.Notificacao;
import com.edufelizardo.maissaudepublica.models.VistoriaVetorial;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.AcaoControleVetorialRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.CancelamentoAcaoVetorialRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.LaboratorioFocoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.RealizacaoAcaoVetorialRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.SituacaoFocoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.VistoriaVetorialRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.AcaoControleVetorialDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.BloqueioPendenteDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.FocoVetorialDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.ImovelVetorialDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.IndicesVetoriaisDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.VistoriaVetorialDto;
import com.edufelizardo.maissaudepublica.models.enuns.ClassificacaoCasoEpidemiologico;
import com.edufelizardo.maissaudepublica.models.enuns.EspecieVetor;
import com.edufelizardo.maissaudepublica.models.enuns.ResultadoLaboratorioFoco;
import com.edufelizardo.maissaudepublica.models.enuns.ResultadoVistoriaVetorial;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoAcaoControleVetorial;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoFoco;
import com.edufelizardo.maissaudepublica.models.enuns.TipoDeposito;
import com.edufelizardo.maissaudepublica.repositories.AcaoControleVetorialRepository;
import com.edufelizardo.maissaudepublica.repositories.FocoVetorialRepository;
import com.edufelizardo.maissaudepublica.repositories.ImovelRepository;
import com.edufelizardo.maissaudepublica.repositories.MicroareaRepository;
import com.edufelizardo.maissaudepublica.repositories.NotificacaoRepository;
import com.edufelizardo.maissaudepublica.repositories.ProfissionalRepository;
import com.edufelizardo.maissaudepublica.repositories.SurtoRepository;
import com.edufelizardo.maissaudepublica.repositories.VistoriaVetorialRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Controle de vetores, fatia V4a da Vigilância (ADR-0162, desenho na ADR-0110): a vistoria do agente de endemias ao imóvel
 * do Território, os focos com o laboratório e o tratamento, os índices do ciclo bimestral (IIP e Breteau, como no PNCD) e
 * as ações de controle, com os casos de arbovirose ainda sem bloqueio. O imóvel não fica preso à microárea do ACS; o
 * agente não vê família nem morador.
 */
@Service
public class ControleVetorialService {

    public static final String VISTORIAR = "ZOONOSES.VISTORIAR";
    public static final String GERENCIAR = "ZOONOSES.GERENCIAR";
    /** Arboviroses transmitidas pelo Aedes, para a sugestão de bloqueio. */
    static final Set<String> ARBOVIROSES = Set.of("DENGUE", "CHIKUNGUNYA", "ZIKA");
    private static final int DIAS_PARA_BLOQUEIO = 30;

    @Autowired private VistoriaVetorialRepository vistoriaRepository;
    @Autowired private FocoVetorialRepository focoRepository;
    @Autowired private AcaoControleVetorialRepository acaoRepository;
    @Autowired private ImovelRepository imovelRepository;
    @Autowired private MicroareaRepository microareaRepository;
    @Autowired private ProfissionalRepository profissionalRepository;
    @Autowired private NotificacaoRepository notificacaoRepository;
    @Autowired private SurtoRepository surtoRepository;
    @Autowired private ControleDeAcesso controleDeAcesso;

    // ── Ciclo ──────────────────────────────────────────────────────────────────────────────────────

    /** O ciclo bimestral do PNCD: 1 (jan-fev) a 6 (nov-dez). */
    static int ciclo(LocalDate data) {
        return (data.getMonthValue() - 1) / 2 + 1;
    }

    static LocalDate inicioDoCiclo(int ano, int ciclo) {
        return LocalDate.of(ano, ciclo * 2 - 1, 1);
    }

    // ── Imóveis ────────────────────────────────────────────────────────────────────────────────────

    /** Os imóveis ativos da área (ou da microárea), com a busca no endereço e a última vistoria; sem família nem morador. */
    @Transactional(readOnly = true)
    public List<ImovelVetorialDto> imoveis(UUID areaId, UUID microareaId, String busca) {
        controleDeAcesso.exigirAlguma(VISTORIAR, GERENCIAR);
        if (areaId == null && microareaId == null) {
            throw new ResourceBadRequestException("Informe a área ou a microárea.");
        }
        List<Imovel> lista = areaId != null ? imovelRepository.findByMicroarea_Area_Uuid(areaId) : imovelRepository.findByMicroarea_Area_Uuid(
                microarea(microareaId).getArea().getUuid());
        String termo = texto(busca) == null ? null : busca.trim().toLowerCase();
        LocalDate hoje = LocalDate.now();
        return lista.stream().filter(Imovel::isAtivo)
                .filter(i -> microareaId == null || i.getMicroarea().getUuid().equals(microareaId))
                .filter(i -> termo == null || endereco(i).toLowerCase().contains(termo))
                .sorted(Comparator.comparing(ControleVetorialService::endereco, String.CASE_INSENSITIVE_ORDER))
                .map(i -> {
                    List<VistoriaVetorial> vs = vistoriaRepository.findDoImovel(i.getUuid());
                    VistoriaVetorial ultima = vs.isEmpty() ? null : vs.get(0);
                    boolean pendente = ultima != null && ultima.getCicloAno() == hoje.getYear() && ultima.getCicloNumero() == ciclo(hoje)
                            && ultima.getResultado() != ResultadoVistoriaVetorial.INSPECIONADA;
                    return imovelDto(i, ultima, pendente);
                }).toList();
    }

    /** As pendências de revisita do ciclo atual: imóveis cuja última vistoria do ciclo ficou fechada ou recusada. */
    @Transactional(readOnly = true)
    public List<ImovelVetorialDto> pendencias(UUID areaId) {
        controleDeAcesso.exigirAlguma(VISTORIAR, GERENCIAR);
        LocalDate hoje = LocalDate.now();
        return ultimasDoCiclo(vistoriaRepository.findDoCiclo(hoje.getYear(), ciclo(hoje))).values().stream()
                .filter(v -> v.getResultado() != ResultadoVistoriaVetorial.INSPECIONADA)
                .filter(v -> areaId == null || v.getImovel().getMicroarea().getArea().getUuid().equals(areaId))
                .sorted(Comparator.comparing(VistoriaVetorial::getData))
                .map(v -> imovelDto(v.getImovel(), v, true)).toList();
    }

    // ── Vistoria ───────────────────────────────────────────────────────────────────────────────────

    /**
     * Registra a vistoria: imóvel ativo (422), agente pela matrícula (404), data não futura (400). Fechada ou recusada não
     * leva depósito nem foco (422). Os focos de um tipo não passam dos depósitos inspecionados desse tipo (422). O larvicida
     * acompanha a quantidade (400). A ação, se houver, não pode estar cancelada (422).
     */
    @Transactional
    public VistoriaVetorialDto registrarVistoria(VistoriaVetorialRequestDto dto) {
        controleDeAcesso.exigirAlguma(VISTORIAR);
        Imovel imovel = imovelRepository.findById(dto.getImovelId()).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar o imóvel " + dto.getImovelId() + " em nossos registros."));
        if (!imovel.isAtivo()) {
            throw new ResourceUnprocessableEntityException("O imóvel está inativo no cadastro territorial.");
        }
        LocalDate data = dto.getData() != null ? dto.getData() : LocalDate.now();
        if (data.isAfter(LocalDate.now())) {
            throw new ResourceBadRequestException("A vistoria não pode ser de data futura.");
        }
        if (dto.getLarvicidaGramas() != null && texto(dto.getLarvicida()) == null) {
            throw new ResourceBadRequestException("Informe o larvicida usado.");
        }
        boolean inspecionada = dto.getResultado() == ResultadoVistoriaVetorial.INSPECIONADA;
        List<VistoriaVetorialRequestDto.Foco> focos = dto.getFocos() == null ? List.of() : dto.getFocos();
        Map<TipoDeposito, Integer> inspecionados = new EnumMap<>(TipoDeposito.class);
        inspecionados.put(TipoDeposito.A1, zero(dto.getDepositosA1()));
        inspecionados.put(TipoDeposito.A2, zero(dto.getDepositosA2()));
        inspecionados.put(TipoDeposito.B, zero(dto.getDepositosB()));
        inspecionados.put(TipoDeposito.C, zero(dto.getDepositosC()));
        inspecionados.put(TipoDeposito.D1, zero(dto.getDepositosD1()));
        inspecionados.put(TipoDeposito.D2, zero(dto.getDepositosD2()));
        inspecionados.put(TipoDeposito.E, zero(dto.getDepositosE()));
        int total = inspecionados.values().stream().mapToInt(Integer::intValue).sum();
        if (!inspecionada && (total > 0 || !focos.isEmpty() || zero(dto.getDepositosEliminados()) > 0 || zero(dto.getDepositosTratados()) > 0)) {
            throw new ResourceUnprocessableEntityException("Imóvel fechado ou recusado não tem depósito nem foco: registre a revisita.");
        }
        Map<TipoDeposito, Long> porTipo = focos.stream().collect(Collectors.groupingBy(VistoriaVetorialRequestDto.Foco::getTipoDeposito, Collectors.counting()));
        for (Map.Entry<TipoDeposito, Long> e : porTipo.entrySet()) {
            if (e.getValue() > inspecionados.get(e.getKey())) {
                throw new ResourceUnprocessableEntityException("Há " + e.getValue() + " foco(s) no tipo " + e.getKey() + ", mais que os depósitos "
                        + "inspecionados desse tipo (" + inspecionados.get(e.getKey()) + ").");
            }
        }
        if (zero(dto.getDepositosEliminados()) + zero(dto.getDepositosTratados()) > total) {
            throw new ResourceUnprocessableEntityException("Os depósitos eliminados e tratados não passam dos inspecionados.");
        }
        AcaoControleVetorial acao = null;
        if (dto.getAcaoId() != null) {
            acao = acao(dto.getAcaoId());
            if (acao.getSituacao() == SituacaoAcaoControleVetorial.CANCELADA) {
                throw new ResourceUnprocessableEntityException("A ação de controle foi cancelada.");
            }
        }
        VistoriaVetorial v = new VistoriaVetorial();
        v.setImovel(imovel);
        v.setData(data);
        v.setCicloAno(data.getYear());
        v.setCicloNumero(ciclo(data));
        v.setAgente(profissionalRepository.findByMatricula(dto.getAgenteMatricula().trim()).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar um profissional com a matrícula " + dto.getAgenteMatricula().trim() + " em nossos registros.")));
        v.setTipo(dto.getTipo());
        v.setResultado(dto.getResultado());
        v.setDepositosA1(inspecionados.get(TipoDeposito.A1));
        v.setDepositosA2(inspecionados.get(TipoDeposito.A2));
        v.setDepositosB(inspecionados.get(TipoDeposito.B));
        v.setDepositosC(inspecionados.get(TipoDeposito.C));
        v.setDepositosD1(inspecionados.get(TipoDeposito.D1));
        v.setDepositosD2(inspecionados.get(TipoDeposito.D2));
        v.setDepositosE(inspecionados.get(TipoDeposito.E));
        v.setDepositosEliminados(zero(dto.getDepositosEliminados()));
        v.setDepositosTratados(zero(dto.getDepositosTratados()));
        v.setLarvicida(texto(dto.getLarvicida()));
        v.setLarvicidaGramas(dto.getLarvicidaGramas());
        v.setAcao(acao);
        v.setObservacao(texto(dto.getObservacao()));
        v.setRegistradoEm(Instant.now());
        v.setRegistradoPorCpf(UsuarioAutenticado.cpf());
        for (VistoriaVetorialRequestDto.Foco f : focos) {
            FocoVetorial foco = new FocoVetorial();
            foco.setVistoria(v);
            foco.setTipoDeposito(f.getTipoDeposito());
            foco.setFase(f.getFase());
            foco.setEspecieSuspeita(f.getEspecieSuspeita() != null ? f.getEspecieSuspeita() : EspecieVetor.NAO_IDENTIFICADA);
            foco.setAmostraColetada(Boolean.TRUE.equals(f.getAmostraColetada()));
            foco.setSituacao(SituacaoFoco.ATIVO);
            v.getFocos().add(foco);
        }
        vistoriaRepository.save(v);
        ContextoAuditoria.registro(v.getUuid());
        ContextoAuditoria.detalhe("Vistoria " + v.getResultado() + (focos.isEmpty() ? "" : ", " + focos.size() + " foco(s)") + ".");
        return dto(v);
    }

    /** As vistorias do ciclo (sem ciclo, o atual), opcionalmente da área ou do imóvel. */
    @Transactional(readOnly = true)
    public List<VistoriaVetorialDto> vistorias(Integer ano, Integer ciclo, UUID areaId, UUID imovelId) {
        controleDeAcesso.exigirAlguma(VISTORIAR, GERENCIAR, "VIGILANCIA.INVESTIGAR", "INDICADOR.CONSULTAR");
        if (imovelId != null) {
            return vistoriaRepository.findDoImovel(imovelId).stream().map(this::dto).toList();
        }
        int[] c = cicloPedido(ano, ciclo);
        return vistoriaRepository.findDoCiclo(c[0], c[1]).stream()
                .filter(v -> areaId == null || v.getImovel().getMicroarea().getArea().getUuid().equals(areaId))
                .map(this::dto).toList();
    }

    // ── Focos ──────────────────────────────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<FocoVetorialDto> focos(SituacaoFoco situacao, UUID areaId) {
        controleDeAcesso.exigirAlguma(VISTORIAR, GERENCIAR, "VIGILANCIA.INVESTIGAR", "INDICADOR.CONSULTAR");
        return focoRepository.findTodos().stream()
                .filter(f -> situacao == null || f.getSituacao() == situacao)
                .filter(f -> areaId == null || f.getVistoria().getImovel().getMicroarea().getArea().getUuid().equals(areaId))
                .map(ControleVetorialService::dto).toList();
    }

    /** O resultado do laboratório: só do foco com amostra (422). */
    @Transactional
    public FocoVetorialDto laboratorio(UUID focoId, LaboratorioFocoRequestDto dto) {
        controleDeAcesso.exigirAlguma(VISTORIAR, GERENCIAR);
        FocoVetorial f = foco(focoId);
        if (!f.isAmostraColetada()) {
            throw new ResourceUnprocessableEntityException("O foco não teve amostra coletada.");
        }
        f.setResultadoLaboratorio(dto.getResultado());
        f.setResultadoEm(Instant.now());
        focoRepository.save(f);
        ContextoAuditoria.registro(f.getUuid());
        ContextoAuditoria.detalhe("Resultado do laboratório: " + dto.getResultado() + ".");
        return dto(f);
    }

    /** Tratado ou eliminado, só do ativo (422); ativo de novo, 400. */
    @Transactional
    public FocoVetorialDto situacaoDoFoco(UUID focoId, SituacaoFocoRequestDto dto) {
        controleDeAcesso.exigirAlguma(VISTORIAR);
        FocoVetorial f = foco(focoId);
        if (dto.getSituacao() == SituacaoFoco.ATIVO) {
            throw new ResourceBadRequestException("O foco passa a tratado ou eliminado.");
        }
        if (f.getSituacao() != SituacaoFoco.ATIVO) {
            throw new ResourceUnprocessableEntityException("O foco já foi " + (f.getSituacao() == SituacaoFoco.TRATADO ? "tratado." : "eliminado."));
        }
        f.setSituacao(dto.getSituacao());
        f.setEncerradoEm(Instant.now());
        f.setObservacao(texto(dto.getObservacao()));
        focoRepository.save(f);
        ContextoAuditoria.registro(f.getUuid());
        return dto(f);
    }

    // ── Índices ────────────────────────────────────────────────────────────────────────────────────

    /**
     * Os índices do ciclo (sem ciclo, o atual), por área e no município: IIP (imóveis positivos ÷ inspecionados × 100) e
     * Breteau (depósitos positivos ÷ imóveis inspecionados × 100); o foco negativo no laboratório não conta. Pendente é o
     * imóvel cuja última vistoria do ciclo ficou fechada ou recusada.
     */
    @Transactional(readOnly = true)
    public IndicesVetoriaisDto indices(Integer ano, Integer ciclo) {
        controleDeAcesso.exigirAlguma(VISTORIAR, GERENCIAR, "VIGILANCIA.INVESTIGAR", "INDICADOR.CONSULTAR");
        int[] c = cicloPedido(ano, ciclo);
        List<VistoriaVetorial> doCiclo = vistoriaRepository.findDoCiclo(c[0], c[1]);
        Map<UUID, List<VistoriaVetorial>> porArea = doCiclo.stream()
                .collect(Collectors.groupingBy(v -> v.getImovel().getMicroarea().getArea().getUuid(), LinkedHashMap::new, Collectors.toList()));
        List<IndicesVetoriaisDto.Linha> areas = porArea.values().stream()
                .map(vs -> linha(vs, vs.get(0).getImovel().getMicroarea().getArea().getUuid(), vs.get(0).getImovel().getMicroarea().getArea().getNome()))
                .sorted(Comparator.comparing(IndicesVetoriaisDto.Linha::getAreaNome, String.CASE_INSENSITIVE_ORDER)).toList();
        LocalDate inicio = inicioDoCiclo(c[0], c[1]);
        return new IndicesVetoriaisDto(c[0], c[1], inicio, inicio.plusMonths(2).minusDays(1), linha(doCiclo, null, null), areas);
    }

    private IndicesVetoriaisDto.Linha linha(List<VistoriaVetorial> vs, UUID areaId, String areaNome) {
        Set<UUID> visitados = new HashSet<>();
        Set<UUID> inspecionados = new HashSet<>();
        Set<UUID> positivos = new HashSet<>();
        int depositosPositivos = 0;
        for (VistoriaVetorial v : vs) {
            visitados.add(v.getImovel().getUuid());
            if (v.getResultado() == ResultadoVistoriaVetorial.INSPECIONADA) {
                inspecionados.add(v.getImovel().getUuid());
                long n = v.getFocos().stream().filter(ControleVetorialService::conta).count();
                if (n > 0) {
                    positivos.add(v.getImovel().getUuid());
                    depositosPositivos += (int) n;
                }
            }
        }
        int pendentes = (int) ultimasDoCiclo(vs).values().stream().filter(v -> v.getResultado() != ResultadoVistoriaVetorial.INSPECIONADA).count();
        Double iip = inspecionados.isEmpty() ? null : percentual(positivos.size(), inspecionados.size());
        Double ib = inspecionados.isEmpty() ? null : percentual(depositosPositivos, inspecionados.size());
        String classificacao = iip == null ? null : iip < 1 ? "SATISFATORIO" : iip < 4 ? "ALERTA" : "RISCO";
        return new IndicesVetoriaisDto.Linha(areaId, areaNome, visitados.size(), inspecionados.size(), pendentes, positivos.size(), depositosPositivos,
                iip, ib, visitados.isEmpty() ? null : percentual(pendentes, visitados.size()), classificacao);
    }

    // ── Ações de controle ──────────────────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<AcaoControleVetorialDto> acoes(SituacaoAcaoControleVetorial situacao) {
        controleDeAcesso.exigirAlguma(VISTORIAR, GERENCIAR, "VIGILANCIA.INVESTIGAR", "INDICADOR.CONSULTAR");
        return acaoRepository.findTodas().stream().filter(a -> situacao == null || a.getSituacao() == situacao).map(this::dto).toList();
    }

    /**
     * Planeja a ação: as microáreas existem (404). O caso notificado precisa ser de arbovirose (422) e não ter outra ação
     * em curso (409); o surto precisa existir (404).
     */
    @Transactional
    public AcaoControleVetorialDto planejar(AcaoControleVetorialRequestDto dto) {
        controleDeAcesso.exigirAlguma(GERENCIAR);
        AcaoControleVetorial a = new AcaoControleVetorial();
        a.setTipo(dto.getTipo());
        a.setDescricao(texto(dto.getDescricao()));
        for (UUID id : new HashSet<>(dto.getMicroareaIds())) {
            a.getMicroareas().add(microarea(id));
        }
        a.setDataPrevista(dto.getDataPrevista());
        a.setResponsavel(dto.getResponsavel().trim());
        if (dto.getNotificacaoId() != null) {
            Notificacao n = notificacaoRepository.findById(dto.getNotificacaoId()).orElseThrow(() -> new ResourceNotFoundException(
                    "Não foi possível encontrar a notificação " + dto.getNotificacaoId() + " em nossos registros."));
            if (!ARBOVIROSES.contains(n.getAgravo().getCodigo())) {
                throw new ResourceUnprocessableEntityException("O bloqueio vetorial é de caso de dengue, chikungunya ou zika.");
            }
            if (acaoRepository.findNotificacoesComAcao().contains(n.getUuid())) {
                throw new ResourceConflictException("Este caso já tem uma ação de controle.");
            }
            a.setNotificacao(n);
        }
        if (dto.getSurtoId() != null) {
            a.setSurto(surtoRepository.findById(dto.getSurtoId()).orElseThrow(() -> new ResourceNotFoundException(
                    "Não foi possível encontrar o surto " + dto.getSurtoId() + " em nossos registros.")));
        }
        a.setSituacao(SituacaoAcaoControleVetorial.PLANEJADA);
        a.setRegistradoEm(Instant.now());
        a.setRegistradoPorCpf(UsuarioAutenticado.cpf());
        acaoRepository.save(a);
        ContextoAuditoria.registro(a.getUuid());
        return dto(a);
    }

    /** Realizada, da planejada (422); a data não é futura (400); sem data, hoje. */
    @Transactional
    public AcaoControleVetorialDto realizar(UUID id, RealizacaoAcaoVetorialRequestDto dto) {
        controleDeAcesso.exigirAlguma(GERENCIAR);
        AcaoControleVetorial a = planejada(id);
        LocalDate data = dto.getData() != null ? dto.getData() : LocalDate.now();
        if (data.isAfter(LocalDate.now())) {
            throw new ResourceBadRequestException("A ação não pode ser realizada em data futura.");
        }
        a.setSituacao(SituacaoAcaoControleVetorial.REALIZADA);
        a.setDataRealizada(data);
        a.setImoveisTrabalhados(dto.getImoveisTrabalhados());
        a.setResultado(texto(dto.getResultado()));
        acaoRepository.save(a);
        ContextoAuditoria.registro(a.getUuid());
        ContextoAuditoria.detalhe("Ação de controle realizada.");
        return dto(a);
    }

    /** Cancelada, da planejada (422), com o motivo. */
    @Transactional
    public AcaoControleVetorialDto cancelar(UUID id, CancelamentoAcaoVetorialRequestDto dto) {
        controleDeAcesso.exigirAlguma(GERENCIAR);
        AcaoControleVetorial a = planejada(id);
        a.setSituacao(SituacaoAcaoControleVetorial.CANCELADA);
        a.setMotivoCancelamento(dto.getMotivo().trim());
        acaoRepository.save(a);
        ContextoAuditoria.registro(a.getUuid());
        return dto(a);
    }

    /**
     * Os casos notificados de dengue, chikungunya ou zika dos últimos 30 dias, não descartados e sem ação de controle em
     * curso: o lugar e as datas para o bloqueio, sem o paciente.
     */
    @Transactional(readOnly = true)
    public List<BloqueioPendenteDto> bloqueiosPendentes() {
        controleDeAcesso.exigirAlguma(GERENCIAR, "VIGILANCIA.INVESTIGAR");
        Set<UUID> comAcao = new HashSet<>(acaoRepository.findNotificacoesComAcao());
        LocalDate desde = LocalDate.now().minusDays(DIAS_PARA_BLOQUEIO);
        return notificacaoRepository.findTodas().stream()
                .filter(n -> ARBOVIROSES.contains(n.getAgravo().getCodigo()))
                .filter(n -> !n.getDataNotificacao().isBefore(desde))
                .filter(n -> n.getClassificacaoFinal() != ClassificacaoCasoEpidemiologico.DESCARTADO)
                .filter(n -> !comAcao.contains(n.getUuid()))
                .sorted(Comparator.comparing(Notificacao::getDataNotificacao).reversed())
                .map(n -> {
                    Microarea m = n.getMicroarea();
                    return new BloqueioPendenteDto(n.getUuid(), n.getAgravo().getNome(), n.getDataNotificacao(), n.getDataPrimeirosSintomas(),
                            n.getUnidade().getNome(), m != null ? m.getUuid() : null, m != null ? m.getCodigo() : null, m != null ? m.getArea().getNome() : null,
                            n.getLocalProvavel(), n.getLatitude(), n.getLongitude());
                }).toList();
    }

    // ── Apoio ──────────────────────────────────────────────────────────────────────────────────────

    /** A última vistoria de cada imóvel na lista (já em ordem decrescente). */
    private static Map<UUID, VistoriaVetorial> ultimasDoCiclo(List<VistoriaVetorial> vs) {
        Map<UUID, VistoriaVetorial> ultima = new LinkedHashMap<>();
        vs.stream().sorted(Comparator.comparing(VistoriaVetorial::getData).thenComparing(VistoriaVetorial::getRegistradoEm).reversed())
                .forEach(v -> ultima.putIfAbsent(v.getImovel().getUuid(), v));
        return ultima;
    }

    private static boolean conta(FocoVetorial f) {
        return f.getResultadoLaboratorio() != ResultadoLaboratorioFoco.NEGATIVO;
    }

    private static int[] cicloPedido(Integer ano, Integer ciclo) {
        LocalDate hoje = LocalDate.now();
        int a = ano != null ? ano : hoje.getYear();
        int c = ciclo != null ? ciclo : ciclo(hoje);
        if (c < 1 || c > 6) {
            throw new ResourceBadRequestException("O ciclo vai de 1 a 6.");
        }
        return new int[]{a, c};
    }

    private static Double percentual(int parte, int total) {
        return BigDecimal.valueOf(parte * 100.0 / total).setScale(1, RoundingMode.HALF_UP).doubleValue();
    }

    private AcaoControleVetorial planejada(UUID id) {
        AcaoControleVetorial a = acao(id);
        if (a.getSituacao() != SituacaoAcaoControleVetorial.PLANEJADA) {
            throw new ResourceUnprocessableEntityException("A ação já foi " + (a.getSituacao() == SituacaoAcaoControleVetorial.REALIZADA ? "realizada." : "cancelada."));
        }
        return a;
    }

    private AcaoControleVetorial acao(UUID id) {
        return acaoRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar a ação de controle " + id + " em nossos registros."));
    }

    private FocoVetorial foco(UUID id) {
        return focoRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar o foco " + id + " em nossos registros."));
    }

    private Microarea microarea(UUID id) {
        return microareaRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar a microárea " + id + " em nossos registros."));
    }

    static String endereco(Imovel i) {
        Endereco e = i.getEndereco();
        if (e == null) {
            return "";
        }
        return Stream.of(Stream.of(e.getLogradouro(), e.getNumeroLogradouro()).filter(s -> s != null && !s.isBlank()).collect(Collectors.joining(", ")),
                        e.getComplemento(), e.getBairro())
                .filter(s -> s != null && !s.isBlank()).collect(Collectors.joining(" · "));
    }

    private static ImovelVetorialDto imovelDto(Imovel i, VistoriaVetorial ultima, boolean pendente) {
        Microarea m = i.getMicroarea();
        return new ImovelVetorialDto(i.getUuid(), i.getTipo(), endereco(i), m.getUuid(), m.getCodigo(), m.getArea().getUuid(), m.getArea().getNome(),
                i.getLatitude(), i.getLongitude(), ultima != null ? ultima.getData() : null, ultima != null ? ultima.getResultado() : null, pendente);
    }

    private VistoriaVetorialDto dto(VistoriaVetorial v) {
        Imovel i = v.getImovel();
        int total = v.getDepositosA1() + v.getDepositosA2() + v.getDepositosB() + v.getDepositosC() + v.getDepositosD1() + v.getDepositosD2() + v.getDepositosE();
        return new VistoriaVetorialDto(v.getUuid(), i.getUuid(), endereco(i), i.getMicroarea().getCodigo(), i.getMicroarea().getArea().getNome(), v.getData(),
                v.getCicloAno(), v.getCicloNumero(), v.getAgente().getMatricula(), v.getAgente().getNome(), v.getTipo(), v.getResultado(), total,
                v.getDepositosA1(), v.getDepositosA2(), v.getDepositosB(), v.getDepositosC(), v.getDepositosD1(), v.getDepositosD2(), v.getDepositosE(),
                v.getDepositosEliminados(), v.getDepositosTratados(), v.getLarvicida(), v.getLarvicidaGramas(),
                v.getAcao() != null ? v.getAcao().getUuid() : null, v.getObservacao(), v.getFocos().stream().map(ControleVetorialService::dto).toList(),
                v.getRegistradoEm());
    }

    private static FocoVetorialDto dto(FocoVetorial f) {
        Imovel i = f.getVistoria().getImovel();
        return new FocoVetorialDto(f.getUuid(), f.getVistoria().getUuid(), i.getUuid(), endereco(i), i.getMicroarea().getCodigo(),
                i.getMicroarea().getArea().getNome(), i.getLatitude(), i.getLongitude(), f.getVistoria().getData(), f.getTipoDeposito(), f.getFase(),
                f.getEspecieSuspeita(), f.isAmostraColetada(), f.getResultadoLaboratorio(), f.getResultadoEm(), f.getSituacao(), f.getEncerradoEm(),
                f.getObservacao(), conta(f));
    }

    private AcaoControleVetorialDto dto(AcaoControleVetorial a) {
        List<Microarea> ms = new ArrayList<>(a.getMicroareas());
        ms.sort(Comparator.comparing(Microarea::getCodigo));
        return new AcaoControleVetorialDto(a.getUuid(), a.getTipo(), a.getDescricao(),
                ms.stream().map(m -> m.getArea().getNome() + " · " + m.getCodigo()).toList(), ms.stream().map(Microarea::getUuid).toList(),
                a.getDataPrevista(), a.getDataRealizada(), a.getResponsavel(), a.getImoveisTrabalhados(), vistoriaRepository.findByAcao_Uuid(a.getUuid()).size(),
                a.getNotificacao() != null ? a.getNotificacao().getUuid() : null, a.getNotificacao() != null ? a.getNotificacao().getAgravo().getNome() : null,
                a.getSurto() != null ? a.getSurto().getUuid() : null, a.getSurto() != null ? a.getSurto().getNome() : null, a.getSituacao(), a.getResultado(),
                a.getMotivoCancelamento(), a.getRegistradoEm());
    }

    private static int zero(Integer n) {
        return n == null ? 0 : n;
    }

    private static String texto(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }
}
