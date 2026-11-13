*** Settings ***
Library     String
Library     Collections
Resource    ../../pom/imunizacao/imunizacao_rede_pom.resource
Resource    sala_de_vacina_flow.resource
Resource    esavi_flow.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
    As duas salas de vacina vêm do modelo operacional; a contraindicação, do ESAVI encerrado com "contraindicar".
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
Duas Salas Com Lote
    [Documentation]    Two vaccination rooms, a dT batch of 10 doses in the first; returns origin data, destination data and the batch.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${origem}=    Seed Sala De Vacina
    ${destino}=    Seed Sala De Vacina
    ${l}=    Entrada De Lote    ${origem}[unidade]    DT
    Should Be Equal As Integers    ${l.status_code}    201
    RETURN    ${origem}    ${destino}    ${l.json()}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Payload Remanejamento
    [Documentation]    The transfer payload.
    [Arguments]    ${lote}    ${destino}    ${doses}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${payload}=    Create Dictionary    loteId=${lote}    unidadeDestinoId=${destino}    doses=${doses}    motivo=EXCESSO
    RETURN    ${payload}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Imunizacao Rede - Remanejamento Recebido - Status Code 200
    [Documentation]    Sent and refused, the doses go back; sent and received within 2 to 8 °C, the batch is born in the destination.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${origem}    ${destino}    ${lote}=    Duas Salas Com Lote
    ${payload}=    Payload Remanejamento    ${lote}[uuid]    ${destino}[unidade]    ${4}
    ${r}=    POST Remanejamento    ${payload}
    Should Be Equal As Integers    ${r.status_code}    201
    Should Be Equal    ${r.json()}[situacao]    ENVIADO
    ${saldo}=    Saldo Do Lote    ${origem}[unidade]    ${lote}[uuid]
    Should Be Equal As Integers    ${saldo}    6
    ${recusa}=    Create Dictionary    motivo=Câmara cheia
    ${x}=    POST Acao Remanejamento    ${r.json()}[uuid]    recusa    ${recusa}
    Should Be Equal As Integers    ${x.status_code}    200
    ${saldo}=    Saldo Do Lote    ${origem}[unidade]    ${lote}[uuid]
    Should Be Equal As Integers    ${saldo}    10
    ${r}=    POST Remanejamento    ${payload}
    ${chegada}=    Create Dictionary    temperaturaChegada=${5.0}
    ${x}=    POST Acao Remanejamento    ${r.json()}[uuid]    recebimento    ${chegada}
    Should Be Equal As Integers    ${x.status_code}    200
    Should Be Equal    ${x.json()}[situacao]    RECEBIDO
    ${saldo}=    Saldo Do Lote    ${destino}[unidade]    ${x.json()}[loteDestinoId]
    Should Be Equal As Integers    ${saldo}    4
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Imunizacao Rede - Recebido Fora Da Faixa - Status Code 200
    [Documentation]    The box out of 2 to 8 °C: the batch arrives under suspicion.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${origem}    ${destino}    ${lote}=    Duas Salas Com Lote
    ${payload}=    Payload Remanejamento    ${lote}[uuid]    ${destino}[unidade]    ${3}
    ${r}=    POST Remanejamento    ${payload}
    ${chegada}=    Create Dictionary    temperaturaChegada=${11.0}
    ${x}=    POST Acao Remanejamento    ${r.json()}[uuid]    recebimento    ${chegada}
    Should Be Equal As Integers    ${x.status_code}    200
    Should Be True    ${x.json()}[chegouForaDaFaixa]
    ${params}=    Create Dictionary    unidadeId=${destino}[unidade]
    ${e}=    GET Estoque Sala De Vacina    ${params}
    ${situacoes}=    Evaluate    [l["situacao"] for l in $e.json()["lotes"] if l["uuid"] == $x.json()["loteDestinoId"]]
    Should Be Equal    ${situacoes}[0]    SOB_SUSPEITA
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Imunizacao Rede - Regras Do Envio - Status Code 422
    [Documentation]    The own unit is 400; more doses than the batch is 422.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${origem}    ${destino}    ${lote}=    Duas Salas Com Lote
    ${payload}=    Payload Remanejamento    ${lote}[uuid]    ${origem}[unidade]    ${2}
    ${r}=    POST Remanejamento    ${payload}
    Should Be Equal As Integers    ${r.status_code}    400
    ${payload}=    Payload Remanejamento    ${lote}[uuid]    ${destino}[unidade]    ${11}
    ${r}=    POST Remanejamento    ${payload}
    Should Be Equal As Integers    ${r.status_code}    422
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Imunizacao Rede - Suspensao Na Rede - Status Code 201
    [Documentation]    The suspension puts the batch under suspicion (409 when repeated); releasing brings it back.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${dados}=    Seed Sala De Vacina
    ${l}=    Entrada De Lote    ${dados}[unidade]    DT
    ${imuno}=    Id Do Imunobiologico    DT
    ${payload}=    Create Dictionary    imunobiologicoId=${imuno}    numeroLote=${l.json()}[numeroLote]    motivo=Comunicado de desvio de qualidade.
    ${s}=    POST Suspensao De Lote    ${payload}
    Should Be Equal As Integers    ${s.status_code}    201
    Should Be True    ${s.json()}[lotesAlcancados] >= 1
    ${r}=    POST Suspensao De Lote    ${payload}
    Should Be Equal As Integers    ${r.status_code}    409
    ${params}=    Create Dictionary    unidadeId=${dados}[unidade]
    ${e}=    GET Estoque Sala De Vacina    ${params}
    ${lote}=    Evaluate    [x for x in $e.json()["lotes"] if x["uuid"] == $l.json()["uuid"]][0]
    Should Be Equal    ${lote}[situacao]    SOB_SUSPEITA
    Should Be Equal    ${lote}[motivoSuspeita]    ESAVI_LOTE
    ${alertas}=    GET Alertas De Lote
    Should Be Equal As Integers    ${alertas.status_code}    200
    ${numeros}=    Evaluate    [a["numeroLote"] for a in $alertas.json()]
    List Should Contain Value    ${numeros}    ${l.json()}[numeroLote]
    ${decisao}=    Create Dictionary    decisao=LIBERAR    parecer=Sem relação com o lote.
    ${d}=    POST Decisao Suspensao    ${s.json()}[uuid]    ${decisao}
    Should Be Equal As Integers    ${d.status_code}    200
    Should Be Equal    ${d.json()}[situacao]    LIBERADA
    ${d}=    POST Decisao Suspensao    ${s.json()}[uuid]    ${decisao}
    Should Be Equal As Integers    ${d.status_code}    422
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Imunizacao Rede - Revogacao Pelo Crie - Status Code 200
    [Documentation]    The CRIE revokes the contraindication with the opinion; revoking again is 422.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${dados}    ${dose}    ${notificacao}=    Esavi Notificado
    Encerrar Com Contraindicacao    ${notificacao}
    ${c}=    GET Caderneta Vacinacao    ${dados}[paciente]
    ${id}=    Set Variable    ${c.json()}[contraindicacoes][0][uuid]
    ${vazio}=    Create Dictionary    parecer=${EMPTY}
    ${r}=    POST Revogacao Contraindicacao    ${id}    ${vazio}
    Should Be Equal As Integers    ${r.status_code}    400
    ${payload}=    Create Dictionary    parecer=Reavaliada no CRIE: reação local.
    ${r}=    POST Revogacao Contraindicacao    ${id}    ${payload}
    Should Be Equal As Integers    ${r.status_code}    200
    Should Not Be Equal    ${r.json()}[contraindicacoes][0][revogadaEm]    ${None}
    ${r}=    POST Revogacao Contraindicacao    ${id}    ${payload}
    Should Be Equal As Integers    ${r.status_code}    422
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Imunizacao Rede - By Cenario
    [Documentation]    Executes the network immunization scenario for the given case.
    [Arguments]    ${caso}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    IF    '${caso}' == 'RECEBIDO'
        Imunizacao Rede - Remanejamento Recebido - Status Code 200
    ELSE IF    '${caso}' == 'FORA_DA_FAIXA'
        Imunizacao Rede - Recebido Fora Da Faixa - Status Code 200
    ELSE IF    '${caso}' == 'REGRAS'
        Imunizacao Rede - Regras Do Envio - Status Code 422
    ELSE IF    '${caso}' == 'SUSPENSAO'
        Imunizacao Rede - Suspensao Na Rede - Status Code 201
    ELSE IF    '${caso}' == 'CRIE'
        Imunizacao Rede - Revogacao Pelo Crie - Status Code 200
    ELSE
        Fail    Caso '${caso}' não suportado no cenário Imunização na Rede.
    END
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
