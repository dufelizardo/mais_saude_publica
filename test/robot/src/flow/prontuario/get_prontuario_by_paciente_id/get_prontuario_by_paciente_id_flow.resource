*** Settings ***
Resource    ../../../pom/prontuario/get_prontuario_by_paciente_id/get_prontuario_by_paciente_id_pom.resource
Resource    ../../atendimento/create_atendimento/create_atendimento_flow.resource
Resource    ../../consulta/create_consulta/create_consulta_flow.resource
Resource    ../../procedimento/create_procedimento/create_procedimento_flow.resource
Resource    ../../common/schema_validation_flow.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
    Prontuário é um endpoint só de leitura (ver ADR-0045) — não tem create/update próprio. Esta flow monta a
    cadeia completa (Paciente → Atendimento → Consulta → Procedimento) reaproveitando as flows já existentes
    de cada domínio, em vez de um "Seed" único, porque nenhuma delas devolve pacienteId até o fim da cadeia.
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
Call GET Get Prontuario By Paciente Id
    [Documentation]    Calls the GET Get Prontuario By Paciente Id keyword from the POM.
    [Arguments]    ${pacienteId}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${response}=    GET Get Prontuario By Paciente Id    ${pacienteId}

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Seed Um Prontuario Completo
    [Documentation]    Seeds a Paciente with one Atendimento, one Consulta and one Procedimento, returning
    ...    pacienteId, atendimentoId, consultaId.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${unidadeId}=    Seed A Unidade De Saude Uuid
    ${matricula}=    Seed A Profissional Matricula
    ${pacienteId}    ${cpf}    ${cartaoSus}    ${nomePaciente}=    Seed A Paciente

    ${atendimentoPayload}=    Build Atendimento Payload    ${pacienteId}    ${matricula}    ${unidadeId}
    ${atendimentoResponse}=    POST Create Atendimento    ${atendimentoPayload}
    Should Be Equal As Integers    ${atendimentoResponse.status_code}    201
    ${atendimentoId}=    Buscar Atendimento Uuid Por Paciente    ${pacienteId}

    ${consultaPayload}=    Build Consulta Payload    ${atendimentoId}    ${matricula}
    ${consultaResponse}=    POST Create Consulta    ${consultaPayload}
    Should Be Equal As Integers    ${consultaResponse.status_code}    201
    ${consultaId}=    Buscar Consulta Uuid Por Atendimento    ${atendimentoId}

    ${procedimentoPayload}=    Build Procedimento Payload    ${consultaId}    ${matricula}
    ${procedimentoResponse}=    POST Create Procedimento    ${procedimentoPayload}
    Should Be Equal As Integers    ${procedimentoResponse.status_code}    201

    RETURN    ${pacienteId}    ${atendimentoId}    ${consultaId}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

GET Get Prontuario By Paciente Id - Status Code 200
    [Documentation]    Seeds a full Atendimento/Consulta/Procedimento chain for a fresh Paciente and fetches
    ...    its Prontuário, expecting HTTP 200 with the aggregation reflecting every level.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${pacienteId}    ${atendimentoId}    ${consultaId}=    Seed Um Prontuario Completo
    ${response}=    Call GET Get Prontuario By Paciente Id    ${pacienteId}
    Should Be Equal As Integers    ${response.status_code}    200

    ${body}=    Set Variable    ${response.json()}
    Should Be Equal As Strings    ${body['pacienteUuid']}    ${pacienteId}
    Length Should Be    ${body['atendimentos']}    1
    Should Be Equal As Strings    ${body['atendimentos'][0]['atendimento']['uuid']}    ${atendimentoId}
    Length Should Be    ${body['atendimentos'][0]['consultas']}    1
    Should Be Equal As Strings    ${body['atendimentos'][0]['consultas'][0]['consulta']['uuid']}    ${consultaId}
    Length Should Be    ${body['atendimentos'][0]['consultas'][0]['procedimentos']}    1

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

GET Get Prontuario By Paciente Id - Status Code 200 - Sem Atendimentos
    [Documentation]    Seeds a fresh Paciente with no Atendimento and fetches its Prontuário, expecting HTTP
    ...    200 with an empty atendimentos list.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${pacienteId}    ${cpf}    ${cartaoSus}    ${nomePaciente}=    Seed A Paciente
    ${response}=    Call GET Get Prontuario By Paciente Id    ${pacienteId}
    Should Be Equal As Integers    ${response.status_code}    200
    Length Should Be    ${response.json()['atendimentos']}    0

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

GET Get Prontuario By Paciente Id - Status Code 404
    [Documentation]    Fetches a Prontuário by a random, non-existent pacienteId, expecting HTTP 404.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${pacienteIdInexistente}=    Evaluate    str(uuid.uuid4())    modules=uuid
    ${response}=    Call GET Get Prontuario By Paciente Id    ${pacienteIdInexistente}
    Should Be Equal As Integers    ${response.status_code}    404

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Prontuario - Get By Paciente Id - GET - By HTTP Status Code
    [Documentation]    Executes the appropriate Get Prontuario By Paciente Id test scenario based on the
    ...    provided HTTP status code.
    [Arguments]    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Run Keyword If    '${meuHTTP}' == '200'    GET Get Prontuario By Paciente Id - Status Code 200
    ...    ELSE IF    '${meuHTTP}' == '404'    GET Get Prontuario By Paciente Id - Status Code 404
    ...    ELSE    Fail    HTTP status code '${meuHTTP}' is not supported for Get Prontuario By Paciente Id test scenario.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
