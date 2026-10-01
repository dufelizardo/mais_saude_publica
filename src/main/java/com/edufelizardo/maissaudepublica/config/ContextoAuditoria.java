package com.edufelizardo.maissaudepublica.config;

import com.edufelizardo.maissaudepublica.models.UnidadeDeSaude;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import java.util.UUID;

/**
 * O que os serviços sabem sobre a requisição e a trilha de auditoria precisa (ADR-0070): unidade,
 * paciente e registro afetados, e — no login — o CPF tentado. Vive nos atributos da requisição; fora de
 * uma requisição HTTP (ex.: rotinas de subida), não faz nada.
 */
public final class ContextoAuditoria {

    static final String UNIDADE = "auditoria.unidade";
    static final String PACIENTE = "auditoria.paciente";
    static final String REGISTRO = "auditoria.registro";
    static final String USUARIO = "auditoria.usuario";
    static final String DETALHE = "auditoria.detalhe";

    private ContextoAuditoria() {
    }

    public static void unidade(UnidadeDeSaude unidade) {
        if (unidade != null) {
            guardar(UNIDADE, unidade.getUuid());
        }
    }

    public static void paciente(UUID pacienteId) {
        guardar(PACIENTE, pacienteId);
    }

    public static void registro(UUID registroId) {
        guardar(REGISTRO, registroId);
    }

    public static void usuario(String cpf) {
        guardar(USUARIO, cpf);
    }

    public static void detalhe(String detalhe) {
        guardar(DETALHE, detalhe);
    }

    private static void guardar(String chave, Object valor) {
        RequestAttributes atributos = RequestContextHolder.getRequestAttributes();
        if (atributos != null && valor != null) {
            atributos.setAttribute(chave, valor, RequestAttributes.SCOPE_REQUEST);
        }
    }
}
