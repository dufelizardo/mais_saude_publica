*** Settings ***
Library     String
Resource    ../../../../pom/ui/administrativo/compras/compras_ui_pom.resource
Resource    ../../../compras/compras_flow.resource
Resource    ../../../compras/contratos_flow.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
    Jornadas da tela Compras (ADRs 0212 e 0213); as bases vêm pela API.
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
UI Compras - Pelo Menu
    [Documentation]    O item do menu abre a tela.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Ir Para Rota Do Frontend    /sala-de-situacao
    ${expandido}=    Get Element Count    css=a.nav-item[href="/administrativo/compras"]
    IF    ${expandido} == 0
        Click    css=button.nav-group__label >> text=Administrativo
    END
    Click    css=a.nav-item[href="/administrativo/compras"]
    Wait For Elements State    css=h1 >> text=Compras    visible
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Compras - Nova Solicitacao
    [Documentation]    Pela tela: monta a solicitação com um serviço, salva o rascunho e envia.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${unidade}=    Seed A Unidade De Saude Uuid
    UI Compras - Abrir Tela
    Clicar Botao    Nova solicitação
    Select Options By    id=f-cp-unidade    value    ${unidade}
    Fill Text    id=f-cp-justificativa    Manutenção preventiva do autoclave.
    Select Options By    id=f-cp-tipo-0    value    SERVICO
    Fill Text    id=f-cp-descricao-0    Manutenção preventiva do autoclave
    Fill Text    id=f-cp-medida-0    serviço
    Enviar Gaveta
    Wait For Elements State    css=#drawer-titulo >> text=/Solicitação \\d{4}\\/\\d{6}/    visible
    Wait For Elements State    css=app-drawer dl >> text=Rascunho    visible
    Click    css=app-drawer button >> text=Enviar para análise
    Wait For Elements State    css=app-drawer dl >> text=Enviada    visible
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Compras - Cotacao E Conclusao
    [Documentation]    Pela tela: três cotações e a pesquisa concluída com o preço de referência.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${id}    ${item}=    Seed Solicitacao Aprovada
    ${r}=    GET On Session    msp_session    ${BASE_URL}/compras/solicitacao/${id}
    UI Compras - Abrir Tela
    Click    css=button[aria-label="Abrir solicitação ${r.json()}[numero]"]
    FOR    ${preco}    IN    10    12    20
        Select Options By    id=f-cp-fonte    value    SITE
        Fill Text    id=f-cp-referencia    loja${preco}.exemplo
        Fill Text    id=f-cp-preco    ${preco}
        Click    css=app-drawer button >> text=Registrar cotação
        Wait For Elements State    css=[aria-label="Cotações do item 1"] >> text=loja${preco}.exemplo    visible
    END
    Click    css=app-drawer button >> text=Concluir pesquisa de preços
    Wait For Elements State    css=app-drawer dl >> text=Cotada    visible
    Wait For Elements State    css=[aria-label="Itens e cotações"] >> text=/preço de referência/    visible
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Compras - Fornecedor
    [Documentation]    Pela tela: cadastra o fornecedor, que nasce com as certidões pendentes, e registra uma certidão.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${cnpj}=    Gerar CNPJ
    ${sufixo}=    Generate Random String    6    [UPPER]
    UI Compras - Abrir Tela
    Click    role=tab[name="Fornecedores"]
    Clicar Botao    Cadastrar fornecedor
    Fill Text    id=f-fo-doc    ${cnpj}
    Fill Text    id=f-fo-razao    Robot UI Fornecedor ${sufixo}
    Enviar Gaveta
    Wait For Elements State    css=#drawer-titulo >> text=Robot UI Fornecedor ${sufixo}    visible
    Wait For Elements State    css=app-drawer dl >> text=Certidão pendente    visible
    ${validade}=    Get Current Date    increment=60 days    result_format=%Y-%m-%d
    Select Options By    id=f-ce-tipo    value    FEDERAL
    Fill Text    id=f-ce-validade    ${validade}
    Click    css=app-drawer button >> text=Registrar certidão
    Wait For Elements State    css=[aria-label="Certidões do fornecedor"] >> text=Federal e previdência    visible
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Compras - Contratacao E Homologacao
    [Documentation]    Pela tela: abre a contratação da solicitação cotada, adjudica e homologa a ata.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${s}=    Seed Solicitacao Cotada
    ${sol}=    GET On Session    msp_session    ${BASE_URL}/compras/solicitacao/${s}
    ${forn}=    Seed Fornecedor Regular
    ${f}=    GET On Session    msp_session    ${BASE_URL}/compras/fornecedor/${forn}
    ${gestor}=    Seed A Profissional Matricula
    ${fiscal}=    Seed A Profissional Matricula
    ${sufixo}=    Generate Random String    8    [UPPER][NUMBERS]
    UI Compras - Abrir Tela
    Click    role=tab[name="Contratações"]
    Clicar Botao    Nova contratação
    Fill Text    id=f-ct-processo    UI ${sufixo}
    Fill Text    id=f-ct-objeto    Insumos do trimestre
    Check Checkbox    css=input[aria-label="Incluir ${sol.json()}[numero]"]
    Enviar Gaveta
    Wait For Elements State    css=#drawer-titulo >> text=Contratação UI ${sufixo}    visible
    Select Options By    id=f-ct-forn-1    value    ${forn}
    Fill Text    id=f-ct-preco-1    11
    Click    css=button[aria-label="Registrar resultado do item 1"]
    Wait For Elements State    css=[aria-label="Itens da contratação"] >> text=Adjudicado    visible
    ${fim}=    Get Current Date    increment=200 days    result_format=%Y-%m-%d
    Fill Text    id=f-ho-numero-0    ARP UI ${sufixo}
    Fill Text    id=f-ho-fim-0    ${fim}
    Fill Text    id=f-ho-gestor-0    ${gestor}
    Fill Text    id=f-ho-fiscal-0    ${fiscal}
    Click    css=app-drawer button >> text=Homologar
    Wait For Elements State    css=app-drawer dl >> text=/Homologada/    visible
    Wait For Elements State    css=app-drawer >> text=ARP UI ${sufixo}    visible
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Compras - Aditivo De Contrato
    [Documentation]    Pela tela: o acréscimo no contrato aparece nos aditivos e na quantidade.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${ct}=    Seed Instrumento    CONTRATO
    ${r}=    GET Instrumento    ${ct}
    UI Compras - Abrir Tela
    Click    role=tab[name="Contratos"]
    Click    css=button[aria-label="Abrir ${r.json()}[numero]"]
    Select Options By    id=f-ad-tipo    value    ACRESCIMO
    Fill Text    id=f-ad-qtd    5
    Fill Text    id=f-ad-just    Aumento da demanda
    Click    css=app-drawer button >> text=Registrar aditivo
    Wait For Elements State    css=[aria-label="Aditivos"] >> text=/Acréscimo de quantidade/    visible
    Wait For Elements State    css=table[aria-label="Itens e saldos"] >> text=inicial 50    visible
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
