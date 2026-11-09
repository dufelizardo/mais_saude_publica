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

UI Catalogos - Ligar Competencia Ao Treinamento
    [Documentation]    Pela gaveta Competências do treinamento (ADR-0173): marca uma competência e a contagem aparece na lista.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${uuid}    ${nome}=    Seed A Treinamento
    UI Catalogos - Abrir Rota    /rh/desenvolvimento
    Clicar Botao    Competências e conteúdos de ${nome}
    Check Checkbox    css=label.perm-check:has-text("Suporte básico de vida") >> input
    Enviar Gaveta
    ${aviso}=    Ler Aviso De Status
    Should Contain    ${aviso}    Competências e conteúdos salvos
    Esperar Texto Aparecer    1 competência(s) · 0 conteúdo(s)
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Catalogos - Cadastrar Competencia
    [Documentation]    Pela aba Competências (ADR-0173): a carga aparece e a competência nova entra na lista.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${sufixo}=    Generate Random String    6    [UPPER]
    UI Catalogos - Abrir Rota    /rh/desenvolvimento?aba=competencias
    Esperar Texto Aparecer    Biossegurança e controle de infecção
    Clicar Botao    Nova competência
    Enviar Gaveta
    ${erro}=    Ler Erro Do Campo Da Gaveta    codigoCompetencia
    Should Contain    ${erro}    código
    Preencher Campo Da Gaveta    codigoCompetencia    UI_${sufixo}
    Preencher Campo Da Gaveta    nomeCompetencia    Competência ${sufixo}
    Enviar Gaveta
    ${aviso}=    Ler Aviso De Status
    Should Contain    ${aviso}    Competência salva
    Esperar Texto Aparecer    Competência ${sufixo}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

