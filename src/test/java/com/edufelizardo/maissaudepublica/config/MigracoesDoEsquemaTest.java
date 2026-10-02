package com.edufelizardo.maissaudepublica.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * O esquema é das migrações do Flyway (ADR-0090). O perfil de teste sobe com {@code ddl-auto=validate}, então
 * tabela ou coluna esquecida numa migração já derruba o contexto. O que o Hibernate não valida são as restrições
 * CHECK das colunas de enum: valor novo num enum sem a migração que amplia a restrição só falharia ao gravar — e,
 * na trilha de auditoria, em silêncio (foi o que a ADR-0080 remediava na subida). Este teste pega isso no CI.
 */
@SpringBootTest
@ActiveProfiles("test")
class MigracoesDoEsquemaTest {

    private static final Pattern VALOR = Pattern.compile("'([^']*)'::");

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void asMigracoesForamAplicadasAteAUltimaVersao() {
        List<String> versoes = jdbcTemplate.queryForList(
                "select version from flyway_schema_history where success order by installed_rank", String.class);
        assertThat(versoes).contains("1", "2");
    }

    @Test
    void cadaRestricaoDeEnumTemExatamenteOsValoresDeUmEnumDoCodigo() {
        List<Set<String>> enums = enumsDoCodigo();
        List<Map<String, Object>> restricoes = jdbcTemplate.queryForList("""
                select c.conrelid::regclass::text as tabela, c.conname as nome, pg_get_constraintdef(c.oid) as definicao
                from pg_constraint c join pg_namespace n on n.oid = c.connamespace
                where c.contype = 'c' and n.nspname = current_schema()
                  and pg_get_constraintdef(c.oid) like '%= ANY (%ARRAY[%'
                """);
        assertThat(restricoes).as("restrições de enum no esquema migrado").isNotEmpty();

        List<String> desatualizadas = new ArrayList<>();
        for (Map<String, Object> r : restricoes) {
            Set<String> valores = new LinkedHashSet<>();
            Matcher m = VALOR.matcher((String) r.get("definicao"));
            while (m.find()) {
                valores.add(m.group(1));
            }
            if (enums.stream().noneMatch(e -> e.equals(valores))) {
                desatualizadas.add(r.get("tabela") + "." + r.get("nome") + " " + valores);
            }
        }
        assertThat(desatualizadas)
                .as("restrição CHECK que não bate com nenhum enum: falta uma migração que a refaça com os valores atuais")
                .isEmpty();
    }

    private static List<Set<String>> enumsDoCodigo() {
        ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AssignableTypeFilter(Enum.class));
        List<Set<String>> enums = new ArrayList<>();
        for (var definicao : scanner.findCandidateComponents("com.edufelizardo.maissaudepublica")) {
            try {
                Class<?> classe = Class.forName(definicao.getBeanClassName());
                if (classe.isEnum()) {
                    enums.add(Arrays.stream(classe.getEnumConstants()).map(c -> ((Enum<?>) c).name())
                            .collect(Collectors.toCollection(LinkedHashSet::new)));
                }
            } catch (ClassNotFoundException ex) {
                throw new IllegalStateException(ex);
            }
        }
        return enums;
    }
}
