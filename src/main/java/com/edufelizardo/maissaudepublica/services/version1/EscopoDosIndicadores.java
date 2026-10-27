package com.edufelizardo.maissaudepublica.services.version1;

import com.edufelizardo.maissaudepublica.exceptions.ResourceForbiddenException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceNotFoundException;
import com.edufelizardo.maissaudepublica.models.UnidadeDeSaude;
import com.edufelizardo.maissaudepublica.repositories.UnidadeDeSaudeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * A abrangência de uma consulta de indicadores (ADR-0142): a unidade pedida, com INDICADOR.CONSULTAR num escopo que a
 * cubra; ou, sem unidade, as unidades do acesso (a rede inteira para quem tem acesso na rede).
 */
@Component
public class EscopoDosIndicadores {

    public static final String CONSULTAR = "INDICADOR.CONSULTAR";

    @Autowired
    private UnidadeDeSaudeRepository unidadeRepository;

    @Autowired
    private ControleDeAcesso controleDeAcesso;

    /** A unidade pedida, ou as do acesso ({@code unidades} nulo = todas). */
    public record Escopo(UnidadeDeSaude unidade, Set<UUID> unidades, String abrangencia) {

        /** O registro sem unidade não entra em nenhuma abrangência (ADR-0143). */
        public boolean contem(UUID unidadeId) {
            return unidadeId != null && (unidades == null || unidades.contains(unidadeId));
        }

        public UUID unidadeId() {
            return unidade == null ? null : unidade.getUuid();
        }
    }

    public Escopo de(UUID unidadeId) {
        if (unidadeId != null) {
            UnidadeDeSaude u = unidadeRepository.findById(unidadeId).orElseThrow(() -> new ResourceNotFoundException(
                    "Não foi possível encontrar a unidade " + unidadeId + " em nossos registros."));
            controleDeAcesso.exigir(CONSULTAR, u);
            return new Escopo(u, Set.of(u.getUuid()), u.getNome());
        }
        controleDeAcesso.exigirAlguma(CONSULTAR);
        Optional<Set<UUID>> visiveis = controleDeAcesso.unidadesVisiveis(CONSULTAR);
        if (visiveis.isEmpty()) {
            return new Escopo(null, null, "Rede municipal");
        }
        if (visiveis.get().isEmpty()) {
            throw new ResourceForbiddenException("Seu acesso não inclui " + CONSULTAR + " em nenhuma unidade.");
        }
        return new Escopo(null, visiveis.get(), "Unidades do seu acesso");
    }
}
