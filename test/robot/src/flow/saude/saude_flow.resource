*** Settings ***
Library     Collections
Resource    ../../pom/saude/saude_pom.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
Saude Da Aplicacao - Health
    [Documentation]    The app health answers UP, with no details.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${r}=    GET Saude Da Aplicacao    /actuator/health
    Should Be Equal As Integers    ${r.status_code}    200
    Should Be Equal    ${r.json()}[status]    UP
    Dictionary Should Not Contain Key    ${r.json()}    components
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Saude Da Aplicacao - Liveness
    [Documentation]    The liveness group answers UP.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${r}=    GET Saude Da Aplicacao    /actuator/health/liveness
    Should Be Equal As Integers    ${r.status_code}    200
    Should Be Equal    ${r.json()}[status]    UP
    Dictionary Should Not Contain Key    ${r.json()}    components
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Saude Da Aplicacao - Readiness
    [Documentation]    The readiness group (with the database) answers UP.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${r}=    GET Saude Da Aplicacao    /actuator/health/readiness
    Should Be Equal As Integers    ${r.status_code}    200
    Should Be Equal    ${r.json()}[status]    UP
    Dictionary Should Not Contain Key    ${r.json()}    components
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Saude Da Aplicacao - Env Fechado
    [Documentation]    The rest of the Actuator is not exposed.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${r}=    GET Saude Da Aplicacao    /actuator/env
    Should Not Be Equal As Integers    ${r.status_code}    200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Saude Da Aplicacao - By Case
    [Documentation]    Executes the health scenario for the given case.
    [Arguments]    ${caso}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Run Keyword If    '${caso}' == '200-health'    Saude Da Aplicacao - Health
    ...    ELSE IF    '${caso}' == '200-liveness'    Saude Da Aplicacao - Liveness
    ...    ELSE IF    '${caso}' == '200-readiness'    Saude Da Aplicacao - Readiness
    ...    ELSE IF    '${caso}' == 'fechado-env'    Saude Da Aplicacao - Env Fechado
    ...    ELSE    Fail    Case '${caso}' is not supported for Saude Da Aplicacao test scenario.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
