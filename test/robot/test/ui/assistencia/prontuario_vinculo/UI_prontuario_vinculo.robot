*** Settings ***
Resource    ../../../../src/scenario/ui/assistencia/prontuario_vinculo/prontuario_vinculo_ui_scenario.resource
Resource    ../../../../src/scenario/common/ui_sessao_scenario.resource
Suite Setup       UI - PRONTUARIO POR VINCULO - PREPARAR
Suite Teardown    UI - FECHAR SISTEMA
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
Metadata    Test Suite - UI Prontuário por vínculo
Metadata    Test Suite Description        Valida o prontuário por vínculo assistencial na interface (ADR-0076), com login, autorização e vínculo ligados.
Metadata    Test Suite Owner              Eduardo Felizardo
Metadata    Test Suite Version            1.0
Metadata    Test Suite Tags               UI    SEGURANCA    UiProntuarioVinculo    MaisSaudePublicaFrontend
Metadata    Test Suite Created On         2026-10-01
Metadata    Test Suite Last Modified      XXXX-XX-XX
Metadata    Project                       Layered Keyword Driven Framework (LKDF)
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
    Roda só contra uma API com login, autorização e vínculo ligados (tag SEGURANCA). No CI, a segunda fase do job
    robot-ui. A ordem importa: a justificativa válida fica por último, porque libera o paciente por 4 horas.
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Variables ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Test Cases ***
CT-001 - Paciente com atendimento na unidade abre o prontuário sem aviso
    [Documentation]    Vínculo pela unidade: sem aviso nem bloqueio.
    [Tags]    UI    SEGURANCA    UiProntuarioVinculo
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    UI - PRONTUARIO POR VINCULO - COM VINCULO
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-002 - Paciente de outra unidade mostra o aviso e bloqueia o histórico
    [Documentation]    Sem vínculo: aviso "Sem vínculo assistencial" e histórico bloqueado.
    [Tags]    UI    SEGURANCA    UiProntuarioVinculo
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    UI - PRONTUARIO POR VINCULO - SEM VINCULO
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-003 - Justificativa curta é recusada na tela
    [Documentation]    Abaixo de 20 caracteres, erro no formulário.
    [Tags]    UI    SEGURANCA    UiProntuarioVinculo
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    UI - PRONTUARIO POR VINCULO - JUSTIFICATIVA CURTA
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-004 - Justificativa válida libera o prontuário com a faixa
    [Documentation]    Registro do acesso justificado, prontuário liberado e faixa "Acesso justificado até".
    [Tags]    UI    SEGURANCA    UiProntuarioVinculo
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    UI - PRONTUARIO POR VINCULO - JUSTIFICATIVA LIBERA
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
