package com.edufelizardo.maissaudepublica.services.version1;

import com.edufelizardo.maissaudepublica.config.ContextoAuditoria;
import com.edufelizardo.maissaudepublica.config.UsuarioAutenticado;
import com.edufelizardo.maissaudepublica.exceptions.ResourceBadRequestException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceConflictException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceNotFoundException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceUnprocessableEntityException;
import com.edufelizardo.maissaudepublica.models.AreaGestao;
import com.edufelizardo.maissaudepublica.models.Profissional;
import com.edufelizardo.maissaudepublica.models.TitularidadeAreaGestao;
import com.edufelizardo.maissaudepublica.models.UnidadeDeSaude;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.AreaGestaoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.FimTitularidadeRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.TitularidadeRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.AreaGestaoDto;
import com.edufelizardo.maissaudepublica.models.enuns.PapelTitularidade;
import com.edufelizardo.maissaudepublica.models.enuns.TipoAreaGestao;
import com.edufelizardo.maissaudepublica.models.enuns.TipoUnidadeDeSaude;
import com.edufelizardo.maissaudepublica.repositories.AreaGestaoRepository;
import com.edufelizardo.maissaudepublica.repositories.ProfissionalRepository;
import com.edufelizardo.maissaudepublica.repositories.TitularidadeAreaGestaoRepository;
import com.edufelizardo.maissaudepublica.repositories.UnidadeDeSaudeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Coordenações e gerência, fatia BI5 (ADR-0175): a estrutura de gestão da secretaria e quem responde por cada área, como histórico. Os
 * indicadores da coordenação regional vêm dos painéis da BI1 a BI4, que somam as unidades abaixo da regional.
 */
@Service
public class EstruturaDeGestaoService {

    static final String GERENCIAR = "ADMINISTRATIVO.GERENCIAR";
    private static final Set<TipoUnidadeDeSaude> ORGANIZADORES = EnumSet.of(TipoUnidadeDeSaude.FEDERAL, TipoUnidadeDeSaude.ESTADUAL,
            TipoUnidadeDeSaude.MUNICIPAL, TipoUnidadeDeSaude.REGIONAL);

    @Autowired private AreaGestaoRepository areaRepository;
    @Autowired private TitularidadeAreaGestaoRepository titularidadeRepository;
    @Autowired private UnidadeDeSaudeRepository unidadeRepository;
    @Autowired private ProfissionalRepository profissionalRepository;
    @Autowired private AutorizacaoService autorizacaoService;
    @Autowired private ControleDeAcesso controleDeAcesso;

    @Transactional(readOnly = true)
    public List<AreaGestaoDto> listar() {
        Map<UUID, List<TitularidadeAreaGestao>> porArea = new HashMap<>();
        for (TitularidadeAreaGestao t : titularidadeRepository.findTodas()) {
            porArea.computeIfAbsent(t.getArea().getUuid(), k -> new ArrayList<>()).add(t);
        }
        List<UnidadeDeSaude> todas = unidadeRepository.findAll();
        return areaRepository.findAllByOrderByNomeAsc().stream().map(a -> dto(a, porArea.getOrDefault(a.getUuid(), List.of()), todas, false)).toList();
    }

    @Transactional(readOnly = true)
    public AreaGestaoDto detalhe(UUID uuid) {
        AreaGestao a = area(uuid);
        return dto(a, titularidadeRepository.findDaArea(uuid), unidadeRepository.findAll(), true);
    }

    @Transactional
    public AreaGestaoDto criar(AreaGestaoRequestDto dto) {
        AreaGestao a = new AreaGestao();
        a.setRegistradaEm(LocalDateTime.now());
        preencher(a, dto);
        areaRepository.save(a);
        ContextoAuditoria.registro(a.getUuid());
        ContextoAuditoria.detalhe("Área de gestão " + a.getNome() + ".");
        return detalhe(a.getUuid());
    }

    @Transactional
    public AreaGestaoDto atualizar(UUID uuid, AreaGestaoRequestDto dto) {
        AreaGestao a = area(uuid);
        exigir(a);
        if (dto.getTipo() != a.getTipo()) {
            throw new ResourceUnprocessableEntityException("O tipo da área não muda; crie outra.");
        }
        boolean desativar = a.isAtiva() && Boolean.FALSE.equals(dto.getAtiva());
        if (desativar && !areaRepository.findBySuperior_UuidAndAtivaTrue(uuid).isEmpty()) {
            throw new ResourceUnprocessableEntityException("A área tem áreas ativas abaixo dela.");
        }
        preencher(a, dto);
        areaRepository.save(a);
        ContextoAuditoria.registro(a.getUuid());
        return detalhe(uuid);
    }

    /**
     * Designa quem responde: profissional ativo (422), não titular e substituto ao mesmo tempo (422). Havendo outro no mesmo papel, ele termina
     * no dia anterior; o novo não pode começar antes dele (422).
     */
    @Transactional
    public AreaGestaoDto designar(UUID areaId, TitularidadeRequestDto dto) {
        AreaGestao a = area(areaId);
        exigir(a);
        if (!a.isAtiva()) {
            throw new ResourceUnprocessableEntityException("A área está inativa.");
        }
        Profissional p = profissionalRepository.findByMatricula(dto.getMatriculaProfissional().trim()).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar o profissional de matrícula " + dto.getMatriculaProfissional() + " em nossos registros."));
        if (!p.isAtivo()) {
            throw new ResourceUnprocessableEntityException("O profissional está inativo.");
        }
        List<TitularidadeAreaGestao> historico = titularidadeRepository.findDaArea(areaId);
        for (TitularidadeAreaGestao t : historico) {
            if (t.getFim() == null && t.getPapel() != dto.getPapel() && t.getProfissional().getUuid().equals(p.getUuid())) {
                throw new ResourceUnprocessableEntityException("O mesmo profissional não é titular e substituto ao mesmo tempo.");
            }
        }
        for (TitularidadeAreaGestao t : historico) {
            if (t.getFim() == null && t.getPapel() == dto.getPapel()) {
                if (!dto.getInicio().isAfter(t.getInicio())) {
                    throw new ResourceUnprocessableEntityException("O início precisa ser depois do início de quem responde hoje (" + t.getInicio() + ").");
                }
                t.setFim(dto.getInicio().minusDays(1));
                titularidadeRepository.save(t);
            }
        }
        TitularidadeAreaGestao nova = new TitularidadeAreaGestao(null, a, p, dto.getPapel(), dto.getInicio(), null, texto(dto.getAto()), LocalDateTime.now(),
                UsuarioAutenticado.cpf());
        titularidadeRepository.save(nova);
        ContextoAuditoria.registro(a.getUuid());
        ContextoAuditoria.detalhe((dto.getPapel() == PapelTitularidade.TITULAR ? "Titular" : "Substituto") + " designado.");
        return detalhe(areaId);
    }

    /** Encerra a designação sem substituto: fim não antes do início (400); já encerrada (422). */
    @Transactional
    public AreaGestaoDto encerrar(UUID titularidadeId, FimTitularidadeRequestDto dto) {
        TitularidadeAreaGestao t = titularidadeRepository.findById(titularidadeId).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar a designação " + titularidadeId + " em nossos registros."));
        exigir(t.getArea());
        if (t.getFim() != null) {
            throw new ResourceUnprocessableEntityException("Esta designação já terminou.");
        }
        if (dto.getFim().isBefore(t.getInicio())) {
            throw new ResourceBadRequestException("O fim não pode ser antes do início.");
        }
        t.setFim(dto.getFim());
        titularidadeRepository.save(t);
        ContextoAuditoria.registro(t.getArea().getUuid());
        return detalhe(t.getArea().getUuid());
    }

    /** As regionais ativas, para a coordenação regional. */
    @Transactional(readOnly = true)
    public List<AreaGestaoDto.Unidade> regionais() {
        return unidadeRepository.findAll().stream().filter(u -> u.getTipo() == TipoUnidadeDeSaude.REGIONAL && u.isAtivo())
                .sorted(Comparator.comparing(UnidadeDeSaude::getNome))
                .map(u -> new AreaGestaoDto.Unidade(u.getUuid(), u.getNome(), u.getTipo(), u.getSituacaoOperacional(), u.isAtivo())).toList();
    }

    // ── Apoio ──────────────────────────────────────────────────────────────────────────────────────

    /**
     * Gerência sem superior; coordenação sob gerência; supervisão sob coordenação ou gerência (422). Só a coordenação tem regional, do tipo
     * REGIONAL (422), uma ativa por regional (409). Sem ciclo na hierarquia (422).
     */
    private void preencher(AreaGestao a, AreaGestaoRequestDto dto) {
        AreaGestao superior = null;
        if (dto.getSuperiorId() != null) {
            superior = area(dto.getSuperiorId());
            for (AreaGestao s = superior; s != null; s = s.getSuperior()) {
                if (a.getUuid() != null && s.getUuid().equals(a.getUuid())) {
                    throw new ResourceUnprocessableEntityException("A área não pode ficar abaixo dela mesma.");
                }
            }
        }
        switch (dto.getTipo()) {
            case GERENCIA -> {
                if (superior != null) {
                    throw new ResourceUnprocessableEntityException("A gerência responde direto à secretaria, sem área superior.");
                }
            }
            case COORDENACAO -> {
                if (superior != null && superior.getTipo() != TipoAreaGestao.GERENCIA) {
                    throw new ResourceUnprocessableEntityException("A coordenação fica sob uma gerência.");
                }
            }
            case SUPERVISAO -> {
                if (superior == null || superior.getTipo() == TipoAreaGestao.SUPERVISAO) {
                    throw new ResourceUnprocessableEntityException("A supervisão fica sob uma coordenação ou uma gerência.");
                }
            }
        }
        UnidadeDeSaude regional = null;
        if (dto.getRegionalId() != null) {
            if (dto.getTipo() != TipoAreaGestao.COORDENACAO) {
                throw new ResourceUnprocessableEntityException("Só a coordenação aponta para uma regional.");
            }
            regional = unidadeRepository.findById(dto.getRegionalId()).orElseThrow(() -> new ResourceNotFoundException(
                    "Não foi possível encontrar a unidade " + dto.getRegionalId() + " em nossos registros."));
            if (regional.getTipo() != TipoUnidadeDeSaude.REGIONAL) {
                throw new ResourceUnprocessableEntityException(regional.getNome() + " não é uma regional.");
            }
            boolean ativa = dto.getAtiva() == null || dto.getAtiva();
            UUID regionalId = regional.getUuid();
            if (ativa && areaRepository.findByRegional_UuidAndAtivaTrue(regionalId).stream().anyMatch(o -> !o.getUuid().equals(a.getUuid()))) {
                throw new ResourceConflictException("A regional " + regional.getNome() + " já tem uma coordenação ativa.");
            }
        }
        if (regional != null) {
            controleDeAcesso.exigir(GERENCIAR, regional);
        } else {
            controleDeAcesso.exigirAlguma(GERENCIAR);
        }
        a.setTipo(dto.getTipo());
        a.setTema(dto.getTema());
        a.setNome(dto.getNome().trim());
        a.setSigla(texto(dto.getSigla()));
        a.setDescricao(texto(dto.getDescricao()));
        a.setSuperior(superior);
        a.setRegional(regional);
        a.setAtiva(dto.getAtiva() == null || dto.getAtiva());
    }

    /** A regional da área: a própria, ou a da coordenação acima. */
    private static UnidadeDeSaude regionalDa(AreaGestao a) {
        for (AreaGestao s = a; s != null; s = s.getSuperior()) {
            if (s.getRegional() != null) {
                return s.getRegional();
            }
        }
        return null;
    }

    private void exigir(AreaGestao a) {
        UnidadeDeSaude regional = regionalDa(a);
        if (regional != null) {
            controleDeAcesso.exigir(GERENCIAR, regional);
        } else {
            controleDeAcesso.exigirAlguma(GERENCIAR);
        }
    }

    private AreaGestaoDto dto(AreaGestao a, List<TitularidadeAreaGestao> historico, List<UnidadeDeSaude> todas, boolean detalhe) {
        LocalDate hoje = LocalDate.now();
        UnidadeDeSaude regional = regionalDa(a);
        List<UnidadeDeSaude> unidades = List.of();
        if (regional != null) {
            Set<UUID> abaixo = autorizacaoService.abaixoDe(Set.of(regional.getUuid()));
            unidades = todas.stream().filter(u -> abaixo.contains(u.getUuid()) && !ORGANIZADORES.contains(u.getTipo()))
                    .sorted(Comparator.comparing(UnidadeDeSaude::getNome)).toList();
        }
        AreaGestaoDto.Titularidade titular = historico.stream().filter(t -> t.getPapel() == PapelTitularidade.TITULAR && t.vigenteEm(hoje)).findFirst().map(EstruturaDeGestaoService::dto).orElse(null);
        AreaGestaoDto.Titularidade substituto = historico.stream().filter(t -> t.getPapel() == PapelTitularidade.SUBSTITUTO && t.vigenteEm(hoje)).findFirst().map(EstruturaDeGestaoService::dto).orElse(null);
        return new AreaGestaoDto(a.getUuid(), a.getTipo(), a.getTema(), a.getNome(), a.getSigla(), a.getDescricao(),
                a.getSuperior() != null ? a.getSuperior().getUuid() : null, a.getSuperior() != null ? a.getSuperior().getNome() : null,
                regional != null ? regional.getUuid() : null, regional != null ? regional.getNome() : null, a.isAtiva(), titular, substituto, unidades.size(),
                detalhe ? historico.stream().sorted(Comparator.comparing(TitularidadeAreaGestao::getInicio).reversed()).map(EstruturaDeGestaoService::dto).toList() : null,
                detalhe ? unidades.stream().map(u -> new AreaGestaoDto.Unidade(u.getUuid(), u.getNome(), u.getTipo(), u.getSituacaoOperacional(), u.isAtivo())).toList() : null);
    }

    private static AreaGestaoDto.Titularidade dto(TitularidadeAreaGestao t) {
        return new AreaGestaoDto.Titularidade(t.getUuid(), t.getProfissional().getUuid(), t.getProfissional().getNome(), t.getProfissional().getMatricula(),
                t.getPapel(), t.getInicio(), t.getFim(), t.getAto());
    }

    private AreaGestao area(UUID uuid) {
        return areaRepository.findById(uuid).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar a área de gestão " + uuid + " em nossos registros."));
    }

    private static String texto(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }
}
