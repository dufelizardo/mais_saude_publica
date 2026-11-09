*** Settings ***
Library     String
Library     Collections
Resource    ../../../pom/laboratorio/faixa_referencia/faixa_referencia_pom.resource
Resource    ../../../pom/laboratorio/pedido_exame/pedido_exame_pom.resource
Resource    ../create_exame_laboratorial/create_exame_laboratorial_flow.resource
Resource    ../../paciente/create_paciente/create_paciente_flow.resource
Resource    ../../setor/create_setor/create_setor_flow.resource
Resource    ../../profissional/get_profissional_by_matricula/get_profissional_by_matricula_flow.resource
Resource    ../../common/schema_validation_flow.resource
Resource    ../../../resource/config/schema/schema.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
    Faixas de referência por sexo e idade, valores críticos e evolução do exame (ADR-0180). O paciente semeado é mulher
    nascida em 1990 (Seed A Paciente); o exame semeado tem a faixa geral de 12 a 16 g/dL.
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
Payload Faixa Feminina Adulta
    [Documentation]    Women from 12 to 59 years: 11.5 to 15 g/dL, critical below 7 or above 20.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${payload}=    Create Dictionary    sexo=FEMININO    idadeMinima=${12}    unidadeIdadeMinima=ANOS    idadeMaxima=${60}    unidadeIdadeMaxima=ANOS
    ...    referenciaMinima=${11.5}    referenciaMaxima=${15}    criticoMinimo=${7}    criticoMaximo=${20}
    RETURN    ${payload}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Seed A Exame Com Faixa Feminina
    [Documentation]    Seeds a numeric exam and adds the adult female range; returns exameId, nome and faixaId.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${exameId}    ${nome}=    Seed A Exame Laboratorial
    ${payload}=    Payload Faixa Feminina Adulta
    ${response}=    POST Create Faixa Referencia    ${exameId}    ${payload}
    Should Be Equal As Integers    ${response.status_code}    201
    ${faixaId}=    Set Variable    ${response.json()}[faixas][0][uuid]
    RETURN    ${exameId}    ${nome}    ${faixaId}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Registrar Resultado Do Exame
    [Documentation]    Orders the exam for the patient, collects, registers the value and optionally releases; returns pedidoId
    ...    and itemId.
    [Arguments]    ${pacienteId}    ${exameId}    ${unidadeId}    ${matricula}    ${valor}    ${liberar}=${True}    ${diasAtras}=${0}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${exames}=    Create List    ${exameId}
    ${pedido}=    Create Dictionary    pacienteId=${pacienteId}    unidadeSolicitanteId=${unidadeId}    profissionalMatricula=${matricula}
    ...    exameIds=${exames}    indicacaoClinica=Astenia e palidez.    prioridade=ROTINA
    ${criado}=    POST Create Pedido Exame    ${pedido}
    Should Be Equal As Integers    ${criado.status_code}    201
    ${lista}=    GET List Pedido Exame Do Paciente    ${pacienteId}
    ${pedidoId}=    Evaluate    [p['uuid'] for p in $lista.json() if p['situacao'] == 'AGUARDANDO_COLETA'][0]
    ${coletadaEm}=    Evaluate    (datetime.datetime.now() - datetime.timedelta(days=int($diasAtras), minutes=1)).replace(microsecond=0).isoformat()    modules=datetime
    ${coleta}=    Create Dictionary    profissionalMatricula=${matricula}    unidadeColetaId=${unidadeId}    coletadaEm=${coletadaEm}
    ${coletado}=    POST Coletar Pedido Exame    ${pedidoId}    ${coleta}
    Should Be Equal As Integers    ${coletado.status_code}    200
    ${detalhe}=    GET Get Pedido Exame By Id    ${pedidoId}
    ${itemId}=    Set Variable    ${detalhe.json()}[itens][0][uuid]
    ${resultado}=    Create Dictionary    profissionalMatricula=${matricula}    valorNumerico=${valor}
    ${registrado}=    POST Acao Item Exame    ${itemId}    resultado    ${resultado}
    Should Be Equal As Integers    ${registrado.status_code}    200
    IF    ${liberar}
        ${lib}=    Create Dictionary    profissionalMatricula=${matricula}
        ${liberado}=    POST Acao Item Exame    ${itemId}    liberacao    ${lib}
        Should Be Equal As Integers    ${liberado.status_code}    200
    END
    RETURN    ${pedidoId}    ${itemId}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Faixa Referencia - Incluir - Status Code 201
    [Documentation]    Adds the adult female range and validates the label and the schema.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${exameId}    ${nome}    ${faixaId}=    Seed A Exame Com Faixa Feminina
    ${exame}=    GET Exame Laboratorial By Id    ${exameId}
    Should Be Equal As Integers    ${exame.status_code}    200
    Should Be Equal    ${exame.json()}[faixas][0][rotulo]    Feminino, 12 a 59 anos
    Validate Object Schema    ${exame.json()}[faixas][0]    ${FAIXA_REFERENCIA_EXAME_SCHEMA}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Faixa Referencia - Incluir - Status Code 409 - Sobreposicao
    [Documentation]    A second female range whose ages overlap the first, expecting HTTP 409.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${exameId}    ${nome}    ${faixaId}=    Seed A Exame Com Faixa Feminina
    ${payload}=    Create Dictionary    sexo=FEMININO    idadeMinima=${50}    unidadeIdadeMinima=ANOS    referenciaMinima=${11}
    ${response}=    POST Create Faixa Referencia    ${exameId}    ${payload}
    Should Be Equal As Integers    ${response.status_code}    409
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Faixa Referencia - Incluir - Status Code 400 - Sexo Ignorado
    [Documentation]    IGNORADO is not a range sex, expecting HTTP 400.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${exameId}    ${nome}=    Seed A Exame Laboratorial
    ${payload}=    Create Dictionary    sexo=IGNORADO    idadeMinima=${0}    unidadeIdadeMinima=DIAS    referenciaMinima=${10}
    ${response}=    POST Create Faixa Referencia    ${exameId}    ${payload}
    Should Be Equal As Integers    ${response.status_code}    400
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Faixa Referencia - Incluir - Status Code 422 - Exame Em Texto
    [Documentation]    A text exam does not take ranges, expecting HTTP 422.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${sufixo}=    Generate Random String    8    [LETTERS][NUMBERS]
    ${criar}=    Create Dictionary    nome=Robot Exame Texto ${sufixo}    material=URINA    tipoResultado=TEXTO
    ${criado}=    POST Create Exame Laboratorial    ${criar}
    Should Be Equal As Integers    ${criado.status_code}    201
    ${lista}=    GET List Exame Laboratorial
    ${exameId}=    Evaluate    [e['uuid'] for e in $lista.json() if e['nome'] == 'Robot Exame Texto ' + $sufixo][0]
    ${payload}=    Create Dictionary    idadeMinima=${0}    unidadeIdadeMinima=DIAS    referenciaMinima=${1}
    ${response}=    POST Create Faixa Referencia    ${exameId}    ${payload}
    Should Be Equal As Integers    ${response.status_code}    422
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Faixa Referencia - Editar E Remover - Status Code 200
    [Documentation]    Edits the range, removes it, and a second removal gives HTTP 404.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${exameId}    ${nome}    ${faixaId}=    Seed A Exame Com Faixa Feminina
    ${payload}=    Payload Faixa Feminina Adulta
    Set To Dictionary    ${payload}    referenciaMinima=${11}    descricao=Adulta
    ${editado}=    PUT Update Faixa Referencia    ${faixaId}    ${payload}
    Should Be Equal As Integers    ${editado.status_code}    200
    Should Be Equal    ${editado.json()}[faixas][0][rotulo]    Adulta (Feminino, 12 a 59 anos)
    ${removido}=    DELETE Faixa Referencia    ${faixaId}
    Should Be Equal As Integers    ${removido.status_code}    200
    Should Be Empty    ${removido.json()}[faixas]
    ${denovo}=    DELETE Faixa Referencia    ${faixaId}
    Should Be Equal As Integers    ${denovo.status_code}    404
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Faixa Referencia - Resultado Usa A Faixa Do Sexo E Da Idade
    [Documentation]    11.8 is low for the general range (12-16) and normal for the adult female range (11.5-15).
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${exameId}    ${nome}    ${faixaId}=    Seed A Exame Com Faixa Feminina
    ${pacienteId}    ${cpf}    ${cartaoSus}    ${pacienteNome}=    Seed A Paciente
    ${unidadeId}=    Seed A Unidade De Saude Uuid
    ${matricula}=    Seed A Profissional Matricula
    ${pedidoId}    ${itemId}=    Registrar Resultado Do Exame    ${pacienteId}    ${exameId}    ${unidadeId}    ${matricula}    ${11.8}
    ${detalhe}=    GET Get Pedido Exame By Id    ${pedidoId}
    Should Be Equal    ${detalhe.json()}[itens][0][resultado][interpretacao]    NORMAL
    Should Be Equal    ${detalhe.json()}[itens][0][resultado][faixaDescricao]    Feminino, 12 a 59 anos
    Should Be Equal As Numbers    ${detalhe.json()}[itens][0][resultado][referenciaMinima]    11.5
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Faixa Referencia - Resultado Critico
    [Documentation]    6 is below the critical minimum (7) of the female range: CRITICO_BAIXO.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${exameId}    ${nome}    ${faixaId}=    Seed A Exame Com Faixa Feminina
    ${pacienteId}    ${cpf}    ${cartaoSus}    ${pacienteNome}=    Seed A Paciente
    ${unidadeId}=    Seed A Unidade De Saude Uuid
    ${matricula}=    Seed A Profissional Matricula
    ${pedidoId}    ${itemId}=    Registrar Resultado Do Exame    ${pacienteId}    ${exameId}    ${unidadeId}    ${matricula}    ${6}
    ${detalhe}=    GET Get Pedido Exame By Id    ${pedidoId}
    Should Be Equal    ${detalhe.json()}[itens][0][resultado][interpretacao]    CRITICO_BAIXO
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Faixa Referencia - Evolucao Do Exame - Status Code 200
    [Documentation]    Two released results and one not released: the evolution brings the two, in collection order.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${exameId}    ${nome}    ${faixaId}=    Seed A Exame Com Faixa Feminina
    ${pacienteId}    ${cpf}    ${cartaoSus}    ${pacienteNome}=    Seed A Paciente
    ${unidadeId}=    Seed A Unidade De Saude Uuid
    ${matricula}=    Seed A Profissional Matricula
    Registrar Resultado Do Exame    ${pacienteId}    ${exameId}    ${unidadeId}    ${matricula}    ${10.2}    diasAtras=${30}
    Registrar Resultado Do Exame    ${pacienteId}    ${exameId}    ${unidadeId}    ${matricula}    ${12.4}    diasAtras=${2}
    Registrar Resultado Do Exame    ${pacienteId}    ${exameId}    ${unidadeId}    ${matricula}    ${14}    liberar=${False}
    ${response}=    GET Evolucao Do Exame    ${pacienteId}    ${exameId}
    Should Be Equal As Integers    ${response.status_code}    200
    Validate Object Schema    ${response.json()}    ${EVOLUCAO_EXAME_SCHEMA}
    Length Should Be    ${response.json()}[pontos]    2
    Should Be Equal As Numbers    ${response.json()}[pontos][0][valor]    10.2
    Should Be Equal As Numbers    ${response.json()}[pontos][1][valor]    12.4
    Should Be Equal    ${response.json()}[pontos][0][interpretacao]    ABAIXO
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Faixa Referencia - Evolucao Do Exame - Status Code 404
    [Documentation]    Unknown exam, expecting HTTP 404.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${pacienteId}    ${cpf}    ${cartaoSus}    ${pacienteNome}=    Seed A Paciente
    ${desconhecido}=    Evaluate    str(__import__('uuid').uuid4())
    ${response}=    GET Evolucao Do Exame    ${pacienteId}    ${desconhecido}
    Should Be Equal As Integers    ${response.status_code}    404
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Faixa Referencia - By Cenario
    [Documentation]    Executes the Faixa Referencia scenario for the given case.
    [Arguments]    ${caso}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    IF    '${caso}' == '201'
        Faixa Referencia - Incluir - Status Code 201
    ELSE IF    '${caso}' == '409'
        Faixa Referencia - Incluir - Status Code 409 - Sobreposicao
    ELSE IF    '${caso}' == '400'
        Faixa Referencia - Incluir - Status Code 400 - Sexo Ignorado
    ELSE IF    '${caso}' == '422'
        Faixa Referencia - Incluir - Status Code 422 - Exame Em Texto
    ELSE IF    '${caso}' == 'EDITAR_REMOVER'
        Faixa Referencia - Editar E Remover - Status Code 200
    ELSE IF    '${caso}' == 'FAIXA_DO_PACIENTE'
        Faixa Referencia - Resultado Usa A Faixa Do Sexo E Da Idade
    ELSE IF    '${caso}' == 'CRITICO'
        Faixa Referencia - Resultado Critico
    ELSE IF    '${caso}' == 'EVOLUCAO'
        Faixa Referencia - Evolucao Do Exame - Status Code 200
    ELSE IF    '${caso}' == 'EVOLUCAO_404'
        Faixa Referencia - Evolucao Do Exame - Status Code 404
    ELSE
        Fail    Caso '${caso}' não suportado no cenário Faixa Referencia.
    END
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
