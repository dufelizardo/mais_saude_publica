package com.edufelizardo.maissaudepublica.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * As rotinas de subida (o administrador inicial e os ajustes da Farmácia) rodam uma réplica de cada vez (ADR-0209). Elas já
 * são idempotentes, mas duas réplicas subindo juntas poderiam lançar o mesmo ajuste duas vezes. {@link Pegar} roda antes de
 * todas e pega uma trava consultiva do Postgres numa conexão própria; {@link Soltar} roda depois de todas e a solta. A
 * segunda réplica espera e, quando roda, encontra o trabalho feito. O Flyway já tem a trava dele.
 */
@Component
public class TravaDeArranque {

    /** Chave da trava consultiva (pg_advisory_lock) das rotinas de subida. */
    static final long CHAVE = 7_270_209L;
    private static final Logger log = LoggerFactory.getLogger(TravaDeArranque.class);

    @Autowired
    private DataSource dataSource;

    private Connection conexao;

    synchronized void pegar() throws SQLException {
        if (conexao != null) {
            return;
        }
        conexao = dataSource.getConnection();
        try (Statement s = conexao.createStatement()) {
            s.execute("select pg_advisory_lock(" + CHAVE + ")");
        }
        log.info("Trava das rotinas de subida obtida.");
    }

    synchronized void soltar() throws SQLException {
        if (conexao == null) {
            return;
        }
        try (Statement s = conexao.createStatement()) {
            s.execute("select pg_advisory_unlock(" + CHAVE + ")");
        } finally {
            conexao.close();
            conexao = null;
        }
        log.info("Trava das rotinas de subida solta.");
    }

    /** Antes de todas as rotinas de subida. */
    @Component
    static class Pegar implements ApplicationRunner, Ordered {
        @Autowired
        private TravaDeArranque trava;

        @Override
        public void run(ApplicationArguments args) throws SQLException {
            trava.pegar();
        }

        @Override
        public int getOrder() {
            return Ordered.HIGHEST_PRECEDENCE;
        }
    }

    /** Depois de todas as rotinas de subida. */
    @Component
    static class Soltar implements ApplicationRunner, Ordered {
        @Autowired
        private TravaDeArranque trava;

        @Override
        public void run(ApplicationArguments args) throws SQLException {
            trava.soltar();
        }

        @Override
        public int getOrder() {
            return Ordered.LOWEST_PRECEDENCE;
        }
    }
}
