*** Settings ***
Library     String
Resource    ../../../../pom/ui/assistencia/laboratorio/laboratorio_ui_pom.resource
Resource    ../../../laboratorio/pedido_exame/pedido_exame_flow.resource
Resource    ../../../atendimento/create_atendimento/create_atendimento_flow.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
    Os dados de apoio (paciente, unidade, profissional, exame, pedido, coleta) são criados pela API; a ação sob
    teste é feita pela tela.
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
UI Laboratorio - Ler Pedido
    [Documentation]    Lê o pedido pela API e devolve o nome do paciente e do primeiro exame.
    [Arguments]    ${pedidoId}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${detalhe}=    GET Get Pedido Exame By Id    ${pedidoId}
    RETURN    ${detalhe.json()}[pacienteNome]    ${detalhe.json()}[itens][0][exameNome]
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Laboratorio - Status Do Item Deve Ser
    [Documentation]    Confere pela API a situação do primeiro exame do pedido.
    [Arguments]    ${pedidoId}    ${esperado}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${detalhe}=    GET Get Pedido Exame By Id    ${pedidoId}
    Should Be Equal    ${detalhe.json()}[itens][0][status]    ${esperado}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Laboratorio - Pedido Pela Gaveta
    [Documentation]    Faz um pedido de exames pela gaveta e vê o aviso e a linha na aba Pedidos.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${pacienteId}    ${cpf}    ${cartaoSus}    ${nomePaciente}=    Seed A Paciente
    ${exameId}    ${exameNome}=    Seed A Exame Laboratorial
    ${unidadeId}=    Seed A Unidade De Saude Uuid
    ${matricula}=    Seed A Profissional Matricula
    UI Laboratorio - Abrir Aba    pedidos
    Clicar Botao    Pedido de exames
    ${titulo}=    Ler Titulo Da Gaveta
    Should Be Equal    ${titulo}    Pedido de exames
    Preencher Campo Da Gaveta    buscaPaciente    ${nomePaciente}
    Escolher Opcao Da Gaveta    pacienteId    ${pacienteId}
    Escolher Opcao Da Gaveta    unidadeSolicitanteId    ${unidadeId}
    Preencher Campo Da Gaveta    profissionalMatricula    ${matricula}
    Preencher Campo Da Gaveta    buscaExame    ${exameNome}
    UI Laboratorio - Marcar Na Gaveta    ${exameNome}
    Preencher Campo Da Gaveta    indicacaoClinica    Astenia e palidez há dois meses.
    Preencher Campo Da Gaveta    cid    D64.9
    Enviar Gaveta
    ${aviso}=    Ler Aviso De Status
    Should Contain    ${aviso}    Pedido de exames registrado
    Esperar Texto Aparecer    ${nomePaciente}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Laboratorio - Gaveta Recusa Pedido Vazio
    [Documentation]    Envia o pedido sem nada preenchido e vê a mensagem do paciente.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    UI Laboratorio - Abrir Aba    pedidos
    Clicar Botao    Pedido de exames
    Preencher Campo Da Gaveta    profissionalMatricula    ${EMPTY}
    Enviar Gaveta
    ${erro}=    Ler Erro Do Campo Da Gaveta    pacienteId
    Should Contain    ${erro}    Selecione o paciente
    ${erroExame}=    Ler Erro Do Campo Da Gaveta    buscaExame
    Should Contain    ${erroExame}    Marque pelo menos um exame
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Laboratorio - Coletar Pela Lista
    [Documentation]    Registra a coleta de um pedido pela aba Para coletar.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${pedidoId}    ${itemId}    ${matricula}    ${unidadeId}    ${pacienteId}=    Seed A Pedido Exame
    ${nomePaciente}    ${exameNome}=    UI Laboratorio - Ler Pedido    ${pedidoId}
    UI Laboratorio - Abrir Aba    coletar
    Clicar Botao    Coletar exames de ${nomePaciente}
    Esperar Texto Aparecer    Jejum de 4 horas
    Escolher Opcao Da Gaveta    unidadeColetaId    ${unidadeId}
    Preencher Campo Da Gaveta    profissionalMatricula    ${matricula}
    Enviar Gaveta
    ${aviso}=    Ler Aviso De Status
    Should Contain    ${aviso}    Coleta registrada
    UI Laboratorio - Status Do Item Deve Ser    ${pedidoId}    COLETADO
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Laboratorio - Resultado E Liberacao
    [Documentation]    Registra o resultado na aba Em análise e libera na aba Para liberar.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${pedidoId}    ${itemId}    ${matricula}    ${amostraId}=    Seed A Pedido Exame Coletado
    ${nomePaciente}    ${exameNome}=    UI Laboratorio - Ler Pedido    ${pedidoId}
    UI Laboratorio - Abrir Aba    analise
    Clicar Botao    Registrar resultado de ${exameNome} de ${nomePaciente}
    Esperar Texto Aparecer    12 a 16 g/dL
    Preencher Campo Da Gaveta    valorNumerico    17.2
    Preencher Campo Da Gaveta    matriculaResultado    ${matricula}
    Enviar Gaveta
    ${aviso}=    Ler Aviso De Status
    Should Contain    ${aviso}    Resultado registrado
    UI Laboratorio - Status Do Item Deve Ser    ${pedidoId}    RESULTADO_REGISTRADO
    UI Laboratorio - Abrir Aba    liberar
    Clicar Botao    Revisar resultado de ${exameNome} de ${nomePaciente}
    Esperar Texto Aparecer    Acima da referência
    Preencher Campo Da Gaveta    matriculaLiberacao    ${matricula}
    Enviar Gaveta
    ${liberado}=    Ler Aviso De Status
    Should Contain    ${liberado}    Resultado liberado
    UI Laboratorio - Status Do Item Deve Ser    ${pedidoId}    LIBERADO
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Laboratorio - Rejeitar Amostra
    [Documentation]    Rejeita a amostra na aba Em análise: o exame volta a aguardar coleta.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${pedidoId}    ${itemId}    ${matricula}    ${amostraId}=    Seed A Pedido Exame Coletado
    ${nomePaciente}    ${exameNome}=    UI Laboratorio - Ler Pedido    ${pedidoId}
    UI Laboratorio - Abrir Aba    analise
    Clicar Botao    Rejeitar amostra de ${exameNome} de ${nomePaciente}
    Esperar Texto Aparecer    Os exames dela
    Escolher Opcao Da Gaveta    motivoRejeicao    HEMOLISADA
    Preencher Campo Da Gaveta    matriculaRejeicao    ${matricula}
    Enviar Gaveta
    ${aviso}=    Ler Aviso De Status
    Should Contain    ${aviso}    Amostra rejeitada
    UI Laboratorio - Status Do Item Deve Ser    ${pedidoId}    SOLICITADO
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Laboratorio - Ver Detalhe Do Pedido
    [Documentation]    Abre o detalhe do pedido na aba Pedidos e vê a indicação clínica e o histórico.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${pedidoId}    ${itemId}    ${matricula}    ${unidadeId}    ${pacienteId}=    Seed A Pedido Exame
    ${nomePaciente}    ${exameNome}=    UI Laboratorio - Ler Pedido    ${pedidoId}
    UI Laboratorio - Abrir Aba    pedidos
    Clicar Botao    Ver pedido de ${nomePaciente}
    Esperar Texto Aparecer    Astenia e palidez.
    Esperar Texto Aparecer    Pedido feito
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Laboratorio - Cadastrar Exame Pela Gaveta
    [Documentation]    Cadastra um exame no catálogo e vê o aviso e a linha.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${sufixo}=    Generate Random String    6    [LETTERS][NUMBERS]
    ${nome}=    Set Variable    Robot UI Exame ${sufixo}
    UI Laboratorio - Abrir Aba    catalogo
    Clicar Botao    + Exame
    Preencher Campo Da Gaveta    nome    ${nome}
    Escolher Opcao Da Gaveta    material    URINA
    Escolher Opcao Da Gaveta    tipoResultado    TEXTO
    Preencher Campo Da Gaveta    referenciaTexto    Negativo
    Enviar Gaveta
    ${aviso}=    Ler Aviso De Status
    Should Contain    ${aviso}    Exame cadastrado
    Esperar Texto Aparecer    ${nome}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Laboratorio - Link Do Atendimento Abre O Pedido Preenchido
    [Documentation]    O link do "Pedir exames" abre o pedido com o paciente e a unidade escolhidos.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${pacienteId}    ${cpf}    ${cartaoSus}    ${nomePaciente}=    Seed A Paciente
    ${unidadeId}=    Seed A Unidade De Saude Uuid
    UI Laboratorio - Abrir Pedido Pelo Link    ${pacienteId}    ${unidadeId}
    ${titulo}=    Ler Titulo Da Gaveta
    Should Be Equal    ${titulo}    Pedido de exames
    Wait Until Keyword Succeeds    10s    500ms    UI Laboratorio - Campo Deve Valer    pacienteId    ${pacienteId}
    ${unidade}=    UI Laboratorio - Ler Valor Do Campo    unidadeSolicitanteId
    Should Be Equal    ${unidade}    ${unidadeId}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Laboratorio - Campo Deve Valer
    [Documentation]    Confere o valor de um campo da gaveta.
    [Arguments]    ${campo}    ${esperado}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${valor}=    UI Laboratorio - Ler Valor Do Campo    ${campo}
    Should Be Equal    ${valor}    ${esperado}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Laboratorio - Liberar Pela API
    [Documentation]    Registra o resultado (17, acima de 12-16) e libera o primeiro exame do pedido pela API.
    [Arguments]    ${itemId}    ${matricula}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${payload}=    Create Dictionary    profissionalMatricula=${matricula}    valorNumerico=${17}
    ${registrado}=    POST Acao Item Exame    ${itemId}    resultado    ${payload}
    Should Be Equal As Integers    ${registrado.status_code}    200
    ${lib}=    Create Dictionary    profissionalMatricula=${matricula}
    ${liberado}=    POST Acao Item Exame    ${itemId}    liberacao    ${lib}
    Should Be Equal As Integers    ${liberado.status_code}    200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Laboratorio - Seed Exame Liberado No Atendimento
    [Documentation]    Cria um atendimento, pede um exame nele, coleta e libera pela API.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${atendimentoId}    ${pacienteId}    ${matricula}    ${unidadeId}=    Seed Um Atendimento
    ${exameId}    ${exameNome}=    Seed A Exame Laboratorial
    ${exames}=    Create List    ${exameId}
    ${payload}=    Create Dictionary    pacienteId=${pacienteId}    atendimentoId=${atendimentoId}    unidadeSolicitanteId=${unidadeId}    profissionalMatricula=${matricula}    exameIds=${exames}    indicacaoClinica=Astenia e palidez.    prioridade=ROTINA
    ${criado}=    POST Create Pedido Exame    ${payload}
    Should Be Equal As Integers    ${criado.status_code}    201
    ${lista}=    GET List Pedido Exame Do Paciente    ${pacienteId}
    ${pedidoId}=    Set Variable    ${lista.json()}[0][uuid]
    ${itemId}=    Set Variable    ${lista.json()}[0][itens][0][uuid]
    ${coleta}=    Create Dictionary    profissionalMatricula=${matricula}    unidadeColetaId=${unidadeId}
    ${coletado}=    POST Coletar Pedido Exame    ${pedidoId}    ${coleta}
    Should Be Equal As Integers    ${coletado.status_code}    200
    UI Laboratorio - Liberar Pela API    ${itemId}    ${matricula}
    RETURN    ${pacienteId}    ${exameNome}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Laboratorio - Recoleta Na Coleta
    [Documentation]    Rejeita a amostra pela API e vê o exame como recoleta na aba Para coletar (ADR-0095).
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${pedidoId}    ${itemId}    ${matricula}    ${amostraId}=    Seed A Pedido Exame Coletado
    ${nomePaciente}    ${exameNome}=    UI Laboratorio - Ler Pedido    ${pedidoId}
    ${payload}=    Create Dictionary    profissionalMatricula=${matricula}    motivo=HEMOLISADA
    ${rejeitada}=    POST Rejeitar Amostra Exame    ${amostraId}    ${payload}
    Should Be Equal As Integers    ${rejeitada.status_code}    200
    UI Laboratorio - Abrir Aba    coletar
    Clicar Botao    Coletar exames de ${nomePaciente}
    Esperar Texto Aparecer    a amostra anterior foi rejeitada
    Wait For Elements State    css=app-drawer label >> text=Recoleta · Hemolisada    visible    10s
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Laboratorio - Laudo Imprimivel
    [Documentation]    Abre o laudo de um pedido liberado e vê o resultado, a interpretação e quem liberou (ADR-0095).
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${pedidoId}    ${itemId}    ${matricula}    ${amostraId}=    Seed A Pedido Exame Coletado
    ${nomePaciente}    ${exameNome}=    UI Laboratorio - Ler Pedido    ${pedidoId}
    UI Laboratorio - Liberar Pela API    ${itemId}    ${matricula}
    Ir Para Rota Do Frontend    /laudo/exame/${pedidoId}
    Esperar Texto Aparecer    Laudo de exames laboratoriais
    Esperar Texto Aparecer    ${exameNome}
    Esperar Texto Aparecer    Acima da referência
    Esperar Texto Aparecer    Liberado por
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Laboratorio - Exames No Prontuario E No Atendimento
    [Documentation]    Vê o exame liberado no prontuário e no atendimento em que foi pedido (ADR-0095).
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${pacienteId}    ${exameNome}=    UI Laboratorio - Seed Exame Liberado No Atendimento
    Ir Para Rota Do Frontend    /assistencia/atendimentos?aba=pront&pacienteId=${pacienteId}
    Esperar Texto Aparecer    Exames laboratoriais
    Wait For Elements State    css=.pront-exames >> text=${exameNome}    visible    10s
    Wait For Elements State    css=.pront-exames >> text=Acima da referência    visible    10s
    Clicar Botao    Abrir
    Wait For Elements State    css=app-drawer app-exames-paciente >> text=${exameNome}    visible    10s
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
