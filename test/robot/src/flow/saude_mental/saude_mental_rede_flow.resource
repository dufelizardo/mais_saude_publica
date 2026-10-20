*** Settings ***
Library     Collections
Resource    ./saude_mental_pts_flow.resource
Resource    ../procedimento_regulado/create_procedimento_regulado/create_procedimento_regulado_flow.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
    Saúde Mental, fatia SM4 (ADR-0122): encaminhamento pela Regulação, cuidado compartilhado, matriciamento e transferência.
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
Seed Destino De Transferencia
    [Documentation]    A second unit offering the program, with a team and a member; returns the unit, team and registration.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${unidade}    ${equipe}    ${matricula}    ${paciente}    ${programa}    ${nomePaciente}=    Seed Base Saude Mental
    RETURN    ${unidade}    ${equipe}    ${matricula}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Saude Mental - Encaminhamento - Status Code 201
    [Documentation]    Refers through the Regulation; the request shows in the case.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${caso}    ${unidade}    ${equipe}    ${matricula}    ${paciente}    ${programa}=    Seed Caso Saude Mental
    ${procedimento}    ${nomeProcedimento}=    Seed A Procedimento Regulado
    ${payload}=    Create Dictionary    procedimentoId=${procedimento}    cid=F32.1    prioridade=VERDE    profissionalMatricula=${matricula}    justificativa=Episódio depressivo sem resposta ao manejo.
    ${response}=    POST Encaminhamento Saude Mental    ${caso}    ${payload}
    Should Be Equal As Integers    ${response.status_code}    201
    Validate Object Schema    ${response.json()}[encaminhamentos][0]    ${ENCAMINHAMENTO_SAUDE_MENTAL_SCHEMA}
    Should Be Equal    ${response.json()}[encaminhamentos][0][status]    SOLICITADA
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Saude Mental - Vincular De Outro Paciente - Status Code 422
    [Documentation]    A request of another patient cannot be linked.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${caso}    ${unidade}    ${equipe}    ${matricula}    ${paciente}    ${programa}=    Seed Caso Saude Mental
    ${procedimento}    ${nomeProcedimento}=    Seed A Procedimento Regulado
    ${payload}=    Create Dictionary    procedimentoId=${procedimento}    cid=F32.1    prioridade=VERDE    profissionalMatricula=${matricula}    justificativa=Episódio depressivo sem resposta ao manejo.
    ${r}=    POST Encaminhamento Saude Mental    ${caso}    ${payload}
    ${outro}    ${u2}    ${e2}    ${m2}    ${p2}    ${pr2}=    Seed Caso Saude Mental
    ${vinculo}=    Create Dictionary    solicitacaoId=${r.json()}[encaminhamentos][0][solicitacaoId]
    ${response}=    POST Encaminhamento Saude Mental    ${outro}    ${vinculo}
    Should Be Equal As Integers    ${response.status_code}    422
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Saude Mental - Compartilhamento - Status Code 201
    [Documentation]    Shares the care; the same team again is refused; ending keeps the history.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${caso}    ${unidade}    ${equipe}    ${matricula}    ${paciente}    ${programa}=    Seed Caso Saude Mental
    ${parceira}    ${nomeParceira}    ${unidadeParceira}=    Seed Uma Equipe
    ${payload}=    Create Dictionary    equipeId=${parceira}    objetivo=Acompanhar a adesão na eSF.
    ${r1}=    POST Compartilhamento Saude Mental    ${caso}    ${payload}
    Should Be Equal As Integers    ${r1.status_code}    201
    Validate Object Schema    ${r1.json()}[compartilhamentos][0]    ${COMPARTILHAMENTO_CUIDADO_SCHEMA}
    ${r2}=    POST Compartilhamento Saude Mental    ${caso}    ${payload}
    Should Be Equal As Integers    ${r2.status_code}    409
    ${motivo}=    Create Dictionary    motivo=Estável.
    ${r3}=    POST Encerrar Compartilhamento Saude Mental    ${caso}    ${r1.json()}[compartilhamentos][0][uuid]    ${motivo}
    Should Be Equal As Integers    ${r3.status_code}    200
    Should Not Be Equal    ${r3.json()}[compartilhamentos][0][fim]    ${None}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Saude Mental - Matriciamento Sem Apoio - Status Code 422
    [Documentation]    The matrix support needs the team and the support relation.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${caso}    ${unidade}    ${equipe}    ${matricula}    ${paciente}    ${programa}=    Seed Caso Saude Mental
    ${tipo}=    Id Do Tipo De Intervencao    MATRICIAMENTO
    ${sem}=    Create Dictionary    tipoId=${tipo}    resumo=Discussão do caso.
    ${r400}=    POST Intervencao Saude Mental    ${caso}    ${sem}
    Should Be Equal As Integers    ${r400.status_code}    400
    ${outra}    ${nomeOutra}    ${unidadeOutra}=    Seed Uma Equipe
    ${com}=    Create Dictionary    tipoId=${tipo}    resumo=Discussão do caso.    equipeMatriciadaId=${outra}
    ${r422}=    POST Intervencao Saude Mental    ${caso}    ${com}
    Should Be Equal As Integers    ${r422.status_code}    422
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Saude Mental - Transferencia Aceita - Status Code 200
    [Documentation]    Proposal, a second one refused, and the acceptance opening the case at the destination.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${caso}    ${unidade}    ${equipe}    ${matricula}    ${paciente}    ${programa}=    Seed Caso Saude Mental
    ${destino}    ${equipeDestino}    ${matriculaDestino}=    Seed Destino De Transferencia
    ${payload}=    Create Dictionary    unidadeDestinoId=${destino}    resumo=Estável; segue na eSF.
    ${r1}=    POST Transferencia Saude Mental    ${caso}    ${payload}
    Should Be Equal As Integers    ${r1.status_code}    201
    Validate Object Schema    ${r1.json()}[transferencias][0]    ${TRANSFERENCIA_CUIDADO_SCHEMA}
    ${r2}=    POST Transferencia Saude Mental    ${caso}    ${payload}
    Should Be Equal As Integers    ${r2.status_code}    409
    ${aceite}=    Create Dictionary    equipeId=${equipeDestino}    tecnicoMatricula=${matriculaDestino}
    ${r3}=    POST Aceite Transferencia Saude Mental    ${r1.json()}[transferencias][0][uuid]    ${aceite}
    Should Be Equal As Integers    ${r3.status_code}    200
    Should Be Equal    ${r3.json()}[casoAnteriorId]    ${caso}
    Should Be Equal    ${r3.json()}[unidadeId]    ${destino}
    ${origem}=    GET Caso Saude Mental    ${caso}
    Should Be Equal    ${origem.json()}[motivoSaida]    TRANSFERENCIA
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Saude Mental - Transferencia Recusada - Status Code 200
    [Documentation]    The destination refuses; the case stays.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${caso}    ${unidade}    ${equipe}    ${matricula}    ${paciente}    ${programa}=    Seed Caso Saude Mental
    ${destino}    ${equipeDestino}    ${matriculaDestino}=    Seed Destino De Transferencia
    ${payload}=    Create Dictionary    unidadeDestinoId=${destino}    resumo=Mudou de bairro.
    ${r1}=    POST Transferencia Saude Mental    ${caso}    ${payload}
    ${motivo}=    Create Dictionary    motivo=Fora da área.
    ${r2}=    POST Recusa Transferencia Saude Mental    ${r1.json()}[transferencias][0][uuid]    ${motivo}
    Should Be Equal As Integers    ${r2.status_code}    200
    Should Be Equal    ${r2.json()}[situacao]    RECUSADA
    ${caso2}=    GET Caso Saude Mental    ${caso}
    Should Be Equal    ${caso2.json()}[situacao]    ATIVO
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Saude Mental - Transferencias Pendentes - Status Code 200
    [Documentation]    The pending transfers list.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${caso}    ${unidade}    ${equipe}    ${matricula}    ${paciente}    ${programa}=    Seed Caso Saude Mental
    ${destino}    ${equipeDestino}    ${matriculaDestino}=    Seed Destino De Transferencia
    ${payload}=    Create Dictionary    unidadeDestinoId=${destino}    resumo=Retorno à UBS.
    POST Transferencia Saude Mental    ${caso}    ${payload}
    ${response}=    GET Transferencias Saude Mental
    Should Be Equal As Integers    ${response.status_code}    200
    ${casos}=    Evaluate    [t["casoId"] for t in $response.json()["recebidas"]]
    Should Contain    ${casos}    ${caso}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Saude Mental Rede - By HTTP Status Code
    [Documentation]    Executes the SM4 scenario for the given HTTP status code.
    [Arguments]    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Run Keyword If    '${meuHTTP}' == '201-encaminhamento'    Saude Mental - Encaminhamento - Status Code 201
    ...    ELSE IF    '${meuHTTP}' == '422-vinculo'    Saude Mental - Vincular De Outro Paciente - Status Code 422
    ...    ELSE IF    '${meuHTTP}' == '201-compartilhamento'    Saude Mental - Compartilhamento - Status Code 201
    ...    ELSE IF    '${meuHTTP}' == '422-matriciamento'    Saude Mental - Matriciamento Sem Apoio - Status Code 422
    ...    ELSE IF    '${meuHTTP}' == '200-aceite'    Saude Mental - Transferencia Aceita - Status Code 200
    ...    ELSE IF    '${meuHTTP}' == '200-recusa'    Saude Mental - Transferencia Recusada - Status Code 200
    ...    ELSE IF    '${meuHTTP}' == '200-pendentes'    Saude Mental - Transferencias Pendentes - Status Code 200
    ...    ELSE    Fail    HTTP status code '${meuHTTP}' is not supported for Saude Mental Rede test scenario.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
