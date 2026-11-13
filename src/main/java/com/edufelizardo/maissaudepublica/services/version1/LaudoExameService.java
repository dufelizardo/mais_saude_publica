package com.edufelizardo.maissaudepublica.services.version1;

import com.edufelizardo.maissaudepublica.config.ContextoAuditoria;
import com.edufelizardo.maissaudepublica.config.UsuarioAutenticado;
import com.edufelizardo.maissaudepublica.exceptions.ResourceNotFoundException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceUnprocessableEntityException;
import com.edufelizardo.maissaudepublica.models.EmissaoLaudoExame;
import com.edufelizardo.maissaudepublica.models.ItemPedidoExame;
import com.edufelizardo.maissaudepublica.models.PedidoExame;
import com.edufelizardo.maissaudepublica.models.ResultadoExame;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.EmissaoLaudoDtos;
import com.edufelizardo.maissaudepublica.models.enuns.CategoriaExame;
import com.edufelizardo.maissaudepublica.models.enuns.StatusItemExame;
import com.edufelizardo.maissaudepublica.repositories.EmissaoLaudoExameRepository;
import com.edufelizardo.maissaudepublica.repositories.ItemPedidoExameRepository;
import com.edufelizardo.maissaudepublica.repositories.PedidoExameRepository;
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
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Laudo de exames em PDF (ADR-0183), gerado no servidor com os resultados liberados e assinados. Cada emissão guarda o PDF, o
 * hash e um código de verificação pública. Baixar de novo sem resultado novo devolve a mesma emissão; com resultado novo,
 * retificação ou adendo, nasce outra versão e a anterior fica substituída.
 */
@Service
public class LaudoExameService {

    private static final DateTimeFormatter DATA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm").withZone(ZoneId.of("America/Sao_Paulo"));
    private static final DateTimeFormatter DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final String LETRAS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final SecureRandom SORTEIO = new SecureRandom();
    private static final Font TITULO = new Font(Font.HELVETICA, 14, Font.BOLD);
    private static final Font NEGRITO = new Font(Font.HELVETICA, 9, Font.BOLD);
    private static final Font NORMAL = new Font(Font.HELVETICA, 9, Font.NORMAL);
    private static final Font PEQUENA = new Font(Font.HELVETICA, 7.5f, Font.NORMAL, new Color(74, 83, 97));

    @Autowired
    private PedidoExameService pedidoExameService;

    @Autowired
    private PedidoExameRepository pedidoRepository;

    @Autowired
    private ItemPedidoExameRepository itemRepository;

    @Autowired
    private EmissaoLaudoExameRepository emissaoRepository;

    @Value("${app.security.recuperacao-senha.url-frontend:}")
    private String frontendUrl;

    /**
     * A emissão vigente do laudo do pedido, criando outra se os resultados liberados mudaram. Mesmas regras de acesso do detalhe
     * do pedido (403 fora do escopo). Nenhum resultado liberado: 422.
     */
    @Transactional
    public EmissaoLaudoExame emitir(UUID pedidoId) {
        pedidoExameService.buscarPorId(pedidoId);
        PedidoExame pedido = pedidoRepository.findById(pedidoId).orElseThrow();
        ContextoAuditoria.paciente(pedido.getPaciente().getUuid());
        ContextoAuditoria.registro(pedido.getUuid());
        List<ItemPedidoExame> liberados = itemRepository.findByPedido_Uuid(pedidoId).stream()
                .filter(i -> i.getStatus() == StatusItemExame.LIBERADO && i.getResultadoAtual() != null)
                .sorted(Comparator.comparing((ItemPedidoExame i) -> i.getExame().getCategoria()).thenComparing(i -> i.getExame().getNome()))
                .toList();
        if (liberados.isEmpty()) {
            throw new ResourceUnprocessableEntityException("O pedido ainda não tem resultado liberado para o laudo.");
        }
        Set<UUID> atuais = liberados.stream().map(i -> i.getResultadoAtual().getUuid()).collect(Collectors.toSet());
        List<EmissaoLaudoExame> anteriores = emissaoRepository.findByPedido_UuidOrderByVersaoDesc(pedidoId);
        Optional<EmissaoLaudoExame> vigente = anteriores.stream().findFirst();
        if (vigente.isPresent() && vigente.get().getResultados().stream().map(ResultadoExame::getUuid).collect(Collectors.toSet()).equals(atuais)) {
            return vigente.get();
        }
        int versao = vigente.map(e -> e.getVersao() + 1).orElse(1);
        String codigo = novoCodigo();
        Instant agora = Instant.now();
        byte[] pdf = gerarPdf(pedido, liberados, versao, codigo, agora);
        EmissaoLaudoExame e = new EmissaoLaudoExame();
        e.setPedido(pedido);
        e.setVersao(versao);
        e.setCodigo(codigo);
        e.setPdf(pdf);
        e.setHashPdf(AssinaturaResultado.sha256(pdf));
        e.setResultados(new java.util.ArrayList<>(liberados.stream().map(ItemPedidoExame::getResultadoAtual).toList()));
        e.setEmitidoEm(agora);
        e.setEmitidoPorCpf(UsuarioAutenticado.cpf());
        return emissaoRepository.save(e);
    }

    /** As emissões do pedido, da mais nova para a mais velha; mesmas regras de acesso do detalhe. */
    @Transactional(readOnly = true)
    public List<EmissaoLaudoDtos.Emissao> emissoes(UUID pedidoId) {
        pedidoExameService.buscarPorId(pedidoId);
        List<EmissaoLaudoExame> lista = emissaoRepository.findByPedido_UuidOrderByVersaoDesc(pedidoId);
        int ultima = lista.isEmpty() ? 0 : lista.get(0).getVersao();
        return lista.stream().map(e -> new EmissaoLaudoDtos.Emissao(e.getUuid(), e.getVersao(), e.getCodigo(), e.getHashPdf(), e.getEmitidoEm(),
                e.getResultados().size(), e.getVersao() == ultima)).toList();
    }

    /** Verificação pública, sem login: a emissão pelo código, sem nome do paciente. Código desconhecido: 404. */
    @Transactional(readOnly = true)
    public EmissaoLaudoDtos.Verificacao verificar(String codigo) {
        String c = codigo == null ? "" : codigo.trim().toUpperCase(Locale.ROOT);
        EmissaoLaudoExame e = emissaoRepository.findByCodigo(c).orElseThrow(() -> new ResourceNotFoundException(
                "Nenhum laudo foi emitido com o código " + c + "."));
        int vigente = emissaoRepository.findByPedido_UuidOrderByVersaoDesc(e.getPedido().getUuid()).get(0).getVersao();
        boolean substituido = e.getVersao() < vigente;
        List<String> exames = e.getResultados().stream().map(r -> r.getItem().getExame().getNome()).sorted().toList();
        return new EmissaoLaudoDtos.Verificacao(e.getCodigo(), !substituido, substituido, e.getVersao(), vigente, e.getEmitidoEm(),
                e.getPedido().getUnidadeSolicitante().getNome(), iniciais(e.getPedido().getPaciente().getNome()), exames, e.getHashPdf());
    }

    // ── PDF ────────────────────────────────────────────────────────────────────────────────────────

    private byte[] gerarPdf(PedidoExame p, List<ItemPedidoExame> itens, int versao, String codigo, Instant emitidoEm) {
        ByteArrayOutputStream saida = new ByteArrayOutputStream();
        Document doc = new Document(PageSize.A4, 40, 40, 40, 110);
        PdfWriter writer = PdfWriter.getInstance(doc, saida);
        String endereco = enderecoDeVerificacao(codigo);
        writer.setPageEvent(new com.lowagie.text.pdf.PdfPageEventHelper() {
            @Override
            public void onEndPage(PdfWriter w, Document d) {
                rodape(w, d, codigo, endereco, versao, emitidoEm);
            }
        });
        doc.addTitle("Laudo de exames");
        doc.addCreator("Mais Saúde Pública");
        doc.open();
        doc.add(new Paragraph("Laudo de exames", TITULO));
        doc.add(new Paragraph(p.getUnidadeSolicitante().getNome(), NORMAL));
        doc.add(Chunk.NEWLINE);

        var paciente = p.getPaciente();
        PdfPTable cab = new PdfPTable(new float[]{1.2f, 2.8f, 1.2f, 2.8f});
        cab.setWidthPercentage(100);
        celula(cab, "Paciente", NEGRITO);
        celula(cab, paciente.getNome(), NORMAL);
        celula(cab, "Nascimento", NEGRITO);
        celula(cab, paciente.getDataNascimento() != null ? DATA.format(paciente.getDataNascimento()) : "—", NORMAL);
        celula(cab, "CNS", NEGRITO);
        celula(cab, paciente.getCartaoSus() != null ? paciente.getCartaoSus() : "—", NORMAL);
        celula(cab, "Sexo", NEGRITO);
        celula(cab, paciente.getSexo() != null ? paciente.getSexo().name().substring(0, 1) + paciente.getSexo().name().substring(1).toLowerCase(Locale.ROOT) : "—", NORMAL);
        celula(cab, "Pedido por", NEGRITO);
        celula(cab, p.getProfissionalSolicitante().getNome(), NORMAL);
        celula(cab, "Pedido em", NEGRITO);
        celula(cab, DATA_HORA.format(p.getSolicitadoEm()), NORMAL);
        doc.add(cab);
        doc.add(Chunk.NEWLINE);

        PdfPTable tabela = new PdfPTable(new float[]{3f, 2.6f, 2.4f});
        tabela.setWidthPercentage(100);
        tabela.setHeaderRows(1);
        for (String t : List.of("Exame", "Resultado", "Referência")) {
            PdfPCell h = new PdfPCell(new Phrase(t, NEGRITO));
            h.setBackgroundColor(new Color(236, 240, 245));
            h.setPadding(5);
            tabela.addCell(h);
        }
        for (ItemPedidoExame i : itens) {
            ResultadoExame r = i.getResultadoAtual();
            boolean imagem = i.getExame().getCategoria() == CategoriaExame.IMAGEM;
            PdfPCell exame = new PdfPCell();
            exame.setPadding(5);
            exame.addElement(new Phrase(i.getExame().getNome(), NEGRITO));
            exame.addElement(new Phrase(imagem ? "Exame de imagem" : "Material: " + (i.getExame().getMaterial() != null
                    ? i.getExame().getMaterial().name().toLowerCase(Locale.ROOT) : "—"), PEQUENA));
            tabela.addCell(exame);
            PdfPCell valor = new PdfPCell();
            valor.setPadding(5);
            valor.addElement(new Phrase(imagem ? r.getLaudoConclusao() : valorDe(r), NEGRITO));
            if (r.getInterpretacao() != null) {
                valor.addElement(new Phrase(rotulo(r.getInterpretacao().name()), PEQUENA));
            }
            tabela.addCell(valor);
            PdfPCell ref = new PdfPCell(new Phrase(imagem ? "—" : referencia(r), NORMAL));
            ref.setPadding(5);
            tabela.addCell(ref);
            if (imagem) {
                PdfPCell laudo = new PdfPCell();
                laudo.setColspan(3);
                laudo.setPadding(5);
                if (r.getLaudoTecnica() != null) {
                    laudo.addElement(new Phrase("Técnica: " + r.getLaudoTecnica(), NORMAL));
                }
                laudo.addElement(new Phrase("Achados: " + r.getLaudoAchados(), NORMAL));
                tabela.addCell(laudo);
            }
            PdfPCell assinatura = new PdfPCell(new Phrase("Assinado eletronicamente por " + r.getLiberadoPor().getNome() + " (matrícula "
                    + r.getLiberadoPor().getMatricula() + ") em " + DATA_HORA.format(r.getLiberadoEm())
                    + (r.getRetificacaoDe() != null ? ". " + (imagem ? "Adendo" : "Retificação") + ": " + r.getMotivoRetificacao() : "")
                    + (r.getHashConteudo() != null ? ". Hash do conteúdo: " + r.getHashConteudo().substring(0, 16) + "…" : ""), PEQUENA));
            assinatura.setColspan(3);
            assinatura.setPadding(4);
            assinatura.setBorderWidthTop(0);
            tabela.addCell(assinatura);
        }
        doc.add(tabela);
        doc.close();
        return saida.toByteArray();
    }

    private void rodape(PdfWriter w, Document d, String codigo, String endereco, int versao, Instant emitidoEm) {
        PdfContentByte cb = w.getDirectContent();
        float tamanho = 70;
        float x = d.right() - tamanho;
        float y = d.bottom() - tamanho - 15;
        desenharQr(cb, endereco, x, y, tamanho);
        PdfPTable t = new PdfPTable(1);
        t.setTotalWidth(d.right() - d.left() - tamanho - 10);
        PdfPCell c = new PdfPCell();
        c.setBorder(0);
        c.addElement(new Phrase("Código de verificação: " + codigo + "  ·  versão " + versao, NEGRITO));
        c.addElement(new Phrase("Confira a autenticidade em " + endereco, PEQUENA));
        c.addElement(new Phrase("Emitido em " + DATA_HORA.format(emitidoEm) + ". Assinaturas eletrônicas internas (Lei 14.063/2020): "
                + "cada resultado foi liberado por profissional autenticado, com o hash do conteúdo registrado.", PEQUENA));
        t.addCell(c);
        t.writeSelectedRows(0, -1, d.left(), d.bottom() - 10, cb);
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
            throw new IllegalStateException("Não foi possível gerar o QR code do laudo.", e);
        }
    }

    private String enderecoDeVerificacao(String codigo) {
        String base = frontendUrl == null || frontendUrl.isBlank() ? "" : frontendUrl.replaceAll("/+$", "");
        return base + "/verificar-laudo?codigo=" + codigo;
    }

    private String novoCodigo() {
        for (int tentativa = 0; tentativa < 20; tentativa++) {
            StringBuilder sb = new StringBuilder();
            for (int k = 0; k < 12; k++) {
                if (k > 0 && k % 4 == 0) {
                    sb.append('-');
                }
                sb.append(LETRAS.charAt(SORTEIO.nextInt(LETRAS.length())));
            }
            if (!emissaoRepository.existsByCodigo(sb.toString())) {
                return sb.toString();
            }
        }
        throw new IllegalStateException("Não foi possível gerar um código de verificação livre.");
    }

    private static void celula(PdfPTable t, String texto, Font fonte) {
        PdfPCell c = new PdfPCell(new Phrase(texto, fonte));
        c.setBorder(0);
        c.setPadding(2);
        c.setVerticalAlignment(Element.ALIGN_MIDDLE);
        t.addCell(c);
    }

    private static String valorDe(ResultadoExame r) {
        if (r.getValorTexto() != null) {
            return r.getValorTexto();
        }
        return numero(r.getValorNumerico()) + (r.getUnidadeMedida() != null ? " " + r.getUnidadeMedida() : "");
    }

    private static String referencia(ResultadoExame r) {
        if (r.getReferenciaTexto() != null) {
            return r.getReferenciaTexto();
        }
        String un = r.getUnidadeMedida() != null ? " " + r.getUnidadeMedida() : "";
        String faixa;
        if (r.getReferenciaMinima() != null && r.getReferenciaMaxima() != null) {
            faixa = numero(r.getReferenciaMinima()) + " a " + numero(r.getReferenciaMaxima()) + un;
        } else if (r.getReferenciaMinima() != null) {
            faixa = "≥ " + numero(r.getReferenciaMinima()) + un;
        } else if (r.getReferenciaMaxima() != null) {
            faixa = "≤ " + numero(r.getReferenciaMaxima()) + un;
        } else {
            faixa = "—";
        }
        return r.getFaixaDescricao() != null ? faixa + " (" + r.getFaixaDescricao() + ")" : faixa;
    }

    private static String numero(BigDecimal v) {
        return v == null ? "" : v.stripTrailingZeros().toPlainString().replace('.', ',');
    }

    private static String rotulo(String interpretacao) {
        return switch (interpretacao) {
            case "NORMAL" -> "Dentro da referência / sem alterações";
            case "ACIMA" -> "Acima da referência";
            case "ABAIXO" -> "Abaixo da referência";
            case "CRITICO_ALTO" -> "Crítico alto";
            case "CRITICO_BAIXO" -> "Crítico baixo";
            case "ALTERADO" -> "Com alterações";
            case "ACHADO_CRITICO" -> "Achado crítico";
            default -> interpretacao;
        };
    }

    /** "Maria da Silva Souza" vira "M. S. S."; sem o nome, nada. */
    static String iniciais(String nome) {
        if (nome == null || nome.isBlank()) {
            return "";
        }
        return java.util.Arrays.stream(nome.trim().split("\\s+"))
                .filter(p -> p.length() > 2 || !Set.of("da", "de", "do", "das", "dos", "e").contains(p.toLowerCase(Locale.ROOT)))
                .map(p -> p.substring(0, 1).toUpperCase(Locale.ROOT) + ".")
                .collect(Collectors.joining(" "));
    }
}
