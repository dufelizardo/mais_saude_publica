*** Settings ***
Resource    ../../../../src/scenario/ui/vigilancia/sanitaria/vigilancia_sanitaria_ui_scenario.resource
Resource    ../../../../src/scenario/common/ui_sessao_scenario.resource
Suite Setup       UI - ABRIR SISTEMA
Suite Teardown    UI - FECHAR SISTEMA
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
Metadata    Test Suite - UI Vigilância sanitária
Metadata    Test Suite Description        Valida a tela da vigilância sanitária: menu, cadastro do estabelecimento e inspeção com roteiro (ADR-0199).
Metadata    Test Suite Owner              Eduardo Felizardo
Metadata    Test Suite Version            1.0
Metadata    Test Suite Tags               UI    UiVigilanciaSanitaria    MaisSaudePublicaFrontend
Metadata    Test Suite Created On         2026-11-12
Metadata    Test Suite Last Modified      XXXX-XX-XX
Metadata    Project                       Layered Keyword Driven Framework (LKDF)
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Variables ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Test Cases ***
CT-001 - Vigilância sanitária pelo menu
    [Documentation]    O item do menu Vigilância abre a tela.
    [Tags]    UI    UiVigilanciaSanitaria
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    UI - VIGILANCIA SANITARIA - PELO MENU
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-002 - Cadastrar estabelecimento
    [Documentation]    O estabelecimento com a atividade de risco III aparece na tabela.
    [Tags]    UI    UiVigilanciaSanitaria
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    UI - VIGILANCIA SANITARIA - CADASTRAR ESTABELECIMENTO
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-003 - Registrar inspeção com roteiro
    [Documentation]    O item não conforme gera a irregularidade e a inspeção fica com ressalvas.
    [Tags]    UI    UiVigilanciaSanitaria
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    UI - VIGILANCIA SANITARIA - REGISTRAR INSPECAO
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
