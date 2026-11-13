*** Settings ***
Library     Browser
Library     String
Resource    ../../../../pom/common/mais_saude_publica_ui_common.resource
Resource    ../../../rede/rede_flow.resource
Resource    ../../../rede/rede_tematica_flow.resource
Resource    ../../assistencia/regulacao/regulacao_ui_flow.resource
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

UI Ppi - Redes Tematicas
    [Documentation]    A aba Redes temáticas mostra os componentes da rede escolhida e o link para a RAPS (ADR-0197).
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Ir Para Rota Do Frontend    /administrativo/ppi
    Click    css=#tab-redes
    Wait For Elements State    css=#pn-redes h3 >> text=SAMU 192 e central de regulação    visible
    Select Options By    css=[aria-label="Rede temática"]    value    CEGONHA_ALYNE
    Wait For Elements State    css=#pn-redes h3 >> text=Parto e nascimento    visible
    Wait For Elements State    css=#pn-redes a >> text=Rede de Atenção Psicossocial (RAPS)    visible
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Ppi - Nova Referencia
    [Documentation]    Pela aba Referências: sem procedimento, a mensagem; com origem e executante, a referência vigente na lista (ADR-0197).
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${procedimento}    ${nomeProcedimento}=    Seed A Procedimento Regulado
    ${origem}=    Seed A Unidade De Saude Uuid
    ${executante}=    Seed A Unidade De Saude Uuid
    Ir Para Rota Do Frontend    /administrativo/ppi
    Click    css=#tab-referencias
    Clicar Botao    Nova referência
    Enviar Gaveta
    ${erro}=    Ler Erro Do Campo Da Gaveta    procedimentoReferencia
    Should Contain    ${erro}    procedimento
    Wait For Elements State    css=#f-procedimentoReferencia option[value="${procedimento}"]    attached
    Select Options By    id=f-procedimentoReferencia    value    ${procedimento}
    Select Options By    id=f-origemReferencia    value    ${origem}
    Select Options By    id=f-executanteReferencia    value    ${executante}
    Enviar Gaveta
    ${aviso}=    Ler Aviso De Status
    Should Contain    ${aviso}    Referência registrada
    Wait For Elements State    css=table[aria-label="Referências"] tr:has-text("${nomeProcedimento}") >> text=Vigente    visible
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Ppi - Regulacao Sugere A Referencia
    [Documentation]    Na gaveta da Regulação, a executante da referência vem escolhida, com o rótulo (ADR-0197).
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${uuid}    ${matricula}    ${procedimentoId}    ${pacienteId}    ${unidadeId}=    Seed A Solicitacao Regulacao    VERMELHO
    ${executante}=    Seed A Unidade De Saude Uuid
    ${payload}=    Create Dictionary    origemId=${unidadeId}    procedimentoId=${procedimentoId}    executanteId=${executante}
    ${r}=    POST Referencia Rede    ${payload}
    Should Be Equal As Integers    ${r.status_code}    201
    ${lida}=    GET Get Solicitacao Regulacao By Id    ${uuid}
    UI Regulacao - Abrir Aba    fila
    Clicar Botao    Analisar solicitação de ${lida.json()}[pacienteNome]
    Esperar Texto Aparecer    Decisão da regulação
    Wait For Elements State    id=h-referenciaRede    visible
    Get Selected Options    id=f-unidadeExecutanteId    value    ==    ${executante}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Ppi - Cobertura
    [Documentation]    A aba Cobertura mostra os indicadores da competência e as origens do procedimento ofertado (ADR-0198).
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${oferta}    ${procedimento}    ${executante}    ${mes}=    Seed Oferta    ${8}
    ${ubs}=    Seed A Unidade De Saude Uuid
    ${cota}=    Create Dictionary    beneficiariaId=${ubs}    quantidade=${3}
    ${r}=    POST Cota Ppi    ${oferta}    ${cota}
    Should Be Equal As Integers    ${r.status_code}    201
    ${lista}=    GET List Procedimento Regulado
    ${nome}=    Evaluate    [p['nome'] for p in $lista.json() if p['uuid'] == $procedimento][0]
    Ir Para Rota Do Frontend    /administrativo/ppi
    Click    css=#tab-cobertura
    Fill Text    css=input[aria-label="Competência"]    ${mes}
    Evaluate JavaScript    css=input[aria-label="Competência"]    (e) => e.dispatchEvent(new Event("change", { bubbles: true }))
    Wait For Elements State    css=[aria-label="Indicadores de pactuação"] >> text=Oferta pactuada    visible
    Click    css=button[aria-label="Origens de ${nome}"]
    Wait For Elements State    css=table[aria-label="Origens de ${nome}"] tbody tr    visible
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
