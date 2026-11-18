*** Settings ***
Library     String
Resource    ../../../../pom/ui/administrativo/compras/compras_ui_pom.resource
Resource    ../../../compras/compras_flow.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
    Jornadas da tela Compras (ADR-0212); as bases vêm pela API.
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
