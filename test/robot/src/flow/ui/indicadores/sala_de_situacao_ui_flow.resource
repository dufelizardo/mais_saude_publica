*** Settings ***
Resource    ../../../pom/ui/indicadores/sala_de_situacao_ui_pom.resource
Resource    ../../indicadores/producao_flow.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
    A base é criada pela API; a consulta sob teste é feita pela tela.
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
UI Sala - Atendimentos Devem Ser
    [Documentation]    Confere o indicador de atendimentos concluídos.
    [Arguments]    ${esperado}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${valor}=    UI Sala - Ler Atendimentos Concluidos
    Should Be Equal    ${valor}    ${esperado}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Sala - Abrir Pelo Menu
    [Documentation]    O item Sala de Situação do menu abre a tela com os indicadores e os blocos Em breve.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Ir Para Rota Do Frontend    /assistencia/pacientes
    Click    css=a.nav-item >> text=Sala de Situação
    Wait For Elements State    css=h1 >> text=Sala de Situação da Saúde    visible
    Wait For Elements State    css=[aria-label="Indicadores do período"]    visible
    Wait For Elements State    css=[aria-label="Em breve na Sala de Situação"] >> text=Cobertura vacinal e doses aplicadas    visible
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Sala - Producao Da Unidade
    [Documentation]    Escolhida a unidade, o atendimento concluído hoje aparece em Hoje e no gráfico por mês.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${unidade}    ${nome}=    Seed Unidade Com Atendimento Concluido
    UI Sala - Abrir
    Select Options By    id=f-ss-unidade    value    ${unidade}
    Wait Until Keyword Succeeds    10s    500ms    UI Sala - Atendimentos Devem Ser    1
    Click    css=[aria-label="Período"] >> role=button[name="Hoje"]
    Wait Until Keyword Succeeds    10s    500ms    UI Sala - Atendimentos Devem Ser    1
    Click    css=[aria-label="Granularidade"] >> role=button[name="Mês"]
    Wait For Elements State    css=svg[aria-label="Gráfico de atendimentos por mês"]    visible
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
