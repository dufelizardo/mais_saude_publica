*** Settings ***
Resource    ../../../pom/administracao_medicamento/retificar_administracao_medicamento/retificar_administracao_medicamento_pom.resource
Resource    ../create_administracao_medicamento/create_administracao_medicamento_flow.resource
Resource    ../../common/schema_validation_flow.resource
Resource    ../../../resource/config/schema/schema.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
    Retificação (ADR-0062): a baixa anterior é estornada antes da nova, e só a versão vigente pode ser retificada.
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
POST Retificar Administracao Medicamento - Status Code 201
    [Documentation]    Administers 2 units, retifies to 3 and checks the Lote balance is 97 (2 returned, 3 taken).
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${atendimentoId}    ${consultaId}    ${medicamentoId}    ${loteId}    ${matricula}=    Seed Uma Prescricao Com Estoque
    ${payload}=    Build Administracao Payload    ${atendimentoId}    ${consultaId}    ${medicamentoId}    ${loteId}    ${matricula}
    ${criada}=    POST Create Administracao Medicamento    ${payload}
    Should Be Equal As Integers    ${criada.status_code}    201
    ${uuid}=    Buscar Administracao Uuid Por Atendimento    ${atendimentoId}
    ${correcao}=    Build Administracao Payload    ${atendimentoId}    ${consultaId}    ${medicamentoId}    ${loteId}    ${matricula}    quantidade=${3}
    Set To Dictionary    ${correcao}    motivoRetificacao=Foram três ampolas

    ${response}=    POST Retificar Administracao Medicamento    ${uuid}    ${correcao}
    Should Be Equal As Integers    ${response.status_code}    201
    Validate Object Schema    ${response.json()}    ${SUCCESS_RESPONSE_SCHEMA}

    ${lote}=    Call GET Get Lote By Id    ${loteId}
    Should Be Equal As Integers    ${lote.json()['quantidade']}    97
    ${original}=    GET Get Administracao Medicamento By Id    ${uuid}
    Should Be True    ${original.json()}[retificado]

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

POST Retificar Administracao Medicamento - Status Code 422 - Versao Ja Retificada
    [Documentation]    Retifies the same version twice: the second attempt targets a version that is no longer current,
    ...    expecting HTTP 422.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${atendimentoId}    ${consultaId}    ${medicamentoId}    ${loteId}    ${matricula}=    Seed Uma Prescricao Com Estoque
    ${payload}=    Build Administracao Payload    ${atendimentoId}    ${consultaId}    ${medicamentoId}    ${loteId}    ${matricula}
    ${criada}=    POST Create Administracao Medicamento    ${payload}
    Should Be Equal As Integers    ${criada.status_code}    201
    ${uuid}=    Buscar Administracao Uuid Por Atendimento    ${atendimentoId}
    ${correcao}=    Build Administracao Payload    ${atendimentoId}    ${consultaId}    ${medicamentoId}    ${loteId}    ${matricula}    quantidade=${3}
    Set To Dictionary    ${correcao}    motivoRetificacao=Foram três ampolas

    ${primeira}=    POST Retificar Administracao Medicamento    ${uuid}    ${correcao}
    Should Be Equal As Integers    ${primeira.status_code}    201
    ${response}=    POST Retificar Administracao Medicamento    ${uuid}    ${correcao}
    Should Be Equal As Integers    ${response.status_code}    422
    Validate Object Schema    ${response.json()}    ${ERROR_RESPONSE_SCHEMA}

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Administracao Medicamento - Retificacao - POST - By HTTP Status Code
    [Documentation]    Executes the appropriate Retificar Administracao Medicamento test scenario based on the provided HTTP
    ...    status code.
    [Arguments]    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Run Keyword If    '${meuHTTP}' == '201'    POST Retificar Administracao Medicamento - Status Code 201
    ...    ELSE IF    '${meuHTTP}' == '422'    POST Retificar Administracao Medicamento - Status Code 422 - Versao Ja Retificada
    ...    ELSE    Fail    HTTP status code '${meuHTTP}' is not supported for Retificar Administracao Medicamento test scenario.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

