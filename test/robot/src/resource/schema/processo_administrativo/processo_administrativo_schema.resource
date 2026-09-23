*** Comments ***
    Mais Saúde Pública API - Processo Administrativo response schema (ProcessoAdministrativoResponseDto).
    Setor Administrativo Adaptativo, Fase 4 (ver docs/adr/0033-processos-administrativos-por-capacidade.md).
*** Variables ***
${PROCESSO_ADMINISTRATIVO_SCHEMA}    ${{{'required': ['uuid', 'capacidadeUuid', 'codigo', 'nome', 'ativo'], 'types': {'uuid': str, 'capacidadeUuid': str, 'capacidadeCodigo': str, 'capacidadeNome': str, 'codigo': str, 'nome': str, 'descricao': str, 'ativo': bool}, 'enums': {}}}}
