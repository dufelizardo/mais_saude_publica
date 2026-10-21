*** Settings ***
Resource    ../../../src/scenario/programas/acao/acao_programatica_scenario.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
Metadata    Test Suite - Acao Programatica
Metadata    Test Suite Description        This test suite validates the programmatic actions (ADR-0132) of the Mais Saúde Pública API.
Metadata    Test Suite Owner              Eduardo Felizardo
Metadata    Test Suite Version            1.0
Metadata    Test Suite Tags               AcaoProgramatica    MaisSaudePublicaAPI
Metadata    Test Suite Created On         2026-10-21
Metadata    Test Suite Last Modified      XXXX-XX-XX
Metadata    Project                       Layered Keyword Driven Framework (LKDF)
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Variables ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Test Cases ***
CT-001 - Validate Acao Programatica - HTTP 201 (Acao Individual)
    [Documentation]    Plans a scheduled consultation for a follow-up; past date is overdue.
    [Tags]    AcaoProgramatica    HTTP201
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ACAO PROGRAMATICA    201-individual
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-002 - Validate Acao Programatica - HTTP 400 (Acao Consulta Sem Acompanhamento)
    [Documentation]    A scheduled consultation needs the follow-up.
    [Tags]    AcaoProgramatica    HTTP400
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ACAO PROGRAMATICA    400-individual
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-003 - Validate Acao Programatica - HTTP 201 (Acao Coletiva)
    [Documentation]    A group with a participant followed in the program.
    [Tags]    AcaoProgramatica    HTTP201
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ACAO PROGRAMATICA    201-coletiva
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-004 - Validate Acao Programatica - HTTP 422 (Acao Participante Fora Do Programa)
    [Documentation]    Participants must be followed in the program.
    [Tags]    AcaoProgramatica    HTTP422
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ACAO PROGRAMATICA    422-participante
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-005 - Validate Acao Programatica - HTTP 200 (Acao Realizacao)
    [Documentation]    Confirms the action; confirming again is refused.
    [Tags]    AcaoProgramatica    HTTP200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ACAO PROGRAMATICA    200-realizar
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-006 - Validate Acao Programatica - HTTP 200 (Acao Nao Realizada)
    [Documentation]    Not done with the reason.
    [Tags]    AcaoProgramatica    HTTP200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ACAO PROGRAMATICA    200-nao-realizada
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-007 - Validate Acao Programatica - HTTP 201 (Acao Busca Ativa)
    [Documentation]    Active search of the follow-up; a second pending one is refused.
    [Tags]    AcaoProgramatica    HTTP201
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ACAO PROGRAMATICA    201-busca-ativa
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-008 - Validate Acao Programatica - HTTP 400 (Acao Busca Sem Resultado)
    [Documentation]    Confirming the active search needs the result; moved suggests closing.
    [Tags]    AcaoProgramatica    HTTP400
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ACAO PROGRAMATICA    400-busca-resultado
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-009 - Validate Acao Programatica - HTTP 200 (Acoes Lista)
    [Documentation]    The list brings the overdue action and the indicators.
    [Tags]    AcaoProgramatica    HTTP200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ACAO PROGRAMATICA    200-lista
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-010 - Validate Acao Programatica - HTTP 200 (Acoes Do Paciente)
    [Documentation]    The patient actions.
    [Tags]    AcaoProgramatica    HTTP200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ACAO PROGRAMATICA    200-paciente
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-011 - Validate Acao Programatica - HTTP 404 (Acao Inexistente)
    [Documentation]    Confirming an action that does not exist.
    [Tags]    AcaoProgramatica    HTTP404
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ACAO PROGRAMATICA    404-acao
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
