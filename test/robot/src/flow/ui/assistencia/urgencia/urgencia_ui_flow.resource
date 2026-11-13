*** Settings ***
Library     Browser
Library     String
Resource    ../../../../pom/common/mais_saude_publica_ui_common.resource
Resource    ../../../urgencia/urgencia_flow.resource
Resource    ../../../urgencia/ficha_aph_flow.resource
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

UI Urgencia - Preencher Ficha Do APH
    [Documentation]    A equipe preenche a ficha, registra sinais vitais e finaliza pela gaveta (ADR-0188).
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${id}    ${central}    ${matricula}=    Seed Ocorrencia Com Recurso
    Get Mais Saude Publica Session
    ${o}=    GET On Session    msp_session    ${BASE_URL}/urgencia/ocorrencia/${id}
    ${protocolo}=    Set Variable    ${o.json()}[protocolo]
    UI Urgencia - Abrir Na Central    ${central}
    Clicar Botao    Ficha do APH da ocorrência ${protocolo}
    Fill Text    id=f-matriculaAph    ${matricula}
    Select Options By    id=f-natureza    value    TRAUMA
    Select Options By    id=f-mecanismo    value    QUEDA
    Fill Text    id=f-situacaoEncontrada    Caído da escada, consciente.
    Select Options By    id=f-glasgowOcular    value    4
    Select Options By    id=f-glasgowVerbal    value    5
    Select Options By    id=f-glasgowMotor    value    6
    Check Checkbox    id=f-proc-COLAR_CERVICAL
    Select Options By    id=f-desfechoAph    value    REMOVIDO_AO_DESTINO
    Fill Text    id=f-recebidoPor    Enf. Clara
    Click    css=app-drawer form button[type="submit"]
    ${aviso}=    Ler Aviso De Status
    Should Contain    ${aviso}    Ficha salva
    Fill Text    id=f-spo2    95
    Fill Text    id=f-fc    88
    Clicar Botao    Registrar sinais
    Wait For Elements State    css=app-drawer >> text=SpO2 95%    visible
    Clicar Botao    Finalizar
    Wait For Elements State    css=app-drawer >> text=Ficha finalizada em    visible
    ${f}=    GET On Session    msp_session    ${BASE_URL}/aph/ocorrencia/${id}/ficha
    Should Be True    ${f.json()}[finalizada]
    Should Be Equal As Integers    ${f.json()}[glasgowTotal]    15
    Length Should Be    ${f.json()}[sinaisVitais]    1
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
