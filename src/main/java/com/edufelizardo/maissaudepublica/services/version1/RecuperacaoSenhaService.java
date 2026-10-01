package com.edufelizardo.maissaudepublica.services.version1;

import com.edufelizardo.maissaudepublica.config.ContextoAuditoria;
import com.edufelizardo.maissaudepublica.exceptions.ResourceBadRequestException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceUnprocessableEntityException;
import com.edufelizardo.maissaudepublica.models.Profissional;
import com.edufelizardo.maissaudepublica.models.RedefinicaoSenha;
import com.edufelizardo.maissaudepublica.models.Usuario;
import com.edufelizardo.maissaudepublica.repositories.ProfissionalRepository;
import com.edufelizardo.maissaudepublica.repositories.RedefinicaoSenhaRepository;
import com.edufelizardo.maissaudepublica.repositories.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;

/**
 * Recuperação de senha sem a administração (ADR-0081): a pessoa informa o CPF, recebe por e-mail um link de uso
 * único e define uma senha nova.
 * <ul>
 *   <li>O e-mail é o do profissional ativo de mesmo CPF (ADR-0065). Sem profissional ou sem e-mail, só a
 *       administração redefine (senha provisória, ADR-0069).</li>
 *   <li>A resposta do pedido é sempre a mesma, tenha ou não cadastro — não revela quem é usuário. O que de fato
 *       aconteceu vai para a trilha de auditoria.</li>
 *   <li>O link vale 30 minutos e uma vez; só o hash do token fica no banco; no máximo 3 pedidos por hora.</li>
 *   <li>Trocar a senha pelo link encerra todas as sessões abertas (ADR-0078) e libera o bloqueio.</li>
 * </ul>
 * Só fica disponível com o login ligado, o SMTP configurado e o endereço do frontend definido.
 */
@Service
public class RecuperacaoSenhaService {

    public static final String MENSAGEM_PEDIDO = "Se o CPF tiver cadastro com e-mail, enviamos um link para criar uma "
            + "senha nova. Ele vale por 30 minutos. Sem e-mail cadastrado, procure a administração do sistema.";
    private static final String MENSAGEM_LINK_INVALIDO = "Este link não vale mais: já foi usado, expirou ou foi "
            + "substituído por um mais novo. Peça outro na tela de login.";
    private static final Duration VALIDADE = Duration.ofMinutes(30);
    private static final int PEDIDOS_POR_HORA = 3;
    private static final SecureRandom ALEATORIO = new SecureRandom();

    @Value("${app.security.enabled:false}")
    private boolean loginLigado;

    @Value("${app.security.recuperacao-senha.url-frontend:}")
    private String urlFrontend;

    @Autowired
    private EnvioDeEmail envioDeEmail;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ProfissionalRepository profissionalRepository;

    @Autowired
    private RedefinicaoSenhaRepository redefinicaoRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public boolean disponivel() {
        return loginLigado && urlFrontend != null && !urlFrontend.isBlank() && envioDeEmail.disponivel();
    }

    /** Pedido do link. Responde sempre a mesma mensagem; o resultado real vai só para a auditoria. */
    @Transactional
    public String solicitar(String cpfInformado) {
        exigirDisponivel();
        String cpf = cpfInformado.replaceAll("\\D", "");
        ContextoAuditoria.usuario(cpf);
        Usuario usuario = usuarioRepository.findByCpf(cpf).filter(Usuario::isAtivo).orElse(null);
        if (usuario == null) {
            ContextoAuditoria.detalhe("Sem usuário ativo com este CPF; nada enviado.");
            return MENSAGEM_PEDIDO;
        }
        Instant agora = Instant.now();
        if (redefinicaoRepository.countByUsuario_UuidAndCriadaEmAfter(usuario.getUuid(), agora.minus(Duration.ofHours(1)))
                >= PEDIDOS_POR_HORA) {
            ContextoAuditoria.detalhe("Limite de " + PEDIDOS_POR_HORA + " pedidos por hora; nada enviado.");
            return MENSAGEM_PEDIDO;
        }
        String email = profissionalRepository.findByCpfAndAtivoTrue(cpf).map(Profissional::getEmail)
                .filter(e -> e != null && !e.isBlank()).orElse(null);
        if (email == null) {
            ContextoAuditoria.detalhe("Sem e-mail de profissional cadastrado; nada enviado.");
            return MENSAGEM_PEDIDO;
        }

        invalidarPendentes(usuario, agora);
        String token = novoToken();
        RedefinicaoSenha r = new RedefinicaoSenha();
        r.setUsuario(usuario);
        r.setTokenHash(hash(token));
        r.setCriadaEm(agora);
        r.setExpiraEm(agora.plus(VALIDADE));
        redefinicaoRepository.save(r);

        boolean enviado = envioDeEmail.enviar(email, "Mais Saúde Pública — criar uma senha nova", textoDoEmail(usuario, token));
        ContextoAuditoria.registro(r.getUuid());
        ContextoAuditoria.detalhe(enviado ? "Link enviado para " + mascarar(email) + "." : "Falha no envio do e-mail.");
        return MENSAGEM_PEDIDO;
    }

    /** Troca a senha pelo link. Link inválido, usado ou vencido → 422. */
    @Transactional
    public void redefinir(String token, String novaSenha) {
        exigirDisponivel();
        Instant agora = Instant.now();
        RedefinicaoSenha r = redefinicaoRepository.findByTokenHash(hash(token)).orElse(null);
        if (r == null || !r.valida(agora) || !r.getUsuario().isAtivo()) {
            if (r != null) {
                ContextoAuditoria.usuario(r.getUsuario().getCpf());
            }
            throw new ResourceUnprocessableEntityException(MENSAGEM_LINK_INVALIDO);
        }
        Usuario usuario = r.getUsuario();
        ContextoAuditoria.usuario(usuario.getCpf());
        ContextoAuditoria.registro(r.getUuid());
        if (novaSenha.replaceAll("\\D", "").equals(usuario.getCpf()) && novaSenha.matches("[\\d.\\-\\s]+")) {
            throw new ResourceBadRequestException("A nova senha não pode ser o CPF.");
        }
        usuario.setSenhaHash(passwordEncoder.encode(novaSenha));
        usuario.setTrocarSenha(false);
        usuario.setSenhaAlteradaEm(agora);
        usuario.setBloqueadoAte(null);
        usuario.setTentativasFalhas(0);
        usuario.encerrarSessoes();
        usuarioRepository.save(usuario);
        invalidarPendentes(usuario, agora);
        ContextoAuditoria.detalhe("Senha redefinida pelo link; sessões abertas encerradas.");
    }

    private void exigirDisponivel() {
        if (!disponivel()) {
            throw new ResourceUnprocessableEntityException(
                    "A recuperação de senha por e-mail não está disponível neste ambiente. Procure a administração do sistema.");
        }
    }

    private void invalidarPendentes(Usuario usuario, Instant agora) {
        for (RedefinicaoSenha pendente : redefinicaoRepository.findByUsuario_UuidAndUsadaEmIsNull(usuario.getUuid())) {
            pendente.setUsadaEm(agora);
            redefinicaoRepository.save(pendente);
        }
    }

    private String textoDoEmail(Usuario usuario, String token) {
        String base = urlFrontend.endsWith("/") ? urlFrontend.substring(0, urlFrontend.length() - 1) : urlFrontend;
        return """
                Olá, %s.

                Recebemos um pedido para criar uma senha nova no Mais Saúde Pública.
                Para continuar, abra o link abaixo (vale por 30 minutos e uma única vez):

                %s/redefinir-senha?token=%s

                Se não foi você, ignore este e-mail: sua senha atual continua valendo.
                """.formatted(usuario.getNome(), base, token);
    }

    private static String novoToken() {
        byte[] bytes = new byte[32];
        ALEATORIO.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    public static String hash(String token) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException(ex);
        }
    }

    /** ana.souza@prefeitura.gov.br → a***@prefeitura.gov.br (a trilha não guarda o endereço inteiro). */
    private static String mascarar(String email) {
        int arroba = email.indexOf('@');
        return arroba <= 1 ? "***" + email.substring(Math.max(arroba, 0)) : email.charAt(0) + "***" + email.substring(arroba);
    }
}
