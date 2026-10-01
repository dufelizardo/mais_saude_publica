*** Settings ***
Library     String
Library     Collections
Resource    ../../../pom/solicitacao_regulacao/get_fila_regulacao/get_fila_regulacao_pom.resource
Resource    ../create_solicitacao_regulacao/create_solicitacao_regulacao_flow.resource
Resource    ../../common/schema_validation_flow.resource
Resource    ../../../resource/config/schema/schema.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
    Fila: prioridade (vermelho primeiro) e hora do pedido, com a posição.
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
GET Fila Regulacao - Status Code 200
    [Documentation]    Puts an AZUL and then a VERMELHO request in the same queue and checks the order.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${azul}    ${matricula}    ${procedimentoId}    ${pacienteId}    ${unidadeId}=    Seed A Solicitacao Regulacao    AZUL
    ${outroPaciente}    ${cpf}    ${cartaoSus}    ${nome}=    Seed A Paciente
    ${payload}=    Build Solicitacao Regulacao Payload    ${outroPaciente}    ${procedimentoId}    ${unidadeId}    ${matricula}    VERMELHO
    ${criada}=    POST Create Solicitacao Regulacao    ${payload}
    Should Be Equal As Integers    ${criada.status_code}    201
    ${fila}=    GET Fila Regulacao Do Procedimento    ${procedimentoId}
    Should Be Equal As Integers    ${fila.status_code}    200
    Validate Array Schema    ${fila.json()}    ${SOLICITACAO_REGULACAO_RESUMO_SCHEMA}
    Length Should Be    ${fila.json()}    2
    Should Be Equal    ${fila.json()}[0][prioridade]    VERMELHO
    Should Be Equal    ${fila.json()}[1][uuid]    ${azul}
    Should Be Equal As Integers    ${fila.json()}[1][posicaoNaFila]    2
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Fila Regulacao - GET - By HTTP Status Code
    [Documentation]    Executes the Fila Regulacao - GET scenario for the given HTTP status code.
    [Arguments]    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Run Keyword If    '${meuHTTP}' == '200'    GET Fila Regulacao - Status Code 200
    ...    ELSE    Fail    HTTP status code '${meuHTTP}' is not supported for Fila Regulacao - GET test scenario.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
