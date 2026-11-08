*** Settings ***
Library     String
Library     Collections
Library     DateTime
Resource    ../../pom/transporte/transporte_pom.resource
Resource    ../urgencia/urgencia_flow.resource
Resource    ../common/schema_validation_flow.resource
Resource    ../../resource/config/schema/schema.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
Placa Aleatoria
    [Documentation]    A random Mercosul plate.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${letras}=    Generate Random String    3    [UPPER]
    ${a}=    Generate Random String    1    [NUMBERS]
    ${b}=    Generate Random String    1    [UPPER]
    ${c}=    Generate Random String    2    [NUMBERS]
    RETURN    ${letras}${a}${b}${c}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Seed Viatura
    [Documentation]    Registers a vehicle of the type at the unit and returns its uuid and prefix.
    [Arguments]    ${tipo}    ${unidade}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${placa}=    Placa Aleatoria
    ${payload}=    Create Dictionary    placa=${placa}    prefixo=RB-${placa}    tipo=${tipo}    unidadeId=${unidade}    maca=${True}
    ${r}=    POST Veiculo    ${payload}
    Should Be Equal As Integers    ${r.status_code}    201
    RETURN    ${r.json()}[uuid]    ${r.json()}[prefixo]
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Plantao De Agora
    [Documentation]    Registers a 12-hour shift starting an hour ago with the given crew list.
    [Arguments]    ${veiculo}    ${tripulacao}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${inicio}=    Get Current Date    UTC    increment=-1 hour    result_format=%Y-%m-%dT%H:%M:%SZ
    ${fim}=    Get Current Date    UTC    increment=11 hours    result_format=%Y-%m-%dT%H:%M:%SZ
    ${payload}=    Create Dictionary    veiculoId=${veiculo}    inicio=${inicio}    fim=${fim}    tripulacao=${tripulacao}
    ${r}=    POST Plantao Viatura    ${payload}
    RETURN    ${r}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Tripulante
    [Documentation]    One crew member.
    [Arguments]    ${matricula}    ${funcao}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${t}=    Create Dictionary    matricula=${matricula}    funcao=${funcao}
    RETURN    ${t}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Transporte - Frota - Status Code 201
    [Documentation]    A vehicle at a mobile-care unit; the schema and the history.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${base}=    Seed Central De Urgencia
    ${uuid}    ${prefixo}=    Seed Viatura    USB    ${base}
    ${r}=    GET Veiculo    ${uuid}
    Should Be Equal As Integers    ${r.status_code}    200
    Validate Object Schema    ${r.json()}    ${VEICULO_SCHEMA}
    Should Be Equal    ${r.json()}[situacao]    DISPONIVEL
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Transporte - Placa E Lotacao - Status Code 422
    [Documentation]    Invalid plate (400) and an SAMU unit without the mobile profile (422).
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${ubs}=    Seed A Unidade De Saude Uuid
    ${ruim}=    Create Dictionary    placa=AB12    prefixo=RB-RUIM    tipo=VAN_SANITARIA    unidadeId=${ubs}
    ${r400}=    POST Veiculo    ${ruim}
    Should Be Equal As Integers    ${r400.status_code}    400
    ${placa}=    Placa Aleatoria
    ${samu}=    Create Dictionary    placa=${placa}    prefixo=RB-${placa}    tipo=USA    unidadeId=${ubs}
    ${r422}=    POST Veiculo    ${samu}
    Should Be Equal As Integers    ${r422.status_code}    422
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Transporte - Plantao - Status Code 201
    [Documentation]    The minimum crew (422) and the same member in two shifts (409).
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${base}=    Seed Central De Urgencia
    ${usb}    ${p1}=    Seed Viatura    USB    ${base}
    ${outra}    ${p2}=    Seed Viatura    USB    ${base}
    ${condutor}=    Seed A Profissional Matricula
    ${tecnico}=    Seed A Profissional Matricula
    ${c}=    Tripulante    ${condutor}    CONDUTOR
    ${t}=    Tripulante    ${tecnico}    TECNICO_ENFERMAGEM
    ${so}=    Create List    ${c}
    ${r422}=    Plantao De Agora    ${usb}    ${so}
    Should Be Equal As Integers    ${r422.status_code}    422
    ${dupla}=    Create List    ${c}    ${t}
    ${r}=    Plantao De Agora    ${usb}    ${dupla}
    Should Be Equal As Integers    ${r.status_code}    201
    Validate Object Schema    ${r.json()}    ${PLANTAO_SCHEMA}
    Should Be True    ${r.json()}[vigente]
    ${r409}=    Plantao De Agora    ${outra}    ${dupla}
    Should Be Equal As Integers    ${r409.status_code}    409
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Transporte - Viatura No Samu - Status Code 200
    [Documentation]    The regulation commits the vehicle; dispatch puts it in service; closing frees it.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${base}=    Seed Central De Urgencia
    ${usb}    ${prefixo}=    Seed Viatura    USB    ${base}
    ${condutor}=    Seed A Profissional Matricula
    ${tecnico}=    Seed A Profissional Matricula
    ${c}=    Tripulante    ${condutor}    CONDUTOR
    ${t}=    Tripulante    ${tecnico}    TECNICO_ENFERMAGEM
    ${dupla}=    Create List    ${c}    ${t}
    ${rp}=    Plantao De Agora    ${usb}    ${dupla}
    Should Be Equal As Integers    ${rp.status_code}    201
    ${id}=    Abrir Chamado De Urgencia    ${base}
    ${regulador}=    Seed A Profissional Matricula
    ${reg}=    Create Dictionary    profissionalMatricula=${regulador}    prioridade=AMARELO    decisao=ENVIO_DE_RECURSO    conduta=Avaliar.    veiculoId=${usb}
    ${r}=    POST Regular Ocorrencia Urgencia    ${id}    ${reg}
    Should Be Equal As Integers    ${r.status_code}    200
    Should Be Equal    ${r.json()}[recursoIdentificacao]    ${prefixo}
    ${envio}=    Create Dictionary    tipo=ENVIO_DE_RECURSO
    ${re}=    POST Deslocamento Ocorrencia Urgencia    ${id}    ${envio}
    Should Be Equal As Integers    ${re.status_code}    200
    ${v1}=    GET Veiculo    ${usb}
    Should Be Equal    ${v1.json()}[situacao]    EM_SERVICO
    ${fim}=    Create Dictionary    profissionalMatricula=${regulador}    desfecho=DESISTENCIA    observacao=Paciente foi por conta própria.
    ${rf}=    POST Encerrar Ocorrencia Urgencia    ${id}    ${fim}
    Should Be Equal As Integers    ${rf.status_code}    200
    ${v2}=    GET Veiculo    ${usb}
    Should Be Equal    ${v2.json()}[situacao]    DISPONIVEL
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Transporte - By HTTP Status Code
    [Documentation]    Dispatches the scenario by HTTP status code.
    [Arguments]    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Run Keyword If    '${meuHTTP}' == '201-frota'    Transporte - Frota - Status Code 201
    Run Keyword If    '${meuHTTP}' == '422-lotacao'    Transporte - Placa E Lotacao - Status Code 422
    Run Keyword If    '${meuHTTP}' == '201-plantao'    Transporte - Plantao - Status Code 201
    Run Keyword If    '${meuHTTP}' == '200-samu'    Transporte - Viatura No Samu - Status Code 200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
