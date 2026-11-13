*** Settings ***
Library     String
Library     Collections
Library     DateTime
Resource    ../../pom/qualidade/seguranca_do_paciente_pom.resource
Resource    ../setor/create_setor/create_setor_flow.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
    A notificação do incidente, a análise do núcleo e o Notivisa (ADR-0203).
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
Seed Incidente
    [Documentation]    A notified incident in a fresh unit; returns the uuid and the unit.
    [Arguments]    ${tipo}=QUEDA    ${horas_atras}=2
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${unidade}=    Seed A Unidade De Saude Uuid
    ${quando}=    Get Current Date    UTC    increment=-${horas_atras} hours    result_format=%Y-%m-%dT%H:%M:%SZ
    ${payload}=    Create Dictionary    unidadeId=${unidade}    ocorridoEm=${quando}    local=Enfermaria Robot    tipo=${tipo}
    ...    descricao=Paciente caiu ao levantar.
    ${r}=    POST Incidente Seguranca    ${payload}
    Should Be Equal As Integers    ${r.status_code}    201
    RETURN    ${r.json()}[uuid]    ${unidade}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Seguranca - Notificacao Anonima - Status Code 201
    [Documentation]    Anonymous notification returns the protocol; a future occurrence is refused.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${unidade}=    Seed A Unidade De Saude Uuid
    ${futuro}=    Get Current Date    UTC    increment=1 day    result_format=%Y-%m-%dT%H:%M:%SZ
    ${payload}=    Create Dictionary    unidadeId=${unidade}    ocorridoEm=${futuro}    local=Posto    tipo=MEDICACAO    descricao=Quase erro.
    ${r}=    POST Incidente Seguranca    ${payload}
    Should Be Equal As Integers    ${r.status_code}    400
    ${ontem}=    Get Current Date    UTC    increment=-1 day    result_format=%Y-%m-%dT%H:%M:%SZ
    Set To Dictionary    ${payload}    ocorridoEm=${ontem}    anonima=${True}
    ${r}=    POST Incidente Seguranca    ${payload}
    Should Be Equal As Integers    ${r.status_code}    201
    Should Be True    ${r.json()}[anonima]
    Should Match Regexp    ${r.json()}[numero]    ^\\d{4}/\\d{6}$
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Seguranca - Analise Com Causa Raiz - Status Code 200
    [Documentation]    Severe harm requires the root cause to conclude.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${id}    ${unidade}=    Seed Incidente
    ${classe}=    Create Dictionary    natureza=EVENTO_ADVERSO    dano=GRAVE
    ${r}=    POST Acao Incidente    ${id}    classificacao    ${classe}
    Should Be Equal As Integers    ${r.status_code}    200
    Should Be Equal    ${r.json()}[situacao]    EM_ANALISE
    ${fatores}=    Create List    AMBIENTE
    ${conclusao}=    Create Dictionary    fatores=${fatores}
    ${r}=    POST Acao Incidente    ${id}    conclusao    ${conclusao}
    Should Be Equal As Integers    ${r.status_code}    400
    Set To Dictionary    ${conclusao}    causaRaiz=Piso molhado sem sinalização.
    ${r}=    POST Acao Incidente    ${id}    conclusao    ${conclusao}
    Should Be Equal As Integers    ${r.status_code}    200
    Should Be Equal    ${r.json()}[situacao]    CONCLUIDA
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Seguranca - Notivisa Do Obito - Status Code 200
    [Documentation]    A death is due in 72 hours; overdue until registered, once only.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${id}    ${unidade}=    Seed Incidente    CIRURGIA    96
    ${classe}=    Create Dictionary    natureza=EVENTO_ADVERSO    dano=OBITO
    ${r}=    POST Acao Incidente    ${id}    classificacao    ${classe}
    Should Be True    ${r.json()}[notivisaVencida]
    ${nv}=    Create Dictionary    numero=NV-ROBOT-1
    ${r}=    POST Acao Incidente    ${id}    notivisa    ${nv}
    Should Be Equal As Integers    ${r.status_code}    200
    Should Not Be True    ${r.json()}[notivisaVencida]
    ${r}=    POST Acao Incidente    ${id}    notivisa    ${nv}
    Should Be Equal As Integers    ${r.status_code}    409
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Seguranca - Painel - Status Code 200
    [Documentation]    The panel counts the notifications.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${id}    ${unidade}=    Seed Incidente
    ${r}=    GET Painel Seguranca
    Should Be Equal As Integers    ${r.status_code}    200
    Should Be True    ${r.json()}[total] >= 1
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Seguranca - By Cenario
    [Documentation]    Executes the patient safety scenario for the given case.
    [Arguments]    ${caso}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    IF    '${caso}' == 'ANONIMA'
        Seguranca - Notificacao Anonima - Status Code 201
    ELSE IF    '${caso}' == 'CAUSA_RAIZ'
        Seguranca - Analise Com Causa Raiz - Status Code 200
    ELSE IF    '${caso}' == 'NOTIVISA'
        Seguranca - Notivisa Do Obito - Status Code 200
    ELSE IF    '${caso}' == 'PAINEL'
        Seguranca - Painel - Status Code 200
    ELSE
        Fail    Caso '${caso}' não suportado no cenário Segurança do Paciente.
    END
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
