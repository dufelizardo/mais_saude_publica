*** Comments ***
    Mais Saúde Pública API - Auth response schemas (LoginResponseDto, SecurityStatusResponseDto).
    Primeira entidade do domínio Identidade/Autorização (ver MAPA-DE-DOMINIOS.md #20, ADR-0054/ADR-0055).
*** Variables ***
${LOGIN_RESPONSE_SCHEMA}    ${{{'required': ['token', 'cpf', 'nome'], 'types': {'token': str, 'cpf': str, 'nome': str}, 'enums': {}}}}
${AUTH_STATUS_SCHEMA}    ${{{'required': ['securityEnabled'], 'types': {'securityEnabled': bool}, 'enums': {}}}}
