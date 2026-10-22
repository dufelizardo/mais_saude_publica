package com.edufelizardo.maissaudepublica.services.version1;

import com.edufelizardo.maissaudepublica.config.ContextoAuditoria;
import com.edufelizardo.maissaudepublica.config.UsuarioAutenticado;
import com.edufelizardo.maissaudepublica.exceptions.ResourceNotFoundException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceUnprocessableEntityException;
import com.edufelizardo.maissaudepublica.models.AcaoProgramatica;
import com.edufelizardo.maissaudepublica.models.AcaoSaudeBucal;
import com.edufelizardo.maissaudepublica.models.AvaliacaoBucalColetiva;
import com.edufelizardo.maissaudepublica.models.Paciente;
import com.edufelizardo.maissaudepublica.models.UnidadeDeSaude;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.AcaoSaudeBucalRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.AvaliacoesBucaisColetivasRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.AcaoSaudeBucalDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.ConvocacaoSaudeBucalDto;
import com.edufelizardo.maissaudepublica.models.enuns.NecessidadeTratamentoOdontologico;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoAcaoProgramatica;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoPlanoTratamento;
import com.edufelizardo.maissaudepublica.repositories.AcaoProgramaticaRepository;
import com.edufelizardo.maissaudepublica.repositories.AcaoSaudeBucalRepository;
import com.edufelizardo.maissaudepublica.repositories.AvaliacaoBucalColetivaRepository;
import com.edufelizardo.maissaudepublica.repositories.PlanoTratamentoRepository;
import com.edufelizardo.maissaudepublica.repositories.UnidadeDeSaudeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Saúde bucal coletiva, fatia SB5 (ADR-0140, desenho na ADR-0111): o detalhe odontológico da ação coletiva de Programas
 * (P4), a avaliação coletiva por participante e a lista de quem precisa de tratamento e ainda não tem plano aberto (a
 * convocar). Só em ação de programa odontológico: Saúde Bucal e Saúde na Escola (PSE).
 */
@Service
public class SaudeBucalColetivaService {

    /** Os programas em que a ação coletiva ganha o detalhe odontológico. */
    static final Set<String> PROGRAMAS_ODONTOLOGICOS = Set.of("SAUDE_BUCAL", "SAUDE_NA_ESCOLA");
    private static final String ACOMPANHAR = AcompanhamentoService.ACOMPANHAR;
    private static final String REGISTRAR = AvaliacaoOdontologicaService.REGISTRAR;

    @Autowired
    private AcaoProgramaticaRepository acaoRepository;

    @Autowired
    private AcaoSaudeBucalRepository detalheRepository;

    @Autowired
    private AvaliacaoBucalColetivaRepository avaliacaoRepository;

    @Autowired
    private PlanoTratamentoRepository planoRepository;

    @Autowired
    private UnidadeDeSaudeRepository unidadeRepository;

    @Autowired
    private ControleDeAcesso controleDeAcesso;

    /** O detalhe e as avaliações da ação (vazio enquanto não preenchido). */
    @Transactional(readOnly = true)
    public AcaoSaudeBucalDto detalhe(UUID acaoId) {
        AcaoProgramatica a = acao(acaoId);
        controleDeAcesso.exigirVisivel(a.getUnidade(), ACOMPANHAR, REGISTRAR);
        ContextoAuditoria.registro(a.getUuid());
        return dto(a);
    }

    /**
     * Preenche o detalhe odontológico da ação coletiva (do programa Saúde Bucal ou Saúde na Escola, 422), prevista ou
     * realizada (a não realizada e a cancelada, 422). O técnico em saúde bucal registra (PROGRAMA.ACOMPANHAR).
     */
    @Transactional
    public AcaoSaudeBucalDto salvarDetalhe(UUID acaoId, AcaoSaudeBucalRequestDto dto) {
        AcaoProgramatica a = acao(acaoId);
        controleDeAcesso.exigir(ACOMPANHAR, a.getUnidade());
        ContextoAuditoria.registro(a.getUuid());
        exigirColetivaOdontologica(a);
        if (a.getSituacao() == SituacaoAcaoProgramatica.NAO_REALIZADA || a.getSituacao() == SituacaoAcaoProgramatica.CANCELADA) {
            throw new ResourceUnprocessableEntityException("A ação está " + a.getSituacao().name().toLowerCase().replace('_', ' ') + ".");
        }
        AcaoSaudeBucal d = detalheRepository.findByAcao_Uuid(acaoId).orElseGet(AcaoSaudeBucal::new);
        d.setAcao(a);
        d.setAtividades(new LinkedHashSet<>(dto.getAtividades()));
        d.setLocalTipo(dto.getLocalTipo());
        d.setInstituicao(texto(dto.getInstituicao()));
        d.setPublicoAlvo(texto(dto.getPublicoAlvo()));
        d.setKitsDistribuidos(dto.getKitsDistribuidos());
        d.setAtualizadoEm(Instant.now());
        d.setAtualizadoPorCpf(UsuarioAutenticado.cpf());
        detalheRepository.save(d);
        ContextoAuditoria.detalhe("Saúde bucal: " + dto.getAtividades());
        return dto(a);
    }

    /**
     * A avaliação coletiva de risco é do cirurgião-dentista (ODONTOLOGIA.REGISTRAR): na ação realizada (422), com a
     * lista nominal (422) e só para os participantes dela (422). O resultado de um participante pode ser corrigido na
     * mesma ação: o novo substitui o anterior, e a trilha de auditoria guarda a mudança.
     */
    @Transactional
    public AcaoSaudeBucalDto avaliar(UUID acaoId, AvaliacoesBucaisColetivasRequestDto dto) {
        AcaoProgramatica a = acao(acaoId);
        controleDeAcesso.exigir(REGISTRAR, a.getUnidade());
        ContextoAuditoria.registro(a.getUuid());
        exigirColetivaOdontologica(a);
        if (a.getSituacao() != SituacaoAcaoProgramatica.REALIZADA) {
            throw new ResourceUnprocessableEntityException("A avaliação coletiva é registrada na ação realizada.");
        }
        if (a.getParticipantes().isEmpty()) {
            throw new ResourceUnprocessableEntityException("A ação não tem lista nominal: registre os participantes para avaliar cada um.");
        }
        Map<UUID, Paciente> participantes = new LinkedHashMap<>();
        a.getParticipantes().forEach(p -> participantes.put(p.getUuid(), p));
        Instant agora = Instant.now();
        for (AvaliacoesBucaisColetivasRequestDto.Resultado r : dto.getResultados()) {
            Paciente p = participantes.get(r.getPacienteId());
            if (p == null) {
                throw new ResourceUnprocessableEntityException("O paciente " + r.getPacienteId() + " não participou desta ação.");
            }
            AvaliacaoBucalColetiva av = avaliacaoRepository.findByAcao_UuidAndPaciente_Uuid(a.getUuid(), p.getUuid()).orElseGet(AvaliacaoBucalColetiva::new);
            av.setAcao(a);
            av.setPaciente(p);
            av.setRisco(r.getRisco());
            av.setNecessidadeTratamento(r.getNecessidadeTratamento());
            av.setObservacao(texto(r.getObservacao()));
            av.setRegistradoEm(agora);
            av.setRegistradoPorCpf(UsuarioAutenticado.cpf());
            avaliacaoRepository.save(av);
        }
        ContextoAuditoria.detalhe(dto.getResultados().size() + " avaliação(ões) coletiva(s) de saúde bucal.");
        return dto(a);
    }

    /**
     * A convocar: a avaliação coletiva mais recente de cada paciente nas ações da unidade, quando aponta necessidade de
     * tratamento e o paciente ainda não tem plano de tratamento aberto. A urgência primeiro.
     */
    @Transactional(readOnly = true)
    public List<ConvocacaoSaudeBucalDto> aConvocar(UUID unidadeId) {
        UnidadeDeSaude unidade = unidadeRepository.findById(unidadeId).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar uma unidade de saúde com o id " + unidadeId + " em nossos registros."));
        controleDeAcesso.exigirVisivel(unidade, ACOMPANHAR, REGISTRAR);
        Map<UUID, AvaliacaoBucalColetiva> maisRecente = new LinkedHashMap<>();
        for (AvaliacaoBucalColetiva av : avaliacaoRepository.findDaUnidade(unidadeId)) {
            maisRecente.putIfAbsent(av.getPaciente().getUuid(), av);
        }
        return maisRecente.values().stream()
                .filter(av -> av.getNecessidadeTratamento() != NecessidadeTratamentoOdontologico.SEM_NECESSIDADE)
                .filter(av -> !planoRepository.existsByPaciente_UuidAndSituacao(av.getPaciente().getUuid(), SituacaoPlanoTratamento.ABERTO))
                .sorted(Comparator.comparing((AvaliacaoBucalColetiva av) -> av.getNecessidadeTratamento().ordinal()).reversed()
                        .thenComparing(av -> av.getPaciente().getNome(), String.CASE_INSENSITIVE_ORDER))
                .map(av -> new ConvocacaoSaudeBucalDto(av.getPaciente().getUuid(), av.getPaciente().getNome(), av.getRisco(),
                        av.getNecessidadeTratamento(), av.getAcao().getUuid(), av.getAcao().getPrograma().getNome(), av.getAcao().getLocal(),
                        av.getAcao().getRealizadaEm(), av.getRegistradoEm()))
                .toList();
    }

    // ── Apoio ──────────────────────────────────────────────────────────────────────────────────────

    private AcaoProgramatica acao(UUID acaoId) {
        return acaoRepository.findById(acaoId).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar a ação programática " + acaoId + " em nossos registros."));
    }

    private static void exigirColetivaOdontologica(AcaoProgramatica a) {
        if (a.getPaciente() != null) {
            throw new ResourceUnprocessableEntityException("A saúde bucal coletiva é da ação coletiva, e não da ação de um paciente.");
        }
        if (a.getPrograma() == null || !PROGRAMAS_ODONTOLOGICOS.contains(a.getPrograma().getCodigo())) {
            throw new ResourceUnprocessableEntityException("O detalhe de saúde bucal é da ação dos programas Saúde Bucal e Saúde na Escola (PSE).");
        }
    }

    private AcaoSaudeBucalDto dto(AcaoProgramatica a) {
        AcaoSaudeBucal d = detalheRepository.findByAcao_Uuid(a.getUuid()).orElse(null);
        List<AcaoSaudeBucalDto.Avaliacao> avaliacoes = avaliacaoRepository.findDaAcao(a.getUuid()).stream()
                .map(av -> new AcaoSaudeBucalDto.Avaliacao(av.getPaciente().getUuid(), av.getPaciente().getNome(), av.getRisco(),
                        av.getNecessidadeTratamento(), av.getObservacao(), av.getRegistradoEm()))
                .toList();
        if (d == null) {
            return new AcaoSaudeBucalDto(a.getUuid(), false, List.of(), null, null, null, null, null, avaliacoes);
        }
        return new AcaoSaudeBucalDto(a.getUuid(), true, d.getAtividades().stream().sorted().toList(), d.getLocalTipo(), d.getInstituicao(),
                d.getPublicoAlvo(), d.getKitsDistribuidos(), d.getAtualizadoEm(), avaliacoes);
    }

    private static String texto(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }
}
