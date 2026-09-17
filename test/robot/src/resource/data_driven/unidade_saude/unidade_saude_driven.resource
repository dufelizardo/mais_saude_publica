*** Comments ***
    Mais Saúde Pública API - Unidade de Saúde data-driven dictionaries.
    Every variable is prefixed with its exact operation name to stay unique inside the shared
    config/driven/data_driven.resource aggregator namespace.
*** Variables ***
# get_unidade_saude_by_nome (GET /api/v1/unidade-saude/{nome}) — 404=nome not found
${get_unidade_saude_by_nome_nonexistent_nome_404}    Unidade Que Nao Existe De Jeito Nenhum (Unidade Saude)

# update_nome_unidade_saude (PATCH /api/v1/unidade-saude/{nome}) — 404=nome not found
${update_nome_unidade_saude_nonexistent_nome_404}    Unidade Que Nao Existe De Jeito Nenhum (Unidade Saude Update Nome)

# update_contato_unidade_saude (PATCH /api/v1/unidade-saude/contato/{nome}) — 404=nome not found
${update_contato_unidade_saude_nonexistent_nome_404}    Unidade Que Nao Existe De Jeito Nenhum (Unidade Saude Update Contato)

# update_horario_funcionamento_unidade_saude (PATCH /api/v1/unidade-saude/horario-de-funcionamento/{nome}) — 404=nome not found
${update_horario_funcionamento_unidade_saude_nonexistent_nome_404}    Unidade Que Nao Existe De Jeito Nenhum (Unidade Saude Horario Funcionamento)

# update_horario_atendimento_unidade_saude (PATCH /api/v1/unidade-saude/horario-de-atendimento/{nome}) — 404=nome not found
${update_horario_atendimento_unidade_saude_nonexistent_nome_404}    Unidade Que Nao Existe De Jeito Nenhum (Unidade Saude Horario Atendimento)

# desabilitar_unidade_saude (DELETE /api/v1/unidade-saude/des-habilitar/{nome}) — 404=nome not found
${desabilitar_unidade_saude_nonexistent_nome_404}    Unidade Que Nao Existe De Jeito Nenhum (Unidade Saude Desabilitar)

# update_supervisao_regional_unidade_saude (PATCH /api/v1/unidade-saude/supervisao-regional/{nome}) — 404=nome not found
${update_supervisao_regional_unidade_saude_nonexistent_nome_404}    Unidade Que Nao Existe De Jeito Nenhum (Unidade Saude Supervisao Regional)

# update_responsavel_unidade_saude (PATCH /api/v1/unidade-saude/responsavel/{nome}) — 404=nome not found
${update_responsavel_unidade_saude_nonexistent_nome_404}    Unidade Que Nao Existe De Jeito Nenhum (Unidade Saude Responsavel)
