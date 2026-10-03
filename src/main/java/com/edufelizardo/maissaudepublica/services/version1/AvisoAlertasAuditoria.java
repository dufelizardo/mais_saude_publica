package com.edufelizardo.maissaudepublica.services.version1;

import com.edufelizardo.maissaudepublica.models.AlertaAuditoria;
import com.edufelizardo.maissaudepublica.models.Profissional;
import com.edufelizardo.maissaudepublica.models.Usuario;
import com.edufelizardo.maissaudepublica.repositories.ProfissionalRepository;
import com.edufelizardo.maissaudepublica.repositories.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * Aviso por e-mail dos alertas de severidade alta (ADR-0096) para quem audita a unidade do alerta (ou a rede inteira,
 * quando o alerta não tem unidade). Sem SMTP, não faz nada: o alerta fica na tela. O e-mail não traz nome de paciente.
 */
@Service
public class AvisoAlertasAuditoria {

    private static final String AUDITAR = "AUDITORIA.CONSULTAR";
    private static final DateTimeFormatter DATA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm").withZone(ZoneId.of("America/Sao_Paulo"));

    @Autowired
    private EnvioDeEmail envioDeEmail;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ProfissionalRepository profissionalRepository;

    @Autowired
    private AutorizacaoService autorizacaoService;

    @Value("${app.security.recuperacao-senha.url-frontend:}")
    private String frontendUrl;

    public int avisar(AlertaAuditoria a) {
        if (!envioDeEmail.disponivel()) {
            return 0;
        }
        int enviados = 0;
        for (Usuario u : usuarioRepository.findAll()) {
            if (!u.isAtivo() || u.getCpf() == null || u.getCpf().equals(a.getUsuarioCpf())) {
                continue;
            }
            boolean audita = a.getUnidadeId() != null ? autorizacaoService.tem(u.getCpf(), AUDITAR, a.getUnidadeId())
                    : autorizacaoService.temNaRedeInteira(u.getCpf(), AUDITAR);
            if (!audita) {
                continue;
            }
            String email = profissionalRepository.findByCpfAndAtivoTrue(u.getCpf()).map(Profissional::getEmail).orElse(null);
            if (email == null || email.isBlank()) {
                continue;
            }
            String texto = "Um alerta de severidade alta foi detectado na auditoria do Mais Saúde Pública.\n\n"
                    + "Tipo: " + a.getTipo() + "\n"
                    + "Quem: " + a.getSujeito() + "\n"
                    + "Período: " + DATA_HORA.format(a.getPrimeiroEventoEm()) + " a " + DATA_HORA.format(a.getUltimoEventoEm()) + "\n"
                    + "O que: " + a.getDescricao() + "\n\n"
                    + "Analise na tela Auditoria, aba Alertas" + (frontendUrl.isBlank() ? "." : ": " + frontendUrl + "/administracao/auditoria?aba=alertas") + "\n";
            if (envioDeEmail.enviar(email, "[Mais Saúde Pública] Alerta da auditoria", texto)) {
                enviados++;
            }
        }
        return enviados;
    }
}
