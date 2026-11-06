package com.edufelizardo.maissaudepublica.services.version1;

import com.edufelizardo.maissaudepublica.config.ContextoAuditoria;
import com.edufelizardo.maissaudepublica.config.UsuarioAutenticado;
import com.edufelizardo.maissaudepublica.exceptions.ResourceBadRequestException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceConflictException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceForbiddenException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceNotFoundException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceUnprocessableEntityException;
import com.edufelizardo.maissaudepublica.models.AcompanhamentoProgramatico;
import com.edufelizardo.maissaudepublica.models.Atendimento;
import com.edufelizardo.maissaudepublica.models.Equipe;
import com.edufelizardo.maissaudepublica.models.EtapaLinhaCuidado;
import com.edufelizardo.maissaudepublica.models.Internacao;
import com.edufelizardo.maissaudepublica.models.ItemPedidoExame;
import com.edufelizardo.maissaudepublica.models.LinhaDeCuidado;
import com.edufelizardo.maissaudepublica.models.Paciente;
import com.edufelizardo.maissaudepublica.models.PercursoLinhaCuidado;
import com.edufelizardo.maissaudepublica.models.RegistroEtapaPercurso;
import com.edufelizardo.maissaudepublica.models.RegistroVacinacao;
import com.edufelizardo.maissaudepublica.models.SolicitacaoRegulacao;
import com.edufelizardo.maissaudepublica.models.UnidadeDeSaude;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.EncerramentoPercursoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.LinhaCuidadoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.PercursoLinhaRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.RegistroEtapaPercursoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.LinhaCuidadoDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.PainelLinhasCuidadoDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.PercursoLinhaDto;
import com.edufelizardo.maissaudepublica.models.enuns.ReferenciaPrazoEtapa;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoEtapaPercurso;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoPercursoLinha;
import com.edufelizardo.maissaudepublica.models.enuns.StatusAtendimento;
import com.edufelizardo.maissaudepublica.models.enuns.StatusSolicitacaoRegulacao;
import com.edufelizardo.maissaudepublica.models.enuns.TipoEtapaLinhaCuidado;
import com.edufelizardo.maissaudepublica.models.enuns.TipoRegistroEtapa;
import com.edufelizardo.maissaudepublica.repositories.AcompanhamentoProgramaticoRepository;
import com.edufelizardo.maissaudepublica.repositories.AtendimentoRepository;
import com.edufelizardo.maissaudepublica.repositories.EquipeRepository;
import com.edufelizardo.maissaudepublica.repositories.EspecialidadeRepository;
import com.edufelizardo.maissaudepublica.repositories.EtapaLinhaCuidadoRepository;
import com.edufelizardo.maissaudepublica.repositories.ExameLaboratorialRepository;
import com.edufelizardo.maissaudepublica.repositories.ImunobiologicoRepository;
import com.edufelizardo.maissaudepublica.repositories.InternacaoRepository;
import com.edufelizardo.maissaudepublica.repositories.ItemPedidoExameRepository;
import com.edufelizardo.maissaudepublica.repositories.LinhaDeCuidadoRepository;
import com.edufelizardo.maissaudepublica.repositories.PacienteRepository;
import com.edufelizardo.maissaudepublica.repositories.PercursoLinhaCuidadoRepository;
import com.edufelizardo.maissaudepublica.repositories.ProcedimentoReguladoRepository;
import com.edufelizardo.maissaudepublica.repositories.ProfissionalRepository;
import com.edufelizardo.maissaudepublica.repositories.ProgramaRepository;
import com.edufelizardo.maissaudepublica.repositories.RegistroEtapaPercursoRepository;
import com.edufelizardo.maissaudepublica.repositories.RegistroVacinacaoRepository;
import com.edufelizardo.maissaudepublica.repositories.SolicitacaoRegulacaoRepository;
import com.edufelizardo.maissaudepublica.repositories.UnidadeDeSaudeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Linhas de cuidado, fatia P5 dos Programas (ADR-0152): o catálogo de linhas com as etapas como dado, o percurso do
 * paciente e o painel com a busca ativa. A situação das etapas sai dos registros dos domínios (atendimento concluído,
 * exame liberado, regulação realizada, dose, alta da internação), calculada na hora; só os registros manuais e as
 * dispensas são guardados. O percurso é dado de saúde: segue as regras do prontuário, e a linha de programa sensível só
 * aparece para quem acompanha na unidade (ADR-0119).
 */
@Service
public class LinhaCuidadoService {

    private static final String GERENCIAR = ProgramaService.GERENCIAR;
    private static final String ACOMPANHAR = AcompanhamentoService.ACOMPANHAR;
    private static final DateTimeFormatter DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @Autowired private LinhaDeCuidadoRepository linhaRepository;
    @Autowired private EtapaLinhaCuidadoRepository etapaRepository;
    @Autowired private PercursoLinhaCuidadoRepository percursoRepository;
    @Autowired private RegistroEtapaPercursoRepository registroRepository;
    @Autowired private ProgramaRepository programaRepository;
    @Autowired private EspecialidadeRepository especialidadeRepository;
    @Autowired private ExameLaboratorialRepository exameRepository;
    @Autowired private ProcedimentoReguladoRepository procedimentoRepository;
    @Autowired private ImunobiologicoRepository imunobiologicoRepository;
    @Autowired private PacienteRepository pacienteRepository;
    @Autowired private UnidadeDeSaudeRepository unidadeRepository;
    @Autowired private EquipeRepository equipeRepository;
    @Autowired private AcompanhamentoProgramaticoRepository acompanhamentoRepository;
    @Autowired private ProfissionalRepository profissionalRepository;
    @Autowired private AtendimentoRepository atendimentoRepository;
    @Autowired private ItemPedidoExameRepository itemExameRepository;
    @Autowired private SolicitacaoRegulacaoRepository regulacaoRepository;
    @Autowired private RegistroVacinacaoRepository vacinacaoRepository;
    @Autowired private InternacaoRepository internacaoRepository;
    @Autowired private ControleDeAcesso controleDeAcesso;
    @Autowired private VinculoAssistencialService vinculoService;
    @Autowired private AcompanhamentoService acompanhamentoService;

    // ── Catálogo ───────────────────────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<LinhaCuidadoDto> linhas() {
        return linhaRepository.findTodas().stream().map(this::dto).toList();
    }

    @Transactional(readOnly = true)
    public LinhaCuidadoDto linha(UUID uuid) {
        return dto(linhaDe(uuid));
    }

    @Transactional
    public LinhaCuidadoDto criar(LinhaCuidadoRequestDto dto) {
        controleDeAcesso.exigirAlguma(GERENCIAR);
        if (linhaRepository.existsByCodigoIgnoreCase(codigo(dto.getCodigo()))) {
            throw new ResourceConflictException("Já existe uma linha de cuidado com o código " + codigo(dto.getCodigo()) + ".");
        }
        LinhaDeCuidado l = new LinhaDeCuidado();
        l.setCriadaEm(Instant.now());
        l.setRegistradoPorCpf(UsuarioAutenticado.cpf());
        aplicar(l, dto);
        ContextoAuditoria.registro(l.getUuid());
        return dto(l);
    }

    @Transactional
    public LinhaCuidadoDto atualizar(UUID uuid, LinhaCuidadoRequestDto dto) {
        controleDeAcesso.exigirAlguma(GERENCIAR);
        LinhaDeCuidado l = linhaDe(uuid);
        String novo = codigo(dto.getCodigo());
        if (!novo.equalsIgnoreCase(l.getCodigo()) && linhaRepository.existsByCodigoIgnoreCase(novo)) {
            throw new ResourceConflictException("Já existe uma linha de cuidado com o código " + novo + ".");
        }
        aplicar(l, dto);
        ContextoAuditoria.registro(l.getUuid());
        return dto(l);
    }

    /**
     * Grava a linha e as etapas: a etapa com uuid é ajustada; a sem uuid é criada; a que não veio é desativada (os
     * percursos guardam registros dela). Cada tipo pede a referência que o cumpre (400); a primeira etapa conta do início.
     */
    private void aplicar(LinhaDeCuidado l, LinhaCuidadoRequestDto dto) {
        l.setCodigo(codigo(dto.getCodigo()));
        l.setNome(dto.getNome().trim());
        l.setDescricao(texto(dto.getDescricao()));
        l.setPrograma(dto.getProgramaId() == null ? null : programaRepository.findById(dto.getProgramaId()).orElseThrow(() ->
                new ResourceNotFoundException("Não foi possível encontrar o programa " + dto.getProgramaId() + " em nossos registros.")));
        l.setAtiva(dto.getAtiva() == null || dto.getAtiva());
        l = linhaRepository.save(l);
        Map<UUID, EtapaLinhaCuidado> atuais = l.getUuid() == null ? Map.of() : etapaRepository.findDaLinha(l.getUuid()).stream()
                .collect(Collectors.toMap(EtapaLinhaCuidado::getUuid, e -> e));
        Set<UUID> mantidas = new HashSet<>();
        int ordem = 1;
        for (LinhaCuidadoRequestDto.Etapa x : dto.getEtapas()) {
            EtapaLinhaCuidado e;
            if (x.getUuid() != null) {
                e = atuais.get(x.getUuid());
                if (e == null) {
                    throw new ResourceUnprocessableEntityException("A etapa " + x.getUuid() + " não é desta linha.");
                }
                mantidas.add(e.getUuid());
            } else {
                e = new EtapaLinhaCuidado();
                e.setLinha(l);
            }
            ReferenciaPrazoEtapa desde = x.getPrazoDesde() == null ? ReferenciaPrazoEtapa.INICIO : x.getPrazoDesde();
            if (ordem == 1 && desde == ReferenciaPrazoEtapa.ETAPA_ANTERIOR) {
                throw new ResourceBadRequestException("A primeira etapa conta o prazo do início do percurso.");
            }
            e.setOrdem(ordem++);
            e.setNome(x.getNome().trim());
            e.setTipo(x.getTipo());
            e.setEspecialidade(null);
            e.setExame(null);
            e.setProcedimento(null);
            e.setImunobiologico(null);
            e.setCids(null);
            switch (x.getTipo()) {
                case ATENDIMENTO -> e.setEspecialidade(x.getEspecialidadeId() == null ? null : especialidadeRepository.findById(x.getEspecialidadeId())
                        .orElseThrow(() -> new ResourceNotFoundException("Não foi possível encontrar a especialidade " + x.getEspecialidadeId() + ".")));
                case EXAME -> e.setExame(exameRepository.findById(exigir(x.getExameId(), "o exame")).orElseThrow(() ->
                        new ResourceNotFoundException("Não foi possível encontrar o exame " + x.getExameId() + ".")));
                case REGULACAO -> e.setProcedimento(procedimentoRepository.findById(exigir(x.getProcedimentoId(), "o procedimento regulado"))
                        .orElseThrow(() -> new ResourceNotFoundException("Não foi possível encontrar o procedimento " + x.getProcedimentoId() + ".")));
                case VACINA -> e.setImunobiologico(imunobiologicoRepository.findById(exigir(x.getImunobiologicoId(), "a vacina")).orElseThrow(() ->
                        new ResourceNotFoundException("Não foi possível encontrar o imunobiológico " + x.getImunobiologicoId() + ".")));
                case INTERNACAO -> e.setCids(cids(x.getCids()));
                case MANUAL -> { }
            }
            e.setQuantidade(x.getQuantidade() == null ? 1 : x.getQuantidade());
            e.setPrazoDias(x.getPrazoDias());
            e.setPrazoDesde(desde);
            e.setObrigatoria(x.getObrigatoria() == null || x.getObrigatoria());
            e.setAtiva(true);
            etapaRepository.save(e);
        }
        for (EtapaLinhaCuidado e : atuais.values()) {
            if (!mantidas.contains(e.getUuid()) && e.isAtiva()) {
                e.setAtiva(false);
                e.setOrdem(1000 + e.getOrdem());
                etapaRepository.save(e);
            }
        }
    }

    // ── Percurso ───────────────────────────────────────────────────────────────────────────────

    /**
     * Abre o percurso: a linha ativa (422), o início não no futuro (400), a equipe da unidade (400), o acompanhamento do
     * paciente no programa da linha (422) e um só percurso em curso por paciente e linha (409).
     */
    @Transactional
    public PercursoLinhaDto abrir(PercursoLinhaRequestDto dto) {
        UnidadeDeSaude unidade = unidadeRepository.findById(dto.getUnidadeId()).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar a unidade " + dto.getUnidadeId() + " em nossos registros."));
        controleDeAcesso.exigir(ACOMPANHAR, unidade);
        Paciente paciente = pacienteRepository.findById(dto.getPacienteId()).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar um paciente com o id " + dto.getPacienteId() + " em nossos registros."));
        ContextoAuditoria.paciente(paciente.getUuid());
        LinhaDeCuidado linha = linhaDe(dto.getLinhaId());
        if (!linha.isAtiva()) {
            throw new ResourceUnprocessableEntityException("A linha de cuidado " + linha.getNome() + " está inativa.");
        }
        if (dto.getInicio().isAfter(LocalDate.now())) {
            throw new ResourceBadRequestException("O início do percurso não pode ser no futuro.");
        }
        if (percursoRepository.existsByPaciente_UuidAndLinha_UuidAndSituacao(paciente.getUuid(), linha.getUuid(), SituacaoPercursoLinha.EM_CURSO)) {
            throw new ResourceConflictException(paciente.getNome() + " já tem um percurso em curso na linha " + linha.getNome() + ".");
        }
        Equipe equipe = null;
        if (dto.getEquipeId() != null) {
            equipe = equipeRepository.findById(dto.getEquipeId()).orElseThrow(() -> new ResourceNotFoundException(
                    "Não foi possível encontrar a equipe " + dto.getEquipeId() + " em nossos registros."));
            if (equipe.getUnidade() == null || !equipe.getUnidade().getUuid().equals(unidade.getUuid())) {
                throw new ResourceBadRequestException("A equipe " + equipe.getNome() + " não é da unidade " + unidade.getNome() + ".");
            }
        }
        AcompanhamentoProgramatico acompanhamento = null;
        if (dto.getAcompanhamentoId() != null) {
            acompanhamento = acompanhamentoRepository.findById(dto.getAcompanhamentoId()).orElseThrow(() -> new ResourceNotFoundException(
                    "Não foi possível encontrar o acompanhamento " + dto.getAcompanhamentoId() + " em nossos registros."));
            if (!acompanhamento.getPaciente().getUuid().equals(paciente.getUuid())
                    || linha.getPrograma() == null || !acompanhamento.getPrograma().getUuid().equals(linha.getPrograma().getUuid())) {
                throw new ResourceUnprocessableEntityException("O acompanhamento não é do paciente no programa da linha.");
            }
        }
        PercursoLinhaCuidado p = new PercursoLinhaCuidado();
        p.setPaciente(paciente);
        p.setLinha(linha);
        p.setInicio(dto.getInicio());
        p.setUnidade(unidade);
        p.setEquipe(equipe);
        p.setAcompanhamento(acompanhamento);
        p.setSituacao(SituacaoPercursoLinha.EM_CURSO);
        p.setRegistradoEm(Instant.now());
        p.setRegistradoPorCpf(UsuarioAutenticado.cpf());
        p = percursoRepository.save(p);
        ContextoAuditoria.registro(p.getUuid());
        ContextoAuditoria.detalhe("Percurso na linha " + linha.getCodigo() + ".");
        return calcular(List.of(p)).get(0);
    }

    /** O percurso, com as regras do prontuário (vínculo ou acesso justificado) e a linha sensível só para quem acompanha. */
    @Transactional(readOnly = true)
    public PercursoLinhaDto percurso(UUID uuid) {
        PercursoLinhaCuidado p = percursoDe(uuid);
        controleDeAcesso.exigirAlguma(AcompanhamentoService.PRONTUARIO, ACOMPANHAR);
        ContextoAuditoria.paciente(p.getPaciente().getUuid());
        vinculoService.exigirVinculo(p.getPaciente().getUuid());
        if (!podeVer(p)) {
            throw new ResourceForbiddenException("A linha " + p.getLinha().getNome() + " é de programa sensível: só quem acompanha na unidade vê.");
        }
        return calcular(List.of(p)).get(0);
    }

    /** Os percursos do paciente, em curso primeiro; os de linha sensível só para quem acompanha na unidade. */
    @Transactional(readOnly = true)
    public List<PercursoLinhaDto> doPaciente(UUID pacienteId) {
        if (!pacienteRepository.existsById(pacienteId)) {
            throw new ResourceNotFoundException("Não foi possível encontrar um paciente com o id " + pacienteId + " em nossos registros.");
        }
        controleDeAcesso.exigirAlguma(AcompanhamentoService.PRONTUARIO, ACOMPANHAR);
        ContextoAuditoria.paciente(pacienteId);
        vinculoService.exigirVinculo(pacienteId);
        List<PercursoLinhaCuidado> visiveis = percursoRepository.findDoPaciente(pacienteId).stream().filter(this::podeVer).toList();
        return calcular(visiveis).stream()
                .sorted(Comparator.comparing((PercursoLinhaDto d) -> d.getSituacao() != SituacaoPercursoLinha.EM_CURSO)
                        .thenComparing(PercursoLinhaDto::getInicio, Comparator.reverseOrder()))
                .toList();
    }

    /**
     * Registra a etapa manual cumprida, ou dispensa qualquer etapa com o motivo (400). O percurso precisa estar em curso e
     * a etapa ser ativa da linha (422); só a manual se cumpre à mão (422); a data não vem antes do início nem no futuro.
     */
    @Transactional
    public PercursoLinhaDto registrar(UUID percursoId, RegistroEtapaPercursoRequestDto dto) {
        PercursoLinhaCuidado p = emCurso(percursoId);
        EtapaLinhaCuidado etapa = etapaRepository.findById(dto.getEtapaId()).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar a etapa " + dto.getEtapaId() + " em nossos registros."));
        if (!etapa.getLinha().getUuid().equals(p.getLinha().getUuid()) || !etapa.isAtiva()) {
            throw new ResourceUnprocessableEntityException("A etapa não é ativa da linha " + p.getLinha().getNome() + ".");
        }
        String observacao = texto(dto.getObservacao());
        if (dto.getTipo() == TipoRegistroEtapa.CUMPRIDA && etapa.getTipo() != TipoEtapaLinhaCuidado.MANUAL) {
            throw new ResourceUnprocessableEntityException("A etapa " + etapa.getNome() + " se cumpre pelo registro do domínio; só a manual se registra à mão.");
        }
        List<RegistroEtapaPercurso> registros = registroRepository.findDosPercursos(List.of(p.getUuid()));
        if (dto.getTipo() == TipoRegistroEtapa.DISPENSADA) {
            if (observacao == null) {
                throw new ResourceBadRequestException("Informe o motivo da dispensa.");
            }
            if (registros.stream().anyMatch(r -> r.getTipo() == TipoRegistroEtapa.DISPENSADA && r.getEtapa().getUuid().equals(etapa.getUuid()))) {
                throw new ResourceConflictException("A etapa " + etapa.getNome() + " já foi dispensada.");
            }
            PercursoLinhaDto.Etapa situacao = calcular(List.of(p)).get(0).getEtapas().stream()
                    .filter(x -> x.getEtapaId().equals(etapa.getUuid())).findFirst().orElseThrow();
            if (situacao.getSituacao() == SituacaoEtapaPercurso.CUMPRIDA) {
                throw new ResourceUnprocessableEntityException("A etapa " + etapa.getNome() + " já foi cumprida.");
            }
        }
        LocalDate data = dto.getData() != null ? dto.getData() : LocalDate.now();
        if (data.isAfter(LocalDate.now())) {
            throw new ResourceBadRequestException("A data não pode ser no futuro.");
        }
        if (data.isBefore(p.getInicio())) {
            throw new ResourceBadRequestException("A data não pode ser antes do início do percurso (" + p.getInicio().format(DATA) + ").");
        }
        RegistroEtapaPercurso r = new RegistroEtapaPercurso();
        r.setPercurso(p);
        r.setEtapa(etapa);
        r.setTipo(dto.getTipo());
        r.setData(data);
        r.setObservacao(observacao);
        r.setProfissional(dto.getProfissionalMatricula() == null || dto.getProfissionalMatricula().isBlank() ? null
                : profissionalRepository.findByMatricula(dto.getProfissionalMatricula().trim()).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar um profissional com a matrícula " + dto.getProfissionalMatricula() + " em nossos registros.")));
        r.setRegistradoEm(Instant.now());
        r.setRegistradoPorCpf(UsuarioAutenticado.cpf());
        registroRepository.save(r);
        ContextoAuditoria.detalhe("Etapa " + etapa.getNome() + ": " + dto.getTipo() + ".");
        return calcular(List.of(p)).get(0);
    }

    /** Encerra: concluído com as obrigatórias cumpridas ou dispensadas (422); interrompido com o motivo (400). */
    @Transactional
    public PercursoLinhaDto encerrar(UUID percursoId, EncerramentoPercursoRequestDto dto) {
        PercursoLinhaCuidado p = emCurso(percursoId);
        if (dto.getSituacao() == SituacaoPercursoLinha.EM_CURSO) {
            throw new ResourceBadRequestException("Encerre como concluído ou interrompido.");
        }
        String motivo = texto(dto.getMotivo());
        if (dto.getSituacao() == SituacaoPercursoLinha.INTERROMPIDO && motivo == null) {
            throw new ResourceBadRequestException("Informe o motivo da interrupção.");
        }
        if (dto.getSituacao() == SituacaoPercursoLinha.CONCLUIDO) {
            PercursoLinhaDto atual = calcular(List.of(p)).get(0);
            if (atual.getObrigatoriasFeitas() < atual.getObrigatorias()) {
                throw new ResourceUnprocessableEntityException("Para concluir, as etapas obrigatórias precisam estar cumpridas ou dispensadas (falta: "
                        + atual.getEtapaAtual() + ").");
            }
        }
        p.setSituacao(dto.getSituacao());
        p.setEncerradoEm(LocalDate.now());
        p.setMotivoEncerramento(motivo);
        percursoRepository.save(p);
        ContextoAuditoria.detalhe("Percurso " + dto.getSituacao() + ".");
        return calcular(List.of(p)).get(0);
    }

    // ── Painel ─────────────────────────────────────────────────────────────────────────────────

    /**
     * Os percursos em curso nas unidades do escopo de quem acompanha, atrasados primeiro, e as sugestões: quem está no
     * programa de uma linha ativa sem percurso em curso nela.
     */
    @Transactional(readOnly = true)
    public PainelLinhasCuidadoDto painel(UUID linhaId, UUID unidadeId, UUID equipeId, boolean atrasados) {
        controleDeAcesso.exigirAlguma(ACOMPANHAR);
        Predicate<UUID> naUnidade = u -> unidadeId == null || unidadeId.equals(u);
        List<PercursoLinhaCuidado> emCurso = controleDeAcesso.filtrar(percursoRepository.findEmCurso().stream(),
                        p -> java.util.stream.Stream.of(p.getUnidade()), ACOMPANHAR)
                .filter(p -> (linhaId == null || p.getLinha().getUuid().equals(linhaId)) && naUnidade.test(p.getUnidade().getUuid())
                        && (equipeId == null || (p.getEquipe() != null && p.getEquipe().getUuid().equals(equipeId))) && podeVer(p))
                .toList();
        List<PercursoLinhaDto> todos = calcular(emCurso).stream().map(d -> {
            d.setEtapas(List.of());
            return d;
        }).toList();
        List<PercursoLinhaDto> lista = todos.stream().filter(d -> !atrasados || d.isAtrasado())
                .sorted(Comparator.comparing((PercursoLinhaDto d) -> !d.isAtrasado()).thenComparing(PercursoLinhaDto::getPacienteNome,
                        String.CASE_INSENSITIVE_ORDER))
                .toList();
        Set<String> abertos = emCurso.stream().map(p -> p.getPaciente().getUuid() + "|" + p.getLinha().getUuid()).collect(Collectors.toSet());
        // Os percursos fora do escopo também contam como abertos: a sugestão não repete quem já está na linha.
        percursoRepository.findEmCurso().forEach(p -> abertos.add(p.getPaciente().getUuid() + "|" + p.getLinha().getUuid()));
        List<PainelLinhasCuidadoDto.Sugestao> sugestoes = new ArrayList<>();
        for (LinhaDeCuidado l : linhaRepository.findTodas()) {
            if (!l.isAtiva() || l.getPrograma() == null || (linhaId != null && !l.getUuid().equals(linhaId))) {
                continue;
            }
            controleDeAcesso.filtrar(acompanhamentoRepository.findVigentesDoPrograma(l.getPrograma().getUuid()).stream(),
                            a -> java.util.stream.Stream.of(a.getUnidade()), ACOMPANHAR)
                    .filter(a -> naUnidade.test(a.getUnidade().getUuid()) && (equipeId == null || (a.getEquipe() != null && a.getEquipe().getUuid().equals(equipeId)))
                            && acompanhamentoService.podeVer(a.getPrograma(), a.getUnidade())
                            && !abertos.contains(a.getPaciente().getUuid() + "|" + l.getUuid()))
                    .forEach(a -> sugestoes.add(new PainelLinhasCuidadoDto.Sugestao(a.getPaciente().getUuid(), a.getPaciente().getNome(), l.getUuid(),
                            l.getNome(), a.getUuid(), a.getUnidade().getUuid(), a.getUnidade().getNome(), a.getEquipe() != null ? a.getEquipe().getUuid() : null,
                            a.getInicio())));
        }
        sugestoes.sort(Comparator.comparing(PainelLinhasCuidadoDto.Sugestao::getPacienteNome, String.CASE_INSENSITIVE_ORDER));
        return new PainelLinhasCuidadoDto(todos.size(), todos.stream().filter(PercursoLinhaDto::isAtrasado).count(), lista, sugestoes);
    }

    // ── Cálculo ────────────────────────────────────────────────────────────────────────────────

    /** O DTO de cada percurso, com as etapas calculadas dos registros do paciente (uma leitura por paciente e domínio). */
    private List<PercursoLinhaDto> calcular(List<PercursoLinhaCuidado> percursos) {
        if (percursos.isEmpty()) {
            return List.of();
        }
        LocalDate hoje = LocalDate.now();
        Map<UUID, List<EtapaLinhaCuidado>> etapasPorLinha = new HashMap<>();
        Map<UUID, Registros> porPaciente = new HashMap<>();
        Map<UUID, List<RegistroEtapaPercurso>> registros = registroRepository.findDosPercursos(percursos.stream().map(PercursoLinhaCuidado::getUuid).toList())
                .stream().collect(Collectors.groupingBy(r -> r.getPercurso().getUuid()));
        List<PercursoLinhaDto> lista = new ArrayList<>();
        for (PercursoLinhaCuidado p : percursos) {
            List<EtapaLinhaCuidado> etapas = etapasPorLinha.computeIfAbsent(p.getLinha().getUuid(),
                    id -> etapaRepository.findDaLinha(id).stream().filter(EtapaLinhaCuidado::isAtiva).toList());
            Registros r = porPaciente.computeIfAbsent(p.getPaciente().getUuid(), this::registros);
            List<RegistroEtapaPercurso> doPercurso = registros.getOrDefault(p.getUuid(), List.of());
            Map<UUID, List<PercursoLinhaCalculo.Evidencia>> evidencias = new HashMap<>();
            Map<UUID, String> andamentos = new HashMap<>();
            for (EtapaLinhaCuidado e : etapas) {
                evidencias.put(e.getUuid(), evidencias(e, r, doPercurso, p.getInicio(), andamentos));
            }
            Map<UUID, RegistroEtapaPercurso> dispensas = doPercurso.stream().filter(x -> x.getTipo() == TipoRegistroEtapa.DISPENSADA)
                    .collect(Collectors.toMap(x -> x.getEtapa().getUuid(), x -> x, (a, b) -> a));
            List<PercursoLinhaDto.Etapa> calculadas = PercursoLinhaCalculo.calcular(etapas, p.getInicio(), evidencias, andamentos, dispensas, hoje);
            List<PercursoLinhaDto.Etapa> obrigatorias = calculadas.stream().filter(PercursoLinhaDto.Etapa::isObrigatoria).toList();
            Predicate<PercursoLinhaDto.Etapa> feita = x -> x.getSituacao() == SituacaoEtapaPercurso.CUMPRIDA || x.getSituacao() == SituacaoEtapaPercurso.DISPENSADA;
            String atual = obrigatorias.stream().filter(feita.negate()).map(PercursoLinhaDto.Etapa::getNome).findFirst().orElse(null);
            boolean emCurso = p.getSituacao() == SituacaoPercursoLinha.EM_CURSO;
            lista.add(new PercursoLinhaDto(p.getUuid(), p.getPaciente().getUuid(), p.getPaciente().getNome(), p.getLinha().getUuid(), p.getLinha().getCodigo(),
                    p.getLinha().getNome(), p.getLinha().isSensivel(), p.getInicio(), p.getUnidade().getUuid(), p.getUnidade().getNome(),
                    p.getEquipe() != null ? p.getEquipe().getUuid() : null, p.getEquipe() != null ? p.getEquipe().getNome() : null,
                    p.getAcompanhamento() != null ? p.getAcompanhamento().getUuid() : null, p.getSituacao(), p.getEncerradoEm(), p.getMotivoEncerramento(),
                    emCurso && obrigatorias.stream().anyMatch(x -> x.getSituacao() == SituacaoEtapaPercurso.ATRASADA), atual,
                    (int) obrigatorias.stream().filter(feita).count(), obrigatorias.size(), p.getRegistradoEm(), calculadas));
        }
        return lista;
    }

    /** Os registros do paciente nos domínios, lidos uma vez. */
    private record Registros(List<Atendimento> atendimentos, List<ItemPedidoExame> exames, List<SolicitacaoRegulacao> regulacoes,
                             List<RegistroVacinacao> doses, List<Internacao> internacoes) {
    }

    private Registros registros(UUID pacienteId) {
        return new Registros(atendimentoRepository.findByPacienteUuid(pacienteId), itemExameRepository.findLiberadosDoPaciente(pacienteId),
                regulacaoRepository.findByPaciente_Uuid(pacienteId), vacinacaoRepository.findVigentesDoPaciente(pacienteId),
                internacaoRepository.findByPaciente_UuidOrderByAdmitidaEmDesc(pacienteId));
    }

    /** O que cumpre a etapa, a partir do início do percurso; na regulação, o andamento da solicitação ainda não realizada. */
    private List<PercursoLinhaCalculo.Evidencia> evidencias(EtapaLinhaCuidado e, Registros r, List<RegistroEtapaPercurso> registros, LocalDate inicio,
                                                            Map<UUID, String> andamentos) {
        Function<Instant, LocalDate> dia = i -> i.atZone(ZoneId.systemDefault()).toLocalDate();
        List<PercursoLinhaCalculo.Evidencia> lista = new ArrayList<>();
        switch (e.getTipo()) {
            case ATENDIMENTO -> r.atendimentos().stream()
                    .filter(a -> a.getStatus() == StatusAtendimento.CONCLUIDO && a.getDataHora() != null
                            && (e.getEspecialidade() == null || (a.getEspecialidade() != null && a.getEspecialidade().getUuid().equals(e.getEspecialidade().getUuid()))))
                    .forEach(a -> lista.add(new PercursoLinhaCalculo.Evidencia(a.getDataHora().toLocalDate(), "atendimento em "
                            + a.getDataHora().toLocalDate().format(DATA) + (a.getEspecialidade() != null ? " · " + a.getEspecialidade().getNome() : ""))));
            case EXAME -> r.exames().stream()
                    .filter(i -> i.getExame().getUuid().equals(e.getExame().getUuid()) && i.getResultadoAtual().getLiberadoEm() != null)
                    .forEach(i -> lista.add(new PercursoLinhaCalculo.Evidencia(dia.apply(i.getResultadoAtual().getLiberadoEm()), i.getExame().getNome()
                            + " liberado em " + dia.apply(i.getResultadoAtual().getLiberadoEm()).format(DATA))));
            case REGULACAO -> {
                List<SolicitacaoRegulacao> doProcedimento = r.regulacoes().stream()
                        .filter(s -> s.getProcedimento().getUuid().equals(e.getProcedimento().getUuid()) && !dia.apply(s.getSolicitadoEm()).isBefore(inicio))
                        .sorted(Comparator.comparing(SolicitacaoRegulacao::getSolicitadoEm)).toList();
                for (SolicitacaoRegulacao s : doProcedimento) {
                    if (s.getStatus() == StatusSolicitacaoRegulacao.REALIZADA) {
                        LocalDate quando = s.getConcluidoEm() != null ? dia.apply(s.getConcluidoEm())
                                : s.getDataHoraPrevista() != null ? s.getDataHoraPrevista().toLocalDate() : dia.apply(s.getSolicitadoEm());
                        lista.add(new PercursoLinhaCalculo.Evidencia(quando, e.getProcedimento().getNome() + " realizado em " + quando.format(DATA)));
                    }
                }
                doProcedimento.stream().filter(s -> ABERTAS.contains(s.getStatus())).reduce((a, b) -> b).ifPresent(s -> andamentos.put(e.getUuid(),
                        e.getProcedimento().getNome() + ": " + andamento(s)));
            }
            case VACINA -> r.doses().stream().filter(d -> d.getImunobiologico().getUuid().equals(e.getImunobiologico().getUuid()))
                    .forEach(d -> lista.add(new PercursoLinhaCalculo.Evidencia(d.getAplicadaEm().toLocalDate(), d.getImunobiologico().getSigla() + ", "
                            + d.getDose().rotulo().toLowerCase(Locale.ROOT) + " em " + d.getAplicadaEm().toLocalDate().format(DATA))));
            case INTERNACAO -> r.internacoes().stream().filter(i -> i.getAltaEm() != null && cidAceito(e.getCids(), i.getCid()))
                    .forEach(i -> lista.add(new PercursoLinhaCalculo.Evidencia(dia.apply(i.getAltaEm()), "internação"
                            + (i.getCid() != null ? " (" + i.getCid() + ")" : "") + " com alta em " + dia.apply(i.getAltaEm()).format(DATA))));
            case MANUAL -> registros.stream().filter(x -> x.getTipo() == TipoRegistroEtapa.CUMPRIDA && x.getEtapa().getUuid().equals(e.getUuid()))
                    .forEach(x -> lista.add(new PercursoLinhaCalculo.Evidencia(x.getData(), "registrado em " + x.getData().format(DATA)
                            + (x.getObservacao() != null ? " · " + x.getObservacao() : ""))));
        }
        return lista;
    }

    private static final Set<StatusSolicitacaoRegulacao> ABERTAS = Set.of(StatusSolicitacaoRegulacao.SOLICITADA, StatusSolicitacaoRegulacao.AUTORIZADA,
            StatusSolicitacaoRegulacao.AGENDADA, StatusSolicitacaoRegulacao.DEVOLVIDA);

    private static String andamento(SolicitacaoRegulacao s) {
        return switch (s.getStatus()) {
            case AGENDADA -> s.getDataHoraPrevista() != null ? "agendado para " + s.getDataHoraPrevista().toLocalDate().format(DATA) : "agendado";
            case AUTORIZADA -> "autorizado, aguardando a vaga";
            case DEVOLVIDA -> "devolvido à unidade solicitante";
            default -> "solicitado em " + s.getSolicitadoEm().atZone(ZoneId.systemDefault()).toLocalDate().format(DATA);
        };
    }

    /** Sem CIDs, qualquer alta; com eles, o CID da internação começa por algum. */
    static boolean cidAceito(String cids, String cid) {
        if (cids == null || cids.isBlank()) {
            return true;
        }
        if (cid == null) {
            return false;
        }
        String c = cid.replace(".", "").trim().toUpperCase(Locale.ROOT);
        return Arrays.stream(cids.split(",")).map(x -> x.replace(".", "").trim().toUpperCase(Locale.ROOT)).filter(x -> !x.isEmpty()).anyMatch(c::startsWith);
    }

    // ── Apoio ──────────────────────────────────────────────────────────────────────────────────

    private boolean podeVer(PercursoLinhaCuidado p) {
        return p.getLinha().getPrograma() == null || acompanhamentoService.podeVer(p.getLinha().getPrograma(), p.getUnidade());
    }

    private PercursoLinhaCuidado emCurso(UUID uuid) {
        PercursoLinhaCuidado p = percursoDe(uuid);
        controleDeAcesso.exigir(ACOMPANHAR, p.getUnidade());
        ContextoAuditoria.paciente(p.getPaciente().getUuid());
        ContextoAuditoria.registro(p.getUuid());
        if (p.getSituacao() != SituacaoPercursoLinha.EM_CURSO) {
            throw new ResourceUnprocessableEntityException("O percurso já foi encerrado.");
        }
        return p;
    }

    private PercursoLinhaCuidado percursoDe(UUID uuid) {
        return percursoRepository.findById(uuid).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar o percurso " + uuid + " em nossos registros."));
    }

    private LinhaDeCuidado linhaDe(UUID uuid) {
        return linhaRepository.findById(uuid).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar a linha de cuidado " + uuid + " em nossos registros."));
    }

    private LinhaCuidadoDto dto(LinhaDeCuidado l) {
        List<LinhaCuidadoDto.Etapa> etapas = etapaRepository.findDaLinha(l.getUuid()).stream().filter(EtapaLinhaCuidado::isAtiva)
                .map(e -> new LinhaCuidadoDto.Etapa(e.getUuid(), e.getOrdem(), e.getNome(), e.getTipo(),
                        e.getEspecialidade() != null ? e.getEspecialidade().getUuid() : null, e.getEspecialidade() != null ? e.getEspecialidade().getNome() : null,
                        e.getExame() != null ? e.getExame().getUuid() : null, e.getExame() != null ? e.getExame().getNome() : null,
                        e.getProcedimento() != null ? e.getProcedimento().getUuid() : null, e.getProcedimento() != null ? e.getProcedimento().getNome() : null,
                        e.getImunobiologico() != null ? e.getImunobiologico().getUuid() : null, e.getImunobiologico() != null ? e.getImunobiologico().getSigla() : null,
                        e.getCids(), e.getQuantidade(), e.getPrazoDias(), e.getPrazoDesde(), e.isObrigatoria(), e.isAtiva(), criterio(e)))
                .toList();
        return new LinhaCuidadoDto(l.getUuid(), l.getCodigo(), l.getNome(), l.getDescricao(), l.getPrograma() != null ? l.getPrograma().getUuid() : null,
                l.getPrograma() != null ? l.getPrograma().getNome() : null, l.isSensivel(), l.isAtiva(),
                percursoRepository.countByLinha_UuidAndSituacao(l.getUuid(), SituacaoPercursoLinha.EM_CURSO), etapas);
    }

    /** Como a etapa se cumpre, em palavras. */
    static String criterio(EtapaLinhaCuidado e) {
        String vezes = e.getQuantidade() > 1 ? e.getQuantidade() + " × " : "";
        String base = switch (e.getTipo()) {
            case ATENDIMENTO -> "Atendimento concluído" + (e.getEspecialidade() != null ? " de " + e.getEspecialidade().getNome() : "");
            case EXAME -> "Exame " + e.getExame().getNome() + " liberado";
            case REGULACAO -> e.getProcedimento().getNome() + " realizado pela regulação";
            case VACINA -> "Dose de " + e.getImunobiologico().getSigla();
            case INTERNACAO -> "Alta de internação" + (e.getCids() != null ? " com CID " + e.getCids().replace(",", ", ") : "");
            case MANUAL -> "Registro do profissional";
        };
        String prazo = e.getPrazoDias() == null ? "" : " · até " + e.getPrazoDias() + " dias "
                + (e.getPrazoDesde() == ReferenciaPrazoEtapa.INICIO ? "do início" : "da etapa anterior");
        return vezes + base + prazo;
    }

    private static UUID exigir(UUID id, String o) {
        if (id == null) {
            throw new ResourceBadRequestException("Informe " + o + " da etapa.");
        }
        return id;
    }

    private static String cids(String s) {
        String t = texto(s);
        return t == null ? null : Arrays.stream(t.split(",")).map(x -> x.replace(".", "").trim().toUpperCase(Locale.ROOT)).filter(x -> !x.isEmpty())
                .collect(Collectors.joining(","));
    }

    private static String codigo(String s) {
        return s.trim().toUpperCase(Locale.ROOT).replace(' ', '_');
    }

    private static String texto(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }
}
