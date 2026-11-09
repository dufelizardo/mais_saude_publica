*** Comments ***
    Mais Saúde Pública API - Gestão da Rede (ADR-0179).
*** Variables ***
${OFERTA_ASSISTENCIAL_SCHEMA}    ${{{'required': ['uuid', 'competencia', 'executanteId', 'procedimentoId', 'quantidade', 'pactuado', 'semPactuar', 'utilizado', 'extracotas', 'cotas'], 'types': {'uuid': str, 'quantidade': int, 'cotas': list}}}}
${PAINEL_PPI_SCHEMA}    ${{{'required': ['competencia', 'ofertado', 'pactuado', 'utilizado', 'extracotas', 'ofertas'], 'types': {'ofertado': int, 'ofertas': list}}}}
