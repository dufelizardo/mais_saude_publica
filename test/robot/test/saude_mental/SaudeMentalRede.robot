*** Settings ***
Resource    ../../src/scenario/saude_mental/saude_mental_rede_scenario.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
Metadata    Test Suite - Saude Mental Rede
Metadata    Test Suite Description        This test suite validates the care network of mental health (ADR-0122) of the Mais Saúde Pública API.
Metadata    Test Suite Owner              Eduardo Felizardo
Metadata    Test Suite Version            1.0
Metadata    Test Suite Tags               SaudeMentalRede    MaisSaudePublicaAPI
Metadata    Test Suite Created On         2026-10-20
Metadata    Test Suite Last Modified      XXXX-XX-XX
Metadata    Project                       Layered Keyword Driven Framework (LKDF)
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Variables ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Test Cases ***
CT-001 - Validate Saude Mental Rede - HTTP 201 (Encaminhamento)
    [Documentation]    Encaminhar pela Regulação.
    [Tags]    SaudeMentalRede    HTTP201
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    SAUDE MENTAL REDE    201-encaminhamento
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-002 - Validate Saude Mental Rede - HTTP 422 (Vincular De Outro Paciente)
    [Documentation]    Solicitação de outro paciente.
    [Tags]    SaudeMentalRede    HTTP422
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    SAUDE MENTAL REDE    422-vinculo
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-003 - Validate Saude Mental Rede - HTTP 201 (Compartilhamento)
    [Documentation]    Compartilhar, repetido e encerrar.
    [Tags]    SaudeMentalRede    HTTP201
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    SAUDE MENTAL REDE    201-compartilhamento
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-004 - Validate Saude Mental Rede - HTTP 422 (Matriciamento Sem Apoio)
    [Documentation]    Matriciamento sem equipe e sem apoio.
    [Tags]    SaudeMentalRede    HTTP422
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    SAUDE MENTAL REDE    422-matriciamento
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-005 - Validate Saude Mental Rede - HTTP 200 (Transferencia Aceita)
    [Documentation]    Proposta e aceite com o caso novo.
    [Tags]    SaudeMentalRede    HTTP200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    SAUDE MENTAL REDE    200-aceite
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-006 - Validate Saude Mental Rede - HTTP 200 (Transferencia Recusada)
    [Documentation]    Recusa pelo destino.
    [Tags]    SaudeMentalRede    HTTP200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    SAUDE MENTAL REDE    200-recusa
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-007 - Validate Saude Mental Rede - HTTP 200 (Transferencias Pendentes)
    [Documentation]    Pendentes do escopo.
    [Tags]    SaudeMentalRede    HTTP200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    SAUDE MENTAL REDE    200-pendentes
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
