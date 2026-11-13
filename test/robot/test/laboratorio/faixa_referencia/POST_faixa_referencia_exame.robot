*** Settings ***
Resource    ../../../src/scenario/laboratorio/faixa_referencia/faixa_referencia_scenario.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
Metadata    Test Suite - POST/PUT/DELETE FaixaReferenciaExame e GET Evolucao
Metadata    Test Suite Description        Faixas de referência por sexo e idade, valores críticos e a evolução do exame (ADR-0180).
Metadata    Test Suite Owner              Eduardo Felizardo
Metadata    Test Suite Version            1.0
Metadata    Test Suite Tags               POST    PUT    DELETE    GET    FaixaReferenciaExame    MaisSaudePublicaAPI
Metadata    Test Suite Created On         2026-11-09
Metadata    Test Suite Last Modified      XXXX-XX-XX
Metadata    Project                       Layered Keyword Driven Framework (LKDF)
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Variables ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Test Cases ***
CT-001 - Validate POST FaixaReferenciaExame - HTTP 201 CREATED
    [Documentation]    Faixa feminina adulta com rótulo.
    [Tags]    POST    FaixaReferenciaExame    HTTP201
    FAIXA REFERENCIA EXAME    201

CT-002 - Validate POST FaixaReferenciaExame - HTTP 409 CONFLICT (Sobreposicao)
    [Documentation]    Idades sobrepostas no mesmo sexo.
    [Tags]    POST    FaixaReferenciaExame    HTTP409
    FAIXA REFERENCIA EXAME    409

CT-003 - Validate POST FaixaReferenciaExame - HTTP 400 BAD REQUEST (Sexo Ignorado)
    [Documentation]    IGNORADO não é sexo de faixa.
    [Tags]    POST    FaixaReferenciaExame    HTTP400
    FAIXA REFERENCIA EXAME    400

CT-004 - Validate POST FaixaReferenciaExame - HTTP 422 UNPROCESSABLE ENTITY (Exame Em Texto)
    [Documentation]    Exame de resultado em texto não tem faixa.
    [Tags]    POST    FaixaReferenciaExame    HTTP422
    FAIXA REFERENCIA EXAME    422

CT-005 - Validate PUT DELETE FaixaReferenciaExame - HTTP 200 OK
    [Documentation]    Edita, remove e a segunda remoção dá 404.
    [Tags]    PUT    DELETE    FaixaReferenciaExame    HTTP200
    FAIXA REFERENCIA EXAME    EDITAR_REMOVER

CT-006 - Validate Resultado Usa A Faixa Do Sexo E Da Idade
    [Documentation]    O resultado da paciente adulta usa a faixa feminina, não a geral.
    [Tags]    POST    FaixaReferenciaExame    HTTP200
    FAIXA REFERENCIA EXAME    FAIXA_DO_PACIENTE

CT-007 - Validate Resultado Critico
    [Documentation]    Abaixo do crítico mínimo: CRITICO_BAIXO.
    [Tags]    POST    FaixaReferenciaExame    HTTP200
    FAIXA REFERENCIA EXAME    CRITICO

CT-008 - Validate GET EvolucaoExame - HTTP 200 OK
    [Documentation]    Só liberados, em ordem de coleta.
    [Tags]    GET    EvolucaoExame    HTTP200
    FAIXA REFERENCIA EXAME    EVOLUCAO

CT-009 - Validate GET EvolucaoExame - HTTP 404 NOT FOUND
    [Documentation]    Exame inexistente.
    [Tags]    GET    EvolucaoExame    HTTP404
    FAIXA REFERENCIA EXAME    EVOLUCAO_404
