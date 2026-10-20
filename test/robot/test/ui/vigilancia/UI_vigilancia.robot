*** Settings ***
Resource    ../../../src/scenario/ui/vigilancia/vigilancia_ui_scenario.resource
Resource    ../../../src/scenario/common/ui_sessao_scenario.resource
Suite Setup       UI - ABRIR SISTEMA
Suite Teardown    UI - FECHAR SISTEMA
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
Metadata    Test Suite - UI Vigilância epidemiológica
Metadata    Test Suite Description        Valida a tela Vigilância epidemiológica: notificação com a ficha, classificação, encerramento e notificação negativa (ADR-0124).
Metadata    Test Suite Owner              Eduardo Felizardo
Metadata    Test Suite Version            1.0
Metadata    Test Suite Tags               UI    UiVigilancia    MaisSaudePublicaFrontend
Metadata    Test Suite Created On         2026-10-20
Metadata    Test Suite Last Modified      XXXX-XX-XX
Metadata    Project                       Layered Keyword Driven Framework (LKDF)
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Variables ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Test Cases ***
CT-001 - Notificar com a ficha
    [Documentation]    Mensagem do campo obrigatório; notificação com a resposta.
    [Tags]    UI    UiVigilancia
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    UI - VIGILANCIA - NOTIFICAR
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-002 - Classificar e encerrar
    [Documentation]    Critério obrigatório; classificação e encerramento.
    [Tags]    UI    UiVigilancia
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    UI - VIGILANCIA - CLASSIFICAR E ENCERRAR
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-003 - Notificação negativa
    [Documentation]    Declarar uma semana pendente.
    [Tags]    UI    UiVigilancia
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    UI - VIGILANCIA - NEGATIVA
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-004 - Registrar contato
    [Documentation]    Nome obrigatório sem cadastro; contato na lista; situação muda (ADR-0130).
    [Tags]    UI    UiVigilancia
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    UI - VIGILANCIA - CONTATO
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-005 - Notificar o contato
    [Documentation]    A notificação do contato fica ligada ao caso índice (ADR-0130).
    [Tags]    UI    UiVigilancia
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    UI - VIGILANCIA - NOTIFICAR CONTATO
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-006 - Local do caso e mapa
    [Documentation]    Marcar no minimapa; o mapa da vigilância com a curva (ADR-0130).
    [Tags]    UI    UiVigilancia
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    UI - VIGILANCIA - LOCAL E MAPA
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
