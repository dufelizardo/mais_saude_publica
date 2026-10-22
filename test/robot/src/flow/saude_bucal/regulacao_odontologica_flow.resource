*** Settings ***
Library     String
Library     Collections
Resource    ../solicitacao_regulacao/create_solicitacao_regulacao/create_solicitacao_regulacao_flow.resource
Resource    ../../pom/solicitacao_regulacao/negativa_solicitacao_regulacao/negativa_solicitacao_regulacao_pom.resource
Resource    ../../pom/solicitacao_regulacao/autorizacao_solicitacao_regulacao/autorizacao_solicitacao_regulacao_pom.resource
Resource    ../../pom/procedimento_regulado/create_procedimento_regulado/create_procedimento_regulado_pom.resource
Resource    ./plano_tratamento_flow.resource
Resource    ../common/schema_validation_flow.resource
Resource    ../../resource/config/schema/schema.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
    Saúde Bucal, fatia SB4a (ADR-0138): encaminhamento odontológico pela Regulação.
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
Id Do Procedimento Regulado
    [Documentation]    Returns the uuid of a regulated catalog item by name.
    [Arguments]    ${nome}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${r}=    GET List Procedimento Regulado
    ${ids}=    Evaluate    [p["uuid"] for p in $r.json() if p["nome"] == $nome]
    RETURN    ${ids}[0]
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Seed Plano Com Pulpotomia
    [Documentation]    Opens a plan with a pulpotomy on 46; returns the item, the patient, the unit and the dentist.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${atendimento}    ${paciente}    ${matricula}=    Seed Atendimento Odontologico
    ${item}=    Item Do Plano    PULPOTOMIA    ${46}
    ${itens}=    Create List    ${item}
    ${payload}=    Create Dictionary    atendimentoId=${atendimento}    profissionalMatricula=${matricula}    prioridade=PRIORITARIA    itens=${itens}
    ${r}=    POST Plano Tratamento    ${payload}
    Should Be Equal As Integers    ${r.status_code}    201
    Get Mais Saude Publica Session
    ${at}=    GET On Session    msp_session    ${BASE_URL}/atendimento/${atendimento}    expected_status=ANY
    RETURN    ${r.json()}[itens][0][uuid]    ${paciente}    ${at.json()}[unidadeUuid]    ${matricula}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Solicitar Endodontia
    [Documentation]    Requests the endodontics regulation, optionally from a plan item.
    [Arguments]    ${paciente}    ${unidade}    ${matricula}    ${item}=${None}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${procedimento}=    Id Do Procedimento Regulado    Endodontia (CEO)
    ${payload}=    Build Solicitacao Regulacao Payload    ${paciente}    ${procedimento}    ${unidade}    ${matricula}    AMARELO    K04.0
    IF    $item is not None    Set To Dictionary    ${payload}    itemPlanoTratamentoId=${item}
    ${r}=    POST Create Solicitacao Regulacao    ${payload}
    RETURN    ${r}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Regulacao Odontologica - Catalogo - Status Code 200
    [Documentation]    The regulated catalog brings endodontics, which requires the CEO capacity.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${r}=    GET List Procedimento Regulado
    Should Be Equal As Integers    ${r.status_code}    200
    ${endo}=    Evaluate    [p for p in $r.json() if p["nome"] == "Endodontia (CEO)"][0]
    Validate Object Schema    ${endo}    ${PROCEDIMENTO_REGULADO_ODONTOLOGICO_SCHEMA}
    Should Be Equal    ${endo}[capacidadeExigidaNome]    Endodontia
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Regulacao Odontologica - Executante Sem Capacidade - Status Code 422
    [Documentation]    A unit without the endodontics capacity does not receive it.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${item}    ${paciente}    ${unidade}    ${matricula}=    Seed Plano Com Pulpotomia
    Solicitar Endodontia    ${paciente}    ${unidade}    ${matricula}
    ${lista}=    GET List Solicitacao Regulacao Do Paciente    ${paciente}
    ${regulador}=    Seed A Profissional Matricula
    ${quando}=    Evaluate    (datetime.datetime.now() + datetime.timedelta(days=3)).replace(second=0, microsecond=0).isoformat()    modules=datetime
    ${payload}=    Create Dictionary    profissionalMatricula=${regulador}    unidadeExecutanteId=${unidade}    dataHoraPrevista=${quando}
    ${r}=    POST Autorizar Solicitacao Regulacao    ${lista.json()}[0][uuid]    ${payload}
    Should Be Equal As Integers    ${r.status_code}    422
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Regulacao Odontologica - Encaminhar O Item - Status Code 201
    [Documentation]    The request from the plan item leaves it forwarded.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${item}    ${paciente}    ${unidade}    ${matricula}=    Seed Plano Com Pulpotomia
    ${r}=    Solicitar Endodontia    ${paciente}    ${unidade}    ${matricula}    ${item}
    Should Be Equal As Integers    ${r.status_code}    201
    ${planos}=    GET Planos Do Paciente    ${paciente}
    Should Be Equal    ${planos.json()}[0][itens][0][situacao]    ENCAMINHADO
    Should Be Equal    ${planos.json()}[0][itens][0][regulacaoProcedimentoNome]    Endodontia (CEO)
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Regulacao Odontologica - Negativa Devolve O Item - Status Code 200
    [Documentation]    The denial brings the item back to planned.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${item}    ${paciente}    ${unidade}    ${matricula}=    Seed Plano Com Pulpotomia
    Solicitar Endodontia    ${paciente}    ${unidade}    ${matricula}    ${item}
    ${lista}=    GET List Solicitacao Regulacao Do Paciente    ${paciente}
    ${regulador}=    Seed A Profissional Matricula
    ${payload}=    Create Dictionary    profissionalMatricula=${regulador}    motivo=Tratar na APS
    ${r}=    POST Negar Solicitacao Regulacao    ${lista.json()}[0][uuid]    ${payload}
    Should Be Equal As Integers    ${r.status_code}    200
    ${planos}=    GET Planos Do Paciente    ${paciente}
    Should Be Equal    ${planos.json()}[0][itens][0][situacao]    PLANEJADO
    Should Be Equal    ${planos.json()}[0][itens][0][motivoSituacao]    Regulação negada: Tratar na APS
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Regulacao Odontologica - Item Ja Encaminhado - Status Code 422
    [Documentation]    A forwarded item is not forwarded again.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${item}    ${paciente}    ${unidade}    ${matricula}=    Seed Plano Com Pulpotomia
    Solicitar Endodontia    ${paciente}    ${unidade}    ${matricula}    ${item}
    ${procedimento}=    Id Do Procedimento Regulado    Periodontia (CEO)
    ${payload}=    Build Solicitacao Regulacao Payload    ${paciente}    ${procedimento}    ${unidade}    ${matricula}    AMARELO    K05.3
    Set To Dictionary    ${payload}    itemPlanoTratamentoId=${item}
    ${r}=    POST Create Solicitacao Regulacao    ${payload}
    Should Be Equal As Integers    ${r.status_code}    422
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Regulacao Odontologica - By HTTP Status Code
    [Documentation]    Executes the SB4a scenario for the given HTTP status code.
    [Arguments]    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Run Keyword If    '${meuHTTP}' == '200-catalogo'    Regulacao Odontologica - Catalogo - Status Code 200
    ...    ELSE IF    '${meuHTTP}' == '422-executante'    Regulacao Odontologica - Executante Sem Capacidade - Status Code 422
    ...    ELSE IF    '${meuHTTP}' == '201-encaminhar'    Regulacao Odontologica - Encaminhar O Item - Status Code 201
    ...    ELSE IF    '${meuHTTP}' == '200-negativa'    Regulacao Odontologica - Negativa Devolve O Item - Status Code 200
    ...    ELSE IF    '${meuHTTP}' == '422-encaminhado'    Regulacao Odontologica - Item Ja Encaminhado - Status Code 422
    ...    ELSE    Fail    HTTP status code '${meuHTTP}' is not supported for Regulacao Odontologica test scenario.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
