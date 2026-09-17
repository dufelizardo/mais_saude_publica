*** Comments ***
    Mais Saúde Pública API - Profissional response schema (ProfissionalResponseDto). Flat RH domain, no
    hierarchy chain — see ADR-0014.
*** Variables ***
${PROFISSIONAL_SCHEMA}    ${{{'required': ['cpf', 'nome'], 'types': {'cpf': str, 'nome': str, 'conselhoClasse': str, 'numeroConselho': str, 'telefone': str, 'email': str, 'dataAdmissao': str, 'dataDesligamento': str, 'ativo': bool}, 'enums': {}}}}
