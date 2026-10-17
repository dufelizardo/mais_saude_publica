*** Settings ***
Library     String
Resource    ../../../../pom/ui/assistencia/programas/programas_ui_pom.resource
Resource    ../../../programas/programa/programa_flow.resource
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
