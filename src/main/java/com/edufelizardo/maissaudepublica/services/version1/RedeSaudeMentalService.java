package com.edufelizardo.maissaudepublica.services.version1;

import com.edufelizardo.maissaudepublica.config.ContextoAuditoria;
import com.edufelizardo.maissaudepublica.config.UsuarioAutenticado;
import com.edufelizardo.maissaudepublica.exceptions.ResourceBadRequestException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceConflictException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceNotFoundException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceUnprocessableEntityException;
import com.edufelizardo.maissaudepublica.models.AcompanhamentoSaudeMental;
import com.edufelizardo.maissaudepublica.models.CompartilhamentoCuidado;
import com.edufelizardo.maissaudepublica.models.EncaminhamentoSaudeMental;
import com.edufelizardo.maissaudepublica.models.Equipe;
import com.edufelizardo.maissaudepublica.models.PessoaRedeApoio;
import com.edufelizardo.maissaudepublica.models.Profissional;
import com.edufelizardo.maissaudepublica.models.ReferenciaSaudeMental;
import com.edufelizardo.maissaudepublica.models.SolicitacaoRegulacao;
import com.edufelizardo.maissaudepublica.models.TransferenciaCuidado;
import com.edufelizardo.maissaudepublica.models.UnidadeDeSaude;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.AceiteTransferenciaRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.CompartilhamentoCuidadoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.EncaminhamentoSaudeMentalRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.EncerramentoAcompanhamentoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.MotivoSaudeMentalRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.SolicitacaoRegulacaoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.TransferenciaCuidadoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.AcompanhamentoSaudeMentalDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.SolicitacaoRegulacaoResponseDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.TransferenciaCuidadoDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.TransferenciasCuidadoDto;
import com.edufelizardo.maissaudepublica.models.enuns.MotivoSaidaAcompanhamento;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoTransferenciaCuidado;
import com.edufelizardo.maissaudepublica.repositories.CompartilhamentoCuidadoRepository;
import com.edufelizardo.maissaudepublica.repositories.EncaminhamentoSaudeMentalRepository;
import com.edufelizardo.maissaudepublica.repositories.EquipeRepository;
import com.edufelizardo.maissaudepublica.repositories.PessoaRedeApoioRepository;
import com.edufelizardo.maissaudepublica.repositories.SolicitacaoRegulacaoRepository;
import com.edufelizardo.maissaudepublica.repositories.TransferenciaCuidadoRepository;
import com.edufelizardo.maissaudepublica.repositories.UnidadeDeSaudeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Saúde Mental, fatia SM4 (ADR-0122): o caso na rede de atenção. Encaminha pela Regulação que já existe (ADRs 0087 a
 * 0089), compartilha o cuidado com outra equipe e transfere o cuidado para outra unidade em duas etapas (proposta e
 * aceite pelo destino). Não há regulação de saúde mental: a ligação fica no domínio, a Regulação não muda.
 */
@Service
public class RedeSaudeMentalService {

    @Autowired
    private SaudeMentalService saudeMentalService;

    @Autowired
    private SolicitacaoRegulacaoService regulacaoService;

    @Autowired
    private RedeLeitura leitura;

    @Autowired
    private EncaminhamentoSaudeMentalRepository encaminhamentoRepository;

    @Autowired
    private CompartilhamentoCuidadoRepository compartilhamentoRepository;

    @Autowired
    private TransferenciaCuidadoRepository transferenciaRepository;

    @Autowired
    private SolicitacaoRegulacaoRepository solicitacaoRepository;

    @Autowired
    private EquipeRepository equipeRepository;

    @Autowired
    private UnidadeDeSaudeRepository unidadeDeSaudeRepository;

    @Autowired
    private PessoaRedeApoioRepository redeApoioRepository;

    @Autowired
    private ControleDeAcesso controleDeAcesso;

    // ── Encaminhamento pela Regulação ─────────────────────────────────────────────────────────────

    /**
     * Encaminha o caso pela Regulação: cria a solicitação (com as regras e a permissão da Regulação, REGULACAO.SOLICITAR
     * na unidade do caso) ou vincula uma existente do mesmo paciente.
     */
    @Transactional
    public AcompanhamentoSaudeMentalDto encaminhar(UUID casoId, EncaminhamentoSaudeMentalRequestDto dto) {
        AcompanhamentoSaudeMental caso = saudeMentalService.casoParaEscrever(casoId);
        SolicitacaoRegulacao solicitacao;
        if (dto.getSolicitacaoId() != null) {
            solicitacao = solicitacaoRepository.findById(dto.getSolicitacaoId()).orElseThrow(() -> new ResourceNotFoundException(
                    "Não foi possível encontrar a solicitação de regulação " + dto.getSolicitacaoId() + " em nossos registros."));
            if (!solicitacao.getPaciente().getUuid().equals(caso.getPaciente().getUuid())) {
                throw new ResourceUnprocessableEntityException("A solicitação de regulação é de outro paciente.");
            }
            if (encaminhamentoRepository.existsByAcompanhamento_UuidAndSolicitacao_Uuid(caso.getUuid(), solicitacao.getUuid())) {
                throw new ResourceConflictException("Essa solicitação já está ligada ao caso.");
            }
        } else {
            if (dto.getProcedimentoId() == null || dto.getPrioridade() == null || texto(dto.getCid()) == null
                    || texto(dto.getJustificativa()) == null) {
                throw new ResourceBadRequestException("Para encaminhar, informe o procedimento, o CID-10, a justificativa e a prioridade.");
            }
            String matricula = texto(dto.getProfissionalMatricula());
            if (matricula == null) {
                Profissional p = saudeMentalService.profissionalDoUsuario();
                if (p == null) {
                    throw new ResourceBadRequestException("Informe a matrícula do profissional solicitante.");
                }
                matricula = p.getMatricula();
            }
            SolicitacaoRegulacaoRequestDto pedido = new SolicitacaoRegulacaoRequestDto();
            pedido.setPacienteId(caso.getPaciente().getUuid());
            pedido.setProcedimentoId(dto.getProcedimentoId());
            pedido.setUnidadeSolicitanteId(caso.getUnidade().getUuid());
            pedido.setProfissionalMatricula(matricula);
            pedido.setCid(dto.getCid());
            pedido.setJustificativa(dto.getJustificativa());
            pedido.setPrioridade(dto.getPrioridade());
            SolicitacaoRegulacaoResponseDto criada = regulacaoService.solicitar(pedido);
            solicitacao = solicitacaoRepository.findById(criada.getUuid()).orElseThrow();
        }
        EncaminhamentoSaudeMental e = new EncaminhamentoSaudeMental();
        e.setAcompanhamento(caso);
        e.setSolicitacao(solicitacao);
        e.setVinculadoEm(Instant.now());
        e.setRegistradoPorCpf(UsuarioAutenticado.cpf());
        encaminhamentoRepository.save(e);
        ContextoAuditoria.registro(caso.getUuid());
        ContextoAuditoria.detalhe("Encaminhamento pela Regulação: " + solicitacao.getUuid() + ".");
        return saudeMentalService.detalheLiberado(caso);
    }

    // ── Cuidado compartilhado ─────────────────────────────────────────────────────────────────────

    @Transactional
    public AcompanhamentoSaudeMentalDto compartilhar(UUID casoId, CompartilhamentoCuidadoRequestDto dto) {
        AcompanhamentoSaudeMental caso = saudeMentalService.casoParaEscrever(casoId);
        Equipe equipe = equipeRepository.findById(dto.getEquipeId()).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar uma equipe com o id " + dto.getEquipeId() + " em nossos registros."));
        if (!equipe.isAtiva()) {
            throw new ResourceUnprocessableEntityException("A equipe " + equipe.getNome() + " está inativa.");
        }
        ReferenciaSaudeMental r = saudeMentalService.referenciaVigente(caso);
        if (r != null && r.getEquipe().getUuid().equals(equipe.getUuid())) {
            throw new ResourceUnprocessableEntityException("A equipe " + equipe.getNome() + " já é a equipe de referência do caso.");
        }
        if (leitura.compartilhamentoVigente(caso.getUuid(), equipe.getUuid()) != null) {
            throw new ResourceConflictException("O cuidado já é compartilhado com a equipe " + equipe.getNome() + ".");
        }
        CompartilhamentoCuidado c = new CompartilhamentoCuidado();
        c.setAcompanhamento(caso);
        c.setEquipe(equipe);
        c.setObjetivo(dto.getObjetivo().trim());
        c.setInicio(LocalDate.now());
        c.setRegistradoPorCpf(UsuarioAutenticado.cpf());
        c = compartilhamentoRepository.save(c);
        ContextoAuditoria.registro(c.getUuid());
        ContextoAuditoria.detalhe("Cuidado compartilhado com " + equipe.getNome() + ".");
        return saudeMentalService.detalheLiberado(caso);
    }

    /** Encerra o compartilhamento: a equipe parceira perde o vínculo e o conteúdo do caso. */
    @Transactional
    public AcompanhamentoSaudeMentalDto encerrarCompartilhamento(UUID casoId, UUID compartilhamentoId, MotivoSaudeMentalRequestDto dto) {
        AcompanhamentoSaudeMental caso = saudeMentalService.casoParaEscrever(casoId);
        CompartilhamentoCuidado c = compartilhamentoRepository.findById(compartilhamentoId)
                .filter(x -> x.getAcompanhamento().getUuid().equals(caso.getUuid()))
                .orElseThrow(() -> new ResourceNotFoundException("Não foi possível encontrar o compartilhamento " + compartilhamentoId
                        + " neste caso."));
        if (c.getFim() != null) {
            throw new ResourceUnprocessableEntityException("Este compartilhamento já foi encerrado.");
        }
        c.setFim(LocalDate.now());
        c.setMotivoFim(dto.getMotivo().trim());
        compartilhamentoRepository.save(c);
        ContextoAuditoria.registro(c.getUuid());
        return saudeMentalService.detalheLiberado(caso);
    }

    // ── Transferência do cuidado ──────────────────────────────────────────────────────────────────

    /** A origem propõe a transferência, com o resumo de contrarreferência. Uma pendente por caso. */
    @Transactional
    public AcompanhamentoSaudeMentalDto propor(UUID casoId, TransferenciaCuidadoRequestDto dto) {
        AcompanhamentoSaudeMental caso = saudeMentalService.casoParaEscrever(casoId);
        UnidadeDeSaude destino = unidadeDeSaudeRepository.findById(dto.getUnidadeDestinoId()).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar uma unidade de saúde com o id " + dto.getUnidadeDestinoId() + " em nossos registros."));
        if (destino.getUuid().equals(caso.getUnidade().getUuid())) {
            throw new ResourceUnprocessableEntityException("O destino precisa ser outra unidade; para mudar a equipe, troque a referência.");
        }
        if (leitura.transferenciaPendente(caso.getUuid()) != null) {
            throw new ResourceConflictException("O caso já tem uma transferência do cuidado pendente.");
        }
        TransferenciaCuidado t = new TransferenciaCuidado();
        t.setAcompanhamento(caso);
        t.setUnidadeDestino(destino);
        t.setResumo(dto.getResumo().trim());
        t.setPropostaEm(Instant.now());
        t.setPropostaPorCpf(UsuarioAutenticado.cpf());
        t.setSituacao(SituacaoTransferenciaCuidado.PROPOSTA);
        t = transferenciaRepository.save(t);
        ContextoAuditoria.registro(t.getUuid());
        ContextoAuditoria.detalhe("Transferência do cuidado proposta para " + destino.getNome() + ".");
        return saudeMentalService.detalheLiberado(caso);
    }

    /**
     * O destino aceita: o caso de origem sai com o motivo "transferência" (fechando o programa lá), o caso novo abre no
     * destino ligado ao anterior, com a nova referência e o programa inscrito lá, e a rede de apoio vigente é copiada.
     * O PTS não é copiado: o destino elabora o seu a partir do resumo.
     */
    @Transactional
    public AcompanhamentoSaudeMentalDto aceitar(UUID transferenciaId, AceiteTransferenciaRequestDto dto) {
        TransferenciaCuidado t = pendente(transferenciaId);
        controleDeAcesso.exigir(SaudeMentalService.ACOMPANHAR, t.getUnidadeDestino());
        AcompanhamentoSaudeMental origem = t.getAcompanhamento();
        ContextoAuditoria.paciente(origem.getPaciente().getUuid());
        saudeMentalService.encerrarCaso(origem, new EncerramentoAcompanhamentoRequestDto(MotivoSaidaAcompanhamento.TRANSFERENCIA,
                corte("Transferido para " + t.getUnidadeDestino().getNome() + ". " + t.getResumo()), null));
        AcompanhamentoSaudeMental novo = saudeMentalService.abrirCaso(origem.getPaciente(), t.getUnidadeDestino(), dto.getEquipeId(),
                dto.getTecnicoMatricula(), LocalDate.now(), origem);
        for (PessoaRedeApoio p : redeApoioRepository.findDoAcompanhamento(origem.getUuid())) {
            if (p.vigente()) {
                PessoaRedeApoio copia = new PessoaRedeApoio(null, novo, p.getNome(), p.getPapel(), p.getVinculo(), p.getTelefone(),
                        p.getObservacao(), p.getInicio(), null, null, UsuarioAutenticado.cpf());
                redeApoioRepository.save(copia);
            }
        }
        t.setSituacao(SituacaoTransferenciaCuidado.ACEITA);
        t.setDecididaEm(Instant.now());
        t.setDecididaPorCpf(UsuarioAutenticado.cpf());
        t.setCasoDestino(novo);
        transferenciaRepository.save(t);
        ContextoAuditoria.registro(novo.getUuid());
        ContextoAuditoria.detalhe("Transferência do cuidado aceita: caso " + origem.getUuid() + " → " + novo.getUuid() + ".");
        return saudeMentalService.detalheLiberado(novo);
    }

    /** O destino recusa, com motivo: o caso continua na origem. */
    @Transactional
    public TransferenciaCuidadoDto recusar(UUID transferenciaId, MotivoSaudeMentalRequestDto dto) {
        TransferenciaCuidado t = pendente(transferenciaId);
        controleDeAcesso.exigir(SaudeMentalService.ACOMPANHAR, t.getUnidadeDestino());
        return decidir(t, SituacaoTransferenciaCuidado.RECUSADA, dto.getMotivo());
    }

    /** A origem desiste enquanto a proposta está pendente. */
    @Transactional
    public TransferenciaCuidadoDto cancelar(UUID transferenciaId, MotivoSaudeMentalRequestDto dto) {
        TransferenciaCuidado t = pendente(transferenciaId);
        saudeMentalService.casoParaEscrever(t.getAcompanhamento().getUuid());
        return decidir(t, SituacaoTransferenciaCuidado.CANCELADA, dto.getMotivo());
    }

    /** As transferências pendentes do escopo: as que chegam às minhas unidades e as que saem delas. */
    @Transactional(readOnly = true)
    public TransferenciasCuidadoDto pendentes(UUID unidadeId) {
        Optional<Set<UUID>> visiveis = controleDeAcesso.unidadesVisiveis(SaudeMentalService.ACOMPANHAR);
        List<TransferenciaCuidado> todas = transferenciaRepository.findPendentes();
        List<TransferenciaCuidadoDto> recebidas = todas.stream()
                .filter(t -> visiveis.map(ids -> ids.contains(t.getUnidadeDestino().getUuid())).orElse(true))
                .filter(t -> unidadeId == null || t.getUnidadeDestino().getUuid().equals(unidadeId))
                .map(RedeLeitura::dto).toList();
        List<TransferenciaCuidadoDto> enviadas = todas.stream()
                .filter(t -> visiveis.map(ids -> ids.contains(t.getAcompanhamento().getUnidade().getUuid())).orElse(true))
                .filter(t -> unidadeId == null || t.getAcompanhamento().getUnidade().getUuid().equals(unidadeId))
                .map(RedeLeitura::dto).toList();
        return new TransferenciasCuidadoDto(recebidas, enviadas);
    }

    // ── Apoio ──────────────────────────────────────────────────────────────────────────────────────

    private TransferenciaCuidado pendente(UUID uuid) {
        TransferenciaCuidado t = transferenciaRepository.findById(uuid).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar a transferência do cuidado " + uuid + " em nossos registros."));
        ContextoAuditoria.registro(t.getUuid());
        if (t.getSituacao() != SituacaoTransferenciaCuidado.PROPOSTA) {
            throw new ResourceUnprocessableEntityException("Esta transferência já foi " + t.getSituacao().name().toLowerCase() + ".");
        }
        return t;
    }

    private TransferenciaCuidadoDto decidir(TransferenciaCuidado t, SituacaoTransferenciaCuidado situacao, String motivo) {
        t.setSituacao(situacao);
        t.setDecididaEm(Instant.now());
        t.setDecididaPorCpf(UsuarioAutenticado.cpf());
        t.setMotivoDecisao(motivo.trim());
        transferenciaRepository.save(t);
        ContextoAuditoria.paciente(t.getAcompanhamento().getPaciente().getUuid());
        ContextoAuditoria.detalhe("Transferência do cuidado " + situacao.name().toLowerCase() + ".");
        return RedeLeitura.dto(t);
    }

    private static String corte(String s) {
        return s.length() > 500 ? s.substring(0, 500) : s;
    }

    private static String texto(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }
}
