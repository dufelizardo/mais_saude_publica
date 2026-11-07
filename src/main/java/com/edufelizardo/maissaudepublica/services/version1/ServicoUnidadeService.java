package com.edufelizardo.maissaudepublica.services.version1;

import com.edufelizardo.maissaudepublica.config.ContextoAuditoria;
import com.edufelizardo.maissaudepublica.config.UsuarioAutenticado;
import com.edufelizardo.maissaudepublica.exceptions.ResourceBadRequestException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceConflictException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceNotFoundException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceUnprocessableEntityException;
import com.edufelizardo.maissaudepublica.models.CapacidadeAssistencial;
import com.edufelizardo.maissaudepublica.models.Leito;
import com.edufelizardo.maissaudepublica.models.Profissional;
import com.edufelizardo.maissaudepublica.models.ServicoUnidade;
import com.edufelizardo.maissaudepublica.models.Setor;
import com.edufelizardo.maissaudepublica.models.TipoServico;
import com.edufelizardo.maissaudepublica.models.UnidadeDeSaude;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.EncerramentoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.ServicoUnidadeRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.TipoServicoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.ServicosDaUnidadeDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.TipoServicoDto;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoLeito;
import com.edufelizardo.maissaudepublica.repositories.CapacidadeAssistencialRepository;
import com.edufelizardo.maissaudepublica.repositories.LeitoRepository;
import com.edufelizardo.maissaudepublica.repositories.ProfissionalRepository;
import com.edufelizardo.maissaudepublica.repositories.ServicoUnidadeRepository;
import com.edufelizardo.maissaudepublica.repositories.SetorRepository;
import com.edufelizardo.maissaudepublica.repositories.TipoServicoRepository;
import com.edufelizardo.maissaudepublica.repositories.UnidadeCapacidadeAssistencialRepository;
import com.edufelizardo.maissaudepublica.repositories.UnidadeDeSaudeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Serviços da unidade, fatia MO2 do Modelo Operacional (ADR-0165, desenho na ADR-0115): o catálogo de tipos e os serviços
 * de cada unidade, ligados aos setores. A capacidade diz o que a unidade pode fazer; o serviço diz como isso está
 * organizado ali. Os leitos vêm dos setores, sem cadastro próprio.
 */
@Service
public class ServicoUnidadeService {

    @Autowired private TipoServicoRepository tipoRepository;
    @Autowired private ServicoUnidadeRepository servicoRepository;
    @Autowired private CapacidadeAssistencialRepository capacidadeRepository;
    @Autowired private UnidadeCapacidadeAssistencialRepository unidadeCapacidadeRepository;
    @Autowired private UnidadeDeSaudeRepository unidadeRepository;
    @Autowired private SetorRepository setorRepository;
    @Autowired private LeitoRepository leitoRepository;
    @Autowired private ProfissionalRepository profissionalRepository;
    @Autowired private ControleDeAcesso controleDeAcesso;

    // ── Catálogo ───────────────────────────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<TipoServicoDto> tipos() {
        return tipoRepository.findTodos().stream().map(this::dto).toList();
    }

    /** Código repetido, 409; capacidade inexistente, 404; inativa, 422. */
    @Transactional
    public TipoServicoDto criarTipo(TipoServicoRequestDto dto) {
        String codigo = codigo(dto.getCodigo());
        if (tipoRepository.findByCodigo(codigo).isPresent()) {
            throw new ResourceConflictException("Já existe o tipo de serviço " + codigo + ".");
        }
        TipoServico t = new TipoServico(null, codigo, dto.getNome().trim(), texto(dto.getDescricao()), capacidade(dto.getCapacidadeExigidaId()),
                dto.getAtivo() == null || dto.getAtivo());
        tipoRepository.save(t);
        ContextoAuditoria.registro(t.getUuid());
        return dto(t);
    }

    /** Corrige ou desativa. A capacidade exigida não muda enquanto houver serviço vigente do tipo (422). */
    @Transactional
    public TipoServicoDto atualizarTipo(UUID uuid, TipoServicoRequestDto dto) {
        TipoServico t = tipoRepository.findById(uuid).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar o tipo de serviço " + uuid + " em nossos registros."));
        String codigo = codigo(dto.getCodigo());
        tipoRepository.findByCodigo(codigo).filter(o -> !o.getUuid().equals(uuid)).ifPresent(o -> {
            throw new ResourceConflictException("Já existe o tipo de serviço " + codigo + ".");
        });
        UUID atual = t.getCapacidadeExigida() != null ? t.getCapacidadeExigida().getUuid() : null;
        if (!Objects.equals(atual, dto.getCapacidadeExigidaId())) {
            if (servicoRepository.contarVigentesDoTipo(uuid) > 0) {
                throw new ResourceUnprocessableEntityException("A capacidade de " + t.getNome() + " não muda enquanto houver serviço vigente deste tipo.");
            }
            t.setCapacidadeExigida(capacidade(dto.getCapacidadeExigidaId()));
        }
        t.setCodigo(codigo);
        t.setNome(dto.getNome().trim());
        t.setDescricao(texto(dto.getDescricao()));
        t.setAtivo(dto.getAtivo() == null || dto.getAtivo());
        tipoRepository.save(t);
        ContextoAuditoria.registro(t.getUuid());
        return dto(t);
    }

    // ── Serviços da unidade ────────────────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public ServicosDaUnidadeDto daUnidade(UUID unidadeId) {
        UnidadeDeSaude u = unidade(unidadeId);
        controleDeAcesso.exigirVisivel(u, RedeUnidadesService.VER);
        return montar(u);
    }

    /**
     * Inclui o serviço: tipo ativo (422), com a capacidade exigida vigente na unidade (422), nome único entre os vigentes
     * (409), início não futuro (400). Os setores seguem as regras de {@link #setores}.
     */
    @Transactional
    public ServicosDaUnidadeDto incluir(UUID unidadeId, ServicoUnidadeRequestDto dto) {
        UnidadeDeSaude u = unidadeDeAtendimento(unidadeId);
        if (dto.getTipoId() == null) {
            throw new ResourceBadRequestException("Informe o tipo do serviço.");
        }
        TipoServico t = tipoRepository.findById(dto.getTipoId()).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar o tipo de serviço " + dto.getTipoId() + " em nossos registros."));
        if (!t.isAtivo()) {
            throw new ResourceUnprocessableEntityException("O tipo " + t.getNome() + " está inativo no catálogo.");
        }
        CapacidadeAssistencial exigida = t.getCapacidadeExigida();
        if (exigida != null && !unidadeCapacidadeRepository.existsByUnidade_UuidAndCapacidade_UuidAndFimIsNull(u.getUuid(), exigida.getUuid())) {
            throw new ResourceUnprocessableEntityException(t.getNome() + " exige a capacidade " + exigida.getNome() + ", que " + u.getNome()
                    + " não tem; inclua a capacidade antes.");
        }
        LocalDate inicio = dto.getInicio() != null ? dto.getInicio() : LocalDate.now();
        if (inicio.isAfter(LocalDate.now())) {
            throw new ResourceBadRequestException("O início não pode ser no futuro.");
        }
        ServicoUnidade s = new ServicoUnidade();
        s.setUnidade(u);
        s.setTipo(t);
        s.setInicio(inicio);
        s.setRegistradoPorCpf(UsuarioAutenticado.cpf());
        preencher(s, dto);
        servicoRepository.save(s);
        ContextoAuditoria.registro(s.getUuid());
        ContextoAuditoria.detalhe("Serviço " + s.getNome() + " incluído.");
        return montar(u);
    }

    /** Ajusta o nome, o responsável, o horário e os setores; encerrado não muda (422). */
    @Transactional
    public ServicosDaUnidadeDto atualizar(UUID unidadeId, UUID servicoId, ServicoUnidadeRequestDto dto) {
        UnidadeDeSaude u = unidadeDeAtendimento(unidadeId);
        ServicoUnidade s = servico(u, servicoId);
        if (!s.vigente()) {
            throw new ResourceUnprocessableEntityException("Este serviço já foi encerrado.");
        }
        preencher(s, dto);
        servicoRepository.save(s);
        ContextoAuditoria.registro(s.getUuid());
        return montar(u);
    }

    /** Encerra com o motivo; o fim fica entre o início e hoje (400). Os setores ficam livres para outro serviço. */
    @Transactional
    public ServicosDaUnidadeDto encerrar(UUID unidadeId, UUID servicoId, EncerramentoRequestDto dto) {
        UnidadeDeSaude u = unidade(unidadeId);
        controleDeAcesso.exigir(RedeUnidadesService.GERENCIAR, u);
        ServicoUnidade s = servico(u, servicoId);
        if (!s.vigente()) {
            throw new ResourceUnprocessableEntityException("Este serviço já foi encerrado.");
        }
        LocalDate fim = dto.getFim() != null ? dto.getFim() : LocalDate.now();
        if (fim.isBefore(s.getInicio()) || fim.isAfter(LocalDate.now())) {
            throw new ResourceBadRequestException("O fim fica entre o início e hoje.");
        }
        s.setFim(fim);
        s.setMotivoFim(dto.getMotivo().trim());
        servicoRepository.save(s);
        ContextoAuditoria.unidade(u);
        ContextoAuditoria.registro(s.getUuid());
        ContextoAuditoria.detalhe("Serviço " + s.getNome() + " encerrado.");
        return montar(u);
    }

    // ── Apoio ──────────────────────────────────────────────────────────────────────────────────────

    private void preencher(ServicoUnidade s, ServicoUnidadeRequestDto dto) {
        String nome = dto.getNome().trim();
        boolean repetido = servicoRepository.findDaUnidade(s.getUnidade().getUuid()).stream()
                .anyMatch(o -> o.vigente() && !o.getUuid().equals(s.getUuid()) && o.getNome().equalsIgnoreCase(nome));
        if (repetido) {
            throw new ResourceConflictException("Já existe o serviço " + nome + " nesta unidade.");
        }
        s.setNome(nome);
        s.setResponsavel(responsavel(dto.getResponsavelMatricula()));
        s.setVinteQuatroHoras(Boolean.TRUE.equals(dto.getVinteQuatroHoras()));
        s.setHorario(s.isVinteQuatroHoras() ? null : texto(dto.getHorario()));
        s.getSetores().clear();
        s.getSetores().addAll(setores(s, dto.getSetorIds()));
    }

    /** Os setores existem (404), são desta unidade (422), estão ativos (422) e não estão em outro serviço vigente (409). */
    private Set<Setor> setores(ServicoUnidade s, List<UUID> ids) {
        Set<Setor> lista = new HashSet<>();
        if (ids == null) {
            return lista;
        }
        Set<UUID> ocupados = servicoRepository.findDaUnidade(s.getUnidade().getUuid()).stream()
                .filter(o -> o.vigente() && !o.getUuid().equals(s.getUuid()))
                .flatMap(o -> o.getSetores().stream()).map(Setor::getUuid).collect(Collectors.toSet());
        for (UUID id : new HashSet<>(ids)) {
            Setor x = setorRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(
                    "Não foi possível encontrar o setor " + id + " em nossos registros."));
            if (!x.getUnidade().getUuid().equals(s.getUnidade().getUuid())) {
                throw new ResourceUnprocessableEntityException("O setor " + x.getNome() + " é de outra unidade.");
            }
            if (!x.isAtivo()) {
                throw new ResourceUnprocessableEntityException("O setor " + x.getNome() + " está inativo.");
            }
            if (ocupados.contains(id)) {
                throw new ResourceConflictException("O setor " + x.getNome() + " já está em outro serviço vigente.");
            }
            lista.add(x);
        }
        return lista;
    }

    private ServicosDaUnidadeDto montar(UnidadeDeSaude u) {
        List<ServicoUnidade> todos = servicoRepository.findDaUnidade(u.getUuid());
        List<Leito> leitos = leitoRepository.findByUnidade_UuidOrderByIdentificacaoAsc(u.getUuid()).stream().filter(Leito::isAtivo).toList();
        Set<UUID> ocupados = todos.stream().filter(ServicoUnidade::vigente).flatMap(s -> s.getSetores().stream()).map(Setor::getUuid).collect(Collectors.toSet());
        List<ServicosDaUnidadeDto.SetorRef> livres = setorRepository.findAll().stream()
                .filter(x -> x.getUnidade().getUuid().equals(u.getUuid()) && x.isAtivo() && !ocupados.contains(x.getUuid()))
                .sorted(Comparator.comparing(Setor::getNome, String.CASE_INSENSITIVE_ORDER))
                .map(x -> new ServicosDaUnidadeDto.SetorRef(x.getUuid(), x.getNome())).toList();
        return new ServicosDaUnidadeDto(u.getUuid(), todos.stream().filter(ServicoUnidade::vigente).map(s -> dto(s, leitos)).toList(),
                todos.stream().filter(s -> !s.vigente()).sorted(Comparator.comparing(ServicoUnidade::getFim).reversed()).map(s -> dto(s, leitos)).toList(), livres);
    }

    private static ServicosDaUnidadeDto.Servico dto(ServicoUnidade s, List<Leito> leitos) {
        Set<UUID> setores = s.getSetores().stream().map(Setor::getUuid).collect(Collectors.toSet());
        List<Leito> doServico = leitos.stream().filter(l -> l.getSetor() != null && setores.contains(l.getSetor().getUuid())).toList();
        Profissional r = s.getResponsavel();
        return new ServicosDaUnidadeDto.Servico(s.getUuid(), s.getTipo().getUuid(), s.getTipo().getCodigo(), s.getTipo().getNome(), s.getNome(),
                r != null ? r.getMatricula() : null, r != null ? r.getNome() : null, s.isVinteQuatroHoras(), s.getHorario(),
                s.getSetores().stream().sorted(Comparator.comparing(Setor::getNome, String.CASE_INSENSITIVE_ORDER))
                        .map(x -> new ServicosDaUnidadeDto.SetorRef(x.getUuid(), x.getNome())).toList(),
                doServico.size(), (int) doServico.stream().filter(l -> l.getSituacao() == SituacaoLeito.LIVRE).count(),
                (int) doServico.stream().filter(l -> l.getSituacao() == SituacaoLeito.OCUPADO).count(), s.getInicio(), s.getFim(), s.getMotivoFim());
    }

    private TipoServicoDto dto(TipoServico t) {
        CapacidadeAssistencial c = t.getCapacidadeExigida();
        return new TipoServicoDto(t.getUuid(), t.getCodigo(), t.getNome(), t.getDescricao(), c != null ? c.getUuid() : null, c != null ? c.getNome() : null,
                t.isAtivo(), servicoRepository.contarVigentesDoTipo(t.getUuid()));
    }

    private CapacidadeAssistencial capacidade(UUID id) {
        if (id == null) {
            return null;
        }
        CapacidadeAssistencial c = capacidadeRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar a capacidade " + id + " em nossos registros."));
        if (!c.isAtivo()) {
            throw new ResourceUnprocessableEntityException("A capacidade " + c.getNome() + " está inativa no catálogo.");
        }
        return c;
    }

    private Profissional responsavel(String matricula) {
        String m = texto(matricula);
        if (m == null) {
            return null;
        }
        return profissionalRepository.findByMatricula(m).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar um profissional com a matrícula " + m + " em nossos registros."));
    }

    private ServicoUnidade servico(UnidadeDeSaude u, UUID id) {
        return servicoRepository.findById(id).filter(s -> s.getUnidade().getUuid().equals(u.getUuid()))
                .orElseThrow(() -> new ResourceNotFoundException("Não foi possível encontrar o serviço " + id + " nesta unidade."));
    }

    private UnidadeDeSaude unidade(UUID id) {
        return unidadeRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar uma unidade de saúde com o id " + id + " em nossos registros."));
    }

    private UnidadeDeSaude unidadeDeAtendimento(UUID id) {
        UnidadeDeSaude u = unidade(id);
        controleDeAcesso.exigir(RedeUnidadesService.GERENCIAR, u);
        if (RedeUnidadesService.NIVEIS_DE_GESTAO.contains(u.getTipo())) {
            throw new ResourceUnprocessableEntityException(u.getNome() + " é um nível de gestão (" + u.getTipo() + "); serviços são das unidades de atendimento.");
        }
        ContextoAuditoria.unidade(u);
        return u;
    }

    private static String codigo(String s) {
        return s.trim().toUpperCase();
    }

    private static String texto(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }
}
