*** Comments ***
    Mais Saúde Pública API - Escalas (ADR-0105): semana da unidade e turno.
*** Variables ***
${ESCALA_SEMANA_SCHEMA}    ${{{'required': ['unidadeId', 'inicio', 'fim', 'horasPrevistas', 'profissionais', 'turnos', 'vagasAbertas', 'plantoes', 'afastados', 'comAlerta', 'linhas', 'vagas', 'ausencias'], 'types': {'unidadeId': str, 'inicio': str, 'fim': str, 'profissionais': int, 'turnos': int, 'vagasAbertas': int, 'plantoes': int, 'afastados': int, 'comAlerta': int, 'linhas': list, 'vagas': list, 'ausencias': list}}}}
${TURNO_ESCALA_SCHEMA}    ${{{'required': ['uuid', 'unidadeId', 'tipo', 'data', 'inicioEm', 'fimEm', 'horas', 'vaga', 'alertas'], 'types': {'uuid': str, 'unidadeId': str, 'tipo': str, 'data': str, 'inicioEm': str, 'fimEm': str, 'vaga': bool, 'alertas': list}}}}
