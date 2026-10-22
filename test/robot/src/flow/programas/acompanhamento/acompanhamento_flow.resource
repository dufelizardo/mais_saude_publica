*** Settings ***
Library     String
Library     Collections
Resource    ../../../pom/programas/acompanhamento/acompanhamento_pom.resource
Resource    ../programa/programa_flow.resource
Resource    ../../paciente/create_paciente/create_paciente_flow.resource
Resource    ../../common/schema_validation_flow.resource
Resource    ../../../resource/config/schema/schema.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
Seed Programa Com Unidade
    [Documentation]    Creates a program offered by a fresh UBS and returns the program uuid, its name and the unit uuid.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${programa}    ${nome}=    Seed Programa
    ${unidade}    ${nomeUnidade}=    Seed Unidade Para Programa
    ${payload}=    Create Dictionary    unidadeId=${unidade}
    ${response}=    POST Participacao Programa    ${programa}    ${payload}
    Should Be Equal As Integers    ${response.status_code}    200
    RETURN    ${programa}    ${nome}    ${unidade}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Seed Acompanhamento
    [Documentation]    Enrolls a fresh patient in a fresh program and returns the follow-up uuid, program and patient.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${programa}    ${nome}    ${unidade}=    Seed Programa Com Unidade
    ${paciente}    ${cpf}    ${cns}    ${nomePaciente}=    Seed A Paciente
    ${payload}=    Create Dictionary    pacienteId=${paciente}    unidadeId=${unidade}
    ${response}=    POST Inscrever Paciente No Programa    ${programa}    ${payload}
    Should Be Equal As Integers    ${response.status_code}    201
    RETURN    ${response.json()}[uuid]    ${programa}    ${paciente}    ${unidade}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Acompanhamento - Inscricao - Status Code 201
    [Documentation]    Enrolls a patient: active follow-up with the entry event.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${programa}    ${nome}    ${unidade}=    Seed Programa Com Unidade
    ${paciente}    ${cpf}    ${cns}    ${nomePaciente}=    Seed A Paciente
    ${payload}=    Create Dictionary    pacienteId=${paciente}    unidadeId=${unidade}
    ${response}=    POST Inscrever Paciente No Programa    ${programa}    ${payload}
    Should Be Equal As Integers    ${response.status_code}    201
    Validate Object Schema    ${response.json()}    ${ACOMPANHAMENTO_SCHEMA}
    Should Be Equal    ${response.json()}[situacao]    ATIVO
    Length Should Be    ${response.json()}[eventos]    1
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Acompanhamento - Repetido - Status Code 409
    [Documentation]    One follow-up in force per patient and program.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${acompanhamento}    ${programa}    ${paciente}    ${unidade}=    Seed Acompanhamento
    ${payload}=    Create Dictionary    pacienteId=${paciente}    unidadeId=${unidade}
    ${response}=    POST Inscrever Paciente No Programa    ${programa}    ${payload}
    Should Be Equal As Integers    ${response.status_code}    409
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Acompanhamento - Unidade Que Nao Oferece - Status Code 422
    [Documentation]    The responsible unit must offer the program.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${programa}    ${nome}    ${unidade}=    Seed Programa Com Unidade
    ${outra}    ${nomeOutra}=    Seed Unidade Para Programa
    ${paciente}    ${cpf}    ${cns}    ${nomePaciente}=    Seed A Paciente
    ${payload}=    Create Dictionary    pacienteId=${paciente}    unidadeId=${outra}
    ${response}=    POST Inscrever Paciente No Programa    ${programa}    ${payload}
    Should Be Equal As Integers    ${response.status_code}    422
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Acompanhamento - Faltoso E Saida - Status Code 200
    [Documentation]    Marks absent, then registers discharge; the program counts reflect it.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${acompanhamento}    ${programa}    ${paciente}    ${unidade}=    Seed Acompanhamento
    ${faltoso}=    Create Dictionary    situacao=FALTOSO    motivo=Faltou às três últimas consultas
    ${response}=    POST Situacao Acompanhamento    ${acompanhamento}    ${faltoso}
    Should Be Equal As Integers    ${response.status_code}    200
    Should Be Equal    ${response.json()}[situacao]    FALTOSO
    ${inscritos}=    GET Inscritos Do Programa    ${programa}
    Validate Object Schema    ${inscritos.json()}    ${INSCRITOS_PROGRAMA_SCHEMA}
    Should Be Equal As Integers    ${inscritos.json()}[faltosos]    1
    ${saida}=    Create Dictionary    motivoSaida=ALTA    observacao=Metas atingidas
    ${encerrado}=    POST Encerrar Acompanhamento    ${acompanhamento}    ${saida}
    Should Be Equal As Integers    ${encerrado.status_code}    200
    Should Be Equal    ${encerrado.json()}[situacao]    CONCLUIDO
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Acompanhamento - Saida Sem Descricao - Status Code 400
    [Documentation]    The exit needs a description.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${acompanhamento}    ${programa}    ${paciente}    ${unidade}=    Seed Acompanhamento
    ${saida}=    Create Dictionary    motivoSaida=ABANDONO
    ${response}=    POST Encerrar Acompanhamento    ${acompanhamento}    ${saida}
    Should Be Equal As Integers    ${response.status_code}    400
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Acompanhamento - Programas Do Paciente - Status Code 200
    [Documentation]    The patient's programs: in force and closed ones.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${acompanhamento}    ${programa}    ${paciente}    ${unidade}=    Seed Acompanhamento
    ${response}=    GET Programas Do Paciente    ${paciente}
    Should Be Equal As Integers    ${response.status_code}    200
    Validate Object Schema    ${response.json()}    ${ACOMPANHAMENTOS_DO_PACIENTE_SCHEMA}
    Length Should Be    ${response.json()}[vigentes]    1
    Should Be Equal As Integers    ${response.json()}[restritos]    0
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Acompanhamento - Inexistente - Status Code 404
    [Documentation]    A follow-up that does not exist.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${uuid}=    Evaluate    str(uuid.uuid4())    modules=uuid
    ${payload}=    Create Dictionary    situacao=FALTOSO    motivo=Inexistente
    ${response}=    POST Situacao Acompanhamento    ${uuid}    ${payload}
    Should Be Equal As Integers    ${response.status_code}    404
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Acompanhamento - By HTTP Status Code
    [Documentation]    Executes the follow-up scenario for the given HTTP status code.
    [Arguments]    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Run Keyword If    '${meuHTTP}' == '201'    Acompanhamento - Inscricao - Status Code 201
    ...    ELSE IF    '${meuHTTP}' == '409'    Acompanhamento - Repetido - Status Code 409
    ...    ELSE IF    '${meuHTTP}' == '422'    Acompanhamento - Unidade Que Nao Oferece - Status Code 422
    ...    ELSE IF    '${meuHTTP}' == '200-situacao'    Acompanhamento - Faltoso E Saida - Status Code 200
    ...    ELSE IF    '${meuHTTP}' == '400'    Acompanhamento - Saida Sem Descricao - Status Code 400
    ...    ELSE IF    '${meuHTTP}' == '200-paciente'    Acompanhamento - Programas Do Paciente - Status Code 200
    ...    ELSE IF    '${meuHTTP}' == '404'    Acompanhamento - Inexistente - Status Code 404
    ...    ELSE    Fail    HTTP status code '${meuHTTP}' is not supported for Acompanhamento test scenario.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
