package com.edufelizardo.maissaudepublica.services.version1;

import com.edufelizardo.maissaudepublica.models.AtribuicaoAcesso;
import com.edufelizardo.maissaudepublica.models.Permissao;
import com.edufelizardo.maissaudepublica.models.UnidadeDeSaude;
import com.edufelizardo.maissaudepublica.repositories.AtribuicaoAcessoRepository;
import com.edufelizardo.maissaudepublica.repositories.UnidadeDeSaudeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

/**
 * Responde "este usuário pode fazer X nesta unidade?" a partir das atribuições vigentes (ADR-0066).
 * Uma atribuição vale para a unidade do escopo e para as que estão abaixo dela — pela unidade superior
 * ({@code unidadeSuperior}) ou pela supervisão regional ({@code supervisaoRegional}); sem unidade, vale
 * para a rede inteira.
 *
 * <p>Nesta fatia o serviço só calcula; a aplicação nas rotas vem na seguinte.
 */
@Service
public class AutorizacaoService {

    @Autowired
    private AtribuicaoAcessoRepository atribuicaoRepository;

    @Autowired
    private UnidadeDeSaudeRepository unidadeDeSaudeRepository;

    /** Tem a permissão em algum escopo? */
    @Transactional(readOnly = true)
    public boolean tem(String cpf, String permissao) {
        return vigentes(cpf).stream().anyMatch(a -> concede(a, permissao));
    }

    /** Tem a permissão num escopo que cobre a unidade? */
    @Transactional(readOnly = true)
    public boolean tem(String cpf, String permissao, UUID unidadeId) {
        if (unidadeId == null) {
            return tem(cpf, permissao);
        }
        Set<UUID> acima = unidadeEAcima(unidadeId);
        return vigentes(cpf).stream()
                .filter(a -> concede(a, permissao))
                .anyMatch(a -> a.getUnidade() == null || acima.contains(a.getUnidade().getUuid()));
    }

    /** Códigos das permissões vigentes, em qualquer escopo. */
    @Transactional(readOnly = true)
    public Set<String> permissoes(String cpf) {
        Set<String> codigos = new TreeSet<>();
        for (AtribuicaoAcesso a : vigentes(cpf)) {
            if (a.getPapel().isAtivo()) {
                a.getPapel().getPermissoes().stream().map(Permissao::getCodigo).forEach(codigos::add);
            }
        }
        return codigos;
    }

    @Transactional(readOnly = true)
    public List<AtribuicaoAcesso> vigentes(String cpf) {
        LocalDate hoje = LocalDate.now();
        return atribuicaoRepository.findByUsuarioCpf(cpf).stream().filter(a -> a.vigenteEm(hoje)).toList();
    }

    private static boolean concede(AtribuicaoAcesso a, String permissao) {
        return a.getPapel().isAtivo()
                && a.getPapel().getPermissoes().stream().anyMatch(p -> p.getCodigo().equals(permissao));
    }

    /** A unidade e tudo o que está acima dela (unidade superior e supervisão regional, recursivamente). */
    Set<UUID> unidadeEAcima(UUID unidadeId) {
        Set<UUID> resultado = new HashSet<>();
        List<UnidadeDeSaude> fila = new java.util.ArrayList<>();
        unidadeDeSaudeRepository.findById(unidadeId).ifPresent(fila::add);
        while (!fila.isEmpty()) {
            UnidadeDeSaude u = fila.remove(0);
            if (!resultado.add(u.getUuid())) {
                continue;
            }
            if (u.getUnidadeSuperior() != null) {
                fila.add(u.getUnidadeSuperior());
            }
            if (u.getSupervisaoRegional() != null) {
                fila.add(u.getSupervisaoRegional());
            }
        }
        return resultado;
    }
}
