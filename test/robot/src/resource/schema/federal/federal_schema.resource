*** Comments ***
    Mais Saúde Pública API - Federal response schema (FederalResponseDto).
*** Variables ***
${FEDERAL_SCHEMA}    ${{{'required': ['nome', 'tipo'], 'types': {'nome': str, 'tipo': str, 'endereco': dict, 'saudeTelefones': list, 'email': str, 'horarioFuncionamento': dict, 'horarioAtendimento': dict}, 'enums': {'tipo': ['FEDERAL']}}}}
