*** Settings ***
Library     String
Resource    ../../../pom/ui/qualidade/indicadores_qualidade_ui_pom.resource
Resource    ../../qualidade/indicadores_qualidade_flow.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
    Jornadas da tela Indicadores de qualidade (ADR-0205); as bases vêm pela API.
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
UI Indicadores Qualidade - Pelo Menu
    [Documentation]    O item do menu abre a tela com o painel.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Ir Para Rota Do Frontend    /sala-de-situacao
    Click    css=a.nav-item[href="/qualidade/indicadores"]
    Wait For Elements State    css=h1 >> text=Indicadores de qualidade    visible
    Wait For Elements State    css=[aria-label="Indicadores de qualidade"] >> text=Taxa de quedas    visible
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Indicadores Qualidade - Meta E Fora Da Meta
    [Documentation]    Pela tela: define a meta da unidade; o painel do mês mostra a situação contra ela.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${unidade}=    Seed A Unidade De Saude Uuid
    ${versao}    ${checklist}=    Versao Higiene Das Maos
    UI Indicadores Qualidade - Abrir Tela
    Select Options By    id=f-iq-unidade    value    ${unidade}
    Click    id=tab-metas
    Select Options By    id=f-meta-ind    value    ADESAO_PROTOCOLO
    Select Options By    id=f-meta-check    value    ${checklist}
    Fill Text    id=f-meta-valor    90
    Click    css=button >> text=Definir meta
    Wait For Elements State    css=[role="status"] >> text=Meta definida.    visible
    Wait For Elements State    css=[aria-label="Metas dos indicadores"] >> text=Adesão ao protocolo    visible
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Indicadores Qualidade - Auditoria
    [Documentation]    Pela tela: responde o checklist da higiene das mãos e a auditoria entra na lista com a adesão.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${unidade}=    Seed A Unidade De Saude Uuid
    ${versao}    ${checklist}=    Versao Higiene Das Maos
    UI Indicadores Qualidade - Abrir Tela
    Select Options By    id=f-iq-unidade    value    ${unidade}
    Click    id=tab-auditorias
    Select Options By    id=f-aud-check    value    ${checklist}
    FOR    ${item}    IN    M1    M2    M3    M4
        Click    css=[aria-label="Resposta do item ${item}"] label >> text=Conforme >> nth=0
    END
    Click    css=[aria-label="Resposta do item M5"] label >> text=Não conforme
    Wait For Elements State    text=Adesão parcial: 80%    visible
    Click    css=button >> text=Registrar auditoria
    Wait For Elements State    css=[role="status"] >> text=Auditoria registrada.    visible
    Wait For Elements State    css=[aria-label="Auditorias de protocolo"] >> text=/80% 4 de 5/    visible
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
