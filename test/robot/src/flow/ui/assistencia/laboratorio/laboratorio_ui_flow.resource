*** Settings ***
Library     String
Resource    ../../../../pom/ui/assistencia/laboratorio/laboratorio_ui_pom.resource
Resource    ../../../laboratorio/pedido_exame/pedido_exame_flow.resource
Resource    ../../../atendimento/create_atendimento/create_atendimento_flow.resource
Resource    ../../../laboratorio/faixa_referencia/faixa_referencia_flow.resource
Resource    ../../../laboratorio/ciencia_entrega/ciencia_entrega_flow.resource
Resource    ../../../laboratorio/exame_imagem/exame_imagem_flow.resource
Resource    ../../../laboratorio/laudo_insumos/laudo_insumos_flow.resource
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

UI Laboratorio - Incluir Faixa Por Sexo E Idade
    [Documentation]    Inclui a faixa feminina adulta pela gaveta do exame do catálogo e vê o rótulo (ADR-0180).
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${exameId}    ${nome}=    Seed A Exame Laboratorial
    UI Laboratorio - Abrir Aba    catalogo
    Clicar Botao    Editar ${nome}
    Clicar Botao    + Faixa
    Escolher Opcao Da Gaveta    faixaSexo    FEMININO
    Preencher Campo Da Gaveta    faixaIdadeMinima    12
    Preencher Campo Da Gaveta    faixaIdadeMaxima    60
    Preencher Campo Da Gaveta    faixaRefMin    11.5
    Preencher Campo Da Gaveta    faixaRefMax    15
    Preencher Campo Da Gaveta    faixaCritMin    7
    Clicar Botao    Incluir faixa
    ${aviso}=    Ler Aviso De Status
    Should Contain    ${aviso}    Faixa incluída
    Wait For Elements State    css=app-drawer .faixas__item >> text=Feminino, 12 a 59 anos    visible    10s
    ${exame}=    GET Exame Laboratorial By Id    ${exameId}
    Should Be Equal    ${exame.json()}[faixas][0][rotulo]    Feminino, 12 a 59 anos
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Laboratorio - Evolucao Do Exame No Prontuario
    [Documentation]    Dois resultados liberados do mesmo exame: o prontuário abre a evolução com o gráfico e a tabela (ADR-0180).
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${exameId}    ${nome}    ${faixaId}=    Seed A Exame Com Faixa Feminina
    ${pacienteId}    ${cpf}    ${cartaoSus}    ${pacienteNome}=    Seed A Paciente
    ${unidadeId}=    Seed A Unidade De Saude Uuid
    ${matricula}=    Seed A Profissional Matricula
    Registrar Resultado Do Exame    ${pacienteId}    ${exameId}    ${unidadeId}    ${matricula}    ${10.2}    diasAtras=${30}
    Registrar Resultado Do Exame    ${pacienteId}    ${exameId}    ${unidadeId}    ${matricula}    ${12.4}    diasAtras=${2}
    Ir Para Rota Do Frontend    /assistencia/atendimentos?aba=pront&pacienteId=${pacienteId}
    Wait For Elements State    css=.pront-exames >> text=Feminino, 12 a 59 anos >> nth=0    visible    10s
    Clicar Botao    Evolução de ${nome}
    ${titulo}=    Ler Titulo Da Gaveta
    Should Be Equal    ${titulo}    Evolução: ${nome}
    Wait For Elements State    css=app-drawer svg.evo__svg    visible    10s
    ${pontos}=    Get Element Count    css=app-drawer svg.evo__svg circle
    Should Be Equal As Integers    ${pontos}    2
    ${linhas}=    Get Element Count    css=app-drawer table.evo__tabela tbody tr
    Should Be Equal As Integers    ${linhas}    2
    Wait For Elements State    css=app-drawer table.evo__tabela >> text=Abaixo da referência    visible    10s
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Laboratorio - Ciencia Pela Aba Para Ciencia
    [Documentation]    Quem pediu vê o resultado liberado na aba Para ciência e registra a ciência (ADR-0181).
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${pedidoId}    ${itemId}    ${matricula}    ${unidadeId}=    Seed A Resultado Liberado
    ${nomePaciente}    ${exameNome}=    UI Laboratorio - Ler Pedido    ${pedidoId}
    UI Laboratorio - Abrir Aba    ciencia
    Fill Text    id=f-matriculaCiencia    ${matricula}
    Clicar Botao    Ver resultados
    Clicar Botao    Registrar ciência de ${exameNome} de ${nomePaciente}
    ${aviso}=    Ler Aviso De Status
    Should Contain    ${aviso}    Ciência registrada
    ${detalhe}=    GET Get Pedido Exame By Id    ${pedidoId}
    Should Be Equal    ${detalhe.json()}[itens][0][resultado][cienciaPorMatricula]    ${matricula}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Laboratorio - Comunicar Critico Pela Aba
    [Documentation]    O laboratório registra a comunicação do crítico pela aba Críticos a comunicar (ADR-0181).
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${pedidoId}    ${itemId}    ${matricula}    ${unidadeId}=    Seed A Resultado Liberado    ${5}
    ${nomePaciente}    ${exameNome}=    UI Laboratorio - Ler Pedido    ${pedidoId}
    UI Laboratorio - Abrir Aba    criticos
    Clicar Botao    Comunicar crítico de ${exameNome} de ${nomePaciente}
    ${titulo}=    Ler Titulo Da Gaveta
    Should Be Equal    ${titulo}    Comunicar valor crítico
    Preencher Campo Da Gaveta    receptorNome    Dra. Plantonista
    Escolher Opcao Da Gaveta    meio    TELEFONE
    Preencher Campo Da Gaveta    matriculaComunicacao    ${matricula}
    Enviar Gaveta
    ${aviso}=    Ler Aviso De Status
    Should Contain    ${aviso}    Comunicação registrada
    ${detalhe}=    GET Get Pedido Exame By Id    ${pedidoId}
    Should Be Equal    ${detalhe.json()}[comunicacoesCritico][0][receptorNome]    Dra. Plantonista
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Laboratorio - Entrega Pelo Detalhe Do Pedido
    [Documentation]    Registra a entrega a um parente pelo detalhe do pedido concluído (ADR-0181).
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${pedidoId}    ${itemId}    ${matricula}    ${unidadeId}=    Seed A Resultado Liberado
    ${nomePaciente}    ${exameNome}=    UI Laboratorio - Ler Pedido    ${pedidoId}
    UI Laboratorio - Abrir Aba    pedidos
    Clicar Botao    Ver pedido de ${nomePaciente}
    Clicar Botao    Registrar entrega
    ${titulo}=    Ler Titulo Da Gaveta
    Should Be Equal    ${titulo}    Registrar entrega
    Click    css=app-drawer label >> text=Outra pessoa
    Preencher Campo Da Gaveta    terceiroNome    Maria da Silva
    Preencher Campo Da Gaveta    terceiroDocumento    RG 12.345.678-9
    Preencher Campo Da Gaveta    terceiroParentesco    Mãe
    Preencher Campo Da Gaveta    matriculaEntrega    ${matricula}
    Enviar Gaveta
    ${aviso}=    Ler Aviso De Status
    Should Contain    ${aviso}    Entrega registrada
    ${detalhe}=    GET Get Pedido Exame By Id    ${pedidoId}
    Should Be Equal    ${detalhe.json()}[entregas][0][terceiroNome]    Maria da Silva
    Should Be Equal    ${detalhe.json()}[entregas][0][unidadeId]    ${unidadeId}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Laboratorio - Cadastrar Exame De Imagem Pela Gaveta
    [Documentation]    Cadastra um exame de imagem no catálogo com a modalidade (ADR-0182).
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${sufixo}=    Generate Random String    6    [LETTERS][NUMBERS]
    ${nome}=    Set Variable    Robot UI USG ${sufixo}
    UI Laboratorio - Abrir Aba    catalogo
    Clicar Botao    + Exame
    Preencher Campo Da Gaveta    nome    ${nome}
    Click    css=app-drawer label >> text=Imagem
    Escolher Opcao Da Gaveta    modalidade    ULTRASSONOGRAFIA
    Preencher Campo Da Gaveta    preparo    Jejum de 8 horas.
    Enviar Gaveta
    ${aviso}=    Ler Aviso De Status
    Should Contain    ${aviso}    Exame cadastrado
    Wait For Elements State    css=table >> text=Imagem · Ultrassonografia >> nth=0    visible    10s
    ${lista}=    GET List Exame Laboratorial
    ${exame}=    Evaluate    [e for e in $lista.json() if e['nome'] == $nome][0]
    Should Be Equal    ${exame}[categoria]    IMAGEM
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Laboratorio - Realizar E Laudar Pela Tela
    [Documentation]    Realiza o exame na aba Para realizar e lauda na aba Para laudar (ADR-0182).
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${pedidoId}    ${itemId}    ${matricula}    ${servico}=    Seed A Pedido De Imagem
    ${nomePaciente}    ${exameNome}=    UI Laboratorio - Ler Pedido    ${pedidoId}
    UI Laboratorio - Abrir Aba    realizar
    Clicar Botao    Realizar ${exameNome} de ${nomePaciente}
    Esperar Texto Aparecer    Retirar objetos metálicos.
    Preencher Campo Da Gaveta    equipamento    Aparelho da sala 1
    Preencher Campo Da Gaveta    matriculaRealizacao    ${matricula}
    Enviar Gaveta
    ${aviso}=    Ler Aviso De Status
    Should Contain    ${aviso}    Exame realizado
    UI Laboratorio - Status Do Item Deve Ser    ${pedidoId}    REALIZADO
    UI Laboratorio - Abrir Aba    laudar
    Clicar Botao    Laudar ${exameNome} de ${nomePaciente}
    Preencher Campo Da Gaveta    achados    Opacidade em base direita.
    Preencher Campo Da Gaveta    conclusao    Pneumonia em base direita.
    Click    css=app-drawer label >> text=Com alterações
    Preencher Campo Da Gaveta    matriculaLaudo    ${matricula}
    Enviar Gaveta
    ${laudado}=    Ler Aviso De Status
    Should Contain    ${laudado}    Laudo liberado
    ${detalhe}=    GET Get Pedido Exame By Id    ${pedidoId}
    Should Be Equal    ${detalhe.json()}[itens][0][status]    LIBERADO
    Should Be Equal    ${detalhe.json()}[itens][0][resultado][interpretacao]    ALTERADO
    Should Be Equal    ${detalhe.json()}[realizacoes][0][equipamento]    Aparelho da sala 1
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Laboratorio - Baixar Laudo Em PDF
    [Documentation]    Baixa o laudo em PDF pelo detalhe do pedido e vê a emissão vigente com o código (ADR-0183).
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${pedidoId}    ${itemId}    ${matricula}=    Seed A Pedido Liberado
    ${nomePaciente}    ${exameNome}=    UI Laboratorio - Ler Pedido    ${pedidoId}
    UI Laboratorio - Abrir Aba    pedidos
    Clicar Botao    Ver pedido de ${nomePaciente}
    Clicar Botao    Baixar laudo (PDF)
    ${aviso}=    Ler Aviso De Status
    Should Contain    ${aviso}    Laudo em PDF emitido
    Wait For Elements State    css=app-drawer >> text=Laudos emitidos    visible    10s
    Wait For Elements State    css=app-drawer .badge >> text=Vigente    visible    10s
    ${emissoes}=    GET Emissoes Laudo    ${pedidoId}
    Should Contain    ${aviso}    ${emissoes.json()}[0][codigo]
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Laboratorio - Verificar Laudo Na Pagina Publica
    [Documentation]    A página pública confere o código do laudo, sem login (ADR-0183).
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${pedidoId}    ${itemId}    ${matricula}=    Seed A Pedido Liberado
    ${pdf}=    GET Laudo Pdf    ${pedidoId}
    ${codigo}=    Set Variable    ${pdf.headers}[X-Codigo-Verificacao]
    Ir Para Rota Do Frontend    /verificar-laudo?codigo=${codigo}
    Esperar Texto Aparecer    Laudo autêntico e vigente
    ${hash}=    Evaluate    hashlib.sha256($pdf.content).hexdigest()    modules=hashlib
    Esperar Texto Aparecer    ${hash}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Laboratorio - Insumo Pela Gaveta Do Exame
    [Documentation]    Inclui um insumo do estoque na gaveta do exame do catálogo (ADR-0183).
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${material}    ${materialNome}=    Seed Material Estoque    ${False}
    ${exameId}    ${nome}=    Seed A Exame Laboratorial
    UI Laboratorio - Abrir Aba    catalogo
    Clicar Botao    Editar ${nome}
    Wait For Elements State    css=#f-insumoMaterial >> option[value="${material}"]    attached    10s
    Escolher Opcao Da Gaveta    insumoMaterial    ${material}
    Preencher Campo Da Gaveta    insumoQuantidade    2
    Clicar Botao    Incluir insumo
    ${aviso}=    Ler Aviso De Status
    Should Contain    ${aviso}    Insumo incluído
    Wait For Elements State    css=app-drawer .faixas__item >> text=${materialNome}    visible    10s
    ${exame}=    GET Exame Laboratorial By Id    ${exameId}
    Should Be Equal As Integers    ${exame.json()}[insumos][0][quantidade]    2
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
