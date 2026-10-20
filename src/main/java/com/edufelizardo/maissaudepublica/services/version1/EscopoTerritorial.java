package com.edufelizardo.maissaudepublica.services.version1;

import com.edufelizardo.maissaudepublica.config.UsuarioAutenticado;
import com.edufelizardo.maissaudepublica.exceptions.ResourceForbiddenException;
import com.edufelizardo.maissaudepublica.models.Adscricao;
import com.edufelizardo.maissaudepublica.models.Area;
import com.edufelizardo.maissaudepublica.models.Equipe;
import com.edufelizardo.maissaudepublica.models.MembroEquipe;
import com.edufelizardo.maissaudepublica.models.Microarea;
import com.edufelizardo.maissaudepublica.models.enuns.FuncaoEquipe;
import com.edufelizardo.maissaudepublica.repositories.AtribuicaoMicroareaRepository;
import com.edufelizardo.maissaudepublica.repositories.MembroEquipeRepository;
import com.edufelizardo.maissaudepublica.repositories.MicroareaRepository;
import com.edufelizardo.maissaudepublica.repositories.ResponsabilidadeTerritorialRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Escopo territorial (ADR-0129, F4 da ADR-0108): uma dimensão a mais do escopo, somada a unidade, papel e permissão, atrás
 * de {@code app.security.escopo-territorial.enabled}. Com ele ligado, o cadastro territorial de quem o faz pela equipe
 * fica no território dela: o ACS nas microáreas atribuídas a ele, os outros membros na área da equipe. Quem gere o
 * território ({@value TerritorioService#GERENCIAR}) não tem restrição territorial. Microárea não é autorização: só
 * estreita o que a permissão já dava.
 */
@Component
public class EscopoTerritorial {

    /** O que o usuário alcança: microáreas (ACS), áreas (outros membros) e as equipes de que é membro. */
    public record Alcance(Set<UUID> microareas, Set<UUID> areas, Set<UUID> equipes) {
        boolean vazio() {
            return microareas.isEmpty() && areas.isEmpty();
        }
    }

    @Value("${app.security.escopo-territorial.enabled:false}")
    private boolean ligado;

    @Autowired
    private ControleDeAcesso controleDeAcesso;

    @Autowired
    private AutorizacaoService autorizacaoService;

    @Autowired
    private MembroEquipeRepository membroRepository;

    @Autowired
    private AtribuicaoMicroareaRepository atribuicaoRepository;

    @Autowired
    private ResponsabilidadeTerritorialRepository responsabilidadeRepository;

    @Autowired
    private MicroareaRepository microareaRepository;

    /** O interruptor, com o login e a autorização ligados. */
    public boolean ativo() {
        return ligado && controleDeAcesso.ativo();
    }

    /** O alcance do usuário; vazio (Optional) = sem restrição territorial (desligado, ou quem gere o território). */
    public Optional<Alcance> alcance() {
        if (!ativo()) {
            return Optional.empty();
        }
        String cpf = UsuarioAutenticado.cpf();
        if (cpf != null && autorizacaoService.tem(cpf, TerritorioService.GERENCIAR)) {
            return Optional.empty();
        }
        Set<UUID> microareas = new HashSet<>();
        Set<UUID> areas = new HashSet<>();
        Set<UUID> equipes = new HashSet<>();
        List<MembroEquipe> membros = cpf == null ? List.of() : membroRepository.findByProfissional_CpfAndFimIsNull(cpf);
        for (MembroEquipe m : membros) {
            equipes.add(m.getEquipe().getUuid());
            if (m.getFuncao() == FuncaoEquipe.ACS) {
                atribuicaoRepository.findByMembro_UuidAndFimIsNull(m.getUuid()).ifPresent(a -> microareas.add(a.getMicroarea().getUuid()));
            } else {
                responsabilidadeRepository.findByEquipe_UuidAndFimIsNull(m.getEquipe().getUuid()).ifPresent(r -> areas.add(r.getArea().getUuid()));
            }
        }
        return Optional.of(new Alcance(microareas, areas, equipes));
    }

    /** Alcança a microárea (vale também para quem não tem restrição). */
    public boolean alcanca(Microarea m) {
        return alcance().map(a -> alcanca(a, m)).orElse(true);
    }

    /** Para filtrar listas com o alcance já calculado uma vez. */
    public static boolean alcanca(Alcance a, Microarea m) {
        return m != null && (a.microareas().contains(m.getUuid()) || a.areas().contains(m.getArea().getUuid()));
    }

    /** A adscrição está no alcance: a da microárea alcançada, ou a manual da equipe de quem tem a área inteira. */
    public static boolean alcanca(Alcance a, Adscricao ad) {
        return ad.getMicroarea() != null ? alcanca(a, ad.getMicroarea()) : a.equipes().contains(ad.getEquipe().getUuid()) && !a.areas().isEmpty();
    }

    /** 403 fora da microárea. */
    public void exigir(Microarea m) {
        alcance().ifPresent(a -> {
            exigirAlgum(a);
            if (!alcanca(a, m)) {
                throw new ResourceForbiddenException("A microárea " + m.getCodigo() + " da área " + m.getArea().getCodigo()
                        + " está fora do seu escopo territorial.");
            }
        });
    }

    /** 403 se a área não tem nada do alcance (o ACS entra na área da microárea dele; a lista vem filtrada). */
    public void exigir(Area area) {
        alcance().ifPresent(a -> {
            exigirAlgum(a);
            boolean temMicroarea = microareaRepository.findByArea_UuidOrderByCodigoAsc(area.getUuid()).stream()
                    .anyMatch(m -> a.microareas().contains(m.getUuid()));
            if (!a.areas().contains(area.getUuid()) && !temMicroarea) {
                throw new ResourceForbiddenException("A área " + area.getCodigo() + " está fora do seu escopo territorial.");
            }
        });
    }

    /** 403 se o usuário não é membro da equipe (adscrição manual, elegíveis). */
    public void exigir(Equipe e) {
        alcance().ifPresent(a -> {
            exigirAlgum(a);
            if (!a.equipes().contains(e.getUuid())) {
                throw new ResourceForbiddenException("A equipe " + e.getNome() + " está fora do seu escopo territorial.");
            }
        });
    }

    private static void exigirAlgum(Alcance a) {
        if (a.vazio() && a.equipes().isEmpty()) {
            throw new ResourceForbiddenException("Você não tem equipe no território: com o escopo territorial ligado, o cadastro "
                    + "é de quem é da equipe da área.");
        }
    }
}
