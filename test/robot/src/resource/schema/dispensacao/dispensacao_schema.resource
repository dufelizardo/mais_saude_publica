*** Comments ***
    Mais Saúde Pública API - Dispensacao response schema (DispensacaoResponseDto). Terceira entidade do
    domínio Farmácia (ver MAPA-DE-DOMINIOS.md #9, ADR-0051).
*** Variables ***
${DISPENSACAO_SCHEMA}    ${{{'required': ['uuid', 'loteUuid', 'pacienteUuid', 'profissionalMatricula', 'quantidade', 'dataHora'], 'types': {'uuid': str, 'loteUuid': str, 'loteNumeroLote': str, 'medicamentoNome': str, 'pacienteUuid': str, 'pacienteNome': str, 'profissionalMatricula': str, 'profissionalNome': str, 'consultaUuid': str, 'quantidade': int, 'dataHora': str}}}}
