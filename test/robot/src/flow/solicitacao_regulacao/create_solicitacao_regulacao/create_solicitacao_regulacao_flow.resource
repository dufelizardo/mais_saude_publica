*** Settings ***
Library     String
Library     Collections
Resource    ../../../pom/solicitacao_regulacao/create_solicitacao_regulacao/create_solicitacao_regulacao_pom.resource
Resource    ../../procedimento_regulado/create_procedimento_regulado/create_procedimento_regulado_flow.resource
Resource    ../../paciente/create_paciente/create_paciente_flow.resource
Resource    ../../setor/create_setor/create_setor_flow.resource
Resource    ../../profissional/get_profissional_by_matricula/get_profissional_by_matricula_flow.resource
Resource    ../../common/schema_validation_flow.resource
Resource    ../../../resource/config/schema/schema.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
    A unidade de origem solicita; a solicitação entra na fila como SOLICITADA (ADR-0087).
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
Build Solicitacao Regulacao Payload
    [Documentation]    Builds a valid SolicitacaoRegulacaoRequestDto payload.
    [Arguments]    ${pacienteId}    ${procedimentoId}    ${unidadeId}    ${matricula}    ${prioridade}=AMARELO    ${cid}=I20.9
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${payload}=    Create Dictionary
    ...    pacienteId=${pacienteId}
    ...    procedimentoId=${procedimentoId}
    ...    unidadeSolicitanteId=${unidadeId}
    ...    profissionalMatricula=${matricula}
    ...    cid=${cid}
    ...    justificativa=Dor precordial aos esforços há três meses, ECG alterado.
    ...    prioridade=${prioridade}
    RETURN    ${payload}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Seed A Solicitacao Regulacao
    [Documentation]    Seeds patient, unit, professional and procedure, requests regulation and returns uuid, matrícula of the requester, procedimentoId, pacienteId and unidadeId.
    [Arguments]    ${prioridade}=AMARELO
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${pacienteId}    ${cpf}    ${cartaoSus}    ${nomePaciente}=    Seed A Paciente
    ${procedimentoId}    ${nomeProcedimento}=    Seed A Procedimento Regulado
    ${unidadeId}=    Seed A Unidade De Saude Uuid
    ${matricula}=    Seed A Profissional Matricula
    ${payload}=    Build Solicitacao Regulacao Payload    ${pacienteId}    ${procedimentoId}    ${unidadeId}    ${matricula}    ${prioridade}
    ${criada}=    POST Create Solicitacao Regulacao    ${payload}
    Should Be Equal As Integers    ${criada.status_code}    201
    ${lista}=    GET List Solicitacao Regulacao Do Paciente    ${pacienteId}
    ${uuid}=    Set Variable    ${lista.json()}[0][uuid]
    RETURN    ${uuid}    ${matricula}    ${procedimentoId}    ${pacienteId}    ${unidadeId}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

POST Create Solicitacao Regulacao - Status Code 201
    [Documentation]    Requests regulation and checks the summary has no clinical data.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${uuid}    ${matricula}    ${procedimentoId}    ${pacienteId}    ${unidadeId}=    Seed A Solicitacao Regulacao
    ${lista}=    GET List Solicitacao Regulacao Do Paciente    ${pacienteId}
    Validate Array Schema    ${lista.json()}    ${SOLICITACAO_REGULACAO_RESUMO_SCHEMA}
    Should Be Equal    ${lista.json()}[0][status]    SOLICITADA
    Should Be Equal As Integers    ${lista.json()}[0][posicaoNaFila]    1
    Dictionary Should Not Contain Key    ${lista.json()}[0]    cid
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

POST Create Solicitacao Regulacao - Status Code 400 - Cid Invalido
    [Documentation]    Sends an invalid ICD-10 code, expecting HTTP 400.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${pacienteId}    ${cpf}    ${cartaoSus}    ${nomePaciente}=    Seed A Paciente
    ${procedimentoId}    ${nome}=    Seed A Procedimento Regulado
    ${unidadeId}=    Seed A Unidade De Saude Uuid
    ${matricula}=    Seed A Profissional Matricula
    ${payload}=    Build Solicitacao Regulacao Payload    ${pacienteId}    ${procedimentoId}    ${unidadeId}    ${matricula}    cid=XYZ
    ${response}=    POST Create Solicitacao Regulacao    ${payload}
    Should Be Equal As Integers    ${response.status_code}    400
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

POST Create Solicitacao Regulacao - Status Code 409 - Ja Em Andamento
    [Documentation]    Requests the same procedure twice for the patient, expecting HTTP 409.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${uuid}    ${matricula}    ${procedimentoId}    ${pacienteId}    ${unidadeId}=    Seed A Solicitacao Regulacao
    ${payload}=    Build Solicitacao Regulacao Payload    ${pacienteId}    ${procedimentoId}    ${unidadeId}    ${matricula}
    ${response}=    POST Create Solicitacao Regulacao    ${payload}
    Should Be Equal As Integers    ${response.status_code}    409
    Validate Object Schema    ${response.json()}    ${ERROR_RESPONSE_SCHEMA}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Solicitacao Regulacao - Create - POST - By HTTP Status Code
    [Documentation]    Executes the Solicitacao Regulacao - Create - POST scenario for the given HTTP status code.
    [Arguments]    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Run Keyword If    '${meuHTTP}' == '201'    POST Create Solicitacao Regulacao - Status Code 201
    ...    ELSE IF    '${meuHTTP}' == '400'    POST Create Solicitacao Regulacao - Status Code 400 - Cid Invalido
    ...    ELSE IF    '${meuHTTP}' == '409'    POST Create Solicitacao Regulacao - Status Code 409 - Ja Em Andamento
    ...    ELSE    Fail    HTTP status code '${meuHTTP}' is not supported for Solicitacao Regulacao - Create - POST test scenario.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
