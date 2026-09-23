*** Comments ***
    Mais Saúde Pública API - Necessidade De Pessoal response schema (NecessidadeDePessoalResponseDto).
    Setor Administrativo Adaptativo, Fase 6 (ver docs/adr/0036-necessidade-de-pessoal-encaminhada-ao-rh.md).
*** Variables ***
${NECESSIDADE_DE_PESSOAL_SCHEMA}    ${{{'required': ['uuid', 'unidadeUuid', 'cargoUuid', 'quantidade', 'jornadaSemanalHoras', 'dataRegistro'], 'types': {'uuid': str, 'unidadeUuid': str, 'unidadeNome': str, 'setorUuid': str, 'setorNome': str, 'cargoUuid': str, 'cargoNome': str, 'quantidade': int, 'jornadaSemanalHoras': int, 'competenciasNecessarias': str, 'justificativa': str, 'dataRegistro': str, 'vagaAssociadaUuid': str}, 'enums': {}}}}
