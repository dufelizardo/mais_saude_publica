*** Settings ***
Resource    ../../../src/scenario/laboratorio/create_exame_laboratorial/create_exame_laboratorial_scenario.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
Metadata    Test Suite - POST ExameLaboratorial
Metadata    Test Suite Description        This test suite validates the POST /api/v1/exame-laboratorial/ endpoint of the Mais Saúde Pública API (ADR-0087).
Metadata    Test Suite Owner              Eduardo Felizardo
Metadata    Test Suite Version            1.0
Metadata    Test Suite Tags               POST    ExameLaboratorial    MaisSaudePublicaAPI
Metadata    Test Suite Created On         2026-10-01
Metadata    Test Suite Last Modified      XXXX-XX-XX
Metadata    Project                       Layered Keyword Driven Framework (LKDF)
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Variables ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Test Cases ***
CT-001 - Validate POST ExameLaboratorial - HTTP 201 CREATED
    [Documentation]    Cadastro no catálogo.
    [Tags]    POST    ExameLaboratorial    HTTP201
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    EXAME LABORATORIAL - POST PATCH    201
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-002 - Validate POST ExameLaboratorial - HTTP 409 CONFLICT (Nome Repetido)
    [Documentation]    Nome repetido.
    [Tags]    POST    ExameLaboratorial    HTTP409
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    EXAME LABORATORIAL - POST PATCH    409
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-003 - Validate POST ExameLaboratorial - HTTP 400 BAD REQUEST (Faixa Invertida)
    [Documentation]    Mínima maior que a máxima.
    [Tags]    POST    ExameLaboratorial    HTTP400
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    EXAME LABORATORIAL - POST PATCH    400
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-004 - Validate POST ExameLaboratorial - HTTP 200 OK (Edicao)
    [Documentation]    Edição: fora de uso.
    [Tags]    POST    ExameLaboratorial    HTTP200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    EXAME LABORATORIAL - POST PATCH    200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
