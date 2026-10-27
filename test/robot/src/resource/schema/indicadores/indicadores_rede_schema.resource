*** Comments ***
    Mais Saúde Pública API - Indicadores e BI, fatia BI3: indicadores da rede (ADR-0146).
*** Variables ***
${PROGRAMAS_NA_REDE_SCHEMA}    ${{{'required': ['de', 'ate', 'abrangencia', 'sensiveisOcultos', 'programas'], 'types': {'de': str, 'ate': str, 'unidadeId': str, 'abrangencia': str, 'sensiveisOcultos': int, 'programas': list}}}}
${PROGRAMA_POR_UNIDADE_SCHEMA}    ${{{'required': ['programaId', 'nome', 'de', 'ate', 'unidades'], 'types': {'programaId': str, 'nome': str, 'de': str, 'ate': str, 'unidades': list}}}}
${VIGILANCIA_NA_REDE_SCHEMA}    ${{{'required': ['de', 'ate', 'abrangencia', 'notificadas', 'confirmadas', 'descartadas', 'emAberto', 'encerradas', 'encerradasNoPrazo', 'sensiveisOcultas', 'agravos', 'semanas', 'regularidade', 'territorio'], 'types': {'de': str, 'ate': str, 'unidadeId': str, 'abrangencia': str, 'notificadas': int, 'confirmadas': int, 'descartadas': int, 'emAberto': int, 'encerradas': int, 'encerradasNoPrazo': int, 'oportunidadeEncerramento': (int, float), 'sensiveisOcultas': int, 'agravos': list, 'semanas': list, 'regularidade': dict, 'territorio': list}}}}
