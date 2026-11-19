*** Settings ***
Library     String
Resource    ../../../../pom/ui/administrativo/financeiro/financeiro_ui_pom.resource
Resource    ../../../financeiro/financeiro_flow.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
    Jornadas da tela Financeiro (ADR-0218); as bases vêm pela API.
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
UI Financeiro - Pelo Menu
    [Documentation]    O item do menu abre a tela.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Ir Para Rota Do Frontend    /sala-de-situacao
    ${expandido}=    Get Element Count    css=a.nav-item[href="/administrativo/financeiro"]
    IF    ${expandido} == 0
        Click    css=button.nav-group__label >> text=Administrativo
    END
    Click    css=a.nav-item[href="/administrativo/financeiro"]
    Wait For Elements State    css=h1 >> text=Financeiro    visible
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Financeiro - Nova Dotacao
    [Documentation]    Pela tela: registra a dotação do exercício.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${acao}=    Generate Random String    4    [NUMBERS]
    UI Financeiro - Abrir Tela
    Clicar Botao    Nova dotação
    Fill Text    id=f-dt-programa    10.302.0011.${acao}
    Fill Text    id=f-dt-natureza    3.3.90.39
    Fill Text    id=f-dt-descricao    Robot UI Média complexidade ${acao}
    Select Options By    id=f-dt-fonte    value    RECURSOS_PROPRIOS
    Fill Text    id=f-dt-valor    25000
    Enviar Gaveta
    Wait For Elements State    css=[role=status] >> text=Dotação 10.302.0011.${acao} · 3.3.90.39 registrada.    visible
    Wait For Elements State    css=table[aria-label="Dotações"] >> text=Robot UI Média complexidade ${acao}    visible
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Financeiro - Registrar Empenho
    [Documentation]    Pela tela: registra o empenho contra a dotação e o fornecedor.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${dotacao}=    Seed Dotacao    ${5000}
    ${fornecedor}=    Seed Fornecedor Regular
    ${n}=    Generate Random String    6    [NUMBERS]
    UI Financeiro - Abrir Tela
    Click    role=tab[name="Empenhos"]
    Clicar Botao    Registrar empenho
    Fill Text    id=f-em-numero    UI${n}
    Select Options By    id=f-em-dotacao    value    ${dotacao}
    Select Options By    id=f-em-fornecedor    value    ${fornecedor}
    Fill Text    id=f-em-valor    1200
    Enviar Gaveta
    Wait For Elements State    css=[role=status] >> text=Empenho UI${n} registrado.    visible
    Click    css=button[aria-label="Abrir empenho UI${n}"]
    Wait For Elements State    css=app-drawer dl >> text=/R\\$\\s*1\\.200,00/    visible
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

