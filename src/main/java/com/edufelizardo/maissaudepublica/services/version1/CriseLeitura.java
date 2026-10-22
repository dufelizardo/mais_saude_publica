package com.edufelizardo.maissaudepublica.services.version1;

import com.edufelizardo.maissaudepublica.models.AvaliacaoRisco;
import com.edufelizardo.maissaudepublica.models.EtapaCrise;
import com.edufelizardo.maissaudepublica.models.EventoCrise;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.AvaliacaoRiscoDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.EventoCriseDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.InternacaoResumoSaudeMentalDto;
import com.edufelizardo.maissaudepublica.models.enuns.NivelRiscoSaudeMental;
import com.edufelizardo.maissaudepublica.models.enuns.TipoRiscoSaudeMental;
import com.edufelizardo.maissaudepublica.repositories.AvaliacaoRiscoRepository;
import com.edufelizardo.maissaudepublica.repositories.EtapaCriseRepository;
import com.edufelizardo.maissaudepublica.repositories.EventoCriseRepository;
import com.edufelizardo.maissaudepublica.repositories.InternacaoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Collection;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Leitura do que a SM3 pendura no caso em saúde mental (ADR-0123): risco com histórico, crises e as internações do
 * paciente. Só repositórios, para o {@link SaudeMentalService} montar o detalhe e os indicadores sem depender das
 * escritas, que ficam no {@link RiscoCriseService}.
 */
@Component
public class CriseLeitura {

    @Autowired
    private AvaliacaoRiscoRepository riscoRepository;

    @Autowired
    private EventoCriseRepository criseRepository;

    @Autowired
    private EtapaCriseRepository etapaRepository;

    @Autowired
    private InternacaoRepository internacaoRepository;

    /** Indicadores de risco por caso: [risco alto vigente, reavaliação vencida]. */
    public Map<UUID, boolean[]> indicadores(Collection<UUID> casos) {
        if (casos.isEmpty()) {
            return Map.of();
        }
        Map<UUID, Map<TipoRiscoSaudeMental, AvaliacaoRisco>> vigentes = new HashMap<>();
        for (AvaliacaoRisco a : riscoRepository.findDosAcompanhamentos(casos)) {
            vigentes.computeIfAbsent(a.getAcompanhamento().getUuid(), k -> new EnumMap<>(TipoRiscoSaudeMental.class)).putIfAbsent(a.getTipo(), a);
        }
        Map<UUID, boolean[]> m = new HashMap<>();
        vigentes.forEach((caso, porTipo) -> m.put(caso, new boolean[]{
                porTipo.values().stream().anyMatch(a -> a.getNivel() == NivelRiscoSaudeMental.ALTO),
                porTipo.values().stream().anyMatch(CriseLeitura::vencida)}));
        return m;
    }

    /** O histórico de risco do caso, o mais recente primeiro, com os vigentes (o mais recente de cada tipo) marcados. */
    public List<AvaliacaoRiscoDto> riscos(UUID caso) {
        Set<TipoRiscoSaudeMental> vistos = new HashSet<>();
        return riscoRepository.findDoAcompanhamento(caso).stream().map(a -> {
            boolean vigente = vistos.add(a.getTipo());
            return new AvaliacaoRiscoDto(a.getUuid(), a.getTipo(), a.getNivel(), a.getFatores(), a.getMedidas(), a.isPlanoSeguranca(),
                    a.getFrequenciaContato(), a.getReavaliarEm(), vigente && vencida(a), a.getAvaliadaEm(),
                    a.getProfissional() != null ? a.getProfissional().getNome() : null, vigente);
        }).toList();
    }

    /** As crises ligadas ao caso, com as etapas. */
    public List<EventoCriseDto> crisesDoCaso(UUID pacienteId, UUID caso) {
        return criseRepository.findDoPaciente(pacienteId).stream()
                .filter(c -> c.getAcompanhamento() != null && c.getAcompanhamento().getUuid().equals(caso))
                .map(c -> dto(c, true, etapaRepository.findDaCrise(c.getUuid()))).toList();
    }

    public List<InternacaoResumoSaudeMentalDto> internacoes(UUID pacienteId) {
        return internacaoRepository.findByPaciente_UuidOrderByAdmitidaEmDesc(pacienteId).stream()
                .map(i -> new InternacaoResumoSaudeMentalDto(i.getUuid(), i.getUnidade().getNome(), i.getAdmitidaEm(), i.getAltaEm(),
                        i.getStatus(), i.getModalidade())).toList();
    }

    private static boolean vencida(AvaliacaoRisco a) {
        return a.getNivel() != NivelRiscoSaudeMental.SEM_RISCO_ATUAL && a.getReavaliarEm() != null && a.getReavaliarEm().isBefore(LocalDate.now());
    }

    static EventoCriseDto dto(EventoCrise c, boolean liberado, List<EtapaCrise> etapas) {
        boolean pendente = c.isNotificacaoSugerida() && c.getNotificadaEm() == null;
        if (!liberado) {
            return new EventoCriseDto(c.getUuid(), c.getPaciente().getUuid(), c.getPaciente().getNome(), c.getUnidade().getUuid(),
                    c.getUnidade().getNome(), c.getAcompanhamento() != null ? c.getAcompanhamento().getUuid() : null, c.getIdentificadaEm(),
                    c.getSituacao(), false, null, null, null, null, null, false, pendente, null, null, null, List.of());
        }
        return new EventoCriseDto(c.getUuid(), c.getPaciente().getUuid(), c.getPaciente().getNome(), c.getUnidade().getUuid(),
                c.getUnidade().getNome(), c.getAcompanhamento() != null ? c.getAcompanhamento().getUuid() : null, c.getIdentificadaEm(),
                c.getSituacao(), true, c.getTipo(), c.getLocalOcorrencia(), c.getDescricao(), c.getDesfecho(), c.getEncerradaEm(),
                c.isNotificacaoSugerida(), pendente, c.getNotificadaEm(), c.getNotificacaoNumero(),
                c.getProfissional() != null ? c.getProfissional().getNome() : null,
                etapas.stream().map(e -> new EventoCriseDto.Etapa(e.getUuid(), e.getEtapa(), e.getDescricao(), e.getOcorridaEm(),
                        e.getProfissional() != null ? e.getProfissional().getNome() : null)).toList());
    }
}
