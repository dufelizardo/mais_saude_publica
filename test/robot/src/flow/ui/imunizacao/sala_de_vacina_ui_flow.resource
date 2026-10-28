*** Settings ***
Resource    ../../../pom/ui/imunizacao/sala_de_vacina_ui_pom.resource
Resource    ../../imunizacao/vacinacao_flow.resource
Resource    ../../imunizacao/sala_de_vacina_flow.resource
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

UI Vacina - Entrada E Baixa
    [Documentation]    Na aba Estoque: dar entrada num lote de dT; registrar a dose escolhendo o lote do estoque; ver o frasco aberto (ADR-0148).
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${dados}=    Seed Sala De Vacina
    ${imuno}=    Id Do Imunobiologico    DT
    ${validade}=    Get Current Date    increment=120 days    result_format=%Y-%m-%d
    ${numero}=    Generate Random String    6    [UPPER][NUMBERS]
    UI Vacina - Abrir Sala
    Select Options By    id=f-sv-unidade    value    ${dados}[unidade]
    Click    role=tab[name="Estoque"]
    Clicar Botao    Entrada de lote
    Select Options By    id=f-ent-imuno    value    ${imuno}
    Fill Text    id=f-ent-lote    UI${numero}
    Fill Text    id=f-ent-validade    ${validade}
    Fill Text    id=f-ent-doses    10
    Click    css=app-drawer >> role=button[name="Registrar"]
    Wait For Elements State    css=table[aria-label="Lotes"] >> text=UI${numero}    visible
    Clicar Botao    Registrar vacinação
    Fill Text    id=f-vac-paciente    ${dados}[nome]
    Click    css=[aria-label="Pacientes encontrados"] >> role=option[name="${dados}[nome]"]
    Select Options By    id=f-vac-imuno    value    ${imuno}
    Select Options By    id=f-vac-dose    value    REFORCO
    Fill Text    id=f-vac-matricula    ${dados}[matricula]
    Wait For Elements State    id=f-vac-lote-estoque >> option >> text=UI${numero}    attached
    Select Options By    id=f-vac-lote-estoque    label    UI${numero} · val. ${validade[8:10]}/${validade[5:7]}/${validade[0:4]} · 10 doses
    Click    css=app-drawer >> role=button[name="Registrar"]
    Wait For Elements State    css=table[aria-label="Frascos abertos"] >> text=9 de 10    visible
    Wait For Elements State    css=table[aria-label="Lotes"] >> tr:has-text("UI${numero}") >> text=9    visible
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Vacina - Excursao E Avaliacao
    [Documentation]    Na aba Cadeia de frio: registrar a leitura fora da faixa, ver o lote sob suspeita e liberá-lo com o parecer (ADR-0148).
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${dados}=    Seed Sala De Vacina
    ${payload}=    Create Dictionary    unidadeId=${dados}[unidade]    nome=Câmara UI
    ${c}=    POST Camara Refrigeracao    ${payload}
    ${l}=    Entrada De Lote    ${dados}[unidade]    DT    ${c.json()}[uuid]
    UI Vacina - Abrir Sala
    Select Options By    id=f-sv-unidade    value    ${dados}[unidade]
    Click    role=tab[name="Cadeia de frio"]
    Click    role=button[name="Registrar leitura da Câmara UI"]
    Fill Text    id=f-lei-atual    6
    Fill Text    id=f-lei-min    4
    Fill Text    id=f-lei-max    11
    Click    css=app-drawer >> role=button[name="Salvar"]
    Wait For Elements State    css=table[aria-label="Leituras de temperatura"] >> text=Excursão    visible
    Wait For Elements State    css=section[aria-label="Lotes sob suspeita"] >> text=${l.json()}[numeroLote]    visible
    Click    role=button[name="Avaliar o lote ${l.json()}[numeroLote]"]
    Fill Text    id=f-ava-parecer    Dentro da estabilidade informada pelo fabricante.
    Click    css=app-drawer >> role=button[name="Liberar"]
    Wait For Elements State    css=section[aria-label="Lotes sob suspeita"]    detached
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
