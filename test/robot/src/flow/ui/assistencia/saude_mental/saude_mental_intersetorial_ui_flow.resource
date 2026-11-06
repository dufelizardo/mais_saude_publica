*** Settings ***
Library     String
Resource    ../../../../pom/ui/assistencia/saude_mental/saude_mental_ui_pom.resource
Resource    saude_mental_ui_flow.resource
Resource    ../../../intersetorialidade/intersetorialidade_flow.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
    O caso e a instituição vêm pela API; o encaminhamento e o retorno são feitos pela tela (ADR-0155).
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
UI Saude Mental - Encaminhamento Intersetorial
    [Documentation]    Na aba Rede de apoio: sem instituição, a mensagem; com os dados, o encaminhamento aparece; o retorno conclui.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${caso}    ${unidade}    ${equipe}    ${matricula}    ${paciente}    ${programa}=    Seed Caso Saude Mental
    ${instituicao}=    Seed Instituicao Externa    CRAS Tela
    ${detalhe}=    GET Caso Saude Mental    ${caso}
    UI Saude Mental - Abrir Caso    ${caso}
    Wait Until Keyword Succeeds    10s    500ms    UI Saude Mental - Titulo Deve Ser    ${detalhe.json()}[pacienteNome]
    Click    role=tab[name=/^Rede de apoio/]
    Clicar Botao    Encaminhar a outro setor
    Enviar Gaveta
    ${erro}=    Ler Erro Do Campo Da Gaveta    instituicaoIntersetorial
    Should Contain    ${erro}    instituição
    UI Saude Mental - Esperar Opcao    instituicaoIntersetorial    ${instituicao}
    Escolher Opcao Da Gaveta    instituicaoIntersetorial    ${instituicao}
    Fill Text    id=f-motivoIntersetorial    Inclusão no CadÚnico e benefício eventual
    Enviar Gaveta
    Wait For Elements State    css=[aria-label="Encaminhamentos intersetoriais"] >> text=Enviado    visible
    Click    css=[aria-label="Encaminhamentos intersetoriais"] >> role=button[name^="Retorno de"]
    Escolher Opcao Da Gaveta    desfechoIntersetorial    CONCLUIDO
    Fill Text    id=f-textoIntersetorial    Família inscrita; benefício concedido.
    Enviar Gaveta
    Wait For Elements State    css=[aria-label="Encaminhamentos intersetoriais"] >> text=Concluído    visible
    Wait For Elements State    css=[aria-label="Encaminhamentos intersetoriais"] >> text=/benefício concedido/    visible
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
