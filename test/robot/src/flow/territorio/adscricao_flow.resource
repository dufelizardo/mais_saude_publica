*** Settings ***
Library     String
Library     Collections
Resource    ../../pom/territorio/adscricao_pom.resource
Resource    ./cadastro_territorial_flow.resource
Resource    ../programas/acompanhamento/acompanhamento_flow.resource
Resource    ../common/schema_validation_flow.resource
Resource    ../../resource/config/schema/schema.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
Seed Familia Adscrita
    [Documentation]    Creates an area with a team, a household and a family of one; returns the patient, the team, the unit and the family.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${area}=    Seed Area Territorial
    ${geometria}=    Quadrado GeoJSON    -47.0    -22.0    0.01
    ${micro}=    Seed Microarea    ${area}    07    ${geometria}
    ${equipe}    ${nome}    ${unidadeId}=    Seed Uma Equipe
    ${resp}=    Create Dictionary    equipeId=${equipe}
    POST Responsabilidade Territorial    ${area}    ${resp}
    ${domicilio}    ${imovel}=    Seed Domicilio    ${micro}
    ${paciente}    ${cpf}    ${cns}    ${nomePaciente}=    Seed A Paciente
    ${payload}=    Create Dictionary    domicilioId=${domicilio}    responsavelId=${paciente}
    ${f}=    POST Familia    ${payload}
    Should Be Equal As Integers    ${f.status_code}    201
    RETURN    ${paciente}    ${equipe}    ${unidadeId}    ${f.json()}[domicilios][0][familias][0][uuid]    ${nomePaciente}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Seed Programa Adulto Na Unidade
    [Documentation]    Creates a program for adults (18+) offered by the unit; returns its uuid.
    [Arguments]    ${unidade}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${sufixo}=    Generate Random String    8    [UPPER][NUMBERS]
    ${categoria}=    Id Da Categoria    Crônicos
    ${programaPayload}=    Create Dictionary    codigo=ROBOT_${sufixo}    nome=Robot Adultos ${sufixo}    categoriaId=${categoria}    idadeMinima=${18}
    ${p}=    POST Create Programa    ${programaPayload}
    Should Be Equal As Integers    ${p.status_code}    201
    ${programa}=    Set Variable    ${p.json()}[resumo][uuid]
    ${part}=    Create Dictionary    unidadeId=${unidade}
    ${r}=    POST Participacao Programa    ${programa}    ${part}
    Should Be Equal As Integers    ${r.status_code}    200
    RETURN    ${programa}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Adscricao - Pelo Domicilio - Status Code 200
    [Documentation]    The family member is adscribed to the area team, with a valid schema.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${paciente}    ${equipe}    ${unidade}    ${familia}    ${nome}=    Seed Familia Adscrita
    ${response}=    GET Adscricao Do Paciente    ${paciente}
    Should Be Equal As Integers    ${response.status_code}    200
    Validate Object Schema    ${response.json()}    ${ADSCRICAO_PACIENTE_SCHEMA}
    Should Be Equal    ${response.json()}[atual][origem]    TERRITORIO
    Should Be Equal    ${response.json()}[atual][equipeId]    ${equipe}
    Should Be Equal    ${response.json()}[microareaCodigo]    07
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Adscricao - Manual - Status Code 200
    [Documentation]    A manual adscription is not replaced by the household one; ended, the household one comes back.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${paciente}    ${equipe}    ${unidade}    ${familia}    ${nome}=    Seed Familia Adscrita
    ${outra}    ${nomeOutra}    ${unidadeOutra}=    Seed Uma Equipe
    ${payload}=    Create Dictionary    equipeId=${outra}    motivo=Escolha do paciente
    ${response}=    POST Adscricao Manual    ${paciente}    ${payload}
    Should Be Equal As Integers    ${response.status_code}    200
    Should Be Equal    ${response.json()}[atual][origem]    MANUAL
    ${fim}=    Create Dictionary    motivo=Voltou para a equipe da área
    ${encerrada}=    POST Encerrar Adscricao    ${paciente}    ${fim}
    Should Be Equal As Integers    ${encerrada.status_code}    200
    Should Be Equal    ${encerrada.json()}[atual][origem]    TERRITORIO
    Should Be Equal    ${encerrada.json()}[atual][equipeId]    ${equipe}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Adscricao - Sem Motivo - Status Code 400
    [Documentation]    The manual adscription needs a reason.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${paciente}    ${cpf}    ${cns}    ${nome}=    Seed A Paciente
    ${equipe}    ${nomeEquipe}    ${unidade}=    Seed Uma Equipe
    ${payload}=    Create Dictionary    equipeId=${equipe}
    ${response}=    POST Adscricao Manual    ${paciente}    ${payload}
    Should Be Equal As Integers    ${response.status_code}    400
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Adscricao - Encerrar Do Territorio - Status Code 422
    [Documentation]    The household adscription does not end through this endpoint.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${paciente}    ${equipe}    ${unidade}    ${familia}    ${nome}=    Seed Familia Adscrita
    ${fim}=    Create Dictionary    motivo=Tentativa
    ${response}=    POST Encerrar Adscricao    ${paciente}    ${fim}
    Should Be Equal As Integers    ${response.status_code}    422
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Adscricao - Cobertura - Status Code 200
    [Documentation]    The team coverage counts the adscribed people and the micro-areas.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${paciente}    ${equipe}    ${unidade}    ${familia}    ${nome}=    Seed Familia Adscrita
    ${response}=    GET Cobertura Da Equipe    ${equipe}
    Should Be Equal As Integers    ${response.status_code}    200
    Validate Object Schema    ${response.json()}    ${COBERTURA_SCHEMA}
    Should Be Equal As Integers    ${response.json()}[pessoasAdscritas]    1
    Should Be Equal As Integers    ${response.json()}[familias]    1
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Adscricao - Elegiveis - Status Code 200
    [Documentation]    The adscribed adult is eligible for an adult program; enrolled, no longer.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${paciente}    ${equipe}    ${unidade}    ${familia}    ${nome}=    Seed Familia Adscrita
    ${programa}=    Seed Programa Adulto Na Unidade    ${unidade}
    ${params}=    Create Dictionary    equipeId=${equipe}
    ${response}=    GET Elegiveis Do Programa    ${programa}    ${params}
    Should Be Equal As Integers    ${response.status_code}    200
    Validate Object Schema    ${response.json()}    ${ELEGIVEIS_SCHEMA}
    Length Should Be    ${response.json()}[elegiveis]    1
    ${insc}=    Create Dictionary    pacienteId=${paciente}    unidadeId=${unidade}
    ${i}=    POST Inscrever Paciente No Programa    ${programa}    ${insc}
    Should Be Equal As Integers    ${i.status_code}    201
    ${depois}=    GET Elegiveis Do Programa    ${programa}    ${params}
    Length Should Be    ${depois.json()}[elegiveis]    0
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Adscricao - Paciente - Status Code 404
    [Documentation]    Fetches the adscription of a patient that does not exist.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${uuid}=    Evaluate    str(uuid.uuid4())    modules=uuid
    ${response}=    GET Adscricao Do Paciente    ${uuid}
    Should Be Equal As Integers    ${response.status_code}    404
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Adscricao - By HTTP Status Code
    [Documentation]    Executes the adscription scenario for the given HTTP status code.
    [Arguments]    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Run Keyword If    '${meuHTTP}' == '200-domicilio'    Adscricao - Pelo Domicilio - Status Code 200
    ...    ELSE IF    '${meuHTTP}' == '200-manual'    Adscricao - Manual - Status Code 200
    ...    ELSE IF    '${meuHTTP}' == '400-motivo'    Adscricao - Sem Motivo - Status Code 400
    ...    ELSE IF    '${meuHTTP}' == '422-territorio'    Adscricao - Encerrar Do Territorio - Status Code 422
    ...    ELSE IF    '${meuHTTP}' == '200-cobertura'    Adscricao - Cobertura - Status Code 200
    ...    ELSE IF    '${meuHTTP}' == '200-elegiveis'    Adscricao - Elegiveis - Status Code 200
    ...    ELSE IF    '${meuHTTP}' == '404'    Adscricao - Paciente - Status Code 404
    ...    ELSE    Fail    HTTP status code '${meuHTTP}' is not supported for Adscricao test scenario.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
