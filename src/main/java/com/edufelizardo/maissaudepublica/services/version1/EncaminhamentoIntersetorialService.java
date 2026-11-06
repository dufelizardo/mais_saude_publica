package com.edufelizardo.maissaudepublica.services.version1;

import com.edufelizardo.maissaudepublica.config.ContextoAuditoria;
import com.edufelizardo.maissaudepublica.config.UsuarioAutenticado;
import com.edufelizardo.maissaudepublica.exceptions.ResourceBadRequestException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceConflictException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceNotFoundException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceUnprocessableEntityException;
import com.edufelizardo.maissaudepublica.models.EncaminhamentoIntersetorial;
import com.edufelizardo.maissaudepublica.models.InstituicaoExterna;
import com.edufelizardo.maissaudepublica.models.Paciente;
import com.edufelizardo.maissaudepublica.models.Profissional;
import com.edufelizardo.maissaudepublica.models.UnidadeDeSaude;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.EncaminhamentoIntersetorialRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.RetornoEncaminhamentoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.EncaminhamentoIntersetorialDto;
import com.edufelizardo.maissaudepublica.models.enuns.DominioOrigemEncaminhamento;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoEncaminhamentoIntersetorial;
import com.edufelizardo.maissaudepublica.repositories.EncaminhamentoIntersetorialRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * O encaminhamento intersetorial, fatia IS1 (ADR-0154): as regras do ciclo (envio, recebimento, retorno, cancelamento),
 * iguais para todos os domínios. Não confere acesso: o domínio de origem confere antes de chamar (na Saúde Mental, a
 * equipe de referência e o sigilo do caso).
 */
@Service
public class EncaminhamentoIntersetorialService {

    @Autowired
    private EncaminhamentoIntersetorialRepository repository;

    @Autowired
    private InstituicaoExternaService instituicaoService;

    /**
     * Encaminha: instituição inativa, 422; envio no futuro ou retorno esperado antes do envio, 400; já há encaminhamento
     * em aberto para a mesma instituição na mesma origem, 409.
     */
    EncaminhamentoIntersetorial encaminhar(DominioOrigemEncaminhamento dominio, UUID origemId, Paciente paciente, UnidadeDeSaude unidade,
                                           Profissional profissional, EncaminhamentoIntersetorialRequestDto dto) {
        InstituicaoExterna instituicao = instituicaoService.instituicao(dto.getInstituicaoId());
        if (!instituicao.isAtiva()) {
            throw new ResourceUnprocessableEntityException("A instituição " + instituicao.getNome() + " está inativa no catálogo.");
        }
        LocalDate hoje = LocalDate.now();
        LocalDate enviado = dto.getEnviadoEm() != null ? dto.getEnviadoEm() : hoje;
        if (enviado.isAfter(hoje)) {
            throw new ResourceBadRequestException("O envio não pode ser no futuro.");
        }
        if (dto.getRetornoEsperadoEm().isBefore(enviado)) {
            throw new ResourceBadRequestException("O retorno esperado não pode ser antes do envio.");
        }
        boolean aberto = repository.findDaOrigem(dominio, origemId).stream()
                .anyMatch(e -> e.getSituacao().emAberto() && e.getInstituicao().getUuid().equals(instituicao.getUuid()));
        if (aberto) {
            throw new ResourceConflictException("Já há um encaminhamento em aberto para " + instituicao.getNome() + ".");
        }
        EncaminhamentoIntersetorial e = new EncaminhamentoIntersetorial();
        e.setPaciente(paciente);
        e.setInstituicao(instituicao);
        e.setDominioOrigem(dominio);
        e.setOrigemId(origemId);
        e.setUnidade(unidade);
        e.setProfissional(profissional);
        e.setMotivo(dto.getMotivo().trim());
        e.setEnviadoEm(enviado);
        e.setRetornoEsperadoEm(dto.getRetornoEsperadoEm());
        e.setSituacao(SituacaoEncaminhamentoIntersetorial.ENVIADO);
        e.setRegistradoEm(Instant.now());
        e.setRegistradoPorCpf(UsuarioAutenticado.cpf());
        repository.save(e);
        ContextoAuditoria.detalhe("Encaminhamento intersetorial a " + instituicao.getNome() + ".");
        return e;
    }

    /** A instituição confirmou que recebeu: só do ENVIADO (422), entre o envio e hoje (400). */
    void receber(EncaminhamentoIntersetorial e, RetornoEncaminhamentoRequestDto dto) {
        if (e.getSituacao() != SituacaoEncaminhamentoIntersetorial.ENVIADO) {
            throw new ResourceUnprocessableEntityException("Só o encaminhamento enviado pode ser marcado como recebido.");
        }
        e.setRecebidoEm(dataEntre(e, dto.getData()));
        e.setSituacao(SituacaoEncaminhamentoIntersetorial.RECEBIDO);
        repository.save(e);
        ContextoAuditoria.detalhe("Encaminhamento intersetorial recebido pela instituição.");
    }

    /** O retorno da instituição: em aberto (422), desfecho CONCLUIDO ou RECUSADO e o texto (400), entre o envio e hoje (400). */
    void registrarRetorno(EncaminhamentoIntersetorial e, RetornoEncaminhamentoRequestDto dto) {
        if (!e.getSituacao().emAberto()) {
            throw new ResourceUnprocessableEntityException("Este encaminhamento já foi " + e.getSituacao().name().toLowerCase() + ".");
        }
        if (dto.getDesfecho() != SituacaoEncaminhamentoIntersetorial.CONCLUIDO && dto.getDesfecho() != SituacaoEncaminhamentoIntersetorial.RECUSADO) {
            throw new ResourceBadRequestException("O retorno conclui (CONCLUIDO) ou recusa (RECUSADO) o encaminhamento.");
        }
        if (dto.getRetorno() == null || dto.getRetorno().isBlank()) {
            throw new ResourceBadRequestException("Descreva o retorno da instituição.");
        }
        e.setRetornoEm(dataEntre(e, dto.getData()));
        e.setRetorno(dto.getRetorno().trim());
        e.setSituacao(dto.getDesfecho());
        e.setRetornoRegistradoPorCpf(UsuarioAutenticado.cpf());
        repository.save(e);
        ContextoAuditoria.detalhe("Retorno do encaminhamento intersetorial: " + dto.getDesfecho() + ".");
    }

    /** Quem encaminhou desfaz (registro errado ou desistência): só em aberto (422). */
    void cancelar(EncaminhamentoIntersetorial e, String motivo) {
        if (!e.getSituacao().emAberto()) {
            throw new ResourceUnprocessableEntityException("Este encaminhamento já foi " + e.getSituacao().name().toLowerCase() + ".");
        }
        e.setSituacao(SituacaoEncaminhamentoIntersetorial.CANCELADO);
        e.setMotivoCancelamento(motivo.trim());
        repository.save(e);
        ContextoAuditoria.detalhe("Encaminhamento intersetorial cancelado.");
    }

    /** O encaminhamento da origem (404 se for de outra). */
    EncaminhamentoIntersetorial daOrigem(UUID encaminhamentoId, DominioOrigemEncaminhamento dominio, UUID origemId) {
        return repository.findById(encaminhamentoId)
                .filter(e -> e.getDominioOrigem() == dominio && e.getOrigemId().equals(origemId))
                .orElseThrow(() -> new ResourceNotFoundException("Não foi possível encontrar o encaminhamento intersetorial " + encaminhamentoId
                        + " neste registro."));
    }

    List<EncaminhamentoIntersetorialDto> listarDaOrigem(DominioOrigemEncaminhamento dominio, UUID origemId) {
        LocalDate hoje = LocalDate.now();
        return repository.findDaOrigem(dominio, origemId).stream().map(e -> dto(e, hoje)).toList();
    }

    /** As origens com algum encaminhamento sem retorno (em aberto depois do prazo). */
    Set<UUID> origensSemRetorno(DominioOrigemEncaminhamento dominio, Collection<UUID> origens) {
        if (origens.isEmpty()) {
            return Set.of();
        }
        LocalDate hoje = LocalDate.now();
        return repository.findDasOrigens(dominio, origens).stream().filter(e -> e.semRetorno(hoje))
                .map(EncaminhamentoIntersetorial::getOrigemId).collect(Collectors.toSet());
    }

    private static LocalDate dataEntre(EncaminhamentoIntersetorial e, LocalDate data) {
        LocalDate hoje = LocalDate.now();
        LocalDate d = data != null ? data : hoje;
        if (d.isBefore(e.getEnviadoEm()) || d.isAfter(hoje)) {
            throw new ResourceBadRequestException("A data vai do envio (" + e.getEnviadoEm() + ") até hoje.");
        }
        return d;
    }

    static EncaminhamentoIntersetorialDto dto(EncaminhamentoIntersetorial e, LocalDate hoje) {
        InstituicaoExterna i = e.getInstituicao();
        return new EncaminhamentoIntersetorialDto(e.getUuid(), i.getUuid(), i.getNome(), i.getSetor(), i.getTipo(), i.getTelefone(), e.getMotivo(),
                e.getEnviadoEm(), e.getRetornoEsperadoEm(), e.getSituacao(), e.semRetorno(hoje), e.getRecebidoEm(), e.getRetornoEm(), e.getRetorno(),
                e.getMotivoCancelamento(), e.getProfissional() != null ? e.getProfissional().getNome() : null);
    }
}
