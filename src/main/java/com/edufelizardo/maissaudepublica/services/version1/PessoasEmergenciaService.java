package com.edufelizardo.maissaudepublica.services.version1;

import com.edufelizardo.maissaudepublica.config.ContextoAuditoria;
import com.edufelizardo.maissaudepublica.config.UsuarioAutenticado;
import com.edufelizardo.maissaudepublica.exceptions.ResourceBadRequestException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceConflictException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceNotFoundException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceUnprocessableEntityException;
import com.edufelizardo.maissaudepublica.models.AcompanhamentoProgramatico;
import com.edufelizardo.maissaudepublica.models.AtribuicaoMicroarea;
import com.edufelizardo.maissaudepublica.models.Emergencia;
import com.edufelizardo.maissaudepublica.models.EncaminhamentoIntersetorial;
import com.edufelizardo.maissaudepublica.models.Endereco;
import com.edufelizardo.maissaudepublica.models.Imovel;
import com.edufelizardo.maissaudepublica.models.InstituicaoExterna;
import com.edufelizardo.maissaudepublica.models.MembroNucleo;
import com.edufelizardo.maissaudepublica.models.Microarea;
import com.edufelizardo.maissaudepublica.models.Paciente;
import com.edufelizardo.maissaudepublica.models.PacienteDeslocado;
import com.edufelizardo.maissaudepublica.models.SolicitacaoTransporte;
import com.edufelizardo.maissaudepublica.models.UnidadeDeSaude;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.DeslocadoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.EncaminhamentoIntersetorialRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.RetornoEncaminhamentoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.PessoasEmergenciaDtos;
import com.edufelizardo.maissaudepublica.models.enuns.DominioOrigemEncaminhamento;
import com.edufelizardo.maissaudepublica.models.enuns.LocalDeslocamento;
import com.edufelizardo.maissaudepublica.models.enuns.MarcadorVulnerabilidade;
import com.edufelizardo.maissaudepublica.models.enuns.MotivoTransporte;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoDeslocamento;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoEmergencia;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoSolicitacaoTransporte;
import com.edufelizardo.maissaudepublica.repositories.AcompanhamentoProgramaticoRepository;
import com.edufelizardo.maissaudepublica.repositories.AtribuicaoMicroareaRepository;
import com.edufelizardo.maissaudepublica.repositories.EmergenciaRepository;
import com.edufelizardo.maissaudepublica.repositories.MembroNucleoRepository;
import com.edufelizardo.maissaudepublica.repositories.PacienteDeslocadoRepository;
import com.edufelizardo.maissaudepublica.repositories.PacienteRepository;
import com.edufelizardo.maissaudepublica.repositories.ProfissionalRepository;
import com.edufelizardo.maissaudepublica.repositories.SolicitacaoTransporteRepository;
import com.edufelizardo.maissaudepublica.repositories.UnidadeDeSaudeRepository;
import org.locationtech.jts.geom.Geometry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * As pessoas na emergência, fatia E4a (ADR-0193): os vulneráveis da área afetada, calculados do que os donos já sabem (Território,
 * Programas, Transporte), e os pacientes deslocados, com os destinos (abrigos), o transporte ligado e o encaminhamento intersetorial.
 * Tudo é dado de paciente: pede {@code EMERGENCIA.COORDENAR} e {@code PACIENTE.CONSULTAR}, e cada leitura entra na auditoria.
 */
@Service
public class PessoasEmergenciaService {

    static final String COORDENAR = "EMERGENCIA.COORDENAR";
    static final String PACIENTE = "PACIENTE.CONSULTAR";
    private static final DominioOrigemEncaminhamento ORIGEM = DominioOrigemEncaminhamento.EMERGENCIAS;

    @Autowired private EmergenciaRepository emergenciaRepository;
    @Autowired private MembroNucleoRepository membroRepository;
    @Autowired private AtribuicaoMicroareaRepository atribuicaoRepository;
    @Autowired private AcompanhamentoProgramaticoRepository acompanhamentoRepository;
    @Autowired private SolicitacaoTransporteRepository solicitacaoRepository;
    @Autowired private PacienteDeslocadoRepository deslocadoRepository;
    @Autowired private PacienteRepository pacienteRepository;
    @Autowired private UnidadeDeSaudeRepository unidadeRepository;
    @Autowired private ProfissionalRepository profissionalRepository;
    @Autowired private InstituicaoExternaService instituicaoService;
    @Autowired private EncaminhamentoIntersetorialService encaminhamentoService;
    @Autowired private ControleDeAcesso controleDeAcesso;

    // ── Vulneráveis ───────────────────────────────────────────────────────────────────────────────

    /**
     * As pessoas vigentes dos domicílios nas microáreas afetadas ou em imóvel dentro do polígono, com os marcadores. Por padrão só quem
     * tem algum marcador; {@code todos} traz todas. Programa sensível não aparece, nem como marcador.
     */
    @Transactional(readOnly = true)
    public PessoasEmergenciaDtos.Vulneraveis vulneraveis(UUID emergenciaId, boolean todos, MarcadorVulnerabilidade marcador, UUID microareaId) {
        exigirAcesso();
        Emergencia em = emergencia(emergenciaId);
        Set<UUID> microareas = em.getMicroareas().stream().map(Microarea::getUuid).collect(Collectors.toSet());
        Geometry area = GeometriaTerritorial.jts(em.getGeometria());
        List<MembroNucleo> naArea = membroRepository.findVigentesComImovel().stream().filter(m -> {
            Imovel i = m.getNucleo().getDomicilio().getImovel();
            return microareas.contains(i.getMicroarea().getUuid())
                    || (area != null && i.getLatitude() != null && i.getLongitude() != null && GeometriaTerritorial.contem(area, i.getLatitude(), i.getLongitude()));
        }).toList();
        Set<UUID> pacientes = naArea.stream().map(m -> m.getPaciente().getUuid()).collect(Collectors.toSet());

        Map<UUID, String> acs = new HashMap<>();
        for (AtribuicaoMicroarea a : atribuicaoRepository.findByFimIsNull()) {
            acs.putIfAbsent(a.getMicroarea().getUuid(), a.getMembro().getProfissional().getNome());
        }
        Map<UUID, List<String>> programas = new HashMap<>();
        Set<UUID> gestantes = new HashSet<>();
        for (AcompanhamentoProgramatico a : acompanhamentoRepository.findVigentes()) {
            UUID p = a.getPaciente().getUuid();
            if (!pacientes.contains(p) || a.getPrograma().isSensivel()) {
                continue;
            }
            programas.computeIfAbsent(p, k -> new ArrayList<>()).add(a.getPrograma().getNome());
            if (a.getPrograma().isGestante()) {
                gestantes.add(p);
            }
        }
        LocalDateTime agora = LocalDateTime.now();
        Set<UUID> dialise = new HashSet<>();
        Set<UUID> oxigenio = new HashSet<>();
        for (SolicitacaoTransporte s : solicitacaoRepository.findNoPeriodo(agora.minusDays(90), agora.plusDays(120))) {
            UUID p = s.getPaciente().getUuid();
            if (!pacientes.contains(p) || s.getSituacao() == SituacaoSolicitacaoTransporte.CANCELADA
                    || s.getSituacao() == SituacaoSolicitacaoTransporte.NEGADA) {
                continue;
            }
            if ((s.getMotivo() == MotivoTransporte.HEMODIALISE || s.getMotivo() == MotivoTransporte.TERAPIA_SERIADA) && !s.getDataHora().isBefore(agora)) {
                dialise.add(p);
            }
            if (s.isOxigenio()) {
                oxigenio.add(p);
            }
        }
        Map<UUID, String> deslocados = deslocadoRepository.findDaEmergencia(em.getUuid()).stream()
                .filter(d -> d.getSituacao() == SituacaoDeslocamento.DESLOCADO)
                .collect(Collectors.toMap(d -> d.getPaciente().getUuid(), PacienteDeslocado::destinoNome, (a, b) -> a));

        LocalDate hoje = LocalDate.now();
        List<PessoasEmergenciaDtos.Vulneravel> lista = new ArrayList<>();
        Set<UUID> vistos = new HashSet<>();
        int comMarcador = 0;
        for (MembroNucleo m : naArea) {
            Paciente p = m.getPaciente();
            if (!vistos.add(p.getUuid()) || !p.isAtivo()) {
                continue;
            }
            Integer idade = p.getDataNascimento() != null ? Period.between(p.getDataNascimento(), hoje).getYears() : null;
            List<MarcadorVulnerabilidade> marcadores = new ArrayList<>();
            if (idade != null && idade >= 60) {
                marcadores.add(MarcadorVulnerabilidade.IDOSO);
            }
            if (idade != null && idade < 5) {
                marcadores.add(MarcadorVulnerabilidade.CRIANCA);
            }
            if (gestantes.contains(p.getUuid())) {
                marcadores.add(MarcadorVulnerabilidade.GESTANTE);
            }
            List<String> progs = programas.getOrDefault(p.getUuid(), List.of());
            if (!progs.isEmpty()) {
                marcadores.add(MarcadorVulnerabilidade.PROGRAMA);
            }
            if (dialise.contains(p.getUuid())) {
                marcadores.add(MarcadorVulnerabilidade.DIALISE_TERAPIA);
            }
            if (oxigenio.contains(p.getUuid())) {
                marcadores.add(MarcadorVulnerabilidade.OXIGENIO);
            }
            comMarcador += marcadores.isEmpty() ? 0 : 1;
            Microarea mi = m.getNucleo().getDomicilio().getImovel().getMicroarea();
            if (!todos && marcadores.isEmpty()) {
                continue;
            }
            if (marcador != null && !marcadores.contains(marcador)) {
                continue;
            }
            if (microareaId != null && !mi.getUuid().equals(microareaId)) {
                continue;
            }
            Endereco e = m.getNucleo().getDomicilio().getImovel().getEndereco();
            lista.add(new PessoasEmergenciaDtos.Vulneravel(p.getUuid(), p.getNome(), idade,
                    p.getTelefones() == null ? null : p.getTelefones().stream().sorted().findFirst().orElse(null),
                    endereco(e != null ? e : p.getEndereco()), mi.getUuid(), mi.getArea().getNome() + " " + mi.getCodigo(), acs.get(mi.getUuid()),
                    marcadores, progs.stream().sorted().toList(), deslocados.containsKey(p.getUuid()), deslocados.get(p.getUuid())));
        }
        lista.sort(Comparator.comparing((PessoasEmergenciaDtos.Vulneravel v) -> -v.marcadores().size())
                .thenComparing(PessoasEmergenciaDtos.Vulneravel::microarea).thenComparing(PessoasEmergenciaDtos.Vulneravel::nome, String.CASE_INSENSITIVE_ORDER));
        ContextoAuditoria.registro(em.getUuid());
        ContextoAuditoria.detalhe("Lista de vulneráveis da emergência: " + lista.size() + " pessoa(s).");
        return new PessoasEmergenciaDtos.Vulneraveis(vistos.size(), comMarcador, lista);
    }

    /** A mesma lista em CSV (separador ;), para a busca ativa em campo. */
    @Transactional(readOnly = true)
    public String vulneraveisCsv(UUID emergenciaId, boolean todos, MarcadorVulnerabilidade marcador, UUID microareaId) {
        StringBuilder sb = new StringBuilder("Nome;Idade;Telefone;Endereço;Microárea;ACS;Marcadores;Programas;Deslocado para\n");
        for (PessoasEmergenciaDtos.Vulneravel v : vulneraveis(emergenciaId, todos, marcador, microareaId).lista()) {
            sb.append(String.join(";", celula(v.nome()), celula(v.idade()), celula(v.telefone()), celula(v.endereco()), celula(v.microarea()),
                    celula(v.acsNome()), celula(v.marcadores().stream().map(Enum::name).collect(Collectors.joining(", "))),
                    celula(String.join(", ", v.programas())), celula(v.deslocadoPara()))).append('\n');
        }
        return sb.toString();
    }

    // ── Deslocados ────────────────────────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<PessoasEmergenciaDtos.Deslocado> deslocados(UUID emergenciaId) {
        exigirAcesso();
        Emergencia em = emergencia(emergenciaId);
        ContextoAuditoria.registro(em.getUuid());
        return lista(em);
    }

    /**
     * Registra o paciente fora de casa: a emergência ativa (422); o paciente (404) já deslocado em aberto nela, 409; o destino com o que
     * ele pede (400); a saída no futuro, 400; o transporte de outro paciente, 422.
     */
    @Transactional
    public List<PessoasEmergenciaDtos.Deslocado> registrar(UUID emergenciaId, DeslocadoRequestDto dto) {
        exigirAcesso();
        Emergencia em = ativa(emergenciaId);
        if (dto.getPacienteId() == null) {
            throw new ResourceBadRequestException("Informe o paciente.");
        }
        Paciente p = pacienteRepository.findById(dto.getPacienteId()).orElseThrow(() -> naoEncontrado("o paciente", dto.getPacienteId()));
        boolean aberto = deslocadoRepository.findAbertosDoPaciente(p.getUuid()).stream().anyMatch(d -> d.getEmergencia().getUuid().equals(em.getUuid()));
        if (aberto) {
            throw new ResourceConflictException(p.getNome() + " já está deslocado nesta emergência; use a realocação para mudar o destino.");
        }
        PacienteDeslocado d = new PacienteDeslocado();
        d.setEmergencia(em);
        d.setPaciente(p);
        LocalDeslocamento origem = dto.getOrigemTipo() != null ? dto.getOrigemTipo() : LocalDeslocamento.DOMICILIO;
        d.setOrigemTipo(origem);
        if (origem == LocalDeslocamento.UNIDADE_SAUDE) {
            if (dto.getOrigemUnidadeId() == null) {
                throw new ResourceBadRequestException("Informe a unidade de onde o paciente saiu.");
            }
            d.setOrigemUnidade(unidade(dto.getOrigemUnidadeId()));
        }
        d.setOrigemDescricao(texto(dto.getOrigemDescricao()) != null ? dto.getOrigemDescricao().trim()
                : origem == LocalDeslocamento.DOMICILIO ? endereco(p.getEndereco()) : null);
        Instant saida = dto.getSaidaEm() != null ? dto.getSaidaEm() : Instant.now();
        if (saida.isAfter(Instant.now().plusSeconds(60))) {
            throw new ResourceBadRequestException("A saída não pode ser no futuro.");
        }
        d.setSaidaEm(saida);
        preencherDestino(d, dto);
        d.setNecessidades(texto(dto.getNecessidades()));
        d.setResponsavel(texto(dto.getResponsavel()));
        d.setSolicitacaoTransporte(transporte(p, dto.getSolicitacaoTransporteId()));
        d.setSituacao(SituacaoDeslocamento.DESLOCADO);
        d.setRegistradoEm(Instant.now());
        d.setRegistradoPorCpf(UsuarioAutenticado.cpf());
        deslocadoRepository.save(d);
        ContextoAuditoria.registro(em.getUuid());
        ContextoAuditoria.detalhe("Paciente deslocado para " + d.destinoNome() + ".");
        return lista(em);
    }

    /** Muda o destino: só o deslocado (422) na emergência ativa (422); este fica realocado e nasce o novo, ligado a ele. */
    @Transactional
    public List<PessoasEmergenciaDtos.Deslocado> realocar(UUID emergenciaId, UUID deslocadoId, DeslocadoRequestDto dto) {
        exigirAcesso();
        Emergencia em = ativa(emergenciaId);
        PacienteDeslocado atual = deslocado(em, deslocadoId);
        exigirAberto(atual);
        Instant agora = Instant.now();
        PacienteDeslocado novo = new PacienteDeslocado();
        novo.setEmergencia(em);
        novo.setPaciente(atual.getPaciente());
        novo.setOrigemTipo(atual.getDestinoTipo());
        novo.setOrigemUnidade(atual.getDestinoUnidade());
        novo.setOrigemDescricao(atual.destinoNome());
        novo.setSaidaEm(agora);
        preencherDestino(novo, dto);
        novo.setNecessidades(texto(dto.getNecessidades()) != null ? dto.getNecessidades().trim() : atual.getNecessidades());
        novo.setResponsavel(texto(dto.getResponsavel()) != null ? dto.getResponsavel().trim() : atual.getResponsavel());
        novo.setSolicitacaoTransporte(transporte(atual.getPaciente(), dto.getSolicitacaoTransporteId()));
        novo.setAnterior(atual);
        novo.setSituacao(SituacaoDeslocamento.DESLOCADO);
        novo.setRegistradoEm(agora);
        novo.setRegistradoPorCpf(UsuarioAutenticado.cpf());
        atual.setSituacao(SituacaoDeslocamento.REALOCADO);
        atual.setFimEm(agora);
        atual.setMotivoFim("Realocado para " + novo.destinoNome() + ".");
        deslocadoRepository.save(atual);
        deslocadoRepository.save(novo);
        ContextoAuditoria.registro(em.getUuid());
        ContextoAuditoria.detalhe("Paciente realocado para " + novo.destinoNome() + ".");
        return lista(em);
    }

    /** O retorno para casa: só o deslocado (422), com a emergência ainda aberta (encerrada, 422); a data entre a saída e agora (400). */
    @Transactional
    public List<PessoasEmergenciaDtos.Deslocado> retornar(UUID emergenciaId, UUID deslocadoId, DeslocadoRequestDto.Fim dto) {
        return fechar(emergenciaId, deslocadoId, dto, SituacaoDeslocamento.RETORNOU);
    }

    /** Encerra sem retorno (óbito, mudança definitiva, outro): o motivo é obrigatório (400). */
    @Transactional
    public List<PessoasEmergenciaDtos.Deslocado> encerrar(UUID emergenciaId, UUID deslocadoId, DeslocadoRequestDto.Fim dto) {
        if (texto(dto.getMotivo()) == null) {
            throw new ResourceBadRequestException("Informe o motivo do encerramento.");
        }
        return fechar(emergenciaId, deslocadoId, dto, SituacaoDeslocamento.ENCERRADO);
    }

    /** Liga a solicitação de transporte (T2) do mesmo paciente (outro, 422). */
    @Transactional
    public List<PessoasEmergenciaDtos.Deslocado> ligarTransporte(UUID emergenciaId, UUID deslocadoId, DeslocadoRequestDto.Transporte dto) {
        exigirAcesso();
        Emergencia em = aberta(emergenciaId);
        PacienteDeslocado d = deslocado(em, deslocadoId);
        d.setSolicitacaoTransporte(transporte(d.getPaciente(), dto.getSolicitacaoId()));
        deslocadoRepository.save(d);
        ContextoAuditoria.registro(em.getUuid());
        return lista(em);
    }

    // ── Encaminhamento intersetorial (ADR-0154) ───────────────────────────────────────────────────

    @Transactional
    public List<PessoasEmergenciaDtos.Deslocado> encaminhar(UUID emergenciaId, UUID deslocadoId, DeslocadoRequestDto.Encaminhamento dto) {
        exigirAcesso();
        Emergencia em = aberta(emergenciaId);
        PacienteDeslocado d = deslocado(em, deslocadoId);
        EncaminhamentoIntersetorialRequestDto e = new EncaminhamentoIntersetorialRequestDto(dto.getInstituicaoId(), dto.getMotivo(), dto.getEnviadoEm(),
                dto.getRetornoEsperadoEm());
        String cpf = UsuarioAutenticado.cpf();
        encaminhamentoService.encaminhar(ORIGEM, d.getUuid(), d.getPaciente(), unidade(dto.getUnidadeId()),
                cpf == null ? null : profissionalRepository.findByCpfAndAtivoTrue(cpf).orElse(null), e);
        ContextoAuditoria.registro(em.getUuid());
        return lista(em);
    }

    @Transactional
    public List<PessoasEmergenciaDtos.Deslocado> receberEncaminhamento(UUID emergenciaId, UUID deslocadoId, UUID encaminhamentoId,
                                                                      RetornoEncaminhamentoRequestDto dto) {
        exigirAcesso();
        Emergencia em = aberta(emergenciaId);
        PacienteDeslocado d = deslocado(em, deslocadoId);
        encaminhamentoService.receber(encaminhamentoService.daOrigem(encaminhamentoId, ORIGEM, d.getUuid()), dto);
        return lista(em);
    }

    @Transactional
    public List<PessoasEmergenciaDtos.Deslocado> retornoEncaminhamento(UUID emergenciaId, UUID deslocadoId, UUID encaminhamentoId,
                                                                      RetornoEncaminhamentoRequestDto dto) {
        exigirAcesso();
        Emergencia em = aberta(emergenciaId);
        PacienteDeslocado d = deslocado(em, deslocadoId);
        encaminhamentoService.registrarRetorno(encaminhamentoService.daOrigem(encaminhamentoId, ORIGEM, d.getUuid()), dto);
        return lista(em);
    }

    @Transactional
    public List<PessoasEmergenciaDtos.Deslocado> cancelarEncaminhamento(UUID emergenciaId, UUID deslocadoId, UUID encaminhamentoId,
                                                                       DeslocadoRequestDto.Fim dto) {
        exigirAcesso();
        if (texto(dto.getMotivo()) == null) {
            throw new ResourceBadRequestException("Informe o motivo do cancelamento.");
        }
        Emergencia em = aberta(emergenciaId);
        PacienteDeslocado d = deslocado(em, deslocadoId);
        encaminhamentoService.cancelar(encaminhamentoService.daOrigem(encaminhamentoId, ORIGEM, d.getUuid()), dto.getMotivo());
        return lista(em);
    }

    // ── Para o prontuário ─────────────────────────────────────────────────────────────────────────

    /** O paciente fora de casa por alguma emergência, para o aviso no prontuário. */
    @Transactional(readOnly = true)
    public List<PessoasEmergenciaDtos.DeslocamentoAberto> deslocamentosDoPaciente(UUID pacienteId) {
        controleDeAcesso.exigirAlguma(PACIENTE, "PRONTUARIO.CONSULTAR");
        return deslocadoRepository.findAbertosDoPaciente(pacienteId).stream()
                .map(d -> new PessoasEmergenciaDtos.DeslocamentoAberto(d.getUuid(), d.getEmergencia().getUuid(), d.getEmergencia().getNome(),
                        d.getDestinoTipo(), d.destinoNome(), d.getSaidaEm())).toList();
    }

    // ── Para a emergência, a desmobilização e o relatório ─────────────────────────────────────────

    /** A ocupação dos destinos agora (sem nomes de pacientes), as maiores primeiro. */
    List<PessoasEmergenciaDtos.Abrigo> abrigos(Emergencia em) {
        Map<String, PessoasEmergenciaDtos.Abrigo> mapa = new LinkedHashMap<>();
        for (PacienteDeslocado d : deslocadoRepository.findDaEmergencia(em.getUuid())) {
            if (d.getSituacao() != SituacaoDeslocamento.DESLOCADO) {
                continue;
            }
            String chave = d.getDestinoTipo() + "|" + d.destinoNome();
            PessoasEmergenciaDtos.Abrigo a = mapa.get(chave);
            mapa.put(chave, new PessoasEmergenciaDtos.Abrigo(d.getDestinoTipo(), d.destinoNome(), a == null ? 1 : a.pessoas() + 1));
        }
        return mapa.values().stream().sorted(Comparator.comparing(PessoasEmergenciaDtos.Abrigo::pessoas).reversed()).toList();
    }

    /** {pacientes deslocados, retornaram, ainda fora, destinos de abrigo distintos, encaminhamentos}. */
    int[] totais(Emergencia em) {
        List<PacienteDeslocado> todos = deslocadoRepository.findDaEmergencia(em.getUuid());
        Map<UUID, List<PacienteDeslocado>> porPaciente = todos.stream().collect(Collectors.groupingBy(d -> d.getPaciente().getUuid()));
        int retornaram = (int) porPaciente.values().stream().filter(l -> l.stream().anyMatch(d -> d.getSituacao() == SituacaoDeslocamento.RETORNOU)).count();
        int fora = (int) porPaciente.values().stream().filter(l -> l.stream().anyMatch(d -> d.getSituacao() == SituacaoDeslocamento.DESLOCADO)).count();
        int abrigos = (int) todos.stream().filter(d -> d.getDestinoTipo() == LocalDeslocamento.ABRIGO).map(PacienteDeslocado::destinoNome).distinct().count();
        int encaminhamentos = todos.isEmpty() ? 0 : todos.stream().mapToInt(d -> encaminhamentoService.listarDaOrigem(ORIGEM, d.getUuid()).size()).sum();
        return new int[]{porPaciente.size(), retornaram, fora, abrigos, encaminhamentos};
    }

    // ── Apoio ─────────────────────────────────────────────────────────────────────────────────────

    private List<PessoasEmergenciaDtos.Deslocado> fechar(UUID emergenciaId, UUID deslocadoId, DeslocadoRequestDto.Fim dto, SituacaoDeslocamento s) {
        exigirAcesso();
        Emergencia em = aberta(emergenciaId);
        PacienteDeslocado d = deslocado(em, deslocadoId);
        exigirAberto(d);
        Instant quando = dto.getData() != null ? dto.getData() : Instant.now();
        if (quando.isBefore(d.getSaidaEm()) || quando.isAfter(Instant.now().plusSeconds(60))) {
            throw new ResourceBadRequestException("A data vai da saída até agora.");
        }
        d.setSituacao(s);
        d.setFimEm(quando);
        d.setMotivoFim(texto(dto.getMotivo()));
        deslocadoRepository.save(d);
        ContextoAuditoria.registro(em.getUuid());
        ContextoAuditoria.detalhe(s == SituacaoDeslocamento.RETORNOU ? "Paciente deslocado retornou." : "Deslocamento encerrado: " + d.getMotivoFim());
        return lista(em);
    }

    /** O destino pelo tipo: a unidade (400 sem ela), o abrigo (a instituição ativa ou o nome; 400 sem nenhum), a casa ou outro (400 sem texto). */
    private void preencherDestino(PacienteDeslocado d, DeslocadoRequestDto dto) {
        LocalDeslocamento t = dto.getDestinoTipo();
        if (t == null) {
            throw new ResourceBadRequestException("Informe o destino.");
        }
        if (t == LocalDeslocamento.DOMICILIO) {
            throw new ResourceBadRequestException("Voltar para casa é o retorno, não um destino.");
        }
        d.setDestinoTipo(t);
        String texto = texto(dto.getDestinoDescricao());
        switch (t) {
            case UNIDADE_SAUDE -> {
                if (dto.getDestinoUnidadeId() == null) {
                    throw new ResourceBadRequestException("Informe a unidade de saúde de destino.");
                }
                d.setDestinoUnidade(unidade(dto.getDestinoUnidadeId()));
            }
            case ABRIGO -> {
                if (dto.getDestinoInstituicaoId() != null) {
                    InstituicaoExterna i = instituicaoService.instituicao(dto.getDestinoInstituicaoId());
                    if (!i.isAtiva()) {
                        throw new ResourceUnprocessableEntityException("A instituição " + i.getNome() + " está inativa no catálogo.");
                    }
                    d.setDestinoInstituicao(i);
                } else if (texto == null) {
                    throw new ResourceBadRequestException("Informe o abrigo: a instituição da rede intersetorial ou o nome do local.");
                }
            }
            default -> {
                if (texto == null) {
                    throw new ResourceBadRequestException(t == LocalDeslocamento.CASA_PARENTE ? "Informe o endereço da casa do parente." : "Descreva o destino.");
                }
            }
        }
        d.setDestinoDescricao(texto);
    }

    private SolicitacaoTransporte transporte(Paciente p, UUID solicitacaoId) {
        if (solicitacaoId == null) {
            return null;
        }
        SolicitacaoTransporte s = solicitacaoRepository.findById(solicitacaoId).orElseThrow(() -> naoEncontrado("a solicitação de transporte", solicitacaoId));
        if (!s.getPaciente().getUuid().equals(p.getUuid())) {
            throw new ResourceUnprocessableEntityException("A solicitação de transporte é de outro paciente.");
        }
        return s;
    }

    private List<PessoasEmergenciaDtos.Deslocado> lista(Emergencia em) {
        List<PacienteDeslocado> todos = deslocadoRepository.findDaEmergencia(em.getUuid());
        LocalDate hoje = LocalDate.now();
        return todos.stream()
                .sorted(Comparator.comparing((PacienteDeslocado d) -> d.getSituacao() != SituacaoDeslocamento.DESLOCADO)
                        .thenComparing(d -> d.getPaciente().getNome(), String.CASE_INSENSITIVE_ORDER))
                .map(d -> new PessoasEmergenciaDtos.Deslocado(d.getUuid(), d.getPaciente().getUuid(), d.getPaciente().getNome(), d.getOrigemTipo(),
                        d.getOrigemUnidade() != null ? d.getOrigemUnidade().getNome() : d.getOrigemDescricao(), d.getDestinoTipo(),
                        d.getDestinoUnidade() != null ? d.getDestinoUnidade().getUuid() : null,
                        d.getDestinoInstituicao() != null ? d.getDestinoInstituicao().getUuid() : null, d.destinoNome(), d.getSaidaEm(),
                        d.getNecessidades(), d.getResponsavel(), d.getSituacao(), d.getFimEm(), d.getMotivoFim(),
                        d.getAnterior() != null ? d.getAnterior().getUuid() : null,
                        d.getSolicitacaoTransporte() != null ? d.getSolicitacaoTransporte().getUuid() : null,
                        d.getSolicitacaoTransporte() != null ? d.getSolicitacaoTransporte().getSituacao().name() : null,
                        encaminhamentoService.listarDaOrigem(ORIGEM, d.getUuid())))
                .toList();
    }

    private void exigirAcesso() {
        controleDeAcesso.exigirAlguma(COORDENAR);
        controleDeAcesso.exigirAlguma(PACIENTE);
    }

    private static void exigirAberto(PacienteDeslocado d) {
        if (d.getSituacao() != SituacaoDeslocamento.DESLOCADO) {
            throw new ResourceUnprocessableEntityException("Este deslocamento já está " + d.getSituacao().name().toLowerCase() + ".");
        }
    }

    private Emergencia emergencia(UUID id) {
        return emergenciaRepository.findById(id).orElseThrow(() -> naoEncontrado("a emergência", id));
    }

    private Emergencia ativa(UUID id) {
        Emergencia em = emergencia(id);
        if (em.getSituacao() != SituacaoEmergencia.ATIVA) {
            throw new ResourceUnprocessableEntityException("A emergência não está ativa: o deslocamento novo entra só durante a resposta.");
        }
        return em;
    }

    /** Ativa ou em desmobilização: o retorno e o encaminhamento ainda se registram (encerrada, 422). */
    private Emergencia aberta(UUID id) {
        Emergencia em = emergencia(id);
        if (em.getSituacao() != SituacaoEmergencia.ATIVA && em.getSituacao() != SituacaoEmergencia.EM_DESMOBILIZACAO) {
            throw new ResourceUnprocessableEntityException("A emergência " + (em.getSituacao() == SituacaoEmergencia.ENCERRADA ? "já foi encerrada." : "não está ativa."));
        }
        return em;
    }

    private PacienteDeslocado deslocado(Emergencia em, UUID id) {
        return deslocadoRepository.findById(id).filter(d -> d.getEmergencia().getUuid().equals(em.getUuid()))
                .orElseThrow(() -> new ResourceNotFoundException("Não foi possível encontrar o deslocamento " + id + " nesta emergência."));
    }

    private UnidadeDeSaude unidade(UUID id) {
        return unidadeRepository.findById(id).orElseThrow(() -> naoEncontrado("a unidade de saúde", id));
    }

    static String endereco(Endereco e) {
        if (e == null) {
            return null;
        }
        String s = Stream.of(e.getLogradouro() == null ? null : e.getLogradouro() + (e.getNumeroLogradouro() != null ? ", " + e.getNumeroLogradouro() : ""),
                        e.getBairro(), e.getCidade()).filter(x -> x != null && !x.isBlank()).collect(Collectors.joining(" · "));
        return s.isBlank() ? null : s;
    }

    private static String celula(Object v) {
        return v == null ? "" : String.valueOf(v).replace(';', ',').replace('\n', ' ');
    }

    private static String texto(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }

    private static ResourceNotFoundException naoEncontrado(String o, UUID id) {
        return new ResourceNotFoundException("Não foi possível encontrar " + o + " " + id + " em nossos registros.");
    }
}
