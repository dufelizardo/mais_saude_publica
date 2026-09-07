*** Comments ***
    Mais Saúde Pública API - Municipal data-driven dictionaries.
    Every variable is prefixed with its exact operation name to stay unique inside the shared
    config/driven/data_driven.resource aggregator namespace.
*** Variables ***
# get_municipal_by_nome (GET /api/v1/municipal/{nome}) — 404=nome not found
${get_municipal_by_nome_nonexistent_nome_404}    Unidade Que Nao Existe De Jeito Nenhum (Municipal)

# update_nome_municipal (PATCH /api/v1/municipal/{nome}) — 404=nome not found
${update_nome_municipal_nonexistent_nome_404}    Unidade Que Nao Existe De Jeito Nenhum (Municipal Update Nome)

# update_contato_municipal (PATCH /api/v1/municipal/contato/{nome}) — 404=nome not found
${update_contato_municipal_nonexistent_nome_404}    Unidade Que Nao Existe De Jeito Nenhum (Municipal Update Contato)

# update_horario_funcionamento_municipal (PATCH /api/v1/municipal/horario-de-funcionamento/{nome}) — 404=nome not found
${update_horario_funcionamento_municipal_nonexistent_nome_404}    Unidade Que Nao Existe De Jeito Nenhum (Municipal Horario Funcionamento)

# update_horario_atendimento_municipal (PATCH /api/v1/municipal/horario-de-atendimento/{nome}) — 404=nome not found
${update_horario_atendimento_municipal_nonexistent_nome_404}    Unidade Que Nao Existe De Jeito Nenhum (Municipal Horario Atendimento)

# desabilitar_municipal (DELETE /api/v1/municipal/des-habilitar/{nome}) — 404=nome not found
${desabilitar_municipal_nonexistent_nome_404}    Unidade Que Nao Existe De Jeito Nenhum (Municipal Desabilitar)
