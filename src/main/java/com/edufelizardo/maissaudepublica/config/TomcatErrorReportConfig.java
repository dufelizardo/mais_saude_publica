package com.edufelizardo.maissaudepublica.config;

import org.apache.catalina.Container;
import org.apache.catalina.core.StandardHost;
import org.springframework.boot.tomcat.servlet.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Configuration;

/**
 * Registra o JsonErrorReportValve no Host do Tomcat embutido, para que erros tratados pelo
 * container (antes do Spring MVC) também respondam no contrato ErrorExceptionResponse — ver
 * AQUAQE-216.
 */
@Configuration
public class TomcatErrorReportConfig implements WebServerFactoryCustomizer<TomcatServletWebServerFactory> {

    @Override
    public void customize(TomcatServletWebServerFactory factory) {
        factory.addContextCustomizers(context -> {
            Container parent = context.getParent();
            if (parent instanceof StandardHost host) {
                host.setErrorReportValveClass(JsonErrorReportValve.class.getName());
            }
        });
    }
}
