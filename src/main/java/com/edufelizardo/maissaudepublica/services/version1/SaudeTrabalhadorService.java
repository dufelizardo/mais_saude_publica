package com.edufelizardo.maissaudepublica.services.version1;

import com.edufelizardo.maissaudepublica.config.ContextoAuditoria;
import com.edufelizardo.maissaudepublica.config.UsuarioAutenticado;
import com.edufelizardo.maissaudepublica.exceptions.ResourceBadRequestException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceConflictException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceForbiddenException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceNotFoundException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceUnprocessableEntityException;
import com.edufelizardo.maissaudepublica.models.AcidenteTrabalho;
import com.edufelizardo.maissaudepublica.models.EstabelecimentoRegulado;
import com.edufelizardo.maissaudepublica.models.EventoTrabalho;
import com.edufelizardo.maissaudepublica.models.Notificacao;
import com.edufelizardo.maissaudepublica.models.Paciente;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.AcompanhamentoSorologicoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.EventoTrabalhoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.SaudeTrabalhadorDtos;
import com.edufelizardo.maissaudepublica.models.enuns.EtapaAcompanhamentoSorologico;
import com.edufelizardo.maissaudepublica.models.enuns.NexoTrabalho;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoNotificacao;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoRiscoOcupacional;
import com.edufelizardo.maissaudepublica.repositories.InvestigacaoTrabalhoRepository;
import com.edufelizardo.maissaudepublica.repositories.MedidaTrabalhoRepository;
import com.edufelizardo.maissaudepublica.repositories.RiscoOcupacionalRepository;
import com.edufelizardo.maissaudepublica.repositories.AcidenteTrabalhoRepository;
import com.edufelizardo.maissaudepublica.repositories.EstabelecimentoReguladoRepository;
import com.edufelizardo.maissaudepublica.repositories.EventoTrabalhoRepository;
import com.edufelizardo.maissaudepublica.repositories.NotificacaoRepository;
import com.edufelizardo.maissaudepublica.repositories.PacienteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Saúde do Trabalhador, fatia V5a da Vigilância (ADR-0207, desenho na ADR-0110): o bloco do trabalho na notificação, o
 * acompanhamento sorológico do acidente com material biológico e a ligação ao acidente do servidor no RH. A vigilância
 * notifica e investiga; a CAT e o afastamento continuam com o RH.
 */
@Service
public class SaudeTrabalhadorService {

    public static final String ACIDENTE_GRAVE = "ACIDENTE_TRABALHO_GRAVE";
    public static final String MATERIAL_BIOLOGICO = "ACIDENTE_TRABALHO_MATERIAL_BIOLOGICO";
    static final Set<String> ACIDENTES = Set.of(ACIDENTE_GRAVE, MATERIAL_BIOLOGICO);
    /** Antecedência com que a etapa do acompanhamento entra nas pendências. */
    static final int AVISO_ACOMPANHAMENTO_DIAS = 7;

    @Autowired
    private EventoTrabalhoRepository eventoRepository;

    @Autowired
    private NotificacaoRepository notificacaoRepository;

    @Autowired
    private EstabelecimentoReguladoRepository estabelecimentoRepository;

    @Autowired
    private AcidenteTrabalhoRepository acidenteRepository;

    @Autowired
    private PacienteRepository pacienteRepository;

    @Autowired
    private ControleDeAcesso controleDeAcesso;

    @Autowired
    private AutorizacaoService autorizacaoService;

    /** V5b (ADR-0208): a investigação, as medidas e os riscos. */
    @Autowired
    private InvestigacaoTrabalhoService investigacaoService;

    @Autowired
    private InvestigacaoTrabalhoRepository investigacaoRepository;

    @Autowired
    private MedidaTrabalhoRepository medidaRepository;

    @Autowired
    private RiscoOcupacionalRepository riscoRepository;

    @Autowired
    private EscopoDosIndicadores escopo;

    // ── Na notificação ─────────────────────────────────────────────────────────────────────────────

    /**
     * Confere o bloco do trabalho antes de gravar a notificação: obrigatório no agravo relacionado ao trabalho, opcional nos
     * demais (a intoxicação por agrotóxico na lavoura, por exemplo). Devolve o evento ainda sem a notificação.
     */
    EventoTrabalho preparar(Notificacao n, EventoTrabalhoRequestDto dto) {
        if (dto == null) {
            if (n.getAgravo().isRelacionadoAoTrabalho()) {
                throw new ResourceBadRequestException("A notificação de " + n.getAgravo().getNome()
                        + " pede o bloco do trabalho: a ocupação e a situação no mercado de trabalho.");
            }
            return null;
        }
        EventoTrabalho e = new EventoTrabalho();
        preencher(e, n, dto);
        return e;
    }

    void gravar(EventoTrabalho e, Notificacao n) {
        if (e == null) {
            return;
        }
        e.setNotificacao(n);
        e.setRegistradoEm(Instant.now());
        e.setRegistradoPorCpf(UsuarioAutenticado.cpf());
        eventoRepository.save(e);
    }

    /** O bloco do trabalho para o detalhe da notificação; nulo sem bloco. */
    SaudeTrabalhadorDtos.Trabalho dto(Notificacao n) {
        return eventoRepository.findByNotificacao_Uuid(n.getUuid()).map(e -> dto(e, n, LocalDate.now())).orElse(null);
    }

    // ── Pela investigação ──────────────────────────────────────────────────────────────────────────

    /** Completa ou corrige o bloco do trabalho (o notificante nem sempre sabe o empregador); só quem investiga, antes do encerramento. */
    @Transactional
    public void atualizar(UUID notificacaoId, EventoTrabalhoRequestDto dto) {
        Notificacao n = buscar(notificacaoId);
        controleDeAcesso.exigir(VigilanciaService.INVESTIGAR, n.getUnidade());
        auditar(n);
        if (n.getSituacao() == SituacaoNotificacao.ENCERRADA) {
            throw new ResourceUnprocessableEntityException("A notificação foi encerrada e não muda mais.");
        }
        EventoTrabalho e = eventoRepository.findByNotificacao_Uuid(n.getUuid()).orElse(null);
        boolean novo = e == null;
        if (novo) {
            e = new EventoTrabalho();
            e.setNotificacao(n);
            e.setRegistradoEm(Instant.now());
            e.setRegistradoPorCpf(UsuarioAutenticado.cpf());
        }
        preencher(e, n, dto);
        e.setAtualizadoEm(Instant.now());
        e.setAtualizadoPorCpf(UsuarioAutenticado.cpf());
        eventoRepository.save(e);
        ContextoAuditoria.detalhe(novo ? "Bloco do trabalho incluído na investigação." : "Bloco do trabalho atualizado.");
    }

    /** Registra uma etapa do acompanhamento sorológico do acidente com material biológico; quem notifica ou investiga na unidade. */
    @Transactional
    public void registrarAcompanhamento(UUID notificacaoId, AcompanhamentoSorologicoRequestDto dto) {
        Notificacao n = buscar(notificacaoId);
        controleDeAcesso.exigirVisivel(n.getUnidade(), VigilanciaService.NOTIFICAR, VigilanciaService.INVESTIGAR);
        auditar(n);
        if (!MATERIAL_BIOLOGICO.equals(n.getAgravo().getCodigo())) {
            throw new ResourceUnprocessableEntityException("O acompanhamento sorológico é do acidente com material biológico.");
        }
        if (n.getSituacao() == SituacaoNotificacao.ENCERRADA) {
            throw new ResourceUnprocessableEntityException("A notificação foi encerrada e não muda mais.");
        }
        EventoTrabalho e = eventoRepository.findByNotificacao_Uuid(n.getUuid()).orElseThrow(() -> new ResourceUnprocessableEntityException(
                "A notificação não tem o bloco do trabalho."));
        LocalDate acidente = n.getDataPrimeirosSintomas();
        if (dto.getRealizadoEm().isAfter(LocalDate.now())) {
            throw new ResourceBadRequestException("O acompanhamento não pode ser no futuro.");
        }
        if (acidente != null && dto.getRealizadoEm().isBefore(acidente)) {
            throw new ResourceBadRequestException("O acompanhamento não pode ser antes do acidente.");
        }
        switch (dto.getEtapa()) {
            case SEIS_SEMANAS -> e.setAcompanhamentoSeisSemanas(dto.getRealizadoEm());
            case TRES_MESES -> e.setAcompanhamentoTresMeses(dto.getRealizadoEm());
            case SEIS_MESES -> e.setAcompanhamentoSeisMeses(dto.getRealizadoEm());
        }
        e.setAtualizadoEm(Instant.now());
        e.setAtualizadoPorCpf(UsuarioAutenticado.cpf());
        eventoRepository.save(e);
        ContextoAuditoria.detalhe("Acompanhamento sorológico: " + dto.getEtapa() + " em " + dto.getRealizadoEm() + ".");
    }

    // ── Painel ─────────────────────────────────────────────────────────────────────────────────────

    /** As notificações relacionadas ao trabalho do escopo e as etapas do acompanhamento sorológico a fazer. */
    @Transactional(readOnly = true)
    public SaudeTrabalhadorDtos.Painel painel(UUID unidadeId) {
        controleDeAcesso.exigirAlguma(VigilanciaService.NOTIFICAR, VigilanciaService.INVESTIGAR);
        LocalDate hoje = LocalDate.now();
        List<EventoTrabalho> eventos = controleDeAcesso.filtrar(eventoRepository.findTodos().stream(),
                        e -> java.util.stream.Stream.of(e.getNotificacao().getUnidade()), VigilanciaService.NOTIFICAR, VigilanciaService.INVESTIGAR)
                .filter(e -> podeVerConteudo(e.getNotificacao()))
                .filter(e -> unidadeId == null || e.getNotificacao().getUnidade().getUuid().equals(unidadeId))
                .toList();
        List<SaudeTrabalhadorDtos.NotificacaoTrabalho> lista = eventos.stream().map(e -> {
            Notificacao n = e.getNotificacao();
            return new SaudeTrabalhadorDtos.NotificacaoTrabalho(n.getUuid(), n.getPaciente().getUuid(), n.getPaciente().getNome(),
                    n.getAgravo().getCodigo(), n.getAgravo().getNome(), n.getUnidade().getNome(), n.getDataNotificacao(), n.getDataPrimeirosSintomas(),
                    n.getSituacao(), e.getOcupacaoCbo() + " · " + e.getOcupacaoDescricao(), e.getSituacaoTrabalho(), e.getEmpregadorNome(),
                    e.getEmpregadorCnae(), e.isObito(), e.getAcidenteTrabalho() != null);
        }).toList();
        List<SaudeTrabalhadorDtos.AcompanhamentoPendente> pendentes = new ArrayList<>();
        for (EventoTrabalho e : eventos) {
            Notificacao n = e.getNotificacao();
            if (!MATERIAL_BIOLOGICO.equals(n.getAgravo().getCodigo()) || n.getSituacao() == SituacaoNotificacao.ENCERRADA) {
                continue;
            }
            for (SaudeTrabalhadorDtos.Acompanhamento a : acompanhamentos(e, n, hoje)) {
                if (a.realizadoEm() == null && a.prevista() != null && !a.prevista().isAfter(hoje.plusDays(AVISO_ACOMPANHAMENTO_DIAS))) {
                    pendentes.add(new SaudeTrabalhadorDtos.AcompanhamentoPendente(n.getUuid(), n.getPaciente().getNome(), n.getUnidade().getNome(),
                            a.etapa(), a.prevista(), a.vencida()));
                }
            }
        }
        pendentes.sort(Comparator.comparing(SaudeTrabalhadorDtos.AcompanhamentoPendente::prevista));
        long acidentes = eventos.stream().filter(e -> ACIDENTES.contains(e.getNotificacao().getAgravo().getCodigo())).count();
        long obitos = eventos.stream().filter(EventoTrabalho::isObito).count();
        long biologico = eventos.stream().filter(e -> MATERIAL_BIOLOGICO.equals(e.getNotificacao().getAgravo().getCodigo())).count();
        // V5b (ADR-0208): as medidas recomendadas com o prazo vencido, nas notificações do escopo.
        Set<UUID> doEscopo = eventos.stream().map(e -> e.getNotificacao().getUuid()).collect(java.util.stream.Collectors.toSet());
        List<SaudeTrabalhadorDtos.MedidaVencida> vencidas = medidaRepository.findTodas().stream()
                .filter(m -> doEscopo.contains(m.getInvestigacao().getNotificacao().getUuid()) && InvestigacaoTrabalhoService.vencida(m, hoje))
                .sorted(Comparator.comparing(com.edufelizardo.maissaudepublica.models.MedidaTrabalho::getPrazo))
                .map(m -> new SaudeTrabalhadorDtos.MedidaVencida(m.getInvestigacao().getNotificacao().getUuid(),
                        m.getInvestigacao().getNotificacao().getPaciente().getNome(), m.getInvestigacao().getNotificacao().getUnidade().getNome(),
                        m.getTipo(), m.getDescricao(), m.getPrazo())).toList();
        return new SaudeTrabalhadorDtos.Painel(eventos.size(), acidentes, obitos, biologico, lista, pendentes, vencidas);
    }

    /**
     * A semana da saúde do trabalhador para a Sala de Situação (ADR-0208): os acidentes de trabalho dos últimos 7 dias pela data do evento,
     * os óbitos, as investigações abertas, as medidas vencidas e os riscos ativos. Os alertas não levam o nome do trabalhador.
     */
    @Transactional(readOnly = true)
    public SaudeTrabalhadorDtos.Semana semana() {
        controleDeAcesso.exigirAlguma(VigilanciaService.NOTIFICAR, VigilanciaService.INVESTIGAR);
        LocalDate hoje = LocalDate.now();
        LocalDate desde = hoje.minusDays(6);
        List<EventoTrabalho> eventos = controleDeAcesso.filtrar(eventoRepository.findTodos().stream(),
                e -> java.util.stream.Stream.of(e.getNotificacao().getUnidade()), VigilanciaService.NOTIFICAR, VigilanciaService.INVESTIGAR).toList();
        List<SaudeTrabalhadorDtos.AlertaSemana> alertas = eventos.stream()
                .filter(e -> ACIDENTES.contains(e.getNotificacao().getAgravo().getCodigo()))
                .filter(e -> {
                    LocalDate d = dataEvento(e.getNotificacao());
                    return !d.isBefore(desde) && !d.isAfter(hoje);
                })
                .sorted(Comparator.comparing((EventoTrabalho e) -> !e.isObito()).thenComparing(e -> dataEvento(e.getNotificacao()), Comparator.reverseOrder()))
                .map(e -> new SaudeTrabalhadorDtos.AlertaSemana(e.getNotificacao().getAgravo().getNome(), e.getNotificacao().getUnidade().getNome(),
                        dataEvento(e.getNotificacao()), e.isObito(), e.getEmpregadorNome())).toList();
        Set<UUID> doEscopo = eventos.stream().map(e -> e.getNotificacao().getUuid()).collect(java.util.stream.Collectors.toSet());
        long abertas = investigacaoRepository.findTodas().stream().filter(i -> doEscopo.contains(i.getNotificacao().getUuid())
                && i.getNotificacao().getSituacao() != SituacaoNotificacao.ENCERRADA).count();
        long vencidas = medidaRepository.findTodas().stream().filter(m -> doEscopo.contains(m.getInvestigacao().getNotificacao().getUuid())
                && InvestigacaoTrabalhoService.vencida(m, hoje)).count();
        long riscos = riscoRepository.findTodos().stream().filter(r -> r.getSituacao() == SituacaoRiscoOcupacional.ATIVO).count();
        return new SaudeTrabalhadorDtos.Semana(alertas.size(), alertas.stream().filter(SaudeTrabalhadorDtos.AlertaSemana::obito).count(), abertas,
                vencidas, riscos, alertas);
    }

    /**
     * Os números do período para o relatório Vigilância do #19 (ADR-0208), pela data da notificação e no escopo dos indicadores. As de nexo
     * descartado saem das contagens do trabalho (só aparecem no total de descartadas).
     */
    @Transactional(readOnly = true)
    public SaudeTrabalhadorDtos.Resumo resumo(LocalDate de, LocalDate ate, UUID unidadeId) {
        EscopoDosIndicadores.Escopo esc = escopo.de(unidadeId);
        LocalDate hoje = LocalDate.now();
        java.util.Map<UUID, com.edufelizardo.maissaudepublica.models.InvestigacaoTrabalho> investigacoes = investigacaoRepository.findTodas().stream()
                .collect(java.util.stream.Collectors.toMap(i -> i.getNotificacao().getUuid(), i -> i));
        List<EventoTrabalho> doPeriodo = eventoRepository.findTodos().stream()
                .filter(e -> !e.getNotificacao().getDataNotificacao().isBefore(de) && !e.getNotificacao().getDataNotificacao().isAfter(ate))
                .filter(e -> esc.unidades() == null || esc.contem(e.getNotificacao().getUnidade().getUuid())).toList();
        long descartadas = doPeriodo.stream().filter(e -> nexo(investigacoes, e) == NexoTrabalho.DESCARTADO).count();
        List<EventoTrabalho> validos = doPeriodo.stream().filter(e -> nexo(investigacoes, e) != NexoTrabalho.DESCARTADO).toList();
        long concluido = doPeriodo.stream().filter(e -> nexo(investigacoes, e) != null && nexo(investigacoes, e) != NexoTrabalho.EM_ANALISE).count();
        long confirmado = validos.stream().filter(e -> nexo(investigacoes, e) == NexoTrabalho.CONFIRMADO).count();
        long obitos = validos.stream().filter(EventoTrabalho::isObito).count();
        long menores = validos.stream().filter(this::menor).count();
        long biologicoSaude = validos.stream().filter(e -> MATERIAL_BIOLOGICO.equals(e.getNotificacao().getAgravo().getCodigo()) && daSaude(e)).count();
        Set<UUID> ids = doPeriodo.stream().map(e -> e.getNotificacao().getUuid()).collect(java.util.stream.Collectors.toSet());
        long vencidas = medidaRepository.findTodas().stream().filter(m -> ids.contains(m.getInvestigacao().getNotificacao().getUuid())
                && InvestigacaoTrabalhoService.vencida(m, hoje)).count();
        java.util.Map<String, long[]> porAgravo = new java.util.TreeMap<>();
        java.util.Map<String, Long> porCnae = new java.util.TreeMap<>();
        java.util.Map<String, Long> porCbo = new java.util.TreeMap<>();
        for (EventoTrabalho e : validos) {
            long[] c = porAgravo.computeIfAbsent(e.getNotificacao().getAgravo().getNome(), k -> new long[4]);
            c[0]++;
            if (nexo(investigacoes, e) == NexoTrabalho.CONFIRMADO) {
                c[1]++;
            }
            if (e.isObito()) {
                c[2]++;
            }
            if (menor(e)) {
                c[3]++;
            }
            porCnae.merge(secaoCnae(e.getEmpregadorCnae()), 1L, Long::sum);
            porCbo.merge(grandeGrupoCbo(e.getOcupacaoCbo()), 1L, Long::sum);
        }
        return new SaudeTrabalhadorDtos.Resumo(validos.size(), concluido, confirmado, descartadas, obitos, menores, biologicoSaude, vencidas,
                porAgravo.entrySet().stream().map(x -> new SaudeTrabalhadorDtos.LinhaAgravo(x.getKey(), x.getValue()[0], x.getValue()[1], x.getValue()[2],
                        x.getValue()[3])).toList(),
                porCnae.entrySet().stream().map(x -> new SaudeTrabalhadorDtos.Contagem(x.getKey(), x.getValue())).toList(),
                porCbo.entrySet().stream().map(x -> new SaudeTrabalhadorDtos.Contagem(x.getKey(), x.getValue())).toList());
    }

    private static NexoTrabalho nexo(java.util.Map<UUID, com.edufelizardo.maissaudepublica.models.InvestigacaoTrabalho> investigacoes, EventoTrabalho e) {
        var i = investigacoes.get(e.getNotificacao().getUuid());
        return i == null ? null : i.getNexo();
    }

    private static LocalDate dataEvento(Notificacao n) {
        return n.getDataPrimeirosSintomas() != null ? n.getDataPrimeirosSintomas() : n.getDataNotificacao();
    }

    /** Menor de 18 anos na data do evento. */
    private boolean menor(EventoTrabalho e) {
        LocalDate nascimento = e.getNotificacao().getPaciente().getDataNascimento();
        return nascimento != null && java.time.Period.between(nascimento, dataEvento(e.getNotificacao())).getYears() < 18;
    }

    /** Trabalhador da saúde: empregador da seção Q do CNAE ou ocupação da saúde na CBO (22, 322, 515). */
    static boolean daSaude(EventoTrabalho e) {
        String cnae = e.getEmpregadorCnae();
        String cbo = e.getOcupacaoCbo();
        return "Q".equals(letraCnae(cnae)) || cbo.startsWith("22") || cbo.startsWith("322") || cbo.startsWith("515");
    }

    /** A seção do CNAE 2.0 pela divisão (dois primeiros dígitos). */
    static String letraCnae(String cnae) {
        if (cnae == null || cnae.length() < 2) {
            return null;
        }
        int d = Integer.parseInt(cnae.substring(0, 2));
        if (d <= 3) return "A";
        if (d <= 9) return "B";
        if (d <= 33) return "C";
        if (d == 35) return "D";
        if (d <= 39) return "E";
        if (d <= 43) return "F";
        if (d <= 47) return "G";
        if (d <= 53) return "H";
        if (d <= 56) return "I";
        if (d <= 63) return "J";
        if (d <= 66) return "K";
        if (d == 68) return "L";
        if (d <= 75) return "M";
        if (d <= 82) return "N";
        if (d == 84) return "O";
        if (d == 85) return "P";
        if (d <= 88) return "Q";
        if (d <= 93) return "R";
        if (d <= 96) return "S";
        if (d == 97) return "T";
        if (d == 99) return "U";
        return null;
    }

    static final java.util.Map<String, String> SECOES_CNAE = java.util.Map.ofEntries(
            java.util.Map.entry("A", "Agropecuária e pesca"), java.util.Map.entry("B", "Indústrias extrativas"),
            java.util.Map.entry("C", "Indústrias de transformação"), java.util.Map.entry("D", "Eletricidade e gás"),
            java.util.Map.entry("E", "Água, esgoto e resíduos"), java.util.Map.entry("F", "Construção"),
            java.util.Map.entry("G", "Comércio"), java.util.Map.entry("H", "Transporte e armazenagem"),
            java.util.Map.entry("I", "Alojamento e alimentação"), java.util.Map.entry("J", "Informação e comunicação"),
            java.util.Map.entry("K", "Atividades financeiras"), java.util.Map.entry("L", "Atividades imobiliárias"),
            java.util.Map.entry("M", "Atividades profissionais e técnicas"), java.util.Map.entry("N", "Atividades administrativas"),
            java.util.Map.entry("O", "Administração pública"), java.util.Map.entry("P", "Educação"),
            java.util.Map.entry("Q", "Saúde humana e serviços sociais"), java.util.Map.entry("R", "Artes, cultura e esporte"),
            java.util.Map.entry("S", "Outros serviços"), java.util.Map.entry("T", "Serviços domésticos"),
            java.util.Map.entry("U", "Organismos internacionais"));

    static String secaoCnae(String cnae) {
        String l = letraCnae(cnae);
        return l == null ? "Sem CNAE" : l + " · " + SECOES_CNAE.get(l);
    }

    static final String[] GRANDES_GRUPOS_CBO = {"Forças armadas, policiais e bombeiros militares", "Dirigentes e gerentes",
            "Profissionais das ciências e das artes", "Técnicos de nível médio", "Trabalhadores de serviços administrativos",
            "Trabalhadores dos serviços e vendedores", "Trabalhadores agropecuários, florestais e da pesca",
            "Trabalhadores da produção de bens e serviços industriais", "Trabalhadores da produção de bens e serviços industriais (contínuos)",
            "Trabalhadores de reparação e manutenção"};

    static String grandeGrupoCbo(String cbo) {
        int g = cbo.charAt(0) - '0';
        return g + " · " + GRANDES_GRUPOS_CBO[g];
    }

    /**
     * Os acidentes de servidores registrados no RH sem notificação na vigilância. O servidor não tem unidade no RH, então a
     * lista é da vigilância com acesso à rede inteira (como o CEREST municipal).
     */
    @Transactional(readOnly = true)
    public List<SaudeTrabalhadorDtos.AcidenteSemNotificacao> acidentesSemNotificacao() {
        controleDeAcesso.exigirAlguma(VigilanciaService.INVESTIGAR);
        if (controleDeAcesso.unidadesVisiveis(VigilanciaService.INVESTIGAR).isPresent()) {
            throw new ResourceForbiddenException("Os acidentes dos servidores são da rede inteira: só a vigilância com acesso a todas as unidades vê a lista.");
        }
        Set<UUID> notificados = eventoRepository.findAcidentesNotificados();
        return acidenteRepository.findAll().stream()
                .filter(a -> !notificados.contains(a.getUuid()))
                .sorted(Comparator.comparing(AcidenteTrabalho::getDataHora, Comparator.nullsLast(Comparator.reverseOrder())))
                .map(a -> new SaudeTrabalhadorDtos.AcidenteSemNotificacao(a.getUuid(), a.getProfissional().getMatricula(), a.getProfissional().getNome(),
                        a.getDataHora(), a.getDescricao(), a.isCatEmitida(), a.getDiasAfastamento(), pacienteDoCpf(a.getProfissional().getCpf())))
                .toList();
    }

    /** Para o RH: se o acidente do servidor já foi notificado à vigilância (sem o conteúdo, que é dado de saúde). */
    @Transactional(readOnly = true)
    public boolean notificado(UUID acidenteId) {
        return eventoRepository.existsByAcidenteTrabalho_Uuid(acidenteId);
    }

    // ── Regras ─────────────────────────────────────────────────────────────────────────────────────

    private void preencher(EventoTrabalho e, Notificacao n, EventoTrabalhoRequestDto dto) {
        String cbo = digitos(dto.getOcupacaoCbo());
        if (cbo.length() != 6) {
            throw new ResourceBadRequestException("A ocupação usa o código da CBO, de seis dígitos (ex.: 2235-05).");
        }
        e.setOcupacaoCbo(cbo);
        e.setOcupacaoDescricao(dto.getOcupacaoDescricao().trim());
        e.setSituacaoTrabalho(dto.getSituacaoTrabalho());
        String nome = texto(dto.getEmpregadorNome());
        if (texto(dto.getEmpregadorDocumento()) != null) {
            String documento = VigilanciaSanitariaService.documento(dto.getEmpregadorDocumento());
            EstabelecimentoRegulado est = estabelecimentoRepository.findByDocumento(documento).orElse(null);
            if (nome == null && est != null) {
                nome = est.getRazaoSocial();
            }
            if (nome == null) {
                throw new ResourceBadRequestException("Informe o nome do empregador.");
            }
            e.setEmpregadorDocumento(documento);
            e.setEstabelecimento(est);
        } else {
            e.setEmpregadorDocumento(null);
            e.setEstabelecimento(null);
        }
        e.setEmpregadorNome(nome);
        String cnae = texto(dto.getEmpregadorCnae()) == null ? null : digitos(dto.getEmpregadorCnae());
        if (cnae != null && cnae.length() != 7) {
            throw new ResourceBadRequestException("O CNAE tem sete dígitos (ex.: 8610-1/01).");
        }
        e.setEmpregadorCnae(cnae);
        e.setLocalEvento(texto(dto.getLocalEvento()));
        if ((dto.getLatitude() == null) != (dto.getLongitude() == null)) {
            throw new ResourceBadRequestException("Informe a latitude e a longitude do local do evento, ou nenhuma das duas.");
        }
        e.setLatitude(dto.getLatitude());
        e.setLongitude(dto.getLongitude());
        String codigo = n.getAgravo().getCodigo();
        boolean acidente = ACIDENTES.contains(codigo);
        boolean dadosDoAcidente = dto.getTipoAcidente() != null || dto.getHorasAposInicioJornada() != null || dto.getCat() != null
                || dto.getOutrosTrabalhadoresAtingidos() != null || Boolean.TRUE.equals(dto.getObito()) || dto.getAcidenteTrabalhoId() != null;
        if (!acidente && dadosDoAcidente) {
            throw new ResourceBadRequestException("O tipo, a jornada, a CAT, os atingidos, o óbito e o acidente do RH são só do acidente de trabalho.");
        }
        if (acidente && n.getDataPrimeirosSintomas() == null) {
            throw new ResourceBadRequestException("Informe a data do acidente (no campo dos primeiros sintomas).");
        }
        if (ACIDENTE_GRAVE.equals(codigo) && dto.getTipoAcidente() == null) {
            throw new ResourceBadRequestException("Informe o tipo do acidente: típico ou de trajeto.");
        }
        e.setTipoAcidente(dto.getTipoAcidente());
        e.setHorasAposInicioJornada(dto.getHorasAposInicioJornada());
        e.setCat(dto.getCat());
        e.setOutrosTrabalhadoresAtingidos(dto.getOutrosTrabalhadoresAtingidos());
        e.setObito(Boolean.TRUE.equals(dto.getObito()));
        e.setAcidenteTrabalho(dto.getAcidenteTrabalhoId() == null ? null : acidenteDoServidor(dto.getAcidenteTrabalhoId(), n.getPaciente(), e));
    }

    /** O acidente do RH: existe (404), não está em outra notificação (409) e é do mesmo CPF do paciente (422). */
    private AcidenteTrabalho acidenteDoServidor(UUID id, Paciente paciente, EventoTrabalho e) {
        AcidenteTrabalho a = acidenteRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar o acidente de trabalho " + id + " em nossos registros."));
        eventoRepository.findByAcidenteTrabalho_Uuid(id).filter(outro -> e.getUuid() == null || !outro.getUuid().equals(e.getUuid()))
                .ifPresent(outro -> {
                    throw new ResourceConflictException("O acidente do RH já está ligado a outra notificação.");
                });
        String cpfServidor = digitos(a.getProfissional().getCpf());
        if (cpfServidor.isEmpty() || !cpfServidor.equals(digitos(paciente.getCpf()))) {
            throw new ResourceUnprocessableEntityException("O acidente do RH é de outro trabalhador: o CPF do servidor não é o do paciente.");
        }
        return a;
    }

    private SaudeTrabalhadorDtos.Trabalho dto(EventoTrabalho e, Notificacao n, LocalDate hoje) {
        String codigo = n.getAgravo().getCodigo();
        boolean biologico = MATERIAL_BIOLOGICO.equals(codigo);
        AcidenteTrabalho a = e.getAcidenteTrabalho();
        return new SaudeTrabalhadorDtos.Trabalho(e.getUuid(), e.getOcupacaoCbo(), e.getOcupacaoDescricao(), e.getSituacaoTrabalho(),
                e.getEmpregadorDocumento(), e.getEmpregadorNome(), e.getEmpregadorCnae(),
                e.getEstabelecimento() != null ? e.getEstabelecimento().getUuid() : null, e.getLocalEvento(), e.getLatitude(), e.getLongitude(),
                e.getTipoAcidente(), e.getHorasAposInicioJornada(), e.getCat(), e.getOutrosTrabalhadoresAtingidos(), e.isObito(),
                a != null ? a.getUuid() : null, a != null ? a.getDataHora() : null, ACIDENTES.contains(codigo), biologico,
                biologico ? acompanhamentos(e, n, hoje) : List.of(), e.getAtualizadoEm(), investigacaoService.dto(n, hoje));
    }

    /** As três etapas, previstas a partir da data do acidente (6 semanas, 3 e 6 meses). */
    static List<SaudeTrabalhadorDtos.Acompanhamento> acompanhamentos(EventoTrabalho e, Notificacao n, LocalDate hoje) {
        LocalDate base = n.getDataPrimeirosSintomas();
        boolean aberta = n.getSituacao() != SituacaoNotificacao.ENCERRADA;
        List<SaudeTrabalhadorDtos.Acompanhamento> lista = new ArrayList<>();
        for (EtapaAcompanhamentoSorologico etapa : EtapaAcompanhamentoSorologico.values()) {
            LocalDate prevista = base == null ? null : switch (etapa) {
                case SEIS_SEMANAS -> base.plusWeeks(6);
                case TRES_MESES -> base.plusMonths(3);
                case SEIS_MESES -> base.plusMonths(6);
            };
            LocalDate realizado = switch (etapa) {
                case SEIS_SEMANAS -> e.getAcompanhamentoSeisSemanas();
                case TRES_MESES -> e.getAcompanhamentoTresMeses();
                case SEIS_MESES -> e.getAcompanhamentoSeisMeses();
            };
            lista.add(new SaudeTrabalhadorDtos.Acompanhamento(etapa, prevista, realizado,
                    aberta && realizado == null && prevista != null && prevista.isBefore(hoje)));
        }
        return lista;
    }

    private UUID pacienteDoCpf(String cpf) {
        String d = digitos(cpf);
        if (d.isEmpty()) {
            return null;
        }
        // O CPF do paciente pode estar só com os dígitos ou com a pontuação.
        List<Paciente> achados = new ArrayList<>(pacienteRepository.findByCpf(d));
        if (d.length() == 11) {
            achados.addAll(pacienteRepository.findByCpf(d.substring(0, 3) + "." + d.substring(3, 6) + "." + d.substring(6, 9) + "-" + d.substring(9)));
        }
        List<UUID> ids = achados.stream().filter(Paciente::isAtivo).map(Paciente::getUuid).distinct().toList();
        return ids.size() == 1 ? ids.get(0) : null;
    }

    private boolean podeVerConteudo(Notificacao n) {
        if (!n.getAgravo().isSensivel() || !controleDeAcesso.ativo()) {
            return true;
        }
        String cpf = UsuarioAutenticado.cpf();
        return cpf != null && (cpf.equals(n.getRegistradoPorCpf()) || autorizacaoService.tem(cpf, VigilanciaService.INVESTIGAR, n.getUnidade().getUuid()));
    }

    private Notificacao buscar(UUID id) {
        return notificacaoRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar uma notificação com o id " + id + " em nossos registros."));
    }

    private static void auditar(Notificacao n) {
        ContextoAuditoria.paciente(n.getPaciente().getUuid());
        ContextoAuditoria.registro(n.getUuid());
    }

    private static String digitos(String s) {
        return s == null ? "" : s.replaceAll("\\D", "");
    }

    private static String texto(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }
}
