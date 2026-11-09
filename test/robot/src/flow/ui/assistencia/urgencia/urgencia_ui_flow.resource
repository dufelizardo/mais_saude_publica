*** Settings ***
Library     Browser
Library     String
Resource    ../../../../pom/common/mais_saude_publica_ui_common.resource
Resource    ../../../urgencia/urgencia_flow.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
    Jornadas da tela Regulação de urgência (ADR-0176).
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
UI Urgencia - Abrir Na Central
    [Documentation]    Abre a tela e escolhe a central, quando há mais de uma.
    [Arguments]    ${central}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Ir Para Rota Do Frontend    /assistencia/urgencia
    Wait For Elements State    css=h1 >> text=Regulação de urgência    visible
    ${filtro}=    Get Element Count    css=select[aria-label="Central de regulação"]
    IF    ${filtro} > 0
        Wait For Elements State    css=select[aria-label="Central de regulação"] option[value="${central}"]    attached
        Select Options By    css=select[aria-label="Central de regulação"]    value    ${central}
    END
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Urgencia - Chamado Regulado E Recurso Enviado
    [Documentation]    Sem queixa, a mensagem; abre o chamado, regula com envio de USA e registra a saída do recurso.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${central}=    Seed Central De Urgencia
    ${regulador}=    Seed A Profissional Matricula
    UI Urgencia - Abrir Na Central    ${central}
    Clicar Botao    Novo chamado
    Wait For Elements State    css=#f-centralChamado option[value="${central}"]    attached
    Select Options By    id=f-centralChamado    value    ${central}
    Enviar Gaveta
    ${erro}=    Ler Erro Do Campo Da Gaveta    queixaChamado
    Should Contain    ${erro}    queixa
    Preencher Campo Da Gaveta    telefoneChamado    11966665555
    Preencher Campo Da Gaveta    queixaChamado    Dor no peito forte há 20 minutos.
    Enviar Gaveta
    ${aviso}=    Ler Aviso De Status
    Should Contain    ${aviso}    Ocorrência aberta
    Wait For Elements State    css=#h-ocorrencia >> text=Ocorrência    visible
    Clicar Botao    Regular
    Select Options By    id=f-prioridadeRegular    value    VERMELHO
    Select Options By    id=f-recursoRegular    value    USA
    Preencher Campo Da Gaveta    condutaRegular    Suspeita de síndrome coronariana; USA ao local.
    Preencher Campo Da Gaveta    reguladorRegular    ${regulador}
    Enviar Gaveta
    ${aviso}=    Ler Aviso De Status
    Should Contain    ${aviso}    Ocorrência regulada
    Clicar Botao    Registrar: Recurso enviado
    Wait For Elements State    css=ol[aria-label="Linha do tempo da ocorrência"] .cell-main >> text="Recurso enviado"    visible
    Wait For Elements State    css=section[aria-labelledby="h-ocorrencia"] >> text=Recurso em campo    visible
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
