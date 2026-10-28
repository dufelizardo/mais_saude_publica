*** Settings ***
Resource    ../../../pom/ui/indicadores/sala_de_situacao_ui_pom.resource
Resource    ../../indicadores/producao_flow.resource
Resource    ../../indicadores/situacao_flow.resource
Resource    ../../indicadores/indicadores_rede_flow.resource
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
    [Documentation]    O item Sala de Situação do menu abre a tela com os indicadores e o bloco de vacinação (ADR-0149).
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Ir Para Rota Do Frontend    /assistencia/pacientes
    Click    css=a.nav-item >> text=Sala de Situação
    Wait For Elements State    css=h1 >> text=Sala de Situação da Saúde    visible
    Wait For Elements State    css=[aria-label="Indicadores do período"]    visible
    Wait For Elements State    css=section[aria-label="Vacinação"] >> text=doses aplicadas    visible
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

UI Sala - Situacao Operacional
    [Documentation]    A vaga aberta da próxima hora acende o alerta de escala e aparece nas ocorrências; os cartões da BI2 aparecem.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${unidade}=    Seed Unidade Com Turno Sem Cobertura
    UI Sala - Abrir
    Select Options By    id=f-ss-unidade    value    ${unidade}
    Wait For Elements State    css=[aria-label="Alertas da situação"] >> text=Escala descoberta    visible
    Wait For Elements State    css=table[aria-label="Lista de ocorrências da escala"] >> text=Vaga aberta    visible
    Wait For Elements State    css=[aria-label="Situação agora"] >> text=Dados ao vivo    visible
    Wait For Elements State    css=[aria-label="Ocupação das agendas"]    visible
    Wait For Elements State    css=[aria-label="Tempo de espera"]    visible
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Sala - Indicadores Da Rede
    [Documentation]    Os cartões de programas e de vigilância aparecem; o HiperDia abre a lista por unidade com a unidade nova.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${programa}    ${unidade}    ${nome}=    Seed HiperDia Com Inscrito
    UI Sala - Abrir
    Select Options By    id=f-ss-unidade    value    ${unidade}
    Wait For Elements State    css=[aria-label="Vigilância"] >> text=regularidade    visible
    Click    css=[aria-label="Programas de saúde"] >> role=button[name="Programa: HiperDia"]
    Wait For Elements State    css=table[aria-label="Por unidade: HiperDia"]    visible
    Wait For Elements State    css=table[aria-label="Por unidade: HiperDia"] >> text=Acompanhados    visible
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
