*** Settings ***
Library     String
Library     Collections
Library     DateTime
Resource    ../../pom/campanha_comunicacao/campanha_comunicacao_pom.resource
Resource    ../comunicacao/comunicacao_flow.resource
Resource    ../common/schema_validation_flow.resource
Resource    ../../resource/config/schema/schema.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
Payload Da Campanha
    [Documentation]    Builds a campaign for the next month.
    [Arguments]    ${nome}    ${tipoCanal}=PORTAL
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${hoje}=    Get Current Date    result_format=%Y-%m-%d
    ${fim}=    Add Time To Date    ${hoje}    30 days    result_format=%Y-%m-%d
    ${canais}=    Create List
    IF    '${tipoCanal}' != '${EMPTY}'
        ${canal}=    Id Do Canal    ${tipoCanal}
        Append To List    ${canais}    ${canal}
    END
    ${temas}=    Create List    Câncer de mama
    ${conteudos}=    Create List
    ${payload}=    Create Dictionary    nome=${nome}    objetivo=Estimular a mamografia    inicio=${hoje}    fim=${fim}    publicoAlvo=POPULACAO_GERAL    temas=${temas}    canalIds=${canais}    conteudoGrupoIds=${conteudos}
    RETURN    ${payload}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Campanhas - Criar - Status Code 201
    [Documentation]    A new campaign is planned.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${sufixo}=    Generate Random String    6    [LETTERS][NUMBERS]
    ${payload}=    Payload Da Campanha    Robot Outubro Rosa ${sufixo}
    ${response}=    POST Campanha Comunicacao    ${payload}
    Should Be Equal As Integers    ${response.status_code}    201
    Validate Object Schema    ${response.json()}    ${CAMPANHA_COMUNICACAO_SCHEMA}
    Should Be Equal    ${response.json()}[situacao]    PLANEJADA
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Campanhas - Ciclo E Portal - Status Code 200
    [Documentation]    Starts the campaign, sees it on the portal and closes it with the summary.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${sufixo}=    Generate Random String    6    [LETTERS][NUMBERS]
    ${nome}=    Set Variable    Robot Campanha ${sufixo}
    ${payload}=    Payload Da Campanha    ${nome}
    ${criada}=    POST Campanha Comunicacao    ${payload}
    ${uuid}=    Set Variable    ${criada.json()}[uuid]
    ${inicio}=    POST Acao Da Campanha    ${uuid}    inicio
    Should Be Equal As Integers    ${inicio.status_code}    200
    Should Be Equal    ${inicio.json()}[situacao]    EM_ANDAMENTO
    ${portal}=    GET Campanhas Publicas De Comunicacao
    ${nomes}=    Evaluate    [c["nome"] for c in $portal.json()]
    Should Contain    ${nomes}    ${nome}
    ${balanco}=    Create Dictionary    balanco=Alcançou as UBS
    ${fim}=    POST Acao Da Campanha    ${uuid}    encerramento    ${balanco}
    Should Be Equal As Integers    ${fim.status_code}    200
    Should Be Equal    ${fim.json()}[situacao]    ENCERRADA
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Campanhas - Sem Canal - Status Code 422
    [Documentation]    Without a channel, it does not start.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${sufixo}=    Generate Random String    6    [LETTERS][NUMBERS]
    ${payload}=    Payload Da Campanha    Robot Sem Canal ${sufixo}    ${EMPTY}
    ${criada}=    POST Campanha Comunicacao    ${payload}
    ${response}=    POST Acao Da Campanha    ${criada.json()}[uuid]    inicio
    Should Be Equal As Integers    ${response.status_code}    422
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Campanhas - Comunicado Fora Do Periodo - Status Code 422
    [Documentation]    The notice period must fit in the campaign.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${sufixo}=    Generate Random String    6    [LETTERS][NUMBERS]
    ${payload}=    Payload Da Campanha    Robot Periodo ${sufixo}
    ${criada}=    POST Campanha Comunicacao    ${payload}
    ${comunicado}=    Payload Do Comunicado    Robot Aviso ${sufixo}
    ${hoje}=    Get Current Date    result_format=%Y-%m-%d
    ${longe}=    Add Time To Date    ${hoje}    60 days    result_format=%Y-%m-%d
    Set To Dictionary    ${comunicado}    fim=${longe}    campanhaId=${criada.json()}[uuid]
    ${response}=    POST Create Comunicado    ${comunicado}
    Should Be Equal As Integers    ${response.status_code}    422
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Campanhas - Material Sem Https - Status Code 400
    [Documentation]    The material link must be https.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${conteudo}    ${titulo}=    Seed Conteudo Aprovado
    ${lista}=    GET Conteudos Da Comunicacao
    ${grupo}=    Evaluate    [c["grupoId"] for c in $lista.json() if c["uuid"] == $conteudo][0]
    ${material}=    Create Dictionary    conteudoGrupoId=${grupo}    formato=VIDEO    titulo=Robot Vídeo    link=http://inseguro
    ${response}=    POST Material Educativo    ${material}
    Should Be Equal As Integers    ${response.status_code}    400
    Set To Dictionary    ${material}    link=https://youtu.be/robot
    ${criado}=    POST Material Educativo    ${material}
    Should Be Equal As Integers    ${criado.status_code}    201
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Campanhas - By HTTP Status Code
    [Documentation]    Dispatches the scenario by HTTP status code.
    [Arguments]    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Run Keyword If    '${meuHTTP}' == '201-criar'    Campanhas - Criar - Status Code 201
    Run Keyword If    '${meuHTTP}' == '200-ciclo'    Campanhas - Ciclo E Portal - Status Code 200
    Run Keyword If    '${meuHTTP}' == '422-canal'    Campanhas - Sem Canal - Status Code 422
    Run Keyword If    '${meuHTTP}' == '422-periodo'    Campanhas - Comunicado Fora Do Periodo - Status Code 422
    Run Keyword If    '${meuHTTP}' == '400-material'    Campanhas - Material Sem Https - Status Code 400
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

