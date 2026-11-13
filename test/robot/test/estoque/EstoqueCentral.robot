*** Settings ***
Resource    ../../src/scenario/estoque/central_estoque_scenario.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
Metadata    Test Suite - POST/GET Estoque Central
Metadata    Test Suite Description        Almoxarifado central, requisição, transferência e inventário (ADR-0189).
Metadata    Test Suite Owner              Eduardo Felizardo
Metadata    Test Suite Version            1.0
Metadata    Test Suite Tags               POST    GET    EstoqueCentral    MaisSaudePublicaAPI
Metadata    Test Suite Created On         2026-11-10
Metadata    Test Suite Last Modified      XXXX-XX-XX
Metadata    Project                       Layered Keyword Driven Framework (LKDF)
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Variables ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Test Cases ***
CT-001 - Validate Requisicao Atendida E Recebida - HTTP 400 E 200
    [Documentation]    Atendimento acima do pedido dá 400; atendida pelos lotes que vencem primeiro e recebida com falta.
    [Tags]    POST    GET    EstoqueCentral    HTTP400    HTTP200
    ESTOQUE CENTRAL    ATENDIDA_RECEBIDA

CT-002 - Validate Negacao E Central Invalida - HTTP 422 400 E 200
    [Documentation]    Pedir a quem não é central dá 422; negar pede motivo.
    [Tags]    POST    EstoqueCentral    HTTP422    HTTP400    HTTP200
    ESTOQUE CENTRAL    NEGAR

CT-003 - Validate Inventario - HTTP 409 422 E 200
    [Documentation]    O inventário aberto bloqueia a movimentação; fechar pede todos contados.
    [Tags]    POST    EstoqueCentral    HTTP409    HTTP422    HTTP200
    ESTOQUE CENTRAL    INVENTARIO
