package com.edufelizardo.maissaudepublica.services.version1;

import com.edufelizardo.maissaudepublica.config.ContextoAuditoria;
import com.edufelizardo.maissaudepublica.config.UsuarioAutenticado;
import com.edufelizardo.maissaudepublica.exceptions.ResourceBadRequestException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceConflictException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceForbiddenException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceNotFoundException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceUnprocessableEntityException;
import com.edufelizardo.maissaudepublica.models.EventoCrise;
import com.edufelizardo.maissaudepublica.models.EventoUrgencia;
import com.edufelizardo.maissaudepublica.models.Leito;
import com.edufelizardo.maissaudepublica.models.OcorrenciaUrgencia;
import com.edufelizardo.maissaudepublica.models.Paciente;
import com.edufelizardo.maissaudepublica.models.Profissional;
import com.edufelizardo.maissaudepublica.models.UnidadeDeSaude;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.AcionamentoSamuRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.DeslocamentoUrgenciaRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.DestinoUrgenciaRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.EncerramentoUrgenciaRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.MotivoUrgenciaRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.OcorrenciaUrgenciaRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.RecebimentoUrgenciaRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.ReclassificacaoUrgenciaRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.RegulacaoUrgenciaRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.VinculoPacienteUrgenciaRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.CentralUrgenciaDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.DestinoUrgenciaDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.OcorrenciaUrgenciaDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.PainelUrgenciaDto;
import com.edufelizardo.maissaudepublica.models.enuns.DecisaoRegulacaoUrgencia;
import com.edufelizardo.maissaudepublica.models.enuns.DesfechoOcorrenciaUrgencia;
import com.edufelizardo.maissaudepublica.models.enuns.OrigemOcorrenciaUrgencia;
import com.edufelizardo.maissaudepublica.models.enuns.PrioridadeRegulacao;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoLeito;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoOcorrenciaUrgencia;
import com.edufelizardo.maissaudepublica.models.enuns.TipoEventoUrgencia;
import com.edufelizardo.maissaudepublica.repositories.EventoUrgenciaRepository;
import com.edufelizardo.maissaudepublica.repositories.LeitoRepository;
import com.edufelizardo.maissaudepublica.repositories.OcorrenciaUrgenciaRepository;
import com.edufelizardo.maissaudepublica.repositories.PacienteRepository;
import com.edufelizardo.maissaudepublica.repositories.ProfissionalRepository;
import com.edufelizardo.maissaudepublica.repositories.UnidadeCapacidadeAssistencialRepository;
import com.edufelizardo.maissaudepublica.repositories.UnidadeDeSaudeRepository;
import com.edufelizardo.maissaudepublica.repositories.UnidadePerfilOperacionalRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Regulação de urgência (ADR-0176). O atendente da central ({@value #RECEBER}) abre a ocorrência; o médico regulador
 * ({@value #REGULAR}) classifica a gravidade, decide — orientação, envio de recurso, encaminhamento ou transferência — e
 * indica o destino, com vaga zero quando precisa; o rádio-operador registra os deslocamentos; a unidade de destino recebe
 * o paciente ({@value #ATENDIMENTO}). Cada passo grava um {@link EventoUrgencia}, e os tempos de resposta saem deles.
 *
 * <p>A central é uma unidade com a capacidade {@value #CAPACIDADE_CENTRAL} e o destino, uma com o perfil {@value #PERFIL_DESTINO}
 * (o domínio pergunta ao modelo operacional, MO4). As viaturas, equipes e bases vêm com o Transporte (#16): até lá, o
 * recurso é o tipo e um texto.
 */
@Service
public class OcorrenciaUrgenciaService {

    public static final String CONSULTAR = "URGENCIA.CONSULTAR";
    public static final String RECEBER = "URGENCIA.RECEBER";
    public static final String REGULAR = "URGENCIA.REGULAR";
    public static final String ATENDIMENTO = "ATENDIMENTO.GERENCIAR";
    public static final String CAPACIDADE_CENTRAL = "REGULACAO_MEDICA_URGENCIA";
    public static final String PERFIL_DESTINO = "URGENCIA";

    static final Set<SituacaoOcorrenciaUrgencia> ABERTAS = EnumSet.of(SituacaoOcorrenciaUrgencia.RECEBIDA,
            SituacaoOcorrenciaUrgencia.REGULADA, SituacaoOcorrenciaUrgencia.EM_ATENDIMENTO);
    private static final Set<TipoEventoUrgencia> DESLOCAMENTOS = EnumSet.of(TipoEventoUrgencia.ENVIO_DE_RECURSO,
            TipoEventoUrgencia.CHEGADA_AO_LOCAL, TipoEventoUrgencia.SAIDA_DO_LOCAL, TipoEventoUrgencia.CHEGADA_AO_DESTINO);
    /** Os desfechos que o regulador registra; orientado e recebido no destino têm caminho próprio. */
    private static final Set<DesfechoOcorrenciaUrgencia> NO_ENCERRAMENTO = EnumSet.of(DesfechoOcorrenciaUrgencia.ATENDIDO_NO_LOCAL,
            DesfechoOcorrenciaUrgencia.OBITO_NO_LOCAL, DesfechoOcorrenciaUrgencia.RECUSA, DesfechoOcorrenciaUrgencia.SEM_OCORRENCIA,
            DesfechoOcorrenciaUrgencia.DESISTENCIA);
    /** Desfechos que pressupõem a equipe no local. */
    private static final Set<DesfechoOcorrenciaUrgencia> COM_EQUIPE_NO_LOCAL = EnumSet.of(DesfechoOcorrenciaUrgencia.ATENDIDO_NO_LOCAL,
            DesfechoOcorrenciaUrgencia.OBITO_NO_LOCAL);

    /** Aguardando regulação primeiro; depois a gravidade (vermelho primeiro) e quem chamou antes. */
    static final Comparator<OcorrenciaUrgencia> ORDEM_DO_PAINEL = Comparator
            .comparing((OcorrenciaUrgencia o) -> o.getPrioridade() == null ? -1 : o.getPrioridade().ordinal())
            .thenComparing(OcorrenciaUrgencia::getRecebidaEm);

    @Autowired private OcorrenciaUrgenciaRepository repository;
    @Autowired private EventoUrgenciaRepository eventoRepository;
    @Autowired private UnidadeDeSaudeRepository unidadeRepository;
    @Autowired private PacienteRepository pacienteRepository;
    @Autowired private ProfissionalRepository profissionalRepository;
    @Autowired private UnidadeCapacidadeAssistencialRepository capacidadeRepository;
    @Autowired private UnidadePerfilOperacionalRepository perfilRepository;
    @Autowired private LeitoRepository leitoRepository;
    @Autowired private ControleDeAcesso controleDeAcesso;

    // ── Central: abertura ─────────────────────────────────────────────────────────────────────────

    @Transactional
    public OcorrenciaUrgenciaDto abrir(OcorrenciaUrgenciaRequestDto dto) {
        UnidadeDeSaude central = central(dto.getCentralId());
        controleDeAcesso.exigir(RECEBER, central);
        if (dto.getOrigem() == OrigemOcorrenciaUrgencia.CRISE_SAUDE_MENTAL) {
            throw new ResourceBadRequestException("O acionamento pela crise em saúde mental é feito na própria crise.");
        }
        UnidadeDeSaude solicitante = null;
        if (dto.getOrigem() == OrigemOcorrenciaUrgencia.UNIDADE_DE_SAUDE) {
            if (dto.getUnidadeSolicitanteId() == null) {
                throw new ResourceBadRequestException("Informe a unidade que pede a urgência.");
            }
            solicitante = unidade(dto.getUnidadeSolicitanteId());
        } else if (texto(dto.getSolicitanteTelefone()) == null) {
            throw new ResourceBadRequestException("Informe o telefone de quem chamou, para o retorno da central.");
        }
        if ((dto.getLatitude() == null) != (dto.getLongitude() == null)) {
            throw new ResourceBadRequestException("Informe latitude e longitude juntas.");
        }
        if (dto.getLatitude() != null && (Math.abs(dto.getLatitude()) > 90 || Math.abs(dto.getLongitude()) > 180)) {
            throw new ResourceBadRequestException("Latitude ou longitude fora do intervalo.");
        }
        OcorrenciaUrgencia o = nova(central, dto.getOrigem());
        o.setSolicitanteNome(texto(dto.getSolicitanteNome()));
        o.setSolicitanteTelefone(texto(dto.getSolicitanteTelefone()));
        o.setUnidadeSolicitante(solicitante);
        o.setPaciente(dto.getPacienteId() != null ? pacienteAtivo(dto.getPacienteId()) : null);
        o.setDescricaoPaciente(texto(dto.getDescricaoPaciente()));
        o.setLocalTipo(dto.getLocalTipo());
        o.setEndereco(texto(dto.getEndereco()));
        o.setReferencia(texto(dto.getReferencia()));
        o.setLatitude(dto.getLatitude());
        o.setLongitude(dto.getLongitude());
        o.setQueixa(dto.getQueixa().trim());
        Profissional atendente = texto(dto.getAtendenteMatricula()) != null ? profissional(dto.getAtendenteMatricula().trim()) : null;
        o.setAtendente(atendente);
        repository.save(o);
        registrar(o, TipoEventoUrgencia.RECEBIMENTO, solicitante != null ? "Pedido de " + solicitante.getNome() + "." : "Chamado recebido.",
                atendente, o.getRecebidaEm());
        return detalhe(o);
    }

    /** Pela crise em saúde mental (fecha a SM3): o acesso é conferido na crise; uma ocorrência aberta por crise. */
    @Transactional
    public OcorrenciaUrgenciaDto abrirPelaCrise(EventoCrise crise, AcionamentoSamuRequestDto dto, Profissional profissional) {
        UnidadeDeSaude central = central(dto.getCentralId());
        if (repository.existsByCrise_UuidAndSituacaoIn(crise.getUuid(), ABERTAS)) {
            throw new ResourceConflictException("Já há uma ocorrência de urgência aberta para esta crise.");
        }
        OcorrenciaUrgencia o = nova(central, OrigemOcorrenciaUrgencia.CRISE_SAUDE_MENTAL);
        o.setCrise(crise);
        o.setPaciente(crise.getPaciente());
        o.setUnidadeSolicitante(crise.getUnidade());
        o.setSolicitanteNome(profissional != null ? profissional.getNome() : null);
        o.setSolicitanteTelefone(texto(dto.getTelefone()));
        o.setEndereco(texto(dto.getEndereco()) != null ? texto(dto.getEndereco()) : crise.getLocalOcorrencia());
        o.setQueixa(dto.getMotivo().trim());
        repository.save(o);
        registrar(o, TipoEventoUrgencia.RECEBIMENTO, "Acionada pela crise em saúde mental em " + crise.getUnidade().getNome() + ".",
                profissional, o.getRecebidaEm());
        return detalhe(o);
    }

    private OcorrenciaUrgencia nova(UnidadeDeSaude central, OrigemOcorrenciaUrgencia origem) {
        OcorrenciaUrgencia o = new OcorrenciaUrgencia();
        Instant agora = Instant.now();
        o.setProtocolo(LocalDate.now().getYear() + "-" + String.format("%06d", repository.proximoNumero()));
        o.setCentral(central);
        o.setOrigem(origem);
        o.setRecebidaEm(agora);
        o.setRecebidaPorCpf(UsuarioAutenticado.cpf());
        o.setSituacao(SituacaoOcorrenciaUrgencia.RECEBIDA);
        return o;
    }

    // ── Médico regulador ──────────────────────────────────────────────────────────────────────────

    @Transactional
    public OcorrenciaUrgenciaDto regular(UUID uuid, RegulacaoUrgenciaRequestDto dto) {
        OcorrenciaUrgencia o = travar(uuid);
        controleDeAcesso.exigir(REGULAR, o.getCentral());
        exigirSituacao(o, "regular", SituacaoOcorrenciaUrgencia.RECEBIDA);
        Profissional regulador = profissional(dto.getProfissionalMatricula());
        DecisaoRegulacaoUrgencia decisao = dto.getDecisao();
        boolean comDestino = dto.getDestinoId() != null;
        switch (decisao) {
            case ORIENTACAO -> {
                if (comDestino || dto.getTipoRecurso() != null) {
                    throw new ResourceBadRequestException("A orientação por telefone não tem recurso nem destino.");
                }
            }
            case ENVIO_DE_RECURSO -> {
                if (dto.getTipoRecurso() == null) {
                    throw new ResourceBadRequestException("Informe o tipo de recurso enviado.");
                }
            }
            case ENCAMINHAMENTO -> {
                if (!comDestino) {
                    throw new ResourceBadRequestException("Informe a unidade para onde o paciente vai.");
                }
                if (dto.getTipoRecurso() != null) {
                    throw new ResourceBadRequestException("No encaminhamento o paciente vai por meios próprios; com recurso, a decisão é envio de recurso.");
                }
            }
            case TRANSFERENCIA -> {
                if (o.getUnidadeSolicitante() == null) {
                    throw new ResourceUnprocessableEntityException("A transferência é de unidade para unidade; este chamado não veio de uma unidade.");
                }
                if (!comDestino) {
                    throw new ResourceBadRequestException("Informe a unidade de destino da transferência.");
                }
            }
        }
        o.setPrioridade(dto.getPrioridade());
        o.setDecisao(decisao);
        o.setConduta(dto.getConduta().trim());
        o.setTipoRecurso(dto.getTipoRecurso());
        o.setRecursoIdentificacao(dto.getTipoRecurso() != null ? texto(dto.getRecursoIdentificacao()) : null);
        o.setRegulador(regulador);
        o.setReguladaEm(Instant.now());
        String destino = comDestino ? definirDestino(o, dto.getDestinoId(), Boolean.TRUE.equals(dto.getVagaZero()), dto.getJustificativaVagaZero()) : null;
        if (!comDestino && Boolean.TRUE.equals(dto.getVagaZero())) {
            throw new ResourceBadRequestException("A vaga zero é para um destino; informe a unidade.");
        }
        if (decisao == DecisaoRegulacaoUrgencia.ORIENTACAO) {
            o.setSituacao(SituacaoOcorrenciaUrgencia.CONCLUIDA);
            o.setDesfecho(DesfechoOcorrenciaUrgencia.ORIENTADO);
            o.setEncerradaEm(o.getReguladaEm());
        } else {
            o.setSituacao(SituacaoOcorrenciaUrgencia.REGULADA);
        }
        StringBuilder t = new StringBuilder("Gravidade " + dto.getPrioridade() + ". Decisão: " + rotulo(decisao) + ".");
        if (o.getTipoRecurso() != null) {
            t.append(" Recurso ").append(o.getTipoRecurso()).append(o.getRecursoIdentificacao() != null ? " (" + o.getRecursoIdentificacao() + ")" : "").append(".");
        }
        if (destino != null) {
            t.append(" ").append(destino);
        }
        registrar(o, TipoEventoUrgencia.REGULACAO, t.toString(), regulador, o.getReguladaEm());
        return detalhe(o);
    }

    /** Destino definido ou trocado depois, com a equipe no local; antes da chegada ao destino. */
    @Transactional
    public OcorrenciaUrgenciaDto destino(UUID uuid, DestinoUrgenciaRequestDto dto) {
        OcorrenciaUrgencia o = travar(uuid);
        controleDeAcesso.exigir(REGULAR, o.getCentral());
        exigirSituacao(o, "definir o destino de", SituacaoOcorrenciaUrgencia.REGULADA, SituacaoOcorrenciaUrgencia.EM_ATENDIMENTO);
        if (o.getDecisao() == DecisaoRegulacaoUrgencia.ORIENTACAO) {
            throw new ResourceUnprocessableEntityException("A ocorrência foi orientada por telefone.");
        }
        if (passos(o).contains(TipoEventoUrgencia.CHEGADA_AO_DESTINO)) {
            throw new ResourceUnprocessableEntityException("O paciente já chegou ao destino.");
        }
        Profissional regulador = profissional(dto.getProfissionalMatricula());
        String texto = definirDestino(o, dto.getDestinoId(), Boolean.TRUE.equals(dto.getVagaZero()), dto.getJustificativaVagaZero());
        registrar(o, TipoEventoUrgencia.DESTINO, texto, regulador, Instant.now());
        return detalhe(o);
    }

    @Transactional
    public OcorrenciaUrgenciaDto reclassificar(UUID uuid, ReclassificacaoUrgenciaRequestDto dto) {
        OcorrenciaUrgencia o = travar(uuid);
        controleDeAcesso.exigir(REGULAR, o.getCentral());
        exigirSituacao(o, "reclassificar", SituacaoOcorrenciaUrgencia.REGULADA, SituacaoOcorrenciaUrgencia.EM_ATENDIMENTO);
        Profissional regulador = profissional(dto.getProfissionalMatricula());
        PrioridadeRegulacao anterior = o.getPrioridade();
        if (anterior == dto.getPrioridade()) {
            throw new ResourceUnprocessableEntityException("A ocorrência já está com a gravidade " + anterior + ".");
        }
        o.setPrioridade(dto.getPrioridade());
        registrar(o, TipoEventoUrgencia.RECLASSIFICACAO, "De " + anterior + " para " + dto.getPrioridade() + ": " + dto.getMotivo().trim(),
                regulador, Instant.now());
        return detalhe(o);
    }

    @Transactional
    public OcorrenciaUrgenciaDto encerrar(UUID uuid, EncerramentoUrgenciaRequestDto dto) {
        OcorrenciaUrgencia o = travar(uuid);
        controleDeAcesso.exigir(REGULAR, o.getCentral());
        exigirSituacao(o, "encerrar", ABERTAS.toArray(SituacaoOcorrenciaUrgencia[]::new));
        DesfechoOcorrenciaUrgencia desfecho = dto.getDesfecho();
        if (!NO_ENCERRAMENTO.contains(desfecho)) {
            throw new ResourceUnprocessableEntityException(desfecho == DesfechoOcorrenciaUrgencia.ORIENTADO
                    ? "A orientação é uma decisão da regulação." : "O recebimento é registrado pela unidade de destino.");
        }
        if (o.getSituacao() == SituacaoOcorrenciaUrgencia.RECEBIDA && desfecho != DesfechoOcorrenciaUrgencia.SEM_OCORRENCIA
                && desfecho != DesfechoOcorrenciaUrgencia.DESISTENCIA) {
            throw new ResourceUnprocessableEntityException("Antes da regulação, só se encerra como sem ocorrência ou desistência.");
        }
        if (COM_EQUIPE_NO_LOCAL.contains(desfecho) && !passos(o).contains(TipoEventoUrgencia.CHEGADA_AO_LOCAL)) {
            throw new ResourceUnprocessableEntityException("Registre antes a chegada da equipe ao local.");
        }
        Profissional regulador = profissional(dto.getProfissionalMatricula());
        Instant agora = Instant.now();
        o.setDesfecho(desfecho);
        o.setEncerradaEm(agora);
        o.setSituacao(SituacaoOcorrenciaUrgencia.CONCLUIDA);
        registrar(o, TipoEventoUrgencia.ENCERRAMENTO, rotulo(desfecho) + ": " + dto.getObservacao().trim(), regulador, agora);
        return detalhe(o);
    }

    // ── Central: cancelamento, deslocamento e paciente ───────────────────────────────────────────

    /** Duplicada ou aberta por engano: antes do envio do recurso. */
    @Transactional
    public OcorrenciaUrgenciaDto cancelar(UUID uuid, MotivoUrgenciaRequestDto dto) {
        OcorrenciaUrgencia o = travar(uuid);
        exigirNaCentral(o, RECEBER, REGULAR);
        exigirSituacao(o, "cancelar", SituacaoOcorrenciaUrgencia.RECEBIDA, SituacaoOcorrenciaUrgencia.REGULADA);
        if (passos(o).contains(TipoEventoUrgencia.ENVIO_DE_RECURSO)) {
            throw new ResourceUnprocessableEntityException("O recurso já saiu; encerre com o desfecho.");
        }
        Instant agora = Instant.now();
        o.setSituacao(SituacaoOcorrenciaUrgencia.CANCELADA);
        o.setEncerradaEm(agora);
        registrar(o, TipoEventoUrgencia.CANCELAMENTO, dto.getMotivo().trim(), null, agora);
        return detalhe(o);
    }

    /** Envio → chegada ao local → saída do local → chegada ao destino, um de cada, na ordem. */
    @Transactional
    public OcorrenciaUrgenciaDto deslocamento(UUID uuid, DeslocamentoUrgenciaRequestDto dto) {
        OcorrenciaUrgencia o = travar(uuid);
        controleDeAcesso.exigir(RECEBER, o.getCentral());
        TipoEventoUrgencia tipo = dto.getTipo();
        if (!DESLOCAMENTOS.contains(tipo)) {
            throw new ResourceBadRequestException("O deslocamento é envio, chegada ao local, saída do local ou chegada ao destino.");
        }
        exigirSituacao(o, "registrar deslocamento em", SituacaoOcorrenciaUrgencia.REGULADA, SituacaoOcorrenciaUrgencia.EM_ATENDIMENTO);
        if (!o.comRecurso()) {
            throw new ResourceUnprocessableEntityException("A regulação não enviou recurso para esta ocorrência.");
        }
        List<EventoUrgencia> eventos = eventoRepository.findDaOcorrencia(o.getUuid());
        Set<TipoEventoUrgencia> feitos = eventos.stream().map(EventoUrgencia::getTipo).collect(Collectors.toSet());
        if (feitos.contains(tipo)) {
            throw new ResourceConflictException("Este passo do deslocamento já foi registrado.");
        }
        TipoEventoUrgencia antes = switch (tipo) {
            case CHEGADA_AO_LOCAL -> TipoEventoUrgencia.ENVIO_DE_RECURSO;
            case SAIDA_DO_LOCAL -> TipoEventoUrgencia.CHEGADA_AO_LOCAL;
            case CHEGADA_AO_DESTINO -> TipoEventoUrgencia.SAIDA_DO_LOCAL;
            default -> null;
        };
        if (antes != null && !feitos.contains(antes)) {
            throw new ResourceUnprocessableEntityException("Registre antes: " + rotulo(antes) + ".");
        }
        if (tipo == TipoEventoUrgencia.CHEGADA_AO_DESTINO && o.getDestino() == null) {
            throw new ResourceUnprocessableEntityException("A regulação ainda não definiu o destino.");
        }
        Instant ultimo = eventos.stream().map(EventoUrgencia::getOcorridoEm).max(Comparator.naturalOrder()).orElse(o.getRecebidaEm());
        Instant quando = dto.getOcorridoEm() != null ? dto.getOcorridoEm() : Instant.now();
        if (quando.isBefore(ultimo) || quando.isAfter(Instant.now().plusSeconds(60))) {
            throw new ResourceBadRequestException("A hora precisa ser entre o último passo registrado e agora.");
        }
        if (tipo == TipoEventoUrgencia.ENVIO_DE_RECURSO) {
            if (texto(dto.getRecursoIdentificacao()) != null) {
                o.setRecursoIdentificacao(dto.getRecursoIdentificacao().trim());
            }
            o.setSituacao(SituacaoOcorrenciaUrgencia.EM_ATENDIMENTO);
        }
        String t = rotulo(tipo) + (tipo == TipoEventoUrgencia.ENVIO_DE_RECURSO && o.getRecursoIdentificacao() != null
                ? " (" + o.getRecursoIdentificacao() + ")" : "") + (texto(dto.getObservacao()) != null ? ": " + dto.getObservacao().trim() : ".");
        registrar(o, tipo, t, null, quando);
        return detalhe(o);
    }

    /** Identifica o paciente: a central ou a unidade de destino, que costuma saber quem é. */
    @Transactional
    public OcorrenciaUrgenciaDto vincularPaciente(UUID uuid, VinculoPacienteUrgenciaRequestDto dto) {
        OcorrenciaUrgencia o = travar(uuid);
        if (!naCentral(o, RECEBER, REGULAR) && !(o.getDestino() != null && visivel(controleDeAcesso.unidadesVisiveis(ATENDIMENTO), o.getDestino()))) {
            throw new ResourceForbiddenException("Seu acesso não inclui identificar o paciente desta ocorrência.");
        }
        if (o.getPaciente() != null) {
            throw new ResourceConflictException("A ocorrência já tem paciente.");
        }
        Paciente p = pacienteAtivo(dto.getPacienteId());
        o.setPaciente(p);
        registrar(o, TipoEventoUrgencia.VINCULO_DO_PACIENTE, "Paciente identificado.", null, Instant.now());
        return detalhe(o);
    }

    // ── Unidade de destino ────────────────────────────────────────────────────────────────────────

    @Transactional
    public OcorrenciaUrgenciaDto receber(UUID uuid, RecebimentoUrgenciaRequestDto dto) {
        OcorrenciaUrgencia o = travar(uuid);
        if (o.getDestino() == null) {
            throw new ResourceUnprocessableEntityException("A ocorrência não tem unidade de destino.");
        }
        controleDeAcesso.exigir(ATENDIMENTO, o.getDestino());
        exigirSituacao(o, "receber", SituacaoOcorrenciaUrgencia.REGULADA, SituacaoOcorrenciaUrgencia.EM_ATENDIMENTO);
        Set<TipoEventoUrgencia> feitos = passos(o);
        if (o.comRecurso() && !feitos.contains(TipoEventoUrgencia.SAIDA_DO_LOCAL)) {
            throw new ResourceUnprocessableEntityException("A central ainda não registrou a saída do recurso com o paciente.");
        }
        Profissional quem = texto(dto.getProfissionalMatricula()) != null ? profissional(dto.getProfissionalMatricula().trim()) : null;
        Instant agora = Instant.now();
        if (o.comRecurso() && !feitos.contains(TipoEventoUrgencia.CHEGADA_AO_DESTINO)) {
            registrar(o, TipoEventoUrgencia.CHEGADA_AO_DESTINO, rotulo(TipoEventoUrgencia.CHEGADA_AO_DESTINO) + ", com o recebimento.", null, agora);
        }
        o.setRecebidaNoDestinoEm(agora);
        o.setDesfecho(DesfechoOcorrenciaUrgencia.RECEBIDO_NO_DESTINO);
        o.setEncerradaEm(agora);
        o.setSituacao(SituacaoOcorrenciaUrgencia.CONCLUIDA);
        registrar(o, TipoEventoUrgencia.RECEBIMENTO_NO_DESTINO, "Recebido em " + o.getDestino().getNome()
                + (texto(dto.getObservacao()) != null ? ": " + dto.getObservacao().trim() : "."), quem, agora);
        return detalhe(o);
    }

    // ── Leitura ───────────────────────────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public PainelUrgenciaDto painel(UUID centralId) {
        controleDeAcesso.exigirAlguma(CONSULTAR, RECEBER, REGULAR);
        Visao v = visao();
        List<OcorrenciaUrgencia> abertas = repository.findNasSituacoes(ABERTAS).stream()
                .filter(o -> centralId == null || o.getCentral().getUuid().equals(centralId))
                .filter(v::visivel).sorted(ORDEM_DO_PAINEL).toList();
        Instant inicioDoDia = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant();
        List<OcorrenciaUrgencia> hoje = repository.findRecebidasDesde(inicioDoDia).stream()
                .filter(o -> centralId == null || o.getCentral().getUuid().equals(centralId))
                .filter(v::visivel).toList();
        Map<UUID, List<EventoUrgencia>> eventos = eventos(Stream.concat(abertas.stream(), hoje.stream()).map(OcorrenciaUrgencia::getUuid).distinct().toList());
        Map<String, Long> porPrioridade = new LinkedHashMap<>();
        for (PrioridadeRegulacao p : PrioridadeRegulacao.values()) {
            porPrioridade.put(p.name(), abertas.stream().filter(o -> o.getPrioridade() == p).count());
        }
        List<OcorrenciaUrgenciaDto> dtos = abertas.stream().map(o -> dto(o, v.clinico(o), eventos.getOrDefault(o.getUuid(), List.of()), false)).toList();
        List<OcorrenciaUrgenciaDto> doDia = hoje.stream().map(o -> dto(o, false, eventos.getOrDefault(o.getUuid(), List.of()), false)).toList();
        return new PainelUrgenciaDto(dtos,
                abertas.stream().filter(o -> o.getSituacao() == SituacaoOcorrenciaUrgencia.RECEBIDA).count(),
                abertas.stream().filter(o -> o.getSituacao() == SituacaoOcorrenciaUrgencia.EM_ATENDIMENTO).count(),
                porPrioridade, hoje.size(),
                hoje.stream().filter(o -> o.getSituacao() == SituacaoOcorrenciaUrgencia.CONCLUIDA).count(),
                media(doDia.stream().map(OcorrenciaUrgenciaDto::getMinutosAteRegulacao)),
                media(doDia.stream().map(OcorrenciaUrgenciaDto::getMinutosAteEnvio)));
    }

    /** Histórico dos últimos dias (padrão 7, até 90), mais recente primeiro. */
    @Transactional(readOnly = true)
    public List<OcorrenciaUrgenciaDto> listar(UUID centralId, SituacaoOcorrenciaUrgencia situacao, UUID destinoId, Integer dias) {
        int d = dias == null ? 7 : Math.max(1, Math.min(dias, 90));
        Visao v = visao();
        List<OcorrenciaUrgencia> lista = repository.findRecebidasDesde(Instant.now().minus(Duration.ofDays(d))).stream()
                .filter(o -> centralId == null || o.getCentral().getUuid().equals(centralId))
                .filter(o -> situacao == null || o.getSituacao() == situacao)
                .filter(o -> destinoId == null || (o.getDestino() != null && o.getDestino().getUuid().equals(destinoId)))
                .filter(v::visivel).toList();
        Map<UUID, List<EventoUrgencia>> eventos = eventos(lista.stream().map(OcorrenciaUrgencia::getUuid).toList());
        return lista.stream().map(o -> dto(o, v.clinico(o), eventos.getOrDefault(o.getUuid(), List.of()), false)).toList();
    }

    @Transactional(readOnly = true)
    public OcorrenciaUrgenciaDto buscar(UUID uuid) {
        OcorrenciaUrgencia o = repository.findById(uuid).orElseThrow(() -> naoEncontrada(uuid));
        ContextoAuditoria.unidade(o.getCentral());
        Visao v = visao();
        if (!v.visivel(o)) {
            throw new ResourceForbiddenException("Esta ocorrência é de uma central fora do seu acesso.");
        }
        return v.clinico(o) ? detalhe(o) : dto(o, false, eventoRepository.findDaOcorrencia(o.getUuid()), true);
    }

    /** As unidades com perfil Urgência vigente, com os leitos livres e o que já vai para elas. */
    @Transactional(readOnly = true)
    public List<DestinoUrgenciaDto> destinos() {
        controleDeAcesso.exigirAlguma(CONSULTAR, RECEBER, REGULAR);
        List<UnidadeDeSaude> unidades = perfilRepository.findVigentesComPerfil().stream()
                .filter(a -> PERFIL_DESTINO.equals(a.getPerfil().getCodigo()))
                .map(a -> a.getUnidade()).filter(UnidadeDeSaude::isAtivo).distinct().toList();
        Set<UUID> ids = unidades.stream().map(UnidadeDeSaude::getUuid).collect(Collectors.toSet());
        Map<UUID, List<Leito>> leitos = leitoRepository.findAllByOrderByIdentificacaoAsc().stream()
                .filter(l -> l.isAtivo() && ids.contains(l.getUnidade().getUuid()))
                .collect(Collectors.groupingBy(l -> l.getUnidade().getUuid()));
        Map<UUID, Long> aCaminho = repository.findNasSituacoes(ABERTAS).stream().filter(o -> o.getDestino() != null)
                .collect(Collectors.groupingBy(o -> o.getDestino().getUuid(), Collectors.counting()));
        return unidades.stream()
                .map(u -> {
                    List<Leito> l = leitos.getOrDefault(u.getUuid(), List.of());
                    return new DestinoUrgenciaDto(u.getUuid(), u.getNome(), u.getTipo(),
                            l.stream().filter(x -> x.getSituacao() == SituacaoLeito.LIVRE).count(), l.size(), aCaminho.getOrDefault(u.getUuid(), 0L));
                })
                .sorted(Comparator.comparing(DestinoUrgenciaDto::getNome, String.CASE_INSENSITIVE_ORDER)).toList();
    }

    @Transactional(readOnly = true)
    public List<CentralUrgenciaDto> centrais() {
        return capacidadeRepository.findVigentesComCapacidade().stream()
                .filter(a -> CAPACIDADE_CENTRAL.equals(a.getCapacidade().getCodigo()))
                .map(a -> a.getUnidade()).filter(UnidadeDeSaude::isAtivo).distinct()
                .map(u -> new CentralUrgenciaDto(u.getUuid(), u.getNome()))
                .sorted(Comparator.comparing(CentralUrgenciaDto::getNome, String.CASE_INSENSITIVE_ORDER)).toList();
    }

    /** Para o prontuário, que já conferiu o vínculo assistencial (ADR-0076). */
    @Transactional(readOnly = true)
    public List<OcorrenciaUrgenciaDto> doPaciente(UUID pacienteId) {
        List<OcorrenciaUrgencia> lista = repository.findDoPaciente(pacienteId);
        Map<UUID, List<EventoUrgencia>> eventos = eventos(lista.stream().map(OcorrenciaUrgencia::getUuid).toList());
        return lista.stream().map(o -> dto(o, true, eventos.getOrDefault(o.getUuid(), List.of()), false)).toList();
    }

    // ── Apoio ─────────────────────────────────────────────────────────────────────────────────────

    /** Quem vê a ocorrência e quem vê o dado clínico dela, a partir das unidades visíveis do usuário. */
    private record Visao(Optional<Set<UUID>> naCentral, Optional<Set<UUID>> clinicoNaCentral, Optional<Set<UUID>> naUnidade) {
        boolean visivel(OcorrenciaUrgencia o) {
            return OcorrenciaUrgenciaService.visivel(naCentral, o.getCentral()) || OcorrenciaUrgenciaService.visivel(naUnidade, o.getDestino())
                    || OcorrenciaUrgenciaService.visivel(naUnidade, o.getUnidadeSolicitante());
        }

        /** A central (atendente e regulador) e a unidade de destino; a solicitante e quem só consulta veem o andamento. */
        boolean clinico(OcorrenciaUrgencia o) {
            return OcorrenciaUrgenciaService.visivel(clinicoNaCentral, o.getCentral()) || OcorrenciaUrgenciaService.visivel(naUnidade, o.getDestino());
        }
    }

    private Visao visao() {
        return new Visao(controleDeAcesso.unidadesVisiveis(CONSULTAR, RECEBER, REGULAR), controleDeAcesso.unidadesVisiveis(RECEBER, REGULAR),
                controleDeAcesso.unidadesVisiveis(ATENDIMENTO));
    }

    private static boolean visivel(Optional<Set<UUID>> visiveis, UnidadeDeSaude u) {
        return u != null && visiveis.map(s -> s.contains(u.getUuid())).orElse(true);
    }

    private boolean naCentral(OcorrenciaUrgencia o, String... permissoes) {
        return visivel(controleDeAcesso.unidadesVisiveis(permissoes), o.getCentral());
    }

    private void exigirNaCentral(OcorrenciaUrgencia o, String... permissoes) {
        ContextoAuditoria.unidade(o.getCentral());
        if (!naCentral(o, permissoes)) {
            throw new ResourceForbiddenException("Seu acesso não inclui " + String.join(" ou ", permissoes) + " na central " + o.getCentral().getNome() + ".");
        }
    }

    private String definirDestino(OcorrenciaUrgencia o, UUID destinoId, boolean vagaZero, String justificativa) {
        UnidadeDeSaude destino = unidade(destinoId);
        if (!destino.isAtivo()) {
            throw new ResourceUnprocessableEntityException("A unidade " + destino.getNome() + " está inativa.");
        }
        if (o.getUnidadeSolicitante() != null && o.getUnidadeSolicitante().getUuid().equals(destino.getUuid())) {
            throw new ResourceUnprocessableEntityException("O destino precisa ser outra unidade, não a que pediu.");
        }
        if (perfilRepository.findDaUnidade(destino.getUuid()).stream().noneMatch(a -> a.getFim() == null && PERFIL_DESTINO.equals(a.getPerfil().getCodigo()))) {
            throw new ResourceUnprocessableEntityException(destino.getNome() + " não tem o perfil Urgência (modelo operacional).");
        }
        if (vagaZero && texto(justificativa) == null) {
            throw new ResourceBadRequestException("Justifique a vaga zero.");
        }
        o.setDestino(destino);
        o.setVagaZero(vagaZero);
        o.setJustificativaVagaZero(vagaZero ? justificativa.trim() : null);
        return "Destino " + destino.getNome() + (vagaZero ? ", vaga zero: " + justificativa.trim() + "." : ".");
    }

    private OcorrenciaUrgenciaDto detalhe(OcorrenciaUrgencia o) {
        if (o.getPaciente() != null) {
            ContextoAuditoria.paciente(o.getPaciente().getUuid());
        }
        ContextoAuditoria.registro(o.getUuid());
        return dto(o, true, eventoRepository.findDaOcorrencia(o.getUuid()), true);
    }

    private Map<UUID, List<EventoUrgencia>> eventos(List<UUID> ids) {
        if (ids.isEmpty()) {
            return Map.of();
        }
        return eventoRepository.findDasOcorrencias(ids).stream().collect(Collectors.groupingBy(e -> e.getOcorrencia().getUuid()));
    }

    static OcorrenciaUrgenciaDto dto(OcorrenciaUrgencia o, boolean clinico, List<EventoUrgencia> eventos, boolean comEventos) {
        OcorrenciaUrgenciaDto d = new OcorrenciaUrgenciaDto();
        d.setUuid(o.getUuid());
        d.setProtocolo(o.getProtocolo());
        d.setCentralId(o.getCentral().getUuid());
        d.setCentralNome(o.getCentral().getNome());
        d.setOrigem(o.getOrigem());
        d.setSituacao(o.getSituacao());
        d.setPrioridade(o.getPrioridade());
        d.setClinico(clinico);
        if (o.getUnidadeSolicitante() != null) {
            d.setUnidadeSolicitanteId(o.getUnidadeSolicitante().getUuid());
            d.setUnidadeSolicitanteNome(o.getUnidadeSolicitante().getNome());
        }
        d.setRecebidaEm(o.getRecebidaEm());
        d.setReguladaEm(o.getReguladaEm());
        d.setDecisao(o.getDecisao());
        d.setTipoRecurso(o.getTipoRecurso());
        d.setRecursoIdentificacao(o.getRecursoIdentificacao());
        if (o.getDestino() != null) {
            d.setDestinoId(o.getDestino().getUuid());
            d.setDestinoNome(o.getDestino().getNome());
        }
        d.setVagaZero(o.isVagaZero());
        d.setRecebidaNoDestinoEm(o.getRecebidaNoDestinoEm());
        d.setDesfecho(o.getDesfecho());
        d.setEncerradaEm(o.getEncerradaEm());
        d.setLocalTipo(o.getLocalTipo());
        if (clinico) {
            d.setSolicitanteNome(o.getSolicitanteNome());
            d.setSolicitanteTelefone(o.getSolicitanteTelefone());
            d.setCriseId(o.getCrise() != null ? o.getCrise().getUuid() : null);
            if (o.getPaciente() != null) {
                d.setPacienteId(o.getPaciente().getUuid());
                d.setPacienteNome(o.getPaciente().getNome());
            }
            d.setDescricaoPaciente(o.getDescricaoPaciente());
            d.setEndereco(o.getEndereco());
            d.setReferencia(o.getReferencia());
            d.setLatitude(o.getLatitude());
            d.setLongitude(o.getLongitude());
            d.setQueixa(o.getQueixa());
            d.setReguladorNome(o.getRegulador() != null ? o.getRegulador().getNome() : null);
            d.setConduta(o.getConduta());
            d.setJustificativaVagaZero(o.getJustificativaVagaZero());
        }
        Map<TipoEventoUrgencia, Instant> primeiro = new LinkedHashMap<>();
        eventos.stream().sorted(Comparator.comparing(EventoUrgencia::getOcorridoEm))
                .forEach(e -> primeiro.putIfAbsent(e.getTipo(), e.getOcorridoEm()));
        d.setUltimoDeslocamento(eventos.stream().filter(e -> DESLOCAMENTOS.contains(e.getTipo()))
                .max(Comparator.comparing(EventoUrgencia::getOcorridoEm)).map(EventoUrgencia::getTipo).orElse(null));
        d.setMinutosAteRegulacao(minutos(o.getRecebidaEm(), primeiro.get(TipoEventoUrgencia.REGULACAO)));
        d.setMinutosAteEnvio(minutos(o.getRecebidaEm(), primeiro.get(TipoEventoUrgencia.ENVIO_DE_RECURSO)));
        d.setMinutosEnvioAteLocal(minutos(primeiro.get(TipoEventoUrgencia.ENVIO_DE_RECURSO), primeiro.get(TipoEventoUrgencia.CHEGADA_AO_LOCAL)));
        d.setMinutosTotal(minutos(o.getRecebidaEm(), o.getEncerradaEm()));
        if (comEventos) {
            List<OcorrenciaUrgenciaDto.Evento> lista = new ArrayList<>();
            for (EventoUrgencia e : eventos) {
                lista.add(new OcorrenciaUrgenciaDto.Evento(e.getUuid(), e.getTipo(), e.getSituacaoResultante(), e.getPrioridade(),
                        clinico ? e.getTexto() : null, e.getProfissional() != null ? e.getProfissional().getNome() : null, e.getOcorridoEm()));
            }
            d.setEventos(lista);
        }
        return d;
    }

    private static Long minutos(Instant de, Instant ate) {
        return de == null || ate == null ? null : Duration.between(de, ate).toMinutes();
    }

    private static Double media(Stream<Long> valores) {
        List<Long> l = valores.filter(Objects::nonNull).toList();
        return l.isEmpty() ? null : Math.round(l.stream().mapToLong(Long::longValue).average().orElse(0) * 10) / 10.0;
    }

    private Set<TipoEventoUrgencia> passos(OcorrenciaUrgencia o) {
        return eventoRepository.findDaOcorrencia(o.getUuid()).stream().map(EventoUrgencia::getTipo).collect(Collectors.toSet());
    }

    private void registrar(OcorrenciaUrgencia o, TipoEventoUrgencia tipo, String texto, Profissional profissional, Instant quando) {
        repository.save(o);
        eventoRepository.save(new EventoUrgencia(o, tipo, texto, profissional, quando, UsuarioAutenticado.cpf()));
        ContextoAuditoria.detalhe("Ocorrência de urgência " + o.getProtocolo() + ": " + tipo + ".");
    }

    private OcorrenciaUrgencia travar(UUID uuid) {
        OcorrenciaUrgencia o = repository.findByIdParaAtualizar(uuid).orElseThrow(() -> naoEncontrada(uuid));
        ContextoAuditoria.registro(o.getUuid());
        if (o.getPaciente() != null) {
            ContextoAuditoria.paciente(o.getPaciente().getUuid());
        }
        return o;
    }

    private static void exigirSituacao(OcorrenciaUrgencia o, String acao, SituacaoOcorrenciaUrgencia... permitidas) {
        if (Stream.of(permitidas).noneMatch(s -> s == o.getSituacao())) {
            throw new ResourceUnprocessableEntityException("Não é possível " + acao + " uma ocorrência " + o.getSituacao() + ".");
        }
    }

    private UnidadeDeSaude central(UUID id) {
        if (id == null) {
            throw new ResourceBadRequestException("Informe a central de regulação.");
        }
        UnidadeDeSaude u = unidade(id);
        if (!capacidadeRepository.existsByUnidade_UuidAndCapacidade_CodigoAndFimIsNull(u.getUuid(), CAPACIDADE_CENTRAL)) {
            throw new ResourceUnprocessableEntityException(u.getNome() + " não tem a capacidade de regulação médica de urgência (modelo operacional).");
        }
        return u;
    }

    private UnidadeDeSaude unidade(UUID id) {
        return unidadeRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar uma unidade de saúde com o id " + id + " em nossos registros."));
    }

    private Paciente pacienteAtivo(UUID id) {
        Paciente p = pacienteRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar um paciente com o id " + id + " em nossos registros."));
        if (!p.isAtivo()) {
            throw new ResourceUnprocessableEntityException("O paciente está inativo.");
        }
        ContextoAuditoria.paciente(p.getUuid());
        return p;
    }

    private Profissional profissional(String matricula) {
        return profissionalRepository.findByMatricula(matricula).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar um profissional com a matrícula " + matricula + " em nossos registros."));
    }

    private ResourceNotFoundException naoEncontrada(UUID uuid) {
        return new ResourceNotFoundException("Não foi possível encontrar uma ocorrência de urgência com o id " + uuid + " em nossos registros.");
    }

    private static String texto(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }

    static String rotulo(DecisaoRegulacaoUrgencia d) {
        return switch (d) {
            case ORIENTACAO -> "orientação por telefone";
            case ENVIO_DE_RECURSO -> "envio de recurso";
            case ENCAMINHAMENTO -> "encaminhamento";
            case TRANSFERENCIA -> "transferência";
        };
    }

    static String rotulo(TipoEventoUrgencia t) {
        return switch (t) {
            case ENVIO_DE_RECURSO -> "Recurso enviado";
            case CHEGADA_AO_LOCAL -> "Chegada ao local";
            case SAIDA_DO_LOCAL -> "Saída do local";
            case CHEGADA_AO_DESTINO -> "Chegada ao destino";
            default -> t.name();
        };
    }

    static String rotulo(DesfechoOcorrenciaUrgencia d) {
        return switch (d) {
            case ORIENTADO -> "Orientado";
            case ATENDIDO_NO_LOCAL -> "Atendido no local";
            case RECEBIDO_NO_DESTINO -> "Recebido no destino";
            case OBITO_NO_LOCAL -> "Óbito no local";
            case RECUSA -> "Recusa";
            case SEM_OCORRENCIA -> "Sem ocorrência";
            case DESISTENCIA -> "Desistência";
        };
    }
}
