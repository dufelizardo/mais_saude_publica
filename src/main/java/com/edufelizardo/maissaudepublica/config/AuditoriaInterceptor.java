package com.edufelizardo.maissaudepublica.config;

import com.edufelizardo.maissaudepublica.models.EventoAuditoria;
import com.edufelizardo.maissaudepublica.models.enuns.AcaoAuditoria;
import com.edufelizardo.maissaudepublica.models.enuns.ResultadoAuditoria;
import com.edufelizardo.maissaudepublica.services.version1.AuditoriaService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Um evento de auditoria por requisição relevante (ADR-0070), gravado depois que a resposta está
 * decidida. Entram: alterações bem-sucedidas (POST/PATCH/PUT/DELETE), leituras marcadas com
 * {@link AuditarLeitura}, tudo o que foi recusado com 403, e o login (aceito ou recusado). Ficam de fora
 * listagens e erros de validação (400/404/409/422) — não são ações sobre dado nem tentativas de acesso.
 *
 * <p>Registrado antes do {@link AutorizacaoInterceptor}, para ver também as recusas feitas por ele.
 */
@Component
public class AuditoriaInterceptor implements HandlerInterceptor {

    private static final String LOGIN = "/api/v1/auth/login";
    private static final String SENHA = "/api/v1/auth/senha";

    @Autowired
    private AuditoriaService auditoriaService;

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        if (!(handler instanceof HandlerMethod metodo)) {
            return;
        }
        String rota = padraoDaRota(request);
        int status = response.getStatus();
        AcaoAuditoria acao = acao(request.getMethod(), rota, metodo);
        if (acao == null) {
            // Leitura comum (listagem) só entra se foi recusada: tentativa de acesso conta.
            if (status != 403) {
                return;
            }
            acao = AcaoAuditoria.LEITURA;
        }
        boolean negado = status == 403 || (acao == AcaoAuditoria.LOGIN && status == 401);
        boolean sucesso = status >= 200 && status < 300;
        if (!negado && !sucesso) {
            return;
        }

        EventoAuditoria e = new EventoAuditoria();
        e.setOcorridoEm(Instant.now());
        String cpf = UsuarioAutenticado.cpf();
        e.setUsuarioCpf(cpf != null ? cpf : (String) request.getAttribute(ContextoAuditoria.USUARIO));
        e.setAcao(acao);
        e.setResultado(negado ? ResultadoAuditoria.NEGADO : ResultadoAuditoria.PERMITIDO);
        e.setRecurso(recurso(metodo));
        e.setMetodo(request.getMethod());
        e.setRota(rota);
        e.setStatusHttp(status);
        e.setUnidadeId((UUID) request.getAttribute(ContextoAuditoria.UNIDADE));
        e.setPacienteId(paciente(request));
        e.setRegistroId(registro(request));
        e.setOrigemIp(origem(request));
        Object detalhe = request.getAttribute(ContextoAuditoria.DETALHE);
        if (detalhe != null) {
            String texto = detalhe.toString();
            e.setDetalhe(texto.length() > 500 ? texto.substring(0, 500) : texto);
        }
        auditoriaService.registrarSemFalhar(e);
    }

    private static AcaoAuditoria acao(String metodoHttp, String rota, HandlerMethod metodo) {
        if (LOGIN.equals(rota)) return AcaoAuditoria.LOGIN;
        if (SENHA.equals(rota)) return AcaoAuditoria.TROCA_DE_SENHA;
        switch (metodoHttp) {
            case "GET":
                return metodo.hasMethodAnnotation(AuditarLeitura.class) ? AcaoAuditoria.LEITURA : null;
            case "DELETE":
                return AcaoAuditoria.EXCLUSAO;
            case "PATCH":
            case "PUT":
                return AcaoAuditoria.ALTERACAO;
            case "POST":
                if (rota.endsWith("/retificacao")) return AcaoAuditoria.RETIFICACAO;
                if (rota.endsWith("/revogacao")) return AcaoAuditoria.REVOGACAO;
                // POST na coleção cria; POST numa ação do registro (recebimento, desbloqueio…) altera.
                return rota.contains("{") ? AcaoAuditoria.ALTERACAO : AcaoAuditoria.CRIACAO;
            default:
                return null;
        }
    }

    private static String padraoDaRota(HttpServletRequest request) {
        Object padrao = request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
        String rota = padrao != null ? padrao.toString() : request.getRequestURI();
        return rota.length() > 1 && rota.endsWith("/") ? rota.substring(0, rota.length() - 1) : rota;
    }

    /** TriagemController → TRIAGEM; EvolucaoEnfermagemController → EVOLUCAO_ENFERMAGEM. */
    private static String recurso(HandlerMethod metodo) {
        String nome = metodo.getBeanType().getSimpleName().replaceFirst("Controller$", "");
        return nome.replaceAll("([a-z0-9])([A-Z])", "$1_$2").toUpperCase();
    }

    private static UUID paciente(HttpServletRequest request) {
        Object doServico = request.getAttribute(ContextoAuditoria.PACIENTE);
        if (doServico != null) return (UUID) doServico;
        return comoUuid(variaveis(request).get("pacienteId"));
    }

    /** O registro informado pelo serviço; senão, o id da rota (uuid, ou a primeira variável que for um UUID). */
    private static UUID registro(HttpServletRequest request) {
        Object doServico = request.getAttribute(ContextoAuditoria.REGISTRO);
        if (doServico != null) return (UUID) doServico;
        Map<String, String> variaveis = variaveis(request);
        UUID uuid = comoUuid(variaveis.get("uuid"));
        if (uuid != null) return uuid;
        return variaveis.values().stream().map(AuditoriaInterceptor::comoUuid).filter(v -> v != null).findFirst().orElse(null);
    }

    @SuppressWarnings("unchecked")
    private static Map<String, String> variaveis(HttpServletRequest request) {
        Object v = request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
        return v instanceof Map<?, ?> m ? (Map<String, String>) m : Map.of();
    }

    private static UUID comoUuid(String valor) {
        if (valor == null) return null;
        try {
            return UUID.fromString(valor);
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    /** Primeiro endereço do X-Forwarded-For (atrás do ingress), senão o endereço da conexão. */
    private static String origem(HttpServletRequest request) {
        String encaminhado = request.getHeader("X-Forwarded-For");
        if (encaminhado != null && !encaminhado.isBlank()) {
            return encaminhado.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
