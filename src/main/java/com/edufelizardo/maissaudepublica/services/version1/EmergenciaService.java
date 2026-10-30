package com.edufelizardo.maissaudepublica.services.version1;

import com.edufelizardo.maissaudepublica.config.ContextoAuditoria;
import com.edufelizardo.maissaudepublica.config.UsuarioAutenticado;
import com.edufelizardo.maissaudepublica.exceptions.ResourceBadRequestException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceConflictException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceNotFoundException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceUnprocessableEntityException;
import com.edufelizardo.maissaudepublica.models.AvaliacaoImpacto;
import com.edufelizardo.maissaudepublica.models.Emergencia;
import com.edufelizardo.maissaudepublica.models.EventoEmergencial;
import com.edufelizardo.maissaudepublica.models.Leito;
import com.edufelizardo.maissaudepublica.models.Microarea;
import com.edufelizardo.maissaudepublica.models.MudancaNivelEmergencia;
import com.edufelizardo.maissaudepublica.models.NivelEmergencia;
import com.edufelizardo.maissaudepublica.models.Profissional;
import com.edufelizardo.maissaudepublica.models.Surto;
import com.edufelizardo.maissaudepublica.models.TipoEmergencia;
import com.edufelizardo.maissaudepublica.models.UnidadeDeSaude;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.AvaliacaoImpactoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.ConversaoEmergenciaRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.EncerramentoEmergenciaRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.EventoEmergencialRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.MudancaNivelEmergenciaRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.NivelEmergenciaRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.ReferenciasLegaisEmergenciaRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.SituacaoEventoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.TerritorioEmergenciaRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.TipoEmergenciaRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.EmergenciaDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.EventoEmergencialDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.NivelEmergenciaDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.SugestaoImpactoDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.TipoEmergenciaDto;
import com.edufelizardo.maissaudepublica.models.enuns.PerfilTipoEmergencia;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoEmergencia;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoEventoEmergencial;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoLeito;
import com.edufelizardo.maissaudepublica.repositories.AvaliacaoImpactoRepository;
import com.edufelizardo.maissaudepublica.repositories.EmergenciaRepository;
import com.edufelizardo.maissaudepublica.repositories.EventoEmergencialRepository;
import com.edufelizardo.maissaudepublica.repositories.LeitoRepository;
import com.edufelizardo.maissaudepublica.repositories.MembroNucleoRepository;
import com.edufelizardo.maissaudepublica.repositories.MicroareaRepository;
import com.edufelizardo.maissaudepublica.repositories.MudancaNivelEmergenciaRepository;
import com.edufelizardo.maissaudepublica.repositories.NivelEmergenciaRepository;
import com.edufelizardo.maissaudepublica.repositories.ProfissionalRepository;
import com.edufelizardo.maissaudepublica.repositories.SurtoRepository;
import com.edufelizardo.maissaudepublica.repositories.TipoEmergenciaRepository;
import com.edufelizardo.maissaudepublica.repositories.UnidadeDeSaudeRepository;
import org.locationtech.jts.geom.Geometry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Emergências e Desastres, fatia E1 (ADR-0156, desenho na ADR-0113): os catálogos de tipo e nível, o evento
 * emergencial, a emergência (nível com histórico, território afetado, referências legais, encerramento) e a avaliação
 * de impacto, com as sugestões que vêm dos donos (Território, Organização, Leitos). Coordenação, não posse: nada aqui
 * muda leito, escala ou estoque. Sem dado de paciente.
 */
@Service
public class EmergenciaService {

    public static final String REGISTRAR = "EMERGENCIA.REGISTRAR";
    public static final String ATIVAR = "EMERGENCIA.ATIVAR";
    public static final String CATALOGO = "CONTINGENCIA.GERENCIAR";

    @Autowired private TipoEmergenciaRepository tipoRepository;
    @Autowired private NivelEmergenciaRepository nivelRepository;
    @Autowired private EventoEmergencialRepository eventoRepository;
    @Autowired private EmergenciaRepository emergenciaRepository;
    @Autowired private MudancaNivelEmergenciaRepository mudancaRepository;
    @Autowired private AvaliacaoImpactoRepository avaliacaoRepository;
    @Autowired private SurtoRepository surtoRepository;
    @Autowired private MicroareaRepository microareaRepository;
    @Autowired private MembroNucleoRepository membroNucleoRepository;
    @Autowired private UnidadeDeSaudeRepository unidadeRepository;
    @Autowired private LeitoRepository leitoRepository;
    @Autowired private ProfissionalRepository profissionalRepository;
    @Autowired private ControleDeAcesso controleDeAcesso;
    @Autowired private AtivacaoPlanoService ativacao;
    @Autowired private CoordenacaoEmergenciaService coordenacao;
    @Autowired private com.edufelizardo.maissaudepublica.repositories.DesmobilizacaoRepository desmobilizacaoRepository;
    @Autowired private EventoEspecializadoService especializado;

    // ── Catálogos ──────────────────────────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<TipoEmergenciaDto> tipos() {
        return tipoRepository.findAllByOrderByNomeAsc().stream().map(EmergenciaService::dto).toList();
    }

    /** Código repetido, 409. */
    @Transactional
    public TipoEmergenciaDto criarTipo(TipoEmergenciaRequestDto dto) {
        controleDeAcesso.exigirAlguma(CATALOGO);
        String codigo = dto.getCodigo().trim().toUpperCase();
        if (tipoRepository.existsByCodigoIgnoreCase(codigo)) {
            throw new ResourceConflictException("Já existe o tipo de emergência " + codigo + ".");
        }
        TipoEmergencia t = new TipoEmergencia();
        t.setCodigo(codigo);
        t.setPerfil(PerfilTipoEmergencia.GERAL);
        preencher(t, dto);
        t.setAtivo(dto.getAtivo() == null || dto.getAtivo());
        tipoRepository.save(t);
        ContextoAuditoria.registro(t.getUuid());
        return dto(t);
    }

    /** O código não muda (422): é a chave estável do tipo. Sem {@code ativo}, mantém. */
    @Transactional
    public TipoEmergenciaDto atualizarTipo(UUID uuid, TipoEmergenciaRequestDto dto) {
        controleDeAcesso.exigirAlguma(CATALOGO);
        TipoEmergencia t = tipo(uuid);
        if (!t.getCodigo().equalsIgnoreCase(dto.getCodigo().trim())) {
            throw new ResourceUnprocessableEntityException("O código do tipo não muda: cadastre outro tipo.");
        }
        preencher(t, dto);
        if (dto.getAtivo() != null) {
            t.setAtivo(dto.getAtivo());
        }
        tipoRepository.save(t);
        ContextoAuditoria.registro(t.getUuid());
        return dto(t);
    }

    private static void preencher(TipoEmergencia t, TipoEmergenciaRequestDto dto) {
        t.setNome(dto.getNome().trim());
        t.setCategoria(dto.getCategoria());
        t.setCobrade(texto(dto.getCobrade()));
        if (dto.getPerfil() != null) {
            t.setPerfil(dto.getPerfil());
        }
    }

    @Transactional(readOnly = true)
    public List<NivelEmergenciaDto> niveis() {
        return nivelRepository.findAllByOrderByOrdemAsc().stream().map(EmergenciaService::dto).toList();
    }

    /** Ordem repetida, 409. */
    @Transactional
    public NivelEmergenciaDto criarNivel(NivelEmergenciaRequestDto dto) {
        controleDeAcesso.exigirAlguma(CATALOGO);
        if (nivelRepository.existsByOrdem(dto.getOrdem())) {
            throw new ResourceConflictException("Já existe o nível de ordem " + dto.getOrdem() + ".");
        }
        NivelEmergencia n = new NivelEmergencia();
        n.setOrdem(dto.getOrdem());
        n.setNome(dto.getNome().trim());
        n.setDescricao(texto(dto.getDescricao()));
        n.setAtivo(dto.getAtivo() == null || dto.getAtivo());
        nivelRepository.save(n);
        ContextoAuditoria.registro(n.getUuid());
        return dto(n);
    }

    /** A ordem não muda (422); o nome, a descrição e a situação, sim. */
    @Transactional
    public NivelEmergenciaDto atualizarNivel(UUID uuid, NivelEmergenciaRequestDto dto) {
        controleDeAcesso.exigirAlguma(CATALOGO);
        NivelEmergencia n = nivel(uuid);
        if (n.getOrdem() != dto.getOrdem()) {
            throw new ResourceUnprocessableEntityException("A ordem do nível não muda.");
        }
        n.setNome(dto.getNome().trim());
        n.setDescricao(texto(dto.getDescricao()));
        if (dto.getAtivo() != null) {
            n.setAtivo(dto.getAtivo());
        }
        nivelRepository.save(n);
        ContextoAuditoria.registro(n.getUuid());
        return dto(n);
    }

    // ── Evento emergencial ─────────────────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<EventoEmergencialDto> eventos(SituacaoEventoEmergencial situacao) {
        Map<UUID, UUID> emergencias = new HashMap<>();
        emergenciaRepository.findTodas().forEach(e -> emergencias.put(e.getEvento().getUuid(), e.getUuid()));
        return eventoRepository.findTodos().stream().filter(e -> situacao == null || e.getSituacao() == situacao)
                .map(e -> dto(e, emergencias.get(e.getUuid()))).toList();
    }

    @Transactional(readOnly = true)
    public EventoEmergencialDto evento(UUID uuid) {
        EventoEmergencial e = buscarEvento(uuid);
        return dto(e, emergenciaRepository.findByEvento_Uuid(uuid).map(Emergencia::getUuid).orElse(null));
    }

    /** Tipo inativo, 422; no futuro ou área inválida, 400; o surto precisa existir (404). */
    @Transactional
    public EventoEmergencialDto registrarEvento(EventoEmergencialRequestDto dto) {
        controleDeAcesso.exigirAlguma(REGISTRAR);
        TipoEmergencia tipo = tipo(dto.getTipoId());
        if (!tipo.isAtivo()) {
            throw new ResourceUnprocessableEntityException("O tipo " + tipo.getNome() + " está inativo.");
        }
        Instant agora = Instant.now();
        Instant ocorrido = dto.getOcorridoEm() != null ? dto.getOcorridoEm() : agora;
        if (ocorrido.isAfter(agora.plusSeconds(60))) {
            throw new ResourceBadRequestException("O evento não pode ser no futuro.");
        }
        Surto surto = null;
        if (dto.getSurtoId() != null) {
            surto = surtoRepository.findById(dto.getSurtoId()).orElseThrow(() -> new ResourceNotFoundException(
                    "Não foi possível encontrar o surto " + dto.getSurtoId() + " em nossos registros."));
        }
        EventoEmergencial e = new EventoEmergencial();
        e.setTipo(tipo);
        e.setOcorridoEm(ocorrido);
        e.setLocal(texto(dto.getLocal()));
        e.setGeometria(GeometriaTerritorial.normalizar(dto.getGeometria()));
        e.setFonte(dto.getFonte());
        e.setDescricao(dto.getDescricao().trim());
        e.setSurto(surto);
        e.setSituacao(SituacaoEventoEmergencial.REGISTRADO);
        e.setRegistradoEm(agora);
        e.setRegistradoPorCpf(UsuarioAutenticado.cpf());
        eventoRepository.save(e);
        ContextoAuditoria.registro(e.getUuid());
        return dto(e, null);
    }

    /** Monitorar ou descartar (com o motivo, 400): só do registrado ou monitorado (422). */
    @Transactional
    public EventoEmergencialDto mudarSituacaoDoEvento(UUID uuid, SituacaoEventoRequestDto dto) {
        controleDeAcesso.exigirAlguma(REGISTRAR);
        EventoEmergencial e = buscarEvento(uuid);
        if (!aberto(e)) {
            throw new ResourceUnprocessableEntityException("O evento já foi " + (e.getSituacao() == SituacaoEventoEmergencial.CONVERTIDO
                    ? "convertido em emergência." : "descartado."));
        }
        if (dto.getSituacao() != SituacaoEventoEmergencial.MONITORADO && dto.getSituacao() != SituacaoEventoEmergencial.DESCARTADO) {
            throw new ResourceBadRequestException("O evento passa a monitorado ou descartado; para emergência, converta.");
        }
        if (dto.getSituacao() == SituacaoEventoEmergencial.DESCARTADO && texto(dto.getMotivo()) == null) {
            throw new ResourceBadRequestException("Informe o motivo do descarte.");
        }
        e.setSituacao(dto.getSituacao());
        e.setMotivoDescarte(dto.getSituacao() == SituacaoEventoEmergencial.DESCARTADO ? dto.getMotivo().trim() : null);
        eventoRepository.save(e);
        ContextoAuditoria.registro(e.getUuid());
        ContextoAuditoria.detalhe("Evento emergencial " + dto.getSituacao() + ".");
        return dto(e, null);
    }

    // ── Emergência ─────────────────────────────────────────────────────────────────────────────────

    /**
     * Converte o evento em emergência, no nível inicial e com a justificativa (a primeira mudança de nível). Já convertido,
     * 409; descartado, 422. Sem território, a área do evento e as microáreas que ela atinge. Planejada (ADR-0160), só no
     * perfil evento de massa (422): a operação começa depois.
     */
    @Transactional
    public EmergenciaDto converter(UUID eventoId, ConversaoEmergenciaRequestDto dto) {
        controleDeAcesso.exigirAlguma(ATIVAR);
        EventoEmergencial evento = buscarEvento(eventoId);
        if (evento.getSituacao() == SituacaoEventoEmergencial.CONVERTIDO || emergenciaRepository.findByEvento_Uuid(eventoId).isPresent()) {
            throw new ResourceConflictException("Este evento já virou emergência.");
        }
        if (evento.getSituacao() == SituacaoEventoEmergencial.DESCARTADO) {
            throw new ResourceUnprocessableEntityException("O evento foi descartado; registre outro se a situação mudou.");
        }
        boolean planejada = Boolean.TRUE.equals(dto.getPlanejada());
        if (planejada && evento.getTipo().getPerfil() != PerfilTipoEmergencia.EVENTO_DE_MASSA) {
            throw new ResourceUnprocessableEntityException("Só o evento de massa é planejado; os outros tipos já começam ativos.");
        }
        NivelEmergencia nivel = nivelAtivo(dto.getNivelId());
        Emergencia em = new Emergencia();
        em.setEvento(evento);
        em.setNome(dto.getNome().trim());
        em.setTipo(evento.getTipo());
        em.setNivel(nivel);
        em.setSituacao(planejada ? SituacaoEmergencia.PLANEJADA : SituacaoEmergencia.ATIVA);
        em.setInicio(Instant.now());
        em.setResponsavel(responsavel(dto.getResponsavelMatricula()));
        em.setRegistradoPorCpf(UsuarioAutenticado.cpf());
        String geometria = dto.getGeometria() != null ? dto.getGeometria() : evento.getGeometria();
        aplicarTerritorio(em, geometria, dto.getMicroareaIds());
        emergenciaRepository.save(em);
        registrarMudanca(em, null, nivel, dto.getJustificativa());
        evento.setSituacao(SituacaoEventoEmergencial.CONVERTIDO);
        eventoRepository.save(evento);
        ContextoAuditoria.registro(em.getUuid());
        ContextoAuditoria.detalhe(planejada ? "Evento de massa planejado, previsto no " + nivel.getNome() + "."
                : "Emergência ativada no " + nivel.getNome() + ".");
        return detalhe(em);
    }

    @Transactional(readOnly = true)
    public List<EmergenciaDto> emergencias(SituacaoEmergencia situacao) {
        List<Emergencia> lista = emergenciaRepository.findTodas().stream().filter(e -> situacao == null || e.getSituacao() == situacao).toList();
        return lista.stream().map(e -> {
            EmergenciaDto d = resumo(e, avaliacaoRepository.findDaEmergencia(e.getUuid()).stream().findFirst().orElse(null));
            d.setAcoesAtrasadas(coordenacao.atrasadas(e));
            d.setEventoMassa(especializado.eventoMassa(e));
            return d;
        }).toList();
    }

    @Transactional(readOnly = true)
    public EmergenciaDto emergencia(UUID uuid) {
        return detalhe(buscarEmergencia(uuid));
    }

    /** Ativa (422), outro nível ativo (422), com a justificativa; o histórico guarda o anterior. */
    @Transactional
    public EmergenciaDto mudarNivel(UUID uuid, MudancaNivelEmergenciaRequestDto dto) {
        controleDeAcesso.exigirAlguma(ATIVAR);
        Emergencia em = ativa(uuid);
        NivelEmergencia novo = nivelAtivo(dto.getNivelId());
        if (novo.getUuid().equals(em.getNivel().getUuid())) {
            throw new ResourceUnprocessableEntityException("A emergência já está no " + novo.getNome() + ".");
        }
        NivelEmergencia anterior = em.getNivel();
        em.setNivel(novo);
        emergenciaRepository.save(em);
        registrarMudanca(em, anterior, novo, dto.getJustificativa());
        ativacao.registrarNivel(em, novo, dto.getJustificativa());
        ContextoAuditoria.registro(em.getUuid());
        ContextoAuditoria.detalhe((novo.getOrdem() > anterior.getOrdem() ? "Nível elevado: " : "Nível reduzido: ") + anterior.getNome() + " → "
                + novo.getNome() + ".");
        return detalhe(em);
    }

    /** O polígono e as microáreas; sem a lista, as que o polígono atinge. Área inválida, 400. */
    @Transactional
    public EmergenciaDto definirTerritorio(UUID uuid, TerritorioEmergenciaRequestDto dto) {
        controleDeAcesso.exigirAlguma(REGISTRAR, ATIVAR);
        Emergencia em = ativaOuPlanejada(uuid);
        aplicarTerritorio(em, dto.getGeometria(), dto.getMicroareaIds());
        emergenciaRepository.save(em);
        ContextoAuditoria.registro(em.getUuid());
        return detalhe(em);
    }

    /** A ESPIN e o decreto como registro (o sistema não declara): cada um completo (número e data; o decreto, o tipo) ou vazio (400). */
    @Transactional
    public EmergenciaDto registrarReferencias(UUID uuid, ReferenciasLegaisEmergenciaRequestDto dto) {
        controleDeAcesso.exigirAlguma(ATIVAR);
        Emergencia em = ativa(uuid);
        String espin = texto(dto.getEspinNumero());
        if ((espin == null) != (dto.getEspinData() == null)) {
            throw new ResourceBadRequestException("A ESPIN pede o número e a data.");
        }
        String decreto = texto(dto.getDecretoNumero());
        boolean algum = decreto != null || dto.getDecretoData() != null || dto.getDecretoTipo() != null;
        if (algum && (decreto == null || dto.getDecretoData() == null || dto.getDecretoTipo() == null)) {
            throw new ResourceBadRequestException("O decreto pede o tipo, o número e a data.");
        }
        em.setEspinNumero(espin);
        em.setEspinData(dto.getEspinData());
        em.setDecretoTipo(dto.getDecretoTipo());
        em.setDecretoNumero(decreto);
        em.setDecretoData(dto.getDecretoData());
        emergenciaRepository.save(em);
        ContextoAuditoria.registro(em.getUuid());
        return detalhe(em);
    }

    /**
     * Encerra com o motivo. Da ativa, faz as liberações da desmobilização (evento curto); da desmobilização, pede nenhuma ação
     * aberta (422); da planejada, cancela o evento (ADR-0160). Encerrada, 422.
     */
    @Transactional
    public EmergenciaDto encerrar(UUID uuid, EncerramentoEmergenciaRequestDto dto) {
        controleDeAcesso.exigirAlguma(ATIVAR);
        Emergencia em = aberta(uuid);
        boolean planejadaAntes = em.getSituacao() == SituacaoEmergencia.PLANEJADA;
        if (em.getSituacao() == SituacaoEmergencia.EM_DESMOBILIZACAO && coordenacao.acoesAbertas(em) > 0) {
            throw new ResourceUnprocessableEntityException("Há ações em aberto: conclua ou cancele antes de encerrar.");
        }
        liberar(em, "Emergência encerrada: " + dto.getMotivo().trim());
        em.setSituacao(SituacaoEmergencia.ENCERRADA);
        em.setFim(Instant.now());
        em.setMotivoEncerramento(dto.getMotivo().trim());
        emergenciaRepository.save(em);
        ContextoAuditoria.registro(em.getUuid());
        ContextoAuditoria.detalhe(planejadaAntes ? "Evento de massa cancelado antes da operação." : "Emergência encerrada.");
        return detalhe(em);
    }

    // ── Desmobilização (E5, ADR-0159) ───────────────────────────────────────────────────────────────

    /**
     * Inicia a desmobilização da emergência ativa (422): conclui as mobilizações abertas, desativa a capacidade e os planos e
     * registra o que foi liberado. Daí em diante não entra ação, mobilização nem capacidade nova.
     */
    @Transactional
    public EmergenciaDto iniciarDesmobilizacao(UUID uuid, com.edufelizardo.maissaudepublica.models.dtos.version1.request.DesmobilizacaoRequestDto dto) {
        controleDeAcesso.exigirAlguma(ATIVAR);
        Emergencia em = ativa(uuid);
        int[] liberado = liberar(em, "Desmobilização: " + dto.getJustificativa().trim());
        desmobilizacaoRepository.save(new com.edufelizardo.maissaudepublica.models.Desmobilizacao(null, em, Instant.now(), dto.getResponsavel().trim(),
                dto.getJustificativa().trim(), liberado[0], liberado[1], liberado[2], liberado[3], liberado[4], UsuarioAutenticado.cpf()));
        em.setSituacao(SituacaoEmergencia.EM_DESMOBILIZACAO);
        emergenciaRepository.save(em);
        ContextoAuditoria.registro(em.getUuid());
        ContextoAuditoria.detalhe("Desmobilização iniciada: " + liberado[0] + " mobilização(ões), " + liberado[2] + " capacidade(s), " + liberado[3]
                + " plano(s), " + liberado[4] + " estrutura(s).");
        return detalhe(em);
    }

    /** Cancela as ações que restaram abertas na desmobilização (fora dela, 422; nenhuma aberta, 422). */
    @Transactional
    public EmergenciaDto cancelarAcoesRestantes(UUID uuid, com.edufelizardo.maissaudepublica.models.dtos.version1.request.CancelamentoAcoesRequestDto dto) {
        controleDeAcesso.exigirAlguma(COORDENAR, ATIVAR);
        Emergencia em = buscarEmergencia(uuid);
        if (em.getSituacao() != SituacaoEmergencia.EM_DESMOBILIZACAO) {
            throw new ResourceUnprocessableEntityException("Cancelar as ações restantes é da desmobilização.");
        }
        if (coordenacao.cancelarAbertas(em, dto.getMotivo().trim()) == 0) {
            throw new ResourceUnprocessableEntityException("Não há ação em aberto.");
        }
        ContextoAuditoria.registro(em.getUuid());
        return detalhe(em);
    }

    /** As liberações da desmobilização e do encerramento: {mobilizações, profissionais, capacidades, planos, estruturas}. */
    private int[] liberar(Emergencia em, String justificativa) {
        int[] mob = coordenacao.concluirMobilizacoes(em);
        int capacidades = coordenacao.desativarCapacidades(em, justificativa);
        int planos = ativacao.desativarTodas(em, justificativa);
        int estruturas = especializado.desativarTodas(em, justificativa);
        return new int[]{mob[0], mob[1], capacidades, planos, estruturas};
    }

    // ── Eventos especializados (E6a, ADR-0160) ──────────────────────────────────────────────────────

    /** Os dados do evento de massa: a emergência planejada ou ativa (422), do perfil evento de massa (422). */
    @Transactional
    public EmergenciaDto salvarEventoMassa(UUID uuid, com.edufelizardo.maissaudepublica.models.dtos.version1.request.EventoMassaRequestDto dto) {
        controleDeAcesso.exigirAlguma(COORDENAR, ATIVAR);
        Emergencia em = ativaOuPlanejada(uuid);
        especializado.salvarEventoMassa(em, dto);
        ContextoAuditoria.registro(em.getUuid());
        return detalhe(em);
    }

    /** Inicia a operação da planejada (422), com os dados do evento preenchidos (422): passa a ativa, a contar de agora. */
    @Transactional
    public EmergenciaDto iniciarOperacao(UUID uuid, com.edufelizardo.maissaudepublica.models.dtos.version1.request.InicioOperacaoRequestDto dto) {
        controleDeAcesso.exigirAlguma(ATIVAR);
        Emergencia em = buscarEmergencia(uuid);
        if (em.getSituacao() != SituacaoEmergencia.PLANEJADA) {
            throw new ResourceUnprocessableEntityException("A operação começa da emergência planejada.");
        }
        if (!especializado.temEventoMassa(em)) {
            throw new ResourceUnprocessableEntityException("Preencha os dados do evento de massa antes de iniciar a operação.");
        }
        em.setSituacao(SituacaoEmergencia.ATIVA);
        em.setInicio(Instant.now());
        emergenciaRepository.save(em);
        ContextoAuditoria.registro(em.getUuid());
        ContextoAuditoria.detalhe("Operação iniciada: " + dto.getJustificativa().trim());
        return detalhe(em);
    }

    /** Estrutura temporária nova (planejada): a emergência planejada ou ativa (422). */
    @Transactional
    public EmergenciaDto novaEstrutura(UUID uuid, com.edufelizardo.maissaudepublica.models.dtos.version1.request.EstruturaTemporariaRequestDto dto) {
        controleDeAcesso.exigirAlguma(COORDENAR, ATIVAR);
        Emergencia em = ativaOuPlanejada(uuid);
        especializado.novaEstrutura(em, dto);
        ContextoAuditoria.registro(em.getUuid());
        return detalhe(em);
    }

    @Transactional
    public EmergenciaDto atualizarEstrutura(UUID uuid, UUID estruturaId,
                                           com.edufelizardo.maissaudepublica.models.dtos.version1.request.EstruturaTemporariaRequestDto dto) {
        controleDeAcesso.exigirAlguma(COORDENAR, ATIVAR);
        Emergencia em = ativaOuPlanejada(uuid);
        especializado.atualizarEstrutura(em, estruturaId, dto);
        ContextoAuditoria.registro(em.getUuid());
        return detalhe(em);
    }

    /** Em operação ou desativada; desativar vale também na desmobilização. */
    @Transactional
    public EmergenciaDto situacaoDaEstrutura(UUID uuid, UUID estruturaId,
                                            com.edufelizardo.maissaudepublica.models.dtos.version1.request.SituacaoEstruturaRequestDto dto) {
        controleDeAcesso.exigirAlguma(COORDENAR, ATIVAR);
        Emergencia em = aberta(uuid);
        especializado.situacaoDaEstrutura(em, estruturaId, dto);
        ContextoAuditoria.registro(em.getUuid());
        ContextoAuditoria.detalhe("Estrutura temporária " + dto.getSituacao() + ".");
        return detalhe(em);
    }

    /** Boletim da estrutura que já operou; vale também na desmobilização (o último dia). */
    @Transactional
    public EmergenciaDto boletim(UUID uuid, UUID estruturaId, com.edufelizardo.maissaudepublica.models.dtos.version1.request.BoletimEstruturaRequestDto dto) {
        controleDeAcesso.exigirAlguma(REGISTRAR);
        Emergencia em = aberta(uuid);
        especializado.boletim(em, estruturaId, dto);
        ContextoAuditoria.registro(em.getUuid());
        return detalhe(em);
    }

    // ── Planos de contingência na emergência (E2, ADR-0157) ─────────────────────────────────────────

    /** Ativa o plano vigente do tipo da emergência que cobre o nível atual (422); já ativo, 409. */
    @Transactional
    public EmergenciaDto ativarPlano(UUID uuid, com.edufelizardo.maissaudepublica.models.dtos.version1.request.AtivacaoPlanoRequestDto dto) {
        controleDeAcesso.exigirAlguma(ATIVAR);
        Emergencia em = ativa(uuid);
        ativacao.ativar(em, dto);
        ContextoAuditoria.registro(em.getUuid());
        return detalhe(em);
    }

    /** Desativa o plano ativo na emergência, com a justificativa; já desativado, 422. */
    @Transactional
    public EmergenciaDto desativarPlano(UUID uuid, UUID ativacaoId, com.edufelizardo.maissaudepublica.models.dtos.version1.request.DesativacaoPlanoRequestDto dto) {
        controleDeAcesso.exigirAlguma(ATIVAR);
        Emergencia em = aberta(uuid);
        ativacao.desativar(em, ativacaoId, dto);
        ContextoAuditoria.registro(em.getUuid());
        return detalhe(em);
    }

    // ── Coordenação: ações, mobilização e capacidade (E3, ADR-0158) ────────────────────────────────

    private static final String COORDENAR = "EMERGENCIA.COORDENAR";

    /** Executa a escrita da coordenação na emergência ativa (422) e devolve a emergência. */
    private EmergenciaDto coordenar(UUID uuid, java.util.function.Consumer<Emergencia> escrita) {
        return coordenar(uuid, false, escrita);
    }

    /** Idem; com {@code naDesmobilizacao}, vale também na desmobilização (concluir o que ficou aberto). */
    private EmergenciaDto coordenar(UUID uuid, boolean naDesmobilizacao, java.util.function.Consumer<Emergencia> escrita) {
        controleDeAcesso.exigirAlguma(COORDENAR);
        Emergencia em = naDesmobilizacao ? aberta(uuid) : ativa(uuid);
        escrita.accept(em);
        ContextoAuditoria.registro(em.getUuid());
        return detalhe(em);
    }

    @Transactional
    public EmergenciaDto novaAcao(UUID uuid, com.edufelizardo.maissaudepublica.models.dtos.version1.request.AcaoEmergencialRequestDto dto) {
        return coordenar(uuid, em -> coordenacao.novaAcao(em, dto));
    }

    @Transactional
    public EmergenciaDto gerarAcoesDoPlano(UUID uuid) {
        return coordenar(uuid, coordenacao::gerarAcoesDoPlano);
    }

    @Transactional
    public EmergenciaDto situacaoDaAcao(UUID uuid, UUID acaoId, com.edufelizardo.maissaudepublica.models.dtos.version1.request.SituacaoAcaoEmergencialRequestDto dto) {
        return coordenar(uuid, true, em -> coordenacao.situacaoDaAcao(em, acaoId, dto));
    }

    @Transactional
    public EmergenciaDto mobilizar(UUID uuid, com.edufelizardo.maissaudepublica.models.dtos.version1.request.MobilizacaoRequestDto dto) {
        return coordenar(uuid, em -> coordenacao.mobilizar(em, dto));
    }

    @Transactional
    public EmergenciaDto situacaoDaMobilizacao(UUID uuid, UUID mobilizacaoId,
                                              com.edufelizardo.maissaudepublica.models.dtos.version1.request.SituacaoMobilizacaoRequestDto dto) {
        return coordenar(uuid, true, em -> coordenacao.situacaoDaMobilizacao(em, mobilizacaoId, dto));
    }

    @Transactional
    public EmergenciaDto ligarTransferencia(UUID uuid, UUID mobilizacaoId,
                                           com.edufelizardo.maissaudepublica.models.dtos.version1.request.TransferenciaMobilizacaoRequestDto dto) {
        return coordenar(uuid, em -> coordenacao.ligarTransferencia(em, mobilizacaoId, dto));
    }

    @Transactional
    public EmergenciaDto ativarCapacidade(UUID uuid, com.edufelizardo.maissaudepublica.models.dtos.version1.request.AtivacaoCapacidadeRequestDto dto) {
        return coordenar(uuid, em -> coordenacao.ativarCapacidade(em, dto));
    }

    @Transactional
    public EmergenciaDto desativarCapacidade(UUID uuid, UUID ativacaoId,
                                            com.edufelizardo.maissaudepublica.models.dtos.version1.request.DesativacaoCapacidadeRequestDto dto) {
        return coordenar(uuid, true, em -> coordenacao.desativarCapacidade(em, ativacaoId, dto.getJustificativa()));
    }

    // ── Impacto ────────────────────────────────────────────────────────────────────────────────────

    /**
     * O que os donos já dizem: as pessoas cadastradas nas microáreas afetadas (sem cadastro, a população de referência), as
     * unidades com a localização dentro do polígono e os leitos delas fora de uso (ocupados, em higienização, bloqueados).
     */
    @Transactional(readOnly = true)
    public SugestaoImpactoDto sugestao(UUID uuid) {
        Emergencia em = buscarEmergencia(uuid);
        Map<UUID, Long> pessoas = new HashMap<>();
        for (Object[] l : membroNucleoRepository.contarVigentesPorMicroarea()) {
            pessoas.put((UUID) l[0], (Long) l[1]);
        }
        Integer populacao = null;
        for (Microarea m : em.getMicroareas()) {
            long n = pessoas.getOrDefault(m.getUuid(), 0L);
            Integer estimada = n > 0 ? Integer.valueOf((int) n) : m.getPopulacaoReferencia();
            if (estimada != null) {
                populacao = (populacao == null ? 0 : populacao) + estimada;
            }
        }
        List<SugestaoImpactoDto.Unidade> unidades = new ArrayList<>();
        int indisponiveis = 0;
        for (UnidadeDeSaude u : unidadesNaArea(em.getGeometria())) {
            List<Leito> leitos = leitoRepository.findByUnidade_UuidOrderByIdentificacaoAsc(u.getUuid()).stream().filter(Leito::isAtivo).toList();
            int fora = (int) leitos.stream().filter(l -> l.getSituacao() != SituacaoLeito.LIVRE).count();
            indisponiveis += fora;
            unidades.add(new SugestaoImpactoDto.Unidade(u.getUuid(), u.getNome(), u.getSituacaoOperacional(), leitos.size(), fora));
        }
        return new SugestaoImpactoDto(populacao, em.getMicroareas().size(), unidades, indisponiveis);
    }

    /** Uma avaliação nova (as anteriores ficam): só na ativa (422); as unidades precisam existir (404). */
    @Transactional
    public EmergenciaDto avaliar(UUID uuid, AvaliacaoImpactoRequestDto dto) {
        controleDeAcesso.exigirAlguma(REGISTRAR);
        Emergencia em = ativa(uuid);
        AvaliacaoImpacto a = new AvaliacaoImpacto();
        a.setEmergencia(em);
        a.setAvaliadaEm(Instant.now());
        a.setPopulacaoAfetada(dto.getPopulacaoAfetada());
        if (dto.getUnidadeIds() != null) {
            for (UUID id : new HashSet<>(dto.getUnidadeIds())) {
                a.getUnidadesAfetadas().add(unidadeRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(
                        "Não foi possível encontrar uma unidade de saúde com o id " + id + " em nossos registros.")));
            }
        }
        a.setProfissionaisIndisponiveis(dto.getProfissionaisIndisponiveis());
        a.setLeitosIndisponiveis(dto.getLeitosIndisponiveis());
        a.setMedicamentosCriticos(texto(dto.getMedicamentosCriticos()));
        a.setEquipamentos(texto(dto.getEquipamentos()));
        a.setTransporte(texto(dto.getTransporte()));
        a.setInfraestrutura(texto(dto.getInfraestrutura()));
        a.setNivelImpacto(dto.getNivelImpacto());
        a.setObservacao(texto(dto.getObservacao()));
        a.setRegistradoPorCpf(UsuarioAutenticado.cpf());
        avaliacaoRepository.save(a);
        ContextoAuditoria.registro(em.getUuid());
        ContextoAuditoria.detalhe("Avaliação de impacto: " + dto.getNivelImpacto() + ".");
        return detalhe(em);
    }

    // ── Apoio ──────────────────────────────────────────────────────────────────────────────────────

    private void aplicarTerritorio(Emergencia em, String geojson, List<UUID> microareaIds) {
        String geometria = GeometriaTerritorial.normalizar(geojson);
        em.setGeometria(geometria);
        Set<Microarea> microareas = new HashSet<>();
        if (microareaIds != null) {
            for (UUID id : new HashSet<>(microareaIds)) {
                microareas.add(microareaRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(
                        "Não foi possível encontrar a microárea " + id + " em nossos registros.")));
            }
        } else if (geometria != null) {
            Geometry area = GeometriaTerritorial.jts(geometria);
            for (Microarea m : microareaRepository.findAllByOrderByCodigoAsc()) {
                Geometry g = GeometriaTerritorial.jts(m.getGeometria());
                if (g != null && (GeometriaTerritorial.sobrepoe(area, g) || GeometriaTerritorial.dentro(area, g))) {
                    microareas.add(m);
                }
            }
        }
        em.getMicroareas().clear();
        em.getMicroareas().addAll(microareas);
    }

    private List<UnidadeDeSaude> unidadesNaArea(String geometria) {
        Geometry area = GeometriaTerritorial.jts(geometria);
        if (area == null) {
            return List.of();
        }
        return unidadeRepository.findAll().stream()
                .filter(u -> u.getLatitude() != null && u.getLongitude() != null && GeometriaTerritorial.contem(area, u.getLatitude(), u.getLongitude()))
                .sorted(Comparator.comparing(UnidadeDeSaude::getNome, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    private void registrarMudanca(Emergencia em, NivelEmergencia anterior, NivelEmergencia novo, String justificativa) {
        mudancaRepository.save(new MudancaNivelEmergencia(null, em, anterior, novo, justificativa.trim(), Instant.now(), UsuarioAutenticado.cpf()));
    }

    private Profissional responsavel(String matricula) {
        String m = texto(matricula);
        if (m == null) {
            return null;
        }
        return profissionalRepository.findByMatricula(m).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar um profissional com a matrícula " + m + " em nossos registros."));
    }

    private NivelEmergencia nivelAtivo(UUID id) {
        NivelEmergencia n = nivel(id);
        if (!n.isAtivo()) {
            throw new ResourceUnprocessableEntityException("O " + n.getNome() + " está inativo.");
        }
        return n;
    }

    private Emergencia ativa(UUID uuid) {
        Emergencia em = buscarEmergencia(uuid);
        if (em.getSituacao() == SituacaoEmergencia.PLANEJADA) {
            throw new ResourceUnprocessableEntityException("A emergência está planejada: inicie a operação antes.");
        }
        if (em.getSituacao() == SituacaoEmergencia.EM_DESMOBILIZACAO) {
            throw new ResourceUnprocessableEntityException("A emergência está em desmobilização: só se conclui o que ficou aberto.");
        }
        if (em.getSituacao() != SituacaoEmergencia.ATIVA) {
            throw new ResourceUnprocessableEntityException("A emergência já foi encerrada.");
        }
        return em;
    }

    /** Planejada ou ativa (em desmobilização ou encerrada, 422). */
    private Emergencia ativaOuPlanejada(UUID uuid) {
        Emergencia em = buscarEmergencia(uuid);
        return em.getSituacao() == SituacaoEmergencia.PLANEJADA ? em : ativa(uuid);
    }

    /** Planejada, ativa ou em desmobilização (encerrada, 422). */
    private Emergencia aberta(UUID uuid) {
        Emergencia em = buscarEmergencia(uuid);
        if (em.getSituacao() == SituacaoEmergencia.ENCERRADA) {
            throw new ResourceUnprocessableEntityException("A emergência já foi encerrada.");
        }
        return em;
    }

    private static boolean aberto(EventoEmergencial e) {
        return e.getSituacao() == SituacaoEventoEmergencial.REGISTRADO || e.getSituacao() == SituacaoEventoEmergencial.MONITORADO;
    }

    private TipoEmergencia tipo(UUID uuid) {
        return tipoRepository.findById(uuid).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar o tipo de emergência " + uuid + " em nossos registros."));
    }

    private NivelEmergencia nivel(UUID uuid) {
        return nivelRepository.findById(uuid).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar o nível de emergência " + uuid + " em nossos registros."));
    }

    private EventoEmergencial buscarEvento(UUID uuid) {
        return eventoRepository.findById(uuid).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar o evento emergencial " + uuid + " em nossos registros."));
    }

    private Emergencia buscarEmergencia(UUID uuid) {
        return emergenciaRepository.findById(uuid).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar a emergência " + uuid + " em nossos registros."));
    }

    private EmergenciaDto detalhe(Emergencia em) {
        List<AvaliacaoImpacto> avaliacoes = avaliacaoRepository.findDaEmergencia(em.getUuid());
        EmergenciaDto d = resumo(em, avaliacoes.isEmpty() ? null : avaliacoes.get(0));
        d.setGeometria(em.getGeometria());
        d.setMicroareas(em.getMicroareas().stream().sorted(Comparator.comparing(Microarea::getCodigo))
                .map(m -> new EmergenciaDto.MicroareaAfetada(m.getUuid(), m.getCodigo(), m.getNome(), m.getArea().getNome(), m.getGeometria())).toList());
        d.setNiveis(mudancaRepository.findDaEmergencia(em.getUuid()).stream().map(m -> new EmergenciaDto.MudancaNivel(m.getUuid(),
                m.getNivelAnterior() != null ? m.getNivelAnterior().getNome() : null, m.getNivelNovo().getNome(), m.getNivelNovo().getOrdem(),
                m.getJustificativa(), m.getMudadoEm(), m.getMudadoPorCpf())).toList());
        d.setAvaliacoes(avaliacoes.stream().map(a -> new EmergenciaDto.Avaliacao(a.getUuid(), a.getAvaliadaEm(), a.getPopulacaoAfetada(),
                a.getUnidadesAfetadas().stream().map(UnidadeDeSaude::getNome).sorted(String.CASE_INSENSITIVE_ORDER).toList(),
                a.getProfissionaisIndisponiveis(), a.getLeitosIndisponiveis(), a.getMedicamentosCriticos(), a.getEquipamentos(), a.getTransporte(),
                a.getInfraestrutura(), a.getNivelImpacto(), a.getObservacao())).toList());
        d.setPlanos(ativacao.planosDaEmergencia(em));
        coordenacao.preencher(d, em, avaliacoes.isEmpty() ? null : avaliacoes.get(0));
        desmobilizacaoRepository.findByEmergencia_Uuid(em.getUuid()).ifPresent(x -> d.setDesmobilizacao(new EmergenciaDto.DesmobilizacaoItem(
                x.getIniciadaEm(), x.getResponsavel(), x.getJustificativa(), x.getMobilizacoesConcluidas(), x.getProfissionaisLiberados(),
                x.getCapacidadesDesativadas(), x.getPlanosDesativados(), x.getEstruturasDesativadas())));
        d.setPendencias(em.getSituacao() == SituacaoEmergencia.ENCERRADA ? List.of()
                : coordenacao.pendencias(em, avaliacoes.isEmpty() ? null : avaliacoes.get(0)));
        d.setEventoMassa(especializado.eventoMassa(em));
        d.setEstruturas(especializado.estruturas(em));
        return d;
    }

    private static EmergenciaDto resumo(Emergencia em, AvaliacaoImpacto ultima) {
        TipoEmergencia t = em.getTipo();
        NivelEmergencia n = em.getNivel();
        Profissional r = em.getResponsavel();
        return new EmergenciaDto(em.getUuid(), em.getNome(), em.getEvento().getUuid(), t.getUuid(), t.getNome(), t.getCategoria(), t.getCobrade(),
                n.getUuid(), n.getOrdem(), n.getNome(), em.getSituacao(), em.getInicio(), em.getFim(), em.getMotivoEncerramento(),
                r != null ? r.getMatricula() : null, r != null ? r.getNome() : null, ultima != null ? ultima.getNivelImpacto() : null,
                ultima != null ? ultima.getAvaliadaEm() : null, null, List.of(), em.getEspinNumero(), em.getEspinData(), em.getDecretoTipo(),
                em.getDecretoNumero(), em.getDecretoData(), List.of(), List.of(), List.of(), List.of(), 0, List.of(), List.of(), List.of(), null, List.of(),
                t.getPerfil(), null, List.of());
    }

    private static EventoEmergencialDto dto(EventoEmergencial e, UUID emergenciaId) {
        return new EventoEmergencialDto(e.getUuid(), e.getTipo().getUuid(), e.getTipo().getNome(), e.getTipo().getCobrade(), e.getOcorridoEm(),
                e.getLocal(), e.getGeometria(), e.getFonte(), e.getDescricao(), e.getSurto() != null ? e.getSurto().getUuid() : null,
                e.getSurto() != null ? e.getSurto().getNome() : null, e.getSituacao(), e.getMotivoDescarte(), emergenciaId, e.getRegistradoEm());
    }

    private static TipoEmergenciaDto dto(TipoEmergencia t) {
        return new TipoEmergenciaDto(t.getUuid(), t.getCodigo(), t.getNome(), t.getCategoria(), t.getCobrade(), t.getPerfil(), t.isAtivo());
    }

    private static NivelEmergenciaDto dto(NivelEmergencia n) {
        return new NivelEmergenciaDto(n.getUuid(), n.getOrdem(), n.getNome(), n.getDescricao(), n.isAtivo());
    }

    private static String texto(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }
}
