*** Comments ***
    Mais Saúde Pública API - Saúde Mental, fatia SM5 (ADR-0153).
*** Variables ***
${PAINEL_RAPS_SCHEMA}    ${{{'required': ['abrangencia', 'componentes', 'lacunas'], 'types': {'abrangencia': str, 'componentes': list, 'lacunas': list}}}}
${ACOLHIMENTO_NOTURNO_SCHEMA}    ${{{'required': ['unidadeId', 'temAcolhimentoNoturno', 'ocupadas', 'acolhidos', 'saidasRecentes'], 'types': {'unidadeId': str, 'unidadeNome': str, 'temAcolhimentoNoturno': bool, 'vagas': int, 'permanenciaMaximaDias': int, 'ocupadas': int, 'acolhidos': list, 'saidasRecentes': list}}}}
${PERMANENCIA_ACOLHIMENTO_SCHEMA}    ${{{'required': ['uuid', 'pacienteId', 'unidadeId', 'entrada', 'saidaPrevista', 'dias', 'passouDaSaidaPrevista', 'alemDoMaximo'], 'types': {'uuid': str, 'pacienteId': str, 'pacienteNome': str, 'unidadeId': str, 'entrada': str, 'saidaPrevista': str, 'saida': str, 'dias': int, 'passouDaSaidaPrevista': bool, 'alemDoMaximo': bool, 'justificativaPrazo': str, 'motivoSaida': str}}}}
