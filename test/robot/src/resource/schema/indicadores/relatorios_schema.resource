*** Comments ***
    Mais Saúde Pública API - Relatórios (ADR-0167): catálogo e relatório gerado.
*** Variables ***
${RELATORIO_CATALOGO_SCHEMA}    ${{{'required': ['codigo', 'nome', 'categoria', 'descricao'], 'types': {'codigo': str, 'nome': str, 'categoria': str, 'descricao': str}}}}
${RELATORIO_SCHEMA}    ${{{'required': ['codigo', 'nome', 'categoria', 'de', 'ate', 'abrangencia', 'resumo', 'secoes', 'notas'], 'types': {'codigo': str, 'nome': str, 'categoria': str, 'de': str, 'ate': str, 'abrangencia': str, 'resumo': list, 'secoes': list, 'notas': list}}}}
