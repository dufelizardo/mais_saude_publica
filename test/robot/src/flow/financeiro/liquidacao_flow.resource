*** Settings ***
Library     String
Library     Collections
Library     DateTime
Resource    ../../pom/financeiro/liquidacao_pom.resource
Resource    ./financeiro_flow.resource
Resource    ../transporte/transporte_eletivo_flow.resource
Resource    ./custos_flow.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
    A fila a liquidar, a liquidação, o pagamento com a ordem cronológica, as diárias do TFD e a execução (ADR-0219).
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
Gerar CPF
    [Documentation]    A random CPF with valid check digits.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${cpf}=    Evaluate    (lambda b: b + (lambda d1: str(d1) + str((sum(int(x) * (11 - i) for i, x in enumerate(b + str(d1))) * 10) % 11 % 10))((sum(int(x) * (10 - i) for i, x in enumerate(b)) * 10) % 11 % 10))('9' + ''.join(random.choice('0123456789') for _ in range(8)))    modules=random
    RETURN    ${cpf}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Seed Recebimento Liquidavel
    [Documentation]    A contract, a commitment of 500 tied to an order of 5 units (60) and its definitive receipt; returns the receipt, the commitment and the order number.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${ct}=    Seed Instrumento    CONTRATO
    ${i}=    GET Instrumento    ${ct}
    ${dotacao}=    Seed Dotacao    ${10000}
    ${payload}=    Empenho Payload    ${dotacao}    ${i.json()}[fornecedorId]    ${500}    ${ct}
    ${r}=    POST Empenho    ${payload}
    Should Be Equal As Integers    ${r.status_code}    201
    ${empenho}=    Set Variable    ${r.json()}[uuid]
    ${unidade}=    Seed A Unidade De Saude Uuid
    ${item}=    Create Dictionary    itemInstrumentoId=${i.json()}[itens][0][uuid]    quantidade=${5}
    ${itens}=    Create List    ${item}
    ${ordem}=    Create Dictionary    unidadeId=${unidade}    empenhoId=${empenho}    itens=${itens}
    ${o}=    POST Ordem Fornecimento    ${ct}    ${ordem}
    Should Be Equal As Integers    ${o.status_code}    201
    ${hoje}=    Hoje Em Brasilia
    ${ri}=    Create Dictionary    itemOrdemId=${o.json()}[itens][0][uuid]    quantidade=${5}
    ${ris}=    Create List    ${ri}
    ${payload}=    Create Dictionary    notaFiscal=NF 900    notaEmitidaEm=${hoje}    itens=${ris}
    ${r}=    POST Recebimento Ordem    ${o.json()}[uuid]    ${payload}
    Should Be Equal As Integers    ${r.status_code}    201
    ${rec}=    Set Variable    ${r.json()}[recebimentos][0][uuid]
    ${d}=    Create Dictionary    itemRecebimentoId=${r.json()}[recebimentos][0][itens][0][uuid]    quantidadeAceita=${5}
    ${ds}=    Create List    ${d}
    ${payload}=    Create Dictionary    itens=${ds}
    ${r}=    POST Recebimento Definitivo    ${rec}    ${payload}
    Should Be Equal As Integers    ${r.status_code}    200
    RETURN    ${rec}    ${empenho}    ${o.json()}[numero]
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Liquidacao - Recebimento - Status Code 422 409 E 201
    [Documentation]    The receipt is in the queue; above its value is 422; the settlement with the ISS; again is 409.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${rec}    ${empenho}    ${numero}=    Seed Recebimento Liquidavel
    ${r}=    GET A Liquidar
    ${origens}=    Evaluate    [p['origemId'] for p in $r.json()]
    List Should Contain Value    ${origens}    ${rec}
    ${payload}=    Create Dictionary    origem=RECEBIMENTO    origemId=${rec}    empenhoId=${empenho}    valorBruto=${61}
    ${r}=    POST Liquidacao    ${payload}
    Should Be Equal As Integers    ${r.status_code}    422
    Set To Dictionary    ${payload}    valorBruto=${60}    iss=${3}
    ${r}=    POST Liquidacao    ${payload}
    Should Be Equal As Integers    ${r.status_code}    201
    Should Be Equal As Numbers    ${r.json()}[valorLiquido]    57
    ${r}=    POST Liquidacao    ${payload}
    Should Be Equal As Integers    ${r.status_code}    409
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Liquidacao - Ordem Cronologica - Status Code 422 E 200
    [Documentation]    Paying ahead of an older overdue settlement of the same source asks for the justification.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${dotacao}=    Seed Dotacao    ${1000}
    ${fornecedor}=    Seed Fornecedor Regular
    ${payload}=    Empenho Payload    ${dotacao}    ${fornecedor}    ${300}
    ${r}=    POST Empenho    ${payload}
    ${empenho}=    Set Variable    ${r.json()}[uuid]
    ${quem}=    Seed A Profissional Matricula
    ${antes}=    Get Current Date    UTC    - 5 days    result_format=%Y-%m-%d
    ${hoje}=    Hoje Em Brasilia
    ${centro}=    Centro Geral Ativo
    ${payload}=    Create Dictionary    origem=AVULSA    empenhoId=${empenho}    valorBruto=${40}    vencimento=${antes}    justificativa=Energia.    atestadoPorMatricula=${quem}    centroCustoId=${centro}
    ${r}=    POST Liquidacao    ${payload}
    Should Be Equal As Integers    ${r.status_code}    201
    Set To Dictionary    ${payload}    valorBruto=${60}    vencimento=${hoje}
    ${r}=    POST Liquidacao    ${payload}
    ${nova}=    Set Variable    ${r.json()}[uuid]
    ${pg}=    Create Dictionary    data=${hoje}    ordemBancaria=OB-ROBOT
    ${r}=    POST Pagamento Liquidacao    ${nova}    ${pg}
    Should Be Equal As Integers    ${r.status_code}    422
    Set To Dictionary    ${pg}    justificativaForaDeOrdem=Risco de corte do serviço.
    ${r}=    POST Pagamento Liquidacao    ${nova}    ${pg}
    Should Be Equal As Integers    ${r.status_code}    200
    Should Be True    ${r.json()}[paga]
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Seed Tfd Autorizado
    [Documentation]    A TFD request authorized by the commission; returns the request and the patient CPF.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${unidade}=    Seed A Unidade De Saude Uuid
    ${paciente}    ${cpf}    ${cns}    ${nome}=    Seed A Paciente
    ${medico}=    Seed A Profissional Matricula
    ${tfd}=    Pedido De Transporte    ${paciente}    ${unidade}    TFD    cid=C50    justificativa=Radioterapia sem oferta no município
    ...    profissionalMatricula=${medico}
    ${r}=    POST Transporte Eletivo    solicitacao    ${tfd}
    Should Be Equal As Integers    ${r.status_code}    201
    ${id}=    Set Variable    ${r.json()}[uuid]
    ${ra}=    POST Transporte Eletivo    solicitacao/${id}/autorizacao
    Should Be Equal As Integers    ${ra.status_code}    200
    RETURN    ${id}    ${cpf}    ${nome}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Liquidacao - Diaria Tfd - Status Code 201 E 409
    [Documentation]    The daily allowance to the patient by CPF; the same beneficiary again is 409.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${id}    ${cpfPaciente}    ${nome}=    Seed Tfd Autorizado
    ${cpf}=    Gerar CPF
    ${payload}=    Create Dictionary    solicitacaoId=${id}    beneficiario=PACIENTE    credorCpf=${cpf}    dias=${2}    valorDiaria=${24.75}
    ${r}=    POST Diaria Tfd    ${payload}
    Should Be Equal As Integers    ${r.status_code}    201
    Should Be Equal As Numbers    ${r.json()}[valorTotal]    49.5
    ${r}=    POST Diaria Tfd    ${payload}
    Should Be Equal As Integers    ${r.status_code}    409
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Liquidacao - Execucao E Restos - Status Code 200
    [Documentation]    The execution and the unpaid commitments answer.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${r}=    GET Execucao
    Should Be Equal As Integers    ${r.status_code}    200
    Dictionary Should Contain Key    ${r.json()}    fontes
    Dictionary Should Contain Key    ${r.json()}    aLiquidarQuantidade
    ${r}=    GET Restos A Pagar
    Should Be Equal As Integers    ${r.status_code}    200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Liquidacao - By Cenario
    [Documentation]    Executes the settlement scenario for the given case.
    [Arguments]    ${caso}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    IF    '${caso}' == 'RECEBIMENTO'
        Liquidacao - Recebimento - Status Code 422 409 E 201
    ELSE IF    '${caso}' == 'CRONOLOGICA'
        Liquidacao - Ordem Cronologica - Status Code 422 E 200
    ELSE IF    '${caso}' == 'DIARIA'
        Liquidacao - Diaria Tfd - Status Code 201 E 409
    ELSE IF    '${caso}' == 'EXECUCAO'
        Liquidacao - Execucao E Restos - Status Code 200
    ELSE
        Fail    Caso '${caso}' não suportado no cenário Liquidação.
    END
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

