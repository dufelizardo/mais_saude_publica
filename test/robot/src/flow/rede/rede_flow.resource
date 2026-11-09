*** Settings ***
Library     String
Library     Collections
Library     DateTime
Resource    ../../pom/rede/rede_pom.resource
Resource    ../../pom/solicitacao_regulacao/autorizacao_solicitacao_regulacao/autorizacao_solicitacao_regulacao_pom.resource
Resource    ../solicitacao_regulacao/create_solicitacao_regulacao/create_solicitacao_regulacao_flow.resource
Resource    ../common/schema_validation_flow.resource
Resource    ../../resource/config/schema/schema.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
Proxima Competencia
    [Documentation]    Next month as YYYY-MM.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${m}=    Evaluate    (datetime.date.today().replace(day=1) + datetime.timedelta(days=32)).strftime('%Y-%m')    modules=datetime
    RETURN    ${m}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Seed Oferta
    [Documentation]    Seeds a procedure and an executing unit with an offer for next month; returns oferta, procedimento, executante, competência.
    [Arguments]    ${quantidade}=${5}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${procedimento}    ${nome}=    Seed A Procedimento Regulado
    ${executante}=    Seed A Unidade De Saude Uuid
    ${mes}=    Proxima Competencia
    ${payload}=    Create Dictionary    executanteId=${executante}    procedimentoId=${procedimento}    competencia=${mes}    quantidade=${quantidade}
    ${r}=    POST Oferta Assistencial    ${payload}
    Should Be Equal As Integers    ${r.status_code}    201
    RETURN    ${r.json()}[uuid]    ${procedimento}    ${executante}    ${mes}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Solicitar E Autorizar
    [Documentation]    Requests regulation from the unit and tries to authorize it at the executante in the month.
    [Arguments]    ${procedimento}    ${executante}    ${unidade}    ${mes}    ${justificativa}=${EMPTY}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${paciente}    ${cpf}    ${cns}    ${nome}=    Seed A Paciente
    ${solicitante}=    Seed A Profissional Matricula
    ${regulador}=    Seed A Profissional Matricula
    ${payload}=    Build Solicitacao Regulacao Payload    ${paciente}    ${procedimento}    ${unidade}    ${solicitante}
    ${criada}=    POST Create Solicitacao Regulacao    ${payload}
    Should Be Equal As Integers    ${criada.status_code}    201
    ${lista}=    GET List Solicitacao Regulacao Do Paciente    ${paciente}
    ${id}=    Set Variable    ${lista.json()}[0][uuid]
    ${autorizacao}=    Create Dictionary    profissionalMatricula=${regulador}    unidadeExecutanteId=${executante}    dataHoraPrevista=${mes}-10T09:00:00
    IF    '${justificativa}' != '${EMPTY}'
        Set To Dictionary    ${autorizacao}    extraCota=${True}    justificativaExtraCota=${justificativa}
    END
    ${r}=    POST Autorizar Solicitacao Regulacao    ${id}    ${autorizacao}
    RETURN    ${r}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Rede - Oferta E Cota - Status Code 201
    [Documentation]    Offer and quota; the same offer twice (409) and quotas over the offer (422).
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${oferta}    ${procedimento}    ${executante}    ${mes}=    Seed Oferta    ${5}
    ${repetida}=    Create Dictionary    executanteId=${executante}    procedimentoId=${procedimento}    competencia=${mes}    quantidade=${3}
    ${r409}=    POST Oferta Assistencial    ${repetida}
    Should Be Equal As Integers    ${r409.status_code}    409
    ${ubs}=    Seed A Unidade De Saude Uuid
    ${cota}=    Create Dictionary    beneficiariaId=${ubs}    quantidade=${4}
    ${r}=    POST Cota Ppi    ${oferta}    ${cota}
    Should Be Equal As Integers    ${r.status_code}    201
    Validate Object Schema    ${r.json()}    ${OFERTA_ASSISTENCIAL_SCHEMA}
    Should Be Equal As Integers    ${r.json()}[semPactuar]    1
    ${outra}=    Seed A Unidade De Saude Uuid
    ${demais}=    Create Dictionary    beneficiariaId=${outra}    quantidade=${2}
    ${r422}=    POST Cota Ppi    ${oferta}    ${demais}
    Should Be Equal As Integers    ${r422.status_code}    422
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Rede - Regulacao Consome A Cota - Status Code 200
    [Documentation]    The authorization uses the quota; without balance only as justified extra quota.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${oferta}    ${procedimento}    ${executante}    ${mes}=    Seed Oferta    ${5}
    ${ubs}=    Seed A Unidade De Saude Uuid
    ${cota}=    Create Dictionary    beneficiariaId=${ubs}    quantidade=${1}
    ${rc}=    POST Cota Ppi    ${oferta}    ${cota}
    Should Be Equal As Integers    ${rc.status_code}    201
    ${r1}=    Solicitar E Autorizar    ${procedimento}    ${executante}    ${ubs}    ${mes}
    Should Be Equal As Integers    ${r1.status_code}    200
    ${params}=    Create Dictionary    procedimentoId=${procedimento}    executanteId=${executante}    solicitanteId=${ubs}    competencia=${mes}
    ${saldo}=    GET Saldo Ppi    ${params}
    Should Be Equal As Integers    ${saldo.json()}[saldo]    0
    ${r422}=    Solicitar E Autorizar    ${procedimento}    ${executante}    ${ubs}    ${mes}
    Should Be Equal As Integers    ${r422.status_code}    422
    ${r2}=    Solicitar E Autorizar    ${procedimento}    ${executante}    ${ubs}    ${mes}    Gestante de alto risco.
    Should Be Equal As Integers    ${r2.status_code}    200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Rede - Painel E Copia - Status Code 200
    [Documentation]    The month panel and the copy to the following month.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${oferta}    ${procedimento}    ${executante}    ${mes}=    Seed Oferta    ${7}
    ${params}=    Create Dictionary    competencia=${mes}    executanteId=${executante}
    ${r}=    GET Painel Ppi    ${params}
    Should Be Equal As Integers    ${r.status_code}    200
    Validate Object Schema    ${r.json()}    ${PAINEL_PPI_SCHEMA}
    Should Be Equal As Integers    ${r.json()}[ofertado]    7
    ${destino}=    Evaluate    (datetime.date.fromisoformat($mes + '-01') + datetime.timedelta(days=32)).strftime('%Y-%m')    modules=datetime
    ${copia}=    Create Dictionary    origem=${mes}    destino=${destino}
    ${rc}=    POST Copia Ppi    ${copia}
    Should Be Equal As Integers    ${rc.status_code}    200
    Should Be True    ${rc.json()}[ofertasCopiadas] >= 1
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Rede - By HTTP Status Code
    [Documentation]    Dispatches the scenario by HTTP status code.
    [Arguments]    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Run Keyword If    '${meuHTTP}' == '201-oferta'    Rede - Oferta E Cota - Status Code 201
    Run Keyword If    '${meuHTTP}' == '200-regulacao'    Rede - Regulacao Consome A Cota - Status Code 200
    Run Keyword If    '${meuHTTP}' == '200-painel'    Rede - Painel E Copia - Status Code 200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
