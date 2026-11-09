*** Comments ***
    Mais Saúde Pública API - Transporte (ADR-0178).
*** Variables ***
${VEICULO_SCHEMA}    ${{{'required': ['uuid', 'placa', 'prefixo', 'tipo', 'doSamu', 'maca', 'unidadeId', 'situacao', 'historico'], 'types': {'uuid': str, 'placa': str, 'doSamu': bool, 'historico': list}}}}
${PLANTAO_SCHEMA}    ${{{'required': ['uuid', 'veiculoId', 'prefixo', 'inicio', 'fim', 'vigente', 'tripulacao'], 'types': {'uuid': str, 'vigente': bool, 'tripulacao': list}}}}
