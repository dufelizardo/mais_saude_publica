*** Settings ***
Library     String
Library     Collections
Resource    ../../pom/territorio/cadastro_territorial_pom.resource
Resource    ./territorio_flow.resource
Resource    ../paciente/create_paciente/create_paciente_flow.resource
Resource    ../common/schema_validation_flow.resource
Resource    ../../resource/config/schema/schema.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
Endereco Do Imovel
    [Documentation]    Returns an address payload.
    [Arguments]    ${logradouro}    ${numero}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${endereco}=    Create Dictionary    cep=13000-000    logradouro=${logradouro}    numeroLogradouro=${numero}    bairro=Centro    cidade=Campinas    estado=SP
    RETURN    ${endereco}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Seed Area Com Equipe E Microarea
    [Documentation]    Creates an area 0.02° wide with a team and the micro-area 07 drawn; returns the area and the micro-area.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${area}=    Seed Area Territorial
    ${geometria}=    Quadrado GeoJSON    -47.0    -22.0    0.01
    ${micro}=    Seed Microarea    ${area}    07    ${geometria}
    ${equipe}    ${nome}    ${unidadeId}=    Seed Uma Equipe
    ${resp}=    Create Dictionary    equipeId=${equipe}
    ${r}=    POST Responsabilidade Territorial    ${area}    ${resp}
    Should Be Equal As Integers    ${r.status_code}    200
    RETURN    ${area}    ${micro}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Seed Domicilio
    [Documentation]    Creates a home property with one household in the micro-area; returns the household and the property.
    [Arguments]    ${micro}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${sufixo}=    Generate Random String    5    [NUMBERS]
    ${endereco}=    Endereco Do Imovel    Rua Robot    ${sufixo}
    ${imovel}=    Create Dictionary    microareaId=${micro}    tipo=DOMICILIO    endereco=${endereco}    latitude=${-21.995}    longitude=${-46.995}
    ${i}=    POST Imovel    ${imovel}
    Should Be Equal As Integers    ${i.status_code}    201
    ${dom}=    Create Dictionary    imovelId=${i.json()}[imovel][uuid]    situacao=PROPRIO    comodos=${4}
    ${d}=    POST Domicilio    ${dom}
    Should Be Equal As Integers    ${d.status_code}    201
    RETURN    ${d.json()}[domicilios][0][uuid]    ${i.json()}[imovel][uuid]
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Cadastro - Imovel - Status Code 201
    [Documentation]    A property inside the micro-area, found by the point, with a valid schema.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${area}    ${micro}=    Seed Area Com Equipe E Microarea
    ${endereco}=    Endereco Do Imovel    Rua A    100
    ${payload}=    Create Dictionary    tipo=DOMICILIO    endereco=${endereco}    latitude=${-21.995}    longitude=${-46.995}
    ${response}=    POST Imovel    ${payload}
    Should Be Equal As Integers    ${response.status_code}    201
    Validate Object Schema    ${response.json()}    ${IMOVEL_DETALHE_SCHEMA}
    Should Be Equal    ${response.json()}[imovel][microareaCodigo]    07
    ${lista}=    GET Imoveis Da Area    ${area}
    Should Be Equal As Integers    ${lista.status_code}    200
    Length Should Be    ${lista.json()}    1
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Cadastro - Ponto Fora - Status Code 422
    [Documentation]    A point outside the chosen micro-area is refused.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${area}    ${micro}=    Seed Area Com Equipe E Microarea
    ${endereco}=    Endereco Do Imovel    Rua A    101
    ${payload}=    Create Dictionary    microareaId=${micro}    tipo=DOMICILIO    endereco=${endereco}    latitude=${-21.985}    longitude=${-46.985}
    ${response}=    POST Imovel    ${payload}
    Should Be Equal As Integers    ${response.status_code}    422
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Cadastro - Sem Equipe - Status Code 422
    [Documentation]    Without a responsible team the area has no household registry.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${area}=    Seed Area Territorial
    ${micro}=    Seed Microarea    ${area}    07
    ${endereco}=    Endereco Do Imovel    Rua A    102
    ${payload}=    Create Dictionary    microareaId=${micro}    tipo=DOMICILIO    endereco=${endereco}
    ${response}=    POST Imovel    ${payload}
    Should Be Equal As Integers    ${response.status_code}    422
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Cadastro - Domicilio Em Comercio - Status Code 422
    [Documentation]    A household only in a home property.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${area}    ${micro}=    Seed Area Com Equipe E Microarea
    ${endereco}=    Endereco Do Imovel    Rua A    103
    ${payload}=    Create Dictionary    microareaId=${micro}    tipo=COMERCIO    endereco=${endereco}
    ${i}=    POST Imovel    ${payload}
    ${dom}=    Create Dictionary    imovelId=${i.json()}[imovel][uuid]
    ${response}=    POST Domicilio    ${dom}
    Should Be Equal As Integers    ${response.status_code}    422
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Cadastro - Familia - Status Code 201
    [Documentation]    A family with the head and a child; the same person in another family is refused.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${area}    ${micro}=    Seed Area Com Equipe E Microarea
    ${domicilio}    ${imovel}=    Seed Domicilio    ${micro}
    ${outro}    ${outroImovel}=    Seed Domicilio    ${micro}
    ${ana}    ${c1}    ${s1}    ${n1}=    Seed A Paciente
    ${bia}    ${c2}    ${s2}    ${n2}=    Seed A Paciente
    ${filho}=    Create Dictionary    pacienteId=${bia}    parentesco=FILHO
    ${membros}=    Create List    ${filho}
    ${payload}=    Create Dictionary    domicilioId=${domicilio}    responsavelId=${ana}    rendaFamiliar=ATE_UM_SM    membros=${membros}
    ${response}=    POST Familia    ${payload}
    Should Be Equal As Integers    ${response.status_code}    201
    Length Should Be    ${response.json()}[domicilios][0][familias][0][membros]    2
    ${repetida}=    Create Dictionary    domicilioId=${outro}    responsavelId=${bia}
    ${r409}=    POST Familia    ${repetida}
    Should Be Equal As Integers    ${r409.status_code}    409
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Cadastro - Responsavel Sai Antes - Status Code 422
    [Documentation]    The head leaves last: before, swap the head.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${area}    ${micro}=    Seed Area Com Equipe E Microarea
    ${domicilio}    ${imovel}=    Seed Domicilio    ${micro}
    ${ana}    ${c1}    ${s1}    ${n1}=    Seed A Paciente
    ${bia}    ${c2}    ${s2}    ${n2}=    Seed A Paciente
    ${filho}=    Create Dictionary    pacienteId=${bia}    parentesco=FILHO
    ${membros}=    Create List    ${filho}
    ${payload}=    Create Dictionary    domicilioId=${domicilio}    responsavelId=${ana}    membros=${membros}
    ${f}=    POST Familia    ${payload}
    ${familia}=    Set Variable    ${f.json()}[domicilios][0][familias][0]
    ${daAna}=    Evaluate    [m["uuid"] for m in $familia["membros"] if m["pacienteId"] == $ana][0]
    ${daBia}=    Evaluate    [m["uuid"] for m in $familia["membros"] if m["pacienteId"] == $bia][0]
    ${saida}=    Create Dictionary    motivo=Mudou de cidade
    ${r422}=    POST Saida Membro Familia    ${daAna}    ${saida}
    Should Be Equal As Integers    ${r422.status_code}    422
    ${troca}=    Create Dictionary    membroId=${daBia}    parentescoDoAnterior=PAI_OU_MAE
    ${t}=    POST Responsavel Familia    ${familia}[uuid]    ${troca}
    Should Be Equal As Integers    ${t.status_code}    200
    ${response}=    POST Saida Membro Familia    ${daAna}    ${saida}
    Should Be Equal As Integers    ${response.status_code}    200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Cadastro - Mudanca - Status Code 200
    [Documentation]    The whole family moves; the patient keeps the history of homes.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${area}    ${micro}=    Seed Area Com Equipe E Microarea
    ${domicilio}    ${imovel}=    Seed Domicilio    ${micro}
    ${destino}    ${destinoImovel}=    Seed Domicilio    ${micro}
    ${ana}    ${c1}    ${s1}    ${n1}=    Seed A Paciente
    ${payload}=    Create Dictionary    domicilioId=${domicilio}    responsavelId=${ana}
    ${f}=    POST Familia    ${payload}
    ${mud}=    Create Dictionary    domicilioId=${destino}    motivo=Aluguel
    ${response}=    POST Mudanca Familia    ${f.json()}[domicilios][0][familias][0][uuid]    ${mud}
    Should Be Equal As Integers    ${response.status_code}    200
    Should Be Equal    ${response.json()}[imovel][uuid]    ${destinoImovel}
    ${daAna}=    GET Familia Do Paciente    ${ana}
    Should Be Equal As Integers    ${daAna.status_code}    200
    Length Should Be    ${daAna.json()}[moradias]    2
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Cadastro - Imovel - Status Code 404
    [Documentation]    Fetches a property that does not exist.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${uuid}=    Evaluate    str(uuid.uuid4())    modules=uuid
    ${response}=    GET Imovel    ${uuid}
    Should Be Equal As Integers    ${response.status_code}    404
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Cadastro Territorial - By HTTP Status Code
    [Documentation]    Executes the household registry scenario for the given HTTP status code.
    [Arguments]    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Run Keyword If    '${meuHTTP}' == '201-imovel'    Cadastro - Imovel - Status Code 201
    ...    ELSE IF    '${meuHTTP}' == '422-ponto'    Cadastro - Ponto Fora - Status Code 422
    ...    ELSE IF    '${meuHTTP}' == '422-equipe'    Cadastro - Sem Equipe - Status Code 422
    ...    ELSE IF    '${meuHTTP}' == '422-domicilio'    Cadastro - Domicilio Em Comercio - Status Code 422
    ...    ELSE IF    '${meuHTTP}' == '201-familia'    Cadastro - Familia - Status Code 201
    ...    ELSE IF    '${meuHTTP}' == '422-responsavel'    Cadastro - Responsavel Sai Antes - Status Code 422
    ...    ELSE IF    '${meuHTTP}' == '200-mudanca'    Cadastro - Mudanca - Status Code 200
    ...    ELSE IF    '${meuHTTP}' == '404'    Cadastro - Imovel - Status Code 404
    ...    ELSE    Fail    HTTP status code '${meuHTTP}' is not supported for Cadastro Territorial test scenario.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
