package com.edufelizardo.maissaudepublica.services.version1;

import com.edufelizardo.maissaudepublica.config.ContextoAuditoria;
import com.edufelizardo.maissaudepublica.config.UsuarioAutenticado;
import com.edufelizardo.maissaudepublica.exceptions.ResourceConflictException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceNotFoundException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceUnprocessableEntityException;
import com.edufelizardo.maissaudepublica.models.InstituicaoExterna;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.InstituicaoExternaRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.InstituicaoExternaDto;
import com.edufelizardo.maissaudepublica.models.enuns.SetorIntersetorial;
import com.edufelizardo.maissaudepublica.repositories.InstituicaoExternaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * O catálogo único de instituições externas, fatia IS1 da Intersetorialidade (ADR-0154): leitura aberta a quem está
 * logado (é cadastro, sem paciente); manter é de INTERSETORIAL.GERENCIAR.
 */
@Service
public class InstituicaoExternaService {

    public static final String GERENCIAR = "INTERSETORIAL.GERENCIAR";

    @Autowired
    private InstituicaoExternaRepository repository;

    @Autowired
    private ControleDeAcesso controleDeAcesso;

    /** O catálogo em ordem de nome, pelo setor e, com {@code ativas}, só as ativas. */
    @Transactional(readOnly = true)
    public List<InstituicaoExternaDto> listar(SetorIntersetorial setor, Boolean ativas) {
        return repository.findAllByOrderByNomeAsc().stream()
                .filter(i -> setor == null || i.getSetor() == setor)
                .filter(i -> !Boolean.TRUE.equals(ativas) || i.isAtiva())
                .map(InstituicaoExternaService::dto)
                .toList();
    }

    @Transactional(readOnly = true)
    public InstituicaoExternaDto buscar(UUID uuid) {
        return dto(instituicao(uuid));
    }

    /** Nome repetido, 409; o tipo de outro setor, 422. */
    @Transactional
    public InstituicaoExternaDto criar(InstituicaoExternaRequestDto dto) {
        controleDeAcesso.exigirAlguma(GERENCIAR);
        String nome = dto.getNome().trim();
        if (repository.existsByNomeIgnoreCase(nome)) {
            throw new ResourceConflictException("Já existe a instituição " + nome + " no catálogo.");
        }
        InstituicaoExterna i = new InstituicaoExterna();
        preencher(i, dto);
        i.setAtiva(dto.getAtiva() == null || dto.getAtiva());
        i.setRegistradoPorCpf(UsuarioAutenticado.cpf());
        repository.save(i);
        ContextoAuditoria.registro(i.getUuid());
        return dto(i);
    }

    /** Ajusta os dados; sem {@code ativa}, mantém. Inativar não mexe nos encaminhamentos já feitos. */
    @Transactional
    public InstituicaoExternaDto atualizar(UUID uuid, InstituicaoExternaRequestDto dto) {
        controleDeAcesso.exigirAlguma(GERENCIAR);
        InstituicaoExterna i = instituicao(uuid);
        String nome = dto.getNome().trim();
        if (repository.existsByNomeIgnoreCaseAndUuidNot(nome, uuid)) {
            throw new ResourceConflictException("Já existe a instituição " + nome + " no catálogo.");
        }
        preencher(i, dto);
        if (dto.getAtiva() != null) {
            i.setAtiva(dto.getAtiva());
        }
        repository.save(i);
        ContextoAuditoria.registro(i.getUuid());
        return dto(i);
    }

    private static void preencher(InstituicaoExterna i, InstituicaoExternaRequestDto dto) {
        if (dto.getTipo().setor() != null && dto.getTipo().setor() != dto.getSetor()) {
            throw new ResourceUnprocessableEntityException("O tipo " + dto.getTipo() + " é do setor " + dto.getTipo().setor() + ", não de " + dto.getSetor() + ".");
        }
        i.setNome(dto.getNome().trim());
        i.setSetor(dto.getSetor());
        i.setTipo(dto.getTipo());
        i.setEndereco(texto(dto.getEndereco()));
        i.setTelefone(texto(dto.getTelefone()));
        i.setEmail(texto(dto.getEmail()));
        i.setResponsavel(texto(dto.getResponsavel()));
        i.setObservacao(texto(dto.getObservacao()));
        i.setAtualizadaEm(Instant.now());
    }

    InstituicaoExterna instituicao(UUID uuid) {
        return repository.findById(uuid).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar a instituição externa " + uuid + " em nossos registros."));
    }

    static InstituicaoExternaDto dto(InstituicaoExterna i) {
        return new InstituicaoExternaDto(i.getUuid(), i.getNome(), i.getSetor(), i.getTipo(), i.getEndereco(), i.getTelefone(), i.getEmail(),
                i.getResponsavel(), i.getObservacao(), i.isAtiva(), i.getAtualizadaEm());
    }

    private static String texto(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }
}
