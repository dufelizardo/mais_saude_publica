package com.edufelizardo.maissaudepublica.services.version1;

import com.edufelizardo.maissaudepublica.models.Area;
import com.edufelizardo.maissaudepublica.models.AtribuicaoMicroarea;
import com.edufelizardo.maissaudepublica.models.MembroEquipe;
import com.edufelizardo.maissaudepublica.models.Microarea;
import com.edufelizardo.maissaudepublica.models.ResponsabilidadeTerritorial;
import com.edufelizardo.maissaudepublica.repositories.AtribuicaoMicroareaRepository;
import com.edufelizardo.maissaudepublica.repositories.MicroareaRepository;
import com.edufelizardo.maissaudepublica.repositories.ResponsabilidadeTerritorialRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * O que a equipe sabe do Território (ADR-0125), sem o {@link EquipeService} depender do {@link TerritorioService}: a
 * área vigente, as microáreas e a microárea de cada ACS. Com área, o Território é a fonte dos antigos textos
 * {@code Equipe.microareas} e {@code MembroEquipe.microarea}.
 */
@Component
class TerritorioDaEquipe {

    /** A área vigente da equipe, os códigos das microáreas e a microárea de cada participação (vigente ou a última). */
    record Vinculo(Area area, List<String> microareas, Map<UUID, String> microareaPorMembro) {
        String microareasTexto() {
            return microareas.isEmpty() ? null : String.join(", ", microareas);
        }
    }

    @Autowired
    private ResponsabilidadeTerritorialRepository responsabilidadeRepository;

    @Autowired
    private MicroareaRepository microareaRepository;

    @Autowired
    private AtribuicaoMicroareaRepository atribuicaoRepository;

    Optional<Vinculo> daEquipe(UUID equipeId, Collection<UUID> membroIds) {
        return responsabilidadeRepository.findByEquipe_UuidAndFimIsNull(equipeId).map(r -> {
            List<String> codigos = microareaRepository.findByArea_UuidOrderByCodigoAsc(r.getArea().getUuid()).stream()
                    .map(Microarea::getCodigo).toList();
            Map<UUID, String> porMembro = new HashMap<>();
            if (!membroIds.isEmpty()) {
                // Do mais recente ao mais antigo: fica a vigente, ou a última de quem já saiu.
                for (AtribuicaoMicroarea a : atribuicaoRepository.findByMembro_UuidInOrderByInicioDesc(List.copyOf(membroIds))) {
                    UUID membro = a.getMembro().getUuid();
                    if (a.vigente() || !porMembro.containsKey(membro)) {
                        porMembro.put(membro, a.getMicroarea().getCodigo());
                    }
                }
            }
            return new Vinculo(r.getArea(), codigos, porMembro);
        });
    }

    /** Para a lista de equipes: o texto das microáreas de cada equipe com área vigente. */
    Map<UUID, String> microareasPorEquipe() {
        Map<UUID, String> porEquipe = new HashMap<>();
        Map<UUID, List<String>> porArea = microareaRepository.findAllByOrderByCodigoAsc().stream()
                .collect(Collectors.groupingBy(m -> m.getArea().getUuid(), Collectors.mapping(Microarea::getCodigo, Collectors.toList())));
        for (ResponsabilidadeTerritorial r : responsabilidadeRepository.findByFimIsNull()) {
            List<String> codigos = porArea.getOrDefault(r.getArea().getUuid(), List.of());
            porEquipe.put(r.getEquipe().getUuid(), codigos.isEmpty() ? null : String.join(", ", codigos));
        }
        return porEquipe;
    }

    /** O ACS saiu da equipe: a microárea fica sem ele na mesma data. */
    void encerrarDoMembro(MembroEquipe membro, LocalDate fim, String motivo) {
        atribuicaoRepository.findByMembro_UuidAndFimIsNull(membro.getUuid()).ifPresent(a -> {
            a.setFim(fim.isBefore(a.getInicio()) ? a.getInicio() : fim);
            String texto = "Saída da equipe: " + motivo;
            a.setMotivoFim(texto.length() > 500 ? texto.substring(0, 500) : texto);
            atribuicaoRepository.save(a);
        });
    }
}
