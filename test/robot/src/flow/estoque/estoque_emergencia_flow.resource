*** Settings ***
Library     String
Library     Collections
Resource    ../../pom/estoque/estoque_emergencia_pom.resource
Resource    ../../pom/emergencias/coordenacao_pom.resource
Resource    ../../pom/emergencias/desmobilizacao_pom.resource
Resource    ../emergencias/emergencias_flow.resource
Resource    ./central_estoque_flow.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
    Estoque nas emergências (ADR-0191): a reserva barra as saídas comuns; o insumo pela central vira a requisição urgente que usa a
    reserva; o consumo da emergência abate dela; a desmobilização libera o que restar e o relatório conta o consumo.
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
Reservar
    [Documentation]    Reserves the material at the unit for the emergency and returns the response.
    [Arguments]    ${emergencia}    ${unidade}    ${material}    ${quantidade}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${p}=    Create Dictionary    emergenciaId=${emergencia}    unidadeId=${unidade}    materialId=${material}    quantidade=${quantidade}    justificativa=Kits do Robot
    ${r}=    POST Estoque Emergencia    reserva    ${p}
    RETURN    ${r}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Item Do Saldo
    [Documentation]    The balance item of the material at the unit.
    [Arguments]    ${unidade}    ${material}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${s}=    GET Estoque Emergencia    saldo?unidadeId=${unidade}
    Should Be Equal As Integers    ${s.status_code}    200
    ${item}=    Evaluate    [i for i in $s.json()['itens'] if i['materialId'] == $material][0]
    RETURN    ${item}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Estoque Emergencia - Reserva E Requisicao Urgente
    [Documentation]    The center reserves 60 of 80; a reservation above the free part is 422; the coordination mobilizes 50 from the center, which
    ...    becomes the urgent requisition already sent; served, it consumes the reservation and the mobilization follows the stock.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${central}    ${unidade}    ${material}    ${matricula}=    Seed Central De Abastecimento
    ${em}=    Seed Emergencia
    ${emergencia}=    Set Variable    ${em.json()}[uuid]
    ${r}=    Reservar    ${emergencia}    ${central}    ${material}    ${60}
    Should Be Equal As Integers    ${r.status_code}    201
    ${item}=    Item Do Saldo    ${central}    ${material}
    Should Be Equal As Integers    ${item}[reservado]    60
    Should Be Equal As Integers    ${item}[disponivel]    20
    ${r}=    Reservar    ${emergencia}    ${central}    ${material}    ${21}
    Should Be Equal As Integers    ${r.status_code}    422
    ${abertas}=    GET Estoque Emergencia    emergencias
    ${ids}=    Evaluate    [e['uuid'] for e in $abertas.json()]
    List Should Contain Value    ${ids}    ${emergencia}
    ${mob}=    Create Dictionary    tipo=INSUMO    origemId=${central}    destinoId=${unidade}    materialId=${material}    quantidade=${50}
    ${m}=    POST Mobilizacao    ${emergencia}    ${mob}
    Should Be Equal As Integers    ${m.status_code}    201
    ${linha}=    Evaluate    [x for x in $m.json()['mobilizacoes'] if x.get('materialId') == $material][0]
    Should Be Equal    ${linha}[situacao]    SOLICITADA
    ${req}=    Set Variable    ${linha}[requisicaoEstoqueId]
    ${d}=    GET Estoque Central    requisicao/${req}
    Should Be Equal    ${d.json()}[situacao]    ENVIADA
    Should Be Equal    ${d.json()}[prioridade]    URGENTE
    Should Be Equal    ${d.json()}[emergenciaId]    ${emergencia}
    ${i}=    Create Dictionary    materialId=${material}    quantidade=${50}
    ${itens}=    Create List    ${i}
    ${p}=    Create Dictionary    profissionalMatricula=${matricula}    itens=${itens}
    ${a}=    POST Estoque Central    requisicao/${req}/atendimento    ${p}
    Should Be Equal As Integers    ${a.status_code}    200
    ${depois}=    GET Emergencia    ${emergencia}
    ${linha}=    Evaluate    [x for x in $depois.json()['mobilizacoes'] if x.get('materialId') == $material][0]
    Should Be Equal    ${linha}[situacao]    EM_ANDAMENTO
    ${item}=    Item Do Saldo    ${central}    ${material}
    Should Be Equal As Integers    ${item}[reservado]    10
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Estoque Emergencia - Consumo E Desmobilizacao
    [Documentation]    With 25 of 30 reserved, a common exit of 10 is 422; the emergency consumes 5 from the reservation; the demobilization
    ...    releases the rest and the post-event report counts the consumption.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${unidade}=    Seed A Unidade De Saude Uuid
    ${material}    ${nome}=    Seed Material Estoque
    ${sufixo}=    Generate Random String    6    [UPPER][NUMBERS]
    ${e}=    Entrada De Lote    ${unidade}    ${material}    ${30}    EMG${sufixo}    200
    Should Be Equal As Integers    ${e.status_code}    201
    ${em}=    Seed Emergencia
    ${emergencia}=    Set Variable    ${em.json()}[uuid]
    ${r}=    Reservar    ${emergencia}    ${unidade}    ${material}    ${25}
    Should Be Equal As Integers    ${r.status_code}    201
    ${reserva}=    Set Variable    ${r.json()}[uuid]
    ${comum}=    Create Dictionary    unidadeId=${unidade}    materialId=${material}    quantidade=${10}
    ${s}=    POST Saida Estoque    ${comum}
    Should Be Equal As Integers    ${s.status_code}    422
    ${extra}=    Create Dictionary    unidadeId=${unidade}    materialId=${material}    quantidade=${5}    emergenciaId=${emergencia}
    ${s}=    POST Saida Estoque    ${extra}
    Should Be Equal As Integers    ${s.status_code}    201
    ${d}=    GET Emergencia    ${emergencia}
    Should Be Equal As Integers    ${d.json()}[estoque][quantidadeConsumida]    5
    ${p}=    Create Dictionary    responsavel=Secretário Robot    justificativa=Águas baixando
    ${dm}=    POST Desmobilizacao    ${emergencia}    ${p}
    Should Be Equal As Integers    ${dm.status_code}    200
    ${lista}=    GET Estoque Emergencia    reserva?emergenciaId=${emergencia}
    Should Be Equal    ${lista.json()}[0][situacao]    LIBERADA
    ${rel}=    GET Relatorio Pos Evento    ${emergencia}
    Should Be Equal As Integers    ${rel.json()}[indicadores][insumosConsumidos]    5
    ${motivo}=    Create Dictionary    motivo=De novo
    ${l}=    POST Estoque Emergencia    reserva/${reserva}/liberacao    ${motivo}
    Should Be Equal As Integers    ${l.status_code}    422
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Estoque Emergencia - By Cenario
    [Documentation]    Executes the stock-in-emergencies scenario for the given case.
    [Arguments]    ${caso}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    IF    '${caso}' == 'RESERVA_REQUISICAO'
        Estoque Emergencia - Reserva E Requisicao Urgente
    ELSE IF    '${caso}' == 'CONSUMO_DESMOBILIZACAO'
        Estoque Emergencia - Consumo E Desmobilizacao
    ELSE
        Fail    Caso '${caso}' não suportado no cenário Estoque nas Emergências.
    END
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
