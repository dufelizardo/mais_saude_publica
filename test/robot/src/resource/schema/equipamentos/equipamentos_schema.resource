*** Comments ***
    Mais Saúde Pública API - Equipamentos de Saúde (ADR-0101): rede, resumo da unidade e ficha.
*** Variables ***
${REDE_UNIDADES_SCHEMA}    ${{{'required': ['unidades', 'ativas', 'emOperacao', 'foraDeOperacao', 'porTipo', 'rede'], 'types': {'unidades': int, 'ativas': int, 'emOperacao': int, 'foraDeOperacao': int, 'porTipo': dict, 'rede': list}}}}
${REDE_UNIDADE_RESUMO_SCHEMA}    ${{{'required': ['uuid', 'nome', 'situacaoOperacional', 'ativo', 'funciona24h', 'profissionaisLotados', 'setores', 'leitos', 'leitosOcupados', 'comHorario'], 'types': {'uuid': str, 'nome': str, 'tipo': str, 'cnes': str, 'situacaoOperacional': str, 'ativo': bool, 'funciona24h': bool, 'profissionaisLotados': int, 'setores': int, 'leitos': int, 'leitosOcupados': int, 'comHorario': bool}, 'enums': {'situacaoOperacional': ['EM_OPERACAO', 'EM_MANUTENCAO', 'EM_OBRA', 'INOPERANTE']}}}}
${FICHA_UNIDADE_SCHEMA}    ${{{'required': ['resumo', 'turnos', 'subordinadas', 'setoresDaUnidade', 'profissionais', 'historico'], 'types': {'resumo': dict, 'turnos': list, 'subordinadas': list, 'setoresDaUnidade': list, 'profissionais': list, 'historico': list}}}}
