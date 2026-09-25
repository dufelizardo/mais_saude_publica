*** Comments ***
    Mais Saúde Pública API - Consulta response schema (ConsultaResponseDto). Quarta entidade da onda
    "Operação Assistencial" (ver ADR-0039/ADR-0043, MAPA-DE-DOMINIOS.md #6).
*** Variables ***
${CONSULTA_SCHEMA}    ${{{'required': ['uuid', 'atendimentoUuid', 'profissionalMatricula', 'dataHora', 'tipoConsulta'], 'types': {'uuid': str, 'atendimentoUuid': str, 'profissionalMatricula': str, 'profissionalNome': str, 'dataHora': str, 'tipoConsulta': str, 'queixaPrincipal': str, 'diagnostico': str, 'receituario': str, 'examesSolicitados': str, 'retorno': str}, 'enums': {'tipoConsulta': ['PRIMEIRA', 'RETORNO', 'URGENCIA']}}}}
