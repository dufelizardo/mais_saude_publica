package com.edufelizardo.maissaudepublica.services.version1;

import com.edufelizardo.maissaudepublica.config.ContextoAuditoria;
import com.edufelizardo.maissaudepublica.config.UsuarioAutenticado;
import com.edufelizardo.maissaudepublica.exceptions.ResourceBadRequestException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceConflictException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceForbiddenException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceNotFoundException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceUnprocessableEntityException;
import com.edufelizardo.maissaudepublica.models.FrascoAberto;
import com.edufelizardo.maissaudepublica.models.Imunobiologico;
import com.edufelizardo.maissaudepublica.models.LoteImunobiologico;
import com.edufelizardo.maissaudepublica.models.Notificacao;
import com.edufelizardo.maissaudepublica.models.RegistroVacinacao;
import com.edufelizardo.maissaudepublica.models.SuspensaoLoteRede;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.DecisaoSuspensaoLoteRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.SuspensaoLoteRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.ImunizacaoRedeDtos;
import com.edufelizardo.maissaudepublica.models.enuns.CausalidadeEsavi;
import com.edufelizardo.maissaudepublica.models.enuns.ClassificacaoCasoEpidemiologico;
import com.edufelizardo.maissaudepublica.models.enuns.GravidadeEsavi;
import com.edufelizardo.maissaudepublica.models.enuns.MotivoEncerramentoFrasco;
import com.edufelizardo.maissaudepublica.models.enuns.MotivoPerdaImunobiologico;
import com.edufelizardo.maissaudepublica.models.enuns.MotivoSuspeitaLote;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoLoteImunobiologico;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoSuspensaoLote;
import com.edufelizardo.maissaudepublica.models.enuns.TipoMovimentacaoImunobiologico;
import com.edufelizardo.maissaudepublica.repositories.FrascoAbertoRepository;
import com.edufelizardo.maissaudepublica.repositories.ImunobiologicoRepository;
import com.edufelizardo.maissaudepublica.repositories.LoteImunobiologicoRepository;
import com.edufelizardo.maissaudepublica.repositories.NotificacaoRepository;
import com.edufelizardo.maissaudepublica.repositories.SuspensaoLoteRedeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

/**
 * O lote na rede, I6 da Imunização (ADR-0206). O lote da rede é o imunobiológico mais o número do lote (sem espaços e em maiúsculas),
 * que junta as doses com baixa no estoque e as de lote digitado. O alerta é calculado: dois ou mais ESAVI graves não descartados
 * notificados em 30 dias, ou um ESAVI encerrado com causalidade A2 (desvio de qualidade) no último ano. A coordenação da rede decide
 * suspender (todos os lotes com o número ficam sob suspeita) e depois liberar ou inutilizar.
 */
@Service
public class LoteRedeService {

    static final int JANELA_GRAVES_DIAS = 30;
    static final int MINIMO_GRAVES = 2;
    static final int JANELA_A2_DIAS = 365;

    @Autowired private SuspensaoLoteRedeRepository suspensaoRepository;
    @Autowired private LoteImunobiologicoRepository loteRepository;
    @Autowired private FrascoAbertoRepository frascoRepository;
    @Autowired private ImunobiologicoRepository imunobiologicoRepository;
    @Autowired private NotificacaoRepository notificacaoRepository;
    @Autowired private EstoqueVacinaService estoqueService;
    @Autowired private ControleDeAcesso controleDeAcesso;

    private record Chave(UUID imunobiologicoId, String numero) {
    }

    // ── Alertas ────────────────────────────────────────────────────────────────────────────────

    /** Os lotes com sinal de ESAVI e os suspensos na rede, com o estoque e a última suspensão. Só agregados, sem o paciente. */
    @Transactional(readOnly = true)
    public List<ImunizacaoRedeDtos.AlertaLote> alertas() {
        controleDeAcesso.exigirAlguma(ImunizacaoCatalogoService.GERENCIAR);
        LocalDate hoje = LocalDate.now();
        Instant limiteA2 = hoje.minusDays(JANELA_A2_DIAS).atStartOfDay(ZoneId.systemDefault()).toInstant();
        Map<Chave, Set<Notificacao>> graves = new LinkedHashMap<>();
        Map<Chave, Set<Notificacao>> a2 = new LinkedHashMap<>();
        Map<UUID, Imunobiologico> vacinas = new HashMap<>();
        for (Notificacao n : notificacaoRepository.findEsaviComDoses()) {
            boolean grave = n.getGravidadeEsavi() == GravidadeEsavi.GRAVE && n.getClassificacaoFinal() != ClassificacaoCasoEpidemiologico.DESCARTADO
                    && !n.getDataNotificacao().isBefore(hoje.minusDays(JANELA_GRAVES_DIAS));
            boolean desvio = n.getCausalidadeEsavi() == CausalidadeEsavi.A2_DESVIO_QUALIDADE && n.getEncerradaEm() != null
                    && n.getEncerradaEm().isAfter(limiteA2);
            if (!grave && !desvio) {
                continue;
            }
            for (RegistroVacinacao d : n.getDoses()) {
                String numero = EstoqueVacinaService.normalizar(d.getLoteEstoque() != null ? d.getLoteEstoque().getNumeroLote() : d.getLote());
                if (numero == null || numero.isEmpty()) {
                    continue;
                }
                Chave k = new Chave(d.getImunobiologico().getUuid(), numero);
                vacinas.putIfAbsent(k.imunobiologicoId(), d.getImunobiologico());
                if (grave) {
                    graves.computeIfAbsent(k, x -> new LinkedHashSet<>()).add(n);
                }
                if (desvio) {
                    a2.computeIfAbsent(k, x -> new LinkedHashSet<>()).add(n);
                }
            }
        }
        Map<Chave, SuspensaoLoteRede> ultima = new LinkedHashMap<>();
        for (SuspensaoLoteRede s : suspensaoRepository.findTodas()) {
            Chave k = new Chave(s.getImunobiologico().getUuid(), s.getNumeroLote());
            ultima.putIfAbsent(k, s);
            vacinas.putIfAbsent(k.imunobiologicoId(), s.getImunobiologico());
        }
        Set<Chave> chaves = new LinkedHashSet<>();
        graves.forEach((k, ns) -> {
            if (ns.size() >= MINIMO_GRAVES) {
                chaves.add(k);
            }
        });
        chaves.addAll(a2.keySet());
        ultima.forEach((k, s) -> {
            if (s.getSituacao() == SituacaoSuspensaoLote.VIGENTE) {
                chaves.add(k);
            }
        });
        List<ImunizacaoRedeDtos.AlertaLote> lista = new ArrayList<>();
        for (Chave k : chaves) {
            Imunobiologico i = vacinas.get(k.imunobiologicoId());
            Set<Notificacao> g = graves.getOrDefault(k, Set.of());
            Set<Notificacao> d = a2.getOrDefault(k, Set.of());
            Set<Notificacao> todas = new LinkedHashSet<>(g);
            todas.addAll(d);
            List<LoteImunobiologico> lotes = lotesDaRede(k);
            List<LoteImunobiologico> comEstoque = lotes.stream().filter(l -> l.getSituacao() != SituacaoLoteImunobiologico.INUTILIZADO && l.getDoses() > 0).toList();
            SuspensaoLoteRede s = ultima.get(k);
            lista.add(new ImunizacaoRedeDtos.AlertaLote(i.getUuid(), i.getSigla(), i.getNome(), k.numero(), g.size(), !d.isEmpty(), sinal(g.size(), !d.isEmpty()),
                    todas.stream().sorted(Comparator.comparing(Notificacao::getDataNotificacao).reversed())
                            .map(n -> new ImunizacaoRedeDtos.NotificacaoDoLote(n.getUuid(), n.getDataNotificacao(), n.getUnidade().getNome(), n.getGravidadeEsavi(),
                                    n.getCausalidadeEsavi())).toList(),
                    comEstoque.size(), comEstoque.stream().mapToInt(LoteImunobiologico::getDoses).sum(), s != null ? dto(s) : null));
        }
        lista.sort(Comparator.comparing((ImunizacaoRedeDtos.AlertaLote a) -> a.suspensao() == null || a.suspensao().situacao() != SituacaoSuspensaoLote.VIGENTE)
                .thenComparing(a -> -a.gravesEm30Dias()));
        return lista;
    }

    @Transactional(readOnly = true)
    public List<ImunizacaoRedeDtos.Suspensao> suspensoes() {
        controleDeAcesso.exigirAlguma(ImunizacaoCatalogoService.GERENCIAR);
        return suspensaoRepository.findTodas().stream().map(this::dto).toList();
    }

    // ── Suspensão e decisão ────────────────────────────────────────────────────────────────────

    /** A rede suspende o lote (com ou sem sinal: o comunicado de desvio de qualidade também vale); uma vigente por lote (409). */
    @Transactional
    public ImunizacaoRedeDtos.Suspensao suspender(SuspensaoLoteRequestDto dto) {
        exigirRede();
        Imunobiologico i = imunobiologicoRepository.findById(dto.getImunobiologicoId()).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar o imunobiológico " + dto.getImunobiologicoId() + " em nossos registros."));
        String numero = EstoqueVacinaService.normalizar(dto.getNumeroLote());
        if (numero.isEmpty()) {
            throw new ResourceBadRequestException("Informe o número do lote.");
        }
        if (estoqueService.suspensaoVigente(i.getUuid(), numero).isPresent()) {
            throw new ResourceConflictException("O lote " + numero + " de " + i.getSigla() + " já está suspenso na rede.");
        }
        Chave k = new Chave(i.getUuid(), numero);
        ImunizacaoRedeDtos.AlertaLote alerta = alertas().stream().filter(a -> a.imunobiologicoId().equals(i.getUuid()) && a.numeroLote().equals(numero))
                .findFirst().orElse(null);
        SuspensaoLoteRede s = new SuspensaoLoteRede();
        s.setImunobiologico(i);
        s.setNumeroLote(numero);
        s.setSinal(alerta != null && (alerta.gravesEm30Dias() > 0 || alerta.desvioDeQualidade()) ? alerta.sinal()
                : "Sem sinal de ESAVI na rede: decisão da coordenação.");
        s.setNotificacoes(alerta == null || alerta.notificacoes().isEmpty() ? null
                : String.join(",", alerta.notificacoes().stream().map(n -> n.uuid().toString()).toList()));
        s.setMotivo(dto.getMotivo().trim());
        s.setSituacao(SituacaoSuspensaoLote.VIGENTE);
        s.setSuspensaEm(Instant.now());
        s.setSuspensaPorCpf(UsuarioAutenticado.cpf());
        s = suspensaoRepository.save(s);
        for (LoteImunobiologico l : lotesDaRede(k)) {
            estoqueService.aplicarSuspensao(l, s);
            loteRepository.save(l);
        }
        ContextoAuditoria.registro(s.getUuid());
        return dto(s);
    }

    /**
     * Liberar: os lotes suspensos só pelo ESAVI voltam a disponíveis; os que também passaram por excursão seguem para a avaliação da
     * unidade. Inutilizar: o saldo de cada sala sai como perda física pelo ESAVI do lote e os frascos abertos se encerram.
     */
    @Transactional
    public ImunizacaoRedeDtos.Suspensao decidir(UUID id, DecisaoSuspensaoLoteRequestDto dto) {
        exigirRede();
        SuspensaoLoteRede s = suspensaoRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar a suspensão " + id + " em nossos registros."));
        String decisao = dto.getDecisao().trim().toUpperCase(Locale.ROOT);
        if (!decisao.equals("LIBERAR") && !decisao.equals("INUTILIZAR")) {
            throw new ResourceBadRequestException("A decisão é LIBERAR ou INUTILIZAR.");
        }
        if (s.getSituacao() != SituacaoSuspensaoLote.VIGENTE) {
            throw new ResourceUnprocessableEntityException("A suspensão já foi decidida.");
        }
        String parecer = dto.getParecer().trim();
        s.setSituacao(decisao.equals("LIBERAR") ? SituacaoSuspensaoLote.LIBERADA : SituacaoSuspensaoLote.INUTILIZADA);
        s.setDecididaEm(Instant.now());
        s.setDecididaPorCpf(UsuarioAutenticado.cpf());
        s.setParecer(parecer);
        s = suspensaoRepository.save(s);
        LocalDateTime agora = LocalDateTime.now();
        for (LoteImunobiologico x : loteRepository.findDaSuspensao(s.getUuid())) {
            LoteImunobiologico l = estoqueService.loteComTrava(x.getUuid());
            if (l.getSituacao() == SituacaoLoteImunobiologico.INUTILIZADO) {
                continue;
            }
            if (decisao.equals("LIBERAR")) {
                if (l.getMotivoSuspeita() == MotivoSuspeitaLote.ESAVI_LOTE) {
                    l.setSituacao(SituacaoLoteImunobiologico.DISPONIVEL);
                    l.setMotivoSuspeita(null);
                    l.setParecer(parecer);
                    l.setAvaliadoEm(Instant.now());
                    l.setAvaliadoPorCpf(UsuarioAutenticado.cpf());
                }
            } else {
                for (FrascoAberto f : frascoRepository.findByLote_UuidAndEncerradoEmIsNullOrderByAbertoEmAsc(l.getUuid())) {
                    f.setEncerradoEm(agora);
                    f.setMotivoEncerramento(MotivoEncerramentoFrasco.VENCIDO);
                    frascoRepository.save(f);
                }
                l.setSituacao(SituacaoLoteImunobiologico.INUTILIZADO);
                l.setParecer(parecer);
                l.setAvaliadoEm(Instant.now());
                l.setAvaliadoPorCpf(UsuarioAutenticado.cpf());
                int doses = l.getDoses();
                l.setDoses(0);
                if (doses > 0) {
                    estoqueService.lancar(l, TipoMovimentacaoImunobiologico.PERDA_FISICA, -doses, MotivoPerdaImunobiologico.ESAVI_LOTE, parecer, null, null);
                }
            }
            loteRepository.save(l);
        }
        ContextoAuditoria.registro(s.getUuid());
        return dto(s);
    }

    // ── Apoio ──────────────────────────────────────────────────────────────────────────────────

    /** Suspender e decidir são da coordenação da rede: quem gerencia só algumas unidades não alcança as outras salas (403). */
    private void exigirRede() {
        controleDeAcesso.exigirAlguma(ImunizacaoCatalogoService.GERENCIAR);
        if (controleDeAcesso.unidadesVisiveis(ImunizacaoCatalogoService.GERENCIAR).isPresent()) {
            throw new ResourceForbiddenException("A suspensão do lote vale para a rede inteira: é da coordenação com acesso a todas as unidades.");
        }
    }

    private List<LoteImunobiologico> lotesDaRede(Chave k) {
        return loteRepository.findDoImunobiologico(k.imunobiologicoId()).stream()
                .filter(l -> k.numero().equals(EstoqueVacinaService.normalizar(l.getNumeroLote()))).toList();
    }

    private static String sinal(long graves, boolean desvio) {
        List<String> partes = new ArrayList<>();
        if (graves > 0) {
            partes.add(graves + (graves == 1 ? " ESAVI grave" : " ESAVI graves") + " em " + JANELA_GRAVES_DIAS + " dias");
        }
        if (desvio) {
            partes.add("ESAVI com desvio de qualidade do produto (A2)");
        }
        return partes.isEmpty() ? "Sem sinal de ESAVI na rede." : String.join("; ", partes) + ".";
    }

    private ImunizacaoRedeDtos.Suspensao dto(SuspensaoLoteRede s) {
        Imunobiologico i = s.getImunobiologico();
        return new ImunizacaoRedeDtos.Suspensao(s.getUuid(), i.getUuid(), i.getSigla(), s.getNumeroLote(), s.getSinal(), s.getMotivo(), s.getSituacao(),
                s.getSuspensaEm(), s.getDecididaEm(), s.getParecer(), loteRepository.findDaSuspensao(s.getUuid()).size());
    }
}
