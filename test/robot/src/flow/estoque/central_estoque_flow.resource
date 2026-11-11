*** Settings ***
Library     String
Library     Collections
Resource    ../../pom/estoque/central_estoque_pom.resource
Resource    ./estoque_flow.resource
Resource    ../equipamentos/modelo_operacional/modelo_operacional_flow.resource
Resource    ../profissional/get_profissional_by_matricula/get_profissional_by_matricula_flow.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
    Almoxarifado central (ADR-0189): a central é uma unidade semeada com a capacidade CENTRAL_ABASTECIMENTO.
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
Seed Central De Abastecimento
    [Documentation]    A unit with the supply center capability, a requesting unit, a material with two lots at the center and a registration.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${central}=    Seed A Unidade De Saude Uuid
    ${r}=    Incluir Capacidade Por Codigo    ${central}    CENTRAL_ABASTECIMENTO
    Should Be Equal As Integers    ${r.status_code}    200
    ${unidade}=    Seed A Unidade De Saude Uuid
    ${material}    ${nome}=    Seed Material Estoque
    ${sufixo}=    Generate Random String    6    [UPPER][NUMBERS]
    ${e1}=    Entrada De Lote    ${central}    ${material}    ${30}    CEDO${sufixo}    30
    Should Be Equal As Integers    ${e1.status_code}    201
    ${e2}=    Entrada De Lote    ${central}    ${material}    ${50}    TARDE${sufixo}    300
    Should Be Equal As Integers    ${e2.status_code}    201
    ${matricula}=    Seed A Profissional Matricula
    RETURN    ${central}    ${unidade}    ${material}    ${matricula}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Requisicao Enviada
    [Documentation]    Creates and sends a requisition of the unit to the center; returns its uuid.
    [Arguments]    ${central}    ${unidade}    ${material}    ${matricula}    ${quantidade}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${item}=    Create Dictionary    materialId=${material}    quantidade=${quantidade}
    ${itens}=    Create List    ${item}
    ${payload}=    Create Dictionary    profissionalMatricula=${matricula}    unidadeId=${unidade}    centralId=${central}    prioridade=URGENTE    itens=${itens}
    ${r}=    POST Estoque Central    requisicao    ${payload}
    Should Be Equal As Integers    ${r.status_code}    201
    ${id}=    Set Variable    ${r.json()}[uuid]
    ${quem}=    Create Dictionary    profissionalMatricula=${matricula}
    ${e}=    POST Estoque Central    requisicao/${id}/envio    ${quem}
    Should Be Equal As Integers    ${e.status_code}    200
    RETURN    ${id}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Central - Requisicao Atendida E Recebida
    [Documentation]    The center serves 40 by the lots that expire first; the unit receives with a shortfall that goes back.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${central}    ${unidade}    ${material}    ${matricula}=    Seed Central De Abastecimento
    ${id}=    Requisicao Enviada    ${central}    ${unidade}    ${material}    ${matricula}    ${40}
    ${d}=    GET Estoque Central    requisicao/${id}
    Should Be Equal As Integers    ${d.json()}[itens][0][saldoNaCentral]    80
    ${demais}=    Create Dictionary    materialId=${material}    quantidade=${41}
    ${l1}=    Create List    ${demais}
    ${p1}=    Create Dictionary    profissionalMatricula=${matricula}    itens=${l1}
    ${r}=    POST Estoque Central    requisicao/${id}/atendimento    ${p1}
    Should Be Equal As Integers    ${r.status_code}    400
    ${ok}=    Create Dictionary    materialId=${material}    quantidade=${40}
    ${l2}=    Create List    ${ok}
    ${p2}=    Create Dictionary    profissionalMatricula=${matricula}    itens=${l2}
    ${r}=    POST Estoque Central    requisicao/${id}/atendimento    ${p2}
    Should Be Equal As Integers    ${r.status_code}    200
    Should Be Equal    ${r.json()}[situacao]    ATENDIDA
    ${t}=    GET Estoque Central    transferencia/${r.json()}[transferenciaId]
    Length Should Be    ${t.json()}[itens]    2
    ${recebidos}=    Create List
    FOR    ${i}    IN    @{t.json()}[itens]
        ${chegou}=    Evaluate    $i['quantidadeEnviada'] - (2 if $i['quantidadeEnviada'] == 30 else 0)
        ${item}=    Create Dictionary    itemId=${i}[uuid]    quantidade=${chegou}
        IF    ${chegou} < ${i}[quantidadeEnviada]    Set To Dictionary    ${item}    motivoDivergencia=FALTA
        Append To List    ${recebidos}    ${item}
    END
    ${p3}=    Create Dictionary    profissionalMatricula=${matricula}    itens=${recebidos}
    ${rec}=    POST Estoque Central    transferencia/${t.json()}[uuid]/recebimento    ${p3}
    Should Be Equal As Integers    ${rec.status_code}    200
    Should Be True    ${rec.json()}[comDivergencia]
    ${depois}=    GET Estoque Central    requisicao/${id}
    Should Be Equal    ${depois.json()}[situacao]    RECEBIDA
    Should Be Equal As Integers    ${depois.json()}[itens][0][saldoNaUnidade]    38
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Central - Negar E Unidade Nao Central
    [Documentation]    Asking a unit that is not a supply center gives 422; the center denies with a reason (400 without it).
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${central}    ${unidade}    ${material}    ${matricula}=    Seed Central De Abastecimento
    ${item}=    Create Dictionary    materialId=${material}    quantidade=${5}
    ${itens}=    Create List    ${item}
    ${errada}=    Create Dictionary    profissionalMatricula=${matricula}    unidadeId=${central}    centralId=${unidade}    itens=${itens}
    ${r}=    POST Estoque Central    requisicao    ${errada}
    Should Be Equal As Integers    ${r.status_code}    422
    ${id}=    Requisicao Enviada    ${central}    ${unidade}    ${material}    ${matricula}    ${5}
    ${sem}=    Create Dictionary    profissionalMatricula=${matricula}
    ${r}=    POST Estoque Central    requisicao/${id}/negacao    ${sem}
    Should Be Equal As Integers    ${r.status_code}    400
    ${com}=    Create Dictionary    profissionalMatricula=${matricula}    motivo=Material em falta na central.
    ${r}=    POST Estoque Central    requisicao/${id}/negacao    ${com}
    Should Be Equal As Integers    ${r.status_code}    200
    Should Be Equal    ${r.json()}[situacao]    NEGADA
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Central - Inventario Bloqueia E Fecha
    [Documentation]    The open inventory blocks the movements (422) and a second one (409); closing needs every lot counted.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${central}    ${unidade}    ${material}    ${matricula}=    Seed Central De Abastecimento
    ${p}=    Create Dictionary    profissionalMatricula=${matricula}    unidadeId=${central}
    ${inv}=    POST Estoque Central    inventario    ${p}
    Should Be Equal As Integers    ${inv.status_code}    201
    ${dup}=    POST Estoque Central    inventario    ${p}
    Should Be Equal As Integers    ${dup.status_code}    409
    ${saida}=    Create Dictionary    unidadeId=${central}    materialId=${material}    quantidade=${1}
    ${s}=    POST Saida Estoque    ${saida}
    Should Be Equal As Integers    ${s.status_code}    422
    ${id}=    Set Variable    ${inv.json()}[uuid]
    ${quem}=    Create Dictionary    profissionalMatricula=${matricula}
    ${f}=    POST Estoque Central    inventario/${id}/fechamento    ${quem}
    Should Be Equal As Integers    ${f.status_code}    422
    ${contagens}=    Create List
    FOR    ${i}    IN    @{inv.json()}[itens]
        ${c}=    Create Dictionary    itemId=${i}[uuid]    quantidade=${i}[saldoSistema]
        Append To List    ${contagens}    ${c}
    END
    Set To Dictionary    ${contagens}[0]    quantidade=${25}
    ${pc}=    Create Dictionary    profissionalMatricula=${matricula}    itens=${contagens}
    ${c}=    POST Estoque Central    inventario/${id}/contagem    ${pc}
    Should Be Equal As Integers    ${c.status_code}    200
    Should Be Equal As Integers    ${c.json()}[comDiferenca]    1
    ${f}=    POST Estoque Central    inventario/${id}/fechamento    ${quem}
    Should Be Equal As Integers    ${f.status_code}    200
    Should Be Equal    ${f.json()}[situacao]    FECHADO
    ${s}=    POST Saida Estoque    ${saida}
    Should Be Equal As Integers    ${s.status_code}    201
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Central - By Cenario
    [Documentation]    Executes the Central Estoque scenario for the given case.
    [Arguments]    ${caso}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    IF    '${caso}' == 'ATENDIDA_RECEBIDA'
        Central - Requisicao Atendida E Recebida
    ELSE IF    '${caso}' == 'NEGAR'
        Central - Negar E Unidade Nao Central
    ELSE IF    '${caso}' == 'INVENTARIO'
        Central - Inventario Bloqueia E Fecha
    ELSE
        Fail    Caso '${caso}' não suportado no cenário Central Estoque.
    END
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
