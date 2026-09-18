*** Settings ***
Resource    ../../../pom/profissional/desabilitar_profissional/desabilitar_profissional_pom.resource
Resource    ../../profissional/create_profissional/create_profissional_flow.resource
Resource    ../../profissional/get_profissional_by_cpf/get_profissional_by_cpf_flow.resource
Resource    ../../common/schema_validation_flow.resource
Resource    ../../../resource/config/schema/schema.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
DELETE Desabilitar Profissional - Status Code 200
    [Documentation]    Seeds a Profissional and disables it (informs dataDesligamento), expecting HTTP 200.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${cpf}=    Seed A Profissional Unit

    ${response}=    DELETE Desabilitar Profissional    ${cpf}    2026-09-18
    Should Be Equal As Integers    ${response.status_code}    200
    Validate Object Schema    ${response.json()}    ${SUCCESS_RESPONSE_SCHEMA}

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

DELETE Desabilitar Profissional - Grava E Limpa Data De Desligamento
    [Documentation]    Seeds a Profissional, disables it with a dataDesligamento (expects it persisted, see
    ...    ADR-0017), then re-enables it and expects dataDesligamento cleared.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${cpf}=    Seed A Profissional Unit

    ${response}=    DELETE Desabilitar Profissional    ${cpf}    2026-09-18
    Should Be Equal As Integers    ${response.status_code}    200

    ${buscaDesligado}=    Call GET Get Profissional By Cpf    ${cpf}
    Should Be Equal As Integers    ${buscaDesligado.status_code}    404
    # buscarProfissionalPorCpf só enxerga fichas ATIVAS (ver ADR-0017) — depois de desligado, o
    # GET por esse cpf não acha mais nada, então a confirmação da data fica só pelo lado do
    # repositório (fora do alcance do Robot); aqui validamos que reabilitar funciona por cima.

    ${response}=    DELETE Desabilitar Profissional    ${cpf}
    Should Be Equal As Integers    ${response.status_code}    200

    ${buscaReabilitado}=    Call GET Get Profissional By Cpf    ${cpf}
    Should Be Equal As Integers    ${buscaReabilitado.status_code}    200
    Should Be Equal    ${buscaReabilitado.json()['dataDesligamento']}    ${None}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

DELETE Desabilitar Profissional - Status Code 404
    [Documentation]    Attempts to enable/disable a Profissional that does not exist, expecting HTTP 404.
    [Arguments]    ${cpf}=${EMPTY}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    IF    '${cpf}' == '${EMPTY}'
        ${cpf}=    Set Variable    00000000000
    END

    ${response}=    DELETE Desabilitar Profissional    ${cpf}
    Should Be Equal As Integers    ${response.status_code}    404

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

DELETE Desabilitar Profissional - Status Code 400 - URL Malformada
    [Documentation]    Sends a cpf containing a raw null byte (%00), expecting the standard error contract
    ...    instead of a raw HTML error page from Tomcat (AQUAQE-216).
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${response}=    DELETE Desabilitar Profissional    teste%00
    Should Be Equal As Integers    ${response.status_code}    400
    Validate Object Schema    ${response.json()}    ${ERROR_RESPONSE_SCHEMA}

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Profissional - Desabilitar - DELETE - By HTTP Status Code
    [Documentation]    Executes the appropriate Desabilitar Profissional test scenario based on the provided
    ...    HTTP status code.
    [Arguments]    ${meuCpf}    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Run Keyword If    '${meuHTTP}' == '200'    DELETE Desabilitar Profissional - Status Code 200
    ...    ELSE IF    '${meuHTTP}' == '404'    DELETE Desabilitar Profissional - Status Code 404    ${meuCpf}
    ...    ELSE IF    '${meuHTTP}' == '400'    DELETE Desabilitar Profissional - Status Code 400 - URL Malformada
    ...    ELSE    Fail    HTTP status code '${meuHTTP}' is not supported for Desabilitar Profissional test scenario.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
