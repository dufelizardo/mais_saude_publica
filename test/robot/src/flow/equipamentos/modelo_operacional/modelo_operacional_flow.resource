*** Settings ***
Library     String
Library     Collections
Resource    ../../../pom/equipamentos/modelo_operacional/modelo_operacional_pom.resource
Resource    ../../../pom/equipamentos/rede_unidades/rede_unidades_pom.resource
Resource    ../../common/schema_validation_flow.resource
Resource    ../../../resource/config/schema/schema.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
Seed Unidade Do Tipo
    [Documentation]    Creates a unit of the given type with a random name and returns its uuid.
    [Arguments]    ${tipo}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${sufixo}=    Generate Random String    8    [LETTERS][NUMBERS]
    ${payload}=    Create Dictionary    nome=Robot ${tipo} ModeloOp ${sufixo}    tipo=${tipo}
    ${response}=    POST Create Unidade Por Id    ${payload}
    Should Be Equal As Integers    ${response.status_code}    201
    RETURN    ${response.json()}[resumo][uuid]
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Id Do Perfil
    [Documentation]    Returns the uuid of a catalog profile by code.
    [Arguments]    ${codigo}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${response}=    GET Perfis Operacionais
    ${ids}=    Evaluate    [p["uuid"] for p in $response.json() if p["codigo"] == $codigo]
    RETURN    ${ids}[0]
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Id Da Capacidade
    [Documentation]    Returns the uuid of a catalog capability by code.
    [Arguments]    ${codigo}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${response}=    GET Capacidades Assistenciais
    ${ids}=    Evaluate    [c["uuid"] for c in $response.json() if c["codigo"] == $codigo]
    RETURN    ${ids}[0]
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Incluir Perfil Por Codigo
    [Documentation]    Adds a profile by code to the unit and returns the response.
    [Arguments]    ${unidade}    ${codigo}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${perfil}=    Id Do Perfil    ${codigo}
    ${payload}=    Create Dictionary    perfilId=${perfil}
    ${response}=    POST Incluir Perfil Na Unidade    ${unidade}    ${payload}
    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Incluir Capacidade Por Codigo
    [Documentation]    Adds a capability by code to the unit and returns the response.
    [Arguments]    ${unidade}    ${codigo}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${capacidade}=    Id Da Capacidade    ${codigo}
    ${payload}=    Create Dictionary    capacidadeId=${capacidade}
    ${response}=    POST Incluir Capacidade Na Unidade    ${unidade}    ${payload}
    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Associacao Por Codigo
    [Documentation]    Returns the association id with the given code from a list of the operational model.
    [Arguments]    ${modelo}    ${lista}    ${codigo}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${ids}=    Evaluate    [a["uuid"] for a in $modelo[$lista] if a["codigo"] == $codigo]
    RETURN    ${ids}[0]
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Modelo - Catalogo - Status Code 200
    [Documentation]    The initial load brings the ten profiles and the grouped capabilities.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${response}=    GET Perfis Operacionais
    Should Be Equal As Integers    ${response.status_code}    200
    Validate Array Schema    ${response.json()}    ${PERFIL_OPERACIONAL_SCHEMA}
    ${codigos}=    Evaluate    [p["codigo"] for p in $response.json()]
    FOR    ${codigo}    IN    ATENCAO_PRIMARIA    URGENCIA    INTERNACAO    ATENCAO_ESPECIALIZADA    ATENCAO_PSICOSSOCIAL
    ...    DIAGNOSTICO    ASSISTENCIA_FARMACEUTICA    VIGILANCIA    REABILITACAO    ATENCAO_MOVEL
        Should Contain    ${codigos}    ${codigo}
    END
    ${capacidades}=    GET Capacidades Assistenciais
    Should Be Equal As Integers    ${capacidades.status_code}    200
    Validate Array Schema    ${capacidades.json()}    ${CAPACIDADE_ASSISTENCIAL_SCHEMA}
    ${capsAd}=    Evaluate    [c for c in $capacidades.json() if c["codigo"] == "CAPS_AD"][0]
    Should Be Equal    ${capsAd}[perfilCodigo]    ATENCAO_PSICOSSOCIAL
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Modelo - Unidade - Status Code 200
    [Documentation]    Profile, group capability and cross-cutting capability in a CAPS; the network shows the profile.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${caps}=    Seed Unidade Do Tipo    CAPS
    ${perfil}=    Incluir Perfil Por Codigo    ${caps}    ATENCAO_PSICOSSOCIAL
    Should Be Equal As Integers    ${perfil.status_code}    200
    ${capacidade}=    Incluir Capacidade Por Codigo    ${caps}    CAPS_AD
    Should Be Equal As Integers    ${capacidade.status_code}    200
    ${transversal}=    Incluir Capacidade Por Codigo    ${caps}    ACOLHIMENTO
    Should Be Equal As Integers    ${transversal.status_code}    200
    ${response}=    GET Modelo Operacional Unidade    ${caps}
    Should Be Equal As Integers    ${response.status_code}    200
    Validate Object Schema    ${response.json()}    ${MODELO_OPERACIONAL_UNIDADE_SCHEMA}
    Length Should Be    ${response.json()}[perfis]    1
    Length Should Be    ${response.json()}[capacidades]    2
    ${rede}=    GET Rede Unidades
    ${perfis}=    Evaluate    [u["perfis"] for u in $rede.json()["rede"] if u["uuid"] == $caps][0]
    Should Contain    ${perfis}    ATENCAO_PSICOSSOCIAL
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Modelo - Capacidade Sem Perfil - Status Code 422
    [Documentation]    A group capability requires the group profile in the unit.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${ubs}=    Seed Unidade Do Tipo    UBS
    ${response}=    Incluir Capacidade Por Codigo    ${ubs}    UTI
    Should Be Equal As Integers    ${response.status_code}    422
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Modelo - Perfil Repetido - Status Code 409
    [Documentation]    The same profile cannot be in force twice in the unit.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${ubs}=    Seed Unidade Do Tipo    UBS
    ${primeiro}=    Incluir Perfil Por Codigo    ${ubs}    ATENCAO_PRIMARIA
    Should Be Equal As Integers    ${primeiro.status_code}    200
    ${repetido}=    Incluir Perfil Por Codigo    ${ubs}    ATENCAO_PRIMARIA
    Should Be Equal As Integers    ${repetido.status_code}    409
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Modelo - Encerramento - Status Code 422 E 200
    [Documentation]    The profile only ends after its group capabilities; both stay in the history.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${caps}=    Seed Unidade Do Tipo    CAPS
    ${perfil}=    Incluir Perfil Por Codigo    ${caps}    ATENCAO_PSICOSSOCIAL
    ${capacidade}=    Incluir Capacidade Por Codigo    ${caps}    CAPS_II
    ${idPerfil}=    Associacao Por Codigo    ${capacidade.json()}    perfis    ATENCAO_PSICOSSOCIAL
    ${idCapacidade}=    Associacao Por Codigo    ${capacidade.json()}    capacidades    CAPS_II
    ${motivo}=    Create Dictionary    motivo=Reclassificação da habilitação
    ${recusado}=    POST Encerrar Na Unidade    ${caps}    perfil    ${idPerfil}    ${motivo}
    Should Be Equal As Integers    ${recusado.status_code}    422
    ${semCapacidade}=    POST Encerrar Na Unidade    ${caps}    capacidade    ${idCapacidade}    ${motivo}
    Should Be Equal As Integers    ${semCapacidade.status_code}    200
    ${response}=    POST Encerrar Na Unidade    ${caps}    perfil    ${idPerfil}    ${motivo}
    Should Be Equal As Integers    ${response.status_code}    200
    Length Should Be    ${response.json()}[perfis]    0
    Length Should Be    ${response.json()}[perfisEncerrados]    1
    Length Should Be    ${response.json()}[capacidadesEncerradas]    1
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Modelo - Nivel De Gestao - Status Code 422
    [Documentation]    Management levels (federal to regional) do not get profiles.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${estado}=    Seed Unidade Do Tipo    ESTADUAL
    ${response}=    Incluir Perfil Por Codigo    ${estado}    ATENCAO_PRIMARIA
    Should Be Equal As Integers    ${response.status_code}    422
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Modelo - Catalogo - Status Code 409
    [Documentation]    A catalog code cannot repeat.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${response}=    POST Create Perfil Operacional    ${{{'codigo': 'ATENCAO_PRIMARIA', 'nome': 'Repetido'}}}
    Should Be Equal As Integers    ${response.status_code}    409
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Modelo - Unidade - Status Code 404
    [Documentation]    Operational model of a unit that does not exist.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${uuid}=    Evaluate    str(uuid.uuid4())    modules=uuid
    ${response}=    GET Modelo Operacional Unidade    ${uuid}
    Should Be Equal As Integers    ${response.status_code}    404
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Modelo Operacional - By HTTP Status Code
    [Documentation]    Executes the operational model scenario for the given HTTP status code.
    [Arguments]    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Run Keyword If    '${meuHTTP}' == '200-catalogo'    Modelo - Catalogo - Status Code 200
    ...    ELSE IF    '${meuHTTP}' == '200-unidade'    Modelo - Unidade - Status Code 200
    ...    ELSE IF    '${meuHTTP}' == '422-grupo'    Modelo - Capacidade Sem Perfil - Status Code 422
    ...    ELSE IF    '${meuHTTP}' == '409-perfil'    Modelo - Perfil Repetido - Status Code 409
    ...    ELSE IF    '${meuHTTP}' == '200-encerramento'    Modelo - Encerramento - Status Code 422 E 200
    ...    ELSE IF    '${meuHTTP}' == '422-gestao'    Modelo - Nivel De Gestao - Status Code 422
    ...    ELSE IF    '${meuHTTP}' == '409-catalogo'    Modelo - Catalogo - Status Code 409
    ...    ELSE IF    '${meuHTTP}' == '404'    Modelo - Unidade - Status Code 404
    ...    ELSE    Fail    HTTP status code '${meuHTTP}' is not supported for Modelo Operacional test scenario.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
