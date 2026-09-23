*** Settings ***
Library    String
Resource    ../../../pom/cargo/create_cargo/create_cargo_pom.resource
Resource    ../../../pom/cargo/get_all_cargos/get_all_cargos_pom.resource
Resource    ../../categoria_salarial/create_categoria_salarial/create_categoria_salarial_flow.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
    Cargo (módulo RH — ver docs/rh/MODELO-RH.md) não tem suíte Robot própria ainda; este flow existe só
    para seedar um cargo a partir do domínio Administrativo (NecessidadeDePessoal referencia Cargo, ver
    ADR-0036). Mesma técnica de resolução de uuid via list+filter usada por Setor/ProcessoAdministrativo.
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
Seed A Cargo Uuid
    [Documentation]    Seeds a fresh Categoria Salarial and a fresh Cargo pointing at it, with a random suffix
    ...    on the nome, and returns the Cargo's uuid.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${categoriaId}=    Seed A Categoria Salarial Uuid
    ${suffix}=    Generate Random String    8    [LETTERS][NUMBERS]
    ${nome}=    Set Variable    Robot Teste Cargo ${suffix}
    ${payload}=    Create Dictionary
    ...    categoriaId=${categoriaId}
    ...    nome=${nome}

    ${response}=    POST Create Cargo    ${payload}
    Should Be Equal As Integers    ${response.status_code}    201

    ${listResponse}=    GET Get All Cargos
    Should Be Equal As Integers    ${listResponse.status_code}    200
    ${uuid}=    Evaluate    [c['uuid'] for c in $listResponse.json() if c['nome'] == '${nome}'][0]

    RETURN    ${uuid}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
