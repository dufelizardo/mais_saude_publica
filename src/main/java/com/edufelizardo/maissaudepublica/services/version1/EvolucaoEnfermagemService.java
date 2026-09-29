package com.edufelizardo.maissaudepublica.services.version1;

import com.edufelizardo.maissaudepublica.models.dtos.version1.request.RetificacaoEvolucaoEnfermagemRequestDto;
import com.edufelizardo.maissaudepublica.exceptions.ResourceUnprocessableEntityException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceBadRequestException;
import com.edufelizardo.maissaudepublica.config.UsuarioAutenticado;
import org.springframework.transaction.annotation.Transactional;
import java.util.Map;
import java.time.Instant;
import com.edufelizardo.maissaudepublica.exceptions.ResourceNotFoundException;
import com.edufelizardo.maissaudepublica.models.Atendimento;
import com.edufelizardo.maissaudepublica.models.EvolucaoEnfermagem;
import com.edufelizardo.maissaudepublica.models.Profissional;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.EvolucaoEnfermagemRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.EvolucaoEnfermagemResponseDto;
import com.edufelizardo.maissaudepublica.repositories.AtendimentoRepository;
import com.edufelizardo.maissaudepublica.repositories.EvolucaoEnfermagemRepository;
import com.edufelizardo.maissaudepublica.repositories.ProfissionalRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * CRUD da Evolução de Enfermagem (Enfermagem — ver MAPA-DE-DOMINIOS.md #8, ADR-0048).
 * {@code profissionalMatricula} é resolvido para o {@link Profissional} real na fronteira da API,
 * mesmo padrão do {@code TriagemService} (ver ADR-0034/ADR-0047).
 */
@Service
public class EvolucaoEnfermagemService {

    @Autowired
    private EvolucaoEnfermagemRepository evolucaoEnfermagemRepository;

    @Autowired
    private AtendimentoRepository atendimentoRepository;

    @Autowired
    private ProfissionalRepository profissionalRepository;

    @Transactional
    public EvolucaoEnfermagemResponseDto criar(EvolucaoEnfermagemRequestDto dto) {
        Atendimento atendimento = buscarAtendimentoPorId(dto.getAtendimentoId());
        Profissional profissional = buscarProfissionalPorMatricula(dto.getProfissionalMatricula());

        EvolucaoEnfermagem evolucao = new EvolucaoEnfermagem(atendimento, profissional, dto.getDataHora(),
                dto.getDescricao());
        evolucao.setRegistradoEm(Instant.now());
        evolucao.setRegistradoPorCpf(UsuarioAutenticado.cpf());
        evolucao = evolucaoEnfermagemRepository.save(evolucao);
        return EvolucaoEnfermagemResponseDto.fromEvolucaoEnfermagem(evolucao);
    }

    /**
     * Retificação (ADR-0062): grava uma nova versão ligada à anterior, que continua no prontuário. Só a
     * versão vigente pode ser retificada, e a evolução de enfermagem continua no mesmo atendimento.
     */
    @Transactional
    public EvolucaoEnfermagemResponseDto retificar(UUID uuid, RetificacaoEvolucaoEnfermagemRequestDto dto) {
        EvolucaoEnfermagem original = evolucaoEnfermagemRepository.findByIdParaRetificar(uuid)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Não foi possível encontrar uma evolução de enfermagem com o id " + uuid + " em nossos registros."));
        UUID vigente = sucessores().get(uuid);
        if (vigente != null) {
            throw new ResourceUnprocessableEntityException("Esta evolução de enfermagem já foi retificada pela versão " + vigente
                    + ": retifique a versão vigente.");
        }
        if (!original.getAtendimento().getUuid().equals(dto.getAtendimentoId())) {
            throw new ResourceBadRequestException("A retificação precisa manter o mesmo atendimento do registro original.");
        }
        Profissional profissional = buscarProfissionalPorMatricula(dto.getProfissionalMatricula());

        EvolucaoEnfermagem evolucao = new EvolucaoEnfermagem(original.getAtendimento(), profissional, dto.getDataHora(),
                dto.getDescricao());
        evolucao.setRetificacaoDe(original);
        evolucao.setMotivoRetificacao(dto.getMotivoRetificacao().trim());
        evolucao.setRegistradoEm(Instant.now());
        evolucao.setRegistradoPorCpf(UsuarioAutenticado.cpf());
        evolucao = evolucaoEnfermagemRepository.save(evolucao);
        return EvolucaoEnfermagemResponseDto.fromEvolucaoEnfermagem(evolucao, null);
    }

    /** Versão que corrige cada registro retificado (registro → sucessor). */
    private Map<UUID, UUID> sucessores() {
        return evolucaoEnfermagemRepository.findParesDeRetificacao().stream()
                .collect(Collectors.toMap(par -> (UUID) par[0], par -> (UUID) par[1]));
    }

    public List<EvolucaoEnfermagemResponseDto> listar() {
        Map<UUID, UUID> sucessores = sucessores();
        return evolucaoEnfermagemRepository.findAll()
                .stream()
                .map(r -> EvolucaoEnfermagemResponseDto.fromEvolucaoEnfermagem(r, sucessores.get(r.getUuid())))
                .collect(Collectors.toList());
    }

    public EvolucaoEnfermagemResponseDto buscarPorId(UUID uuid) {
        return EvolucaoEnfermagemResponseDto.fromEvolucaoEnfermagem(buscarEntidadePorId(uuid), sucessores().get(uuid));
    }

    private EvolucaoEnfermagem buscarEntidadePorId(UUID uuid) {
        return evolucaoEnfermagemRepository.findById(uuid)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Não foi possível encontrar uma evolução de enfermagem com o id " + uuid + " em nossos registros."));
    }

    private Atendimento buscarAtendimentoPorId(UUID uuid) {
        return atendimentoRepository.findById(uuid)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Não foi possível encontrar um atendimento com o id " + uuid + " em nossos registros."));
    }

    private Profissional buscarProfissionalPorMatricula(String matricula) {
        return profissionalRepository.findByMatricula(matricula)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Não foi possível encontrar um profissional com a matrícula " + matricula + " em nossos registros."));
    }
}
