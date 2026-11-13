*** Settings ***
Resource    ../../src/scenario/vigilancia/saude_trabalhador_scenario.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
Metadata    Test Suite - Investigação do Trabalho
Metadata    Test Suite Description        A investigação do ambiente de trabalho, as medidas, a VISA, o encaminhamento e o risco ocupacional (ADR-0208).
Metadata    Test Suite Owner              Eduardo Felizardo
Metadata    Test Suite Version            1.0
Metadata    Test Suite Tags               POST    PUT    GET    InvestigacaoTrabalho    MaisSaudePublicaAPI
Metadata    Test Suite Created On         2026-11-13
Metadata    Test Suite Last Modified      XXXX-XX-XX
Metadata    Project                       Layered Keyword Driven Framework (LKDF)
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Variables ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Test Cases ***
CT-001 - Validate Ciclo Da Investigacao - HTTP 200
    [Documentation]    A visita, o nexo confirmado e a medida verificada uma vez (422 na segunda).
    [Tags]    PUT    POST    InvestigacaoTrabalho    HTTP200    HTTP422
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    INVESTIGACAO DO TRABALHO    CICLO
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-002 - Validate Sem Bloco E Visita Futura - HTTP 422
    [Documentation]    Sem o bloco do trabalho, 422; a visita no futuro, 400.
    [Tags]    PUT    InvestigacaoTrabalho    HTTP422    HTTP400
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    INVESTIGACAO DO TRABALHO    SEM_BLOCO
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-003 - Validate Comunicacao A Vigilancia Sanitaria - HTTP 201
    [Documentation]    A denúncia contra o empregador; a segunda, 409.
    [Tags]    POST    InvestigacaoTrabalho    HTTP201    HTTP409
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    INVESTIGACAO DO TRABALHO    VISA
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-004 - Validate Encaminhamento Intersetorial - HTTP 201
    [Documentation]    O encaminhamento à Superintendência do Trabalho.
    [Tags]    POST    InvestigacaoTrabalho    HTTP201
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    INVESTIGACAO DO TRABALHO    ENCAMINHAMENTO
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-005 - Validate Risco Ocupacional - HTTP 201
    [Documentation]    O risco herda o empregador; controlado uma vez; sem empregador, 400.
    [Tags]    POST    InvestigacaoTrabalho    HTTP201    HTTP400    HTTP422
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    INVESTIGACAO DO TRABALHO    RISCO
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-006 - Validate Semana E Relatorio - HTTP 200
    [Documentation]    A semana da Sala de Situação e o indicador do relatório Vigilância.
    [Tags]    GET    InvestigacaoTrabalho    HTTP200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    INVESTIGACAO DO TRABALHO    SEMANA
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
