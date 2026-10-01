package com.edufelizardo.maissaudepublica.services.version1;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * Envio de e-mail (ADR-0081), por SMTP configurado nas propriedades padrão do Spring ({@code spring.mail.host},
 * {@code spring.mail.username}, {@code spring.mail.password}…). Sem {@code spring.mail.host}, não há
 * {@link JavaMailSender} e {@link #disponivel()} é falso: quem depende de e-mail se desliga.
 */
@Service
@Slf4j
public class EnvioDeEmail {

    @Autowired
    private ObjectProvider<JavaMailSender> mailSender;

    @Value("${app.email.remetente:nao-responda@mais-saude.local}")
    private String remetente;

    public boolean disponivel() {
        return mailSender.getIfAvailable() != null;
    }

    /** Envia texto simples. Falha de SMTP não derruba a requisição: fica no log, sem o conteúdo. */
    public boolean enviar(String para, String assunto, String texto) {
        JavaMailSender sender = mailSender.getIfAvailable();
        if (sender == null) {
            return false;
        }
        try {
            SimpleMailMessage mensagem = new SimpleMailMessage();
            mensagem.setFrom(remetente);
            mensagem.setTo(para);
            mensagem.setSubject(assunto);
            mensagem.setText(texto);
            sender.send(mensagem);
            return true;
        } catch (RuntimeException ex) {
            log.warn("Falha ao enviar e-mail ({}): {}", assunto, ex.getMessage());
            return false;
        }
    }
}
