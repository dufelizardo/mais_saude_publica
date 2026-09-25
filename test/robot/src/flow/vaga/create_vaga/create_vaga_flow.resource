*** Settings ***
Resource    ../../../pom/vaga/create_vaga/create_vaga_pom.resource
Resource    ../../../pom/vaga/get_all_vagas/get_all_vagas_pom.resource
Resource    ../../setor/create_setor/create_setor_flow.resource
Resource    ../../cargo/create_cargo/create_cargo_flow.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
    Vaga (módulo RH, fatia 8c/recrutamento — ver docs/rh/MODELO-RH.md) não tem suíte Robot própria
    ainda; este flow existe só para seedar uma vaga a partir do domínio Administrativo
    (NecessidadeDePessoal.vagaAssociada referencia Vaga, ver ADR-0036). Sem constraint de unicidade —
    resolve o uuid via list+filter por unidade+cargo, igual ProcessoAdministrativo.
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
Seed A Vaga Uuid
    [Documentation]    Seeds a fresh Unidade de Saúde and Cargo, and a Vaga pointing at both, returning the
    ...    Vaga's uuid, the unidade's uuid and the cargo's uuid.
    [Arguments]    ${unidadeId}=${EMPTY}    ${cargoId}=${EMPTY}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    IF    '${unidadeId}' == '${EMPTY}'
        ${unidadeId}=    Seed A Unidade De Saude Uuid
    END
    IF    '${cargoId}' == '${EMPTY}'
        ${cargoId}=    Seed A Cargo Uuid
    END
    ${payload}=    Create Dictionary
    ...    unidadeId=${unidadeId}
    ...    cargoId=${cargoId}
    ...    quantidade=1
    ...    status=ABERTA

    ${response}=    POST Create Vaga    ${payload}
    Should Be Equal As Integers    ${response.status_code}    201

    ${listResponse}=    GET Get All Vagas
    Should Be Equal As Integers    ${listResponse.status_code}    200
    ${uuid}=    Evaluate
    ...    [v['uuid'] for v in $listResponse.json() if v['unidadeUuid'] == '${unidadeId}' and v['cargoUuid'] == '${cargoId}'][0]

    RETURN    ${uuid}    ${unidadeId}    ${cargoId}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
