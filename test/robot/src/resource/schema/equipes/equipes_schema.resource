*** Comments ***
    Mais Saúde Pública API - Equipes (ADR-0103): lista com indicadores e equipe com membros.
*** Variables ***
${EQUIPES_SCHEMA}    ${{{'required': ['equipes', 'ativas', 'porTipo', 'profissionaisVinculados', 'incompletas', 'lista'], 'types': {'equipes': int, 'ativas': int, 'porTipo': dict, 'profissionaisVinculados': int, 'incompletas': int, 'lista': list}}}}
${EQUIPE_SCHEMA}    ${{{'required': ['resumo', 'membros', 'antigos', 'apoiadas', 'apoiadaPor'], 'types': {'resumo': dict, 'membros': list, 'antigos': list, 'apoiadas': list, 'apoiadaPor': list}}}}
