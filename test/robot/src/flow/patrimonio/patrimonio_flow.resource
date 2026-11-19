*** Settings ***
Library     String
Library     Collections
Library     DateTime
Resource    ../../pom/patrimonio/patrimonio_pom.resource
Resource    ../compras/entregas_flow.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
    O bem tombado, o responsável, a transferência e o bem comprado que nasce a tombar (ADR-0215).
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
Bem Payload
    [Documentation]    The payload of a donated asset in the unit, with the responsible.
    [Arguments]    ${unidade}    ${setor}    ${matricula}    ${aquisicao}=${EMPTY}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${hoje}=    Hoje Em Brasilia
    ${data}=    Set Variable If    '${aquisicao}' == '${EMPTY}'    ${hoje}    ${aquisicao}
    ${payload}=    Create Dictionary    descricao=Monitor multiparamétrico    classe=EQUIPAMENTO_MEDICO_HOSPITALAR    unidadeId=${unidade}
    ...    setorId=${setor}    origem=DOACAO    dataAquisicao=${data}    responsavelMatricula=${matricula}
    RETURN    ${payload}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Seed Bem Patrimonial
    [Documentation]    A donated asset in a new unit with a sector; returns the asset, the unit and the responsible.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${setor}    ${codigo}    ${unidade}=    Seed A Setor
    ${matricula}=    Seed A Profissional Matricula
    ${payload}=    Bem Payload    ${unidade}    ${setor}    ${matricula}
    ${r}=    POST Bem Patrimonial    ${payload}
    Should Be Equal As Integers    ${r.status_code}    201
    RETURN    ${r.json()}[bem][uuid]    ${unidade}    ${matricula}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Patrimonio - Cadastro - Status Code 400 E 201
    [Documentation]    A future acquisition is 400; the asset enters tagged and in use.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${setor}    ${codigo}    ${unidade}=    Seed A Setor
    ${matricula}=    Seed A Profissional Matricula
    ${amanha}=    Get Current Date    UTC    + 1 day    result_format=%Y-%m-%d
    ${payload}=    Bem Payload    ${unidade}    ${setor}    ${matricula}    ${amanha}
    ${r}=    POST Bem Patrimonial    ${payload}
    Should Be Equal As Integers    ${r.status_code}    400
    ${payload}=    Bem Payload    ${unidade}    ${setor}    ${matricula}
    ${r}=    POST Bem Patrimonial    ${payload}
    Should Be Equal As Integers    ${r.status_code}    201
    Should Match Regexp    ${r.json()}[bem][numeroTombamento]    ^\\d{4}/\\d{6}$
    Should Be Equal    ${r.json()}[bem][situacao]    EM_USO
    Should Be Equal    ${r.json()}[bem][responsavelMatricula]    ${matricula}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Patrimonio - Responsavel - Status Code 422 E 200
    [Documentation]    The same person is 422; the change closes the previous term.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${bem}    ${unidade}    ${matricula}=    Seed Bem Patrimonial
    ${payload}=    Create Dictionary    responsavelMatricula=${matricula}
    ${r}=    POST Responsavel Bem    ${bem}    ${payload}
    Should Be Equal As Integers    ${r.status_code}    422
    ${outro}=    Seed A Profissional Matricula
    ${payload}=    Create Dictionary    responsavelMatricula=${outro}
    ${r}=    POST Responsavel Bem    ${bem}    ${payload}
    Should Be Equal As Integers    ${r.status_code}    200
    Should Be Equal    ${r.json()}[bem][responsavelMatricula]    ${outro}
    Length Should Be    ${r.json()}[responsaveis]    2
    ${payload}=    Create Dictionary    situacao=BAIXADO
    ${r}=    POST Situacao Bem    ${bem}    ${payload}
    Should Be Equal As Integers    ${r.status_code}    400
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Patrimonio - Transferencia - Status Code 409 E 200
    [Documentation]    One transfer at a time (409); the destination accepts and the asset changes unit.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${bem}    ${unidade}    ${matricula}=    Seed Bem Patrimonial
    ${setor2}    ${codigo}    ${destino}=    Seed A Setor
    ${payload}=    Create Dictionary    destinoId=${destino}    motivo=Remanejamento.
    ${r}=    POST Transferencia Bem    ${bem}    ${payload}
    Should Be Equal As Integers    ${r.status_code}    201
    ${t}=    Set Variable    ${r.json()}[bem][transferenciaPendente][uuid]
    ${r}=    POST Transferencia Bem    ${bem}    ${payload}
    Should Be Equal As Integers    ${r.status_code}    409
    ${novo}=    Seed A Profissional Matricula
    ${payload}=    Create Dictionary    setorId=${setor2}    responsavelMatricula=${novo}
    ${r}=    POST Aceite Transferencia Bem    ${t}    ${payload}
    Should Be Equal As Integers    ${r.status_code}    200
    Should Be Equal    ${r.json()}[bem][unidadeId]    ${destino}
    Should Be Equal    ${r.json()}[bem][responsavelMatricula]    ${novo}
    ${r}=    POST Cancelamento Transferencia Bem    ${t}
    Should Be Equal As Integers    ${r.status_code}    422
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Seed Bem A Tombar
    [Documentation]    A permanent asset bought, delivered and accepted in a new unit; returns the unit. The asset waits to be tagged.
    [Arguments]    ${descricao}=Cadeira odontológica
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${unidade}=    Seed A Unidade De Saude Uuid
    ${item}=    Create Dictionary    tipo=BEM_PERMANENTE    descricao=${descricao}    unidadeMedida=unidade    quantidade=${1}
    ${itens}=    Create List    ${item}
    ${payload}=    Create Dictionary    unidadeId=${unidade}    justificativa=Equipar o consultório.    itens=${itens}
    ${r}=    POST Solicitacao Compra    ${payload}
    Should Be Equal As Integers    ${r.status_code}    201
    ${s}=    Set Variable    ${r.json()}[uuid]
    POST Envio Solicitacao Compra    ${s}
    ${payload}=    Create Dictionary    decisao=APROVAR
    ${r}=    POST Analise Solicitacao Compra    ${s}    ${payload}
    ${i}=    Set Variable    ${r.json()}[itens][0][uuid]
    Cotar Item    ${i}    15000
    Cotar Item    ${i}    15200
    Cotar Item    ${i}    15400
    ${payload}=    Create Dictionary
    ${r}=    POST Pesquisa Solicitacao Compra    ${s}    ${payload}
    Should Be Equal As Integers    ${r.status_code}    200
    ${r}=    Abrir Contratacao    ${s}
    ${forn}=    Seed Fornecedor Regular
    ${payload}=    Create Dictionary    resultado=ADJUDICADO    fornecedorId=${forn}    precoUnitario=${14900}
    ${r}=    POST Resultado Item Contratacao    ${r.json()}[itens][0][uuid]    ${payload}
    ${gestor}=    Seed A Profissional Matricula
    ${fiscal}=    Seed A Profissional Matricula
    ${payload}=    Instrumento Payload    ${forn}    CONTRATO    300    ${gestor}    ${fiscal}
    ${r}=    POST Homologacao Contratacao    ${r.json()}[uuid]    ${payload}
    Should Be Equal As Integers    ${r.status_code}    200
    ${ct}=    Set Variable    ${r.json()}[instrumentos][0][uuid]
    ${r}=    GET Instrumento    ${ct}
    ${oi}=    Create Dictionary    itemInstrumentoId=${r.json()}[itens][0][uuid]    quantidade=${1}
    ${itens}=    Create List    ${oi}
    ${payload}=    Create Dictionary    unidadeId=${unidade}    itens=${itens}
    ${r}=    POST Ordem Fornecimento    ${ct}    ${payload}
    Should Be Equal As Integers    ${r.status_code}    201
    ${hoje}=    Hoje Em Brasilia
    ${ri}=    Create Dictionary    itemOrdemId=${r.json()}[itens][0][uuid]    quantidade=${1}
    ${itens}=    Create List    ${ri}
    ${payload}=    Create Dictionary    notaFiscal=NF 501    notaEmitidaEm=${hoje}    itens=${itens}
    ${r}=    POST Recebimento Ordem    ${r.json()}[uuid]    ${payload}
    Should Be Equal As Integers    ${r.status_code}    201
    ${d}=    Create Dictionary    itemRecebimentoId=${r.json()}[recebimentos][0][itens][0][uuid]    quantidadeAceita=${1}
    ${itens}=    Create List    ${d}
    ${payload}=    Create Dictionary    itens=${itens}
    ${r}=    POST Recebimento Definitivo    ${r.json()}[recebimentos][0][uuid]    ${payload}
    Should Be Equal As Integers    ${r.status_code}    200
    RETURN    ${unidade}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Patrimonio - Bem Comprado A Tombar - Status Code 200
    [Documentation]    The permanent asset accepted in the definitive receipt is born to tag; tagging it gives the number.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${unidade}=    Seed Bem A Tombar
    ${params}=    Create Dictionary    unidadeId=${unidade}    situacao=A_TOMBAR
    ${r}=    GET Bens Patrimoniais    ${params}
    Length Should Be    ${r.json()}    1
    Should Be Equal    ${r.json()}[0][documento]    NF 501
    ${bem}=    Set Variable    ${r.json()}[0][uuid]
    ${resp}=    Seed A Profissional Matricula
    ${payload}=    Create Dictionary    classe=EQUIPAMENTO_MEDICO_HOSPITALAR    numeroSerie=CO-77    responsavelMatricula=${resp}
    ${r}=    POST Tombamento Bem    ${bem}    ${payload}
    Should Be Equal As Integers    ${r.status_code}    200
    Should Be Equal    ${r.json()}[bem][situacao]    EM_USO
    ${r}=    POST Tombamento Bem    ${bem}    ${payload}
    Should Be Equal As Integers    ${r.status_code}    422
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Patrimonio - By Cenario
    [Documentation]    Executes the patrimony scenario for the given case.
    [Arguments]    ${caso}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    IF    '${caso}' == 'CADASTRO'
        Patrimonio - Cadastro - Status Code 400 E 201
    ELSE IF    '${caso}' == 'RESPONSAVEL'
        Patrimonio - Responsavel - Status Code 422 E 200
    ELSE IF    '${caso}' == 'TRANSFERENCIA'
        Patrimonio - Transferencia - Status Code 409 E 200
    ELSE IF    '${caso}' == 'A_TOMBAR'
        Patrimonio - Bem Comprado A Tombar - Status Code 200
    ELSE
        Fail    Caso '${caso}' não suportado no cenário Patrimônio.
    END
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

