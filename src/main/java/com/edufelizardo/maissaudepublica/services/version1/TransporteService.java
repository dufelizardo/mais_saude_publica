package com.edufelizardo.maissaudepublica.services.version1;

import com.edufelizardo.maissaudepublica.config.ContextoAuditoria;
import com.edufelizardo.maissaudepublica.config.UsuarioAutenticado;
import com.edufelizardo.maissaudepublica.exceptions.ResourceBadRequestException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceConflictException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceNotFoundException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceUnprocessableEntityException;
import com.edufelizardo.maissaudepublica.models.PlantaoViatura;
import com.edufelizardo.maissaudepublica.models.Profissional;
import com.edufelizardo.maissaudepublica.models.SituacaoVeiculoRegistro;
import com.edufelizardo.maissaudepublica.models.TripulantePlantao;
import com.edufelizardo.maissaudepublica.models.UnidadeDeSaude;
import com.edufelizardo.maissaudepublica.models.Veiculo;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.PlantaoViaturaRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.SituacaoVeiculoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.VeiculoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.TransporteDtos;
import com.edufelizardo.maissaudepublica.models.enuns.FuncaoTripulante;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoVeiculo;
import com.edufelizardo.maissaudepublica.models.enuns.TipoRecursoUrgencia;
import com.edufelizardo.maissaudepublica.models.enuns.TipoVeiculo;
import com.edufelizardo.maissaudepublica.repositories.PlantaoViaturaRepository;
import com.edufelizardo.maissaudepublica.repositories.ProfissionalRepository;
import com.edufelizardo.maissaudepublica.repositories.SituacaoVeiculoRegistroRepository;
import com.edufelizardo.maissaudepublica.repositories.UnidadeDeSaudeRepository;
import com.edufelizardo.maissaudepublica.repositories.UnidadePerfilOperacionalRepository;
import com.edufelizardo.maissaudepublica.repositories.VeiculoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Transporte, fatia T1 (ADR-0178): a frota com a situação em histórico, o plantão de cada viatura com a tripulação mínima e a viatura
 * que o SAMU empenha. A Regulação de urgência ({@link OcorrenciaUrgenciaService}) escolhe a viatura e a põe em serviço e de volta.
 */
@Service
public class TransporteService {

    public static final String CONSULTAR = "TRANSPORTE.CONSULTAR";
    public static final String GERENCIAR = "TRANSPORTE.GERENCIAR";
    static final String PERFIL_MOVEL = "ATENCAO_MOVEL";
    private static final Pattern PLACA = Pattern.compile("^[A-Z]{3}[0-9][A-Z0-9][0-9]{2}$");
    private static final Duration PLANTAO_MAXIMO = Duration.ofHours(24);

    @Autowired private VeiculoRepository veiculoRepository;
    @Autowired private SituacaoVeiculoRegistroRepository situacaoRepository;
    @Autowired private PlantaoViaturaRepository plantaoRepository;
    @Autowired private UnidadeDeSaudeRepository unidadeRepository;
    @Autowired private UnidadePerfilOperacionalRepository perfilRepository;
    @Autowired private ProfissionalRepository profissionalRepository;
    @Autowired private ControleDeAcesso controleDeAcesso;

    // ── Frota ─────────────────────────────────────────────────────────────────────────────────────

    @Transactional
    public TransporteDtos.Veiculo criar(VeiculoRequestDto dto) {
        UnidadeDeSaude u = unidade(dto.getUnidadeId());
        controleDeAcesso.exigir(GERENCIAR, u);
        String placa = placa(dto.getPlaca());
        String prefixo = dto.getPrefixo().trim().toUpperCase(Locale.ROOT);
        if (veiculoRepository.existsByPlaca(placa)) {
            throw new ResourceConflictException("Já existe um veículo com a placa " + placa + ".");
        }
        if (veiculoRepository.existsByPrefixoIgnoreCase(prefixo)) {
            throw new ResourceConflictException("Já existe um veículo com o prefixo " + prefixo + ".");
        }
        exigirLotacao(dto.getTipo(), u);
        Veiculo v = new Veiculo();
        preencher(v, dto, placa, prefixo, u);
        v.setSituacao(SituacaoVeiculo.DISPONIVEL);
        veiculoRepository.save(v);
        registrarSituacao(v, "Cadastro na frota.");
        ContextoAuditoria.registro(v.getUuid());
        return detalhe(v);
    }

    @Transactional
    public TransporteDtos.Veiculo atualizar(UUID uuid, VeiculoRequestDto dto) {
        Veiculo v = travar(uuid);
        controleDeAcesso.exigir(GERENCIAR, v.getUnidade());
        UnidadeDeSaude u = unidade(dto.getUnidadeId());
        if (!u.getUuid().equals(v.getUnidade().getUuid())) {
            controleDeAcesso.exigir(GERENCIAR, u);
        }
        String placa = placa(dto.getPlaca());
        String prefixo = dto.getPrefixo().trim().toUpperCase(Locale.ROOT);
        if (veiculoRepository.existsByPlacaAndUuidNot(placa, uuid)) {
            throw new ResourceConflictException("Já existe um veículo com a placa " + placa + ".");
        }
        if (veiculoRepository.existsByPrefixoIgnoreCaseAndUuidNot(prefixo, uuid)) {
            throw new ResourceConflictException("Já existe um veículo com o prefixo " + prefixo + ".");
        }
        exigirLotacao(dto.getTipo(), u);
        if (dto.getTipo() != v.getTipo() && v.getSituacao() == SituacaoVeiculo.EM_SERVICO) {
            throw new ResourceUnprocessableEntityException("O veículo está em serviço; o tipo muda depois.");
        }
        preencher(v, dto, placa, prefixo, u);
        veiculoRepository.save(v);
        ContextoAuditoria.registro(v.getUuid());
        return detalhe(v);
    }

    /** Manutenção, volta a disponível ou inativação; "em serviço" vem do SAMU. */
    @Transactional
    public TransporteDtos.Veiculo mudarSituacao(UUID uuid, SituacaoVeiculoRequestDto dto) {
        Veiculo v = travar(uuid);
        controleDeAcesso.exigir(GERENCIAR, v.getUnidade());
        if (dto.getSituacao() == SituacaoVeiculo.EM_SERVICO) {
            throw new ResourceBadRequestException("Em serviço vem do empenho pelo SAMU, da saída da viagem ou da cessão à emergência, não se marca à mão.");
        }
        if (v.getSituacao() == SituacaoVeiculo.EM_SERVICO) {
            throw new ResourceUnprocessableEntityException("O veículo está em serviço numa ocorrência, viagem ou emergência; espere o fim dela.");
        }
        if (v.getSituacao() == dto.getSituacao()) {
            throw new ResourceUnprocessableEntityException("O veículo já está " + dto.getSituacao() + ".");
        }
        if (dto.getQuilometragem() != null) {
            if (v.getQuilometragem() != null && dto.getQuilometragem() < v.getQuilometragem()) {
                throw new ResourceUnprocessableEntityException("A quilometragem não pode voltar: está em " + v.getQuilometragem() + ".");
            }
            v.setQuilometragem(dto.getQuilometragem());
        }
        v.setSituacao(dto.getSituacao());
        veiculoRepository.save(v);
        registrarSituacao(v, dto.getMotivo().trim());
        return detalhe(v);
    }

    @Transactional(readOnly = true)
    public List<TransporteDtos.Veiculo> listar(UUID unidadeId, SituacaoVeiculo situacao, TipoVeiculo tipo) {
        controleDeAcesso.exigirAlguma(CONSULTAR, GERENCIAR);
        Instant agora = Instant.now();
        Map<UUID, PlantaoViatura> vigentes = plantoesVigentes(agora);
        Stream<Veiculo> base = controleDeAcesso.filtrar(veiculoRepository.findTodos().stream(), v -> Stream.of(v.getUnidade()), CONSULTAR, GERENCIAR);
        return base.filter(v -> unidadeId == null || v.getUnidade().getUuid().equals(unidadeId))
                .filter(v -> situacao == null || v.getSituacao() == situacao)
                .filter(v -> tipo == null || v.getTipo() == tipo)
                .map(v -> dto(v, vigentes.get(v.getUuid()), null, null, agora)).toList();
    }

    @Transactional(readOnly = true)
    public TransporteDtos.Veiculo buscar(UUID uuid) {
        Veiculo v = veiculoRepository.findById(uuid).orElseThrow(() -> naoEncontrado(uuid));
        controleDeAcesso.exigirVisivel(v.getUnidade(), CONSULTAR, GERENCIAR);
        return detalhe(v);
    }

    // ── Plantão ───────────────────────────────────────────────────────────────────────────────────

    @Transactional
    public TransporteDtos.Plantao criarPlantao(PlantaoViaturaRequestDto dto) {
        Veiculo v = travar(dto.getVeiculoId());
        controleDeAcesso.exigir(GERENCIAR, v.getUnidade());
        if (!dto.getFim().isAfter(dto.getInicio())) {
            throw new ResourceBadRequestException("O fim do plantão precisa ser depois do início.");
        }
        if (Duration.between(dto.getInicio(), dto.getFim()).compareTo(PLANTAO_MAXIMO) > 0) {
            throw new ResourceBadRequestException("O plantão vai até 24 horas.");
        }
        if (v.getSituacao() == SituacaoVeiculo.INATIVO) {
            throw new ResourceUnprocessableEntityException("O veículo " + v.getPrefixo() + " está inativo.");
        }
        Set<String> matriculas = new HashSet<>();
        List<TripulantePlantao> tripulacao = new ArrayList<>();
        PlantaoViatura p = new PlantaoViatura();
        for (PlantaoViaturaRequestDto.Tripulante t : dto.getTripulacao()) {
            String m = t.getMatricula().trim();
            if (!matriculas.add(m)) {
                throw new ResourceBadRequestException("O tripulante " + m + " aparece duas vezes.");
            }
            Profissional prof = profissionalRepository.findByMatricula(m).orElseThrow(() -> new ResourceNotFoundException(
                    "Não foi possível encontrar um profissional com a matrícula " + m + " em nossos registros."));
            if (!prof.isAtivo()) {
                throw new ResourceUnprocessableEntityException(prof.getNome() + " está inativo.");
            }
            tripulacao.add(new TripulantePlantao(null, p, prof, t.getFuncao()));
        }
        exigirComposicao(v.getTipo(), tripulacao.stream().map(TripulantePlantao::getFuncao).collect(Collectors.toSet()));
        List<PlantaoViatura> cruzam = plantaoRepository.findNoIntervalo(dto.getInicio(), dto.getFim());
        if (cruzam.stream().anyMatch(x -> x.getVeiculo().getUuid().equals(v.getUuid()))) {
            throw new ResourceConflictException("O veículo " + v.getPrefixo() + " já tem plantão nesse período.");
        }
        for (PlantaoViatura x : cruzam) {
            for (TripulantePlantao t : x.getTripulacao()) {
                if (matriculas.contains(t.getProfissional().getMatricula())) {
                    throw new ResourceConflictException(t.getProfissional().getNome() + " já está no plantão de " + x.getVeiculo().getPrefixo()
                            + " nesse período.");
                }
            }
        }
        p.setVeiculo(v);
        p.setInicio(dto.getInicio());
        p.setFim(dto.getFim());
        p.setObservacao(texto(dto.getObservacao()));
        p.setRegistradoPorCpf(UsuarioAutenticado.cpf());
        p.getTripulacao().addAll(tripulacao);
        plantaoRepository.save(p);
        ContextoAuditoria.registro(p.getUuid());
        ContextoAuditoria.detalhe("Plantão de " + v.getPrefixo() + ".");
        return plantao(p, Instant.now());
    }

    /** Encerra agora o plantão vigente, ou cancela um futuro (fim no início). */
    @Transactional
    public TransporteDtos.Plantao encerrarPlantao(UUID uuid) {
        PlantaoViatura p = plantaoRepository.findById(uuid).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar o plantão " + uuid + " em nossos registros."));
        controleDeAcesso.exigir(GERENCIAR, p.getVeiculo().getUnidade());
        Instant agora = Instant.now();
        if (!p.getFim().isAfter(agora)) {
            throw new ResourceUnprocessableEntityException("O plantão já terminou.");
        }
        p.setFim(p.getInicio().isAfter(agora) ? p.getInicio() : agora);
        plantaoRepository.save(p);
        return plantao(p, agora);
    }

    /** Plantões que cruzam o dia (padrão: hoje) e os próximos dias, até 14. */
    @Transactional(readOnly = true)
    public List<TransporteDtos.Plantao> plantoes(UUID unidadeId, Integer dias) {
        controleDeAcesso.exigirAlguma(CONSULTAR, GERENCIAR);
        Instant agora = Instant.now();
        Instant de = agora.minus(Duration.ofHours(24));
        Instant ate = agora.plus(Duration.ofDays(dias == null ? 7 : Math.max(1, Math.min(dias, 14))));
        return controleDeAcesso.filtrar(plantaoRepository.findNoIntervalo(de, ate).stream(), p -> Stream.of(p.getVeiculo().getUnidade()), CONSULTAR, GERENCIAR)
                .filter(p -> unidadeId == null || p.getVeiculo().getUnidade().getUuid().equals(unidadeId))
                .map(p -> plantao(p, agora)).toList();
    }

    // ── Para o SAMU ───────────────────────────────────────────────────────────────────────────────

    /** Viaturas do SAMU disponíveis com plantão vigente, menos as que {@code ocupada} diz estarem numa ocorrência aberta. */
    @Transactional(readOnly = true)
    public List<TransporteDtos.ViaturaDisponivel> viaturasDisponiveis(Predicate<UUID> ocupada) {
        Instant agora = Instant.now();
        Map<UUID, PlantaoViatura> vigentes = plantoesVigentes(agora);
        return veiculoRepository.findTodos().stream()
                .filter(v -> v.getTipo().doSamu() && v.getSituacao() == SituacaoVeiculo.DISPONIVEL && vigentes.containsKey(v.getUuid()))
                .filter(v -> !ocupada.test(v.getUuid()))
                .map(v -> new TransporteDtos.ViaturaDisponivel(v.getUuid(), v.getPrefixo(), v.getTipo(), v.getUnidade().getNome(),
                        vigentes.get(v.getUuid()).getFim()))
                .sorted(Comparator.comparing(TransporteDtos.ViaturaDisponivel::tipo).thenComparing(TransporteDtos.ViaturaDisponivel::prefixo)).toList();
    }

    /**
     * A viatura escolhida pela regulação: disponível, com plantão vigente e do tipo pedido (OUTRO aceita qualquer). Sem tipo pedido (a
     * transferência), o tipo da viatura vira o recurso.
     */
    @Transactional
    public Veiculo viaturaParaEmpenho(UUID veiculoId, TipoRecursoUrgencia pedido) {
        Veiculo v = travar(veiculoId);
        if (v.getSituacao() != SituacaoVeiculo.DISPONIVEL) {
            throw new ResourceUnprocessableEntityException("A viatura " + v.getPrefixo() + " não está disponível (" + v.getSituacao() + ").");
        }
        if (plantoesVigentes(Instant.now()).get(v.getUuid()) == null) {
            throw new ResourceUnprocessableEntityException("A viatura " + v.getPrefixo() + " está sem plantão agora.");
        }
        if (pedido != null && pedido != TipoRecursoUrgencia.OUTRO && recurso(v.getTipo()) != pedido) {
            throw new ResourceUnprocessableEntityException("A viatura " + v.getPrefixo() + " é " + v.getTipo() + "; o recurso pedido é " + pedido + ".");
        }
        return v;
    }

    /** O tipo de recurso da urgência que a viatura é. */
    public static TipoRecursoUrgencia recurso(TipoVeiculo t) {
        return switch (t) {
            case USA -> TipoRecursoUrgencia.USA;
            case USB -> TipoRecursoUrgencia.USB;
            case MOTOLANCIA -> TipoRecursoUrgencia.MOTOLANCIA;
            case AEROMEDICO -> TipoRecursoUrgencia.AEROMEDICO;
            default -> TipoRecursoUrgencia.OUTRO;
        };
    }

    @Transactional
    public void colocarEmServico(Veiculo v, String motivo) {
        Veiculo atual = travar(v.getUuid());
        if (atual.getSituacao() != SituacaoVeiculo.DISPONIVEL) {
            throw new ResourceUnprocessableEntityException("A viatura " + atual.getPrefixo() + " não está disponível (" + atual.getSituacao() + ").");
        }
        atual.setSituacao(SituacaoVeiculo.EM_SERVICO);
        veiculoRepository.save(atual);
        registrarSituacao(atual, motivo);
    }

    /** Volta a disponível se estava em serviço; noutra situação (alguém pôs em manutenção), fica como está. */
    @Transactional
    public void liberar(Veiculo v, String motivo) {
        Veiculo atual = travar(v.getUuid());
        if (atual.getSituacao() == SituacaoVeiculo.EM_SERVICO) {
            atual.setSituacao(SituacaoVeiculo.DISPONIVEL);
            veiculoRepository.save(atual);
            registrarSituacao(atual, motivo);
        }
    }

    // ── Apoio ─────────────────────────────────────────────────────────────────────────────────────

    /** A composição mínima: USB, condutor e técnico; USA, condutor, enfermeiro e médico; motolância, técnico ou enfermeiro. */
    static void exigirComposicao(TipoVeiculo tipo, Set<FuncaoTripulante> funcoes) {
        List<String> faltam = new ArrayList<>();
        switch (tipo) {
            case USB -> {
                if (!funcoes.contains(FuncaoTripulante.CONDUTOR)) faltam.add("condutor");
                if (!funcoes.contains(FuncaoTripulante.TECNICO_ENFERMAGEM) && !funcoes.contains(FuncaoTripulante.ENFERMEIRO)) faltam.add("técnico de enfermagem");
            }
            case USA, AEROMEDICO -> {
                if (tipo == TipoVeiculo.USA && !funcoes.contains(FuncaoTripulante.CONDUTOR)) faltam.add("condutor");
                if (!funcoes.contains(FuncaoTripulante.ENFERMEIRO)) faltam.add("enfermeiro");
                if (!funcoes.contains(FuncaoTripulante.MEDICO)) faltam.add("médico");
            }
            case MOTOLANCIA -> {
                if (!funcoes.contains(FuncaoTripulante.TECNICO_ENFERMAGEM) && !funcoes.contains(FuncaoTripulante.ENFERMEIRO)) {
                    faltam.add("técnico de enfermagem ou enfermeiro");
                }
            }
            default -> {
                if (!funcoes.contains(FuncaoTripulante.CONDUTOR)) faltam.add("condutor");
            }
        }
        if (!faltam.isEmpty()) {
            throw new ResourceUnprocessableEntityException("A tripulação de " + tipo + " precisa de: " + String.join(", ", faltam) + ".");
        }
    }

    private void exigirLotacao(TipoVeiculo tipo, UnidadeDeSaude u) {
        if (tipo.doSamu() && perfilRepository.findDaUnidade(u.getUuid()).stream()
                .noneMatch(a -> a.getFim() == null && PERFIL_MOVEL.equals(a.getPerfil().getCodigo()))) {
            throw new ResourceUnprocessableEntityException("Viatura do SAMU fica em unidade com o perfil Atenção móvel; " + u.getNome() + " não tem.");
        }
    }

    private void preencher(Veiculo v, VeiculoRequestDto dto, String placa, String prefixo, UnidadeDeSaude u) {
        v.setPlaca(placa);
        v.setPrefixo(prefixo);
        v.setTipo(dto.getTipo());
        v.setModelo(texto(dto.getModelo()));
        v.setAno(dto.getAno());
        v.setLugares(dto.getLugares());
        v.setMaca(Boolean.TRUE.equals(dto.getMaca()));
        v.setCadeiraDeRodas(Boolean.TRUE.equals(dto.getCadeiraDeRodas()));
        if (dto.getQuilometragem() != null) {
            if (v.getQuilometragem() != null && dto.getQuilometragem() < v.getQuilometragem()) {
                throw new ResourceUnprocessableEntityException("A quilometragem não pode voltar: está em " + v.getQuilometragem() + ".");
            }
            v.setQuilometragem(dto.getQuilometragem());
        }
        v.setUnidade(u);
        v.setObservacao(texto(dto.getObservacao()));
    }

    private void registrarSituacao(Veiculo v, String motivo) {
        situacaoRepository.save(new SituacaoVeiculoRegistro(null, v, v.getSituacao(), Instant.now(), motivo, UsuarioAutenticado.cpf()));
        ContextoAuditoria.unidade(v.getUnidade());
        ContextoAuditoria.detalhe("Veículo " + v.getPrefixo() + ": " + v.getSituacao() + ".");
    }

    private Map<UUID, PlantaoViatura> plantoesVigentes(Instant agora) {
        return plantaoRepository.findNoIntervalo(agora, agora.plusMillis(1)).stream()
                .collect(Collectors.toMap(p -> p.getVeiculo().getUuid(), p -> p, (a, b) -> a));
    }

    private TransporteDtos.Veiculo detalhe(Veiculo v) {
        Instant agora = Instant.now();
        List<TransporteDtos.Situacao> historico = situacaoRepository.findByVeiculo_UuidOrderByInicioDesc(v.getUuid()).stream()
                .map(s -> new TransporteDtos.Situacao(s.getSituacao(), s.getInicio(), s.getMotivo())).toList();
        List<TransporteDtos.Plantao> plantoes = plantaoRepository.findNoIntervalo(agora.minus(Duration.ofDays(7)), agora.plus(Duration.ofDays(14))).stream()
                .filter(p -> p.getVeiculo().getUuid().equals(v.getUuid())).map(p -> plantao(p, agora)).toList();
        PlantaoViatura vigente = plantoesVigentes(agora).get(v.getUuid());
        return dto(v, vigente, historico, plantoes, agora);
    }

    private TransporteDtos.Veiculo dto(Veiculo v, PlantaoViatura vigente, List<TransporteDtos.Situacao> historico, List<TransporteDtos.Plantao> plantoes,
                                       Instant agora) {
        Instant desde = historico != null && !historico.isEmpty() ? historico.get(0).inicio() : null;
        return new TransporteDtos.Veiculo(v.getUuid(), v.getPlaca(), v.getPrefixo(), v.getTipo(), v.getTipo().doSamu(), v.getModelo(), v.getAno(),
                v.getLugares(), v.isMaca(), v.isCadeiraDeRodas(), v.getQuilometragem(), v.getUnidade().getUuid(), v.getUnidade().getNome(), v.getSituacao(),
                desde, v.getObservacao(), vigente != null ? plantao(vigente, agora) : null, historico, plantoes);
    }

    private TransporteDtos.Plantao plantao(PlantaoViatura p, Instant agora) {
        return new TransporteDtos.Plantao(p.getUuid(), p.getVeiculo().getUuid(), p.getVeiculo().getPrefixo(), p.getVeiculo().getTipo(), p.getInicio(),
                p.getFim(), p.vigenteEm(agora), p.getObservacao(), p.getTripulacao().stream()
                .sorted(Comparator.comparing(TripulantePlantao::getFuncao))
                .map(t -> new TransporteDtos.Tripulante(t.getProfissional().getUuid(), t.getProfissional().getNome(), t.getProfissional().getMatricula(),
                        t.getFuncao())).toList());
    }

    private Veiculo travar(UUID uuid) {
        return veiculoRepository.findByIdParaAtualizar(uuid).orElseThrow(() -> naoEncontrado(uuid));
    }

    private static String placa(String bruta) {
        String p = bruta.replaceAll("[\\s-]", "").toUpperCase(Locale.ROOT);
        if (!PLACA.matcher(p).matches()) {
            throw new ResourceBadRequestException("Placa inválida: use o padrão ABC1234 ou o Mercosul ABC1D23.");
        }
        return p;
    }

    private UnidadeDeSaude unidade(UUID id) {
        return unidadeRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar uma unidade de saúde com o id " + id + " em nossos registros."));
    }

    private ResourceNotFoundException naoEncontrado(UUID uuid) {
        return new ResourceNotFoundException("Não foi possível encontrar o veículo " + uuid + " em nossos registros.");
    }

    private static String texto(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }
}
