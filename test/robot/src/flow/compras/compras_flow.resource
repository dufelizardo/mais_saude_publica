*** Settings ***
Library     String
Library     Collections
Library     DateTime
Resource    ../../pom/compras/compras_pom.resource
Resource    ../../pom/estoque/estoque_pom.resource
Resource    ../estoque/estoque_flow.resource
Resource    ../setor/create_setor/create_setor_flow.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
    O fornecedor, a solicitação de compra e a pesquisa de preços (ADR-0212).
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
Gerar CNPJ
    [Documentation]    A random CNPJ with valid check digits.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${base}=    Generate Random String    12    [NUMBERS]
    ${cnpj}=    Evaluate    (lambda b: (lambda d1: b + str(d1) + str((lambda r: 0 if r < 2 else 11 - r)(sum(int(a) * p for a, p in zip(b + str(d1), [6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2])) % 11)))((lambda r: 0 if r < 2 else 11 - r)(sum(int(a) * p for a, p in zip(b, [5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2])) % 11)))($base)
    RETURN    ${cnpj}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Seed Fornecedor
    [Documentation]    A supplier with a random CNPJ; returns its uuid and name.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${cnpj}=    Gerar CNPJ
    ${sufixo}=    Generate Random String    6    [UPPER]
    ${payload}=    Create Dictionary    documento=${cnpj}    razaoSocial=Robot Fornecedor ${sufixo}
    ${r}=    POST Fornecedor    ${payload}
    Should Be Equal As Integers    ${r.status_code}    201
    RETURN    ${r.json()}[uuid]    ${r.json()}[razaoSocial]
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Seed Solicitacao Compra
    [Documentation]    A purchase request with one material; returns the uuid, the unit and the material.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${unidade}=    Seed A Unidade De Saude Uuid
    ${material}    ${nome}=    Seed Material Estoque    ${False}
    ${item}=    Create Dictionary    tipo=MATERIAL    materialId=${material}    quantidade=${50}
    ${itens}=    Create List    ${item}
    ${payload}=    Create Dictionary    unidadeId=${unidade}    justificativa=Reposição do trimestre.    itens=${itens}
    ${r}=    POST Solicitacao Compra    ${payload}
    Should Be Equal As Integers    ${r.status_code}    201
    RETURN    ${r.json()}[uuid]    ${unidade}    ${material}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Seed Solicitacao Aprovada
    [Documentation]    A purchase request sent and approved; returns the uuid and the item.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${id}    ${unidade}    ${material}=    Seed Solicitacao Compra
    ${r}=    POST Envio Solicitacao Compra    ${id}
    Should Be Equal As Integers    ${r.status_code}    200
    ${payload}=    Create Dictionary    decisao=APROVAR
    ${r}=    POST Analise Solicitacao Compra    ${id}    ${payload}
    Should Be Equal As Integers    ${r.status_code}    200
    RETURN    ${id}    ${r.json()}[itens][0][uuid]
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Cotar Item
    [Documentation]    Registers a quote from a site with the given price.
    [Arguments]    ${item}    ${preco}    ${referencia}=loja.exemplo
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${hoje}=    Get Current Date    result_format=%Y-%m-%d
    ${payload}=    Create Dictionary    fonte=SITE    referencia=${referencia}    precoUnitario=${preco}    cotadoEm=${hoje}
    ${r}=    POST Cotacao Item Compra    ${item}    ${payload}
    Should Be Equal As Integers    ${r.status_code}    201
    RETURN    ${r}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Compras - Fornecedor Documento - Status Code 400 E 409
    [Documentation]    An invalid CNPJ is 400; the same CNPJ twice is 409.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${payload}=    Create Dictionary    documento=11.222.333/0001-00    razaoSocial=Robot Inválido
    ${r}=    POST Fornecedor    ${payload}
    Should Be Equal As Integers    ${r.status_code}    400
    ${cnpj}=    Gerar CNPJ
    ${payload}=    Create Dictionary    documento=${cnpj}    razaoSocial=Robot Duplicado
    ${r}=    POST Fornecedor    ${payload}
    Should Be Equal As Integers    ${r.status_code}    201
    ${r}=    POST Fornecedor    ${payload}
    Should Be Equal As Integers    ${r.status_code}    409
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Compras - Fornecedor Certidoes E Impedimento - Status Code 201
    [Documentation]    Five valid certificates make it regular; an impediment makes it impeded.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${id}    ${nome}=    Seed Fornecedor
    ${hoje}=    Get Current Date    result_format=%Y-%m-%d
    ${validade}=    Get Current Date    increment=90 days    result_format=%Y-%m-%d
    FOR    ${tipo}    IN    FEDERAL    FGTS    TRABALHISTA    ESTADUAL    MUNICIPAL
        ${payload}=    Create Dictionary    tipo=${tipo}    emitidaEm=${hoje}    validaAte=${validade}
        ${r}=    POST Certidao Fornecedor    ${id}    ${payload}
        Should Be Equal As Integers    ${r.status_code}    201
    END
    Should Be True    ${r.json()}[regular]
    ${fim}=    Get Current Date    increment=365 days    result_format=%Y-%m-%d
    ${payload}=    Create Dictionary    tipo=IMPEDIMENTO    inicio=${hoje}    fim=${fim}    motivo=Inexecução parcial.
    ${r}=    POST Sancao Fornecedor    ${id}    ${payload}
    Should Be Equal As Integers    ${r.status_code}    201
    Should Be True    ${r.json()}[impedido]
    Should Not Be True    ${r.json()}[regular]
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Compras - Pesquisa De Precos - Status Code 200
    [Documentation]    Three quotes and the median becomes the reference price.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${id}    ${item}=    Seed Solicitacao Aprovada
    Cotar Item    ${item}    10
    Cotar Item    ${item}    14.5
    ${r}=    Cotar Item    ${item}    12
    Should Be Equal    ${r.json()}[situacao]    EM_COTACAO
    ${payload}=    Create Dictionary
    ${r}=    POST Pesquisa Solicitacao Compra    ${id}    ${payload}
    Should Be Equal As Integers    ${r.status_code}    200
    Should Be Equal    ${r.json()}[situacao]    COTADA
    Should Be Equal As Numbers    ${r.json()}[itens][0][precoReferencia]    12
    Should Be Equal As Numbers    ${r.json()}[valorEstimado]    600
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Compras - Menos De Tres Cotacoes - Status Code 422
    [Documentation]    With two quotes the conclusion asks for the justification.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${id}    ${item}=    Seed Solicitacao Aprovada
    Cotar Item    ${item}    10
    Cotar Item    ${item}    20
    ${payload}=    Create Dictionary
    ${r}=    POST Pesquisa Solicitacao Compra    ${id}    ${payload}
    Should Be Equal As Integers    ${r.status_code}    422
    ${j}=    Create Dictionary    itemId=${item}    justificativa=Só dois fornecedores no mercado local.
    ${lista}=    Create List    ${j}
    ${payload}=    Create Dictionary    justificativas=${lista}
    ${r}=    POST Pesquisa Solicitacao Compra    ${id}    ${payload}
    Should Be Equal As Integers    ${r.status_code}    200
    Should Be Equal As Numbers    ${r.json()}[itens][0][precoReferencia]    15
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Compras - Devolucao - Status Code 400
    [Documentation]    Returning without a reason is 400; with it, the unit edits and sends again.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${id}    ${unidade}    ${material}=    Seed Solicitacao Compra
    POST Envio Solicitacao Compra    ${id}
    ${payload}=    Create Dictionary    decisao=DEVOLVER
    ${r}=    POST Analise Solicitacao Compra    ${id}    ${payload}
    Should Be Equal As Integers    ${r.status_code}    400
    ${payload}=    Create Dictionary    decisao=DEVOLVER    motivo=Detalhe a especificação.
    ${r}=    POST Analise Solicitacao Compra    ${id}    ${payload}
    Should Be Equal    ${r.json()}[situacao]    DEVOLVIDA
    ${item}=    Create Dictionary    tipo=MATERIAL    materialId=${material}    descricao=Tamanho M    quantidade=${80}
    ${itens}=    Create List    ${item}
    ${payload}=    Create Dictionary    unidadeId=${unidade}    justificativa=Reposição do trimestre.    itens=${itens}
    ${r}=    PUT Solicitacao Compra    ${id}    ${payload}
    Should Be Equal As Integers    ${r.status_code}    200
    Should Be Equal As Integers    ${r.json()}[itens][0][quantidade]    80
    ${r}=    POST Envio Solicitacao Compra    ${id}
    Should Be Equal    ${r.json()}[situacao]    ENVIADA
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Compras - Sugestao Pelo Estoque - Status Code 200
    [Documentation]    A material under the minimum comes suggested up to twice the minimum.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${unidade}=    Seed A Unidade De Saude Uuid
    ${material}    ${nome}=    Seed Material Estoque    ${False}
    ${payload}=    Create Dictionary    unidadeId=${unidade}    materialId=${material}    quantidadeMinima=${10}
    ${r}=    PUT Minimo Estoque    ${payload}
    Should Be Equal As Integers    ${r.status_code}    200
    ${payload}=    Create Dictionary    unidadeId=${unidade}    materialId=${material}    quantidade=${3}    origem=DOACAO
    ${r}=    POST Entrada Estoque    ${payload}
    Should Be Equal As Integers    ${r.status_code}    201
    ${r}=    GET Sugestao Compra    ${unidade}
    Should Be Equal As Integers    ${r.status_code}    200
    ${sug}=    Evaluate    [s for s in $r.json() if s["materialId"] == $material]
    Length Should Be    ${sug}    1
    Should Be Equal As Integers    ${sug}[0][quantidadeSugerida]    17
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Compras - By Cenario
    [Documentation]    Executes the purchases scenario for the given case.
    [Arguments]    ${caso}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    IF    '${caso}' == 'DOCUMENTO'
        Compras - Fornecedor Documento - Status Code 400 E 409
    ELSE IF    '${caso}' == 'CERTIDOES'
        Compras - Fornecedor Certidoes E Impedimento - Status Code 201
    ELSE IF    '${caso}' == 'PESQUISA'
        Compras - Pesquisa De Precos - Status Code 200
    ELSE IF    '${caso}' == 'JUSTIFICATIVA'
        Compras - Menos De Tres Cotacoes - Status Code 422
    ELSE IF    '${caso}' == 'DEVOLUCAO'
        Compras - Devolucao - Status Code 400
    ELSE IF    '${caso}' == 'SUGESTAO'
        Compras - Sugestao Pelo Estoque - Status Code 200
    ELSE
        Fail    Caso '${caso}' não suportado no cenário Compras.
    END
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
