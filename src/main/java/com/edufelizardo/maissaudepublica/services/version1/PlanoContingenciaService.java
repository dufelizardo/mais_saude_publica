package com.edufelizardo.maissaudepublica.services.version1;

import com.edufelizardo.maissaudepublica.config.ContextoAuditoria;
import com.edufelizardo.maissaudepublica.config.UsuarioAutenticado;
import com.edufelizardo.maissaudepublica.exceptions.ResourceBadRequestException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceConflictException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceNotFoundException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceUnprocessableEntityException;
import com.edufelizardo.maissaudepublica.models.AcaoPlano;
import com.edufelizardo.maissaudepublica.models.ContatoPlano;
import com.edufelizardo.maissaudepublica.models.CriterioAtivacao;
import com.edufelizardo.maissaudepublica.models.NivelEmergencia;
import com.edufelizardo.maissaudepublica.models.PlanoContingencia;
import com.edufelizardo.maissaudepublica.models.RecursoPlano;
import com.edufelizardo.maissaudepublica.models.UnidadeDeSaude;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.PlanoContingenciaRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.RevogacaoPlanoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.PlanoContingenciaDto;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoPlanoContingencia;
import com.edufelizardo.maissaudepublica.repositories.NivelEmergenciaRepository;
import com.edufelizardo.maissaudepublica.repositories.PlanoContingenciaRepository;
import com.edufelizardo.maissaudepublica.repositories.TipoEmergenciaRepository;
import com.edufelizardo.maissaudepublica.repositories.UnidadeDeSaudeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Plano de contingência, fatia E2 das Emergências (ADR-0157, desenho na ADR-0113): configurável e versionado. Só o
 * rascunho se edita; publicar torna vigente e substitui a versão anterior; revisar copia a vigente para um rascunho
 * novo; revogar tira de uso. Manter é de CONTINGENCIA.GERENCIAR.
 */
@Service
public class PlanoContingenciaService {

    @Autowired private PlanoContingenciaRepository repository;
    @Autowired private TipoEmergenciaRepository tipoRepository;
    @Autowired private NivelEmergenciaRepository nivelRepository;
    @Autowired private UnidadeDeSaudeRepository unidadeRepository;
    @Autowired private ControleDeAcesso controleDeAcesso;

    /** As versões, pelo nome; com {@code vigentes}, só as vigentes; com o tipo, só as dele. */
    @Transactional(readOnly = true)
    public List<PlanoContingenciaDto> listar(Boolean vigentes, UUID tipoId) {
        return repository.findTodos().stream()
                .filter(p -> !Boolean.TRUE.equals(vigentes) || p.getSituacao() == SituacaoPlanoContingencia.VIGENTE)
                .filter(p -> tipoId == null || p.getTipo().getUuid().equals(tipoId))
                .map(p -> dto(p, false)).toList();
    }

    @Transactional(readOnly = true)
    public PlanoContingenciaDto buscar(UUID uuid) {
        return dto(plano(uuid), true);
    }

    /** A primeira versão, em rascunho: o código é obrigatório (400) e único (409). */
    @Transactional
    public PlanoContingenciaDto criar(PlanoContingenciaRequestDto dto) {
        controleDeAcesso.exigirAlguma(EmergenciaService.CATALOGO);
        String codigo = dto.getCodigo() == null ? "" : dto.getCodigo().trim().toUpperCase();
        if (codigo.isEmpty()) {
            throw new ResourceBadRequestException("Informe o código do plano.");
        }
        if (!repository.findVersoes(codigo).isEmpty()) {
            throw new ResourceConflictException("Já existe o plano " + codigo + ": revise a versão vigente.");
        }
        PlanoContingencia p = new PlanoContingencia();
        p.setCodigo(codigo);
        p.setVersao(1);
        p.setSituacao(SituacaoPlanoContingencia.RASCUNHO);
        p.setCriadoEm(Instant.now());
        p.setRegistradoPorCpf(UsuarioAutenticado.cpf());
        preencher(p, dto);
        repository.save(p);
        ContextoAuditoria.registro(p.getUuid());
        return dto(p, true);
    }

    /** Ajusta o rascunho (fora dele, 422); o código não muda (422). */
    @Transactional
    public PlanoContingenciaDto atualizar(UUID uuid, PlanoContingenciaRequestDto dto) {
        controleDeAcesso.exigirAlguma(EmergenciaService.CATALOGO);
        PlanoContingencia p = plano(uuid);
        if (p.getSituacao() != SituacaoPlanoContingencia.RASCUNHO) {
            throw new ResourceUnprocessableEntityException("Só o rascunho se edita: revise a versão vigente.");
        }
        if (dto.getCodigo() != null && !dto.getCodigo().isBlank() && !dto.getCodigo().trim().equalsIgnoreCase(p.getCodigo())) {
            throw new ResourceUnprocessableEntityException("O código do plano não muda.");
        }
        preencher(p, dto);
        repository.save(p);
        ContextoAuditoria.registro(p.getUuid());
        return dto(p, true);
    }

    /** Copia a versão vigente (422 se não for) para um rascunho novo; já há rascunho do plano, 409. */
    @Transactional
    public PlanoContingenciaDto revisar(UUID uuid) {
        controleDeAcesso.exigirAlguma(EmergenciaService.CATALOGO);
        PlanoContingencia base = plano(uuid);
        if (base.getSituacao() != SituacaoPlanoContingencia.VIGENTE) {
            throw new ResourceUnprocessableEntityException("Só a versão vigente se revisa.");
        }
        List<PlanoContingencia> versoes = repository.findVersoes(base.getCodigo());
        if (versoes.stream().anyMatch(v -> v.getSituacao() == SituacaoPlanoContingencia.RASCUNHO)) {
            throw new ResourceConflictException("Já há um rascunho do plano " + base.getCodigo() + " em revisão.");
        }
        PlanoContingencia p = new PlanoContingencia();
        p.setCodigo(base.getCodigo());
        p.setVersao(versoes.stream().mapToInt(PlanoContingencia::getVersao).max().orElse(0) + 1);
        p.setSituacao(SituacaoPlanoContingencia.RASCUNHO);
        p.setCriadoEm(Instant.now());
        p.setRegistradoPorCpf(UsuarioAutenticado.cpf());
        p.setNome(base.getNome());
        p.setTipo(base.getTipo());
        p.setObjetivo(base.getObjetivo());
        p.setVigenciaInicio(base.getVigenciaInicio());
        p.setVigenciaFim(base.getVigenciaFim());
        p.getNiveis().addAll(base.getNiveis());
        p.getUnidades().addAll(base.getUnidades());
        int i = 0;
        for (CriterioAtivacao c : base.getCriterios()) {
            p.getCriterios().add(new CriterioAtivacao(null, p, i++, c.getNivel(), c.getDescricao()));
        }
        i = 0;
        for (AcaoPlano a : base.getAcoes()) {
            p.getAcoes().add(new AcaoPlano(null, p, i++, a.getNivel(), a.getDescricao(), a.getResponsavel(), a.getPrazoHoras(), a.getUnidade()));
        }
        i = 0;
        for (RecursoPlano r : base.getRecursos()) {
            p.getRecursos().add(new RecursoPlano(null, p, i++, r.getDescricao(), r.getQuantidade()));
        }
        i = 0;
        for (ContatoPlano c : base.getContatos()) {
            p.getContatos().add(new ContatoPlano(null, p, i++, c.getNome(), c.getPapel(), c.getTelefone()));
        }
        repository.save(p);
        ContextoAuditoria.registro(p.getUuid());
        ContextoAuditoria.detalhe("Revisão do plano " + p.getCodigo() + ": versão " + p.getVersao() + ".");
        return dto(p, true);
    }

    /**
     * Publica o rascunho (fora dele, 422): pede ao menos uma ação prevista e a vigência que não terminou (422). A versão
     * vigente anterior passa a substituída; as ativações já feitas continuam na versão em que foram feitas.
     */
    @Transactional
    public PlanoContingenciaDto publicar(UUID uuid) {
        controleDeAcesso.exigirAlguma(EmergenciaService.CATALOGO);
        PlanoContingencia p = plano(uuid);
        if (p.getSituacao() != SituacaoPlanoContingencia.RASCUNHO) {
            throw new ResourceUnprocessableEntityException("Só o rascunho se publica.");
        }
        if (p.getAcoes().isEmpty()) {
            throw new ResourceUnprocessableEntityException("Inclua ao menos uma ação prevista antes de publicar.");
        }
        if (p.getVigenciaFim() != null && p.getVigenciaFim().isBefore(LocalDate.now())) {
            throw new ResourceUnprocessableEntityException("A vigência do plano já terminou.");
        }
        Instant agora = Instant.now();
        for (PlanoContingencia v : repository.findVersoes(p.getCodigo())) {
            if (v.getSituacao() == SituacaoPlanoContingencia.VIGENTE) {
                v.setSituacao(SituacaoPlanoContingencia.SUBSTITUIDA);
                v.setEncerradoEm(agora);
                repository.save(v);
            }
        }
        p.setSituacao(SituacaoPlanoContingencia.VIGENTE);
        p.setPublicadoEm(agora);
        repository.save(p);
        ContextoAuditoria.registro(p.getUuid());
        ContextoAuditoria.detalhe("Plano " + p.getCodigo() + " publicado na versão " + p.getVersao() + ".");
        return dto(p, true);
    }

    /** Revoga a vigente ou descarta o rascunho, com o motivo; substituída ou revogada, 422. */
    @Transactional
    public PlanoContingenciaDto revogar(UUID uuid, RevogacaoPlanoRequestDto dto) {
        controleDeAcesso.exigirAlguma(EmergenciaService.CATALOGO);
        PlanoContingencia p = plano(uuid);
        if (p.getSituacao() != SituacaoPlanoContingencia.VIGENTE && p.getSituacao() != SituacaoPlanoContingencia.RASCUNHO) {
            throw new ResourceUnprocessableEntityException("Esta versão já não está em uso.");
        }
        p.setSituacao(SituacaoPlanoContingencia.REVOGADA);
        p.setEncerradoEm(Instant.now());
        p.setMotivoRevogacao(dto.getMotivo().trim());
        repository.save(p);
        ContextoAuditoria.registro(p.getUuid());
        return dto(p, true);
    }

    /** O conteúdo inteiro; critério ou ação em nível que o plano não cobre, 422; fim antes do início, 400. */
    private void preencher(PlanoContingencia p, PlanoContingenciaRequestDto dto) {
        if (dto.getVigenciaFim() != null && dto.getVigenciaFim().isBefore(dto.getVigenciaInicio())) {
            throw new ResourceBadRequestException("A vigência termina antes de começar.");
        }
        p.setNome(dto.getNome().trim());
        p.setTipo(tipoRepository.findById(dto.getTipoId()).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar o tipo de emergência " + dto.getTipoId() + " em nossos registros.")));
        p.setObjetivo(dto.getObjetivo().trim());
        p.setVigenciaInicio(dto.getVigenciaInicio());
        p.setVigenciaFim(dto.getVigenciaFim());
        Map<UUID, NivelEmergencia> niveis = new HashMap<>();
        for (UUID id : new HashSet<>(dto.getNivelIds())) {
            niveis.put(id, nivelRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(
                    "Não foi possível encontrar o nível de emergência " + id + " em nossos registros.")));
        }
        p.getNiveis().clear();
        p.getNiveis().addAll(niveis.values());
        p.getUnidades().clear();
        if (dto.getUnidadeIds() != null) {
            for (UUID id : new HashSet<>(dto.getUnidadeIds())) {
                p.getUnidades().add(unidade(id));
            }
        }
        p.getCriterios().clear();
        int i = 0;
        for (PlanoContingenciaRequestDto.Criterio c : lista(dto.getCriterios())) {
            p.getCriterios().add(new CriterioAtivacao(null, p, i++, nivelDoPlano(niveis, c.getNivelId()), c.getDescricao().trim()));
        }
        p.getAcoes().clear();
        i = 0;
        for (PlanoContingenciaRequestDto.Acao a : lista(dto.getAcoes())) {
            p.getAcoes().add(new AcaoPlano(null, p, i++, nivelDoPlano(niveis, a.getNivelId()), a.getDescricao().trim(), a.getResponsavel().trim(),
                    a.getPrazoHoras(), a.getUnidadeId() != null ? unidade(a.getUnidadeId()) : null));
        }
        p.getRecursos().clear();
        i = 0;
        for (PlanoContingenciaRequestDto.Recurso r : lista(dto.getRecursos())) {
            p.getRecursos().add(new RecursoPlano(null, p, i++, r.getDescricao().trim(), r.getQuantidade()));
        }
        p.getContatos().clear();
        i = 0;
        for (PlanoContingenciaRequestDto.Contato c : lista(dto.getContatos())) {
            p.getContatos().add(new ContatoPlano(null, p, i++, c.getNome().trim(), texto(c.getPapel()), texto(c.getTelefone())));
        }
    }

    private static NivelEmergencia nivelDoPlano(Map<UUID, NivelEmergencia> niveis, UUID id) {
        NivelEmergencia n = niveis.get(id);
        if (n == null) {
            throw new ResourceUnprocessableEntityException("Critérios e ações só nos níveis que o plano cobre.");
        }
        return n;
    }

    private UnidadeDeSaude unidade(UUID id) {
        return unidadeRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar uma unidade de saúde com o id " + id + " em nossos registros."));
    }

    PlanoContingencia plano(UUID uuid) {
        return repository.findById(uuid).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar o plano de contingência " + uuid + " em nossos registros."));
    }

    private PlanoContingenciaDto dto(PlanoContingencia p, boolean completo) {
        List<PlanoContingenciaDto.Nivel> niveis = p.getNiveis().stream().sorted(Comparator.comparingInt(NivelEmergencia::getOrdem))
                .map(n -> new PlanoContingenciaDto.Nivel(n.getUuid(), n.getOrdem(), n.getNome())).toList();
        if (!completo) {
            return new PlanoContingenciaDto(p.getUuid(), p.getCodigo(), p.getVersao(), p.getNome(), p.getTipo().getUuid(), p.getTipo().getNome(),
                    p.getObjetivo(), p.getVigenciaInicio(), p.getVigenciaFim(), p.getSituacao(), niveis, List.of(), List.of(), List.of(), List.of(),
                    List.of(), p.getCriadoEm(), p.getPublicadoEm(), p.getEncerradoEm(), p.getMotivoRevogacao(), List.of());
        }
        return new PlanoContingenciaDto(p.getUuid(), p.getCodigo(), p.getVersao(), p.getNome(), p.getTipo().getUuid(), p.getTipo().getNome(),
                p.getObjetivo(), p.getVigenciaInicio(), p.getVigenciaFim(), p.getSituacao(), niveis,
                p.getUnidades().stream().sorted(Comparator.comparing(UnidadeDeSaude::getNome, String.CASE_INSENSITIVE_ORDER))
                        .map(u -> new PlanoContingenciaDto.Unidade(u.getUuid(), u.getNome())).toList(),
                p.getCriterios().stream().map(c -> new PlanoContingenciaDto.Criterio(c.getUuid(), c.getNivel().getUuid(), c.getNivel().getNome(),
                        c.getDescricao())).toList(),
                p.getAcoes().stream().map(a -> new PlanoContingenciaDto.Acao(a.getUuid(), a.getNivel().getUuid(), a.getNivel().getNome(), a.getDescricao(),
                        a.getResponsavel(), a.getPrazoHoras(), a.getUnidade() != null ? a.getUnidade().getUuid() : null,
                        a.getUnidade() != null ? a.getUnidade().getNome() : null)).toList(),
                p.getRecursos().stream().map(r -> new PlanoContingenciaDto.Recurso(r.getDescricao(), r.getQuantidade())).toList(),
                p.getContatos().stream().map(c -> new PlanoContingenciaDto.Contato(c.getNome(), c.getPapel(), c.getTelefone())).toList(),
                p.getCriadoEm(), p.getPublicadoEm(), p.getEncerradoEm(), p.getMotivoRevogacao(),
                repository.findVersoes(p.getCodigo()).stream().map(v -> new PlanoContingenciaDto.Versao(v.getUuid(), v.getVersao(), v.getSituacao(),
                        v.getPublicadoEm(), v.getEncerradoEm())).toList());
    }

    private static <T> List<T> lista(List<T> l) {
        return l == null ? List.of() : l;
    }

    private static String texto(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }
}
