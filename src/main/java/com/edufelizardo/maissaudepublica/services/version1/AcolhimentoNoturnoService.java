package com.edufelizardo.maissaudepublica.services.version1;

import com.edufelizardo.maissaudepublica.config.ContextoAuditoria;
import com.edufelizardo.maissaudepublica.config.UsuarioAutenticado;
import com.edufelizardo.maissaudepublica.exceptions.ResourceBadRequestException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceConflictException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceNotFoundException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceUnprocessableEntityException;
import com.edufelizardo.maissaudepublica.models.AcompanhamentoSaudeMental;
import com.edufelizardo.maissaudepublica.models.ConfiguracaoAcolhimentoNoturno;
import com.edufelizardo.maissaudepublica.models.Paciente;
import com.edufelizardo.maissaudepublica.models.PermanenciaAcolhimentoNoturno;
import com.edufelizardo.maissaudepublica.models.UnidadeDeSaude;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.ConfiguracaoAcolhimentoNoturnoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.PermanenciaAcolhimentoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.ProrrogacaoAcolhimentoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.SaidaAcolhimentoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.AcolhimentoNoturnoDto;
import com.edufelizardo.maissaudepublica.repositories.AcompanhamentoSaudeMentalRepository;
import com.edufelizardo.maissaudepublica.repositories.ConfiguracaoAcolhimentoNoturnoRepository;
import com.edufelizardo.maissaudepublica.repositories.PacienteRepository;
import com.edufelizardo.maissaudepublica.repositories.PermanenciaAcolhimentoNoturnoRepository;
import com.edufelizardo.maissaudepublica.repositories.UnidadeCapacidadeAssistencialRepository;
import com.edufelizardo.maissaudepublica.repositories.UnidadeDeSaudeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

/**
 * O acolhimento noturno do CAPS (SM5, ADR-0153): na unidade com a capacidade "Acolhimento noturno", as vagas e a
 * permanência máxima configuradas pela gestão da rede; a entrada do paciente em acompanhamento de saúde mental na
 * unidade, a saída prevista (além do máximo, com a justificativa), a prorrogação e a saída com o motivo.
 */
@Service
public class AcolhimentoNoturnoService {

    static final String CAPACIDADE = "ACOLHIMENTO_NOTURNO";
    private static final String ACOMPANHAR = SaudeMentalService.ACOMPANHAR;

    @Autowired private ConfiguracaoAcolhimentoNoturnoRepository configuracaoRepository;
    @Autowired private PermanenciaAcolhimentoNoturnoRepository permanenciaRepository;
    @Autowired private UnidadeCapacidadeAssistencialRepository capacidadeRepository;
    @Autowired private UnidadeDeSaudeRepository unidadeRepository;
    @Autowired private PacienteRepository pacienteRepository;
    @Autowired private AcompanhamentoSaudeMentalRepository acompanhamentoRepository;
    @Autowired private ControleDeAcesso controleDeAcesso;
    @Autowired private VinculoAssistencialService vinculoService;

    /** A ocupação da unidade: vagas, quem está acolhido e as saídas dos últimos 30 dias. */
    @Transactional(readOnly = true)
    public AcolhimentoNoturnoDto painel(UUID unidadeId) {
        UnidadeDeSaude u = unidade(unidadeId);
        controleDeAcesso.exigir(ACOMPANHAR, u);
        return montar(u);
    }

    private AcolhimentoNoturnoDto montar(UnidadeDeSaude u) {
        UUID unidadeId = u.getUuid();
        ConfiguracaoAcolhimentoNoturno c = configuracaoRepository.findByUnidade_Uuid(unidadeId).orElse(null);
        List<PermanenciaAcolhimentoNoturno> abertas = permanenciaRepository.findAbertasDaUnidade(unidadeId);
        LocalDate hoje = LocalDate.now();
        return new AcolhimentoNoturnoDto(u.getUuid(), u.getNome(), temCapacidade(unidadeId), c != null ? c.getVagas() : null,
                c != null ? c.getPermanenciaMaximaDias() : null, abertas.size(), abertas.stream().map(p -> dto(p, c, hoje)).toList(),
                permanenciaRepository.findSaidasDaUnidadeDesde(unidadeId, hoje.minusDays(30)).stream().map(p -> dto(p, c, hoje)).toList());
    }

    /** As vagas e a permanência máxima: na unidade com a capacidade (422), sem ficar abaixo da ocupação (422); a resposta não tem nomes. */
    @Transactional
    public AcolhimentoNoturnoDto configurar(UUID unidadeId, ConfiguracaoAcolhimentoNoturnoRequestDto dto) {
        UnidadeDeSaude u = unidade(unidadeId);
        controleDeAcesso.exigir(RedeUnidadesService.GERENCIAR, u);
        exigirCapacidade(u);
        long ocupadas = permanenciaRepository.countByUnidade_UuidAndSaidaIsNull(unidadeId);
        if (dto.getVagas() < ocupadas) {
            throw new ResourceUnprocessableEntityException("A unidade tem " + ocupadas + " acolhido(s) agora: as vagas não podem ficar abaixo disso.");
        }
        ConfiguracaoAcolhimentoNoturno c = configuracaoRepository.findByUnidade_Uuid(unidadeId).orElseGet(() -> {
            ConfiguracaoAcolhimentoNoturno n = new ConfiguracaoAcolhimentoNoturno();
            n.setUnidade(u);
            return n;
        });
        c.setVagas(dto.getVagas());
        c.setPermanenciaMaximaDias(dto.getPermanenciaMaximaDias());
        c.setAtualizadaEm(Instant.now());
        c.setRegistradoPorCpf(UsuarioAutenticado.cpf());
        configuracaoRepository.save(c);
        ContextoAuditoria.detalhe("Acolhimento noturno: " + dto.getVagas() + " vaga(s), até " + dto.getPermanenciaMaximaDias() + " dia(s).");
        // Quem configura é a administração da rede, sem acesso clínico: a resposta vai sem os nomes dos acolhidos.
        AcolhimentoNoturnoDto d = montar(u);
        d.setAcolhidos(List.of());
        d.setSaidasRecentes(List.of());
        return d;
    }

    /**
     * A entrada: unidade com a capacidade e configurada (422), paciente em acompanhamento de saúde mental nela (422),
     * uma permanência aberta por paciente (409), com vaga (422); a saída prevista não antes da entrada (400) e, além da
     * permanência máxima, com a justificativa (422).
     */
    @Transactional
    public AcolhimentoNoturnoDto.Permanencia entrar(PermanenciaAcolhimentoRequestDto dto) {
        UnidadeDeSaude u = unidade(dto.getUnidadeId());
        controleDeAcesso.exigir(ACOMPANHAR, u);
        exigirCapacidade(u);
        ConfiguracaoAcolhimentoNoturno c = configuracaoRepository.findByUnidade_Uuid(u.getUuid()).orElseThrow(() ->
                new ResourceUnprocessableEntityException("Configure as vagas e a permanência máxima do acolhimento noturno de " + u.getNome() + "."));
        Paciente p = pacienteRepository.findById(dto.getPacienteId()).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar um paciente com o id " + dto.getPacienteId() + " em nossos registros."));
        ContextoAuditoria.paciente(p.getUuid());
        AcompanhamentoSaudeMental caso = acompanhamentoRepository.findVigenteDoPaciente(p.getUuid())
                .filter(a -> a.getUnidade().getUuid().equals(u.getUuid()))
                .orElseThrow(() -> new ResourceUnprocessableEntityException(p.getNome() + " não está em acompanhamento de saúde mental em " + u.getNome() + "."));
        if (permanenciaRepository.existsByPaciente_UuidAndSaidaIsNull(p.getUuid())) {
            throw new ResourceConflictException(p.getNome() + " já está em acolhimento noturno.");
        }
        if (permanenciaRepository.countByUnidade_UuidAndSaidaIsNull(u.getUuid()) >= c.getVagas()) {
            throw new ResourceUnprocessableEntityException("Sem vaga no acolhimento noturno de " + u.getNome() + " (" + c.getVagas() + " ocupada(s)).");
        }
        LocalDate entrada = dto.getEntrada() != null ? dto.getEntrada() : LocalDate.now();
        if (entrada.isAfter(LocalDate.now())) {
            throw new ResourceBadRequestException("A entrada não pode ser no futuro.");
        }
        String justificativa = prazo(entrada, dto.getSaidaPrevista(), dto.getJustificativa(), c);
        PermanenciaAcolhimentoNoturno n = new PermanenciaAcolhimentoNoturno();
        n.setUnidade(u);
        n.setPaciente(p);
        n.setAcompanhamento(caso);
        n.setEntrada(entrada);
        n.setSaidaPrevista(dto.getSaidaPrevista());
        n.setJustificativaPrazo(justificativa);
        n.setRegistradaEm(Instant.now());
        n.setRegistradoPorCpf(UsuarioAutenticado.cpf());
        n = permanenciaRepository.save(n);
        ContextoAuditoria.registro(n.getUuid());
        return dto(n, c, LocalDate.now());
    }

    /** A nova saída prevista: só em aberto (422); além da permanência máxima, com a justificativa (422). */
    @Transactional
    public AcolhimentoNoturnoDto.Permanencia prorrogar(UUID uuid, ProrrogacaoAcolhimentoRequestDto dto) {
        PermanenciaAcolhimentoNoturno n = aberta(uuid);
        ConfiguracaoAcolhimentoNoturno c = configuracaoRepository.findByUnidade_Uuid(n.getUnidade().getUuid()).orElse(null);
        String justificativa = prazo(n.getEntrada(), dto.getSaidaPrevista(), dto.getJustificativa(), c);
        n.setSaidaPrevista(dto.getSaidaPrevista());
        if (justificativa != null) {
            n.setJustificativaPrazo(justificativa);
        }
        ContextoAuditoria.detalhe("Saída prevista para " + dto.getSaidaPrevista() + ".");
        return dto(permanenciaRepository.save(n), c, LocalDate.now());
    }

    /** A saída, com o motivo: não antes da entrada nem no futuro (400). */
    @Transactional
    public AcolhimentoNoturnoDto.Permanencia sair(UUID uuid, SaidaAcolhimentoRequestDto dto) {
        PermanenciaAcolhimentoNoturno n = aberta(uuid);
        LocalDate saida = dto.getSaida() != null ? dto.getSaida() : LocalDate.now();
        if (saida.isBefore(n.getEntrada()) || saida.isAfter(LocalDate.now())) {
            throw new ResourceBadRequestException("A saída precisa ser entre a entrada (" + n.getEntrada() + ") e hoje.");
        }
        n.setSaida(saida);
        n.setMotivoSaida(dto.getMotivo().trim());
        ContextoAuditoria.detalhe("Saída do acolhimento noturno.");
        return dto(permanenciaRepository.save(n), configuracaoRepository.findByUnidade_Uuid(n.getUnidade().getUuid()).orElse(null), LocalDate.now());
    }

    /** As permanências do paciente, com as regras do prontuário. */
    @Transactional(readOnly = true)
    public List<AcolhimentoNoturnoDto.Permanencia> doPaciente(UUID pacienteId) {
        if (!pacienteRepository.existsById(pacienteId)) {
            throw new ResourceNotFoundException("Não foi possível encontrar um paciente com o id " + pacienteId + " em nossos registros.");
        }
        controleDeAcesso.exigirAlguma(ACOMPANHAR);
        ContextoAuditoria.paciente(pacienteId);
        vinculoService.exigirVinculo(pacienteId);
        LocalDate hoje = LocalDate.now();
        return permanenciaRepository.findDoPaciente(pacienteId).stream()
                .map(p -> dto(p, configuracaoRepository.findByUnidade_Uuid(p.getUnidade().getUuid()).orElse(null), hoje)).toList();
    }

    // ── Apoio ──────────────────────────────────────────────────────────────────────────────────

    /** A saída prevista não antes da entrada (400); além da permanência máxima, pede a justificativa (422). */
    private static String prazo(LocalDate entrada, LocalDate saidaPrevista, String justificativa, ConfiguracaoAcolhimentoNoturno c) {
        if (saidaPrevista.isBefore(entrada)) {
            throw new ResourceBadRequestException("A saída prevista não pode ser antes da entrada.");
        }
        String texto = justificativa == null || justificativa.isBlank() ? null : justificativa.trim();
        if (c != null && ChronoUnit.DAYS.between(entrada, saidaPrevista) > c.getPermanenciaMaximaDias() && texto == null) {
            throw new ResourceUnprocessableEntityException("A saída prevista passa da permanência máxima de " + c.getPermanenciaMaximaDias()
                    + " dia(s): informe a justificativa.");
        }
        return texto;
    }

    private PermanenciaAcolhimentoNoturno aberta(UUID uuid) {
        PermanenciaAcolhimentoNoturno n = permanenciaRepository.findById(uuid).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar a permanência " + uuid + " em nossos registros."));
        controleDeAcesso.exigir(ACOMPANHAR, n.getUnidade());
        ContextoAuditoria.paciente(n.getPaciente().getUuid());
        ContextoAuditoria.registro(n.getUuid());
        if (n.getSaida() != null) {
            throw new ResourceUnprocessableEntityException("A permanência já foi encerrada.");
        }
        return n;
    }

    private boolean temCapacidade(UUID unidadeId) {
        return capacidadeRepository.existsByUnidade_UuidAndCapacidade_CodigoAndFimIsNull(unidadeId, CAPACIDADE);
    }

    private void exigirCapacidade(UnidadeDeSaude u) {
        if (!temCapacidade(u.getUuid())) {
            throw new ResourceUnprocessableEntityException("A unidade " + u.getNome() + " não tem acolhimento noturno (capacidade do modelo operacional).");
        }
    }

    private UnidadeDeSaude unidade(UUID id) {
        return unidadeRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar a unidade " + id + " em nossos registros."));
    }

    private static AcolhimentoNoturnoDto.Permanencia dto(PermanenciaAcolhimentoNoturno p, ConfiguracaoAcolhimentoNoturno c, LocalDate hoje) {
        LocalDate fim = p.getSaida() != null ? p.getSaida() : hoje;
        return new AcolhimentoNoturnoDto.Permanencia(p.getUuid(), p.getPaciente().getUuid(), p.getPaciente().getNome(), p.getUnidade().getUuid(),
                p.getUnidade().getNome(), p.getEntrada(), p.getSaidaPrevista(), p.getSaida(), ChronoUnit.DAYS.between(p.getEntrada(), fim),
                p.getSaida() == null && p.getSaidaPrevista().isBefore(hoje),
                c != null && ChronoUnit.DAYS.between(p.getEntrada(), p.getSaidaPrevista()) > c.getPermanenciaMaximaDias(),
                p.getJustificativaPrazo(), p.getMotivoSaida());
    }
}
