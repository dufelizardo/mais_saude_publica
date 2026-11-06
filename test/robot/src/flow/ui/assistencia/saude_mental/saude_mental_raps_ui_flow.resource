*** Settings ***
Library     String
Resource    ../../../../pom/ui/assistencia/saude_mental/saude_mental_ui_pom.resource
Resource    ../../../saude_mental/saude_mental_raps_flow.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
    A unidade vira CAPS III com acolhimento noturno pela API; a consulta e a entrada são feitas pela tela (ADR-0153).
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
UI Saude Mental - Rede Raps
    [Documentation]    Pelo botão "Rede (RAPS)": a unidade aparece na atenção psicossocial especializada com o CAPS III.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${caso}    ${unidade}    ${paciente}    ${equipe}    ${matricula}=    Seed Caps Com Acolhimento
    ${c}=    GET Caso Saude Mental    ${caso}
    Ir Para Rota Do Frontend    /assistencia/saude-mental
    Click    role=link[name="Rede (RAPS)"]
    Wait For Elements State    css=section[aria-label="Atenção psicossocial especializada"] >> tr:has-text("${c.json()}[unidadeNome]") >> text=CAPS III    visible
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Saude Mental - Acolhimento Noturno
    [Documentation]    Na tela do acolhimento noturno: a entrada pelo caso da unidade e a saída com o motivo.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${caso}    ${unidade}    ${paciente}    ${equipe}    ${matricula}=    Seed Caps Com Acolhimento
    ${c}=    GET Caso Saude Mental    ${caso}
    ${nome}=    Set Variable    ${c.json()}[pacienteNome]
    ${prevista}=    Evaluate    (datetime.date.today() + datetime.timedelta(days=5)).isoformat()    modules=datetime
    Ir Para Rota Do Frontend    /assistencia/saude-mental/acolhimento-noturno
    Select Options By    id=f-an-unidade    value    ${unidade}
    Click    role=button[name="Registrar entrada"]
    Wait For Elements State    id=f-an-paciente >> option[value="${paciente}"]    attached
    Select Options By    id=f-an-paciente    value    ${paciente}
    Fill Text    id=f-an-prevista    ${prevista}
    Click    css=app-drawer >> role=button[name="Salvar"]
    Wait For Elements State    css=table[aria-label="Pacientes acolhidos"] >> text=${nome}    visible
    Click    role=button[name="Saída de ${nome}"]
    Fill Text    id=f-an-motivo    Estabilizado
    Click    css=app-drawer >> role=button[name="Salvar"]
    Wait For Elements State    css=[aria-label="Saídas recentes"] >> text=${nome}    visible
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
