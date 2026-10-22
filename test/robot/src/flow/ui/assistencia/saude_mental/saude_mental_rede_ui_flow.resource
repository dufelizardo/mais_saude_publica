*** Settings ***
Resource    ../../../../pom/ui/assistencia/saude_mental/saude_mental_ui_pom.resource
Resource    ./saude_mental_ui_flow.resource
Resource    ../../../saude_mental/saude_mental_rede_flow.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
    A base é criada pela API; a ação sob teste (encaminhar, compartilhar, transferir, aceitar) é feita pela tela.
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
UI Saude Mental - Encaminhar Pela Regulacao
    [Documentation]    Sem procedimento, a mensagem; com os dados, a solicitação aparece no caso, na fila.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${caso}    ${unidade}    ${equipe}    ${matricula}    ${paciente}    ${programa}=    Seed Caso Saude Mental
    ${procedimento}    ${nomeProcedimento}=    Seed A Procedimento Regulado
    ${detalhe}=    GET Caso Saude Mental    ${caso}
    ${nomePaciente}=    Set Variable    ${detalhe.json()}[pacienteNome]
    UI Saude Mental - Abrir Caso    ${caso}
    Wait Until Keyword Succeeds    10s    500ms    UI Saude Mental - Titulo Deve Ser    ${nomePaciente}
    Click    role=tab[name=/^Rede de atenção/]
    Clicar Botao    Encaminhar
    Enviar Gaveta
    ${erro}=    Ler Erro Do Campo Da Gaveta    procedimentoEncaminhamento
    Should Contain    ${erro}    procedimento
    UI Saude Mental - Esperar Opcao    procedimentoEncaminhamento    ${procedimento}
    Escolher Opcao Da Gaveta    procedimentoEncaminhamento    ${procedimento}
    Preencher Campo Da Gaveta    cidEncaminhamento    F32.1
    Escolher Opcao Da Gaveta    prioridadeEncaminhamento    VERDE
    Preencher Campo Da Gaveta    justificativaEncaminhamento    Episódio depressivo sem resposta ao manejo.
    Preencher Campo Da Gaveta    solicitanteEncaminhamento    ${matricula}
    Enviar Gaveta
    ${aviso}=    Ler Aviso De Status
    Should Contain    ${aviso}    Encaminhamento registrado
    Wait For Elements State    css=[aria-label="Encaminhamentos do caso"] >> text=${nomeProcedimento}    visible
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Saude Mental - Compartilhar O Cuidado
    [Documentation]    Compartilha com outra equipe e encerra, com motivo.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${caso}    ${unidade}    ${equipe}    ${matricula}    ${paciente}    ${programa}=    Seed Caso Saude Mental
    ${parceira}    ${nomeParceira}    ${unidadeParceira}=    Seed Uma Equipe
    ${detalhe}=    GET Caso Saude Mental    ${caso}
    ${nomePaciente}=    Set Variable    ${detalhe.json()}[pacienteNome]
    UI Saude Mental - Abrir Caso    ${caso}
    Wait Until Keyword Succeeds    10s    500ms    UI Saude Mental - Titulo Deve Ser    ${nomePaciente}
    Click    role=tab[name=/^Rede de atenção/]
    Clicar Botao    Compartilhar o cuidado
    UI Saude Mental - Esperar Opcao    equipeParceira    ${parceira}
    Escolher Opcao Da Gaveta    equipeParceira    ${parceira}
    Preencher Campo Da Gaveta    objetivoCompartilhamento    Acompanhar a adesão na eSF.
    Enviar Gaveta
    ${aviso}=    Ler Aviso De Status
    Should Contain    ${aviso}    Cuidado compartilhado
    Wait For Elements State    css=[aria-label="Cuidado compartilhado"] >> text=Vigente    visible
    Clicar Botao    Encerrar compartilhamento: ${nomeParceira}
    Preencher Campo Da Gaveta    motivoRede    Estável.
    Enviar Gaveta
    Wait For Elements State    css=[aria-label="Cuidado compartilhado"] >> text=Encerrado    visible
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Saude Mental - Propor Transferencia
    [Documentation]    Sem destino, a mensagem; com o resumo, a transferência aguarda o destino.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${caso}    ${unidade}    ${equipe}    ${matricula}    ${paciente}    ${programa}=    Seed Caso Saude Mental
    ${destino}    ${equipeDestino}    ${matriculaDestino}=    Seed Destino De Transferencia
    ${detalhe}=    GET Caso Saude Mental    ${caso}
    ${nomePaciente}=    Set Variable    ${detalhe.json()}[pacienteNome]
    UI Saude Mental - Abrir Caso    ${caso}
    Wait Until Keyword Succeeds    10s    500ms    UI Saude Mental - Titulo Deve Ser    ${nomePaciente}
    Click    role=tab[name=/^Rede de atenção/]
    Clicar Botao    Transferir o cuidado
    Enviar Gaveta
    ${erro}=    Ler Erro Do Campo Da Gaveta    unidadeDestino
    Should Contain    ${erro}    destino
    UI Saude Mental - Esperar Opcao    unidadeDestino    ${destino}
    Escolher Opcao Da Gaveta    unidadeDestino    ${destino}
    Preencher Campo Da Gaveta    resumoTransferencia    Estável há seis meses; segue na eSF.
    Enviar Gaveta
    ${aviso}=    Ler Aviso De Status
    Should Contain    ${aviso}    Transferência proposta
    Wait For Elements State    css=[aria-label="Transferências do caso"] >> text=Aguardando o destino    visible
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Saude Mental - Aceitar Transferencia
    [Documentation]    O destino aceita pelo painel: o caso abre lá, ligado ao anterior.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${caso}    ${unidade}    ${equipe}    ${matricula}    ${paciente}    ${programa}=    Seed Caso Saude Mental
    ${destino}    ${equipeDestino}    ${matriculaDestino}=    Seed Destino De Transferencia
    ${payload}=    Create Dictionary    unidadeDestinoId=${destino}    resumo=Retorno à UBS.
    ${r}=    POST Transferencia Saude Mental    ${caso}    ${payload}
    Should Be Equal As Integers    ${r.status_code}    201
    ${nomePaciente}=    Set Variable    ${r.json()}[pacienteNome]
    UI Saude Mental - Abrir
    Clicar Botao    Aceitar transferência: ${nomePaciente}
    UI Saude Mental - Esperar Opcao    equipeAceite    ${equipeDestino}
    Escolher Opcao Da Gaveta    equipeAceite    ${equipeDestino}
    UI Saude Mental - Esperar Opcao    tecnicoAceite    ${matriculaDestino}
    Escolher Opcao Da Gaveta    tecnicoAceite    ${matriculaDestino}
    Enviar Gaveta
    ${aviso}=    Ler Aviso De Status
    Should Contain    ${aviso}    Transferência aceita
    ${origem}=    GET Caso Saude Mental    ${caso}
    Should Be Equal    ${origem.json()}[motivoSaida]    TRANSFERENCIA
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
