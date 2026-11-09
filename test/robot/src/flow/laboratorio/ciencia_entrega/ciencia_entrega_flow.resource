*** Settings ***
Library     Collections
Resource    ../../../pom/laboratorio/ciencia_entrega/ciencia_entrega_pom.resource
Resource    ../faixa_referencia/faixa_referencia_flow.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
    Ciência de quem pediu, comunicação do valor crítico e entrega ao paciente (ADR-0181). O mesmo profissional semeado pede,
    analisa e libera; a paciente semeada é mulher nascida em 1990, e a faixa feminina adulta tem críticos abaixo de 7 e acima de 20.
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
Seed A Resultado Liberado
    [Documentation]    Seeds an exam with the adult female range and a released result; returns pedidoId, itemId, matricula and unidadeId.
    [Arguments]    ${valor}=${12.4}    ${liberar}=${True}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${exameId}    ${nome}    ${faixaId}=    Seed A Exame Com Faixa Feminina
    ${pacienteId}    ${cpf}    ${cartaoSus}    ${pacienteNome}=    Seed A Paciente
    ${unidadeId}=    Seed A Unidade De Saude Uuid
    ${matricula}=    Seed A Profissional Matricula
    ${pedidoId}    ${itemId}=    Registrar Resultado Do Exame    ${pacienteId}    ${exameId}    ${unidadeId}    ${matricula}    ${valor}    liberar=${liberar}
    RETURN    ${pedidoId}    ${itemId}    ${matricula}    ${unidadeId}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Ciencia Entrega - Para Ciencia - Status Code 200
    [Documentation]    The released result shows in the requester's list, with the schema.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${pedidoId}    ${itemId}    ${matricula}    ${unidadeId}=    Seed A Resultado Liberado
    ${response}=    GET Para Ciencia    ${matricula}
    Should Be Equal As Integers    ${response.status_code}    200
    ${linha}=    Evaluate    [a for a in $response.json() if a['itemId'] == $itemId][0]
    Should Be Equal    ${linha}[interpretacao]    NORMAL
    Should Not Be True    ${linha}[critico]
    Validate Object Schema    ${linha}    ${RESULTADO_AVISO_EXAME_SCHEMA}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Ciencia Entrega - Ciencia - Status Code 200 E 409
    [Documentation]    Acknowledges the result; a second acknowledgment gives HTTP 409 and the list is empty.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${pedidoId}    ${itemId}    ${matricula}    ${unidadeId}=    Seed A Resultado Liberado
    ${payload}=    Create Dictionary    profissionalMatricula=${matricula}
    ${ok}=    POST Acao Item Exame    ${itemId}    ciencia    ${payload}
    Should Be Equal As Integers    ${ok.status_code}    200
    ${denovo}=    POST Acao Item Exame    ${itemId}    ciencia    ${payload}
    Should Be Equal As Integers    ${denovo.status_code}    409
    ${detalhe}=    GET Get Pedido Exame By Id    ${pedidoId}
    Should Be Equal    ${detalhe.json()}[itens][0][resultado][cienciaPorMatricula]    ${matricula}
    ${pedido}=    POST Ciencia Do Pedido    ${pedidoId}    ${payload}
    Should Be Equal As Integers    ${pedido.status_code}    409
    ${lista}=    GET Para Ciencia    ${matricula}
    ${restantes}=    Evaluate    [a for a in $lista.json() if a['itemId'] == $itemId]
    Should Be Empty    ${restantes}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Ciencia Entrega - Ciencia - Status Code 422 - Outro Profissional
    [Documentation]    Only the requester acknowledges, expecting HTTP 422.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${pedidoId}    ${itemId}    ${matricula}    ${unidadeId}=    Seed A Resultado Liberado
    ${outra}=    Seed A Profissional Matricula
    ${payload}=    Create Dictionary    profissionalMatricula=${outra}
    ${response}=    POST Acao Item Exame    ${itemId}    ciencia    ${payload}
    Should Be Equal As Integers    ${response.status_code}    422
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Ciencia Entrega - Para Ciencia - Status Code 400 - Sem Matricula
    [Documentation]    Without login, the list needs the registration, expecting HTTP 400.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${response}=    GET Para Ciencia
    Should Be Equal As Integers    ${response.status_code}    400
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Ciencia Entrega - Comunicar Critico - Status Code 200
    [Documentation]    A critical result is listed for the lab, communicated and leaves the list.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${pedidoId}    ${itemId}    ${matricula}    ${unidadeId}=    Seed A Resultado Liberado    ${5}
    ${antes}=    GET Criticos A Comunicar
    Should Be Equal As Integers    ${antes.status_code}    200
    ${linha}=    Evaluate    [a for a in $antes.json() if a['itemId'] == $itemId][0]
    Should Be Equal    ${linha}[interpretacao]    CRITICO_BAIXO
    ${payload}=    Create Dictionary    profissionalMatricula=${matricula}    receptorNome=Dra. Plantonista    meio=TELEFONE
    ${response}=    POST Acao Item Exame    ${itemId}    comunicacao-critico    ${payload}
    Should Be Equal As Integers    ${response.status_code}    200
    ${detalhe}=    GET Get Pedido Exame By Id    ${pedidoId}
    Should Be Equal    ${detalhe.json()}[comunicacoesCritico][0][receptorNome]    Dra. Plantonista
    Should Be Equal    ${detalhe.json()}[eventos][-1][tipo]    COMUNICACAO_CRITICO
    ${depois}=    GET Criticos A Comunicar
    ${restantes}=    Evaluate    [a for a in $depois.json() if a['itemId'] == $itemId]
    Should Be Empty    ${restantes}
    ${denovo}=    POST Acao Item Exame    ${itemId}    comunicacao-critico    ${payload}
    Should Be Equal As Integers    ${denovo.status_code}    409
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Ciencia Entrega - Comunicar Critico - Status Code 422 - Nao Critico
    [Documentation]    A normal result is not communicated as critical, expecting HTTP 422.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${pedidoId}    ${itemId}    ${matricula}    ${unidadeId}=    Seed A Resultado Liberado
    ${payload}=    Create Dictionary    profissionalMatricula=${matricula}    receptorNome=Dra. Plantonista    meio=PRESENCIAL
    ${response}=    POST Acao Item Exame    ${itemId}    comunicacao-critico    ${payload}
    Should Be Equal As Integers    ${response.status_code}    422
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Ciencia Entrega - Entrega - Status Code 200
    [Documentation]    Delivered to the patient, then a second copy to a relative, with the schema.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${pedidoId}    ${itemId}    ${matricula}    ${unidadeId}=    Seed A Resultado Liberado
    ${paciente}=    Create Dictionary    profissionalMatricula=${matricula}    unidadeId=${unidadeId}    retiradoPeloPaciente=${True}    forma=IMPRESSO_NA_UNIDADE
    ${primeira}=    POST Entrega Resultado    ${pedidoId}    ${paciente}
    Should Be Equal As Integers    ${primeira.status_code}    200
    ${terceiro}=    Create Dictionary    profissionalMatricula=${matricula}    unidadeId=${unidadeId}    retiradoPeloPaciente=${False}
    ...    terceiroNome=Maria da Silva    terceiroDocumento=RG 12.345.678-9    terceiroParentesco=Mãe    forma=IMPRESSO_NA_UNIDADE
    ${segunda}=    POST Entrega Resultado    ${pedidoId}    ${terceiro}
    Should Be Equal As Integers    ${segunda.status_code}    200
    ${detalhe}=    GET Get Pedido Exame By Id    ${pedidoId}
    Length Should Be    ${detalhe.json()}[entregas]    2
    Should Be Equal    ${detalhe.json()}[entregas][1][terceiroNome]    Maria da Silva
    Should Be Equal    ${detalhe.json()}[eventos][-1][texto]    Segunda via entregue impresso a Maria da Silva (Mãe)
    Validate Object Schema    ${detalhe.json()}[entregas][0]    ${ENTREGA_RESULTADO_EXAME_SCHEMA}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Ciencia Entrega - Entrega - Status Code 400 - Terceiro Sem Documento
    [Documentation]    Another person without document, expecting HTTP 400.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${pedidoId}    ${itemId}    ${matricula}    ${unidadeId}=    Seed A Resultado Liberado
    ${payload}=    Create Dictionary    profissionalMatricula=${matricula}    unidadeId=${unidadeId}    retiradoPeloPaciente=${False}
    ...    terceiroNome=Maria da Silva    forma=IMPRESSO_NA_UNIDADE
    ${response}=    POST Entrega Resultado    ${pedidoId}    ${payload}
    Should Be Equal As Integers    ${response.status_code}    400
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Ciencia Entrega - Entrega - Status Code 422 - Em Andamento
    [Documentation]    The result is not released yet, expecting HTTP 422.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${pedidoId}    ${itemId}    ${matricula}    ${unidadeId}=    Seed A Resultado Liberado    liberar=${False}
    ${payload}=    Create Dictionary    profissionalMatricula=${matricula}    unidadeId=${unidadeId}    retiradoPeloPaciente=${True}    forma=NA_CONSULTA
    ${response}=    POST Entrega Resultado    ${pedidoId}    ${payload}
    Should Be Equal As Integers    ${response.status_code}    422
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Ciencia Entrega - By Cenario
    [Documentation]    Executes the Ciencia Entrega scenario for the given case.
    [Arguments]    ${caso}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    IF    '${caso}' == 'PARA_CIENCIA'
        Ciencia Entrega - Para Ciencia - Status Code 200
    ELSE IF    '${caso}' == 'CIENCIA'
        Ciencia Entrega - Ciencia - Status Code 200 E 409
    ELSE IF    '${caso}' == 'CIENCIA_422'
        Ciencia Entrega - Ciencia - Status Code 422 - Outro Profissional
    ELSE IF    '${caso}' == 'PARA_CIENCIA_400'
        Ciencia Entrega - Para Ciencia - Status Code 400 - Sem Matricula
    ELSE IF    '${caso}' == 'COMUNICAR'
        Ciencia Entrega - Comunicar Critico - Status Code 200
    ELSE IF    '${caso}' == 'COMUNICAR_422'
        Ciencia Entrega - Comunicar Critico - Status Code 422 - Nao Critico
    ELSE IF    '${caso}' == 'ENTREGA'
        Ciencia Entrega - Entrega - Status Code 200
    ELSE IF    '${caso}' == 'ENTREGA_400'
        Ciencia Entrega - Entrega - Status Code 400 - Terceiro Sem Documento
    ELSE IF    '${caso}' == 'ENTREGA_422'
        Ciencia Entrega - Entrega - Status Code 422 - Em Andamento
    ELSE
        Fail    Caso '${caso}' não suportado no cenário Ciencia Entrega.
    END
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
