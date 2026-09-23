*** Comments ***
    Mais Saúde Pública API - Perfil Por Tipo Unidade response schema (PerfilPorTipoUnidadeResponseDto).
    Setor Administrativo Adaptativo, Fase 3 (ver docs/adr/0031-perfil-administrativo-por-tipo-de-unidade.md) —
    resolve qual PerfilAdministrativo vale para cada TipoUnidadeDeSaude, sem condicional no código.
*** Variables ***
${PERFIL_POR_TIPO_UNIDADE_SCHEMA}    ${{{'required': ['uuid', 'tipo', 'perfilAdministrativoUuid', 'perfilAdministrativoCodigo'], 'types': {'uuid': str, 'tipo': str, 'perfilAdministrativoUuid': str, 'perfilAdministrativoCodigo': str, 'perfilAdministrativoNome': str}, 'enums': {'tipo': ['FEDERAL', 'ESTADUAL', 'MUNICIPAL', 'REGIONAL', 'UBS', 'HOSPITAL', 'UPA', 'LABORATORIO', 'CAPS', 'CENTRO_ESPECIALIDADES', 'CENTRO_REABILITACAO', 'POLICLINICA']}}}}
