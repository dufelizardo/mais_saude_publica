*** Settings ***
Resource    ../common/mais_saude_publica_common.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
    Saúde do Trabalhador, fatia V5a da Vigilância (ADR-0207).
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
PUT Trabalho Da Notificacao
    [Documentation]    Completes or corrects the work block of the notification.
    [Arguments]    ${notificacao}    ${payload}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Get Mais Saude Publica Session
    ${response}=    PUT On Session    msp_session    ${BASE_URL}/vigilancia/notificacao/${notificacao}/trabalho    json=${payload}    expected_status=ANY
    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

POST Acompanhamento Sorologico
    [Documentation]    Registers a step of the serological follow-up.
    [Arguments]    ${notificacao}    ${payload}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Get Mais Saude Publica Session
    ${response}=    POST On Session    msp_session    ${BASE_URL}/vigilancia/notificacao/${notificacao}/trabalho/acompanhamento    json=${payload}    expected_status=ANY
    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

GET Painel Saude Do Trabalhador
    [Documentation]    The worker health panel of the unit.
    [Arguments]    ${unidade}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Get Mais Saude Publica Session
    ${params}=    Create Dictionary    unidadeId=${unidade}
    ${response}=    GET On Session    msp_session    ${BASE_URL}/vigilancia/saude-trabalhador/painel    params=${params}    expected_status=ANY
    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

GET Acidentes Sem Notificacao
    [Documentation]    The accidents of civil servants without a notification.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Get Mais Saude Publica Session
    ${response}=    GET On Session    msp_session    ${BASE_URL}/vigilancia/saude-trabalhador/acidentes-sem-notificacao    expected_status=ANY
    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

POST Acidente De Trabalho Do RH
    [Documentation]    Registers the work accident of a civil servant in HR.
    [Arguments]    ${payload}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Get Mais Saude Publica Session
    ${response}=    POST On Session    msp_session    ${BASE_URL}/acidente-trabalho/    json=${payload}    expected_status=ANY
    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

GET Acidentes Do Profissional
    [Documentation]    The HR accident history of the professional.
    [Arguments]    ${matricula}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Get Mais Saude Publica Session
    ${response}=    GET On Session    msp_session    ${BASE_URL}/acidente-trabalho/profissional/${matricula}    expected_status=ANY
    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
