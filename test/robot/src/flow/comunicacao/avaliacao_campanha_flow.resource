*** Settings ***
Library     String
Library     Collections
Library     DateTime
Resource    ./educacao_em_saude_flow.resource
Resource    ../campanha_comunicacao/campanha_comunicacao_flow.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
    Avaliação da comunicação e da educação (ADR-0196): metas, apuração, avaliação na encerrada e os relatórios do #19.
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
Seed Campanha Com Metas
    [Documentation]    A started campaign with goals of people and actions; returns the uuid and the name.
    [Arguments]    ${metaPessoas}=30    ${metaAcoes}=1
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${sufixo}=    Generate Random String    6    [LETTERS][NUMBERS]
    ${nome}=    Set Variable    Robot Avaliacao ${sufixo}
    ${payload}=    Payload Da Campanha    ${nome}
    Set To Dictionary    ${payload}    metaPessoas=${${metaPessoas}}    metaAcoes=${${metaAcoes}}
    ${r}=    POST Campanha Comunicacao    ${payload}
    Should Be Equal As Integers    ${r.status_code}    201
    ${uuid}=    Set Variable    ${r.json()}[uuid]
    ${inicio}=    POST Acao Da Campanha    ${uuid}    inicio
    Should Be Equal As Integers    ${inicio.status_code}    200
    RETURN    ${uuid}    ${nome}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Avaliacao - Metas Apuracao E Registro
    [Documentation]    Goal zero, 400; the linked action counts as planned; evaluating the running campaign, 422; closed, without conclusions 400, then 200; the report lists it.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${sufixo}=    Generate Random String    6    [LETTERS][NUMBERS]
    ${invalida}=    Payload Da Campanha    Robot Meta Zero ${sufixo}
    Set To Dictionary    ${invalida}    metaPessoas=${0}
    ${r}=    POST Campanha Comunicacao    ${invalida}
    Should Be Equal As Integers    ${r.status_code}    400
    ${campanha}    ${nome}=    Seed Campanha Com Metas
    ${acao}    ${paciente}    ${unidade}    ${nomePaciente}=    Seed Acao Bucal Coletiva    SAUDE_DA_FAMILIA    ${False}    Escola Robot
    ${educacao}=    Create Dictionary    tema=Dengue    campanhaComunicacaoId=${campanha}
    ${r}=    PUT Educacao Da Acao    ${acao}    ${educacao}
    Should Be Equal As Integers    ${r.status_code}    200
    ${r}=    GET Educacao Generico    comunicacao/campanha/${campanha}/avaliacao
    Should Be Equal As Integers    ${r.status_code}    200
    Should Be Equal As Integers    ${r.json()}[acoes][previstas]    1
    Should Be Equal    ${r.json()}[metaPessoas][situacao]    EM_ANDAMENTO
    ${avaliacao}=    Create Dictionary    publicoAtingido=PARCIAL    conclusoes=Faltou alcançar as escolas.
    ${r}=    POST Educacao Generico    comunicacao/campanha/${campanha}/avaliacao    ${avaliacao}
    Should Be Equal As Integers    ${r.status_code}    422
    ${balanco}=    Create Dictionary    balanco=Encerrada pelo Robot
    ${r}=    POST Acao Da Campanha    ${campanha}    encerramento    ${balanco}
    Should Be Equal As Integers    ${r.status_code}    200
    ${semConclusao}=    Create Dictionary    publicoAtingido=SIM
    ${r}=    POST Educacao Generico    comunicacao/campanha/${campanha}/avaliacao    ${semConclusao}
    Should Be Equal As Integers    ${r.status_code}    400
    ${r}=    POST Educacao Generico    comunicacao/campanha/${campanha}/avaliacao    ${avaliacao}
    Should Be Equal As Integers    ${r.status_code}    200
    Should Be Equal    ${r.json()}[avaliacao][publicoAtingido]    PARCIAL
    Should Be Equal    ${r.json()}[avaliacao][situacaoMetaPessoas]    ABAIXO
    ${rel}=    GET Educacao Generico    relatorios/CAMPANHAS_COMUNICACAO
    Should Be Equal As Integers    ${rel.status_code}    200
    ${nomes}=    Evaluate    [l[0] for l in $rel.json()['secoes'][0]['linhas']]
    Should Contain    ${nomes}    ${nome}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Avaliacao - By Cenario
    [Documentation]    Executes the campaign evaluation scenario for the given case.
    [Arguments]    ${caso}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    IF    '${caso}' == 'REGISTRO'
        Avaliacao - Metas Apuracao E Registro
    ELSE
        Fail    Caso '${caso}' não suportado no cenário Avaliação da Campanha.
    END
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
