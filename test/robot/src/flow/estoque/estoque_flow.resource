*** Settings ***
Library     String
Library     Collections
Library     DateTime
Resource    ../../pom/estoque/estoque_pom.resource
Resource    ../setor/create_setor/create_setor_flow.resource
Resource    ../common/schema_validation_flow.resource
Resource    ../../resource/config/schema/schema.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
Seed Material Estoque
    [Documentation]    Creates a material with a random code and returns its uuid and name.
    [Arguments]    ${controlaLote}=${True}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${sufixo}=    Generate Random String    8    [UPPER][NUMBERS]
    ${payload}=    Create Dictionary    codigo=RB-${sufixo}    nome=Robot Material ${sufixo}    categoria=MEDICO_HOSPITALAR    unidadeMedida=UNIDADE    controlaLote=${controlaLote}
    ${r}=    POST Material Estoque    ${payload}
    Should Be Equal As Integers    ${r.status_code}    201
    RETURN    ${r.json()}[uuid]    ${r.json()}[nome]
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Entrada De Lote
    [Documentation]    Registers a donation entry of a lot and returns the response.
    [Arguments]    ${unidade}    ${material}    ${quantidade}    ${lote}    ${dias}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${validade}=    Get Current Date    increment=${dias} days    result_format=%Y-%m-%d
    ${payload}=    Create Dictionary    unidadeId=${unidade}    materialId=${material}    quantidade=${quantidade}    origem=DOACAO    numeroLote=${lote}    validade=${validade}
    ${r}=    POST Entrada Estoque    ${payload}
    RETURN    ${r}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Estoque - Catalogo - Status Code 201
    [Documentation]    Creates a material; the same code conflicts.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${uuid}    ${nome}=    Seed Material Estoque
    ${sufixo}=    Generate Random String    8    [UPPER][NUMBERS]
    ${repetido}=    Create Dictionary    codigo=RB-REP-${sufixo}    nome=Robot Repetido A ${sufixo}    categoria=EPI    unidadeMedida=CAIXA
    ${r1}=    POST Material Estoque    ${repetido}
    Should Be Equal As Integers    ${r1.status_code}    201
    Validate Object Schema    ${r1.json()}    ${MATERIAL_ESTOQUE_SCHEMA}
    Set To Dictionary    ${repetido}    nome=Robot Repetido B ${sufixo}
    ${r2}=    POST Material Estoque    ${repetido}
    Should Be Equal As Integers    ${r2.status_code}    409
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Estoque - Entrada - Status Code 400
    [Documentation]    A material with lot needs lot and expiry; an invoice needs its number.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${unidade}=    Seed A Unidade De Saude Uuid
    ${material}    ${nome}=    Seed Material Estoque
    ${sem}=    Create Dictionary    unidadeId=${unidade}    materialId=${material}    quantidade=${10}    origem=DOACAO
    ${r1}=    POST Entrada Estoque    ${sem}
    Should Be Equal As Integers    ${r1.status_code}    400
    ${validade}=    Get Current Date    increment=100 days    result_format=%Y-%m-%d
    ${nf}=    Create Dictionary    unidadeId=${unidade}    materialId=${material}    quantidade=${10}    origem=NOTA_FISCAL    numeroLote=L1    validade=${validade}
    ${r2}=    POST Entrada Estoque    ${nf}
    Should Be Equal As Integers    ${r2.status_code}    400
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Estoque - Saida Pelos Que Vencem Primeiro - Status Code 201
    [Documentation]    The exit takes the lot that expires first, then the next.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${unidade}=    Seed A Unidade De Saude Uuid
    ${material}    ${nome}=    Seed Material Estoque
    ${r1}=    Entrada De Lote    ${unidade}    ${material}    ${5}    TARDE    200
    Should Be Equal As Integers    ${r1.status_code}    201
    Validate Object Schema    ${r1.json()}    ${MOVIMENTACAO_ESTOQUE_SCHEMA}
    ${r2}=    Entrada De Lote    ${unidade}    ${material}    ${4}    CEDO    10
    Should Be Equal As Integers    ${r2.status_code}    201
    ${saida}=    Create Dictionary    unidadeId=${unidade}    materialId=${material}    quantidade=${6}
    ${r}=    POST Saida Estoque    ${saida}
    Should Be Equal As Integers    ${r.status_code}    201
    ${lotes}=    Evaluate    [m['numeroLote'] for m in $r.json()]
    Should Be Equal    ${lotes}    ${{['CEDO', 'TARDE']}}
    Set To Dictionary    ${saida}    quantidade=${10}
    ${r422}=    POST Saida Estoque    ${saida}
    Should Be Equal As Integers    ${r422.status_code}    422
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Estoque - Perda E Ajuste - Status Code 201
    [Documentation]    A loss larger than the lot is refused; the adjustment records the difference.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${unidade}=    Seed A Unidade De Saude Uuid
    ${material}    ${nome}=    Seed Material Estoque
    ${r1}=    Entrada De Lote    ${unidade}    ${material}    ${8}    P1    60
    ${lote}=    Set Variable    ${r1.json()}[loteId]
    ${grande}=    Create Dictionary    loteId=${lote}    quantidade=${9}    motivo=AVARIA
    ${r422}=    POST Perda Estoque    ${grande}
    Should Be Equal As Integers    ${r422.status_code}    422
    ${perda}=    Create Dictionary    loteId=${lote}    quantidade=${2}    motivo=AVARIA
    ${rp}=    POST Perda Estoque    ${perda}
    Should Be Equal As Integers    ${rp.status_code}    201
    ${ajuste}=    Create Dictionary    loteId=${lote}    quantidadeContada=${5}    justificativa=Contagem do inventário.
    ${ra}=    POST Ajuste Estoque    ${ajuste}
    Should Be Equal As Integers    ${ra.status_code}    201
    Should Be Equal As Integers    ${ra.json()}[quantidade]    -1
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Estoque - Saldo E Alertas - Status Code 200
    [Documentation]    Minimum and expiry alerts in the unit stock.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${unidade}=    Seed A Unidade De Saude Uuid
    ${material}    ${nome}=    Seed Material Estoque
    ${r1}=    Entrada De Lote    ${unidade}    ${material}    ${3}    V1    20
    ${minimo}=    Create Dictionary    unidadeId=${unidade}    materialId=${material}    quantidadeMinima=${10}
    ${rm}=    PUT Minimo Estoque    ${minimo}
    Should Be Equal As Integers    ${rm.status_code}    200
    ${params}=    Create Dictionary    unidadeId=${unidade}
    ${r}=    GET Saldo Estoque    ${params}
    Should Be Equal As Integers    ${r.status_code}    200
    Validate Object Schema    ${r.json()}    ${SALDO_ESTOQUE_SCHEMA}
    Should Be Equal As Integers    ${r.json()}[abaixoDoMinimo]    1
    Should Be Equal As Integers    ${r.json()}[vencendo]    1
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Estoque - By HTTP Status Code
    [Documentation]    Dispatches the scenario by HTTP status code.
    [Arguments]    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Run Keyword If    '${meuHTTP}' == '201-catalogo'    Estoque - Catalogo - Status Code 201
    Run Keyword If    '${meuHTTP}' == '400-entrada'    Estoque - Entrada - Status Code 400
    Run Keyword If    '${meuHTTP}' == '201-saida'    Estoque - Saida Pelos Que Vencem Primeiro - Status Code 201
    Run Keyword If    '${meuHTTP}' == '201-perda'    Estoque - Perda E Ajuste - Status Code 201
    Run Keyword If    '${meuHTTP}' == '200-saldo'    Estoque - Saldo E Alertas - Status Code 200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
