package com.edufelizardo.maissaudepublica.services.version1;

import com.edufelizardo.maissaudepublica.config.ContextoAuditoria;
import com.edufelizardo.maissaudepublica.config.UsuarioAutenticado;
import com.edufelizardo.maissaudepublica.exceptions.ResourceBadRequestException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceNotFoundException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceUnprocessableEntityException;
import com.edufelizardo.maissaudepublica.models.Atendimento;
import com.edufelizardo.maissaudepublica.models.EtapaTratamentoProtetico;
import com.edufelizardo.maissaudepublica.models.Profissional;
import com.edufelizardo.maissaudepublica.models.TratamentoProtetico;
import com.edufelizardo.maissaudepublica.models.UnidadeDeSaude;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.EtapaProteticaRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.TratamentoProteticoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.TratamentoProteticoDto;
import com.edufelizardo.maissaudepublica.models.enuns.ArcadaDentaria;
import com.edufelizardo.maissaudepublica.models.enuns.EtapaProtetica;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoPlanoTratamento;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoTratamentoProtetico;
import com.edufelizardo.maissaudepublica.models.enuns.TipoProtese;
import com.edufelizardo.maissaudepublica.repositories.AtendimentoRepository;
import com.edufelizardo.maissaudepublica.repositories.EtapaTratamentoProteticoRepository;
import com.edufelizardo.maissaudepublica.repositories.PacienteRepository;
import com.edufelizardo.maissaudepublica.repositories.PlanoTratamentoRepository;
import com.edufelizardo.maissaudepublica.repositories.ProfissionalRepository;
import com.edufelizardo.maissaudepublica.repositories.TratamentoProteticoRepository;
import com.edufelizardo.maissaudepublica.repositories.UnidadeCapacidadeAssistencialRepository;
import com.edufelizardo.maissaudepublica.repositories.UnidadeDeSaudeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Tratamento protético, fatia SB4b (ADR-0139, desenho na ADR-0111): o ciclo da prótese em etapas registradas, o
 * laboratório de prótese (unidade com a capacidade LRPD ou contratado), a entrega (que grava a prótese unitária no
 * odontograma), a conclusão e o cancelamento. É do cirurgião-dentista ({@code ODONTOLOGIA.PLANEJAR}).
 */
@Service
public class TratamentoProteticoService {

    static final String CAPACIDADE_LRPD = "LRPD";

    @Autowired
    private TratamentoProteticoRepository repository;

    @Autowired
    private EtapaTratamentoProteticoRepository etapaRepository;

    @Autowired
    private AtendimentoRepository atendimentoRepository;

    @Autowired
    private ProfissionalRepository profissionalRepository;

    @Autowired
    private UnidadeDeSaudeRepository unidadeRepository;

    @Autowired
    private UnidadeCapacidadeAssistencialRepository unidadeCapacidadeRepository;

    @Autowired
    private PlanoTratamentoRepository planoRepository;

    @Autowired
    private PacienteRepository pacienteRepository;

    @Autowired
    private OdontogramaService odontogramaService;

    @Autowired
    private ControleDeAcesso controleDeAcesso;

    @Autowired
    private VinculoAssistencialService vinculoService;

    /**
     * Abre no atendimento de odontologia (422 fora dele). A total e a parcial removível pedem a arcada (400) e não levam
     * dente (422); a unitária pede o dente FDI (400), e a arcada vem dele. O laboratório é opcional na abertura: uma
     * unidade com a capacidade LRPD (422) ou o contratado, não os dois (400). Liga-se ao plano aberto do paciente.
     */
    @Transactional
    public TratamentoProteticoDto abrir(TratamentoProteticoRequestDto dto) {
        Atendimento atendimento = atendimentoRepository.findById(dto.getAtendimentoId()).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar um atendimento com o id " + dto.getAtendimentoId() + " em nossos registros."));
        controleDeAcesso.exigir(PlanoTratamentoService.PLANEJAR, atendimento.getUnidade());
        ContextoAuditoria.paciente(atendimento.getPaciente().getUuid());
        if (atendimento.getEspecialidade() == null || !atendimento.getEspecialidade().isOdontologica()) {
            throw new ResourceUnprocessableEntityException("O tratamento protético é aberto num atendimento de especialidade odontológica.");
        }
        ArcadaDentaria arcada = arcada(dto.getTipo(), dto.getArcada(), dto.getDente());
        Profissional profissional = profissional(dto.getProfissionalMatricula());

        TratamentoProtetico t = new TratamentoProtetico();
        t.setPaciente(atendimento.getPaciente());
        t.setUnidade(atendimento.getUnidade());
        t.setAtendimento(atendimento);
        t.setPlano(planoRepository.findByPaciente_UuidOrderByAbertoEmDesc(atendimento.getPaciente().getUuid()).stream()
                .filter(p -> p.getSituacao() == SituacaoPlanoTratamento.ABERTO).findFirst().orElse(null));
        t.setProfissional(profissional);
        t.setTipo(dto.getTipo());
        t.setArcada(arcada);
        t.setDente(dto.getTipo() == TipoProtese.UNITARIA ? dto.getDente() : null);
        definirLaboratorio(t, dto.getLaboratorioUnidadeId(), dto.getLaboratorioExterno());
        t.setEtapaAtual(EtapaProtetica.PLANEJAMENTO);
        t.setSituacao(SituacaoTratamentoProtetico.EM_ANDAMENTO);
        t.setObservacao(texto(dto.getObservacao()));
        t.setAbertoEm(Instant.now());
        t.setAbertoPorCpf(UsuarioAutenticado.cpf());
        t = repository.save(t);
        registrar(t, EtapaProtetica.PLANEJAMENTO, LocalDate.now(), profissional, null);
        ContextoAuditoria.registro(t.getUuid());
        return dto(t);
    }

    /**
     * Registra a etapa, na ordem do ciclo (422): prova e laboratório depois da moldagem, e podem se alternar; o
     * laboratório pede o laboratório definido; a entrega pede ter passado pelo laboratório e grava a data (a unitária
     * vira "Prótese" no odontograma); depois da entrega, só o acompanhamento. A data não fica no futuro (400).
     */
    @Transactional
    public TratamentoProteticoDto registrarEtapa(UUID id, EtapaProteticaRequestDto dto) {
        TratamentoProtetico t = emAndamento(id);
        LocalDate data = dto.getData() != null ? dto.getData() : LocalDate.now();
        if (data.isAfter(LocalDate.now())) {
            throw new ResourceBadRequestException("A data da etapa não pode ser no futuro.");
        }
        if (dto.getEtapa() == EtapaProtetica.LABORATORIO && (dto.getLaboratorioUnidadeId() != null || texto(dto.getLaboratorioExterno()) != null)) {
            definirLaboratorio(t, dto.getLaboratorioUnidadeId(), dto.getLaboratorioExterno());
        }
        Set<EtapaProtetica> feitas = feitas(t.getUuid());
        if (!permitidas(t, feitas).contains(dto.getEtapa())) {
            throw new ResourceUnprocessableEntityException(motivoRecusa(t, dto.getEtapa(), feitas));
        }
        Profissional profissional = profissional(dto.getProfissionalMatricula());
        registrar(t, dto.getEtapa(), data, profissional, texto(dto.getObservacao()));
        t.setEtapaAtual(dto.getEtapa());
        if (dto.getEtapa() == EtapaProtetica.ENTREGA) {
            t.setDataEntrega(data);
            if (t.getTipo() == TipoProtese.UNITARIA) {
                odontogramaService.gravarDaProtese(t, data);
            }
        }
        repository.save(t);
        ContextoAuditoria.detalhe("Etapa " + dto.getEtapa() + " em " + data);
        return dto(t);
    }

    /** Concluído depois da entrega (422). */
    @Transactional
    public TratamentoProteticoDto concluir(UUID id) {
        TratamentoProtetico t = emAndamento(id);
        if (t.getDataEntrega() == null) {
            throw new ResourceUnprocessableEntityException("A prótese ainda não foi entregue: registre a entrega antes de concluir.");
        }
        encerrar(t, SituacaoTratamentoProtetico.CONCLUIDO, null);
        return dto(t);
    }

    /** Cancelado com motivo (400). */
    @Transactional
    public TratamentoProteticoDto cancelar(UUID id, String motivo) {
        TratamentoProtetico t = emAndamento(id);
        if (texto(motivo) == null) {
            throw new ResourceBadRequestException("Informe o motivo do cancelamento.");
        }
        encerrar(t, SituacaoTratamentoProtetico.CANCELADO, texto(motivo));
        return dto(t);
    }

    /** Os tratamentos do paciente, o mais recente primeiro, com as regras do prontuário (vínculo e leitura auditada). */
    @Transactional(readOnly = true)
    public List<TratamentoProteticoDto> doPaciente(UUID pacienteId) {
        if (!pacienteRepository.existsById(pacienteId)) {
            throw new ResourceNotFoundException("Não foi possível encontrar um paciente com o id " + pacienteId + " em nossos registros.");
        }
        ContextoAuditoria.paciente(pacienteId);
        vinculoService.exigirVinculo(pacienteId);
        List<TratamentoProtetico> lista = repository.findByPaciente_UuidOrderByAbertoEmDesc(pacienteId);
        Map<UUID, List<EtapaTratamentoProtetico>> etapas = etapasDos(lista.stream().map(TratamentoProtetico::getUuid).toList());
        return lista.stream().map(t -> dto(t, etapas.getOrDefault(t.getUuid(), List.of()))).toList();
    }

    // ── Regras ─────────────────────────────────────────────────────────────────────────────────────

    /** As etapas que ainda podem ser registradas, na ordem do ciclo. */
    static Set<EtapaProtetica> permitidas(TratamentoProtetico t, Set<EtapaProtetica> feitas) {
        Set<EtapaProtetica> ok = EnumSet.noneOf(EtapaProtetica.class);
        if (t.getSituacao() != SituacaoTratamentoProtetico.EM_ANDAMENTO) {
            return ok;
        }
        if (feitas.contains(EtapaProtetica.ENTREGA)) {
            ok.add(EtapaProtetica.ACOMPANHAMENTO);
            return ok;
        }
        ok.add(EtapaProtetica.MOLDAGEM);
        if (feitas.contains(EtapaProtetica.MOLDAGEM)) {
            ok.add(EtapaProtetica.PROVA);
            if (t.getLaboratorioUnidade() != null || t.getLaboratorioExterno() != null) {
                ok.add(EtapaProtetica.LABORATORIO);
            }
        }
        if (feitas.contains(EtapaProtetica.LABORATORIO)) {
            ok.add(EtapaProtetica.ENTREGA);
        }
        return ok;
    }

    private static String motivoRecusa(TratamentoProtetico t, EtapaProtetica etapa, Set<EtapaProtetica> feitas) {
        if (feitas.contains(EtapaProtetica.ENTREGA)) {
            return "A prótese já foi entregue: depois da entrega, só o acompanhamento.";
        }
        return switch (etapa) {
            case PLANEJAMENTO -> "O planejamento é registrado na abertura do tratamento.";
            case PROVA -> "A prova vem depois da moldagem.";
            case LABORATORIO -> !feitas.contains(EtapaProtetica.MOLDAGEM) ? "O envio ao laboratório vem depois da moldagem."
                    : "Informe o laboratório: a unidade com a capacidade LRPD ou o laboratório contratado.";
            case ENTREGA -> "A entrega vem depois do laboratório.";
            case ACOMPANHAMENTO -> "O acompanhamento vem depois da entrega.";
            default -> "A etapa " + etapa + " não pode ser registrada agora.";
        };
    }

    private static ArcadaDentaria arcada(TipoProtese tipo, ArcadaDentaria arcada, Integer dente) {
        if (tipo == TipoProtese.UNITARIA) {
            if (dente == null) {
                throw new ResourceBadRequestException("Informe o dente (FDI) da prótese unitária.");
            }
            OdontogramaService.exigirDente(dente);
            int quadrante = dente / 10;
            return quadrante == 1 || quadrante == 2 || quadrante == 5 || quadrante == 6 ? ArcadaDentaria.SUPERIOR : ArcadaDentaria.INFERIOR;
        }
        if (dente != null) {
            throw new ResourceUnprocessableEntityException("A prótese " + tipo + " é da arcada, e não de um dente.");
        }
        if (arcada == null) {
            throw new ResourceBadRequestException("Informe a arcada da prótese: SUPERIOR ou INFERIOR.");
        }
        return arcada;
    }

    /** A unidade com a capacidade LRPD vigente (422) ou o contratado; os dois juntos, 400. */
    private void definirLaboratorio(TratamentoProtetico t, UUID unidadeId, String externo) {
        String nome = texto(externo);
        if (unidadeId != null && nome != null) {
            throw new ResourceBadRequestException("Informe o laboratório da rede ou o contratado, não os dois.");
        }
        if (unidadeId != null) {
            UnidadeDeSaude u = unidadeRepository.findById(unidadeId).orElseThrow(() -> new ResourceNotFoundException(
                    "Não foi possível encontrar a unidade " + unidadeId + " em nossos registros."));
            if (!unidadeCapacidadeRepository.existsByUnidade_UuidAndCapacidade_CodigoAndFimIsNull(u.getUuid(), CAPACIDADE_LRPD)) {
                throw new ResourceUnprocessableEntityException("A unidade " + u.getNome() + " não tem a capacidade de laboratório regional de "
                        + "prótese dentária (LRPD) no Modelo Operacional.");
            }
            t.setLaboratorioUnidade(u);
            t.setLaboratorioExterno(null);
        } else if (nome != null) {
            t.setLaboratorioUnidade(null);
            t.setLaboratorioExterno(nome);
        }
    }

    private TratamentoProtetico emAndamento(UUID id) {
        TratamentoProtetico t = repository.findById(id).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar o tratamento protético " + id + " em nossos registros."));
        controleDeAcesso.exigir(PlanoTratamentoService.PLANEJAR, t.getUnidade());
        ContextoAuditoria.paciente(t.getPaciente().getUuid());
        ContextoAuditoria.registro(t.getUuid());
        if (t.getSituacao() != SituacaoTratamentoProtetico.EM_ANDAMENTO) {
            throw new ResourceUnprocessableEntityException("O tratamento protético está " + t.getSituacao() + ".");
        }
        return t;
    }

    private Set<EtapaProtetica> feitas(UUID tratamentoId) {
        return etapaRepository.findDosTratamentos(List.of(tratamentoId)).stream().map(EtapaTratamentoProtetico::getEtapa)
                .collect(Collectors.toCollection(() -> EnumSet.noneOf(EtapaProtetica.class)));
    }

    private void registrar(TratamentoProtetico t, EtapaProtetica etapa, LocalDate data, Profissional profissional, String observacao) {
        EtapaTratamentoProtetico e = new EtapaTratamentoProtetico();
        e.setTratamento(t);
        e.setEtapa(etapa);
        e.setData(data);
        e.setProfissional(profissional);
        e.setObservacao(observacao);
        e.setRegistradoEm(Instant.now());
        e.setRegistradoPorCpf(UsuarioAutenticado.cpf());
        etapaRepository.save(e);
    }

    private void encerrar(TratamentoProtetico t, SituacaoTratamentoProtetico situacao, String motivo) {
        t.setSituacao(situacao);
        t.setEncerradoEm(Instant.now());
        t.setEncerradoPorCpf(UsuarioAutenticado.cpf());
        t.setMotivoCancelamento(motivo);
        repository.save(t);
        ContextoAuditoria.detalhe("Tratamento protético " + situacao + (motivo != null ? ": " + motivo : ""));
    }

    private Profissional profissional(String matricula) {
        return profissionalRepository.findByMatricula(matricula).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar um profissional com a matrícula " + matricula + " em nossos registros."));
    }

    private Map<UUID, List<EtapaTratamentoProtetico>> etapasDos(Collection<UUID> ids) {
        if (ids.isEmpty()) {
            return Map.of();
        }
        return etapaRepository.findDosTratamentos(ids).stream().collect(Collectors.groupingBy(e -> e.getTratamento().getUuid()));
    }

    private TratamentoProteticoDto dto(TratamentoProtetico t) {
        return dto(t, etapasDos(List.of(t.getUuid())).getOrDefault(t.getUuid(), List.of()));
    }

    private static TratamentoProteticoDto dto(TratamentoProtetico t, List<EtapaTratamentoProtetico> etapas) {
        Set<EtapaProtetica> feitas = etapas.stream().map(EtapaTratamentoProtetico::getEtapa)
                .collect(Collectors.toCollection(() -> EnumSet.noneOf(EtapaProtetica.class)));
        List<EtapaProtetica> proximas = Arrays.stream(EtapaProtetica.values()).filter(permitidas(t, feitas)::contains).toList();
        UnidadeDeSaude lab = t.getLaboratorioUnidade();
        return new TratamentoProteticoDto(t.getUuid(), t.getPaciente().getUuid(), t.getAtendimento().getUuid(),
                t.getPlano() != null ? t.getPlano().getUuid() : null, t.getUnidade().getNome(), t.getProfissional().getMatricula(),
                t.getProfissional().getNome(), t.getTipo(), t.getArcada(), t.getDente(), lab != null ? lab.getUuid() : null,
                lab != null ? lab.getNome() : t.getLaboratorioExterno(), lab == null && t.getLaboratorioExterno() != null, t.getEtapaAtual(),
                t.getSituacao(), t.getDataEntrega(), t.getObservacao(), t.getAbertoEm(), t.getEncerradoEm(), t.getMotivoCancelamento(), proximas,
                etapas.stream().map(e -> new TratamentoProteticoDto.Etapa(e.getUuid(), e.getEtapa(), e.getData(), e.getProfissional().getNome(),
                        e.getObservacao(), e.getRegistradoEm())).toList());
    }

    private static String texto(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }
}
