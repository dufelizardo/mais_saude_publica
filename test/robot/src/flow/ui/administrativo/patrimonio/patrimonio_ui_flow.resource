*** Settings ***
Library     String
Resource    ../../../../pom/ui/administrativo/patrimonio/patrimonio_ui_pom.resource
Resource    ../../../patrimonio/patrimonio_flow.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
    Jornadas da tela Patrimônio (ADR-0215); as bases vêm pela API.
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
UI Patrimonio - Pelo Menu
    [Documentation]    O item do menu abre a tela.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Ir Para Rota Do Frontend    /sala-de-situacao
    ${expandido}=    Get Element Count    css=a.nav-item[href="/administrativo/patrimonio"]
    IF    ${expandido} == 0
        Click    css=button.nav-group__label >> text=Administrativo
    END
    Click    css=a.nav-item[href="/administrativo/patrimonio"]
    Wait For Elements State    css=h1 >> text=Patrimônio    visible
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Patrimonio - Cadastro
    [Documentation]    Pela tela: cadastra o bem doado, que entra tombado e aparece na lista da unidade.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${setor}    ${codigo}    ${unidade}=    Seed A Setor
    ${matricula}=    Seed A Profissional Matricula
    ${sufixo}=    Generate Random String    6    [UPPER]
    UI Patrimonio - Abrir Tela
    Clicar Botao    Cadastrar bem
    Fill Text    id=f-pt-descricao    Robot UI Mesa ${sufixo}
    Select Options By    id=f-pt-cad-classe    value    MOBILIARIO
    Select Options By    id=f-pt-cad-unidade    value    ${unidade}
    Select Options By    id=f-pt-cad-setor    value    ${setor}
    Fill Text    id=f-pt-responsavel    ${matricula}
    Enviar Gaveta
    Wait For Elements State    css=[role=status] >> text=/Bem cadastrado com o tombamento \\d{4}\\/\\d{6}/    visible
    UI Patrimonio - Filtrar Unidade    ${unidade}
    Wait For Elements State    css=table[aria-label="Bens patrimoniais"] >> text=Robot UI Mesa ${sufixo}    visible
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Patrimonio - Transferencia
    [Documentation]    Pela tela: a ficha envia o bem para outra unidade e a aba Transferências aceita.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${bem}    ${unidade}    ${matricula}=    Seed Bem Patrimonial
    ${setor2}    ${codigo}    ${destino}=    Seed A Setor
    ${r}=    GET Bem Patrimonial    ${bem}
    ${numero}=    Set Variable    ${r.json()}[bem][numeroTombamento]
    ${novo}=    Seed A Profissional Matricula
    UI Patrimonio - Abrir Tela
    UI Patrimonio - Filtrar Unidade    ${unidade}
    Click    css=button[aria-label="Abrir bem ${numero}"]
    Select Options By    id=f-pt-destino    value    ${destino}
    Fill Text    id=f-pt-motivo-envio    Remanejamento pela tela.
    Click    css=app-drawer button >> text=Enviar
    Wait For Elements State    css=app-drawer >> text=/à espera do aceite/    visible
    Click    css=app-drawer button[aria-label="Fechar"]
    Click    role=tab[name=/Transferências/]
    Click    css=button[aria-label="Aceitar ${numero}"]
    Select Options By    id=f-pt-ac-setor    value    ${setor2}
    Fill Text    id=f-pt-ac-responsavel    ${novo}
    Enviar Gaveta
    Wait For Elements State    css=[role=status] >> text=Bem recebido na unidade.    visible
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Patrimonio - Tombamento
    [Documentation]    Pela tela: o bem comprado aparece na aba A tombar e recebe o número.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${sufixo}=    Generate Random String    6    [UPPER]
    Seed Bem A Tombar    Robot UI Autoclave ${sufixo}
    ${matricula}=    Seed A Profissional Matricula
    UI Patrimonio - Abrir Tela
    Click    role=tab[name=/A tombar/]
    Click    css=button[aria-label="Tombar Robot UI Autoclave ${sufixo}"]
    Select Options By    id=f-pt-tb-classe    value    EQUIPAMENTO_MEDICO_HOSPITALAR
    Fill Text    id=f-pt-tb-serie    SN-${sufixo}
    Fill Text    id=f-pt-tb-responsavel    ${matricula}
    Enviar Gaveta
    Wait For Elements State    css=[role=status] >> text=/Bem tombado com o número \\d{4}\\/\\d{6}/    visible
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

