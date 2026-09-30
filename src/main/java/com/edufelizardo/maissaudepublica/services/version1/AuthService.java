package com.edufelizardo.maissaudepublica.services.version1;

import com.edufelizardo.maissaudepublica.models.dtos.version1.response.UsuarioAtualResponseDto;
import com.edufelizardo.maissaudepublica.config.UsuarioAutenticado;
import com.edufelizardo.maissaudepublica.exceptions.ResourceUnauthorizedException;
import com.edufelizardo.maissaudepublica.models.Profissional;
import com.edufelizardo.maissaudepublica.models.Usuario;
import com.edufelizardo.maissaudepublica.models.enuns.TipoIdentificadorLogin;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.LoginRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.LoginResponseDto;
import com.edufelizardo.maissaudepublica.repositories.ProfissionalRepository;
import com.edufelizardo.maissaudepublica.repositories.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

/**
 * Autenticação (você está logado ou não) — corte da ADR-0055, subconjunto do modelo completo da
 * ADR-0054 (sem Papel/Permissao/EscopoAcesso ainda). Resolve tanto login por CPF quanto por
 * Matrícula funcional para o mesmo {@link Usuario}, reaproveitando
 * {@link ProfissionalRepository#findByMatricula} para o segundo caso — {@code Usuario} nunca guarda
 * a matrícula, só o cpf (ver ADR-0055 sobre a distinção entre CPF único em {@code Usuario} e não
 * único em {@code Profissional}, ADR-0017).
 */
@Service
public class AuthService {

    private static final int MAX_TENTATIVAS = 5;
    private static final Duration DURACAO_BLOQUEIO = Duration.ofMinutes(15);
    private static final String MENSAGEM_GENERICA = "CPF/matrícula ou senha inválidos.";

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ProfissionalRepository profissionalRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private AutorizacaoService autorizacaoService;

    @Value("${app.security.enabled:false}")
    private boolean securityEnabled;

    public boolean isSecurityEnabled() {
        return securityEnabled;
    }

    public LoginResponseDto login(LoginRequestDto dto) {
        String cpf = resolverCpf(dto);

        Usuario usuario = usuarioRepository.findByCpf(cpf)
                .orElseThrow(() -> new ResourceUnauthorizedException(MENSAGEM_GENERICA));

        if (!usuario.isAtivo()) {
            throw new ResourceUnauthorizedException(MENSAGEM_GENERICA);
        }

        if (usuario.getBloqueadoAte() != null && usuario.getBloqueadoAte().isAfter(Instant.now())) {
            throw new ResourceUnauthorizedException(
                    "Conta temporariamente bloqueada por excesso de tentativas. Tente novamente mais tarde.");
        }

        if (!passwordEncoder.matches(dto.getSenha(), usuario.getSenhaHash())) {
            registrarTentativaFalha(usuario);
            throw new ResourceUnauthorizedException(MENSAGEM_GENERICA);
        }

        usuario.setTentativasFalhas(0);
        usuario.setBloqueadoAte(null);
        usuario.setUltimoAcessoEm(Instant.now());
        usuarioRepository.save(usuario);

        String token = jwtService.gerarToken(usuario.getCpf(), usuario.getNome(), dto.isManterConectado());
        return new LoginResponseDto(token, jwtService.extrairExpiracao(token), usuario.getNome(), usuario.getCpf());
    }

    private String resolverCpf(LoginRequestDto dto) {
        String identificador = dto.getIdentificador();
        if (dto.getTipo() == TipoIdentificadorLogin.MATRICULA) {
            Profissional profissional = profissionalRepository.findByMatricula(identificador)
                    .orElseThrow(() -> new ResourceUnauthorizedException(MENSAGEM_GENERICA));
            return normalizarCpf(profissional.getCpf());
        }
        return normalizarCpf(identificador);
    }

    private String normalizarCpf(String cpf) {
        return cpf == null ? null : cpf.replaceAll("\\D", "");
    }

    private void registrarTentativaFalha(Usuario usuario) {
        int tentativas = usuario.getTentativasFalhas() + 1;
        usuario.setTentativasFalhas(tentativas);
        if (tentativas >= MAX_TENTATIVAS) {
            usuario.setBloqueadoAte(Instant.now().plus(DURACAO_BLOQUEIO));
            usuario.setTentativasFalhas(0);
        }
        usuarioRepository.save(usuario);
    }

    /**
     * Quem está logado (ADR-0065), com o profissional ativo de mesmo CPF quando existir. Sem usuário
     * autenticado — inclusive com o toggle de segurança desligado, em que toda requisição é anônima —
     * responde 401. Inclui papéis vigentes e permissões efetivas (ADR-0066).
     */
    @Transactional(readOnly = true)
    public UsuarioAtualResponseDto usuarioAtual() {
        String cpf = UsuarioAutenticado.cpf();
        if (cpf == null) {
            throw new ResourceUnauthorizedException("Nenhum usuário autenticado.");
        }
        Usuario usuario = usuarioRepository.findByCpf(cpf)
                .filter(Usuario::isAtivo)
                .orElseThrow(() -> new ResourceUnauthorizedException("Nenhum usuário autenticado."));
        Profissional profissional = profissionalRepository.findByCpfAndAtivoTrue(cpf).orElse(null);
        return new UsuarioAtualResponseDto(usuario.getCpf(), usuario.getNome(),
                profissional != null ? profissional.getUuid() : null,
                profissional != null ? profissional.getMatricula() : null,
                profissional != null ? profissional.getNome() : null,
                autorizacaoService.vigentes(cpf).stream()
                        .filter(a -> a.getPapel().isAtivo())
                        .map(a -> new UsuarioAtualResponseDto.AcessoVigente(a.getPapel().getCodigo(), a.getPapel().getNome(),
                                a.getUnidade() != null ? a.getUnidade().getUuid() : null,
                                a.getUnidade() != null ? a.getUnidade().getNome() : null))
                        .toList(),
                List.copyOf(autorizacaoService.permissoes(cpf)));
    }
}
