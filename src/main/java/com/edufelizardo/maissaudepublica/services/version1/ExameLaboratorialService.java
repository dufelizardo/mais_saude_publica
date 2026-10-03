package com.edufelizardo.maissaudepublica.services.version1;

import com.edufelizardo.maissaudepublica.exceptions.ResourceBadRequestException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceConflictException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceNotFoundException;
import com.edufelizardo.maissaudepublica.models.ExameLaboratorial;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.ExameLaboratorialRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.ExameLaboratorialResponseDto;
import com.edufelizardo.maissaudepublica.models.enuns.TipoResultadoExame;
import com.edufelizardo.maissaudepublica.repositories.ExameLaboratorialRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/** Catálogo de exames do laboratório assistencial (ADR-0093). */
@Service
public class ExameLaboratorialService {

    @Autowired
    private ExameLaboratorialRepository repository;

    @Transactional
    public ExameLaboratorialResponseDto criar(ExameLaboratorialRequestDto dto) {
        ExameLaboratorial exame = new ExameLaboratorial();
        aplicar(exame, dto, null);
        exame.setAtivo(dto.getAtivo() == null || dto.getAtivo());
        return ExameLaboratorialResponseDto.fromExame(repository.save(exame));
    }

    @Transactional
    public ExameLaboratorialResponseDto atualizar(UUID uuid, ExameLaboratorialRequestDto dto) {
        ExameLaboratorial exame = buscar(uuid);
        aplicar(exame, dto, uuid);
        if (dto.getAtivo() != null) {
            exame.setAtivo(dto.getAtivo());
        }
        return ExameLaboratorialResponseDto.fromExame(repository.save(exame));
    }

    @Transactional(readOnly = true)
    public List<ExameLaboratorialResponseDto> listar(boolean apenasAtivos) {
        return repository.findAllByOrderByNomeAsc().stream().filter(e -> !apenasAtivos || e.isAtivo())
                .map(ExameLaboratorialResponseDto::fromExame).toList();
    }

    @Transactional(readOnly = true)
    public ExameLaboratorialResponseDto buscarPorId(UUID uuid) {
        return ExameLaboratorialResponseDto.fromExame(buscar(uuid));
    }

    ExameLaboratorial buscar(UUID uuid) {
        return repository.findById(uuid).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar um exame com o id " + uuid + " em nossos registros."));
    }

    private void aplicar(ExameLaboratorial exame, ExameLaboratorialRequestDto dto, UUID proprio) {
        String nome = dto.getNome().trim();
        repository.findByNomeIgnoreCase(nome).filter(e -> !e.getUuid().equals(proprio)).ifPresent(e -> {
            throw new ResourceConflictException("Já existe um exame com o nome " + nome + ".");
        });
        boolean numerico = dto.getTipoResultado() == TipoResultadoExame.NUMERICO;
        if (numerico && dto.getReferenciaMinima() != null && dto.getReferenciaMaxima() != null
                && dto.getReferenciaMinima().compareTo(dto.getReferenciaMaxima()) > 0) {
            throw new ResourceBadRequestException("A referência mínima não pode ser maior que a máxima.");
        }
        exame.setNome(nome);
        exame.setMaterial(dto.getMaterial());
        exame.setTipoResultado(dto.getTipoResultado());
        exame.setUnidadeMedida(numerico ? textoOuNulo(dto.getUnidadeMedida()) : null);
        exame.setReferenciaMinima(numerico ? dto.getReferenciaMinima() : null);
        exame.setReferenciaMaxima(numerico ? dto.getReferenciaMaxima() : null);
        exame.setReferenciaTexto(numerico ? null : textoOuNulo(dto.getReferenciaTexto()));
        exame.setPreparo(textoOuNulo(dto.getPreparo()));
        exame.setPrazoDias(dto.getPrazoDias());
    }

    private static String textoOuNulo(String texto) {
        return texto == null || texto.isBlank() ? null : texto.trim();
    }
}
