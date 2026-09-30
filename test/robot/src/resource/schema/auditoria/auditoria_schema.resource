*** Comments ***
    Mais Saúde Pública API - Auditoria response schemas (PaginaAuditoriaResponseDto, EventoAuditoriaResponseDto).
    Trilha de auditoria (ver ADR-0070 e ADR-0071).
*** Variables ***
${PAGINA_AUDITORIA_SCHEMA}    ${{{'required': ['itens', 'total', 'pagina', 'tamanho', 'resumo'], 'types': {'itens': list, 'total': int, 'pagina': int, 'tamanho': int, 'resumo': dict}, 'enums': {}}}}
${EVENTO_AUDITORIA_SCHEMA}    ${{{'required': ['uuid', 'ocorridoEm', 'acao', 'resultado', 'recurso', 'metodo', 'rota', 'statusHttp'], 'types': {'uuid': str, 'ocorridoEm': str, 'usuarioCpf': str, 'usuarioNome': str, 'acao': str, 'resultado': str, 'recurso': str, 'metodo': str, 'rota': str, 'statusHttp': int, 'registroId': str, 'pacienteId': str, 'pacienteNome': str, 'unidadeId': str, 'unidadeNome': str, 'origemIp': str, 'detalhe': str}, 'enums': {'acao': ['LOGIN', 'TROCA_DE_SENHA', 'LEITURA', 'CRIACAO', 'ALTERACAO', 'RETIFICACAO', 'REVOGACAO', 'EXCLUSAO'], 'resultado': ['PERMITIDO', 'NEGADO']}}}}
