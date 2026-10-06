package com.edufelizardo.maissaudepublica.services.version1;

import com.edufelizardo.maissaudepublica.config.ContextoAuditoria;
import com.edufelizardo.maissaudepublica.config.UsuarioAutenticado;
import com.edufelizardo.maissaudepublica.exceptions.ResourceBadRequestException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceConflictException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceNotFoundException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceUnprocessableEntityException;
import com.edufelizardo.maissaudepublica.models.Afastamento;
import com.edufelizardo.maissaudepublica.models.Equipe;
import com.edufelizardo.maissaudepublica.models.Lotacao;
import com.edufelizardo.maissaudepublica.models.MembroEquipe;
import com.edufelizardo.maissaudepublica.models.Profissional;
import com.edufelizardo.maissaudepublica.models.UnidadeDeSaude;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.EquipeRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.MembroEquipeRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.SaidaMembroEquipeRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.EquipeResponseDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.EquipeResumoDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.EquipesResponseDto;
import com.edufelizardo.maissaudepublica.models.enuns.FuncaoEquipe;
import com.edufelizardo.maissaudepublica.models.enuns.StatusAfastamento;
import com.edufelizardo.maissaudepublica.models.enuns.TipoEquipe;
import com.edufelizardo.maissaudepublica.repositories.AfastamentoRepository;
import com.edufelizardo.maissaudepublica.repositories.EquipeRepository;
import com.edufelizardo.maissaudepublica.repositories.LotacaoRepository;
import com.edufelizardo.maissaudepublica.repositories.MembroEquipeRepository;
import com.edufelizardo.maissaudepublica.repositories.ProfissionalRepository;
import com.edufelizardo.maissaudepublica.repositories.UnidadeDeSaudeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Equipes de saúde (ADR-0103): cadastro na unidade, membros como histórico (entrada e saída), composição mínima calculada
 * pelo tipo, coordenação e equipes apoiadas pela eMulti.
 */
@Service
public class EquipeService {

    public static final String GERENCIAR = "EQUIPE.GERENCIAR";
    static final String[] VER = {GERENCIAR, "RH.CONSULTAR", "RH.GERENCIAR", "ADMINISTRATIVO.CONSULTAR"};
    private static final Set<StatusAfastamento> AFASTA = Set.of(StatusAfastamento.APROVADO, StatusAfastamento.EM_ANDAMENTO);

    @Autowired
    private EquipeRepository equipeRepository;

    @Autowired
    private MembroEquipeRepository membroRepository;

    @Autowired
    private UnidadeDeSaudeRepository unidadeDeSaudeRepository;

    @Autowired
    private ProfissionalRepository profissionalRepository;

    @Autowired
    private LotacaoRepository lotacaoRepository;

    @Autowired
    private AfastamentoRepository afastamentoRepository;

    @Autowired
    private ControleDeAcesso controleDeAcesso;

    // ── Leitura ────────────────────────────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public EquipesResponseDto listar(UUID unidadeId) {
        List<Equipe> equipes = visiveis(unidadeId);
        Map<UUID, List<MembroEquipe>> membros = membrosVigentes(equipes);
        List<EquipeResumoDto> lista = equipes.stream().map(e -> resumo(e, membros.getOrDefault(e.getUuid(), List.of()))).toList();
        Map<TipoEquipe, Long> porTipo = new EnumMap<>(TipoEquipe.class);
        equipes.stream().filter(Equipe::isAtiva).forEach(e -> porTipo.merge(e.getTipo(), 1L, Long::sum));
        long vinculados = equipes.stream().filter(Equipe::isAtiva)
                .flatMap(e -> membros.getOrDefault(e.getUuid(), List.of()).stream())
                .map(m -> m.getProfissional().getUuid()).distinct().count();
        return new EquipesResponseDto(equipes.size(), equipes.stream().filter(Equipe::isAtiva).count(), porTipo, vinculados,
                lista.stream().filter(r -> r.isAtiva() && !r.isCompleta()).count(), lista);
    }

    @Transactional(readOnly = true)
    public EquipeResponseDto buscarPorId(UUID uuid) {
        Equipe e = buscar(uuid);
        controleDeAcesso.exigirVisivel(e.getUnidade(), VER);
        return detalhe(e);
    }

    // ── Escrita ────────────────────────────────────────────────────────────────────────────────────

    @Transactional
    public EquipeResponseDto criar(EquipeRequestDto dto) {
        UnidadeDeSaude unidade = unidadeDeSaudeRepository.findById(dto.getUnidadeId()).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar uma unidade de saúde com o id " + dto.getUnidadeId() + " em nossos registros."));
        controleDeAcesso.exigir(GERENCIAR, unidade);
        String nome = dto.getNome().trim();
        if (equipeRepository.existsByUnidade_UuidAndNomeIgnoreCase(unidade.getUuid(), nome)) {
            throw new ResourceConflictException("Já existe a equipe " + nome + " nesta unidade.");
        }
        Equipe e = new Equipe();
        e.setUnidade(unidade);
        e.setTipo(dto.getTipo());
        e.setNome(nome);
        e.setAtiva(dto.getAtiva() == null || dto.getAtiva());
        aplicar(e, dto, List.of());
        e = equipeRepository.save(e);
        ContextoAuditoria.unidade(unidade);
        ContextoAuditoria.registro(e.getUuid());
        return detalhe(e);
    }

    /** Edita nome, INE, situação, microáreas, coordenação, reunião e apoiadas. Unidade e tipo ficam. */
    @Transactional
    public EquipeResponseDto atualizar(UUID uuid, EquipeRequestDto dto) {
        Equipe e = buscar(uuid);
        controleDeAcesso.exigir(GERENCIAR, e.getUnidade());
        if (dto.getTipo() != e.getTipo()) {
            throw new ResourceUnprocessableEntityException("O tipo da equipe não muda pela edição.");
        }
        String nome = dto.getNome().trim();
        if (!nome.equalsIgnoreCase(e.getNome()) && equipeRepository.existsByUnidade_UuidAndNomeIgnoreCase(e.getUnidade().getUuid(), nome)) {
            throw new ResourceConflictException("Já existe a equipe " + nome + " nesta unidade.");
        }
        e.setNome(nome);
        e.setAtiva(dto.getAtiva() == null || dto.getAtiva());
        aplicar(e, dto, membroRepository.findByEquipe_UuidInAndFimIsNull(List.of(e.getUuid())));
        return detalhe(equipeRepository.save(e));
    }

    /**
     * Entrada de um profissional: lotado na unidade da equipe, uma participação vigente por equipe e, na eSF e na eAP, uma
     * equipe só.
     */
    @Transactional
    public EquipeResponseDto adicionarMembro(UUID equipeId, MembroEquipeRequestDto dto) {
        Equipe e = buscar(equipeId);
        controleDeAcesso.exigir(GERENCIAR, e.getUnidade());
        if (!e.isAtiva()) {
            throw new ResourceUnprocessableEntityException("A equipe está inativa; reative antes de incluir membros.");
        }
        Profissional p = profissionalRepository.findByMatricula(dto.getProfissionalMatricula()).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar um profissional com a matrícula " + dto.getProfissionalMatricula() + " em nossos registros."));
        boolean lotado = lotacaoRepository.findByProfissional_MatriculaOrderByDataInicioDesc(p.getMatricula()).stream()
                .anyMatch(l -> l.getDataFim() == null && l.getUnidade() != null && l.getUnidade().getUuid().equals(e.getUnidade().getUuid()));
        if (!lotado) {
            throw new ResourceUnprocessableEntityException(p.getNome() + " não tem lotação vigente na " + e.getUnidade().getNome()
                    + "; a equipe é da unidade (CNES).");
        }
        List<MembroEquipe> vigentes = membroRepository.findByProfissional_UuidAndFimIsNull(p.getUuid());
        if (vigentes.stream().anyMatch(m -> m.getEquipe().getUuid().equals(e.getUuid()))) {
            throw new ResourceConflictException(p.getNome() + " já é membro desta equipe.");
        }
        if (e.getTipo().exclusiva()) {
            vigentes.stream().filter(m -> m.getEquipe().getTipo().exclusiva() && m.getEquipe().isAtiva()).findFirst().ifPresent(m -> {
                throw new ResourceConflictException(p.getNome() + " já está na equipe " + m.getEquipe().getNome()
                        + "; na eSF e na eAP o profissional é de uma equipe só.");
            });
        }
        LocalDate inicio = dto.getInicio() != null ? dto.getInicio() : LocalDate.now();
        if (inicio.isAfter(LocalDate.now())) {
            throw new ResourceBadRequestException("A entrada não pode ser no futuro.");
        }
        MembroEquipe m = new MembroEquipe(null, e, p, dto.getFuncao(),
                dto.getMicroarea() == null || dto.getMicroarea().isBlank() ? null : dto.getMicroarea().trim(), inicio, null, null,
                UsuarioAutenticado.cpf());
        membroRepository.save(m);
        ContextoAuditoria.detalhe("Entrada de " + p.getMatricula() + " como " + dto.getFuncao() + ".");
        return detalhe(e);
    }

    /** Saída: fecha a participação com data e motivo. Quem coordena deixa de coordenar. */
    @Transactional
    public EquipeResponseDto registrarSaida(UUID membroId, SaidaMembroEquipeRequestDto dto) {
        MembroEquipe m = membroRepository.findById(membroId).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar a participação " + membroId + " em nossos registros."));
        Equipe e = m.getEquipe();
        controleDeAcesso.exigir(GERENCIAR, e.getUnidade());
        if (!m.vigente()) {
            throw new ResourceUnprocessableEntityException("Esta participação já foi encerrada.");
        }
        LocalDate fim = dto.getFim() != null ? dto.getFim() : LocalDate.now();
        if (fim.isBefore(m.getInicio()) || fim.isAfter(LocalDate.now())) {
            throw new ResourceBadRequestException("A saída precisa ser entre a entrada e hoje.");
        }
        m.setFim(fim);
        m.setMotivoSaida(dto.getMotivo().trim());
        membroRepository.save(m);
        if (e.getCoordenador() != null && e.getCoordenador().getUuid().equals(m.getProfissional().getUuid())) {
            e.setCoordenador(null);
            equipeRepository.save(e);
        }
        ContextoAuditoria.registro(e.getUuid());
        return detalhe(e);
    }

    // ── Apoio ──────────────────────────────────────────────────────────────────────────────────────

    private void aplicar(Equipe e, EquipeRequestDto dto, List<MembroEquipe> vigentes) {
        String ine = dto.getIne() == null || dto.getIne().isBlank() ? null : dto.getIne().trim();
        if (ine != null) {
            equipeRepository.findByIne(ine).filter(o -> !o.getUuid().equals(e.getUuid())).ifPresent(o -> {
                throw new ResourceConflictException("O INE " + ine + " já é da equipe " + o.getNome() + ".");
            });
        }
        e.setIne(ine);
        e.setMicroareas(dto.getMicroareas() == null || dto.getMicroareas().isBlank() ? null : dto.getMicroareas().trim());
        if (dto.getReuniaoInicio() != null && dto.getReuniaoFim() != null && !dto.getReuniaoFim().isAfter(dto.getReuniaoInicio())) {
            throw new ResourceBadRequestException("A reunião precisa terminar depois de começar.");
        }
        e.setReuniaoDia(dto.getReuniaoDia());
        e.setReuniaoInicio(dto.getReuniaoInicio());
        e.setReuniaoFim(dto.getReuniaoFim());
        e.setReuniaoLocal(dto.getReuniaoLocal() == null || dto.getReuniaoLocal().isBlank() ? null : dto.getReuniaoLocal().trim());
        String coord = dto.getCoordenadorMatricula() == null || dto.getCoordenadorMatricula().isBlank() ? null : dto.getCoordenadorMatricula().trim();
        if (coord == null) {
            e.setCoordenador(null);
        } else {
            MembroEquipe membro = vigentes.stream().filter(m -> coord.equals(m.getProfissional().getMatricula())).findFirst()
                    .orElseThrow(() -> new ResourceUnprocessableEntityException("Quem coordena precisa ser membro vigente da equipe."));
            e.setCoordenador(membro.getProfissional());
        }
        List<UUID> apoiadas = dto.getApoiadasIds() == null ? List.of() : dto.getApoiadasIds();
        if (!apoiadas.isEmpty() && e.getTipo() != TipoEquipe.EMULTI) {
            throw new ResourceUnprocessableEntityException("Só a eMulti apoia outras equipes.");
        }
        Set<Equipe> alvo = new HashSet<>();
        for (UUID id : apoiadas) {
            Equipe a = buscar(id);
            if (a.getTipo() != TipoEquipe.ESF && a.getTipo() != TipoEquipe.EAB) {
                throw new ResourceUnprocessableEntityException("A eMulti apoia equipes de Saúde da Família e de Atenção Primária; " + a.getNome() + " é " + a.getTipo() + ".");
            }
            alvo.add(a);
        }
        e.getApoiadas().clear();
        e.getApoiadas().addAll(alvo);
    }

    private List<Equipe> visiveis(UUID unidadeId) {
        return controleDeAcesso.filtrar(equipeRepository.findAllByOrderByNomeAsc().stream(), e -> Stream.of(e.getUnidade()), VER)
                .filter(e -> unidadeId == null || e.getUnidade().getUuid().equals(unidadeId))
                .toList();
    }

    private Map<UUID, List<MembroEquipe>> membrosVigentes(List<Equipe> equipes) {
        if (equipes.isEmpty()) {
            return Map.of();
        }
        return membroRepository.findByEquipe_UuidInAndFimIsNull(equipes.stream().map(Equipe::getUuid).toList()).stream()
                .collect(Collectors.groupingBy(m -> m.getEquipe().getUuid()));
    }

    private EquipeResumoDto resumo(Equipe e, List<MembroEquipe> vigentes) {
        Set<FuncaoEquipe> presentes = vigentes.stream().map(MembroEquipe::getFuncao).collect(Collectors.toSet());
        List<String> faltando = e.getTipo().composicaoMinima().stream()
                .filter(grupo -> grupo.stream().noneMatch(presentes::contains))
                .map(grupo -> grupo.stream().map(Enum::name).sorted().collect(Collectors.joining(" ou ")))
                .toList();
        return new EquipeResumoDto(e.getUuid(), e.getNome(), e.getTipo(), e.getIne(), e.isAtiva(), e.getUnidade().getUuid(),
                e.getUnidade().getNome(), e.getMicroareas(), e.getCoordenador() != null ? e.getCoordenador().getNome() : null,
                vigentes.size(), (int) vigentes.stream().filter(m -> m.getFuncao() == FuncaoEquipe.ACS).count(), faltando.isEmpty(), faltando,
                vigentes.stream().map(m -> m.getProfissional().getNome()).sorted(String.CASE_INSENSITIVE_ORDER).toList(), e.getApoiadas().size());
    }

    private EquipeResponseDto detalhe(Equipe e) {
        List<MembroEquipe> todos = membroRepository.findByEquipe_UuidOrderByInicioAsc(e.getUuid());
        List<MembroEquipe> vigentes = todos.stream().filter(MembroEquipe::vigente).toList();
        Map<String, Lotacao> lotacoes = lotacaoRepository.findVigentesComUnidadeECargo().stream()
                .filter(l -> l.getUnidade() != null && l.getUnidade().getUuid().equals(e.getUnidade().getUuid()))
                .collect(Collectors.toMap(l -> l.getProfissional().getMatricula(), Function.identity(), (a, b) -> a));
        Map<String, Afastamento> afastados = afastamentoRepository.findVigentesEm(LocalDate.now(), AFASTA).stream()
                .collect(Collectors.toMap(a -> a.getProfissional().getMatricula(), Function.identity(), (a, b) -> a));
        Function<MembroEquipe, EquipeResponseDto.Membro> membro = m -> {
            Profissional p = m.getProfissional();
            Lotacao l = lotacoes.get(p.getMatricula());
            Afastamento a = m.vigente() ? afastados.get(p.getMatricula()) : null;
            return new EquipeResponseDto.Membro(m.getUuid(), p.getMatricula(), p.getNome(),
                    p.getConselhoClasse() == null ? null : p.getConselhoClasse() + " " + Objects.toString(p.getNumeroConselho(), ""),
                    m.getFuncao(), m.getMicroarea(), m.getInicio(), m.getFim(), m.getMotivoSaida(),
                    l != null && l.getCargo() != null ? l.getCargo().getNome() : null, l != null ? l.getJornadaSemanalHoras() : null,
                    a != null ? a.getTipo().name() : null, a != null ? a.getDataFim() : null);
        };
        List<EquipeResponseDto.Vinculo> apoiadas = e.getApoiadas().stream()
                .sorted(Comparator.comparing(Equipe::getNome, String.CASE_INSENSITIVE_ORDER))
                .map(a -> new EquipeResponseDto.Vinculo(a.getUuid(), a.getNome(), a.getTipo().name(), a.getUnidade().getNome())).toList();
        List<EquipeResponseDto.Vinculo> apoiadaPor = equipeRepository.findAllByOrderByNomeAsc().stream()
                .filter(o -> o.getApoiadas().stream().anyMatch(a -> a.getUuid().equals(e.getUuid())))
                .map(o -> new EquipeResponseDto.Vinculo(o.getUuid(), o.getNome(), o.getTipo().name(), o.getUnidade().getNome())).toList();
        return new EquipeResponseDto(resumo(e, vigentes), e.getCoordenador() != null ? e.getCoordenador().getMatricula() : null,
                e.getReuniaoDia(), e.getReuniaoInicio(), e.getReuniaoFim(), e.getReuniaoLocal(),
                vigentes.stream().sorted(Comparator.comparing(MembroEquipe::getFuncao).thenComparing(m -> m.getProfissional().getNome()))
                        .map(membro).toList(),
                todos.stream().filter(m -> !m.vigente()).sorted(Comparator.comparing(MembroEquipe::getFim).reversed()).map(membro).toList(),
                apoiadas, apoiadaPor);
    }

    private Equipe buscar(UUID uuid) {
        return equipeRepository.findById(uuid).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar uma equipe com o id " + uuid + " em nossos registros."));
    }
}
