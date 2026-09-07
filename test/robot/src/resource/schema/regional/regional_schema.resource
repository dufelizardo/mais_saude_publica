*** Comments ***
    Mais Saúde Pública API - Regional response schema (RegionalResponseDto).
*** Variables ***
${REGIONAL_SCHEMA}    ${{{'required': ['nome', 'tipo', 'regiao'], 'types': {'nome': str, 'tipo': str, 'regiao': str, 'administracaoSuperior': str, 'endereco': dict, 'saudeTelefones': list, 'email': str, 'horarioFuncionamento': dict, 'horarioAtendimento': dict}, 'enums': {'tipo': ['REGIONAL']}}}}
