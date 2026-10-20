*** Settings ***
Library     String
Library     Collections
Resource    ../../pom/saude_mental/saude_mental_pom.resource
Resource    ../programas/acompanhamento/acompanhamento_flow.resource
Resource    ../equipes/equipe/equipe_flow.resource
Resource    ../common/schema_validation_flow.resource
Resource    ../../resource/config/schema/schema.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
Seed Base Saude Mental
    [Documentation]    Creates a unit offering the Saúde Mental program, a team with a posted member and a patient; returns them.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${equipe}    ${nomeEquipe}    ${unidade}=    Seed Uma Equipe
    ${matricula}=    Seed Um Profissional Lotado    ${unidade}
    ${membro}=    Create Dictionary    profissionalMatricula=${matricula}    funcao=PSICOLOGO
    ${r}=    POST Membro Equipe    ${equipe}    ${membro}
    Should Be Equal As Integers    ${r.status_code}    200
    ${programa}=    Id Do Programa Saude Mental
    ${participacao}=    Create Dictionary    unidadeId=${unidade}
    ${p}=    POST Participacao Programa    ${programa}    ${participacao}
    Should Be Equal As Integers    ${p.status_code}    200
    ${paciente}    ${cpf}    ${cns}    ${nomePaciente}=    Seed A Paciente
    RETURN    ${unidade}    ${equipe}    ${matricula}    ${paciente}    ${programa}    ${nomePaciente}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Id Do Programa Saude Mental
    [Documentation]    Returns the uuid of the SAUDE_MENTAL program of the catalog.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${response}=    GET Programas
    ${ids}=    Evaluate    [p["uuid"] for p in $response.json() if p["codigo"] == "SAUDE_MENTAL"]
    RETURN    ${ids}[0]
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Seed Caso Saude Mental
    [Documentation]    Opens a case on a fresh base and returns the case uuid and the base.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${unidade}    ${equipe}    ${matricula}    ${paciente}    ${programa}    ${nomePaciente}=    Seed Base Saude Mental
    ${payload}=    Create Dictionary    pacienteId=${paciente}    unidadeId=${unidade}    equipeId=${equipe}    tecnicoMatricula=${matricula}
    ${response}=    POST Abrir Caso Saude Mental    ${payload}
    Should Be Equal As Integers    ${response.status_code}    201
    RETURN    ${response.json()}[uuid]    ${unidade}    ${equipe}    ${matricula}    ${paciente}    ${programa}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Saude Mental - Acolhimento Abre Caso - Status Code 201
    [Documentation]    The reception with follow-up opens the case in the program; the next one joins it.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${unidade}    ${equipe}    ${matricula}    ${paciente}    ${programa}    ${nomePaciente}=    Seed Base Saude Mental
    ${payload}=    Create Dictionary    pacienteId=${paciente}    unidadeId=${unidade}    motivoProcura=Tristeza persistente e insônia.    riscoInicial=MODERADO    desfecho=ACOMPANHAMENTO_NA_UNIDADE    equipeId=${equipe}    tecnicoMatricula=${matricula}
    ${response}=    POST Acolhimento Saude Mental    ${payload}
    Should Be Equal As Integers    ${response.status_code}    201
    Validate Object Schema    ${response.json()}    ${ACOLHIMENTO_SAUDE_MENTAL_SCHEMA}
    Should Not Be Equal    ${response.json()}[acompanhamentoId]    ${None}
    ${caso}=    GET Caso Saude Mental    ${response.json()}[acompanhamentoId]
    Validate Object Schema    ${caso.json()}    ${CASO_SAUDE_MENTAL_SCHEMA}
    Should Be Equal    ${caso.json()}[situacao]    ATIVO
    ${segundo}=    Create Dictionary    pacienteId=${paciente}    unidadeId=${unidade}    motivoProcura=Retorno espontâneo.    riscoInicial=BAIXO    desfecho=CUIDADO_COMPARTILHADO
    ${r2}=    POST Acolhimento Saude Mental    ${segundo}
    Should Be Equal As Integers    ${r2.status_code}    201
    Should Be Equal    ${r2.json()}[acompanhamentoId]    ${response.json()}[acompanhamentoId]
    Should Contain    ${r2.json()}[aviso]    já é acompanhado
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Saude Mental - Caso Repetido - Status Code 409
    [Documentation]    One case in force per patient.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${caso}    ${unidade}    ${equipe}    ${matricula}    ${paciente}    ${programa}=    Seed Caso Saude Mental
    ${payload}=    Create Dictionary    pacienteId=${paciente}    unidadeId=${unidade}    equipeId=${equipe}    tecnicoMatricula=${matricula}
    ${response}=    POST Abrir Caso Saude Mental    ${payload}
    Should Be Equal As Integers    ${response.status_code}    409
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Saude Mental - Acolhimento Sem Referencia - Status Code 400
    [Documentation]    Following up needs the reference team and technician.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${unidade}    ${equipe}    ${matricula}    ${paciente}    ${programa}    ${nomePaciente}=    Seed Base Saude Mental
    ${payload}=    Create Dictionary    pacienteId=${paciente}    unidadeId=${unidade}    motivoProcura=Ansiedade.    riscoInicial=BAIXO    desfecho=ACOMPANHAMENTO_NA_UNIDADE
    ${response}=    POST Acolhimento Saude Mental    ${payload}
    Should Be Equal As Integers    ${response.status_code}    400
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Saude Mental - Tecnico Fora Da Equipe - Status Code 422
    [Documentation]    The technician must be a current member of the team.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${unidade}    ${equipe}    ${matricula}    ${paciente}    ${programa}    ${nomePaciente}=    Seed Base Saude Mental
    ${outro}=    Seed Um Profissional Lotado    ${unidade}
    ${payload}=    Create Dictionary    pacienteId=${paciente}    unidadeId=${unidade}    equipeId=${equipe}    tecnicoMatricula=${outro}
    ${response}=    POST Abrir Caso Saude Mental    ${payload}
    Should Be Equal As Integers    ${response.status_code}    422
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Saude Mental - Troca De Referencia - Status Code 200
    [Documentation]    The reference is history: the previous one closes.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${caso}    ${unidade}    ${equipe}    ${matricula}    ${paciente}    ${programa}=    Seed Caso Saude Mental
    ${outro}=    Seed Um Profissional Lotado    ${unidade}
    ${membro}=    Create Dictionary    profissionalMatricula=${outro}    funcao=ASSISTENTE_SOCIAL
    ${r}=    POST Membro Equipe    ${equipe}    ${membro}
    Should Be Equal As Integers    ${r.status_code}    200
    ${payload}=    Create Dictionary    equipeId=${equipe}    tecnicoMatricula=${outro}    motivo=Vínculo maior com a assistente social.
    ${response}=    POST Referencia Saude Mental    ${caso}    ${payload}
    Should Be Equal As Integers    ${response.status_code}    200
    Should Be Equal    ${response.json()}[tecnicoMatricula]    ${outro}
    Length Should Be    ${response.json()}[referencias]    2
    ${repetida}=    POST Referencia Saude Mental    ${caso}    ${payload}
    Should Be Equal As Integers    ${repetida.status_code}    422
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Saude Mental - Avaliacao - Status Code 201
    [Documentation]    Registers an assessment; an invalid CID-10 is refused with 400.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${caso}    ${unidade}    ${equipe}    ${matricula}    ${paciente}    ${programa}=    Seed Caso Saude Mental
    ${invalida}=    Create Dictionary    contextoHistoria=Luto recente.    necessidades=Acompanhamento semanal.    cid=X1
    ${r400}=    POST Avaliacao Saude Mental    ${caso}    ${invalida}
    Should Be Equal As Integers    ${r400.status_code}    400
    ${payload}=    Create Dictionary    contextoHistoria=Luto recente.    necessidades=Acompanhamento semanal.    cid=F32.1
    ${response}=    POST Avaliacao Saude Mental    ${caso}    ${payload}
    Should Be Equal As Integers    ${response.status_code}    201
    Length Should Be    ${response.json()}[avaliacoes]    1
    Validate Object Schema    ${response.json()}[avaliacoes][0]    ${AVALIACAO_SAUDE_MENTAL_SCHEMA}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Saude Mental - Encerramento - Status Code 200
    [Documentation]    Closing the case closes the program follow-up too; before it, the program refuses.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${caso}    ${unidade}    ${equipe}    ${matricula}    ${paciente}    ${programa}=    Seed Caso Saude Mental
    ${detalhe}=    GET Caso Saude Mental    ${caso}
    ${saida}=    Create Dictionary    motivoSaida=ALTA    observacao=Objetivos do cuidado alcançados.
    ${pelo_programa}=    POST Encerrar Acompanhamento    ${detalhe.json()}[acompanhamentoProgramaticoId]    ${saida}
    Should Be Equal As Integers    ${pelo_programa.status_code}    422
    ${response}=    POST Encerrar Caso Saude Mental    ${caso}    ${saida}
    Should Be Equal As Integers    ${response.status_code}    200
    Should Be Equal    ${response.json()}[situacao]    CONCLUIDO
    ${programas}=    GET Programas Do Paciente    ${paciente}
    Length Should Be    ${programas.json()}[vigentes]    0
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Saude Mental - Casos - Status Code 200
    [Documentation]    Lists the cases in force with the counters.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${caso}    ${unidade}    ${equipe}    ${matricula}    ${paciente}    ${programa}=    Seed Caso Saude Mental
    ${response}=    GET Casos Saude Mental
    Should Be Equal As Integers    ${response.status_code}    200
    Validate Object Schema    ${response.json()}    ${CASOS_SAUDE_MENTAL_SCHEMA}
    ${ids}=    Evaluate    [c["uuid"] for c in $response.json()["lista"]]
    Should Contain    ${ids}    ${caso}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Saude Mental - Inexistente - Status Code 404
    [Documentation]    A case that does not exist.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${uuid}=    Evaluate    str(uuid.uuid4())    modules=uuid
    ${response}=    GET Caso Saude Mental    ${uuid}
    Should Be Equal As Integers    ${response.status_code}    404
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Saude Mental - By HTTP Status Code
    [Documentation]    Executes the mental health scenario for the given HTTP status code.
    [Arguments]    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Run Keyword If    '${meuHTTP}' == '201'    Saude Mental - Acolhimento Abre Caso - Status Code 201
    ...    ELSE IF    '${meuHTTP}' == '409'    Saude Mental - Caso Repetido - Status Code 409
    ...    ELSE IF    '${meuHTTP}' == '400'    Saude Mental - Acolhimento Sem Referencia - Status Code 400
    ...    ELSE IF    '${meuHTTP}' == '422'    Saude Mental - Tecnico Fora Da Equipe - Status Code 422
    ...    ELSE IF    '${meuHTTP}' == '200-referencia'    Saude Mental - Troca De Referencia - Status Code 200
    ...    ELSE IF    '${meuHTTP}' == '201-avaliacao'    Saude Mental - Avaliacao - Status Code 201
    ...    ELSE IF    '${meuHTTP}' == '200-encerramento'    Saude Mental - Encerramento - Status Code 200
    ...    ELSE IF    '${meuHTTP}' == '200-casos'    Saude Mental - Casos - Status Code 200
    ...    ELSE IF    '${meuHTTP}' == '404'    Saude Mental - Inexistente - Status Code 404
    ...    ELSE    Fail    HTTP status code '${meuHTTP}' is not supported for Saude Mental test scenario.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
