*** Settings ***
Library     String
Resource    ../../../../pom/ui/vigilancia/vetores/controle_vetorial_ui_pom.resource
Resource    ../../../vigilancia/zoonoses_flow.resource
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

UI Controle Vetorial - Animal E Observacao
    [Documentation]    Pela tela: registra o cão e abre a observação do agressor (ADR-0163).
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${sufixo}=    Generate Random String    6    [NUMBERS]
    UI Controle Vetorial - Abrir Tela
    Wait For Elements State    css=h1 >> text=Zoonoses e vetores    visible
    Click    role=tab[name="Animais"]
    Clicar Botao    Registrar animal
    Fill Text    id=f-za-nome    Totó ${sufixo}
    Fill Text    id=f-za-tutor    Dona Rita
    Fill Text    id=f-za-endereco    Rua Robot ${sufixo}
    Enviar Gaveta
    Wait For Elements State    css=[aria-label="Animais"] >> text=Totó ${sufixo}    visible
    Click    role=button[name="Abrir observação: Totó ${sufixo}"]
    Enviar Gaveta
    Click    role=tab[name="Observação de agressores"]
    Wait For Elements State    css=[aria-label="Observações de agressores"] tr:has-text("Totó ${sufixo}") >> text=Em observação    visible
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Controle Vetorial - Exame De Leishmaniose
    [Documentation]    Pela tela: coleta do teste rápido e resultado reagente com o desfecho do cão (ADR-0163).
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${animal}=    Seed Cao Zoonoses
    ${payload}=    Create Dictionary    animalId=${animal}    tipo=LEISHMANIOSE_TR_DPP
    ${e}=    POST Exame Animal    ${payload}
    ${nome}=    Set Variable    ${e.json()}[animalNome]
    UI Controle Vetorial - Abrir Tela
    Wait For Elements State    css=h1 >> text=Zoonoses e vetores    visible
    Click    role=tab[name="Animais"]
    Click    css=[aria-label="Exames de animais"] tr:has-text("${nome}") >> role=button[name^="Resultado"]
    Select Options By    id=f-za-resultado    value    REAGENTE
    Select Options By    id=f-za-leish    value    ACOMPANHAMENTO
    Enviar Gaveta
    Wait For Elements State    css=[aria-label="Exames de animais"] tr:has-text("${nome}") >> text=Acompanhamento    visible
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Controle Vetorial - Vacinacao Antirrabica
    [Documentation]    Pela tela: registra o posto de vacinação antirrábica (ADR-0163).
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${area}    ${micro}    ${imovel}=    Seed Imovel Vetorial
    UI Controle Vetorial - Abrir Tela
    Wait For Elements State    css=h1 >> text=Zoonoses e vetores    visible
    Click    role=tab[name="Vacinação antirrábica"]
    Clicar Botao    Registrar posto
    Select Options By    id=f-za-v-area    value    ${area}
    Select Options By    id=f-za-v-micro    value    ${micro}
    Fill Text    id=f-za-v-posto    Posto Robot Praça
    Fill Text    id=f-za-v-caes    18
    Fill Text    id=f-za-v-resp    Equipe Robot
    Enviar Gaveta
    Wait For Elements State    css=[aria-label="Vacinação por posto"] >> text=Posto Robot Praça >> nth=0    visible
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
