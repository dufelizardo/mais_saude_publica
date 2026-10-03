*** Settings ***
Library     String
Resource    ../../../pom/internacao/leito_internacao/leito_internacao_pom.resource
Resource    ../../setor/create_setor/create_setor_flow.resource
Resource    ../../paciente/create_paciente/create_paciente_flow.resource
Resource    ../../profissional/get_profissional_by_matricula/get_profissional_by_matricula_flow.resource
Resource    ../../common/schema_validation_flow.resource
Resource    ../../../pom/prontuario/get_prontuario_by_paciente_id/get_prontuario_by_paciente_id_pom.resource
Resource    ../../../resource/config/schema/schema.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
    O Seed A Paciente cria paciente do sexo feminino: os leitos de apoio são femininos ou mistos.
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
Seed Um Leito
    [Documentation]    Creates a bed in a fresh assistential sector and returns its uuid, the unit and the sector.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${setorId}    ${codigo}    ${unidadeId}=    Seed A Setor    ASSISTENCIAL
    ${leitoId}=    Seed Um Leito No Setor    ${unidadeId}    ${setorId}    MISTO
    RETURN    ${leitoId}    ${unidadeId}    ${setorId}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Seed Um Leito No Setor
    [Documentation]    Creates a bed in the given sector and returns its uuid.
    [Arguments]    ${unidadeId}    ${setorId}    ${sexo}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${sufixo}=    Generate Random String    6    [LETTERS][NUMBERS]
    ${payload}=    Create Dictionary    unidadeId=${unidadeId}    setorId=${setorId}    identificacao=Robot Leito ${sufixo}    tipo=CLINICO    sexo=${sexo}
    ${response}=    POST Create Leito    ${payload}
    Should Be Equal As Integers    ${response.status_code}    201
    RETURN    ${response.json()}[uuid]
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Seed Uma Internacao
    [Documentation]    Admits a fresh patient in a fresh bed; returns the admission, the bed, the unit, the sector, the patient and the doctor registration.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${leitoId}    ${unidadeId}    ${setorId}=    Seed Um Leito
    ${pacienteId}    ${cpf}    ${cartaoSus}    ${nome}=    Seed A Paciente
    ${matricula}=    Seed A Profissional Matricula
    ${payload}=    Create Dictionary    pacienteId=${pacienteId}    leitoId=${leitoId}    medicoMatricula=${matricula}    cid=J18.9    motivo=Pneumonia com hipoxemia.    carater=URGENCIA
    ${response}=    POST Create Internacao    ${payload}
    Should Be Equal As Integers    ${response.status_code}    201
    RETURN    ${response.json()}[uuid]    ${leitoId}    ${unidadeId}    ${setorId}    ${pacienteId}    ${matricula}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Leito - Criar - Status Code 201
    [Documentation]    Creates a bed, free, with a valid schema.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${setorId}    ${codigo}    ${unidadeId}=    Seed A Setor    ASSISTENCIAL
    ${payload}=    Create Dictionary    unidadeId=${unidadeId}    setorId=${setorId}    identificacao=Enf. 1 · Leito 01    tipo=CLINICO    sexo=FEMININO
    ${response}=    POST Create Leito    ${payload}
    Should Be Equal As Integers    ${response.status_code}    201
    Validate Object Schema    ${response.json()}    ${LEITO_SCHEMA}
    Should Be Equal    ${response.json()}[situacao]    LIVRE
    ${repetido}=    POST Create Leito    ${payload}
    Should Be Equal As Integers    ${repetido.status_code}    409
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Leito - Criar - Status Code 422 - Setor Nao Assistencial
    [Documentation]    Creates a bed in an administrative sector.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${setorId}    ${codigo}    ${unidadeId}=    Seed A Setor    ADMINISTRATIVO
    ${payload}=    Create Dictionary    unidadeId=${unidadeId}    setorId=${setorId}    identificacao=Adm 01    tipo=CLINICO    sexo=MISTO
    ${response}=    POST Create Leito    ${payload}
    Should Be Equal As Integers    ${response.status_code}    422
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Internacao - Criar - Status Code 201
    [Documentation]    Admits a patient: the bed becomes occupied; a second admission of the same patient is refused with 409.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${internacaoId}    ${leitoId}    ${unidadeId}    ${setorId}    ${pacienteId}    ${matricula}=    Seed Uma Internacao
    ${detalhe}=    GET Internacao By Id    ${internacaoId}
    Should Be Equal As Integers    ${detalhe.status_code}    200
    Validate Object Schema    ${detalhe.json()}    ${INTERNACAO_SCHEMA}
    Should Be Equal    ${detalhe.json()}[status]    INTERNADO
    ${params}=    Create Dictionary    unidadeId=${unidadeId}
    ${mapa}=    GET Mapa Leitos    ${params}
    Validate Array Schema    ${mapa.json()}    ${LEITO_SCHEMA}
    Should Be Equal    ${mapa.json()}[0][situacao]    OCUPADO
    ${outro}=    Seed Um Leito No Setor    ${unidadeId}    ${setorId}    MISTO
    ${payload}=    Create Dictionary    pacienteId=${pacienteId}    leitoId=${outro}    medicoMatricula=${matricula}    cid=J18.9    motivo=De novo.    carater=URGENCIA
    ${segunda}=    POST Create Internacao    ${payload}
    Should Be Equal As Integers    ${segunda.status_code}    409
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Internacao - Criar - Status Code 422 - Sexo Incompativel
    [Documentation]    Admits a female patient in a male ward bed.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${setorId}    ${codigo}    ${unidadeId}=    Seed A Setor    ASSISTENCIAL
    ${leitoId}=    Seed Um Leito No Setor    ${unidadeId}    ${setorId}    MASCULINO
    ${pacienteId}    ${cpf}    ${cartaoSus}    ${nome}=    Seed A Paciente
    ${matricula}=    Seed A Profissional Matricula
    ${payload}=    Create Dictionary    pacienteId=${pacienteId}    leitoId=${leitoId}    medicoMatricula=${matricula}    cid=J18.9    motivo=Pneumonia.    carater=ELETIVA
    ${response}=    POST Create Internacao    ${payload}
    Should Be Equal As Integers    ${response.status_code}    422
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Internacao - Troca De Leito - Status Code 200
    [Documentation]    Moves the patient; the old bed goes to cleaning.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${internacaoId}    ${leitoId}    ${unidadeId}    ${setorId}    ${pacienteId}    ${matricula}=    Seed Uma Internacao
    ${destino}=    Seed Um Leito No Setor    ${unidadeId}    ${setorId}    FEMININO
    ${payload}=    Create Dictionary    leitoId=${destino}    profissionalMatricula=${matricula}    motivo=Perto do posto
    ${response}=    POST Acao Internacao    ${internacaoId}    troca-de-leito    ${payload}
    Should Be Equal As Integers    ${response.status_code}    200
    Should Be Equal    ${response.json()}[leitoId]    ${destino}
    ${params}=    Create Dictionary    unidadeId=${unidadeId}
    ${mapa}=    GET Mapa Leitos    ${params}
    ${situacoes}=    Evaluate    {l["uuid"]: l["situacao"] for l in $mapa.json()}
    Should Be Equal    ${situacoes}[${leitoId}]    HIGIENIZACAO
    Should Be Equal    ${situacoes}[${destino}]    OCUPADO
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Internacao - Alta - Status Code 200
    [Documentation]    Discharges the patient; after cleaning, the bed is free again.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${internacaoId}    ${leitoId}    ${unidadeId}    ${setorId}    ${pacienteId}    ${matricula}=    Seed Uma Internacao
    ${payload}=    Create Dictionary    tipoAlta=MELHORADO    sumario=Pneumonia tratada, alta com antibiótico oral.    medicoMatricula=${matricula}
    ${response}=    POST Acao Internacao    ${internacaoId}    alta    ${payload}
    Should Be Equal As Integers    ${response.status_code}    200
    Should Be Equal    ${response.json()}[status]    ALTA
    ${liberado}=    POST Acao Leito    ${leitoId}    liberacao
    Should Be Equal As Integers    ${liberado.status_code}    200
    Should Be Equal    ${liberado.json()}[situacao]    LIVRE
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Internacao - Alta - Status Code 400 - Sumario Curto
    [Documentation]    Discharges with a summary shorter than 10 characters.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${internacaoId}    ${leitoId}    ${unidadeId}    ${setorId}    ${pacienteId}    ${matricula}=    Seed Uma Internacao
    ${payload}=    Create Dictionary    tipoAlta=MELHORADO    sumario=ok    medicoMatricula=${matricula}
    ${response}=    POST Acao Internacao    ${internacaoId}    alta    ${payload}
    Should Be Equal As Integers    ${response.status_code}    400
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Leito - Bloqueio E Desbloqueio - Status Code 200
    [Documentation]    Blocks a free bed with a reason and unblocks it.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${leitoId}    ${unidadeId}    ${setorId}=    Seed Um Leito
    ${payload}=    Create Dictionary    motivo=Manutenção da cama
    ${bloqueado}=    POST Acao Leito    ${leitoId}    bloqueio    ${payload}
    Should Be Equal As Integers    ${bloqueado.status_code}    200
    Should Be Equal    ${bloqueado.json()}[situacao]    BLOQUEADO
    ${desbloqueado}=    POST Acao Leito    ${leitoId}    desbloqueio
    Should Be Equal As Integers    ${desbloqueado.status_code}    200
    Should Be Equal    ${desbloqueado.json()}[situacao]    LIVRE
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Leito - Indicadores - Status Code 200
    [Documentation]    Reads the indicators of a unit with one occupied bed.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${internacaoId}    ${leitoId}    ${unidadeId}    ${setorId}    ${pacienteId}    ${matricula}=    Seed Uma Internacao
    ${params}=    Create Dictionary    unidadeId=${unidadeId}
    ${response}=    GET Indicadores Leitos    ${params}
    Should Be Equal As Integers    ${response.status_code}    200
    Validate Object Schema    ${response.json()}    ${INDICADORES_LEITOS_SCHEMA}
    Should Be Equal As Integers    ${response.json()}[ocupados]    1
    Should Be Equal As Numbers    ${response.json()}[taxaOcupacao]    100
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Internacao - Detalhe - Status Code 404
    [Documentation]    Fetches an admission that does not exist.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${uuid}=    Evaluate    str(uuid.uuid4())    modules=uuid
    ${response}=    GET Internacao By Id    ${uuid}
    Should Be Equal As Integers    ${response.status_code}    404
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Internacao - Prontuario - Status Code 200
    [Documentation]    Reads the admission in the patient record, with the reason and the movements (ADR-0100).
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${internacaoId}    ${leitoId}    ${unidadeId}    ${setorId}    ${pacienteId}    ${matricula}=    Seed Uma Internacao
    ${response}=    GET Get Prontuario By Paciente Id    ${pacienteId}
    Should Be Equal As Integers    ${response.status_code}    200
    Validate Array Schema    ${response.json()}[internacoes]    ${INTERNACAO_SCHEMA}
    Should Be Equal    ${response.json()}[internacoes][0][uuid]    ${internacaoId}
    Should Be Equal    ${response.json()}[internacoes][0][motivo]    Pneumonia com hipoxemia.
    Should Be Equal    ${response.json()}[internacoes][0][eventos][0][tipo]    ADMISSAO
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Leito Internacao - By HTTP Status Code
    [Documentation]    Executes the bed and admission scenario for the given HTTP status code.
    [Arguments]    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Run Keyword If    '${meuHTTP}' == '201-leito'    Leito - Criar - Status Code 201
    ...    ELSE IF    '${meuHTTP}' == '422-leito'    Leito - Criar - Status Code 422 - Setor Nao Assistencial
    ...    ELSE IF    '${meuHTTP}' == '201-internacao'    Internacao - Criar - Status Code 201
    ...    ELSE IF    '${meuHTTP}' == '422-sexo'    Internacao - Criar - Status Code 422 - Sexo Incompativel
    ...    ELSE IF    '${meuHTTP}' == '200-troca'    Internacao - Troca De Leito - Status Code 200
    ...    ELSE IF    '${meuHTTP}' == '200-alta'    Internacao - Alta - Status Code 200
    ...    ELSE IF    '${meuHTTP}' == '400-alta'    Internacao - Alta - Status Code 400 - Sumario Curto
    ...    ELSE IF    '${meuHTTP}' == '200-bloqueio'    Leito - Bloqueio E Desbloqueio - Status Code 200
    ...    ELSE IF    '${meuHTTP}' == '200-indicadores'    Leito - Indicadores - Status Code 200
    ...    ELSE IF    '${meuHTTP}' == '404'    Internacao - Detalhe - Status Code 404
    ...    ELSE IF    '${meuHTTP}' == '200-prontuario'    Internacao - Prontuario - Status Code 200
    ...    ELSE    Fail    HTTP status code '${meuHTTP}' is not supported for Leito Internacao test scenario.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
