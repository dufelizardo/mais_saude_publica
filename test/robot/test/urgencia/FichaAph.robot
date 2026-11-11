*** Settings ***
Resource    ../../src/scenario/urgencia/ficha_aph_scenario.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
Metadata    Test Suite - PUT/POST/GET Ficha Do APH
Metadata    Test Suite Description        Ficha clínica do atendimento pré-hospitalar na ocorrência de urgência (ADR-0188).
Metadata    Test Suite Owner              Eduardo Felizardo
Metadata    Test Suite Version            1.0
Metadata    Test Suite Tags               PUT    POST    GET    FichaAph    MaisSaudePublicaAPI
Metadata    Test Suite Created On         2026-11-10
Metadata    Test Suite Last Modified      XXXX-XX-XX
Metadata    Project                       Layered Keyword Driven Framework (LKDF)
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Variables ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Test Cases ***
CT-001 - Validate PUT FichaAph - HTTP 422 E 200 (So Com Recurso)
    [Documentation]    Sem recurso enviado não há ficha; com recurso, abre e aparece na lista da equipe.
    [Tags]    PUT    GET    FichaAph    HTTP422    HTTP200
    FICHA DO APH    SO_COM_RECURSO

CT-002 - Validate Glasgow E Sinais Vitais - HTTP 400 E 200
    [Documentation]    Glasgow e sinais fora da faixa dão 400; o total é calculado; a série fica guardada.
    [Tags]    PUT    POST    FichaAph    HTTP400    HTTP200
    FICHA DO APH    GLASGOW_SINAIS

CT-003 - Validate Finalizacao E Adendo - HTTP 422 E 200
    [Documentation]    Finalizar pede o desfecho e quem recebeu; finalizada, só adendo; PDF.
    [Tags]    PUT    POST    GET    FichaAph    HTTP422    HTTP200
    FICHA DO APH    FINALIZAR_ADENDO

CT-004 - Validate Obito No Local - HTTP 422 E 200
    [Documentation]    O óbito no local pede a hora e o médico que constatou.
    [Tags]    PUT    POST    FichaAph    HTTP422    HTTP200
    FICHA DO APH    OBITO
