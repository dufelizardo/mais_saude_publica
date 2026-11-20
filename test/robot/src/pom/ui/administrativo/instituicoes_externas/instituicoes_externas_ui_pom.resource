*** Settings ***
Resource    ../../../common/mais_saude_publica_ui_common.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
    Tela Instituições da rede intersetorial (ADR-0154): o catálogo e a gaveta de cadastro e edição.
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
UI Instituicoes Externas - Abrir Tela
    [Documentation]    Abre a tela das instituições da rede intersetorial.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Ir Para Rota Do Frontend    /administrativo/instituicoes-externas
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Instituicoes Externas - Abrir Painel
    [Documentation]    Abre a tela direto na aba Painel (IS2, ADR-0211).
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Ir Para Rota Do Frontend    /administrativo/instituicoes-externas?aba=painel
    Wait For Elements State    role=tab[name="Painel"][selected]    visible
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
