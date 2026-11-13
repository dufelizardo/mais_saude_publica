*** Settings ***
Library     String
Library     Collections
Resource    ./rede_tematica_flow.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
    Cobertura da rede e indicadores de pactuação (ADR-0198).
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
Cobertura - Competencia E Relatorio
    [Documentation]    An offer with a quota and one authorization: the coverage shows the pact, the use and the origin; the #19 report answers.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${oferta}    ${procedimento}    ${executante}    ${mes}=    Seed Oferta    ${10}
    ${ubs}=    Seed A Unidade De Saude Uuid
    ${cota}=    Create Dictionary    beneficiariaId=${ubs}    quantidade=${4}
    ${r}=    POST Cota Ppi    ${oferta}    ${cota}
    Should Be Equal As Integers    ${r.status_code}    201
    ${r}=    Solicitar E Autorizar    ${procedimento}    ${executante}    ${ubs}    ${mes}
    Should Be Equal As Integers    ${r.status_code}    200
    ${params}=    Create Dictionary    competencia=${mes}    procedimentoId=${procedimento}
    ${r}=    GET Rede Generico    cobertura    ${params}
    Should Be Equal As Integers    ${r.status_code}    200
    Should Be Equal As Integers    ${r.json()}[procedimentos][0][ofertado]    10
    Should Be Equal As Integers    ${r.json()}[procedimentos][0][pactuado]    4
    Should Be Equal As Integers    ${r.json()}[procedimentos][0][utilizado]    1
    ${saldos}=    Evaluate    [o['saldo'] for o in $r.json()['procedimentos'][0]['origens'] if o['unidadeId'] == $ubs]
    Should Be Equal As Integers    ${saldos}[0]    3
    Get Mais Saude Publica Session
    ${rel}=    GET On Session    msp_session    ${BASE_URL}/relatorios/PACTUACAO_REDE    expected_status=ANY
    Should Be Equal As Integers    ${rel.status_code}    200
    Should Be Equal    ${rel.json()}[categoria]    Gestão da rede
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Cobertura - By Cenario
    [Documentation]    Executes the network coverage scenario for the given case.
    [Arguments]    ${caso}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    IF    '${caso}' == 'COMPETENCIA'
        Cobertura - Competencia E Relatorio
    ELSE
        Fail    Caso '${caso}' não suportado no cenário Cobertura da Rede.
    END
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
