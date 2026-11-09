package com.edufelizardo.maissaudepublica.services.version1;

import com.edufelizardo.maissaudepublica.config.ContextoAuditoria;
import com.edufelizardo.maissaudepublica.config.UsuarioAutenticado;
import com.edufelizardo.maissaudepublica.exceptions.ResourceBadRequestException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceConflictException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceNotFoundException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceUnprocessableEntityException;
import com.edufelizardo.maissaudepublica.models.CotaPpi;
import com.edufelizardo.maissaudepublica.models.OfertaAssistencial;
import com.edufelizardo.maissaudepublica.models.ProcedimentoRegulado;
import com.edufelizardo.maissaudepublica.models.SolicitacaoRegulacao;
import com.edufelizardo.maissaudepublica.models.UnidadeDeSaude;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.CopiaCompetenciaRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.CotaPpiRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.OfertaAssistencialRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.RedeDtos;
import com.edufelizardo.maissaudepublica.models.enuns.StatusSolicitacaoRegulacao;
import com.edufelizardo.maissaudepublica.repositories.CotaPpiRepository;
import com.edufelizardo.maissaudepublica.repositories.OfertaAssistencialRepository;
import com.edufelizardo.maissaudepublica.repositories.SolicitacaoRegulacaoRepository;
import com.edufelizardo.maissaudepublica.repositories.UnidadeCapacidadeAssistencialRepository;
import com.edufelizardo.maissaudepublica.repositories.UnidadeDeSaudeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Gestão da Rede, fatia GR1 (ADR-0179): a oferta de cada executante por procedimento e competência e as cotas da PPI que a repartem entre
 * unidades e esferas. A Regulação ({@link SolicitacaoRegulacaoService}) consome a cota na autorização; sem oferta cadastrada, nada muda.
 */
@Service
public class RedeService {

    public static final String CONSULTAR = "REDE.CONSULTAR";
    public static final String GERENCIAR = "REDE.GERENCIAR";
    /** As situações em que a autorização ocupa a vaga: a falta também gasta a vaga. */
    static final Set<StatusSolicitacaoRegulacao> CONSOMEM = EnumSet.of(StatusSolicitacaoRegulacao.AUTORIZADA, StatusSolicitacaoRegulacao.AGENDADA,
            StatusSolicitacaoRegulacao.REALIZADA, StatusSolicitacaoRegulacao.FALTOU);

    @Autowired private OfertaAssistencialRepository ofertaRepository;
    @Autowired private CotaPpiRepository cotaRepository;
    @Autowired private SolicitacaoRegulacaoRepository solicitacaoRepository;
    @Autowired private UnidadeDeSaudeRepository unidadeRepository;
    @Autowired private UnidadeCapacidadeAssistencialRepository capacidadeRepository;
    @Autowired private ProcedimentoReguladoService procedimentoService;
    @Autowired private ControleDeAcesso controleDeAcesso;

    // ── Oferta ────────────────────────────────────────────────────────────────────────────────────

    @Transactional
    public RedeDtos.Oferta criarOferta(OfertaAssistencialRequestDto dto) {
        if (dto.getExecutanteId() == null || dto.getProcedimentoId() == null || dto.getCompetencia() == null) {
            throw new ResourceBadRequestException("Informe a executante, o procedimento e a competência.");
        }
        UnidadeDeSaude executante = unidade(dto.getExecutanteId());
        controleDeAcesso.exigir(GERENCIAR, executante);
        ProcedimentoRegulado procedimento = procedimentoService.buscar(dto.getProcedimentoId());
        if (!procedimento.isAtivo()) {
            throw new ResourceUnprocessableEntityException("O procedimento " + procedimento.getNome() + " está fora de uso no catálogo da regulação.");
        }
        var exigida = procedimento.getCapacidadeExigida();
        if (exigida != null && !capacidadeRepository.existsByUnidade_UuidAndCapacidade_UuidAndFimIsNull(executante.getUuid(), exigida.getUuid())) {
            throw new ResourceUnprocessableEntityException(executante.getNome() + " não tem a capacidade " + exigida.getNome() + ", que "
                    + procedimento.getNome() + " exige (modelo operacional).");
        }
        LocalDate competencia = dto.getCompetencia().atDay(1);
        if (ofertaRepository.existsByExecutante_UuidAndProcedimento_UuidAndCompetencia(executante.getUuid(), procedimento.getUuid(), competencia)) {
            throw new ResourceConflictException(executante.getNome() + " já tem oferta de " + procedimento.getNome() + " em " + dto.getCompetencia() + ".");
        }
        OfertaAssistencial o = ofertaRepository.save(new OfertaAssistencial(null, executante, procedimento, competencia, dto.getQuantidade(),
                texto(dto.getObservacao()), Instant.now(), UsuarioAutenticado.cpf()));
        ContextoAuditoria.registro(o.getUuid());
        return oferta(o, List.of());
    }

    /** Só a quantidade e a observação; não fica abaixo do que já está pactuado nem do que já foi usado (422). */
    @Transactional
    public RedeDtos.Oferta atualizarOferta(UUID uuid, OfertaAssistencialRequestDto dto) {
        OfertaAssistencial o = ofertaPorId(uuid);
        controleDeAcesso.exigir(GERENCIAR, o.getExecutante());
        List<CotaPpi> cotas = cotaRepository.findDasOfertas(List.of(o.getUuid()));
        int pactuado = cotas.stream().mapToInt(CotaPpi::getQuantidade).sum();
        if (dto.getQuantidade() < pactuado) {
            throw new ResourceUnprocessableEntityException("Já há " + pactuado + " vaga(s) pactuada(s); reduza as cotas antes.");
        }
        int usado = usados(o).size();
        if (dto.getQuantidade() < usado) {
            throw new ResourceUnprocessableEntityException("Já há " + usado + " vaga(s) autorizada(s) nesta oferta.");
        }
        o.setQuantidade(dto.getQuantidade());
        o.setObservacao(texto(dto.getObservacao()));
        o.setAtualizadaEm(Instant.now());
        o.setAtualizadaPorCpf(UsuarioAutenticado.cpf());
        ofertaRepository.save(o);
        return oferta(o, cotas);
    }

    // ── Cotas ─────────────────────────────────────────────────────────────────────────────────────

    @Transactional
    public RedeDtos.Oferta criarCota(UUID ofertaId, CotaPpiRequestDto dto) {
        OfertaAssistencial o = ofertaPorId(ofertaId);
        controleDeAcesso.exigir(GERENCIAR, o.getExecutante());
        if (dto.getBeneficiariaId() == null) {
            throw new ResourceBadRequestException("Informe a unidade ou a esfera beneficiária.");
        }
        UnidadeDeSaude beneficiaria = unidade(dto.getBeneficiariaId());
        if (cotaRepository.existsByOferta_UuidAndBeneficiaria_Uuid(o.getUuid(), beneficiaria.getUuid())) {
            throw new ResourceConflictException(beneficiaria.getNome() + " já tem cota nesta oferta; ajuste a existente.");
        }
        List<CotaPpi> cotas = cotaRepository.findDasOfertas(List.of(o.getUuid()));
        exigirTeto(o, cotas.stream().mapToInt(CotaPpi::getQuantidade).sum() + dto.getQuantidade());
        CotaPpi c = cotaRepository.save(new CotaPpi(null, o, beneficiaria, dto.getQuantidade(), Instant.now(), UsuarioAutenticado.cpf()));
        ContextoAuditoria.detalhe("Cota de " + beneficiaria.getNome() + " em " + o.getProcedimento().getNome() + ": " + dto.getQuantidade() + ".");
        List<CotaPpi> todas = new ArrayList<>(cotas);
        todas.add(c);
        return oferta(o, todas);
    }

    @Transactional
    public RedeDtos.Oferta atualizarCota(UUID cotaId, CotaPpiRequestDto dto) {
        CotaPpi c = cotaRepository.findByIdParaAtualizar(cotaId).orElseThrow(() -> cotaNaoEncontrada(cotaId));
        OfertaAssistencial o = c.getOferta();
        controleDeAcesso.exigir(GERENCIAR, o.getExecutante());
        int usado = (int) solicitacaoRepository.countByCota_UuidAndStatusIn(c.getUuid(), CONSOMEM);
        if (dto.getQuantidade() < usado) {
            throw new ResourceUnprocessableEntityException("A cota já tem " + usado + " autorização(ões); não fica abaixo disso.");
        }
        List<CotaPpi> cotas = cotaRepository.findDasOfertas(List.of(o.getUuid()));
        exigirTeto(o, cotas.stream().filter(x -> !x.getUuid().equals(c.getUuid())).mapToInt(CotaPpi::getQuantidade).sum() + dto.getQuantidade());
        c.setQuantidade(dto.getQuantidade());
        c.setAtualizadaEm(Instant.now());
        c.setAtualizadaPorCpf(UsuarioAutenticado.cpf());
        cotaRepository.save(c);
        return oferta(o, cotaRepository.findDasOfertas(List.of(o.getUuid())));
    }

    /** Só a cota sem uso sai; a usada fica no histórico das autorizações (422). */
    @Transactional
    public RedeDtos.Oferta removerCota(UUID cotaId) {
        CotaPpi c = cotaRepository.findByIdParaAtualizar(cotaId).orElseThrow(() -> cotaNaoEncontrada(cotaId));
        OfertaAssistencial o = c.getOferta();
        controleDeAcesso.exigir(GERENCIAR, o.getExecutante());
        if (solicitacaoRepository.countByCota_UuidAndStatusIn(c.getUuid(), EnumSet.allOf(StatusSolicitacaoRegulacao.class)) > 0) {
            throw new ResourceUnprocessableEntityException("A cota já foi usada em autorizações; reduza-a em vez de removê-la.");
        }
        cotaRepository.delete(c);
        return oferta(o, cotaRepository.findDasOfertas(List.of(o.getUuid())).stream().filter(x -> !x.getUuid().equals(cotaId)).toList());
    }

    /** Copia as ofertas e cotas que o usuário gerencia; a oferta que já existe no destino fica como está. */
    @Transactional
    public RedeDtos.Copia copiar(CopiaCompetenciaRequestDto dto) {
        controleDeAcesso.exigirAlguma(GERENCIAR);
        if (!dto.getDestino().isAfter(dto.getOrigem())) {
            throw new ResourceBadRequestException("O destino precisa ser depois da origem.");
        }
        List<OfertaAssistencial> origem = controleDeAcesso.filtrar(ofertaRepository.findDaCompetencia(dto.getOrigem().atDay(1)).stream(),
                o -> Stream.of(o.getExecutante()), GERENCIAR).toList();
        Map<UUID, List<CotaPpi>> cotas = cotaRepository.findDasOfertas(origem.stream().map(OfertaAssistencial::getUuid).toList()).stream()
                .collect(Collectors.groupingBy(c -> c.getOferta().getUuid()));
        LocalDate destino = dto.getDestino().atDay(1);
        int ofertas = 0;
        int copiadas = 0;
        int existentes = 0;
        for (OfertaAssistencial o : origem) {
            if (ofertaRepository.existsByExecutante_UuidAndProcedimento_UuidAndCompetencia(o.getExecutante().getUuid(), o.getProcedimento().getUuid(), destino)) {
                existentes++;
                continue;
            }
            OfertaAssistencial nova = ofertaRepository.save(new OfertaAssistencial(null, o.getExecutante(), o.getProcedimento(), destino, o.getQuantidade(),
                    o.getObservacao(), Instant.now(), UsuarioAutenticado.cpf()));
            ofertas++;
            for (CotaPpi c : cotas.getOrDefault(o.getUuid(), List.of())) {
                cotaRepository.save(new CotaPpi(null, nova, c.getBeneficiaria(), c.getQuantidade(), Instant.now(), UsuarioAutenticado.cpf()));
                copiadas++;
            }
        }
        ContextoAuditoria.detalhe("PPI copiada de " + dto.getOrigem() + " para " + dto.getDestino() + ": " + ofertas + " oferta(s).");
        return new RedeDtos.Copia(dto.getOrigem(), dto.getDestino(), ofertas, copiadas, existentes);
    }

    // ── Leitura ───────────────────────────────────────────────────────────────────────────────────

    /** As ofertas da competência cuja executante ou alguma beneficiária está no escopo. */
    @Transactional(readOnly = true)
    public RedeDtos.Painel painel(YearMonth competencia, UUID executanteId) {
        controleDeAcesso.exigirAlguma(CONSULTAR, GERENCIAR);
        YearMonth mes = competencia != null ? competencia : YearMonth.now();
        List<OfertaAssistencial> todas = ofertaRepository.findDaCompetencia(mes.atDay(1)).stream()
                .filter(o -> executanteId == null || o.getExecutante().getUuid().equals(executanteId)).toList();
        Map<UUID, List<CotaPpi>> cotas = cotaRepository.findDasOfertas(todas.stream().map(OfertaAssistencial::getUuid).toList()).stream()
                .collect(Collectors.groupingBy(c -> c.getOferta().getUuid()));
        List<RedeDtos.Oferta> ofertas = controleDeAcesso.filtrar(todas.stream(), o -> Stream.concat(Stream.of(o.getExecutante()),
                        cotas.getOrDefault(o.getUuid(), List.of()).stream().map(CotaPpi::getBeneficiaria)), CONSULTAR, GERENCIAR)
                .map(o -> oferta(o, cotas.getOrDefault(o.getUuid(), List.of())))
                .sorted(Comparator.comparing(RedeDtos.Oferta::procedimentoNome, String.CASE_INSENSITIVE_ORDER)
                        .thenComparing(RedeDtos.Oferta::executanteNome, String.CASE_INSENSITIVE_ORDER)).toList();
        return new RedeDtos.Painel(mes, ofertas.stream().mapToInt(RedeDtos.Oferta::quantidade).sum(), ofertas.stream().mapToInt(RedeDtos.Oferta::pactuado).sum(),
                ofertas.stream().mapToInt(RedeDtos.Oferta::utilizado).sum(), ofertas.stream().mapToInt(RedeDtos.Oferta::extracotas).sum(), ofertas);
    }

    /** Para a tela da Regulação: o que a autorização vai encontrar, sem consumir nada. */
    @Transactional(readOnly = true)
    public RedeDtos.Saldo saldo(UUID procedimentoId, UUID executanteId, UUID solicitanteId, YearMonth competencia) {
        controleDeAcesso.exigirAlguma(CONSULTAR, GERENCIAR, SolicitacaoRegulacaoService.REGULAR);
        var r = resolver(procedimentoId, executanteId, unidade(solicitanteId), competencia.atDay(1), false);
        return r.saldo;
    }

    // ── Para a Regulação ──────────────────────────────────────────────────────────────────────────

    /** O resultado para a autorização: a cota a consumir (travada), se houver saldo. */
    public record ParaAutorizar(boolean controlada, CotaPpi cotaComSaldo, String descricao) {
    }

    /** Chamado dentro da transação da autorização; trava a cota para o saldo não ser usado duas vezes. */
    @Transactional
    public ParaAutorizar paraAutorizar(ProcedimentoRegulado procedimento, UnidadeDeSaude executante, UnidadeDeSaude solicitante, LocalDateTime quando) {
        var r = resolver(procedimento.getUuid(), executante.getUuid(), solicitante, YearMonth.from(quando).atDay(1), true);
        if (!r.saldo.controlada()) {
            return new ParaAutorizar(false, null, null);
        }
        if (r.cota == null) {
            return new ParaAutorizar(true, null, "sem cota pactuada para " + solicitante.getNome());
        }
        return new ParaAutorizar(true, r.saldo.saldo() > 0 ? r.cota : null,
                "cota de " + r.cota.getBeneficiaria().getNome() + " (" + r.saldo.saldo() + " de " + r.saldo.cota() + ")");
    }

    private record Resolvido(RedeDtos.Saldo saldo, CotaPpi cota) {
    }

    private Resolvido resolver(UUID procedimentoId, UUID executanteId, UnidadeDeSaude solicitante, LocalDate competencia, boolean travar) {
        OfertaAssistencial o = ofertaRepository.findByExecutante_UuidAndProcedimento_UuidAndCompetencia(executanteId, procedimentoId, competencia).orElse(null);
        if (o == null) {
            return new Resolvido(new RedeDtos.Saldo(false, null, null, null, null, null, null), null);
        }
        Map<UUID, CotaPpi> porBeneficiaria = cotaRepository.findDasOfertas(List.of(o.getUuid())).stream()
                .collect(Collectors.toMap(c -> c.getBeneficiaria().getUuid(), c -> c));
        CotaPpi cota = acima(solicitante).stream().map(porBeneficiaria::get).filter(Objects::nonNull).findFirst().orElse(null);
        if (cota == null) {
            return new Resolvido(new RedeDtos.Saldo(true, o.getUuid(), null, null, null, null, null), null);
        }
        if (travar) {
            cota = cotaRepository.findByIdParaAtualizar(cota.getUuid()).orElseThrow();
        }
        int usado = (int) solicitacaoRepository.countByCota_UuidAndStatusIn(cota.getUuid(), CONSOMEM);
        return new Resolvido(new RedeDtos.Saldo(true, o.getUuid(), cota.getUuid(), cota.getBeneficiaria().getNome(), cota.getQuantidade(), usado,
                Math.max(0, cota.getQuantidade() - usado)), cota);
    }

    /** A unidade e as que estão acima dela (superior e supervisão regional), da mais próxima para a mais distante. */
    static List<UUID> acima(UnidadeDeSaude u) {
        Set<UUID> vistos = new LinkedHashSet<>();
        Deque<UnidadeDeSaude> fila = new ArrayDeque<>();
        fila.add(u);
        while (!fila.isEmpty()) {
            UnidadeDeSaude x = fila.poll();
            if (x == null || !vistos.add(x.getUuid())) {
                continue;
            }
            if (x.getSupervisaoRegional() != null) {
                fila.add(x.getSupervisaoRegional());
            }
            if (x.getUnidadeSuperior() != null) {
                fila.add(x.getUnidadeSuperior());
            }
        }
        return new ArrayList<>(vistos);
    }

    // ── Apoio ─────────────────────────────────────────────────────────────────────────────────────

    private List<SolicitacaoRegulacao> usados(OfertaAssistencial o) {
        LocalDateTime de = o.getCompetencia().atStartOfDay();
        return solicitacaoRepository.findAutorizadasNoPeriodo(o.getExecutante().getUuid(), o.getProcedimento().getUuid(), de, de.plusMonths(1), CONSOMEM)
                .stream().filter(s -> s.getCota() != null || s.isExtraCota()).toList();
    }

    private RedeDtos.Oferta oferta(OfertaAssistencial o, List<CotaPpi> cotas) {
        List<SolicitacaoRegulacao> usados = usados(o);
        Map<UUID, Long> porCota = usados.stream().filter(s -> s.getCota() != null)
                .collect(Collectors.groupingBy(s -> s.getCota().getUuid(), Collectors.counting()));
        List<RedeDtos.Cota> lista = cotas.stream()
                .map(c -> {
                    int u = porCota.getOrDefault(c.getUuid(), 0L).intValue();
                    return new RedeDtos.Cota(c.getUuid(), c.getBeneficiaria().getUuid(), c.getBeneficiaria().getNome(),
                            c.getBeneficiaria().getTipo() != null ? c.getBeneficiaria().getTipo().name() : null, c.getQuantidade(), u, Math.max(0, c.getQuantidade() - u));
                })
                .sorted(Comparator.comparing(RedeDtos.Cota::beneficiariaNome, String.CASE_INSENSITIVE_ORDER)).toList();
        int pactuado = cotas.stream().mapToInt(CotaPpi::getQuantidade).sum();
        int extras = (int) usados.stream().filter(SolicitacaoRegulacao::isExtraCota).count();
        return new RedeDtos.Oferta(o.getUuid(), YearMonth.from(o.getCompetencia()), o.getExecutante().getUuid(), o.getExecutante().getNome(),
                o.getProcedimento().getUuid(), o.getProcedimento().getNome(), o.getQuantidade(), pactuado, Math.max(0, o.getQuantidade() - pactuado),
                usados.size(), extras, o.getObservacao(), lista);
    }

    private static void exigirTeto(OfertaAssistencial o, int total) {
        if (total > o.getQuantidade()) {
            throw new ResourceUnprocessableEntityException("As cotas somariam " + total + ", mais que a oferta de " + o.getQuantidade() + ".");
        }
    }

    private OfertaAssistencial ofertaPorId(UUID id) {
        return ofertaRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar a oferta " + id + " em nossos registros."));
    }

    private ResourceNotFoundException cotaNaoEncontrada(UUID id) {
        return new ResourceNotFoundException("Não foi possível encontrar a cota " + id + " em nossos registros.");
    }

    private UnidadeDeSaude unidade(UUID id) {
        if (id == null) {
            throw new ResourceBadRequestException("Informe a unidade.");
        }
        return unidadeRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar uma unidade de saúde com o id " + id + " em nossos registros."));
    }

    private static String texto(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }
}
