*** Comments ***
    Mais Saúde Pública API - Saúde Bucal, fatia SB4a: item regulado com a capacidade exigida da executante (ADR-0138).
*** Variables ***
${PROCEDIMENTO_REGULADO_ODONTOLOGICO_SCHEMA}    ${{{'required': ['uuid', 'nome', 'tipo', 'ativo', 'capacidadeExigidaId', 'capacidadeExigidaNome'], 'types': {'uuid': str, 'nome': str, 'tipo': str, 'ativo': bool, 'capacidadeExigidaId': str, 'capacidadeExigidaNome': str}, 'enums': {'tipo': ['CONSULTA_ESPECIALIZADA', 'EXAME', 'PROCEDIMENTO']}}}}
