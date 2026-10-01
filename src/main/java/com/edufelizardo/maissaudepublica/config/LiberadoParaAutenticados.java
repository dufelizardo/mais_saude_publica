package com.edufelizardo.maissaudepublica.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Rota aberta a qualquer usuário autenticado, sem permissão específica (ADR-0067) — ex.: a estrutura
 * de unidades e o quadro de profissionais, que toda tela usa para escolher unidade e responsável.
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface LiberadoParaAutenticados {
}
