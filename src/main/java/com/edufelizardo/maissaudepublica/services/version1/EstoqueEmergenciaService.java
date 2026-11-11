package com.edufelizardo.maissaudepublica.services.version1;

import com.edufelizardo.maissaudepublica.config.ContextoAuditoria;
import com.edufelizardo.maissaudepublica.config.UsuarioAutenticado;
import com.edufelizardo.maissaudepublica.exceptions.ResourceNotFoundException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceUnprocessableEntityException;
import com.edufelizardo.maissaudepublica.models.Emergencia;
import com.edufelizardo.maissaudepublica.models.LoteMaterial;
import com.edufelizardo.maissaudepublica.models.MaterialEstoque;
import com.edufelizardo.maissaudepublica.models.MovimentacaoEstoque;
import com.edufelizardo.maissaudepublica.models.ReservaEstoque;
import com.edufelizardo.maissaudepublica.models.UnidadeDeSaude;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.ReservaEstoqueRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.EstoqueEmergenciaDtos;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoEmergencia;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoReservaEstoque;
import com.edufelizardo.maissaudepublica.models.enuns.TipoMovimentacaoEstoque;
import com.edufelizardo.maissaudepublica.repositories.EmergenciaRepository;
import com.edufelizardo.maissaudepublica.repositories.LoteMaterialRepository;
import com.edufelizardo.maissaudepublica.repositories.MaterialEstoqueRepository;
import com.edufelizardo.maissaudepublica.repositories.MovimentacaoEstoqueRepository;
import com.edufelizardo.maissaudepublica.repositories.ReservaEstoqueRepository;
import com.edufelizardo.maissaudepublica.repositories.UnidadeDeSaudeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

/**
 * O estoque nas emergências, fatia ES3 (ADR-0191): a reserva de material para a emergência numa unidade, feita pelo próprio estoque, e
 * o resumo do que foi reservado e consumido, que a emergência e o relatório pós-evento mostram. O consumo extraordinário e a requisição
 * urgente usam a reserva em {@link EstoqueService} e {@link CentralEstoqueService}.
 */
@Service
public class EstoqueEmergenciaService {

    private static final String COORDENAR = "EMERGENCIA.COORDENAR";
    private static final String[] VER = {EstoqueService.CONSULTAR, EstoqueService.MOVIMENTAR, EstoqueService.GERENCIAR, COORDENAR};

    @Autowired private ReservaEstoqueRepository reservaRepository;
    @Autowired private EmergenciaRepository emergenciaRepository;
    @Autowired private MaterialEstoqueRepository materialRepository;
    @Autowired private UnidadeDeSaudeRepository unidadeRepository;
    @Autowired private LoteMaterialRepository loteRepository;
    @Autowired private MovimentacaoEstoqueRepository movimentacaoRepository;
    @Autowired private EstoqueService estoqueService;
    @Autowired private ControleDeAcesso controleDeAcesso;

    /**
     * Reserva o material na unidade para a emergência ativa (422), até o que está livre: o saldo dentro da validade menos as reservas
     * ativas (422). Material inativo, 422.
     */
    @Transactional
    public EstoqueEmergenciaDtos.Reserva reservar(ReservaEstoqueRequestDto dto) {
        UnidadeDeSaude u = unidadeRepository.findById(dto.getUnidadeId()).orElseThrow(() -> naoEncontrada("a unidade de saúde", dto.getUnidadeId()));
        controleDeAcesso.exigir(EstoqueService.MOVIMENTAR, u);
        Emergencia em = emergenciaRepository.findById(dto.getEmergenciaId()).orElseThrow(() -> naoEncontrada("a emergência", dto.getEmergenciaId()));
        if (em.getSituacao() != SituacaoEmergencia.ATIVA) {
            throw new ResourceUnprocessableEntityException("A emergência " + em.getNome() + " não está ativa: a reserva vale só durante a resposta.");
        }
        MaterialEstoque m = materialRepository.findById(dto.getMaterialId()).orElseThrow(() -> naoEncontrada("o material", dto.getMaterialId()));
        if (!m.isAtivo()) {
            throw new ResourceUnprocessableEntityException("O material " + m.getNome() + " está inativo no catálogo.");
        }
        LocalDate hoje = LocalDate.now();
        List<LoteMaterial> lotes = loteRepository.findDoMaterialNaUnidadeParaAtualizar(m.getUuid(), u.getUuid());
        int valido = lotes.stream().filter(l -> l.getQuantidade() > 0 && !l.vencido(hoje)).mapToInt(LoteMaterial::getQuantidade).sum();
        int livre = Math.max(0, valido - estoqueService.reservado(u.getUuid(), m.getUuid(), null));
        if (dto.getQuantidade() > livre) {
            throw new ResourceUnprocessableEntityException("A " + u.getNome() + " tem " + livre + " de " + m.getNome()
                    + " livre dentro da validade; não dá para reservar " + dto.getQuantidade() + ".");
        }
        ReservaEstoque r = reservaRepository.save(new ReservaEstoque(null, em, u, m, dto.getQuantidade(), 0, SituacaoReservaEstoque.ATIVA,
                dto.getJustificativa().trim(), Instant.now(), null, null, UsuarioAutenticado.cpf(), null));
        ContextoAuditoria.unidade(u);
        ContextoAuditoria.registro(r.getUuid());
        ContextoAuditoria.detalhe("Reserva de " + dto.getQuantidade() + " " + m.getCodigo() + " para a emergência " + em.getNome() + ".");
        return reserva(r);
    }

    /** Libera o que resta da reserva ativa (já liberada ou consumida, 422), com o motivo. */
    @Transactional
    public EstoqueEmergenciaDtos.Reserva liberar(UUID uuid, ReservaEstoqueRequestDto.Liberacao dto) {
        ReservaEstoque r = reservaRepository.findByIdParaAtualizar(uuid).orElseThrow(() -> naoEncontrada("a reserva", uuid));
        controleDeAcesso.exigir(EstoqueService.MOVIMENTAR, r.getUnidade());
        if (r.getSituacao() != SituacaoReservaEstoque.ATIVA) {
            throw new ResourceUnprocessableEntityException("A reserva já está " + r.getSituacao() + ".");
        }
        fechar(r, dto.getMotivo().trim());
        ContextoAuditoria.unidade(r.getUnidade());
        ContextoAuditoria.registro(r.getUuid());
        ContextoAuditoria.detalhe("Reserva liberada: " + r.getMotivoLiberacao());
        return reserva(r);
    }

    /** As reservas visíveis, as ativas primeiro, pela unidade, pela emergência e pela situação. */
    @Transactional(readOnly = true)
    public List<EstoqueEmergenciaDtos.Reserva> reservas(UUID unidadeId, UUID emergenciaId, SituacaoReservaEstoque situacao) {
        controleDeAcesso.exigirAlguma(VER);
        Stream<ReservaEstoque> base = controleDeAcesso.filtrar(reservaRepository.findTodas().stream(), r -> Stream.of(r.getUnidade()), VER);
        return base.filter(r -> unidadeId == null || r.getUnidade().getUuid().equals(unidadeId))
                .filter(r -> emergenciaId == null || r.getEmergencia().getUuid().equals(emergenciaId))
                .filter(r -> situacao == null || r.getSituacao() == situacao)
                .sorted(Comparator.comparing((ReservaEstoque r) -> r.getSituacao() != SituacaoReservaEstoque.ATIVA))
                .map(EstoqueEmergenciaService::reserva).toList();
    }

    /** As emergências ativas e em desmobilização, para o estoque (que não lê as emergências) escolher na reserva e no consumo. */
    @Transactional(readOnly = true)
    public List<EstoqueEmergenciaDtos.EmergenciaAberta> emergenciasAbertas() {
        controleDeAcesso.exigirAlguma(VER);
        return emergenciaRepository.findTodas().stream()
                .filter(e -> e.getSituacao() == SituacaoEmergencia.ATIVA || e.getSituacao() == SituacaoEmergencia.EM_DESMOBILIZACAO)
                .map(e -> new EstoqueEmergenciaDtos.EmergenciaAberta(e.getUuid(), e.getNome(), e.getSituacao())).toList();
    }

    /** Desmobilizar ou encerrar a emergência libera o que resta das reservas dela; devolve quantas. */
    int liberarDaEmergencia(Emergencia em, String motivo) {
        int n = 0;
        for (ReservaEstoque r : reservaRepository.findDaEmergencia(em.getUuid())) {
            if (r.getSituacao() == SituacaoReservaEstoque.ATIVA) {
                fechar(r, motivo.length() > 500 ? motivo.substring(0, 500) : motivo);
                n++;
            }
        }
        return n;
    }

    /** O que a emergência mostra: as reservas e o consumo por unidade e material, com o valor pelo custo do lote. */
    EstoqueEmergenciaDtos.Resumo resumo(Emergencia em) {
        List<EstoqueEmergenciaDtos.Reserva> reservas = reservaRepository.findDaEmergencia(em.getUuid()).stream()
                .sorted(Comparator.comparing((ReservaEstoque r) -> r.getSituacao() != SituacaoReservaEstoque.ATIVA))
                .map(EstoqueEmergenciaService::reserva).toList();
        Map<String, Object[]> grupos = new LinkedHashMap<>();
        for (MovimentacaoEstoque m : movimentacaoRepository.findDaEmergencia(em.getUuid())) {
            if (m.getTipo() != TipoMovimentacaoEstoque.SAIDA_CONSUMO) {
                continue;
            }
            LoteMaterial l = m.getLote();
            Object[] g = grupos.computeIfAbsent(l.getUnidade().getUuid() + "/" + l.getMaterial().getUuid(), k -> new Object[]{l, 0, null});
            g[1] = (int) g[1] - m.getQuantidade();
            if (l.getCustoUnitario() != null) {
                BigDecimal v = l.getCustoUnitario().multiply(BigDecimal.valueOf(-m.getQuantidade()));
                g[2] = g[2] == null ? v : ((BigDecimal) g[2]).add(v);
            }
        }
        List<EstoqueEmergenciaDtos.Consumo> consumo = new ArrayList<>();
        for (Object[] g : grupos.values()) {
            LoteMaterial l = (LoteMaterial) g[0];
            BigDecimal valor = g[2] == null ? null : ((BigDecimal) g[2]).setScale(2, java.math.RoundingMode.HALF_UP);
            consumo.add(new EstoqueEmergenciaDtos.Consumo(l.getUnidade().getUuid(), l.getUnidade().getNome(), l.getMaterial().getUuid(),
                    l.getMaterial().getCodigo(), l.getMaterial().getNome(), l.getMaterial().getUnidadeMedida(), (int) g[1], valor));
        }
        consumo.sort(Comparator.comparing(EstoqueEmergenciaDtos.Consumo::unidadeNome, String.CASE_INSENSITIVE_ORDER)
                .thenComparing(EstoqueEmergenciaDtos.Consumo::materialNome, String.CASE_INSENSITIVE_ORDER));
        BigDecimal total = consumo.stream().map(EstoqueEmergenciaDtos.Consumo::valor).filter(java.util.Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new EstoqueEmergenciaDtos.Resumo(reservas, consumo, consumo.stream().mapToInt(EstoqueEmergenciaDtos.Consumo::quantidade).sum(),
                total.setScale(2, java.math.RoundingMode.HALF_UP));
    }

    private void fechar(ReservaEstoque r, String motivo) {
        r.setSituacao(SituacaoReservaEstoque.LIBERADA);
        r.setLiberadaEm(Instant.now());
        r.setMotivoLiberacao(motivo);
        r.setLiberadoPorCpf(UsuarioAutenticado.cpf());
        reservaRepository.save(r);
    }

    private static EstoqueEmergenciaDtos.Reserva reserva(ReservaEstoque r) {
        MaterialEstoque m = r.getMaterial();
        return new EstoqueEmergenciaDtos.Reserva(r.getUuid(), r.getEmergencia().getUuid(), r.getEmergencia().getNome(), r.getUnidade().getUuid(),
                r.getUnidade().getNome(), m.getUuid(), m.getCodigo(), m.getNome(), m.getUnidadeMedida(), r.getQuantidade(), r.getConsumida(), r.restante(),
                r.getSituacao(), r.getJustificativa(), r.getCriadaEm(), r.getLiberadaEm(), r.getMotivoLiberacao());
    }

    private static ResourceNotFoundException naoEncontrada(String o, UUID uuid) {
        return new ResourceNotFoundException("Não foi possível encontrar " + o + " " + uuid + " em nossos registros.");
    }
}
