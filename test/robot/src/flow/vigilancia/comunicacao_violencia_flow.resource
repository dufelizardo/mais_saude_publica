*** Settings ***
Library     String
Library     Collections
Resource    ../../pom/vigilancia/comunicacao_violencia_pom.resource
Resource    ./vigilancia_flow.resource
Resource    ../intersetorialidade/intersetorialidade_flow.resource
Resource    ../paciente/create_paciente/create_paciente_flow.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
    A comunicação obrigatória da violência notificada à rede intersetorial (ADR-0201).
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
Seed Paciente Com Idade
    [Documentation]    A patient with the given age and sex; returns the uuid and the name.
    [Arguments]    ${idade}    ${sexo}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${suffix}=    Generate Random String    10    [NUMBERS]
    ${nome}=    Set Variable    Robot Violencia ${suffix}
    ${payload}=    Build Paciente Payload    ${nome}    ${suffix}
    ${nascimento}=    Evaluate    (datetime.date.today() - datetime.timedelta(days=int(${idade}) * 366)).isoformat()    modules=datetime
    Set To Dictionary    ${payload}    dataNascimento=${nascimento}    sexo=${sexo}
    ${r}=    POST Create Paciente    ${payload}
    Should Be Equal As Integers    ${r.status_code}    201
    ${uuid}=    Buscar Paciente Uuid Por Cpf    ${suffix}
    RETURN    ${uuid}    ${nome}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Notificar Violencia
    [Documentation]    Notifies physical violence against the patient; returns the notification uuid.
    [Arguments]    ${paciente}    ${unidade}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${agravo}=    Id Do Agravo    VIOLENCIA_INTERPESSOAL_AUTOPROVOCADA
    ${respostas}=    Create Dictionary    tipoViolencia=Física    lesaoAutoprovocada=${False}
    ${payload}=    Create Dictionary    pacienteId=${paciente}    agravoId=${agravo}    unidadeId=${unidade}    origem=ATENDIMENTO    respostas=${respostas}
    ${r}=    POST Notificacao    ${payload}
    Should Be Equal As Integers    ${r.status_code}    201
    RETURN    ${r.json()}[uuid]
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Comunicacao - Mulher Adulta - Status Code 201
    [Documentation]    Violence against an adult woman goes to the police; another type is refused.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${unidade}=    Seed A Unidade De Saude Uuid
    ${paciente}    ${nome}=    Seed Paciente Com Idade    30    FEMININO
    ${n}=    Notificar Violencia    ${paciente}    ${unidade}
    ${r}=    GET Comunicacao Violencia    ${n}
    Should Be True    ${r.json()}[obrigatoria]
    Should Be Equal    ${r.json()}[destino]    Autoridade policial
    ${cras}=    Seed Instituicao Externa
    ${payload}=    Create Dictionary    instituicaoId=${cras}
    ${r}=    POST Comunicacao Violencia    ${n}    ${payload}
    Should Be Equal As Integers    ${r.status_code}    422
    ${delegacia}=    Seed Instituicao Externa    Delegacia Robot    SEGURANCA_PUBLICA    SEGURANCA_PUBLICA
    ${payload}=    Create Dictionary    instituicaoId=${delegacia}
    ${r}=    POST Comunicacao Violencia    ${n}    ${payload}
    Should Be Equal As Integers    ${r.status_code}    201
    Should Be Equal    ${r.json()}[situacao]    FEITA
    Should Be True    ${r.json()}[noPrazo]
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Comunicacao - Crianca - Status Code 201
    [Documentation]    Violence against a child goes to the Conselho Tutelar.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${unidade}=    Seed A Unidade De Saude Uuid
    ${paciente}    ${nome}=    Seed Paciente Com Idade    9    MASCULINO
    ${n}=    Notificar Violencia    ${paciente}    ${unidade}
    ${r}=    GET Comunicacao Violencia    ${n}
    Should Be Equal    ${r.json()}[destino]    Conselho Tutelar
    ${ct}=    Seed Instituicao Externa    Conselho Tutelar Robot    GARANTIA_DE_DIREITOS    CONSELHO_TUTELAR
    ${payload}=    Create Dictionary    instituicaoId=${ct}
    ${r}=    POST Comunicacao Violencia    ${n}    ${payload}
    Should Be Equal As Integers    ${r.status_code}    201
    Should Be Equal    ${r.json()}[situacao]    FEITA
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Comunicacao - Pendentes - Status Code 200
    [Documentation]    A notification without the communication is in the pending list.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${unidade}=    Seed A Unidade De Saude Uuid
    ${paciente}    ${nome}=    Seed Paciente Com Idade    70    MASCULINO
    ${n}=    Notificar Violencia    ${paciente}    ${unidade}
    ${r}=    GET Comunicacoes Pendentes
    Should Be Equal As Integers    ${r.status_code}    200
    ${ids}=    Evaluate    [x['notificacaoId'] for x in $r.json()]
    List Should Contain Value    ${ids}    ${n}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Comunicacao - Homem Adulto - Status Code 422
    [Documentation]    Violence against an adult man has no mandatory communication.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${unidade}=    Seed A Unidade De Saude Uuid
    ${paciente}    ${nome}=    Seed Paciente Com Idade    30    MASCULINO
    ${n}=    Notificar Violencia    ${paciente}    ${unidade}
    ${r}=    GET Comunicacao Violencia    ${n}
    Should Not Be True    ${r.json()}[obrigatoria]
    ${delegacia}=    Seed Instituicao Externa    Delegacia Robot    SEGURANCA_PUBLICA    SEGURANCA_PUBLICA
    ${payload}=    Create Dictionary    instituicaoId=${delegacia}
    ${r}=    POST Comunicacao Violencia    ${n}    ${payload}
    Should Be Equal As Integers    ${r.status_code}    422
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Comunicacao - By Cenario
    [Documentation]    Executes the mandatory communication scenario for the given case.
    [Arguments]    ${caso}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    IF    '${caso}' == 'MULHER'
        Comunicacao - Mulher Adulta - Status Code 201
    ELSE IF    '${caso}' == 'CRIANCA'
        Comunicacao - Crianca - Status Code 201
    ELSE IF    '${caso}' == 'PENDENTES'
        Comunicacao - Pendentes - Status Code 200
    ELSE IF    '${caso}' == 'HOMEM'
        Comunicacao - Homem Adulto - Status Code 422
    ELSE
        Fail    Caso '${caso}' não suportado no cenário Comunicação da Violência.
    END
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
