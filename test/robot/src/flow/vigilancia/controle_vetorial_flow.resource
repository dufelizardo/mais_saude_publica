*** Settings ***
Library     String
Library     Collections
Resource    ../../pom/vigilancia/controle_vetorial_pom.resource
Resource    ../territorio/cadastro_territorial_flow.resource
Resource    ./vigilancia_flow.resource
Resource    ../profissional/get_profissional_by_matricula/get_profissional_by_matricula_flow.resource
Resource    ../setor/create_setor/create_setor_flow.resource
Resource    ../common/schema_validation_flow.resource
Resource    ../../resource/config/schema/schema.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
    Controle de vetores (ADR-0162).
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
Seed Imovel Vetorial
    [Documentation]    An area with a team and a micro-area at a random place, with one property; returns the area, the micro-area and the property.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${lon}=    Evaluate    round(-60 + random.random() * 15, 4)    modules=random
    ${lat}=    Evaluate    round(-30 + random.random() * 20, 4)    modules=random
    ${area}    ${micro}=    Seed Area Com Equipe E Microarea    ${lon}    ${lat}
    ${sufixo}=    Generate Random String    5    [NUMBERS]
    ${endereco}=    Endereco Do Imovel    Rua Robot Vetor    ${sufixo}
    ${ilat}=    Evaluate    ${lat} + 0.005
    ${ilon}=    Evaluate    ${lon} + 0.005
    ${imovel}=    Create Dictionary    microareaId=${micro}    tipo=DOMICILIO    endereco=${endereco}    latitude=${ilat}    longitude=${ilon}
    ${i}=    POST Imovel    ${imovel}
    Should Be Equal As Integers    ${i.status_code}    201
    RETURN    ${area}    ${micro}    ${i.json()}[imovel][uuid]
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Vistoria Com Foco
    [Documentation]    An inspected property with one breeding site (with sample) in a type B deposit; returns the response.
    [Arguments]    ${imovel}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${matricula}=    Seed A Profissional Matricula
    ${foco}=    Create Dictionary    tipoDeposito=B    fase=LARVA    especieSuspeita=AEDES_AEGYPTI    amostraColetada=${True}
    ${focos}=    Create List    ${foco}
    ${payload}=    Create Dictionary    imovelId=${imovel}    agenteMatricula=${matricula}    tipo=ROTINA    resultado=INSPECIONADA    depositosB=${3}    depositosTratados=${1}    larvicida=Pyriproxyfen    larvicidaGramas=${1.5}    focos=${focos}
    ${r}=    POST Vistoria Vetorial    ${payload}
    RETURN    ${r}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Vistoria Fechada
    [Documentation]    A closed property; returns the response.
    [Arguments]    ${imovel}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${matricula}=    Seed A Profissional Matricula
    ${payload}=    Create Dictionary    imovelId=${imovel}    agenteMatricula=${matricula}    tipo=ROTINA    resultado=FECHADA
    ${r}=    POST Vistoria Vetorial    ${payload}
    RETURN    ${r}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Seed Caso De Dengue
    [Documentation]    Notifies dengue for a new patient; returns the notification.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${unidade}=    Seed A Unidade De Saude Uuid
    ${paciente}    ${cpf}    ${cns}    ${nome}=    Seed A Paciente
    ${dengue}=    Id Do Agravo    DENGUE
    ${respostas}=    Create Dictionary    febre=${True}
    ${payload}=    Create Dictionary    pacienteId=${paciente}    agravoId=${dengue}    unidadeId=${unidade}    origem=ATENDIMENTO    respostas=${respostas}
    ${n}=    POST Notificacao    ${payload}
    Should Be Equal As Integers    ${n.status_code}    201
    RETURN    ${n.json()}[uuid]
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Planejar Acao Vetorial
    [Documentation]    A planned cleaning effort in the micro-area; returns the response.
    [Arguments]    ${micro}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${hoje}=    Evaluate    datetime.date.today().isoformat()    modules=datetime
    ${micros}=    Create List    ${micro}
    ${payload}=    Create Dictionary    tipo=MUTIRAO_LIMPEZA    microareaIds=${micros}    dataPrevista=${hoje}    responsavel=Equipe de endemias
    ${r}=    POST Acao Vetorial    ${payload}
    RETURN    ${r}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Controle Vetorial - Imoveis - Status Code 200
    [Documentation]    The area properties without household data.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${area}    ${micro}    ${imovel}=    Seed Imovel Vetorial
    ${params}=    Create Dictionary    areaId=${area}
    ${r}=    GET Imoveis Vetoriais    ${params}
    Should Be Equal As Integers    ${r.status_code}    200
    Validate Object Schema    ${r.json()}[0]    ${IMOVEL_VETORIAL_SCHEMA}
    Should Contain    ${r.json()}[0][endereco]    Rua Robot Vetor
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Controle Vetorial - Imoveis Sem Area - Status Code 400
    [Documentation]    The area or the micro-area is required.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${params}=    Create Dictionary
    ${r}=    GET Imoveis Vetoriais    ${params}
    Should Be Equal As Integers    ${r.status_code}    400
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Controle Vetorial - Vistoria - Status Code 201
    [Documentation]    The inspection with a breeding site.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${area}    ${micro}    ${imovel}=    Seed Imovel Vetorial
    ${r}=    Vistoria Com Foco    ${imovel}
    Should Be Equal As Integers    ${r.status_code}    201
    Validate Object Schema    ${r.json()}    ${VISTORIA_VETORIAL_SCHEMA}
    Should Be Equal As Integers    ${r.json()}[depositosInspecionados]    3
    Should Be Equal    ${r.json()}[focos][0][situacao]    ATIVO
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Controle Vetorial - Fechada Com Deposito - Status Code 422
    [Documentation]    A closed property has no deposit.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${area}    ${micro}    ${imovel}=    Seed Imovel Vetorial
    ${matricula}=    Seed A Profissional Matricula
    ${payload}=    Create Dictionary    imovelId=${imovel}    agenteMatricula=${matricula}    tipo=ROTINA    resultado=FECHADA    depositosB=${2}
    ${r}=    POST Vistoria Vetorial    ${payload}
    Should Be Equal As Integers    ${r.status_code}    422
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Controle Vetorial - Foco Acima Dos Depositos - Status Code 422
    [Documentation]    Breeding sites of a type do not exceed its deposits.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${area}    ${micro}    ${imovel}=    Seed Imovel Vetorial
    ${matricula}=    Seed A Profissional Matricula
    ${foco}=    Create Dictionary    tipoDeposito=D1    fase=PUPA
    ${focos}=    Create List    ${foco}
    ${payload}=    Create Dictionary    imovelId=${imovel}    agenteMatricula=${matricula}    tipo=ROTINA    resultado=INSPECIONADA    depositosB=${2}    focos=${focos}
    ${r}=    POST Vistoria Vetorial    ${payload}
    Should Be Equal As Integers    ${r.status_code}    422
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Controle Vetorial - Pendencia - Status Code 200
    [Documentation]    A closed property becomes a pending revisit.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${area}    ${micro}    ${imovel}=    Seed Imovel Vetorial
    Vistoria Fechada    ${imovel}
    ${params}=    Create Dictionary    areaId=${area}
    ${r}=    GET Pendencias Vetoriais    ${params}
    Should Be Equal As Integers    ${r.status_code}    200
    Should Be Equal    ${r.json()}[0][uuid]    ${imovel}
    Should Be True    ${r.json()}[0][pendente]
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Controle Vetorial - Indices - Status Code 200
    [Documentation]    The area with one positive property has IIP 100 (risk).
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${area}    ${micro}    ${imovel}=    Seed Imovel Vetorial
    Vistoria Com Foco    ${imovel}
    ${params}=    Create Dictionary
    ${r}=    GET Indices Vetoriais    ${params}
    Should Be Equal As Integers    ${r.status_code}    200
    Validate Object Schema    ${r.json()}    ${INDICES_VETORIAIS_SCHEMA}
    ${linha}=    Evaluate    [a for a in $r.json()['areas'] if a['areaId'] == $area][0]
    Should Be Equal As Numbers    ${linha}[iip]    100.0
    Should Be Equal    ${linha}[classificacao]    RISCO
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Controle Vetorial - Laboratorio Negativo - Status Code 200
    [Documentation]    A negative lab result stops counting.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${area}    ${micro}    ${imovel}=    Seed Imovel Vetorial
    ${v}=    Vistoria Com Foco    ${imovel}
    ${payload}=    Create Dictionary    resultado=NEGATIVO
    ${r}=    POST Laboratorio Do Foco    ${v.json()}[focos][0][uuid]    ${payload}
    Should Be Equal As Integers    ${r.status_code}    200
    Should Not Be True    ${r.json()}[contaNosIndices]
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Controle Vetorial - Foco Eliminado - Status Code 200
    [Documentation]    The breeding site is removed; again, 422.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${area}    ${micro}    ${imovel}=    Seed Imovel Vetorial
    ${v}=    Vistoria Com Foco    ${imovel}
    ${payload}=    Create Dictionary    situacao=ELIMINADO    observacao=Recipiente descartado
    ${r}=    POST Situacao Do Foco    ${v.json()}[focos][0][uuid]    ${payload}
    Should Be Equal As Integers    ${r.status_code}    200
    Should Be Equal    ${r.json()}[situacao]    ELIMINADO
    ${again}=    POST Situacao Do Foco    ${v.json()}[focos][0][uuid]    ${payload}
    Should Be Equal As Integers    ${again.status_code}    422
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Controle Vetorial - Acao - Status Code 201
    [Documentation]    The control action is planned in the micro-area.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${area}    ${micro}    ${imovel}=    Seed Imovel Vetorial
    ${r}=    Planejar Acao Vetorial    ${micro}
    Should Be Equal As Integers    ${r.status_code}    201
    Should Be Equal    ${r.json()}[situacao]    PLANEJADA
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Controle Vetorial - Acao Realizada - Status Code 200
    [Documentation]    The action is registered as done.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${area}    ${micro}    ${imovel}=    Seed Imovel Vetorial
    ${a}=    Planejar Acao Vetorial    ${micro}
    ${payload}=    Create Dictionary    imoveisTrabalhados=${40}    resultado=Quarteirões 1 a 4
    ${r}=    POST Realizacao Da Acao Vetorial    ${a.json()}[uuid]    ${payload}
    Should Be Equal As Integers    ${r.status_code}    200
    Should Be Equal    ${r.json()}[situacao]    REALIZADA
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Controle Vetorial - Bloqueios Pendentes - Status Code 200
    [Documentation]    A dengue case without control action shows up.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${caso}=    Seed Caso De Dengue
    ${r}=    GET Bloqueios Pendentes
    Should Be Equal As Integers    ${r.status_code}    200
    ${ids}=    Evaluate    [b['notificacaoId'] for b in $r.json()]
    List Should Contain Value    ${ids}    ${caso}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Controle Vetorial - By HTTP Status Code
    [Documentation]    Executes the V4a scenario for the given HTTP status code.
    [Arguments]    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Run Keyword If    '${meuHTTP}' == '200-imoveis'    Controle Vetorial - Imoveis - Status Code 200
    ...    ELSE IF    '${meuHTTP}' == '400-imoveis-sem-area'    Controle Vetorial - Imoveis Sem Area - Status Code 400
    ...    ELSE IF    '${meuHTTP}' == '201-vistoria'    Controle Vetorial - Vistoria - Status Code 201
    ...    ELSE IF    '${meuHTTP}' == '422-fechada-com-deposito'    Controle Vetorial - Fechada Com Deposito - Status Code 422
    ...    ELSE IF    '${meuHTTP}' == '422-foco-acima-dos-depositos'    Controle Vetorial - Foco Acima Dos Depositos - Status Code 422
    ...    ELSE IF    '${meuHTTP}' == '200-pendencia'    Controle Vetorial - Pendencia - Status Code 200
    ...    ELSE IF    '${meuHTTP}' == '200-indices'    Controle Vetorial - Indices - Status Code 200
    ...    ELSE IF    '${meuHTTP}' == '200-laboratorio-negativo'    Controle Vetorial - Laboratorio Negativo - Status Code 200
    ...    ELSE IF    '${meuHTTP}' == '200-foco-eliminado'    Controle Vetorial - Foco Eliminado - Status Code 200
    ...    ELSE IF    '${meuHTTP}' == '201-acao'    Controle Vetorial - Acao - Status Code 201
    ...    ELSE IF    '${meuHTTP}' == '200-acao-realizada'    Controle Vetorial - Acao Realizada - Status Code 200
    ...    ELSE IF    '${meuHTTP}' == '200-bloqueios-pendentes'    Controle Vetorial - Bloqueios Pendentes - Status Code 200
    ...    ELSE    Fail    HTTP status code '${meuHTTP}' is not supported for Controle Vetorial test scenario.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
