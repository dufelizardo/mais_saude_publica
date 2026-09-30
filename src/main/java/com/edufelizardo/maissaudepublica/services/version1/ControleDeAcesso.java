package com.edufelizardo.maissaudepublica.services.version1;

import com.edufelizardo.maissaudepublica.config.UsuarioAutenticado;
import com.edufelizardo.maissaudepublica.exceptions.ResourceForbiddenException;
import com.edufelizardo.maissaudepublica.models.UnidadeDeSaude;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * Aplicação da autorização (ADR-0067) sobre o cálculo do {@link AutorizacaoService} (ADR-0066). Só age
 * com login e autorização ligados ({@code app.security.enabled} e {@code app.security.authorization.enabled});
 * desligada, tudo passa — o comportamento de local, CI e dos ambientes que ainda não ligaram.
 */
@Service
public class ControleDeAcesso {

    public static final String RETIFICAR_DE_OUTROS = "REGISTRO_CLINICO.RETIFICAR_DE_OUTROS";

    @Value("${app.security.enabled:false}")
    private boolean autenticacaoLigada;

    @Value("${app.security.authorization.enabled:false}")
    private boolean autorizacaoLigada;

    @Autowired
    private AutorizacaoService autorizacaoService;

    public boolean ativo() {
        return autenticacaoLigada && autorizacaoLigada;
    }

    /** Alguma das permissões, em qualquer escopo (conferência da rota). */
    public void exigirAlguma(String... permissoes) {
        if (!ativo()) {
            return;
        }
        String cpf = UsuarioAutenticado.cpf();
        if (cpf == null || Arrays.stream(permissoes).noneMatch(p -> autorizacaoService.tem(cpf, p))) {
            throw new ResourceForbiddenException("Seu acesso não inclui esta operação (" + String.join(" ou ", permissoes) + ").");
        }
    }

    /** A permissão num escopo que cubra a unidade; sem unidade, na rede inteira. */
    public void exigir(String permissao, UnidadeDeSaude unidade) {
        if (!ativo()) {
            return;
        }
        String cpf = UsuarioAutenticado.cpf();
        UUID unidadeId = unidade != null ? unidade.getUuid() : null;
        boolean pode = cpf != null && (unidadeId == null
                ? autorizacaoService.temNaRedeInteira(cpf, permissao)
                : autorizacaoService.tem(cpf, permissao, unidadeId));
        if (!pode) {
            throw new ResourceForbiddenException("Seu acesso não inclui " + permissao + " "
                    + (unidadeId == null ? "na rede inteira." : "na unidade " + unidade.getNome() + "."));
        }
    }

    /**
     * Retificação de registro clínico (ADR-0062): o próprio autor, ou quem supervisiona a unidade
     * ({@value #RETIFICAR_DE_OUTROS}). Registro sem autor gravado (anterior ao login) só pela supervisão.
     */
    public void exigirAutoriaOuSupervisao(String autorCpf, UnidadeDeSaude unidade) {
        if (!ativo()) {
            return;
        }
        String cpf = UsuarioAutenticado.cpf();
        if (cpf != null && cpf.equals(autorCpf)) {
            return;
        }
        if (cpf == null || !autorizacaoService.tem(cpf, RETIFICAR_DE_OUTROS, unidade != null ? unidade.getUuid() : null)) {
            throw new ResourceForbiddenException("Só quem registrou pode retificar este registro, ou a supervisão da unidade ("
                    + RETIFICAR_DE_OUTROS + ").");
        }
    }

    /**
     * Unidades onde o usuário tem alguma das permissões, com as de baixo. Vazio = sem restrição (autorização
     * desligada ou acesso na rede inteira).
     */
    public Optional<Set<UUID>> unidadesVisiveis(String... permissoes) {
        if (!ativo()) {
            return Optional.empty();
        }
        String cpf = UsuarioAutenticado.cpf();
        return cpf == null ? Optional.of(Set.of()) : autorizacaoService.unidadesCobertas(cpf, permissoes);
    }

    /** Filtra uma lista pelas unidades visíveis; um item vale se alguma das suas unidades é visível. */
    public <T> Stream<T> filtrar(Stream<T> itens, Function<T, Stream<UnidadeDeSaude>> unidades, String... permissoes) {
        Optional<Set<UUID>> visiveis = unidadesVisiveis(permissoes);
        if (visiveis.isEmpty()) {
            return itens;
        }
        Set<UUID> ids = visiveis.get();
        return itens.filter(i -> unidades.apply(i).filter(Objects::nonNull).anyMatch(u -> ids.contains(u.getUuid())));
    }

    /** Confere que o registro está numa unidade visível; fora dela, 403. */
    public void exigirVisivel(UnidadeDeSaude unidade, String... permissoes) {
        Optional<Set<UUID>> visiveis = unidadesVisiveis(permissoes);
        if (visiveis.isPresent() && (unidade == null || !visiveis.get().contains(unidade.getUuid()))) {
            throw new ResourceForbiddenException("Este registro é de uma unidade fora do seu acesso.");
        }
    }
}
