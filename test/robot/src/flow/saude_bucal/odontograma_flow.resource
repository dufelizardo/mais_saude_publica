*** Settings ***
Library     String
Library     Collections
Resource    ../../pom/saude_bucal/odontograma_pom.resource
Resource    ./saude_bucal_flow.resource
Resource    ../common/schema_validation_flow.resource
Resource    ../../resource/config/schema/schema.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
Id Da Condicao
    [Documentation]    Returns the uuid of an odontogram condition by code.
    [Arguments]    ${codigo}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${r}=    GET Condicoes Odontologicas
    ${ids}=    Evaluate    [c["uuid"] for c in $r.json() if c["codigo"] == $codigo]
    RETURN    ${ids}[0]
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Seed Avaliacao Odontologica
    [Documentation]    Creates a dental-care visit with an assessment; returns the assessment and the patient.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${atendimento}    ${paciente}    ${matricula}=    Seed Atendimento Odontologico
    ${payload}=    Payload Da Avaliacao    ${atendimento}    ${matricula}
    ${a}=    POST Avaliacao Odontologica    ${payload}
    Should Be Equal As Integers    ${a.status_code}    201
    RETURN    ${a.json()}[uuid]    ${paciente}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Marcacao
    [Documentation]    Builds one odontogram mark.
    [Arguments]    ${dente}    ${face}    ${codigo}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${condicao}=    Id Da Condicao    ${codigo}
    ${m}=    Create Dictionary    dente=${dente}    condicaoId=${condicao}
    IF    $face    Set To Dictionary    ${m}    face=${face}
    RETURN    ${m}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Odontograma - Catalogo - Status Code 200
    [Documentation]    The condition catalog brings caries as a face condition.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${r}=    GET Condicoes Odontologicas
    Should Be Equal As Integers    ${r.status_code}    200
    ${carie}=    Evaluate    [c for c in $r.json() if c["codigo"] == "CARIE"][0]
    Validate Object Schema    ${carie}    ${CONDICAO_ODONTOLOGICA_SCHEMA}
    Should Be Equal    ${carie}[alcance]    FACE
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Odontograma - Marcacao - Status Code 201
    [Documentation]    Marks caries on the occlusal of tooth 36 and an absent tooth 48.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${avaliacao}    ${paciente}=    Seed Avaliacao Odontologica
    ${m1}=    Marcacao    ${36}    OCLUSAL    CARIE
    ${m2}=    Marcacao    ${48}    ${None}    AUSENTE
    ${lista}=    Create List    ${m1}    ${m2}
    ${payload}=    Create Dictionary    marcacoes=${lista}
    ${r}=    POST Marcacoes Odontograma    ${avaliacao}    ${payload}
    Should Be Equal As Integers    ${r.status_code}    201
    Validate Object Schema    ${r.json()}    ${ODONTOGRAMA_SCHEMA}
    ${dentes}=    Evaluate    [d["dente"] for d in $r.json()["dentes"]]
    Should Be Equal As Strings    ${dentes}    [36, 48]
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Odontograma - Dente Fora Da FDI - Status Code 400
    [Documentation]    Tooth 19 does not exist in FDI.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${avaliacao}    ${paciente}=    Seed Avaliacao Odontologica
    ${m}=    Marcacao    ${19}    OCLUSAL    CARIE
    ${lista}=    Create List    ${m}
    ${payload}=    Create Dictionary    marcacoes=${lista}
    ${r}=    POST Marcacoes Odontograma    ${avaliacao}    ${payload}
    Should Be Equal As Integers    ${r.status_code}    400
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Odontograma - Face Que O Dente Nao Tem - Status Code 422
    [Documentation]    An anterior tooth has no occlusal face.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${avaliacao}    ${paciente}=    Seed Avaliacao Odontologica
    ${m}=    Marcacao    ${11}    OCLUSAL    CARIE
    ${lista}=    Create List    ${m}
    ${payload}=    Create Dictionary    marcacoes=${lista}
    ${r}=    POST Marcacoes Odontograma    ${avaliacao}    ${payload}
    Should Be Equal As Integers    ${r.status_code}    422
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Odontograma - Alcance Da Condicao - Status Code 422
    [Documentation]    Caries needs a face.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${avaliacao}    ${paciente}=    Seed Avaliacao Odontologica
    ${m}=    Marcacao    ${36}    ${None}    CARIE
    ${lista}=    Create List    ${m}
    ${payload}=    Create Dictionary    marcacoes=${lista}
    ${r}=    POST Marcacoes Odontograma    ${avaliacao}    ${payload}
    Should Be Equal As Integers    ${r.status_code}    422
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Odontograma - Projecao Por Data - Status Code 200
    [Documentation]    Before the restoration, the caries; today, the restoration.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${avaliacao}    ${paciente}=    Seed Avaliacao Odontologica
    ${antes}=    Evaluate    (datetime.date.today() - datetime.timedelta(days=10)).isoformat()    modules=datetime
    ${meio}=    Evaluate    (datetime.date.today() - datetime.timedelta(days=5)).isoformat()    modules=datetime
    ${m1}=    Marcacao    ${36}    OCLUSAL    CARIE
    ${l1}=    Create List    ${m1}
    ${p1}=    Create Dictionary    data=${antes}    marcacoes=${l1}
    POST Marcacoes Odontograma    ${avaliacao}    ${p1}
    ${m2}=    Marcacao    ${36}    OCLUSAL    RESTAURADO
    ${l2}=    Create List    ${m2}
    ${p2}=    Create Dictionary    marcacoes=${l2}
    POST Marcacoes Odontograma    ${avaliacao}    ${p2}
    ${params}=    Create Dictionary    data=${meio}
    ${r}=    GET Odontograma    ${paciente}    ${params}
    Should Be Equal As Integers    ${r.status_code}    200
    Should Be Equal    ${r.json()}[dentes][0][faces][0][registro][condicaoCodigo]    CARIE
    ${hoje}=    GET Odontograma    ${paciente}
    Should Be Equal    ${hoje.json()}[dentes][0][faces][0][registro][condicaoCodigo]    RESTAURADO
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Odontograma - Anulacao - Status Code 200
    [Documentation]    Voiding keeps the record in the history; twice is refused.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${avaliacao}    ${paciente}=    Seed Avaliacao Odontologica
    ${m}=    Marcacao    ${26}    MESIAL    CARIE
    ${lista}=    Create List    ${m}
    ${payload}=    Create Dictionary    marcacoes=${lista}
    POST Marcacoes Odontograma    ${avaliacao}    ${payload}
    ${h}=    GET Historico Do Dente    ${paciente}    26
    ${registro}=    Set Variable    ${h.json()}[0][uuid]
    ${motivo}=    Create Dictionary    motivo=Dente errado
    ${r}=    POST Anulacao Registro Odontograma    ${registro}    ${motivo}
    Should Be Equal As Integers    ${r.status_code}    200
    Should Be True    ${r.json()}[anulado]
    ${o}=    GET Odontograma    ${paciente}
    Should Be Empty    ${o.json()}[dentes]
    ${r2}=    POST Anulacao Registro Odontograma    ${registro}    ${motivo}
    Should Be Equal As Integers    ${r2.status_code}    422
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Odontograma - Anulacao Sem Motivo - Status Code 400
    [Documentation]    Voiding needs the reason.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${avaliacao}    ${paciente}=    Seed Avaliacao Odontologica
    ${m}=    Marcacao    ${16}    OCLUSAL    SELANTE
    ${lista}=    Create List    ${m}
    ${payload}=    Create Dictionary    marcacoes=${lista}
    POST Marcacoes Odontograma    ${avaliacao}    ${payload}
    ${h}=    GET Historico Do Dente    ${paciente}    16
    ${vazio}=    Create Dictionary
    ${r}=    POST Anulacao Registro Odontograma    ${h.json()}[0][uuid]    ${vazio}
    Should Be Equal As Integers    ${r.status_code}    400
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Odontograma - Avaliacao Inexistente - Status Code 404
    [Documentation]    Marking an assessment that does not exist.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${uuid}=    Evaluate    str(uuid.uuid4())    modules=uuid
    ${m}=    Marcacao    ${36}    OCLUSAL    CARIE
    ${lista}=    Create List    ${m}
    ${payload}=    Create Dictionary    marcacoes=${lista}
    ${r}=    POST Marcacoes Odontograma    ${uuid}    ${payload}
    Should Be Equal As Integers    ${r.status_code}    404
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Odontograma - By HTTP Status Code
    [Documentation]    Executes the SB2 scenario for the given HTTP status code.
    [Arguments]    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Run Keyword If    '${meuHTTP}' == '200-condicoes'    Odontograma - Catalogo - Status Code 200
    ...    ELSE IF    '${meuHTTP}' == '201-marcacao'    Odontograma - Marcacao - Status Code 201
    ...    ELSE IF    '${meuHTTP}' == '400-dente'    Odontograma - Dente Fora Da FDI - Status Code 400
    ...    ELSE IF    '${meuHTTP}' == '422-face'    Odontograma - Face Que O Dente Nao Tem - Status Code 422
    ...    ELSE IF    '${meuHTTP}' == '422-alcance'    Odontograma - Alcance Da Condicao - Status Code 422
    ...    ELSE IF    '${meuHTTP}' == '200-projecao'    Odontograma - Projecao Por Data - Status Code 200
    ...    ELSE IF    '${meuHTTP}' == '200-anulacao'    Odontograma - Anulacao - Status Code 200
    ...    ELSE IF    '${meuHTTP}' == '400-anulacao'    Odontograma - Anulacao Sem Motivo - Status Code 400
    ...    ELSE IF    '${meuHTTP}' == '404-avaliacao'    Odontograma - Avaliacao Inexistente - Status Code 404
    ...    ELSE    Fail    HTTP status code '${meuHTTP}' is not supported for Odontograma test scenario.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
