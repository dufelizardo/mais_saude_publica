package com.edufelizardo.maissaudepublica.services.version1;

import com.edufelizardo.maissaudepublica.config.ContextoAuditoria;
import com.edufelizardo.maissaudepublica.config.UsuarioAutenticado;
import com.edufelizardo.maissaudepublica.exceptions.ResourceBadRequestException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceNotFoundException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceUnprocessableEntityException;
import com.edufelizardo.maissaudepublica.models.AcaoEducativa;
import com.edufelizardo.maissaudepublica.models.AcaoEducativaMaterial;
import com.edufelizardo.maissaudepublica.models.AcaoProgramatica;
import com.edufelizardo.maissaudepublica.models.CampanhaComunicacao;
import com.edufelizardo.maissaudepublica.models.ConteudoEducativo;
import com.edufelizardo.maissaudepublica.models.MaterialEducativo;
import com.edufelizardo.maissaudepublica.models.Microarea;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.AcaoEducativaRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.AcaoEducativaDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.EducacaoPermanenteDto;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoAcaoProgramatica;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoCampanhaComunicacao;
import com.edufelizardo.maissaudepublica.models.enuns.TipoAcaoProgramatica;
import com.edufelizardo.maissaudepublica.repositories.AcaoEducativaRepository;
import com.edufelizardo.maissaudepublica.repositories.AcaoProgramaticaRepository;
import com.edufelizardo.maissaudepublica.repositories.CampanhaComunicacaoRepository;
import com.edufelizardo.maissaudepublica.repositories.MaterialEducativoRepository;
import com.edufelizardo.maissaudepublica.repositories.MicroareaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * Educação em saúde, fatia C3 (ADR-0195): o detalhe educativo da ação coletiva de Programas (P4). Este domínio fornece o tema, os conteúdos
 * revisados, os materiais distribuídos, as microáreas alvo, a campanha de comunicação e a avaliação da equipe; a ação, o local, a equipe e
 * os participantes continuam em Programas. Quem registra é quem acompanha o programa (PROGRAMA.ACOMPANHAR).
 */
@Service
public class EducacaoEmSaudeService {

    /** Os tipos de ação coletiva que podem ser educativos. */
    static final Set<TipoAcaoProgramatica> EDUCATIVOS = EnumSet.of(TipoAcaoProgramatica.GRUPO, TipoAcaoProgramatica.ORIENTACAO,
            TipoAcaoProgramatica.MOBILIZACAO, TipoAcaoProgramatica.OUTRA);
    private static final String ACOMPANHAR = AcompanhamentoService.ACOMPANHAR;

    @Autowired private AcaoProgramaticaRepository acaoRepository;
    @Autowired private AcaoEducativaRepository educativaRepository;
    @Autowired private MaterialEducativoRepository materialRepository;
    @Autowired private MicroareaRepository microareaRepository;
    @Autowired private CampanhaComunicacaoRepository campanhaRepository;
    @Autowired private EducacaoPermanenteService educacaoPermanenteService;
    @Autowired private ControleDeAcesso controleDeAcesso;

    @Transactional(readOnly = true)
    public AcaoEducativaDto detalhe(UUID acaoId) {
        AcaoProgramatica a = acao(acaoId);
        controleDeAcesso.exigirVisivel(a.getUnidade(), ACOMPANHAR);
        return dto(a, educativaRepository.findByAcao_Uuid(acaoId).orElse(null));
    }

    /**
     * Preenche o detalhe educativo: ação coletiva (individual, 422) de grupo, orientação, mobilização ou outra (422), prevista ou realizada
     * (não realizada e cancelada, 422). Só conteúdo revisado (422); material retirado (422) ou repetido (400); campanha planejada ou em
     * andamento (422). Substitui as ligações.
     */
    @Transactional
    public AcaoEducativaDto salvar(UUID acaoId, AcaoEducativaRequestDto dto) {
        AcaoProgramatica a = acao(acaoId);
        controleDeAcesso.exigir(ACOMPANHAR, a.getUnidade());
        exigirEducativa(a);
        if (a.getSituacao() == SituacaoAcaoProgramatica.NAO_REALIZADA || a.getSituacao() == SituacaoAcaoProgramatica.CANCELADA) {
            throw new ResourceUnprocessableEntityException("A ação está " + a.getSituacao().name().toLowerCase().replace('_', ' ') + ".");
        }
        Map<UUID, ConteudoEducativo> vigentes = educacaoPermanenteService.vigentes();
        Set<UUID> conteudos = new LinkedHashSet<>(dto.getConteudoGrupoIds() == null ? List.of() : dto.getConteudoGrupoIds());
        for (UUID g : conteudos) {
            if (!vigentes.containsKey(g)) {
                throw new ResourceUnprocessableEntityException("Só entra conteúdo que passou pela revisão técnica (aprovado ou publicado).");
            }
        }
        AcaoEducativa e = educativaRepository.findByAcao_Uuid(acaoId).orElseGet(AcaoEducativa::new);
        e.setAcao(a);
        e.setTema(dto.getTema().trim());
        e.setConteudos(new HashSet<>(conteudos));
        Set<Microarea> microareas = new HashSet<>();
        for (UUID id : dto.getMicroareaIds() == null ? List.<UUID>of() : dto.getMicroareaIds()) {
            microareas.add(microareaRepository.findById(id).orElseThrow(() -> naoEncontrado("a microárea", id)));
        }
        e.getMicroareas().clear();
        e.getMicroareas().addAll(microareas);
        CampanhaComunicacao campanha = null;
        if (dto.getCampanhaComunicacaoId() != null) {
            campanha = campanhaRepository.findById(dto.getCampanhaComunicacaoId()).orElseThrow(() -> naoEncontrado("a campanha", dto.getCampanhaComunicacaoId()));
            if (campanha.getSituacao() != SituacaoCampanhaComunicacao.PLANEJADA && campanha.getSituacao() != SituacaoCampanhaComunicacao.EM_ANDAMENTO) {
                throw new ResourceUnprocessableEntityException("A campanha " + campanha.getNome() + " está " + campanha.getSituacao().name().toLowerCase().replace('_', ' ') + ".");
            }
        }
        e.setCampanhaComunicacao(campanha);
        Set<UUID> vistos = new HashSet<>();
        e.getMateriais().clear();
        for (AcaoEducativaRequestDto.Material m : dto.getMateriais() == null ? List.<AcaoEducativaRequestDto.Material>of() : dto.getMateriais()) {
            if (!vistos.add(m.getMaterialId())) {
                throw new ResourceBadRequestException("O mesmo material aparece duas vezes; some as quantidades.");
            }
            MaterialEducativo mat = materialRepository.findById(m.getMaterialId()).orElseThrow(() -> naoEncontrado("o material", m.getMaterialId()));
            if (mat.getRetiradoEm() != null) {
                throw new ResourceUnprocessableEntityException("O material " + mat.getTitulo() + " foi retirado do catálogo.");
            }
            e.getMateriais().add(new AcaoEducativaMaterial(null, e, mat, m.getQuantidade()));
        }
        e.setAtualizadoEm(Instant.now());
        e.setAtualizadoPorCpf(UsuarioAutenticado.cpf());
        educativaRepository.save(e);
        ContextoAuditoria.unidade(a.getUnidade());
        ContextoAuditoria.registro(a.getUuid());
        ContextoAuditoria.detalhe("Educação em saúde: " + e.getTema() + ".");
        return dto(a, e);
    }

    /** A avaliação da equipe: na ação realizada (422) já com o detalhe educativo (422). */
    @Transactional
    public AcaoEducativaDto avaliar(UUID acaoId, AcaoEducativaRequestDto.Avaliacao dto) {
        AcaoProgramatica a = acao(acaoId);
        controleDeAcesso.exigir(ACOMPANHAR, a.getUnidade());
        if (a.getSituacao() != SituacaoAcaoProgramatica.REALIZADA) {
            throw new ResourceUnprocessableEntityException("A avaliação é da ação realizada.");
        }
        AcaoEducativa e = educativaRepository.findByAcao_Uuid(acaoId).orElseThrow(() -> new ResourceUnprocessableEntityException(
                "Preencha a educação em saúde da ação (tema e conteúdos) antes de avaliar."));
        e.setParticipacao(dto.getParticipacao());
        e.setAvaliacao(dto.getAvaliacao() == null || dto.getAvaliacao().isBlank() ? null : dto.getAvaliacao().trim());
        e.setAvaliadoEm(Instant.now());
        e.setAvaliadoPorCpf(UsuarioAutenticado.cpf());
        educativaRepository.save(e);
        ContextoAuditoria.unidade(a.getUnidade());
        ContextoAuditoria.registro(a.getUuid());
        ContextoAuditoria.detalhe("Avaliação da ação educativa: participação " + dto.getParticipacao() + ".");
        return dto(a, e);
    }

    // ── Para a Comunicação ────────────────────────────────────────────────────────────────────────

    /** As ações educativas da campanha de comunicação. */
    List<AcaoEducativaDto.DaCampanha> daCampanha(UUID campanhaId) {
        return educativaRepository.findDaCampanha(campanhaId).stream().map(e -> {
            AcaoProgramatica a = e.getAcao();
            return new AcaoEducativaDto.DaCampanha(a.getUuid(), a.getDescricao(), a.getLocal(), a.getUnidade() != null ? a.getUnidade().getNome() : null,
                    a.getDataPrevista(), a.getRealizadaEm(), a.getSituacao(), e.getTema(), publico(a),
                    e.getMateriais().stream().mapToInt(AcaoEducativaMaterial::getQuantidade).sum(), e.getParticipacao());
        }).toList();
    }

    /** Por grupo de conteúdo: {ações educativas, pessoas alcançadas nas realizadas}. */
    Map<UUID, int[]> usoDosConteudos() {
        Map<UUID, int[]> uso = new HashMap<>();
        for (AcaoEducativa e : educativaRepository.findTodas()) {
            int alcance = e.getAcao().getSituacao() == SituacaoAcaoProgramatica.REALIZADA ? Objects.requireNonNullElse(publico(e.getAcao()), 0) : 0;
            for (UUID g : e.getConteudos()) {
                int[] u = uso.computeIfAbsent(g, k -> new int[2]);
                u[0]++;
                u[1] += alcance;
            }
        }
        return uso;
    }

    // ── Apoio ─────────────────────────────────────────────────────────────────────────────────────

    /** O público alcançado: a quantidade informada na realização ou, sem ela, os participantes nominais. */
    private static Integer publico(AcaoProgramatica a) {
        if (a.getQuantidadeAtendida() != null) {
            return a.getQuantidadeAtendida();
        }
        return a.getParticipantes().isEmpty() ? null : a.getParticipantes().size();
    }

    private AcaoEducativaDto dto(AcaoProgramatica a, AcaoEducativa e) {
        if (e == null) {
            return new AcaoEducativaDto(a.getUuid(), a.getSituacao(), null, null, null, List.of(), List.of(), List.of(), publico(a), null, null, null);
        }
        Map<UUID, ConteudoEducativo> vigentes = educacaoPermanenteService.vigentes();
        return new AcaoEducativaDto(a.getUuid(), a.getSituacao(), e.getTema(),
                e.getCampanhaComunicacao() != null ? e.getCampanhaComunicacao().getUuid() : null,
                e.getCampanhaComunicacao() != null ? e.getCampanhaComunicacao().getNome() : null,
                e.getConteudos().stream().map(vigentes::get).filter(Objects::nonNull).map(EducacaoPermanenteService::dto)
                        .sorted(Comparator.comparing(EducacaoPermanenteDto.Conteudo::getTitulo)).toList(),
                e.getMateriais().stream().map(m -> new AcaoEducativaDto.Material(m.getMaterial().getUuid(), m.getMaterial().getTitulo(), m.getMaterial().getFormato(),
                        m.getQuantidade(), m.getMaterial().getRetiradoEm() != null)).sorted(Comparator.comparing(AcaoEducativaDto.Material::titulo)).toList(),
                e.getMicroareas().stream().map(m -> new AcaoEducativaDto.Microarea(m.getUuid(), m.getCodigo(), m.getArea().getNome()))
                        .sorted(Comparator.comparing(AcaoEducativaDto.Microarea::codigo)).toList(),
                publico(a), e.getParticipacao(), e.getAvaliacao(), e.getAvaliadoEm());
    }

    private static void exigirEducativa(AcaoProgramatica a) {
        if (a.getPaciente() != null) {
            throw new ResourceUnprocessableEntityException("A educação em saúde é da ação coletiva, não da individual.");
        }
        if (!EDUCATIVOS.contains(a.getTipo())) {
            throw new ResourceUnprocessableEntityException("A ação educativa é de grupo, orientação, mobilização ou outra; esta é " + a.getTipo() + ".");
        }
    }

    private AcaoProgramatica acao(UUID acaoId) {
        return acaoRepository.findById(acaoId).orElseThrow(() -> naoEncontrado("a ação programática", acaoId));
    }

    private static ResourceNotFoundException naoEncontrado(String o, UUID id) {
        return new ResourceNotFoundException("Não foi possível encontrar " + o + " " + id + " em nossos registros.");
    }
}
