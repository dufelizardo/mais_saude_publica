*** Settings ***
Library     String
Resource    ../../../../pom/ui/vigilancia/vetores/controle_vetorial_ui_pom.resource
Resource    ../../../vigilancia/controle_vetorial_flow.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
    Jornadas da tela Zoonoses e vetores (ADR-0162); as bases vêm pela API.
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
UI Controle Vetorial - Pelo Menu
    [Documentation]    O item do menu Vigilância abre a tela.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Ir Para Rota Do Frontend    /vigilancia
    ${expandido}=    Get Element Count    css=a.nav-item[href="/vigilancia/vetores"]
    IF    ${expandido} == 0
        Click    css=button.nav-group__label >> text=Vigilância
    END
    Click    css=a.nav-item[href="/vigilancia/vetores"]
    Wait For Elements State    css=h1 >> text=Zoonoses e vetores    visible
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Controle Vetorial - Registrar Vistoria
    [Documentation]    Pela tela: registra a vistoria com um foco e vê na tabela e nos índices.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${area}    ${micro}    ${imovel}=    Seed Imovel Vetorial
    ${matricula}=    Seed A Profissional Matricula
    UI Controle Vetorial - Abrir Tela
    Wait For Elements State    css=h1 >> text=Zoonoses e vetores    visible
    Select Options By    id=f-cv-area    value    ${area}
    Clicar Botao    Registrar vistoria
    Wait For Elements State    css=#f-cv-v-imovel option[value="${imovel}"]    attached
    Select Options By    id=f-cv-v-imovel    value    ${imovel}
    Fill Text    id=f-cv-v-agente    ${matricula}
    Fill Text    id=f-cv-dep-B    3
    Click    role=button[name="Adicionar foco"]
    Enviar Gaveta
    Wait For Elements State    css=[aria-label="Vistorias do ciclo"] >> text=1 foco(s)    visible
    Click    role=tab[name=/Índices do ciclo/]
    Wait For Elements State    css=[aria-label="Índices por área"] >> text=Risco    visible
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Controle Vetorial - Acao De Controle
    [Documentation]    Pela tela: planeja a ação na microárea e registra a realização.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${area}    ${micro}    ${imovel}=    Seed Imovel Vetorial
    UI Controle Vetorial - Abrir Tela
    Wait For Elements State    css=h1 >> text=Zoonoses e vetores    visible
    Click    role=tab[name=/Ações de controle/]
    Clicar Botao    Planejar ação
    Select Options By    id=f-cv-a-area    value    ${area}
    Click    css=app-drawer label.cv-check >> nth=0
    Fill Text    id=f-cv-a-resp    Equipe Robot de endemias
    Enviar Gaveta
    Wait For Elements State    css=[aria-label="Ações de controle"] >> text=Equipe Robot de endemias    visible
    Click    css=[aria-label="Ações de controle"] tr:has-text("Equipe Robot de endemias") >> role=button[name^="Registrar realização"]
    Fill Text    id=f-cv-r-imoveis    35
    Enviar Gaveta
    Wait For Elements State    css=[aria-label="Ações de controle"] tr:has-text("Equipe Robot de endemias") >> text=Realizada    visible
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
