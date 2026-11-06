package com.edufelizardo.maissaudepublica.services.version1;

import com.edufelizardo.maissaudepublica.config.ContextoAuditoria;
import com.edufelizardo.maissaudepublica.config.UsuarioAutenticado;
import com.edufelizardo.maissaudepublica.exceptions.ResourceConflictException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceNotFoundException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceUnprocessableEntityException;
import com.edufelizardo.maissaudepublica.models.AtivacaoPlano;
import com.edufelizardo.maissaudepublica.models.Emergencia;
import com.edufelizardo.maissaudepublica.models.NivelEmergencia;
import com.edufelizardo.maissaudepublica.models.PlanoContingencia;
import com.edufelizardo.maissaudepublica.models.RegistroAtivacaoPlano;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.AtivacaoPlanoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.DesativacaoPlanoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.EmergenciaDto;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoAtivacaoPlano;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoPlanoContingencia;
import com.edufelizardo.maissaudepublica.models.enuns.TipoRegistroAtivacao;
import com.edufelizardo.maissaudepublica.repositories.AtivacaoPlanoRepository;
import com.edufelizardo.maissaudepublica.repositories.PlanoContingenciaRepository;
import com.edufelizardo.maissaudepublica.repositories.RegistroAtivacaoPlanoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * A ativação do plano de contingência na emergência (ADR-0157): ativar, desativar e o histórico, que ganha um passo a
 * cada mudança de nível da emergência. Não confere acesso nem a situação da emergência: o {@link EmergenciaService}
 * confere antes de chamar e devolve a emergência.
 */
@Component
class AtivacaoPlanoService {

    @Autowired private PlanoContingenciaRepository planoRepository;
    @Autowired private AtivacaoPlanoRepository ativacaoRepository;
    @Autowired private RegistroAtivacaoPlanoRepository registroRepository;

    /** O plano vigente, em vigência, do tipo da emergência e cobrindo o nível atual (422); o mesmo plano já ativo, 409. */
    void ativar(Emergencia em, AtivacaoPlanoRequestDto dto) {
        PlanoContingencia p = planoRepository.findById(dto.getPlanoId()).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar o plano de contingência " + dto.getPlanoId() + " em nossos registros."));
        if (p.getSituacao() != SituacaoPlanoContingencia.VIGENTE) {
            throw new ResourceUnprocessableEntityException("Só a versão vigente do plano se ativa.");
        }
        LocalDate hoje = LocalDate.now();
        if (p.getVigenciaInicio().isAfter(hoje) || (p.getVigenciaFim() != null && p.getVigenciaFim().isBefore(hoje))) {
            throw new ResourceUnprocessableEntityException("O plano está fora da vigência.");
        }
        if (!p.getTipo().getUuid().equals(em.getTipo().getUuid())) {
            throw new ResourceUnprocessableEntityException("O plano é de " + p.getTipo().getNome() + "; a emergência é de " + em.getTipo().getNome() + ".");
        }
        if (!cobre(p, em.getNivel())) {
            throw new ResourceUnprocessableEntityException("O plano não cobre o " + em.getNivel().getNome() + ".");
        }
        boolean jaAtivo = ativacaoRepository.findDaEmergencia(em.getUuid()).stream()
                .anyMatch(a -> a.getSituacao() == SituacaoAtivacaoPlano.ATIVA && a.getPlano().getCodigo().equalsIgnoreCase(p.getCodigo()));
        if (jaAtivo) {
            throw new ResourceConflictException("O plano " + p.getNome() + " já está ativo nesta emergência.");
        }
        Instant agora = Instant.now();
        AtivacaoPlano a = ativacaoRepository.save(new AtivacaoPlano(null, em, p, SituacaoAtivacaoPlano.ATIVA, agora, null));
        registrar(a, TipoRegistroAtivacao.ATIVADO, em.getNivel(), true, dto.getAutoridade().trim(), dto.getJustificativa().trim(), agora);
        ContextoAuditoria.detalhe("Plano " + p.getCodigo() + " v" + p.getVersao() + " ativado no " + em.getNivel().getNome() + ".");
    }

    /** Desativa o plano da emergência (de outra, 404; já desativado, 422). */
    void desativar(Emergencia em, UUID ativacaoId, DesativacaoPlanoRequestDto dto) {
        AtivacaoPlano a = ativacaoRepository.findById(ativacaoId).filter(x -> x.getEmergencia().getUuid().equals(em.getUuid()))
                .orElseThrow(() -> new ResourceNotFoundException("Não foi possível encontrar a ativação " + ativacaoId + " nesta emergência."));
        if (a.getSituacao() != SituacaoAtivacaoPlano.ATIVA) {
            throw new ResourceUnprocessableEntityException("O plano já foi desativado.");
        }
        fechar(a, em, texto(dto.getAutoridade()), dto.getJustificativa().trim());
        ContextoAuditoria.detalhe("Plano " + a.getPlano().getCodigo() + " desativado.");
    }

    /** A mudança de nível da emergência entra no histórico de cada plano ativo, dizendo se ele cobre o nível novo. */
    void registrarNivel(Emergencia em, NivelEmergencia novo, String justificativa) {
        Instant agora = Instant.now();
        for (AtivacaoPlano a : ativas(em)) {
            registrar(a, TipoRegistroAtivacao.NIVEL_ALTERADO, novo, cobre(a.getPlano(), novo), null, justificativa.trim(), agora);
        }
    }

    /** Desmobilizar ou encerrar a emergência desativa os planos ativos; devolve quantos. */
    int desativarTodas(Emergencia em, String justificativa) {
        List<AtivacaoPlano> ativas = ativas(em);
        for (AtivacaoPlano a : ativas) {
            fechar(a, em, null, justificativa);
        }
        return ativas.size();
    }

    /** Os planos da emergência (os ativos primeiro), com os passos e, nos ativos, as ações previstas para o nível atual. */
    List<EmergenciaDto.PlanoAtivo> planosDaEmergencia(Emergencia em) {
        List<AtivacaoPlano> ativacoes = ativacaoRepository.findDaEmergencia(em.getUuid());
        if (ativacoes.isEmpty()) {
            return List.of();
        }
        Map<UUID, List<RegistroAtivacaoPlano>> registros = new HashMap<>();
        for (RegistroAtivacaoPlano r : registroRepository.findDasAtivacoes(ativacoes.stream().map(AtivacaoPlano::getUuid).toList())) {
            registros.computeIfAbsent(r.getAtivacao().getUuid(), k -> new ArrayList<>()).add(r);
        }
        List<EmergenciaDto.PlanoAtivo> lista = new ArrayList<>();
        for (AtivacaoPlano a : ativacoes) {
            PlanoContingencia p = a.getPlano();
            List<RegistroAtivacaoPlano> passos = registros.getOrDefault(a.getUuid(), List.of());
            boolean ativa = a.getSituacao() == SituacaoAtivacaoPlano.ATIVA;
            List<EmergenciaDto.AcaoPrevista> acoes = List.of();
            if (ativa) {
                // O prazo conta de quando a emergência chegou ao nível atual com o plano ativo: o passo mais recente.
                Instant base = passos.isEmpty() ? a.getAtivadaEm() : passos.get(0).getRegistradoEm();
                acoes = p.getAcoes().stream().filter(x -> x.getNivel().getUuid().equals(em.getNivel().getUuid()))
                        .map(x -> new EmergenciaDto.AcaoPrevista(x.getDescricao(), x.getResponsavel(), x.getPrazoHoras(),
                                x.getPrazoHoras() != null ? base.plus(Duration.ofHours(x.getPrazoHoras())) : null,
                                x.getUnidade() != null ? x.getUnidade().getNome() : null)).toList();
            }
            lista.add(new EmergenciaDto.PlanoAtivo(a.getUuid(), p.getUuid(), p.getCodigo(), p.getNome(), p.getVersao(), a.getSituacao(), a.getAtivadaEm(),
                    a.getDesativadaEm(), cobre(p, em.getNivel()),
                    passos.stream().map(r -> new EmergenciaDto.RegistroAtivacao(r.getTipo(), r.getNivel().getNome(), r.isNivelCoberto(), r.getAutoridade(),
                            r.getJustificativa(), r.getRegistradoEm())).toList(), acoes));
        }
        lista.sort(Comparator.comparing((EmergenciaDto.PlanoAtivo x) -> x.getSituacao() != SituacaoAtivacaoPlano.ATIVA)
                .thenComparing(EmergenciaDto.PlanoAtivo::getAtivadaEm));
        return lista;
    }

    private void fechar(AtivacaoPlano a, Emergencia em, String autoridade, String justificativa) {
        Instant agora = Instant.now();
        a.setSituacao(SituacaoAtivacaoPlano.DESATIVADA);
        a.setDesativadaEm(agora);
        ativacaoRepository.save(a);
        registrar(a, TipoRegistroAtivacao.DESATIVADO, em.getNivel(), cobre(a.getPlano(), em.getNivel()), autoridade, justificativa, agora);
    }

    private List<AtivacaoPlano> ativas(Emergencia em) {
        return ativacaoRepository.findDaEmergencia(em.getUuid()).stream().filter(a -> a.getSituacao() == SituacaoAtivacaoPlano.ATIVA).toList();
    }

    private void registrar(AtivacaoPlano a, TipoRegistroAtivacao tipo, NivelEmergencia nivel, boolean coberto, String autoridade, String justificativa,
                           Instant em) {
        registroRepository.save(new RegistroAtivacaoPlano(null, a, tipo, nivel, coberto, autoridade, justificativa, em, UsuarioAutenticado.cpf()));
    }

    private static boolean cobre(PlanoContingencia p, NivelEmergencia n) {
        return p.getNiveis().stream().anyMatch(x -> x.getUuid().equals(n.getUuid()));
    }

    private static String texto(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }
}
