*** Settings ***
Library    Collections
Resource    ../../../pom/administracao_medicamento/create_administracao_medicamento/create_administracao_medicamento_pom.resource
Resource    ../../../pom/administracao_medicamento/get_administracao_medicamento_by_id/get_administracao_medicamento_by_id_pom.resource
Resource    ../../consulta/create_consulta/create_consulta_flow.resource
Resource    ../../lote/create_lote/create_lote_flow.resource
Resource    ../../lote/get_lote_by_id/get_lote_by_id_flow.resource
Resource    ../../common/schema_validation_flow.resource
Resource    ../../../resource/config/schema/schema.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
    Administração de medicamento (ADR-0064): checagem de enfermagem de um item prescrito. Administrado baixa o
    lote da unidade do atendimento pelo livro da Farmácia; não administrado exige motivo e não mexe no estoque.
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
Seed Uma Prescricao Com Estoque
    [Documentation]    Seeds an Atendimento with a Consulta (the prescription) and a Lote (100) of a fresh Medicamento in the
    ...    same unit, returning atendimentoId, consultaId, medicamentoId, loteId and the matrícula.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${atendimentoId}    ${pacienteId}    ${matricula}    ${unidadeId}=    Seed Um Atendimento
    ${consulta}=    Build Consulta Payload    ${atendimentoId}    ${matricula}
    ${criada}=    POST Create Consulta    ${consulta}
    Should Be Equal As Integers    ${criada.status_code}    201
    ${consultaId}=    Buscar Consulta Uuid Por Atendimento    ${atendimentoId}

    ${medicamentoId}    ${nomeMedicamento}=    Seed Um Medicamento
    ${lote}    ${numeroLote}=    Build Lote Payload    ${medicamentoId}    ${unidadeId}
    ${loteCriado}=    POST Create Lote    ${lote}
    Should Be Equal As Integers    ${loteCriado.status_code}    201
    ${loteId}=    Buscar Lote Uuid Por NumeroLote    ${numeroLote}

    RETURN    ${atendimentoId}    ${consultaId}    ${medicamentoId}    ${loteId}    ${matricula}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Build Administracao Payload
    [Documentation]    Builds an ADMINISTRADO AdministracaoMedicamentoRequestDto payload.
    [Arguments]    ${atendimentoId}    ${consultaId}    ${medicamentoId}    ${loteId}    ${matricula}    ${quantidade}=${2}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${payload}=    Create Dictionary
    ...    atendimentoId=${atendimentoId}
    ...    consultaId=${consultaId}
    ...    medicamentoId=${medicamentoId}
    ...    situacao=ADMINISTRADO
    ...    loteId=${loteId}
    ...    dose=1 g
    ...    via=INTRAMUSCULAR
    ...    quantidade=${quantidade}
    ...    dataHora=2026-03-01T08:40:00
    ...    profissionalMatricula=${matricula}

    RETURN    ${payload}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Buscar Administracao Uuid Por Atendimento
    [Documentation]    Lists every AdministracaoMedicamento and returns the uuid of the current one of ${atendimentoId}.
    [Arguments]    ${atendimentoId}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${lista}=    GET List Administracoes Medicamento
    Should Be Equal As Integers    ${lista.status_code}    200
    ${uuid}=    Evaluate    [a['uuid'] for a in $lista.json() if a['atendimentoUuid'] == $atendimentoId and not a['retificado']][0]

    RETURN    ${uuid}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

POST Create Administracao Medicamento - Status Code 201
    [Documentation]    Administers 2 units of a fresh Lote (100) and checks the Lote balance dropped to 98.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${atendimentoId}    ${consultaId}    ${medicamentoId}    ${loteId}    ${matricula}=    Seed Uma Prescricao Com Estoque
    ${payload}=    Build Administracao Payload    ${atendimentoId}    ${consultaId}    ${medicamentoId}    ${loteId}    ${matricula}

    ${response}=    POST Create Administracao Medicamento    ${payload}
    Should Be Equal As Integers    ${response.status_code}    201
    Validate Object Schema    ${response.json()}    ${SUCCESS_RESPONSE_SCHEMA}

    ${lote}=    Call GET Get Lote By Id    ${loteId}
    Should Be Equal As Integers    ${lote.json()['quantidade']}    98

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

POST Create Administracao Medicamento - Status Code 400 - Administrado Sem Lote
    [Documentation]    Registers an ADMINISTRADO check without loteId, expecting HTTP 400 and an unchanged balance.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${atendimentoId}    ${consultaId}    ${medicamentoId}    ${loteId}    ${matricula}=    Seed Uma Prescricao Com Estoque
    ${payload}=    Build Administracao Payload    ${atendimentoId}    ${consultaId}    ${medicamentoId}    ${loteId}    ${matricula}
    Remove From Dictionary    ${payload}    loteId

    ${response}=    POST Create Administracao Medicamento    ${payload}
    Should Be Equal As Integers    ${response.status_code}    400
    Validate Object Schema    ${response.json()}    ${ERROR_RESPONSE_SCHEMA}

    ${lote}=    Call GET Get Lote By Id    ${loteId}
    Should Be Equal As Integers    ${lote.json()['quantidade']}    100

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

POST Create Administracao Medicamento - Status Code 422 - Maior Que O Saldo
    [Documentation]    Administers more than the Lote balance, expecting HTTP 422 and an unchanged balance.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${atendimentoId}    ${consultaId}    ${medicamentoId}    ${loteId}    ${matricula}=    Seed Uma Prescricao Com Estoque
    ${payload}=    Build Administracao Payload    ${atendimentoId}    ${consultaId}    ${medicamentoId}    ${loteId}    ${matricula}    quantidade=${500}

    ${response}=    POST Create Administracao Medicamento    ${payload}
    Should Be Equal As Integers    ${response.status_code}    422
    Validate Object Schema    ${response.json()}    ${ERROR_RESPONSE_SCHEMA}

    ${lote}=    Call GET Get Lote By Id    ${loteId}
    Should Be Equal As Integers    ${lote.json()['quantidade']}    100

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Administracao Medicamento - Create - POST - By HTTP Status Code
    [Documentation]    Executes the appropriate Create Administracao Medicamento test scenario based on the provided HTTP
    ...    status code.
    [Arguments]    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Run Keyword If    '${meuHTTP}' == '201'    POST Create Administracao Medicamento - Status Code 201
    ...    ELSE IF    '${meuHTTP}' == '400'    POST Create Administracao Medicamento - Status Code 400 - Administrado Sem Lote
    ...    ELSE IF    '${meuHTTP}' == '422'    POST Create Administracao Medicamento - Status Code 422 - Maior Que O Saldo
    ...    ELSE    Fail    HTTP status code '${meuHTTP}' is not supported for Create Administracao Medicamento test scenario.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

