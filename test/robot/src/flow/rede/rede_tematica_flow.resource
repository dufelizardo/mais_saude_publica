*** Settings ***
Library     String
Library     Collections
Resource    ../../pom/rede/rede_tematica_pom.resource
Resource    ./rede_flow.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
    Redes temáticas e referência estrutural (ADR-0197): a referência como sugestão e a autorização fora dela marcada.
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
Seed Referencia
    [Documentation]    A procedure, an origin and an executing unit with the reference between them; returns procedimento, origem, executante and the reference.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${procedimento}    ${nome}=    Seed A Procedimento Regulado
    ${origem}=    Seed A Unidade De Saude Uuid
    ${executante}=    Seed A Unidade De Saude Uuid
    ${payload}=    Create Dictionary    origemId=${origem}    procedimentoId=${procedimento}    executanteId=${executante}
    ${r}=    POST Referencia Rede    ${payload}
    Should Be Equal As Integers    ${r.status_code}    201
    RETURN    ${procedimento}    ${origem}    ${executante}    ${r.json()}[uuid]
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Rede Tematica - Referencia E Autorizacao
    [Documentation]    Same unit, 400; repeated, 409; the suggestion; the authorization elsewhere marked; the indicator; the panel of a network; closing.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${procedimento}    ${origem}    ${executante}    ${referencia}=    Seed Referencia
    ${mesma}=    Create Dictionary    origemId=${origem}    procedimentoId=${procedimento}    executanteId=${origem}
    ${r}=    POST Referencia Rede    ${mesma}
    Should Be Equal As Integers    ${r.status_code}    400
    ${repetida}=    Create Dictionary    origemId=${origem}    procedimentoId=${procedimento}    executanteId=${executante}
    ${r}=    POST Referencia Rede    ${repetida}
    Should Be Equal As Integers    ${r.status_code}    409
    ${params}=    Create Dictionary    procedimentoId=${procedimento}    solicitanteId=${origem}
    ${r}=    GET Rede Generico    referencia/sugestao    ${params}
    Should Be Equal    ${r.json()}[executanteId]    ${executante}
    ${outra}=    Seed A Unidade De Saude Uuid
    ${mes}=    Proxima Competencia
    ${r}=    Solicitar E Autorizar    ${procedimento}    ${outra}    ${origem}    ${mes}
    Should Be Equal As Integers    ${r.status_code}    200
    ${filtro}=    Create Dictionary    procedimentoId=${procedimento}
    ${r}=    GET Rede Generico    referencia    ${filtro}
    Should Be Equal As Integers    ${r.json()}[indicadores][0][foraDaReferencia]    1
    ${r}=    GET Rede Generico    tematica/CEGONHA_ALYNE
    Should Be Equal As Integers    ${r.status_code}    200
    Length Should Be    ${r.json()}[componentes]    4
    ${r}=    POST Encerramento Referencia Rede    ${referencia}
    Should Be Equal As Integers    ${r.status_code}    200
    ${r}=    POST Encerramento Referencia Rede    ${referencia}
    Should Be Equal As Integers    ${r.status_code}    422
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Rede Tematica - By Cenario
    [Documentation]    Executes the thematic networks scenario for the given case.
    [Arguments]    ${caso}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    IF    '${caso}' == 'REFERENCIA'
        Rede Tematica - Referencia E Autorizacao
    ELSE
        Fail    Caso '${caso}' não suportado no cenário Redes Temáticas.
    END
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
