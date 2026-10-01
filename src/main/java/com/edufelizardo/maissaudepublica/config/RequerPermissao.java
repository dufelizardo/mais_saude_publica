package com.edufelizardo.maissaudepublica.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Permissões que liberam a rota — basta uma delas, em qualquer escopo (ADR-0067). O escopo da unidade
 * de cada registro é conferido no serviço. Na rota vale a anotação do método; sem ela, a da classe.
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequerPermissao {
    String[] value();
}
