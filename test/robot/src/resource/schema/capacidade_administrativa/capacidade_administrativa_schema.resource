*** Comments ***
    Mais Saúde Pública API - Capacidade Administrativa response schema (CapacidadeAdministrativaResponseDto).
    Setor Administrativo Adaptativo, Fase 2 (ver docs/adr/0032-catalogo-de-capacidades-administrativas.md) —
    catálogo/feature-toggle de domínio, não controle de acesso.
*** Variables ***
${CAPACIDADE_ADMINISTRATIVA_SCHEMA}    ${{{'required': ['uuid', 'codigo', 'nome', 'ativo'], 'types': {'uuid': str, 'codigo': str, 'nome': str, 'descricao': str, 'ativo': bool}, 'enums': {}}}}
