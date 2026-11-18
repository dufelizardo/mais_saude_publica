package com.edufelizardo.maissaudepublica.config;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import javax.sql.DataSource;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * A resiliência do deploy, R1 (ADR-0209): a trava das tarefas agendadas deixa uma réplica só rodar até a trava vencer; a trava
 * das rotinas de subida segura as outras réplicas; e a API aceita o banco com uma migração que ela ainda não conhece (a versão
 * anterior voltando a subir depois de uma versão nova ter migrado), com a regra explícita na configuração.
 */
@SpringBootTest
@ActiveProfiles("test")
class ResilienciaDoDeployTest {

    private static final String PREFIXO = "teste-r1-";
    private static final String FUTURA = "9999";

    @Autowired private JdbcTemplate jdbc;
    @Autowired private TravaDeAgendamento travaDeAgendamento;
    @Autowired private TravaDeArranque travaDeArranque;
    @Autowired private Flyway flyway;
    @Autowired private DataSource dataSource;

    @AfterEach
    void limpar() {
        jdbc.update("delete from tb_trava_agendamento where nome like ?", PREFIXO + "%");
        jdbc.update("delete from flyway_schema_history where version = ?", FUTURA);
    }

    @Test
    void umaReplicaPorVezAteATravaVencer() {
        Instant agora = Instant.now();
        Duration dez = Duration.ofMinutes(10);
        assertThat(travaDeAgendamento.tentar(PREFIXO + "a", dez, agora)).isTrue();
        // A outra réplica, no mesmo horário ou cinco minutos depois, pula.
        assertThat(travaDeAgendamento.tentar(PREFIXO + "a", dez, agora)).isFalse();
        assertThat(travaDeAgendamento.tentar(PREFIXO + "a", dez, agora.plus(Duration.ofMinutes(5)))).isFalse();
        // Vencida a trava, a próxima execução roda.
        assertThat(travaDeAgendamento.tentar(PREFIXO + "a", dez, agora.plus(Duration.ofMinutes(11)))).isTrue();
        // Tarefas diferentes não se bloqueiam.
        assertThat(travaDeAgendamento.tentar(PREFIXO + "b", dez, agora)).isTrue();

        AtomicInteger execucoes = new AtomicInteger();
        assertThat(travaDeAgendamento.executar(PREFIXO + "c", dez, execucoes::incrementAndGet)).isTrue();
        assertThat(travaDeAgendamento.executar(PREFIXO + "c", dez, execucoes::incrementAndGet)).isFalse();
        assertThat(execucoes).hasValue(1);
    }

    @Test
    void aTravaDeSubidaSeguraAOutraReplica() throws Exception {
        String tentar = "select pg_try_advisory_lock(" + TravaDeArranque.CHAVE + ")";
        String soltar = "select pg_advisory_unlock(" + TravaDeArranque.CHAVE + ")";
        travaDeArranque.pegar();
        try (var outra = dataSource.getConnection(); var s = outra.createStatement()) {
            var r = s.executeQuery(tentar);
            r.next();
            assertThat(r.getBoolean(1)).as("outra réplica não pega a trava enquanto a primeira sobe").isFalse();
            travaDeArranque.soltar();
            r = s.executeQuery(tentar);
            r.next();
            assertThat(r.getBoolean(1)).as("solta, a outra réplica segue").isTrue();
            s.execute(soltar);
        } finally {
            travaDeArranque.soltar();
        }
    }

    @Test
    void aVersaoAnteriorAceitaOBancoAFrente() {
        Integer rank = jdbc.queryForObject("select coalesce(max(installed_rank), 0) + 1 from flyway_schema_history", Integer.class);
        jdbc.update("insert into flyway_schema_history (installed_rank, version, description, type, script, checksum, installed_by, installed_on, "
                + "execution_time, success) values (?, ?, 'futura', 'SQL', 'V9999__futura.sql', 0, 'teste', now(), 0, true)", rank, FUTURA);
        // Com a configuração da aplicação (explícita, para não depender do padrão do Flyway), a migração que ela não conhece é
        // ignorada e a versão anterior sobe.
        assertThat(flyway.getConfiguration().getIgnoreMigrationPatterns()).extracting(Object::toString).contains("*:future");
        flyway.validate();
    }
}
