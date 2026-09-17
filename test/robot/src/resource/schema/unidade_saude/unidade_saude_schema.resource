*** Comments ***
    Mais Saúde Pública API - Unidade de Saúde response schema (UnidadeSaudeResponseDto). Unlike the other 4
    hierarchy levels, this domain accepts two tipo values (UBS and HOSPITAL — see ADR-0013) and carries two
    extra lateral/weak associations: supervisaoRegional (nome of a REGIONAL unit) and responsavelCpf/
    responsavelNome (weak link to a Profissional, resolved by CPF reconciliation — see ADR-0014).
*** Variables ***
${UNIDADE_SAUDE_SCHEMA}    ${{{'required': ['nome', 'tipo'], 'types': {'nome': str, 'tipo': str, 'administracaoSuperior': str, 'supervisaoRegional': str, 'responsavelCpf': str, 'responsavelNome': str, 'endereco': dict, 'saudeTelefones': list, 'email': str, 'horarioFuncionamento': dict, 'horarioAtendimento': dict}, 'enums': {'tipo': ['UBS', 'HOSPITAL']}}}}
