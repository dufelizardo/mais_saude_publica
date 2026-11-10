package com.edufelizardo.maissaudepublica.services.version1;

import com.edufelizardo.maissaudepublica.config.ContextoAuditoria;
import com.edufelizardo.maissaudepublica.config.UsuarioAutenticado;
import com.edufelizardo.maissaudepublica.exceptions.ResourceBadRequestException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceConflictException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceNotFoundException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceUnprocessableEntityException;
import com.edufelizardo.maissaudepublica.models.EventoLaudoAih;
import com.edufelizardo.maissaudepublica.models.FaixaAih;
import com.edufelizardo.maissaudepublica.models.Internacao;
import com.edufelizardo.maissaudepublica.models.InternacaoAgendada;
import com.edufelizardo.maissaudepublica.models.LaudoAih;
import com.edufelizardo.maissaudepublica.models.ProcedimentoCatalogo;
import com.edufelizardo.maissaudepublica.models.Profissional;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.DecisaoLaudoAihRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.FaixaAihRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.LaudoAihRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.LaudoAihDtos;
import com.edufelizardo.maissaudepublica.models.enuns.CaraterInternacao;
import com.edufelizardo.maissaudepublica.models.enuns.StatusInternacao;
import com.edufelizardo.maissaudepublica.models.enuns.StatusInternacaoAgendada;
import com.edufelizardo.maissaudepublica.models.enuns.StatusLaudoAih;
import com.edufelizardo.maissaudepublica.models.enuns.TipoEventoLaudoAih;
import com.edufelizardo.maissaudepublica.repositories.EventoLaudoAihRepository;
import com.edufelizardo.maissaudepublica.repositories.FaixaAihRepository;
import com.edufelizardo.maissaudepublica.repositories.InternacaoAgendadaRepository;
import com.edufelizardo.maissaudepublica.repositories.InternacaoRepository;
import com.edufelizardo.maissaudepublica.repositories.LaudoAihRepository;
import com.edufelizardo.maissaudepublica.repositories.ProcedimentoCatalogoRepository;
import com.edufelizardo.maissaudepublica.repositories.ProfissionalRepository;
import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

/**
 * Laudo para solicitação de AIH e autorização interna (ADR-0186). O médico solicita (da internação ou, antes da admissão, da eletiva
 * agendada); o médico autorizador, que não pode ser quem solicitou, autoriza com o próximo número da faixa vigente, devolve para
 * correção ou nega. Internação de urgência sem laudo passadas 48 horas da admissão aparece nas pendências.
 */
@Service
public class LaudoAihService {

    public static final String SOLICITAR = "AIH.SOLICITAR";
    public static final String AUTORIZAR = "AIH.AUTORIZAR";
    public static final String NUMERAR = "AIH.NUMERAR";
    static final long HORAS_PARA_O_LAUDO = 48;
    private static final Set<StatusLaudoAih> ATIVOS = Set.of(StatusLaudoAih.SOLICITADO, StatusLaudoAih.DEVOLVIDO, StatusLaudoAih.AUTORIZADO);
    private static final String[] VER = {"INTERNACAO.CONSULTAR", "INTERNACAO.GERENCIAR", "INTERNACAO.ALTA", SOLICITAR, AUTORIZAR};
    private static final DateTimeFormatter DATA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm").withZone(ZoneId.of("America/Sao_Paulo"));
    private static final DateTimeFormatter DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final Font TITULO = new Font(Font.HELVETICA, 14, Font.BOLD);
    private static final Font SECAO = new Font(Font.HELVETICA, 10, Font.BOLD);
    private static final Font NEGRITO = new Font(Font.HELVETICA, 9, Font.BOLD);
    private static final Font NORMAL = new Font(Font.HELVETICA, 9, Font.NORMAL);
    private static final Font PEQUENA = new Font(Font.HELVETICA, 7.5f, Font.NORMAL, new Color(74, 83, 97));

    @Autowired
    private LaudoAihRepository laudoRepository;

    @Autowired
    private EventoLaudoAihRepository eventoRepository;

    @Autowired
    private FaixaAihRepository faixaRepository;

    @Autowired
    private InternacaoRepository internacaoRepository;

    @Autowired
    private InternacaoAgendadaRepository agendamentoRepository;

    @Autowired
    private ProcedimentoCatalogoRepository procedimentoRepository;

    @Autowired
    private ProfissionalRepository profissionalRepository;

    @Autowired
    private ControleDeAcesso controleDeAcesso;

    // ── Laudo ──────────────────────────────────────────────────────────────────────────────────────────────────────────

    /** Solicita a AIH: de uma internação ou de uma eletiva agendada (uma das duas, 400). Um laudo em andamento por vez (409). */
    @Transactional
    public LaudoAihDtos.Laudo solicitar(LaudoAihRequestDto dto) {
        if ((dto.getInternacaoId() == null) == (dto.getAgendamentoId() == null)) {
            throw new ResourceBadRequestException("Informe a internação ou a internação eletiva agendada (uma das duas).");
        }
        LaudoAih l = new LaudoAih();
        if (dto.getInternacaoId() != null) {
            Internacao i = internacaoRepository.findById(dto.getInternacaoId()).orElseThrow(() -> new ResourceNotFoundException(
                    "Não foi possível encontrar uma internação com o id " + dto.getInternacaoId() + " em nossos registros."));
            controleDeAcesso.exigir(SOLICITAR, i.getUnidade());
            if (laudoRepository.existsByInternacao_UuidAndStatusIn(i.getUuid(), ATIVOS)) {
                throw new ResourceConflictException("A internação já tem um laudo de AIH em andamento ou autorizado.");
            }
            l.setInternacao(i);
            l.setPaciente(i.getPaciente());
            l.setUnidade(i.getUnidade());
            l.setCarater(i.getCarater());
        } else {
            InternacaoAgendada a = agendamentoRepository.findById(dto.getAgendamentoId()).orElseThrow(() -> new ResourceNotFoundException(
                    "Não foi possível encontrar uma internação eletiva com o id " + dto.getAgendamentoId() + " em nossos registros."));
            controleDeAcesso.exigir(SOLICITAR, a.getUnidade());
            if (a.getStatus() == StatusInternacaoAgendada.ADMITIDA && a.getInternacao() != null) {
                throw new ResourceUnprocessableEntityException("A eletiva já foi admitida: faça o laudo pela internação.");
            }
            if (a.getStatus() != StatusInternacaoAgendada.AGENDADA && a.getStatus() != StatusInternacaoAgendada.RESERVADA) {
                throw new ResourceUnprocessableEntityException("Não é possível pedir AIH de uma eletiva " + a.getStatus() + ".");
            }
            if (laudoRepository.existsByAgendamento_UuidAndStatusIn(a.getUuid(), ATIVOS)) {
                throw new ResourceConflictException("A eletiva já tem um laudo de AIH em andamento ou autorizado.");
            }
            l.setAgendamento(a);
            l.setPaciente(a.getPaciente());
            l.setUnidade(a.getUnidade());
            l.setCarater(CaraterInternacao.ELETIVA);
        }
        ContextoAuditoria.paciente(l.getPaciente().getUuid());
        Profissional solicitante = profissional(dto.getProfissionalMatricula());
        l.setSolicitante(solicitante);
        preencher(l, dto);
        l.setStatus(StatusLaudoAih.SOLICITADO);
        l.setSolicitadoEm(Instant.now());
        l.setRegistradoPorCpf(UsuarioAutenticado.cpf());
        l = laudoRepository.save(l);
        registrar(l, TipoEventoLaudoAih.SOLICITACAO, l.getProcedimento().getNome(), solicitante);
        return detalhe(l);
    }

    /** Corrige o laudo solicitado ou devolvido; o devolvido volta para a fila. */
    @Transactional
    public LaudoAihDtos.Laudo corrigir(UUID uuid, LaudoAihRequestDto dto) {
        LaudoAih l = travar(uuid);
        controleDeAcesso.exigir(SOLICITAR, l.getUnidade());
        if (l.getStatus() != StatusLaudoAih.SOLICITADO && l.getStatus() != StatusLaudoAih.DEVOLVIDO) {
            throw new ResourceUnprocessableEntityException("Só o laudo solicitado ou devolvido pode ser corrigido; este está " + l.getStatus() + ".");
        }
        Profissional quem = profissional(dto.getProfissionalMatricula());
        preencher(l, dto);
        l.setSolicitante(quem);
        l.setStatus(StatusLaudoAih.SOLICITADO);
        l.setSolicitadoEm(Instant.now());
        registrar(l, TipoEventoLaudoAih.CORRECAO, null, quem);
        return detalhe(laudoRepository.save(l));
    }

    @Transactional(readOnly = true)
    public LaudoAihDtos.Laudo buscarPorId(UUID uuid) {
        LaudoAih l = laudoRepository.findById(uuid).orElseThrow(() -> naoEncontrado(uuid));
        controleDeAcesso.exigirVisivel(l.getUnidade(), VER);
        return detalhe(l);
    }

    /** Os laudos de uma internação ou de uma eletiva, do mais novo para o mais velho. */
    @Transactional(readOnly = true)
    public List<LaudoAihDtos.Laudo> listar(UUID internacaoId, UUID agendamentoId) {
        if ((internacaoId == null) == (agendamentoId == null)) {
            throw new ResourceBadRequestException("Informe a internação ou a internação eletiva agendada (uma das duas).");
        }
        List<LaudoAih> laudos = internacaoId != null ? laudoRepository.findByInternacao_UuidOrderBySolicitadoEmDesc(internacaoId)
                : laudoRepository.findByAgendamento_UuidOrderBySolicitadoEmDesc(agendamentoId);
        return controleDeAcesso.filtrar(laudos.stream(), l -> Stream.of(l.getUnidade()), VER).map(this::detalhe).toList();
    }

    /** A fila do autorizador: os laudos solicitados, urgência primeiro e depois a hora da solicitação. */
    @Transactional(readOnly = true)
    public List<LaudoAihDtos.Laudo> fila() {
        controleDeAcesso.exigirAlguma(AUTORIZAR);
        return controleDeAcesso.filtrar(laudoRepository.findByStatusOrderBySolicitadoEmAsc(StatusLaudoAih.SOLICITADO).stream(),
                        l -> Stream.of(l.getUnidade()), AUTORIZAR)
                .sorted(Comparator.comparing((LaudoAih l) -> l.getCarater() != CaraterInternacao.URGENCIA).thenComparing(LaudoAih::getSolicitadoEm))
                .map(l -> LaudoAihDtos.Laudo.from(l, List.of())).toList();
    }

    /** Autoriza com o próximo número da faixa vigente do ano (422 sem faixa ou com ela esgotada). Quem solicitou não autoriza. */
    @Transactional
    public LaudoAihDtos.Laudo autorizar(UUID uuid, DecisaoLaudoAihRequestDto dto) {
        LaudoAih l = decidindo(uuid);
        Profissional autorizador = autorizador(l, dto);
        String numero = proximoNumero();
        l.setNumeroAih(numero);
        l.setStatus(StatusLaudoAih.AUTORIZADO);
        l.setAutorizador(autorizador);
        l.setDecididoEm(Instant.now());
        l.setMotivoDecisao(texto(dto.getMotivo()));
        if (l.getInternacao() != null) {
            Internacao i = l.getInternacao();
            i.setNumeroAih(numero);
            internacaoRepository.save(i);
        }
        registrar(l, TipoEventoLaudoAih.AUTORIZACAO, "AIH " + numero, autorizador);
        return detalhe(laudoRepository.save(l));
    }

    /** Devolve para correção, com motivo. */
    @Transactional
    public LaudoAihDtos.Laudo devolver(UUID uuid, DecisaoLaudoAihRequestDto dto) {
        LaudoAih l = decidindo(uuid);
        String motivo = exigirMotivo(dto);
        Profissional autorizador = autorizador(l, dto);
        l.setStatus(StatusLaudoAih.DEVOLVIDO);
        l.setAutorizador(autorizador);
        l.setDecididoEm(Instant.now());
        l.setMotivoDecisao(motivo);
        registrar(l, TipoEventoLaudoAih.DEVOLUCAO, motivo, autorizador);
        return detalhe(laudoRepository.save(l));
    }

    /** Nega, com motivo. */
    @Transactional
    public LaudoAihDtos.Laudo negar(UUID uuid, DecisaoLaudoAihRequestDto dto) {
        LaudoAih l = decidindo(uuid);
        String motivo = exigirMotivo(dto);
        Profissional autorizador = autorizador(l, dto);
        l.setStatus(StatusLaudoAih.NEGADO);
        l.setAutorizador(autorizador);
        l.setDecididoEm(Instant.now());
        l.setMotivoDecisao(motivo);
        registrar(l, TipoEventoLaudoAih.NEGACAO, motivo, autorizador);
        return detalhe(laudoRepository.save(l));
    }

    /** Quem solicitou cancela o laudo ainda não decidido (solicitado ou devolvido), com motivo. */
    @Transactional
    public LaudoAihDtos.Laudo cancelar(UUID uuid, DecisaoLaudoAihRequestDto dto) {
        LaudoAih l = travar(uuid);
        controleDeAcesso.exigir(SOLICITAR, l.getUnidade());
        if (l.getStatus() != StatusLaudoAih.SOLICITADO && l.getStatus() != StatusLaudoAih.DEVOLVIDO) {
            throw new ResourceUnprocessableEntityException("Só o laudo solicitado ou devolvido pode ser cancelado; este está " + l.getStatus() + ".");
        }
        String motivo = exigirMotivo(dto);
        Profissional quem = profissional(dto.getProfissionalMatricula());
        l.setStatus(StatusLaudoAih.CANCELADO);
        l.setMotivoDecisao(motivo);
        l.setDecididoEm(Instant.now());
        registrar(l, TipoEventoLaudoAih.CANCELAMENTO, motivo, quem);
        return detalhe(laudoRepository.save(l));
    }

    /** Na admissão da eletiva (ADR-0185): o laudo feito antes passa para a internação, com o número da AIH se já autorizado. */
    @Transactional
    public void vincularInternacao(InternacaoAgendada agendamento, Internacao internacao) {
        for (LaudoAih l : laudoRepository.findByAgendamento_UuidOrderBySolicitadoEmDesc(agendamento.getUuid())) {
            if (!ATIVOS.contains(l.getStatus()) || l.getInternacao() != null) {
                continue;
            }
            l.setInternacao(internacao);
            if (l.getStatus() == StatusLaudoAih.AUTORIZADO) {
                internacao.setNumeroAih(l.getNumeroAih());
                internacaoRepository.save(internacao);
            }
            laudoRepository.save(l);
            registrar(l, TipoEventoLaudoAih.VINCULO_INTERNACAO, "Leito " + internacao.getLeito().getIdentificacao(), null);
        }
    }

    /** Internações de urgência ainda internadas, sem laudo (fora os cancelados) passadas 48 horas da admissão. */
    @Transactional(readOnly = true)
    public List<LaudoAihDtos.Pendencia> pendencias() {
        controleDeAcesso.exigirAlguma(SOLICITAR, AUTORIZAR, InternacaoService.GERENCIAR);
        Instant agora = Instant.now();
        List<Internacao> candidatas = controleDeAcesso.filtrar(internacaoRepository.findByStatusAndCaraterAndAdmitidaEmBefore(StatusInternacao.INTERNADO,
                CaraterInternacao.URGENCIA, agora.minus(Duration.ofHours(HORAS_PARA_O_LAUDO))).stream(), i -> Stream.of(i.getUnidade()),
                SOLICITAR, AUTORIZAR, InternacaoService.GERENCIAR).toList();
        if (candidatas.isEmpty()) {
            return List.of();
        }
        Set<UUID> comLaudo = new HashSet<>(laudoRepository.findInternacoesComLaudo(candidatas.stream().map(Internacao::getUuid).toList(),
                Set.of(StatusLaudoAih.CANCELADO)));
        return candidatas.stream().filter(i -> !comLaudo.contains(i.getUuid()))
                .sorted(Comparator.comparing(Internacao::getAdmitidaEm))
                .map(i -> new LaudoAihDtos.Pendencia(i.getUuid(), i.getPaciente().getUuid(), i.getPaciente().getNome(), i.getUnidade().getUuid(),
                        i.getUnidade().getNome(), i.getLeito().getIdentificacao(), i.getAdmitidaEm(),
                        Duration.between(i.getAdmitidaEm(), agora).toHours())).toList();
    }

    // ── Faixas de numeração ────────────────────────────────────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<LaudoAihDtos.Faixa> faixas() {
        controleDeAcesso.exigirAlguma(NUMERAR, AUTORIZAR);
        return faixaRepository.findAllByOrderByAnoDescInicioAsc().stream().map(LaudoAihService::faixaDto).toList();
    }

    /** Cadastra a faixa recebida do gestor (rede inteira). Início depois do fim é 400; sobreposta a outra é 409. */
    @Transactional
    public LaudoAihDtos.Faixa cadastrarFaixa(FaixaAihRequestDto dto) {
        controleDeAcesso.exigir(NUMERAR, null);
        if (dto.getInicio() > dto.getFim()) {
            throw new ResourceBadRequestException("O início da faixa não pode ser maior que o fim.");
        }
        if (!faixaRepository.findSobrepostas(dto.getInicio(), dto.getFim()).isEmpty()) {
            throw new ResourceConflictException("A faixa se sobrepõe a outra já cadastrada.");
        }
        FaixaAih f = new FaixaAih();
        f.setAno(dto.getAno());
        f.setInicio(dto.getInicio());
        f.setFim(dto.getFim());
        f.setProximo(dto.getInicio());
        f.setAtiva(true);
        f.setRegistradaEm(Instant.now());
        f.setRegistradaPorCpf(UsuarioAutenticado.cpf());
        return faixaDto(faixaRepository.save(f));
    }

    /** Ativa ou desativa a faixa (a desativada não numera). */
    @Transactional
    public LaudoAihDtos.Faixa ativarFaixa(UUID uuid, boolean ativa) {
        controleDeAcesso.exigir(NUMERAR, null);
        FaixaAih f = faixaRepository.findById(uuid).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar uma faixa de AIH com o id " + uuid + " em nossos registros."));
        f.setAtiva(ativa);
        return faixaDto(faixaRepository.save(f));
    }

    /** O número da AIH: os 12 dígitos e o dígito verificador, o resto da divisão por 11 (10 vira 0). */
    public static String numeroComDigito(long base) {
        long dv = base % 11;
        return String.format(Locale.ROOT, "%012d%d", base, dv == 10 ? 0 : dv);
    }

    // ── PDF ────────────────────────────────────────────────────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public byte[] pdf(UUID uuid) {
        LaudoAih l = laudoRepository.findById(uuid).orElseThrow(() -> naoEncontrado(uuid));
        controleDeAcesso.exigirVisivel(l.getUnidade(), VER);
        ContextoAuditoria.paciente(l.getPaciente().getUuid());
        ContextoAuditoria.registro(l.getUuid());
        ByteArrayOutputStream saida = new ByteArrayOutputStream();
        Document doc = new Document(PageSize.A4, 40, 40, 40, 40);
        PdfWriter.getInstance(doc, saida);
        doc.addTitle("Laudo para solicitação de AIH");
        doc.addCreator("Mais Saúde Pública");
        doc.open();
        doc.add(new Paragraph("Laudo para solicitação/autorização de internação hospitalar", TITULO));
        doc.add(new Paragraph(l.getUnidade().getNome() + (l.getUnidade().getCnes() != null ? " · CNES " + l.getUnidade().getCnes() : ""), NORMAL));
        doc.add(Chunk.NEWLINE);

        var p = l.getPaciente();
        PdfPTable cab = tabela();
        par(cab, "Paciente", p.getNome());
        par(cab, "CNS", p.getCartaoSus());
        par(cab, "Nascimento", p.getDataNascimento() != null ? DATA.format(p.getDataNascimento()) : null);
        par(cab, "Sexo", p.getSexo() != null ? rotulo(p.getSexo().name()) : null);
        par(cab, "Município", p.getEndereco() != null ? p.getEndereco().getCidade() : null);
        par(cab, "Caráter", rotulo(l.getCarater().name()));
        doc.add(cab);

        secao(doc, "Justificativa da internação");
        doc.add(bloco("Principais sinais e sintomas clínicos", l.getSinaisSintomas()));
        doc.add(bloco("Condições que justificam a internação", l.getCondicoesJustificativas()));
        if (l.getResultadosExames() != null) {
            doc.add(bloco("Principais resultados de provas diagnósticas", l.getResultadosExames()));
        }

        secao(doc, "Diagnóstico e procedimento");
        PdfPTable diag = tabela();
        par(diag, "Diagnóstico inicial", l.getDiagnosticoInicial());
        par(diag, "CID-10 principal", l.getCidPrincipal());
        par(diag, "CID-10 secundário", l.getCidSecundario());
        par(diag, "CID-10 causa associada", l.getCidCausaAssociada());
        par(diag, "Procedimento", l.getProcedimento().getNome());
        par(diag, "Código SIGTAP", l.getProcedimento().getSigtap());
        par(diag, "Clínica", rotulo(l.getClinica().name()));
        par(diag, "Causa externa", l.getCausaExterna() != null ? rotulo(l.getCausaExterna().name()) : null);
        doc.add(diag);

        secao(doc, "Solicitação");
        PdfPTable sol = tabela();
        par(sol, "Médico solicitante", l.getSolicitante().getNome() + " (matrícula " + l.getSolicitante().getMatricula() + ")");
        par(sol, "Solicitado em", DATA_HORA.format(l.getSolicitadoEm()));
        doc.add(sol);

        secao(doc, "Autorização");
        PdfPTable aut = tabela();
        par(aut, "Situação", rotulo(l.getStatus().name()));
        par(aut, "Número da AIH", l.getNumeroAih());
        par(aut, "Autorizador", l.getAutorizador() != null ? l.getAutorizador().getNome() + " (matrícula " + l.getAutorizador().getMatricula() + ")" : null);
        par(aut, "Decidido em", l.getDecididoEm() != null ? DATA_HORA.format(l.getDecididoEm()) : null);
        par(aut, "Observação", l.getMotivoDecisao());
        doc.add(aut);
        doc.add(Chunk.NEWLINE);
        doc.add(new Paragraph("Autorização interna. O envio ao SIH é feito fora deste sistema.", PEQUENA));
        doc.close();
        return saida.toByteArray();
    }

    // ── Apoio ──────────────────────────────────────────────────────────────────────────────────────────────────────────

    private void preencher(LaudoAih l, LaudoAihRequestDto dto) {
        ProcedimentoCatalogo proc = procedimentoRepository.findById(dto.getProcedimentoId()).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar um procedimento com o id " + dto.getProcedimentoId() + " em nossos registros."));
        if (!proc.isAtivo()) {
            throw new ResourceUnprocessableEntityException("O procedimento " + proc.getNome() + " está inativo.");
        }
        l.setProcedimento(proc);
        l.setSinaisSintomas(dto.getSinaisSintomas().trim());
        l.setCondicoesJustificativas(dto.getCondicoesJustificativas().trim());
        l.setResultadosExames(texto(dto.getResultadosExames()));
        l.setDiagnosticoInicial(dto.getDiagnosticoInicial().trim());
        l.setCidPrincipal(cid(dto.getCidPrincipal()));
        l.setCidSecundario(cid(dto.getCidSecundario()));
        l.setCidCausaAssociada(cid(dto.getCidCausaAssociada()));
        l.setClinica(dto.getClinica());
        l.setCausaExterna(dto.getCausaExterna());
    }

    private LaudoAih decidindo(UUID uuid) {
        LaudoAih l = travar(uuid);
        controleDeAcesso.exigir(AUTORIZAR, l.getUnidade());
        if (l.getStatus() != StatusLaudoAih.SOLICITADO) {
            throw new ResourceUnprocessableEntityException("Só o laudo solicitado pode ser decidido; este está " + l.getStatus() + ".");
        }
        return l;
    }

    /** O autorizador não pode ser quem solicitou: nem o mesmo profissional, nem o mesmo usuário que registrou (422). */
    private Profissional autorizador(LaudoAih l, DecisaoLaudoAihRequestDto dto) {
        Profissional a = profissional(dto.getProfissionalMatricula());
        String cpf = UsuarioAutenticado.cpf();
        boolean mesmoProfissional = a.getUuid().equals(l.getSolicitante().getUuid());
        boolean mesmoUsuario = cpf != null && (cpf.equals(l.getRegistradoPorCpf()) || cpf.equals(l.getSolicitante().getCpf()));
        if (mesmoProfissional || mesmoUsuario) {
            throw new ResourceUnprocessableEntityException("Quem solicitou a AIH não pode decidir sobre ela; outro médico autorizador decide.");
        }
        return a;
    }

    private String proximoNumero() {
        int ano = LocalDate.now().getYear();
        List<FaixaAih> vigentes = faixaRepository.findVigentesParaNumerar(ano);
        if (vigentes.isEmpty()) {
            throw new ResourceUnprocessableEntityException("Não há faixa de numeração de AIH vigente para " + ano
                    + " com número livre. Cadastre a faixa recebida do gestor.");
        }
        FaixaAih f = vigentes.get(0);
        long base = f.getProximo();
        f.setProximo(base + 1);
        faixaRepository.save(f);
        return numeroComDigito(base);
    }

    private static LaudoAihDtos.Faixa faixaDto(FaixaAih f) {
        return LaudoAihDtos.Faixa.from(f, numeroComDigito(f.getInicio()), numeroComDigito(f.getFim()));
    }

    private static String exigirMotivo(DecisaoLaudoAihRequestDto dto) {
        String motivo = texto(dto.getMotivo());
        if (motivo == null) {
            throw new ResourceBadRequestException("Informe o motivo.");
        }
        return motivo;
    }

    private static String texto(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }

    private static String cid(String s) {
        String t = texto(s);
        return t == null ? null : t.toUpperCase(Locale.ROOT).replace(".", "");
    }

    private LaudoAihDtos.Laudo detalhe(LaudoAih l) {
        ContextoAuditoria.paciente(l.getPaciente().getUuid());
        ContextoAuditoria.registro(l.getUuid());
        return LaudoAihDtos.Laudo.from(l, eventoRepository.findByLaudo_UuidOrderByOcorridoEmAsc(l.getUuid()));
    }

    private void registrar(LaudoAih l, TipoEventoLaudoAih tipo, String texto, Profissional quem) {
        EventoLaudoAih e = new EventoLaudoAih();
        e.setLaudo(l);
        e.setTipo(tipo);
        e.setTexto(texto != null && texto.length() > 1000 ? texto.substring(0, 1000) : texto);
        e.setProfissional(quem);
        e.setOcorridoEm(Instant.now());
        e.setRegistradoPorCpf(UsuarioAutenticado.cpf());
        eventoRepository.save(e);
    }

    private LaudoAih travar(UUID uuid) {
        LaudoAih l = laudoRepository.findByIdParaAtualizar(uuid).orElseThrow(() -> naoEncontrado(uuid));
        ContextoAuditoria.paciente(l.getPaciente().getUuid());
        return l;
    }

    private Profissional profissional(String matricula) {
        return profissionalRepository.findByMatricula(matricula.trim()).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar um profissional com a matrícula " + matricula.trim() + " em nossos registros."));
    }

    private static ResourceNotFoundException naoEncontrado(UUID uuid) {
        return new ResourceNotFoundException("Não foi possível encontrar um laudo de AIH com o id " + uuid + " em nossos registros.");
    }

    private static PdfPTable tabela() {
        PdfPTable t = new PdfPTable(new float[]{1.4f, 4f});
        t.setWidthPercentage(100);
        return t;
    }

    private static void par(PdfPTable t, String rotulo, String valor) {
        PdfPCell r = new PdfPCell(new Phrase(rotulo, NEGRITO));
        r.setBorder(0);
        r.setPadding(2);
        r.setVerticalAlignment(Element.ALIGN_TOP);
        t.addCell(r);
        PdfPCell v = new PdfPCell(new Phrase(valor != null && !valor.isBlank() ? valor : "—", NORMAL));
        v.setBorder(0);
        v.setPadding(2);
        t.addCell(v);
    }

    private static void secao(Document doc, String titulo) {
        Paragraph p = new Paragraph(titulo, SECAO);
        p.setSpacingBefore(10);
        p.setSpacingAfter(4);
        doc.add(p);
    }

    private static Paragraph bloco(String rotulo, String texto) {
        Paragraph p = new Paragraph();
        p.add(new Phrase(rotulo + ": ", NEGRITO));
        p.add(new Phrase(texto, NORMAL));
        p.setSpacingAfter(4);
        return p;
    }

    private static String rotulo(String nome) {
        String s = nome.replace('_', ' ').toLowerCase(Locale.ROOT);
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }
}
