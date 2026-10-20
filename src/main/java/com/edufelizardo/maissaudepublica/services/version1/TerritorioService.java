package com.edufelizardo.maissaudepublica.services.version1;

import com.edufelizardo.maissaudepublica.config.ContextoAuditoria;
import com.edufelizardo.maissaudepublica.config.UsuarioAutenticado;
import com.edufelizardo.maissaudepublica.exceptions.ResourceBadRequestException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceConflictException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceNotFoundException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceUnprocessableEntityException;
import com.edufelizardo.maissaudepublica.models.Area;
import com.edufelizardo.maissaudepublica.models.AtribuicaoMicroarea;
import com.edufelizardo.maissaudepublica.models.Equipe;
import com.edufelizardo.maissaudepublica.models.MembroEquipe;
import com.edufelizardo.maissaudepublica.models.Microarea;
import com.edufelizardo.maissaudepublica.models.ResponsabilidadeTerritorial;
import com.edufelizardo.maissaudepublica.models.SegmentoTerritorial;
import com.edufelizardo.maissaudepublica.models.Territorio;
import com.edufelizardo.maissaudepublica.models.UnidadeDeSaude;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.AreaRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.AtribuicaoMicroareaRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.CoordenadasUnidadeRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.EncerramentoAtribuicaoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.MicroareaRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.ResponsabilidadeTerritorialRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.SegmentoTerritorialRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.TerritorioRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.AreaDetalheDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.ArvoreTerritorialDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.ArvoreTerritorialDto.AcsRef;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.ArvoreTerritorialDto.AreaNo;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.ArvoreTerritorialDto.EquipeRef;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.ArvoreTerritorialDto.MicroareaNo;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.TerritorioDaEquipeDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.UnidadeNoMapaDto;
import com.edufelizardo.maissaudepublica.models.enuns.FuncaoEquipe;
import com.edufelizardo.maissaudepublica.repositories.AreaRepository;
import com.edufelizardo.maissaudepublica.repositories.AtribuicaoMicroareaRepository;
import com.edufelizardo.maissaudepublica.repositories.EquipeRepository;
import com.edufelizardo.maissaudepublica.repositories.MembroEquipeRepository;
import com.edufelizardo.maissaudepublica.repositories.MicroareaRepository;
import com.edufelizardo.maissaudepublica.repositories.ResponsabilidadeTerritorialRepository;
import com.edufelizardo.maissaudepublica.repositories.SegmentoTerritorialRepository;
import com.edufelizardo.maissaudepublica.repositories.TerritorioRepository;
import com.edufelizardo.maissaudepublica.repositories.UnidadeDeSaudeRepository;
import org.locationtech.jts.geom.Geometry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Território e adscrição, fatia F1 (ADR-0125, desenho na ADR-0108): segmento, território com finalidade, área e
 * microárea com GeoJSON; a equipe responsável pela área e o ACS da microárea como histórico; as coordenadas da unidade.
 * Ler é de quem está logado (recorte geográfico não é dado de saúde); cadastrar pede {@value #GERENCIAR} na unidade de
 * gestão do segmento, e as associações, na unidade da equipe.
 */
@Service
public class TerritorioService {

    public static final String GERENCIAR = "TERRITORIO.GERENCIAR";

    @Autowired
    private SegmentoTerritorialRepository segmentoRepository;

    @Autowired
    private TerritorioRepository territorioRepository;

    @Autowired
    private AreaRepository areaRepository;

    @Autowired
    private MicroareaRepository microareaRepository;

    @Autowired
    private ResponsabilidadeTerritorialRepository responsabilidadeRepository;

    @Autowired
    private AtribuicaoMicroareaRepository atribuicaoRepository;

    @Autowired
    private EquipeRepository equipeRepository;

    @Autowired
    private MembroEquipeRepository membroRepository;

    @Autowired
    private UnidadeDeSaudeRepository unidadeDeSaudeRepository;

    @Autowired
    private ControleDeAcesso controleDeAcesso;

    // ── Leitura ────────────────────────────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public ArvoreTerritorialDto arvore() {
        Map<UUID, ResponsabilidadeTerritorial> equipes = responsabilidadeRepository.findByFimIsNull().stream()
                .collect(Collectors.toMap(r -> r.getArea().getUuid(), Function.identity(), (a, b) -> a));
        Map<UUID, AtribuicaoMicroarea> acs = atribuicaoRepository.findByFimIsNull().stream()
                .collect(Collectors.toMap(a -> a.getMicroarea().getUuid(), Function.identity(), (a, b) -> a));
        Map<UUID, List<MicroareaNo>> microareas = microareaRepository.findAllByOrderByCodigoAsc().stream()
                .collect(Collectors.groupingBy(m -> m.getArea().getUuid(), Collectors.mapping(m -> microareaNo(m, acs.get(m.getUuid())), Collectors.toList())));
        Map<UUID, List<AreaNo>> areas = areaRepository.findAllByOrderByCodigoAsc().stream()
                .collect(Collectors.groupingBy(a -> a.getTerritorio().getUuid(), Collectors.mapping(
                        a -> areaNo(a, equipes.get(a.getUuid()), microareas.getOrDefault(a.getUuid(), List.of())), Collectors.toList())));
        Map<UUID, List<ArvoreTerritorialDto.TerritorioNo>> territorios = territorioRepository.findAllByOrderByNomeAsc().stream()
                .collect(Collectors.groupingBy(t -> t.getSegmento().getUuid(), Collectors.mapping(
                        t -> new ArvoreTerritorialDto.TerritorioNo(t.getUuid(), t.getSegmento().getUuid(), t.getNome(), t.getFinalidade(), t.isAtivo(),
                                areas.getOrDefault(t.getUuid(), List.of())), Collectors.toList())));
        List<ArvoreTerritorialDto.Segmento> segmentos = segmentoRepository.findAllByOrderByNomeAsc().stream()
                .map(s -> new ArvoreTerritorialDto.Segmento(s.getUuid(), s.getCodigo(), s.getNome(), s.getTipo(),
                        s.getUnidadeGestao() != null ? s.getUnidadeGestao().getUuid() : null,
                        s.getUnidadeGestao() != null ? s.getUnidadeGestao().getNome() : null, s.getGeometria(),
                        territorios.getOrDefault(s.getUuid(), List.of())))
                .toList();
        List<AreaNo> todasAreas = areas.values().stream().flatMap(List::stream).toList();
        List<MicroareaNo> todasMicro = todasAreas.stream().flatMap(a -> a.getMicroareas().stream()).toList();
        ArvoreTerritorialDto.Indicadores indicadores = new ArvoreTerritorialDto.Indicadores(todasAreas.size(),
                (int) todasAreas.stream().filter(a -> a.getEquipe() == null).count(), todasMicro.size(),
                (int) todasMicro.stream().filter(m -> m.getAcs() == null).count(),
                (int) (todasAreas.stream().filter(a -> a.getGeometria() == null).count() + todasMicro.stream().filter(m -> m.getGeometria() == null).count()),
                (int) todasMicro.stream().filter(m -> m.getPopulacaoReferencia() != null && m.getPopulacaoReferencia() > Microarea.POPULACAO_REFERENCIA_CNES).count());
        return new ArvoreTerritorialDto(indicadores, segmentos);
    }

    @Transactional(readOnly = true)
    public AreaDetalheDto area(UUID uuid) {
        return detalhe(buscarArea(uuid));
    }

    @Transactional(readOnly = true)
    public TerritorioDaEquipeDto daEquipe(UUID equipeId) {
        Equipe e = equipeRepository.findById(equipeId).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar uma equipe com o id " + equipeId + " em nossos registros."));
        return responsabilidadeRepository.findByEquipe_UuidAndFimIsNull(e.getUuid())
                .map(r -> {
                    Territorio t = r.getArea().getTerritorio();
                    return new TerritorioDaEquipeDto(e.getUuid(), t.getNome(), t.getFinalidade(), areaNo(r.getArea()));
                })
                .orElseGet(() -> new TerritorioDaEquipeDto(e.getUuid(), null, null, null));
    }

    @Transactional(readOnly = true)
    public List<UnidadeNoMapaDto> unidades() {
        return unidadeDeSaudeRepository.findAll().stream().filter(UnidadeDeSaude::isAtivo)
                .sorted(Comparator.comparing(UnidadeDeSaude::getNome, String.CASE_INSENSITIVE_ORDER))
                .map(this::unidadeNoMapa).toList();
    }

    // ── Cadastro ───────────────────────────────────────────────────────────────────────────────────

    @Transactional
    public ArvoreTerritorialDto.Segmento criarSegmento(SegmentoTerritorialRequestDto dto) {
        SegmentoTerritorial s = new SegmentoTerritorial();
        aplicar(s, dto);
        s = segmentoRepository.save(s);
        ContextoAuditoria.registro(s.getUuid());
        return segmento(s);
    }

    @Transactional
    public ArvoreTerritorialDto.Segmento atualizarSegmento(UUID uuid, SegmentoTerritorialRequestDto dto) {
        SegmentoTerritorial s = segmentoRepository.findById(uuid).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar um segmento territorial com o id " + uuid + " em nossos registros."));
        controleDeAcesso.exigir(GERENCIAR, s.getUnidadeGestao());
        aplicar(s, dto);
        ContextoAuditoria.registro(s.getUuid());
        return segmento(segmentoRepository.save(s));
    }

    @Transactional
    public ArvoreTerritorialDto.TerritorioNo criarTerritorio(TerritorioRequestDto dto) {
        SegmentoTerritorial s = segmentoRepository.findById(dto.getSegmentoId()).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar um segmento territorial com o id " + dto.getSegmentoId() + " em nossos registros."));
        controleDeAcesso.exigir(GERENCIAR, s.getUnidadeGestao());
        String nome = dto.getNome().trim();
        if (territorioRepository.existsBySegmento_UuidAndNomeIgnoreCase(s.getUuid(), nome)) {
            throw new ResourceConflictException("Já existe o território " + nome + " neste segmento.");
        }
        Territorio t = new Territorio(null, s, nome, dto.getFinalidade(), dto.getAtivo() == null || dto.getAtivo());
        t = territorioRepository.save(t);
        ContextoAuditoria.registro(t.getUuid());
        return territorioNo(t);
    }

    @Transactional
    public ArvoreTerritorialDto.TerritorioNo atualizarTerritorio(UUID uuid, TerritorioRequestDto dto) {
        Territorio t = buscarTerritorio(uuid);
        controleDeAcesso.exigir(GERENCIAR, t.getSegmento().getUnidadeGestao());
        if (!dto.getSegmentoId().equals(t.getSegmento().getUuid())) {
            throw new ResourceUnprocessableEntityException("O segmento do território não muda pela edição.");
        }
        String nome = dto.getNome().trim();
        if (!nome.equalsIgnoreCase(t.getNome()) && territorioRepository.existsBySegmento_UuidAndNomeIgnoreCase(t.getSegmento().getUuid(), nome)) {
            throw new ResourceConflictException("Já existe o território " + nome + " neste segmento.");
        }
        t.setNome(nome);
        t.setFinalidade(dto.getFinalidade());
        t.setAtivo(dto.getAtivo() == null || dto.getAtivo());
        ContextoAuditoria.registro(t.getUuid());
        return territorioNo(territorioRepository.save(t));
    }

    @Transactional
    public AreaDetalheDto criarArea(AreaRequestDto dto) {
        Territorio t = buscarTerritorio(dto.getTerritorioId());
        controleDeAcesso.exigir(GERENCIAR, t.getSegmento().getUnidadeGestao());
        if (!t.isAtivo()) {
            throw new ResourceUnprocessableEntityException("O território " + t.getNome() + " está inativo; reative antes de incluir áreas.");
        }
        Area a = new Area();
        a.setTerritorio(t);
        aplicar(a, dto);
        a = areaRepository.save(a);
        ContextoAuditoria.registro(a.getUuid());
        return detalhe(a);
    }

    /** Mudando o desenho da área, as microáreas desenhadas precisam continuar dentro dela (422). */
    @Transactional
    public AreaDetalheDto atualizarArea(UUID uuid, AreaRequestDto dto) {
        Area a = buscarArea(uuid);
        controleDeAcesso.exigir(GERENCIAR, a.getTerritorio().getSegmento().getUnidadeGestao());
        if (!dto.getTerritorioId().equals(a.getTerritorio().getUuid())) {
            throw new ResourceUnprocessableEntityException("O território da área não muda pela edição.");
        }
        aplicar(a, dto);
        Geometry contorno = GeometriaTerritorial.jts(a.getGeometria());
        if (contorno != null) {
            List<String> fora = microareaRepository.findByArea_UuidOrderByCodigoAsc(a.getUuid()).stream()
                    .filter(m -> m.getGeometria() != null && !GeometriaTerritorial.dentro(GeometriaTerritorial.jts(m.getGeometria()), contorno))
                    .map(Microarea::getCodigo).toList();
            if (!fora.isEmpty()) {
                throw new ResourceUnprocessableEntityException("Com esse desenho, a(s) microárea(s) " + String.join(", ", fora)
                        + " ficariam fora da área.");
            }
        }
        ContextoAuditoria.registro(a.getUuid());
        return detalhe(areaRepository.save(a));
    }

    @Transactional
    public AreaDetalheDto criarMicroarea(MicroareaRequestDto dto) {
        Area a = buscarArea(dto.getAreaId());
        controleDeAcesso.exigir(GERENCIAR, a.getTerritorio().getSegmento().getUnidadeGestao());
        Microarea m = new Microarea();
        m.setArea(a);
        aplicar(m, dto);
        m = microareaRepository.save(m);
        ContextoAuditoria.registro(m.getUuid());
        return detalhe(a);
    }

    @Transactional
    public AreaDetalheDto atualizarMicroarea(UUID uuid, MicroareaRequestDto dto) {
        Microarea m = buscarMicroarea(uuid);
        controleDeAcesso.exigir(GERENCIAR, m.getArea().getTerritorio().getSegmento().getUnidadeGestao());
        if (!dto.getAreaId().equals(m.getArea().getUuid())) {
            throw new ResourceUnprocessableEntityException("A área da microárea não muda pela edição.");
        }
        aplicar(m, dto);
        microareaRepository.save(m);
        ContextoAuditoria.registro(m.getUuid());
        return detalhe(m.getArea());
    }

    // ── Associações (histórico) ────────────────────────────────────────────────────────────────────

    /**
     * Equipe responsável pela área. Uma vigente por área e uma área vigente por equipe (409). Trocando, a anterior fecha
     * na data de início da nova, com motivo, e os ACS dela deixam as microáreas.
     */
    @Transactional
    public AreaDetalheDto atribuirEquipe(UUID areaId, ResponsabilidadeTerritorialRequestDto dto) {
        Area a = buscarArea(areaId);
        Equipe e = equipeRepository.findById(dto.getEquipeId()).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar uma equipe com o id " + dto.getEquipeId() + " em nossos registros."));
        controleDeAcesso.exigir(GERENCIAR, e.getUnidade());
        if (!e.isAtiva()) {
            throw new ResourceUnprocessableEntityException("A equipe " + e.getNome() + " está inativa.");
        }
        LocalDate inicio = inicio(dto.getInicio());
        ResponsabilidadeTerritorial atual = responsabilidadeRepository.findByArea_UuidAndFimIsNull(a.getUuid()).orElse(null);
        if (atual != null && atual.getEquipe().getUuid().equals(e.getUuid())) {
            throw new ResourceConflictException("A equipe " + e.getNome() + " já é a responsável por esta área.");
        }
        responsabilidadeRepository.findByEquipe_UuidAndFimIsNull(e.getUuid()).ifPresent(r -> {
            throw new ResourceConflictException("A equipe " + e.getNome() + " já responde pela área " + r.getArea().getCodigo()
                    + "; uma equipe tem uma área vigente.");
        });
        if (atual != null) {
            String motivo = motivo(dto.getMotivo(), "Informe o motivo da troca de equipe.");
            if (inicio.isBefore(atual.getInicio())) {
                throw new ResourceBadRequestException("A nova equipe não pode começar antes da atual (" + atual.getInicio() + ").");
            }
            atual.setFim(inicio);
            atual.setMotivoFim(motivo);
            responsabilidadeRepository.saveAndFlush(atual);
            for (AtribuicaoMicroarea at : atribuicaoRepository.findByMicroarea_Area_UuidAndFimIsNull(a.getUuid())) {
                at.setFim(inicio.isBefore(at.getInicio()) ? at.getInicio() : inicio);
                at.setMotivoFim("Troca da equipe responsável: " + motivo);
                atribuicaoRepository.saveAndFlush(at);
            }
        }
        responsabilidadeRepository.save(new ResponsabilidadeTerritorial(null, a, e, inicio, null, null, UsuarioAutenticado.cpf()));
        ContextoAuditoria.registro(a.getUuid());
        ContextoAuditoria.detalhe("Equipe " + e.getNome() + " responsável pela área " + a.getCodigo() + ".");
        return detalhe(a);
    }

    /**
     * ACS da microárea: membro vigente, com a função ACS, da equipe responsável pela área (422). Um ACS por microárea e
     * uma microárea por ACS (409). Trocando, o anterior sai na data de início do novo, com motivo.
     */
    @Transactional
    public AreaDetalheDto atribuirAcs(UUID microareaId, AtribuicaoMicroareaRequestDto dto) {
        Microarea m = buscarMicroarea(microareaId);
        ResponsabilidadeTerritorial r = responsabilidadeRepository.findByArea_UuidAndFimIsNull(m.getArea().getUuid())
                .orElseThrow(() -> new ResourceUnprocessableEntityException("A área " + m.getArea().getCodigo()
                        + " ainda não tem equipe responsável; atribua a equipe antes do ACS."));
        controleDeAcesso.exigir(GERENCIAR, r.getEquipe().getUnidade());
        MembroEquipe membro = membroRepository.findById(dto.getMembroId()).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar a participação " + dto.getMembroId() + " em nossos registros."));
        String nome = membro.getProfissional().getNome();
        if (!membro.vigente()) {
            throw new ResourceUnprocessableEntityException(nome + " já saiu da equipe " + membro.getEquipe().getNome() + ".");
        }
        if (membro.getFuncao() != FuncaoEquipe.ACS) {
            throw new ResourceUnprocessableEntityException(nome + " está na equipe como " + membro.getFuncao()
                    + "; a microárea é do agente comunitário de saúde (ACS).");
        }
        if (!membro.getEquipe().getUuid().equals(r.getEquipe().getUuid())) {
            throw new ResourceUnprocessableEntityException(nome + " é da equipe " + membro.getEquipe().getNome()
                    + "; a área " + m.getArea().getCodigo() + " é da equipe " + r.getEquipe().getNome() + ".");
        }
        LocalDate inicio = inicio(dto.getInicio());
        AtribuicaoMicroarea atual = atribuicaoRepository.findByMicroarea_UuidAndFimIsNull(m.getUuid()).orElse(null);
        if (atual != null && atual.getMembro().getUuid().equals(membro.getUuid())) {
            throw new ResourceConflictException(nome + " já é o ACS da microárea " + m.getCodigo() + ".");
        }
        atribuicaoRepository.findByMembro_UuidAndFimIsNull(membro.getUuid()).ifPresent(o -> {
            throw new ResourceConflictException(nome + " já é o ACS da microárea " + o.getMicroarea().getCodigo()
                    + "; um ACS tem uma microárea vigente.");
        });
        if (inicio.isBefore(membro.getInicio())) {
            throw new ResourceBadRequestException("O início não pode ser antes da entrada de " + nome + " na equipe (" + membro.getInicio() + ").");
        }
        if (atual != null) {
            String motivo = motivo(dto.getMotivo(), "Informe o motivo da troca de ACS.");
            if (inicio.isBefore(atual.getInicio())) {
                throw new ResourceBadRequestException("O novo ACS não pode começar antes do atual (" + atual.getInicio() + ").");
            }
            atual.setFim(inicio);
            atual.setMotivoFim(motivo);
            atribuicaoRepository.saveAndFlush(atual);
        }
        atribuicaoRepository.save(new AtribuicaoMicroarea(null, m, membro, inicio, null, null, UsuarioAutenticado.cpf()));
        ContextoAuditoria.registro(m.getUuid());
        ContextoAuditoria.detalhe("ACS " + membro.getProfissional().getMatricula() + " na microárea " + m.getCodigo() + ".");
        return detalhe(m.getArea());
    }

    @Transactional
    public AreaDetalheDto encerrarAcs(UUID microareaId, EncerramentoAtribuicaoRequestDto dto) {
        Microarea m = buscarMicroarea(microareaId);
        AtribuicaoMicroarea atual = atribuicaoRepository.findByMicroarea_UuidAndFimIsNull(m.getUuid())
                .orElseThrow(() -> new ResourceUnprocessableEntityException("A microárea " + m.getCodigo() + " está sem ACS."));
        controleDeAcesso.exigir(GERENCIAR, atual.getMembro().getEquipe().getUnidade());
        LocalDate fim = dto.getFim() != null ? dto.getFim() : LocalDate.now();
        if (fim.isBefore(atual.getInicio()) || fim.isAfter(LocalDate.now())) {
            throw new ResourceBadRequestException("O fim precisa ser entre o início (" + atual.getInicio() + ") e hoje.");
        }
        atual.setFim(fim);
        atual.setMotivoFim(dto.getMotivo().trim());
        atribuicaoRepository.save(atual);
        ContextoAuditoria.registro(m.getUuid());
        return detalhe(m.getArea());
    }

    /** Latitude e longitude da unidade, juntas ou nenhuma (400). */
    @Transactional
    public UnidadeNoMapaDto definirCoordenadas(UUID unidadeId, CoordenadasUnidadeRequestDto dto) {
        UnidadeDeSaude u = unidadeDeSaudeRepository.findById(unidadeId).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar uma unidade de saúde com o id " + unidadeId + " em nossos registros."));
        controleDeAcesso.exigir("ORGANIZACAO.GERENCIAR", u);
        if ((dto.getLatitude() == null) != (dto.getLongitude() == null)) {
            throw new ResourceBadRequestException("Informe a latitude e a longitude juntas, ou nenhuma das duas.");
        }
        u.setLatitude(dto.getLatitude());
        u.setLongitude(dto.getLongitude());
        ContextoAuditoria.registro(u.getUuid());
        return unidadeNoMapa(unidadeDeSaudeRepository.save(u));
    }

    // ── Regras do cadastro ─────────────────────────────────────────────────────────────────────────

    private void aplicar(SegmentoTerritorial s, SegmentoTerritorialRequestDto dto) {
        UnidadeDeSaude unidade = dto.getUnidadeGestaoId() == null ? null : unidadeDeSaudeRepository.findById(dto.getUnidadeGestaoId())
                .orElseThrow(() -> new ResourceNotFoundException("Não foi possível encontrar uma unidade de saúde com o id "
                        + dto.getUnidadeGestaoId() + " em nossos registros."));
        controleDeAcesso.exigir(GERENCIAR, unidade);
        String codigo = dto.getCodigo().trim().toUpperCase();
        segmentoRepository.findByCodigoIgnoreCase(codigo).filter(o -> !o.getUuid().equals(s.getUuid())).ifPresent(o -> {
            throw new ResourceConflictException("O código " + codigo + " já é do segmento " + o.getNome() + ".");
        });
        s.setCodigo(codigo);
        s.setNome(dto.getNome().trim());
        s.setTipo(dto.getTipo());
        s.setUnidadeGestao(unidade);
        s.setGeometria(GeometriaTerritorial.normalizar(dto.getGeometria()));
    }

    private void aplicar(Area a, AreaRequestDto dto) {
        String codigo = dto.getCodigo().trim().toUpperCase();
        areaRepository.findByTerritorio_UuidAndCodigoIgnoreCase(a.getTerritorio().getUuid(), codigo)
                .filter(o -> !o.getUuid().equals(a.getUuid())).ifPresent(o -> {
                    throw new ResourceConflictException("Já existe a área " + codigo + " neste território.");
                });
        a.setCodigo(codigo);
        a.setCodigoCnes(dto.getCodigoCnes() == null || dto.getCodigoCnes().isBlank() ? null : dto.getCodigoCnes().trim());
        a.setNome(dto.getNome().trim());
        a.setGeometria(GeometriaTerritorial.normalizar(dto.getGeometria()));
    }

    /** A microárea fica dentro da área e não se sobrepõe às irmãs, quando as geometrias existem (422). */
    private void aplicar(Microarea m, MicroareaRequestDto dto) {
        Area area = m.getArea();
        String codigo = dto.getCodigo().trim().toUpperCase();
        microareaRepository.findByArea_UuidAndCodigoIgnoreCase(area.getUuid(), codigo)
                .filter(o -> !o.getUuid().equals(m.getUuid())).ifPresent(o -> {
                    throw new ResourceConflictException("Já existe a microárea " + codigo + " na área " + area.getCodigo() + ".");
                });
        String geometria = GeometriaTerritorial.normalizar(dto.getGeometria());
        Geometry g = GeometriaTerritorial.jts(geometria);
        if (g != null) {
            Geometry contorno = GeometriaTerritorial.jts(area.getGeometria());
            if (contorno != null && !GeometriaTerritorial.dentro(g, contorno)) {
                throw new ResourceUnprocessableEntityException("A microárea " + codigo + " sai do contorno da área " + area.getCodigo() + ".");
            }
            List<String> sobrepostas = microareaRepository.findByArea_UuidOrderByCodigoAsc(area.getUuid()).stream()
                    .filter(o -> !o.getUuid().equals(m.getUuid()) && o.getGeometria() != null)
                    .filter(o -> GeometriaTerritorial.sobrepoe(g, GeometriaTerritorial.jts(o.getGeometria())))
                    .map(Microarea::getCodigo).toList();
            if (!sobrepostas.isEmpty()) {
                throw new ResourceUnprocessableEntityException("A microárea " + codigo + " se sobrepõe à(s) microárea(s) "
                        + String.join(", ", sobrepostas) + "; microáreas da mesma área não se sobrepõem.");
            }
        }
        m.setCodigo(codigo);
        m.setNome(dto.getNome() == null || dto.getNome().isBlank() ? null : dto.getNome().trim());
        m.setGeometria(geometria);
        m.setPopulacaoReferencia(dto.getPopulacaoReferencia());
    }

    private static LocalDate inicio(LocalDate informado) {
        LocalDate inicio = informado != null ? informado : LocalDate.now();
        if (inicio.isAfter(LocalDate.now())) {
            throw new ResourceBadRequestException("O início não pode ser no futuro.");
        }
        return inicio;
    }

    private static String motivo(String informado, String falta) {
        if (informado == null || informado.isBlank()) {
            throw new ResourceBadRequestException(falta);
        }
        String m = informado.trim();
        return m.length() > 400 ? m.substring(0, 400) : m;
    }

    // ── Montagem ───────────────────────────────────────────────────────────────────────────────────

    private AreaDetalheDto detalhe(Area a) {
        List<ResponsabilidadeTerritorial> responsabilidades = responsabilidadeRepository.findByArea_UuidOrderByInicioDesc(a.getUuid());
        List<AtribuicaoMicroarea> atribuicoes = atribuicaoRepository.findByMicroarea_Area_UuidOrderByInicioDesc(a.getUuid());
        Territorio t = a.getTerritorio();
        List<AcsRef> semMicroarea = new ArrayList<>();
        responsabilidades.stream().filter(ResponsabilidadeTerritorial::vigente).findFirst().ifPresent(r -> {
            Set<UUID> comMicroarea = atribuicaoRepository.findByFimIsNull().stream().map(x -> x.getMembro().getUuid()).collect(Collectors.toSet());
            membroRepository.findByEquipe_UuidInAndFimIsNull(List.of(r.getEquipe().getUuid())).stream()
                    .filter(m -> m.getFuncao() == FuncaoEquipe.ACS && !comMicroarea.contains(m.getUuid()))
                    .sorted(Comparator.comparing(m -> m.getProfissional().getNome(), String.CASE_INSENSITIVE_ORDER))
                    .forEach(m -> semMicroarea.add(new AcsRef(m.getUuid(), m.getProfissional().getMatricula(), m.getProfissional().getNome(), null)));
        });
        return new AreaDetalheDto(areaNo(a), t.getNome(), t.getFinalidade(), t.getSegmento().getNome(),
                responsabilidades.stream().map(r -> new AreaDetalheDto.Responsabilidade(r.getUuid(), r.getEquipe().getUuid(), r.getEquipe().getNome(),
                        r.getInicio(), r.getFim(), r.getMotivoFim())).toList(),
                atribuicoes.stream().map(x -> new AreaDetalheDto.Atribuicao(x.getUuid(), x.getMicroarea().getUuid(), x.getMicroarea().getCodigo(),
                        x.getMembro().getUuid(), x.getMembro().getProfissional().getMatricula(), x.getMembro().getProfissional().getNome(),
                        x.getInicio(), x.getFim(), x.getMotivoFim())).toList(),
                semMicroarea);
    }

    private AreaNo areaNo(Area a) {
        Map<UUID, AtribuicaoMicroarea> acs = atribuicaoRepository.findByMicroarea_Area_UuidAndFimIsNull(a.getUuid()).stream()
                .collect(Collectors.toMap(x -> x.getMicroarea().getUuid(), Function.identity(), (x, y) -> x));
        return areaNo(a, responsabilidadeRepository.findByArea_UuidAndFimIsNull(a.getUuid()).orElse(null),
                microareaRepository.findByArea_UuidOrderByCodigoAsc(a.getUuid()).stream().map(m -> microareaNo(m, acs.get(m.getUuid()))).toList());
    }

    private static AreaNo areaNo(Area a, ResponsabilidadeTerritorial r, List<MicroareaNo> microareas) {
        EquipeRef equipe = r == null ? null : new EquipeRef(r.getEquipe().getUuid(), r.getEquipe().getNome(), r.getEquipe().getTipo(),
                r.getEquipe().getUnidade().getUuid(), r.getEquipe().getUnidade().getNome(), r.getInicio());
        return new AreaNo(a.getUuid(), a.getTerritorio().getUuid(), a.getCodigo(), a.getCodigoCnes(), a.getNome(), a.getGeometria(), equipe, microareas);
    }

    private static MicroareaNo microareaNo(Microarea m, AtribuicaoMicroarea a) {
        List<String> avisos = new ArrayList<>();
        if (a == null) {
            avisos.add("Sem ACS");
        }
        if (m.getGeometria() == null) {
            avisos.add("Sem desenho");
        }
        if (m.getPopulacaoReferencia() != null && m.getPopulacaoReferencia() > Microarea.POPULACAO_REFERENCIA_CNES) {
            avisos.add("População de referência acima de " + Microarea.POPULACAO_REFERENCIA_CNES + " pessoas (CNES)");
        }
        AcsRef acs = a == null ? null : new AcsRef(a.getMembro().getUuid(), a.getMembro().getProfissional().getMatricula(),
                a.getMembro().getProfissional().getNome(), a.getInicio());
        return new MicroareaNo(m.getUuid(), m.getArea().getUuid(), m.getCodigo(), m.getNome(), m.getGeometria(), m.getPopulacaoReferencia(), acs, avisos);
    }

    private ArvoreTerritorialDto.Segmento segmento(SegmentoTerritorial s) {
        return new ArvoreTerritorialDto.Segmento(s.getUuid(), s.getCodigo(), s.getNome(), s.getTipo(),
                s.getUnidadeGestao() != null ? s.getUnidadeGestao().getUuid() : null,
                s.getUnidadeGestao() != null ? s.getUnidadeGestao().getNome() : null, s.getGeometria(), List.of());
    }

    private ArvoreTerritorialDto.TerritorioNo territorioNo(Territorio t) {
        return new ArvoreTerritorialDto.TerritorioNo(t.getUuid(), t.getSegmento().getUuid(), t.getNome(), t.getFinalidade(), t.isAtivo(), List.of());
    }

    private UnidadeNoMapaDto unidadeNoMapa(UnidadeDeSaude u) {
        return new UnidadeNoMapaDto(u.getUuid(), u.getNome(), u.getTipo(), u.getCnes(), u.getLatitude(), u.getLongitude());
    }

    private Territorio buscarTerritorio(UUID uuid) {
        return territorioRepository.findById(uuid).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar um território com o id " + uuid + " em nossos registros."));
    }

    private Area buscarArea(UUID uuid) {
        return areaRepository.findById(uuid).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar uma área com o id " + uuid + " em nossos registros."));
    }

    private Microarea buscarMicroarea(UUID uuid) {
        return microareaRepository.findById(Objects.requireNonNull(uuid)).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar uma microárea com o id " + uuid + " em nossos registros."));
    }
}
