package com.edufelizardo.maissaudepublica.services.version1;

import com.edufelizardo.maissaudepublica.exceptions.ResourceForbiddenException;
import com.edufelizardo.maissaudepublica.models.AcompanhamentoSaudeMental;
import com.edufelizardo.maissaudepublica.models.IntervencaoSaudeMental;
import com.edufelizardo.maissaudepublica.models.UnidadeCapacidadeAssistencial;
import com.edufelizardo.maissaudepublica.models.UnidadeDeSaude;
import com.edufelizardo.maissaudepublica.models.UnidadePerfilOperacional;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.ContagemProducaoDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.PainelRapsDto;
import com.edufelizardo.maissaudepublica.models.enuns.ComponenteRaps;
import com.edufelizardo.maissaudepublica.repositories.AcompanhamentoSaudeMentalRepository;
import com.edufelizardo.maissaudepublica.repositories.IntervencaoSaudeMentalRepository;
import com.edufelizardo.maissaudepublica.repositories.UnidadeCapacidadeAssistencialRepository;
import com.edufelizardo.maissaudepublica.repositories.UnidadePerfilOperacionalRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * A Rede de Atenção Psicossocial (SM5, ADR-0153). Os componentes vêm do modelo operacional: o perfil e as capacidades
 * vigentes de cada unidade dizem em que componente ela está, e uma unidade pode estar em vários (o hospital com leitos de
 * saúde mental está no hospitalar sem virar "unidade de saúde mental"). Com isso, os casos em acompanhamento e as
 * intervenções dos últimos 90 dias de cada ponto, e as lacunas da rede. Só números.
 */
@Service
public class RedePsicossocialService {

    static final String[] LEITURA = {EscopoDosIndicadores.CONSULTAR, SaudeMentalService.ACOMPANHAR};
    private static final Duration JANELA = Duration.ofDays(90);

    @Autowired private UnidadePerfilOperacionalRepository perfilRepository;
    @Autowired private UnidadeCapacidadeAssistencialRepository capacidadeRepository;
    @Autowired private AcompanhamentoSaudeMentalRepository acompanhamentoRepository;
    @Autowired private IntervencaoSaudeMentalRepository intervencaoRepository;
    @Autowired private ControleDeAcesso controleDeAcesso;

    @Transactional(readOnly = true)
    public PainelRapsDto painel() {
        controleDeAcesso.exigirAlguma(LEITURA);
        Optional<Set<UUID>> visiveis = controleDeAcesso.unidadesVisiveis(LEITURA);
        if (visiveis.isPresent() && visiveis.get().isEmpty()) {
            throw new ResourceForbiddenException("Seu acesso não inclui a rede de atenção psicossocial em nenhuma unidade.");
        }
        Predicate<UnidadeDeSaude> naAbrangencia = u -> u.isAtivo() && (visiveis.isEmpty() || visiveis.get().contains(u.getUuid()));

        // Componente → unidade → as capacidades dela nele (o perfil põe a unidade no componente sem capacidade a listar).
        Map<ComponenteRaps, Map<UUID, Set<String>>> pontos = new EnumMap<>(ComponenteRaps.class);
        Map<UUID, UnidadeDeSaude> unidades = new HashMap<>();
        Set<String> codigos = new HashSet<>();
        for (UnidadePerfilOperacional p : perfilRepository.findVigentesComPerfil()) {
            if (p.getPerfil().getComponenteRaps() != null && naAbrangencia.test(p.getUnidade())) {
                unidades.put(p.getUnidade().getUuid(), p.getUnidade());
                pontos.computeIfAbsent(p.getPerfil().getComponenteRaps(), k -> new LinkedHashMap<>())
                        .computeIfAbsent(p.getUnidade().getUuid(), k -> new TreeSet<>());
            }
        }
        for (UnidadeCapacidadeAssistencial c : capacidadeRepository.findVigentesComCapacidade()) {
            if (c.getCapacidade().getComponenteRaps() != null && naAbrangencia.test(c.getUnidade())) {
                unidades.put(c.getUnidade().getUuid(), c.getUnidade());
                codigos.add(c.getCapacidade().getCodigo());
                pontos.computeIfAbsent(c.getCapacidade().getComponenteRaps(), k -> new LinkedHashMap<>())
                        .computeIfAbsent(c.getUnidade().getUuid(), k -> new TreeSet<>()).add(c.getCapacidade().getNome());
            }
        }
        Map<UUID, Long> casos = acompanhamentoRepository.findVigentes().stream()
                .collect(Collectors.groupingBy(a -> a.getUnidade().getUuid(), Collectors.counting()));
        Map<UUID, Map<String, Long>> intervencoes = new HashMap<>();
        for (IntervencaoSaudeMental i : intervencaoRepository.findRealizadasDesde(Instant.now().minus(JANELA))) {
            intervencoes.computeIfAbsent(i.getAcompanhamento().getUnidade().getUuid(), k -> new TreeMap<>()).merge(i.getTipo().getNome(), 1L, Long::sum);
        }

        List<PainelRapsDto.Componente> componentes = new ArrayList<>();
        List<String> lacunas = new ArrayList<>();
        for (ComponenteRaps c : ComponenteRaps.values()) {
            Map<UUID, Set<String>> doComponente = pontos.getOrDefault(c, Map.of());
            if (doComponente.isEmpty()) {
                lacunas.add("Nenhum ponto de " + c.rotulo().toLowerCase(Locale.ROOT) + ".");
            }
            List<PainelRapsDto.Ponto> lista = doComponente.entrySet().stream().map(e -> {
                UnidadeDeSaude u = unidades.get(e.getKey());
                List<ContagemProducaoDto> feitas = intervencoes.getOrDefault(u.getUuid(), Map.of()).entrySet().stream()
                        .map(x -> new ContagemProducaoDto(x.getKey(), x.getKey(), x.getValue()))
                        .sorted(Comparator.comparingLong(ContagemProducaoDto::getQuantidade).reversed()).toList();
                return new PainelRapsDto.Ponto(u.getUuid(), u.getNome(), u.getTipo() != null ? u.getTipo().name() : null, List.copyOf(e.getValue()),
                        casos.getOrDefault(u.getUuid(), 0L), feitas);
            }).sorted(Comparator.comparing(PainelRapsDto.Ponto::getUnidadeNome, String.CASE_INSENSITIVE_ORDER)).toList();
            componentes.add(new PainelRapsDto.Componente(c, c.rotulo(), lista));
        }
        lacunas.addAll(lacunasDeCaps(codigos));
        return new PainelRapsDto(visiveis.isEmpty() ? "Rede municipal" : "Unidades do seu acesso", componentes, lacunas);
    }

    /** As modalidades de CAPS que a rede não tem: para adultos (I, II ou III), infantojuvenil e álcool e drogas (AD ou AD III). */
    static List<String> lacunasDeCaps(Set<String> codigos) {
        List<String> lacunas = new ArrayList<>();
        if (codigos.stream().noneMatch(c -> c.equals("CAPS_I") || c.equals("CAPS_II") || c.equals("CAPS_III"))) {
            lacunas.add("Sem CAPS para adultos (I, II ou III).");
        }
        if (!codigos.contains("CAPS_INFANTOJUVENIL")) {
            lacunas.add("Sem CAPS infantojuvenil.");
        }
        if (!codigos.contains("CAPS_AD") && !codigos.contains("CAPS_AD_III")) {
            lacunas.add("Sem CAPS álcool e drogas (AD ou AD III).");
        }
        return lacunas;
    }
}
