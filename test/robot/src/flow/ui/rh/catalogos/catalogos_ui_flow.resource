*** Settings ***
Resource    ../../../../pom/ui/rh/catalogos/catalogos_ui_pom.resource
Resource    ../../../treinamento/update_treinamento/update_treinamento_flow.resource
Resource    ../../../tipo_beneficio/update_tipo_beneficio/update_tipo_beneficio_flow.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
    O item é criado pela API; a edição é feita pela gaveta da tela.
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
UI Catalogos - Editar Treinamento Pela Gaveta
    [Documentation]    Edita o nome do treinamento e vê o aviso e o nome novo.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${uuid}    ${nome}=    Seed A Treinamento
    UI Catalogos - Abrir Rota    /rh/desenvolvimento
    UI Catalogos - Clicar Editar    ${nome}
    ${titulo}=    Ler Titulo Da Gaveta
    Should Be Equal    ${titulo}    Editar treinamento
    Preencher Campo Da Gaveta    nome    ${nome} UI
    Enviar Gaveta
    ${aviso}=    Ler Aviso De Status
    Should Contain    ${aviso}    Treinamento atualizado
    Esperar Texto Aparecer    ${nome} UI
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Catalogos - Editar Beneficio Pela Gaveta
    [Documentation]    Edita o nome do tipo de benefício e vê o aviso e o nome novo.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${uuid}    ${nome}=    Seed A Tipo Beneficio
    UI Catalogos - Abrir Rota    /rh/beneficios
    UI Catalogos - Clicar Editar    ${nome}
    ${titulo}=    Ler Titulo Da Gaveta
    Should Be Equal    ${titulo}    Editar benefício
    Preencher Campo Da Gaveta    nome    ${nome} UI
    Enviar Gaveta
    ${aviso}=    Ler Aviso De Status
    Should Contain    ${aviso}    Benefício atualizado
    Esperar Texto Aparecer    ${nome} UI
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
