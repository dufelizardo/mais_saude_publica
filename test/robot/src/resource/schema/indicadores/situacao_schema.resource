*** Comments ***
    Mais Saúde Pública API - Indicadores e BI, fatia BI2: situação operacional (ADR-0144).
*** Variables ***
${SITUACAO_AGORA_SCHEMA}    ${{{'required': ['atualizadoEm', 'abrangencia', 'emAtendimento', 'aguardandoTriagem', 'aguardandoConsulta', 'alertas'], 'types': {'atualizadoEm': str, 'unidadeId': str, 'abrangencia': str, 'emAtendimento': int, 'aguardandoTriagem': int, 'aguardandoConsulta': int, 'maiorEsperaMinutos': int, 'alertas': list}}}}
${ESPERA_ATENDIMENTO_SCHEMA}    ${{{'required': ['de', 'ate', 'abrangencia', 'comChegada', 'descartados', 'primeiroContato', 'consulta'], 'types': {'de': str, 'ate': str, 'unidadeId': str, 'abrangencia': str, 'comChegada': int, 'descartados': int, 'primeiroContato': dict, 'consulta': dict}}}}
${OCUPACAO_AGENDA_SCHEMA}    ${{{'required': ['de', 'ate', 'abrangencia', 'ofertadas', 'ocupadas', 'especialidades'], 'types': {'de': str, 'ate': str, 'unidadeId': str, 'abrangencia': str, 'ofertadas': int, 'ocupadas': int, 'percentual': int, 'especialidades': list}}}}
${OCORRENCIA_ESCALA_SCHEMA}    ${{{'required': ['turnoId', 'unidadeId', 'unidadeNome', 'inicio', 'fim', 'situacao', 'motivo'], 'types': {'turnoId': str, 'unidadeId': str, 'unidadeNome': str, 'profissionalNome': str, 'funcao': str, 'tipoTurno': str, 'inicio': str, 'fim': str, 'situacao': str, 'motivo': str}, 'enums': {'situacao': ['SEM_COBERTURA', 'AFASTADO']}}}}
