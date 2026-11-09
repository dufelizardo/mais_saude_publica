*** Comments ***
    Mais Saúde Pública API - Estrutura de gestão (ADR-0175).
*** Variables ***
${AREA_GESTAO_SCHEMA}    ${{{'required': ['uuid', 'tipo', 'tema', 'nome', 'ativa', 'unidades', 'historico'], 'types': {'uuid': str, 'nome': str, 'ativa': bool, 'unidades': int, 'historico': list}}}}
