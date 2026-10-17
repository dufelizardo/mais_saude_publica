package com.edufelizardo.maissaudepublica.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Rota aberta sem login (ADR-0118): a consulta pública da landing page, em {@code /api/v1/publico/**}. Só dado agregado
 * ou institucional, nunca dado de paciente. O {@link SecurityConfig} libera o caminho e o {@link AutorizacaoInterceptor}
 * não exige permissão.
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface RotaPublica {
}
