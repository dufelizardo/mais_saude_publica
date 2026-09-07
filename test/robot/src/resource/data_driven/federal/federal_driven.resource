*** Comments ***
    Mais Saúde Pública API - Federal data-driven dictionaries.
    Every variable is prefixed with its exact operation name to stay unique inside the shared
    config/driven/data_driven.resource aggregator namespace.
*** Variables ***
# get_federal_by_nome (GET /api/v1/federal/{nome}) — 404=nome not found
${get_federal_by_nome_nonexistent_nome_404}    Unidade Que Nao Existe De Jeito Nenhum AQUAQE-158

# update_nome_federal (PATCH /api/v1/federal/{nome}) — 404=nome not found
${update_nome_federal_nonexistent_nome_404}    Unidade Que Nao Existe De Jeito Nenhum (Update Nome)

# update_contato_federal (PATCH /api/v1/federal/contato/{nome}) — 404=nome not found
${update_contato_federal_nonexistent_nome_404}    Unidade Que Nao Existe De Jeito Nenhum (Update Contato)

# update_horario_funcionamento_federal (PATCH /api/v1/federal/horario-de-funcionamento/{nome}) — 404=nome not found
${update_horario_funcionamento_federal_nonexistent_nome_404}    Unidade Que Nao Existe De Jeito Nenhum (Horario Funcionamento)

# update_horario_atendimento_federal (PATCH /api/v1/federal/horario-de-atendimento/{nome}) — 404=nome not found
${update_horario_atendimento_federal_nonexistent_nome_404}    Unidade Que Nao Existe De Jeito Nenhum (Horario Atendimento)

# desabilitar_federal (DELETE /api/v1/federal/des-habilitar/{nome}) — 404=nome not found
${desabilitar_federal_nonexistent_nome_404}    Unidade Que Nao Existe De Jeito Nenhum (Desabilitar)
