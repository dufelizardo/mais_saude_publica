*** Comments ***
    Mais Saúde Pública API - Lote response schema (LoteResponseDto). Segunda entidade do domínio
    Farmácia (ver MAPA-DE-DOMINIOS.md #9, ADR-0050).
*** Variables ***
${LOTE_SCHEMA}    ${{{'required': ['uuid', 'medicamentoUuid', 'unidadeUuid', 'numeroLote', 'validade', 'quantidade'], 'types': {'uuid': str, 'medicamentoUuid': str, 'medicamentoNome': str, 'unidadeUuid': str, 'unidadeNome': str, 'numeroLote': str, 'validade': str, 'quantidade': int}}}}
