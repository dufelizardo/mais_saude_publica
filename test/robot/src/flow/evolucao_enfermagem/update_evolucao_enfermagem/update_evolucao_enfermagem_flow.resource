*** Settings ***
Resource    ../../../pom/evolucao_enfermagem/update_evolucao_enfermagem/update_evolucao_enfermagem_pom.resource
Resource    ../create_evolucao_enfermagem/create_evolucao_enfermagem_flow.resource
Resource    ../get_evolucao_enfermagem_by_id/get_evolucao_enfermagem_by_id_flow.resource
Resource    ../../common/schema_validation_flow.resource
Resource    ../../../resource/config/schema/schema.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
    EvolucaoEnfermagem's PATCH replaces every editable field at once (mesmo contrato do
    Setor/Atendimento/Consulta/Triagem, ver ADR-0048) — corrigir a descrição é uma atualização comum.
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
Call PATCH Update Evolucao Enfermagem
    [Documentation]    Calls the PATCH Update Evolucao Enfermagem keyword from the POM.
    [Arguments]    ${uuid}    ${payload}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${response}=    PATCH Update Evolucao Enfermagem    ${uuid}    ${payload}

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

PATCH Update Evolucao Enfermagem - Status Code 200
    [Documentation]    Seeds a fresh EvolucaoEnfermagem and corrects its descricao.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${evolucaoId}    ${atendimentoId}    ${matricula}=    Seed Uma Evolucao Enfermagem
    ${payload}=    Build Evolucao Enfermagem Payload    ${atendimentoId}    ${matricula}    descricao=Descrição corrigida

    ${response}=    Call PATCH Update Evolucao Enfermagem    ${evolucaoId}    ${payload}
    Should Be Equal As Integers    ${response.status_code}    200

    ${getResponse}=    Call GET Get Evolucao Enfermagem By Id    ${evolucaoId}
    Should Be Equal As Integers    ${getResponse.status_code}    200
    Should Be Equal As Strings    ${getResponse.json()['descricao']}    Descrição corrigida

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

PATCH Update Evolucao Enfermagem - Status Code 404
    [Documentation]    Attempts to update an EvolucaoEnfermagem identified by a random, non-existent uuid,
    ...    expecting HTTP 404.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${atendimentoId}    ${pacienteId}    ${matricula}    ${unidadeId}=    Seed Um Atendimento
    ${payload}=    Build Evolucao Enfermagem Payload    ${atendimentoId}    ${matricula}
    ${uuidInexistente}=    Evaluate    str(uuid.uuid4())    modules=uuid

    ${response}=    Call PATCH Update Evolucao Enfermagem    ${uuidInexistente}    ${payload}
    Should Be Equal As Integers    ${response.status_code}    404

    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Evolucao Enfermagem - Update - PATCH - By HTTP Status Code
    [Documentation]    Executes the appropriate Update Evolucao Enfermagem test scenario based on the
    ...    provided HTTP status code.
    [Arguments]    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Run Keyword If    '${meuHTTP}' == '200'    PATCH Update Evolucao Enfermagem - Status Code 200
    ...    ELSE IF    '${meuHTTP}' == '404'    PATCH Update Evolucao Enfermagem - Status Code 404
    ...    ELSE    Fail    HTTP status code '${meuHTTP}' is not supported for Update Evolucao Enfermagem test scenario.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
