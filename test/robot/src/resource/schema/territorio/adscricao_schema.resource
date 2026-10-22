*** Comments ***
    Mais Saúde Pública API - Adscrição e cobertura, fatia F3b (ADR-0128).
*** Variables ***
${ADSCRICAO_PACIENTE_SCHEMA}    ${{{'required': ['pacienteId', 'atual', 'historico'], 'types': {'pacienteId': str, 'atual': dict, 'historico': list}}}}
${COBERTURA_SCHEMA}    ${{{'required': ['equipeId', 'equipeNome', 'pessoasAdscritas', 'adscritasManual', 'familias', 'imoveis', 'microareasSemCadastro', 'microareas'], 'types': {'equipeId': str, 'equipeNome': str, 'pessoasAdscritas': int, 'adscritasManual': int, 'familias': int, 'imoveis': int, 'microareasSemCadastro': int, 'microareas': list}}}}
${ELEGIVEIS_SCHEMA}    ${{{'required': ['programaId', 'equipeId', 'criteriosAvaliados', 'criteriosNaoAvaliados', 'semDataNascimento', 'elegiveis'], 'types': {'programaId': str, 'equipeId': str, 'criteriosAvaliados': list, 'criteriosNaoAvaliados': list, 'semDataNascimento': int, 'elegiveis': list}}}}
