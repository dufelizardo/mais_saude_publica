*** Settings ***
Resource    ../../../common/mais_saude_publica_ui_common.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
    Tela Modelo operacional (ADR-0117): catálogos de perfis operacionais e de capacidades assistenciais; e a aba
    Capacidades da ficha da unidade em Equipamentos de Saúde.
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
UI Modelo Operacional - Abrir
    [Documentation]    Abre a tela Modelo operacional.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Ir Para Rota Do Frontend    /administrativo/modelo-operacional
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Modelo Operacional - Buscar
    [Documentation]    Filtra a tabela da aba aberta pela busca.
    [Arguments]    ${texto}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Fill Text    css=input[placeholder="Código, nome ou descrição…"]    ${texto}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Modelo Operacional - Ver Capacidades Do Perfil
    [Documentation]    Clica em "Capacidades" na linha do perfil.
    [Arguments]    ${nome}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Click    css=tr:has-text("${nome}") >> role=button[name="Capacidades"]
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Modelo Operacional - Filtrar Lista Da Rede Por Perfil
    [Documentation]    Escolhe o perfil no filtro da lista de Equipamentos (pelo código).
    [Arguments]    ${codigo}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Select Options By    css=select[aria-label="Perfil operacional"]    value    ${codigo}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
