*** Comments ***
    Mais Saúde Pública API - Documentos (ADR-0172).
*** Variables ***
${DOCUMENTOS_DO_PACIENTE_SCHEMA}    ${{{'required': ['podeAbrir', 'documentos', 'retirados'], 'types': {'podeAbrir': bool, 'documentos': list, 'retirados': list}}}}
