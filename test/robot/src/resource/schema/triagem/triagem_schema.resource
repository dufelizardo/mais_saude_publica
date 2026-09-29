*** Comments ***
    Mais Saúde Pública API - Triagem response schema (TriagemResponseDto). Primeira entidade do
    domínio Enfermagem (ver MAPA-DE-DOMINIOS.md #8, ADR-0047).
*** Variables ***
${TRIAGEM_SCHEMA}    ${{{'required': ['uuid', 'atendimentoUuid', 'profissionalMatricula', 'dataHora', 'classificacaoRisco'], 'types': {'uuid': str, 'atendimentoUuid': str, 'profissionalMatricula': str, 'profissionalNome': str, 'dataHora': str, 'pressaoArterial': str, 'temperatura': float, 'saturacaoOxigenio': float, 'frequenciaCardiaca': int, 'peso': float, 'classificacaoRisco': str, 'observacoes': str, 'retificacaoDeUuid': str, 'motivoRetificacao': str, 'registradoEm': str, 'registradoPorCpf': str, 'retificado': bool, 'retificadoPorUuid': str}, 'enums': {'classificacaoRisco': ['AZUL', 'VERDE', 'AMARELO', 'LARANJA', 'VERMELHO']}}}}
