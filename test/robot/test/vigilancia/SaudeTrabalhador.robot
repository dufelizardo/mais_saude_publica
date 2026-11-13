*** Settings ***
Resource    ../../src/scenario/vigilancia/saude_trabalhador_scenario.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
Metadata    Test Suite - Saúde do Trabalhador
Metadata    Test Suite Description        O bloco do trabalho na notificação, o acompanhamento sorológico e o acidente do servidor (ADR-0207).
Metadata    Test Suite Owner              Eduardo Felizardo
Metadata    Test Suite Version            1.0
Metadata    Test Suite Tags               POST    PUT    GET    SaudeTrabalhador    MaisSaudePublicaAPI
Metadata    Test Suite Created On         2026-11-13
Metadata    Test Suite Last Modified      XXXX-XX-XX
Metadata    Project                       Layered Keyword Driven Framework (LKDF)
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Variables ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Test Cases ***
CT-001 - Validate Acidente Grave Com O Bloco Do Trabalho - HTTP 201
    [Documentation]    Sem o bloco, 400; com ele, 201 e o CNPJ só com os dígitos.
    [Tags]    POST    SaudeTrabalhador    HTTP201    HTTP400
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    SAUDE DO TRABALHADOR    GRAVE
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-002 - Validate Dado Do Acidente Fora Do Acidente - HTTP 400
    [Documentation]    O óbito na LER/DORT é recusado.
    [Tags]    POST    SaudeTrabalhador    HTTP400
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    SAUDE DO TRABALHADOR    FORA_DO_ACIDENTE
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-003 - Validate Acompanhamento Sorologico - HTTP 200
    [Documentation]    A etapa de 6 semanas vencida; no futuro, 400; registrada hoje, feita.
    [Tags]    POST    SaudeTrabalhador    HTTP200    HTTP400
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    SAUDE DO TRABALHADOR    SOROLOGICO
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-004 - Validate Painel Da Saude Do Trabalhador - HTTP 200
    [Documentation]    O painel da unidade com o acidente e o empregador.
    [Tags]    GET    SaudeTrabalhador    HTTP200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    SAUDE DO TRABALHADOR    PAINEL
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-005 - Validate Correcao Pela Investigacao - HTTP 200
    [Documentation]    A investigação corrige a CAT; CBO errado, 400.
    [Tags]    PUT    SaudeTrabalhador    HTTP200    HTTP400
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    SAUDE DO TRABALHADOR    CORRECAO
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-006 - Validate Acidente Do Servidor No RH - HTTP 201
    [Documentation]    O acidente do RH sem notificação, notificado uma vez (409 na segunda) e marcado no RH.
    [Tags]    POST    GET    SaudeTrabalhador    HTTP201    HTTP409
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    SAUDE DO TRABALHADOR    SERVIDOR
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
