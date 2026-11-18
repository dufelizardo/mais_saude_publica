package com.edufelizardo.maissaudepublica.infra;

import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * O Postgres se reinicia sozinho quando trava (ADR-0210): o manifesto tem startupProbe e livenessProbe com {@code pg_isready},
 * e a janela de subida é maior que a de vida, para uma recuperação de verdade não ser interrompida.
 */
class ManifestoPostgresTest {

    @Test
    @SuppressWarnings("unchecked")
    void postgresTemLivenessEStartupComPgIsready() throws IOException {
        Map<String, Object> deployment = new Yaml().load(Files.readString(Path.of("k8s/base/deployment-postgres.yaml")));
        Map<String, Object> spec = (Map<String, Object>) ((Map<String, Object>) ((Map<String, Object>) deployment.get("spec"))
                .get("template")).get("spec");
        Map<String, Object> container = ((List<Map<String, Object>>) spec.get("containers")).get(0);

        Map<String, Object> startup = (Map<String, Object>) container.get("startupProbe");
        Map<String, Object> liveness = (Map<String, Object>) container.get("livenessProbe");
        assertThat(startup).as("startupProbe do Postgres").isNotNull();
        assertThat(liveness).as("livenessProbe do Postgres").isNotNull();
        for (Map<String, Object> probe : List.of(startup, liveness, (Map<String, Object>) container.get("readinessProbe"))) {
            assertThat((List<String>) ((Map<String, Object>) probe.get("exec")).get("command")).first().isEqualTo("pg_isready");
        }

        int janelaSubida = (int) startup.get("periodSeconds") * (int) startup.get("failureThreshold");
        int janelaVida = (int) liveness.get("periodSeconds") * (int) liveness.get("failureThreshold");
        assertThat(janelaSubida).as("subida em segundos").isGreaterThanOrEqualTo(600);
        assertThat(janelaVida).as("travado em segundos até reiniciar").isBetween(60, 300);
    }
}
