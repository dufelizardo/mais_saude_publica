*** Settings ***
Resource    ../../../pom/ui/imunizacao/sala_de_vacina_ui_pom.resource
Resource    ../../imunizacao/vacinacao_flow.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
    A base é criada pela API; a ação sob teste é feita pela tela.
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
UI Vacina - Registrar Na Sala
    [Documentation]    Na Sala de Vacina: escolher a unidade, registrar a dT do paciente e vê-la nas doses do dia.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${dados}=    Seed Vacinacao
    ${imuno}=    Id Do Imunobiologico    DT
    UI Vacina - Abrir Sala
    Select Options By    id=f-sv-unidade    value    ${dados}[unidade]
    Clicar Botao    Registrar vacinação
    Fill Text    id=f-vac-paciente    ${dados}[nome]
    Click    css=[aria-label="Pacientes encontrados"] >> role=option[name="${dados}[nome]"]
    Select Options By    id=f-vac-imuno    value    ${imuno}
    Select Options By    id=f-vac-dose    value    REFORCO
    Fill Text    id=f-vac-matricula    ${dados}[matricula]
    Fill Text    id=f-vac-lote    LT-UI-1
    Click    css=app-drawer >> role=button[name="Registrar"]
    Wait For Elements State    css=table[aria-label="Doses aplicadas"] >> text=${dados}[nome]    visible
    Wait For Elements State    css=[aria-label="Doses por vacina"] >> text=dT: 1    visible
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Vacina - Caderneta Do Paciente
    [Documentation]    A aba Vacinação do paciente mostra a dose registrada e o calendário.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${dados}=    Seed Vacinacao
    ${payload}=    Dose Aplicada    ${dados}    DT    REFORCO
    POST Registro Vacinacao    ${payload}
    UI Vacina - Abrir Caderneta    ${dados}[paciente]
    Wait For Elements State    css=ul[aria-label="Doses registradas"] >> text=dT · Reforço    visible
    Wait For Elements State    css=table[aria-label="Calendário vacinal do paciente"]    visible
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
