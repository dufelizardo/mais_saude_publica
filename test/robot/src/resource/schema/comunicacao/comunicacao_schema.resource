*** Comments ***
    Mais Saúde Pública API - Comunicação (ADR-0168): comunicado e conteúdo educativo.
*** Variables ***
${COMUNICADO_SCHEMA}    ${{{'required': ['uuid', 'titulo', 'mensagem', 'prioridade', 'publico', 'inicio', 'fim', 'canalIds', 'situacao', 'vigente', 'publicacoes'], 'types': {'uuid': str, 'titulo': str, 'mensagem': str, 'prioridade': str, 'publico': str, 'inicio': str, 'fim': str, 'canalIds': list, 'situacao': str, 'vigente': bool, 'publicacoes': list}}}}
${CONTEUDO_EDUCATIVO_SCHEMA}    ${{{'required': ['uuid', 'grupoId', 'versao', 'titulo', 'resumo', 'texto', 'tema', 'publico', 'validade', 'situacao', 'publicacoes'], 'types': {'uuid': str, 'grupoId': str, 'versao': int, 'titulo': str, 'resumo': str, 'texto': str, 'tema': str, 'publico': str, 'validade': str, 'situacao': str, 'publicacoes': list}}}}
