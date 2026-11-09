*** Comments ***
    Mais Saúde Pública API - Campanhas de comunicação (ADR-0174).
*** Variables ***
${CAMPANHA_COMUNICACAO_SCHEMA}    ${{{'required': ['uuid', 'nome', 'objetivo', 'situacao', 'publicoAlvo', 'temas', 'canalIds', 'conteudos', 'comunicados', 'alcance'], 'types': {'uuid': str, 'nome': str, 'temas': list, 'canalIds': list, 'conteudos': list, 'comunicados': list, 'alcance': list}}}}
