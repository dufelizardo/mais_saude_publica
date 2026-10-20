*** Comments ***
    Mais Saúde Pública API - Território, fatia F1 (ADR-0125).
*** Variables ***
${ARVORE_TERRITORIAL_SCHEMA}    ${{{'required': ['indicadores', 'segmentos'], 'types': {'indicadores': dict, 'segmentos': list}}}}
${AREA_TERRITORIAL_SCHEMA}    ${{{'required': ['area', 'territorioNome', 'finalidade', 'segmentoNome', 'responsabilidades', 'atribuicoes', 'acsSemMicroarea'], 'types': {'area': dict, 'territorioNome': str, 'finalidade': str, 'segmentoNome': str, 'responsabilidades': list, 'atribuicoes': list, 'acsSemMicroarea': list}, 'enums': {'finalidade': ['ATENCAO_PRIMARIA', 'REFERENCIA_ASSISTENCIAL', 'VIGILANCIA']}}}}
