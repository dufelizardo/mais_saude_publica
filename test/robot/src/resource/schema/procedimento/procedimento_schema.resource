*** Comments ***
    Mais Saúde Pública API - Procedimento response schema (ProcedimentoResponseDto). Quinta e última
    entidade da onda "Operação Assistencial" antes do endpoint de agregação do Prontuário (ver
    ADR-0039/ADR-0044, MAPA-DE-DOMINIOS.md #6).
*** Variables ***
${PROCEDIMENTO_SCHEMA}    ${{{'required': ['uuid', 'consultaUuid', 'profissionalMatricula', 'tipo', 'dataRealizacao', 'status'], 'types': {'uuid': str, 'consultaUuid': str, 'profissionalMatricula': str, 'profissionalNome': str, 'tipo': str, 'descricao': str, 'dataRealizacao': str, 'status': str}, 'enums': {'status': ['AGENDADO', 'REALIZADO', 'CANCELADO']}}}}
