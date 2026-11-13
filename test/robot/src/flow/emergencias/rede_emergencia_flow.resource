*** Settings ***
Library     String
Library     Collections
Resource    ../../pom/emergencias/pessoas_emergencia_pom.resource
Resource    ./emergencias_flow.resource
Resource    ../estoque/estoque_flow.resource
Resource    ../solicitacao_regulacao/create_solicitacao_regulacao/create_solicitacao_regulacao_flow.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
    A rede na emergência (ADR-0194): onde tem o recurso, compromissos nas unidades afetadas e a prioridade da emergência na Regulação.
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
DELETE Prioridade Emergencia
    [Documentation]    Removes the emergency mark from the regulation request.
    [Arguments]    ${emergencia}    ${solicitacao}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Get Mais Saude Publica Session
    ${response}=    DELETE On Session    msp_session    ${BASE_URL}/emergencia/${emergencia}/regulacao/${solicitacao}/prioridade    expected_status=ANY
    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Paciente Deslocado Na Emergencia
    [Documentation]    A fresh emergency with the patient displaced to a school; returns the emergency uuid.
    [Arguments]    ${paciente}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${em}=    Seed Emergencia
    ${id}=    Set Variable    ${em.json()}[uuid]
    ${p}=    Create Dictionary    pacienteId=${paciente}    destinoTipo=OUTRO    destinoDescricao=Escola Robot
    ${r}=    POST Pessoas Emergencia    emergencia/${id}/deslocado    ${p}
    Should Be Equal As Integers    ${r.status_code}    201
    RETURN    ${id}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Rede - Onde Tem
    [Documentation]    The material with stock at a unit shows there; asking for both or none is 400.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${unidade}=    Seed A Unidade De Saude Uuid
    ${material}    ${nome}=    Seed Material Estoque
    ${sufixo}=    Generate Random String    6    [UPPER][NUMBERS]
    ${e}=    Entrada De Lote    ${unidade}    ${material}    ${25}    RDE${sufixo}    200
    Should Be Equal As Integers    ${e.status_code}    201
    ${em}=    Seed Emergencia
    ${id}=    Set Variable    ${em.json()}[uuid]
    ${r}=    GET Pessoas Emergencia    emergencia/${id}/disponibilidade
    Should Be Equal As Integers    ${r.status_code}    400
    ${r}=    GET Pessoas Emergencia    emergencia/${id}/disponibilidade?materialId=${material}
    Should Be Equal As Integers    ${r.status_code}    200
    ${linha}=    Evaluate    [d for d in $r.json() if d['unidadeId'] == $unidade][0]
    Should Be Equal As Integers    ${linha}[livre]    25
    ${rede}=    GET Pessoas Emergencia    emergencia/${id}/rede
    Should Be Equal As Integers    ${rede.status_code}    200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Rede - Prioridade Na Regulacao
    [Documentation]    The request of the displaced patient goes up in the queue with the mark; twice is 409; removing it twice is 422.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${uuid}    ${matricula}    ${procedimento}    ${paciente}    ${unidade}=    Seed A Solicitacao Regulacao    AZUL
    ${em}=    Seed Emergencia
    ${outra}=    Set Variable    ${em.json()}[uuid]
    ${p}=    Create Dictionary
    ${r}=    POST Pessoas Emergencia    emergencia/${outra}/regulacao/${uuid}/prioridade    ${p}
    Should Be Equal As Integers    ${r.status_code}    422
    ${em}=    Paciente Deslocado Na Emergencia    ${paciente}
    ${lista}=    GET Pessoas Emergencia    emergencia/${em}/regulacoes?pacienteId=${paciente}
    Should Be Equal    ${lista.json()}[0][id]    ${uuid}
    ${r}=    POST Pessoas Emergencia    emergencia/${em}/regulacao/${uuid}/prioridade    ${p}
    Should Be Equal As Integers    ${r.status_code}    200
    Should Be Equal    ${r.json()}[priorizadas][0][id]    ${uuid}
    ${r}=    POST Pessoas Emergencia    emergencia/${em}/regulacao/${uuid}/prioridade    ${p}
    Should Be Equal As Integers    ${r.status_code}    409
    ${fila}=    GET Pessoas Emergencia    solicitacao-regulacao/fila?procedimentoId=${procedimento}
    Should Be Equal    ${fila.json()}[0][emergenciaNome]    Enchente Robot
    ${r}=    DELETE Prioridade Emergencia    ${em}    ${uuid}
    Should Be Equal As Integers    ${r.status_code}    200
    ${r}=    DELETE Prioridade Emergencia    ${em}    ${uuid}
    Should Be Equal As Integers    ${r.status_code}    422
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Rede - By Cenario
    [Documentation]    Executes the network-in-emergencies scenario for the given case.
    [Arguments]    ${caso}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    IF    '${caso}' == 'ONDE_TEM'
        Rede - Onde Tem
    ELSE IF    '${caso}' == 'PRIORIDADE'
        Rede - Prioridade Na Regulacao
    ELSE
        Fail    Caso '${caso}' não suportado no cenário Rede na Emergência.
    END
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
