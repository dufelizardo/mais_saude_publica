package com.edufelizardo.maissaudepublica.services.version1;

import com.edufelizardo.maissaudepublica.config.ContextoAuditoria;
import com.edufelizardo.maissaudepublica.config.UsuarioAutenticado;
import com.edufelizardo.maissaudepublica.exceptions.ResourceBadRequestException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceForbiddenException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceNotFoundException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceUnprocessableEntityException;
import com.edufelizardo.maissaudepublica.models.AdendoFichaAph;
import com.edufelizardo.maissaudepublica.models.FichaAph;
import com.edufelizardo.maissaudepublica.models.MedicamentoAph;
import com.edufelizardo.maissaudepublica.models.OcorrenciaUrgencia;
import com.edufelizardo.maissaudepublica.models.Profissional;
import com.edufelizardo.maissaudepublica.models.SinaisVitaisAph;
import com.edufelizardo.maissaudepublica.models.UnidadeDeSaude;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.FichaAphRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.RegistroAphRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.FichaAphDtos;
import com.edufelizardo.maissaudepublica.models.enuns.DesfechoAph;
import com.edufelizardo.maissaudepublica.models.enuns.NaturezaAph;
import com.edufelizardo.maissaudepublica.models.enuns.ProcedimentoAph;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoOcorrenciaUrgencia;
import com.edufelizardo.maissaudepublica.repositories.AdendoFichaAphRepository;
import com.edufelizardo.maissaudepublica.repositories.FichaAphRepository;
import com.edufelizardo.maissaudepublica.repositories.MedicamentoAphRepository;
import com.edufelizardo.maissaudepublica.repositories.OcorrenciaUrgenciaRepository;
import com.edufelizardo.maissaudepublica.repositories.ProfissionalRepository;
import com.edufelizardo.maissaudepublica.repositories.SinaisVitaisAphRepository;
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
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Ficha clínica do atendimento pré-hospitalar (ADR-0188). A equipe da viatura preenche na ocorrência com recurso enviado: cena,
 * avaliação primária, sinais vitais em série, procedimentos, medicamentos, impressão diagnóstica, desfecho e entrega no destino.
 * Finalizada, não muda; correção é adendo. Lê a ficha quem tem dado clínico da ocorrência: a central (equipe, atendente, regulador) e a
 * unidade de destino.
 */
@Service
public class FichaAphService {

    public static final String REGISTRAR = "APH.REGISTRAR";
    private static final DateTimeFormatter DATA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm").withZone(ZoneId.of("America/Sao_Paulo"));
    private static final Font TITULO = new Font(Font.HELVETICA, 14, Font.BOLD);
    private static final Font SECAO = new Font(Font.HELVETICA, 10, Font.BOLD);
    private static final Font NEGRITO = new Font(Font.HELVETICA, 9, Font.BOLD);
    private static final Font NORMAL = new Font(Font.HELVETICA, 9, Font.NORMAL);
    private static final Font PEQUENA = new Font(Font.HELVETICA, 7.5f, Font.NORMAL, new Color(74, 83, 97));

    @Autowired
    private FichaAphRepository fichaRepository;

    @Autowired
    private SinaisVitaisAphRepository sinaisRepository;

    @Autowired
    private MedicamentoAphRepository medicamentoRepository;

    @Autowired
    private AdendoFichaAphRepository adendoRepository;

    @Autowired
    private OcorrenciaUrgenciaRepository ocorrenciaRepository;

    @Autowired
    private ProfissionalRepository profissionalRepository;

    @Autowired
    private ControleDeAcesso controleDeAcesso;

    /** As ocorrências com recurso enviado ainda em curso, e as concluídas nas últimas 24 horas com a ficha por finalizar. */
    @Transactional(readOnly = true)
    public List<FichaAphDtos.Atendimento> atendimentos(UUID centralId) {
        controleDeAcesso.exigirAlguma(REGISTRAR);
        Instant desde = Instant.now().minus(Duration.ofHours(24));
        Map<UUID, OcorrenciaUrgencia> ocorrencias = new LinkedHashMap<>();
        Stream.concat(ocorrenciaRepository.findNasSituacoes(OcorrenciaUrgenciaService.ABERTAS).stream(),
                        ocorrenciaRepository.findRecebidasDesde(desde).stream())
                .filter(OcorrenciaUrgencia::comRecurso)
                .filter(o -> o.getSituacao() != SituacaoOcorrenciaUrgencia.CANCELADA)
                .filter(o -> centralId == null || o.getCentral().getUuid().equals(centralId))
                .forEach(o -> ocorrencias.putIfAbsent(o.getUuid(), o));
        List<OcorrenciaUrgencia> visiveis = controleDeAcesso.filtrar(ocorrencias.values().stream(), o -> Stream.of(o.getCentral()), REGISTRAR).toList();
        Map<UUID, FichaAph> fichas = visiveis.isEmpty() ? Map.of()
                : fichaRepository.findByOcorrencia_UuidIn(visiveis.stream().map(OcorrenciaUrgencia::getUuid).toList()).stream()
                .collect(Collectors.toMap(f -> f.getOcorrencia().getUuid(), Function.identity()));
        return visiveis.stream()
                .filter(o -> o.getSituacao() != SituacaoOcorrenciaUrgencia.CONCLUIDA || fichas.get(o.getUuid()) == null || !fichas.get(o.getUuid()).finalizada())
                .sorted(Comparator.comparing(OcorrenciaUrgencia::getRecebidaEm).reversed())
                .map(o -> FichaAphDtos.Atendimento.from(o, fichas.get(o.getUuid()))).toList();
    }

    /** A ficha da ocorrência (404 se ainda não aberta). */
    @Transactional(readOnly = true)
    public FichaAphDtos.Ficha daOcorrencia(UUID ocorrenciaId) {
        OcorrenciaUrgencia o = ocorrencia(ocorrenciaId);
        exigirLeitura(o);
        FichaAph f = fichaRepository.findByOcorrencia_Uuid(ocorrenciaId).orElseThrow(() -> new ResourceNotFoundException(
                "A ocorrência " + o.getProtocolo() + " ainda não tem ficha do APH."));
        return detalhe(f);
    }

    /**
     * Abre ou atualiza a ficha da ocorrência. Só com recurso enviado (422), fora da cancelada (422), antes da finalização (422).
     * "Outro" procedimento pede a descrição (400).
     */
    @Transactional
    public FichaAphDtos.Ficha salvar(UUID ocorrenciaId, FichaAphRequestDto dto) {
        OcorrenciaUrgencia o = ocorrencia(ocorrenciaId);
        controleDeAcesso.exigir(REGISTRAR, o.getCentral());
        if (!o.comRecurso()) {
            throw new ResourceUnprocessableEntityException("A ficha do APH é das ocorrências com recurso enviado à cena.");
        }
        if (o.getSituacao() == SituacaoOcorrenciaUrgencia.CANCELADA) {
            throw new ResourceUnprocessableEntityException("A ocorrência foi cancelada.");
        }
        Instant agora = Instant.now();
        FichaAph f = fichaRepository.findByOcorrenciaParaAtualizar(ocorrenciaId).orElseGet(() -> {
            FichaAph nova = new FichaAph();
            nova.setOcorrencia(o);
            nova.setAbertaEm(agora);
            nova.setAbertaPorCpf(UsuarioAutenticado.cpf());
            return nova;
        });
        exigirAberta(f);
        Set<ProcedimentoAph> procedimentos = dto.getProcedimentos() != null ? dto.getProcedimentos() : Set.of();
        if (procedimentos.contains(ProcedimentoAph.OUTRO) && texto(dto.getProcedimentoOutro()) == null) {
            throw new ResourceBadRequestException("Descreva o outro procedimento.");
        }
        f.setNatureza(dto.getNatureza());
        f.setMecanismoTrauma(dto.getNatureza() == NaturezaAph.TRAUMA ? dto.getMecanismoTrauma() : null);
        f.setSituacaoEncontrada(texto(dto.getSituacaoEncontrada()));
        f.setHemorragiaExsanguinante(dto.getHemorragiaExsanguinante());
        f.setViaAerea(texto(dto.getViaAerea()));
        f.setRespiracao(texto(dto.getRespiracao()));
        f.setCirculacao(texto(dto.getCirculacao()));
        f.setGlasgowOcular(dto.getGlasgowOcular());
        f.setGlasgowVerbal(dto.getGlasgowVerbal());
        f.setGlasgowMotor(dto.getGlasgowMotor());
        f.setPupilas(texto(dto.getPupilas()));
        f.setExposicao(texto(dto.getExposicao()));
        f.setProcedimentos(new LinkedHashSet<>(procedimentos));
        f.setProcedimentoOutro(procedimentos.contains(ProcedimentoAph.OUTRO) ? texto(dto.getProcedimentoOutro()) : null);
        f.setImpressaoDiagnostica(texto(dto.getImpressaoDiagnostica()));
        String cid = texto(dto.getCid());
        f.setCid(cid != null ? cid.toUpperCase(Locale.ROOT).replace(".", "") : null);
        f.setDesfecho(dto.getDesfecho());
        f.setRecusaNome(texto(dto.getRecusaNome()));
        f.setObitoEm(dto.getObitoEm());
        String constatou = texto(dto.getObitoConstatadoPorMatricula());
        f.setObitoConstatadoPor(constatou != null ? profissional(constatou) : null);
        f.setRecebidoPor(texto(dto.getRecebidoPor()));
        f.setEntregueEm(dto.getEntregueEm());
        f.setAtualizadaEm(agora);
        return detalhe(fichaRepository.save(f));
    }

    /** Uma série de sinais vitais: ao menos uma medida (400); a hora não pode estar no futuro (422). */
    @Transactional
    public FichaAphDtos.Ficha registrarSinais(UUID fichaId, RegistroAphRequestDto dto) {
        FichaAph f = travarParaRegistrar(fichaId);
        if (Stream.of(dto.getPaSistolica(), dto.getPaDiastolica(), dto.getFrequenciaCardiaca(), dto.getFrequenciaRespiratoria(), dto.getSaturacao(),
                dto.getTemperatura(), dto.getGlicemia(), dto.getDor(), dto.getGlasgow()).allMatch(java.util.Objects::isNull)) {
            throw new ResourceBadRequestException("Informe ao menos uma medida.");
        }
        SinaisVitaisAph s = new SinaisVitaisAph();
        s.setFicha(f);
        s.setMedidoEm(hora(dto.getEm()));
        s.setPaSistolica(dto.getPaSistolica());
        s.setPaDiastolica(dto.getPaDiastolica());
        s.setFrequenciaCardiaca(dto.getFrequenciaCardiaca());
        s.setFrequenciaRespiratoria(dto.getFrequenciaRespiratoria());
        s.setSaturacao(dto.getSaturacao());
        s.setTemperatura(dto.getTemperatura());
        s.setGlicemia(dto.getGlicemia());
        s.setDor(dto.getDor());
        s.setGlasgow(dto.getGlasgow());
        s.setProfissional(profissional(dto.getProfissionalMatricula()));
        s.setRegistradoEm(Instant.now());
        s.setRegistradoPorCpf(UsuarioAutenticado.cpf());
        sinaisRepository.save(s);
        return detalhe(f);
    }

    /** Um medicamento dado: nome, dose e via (400). */
    @Transactional
    public FichaAphDtos.Ficha registrarMedicamento(UUID fichaId, RegistroAphRequestDto dto) {
        FichaAph f = travarParaRegistrar(fichaId);
        if (texto(dto.getNome()) == null || texto(dto.getDose()) == null || dto.getVia() == null) {
            throw new ResourceBadRequestException("Informe o medicamento, a dose e a via.");
        }
        MedicamentoAph m = new MedicamentoAph();
        m.setFicha(f);
        m.setNome(dto.getNome().trim());
        m.setDose(dto.getDose().trim());
        m.setVia(dto.getVia());
        m.setAdministradoEm(hora(dto.getEm()));
        m.setProfissional(profissional(dto.getProfissionalMatricula()));
        m.setRegistradoEm(Instant.now());
        m.setRegistradoPorCpf(UsuarioAutenticado.cpf());
        medicamentoRepository.save(m);
        return detalhe(f);
    }

    /**
     * Finaliza a ficha (422 com o que falta): natureza e desfecho; no óbito, a hora e o médico que constatou; na recusa, quem recusou;
     * na remoção, quem recebeu no destino.
     */
    @Transactional
    public FichaAphDtos.Ficha finalizar(UUID fichaId, RegistroAphRequestDto dto) {
        FichaAph f = travarParaRegistrar(fichaId);
        if (f.getNatureza() == null || f.getDesfecho() == null) {
            throw new ResourceUnprocessableEntityException("Informe a natureza do atendimento e o desfecho antes de finalizar.");
        }
        if (f.getDesfecho() == DesfechoAph.OBITO_NO_LOCAL && (f.getObitoEm() == null || f.getObitoConstatadoPor() == null)) {
            throw new ResourceUnprocessableEntityException("No óbito no local, informe a hora e o médico que constatou.");
        }
        if (f.getDesfecho() == DesfechoAph.RECUSA_DE_REMOCAO && f.getRecusaNome() == null) {
            throw new ResourceUnprocessableEntityException("Na recusa de remoção, informe quem recusou.");
        }
        if (f.getDesfecho() == DesfechoAph.REMOVIDO_AO_DESTINO && f.getRecebidoPor() == null) {
            throw new ResourceUnprocessableEntityException("Na remoção, informe quem recebeu o paciente no destino.");
        }
        f.setFinalizadaEm(Instant.now());
        f.setFinalizadaPor(profissional(dto.getProfissionalMatricula()));
        return detalhe(fichaRepository.save(f));
    }

    /** Adendo à ficha finalizada (422 antes), com o texto (400). */
    @Transactional
    public FichaAphDtos.Ficha adendo(UUID fichaId, RegistroAphRequestDto dto) {
        FichaAph f = fichaRepository.findByIdParaAtualizar(fichaId).orElseThrow(() -> naoEncontrada(fichaId));
        ContextoAuditoria.registro(f.getUuid());
        controleDeAcesso.exigir(REGISTRAR, f.getOcorrencia().getCentral());
        if (!f.finalizada()) {
            throw new ResourceUnprocessableEntityException("A ficha ainda não foi finalizada: corrija direto nela.");
        }
        if (texto(dto.getTexto()) == null) {
            throw new ResourceBadRequestException("Escreva o adendo.");
        }
        AdendoFichaAph a = new AdendoFichaAph();
        a.setFicha(f);
        a.setTexto(dto.getTexto().trim());
        a.setProfissional(profissional(dto.getProfissionalMatricula()));
        a.setRegistradoEm(Instant.now());
        a.setRegistradoPorCpf(UsuarioAutenticado.cpf());
        adendoRepository.save(a);
        return detalhe(f);
    }

    /** A ficha em PDF, para entregar no destino e anexar ao prontuário físico. */
    @Transactional(readOnly = true)
    public byte[] pdf(UUID fichaId) {
        FichaAph f = fichaRepository.findById(fichaId).orElseThrow(() -> naoEncontrada(fichaId));
        exigirLeitura(f.getOcorrencia());
        FichaAphDtos.Ficha d = detalhe(f);
        ByteArrayOutputStream saida = new ByteArrayOutputStream();
        Document doc = new Document(PageSize.A4, 40, 40, 40, 40);
        PdfWriter.getInstance(doc, saida);
        doc.addTitle("Ficha de atendimento pré-hospitalar");
        doc.addCreator("Mais Saúde Pública");
        doc.open();
        doc.add(new Paragraph("Ficha de atendimento pré-hospitalar", TITULO));
        doc.add(new Paragraph(d.centralNome() + " · ocorrência " + d.protocolo() + " · " + d.recurso(), NORMAL));
        doc.add(Chunk.NEWLINE);
        PdfPTable cab = tabela();
        par(cab, "Paciente", d.pacienteNome());
        par(cab, "Natureza", rotulo(d.natureza()));
        par(cab, "Mecanismo", rotulo(d.mecanismoTrauma()));
        par(cab, "Situação encontrada", d.situacaoEncontrada());
        doc.add(cab);
        secao(doc, "Avaliação primária (XABCDE)");
        PdfPTable av = tabela();
        par(av, "Hemorragia exsanguinante", d.hemorragiaExsanguinante() == null ? null : d.hemorragiaExsanguinante() ? "Sim" : "Não");
        par(av, "Via aérea", d.viaAerea());
        par(av, "Respiração", d.respiracao());
        par(av, "Circulação", d.circulacao());
        par(av, "Glasgow", d.glasgowTotal() != null ? d.glasgowTotal() + " (O" + d.glasgowOcular() + " V" + d.glasgowVerbal() + " M" + d.glasgowMotor() + ")" : null);
        par(av, "Pupilas", d.pupilas());
        par(av, "Exposição", d.exposicao());
        doc.add(av);
        secao(doc, "Sinais vitais");
        if (d.sinaisVitais().isEmpty()) {
            doc.add(new Paragraph("Sem registro.", NORMAL));
        }
        for (FichaAphDtos.Sinais s : d.sinaisVitais()) {
            doc.add(new Paragraph(DATA_HORA.format(s.medidoEm()) + " · " + sinais(s) + " · " + s.profissionalNome(), NORMAL));
        }
        secao(doc, "Procedimentos e medicamentos");
        doc.add(new Paragraph(d.procedimentos().isEmpty() ? "Nenhum procedimento." : d.procedimentos().stream().map(FichaAphService::rotulo)
                .collect(Collectors.joining(", ")) + (d.procedimentoOutro() != null ? " (" + d.procedimentoOutro() + ")" : ""), NORMAL));
        for (FichaAphDtos.Medicamento m : d.medicamentos()) {
            doc.add(new Paragraph(DATA_HORA.format(m.administradoEm()) + " · " + m.nome() + " " + m.dose() + " " + rotulo(m.via()) + " · "
                    + m.profissionalNome(), NORMAL));
        }
        secao(doc, "Impressão diagnóstica e desfecho");
        PdfPTable fim = tabela();
        par(fim, "Impressão diagnóstica", d.impressaoDiagnostica());
        par(fim, "CID-10", d.cid());
        par(fim, "Desfecho", rotulo(d.desfecho()));
        par(fim, "Recusa por", d.recusaNome());
        par(fim, "Óbito", d.obitoEm() != null ? DATA_HORA.format(d.obitoEm()) + " · constatado por " + d.obitoConstatadoPorNome() : null);
        par(fim, "Destino", d.destinoNome());
        par(fim, "Recebido por", d.recebidoPor() != null ? d.recebidoPor() + (d.entregueEm() != null ? " às " + DATA_HORA.format(d.entregueEm()) : "") : null);
        par(fim, "Finalizada", d.finalizadaEm() != null ? DATA_HORA.format(d.finalizadaEm()) + " por " + d.finalizadaPorNome() : "Não finalizada");
        doc.add(fim);
        if (!d.adendos().isEmpty()) {
            secao(doc, "Adendos");
            for (FichaAphDtos.Adendo a : d.adendos()) {
                doc.add(new Paragraph(DATA_HORA.format(a.registradoEm()) + " · " + a.profissionalNome() + ": " + a.texto(), NORMAL));
            }
        }
        doc.add(Chunk.NEWLINE);
        doc.add(new Paragraph("Documento gerado pelo Mais Saúde Pública.", PEQUENA));
        doc.close();
        return saida.toByteArray();
    }

    // ── Apoio ──────────────────────────────────────────────────────────────────────────────────────────────────────────

    private FichaAph travarParaRegistrar(UUID fichaId) {
        FichaAph f = fichaRepository.findByIdParaAtualizar(fichaId).orElseThrow(() -> naoEncontrada(fichaId));
        ContextoAuditoria.registro(f.getUuid());
        controleDeAcesso.exigir(REGISTRAR, f.getOcorrencia().getCentral());
        exigirAberta(f);
        return f;
    }

    private static void exigirAberta(FichaAph f) {
        if (f.finalizada()) {
            throw new ResourceUnprocessableEntityException("A ficha já foi finalizada: registre um adendo.");
        }
    }

    /** Dado clínico da ocorrência: a central (equipe, atendente, regulador) ou a unidade de destino. */
    private void exigirLeitura(OcorrenciaUrgencia o) {
        ContextoAuditoria.unidade(o.getCentral());
        if (o.getPaciente() != null) {
            ContextoAuditoria.paciente(o.getPaciente().getUuid());
        }
        boolean central = visivel(controleDeAcesso.unidadesVisiveis(REGISTRAR, OcorrenciaUrgenciaService.RECEBER, OcorrenciaUrgenciaService.REGULAR),
                o.getCentral());
        boolean destino = visivel(controleDeAcesso.unidadesVisiveis(OcorrenciaUrgenciaService.ATENDIMENTO), o.getDestino());
        if (!central && !destino) {
            throw new ResourceForbiddenException("A ficha do APH é dado clínico: só a central e a unidade de destino a veem.");
        }
    }

    private static boolean visivel(Optional<Set<UUID>> visiveis, UnidadeDeSaude u) {
        return u != null && visiveis.map(s -> s.contains(u.getUuid())).orElse(true);
    }

    private static Instant hora(Instant informada) {
        Instant agora = Instant.now();
        Instant h = informada != null ? informada : agora;
        if (h.isAfter(agora.plus(Duration.ofMinutes(5)))) {
            throw new ResourceUnprocessableEntityException("A hora não pode estar no futuro.");
        }
        return h;
    }

    private FichaAphDtos.Ficha detalhe(FichaAph f) {
        ContextoAuditoria.registro(f.getUuid());
        if (f.getOcorrencia().getPaciente() != null) {
            ContextoAuditoria.paciente(f.getOcorrencia().getPaciente().getUuid());
        }
        if (f.getUuid() == null) {
            return FichaAphDtos.Ficha.from(f, List.of(), List.of(), List.of());
        }
        return FichaAphDtos.Ficha.from(f, sinaisRepository.findByFicha_UuidOrderByMedidoEmAsc(f.getUuid()),
                medicamentoRepository.findByFicha_UuidOrderByAdministradoEmAsc(f.getUuid()),
                adendoRepository.findByFicha_UuidOrderByRegistradoEmAsc(f.getUuid()));
    }

    private OcorrenciaUrgencia ocorrencia(UUID uuid) {
        return ocorrenciaRepository.findById(uuid).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar uma ocorrência com o id " + uuid + " em nossos registros."));
    }

    private Profissional profissional(String matricula) {
        if (matricula == null || matricula.isBlank()) {
            throw new ResourceBadRequestException("Informe a matrícula.");
        }
        return profissionalRepository.findByMatricula(matricula.trim()).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar um profissional com a matrícula " + matricula.trim() + " em nossos registros."));
    }

    private static String texto(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }

    private static ResourceNotFoundException naoEncontrada(UUID uuid) {
        return new ResourceNotFoundException("Não foi possível encontrar uma ficha do APH com o id " + uuid + " em nossos registros.");
    }

    private static String sinais(FichaAphDtos.Sinais s) {
        StringBuilder b = new StringBuilder();
        if (s.paSistolica() != null || s.paDiastolica() != null) {
            b.append("PA ").append(s.paSistolica() != null ? s.paSistolica() : "?").append('/').append(s.paDiastolica() != null ? s.paDiastolica() : "?").append(' ');
        }
        if (s.frequenciaCardiaca() != null) b.append("FC ").append(s.frequenciaCardiaca()).append(' ');
        if (s.frequenciaRespiratoria() != null) b.append("FR ").append(s.frequenciaRespiratoria()).append(' ');
        if (s.saturacao() != null) b.append("SpO2 ").append(s.saturacao()).append("% ");
        if (s.temperatura() != null) b.append("T ").append(s.temperatura()).append("°C ");
        if (s.glicemia() != null) b.append("Glic ").append(s.glicemia()).append(' ');
        if (s.dor() != null) b.append("Dor ").append(s.dor()).append(' ');
        if (s.glasgow() != null) b.append("GCS ").append(s.glasgow());
        return b.toString().trim();
    }

    private static PdfPTable tabela() {
        PdfPTable t = new PdfPTable(new float[]{1.6f, 4f});
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

    private static String rotulo(Enum<?> e) {
        if (e == null) {
            return null;
        }
        String s = e.name().replace('_', ' ').toLowerCase(Locale.ROOT);
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }
}
