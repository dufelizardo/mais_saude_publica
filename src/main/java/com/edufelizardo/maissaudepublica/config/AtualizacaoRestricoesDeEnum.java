package com.edufelizardo.maissaudepublica.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.annotation.Order;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Mantém as restrições CHECK das colunas de enum em dia com o código.
 *
 * <p>O Hibernate cria, junto com a tabela, um {@code CHECK (coluna IN (...))} com os valores do enum daquele
 * momento. Com {@code ddl-auto=update}, ele nunca refaz essa restrição: quando o enum ganha um valor novo, gravar
 * esse valor num banco que já existia falha — e onde o erro é engolido de propósito (a trilha de auditoria,
 * {@code registrarSemFalhar}) o registro some em silêncio. Foi o que aconteceu com {@code ACESSO_JUSTIFICADO}
 * (ADR-0076) nos ambientes com banco anterior. Em banco novo (CI) não aparece.
 *
 * <p>Na subida, antes de qualquer outra rotina, cada restrição de enum do Postgres é comparada com os enums do
 * código: se exatamente um enum contém todos os valores antigos e tem valores a mais, a restrição é refeita com a
 * lista atual. Só amplia — valor removido ou renomeado no código fica como está e vai para o log, para decisão
 * manual. Idempotente.
 */
@Component
@Order(-10)
@Slf4j
public class AtualizacaoRestricoesDeEnum implements ApplicationRunner {

    private static final String PACOTE = "com.edufelizardo.maissaudepublica";
    /** CHECK (((acao)::text = ANY ((ARRAY['LOGIN'::character varying, ...])::text[]))) */
    private static final Pattern COLUNA = Pattern.compile("^CHECK \\(+\"?(\\w+)\"?\\)?::text = ANY");
    private static final Pattern VALOR = Pattern.compile("'([^']*)'::");

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public void run(ApplicationArguments args) {
        String banco = jdbcTemplate.execute((java.sql.Connection c) -> c.getMetaData().getDatabaseProductName());
        if (banco == null || !banco.toLowerCase().contains("postgres")) {
            return;
        }
        List<Set<String>> enums = enumsDoCodigo();
        List<Map<String, Object>> restricoes = jdbcTemplate.queryForList("""
                select c.conrelid::regclass::text as tabela, c.conname as nome, pg_get_constraintdef(c.oid) as definicao
                from pg_constraint c join pg_namespace n on n.oid = c.connamespace
                where c.contype = 'c' and n.nspname = current_schema()
                  and pg_get_constraintdef(c.oid) like '%= ANY (%ARRAY[%'
                """);
        int atualizadas = 0;
        for (Map<String, Object> r : restricoes) {
            if (atualizar((String) r.get("tabela"), (String) r.get("nome"), (String) r.get("definicao"), enums)) {
                atualizadas++;
            }
        }
        if (atualizadas > 0) {
            log.info("Restrições de enum atualizadas com os valores atuais do código: {}.", atualizadas);
        }
    }

    private boolean atualizar(String tabela, String nome, String definicao, List<Set<String>> enums) {
        Matcher coluna = COLUNA.matcher(definicao);
        if (!coluna.find()) {
            return false;
        }
        Set<String> atuais = new LinkedHashSet<>();
        Matcher valor = VALOR.matcher(definicao);
        while (valor.find()) {
            atuais.add(valor.group(1));
        }
        if (atuais.isEmpty()) {
            return false;
        }
        Set<Set<String>> candidatos = enums.stream().filter(e -> e.containsAll(atuais)).collect(Collectors.toSet());
        if (candidatos.isEmpty()) {
            log.warn("Restrição {} em {}: nenhum enum do código contém {} — conferir à mão.", nome, tabela, atuais);
            return false;
        }
        if (candidatos.stream().anyMatch(e -> e.size() == atuais.size())) {
            return false; // já está em dia
        }
        if (candidatos.size() > 1) {
            log.warn("Restrição {} em {}: mais de um enum serve para {} — conferir à mão.", nome, tabela, atuais);
            return false;
        }
        Set<String> novos = candidatos.iterator().next();
        String lista = novos.stream().map(v -> "'" + v.replace("'", "''") + "'").collect(Collectors.joining(", "));
        try {
            jdbcTemplate.execute("alter table " + tabela + " drop constraint \"" + nome + "\"");
            jdbcTemplate.execute("alter table " + tabela + " add constraint \"" + nome + "\" check (\"" + coluna.group(1)
                    + "\" in (" + lista + "))");
            log.info("Restrição {} em {}.{} ampliada para {}.", nome, tabela, coluna.group(1), novos);
            return true;
        } catch (RuntimeException ex) {
            log.warn("Não foi possível atualizar a restrição {} em {}: {}", nome, tabela, ex.getMessage());
            return false;
        }
    }

    /** Os valores de cada enum do projeto. */
    private static List<Set<String>> enumsDoCodigo() {
        ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AssignableTypeFilter(Enum.class));
        List<Set<String>> enums = new ArrayList<>();
        for (var definicao : scanner.findCandidateComponents(PACOTE)) {
            try {
                Class<?> classe = Class.forName(definicao.getBeanClassName());
                if (classe.isEnum()) {
                    enums.add(Arrays.stream(classe.getEnumConstants()).map(c -> ((Enum<?>) c).name())
                            .collect(Collectors.toCollection(LinkedHashSet::new)));
                }
            } catch (ClassNotFoundException ex) {
                log.debug("Enum não carregado: {}", definicao.getBeanClassName());
            }
        }
        return enums;
    }
}
