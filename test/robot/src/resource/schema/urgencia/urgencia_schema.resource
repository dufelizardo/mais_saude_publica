*** Comments ***
    Mais Saúde Pública API - Regulação de urgência (ADR-0176).
*** Variables ***
${OCORRENCIA_URGENCIA_SCHEMA}    ${{{'required': ['uuid', 'protocolo', 'centralId', 'origem', 'situacao', 'clinico', 'recebidaEm', 'vagaZero', 'eventos'], 'types': {'uuid': str, 'protocolo': str, 'situacao': str, 'clinico': bool, 'vagaZero': bool, 'eventos': list}}}}
${PAINEL_URGENCIA_SCHEMA}    ${{{'required': ['abertas', 'aguardandoRegulacao', 'emAtendimento', 'porPrioridade', 'recebidasHoje', 'concluidasHoje'], 'types': {'abertas': list, 'aguardandoRegulacao': int, 'porPrioridade': dict}}}}
