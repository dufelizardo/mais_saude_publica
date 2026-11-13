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

PUT Investigacao Do Trabalho
    [Documentation]    Records the workplace visit (ADR-0208).
    [Arguments]    ${notificacao}    ${payload}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Get Mais Saude Publica Session
    ${response}=    PUT On Session    msp_session    ${BASE_URL}/vigilancia/notificacao/${notificacao}/trabalho/investigacao    json=${payload}    expected_status=ANY
    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

POST Nexo Do Trabalho
    [Documentation]    Concludes the work-relatedness.
    [Arguments]    ${notificacao}    ${payload}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Get Mais Saude Publica Session
    ${response}=    POST On Session    msp_session    ${BASE_URL}/vigilancia/notificacao/${notificacao}/trabalho/investigacao/nexo    json=${payload}    expected_status=ANY
    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

POST Medida Do Trabalho
    [Documentation]    Recommends a measure.
    [Arguments]    ${notificacao}    ${payload}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Get Mais Saude Publica Session
    ${response}=    POST On Session    msp_session    ${BASE_URL}/vigilancia/notificacao/${notificacao}/trabalho/investigacao/medida    json=${payload}    expected_status=ANY
    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

POST Verificacao Da Medida Do Trabalho
    [Documentation]    Verifies a measure.
    [Arguments]    ${medida}    ${payload}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Get Mais Saude Publica Session
    ${response}=    POST On Session    msp_session    ${BASE_URL}/vigilancia/medida-trabalho/${medida}/verificacao    json=${payload}    expected_status=ANY
    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

POST Comunicacao Visa
    [Documentation]    Reports the employer to the health inspection.
    [Arguments]    ${notificacao}    ${payload}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Get Mais Saude Publica Session
    ${response}=    POST On Session    msp_session    ${BASE_URL}/vigilancia/notificacao/${notificacao}/trabalho/investigacao/comunicacao-visa    json=${payload}    expected_status=ANY
    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

POST Encaminhamento Do Trabalho
    [Documentation]    Intersectoral referral from the investigation.
    [Arguments]    ${notificacao}    ${payload}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Get Mais Saude Publica Session
    ${response}=    POST On Session    msp_session    ${BASE_URL}/vigilancia/notificacao/${notificacao}/trabalho/investigacao/encaminhamento    json=${payload}    expected_status=ANY
    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

POST Risco Ocupacional
    [Documentation]    Registers an occupational risk.
    [Arguments]    ${payload}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Get Mais Saude Publica Session
    ${response}=    POST On Session    msp_session    ${BASE_URL}/vigilancia/saude-trabalhador/risco    json=${payload}    expected_status=ANY
    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

POST Controle Do Risco Ocupacional
    [Documentation]    Marks the risk as controlled.
    [Arguments]    ${risco}    ${payload}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Get Mais Saude Publica Session
    ${response}=    POST On Session    msp_session    ${BASE_URL}/vigilancia/risco-ocupacional/${risco}/controle    json=${payload}    expected_status=ANY
    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

GET Semana Da Saude Do Trabalhador
    [Documentation]    The worker health week for the situation room.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Get Mais Saude Publica Session
    ${response}=    GET On Session    msp_session    ${BASE_URL}/vigilancia/saude-trabalhador/semana    expected_status=ANY
    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

GET Relatorio Vigilancia
    [Documentation]    The surveillance report of the unit, last 7 days.
    [Arguments]    ${unidade}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Get Mais Saude Publica Session
    ${de}=    Evaluate    (datetime.date.today() - datetime.timedelta(days=7)).isoformat()    modules=datetime
    ${ate}=    Evaluate    datetime.date.today().isoformat()    modules=datetime
    ${params}=    Create Dictionary    de=${de}    ate=${ate}    unidadeId=${unidade}
    ${response}=    GET On Session    msp_session    ${BASE_URL}/relatorios/VIGILANCIA    params=${params}    expected_status=ANY
    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
