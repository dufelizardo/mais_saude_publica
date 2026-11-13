package com.edufelizardo.maissaudepublica.services.version1;

import com.edufelizardo.maissaudepublica.models.EstabelecimentoRegulado;
import com.edufelizardo.maissaudepublica.models.InspecaoSanitaria;
import com.edufelizardo.maissaudepublica.models.Profissional;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.VigilanciaSanitariaDtos;
import com.edufelizardo.maissaudepublica.models.enuns.RespostaRoteiro;
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

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * O termo de inspeção sanitária em PDF (ADR-0199): o estabelecimento, a inspeção, o roteiro respondido, as irregularidades com o
 * prazo de correção (a intimação), o relatório e a assinatura eletrônica interna do fiscal (CPF e SHA-256, como na ADR-0183).
 */
final class TermoInspecaoPdf {

    private static final DateTimeFormatter DATA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm").withZone(ZoneId.of("America/Sao_Paulo"));
    private static final DateTimeFormatter DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final Font TITULO = new Font(Font.HELVETICA, 14, Font.BOLD);
    private static final Font SECAO = new Font(Font.HELVETICA, 10, Font.BOLD);
    private static final Font NEGRITO = new Font(Font.HELVETICA, 9, Font.BOLD);
    private static final Font NORMAL = new Font(Font.HELVETICA, 9, Font.NORMAL);
    private static final Font PEQUENA = new Font(Font.HELVETICA, 7.5f, Font.NORMAL, new Color(74, 83, 97));
    private static final Map<RespostaRoteiro, String> RESPOSTAS = Map.of(RespostaRoteiro.CONFORME, "Conforme",
            RespostaRoteiro.NAO_CONFORME, "Não conforme", RespostaRoteiro.NAO_SE_APLICA, "Não se aplica");

    private TermoInspecaoPdf() {
    }

    static byte[] gerar(InspecaoSanitaria i, VigilanciaSanitariaDtos.Inspecao dto) {
        ByteArrayOutputStream saida = new ByteArrayOutputStream();
        Document doc = new Document(PageSize.A4, 40, 40, 40, 40);
        PdfWriter.getInstance(doc, saida);
        doc.addTitle("Termo de inspeção sanitária");
        doc.addCreator("Mais Saúde Pública");
        doc.open();
        doc.add(new Paragraph("Termo de inspeção sanitária", TITULO));
        doc.add(new Paragraph("Vigilância Sanitária municipal · poder de polícia administrativa (Lei 6.437/1977)", NORMAL));
        doc.add(Chunk.NEWLINE);

        EstabelecimentoRegulado e = i.getEstabelecimento();
        secao(doc, "Estabelecimento");
        PdfPTable est = tabela();
        par(est, "Razão social", e.getRazaoSocial());
        par(est, "Nome fantasia", e.getNomeFantasia());
        par(est, e.getDocumento().length() == 14 ? "CNPJ" : "CPF", formatar(e.getDocumento()));
        par(est, "Endereço", e.getEndereco());
        par(est, "Atividades", e.getAtividades().stream().map(a -> a.getCnae() + " " + a.getDescricao()).collect(Collectors.joining("; ")));
        par(est, "Responsável técnico", e.getResponsavelTecnico() != null
                ? e.getResponsavelTecnico() + (e.getRegistroConselhoRt() != null ? " (" + e.getRegistroConselhoRt() + ")" : "") : null);
        doc.add(est);

        secao(doc, "Inspeção");
        PdfPTable insp = tabela();
        par(insp, "Data", DATA.format(i.getDataInspecao()));
        par(insp, "Motivo", VigilanciaSanitariaService.rotulo(i.getMotivo().name()) + (dto.denunciaProtocolo() != null ? " (denúncia " + dto.denunciaProtocolo() + ")" : ""));
        par(insp, "Resultado", VigilanciaSanitariaService.rotulo(i.getResultado().name()));
        par(insp, "Fiscais", i.getFiscais().stream().map(p -> p.getNome() + " (matrícula " + p.getMatricula() + ")").collect(Collectors.joining("; ")));
        par(insp, "Roteiro", dto.roteiroNome() != null ? dto.roteiroNome() + ", versão " + dto.roteiroVersao() : null);
        doc.add(insp);

        if (!dto.itens().isEmpty()) {
            secao(doc, "Roteiro de inspeção");
            PdfPTable rot = new PdfPTable(new float[]{1, 7, 2});
            rot.setWidthPercentage(100);
            for (VigilanciaSanitariaDtos.ItemRoteiro item : dto.itens()) {
                celula(rot, item.codigo(), NEGRITO);
                celula(rot, item.texto(), NORMAL);
                RespostaRoteiro r = dto.respostas().get(item.codigo());
                celula(rot, r != null ? RESPOSTAS.get(r) : "—", r == RespostaRoteiro.NAO_CONFORME ? NEGRITO : NORMAL);
            }
            doc.add(rot);
        }

        if (!dto.irregularidades().isEmpty()) {
            secao(doc, "Irregularidades e intimação para correção");
            for (VigilanciaSanitariaDtos.Irregularidade r : dto.irregularidades()) {
                Paragraph p = new Paragraph();
                p.add(new Phrase((r.itemCodigo() != null ? r.itemCodigo() + " · " : "") + VigilanciaSanitariaService.rotulo(r.gravidade().name()) + ": ", NEGRITO));
                p.add(new Phrase(r.descricao(), NORMAL));
                if (r.enquadramento() != null) {
                    p.add(new Phrase(" Enquadramento: " + r.enquadramento() + ".", NORMAL));
                }
                p.add(new Phrase(r.prazoCorrecao() != null ? " Corrigir até " + DATA.format(r.prazoCorrecao()) + "." : " Sem prazo de correção.", NEGRITO));
                p.setSpacingAfter(3);
                doc.add(p);
            }
        }

        secao(doc, "Relatório");
        doc.add(new Paragraph(i.getRelatorio(), NORMAL));
        doc.add(Chunk.NEWLINE);

        secao(doc, "Assinatura");
        String assinante = i.getAssinadoPorCpf() == null ? null : i.getFiscais().stream().filter(p -> i.getAssinadoPorCpf().equals(p.getCpf()))
                .map(Profissional::getNome).findFirst().orElse(null);
        doc.add(new Paragraph(assinante != null
                ? "Assinado eletronicamente por " + assinante + " em " + DATA_HORA.format(i.getRegistradoEm()) + " (assinatura eletrônica avançada, Lei 14.063/2020)."
                : "Registrado em " + DATA_HORA.format(i.getRegistradoEm()) + " sem assinatura eletrônica (sem login).", NORMAL));
        if (i.getHashConteudo() != null) {
            doc.add(new Paragraph("SHA-256 do conteúdo: " + i.getHashConteudo(), PEQUENA));
        }
        doc.add(Chunk.NEWLINE);
        doc.add(new Paragraph("Ciente do responsável pelo estabelecimento: ______________________________________   Data: ____/____/______", NORMAL));
        doc.close();
        return saida.toByteArray();
    }

    private static String formatar(String d) {
        if (d.length() == 14) {
            return d.substring(0, 2) + "." + d.substring(2, 5) + "." + d.substring(5, 8) + "/" + d.substring(8, 12) + "-" + d.substring(12);
        }
        return d.substring(0, 3) + "." + d.substring(3, 6) + "." + d.substring(6, 9) + "-" + d.substring(9);
    }

    private static PdfPTable tabela() {
        PdfPTable t = new PdfPTable(new float[]{1, 3});
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

    private static void celula(PdfPTable t, String texto, Font fonte) {
        PdfPCell c = new PdfPCell(new Phrase(texto, fonte));
        c.setPadding(3);
        c.setBorderColor(new Color(210, 214, 220));
        t.addCell(c);
    }

    private static void secao(Document doc, String titulo) {
        Paragraph p = new Paragraph(titulo, SECAO);
        p.setSpacingBefore(10);
        p.setSpacingAfter(4);
        doc.add(p);
    }
}
