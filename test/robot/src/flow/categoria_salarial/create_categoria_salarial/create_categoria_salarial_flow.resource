*** Settings ***
Library    String
Resource    ../../../pom/categoria_salarial/create_categoria_salarial/create_categoria_salarial_pom.resource
Resource    ../../../pom/categoria_salarial/get_all_categorias_salariais/get_all_categorias_salariais_pom.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
    CategoriaSalarial (módulo RH — ver docs/rh/MODELO-RH.md) não tem suíte Robot própria ainda;
    este flow existe só para seedar uma categoria a partir do domínio Administrativo (NecessidadeDePessoal
    precisa de um Cargo, que por sua vez precisa de uma CategoriaSalarial). Mesma técnica de resolução de
    uuid via list+filter usada por Setor/ProcessoAdministrativo, já que o create só retorna
    SuccessResponseDto.
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
Seed A Categoria Salarial Uuid
    [Documentation]    Seeds a fresh Categoria Salarial with a random suffix on the nome and returns its uuid.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${suffix}=    Generate Random String    8    [LETTERS][NUMBERS]
    ${nome}=    Set Variable    Robot Teste Categoria ${suffix}
    ${payload}=    Create Dictionary
    ...    nome=${nome}

    ${response}=    POST Create Categoria Salarial    ${payload}
    Should Be Equal As Integers    ${response.status_code}    201

    ${listResponse}=    GET Get All Categorias Salariais
    Should Be Equal As Integers    ${listResponse.status_code}    200
    ${uuid}=    Evaluate    [c['uuid'] for c in $listResponse.json() if c['nome'] == '${nome}'][0]

    RETURN    ${uuid}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
