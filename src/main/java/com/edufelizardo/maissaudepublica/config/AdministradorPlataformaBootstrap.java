package com.edufelizardo.maissaudepublica.config;

import com.edufelizardo.maissaudepublica.models.AtribuicaoAcesso;
import com.edufelizardo.maissaudepublica.models.Usuario;
import com.edufelizardo.maissaudepublica.repositories.AtribuicaoAcessoRepository;
import com.edufelizardo.maissaudepublica.repositories.PapelRepository;
import com.edufelizardo.maissaudepublica.repositories.UsuarioRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * Semente do {@code AdministradorPlataforma} (ADR-0054 decisão 5): a primeira identidade de login
 * nasce fora do fluxo normal de concessão de papel/acesso, e recebe o papel de administrador da
 * plataforma em toda a rede (ADR-0066) — a partir dela são concedidos os demais acessos.
 *
 * <p>Sem CPF/senha de bootstrap configurados (variáveis de ambiente ausentes), não faz nada — não é
 * uma falha, só significa que ninguém provisionou um admin inicial ainda (caso normal em
 * local/CI/ambientes recém-criados). Nunca sobrescreve um {@code Usuario} já existente com esse CPF.
 */
@Component
@Slf4j
public class AdministradorPlataformaBootstrap implements ApplicationRunner {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private PapelRepository papelRepository;

    @Autowired
    private AtribuicaoAcessoRepository atribuicaoAcessoRepository;

    @Value("${app.security.bootstrap.cpf:}")
    private String bootstrapCpf;

    @Value("${app.security.bootstrap.senha:}")
    private String bootstrapSenha;

    @Value("${app.security.bootstrap.nome:Administrador da Plataforma}")
    private String bootstrapNome;

    @Override
    public void run(ApplicationArguments args) {
        if (bootstrapCpf == null || bootstrapCpf.isBlank()
                || bootstrapSenha == null || bootstrapSenha.isBlank()) {
            return;
        }

        String cpfNormalizado = bootstrapCpf.replaceAll("\\D", "");
        Usuario administrador = usuarioRepository.findByCpf(cpfNormalizado).orElse(null);
        if (administrador == null) {
            administrador = usuarioRepository.save(
                    new Usuario(cpfNormalizado, bootstrapNome, passwordEncoder.encode(bootstrapSenha)));
            log.info("AdministradorPlataforma inicial criado (cpf terminado em {}).",
                    cpfNormalizado.substring(Math.max(0, cpfNormalizado.length() - 3)));
        }
        garantirPapelDeAdministrador(administrador);
    }

    /**
     * O administrador inicial recebe o papel de administrador da plataforma em toda a rede, se ainda não
     * tiver nenhuma atribuição (ADR-0066) — inclusive onde o usuário já existia antes dos papéis.
     */
    private void garantirPapelDeAdministrador(Usuario administrador) {
        if (!atribuicaoAcessoRepository.findByUsuarioUuid(administrador.getUuid()).isEmpty()) {
            return;
        }
        papelRepository.findByCodigo(CatalogoDeAcesso.ADMINISTRADOR_PLATAFORMA).ifPresent(papel -> {
            AtribuicaoAcesso atribuicao = new AtribuicaoAcesso();
            atribuicao.setUsuario(administrador);
            atribuicao.setPapel(papel);
            atribuicao.setConcedidoEm(Instant.now());
            atribuicaoAcessoRepository.save(atribuicao);
            log.info("Papel de administrador da plataforma atribuído ao administrador inicial (ADR-0066).");
        });
    }
}
