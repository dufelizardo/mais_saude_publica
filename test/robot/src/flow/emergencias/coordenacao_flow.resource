*** Settings ***
Library     String
Library     Collections
Resource    ../../pom/emergencias/coordenacao_pom.resource
Resource    ../../pom/escalas/escala/escala_pom.resource
Resource    contingencia_flow.resource
Resource    ../setor/create_setor/create_setor_flow.resource
Resource    ../profissional/get_profissional_by_matricula/get_profissional_by_matricula_flow.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
    Ações emergenciais, mobilização (com a exceção da Escala) e capacidade emergencial (ADR-0158).
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
Nova Acao Avulsa
    [Documentation]    A free action; returns the response.
    [Arguments]    ${emergencia}    ${descricao}=Abrir abrigo    ${prioridade}=ALTA
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${payload}=    Create Dictionary    descricao=${descricao}    responsavel=Coordenação    prioridade=${prioridade}
    ${r}=    POST Acao Emergencial    ${emergencia}    ${payload}
    RETURN    ${r}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Mobilizar Profissional
    [Documentation]    Mobilizes a professional to the unit; returns the response.
    [Arguments]    ${emergencia}    ${matricula}    ${destino}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${payload}=    Create Dictionary    tipo=PROFISSIONAL    profissionalMatricula=${matricula}    destinoId=${destino}
    ${r}=    POST Mobilizacao    ${emergencia}    ${payload}
    RETURN    ${r}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Seed Capacidade
    [Documentation]    Registers 30 activatable beds in a fresh unit; returns the capacity uuid.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${unidade}=    Seed A Unidade De Saude Uuid
    ${payload}=    Create Dictionary    unidadeId=${unidade}    recurso=LEITOS    normal=${120}    emergencial=${30}
    ${r}=    POST Capacidade Emergencial    ${payload}
    Should Be Equal As Integers    ${r.status_code}    201
    RETURN    ${r.json()}[uuid]
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Coordenacao - Acao Avulsa - Status Code 201
    [Documentation]    A free action enters the board.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${em}=    Seed Emergencia
    ${r}=    Nova Acao Avulsa    ${em.json()}[uuid]
    Should Be Equal As Integers    ${r.status_code}    201
    Should Be Equal    ${r.json()}[acoes][0][situacao]    PENDENTE
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Coordenacao - Acoes Do Plano - Status Code 201
    [Documentation]    The planned actions of the level become actions.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${em}=    Seed Emergencia
    ${plano}=    Seed Plano Vigente    INUNDACAO    1
    Ativar Plano Na Emergencia    ${em.json()}[uuid]    ${plano}
    ${r}=    POST Gerar Acoes Do Plano    ${em.json()}[uuid]
    Should Be Equal As Integers    ${r.status_code}    201
    Should Be Equal    ${r.json()}[acoes][0][descricao]    Abrir o COE
    Should Not Be Equal    ${r.json()}[acoes][0][planoCodigo]    ${None}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Coordenacao - Nada A Gerar - Status Code 422
    [Documentation]    Without an active plan there is nothing to generate.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${em}=    Seed Emergencia
    ${r}=    POST Gerar Acoes Do Plano    ${em.json()}[uuid]
    Should Be Equal As Integers    ${r.status_code}    422
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Coordenacao - Concluir Acao - Status Code 200
    [Documentation]    The action is concluded.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${em}=    Seed Emergencia
    ${a}=    Nova Acao Avulsa    ${em.json()}[uuid]
    ${payload}=    Create Dictionary    situacao=CONCLUIDA
    ${r}=    POST Situacao Acao    ${em.json()}[uuid]    ${a.json()}[acoes][0][uuid]    ${payload}
    Should Be Equal As Integers    ${r.status_code}    200
    Should Be Equal    ${r.json()}[acoes][0][situacao]    CONCLUIDA
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Coordenacao - Cancelar Sem Motivo - Status Code 400
    [Documentation]    Cancelling needs the reason.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${em}=    Seed Emergencia
    ${a}=    Nova Acao Avulsa    ${em.json()}[uuid]
    ${payload}=    Create Dictionary    situacao=CANCELADA
    ${r}=    POST Situacao Acao    ${em.json()}[uuid]    ${a.json()}[acoes][0][uuid]    ${payload}
    Should Be Equal As Integers    ${r.status_code}    400
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Coordenacao - Mobilizar Profissional - Status Code 201
    [Documentation]    The mobilized professional authorizes the Escala.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${em}=    Seed Emergencia
    ${matricula}=    Seed A Profissional Matricula
    ${destino}=    Seed A Unidade De Saude Uuid
    ${r}=    Mobilizar Profissional    ${em.json()}[uuid]    ${matricula}    ${destino}
    Should Be Equal As Integers    ${r.status_code}    201
    Should Be Equal    ${r.json()}[mobilizacoes][0][situacao]    EM_ANDAMENTO
    Should Be True    ${r.json()}[mobilizacoes][0][autorizaEscala]
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Coordenacao - Ja Mobilizado - Status Code 409
    [Documentation]    The same professional once per destination.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${em}=    Seed Emergencia
    ${matricula}=    Seed A Profissional Matricula
    ${destino}=    Seed A Unidade De Saude Uuid
    Mobilizar Profissional    ${em.json()}[uuid]    ${matricula}    ${destino}
    ${r}=    Mobilizar Profissional    ${em.json()}[uuid]    ${matricula}    ${destino}
    Should Be Equal As Integers    ${r.status_code}    409
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Coordenacao - Turno Por Mobilizacao - Status Code 201
    [Documentation]    Without lotação, the Escala accepts the mobilized professional.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${em}=    Seed Emergencia
    ${matricula}=    Seed A Profissional Matricula
    ${destino}=    Seed A Unidade De Saude Uuid
    ${amanha}=    Evaluate    (datetime.date.today() + datetime.timedelta(days=1)).isoformat()    modules=datetime
    ${turno}=    Create Dictionary    unidadeId=${destino}    profissionalMatricula=${matricula}    tipo=MANHA    data=${amanha}    inicio=07:00    fim=13:00
    ${sem}=    POST Create Turno    ${turno}
    Should Be Equal As Integers    ${sem.status_code}    422
    Mobilizar Profissional    ${em.json()}[uuid]    ${matricula}    ${destino}
    ${r}=    POST Create Turno    ${turno}
    Should Be Equal As Integers    ${r.status_code}    201
    Should Be Equal    ${r.json()}[mobilizacaoEmergencia]    ${em.json()}[nome]
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Coordenacao - Recurso Ausente - Status Code 400
    [Documentation]    A supply needs the resource description.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${em}=    Seed Emergencia
    ${destino}=    Seed A Unidade De Saude Uuid
    ${payload}=    Create Dictionary    tipo=INSUMO    destinoId=${destino}
    ${r}=    POST Mobilizacao    ${em.json()}[uuid]    ${payload}
    Should Be Equal As Integers    ${r.status_code}    400
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Coordenacao - Ativar Capacidade - Status Code 201
    [Documentation]    Activating part of the activatable beds.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${em}=    Seed Emergencia
    ${cap}=    Seed Capacidade
    ${payload}=    Create Dictionary    capacidadeId=${cap}    quantidade=${10}    justificativa=Ala de observação
    ${r}=    POST Ativar Capacidade    ${em.json()}[uuid]    ${payload}
    Should Be Equal As Integers    ${r.status_code}    201
    Should Be Equal As Integers    ${r.json()}[capacidades][0][quantidade]    10
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Coordenacao - Acima Do Ativavel - Status Code 422
    [Documentation]    Not above what remains activatable.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${em}=    Seed Emergencia
    ${cap}=    Seed Capacidade
    ${payload}=    Create Dictionary    capacidadeId=${cap}    quantidade=${31}    justificativa=Demais
    ${r}=    POST Ativar Capacidade    ${em.json()}[uuid]    ${payload}
    Should Be Equal As Integers    ${r.status_code}    422
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Coordenacao - By HTTP Status Code
    [Documentation]    Executes the E3 scenario for the given HTTP status code.
    [Arguments]    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Run Keyword If    '${meuHTTP}' == '201-acao-avulsa'    Coordenacao - Acao Avulsa - Status Code 201
    ...    ELSE IF    '${meuHTTP}' == '201-acoes-do-plano'    Coordenacao - Acoes Do Plano - Status Code 201
    ...    ELSE IF    '${meuHTTP}' == '422-nada-a-gerar'    Coordenacao - Nada A Gerar - Status Code 422
    ...    ELSE IF    '${meuHTTP}' == '200-concluir-acao'    Coordenacao - Concluir Acao - Status Code 200
    ...    ELSE IF    '${meuHTTP}' == '400-cancelar-sem-motivo'    Coordenacao - Cancelar Sem Motivo - Status Code 400
    ...    ELSE IF    '${meuHTTP}' == '201-mobilizar-profissional'    Coordenacao - Mobilizar Profissional - Status Code 201
    ...    ELSE IF    '${meuHTTP}' == '409-ja-mobilizado'    Coordenacao - Ja Mobilizado - Status Code 409
    ...    ELSE IF    '${meuHTTP}' == '201-turno-por-mobilizacao'    Coordenacao - Turno Por Mobilizacao - Status Code 201
    ...    ELSE IF    '${meuHTTP}' == '400-recurso-ausente'    Coordenacao - Recurso Ausente - Status Code 400
    ...    ELSE IF    '${meuHTTP}' == '201-ativar-capacidade'    Coordenacao - Ativar Capacidade - Status Code 201
    ...    ELSE IF    '${meuHTTP}' == '422-acima-do-ativavel'    Coordenacao - Acima Do Ativavel - Status Code 422
    ...    ELSE    Fail    HTTP status code '${meuHTTP}' is not supported for Coordenacao test scenario.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
