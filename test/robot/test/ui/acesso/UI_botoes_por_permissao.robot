*** Settings ***
Resource    ../../../src/scenario/ui/acesso/botoes_por_permissao_ui_scenario.resource
Resource    ../../../src/scenario/common/ui_sessao_scenario.resource
Suite Setup       UI - BOTOES POR PERMISSAO - PREPARAR
Suite Teardown    UI - FECHAR SISTEMA
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
Metadata    Test Suite - UI Botões por permissão
Metadata    Test Suite Description        Valida que as telas só oferecem as ações que o perfil permite (ADR-0079), com login e autorização ligados.
Metadata    Test Suite Owner              Eduardo Felizardo
Metadata    Test Suite Version            1.0
Metadata    Test Suite Tags               UI    SEGURANCA    UiBotoesPorPermissao    MaisSaudePublicaFrontend
Metadata    Test Suite Created On         2026-10-01
Metadata    Test Suite Last Modified      XXXX-XX-XX
Metadata    Project                       Layered Keyword Driven Framework (LKDF)
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Variables ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Test Cases ***
CT-001 - Técnico de enfermagem não vê cadastro de paciente nem abertura de atendimento
    [Documentation]    Sem PACIENTE.CADASTRAR e ATENDIMENTO.GERENCIAR, os botões não aparecem.
    [Tags]    UI    SEGURANCA    UiBotoesPorPermissao
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    UI - BOTOES POR PERMISSAO - TECNICO
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-002 - Recepção vê cadastro de paciente e abertura de atendimento
    [Documentation]    Com as permissões, os botões aparecem.
    [Tags]    UI    SEGURANCA    UiBotoesPorPermissao
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    UI - BOTOES POR PERMISSAO - RECEPCAO
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
