*** Comments ***
    Mais Saúde Pública API - Estadual response schema (EstadualResponseDto).
*** Variables ***
${ESTADUAL_SCHEMA}    ${{{'required': ['nome', 'tipo', 'estado'], 'types': {'nome': str, 'tipo': str, 'estado': str, 'administracaoSuperior': str, 'endereco': dict, 'saudeTelefones': list, 'email': str, 'horarioFuncionamento': dict, 'horarioAtendimento': dict}, 'enums': {'tipo': ['ESTADUAL']}}}}
