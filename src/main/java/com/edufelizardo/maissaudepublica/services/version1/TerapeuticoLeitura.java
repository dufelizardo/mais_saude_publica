package com.edufelizardo.maissaudepublica.services.version1;

import com.edufelizardo.maissaudepublica.models.IntervencaoPrevista;
import com.edufelizardo.maissaudepublica.models.IntervencaoSaudeMental;
import com.edufelizardo.maissaudepublica.models.ObjetivoTerapeutico;
import com.edufelizardo.maissaudepublica.models.PessoaRedeApoio;
import com.edufelizardo.maissaudepublica.models.ProjetoTerapeuticoSingular;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.IntervencaoSaudeMentalDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.PessoaRedeApoioDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.ProjetoTerapeuticoDto;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoIntervencaoSaudeMental;
import com.edufelizardo.maissaudepublica.repositories.IntervencaoPrevistaRepository;
import com.edufelizardo.maissaudepublica.repositories.IntervencaoSaudeMentalRepository;
import com.edufelizardo.maissaudepublica.repositories.ObjetivoTerapeuticoRepository;
import com.edufelizardo.maissaudepublica.repositories.PessoaRedeApoioRepository;
import com.edufelizardo.maissaudepublica.repositories.ProjetoTerapeuticoSingularRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Leitura do que a SM2 pendura no caso em saúde mental (ADR-0121): versões do PTS, intervenções e rede de apoio. Só
 * repositórios, para o {@link SaudeMentalService} montar o detalhe e os indicadores sem depender das escritas, que ficam
 * no {@link ProjetoTerapeuticoService}.
 */
@Component
public class TerapeuticoLeitura {

    @Autowired
    private ProjetoTerapeuticoSingularRepository ptsRepository;

    @Autowired
    private ObjetivoTerapeuticoRepository objetivoRepository;

    @Autowired
    private IntervencaoPrevistaRepository previstaRepository;

    @Autowired
    private IntervencaoSaudeMentalRepository intervencaoRepository;

    @Autowired
    private PessoaRedeApoioRepository redeRepository;

    /** As versões vigentes do PTS por caso, para "Sem PTS" e "PTS a revisar". */
    public Map<UUID, ProjetoTerapeuticoSingular> ptsVigentes(Collection<UUID> casos) {
        if (casos.isEmpty()) {
            return Map.of();
        }
        Map<UUID, ProjetoTerapeuticoSingular> m = new HashMap<>();
        for (ProjetoTerapeuticoSingular p : ptsRepository.findVigentes(casos)) {
            m.put(p.getAcompanhamento().getUuid(), p);
        }
        return m;
    }

    public static boolean revisaoVencida(ProjetoTerapeuticoSingular p) {
        return p != null && p.vigente() && p.getRevisarEm().isBefore(LocalDate.now());
    }

    /** Todas as versões, a vigente primeiro. */
    public List<ProjetoTerapeuticoDto> versoes(UUID caso) {
        List<ProjetoTerapeuticoSingular> versoes = ptsRepository.findDoAcompanhamento(caso);
        if (versoes.isEmpty()) {
            return List.of();
        }
        List<UUID> ids = versoes.stream().map(ProjetoTerapeuticoSingular::getUuid).toList();
        Map<UUID, List<ObjetivoTerapeutico>> objetivos = objetivoRepository.findDosPts(ids).stream()
                .collect(Collectors.groupingBy(o -> o.getPts().getUuid()));
        Map<UUID, List<IntervencaoPrevista>> previstas = previstaRepository.findDosPts(ids).stream()
                .collect(Collectors.groupingBy(i -> i.getPts().getUuid()));
        return versoes.stream().map(p -> new ProjetoTerapeuticoDto(p.getUuid(), p.getVersao(), p.vigente(), p.getNecessidades(),
                p.getPeriodicidadeRevisao(), p.getRevisarEm(), revisaoVencida(p), p.isPactuadoComPessoa(), p.getPactuacaoObservacao(),
                p.getSinteseRevisao(), p.getElaboradoEm(), p.getProfissional() != null ? p.getProfissional().getNome() : null,
                objetivos.getOrDefault(p.getUuid(), List.of()).stream().map(o -> new ProjetoTerapeuticoDto.Objetivo(o.getUuid(),
                        o.getDescricao(), o.getMeta(), o.getPrazo(),
                        o.getResponsavel() != null ? o.getResponsavel().getMatricula() : null,
                        o.getResponsavel() != null ? o.getResponsavel().getNome() : null, o.getSituacao())).toList(),
                previstas.getOrDefault(p.getUuid(), List.of()).stream().map(i -> new ProjetoTerapeuticoDto.Prevista(i.getUuid(),
                        i.getTipo().getUuid(), i.getTipo().getNome(), i.getFrequencia(),
                        i.getResponsavel() != null ? i.getResponsavel().getMatricula() : null,
                        i.getResponsavel() != null ? i.getResponsavel().getNome() : null, i.getServico())).toList())).toList();
    }

    public List<IntervencaoSaudeMentalDto> intervencoes(UUID caso) {
        return intervencaoRepository.findDoAcompanhamento(caso).stream().map(i -> new IntervencaoSaudeMentalDto(i.getUuid(),
                i.getTipo().getUuid(), i.getTipo().getNome(), i.getRealizadaEm(), i.getSituacao(),
                i.getProfissional() != null ? i.getProfissional().getNome() : null,
                i.getCompartilhadoCom() != null ? i.getCompartilhadoCom().getNome() : null,
                i.getPrevista() != null ? i.getPrevista().getUuid() : null, i.getResumo())).toList();
    }

    /** Quantas das intervenções mais recentes, em sequência, foram faltas (sugere marcar faltoso). */
    public static int faltasSeguidas(List<IntervencaoSaudeMentalDto> intervencoes) {
        int n = 0;
        for (IntervencaoSaudeMentalDto i : intervencoes) {
            if (i.getSituacao() != SituacaoIntervencaoSaudeMental.NAO_COMPARECEU) {
                break;
            }
            n++;
        }
        return n;
    }

    public List<PessoaRedeApoioDto> redeApoio(UUID caso) {
        return redeRepository.findDoAcompanhamento(caso).stream().map(p -> new PessoaRedeApoioDto(p.getUuid(), p.getNome(), p.getPapel(),
                p.getVinculo(), p.getTelefone(), p.getObservacao(), p.getInicio(), p.getFim(), p.getMotivoFim())).toList();
    }

    /** Registrou algo da SM2 no caso (versão do PTS, intervenção ou rede de apoio): conta como autor para o sigilo. */
    public boolean autor(UUID caso, String cpf) {
        return ptsRepository.findDoAcompanhamento(caso).stream().anyMatch(p -> cpf.equals(p.getRegistradoPorCpf()))
                || intervencaoRepository.findDoAcompanhamento(caso).stream().anyMatch(i -> cpf.equals(i.getRegistradoPorCpf()))
                || redeRepository.findDoAcompanhamento(caso).stream().anyMatch(p -> cpf.equals(p.getRegistradoPorCpf()));
    }

    /** Uma pessoa da rede de apoio, para o encerramento. */
    public PessoaRedeApoio pessoa(UUID uuid) {
        return redeRepository.findById(uuid).orElse(null);
    }
}
