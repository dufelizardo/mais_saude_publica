package com.edufelizardo.maissaudepublica.services.version1;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfPageEventHelper;
import com.lowagie.text.pdf.PdfWriter;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.util.List;

/**
 * Os termos da vigilância sanitária em PDF (ADR-0200): auto de infração, decisão, decisão do recurso, interdição, apreensão e
 * desinterdição. Cada página leva no rodapé o código e o QR code da verificação pública, como o laudo da ADR-0183.
 */
final class TermoSanitarioPdf {

    private static final Font TITULO = new Font(Font.HELVETICA, 14, Font.BOLD);
    private static final Font SECAO = new Font(Font.HELVETICA, 10, Font.BOLD);
    private static final Font NEGRITO = new Font(Font.HELVETICA, 9, Font.BOLD);
    private static final Font NORMAL = new Font(Font.HELVETICA, 9, Font.NORMAL);
    private static final Font PEQUENA = new Font(Font.HELVETICA, 7.5f, Font.NORMAL, new Color(74, 83, 97));

    /** Um bloco do termo: os pares rótulo e valor e, depois, os parágrafos. */
    record Bloco(String titulo, List<String[]> pares, List<String> paragrafos) {
    }

    private TermoSanitarioPdf() {
    }

    static byte[] gerar(String titulo, String subtitulo, List<Bloco> blocos, String assinatura, String codigo, String endereco) {
        ByteArrayOutputStream saida = new ByteArrayOutputStream();
        Document doc = new Document(PageSize.A4, 40, 40, 40, 100);
        PdfWriter writer = PdfWriter.getInstance(doc, saida);
        writer.setPageEvent(new PdfPageEventHelper() {
            @Override
            public void onEndPage(PdfWriter w, Document d) {
                rodape(w, d, codigo, endereco);
            }
        });
        doc.addTitle(titulo);
        doc.addCreator("Mais Saúde Pública");
        doc.open();
        doc.add(new Paragraph(titulo, TITULO));
        doc.add(new Paragraph(subtitulo, NORMAL));
        doc.add(Chunk.NEWLINE);
        for (Bloco b : blocos) {
            Paragraph s = new Paragraph(b.titulo(), SECAO);
            s.setSpacingBefore(8);
            s.setSpacingAfter(4);
            doc.add(s);
            if (!b.pares().isEmpty()) {
                PdfPTable t = new PdfPTable(new float[]{1, 3});
                t.setWidthPercentage(100);
                for (String[] par : b.pares()) {
                    celula(t, par[0], NEGRITO);
                    celula(t, par[1] != null && !par[1].isBlank() ? par[1] : "—", NORMAL);
                }
                doc.add(t);
            }
            for (String p : b.paragrafos()) {
                Paragraph x = new Paragraph(p, NORMAL);
                x.setSpacingAfter(3);
                doc.add(x);
            }
        }
        Paragraph s = new Paragraph("Assinatura", SECAO);
        s.setSpacingBefore(8);
        doc.add(s);
        doc.add(new Paragraph(assinatura, NORMAL));
        doc.add(Chunk.NEWLINE);
        doc.add(new Paragraph("Ciente do autuado ou responsável: ______________________________________   Data: ____/____/______", NORMAL));
        doc.close();
        return saida.toByteArray();
    }

    private static void rodape(PdfWriter w, Document d, String codigo, String endereco) {
        PdfContentByte cb = w.getDirectContent();
        float tamanho = 64;
        desenharQr(cb, endereco, d.left(), d.bottom() - tamanho - 12, tamanho);
        PdfPTable t = new PdfPTable(1);
        t.setTotalWidth(d.right() - d.left() - tamanho - 12);
        PdfPCell c = new PdfPCell();
        c.setBorder(0);
        c.addElement(new Phrase("Código de verificação: " + codigo, NEGRITO));
        c.addElement(new Phrase("Confira a autenticidade em " + endereco, PEQUENA));
        c.addElement(new Phrase("Assinatura eletrônica interna (Lei 14.063/2020): o servidor autenticado emitiu o termo, e o sistema guarda o hash do arquivo.",
                PEQUENA));
        t.addCell(c);
        t.writeSelectedRows(0, -1, d.left() + tamanho + 12, d.bottom() - 10, cb);
    }

    private static void desenharQr(PdfContentByte cb, String texto, float x, float y, float tamanho) {
        try {
            BitMatrix m = new QRCodeWriter().encode(texto, BarcodeFormat.QR_CODE, 0, 0);
            float modulo = tamanho / m.getWidth();
            cb.saveState();
            cb.setColorFill(Color.BLACK);
            for (int i = 0; i < m.getWidth(); i++) {
                for (int j = 0; j < m.getHeight(); j++) {
                    if (m.get(i, j)) {
                        cb.rectangle(x + i * modulo, y + tamanho - (j + 1) * modulo, modulo, modulo);
                    }
                }
            }
            cb.fill();
            cb.restoreState();
        } catch (WriterException e) {
            throw new IllegalStateException("Não foi possível gerar o QR code do termo.", e);
        }
    }

    private static void celula(PdfPTable t, String texto, Font fonte) {
        PdfPCell c = new PdfPCell(new Phrase(texto, fonte));
        c.setBorder(0);
        c.setPadding(2);
        c.setVerticalAlignment(Element.ALIGN_TOP);
        t.addCell(c);
    }
}
