package com.edufelizardo.maissaudepublica.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private AuditoriaInterceptor auditoriaInterceptor;

    @Autowired
    private AutorizacaoInterceptor autorizacaoInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // Auditoria primeiro: o afterCompletion dela roda mesmo quando a autorização recusa a rota (ADR-0070).
        registry.addInterceptor(auditoriaInterceptor).addPathPatterns("/api/**");
        registry.addInterceptor(autorizacaoInterceptor).addPathPatterns("/api/**");
    }
}
