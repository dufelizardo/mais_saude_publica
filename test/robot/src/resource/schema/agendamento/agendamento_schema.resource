*** Comments ***
    Mais Saúde Pública API - Agendamento response schema (AgendamentoResponseDto). Terceira entidade da onda
    "Operação Assistencial" (ver ADR-0039/ADR-0042, MAPA-DE-DOMINIOS.md #7).
*** Variables ***
${AGENDAMENTO_SCHEMA}    ${{{'required': ['uuid', 'pacienteUuid', 'profissionalMatricula', 'dataHora', 'status', 'tipo'], 'types': {'uuid': str, 'pacienteUuid': str, 'pacienteNome': str, 'profissionalMatricula': str, 'profissionalNome': str, 'dataHora': str, 'status': str, 'tipo': str, 'observacao': str, 'unidadeUuid': str, 'unidadeNome': str, 'encaixe': bool}, 'enums': {'status': ['AGENDADO', 'CONFIRMADO', 'REALIZADO', 'CANCELADO', 'FALTOU'], 'tipo': ['CONSULTA', 'PROCEDIMENTO', 'RETORNO']}}}}
