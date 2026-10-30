*** Settings ***
Library     String
Library     Collections
Resource    ../../pom/emergencias/eventos_especializados_pom.resource
Resource    desmobilizacao_flow.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
    Evento de massa planejado e estruturas temporárias (ADR-0160).
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
Seed Evento De Massa Planejado
    [Documentation]    A mass gathering event planned (not started yet); returns the emergency uuid.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${tipo}=    Id Do Tipo Emergencia    EVENTO_DE_MASSA
    ${sufixo}=    Generate Random String    8    [NUMBERS]
    ${evento}=    Create Dictionary    tipoId=${tipo}    fonte=GESTOR    local=Avenida Robot    descricao=Robot carnaval ${sufixo}
    ${e}=    POST Evento Emergencial    ${evento}
    ${nivel}=    Id Do Nivel Emergencia    1
    ${payload}=    Create Dictionary    nome=Carnaval Robot    nivelId=${nivel}    justificativa=Público acima de 50 mil    planejada=${True}
    ${r}=    POST Converter Em Emergencia    ${e.json()}[uuid]    ${payload}
    Should Be Equal As Integers    ${r.status_code}    201
    RETURN    ${r.json()}[uuid]
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Dados Do Evento De Massa
    [Documentation]    The mass gathering data, starting in 3 days for 12 hours; returns the payload.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${inicio}=    Evaluate    (datetime.datetime.now(datetime.timezone.utc) + datetime.timedelta(days=3)).replace(microsecond=0)    modules=datetime
    ${fim}=    Evaluate    $inicio + datetime.timedelta(hours=12)    modules=datetime
    ${payload}=    Create Dictionary    natureza=FESTA_POPULAR    local=Avenida Central    inicioPrevisto=${inicio.isoformat()}    fimPrevisto=${fim.isoformat()}
    ...    publicoEstimado=${80000}    publicoMaximo=${120000}    organizador=Liga das escolas    risco=ALTO
    RETURN    ${payload}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Nova Estrutura Na Emergencia
    [Documentation]    A planned medical post linked to a reference unit; returns the response.
    [Arguments]    ${emergencia}    ${tipo}=POSTO_MEDICO
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${unidade}=    Seed A Unidade De Saude Uuid
    ${payload}=    Create Dictionary    tipo=${tipo}    nome=Posto Robot    local=Praça Robot    latitude=${-23.55}    longitude=${-46.63}
    ...    unidadeReferenciaId=${unidade}    leitos=${6}    atendimentosDia=${300}    ambulancias=${2}    horario=18h às 6h
    ${r}=    POST Estrutura Temporaria    ${emergencia}    ${payload}
    RETURN    ${r}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Evento De Massa Em Operacao
    [Documentation]    A planned mass event with data and a medical post, then started and the post opened; returns the emergency and the post.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${em}=    Seed Evento De Massa Planejado
    ${dados}=    Dados Do Evento De Massa
    PUT Evento Massa    ${em}    ${dados}
    ${r}=    Nova Estrutura Na Emergencia    ${em}
    ${posto}=    Set Variable    ${r.json()}[estruturas][0][uuid]
    ${j}=    Create Dictionary    justificativa=Abertura dos portões
    ${o}=    POST Iniciar Operacao    ${em}    ${j}
    Should Be Equal As Integers    ${o.status_code}    200
    ${s}=    Create Dictionary    situacao=EM_OPERACAO
    ${a}=    POST Situacao Da Estrutura    ${em}    ${posto}    ${s}
    Should Be Equal As Integers    ${a.status_code}    200
    RETURN    ${em}    ${posto}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Boletim De Hoje
    [Documentation]    A report for today.
    [Arguments]    ${emergencia}    ${estrutura}    ${atendimentos}=${120}    ${remocoes}=${4}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${hoje}=    Evaluate    datetime.date.today().isoformat()    modules=datetime
    ${payload}=    Create Dictionary    data=${hoje}    atendimentos=${atendimentos}    remocoes=${remocoes}
    ${r}=    POST Boletim Da Estrutura    ${emergencia}    ${estrutura}    ${payload}
    RETURN    ${r}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Eventos Especializados - Perfil Do Tipo - Status Code 200
    [Documentation]    The seeded types carry the profile.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${t}=    GET Tipos Emergencia
    Should Be Equal As Integers    ${t.status_code}    200
    ${massa}=    Evaluate    [x['perfil'] for x in $t.json() if x['codigo'] == 'EVENTO_DE_MASSA'][0]
    Should Be Equal    ${massa}    EVENTO_DE_MASSA
    ${geral}=    Evaluate    [x['perfil'] for x in $t.json() if x['codigo'] == 'INUNDACAO'][0]
    Should Be Equal    ${geral}    GERAL
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Eventos Especializados - Planejar - Status Code 201
    [Documentation]    The mass gathering event is planned before it starts.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${em}=    Seed Evento De Massa Planejado
    ${r}=    GET Emergencia    ${em}
    Should Be Equal    ${r.json()}[situacao]    PLANEJADA
    Should Be Equal    ${r.json()}[perfil]    EVENTO_DE_MASSA
    Validate Object Schema    ${r.json()}    ${EMERGENCIA_SCHEMA}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Eventos Especializados - Planejar Outro Perfil - Status Code 422
    [Documentation]    Only the mass gathering event is planned.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${e}=    Seed Evento Emergencial
    ${nivel}=    Id Do Nivel Emergencia    1
    ${payload}=    Create Dictionary    nome=Enchente Robot    nivelId=${nivel}    justificativa=Cota    planejada=${True}
    ${r}=    POST Converter Em Emergencia    ${e.json()}[uuid]    ${payload}
    Should Be Equal As Integers    ${r.status_code}    422
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Eventos Especializados - Operacao Sem Dados - Status Code 422
    [Documentation]    The operation needs the event data.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${em}=    Seed Evento De Massa Planejado
    ${j}=    Create Dictionary    justificativa=Abertura
    ${r}=    POST Iniciar Operacao    ${em}    ${j}
    Should Be Equal As Integers    ${r.status_code}    422
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Eventos Especializados - Periodo Invalido - Status Code 400
    [Documentation]    The end does not come before the start.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${em}=    Seed Evento De Massa Planejado
    ${dados}=    Dados Do Evento De Massa
    Set To Dictionary    ${dados}    fimPrevisto=2020-01-01T00:00:00Z
    ${r}=    PUT Evento Massa    ${em}    ${dados}
    Should Be Equal As Integers    ${r.status_code}    400
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Eventos Especializados - Dados Do Evento - Status Code 200
    [Documentation]    The event data is saved.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${em}=    Seed Evento De Massa Planejado
    ${dados}=    Dados Do Evento De Massa
    ${r}=    PUT Evento Massa    ${em}    ${dados}
    Should Be Equal As Integers    ${r.status_code}    200
    Should Be Equal As Integers    ${r.json()}[eventoMassa][publicoEstimado]    80000
    Should Be Equal    ${r.json()}[eventoMassa][risco]    ALTO
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Eventos Especializados - Estrutura - Status Code 201
    [Documentation]    The structure is born planned, linked to the reference unit.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${em}=    Seed Evento De Massa Planejado
    ${r}=    Nova Estrutura Na Emergencia    ${em}
    Should Be Equal As Integers    ${r.status_code}    201
    Should Be Equal    ${r.json()}[estruturas][0][situacao]    PLANEJADA
    Validate Object Schema    ${r.json()}[estruturas][0]    ${ESTRUTURA_TEMPORARIA_SCHEMA}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Eventos Especializados - Abrir Antes Da Operacao - Status Code 422
    [Documentation]    The structure opens only after the operation starts.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${em}=    Seed Evento De Massa Planejado
    ${r}=    Nova Estrutura Na Emergencia    ${em}
    ${s}=    Create Dictionary    situacao=EM_OPERACAO
    ${a}=    POST Situacao Da Estrutura    ${em}    ${r.json()}[estruturas][0][uuid]    ${s}
    Should Be Equal As Integers    ${a.status_code}    422
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Eventos Especializados - Iniciar Operacao - Status Code 200
    [Documentation]    The planned emergency becomes active and the post opens.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${em}    ${posto}=    Evento De Massa Em Operacao
    ${r}=    GET Emergencia    ${em}
    Should Be Equal    ${r.json()}[situacao]    ATIVA
    Should Be Equal    ${r.json()}[estruturas][0][situacao]    EM_OPERACAO
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Eventos Especializados - Boletim - Status Code 201
    [Documentation]    The reports add up in the structure.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${em}    ${posto}=    Evento De Massa Em Operacao
    Boletim De Hoje    ${em}    ${posto}
    ${r}=    Boletim De Hoje    ${em}    ${posto}    ${80}    ${1}
    Should Be Equal As Integers    ${r.status_code}    201
    Should Be Equal As Integers    ${r.json()}[estruturas][0][atendimentos]    200
    Should Be Equal As Integers    ${r.json()}[estruturas][0][remocoes]    5
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Eventos Especializados - Desmobilizar E Relatorio - Status Code 200
    [Documentation]    The demobilization deactivates the structures; the report sums the reports.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${em}    ${posto}=    Evento De Massa Em Operacao
    Boletim De Hoje    ${em}    ${posto}
    ${payload}=    Create Dictionary    responsavel=Coordenação    justificativa=Fim do desfile
    ${r}=    POST Desmobilizacao    ${em}    ${payload}
    Should Be Equal As Integers    ${r.status_code}    200
    Should Be Equal As Integers    ${r.json()}[desmobilizacao][estruturasDesativadas]    1
    ${rel}=    GET Relatorio Pos Evento    ${em}
    Should Be Equal As Integers    ${rel.json()}[indicadores][atendimentosEstruturas]    120
    Should Be Equal As Integers    ${rel.json()}[indicadores][publicoEstimado]    80000
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Eventos Especializados - Cancelar Planejada - Status Code 200
    [Documentation]    The planned event is cancelled before the operation.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${em}=    Seed Evento De Massa Planejado
    ${payload}=    Create Dictionary    motivo=Show cancelado pela organização
    ${r}=    POST Encerrar Emergencia E5    ${em}    ${payload}
    Should Be Equal As Integers    ${r.status_code}    200
    Should Be Equal    ${r.json()}[situacao]    ENCERRADA
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Eventos Especializados - By HTTP Status Code
    [Documentation]    Executes the E6a scenario for the given HTTP status code.
    [Arguments]    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Run Keyword If    '${meuHTTP}' == '200-perfil-do-tipo'    Eventos Especializados - Perfil Do Tipo - Status Code 200
    ...    ELSE IF    '${meuHTTP}' == '201-planejar'    Eventos Especializados - Planejar - Status Code 201
    ...    ELSE IF    '${meuHTTP}' == '422-planejar-outro-perfil'    Eventos Especializados - Planejar Outro Perfil - Status Code 422
    ...    ELSE IF    '${meuHTTP}' == '422-operacao-sem-dados'    Eventos Especializados - Operacao Sem Dados - Status Code 422
    ...    ELSE IF    '${meuHTTP}' == '400-periodo-invalido'    Eventos Especializados - Periodo Invalido - Status Code 400
    ...    ELSE IF    '${meuHTTP}' == '200-dados-do-evento'    Eventos Especializados - Dados Do Evento - Status Code 200
    ...    ELSE IF    '${meuHTTP}' == '201-estrutura'    Eventos Especializados - Estrutura - Status Code 201
    ...    ELSE IF    '${meuHTTP}' == '422-abrir-antes-da-operacao'    Eventos Especializados - Abrir Antes Da Operacao - Status Code 422
    ...    ELSE IF    '${meuHTTP}' == '200-iniciar-operacao'    Eventos Especializados - Iniciar Operacao - Status Code 200
    ...    ELSE IF    '${meuHTTP}' == '201-boletim'    Eventos Especializados - Boletim - Status Code 201
    ...    ELSE IF    '${meuHTTP}' == '200-desmobilizar-e-relatorio'    Eventos Especializados - Desmobilizar E Relatorio - Status Code 200
    ...    ELSE IF    '${meuHTTP}' == '200-cancelar-planejada'    Eventos Especializados - Cancelar Planejada - Status Code 200
    ...    ELSE    Fail    HTTP status code '${meuHTTP}' is not supported for Eventos Especializados test scenario.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
