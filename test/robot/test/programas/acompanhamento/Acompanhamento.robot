*** Settings ***
Resource    ../../../src/scenario/programas/acompanhamento/acompanhamento_scenario.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
Metadata    Test Suite - Acompanhamento
Metadata    Test Suite Description        This test suite validates the program follow-up of patients (ADR-0119) of the Mais Saúde Pública API.
Metadata    Test Suite Owner              Eduardo Felizardo
Metadata    Test Suite Version            1.0
Metadata    Test Suite Tags               Acompanhamento    MaisSaudePublicaAPI
Metadata    Test Suite Created On         2026-10-17
Metadata    Test Suite Last Modified      XXXX-XX-XX
Metadata    Project                       Layered Keyword Driven Framework (LKDF)
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Variables ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Test Cases ***
CT-001 - Validate Acompanhamento - HTTP 201 (Inscricao)
    [Documentation]    Inscrição ativa com o evento de entrada.
    [Tags]    Acompanhamento    HTTP201
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ACOMPANHAMENTO    201
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-002 - Validate Acompanhamento - HTTP 409 (Repetido)
    [Documentation]    Um acompanhamento vigente por paciente e programa.
    [Tags]    Acompanhamento    HTTP409
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ACOMPANHAMENTO    409
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-003 - Validate Acompanhamento - HTTP 422 (Unidade Que Nao Oferece)
    [Documentation]    A unidade responsável precisa oferecer o programa.
    [Tags]    Acompanhamento    HTTP422
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ACOMPANHAMENTO    422
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-004 - Validate Acompanhamento - HTTP 200 (Faltoso E Saida)
    [Documentation]    Faltoso, contagem no programa e saída com alta.
    [Tags]    Acompanhamento    HTTP200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ACOMPANHAMENTO    200-situacao
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-005 - Validate Acompanhamento - HTTP 400 (Saida Sem Descricao)
    [Documentation]    A saída exige descrição.
    [Tags]    Acompanhamento    HTTP400
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ACOMPANHAMENTO    400
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-006 - Validate Acompanhamento - HTTP 200 (Programas Do Paciente)
    [Documentation]    Programas vigentes e encerrados do paciente.
    [Tags]    Acompanhamento    HTTP200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ACOMPANHAMENTO    200-paciente
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-007 - Validate Acompanhamento - HTTP 404 (Inexistente)
    [Documentation]    Acompanhamento que não existe.
    [Tags]    Acompanhamento    HTTP404
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ACOMPANHAMENTO    404
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
