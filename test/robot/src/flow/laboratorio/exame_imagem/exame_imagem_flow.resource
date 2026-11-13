*** Settings ***
Library     String
Library     Collections
Resource    ../../../pom/laboratorio/pedido_exame/pedido_exame_pom.resource
Resource    ../../../pom/laboratorio/ciencia_entrega/ciencia_entrega_pom.resource
Resource    ../create_exame_laboratorial/create_exame_laboratorial_flow.resource
Resource    ../../paciente/create_paciente/create_paciente_flow.resource
Resource    ../../setor/create_setor/create_setor_flow.resource
Resource    ../../equipamentos/modelo_operacional/modelo_operacional_flow.resource
Resource    ../../profissional/get_profissional_by_matricula/get_profissional_by_matricula_flow.resource
Resource    ../../common/schema_validation_flow.resource
Resource    ../../../resource/config/schema/schema.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
    Exames de imagem (ADR-0182): catálogo com modalidade, serviço de imagem com a capacidade DIAGNOSTICO_POR_IMAGEM, realização,
    laudo, adendo e o achado crítico entrando na comunicação da ADR-0181. Com a segurança desligada, o mesmo profissional semeado
    pede, realiza e lauda.
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
Seed A Exame De Imagem
    [Documentation]    Creates an X-ray exam in the catalog; returns exameId and nome.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${sufixo}=    Generate Random String    8    [LETTERS][NUMBERS]
    ${nome}=    Set Variable    Robot Raio-X Torax ${sufixo}
    ${payload}=    Create Dictionary    nome=${nome}    categoria=IMAGEM    modalidade=RAIO_X    preparo=Retirar objetos metálicos.
    ${criado}=    POST Create Exame Laboratorial    ${payload}
    Should Be Equal As Integers    ${criado.status_code}    201
    ${lista}=    GET List Exame Laboratorial
    ${exameId}=    Evaluate    [e['uuid'] for e in $lista.json() if e['nome'] == $nome][0]
    RETURN    ${exameId}    ${nome}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Seed A Servico De Imagem
    [Documentation]    Creates a unit with the DIAGNOSTICO profile and the DIAGNOSTICO_POR_IMAGEM capacity; returns its uuid.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${unidade}=    Seed A Unidade De Saude Uuid
    Incluir Perfil Por Codigo    ${unidade}    DIAGNOSTICO
    Incluir Capacidade Por Codigo    ${unidade}    DIAGNOSTICO_POR_IMAGEM
    RETURN    ${unidade}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Seed A Pedido De Imagem
    [Documentation]    Orders the X-ray for a fresh patient at a fresh imaging service; returns pedidoId, itemId, matricula and servico.
    [Arguments]    ${realizar}=${False}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${exameId}    ${nome}=    Seed A Exame De Imagem
    ${servico}=    Seed A Servico De Imagem
    ${pacienteId}    ${cpf}    ${cartaoSus}    ${pacienteNome}=    Seed A Paciente
    ${unidadeId}=    Seed A Unidade De Saude Uuid
    ${matricula}=    Seed A Profissional Matricula
    ${exames}=    Create List    ${exameId}
    ${pedido}=    Create Dictionary    pacienteId=${pacienteId}    unidadeSolicitanteId=${unidadeId}    profissionalMatricula=${matricula}
    ...    exameIds=${exames}    indicacaoClinica=Tosse e febre há 5 dias.    prioridade=ROTINA    unidadeExecutanteImagemId=${servico}
    ${criado}=    POST Create Pedido Exame    ${pedido}
    Should Be Equal As Integers    ${criado.status_code}    201
    ${lista}=    GET List Pedido Exame Do Paciente    ${pacienteId}
    ${pedidoId}=    Set Variable    ${lista.json()}[0][uuid]
    ${itemId}=    Set Variable    ${lista.json()}[0][itens][0][uuid]
    IF    ${realizar}
        ${r}=    Create Dictionary    profissionalMatricula=${matricula}    equipamento=Aparelho da sala 1
        ${feito}=    POST Acao Item Exame    ${itemId}    realizacao    ${r}
        Should Be Equal As Integers    ${feito.status_code}    200
    END
    RETURN    ${pedidoId}    ${itemId}    ${matricula}    ${servico}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Payload Laudo
    [Documentation]    Report payload for the given classification.
    [Arguments]    ${matricula}    ${classificacao}=NORMAL
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${payload}=    Create Dictionary    profissionalMatricula=${matricula}    tecnica=Incidências PA e perfil.
    ...    achados=Opacidade em base direita.    conclusao=Pneumonia em base direita.    classificacao=${classificacao}
    RETURN    ${payload}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Exame Imagem - Catalogo - Status Code 201 E 400
    [Documentation]    The imaging exam needs a modality (400); with it, 201 with category and no material.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${sem}=    Create Dictionary    nome=Robot Sem Modalidade    categoria=IMAGEM
    ${r}=    POST Create Exame Laboratorial    ${sem}
    Should Be Equal As Integers    ${r.status_code}    400
    ${exameId}    ${nome}=    Seed A Exame De Imagem
    ${lista}=    GET List Exame Laboratorial
    ${exame}=    Evaluate    [e for e in $lista.json() if e['uuid'] == $exameId][0]
    Should Be Equal    ${exame}[categoria]    IMAGEM
    Should Be Equal    ${exame}[modalidade]    RAIO_X
    Should Be Equal    ${exame}[material]    ${None}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Exame Imagem - Pedido - Status Code 400 E 422
    [Documentation]    Without the imaging service: 400; a unit without the capacity: 422.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${exameId}    ${nome}=    Seed A Exame De Imagem
    ${pacienteId}    ${cpf}    ${cartaoSus}    ${pacienteNome}=    Seed A Paciente
    ${unidadeId}=    Seed A Unidade De Saude Uuid
    ${matricula}=    Seed A Profissional Matricula
    ${exames}=    Create List    ${exameId}
    ${pedido}=    Create Dictionary    pacienteId=${pacienteId}    unidadeSolicitanteId=${unidadeId}    profissionalMatricula=${matricula}
    ...    exameIds=${exames}    indicacaoClinica=Tosse.    prioridade=ROTINA
    ${sem}=    POST Create Pedido Exame    ${pedido}
    Should Be Equal As Integers    ${sem.status_code}    400
    Set To Dictionary    ${pedido}    unidadeExecutanteImagemId=${unidadeId}
    ${semCapacidade}=    POST Create Pedido Exame    ${pedido}
    Should Be Equal As Integers    ${semCapacidade.status_code}    422
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Exame Imagem - Realizacao - Status Code 200
    [Documentation]    Realizes the exam, shows in the to-report list and in the order detail, with the schema.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${pedidoId}    ${itemId}    ${matricula}    ${servico}=    Seed A Pedido De Imagem
    ${antes}=    GET Trabalho Laboratorio    PARA_REALIZAR
    ${linha}=    Evaluate    [i for i in $antes.json() if i['itemId'] == $itemId][0]
    Should Be Equal    ${linha}[modalidade]    RAIO_X
    ${payload}=    Create Dictionary    profissionalMatricula=${matricula}    contraste=${True}    contrasteDescricao=Iodado não iônico
    ${r}=    POST Acao Item Exame    ${itemId}    realizacao    ${payload}
    Should Be Equal As Integers    ${r.status_code}    200
    ${depois}=    GET Trabalho Laboratorio    PARA_LAUDAR
    ${laudar}=    Evaluate    [i for i in $depois.json() if i['itemId'] == $itemId]
    Length Should Be    ${laudar}    1
    ${detalhe}=    GET Get Pedido Exame By Id    ${pedidoId}
    Should Be Equal    ${detalhe.json()}[itens][0][status]    REALIZADO
    Validate Object Schema    ${detalhe.json()}[realizacoes][0]    ${REALIZACAO_EXAME_IMAGEM_SCHEMA}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Exame Imagem - Realizacao - Status Code 400 - Contraste Sem Descricao
    [Documentation]    Contrast without the description, expecting HTTP 400.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${pedidoId}    ${itemId}    ${matricula}    ${servico}=    Seed A Pedido De Imagem
    ${payload}=    Create Dictionary    profissionalMatricula=${matricula}    contraste=${True}
    ${r}=    POST Acao Item Exame    ${itemId}    realizacao    ${payload}
    Should Be Equal As Integers    ${r.status_code}    400
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Exame Imagem - Laudo - Status Code 200
    [Documentation]    The signed report is released with the conclusion as the value.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${pedidoId}    ${itemId}    ${matricula}    ${servico}=    Seed A Pedido De Imagem    realizar=${True}
    ${payload}=    Payload Laudo    ${matricula}    ALTERADO
    ${r}=    POST Acao Item Exame    ${itemId}    laudo    ${payload}
    Should Be Equal As Integers    ${r.status_code}    200
    ${detalhe}=    GET Get Pedido Exame By Id    ${pedidoId}
    ${item}=    Set Variable    ${detalhe.json()}[itens][0]
    Should Be Equal    ${item}[status]    LIBERADO
    Should Be Equal    ${item}[resultado][laudoConclusao]    Pneumonia em base direita.
    Should Be Equal    ${item}[resultado][interpretacao]    ALTERADO
    Should Be Equal    ${detalhe.json()}[eventos][-1][tipo]    LAUDO
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Exame Imagem - Laudo - Status Code 422 - Sem Realizacao
    [Documentation]    A report before the exam is done, expecting HTTP 422.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${pedidoId}    ${itemId}    ${matricula}    ${servico}=    Seed A Pedido De Imagem
    ${payload}=    Payload Laudo    ${matricula}
    ${r}=    POST Acao Item Exame    ${itemId}    laudo    ${payload}
    Should Be Equal As Integers    ${r.status_code}    422
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Exame Imagem - Adendo - Status Code 200 E 400
    [Documentation]    The addendum needs a reason (400); with it, a new linked report.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${pedidoId}    ${itemId}    ${matricula}    ${servico}=    Seed A Pedido De Imagem    realizar=${True}
    ${payload}=    Payload Laudo    ${matricula}
    ${laudo}=    POST Acao Item Exame    ${itemId}    laudo    ${payload}
    Should Be Equal As Integers    ${laudo.status_code}    200
    ${sem}=    POST Acao Item Exame    ${itemId}    adendo    ${payload}
    Should Be Equal As Integers    ${sem.status_code}    400
    Set To Dictionary    ${payload}    motivo=Revisão com a incidência em perfil.
    ${ok}=    POST Acao Item Exame    ${itemId}    adendo    ${payload}
    Should Be Equal As Integers    ${ok.status_code}    200
    ${detalhe}=    GET Get Pedido Exame By Id    ${pedidoId}
    Should Not Be Equal    ${detalhe.json()}[itens][0][resultado][retificacaoDeId]    ${None}
    Should Be Equal    ${detalhe.json()}[eventos][-1][tipo]    ADENDO
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Exame Imagem - Achado Critico
    [Documentation]    A critical finding enters the critical list and is communicated by the radiologist.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${pedidoId}    ${itemId}    ${matricula}    ${servico}=    Seed A Pedido De Imagem    realizar=${True}
    ${payload}=    Payload Laudo    ${matricula}    ACHADO_CRITICO
    ${laudo}=    POST Acao Item Exame    ${itemId}    laudo    ${payload}
    Should Be Equal As Integers    ${laudo.status_code}    200
    ${lista}=    GET Criticos A Comunicar
    ${linha}=    Evaluate    [a for a in $lista.json() if a['itemId'] == $itemId][0]
    Should Be Equal    ${linha}[interpretacao]    ACHADO_CRITICO
    ${c}=    Create Dictionary    profissionalMatricula=${matricula}    receptorNome=Dr. Plantonista    meio=TELEFONE
    ${r}=    POST Acao Item Exame    ${itemId}    comunicacao-critico    ${c}
    Should Be Equal As Integers    ${r.status_code}    200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Exame Imagem - By Cenario
    [Documentation]    Executes the Exame Imagem scenario for the given case.
    [Arguments]    ${caso}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    IF    '${caso}' == 'CATALOGO'
        Exame Imagem - Catalogo - Status Code 201 E 400
    ELSE IF    '${caso}' == 'PEDIDO_400_422'
        Exame Imagem - Pedido - Status Code 400 E 422
    ELSE IF    '${caso}' == 'REALIZACAO'
        Exame Imagem - Realizacao - Status Code 200
    ELSE IF    '${caso}' == 'REALIZACAO_400'
        Exame Imagem - Realizacao - Status Code 400 - Contraste Sem Descricao
    ELSE IF    '${caso}' == 'LAUDO'
        Exame Imagem - Laudo - Status Code 200
    ELSE IF    '${caso}' == 'LAUDO_422'
        Exame Imagem - Laudo - Status Code 422 - Sem Realizacao
    ELSE IF    '${caso}' == 'ADENDO'
        Exame Imagem - Adendo - Status Code 200 E 400
    ELSE IF    '${caso}' == 'ACHADO_CRITICO'
        Exame Imagem - Achado Critico
    ELSE
        Fail    Caso '${caso}' não suportado no cenário Exame Imagem.
    END
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
