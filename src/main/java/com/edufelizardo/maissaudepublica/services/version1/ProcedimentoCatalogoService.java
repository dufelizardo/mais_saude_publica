package com.edufelizardo.maissaudepublica.services.version1;

import com.edufelizardo.maissaudepublica.config.ContextoAuditoria;
import com.edufelizardo.maissaudepublica.exceptions.ResourceConflictException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceNotFoundException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceUnprocessableEntityException;
import com.edufelizardo.maissaudepublica.models.CondicaoOdontologica;
import com.edufelizardo.maissaudepublica.models.Especialidade;
import com.edufelizardo.maissaudepublica.models.ProcedimentoCatalogo;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.ProcedimentoCatalogoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.ProcedimentoCatalogoDto;
import com.edufelizardo.maissaudepublica.models.enuns.AlcanceCondicaoOdontologica;
import com.edufelizardo.maissaudepublica.models.enuns.AlcanceProcedimento;
import com.edufelizardo.maissaudepublica.repositories.CondicaoOdontologicaRepository;
import com.edufelizardo.maissaudepublica.repositories.EspecialidadeRepository;
import com.edufelizardo.maissaudepublica.repositories.ProcedimentoCatalogoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Catálogo geral de procedimentos (ADR-0136, desenho na ADR-0111): não é um catálogo odontológico, serve a todas as
 * especialidades. O código não muda; o item sai de uso desativado, sem apagar (os procedimentos antigos apontam para ele).
 * Dente, face, condição do odontograma e a exigência de dentista só cabem em procedimento odontológico.
 */
@Service
public class ProcedimentoCatalogoService {

    @Autowired
    private ProcedimentoCatalogoRepository repository;

    @Autowired
    private EspecialidadeRepository especialidadeRepository;

    @Autowired
    private CondicaoOdontologicaRepository condicaoRepository;

    /** O catálogo por nome. Com a especialidade, os itens dela mais os de uso geral. */
    @Transactional(readOnly = true)
    public List<ProcedimentoCatalogoDto> listar(UUID especialidadeId) {
        return repository.findTodos().stream()
                .filter(p -> especialidadeId == null || p.getEspecialidade() == null || p.getEspecialidade().getUuid().equals(especialidadeId))
                .map(ProcedimentoCatalogoService::dto).toList();
    }

    @Transactional
    public ProcedimentoCatalogoDto criar(ProcedimentoCatalogoRequestDto dto) {
        if (repository.existsByCodigo(dto.getCodigo())) {
            throw new ResourceConflictException("Já existe um procedimento no catálogo com o código " + dto.getCodigo() + ".");
        }
        ProcedimentoCatalogo p = new ProcedimentoCatalogo();
        p.setCodigo(dto.getCodigo());
        preencher(p, dto);
        p = repository.save(p);
        ContextoAuditoria.registro(p.getUuid());
        return dto(p);
    }

    @Transactional
    public ProcedimentoCatalogoDto atualizar(UUID uuid, ProcedimentoCatalogoRequestDto dto) {
        ProcedimentoCatalogo p = repository.findById(uuid).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar o procedimento " + uuid + " no catálogo."));
        if (!p.getCodigo().equals(dto.getCodigo())) {
            throw new ResourceUnprocessableEntityException("O código do procedimento não muda (" + p.getCodigo() + ").");
        }
        preencher(p, dto);
        repository.save(p);
        ContextoAuditoria.registro(p.getUuid());
        return dto(p);
    }

    /** O item informado, ativo (422), para registrar o procedimento. Nulo, nenhum (o tipo em texto livre). */
    @Transactional(readOnly = true)
    public ProcedimentoCatalogo ativo(UUID uuid) {
        if (uuid == null) {
            return null;
        }
        ProcedimentoCatalogo p = repository.findById(uuid).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar o procedimento " + uuid + " no catálogo."));
        if (!p.isAtivo()) {
            throw new ResourceUnprocessableEntityException("O procedimento " + p.getNome() + " está inativo no catálogo.");
        }
        return p;
    }

    private void preencher(ProcedimentoCatalogo p, ProcedimentoCatalogoRequestDto dto) {
        Especialidade especialidade = dto.getEspecialidadeId() == null ? null : especialidadeRepository.findById(dto.getEspecialidadeId())
                .orElseThrow(() -> new ResourceNotFoundException("Não foi possível encontrar a especialidade " + dto.getEspecialidadeId() + " em nossos registros."));
        AlcanceProcedimento alcance = dto.getAlcance() != null ? dto.getAlcance() : AlcanceProcedimento.NENHUM;
        CondicaoOdontologica condicao = dto.getCondicaoResultanteId() == null ? null : condicaoRepository.findById(dto.getCondicaoResultanteId())
                .orElseThrow(() -> new ResourceNotFoundException("Não foi possível encontrar a condição " + dto.getCondicaoResultanteId() + " em nossos registros."));
        boolean exigeDentista = Boolean.TRUE.equals(dto.getExigeDentista());
        boolean odontologico = especialidade != null && especialidade.isOdontologica();
        if (!odontologico && (alcance != AlcanceProcedimento.NENHUM || condicao != null || exigeDentista)) {
            throw new ResourceUnprocessableEntityException("Dente, face, condição do odontograma e a exigência de dentista são de procedimento "
                    + "odontológico: escolha uma especialidade odontológica.");
        }
        if (condicao != null) {
            exigirCondicaoCompativel(condicao, alcance);
        }
        p.setNome(dto.getNome().trim());
        p.setEspecialidade(especialidade);
        p.setSigtap(dto.getSigtap() == null || dto.getSigtap().isBlank() ? null : dto.getSigtap());
        p.setAlcance(alcance);
        p.setCondicaoResultante(condicao);
        p.setExigeDentista(exigeDentista);
        p.setAtivo(!Boolean.FALSE.equals(dto.getAtivo()));
    }

    /** A condição gravada precisa caber no que o procedimento registra: face com face, dente inteiro com dente. */
    private static void exigirCondicaoCompativel(CondicaoOdontologica c, AlcanceProcedimento alcance) {
        if (!c.isAtiva()) {
            throw new ResourceUnprocessableEntityException("A condição " + c.getNome() + " está inativa.");
        }
        if (alcance == AlcanceProcedimento.NENHUM) {
            throw new ResourceUnprocessableEntityException("Para gravar no odontograma, o procedimento precisa registrar o dente ou a face.");
        }
        if (alcance == AlcanceProcedimento.DENTE && c.getAlcance() == AlcanceCondicaoOdontologica.FACE) {
            throw new ResourceUnprocessableEntityException("A condição " + c.getNome() + " se marca numa face: o procedimento precisa registrar a face.");
        }
        if (alcance == AlcanceProcedimento.FACE && c.getAlcance() == AlcanceCondicaoOdontologica.DENTE) {
            throw new ResourceUnprocessableEntityException("A condição " + c.getNome() + " é do dente inteiro: o procedimento registra o dente, sem face.");
        }
    }

    static ProcedimentoCatalogoDto dto(ProcedimentoCatalogo p) {
        Especialidade e = p.getEspecialidade();
        CondicaoOdontologica c = p.getCondicaoResultante();
        return new ProcedimentoCatalogoDto(p.getUuid(), p.getCodigo(), p.getNome(), e != null ? e.getUuid() : null, e != null ? e.getNome() : null,
                p.odontologico(), p.getSigtap(), p.getAlcance(), c != null ? c.getUuid() : null, c != null ? c.getNome() : null,
                p.isExigeDentista(), p.isAtivo());
    }
}
