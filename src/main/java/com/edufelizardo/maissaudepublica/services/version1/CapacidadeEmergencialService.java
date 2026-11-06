package com.edufelizardo.maissaudepublica.services.version1;

import com.edufelizardo.maissaudepublica.config.ContextoAuditoria;
import com.edufelizardo.maissaudepublica.config.UsuarioAutenticado;
import com.edufelizardo.maissaudepublica.exceptions.ResourceConflictException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceNotFoundException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceUnprocessableEntityException;
import com.edufelizardo.maissaudepublica.models.CapacidadeEmergencial;
import com.edufelizardo.maissaudepublica.models.Leito;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.CapacidadeEmergencialRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.CapacidadeEmergencialDto;
import com.edufelizardo.maissaudepublica.models.enuns.RecursoCapacidade;
import com.edufelizardo.maissaudepublica.repositories.AtivacaoCapacidadeRepository;
import com.edufelizardo.maissaudepublica.repositories.CapacidadeEmergencialRepository;
import com.edufelizardo.maissaudepublica.repositories.LeitoRepository;
import com.edufelizardo.maissaudepublica.repositories.UnidadeDeSaudeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * O cadastro da capacidade emergencial das unidades (ADR-0158), feito antes da emergência: a capacidade normal e quanto
 * se pode ativar a mais, por unidade e recurso. Manter é de CONTINGENCIA.GERENCIAR; ativar na emergência é da coordenação.
 */
@Service
public class CapacidadeEmergencialService {

    @Autowired private CapacidadeEmergencialRepository repository;
    @Autowired private AtivacaoCapacidadeRepository ativacaoRepository;
    @Autowired private UnidadeDeSaudeRepository unidadeRepository;
    @Autowired private LeitoRepository leitoRepository;
    @Autowired private ControleDeAcesso controleDeAcesso;

    @Transactional(readOnly = true)
    public List<CapacidadeEmergencialDto> listar(UUID unidadeId) {
        return repository.findTodas().stream().filter(c -> unidadeId == null || c.getUnidade().getUuid().equals(unidadeId)).map(this::dto).toList();
    }

    /** Uma por unidade e recurso (409). */
    @Transactional
    public CapacidadeEmergencialDto criar(CapacidadeEmergencialRequestDto dto) {
        controleDeAcesso.exigirAlguma(EmergenciaService.CATALOGO);
        if (repository.existsByUnidade_UuidAndRecurso(dto.getUnidadeId(), dto.getRecurso())) {
            throw new ResourceConflictException("A unidade já tem a capacidade emergencial de " + dto.getRecurso() + ": ajuste a existente.");
        }
        CapacidadeEmergencial c = new CapacidadeEmergencial();
        c.setUnidade(unidadeRepository.findById(dto.getUnidadeId()).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar uma unidade de saúde com o id " + dto.getUnidadeId() + " em nossos registros.")));
        c.setRecurso(dto.getRecurso());
        c.setAtiva(dto.getAtiva() == null || dto.getAtiva());
        c.setRegistradoPorCpf(UsuarioAutenticado.cpf());
        preencher(c, dto);
        repository.save(c);
        ContextoAuditoria.registro(c.getUuid());
        return dto(c);
    }

    /** A unidade e o recurso não mudam (422); o ativável não fica abaixo do que já está ativado (422). */
    @Transactional
    public CapacidadeEmergencialDto atualizar(UUID uuid, CapacidadeEmergencialRequestDto dto) {
        controleDeAcesso.exigirAlguma(EmergenciaService.CATALOGO);
        CapacidadeEmergencial c = repository.findById(uuid).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar a capacidade emergencial " + uuid + " em nossos registros."));
        if (!c.getUnidade().getUuid().equals(dto.getUnidadeId()) || c.getRecurso() != dto.getRecurso()) {
            throw new ResourceUnprocessableEntityException("A unidade e o recurso da capacidade não mudam.");
        }
        long ativada = ativacaoRepository.somaAtivas(c.getUuid());
        if (dto.getEmergencial() < ativada) {
            throw new ResourceUnprocessableEntityException("Há " + ativada + " ativado(s) agora: o ativável não fica abaixo disso.");
        }
        preencher(c, dto);
        if (dto.getAtiva() != null) {
            c.setAtiva(dto.getAtiva());
        }
        repository.save(c);
        ContextoAuditoria.registro(c.getUuid());
        return dto(c);
    }

    private static void preencher(CapacidadeEmergencial c, CapacidadeEmergencialRequestDto dto) {
        c.setNormal(dto.getNormal());
        c.setEmergencial(dto.getEmergencial());
        c.setObservacao(dto.getObservacao() == null || dto.getObservacao().isBlank() ? null : dto.getObservacao().trim());
        c.setAtualizadaEm(Instant.now());
    }

    private CapacidadeEmergencialDto dto(CapacidadeEmergencial c) {
        Integer leitos = c.getRecurso() == RecursoCapacidade.LEITOS
                ? (int) leitoRepository.findByUnidade_UuidOrderByIdentificacaoAsc(c.getUnidade().getUuid()).stream().filter(Leito::isAtivo).count() : null;
        return new CapacidadeEmergencialDto(c.getUuid(), c.getUnidade().getUuid(), c.getUnidade().getNome(), c.getRecurso(), c.getNormal(), c.getEmergencial(),
                ativacaoRepository.somaAtivas(c.getUuid()), leitos, c.getObservacao(), c.isAtiva(), c.getAtualizadaEm());
    }
}
