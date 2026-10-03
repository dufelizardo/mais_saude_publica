*** Settings ***
Resource    ../../../../src/scenario/ui/administracao/auditoria/alertas_auditoria_ui_scenario.resource
Resource    ../../../../src/scenario/common/ui_sessao_scenario.resource
Suite Setup       UI - ABRIR SISTEMA
Suite Teardown    UI - FECHAR SISTEMA
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
Metadata    Test Suite - UI Administração - Alertas da auditoria
Metadata    Test Suite Description        Valida a aba Alertas da tela Auditoria: análise pela gaveta, validação, "Ver eventos" e o contador do menu (ADR-0097).
Metadata    Test Suite Owner              Eduardo Felizardo
Metadata    Test Suite Version            1.0
Metadata    Test Suite Tags               UI    UiAlertasAuditoria    MaisSaudePublicaFrontend
Metadata    Test Suite Created On         2026-10-02
Metadata    Test Suite Last Modified      XXXX-XX-XX
Metadata    Project                       Layered Keyword Driven Framework (LKDF)
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Variables ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Test Cases ***
CT-001 - Analisar alerta pela gaveta
    [Documentation]    Aviso e alerta procedente.
    [Tags]    UI    UiAlertasAuditoria
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    UI - ALERTAS AUDITORIA - ANALISAR
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-002 - Gaveta exige conclusão e parecer
    [Documentation]    Mensagens dos campos.
    [Tags]    UI    UiAlertasAuditoria
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    UI - ALERTAS AUDITORIA - VALIDACAO
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-003 - Ver eventos de quem foi alertado
    [Documentation]    Aba Eventos filtrada pelo CPF.
    [Tags]    UI    UiAlertasAuditoria
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    UI - ALERTAS AUDITORIA - VER EVENTOS
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-004 - Contador de alertas abertos no menu
    [Documentation]    Contador visível no item Auditoria.
    [Tags]    UI    UiAlertasAuditoria
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    UI - ALERTAS AUDITORIA - CONTADOR
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
