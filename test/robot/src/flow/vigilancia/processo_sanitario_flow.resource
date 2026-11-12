*** Settings ***
Library     String
Library     Collections
Library     DateTime
Resource    ../../pom/vigilancia/processo_sanitario_pom.resource
Resource    ./vigilancia_sanitaria_flow.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
    Processo administrativo sanitário e medidas (ADR-0200): o rito com os prazos, os efeitos e a verificação pública.
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
Dia Relativo
    [Documentation]    The ISO date N days from today (negative for the past).
    [Arguments]    ${dias}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${d}=    Get Current Date    increment=${dias} days    result_format=%Y-%m-%d
    RETURN    ${d}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Seed Processo Com Ciencia
    [Documentation]    A licensed establishment, an inspection with one irregularity and the notice issued and acknowledged in the past; returns the establishment and the process.
    [Arguments]    ${dias_atras}=80
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${estab}=    Seed Estabelecimento Visa    MEDIO
    ${emissao}=    Dia Relativo    -100
    ${lic}=    Create Dictionary    emitidaEm=${emissao}
    ${r}=    POST Licenca Visa    ${estab}    ${lic}
    Should Be Equal As Integers    ${r.status_code}    201
    ${matricula}=    Seed A Profissional Matricula
    ${data}=    Dia Relativo    -${dias_atras}
    ${irreg}=    Create Dictionary    descricao=Alimento vencido    gravidade=GRAVE
    ${irregs}=    Create List    ${irreg}
    ${fiscais}=    Create List    ${matricula}
    ${insp}=    Create Dictionary    estabelecimentoId=${estab}    motivo=PROGRAMADA    dataInspecao=${data}    resultado=INSATISFATORIA
    ...    relatorio=Alimento vencido exposto.    fiscais=${fiscais}    irregularidades=${irregs}
    ${r}=    POST Inspecao Visa    ${insp}
    Should Be Equal As Integers    ${r.status_code}    201
    ${ids}=    Create List    ${r.json()}[irregularidades][0][uuid]
    ${auto}=    Create Dictionary    estabelecimentoId=${estab}    irregularidades=${ids}    dispositivoLegal=Lei 6.437/1977, art. 10, IV
    ...    descricao=Expor à venda alimento vencido.    dataLavratura=${data}    fiscais=${fiscais}
    ${r}=    POST Processo Sanitario    ${auto}
    Should Be Equal As Integers    ${r.status_code}    201
    ${processo}=    Set Variable    ${r.json()}[uuid]
    ${dia}=    Dia Relativo    -${{int($dias_atras) - 1}}
    ${ciencia}=    Create Dictionary    forma=PESSOAL    data=${dia}
    ${r}=    POST Acao Processo Sanitario    ${processo}    ciencia    ${ciencia}
    Should Be Equal As Integers    ${r.status_code}    200
    RETURN    ${estab}    ${processo}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Processo Sanitario - Rito Com Encerramento - Status Code 200
    [Documentation]    Defense, decision with fine and license suspension, acknowledgment, and closing after the appeal deadline: the license is suspended.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${estab}    ${processo}=    Seed Processo Com Ciencia
    ${dia}=    Dia Relativo    -60
    ${defesa}=    Create Dictionary    texto=Era para descarte.    data=${dia}
    ${r}=    POST Acao Processo Sanitario    ${processo}    defesa    ${defesa}
    Should Be Equal As Integers    ${r.status_code}    200
    Should Be True    ${r.json()}[defesaIntempestiva]
    ${ate}=    Dia Relativo    30
    ${multa}=    Create Dictionary    tipo=MULTA    valorMulta=${2000}
    ${suspensao}=    Create Dictionary    tipo=SUSPENSAO_LICENCA    suspensaoAte=${ate}
    ${pens}=    Create List    ${multa}    ${suspensao}
    ${dia}=    Dia Relativo    -50
    ${decisao}=    Create Dictionary    resultado=PENALIDADE    fundamentacao=Infração comprovada.    data=${dia}    penalidades=${pens}
    ${r}=    POST Acao Processo Sanitario    ${processo}    decisao    ${decisao}
    Should Be Equal As Integers    ${r.status_code}    200
    Should Be Equal    ${r.json()}[fase]    DECIDIDO
    ${dia}=    Dia Relativo    -40
    ${ciencia}=    Create Dictionary    forma=POSTAL    data=${dia}
    ${r}=    POST Acao Processo Sanitario    ${processo}    ciencia    ${ciencia}
    Should Be Equal As Integers    ${r.status_code}    200
    ${vazio}=    Create Dictionary
    ${r}=    POST Acao Processo Sanitario    ${processo}    encerramento    ${vazio}
    Should Be Equal As Integers    ${r.status_code}    200
    Should Be Equal    ${r.json()}[fase]    ENCERRADO
    ${r}=    GET Estabelecimento Visa    ${estab}
    Should Be Equal    ${r.json()}[estabelecimento][licencaAtual][situacao]    SUSPENSA
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Processo Sanitario - Decisao Antes Do Prazo - Status Code 422
    [Documentation]    The decision without defense waits for the 15-day deadline.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${estab}    ${processo}=    Seed Processo Com Ciencia    5
    ${decisao}=    Create Dictionary    resultado=ARQUIVAMENTO    fundamentacao=Cedo demais.
    ${r}=    POST Acao Processo Sanitario    ${processo}    decisao    ${decisao}
    Should Be Equal As Integers    ${r.status_code}    422
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Processo Sanitario - Verificacao Publica - Status Code 200
    [Documentation]    The infraction notice code is checked without login, with the file intact.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${estab}    ${processo}=    Seed Processo Com Ciencia    10
    ${r}=    GET Processo Sanitario    ${processo}
    ${codigo}=    Set Variable    ${r.json()}[termos][0][codigo]
    ${r}=    GET Verificacao Termo Visa    ${codigo}
    Should Be Equal As Integers    ${r.status_code}    200
    Should Be True    ${r.json()}[integro]
    Should Be Equal    ${r.json()}[tipo]    AUTO_INFRACAO
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Processo Sanitario - Interdicao Cautelar - Status Code 201
    [Documentation]    The precautionary closure goes up to 90 days and is lifted with its own document.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${estab}=    Seed Estabelecimento Visa    MEDIO
    ${fim}=    Dia Relativo    91
    ${payload}=    Create Dictionary    estabelecimentoId=${estab}    tipo=INTERDICAO_CAUTELAR    descricao=Câmara fria parada.    fimPrevisto=${fim}
    ${r}=    POST Medida Sanitaria    ${payload}
    Should Be Equal As Integers    ${r.status_code}    400
    Remove From Dictionary    ${payload}    fimPrevisto
    ${r}=    POST Medida Sanitaria    ${payload}
    Should Be Equal As Integers    ${r.status_code}    201
    ${medida}=    Set Variable    ${r.json()}[uuid]
    ${motivo}=    Create Dictionary    motivo=Conserto feito.
    ${r}=    POST Encerramento Medida Sanitaria    ${medida}    ${motivo}
    Should Be Equal As Integers    ${r.status_code}    200
    Should Be Equal    ${r.json()}[situacao]    ENCERRADA
    ${tipos}=    Evaluate    [t['tipo'] for t in $r.json()['termos']]
    Should Be Equal    ${tipos}    ${{['INTERDICAO', 'DESINTERDICAO']}}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Processo Sanitario - By Cenario
    [Documentation]    Executes the sanitary process scenario for the given case.
    [Arguments]    ${caso}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    IF    '${caso}' == 'RITO'
        Processo Sanitario - Rito Com Encerramento - Status Code 200
    ELSE IF    '${caso}' == 'PRAZO_DEFESA'
        Processo Sanitario - Decisao Antes Do Prazo - Status Code 422
    ELSE IF    '${caso}' == 'VERIFICACAO'
        Processo Sanitario - Verificacao Publica - Status Code 200
    ELSE IF    '${caso}' == 'INTERDICAO'
        Processo Sanitario - Interdicao Cautelar - Status Code 201
    ELSE
        Fail    Caso '${caso}' não suportado no cenário Processo Sanitário.
    END
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
