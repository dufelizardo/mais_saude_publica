*** Settings ***
Resource    ../../../pom/responsabilidade_administrativa/encerrar_responsabilidade_administrativa/encerrar_responsabilidade_administrativa_pom.resource
Resource    ../get_historico_responsabilidade_administrativa/get_historico_responsabilidade_administrativa_flow.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
    Não há endpoint "get all"/"get by id" para Responsabilidade Administrativa — apenas histórico por
    matrícula (ver ADR-0035, mesmo contrato de AjusteIndividual/AdesaoBeneficio no RH). Por isso o uuid
    de uma responsabilidade recém-criada é resolvido via GET histórico, pegando o primeiro item de um
    profissional fresco (que só tem essa responsabilidade).
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
Call PATCH Encerrar Responsabilidade Administrativa
    [Documentation]    Calls the PATCH Encerrar Responsabilidade Administrativa keyword from the POM.
    [Arguments]    ${uuid}    ${dataFim}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${response}=    PATCH Encerrar Responsabilidade Administrativa    ${uuid}    ${dataFim}

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Seed Uuid De Uma Responsabilidade Administrativa
    [Documentation]    Seeds a fresh Responsabilidade Administrativa and resolves its uuid via GET histórico.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${matricula}    ${setorId}=    Seed A Responsabilidade Administrativa
    ${historicoResponse}=    Call GET Get Historico Responsabilidade Administrativa    ${matricula}
    ${uuid}=    Set Variable    ${historicoResponse.json()[0]['uuid']}

    RETURN    ${uuid}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

PATCH Encerrar Responsabilidade Administrativa - Status Code 200
    [Documentation]    Seeds a fresh Responsabilidade Administrativa and encerra it, expecting HTTP 200.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${uuid}=    Seed Uuid De Uma Responsabilidade Administrativa
    ${response}=    Call PATCH Encerrar Responsabilidade Administrativa    ${uuid}    2026-06-30
    Should Be Equal As Integers    ${response.status_code}    200

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

PATCH Encerrar Responsabilidade Administrativa - Status Code 404
    [Documentation]    Attempts to encerrar a Responsabilidade Administrativa identified by a random,
    ...    non-existent uuid, expecting HTTP 404.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${uuidInexistente}=    Evaluate    str(uuid.uuid4())    modules=uuid
    ${response}=    Call PATCH Encerrar Responsabilidade Administrativa    ${uuidInexistente}    2026-06-30
    Should Be Equal As Integers    ${response.status_code}    404

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

PATCH Encerrar Responsabilidade Administrativa - Status Code 409 - Ja Encerrada
    [Documentation]    Encerra uma Responsabilidade Administrativa e tenta encerrar de novo, expecting HTTP 409.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${uuid}=    Seed Uuid De Uma Responsabilidade Administrativa
    Call PATCH Encerrar Responsabilidade Administrativa    ${uuid}    2026-06-30
    ${response}=    Call PATCH Encerrar Responsabilidade Administrativa    ${uuid}    2026-07-31
    Should Be Equal As Integers    ${response.status_code}    409

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Responsabilidade Administrativa - Encerrar - PATCH - By HTTP Status Code
    [Documentation]    Executes the appropriate Encerrar Responsabilidade Administrativa test scenario based on
    ...    the provided HTTP status code.
    [Arguments]    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Run Keyword If    '${meuHTTP}' == '200'    PATCH Encerrar Responsabilidade Administrativa - Status Code 200
    ...    ELSE IF    '${meuHTTP}' == '404'    PATCH Encerrar Responsabilidade Administrativa - Status Code 404
    ...    ELSE IF    '${meuHTTP}' == '409'    PATCH Encerrar Responsabilidade Administrativa - Status Code 409 - Ja Encerrada
    ...    ELSE    Fail    HTTP status code '${meuHTTP}' is not supported for Encerrar Responsabilidade Administrativa test scenario.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
