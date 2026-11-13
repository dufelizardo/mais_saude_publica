package com.edufelizardo.maissaudepublica.services.version1;

import com.edufelizardo.maissaudepublica.config.ContextoAuditoria;
import com.edufelizardo.maissaudepublica.config.UsuarioAutenticado;
import com.edufelizardo.maissaudepublica.exceptions.ResourceBadRequestException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceConflictException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceNotFoundException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceUnprocessableEntityException;
import com.edufelizardo.maissaudepublica.models.DenunciaSanitaria;
import com.edufelizardo.maissaudepublica.models.EstabelecimentoRegulado;
import com.edufelizardo.maissaudepublica.models.EventoTrabalho;
import com.edufelizardo.maissaudepublica.models.InvestigacaoTrabalho;
import com.edufelizardo.maissaudepublica.models.MedidaTrabalho;
import com.edufelizardo.maissaudepublica.models.Notificacao;
import com.edufelizardo.maissaudepublica.models.RiscoOcupacional;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.ComunicacaoVisaRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.ControleRiscoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.EncaminhamentoIntersetorialRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.EncaminhamentoTrabalhoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.InvestigacaoTrabalhoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.MedidaTrabalhoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.NexoTrabalhoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.RiscoOcupacionalRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.VerificacaoMedidaTrabalhoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.SaudeTrabalhadorDtos;
import com.edufelizardo.maissaudepublica.models.enuns.CanalDenuncia;
import com.edufelizardo.maissaudepublica.models.enuns.DominioOrigemEncaminhamento;
import com.edufelizardo.maissaudepublica.models.enuns.NexoTrabalho;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoDenuncia;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoMedidaTrabalho;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoNotificacao;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoRiscoOcupacional;
import com.edufelizardo.maissaudepublica.repositories.DenunciaSanitariaRepository;
import com.edufelizardo.maissaudepublica.repositories.EncaminhamentoIntersetorialRepository;
import com.edufelizardo.maissaudepublica.repositories.EstabelecimentoReguladoRepository;
import com.edufelizardo.maissaudepublica.repositories.EventoTrabalhoRepository;
import com.edufelizardo.maissaudepublica.repositories.InvestigacaoTrabalhoRepository;
import com.edufelizardo.maissaudepublica.repositories.MedidaTrabalhoRepository;
import com.edufelizardo.maissaudepublica.repositories.NotificacaoRepository;
import com.edufelizardo.maissaudepublica.repositories.ProfissionalRepository;
import com.edufelizardo.maissaudepublica.repositories.RiscoOcupacionalRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

/**
 * Saúde do Trabalhador, fatia V5b da Vigilância (ADR-0208): a investigação do ambiente de trabalho (visita, achados e nexo), as medidas
 * com prazo e verificação, a comunicação à vigilância sanitária, o encaminhamento intersetorial e o risco ocupacional por empregador.
 */
@Service
public class InvestigacaoTrabalhoService {

    static final int RETORNO_PADRAO_DIAS = 30;
    static final String DENUNCIANTE = "Vigilância em saúde do trabalhador";
    private static final DateTimeFormatter BR = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @Autowired private InvestigacaoTrabalhoRepository investigacaoRepository;
    @Autowired private MedidaTrabalhoRepository medidaRepository;
    @Autowired private RiscoOcupacionalRepository riscoRepository;
    @Autowired private EventoTrabalhoRepository eventoRepository;
    @Autowired private NotificacaoRepository notificacaoRepository;
    @Autowired private EstabelecimentoReguladoRepository estabelecimentoRepository;
    @Autowired private DenunciaSanitariaRepository denunciaRepository;
    @Autowired private EncaminhamentoIntersetorialRepository encaminhamentoRepository;
    @Autowired private EncaminhamentoIntersetorialService encaminhamentoService;
    @Autowired private ProfissionalRepository profissionalRepository;
    @Autowired private ControleDeAcesso controleDeAcesso;

    // ── Investigação ───────────────────────────────────────────────────────────────────────────────

    /** Registra ou atualiza a visita e os achados; a notificação passa a em investigação. */
    @Transactional
    public void investigar(UUID notificacaoId, InvestigacaoTrabalhoRequestDto dto) {
        Notificacao n = paraInvestigar(notificacaoId);
        if (eventoRepository.findByNotificacao_Uuid(n.getUuid()).isEmpty()) {
            throw new ResourceUnprocessableEntityException("A notificação não tem o bloco do trabalho: inclua-o antes de investigar o ambiente.");
        }
        if (dto.getVisitaEm() != null) {
            if (dto.getVisitaEm().isAfter(LocalDate.now())) {
                throw new ResourceBadRequestException("A visita não pode ser no futuro.");
            }
            if (n.getDataPrimeirosSintomas() != null && dto.getVisitaEm().isBefore(n.getDataPrimeirosSintomas())) {
                throw new ResourceBadRequestException("A visita não pode ser antes do evento.");
            }
        }
        InvestigacaoTrabalho i = investigacaoRepository.findByNotificacao_Uuid(n.getUuid()).orElse(null);
        boolean nova = i == null;
        if (nova) {
            i = new InvestigacaoTrabalho();
            i.setNotificacao(n);
            i.setNexo(NexoTrabalho.EM_ANALISE);
            i.setIniciadaEm(Instant.now());
            i.setRegistradoPorCpf(UsuarioAutenticado.cpf());
            String cpf = UsuarioAutenticado.cpf();
            i.setInvestigador(cpf == null ? null : profissionalRepository.findByCpfAndAtivoTrue(cpf).orElse(null));
        } else {
            i.setAtualizadoEm(Instant.now());
            i.setAtualizadoPorCpf(UsuarioAutenticado.cpf());
        }
        i.setVisitaEm(dto.getVisitaEm());
        i.setLocalVisitado(texto(dto.getLocalVisitado()));
        i.setAchados(texto(dto.getAchados()));
        investigacaoRepository.save(i);
        if (n.getSituacao() == SituacaoNotificacao.NOTIFICADA) {
            n.setSituacao(SituacaoNotificacao.EM_INVESTIGACAO);
            notificacaoRepository.save(n);
        }
        ContextoAuditoria.detalhe(nova ? "Investigação do trabalho iniciada." : "Investigação do trabalho atualizada.");
    }

    /** Conclui (ou revê) o nexo com a justificativa; só com a investigação iniciada (422). */
    @Transactional
    public void concluirNexo(UUID notificacaoId, NexoTrabalhoRequestDto dto) {
        InvestigacaoTrabalho i = investigacao(paraInvestigar(notificacaoId));
        if (dto.getNexo() == NexoTrabalho.EM_ANALISE) {
            throw new ResourceBadRequestException("Conclua o nexo como confirmado, descartado ou inconclusivo.");
        }
        i.setNexo(dto.getNexo());
        i.setJustificativaNexo(dto.getJustificativa().trim());
        i.setNexoConcluidoEm(Instant.now());
        i.setNexoConcluidoPorCpf(UsuarioAutenticado.cpf());
        investigacaoRepository.save(i);
        ContextoAuditoria.detalhe("Nexo com o trabalho: " + dto.getNexo() + ".");
    }

    /** Recomenda uma medida com prazo (não no passado, 400). */
    @Transactional
    public void recomendarMedida(UUID notificacaoId, MedidaTrabalhoRequestDto dto) {
        InvestigacaoTrabalho i = investigacao(paraInvestigar(notificacaoId));
        if (dto.getPrazo().isBefore(LocalDate.now())) {
            throw new ResourceBadRequestException("O prazo da medida não pode ser no passado.");
        }
        MedidaTrabalho m = new MedidaTrabalho();
        m.setInvestigacao(i);
        m.setTipo(dto.getTipo());
        m.setDescricao(dto.getDescricao().trim());
        m.setPrazo(dto.getPrazo());
        m.setSituacao(SituacaoMedidaTrabalho.RECOMENDADA);
        m.setCriadaEm(Instant.now());
        m.setRegistradoPorCpf(UsuarioAutenticado.cpf());
        medidaRepository.save(m);
        ContextoAuditoria.detalhe("Medida do trabalho recomendada: " + dto.getTipo() + ", prazo " + dto.getPrazo() + ".");
    }

    /**
     * Verifica a medida: cumprida ou não cumprida (a não cumprida pede a observação, 400). Vale depois do encerramento da notificação,
     * porque o prazo da medida pode passar dele. Já verificada, 422. Devolve a notificação.
     */
    @Transactional
    public UUID verificarMedida(UUID medidaId, VerificacaoMedidaTrabalhoRequestDto dto) {
        MedidaTrabalho m = medidaRepository.findById(medidaId).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar a medida " + medidaId + " em nossos registros."));
        Notificacao n = m.getInvestigacao().getNotificacao();
        controleDeAcesso.exigir(VigilanciaService.INVESTIGAR, n.getUnidade());
        auditar(n);
        if (m.getSituacao() != SituacaoMedidaTrabalho.RECOMENDADA) {
            throw new ResourceUnprocessableEntityException("A medida já foi verificada.");
        }
        if (dto.getSituacao() == SituacaoMedidaTrabalho.RECOMENDADA) {
            throw new ResourceBadRequestException("A verificação diz se a medida foi cumprida ou não.");
        }
        String obs = texto(dto.getObservacao());
        if (dto.getSituacao() == SituacaoMedidaTrabalho.NAO_CUMPRIDA && obs == null) {
            throw new ResourceBadRequestException("Diga o que não foi cumprido.");
        }
        LocalDate em = dto.getVerificadaEm() != null ? dto.getVerificadaEm() : LocalDate.now();
        if (em.isAfter(LocalDate.now())) {
            throw new ResourceBadRequestException("A verificação não pode ser no futuro.");
        }
        m.setSituacao(dto.getSituacao());
        m.setVerificadaEm(em);
        m.setObservacaoVerificacao(obs);
        m.setVerificadaPorCpf(UsuarioAutenticado.cpf());
        medidaRepository.save(m);
        ContextoAuditoria.detalhe("Medida do trabalho verificada: " + dto.getSituacao() + ".");
        return n.getUuid();
    }

    /**
     * Comunica a vigilância sanitária: abre a denúncia de canal VIGILANCIA contra o empregador, sem os dados do trabalhador (só o agravo e a
     * data). Uma por investigação (409); sem empregador no bloco, 422.
     */
    @Transactional
    public void comunicarVisa(UUID notificacaoId, ComunicacaoVisaRequestDto dto) {
        Notificacao n = paraInvestigar(notificacaoId);
        InvestigacaoTrabalho i = investigacao(n);
        if (i.getDenuncia() != null) {
            throw new ResourceConflictException("A investigação já comunicou a vigilância sanitária (denúncia " + i.getDenuncia().getProtocolo() + ").");
        }
        EventoTrabalho e = eventoRepository.findByNotificacao_Uuid(n.getUuid()).orElseThrow();
        if (e.getEstabelecimento() == null && e.getEmpregadorNome() == null) {
            throw new ResourceUnprocessableEntityException("O bloco do trabalho não tem o empregador: complete-o antes de comunicar a vigilância sanitária.");
        }
        DenunciaSanitaria d = new DenunciaSanitaria();
        d.setProtocolo(VigilanciaSanitariaService.numero(LocalDate.now().getYear(), denunciaRepository::countByProtocoloStartingWith));
        d.setCanal(CanalDenuncia.VIGILANCIA);
        String evento = n.getDataPrimeirosSintomas() != null ? n.getDataPrimeirosSintomas().format(BR) : n.getDataNotificacao().format(BR);
        d.setDescricao("Investigação de " + n.getAgravo().getNome() + " (evento em " + evento + "): " + dto.getDescricao().trim());
        d.setEstabelecimento(e.getEstabelecimento());
        d.setEstabelecimentoDescricao(e.getEstabelecimento() != null ? null
                : e.getEmpregadorNome() + (e.getEmpregadorDocumento() != null ? " (" + e.getEmpregadorDocumento() + ")" : ""));
        d.setAnonima(false);
        d.setDenuncianteNome(DENUNCIANTE);
        d.setSituacao(SituacaoDenuncia.RECEBIDA);
        d.setRecebidaEm(Instant.now());
        d.setRegistradoPorCpf(UsuarioAutenticado.cpf());
        denunciaRepository.save(d);
        i.setDenuncia(d);
        investigacaoRepository.save(i);
        ContextoAuditoria.detalhe("Vigilância sanitária comunicada: denúncia " + d.getProtocolo() + ".");
    }

    /** Encaminha à rede intersetorial (Superintendência do Trabalho, MPT, sindicato), com a investigação como origem. */
    @Transactional
    public void encaminhar(UUID notificacaoId, EncaminhamentoTrabalhoRequestDto dto) {
        Notificacao n = paraInvestigar(notificacaoId);
        InvestigacaoTrabalho i = investigacao(n);
        EncaminhamentoIntersetorialRequestDto e = new EncaminhamentoIntersetorialRequestDto();
        e.setInstituicaoId(dto.getInstituicaoId());
        e.setEnviadoEm(LocalDate.now());
        e.setRetornoEsperadoEm(dto.getRetornoEsperadoEm() != null ? dto.getRetornoEsperadoEm() : LocalDate.now().plusDays(RETORNO_PADRAO_DIAS));
        e.setMotivo(dto.getMotivo());
        encaminhamentoService.encaminhar(DominioOrigemEncaminhamento.VIGILANCIA, i.getUuid(), n.getPaciente(), n.getUnidade(), i.getInvestigador(), e);
        ContextoAuditoria.detalhe("Encaminhamento intersetorial pela investigação do trabalho.");
    }

    // ── Risco ocupacional ──────────────────────────────────────────────────────────────────────────

    /**
     * Registra o risco do empregador. Pela investigação (com a notificação), exige investigar na unidade e o empregador vem do bloco se não
     * informado; sem ela, é o cadastro da vigilância do trabalhador.
     */
    @Transactional
    public SaudeTrabalhadorDtos.Risco registrarRisco(RiscoOcupacionalRequestDto dto) {
        InvestigacaoTrabalho i = null;
        EventoTrabalho evento = null;
        if (dto.getNotificacaoId() != null) {
            Notificacao n = paraInvestigar(dto.getNotificacaoId());
            i = investigacao(n);
            evento = eventoRepository.findByNotificacao_Uuid(n.getUuid()).orElseThrow();
        } else {
            controleDeAcesso.exigirAlguma(VigilanciaService.INVESTIGAR);
        }
        RiscoOcupacional r = new RiscoOcupacional();
        r.setTipo(dto.getTipo());
        r.setAgente(dto.getAgente().trim());
        r.setDescricao(texto(dto.getDescricao()));
        String nome = texto(dto.getEmpregadorNome());
        if (texto(dto.getEmpregadorDocumento()) != null) {
            String documento = VigilanciaSanitariaService.documento(dto.getEmpregadorDocumento());
            EstabelecimentoRegulado est = estabelecimentoRepository.findByDocumento(documento).orElse(null);
            r.setEmpregadorDocumento(documento);
            r.setEstabelecimento(est);
            if (nome == null && est != null) {
                nome = est.getRazaoSocial();
            }
        } else if (evento != null && nome == null) {
            r.setEmpregadorDocumento(evento.getEmpregadorDocumento());
            r.setEstabelecimento(evento.getEstabelecimento());
            nome = evento.getEmpregadorNome();
        }
        if (nome == null) {
            throw new ResourceBadRequestException("Informe o empregador.");
        }
        r.setEmpregadorNome(nome);
        r.setLocal(texto(dto.getLocal()));
        r.setTrabalhadoresExpostos(dto.getTrabalhadoresExpostos());
        r.setSituacao(SituacaoRiscoOcupacional.ATIVO);
        r.setInvestigacao(i);
        r.setRegistradoEm(Instant.now());
        r.setRegistradoPorCpf(UsuarioAutenticado.cpf());
        riscoRepository.save(r);
        ContextoAuditoria.registro(r.getUuid());
        ContextoAuditoria.detalhe("Risco ocupacional " + dto.getTipo() + ": " + r.getAgente() + ".");
        return risco(r);
    }

    /** Marca o risco como controlado, com o motivo; já controlado, 422. */
    @Transactional
    public SaudeTrabalhadorDtos.Risco controlarRisco(UUID riscoId, ControleRiscoRequestDto dto) {
        controleDeAcesso.exigirAlguma(VigilanciaService.INVESTIGAR);
        RiscoOcupacional r = riscoRepository.findById(riscoId).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar o risco ocupacional " + riscoId + " em nossos registros."));
        if (r.getSituacao() == SituacaoRiscoOcupacional.CONTROLADO) {
            throw new ResourceUnprocessableEntityException("O risco já está controlado.");
        }
        r.setSituacao(SituacaoRiscoOcupacional.CONTROLADO);
        r.setControladoEm(Instant.now());
        r.setMotivoControle(dto.getMotivo().trim());
        riscoRepository.save(r);
        ContextoAuditoria.registro(r.getUuid());
        ContextoAuditoria.detalhe("Risco ocupacional controlado.");
        return risco(r);
    }

    /** Os riscos ocupacionais, ativos primeiro; filtra pela situação. */
    @Transactional(readOnly = true)
    public List<SaudeTrabalhadorDtos.Risco> riscos(SituacaoRiscoOcupacional situacao) {
        controleDeAcesso.exigirAlguma(VigilanciaService.INVESTIGAR);
        return riscoRepository.findTodos().stream().filter(r -> situacao == null || r.getSituacao() == situacao)
                .sorted(java.util.Comparator.comparing((RiscoOcupacional r) -> r.getSituacao() != SituacaoRiscoOcupacional.ATIVO))
                .map(this::risco).toList();
    }

    // ── Leitura ────────────────────────────────────────────────────────────────────────────────────

    /** A investigação para o detalhe da notificação; nula sem investigação. */
    SaudeTrabalhadorDtos.Investigacao dto(Notificacao n, LocalDate hoje) {
        return investigacaoRepository.findByNotificacao_Uuid(n.getUuid()).map(i -> new SaudeTrabalhadorDtos.Investigacao(i.getUuid(), i.getVisitaEm(),
                i.getLocalVisitado(), i.getAchados(), i.getNexo(), i.getJustificativaNexo(), i.getNexoConcluidoEm(),
                i.getInvestigador() != null ? i.getInvestigador().getNome() : null,
                i.getDenuncia() != null ? i.getDenuncia().getProtocolo() : null, i.getDenuncia() != null ? i.getDenuncia().getSituacao() : null,
                medidaRepository.findByInvestigacao_UuidOrderByCriadaEmAsc(i.getUuid()).stream().map(m -> medida(m, hoje)).toList(),
                riscoRepository.findByInvestigacao_UuidOrderByRegistradoEmAsc(i.getUuid()).stream().map(this::risco).toList(),
                encaminhamentoRepository.findDaOrigem(DominioOrigemEncaminhamento.VIGILANCIA, i.getUuid()).stream()
                        .map(x -> new SaudeTrabalhadorDtos.Encaminhamento(x.getUuid(), x.getInstituicao().getNome(), x.getMotivo(), x.getEnviadoEm(),
                                x.getRetornoEsperadoEm(), x.getSituacao().name())).toList())).orElse(null);
    }

    static SaudeTrabalhadorDtos.Medida medida(MedidaTrabalho m, LocalDate hoje) {
        return new SaudeTrabalhadorDtos.Medida(m.getUuid(), m.getTipo(), m.getDescricao(), m.getPrazo(), m.getSituacao(), vencida(m, hoje),
                m.getVerificadaEm(), m.getObservacaoVerificacao());
    }

    static boolean vencida(MedidaTrabalho m, LocalDate hoje) {
        return m.getSituacao() == SituacaoMedidaTrabalho.RECOMENDADA && m.getPrazo().isBefore(hoje);
    }

    SaudeTrabalhadorDtos.Risco risco(RiscoOcupacional r) {
        return new SaudeTrabalhadorDtos.Risco(r.getUuid(), r.getTipo(), r.getAgente(), r.getDescricao(), r.getEmpregadorDocumento(), r.getEmpregadorNome(),
                r.getEstabelecimento() != null ? r.getEstabelecimento().getUuid() : null, r.getLocal(), r.getTrabalhadoresExpostos(), r.getSituacao(),
                r.getControladoEm(), r.getMotivoControle(), r.getInvestigacao() != null ? r.getInvestigacao().getNotificacao().getUuid() : null);
    }

    // ── Apoio ──────────────────────────────────────────────────────────────────────────────────────

    private Notificacao paraInvestigar(UUID id) {
        Notificacao n = notificacaoRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar uma notificação com o id " + id + " em nossos registros."));
        controleDeAcesso.exigir(VigilanciaService.INVESTIGAR, n.getUnidade());
        auditar(n);
        if (n.getSituacao() == SituacaoNotificacao.ENCERRADA) {
            throw new ResourceUnprocessableEntityException("A notificação foi encerrada e não muda mais.");
        }
        return n;
    }

    private InvestigacaoTrabalho investigacao(Notificacao n) {
        return investigacaoRepository.findByNotificacao_Uuid(n.getUuid()).orElseThrow(() -> new ResourceUnprocessableEntityException(
                "Inicie a investigação do trabalho (a visita) antes."));
    }

    private static void auditar(Notificacao n) {
        ContextoAuditoria.paciente(n.getPaciente().getUuid());
        ContextoAuditoria.registro(n.getUuid());
    }

    private static String texto(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }
}
