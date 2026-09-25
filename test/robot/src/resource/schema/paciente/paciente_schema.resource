*** Comments ***
    Mais Saúde Pública API - Paciente response schema (PacienteResponseDto). Primeira entidade da onda
    "Operação Assistencial" (ver ADR-0039/ADR-0040, MAPA-DE-DOMINIOS.md #5).
*** Variables ***
${PACIENTE_SCHEMA}    ${{{'required': ['uuid', 'nome', 'cpf', 'dataNascimento', 'sexo', 'endereco', 'ativo'], 'types': {'uuid': str, 'nome': str, 'cpf': str, 'cartaoSus': str, 'dataNascimento': str, 'sexo': str, 'endereco': dict, 'telefones': list, 'email': str, 'ativo': bool}, 'enums': {'sexo': ['MASCULINO', 'FEMININO', 'IGNORADO']}}}}
