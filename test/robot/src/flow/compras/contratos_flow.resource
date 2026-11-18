*** Settings ***
Library     String
Library     Collections
Library     DateTime
Resource    ../../pom/compras/contratos_pom.resource
Resource    ./compras_flow.resource
Resource    ../profissional/get_profissional_by_matricula/get_profissional_by_matricula_flow.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
    A contratação, a homologação e os aditivos (ADR-0213).
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
Seed Fornecedor Regular
    [Documentation]    A supplier with the five valid certificates; returns its uuid.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${id}    ${nome}=    Seed Fornecedor
    ${hoje}=    Get Current Date    result_format=%Y-%m-%d
    ${validade}=    Get Current Date    increment=180 days    result_format=%Y-%m-%d
    FOR    ${tipo}    IN    FEDERAL    FGTS    TRABALHISTA    ESTADUAL    MUNICIPAL
        ${payload}=    Create Dictionary    tipo=${tipo}    emitidaEm=${hoje}    validaAte=${validade}
        POST Certidao Fornecedor    ${id}    ${payload}
    END
    RETURN    ${id}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Seed Solicitacao Cotada
    [Documentation]    A purchase request of 50 units quoted at 10, 12 and 14 (reference 12); returns its uuid.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${id}    ${item}=    Seed Solicitacao Aprovada
    Cotar Item    ${item}    10
    Cotar Item    ${item}    12
    Cotar Item    ${item}    14
    ${payload}=    Create Dictionary
    ${r}=    POST Pesquisa Solicitacao Compra    ${id}    ${payload}
    Should Be Equal As Integers    ${r.status_code}    200
    RETURN    ${id}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Abrir Contratacao
    [Documentation]    Opens an electronic auction for the request; returns the response.
    [Arguments]    ${solicitacao}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${sufixo}=    Generate Random String    8    [UPPER][NUMBERS]
    ${ids}=    Create List    ${solicitacao}
    ${payload}=    Create Dictionary    numeroProcesso=Robot ${sufixo}    modalidade=PREGAO_ELETRONICO    objeto=Insumos.    solicitacaoIds=${ids}
    ${r}=    POST Contratacao    ${payload}
    Should Be Equal As Integers    ${r.status_code}    201
    RETURN    ${r}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Instrumento Payload
    [Documentation]    The instrument of the homologation.
    [Arguments]    ${fornecedor}    ${tipo}    ${dias}    ${gestor}    ${fiscal}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${sufixo}=    Generate Random String    8    [UPPER][NUMBERS]
    ${inicio}=    Get Current Date    result_format=%Y-%m-%d
    ${fim}=    Get Current Date    increment=${dias} days    result_format=%Y-%m-%d
    ${i}=    Create Dictionary    fornecedorId=${fornecedor}    tipo=${tipo}    numero=Robot ${sufixo}    inicio=${inicio}    fim=${fim}
    ...    servicoContinuo=${False}    gestorMatricula=${gestor}    fiscalMatricula=${fiscal}
    ${lista}=    Create List    ${i}
    ${payload}=    Create Dictionary    instrumentos=${lista}
    RETURN    ${payload}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Seed Instrumento
    [Documentation]    A ratified procurement with one instrument of the given type (50 units at 12); returns its uuid.
    [Arguments]    ${tipo}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${s}=    Seed Solicitacao Cotada
    ${r}=    Abrir Contratacao    ${s}
    ${forn}=    Seed Fornecedor Regular
    ${payload}=    Create Dictionary    resultado=ADJUDICADO    fornecedorId=${forn}    precoUnitario=${12}
    ${r}=    POST Resultado Item Contratacao    ${r.json()}[itens][0][uuid]    ${payload}
    ${gestor}=    Seed A Profissional Matricula
    ${fiscal}=    Seed A Profissional Matricula
    ${payload}=    Instrumento Payload    ${forn}    ${tipo}    300    ${gestor}    ${fiscal}
    ${r}=    POST Homologacao Contratacao    ${r.json()}[uuid]    ${payload}
    Should Be Equal As Integers    ${r.status_code}    200
    RETURN    ${r.json()}[instrumentos][0][uuid]
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Contratos - Abertura - Status Code 400 E 201
    [Documentation]    Waiver without the legal basis is 400; the auction opens with the summed items.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${s}=    Seed Solicitacao Cotada
    ${ids}=    Create List    ${s}
    ${payload}=    Create Dictionary    numeroProcesso=Robot D-${s}    modalidade=DISPENSA    objeto=Insumos.    solicitacaoIds=${ids}
    ${r}=    POST Contratacao    ${payload}
    Should Be Equal As Integers    ${r.status_code}    400
    ${r}=    Abrir Contratacao    ${s}
    Should Be Equal    ${r.json()}[situacao]    EM_ANDAMENTO
    Should Be Equal As Integers    ${r.json()}[itens][0][quantidade]    50
    Should Be Equal As Numbers    ${r.json()}[itens][0][precoReferencia]    12
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Contratos - Homologacao - Status Code 200
    [Documentation]    Above the reference without justification is 422; the record is ratified and the request contracted.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${s}=    Seed Solicitacao Cotada
    ${r}=    Abrir Contratacao    ${s}
    ${c}=    Set Variable    ${r.json()}[uuid]
    ${item}=    Set Variable    ${r.json()}[itens][0][uuid]
    ${forn}=    Seed Fornecedor Regular
    ${payload}=    Create Dictionary    resultado=ADJUDICADO    fornecedorId=${forn}    precoUnitario=${13}
    ${r}=    POST Resultado Item Contratacao    ${item}    ${payload}
    Should Be Equal As Integers    ${r.status_code}    422
    ${payload}=    Create Dictionary    resultado=ADJUDICADO    fornecedorId=${forn}    precoUnitario=${13}    justificativaPreco=Frete incluído.
    ${r}=    POST Resultado Item Contratacao    ${item}    ${payload}
    Should Be Equal As Integers    ${r.status_code}    200
    ${gestor}=    Seed A Profissional Matricula
    ${fiscal}=    Seed A Profissional Matricula
    ${payload}=    Instrumento Payload    ${forn}    ATA_REGISTRO_PRECOS    300    ${gestor}    ${gestor}
    ${r}=    POST Homologacao Contratacao    ${c}    ${payload}
    Should Be Equal As Integers    ${r.status_code}    422
    ${payload}=    Instrumento Payload    ${forn}    ATA_REGISTRO_PRECOS    300    ${gestor}    ${fiscal}
    ${r}=    POST Homologacao Contratacao    ${c}    ${payload}
    Should Be Equal As Integers    ${r.status_code}    200
    Should Be Equal    ${r.json()}[situacao]    HOMOLOGADA
    ${sol}=    GET On Session    msp_session    ${BASE_URL}/compras/solicitacao/${s}
    Should Be Equal    ${sol.json()}[situacao]    CONTRATADA
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Contratos - Aditivos Do Contrato - Status Code 201
    [Documentation]    Additions up to 25% of the initial value; beyond it, 422.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${ct}=    Seed Instrumento    CONTRATO
    ${r}=    GET Instrumento    ${ct}
    ${item}=    Set Variable    ${r.json()}[itens][0][uuid]
    ${hoje}=    Get Current Date    result_format=%Y-%m-%d
    ${payload}=    Create Dictionary    tipo=ACRESCIMO    itemId=${item}    quantidade=${12}    justificativa=Mais demanda.    assinadoEm=${hoje}
    ${r}=    POST Aditivo Instrumento    ${ct}    ${payload}
    Should Be Equal As Integers    ${r.status_code}    201
    Should Be Equal As Integers    ${r.json()}[itens][0][quantidade]    62
    ${payload}=    Create Dictionary    tipo=ACRESCIMO    itemId=${item}    quantidade=${1}    justificativa=Mais.    assinadoEm=${hoje}
    ${r}=    POST Aditivo Instrumento    ${ct}    ${payload}
    Should Be Equal As Integers    ${r.status_code}    422
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Contratos - Aditivos Da Ata - Status Code 422
    [Documentation]    The record takes no addition and is extended only once.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${ata}=    Seed Instrumento    ATA_REGISTRO_PRECOS
    ${r}=    GET Instrumento    ${ata}
    ${item}=    Set Variable    ${r.json()}[itens][0][uuid]
    ${hoje}=    Get Current Date    result_format=%Y-%m-%d
    ${payload}=    Create Dictionary    tipo=ACRESCIMO    itemId=${item}    quantidade=${5}    justificativa=Mais.    assinadoEm=${hoje}
    ${r}=    POST Aditivo Instrumento    ${ata}    ${payload}
    Should Be Equal As Integers    ${r.status_code}    422
    ${fim}=    Get Current Date    increment=500 days    result_format=%Y-%m-%d
    ${payload}=    Create Dictionary    tipo=PRAZO    novoFim=${fim}    justificativa=Vantajosa.    assinadoEm=${hoje}
    ${r}=    POST Aditivo Instrumento    ${ata}    ${payload}
    Should Be Equal As Integers    ${r.status_code}    201
    ${fim}=    Get Current Date    increment=600 days    result_format=%Y-%m-%d
    ${payload}=    Create Dictionary    tipo=PRAZO    novoFim=${fim}    justificativa=De novo.    assinadoEm=${hoje}
    ${r}=    POST Aditivo Instrumento    ${ata}    ${payload}
    Should Be Equal As Integers    ${r.status_code}    422
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Contratos - By Cenario
    [Documentation]    Executes the contracts scenario for the given case.
    [Arguments]    ${caso}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    IF    '${caso}' == 'ABERTURA'
        Contratos - Abertura - Status Code 400 E 201
    ELSE IF    '${caso}' == 'HOMOLOGACAO'
        Contratos - Homologacao - Status Code 200
    ELSE IF    '${caso}' == 'ADITIVO_CONTRATO'
        Contratos - Aditivos Do Contrato - Status Code 201
    ELSE IF    '${caso}' == 'ADITIVO_ATA'
        Contratos - Aditivos Da Ata - Status Code 422
    ELSE
        Fail    Caso '${caso}' não suportado no cenário Contratos.
    END
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
