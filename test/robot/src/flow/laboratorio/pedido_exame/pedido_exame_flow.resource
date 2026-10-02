*** Settings ***
Library     String
Library     Collections
Resource    ../../../pom/laboratorio/pedido_exame/pedido_exame_pom.resource
Resource    ../create_exame_laboratorial/create_exame_laboratorial_flow.resource
Resource    ../../paciente/create_paciente/create_paciente_flow.resource
Resource    ../../setor/create_setor/create_setor_flow.resource
Resource    ../../profissional/get_profissional_by_matricula/get_profissional_by_matricula_flow.resource
Resource    ../../common/schema_validation_flow.resource
Resource    ../../../pom/prontuario/get_prontuario_by_paciente_id/get_prontuario_by_paciente_id_pom.resource
Resource    ../../../resource/config/schema/schema.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
    Pedido, coleta, resultado, liberação, retificação, rejeição e cancelamento (ADR-0093).
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
Seed A Pedido Exame
    [Documentation]    Seeds patient, unit, professional and exam and orders it; returns pedidoId, itemId, matrícula and unidadeId.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${pacienteId}    ${cpf}    ${cartaoSus}    ${nome}=    Seed A Paciente
    ${exameId}    ${exameNome}=    Seed A Exame Laboratorial
    ${unidadeId}=    Seed A Unidade De Saude Uuid
    ${matricula}=    Seed A Profissional Matricula
    ${exames}=    Create List    ${exameId}
    ${payload}=    Create Dictionary    pacienteId=${pacienteId}    unidadeSolicitanteId=${unidadeId}    profissionalMatricula=${matricula}    exameIds=${exames}    indicacaoClinica=Astenia e palidez.    prioridade=ROTINA
    ${criado}=    POST Create Pedido Exame    ${payload}
    Should Be Equal As Integers    ${criado.status_code}    201
    ${lista}=    GET List Pedido Exame Do Paciente    ${pacienteId}
    ${pedidoId}=    Set Variable    ${lista.json()}[0][uuid]
    ${itemId}=    Set Variable    ${lista.json()}[0][itens][0][uuid]
    RETURN    ${pedidoId}    ${itemId}    ${matricula}    ${unidadeId}    ${pacienteId}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Seed A Pedido Exame Coletado
    [Documentation]    Seeds an order and collects it at the same unit; also returns the sample id.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${pedidoId}    ${itemId}    ${matricula}    ${unidadeId}    ${pacienteId}=    Seed A Pedido Exame
    ${coleta}=    Create Dictionary    profissionalMatricula=${matricula}    unidadeColetaId=${unidadeId}
    ${coletado}=    POST Coletar Pedido Exame    ${pedidoId}    ${coleta}
    Should Be Equal As Integers    ${coletado.status_code}    200
    ${detalhe}=    GET Get Pedido Exame By Id    ${pedidoId}
    ${amostraId}=    Set Variable    ${detalhe.json()}[amostras][0][uuid]
    RETURN    ${pedidoId}    ${itemId}    ${matricula}    ${amostraId}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Pedido Exame - Criar - Status Code 201
    [Documentation]    Orders an exam and validates summary and detail.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${pedidoId}    ${itemId}    ${matricula}    ${unidadeId}    ${pacienteId}=    Seed A Pedido Exame
    ${lista}=    GET List Pedido Exame Do Paciente    ${pacienteId}
    Validate Array Schema    ${lista.json()}    ${PEDIDO_EXAME_RESUMO_SCHEMA}
    Should Be Equal    ${lista.json()}[0][situacao]    AGUARDANDO_COLETA
    ${detalhe}=    GET Get Pedido Exame By Id    ${pedidoId}
    Validate Object Schema    ${detalhe.json()}    ${PEDIDO_EXAME_SCHEMA}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Pedido Exame - Criar - Status Code 400 - Exame Repetido
    [Documentation]    Orders the same exam twice in one order, expecting HTTP 400.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${pacienteId}    ${cpf}    ${cartaoSus}    ${nome}=    Seed A Paciente
    ${exameId}    ${exameNome}=    Seed A Exame Laboratorial
    ${unidadeId}=    Seed A Unidade De Saude Uuid
    ${matricula}=    Seed A Profissional Matricula
    ${exames}=    Create List    ${exameId}    ${exameId}
    ${payload}=    Create Dictionary    pacienteId=${pacienteId}    unidadeSolicitanteId=${unidadeId}    profissionalMatricula=${matricula}    exameIds=${exames}    indicacaoClinica=Teste.    prioridade=ROTINA
    ${response}=    POST Create Pedido Exame    ${payload}
    Should Be Equal As Integers    ${response.status_code}    400
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Pedido Exame - Detalhe - Status Code 404
    [Documentation]    Reads a random order, expecting HTTP 404.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${uuid}=    Evaluate    str(uuid.uuid4())    modules=uuid
    ${response}=    GET Get Pedido Exame By Id    ${uuid}
    Should Be Equal As Integers    ${response.status_code}    404
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Pedido Exame - Coleta - Status Code 200
    [Documentation]    Collects and checks the sample and the worklist.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${pedidoId}    ${itemId}    ${matricula}    ${amostraId}=    Seed A Pedido Exame Coletado
    ${detalhe}=    GET Get Pedido Exame By Id    ${pedidoId}
    Should Be Equal    ${detalhe.json()}[itens][0][status]    COLETADO
    ${trabalho}=    GET Trabalho Laboratorio    EM_ANALISE
    Should Be Equal As Integers    ${trabalho.status_code}    200
    Validate Array Schema    ${trabalho.json()}    ${ITEM_TRABALHO_EXAME_SCHEMA}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Pedido Exame - Coleta - Status Code 422 - Nada Aguardando
    [Documentation]    Collects twice, expecting HTTP 422.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${pedidoId}    ${itemId}    ${matricula}    ${unidadeId}    ${pacienteId}=    Seed A Pedido Exame
    ${coleta}=    Create Dictionary    profissionalMatricula=${matricula}    unidadeColetaId=${unidadeId}
    ${primeira}=    POST Coletar Pedido Exame    ${pedidoId}    ${coleta}
    Should Be Equal As Integers    ${primeira.status_code}    200
    ${response}=    POST Coletar Pedido Exame    ${pedidoId}    ${coleta}
    Should Be Equal As Integers    ${response.status_code}    422
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Pedido Exame - Resultado E Liberacao - Status Code 200
    [Documentation]    Registers 10.5 (below 12-16), releases it and reads ABAIXO.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${pedidoId}    ${itemId}    ${matricula}    ${amostraId}=    Seed A Pedido Exame Coletado
    ${payload}=    Create Dictionary    profissionalMatricula=${matricula}    valorNumerico=${10.5}
    ${registrado}=    POST Acao Item Exame    ${itemId}    resultado    ${payload}
    Should Be Equal As Integers    ${registrado.status_code}    200
    ${lib}=    Create Dictionary    profissionalMatricula=${matricula}
    ${liberado}=    POST Acao Item Exame    ${itemId}    liberacao    ${lib}
    Should Be Equal As Integers    ${liberado.status_code}    200
    ${detalhe}=    GET Get Pedido Exame By Id    ${pedidoId}
    Should Be Equal    ${detalhe.json()}[itens][0][status]    LIBERADO
    Should Be Equal    ${detalhe.json()}[itens][0][resultado][interpretacao]    ABAIXO
    Should Be Equal    ${detalhe.json()}[situacao]    CONCLUIDO
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Pedido Exame - Resultado - Status Code 400 - Sem Valor
    [Documentation]    Registers a numeric exam without value, expecting HTTP 400.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${pedidoId}    ${itemId}    ${matricula}    ${amostraId}=    Seed A Pedido Exame Coletado
    ${payload}=    Create Dictionary    profissionalMatricula=${matricula}    valorTexto=baixo
    ${response}=    POST Acao Item Exame    ${itemId}    resultado    ${payload}
    Should Be Equal As Integers    ${response.status_code}    400
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Pedido Exame - Liberacao - Status Code 422 - Sem Resultado
    [Documentation]    Releases without result, expecting HTTP 422.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${pedidoId}    ${itemId}    ${matricula}    ${amostraId}=    Seed A Pedido Exame Coletado
    ${lib}=    Create Dictionary    profissionalMatricula=${matricula}
    ${response}=    POST Acao Item Exame    ${itemId}    liberacao    ${lib}
    Should Be Equal As Integers    ${response.status_code}    422
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Pedido Exame - Retificacao - Status Code 200
    [Documentation]    Corrects a released result with a reason.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${pedidoId}    ${itemId}    ${matricula}    ${amostraId}=    Seed A Pedido Exame Coletado
    ${payload}=    Create Dictionary    profissionalMatricula=${matricula}    valorNumerico=${10.5}
    ${registrado}=    POST Acao Item Exame    ${itemId}    resultado    ${payload}
    Should Be Equal As Integers    ${registrado.status_code}    200
    ${lib}=    Create Dictionary    profissionalMatricula=${matricula}
    ${liberado}=    POST Acao Item Exame    ${itemId}    liberacao    ${lib}
    Should Be Equal As Integers    ${liberado.status_code}    200
    ${payload}=    Create Dictionary    profissionalMatricula=${matricula}    valorNumerico=${13.5}    motivo=Erro de digitação
    ${response}=    POST Acao Item Exame    ${itemId}    retificacao    ${payload}
    Should Be Equal As Integers    ${response.status_code}    200
    ${detalhe}=    GET Get Pedido Exame By Id    ${pedidoId}
    Should Be Equal    ${detalhe.json()}[itens][0][resultado][motivoRetificacao]    Erro de digitação
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Pedido Exame - Retificacao - Status Code 400 - Sem Motivo
    [Documentation]    Corrects without reason, expecting HTTP 400.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${pedidoId}    ${itemId}    ${matricula}    ${amostraId}=    Seed A Pedido Exame Coletado
    ${payload}=    Create Dictionary    profissionalMatricula=${matricula}    valorNumerico=${12.9}
    ${registrado}=    POST Acao Item Exame    ${itemId}    resultado    ${payload}
    Should Be Equal As Integers    ${registrado.status_code}    200
    ${lib}=    Create Dictionary    profissionalMatricula=${matricula}
    ${liberado}=    POST Acao Item Exame    ${itemId}    liberacao    ${lib}
    Should Be Equal As Integers    ${liberado.status_code}    200
    ${payload}=    Create Dictionary    profissionalMatricula=${matricula}    valorNumerico=${13}
    ${response}=    POST Acao Item Exame    ${itemId}    retificacao    ${payload}
    Should Be Equal As Integers    ${response.status_code}    400
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Pedido Exame - Rejeicao - Status Code 200
    [Documentation]    Rejects the sample and the exam goes back to collection.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${pedidoId}    ${itemId}    ${matricula}    ${amostraId}=    Seed A Pedido Exame Coletado
    ${payload}=    Create Dictionary    profissionalMatricula=${matricula}    motivo=HEMOLISADA
    ${response}=    POST Rejeitar Amostra Exame    ${amostraId}    ${payload}
    Should Be Equal As Integers    ${response.status_code}    200
    ${detalhe}=    GET Get Pedido Exame By Id    ${pedidoId}
    Should Be Equal    ${detalhe.json()}[itens][0][status]    SOLICITADO
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Pedido Exame - Rejeicao - Status Code 422 - Ja Rejeitada
    [Documentation]    Rejects twice, expecting HTTP 422.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${pedidoId}    ${itemId}    ${matricula}    ${amostraId}=    Seed A Pedido Exame Coletado
    ${payload}=    Create Dictionary    profissionalMatricula=${matricula}    motivo=INSUFICIENTE
    ${primeira}=    POST Rejeitar Amostra Exame    ${amostraId}    ${payload}
    Should Be Equal As Integers    ${primeira.status_code}    200
    ${response}=    POST Rejeitar Amostra Exame    ${amostraId}    ${payload}
    Should Be Equal As Integers    ${response.status_code}    422
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Pedido Exame - Cancelamento - Status Code 200
    [Documentation]    Cancels an exam not yet released.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${pedidoId}    ${itemId}    ${matricula}    ${unidadeId}    ${pacienteId}=    Seed A Pedido Exame
    ${payload}=    Create Dictionary    profissionalMatricula=${matricula}    motivo=Pedido em duplicidade
    ${response}=    POST Acao Item Exame    ${itemId}    cancelamento    ${payload}
    Should Be Equal As Integers    ${response.status_code}    200
    ${detalhe}=    GET Get Pedido Exame By Id    ${pedidoId}
    Should Be Equal    ${detalhe.json()}[situacao]    CANCELADO
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Pedido Exame - Cancelamento - Status Code 422 - Liberado
    [Documentation]    Cancels a released exam, expecting HTTP 422.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${pedidoId}    ${itemId}    ${matricula}    ${amostraId}=    Seed A Pedido Exame Coletado
    ${payload}=    Create Dictionary    profissionalMatricula=${matricula}    valorNumerico=${12.9}
    ${registrado}=    POST Acao Item Exame    ${itemId}    resultado    ${payload}
    Should Be Equal As Integers    ${registrado.status_code}    200
    ${lib}=    Create Dictionary    profissionalMatricula=${matricula}
    ${liberado}=    POST Acao Item Exame    ${itemId}    liberacao    ${lib}
    Should Be Equal As Integers    ${liberado.status_code}    200
    ${payload}=    Create Dictionary    profissionalMatricula=${matricula}    motivo=Tarde
    ${response}=    POST Acao Item Exame    ${itemId}    cancelamento    ${payload}
    Should Be Equal As Integers    ${response.status_code}    422
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Pedido Exame - Trabalho - Status Code 400 - Etapa Invalida
    [Documentation]    Asks for an unknown stage, expecting HTTP 400.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${response}=    GET Trabalho Laboratorio    QUALQUER
    Should Be Equal As Integers    ${response.status_code}    400
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Pedido Exame - Trabalho - Recoleta Com Motivo
    [Documentation]    Rejects the sample and reads the exam back in PARA_COLETAR flagged as recollection, with the reason (ADR-0095).
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${pedidoId}    ${itemId}    ${matricula}    ${amostraId}=    Seed A Pedido Exame Coletado
    ${payload}=    Create Dictionary    profissionalMatricula=${matricula}    motivo=COAGULADA
    ${rejeitada}=    POST Rejeitar Amostra Exame    ${amostraId}    ${payload}
    Should Be Equal As Integers    ${rejeitada.status_code}    200
    ${lista}=    GET Trabalho Laboratorio    PARA_COLETAR
    Should Be Equal As Integers    ${lista.status_code}    200
    Validate Array Schema    ${lista.json()}    ${ITEM_TRABALHO_EXAME_SCHEMA}
    ${motivos}=    Evaluate    [i.get("motivoRecoleta") for i in $lista.json() if i["itemId"] == $itemId]
    Should Be Equal    ${motivos}[0]    COAGULADA
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Pedido Exame - Prontuario - Resultado So Depois De Liberado
    [Documentation]    Reads the exams in the patient record: no value while only registered, the value and interpretation after release (ADR-0095).
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${pedidoId}    ${itemId}    ${matricula}    ${unidadeId}    ${pacienteId}=    Seed A Pedido Exame
    ${coleta}=    Create Dictionary    profissionalMatricula=${matricula}    unidadeColetaId=${unidadeId}
    ${coletado}=    POST Coletar Pedido Exame    ${pedidoId}    ${coleta}
    Should Be Equal As Integers    ${coletado.status_code}    200
    ${payload}=    Create Dictionary    profissionalMatricula=${matricula}    valorNumerico=${17}
    ${registrado}=    POST Acao Item Exame    ${itemId}    resultado    ${payload}
    Should Be Equal As Integers    ${registrado.status_code}    200
    ${antes}=    GET Get Prontuario By Paciente Id    ${pacienteId}
    Should Be Equal As Integers    ${antes.status_code}    200
    Validate Array Schema    ${antes.json()}[exames]    ${EXAME_PRONTUARIO_SCHEMA}
    Should Be Equal    ${antes.json()}[exames][0][status]    RESULTADO_REGISTRADO
    Should Be Equal    ${antes.json()}[exames][0][resultado]    ${None}
    ${lib}=    Create Dictionary    profissionalMatricula=${matricula}
    ${liberado}=    POST Acao Item Exame    ${itemId}    liberacao    ${lib}
    Should Be Equal As Integers    ${liberado.status_code}    200
    ${depois}=    GET Get Prontuario By Paciente Id    ${pacienteId}
    Should Be Equal    ${depois.json()}[exames][0][status]    LIBERADO
    Should Be Equal    ${depois.json()}[exames][0][resultado][interpretacao]    ACIMA
    Should Be Equal    ${depois.json()}[exames][0][pedidoId]    ${pedidoId}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Pedido Exame - By HTTP Status Code
    [Documentation]    Executes the Pedido Exame scenario for the given HTTP status code.
    [Arguments]    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Run Keyword If    '${meuHTTP}' == '201'    Pedido Exame - Criar - Status Code 201
    ...    ELSE IF    '${meuHTTP}' == '400-repetido'    Pedido Exame - Criar - Status Code 400 - Exame Repetido
    ...    ELSE IF    '${meuHTTP}' == '404'    Pedido Exame - Detalhe - Status Code 404
    ...    ELSE IF    '${meuHTTP}' == '200-coleta'    Pedido Exame - Coleta - Status Code 200
    ...    ELSE IF    '${meuHTTP}' == '422-coleta'    Pedido Exame - Coleta - Status Code 422 - Nada Aguardando
    ...    ELSE IF    '${meuHTTP}' == '200-liberacao'    Pedido Exame - Resultado E Liberacao - Status Code 200
    ...    ELSE IF    '${meuHTTP}' == '400-resultado'    Pedido Exame - Resultado - Status Code 400 - Sem Valor
    ...    ELSE IF    '${meuHTTP}' == '422-liberacao'    Pedido Exame - Liberacao - Status Code 422 - Sem Resultado
    ...    ELSE IF    '${meuHTTP}' == '200-retificacao'    Pedido Exame - Retificacao - Status Code 200
    ...    ELSE IF    '${meuHTTP}' == '400-retificacao'    Pedido Exame - Retificacao - Status Code 400 - Sem Motivo
    ...    ELSE IF    '${meuHTTP}' == '200-rejeicao'    Pedido Exame - Rejeicao - Status Code 200
    ...    ELSE IF    '${meuHTTP}' == '422-rejeicao'    Pedido Exame - Rejeicao - Status Code 422 - Ja Rejeitada
    ...    ELSE IF    '${meuHTTP}' == '200-cancelamento'    Pedido Exame - Cancelamento - Status Code 200
    ...    ELSE IF    '${meuHTTP}' == '422-cancelamento'    Pedido Exame - Cancelamento - Status Code 422 - Liberado
    ...    ELSE IF    '${meuHTTP}' == '400-trabalho'    Pedido Exame - Trabalho - Status Code 400 - Etapa Invalida
    ...    ELSE IF    '${meuHTTP}' == '200-recoleta'    Pedido Exame - Trabalho - Recoleta Com Motivo
    ...    ELSE IF    '${meuHTTP}' == '200-prontuario'    Pedido Exame - Prontuario - Resultado So Depois De Liberado
    ...    ELSE    Fail    HTTP status code '${meuHTTP}' is not supported for Pedido Exame test scenario.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
