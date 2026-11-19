*** Settings ***
Library     String
Library     Collections
Library     DateTime
Resource    ../../pom/patrimonio/manutencao_pom.resource
Resource    ./patrimonio_flow.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
    O chamado, a execução, o plano vencido, a calibração e o cancelamento (ADR-0216).
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
Seed Chamado
    [Documentation]    A tagged asset with a call opened; returns the work order, its number, the asset and the unit.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${bem}    ${unidade}    ${matricula}=    Seed Bem Patrimonial
    ${payload}=    Create Dictionary    defeito=Não liga.    bemParado=${True}
    ${r}=    POST Chamado Manutencao    ${bem}    ${payload}
    Should Be Equal As Integers    ${r.status_code}    201
    RETURN    ${r.json()}[uuid]    ${r.json()}[numero]    ${bem}    ${unidade}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Manutencao - Chamado E Execucao - Status Code 400 E 200
    [Documentation]    The internal execution asks for the technician; the conclusion returns the asset to use.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${os}    ${numero}    ${bem}    ${unidade}=    Seed Chamado
    ${payload}=    Create Dictionary    execucao=INTERNA
    ${r}=    POST Inicio Ordem Servico    ${os}    ${payload}
    Should Be Equal As Integers    ${r.status_code}    400
    ${payload}=    Create Dictionary    execucao=INTERNA    tecnico=Equipe de eletrônica
    ${r}=    POST Inicio Ordem Servico    ${os}    ${payload}
    Should Be Equal As Integers    ${r.status_code}    200
    Should Be Equal    ${r.json()}[situacao]    EM_EXECUCAO
    ${b}=    GET Bem Patrimonial    ${bem}
    Should Be Equal    ${b.json()}[bem][situacao]    EM_MANUTENCAO
    ${pecas}=    Create List
    ${payload}=    Create Dictionary    servicoRealizado=Troca da fonte.    custoMaoDeObra=${80}    pecas=${pecas}
    ${r}=    POST Conclusao Ordem Servico    ${os}    ${payload}
    Should Be Equal As Integers    ${r.status_code}    200
    Should Be Equal    ${r.json()}[situacao]    CONCLUIDA
    ${b}=    GET Bem Patrimonial    ${bem}
    Should Be Equal    ${b.json()}[bem][situacao]    EM_USO
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Manutencao - Plano Vencido - Status Code 400 409 E 200
    [Documentation]    Corrective has no plan (400); one active plan per type (409); the overdue plan opens one order.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${bem}    ${unidade}    ${matricula}=    Seed Bem Patrimonial
    ${payload}=    Create Dictionary    tipo=CORRETIVA    periodicidadeDias=${30}
    ${r}=    POST Plano Manutencao    ${bem}    ${payload}
    Should Be Equal As Integers    ${r.status_code}    400
    ${ultima}=    Get Current Date    UTC    - 400 days    result_format=%Y-%m-%d
    ${payload}=    Create Dictionary    tipo=PREVENTIVA    periodicidadeDias=${365}    ultimaExecucao=${ultima}
    ${r}=    POST Plano Manutencao    ${bem}    ${payload}
    Should Be Equal As Integers    ${r.status_code}    201
    Should Be True    ${r.json()}[planos][0][vencido]
    ${r}=    POST Plano Manutencao    ${bem}    ${payload}
    Should Be Equal As Integers    ${r.status_code}    409
    ${params}=    Create Dictionary    unidadeId=${unidade}
    ${r}=    GET Ordens Servico    ${params}
    Length Should Be    ${r.json()}    1
    Should Be Equal    ${r.json()}[0][tipo]    PREVENTIVA
    ${r}=    GET Ordens Servico    ${params}
    Length Should Be    ${r.json()}    1
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Manutencao - Calibracao - Status Code 400 E 200
    [Documentation]    The calibration asks for the certificate and a future validity; the plan counts the next date from today.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${bem}    ${unidade}    ${matricula}=    Seed Bem Patrimonial
    ${ultima}=    Get Current Date    UTC    - 200 days    result_format=%Y-%m-%d
    ${payload}=    Create Dictionary    tipo=CALIBRACAO    periodicidadeDias=${180}    ultimaExecucao=${ultima}
    POST Plano Manutencao    ${bem}    ${payload}
    ${params}=    Create Dictionary    unidadeId=${unidade}
    ${r}=    GET Ordens Servico    ${params}
    ${os}=    Set Variable    ${r.json()}[0][uuid]
    ${payload}=    Create Dictionary    execucao=INTERNA    tecnico=Metrologia
    POST Inicio Ordem Servico    ${os}    ${payload}
    ${payload}=    Create Dictionary    servicoRealizado=Calibrado.
    ${r}=    POST Conclusao Ordem Servico    ${os}    ${payload}
    Should Be Equal As Integers    ${r.status_code}    400
    ${validade}=    Get Current Date    UTC    + 365 days    result_format=%Y-%m-%d
    ${payload}=    Create Dictionary    servicoRealizado=Calibrado.    certificado=CAL-9    calibracaoValidaAte=${validade}
    ${r}=    POST Conclusao Ordem Servico    ${os}    ${payload}
    Should Be Equal As Integers    ${r.status_code}    200
    ${m}=    GET Manutencao Do Bem    ${bem}
    Should Not Be True    ${m.json()}[calibracaoVencida]
    Should Not Be True    ${m.json()}[planos][0][vencido]
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Manutencao - Cancelamento - Status Code 200 E 422
    [Documentation]    Cancelling the execution returns the asset; cancelling again is 422.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${os}    ${numero}    ${bem}    ${unidade}=    Seed Chamado
    ${payload}=    Create Dictionary    execucao=INTERNA    tecnico=Equipe
    POST Inicio Ordem Servico    ${os}    ${payload}
    ${payload}=    Create Dictionary    motivo=Peça em falta.
    ${r}=    POST Cancelamento Ordem Servico    ${os}    ${payload}
    Should Be Equal As Integers    ${r.status_code}    200
    ${b}=    GET Bem Patrimonial    ${bem}
    Should Be Equal    ${b.json()}[bem][situacao]    EM_USO
    ${r}=    POST Cancelamento Ordem Servico    ${os}    ${payload}
    Should Be Equal As Integers    ${r.status_code}    422
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Manutencao - By Cenario
    [Documentation]    Executes the maintenance scenario for the given case.
    [Arguments]    ${caso}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    IF    '${caso}' == 'EXECUCAO'
        Manutencao - Chamado E Execucao - Status Code 400 E 200
    ELSE IF    '${caso}' == 'PLANO'
        Manutencao - Plano Vencido - Status Code 400 409 E 200
    ELSE IF    '${caso}' == 'CALIBRACAO'
        Manutencao - Calibracao - Status Code 400 E 200
    ELSE IF    '${caso}' == 'CANCELAMENTO'
        Manutencao - Cancelamento - Status Code 200 E 422
    ELSE
        Fail    Caso '${caso}' não suportado no cenário Manutenção.
    END
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

