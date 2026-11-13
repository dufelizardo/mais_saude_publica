*** Settings ***
Library     String
Library     Collections
Resource    ../../pom/vigilancia/saude_trabalhador_pom.resource
Resource    ./vigilancia_flow.resource
Resource    ../paciente/create_paciente/create_paciente_flow.resource
Resource    ../profissional/get_profissional_by_matricula/get_profissional_by_matricula_flow.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
    Saúde do Trabalhador (ADR-0207): o bloco do trabalho na notificação, o acompanhamento sorológico do material biológico e o
    acidente do servidor no RH.
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
Bloco Do Trabalho
    [Documentation]    A valid work block; extra keys are merged in.
    [Arguments]    &{extras}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${bloco}=    Create Dictionary    ocupacaoCbo=7152-10    ocupacaoDescricao=Pedreiro    situacaoTrabalho=EMPREGADO_REGISTRADO
    Set To Dictionary    ${bloco}    &{extras}
    RETURN    ${bloco}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Payload Notificacao Do Trabalho
    [Documentation]    The notification payload of a work agravo, with the accident date (days ago) and the block.
    [Arguments]    ${paciente}    ${unidade}    ${codigo}    ${dias}    ${bloco}=${NONE}    ${respostas}=${NONE}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${agravo}=    Id Do Agravo    ${codigo}
    ${data}=    Evaluate    (datetime.date.today() - datetime.timedelta(days=int(${dias}))).isoformat()    modules=datetime
    ${payload}=    Create Dictionary    pacienteId=${paciente}    agravoId=${agravo}    unidadeId=${unidade}    origem=ATENDIMENTO    dataPrimeirosSintomas=${data}
    IF    $bloco is not None    Set To Dictionary    ${payload}    trabalho=${bloco}
    IF    $respostas is not None    Set To Dictionary    ${payload}    respostas=${respostas}
    RETURN    ${payload}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Respostas Material Biologico
    [Documentation]    Answers to the biological material form.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${exposicao}=    Create List    Percutânea
    ${r}=    Create Dictionary    tipoExposicao=${exposicao}    materialOrganico=Sangue    vacinadoHepatiteB=${True}    fonteConhecida=${False}
    RETURN    ${r}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Notificar Acidente Grave
    [Documentation]    Notifies a serious work accident two days ago; returns the notification uuid.
    [Arguments]    ${paciente}    ${unidade}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${bloco}=    Bloco Do Trabalho    tipoAcidente=TIPICO    cat=NAO    empregadorDocumento=11.222.333/0001-81    empregadorNome=Construtora Robot
    ${payload}=    Payload Notificacao Do Trabalho    ${paciente}    ${unidade}    ACIDENTE_TRABALHO_GRAVE    2    ${bloco}
    ${r}=    POST Notificacao    ${payload}
    Should Be Equal As Integers    ${r.status_code}    201
    RETURN    ${r.json()}[uuid]
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Trabalhador - Acidente Grave - Status Code 201
    [Documentation]    A serious accident needs the work block; with it, 201 and the CNPJ normalized.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${unidade}=    Seed A Unidade De Saude Uuid
    ${paciente}    ${cpf}    ${cns}    ${nome}=    Seed A Paciente
    ${payload}=    Payload Notificacao Do Trabalho    ${paciente}    ${unidade}    ACIDENTE_TRABALHO_GRAVE    2
    ${r}=    POST Notificacao    ${payload}
    Should Be Equal As Integers    ${r.status_code}    400
    ${bloco}=    Bloco Do Trabalho    tipoAcidente=TIPICO    empregadorDocumento=11.222.333/0001-81    empregadorNome=Construtora Robot    obito=${True}
    ${payload}=    Payload Notificacao Do Trabalho    ${paciente}    ${unidade}    ACIDENTE_TRABALHO_GRAVE    2    ${bloco}
    ${r}=    POST Notificacao    ${payload}
    Should Be Equal As Integers    ${r.status_code}    201
    Should Be Equal    ${r.json()}[trabalho][ocupacaoCbo]    715210
    Should Be Equal    ${r.json()}[trabalho][empregadorDocumento]    11222333000181
    Should Be True    ${r.json()}[trabalho][obito]
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Trabalhador - Dado Do Acidente Fora Do Acidente - Status Code 400
    [Documentation]    The accident data is refused on LER/DORT.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${unidade}=    Seed A Unidade De Saude Uuid
    ${paciente}    ${cpf}    ${cns}    ${nome}=    Seed A Paciente
    ${bloco}=    Bloco Do Trabalho    obito=${True}
    ${payload}=    Payload Notificacao Do Trabalho    ${paciente}    ${unidade}    LER_DORT    30    ${bloco}
    ${r}=    POST Notificacao    ${payload}
    Should Be Equal As Integers    ${r.status_code}    400
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Trabalhador - Acompanhamento Sorologico - Status Code 200
    [Documentation]    The six-week step is overdue; registered today, it is done; in the future, 400.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${unidade}=    Seed A Unidade De Saude Uuid
    ${paciente}    ${cpf}    ${cns}    ${nome}=    Seed A Paciente
    ${bloco}=    Bloco Do Trabalho    ocupacaoCbo=322205    ocupacaoDescricao=Técnico de enfermagem    situacaoTrabalho=SERVIDOR_ESTATUTARIO
    ${respostas}=    Respostas Material Biologico
    ${payload}=    Payload Notificacao Do Trabalho    ${paciente}    ${unidade}    ACIDENTE_TRABALHO_MATERIAL_BIOLOGICO    50    ${bloco}    ${respostas}
    ${r}=    POST Notificacao    ${payload}
    Should Be Equal As Integers    ${r.status_code}    201
    ${n}=    Set Variable    ${r.json()}[uuid]
    Should Be True    ${r.json()}[trabalho][acompanhamentos][0][vencida]
    ${amanha}=    Evaluate    (datetime.date.today() + datetime.timedelta(days=1)).isoformat()    modules=datetime
    ${corpo}=    Create Dictionary    etapa=SEIS_SEMANAS    realizadoEm=${amanha}
    ${r}=    POST Acompanhamento Sorologico    ${n}    ${corpo}
    Should Be Equal As Integers    ${r.status_code}    400
    ${hoje}=    Evaluate    datetime.date.today().isoformat()    modules=datetime
    ${corpo}=    Create Dictionary    etapa=SEIS_SEMANAS    realizadoEm=${hoje}
    ${r}=    POST Acompanhamento Sorologico    ${n}    ${corpo}
    Should Be Equal As Integers    ${r.status_code}    200
    Should Be Equal    ${r.json()}[trabalho][acompanhamentos][0][realizadoEm]    ${hoje}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Trabalhador - Painel - Status Code 200
    [Documentation]    The panel of the unit lists the accident with the employer.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${unidade}=    Seed A Unidade De Saude Uuid
    ${paciente}    ${cpf}    ${cns}    ${nome}=    Seed A Paciente
    ${n}=    Notificar Acidente Grave    ${paciente}    ${unidade}
    ${r}=    GET Painel Saude Do Trabalhador    ${unidade}
    Should Be Equal As Integers    ${r.status_code}    200
    Should Be Equal As Integers    ${r.json()}[total]    1
    Should Be Equal As Integers    ${r.json()}[acidentes]    1
    Should Be Equal    ${r.json()}[notificacoes][0][empregadorNome]    Construtora Robot
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Trabalhador - Correcao Pela Investigacao - Status Code 200
    [Documentation]    The investigation corrects the block: the CAT was issued.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${unidade}=    Seed A Unidade De Saude Uuid
    ${paciente}    ${cpf}    ${cns}    ${nome}=    Seed A Paciente
    ${n}=    Notificar Acidente Grave    ${paciente}    ${unidade}
    ${bloco}=    Bloco Do Trabalho    tipoAcidente=TIPICO    cat=SIM
    ${r}=    PUT Trabalho Da Notificacao    ${n}    ${bloco}
    Should Be Equal As Integers    ${r.status_code}    200
    Should Be Equal    ${r.json()}[trabalho][cat]    SIM
    ${bloco}=    Bloco Do Trabalho    ocupacaoCbo=12
    ${r}=    PUT Trabalho Da Notificacao    ${n}    ${bloco}
    Should Be Equal As Integers    ${r.status_code}    400
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Trabalhador - Servidor Do RH - Status Code 201
    [Documentation]    The HR accident of a civil servant is listed, notified once (409 the second time) and marked in HR.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${unidade}=    Seed A Unidade De Saude Uuid
    ${cpf}=    Seed A Profissional Unit
    ${prof}=    Call GET Get Profissional By Cpf    ${cpf}
    ${matricula}=    Set Variable    ${prof.json()}[matricula]
    ${suffix}=    Generate Random String    8    [NUMBERS]
    ${pac}=    Build Paciente Payload    Robot Servidor ${suffix}    ${cpf}
    ${r}=    POST Create Paciente    ${pac}
    Should Be Equal As Integers    ${r.status_code}    201
    ${paciente}=    Buscar Paciente Uuid Por Cpf    ${cpf}
    ${quando}=    Evaluate    (datetime.datetime.now() - datetime.timedelta(days=1)).replace(microsecond=0).isoformat()    modules=datetime
    ${acidente}=    Create Dictionary    matriculaProfissional=${matricula}    dataHora=${quando}    descricao=Queda da escada    catEmitida=${True}    diasAfastamento=${3}
    ${r}=    POST Acidente De Trabalho Do RH    ${acidente}
    Should Be Equal As Integers    ${r.status_code}    201
    ${r}=    GET Acidentes Do Profissional    ${matricula}
    ${acidenteId}=    Set Variable    ${r.json()}[0][uuid]
    ${r}=    GET Acidentes Sem Notificacao
    Should Be Equal As Integers    ${r.status_code}    200
    ${pacientes}=    Evaluate    [x['pacienteId'] for x in $r.json() if x['acidenteId'] == $acidenteId]
    Should Be Equal    ${pacientes}[0]    ${paciente}
    ${bloco}=    Bloco Do Trabalho    situacaoTrabalho=SERVIDOR_ESTATUTARIO    tipoAcidente=TIPICO    acidenteTrabalhoId=${acidenteId}
    ${payload}=    Payload Notificacao Do Trabalho    ${paciente}    ${unidade}    ACIDENTE_TRABALHO_GRAVE    1    ${bloco}
    ${r}=    POST Notificacao    ${payload}
    Should Be Equal As Integers    ${r.status_code}    201
    ${r}=    POST Notificacao    ${payload}
    Should Be Equal As Integers    ${r.status_code}    409
    ${r}=    GET Acidentes Do Profissional    ${matricula}
    Should Be True    ${r.json()}[0][notificadoVigilancia]
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Trabalhador - By Cenario
    [Documentation]    Executes the worker health scenario for the given case.
    [Arguments]    ${caso}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    IF    '${caso}' == 'GRAVE'
        Trabalhador - Acidente Grave - Status Code 201
    ELSE IF    '${caso}' == 'FORA_DO_ACIDENTE'
        Trabalhador - Dado Do Acidente Fora Do Acidente - Status Code 400
    ELSE IF    '${caso}' == 'SOROLOGICO'
        Trabalhador - Acompanhamento Sorologico - Status Code 200
    ELSE IF    '${caso}' == 'PAINEL'
        Trabalhador - Painel - Status Code 200
    ELSE IF    '${caso}' == 'CORRECAO'
        Trabalhador - Correcao Pela Investigacao - Status Code 200
    ELSE IF    '${caso}' == 'SERVIDOR'
        Trabalhador - Servidor Do RH - Status Code 201
    ELSE
        Fail    Caso '${caso}' não suportado no cenário Saúde do Trabalhador.
    END
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
