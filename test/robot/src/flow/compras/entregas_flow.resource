*** Settings ***
Library     String
Library     Collections
Library     DateTime
Resource    ../../pom/compras/entregas_pom.resource
Resource    ./contratos_flow.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
    A ordem de fornecimento, o recebimento, as ocorrências e o painel (ADR-0214).
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
Seed Ordem
    [Documentation]    A contract (50 units at 12) and an order of 20 for a new unit; returns the order uuid, the order item and the unit.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${ct}=    Seed Instrumento    CONTRATO
    ${r}=    GET Instrumento    ${ct}
    ${unidade}=    Seed A Unidade De Saude Uuid
    ${item}=    Create Dictionary    itemInstrumentoId=${r.json()}[itens][0][uuid]    quantidade=${20}
    ${itens}=    Create List    ${item}
    ${payload}=    Create Dictionary    unidadeId=${unidade}    itens=${itens}
    ${r}=    POST Ordem Fornecimento    ${ct}    ${payload}
    Should Be Equal As Integers    ${r.status_code}    201
    RETURN    ${r.json()}[uuid]    ${r.json()}[itens][0][uuid]    ${unidade}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Entregas - Ordem Dentro Do Saldo - Status Code 422 E 201
    [Documentation]    Above the balance is 422; the order reserves the balance.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${ct}=    Seed Instrumento    CONTRATO
    ${r}=    GET Instrumento    ${ct}
    ${itemId}=    Set Variable    ${r.json()}[itens][0][uuid]
    ${unidade}=    Seed A Unidade De Saude Uuid
    ${item}=    Create Dictionary    itemInstrumentoId=${itemId}    quantidade=${51}
    ${itens}=    Create List    ${item}
    ${payload}=    Create Dictionary    unidadeId=${unidade}    itens=${itens}
    ${r}=    POST Ordem Fornecimento    ${ct}    ${payload}
    Should Be Equal As Integers    ${r.status_code}    422
    ${item}=    Create Dictionary    itemInstrumentoId=${itemId}    quantidade=${30}
    ${itens}=    Create List    ${item}
    ${payload}=    Create Dictionary    unidadeId=${unidade}    empenho=2026NE000001    itens=${itens}
    ${r}=    POST Ordem Fornecimento    ${ct}    ${payload}
    Should Be Equal As Integers    ${r.status_code}    201
    Should Be Equal    ${r.json()}[situacao]    EMITIDA
    ${r}=    GET Instrumento    ${ct}
    Should Be Equal As Integers    ${r.json()}[itens][0][saldo]    20
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Entregas - Recebimento - Status Code 200
    [Documentation]    Provisional and definitive receipt with a refusal: partial order, occurrence and stock entry.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${ordem}    ${item}    ${unidade}=    Seed Ordem
    ${hoje}=    Get Current Date    result_format=%Y-%m-%d
    ${i}=    Create Dictionary    itemOrdemId=${item}    quantidade=${20}
    ${itens}=    Create List    ${i}
    ${payload}=    Create Dictionary    notaFiscal=NF 77    notaEmitidaEm=${hoje}    itens=${itens}
    ${r}=    POST Recebimento Ordem    ${ordem}    ${payload}
    Should Be Equal As Integers    ${r.status_code}    201
    ${rec}=    Set Variable    ${r.json()}[recebimentos][0][uuid]
    ${d}=    Create Dictionary    itemRecebimentoId=${r.json()}[recebimentos][0][itens][0][uuid]    quantidadeAceita=${18}    motivoRecusa=Embalagem violada
    ${itens}=    Create List    ${d}
    ${payload}=    Create Dictionary    itens=${itens}
    ${r}=    POST Recebimento Definitivo    ${rec}    ${payload}
    Should Be Equal As Integers    ${r.status_code}    200
    Should Be Equal    ${r.json()}[situacao]    PARCIAL
    Should Be Equal As Integers    ${r.json()}[itens][0][pendente]    2
    Should Be Equal    ${r.json()}[ocorrencias][0][tipo]    FORA_ESPECIFICACAO
    ${params}=    Create Dictionary    unidadeId=${unidade}
    ${s}=    GET On Session    msp_session    ${BASE_URL}/estoque/saldo    params=${params}
    Should Be Equal As Integers    ${s.json()}[itens][0][total]    18
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Entregas - Ocorrencias - Status Code 400 E 200
    [Documentation]    Delay is not registered by hand; another occurrence is registered and closed.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${ordem}    ${item}    ${unidade}=    Seed Ordem
    ${payload}=    Create Dictionary    tipo=ATRASO    descricao=x
    ${r}=    POST Ocorrencia Ordem    ${ordem}    ${payload}
    Should Be Equal As Integers    ${r.status_code}    400
    ${payload}=    Create Dictionary    tipo=OUTRA    descricao=Entrega sem agendamento.
    ${r}=    POST Ocorrencia Ordem    ${ordem}    ${payload}
    Should Be Equal As Integers    ${r.status_code}    201
    ${payload}=    Create Dictionary    motivo=Fornecedor passou a agendar.
    ${r}=    POST Encerramento Ocorrencia    ${r.json()}[ocorrencias][0][uuid]    ${payload}
    Should Be Equal As Integers    ${r.status_code}    200
    Should Be Equal    ${r.json()}[situacao]    ENCERRADA
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Entregas - Painel - Status Code 200 E 400
    [Documentation]    The dashboard answers; a period with the start after the end is 400.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Seed Ordem
    ${params}=    Create Dictionary
    ${r}=    GET Painel Compras    ${params}
    Should Be Equal As Integers    ${r.status_code}    200
    Should Be True    ${r.json()}[ordensEmAberto] >= 1
    Dictionary Should Contain Key    ${r.json()}    fornecedores
    ${params}=    Create Dictionary    inicio=2026-02-01    fim=2026-01-01
    ${r}=    GET Painel Compras    ${params}
    Should Be Equal As Integers    ${r.status_code}    400
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Entregas - By Cenario
    [Documentation]    Executes the deliveries scenario for the given case.
    [Arguments]    ${caso}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    IF    '${caso}' == 'ORDEM'
        Entregas - Ordem Dentro Do Saldo - Status Code 422 E 201
    ELSE IF    '${caso}' == 'RECEBIMENTO'
        Entregas - Recebimento - Status Code 200
    ELSE IF    '${caso}' == 'OCORRENCIAS'
        Entregas - Ocorrencias - Status Code 400 E 200
    ELSE IF    '${caso}' == 'PAINEL'
        Entregas - Painel - Status Code 200 E 400
    ELSE
        Fail    Caso '${caso}' não suportado no cenário Entregas.
    END
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
