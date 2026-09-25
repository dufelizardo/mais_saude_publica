*** Settings ***
Resource    ../../../src/scenario/paciente/create_paciente/create_paciente_scenario.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
Metadata    Test Suite - POST Create Paciente
Metadata    Test Suite Description        This test suite validates the POST create Paciente endpoint of the Mais Saúde Pública API.
Metadata    Test Suite Owner              Eduardo Felizardo
Metadata    Test Suite Version            1.0
Metadata    Test Suite Tags               POST    CreatePaciente    MaisSaudePublicaAPI
Metadata    Test Suite Created On         2026-09-25
Metadata    Test Suite Last Modified      XXXX-XX-XX
Metadata    Project                       Layered Keyword Driven Framework (LKDF)
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Variables ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Test Cases ***
CT-001 - Validate POST Create Paciente - HTTP 201 CREATED
    [Documentation]    Test case to validate the POST create Paciente endpoint with HTTP 201 CREATED response.
    [Tags]    POST    CreatePaciente    HTTP201
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    PACIENTE - CREATE - POST    201
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-002 - Validate POST Create Paciente - HTTP 400 BAD REQUEST (Campos Obrigatorios Em Branco)
    [Documentation]    Test case to validate the POST create Paciente endpoint with HTTP 400 BAD REQUEST
    ...    response, when required fields (nome, cpf, dataNascimento, sexo, endereco, telefones, ativo) are
    ...    left blank/omitted.
    [Tags]    POST    CreatePaciente    HTTP400
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    PACIENTE - CREATE - POST    400
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-003 - Validate POST Create Paciente - HTTP 201 CREATED (Sem Cartao Sus)
    [Documentation]    Test case to validate the POST create Paciente endpoint without a cartaoSus, expecting
    ...    HTTP 201 CREATED (ver DER.md: nem todo paciente tem o CNS em mãos no primeiro contato).
    [Tags]    POST    CreatePaciente    HTTP201
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    PACIENTE - CREATE - POST - Sem Cartao Sus
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
