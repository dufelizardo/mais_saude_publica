*** Comments ***
    Mais Saúde Pública API - Atendimento response schema (AtendimentoResponseDto). Segunda entidade da onda
    "Operação Assistencial" (ver ADR-0039/ADR-0041, MAPA-DE-DOMINIOS.md #4).
*** Variables ***
${ATENDIMENTO_SCHEMA}    ${{{'required': ['uuid', 'pacienteUuid', 'profissionalMatricula', 'unidadeUuid', 'tipo', 'status', 'dataHora'], 'types': {'uuid': str, 'pacienteUuid': str, 'pacienteNome': str, 'profissionalMatricula': str, 'profissionalNome': str, 'unidadeUuid': str, 'unidadeNome': str, 'setorUuid': str, 'setorNome': str, 'agendamentoUuid': str, 'tipo': str, 'status': str, 'dataHora': str, 'pacienteCpf': str, 'pacienteDataNascimento': str, 'classificacaoRiscoAtual': str, 'totalTriagens': int, 'totalConsultas': int, 'totalProcedimentos': int, 'totalEvolucoes': int, 'totalAdministracoes': int}, 'enums': {'tipo': ['CONSULTA', 'URGENCIA', 'INTERNACAO'], 'status': ['AGENDADO', 'EM_ANDAMENTO', 'CONCLUIDO']}}}}
