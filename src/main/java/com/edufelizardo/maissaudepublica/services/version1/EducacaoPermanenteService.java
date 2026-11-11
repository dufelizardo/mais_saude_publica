package com.edufelizardo.maissaudepublica.services.version1;

import com.edufelizardo.maissaudepublica.config.ContextoAuditoria;
import com.edufelizardo.maissaudepublica.exceptions.ResourceConflictException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceNotFoundException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceUnprocessableEntityException;
import com.edufelizardo.maissaudepublica.models.Competencia;
import com.edufelizardo.maissaudepublica.models.ConteudoEducativo;
import com.edufelizardo.maissaudepublica.models.ParticipacaoTreinamento;
import com.edufelizardo.maissaudepublica.models.Treinamento;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.CompetenciaRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.EducacaoDoTreinamentoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.EducacaoPermanenteDto;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoConteudo;
import com.edufelizardo.maissaudepublica.repositories.CompetenciaRepository;
import com.edufelizardo.maissaudepublica.repositories.ConteudoEducativoRepository;
import com.edufelizardo.maissaudepublica.repositories.ParticipacaoTreinamentoRepository;
import com.edufelizardo.maissaudepublica.repositories.ProfissionalRepository;
import com.edufelizardo.maissaudepublica.repositories.TreinamentoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Educação permanente, fatia C4 (ADR-0173): competências que cada treinamento desenvolve e o material didático, que são conteúdos
 * educativos já revisados. As competências do profissional saem das participações do RH, sem registro paralelo.
 */
@Service
public class EducacaoPermanenteService {

    /** Versões que passaram pela revisão técnica. */
    private static final Set<SituacaoConteudo> REVISADOS = EnumSet.of(SituacaoConteudo.APROVADO, SituacaoConteudo.PUBLICADO);

    @Autowired @org.springframework.context.annotation.Lazy private EducacaoEmSaudeService educacaoEmSaude;
    @Autowired private CompetenciaRepository competenciaRepository;
    @Autowired private TreinamentoRepository treinamentoRepository;
    @Autowired private ConteudoEducativoRepository conteudoRepository;
    @Autowired private ParticipacaoTreinamentoRepository participacaoRepository;
    @Autowired private ProfissionalRepository profissionalRepository;

    // ── Competências ───────────────────────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<EducacaoPermanenteDto.Competencia> competencias() {
        return competenciaRepository.findAllByOrderByNomeAsc().stream().map(EducacaoPermanenteService::dto).toList();
    }

    @Transactional
    public EducacaoPermanenteDto.Competencia criarCompetencia(CompetenciaRequestDto dto) {
        Competencia c = new Competencia();
        preencher(c, dto, null);
        competenciaRepository.save(c);
        ContextoAuditoria.registro(c.getUuid());
        return dto(c);
    }

    @Transactional
    public EducacaoPermanenteDto.Competencia atualizarCompetencia(UUID uuid, CompetenciaRequestDto dto) {
        Competencia c = competenciaRepository.findById(uuid).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar a competência " + uuid + " em nossos registros."));
        preencher(c, dto, uuid);
        ContextoAuditoria.registro(c.getUuid());
        return dto(competenciaRepository.save(c));
    }

    private void preencher(Competencia c, CompetenciaRequestDto dto, UUID atual) {
        String codigo = dto.getCodigo().trim().toUpperCase();
        competenciaRepository.findByCodigo(codigo).filter(o -> !o.getUuid().equals(atual)).ifPresent(o -> {
            throw new ResourceConflictException("Já existe a competência " + codigo + ".");
        });
        c.setCodigo(codigo);
        c.setNome(dto.getNome().trim());
        c.setDescricao(dto.getDescricao() == null || dto.getDescricao().isBlank() ? null : dto.getDescricao().trim());
        c.setAtivo(dto.getAtivo() == null || dto.getAtivo());
    }

    // ── Treinamentos ───────────────────────────────────────────────────────────────────────────────

    /** As ligações de todos os treinamentos do catálogo. */
    @Transactional(readOnly = true)
    public List<EducacaoPermanenteDto.Treinamento> treinamentos() {
        Map<UUID, ConteudoEducativo> vigentes = vigentes();
        return treinamentoRepository.findAll().stream().sorted(Comparator.comparing(Treinamento::getNome)).map(t -> dto(t, vigentes)).toList();
    }

    /**
     * Substitui as competências e os conteúdos do treinamento. Competência ou conteúdo que não existe: 404. Competência inativa só fica se já
     * estava ligada (422). Conteúdo sem versão revisada, só rascunho ou em revisão: 422.
     */
    @Transactional
    public EducacaoPermanenteDto.Treinamento ligar(UUID treinamentoId, EducacaoDoTreinamentoRequestDto dto) {
        Treinamento t = treinamentoRepository.findById(treinamentoId).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar um treinamento com o id " + treinamentoId + " em nossos registros."));
        Set<UUID> atuais = t.getCompetencias().stream().map(Competencia::getUuid).collect(Collectors.toSet());
        Set<Competencia> competencias = new HashSet<>();
        for (UUID id : new LinkedHashSet<>(dto.getCompetenciaIds())) {
            Competencia c = competenciaRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(
                    "Não foi possível encontrar a competência " + id + " em nossos registros."));
            if (!c.isAtivo() && !atuais.contains(id)) {
                throw new ResourceUnprocessableEntityException("A competência " + c.getNome() + " está inativa.");
            }
            competencias.add(c);
        }
        Map<UUID, ConteudoEducativo> vigentes = vigentes();
        Set<UUID> conteudos = new LinkedHashSet<>(dto.getConteudoGrupoIds());
        for (UUID grupo : conteudos) {
            if (!vigentes.containsKey(grupo)) {
                if (conteudoRepository.findByGrupoId(grupo).isEmpty()) {
                    throw new ResourceNotFoundException("Não foi possível encontrar o conteúdo " + grupo + " em nossos registros.");
                }
                throw new ResourceUnprocessableEntityException("O conteúdo ainda não passou pela revisão técnica.");
            }
        }
        t.getCompetencias().clear();
        t.getCompetencias().addAll(competencias);
        t.getConteudos().clear();
        t.getConteudos().addAll(conteudos);
        treinamentoRepository.save(t);
        ContextoAuditoria.registro(t.getUuid());
        ContextoAuditoria.detalhe("Educação permanente: " + competencias.size() + " competência(s) e " + conteudos.size() + " conteúdo(s).");
        return dto(t, vigentes);
    }

    /** Os conteúdos que podem ser material didático: a versão em vigor dos que passaram pela revisão. */
    @Transactional(readOnly = true)
    public List<EducacaoPermanenteDto.Conteudo> conteudosDisponiveis() {
        return vigentes().values().stream().map(EducacaoPermanenteService::dto)
                .sorted(Comparator.comparing(EducacaoPermanenteDto.Conteudo::getTitulo)).toList();
    }

    /** Em quais treinamentos cada conteúdo é usado. */
    @Transactional(readOnly = true)
    public List<EducacaoPermanenteDto.UsoDoConteudo> usoDosConteudos() {
        Map<UUID, List<String>> uso = new TreeMap<>();
        for (Treinamento t : treinamentoRepository.findAll()) {
            for (UUID g : t.getConteudos()) {
                uso.computeIfAbsent(g, k -> new ArrayList<>()).add(t.getNome());
            }
        }
        Map<UUID, int[]> acoes = educacaoEmSaude.usoDosConteudos();
        acoes.keySet().forEach(g -> uso.computeIfAbsent(g, k -> new ArrayList<>()));
        return uso.entrySet().stream().map(e -> {
            int[] a = acoes.getOrDefault(e.getKey(), new int[2]);
            return new EducacaoPermanenteDto.UsoDoConteudo(e.getKey(), e.getValue().stream().sorted().toList(), a[0], a[1]);
        }).toList();
    }

    // ── Profissional ───────────────────────────────────────────────────────────────────────────────

    /**
     * As competências do profissional, pelas participações concluídas. Com mais de um treinamento para a mesma competência, vale a
     * participação em dia de validade mais longa; vencida só aparece se não houver outra.
     */
    @Transactional(readOnly = true)
    public List<EducacaoPermanenteDto.CompetenciaDoProfissional> doProfissional(String matricula) {
        if (!profissionalRepository.existsByMatricula(matricula)) {
            throw new ResourceNotFoundException("Não foi possível encontrar um profissional com a matrícula " + matricula + " em nossos registros.");
        }
        LocalDate hoje = LocalDate.now();
        Map<UUID, EducacaoPermanenteDto.CompetenciaDoProfissional> melhor = new HashMap<>();
        for (ParticipacaoTreinamento p : participacaoRepository.findByProfissional_MatriculaOrderByDataConclusaoDesc(matricula)) {
            boolean vencida = p.getDataValidade() != null && p.getDataValidade().isBefore(hoje);
            for (Competencia c : p.getTreinamento().getCompetencias()) {
                var nova = new EducacaoPermanenteDto.CompetenciaDoProfissional(c.getUuid(), c.getCodigo(), c.getNome(), p.getTreinamento().getNome(),
                        p.getDataConclusao(), p.getDataValidade(), vencida);
                melhor.merge(c.getUuid(), nova, (a, b) -> melhor(a, b) ? a : b);
            }
        }
        return melhor.values().stream().sorted(Comparator.comparing(EducacaoPermanenteDto.CompetenciaDoProfissional::getNome)).toList();
    }

    private static boolean melhor(EducacaoPermanenteDto.CompetenciaDoProfissional a, EducacaoPermanenteDto.CompetenciaDoProfissional b) {
        if (a.isVencida() != b.isVencida()) {
            return !a.isVencida();
        }
        if (a.getDataValidade() == null || b.getDataValidade() == null) {
            return a.getDataValidade() == null;
        }
        return !a.getDataValidade().isBefore(b.getDataValidade());
    }

    // ── Apoio ──────────────────────────────────────────────────────────────────────────────────────

    /** Por grupo, a versão revisada mais recente. Também usada pelas campanhas (ADR-0174). */
    public Map<UUID, ConteudoEducativo> vigentes() {
        Map<UUID, ConteudoEducativo> vigentes = new HashMap<>();
        for (ConteudoEducativo c : conteudoRepository.findBySituacaoIn(REVISADOS)) {
            vigentes.merge(c.getGrupoId(), c, (a, b) -> a.getVersao() >= b.getVersao() ? a : b);
        }
        return vigentes;
    }

    private static EducacaoPermanenteDto.Treinamento dto(Treinamento t, Map<UUID, ConteudoEducativo> vigentes) {
        return new EducacaoPermanenteDto.Treinamento(t.getUuid(), t.getNome(),
                t.getCompetencias().stream().map(EducacaoPermanenteService::dto).sorted(Comparator.comparing(EducacaoPermanenteDto.Competencia::getNome)).toList(),
                t.getConteudos().stream().map(vigentes::get).filter(Objects::nonNull).map(EducacaoPermanenteService::dto)
                        .sorted(Comparator.comparing(EducacaoPermanenteDto.Conteudo::getTitulo)).toList());
    }

    private static EducacaoPermanenteDto.Competencia dto(Competencia c) {
        return new EducacaoPermanenteDto.Competencia(c.getUuid(), c.getCodigo(), c.getNome(), c.getDescricao(), c.isAtivo());
    }

    public static EducacaoPermanenteDto.Conteudo dto(ConteudoEducativo c) {
        boolean vencido = c.getValidade().isBefore(LocalDate.now());
        SituacaoConteudo situacao = c.getSituacao() == SituacaoConteudo.PUBLICADO && vencido ? SituacaoConteudo.EXPIRADO : c.getSituacao();
        return new EducacaoPermanenteDto.Conteudo(c.getGrupoId(), c.getUuid(), c.getTitulo(), c.getVersao(), situacao, c.getPublico(), c.getValidade(), vencido);
    }
}
