package com.edufelizardo.maissaudepublica.services.version1;

import com.edufelizardo.maissaudepublica.config.ContextoAuditoria;
import com.edufelizardo.maissaudepublica.config.UsuarioAutenticado;
import com.edufelizardo.maissaudepublica.exceptions.ResourceNotFoundException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceUnprocessableEntityException;
import com.edufelizardo.maissaudepublica.models.Leito;
import com.edufelizardo.maissaudepublica.models.LimiteLeitos;
import com.edufelizardo.maissaudepublica.models.UnidadeCapacidadeAssistencial;
import com.edufelizardo.maissaudepublica.models.UnidadeDeSaude;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.LimiteLeitosRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.LimiteLeitosDto;
import com.edufelizardo.maissaudepublica.models.enuns.TipoLeito;
import com.edufelizardo.maissaudepublica.repositories.LeitoRepository;
import com.edufelizardo.maissaudepublica.repositories.LimiteLeitosRepository;
import com.edufelizardo.maissaudepublica.repositories.UnidadeCapacidadeAssistencialRepository;
import com.edufelizardo.maissaudepublica.repositories.UnidadeDeSaudeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Leitos autorizados por capacidade (ADR-0171). A gestão responde pelo número, por isso ele bloqueia; sem número, não há limite. Só os
 * leitos ativos contam, e os leitos extras da capacidade emergencial ficam de fora.
 */
@Service
public class LimiteLeitosService {

    public static final String AUTORIZAR = "LEITO.AUTORIZAR";
    private static final String[] LER = {AUTORIZAR, LeitoService.GERENCIAR, RedeUnidadesService.GERENCIAR, "ADMINISTRATIVO.CONSULTAR", "INTERNACAO.CONSULTAR"};

    @Autowired private LimiteLeitosRepository limiteRepository;
    @Autowired private LeitoRepository leitoRepository;
    @Autowired private UnidadeCapacidadeAssistencialRepository unidadeCapacidadeRepository;
    @Autowired private UnidadeDeSaudeRepository unidadeRepository;
    @Autowired private ControleDeAcesso controleDeAcesso;

    /** As capacidades de leito vigentes na unidade. */
    @Transactional(readOnly = true)
    public List<LimiteLeitosDto> daUnidade(UUID unidadeId) {
        UnidadeDeSaude u = unidade(unidadeId);
        controleDeAcesso.exigirVisivel(u, LER);
        List<LimiteLeitos> historico = limiteRepository.findDaUnidade(unidadeId);
        List<Leito> leitos = leitoRepository.findByUnidade_UuidOrderByIdentificacaoAsc(unidadeId);
        return unidadeCapacidadeRepository.findDaUnidade(unidadeId).stream().filter(UnidadeCapacidadeAssistencial::vigente)
                .filter(a -> !tipos(a.getCapacidade().getCodigo()).isEmpty())
                .sorted(Comparator.comparing(a -> a.getCapacidade().getNome()))
                .map(a -> {
                    String codigo = a.getCapacidade().getCodigo();
                    List<LimiteLeitosDto.Alteracao> alteracoes = historico.stream().filter(l -> l.getCapacidade().getCodigo().equals(codigo))
                            .map(l -> new LimiteLeitosDto.Alteracao(l.getQuantidade(), l.getMotivo(), l.getRegistradoEm())).toList();
                    return new LimiteLeitosDto(a.getCapacidade().getUuid(), codigo, a.getCapacidade().getNome(), tipos(codigo), ativos(leitos, codigo, null),
                            alteracoes.isEmpty() ? null : alteracoes.get(0).getQuantidade(), alteracoes);
                }).toList();
    }

    /** Muda o limite: capacidade de leito vigente na unidade (422), diferente do atual (422) e sem ficar abaixo dos leitos ativos (422). */
    @Transactional
    public List<LimiteLeitosDto> alterar(UUID unidadeId, LimiteLeitosRequestDto dto) {
        UnidadeDeSaude u = unidade(unidadeId);
        controleDeAcesso.exigir(AUTORIZAR, u);
        UnidadeCapacidadeAssistencial a = unidadeCapacidadeRepository.findDaUnidade(unidadeId).stream()
                .filter(x -> x.vigente() && x.getCapacidade().getUuid().equals(dto.getCapacidadeId())).findFirst()
                .orElseThrow(() -> new ResourceUnprocessableEntityException(u.getNome() + " não tem esta capacidade vigente."));
        String codigo = a.getCapacidade().getCodigo();
        if (tipos(codigo).isEmpty()) {
            throw new ResourceUnprocessableEntityException(a.getCapacidade().getNome() + " não é uma capacidade de leitos.");
        }
        if (Objects.equals(atual(unidadeId, codigo).orElse(null), dto.getQuantidade())) {
            throw new ResourceUnprocessableEntityException("O limite já é " + dto.getQuantidade() + ".");
        }
        int ativos = ativos(leitoRepository.findByUnidade_UuidOrderByIdentificacaoAsc(unidadeId), codigo, null);
        if (dto.getQuantidade() < ativos) {
            throw new ResourceUnprocessableEntityException(a.getCapacidade().getNome() + " tem " + ativos + " leito(s) ativo(s): tire-os de uso antes de reduzir.");
        }
        LimiteLeitos l = new LimiteLeitos(null, u, a.getCapacidade(), dto.getQuantidade(), dto.getMotivo().trim(), LocalDateTime.now(), UsuarioAutenticado.cpf());
        limiteRepository.save(l);
        ContextoAuditoria.unidade(u);
        ContextoAuditoria.registro(l.getUuid());
        ContextoAuditoria.detalhe(a.getCapacidade().getNome() + ": " + dto.getQuantidade() + " leito(s) autorizado(s).");
        return daUnidade(unidadeId);
    }

    /** Recusa o leito que passaria do limite da capacidade (422). O próprio leito, numa edição, não conta. */
    @Transactional(readOnly = true)
    public void conferir(UnidadeDeSaude unidade, TipoLeito tipo, UUID leitoEditado) {
        String codigo = LeitoService.capacidadeDoTipo(tipo);
        Optional<Integer> limite = atual(unidade.getUuid(), codigo);
        if (limite.isEmpty()) {
            return;
        }
        int ativos = ativos(leitoRepository.findByUnidade_UuidOrderByIdentificacaoAsc(unidade.getUuid()), codigo, leitoEditado);
        if (ativos >= limite.get()) {
            throw new ResourceUnprocessableEntityException(unidade.getNome() + " já está no limite de " + codigo + ": " + ativos + " de " + limite.get()
                    + " leito(s) autorizado(s). A gestão da unidade pode alterar o limite.");
        }
    }

    private Optional<Integer> atual(UUID unidadeId, String codigo) {
        return limiteRepository.findDaUnidade(unidadeId).stream().filter(l -> l.getCapacidade().getCodigo().equals(codigo)).findFirst().map(LimiteLeitos::getQuantidade);
    }

    private static int ativos(List<Leito> leitos, String codigo, UUID ignorar) {
        return (int) leitos.stream().filter(Leito::isAtivo).filter(l -> !l.getUuid().equals(ignorar))
                .filter(l -> codigo.equals(LeitoService.capacidadeDoTipo(l.getTipo()))).count();
    }

    private static List<TipoLeito> tipos(String codigo) {
        return Arrays.stream(TipoLeito.values()).filter(t -> codigo.equals(LeitoService.capacidadeDoTipo(t))).toList();
    }

    private UnidadeDeSaude unidade(UUID id) {
        return unidadeRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar uma unidade de saúde com o id " + id + " em nossos registros."));
    }
}
