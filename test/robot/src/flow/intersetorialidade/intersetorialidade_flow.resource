*** Settings ***
Library     String
Library     Collections
Resource    ../../pom/intersetorialidade/intersetorialidade_pom.resource
Resource    ../saude_mental/saude_mental_flow.resource
Resource    ../common/schema_validation_flow.resource
Resource    ../../resource/config/schema/schema.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
    O catálogo de instituições externas e o encaminhamento intersetorial do caso em saúde mental (ADRs 0154 e 0155).
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
Seed Instituicao Externa
    [Documentation]    An active institution with a unique name; returns the uuid.
    [Arguments]    ${prefixo}=CRAS Robot    ${setor}=ASSISTENCIA_SOCIAL    ${tipo}=CRAS
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${sufixo}=    Generate Random String    8    [NUMBERS]
    ${payload}=    Create Dictionary    nome=${prefixo} ${sufixo}    setor=${setor}    tipo=${tipo}    telefone=(11) 3333-0000
    ${r}=    POST Instituicao Externa    ${payload}
    Should Be Equal As Integers    ${r.status_code}    201
    RETURN    ${r.json()}[uuid]
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Encaminhar Caso
    [Documentation]    Refers the case to the institution; returns the response.
    [Arguments]    ${caso}    ${instituicao}    ${enviadoHa}=0    ${prazoEm}=15
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${enviado}=    Evaluate    (datetime.date.today() - datetime.timedelta(days=int(${enviadoHa}))).isoformat()    modules=datetime
    ${prazo}=    Evaluate    (datetime.date.today() + datetime.timedelta(days=int(${prazoEm}))).isoformat()    modules=datetime
    ${payload}=    Create Dictionary    instituicaoId=${instituicao}    motivo=Inclusão no CadÚnico e benefício eventual    enviadoEm=${enviado}    retornoEsperadoEm=${prazo}
    ${r}=    POST Encaminhamento Intersetorial    ${caso}    ${payload}
    RETURN    ${r}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Instituicao - Cadastro - Status Code 201
    [Documentation]    The institution enters the catalog, with the schema.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${sufixo}=    Generate Random String    8    [NUMBERS]
    ${payload}=    Create Dictionary    nome=CREAS Robot ${sufixo}    setor=ASSISTENCIA_SOCIAL    tipo=CREAS    responsavel=Coordenação
    ${r}=    POST Instituicao Externa    ${payload}
    Should Be Equal As Integers    ${r.status_code}    201
    Validate Object Schema    ${r.json()}    ${INSTITUICAO_EXTERNA_SCHEMA}
    Should Be True    ${r.json()}[ativa]
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Instituicao - Nome Repetido - Status Code 409
    [Documentation]    The name is unique in the catalog.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${sufixo}=    Generate Random String    8    [NUMBERS]
    ${payload}=    Create Dictionary    nome=Escola Robot ${sufixo}    setor=EDUCACAO    tipo=ESCOLA
    ${r1}=    POST Instituicao Externa    ${payload}
    Should Be Equal As Integers    ${r1.status_code}    201
    ${r}=    POST Instituicao Externa    ${payload}
    Should Be Equal As Integers    ${r.status_code}    409
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Instituicao - Tipo De Outro Setor - Status Code 422
    [Documentation]    The type tells the sector.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${sufixo}=    Generate Random String    8    [NUMBERS]
    ${payload}=    Create Dictionary    nome=Escola Robot ${sufixo}    setor=ASSISTENCIA_SOCIAL    tipo=ESCOLA
    ${r}=    POST Instituicao Externa    ${payload}
    Should Be Equal As Integers    ${r.status_code}    422
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Instituicao - Inativar - Status Code 200
    [Documentation]    The inactive institution leaves the list of the active ones.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${uuid}=    Seed Instituicao Externa    CT Robot    GARANTIA_DE_DIREITOS    CONSELHO_TUTELAR
    ${sufixo}=    Generate Random String    8    [NUMBERS]
    ${payload}=    Create Dictionary    nome=CT Robot inativo ${sufixo}    setor=GARANTIA_DE_DIREITOS    tipo=CONSELHO_TUTELAR    ativa=${False}
    ${r}=    PUT Instituicao Externa    ${uuid}    ${payload}
    Should Be Equal As Integers    ${r.status_code}    200
    Should Not Be True    ${r.json()}[ativa]
    ${params}=    Create Dictionary    ativas=true
    ${lista}=    GET Instituicoes Externas    ${params}
    Should Be Equal As Integers    ${lista.status_code}    200
    ${ids}=    Evaluate    [i["uuid"] for i in $lista.json()]
    List Should Not Contain Value    ${ids}    ${uuid}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Encaminhamento - Envio - Status Code 201
    [Documentation]    The case is referred; the case comes back with the referral.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${caso}    ${unidade}    ${equipe}    ${matricula}    ${paciente}    ${programa}=    Seed Caso Saude Mental
    ${instituicao}=    Seed Instituicao Externa
    ${r}=    Encaminhar Caso    ${caso}    ${instituicao}
    Should Be Equal As Integers    ${r.status_code}    201
    ${e}=    Set Variable    ${r.json()}[encaminhamentosIntersetoriais][0]
    Validate Object Schema    ${e}    ${ENCAMINHAMENTO_INTERSETORIAL_SCHEMA}
    Should Be Equal    ${e}[situacao]    ENVIADO
    Should Not Be True    ${r.json()}[retornoIntersetorialVencido]
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Encaminhamento - Em Aberto - Status Code 409
    [Documentation]    One open referral per institution in the case.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${caso}    ${unidade}    ${equipe}    ${matricula}    ${paciente}    ${programa}=    Seed Caso Saude Mental
    ${instituicao}=    Seed Instituicao Externa
    Encaminhar Caso    ${caso}    ${instituicao}
    ${r}=    Encaminhar Caso    ${caso}    ${instituicao}
    Should Be Equal As Integers    ${r.status_code}    409
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Encaminhamento - Retorno - Status Code 200
    [Documentation]    Received, then concluded with the return of the institution.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${caso}    ${unidade}    ${equipe}    ${matricula}    ${paciente}    ${programa}=    Seed Caso Saude Mental
    ${instituicao}=    Seed Instituicao Externa
    ${r}=    Encaminhar Caso    ${caso}    ${instituicao}
    ${id}=    Set Variable    ${r.json()}[encaminhamentosIntersetoriais][0][uuid]
    ${vazio}=    Create Dictionary
    ${rec}=    POST Acao Encaminhamento Intersetorial    ${caso}    ${id}    recebimento    ${vazio}
    Should Be Equal As Integers    ${rec.status_code}    200
    Should Be Equal    ${rec.json()}[encaminhamentosIntersetoriais][0][situacao]    RECEBIDO
    ${payload}=    Create Dictionary    desfecho=CONCLUIDO    retorno=Família inscrita; benefício concedido.
    ${ret}=    POST Acao Encaminhamento Intersetorial    ${caso}    ${id}    retorno    ${payload}
    Should Be Equal As Integers    ${ret.status_code}    200
    Should Be Equal    ${ret.json()}[encaminhamentosIntersetoriais][0][situacao]    CONCLUIDO
    Validate Object Schema    ${ret.json()}[encaminhamentosIntersetoriais][0]    ${ENCAMINHAMENTO_INTERSETORIAL_SCHEMA}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Encaminhamento - Retorno Sem Texto - Status Code 400
    [Documentation]    The return needs what the institution said.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${caso}    ${unidade}    ${equipe}    ${matricula}    ${paciente}    ${programa}=    Seed Caso Saude Mental
    ${instituicao}=    Seed Instituicao Externa
    ${r}=    Encaminhar Caso    ${caso}    ${instituicao}
    ${payload}=    Create Dictionary    desfecho=CONCLUIDO
    ${ret}=    POST Acao Encaminhamento Intersetorial    ${caso}    ${r.json()}[encaminhamentosIntersetoriais][0][uuid]    retorno    ${payload}
    Should Be Equal As Integers    ${ret.status_code}    400
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Encaminhamento - Sem Retorno No Prazo - Status Code 200
    [Documentation]    Open past the expected return: a pending item of the case and of the list.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${caso}    ${unidade}    ${equipe}    ${matricula}    ${paciente}    ${programa}=    Seed Caso Saude Mental
    ${instituicao}=    Seed Instituicao Externa
    ${r}=    Encaminhar Caso    ${caso}    ${instituicao}    20    -5
    Should Be Equal As Integers    ${r.status_code}    201
    Should Be True    ${r.json()}[retornoIntersetorialVencido]
    Should Be True    ${r.json()}[encaminhamentosIntersetoriais][0][semRetorno]
    ${filtro}=    Create Dictionary    unidadeId=${unidade}    pendencia=RETORNO_INTERSETORIAL
    ${lista}=    GET Casos Com Filtro    ${filtro}
    Should Be Equal As Integers    ${lista.status_code}    200
    ${ids}=    Evaluate    [c["uuid"] for c in $lista.json()["lista"]]
    List Should Contain Value    ${ids}    ${caso}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Indicadores - Saude Mental - Status Code 200
    [Documentation]    The five indicators of the Mental Health program, in the case's unit.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${caso}    ${unidade}    ${equipe}    ${matricula}    ${paciente}    ${programa}=    Seed Caso Saude Mental
    ${instituicao}=    Seed Instituicao Externa
    Encaminhar Caso    ${caso}    ${instituicao}
    ${params}=    Create Dictionary    unidadeId=${unidade}
    ${r}=    GET Indicadores Do Programa    ${programa}    ${params}
    Should Be Equal As Integers    ${r.status_code}    200
    ${codigos}=    Evaluate    [i["indicador"] for i in $r.json()]
    FOR    ${c}    IN    PTS_EM_DIA    AVALIACAO_EM_30_DIAS    POS_CRISE_EM_7_DIAS    COMUNICACAO_MP_NO_PRAZO    RETORNO_INTERSETORIAL
        List Should Contain Value    ${codigos}    ${c}
    END
    ${retorno}=    Evaluate    [i for i in $r.json() if i["indicador"] == "RETORNO_INTERSETORIAL"][0]
    Should Be Equal As Integers    ${retorno}[numerador]    0
    Should Be Equal As Integers    ${retorno}[denominador]    1
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Intersetorialidade - By HTTP Status Code
    [Documentation]    Executes the IS1/SM6 scenario for the given HTTP status code.
    [Arguments]    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Run Keyword If    '${meuHTTP}' == '201-instituicao'    Instituicao - Cadastro - Status Code 201
    ...    ELSE IF    '${meuHTTP}' == '409-nome-repetido'    Instituicao - Nome Repetido - Status Code 409
    ...    ELSE IF    '${meuHTTP}' == '422-tipo-de-outro-setor'    Instituicao - Tipo De Outro Setor - Status Code 422
    ...    ELSE IF    '${meuHTTP}' == '200-inativar'    Instituicao - Inativar - Status Code 200
    ...    ELSE IF    '${meuHTTP}' == '201-encaminhamento'    Encaminhamento - Envio - Status Code 201
    ...    ELSE IF    '${meuHTTP}' == '409-em-aberto'    Encaminhamento - Em Aberto - Status Code 409
    ...    ELSE IF    '${meuHTTP}' == '200-retorno'    Encaminhamento - Retorno - Status Code 200
    ...    ELSE IF    '${meuHTTP}' == '400-retorno-sem-texto'    Encaminhamento - Retorno Sem Texto - Status Code 400
    ...    ELSE IF    '${meuHTTP}' == '200-sem-retorno'    Encaminhamento - Sem Retorno No Prazo - Status Code 200
    ...    ELSE IF    '${meuHTTP}' == '200-indicadores'    Indicadores - Saude Mental - Status Code 200
    ...    ELSE    Fail    HTTP status code '${meuHTTP}' is not supported for Intersetorialidade test scenario.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
