package com.edufelizardo.maissaudepublica.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Leitura que entra na trilha de auditoria (ADR-0070): o detalhe de um dado de saúde ou pessoal —
 * prontuário, atendimento, registro clínico, paciente, dispensação. Listagens não entram, para a trilha
 * dizer quem abriu o quê, sem o ruído de cada tela carregada.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AuditarLeitura {
}
