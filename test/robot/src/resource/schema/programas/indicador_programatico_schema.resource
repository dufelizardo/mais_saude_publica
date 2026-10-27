*** Comments ***
    Mais Saúde Pública API - Programas, fatia P6: metas e indicadores programáticos (ADR-0141).
*** Variables ***
${INDICADOR_APURADO_SCHEMA}    ${{{'required': ['indicador', 'nome', 'descricao', 'tipo', 'aplicavel', 'numerador', 'de', 'ate'], 'types': {'indicador': str, 'nome': str, 'descricao': str, 'tipo': str, 'aplicavel': bool, 'motivo': str, 'numerador': int, 'denominador': int, 'valor': (int, float), 'de': str, 'ate': str, 'metaId': str, 'alvo': (int, float), 'situacao': str}, 'enums': {'tipo': ['PERCENTUAL', 'QUANTIDADE']}}}}
${META_PROGRAMATICA_SCHEMA}    ${{{'required': ['uuid', 'programaId', 'indicador', 'indicadorNome', 'alvo', 'inicio', 'fim'], 'types': {'uuid': str, 'programaId': str, 'indicador': str, 'indicadorNome': str, 'unidadeId': str, 'unidadeNome': str, 'alvo': (int, float), 'inicio': str, 'fim': str, 'exameId': str, 'exameNome': str, 'limiteMinimo': (int, float), 'limiteMaximo': (int, float)}}}}
${SERIE_INDICADOR_SCHEMA}    ${{{'required': ['indicador', 'nome', 'aplicavel', 'pontos'], 'types': {'indicador': str, 'nome': str, 'aplicavel': bool, 'motivo': str, 'pontos': list}}}}
