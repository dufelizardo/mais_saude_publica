*** Settings ***
Resource    ../../../pom/necessidade_de_pessoal/vincular_vaga_necessidade_de_pessoal/vincular_vaga_necessidade_de_pessoal_pom.resource
Resource    ../create_necessidade_de_pessoal/create_necessidade_de_pessoal_flow.resource
Resource    ../get_necessidade_de_pessoal_by_id/get_necessidade_de_pessoal_by_id_flow.resource
Resource    ../../vaga/create_vaga/create_vaga_flow.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
    Vincular a Vaga é um link informativo, nunca um gatilho automático (ver ADR-0036) — mesmo espírito
    do "encerrar" de AdesaoBeneficio/ResponsabilidadeAdministrativa: uma ação que só acontece uma vez
    (409 se a necessidade já tiver uma vaga associada).
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
Call PATCH Vincular Vaga Necessidade De Pessoal
    [Documentation]    Calls the PATCH Vincular Vaga Necessidade De Pessoal keyword from the POM.
    [Arguments]    ${uuid}    ${vagaId}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${response}=    PATCH Vincular Vaga Necessidade De Pessoal    ${uuid}    ${vagaId}

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

PATCH Vincular Vaga Necessidade De Pessoal - Status Code 200
    [Documentation]    Seeds a fresh Necessidade De Pessoal and Vaga (mesma unidade+cargo) and vincula uma à
    ...    outra, confirmando via GET.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${necessidadeId}    ${unidadeId}    ${cargoId}=    Seed A Necessidade De Pessoal
    ${vagaId}    ${unidadeId2}    ${cargoId2}=    Seed A Vaga Uuid    ${unidadeId}    ${cargoId}

    ${response}=    Call PATCH Vincular Vaga Necessidade De Pessoal    ${necessidadeId}    ${vagaId}
    Should Be Equal As Integers    ${response.status_code}    200

    ${getResponse}=    Call GET Get Necessidade De Pessoal By Id    ${necessidadeId}
    Should Be Equal As Integers    ${getResponse.status_code}    200
    Should Be Equal As Strings    ${getResponse.json()['vagaAssociadaUuid']}    ${vagaId}

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

PATCH Vincular Vaga Necessidade De Pessoal - Status Code 404
    [Documentation]    Attempts to vincular a vagaId that doesn't exist to a fresh Necessidade De Pessoal,
    ...    expecting HTTP 404.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${necessidadeId}    ${unidadeId}    ${cargoId}=    Seed A Necessidade De Pessoal
    ${vagaIdInexistente}=    Evaluate    str(uuid.uuid4())    modules=uuid

    ${response}=    Call PATCH Vincular Vaga Necessidade De Pessoal    ${necessidadeId}    ${vagaIdInexistente}
    Should Be Equal As Integers    ${response.status_code}    404

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

PATCH Vincular Vaga Necessidade De Pessoal - Status Code 409 - Ja Vinculada
    [Documentation]    Vincula uma Vaga a uma Necessidade De Pessoal e tenta vincular outra Vaga em seguida,
    ...    expecting HTTP 409.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${necessidadeId}    ${unidadeId}    ${cargoId}=    Seed A Necessidade De Pessoal
    ${primeiraVagaId}    ${unidadeId2}    ${cargoId2}=    Seed A Vaga Uuid    ${unidadeId}    ${cargoId}
    ${segundaVagaId}    ${unidadeId3}    ${cargoId3}=    Seed A Vaga Uuid    ${unidadeId}    ${cargoId}

    Call PATCH Vincular Vaga Necessidade De Pessoal    ${necessidadeId}    ${primeiraVagaId}
    ${response}=    Call PATCH Vincular Vaga Necessidade De Pessoal    ${necessidadeId}    ${segundaVagaId}
    Should Be Equal As Integers    ${response.status_code}    409

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Necessidade De Pessoal - Vincular Vaga - PATCH - By HTTP Status Code
    [Documentation]    Executes the appropriate Vincular Vaga Necessidade De Pessoal test scenario based on the
    ...    provided HTTP status code.
    [Arguments]    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Run Keyword If    '${meuHTTP}' == '200'    PATCH Vincular Vaga Necessidade De Pessoal - Status Code 200
    ...    ELSE IF    '${meuHTTP}' == '404'    PATCH Vincular Vaga Necessidade De Pessoal - Status Code 404
    ...    ELSE IF    '${meuHTTP}' == '409'    PATCH Vincular Vaga Necessidade De Pessoal - Status Code 409 - Ja Vinculada
    ...    ELSE    Fail    HTTP status code '${meuHTTP}' is not supported for Vincular Vaga Necessidade De Pessoal test scenario.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
