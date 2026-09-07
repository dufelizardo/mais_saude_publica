*** Comments ***
    Mais Saúde Pública API - Estadual data-driven dictionaries.
    Every variable is prefixed with its exact operation name to stay unique inside the shared
    config/driven/data_driven.resource aggregator namespace.
*** Variables ***
# get_estadual_by_nome (GET /api/v1/estadual/{nome}) — 404=nome not found
${get_estadual_by_nome_nonexistent_nome_404}    Unidade Que Nao Existe De Jeito Nenhum (Estadual)

# update_nome_estadual (PATCH /api/v1/estadual/{nome}) — 404=nome not found
${update_nome_estadual_nonexistent_nome_404}    Unidade Que Nao Existe De Jeito Nenhum (Estadual Update Nome)

# update_contato_estadual (PATCH /api/v1/estadual/contato/{nome}) — 404=nome not found
${update_contato_estadual_nonexistent_nome_404}    Unidade Que Nao Existe De Jeito Nenhum (Estadual Update Contato)

# update_horario_funcionamento_estadual (PATCH /api/v1/estadual/horario-de-funcionamento/{nome}) — 404=nome not found
${update_horario_funcionamento_estadual_nonexistent_nome_404}    Unidade Que Nao Existe De Jeito Nenhum (Estadual Horario Funcionamento)

# update_horario_atendimento_estadual (PATCH /api/v1/estadual/horario-de-atendimento/{nome}) — 404=nome not found
${update_horario_atendimento_estadual_nonexistent_nome_404}    Unidade Que Nao Existe De Jeito Nenhum (Estadual Horario Atendimento)

# desabilitar_estadual (DELETE /api/v1/estadual/des-habilitar/{nome}) — 404=nome not found
${desabilitar_estadual_nonexistent_nome_404}    Unidade Que Nao Existe De Jeito Nenhum (Estadual Desabilitar)
