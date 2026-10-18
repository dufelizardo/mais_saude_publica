*** Settings ***
Library     String
Resource    ../../../../pom/ui/assistencia/programas/programas_ui_pom.resource
Resource    ../../../programas/programa/programa_flow.resource
Resource    ../../../programas/acompanhamento/acompanhamento_flow.resource
Resource    ../../../../pom/ui/assistencia/pacientes/pacientes_ui_pom.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
    Os programas e as unidades de apoio são criados pela API; a ação sob teste é feita pela tela.
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
UI Programas - Detalhe Deve Ser
    [Documentation]    Confere o nome do programa no detalhe.
    [Arguments]    ${nome}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${titulo}=    UI Programas - Ler Titulo Do Detalhe
    Should Be Equal    ${titulo}    ${nome}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Programas - Ver Cartao E Detalhe
    [Documentation]    A grade mostra os programas da carga; clicar no cartão abre o detalhe com o público-alvo.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    UI Programas - Abrir
    UI Programas - Clicar No Cartao    Saúde do Idoso
    Wait Until Keyword Succeeds    10s    500ms    UI Programas - Detalhe Deve Ser    Saúde do Idoso
    Esperar Texto Aparecer    60 anos ou mais
    Esperar Texto Aparecer    Unidades e equipes participantes
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Programas - Cadastrar Programa
    [Documentation]    Cadastra um programa pela gaveta e vê o aviso e o detalhe.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${sufixo}=    Generate Random String    6    [UPPER][NUMBERS]
    ${nome}=    Set Variable    UI Programa ${sufixo}
    ${categoria}=    Id Da Categoria    Ciclos de vida
    UI Programas - Abrir
    Clicar Botao    Novo programa
    Preencher Campo Da Gaveta    codigo    UI_${sufixo}
    Preencher Campo Da Gaveta    nome    ${nome}
    Escolher Opcao Da Gaveta    categoriaId    ${categoria}
    Preencher Campo Da Gaveta    idadeMinima    10
    Preencher Campo Da Gaveta    idadeMaxima    19
    Enviar Gaveta
    ${aviso}=    Ler Aviso De Status
    Should Contain    ${aviso}    Programa cadastrado
    Wait Until Keyword Succeeds    10s    500ms    UI Programas - Detalhe Deve Ser    ${nome}
    Esperar Texto Aparecer    De 10 a 19 anos
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Programas - Gaveta Recusa Dados Invalidos
    [Documentation]    Código com espaço, nome vazio e condição inválida mostram as mensagens dos campos.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    UI Programas - Abrir
    Clicar Botao    Novo programa
    Preencher Campo Da Gaveta    codigo    COM ESPACO
    Preencher Campo Da Gaveta    condicoes    hipertensao
    Enviar Gaveta
    ${erroCodigo}=    Ler Erro Do Campo Da Gaveta    codigo
    Should Contain    ${erroCodigo}    sublinhado
    ${erroNome}=    Ler Erro Do Campo Da Gaveta    nome
    Should Contain    ${erroNome}    nome
    ${erroCondicao}=    Ler Erro Do Campo Da Gaveta    condicoes
    Should Contain    ${erroCondicao}    CID-10
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Programas - Incluir Unidade
    [Documentation]    Inclui uma unidade participante pela gaveta e a vê no detalhe.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${programa}    ${nome}=    Seed Programa
    ${unidade}    ${nomeUnidade}=    Seed Unidade Para Programa
    UI Programas - Abrir Programa    ${programa}
    Wait Until Keyword Succeeds    10s    500ms    UI Programas - Detalhe Deve Ser    ${nome}
    Clicar Botao    Incluir unidade
    Escolher Opcao Da Gaveta    unidadeId    ${unidade}
    Enviar Gaveta
    ${aviso}=    Ler Aviso De Status
    Should Contain    ${aviso}    Unidade incluída
    Wait For Elements State    css=[aria-label="Participações vigentes"] >> text=${nomeUnidade}    visible
    ${detalhe}=    GET Programa    ${programa}
    Length Should Be    ${detalhe.json()}[participacoes]    1
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Programas - Encerrar Participacao
    [Documentation]    Encerra a participação pela gaveta: sem motivo, a mensagem; com motivo, vai para o histórico.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${programa}    ${nome}=    Seed Programa
    ${unidade}    ${nomeUnidade}=    Seed Unidade Para Programa
    ${payload}=    Create Dictionary    unidadeId=${unidade}
    POST Participacao Programa    ${programa}    ${payload}
    UI Programas - Abrir Programa    ${programa}
    Wait Until Keyword Succeeds    10s    500ms    UI Programas - Detalhe Deve Ser    ${nome}
    Clicar Botao    Encerrar ${nomeUnidade}
    Enviar Gaveta
    ${erro}=    Ler Erro Do Campo Da Gaveta    motivoFim
    Should Contain    ${erro}    motivo
    Preencher Campo Da Gaveta    motivoFim    Programa passou a outra unidade
    Enviar Gaveta
    ${aviso}=    Ler Aviso De Status
    Should Contain    ${aviso}    Participação encerrada
    ${detalhe}=    GET Programa    ${programa}
    Length Should Be    ${detalhe.json()}[participacoes]    0
    Should Be Equal    ${detalhe.json()}[participacoesEncerradas][0][motivoFim]    Programa passou a outra unidade
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Programas - Consulta Publica Na Landing
    [Documentation]    A landing page mostra os programas ativos e onde são oferecidos, sem login.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${programa}    ${nome}=    Seed Programa
    ${unidade}    ${nomeUnidade}=    Seed Unidade Para Programa
    ${payload}=    Create Dictionary    unidadeId=${unidade}
    POST Participacao Programa    ${programa}    ${payload}
    UI Programas - Abrir Landing
    Esperar Texto Aparecer    Cuidado organizado para cada fase da vida
    Wait For Elements State    css=#programas article[aria-label="${nome}"]    visible
    ${texto}=    Get Text    css=#programas article[aria-label="${nome}"]
    Should Contain    ${texto}    ${nomeUnidade}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Programas - Inscrever Paciente
    [Documentation]    Inscreve um paciente pela gaveta (busca pelo nome) e o vê na lista de acompanhados.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${programa}    ${nome}    ${unidade}=    Seed Programa Com Unidade
    ${paciente}    ${cpf}    ${cns}    ${nomePaciente}=    Seed A Paciente
    UI Programas - Abrir Programa    ${programa}
    Wait Until Keyword Succeeds    10s    500ms    UI Programas - Detalhe Deve Ser    ${nome}
    Clicar Botao    Inscrever paciente
    Preencher Campo Da Gaveta    buscaPaciente    ${nomePaciente}
    Click    css=app-drawer [role="listbox"] >> role=option[name="${nomePaciente}"]
    Enviar Gaveta
    ${aviso}=    Ler Aviso De Status
    Should Contain    ${aviso}    Paciente inscrito
    Wait For Elements State    css=table[aria-label="Pacientes acompanhados"] >> text=${nomePaciente}    visible
    ${inscritos}=    GET Inscritos Do Programa    ${programa}
    Should Be Equal As Integers    ${inscritos.json()}[inscritos]    1
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Programas - Marcar Faltoso
    [Documentation]    Marca o acompanhado como faltoso pela lista, com motivo.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${acompanhamento}    ${programa}    ${paciente}    ${unidade}=    Seed Acompanhamento
    ${detalhe}=    GET Programa    ${programa}
    ${nome}=    Set Variable    ${detalhe.json()}[resumo][nome]
    ${inscritos}=    GET Inscritos Do Programa    ${programa}
    ${nomePaciente}=    Set Variable    ${inscritos.json()}[lista][0][pacienteNome]
    UI Programas - Abrir Programa    ${programa}
    Wait Until Keyword Succeeds    10s    500ms    UI Programas - Detalhe Deve Ser    ${nome}
    Clicar Botao    Marcar faltoso: ${nomePaciente}
    Enviar Gaveta
    ${erro}=    Ler Erro Do Campo Da Gaveta    motivoSituacao
    Should Contain    ${erro}    motivo
    Preencher Campo Da Gaveta    motivoSituacao    Não compareceu às três últimas consultas
    Enviar Gaveta
    ${aviso}=    Ler Aviso De Status
    Should Contain    ${aviso}    Marcado como faltoso
    ${depois}=    GET Inscritos Do Programa    ${programa}
    Should Be Equal As Integers    ${depois.json()}[faltosos]    1
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Programas - Aba Programas Do Paciente
    [Documentation]    A aba Programas de Pacientes mostra o programa em que o paciente é acompanhado.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${acompanhamento}    ${programa}    ${paciente}    ${unidade}=    Seed Acompanhamento
    ${detalhe}=    GET Programa    ${programa}
    ${nome}=    Set Variable    ${detalhe.json()}[resumo][nome]
    ${inscritos}=    GET Inscritos Do Programa    ${programa}
    ${nomePaciente}=    Set Variable    ${inscritos.json()}[lista][0][pacienteNome]
    UI Pacientes - Abrir Tela
    UI Pacientes - Buscar    ${nomePaciente}
    UI Pacientes - Abrir Paciente    ${nomePaciente}
    Clicar Aba    Programas
    Wait For Elements State    css=[aria-label="Programas vigentes do paciente"] >> text=${nome} >> nth=0    visible
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Programas - Faltoso Pela Aba Do Paciente
    [Documentation]    Na aba Programas de Pacientes, marca o programa como faltoso com motivo.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${acompanhamento}    ${programa}    ${paciente}    ${unidade}=    Seed Acompanhamento
    ${detalhe}=    GET Programa    ${programa}
    ${nome}=    Set Variable    ${detalhe.json()}[resumo][nome]
    ${inscritos}=    GET Inscritos Do Programa    ${programa}
    ${nomePaciente}=    Set Variable    ${inscritos.json()}[lista][0][pacienteNome]
    UI Pacientes - Abrir Tela
    UI Pacientes - Buscar    ${nomePaciente}
    UI Pacientes - Abrir Paciente    ${nomePaciente}
    Clicar Aba    Programas
    Clicar Botao    Marcar faltoso: ${nome}
    Enviar Gaveta
    ${erro}=    Ler Erro Do Campo Da Gaveta    motivoSituacaoPrograma
    Should Contain    ${erro}    motivo
    Preencher Campo Da Gaveta    motivoSituacaoPrograma    Não veio às últimas consultas
    Enviar Gaveta
    ${aviso}=    Ler Aviso De Status
    Should Contain    ${aviso}    Marcado como faltoso
    ${depois}=    GET Inscritos Do Programa    ${programa}
    Should Be Equal As Integers    ${depois.json()}[faltosos]    1
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
