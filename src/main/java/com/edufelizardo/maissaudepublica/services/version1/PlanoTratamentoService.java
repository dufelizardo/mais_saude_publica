package com.edufelizardo.maissaudepublica.services.version1;

import com.edufelizardo.maissaudepublica.config.ContextoAuditoria;
import com.edufelizardo.maissaudepublica.config.UsuarioAutenticado;
import com.edufelizardo.maissaudepublica.exceptions.ResourceBadRequestException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceConflictException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceNotFoundException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceUnprocessableEntityException;
import com.edufelizardo.maissaudepublica.models.Atendimento;
import com.edufelizardo.maissaudepublica.models.AvaliacaoOdontologica;
import com.edufelizardo.maissaudepublica.models.ComplementoOdontologico;
import com.edufelizardo.maissaudepublica.models.ItemPlanoTratamento;
import com.edufelizardo.maissaudepublica.models.PlanoTratamento;
import com.edufelizardo.maissaudepublica.models.Procedimento;
import com.edufelizardo.maissaudepublica.models.ProcedimentoCatalogo;
import com.edufelizardo.maissaudepublica.models.Profissional;
import com.edufelizardo.maissaudepublica.models.SolicitacaoRegulacao;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.ItemPlanoTratamentoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.PlanoTratamentoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.SituacaoItemPlanoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.PlanoTratamentoDto;
import com.edufelizardo.maissaudepublica.models.enuns.FaceDente;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoItemPlanoTratamento;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoPlanoTratamento;
import com.edufelizardo.maissaudepublica.models.enuns.StatusProcedimento;
import com.edufelizardo.maissaudepublica.repositories.AtendimentoRepository;
import com.edufelizardo.maissaudepublica.repositories.AvaliacaoOdontologicaRepository;
import com.edufelizardo.maissaudepublica.repositories.ItemPlanoTratamentoRepository;
import com.edufelizardo.maissaudepublica.repositories.PacienteRepository;
import com.edufelizardo.maissaudepublica.repositories.PlanoTratamentoRepository;
import com.edufelizardo.maissaudepublica.repositories.ProfissionalRepository;
import com.edufelizardo.maissaudepublica.repositories.SolicitacaoRegulacaoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Plano de tratamento odontológico, fatia SB3b (ADR-0137, desenho na ADR-0111): o plano com os itens previstos, que os
 * procedimentos executam. Abrir, incluir item, mudar a situação, concluir e abandonar são do cirurgião-dentista
 * ({@code ODONTOLOGIA.PLANEJAR}); executar é registrar o procedimento, como na SB3a. Um plano aberto por paciente.
 */
@Service
public class PlanoTratamentoService {

    static final String PLANEJAR = "ODONTOLOGIA.PLANEJAR";

    @Autowired
    private PlanoTratamentoRepository planoRepository;

    @Autowired
    private ItemPlanoTratamentoRepository itemRepository;

    @Autowired
    private AtendimentoRepository atendimentoRepository;

    @Autowired
    private AvaliacaoOdontologicaRepository avaliacaoRepository;

    @Autowired
    private ProfissionalRepository profissionalRepository;

    @Autowired
    private SolicitacaoRegulacaoRepository solicitacaoRepository;

    @Autowired
    private PacienteRepository pacienteRepository;

    @Autowired
    private ProcedimentoCatalogoService catalogoService;

    @Autowired
    private ControleDeAcesso controleDeAcesso;

    @Autowired
    private VinculoAssistencialService vinculoService;

    // ── Plano ──────────────────────────────────────────────────────────────────────────────────────

    /**
     * Abre o plano no atendimento de odontologia (422 fora dele). Um aberto por paciente (409). Sem diagnóstico ou
     * prioridade, valem os da avaliação de origem; sem avaliação, a prioridade é obrigatória (400).
     */
    @Transactional
    public PlanoTratamentoDto abrir(PlanoTratamentoRequestDto dto) {
        Atendimento atendimento = atendimentoRepository.findById(dto.getAtendimentoId()).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar um atendimento com o id " + dto.getAtendimentoId() + " em nossos registros."));
        controleDeAcesso.exigir(PLANEJAR, atendimento.getUnidade());
        ContextoAuditoria.paciente(atendimento.getPaciente().getUuid());
        if (atendimento.getEspecialidade() == null || !atendimento.getEspecialidade().isOdontologica()) {
            throw new ResourceUnprocessableEntityException("O plano de tratamento é aberto num atendimento de especialidade odontológica.");
        }
        if (planoRepository.existsByPaciente_UuidAndSituacao(atendimento.getPaciente().getUuid(), SituacaoPlanoTratamento.ABERTO)) {
            throw new ResourceConflictException("O paciente já tem um plano de tratamento aberto: inclua os itens nele, ou conclua ou abandone antes.");
        }
        AvaliacaoOdontologica avaliacao = null;
        if (dto.getAvaliacaoId() != null) {
            avaliacao = avaliacaoRepository.findById(dto.getAvaliacaoId()).orElseThrow(() -> new ResourceNotFoundException(
                    "Não foi possível encontrar a avaliação odontológica " + dto.getAvaliacaoId() + " em nossos registros."));
            if (!avaliacao.getAtendimento().getPaciente().getUuid().equals(atendimento.getPaciente().getUuid())) {
                throw new ResourceUnprocessableEntityException("A avaliação odontológica é de outro paciente.");
            }
        }
        if (dto.getPrioridade() == null && avaliacao == null) {
            throw new ResourceBadRequestException("Informe a prioridade do plano (ou a avaliação de origem).");
        }
        Profissional profissional = profissionalRepository.findByMatricula(dto.getProfissionalMatricula()).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar um profissional com a matrícula " + dto.getProfissionalMatricula() + " em nossos registros."));

        PlanoTratamento plano = new PlanoTratamento();
        plano.setPaciente(atendimento.getPaciente());
        plano.setUnidade(atendimento.getUnidade());
        plano.setAtendimento(atendimento);
        plano.setAvaliacao(avaliacao);
        plano.setProfissional(profissional);
        plano.setDiagnostico(texto(dto.getDiagnostico()) != null ? texto(dto.getDiagnostico()) : avaliacao != null ? avaliacao.getDiagnostico() : null);
        plano.setObjetivo(texto(dto.getObjetivo()));
        plano.setPrioridade(dto.getPrioridade() != null ? dto.getPrioridade() : avaliacao.getNecessidadeTratamento());
        plano.setSituacao(SituacaoPlanoTratamento.ABERTO);
        plano.setAbertoEm(Instant.now());
        plano.setAbertoPorCpf(UsuarioAutenticado.cpf());
        plano = planoRepository.save(plano);
        int ordem = 0;
        for (ItemPlanoTratamentoRequestDto item : dto.getItens() == null ? List.<ItemPlanoTratamentoRequestDto>of() : dto.getItens()) {
            novoItem(plano, item, ++ordem);
        }
        ContextoAuditoria.registro(plano.getUuid());
        return dto(plano);
    }

    @Transactional
    public PlanoTratamentoDto incluirItem(UUID planoId, ItemPlanoTratamentoRequestDto dto) {
        PlanoTratamento plano = planoAberto(planoId);
        int ordem = itemRepository.findByPlano_Uuid(planoId).stream().mapToInt(ItemPlanoTratamento::getOrdem).max().orElse(0) + 1;
        novoItem(plano, dto, ordem);
        return dto(plano);
    }

    /**
     * O tratamento concluído (indicador da APS): nenhum item planejado ou em andamento, e ao menos um concluído (422).
     * Encaminhados e cancelados não impedem: o encaminhado segue pela Regulação.
     */
    @Transactional
    public PlanoTratamentoDto concluir(UUID planoId) {
        PlanoTratamento plano = planoAberto(planoId);
        List<ItemPlanoTratamento> itens = itemRepository.findByPlano_Uuid(planoId);
        long pendentes = itens.stream().filter(i -> i.getSituacao() == SituacaoItemPlanoTratamento.PLANEJADO
                || i.getSituacao() == SituacaoItemPlanoTratamento.EM_ANDAMENTO).count();
        if (pendentes > 0) {
            throw new ResourceUnprocessableEntityException("Há " + pendentes + " item(ns) planejado(s) ou em andamento: execute, encaminhe ou "
                    + "cancele antes de concluir o tratamento.");
        }
        if (itens.stream().noneMatch(i -> i.getSituacao() == SituacaoItemPlanoTratamento.CONCLUIDO)) {
            throw new ResourceUnprocessableEntityException("Nenhum item foi concluído: sem procedimento realizado não há tratamento concluído. "
                    + "Para encerrar sem tratamento, abandone o plano.");
        }
        encerrar(plano, SituacaoPlanoTratamento.CONCLUIDO, null);
        return dto(plano);
    }

    /** Abandono (o paciente não voltou, desistiu), com motivo (400). */
    @Transactional
    public PlanoTratamentoDto abandonar(UUID planoId, String motivo) {
        PlanoTratamento plano = planoAberto(planoId);
        if (texto(motivo) == null) {
            throw new ResourceBadRequestException("Informe o motivo do abandono.");
        }
        encerrar(plano, SituacaoPlanoTratamento.ABANDONADO, texto(motivo));
        return dto(plano);
    }

    // ── Item ───────────────────────────────────────────────────────────────────────────────────────

    /**
     * O dentista põe o item em andamento (tratamento de várias sessões), encaminha (com a solicitação de regulação do
     * mesmo paciente, opcional) ou cancela (com motivo, 400). Concluído só pelo procedimento realizado (400); concluído e
     * cancelado não mudam mais (422).
     */
    @Transactional
    public PlanoTratamentoDto mudarSituacao(UUID itemId, SituacaoItemPlanoRequestDto dto) {
        ItemPlanoTratamento item = itemRepository.findById(itemId).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar o item do plano " + itemId + " em nossos registros."));
        PlanoTratamento plano = planoAberto(item.getPlano().getUuid());
        ContextoAuditoria.registro(item.getUuid());
        SituacaoItemPlanoTratamento atual = item.getSituacao();
        if (atual == SituacaoItemPlanoTratamento.CONCLUIDO || atual == SituacaoItemPlanoTratamento.CANCELADO) {
            throw new ResourceUnprocessableEntityException("O item está " + atual + " e não muda mais.");
        }
        String motivo = texto(dto.getMotivo());
        switch (dto.getSituacao()) {
            case EM_ANDAMENTO -> {
                if (atual != SituacaoItemPlanoTratamento.PLANEJADO) {
                    throw new ResourceUnprocessableEntityException("Só o item planejado passa a em andamento.");
                }
            }
            case ENCAMINHADO -> {
                if (atual == SituacaoItemPlanoTratamento.ENCAMINHADO) {
                    throw new ResourceUnprocessableEntityException("O item já foi encaminhado.");
                }
                if (dto.getSolicitacaoRegulacaoId() != null) {
                    SolicitacaoRegulacao s = solicitacaoRepository.findById(dto.getSolicitacaoRegulacaoId()).orElseThrow(() -> new ResourceNotFoundException(
                            "Não foi possível encontrar a solicitação de regulação " + dto.getSolicitacaoRegulacaoId() + " em nossos registros."));
                    if (!s.getPaciente().getUuid().equals(plano.getPaciente().getUuid())) {
                        throw new ResourceUnprocessableEntityException("A solicitação de regulação é de outro paciente.");
                    }
                    item.setSolicitacaoRegulacao(s);
                }
            }
            case CANCELADO -> {
                if (motivo == null) {
                    throw new ResourceBadRequestException("Informe o motivo do cancelamento do item.");
                }
            }
            default -> throw new ResourceBadRequestException("O item é concluído pelo procedimento realizado; aqui, a situação é EM_ANDAMENTO, "
                    + "ENCAMINHADO ou CANCELADO.");
        }
        item.setSituacao(dto.getSituacao());
        item.setMotivoSituacao(motivo);
        marcarAlteracao(item);
        itemRepository.save(item);
        ContextoAuditoria.detalhe("Item " + item.getProcedimentoPrevisto().getNome() + ": " + dto.getSituacao());
        return dto(plano);
    }

    // ── Execução pelo procedimento (chamada pelo ProcedimentoService) ──────────────────────────────

    /**
     * O procedimento registrado executa o item: do mesmo paciente, num plano aberto, com o item planejado ou em andamento,
     * e o mesmo procedimento do catálogo, dente e faces (422). Realizado, conclui o item; agendado, põe em andamento.
     */
    @Transactional
    public void executar(UUID itemId, Procedimento p, ComplementoOdontologico complemento) {
        ItemPlanoTratamento item = itemRepository.findById(itemId).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar o item do plano " + itemId + " em nossos registros."));
        PlanoTratamento plano = item.getPlano();
        if (!plano.getPaciente().getUuid().equals(p.getAtendimento().getPaciente().getUuid())) {
            throw new ResourceUnprocessableEntityException("O item é do plano de outro paciente.");
        }
        if (plano.getSituacao() != SituacaoPlanoTratamento.ABERTO) {
            throw new ResourceUnprocessableEntityException("O plano de tratamento está " + plano.getSituacao() + ".");
        }
        if (item.getSituacao() != SituacaoItemPlanoTratamento.PLANEJADO && item.getSituacao() != SituacaoItemPlanoTratamento.EM_ANDAMENTO) {
            throw new ResourceUnprocessableEntityException("O item está " + item.getSituacao() + ": só se executa o planejado ou em andamento.");
        }
        if (p.getStatus() == StatusProcedimento.CANCELADO) {
            throw new ResourceUnprocessableEntityException("Para executar o item, registre o procedimento realizado ou agendado.");
        }
        exigirMesmoItem(item, p, complemento);
        item.setProcedimento(p);
        situacaoPeloProcedimento(item, p);
        itemRepository.save(item);
    }

    /** Desfecho do procedimento agendado (ADR-0062): realizado conclui o item; cancelado o devolve a planejado. */
    @Transactional
    public void aoMudarStatus(Procedimento p) {
        itemRepository.findByProcedimento_Uuid(p.getUuid()).ifPresent(item -> {
            if (item.getPlano().getSituacao() == SituacaoPlanoTratamento.ABERTO) {
                situacaoPeloProcedimento(item, p);
                itemRepository.save(item);
            }
        });
    }

    /**
     * Retificação do procedimento: o item passa a apontar para a nova versão, que precisa continuar sendo o mesmo
     * procedimento, dente e faces (422), e a situação é recalculada por ela.
     */
    @Transactional
    public void aoRetificar(Procedimento original, Procedimento nova, ComplementoOdontologico complemento) {
        itemRepository.findByProcedimento_Uuid(original.getUuid()).ifPresent(item -> {
            exigirMesmoItem(item, nova, complemento);
            item.setProcedimento(nova);
            if (item.getPlano().getSituacao() == SituacaoPlanoTratamento.ABERTO) {
                situacaoPeloProcedimento(item, nova);
            }
            itemRepository.save(item);
        });
    }

    // ── Leitura ────────────────────────────────────────────────────────────────────────────────────

    /** Os planos do paciente, o mais recente primeiro, com as regras do prontuário (vínculo e leitura auditada). */
    @Transactional(readOnly = true)
    public List<PlanoTratamentoDto> planosDoPaciente(UUID pacienteId) {
        if (!pacienteRepository.existsById(pacienteId)) {
            throw new ResourceNotFoundException("Não foi possível encontrar um paciente com o id " + pacienteId + " em nossos registros.");
        }
        ContextoAuditoria.paciente(pacienteId);
        vinculoService.exigirVinculo(pacienteId);
        List<PlanoTratamento> planos = planoRepository.findByPaciente_UuidOrderByAbertoEmDesc(pacienteId);
        Map<UUID, List<ItemPlanoTratamento>> itens = itensDos(planos.stream().map(PlanoTratamento::getUuid).toList());
        return planos.stream().map(p -> dto(p, itens.getOrDefault(p.getUuid(), List.of()))).toList();
    }

    // ── Apoio ──────────────────────────────────────────────────────────────────────────────────────

    private PlanoTratamento planoAberto(UUID planoId) {
        PlanoTratamento plano = planoRepository.findById(planoId).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar o plano de tratamento " + planoId + " em nossos registros."));
        controleDeAcesso.exigir(PLANEJAR, plano.getUnidade());
        ContextoAuditoria.paciente(plano.getPaciente().getUuid());
        ContextoAuditoria.registro(plano.getUuid());
        if (plano.getSituacao() != SituacaoPlanoTratamento.ABERTO) {
            throw new ResourceUnprocessableEntityException("O plano de tratamento está " + plano.getSituacao() + ".");
        }
        return plano;
    }

    /** O item segue as regras do complemento da SB3a: o procedimento odontológico do catálogo, o dente e as faces dele. */
    private void novoItem(PlanoTratamento plano, ItemPlanoTratamentoRequestDto dto, int ordemPadrao) {
        ProcedimentoCatalogo previsto = catalogoService.ativo(dto.getProcedimentoCatalogoId());
        if (!previsto.odontologico()) {
            throw new ResourceUnprocessableEntityException("O procedimento " + previsto.getNome() + " não é odontológico.");
        }
        Set<FaceDente> faces = ProcedimentoService.validarComplemento(previsto, plano.getAtendimento(), dto.getDente(), dto.getFaces());
        ItemPlanoTratamento item = new ItemPlanoTratamento();
        item.setPlano(plano);
        item.setOrdem(dto.getOrdem() != null ? dto.getOrdem() : ordemPadrao);
        item.setProcedimentoPrevisto(previsto);
        item.setDente(dto.getDente());
        item.setFaces(new LinkedHashSet<>(faces));
        item.setObservacao(texto(dto.getObservacao()));
        item.setSituacao(SituacaoItemPlanoTratamento.PLANEJADO);
        item.setCriadoEm(Instant.now());
        item.setCriadoPorCpf(UsuarioAutenticado.cpf());
        itemRepository.save(item);
    }

    private static void exigirMesmoItem(ItemPlanoTratamento item, Procedimento p, ComplementoOdontologico complemento) {
        Integer dente = complemento != null ? complemento.getDente() : null;
        Set<FaceDente> faces = complemento != null ? complemento.getFaces() : Set.of();
        boolean mesmo = p.getCatalogo() != null && p.getCatalogo().getUuid().equals(item.getProcedimentoPrevisto().getUuid())
                && Objects.equals(dente, item.getDente()) && faces.equals(item.getFaces());
        if (!mesmo) {
            throw new ResourceUnprocessableEntityException("O procedimento não corresponde ao item do plano (" + item.getProcedimentoPrevisto().getNome()
                    + (item.getDente() != null ? ", dente " + item.getDente() : "") + "): use o mesmo procedimento, dente e faces.");
        }
    }

    private static void situacaoPeloProcedimento(ItemPlanoTratamento item, Procedimento p) {
        switch (p.getStatus()) {
            case REALIZADO -> item.setSituacao(SituacaoItemPlanoTratamento.CONCLUIDO);
            case AGENDADO -> item.setSituacao(SituacaoItemPlanoTratamento.EM_ANDAMENTO);
            case CANCELADO -> {
                item.setSituacao(SituacaoItemPlanoTratamento.PLANEJADO);
                item.setProcedimento(null);
            }
        }
        item.setMotivoSituacao(null);
        marcarAlteracao(item);
    }

    private static void marcarAlteracao(ItemPlanoTratamento item) {
        item.setSituacaoAlteradaEm(Instant.now());
        item.setSituacaoAlteradaPorCpf(UsuarioAutenticado.cpf());
    }

    private void encerrar(PlanoTratamento plano, SituacaoPlanoTratamento situacao, String motivo) {
        plano.setSituacao(situacao);
        plano.setEncerradoEm(Instant.now());
        plano.setEncerradoPorCpf(UsuarioAutenticado.cpf());
        plano.setMotivoEncerramento(motivo);
        planoRepository.save(plano);
        ContextoAuditoria.detalhe("Plano de tratamento " + situacao + (motivo != null ? ": " + motivo : ""));
    }

    private Map<UUID, List<ItemPlanoTratamento>> itensDos(Collection<UUID> planoIds) {
        if (planoIds.isEmpty()) {
            return Map.of();
        }
        return itemRepository.findDosPlanos(planoIds).stream().collect(Collectors.groupingBy(i -> i.getPlano().getUuid()));
    }

    private PlanoTratamentoDto dto(PlanoTratamento plano) {
        return dto(plano, itensDos(List.of(plano.getUuid())).getOrDefault(plano.getUuid(), List.of()));
    }

    private static PlanoTratamentoDto dto(PlanoTratamento p, List<ItemPlanoTratamento> itens) {
        return new PlanoTratamentoDto(p.getUuid(), p.getPaciente().getUuid(), p.getAtendimento().getUuid(),
                p.getAvaliacao() != null ? p.getAvaliacao().getUuid() : null, p.getUnidade().getNome(), p.getProfissional().getMatricula(),
                p.getProfissional().getNome(), p.getDiagnostico(), p.getObjetivo(), p.getPrioridade(), p.getSituacao(), p.getAbertoEm(),
                p.getEncerradoEm(), p.getMotivoEncerramento(), itens.stream().map(PlanoTratamentoService::item).toList());
    }

    private static PlanoTratamentoDto.Item item(ItemPlanoTratamento i) {
        ProcedimentoCatalogo c = i.getProcedimentoPrevisto();
        Procedimento p = i.getProcedimento();
        return new PlanoTratamentoDto.Item(i.getUuid(), i.getOrdem(), c.getUuid(), c.getNome(), c.getSigtap(), c.getAlcance(), c.isExigeDentista(),
                i.getDente(), i.getFaces().stream().sorted().toList(), i.getObservacao(), i.getSituacao(), p != null ? p.getUuid() : null,
                p != null ? p.getStatus() : null, i.getSolicitacaoRegulacao() != null ? i.getSolicitacaoRegulacao().getUuid() : null,
                i.getMotivoSituacao(), i.getSituacaoAlteradaEm());
    }

    private static String texto(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }
}
