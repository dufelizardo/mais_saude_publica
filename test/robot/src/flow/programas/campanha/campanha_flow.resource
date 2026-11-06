*** Settings ***
Library     String
Library     Collections
Resource    ../../../pom/programas/campanha/campanha_pom.resource
Resource    ../acao/acao_programatica_flow.resource
Resource    ../../setor/create_setor/create_setor_flow.resource
Resource    ../../common/schema_validation_flow.resource
Resource    ../../../resource/config/schema/schema.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
Seed Campanha Com Local
    [Documentation]    Creates a running campaign (vaccination by default) with a network unit as place; returns the campaign, its name, the place and the unit.
    [Arguments]    ${tipo}=VACINACAO
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${hoje}=    Evaluate    datetime.date.today().isoformat()    modules=datetime
    ${fim}=    Evaluate    (datetime.date.today() + datetime.timedelta(days=30)).isoformat()    modules=datetime
    ${sufixo}=    Generate Random String    6    [UPPER]
    ${nome}=    Set Variable    Campanha Robot ${sufixo}
    ${payload}=    Create Dictionary    nome=${nome}    tipo=${tipo}    inicio=${hoje}    fim=${fim}    idadeMinima=${60}    metaQuantidade=${100}
    ${c}=    POST Campanha    ${payload}
    Should Be Equal As Integers    ${c.status_code}    201
    ${unidade}=    Seed A Unidade De Saude Uuid
    ${local}=    Create Dictionary    unidadeId=${unidade}    funcionamento=seg a sex, 8h às 17h
    ${l}=    POST Local Campanha    ${c.json()}[uuid]    ${local}
    Should Be Equal As Integers    ${l.status_code}    201
    RETURN    ${c.json()}[uuid]    ${nome}    ${l.json()}[locais][0][uuid]    ${unidade}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Campanha - Cadastro - Status Code 201
    [Documentation]    Creates a running campaign; the audience comes from the fields.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${hoje}=    Evaluate    datetime.date.today().isoformat()    modules=datetime
    ${fim}=    Evaluate    (datetime.date.today() + datetime.timedelta(days=30)).isoformat()    modules=datetime
    ${payload}=    Create Dictionary    nome=Campanha Robot Gripe    tipo=VACINACAO    inicio=${hoje}    fim=${fim}    idadeMinima=${60}
    ${r}=    POST Campanha    ${payload}
    Should Be Equal As Integers    ${r.status_code}    201
    Validate Object Schema    ${r.json()}    ${CAMPANHA_SCHEMA}
    Should Be Equal    ${r.json()}[situacao]    EM_ANDAMENTO
    Should Be Equal    ${r.json()}[publico]    60 anos ou mais
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Campanha - Fim Antes Do Inicio - Status Code 400
    [Documentation]    The end cannot be before the start.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${hoje}=    Evaluate    datetime.date.today().isoformat()    modules=datetime
    ${fim}=    Evaluate    (datetime.date.today() + datetime.timedelta(days=30)).isoformat()    modules=datetime
    ${payload}=    Create Dictionary    nome=Campanha Robot Errada    tipo=MUTIRAO    inicio=${fim}    fim=${hoje}
    ${r}=    POST Campanha    ${payload}
    Should Be Equal As Integers    ${r.status_code}    400
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Campanha - Publicacao - Status Code 200
    [Documentation]    Only a published campaign appears in the public query.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${campanha}    ${nome}    ${local}    ${unidade}=    Seed Campanha Com Local
    ${antes}=    GET Campanhas Publicas
    ${nomes}=    Evaluate    [c["nome"] for c in $antes.json()]
    Should Not Contain    ${nomes}    ${nome}
    ${payload}=    Create Dictionary    publicada=${True}
    ${r}=    POST Publicacao Campanha    ${campanha}    ${payload}
    Should Be Equal As Integers    ${r.status_code}    200
    ${depois}=    GET Campanhas Publicas
    Should Be Equal As Integers    ${depois.status_code}    200
    ${publica}=    Evaluate    [c for c in $depois.json() if c["nome"] == $nome][0]
    Validate Object Schema    ${publica}    ${CAMPANHA_PUBLICA_SCHEMA}
    Length Should Be    ${publica}[locais]    1
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Campanha - Local Repetido - Status Code 409
    [Documentation]    The same network unit twice is refused.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${campanha}    ${nome}    ${local}    ${unidade}=    Seed Campanha Com Local
    ${payload}=    Create Dictionary    unidadeId=${unidade}
    ${r}=    POST Local Campanha    ${campanha}    ${payload}
    Should Be Equal As Integers    ${r.status_code}    409
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Campanha - Local Temporario Sem Nome - Status Code 400
    [Documentation]    A temporary place needs the name.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${campanha}    ${nome}    ${local}    ${unidade}=    Seed Campanha Com Local
    ${payload}=    Create Dictionary    unidadeId=${unidade}    temporario=${True}
    ${r}=    POST Local Campanha    ${campanha}    ${payload}
    Should Be Equal As Integers    ${r.status_code}    400
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Campanha - Acao E Andamento - Status Code 201
    [Documentation]    A campaign action at a place; done with the people served, it moves the goal (the vaccination one counts the doses, ADR-0149).
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${campanha}    ${nome}    ${local}    ${unidade}=    Seed Campanha Com Local    RASTREAMENTO
    ${hoje}=    Evaluate    datetime.date.today().isoformat()    modules=datetime
    ${payload}=    Create Dictionary    campanhaId=${campanha}    localCampanhaId=${local}    tipo=AFERICAO    dataPrevista=${hoje}
    ${a}=    POST Acao Programatica    ${payload}
    Should Be Equal As Integers    ${a.status_code}    201
    Should Be Equal    ${a.json()}[campanhaNome]    ${nome}
    ${feito}=    Create Dictionary    quantidadeAtendida=${25}
    ${r}=    POST Realizacao Acao    ${a.json()}[uuid]    ${feito}
    Should Be Equal As Integers    ${r.status_code}    200
    ${c}=    GET Campanha    ${campanha}
    Should Be Equal As Integers    ${c.json()}[realizado]    25
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Campanha - Acao Fora Do Periodo - Status Code 422
    [Documentation]    The action date must be within the campaign.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${campanha}    ${nome}    ${local}    ${unidade}=    Seed Campanha Com Local
    ${depois}=    Evaluate    (datetime.date.today() + datetime.timedelta(days=60)).isoformat()    modules=datetime
    ${payload}=    Create Dictionary    campanhaId=${campanha}    localCampanhaId=${local}    tipo=VACINA    dataPrevista=${depois}
    ${a}=    POST Acao Programatica    ${payload}
    Should Be Equal As Integers    ${a.status_code}    422
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Campanha - Remover Local Com Acao - Status Code 409
    [Documentation]    A place with actions cannot be removed.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${campanha}    ${nome}    ${local}    ${unidade}=    Seed Campanha Com Local
    ${hoje}=    Evaluate    datetime.date.today().isoformat()    modules=datetime
    ${payload}=    Create Dictionary    campanhaId=${campanha}    localCampanhaId=${local}    tipo=MOBILIZACAO    dataPrevista=${hoje}
    POST Acao Programatica    ${payload}
    ${r}=    DELETE Local Campanha    ${local}
    Should Be Equal As Integers    ${r.status_code}    409
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Campanha - Cancelamento - Status Code 200
    [Documentation]    Cancelling needs the reason; then it cannot be published.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${campanha}    ${nome}    ${local}    ${unidade}=    Seed Campanha Com Local
    ${vazio}=    Create Dictionary
    ${r1}=    POST Cancelamento Campanha    ${campanha}    ${vazio}
    Should Be Equal As Integers    ${r1.status_code}    400
    ${motivo}=    Create Dictionary    motivo=Falta de insumos
    ${r2}=    POST Cancelamento Campanha    ${campanha}    ${motivo}
    Should Be Equal As Integers    ${r2.status_code}    200
    Should Be Equal    ${r2.json()}[situacao]    CANCELADA
    ${publicar}=    Create Dictionary    publicada=${True}
    ${r3}=    POST Publicacao Campanha    ${campanha}    ${publicar}
    Should Be Equal As Integers    ${r3.status_code}    422
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Campanhas - Lista - Status Code 200
    [Documentation]    The list filtered by situation.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${campanha}    ${nome}    ${local}    ${unidade}=    Seed Campanha Com Local
    ${params}=    Create Dictionary    situacao=EM_ANDAMENTO
    ${r}=    GET Campanhas    ${params}
    Should Be Equal As Integers    ${r.status_code}    200
    ${nomes}=    Evaluate    [c["nome"] for c in $r.json()]
    Should Contain    ${nomes}    ${nome}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Campanha - Inexistente - Status Code 404
    [Documentation]    Reading a campaign that does not exist.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${uuid}=    Evaluate    str(uuid.uuid4())    modules=uuid
    ${r}=    GET Campanha    ${uuid}
    Should Be Equal As Integers    ${r.status_code}    404
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Campanha - By HTTP Status Code
    [Documentation]    Executes the P3 scenario for the given HTTP status code.
    [Arguments]    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Run Keyword If    '${meuHTTP}' == '201-campanha'    Campanha - Cadastro - Status Code 201
    ...    ELSE IF    '${meuHTTP}' == '400-periodo'    Campanha - Fim Antes Do Inicio - Status Code 400
    ...    ELSE IF    '${meuHTTP}' == '200-publicacao'    Campanha - Publicacao - Status Code 200
    ...    ELSE IF    '${meuHTTP}' == '409-local'    Campanha - Local Repetido - Status Code 409
    ...    ELSE IF    '${meuHTTP}' == '400-local-temporario'    Campanha - Local Temporario Sem Nome - Status Code 400
    ...    ELSE IF    '${meuHTTP}' == '201-acao'    Campanha - Acao E Andamento - Status Code 201
    ...    ELSE IF    '${meuHTTP}' == '422-acao-periodo'    Campanha - Acao Fora Do Periodo - Status Code 422
    ...    ELSE IF    '${meuHTTP}' == '409-remover-local'    Campanha - Remover Local Com Acao - Status Code 409
    ...    ELSE IF    '${meuHTTP}' == '200-cancelamento'    Campanha - Cancelamento - Status Code 200
    ...    ELSE IF    '${meuHTTP}' == '200-lista'    Campanhas - Lista - Status Code 200
    ...    ELSE IF    '${meuHTTP}' == '404-campanha'    Campanha - Inexistente - Status Code 404
    ...    ELSE    Fail    HTTP status code '${meuHTTP}' is not supported for Campanha test scenario.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
