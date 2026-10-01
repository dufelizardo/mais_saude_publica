*** Settings ***
Resource    ../../../pom/evolucao_enfermagem/retificar_evolucao_enfermagem/retificar_evolucao_enfermagem_pom.resource
Resource    ../create_evolucao_enfermagem/create_evolucao_enfermagem_flow.resource
Resource    ../get_evolucao_enfermagem_by_id/get_evolucao_enfermagem_by_id_flow.resource
Resource    ../../common/schema_validation_flow.resource
Resource    ../../../resource/config/schema/schema.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
    Registro clínico não é editado (ADR-0062): a retificação cria uma nova versão ligada à anterior, que
    continua existindo e passa a ser marcada como retificada. Só a versão vigente pode ser retificada.
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
POST Retificar Evolucao Enfermagem - Status Code 201
    [Documentation]    Seeds a fresh Evolucao Enfermagem, retifies it and checks the original is kept and flagged as retificado.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${evolucaoId}    ${atendimentoId}    ${matricula}=    Seed Uma Evolucao Enfermagem
    ${payload}=    Build Evolucao Enfermagem Payload    ${atendimentoId}    ${matricula}    descricao=Descrição corrigida
    Set To Dictionary    ${payload}    motivoRetificacao=Valor registrado errado

    ${response}=    POST Retificar Evolucao Enfermagem    ${evolucaoId}    ${payload}
    Should Be Equal As Integers    ${response.status_code}    201
    Validate Object Schema    ${response.json()}    ${SUCCESS_RESPONSE_SCHEMA}

    ${original}=    Call GET Get Evolucao Enfermagem By Id    ${evolucaoId}
    Should Be Equal As Integers    ${original.status_code}    200
    Should Be True    ${original.json()}[retificado]
    Should Not Be Empty    ${original.json()}[retificadoPorUuid]

    ${nova}=    Call GET Get Evolucao Enfermagem By Id    ${original.json()}[retificadoPorUuid]
    Should Be Equal As Strings    ${nova.json()}[descricao]    Descrição corrigida
    Should Be Equal As Strings    ${nova.json()}[retificacaoDeUuid]    ${evolucaoId}

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

POST Retificar Evolucao Enfermagem - Status Code 404
    [Documentation]    Attempts to retify a Evolucao Enfermagem identified by a random, non-existent uuid, expecting HTTP 404.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${atendimentoId}    ${pacienteId}    ${matricula}    ${unidadeId}=    Seed Um Atendimento
    ${payload}=    Build Evolucao Enfermagem Payload    ${atendimentoId}    ${matricula}
    ${uuidInexistente}=    Evaluate    str(uuid.uuid4())    modules=uuid
    Set To Dictionary    ${payload}    motivoRetificacao=Correção

    ${response}=    POST Retificar Evolucao Enfermagem    ${uuidInexistente}    ${payload}
    Should Be Equal As Integers    ${response.status_code}    404

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

POST Retificar Evolucao Enfermagem - Status Code 422 - Versao Ja Retificada
    [Documentation]    Retifies the same Evolucao Enfermagem twice: the second attempt targets a version that is no longer current,
    ...    expecting HTTP 422.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${evolucaoId}    ${atendimentoId}    ${matricula}=    Seed Uma Evolucao Enfermagem
    ${payload}=    Build Evolucao Enfermagem Payload    ${atendimentoId}    ${matricula}    descricao=Descrição corrigida
    Set To Dictionary    ${payload}    motivoRetificacao=Correção
    ${primeira}=    POST Retificar Evolucao Enfermagem    ${evolucaoId}    ${payload}
    Should Be Equal As Integers    ${primeira.status_code}    201

    ${response}=    POST Retificar Evolucao Enfermagem    ${evolucaoId}    ${payload}
    Should Be Equal As Integers    ${response.status_code}    422
    Validate Object Schema    ${response.json()}    ${ERROR_RESPONSE_SCHEMA}

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Evolucao Enfermagem - Retificacao - POST - By HTTP Status Code
    [Documentation]    Executes the appropriate Retificar Evolucao Enfermagem test scenario based on the provided HTTP status code.
    [Arguments]    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Run Keyword If    '${meuHTTP}' == '201'    POST Retificar Evolucao Enfermagem - Status Code 201
    ...    ELSE IF    '${meuHTTP}' == '404'    POST Retificar Evolucao Enfermagem - Status Code 404
    ...    ELSE IF    '${meuHTTP}' == '422'    POST Retificar Evolucao Enfermagem - Status Code 422 - Versao Ja Retificada
    ...    ELSE    Fail    HTTP status code '${meuHTTP}' is not supported for Retificar Evolucao Enfermagem test scenario.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

