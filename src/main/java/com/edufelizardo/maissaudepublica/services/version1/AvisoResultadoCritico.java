package com.edufelizardo.maissaudepublica.services.version1;

import com.edufelizardo.maissaudepublica.models.ItemPedidoExame;
import com.edufelizardo.maissaudepublica.models.Profissional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Aviso por e-mail ao solicitante quando um resultado crítico é liberado (ADR-0181), no padrão do aviso da auditoria
 * (ADR-0096). Sem SMTP ou sem e-mail no cadastro, não faz nada: o resultado fica em destaque na aba Para ciência e na
 * lista de críticos a comunicar. O e-mail não traz nome do paciente, exame nem valor.
 */
@Service
public class AvisoResultadoCritico {

    @Autowired
    private EnvioDeEmail envioDeEmail;

    @Value("${app.security.recuperacao-senha.url-frontend:}")
    private String frontendUrl;

    /** Devolve se o e-mail saiu. */
    public boolean avisar(ItemPedidoExame item) {
        Profissional solicitante = item.getPedido().getProfissionalSolicitante();
        if (!envioDeEmail.disponivel() || solicitante.getEmail() == null || solicitante.getEmail().isBlank()) {
            return false;
        }
        String texto = "Um exame que você pediu teve resultado com valor crítico liberado pelo laboratório.\n\n"
                + "Veja o resultado e registre a ciência na tela Laboratório, aba Para ciência"
                + (frontendUrl.isBlank() ? "." : ": " + frontendUrl + "/assistencia/laboratorio?aba=ciencia") + "\n\n"
                + "Por segurança, este e-mail não traz dados do paciente.\n";
        return envioDeEmail.enviar(solicitante.getEmail(), "[Mais Saúde Pública] Resultado crítico liberado", texto);
    }
}
