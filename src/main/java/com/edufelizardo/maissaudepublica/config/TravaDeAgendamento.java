package com.edufelizardo.maissaudepublica.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;

/**
 * Trava das tarefas agendadas entre réplicas da API (ADR-0209). Antes de rodar, a tarefa tenta marcar a própria linha em
 * {@code tb_trava_agendamento} até {@code agora + duração}: só uma réplica consegue (o UPDATE é atômico) e as outras pulam
 * aquela execução. A duração cobre a execução mais longa esperada e também impede a mesma tarefa de rodar duas vezes no
 * mesmo horário quando os relógios das réplicas diferem por alguns segundos.
 */
@Component
public class TravaDeAgendamento {

    private static final Logger log = LoggerFactory.getLogger(TravaDeAgendamento.class);

    @Autowired
    private JdbcTemplate jdbc;

    /** Roda a tarefa se esta réplica conseguir a trava; devolve se rodou. */
    public boolean executar(String nome, Duration duracao, Runnable tarefa) {
        if (!tentar(nome, duracao, Instant.now())) {
            log.debug("Tarefa {} já em execução em outra réplica; pulando.", nome);
            return false;
        }
        tarefa.run();
        return true;
    }

    /** Marca a trava se ela estiver livre em {@code agora}; cria a linha na primeira vez. */
    boolean tentar(String nome, Duration duracao, Instant agora) {
        Timestamp ate = Timestamp.from(agora.plus(duracao));
        Timestamp em = Timestamp.from(agora);
        String por = replica();
        int atualizadas = jdbc.update("update tb_trava_agendamento set travada_ate = ?, travada_em = ?, travada_por = ? "
                + "where nome = ? and travada_ate <= ?", ate, em, por, nome, em);
        if (atualizadas == 1) {
            return true;
        }
        // Primeira execução: cria a linha; se outra réplica criou no mesmo instante, a chave primária decide.
        return jdbc.update("insert into tb_trava_agendamento (nome, travada_ate, travada_em, travada_por) values (?, ?, ?, ?) "
                + "on conflict (nome) do nothing", nome, ate, em, por) == 1;
    }

    private static String replica() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (Exception e) {
            return "desconhecida";
        }
    }
}
