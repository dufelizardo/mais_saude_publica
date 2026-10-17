*** Settings ***
Resource    ../../../../src/scenario/ui/assistencia/programas/programas_ui_scenario.resource
Resource    ../../../../src/scenario/common/ui_sessao_scenario.resource
Suite Setup       UI - ABRIR SISTEMA
Suite Teardown    UI - FECHAR SISTEMA
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
Metadata    Test Suite - UI Assistência - Programas de Saúde
Metadata    Test Suite Description        Valida a tela Programas de Saúde e a consulta pública na landing page (ADR-0118).
Metadata    Test Suite Owner              Eduardo Felizardo
Metadata    Test Suite Version            1.0
Metadata    Test Suite Tags               UI    UiProgramas    MaisSaudePublicaFrontend
Metadata    Test Suite Created On         2026-10-17
Metadata    Test Suite Last Modified      XXXX-XX-XX
Metadata    Project                       Layered Keyword Driven Framework (LKDF)
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Variables ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Test Cases ***
CT-001 - Cartão e detalhe do programa
    [Documentation]    Saúde do Idoso com o público-alvo.
    [Tags]    UI    UiProgramas
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    UI - PROGRAMAS - DETALHE
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-002 - Cadastrar programa
    [Documentation]    Aviso e detalhe do programa novo.
    [Tags]    UI    UiProgramas
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    UI - PROGRAMAS - CADASTRAR
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-003 - Gaveta recusa dados inválidos
    [Documentation]    Mensagens do código, do nome e da condição.
    [Tags]    UI    UiProgramas
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    UI - PROGRAMAS - VALIDACAO
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-004 - Incluir unidade participante
    [Documentation]    A unidade aparece no detalhe.
    [Tags]    UI    UiProgramas
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    UI - PROGRAMAS - INCLUIR UNIDADE
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-005 - Encerrar participação com motivo
    [Documentation]    Sem motivo, a mensagem; com motivo, vai para o histórico.
    [Tags]    UI    UiProgramas
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    UI - PROGRAMAS - ENCERRAR
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-006 - Consulta pública na landing page
    [Documentation]    Programa ativo e onde é oferecido, sem login.
    [Tags]    UI    UiProgramas
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    UI - PROGRAMAS - LANDING
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
