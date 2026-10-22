package com.edufelizardo.maissaudepublica.services.version1;

import com.edufelizardo.maissaudepublica.config.ContextoAuditoria;
import com.edufelizardo.maissaudepublica.config.UsuarioAutenticado;
import com.edufelizardo.maissaudepublica.exceptions.ResourceBadRequestException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceForbiddenException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceNotFoundException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceUnprocessableEntityException;
import com.edufelizardo.maissaudepublica.models.AcompanhamentoSaudeMental;
import com.edufelizardo.maissaudepublica.models.AvaliacaoRisco;
import com.edufelizardo.maissaudepublica.models.EtapaCrise;
import com.edufelizardo.maissaudepublica.models.EventoCrise;
import com.edufelizardo.maissaudepublica.models.Paciente;
import com.edufelizardo.maissaudepublica.models.UnidadeDeSaude;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.AvaliacaoRiscoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.EncerramentoCriseRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.EtapaCriseRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.EventoCriseRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.NotificacaoCriseRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.AcompanhamentoSaudeMentalDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.CrisesSaudeMentalDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.EventoCriseDto;
import com.edufelizardo.maissaudepublica.models.enuns.EtapaCriseSaudeMental;
import com.edufelizardo.maissaudepublica.models.enuns.NivelRiscoSaudeMental;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoCriseSaudeMental;
import com.edufelizardo.maissaudepublica.models.enuns.TipoCriseSaudeMental;
import com.edufelizardo.maissaudepublica.repositories.AcompanhamentoSaudeMentalRepository;
import com.edufelizardo.maissaudepublica.repositories.AvaliacaoRiscoRepository;
import com.edufelizardo.maissaudepublica.repositories.EtapaCriseRepository;
import com.edufelizardo.maissaudepublica.repositories.EventoCriseRepository;
import com.edufelizardo.maissaudepublica.repositories.PacienteRepository;
import com.edufelizardo.maissaudepublica.repositories.UnidadeDeSaudeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Saúde Mental, fatia SM3 (ADR-0123): o risco com histórico no caso e o evento de crise com as etapas. A notificação
 * compulsória de violência autoprovocada é só sugerida e registrada como feita (a notificação no sistema vem com a
 * Vigilância V1); o acionamento do SAMU é registrado em texto (depende da regulação de urgência).
 */
@Service
public class RiscoCriseService {

    public static final String CRISE = "SAUDE_MENTAL.CRISE";

    @Autowired
    private SaudeMentalService saudeMentalService;

    @Autowired
    private AvaliacaoRiscoRepository riscoRepository;

    @Autowired
    private EventoCriseRepository criseRepository;

    @Autowired
    private EtapaCriseRepository etapaRepository;

    @Autowired
    private AcompanhamentoSaudeMentalRepository casoRepository;

    @Autowired
    private PacienteRepository pacienteRepository;

    @Autowired
    private UnidadeDeSaudeRepository unidadeDeSaudeRepository;

    @Autowired
    private ControleDeAcesso controleDeAcesso;

    @Autowired
    private VinculoAssistencialService vinculoService;

    // ── Risco ──────────────────────────────────────────────────────────────────────────────────────

    /** Avalia ou reavalia um tipo de risco: sempre um registro novo. */
    @Transactional
    public AcompanhamentoSaudeMentalDto avaliarRisco(UUID casoId, AvaliacaoRiscoRequestDto dto) {
        AcompanhamentoSaudeMental caso = saudeMentalService.casoParaEscrever(casoId);
        boolean moderadoOuMais = dto.getNivel() == NivelRiscoSaudeMental.MODERADO || dto.getNivel() == NivelRiscoSaudeMental.ALTO;
        if (moderadoOuMais && texto(dto.getMedidas()) == null) {
            throw new ResourceBadRequestException("Com risco moderado ou alto, descreva as medidas adotadas.");
        }
        if (dto.getNivel() == NivelRiscoSaudeMental.ALTO && dto.getReavaliarEm() == null) {
            throw new ResourceBadRequestException("Com risco alto, informe quando reavaliar.");
        }
        if (dto.getReavaliarEm() != null && dto.getReavaliarEm().isBefore(LocalDate.now())) {
            throw new ResourceBadRequestException("A reavaliação não pode ser no passado.");
        }
        AvaliacaoRisco a = new AvaliacaoRisco();
        a.setAcompanhamento(caso);
        a.setTipo(dto.getTipo());
        a.setNivel(dto.getNivel());
        a.setFatores(texto(dto.getFatores()));
        a.setMedidas(texto(dto.getMedidas()));
        a.setPlanoSeguranca(Boolean.TRUE.equals(dto.getPlanoSeguranca()));
        a.setFrequenciaContato(texto(dto.getFrequenciaContato()));
        a.setReavaliarEm(dto.getNivel() == NivelRiscoSaudeMental.SEM_RISCO_ATUAL ? null : dto.getReavaliarEm());
        a.setAvaliadaEm(Instant.now());
        a.setProfissional(saudeMentalService.profissionalDoUsuario());
        a.setRegistradoPorCpf(UsuarioAutenticado.cpf());
        a = riscoRepository.save(a);
        ContextoAuditoria.registro(a.getUuid());
        ContextoAuditoria.detalhe("Risco " + dto.getTipo() + ": " + dto.getNivel() + ".");
        return saudeMentalService.detalheLiberado(caso);
    }

    // ── Crise ──────────────────────────────────────────────────────────────────────────────────────

    /** Registra a crise na unidade; se o paciente tem caso vigente, liga-se a ele. */
    @Transactional
    public EventoCriseDto registrar(EventoCriseRequestDto dto) {
        UnidadeDeSaude unidade = unidadeDeSaudeRepository.findById(dto.getUnidadeId()).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar uma unidade de saúde com o id " + dto.getUnidadeId() + " em nossos registros."));
        controleDeAcesso.exigir(CRISE, unidade);
        Paciente paciente = pacienteRepository.findById(dto.getPacienteId()).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar um paciente com o id " + dto.getPacienteId() + " em nossos registros."));
        ContextoAuditoria.paciente(paciente.getUuid());
        Instant quando = dto.getIdentificadaEm() != null ? dto.getIdentificadaEm() : Instant.now();
        if (quando.isAfter(Instant.now().plusSeconds(60))) {
            throw new ResourceBadRequestException("A crise não pode ser no futuro.");
        }
        EventoCrise c = new EventoCrise();
        c.setPaciente(paciente);
        c.setUnidade(unidade);
        c.setAcompanhamento(casoRepository.findVigenteDoPaciente(paciente.getUuid()).orElse(null));
        c.setIdentificadaEm(quando);
        c.setLocalOcorrencia(texto(dto.getLocalOcorrencia()));
        c.setTipo(dto.getTipo());
        c.setDescricao(dto.getDescricao().trim());
        c.setSituacao(SituacaoCriseSaudeMental.EM_CURSO);
        c.setNotificacaoSugerida(dto.getTipo() == TipoCriseSaudeMental.AUTOAGRESSAO || dto.getTipo() == TipoCriseSaudeMental.TENTATIVA_DE_SUICIDIO);
        c.setProfissional(saudeMentalService.profissionalDoUsuario());
        c.setRegistradoPorCpf(UsuarioAutenticado.cpf());
        c = criseRepository.save(c);
        ContextoAuditoria.registro(c.getUuid());
        ContextoAuditoria.detalhe("Crise " + dto.getTipo() + ".");
        return CriseLeitura.dto(c, true, List.of());
    }

    @Transactional
    public EventoCriseDto registrarEtapa(UUID criseId, EtapaCriseRequestDto dto) {
        EventoCrise c = paraEscrever(criseId);
        if (c.getSituacao() != SituacaoCriseSaudeMental.EM_CURSO) {
            throw new ResourceUnprocessableEntityException("A crise já foi encerrada.");
        }
        Instant quando = dto.getOcorridaEm() != null ? dto.getOcorridaEm() : Instant.now();
        if (quando.isBefore(c.getIdentificadaEm()) || quando.isAfter(Instant.now().plusSeconds(60))) {
            throw new ResourceBadRequestException("A etapa precisa ser entre a identificação da crise e agora.");
        }
        EtapaCrise e = new EtapaCrise();
        e.setCrise(c);
        e.setEtapa(dto.getEtapa());
        e.setDescricao(dto.getDescricao().trim());
        e.setOcorridaEm(quando);
        e.setProfissional(saudeMentalService.profissionalDoUsuario());
        e.setRegistradoPorCpf(UsuarioAutenticado.cpf());
        etapaRepository.save(e);
        ContextoAuditoria.detalhe("Etapa da crise: " + dto.getEtapa() + ".");
        return CriseLeitura.dto(c, true, etapaRepository.findDaCrise(c.getUuid()));
    }

    /** Encerra com o desfecho; pede a etapa de pós-crise antes. */
    @Transactional
    public EventoCriseDto encerrar(UUID criseId, EncerramentoCriseRequestDto dto) {
        EventoCrise c = paraEscrever(criseId);
        if (c.getSituacao() != SituacaoCriseSaudeMental.EM_CURSO) {
            throw new ResourceUnprocessableEntityException("A crise já foi encerrada.");
        }
        List<EtapaCrise> etapas = etapaRepository.findDaCrise(c.getUuid());
        if (etapas.stream().noneMatch(e -> e.getEtapa() == EtapaCriseSaudeMental.POS_CRISE)) {
            throw new ResourceUnprocessableEntityException("Registre o acompanhamento pós-crise antes de encerrar.");
        }
        c.setSituacao(SituacaoCriseSaudeMental.ENCERRADA);
        c.setDesfecho(dto.getDesfecho());
        c.setEncerradaEm(Instant.now());
        criseRepository.save(c);
        ContextoAuditoria.detalhe("Crise encerrada: " + dto.getDesfecho() + ".");
        return CriseLeitura.dto(c, true, etapas);
    }

    /** O profissional registra que fez a notificação compulsória sugerida (fora do sistema até a Vigilância V1). */
    @Transactional
    public EventoCriseDto registrarNotificacao(UUID criseId, NotificacaoCriseRequestDto dto) {
        EventoCrise c = paraEscrever(criseId);
        if (!c.isNotificacaoSugerida()) {
            throw new ResourceUnprocessableEntityException("Esta crise não pede notificação compulsória.");
        }
        if (c.getNotificadaEm() != null) {
            throw new ResourceUnprocessableEntityException("A notificação já foi registrada em " + c.getNotificadaEm() + ".");
        }
        LocalDate em = dto.getNotificadaEm() != null ? dto.getNotificadaEm() : LocalDate.now();
        LocalDate identificada = c.getIdentificadaEm().atZone(ZoneId.systemDefault()).toLocalDate();
        if (em.isBefore(identificada) || em.isAfter(LocalDate.now())) {
            throw new ResourceBadRequestException("A notificação precisa ser entre o dia da crise e hoje.");
        }
        c.setNotificadaEm(em);
        c.setNotificacaoNumero(texto(dto.getNumero()));
        c.setNotificacaoRegistradaPorCpf(UsuarioAutenticado.cpf());
        criseRepository.save(c);
        ContextoAuditoria.detalhe("Notificação compulsória registrada.");
        return CriseLeitura.dto(c, true, etapaRepository.findDaCrise(c.getUuid()));
    }

    // ── Leitura ────────────────────────────────────────────────────────────────────────────────────

    /** As crises das unidades do escopo, as em curso primeiro; sem conteúdo onde não estiver liberado. */
    @Transactional(readOnly = true)
    public CrisesSaudeMentalDto crises(UUID unidadeId, SituacaoCriseSaudeMental situacao) {
        List<EventoCrise> todas = controleDeAcesso.filtrar(criseRepository.findTodas().stream(), c -> java.util.stream.Stream.of(c.getUnidade()),
                        CRISE, SaudeMentalService.ACOMPANHAR)
                .filter(c -> unidadeId == null || c.getUnidade().getUuid().equals(unidadeId))
                .toList();
        List<EventoCriseDto> dtos = todas.stream().map(c -> CriseLeitura.dto(c, liberada(c), List.of())).toList();
        List<EventoCriseDto> lista = dtos.stream().filter(d -> situacao == null || d.getSituacao() == situacao)
                .sorted(java.util.Comparator.comparing((EventoCriseDto d) -> d.getSituacao() != SituacaoCriseSaudeMental.EM_CURSO)
                        .thenComparing(EventoCriseDto::getIdentificadaEm, java.util.Comparator.reverseOrder()))
                .toList();
        return new CrisesSaudeMentalDto(dtos.stream().filter(d -> d.getSituacao() == SituacaoCriseSaudeMental.EM_CURSO).count(),
                dtos.stream().filter(EventoCriseDto::isNotificacaoPendente).count(), lista);
    }

    /** A crise com as etapas: para quem tem a permissão na unidade dela, a referência do caso ou o acesso justificado. */
    @Transactional(readOnly = true)
    public EventoCriseDto crise(UUID criseId) {
        EventoCrise c = buscar(criseId);
        ContextoAuditoria.paciente(c.getPaciente().getUuid());
        ContextoAuditoria.registro(c.getUuid());
        if (!liberada(c)) {
            throw new ResourceForbiddenException("Esta crise é de uma unidade fora do seu acesso; registre um acesso justificado.");
        }
        return CriseLeitura.dto(c, true, etapaRepository.findDaCrise(c.getUuid()));
    }

    // ── Apoio ──────────────────────────────────────────────────────────────────────────────────────

    private EventoCrise paraEscrever(UUID criseId) {
        EventoCrise c = buscar(criseId);
        controleDeAcesso.exigir(CRISE, c.getUnidade());
        ContextoAuditoria.paciente(c.getPaciente().getUuid());
        ContextoAuditoria.registro(c.getUuid());
        return c;
    }

    private boolean liberada(EventoCrise c) {
        if (!controleDeAcesso.ativo()) {
            return true;
        }
        String cpf = UsuarioAutenticado.cpf();
        if (cpf == null) {
            return false;
        }
        if (cpf.equals(c.getRegistradoPorCpf())) {
            return true;
        }
        Optional<Set<UUID>> visiveis = controleDeAcesso.unidadesVisiveis(CRISE, SaudeMentalService.ACOMPANHAR);
        if (visiveis.map(ids -> ids.contains(c.getUnidade().getUuid())).orElse(true)) {
            return true;
        }
        if (c.getAcompanhamento() != null && saudeMentalService.conteudoLiberado(c.getAcompanhamento())) {
            return true;
        }
        return vinculoService.acessoJustificadoVigente(cpf, c.getPaciente().getUuid());
    }

    private EventoCrise buscar(UUID uuid) {
        return criseRepository.findById(uuid).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar a crise " + uuid + " em nossos registros."));
    }

    private static String texto(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }
}
