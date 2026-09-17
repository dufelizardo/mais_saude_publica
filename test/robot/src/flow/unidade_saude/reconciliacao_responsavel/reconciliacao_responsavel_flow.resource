*** Settings ***
Library    String
Library    Collections
Resource    ../../unidade_saude/create_unidade_saude/create_unidade_saude_flow.resource
Resource    ../../unidade_saude/get_unidade_saude_by_nome/get_unidade_saude_by_nome_flow.resource
Resource    ../../profissional/create_profissional/create_profissional_flow.resource
Resource    ../../common/schema_validation_flow.resource
Resource    ../../../resource/config/schema/schema.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
    End-to-end coverage for the "profissional chega depois" (synchronous) direction of the weak CPF-based
    reconciliation described in ADR-0014: a Unidade de Saúde is created with a responsavelCpf for which no
    Profissional exists yet, so responsavelNome starts out empty; once a Profissional with that exact CPF is
    created, ProfissionalService.create() reconciles every pending Unidade de Saúde on the spot, so a
    subsequent GET shows responsavelNome populated. The scheduled-job direction (ReconciliacaoResponsavelScheduler,
    for units that already existed before the Profissional does but weren't reconciled synchronously) depends
    on backend timing and can't be exercised from Robot, so it's intentionally out of scope here.
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
Reconciliacao Responsavel Unidade Saude - Antes E Depois Do Profissional Existir
    [Documentation]    Creates a Unidade de Saúde with a responsavelCpf that doesn't match any Profissional yet,
    ...    confirms responsavelNome is absent on GET, creates the matching Profissional, then confirms a
    ...    subsequent GET now shows responsavelNome populated with the Profissional's nome.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${nomeSuperior}=    Seed A Municipal Unit
    ${cpf}=    Generate Random String    11    [NUMBERS]
    ${suffixUnidade}=    Generate Random String    8    [LETTERS][NUMBERS]
    ${nomeUnidade}=    Set Variable    Robot Teste Unidade Saude Reconciliacao ${suffixUnidade}

    POST Create Unidade Saude - Status Code 201
    ...    ${nomeUnidade}    ${nomeSuperior}    UBS    ${EMPTY}    ${cpf}

    ${responseAntes}=    Call GET Get Unidade Saude By Nome    ${nomeUnidade}
    Should Be Equal As Integers    ${responseAntes.status_code}    200
    Validate Object Schema    ${responseAntes.json()}    ${UNIDADE_SAUDE_SCHEMA}
    Should Be Equal As Strings    ${responseAntes.json()['responsavelCpf']}    ${cpf}
    ${responsavelNomeAntes}=    Get From Dictionary    ${responseAntes.json()}    responsavelNome    default=${None}
    Should Be Equal    ${responsavelNomeAntes}    ${None}

    ${suffixProfissional}=    Generate Random String    8    [LETTERS][NUMBERS]
    ${nomeProfissional}=    Set Variable    Robot Teste Profissional Reconciliacao ${suffixProfissional}
    POST Create Profissional - Status Code 201    ${cpf}    ${nomeProfissional}

    ${responseDepois}=    Call GET Get Unidade Saude By Nome    ${nomeUnidade}
    Should Be Equal As Integers    ${responseDepois.status_code}    200
    Validate Object Schema    ${responseDepois.json()}    ${UNIDADE_SAUDE_SCHEMA}
    Should Be Equal As Strings    ${responseDepois.json()['responsavelNome']}    ${nomeProfissional}

    RETURN    ${responseDepois}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
