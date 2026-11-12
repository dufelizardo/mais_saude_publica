*** Settings ***
Library     String
Resource    ../../../pom/ui/qualidade/nao_conformidades_ui_pom.resource
Resource    ../../qualidade/nao_conformidade_flow.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
    Jornadas da tela Não conformidades (ADR-0204); as bases vêm pela API.
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
UI Nao Conformidades - Pelo Menu
    [Documentation]    O item do menu abre a tela.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Ir Para Rota Do Frontend    /sala-de-situacao
    Click    css=a.nav-item[href="/qualidade/nao-conformidades"]
    Wait For Elements State    css=h1 >> text=Não conformidades    visible
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Nao Conformidades - Registrar
    [Documentation]    Pela tela: registra a não conformidade e abre o detalhe.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${unidade}=    Seed A Unidade De Saude Uuid
    ${matricula}=    Seed A Profissional Matricula
    UI Nao Conformidades - Abrir Tela
    Clicar Botao    Registrar não conformidade
    Select Options By    id=f-nc-unidade    value    ${unidade}
    Fill Text    id=f-nc-desc    Lavatório da sala de curativo sem sabão.
    Fill Text    id=f-nc-resp    ${matricula}
    Enviar Gaveta
    Wait For Elements State    css=#drawer-titulo >> text=/Não conformidade \\d{4}\\/\\d{6}/    visible
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Nao Conformidades - Plano De Acao
    [Documentation]    Pela tela: a ação do plano entra e a não conformidade fica em tratamento.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${nc}    ${unidade}    ${matricula}=    Seed Nao Conformidade
    UI Nao Conformidades - Abrir Tela
    ${r}=    GET On Session    msp_session    ${BASE_URL}/qualidade/nao-conformidade/${nc}
    Click    css=button[aria-label="Abrir não conformidade ${r.json()}[numero]"]
    Fill Text    id=f-nc-oque    Checklist diário do carrinho
    Fill Text    id=f-nc-porque    Evitar vencidos
    Fill Text    id=f-nc-quem    ${matricula}
    Fill Text    id=f-nc-quando    2099-12-31
    Click    css=app-drawer button >> text=Adicionar ação
    Wait For Elements State    css=[aria-label="Ações do plano"] >> text=Checklist diário do carrinho    visible
    Wait For Elements State    css=app-drawer dl >> text=/Em tratamento/    visible
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
