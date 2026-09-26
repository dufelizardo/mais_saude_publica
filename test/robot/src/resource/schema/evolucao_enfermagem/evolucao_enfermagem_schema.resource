*** Comments ***
    Mais Saúde Pública API - EvolucaoEnfermagem response schema (EvolucaoEnfermagemResponseDto).
    Segunda entidade do domínio Enfermagem (ver MAPA-DE-DOMINIOS.md #8, ADR-0048).
*** Variables ***
${EVOLUCAO_ENFERMAGEM_SCHEMA}    ${{{'required': ['uuid', 'atendimentoUuid', 'profissionalMatricula', 'dataHora', 'descricao'], 'types': {'uuid': str, 'atendimentoUuid': str, 'profissionalMatricula': str, 'profissionalNome': str, 'dataHora': str, 'descricao': str}}}}
