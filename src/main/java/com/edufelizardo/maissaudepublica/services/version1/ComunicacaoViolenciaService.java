package com.edufelizardo.maissaudepublica.services.version1;

import com.edufelizardo.maissaudepublica.config.ContextoAuditoria;
import com.edufelizardo.maissaudepublica.config.UsuarioAutenticado;
import com.edufelizardo.maissaudepublica.exceptions.ResourceBadRequestException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceForbiddenException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceNotFoundException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceUnprocessableEntityException;
import com.edufelizardo.maissaudepublica.models.EncaminhamentoIntersetorial;
import com.edufelizardo.maissaudepublica.models.InstituicaoExterna;
import com.edufelizardo.maissaudepublica.models.Notificacao;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.ComunicacaoViolenciaRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.EncaminhamentoIntersetorialRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.ComunicacaoViolenciaDtos;
import com.edufelizardo.maissaudepublica.models.enuns.DominioOrigemEncaminhamento;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoEncaminhamentoIntersetorial;
import com.edufelizardo.maissaudepublica.models.enuns.Sexo;
import com.edufelizardo.maissaudepublica.models.enuns.TipoInstituicaoExterna;
import com.edufelizardo.maissaudepublica.repositories.EncaminhamentoIntersetorialRepository;
import com.edufelizardo.maissaudepublica.repositories.NotificacaoRepository;
import com.edufelizardo.maissaudepublica.repositories.ProfissionalRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * A comunicação obrigatória da violência notificada, fatia V3c da Vigilância (ADR-0201): pela idade e pelo sexo da vítima e pela
 * ficha, o destino que a lei exige (Conselho Tutelar, ECA arts. 13 e 245; conselho da pessoa idosa, Ministério Público ou autoridade
 * policial, Estatuto da Pessoa Idosa art. 19; autoridade policial, Lei 10.778/2003 alterada pela Lei 13.931/2019), em 24 horas. A
 * rede sugere e o profissional registra a comunicação como encaminhamento intersetorial (ADR-0154), com a Vigilância como origem.
 */
@Service
public class ComunicacaoViolenciaService {

    static final String AGRAVO = "VIOLENCIA_INTERPESSOAL_AUTOPROVOCADA";
    static final Duration PRAZO = Duration.ofHours(24);
    private static final int RETORNO_PADRAO_DIAS = 30;
    private static final ObjectMapper JSON = new ObjectMapper();
    private static final ZoneId ZONA = ZoneId.of("America/Sao_Paulo");
    private static final DateTimeFormatter DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    /** O que a lei exige para a vítima: o destino, os tipos de instituição aceitos e a base legal. */
    record Obrigacao(String destino, Set<TipoInstituicaoExterna> tipos, String baseLegal) {
    }

    @Autowired private NotificacaoRepository notificacaoRepository;
    @Autowired private EncaminhamentoIntersetorialRepository encaminhamentoRepository;
    @Autowired private EncaminhamentoIntersetorialService encaminhamentoService;
    @Autowired private InstituicaoExternaService instituicaoService;
    @Autowired private ProfissionalRepository profissionalRepository;
    @Autowired private ControleDeAcesso controleDeAcesso;
    @Autowired private AutorizacaoService autorizacaoService;
    @Autowired private EscopoDosIndicadores escopo;

    /**
     * A obrigação da notificação: só no agravo de violência. Menor de 18 anos, inclusive lesão autoprovocada: Conselho Tutelar. A
     * partir de 60 anos, fora a autoprovocada: conselho de direitos, Ministério Público ou segurança pública. Mulher de 18 a 59 anos,
     * fora a autoprovocada: segurança pública. A idade é a da data dos primeiros sintomas (a ocorrência) ou da notificação; sem a data
     * de nascimento, não se calcula.
     */
    static Optional<Obrigacao> obrigacao(Notificacao n) {
        if (!AGRAVO.equals(n.getAgravo().getCodigo()) || n.getPaciente().getDataNascimento() == null) {
            return Optional.empty();
        }
        LocalDate ocorrencia = n.getDataPrimeirosSintomas() != null ? n.getDataPrimeirosSintomas() : n.getDataNotificacao();
        int idade = Period.between(n.getPaciente().getDataNascimento(), ocorrencia).getYears();
        boolean autoprovocada = autoprovocada(n.getRespostas());
        if (idade < 18) {
            return Optional.of(new Obrigacao("Conselho Tutelar", Set.of(TipoInstituicaoExterna.CONSELHO_TUTELAR), "ECA, arts. 13 e 245"));
        }
        if (autoprovocada) {
            return Optional.empty();
        }
        if (idade >= 60) {
            return Optional.of(new Obrigacao("Conselho da pessoa idosa, Ministério Público ou autoridade policial", Set.of(TipoInstituicaoExterna.CONSELHO_DE_DIREITOS,
                    TipoInstituicaoExterna.MINISTERIO_PUBLICO, TipoInstituicaoExterna.SEGURANCA_PUBLICA), "Estatuto da Pessoa Idosa, art. 19"));
        }
        if (n.getPaciente().getSexo() == Sexo.FEMININO) {
            return Optional.of(new Obrigacao("Autoridade policial", Set.of(TipoInstituicaoExterna.SEGURANCA_PUBLICA),
                    "Lei 10.778/2003, alterada pela Lei 13.931/2019"));
        }
        return Optional.empty();
    }

    /** Lesão autoprovocada pela ficha: o campo sim/não ou o tipo "Autoprovocada". */
    static boolean autoprovocada(String respostas) {
        if (respostas == null || respostas.isBlank()) {
            return false;
        }
        try {
            Map<String, Object> r = JSON.readValue(respostas, new TypeReference<>() {
            });
            return Boolean.TRUE.equals(r.get("lesaoAutoprovocada")) || "Autoprovocada".equals(r.get("tipoViolencia"));
        } catch (Exception e) {
            return false;
        }
    }

    // ── Consulta e registro ────────────────────────────────────────────────────────────────────────

    /** A comunicação da notificação: obrigatória ou não, o prazo, a situação e os encaminhamentos. Com o acesso ao detalhe da notificação. */
    @Transactional(readOnly = true)
    public ComunicacaoViolenciaDtos.Comunicacao comunicacao(UUID notificacaoId) {
        Notificacao n = notificacaoComAcesso(notificacaoId);
        return dto(n, encaminhamentoRepository.findDaOrigem(DominioOrigemEncaminhamento.VIGILANCIA, n.getUuid()));
    }

    /**
     * Registra a comunicação: só onde há obrigação (422) e a uma instituição de tipo aceito (422); as regras do encaminhamento são as da
     * ADR-0154 (inativa 422, duplicada em aberto 409). O motivo leva só o mínimo: o tipo de violência, a data e a unidade.
     */
    @Transactional
    public ComunicacaoViolenciaDtos.Comunicacao comunicar(UUID notificacaoId, ComunicacaoViolenciaRequestDto dto) {
        Notificacao n = notificacaoComAcesso(notificacaoId);
        Obrigacao o = obrigacao(n).orElseThrow(() -> new ResourceUnprocessableEntityException("Esta notificação não tem comunicação obrigatória."));
        InstituicaoExterna i = instituicaoService.instituicao(dto.getInstituicaoId());
        if (!o.tipos().contains(i.getTipo())) {
            throw new ResourceUnprocessableEntityException("A comunicação desta notificação vai a: " + o.destino() + ".");
        }
        LocalDate enviado = dto.getEnviadoEm() != null ? dto.getEnviadoEm() : LocalDate.now();
        if (enviado.isBefore(n.getDataNotificacao())) {
            throw new ResourceBadRequestException("A comunicação não pode ser antes da notificação.");
        }
        EncaminhamentoIntersetorialRequestDto e = new EncaminhamentoIntersetorialRequestDto();
        e.setInstituicaoId(i.getUuid());
        e.setEnviadoEm(enviado);
        e.setRetornoEsperadoEm(dto.getRetornoEsperadoEm() != null ? dto.getRetornoEsperadoEm() : enviado.plusDays(RETORNO_PADRAO_DIAS));
        e.setMotivo("Comunicação obrigatória de violência (" + o.baseLegal() + "): " + tipoViolencia(n.getRespostas()) + ", notificada em "
                + DATA.format(n.getDataNotificacao()) + " por " + n.getUnidade().getNome() + ".");
        String cpf = UsuarioAutenticado.cpf();
        var profissional = cpf != null ? profissionalRepository.findByCpfAndAtivoTrue(cpf).orElse(n.getProfissional()) : n.getProfissional();
        encaminhamentoService.encaminhar(DominioOrigemEncaminhamento.VIGILANCIA, n.getUuid(), n.getPaciente(), n.getUnidade(), profissional, e);
        ContextoAuditoria.paciente(n.getPaciente().getUuid());
        ContextoAuditoria.registro(n.getUuid());
        return dto(n, encaminhamentoRepository.findDaOrigem(DominioOrigemEncaminhamento.VIGILANCIA, n.getUuid()));
    }

    /** Desfaz a comunicação registrada por engano (em aberto, 422; de outra notificação, 404). */
    @Transactional
    public ComunicacaoViolenciaDtos.Comunicacao cancelar(UUID notificacaoId, UUID encaminhamentoId, String motivo) {
        Notificacao n = notificacaoComAcesso(notificacaoId);
        EncaminhamentoIntersetorial e = encaminhamentoService.daOrigem(encaminhamentoId, DominioOrigemEncaminhamento.VIGILANCIA, n.getUuid());
        encaminhamentoService.cancelar(e, motivo);
        ContextoAuditoria.paciente(n.getPaciente().getUuid());
        ContextoAuditoria.registro(n.getUuid());
        return dto(n, encaminhamentoRepository.findDaOrigem(DominioOrigemEncaminhamento.VIGILANCIA, n.getUuid()));
    }

    /** As comunicações obrigatórias ainda não feitas nas unidades em que se investiga (o agravo é sensível), das vencidas para as outras. */
    @Transactional(readOnly = true)
    public List<ComunicacaoViolenciaDtos.Pendente> pendentes() {
        controleDeAcesso.exigirAlguma(VigilanciaService.INVESTIGAR);
        Optional<Set<UUID>> visiveis = controleDeAcesso.unidadesVisiveis(VigilanciaService.INVESTIGAR);
        List<Notificacao> todas = notificacaoRepository.findDoAgravo(AGRAVO).stream()
                .filter(n -> visiveis.isEmpty() || visiveis.get().contains(n.getUnidade().getUuid())).toList();
        Map<UUID, List<EncaminhamentoIntersetorial>> porOrigem = encaminhamentos(todas);
        Instant agora = Instant.now();
        List<ComunicacaoViolenciaDtos.Pendente> lista = new ArrayList<>();
        for (Notificacao n : todas) {
            Optional<Obrigacao> o = obrigacao(n);
            if (o.isEmpty() || cumprida(o.get(), porOrigem.getOrDefault(n.getUuid(), List.of())) != null) {
                continue;
            }
            Instant prazo = n.getRegistradaEm().plus(PRAZO);
            lista.add(new ComunicacaoViolenciaDtos.Pendente(n.getUuid(), n.getPaciente().getNome(), n.getUnidade().getNome(), n.getDataNotificacao(),
                    o.get().destino(), prazo, prazo.isBefore(agora)));
        }
        lista.sort(Comparator.comparing(ComunicacaoViolenciaDtos.Pendente::prazo));
        return lista;
    }

    /** Para o relatório Vigilância do #19: as obrigações das notificações do período no escopo, por destino. */
    @Transactional(readOnly = true)
    public List<ComunicacaoViolenciaDtos.Resumo> resumo(LocalDate de, LocalDate ate, UUID unidadeId) {
        EscopoDosIndicadores.Escopo e = escopo.de(unidadeId);
        List<Notificacao> todas = notificacaoRepository.findDoAgravo(AGRAVO).stream()
                .filter(n -> !n.getDataNotificacao().isBefore(de) && !n.getDataNotificacao().isAfter(ate))
                .filter(n -> e.unidades() == null || e.contem(n.getUnidade().getUuid())).toList();
        Map<UUID, List<EncaminhamentoIntersetorial>> porOrigem = encaminhamentos(todas);
        Map<String, long[]> porDestino = new java.util.TreeMap<>();
        for (Notificacao n : todas) {
            Optional<Obrigacao> o = obrigacao(n);
            if (o.isEmpty()) {
                continue;
            }
            long[] c = porDestino.computeIfAbsent(o.get().destino(), k -> new long[3]);
            c[0]++;
            EncaminhamentoIntersetorial feita = cumprida(o.get(), porOrigem.getOrDefault(n.getUuid(), List.of()));
            if (feita != null) {
                c[1]++;
                if (!feita.getRegistradoEm().isAfter(n.getRegistradaEm().plus(PRAZO))) {
                    c[2]++;
                }
            }
        }
        return porDestino.entrySet().stream().map(x -> new ComunicacaoViolenciaDtos.Resumo(x.getKey(), x.getValue()[0], x.getValue()[1], x.getValue()[2])).toList();
    }

    // ── Apoio ──────────────────────────────────────────────────────────────────────────────────────

    private Map<UUID, List<EncaminhamentoIntersetorial>> encaminhamentos(List<Notificacao> notificacoes) {
        if (notificacoes.isEmpty()) {
            return Map.of();
        }
        return encaminhamentoRepository.findDasOrigens(DominioOrigemEncaminhamento.VIGILANCIA, notificacoes.stream().map(Notificacao::getUuid).toList())
                .stream().collect(Collectors.groupingBy(EncaminhamentoIntersetorial::getOrigemId));
    }

    /** O primeiro encaminhamento não cancelado a um tipo aceito: a comunicação feita. */
    private static EncaminhamentoIntersetorial cumprida(Obrigacao o, List<EncaminhamentoIntersetorial> lista) {
        return lista.stream().filter(e -> e.getSituacao() != SituacaoEncaminhamentoIntersetorial.CANCELADO && o.tipos().contains(e.getInstituicao().getTipo()))
                .min(Comparator.comparing(EncaminhamentoIntersetorial::getRegistradoEm)).orElse(null);
    }

    /** O acesso ao detalhe da notificação (o mesmo da V1): visível pela unidade e, no agravo sensível, quem investiga ou quem notificou. */
    private Notificacao notificacaoComAcesso(UUID id) {
        Notificacao n = notificacaoRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar a notificação " + id + " em nossos registros."));
        controleDeAcesso.exigirVisivel(n.getUnidade(), VigilanciaService.NOTIFICAR, VigilanciaService.INVESTIGAR);
        if (n.getAgravo().isSensivel() && controleDeAcesso.ativo()) {
            String cpf = UsuarioAutenticado.cpf();
            boolean pode = cpf != null && (cpf.equals(n.getRegistradoPorCpf())
                    || autorizacaoService.tem(cpf, VigilanciaService.INVESTIGAR, n.getUnidade().getUuid()));
            if (!pode) {
                throw new ResourceForbiddenException("Notificação de agravo sensível: só a vigilância e quem notificou comunicam.");
            }
        }
        return n;
    }

    private static String tipoViolencia(String respostas) {
        try {
            Object t = respostas == null ? null : JSON.readValue(respostas, new TypeReference<Map<String, Object>>() {
            }).get("tipoViolencia");
            return t == null ? "tipo não informado" : t.toString().toLowerCase();
        } catch (Exception e) {
            return "tipo não informado";
        }
    }

    private ComunicacaoViolenciaDtos.Comunicacao dto(Notificacao n, List<EncaminhamentoIntersetorial> encaminhamentos) {
        Optional<Obrigacao> o = obrigacao(n);
        LocalDate hoje = LocalDate.now();
        var lista = encaminhamentos.stream().map(e -> EncaminhamentoIntersetorialService.dto(e, hoje)).toList();
        if (o.isEmpty()) {
            return new ComunicacaoViolenciaDtos.Comunicacao(false, null, List.of(), null, null, "NAO_SE_APLICA", null, null, lista);
        }
        Instant prazo = n.getRegistradaEm().plus(PRAZO);
        EncaminhamentoIntersetorial feita = cumprida(o.get(), encaminhamentos);
        String situacao = feita != null ? "FEITA" : prazo.isBefore(Instant.now()) ? "VENCIDA" : "PENDENTE";
        return new ComunicacaoViolenciaDtos.Comunicacao(true, o.get().destino(), o.get().tipos().stream().map(Enum::name).sorted().toList(), o.get().baseLegal(),
                prazo, situacao, feita != null ? feita.getRegistradoEm() : null, feita != null ? !feita.getRegistradoEm().isAfter(prazo) : null, lista);
    }

    static LocalDate dia(Instant i) {
        return LocalDate.ofInstant(i, ZONA);
    }
}
