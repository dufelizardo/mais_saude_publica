*** Settings ***
Library     String
Library     Collections
Library     DateTime
Resource    ../../pom/patrimonio/inventario_pom.resource
Resource    ./patrimonio_flow.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
    O inventário com a comissão, a conferência, o fechamento, a baixa e o painel (ADR-0217).
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
Comissao Inventario
    [Documentation]    Three fresh professionals for the inventory committee; returns the list of registrations.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${m1}=    Seed A Profissional Matricula
    ${m2}=    Seed A Profissional Matricula
    ${m3}=    Seed A Profissional Matricula
    ${lista}=    Create List    ${m1}    ${m2}    ${m3}
    RETURN    ${lista}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Inventario - Abertura - Status Code 400 409 E 201
    [Documentation]    Two members are 400; three open it; a second one in the same unit is 409.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${bem}    ${unidade}    ${matricula}=    Seed Bem Patrimonial
    ${comissao}=    Comissao Inventario
    ${duas}=    Create List    ${comissao}[0]    ${comissao}[1]
    ${payload}=    Create Dictionary    unidadeId=${unidade}    membrosMatriculas=${duas}
    ${r}=    POST Inventario Patrimonio    ${payload}
    Should Be Equal As Integers    ${r.status_code}    400
    ${payload}=    Create Dictionary    unidadeId=${unidade}    membrosMatriculas=${comissao}
    ${r}=    POST Inventario Patrimonio    ${payload}
    Should Be Equal As Integers    ${r.status_code}    201
    Should Be Equal As Integers    ${r.json()}[total]    1
    ${r}=    POST Inventario Patrimonio    ${payload}
    Should Be Equal As Integers    ${r.status_code}    409
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Inventario - Conferencia E Fechamento - Status Code 422 E 200
    [Documentation]    Closing with assets to check is 422; the asset not found gets the suggested write-off.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${bem}    ${unidade}    ${matricula}=    Seed Bem Patrimonial
    ${comissao}=    Comissao Inventario
    ${payload}=    Create Dictionary    unidadeId=${unidade}    membrosMatriculas=${comissao}
    ${r}=    POST Inventario Patrimonio    ${payload}
    ${inv}=    Set Variable    ${r.json()}[uuid]
    ${item}=    Set Variable    ${r.json()}[itens][0][uuid]
    ${r}=    POST Fechamento Inventario    ${inv}
    Should Be Equal As Integers    ${r.status_code}    422
    ${payload}=    Create Dictionary    resultado=NAO_ENCONTRADO
    ${r}=    POST Conferencia Inventario    ${item}    ${payload}
    Should Be Equal As Integers    ${r.status_code}    200
    ${r}=    POST Fechamento Inventario    ${inv}
    Should Be Equal As Integers    ${r.status_code}    200
    Should Be Equal    ${r.json()}[situacao]    FECHADO
    Should Be Equal    ${r.json()}[itens][0][acao]    BAIXA_SUGERIDA
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Inventario - Baixa - Status Code 400 201 E 422
    [Documentation]    Theft without the police report is 400; the write-off below the limit is immediate; again is 422.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${bem}    ${unidade}    ${matricula}=    Seed Bem Patrimonial
    ${hoje}=    Hoje Em Brasilia
    ${payload}=    Create Dictionary    motivo=FURTO_ROUBO    processo=PA 9/2026    dataBaixa=${hoje}
    ${r}=    POST Baixa Bem    ${bem}    ${payload}
    Should Be Equal As Integers    ${r.status_code}    400
    ${payload}=    Create Dictionary    motivo=INSERVIVEL    processo=PA 9/2026    dataBaixa=${hoje}
    ${r}=    POST Baixa Bem    ${bem}    ${payload}
    Should Be Equal As Integers    ${r.status_code}    201
    Should Be Equal    ${r.json()}[situacao]    CONFIRMADA
    ${b}=    GET Bem Patrimonial    ${bem}
    Should Be Equal    ${b.json()}[bem][situacao]    BAIXADO
    ${r}=    POST Baixa Bem    ${bem}    ${payload}
    Should Be Equal As Integers    ${r.status_code}    422
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Inventario - Painel - Status Code 200
    [Documentation]    The dashboard answers with the totals and the lists.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Seed Bem Patrimonial
    ${r}=    GET Painel Patrimonio
    Should Be Equal As Integers    ${r.status_code}    200
    Should Be True    ${r.json()}[bens] >= 1
    Dictionary Should Contain Key    ${r.json()}    porUnidade
    Dictionary Should Contain Key    ${r.json()}    manutencao
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Inventario - By Cenario
    [Documentation]    Executes the inventory scenario for the given case.
    [Arguments]    ${caso}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    IF    '${caso}' == 'ABERTURA'
        Inventario - Abertura - Status Code 400 409 E 201
    ELSE IF    '${caso}' == 'FECHAMENTO'
        Inventario - Conferencia E Fechamento - Status Code 422 E 200
    ELSE IF    '${caso}' == 'BAIXA'
        Inventario - Baixa - Status Code 400 201 E 422
    ELSE IF    '${caso}' == 'PAINEL'
        Inventario - Painel - Status Code 200
    ELSE
        Fail    Caso '${caso}' não suportado no cenário Inventário.
    END
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

