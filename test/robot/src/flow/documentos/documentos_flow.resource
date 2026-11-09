*** Settings ***
Library     String
Library     Collections
Resource    ../../pom/documentos/documentos_pom.resource
Resource    ../paciente/create_paciente/create_paciente_flow.resource
Resource    ../setor/create_setor/create_setor_flow.resource
Resource    ../common/schema_validation_flow.resource
Resource    ../../resource/config/schema/schema.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
Arquivo Pdf De Teste
    [Documentation]    Builds a small PDF upload.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${arquivos}=    Evaluate    {'arquivo': ('exame.pdf', b'%PDF-1.4 teste', 'application/pdf')}
    RETURN    ${arquivos}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Dados Do Documento
    [Documentation]    Builds the form fields of an attachment.
    [Arguments]    ${titulo}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${tipos}=    GET Tipos De Documento
    ${tipo}=    Evaluate    [t["uuid"] for t in $tipos.json() if t["codigo"] == "LAUDO"][0]
    ${unidade}=    Seed A Unidade De Saude Uuid
    ${dados}=    Create Dictionary    tipoId=${tipo}    unidadeId=${unidade}    titulo=${titulo}
    RETURN    ${dados}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Documentos - Catalogo - Status Code 200
    [Documentation]    The initial load of the types.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${response}=    GET Tipos De Documento
    Should Be Equal As Integers    ${response.status_code}    200
    ${codigos}=    Evaluate    [t["codigo"] for t in $response.json()]
    Should Contain    ${codigos}    EXAME_EXTERNO
    Should Contain    ${codigos}    TERMO_CONSENTIMENTO
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Documentos - Anexar E Abrir - Status Code 200
    [Documentation]    Attaches, opens as an attachment and keeps the versions.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${paciente}    ${cpf}    ${cartao}    ${nome}=    Seed A Paciente
    ${dados}=    Dados Do Documento    Hemograma
    ${arquivos}=    Arquivo Pdf De Teste
    ${response}=    POST Anexar Documento    ${paciente}    ${dados}    ${arquivos}
    Should Be Equal As Integers    ${response.status_code}    200
    Validate Object Schema    ${response.json()}    ${DOCUMENTOS_DO_PACIENTE_SCHEMA}
    ${documento}=    Set Variable    ${response.json()}[documentos][0]
    ${arquivo}=    GET Arquivo Da Versao    ${documento}[versoes][0][uuid]
    Should Be Equal As Integers    ${arquivo.status_code}    200
    Should Contain    ${arquivo.headers}[Content-Disposition]    attachment
    Should Be Equal    ${arquivo.headers}[X-Content-Type-Options]    nosniff
    ${versao}=    POST Nova Versao Do Documento    ${documento}[uuid]    ${arquivos}
    Should Be Equal As Integers    ${versao.status_code}    200
    Length Should Be    ${versao.json()}[documentos][0][versoes]    2
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Documentos - Arquivo Falso - Status Code 400
    [Documentation]    The type comes from the content, not from the extension.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${paciente}    ${cpf}    ${cartao}    ${nome}=    Seed A Paciente
    ${dados}=    Dados Do Documento    Falso
    ${arquivos}=    Evaluate    {'arquivo': ('falso.pdf', b'<html></html>', 'application/pdf')}
    ${response}=    POST Anexar Documento    ${paciente}    ${dados}    ${arquivos}
    Should Be Equal As Integers    ${response.status_code}    400
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Documentos - Retirada - Status Code 200 E 422
    [Documentation]    A withdrawn document leaves the list and no longer opens.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${paciente}    ${cpf}    ${cartao}    ${nome}=    Seed A Paciente
    ${dados}=    Dados Do Documento    Errado
    ${arquivos}=    Arquivo Pdf De Teste
    ${anexado}=    POST Anexar Documento    ${paciente}    ${dados}    ${arquivos}
    ${documento}=    Set Variable    ${anexado.json()}[documentos][0]
    ${motivo}=    Create Dictionary    motivo=Paciente errado
    ${retirado}=    POST Retirar Documento    ${documento}[uuid]    ${motivo}
    Should Be Equal As Integers    ${retirado.status_code}    200
    Length Should Be    ${retirado.json()}[documentos]    0
    Should Be Equal    ${retirado.json()}[retirados][0][motivoRetirada]    Paciente errado
    ${arquivo}=    GET Arquivo Da Versao    ${documento}[versoes][0][uuid]
    Should Be Equal As Integers    ${arquivo.status_code}    422
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Documentos - Paciente Inexistente - Status Code 404
    [Documentation]    An unknown patient is not found.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${id}=    Evaluate    str(__import__("uuid").uuid4())
    ${response}=    GET Documentos Do Paciente    ${id}
    Should Be Equal As Integers    ${response.status_code}    404
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Documentos - By HTTP Status Code
    [Documentation]    Dispatches the scenario by HTTP status code.
    [Arguments]    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Run Keyword If    '${meuHTTP}' == '200-catalogo'    Documentos - Catalogo - Status Code 200
    Run Keyword If    '${meuHTTP}' == '200-anexar'    Documentos - Anexar E Abrir - Status Code 200
    Run Keyword If    '${meuHTTP}' == '400-arquivo'    Documentos - Arquivo Falso - Status Code 400
    Run Keyword If    '${meuHTTP}' == '200-retirada'    Documentos - Retirada - Status Code 200 E 422
    Run Keyword If    '${meuHTTP}' == '404-paciente'    Documentos - Paciente Inexistente - Status Code 404
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

