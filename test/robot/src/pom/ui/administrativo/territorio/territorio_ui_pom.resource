*** Settings ***
Resource    ../../../common/mais_saude_publica_ui_common.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
    Tela Território (ADR-0125): segmentos, territórios, áreas e microáreas, a equipe da área e o ACS da microárea.
    Mapa (ADR-0126): os tiles ficam desligados nos testes (msp.mapa.tiles = off); polígonos e pinos são desenhados.
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
UI Territorio - Abrir
    [Documentation]    Abre a tela Território.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Ir Para Rota Do Frontend    /administrativo/territorio
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Territorio - Abrir Area
    [Documentation]    Abre a tela Território já na área.
    [Arguments]    ${uuid}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Ir Para Rota Do Frontend    /administrativo/territorio?area=${uuid}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Territorio - Detalhe Deve Mostrar
    [Documentation]    Confere que o detalhe da área mostra o texto.
    [Arguments]    ${texto}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Wait For Elements State    css=aside[aria-label="Detalhes da área"] >> text=${texto}    visible
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Mapa - Sem Fundo
    [Documentation]    Desliga os tiles do mapa neste navegador (testes sem depender do OpenStreetMap).
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    LocalStorage Set Item    msp.mapa.tiles    off
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Mapa - Caixa Do Poligono
    [Documentation]    Rola até o polígono e devolve a caixa dele na janela (x, y, width, height), pelo nome acessível.
    [Arguments]    ${nome}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Wait For Elements State    css=path[data-nome="${nome}"]    visible
    Evaluate JavaScript    css=path[data-nome="${nome}"]    (el) => el.scrollIntoView({ block: "center" })
    Sleep    500ms
    ${caixa}=    Get BoundingBox    css=path[data-nome="${nome}"]
    RETURN    ${caixa}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Mapa - Desenhar Retangulo
    [Documentation]    Escolhe o retângulo do geoman e clica nos dois cantos (coordenadas da página).
    [Arguments]    ${x1}    ${y1}    ${x2}    ${y2}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Click    css=.button-container[title="Desenhar Retângulo"] a
    Mouse Button    click    ${x1}    ${y1}
    Mouse Button    click    ${x2}    ${y2}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Mapa - Clicar No Centro
    [Documentation]    Clica no centro do mapa visível (marcar ponto).
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Wait For Elements State    css=app-mapa .leaflet-container >> nth=0    visible
    Evaluate JavaScript    css=app-mapa .mapa__alvo >> nth=0    (el) => el.scrollIntoView({ block: "center" })
    Sleep    1s
    ${caixa}=    Get BoundingBox    css=app-mapa .mapa__alvo >> nth=0
    ${x}=    Evaluate    ${caixa}[x] + ${caixa}[width] / 2
    ${y}=    Evaluate    ${caixa}[y] + ${caixa}[height] / 2
    Mouse Button    click    ${x}    ${y}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Mapa - Pino Deve Aparecer
    [Documentation]    Confere o pino (marcador) pelo título.
    [Arguments]    ${nome}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Wait For Elements State    css=.leaflet-marker-icon[title="${nome}"]    visible
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Mapa - Campo Deve Ter Valor
    [Documentation]    Confere que o campo (id f-<campo>) foi preenchido, por exemplo pelo clique no mapa.
    [Arguments]    ${campo}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${valor}=    Get Property    id=${campo}    value
    Should Not Be Empty    ${valor}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
