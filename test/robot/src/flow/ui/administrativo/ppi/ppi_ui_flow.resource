*** Settings ***
Library     Browser
Library     String
Resource    ../../../../pom/common/mais_saude_publica_ui_common.resource
Resource    ../../../rede/rede_flow.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
    Jornadas da tela Rede e pactuação (ADR-0179).
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
UI Ppi - Oferta E Cota
    [Documentation]    Cadastra a oferta do mês seguinte pela tela (procedimento obrigatório) e pactua uma cota: acima do que sobra, a
    ...    mensagem; dentro, a cota aparece na oferta.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${procedimento}    ${nomeProcedimento}=    Seed A Procedimento Regulado
    ${executante}=    Seed A Unidade De Saude Uuid
    ${ubs}=    Seed A Unidade De Saude Uuid
    ${mes}=    Proxima Competencia
    Ir Para Rota Do Frontend    /administrativo/ppi
    Wait For Elements State    css=h1 >> text=Rede e pactuação (PPI)    visible
    Fill Text    css=input[aria-label="Competência"]    ${mes}
    Evaluate JavaScript    css=input[aria-label="Competência"]    (e) => e.dispatchEvent(new Event("change", { bubbles: true }))
    Clicar Botao    Nova oferta
    Enviar Gaveta
    ${erro}=    Ler Erro Do Campo Da Gaveta    procedimentoOferta
    Should Contain    ${erro}    procedimento
    Wait For Elements State    css=#f-procedimentoOferta option[value="${procedimento}"]    attached
    Select Options By    id=f-procedimentoOferta    value    ${procedimento}
    Wait For Elements State    css=#f-executanteOferta option[value="${executante}"]    attached
    Select Options By    id=f-executanteOferta    value    ${executante}
    Preencher Campo Da Gaveta    quantidadeOferta    6
    Enviar Gaveta
    ${aviso}=    Ler Aviso De Status
    Should Contain    ${aviso}    Oferta cadastrada
    Click    css=button[aria-label^="Pactuar cota de ${nomeProcedimento} em"]
    Wait For Elements State    css=#f-beneficiariaCota option[value="${ubs}"]    attached
    Select Options By    id=f-beneficiariaCota    value    ${ubs}
    Preencher Campo Da Gaveta    quantidadeCota    9
    Enviar Gaveta
    ${erro}=    Ler Erro Do Campo Da Gaveta    quantidadeCota
    Should Contain    ${erro}    6
    Preencher Campo Da Gaveta    quantidadeCota    4
    Enviar Gaveta
    Wait For Elements State    css=table[aria-label^="Cotas de ${nomeProcedimento} em"]    visible
    Get Text    css=table[aria-label^="Cotas de ${nomeProcedimento} em"] tbody td.mono >> nth=0    ==    4
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
