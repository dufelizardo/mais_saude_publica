*** Settings ***
Library     String
Resource    ../../../pom/auditoria/alerta_auditoria/alerta_auditoria_pom.resource
Resource    ../../auth/login/login_flow.resource
Resource    ../../common/schema_validation_flow.resource
Resource    ../../../resource/config/schema/schema.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
    O alerta de apoio nasce de logins recusados em sequência (regra LOGIN_RECUSADO, que vale com o login desligado
    também) e da detecção sob demanda.
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
Seed Um Alerta De Login Recusado
    [Documentation]    Fails five logins with a random CPF, runs the detection and returns the alert uuid and the CPF.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${cpf}=    Generate Random String    11    [NUMBERS]
    FOR    ${i}    IN RANGE    5
        ${payload}=    Build Login Payload    tipo=CPF    identificador=${cpf}    senha=SenhaErrada${i}
        ${login}=    POST Login    ${payload}
        Should Be Equal As Integers    ${login.status_code}    401
    END
    ${deteccao}=    POST Detectar Alertas Auditoria
    Should Be Equal As Integers    ${deteccao.status_code}    200
    ${params}=    Create Dictionary    tipo=LOGIN_RECUSADO
    ${lista}=    GET Listar Alertas Auditoria    ${params}
    ${uuids}=    Evaluate    [a["uuid"] for a in $lista.json() if a["sujeito"] == $cpf]
    Length Should Be    ${uuids}    1
    RETURN    ${uuids}[0]    ${cpf}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Alerta Auditoria - Login Recusado Vira Alerta - Status Code 200
    [Documentation]    Five refused logins become one LOGIN_RECUSADO alert, high severity, with the CPF as subject.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${uuid}    ${cpf}=    Seed Um Alerta De Login Recusado
    ${response}=    GET Alerta Auditoria By Id    ${uuid}
    Should Be Equal As Integers    ${response.status_code}    200
    Validate Object Schema    ${response.json()}    ${ALERTA_AUDITORIA_SCHEMA}
    Should Be Equal    ${response.json()}[tipo]    LOGIN_RECUSADO
    Should Be Equal    ${response.json()}[severidade]    ALTA
    Should Be Equal    ${response.json()}[status]    ABERTO
    Should Be Equal As Integers    ${response.json()}[quantidade]    5
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Alerta Auditoria - Listar E Resumo - Status Code 200
    [Documentation]    Lists open alerts and reads the counters.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${params}=    Create Dictionary    status=ABERTO
    ${lista}=    GET Listar Alertas Auditoria    ${params}
    Should Be Equal As Integers    ${lista.status_code}    200
    Validate Array Schema    ${lista.json()}    ${ALERTA_AUDITORIA_SCHEMA}
    ${resumo}=    GET Resumo Alertas Auditoria
    Should Be Equal As Integers    ${resumo.status_code}    200
    Validate Object Schema    ${resumo.json()}    ${RESUMO_ALERTAS_AUDITORIA_SCHEMA}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Alerta Auditoria - Detalhe - Status Code 404
    [Documentation]    Fetches an alert that does not exist.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${uuid}=    Evaluate    str(uuid.uuid4())    modules=uuid
    ${response}=    GET Alerta Auditoria By Id    ${uuid}
    Should Be Equal As Integers    ${response.status_code}    404
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Alerta Auditoria - Analise - Status Code 200
    [Documentation]    Concludes the alert as procedente; a second analysis is refused with 422.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${uuid}    ${cpf}=    Seed Um Alerta De Login Recusado
    ${payload}=    Create Dictionary    conclusao=PROCEDENTE    parecer=Tentativa de adivinhar a senha, CPF bloqueado.
    ${response}=    POST Analisar Alerta Auditoria    ${uuid}    ${payload}
    Should Be Equal As Integers    ${response.status_code}    200
    Should Be Equal    ${response.json()}[status]    PROCEDENTE
    Should Not Be Empty    ${response.json()}[analisadoEm]
    ${repetida}=    POST Analisar Alerta Auditoria    ${uuid}    ${payload}
    Should Be Equal As Integers    ${repetida.status_code}    422
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Alerta Auditoria - Analise - Status Code 400 - Parecer Curto
    [Documentation]    Concludes with an opinion shorter than 10 characters.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${uuid}    ${cpf}=    Seed Um Alerta De Login Recusado
    ${payload}=    Create Dictionary    conclusao=IMPROCEDENTE    parecer=ok
    ${response}=    POST Analisar Alerta Auditoria    ${uuid}    ${payload}
    Should Be Equal As Integers    ${response.status_code}    400
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Alerta Auditoria - By HTTP Status Code
    [Documentation]    Executes the alert scenario for the given HTTP status code.
    [Arguments]    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Run Keyword If    '${meuHTTP}' == '200-login'    Alerta Auditoria - Login Recusado Vira Alerta - Status Code 200
    ...    ELSE IF    '${meuHTTP}' == '200-lista'    Alerta Auditoria - Listar E Resumo - Status Code 200
    ...    ELSE IF    '${meuHTTP}' == '404'    Alerta Auditoria - Detalhe - Status Code 404
    ...    ELSE IF    '${meuHTTP}' == '200-analise'    Alerta Auditoria - Analise - Status Code 200
    ...    ELSE IF    '${meuHTTP}' == '400-analise'    Alerta Auditoria - Analise - Status Code 400 - Parecer Curto
    ...    ELSE    Fail    HTTP status code '${meuHTTP}' is not supported for Alerta Auditoria test scenario.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
