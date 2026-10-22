*** Settings ***
Library     String
Library     Collections
Resource    ../../pom/saude_bucal/plano_tratamento_pom.resource
Resource    ./procedimento_odontologico_flow.resource
Resource    ../common/schema_validation_flow.resource
Resource    ../../resource/config/schema/schema.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
Item Do Plano
    [Documentation]    Builds a plan item from the catalog code, with the optional tooth and faces.
    [Arguments]    ${codigo}    ${dente}=${None}    ${faces}=${None}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${id}=    Id Do Item Do Catalogo    ${codigo}
    ${i}=    Create Dictionary    procedimentoCatalogoId=${id}
    IF    $dente is not None    Set To Dictionary    ${i}    dente=${dente}
    IF    $faces is not None    Set To Dictionary    ${i}    faces=${faces}
    RETURN    ${i}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Seed Plano Com Restauracao
    [Documentation]    Opens a plan with a resin restoration on 36 occlusal; returns the plan, the item, the visit, the patient and the dentist.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${atendimento}    ${paciente}    ${matricula}=    Seed Atendimento Odontologico
    ${faces}=    Create List    OCLUSAL
    ${item}=    Item Do Plano    RESTAURACAO_RESINA    ${36}    ${faces}
    ${itens}=    Create List    ${item}
    ${payload}=    Create Dictionary    atendimentoId=${atendimento}    profissionalMatricula=${matricula}    prioridade=ELETIVA    itens=${itens}
    ${r}=    POST Plano Tratamento    ${payload}
    Should Be Equal As Integers    ${r.status_code}    201
    RETURN    ${r.json()}[uuid]    ${r.json()}[itens][0][uuid]    ${atendimento}    ${paciente}    ${matricula}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Executar A Restauracao
    [Documentation]    Registers the realized resin restoration executing the plan item.
    [Arguments]    ${atendimento}    ${matricula}    ${item}    ${dente}=${36}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${faces}=    Create List    OCLUSAL
    ${p}=    Payload Do Procedimento    ${atendimento}    ${matricula}    RESTAURACAO_RESINA    ${dente}    ${faces}
    Set To Dictionary    ${p}    itemPlanoId=${item}
    ${r}=    POST Procedimento Do Atendimento    ${p}
    RETURN    ${r}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Plano Tratamento - Abrir - Status Code 201
    [Documentation]    Opens a plan with two items, both planned.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${atendimento}    ${paciente}    ${matricula}=    Seed Atendimento Odontologico
    ${faces}=    Create List    OCLUSAL
    ${i1}=    Item Do Plano    RESTAURACAO_RESINA    ${36}    ${faces}
    ${i2}=    Item Do Plano    EXODONTIA    ${48}
    ${itens}=    Create List    ${i1}    ${i2}
    ${payload}=    Create Dictionary    atendimentoId=${atendimento}    profissionalMatricula=${matricula}    prioridade=PRIORITARIA    itens=${itens}
    ${r}=    POST Plano Tratamento    ${payload}
    Should Be Equal As Integers    ${r.status_code}    201
    Validate Object Schema    ${r.json()}    ${PLANO_TRATAMENTO_SCHEMA}
    Validate Array Schema    ${r.json()}[itens]    ${ITEM_PLANO_TRATAMENTO_SCHEMA}
    Should Be Equal    ${r.json()}[situacao]    ABERTO
    ${situacoes}=    Evaluate    [i["situacao"] for i in $r.json()["itens"]]
    Should Be Equal As Strings    ${situacoes}    ['PLANEJADO', 'PLANEJADO']
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Plano Tratamento - Segundo Plano Aberto - Status Code 409
    [Documentation]    One open plan per patient.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${plano}    ${item}    ${atendimento}    ${paciente}    ${matricula}=    Seed Plano Com Restauracao
    ${payload}=    Create Dictionary    atendimentoId=${atendimento}    profissionalMatricula=${matricula}    prioridade=ELETIVA
    ${r}=    POST Plano Tratamento    ${payload}
    Should Be Equal As Integers    ${r.status_code}    409
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Plano Tratamento - Fora Do Atendimento Odontologico - Status Code 422
    [Documentation]    The plan is opened in a dental-care visit.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${atendimento}    ${paciente}    ${matricula}    ${unidade}=    Seed Um Atendimento
    ${payload}=    Create Dictionary    atendimentoId=${atendimento}    profissionalMatricula=${matricula}    prioridade=ELETIVA
    ${r}=    POST Plano Tratamento    ${payload}
    Should Be Equal As Integers    ${r.status_code}    422
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Plano Tratamento - Executar Pelo Procedimento - Status Code 201
    [Documentation]    The realized procedure concludes the item.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${plano}    ${item}    ${atendimento}    ${paciente}    ${matricula}=    Seed Plano Com Restauracao
    ${r}=    Executar A Restauracao    ${atendimento}    ${matricula}    ${item}
    Should Be Equal As Integers    ${r.status_code}    201
    ${planos}=    GET Planos Do Paciente    ${paciente}
    Should Be Equal As Integers    ${planos.status_code}    200
    Should Be Equal    ${planos.json()}[0][itens][0][situacao]    CONCLUIDO
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Plano Tratamento - Procedimento Diferente Do Item - Status Code 422
    [Documentation]    The procedure must match the item tooth.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${plano}    ${item}    ${atendimento}    ${paciente}    ${matricula}=    Seed Plano Com Restauracao
    ${r}=    Executar A Restauracao    ${atendimento}    ${matricula}    ${item}    ${37}
    Should Be Equal As Integers    ${r.status_code}    422
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Plano Tratamento - Concluir Com Pendente - Status Code 422
    [Documentation]    A planned item blocks the conclusion.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${plano}    ${item}    ${atendimento}    ${paciente}    ${matricula}=    Seed Plano Com Restauracao
    ${r}=    POST Conclusao Plano    ${plano}
    Should Be Equal As Integers    ${r.status_code}    422
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Plano Tratamento - Concluir - Status Code 200
    [Documentation]    With the item executed, the treatment is concluded.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${plano}    ${item}    ${atendimento}    ${paciente}    ${matricula}=    Seed Plano Com Restauracao
    Executar A Restauracao    ${atendimento}    ${matricula}    ${item}
    ${r}=    POST Conclusao Plano    ${plano}
    Should Be Equal As Integers    ${r.status_code}    200
    Should Be Equal    ${r.json()}[situacao]    CONCLUIDO
    Should Not Be Equal    ${r.json()}[encerradoEm]    ${None}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Plano Tratamento - Abandono Sem Motivo - Status Code 400
    [Documentation]    Abandoning needs the reason.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${plano}    ${item}    ${atendimento}    ${paciente}    ${matricula}=    Seed Plano Com Restauracao
    ${vazio}=    Create Dictionary
    ${r}=    POST Abandono Plano    ${plano}    ${vazio}
    Should Be Equal As Integers    ${r.status_code}    400
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Plano Tratamento - Cancelar Item - Status Code 200
    [Documentation]    The dentist cancels an item with the reason.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${plano}    ${item}    ${atendimento}    ${paciente}    ${matricula}=    Seed Plano Com Restauracao
    ${payload}=    Create Dictionary    situacao=CANCELADO    motivo=Recusou
    ${r}=    POST Situacao Item Plano    ${item}    ${payload}
    Should Be Equal As Integers    ${r.status_code}    200
    Should Be Equal    ${r.json()}[itens][0][situacao]    CANCELADO
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Plano Tratamento - By HTTP Status Code
    [Documentation]    Executes the SB3b scenario for the given HTTP status code.
    [Arguments]    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Run Keyword If    '${meuHTTP}' == '201-abrir'    Plano Tratamento - Abrir - Status Code 201
    ...    ELSE IF    '${meuHTTP}' == '409-aberto'    Plano Tratamento - Segundo Plano Aberto - Status Code 409
    ...    ELSE IF    '${meuHTTP}' == '422-atendimento'    Plano Tratamento - Fora Do Atendimento Odontologico - Status Code 422
    ...    ELSE IF    '${meuHTTP}' == '201-executar'    Plano Tratamento - Executar Pelo Procedimento - Status Code 201
    ...    ELSE IF    '${meuHTTP}' == '422-item'    Plano Tratamento - Procedimento Diferente Do Item - Status Code 422
    ...    ELSE IF    '${meuHTTP}' == '422-concluir'    Plano Tratamento - Concluir Com Pendente - Status Code 422
    ...    ELSE IF    '${meuHTTP}' == '200-concluir'    Plano Tratamento - Concluir - Status Code 200
    ...    ELSE IF    '${meuHTTP}' == '400-abandono'    Plano Tratamento - Abandono Sem Motivo - Status Code 400
    ...    ELSE IF    '${meuHTTP}' == '200-cancelar'    Plano Tratamento - Cancelar Item - Status Code 200
    ...    ELSE    Fail    HTTP status code '${meuHTTP}' is not supported for Plano Tratamento test scenario.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
