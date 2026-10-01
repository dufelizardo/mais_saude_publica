*** Settings ***
Resource    ../../../pom/triagem/retificar_triagem/retificar_triagem_pom.resource
Resource    ../create_triagem/create_triagem_flow.resource
Resource    ../get_triagem_by_id/get_triagem_by_id_flow.resource
Resource    ../../common/schema_validation_flow.resource
Resource    ../../../resource/config/schema/schema.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
    Registro clínico não é editado (ADR-0062): a retificação cria uma nova versão ligada à anterior, que
    continua existindo e passa a ser marcada como retificada. Só a versão vigente pode ser retificada.
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
POST Retificar Triagem - Status Code 201
    [Documentation]    Seeds a fresh Triagem, retifies it and checks the original is kept and flagged as retificado.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${triagemId}    ${atendimentoId}    ${matricula}=    Seed Uma Triagem
    ${payload}=    Build Triagem Payload    ${atendimentoId}    ${matricula}    classificacaoRisco=VERMELHO
    Set To Dictionary    ${payload}    motivoRetificacao=Valor registrado errado

    ${response}=    POST Retificar Triagem    ${triagemId}    ${payload}
    Should Be Equal As Integers    ${response.status_code}    201
    Validate Object Schema    ${response.json()}    ${SUCCESS_RESPONSE_SCHEMA}

    ${original}=    Call GET Get Triagem By Id    ${triagemId}
    Should Be Equal As Integers    ${original.status_code}    200
    Should Be True    ${original.json()}[retificado]
    Should Not Be Empty    ${original.json()}[retificadoPorUuid]

    ${nova}=    Call GET Get Triagem By Id    ${original.json()}[retificadoPorUuid]
    Should Be Equal As Strings    ${nova.json()}[classificacaoRisco]    VERMELHO
    Should Be Equal As Strings    ${nova.json()}[retificacaoDeUuid]    ${triagemId}

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

POST Retificar Triagem - Status Code 404
    [Documentation]    Attempts to retify a Triagem identified by a random, non-existent uuid, expecting HTTP 404.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${atendimentoId}    ${pacienteId}    ${matricula}    ${unidadeId}=    Seed Um Atendimento
    ${payload}=    Build Triagem Payload    ${atendimentoId}    ${matricula}
    ${uuidInexistente}=    Evaluate    str(uuid.uuid4())    modules=uuid
    Set To Dictionary    ${payload}    motivoRetificacao=Correção

    ${response}=    POST Retificar Triagem    ${uuidInexistente}    ${payload}
    Should Be Equal As Integers    ${response.status_code}    404

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

POST Retificar Triagem - Status Code 422 - Versao Ja Retificada
    [Documentation]    Retifies the same Triagem twice: the second attempt targets a version that is no longer current,
    ...    expecting HTTP 422.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${triagemId}    ${atendimentoId}    ${matricula}=    Seed Uma Triagem
    ${payload}=    Build Triagem Payload    ${atendimentoId}    ${matricula}    classificacaoRisco=VERMELHO
    Set To Dictionary    ${payload}    motivoRetificacao=Correção
    ${primeira}=    POST Retificar Triagem    ${triagemId}    ${payload}
    Should Be Equal As Integers    ${primeira.status_code}    201

    ${response}=    POST Retificar Triagem    ${triagemId}    ${payload}
    Should Be Equal As Integers    ${response.status_code}    422
    Validate Object Schema    ${response.json()}    ${ERROR_RESPONSE_SCHEMA}

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Triagem - Retificacao - POST - By HTTP Status Code
    [Documentation]    Executes the appropriate Retificar Triagem test scenario based on the provided HTTP status code.
    [Arguments]    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Run Keyword If    '${meuHTTP}' == '201'    POST Retificar Triagem - Status Code 201
    ...    ELSE IF    '${meuHTTP}' == '404'    POST Retificar Triagem - Status Code 404
    ...    ELSE IF    '${meuHTTP}' == '422'    POST Retificar Triagem - Status Code 422 - Versao Ja Retificada
    ...    ELSE    Fail    HTTP status code '${meuHTTP}' is not supported for Retificar Triagem test scenario.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

