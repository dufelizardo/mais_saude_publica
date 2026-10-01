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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

/**
 * Responde "este usuário pode fazer X nesta unidade?" a partir das atribuições vigentes (ADR-0066).
 * Uma atribuição vale para a unidade do escopo e para as que estão abaixo dela — pela unidade superior
 * ({@code unidadeSuperior}) ou pela supervisão regional ({@code supervisaoRegional}); sem unidade, vale
 * para a rede inteira.
 *
 * <p>A aplicação nas rotas e nos serviços fica no {@link ControleDeAcesso} (ADR-0067).
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

    /** Tem a permissão num acesso sem unidade (rede inteira)? */
    @Transactional(readOnly = true)
    public boolean temNaRedeInteira(String cpf, String permissao) {
        return vigentes(cpf).stream().anyMatch(a -> a.getUnidade() == null && concede(a, permissao));
    }

    /**
     * Unidades cobertas por alguma das permissões: as dos acessos e todas abaixo delas. Vazio = rede
     * inteira (algum acesso sem unidade).
     */
    @Transactional(readOnly = true)
    public Optional<Set<UUID>> unidadesCobertas(String cpf, String... permissoes) {
        Set<UUID> raizes = new HashSet<>();
        for (AtribuicaoAcesso a : vigentes(cpf)) {
            if (Arrays.stream(permissoes).noneMatch(p -> concede(a, p))) {
                continue;
            }
            if (a.getUnidade() == null) {
                return Optional.empty();
            }
            raizes.add(a.getUnidade().getUuid());
        }
        if (raizes.isEmpty()) {
            return Optional.of(Set.of());
        }
        Map<UUID, List<UUID>> filhas = new HashMap<>();
        for (UnidadeDeSaude u : unidadeDeSaudeRepository.findAll()) {
            if (u.getUnidadeSuperior() != null) {
                filhas.computeIfAbsent(u.getUnidadeSuperior().getUuid(), k -> new ArrayList<>()).add(u.getUuid());
            }
            if (u.getSupervisaoRegional() != null) {
                filhas.computeIfAbsent(u.getSupervisaoRegional().getUuid(), k -> new ArrayList<>()).add(u.getUuid());
            }
        }
        Set<UUID> cobertas = new HashSet<>();
        List<UUID> fila = new ArrayList<>(raizes);
        while (!fila.isEmpty()) {
            UUID id = fila.remove(fila.size() - 1);
            if (cobertas.add(id)) {
                fila.addAll(filhas.getOrDefault(id, List.of()));
            }
        }
        return Optional.of(cobertas);
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
        List<UnidadeDeSaude> fila = new ArrayList<>();
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
