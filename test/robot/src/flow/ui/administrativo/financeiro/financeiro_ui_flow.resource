*** Settings ***
Library     String
Resource    ../../../../pom/ui/administrativo/financeiro/financeiro_ui_pom.resource
Resource    ../../../financeiro/financeiro_flow.resource
Resource    ../../../financeiro/liquidacao_flow.resource
Resource    ../../../financeiro/custos_flow.resource
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

UI Financeiro - Liquidar E Pagar
    [Documentation]    Pela tela: liquida o recebimento da fila, paga a liquidação e abre a execução.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${rec}    ${empenho}    ${numero}=    Seed Recebimento Liquidavel
    UI Financeiro - Abrir Tela
    Click    role=tab[name="A liquidar"]
    Click    css=button[aria-label^="Liquidar Ordem ${numero}"]
    Fill Text    id=f-lq-iss    3
    Enviar Gaveta
    Wait For Elements State    css=[role=status] >> text=/Liquidado: Ordem ${numero}/    visible
    Click    role=tab[name="Liquidações"]
    Click    css=button[aria-label^="Pagar Ordem ${numero}"]
    Fill Text    id=f-pg-ob    OB-UI
    Fill Text    id=f-pg-justificativa    Pagamento pela tela.
    Enviar Gaveta
    Wait For Elements State    css=[role=status] >> text=/Pago: Ordem ${numero}/    visible
    Click    role=tab[name="Execução"]
    Wait For Elements State    css=table[aria-label="Execução por fonte"]    visible
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Frota - Diarias Do Tfd
    [Documentation]    Pela tela: a solicitação de TFD autorizada concede a diária ao paciente.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${id}    ${cpfPaciente}    ${nome}=    Seed Tfd Autorizado
    ${cpf}=    Gerar CPF
    Ir Para Rota Do Frontend    /administrativo/frota
    Wait For Elements State    css=h1 >> text=Frota e transporte    visible
    Clicar Aba    Solicitações
    Click    css=button[aria-label="Diárias do TFD de ${nome}"]
    Fill Text    id=f-dt-cpf    ${cpf}
    Fill Text    id=f-dt-dias    3
    Fill Text    id=f-dt-valor    24.75
    Enviar Gaveta
    Wait For Elements State    css=app-drawer [role=status] >> text=/Diária de R\\$\\s*74,25 concedida/    visible
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Financeiro - Custos
    [Documentation]    Pela tela: a aba Custos apura a competência anterior (se aberta) e cria o centro de um setor.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${setor}    ${codigo}    ${unidade}=    Seed A Setor    ASSISTENCIAL
    UI Financeiro - Abrir Tela
    Click    role=tab[name="Custos"]
    Wait For Elements State    css=table[aria-label="Centros de custo"]    visible
    ${apurar}=    Get Element Count    css=app-custos-financeiro button.btn >> text=/Apurar|Refazer a apuração/
    IF    ${apurar} > 0
        Click    css=app-custos-financeiro button.btn >> text=/Apurar|Refazer a apuração/
        Wait For Elements State    css=[role=status] >> text=/apurada/    visible
    END
    Wait For Elements State    css=table[aria-label="Custo por centro"]    visible
    Clicar Botao    Novo centro do setor
    Wait For Elements State    css=#f-cc-setor option[value="${setor}"]    attached
    Select Options By    id=f-cc-setor    value    ${setor}
    Fill Text    id=f-cc-nome    Robot UI Centro ${codigo}
    Enviar Gaveta
    Wait For Elements State    css=[role=status] >> text=/Robot UI Centro ${codigo} criado/    visible
    Wait For Elements State    css=table[aria-label="Centros de custo"] >> text=Robot UI Centro ${codigo}    visible
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
