package com.edufelizardo.maissaudepublica.services.version1;

import com.edufelizardo.maissaudepublica.config.CatalogoDeAcesso;
import com.edufelizardo.maissaudepublica.config.UsuarioAutenticado;
import com.edufelizardo.maissaudepublica.exceptions.ResourceBadRequestException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceConflictException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceNotFoundException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceUnprocessableEntityException;
import com.edufelizardo.maissaudepublica.models.AtribuicaoAcesso;
import com.edufelizardo.maissaudepublica.models.Papel;
import com.edufelizardo.maissaudepublica.models.Permissao;
import com.edufelizardo.maissaudepublica.models.UnidadeDeSaude;
import com.edufelizardo.maissaudepublica.models.Usuario;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.AtribuicaoAcessoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.PapelAtualizacaoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.PapelRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.RevogacaoAcessoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.UsuarioAtualizacaoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.UsuarioRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.AtribuicaoAcessoResponseDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.EscopoAcessoResponseDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.PapelResponseDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.PermissaoResponseDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.UsuarioResponseDto;
import com.edufelizardo.maissaudepublica.repositories.AtribuicaoAcessoRepository;
import com.edufelizardo.maissaudepublica.repositories.PapelRepository;
import com.edufelizardo.maissaudepublica.repositories.PermissaoRepository;
import com.edufelizardo.maissaudepublica.repositories.UnidadeDeSaudeRepository;
import com.edufelizardo.maissaudepublica.repositories.UsuarioRepository;
import com.edufelizardo.maissaudepublica.models.enuns.DimensaoPermissao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

/**
 * Catálogo de permissões, papéis e atribuições de acesso (ADR-0054, ADR-0066). Nesta fatia a API
 * existe e o cálculo de autorização ({@link AutorizacaoService}) está pronto; exigir permissão nas rotas
 * é a fatia seguinte.
 */
@Service
public class AcessoService {

    private static final String ACESSO_GERENCIAR = "ACESSO.GERENCIAR";

    @Autowired
    private PermissaoRepository permissaoRepository;

    @Autowired
    private PapelRepository papelRepository;

    @Autowired
    private AtribuicaoAcessoRepository atribuicaoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private UnidadeDeSaudeRepository unidadeDeSaudeRepository;

    @Autowired
    private ControleDeAcesso controleDeAcesso;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // ── Permissões ─────────────────────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<PermissaoResponseDto> listarPermissoes() {
        return permissaoRepository.findAll().stream()
                .sorted(Comparator.comparing(Permissao::getCodigo))
                .map(PermissaoResponseDto::fromPermissao)
                .toList();
    }

    // ── Papéis ─────────────────────────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<PapelResponseDto> listarPapeis() {
        return papelRepository.findAll().stream()
                .sorted(Comparator.comparing(Papel::getNome))
                .map(this::paraDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public PapelResponseDto buscarPapel(UUID uuid) {
        return paraDto(papel(uuid));
    }

    @Transactional
    public PapelResponseDto criarPapel(PapelRequestDto dto) {
        // Papel vale para toda a rede: só quem gerencia acesso na rede inteira o define (ADR-0067).
        controleDeAcesso.exigir(ACESSO_GERENCIAR, null);
        String codigo = dto.getCodigo().trim().toUpperCase();
        if (papelRepository.findByCodigo(codigo).isPresent()) {
            throw new ResourceConflictException("Já existe um papel com o código " + codigo + ".");
        }
        Papel papel = new Papel(codigo, dto.getNome().trim(), textoOuNulo(dto.getDescricao()), permissoes(dto.getPermissoes()));
        return paraDto(papelRepository.save(papel));
    }

    @Transactional
    public PapelResponseDto atualizarPapel(UUID uuid, PapelAtualizacaoRequestDto dto) {
        controleDeAcesso.exigir(ACESSO_GERENCIAR, null);
        Papel papel = papel(uuid);
        Set<Permissao> permissoes = permissoes(dto.getPermissoes());
        if (CatalogoDeAcesso.ADMINISTRADOR_PLATAFORMA.equals(papel.getCodigo())
                && (!dto.getAtivo() || permissoes.stream().noneMatch(p -> p.getCodigo().equals("ACESSO.GERENCIAR")))) {
            throw new ResourceUnprocessableEntityException(
                    "O papel de administrador da plataforma não pode ser desativado nem perder ACESSO.GERENCIAR: "
                            + "ninguém mais conseguiria conceder acesso.");
        }
        papel.setNome(dto.getNome().trim());
        papel.setDescricao(textoOuNulo(dto.getDescricao()));
        papel.setAtivo(dto.getAtivo());
        papel.setPermissoes(permissoes);
        return paraDto(papelRepository.save(papel));
    }

    // ── Atribuições ────────────────────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<AtribuicaoAcessoResponseDto> listarAtribuicoes(UUID usuarioId) {
        List<AtribuicaoAcesso> lista = usuarioId == null ? atribuicaoRepository.findAll()
                : atribuicaoRepository.findByUsuarioUuid(usuarioId);
        return controleDeAcesso.filtrar(lista.stream(), a -> Stream.of(a.getUnidade()), ACESSO_GERENCIAR)
                .sorted(Comparator.comparing(AtribuicaoAcesso::getConcedidoEm).reversed())
                .map(AtribuicaoAcessoResponseDto::fromAtribuicao)
                .toList();
    }

    /** Unidades de todos os níveis onde quem consulta pode conceder acesso, em ordem alfabética. */
    @Transactional(readOnly = true)
    public List<EscopoAcessoResponseDto> listarEscopos() {
        return controleDeAcesso.filtrar(unidadeDeSaudeRepository.findAll().stream(), u -> Stream.of(u), ACESSO_GERENCIAR)
                .sorted(Comparator.comparing(UnidadeDeSaude::getNome))
                .map(EscopoAcessoResponseDto::fromUnidade)
                .toList();
    }

    @Transactional
    public AtribuicaoAcessoResponseDto conceder(AtribuicaoAcessoRequestDto dto) {
        Usuario usuario = usuarioRepository.findById(dto.getUsuarioId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Não foi possível encontrar um usuário com o id " + dto.getUsuarioId() + " em nossos registros."));
        Papel papel = papel(dto.getPapelId());
        if (!papel.isAtivo()) {
            throw new ResourceUnprocessableEntityException("O papel " + papel.getCodigo() + " está inativo.");
        }
        UnidadeDeSaude unidade = dto.getUnidadeId() == null ? null : unidadeDeSaudeRepository.findById(dto.getUnidadeId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Não foi possível encontrar uma unidade de saúde com o id " + dto.getUnidadeId() + " em nossos registros."));
        exigirPodeConceder(papel, unidade);
        if (dto.getInicio() != null && dto.getFim() != null && dto.getFim().isBefore(dto.getInicio())) {
            throw new ResourceBadRequestException("O fim do acesso não pode ser antes do início.");
        }
        LocalDate hoje = LocalDate.now();
        boolean duplicada = atribuicaoRepository.findByUsuarioUuid(usuario.getUuid()).stream()
                .filter(a -> a.getRevogadoEm() == null && (a.getFim() == null || !a.getFim().isBefore(hoje)))
                .anyMatch(a -> a.getPapel().getUuid().equals(papel.getUuid())
                        && Objects.equals(a.getUnidade() != null ? a.getUnidade().getUuid() : null, dto.getUnidadeId()));
        if (duplicada) {
            throw new ResourceConflictException("Este usuário já tem o papel " + papel.getCodigo() + " neste escopo.");
        }

        AtribuicaoAcesso a = new AtribuicaoAcesso();
        a.setUsuario(usuario);
        a.setPapel(papel);
        a.setUnidade(unidade);
        a.setInicio(dto.getInicio());
        a.setFim(dto.getFim());
        a.setConcedidoEm(Instant.now());
        a.setConcedidoPorCpf(UsuarioAutenticado.cpf());
        return AtribuicaoAcessoResponseDto.fromAtribuicao(atribuicaoRepository.save(a));
    }

    @Transactional
    public AtribuicaoAcessoResponseDto revogar(UUID uuid, RevogacaoAcessoRequestDto dto) {
        AtribuicaoAcesso a = atribuicaoRepository.findById(uuid)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Não foi possível encontrar uma atribuição de acesso com o id " + uuid + " em nossos registros."));
        exigirPodeConceder(a.getPapel(), a.getUnidade());
        if (a.getRevogadoEm() != null) {
            throw new ResourceUnprocessableEntityException("Este acesso já foi revogado.");
        }
        a.setRevogadoEm(Instant.now());
        a.setRevogadoPorCpf(UsuarioAutenticado.cpf());
        a.setMotivoRevogacao(dto.getMotivo().trim());
        return AtribuicaoAcessoResponseDto.fromAtribuicao(atribuicaoRepository.save(a));
    }

    // ── Usuários ────────────────────────────────────────────────────────────────────────────────

    /** Nova identidade de login (ADR-0068): CPF válido e único; o acesso vem depois, por atribuição. */
    @Transactional
    public UsuarioResponseDto criarUsuario(UsuarioRequestDto dto) {
        String cpf = dto.getCpf().replaceAll("\\D", "");
        if (!cpfValido(cpf)) {
            throw new ResourceBadRequestException("CPF inválido.");
        }
        if (usuarioRepository.findByCpf(cpf).isPresent()) {
            throw new ResourceConflictException("Já existe um usuário com este CPF.");
        }
        Usuario usuario = new Usuario(cpf, dto.getNome().trim(), passwordEncoder.encode(dto.getSenha()));
        return UsuarioResponseDto.fromUsuario(usuarioRepository.save(usuario));
    }

    /** Nome e situação. Ninguém desativa o próprio usuário — evita trancar a administração por engano. */
    @Transactional
    public UsuarioResponseDto atualizarUsuario(UUID uuid, UsuarioAtualizacaoRequestDto dto) {
        Usuario usuario = usuario(uuid);
        if (!dto.getAtivo() && usuario.getCpf().equals(UsuarioAutenticado.cpf())) {
            throw new ResourceUnprocessableEntityException("Você não pode desativar o seu próprio usuário.");
        }
        usuario.setNome(dto.getNome().trim());
        usuario.setAtivo(dto.getAtivo());
        return UsuarioResponseDto.fromUsuario(usuarioRepository.save(usuario));
    }

    /** Libera antes do prazo o bloqueio por tentativas de senha (ADR-0055). */
    @Transactional
    public UsuarioResponseDto desbloquearUsuario(UUID uuid) {
        Usuario usuario = usuario(uuid);
        if (usuario.getBloqueadoAte() == null || !usuario.getBloqueadoAte().isAfter(Instant.now())) {
            throw new ResourceUnprocessableEntityException("Este usuário não está bloqueado.");
        }
        usuario.setBloqueadoAte(null);
        usuario.setTentativasFalhas(0);
        return UsuarioResponseDto.fromUsuario(usuarioRepository.save(usuario));
    }

    private Usuario usuario(UUID uuid) {
        return usuarioRepository.findById(uuid)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Não foi possível encontrar um usuário com o id " + uuid + " em nossos registros."));
    }

    /** Onze dígitos, não todos iguais, com os dois dígitos verificadores corretos. */
    static boolean cpfValido(String cpf) {
        if (cpf == null || !cpf.matches("\\d{11}") || cpf.chars().distinct().count() == 1) {
            return false;
        }
        for (int posicao = 9; posicao <= 10; posicao++) {
            int soma = 0;
            for (int i = 0; i < posicao; i++) {
                soma += (cpf.charAt(i) - '0') * (posicao + 1 - i);
            }
            int digito = (soma * 10) % 11 % 10;
            if (digito != cpf.charAt(posicao) - '0') {
                return false;
            }
        }
        return true;
    }

    @Transactional(readOnly = true)
    public List<UsuarioResponseDto> listarUsuarios() {
        return usuarioRepository.findAll().stream()
                .sorted(Comparator.comparing(Usuario::getNome))
                .map(UsuarioResponseDto::fromUsuario)
                .toList();
    }

    @Transactional(readOnly = true)
    public UsuarioResponseDto buscarUsuario(UUID uuid) {
        return usuarioRepository.findById(uuid).map(UsuarioResponseDto::fromUsuario)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Não foi possível encontrar um usuário com o id " + uuid + " em nossos registros."));
    }

    /**
     * Conceder ou revogar: ACESSO.GERENCIAR num escopo que cubra a unidade (sem unidade, na rede inteira).
     * Papel com permissão de administração do sistema só por quem gerencia acesso na rede inteira — um
     * gestor de unidade não cria outro administrador (ADR-0067).
     */
    private void exigirPodeConceder(Papel papel, UnidadeDeSaude unidade) {
        controleDeAcesso.exigir(ACESSO_GERENCIAR, unidade);
        if (papel.getPermissoes().stream().anyMatch(p -> p.getDimensao() == DimensaoPermissao.ADMINISTRACAO_DO_SISTEMA)) {
            controleDeAcesso.exigir(ACESSO_GERENCIAR, null);
        }
    }

    // ── Apoio ──────────────────────────────────────────────────────────────────────────────────

    private Papel papel(UUID uuid) {
        return papelRepository.findById(uuid)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Não foi possível encontrar um papel com o id " + uuid + " em nossos registros."));
    }

    /** Códigos → permissões do catálogo; código desconhecido responde 400. */
    private Set<Permissao> permissoes(List<String> codigos) {
        Set<String> pedidos = new HashSet<>(codigos.stream().map(c -> c.trim().toUpperCase()).toList());
        List<Permissao> encontradas = permissaoRepository.findByCodigoIn(pedidos);
        if (encontradas.size() != pedidos.size()) {
            Set<String> achados = new HashSet<>(encontradas.stream().map(Permissao::getCodigo).toList());
            pedidos.removeAll(achados);
            throw new ResourceBadRequestException("Permissão fora do catálogo: " + String.join(", ", pedidos) + ".");
        }
        return new HashSet<>(encontradas);
    }

    private PapelResponseDto paraDto(Papel p) {
        return PapelResponseDto.fromPapel(p, CatalogoDeAcesso.ehPapelPadrao(p.getCodigo()));
    }

    private static String textoOuNulo(String texto) {
        return texto == null || texto.isBlank() ? null : texto.trim();
    }
}
