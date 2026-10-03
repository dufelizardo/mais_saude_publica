*** Settings ***
Library     String
Resource    ../../../../pom/ui/rh/escalas/escalas_ui_pom.resource
Resource    ../../../escalas/escala/escala_flow.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
    Unidade, profissional lotado e turnos de apoio são criados pela API, na semana que vem; a ação sob teste é feita pela tela.
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
UI Escalas - Semana Da API
    [Documentation]    Busca a semana da unidade pela API.
    [Arguments]    ${unidadeId}    ${semana}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${params}=    Create Dictionary    unidadeId=${unidadeId}    semana=${semana}
    ${response}=    GET Escala Semana    ${params}
    Should Be Equal As Integers    ${response.status_code}    200
    RETURN    ${response.json()}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Escalas - Ver Profissional Na Grade
    [Documentation]    Abre a semana, vê o profissional lotado e navega para a semana seguinte.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${seg}=    Proxima Segunda
    ${unidadeId}=    Seed A Unidade De Saude Uuid
    ${matricula}=    Seed Um Profissional Lotado    ${unidadeId}
    ${semana}=    UI Escalas - Semana Da API    ${unidadeId}    ${seg}
    ${nome}=    Set Variable    ${semana}[linhas][0][nome]
    UI Escalas - Abrir Semana    ${unidadeId}    ${seg}
    Esperar Texto Aparecer    ${nome}
    ${antes}=    UI Escalas - Ler Titulo Da Grade
    Clicar Botao    Próxima semana
    Wait Until Keyword Succeeds    10s    500ms    UI Escalas - Titulo Deve Mudar    ${antes}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Escalas - Titulo Deve Mudar
    [Documentation]    Confere que o título da grade mudou.
    [Arguments]    ${antes}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${agora}=    UI Escalas - Ler Titulo Da Grade
    Should Not Be Equal    ${agora}    ${antes}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Escalas - Criar Turno Na Folga
    [Documentation]    Clica na folga de segunda, cria um turno de manhã e confere pela API.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${seg}=    Proxima Segunda
    ${unidadeId}=    Seed A Unidade De Saude Uuid
    ${matricula}=    Seed Um Profissional Lotado    ${unidadeId}
    ${semana}=    UI Escalas - Semana Da API    ${unidadeId}    ${seg}
    ${nome}=    Set Variable    ${semana}[linhas][0][nome]
    ${dia}=    Evaluate    f"{$seg[8:10]}/{$seg[5:7]}"
    UI Escalas - Abrir Semana    ${unidadeId}    ${seg}
    UI Escalas - Clicar Folga    ${nome}    ${dia}
    ${titulo}=    Ler Titulo Da Gaveta
    Should Be Equal    ${titulo}    Novo turno
    Preencher Campo Da Gaveta    descricao    Acolhimento
    Enviar Gaveta
    ${aviso}=    Ler Aviso De Status
    Should Contain    ${aviso}    Turno criado
    Esperar Texto Aparecer    Acolhimento
    ${depois}=    UI Escalas - Semana Da API    ${unidadeId}    ${seg}
    Should Be Equal As Integers    ${depois}[turnos]    1
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Escalas - Gaveta Recusa Vaga Sem Funcao
    [Documentation]    Abre um novo turno como vaga, sem função, e vê a mensagem.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${seg}=    Proxima Segunda
    ${unidadeId}=    Seed A Unidade De Saude Uuid
    UI Escalas - Abrir Semana    ${unidadeId}    ${seg}
    Clicar Botao    Novo turno
    Enviar Gaveta
    ${erro}=    Ler Erro Do Campo Da Gaveta    funcao
    Should Contain    ${erro}    função que falta
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Escalas - Designar Vaga
    [Documentation]    Preenche uma vaga aberta pela gaveta de designação e confere pela API.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${seg}=    Proxima Segunda
    ${unidadeId}=    Seed A Unidade De Saude Uuid
    ${matricula}=    Seed Um Profissional Lotado    ${unidadeId}
    ${payload}=    Payload De Turno    ${unidadeId}    ${EMPTY}    MEDICO    MANHA    ${seg}    07:00    13:00
    ${vaga}=    POST Create Turno    ${payload}
    Should Be Equal As Integers    ${vaga.status_code}    201
    UI Escalas - Abrir Semana    ${unidadeId}    ${seg}
    UI Escalas - Clicar Primeira Vaga
    ${titulo}=    Ler Titulo Da Gaveta
    Should Be Equal    ${titulo}    Designar profissional
    Escolher Opcao Da Gaveta    profissionalMatricula    ${matricula}
    Enviar Gaveta
    ${aviso}=    Ler Aviso De Status
    Should Contain    ${aviso}    Vaga preenchida
    ${depois}=    UI Escalas - Semana Da API    ${unidadeId}    ${seg}
    Should Be Equal As Integers    ${depois}[vagasAbertas]    0
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Escalas - Copiar Semana
    [Documentation]    Abre a semana seguinte, copia a anterior pela gaveta e confere pela API.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${seg}=    Proxima Segunda
    ${unidadeId}=    Seed A Unidade De Saude Uuid
    ${matricula}=    Seed Um Profissional Lotado    ${unidadeId}
    ${payload}=    Payload De Turno    ${unidadeId}    ${matricula}    ${EMPTY}    MANHA    ${seg}    07:00    13:00
    ${turno}=    POST Create Turno    ${payload}
    Should Be Equal As Integers    ${turno.status_code}    201
    ${destino}=    Add Time To Date    ${seg}    7 days    result_format=%Y-%m-%d    date_format=%Y-%m-%d
    UI Escalas - Abrir Semana    ${unidadeId}    ${destino}
    Clicar Botao    Copiar semana
    Enviar Gaveta
    Esperar Texto Aparecer    1 turno(s) copiado(s)
    ${depois}=    UI Escalas - Semana Da API    ${unidadeId}    ${destino}
    Should Be Equal As Integers    ${depois}[turnos]    1
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Escalas - Remover Turno
    [Documentation]    Abre um turno, remove com confirmação e confere pela API.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${seg}=    Proxima Segunda
    ${unidadeId}=    Seed A Unidade De Saude Uuid
    ${matricula}=    Seed Um Profissional Lotado    ${unidadeId}
    ${payload}=    Payload De Turno    ${unidadeId}    ${matricula}    ${EMPTY}    TARDE    ${seg}    13:00    17:00
    ${turno}=    POST Create Turno    ${payload}
    Should Be Equal As Integers    ${turno.status_code}    201
    UI Escalas - Abrir Semana    ${unidadeId}    ${seg}
    UI Escalas - Clicar Primeiro Turno Editavel
    Clicar Botao    Remover turno
    Clicar Botao    Confirmar remoção
    ${aviso}=    Ler Aviso De Status
    Should Contain    ${aviso}    Turno removido
    ${depois}=    UI Escalas - Semana Da API    ${unidadeId}    ${seg}
    Should Be Equal As Integers    ${depois}[turnos]    0
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Escalas - Aba De Plantoes
    [Documentation]    Abre a aba Plantões numa unidade sem plantão e vê o estado vazio.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${seg}=    Proxima Segunda
    ${unidadeId}=    Seed A Unidade De Saude Uuid
    UI Escalas - Abrir Semana    ${unidadeId}    ${seg}
    Clicar Aba    Plantões
    Esperar Texto Aparecer    Sem plantões nesta semana
    ${aba}=    Ler Aba Selecionada
    Should Be Equal    ${aba}    Plantões
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
