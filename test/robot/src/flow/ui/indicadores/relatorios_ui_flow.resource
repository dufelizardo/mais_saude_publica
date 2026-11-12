*** Settings ***
Library     Browser
Resource    ../../../pom/common/mais_saude_publica_ui_common.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
    Tela Relatórios (ADR-0167): catálogo, prévia e exportação.
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
UI Relatorios - Abrir Pelo Menu
    [Documentation]    O item Relatórios do menu abre a tela com a primeira prévia.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Ir Para Rota Do Frontend    /assistencia/pacientes
    Click    css=a.nav-item >> text=Relatórios
    Wait For Elements State    css=h1 >> text=Relatórios    visible
    Wait For Elements State    css=.rel-detalhe h2 >> text=Produção assistencial    visible
    Wait For Elements State    css=[aria-label="Indicadores resumidos"] >> text=Atendimentos concluídos    visible
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Relatorios - Filtrar E Escolher
    [Documentation]    Filtra a categoria, busca e abre a cobertura vacinal.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Ir Para Rota Do Frontend    /relatorios
    Click    css=.rel-chip:has-text("Epidemiológico")
    Wait For Elements State    css=.rel-item:has-text("Produção assistencial")    detached
    Fill Text    css=.rel-busca input    cobertura
    Wait For Elements State    css=.rel-item:has-text("Vigilância em saúde")    detached
    Click    css=.rel-item:has-text("Cobertura vacinal")
    Wait For Elements State    css=.rel-detalhe h2 >> text=Cobertura vacinal    visible
    Wait For Elements State    css=.rel-detalhe >> text=Doses aplicadas    visible
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Relatorios - Exportar
    [Documentation]    Exporta o relatório aberto e vê o aviso.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Ir Para Rota Do Frontend    /relatorios
    Wait For Elements State    css=.rel-detalhe h2    visible
    Clicar Botao    Exportar CSV
    ${aviso}=    Ler Aviso De Status
    Should Contain    ${aviso}    relatorio-producao.csv
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Relatorios - Periodo Invalido
    [Documentation]    Com o fim antes do início, a mensagem da API aparece no lugar da prévia.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Ir Para Rota Do Frontend    /relatorios
    Wait For Elements State    css=.rel-detalhe h2    visible
    Fill Text    id=f-rel-de    2030-01-10
    Fill Text    id=f-rel-ate    2030-01-01
    Wait For Elements State    css=.rel-detalhe [role="alert"]    visible
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════


UI Relatorios - Comunicacao E Educacao
    [Documentation]    A categoria Comunicação e educação traz os dois relatórios da C6 (ADR-0196).
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Ir Para Rota Do Frontend    /relatorios
    Click    css=.rel-chip:has-text("Comunicação e educação")
    Wait For Elements State    css=.rel-item:has-text("Produção assistencial")    detached
    Wait For Elements State    css=.rel-item:has-text("Campanhas de comunicação")    visible
    Click    css=.rel-item:has-text("Educação em saúde")
    Wait For Elements State    css=.rel-detalhe h2 >> text=Educação em saúde    visible
    Wait For Elements State    css=[aria-label="Indicadores resumidos"] >> text=Ações educativas realizadas    visible
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Relatorios - Gestao Da Rede
    [Documentation]    A categoria Gestão da rede traz a pactuação e a cobertura (ADR-0198).
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Ir Para Rota Do Frontend    /relatorios
    Click    css=.rel-chip:has-text("Gestão da rede")
    Click    css=.rel-item:has-text("Pactuação e cobertura da rede")
    Wait For Elements State    css=.rel-detalhe h2 >> text=Pactuação e cobertura da rede    visible
    Wait For Elements State    css=[aria-label="Indicadores resumidos"] >> text=Oferta pactuada (%)    visible
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
