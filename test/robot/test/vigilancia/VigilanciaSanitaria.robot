*** Settings ***
Resource    ../../src/scenario/vigilancia/vigilancia_sanitaria_scenario.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
Metadata    Test Suite - Vigilância Sanitária
Metadata    Test Suite Description        Estabelecimentos, licença, denúncia, inspeção e pendências (ADR-0199).
Metadata    Test Suite Owner              Eduardo Felizardo
Metadata    Test Suite Version            1.0
Metadata    Test Suite Tags               POST    GET    VigilanciaSanitaria    MaisSaudePublicaAPI
Metadata    Test Suite Created On         2026-11-12
Metadata    Test Suite Last Modified      XXXX-XX-XX
Metadata    Project                       Layered Keyword Driven Framework (LKDF)
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Variables ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Test Cases ***
CT-001 - Validate Dispensa Do Risco I - HTTP 201
    [Documentation]    Risco I: a dispensa sem validade, que não se repete (409).
    [Tags]    POST    VigilanciaSanitaria    HTTP201
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    VIGILANCIA SANITARIA    DISPENSA
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-002 - Validate Cnpj Invalido - HTTP 400
    [Documentation]    O dígito verificador errado é recusado.
    [Tags]    POST    VigilanciaSanitaria    HTTP400
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    VIGILANCIA SANITARIA    CNPJ_INVALIDO
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-003 - Validate Risco III Com Inspecao - HTTP 422 E 201
    [Documentation]    A licença do risco III espera a inspeção satisfatória.
    [Tags]    POST    VigilanciaSanitaria    HTTP422    HTTP201
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    VIGILANCIA SANITARIA    RISCO_ALTO
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-004 - Validate Denuncia Inspecao E Retorno - HTTP 201
    [Documentation]    Denúncia inspecionada, procedente, e o retorno confirma a correção; o termo em PDF.
    [Tags]    POST    VigilanciaSanitaria    HTTP201
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    VIGILANCIA SANITARIA    DENUNCIA
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-005 - Validate Pendencias - HTTP 200
    [Documentation]    O risco II sem licença entra nas pendências.
    [Tags]    GET    VigilanciaSanitaria    HTTP200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    VIGILANCIA SANITARIA    PENDENCIAS
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
