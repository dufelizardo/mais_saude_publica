*** Comments ***
    Mais Saúde Pública API - Programas de Saúde (ADR-0118): catálogo, programa com participações e consulta pública.
*** Variables ***
${PROGRAMA_RESUMO_SCHEMA}    ${{{'required': ['uuid', 'codigo', 'nome', 'categoriaId', 'categoriaNome', 'abrangencia', 'situacao', 'periodicidade', 'sensivel', 'gestante', 'condicoes', 'publico', 'unidades'], 'types': {'uuid': str, 'codigo': str, 'nome': str, 'descricao': str, 'categoriaId': str, 'categoriaNome': str, 'abrangencia': str, 'situacao': str, 'periodicidade': str, 'sensivel': bool, 'idadeMinima': int, 'idadeMaxima': int, 'sexo': str, 'gestante': bool, 'condicoes': list, 'publico': str, 'unidades': int}, 'enums': {'abrangencia': ['NACIONAL', 'ESTADUAL', 'MUNICIPAL'], 'situacao': ['ATIVO', 'SUSPENSO', 'ENCERRADO']}}}}
${PROGRAMA_SCHEMA}    ${{{'required': ['resumo', 'participacoes', 'participacoesEncerradas'], 'types': {'resumo': dict, 'participacoes': list, 'participacoesEncerradas': list}}}}
${CATEGORIA_PROGRAMA_SCHEMA}    ${{{'required': ['uuid', 'nome', 'ordem', 'ativa', 'programas'], 'types': {'uuid': str, 'nome': str, 'ordem': int, 'ativa': bool, 'programas': int}}}}
${PROGRAMA_PUBLICO_SCHEMA}    ${{{'required': ['nome', 'categoria', 'publico', 'unidades'], 'types': {'nome': str, 'categoria': str, 'descricao': str, 'publico': str, 'unidades': list}}}}
