*** Settings ***
Library     String
Resource    ../../../../pom/ui/vigilancia/emergencias/emergencias_ui_pom.resource
Resource    ../../../emergencias/emergencias_flow.resource
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
