*** Comments ***
    Mais Saúde Pública API - Profissional data-driven dictionaries.
    Every variable is prefixed with its exact operation name to stay unique inside the shared
    config/driven/data_driven.resource aggregator namespace.
*** Variables ***
# get_profissional_by_cpf (GET /api/v1/profissional/{cpf}) — 404=cpf not found
${get_profissional_by_cpf_nonexistent_cpf_404}    00000000000

# update_contato_profissional (PATCH /api/v1/profissional/contato/{cpf}) — 404=cpf not found
${update_contato_profissional_nonexistent_cpf_404}    00000000001

# desabilitar_profissional (DELETE /api/v1/profissional/des-habilitar/{cpf}) — 404=cpf not found
${desabilitar_profissional_nonexistent_cpf_404}    00000000002
