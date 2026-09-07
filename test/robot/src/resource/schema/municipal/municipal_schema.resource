*** Comments ***
    Mais Saúde Pública API - Municipal response schema (MunicipalResponseDto).
*** Variables ***
${MUNICIPAL_SCHEMA}    ${{{'required': ['nome', 'tipo', 'municipio'], 'types': {'nome': str, 'tipo': str, 'municipio': str, 'administracaoSuperior': str, 'endereco': dict, 'saudeTelefones': list, 'email': str, 'horarioFuncionamento': dict, 'horarioAtendimento': dict}, 'enums': {'tipo': ['MUNICIPAL']}}}}
