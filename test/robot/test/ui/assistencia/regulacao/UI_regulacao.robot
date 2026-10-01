*** Settings ***
Resource    ../../../../src/scenario/ui/assistencia/regulacao/regulacao_ui_scenario.resource
Resource    ../../../../src/scenario/common/ui_sessao_scenario.resource
Suite Setup       UI - ABRIR SISTEMA
Suite Teardown    UI - FECHAR SISTEMA
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
Metadata    Test Suite - UI Assistência - Regulação
Metadata    Test Suite Description        Valida a tela Regulação: nova solicitação, análise pela fila, devolução e complemento, catálogo e o link do Encaminhar (ADR-0088).
Metadata    Test Suite Owner              Eduardo Felizardo
Metadata    Test Suite Version            1.0
Metadata    Test Suite Tags               UI    UiRegulacao    MaisSaudePublicaFrontend
Metadata    Test Suite Created On         2026-10-01
Metadata    Test Suite Last Modified      XXXX-XX-XX
Metadata    Project                       Layered Keyword Driven Framework (LKDF)
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Variables ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Test Cases ***
CT-001 - Encaminhar paciente pela gaveta
    [Documentation]    Aviso e linha na aba Solicitações.
    [Tags]    UI    UiRegulacao
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    UI - REGULACAO - NOVA SOLICITACAO
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-002 - Regulador autoriza com vaga pela fila
    [Documentation]    Aviso e solicitação autorizada.
    [Tags]    UI    UiRegulacao
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    UI - REGULACAO - AUTORIZAR PELA FILA
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-003 - Regulador devolve e solicitante complementa
    [Documentation]    A solicitação volta à fila.
    [Tags]    UI    UiRegulacao
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    UI - REGULACAO - DEVOLVER E COMPLEMENTAR
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-004 - Gaveta recusa CID inválido
    [Documentation]    Mensagem do campo CID.
    [Tags]    UI    UiRegulacao
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    UI - REGULACAO - CID INVALIDO
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-005 - Cadastrar procedimento regulado
    [Documentation]    Aviso e linha no catálogo.
    [Tags]    UI    UiRegulacao
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    UI - REGULACAO - CADASTRAR PROCEDIMENTO
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-006 - Encaminhar abre a gaveta com paciente e unidade
    [Documentation]    Campos já preenchidos.
    [Tags]    UI    UiRegulacao
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    UI - REGULACAO - ENCAMINHAR PREENCHIDO
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
