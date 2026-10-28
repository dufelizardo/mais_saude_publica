*** Settings ***
Library     String
Library     Collections
Library     DateTime
Resource    ../../pom/imunizacao/cobertura_vacinal_pom.resource
Resource    ../../pom/imunizacao/vacinacao_pom.resource
Resource    ../../pom/programas/campanha/campanha_pom.resource
Resource    vacinacao_flow.resource
Resource    ../territorio/cadastro_territorial_flow.resource
Resource    ../common/schema_validation_flow.resource
Resource    ../../resource/config/schema/schema.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
    Uma criança de 10 meses mora na microárea 07, com agente comunitário; a cobertura sai do cadastro (ADR-0149).
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
Seed Crianca
    [Documentation]    Creates a child patient born about ${meses} months ago; returns the uuid and the name.
    [Arguments]    ${meses}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${suffix}=    Generate Random String    10    [NUMBERS]
    ${nome}=    Set Variable    Robot Teste Crianca ${suffix}
    ${payload}=    Build Paciente Payload    ${nome}    ${suffix}    7${suffix}
    ${nascimento}=    Evaluate    (datetime.date.today() - datetime.timedelta(days=int(${meses} * 30.44) + 5)).isoformat()    modules=datetime
    Set To Dictionary    ${payload}    dataNascimento=${nascimento}
    ${r}=    POST Create Paciente    ${payload}
    Should Be Equal As Integers    ${r.status_code}    201
    ${uuid}=    Buscar Paciente Uuid Por Cpf    ${suffix}
    RETURN    ${uuid}    ${nome}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Seed Crianca Na Microarea
    [Documentation]    A team with the area, an agent in micro-area 07 and a child of 10 months living there; returns a dictionary.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${area}=    Seed Area Territorial
    ${geometria}=    Quadrado GeoJSON    -47.0    -22.0    0.01
    ${micro}=    Seed Microarea    ${area}    07    ${geometria}
    ${equipe}    ${nomeEquipe}    ${unidade}=    Seed Uma Equipe
    ${acs}=    Seed Membro Da Equipe    ${equipe}    ${unidade}    ACS
    ${resp}=    Create Dictionary    equipeId=${equipe}
    ${r}=    POST Responsabilidade Territorial    ${area}    ${resp}
    Should Be Equal As Integers    ${r.status_code}    200
    ${atrib}=    Create Dictionary    membroId=${acs}
    ${a}=    POST Atribuicao Microarea    ${micro}    ${atrib}
    Should Be Equal As Integers    ${a.status_code}    200
    ${domicilio}    ${imovel}=    Seed Domicilio    ${micro}
    ${paciente}    ${nome}=    Seed Crianca    10
    ${familia}=    Create Dictionary    domicilioId=${domicilio}    responsavelId=${paciente}
    ${f}=    POST Familia    ${familia}
    Should Be Equal As Integers    ${f.status_code}    201
    ${matricula}=    Seed A Profissional Matricula
    ${dados}=    Create Dictionary    unidade=${unidade}    equipe=${equipe}    micro=${micro}    paciente=${paciente}    nome=${nome}    matricula=${matricula}
    RETURN    ${dados}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Transcrever Dose
    [Documentation]    Registers a dose transcribed from the vaccination card, ${dias} days ago.
    [Arguments]    ${dados}    ${codigo}    ${dose}    ${dias}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${imuno}=    Id Do Imunobiologico    ${codigo}
    ${quando}=    Evaluate    (datetime.date.today() - datetime.timedelta(days=int(${dias}))).isoformat() + 'T09:00:00'    modules=datetime
    ${payload}=    Create Dictionary    pacienteId=${dados}[paciente]    imunobiologicoId=${imuno}    dose=${dose}    unidadeId=${dados}[unidade]
    ...    origem=TRANSCRITA    aplicadaEm=${quando}
    ${r}=    POST Registro Vacinacao    ${payload}
    Should Be Equal As Integers    ${r.status_code}    201
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Vacina Da Cobertura
    [Documentation]    Returns the vaccine line of the coverage by acronym and dose.
    [Arguments]    ${cobertura}    ${sigla}    ${dose}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${v}=    Evaluate    [x for x in $cobertura["vacinas"] if x["sigla"] == $sigla and x["dose"] == $dose]
    Length Should Be    ${v}    1
    RETURN    ${v}[0]
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Campanha De Vacinacao
    [Documentation]    Creates a running vaccination campaign of influenza with a goal of 50 people; returns its uuid.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${influenza}=    Id Do Imunobiologico    INFLUENZA
    ${inicio}=    Get Current Date    increment=-1 day    result_format=%Y-%m-%d
    ${fim}=    Get Current Date    increment=10 days    result_format=%Y-%m-%d
    ${sufixo}=    Generate Random String    6    [UPPER][NUMBERS]
    ${vacinas}=    Create List    ${influenza}
    ${payload}=    Create Dictionary    nome=Robot Gripe ${sufixo}    tipo=VACINACAO    inicio=${inicio}    fim=${fim}    metaQuantidade=${50}    imunobiologicoIds=${vacinas}
    ${c}=    POST Campanha    ${payload}
    Should Be Equal As Integers    ${c.status_code}    201
    RETURN    ${c.json()}[uuid]
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Cobertura Vacinal - Unidade - Status Code 200
    [Documentation]    The unit coverage by team, with the schema: the child with BCG reaches the goal.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${dados}=    Seed Crianca Na Microarea
    Transcrever Dose    ${dados}    BCG    UNICA    300
    ${params}=    Create Dictionary    unidadeId=${dados}[unidade]
    ${r}=    GET Cobertura Vacinal    ${params}
    Should Be Equal As Integers    ${r.status_code}    200
    Validate Object Schema    ${r.json()}    ${COBERTURA_VACINAL_SCHEMA}
    Validate Array Schema    ${r.json()}[vacinas]    ${COBERTURA_VACINA_SCHEMA}
    Should Be Equal    ${r.json()}[nivel]    UNIDADE
    ${bcg}=    Vacina Da Cobertura    ${r.json()}    BCG    UNICA
    Should Be Equal As Integers    ${bcg}[coorte]    1
    Should Be Equal As Integers    ${bcg}[vacinados]    1
    Should Be True    ${bcg}[atingida]
    ${penta}=    Vacina Da Cobertura    ${r.json()}    Penta    TERCEIRA
    Should Be Equal As Integers    ${penta}[vacinados]    0
    Should Be Equal    ${r.json()}[recortes][0][id]    ${dados}[equipe]
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Cobertura Vacinal - Equipe Por Microarea - Status Code 200
    [Documentation]    The team coverage is split by micro-area; the micro-area alone has no split.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${dados}=    Seed Crianca Na Microarea
    ${params}=    Create Dictionary    equipeId=${dados}[equipe]
    ${r}=    GET Cobertura Vacinal    ${params}
    Should Be Equal As Integers    ${r.status_code}    200
    Should Be Equal    ${r.json()}[recorteRotulo]    Microárea
    Should Be Equal    ${r.json()}[recortes][0][id]    ${dados}[micro]
    Should Be Equal    ${r.json()}[recortes][0][nome]    Microárea 07
    Set To Dictionary    ${params}    microareaId=${dados}[micro]
    ${m}=    GET Cobertura Vacinal    ${params}
    Should Be Equal    ${m.json()}[nivel]    MICROAREA
    Should Be Empty    ${m.json()}[recortes]
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Cobertura Vacinal - Abandono - Status Code 200
    [Documentation]    The child that took only the first penta dose counts in the dropout rate.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${dados}=    Seed Crianca Na Microarea
    Transcrever Dose    ${dados}    PENTA    PRIMEIRA    250
    ${params}=    Create Dictionary    unidadeId=${dados}[unidade]
    ${r}=    GET Cobertura Vacinal    ${params}
    ${penta}=    Vacina Da Cobertura    ${r.json()}    Penta    TERCEIRA
    Should Be Equal As Integers    ${penta}[comPrimeiraDose]    1
    Should Be Equal As Numbers    ${penta}[abandono]    100.0
    Should Be Equal    ${penta}[classeAbandono]    ALTA
    Should Be True    ${r.json()}[abandonoAlto] >= 1
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Cobertura Vacinal - Microarea Sem Equipe - Status Code 400
    [Documentation]    The micro-area needs the team.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${dados}=    Seed Crianca Na Microarea
    ${params}=    Create Dictionary    microareaId=${dados}[micro]
    ${r}=    GET Cobertura Vacinal    ${params}
    Should Be Equal As Integers    ${r.status_code}    400
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Cobertura Vacinal - Data Futura - Status Code 400
    [Documentation]    The reference date cannot be in the future.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${amanha}=    Get Current Date    increment=1 day    result_format=%Y-%m-%d
    ${params}=    Create Dictionary    data=${amanha}
    ${r}=    GET Cobertura Vacinal    ${params}
    Should Be Equal As Integers    ${r.status_code}    400
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Busca Ativa - Microarea Com ACS - Status Code 200
    [Documentation]    The overdue list of the micro-area comes with the agent and the address.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${dados}=    Seed Crianca Na Microarea
    ${params}=    Create Dictionary    unidadeId=${dados}[unidade]    equipeId=${dados}[equipe]    microareaId=${dados}[micro]
    ${r}=    GET Vacinacao Atrasados    ${params}
    Should Be Equal As Integers    ${r.status_code}    200
    ${linha}=    Evaluate    [a for a in $r.json() if a["pacienteId"] == $dados["paciente"]]
    Length Should Be    ${linha}    1
    Should Not Be Equal    ${linha}[0][acsNome]    ${None}
    Should Be Equal    ${linha}[0][microareaId]    ${dados}[micro]
    Should Contain    ${linha}[0][endereco]    Rua Robot
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Busca Ativa - Equipe De Outra Unidade - Status Code 400
    [Documentation]    A team of another unit is not a filter of the unit.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${dados}=    Seed Crianca Na Microarea
    ${outra}    ${nome}    ${unidadeOutra}=    Seed Uma Equipe
    ${params}=    Create Dictionary    unidadeId=${dados}[unidade]    equipeId=${outra}
    ${r}=    GET Vacinacao Atrasados    ${params}
    Should Be Equal As Integers    ${r.status_code}    400
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Campanha - Andamento Pela Dose - Status Code 201
    [Documentation]    The influenza dose with the campaign moves the goal.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${dados}=    Seed Crianca Na Microarea
    ${campanha}=    Campanha De Vacinacao
    ${payload}=    Dose Aplicada    ${dados}    INFLUENZA    ANUAL
    Set To Dictionary    ${payload}    campanhaId=${campanha}
    ${r}=    POST Registro Vacinacao    ${payload}
    Should Be Equal As Integers    ${r.status_code}    201
    Should Be Equal    ${r.json()}[estrategia]    CAMPANHA
    ${c}=    GET Campanha    ${campanha}
    Should Be Equal As Integers    ${c.json()}[realizado]    1
    Should Be Equal    ${c.json()}[imunobiologicoSiglas][0]    Influenza
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Campanha - Outra Vacina - Status Code 422
    [Documentation]    The dose of a vaccine outside the campaign does not enter it.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${dados}=    Seed Crianca Na Microarea
    ${campanha}=    Campanha De Vacinacao
    ${payload}=    Dose Aplicada    ${dados}    PENTA    PRIMEIRA
    Set To Dictionary    ${payload}    campanhaId=${campanha}
    ${r}=    POST Registro Vacinacao    ${payload}
    Should Be Equal As Integers    ${r.status_code}    422
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Sala De Situacao - Vacinacao - Status Code 200
    [Documentation]    The vaccination block of the unit, with the schema.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${dados}=    Seed Crianca Na Microarea
    ${payload}=    Dose Aplicada    ${dados}    INFLUENZA    ANUAL
    ${d}=    POST Registro Vacinacao    ${payload}
    Should Be Equal As Integers    ${d.status_code}    201
    ${params}=    Create Dictionary    unidadeId=${dados}[unidade]
    ${r}=    GET Vacinacao Na Rede    ${params}
    Should Be Equal As Integers    ${r.status_code}    200
    Validate Object Schema    ${r.json()}    ${VACINACAO_NA_REDE_SCHEMA}
    Should Be Equal As Integers    ${r.json()}[dosesAplicadas]    1
    Should Be Equal    ${r.json()}[porEstrategia][0][chave]    ROTINA
    ${penta}=    Evaluate    [x for x in $r.json()["cobertura"] if x["sigla"] == "Penta" and x["dose"] == "TERCEIRA"]
    Should Be Equal As Integers    ${penta}[0][coorte]    1
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Calendario - Meta De Cobertura Sem Valor - Status Code 400
    [Documentation]    The dose that measures the coverage needs the goal.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${bcg}=    Id Do Imunobiologico    BCG
    ${hoje}=    Get Current Date    result_format=%Y-%m-%d
    ${payload}=    Create Dictionary    imunobiologicoId=${bcg}    dose=ADICIONAL    publico=CRIANCA    idadeRecomendadaMeses=${12}
    ...    contaNaSituacao=${False}    vigenteDesde=${hoje}    medeCobertura=${True}
    ${r}=    POST Esquema Vacinal    ${payload}
    Should Be Equal As Integers    ${r.status_code}    400
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Cobertura Vacinal - By HTTP Status Code
    [Documentation]    Executes the I3 scenario for the given HTTP status code.
    [Arguments]    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Run Keyword If    '${meuHTTP}' == '200-unidade'    Cobertura Vacinal - Unidade - Status Code 200
    ...    ELSE IF    '${meuHTTP}' == '200-microarea'    Cobertura Vacinal - Equipe Por Microarea - Status Code 200
    ...    ELSE IF    '${meuHTTP}' == '200-abandono'    Cobertura Vacinal - Abandono - Status Code 200
    ...    ELSE IF    '${meuHTTP}' == '400-microarea'    Cobertura Vacinal - Microarea Sem Equipe - Status Code 400
    ...    ELSE IF    '${meuHTTP}' == '400-futuro'    Cobertura Vacinal - Data Futura - Status Code 400
    ...    ELSE IF    '${meuHTTP}' == '200-busca-ativa'    Busca Ativa - Microarea Com ACS - Status Code 200
    ...    ELSE IF    '${meuHTTP}' == '400-equipe'    Busca Ativa - Equipe De Outra Unidade - Status Code 400
    ...    ELSE IF    '${meuHTTP}' == '201-campanha'    Campanha - Andamento Pela Dose - Status Code 201
    ...    ELSE IF    '${meuHTTP}' == '422-campanha'    Campanha - Outra Vacina - Status Code 422
    ...    ELSE IF    '${meuHTTP}' == '200-sala-situacao'    Sala De Situacao - Vacinacao - Status Code 200
    ...    ELSE IF    '${meuHTTP}' == '400-meta'    Calendario - Meta De Cobertura Sem Valor - Status Code 400
    ...    ELSE    Fail    HTTP status code '${meuHTTP}' is not supported for Cobertura Vacinal test scenario.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
