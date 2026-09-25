*** Comments ***
    Mais Saúde Pública API - Responsabilidade Administrativa response schema
    (ResponsabilidadeAdministrativaResponseDto). Setor Administrativo Adaptativo, Fase 6 (ver
    docs/adr/0035-responsabilidade-administrativa-separada-da-lotacao.md).
*** Variables ***
${RESPONSABILIDADE_ADMINISTRATIVA_SCHEMA}    ${{{'required': ['uuid', 'profissionalMatricula', 'setorUuid', 'tipo', 'dataInicio'], 'types': {'uuid': str, 'profissionalMatricula': str, 'profissionalNome': str, 'setorUuid': str, 'setorNome': str, 'tipo': str, 'descricao': str, 'dataInicio': str, 'dataFim': str}, 'enums': {}}}}
