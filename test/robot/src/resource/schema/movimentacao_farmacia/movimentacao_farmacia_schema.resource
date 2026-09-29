*** Comments ***
    Mais Saúde Pública API - MovimentacaoFarmacia response schema (MovimentacaoFarmaciaResponseDto). Livro de
    movimentação do estoque de lotes da Farmácia (ver ADR-0057, MAPA-DE-DOMINIOS.md #9).
*** Variables ***
${MOVIMENTACAO_FARMACIA_SCHEMA}    ${{{'required': ['uuid', 'loteId', 'tipo', 'quantidade', 'saldoApos', 'registradoEm'], 'types': {'uuid': str, 'loteId': str, 'numeroLote': str, 'medicamentoNome': str, 'tipo': str, 'quantidade': int, 'saldoApos': int, 'motivoPerda': str, 'justificativa': str, 'profissionalMatricula': str, 'profissionalNome': str, 'dispensacaoId': str, 'registradoEm': str, 'registradoPorCpf': str}, 'enums': {'tipo': ['SALDO_INICIAL', 'ENTRADA', 'DISPENSACAO', 'PERDA', 'AJUSTE_INVENTARIO'], 'motivoPerda': [None, 'VENCIMENTO', 'AVARIA', 'EXTRAVIO', 'OUTRO']}}}}
