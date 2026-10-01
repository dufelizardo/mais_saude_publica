*** Settings ***
Library    RequestsLibrary
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
    Caixa de e-mail de teste (Mailpit) dos testes de recuperação de senha (ADR-0081). MSP_MAILPIT_URL é a API HTTP dele (ex.: http://localhost:8025); vazio = sem caixa de teste.
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Variables ***
${MAILPIT_URL}    %{MSP_MAILPIT_URL=}
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
GET Mensagens Para
    [Documentation]    Busca as mensagens enviadas para o endereço e devolve a resposta.
    [Arguments]    ${email}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Create Session    mailpit    ${MAILPIT_URL}    verify=False
    ${response}=    GET On Session    mailpit    /api/v1/search    params=query=to:${email}    expected_status=ANY
    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

GET Mensagem
    [Documentation]    Lê uma mensagem pelo ID e devolve a resposta.
    [Arguments]    ${id}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Create Session    mailpit    ${MAILPIT_URL}    verify=False
    ${response}=    GET On Session    mailpit    /api/v1/message/${id}    expected_status=ANY
    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
