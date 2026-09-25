*** Comments ***
    Mais Saúde Pública API - Perfil Administrativo response schema (PerfilAdministrativoResponseDto).
    Setor Administrativo Adaptativo, Fase 3 (ver docs/adr/0031-perfil-administrativo-por-tipo-de-unidade.md).
*** Variables ***
${PERFIL_ADMINISTRATIVO_SCHEMA}    ${{{'required': ['uuid', 'codigo', 'nome', 'ativo'], 'types': {'uuid': str, 'codigo': str, 'nome': str, 'descricao': str, 'ativo': bool}, 'enums': {}}}}
