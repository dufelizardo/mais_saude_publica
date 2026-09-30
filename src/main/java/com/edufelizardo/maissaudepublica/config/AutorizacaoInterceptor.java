package com.edufelizardo.maissaudepublica.config;

import com.edufelizardo.maissaudepublica.exceptions.ResourceForbiddenException;
import com.edufelizardo.maissaudepublica.services.version1.ControleDeAcesso;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Confere a permissão da rota antes do controller (ADR-0067). Negado por padrão: rota sem
 * {@link RequerPermissao} nem {@link LiberadoParaAutenticados} responde 403 — um endpoint novo esquecido
 * não fica aberto. Só age com a autorização ligada ({@link ControleDeAcesso#ativo()}).
 */
@Component
public class AutorizacaoInterceptor implements HandlerInterceptor {

    @Autowired
    private ControleDeAcesso controleDeAcesso;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!controleDeAcesso.ativo() || !(handler instanceof HandlerMethod metodo)) {
            return true;
        }
        if (liberado(metodo)) {
            return true;
        }
        RequerPermissao requer = permissoes(metodo);
        if (requer == null) {
            throw new ResourceForbiddenException("Esta rota ainda não tem permissão definida.");
        }
        controleDeAcesso.exigirAlguma(requer.value());
        return true;
    }

    private static boolean liberado(HandlerMethod metodo) {
        if (metodo.hasMethodAnnotation(RequerPermissao.class)) {
            return false;
        }
        return metodo.hasMethodAnnotation(LiberadoParaAutenticados.class)
                || AnnotatedElementUtils.hasAnnotation(metodo.getBeanType(), LiberadoParaAutenticados.class);
    }

    static RequerPermissao permissoes(HandlerMethod metodo) {
        RequerPermissao doMetodo = metodo.getMethodAnnotation(RequerPermissao.class);
        return doMetodo != null ? doMetodo : AnnotatedElementUtils.findMergedAnnotation(metodo.getBeanType(), RequerPermissao.class);
    }
}
