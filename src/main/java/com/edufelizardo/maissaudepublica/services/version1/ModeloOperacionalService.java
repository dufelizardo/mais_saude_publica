package com.edufelizardo.maissaudepublica.services.version1;

import com.edufelizardo.maissaudepublica.config.ContextoAuditoria;
import com.edufelizardo.maissaudepublica.config.UsuarioAutenticado;
import com.edufelizardo.maissaudepublica.exceptions.ResourceBadRequestException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceConflictException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceNotFoundException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceUnprocessableEntityException;
import com.edufelizardo.maissaudepublica.models.CapacidadeAssistencial;
import com.edufelizardo.maissaudepublica.models.PerfilOperacional;
import com.edufelizardo.maissaudepublica.models.UnidadeCapacidadeAssistencial;
import com.edufelizardo.maissaudepublica.models.UnidadeDeSaude;
import com.edufelizardo.maissaudepublica.models.UnidadePerfilOperacional;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.CapacidadeAssistencialRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.CapacidadeDaUnidadeRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.EncerramentoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.PerfilDaUnidadeRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.PerfilOperacionalRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.CapacidadeAssistencialResponseDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.ModeloOperacionalUnidadeDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.PerfilOperacionalResponseDto;
import com.edufelizardo.maissaudepublica.repositories.CapacidadeAssistencialRepository;
import com.edufelizardo.maissaudepublica.repositories.PerfilOperacionalRepository;
import com.edufelizardo.maissaudepublica.repositories.UnidadeCapacidadeAssistencialRepository;
import com.edufelizardo.maissaudepublica.repositories.UnidadeDeSaudeRepository;
import com.edufelizardo.maissaudepublica.repositories.UnidadePerfilOperacionalRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Modelo operacional das unidades (ADR-0117, fatia MO1 da ADR-0115): os catálogos de perfil operacional e de capacidade
 * assistencial, e o que cada unidade opera e consegue fazer, como histórico. Os domínios perguntam
 * {@link #unidadeTemCapacidade} em vez de decidir pelo tipo da unidade (MO4).
 */
@Service
public class ModeloOperacionalService {

    @Autowired
    private PerfilOperacionalRepository perfilRepository;

    @Autowired
    private CapacidadeAssistencialRepository capacidadeRepository;

    @Autowired
    private UnidadePerfilOperacionalRepository unidadePerfilRepository;

    @Autowired
    private UnidadeCapacidadeAssistencialRepository unidadeCapacidadeRepository;

    @Autowired
    private UnidadeDeSaudeRepository unidadeDeSaudeRepository;

    @Autowired
    private ControleDeAcesso controleDeAcesso;

    // ── Catálogo de perfis ─────────────────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<PerfilOperacionalResponseDto> listarPerfis() {
        Map<UUID, Long> capacidades = capacidadeRepository.findAllComPerfil().stream().filter(c -> c.getPerfil() != null)
                .collect(Collectors.groupingBy(c -> c.getPerfil().getUuid(), Collectors.counting()));
        Map<UUID, Long> unidades = unidadesPorPerfil();
        return perfilRepository.findAllByOrderByNomeAsc().stream()
                .map(p -> perfil(p, capacidades.getOrDefault(p.getUuid(), 0L), unidades.getOrDefault(p.getUuid(), 0L))).toList();
    }

    @Transactional(readOnly = true)
    public PerfilOperacionalResponseDto buscarPerfil(UUID uuid) {
        PerfilOperacional p = perfilPorId(uuid);
        return perfil(p, capacidadeRepository.findAllComPerfil().stream()
                        .filter(c -> c.getPerfil() != null && c.getPerfil().getUuid().equals(uuid)).count(),
                unidadesPorPerfil().getOrDefault(uuid, 0L));
    }

    @Transactional
    public PerfilOperacionalResponseDto criarPerfil(PerfilOperacionalRequestDto dto) {
        String codigo = codigo(dto.getCodigo());
        if (perfilRepository.findByCodigo(codigo).isPresent()) {
            throw new ResourceConflictException("Já existe o perfil operacional " + codigo + ".");
        }
        PerfilOperacional p = new PerfilOperacional(null, codigo, dto.getNome().trim(), texto(dto.getDescricao()),
                dto.getAtivo() == null || dto.getAtivo());
        p = perfilRepository.save(p);
        ContextoAuditoria.registro(p.getUuid());
        return perfil(p, 0, 0);
    }

    /** Corrige código, nome e descrição, ou desativa: um perfil inativo não entra em unidade nova, mas os vigentes ficam. */
    @Transactional
    public PerfilOperacionalResponseDto atualizarPerfil(UUID uuid, PerfilOperacionalRequestDto dto) {
        PerfilOperacional p = perfilPorId(uuid);
        String codigo = codigo(dto.getCodigo());
        perfilRepository.findByCodigo(codigo).filter(o -> !o.getUuid().equals(uuid)).ifPresent(o -> {
            throw new ResourceConflictException("Já existe o perfil operacional " + codigo + ".");
        });
        p.setCodigo(codigo);
        p.setNome(dto.getNome().trim());
        p.setDescricao(texto(dto.getDescricao()));
        p.setAtivo(dto.getAtivo() == null || dto.getAtivo());
        perfilRepository.save(p);
        return buscarPerfil(uuid);
    }

    // ── Catálogo de capacidades ────────────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<CapacidadeAssistencialResponseDto> listarCapacidades(UUID perfilId) {
        Map<UUID, Long> unidades = unidadesPorCapacidade();
        return capacidadeRepository.findAllComPerfil().stream()
                .filter(c -> perfilId == null || (c.getPerfil() != null && c.getPerfil().getUuid().equals(perfilId)))
                .map(c -> capacidade(c, unidades.getOrDefault(c.getUuid(), 0L))).toList();
    }

    @Transactional(readOnly = true)
    public CapacidadeAssistencialResponseDto buscarCapacidade(UUID uuid) {
        return capacidade(capacidadePorId(uuid), unidadesPorCapacidade().getOrDefault(uuid, 0L));
    }

    @Transactional
    public CapacidadeAssistencialResponseDto criarCapacidade(CapacidadeAssistencialRequestDto dto) {
        String codigo = codigo(dto.getCodigo());
        if (capacidadeRepository.findByCodigo(codigo).isPresent()) {
            throw new ResourceConflictException("Já existe a capacidade assistencial " + codigo + ".");
        }
        PerfilOperacional perfil = grupo(dto.getPerfilId());
        CapacidadeAssistencial c = new CapacidadeAssistencial(null, codigo, dto.getNome().trim(), texto(dto.getDescricao()),
                dto.getAtivo() == null || dto.getAtivo(), perfil);
        c = capacidadeRepository.save(c);
        ContextoAuditoria.registro(c.getUuid());
        return capacidade(c, 0);
    }

    /**
     * Corrige código, nome, descrição e perfil, ou desativa. O perfil não muda enquanto a capacidade estiver vigente em
     * alguma unidade: a unidade poderia ficar com uma capacidade sem o perfil do grupo.
     */
    @Transactional
    public CapacidadeAssistencialResponseDto atualizarCapacidade(UUID uuid, CapacidadeAssistencialRequestDto dto) {
        CapacidadeAssistencial c = capacidadePorId(uuid);
        String codigo = codigo(dto.getCodigo());
        capacidadeRepository.findByCodigo(codigo).filter(o -> !o.getUuid().equals(uuid)).ifPresent(o -> {
            throw new ResourceConflictException("Já existe a capacidade assistencial " + codigo + ".");
        });
        UUID perfilAtual = c.getPerfil() != null ? c.getPerfil().getUuid() : null;
        if (!Objects.equals(perfilAtual, dto.getPerfilId())) {
            if (unidadeCapacidadeRepository.existsByCapacidade_UuidAndFimIsNull(uuid)) {
                throw new ResourceUnprocessableEntityException("O perfil de " + c.getNome()
                        + " não muda enquanto ela estiver vigente em alguma unidade; encerre-a nas unidades antes.");
            }
            c.setPerfil(grupo(dto.getPerfilId()));
        }
        c.setCodigo(codigo);
        c.setNome(dto.getNome().trim());
        c.setDescricao(texto(dto.getDescricao()));
        c.setAtivo(dto.getAtivo() == null || dto.getAtivo());
        capacidadeRepository.save(c);
        return buscarCapacidade(uuid);
    }

    // ── Unidade ────────────────────────────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public ModeloOperacionalUnidadeDto modelo(UUID unidadeId) {
        UnidadeDeSaude u = unidade(unidadeId);
        controleDeAcesso.exigirVisivel(u, RedeUnidadesService.VER);
        return montar(u);
    }

    @Transactional
    public ModeloOperacionalUnidadeDto incluirPerfil(UUID unidadeId, PerfilDaUnidadeRequestDto dto) {
        UnidadeDeSaude u = unidadeDeAtendimento(unidadeId);
        PerfilOperacional p = perfilPorId(dto.getPerfilId());
        if (!p.isAtivo()) {
            throw new ResourceUnprocessableEntityException("O perfil " + p.getNome() + " está inativo no catálogo.");
        }
        if (unidadePerfilRepository.existsByUnidade_UuidAndPerfil_UuidAndFimIsNull(u.getUuid(), p.getUuid())) {
            throw new ResourceConflictException(u.getNome() + " já tem o perfil " + p.getNome() + ".");
        }
        UnidadePerfilOperacional a = unidadePerfilRepository.save(new UnidadePerfilOperacional(null, u, p, inicio(dto.getInicio()),
                null, null, UsuarioAutenticado.cpf()));
        ContextoAuditoria.registro(a.getUuid());
        ContextoAuditoria.detalhe("Perfil " + p.getCodigo() + " incluído.");
        return montar(u);
    }

    /** Encerra o perfil. Antes, as capacidades do grupo dele precisam estar encerradas. */
    @Transactional
    public ModeloOperacionalUnidadeDto encerrarPerfil(UUID unidadeId, UUID associacaoId, EncerramentoRequestDto dto) {
        UnidadeDeSaude u = unidade(unidadeId);
        controleDeAcesso.exigir(RedeUnidadesService.GERENCIAR, u);
        UnidadePerfilOperacional a = unidadePerfilRepository.findById(associacaoId)
                .filter(x -> x.getUnidade().getUuid().equals(u.getUuid()))
                .orElseThrow(() -> new ResourceNotFoundException("Não foi possível encontrar o perfil " + associacaoId + " nesta unidade."));
        if (!a.vigente()) {
            throw new ResourceUnprocessableEntityException("Este perfil já foi encerrado.");
        }
        List<String> doGrupo = unidadeCapacidadeRepository.findDaUnidade(u.getUuid()).stream()
                .filter(UnidadeCapacidadeAssistencial::vigente)
                .filter(c -> c.getCapacidade().getPerfil() != null && c.getCapacidade().getPerfil().getUuid().equals(a.getPerfil().getUuid()))
                .map(c -> c.getCapacidade().getNome()).sorted().toList();
        if (!doGrupo.isEmpty()) {
            throw new ResourceUnprocessableEntityException("Encerre antes as capacidades de " + a.getPerfil().getNome() + ": "
                    + String.join(", ", doGrupo) + ".");
        }
        a.setFim(fim(dto.getFim(), a.getInicio()));
        a.setMotivoFim(dto.getMotivo().trim());
        unidadePerfilRepository.save(a);
        ContextoAuditoria.unidade(u);
        ContextoAuditoria.registro(a.getUuid());
        ContextoAuditoria.detalhe("Perfil " + a.getPerfil().getCodigo() + " encerrado.");
        return montar(u);
    }

    /** Inclui a capacidade. A de um grupo exige o perfil do grupo vigente na unidade; a transversal, não. */
    @Transactional
    public ModeloOperacionalUnidadeDto incluirCapacidade(UUID unidadeId, CapacidadeDaUnidadeRequestDto dto) {
        UnidadeDeSaude u = unidadeDeAtendimento(unidadeId);
        CapacidadeAssistencial c = capacidadePorId(dto.getCapacidadeId());
        if (!c.isAtivo()) {
            throw new ResourceUnprocessableEntityException("A capacidade " + c.getNome() + " está inativa no catálogo.");
        }
        if (unidadeCapacidadeRepository.existsByUnidade_UuidAndCapacidade_UuidAndFimIsNull(u.getUuid(), c.getUuid())) {
            throw new ResourceConflictException(u.getNome() + " já tem a capacidade " + c.getNome() + ".");
        }
        PerfilOperacional grupo = c.getPerfil();
        if (grupo != null && !unidadePerfilRepository.existsByUnidade_UuidAndPerfil_UuidAndFimIsNull(u.getUuid(), grupo.getUuid())) {
            throw new ResourceUnprocessableEntityException(c.getNome() + " é do perfil " + grupo.getNome()
                    + ", que " + u.getNome() + " não tem; inclua o perfil antes.");
        }
        UnidadeCapacidadeAssistencial a = unidadeCapacidadeRepository.save(new UnidadeCapacidadeAssistencial(null, u, c,
                inicio(dto.getInicio()), null, texto(dto.getObservacao()), null, UsuarioAutenticado.cpf()));
        ContextoAuditoria.registro(a.getUuid());
        ContextoAuditoria.detalhe("Capacidade " + c.getCodigo() + " incluída.");
        return montar(u);
    }

    @Transactional
    public ModeloOperacionalUnidadeDto encerrarCapacidade(UUID unidadeId, UUID associacaoId, EncerramentoRequestDto dto) {
        UnidadeDeSaude u = unidade(unidadeId);
        controleDeAcesso.exigir(RedeUnidadesService.GERENCIAR, u);
        UnidadeCapacidadeAssistencial a = unidadeCapacidadeRepository.findById(associacaoId)
                .filter(x -> x.getUnidade().getUuid().equals(u.getUuid()))
                .orElseThrow(() -> new ResourceNotFoundException("Não foi possível encontrar a capacidade " + associacaoId + " nesta unidade."));
        if (!a.vigente()) {
            throw new ResourceUnprocessableEntityException("Esta capacidade já foi encerrada.");
        }
        a.setFim(fim(dto.getFim(), a.getInicio()));
        a.setMotivoFim(dto.getMotivo().trim());
        unidadeCapacidadeRepository.save(a);
        ContextoAuditoria.unidade(u);
        ContextoAuditoria.registro(a.getUuid());
        ContextoAuditoria.detalhe("Capacidade " + a.getCapacidade().getCodigo() + " encerrada.");
        return montar(u);
    }

    // ── Consulta pelos domínios ────────────────────────────────────────────────────────────────────

    /** A unidade tem a capacidade vigente? É a pergunta que os domínios passam a fazer no lugar do tipo (MO4). */
    @Transactional(readOnly = true)
    public boolean unidadeTemCapacidade(UUID unidadeId, String codigoCapacidade) {
        return unidadeCapacidadeRepository.existsByUnidade_UuidAndCapacidade_CodigoAndFimIsNull(unidadeId, codigoCapacidade);
    }

    // ── Apoio ──────────────────────────────────────────────────────────────────────────────────────

    private ModeloOperacionalUnidadeDto montar(UnidadeDeSaude u) {
        List<ModeloOperacionalUnidadeDto.Perfil> perfis = unidadePerfilRepository.findDaUnidade(u.getUuid()).stream()
                .map(a -> new ModeloOperacionalUnidadeDto.Perfil(a.getUuid(), a.getPerfil().getUuid(), a.getPerfil().getCodigo(),
                        a.getPerfil().getNome(), a.getInicio(), a.getFim(), a.getMotivoFim())).toList();
        List<ModeloOperacionalUnidadeDto.Capacidade> capacidades = unidadeCapacidadeRepository.findDaUnidade(u.getUuid()).stream()
                .map(a -> {
                    PerfilOperacional g = a.getCapacidade().getPerfil();
                    return new ModeloOperacionalUnidadeDto.Capacidade(a.getUuid(), a.getCapacidade().getUuid(), a.getCapacidade().getCodigo(),
                            a.getCapacidade().getNome(), g != null ? g.getUuid() : null, g != null ? g.getNome() : null,
                            a.getInicio(), a.getFim(), a.getObservacao(), a.getMotivoFim());
                }).toList();
        Comparator<ModeloOperacionalUnidadeDto.Perfil> porNome = Comparator.comparing(ModeloOperacionalUnidadeDto.Perfil::getNome, String.CASE_INSENSITIVE_ORDER);
        Comparator<ModeloOperacionalUnidadeDto.Capacidade> porCapacidade = Comparator.comparing(ModeloOperacionalUnidadeDto.Capacidade::getNome, String.CASE_INSENSITIVE_ORDER);
        return new ModeloOperacionalUnidadeDto(u.getUuid(), u.getNome(), u.getTipo(), RedeUnidadesService.NIVEIS_DE_GESTAO.contains(u.getTipo()),
                perfis.stream().filter(p -> p.getFim() == null).sorted(porNome).toList(),
                capacidades.stream().filter(c -> c.getFim() == null).sorted(porCapacidade).toList(),
                perfis.stream().filter(p -> p.getFim() != null).sorted(Comparator.comparing(ModeloOperacionalUnidadeDto.Perfil::getFim).reversed()).toList(),
                capacidades.stream().filter(c -> c.getFim() != null).sorted(Comparator.comparing(ModeloOperacionalUnidadeDto.Capacidade::getFim).reversed()).toList());
    }

    private UnidadeDeSaude unidadeDeAtendimento(UUID unidadeId) {
        UnidadeDeSaude u = unidade(unidadeId);
        controleDeAcesso.exigir(RedeUnidadesService.GERENCIAR, u);
        if (RedeUnidadesService.NIVEIS_DE_GESTAO.contains(u.getTipo())) {
            throw new ResourceUnprocessableEntityException(u.getNome() + " é um nível de gestão (" + u.getTipo()
                    + "); perfis e capacidades são das unidades de atendimento.");
        }
        ContextoAuditoria.unidade(u);
        return u;
    }

    private PerfilOperacionalResponseDto perfil(PerfilOperacional p, long capacidades, long unidades) {
        return new PerfilOperacionalResponseDto(p.getUuid(), p.getCodigo(), p.getNome(), p.getDescricao(), p.isAtivo(), capacidades, unidades);
    }

    private CapacidadeAssistencialResponseDto capacidade(CapacidadeAssistencial c, long unidades) {
        PerfilOperacional g = c.getPerfil();
        return new CapacidadeAssistencialResponseDto(c.getUuid(), c.getCodigo(), c.getNome(), c.getDescricao(), c.isAtivo(),
                g != null ? g.getUuid() : null, g != null ? g.getCodigo() : null, g != null ? g.getNome() : null, unidades);
    }

    private Map<UUID, Long> unidadesPorPerfil() {
        return unidadePerfilRepository.findVigentesComPerfil().stream()
                .collect(Collectors.groupingBy(a -> a.getPerfil().getUuid(), Collectors.counting()));
    }

    private Map<UUID, Long> unidadesPorCapacidade() {
        return unidadeCapacidadeRepository.findAll().stream().filter(UnidadeCapacidadeAssistencial::vigente)
                .collect(Collectors.groupingBy(a -> a.getCapacidade().getUuid(), Collectors.counting()));
    }

    private PerfilOperacional grupo(UUID perfilId) {
        if (perfilId == null) {
            return null;
        }
        PerfilOperacional p = perfilPorId(perfilId);
        if (!p.isAtivo()) {
            throw new ResourceUnprocessableEntityException("O perfil " + p.getNome() + " está inativo no catálogo.");
        }
        return p;
    }

    private static LocalDate inicio(LocalDate informado) {
        LocalDate inicio = informado != null ? informado : LocalDate.now();
        if (inicio.isAfter(LocalDate.now())) {
            throw new ResourceBadRequestException("O início não pode ser no futuro.");
        }
        return inicio;
    }

    private static LocalDate fim(LocalDate informado, LocalDate inicio) {
        LocalDate fim = informado != null ? informado : LocalDate.now();
        if (fim.isBefore(inicio) || fim.isAfter(LocalDate.now())) {
            throw new ResourceBadRequestException("O fim precisa ser entre o início (" + inicio + ") e hoje.");
        }
        return fim;
    }

    private static String codigo(String codigo) {
        return codigo.trim().toUpperCase(Locale.ROOT);
    }

    private static String texto(String texto) {
        return texto == null || texto.isBlank() ? null : texto.trim();
    }

    private PerfilOperacional perfilPorId(UUID uuid) {
        return perfilRepository.findById(uuid).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar um perfil operacional com o id " + uuid + " em nossos registros."));
    }

    private CapacidadeAssistencial capacidadePorId(UUID uuid) {
        return capacidadeRepository.findById(uuid).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar uma capacidade assistencial com o id " + uuid + " em nossos registros."));
    }

    private UnidadeDeSaude unidade(UUID uuid) {
        return unidadeDeSaudeRepository.findById(uuid).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar uma unidade de saúde com o id " + uuid + " em nossos registros."));
    }
}
