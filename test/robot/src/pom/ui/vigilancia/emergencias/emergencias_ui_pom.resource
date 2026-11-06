*** Settings ***
Resource    ../../../common/mais_saude_publica_ui_common.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
    Tela Emergências e desastres (ADR-0156): abas, detalhe da emergência e gavetas.
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
UI Emergencias - Abrir Tela
    [Documentation]    Abre a tela de emergências, opcionalmente numa emergência.
    [Arguments]    ${emergencia}=${EMPTY}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    IF    '${emergencia}' == '${EMPTY}'
        Ir Para Rota Do Frontend    /vigilancia/emergencias
    ELSE
        Ir Para Rota Do Frontend    /vigilancia/emergencias?emergencia=${emergencia}
    END
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
