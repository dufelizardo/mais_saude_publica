*** Settings ***
Library     String
Library     Collections
Resource    ../../pom/saude_mental/raps_pom.resource
Resource    saude_mental_flow.resource
Resource    ../equipamentos/modelo_operacional/modelo_operacional_flow.resource
Resource    ../common/schema_validation_flow.resource
Resource    ../../resource/config/schema/schema.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
    O caso de saúde mental numa unidade que vira CAPS III com acolhimento noturno de uma vaga (ADR-0153).
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
Seed Caps Com Acolhimento
    [Documentation]    A case in a unit with the psychosocial profile, turned CAPS III with night shelter, one bed and 14 days; returns the case, the unit and the patient.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${caso}    ${unidade}    ${equipe}    ${matricula}    ${paciente}    ${programa}=    Seed Caso Saude Mental
    ${perfil}=    Incluir Perfil Por Codigo    ${unidade}    ATENCAO_PSICOSSOCIAL
    Should Be True    ${perfil.status_code} in (200, 201)
    ${c1}=    Incluir Capacidade Por Codigo    ${unidade}    CAPS_III
    Should Be True    ${c1.status_code} in (200, 201)
    ${c2}=    Incluir Capacidade Por Codigo    ${unidade}    ACOLHIMENTO_NOTURNO
    Should Be True    ${c2.status_code} in (200, 201)
    ${config}=    Create Dictionary    vagas=${1}    permanenciaMaximaDias=${14}
    ${r}=    PUT Configuracao Acolhimento    ${unidade}    ${config}
    Should Be Equal As Integers    ${r.status_code}    200
    RETURN    ${caso}    ${unidade}    ${paciente}    ${equipe}    ${matricula}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Entrada Do Paciente
    [Documentation]    Enters the patient with the expected exit some days ahead; returns the response.
    [Arguments]    ${unidade}    ${paciente}    ${dias}=5
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${prevista}=    Evaluate    (datetime.date.today() + datetime.timedelta(days=int(${dias}))).isoformat()    modules=datetime
    ${payload}=    Create Dictionary    unidadeId=${unidade}    pacienteId=${paciente}    saidaPrevista=${prevista}
    ${r}=    POST Entrada Acolhimento    ${payload}
    RETURN    ${r}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Raps - Painel - Status Code 200
    [Documentation]    The unit turned CAPS III appears in the specialized component.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${caso}    ${unidade}    ${equipe}    ${matricula}    ${paciente}    ${programa}=    Seed Caso Saude Mental
    ${perfil}=    Incluir Perfil Por Codigo    ${unidade}    ATENCAO_PSICOSSOCIAL
    Should Be True    ${perfil.status_code} in (200, 201)
    ${c}=    Incluir Capacidade Por Codigo    ${unidade}    CAPS_III
    Should Be True    ${c.status_code} in (200, 201)
    ${r}=    GET Raps
    Should Be Equal As Integers    ${r.status_code}    200
    Validate Object Schema    ${r.json()}    ${PAINEL_RAPS_SCHEMA}
    ${pontos}=    Evaluate    [u for c in $r.json()["componentes"] if c["componente"] == "ATENCAO_PSICOSSOCIAL_ESPECIALIZADA" for u in c["unidades"] if u["unidadeId"] == $unidade]
    Length Should Be    ${pontos}    1
    List Should Contain Value    ${pontos}[0][capacidades]    CAPS III
    Should Be Equal As Integers    ${pontos}[0][casosEmAcompanhamento]    1
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Acolhimento - Configuracao - Status Code 200
    [Documentation]    The beds and the maximum stay of the unit, with the schema.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${caso}    ${unidade}    ${paciente}    ${equipe}    ${matricula}=    Seed Caps Com Acolhimento
    ${r}=    GET Acolhimento Noturno    ${unidade}
    Should Be Equal As Integers    ${r.status_code}    200
    Validate Object Schema    ${r.json()}    ${ACOLHIMENTO_NOTURNO_SCHEMA}
    Should Be True    ${r.json()}[temAcolhimentoNoturno]
    Should Be Equal As Integers    ${r.json()}[vagas]    1
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Acolhimento - Sem Capacidade - Status Code 422
    [Documentation]    A unit without the night shelter capability is not configured.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${caso}    ${unidade}    ${equipe}    ${matricula}    ${paciente}    ${programa}=    Seed Caso Saude Mental
    ${config}=    Create Dictionary    vagas=${2}    permanenciaMaximaDias=${14}
    ${r}=    PUT Configuracao Acolhimento    ${unidade}    ${config}
    Should Be Equal As Integers    ${r.status_code}    422
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Acolhimento - Entrada - Status Code 201
    [Documentation]    The patient with a case in the unit enters the night shelter.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${caso}    ${unidade}    ${paciente}    ${equipe}    ${matricula}=    Seed Caps Com Acolhimento
    ${r}=    Entrada Do Paciente    ${unidade}    ${paciente}
    Should Be Equal As Integers    ${r.status_code}    201
    Validate Object Schema    ${r.json()}    ${PERMANENCIA_ACOLHIMENTO_SCHEMA}
    Should Not Be True    ${r.json()}[alemDoMaximo]
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Acolhimento - Ja Acolhido - Status Code 409
    [Documentation]    One open stay per patient.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${caso}    ${unidade}    ${paciente}    ${equipe}    ${matricula}=    Seed Caps Com Acolhimento
    Entrada Do Paciente    ${unidade}    ${paciente}
    ${r}=    Entrada Do Paciente    ${unidade}    ${paciente}
    Should Be Equal As Integers    ${r.status_code}    409
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Acolhimento - Sem Vaga - Status Code 422
    [Documentation]    With the only bed taken, the next patient waits.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${caso}    ${unidade}    ${paciente}    ${equipe}    ${matricula}=    Seed Caps Com Acolhimento
    Entrada Do Paciente    ${unidade}    ${paciente}
    ${outro}    ${cpf}    ${cns}    ${nome}=    Seed A Paciente
    ${payload}=    Create Dictionary    pacienteId=${outro}    unidadeId=${unidade}    equipeId=${equipe}    tecnicoMatricula=${matricula}
    ${c}=    POST Abrir Caso Saude Mental    ${payload}
    Should Be Equal As Integers    ${c.status_code}    201
    ${r}=    Entrada Do Paciente    ${unidade}    ${outro}
    Should Be Equal As Integers    ${r.status_code}    422
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Acolhimento - Alem Do Maximo - Status Code 422
    [Documentation]    Beyond the maximum stay, only with the justification.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${caso}    ${unidade}    ${paciente}    ${equipe}    ${matricula}=    Seed Caps Com Acolhimento
    ${r}=    Entrada Do Paciente    ${unidade}    ${paciente}    20
    Should Be Equal As Integers    ${r.status_code}    422
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Acolhimento - Saida - Status Code 200
    [Documentation]    The exit with the reason frees the bed.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${caso}    ${unidade}    ${paciente}    ${equipe}    ${matricula}=    Seed Caps Com Acolhimento
    ${e}=    Entrada Do Paciente    ${unidade}    ${paciente}
    ${payload}=    Create Dictionary    motivo=Estabilizado, segue no CAPS
    ${r}=    POST Saida Acolhimento    ${e.json()}[uuid]    ${payload}
    Should Be Equal As Integers    ${r.status_code}    200
    Validate Object Schema    ${r.json()}    ${PERMANENCIA_ACOLHIMENTO_SCHEMA}
    ${a}=    GET Acolhimento Noturno    ${unidade}
    Should Be Equal As Integers    ${a.json()}[ocupadas]    0
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Raps - By HTTP Status Code
    [Documentation]    Executes the SM5 scenario for the given HTTP status code.
    [Arguments]    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Run Keyword If    '${meuHTTP}' == '200-raps'    Raps - Painel - Status Code 200
    ...    ELSE IF    '${meuHTTP}' == '200-configuracao'    Acolhimento - Configuracao - Status Code 200
    ...    ELSE IF    '${meuHTTP}' == '422-sem-capacidade'    Acolhimento - Sem Capacidade - Status Code 422
    ...    ELSE IF    '${meuHTTP}' == '201-entrada'    Acolhimento - Entrada - Status Code 201
    ...    ELSE IF    '${meuHTTP}' == '409-acolhido'    Acolhimento - Ja Acolhido - Status Code 409
    ...    ELSE IF    '${meuHTTP}' == '422-sem-vaga'    Acolhimento - Sem Vaga - Status Code 422
    ...    ELSE IF    '${meuHTTP}' == '422-maximo'    Acolhimento - Alem Do Maximo - Status Code 422
    ...    ELSE IF    '${meuHTTP}' == '200-saida'    Acolhimento - Saida - Status Code 200
    ...    ELSE    Fail    HTTP status code '${meuHTTP}' is not supported for Raps test scenario.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
