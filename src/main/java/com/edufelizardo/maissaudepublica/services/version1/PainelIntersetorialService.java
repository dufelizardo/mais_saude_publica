package com.edufelizardo.maissaudepublica.services.version1;

import com.edufelizardo.maissaudepublica.exceptions.ResourceBadRequestException;
import com.edufelizardo.maissaudepublica.models.EncaminhamentoIntersetorial;
import com.edufelizardo.maissaudepublica.models.InstituicaoExterna;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.PainelIntersetorialDtos.CobrancaDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.PainelIntersetorialDtos.InstituicaoDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.PainelIntersetorialDtos.PainelDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.PainelIntersetorialDtos.SemRetornoDto;
import com.edufelizardo.maissaudepublica.models.enuns.DominioOrigemEncaminhamento;
import com.edufelizardo.maissaudepublica.models.enuns.SetorIntersetorial;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoEncaminhamentoIntersetorial;
import com.edufelizardo.maissaudepublica.repositories.EncaminhamentoIntersetorialRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * O painel da rede intersetorial, fatia IS2 (ADR-0211): para a gestão ver quais instituições respondem aos
 * encaminhamentos e cobrar as que deixam o caso sem retorno.
 * <ul>
 *   <li>enviados, concluídos, recusados, cancelados, taxa de resolução e tempo médio contam os enviados no período;</li>
 *   <li>em aberto e sem retorno são a situação de hoje, de qualquer data de envio: é o que se cobra.</li>
 * </ul>
 * Sem paciente e sem motivo (sigilo da violência e da saúde mental). Acesso de INTERSETORIAL.GERENCIAR.
 */
@Service
public class PainelIntersetorialService {

    /** Sem datas, o painel olha os últimos 90 dias. */
    static final int DIAS_PADRAO = 90;
    /** O período máximo, para o painel não varrer anos de histórico. */
    static final int DIAS_MAXIMOS = 366;

    @Autowired
    private EncaminhamentoIntersetorialRepository repository;

    @Autowired
    private InstituicaoExternaService instituicaoService;

    @Autowired
    private ControleDeAcesso controleDeAcesso;

    @Transactional(readOnly = true)
    public PainelDto painel(LocalDate inicio, LocalDate fim, SetorIntersetorial setor, DominioOrigemEncaminhamento origem, UUID unidadeId) {
        controleDeAcesso.exigirAlguma(InstituicaoExternaService.GERENCIAR);
        LocalDate hoje = LocalDate.now();
        LocalDate ate = fim != null ? fim : hoje;
        LocalDate de = inicio != null ? inicio : ate.minusDays(DIAS_PADRAO - 1L);
        if (de.isAfter(ate)) {
            throw new ResourceBadRequestException("O início do período (" + de + ") é depois do fim (" + ate + ").");
        }
        if (ChronoUnit.DAYS.between(de, ate) >= DIAS_MAXIMOS) {
            throw new ResourceBadRequestException("O período passa de " + DIAS_MAXIMOS + " dias.");
        }
        Predicate<EncaminhamentoIntersetorial> filtro = e -> (setor == null || e.getInstituicao().getSetor() == setor)
                && (origem == null || e.getDominioOrigem() == origem)
                && (unidadeId == null || e.getUnidade().getUuid().equals(unidadeId));
        List<EncaminhamentoIntersetorial> todos = repository.findParaPainel(de, ate, SituacaoEncaminhamentoIntersetorial.ENVIADO,
                SituacaoEncaminhamentoIntersetorial.RECEBIDO).stream().filter(filtro).toList();

        Map<InstituicaoExterna, List<EncaminhamentoIntersetorial>> porInstituicao = todos.stream()
                .collect(Collectors.groupingBy(EncaminhamentoIntersetorial::getInstituicao, LinkedHashMap::new, Collectors.toList()));
        List<InstituicaoDto> linhas = porInstituicao.entrySet().stream()
                .map(x -> linha(x.getKey(), x.getValue(), de, ate, hoje))
                .sorted(Comparator.comparingLong(InstituicaoDto::semRetorno).reversed()
                        .thenComparing(Comparator.comparingLong(InstituicaoDto::emAberto).reversed())
                        .thenComparing(InstituicaoDto::nome, String.CASE_INSENSITIVE_ORDER))
                .toList();
        Contagem c = new Contagem(todos, de, ate, hoje);
        return new PainelDto(de, ate, c.enviados, c.emAberto, c.semRetorno, c.concluidos, c.recusados, c.cancelados, c.taxa(), c.tempoMedio(),
                linhas);
    }

    /** Os encaminhamentos sem retorno da instituição, do mais atrasado ao menos. */
    @Transactional(readOnly = true)
    public CobrancaDto semRetorno(UUID instituicaoId) {
        controleDeAcesso.exigirAlguma(InstituicaoExternaService.GERENCIAR);
        InstituicaoExterna instituicao = instituicaoService.instituicao(instituicaoId);
        LocalDate hoje = LocalDate.now();
        List<SemRetornoDto> lista = repository.findEmAbertoDaInstituicao(instituicaoId, SituacaoEncaminhamentoIntersetorial.ENVIADO,
                        SituacaoEncaminhamentoIntersetorial.RECEBIDO).stream()
                .filter(e -> e.semRetorno(hoje))
                .map(e -> new SemRetornoDto(e.getUuid(), numero(e.getUuid()), e.getDominioOrigem(), e.getUnidade().getNome(), e.getSituacao(),
                        e.getEnviadoEm(), e.getRecebidoEm(), e.getRetornoEsperadoEm(), ChronoUnit.DAYS.between(e.getRetornoEsperadoEm(), hoje)))
                .sorted(Comparator.comparingLong(SemRetornoDto::diasDeAtraso).reversed().thenComparing(SemRetornoDto::enviadoEm))
                .toList();
        return new CobrancaDto(InstituicaoExternaService.dto(instituicao), lista);
    }

    /** O número para falar com a instituição: os 8 primeiros caracteres do identificador. */
    static String numero(UUID uuid) {
        return uuid.toString().substring(0, 8).toUpperCase();
    }

    private static InstituicaoDto linha(InstituicaoExterna i, List<EncaminhamentoIntersetorial> lista, LocalDate de, LocalDate ate, LocalDate hoje) {
        Contagem c = new Contagem(lista, de, ate, hoje);
        Map<DominioOrigemEncaminhamento, Long> porOrigem = new EnumMap<>(DominioOrigemEncaminhamento.class);
        c.doPeriodo.forEach(e -> porOrigem.merge(e.getDominioOrigem(), 1L, Long::sum));
        return new InstituicaoDto(i.getUuid(), i.getNome(), i.getSetor(), i.getTipo(), i.getTelefone(), i.getResponsavel(), i.isAtiva(), c.enviados,
                c.emAberto, c.semRetorno, c.concluidos, c.recusados, c.cancelados, c.taxa(), c.tempoMedio(), porOrigem);
    }

    /** As contas de um conjunto de encaminhamentos: do período pelo envio, e os em aberto de hoje. */
    private static final class Contagem {
        final List<EncaminhamentoIntersetorial> doPeriodo;
        final long enviados;
        final long emAberto;
        final long semRetorno;
        final long concluidos;
        final long recusados;
        final long cancelados;
        final List<Long> diasAteRetorno;

        Contagem(List<EncaminhamentoIntersetorial> lista, LocalDate de, LocalDate ate, LocalDate hoje) {
            doPeriodo = lista.stream().filter(e -> !e.getEnviadoEm().isBefore(de) && !e.getEnviadoEm().isAfter(ate)).toList();
            enviados = doPeriodo.size();
            emAberto = lista.stream().filter(e -> e.getSituacao().emAberto()).count();
            semRetorno = lista.stream().filter(e -> e.semRetorno(hoje)).count();
            concluidos = conta(SituacaoEncaminhamentoIntersetorial.CONCLUIDO);
            recusados = conta(SituacaoEncaminhamentoIntersetorial.RECUSADO);
            cancelados = conta(SituacaoEncaminhamentoIntersetorial.CANCELADO);
            diasAteRetorno = doPeriodo.stream()
                    .filter(e -> e.getRetornoEm() != null && (e.getSituacao() == SituacaoEncaminhamentoIntersetorial.CONCLUIDO
                            || e.getSituacao() == SituacaoEncaminhamentoIntersetorial.RECUSADO))
                    .map(e -> ChronoUnit.DAYS.between(e.getEnviadoEm(), e.getRetornoEm()))
                    .toList();
        }

        private long conta(SituacaoEncaminhamentoIntersetorial s) {
            return doPeriodo.stream().filter(e -> e.getSituacao() == s).count();
        }

        /** Concluídos sobre os que tiveram retorno (concluídos e recusados), em %, com uma casa; sem retorno, nulo. */
        Double taxa() {
            long comRetorno = concluidos + recusados;
            return comRetorno == 0 ? null : Math.round(concluidos * 1000.0 / comRetorno) / 10.0;
        }

        /** Dias do envio ao retorno, em média, com uma casa; sem retorno no período, nulo. */
        Double tempoMedio() {
            return diasAteRetorno.isEmpty() ? null
                    : Math.round(diasAteRetorno.stream().mapToLong(Long::longValue).average().orElse(0) * 10) / 10.0;
        }
    }
}
