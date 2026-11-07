*** Settings ***
Library     String
Library     Collections
Resource    ../../../pom/equipamentos/servicos_unidade/servicos_unidade_pom.resource
Resource    ../modelo_operacional/modelo_operacional_flow.resource
Resource    ../../setor/create_setor/create_setor_flow.resource
Resource    ../../internacao/leito_internacao/leito_internacao_flow.resource
Resource    ../../common/schema_validation_flow.resource
Resource    ../../../resource/config/schema/schema.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
Id Do Tipo De Servico
    [Documentation]    Returns the uuid of a service type by code.
    [Arguments]    ${codigo}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${response}=    GET Tipos De Servico
    ${ids}=    Evaluate    [t["uuid"] for t in $response.json() if t["codigo"] == $codigo]
    RETURN    ${ids}[0]
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Seed Hospital Com UTI
    [Documentation]    Creates a hospital with the UTI capability and returns its uuid.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${hospital}=    Seed Unidade Do Tipo    HOSPITAL
    ${perfil}=    Incluir Perfil Por Codigo    ${hospital}    INTERNACAO
    Should Be Equal As Integers    ${perfil.status_code}    200
    ${capacidade}=    Incluir Capacidade Por Codigo    ${hospital}    UTI
    Should Be Equal As Integers    ${capacidade.status_code}    200
    RETURN    ${hospital}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Seed Setor Na Unidade
    [Documentation]    Creates a care sector in the unit and returns its uuid.
    [Arguments]    ${unidade}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${sufixo}=    Generate Random String    8    [LETTERS][NUMBERS]
    ${payload}=    Build Setor Payload    ${unidade}    Robot Setor ${sufixo}    ROBOT-${sufixo}    ASSISTENCIAL
    ${response}=    POST Create Setor    ${payload}
    Should Be Equal As Integers    ${response.status_code}    201
    ${uuid}=    Buscar Setor Uuid Por Codigo    ROBOT-${sufixo}
    RETURN    ${uuid}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Incluir Servico Do Tipo
    [Documentation]    Adds a 24-hour service of the given type to the unit and returns the response.
    [Arguments]    ${unidade}    ${codigo}    ${nome}    @{setores}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${tipo}=    Id Do Tipo De Servico    ${codigo}
    ${ids}=    Create List    @{setores}
    ${payload}=    Create Dictionary    tipoId=${tipo}    nome=${nome}    vinteQuatroHoras=${True}    setorIds=${ids}
    ${response}=    POST Incluir Servico    ${unidade}    ${payload}
    RETURN    ${response}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Servicos - Catalogo - Status Code 200
    [Documentation]    The initial load brings the types, with the required capability.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${response}=    GET Tipos De Servico
    Should Be Equal As Integers    ${response.status_code}    200
    Validate Array Schema    ${response.json()}    ${TIPO_SERVICO_SCHEMA}
    ${codigos}=    Evaluate    [t["codigo"] for t in $response.json()]
    FOR    ${codigo}    IN    PRONTO_SOCORRO    UTI    INTERNACAO_CLINICA    CENTRO_CIRURGICO    CENTRO_OBSTETRICO    SALA_DE_VACINA
    ...    AMBULATORIO    ODONTOLOGIA    FARMACIA    LABORATORIO    DIAGNOSTICO_POR_IMAGEM
        Should Contain    ${codigos}    ${codigo}
    END
    ${uti}=    Evaluate    [t for t in $response.json() if t["codigo"] == "UTI"][0]
    Should Be Equal    ${uti}[capacidadeExigidaNome]    Terapia intensiva (UTI)
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Servicos - Tipo - Status Code 201 E 409
    [Documentation]    Creates a type; a repeated code is refused.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${sufixo}=    Generate Random String    8    [UPPER]
    ${payload}=    Create Dictionary    codigo=ROBOT_MO2_${sufixo}    nome=Banco de leite ${sufixo}
    ${response}=    POST Create Tipo De Servico    ${payload}
    Should Be Equal As Integers    ${response.status_code}    201
    Validate Object Schema    ${response.json()}    ${TIPO_SERVICO_SCHEMA}
    ${repetido}=    POST Create Tipo De Servico    ${payload}
    Should Be Equal As Integers    ${repetido.status_code}    409
    ${alterado}=    Create Dictionary    codigo=ROBOT_MO2_${sufixo}    nome=Banco de leite humano ${sufixo}    ativo=${False}
    ${edicao}=    PATCH Tipo De Servico    ${response.json()}[uuid]    ${alterado}
    Should Be Equal As Integers    ${edicao.status_code}    200
    Should Be Equal    ${edicao.json()}[ativo]    ${False}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Servicos - Unidade - Status Code 200
    [Documentation]    A service with a sector shows the beds that come from it.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${hospital}=    Seed Hospital Com UTI
    ${setor}=    Seed Setor Na Unidade    ${hospital}
    ${livre}=    Seed Setor Na Unidade    ${hospital}
    Seed Um Leito No Setor    ${hospital}    ${setor}    MISTO
    ${response}=    Incluir Servico Do Tipo    ${hospital}    UTI    UTI Adulto    ${setor}
    Should Be Equal As Integers    ${response.status_code}    200
    Validate Object Schema    ${response.json()}    ${SERVICOS_DA_UNIDADE_SCHEMA}
    ${servico}=    Set Variable    ${response.json()}[servicos][0]
    Should Be Equal    ${servico}[tipoCodigo]    UTI
    Should Be Equal As Integers    ${servico}[leitos]    1
    Should Be Equal As Integers    ${servico}[leitosLivres]    1
    Should Be Equal    ${response.json()}[setoresLivres][0][uuid]    ${livre}
    ${lista}=    GET Servicos Da Unidade    ${hospital}
    Should Be Equal As Integers    ${lista.status_code}    200
    Length Should Be    ${lista.json()}[servicos]    1
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Servicos - Sem Capacidade - Status Code 422
    [Documentation]    A type that requires a capability the unit lacks is refused.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${hospital}=    Seed Hospital Com UTI
    ${response}=    Incluir Servico Do Tipo    ${hospital}    CENTRO_CIRURGICO    Centro cirúrgico
    Should Be Equal As Integers    ${response.status_code}    422
    Should Contain    ${response.json()}[message]    Cirurgia
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Servicos - Setor Ocupado - Status Code 409
    [Documentation]    A sector stays in one current service at a time.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${hospital}=    Seed Hospital Com UTI
    ${setor}=    Seed Setor Na Unidade    ${hospital}
    ${primeiro}=    Incluir Servico Do Tipo    ${hospital}    UTI    UTI Adulto    ${setor}
    Should Be Equal As Integers    ${primeiro.status_code}    200
    ${segundo}=    Incluir Servico Do Tipo    ${hospital}    UTI    UTI Pediátrica    ${setor}
    Should Be Equal As Integers    ${segundo.status_code}    409
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Servicos - Tipo Inexistente - Status Code 404
    [Documentation]    An unknown type is not found.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${hospital}=    Seed Hospital Com UTI
    ${tipo}=    Evaluate    str(__import__("uuid").uuid4())
    ${payload}=    Create Dictionary    tipoId=${tipo}    nome=Sem tipo
    ${response}=    POST Incluir Servico    ${hospital}    ${payload}
    Should Be Equal As Integers    ${response.status_code}    404
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Servicos - Encerramento - Status Code 422 E 200
    [Documentation]    The capability only ends after the service; the service stays in the history.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${hospital}=    Seed Hospital Com UTI
    ${incluido}=    Incluir Servico Do Tipo    ${hospital}    UTI    UTI Adulto
    ${servico}=    Set Variable    ${incluido.json()}[servicos][0][uuid]
    ${modelo}=    GET Modelo Operacional Unidade    ${hospital}
    ${associacao}=    Associacao Por Codigo    ${modelo.json()}    capacidades    UTI
    ${motivo}=    Create Dictionary    motivo=Fim da habilitação
    ${bloqueada}=    POST Encerrar Na Unidade    ${hospital}    capacidade    ${associacao}    ${motivo}
    Should Be Equal As Integers    ${bloqueada.status_code}    422
    Should Contain    ${bloqueada.json()}[message]    UTI Adulto
    ${encerrado}=    POST Encerrar Servico    ${hospital}    ${servico}    ${motivo}
    Should Be Equal As Integers    ${encerrado.status_code}    200
    Length Should Be    ${encerrado.json()}[servicos]    0
    Should Be Equal    ${encerrado.json()}[encerrados][0][motivoFim]    Fim da habilitação
    ${liberada}=    POST Encerrar Na Unidade    ${hospital}    capacidade    ${associacao}    ${motivo}
    Should Be Equal As Integers    ${liberada.status_code}    200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Servicos Unidade - By HTTP Status Code
    [Documentation]    Dispatches the scenario by HTTP status code.
    [Arguments]    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Run Keyword If    '${meuHTTP}' == '200-catalogo'    Servicos - Catalogo - Status Code 200
    Run Keyword If    '${meuHTTP}' == '201-tipo'    Servicos - Tipo - Status Code 201 E 409
    Run Keyword If    '${meuHTTP}' == '200-unidade'    Servicos - Unidade - Status Code 200
    Run Keyword If    '${meuHTTP}' == '422-capacidade'    Servicos - Sem Capacidade - Status Code 422
    Run Keyword If    '${meuHTTP}' == '409-setor'    Servicos - Setor Ocupado - Status Code 409
    Run Keyword If    '${meuHTTP}' == '404-tipo'    Servicos - Tipo Inexistente - Status Code 404
    Run Keyword If    '${meuHTTP}' == '422-encerramento'    Servicos - Encerramento - Status Code 422 E 200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

