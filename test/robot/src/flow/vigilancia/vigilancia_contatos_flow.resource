*** Settings ***
Library     String
Library     Collections
Resource    ../../pom/vigilancia/vigilancia_contatos_pom.resource
Resource    ./vigilancia_flow.resource
Resource    ../territorio/adscricao_flow.resource
Resource    ../common/schema_validation_flow.resource
Resource    ../../resource/config/schema/schema.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
Seed Caso De Sarampo
    [Documentation]    Notifies measles for a new patient; returns the notification and the patient.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${unidade}=    Seed A Unidade De Saude Uuid
    ${paciente}    ${cpf}    ${cns}    ${nome}=    Seed A Paciente
    ${sarampo}=    Id Do Agravo    SARAMPO
    ${payload}=    Create Dictionary    pacienteId=${paciente}    agravoId=${sarampo}    unidadeId=${unidade}    origem=ATENDIMENTO
    ${n}=    POST Notificacao    ${payload}
    Should Be Equal As Integers    ${n.status_code}    201
    RETURN    ${n.json()}[uuid]    ${paciente}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Contato - Registro - Status Code 201
    [Documentation]    A contact without registration, with the monitoring period of measles.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${notificacao}    ${paciente}=    Seed Caso De Sarampo
    ${exposicao}=    Evaluate    (datetime.date.today() - datetime.timedelta(days=5)).isoformat()    modules=datetime
    ${fim}=    Evaluate    (datetime.date.today() + datetime.timedelta(days=16)).isoformat()    modules=datetime
    ${payload}=    Create Dictionary    nome=Vizinha Robot    telefone=19 99999-0000    tipoExposicao=COMUNITARIO    dataExposicao=${exposicao}
    ${r}=    POST Contato    ${notificacao}    ${payload}
    Should Be Equal As Integers    ${r.status_code}    201
    ${contato}=    Set Variable    ${r.json()}[contatos][0]
    Validate Object Schema    ${contato}    ${CONTATO_SCHEMA}
    Should Be Equal    ${contato}[situacao]    EM_ACOMPANHAMENTO
    Should Be Equal    ${contato}[fimMonitoramento]    ${fim}
    Should Not Be True    ${contato}[monitoramentoVencido]
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Contato - Sem Nome - Status Code 400
    [Documentation]    Without a registered patient, the contact needs a name.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${notificacao}    ${paciente}=    Seed Caso De Sarampo
    ${payload}=    Create Dictionary    tipoExposicao=DOMICILIAR
    ${r}=    POST Contato    ${notificacao}    ${payload}
    Should Be Equal As Integers    ${r.status_code}    400
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Contato - Proprio Caso - Status Code 422
    [Documentation]    The case cannot be its own contact.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${notificacao}    ${paciente}=    Seed Caso De Sarampo
    ${payload}=    Create Dictionary    pacienteId=${paciente}    tipoExposicao=DOMICILIAR
    ${r}=    POST Contato    ${notificacao}    ${payload}
    Should Be Equal As Integers    ${r.status_code}    422
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Contato - Vira Caso - Status Code 201
    [Documentation]    Notifying the contact links the new case to the index case; twice is refused.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${notificacao}    ${paciente}=    Seed Caso De Sarampo
    ${outro}    ${cpf}    ${cns}    ${nome}=    Seed A Paciente
    ${payload}=    Create Dictionary    pacienteId=${outro}    tipoExposicao=DOMICILIAR
    ${r}=    POST Contato    ${notificacao}    ${payload}
    Should Be Equal As Integers    ${r.status_code}    201
    ${contato}=    Set Variable    ${r.json()}[contatos][0][uuid]
    ${virou}=    Create Dictionary    tipoExposicao=DOMICILIAR    situacao=VIROU_CASO
    ${p}=    PATCH Contato    ${contato}    ${virou}
    Should Be Equal As Integers    ${p.status_code}    422
    ${sarampo}=    Id Do Agravo    SARAMPO
    ${unidade}=    Set Variable    ${r.json()}[unidadeId]
    ${notif}=    Create Dictionary    pacienteId=${outro}    agravoId=${sarampo}    unidadeId=${unidade}    origem=BUSCA_ATIVA    contatoId=${contato}
    ${n}=    POST Notificacao    ${notif}
    Should Be Equal As Integers    ${n.status_code}    201
    ${d}=    GET Notificacao    ${notificacao}
    Should Be Equal    ${d.json()}[contatos][0][situacao]    VIROU_CASO
    Should Be Equal    ${d.json()}[contatos][0][notificacaoGeradaId]    ${n.json()}[uuid]
    ${n2}=    POST Notificacao    ${notif}
    Should Be Equal As Integers    ${n2.status_code}    409
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Contato - Inexistente - Status Code 404
    [Documentation]    Updating a contact that does not exist.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${uuid}=    Evaluate    str(uuid.uuid4())    modules=uuid
    ${payload}=    Create Dictionary    tipoExposicao=DOMICILIAR    situacao=LIBERADO
    ${r}=    PATCH Contato    ${uuid}    ${payload}
    Should Be Equal As Integers    ${r.status_code}    404
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Local - Pelo Domicilio - Status Code 200
    [Documentation]    Without a marked point, the case takes the family's home and its micro-area.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${paciente}    ${equipe}    ${unidade}    ${familia}    ${nome}=    Seed Familia Adscrita
    ${sarampo}=    Id Do Agravo    SARAMPO
    ${payload}=    Create Dictionary    pacienteId=${paciente}    agravoId=${sarampo}    unidadeId=${unidade}    origem=ATENDIMENTO
    ${n}=    POST Notificacao    ${payload}
    Should Be Equal As Integers    ${n.status_code}    201
    Should Be Equal    ${n.json()}[origemLocal]    DOMICILIO
    Should Be Equal    ${n.json()}[microareaCodigo]    07
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Local - Marcado - Status Code 200
    [Documentation]    The investigator marks the point; the micro-area comes from the map.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${lon}=    Evaluate    round(-60 + random.random() * 5, 3)    modules=random
    ${lat}=    Evaluate    round(-30 + random.random() * 5, 3)    modules=random
    ${area}    ${micro}=    Seed Area Com Equipe E Microarea    ${lon}    ${lat}
    ${notificacao}    ${paciente}=    Seed Caso De Sarampo
    ${plat}=    Evaluate    ${lat} + 0.005
    ${plon}=    Evaluate    ${lon} + 0.005
    ${ponto}=    Create Dictionary    latitude=${plat}    longitude=${plon}
    ${r}=    POST Local Do Caso    ${notificacao}    ${ponto}
    Should Be Equal As Integers    ${r.status_code}    200
    Should Be Equal    ${r.json()}[origemLocal]    MARCADO
    Should Be Equal    ${r.json()}[microareaCodigo]    07
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Local - Sem Longitude - Status Code 400
    [Documentation]    The point needs latitude and longitude.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${notificacao}    ${paciente}=    Seed Caso De Sarampo
    ${ponto}=    Create Dictionary    latitude=${-22.0}
    ${r}=    POST Local Do Caso    ${notificacao}    ${ponto}
    Should Be Equal As Integers    ${r.status_code}    400
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Mapa - Casos - Status Code 200
    [Documentation]    The map brings the case, the curve and, for who investigates, the points.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${notificacao}    ${paciente}=    Seed Caso De Sarampo
    ${sarampo}=    Id Do Agravo    SARAMPO
    ${params}=    Create Dictionary    agravoId=${sarampo}
    ${r}=    GET Mapa Vigilancia    ${params}
    Should Be Equal As Integers    ${r.status_code}    200
    Validate Object Schema    ${r.json()}    ${MAPA_VIGILANCIA_SCHEMA}
    Should Be True    ${r.json()}[total] >= 1
    Should Be True    ${r.json()}[pontosVisiveis]
    Should Not Be Empty    ${r.json()}[curva]
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Vigilancia Contatos - By HTTP Status Code
    [Documentation]    Executes the V2a scenario for the given HTTP status code.
    [Arguments]    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Run Keyword If    '${meuHTTP}' == '201-contato'    Contato - Registro - Status Code 201
    ...    ELSE IF    '${meuHTTP}' == '400-contato'    Contato - Sem Nome - Status Code 400
    ...    ELSE IF    '${meuHTTP}' == '422-contato'    Contato - Proprio Caso - Status Code 422
    ...    ELSE IF    '${meuHTTP}' == '201-vira-caso'    Contato - Vira Caso - Status Code 201
    ...    ELSE IF    '${meuHTTP}' == '404-contato'    Contato - Inexistente - Status Code 404
    ...    ELSE IF    '${meuHTTP}' == '200-local-domicilio'    Local - Pelo Domicilio - Status Code 200
    ...    ELSE IF    '${meuHTTP}' == '200-local-marcado'    Local - Marcado - Status Code 200
    ...    ELSE IF    '${meuHTTP}' == '400-local'    Local - Sem Longitude - Status Code 400
    ...    ELSE IF    '${meuHTTP}' == '200-mapa'    Mapa - Casos - Status Code 200
    ...    ELSE    Fail    HTTP status code '${meuHTTP}' is not supported for Vigilancia Contatos test scenario.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
