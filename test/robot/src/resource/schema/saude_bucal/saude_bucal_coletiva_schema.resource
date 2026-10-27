*** Comments ***
    Mais Saúde Pública API - Saúde Bucal, fatia SB5: saúde bucal coletiva (ADR-0140).
*** Variables ***
${ACAO_SAUDE_BUCAL_SCHEMA}    ${{{'required': ['acaoId', 'preenchido', 'atividades', 'avaliacoes'], 'types': {'acaoId': str, 'preenchido': bool, 'atividades': list, 'localTipo': str, 'instituicao': str, 'publicoAlvo': str, 'kitsDistribuidos': int, 'atualizadoEm': str, 'avaliacoes': list}, 'enums': {'localTipo': ['ESCOLA', 'CRECHE', 'UBS', 'COMUNIDADE', 'EMPRESA', 'INSTITUICAO_LONGA_PERMANENCIA', 'OUTRO']}}}}
${CONVOCACAO_SAUDE_BUCAL_SCHEMA}    ${{{'required': ['pacienteId', 'pacienteNome', 'risco', 'necessidadeTratamento', 'acaoId', 'programaNome', 'avaliadoEm'], 'types': {'pacienteId': str, 'pacienteNome': str, 'risco': str, 'necessidadeTratamento': str, 'acaoId': str, 'programaNome': str, 'local': str, 'realizadaEm': str, 'avaliadoEm': str}, 'enums': {'risco': ['BAIXO', 'MEDIO', 'ALTO'], 'necessidadeTratamento': ['ELETIVA', 'PRIORITARIA', 'URGENCIA']}}}}
