package com.edufelizardo.maissaudepublica.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Banco criado antes de um valor novo do enum: a restrição CHECK antiga recusa o valor; a rotina de subida
 * amplia a restrição e a gravação volta a funcionar.
 */
@SpringBootTest
@ActiveProfiles("test")
class AtualizacaoRestricoesDeEnumTest {

    private static final String INSERE = """
            insert into tb_evento_auditoria (uuid, ocorrido_em, acao, resultado, recurso, metodo, rota, status_http)
            values (?, now(), 'ACESSO_JUSTIFICADO', 'PERMITIDO', 'TESTE_ENUM', 'POST', '/teste', 201)
            """;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private AtualizacaoRestricoesDeEnum atualizacao;

    /** Nome da restrição da coluna acao; o padrão do Hibernate se ela não existir. */
    private String restricaoDaAcao() {
        return jdbcTemplate.queryForList("""
                select conname from pg_constraint
                where conrelid = 'tb_evento_auditoria'::regclass and contype = 'c'
                  and pg_get_constraintdef(oid) like '%acao%'
                """, String.class).stream().findFirst().orElse("tb_evento_auditoria_acao_check");
    }

    @Test
    void ampliaARestricaoAntigaComOsValoresNovosDoEnum() {
        String nome = restricaoDaAcao();
        // Como num banco criado antes da ADR-0076: sem ACESSO_JUSTIFICADO.
        jdbcTemplate.execute("alter table tb_evento_auditoria drop constraint if exists \"" + nome + "\"");
        jdbcTemplate.execute("alter table tb_evento_auditoria add constraint \"" + nome + "\" check (acao in "
                + "('LOGIN', 'TROCA_DE_SENHA', 'LEITURA', 'CRIACAO', 'ALTERACAO', 'RETIFICACAO', 'REVOGACAO', 'EXCLUSAO')) not valid");
        UUID id = UUID.randomUUID();
        try {
            assertThatThrownBy(() -> jdbcTemplate.update(INSERE, id)).isInstanceOf(DataIntegrityViolationException.class);

            atualizacao.run(null);

            jdbcTemplate.update(INSERE, id);
            assertThat(jdbcTemplate.queryForObject("select count(*) from tb_evento_auditoria where uuid = ?", Integer.class, id))
                    .isEqualTo(1);
            // Idempotente: rodar de novo não muda nada nem falha.
            atualizacao.run(null);
            assertThat(restricaoDaAcao()).isEqualTo(nome);
        } finally {
            jdbcTemplate.update("delete from tb_evento_auditoria where uuid = ?", id);
        }
    }
}
