package com.edufelizardo.maissaudepublica.services.version1;

import com.edufelizardo.maissaudepublica.config.ContextoAuditoria;
import com.edufelizardo.maissaudepublica.config.UsuarioAutenticado;
import com.edufelizardo.maissaudepublica.exceptions.ResourceBadRequestException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceConflictException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceForbiddenException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceNotFoundException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceUnprocessableEntityException;
import com.edufelizardo.maissaudepublica.models.AcidenteTrabalho;
import com.edufelizardo.maissaudepublica.models.EstabelecimentoRegulado;
import com.edufelizardo.maissaudepublica.models.EventoTrabalho;
import com.edufelizardo.maissaudepublica.models.Notificacao;
import com.edufelizardo.maissaudepublica.models.Paciente;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.AcompanhamentoSorologicoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.EventoTrabalhoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.SaudeTrabalhadorDtos;
import com.edufelizardo.maissaudepublica.models.enuns.EtapaAcompanhamentoSorologico;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoNotificacao;
import com.edufelizardo.maissaudepublica.repositories.AcidenteTrabalhoRepository;
import com.edufelizardo.maissaudepublica.repositories.EstabelecimentoReguladoRepository;
import com.edufelizardo.maissaudepublica.repositories.EventoTrabalhoRepository;
import com.edufelizardo.maissaudepublica.repositories.NotificacaoRepository;
import com.edufelizardo.maissaudepublica.repositories.PacienteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Saúde do Trabalhador, fatia V5a da Vigilância (ADR-0207, desenho na ADR-0110): o bloco do trabalho na notificação, o
 * acompanhamento sorológico do acidente com material biológico e a ligação ao acidente do servidor no RH. A vigilância
 * notifica e investiga; a CAT e o afastamento continuam com o RH.
 */
@Service
public class SaudeTrabalhadorService {

    public static final String ACIDENTE_GRAVE = "ACIDENTE_TRABALHO_GRAVE";
    public static final String MATERIAL_BIOLOGICO = "ACIDENTE_TRABALHO_MATERIAL_BIOLOGICO";
    static final Set<String> ACIDENTES = Set.of(ACIDENTE_GRAVE, MATERIAL_BIOLOGICO);
    /** Antecedência com que a etapa do acompanhamento entra nas pendências. */
    static final int AVISO_ACOMPANHAMENTO_DIAS = 7;

    @Autowired
    private EventoTrabalhoRepository eventoRepository;

    @Autowired
    private NotificacaoRepository notificacaoRepository;

    @Autowired
    private EstabelecimentoReguladoRepository estabelecimentoRepository;

    @Autowired
    private AcidenteTrabalhoRepository acidenteRepository;

    @Autowired
    private PacienteRepository pacienteRepository;

    @Autowired
    private ControleDeAcesso controleDeAcesso;

    @Autowired
    private AutorizacaoService autorizacaoService;

    // ── Na notificação ─────────────────────────────────────────────────────────────────────────────

    /**
     * Confere o bloco do trabalho antes de gravar a notificação: obrigatório no agravo relacionado ao trabalho, opcional nos
     * demais (a intoxicação por agrotóxico na lavoura, por exemplo). Devolve o evento ainda sem a notificação.
     */
    EventoTrabalho preparar(Notificacao n, EventoTrabalhoRequestDto dto) {
        if (dto == null) {
            if (n.getAgravo().isRelacionadoAoTrabalho()) {
                throw new ResourceBadRequestException("A notificação de " + n.getAgravo().getNome()
                        + " pede o bloco do trabalho: a ocupação e a situação no mercado de trabalho.");
            }
            return null;
        }
        EventoTrabalho e = new EventoTrabalho();
        preencher(e, n, dto);
        return e;
    }

    void gravar(EventoTrabalho e, Notificacao n) {
        if (e == null) {
            return;
        }
        e.setNotificacao(n);
        e.setRegistradoEm(Instant.now());
        e.setRegistradoPorCpf(UsuarioAutenticado.cpf());
        eventoRepository.save(e);
    }

    /** O bloco do trabalho para o detalhe da notificação; nulo sem bloco. */
    SaudeTrabalhadorDtos.Trabalho dto(Notificacao n) {
        return eventoRepository.findByNotificacao_Uuid(n.getUuid()).map(e -> dto(e, n, LocalDate.now())).orElse(null);
    }

    // ── Pela investigação ──────────────────────────────────────────────────────────────────────────

    /** Completa ou corrige o bloco do trabalho (o notificante nem sempre sabe o empregador); só quem investiga, antes do encerramento. */
    @Transactional
    public void atualizar(UUID notificacaoId, EventoTrabalhoRequestDto dto) {
        Notificacao n = buscar(notificacaoId);
        controleDeAcesso.exigir(VigilanciaService.INVESTIGAR, n.getUnidade());
        auditar(n);
        if (n.getSituacao() == SituacaoNotificacao.ENCERRADA) {
            throw new ResourceUnprocessableEntityException("A notificação foi encerrada e não muda mais.");
        }
        EventoTrabalho e = eventoRepository.findByNotificacao_Uuid(n.getUuid()).orElse(null);
        boolean novo = e == null;
        if (novo) {
            e = new EventoTrabalho();
            e.setNotificacao(n);
            e.setRegistradoEm(Instant.now());
            e.setRegistradoPorCpf(UsuarioAutenticado.cpf());
        }
        preencher(e, n, dto);
        e.setAtualizadoEm(Instant.now());
        e.setAtualizadoPorCpf(UsuarioAutenticado.cpf());
        eventoRepository.save(e);
        ContextoAuditoria.detalhe(novo ? "Bloco do trabalho incluído na investigação." : "Bloco do trabalho atualizado.");
    }

    /** Registra uma etapa do acompanhamento sorológico do acidente com material biológico; quem notifica ou investiga na unidade. */
    @Transactional
    public void registrarAcompanhamento(UUID notificacaoId, AcompanhamentoSorologicoRequestDto dto) {
        Notificacao n = buscar(notificacaoId);
        controleDeAcesso.exigirVisivel(n.getUnidade(), VigilanciaService.NOTIFICAR, VigilanciaService.INVESTIGAR);
        auditar(n);
        if (!MATERIAL_BIOLOGICO.equals(n.getAgravo().getCodigo())) {
            throw new ResourceUnprocessableEntityException("O acompanhamento sorológico é do acidente com material biológico.");
        }
        if (n.getSituacao() == SituacaoNotificacao.ENCERRADA) {
            throw new ResourceUnprocessableEntityException("A notificação foi encerrada e não muda mais.");
        }
        EventoTrabalho e = eventoRepository.findByNotificacao_Uuid(n.getUuid()).orElseThrow(() -> new ResourceUnprocessableEntityException(
                "A notificação não tem o bloco do trabalho."));
        LocalDate acidente = n.getDataPrimeirosSintomas();
        if (dto.getRealizadoEm().isAfter(LocalDate.now())) {
            throw new ResourceBadRequestException("O acompanhamento não pode ser no futuro.");
        }
        if (acidente != null && dto.getRealizadoEm().isBefore(acidente)) {
            throw new ResourceBadRequestException("O acompanhamento não pode ser antes do acidente.");
        }
        switch (dto.getEtapa()) {
            case SEIS_SEMANAS -> e.setAcompanhamentoSeisSemanas(dto.getRealizadoEm());
            case TRES_MESES -> e.setAcompanhamentoTresMeses(dto.getRealizadoEm());
            case SEIS_MESES -> e.setAcompanhamentoSeisMeses(dto.getRealizadoEm());
        }
        e.setAtualizadoEm(Instant.now());
        e.setAtualizadoPorCpf(UsuarioAutenticado.cpf());
        eventoRepository.save(e);
        ContextoAuditoria.detalhe("Acompanhamento sorológico: " + dto.getEtapa() + " em " + dto.getRealizadoEm() + ".");
    }

    // ── Painel ─────────────────────────────────────────────────────────────────────────────────────

    /** As notificações relacionadas ao trabalho do escopo e as etapas do acompanhamento sorológico a fazer. */
    @Transactional(readOnly = true)
    public SaudeTrabalhadorDtos.Painel painel(UUID unidadeId) {
        controleDeAcesso.exigirAlguma(VigilanciaService.NOTIFICAR, VigilanciaService.INVESTIGAR);
        LocalDate hoje = LocalDate.now();
        List<EventoTrabalho> eventos = controleDeAcesso.filtrar(eventoRepository.findTodos().stream(),
                        e -> java.util.stream.Stream.of(e.getNotificacao().getUnidade()), VigilanciaService.NOTIFICAR, VigilanciaService.INVESTIGAR)
                .filter(e -> podeVerConteudo(e.getNotificacao()))
                .filter(e -> unidadeId == null || e.getNotificacao().getUnidade().getUuid().equals(unidadeId))
                .toList();
        List<SaudeTrabalhadorDtos.NotificacaoTrabalho> lista = eventos.stream().map(e -> {
            Notificacao n = e.getNotificacao();
            return new SaudeTrabalhadorDtos.NotificacaoTrabalho(n.getUuid(), n.getPaciente().getUuid(), n.getPaciente().getNome(),
                    n.getAgravo().getCodigo(), n.getAgravo().getNome(), n.getUnidade().getNome(), n.getDataNotificacao(), n.getDataPrimeirosSintomas(),
                    n.getSituacao(), e.getOcupacaoCbo() + " · " + e.getOcupacaoDescricao(), e.getSituacaoTrabalho(), e.getEmpregadorNome(),
                    e.getEmpregadorCnae(), e.isObito(), e.getAcidenteTrabalho() != null);
        }).toList();
        List<SaudeTrabalhadorDtos.AcompanhamentoPendente> pendentes = new ArrayList<>();
        for (EventoTrabalho e : eventos) {
            Notificacao n = e.getNotificacao();
            if (!MATERIAL_BIOLOGICO.equals(n.getAgravo().getCodigo()) || n.getSituacao() == SituacaoNotificacao.ENCERRADA) {
                continue;
            }
            for (SaudeTrabalhadorDtos.Acompanhamento a : acompanhamentos(e, n, hoje)) {
                if (a.realizadoEm() == null && a.prevista() != null && !a.prevista().isAfter(hoje.plusDays(AVISO_ACOMPANHAMENTO_DIAS))) {
                    pendentes.add(new SaudeTrabalhadorDtos.AcompanhamentoPendente(n.getUuid(), n.getPaciente().getNome(), n.getUnidade().getNome(),
                            a.etapa(), a.prevista(), a.vencida()));
                }
            }
        }
        pendentes.sort(Comparator.comparing(SaudeTrabalhadorDtos.AcompanhamentoPendente::prevista));
        long acidentes = eventos.stream().filter(e -> ACIDENTES.contains(e.getNotificacao().getAgravo().getCodigo())).count();
        long obitos = eventos.stream().filter(EventoTrabalho::isObito).count();
        long biologico = eventos.stream().filter(e -> MATERIAL_BIOLOGICO.equals(e.getNotificacao().getAgravo().getCodigo())).count();
        return new SaudeTrabalhadorDtos.Painel(eventos.size(), acidentes, obitos, biologico, lista, pendentes);
    }

    /**
     * Os acidentes de servidores registrados no RH sem notificação na vigilância. O servidor não tem unidade no RH, então a
     * lista é da vigilância com acesso à rede inteira (como o CEREST municipal).
     */
    @Transactional(readOnly = true)
    public List<SaudeTrabalhadorDtos.AcidenteSemNotificacao> acidentesSemNotificacao() {
        controleDeAcesso.exigirAlguma(VigilanciaService.INVESTIGAR);
        if (controleDeAcesso.unidadesVisiveis(VigilanciaService.INVESTIGAR).isPresent()) {
            throw new ResourceForbiddenException("Os acidentes dos servidores são da rede inteira: só a vigilância com acesso a todas as unidades vê a lista.");
        }
        Set<UUID> notificados = eventoRepository.findAcidentesNotificados();
        return acidenteRepository.findAll().stream()
                .filter(a -> !notificados.contains(a.getUuid()))
                .sorted(Comparator.comparing(AcidenteTrabalho::getDataHora, Comparator.nullsLast(Comparator.reverseOrder())))
                .map(a -> new SaudeTrabalhadorDtos.AcidenteSemNotificacao(a.getUuid(), a.getProfissional().getMatricula(), a.getProfissional().getNome(),
                        a.getDataHora(), a.getDescricao(), a.isCatEmitida(), a.getDiasAfastamento(), pacienteDoCpf(a.getProfissional().getCpf())))
                .toList();
    }

    /** Para o RH: se o acidente do servidor já foi notificado à vigilância (sem o conteúdo, que é dado de saúde). */
    @Transactional(readOnly = true)
    public boolean notificado(UUID acidenteId) {
        return eventoRepository.existsByAcidenteTrabalho_Uuid(acidenteId);
    }

    // ── Regras ─────────────────────────────────────────────────────────────────────────────────────

    private void preencher(EventoTrabalho e, Notificacao n, EventoTrabalhoRequestDto dto) {
        String cbo = digitos(dto.getOcupacaoCbo());
        if (cbo.length() != 6) {
            throw new ResourceBadRequestException("A ocupação usa o código da CBO, de seis dígitos (ex.: 2235-05).");
        }
        e.setOcupacaoCbo(cbo);
        e.setOcupacaoDescricao(dto.getOcupacaoDescricao().trim());
        e.setSituacaoTrabalho(dto.getSituacaoTrabalho());
        String nome = texto(dto.getEmpregadorNome());
        if (texto(dto.getEmpregadorDocumento()) != null) {
            String documento = VigilanciaSanitariaService.documento(dto.getEmpregadorDocumento());
            EstabelecimentoRegulado est = estabelecimentoRepository.findByDocumento(documento).orElse(null);
            if (nome == null && est != null) {
                nome = est.getRazaoSocial();
            }
            if (nome == null) {
                throw new ResourceBadRequestException("Informe o nome do empregador.");
            }
            e.setEmpregadorDocumento(documento);
            e.setEstabelecimento(est);
        } else {
            e.setEmpregadorDocumento(null);
            e.setEstabelecimento(null);
        }
        e.setEmpregadorNome(nome);
        String cnae = texto(dto.getEmpregadorCnae()) == null ? null : digitos(dto.getEmpregadorCnae());
        if (cnae != null && cnae.length() != 7) {
            throw new ResourceBadRequestException("O CNAE tem sete dígitos (ex.: 8610-1/01).");
        }
        e.setEmpregadorCnae(cnae);
        e.setLocalEvento(texto(dto.getLocalEvento()));
        if ((dto.getLatitude() == null) != (dto.getLongitude() == null)) {
            throw new ResourceBadRequestException("Informe a latitude e a longitude do local do evento, ou nenhuma das duas.");
        }
        e.setLatitude(dto.getLatitude());
        e.setLongitude(dto.getLongitude());
        String codigo = n.getAgravo().getCodigo();
        boolean acidente = ACIDENTES.contains(codigo);
        boolean dadosDoAcidente = dto.getTipoAcidente() != null || dto.getHorasAposInicioJornada() != null || dto.getCat() != null
                || dto.getOutrosTrabalhadoresAtingidos() != null || Boolean.TRUE.equals(dto.getObito()) || dto.getAcidenteTrabalhoId() != null;
        if (!acidente && dadosDoAcidente) {
            throw new ResourceBadRequestException("O tipo, a jornada, a CAT, os atingidos, o óbito e o acidente do RH são só do acidente de trabalho.");
        }
        if (acidente && n.getDataPrimeirosSintomas() == null) {
            throw new ResourceBadRequestException("Informe a data do acidente (no campo dos primeiros sintomas).");
        }
        if (ACIDENTE_GRAVE.equals(codigo) && dto.getTipoAcidente() == null) {
            throw new ResourceBadRequestException("Informe o tipo do acidente: típico ou de trajeto.");
        }
        e.setTipoAcidente(dto.getTipoAcidente());
        e.setHorasAposInicioJornada(dto.getHorasAposInicioJornada());
        e.setCat(dto.getCat());
        e.setOutrosTrabalhadoresAtingidos(dto.getOutrosTrabalhadoresAtingidos());
        e.setObito(Boolean.TRUE.equals(dto.getObito()));
        e.setAcidenteTrabalho(dto.getAcidenteTrabalhoId() == null ? null : acidenteDoServidor(dto.getAcidenteTrabalhoId(), n.getPaciente(), e));
    }

    /** O acidente do RH: existe (404), não está em outra notificação (409) e é do mesmo CPF do paciente (422). */
    private AcidenteTrabalho acidenteDoServidor(UUID id, Paciente paciente, EventoTrabalho e) {
        AcidenteTrabalho a = acidenteRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar o acidente de trabalho " + id + " em nossos registros."));
        eventoRepository.findByAcidenteTrabalho_Uuid(id).filter(outro -> e.getUuid() == null || !outro.getUuid().equals(e.getUuid()))
                .ifPresent(outro -> {
                    throw new ResourceConflictException("O acidente do RH já está ligado a outra notificação.");
                });
        String cpfServidor = digitos(a.getProfissional().getCpf());
        if (cpfServidor.isEmpty() || !cpfServidor.equals(digitos(paciente.getCpf()))) {
            throw new ResourceUnprocessableEntityException("O acidente do RH é de outro trabalhador: o CPF do servidor não é o do paciente.");
        }
        return a;
    }

    private SaudeTrabalhadorDtos.Trabalho dto(EventoTrabalho e, Notificacao n, LocalDate hoje) {
        String codigo = n.getAgravo().getCodigo();
        boolean biologico = MATERIAL_BIOLOGICO.equals(codigo);
        AcidenteTrabalho a = e.getAcidenteTrabalho();
        return new SaudeTrabalhadorDtos.Trabalho(e.getUuid(), e.getOcupacaoCbo(), e.getOcupacaoDescricao(), e.getSituacaoTrabalho(),
                e.getEmpregadorDocumento(), e.getEmpregadorNome(), e.getEmpregadorCnae(),
                e.getEstabelecimento() != null ? e.getEstabelecimento().getUuid() : null, e.getLocalEvento(), e.getLatitude(), e.getLongitude(),
                e.getTipoAcidente(), e.getHorasAposInicioJornada(), e.getCat(), e.getOutrosTrabalhadoresAtingidos(), e.isObito(),
                a != null ? a.getUuid() : null, a != null ? a.getDataHora() : null, ACIDENTES.contains(codigo), biologico,
                biologico ? acompanhamentos(e, n, hoje) : List.of(), e.getAtualizadoEm());
    }

    /** As três etapas, previstas a partir da data do acidente (6 semanas, 3 e 6 meses). */
    static List<SaudeTrabalhadorDtos.Acompanhamento> acompanhamentos(EventoTrabalho e, Notificacao n, LocalDate hoje) {
        LocalDate base = n.getDataPrimeirosSintomas();
        boolean aberta = n.getSituacao() != SituacaoNotificacao.ENCERRADA;
        List<SaudeTrabalhadorDtos.Acompanhamento> lista = new ArrayList<>();
        for (EtapaAcompanhamentoSorologico etapa : EtapaAcompanhamentoSorologico.values()) {
            LocalDate prevista = base == null ? null : switch (etapa) {
                case SEIS_SEMANAS -> base.plusWeeks(6);
                case TRES_MESES -> base.plusMonths(3);
                case SEIS_MESES -> base.plusMonths(6);
            };
            LocalDate realizado = switch (etapa) {
                case SEIS_SEMANAS -> e.getAcompanhamentoSeisSemanas();
                case TRES_MESES -> e.getAcompanhamentoTresMeses();
                case SEIS_MESES -> e.getAcompanhamentoSeisMeses();
            };
            lista.add(new SaudeTrabalhadorDtos.Acompanhamento(etapa, prevista, realizado,
                    aberta && realizado == null && prevista != null && prevista.isBefore(hoje)));
        }
        return lista;
    }

    private UUID pacienteDoCpf(String cpf) {
        String d = digitos(cpf);
        if (d.isEmpty()) {
            return null;
        }
        // O CPF do paciente pode estar só com os dígitos ou com a pontuação.
        List<Paciente> achados = new ArrayList<>(pacienteRepository.findByCpf(d));
        if (d.length() == 11) {
            achados.addAll(pacienteRepository.findByCpf(d.substring(0, 3) + "." + d.substring(3, 6) + "." + d.substring(6, 9) + "-" + d.substring(9)));
        }
        List<UUID> ids = achados.stream().filter(Paciente::isAtivo).map(Paciente::getUuid).distinct().toList();
        return ids.size() == 1 ? ids.get(0) : null;
    }

    private boolean podeVerConteudo(Notificacao n) {
        if (!n.getAgravo().isSensivel() || !controleDeAcesso.ativo()) {
            return true;
        }
        String cpf = UsuarioAutenticado.cpf();
        return cpf != null && (cpf.equals(n.getRegistradoPorCpf()) || autorizacaoService.tem(cpf, VigilanciaService.INVESTIGAR, n.getUnidade().getUuid()));
    }

    private Notificacao buscar(UUID id) {
        return notificacaoRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar uma notificação com o id " + id + " em nossos registros."));
    }

    private static void auditar(Notificacao n) {
        ContextoAuditoria.paciente(n.getPaciente().getUuid());
        ContextoAuditoria.registro(n.getUuid());
    }

    private static String digitos(String s) {
        return s == null ? "" : s.replaceAll("\\D", "");
    }

    private static String texto(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }
}
