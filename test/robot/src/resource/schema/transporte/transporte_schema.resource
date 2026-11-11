*** Comments ***
    Mais Saúde Pública API - Transporte (ADR-0178 e ADR-0190).
*** Variables ***
${VEICULO_SCHEMA}    ${{{'required': ['uuid', 'placa', 'prefixo', 'tipo', 'doSamu', 'maca', 'unidadeId', 'situacao', 'historico'], 'types': {'uuid': str, 'placa': str, 'doSamu': bool, 'historico': list}}}}
${PLANTAO_SCHEMA}    ${{{'required': ['uuid', 'veiculoId', 'prefixo', 'inicio', 'fim', 'vigente', 'tripulacao'], 'types': {'uuid': str, 'vigente': bool, 'tripulacao': list}}}}
${SOLICITACAO_TRANSPORTE_SCHEMA}    ${{{'required': ['uuid', 'pacienteId', 'unidadeId', 'motivo', 'dataHora', 'enderecoEmbarque', 'destino', 'trecho', 'lugares', 'situacao'], 'types': {'uuid': str, 'maca': bool, 'lugares': int}}}}
${VIAGEM_TRANSPORTE_SCHEMA}    ${{{'required': ['uuid', 'veiculoId', 'prefixo', 'condutorNome', 'saidaPrevista', 'retornoPrevisto', 'destino', 'situacao', 'lugaresOcupados', 'embarques'], 'types': {'uuid': str, 'lugaresOcupados': int, 'embarques': list}}}}
