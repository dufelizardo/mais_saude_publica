*** Settings ***
Resource    ../../../src/scenario/laboratorio/ciencia_entrega/ciencia_entrega_scenario.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
Metadata    Test Suite - POST/GET Ciencia, Comunicacao De Critico e Entrega De Resultado
Metadata    Test Suite Description        Ciência de quem pediu, comunicação do valor crítico e entrega do resultado ao paciente (ADR-0181).
Metadata    Test Suite Owner              Eduardo Felizardo
Metadata    Test Suite Version            1.0
Metadata    Test Suite Tags               POST    GET    CienciaEntregaExame    MaisSaudePublicaAPI
Metadata    Test Suite Created On         2026-11-09
Metadata    Test Suite Last Modified      XXXX-XX-XX
Metadata    Project                       Layered Keyword Driven Framework (LKDF)
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Variables ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Test Cases ***
CT-001 - Validate GET ParaCiencia - HTTP 200 OK
    [Documentation]    O resultado liberado aparece para quem pediu.
    [Tags]    GET    CienciaEntregaExame    HTTP200
    CIENCIA ENTREGA EXAME    PARA_CIENCIA

CT-002 - Validate POST Ciencia - HTTP 200 OK E 409 CONFLICT
    [Documentation]    Ciência registrada uma vez; a repetição dá 409.
    [Tags]    POST    CienciaEntregaExame    HTTP200    HTTP409
    CIENCIA ENTREGA EXAME    CIENCIA

CT-003 - Validate POST Ciencia - HTTP 422 UNPROCESSABLE ENTITY (Outro Profissional)
    [Documentation]    Só quem pediu registra a ciência.
    [Tags]    POST    CienciaEntregaExame    HTTP422
    CIENCIA ENTREGA EXAME    CIENCIA_422

CT-004 - Validate GET ParaCiencia - HTTP 400 BAD REQUEST (Sem Matricula)
    [Documentation]    Sem login, a matrícula é obrigatória.
    [Tags]    GET    CienciaEntregaExame    HTTP400
    CIENCIA ENTREGA EXAME    PARA_CIENCIA_400

CT-005 - Validate POST ComunicacaoCritico - HTTP 200 OK
    [Documentation]    O crítico sai da lista do laboratório depois da comunicação; a repetição dá 409.
    [Tags]    POST    GET    CienciaEntregaExame    HTTP200    HTTP409
    CIENCIA ENTREGA EXAME    COMUNICAR

CT-006 - Validate POST ComunicacaoCritico - HTTP 422 UNPROCESSABLE ENTITY (Nao Critico)
    [Documentation]    Resultado normal não é comunicado como crítico.
    [Tags]    POST    CienciaEntregaExame    HTTP422
    CIENCIA ENTREGA EXAME    COMUNICAR_422

CT-007 - Validate POST Entrega - HTTP 200 OK
    [Documentation]    Entrega ao paciente e segunda via a um parente.
    [Tags]    POST    CienciaEntregaExame    HTTP200
    CIENCIA ENTREGA EXAME    ENTREGA

CT-008 - Validate POST Entrega - HTTP 400 BAD REQUEST (Terceiro Sem Documento)
    [Documentation]    Outra pessoa precisa de nome e documento.
    [Tags]    POST    CienciaEntregaExame    HTTP400
    CIENCIA ENTREGA EXAME    ENTREGA_400

CT-009 - Validate POST Entrega - HTTP 422 UNPROCESSABLE ENTITY (Em Andamento)
    [Documentation]    Sem resultado liberado não há entrega.
    [Tags]    POST    CienciaEntregaExame    HTTP422
    CIENCIA ENTREGA EXAME    ENTREGA_422
