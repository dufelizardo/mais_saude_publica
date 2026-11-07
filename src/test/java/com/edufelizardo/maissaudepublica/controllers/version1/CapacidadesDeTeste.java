package com.edufelizardo.maissaudepublica.controllers.version1;

import org.springframework.jdbc.core.JdbcTemplate;

import java.util.UUID;

/** Dá à unidade de um teste as capacidades que os domínios consultam (ADR-0170), sem passar pela API de cada um. */
final class CapacidadesDeTeste {

    private CapacidadesDeTeste() {
    }

    static void habilitar(JdbcTemplate jdbc, UUID unidade, String... codigos) {
        for (String codigo : codigos) {
            jdbc.update("insert into tb_unidade_capacidade_assistencial (uuid, unidade_id, capacidade_assistencial_id, inicio) "
                    + "select gen_random_uuid(), ?, uuid, current_date from tb_capacidade_assistencial where codigo = ?", unidade, codigo);
        }
    }

    static void limpar(JdbcTemplate jdbc, String prefixoDaUnidade) {
        jdbc.update("delete from tb_unidade_capacidade_assistencial where unidade_id in (select uuid from tb_unidade_de_saude where nome like ?)",
                prefixoDaUnidade + "%");
    }
}
