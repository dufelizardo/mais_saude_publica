package com.edufelizardo.maissaudepublica.services.version1;

import com.edufelizardo.maissaudepublica.config.ContextoAuditoria;
import com.edufelizardo.maissaudepublica.config.UsuarioAutenticado;
import com.edufelizardo.maissaudepublica.exceptions.ResourceBadRequestException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceForbiddenException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceNotFoundException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceUnprocessableEntityException;
import com.edufelizardo.maissaudepublica.models.AlertaAuditoria;
import com.edufelizardo.maissaudepublica.models.Paciente;
import com.edufelizardo.maissaudepublica.models.UnidadeDeSaude;
import com.edufelizardo.maissaudepublica.models.Usuario;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.AnaliseAlertaAuditoriaRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.AlertaAuditoriaResponseDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.ResumoAlertasAuditoriaDto;
import com.edufelizardo.maissaudepublica.models.enuns.SeveridadeAlertaAuditoria;
import com.edufelizardo.maissaudepublica.models.enuns.StatusAlertaAuditoria;
import com.edufelizardo.maissaudepublica.models.enuns.TipoAlertaAuditoria;
import com.edufelizardo.maissaudepublica.repositories.AlertaAuditoriaRepository;
import com.edufelizardo.maissaudepublica.repositories.PacienteRepository;
import com.edufelizardo.maissaudepublica.repositories.UnidadeDeSaudeRepository;
import com.edufelizardo.maissaudepublica.repositories.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Consulta e análise dos alertas da auditoria (ADR-0096). Mesmo escopo da trilha (ADR-0071): quem audita uma unidade vê
 * os alertas dela e das de baixo; alerta sem unidade, só quem audita a rede inteira. Quem foi alertado não analisa o
 * próprio alerta.
 */
@Service
public class AlertaAuditoriaService {

    public static final String AUDITAR = "AUDITORIA.CONSULTAR";

    @Autowired
    private AlertaAuditoriaRepository alertaRepository;

    @Autowired
    private ControleDeAcesso controleDeAcesso;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private UnidadeDeSaudeRepository unidadeDeSaudeRepository;

    @Autowired
    private PacienteRepository pacienteRepository;

    @Autowired
    private DeteccaoAlertasAuditoriaService deteccao;

    @Transactional(readOnly = true)
    public List<AlertaAuditoriaResponseDto> listar(StatusAlertaAuditoria status, TipoAlertaAuditoria tipo,
                                                   SeveridadeAlertaAuditoria severidade) {
        List<AlertaAuditoria> alertas = visiveis().stream()
                .filter(a -> status == null || a.getStatus() == status)
                .filter(a -> tipo == null || a.getTipo() == tipo)
                .filter(a -> severidade == null || a.getSeveridade() == severidade)
                .toList();
        return comNomes(alertas);
    }

    @Transactional(readOnly = true)
    public ResumoAlertasAuditoriaDto resumo() {
        List<AlertaAuditoria> alertas = visiveis();
        Instant semana = Instant.now().minus(Duration.ofDays(7));
        return new ResumoAlertasAuditoriaDto(
                alertas.stream().filter(a -> a.getStatus() == StatusAlertaAuditoria.ABERTO).count(),
                alertas.stream().filter(a -> a.getStatus() == StatusAlertaAuditoria.ABERTO
                        && a.getSeveridade() == SeveridadeAlertaAuditoria.ALTA).count(),
                alertas.stream().filter(a -> a.getDetectadoEm().isAfter(semana)).count(),
                alertas.stream().filter(a -> a.getStatus() == StatusAlertaAuditoria.PROCEDENTE).count());
    }

    @Transactional(readOnly = true)
    public AlertaAuditoriaResponseDto buscarPorId(UUID uuid) {
        AlertaAuditoria a = visivel(uuid);
        return comNomes(List.of(a)).get(0);
    }

    @Transactional
    public AlertaAuditoriaResponseDto analisar(UUID uuid, AnaliseAlertaAuditoriaRequestDto dto) {
        AlertaAuditoria a = visivel(uuid);
        if (dto.getConclusao() == StatusAlertaAuditoria.ABERTO) {
            throw new ResourceBadRequestException("Conclua como PROCEDENTE ou IMPROCEDENTE.");
        }
        if (a.getStatus() != StatusAlertaAuditoria.ABERTO) {
            throw new ResourceUnprocessableEntityException("Este alerta já foi analisado.");
        }
        String cpf = UsuarioAutenticado.cpf();
        if (cpf != null && cpf.equals(a.getUsuarioCpf())) {
            throw new ResourceUnprocessableEntityException("Você não analisa um alerta sobre você mesmo.");
        }
        a.setStatus(dto.getConclusao());
        a.setParecer(dto.getParecer().trim());
        a.setAnalisadoPorCpf(cpf != null ? cpf : "anonimo");
        a.setAnalisadoEm(Instant.now());
        a.setAtualizadoEm(a.getAnalisadoEm());
        alertaRepository.save(a);
        ContextoAuditoria.detalhe("Alerta " + a.getTipo() + " concluído como " + a.getStatus() + ".");
        return comNomes(List.of(a)).get(0);
    }

    /** Roda a detecção na hora ("Verificar agora"); a rotina periódica continua. Devolve quantos alertas novos. */
    public int detectarAgora() {
        controleDeAcesso.exigirAlguma(AUDITAR);
        return deteccao.detectar(Instant.now()).size();
    }

    // ── Apoio ──────────────────────────────────────────────────────────────────────────────────────

    private List<AlertaAuditoria> visiveis() {
        Optional<Set<UUID>> escopo = controleDeAcesso.unidadesVisiveis(AUDITAR);
        return alertaRepository.findAllByOrderByDetectadoEmDesc().stream()
                .filter(a -> escopo.isEmpty() || (a.getUnidadeId() != null && escopo.get().contains(a.getUnidadeId())))
                .toList();
    }

    private AlertaAuditoria visivel(UUID uuid) {
        AlertaAuditoria a = alertaRepository.findById(uuid).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar um alerta da auditoria com o id " + uuid + " em nossos registros."));
        ContextoAuditoria.registro(a.getUuid());
        Optional<Set<UUID>> escopo = controleDeAcesso.unidadesVisiveis(AUDITAR);
        if (escopo.isPresent() && (a.getUnidadeId() == null || !escopo.get().contains(a.getUnidadeId()))) {
            throw new ResourceForbiddenException("Este alerta é de uma unidade fora do seu escopo de auditoria.");
        }
        return a;
    }

    private List<AlertaAuditoriaResponseDto> comNomes(List<AlertaAuditoria> alertas) {
        Map<String, String> usuarios = nomes(alertas.stream().flatMap(a -> java.util.stream.Stream.of(a.getUsuarioCpf(), a.getAnalisadoPorCpf()))
                .filter(Objects::nonNull).collect(Collectors.toSet()));
        Map<UUID, String> unidades = unidadeDeSaudeRepository.findAllById(ids(alertas, AlertaAuditoria::getUnidadeId)).stream()
                .collect(Collectors.toMap(UnidadeDeSaude::getUuid, UnidadeDeSaude::getNome));
        Map<UUID, String> pacientes = pacienteRepository.findAllById(ids(alertas, AlertaAuditoria::getPacienteId)).stream()
                .collect(Collectors.toMap(Paciente::getUuid, Paciente::getNome));
        return alertas.stream().map(a -> AlertaAuditoriaResponseDto.fromAlerta(a, usuarios.get(a.getUsuarioCpf()),
                unidades.get(a.getUnidadeId()), pacientes.get(a.getPacienteId()), usuarios.get(a.getAnalisadoPorCpf()))).toList();
    }

    private Map<String, String> nomes(Collection<String> cpfs) {
        return cpfs.isEmpty() ? Map.of() : usuarioRepository.findByCpfIn(cpfs).stream()
                .collect(Collectors.toMap(Usuario::getCpf, Usuario::getNome, (x, y) -> x));
    }

    private static Set<UUID> ids(List<AlertaAuditoria> alertas, Function<AlertaAuditoria, UUID> campo) {
        return alertas.stream().map(campo).filter(Objects::nonNull).collect(Collectors.toSet());
    }
}
