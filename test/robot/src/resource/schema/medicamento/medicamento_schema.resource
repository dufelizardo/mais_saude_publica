*** Comments ***
    Mais Saúde Pública API - Medicamento response schema (MedicamentoResponseDto). Primeira entidade do
    domínio Farmácia (ver MAPA-DE-DOMINIOS.md #9, ADR-0049).
*** Variables ***
${MEDICAMENTO_SCHEMA}    ${{{'required': ['uuid', 'nome'], 'types': {'uuid': str, 'nome': str, 'principioAtivo': str, 'apresentacao': str, 'codigo': str, 'ativo': bool}}}}
