package com.edufelizardo.maissaudepublica.services.version1;

import com.edufelizardo.maissaudepublica.config.UsuarioAutenticado;
import com.edufelizardo.maissaudepublica.exceptions.ResourceBadRequestException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceNotFoundException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceUnprocessableEntityException;
import com.edufelizardo.maissaudepublica.models.Agravo;
import com.edufelizardo.maissaudepublica.models.BoletimEstrutura;
import com.edufelizardo.maissaudepublica.models.ExposicaoAmbiental;
import com.edufelizardo.maissaudepublica.models.Microarea;
import com.edufelizardo.maissaudepublica.models.Notificacao;
import com.edufelizardo.maissaudepublica.models.UnidadeDeSaude;
import com.edufelizardo.maissaudepublica.models.Emergencia;
import com.edufelizardo.maissaudepublica.models.EstruturaTemporaria;
import com.edufelizardo.maissaudepublica.models.EventoMassa;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.BoletimEstruturaRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.EstruturaTemporariaRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.EventoMassaRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.ExposicaoAmbientalRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.SituacaoEstruturaRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.EmergenciaDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.PainelEpidemiaDto;
import com.edufelizardo.maissaudepublica.models.enuns.ClassificacaoCasoEpidemiologico;
import com.edufelizardo.maissaudepublica.models.enuns.EvolucaoCaso;
import com.edufelizardo.maissaudepublica.models.enuns.PerfilTipoEmergencia;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoEmergencia;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoEstruturaTemporaria;
import com.edufelizardo.maissaudepublica.repositories.AgravoRepository;
import com.edufelizardo.maissaudepublica.repositories.BoletimEstruturaRepository;
import com.edufelizardo.maissaudepublica.repositories.ClassificacaoCasoRepository;
import com.edufelizardo.maissaudepublica.repositories.ExposicaoAmbientalRepository;
import com.edufelizardo.maissaudepublica.repositories.NotificacaoRepository;
import com.edufelizardo.maissaudepublica.repositories.EstruturaTemporariaRepository;
import com.edufelizardo.maissaudepublica.repositories.EventoMassaRepository;
import com.edufelizardo.maissaudepublica.repositories.UnidadeDeSaudeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Os eventos especializados, fatia E6a das Emergências (ADR-0160): os dados do evento de massa e as estruturas temporárias
 * de atendimento (posto médico, tenda de hidratação, hospital de campanha), com os boletins. A estrutura é capacidade, não
 * unidade: a unidade de referência continua dona de escala, estoque e leitos. Na E6b (ADR-0161), o painel da epidemia,
 * lido da Vigilância sem duplicar nada, e a exposição ambiental do desastre. O {@link EmergenciaService} confere a
 * permissão e a situação da emergência antes de chamar.
 */
@Component
class EventoEspecializadoService {

    @Autowired private EventoMassaRepository eventoMassaRepository;
    @Autowired private EstruturaTemporariaRepository estruturaRepository;
    @Autowired private BoletimEstruturaRepository boletimRepository;
    @Autowired private UnidadeDeSaudeRepository unidadeRepository;
    @Autowired private AgravoRepository agravoRepository;
    @Autowired private NotificacaoRepository notificacaoRepository;
    @Autowired private ClassificacaoCasoRepository classificacaoRepository;
    @Autowired private ExposicaoAmbientalRepository exposicaoRepository;

    // ── Evento de massa ────────────────────────────────────────────────────────────────────────────

    /** Só no perfil evento de massa (422); o fim não vem antes do início e o público máximo não fica abaixo do estimado (400). */
    void salvarEventoMassa(Emergencia em, EventoMassaRequestDto dto) {
        if (em.getTipo().getPerfil() != PerfilTipoEmergencia.EVENTO_DE_MASSA) {
            throw new ResourceUnprocessableEntityException("Os dados de evento de massa são do tipo de perfil evento de massa.");
        }
        if (dto.getFimPrevisto().isBefore(dto.getInicioPrevisto())) {
            throw new ResourceBadRequestException("O fim previsto não pode vir antes do início.");
        }
        if (dto.getPublicoMaximo() != null && dto.getPublicoMaximo() < dto.getPublicoEstimado()) {
            throw new ResourceBadRequestException("O público máximo não fica abaixo do estimado.");
        }
        EventoMassa e = eventoMassaRepository.findByEmergencia_Uuid(em.getUuid()).orElseGet(() -> {
            EventoMassa novo = new EventoMassa();
            novo.setEmergencia(em);
            return novo;
        });
        e.setNatureza(dto.getNatureza());
        e.setLocal(dto.getLocal().trim());
        e.setInicioPrevisto(dto.getInicioPrevisto());
        e.setFimPrevisto(dto.getFimPrevisto());
        e.setPublicoEstimado(dto.getPublicoEstimado());
        e.setPublicoMaximo(dto.getPublicoMaximo());
        e.setOrganizador(texto(dto.getOrganizador()));
        e.setContato(texto(dto.getContato()));
        e.setRisco(dto.getRisco());
        e.setObservacao(texto(dto.getObservacao()));
        e.setAtualizadoEm(Instant.now());
        e.setRegistradoPorCpf(UsuarioAutenticado.cpf());
        eventoMassaRepository.save(e);
    }

    boolean temEventoMassa(Emergencia em) {
        return eventoMassaRepository.findByEmergencia_Uuid(em.getUuid()).isPresent();
    }

    EmergenciaDto.EventoMassaItem eventoMassa(Emergencia em) {
        return eventoMassaRepository.findByEmergencia_Uuid(em.getUuid()).map(e -> new EmergenciaDto.EventoMassaItem(e.getNatureza(), e.getLocal(),
                e.getInicioPrevisto(), e.getFimPrevisto(), e.getPublicoEstimado(), e.getPublicoMaximo(), e.getOrganizador(), e.getContato(),
                e.getRisco(), e.getObservacao(), e.getAtualizadoEm())).orElse(null);
    }

    // ── Estruturas temporárias ─────────────────────────────────────────────────────────────────────

    /** Nasce planejada; a unidade de referência precisa existir (404). */
    void novaEstrutura(Emergencia em, EstruturaTemporariaRequestDto dto) {
        EstruturaTemporaria e = new EstruturaTemporaria();
        e.setEmergencia(em);
        e.setSituacao(SituacaoEstruturaTemporaria.PLANEJADA);
        e.setRegistradoPorCpf(UsuarioAutenticado.cpf());
        preencher(e, dto);
        estruturaRepository.save(e);
    }

    /** Desativada não muda (422). */
    void atualizarEstrutura(Emergencia em, UUID id, EstruturaTemporariaRequestDto dto) {
        EstruturaTemporaria e = estrutura(em, id);
        if (e.getSituacao() == SituacaoEstruturaTemporaria.DESATIVADA) {
            throw new ResourceUnprocessableEntityException("A estrutura já foi desativada.");
        }
        preencher(e, dto);
        estruturaRepository.save(e);
    }

    /**
     * Planejada → em operação (a emergência ativa, 422) → desativada (com o motivo, 400). Em operação só da planejada (422);
     * desativada não muda (422).
     */
    void situacaoDaEstrutura(Emergencia em, UUID id, SituacaoEstruturaRequestDto dto) {
        EstruturaTemporaria e = estrutura(em, id);
        if (e.getSituacao() == SituacaoEstruturaTemporaria.DESATIVADA) {
            throw new ResourceUnprocessableEntityException("A estrutura já foi desativada.");
        }
        switch (dto.getSituacao()) {
            case EM_OPERACAO -> {
                if (e.getSituacao() != SituacaoEstruturaTemporaria.PLANEJADA) {
                    throw new ResourceUnprocessableEntityException("A estrutura já está em operação.");
                }
                if (em.getSituacao() != SituacaoEmergencia.ATIVA) {
                    throw new ResourceUnprocessableEntityException(em.getSituacao() == SituacaoEmergencia.PLANEJADA
                            ? "Inicie a operação da emergência antes de abrir a estrutura." : "A emergência não está ativa.");
                }
                e.setSituacao(SituacaoEstruturaTemporaria.EM_OPERACAO);
                e.setAtivadaEm(Instant.now());
            }
            case DESATIVADA -> {
                String motivo = texto(dto.getMotivo());
                if (motivo == null) {
                    throw new ResourceBadRequestException("Informe o motivo da desativação.");
                }
                desativar(e, motivo);
            }
            default -> throw new ResourceBadRequestException("A estrutura passa a em operação ou desativada.");
        }
        estruturaRepository.save(e);
    }

    /** Só da estrutura que já operou (422); a data não é futura (400) nem anterior à abertura (422). */
    void boletim(Emergencia em, UUID id, BoletimEstruturaRequestDto dto) {
        EstruturaTemporaria e = estrutura(em, id);
        if (e.getAtivadaEm() == null) {
            throw new ResourceUnprocessableEntityException("A estrutura ainda não entrou em operação.");
        }
        if (dto.getData().isAfter(LocalDate.now())) {
            throw new ResourceBadRequestException("O boletim não pode ser de data futura.");
        }
        if (dto.getData().isBefore(LocalDate.ofInstant(e.getAtivadaEm(), ZoneId.systemDefault()))) {
            throw new ResourceUnprocessableEntityException("O boletim não pode ser de antes da abertura da estrutura.");
        }
        boletimRepository.save(new BoletimEstrutura(null, e, dto.getData(), dto.getAtendimentos(), zero(dto.getRemocoes()),
                zero(dto.getCasosGraves()), zero(dto.getObitos()), texto(dto.getObservacao()), Instant.now(), UsuarioAutenticado.cpf()));
    }

    /** Desativa as estruturas planejadas e em operação (desmobilização e encerramento); devolve quantas. */
    int desativarTodas(Emergencia em, String motivo) {
        int n = 0;
        for (EstruturaTemporaria e : estruturaRepository.findDaEmergencia(em.getUuid())) {
            if (e.getSituacao() != SituacaoEstruturaTemporaria.DESATIVADA) {
                desativar(e, motivo.length() > 500 ? motivo.substring(0, 500) : motivo);
                estruturaRepository.save(e);
                n++;
            }
        }
        return n;
    }

    List<EmergenciaDto.EstruturaItem> estruturas(Emergencia em) {
        Map<UUID, List<BoletimEstrutura>> boletins = boletimRepository.findDaEmergencia(em.getUuid()).stream()
                .collect(Collectors.groupingBy(b -> b.getEstrutura().getUuid()));
        return estruturaRepository.findDaEmergencia(em.getUuid()).stream().map(e -> {
            List<BoletimEstrutura> bs = boletins.getOrDefault(e.getUuid(), List.of());
            return new EmergenciaDto.EstruturaItem(e.getUuid(), e.getTipo(), e.getNome(), e.getLocal(), e.getLatitude(), e.getLongitude(),
                    e.getUnidadeReferencia().getUuid(), e.getUnidadeReferencia().getNome(), e.getLeitos(), e.getAtendimentosDia(), e.getAmbulancias(),
                    e.getHorario(), e.getSituacao(), e.getAtivadaEm(), e.getDesativadaEm(), e.getMotivoDesativacao(),
                    bs.stream().mapToInt(BoletimEstrutura::getAtendimentos).sum(), bs.stream().mapToInt(BoletimEstrutura::getRemocoes).sum(),
                    bs.stream().mapToInt(BoletimEstrutura::getCasosGraves).sum(), bs.stream().mapToInt(BoletimEstrutura::getObitos).sum(),
                    bs.stream().map(b -> new EmergenciaDto.Boletim(b.getUuid(), b.getData(), b.getAtendimentos(), b.getRemocoes(), b.getCasosGraves(),
                            b.getObitos(), b.getObservacao(), b.getRegistradoEm())).toList());
        }).toList();
    }

    /** Para o relatório: {estruturas que operaram, atendimentos, remoções, casos graves, óbitos}. */
    int[] totais(Emergencia em) {
        int operaram = (int) estruturaRepository.findDaEmergencia(em.getUuid()).stream().filter(e -> e.getAtivadaEm() != null).count();
        int[] t = {operaram, 0, 0, 0, 0};
        for (BoletimEstrutura b : boletimRepository.findDaEmergencia(em.getUuid())) {
            t[1] += b.getAtendimentos();
            t[2] += b.getRemocoes();
            t[3] += b.getCasosGraves();
            t[4] += b.getObitos();
        }
        return t;
    }

    // ── Epidemia (E6b, ADR-0161) ───────────────────────────────────────────────────────────────────

    /** Só no perfil epidemia (422); o agravo precisa existir (404) e não ser sensível (422): o painel é de toda a rede. */
    void definirAgravo(Emergencia em, UUID agravoId) {
        exigirPerfil(em, PerfilTipoEmergencia.EPIDEMIA, "O agravo é da emergência de perfil epidemia.");
        Agravo a = agravoRepository.findById(agravoId).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar o agravo " + agravoId + " em nossos registros."));
        if (a.isSensivel()) {
            throw new ResourceUnprocessableEntityException("O agravo " + a.getNome() + " é sensível e não entra no painel da emergência.");
        }
        em.setAgravo(a);
    }

    /** O agravo da emergência; sem ele, o do surto de origem (pode ser nulo). */
    Agravo agravo(Emergencia em) {
        if (em.getAgravo() != null) {
            return em.getAgravo();
        }
        return em.getEvento().getSurto() != null ? em.getEvento().getSurto().getAgravo() : null;
    }

    /**
     * As notificações do agravo desde 4 semanas antes do início, pela data dos primeiros sintomas (ou da notificação); na área
     * afetada, quando a emergência tem microáreas. Só no perfil epidemia (422); sem agravo, 422; agravo sensível, 422.
     */
    PainelEpidemiaDto painel(Emergencia em) {
        exigirPerfil(em, PerfilTipoEmergencia.EPIDEMIA, "O painel é da emergência de perfil epidemia.");
        Agravo agravo = agravo(em);
        if (agravo == null) {
            throw new ResourceUnprocessableEntityException("Escolha o agravo da epidemia.");
        }
        if (agravo.isSensivel()) {
            throw new ResourceUnprocessableEntityException("O agravo " + agravo.getNome() + " é sensível e não entra no painel da emergência.");
        }
        LocalDate ate = em.getFim() != null ? LocalDate.ofInstant(em.getFim(), ZoneId.systemDefault()) : LocalDate.now();
        LocalDate desde = SemanaEpidemiologica.de(LocalDate.ofInstant(em.getInicio(), ZoneId.systemDefault()).minusWeeks(4)).inicio();
        Set<UUID> area = new HashSet<>();
        em.getMicroareas().forEach(m -> area.add(m.getUuid()));
        List<Notificacao> casos = notificacaoRepository.findTodas().stream()
                .filter(n -> n.getAgravo().getUuid().equals(agravo.getUuid()))
                .filter(n -> area.isEmpty() || (n.getMicroarea() != null && area.contains(n.getMicroarea().getUuid())))
                .filter(n -> {
                    LocalDate d = data(n);
                    return !d.isBefore(desde) && !d.isAfter(ate);
                })
                .toList();
        int confirmados = 0;
        int descartados = 0;
        int obitos = 0;
        for (Notificacao n : casos) {
            ClassificacaoCasoEpidemiologico c = classificacao(n);
            if (c == ClassificacaoCasoEpidemiologico.CONFIRMADO) {
                confirmados++;
            } else if (c == ClassificacaoCasoEpidemiologico.DESCARTADO) {
                descartados++;
            }
            if (n.getEvolucao() == EvolucaoCaso.OBITO_PELO_AGRAVO) {
                obitos++;
            }
        }
        List<LocalDate> datas = casos.stream().map(EventoEspecializadoService::data).toList();
        var curva = VigilanciaService.curva(datas, desde, ate);
        int atual = curva.isEmpty() ? 0 : curva.get(curva.size() - 1).getCasos();
        int anterior = curva.size() < 2 ? 0 : curva.get(curva.size() - 2).getCasos();
        return new PainelEpidemiaDto(agravo.getUuid(), agravo.getNome(), em.getAgravo() == null, desde, ate, !area.isEmpty(), casos.size(), confirmados,
                descartados, casos.size() - confirmados - descartados, obitos, atual, anterior, curva);
    }

    private ClassificacaoCasoEpidemiologico classificacao(Notificacao n) {
        if (n.getClassificacaoFinal() != null) {
            return n.getClassificacaoFinal();
        }
        var h = classificacaoRepository.findDaNotificacao(n.getUuid());
        return h.isEmpty() ? null : h.get(0).getClassificacao();
    }

    private static LocalDate data(Notificacao n) {
        return n.getDataPrimeirosSintomas() != null ? n.getDataPrimeirosSintomas() : n.getDataNotificacao();
    }

    // ── Exposição ambiental (E6b, ADR-0161) ────────────────────────────────────────────────────────

    /** Só no perfil desastre ambiental (422); as unidades precisam existir (404). */
    void registrarExposicao(Emergencia em, ExposicaoAmbientalRequestDto dto) {
        exigirPerfil(em, PerfilTipoEmergencia.DESASTRE_AMBIENTAL, "A exposição ambiental é da emergência de perfil desastre ambiental.");
        ExposicaoAmbiental x = new ExposicaoAmbiental();
        x.setEmergencia(em);
        x.setAvaliadaEm(Instant.now());
        x.setMeio(dto.getMeio());
        x.setAgente(dto.getAgente().trim());
        x.setFonte(texto(dto.getFonte()));
        x.setPopulacaoExposta(dto.getPopulacaoExposta());
        if (dto.getUnidadeIds() != null) {
            for (UUID id : new HashSet<>(dto.getUnidadeIds())) {
                x.getUnidades().add(unidadeRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(
                        "Não foi possível encontrar uma unidade de saúde com o id " + id + " em nossos registros.")));
            }
        }
        x.setMedidas(texto(dto.getMedidas()));
        x.setRecomendacao(texto(dto.getRecomendacao()));
        x.setRegistradoPorCpf(UsuarioAutenticado.cpf());
        exposicaoRepository.save(x);
    }

    List<EmergenciaDto.ExposicaoItem> exposicoes(Emergencia em) {
        return exposicaoRepository.findDaEmergencia(em.getUuid()).stream().map(x -> new EmergenciaDto.ExposicaoItem(x.getUuid(), x.getAvaliadaEm(),
                x.getMeio(), x.getAgente(), x.getFonte(), x.getPopulacaoExposta(),
                x.getUnidades().stream().map(UnidadeDeSaude::getNome).sorted(String.CASE_INSENSITIVE_ORDER).toList(), x.getMedidas(),
                x.getRecomendacao())).toList();
    }

    /** Para o relatório: {exposições, maior população exposta (ou nulo)}. */
    Integer[] totaisDeExposicao(Emergencia em) {
        List<ExposicaoAmbiental> lista = exposicaoRepository.findDaEmergencia(em.getUuid());
        Integer maior = lista.stream().map(ExposicaoAmbiental::getPopulacaoExposta).filter(java.util.Objects::nonNull).max(Comparator.naturalOrder())
                .orElse(null);
        return new Integer[]{lista.size(), maior};
    }

    // ── Apoio ──────────────────────────────────────────────────────────────────────────────────────

    private void preencher(EstruturaTemporaria e, EstruturaTemporariaRequestDto dto) {
        if ((dto.getLatitude() == null) != (dto.getLongitude() == null)) {
            throw new ResourceBadRequestException("A localização pede a latitude e a longitude.");
        }
        e.setTipo(dto.getTipo());
        e.setNome(dto.getNome().trim());
        e.setLocal(dto.getLocal().trim());
        e.setLatitude(dto.getLatitude());
        e.setLongitude(dto.getLongitude());
        e.setUnidadeReferencia(unidadeRepository.findById(dto.getUnidadeReferenciaId()).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar uma unidade de saúde com o id " + dto.getUnidadeReferenciaId() + " em nossos registros.")));
        e.setLeitos(zero(dto.getLeitos()));
        e.setAtendimentosDia(dto.getAtendimentosDia());
        e.setAmbulancias(zero(dto.getAmbulancias()));
        e.setHorario(texto(dto.getHorario()));
    }

    private static void exigirPerfil(Emergencia em, PerfilTipoEmergencia perfil, String mensagem) {
        if (em.getTipo().getPerfil() != perfil) {
            throw new ResourceUnprocessableEntityException(mensagem);
        }
    }

    private static void desativar(EstruturaTemporaria e, String motivo) {
        e.setSituacao(SituacaoEstruturaTemporaria.DESATIVADA);
        e.setDesativadaEm(Instant.now());
        e.setMotivoDesativacao(motivo);
    }

    private EstruturaTemporaria estrutura(Emergencia em, UUID id) {
        return estruturaRepository.findById(id).filter(e -> e.getEmergencia().getUuid().equals(em.getUuid()))
                .orElseThrow(() -> new ResourceNotFoundException("Não foi possível encontrar a estrutura " + id + " nesta emergência."));
    }

    private static int zero(Integer n) {
        return n == null ? 0 : n;
    }

    private static String texto(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }
}
