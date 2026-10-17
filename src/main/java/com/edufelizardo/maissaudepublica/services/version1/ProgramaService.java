package com.edufelizardo.maissaudepublica.services.version1;

import com.edufelizardo.maissaudepublica.config.ContextoAuditoria;
import com.edufelizardo.maissaudepublica.config.UsuarioAutenticado;
import com.edufelizardo.maissaudepublica.exceptions.ResourceBadRequestException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceConflictException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceNotFoundException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceUnprocessableEntityException;
import com.edufelizardo.maissaudepublica.models.CategoriaPrograma;
import com.edufelizardo.maissaudepublica.models.Equipe;
import com.edufelizardo.maissaudepublica.models.ParticipacaoPrograma;
import com.edufelizardo.maissaudepublica.models.Profissional;
import com.edufelizardo.maissaudepublica.models.Programa;
import com.edufelizardo.maissaudepublica.models.UnidadeDeSaude;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.CategoriaProgramaRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.EncerramentoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.ParticipacaoProgramaRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.ProgramaRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.CategoriaProgramaDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.ProgramaDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.ProgramaPublicoDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.ProgramaResumoDto;
import com.edufelizardo.maissaudepublica.models.enuns.AbrangenciaPrograma;
import com.edufelizardo.maissaudepublica.models.enuns.PeriodicidadeAcompanhamento;
import com.edufelizardo.maissaudepublica.models.enuns.Sexo;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoPrograma;
import com.edufelizardo.maissaudepublica.repositories.CategoriaProgramaRepository;
import com.edufelizardo.maissaudepublica.repositories.EquipeRepository;
import com.edufelizardo.maissaudepublica.repositories.ParticipacaoProgramaRepository;
import com.edufelizardo.maissaudepublica.repositories.ProfissionalRepository;
import com.edufelizardo.maissaudepublica.repositories.ProgramaRepository;
import com.edufelizardo.maissaudepublica.repositories.UnidadeDeSaudeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Catálogo de programas de saúde (ADR-0118, fatia P1 da ADR-0109): categorias, programas com o público-alvo e as
 * unidades e equipes que os oferecem, como histórico. Nenhum dado de paciente: o acompanhamento é a P2.
 */
@Service
public class ProgramaService {

    public static final String GERENCIAR = "PROGRAMA.GERENCIAR";
    /** CID-10 (I10, E11.9, A15) ou CIAP-2 (T90, K86): letra e dois dígitos, com subcategoria opcional. */
    private static final Pattern CONDICAO = Pattern.compile("[A-Z][0-9]{2}(\\.[0-9A-Z]{1,2})?");

    @Autowired
    private CategoriaProgramaRepository categoriaRepository;

    @Autowired
    private ProgramaRepository programaRepository;

    @Autowired
    private ParticipacaoProgramaRepository participacaoRepository;

    @Autowired
    private UnidadeDeSaudeRepository unidadeDeSaudeRepository;

    @Autowired
    private EquipeRepository equipeRepository;

    @Autowired
    private ProfissionalRepository profissionalRepository;

    @Autowired
    private ControleDeAcesso controleDeAcesso;

    // ── Categorias ─────────────────────────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<CategoriaProgramaDto> categorias() {
        Map<UUID, Long> porCategoria = programaRepository.findAllComCategoria().stream()
                .collect(Collectors.groupingBy(p -> p.getCategoria().getUuid(), Collectors.counting()));
        return categoriaRepository.findAllByOrderByOrdemAscNomeAsc().stream()
                .map(c -> new CategoriaProgramaDto(c.getUuid(), c.getNome(), c.getOrdem(), c.isAtiva(), porCategoria.getOrDefault(c.getUuid(), 0L)))
                .toList();
    }

    @Transactional
    public CategoriaProgramaDto criarCategoria(CategoriaProgramaRequestDto dto) {
        String nome = dto.getNome().trim();
        if (categoriaRepository.findByNomeIgnoreCase(nome).isPresent()) {
            throw new ResourceConflictException("Já existe a categoria " + nome + ".");
        }
        int ordem = dto.getOrdem() != null ? dto.getOrdem()
                : categoriaRepository.findAll().stream().mapToInt(CategoriaPrograma::getOrdem).max().orElse(0) + 1;
        CategoriaPrograma c = categoriaRepository.save(new CategoriaPrograma(null, nome, ordem, dto.getAtiva() == null || dto.getAtiva()));
        ContextoAuditoria.registro(c.getUuid());
        return new CategoriaProgramaDto(c.getUuid(), c.getNome(), c.getOrdem(), c.isAtiva(), 0);
    }

    /** Renomeia, reordena ou desativa: uma categoria inativa não recebe programa novo, mas os que tem ficam. */
    @Transactional
    public CategoriaProgramaDto atualizarCategoria(UUID uuid, CategoriaProgramaRequestDto dto) {
        CategoriaPrograma c = categoria(uuid);
        String nome = dto.getNome().trim();
        categoriaRepository.findByNomeIgnoreCase(nome).filter(o -> !o.getUuid().equals(uuid)).ifPresent(o -> {
            throw new ResourceConflictException("Já existe a categoria " + nome + ".");
        });
        c.setNome(nome);
        if (dto.getOrdem() != null) {
            c.setOrdem(dto.getOrdem());
        }
        c.setAtiva(dto.getAtiva() == null || dto.getAtiva());
        categoriaRepository.save(c);
        return categorias().stream().filter(x -> x.getUuid().equals(uuid)).findFirst().orElseThrow();
    }

    // ── Programas ──────────────────────────────────────────────────────────────────────────────────

    /** O catálogo, por categoria e nome; com unidadeId, só os que a unidade oferece. */
    @Transactional(readOnly = true)
    public List<ProgramaResumoDto> listar(UUID categoriaId, UUID unidadeId) {
        List<ParticipacaoPrograma> vigentes = participacaoRepository.findVigentesComUnidade();
        Map<UUID, Long> unidades = unidadesPorPrograma(vigentes);
        Set<UUID> daUnidade = unidadeId == null ? null : vigentes.stream()
                .filter(p -> p.getUnidade().getUuid().equals(unidadeId)).map(p -> p.getPrograma().getUuid()).collect(Collectors.toSet());
        return programaRepository.findAllComCategoria().stream()
                .filter(p -> categoriaId == null || p.getCategoria().getUuid().equals(categoriaId))
                .filter(p -> daUnidade == null || daUnidade.contains(p.getUuid()))
                .sorted(ordem())
                .map(p -> resumo(p, unidades.getOrDefault(p.getUuid(), 0L)))
                .toList();
    }

    @Transactional(readOnly = true)
    public ProgramaDto buscar(UUID uuid) {
        return detalhe(programa(uuid));
    }

    @Transactional
    public ProgramaDto criar(ProgramaRequestDto dto) {
        String codigo = dto.getCodigo().trim().toUpperCase(Locale.ROOT);
        if (programaRepository.findByCodigo(codigo).isPresent()) {
            throw new ResourceConflictException("Já existe o programa " + codigo + ".");
        }
        Programa p = new Programa();
        p.setCodigo(codigo);
        aplicar(p, dto, true);
        p = programaRepository.save(p);
        ContextoAuditoria.registro(p.getUuid());
        return detalhe(p);
    }

    @Transactional
    public ProgramaDto atualizar(UUID uuid, ProgramaRequestDto dto) {
        Programa p = programa(uuid);
        String codigo = dto.getCodigo().trim().toUpperCase(Locale.ROOT);
        programaRepository.findByCodigo(codigo).filter(o -> !o.getUuid().equals(uuid)).ifPresent(o -> {
            throw new ResourceConflictException("Já existe o programa " + codigo + ".");
        });
        p.setCodigo(codigo);
        aplicar(p, dto, !dto.getCategoriaId().equals(p.getCategoria().getUuid()));
        return detalhe(programaRepository.save(p));
    }

    // ── Participação ───────────────────────────────────────────────────────────────────────────────

    /**
     * A unidade (e, se for o caso, a equipe dela) passa a oferecer o programa. Só unidade de atendimento; a mesma unidade e
     * equipe não fica vigente duas vezes; programa encerrado não recebe participação.
     */
    @Transactional
    public ProgramaDto incluirParticipacao(UUID programaId, ParticipacaoProgramaRequestDto dto) {
        Programa p = programa(programaId);
        UnidadeDeSaude u = unidadeDeSaudeRepository.findById(dto.getUnidadeId()).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar uma unidade de saúde com o id " + dto.getUnidadeId() + " em nossos registros."));
        controleDeAcesso.exigir(GERENCIAR, u);
        if (RedeUnidadesService.NIVEIS_DE_GESTAO.contains(u.getTipo())) {
            throw new ResourceUnprocessableEntityException(u.getNome() + " é um nível de gestão (" + u.getTipo()
                    + "); programas são oferecidos pelas unidades de atendimento.");
        }
        if (p.getSituacao() == SituacaoPrograma.ENCERRADO) {
            throw new ResourceUnprocessableEntityException("O programa " + p.getNome() + " está encerrado.");
        }
        Equipe equipe = null;
        if (dto.getEquipeId() != null) {
            equipe = equipeRepository.findById(dto.getEquipeId()).orElseThrow(() -> new ResourceNotFoundException(
                    "Não foi possível encontrar uma equipe com o id " + dto.getEquipeId() + " em nossos registros."));
            if (!equipe.getUnidade().getUuid().equals(u.getUuid())) {
                throw new ResourceUnprocessableEntityException("A equipe " + equipe.getNome() + " não é da " + u.getNome() + ".");
            }
        }
        UUID equipeId = equipe != null ? equipe.getUuid() : null;
        boolean repetida = participacaoRepository.findDoPrograma(p.getUuid()).stream()
                .anyMatch(x -> x.vigente() && x.getUnidade().getUuid().equals(u.getUuid())
                        && Objects.equals(x.getEquipe() != null ? x.getEquipe().getUuid() : null, equipeId));
        if (repetida) {
            throw new ResourceConflictException(u.getNome() + (equipe != null ? " (" + equipe.getNome() + ")" : "")
                    + " já oferece o programa " + p.getNome() + ".");
        }
        LocalDate inicio = dto.getInicio() != null ? dto.getInicio() : LocalDate.now();
        if (inicio.isAfter(LocalDate.now())) {
            throw new ResourceBadRequestException("O início não pode ser no futuro.");
        }
        ParticipacaoPrograma nova = participacaoRepository.save(new ParticipacaoPrograma(null, p, u, equipe, inicio, null, null, UsuarioAutenticado.cpf()));
        ContextoAuditoria.unidade(u);
        ContextoAuditoria.registro(nova.getUuid());
        ContextoAuditoria.detalhe("Participação de " + u.getNome() + " no programa " + p.getCodigo() + ".");
        return detalhe(p);
    }

    @Transactional
    public ProgramaDto encerrarParticipacao(UUID programaId, UUID participacaoId, EncerramentoRequestDto dto) {
        Programa p = programa(programaId);
        ParticipacaoPrograma x = participacaoRepository.findById(participacaoId)
                .filter(a -> a.getPrograma().getUuid().equals(p.getUuid()))
                .orElseThrow(() -> new ResourceNotFoundException("Não foi possível encontrar a participação " + participacaoId + " neste programa."));
        controleDeAcesso.exigir(GERENCIAR, x.getUnidade());
        if (!x.vigente()) {
            throw new ResourceUnprocessableEntityException("Esta participação já foi encerrada.");
        }
        LocalDate fim = dto.getFim() != null ? dto.getFim() : LocalDate.now();
        if (fim.isBefore(x.getInicio()) || fim.isAfter(LocalDate.now())) {
            throw new ResourceBadRequestException("O fim precisa ser entre o início (" + x.getInicio() + ") e hoje.");
        }
        x.setFim(fim);
        x.setMotivoFim(dto.getMotivo().trim());
        participacaoRepository.save(x);
        ContextoAuditoria.unidade(x.getUnidade());
        ContextoAuditoria.registro(x.getUuid());
        return detalhe(p);
    }

    // ── Consulta pública ───────────────────────────────────────────────────────────────────────────

    /** Os programas ativos para a landing page: só agregados e o que serve ao cidadão. */
    @Transactional(readOnly = true)
    public List<ProgramaPublicoDto> publico() {
        Map<UUID, List<String>> ondeTem = participacaoRepository.findVigentesComUnidade().stream()
                .filter(x -> x.getUnidade().isAtivo())
                .collect(Collectors.groupingBy(x -> x.getPrograma().getUuid(),
                        Collectors.mapping(x -> x.getUnidade().getNome(), Collectors.collectingAndThen(Collectors.toCollection(LinkedHashSet::new),
                                s -> s.stream().sorted(String.CASE_INSENSITIVE_ORDER).toList()))));
        return programaRepository.findAllComCategoria().stream()
                .filter(p -> p.getSituacao() == SituacaoPrograma.ATIVO && p.getCategoria().isAtiva())
                .sorted(ordem())
                .map(p -> new ProgramaPublicoDto(p.getNome(), p.getCategoria().getNome(), p.getDescricao(), publico(p, false),
                        ondeTem.getOrDefault(p.getUuid(), List.of())))
                .toList();
    }

    // ── Apoio ──────────────────────────────────────────────────────────────────────────────────────

    private void aplicar(Programa p, ProgramaRequestDto dto, boolean categoriaNova) {
        CategoriaPrograma categoria = categoria(dto.getCategoriaId());
        if (categoriaNova && !categoria.isAtiva()) {
            throw new ResourceUnprocessableEntityException("A categoria " + categoria.getNome() + " está inativa.");
        }
        SituacaoPrograma situacao = dto.getSituacao() != null ? dto.getSituacao() : SituacaoPrograma.ATIVO;
        if (situacao == SituacaoPrograma.ENCERRADO && dto.getFim() == null) {
            throw new ResourceBadRequestException("Informe a data de fim do programa encerrado.");
        }
        if (dto.getInicio() != null && dto.getFim() != null && dto.getFim().isBefore(dto.getInicio())) {
            throw new ResourceBadRequestException("O fim não pode ser antes do início.");
        }
        if (dto.getIdadeMinima() != null && dto.getIdadeMaxima() != null && dto.getIdadeMaxima() < dto.getIdadeMinima()) {
            throw new ResourceBadRequestException("A idade máxima não pode ser menor que a mínima.");
        }
        if (dto.getSexo() == Sexo.IGNORADO) {
            throw new ResourceBadRequestException("O público-alvo é de um sexo (MASCULINO ou FEMININO) ou de qualquer um (vazio).");
        }
        List<String> condicoes = new ArrayList<>();
        for (String c : dto.getCondicoes() == null ? List.<String>of() : dto.getCondicoes()) {
            String codigo = c == null ? "" : c.trim().toUpperCase(Locale.ROOT);
            if (codigo.isEmpty()) {
                continue;
            }
            if (!CONDICAO.matcher(codigo).matches()) {
                throw new ResourceBadRequestException("Código de condição inválido: " + c + " (use CID-10 ou CIAP-2, ex.: I10, E11, T90).");
            }
            if (!condicoes.contains(codigo)) {
                condicoes.add(codigo);
            }
        }
        String matricula = dto.getCoordenadorMatricula() == null || dto.getCoordenadorMatricula().isBlank() ? null : dto.getCoordenadorMatricula().trim();
        Profissional coordenador = null;
        if (matricula != null) {
            coordenador = profissionalRepository.findByMatricula(matricula).orElseThrow(() -> new ResourceNotFoundException(
                    "Não foi possível encontrar um profissional com a matrícula " + matricula + " em nossos registros."));
            if (!coordenador.isAtivo()) {
                throw new ResourceUnprocessableEntityException(coordenador.getNome() + " está inativo e não pode coordenar o programa.");
            }
        }
        p.setNome(dto.getNome().trim());
        p.setDescricao(dto.getDescricao() == null || dto.getDescricao().isBlank() ? null : dto.getDescricao().trim());
        p.setCategoria(categoria);
        p.setAbrangencia(dto.getAbrangencia() != null ? dto.getAbrangencia() : AbrangenciaPrograma.MUNICIPAL);
        p.setSituacao(situacao);
        p.setInicio(dto.getInicio());
        p.setFim(dto.getFim());
        p.setCoordenador(coordenador);
        p.setPeriodicidade(dto.getPeriodicidade() != null ? dto.getPeriodicidade() : PeriodicidadeAcompanhamento.SEM_PERIODICIDADE);
        p.setSensivel(Boolean.TRUE.equals(dto.getSensivel()));
        p.setIdadeMinima(dto.getIdadeMinima());
        p.setIdadeMaxima(dto.getIdadeMaxima());
        p.setPublicoSexo(dto.getSexo());
        p.setGestante(Boolean.TRUE.equals(dto.getGestante()));
        p.getCondicoes().clear();
        p.getCondicoes().addAll(condicoes);
    }

    private ProgramaDto detalhe(Programa p) {
        List<ParticipacaoPrograma> todas = participacaoRepository.findDoPrograma(p.getUuid());
        List<ProgramaDto.Participacao> vigentes = todas.stream().filter(ParticipacaoPrograma::vigente)
                .sorted(Comparator.comparing((ParticipacaoPrograma x) -> x.getUnidade().getNome(), String.CASE_INSENSITIVE_ORDER))
                .map(this::participacao).toList();
        List<ProgramaDto.Participacao> encerradas = todas.stream().filter(x -> !x.vigente())
                .sorted(Comparator.comparing(ParticipacaoPrograma::getFim).reversed()).map(this::participacao).toList();
        long unidades = vigentes.stream().map(ProgramaDto.Participacao::getUnidadeId).distinct().count();
        return new ProgramaDto(resumo(p, unidades), vigentes, encerradas);
    }

    private ProgramaDto.Participacao participacao(ParticipacaoPrograma x) {
        return new ProgramaDto.Participacao(x.getUuid(), x.getUnidade().getUuid(), x.getUnidade().getNome(),
                x.getEquipe() != null ? x.getEquipe().getUuid() : null, x.getEquipe() != null ? x.getEquipe().getNome() : null,
                x.getInicio(), x.getFim(), x.getMotivoFim());
    }

    private ProgramaResumoDto resumo(Programa p, long unidades) {
        Profissional c = p.getCoordenador();
        return new ProgramaResumoDto(p.getUuid(), p.getCodigo(), p.getNome(), p.getDescricao(), p.getCategoria().getUuid(),
                p.getCategoria().getNome(), p.getAbrangencia(), p.getSituacao(), p.getInicio(), p.getFim(),
                c != null ? c.getMatricula() : null, c != null ? c.getNome() : null, p.getPeriodicidade(), p.isSensivel(),
                p.getIdadeMinima(), p.getIdadeMaxima(), p.getPublicoSexo(), p.isGestante(), List.copyOf(p.getCondicoes()),
                publico(p, true), unidades);
    }

    /** "Gestantes", "60 anos ou mais", "mulheres de 25 a 64 anos"; com condições, só na tela interna. */
    static String publico(Programa p, boolean comCondicoes) {
        List<String> partes = new ArrayList<>();
        String quem = p.isGestante() ? "gestantes" : p.getPublicoSexo() == Sexo.FEMININO ? "mulheres"
                : p.getPublicoSexo() == Sexo.MASCULINO ? "homens" : null;
        Integer min = p.getIdadeMinima();
        Integer max = p.getIdadeMaxima();
        String idade = min != null && max != null ? "de " + min + " a " + max + " anos"
                : min != null ? min + " anos ou mais" : max != null ? "até " + max + " anos" : null;
        if (quem != null && idade != null) {
            partes.add(quem + " " + idade);
        } else if (quem != null) {
            partes.add(quem);
        } else if (idade != null) {
            partes.add(idade);
        }
        if (comCondicoes && !p.getCondicoes().isEmpty()) {
            partes.add("condições " + String.join(", ", p.getCondicoes()));
        }
        if (partes.isEmpty()) {
            return "toda a população";
        }
        String texto = String.join(" · ", partes);
        return Character.toUpperCase(texto.charAt(0)) + texto.substring(1);
    }

    private static Comparator<Programa> ordem() {
        return Comparator.comparingInt((Programa p) -> p.getCategoria().getOrdem())
                .thenComparing(p -> p.getCategoria().getNome(), String.CASE_INSENSITIVE_ORDER)
                .thenComparing(Programa::getNome, String.CASE_INSENSITIVE_ORDER);
    }

    private static Map<UUID, Long> unidadesPorPrograma(List<ParticipacaoPrograma> vigentes) {
        return vigentes.stream().collect(Collectors.groupingBy(x -> x.getPrograma().getUuid(),
                Collectors.mapping(x -> x.getUnidade().getUuid(), Collectors.collectingAndThen(Collectors.toSet(), s -> (long) s.size()))));
    }

    private CategoriaPrograma categoria(UUID uuid) {
        return categoriaRepository.findById(uuid).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar uma categoria de programa com o id " + uuid + " em nossos registros."));
    }

    private Programa programa(UUID uuid) {
        return programaRepository.findById(uuid).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar um programa com o id " + uuid + " em nossos registros."));
    }
}
