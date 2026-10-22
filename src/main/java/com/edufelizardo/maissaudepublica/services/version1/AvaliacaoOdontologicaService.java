package com.edufelizardo.maissaudepublica.services.version1;

import com.edufelizardo.maissaudepublica.config.ContextoAuditoria;
import com.edufelizardo.maissaudepublica.config.UsuarioAutenticado;
import com.edufelizardo.maissaudepublica.exceptions.ResourceBadRequestException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceNotFoundException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceUnprocessableEntityException;
import com.edufelizardo.maissaudepublica.models.Atendimento;
import com.edufelizardo.maissaudepublica.models.AvaliacaoOdontologica;
import com.edufelizardo.maissaudepublica.models.Profissional;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.AvaliacaoOdontologicaRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.AvaliacaoOdontologicaDto;
import com.edufelizardo.maissaudepublica.repositories.AtendimentoRepository;
import com.edufelizardo.maissaudepublica.repositories.AvaliacaoOdontologicaRepository;
import com.edufelizardo.maissaudepublica.repositories.ProfissionalRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Avaliação odontológica, fatia SB1 (ADR-0134, desenho na ADR-0111): registrada no atendimento de especialidade
 * odontológica, pelo cirurgião-dentista (`ODONTOLOGIA.REGISTRAR`; diagnóstico é ato do dentista, Lei 5.081/1966). É
 * registro clínico: não se edita, retifica-se (ADR-0062), e a leitura segue a do prontuário.
 */
@Service
public class AvaliacaoOdontologicaService {

    public static final String REGISTRAR = "ODONTOLOGIA.REGISTRAR";

    @Autowired
    private AvaliacaoOdontologicaRepository repository;

    @Autowired
    private AtendimentoRepository atendimentoRepository;

    @Autowired
    private ProfissionalRepository profissionalRepository;

    @Autowired
    private ControleDeAcesso controleDeAcesso;

    @Transactional
    public AvaliacaoOdontologicaDto criar(AvaliacaoOdontologicaRequestDto dto) {
        Atendimento atendimento = atendimentoRepository.findById(dto.getAtendimentoId()).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar um atendimento com o id " + dto.getAtendimentoId() + " em nossos registros."));
        controleDeAcesso.exigir(REGISTRAR, atendimento.getUnidade());
        ContextoAuditoria.paciente(atendimento.getPaciente().getUuid());
        if (atendimento.getEspecialidade() == null || !atendimento.getEspecialidade().isOdontologica()) {
            throw new ResourceUnprocessableEntityException("A avaliação odontológica é registrada num atendimento de especialidade odontológica"
                    + (atendimento.getEspecialidade() != null ? "; este é de " + atendimento.getEspecialidade().getNome() : "") + ".");
        }
        AvaliacaoOdontologica a = nova(atendimento, dto);
        a = repository.save(a);
        ContextoAuditoria.registro(a.getUuid());
        return dto(a, null);
    }

    /** Retificação (ADR-0062): nova versão ligada à anterior, no mesmo atendimento; só a vigente se retifica. */
    @Transactional
    public AvaliacaoOdontologicaDto retificar(UUID uuid, AvaliacaoOdontologicaRequestDto dto) {
        AvaliacaoOdontologica original = buscar(uuid);
        if (repository.existsByRetificacaoDe_Uuid(uuid)) {
            throw new ResourceUnprocessableEntityException("Esta avaliação já foi retificada: retifique a versão vigente.");
        }
        controleDeAcesso.exigir(REGISTRAR, original.getAtendimento().getUnidade());
        ContextoAuditoria.paciente(original.getAtendimento().getPaciente().getUuid());
        controleDeAcesso.exigirAutoriaOuSupervisao(original.getRegistradoPorCpf(), original.getAtendimento().getUnidade());
        if (!original.getAtendimento().getUuid().equals(dto.getAtendimentoId())) {
            throw new ResourceBadRequestException("A retificação precisa manter o mesmo atendimento do registro original.");
        }
        if (dto.getMotivoRetificacao() == null || dto.getMotivoRetificacao().isBlank()) {
            throw new ResourceBadRequestException("Informe o motivo da retificação.");
        }
        AvaliacaoOdontologica a = nova(original.getAtendimento(), dto);
        a.setRetificacaoDe(original);
        a.setMotivoRetificacao(dto.getMotivoRetificacao().trim());
        a = repository.save(a);
        ContextoAuditoria.registro(a.getUuid());
        return dto(a, null);
    }

    @Transactional(readOnly = true)
    public AvaliacaoOdontologicaDto buscarPorId(UUID uuid) {
        AvaliacaoOdontologica a = buscar(uuid);
        controleDeAcesso.exigirVisivel(a.getAtendimento().getUnidade(), "PRONTUARIO.CONSULTAR", REGISTRAR);
        ContextoAuditoria.paciente(a.getAtendimento().getPaciente().getUuid());
        ContextoAuditoria.registro(a.getUuid());
        UUID sucessor = repository.findByAtendimento_UuidOrderByRegistradoEmAsc(a.getAtendimento().getUuid()).stream()
                .filter(x -> x.getRetificacaoDe() != null && x.getRetificacaoDe().getUuid().equals(uuid))
                .map(AvaliacaoOdontologica::getUuid).findFirst().orElse(null);
        return dto(a, sucessor);
    }

    private AvaliacaoOdontologica nova(Atendimento atendimento, AvaliacaoOdontologicaRequestDto dto) {
        Profissional profissional = profissionalRepository.findByMatricula(dto.getProfissionalMatricula().trim()).orElseThrow(() ->
                new ResourceNotFoundException("Não foi possível encontrar um profissional com a matrícula " + dto.getProfissionalMatricula()
                        + " em nossos registros."));
        LocalDateTime quando = dto.getDataHora() != null ? dto.getDataHora() : LocalDateTime.now();
        if (quando.isAfter(LocalDateTime.now().plusMinutes(5))) {
            throw new ResourceBadRequestException("A avaliação não pode ser no futuro.");
        }
        AvaliacaoOdontologica a = new AvaliacaoOdontologica();
        a.setAtendimento(atendimento);
        a.setProfissional(profissional);
        a.setDataHora(quando);
        a.setQueixaPrincipal(texto(dto.getQueixaPrincipal()));
        a.setAnamnese(texto(dto.getAnamnese()));
        a.setExameClinico(texto(dto.getExameClinico()));
        a.setDiagnostico(texto(dto.getDiagnostico()));
        a.setRisco(dto.getRisco());
        a.setNecessidadeTratamento(dto.getNecessidadeTratamento());
        a.setPlanoInicial(texto(dto.getPlanoInicial()));
        a.setObservacoes(texto(dto.getObservacoes()));
        a.setRegistradoEm(Instant.now());
        a.setRegistradoPorCpf(UsuarioAutenticado.cpf());
        return a;
    }

    private AvaliacaoOdontologica buscar(UUID uuid) {
        return repository.findById(uuid).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar a avaliação odontológica " + uuid + " em nossos registros."));
    }

    static AvaliacaoOdontologicaDto dto(AvaliacaoOdontologica a, UUID sucessor) {
        return new AvaliacaoOdontologicaDto(a.getUuid(), a.getAtendimento().getUuid(), a.getAtendimento().getPaciente().getUuid(),
                a.getProfissional().getMatricula(), a.getProfissional().getNome(), a.getDataHora(), a.getQueixaPrincipal(), a.getAnamnese(),
                a.getExameClinico(), a.getDiagnostico(), a.getRisco(), a.getNecessidadeTratamento(), a.getPlanoInicial(), a.getObservacoes(),
                a.getRetificacaoDe() != null ? a.getRetificacaoDe().getUuid() : null, a.getMotivoRetificacao(), sucessor != null, sucessor,
                a.getRegistradoEm());
    }

    private static String texto(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }
}
