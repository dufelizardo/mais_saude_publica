*** Settings ***
Library     String
Resource    ../../../../pom/ui/vigilancia/emergencias/emergencias_ui_pom.resource
Resource    ../../../emergencias/contingencia_flow.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
    Jornadas da tela Emergências e desastres (ADR-0156); as bases vêm pela API.
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
UI Emergencias - Registrar E Converter
    [Documentation]    Registra o evento pela gaveta e converte em emergência no nível 1.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${sufixo}=    Generate Random String    6    [NUMBERS]
    ${descricao}=    Set Variable    Robot tela deslizamento ${sufixo}
    ${tipo}=    Id Do Tipo Emergencia    DESLIZAMENTO
    ${nivel}=    Id Do Nivel Emergencia    1
    UI Emergencias - Abrir Tela
    Clicar Botao    Registrar evento
    Enviar Gaveta
    Wait For Elements State    css=app-drawer >> text=Informe o tipo, a fonte e a descrição.    visible
    Wait For Elements State    css=#f-em-tipo option[value="${tipo}"]    attached
    Select Options By    id=f-em-tipo    value    ${tipo}
    Select Options By    id=f-em-fonte    value    DEFESA_CIVIL
    Fill Text    id=f-em-descricao    ${descricao}
    Enviar Gaveta
    Click    role=tab[name=/^Eventos/]
    Wait For Elements State    css=table[aria-label="Eventos emergenciais"] >> tr:has-text("${descricao}")    visible
    Click    css=table[aria-label="Eventos emergenciais"] >> tr:has-text("${descricao}") >> role=button[name^="Converter em emergência"]
    Fill Text    id=f-em-nome    Deslizamento Robot ${sufixo}
    Select Options By    id=f-em-nivel    value    ${nivel}
    Fill Text    id=f-em-justificativa    Casas atingidas na encosta
    Enviar Gaveta
    Wait For Elements State    css=[aria-label="Emergência Deslizamento Robot ${sufixo}"]    visible
    Wait For Elements State    css=[aria-label="Histórico de nível"] >> text=/Ativada no/    visible
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Emergencias - Nivel E Avaliacao
    [Documentation]    Na emergência: eleva o nível e registra a avaliação de impacto.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${em}=    Seed Emergencia
    ${nome}=    Set Variable    ${em.json()}[nome]
    ${nivel}=    Id Do Nivel Emergencia    2
    UI Emergencias - Abrir Tela    ${em.json()}[uuid]
    Wait For Elements State    css=section.em-detalhe    visible
    Clicar Botao    Mudar nível
    Select Options By    id=f-em-nivel    value    ${nivel}
    Fill Text    id=f-em-justificativa    Hospital atingido
    Enviar Gaveta
    Wait For Elements State    css=[aria-label="Histórico de nível"] >> text=/Nível 1 · Resposta local →/    visible
    Clicar Botao    Nova avaliação
    Select Options By    id=f-em-impacto    value    CRITICO
    Fill Text    id=f-em-populacao    4800
    Enviar Gaveta
    Wait For Elements State    css=[aria-label="Avaliações de impacto"] >> text=/4800 pessoas afetadas/    visible
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Emergencias - Ativar Plano
    [Documentation]    Na emergência, ativa o plano vigente do tipo; as ações previstas do nível aparecem.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${em}=    Seed Emergencia
    ${plano}=    Seed Plano Vigente    INUNDACAO    1
    UI Emergencias - Abrir Tela    ${em.json()}[uuid]
    Wait For Elements State    css=section.em-detalhe    visible
    Clicar Botao    Ativar plano
    Wait For Elements State    css=#f-em-plano option[value="${plano}"]    attached
    Select Options By    id=f-em-plano    value    ${plano}
    Fill Text    id=f-em-autoridade    Secretário de Saúde
    Fill Text    id=f-em-justificativa    Cota de alerta atingida
    Enviar Gaveta
    Wait For Elements State    css=[aria-label="Planos da emergência"] >> text=Ativo    visible
    Wait For Elements State    css=[aria-label^="Ações previstas para o nível"] >> text=Abrir o COE    visible
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Emergencias - Plano Pelo Editor
    [Documentation]    Na aba de planos: o rascunho pelo editor e a publicação.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${sufixo}=    Generate Random String    6    [NUMBERS]
    ${tipo}=    Id Do Tipo Emergencia    DESLIZAMENTO
    ${n1}=    Id Do Nivel Emergencia    1
    UI Emergencias - Abrir Tela
    Click    role=tab[name="Planos de contingência"]
    Clicar Botao    Novo plano
    Fill Text    id=f-pc-codigo    ROBOT_TELA_${sufixo}
    Fill Text    id=f-pc-nome    Plano de encostas ${sufixo}
    Select Options By    id=f-pc-tipo    value    ${tipo}
    Fill Text    id=f-pc-objetivo    Retirar famílias das áreas de risco
    Click    css=app-drawer >> label:has-text("Nível 1 · Resposta local") >> input
    Clicar Botao    Adicionar ação
    Fill Text    css=app-drawer >> [aria-label="Ação 1"]    Evacuar a encosta
    Fill Text    css=app-drawer >> [aria-label="Responsável pela ação 1"]    Defesa Civil e Atenção Básica
    Enviar Gaveta
    Wait For Elements State    css=app-drawer >> text=Rascunho    visible
    Click    css=app-drawer >> role=button[name="Publicar"]
    Wait For Elements State    css=app-drawer >> [aria-label="Versões do plano"] >> text=Vigente    visible
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
