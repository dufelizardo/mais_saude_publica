*** Comments ***
    Mais Saúde Pública API - Profissional response schema (ProfissionalResponseDto). Flat RH domain, no
    hierarchy chain — see ADR-0014. matricula/endereco/telefones added by ADR-0017.
*** Variables ***
${PROFISSIONAL_SCHEMA}    ${{{'required': ['matricula', 'cpf', 'nome'], 'types': {'matricula': str, 'cpf': str, 'nome': str, 'conselhoClasse': str, 'numeroConselho': str, 'endereco': dict, 'telefones': list, 'email': str, 'dataAdmissao': str, 'dataDesligamento': str, 'ativo': bool}, 'enums': {}}}}
