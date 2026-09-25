*** Comments ***
    Mais Saúde Pública API - Setor response schema (SetorResponseDto). Setor Administrativo Adaptativo,
    Fase 1 (ver docs/adr/0030-setor-administrativo-e-relacao-com-unidade-de-saude.md) — genérico por tipo,
    "Setor Administrativo" é só um Setor com tipo=ADMINISTRATIVO, não uma classe própria.
*** Variables ***
${SETOR_SCHEMA}    ${{{'required': ['uuid', 'unidadeUuid', 'nome', 'codigo', 'tipo', 'ativo'], 'types': {'uuid': str, 'unidadeUuid': str, 'unidadeNome': str, 'nome': str, 'codigo': str, 'tipo': str, 'ativo': bool, 'responsavelMatricula': str, 'responsavelNome': str}, 'enums': {'tipo': ['ADMINISTRATIVO', 'ASSISTENCIAL', 'APOIO', 'TECNICO']}}}}
