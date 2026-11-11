package com.edufelizardo.maissaudepublica.services.version1;

import com.edufelizardo.maissaudepublica.config.ContextoAuditoria;
import com.edufelizardo.maissaudepublica.config.UsuarioAutenticado;
import com.edufelizardo.maissaudepublica.exceptions.ResourceBadRequestException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceNotFoundException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceUnprocessableEntityException;
import com.edufelizardo.maissaudepublica.models.*;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.CampanhaComunicacaoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.EncerramentoCampanhaRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.MaterialEducativoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.MotivoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.CampanhaComunicacaoDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.CampanhaComunicacaoPublicaDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.EducacaoPermanenteDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.MaterialEducativoDto;
import com.edufelizardo.maissaudepublica.models.enuns.PublicoAlvoCampanha;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoCampanhaComunicacao;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoConteudo;
import com.edufelizardo.maissaudepublica.models.enuns.TipoCanalComunicacao;
import com.edufelizardo.maissaudepublica.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Campanhas de comunicação, fatia C2 (ADR-0174): objetivo, público, territórios, conteúdos revisados e comunicados, com o alcance por
 * canal. E o material educativo de cada conteúdo, sem arquivo até a D2 de Documentos.
 */
@Service
public class CampanhaComunicacaoService {

    static final String PUBLICAR = "COMUNICACAO.PUBLICAR";
    static final String EDITAR = "CONTEUDO.EDITAR";
    private static final String[] LER = {PUBLICAR, EDITAR, "CONTEUDO.REVISAR"};
    private static final Set<PublicoAlvoCampanha> PROFISSIONAIS = EnumSet.of(PublicoAlvoCampanha.PROFISSIONAIS_SAUDE, PublicoAlvoCampanha.ACS);

    @Autowired private CampanhaComunicacaoRepository campanhaRepository;
    @Autowired private MaterialEducativoRepository materialRepository;
    @Autowired private ComunicadoRepository comunicadoRepository;
    @Autowired private PublicacaoRepository publicacaoRepository;
    @Autowired private CanalComunicacaoRepository canalRepository;
    @Autowired private TerritorioRepository territorioRepository;
    @Autowired private UnidadeDeSaudeRepository unidadeRepository;
    @Autowired private ProgramaRepository programaRepository;
    @Autowired private CampanhaRepository campanhaOperacionalRepository;
    @Autowired private ConteudoEducativoRepository conteudoRepository;
    @Autowired private EducacaoPermanenteService educacaoPermanenteService;
    @Autowired private EducacaoEmSaudeService educacaoEmSaudeService;
    @Autowired private ControleDeAcesso controleDeAcesso;

    // ── Campanhas ──────────────────────────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<CampanhaComunicacaoDto> listar() {
        Optional<Set<UUID>> visiveis = controleDeAcesso.unidadesVisiveis(LER);
        Map<UUID, ConteudoEducativo> vigentes = educacaoPermanenteService.vigentes();
        return campanhaRepository.findAllByOrderByInicioDesc().stream()
                .filter(c -> c.getUnidade() == null || visiveis.isEmpty() || visiveis.get().contains(c.getUnidade().getUuid()))
                .map(c -> dto(c, vigentes)).toList();
    }

    @Transactional
    public CampanhaComunicacaoDto criar(CampanhaComunicacaoRequestDto dto) {
        CampanhaComunicacao c = new CampanhaComunicacao();
        c.setSituacao(SituacaoCampanhaComunicacao.PLANEJADA);
        c.setRegistradaEm(LocalDateTime.now());
        c.setRegistradaPorCpf(UsuarioAutenticado.cpf());
        preencher(c, dto);
        campanhaRepository.save(c);
        ContextoAuditoria.registro(c.getUuid());
        ContextoAuditoria.detalhe("Campanha de comunicação " + c.getNome() + ".");
        return dto(c, educacaoPermanenteService.vigentes());
    }

    /** Só a planejada se edita (422). */
    @Transactional
    public CampanhaComunicacaoDto atualizar(UUID uuid, CampanhaComunicacaoRequestDto dto) {
        CampanhaComunicacao c = campanha(uuid);
        exigirPublicar(c.getUnidade());
        if (c.getSituacao() != SituacaoCampanhaComunicacao.PLANEJADA) {
            throw new ResourceUnprocessableEntityException("Só a campanha planejada se edita.");
        }
        preencher(c, dto);
        ContextoAuditoria.registro(c.getUuid());
        return dto(campanhaRepository.save(c), educacaoPermanenteService.vigentes());
    }

    /** Planejada → em andamento: ao menos um canal e o período ainda aberto (422). */
    @Transactional
    public CampanhaComunicacaoDto iniciar(UUID uuid) {
        CampanhaComunicacao c = campanha(uuid);
        exigirPublicar(c.getUnidade());
        if (c.getSituacao() != SituacaoCampanhaComunicacao.PLANEJADA) {
            throw new ResourceUnprocessableEntityException("Só a campanha planejada se inicia.");
        }
        if (c.getCanais().isEmpty()) {
            throw new ResourceUnprocessableEntityException("Escolha ao menos um canal antes de iniciar.");
        }
        if (c.getFim().isBefore(LocalDate.now())) {
            throw new ResourceUnprocessableEntityException("O período da campanha já terminou.");
        }
        c.setSituacao(SituacaoCampanhaComunicacao.EM_ANDAMENTO);
        c.setIniciadaEm(LocalDateTime.now());
        ContextoAuditoria.registro(c.getUuid());
        ContextoAuditoria.detalhe("Campanha iniciada.");
        return dto(campanhaRepository.save(c), educacaoPermanenteService.vigentes());
    }

    /** Em andamento → encerrada, com o balanço. */
    @Transactional
    public CampanhaComunicacaoDto encerrar(UUID uuid, EncerramentoCampanhaRequestDto dto) {
        CampanhaComunicacao c = campanha(uuid);
        exigirPublicar(c.getUnidade());
        if (c.getSituacao() != SituacaoCampanhaComunicacao.EM_ANDAMENTO) {
            throw new ResourceUnprocessableEntityException("Só a campanha em andamento se encerra.");
        }
        c.setSituacao(SituacaoCampanhaComunicacao.ENCERRADA);
        c.setEncerradaEm(LocalDateTime.now());
        c.setBalanco(dto.getBalanco().trim());
        ContextoAuditoria.registro(c.getUuid());
        ContextoAuditoria.detalhe("Campanha encerrada.");
        return dto(campanhaRepository.save(c), educacaoPermanenteService.vigentes());
    }

    /** Planejada ou em andamento → cancelada, com o motivo. */
    @Transactional
    public CampanhaComunicacaoDto cancelar(UUID uuid, MotivoRequestDto dto) {
        CampanhaComunicacao c = campanha(uuid);
        exigirPublicar(c.getUnidade());
        if (c.getSituacao() == SituacaoCampanhaComunicacao.ENCERRADA || c.getSituacao() == SituacaoCampanhaComunicacao.CANCELADA) {
            throw new ResourceUnprocessableEntityException("Esta campanha já terminou.");
        }
        c.setSituacao(SituacaoCampanhaComunicacao.CANCELADA);
        c.setEncerradaEm(LocalDateTime.now());
        c.setMotivoCancelamento(dto.getMotivo().trim());
        ContextoAuditoria.registro(c.getUuid());
        ContextoAuditoria.detalhe("Campanha cancelada.");
        return dto(campanhaRepository.save(c), educacaoPermanenteService.vigentes());
    }

    // ── Material educativo ─────────────────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<MaterialEducativoDto> materiais(UUID grupoId) {
        return materialRepository.findByConteudoGrupoIdOrderByTituloAsc(grupoId).stream().map(CampanhaComunicacaoService::dto).toList();
    }

    /** Conteúdo que não existe: 404; link que não é https: 400. */
    @Transactional
    public MaterialEducativoDto criarMaterial(MaterialEducativoRequestDto dto) {
        if (conteudoRepository.findByGrupoId(dto.getConteudoGrupoId()).isEmpty()) {
            throw new ResourceNotFoundException("Não foi possível encontrar o conteúdo " + dto.getConteudoGrupoId() + " em nossos registros.");
        }
        String link = texto(dto.getLink());
        if (link != null && !link.toLowerCase().startsWith("https://")) {
            throw new ResourceBadRequestException("O link precisa começar com https://.");
        }
        MaterialEducativo m = new MaterialEducativo(null, dto.getConteudoGrupoId(), dto.getFormato(), dto.getTitulo().trim(), texto(dto.getDescricao()), link,
                LocalDateTime.now(), UsuarioAutenticado.cpf(), null, null);
        materialRepository.save(m);
        ContextoAuditoria.registro(m.getUuid());
        return dto(m);
    }

    @Transactional
    public MaterialEducativoDto retirarMaterial(UUID uuid, MotivoRequestDto dto) {
        MaterialEducativo m = materialRepository.findById(uuid).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar o material " + uuid + " em nossos registros."));
        if (m.getRetiradoEm() != null) {
            throw new ResourceUnprocessableEntityException("Este material já foi retirado.");
        }
        m.setRetiradoEm(LocalDateTime.now());
        m.setMotivoRetirada(dto.getMotivo().trim());
        ContextoAuditoria.registro(m.getUuid());
        return dto(materialRepository.save(m));
    }

    // ── Portal ─────────────────────────────────────────────────────────────────────────────────────

    /** As campanhas em andamento, no período, com o Portal entre os canais e público que não é de profissionais. */
    @Transactional(readOnly = true)
    public List<CampanhaComunicacaoPublicaDto> publicas() {
        LocalDate hoje = LocalDate.now();
        Map<UUID, ConteudoEducativo> vigentes = educacaoPermanenteService.vigentes();
        List<CampanhaComunicacaoPublicaDto> lista = new ArrayList<>();
        for (CampanhaComunicacao c : campanhaRepository.findBySituacaoOrderByInicioAsc(SituacaoCampanhaComunicacao.EM_ANDAMENTO)) {
            boolean portal = c.getCanais().stream().anyMatch(k -> k.isAtivo() && k.getTipo() == TipoCanalComunicacao.PORTAL);
            if (!portal || PROFISSIONAIS.contains(c.getPublicoAlvo()) || c.getInicio().isAfter(hoje) || c.getFim().isBefore(hoje)) {
                continue;
            }
            List<ConteudoEducativo> publicados = c.getConteudos().stream().map(vigentes::get).filter(Objects::nonNull)
                    .filter(x -> x.getSituacao() == SituacaoConteudo.PUBLICADO && !x.getValidade().isBefore(hoje))
                    .sorted(Comparator.comparing(ConteudoEducativo::getTitulo)).toList();
            Map<UUID, List<MaterialEducativo>> materiais = new HashMap<>();
            if (!publicados.isEmpty()) {
                for (MaterialEducativo m : materialRepository.findByConteudoGrupoIdInAndRetiradoEmIsNullOrderByTituloAsc(publicados.stream().map(ConteudoEducativo::getGrupoId).toList())) {
                    materiais.computeIfAbsent(m.getConteudoGrupoId(), k -> new ArrayList<>()).add(m);
                }
            }
            lista.add(new CampanhaComunicacaoPublicaDto(c.getNome(), c.getObjetivo(), c.getInicio(), c.getFim(), c.getTemas().stream().sorted().toList(),
                    publicados.stream().map(x -> new CampanhaComunicacaoPublicaDto.Conteudo(x.getTitulo(), x.getResumo(),
                            materiais.getOrDefault(x.getGrupoId(), List.of()).stream()
                                    .map(m -> new CampanhaComunicacaoPublicaDto.Material(m.getFormato(), m.getTitulo(), m.getLink())).toList())).toList()));
        }
        return lista;
    }

    // ── Apoio ──────────────────────────────────────────────────────────────────────────────────────

    /**
     * Fim antes do início (400); unidade do acesso (403); território inativo, canal inativo, portal para profissionais, conteúdo sem revisão
     * ou programa que não combina com a campanha operacional (422); o que não existe (404).
     */
    private void preencher(CampanhaComunicacao c, CampanhaComunicacaoRequestDto dto) {
        if (dto.getFim().isBefore(dto.getInicio())) {
            throw new ResourceBadRequestException("O fim vem depois do início.");
        }
        UnidadeDeSaude unidade = null;
        if (dto.getUnidadeId() != null) {
            unidade = unidadeRepository.findById(dto.getUnidadeId()).orElseThrow(() -> new ResourceNotFoundException(
                    "Não foi possível encontrar uma unidade de saúde com o id " + dto.getUnidadeId() + " em nossos registros."));
        }
        exigirPublicar(unidade);
        Set<Territorio> territorios = new HashSet<>();
        for (UUID id : lista(dto.getTerritorioIds())) {
            Territorio t = territorioRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(
                    "Não foi possível encontrar o território " + id + " em nossos registros."));
            if (!t.isAtivo()) {
                throw new ResourceUnprocessableEntityException("O território " + t.getNome() + " está inativo.");
            }
            territorios.add(t);
        }
        Set<CanalComunicacao> canais = new HashSet<>();
        for (UUID id : lista(dto.getCanalIds())) {
            CanalComunicacao k = canalRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(
                    "Não foi possível encontrar o canal " + id + " em nossos registros."));
            if (!k.isAtivo()) {
                throw new ResourceUnprocessableEntityException("O canal " + k.getNome() + " está inativo.");
            }
            if (PROFISSIONAIS.contains(dto.getPublicoAlvo()) && k.getTipo() == TipoCanalComunicacao.PORTAL) {
                throw new ResourceUnprocessableEntityException("O portal é público; para os profissionais, use outro canal.");
            }
            canais.add(k);
        }
        Map<UUID, ConteudoEducativo> vigentes = educacaoPermanenteService.vigentes();
        Set<UUID> conteudos = new LinkedHashSet<>(lista(dto.getConteudoGrupoIds()));
        for (UUID grupo : conteudos) {
            if (!vigentes.containsKey(grupo)) {
                if (conteudoRepository.findByGrupoId(grupo).isEmpty()) {
                    throw new ResourceNotFoundException("Não foi possível encontrar o conteúdo " + grupo + " em nossos registros.");
                }
                throw new ResourceUnprocessableEntityException("O conteúdo ainda não passou pela revisão técnica.");
            }
        }
        Campanha operacional = null;
        if (dto.getCampanhaOperacionalId() != null) {
            operacional = campanhaOperacionalRepository.findById(dto.getCampanhaOperacionalId()).orElseThrow(() -> new ResourceNotFoundException(
                    "Não foi possível encontrar a campanha " + dto.getCampanhaOperacionalId() + " em nossos registros."));
        }
        Programa programa = null;
        if (dto.getProgramaId() != null) {
            programa = programaRepository.findById(dto.getProgramaId()).orElseThrow(() -> new ResourceNotFoundException(
                    "Não foi possível encontrar o programa " + dto.getProgramaId() + " em nossos registros."));
        }
        if (operacional != null && operacional.getPrograma() != null) {
            if (programa != null && !programa.getUuid().equals(operacional.getPrograma().getUuid())) {
                throw new ResourceUnprocessableEntityException("A campanha operacional é de outro programa.");
            }
            programa = operacional.getPrograma();
        }
        List<String> temas = lista(dto.getTemas()).stream().map(String::trim).filter(x -> !x.isEmpty()).distinct().toList();
        c.setNome(dto.getNome().trim());
        c.setObjetivo(dto.getObjetivo().trim());
        c.setDescricao(texto(dto.getDescricao()));
        c.setInicio(dto.getInicio());
        c.setFim(dto.getFim());
        c.setPublicoAlvo(dto.getPublicoAlvo());
        c.setFaixaEtaria(texto(dto.getFaixaEtaria()));
        c.setUnidade(unidade);
        c.getTerritorios().clear();
        c.getTerritorios().addAll(territorios);
        c.getCanais().clear();
        c.getCanais().addAll(canais);
        c.getConteudos().clear();
        c.getConteudos().addAll(conteudos);
        c.getTemas().clear();
        c.getTemas().addAll(temas);
        c.setPrograma(programa);
        c.setCampanhaOperacional(operacional);
        c.setMetaPessoas(dto.getMetaPessoas());
        c.setMetaAcoes(dto.getMetaAcoes());
    }

    private CampanhaComunicacaoDto dto(CampanhaComunicacao c, Map<UUID, ConteudoEducativo> vigentes) {
        Map<String, CampanhaComunicacaoDto.Alcance> alcance = new TreeMap<>();
        List<Comunicado> comunicados = comunicadoRepository.findByCampanha_UuidOrderByInicioAsc(c.getUuid());
        for (Comunicado k : comunicados) {
            for (Publicacao p : publicacaoRepository.findDoComunicado(k.getUuid())) {
                var a = alcance.computeIfAbsent(p.getCanal().getNome(), n -> new CampanhaComunicacaoDto.Alcance(n, 0, 0, 0));
                a.setPublicacoes(a.getPublicacoes() + 1);
                a.setEnviados(a.getEnviados() + p.getEnviados());
                a.setFalhas(a.getFalhas() + p.getFalhas());
            }
        }
        return new CampanhaComunicacaoDto(educacaoEmSaudeService.daCampanha(c.getUuid()), c.getUuid(), c.getNome(), c.getObjetivo(), c.getDescricao(), c.getInicio(), c.getFim(), c.getSituacao(), c.getPublicoAlvo(),
                c.getFaixaEtaria(), c.getUnidade() != null ? c.getUnidade().getUuid() : null, c.getUnidade() != null ? c.getUnidade().getNome() : null,
                c.getTerritorios().stream().map(t -> new CampanhaComunicacaoDto.Item(t.getUuid(), t.getNome())).sorted(Comparator.comparing(CampanhaComunicacaoDto.Item::getNome)).toList(),
                c.getTemas().stream().sorted().toList(), new ArrayList<>(c.getCanais().stream().map(CanalComunicacao::getUuid).toList()),
                c.getConteudos().stream().map(vigentes::get).filter(Objects::nonNull).map(EducacaoPermanenteService::dto)
                        .sorted(Comparator.comparing(EducacaoPermanenteDto.Conteudo::getTitulo)).toList(),
                c.getPrograma() != null ? c.getPrograma().getUuid() : null, c.getPrograma() != null ? c.getPrograma().getNome() : null,
                c.getCampanhaOperacional() != null ? c.getCampanhaOperacional().getUuid() : null, c.getCampanhaOperacional() != null ? c.getCampanhaOperacional().getNome() : null,
                comunicados.stream().map(k -> new CampanhaComunicacaoDto.ComunicadoDaCampanha(k.getUuid(), k.getTitulo(), k.getSituacao(), k.getInicio(), k.getFim())).toList(),
                new ArrayList<>(alcance.values()), c.getIniciadaEm(), c.getEncerradaEm(), c.getBalanco(), c.getMotivoCancelamento(),
                c.getMetaPessoas(), c.getMetaAcoes());
    }

    private static MaterialEducativoDto dto(MaterialEducativo m) {
        return new MaterialEducativoDto(m.getUuid(), m.getConteudoGrupoId(), m.getFormato(), m.getTitulo(), m.getDescricao(), m.getLink(), m.getRegistradoEm(),
                m.getRetiradoEm(), m.getMotivoRetirada());
    }

    private void exigirPublicar(UnidadeDeSaude unidade) {
        if (unidade != null) {
            controleDeAcesso.exigir(PUBLICAR, unidade);
        } else {
            controleDeAcesso.exigirAlguma(PUBLICAR);
        }
    }

    private CampanhaComunicacao campanha(UUID uuid) {
        return campanhaRepository.findById(uuid).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar a campanha " + uuid + " em nossos registros."));
    }

    private static <T> List<T> lista(List<T> l) {
        return l == null ? List.of() : l;
    }

    private static String texto(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }
}
