package com.edufelizardo.maissaudepublica.services.version1;

import com.edufelizardo.maissaudepublica.config.ContextoAuditoria;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.RetificacaoTriagemRequestDto;
import com.edufelizardo.maissaudepublica.exceptions.ResourceUnprocessableEntityException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceBadRequestException;
import com.edufelizardo.maissaudepublica.config.UsuarioAutenticado;
import org.springframework.transaction.annotation.Transactional;
import java.util.Map;
import java.time.Instant;
import com.edufelizardo.maissaudepublica.exceptions.ResourceNotFoundException;
import com.edufelizardo.maissaudepublica.models.Atendimento;
import com.edufelizardo.maissaudepublica.models.Profissional;
import com.edufelizardo.maissaudepublica.models.Triagem;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.TriagemRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.TriagemResponseDto;
import com.edufelizardo.maissaudepublica.repositories.AtendimentoRepository;
import com.edufelizardo.maissaudepublica.repositories.ProfissionalRepository;
import com.edufelizardo.maissaudepublica.repositories.TriagemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * CRUD da Triagem (Enfermagem — ver MAPA-DE-DOMINIOS.md #8, ADR-0047). {@code profissionalMatricula}
 * é resolvido para o {@link Profissional} real na fronteira da API, mesmo padrão do
 * {@code ConsultaService} (ver ADR-0034/ADR-0043).
 */
@Service
public class TriagemService {

    @Autowired
    private TriagemRepository triagemRepository;

    @Autowired
    private ControleDeAcesso controleDeAcesso;

    @Autowired
    private AtendimentoRepository atendimentoRepository;

    @Autowired
    private ProfissionalRepository profissionalRepository;

    @Transactional
    public TriagemResponseDto criar(TriagemRequestDto dto) {
        Atendimento atendimento = buscarAtendimentoPorId(dto.getAtendimentoId());
        controleDeAcesso.exigir("TRIAGEM.REGISTRAR", atendimento.getUnidade());
        ContextoAuditoria.paciente(atendimento.getPaciente().getUuid());
        Profissional profissional = buscarProfissionalPorMatricula(dto.getProfissionalMatricula());

        Triagem triagem = new Triagem(atendimento, profissional, dto.getDataHora(), dto.getPressaoArterial(),
                dto.getTemperatura(), dto.getSaturacaoOxigenio(), dto.getFrequenciaCardiaca(), dto.getPeso(),
                dto.getClassificacaoRisco(), dto.getObservacoes());
        triagem.setRegistradoEm(Instant.now());
        triagem.setRegistradoPorCpf(UsuarioAutenticado.cpf());
        triagem = triagemRepository.save(triagem);
        ContextoAuditoria.registro(triagem.getUuid());
        return TriagemResponseDto.fromTriagem(triagem);
    }

    /**
     * Retificação (ADR-0062): grava uma nova versão ligada à anterior, que continua no prontuário. Só a
     * versão vigente pode ser retificada, e a triagem continua no mesmo atendimento.
     */
    @Transactional
    public TriagemResponseDto retificar(UUID uuid, RetificacaoTriagemRequestDto dto) {
        Triagem original = triagemRepository.findByIdParaRetificar(uuid)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Não foi possível encontrar uma triagem com o id " + uuid + " em nossos registros."));
        UUID vigente = sucessores().get(uuid);
        if (vigente != null) {
            throw new ResourceUnprocessableEntityException("Esta triagem já foi retificada pela versão " + vigente
                    + ": retifique a versão vigente.");
        }
        controleDeAcesso.exigir("TRIAGEM.REGISTRAR", original.getAtendimento().getUnidade());
        ContextoAuditoria.paciente(original.getAtendimento().getPaciente().getUuid());
        controleDeAcesso.exigirAutoriaOuSupervisao(original.getRegistradoPorCpf(), original.getAtendimento().getUnidade());
        if (!original.getAtendimento().getUuid().equals(dto.getAtendimentoId())) {
            throw new ResourceBadRequestException("A retificação precisa manter o mesmo atendimento do registro original.");
        }
        Profissional profissional = buscarProfissionalPorMatricula(dto.getProfissionalMatricula());

        Triagem triagem = new Triagem(original.getAtendimento(), profissional, dto.getDataHora(), dto.getPressaoArterial(),
                dto.getTemperatura(), dto.getSaturacaoOxigenio(), dto.getFrequenciaCardiaca(), dto.getPeso(),
                dto.getClassificacaoRisco(), dto.getObservacoes());
        triagem.setRetificacaoDe(original);
        triagem.setMotivoRetificacao(dto.getMotivoRetificacao().trim());
        triagem.setRegistradoEm(Instant.now());
        triagem.setRegistradoPorCpf(UsuarioAutenticado.cpf());
        triagem = triagemRepository.save(triagem);
        ContextoAuditoria.registro(triagem.getUuid());
        return TriagemResponseDto.fromTriagem(triagem, null);
    }

    /** Versão que corrige cada registro retificado (registro → sucessor). */
    private Map<UUID, UUID> sucessores() {
        return triagemRepository.findParesDeRetificacao().stream()
                .collect(Collectors.toMap(par -> (UUID) par[0], par -> (UUID) par[1]));
    }

    public List<TriagemResponseDto> listar() {
        Map<UUID, UUID> sucessores = sucessores();
        return controleDeAcesso.filtrar(triagemRepository.findAll().stream(), r -> Stream.of(r.getAtendimento().getUnidade()),
                        "PRONTUARIO.CONSULTAR", "TRIAGEM.REGISTRAR")
                .map(r -> TriagemResponseDto.fromTriagem(r, sucessores.get(r.getUuid())))
                .collect(Collectors.toList());
    }

    public TriagemResponseDto buscarPorId(UUID uuid) {
        Triagem registro = buscarEntidadePorId(uuid);
        controleDeAcesso.exigirVisivel(registro.getAtendimento().getUnidade(), "PRONTUARIO.CONSULTAR", "TRIAGEM.REGISTRAR");
        ContextoAuditoria.paciente(registro.getAtendimento().getPaciente().getUuid());
        return TriagemResponseDto.fromTriagem(registro, sucessores().get(uuid));
    }

    private Triagem buscarEntidadePorId(UUID uuid) {
        return triagemRepository.findById(uuid)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Não foi possível encontrar uma triagem com o id " + uuid + " em nossos registros."));
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
