*** Comments ***
    Mais Saúde Pública API - Imunização, fatia I4 (ADR-0150).
*** Variables ***
${NOTIFICACAO_ESAVI_SCHEMA}    ${{{'required': ['uuid', 'agravoCodigo', 'origem', 'situacao', 'doses'], 'types': {'uuid': str, 'agravoCodigo': str, 'origem': str, 'situacao': str, 'gravidadeEsavi': str, 'causalidadeEsavi': str, 'condutaEsavi': str, 'doses': list}, 'enums': {'origem': ['VACINACAO'], 'gravidadeEsavi': ['NAO_GRAVE', 'GRAVE', None], 'causalidadeEsavi': ['A1_REACAO_PRODUTO', 'A2_DESVIO_QUALIDADE', 'A3_ERRO_IMUNIZACAO', 'A4_ANSIEDADE', 'B_INDETERMINADO', 'C_COINCIDENTE', 'D_INCLASSIFICAVEL', None], 'condutaEsavi': ['MANTER', 'PRECAUCAO_CRIE', 'CONTRAINDICAR', None]}}}}
${DOSE_NOTIFICADA_SCHEMA}    ${{{'required': ['uuid', 'imunobiologicoId', 'sigla', 'doseRotulo', 'aplicadaEm', 'unidadeNome'], 'types': {'uuid': str, 'imunobiologicoId': str, 'sigla': str, 'doseRotulo': str, 'aplicadaEm': str, 'lote': str, 'unidadeNome': str}}}}
