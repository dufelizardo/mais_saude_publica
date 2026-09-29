*** Comments ***
    Mais Saúde Pública API - TransferenciaFarmacia response schema (TransferenciaFarmaciaResponseDto). Transferência
    de estoque de um lote entre unidades (ver ADR-0059, MAPA-DE-DOMINIOS.md #9).
*** Variables ***
${TRANSFERENCIA_FARMACIA_SCHEMA}    ${{{'required': ['uuid', 'loteOrigemId', 'loteDestinoId', 'unidadeOrigemId', 'unidadeDestinoId', 'quantidade', 'profissionalMatricula', 'registradoEm'], 'types': {'uuid': str, 'medicamentoNome': str, 'numeroLote': str, 'validade': str, 'loteOrigemId': str, 'unidadeOrigemId': str, 'unidadeOrigemNome': str, 'loteDestinoId': str, 'unidadeDestinoId': str, 'unidadeDestinoNome': str, 'quantidade': int, 'profissionalMatricula': str, 'profissionalNome': str, 'observacao': str, 'registradoEm': str, 'registradoPorCpf': str}, 'enums': {}}}}
