*** Comments ***
    Mais Saúde Pública API - Programas de saúde, fatia P3: campanhas (ADR-0133).
*** Variables ***
${CAMPANHA_SCHEMA}    ${{{'required': ['uuid', 'nome', 'tipo', 'inicio', 'fim', 'situacao', 'publico', 'gestante', 'condicoes', 'realizado', 'publicada', 'acoesPendentes', 'acoesRealizadas', 'locais'], 'types': {'uuid': str, 'nome': str, 'tipo': str, 'inicio': str, 'fim': str, 'situacao': str, 'publico': str, 'gestante': bool, 'condicoes': list, 'realizado': int, 'publicada': bool, 'acoesPendentes': int, 'acoesRealizadas': int, 'locais': list}, 'enums': {'tipo': ['VACINACAO', 'RASTREAMENTO', 'CONSCIENTIZACAO', 'MUTIRAO', 'OUTRA'], 'situacao': ['PLANEJADA', 'EM_ANDAMENTO', 'ENCERRADA', 'CANCELADA']}}}}
${CAMPANHA_PUBLICA_SCHEMA}    ${{{'required': ['nome', 'tipo', 'publico', 'inicio', 'fim', 'situacao', 'realizado', 'locais'], 'types': {'nome': str, 'tipo': str, 'publico': str, 'inicio': str, 'fim': str, 'situacao': str, 'realizado': int, 'locais': list}}}}
