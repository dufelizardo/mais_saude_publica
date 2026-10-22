*** Comments ***
    Mais Saúde Pública API - Saúde Bucal, fatia SB1: especialidades e avaliação odontológica (ADR-0134).
*** Variables ***
${ESPECIALIDADE_SCHEMA}    ${{{'required': ['uuid', 'codigo', 'nome', 'odontologica', 'ativa'], 'types': {'uuid': str, 'codigo': str, 'nome': str, 'odontologica': bool, 'ativa': bool}}}}
${AVALIACAO_ODONTOLOGICA_SCHEMA}    ${{{'required': ['uuid', 'atendimentoId', 'pacienteId', 'profissionalMatricula', 'dataHora', 'risco', 'necessidadeTratamento', 'retificado', 'registradoEm'], 'types': {'uuid': str, 'atendimentoId': str, 'pacienteId': str, 'profissionalMatricula': str, 'dataHora': str, 'risco': str, 'necessidadeTratamento': str, 'retificado': bool, 'registradoEm': str}, 'enums': {'risco': ['BAIXO', 'MEDIO', 'ALTO'], 'necessidadeTratamento': ['SEM_NECESSIDADE', 'ELETIVA', 'PRIORITARIA', 'URGENCIA']}}}}
