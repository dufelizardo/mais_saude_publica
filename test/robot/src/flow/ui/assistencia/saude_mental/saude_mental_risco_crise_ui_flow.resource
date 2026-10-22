*** Settings ***
Library     DateTime
Resource    ../../../../pom/ui/assistencia/saude_mental/saude_mental_ui_pom.resource
Resource    ./saude_mental_ui_flow.resource
Resource    ../../../saude_mental/saude_mental_risco_crise_flow.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
    O caso é aberto pela API; a ação sob teste (risco, crise, notificação) é feita pela tela.
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
UI Saude Mental - Avaliar Risco
    [Documentation]    Sem medidas no risco alto, a mensagem; com elas e a reavaliação, o risco vigente.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${caso}    ${unidade}    ${equipe}    ${matricula}    ${paciente}    ${programa}=    Seed Caso Saude Mental
    ${detalhe}=    GET Caso Saude Mental    ${caso}
    ${nomePaciente}=    Set Variable    ${detalhe.json()}[pacienteNome]
    UI Saude Mental - Abrir Caso    ${caso}
    Wait Until Keyword Succeeds    10s    500ms    UI Saude Mental - Titulo Deve Ser    ${nomePaciente}
    ${reavaliar}=    Get Current Date    increment=7 days    result_format=%Y-%m-%d
    Click    role=tab[name=/^Risco/]
    Clicar Botao    Avaliar risco
    Escolher Opcao Da Gaveta    tipoRisco    AUTOAGRESSAO
    Escolher Opcao Da Gaveta    nivelRisco    ALTO
    Enviar Gaveta
    ${erro}=    Ler Erro Do Campo Da Gaveta    medidasRisco
    Should Contain    ${erro}    medidas
    Preencher Campo Da Gaveta    medidasRisco    Plano de segurança e contato diário.
    Fill Text    id=f-reavaliarEm    ${reavaliar}
    Enviar Gaveta
    ${aviso}=    Ler Aviso De Status
    Should Contain    ${aviso}    Risco avaliado
    Wait For Elements State    css=[aria-label="Risco vigente"] >> text=Autoagressão    visible
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Saude Mental - Registrar Crise E Encerrar
    [Documentation]    Registra a crise no caso, o pós-crise e encerra com o desfecho.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${caso}    ${unidade}    ${equipe}    ${matricula}    ${paciente}    ${programa}=    Seed Caso Saude Mental
    ${detalhe}=    GET Caso Saude Mental    ${caso}
    ${nomePaciente}=    Set Variable    ${detalhe.json()}[pacienteNome]
    UI Saude Mental - Abrir Caso    ${caso}
    Wait Until Keyword Succeeds    10s    500ms    UI Saude Mental - Titulo Deve Ser    ${nomePaciente}
    Click    css=section[aria-labelledby="caso-titulo"] >> role=button[name="Registrar crise"]
    Escolher Opcao Da Gaveta    tipoCrise    AGITACAO_PSICOMOTORA
    Preencher Campo Da Gaveta    descricaoCrise    Agitação na recepção.
    Enviar Gaveta
    ${aviso}=    Ler Aviso De Status
    Should Contain    ${aviso}    Crise registrada
    Wait For Elements State    css=[aria-label="Etapas da crise"] >> text=Identificação    visible
    Escolher Opcao Da Gaveta    etapaCrise    POS_CRISE
    Preencher Campo Da Gaveta    descricaoEtapa    Retorno ao CAPS em 24 horas.
    Clicar Botao    Registrar etapa
    Wait For Elements State    css=[aria-label="Etapas da crise"] >> text=Pós-crise    visible
    Escolher Opcao Da Gaveta    desfechoCrise    RETORNO_AO_DOMICILIO
    Clicar Botao    Encerrar a crise
    Wait For Elements State    css=app-drawer >> text=Com caso em acompanhamento    visible
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Saude Mental - Notificacao Da Crise
    [Documentation]    A crise de autoagressão aparece no painel; a notificação é registrada pela gaveta.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${caso}    ${unidade}    ${equipe}    ${matricula}    ${paciente}    ${programa}=    Seed Caso Saude Mental
    ${payload}=    Create Dictionary    pacienteId=${paciente}    unidadeId=${unidade}    tipo=TENTATIVA_DE_SUICIDIO    descricao=Ingestão de medicamentos.
    ${r}=    POST Crise Saude Mental    ${payload}
    Should Be Equal As Integers    ${r.status_code}    201
    ${crise}=    Set Variable    ${r.json()}[uuid]
    UI Saude Mental - Abrir
    ${detalhe}=    GET Caso Saude Mental    ${caso}
    Clicar Botao    Abrir crise: ${detalhe.json()}[pacienteNome]
    Wait For Elements State    css=app-drawer >> text=Notificação compulsória    visible
    Preencher Campo Da Gaveta    numeroNotificacao    SINAN-123
    Clicar Botao    Registrar notificação
    Wait For Elements State    css=app-drawer >> text=nº SINAN-123    visible
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
