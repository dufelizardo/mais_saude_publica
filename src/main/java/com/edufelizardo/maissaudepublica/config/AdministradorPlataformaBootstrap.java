package com.edufelizardo.maissaudepublica.config;

import com.edufelizardo.maissaudepublica.models.Usuario;
import com.edufelizardo.maissaudepublica.repositories.UsuarioRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Semente do {@code AdministradorPlataforma} (ADR-0054 decisão 5): a primeira identidade de login
 * nasce fora do fluxo normal de concessão de papel/acesso. Sem Papel/Permissao ainda (ver
 * ADR-0055) — é só a primeira identidade capaz de logar, a partir da qual administradores
 * municipais/regionais/de unidade seriam criados quando essa parte do modelo existir.
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
        if (usuarioRepository.findByCpf(cpfNormalizado).isPresent()) {
            return;
        }

        Usuario administrador = new Usuario(cpfNormalizado, bootstrapNome, passwordEncoder.encode(bootstrapSenha));
        usuarioRepository.save(administrador);
        log.info("AdministradorPlataforma inicial criado (cpf terminado em {}).",
                cpfNormalizado.substring(Math.max(0, cpfNormalizado.length() - 3)));
    }
}
