*** Comments ***
    Mais Saúde Pública API - Saúde Bucal, fatia SB2: odontograma (ADR-0135).
*** Variables ***
${CONDICAO_ODONTOLOGICA_SCHEMA}    ${{{'required': ['uuid', 'codigo', 'nome', 'alcance', 'cor', 'sigla', 'ativa'], 'types': {'uuid': str, 'codigo': str, 'nome': str, 'alcance': str, 'cor': str, 'sigla': str, 'ativa': bool}, 'enums': {'alcance': ['FACE', 'DENTE', 'AMBOS']}}}}
${ODONTOGRAMA_SCHEMA}    ${{{'required': ['pacienteId', 'data', 'dentes', 'registros'], 'types': {'pacienteId': str, 'data': str, 'dentes': list, 'registros': int}}}}
