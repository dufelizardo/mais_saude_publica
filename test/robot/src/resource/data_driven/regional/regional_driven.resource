*** Comments ***
    Mais Saúde Pública API - Regional data-driven dictionaries.
    Every variable is prefixed with its exact operation name to stay unique inside the shared
    config/driven/data_driven.resource aggregator namespace.
*** Variables ***
# get_regional_by_nome (GET /api/v1/regional/{nome}) — 404=nome not found
${get_regional_by_nome_nonexistent_nome_404}    Unidade Que Nao Existe De Jeito Nenhum (Regional)

# update_nome_regional (PATCH /api/v1/regional/{nome}) — 404=nome not found
${update_nome_regional_nonexistent_nome_404}    Unidade Que Nao Existe De Jeito Nenhum (Regional Update Nome)

# update_contato_regional (PATCH /api/v1/regional/contato/{nome}) — 404=nome not found
${update_contato_regional_nonexistent_nome_404}    Unidade Que Nao Existe De Jeito Nenhum (Regional Update Contato)

# update_horario_funcionamento_regional (PATCH /api/v1/regional/horario-de-funcionamento/{nome}) — 404=nome not found
${update_horario_funcionamento_regional_nonexistent_nome_404}    Unidade Que Nao Existe De Jeito Nenhum (Regional Horario Funcionamento)

# update_horario_atendimento_regional (PATCH /api/v1/regional/horario-de-atendimento/{nome}) — 404=nome not found
${update_horario_atendimento_regional_nonexistent_nome_404}    Unidade Que Nao Existe De Jeito Nenhum (Regional Horario Atendimento)

# desabilitar_regional (DELETE /api/v1/regional/des-habilitar/{nome}) — 404=nome not found
${desabilitar_regional_nonexistent_nome_404}    Unidade Que Nao Existe De Jeito Nenhum (Regional Desabilitar)
