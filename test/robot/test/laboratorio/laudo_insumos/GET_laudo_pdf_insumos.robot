*** Settings ***
Resource    ../../../src/scenario/laboratorio/laudo_insumos/laudo_insumos_scenario.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
Metadata    Test Suite - GET Laudo PDF, Verificacao Publica e PUT Insumos
Metadata    Test Suite Description        Laudo em PDF com assinatura eletrônica interna, verificação pública e insumos do Estoque (ADR-0183).
Metadata    Test Suite Owner              Eduardo Felizardo
Metadata    Test Suite Version            1.0
Metadata    Test Suite Tags               GET    PUT    POST    LaudoInsumos    MaisSaudePublicaAPI
Metadata    Test Suite Created On         2026-11-09
Metadata    Test Suite Last Modified      XXXX-XX-XX
Metadata    Project                       Layered Keyword Driven Framework (LKDF)
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Variables ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Test Cases ***
CT-001 - Validate GET LaudoPdf - HTTP 200 OK
    [Documentation]    PDF com código de verificação; a verificação pública confere o hash.
    [Tags]    GET    LaudoInsumos    HTTP200
    LAUDO E INSUMOS    PDF

CT-002 - Validate GET LaudoPdf - HTTP 422 UNPROCESSABLE ENTITY (Sem Liberado)
    [Documentation]    Sem resultado liberado não há laudo.
    [Tags]    GET    LaudoInsumos    HTTP422
    LAUDO E INSUMOS    PDF_422

CT-003 - Validate Laudo Substituido Apos Retificacao
    [Documentation]    A versão antiga aparece como substituída.
    [Tags]    GET    LaudoInsumos    HTTP200
    LAUDO E INSUMOS    SUBSTITUIDO

CT-004 - Validate GET VerificarLaudo - HTTP 404 NOT FOUND
    [Documentation]    Código desconhecido.
    [Tags]    GET    LaudoInsumos    HTTP404
    LAUDO E INSUMOS    VERIFICAR_404

CT-005 - Validate Insumos Baixados Na Coleta
    [Documentation]    O estoque da unidade da coleta diminui.
    [Tags]    PUT    LaudoInsumos    HTTP200
    LAUDO E INSUMOS    INSUMOS_COLETA

CT-006 - Validate PUT Insumos - HTTP 400 BAD REQUEST (Repetido)
    [Documentation]    O mesmo material duas vezes.
    [Tags]    PUT    LaudoInsumos    HTTP400
    LAUDO E INSUMOS    INSUMOS_400

CT-007 - Validate Consumo Sem Saldo E Conferencia
    [Documentation]    Coleta sem saldo vira alerta, conferido pelo almoxarife.
    [Tags]    POST    LaudoInsumos    HTTP204
    LAUDO E INSUMOS    CONSUMO_SEM_SALDO
