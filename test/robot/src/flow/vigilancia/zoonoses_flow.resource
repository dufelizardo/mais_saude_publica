*** Settings ***
Library     String
Library     Collections
Resource    ../../pom/vigilancia/zoonoses_pom.resource
Resource    ./controle_vetorial_flow.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
    Animais das zoonoses (ADR-0163).
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
Seed Cao Zoonoses
    [Documentation]    A dog with an address; returns the uuid.
    [Arguments]    ${especie}=CAO
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${sufixo}=    Generate Random String    6    [NUMBERS]
    ${payload}=    Create Dictionary    especie=${especie}    nome=Robot Rex ${sufixo}    tutorNome=Dona Rita    tutorTelefone=(19) 99999-0000    endereco=Rua Robot ${sufixo}
    ${r}=    POST Animal Zoonoses    ${payload}
    Should Be Equal As Integers    ${r.status_code}    201
    RETURN    ${r.json()}[uuid]
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Seed Atendimento Antirrabico
    [Documentation]    Notifies the human rabies care for a new patient; returns the notification.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${unidade}=    Seed A Unidade De Saude Uuid
    ${paciente}    ${cpf}    ${cns}    ${nome}=    Seed A Paciente
    ${agravo}=    Id Do Agravo    ATENDIMENTO_ANTIRRABICO
    ${payload}=    Create Dictionary    pacienteId=${paciente}    agravoId=${agravo}    unidadeId=${unidade}    origem=ATENDIMENTO
    ${n}=    POST Notificacao    ${payload}
    Should Be Equal As Integers    ${n.status_code}    201
    RETURN    ${n.json()}[uuid]
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Observar Cao
    [Documentation]    Opens the observation of the dog bitten days ago; returns the response.
    [Arguments]    ${animal}    ${dias}=2    ${notificacao}=${EMPTY}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${data}=    Evaluate    (datetime.date.today() - datetime.timedelta(days=${dias})).isoformat()    modules=datetime
    ${payload}=    Create Dictionary    animalId=${animal}    dataAgressao=${data}    inicio=${data}
    IF    '${notificacao}' != '${EMPTY}'
        Set To Dictionary    ${payload}    notificacaoId=${notificacao}
    END
    ${r}=    POST Observacao Animal    ${payload}
    RETURN    ${r}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Zoonoses - Animal - Status Code 201
    [Documentation]    The animal is registered with an address.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${id}=    Seed Cao Zoonoses
    ${params}=    Create Dictionary    busca=Robot Rex
    ${r}=    GET Animais Zoonoses    ${params}
    ${ids}=    Evaluate    [a['uuid'] for a in $r.json()]
    List Should Contain Value    ${ids}    ${id}
    Validate Object Schema    ${r.json()}[0]    ${ANIMAL_ZOONOSES_SCHEMA}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Zoonoses - Animal Sem Lugar - Status Code 400
    [Documentation]    The property or the address is required.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${payload}=    Create Dictionary    especie=CAO    nome=Robot sem lugar
    ${r}=    POST Animal Zoonoses    ${payload}
    Should Be Equal As Integers    ${r.status_code}    400
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Zoonoses - Observacao - Status Code 201
    [Documentation]    The dog observation for 10 days, linked to the rabies care.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${animal}=    Seed Cao Zoonoses
    ${notificacao}=    Seed Atendimento Antirrabico
    ${r}=    Observar Cao    ${animal}    2    ${notificacao}
    Should Be Equal As Integers    ${r.status_code}    201
    Validate Object Schema    ${r.json()}    ${OBSERVACAO_ANIMAL_SCHEMA}
    ${fim}=    Evaluate    (datetime.date.today() + datetime.timedelta(days=8)).isoformat()    modules=datetime
    Should Be Equal    ${r.json()}[fimPrevisto]    ${fim}
    ${params}=    Create Dictionary    notificacaoId=${notificacao}
    ${l}=    GET Observacoes Animais    ${params}
    Should Be Equal    ${l.json()}[0][uuid]    ${r.json()}[uuid]
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Zoonoses - Observacao De Morcego - Status Code 422
    [Documentation]    Only dogs and cats are observed.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${animal}=    Seed Cao Zoonoses    MORCEGO
    ${r}=    Observar Cao    ${animal}
    Should Be Equal As Integers    ${r.status_code}    422
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Zoonoses - Observacao Repetida - Status Code 409
    [Documentation]    One open observation per animal.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${animal}=    Seed Cao Zoonoses
    Observar Cao    ${animal}
    ${r}=    Observar Cao    ${animal}
    Should Be Equal As Integers    ${r.status_code}    409
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Zoonoses - Sadio Antes Do Prazo - Status Code 422
    [Documentation]    Healthy only at the end of the 10 days.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${animal}=    Seed Cao Zoonoses
    ${o}=    Observar Cao    ${animal}
    ${payload}=    Create Dictionary    desfecho=SADIO
    ${r}=    POST Desfecho Da Observacao    ${o.json()}[uuid]    ${payload}
    Should Be Equal As Integers    ${r.status_code}    422
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Zoonoses - Sadio No Prazo - Status Code 200
    [Documentation]    An old bite: the overdue observation ends healthy.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${animal}=    Seed Cao Zoonoses
    ${o}=    Observar Cao    ${animal}    12
    Should Be True    ${o.json()}[vencida]
    ${payload}=    Create Dictionary    desfecho=SADIO
    ${r}=    POST Desfecho Da Observacao    ${o.json()}[uuid]    ${payload}
    Should Be Equal As Integers    ${r.status_code}    200
    Should Be Equal    ${r.json()}[desfecho]    SADIO
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Zoonoses - Leishmaniose Reagente - Status Code 200
    [Documentation]    The reactive dog gets the outcome.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${animal}=    Seed Cao Zoonoses
    ${payload}=    Create Dictionary    animalId=${animal}    tipo=LEISHMANIOSE_TR_DPP
    ${e}=    POST Exame Animal    ${payload}
    Should Be Equal As Integers    ${e.status_code}    201
    ${res}=    Create Dictionary    resultado=REAGENTE    desfechoLeishmaniose=ACOMPANHAMENTO
    ${r}=    POST Resultado Exame Animal    ${e.json()}[uuid]    ${res}
    Should Be Equal As Integers    ${r.status_code}    200
    Should Be Equal    ${r.json()}[desfechoLeishmaniose]    ACOMPANHAMENTO
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Zoonoses - Desfecho Na Raiva - Status Code 422
    [Documentation]    The dog outcome is only for leishmaniasis.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${animal}=    Seed Cao Zoonoses
    ${payload}=    Create Dictionary    animalId=${animal}    tipo=RAIVA_IMUNOFLUORESCENCIA
    ${e}=    POST Exame Animal    ${payload}
    ${res}=    Create Dictionary    resultado=REAGENTE    desfechoLeishmaniose=EUTANASIA
    ${r}=    POST Resultado Exame Animal    ${e.json()}[uuid]    ${res}
    Should Be Equal As Integers    ${r.status_code}    422
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Zoonoses - Vacinacao - Status Code 201
    [Documentation]    The post vaccination adds to the year.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${area}    ${micro}    ${imovel}=    Seed Imovel Vetorial
    ${antes}=    GET Vacinacao Antirrabica
    ${payload}=    Create Dictionary    microareaId=${micro}    posto=Escola Robot    caes=${30}    gatos=${9}    responsavel=Equipe Robot
    ${r}=    POST Vacinacao Antirrabica    ${payload}
    Should Be Equal As Integers    ${r.status_code}    201
    ${esperado}=    Evaluate    $antes.json()['caes'] + 30
    Should Be Equal As Integers    ${r.json()}[caes]    ${esperado}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Zoonoses - Vacinacao Vazia - Status Code 400
    [Documentation]    At least one animal.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${area}    ${micro}    ${imovel}=    Seed Imovel Vetorial
    ${payload}=    Create Dictionary    microareaId=${micro}    posto=Escola Robot    responsavel=Equipe Robot
    ${r}=    POST Vacinacao Antirrabica    ${payload}
    Should Be Equal As Integers    ${r.status_code}    400
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Zoonoses - By HTTP Status Code
    [Documentation]    Executes the V4b scenario for the given HTTP status code.
    [Arguments]    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Run Keyword If    '${meuHTTP}' == '201-animal'    Zoonoses - Animal - Status Code 201
    ...    ELSE IF    '${meuHTTP}' == '400-animal-sem-lugar'    Zoonoses - Animal Sem Lugar - Status Code 400
    ...    ELSE IF    '${meuHTTP}' == '201-observacao'    Zoonoses - Observacao - Status Code 201
    ...    ELSE IF    '${meuHTTP}' == '422-observacao-de-morcego'    Zoonoses - Observacao De Morcego - Status Code 422
    ...    ELSE IF    '${meuHTTP}' == '409-observacao-repetida'    Zoonoses - Observacao Repetida - Status Code 409
    ...    ELSE IF    '${meuHTTP}' == '422-sadio-antes-do-prazo'    Zoonoses - Sadio Antes Do Prazo - Status Code 422
    ...    ELSE IF    '${meuHTTP}' == '200-sadio-no-prazo'    Zoonoses - Sadio No Prazo - Status Code 200
    ...    ELSE IF    '${meuHTTP}' == '200-leishmaniose-reagente'    Zoonoses - Leishmaniose Reagente - Status Code 200
    ...    ELSE IF    '${meuHTTP}' == '422-desfecho-na-raiva'    Zoonoses - Desfecho Na Raiva - Status Code 422
    ...    ELSE IF    '${meuHTTP}' == '201-vacinacao'    Zoonoses - Vacinacao - Status Code 201
    ...    ELSE IF    '${meuHTTP}' == '400-vacinacao-vazia'    Zoonoses - Vacinacao Vazia - Status Code 400
    ...    ELSE    Fail    HTTP status code '${meuHTTP}' is not supported for Zoonoses test scenario.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
