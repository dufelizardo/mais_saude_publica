*** Comments ***
    Mais Saúde Pública API - Estoque e almoxarifado (ADR-0177).
*** Variables ***
${MATERIAL_ESTOQUE_SCHEMA}    ${{{'required': ['uuid', 'codigo', 'nome', 'categoria', 'unidadeMedida', 'controlaLote', 'ativo', 'comMovimento'], 'types': {'uuid': str, 'codigo': str, 'controlaLote': bool, 'ativo': bool}}}}
${MOVIMENTACAO_ESTOQUE_SCHEMA}    ${{{'required': ['uuid', 'tipo', 'materialId', 'loteId', 'quantidade', 'saldoApos', 'registradoEm'], 'types': {'uuid': str, 'tipo': str, 'quantidade': int, 'saldoApos': int}}}}
${SALDO_ESTOQUE_SCHEMA}    ${{{'required': ['unidadeId', 'itens', 'abaixoDoMinimo', 'vencendo', 'vencidosComSaldo', 'vencimentos'], 'types': {'itens': list, 'abaixoDoMinimo': int, 'vencimentos': list}}}}
