*** Settings ***
Library     String
Library     Collections
Resource    ../../pom/saude_bucal/procedimento_odontologico_pom.resource
Resource    ../../pom/saude_bucal/odontograma_pom.resource
Resource    ./saude_bucal_flow.resource
Resource    ../common/schema_validation_flow.resource
Resource    ../../resource/config/schema/schema.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
Id Do Item Do Catalogo
    [Documentation]    Returns the uuid of a procedure catalog item by code.
    [Arguments]    ${codigo}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${r}=    GET Procedimentos Catalogo
    ${ids}=    Evaluate    [c["uuid"] for c in $r.json() if c["codigo"] == $codigo]
    RETURN    ${ids}[0]
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Payload Do Procedimento
    [Documentation]    Builds a procedure in the visit, from the catalog, with the optional tooth and faces.
    [Arguments]    ${atendimento}    ${matricula}    ${codigo}    ${dente}=${None}    ${faces}=${None}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${item}=    Id Do Item Do Catalogo    ${codigo}
    ${quando}=    Evaluate    (datetime.datetime.now() - datetime.timedelta(hours=1)).replace(microsecond=0).isoformat()    modules=datetime
    ${p}=    Create Dictionary    atendimentoId=${atendimento}    procedimentoCatalogoId=${item}    profissionalMatricula=${matricula}
    ...    dataRealizacao=${quando}    status=REALIZADO
    IF    $dente is not None    Set To Dictionary    ${p}    dente=${dente}
    IF    $faces is not None    Set To Dictionary    ${p}    faces=${faces}
    RETURN    ${p}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Procedimentos Do Atendimento No Prontuario
    [Documentation]    The procedures registered directly in the visit, from the patient record.
    [Arguments]    ${paciente}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${r}=    GET Prontuario Do Paciente    ${paciente}
    Should Be Equal As Integers    ${r.status_code}    200
    ${lista}=    Evaluate    [p for a in $r.json()["atendimentos"] for p in (a.get("procedimentos") or [])]
    RETURN    ${lista}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Procedimento Odontologico - Catalogo - Status Code 200
    [Documentation]    The catalog brings the composite resin restoration, which records Restored.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${r}=    GET Procedimentos Catalogo
    Should Be Equal As Integers    ${r.status_code}    200
    ${resina}=    Evaluate    [c for c in $r.json() if c["codigo"] == "RESTAURACAO_RESINA"][0]
    Validate Object Schema    ${resina}    ${PROCEDIMENTO_CATALOGO_SCHEMA}
    Should Be Equal    ${resina}[alcance]    FACE
    Should Be Equal    ${resina}[condicaoResultanteNome]    Restaurado
    Should Be True    ${resina}[exigeDentista]
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Procedimento Odontologico - Item Do Catalogo - Status Code 201
    [Documentation]    Registers a general catalog item with a SIGTAP code.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${sufixo}=    Evaluate    uuid.uuid4().hex[:8].upper()    modules=uuid
    ${payload}=    Create Dictionary    codigo=TSTRB_${sufixo}    nome=Teste Robot ${sufixo}    sigtap=0301010072
    ${r}=    POST Item Catalogo Procedimento    ${payload}
    Should Be Equal As Integers    ${r.status_code}    201
    Validate Object Schema    ${r.json()}    ${PROCEDIMENTO_CATALOGO_SCHEMA}
    Should Be Equal    ${r.json()}[alcance]    NENHUM
    Should Not Be True    ${r.json()}[odontologico]
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Procedimento Odontologico - Item Geral Com Dente - Status Code 422
    [Documentation]    Tooth and face are for a dental procedure only.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${sufixo}=    Evaluate    uuid.uuid4().hex[:8].upper()    modules=uuid
    ${payload}=    Create Dictionary    codigo=TSTRB_${sufixo}    nome=Teste    alcance=DENTE
    ${r}=    POST Item Catalogo Procedimento    ${payload}
    Should Be Equal As Integers    ${r.status_code}    422
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Procedimento Odontologico - Codigo Repetido - Status Code 409
    [Documentation]    The catalog code is unique.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${payload}=    Create Dictionary    codigo=CURATIVO    nome=Outro curativo
    ${r}=    POST Item Catalogo Procedimento    ${payload}
    Should Be Equal As Integers    ${r.status_code}    409
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Procedimento Odontologico - Restauracao Grava O Odontograma - Status Code 201
    [Documentation]    A realized restoration on 36 occlusal records Restored, with the procedure as origin.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${atendimento}    ${paciente}    ${matricula}=    Seed Atendimento Odontologico
    ${faces}=    Create List    OCLUSAL
    ${payload}=    Payload Do Procedimento    ${atendimento}    ${matricula}    RESTAURACAO_RESINA    ${36}    ${faces}
    ${r}=    POST Procedimento Do Atendimento    ${payload}
    Should Be Equal As Integers    ${r.status_code}    201
    ${lista}=    Procedimentos Do Atendimento No Prontuario    ${paciente}
    Validate Object Schema    ${lista}[0]    ${PROCEDIMENTO_ODONTOLOGICO_SCHEMA}
    Should Be Equal As Integers    ${lista}[0][dente]    36
    ${o}=    GET Odontograma    ${paciente}
    ${registro}=    Set Variable    ${o.json()}[dentes][0][faces][0][registro]
    Should Be Equal    ${registro}[condicaoCodigo]    RESTAURADO
    Should Be Equal    ${registro}[procedimentoNome]    Restauração em resina composta
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Procedimento Odontologico - Sem Face - Status Code 400
    [Documentation]    The restoration needs the face.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${atendimento}    ${paciente}    ${matricula}=    Seed Atendimento Odontologico
    ${payload}=    Payload Do Procedimento    ${atendimento}    ${matricula}    RESTAURACAO_RESINA    ${36}
    ${r}=    POST Procedimento Do Atendimento    ${payload}
    Should Be Equal As Integers    ${r.status_code}    400
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Procedimento Odontologico - Face Que O Dente Nao Tem - Status Code 422
    [Documentation]    An anterior tooth has no occlusal face.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${atendimento}    ${paciente}    ${matricula}=    Seed Atendimento Odontologico
    ${faces}=    Create List    OCLUSAL
    ${payload}=    Payload Do Procedimento    ${atendimento}    ${matricula}    RESTAURACAO_RESINA    ${11}    ${faces}
    ${r}=    POST Procedimento Do Atendimento    ${payload}
    Should Be Equal As Integers    ${r.status_code}    422
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Procedimento Odontologico - Fora Do Atendimento Odontologico - Status Code 422
    [Documentation]    A dental procedure only in a dental-care visit.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${atendimento}    ${paciente}    ${matricula}    ${unidade}=    Seed Um Atendimento
    ${payload}=    Payload Do Procedimento    ${atendimento}    ${matricula}    APLICACAO_FLUOR
    ${r}=    POST Procedimento Do Atendimento    ${payload}
    Should Be Equal As Integers    ${r.status_code}    422
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Procedimento Odontologico - Retificacao Anula E Regrava - Status Code 201
    [Documentation]    Rectifying the tooth of an extraction voids the 48 record and records 47.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${atendimento}    ${paciente}    ${matricula}=    Seed Atendimento Odontologico
    ${payload}=    Payload Do Procedimento    ${atendimento}    ${matricula}    EXODONTIA    ${48}
    POST Procedimento Do Atendimento    ${payload}
    ${lista}=    Procedimentos Do Atendimento No Prontuario    ${paciente}
    ${correcao}=    Payload Do Procedimento    ${atendimento}    ${matricula}    EXODONTIA    ${47}
    Set To Dictionary    ${correcao}    motivoRetificacao=Dente errado
    ${r}=    POST Retificacao Do Procedimento    ${lista}[0][uuid]    ${correcao}
    Should Be Equal As Integers    ${r.status_code}    201
    ${o}=    GET Odontograma    ${paciente}
    ${dentes}=    Evaluate    [d["dente"] for d in $o.json()["dentes"]]
    Should Be Equal As Strings    ${dentes}    [47]
    ${h}=    GET Historico Do Dente    ${paciente}    48
    Should Be True    ${h.json()}[0][anulado]
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Procedimento Odontologico - Sem Consulta Nem Atendimento - Status Code 400
    [Documentation]    The procedure needs the consultation or the visit.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${quando}=    Evaluate    datetime.datetime.now().replace(microsecond=0).isoformat()    modules=datetime
    ${payload}=    Create Dictionary    profissionalMatricula=000    tipo=Curativo    dataRealizacao=${quando}    status=REALIZADO
    ${r}=    POST Procedimento Do Atendimento    ${payload}
    Should Be Equal As Integers    ${r.status_code}    400
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Procedimento Odontologico - By HTTP Status Code
    [Documentation]    Executes the SB3a scenario for the given HTTP status code.
    [Arguments]    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Run Keyword If    '${meuHTTP}' == '200-catalogo'    Procedimento Odontologico - Catalogo - Status Code 200
    ...    ELSE IF    '${meuHTTP}' == '201-item'    Procedimento Odontologico - Item Do Catalogo - Status Code 201
    ...    ELSE IF    '${meuHTTP}' == '422-item'    Procedimento Odontologico - Item Geral Com Dente - Status Code 422
    ...    ELSE IF    '${meuHTTP}' == '409-item'    Procedimento Odontologico - Codigo Repetido - Status Code 409
    ...    ELSE IF    '${meuHTTP}' == '201-restauracao'    Procedimento Odontologico - Restauracao Grava O Odontograma - Status Code 201
    ...    ELSE IF    '${meuHTTP}' == '400-face'    Procedimento Odontologico - Sem Face - Status Code 400
    ...    ELSE IF    '${meuHTTP}' == '422-face'    Procedimento Odontologico - Face Que O Dente Nao Tem - Status Code 422
    ...    ELSE IF    '${meuHTTP}' == '422-atendimento'    Procedimento Odontologico - Fora Do Atendimento Odontologico - Status Code 422
    ...    ELSE IF    '${meuHTTP}' == '201-retificacao'    Procedimento Odontologico - Retificacao Anula E Regrava - Status Code 201
    ...    ELSE IF    '${meuHTTP}' == '400-atendimento'    Procedimento Odontologico - Sem Consulta Nem Atendimento - Status Code 400
    ...    ELSE    Fail    HTTP status code '${meuHTTP}' is not supported for Procedimento Odontologico test scenario.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
