package com.edufelizardo.maissaudepublica.services.version1;

import com.edufelizardo.maissaudepublica.config.UsuarioAutenticado;
import com.edufelizardo.maissaudepublica.exceptions.ResourceForbiddenException;
import com.edufelizardo.maissaudepublica.models.Profissional;
import com.edufelizardo.maissaudepublica.models.ResultadoExame;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Assinatura eletrônica interna do resultado (ADR-0183), avançada no sentido da Lei 14.063/2020: o profissional autenticado com a
 * própria senha assina, e o sistema guarda o CPF e o SHA-256 do conteúdo assinado. O certificado ICP-Brasil (assinatura
 * qualificada) é integração do domínio 20.
 */
final class AssinaturaResultado {

    private AssinaturaResultado() {
    }

    /** Com login, quem assina é o próprio profissional logado (403). Sem login (segurança desligada), vale a matrícula informada. */
    static void exigirProprioProfissional(Profissional quem, String acao) {
        String cpf = UsuarioAutenticado.cpf();
        if (cpf != null && (quem.getCpf() == null || !cpf.equals(quem.getCpf()))) {
            throw new ResourceForbiddenException("A " + acao + " é assinada por quem está logado: informe a sua própria matrícula.");
        }
    }

    /** Grava quem assinou e o hash do conteúdo. Chamar depois de o resultado ter id e a liberação preenchida. */
    static void assinar(ResultadoExame r) {
        r.setAssinadoPorCpf(UsuarioAutenticado.cpf());
        r.setHashConteudo(sha256(conteudo(r)));
    }

    /** O texto canônico assinado: exame, paciente, valor ou laudo, referência, interpretação, quem liberou e quando. */
    static String conteudo(ResultadoExame r) {
        var item = r.getItem();
        return Stream.of(
                        "resultado=" + r.getUuid(),
                        "pedido=" + item.getPedido().getUuid(),
                        "item=" + item.getUuid(),
                        "paciente=" + item.getPedido().getPaciente().getUuid(),
                        "exame=" + item.getExame().getNome(),
                        "valorNumerico=" + numero(r.getValorNumerico()),
                        "valorTexto=" + texto(r.getValorTexto()),
                        "unidade=" + texto(r.getUnidadeMedida()),
                        "referencia=" + numero(r.getReferenciaMinima()) + ".." + numero(r.getReferenciaMaxima()) + ";" + texto(r.getReferenciaTexto()),
                        "critico=" + numero(r.getCriticoMinimo()) + ".." + numero(r.getCriticoMaximo()),
                        "interpretacao=" + (r.getInterpretacao() != null ? r.getInterpretacao().name() : ""),
                        "laudo=" + texto(r.getLaudoTecnica()) + "|" + texto(r.getLaudoAchados()) + "|" + texto(r.getLaudoConclusao()),
                        "imagens=" + texto(r.getImagemUrl()),
                        "liberadoPor=" + (r.getLiberadoPor() != null ? r.getLiberadoPor().getMatricula() : ""),
                        "liberadoEm=" + Objects.toString(r.getLiberadoEm(), ""),
                        "retificacaoDe=" + (r.getRetificacaoDe() != null ? r.getRetificacaoDe().getUuid() : ""))
                .collect(Collectors.joining("\n"));
    }

    static String sha256(byte[] dados) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(dados));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 indisponível.", e);
        }
    }

    static String sha256(String texto) {
        return sha256(texto.getBytes(StandardCharsets.UTF_8));
    }

    private static String numero(BigDecimal v) {
        return v == null ? "" : v.stripTrailingZeros().toPlainString();
    }

    private static String texto(String v) {
        return v == null ? "" : v;
    }
}
