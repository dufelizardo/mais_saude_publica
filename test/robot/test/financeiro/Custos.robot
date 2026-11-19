*** Settings ***
Resource    ../../src/scenario/financeiro/custos_scenario.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
Metadata    Test Suite - Custos
Metadata    Test Suite Description        Centros de custo, apuração mensal, rateio do PNGC e indicadores de custo (ADR-0220, ADR-0221).
Metadata    Test Suite Owner              Eduardo Felizardo
Metadata    Test Suite Version            1.0
Metadata    Test Suite Tags               POST    PUT    GET    Custos    MaisSaudePublicaAPI
Metadata    Test Suite Created On         2026-11-19
Metadata    Test Suite Last Modified      2026-11-19
Metadata    Project                       Layered Keyword Driven Framework (LKDF)
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Variables ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Test Cases ***
CT-001 - Validate Centros De Custo - HTTP 201 409 404 E 422
    [Documentation]    O geral da unidade, o do setor, o repetido 409, o setor inexistente 404 e o geral não se desativa.
    [Tags]    Custos    HTTP201    HTTP409    HTTP404    HTTP422
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    CUSTOS    CENTROS
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-002 - Validate Apuracao Da Competencia - HTTP 422 400 200 E 404
    [Documentation]    O mês corrente 422; o formato 400; apurar, refazer, fechar e não mudar mais.
    [Tags]    Custos    HTTP422    HTTP400    HTTP200    HTTP404
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    CUSTOS    APURACAO
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-003 - Validate Rateio E Lotacao No Setor - HTTP 201 200 422 E 400
    [Documentation]    O critério padrão e o informado, o produtivo sem critério 422, a área negativa 400, a lotação no setor da unidade 201 e
    ...    no de outra unidade 422, e a apuração com o não rateado.
    [Tags]    Custos    Rateio    HTTP201    HTTP200    HTTP422    HTTP400
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    CUSTOS    RATEIO
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
