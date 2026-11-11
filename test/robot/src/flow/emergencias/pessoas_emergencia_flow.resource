*** Settings ***
Library     String
Library     Collections
Library     DateTime
Resource    ../../pom/emergencias/pessoas_emergencia_pom.resource
Resource    ../../pom/emergencias/desmobilizacao_pom.resource
Resource    ./emergencias_flow.resource
Resource    ../imunizacao/cobertura_vacinal_flow.resource
Resource    ../intersetorialidade/intersetorialidade_flow.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
    Vulneráveis da área afetada, pacientes deslocados e abrigos (ADR-0193). A área vem do Território: a microárea com a criança semeada.
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
Emergencia Na Microarea
    [Documentation]    A fresh emergency with the micro-area marked as affected; returns its uuid.
    [Arguments]    ${micro}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${em}=    Seed Emergencia
    ${id}=    Set Variable    ${em.json()}[uuid]
    ${ids}=    Create List    ${micro}
    ${p}=    Create Dictionary    microareaIds=${ids}
    ${r}=    PUT Pessoas Emergencia    emergencia/${id}/territorio    ${p}
    Should Be Equal As Integers    ${r.status_code}    200
    RETURN    ${id}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Pessoas - Vulneraveis Da Area
    [Documentation]    The child living in the affected micro-area comes with the marker and the agent; the CSV carries the name.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${dados}=    Seed Crianca Na Microarea
    ${em}=    Emergencia Na Microarea    ${dados}[micro]
    ${r}=    GET Pessoas Emergencia    emergencia/${em}/vulneraveis
    Should Be Equal As Integers    ${r.status_code}    200
    Should Be Equal As Integers    ${r.json()}[pessoasNaArea]    1
    ${p}=    Set Variable    ${r.json()}[lista][0]
    Should Be Equal    ${p}[pacienteId]    ${dados}[paciente]
    List Should Contain Value    ${p}[marcadores]    CRIANCA
    Should Not Be Equal    ${p}[acsNome]    ${None}
    ${filtro}=    GET Pessoas Emergencia    emergencia/${em}/vulneraveis?marcador=IDOSO
    Length Should Be    ${filtro.json()}[lista]    0
    ${csv}=    GET Pessoas Emergencia    emergencia/${em}/vulneraveis.csv
    Should Be Equal As Integers    ${csv.status_code}    200
    Should Contain    ${csv.text}    ${dados}[nome]
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Pessoas - Deslocado E Encaminhamento
    [Documentation]    The shelter needs the institution or the name (400); twice is 409; the reallocation keeps the path; the chart shows the
    ...    open displacement; the referral to the CRAS is unique (409); the return closes it.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${dados}=    Seed Crianca Na Microarea
    ${em}=    Emergencia Na Microarea    ${dados}[micro]
    ${abrigo}=    Seed Instituicao Externa    Abrigo Robot    ASSISTENCIA_SOCIAL    ACOLHIMENTO_INSTITUCIONAL
    ${cras}=    Seed Instituicao Externa
    ${sem}=    Create Dictionary    pacienteId=${dados}[paciente]    destinoTipo=ABRIGO
    ${r}=    POST Pessoas Emergencia    emergencia/${em}/deslocado    ${sem}
    Should Be Equal As Integers    ${r.status_code}    400
    ${com}=    Create Dictionary    pacienteId=${dados}[paciente]    destinoTipo=ABRIGO    destinoInstituicaoId=${abrigo}    necessidades=Fraldas
    ${r}=    POST Pessoas Emergencia    emergencia/${em}/deslocado    ${com}
    Should Be Equal As Integers    ${r.status_code}    201
    ${r2}=    POST Pessoas Emergencia    emergencia/${em}/deslocado    ${com}
    Should Be Equal As Integers    ${r2.status_code}    409
    ${d}=    GET Emergencia    ${em}
    Should Be Equal As Integers    ${d.json()}[deslocadosEmAberto]    1
    ${id}=    Set Variable    ${r.json()}[0][uuid]
    ${outro}=    Create Dictionary    destinoTipo=CASA_PARENTE    destinoDescricao=Rua da Avó, 12
    ${r}=    POST Pessoas Emergencia    emergencia/${em}/deslocado/${id}/realocacao    ${outro}
    Should Be Equal As Integers    ${r.status_code}    201
    ${novo}=    Evaluate    [x for x in $r.json() if x['situacao'] == 'DESLOCADO'][0]
    Should Be Equal    ${novo}[anteriorId]    ${id}
    ${pront}=    GET Pessoas Emergencia    emergencias/deslocamentos?pacienteId=${dados}[paciente]
    Should Be Equal    ${pront.json()}[0][destino]    Rua da Avó, 12
    ${prazo}=    Evaluate    (datetime.date.today() + datetime.timedelta(days=10)).isoformat()    modules=datetime
    ${enc}=    Create Dictionary    unidadeId=${dados}[unidade]    instituicaoId=${cras}    motivo=Benefício eventual    retornoEsperadoEm=${prazo}
    ${r}=    POST Pessoas Emergencia    emergencia/${em}/deslocado/${novo}[uuid]/encaminhamento    ${enc}
    Should Be Equal As Integers    ${r.status_code}    201
    ${r}=    POST Pessoas Emergencia    emergencia/${em}/deslocado/${novo}[uuid]/encaminhamento    ${enc}
    Should Be Equal As Integers    ${r.status_code}    409
    ${vazio}=    Create Dictionary
    ${r}=    POST Pessoas Emergencia    emergencia/${em}/deslocado/${novo}[uuid]/retorno    ${vazio}
    Should Be Equal As Integers    ${r.status_code}    200
    ${r}=    POST Pessoas Emergencia    emergencia/${em}/deslocado/${novo}[uuid]/retorno    ${vazio}
    Should Be Equal As Integers    ${r.status_code}    422
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Pessoas - By Cenario
    [Documentation]    Executes the people-in-emergencies scenario for the given case.
    [Arguments]    ${caso}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    IF    '${caso}' == 'VULNERAVEIS'
        Pessoas - Vulneraveis Da Area
    ELSE IF    '${caso}' == 'DESLOCADO'
        Pessoas - Deslocado E Encaminhamento
    ELSE
        Fail    Caso '${caso}' não suportado no cenário Pessoas na Emergência.
    END
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
