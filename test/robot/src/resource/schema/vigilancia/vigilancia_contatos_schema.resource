*** Comments ***
    Mais Saúde Pública API - Vigilância, fatia V2a: contatos, local do caso e mapa (ADR-0130).
*** Variables ***
${CONTATO_SCHEMA}    ${{{'required': ['uuid', 'nome', 'tipoExposicao', 'monitoramentoVencido', 'situacao'], 'types': {'uuid': str, 'nome': str, 'tipoExposicao': str, 'monitoramentoVencido': bool, 'situacao': str}, 'enums': {'tipoExposicao': ['DOMICILIAR', 'SEXUAL', 'TRABALHO', 'ESCOLA', 'SERVICO_SAUDE', 'COMUNITARIO', 'OUTRO'], 'situacao': ['EM_ACOMPANHAMENTO', 'LIBERADO', 'SINTOMATICO', 'VIROU_CASO', 'PERDA_DE_SEGUIMENTO']}}}}
${MAPA_VIGILANCIA_SCHEMA}    ${{{'required': ['de', 'ate', 'total', 'semLocal', 'pontosVisiveis', 'microareas', 'pontos', 'curva'], 'types': {'de': str, 'ate': str, 'total': int, 'semLocal': int, 'pontosVisiveis': bool, 'microareas': list, 'pontos': list, 'curva': list}}}}
