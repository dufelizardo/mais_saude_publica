package com.edufelizardo.maissaudepublica.services.version1;

import com.edufelizardo.maissaudepublica.config.ContextoAuditoria;
import com.edufelizardo.maissaudepublica.config.UsuarioAutenticado;
import com.edufelizardo.maissaudepublica.exceptions.ResourceBadRequestException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceConflictException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceNotFoundException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceUnprocessableEntityException;
import com.edufelizardo.maissaudepublica.models.AcaoEmergencial;
import com.edufelizardo.maissaudepublica.models.AcaoPlano;
import com.edufelizardo.maissaudepublica.models.AtivacaoCapacidade;
import com.edufelizardo.maissaudepublica.models.AtivacaoPlano;
import com.edufelizardo.maissaudepublica.models.AvaliacaoImpacto;
import com.edufelizardo.maissaudepublica.models.CapacidadeEmergencial;
import com.edufelizardo.maissaudepublica.models.Emergencia;
import com.edufelizardo.maissaudepublica.models.Mobilizacao;
import com.edufelizardo.maissaudepublica.models.Profissional;
import com.edufelizardo.maissaudepublica.models.RegistroAtivacaoPlano;
import com.edufelizardo.maissaudepublica.models.TransferenciaFarmacia;
import com.edufelizardo.maissaudepublica.models.UnidadeDeSaude;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.AcaoEmergencialRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.AtivacaoCapacidadeRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.MobilizacaoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.SituacaoAcaoEmergencialRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.SituacaoMobilizacaoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.TransferenciaMobilizacaoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.EmergenciaDto;
import com.edufelizardo.maissaudepublica.models.enuns.PrioridadeAcaoEmergencial;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoAcaoEmergencial;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoAtivacaoCapacidade;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoAtivacaoPlano;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoMobilizacao;
import com.edufelizardo.maissaudepublica.models.enuns.StatusTransferenciaFarmacia;
import com.edufelizardo.maissaudepublica.models.enuns.TipoMobilizacao;
import com.edufelizardo.maissaudepublica.models.enuns.TipoRegistroAtivacao;
import com.edufelizardo.maissaudepublica.repositories.AcaoEmergencialRepository;
import com.edufelizardo.maissaudepublica.repositories.AtivacaoCapacidadeRepository;
import com.edufelizardo.maissaudepublica.repositories.AtivacaoPlanoRepository;
import com.edufelizardo.maissaudepublica.repositories.CapacidadeEmergencialRepository;
import com.edufelizardo.maissaudepublica.repositories.MobilizacaoRepository;
import com.edufelizardo.maissaudepublica.repositories.ProfissionalRepository;
import com.edufelizardo.maissaudepublica.repositories.RegistroAtivacaoPlanoRepository;
import com.edufelizardo.maissaudepublica.repositories.TransferenciaFarmaciaRepository;
import com.edufelizardo.maissaudepublica.repositories.UnidadeDeSaudeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * A coordenação da emergência, fatia E3 (ADR-0158): o quadro de ações (avulsas ou geradas das ações previstas do plano
 * ativo), as mobilizações (Emergências registra, o dono executa) e a capacidade emergencial ativada. Não confere acesso
 * nem a situação da emergência: o {@link EmergenciaService} confere antes de chamar.
 */
@Component
class CoordenacaoEmergenciaService {

    @Autowired private AcaoEmergencialRepository acaoRepository;
    @Autowired private MobilizacaoRepository mobilizacaoRepository;
    @Autowired private CapacidadeEmergencialRepository capacidadeRepository;
    @Autowired private AtivacaoCapacidadeRepository ativacaoCapacidadeRepository;
    @Autowired private AtivacaoPlanoRepository ativacaoPlanoRepository;
    @Autowired private RegistroAtivacaoPlanoRepository registroRepository;
    @Autowired private UnidadeDeSaudeRepository unidadeRepository;
    @Autowired private ProfissionalRepository profissionalRepository;
    @Autowired private TransferenciaFarmaciaRepository transferenciaRepository;

    // ── Ações ──────────────────────────────────────────────────────────────────────────────────────

    void novaAcao(Emergencia em, AcaoEmergencialRequestDto dto) {
        AcaoEmergencial a = nova(em, dto.getDescricao().trim(), dto.getResponsavel().trim(), dto.getUnidadeId() != null ? unidade(dto.getUnidadeId()) : null,
                dto.getPrioridade(), dto.getPrazo());
        acaoRepository.save(a);
        ContextoAuditoria.detalhe("Ação emergencial: " + a.getDescricao() + ".");
    }

    /**
     * Uma ação para cada ação prevista do nível atual nos planos ativos, com o prazo contado do passo mais recente do plano;
     * a já gerada nesta emergência não se repete. Sem nada a gerar, 422.
     */
    void gerarAcoesDoPlano(Emergencia em) {
        Set<UUID> jaGeradas = new HashSet<>();
        acaoRepository.findDaEmergencia(em.getUuid()).forEach(a -> {
            if (a.getAcaoPlano() != null) {
                jaGeradas.add(a.getAcaoPlano().getUuid());
            }
        });
        int geradas = 0;
        for (AtivacaoPlano at : ativacaoPlanoRepository.findDaEmergencia(em.getUuid())) {
            if (at.getSituacao() != SituacaoAtivacaoPlano.ATIVA) {
                continue;
            }
            List<RegistroAtivacaoPlano> passos = registroRepository.findDasAtivacoes(List.of(at.getUuid()));
            Instant base = passos.stream().filter(r -> r.getTipo() != TipoRegistroAtivacao.DESATIVADO).map(RegistroAtivacaoPlano::getRegistradoEm)
                    .findFirst().orElse(at.getAtivadaEm());
            for (AcaoPlano ap : at.getPlano().getAcoes()) {
                if (!ap.getNivel().getUuid().equals(em.getNivel().getUuid()) || jaGeradas.contains(ap.getUuid())) {
                    continue;
                }
                AcaoEmergencial a = nova(em, ap.getDescricao(), ap.getResponsavel(), ap.getUnidade(), PrioridadeAcaoEmergencial.ALTA,
                        ap.getPrazoHoras() != null ? base.plus(Duration.ofHours(ap.getPrazoHoras())) : null);
                a.setAcaoPlano(ap);
                a.setAtivacao(at);
                acaoRepository.save(a);
                jaGeradas.add(ap.getUuid());
                geradas++;
            }
        }
        if (geradas == 0) {
            throw new ResourceUnprocessableEntityException("Não há ação prevista nova para o nível atual nos planos ativos.");
        }
        ContextoAuditoria.detalhe(geradas + " ação(ões) gerada(s) do plano.");
    }

    /** Pendente → em execução → concluída; ou cancelada com o motivo (400). Concluída ou cancelada não muda (422). */
    void situacaoDaAcao(Emergencia em, UUID acaoId, SituacaoAcaoEmergencialRequestDto dto) {
        AcaoEmergencial a = acaoRepository.findById(acaoId).filter(x -> x.getEmergencia().getUuid().equals(em.getUuid()))
                .orElseThrow(() -> new ResourceNotFoundException("Não foi possível encontrar a ação " + acaoId + " nesta emergência."));
        if (a.getSituacao() == SituacaoAcaoEmergencial.CONCLUIDA || a.getSituacao() == SituacaoAcaoEmergencial.CANCELADA) {
            throw new ResourceUnprocessableEntityException("A ação já foi " + (a.getSituacao() == SituacaoAcaoEmergencial.CONCLUIDA ? "concluída." : "cancelada."));
        }
        Instant agora = Instant.now();
        switch (dto.getSituacao()) {
            case EM_EXECUCAO -> a.setIniciadaEm(a.getIniciadaEm() != null ? a.getIniciadaEm() : agora);
            case CONCLUIDA -> {
                a.setIniciadaEm(a.getIniciadaEm() != null ? a.getIniciadaEm() : agora);
                a.setConcluidaEm(agora);
            }
            case CANCELADA -> {
                if (texto(dto.getMotivo()) == null) {
                    throw new ResourceBadRequestException("Informe o motivo do cancelamento.");
                }
                a.setMotivoCancelamento(dto.getMotivo().trim());
            }
            default -> throw new ResourceBadRequestException("A ação passa a em execução, concluída ou cancelada.");
        }
        a.setSituacao(dto.getSituacao());
        if (texto(dto.getObservacao()) != null) {
            a.setObservacao(dto.getObservacao().trim());
        }
        acaoRepository.save(a);
        ContextoAuditoria.detalhe("Ação emergencial " + dto.getSituacao() + ".");
    }

    int atrasadas(Emergencia em) {
        Instant agora = Instant.now();
        return (int) acaoRepository.findDaEmergencia(em.getUuid()).stream().filter(a -> a.atrasada(agora)).count();
    }

    // ── Mobilização ────────────────────────────────────────────────────────────────────────────────

    /**
     * Registra a mobilização: o profissional (pela matrícula, ativo; 404/422) ou o recurso dos demais tipos (400); o leito
     * pode apontar a capacidade ativada nesta emergência (422 se for de outra). Fim antes do início, 400. O profissional
     * nasce em andamento (já autoriza a Escala); os demais, solicitados.
     */
    void mobilizar(Emergencia em, MobilizacaoRequestDto dto) {
        LocalDate inicio = dto.getInicio() != null ? dto.getInicio() : LocalDate.now();
        if (dto.getFim() != null && dto.getFim().isBefore(inicio)) {
            throw new ResourceBadRequestException("O fim da mobilização é antes do início.");
        }
        Mobilizacao m = new Mobilizacao();
        m.setEmergencia(em);
        m.setTipo(dto.getTipo());
        m.setOrigem(dto.getOrigemId() != null ? unidade(dto.getOrigemId()) : null);
        m.setDestino(unidade(dto.getDestinoId()));
        if (dto.getTipo() == TipoMobilizacao.PROFISSIONAL) {
            String matricula = texto(dto.getProfissionalMatricula());
            if (matricula == null) {
                throw new ResourceBadRequestException("Informe a matrícula do profissional mobilizado.");
            }
            Profissional p = profissionalRepository.findByMatricula(matricula).orElseThrow(() -> new ResourceNotFoundException(
                    "Não foi possível encontrar um profissional com a matrícula " + matricula + " em nossos registros."));
            if (!p.isAtivo()) {
                throw new ResourceUnprocessableEntityException(p.getNome() + " está desligado.");
            }
            boolean jaMobilizado = mobilizacaoRepository.findDaEmergencia(em.getUuid()).stream()
                    .anyMatch(x -> x.getProfissional() != null && x.getProfissional().getUuid().equals(p.getUuid())
                            && x.getDestino().getUuid().equals(dto.getDestinoId()) && aberta(x));
            if (jaMobilizado) {
                throw new ResourceConflictException(p.getNome() + " já está mobilizado para a " + m.getDestino().getNome() + " nesta emergência.");
            }
            m.setProfissional(p);
            m.setRecurso(texto(dto.getRecurso()) != null ? dto.getRecurso().trim() : p.getNome());
            m.setSituacao(SituacaoMobilizacao.EM_ANDAMENTO);
        } else {
            if (texto(dto.getRecurso()) == null) {
                throw new ResourceBadRequestException("Descreva o recurso mobilizado.");
            }
            m.setRecurso(dto.getRecurso().trim());
            m.setSituacao(SituacaoMobilizacao.SOLICITADA);
        }
        if (dto.getCapacidadeAtivacaoId() != null) {
            if (dto.getTipo() != TipoMobilizacao.LEITO) {
                throw new ResourceUnprocessableEntityException("Só a mobilização de leito aponta a capacidade ativada.");
            }
            m.setCapacidadeAtivacao(ativacaoCapacidadeRepository.findById(dto.getCapacidadeAtivacaoId())
                    .filter(x -> x.getEmergencia().getUuid().equals(em.getUuid()))
                    .orElseThrow(() -> new ResourceUnprocessableEntityException("A capacidade ativada não é desta emergência.")));
        }
        m.setQuantidade(dto.getQuantidade());
        m.setResponsavel(texto(dto.getResponsavel()));
        m.setInicio(inicio);
        m.setFim(dto.getFim());
        m.setRegistradaEm(Instant.now());
        m.setRegistradoPorCpf(UsuarioAutenticado.cpf());
        mobilizacaoRepository.save(m);
        ContextoAuditoria.detalhe("Mobilização " + m.getTipo() + ": " + m.getRecurso() + " para a " + m.getDestino().getNome() + ".");
    }

    /**
     * Solicitada → em andamento → concluída; ou cancelada com o motivo (400). Concluída ou cancelada não muda (422). A ligada a
     * uma transferência segue a transferência (422). Concluir ou cancelar o profissional acaba a exceção da Escala para turnos
     * novos; os já designados ficam.
     */
    void situacaoDaMobilizacao(Emergencia em, UUID id, SituacaoMobilizacaoRequestDto dto) {
        Mobilizacao m = mobilizacao(em, id);
        if (m.getTransferencia() != null) {
            throw new ResourceUnprocessableEntityException("A mobilização segue a transferência da Farmácia ligada a ela.");
        }
        if (!aberta(m)) {
            throw new ResourceUnprocessableEntityException("A mobilização já foi " + (m.getSituacao() == SituacaoMobilizacao.CONCLUIDA ? "concluída." : "cancelada."));
        }
        switch (dto.getSituacao()) {
            case EM_ANDAMENTO -> { }
            case CONCLUIDA -> m.setFim(dto.getFim() != null ? dto.getFim() : LocalDate.now());
            case CANCELADA -> {
                if (texto(dto.getMotivo()) == null) {
                    throw new ResourceBadRequestException("Informe o motivo do cancelamento.");
                }
                m.setMotivoCancelamento(dto.getMotivo().trim());
            }
            default -> throw new ResourceBadRequestException("A mobilização passa a em andamento, concluída ou cancelada.");
        }
        if (m.getFim() != null && m.getFim().isBefore(m.getInicio())) {
            throw new ResourceBadRequestException("O fim da mobilização é antes do início.");
        }
        m.setSituacao(dto.getSituacao());
        mobilizacaoRepository.save(m);
        ContextoAuditoria.detalhe("Mobilização " + dto.getSituacao() + ".");
    }

    /** Liga a mobilização de medicamento (422 nos demais) a uma transferência da Farmácia para a mesma unidade de destino (422). */
    void ligarTransferencia(Emergencia em, UUID id, TransferenciaMobilizacaoRequestDto dto) {
        Mobilizacao m = mobilizacao(em, id);
        if (m.getTipo() != TipoMobilizacao.MEDICAMENTO) {
            throw new ResourceUnprocessableEntityException("Só a mobilização de medicamento se liga a uma transferência da Farmácia.");
        }
        TransferenciaFarmacia t = transferenciaRepository.findById(dto.getTransferenciaId()).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar a transferência " + dto.getTransferenciaId() + " em nossos registros."));
        if (!t.getUnidadeDestino().getUuid().equals(m.getDestino().getUuid())) {
            throw new ResourceUnprocessableEntityException("A transferência vai para a " + t.getUnidadeDestino().getNome() + ", não para a "
                    + m.getDestino().getNome() + ".");
        }
        m.setTransferencia(t);
        mobilizacaoRepository.save(m);
        ContextoAuditoria.detalhe("Mobilização ligada à transferência " + t.getUuid() + ".");
    }

    // ── Capacidade emergencial ─────────────────────────────────────────────────────────────────────

    /** Ativa até o que resta do ativável da unidade (somando as ativações de todas as emergências; acima, 422); inativa, 422. */
    void ativarCapacidade(Emergencia em, AtivacaoCapacidadeRequestDto dto) {
        CapacidadeEmergencial c = capacidadeRepository.findById(dto.getCapacidadeId()).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar a capacidade emergencial " + dto.getCapacidadeId() + " em nossos registros."));
        if (!c.isAtiva()) {
            throw new ResourceUnprocessableEntityException("A capacidade emergencial está inativa.");
        }
        long resta = c.getEmergencial() - ativacaoCapacidadeRepository.somaAtivas(c.getUuid());
        if (dto.getQuantidade() > resta) {
            throw new ResourceUnprocessableEntityException("A " + c.getUnidade().getNome() + " pode ativar mais " + resta + ", não " + dto.getQuantidade() + ".");
        }
        ativacaoCapacidadeRepository.save(new AtivacaoCapacidade(null, em, c, dto.getQuantidade(), SituacaoAtivacaoCapacidade.ATIVA, Instant.now(), null,
                dto.getJustificativa().trim(), null, UsuarioAutenticado.cpf()));
        ContextoAuditoria.detalhe("Capacidade emergencial ativada: " + dto.getQuantidade() + " " + c.getRecurso() + " na " + c.getUnidade().getNome() + ".");
    }

    void desativarCapacidade(Emergencia em, UUID ativacaoId, String justificativa) {
        AtivacaoCapacidade a = ativacaoCapacidadeRepository.findById(ativacaoId).filter(x -> x.getEmergencia().getUuid().equals(em.getUuid()))
                .orElseThrow(() -> new ResourceNotFoundException("Não foi possível encontrar a capacidade ativada " + ativacaoId + " nesta emergência."));
        if (a.getSituacao() != SituacaoAtivacaoCapacidade.ATIVA) {
            throw new ResourceUnprocessableEntityException("A capacidade já foi desativada.");
        }
        fechar(a, justificativa.trim());
    }

    /** Encerrar a emergência desativa a capacidade ativada nela. */
    void desativarCapacidades(Emergencia em, String justificativa) {
        ativacaoCapacidadeRepository.findDaEmergencia(em.getUuid()).stream().filter(a -> a.getSituacao() == SituacaoAtivacaoCapacidade.ATIVA)
                .forEach(a -> fechar(a, justificativa));
    }

    private void fechar(AtivacaoCapacidade a, String justificativa) {
        a.setSituacao(SituacaoAtivacaoCapacidade.DESATIVADA);
        a.setFim(Instant.now());
        a.setJustificativaFim(justificativa);
        ativacaoCapacidadeRepository.save(a);
    }

    // ── Leitura ────────────────────────────────────────────────────────────────────────────────────

    /** O quadro de ações (atrasadas, depois por prioridade), as mobilizações, a capacidade ativada e as unidades afetadas. */
    void preencher(EmergenciaDto d, Emergencia em, AvaliacaoImpacto ultima) {
        Instant agora = Instant.now();
        List<AcaoEmergencial> acoes = acaoRepository.findDaEmergencia(em.getUuid());
        d.setAcoes(acoes.stream()
                .sorted(Comparator.comparing((AcaoEmergencial a) -> !a.atrasada(agora))
                        .thenComparing(a -> a.getSituacao() == SituacaoAcaoEmergencial.CONCLUIDA || a.getSituacao() == SituacaoAcaoEmergencial.CANCELADA)
                        .thenComparing(a -> -a.getPrioridade().ordinal())
                        .thenComparing(a -> a.getPrazo() == null ? Instant.MAX : a.getPrazo()))
                .map(a -> new EmergenciaDto.AcaoItem(a.getUuid(), a.getDescricao(), a.getResponsavel(), a.getUnidade() != null ? a.getUnidade().getNome() : null,
                        a.getPrioridade(), a.getPrazo(), a.getSituacao(), a.atrasada(agora),
                        a.getAtivacao() != null ? a.getAtivacao().getPlano().getCodigo() : null, a.getIniciadaEm(), a.getConcluidaEm(), a.getMotivoCancelamento(),
                        a.getObservacao())).toList());
        d.setAcoesAtrasadas((int) acoes.stream().filter(a -> a.atrasada(agora)).count());
        LocalDate hoje = LocalDate.now();
        d.setMobilizacoes(mobilizacaoRepository.findDaEmergencia(em.getUuid()).stream().map(m -> {
            SituacaoMobilizacao s = situacaoEfetiva(m);
            boolean autoriza = m.getTipo() == TipoMobilizacao.PROFISSIONAL && s == SituacaoMobilizacao.EM_ANDAMENTO && !m.getInicio().isAfter(hoje)
                    && (m.getFim() == null || !m.getFim().isBefore(hoje)) && em.getSituacao() == com.edufelizardo.maissaudepublica.models.enuns.SituacaoEmergencia.ATIVA;
            return new EmergenciaDto.MobilizacaoItem(m.getUuid(), m.getTipo(), m.getOrigem() != null ? m.getOrigem().getNome() : null, m.getDestino().getUuid(),
                    m.getDestino().getNome(), m.getProfissional() != null ? m.getProfissional().getMatricula() : null,
                    m.getProfissional() != null ? m.getProfissional().getNome() : null, m.getRecurso(), m.getQuantidade(), m.getResponsavel(), m.getInicio(),
                    m.getFim(), s, m.getTransferencia() != null ? m.getTransferencia().getUuid() : null,
                    m.getTransferencia() != null ? m.getTransferencia().getStatus() : null, autoriza, m.getMotivoCancelamento());
        }).toList());
        d.setCapacidades(ativacaoCapacidadeRepository.findDaEmergencia(em.getUuid()).stream()
                .sorted(Comparator.comparing((AtivacaoCapacidade a) -> a.getSituacao() != SituacaoAtivacaoCapacidade.ATIVA))
                .map(a -> new EmergenciaDto.CapacidadeAtivada(a.getUuid(), a.getCapacidade().getUuid(), a.getCapacidade().getUnidade().getNome(),
                        a.getCapacidade().getRecurso(), a.getCapacidade().getNormal(), a.getCapacidade().getEmergencial(), a.getQuantidade(), a.getSituacao(),
                        a.getInicio(), a.getFim(), a.getJustificativa(), a.getJustificativaFim())).toList());
        d.setUnidadesAfetadas(ultima == null ? List.of() : ultima.getUnidadesAfetadas().stream()
                .sorted(Comparator.comparing(UnidadeDeSaude::getNome, String.CASE_INSENSITIVE_ORDER))
                .map(u -> new EmergenciaDto.UnidadeAfetada(u.getUuid(), u.getNome(), u.getSituacaoOperacional())).toList());
    }

    /** A ligada a uma transferência acompanha a dela: recebida conclui, cancelada cancela, em trânsito anda. */
    private static SituacaoMobilizacao situacaoEfetiva(Mobilizacao m) {
        TransferenciaFarmacia t = m.getTransferencia();
        if (t == null) {
            return m.getSituacao();
        }
        StatusTransferenciaFarmacia s = t.getStatus();
        if (s == StatusTransferenciaFarmacia.CANCELADA) {
            return SituacaoMobilizacao.CANCELADA;
        }
        return s == StatusTransferenciaFarmacia.EM_TRANSITO ? SituacaoMobilizacao.EM_ANDAMENTO : SituacaoMobilizacao.CONCLUIDA;
    }

    private static boolean aberta(Mobilizacao m) {
        return m.getSituacao() == SituacaoMobilizacao.SOLICITADA || m.getSituacao() == SituacaoMobilizacao.EM_ANDAMENTO;
    }

    private AcaoEmergencial nova(Emergencia em, String descricao, String responsavel, UnidadeDeSaude unidade, PrioridadeAcaoEmergencial prioridade,
                                 Instant prazo) {
        AcaoEmergencial a = new AcaoEmergencial();
        a.setEmergencia(em);
        a.setDescricao(descricao);
        a.setResponsavel(responsavel);
        a.setUnidade(unidade);
        a.setPrioridade(prioridade);
        a.setPrazo(prazo);
        a.setSituacao(SituacaoAcaoEmergencial.PENDENTE);
        a.setCriadaEm(Instant.now());
        a.setRegistradoPorCpf(UsuarioAutenticado.cpf());
        return a;
    }

    private Mobilizacao mobilizacao(Emergencia em, UUID id) {
        return mobilizacaoRepository.findById(id).filter(x -> x.getEmergencia().getUuid().equals(em.getUuid()))
                .orElseThrow(() -> new ResourceNotFoundException("Não foi possível encontrar a mobilização " + id + " nesta emergência."));
    }

    private UnidadeDeSaude unidade(UUID id) {
        return unidadeRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar uma unidade de saúde com o id " + id + " em nossos registros."));
    }

    private static String texto(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }
}
