*** Settings ***
Library     String
Library     Collections
Library     DateTime
Resource    ../../pom/imunizacao/sala_de_vacina_pom.resource
Resource    ../../pom/imunizacao/vacinacao_pom.resource
Resource    vacinacao_flow.resource
Resource    ../equipamentos/modelo_operacional/modelo_operacional_flow.resource
Resource    ../common/schema_validation_flow.resource
Resource    ../../resource/config/schema/schema.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
    A unidade nova ganha a capacidade "Sala de vacina" pelo modelo operacional; a dose de dT (multidose) sai do estoque.
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
Seed Sala De Vacina
    [Documentation]    Seeds the I1 data and gives the unit the vaccination room capability; returns the dictionary.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${dados}=    Seed Vacinacao
    ${r}=    Incluir Capacidade Por Codigo    ${dados}[unidade]    SALA_VACINA
    Should Be True    ${r.status_code} in (200, 201)
    RETURN    ${dados}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Entrada De Lote
    [Documentation]    Receives 10 doses of a batch into the unit and returns the response.
    [Arguments]    ${unidade}    ${codigo}    ${camara}=${EMPTY}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${imuno}=    Id Do Imunobiologico    ${codigo}
    ${validade}=    Get Current Date    increment=90 days    result_format=%Y-%m-%d
    ${numero}=    Generate Random String    6    [UPPER][NUMBERS]
    ${payload}=    Create Dictionary    unidadeId=${unidade}    imunobiologicoId=${imuno}    numeroLote=RB${numero}    validade=${validade}    doses=${10}
    Run Keyword If    '${camara}' != '${EMPTY}'    Set To Dictionary    ${payload}    camaraId=${camara}
    ${r}=    POST Entrada De Lote    ${payload}
    RETURN    ${r}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Dose Do Estoque
    [Documentation]    An applied dT booster from the stock batch.
    [Arguments]    ${dados}    ${lote}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${payload}=    Dose Aplicada    ${dados}    DT    REFORCO
    Remove From Dictionary    ${payload}    lote
    Set To Dictionary    ${payload}    loteEstoqueId=${lote}
    RETURN    ${payload}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Saldo Do Lote
    [Documentation]    Returns the current doses of the batch in the stock.
    [Arguments]    ${unidade}    ${lote}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${params}=    Create Dictionary    unidadeId=${unidade}
    ${r}=    GET Estoque Sala De Vacina    ${params}
    ${d}=    Evaluate    [l["doses"] for l in $r.json()["lotes"] if l["uuid"] == $lote]
    RETURN    ${d}[0]
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Camara Com Excursao
    [Documentation]    Creates a chamber with a dT batch and a reading out of range; returns the chamber and the batch.
    [Arguments]    ${dados}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${payload}=    Create Dictionary    unidadeId=${dados}[unidade]    nome=Câmara Robot
    ${c}=    POST Camara Refrigeracao    ${payload}
    Should Be Equal As Integers    ${c.status_code}    201
    ${l}=    Entrada De Lote    ${dados}[unidade]    DT    ${c.json()}[uuid]
    ${leitura}=    Create Dictionary    temperaturaAtual=${6}    temperaturaMinima=${4}    temperaturaMaxima=${10.5}    observacao=Porta aberta
    ${r}=    POST Leitura Temperatura    ${c.json()}[uuid]    ${leitura}
    Should Be Equal As Integers    ${r.status_code}    201
    Validate Object Schema    ${r.json()}    ${LEITURA_TEMPERATURA_SCHEMA}
    Should Be True    ${r.json()}[excursao]
    Should Be Equal As Integers    ${r.json()}[lotesPostosSobSuspeita]    1
    RETURN    ${c.json()}[uuid]    ${l.json()}[uuid]
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Sala De Vacina - Entrada De Lote - Status Code 201
    [Documentation]    A batch enters the stock of the unit with the vaccination room, with the schema.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${dados}=    Seed Sala De Vacina
    ${r}=    Entrada De Lote    ${dados}[unidade]    DT
    Should Be Equal As Integers    ${r.status_code}    201
    Validate Object Schema    ${r.json()}    ${LOTE_IMUNOBIOLOGICO_SCHEMA}
    Should Be Equal As Integers    ${r.json()}[doses]    10
    Should Be True    ${r.json()}[aplicavel]
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Sala De Vacina - Entrada Sem Sala - Status Code 422
    [Documentation]    A unit without the capability does not control stock.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${dados}=    Seed Vacinacao
    ${r}=    Entrada De Lote    ${dados}[unidade]    DT
    Should Be Equal As Integers    ${r.status_code}    422
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Sala De Vacina - Dose Com Baixa - Status Code 201
    [Documentation]    The applied dose leaves the stock and opens the multidose vial.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${dados}=    Seed Sala De Vacina
    ${l}=    Entrada De Lote    ${dados}[unidade]    DT
    ${payload}=    Dose Do Estoque    ${dados}    ${l.json()}[uuid]
    ${r}=    POST Registro Vacinacao    ${payload}
    Should Be Equal As Integers    ${r.status_code}    201
    Validate Object Schema    ${r.json()}    ${REGISTRO_VACINACAO_SCHEMA}
    Should Be Equal    ${r.json()}[lote]    ${l.json()}[numeroLote]
    Should Not Be Equal    ${r.json()}[frascoId]    ${None}
    ${saldo}=    Saldo Do Lote    ${dados}[unidade]    ${l.json()}[uuid]
    Should Be Equal As Integers    ${saldo}    9
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Sala De Vacina - Dose Sem Lote Do Estoque - Status Code 400
    [Documentation]    In the unit with the vaccination room, the batch in text is not enough.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${dados}=    Seed Sala De Vacina
    ${payload}=    Dose Aplicada    ${dados}    DT    REFORCO
    ${r}=    POST Registro Vacinacao    ${payload}
    Should Be Equal As Integers    ${r.status_code}    400
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Sala De Vacina - Estoque - Status Code 200
    [Documentation]    The stock panel with the open vial, with the schema.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${dados}=    Seed Sala De Vacina
    ${l}=    Entrada De Lote    ${dados}[unidade]    DT
    ${payload}=    Dose Do Estoque    ${dados}    ${l.json()}[uuid]
    POST Registro Vacinacao    ${payload}
    ${params}=    Create Dictionary    unidadeId=${dados}[unidade]
    ${r}=    GET Estoque Sala De Vacina    ${params}
    Should Be Equal As Integers    ${r.status_code}    200
    Validate Object Schema    ${r.json()}    ${ESTOQUE_SALA_VACINA_SCHEMA}
    Should Be True    ${r.json()}[temSalaDeVacina]
    Length Should Be    ${r.json()}[frascos]    1
    Should Be Equal As Integers    ${r.json()}[frascos][0][dosesRestantes]    9
    Should Be Equal As Integers    ${r.json()}[aplicadasMes]    1
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Sala De Vacina - Anulacao Estorna - Status Code 200
    [Documentation]    Voiding the dose returns it to the batch, with the ledger schema.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${dados}=    Seed Sala De Vacina
    ${l}=    Entrada De Lote    ${dados}[unidade]    DT
    ${payload}=    Dose Do Estoque    ${dados}    ${l.json()}[uuid]
    ${r}=    POST Registro Vacinacao    ${payload}
    ${motivo}=    Create Dictionary    motivo=Paciente errado
    ${a}=    POST Anulacao Vacinacao    ${r.json()}[uuid]    ${motivo}
    Should Be Equal As Integers    ${a.status_code}    200
    ${saldo}=    Saldo Do Lote    ${dados}[unidade]    ${l.json()}[uuid]
    Should Be Equal As Integers    ${saldo}    10
    ${m}=    GET Movimentacoes Do Lote    ${l.json()}[uuid]
    Should Be Equal As Integers    ${m.status_code}    200
    Validate Array Schema    ${m.json()}    ${MOVIMENTACAO_IMUNOBIOLOGICO_SCHEMA}
    ${tipos}=    Evaluate    [x["tipo"] for x in $m.json()]
    ${esperado}=    Create List    ENTRADA    APLICACAO    ESTORNO_APLICACAO
    Lists Should Be Equal    ${tipos}    ${esperado}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Sala De Vacina - Perda Acima Do Saldo - Status Code 422
    [Documentation]    The physical loss does not exceed the balance.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${dados}=    Seed Sala De Vacina
    ${l}=    Entrada De Lote    ${dados}[unidade]    DT
    ${payload}=    Create Dictionary    doses=${11}    motivo=QUEBRA
    ${r}=    POST Perda De Lote    ${l.json()}[uuid]    ${payload}
    Should Be Equal As Integers    ${r.status_code}    422
    Set To Dictionary    ${payload}    doses=${1}
    ${ok}=    POST Perda De Lote    ${l.json()}[uuid]    ${payload}
    Should Be Equal As Integers    ${ok.status_code}    200
    Should Be Equal As Integers    ${ok.json()}[doses]    9
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Sala De Vacina - Excursao Bloqueia - Status Code 422
    [Documentation]    An excursion puts the chamber batch under suspicion; it is not applied.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${dados}=    Seed Sala De Vacina
    ${camara}    ${lote}=    Camara Com Excursao    ${dados}
    ${payload}=    Dose Do Estoque    ${dados}    ${lote}
    ${r}=    POST Registro Vacinacao    ${payload}
    Should Be Equal As Integers    ${r.status_code}    422
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Sala De Vacina - Avaliacao Inutiliza - Status Code 200
    [Documentation]    The coordination discards the batch: the balance leaves as a physical loss.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${dados}=    Seed Sala De Vacina
    ${camara}    ${lote}=    Camara Com Excursao    ${dados}
    ${payload}=    Create Dictionary    decisao=INUTILIZAR    parecer=Fora da estabilidade do fabricante
    ${r}=    POST Avaliacao De Lote    ${lote}    ${payload}
    Should Be Equal As Integers    ${r.status_code}    200
    Validate Object Schema    ${r.json()}    ${LOTE_IMUNOBIOLOGICO_SCHEMA}
    Should Be Equal    ${r.json()}[situacao]    INUTILIZADO
    Should Be Equal As Integers    ${r.json()}[doses]    0
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Sala De Vacina - Cadeia De Frio - Status Code 200
    [Documentation]    The cold chain panel with the chamber, the reading and the suspicious batch, with the schema.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${dados}=    Seed Sala De Vacina
    ${camara}    ${lote}=    Camara Com Excursao    ${dados}
    ${params}=    Create Dictionary    unidadeId=${dados}[unidade]
    ${r}=    GET Cadeia De Frio    ${params}
    Should Be Equal As Integers    ${r.status_code}    200
    Validate Object Schema    ${r.json()}    ${CADEIA_FRIO_SCHEMA}
    Length Should Be    ${r.json()}[camaras]    1
    Length Should Be    ${r.json()}[leituras]    1
    Length Should Be    ${r.json()}[lotesSobSuspeita]    1
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Sala De Vacina - By HTTP Status Code
    [Documentation]    Executes the I2 scenario for the given HTTP status code.
    [Arguments]    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Run Keyword If    '${meuHTTP}' == '201-entrada'    Sala De Vacina - Entrada De Lote - Status Code 201
    ...    ELSE IF    '${meuHTTP}' == '422-sem-sala'    Sala De Vacina - Entrada Sem Sala - Status Code 422
    ...    ELSE IF    '${meuHTTP}' == '201-baixa'    Sala De Vacina - Dose Com Baixa - Status Code 201
    ...    ELSE IF    '${meuHTTP}' == '400-sem-lote-estoque'    Sala De Vacina - Dose Sem Lote Do Estoque - Status Code 400
    ...    ELSE IF    '${meuHTTP}' == '200-estoque'    Sala De Vacina - Estoque - Status Code 200
    ...    ELSE IF    '${meuHTTP}' == '200-estorno'    Sala De Vacina - Anulacao Estorna - Status Code 200
    ...    ELSE IF    '${meuHTTP}' == '422-perda'    Sala De Vacina - Perda Acima Do Saldo - Status Code 422
    ...    ELSE IF    '${meuHTTP}' == '422-excursao'    Sala De Vacina - Excursao Bloqueia - Status Code 422
    ...    ELSE IF    '${meuHTTP}' == '200-inutiliza'    Sala De Vacina - Avaliacao Inutiliza - Status Code 200
    ...    ELSE IF    '${meuHTTP}' == '200-cadeia'    Sala De Vacina - Cadeia De Frio - Status Code 200
    ...    ELSE    Fail    HTTP status code '${meuHTTP}' is not supported for Sala De Vacina test scenario.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
