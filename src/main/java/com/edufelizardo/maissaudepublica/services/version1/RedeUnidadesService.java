package com.edufelizardo.maissaudepublica.services.version1;

import com.edufelizardo.maissaudepublica.config.ContextoAuditoria;
import com.edufelizardo.maissaudepublica.config.UsuarioAutenticado;
import com.edufelizardo.maissaudepublica.exceptions.ResourceBadRequestException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceConflictException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceNotFoundException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceUnprocessableEntityException;
import com.edufelizardo.maissaudepublica.models.Endereco;
import com.edufelizardo.maissaudepublica.models.EventoSituacaoUnidade;
import com.edufelizardo.maissaudepublica.models.HorarioUnidade;
import com.edufelizardo.maissaudepublica.models.Leito;
import com.edufelizardo.maissaudepublica.models.Lotacao;
import com.edufelizardo.maissaudepublica.models.Setor;
import com.edufelizardo.maissaudepublica.models.UnidadeDeSaude;
import com.edufelizardo.maissaudepublica.models.Usuario;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.HorariosUnidadeRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.SituacaoUnidadeRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.TurnoHorarioDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.UnidadeCadastroRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.FichaUnidadeDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.RedeUnidadeResumoDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.RedeUnidadesResponseDto;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoLeito;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoOperacional;
import com.edufelizardo.maissaudepublica.models.enuns.TipoUnidadeDeSaude;
import com.edufelizardo.maissaudepublica.repositories.EventoSituacaoUnidadeRepository;
import com.edufelizardo.maissaudepublica.repositories.HorarioUnidadeRepository;
import com.edufelizardo.maissaudepublica.repositories.LeitoRepository;
import com.edufelizardo.maissaudepublica.repositories.LotacaoRepository;
import com.edufelizardo.maissaudepublica.repositories.ProfissionalRepository;
import com.edufelizardo.maissaudepublica.repositories.SetorRepository;
import com.edufelizardo.maissaudepublica.repositories.UnidadeDeSaudeRepository;
import com.edufelizardo.maissaudepublica.repositories.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Rede de unidades pela tela Equipamentos de Saúde (ADR-0101): lista com resumo e indicadores, ficha, cadastro e edição por
 * id, horário estruturado e situação operacional. As rotas antigas por nome continuam.
 */
@Service
public class RedeUnidadesService {

    public static final String GERENCIAR = "ORGANIZACAO.GERENCIAR";
    static final String[] VER = {GERENCIAR, "ADMINISTRATIVO.CONSULTAR"};
    static final Set<TipoUnidadeDeSaude> NIVEIS_DE_GESTAO = Set.of(TipoUnidadeDeSaude.FEDERAL, TipoUnidadeDeSaude.ESTADUAL,
            TipoUnidadeDeSaude.MUNICIPAL, TipoUnidadeDeSaude.REGIONAL);
    private static final int TURNOS_POR_DIA = 3;

    @Autowired
    private UnidadeDeSaudeRepository unidadeDeSaudeRepository;

    @Autowired
    private HorarioUnidadeRepository horarioRepository;

    @Autowired
    private EventoSituacaoUnidadeRepository eventoRepository;

    @Autowired
    private LotacaoRepository lotacaoRepository;

    @Autowired
    private SetorRepository setorRepository;

    @Autowired
    private LeitoRepository leitoRepository;

    @Autowired
    private ProfissionalRepository profissionalRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ControleDeAcesso controleDeAcesso;

    // ── Leitura ────────────────────────────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public RedeUnidadesResponseDto rede() {
        List<UnidadeDeSaude> unidades = controleDeAcesso.filtrar(unidadeDeSaudeRepository.findAll().stream(), Stream::of, VER)
                .sorted(Comparator.comparing(UnidadeDeSaude::getNome, String.CASE_INSENSITIVE_ORDER)).toList();
        Contagens c = contagens();
        List<RedeUnidadeResumoDto> rede = unidades.stream().map(u -> resumo(u, c)).toList();
        List<UnidadeDeSaude> deAtendimento = unidades.stream().filter(u -> !NIVEIS_DE_GESTAO.contains(u.getTipo())).toList();
        Map<TipoUnidadeDeSaude, Long> porTipo = new EnumMap<>(TipoUnidadeDeSaude.class);
        deAtendimento.forEach(u -> porTipo.merge(u.getTipo(), 1L, Long::sum));
        return new RedeUnidadesResponseDto(deAtendimento.size(), deAtendimento.stream().filter(UnidadeDeSaude::isAtivo).count(),
                deAtendimento.stream().filter(u -> u.isAtivo() && u.getSituacaoOperacional() == SituacaoOperacional.EM_OPERACAO).count(),
                deAtendimento.stream().filter(u -> u.isAtivo() && u.getSituacaoOperacional() != SituacaoOperacional.EM_OPERACAO).count(),
                porTipo, rede);
    }

    @Transactional(readOnly = true)
    public FichaUnidadeDto ficha(UUID uuid) {
        UnidadeDeSaude u = buscar(uuid);
        controleDeAcesso.exigirVisivel(u, VER);
        return montarFicha(u);
    }

    // ── Escrita ────────────────────────────────────────────────────────────────────────────────────

    @Transactional
    public FichaUnidadeDto criar(UnidadeCadastroRequestDto dto) {
        UnidadeDeSaude superior = dto.getUnidadeSuperiorId() == null ? null : buscar(dto.getUnidadeSuperiorId());
        if (superior != null) {
            controleDeAcesso.exigir(GERENCIAR, superior);
            TipoUnidadeDeSaude esperado = AbstractHierarquicoService.tipoSuperiorEsperadoPara(dto.getTipo());
            if (esperado != null && superior.getTipo() != esperado) {
                throw new ResourceUnprocessableEntityException("A unidade superior precisa ser do nível " + esperado
                        + ", mas " + superior.getNome() + " é do nível " + superior.getTipo() + ".");
            }
        } else {
            controleDeAcesso.exigirAlguma(GERENCIAR);
        }
        String nome = dto.getNome().trim();
        if (unidadeDeSaudeRepository.findByNome(nome).isPresent()) {
            throw new ResourceConflictException("Já existe uma unidade com o nome " + nome + ".");
        }
        UnidadeDeSaude u = new UnidadeDeSaude();
        u.setNome(nome);
        u.setTipo(dto.getTipo());
        u.setUnidadeSuperior(superior);
        u.setAtivo(true);
        u.setSituacaoOperacional(SituacaoOperacional.EM_OPERACAO);
        u.setFunciona24h(dto.getTipo() == TipoUnidadeDeSaude.HOSPITAL || dto.getTipo() == TipoUnidadeDeSaude.UPA);
        aplicar(u, dto);
        u = unidadeDeSaudeRepository.save(u);
        ContextoAuditoria.unidade(u);
        ContextoAuditoria.registro(u.getUuid());
        return montarFicha(u);
    }

    /** Edita nome, CNES, endereço, contato, responsável e supervisão regional. Tipo e unidade superior ficam. */
    @Transactional
    public FichaUnidadeDto atualizar(UUID uuid, UnidadeCadastroRequestDto dto) {
        UnidadeDeSaude u = buscar(uuid);
        controleDeAcesso.exigir(GERENCIAR, u);
        if (dto.getTipo() != u.getTipo()) {
            throw new ResourceUnprocessableEntityException("O tipo da unidade não muda pela edição.");
        }
        String nome = dto.getNome().trim();
        unidadeDeSaudeRepository.findByNome(nome).filter(o -> !o.getUuid().equals(u.getUuid())).ifPresent(o -> {
            throw new ResourceConflictException("Já existe uma unidade com o nome " + nome + ".");
        });
        u.setNome(nome);
        aplicar(u, dto);
        return montarFicha(unidadeDeSaudeRepository.save(u));
    }

    /** Troca o horário estruturado inteiro: turnos no mesmo dia, sem sobrepor, até três por dia. */
    @Transactional
    public FichaUnidadeDto horarios(UUID uuid, HorariosUnidadeRequestDto dto) {
        UnidadeDeSaude u = buscar(uuid);
        controleDeAcesso.exigir(GERENCIAR, u);
        Map<DayOfWeek, List<TurnoHorarioDto>> porDia = dto.getTurnos().stream().collect(Collectors.groupingBy(TurnoHorarioDto::getDiaSemana));
        for (Map.Entry<DayOfWeek, List<TurnoHorarioDto>> e : porDia.entrySet()) {
            List<TurnoHorarioDto> turnos = new ArrayList<>(e.getValue());
            if (turnos.size() > TURNOS_POR_DIA) {
                throw new ResourceBadRequestException("Até " + TURNOS_POR_DIA + " turnos por dia (" + e.getKey() + ").");
            }
            turnos.sort(Comparator.comparing(TurnoHorarioDto::getAbre));
            for (int i = 0; i < turnos.size(); i++) {
                TurnoHorarioDto t = turnos.get(i);
                if (!t.getFecha().isAfter(t.getAbre())) {
                    throw new ResourceBadRequestException("O turno de " + e.getKey() + " precisa fechar depois de abrir; para funcionar a noite toda, marque 24 horas.");
                }
                if (i > 0 && t.getAbre().isBefore(turnos.get(i - 1).getFecha())) {
                    throw new ResourceBadRequestException("Os turnos de " + e.getKey() + " se sobrepõem.");
                }
            }
        }
        horarioRepository.deleteByUnidade_Uuid(u.getUuid());
        horarioRepository.flush();
        for (TurnoHorarioDto t : dto.getTurnos()) {
            horarioRepository.save(new HorarioUnidade(null, u, t.getDiaSemana(), t.getAbre(), t.getFecha()));
        }
        u.setFunciona24h(dto.isFunciona24h());
        unidadeDeSaudeRepository.save(u);
        ContextoAuditoria.detalhe("Horário: " + dto.getTurnos().size() + " turno(s)" + (dto.isFunciona24h() ? ", 24 horas" : "") + ".");
        return montarFicha(u);
    }

    /** Muda a situação operacional. Fora de operação exige motivo; volta à operação limpa motivo e previsão. */
    @Transactional
    public FichaUnidadeDto situacao(UUID uuid, SituacaoUnidadeRequestDto dto) {
        UnidadeDeSaude u = buscar(uuid);
        controleDeAcesso.exigir(GERENCIAR, u);
        SituacaoOperacional anterior = u.getSituacaoOperacional();
        String motivo = dto.getMotivo() == null || dto.getMotivo().isBlank() ? null : dto.getMotivo().trim();
        if (dto.getSituacao() != SituacaoOperacional.EM_OPERACAO && motivo == null) {
            throw new ResourceBadRequestException("Informe o motivo da situação " + dto.getSituacao() + ".");
        }
        if (dto.getPrevisaoRetorno() != null && dto.getPrevisaoRetorno().isBefore(LocalDate.now())) {
            throw new ResourceBadRequestException("A previsão de retorno não pode ser no passado.");
        }
        if (anterior == dto.getSituacao() && Objects.equals(motivo, u.getMotivoSituacao())
                && Objects.equals(dto.getPrevisaoRetorno(), u.getPrevisaoRetorno())) {
            throw new ResourceUnprocessableEntityException("A unidade já está nessa situação.");
        }
        boolean operando = dto.getSituacao() == SituacaoOperacional.EM_OPERACAO;
        u.setSituacaoOperacional(dto.getSituacao());
        u.setMotivoSituacao(operando ? null : motivo);
        u.setPrevisaoRetorno(operando ? null : dto.getPrevisaoRetorno());
        unidadeDeSaudeRepository.save(u);
        eventoRepository.save(new EventoSituacaoUnidade(null, u, anterior, dto.getSituacao(), motivo, operando ? null : dto.getPrevisaoRetorno(),
                Instant.now(), UsuarioAutenticado.cpf()));
        ContextoAuditoria.detalhe("Situação: " + anterior + " → " + dto.getSituacao() + ".");
        return montarFicha(u);
    }

    // ── Apoio ──────────────────────────────────────────────────────────────────────────────────────

    private void aplicar(UnidadeDeSaude u, UnidadeCadastroRequestDto dto) {
        String cnes = dto.getCnes() == null || dto.getCnes().isBlank() ? null : dto.getCnes().trim();
        if (cnes != null) {
            unidadeDeSaudeRepository.findAll().stream()
                    .filter(o -> cnes.equals(o.getCnes()) && !o.getUuid().equals(u.getUuid())).findFirst()
                    .ifPresent(o -> {
                        throw new ResourceConflictException("O CNES " + cnes + " já é da unidade " + o.getNome() + ".");
                    });
        }
        u.setCnes(cnes);
        if (dto.getSupervisaoRegionalId() != null) {
            UnidadeDeSaude regional = buscar(dto.getSupervisaoRegionalId());
            if (regional.getTipo() != TipoUnidadeDeSaude.REGIONAL) {
                throw new ResourceUnprocessableEntityException("A supervisão regional precisa ser uma unidade do nível REGIONAL.");
            }
            u.setSupervisaoRegional(regional);
        } else {
            u.setSupervisaoRegional(null);
        }
        if (dto.getEndereco() != null) {
            u.setEndereco(new Endereco(dto.getEndereco()));
        }
        u.setSaudeTelefones(dto.getTelefones() == null ? new HashSet<>() : new HashSet<>(dto.getTelefones()));
        u.setEmail(dto.getEmail() == null || dto.getEmail().isBlank() ? null : dto.getEmail().trim());
        String cpf = dto.getResponsavelCpf() == null || dto.getResponsavelCpf().isBlank() ? null : dto.getResponsavelCpf().replaceAll("\\D", "");
        if (!Objects.equals(cpf, u.getResponsavelCpf())) {
            u.setResponsavelCpf(cpf);
            u.setResponsavel(cpf == null ? null : profissionalRepository.findByCpfAndAtivoTrue(cpf).orElse(null));
        }
    }

    private record Contagens(Map<UUID, Long> lotados, Map<UUID, Long> setores, Map<UUID, Long> leitos, Map<UUID, Long> ocupados,
                             Set<UUID> comHorario) {
    }

    private Contagens contagens() {
        Map<UUID, Long> lotados = lotacaoRepository.findVigentesComUnidadeECargo().stream()
                .filter(l -> l.getUnidade() != null)
                .collect(Collectors.groupingBy(l -> l.getUnidade().getUuid(), Collectors.mapping(l -> l.getProfissional().getUuid(),
                        Collectors.collectingAndThen(Collectors.toSet(), s -> (long) s.size()))));
        Map<UUID, Long> setores = setorRepository.findAll().stream().filter(s -> s.getUnidade() != null && s.isAtivo())
                .collect(Collectors.groupingBy(s -> s.getUnidade().getUuid(), Collectors.counting()));
        List<Leito> leitosAtivos = leitoRepository.findAll().stream().filter(Leito::isAtivo).toList();
        Map<UUID, Long> leitos = leitosAtivos.stream().collect(Collectors.groupingBy(l -> l.getUnidade().getUuid(), Collectors.counting()));
        Map<UUID, Long> ocupados = leitosAtivos.stream().filter(l -> l.getSituacao() == SituacaoLeito.OCUPADO)
                .collect(Collectors.groupingBy(l -> l.getUnidade().getUuid(), Collectors.counting()));
        Set<UUID> comHorario = horarioRepository.findAll().stream().map(h -> h.getUnidade().getUuid()).collect(Collectors.toSet());
        return new Contagens(lotados, setores, leitos, ocupados, comHorario);
    }

    private RedeUnidadeResumoDto resumo(UnidadeDeSaude u, Contagens c) {
        Endereco e = u.getEndereco();
        String endereco = e == null ? null : Stream.of(
                        e.getLogradouro() == null ? null : e.getLogradouro() + (e.getNumeroLogradouro() != null ? ", " + e.getNumeroLogradouro() : ""),
                        e.getBairro(), e.getCidade() == null ? null : e.getCidade() + (e.getEstado() != null ? "/" + e.getEstado() : ""))
                .filter(Objects::nonNull).filter(s -> !s.isBlank()).collect(Collectors.joining(" · "));
        String telefone = u.getSaudeTelefones() == null ? null : u.getSaudeTelefones().stream().sorted().findFirst().orElse(null);
        return new RedeUnidadeResumoDto(u.getUuid(), u.getNome(), u.getTipo(), u.getCnes(), u.getSituacaoOperacional(), u.getMotivoSituacao(),
                u.getPrevisaoRetorno(), u.isAtivo(), u.isFunciona24h(), u.getMunicipio(), u.getEstado(),
                u.getUnidadeSuperior() != null ? u.getUnidadeSuperior().getUuid() : null,
                u.getUnidadeSuperior() != null ? u.getUnidadeSuperior().getNome() : null,
                u.getSupervisaoRegional() != null ? u.getSupervisaoRegional().getUuid() : null,
                u.getSupervisaoRegional() != null ? u.getSupervisaoRegional().getNome() : null,
                endereco == null || endereco.isBlank() ? null : endereco, telefone, u.getEmail(),
                c.lotados().getOrDefault(u.getUuid(), 0L), c.setores().getOrDefault(u.getUuid(), 0L),
                c.leitos().getOrDefault(u.getUuid(), 0L), c.ocupados().getOrDefault(u.getUuid(), 0L), c.comHorario().contains(u.getUuid()));
    }

    private FichaUnidadeDto montarFicha(UnidadeDeSaude u) {
        Endereco e = u.getEndereco();
        List<TurnoHorarioDto> turnos = horarioRepository.findByUnidade_UuidOrderByDiaSemanaAscAbreAsc(u.getUuid()).stream()
                .map(h -> new TurnoHorarioDto(h.getDiaSemana(), h.getAbre(), h.getFecha())).toList();
        List<FichaUnidadeDto.Vinculo> subordinadas = unidadeDeSaudeRepository.findAll().stream()
                .filter(o -> (o.getUnidadeSuperior() != null && o.getUnidadeSuperior().getUuid().equals(u.getUuid()))
                        || (o.getSupervisaoRegional() != null && o.getSupervisaoRegional().getUuid().equals(u.getUuid())))
                .sorted(Comparator.comparing(UnidadeDeSaude::getNome, String.CASE_INSENSITIVE_ORDER))
                .map(o -> new FichaUnidadeDto.Vinculo(o.getUuid(), o.getNome(), o.getTipo().name(), o.isAtivo())).toList();
        List<FichaUnidadeDto.Vinculo> setores = setorRepository.findAll().stream()
                .filter(s -> s.getUnidade() != null && s.getUnidade().getUuid().equals(u.getUuid()))
                .sorted(Comparator.comparing(Setor::getNome, String.CASE_INSENSITIVE_ORDER))
                .map(s -> new FichaUnidadeDto.Vinculo(s.getUuid(), s.getNome(), s.getTipo().name(), s.isAtivo())).toList();
        List<FichaUnidadeDto.Profissional> profissionais = lotacaoRepository.findVigentesComUnidadeECargo().stream()
                .filter(l -> l.getUnidade() != null && l.getUnidade().getUuid().equals(u.getUuid()))
                .sorted(Comparator.comparing((Lotacao l) -> l.getProfissional().getNome(), String.CASE_INSENSITIVE_ORDER))
                .map(l -> new FichaUnidadeDto.Profissional(l.getProfissional().getMatricula(), l.getProfissional().getNome(),
                        l.getCargo() != null ? l.getCargo().getNome() : null,
                        l.getProfissional().getConselhoClasse() == null ? null
                                : l.getProfissional().getConselhoClasse() + " " + Objects.toString(l.getProfissional().getNumeroConselho(), ""),
                        l.getJornadaSemanalHoras()))
                .toList();
        List<EventoSituacaoUnidade> eventos = eventoRepository.findByUnidade_UuidOrderByOcorridoEmDesc(u.getUuid());
        Map<String, String> nomes = usuarioRepository.findByCpfIn(eventos.stream().map(EventoSituacaoUnidade::getRegistradoPorCpf)
                        .filter(Objects::nonNull).collect(Collectors.toSet())).stream()
                .collect(Collectors.toMap(Usuario::getCpf, Usuario::getNome, (a, b) -> a));
        List<FichaUnidadeDto.Evento> historico = eventos.stream().map(ev -> new FichaUnidadeDto.Evento(ev.getSituacaoAnterior(), ev.getSituacao(),
                ev.getMotivo(), ev.getPrevisaoRetorno(), ev.getOcorridoEm(), ev.getRegistradoPorCpf(), nomes.get(ev.getRegistradoPorCpf()))).toList();
        Map<DayOfWeek, String> texto = u.getHorarioFuncionamento() == null ? Map.of() : new HashMap<>(u.getHorarioFuncionamento());
        return new FichaUnidadeDto(resumo(u, contagens()), e != null ? e.getCep() : null, e != null ? e.getLogradouro() : null,
                e != null ? e.getNumeroLogradouro() : null, e != null ? e.getComplemento() : null, e != null ? e.getBairro() : null,
                e != null ? e.getCidade() : null, e != null ? e.getEstado() : null,
                u.getSaudeTelefones() == null ? Set.of() : new HashSet<>(u.getSaudeTelefones()), u.getResponsavelCpf(),
                u.getResponsavel() != null ? u.getResponsavel().getNome() : null, turnos, texto, subordinadas, setores, profissionais, historico);
    }

    private UnidadeDeSaude buscar(UUID uuid) {
        return unidadeDeSaudeRepository.findById(uuid).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar uma unidade de saúde com o id " + uuid + " em nossos registros."));
    }
}
