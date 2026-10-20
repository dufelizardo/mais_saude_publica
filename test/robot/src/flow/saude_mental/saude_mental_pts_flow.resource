*** Settings ***
Library     Collections
Resource    ./saude_mental_flow.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
    Saúde Mental, fatia SM2 (ADR-0121): PTS, intervenções e rede de apoio sobre o caso da SM1.
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
Id Do Tipo De Intervencao
    [Documentation]    Returns the uuid of an intervention type by code.
    [Arguments]    ${codigo}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${response}=    GET Tipos Intervencao Saude Mental
    ${ids}=    Evaluate    [t["uuid"] for t in $response.json() if t["codigo"] == $codigo]
    RETURN    ${ids}[0]
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Saude Mental - PTS Versionado - Status Code 201
    [Documentation]    The first version, then a revision that keeps the previous one.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${caso}    ${unidade}    ${equipe}    ${matricula}    ${paciente}    ${programa}=    Seed Caso Saude Mental
    ${tipo}=    Id Do Tipo De Intervencao    ATENDIMENTO_INDIVIDUAL
    ${objetivo}=    Create Dictionary    descricao=Vínculo com o CAPS    responsavelMatricula=${matricula}
    ${objetivos}=    Create List    ${objetivo}
    ${prevista}=    Create Dictionary    tipoId=${tipo}    frequencia=semanal
    ${previstas}=    Create List    ${prevista}
    ${payload}=    Create Dictionary    necessidades=Vínculo e adesão.    objetivos=${objetivos}    intervencoesPrevistas=${previstas}
    ${v1}=    POST PTS Saude Mental    ${caso}    ${payload}
    Should Be Equal As Integers    ${v1.status_code}    201
    Validate Object Schema    ${v1.json()}[pts][0]    ${PTS_SAUDE_MENTAL_SCHEMA}
    Should Be Equal As Integers    ${v1.json()}[ptsVersao]    1
    ${alcancado}=    Create Dictionary    descricao=Vínculo com o CAPS    situacao=ALCANCADO
    ${novos}=    Create List    ${alcancado}
    ${revisao}=    Create Dictionary    versaoBase=${1}    necessidades=Retorno ao trabalho.    objetivos=${novos}    sinteseRevisao=Vínculo alcançado.
    ${v2}=    POST PTS Saude Mental    ${caso}    ${revisao}
    Should Be Equal As Integers    ${v2.status_code}    201
    Should Be Equal As Integers    ${v2.json()}[ptsVersao]    2
    ${versoes}=    GET PTS Saude Mental    ${caso}
    Length Should Be    ${versoes.json()}    2
    Should Be Equal    ${versoes.json()}[1][objetivos][0][situacao]    EM_ANDAMENTO
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Saude Mental - PTS Concorrente - Status Code 409
    [Documentation]    A revision from an old version is refused.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${caso}    ${unidade}    ${equipe}    ${matricula}    ${paciente}    ${programa}=    Seed Caso Saude Mental
    ${objetivo}=    Create Dictionary    descricao=Vínculo com o CAPS    responsavelMatricula=${matricula}
    ${objetivos}=    Create List    ${objetivo}
    ${payload}=    Create Dictionary    necessidades=Vínculo.    objetivos=${objetivos}
    ${v1}=    POST PTS Saude Mental    ${caso}    ${payload}
    Should Be Equal As Integers    ${v1.status_code}    201
    ${outra}=    POST PTS Saude Mental    ${caso}    ${payload}
    Should Be Equal As Integers    ${outra.status_code}    409
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Saude Mental - Revisao Sem Sintese - Status Code 400
    [Documentation]    The revision needs the synthesis.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${caso}    ${unidade}    ${equipe}    ${matricula}    ${paciente}    ${programa}=    Seed Caso Saude Mental
    ${objetivo}=    Create Dictionary    descricao=Vínculo com o CAPS    responsavelMatricula=${matricula}
    ${objetivos}=    Create List    ${objetivo}
    ${payload}=    Create Dictionary    necessidades=Vínculo.    objetivos=${objetivos}
    ${v1}=    POST PTS Saude Mental    ${caso}    ${payload}
    ${revisao}=    Create Dictionary    versaoBase=${1}    necessidades=Vínculo.    objetivos=${objetivos}
    ${response}=    POST PTS Saude Mental    ${caso}    ${revisao}
    Should Be Equal As Integers    ${response.status_code}    400
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Saude Mental - Responsavel Fora Da Referencia - Status Code 422
    [Documentation]    The responsible must be from the reference team.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${caso}    ${unidade}    ${equipe}    ${matricula}    ${paciente}    ${programa}=    Seed Caso Saude Mental
    ${fora}=    Seed A Profissional Matricula
    ${objetivo}=    Create Dictionary    descricao=Vínculo    responsavelMatricula=${fora}
    ${objetivos}=    Create List    ${objetivo}
    ${payload}=    Create Dictionary    necessidades=Vínculo.    objetivos=${objetivos}
    ${response}=    POST PTS Saude Mental    ${caso}    ${payload}
    Should Be Equal As Integers    ${response.status_code}    422
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Saude Mental - Intervencao - Status Code 201
    [Documentation]    Registers interventions; two absences in a row are counted.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${caso}    ${unidade}    ${equipe}    ${matricula}    ${paciente}    ${programa}=    Seed Caso Saude Mental
    ${tipo}=    Id Do Tipo De Intervencao    VISITA_DOMICILIAR
    ${feita}=    Create Dictionary    tipoId=${tipo}    resumo=Visita com a família.
    ${r1}=    POST Intervencao Saude Mental    ${caso}    ${feita}
    Should Be Equal As Integers    ${r1.status_code}    201
    Validate Object Schema    ${r1.json()}[intervencoes][0]    ${INTERVENCAO_SAUDE_MENTAL_SCHEMA}
    ${falta}=    Create Dictionary    tipoId=${tipo}    resumo=Não estava em casa.    situacao=NAO_COMPARECEU
    POST Intervencao Saude Mental    ${caso}    ${falta}
    ${r3}=    POST Intervencao Saude Mental    ${caso}    ${falta}
    Should Be Equal As Integers    ${r3.json()}[faltasSeguidas]    2
    ${sem}=    Create Dictionary    tipoId=${tipo}
    ${r400}=    POST Intervencao Saude Mental    ${caso}    ${sem}
    Should Be Equal As Integers    ${r400.status_code}    400
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Saude Mental - Rede De Apoio - Status Code 200
    [Documentation]    Adds and removes a person; the history stays.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${caso}    ${unidade}    ${equipe}    ${matricula}    ${paciente}    ${programa}=    Seed Caso Saude Mental
    ${pessoa}=    Create Dictionary    nome=Ana    papel=FAMILIAR    vinculo=irmã
    ${r1}=    POST Rede Apoio Saude Mental    ${caso}    ${pessoa}
    Should Be Equal As Integers    ${r1.status_code}    201
    Validate Object Schema    ${r1.json()}[redeApoio][0]    ${PESSOA_REDE_APOIO_SCHEMA}
    ${motivo}=    Create Dictionary    motivo=Mudou de cidade.
    ${r2}=    POST Encerrar Rede Apoio Saude Mental    ${caso}    ${r1.json()}[redeApoio][0][uuid]    ${motivo}
    Should Be Equal As Integers    ${r2.status_code}    200
    Should Be Equal    ${r2.json()}[redeApoio][0][motivoFim]    Mudou de cidade.
    ${r3}=    POST Encerrar Rede Apoio Saude Mental    ${caso}    ${r1.json()}[redeApoio][0][uuid]    ${motivo}
    Should Be Equal As Integers    ${r3.status_code}    422
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Saude Mental - Tipos De Intervencao - Status Code 200
    [Documentation]    The catalog brings the eight seeded types.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${response}=    GET Tipos Intervencao Saude Mental
    Should Be Equal As Integers    ${response.status_code}    200
    ${codigos}=    Evaluate    [t["codigo"] for t in $response.json()]
    Should Contain    ${codigos}    ATENDIMENTO_INDIVIDUAL
    Should Contain    ${codigos}    ORIENTACAO
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Saude Mental PTS - By HTTP Status Code
    [Documentation]    Executes the SM2 scenario for the given HTTP status code.
    [Arguments]    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Run Keyword If    '${meuHTTP}' == '201-pts'    Saude Mental - PTS Versionado - Status Code 201
    ...    ELSE IF    '${meuHTTP}' == '409-pts'    Saude Mental - PTS Concorrente - Status Code 409
    ...    ELSE IF    '${meuHTTP}' == '400-pts'    Saude Mental - Revisao Sem Sintese - Status Code 400
    ...    ELSE IF    '${meuHTTP}' == '422-pts'    Saude Mental - Responsavel Fora Da Referencia - Status Code 422
    ...    ELSE IF    '${meuHTTP}' == '201-intervencao'    Saude Mental - Intervencao - Status Code 201
    ...    ELSE IF    '${meuHTTP}' == '200-rede'    Saude Mental - Rede De Apoio - Status Code 200
    ...    ELSE IF    '${meuHTTP}' == '200-tipos'    Saude Mental - Tipos De Intervencao - Status Code 200
    ...    ELSE    Fail    HTTP status code '${meuHTTP}' is not supported for Saude Mental PTS test scenario.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
