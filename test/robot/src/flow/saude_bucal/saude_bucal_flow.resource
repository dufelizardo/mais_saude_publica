*** Settings ***
Library     String
Library     Collections
Resource    ../../pom/saude_bucal/saude_bucal_pom.resource
Resource    ../atendimento/create_atendimento/create_atendimento_flow.resource
Resource    ../common/schema_validation_flow.resource
Resource    ../../resource/config/schema/schema.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
Id Da Especialidade
    [Documentation]    Returns the uuid of a specialty by code.
    [Arguments]    ${codigo}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${r}=    GET Especialidades
    ${ids}=    Evaluate    [e["uuid"] for e in $r.json() if e["codigo"] == $codigo]
    RETURN    ${ids}[0]
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Seed Atendimento Odontologico
    [Documentation]    Creates a dental-care visit (specialty ODONTOLOGIA); returns the visit, the patient and the dentist registration.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${unidadeId}=    Seed A Unidade De Saude Uuid
    ${matricula}=    Seed A Profissional Matricula
    ${pacienteId}    ${cpf}    ${cartaoSus}    ${nomePaciente}=    Seed A Paciente
    ${payload}=    Build Atendimento Payload    ${pacienteId}    ${matricula}    ${unidadeId}
    ${odonto}=    Id Da Especialidade    ODONTOLOGIA
    Set To Dictionary    ${payload}    especialidadeId=${odonto}
    ${r}=    POST Create Atendimento    ${payload}
    Should Be Equal As Integers    ${r.status_code}    201
    ${atendimentoId}=    Buscar Atendimento Uuid Por Paciente    ${pacienteId}
    RETURN    ${atendimentoId}    ${pacienteId}    ${matricula}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Payload Da Avaliacao
    [Documentation]    Builds a dental assessment payload.
    [Arguments]    ${atendimento}    ${matricula}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${payload}=    Create Dictionary    atendimentoId=${atendimento}    profissionalMatricula=${matricula}    queixaPrincipal=Dor no dente
    ...    diagnostico=Cárie no 36    risco=MEDIO    necessidadeTratamento=PRIORITARIA
    RETURN    ${payload}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Especialidades - Catalogo - Status Code 200
    [Documentation]    The catalog brings dentistry, flagged as dental.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${r}=    GET Especialidades
    Should Be Equal As Integers    ${r.status_code}    200
    ${odonto}=    Evaluate    [e for e in $r.json() if e["codigo"] == "ODONTOLOGIA"][0]
    Validate Object Schema    ${odonto}    ${ESPECIALIDADE_SCHEMA}
    Should Be True    ${odonto}[odontologica]
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Especialidade - Codigo Repetido - Status Code 409
    [Documentation]    A repeated code is refused.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${payload}=    Create Dictionary    codigo=ODONTOLOGIA    nome=Outra
    ${r}=    POST Especialidade    ${payload}
    Should Be Equal As Integers    ${r.status_code}    409
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Avaliacao - Registro - Status Code 201
    [Documentation]    The dental assessment in a dental-care visit.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${atendimento}    ${paciente}    ${matricula}=    Seed Atendimento Odontologico
    ${payload}=    Payload Da Avaliacao    ${atendimento}    ${matricula}
    ${r}=    POST Avaliacao Odontologica    ${payload}
    Should Be Equal As Integers    ${r.status_code}    201
    Validate Object Schema    ${r.json()}    ${AVALIACAO_ODONTOLOGICA_SCHEMA}
    Should Be Equal    ${r.json()}[risco]    MEDIO
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Avaliacao - Atendimento Sem Odontologia - Status Code 422
    [Documentation]    Only in a dental-care visit.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${atendimento}    ${paciente}    ${matricula}    ${unidade}=    Seed Um Atendimento
    ${payload}=    Payload Da Avaliacao    ${atendimento}    ${matricula}
    ${r}=    POST Avaliacao Odontologica    ${payload}
    Should Be Equal As Integers    ${r.status_code}    422
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Avaliacao - Sem Risco - Status Code 400
    [Documentation]    The risk is required.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${atendimento}    ${paciente}    ${matricula}=    Seed Atendimento Odontologico
    ${payload}=    Create Dictionary    atendimentoId=${atendimento}    profissionalMatricula=${matricula}    necessidadeTratamento=ELETIVA
    ${r}=    POST Avaliacao Odontologica    ${payload}
    Should Be Equal As Integers    ${r.status_code}    400
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Avaliacao - Retificacao - Status Code 201
    [Documentation]    Rectification creates a new version; the old one is marked; twice is refused.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${atendimento}    ${paciente}    ${matricula}=    Seed Atendimento Odontologico
    ${payload}=    Payload Da Avaliacao    ${atendimento}    ${matricula}
    ${a}=    POST Avaliacao Odontologica    ${payload}
    Set To Dictionary    ${payload}    diagnostico=Cárie no 46    motivoRetificacao=Dente errado
    ${r}=    POST Retificacao Avaliacao Odontologica    ${a.json()}[uuid]    ${payload}
    Should Be Equal As Integers    ${r.status_code}    201
    Should Be Equal    ${r.json()}[retificacaoDeUuid]    ${a.json()}[uuid]
    ${o}=    GET Avaliacao Odontologica    ${a.json()}[uuid]
    Should Be True    ${o.json()}[retificado]
    ${r2}=    POST Retificacao Avaliacao Odontologica    ${a.json()}[uuid]    ${payload}
    Should Be Equal As Integers    ${r2.status_code}    422
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Avaliacao - Retificacao Sem Motivo - Status Code 400
    [Documentation]    Rectification needs the reason.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${atendimento}    ${paciente}    ${matricula}=    Seed Atendimento Odontologico
    ${payload}=    Payload Da Avaliacao    ${atendimento}    ${matricula}
    ${a}=    POST Avaliacao Odontologica    ${payload}
    ${r}=    POST Retificacao Avaliacao Odontologica    ${a.json()}[uuid]    ${payload}
    Should Be Equal As Integers    ${r.status_code}    400
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Avaliacao - No Prontuario - Status Code 200
    [Documentation]    The assessment appears in the patient record.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${atendimento}    ${paciente}    ${matricula}=    Seed Atendimento Odontologico
    ${payload}=    Payload Da Avaliacao    ${atendimento}    ${matricula}
    POST Avaliacao Odontologica    ${payload}
    ${r}=    GET Prontuario Do Paciente    ${paciente}
    Should Be Equal As Integers    ${r.status_code}    200
    ${diagnosticos}=    Evaluate    [a["diagnostico"] for at in $r.json()["atendimentos"] for a in (at.get("avaliacoesOdontologicas") or [])]
    Should Contain    ${diagnosticos}    Cárie no 36
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Avaliacao - Inexistente - Status Code 404
    [Documentation]    Reading an assessment that does not exist.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${uuid}=    Evaluate    str(uuid.uuid4())    modules=uuid
    ${r}=    GET Avaliacao Odontologica    ${uuid}
    Should Be Equal As Integers    ${r.status_code}    404
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Saude Bucal - By HTTP Status Code
    [Documentation]    Executes the SB1 scenario for the given HTTP status code.
    [Arguments]    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Run Keyword If    '${meuHTTP}' == '200-especialidades'    Especialidades - Catalogo - Status Code 200
    ...    ELSE IF    '${meuHTTP}' == '409-especialidade'    Especialidade - Codigo Repetido - Status Code 409
    ...    ELSE IF    '${meuHTTP}' == '201-avaliacao'    Avaliacao - Registro - Status Code 201
    ...    ELSE IF    '${meuHTTP}' == '422-avaliacao'    Avaliacao - Atendimento Sem Odontologia - Status Code 422
    ...    ELSE IF    '${meuHTTP}' == '400-avaliacao'    Avaliacao - Sem Risco - Status Code 400
    ...    ELSE IF    '${meuHTTP}' == '201-retificacao'    Avaliacao - Retificacao - Status Code 201
    ...    ELSE IF    '${meuHTTP}' == '400-retificacao'    Avaliacao - Retificacao Sem Motivo - Status Code 400
    ...    ELSE IF    '${meuHTTP}' == '200-prontuario'    Avaliacao - No Prontuario - Status Code 200
    ...    ELSE IF    '${meuHTTP}' == '404-avaliacao'    Avaliacao - Inexistente - Status Code 404
    ...    ELSE    Fail    HTTP status code '${meuHTTP}' is not supported for Saude Bucal test scenario.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
