*** Settings ***
Library     String
Library     Collections
Resource    ../../../pom/equipamentos/pontos_operacionais/pontos_operacionais_pom.resource
Resource    ../modelo_operacional/modelo_operacional_flow.resource
Resource    ../../atendimento/create_atendimento/create_atendimento_flow.resource
Resource    ../../../pom/atendimento/get_atendimento_by_id/get_atendimento_by_id_pom.resource
Resource    ../../common/schema_validation_flow.resource
Resource    ../../../resource/config/schema/schema.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
Seed Unidade Movel
    [Documentation]    Creates a unit with the mobile care profile and returns its uuid.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${unidade}=    Seed Unidade Do Tipo    POLICLINICA
    ${perfil}=    Incluir Perfil Por Codigo    ${unidade}    ATENCAO_MOVEL
    Should Be Equal As Integers    ${perfil.status_code}    200
    RETURN    ${unidade}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Incluir Ponto Do Tipo
    [Documentation]    Adds a point and returns the response.
    [Arguments]    ${unidade}    ${tipo}    ${nome}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${payload}=    Create Dictionary    tipo=${tipo}    nome=${nome}
    ${response}=    POST Incluir Ponto    ${unidade}    ${payload}
    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Id Do Ponto Novo
    [Documentation]    Adds a point and returns its uuid.
    [Arguments]    ${unidade}    ${tipo}    ${nome}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${response}=    Incluir Ponto Do Tipo    ${unidade}    ${tipo}    ${nome}
    Should Be Equal As Integers    ${response.status_code}    200
    ${ids}=    Evaluate    [p["uuid"] for p in $response.json()["pontos"] if p["nome"] == $nome]
    RETURN    ${ids}[0]
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Incluir Rota Com Pontos
    [Documentation]    Adds a route on Mondays and Fridays and returns the response.
    [Arguments]    ${unidade}    ${nome}    @{pontos}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${dias}=    Create List    MONDAY    FRIDAY
    ${ids}=    Create List    @{pontos}
    ${payload}=    Create Dictionary    nome=${nome}    dias=${dias}    horaInicio=08:00    horaFim=12:00    pontoIds=${ids}
    ${response}=    POST Incluir Rota    ${unidade}    ${payload}
    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Pontos - Unidade - Status Code 200
    [Documentation]    Any care unit has points; the end keeps the history.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${ubs}=    Seed Unidade Do Tipo    UBS
    ${response}=    Incluir Ponto Do Tipo    ${ubs}    PONTO_FIXO    Escola Rui Barbosa
    Should Be Equal As Integers    ${response.status_code}    200
    Validate Object Schema    ${response.json()}    ${PONTOS_DA_UNIDADE_SCHEMA}
    Should Be Equal    ${response.json()}[movel]    ${False}
    ${ponto}=    Set Variable    ${response.json()}[pontos][0][uuid]
    ${ajuste}=    Create Dictionary    nome=Escola Municipal Rui Barbosa    referencia=Ao lado da praça
    ${editado}=    PATCH Ponto    ${ubs}    ${ponto}    ${ajuste}
    Should Be Equal As Integers    ${editado.status_code}    200
    ${motivo}=    Create Dictionary    motivo=Fim da parceria
    ${encerrado}=    POST Encerrar Ponto    ${ubs}    ${ponto}    ${motivo}
    Should Be Equal As Integers    ${encerrado.status_code}    200
    Should Be Equal    ${encerrado.json()}[pontosEncerrados][0][motivoFim]    Fim da parceria
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Pontos - Nome Repetido - Status Code 409
    [Documentation]    The name is unique among the active points of the unit.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${ubs}=    Seed Unidade Do Tipo    UBS
    ${primeiro}=    Incluir Ponto Do Tipo    ${ubs}    PONTO_FIXO    Praça
    Should Be Equal As Integers    ${primeiro.status_code}    200
    ${repetido}=    Incluir Ponto Do Tipo    ${ubs}    PONTO_PROGRAMADO    praça
    Should Be Equal As Integers    ${repetido.status_code}    409
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Pontos - Base Sem Perfil - Status Code 422
    [Documentation]    Base and route need the mobile care profile.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${ubs}=    Seed Unidade Do Tipo    UBS
    ${base}=    Incluir Ponto Do Tipo    ${ubs}    BASE    Base
    Should Be Equal As Integers    ${base.status_code}    422
    ${ponto}=    Id Do Ponto Novo    ${ubs}    PONTO_FIXO    Praça
    ${rota}=    Incluir Rota Com Pontos    ${ubs}    Rota    ${ponto}
    Should Be Equal As Integers    ${rota.status_code}    422
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Pontos - Unidade Movel - Status Code 200
    [Documentation]    Base, route in order and the passages of the mobile unit.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${movel}=    Seed Unidade Movel
    ${base}=    Incluir Ponto Do Tipo    ${movel}    BASE    Base Norte
    Should Be Equal As Integers    ${base.status_code}    200
    Should Be Equal    ${base.json()}[movel]    ${True}
    ${segunda}=    Incluir Ponto Do Tipo    ${movel}    BASE    Base Sul
    Should Be Equal As Integers    ${segunda.status_code}    409
    ${a}=    Id Do Ponto Novo    ${movel}    PONTO_PROGRAMADO    Zebra
    ${b}=    Id Do Ponto Novo    ${movel}    PONTO_PROGRAMADO    Abacate
    ${rota}=    Incluir Rota Com Pontos    ${movel}    Rota rural    ${a}    ${b}
    Should Be Equal As Integers    ${rota.status_code}    200
    Should Be Equal    ${rota.json()}[rotas][0][pontos][0][nome]    Zebra
    ${posicao}=    Create Dictionary    pontoId=${a}
    ${passagem}=    POST Registrar Posicao    ${movel}    ${posicao}
    Should Be Equal As Integers    ${passagem.status_code}    200
    ${repetida}=    POST Registrar Posicao    ${movel}    ${posicao}
    Should Be Equal As Integers    ${repetida.status_code}    409
    ${motivo}=    Create Dictionary    motivo=Mudança do trajeto
    ${preso}=    POST Encerrar Ponto    ${movel}    ${a}    ${motivo}
    Should Be Equal As Integers    ${preso.status_code}    422
    Should Contain    ${preso.json()}[message]    Rota rural
    ${fim}=    POST Encerrar Rota    ${movel}    ${rota.json()}[rotas][0][uuid]    ${motivo}
    Should Be Equal As Integers    ${fim.status_code}    200
    ${livre}=    POST Encerrar Ponto    ${movel}    ${a}    ${motivo}
    Should Be Equal As Integers    ${livre.status_code}    200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Pontos - Ponto Repetido Na Rota - Status Code 409
    [Documentation]    The same point does not repeat on a route.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${movel}=    Seed Unidade Movel
    ${a}=    Id Do Ponto Novo    ${movel}    PONTO_PROGRAMADO    A
    ${rota}=    Incluir Rota Com Pontos    ${movel}    Rota    ${a}    ${a}
    Should Be Equal As Integers    ${rota.status_code}    409
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Pontos - Inexistente - Status Code 404
    [Documentation]    An unknown point is not found.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${ubs}=    Seed Unidade Do Tipo    UBS
    ${id}=    Evaluate    str(__import__("uuid").uuid4())
    ${ajuste}=    Create Dictionary    nome=Sem ponto
    ${response}=    PATCH Ponto    ${ubs}    ${id}    ${ajuste}
    Should Be Equal As Integers    ${response.status_code}    404
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Pontos - Local Do Atendimento - Status Code 201 E 400
    [Documentation]    The care setting of the encounter: description, point of the unit and validation.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${unidade}=    Seed A Unidade De Saude Uuid
    ${matricula}=    Seed A Profissional Matricula
    ${paciente}    ${cpf}    ${cartao}    ${nome}=    Seed A Paciente
    ${semDescricao}=    Build Atendimento Payload    ${paciente}    ${matricula}    ${unidade}
    Set To Dictionary    ${semDescricao}    localRealizacao=VIA_PUBLICA
    ${recusado}=    POST Create Atendimento    ${semDescricao}
    Should Be Equal As Integers    ${recusado.status_code}    400
    ${ponto}=    Id Do Ponto Novo    ${unidade}    PONTO_FIXO    Escola Rui Barbosa
    ${locais}=    GET Locais De Realizacao    ${unidade}    ${paciente}
    Should Be Equal As Integers    ${locais.status_code}    200
    Should Be Equal    ${locais.json()}[pontos][0][uuid]    ${ponto}
    ${payload}=    Build Atendimento Payload    ${paciente}    ${matricula}    ${unidade}
    Set To Dictionary    ${payload}    localRealizacao=PONTO_DA_UNIDADE    pontoOperacionalId=${ponto}
    ${response}=    POST Create Atendimento    ${payload}
    Should Be Equal As Integers    ${response.status_code}    201
    ${id}=    Buscar Atendimento Uuid Por Paciente    ${paciente}
    ${atendimento}=    GET Get Atendimento By Id    ${id}
    Should Be Equal    ${atendimento.json()}[pontoOperacionalNome]    Escola Rui Barbosa
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Pontos Operacionais - By HTTP Status Code
    [Documentation]    Dispatches the scenario by HTTP status code.
    [Arguments]    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Run Keyword If    '${meuHTTP}' == '200-unidade'    Pontos - Unidade - Status Code 200
    Run Keyword If    '${meuHTTP}' == '409-nome'    Pontos - Nome Repetido - Status Code 409
    Run Keyword If    '${meuHTTP}' == '422-perfil'    Pontos - Base Sem Perfil - Status Code 422
    Run Keyword If    '${meuHTTP}' == '200-movel'    Pontos - Unidade Movel - Status Code 200
    Run Keyword If    '${meuHTTP}' == '409-rota'    Pontos - Ponto Repetido Na Rota - Status Code 409
    Run Keyword If    '${meuHTTP}' == '404-ponto'    Pontos - Inexistente - Status Code 404
    Run Keyword If    '${meuHTTP}' == '201-local'    Pontos - Local Do Atendimento - Status Code 201 E 400
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

