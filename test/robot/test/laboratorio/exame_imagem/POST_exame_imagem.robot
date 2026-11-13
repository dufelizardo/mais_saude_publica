*** Settings ***
Resource    ../../../src/scenario/laboratorio/exame_imagem/exame_imagem_scenario.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
Metadata    Test Suite - POST Exame De Imagem (Realizacao, Laudo, Adendo)
Metadata    Test Suite Description        Exames de imagem: catálogo, serviço de imagem, realização, laudo, adendo e achado crítico (ADR-0182).
Metadata    Test Suite Owner              Eduardo Felizardo
Metadata    Test Suite Version            1.0
Metadata    Test Suite Tags               POST    GET    ExameImagem    MaisSaudePublicaAPI
Metadata    Test Suite Created On         2026-11-09
Metadata    Test Suite Last Modified      XXXX-XX-XX
Metadata    Project                       Layered Keyword Driven Framework (LKDF)
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Variables ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Test Cases ***
CT-001 - Validate POST ExameImagem Catalogo - HTTP 201 CREATED E 400 BAD REQUEST
    [Documentation]    Modalidade obrigatória; sem material.
    [Tags]    POST    ExameImagem    HTTP201    HTTP400
    EXAME IMAGEM    CATALOGO

CT-002 - Validate POST PedidoExame Com Imagem - HTTP 400 E 422
    [Documentation]    Sem serviço de imagem e unidade sem a capacidade.
    [Tags]    POST    ExameImagem    HTTP400    HTTP422
    EXAME IMAGEM    PEDIDO_400_422

CT-003 - Validate POST Realizacao - HTTP 200 OK
    [Documentation]    Realizado, vai para Para laudar.
    [Tags]    POST    ExameImagem    HTTP200
    EXAME IMAGEM    REALIZACAO

CT-004 - Validate POST Realizacao - HTTP 400 BAD REQUEST (Contraste Sem Descricao)
    [Documentation]    Contraste pede a descrição.
    [Tags]    POST    ExameImagem    HTTP400
    EXAME IMAGEM    REALIZACAO_400

CT-005 - Validate POST Laudo - HTTP 200 OK
    [Documentation]    Laudo assinado sai liberado.
    [Tags]    POST    ExameImagem    HTTP200
    EXAME IMAGEM    LAUDO

CT-006 - Validate POST Laudo - HTTP 422 UNPROCESSABLE ENTITY (Sem Realizacao)
    [Documentation]    Laudo antes da realização.
    [Tags]    POST    ExameImagem    HTTP422
    EXAME IMAGEM    LAUDO_422

CT-007 - Validate POST Adendo - HTTP 200 OK E 400 BAD REQUEST
    [Documentation]    Adendo pede motivo.
    [Tags]    POST    ExameImagem    HTTP200    HTTP400
    EXAME IMAGEM    ADENDO

CT-008 - Validate Achado Critico Na Comunicacao
    [Documentation]    Achado crítico entra nos críticos a comunicar.
    [Tags]    POST    GET    ExameImagem    HTTP200
    EXAME IMAGEM    ACHADO_CRITICO
