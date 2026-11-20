*** Settings ***
Library     String
Library     Collections
Library     DateTime
Resource    ../../pom/financeiro/financeiro_pom.resource
Resource    ../compras/entregas_flow.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
    A dotação, as alterações, o empenho e a ordem de fornecimento com o empenho (ADR-0218).
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
Dotacao Payload
    [Documentation]    A fresh allocation of the current year with the given value.
    [Arguments]    ${valor}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${ano}=    Get Current Date    UTC    - 3 hours    result_format=%Y
    ${exercicio}=    Convert To Integer    ${ano}
    ${acao}=    Generate Random String    4    [NUMBERS]
    ${payload}=    Create Dictionary    exercicio=${exercicio}    programaTrabalho=10.301.0010.${acao}    descricao=Robot Atenção básica ${acao}
    ...    naturezaDespesa=3.3.90.30    fonte=BLOCO_MANUTENCAO    valorInicial=${valor}
    RETURN    ${payload}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Seed Dotacao
    [Documentation]    A fresh allocation of the current year; returns its uuid.
    [Arguments]    ${valor}=${1000}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${payload}=    Dotacao Payload    ${valor}
    ${r}=    POST Dotacao    ${payload}
    Should Be Equal As Integers    ${r.status_code}    201
    RETURN    ${r.json()}[uuid]
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Empenho Payload
    [Documentation]    A commitment payload with a random number.
    [Arguments]    ${dotacao}    ${fornecedor}    ${valor}    ${instrumento}=${None}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${hoje}=    Hoje Em Brasilia
    ${n}=    Generate Random String    6    [NUMBERS]
    ${payload}=    Create Dictionary    numero=RB${n}    tipo=GLOBAL    dotacaoId=${dotacao}    fornecedorId=${fornecedor}    instrumentoId=${instrumento}
    ...    valor=${valor}    data=${hoje}
    RETURN    ${payload}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Financeiro - Dotacao - Status Code 400 409 E 201
    [Documentation]    An invalid year is 400; the allocation is registered; the same one again is 409.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${payload}=    Dotacao Payload    ${1000}
    Set To Dictionary    ${payload}    exercicio=${2000}
    ${r}=    POST Dotacao    ${payload}
    Should Be Equal As Integers    ${r.status_code}    400
    ${payload}=    Dotacao Payload    ${1000}
    ${r}=    POST Dotacao    ${payload}
    Should Be Equal As Integers    ${r.status_code}    201
    Should Be Equal As Numbers    ${r.json()}[disponivel]    1000
    ${r}=    POST Dotacao    ${payload}
    Should Be Equal As Integers    ${r.status_code}    409
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Financeiro - Alteracao - Status Code 201 E 422
    [Documentation]    The supplement raises the authorized value; cancelling more than available is 422.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${id}=    Seed Dotacao    ${1000}
    ${hoje}=    Hoje Em Brasilia
    ${payload}=    Create Dictionary    tipo=SUPLEMENTACAO    valor=${500}    ato=Decreto 1/2026    data=${hoje}
    ${r}=    POST Alteracao Dotacao    ${id}    ${payload}
    Should Be Equal As Integers    ${r.status_code}    201
    Should Be Equal As Numbers    ${r.json()}[autorizado]    1500
    ${payload}=    Create Dictionary    tipo=ANULACAO    valor=${2000}    ato=Decreto 2/2026    data=${hoje}
    ${r}=    POST Alteracao Dotacao    ${id}    ${payload}
    Should Be Equal As Integers    ${r.status_code}    422
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Financeiro - Empenho - Status Code 422 409 E 201
    [Documentation]    Above the available is 422; the commitment is registered; the same number is 409.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${dotacao}=    Seed Dotacao    ${1000}
    ${fornecedor}=    Seed Fornecedor Regular
    ${payload}=    Empenho Payload    ${dotacao}    ${fornecedor}    ${1500}
    ${r}=    POST Empenho    ${payload}
    Should Be Equal As Integers    ${r.status_code}    422
    Set To Dictionary    ${payload}    valor=${400}
    ${r}=    POST Empenho    ${payload}
    Should Be Equal As Integers    ${r.status_code}    201
    Should Be Equal As Numbers    ${r.json()}[saldo]    400
    ${r}=    POST Empenho    ${payload}
    Should Be Equal As Integers    ${r.status_code}    409
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Financeiro - Ordem Com Empenho - Status Code 422 E 201
    [Documentation]    The supply order consumes the commitment balance; above it is 422.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${ct}=    Seed Instrumento    CONTRATO
    ${i}=    GET Instrumento    ${ct}
    ${dotacao}=    Seed Dotacao    ${1000}
    ${payload}=    Empenho Payload    ${dotacao}    ${i.json()}[fornecedorId]    ${100}    ${ct}
    ${r}=    POST Empenho    ${payload}
    Should Be Equal As Integers    ${r.status_code}    201
    ${empenho}=    Set Variable    ${r.json()}[uuid]
    ${unidade}=    Seed A Unidade De Saude Uuid
    ${item}=    Create Dictionary    itemInstrumentoId=${i.json()}[itens][0][uuid]    quantidade=${20}
    ${itens}=    Create List    ${item}
    ${ordem}=    Create Dictionary    unidadeId=${unidade}    empenhoId=${empenho}    itens=${itens}
    ${r}=    POST Ordem Fornecimento    ${ct}    ${ordem}
    Should Be Equal As Integers    ${r.status_code}    422
    Set To Dictionary    ${item}    quantidade=${5}
    ${r}=    POST Ordem Fornecimento    ${ct}    ${ordem}
    Should Be Equal As Integers    ${r.status_code}    201
    Should Be Equal    ${r.json()}[empenhoId]    ${empenho}
    ${e}=    GET Empenho    ${empenho}
    Should Be Equal As Numbers    ${e.json()}[saldo]    40
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Financeiro - By Cenario
    [Documentation]    Executes the financial scenario for the given case.
    [Arguments]    ${caso}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    IF    '${caso}' == 'DOTACAO'
        Financeiro - Dotacao - Status Code 400 409 E 201
    ELSE IF    '${caso}' == 'ALTERACAO'
        Financeiro - Alteracao - Status Code 201 E 422
    ELSE IF    '${caso}' == 'EMPENHO'
        Financeiro - Empenho - Status Code 422 409 E 201
    ELSE IF    '${caso}' == 'ORDEM'
        Financeiro - Ordem Com Empenho - Status Code 422 E 201
    ELSE
        Fail    Caso '${caso}' não suportado no cenário Financeiro.
    END
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

