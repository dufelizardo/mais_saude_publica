*** Settings ***
Resource    ../../../../pom/ui/assistencia/saude_mental/saude_mental_ui_pom.resource
Resource    ./saude_mental_ui_flow.resource
Resource    ../../../saude_mental/saude_mental_pts_flow.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
    O caso é aberto pela API; a ação sob teste (PTS, intervenção, rede de apoio) é feita pela tela.
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
UI Saude Mental - Elaborar PTS
    [Documentation]    Sem necessidades, a mensagem; com um objetivo e uma intervenção prevista, a versão 1.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${caso}    ${unidade}    ${equipe}    ${matricula}    ${paciente}    ${programa}=    Seed Caso Saude Mental
    ${tipo}=    Id Do Tipo De Intervencao    ATENDIMENTO_INDIVIDUAL
    ${detalhe}=    GET Caso Saude Mental    ${caso}
    ${nomePaciente}=    Set Variable    ${detalhe.json()}[pacienteNome]
    UI Saude Mental - Abrir Caso    ${caso}
    Wait Until Keyword Succeeds    10s    500ms    UI Saude Mental - Titulo Deve Ser    ${nomePaciente}
    Click    role=tab[name=/^PTS/]
    Clicar Botao    Elaborar PTS
    Enviar Gaveta
    ${erro}=    Ler Erro Do Campo Da Gaveta    necessidadesPts
    Should Contain    ${erro}    necessidades
    Preencher Campo Da Gaveta    necessidadesPts    Vínculo com o serviço e adesão ao cuidado.
    Preencher Campo Da Gaveta    objetivoDescricao-0    Vínculo com o CAPS
    UI Saude Mental - Esperar Opcao    objetivoResponsavel-0    ${matricula}
    Escolher Opcao Da Gaveta    objetivoResponsavel-0    ${matricula}
    Clicar Botao    Adicionar intervenção prevista
    Escolher Opcao Da Gaveta    previstaTipo-0    ${tipo}
    Preencher Campo Da Gaveta    previstaFrequencia-0    semanal
    Enviar Gaveta
    ${aviso}=    Ler Aviso De Status
    Should Contain    ${aviso}    PTS elaborado
    Wait For Elements State    css=[aria-label="Objetivos do PTS"] >> text=Vínculo com o CAPS    visible
    ${depois}=    GET Caso Saude Mental    ${caso}
    Should Be Equal As Integers    ${depois.json()}[ptsVersao]    1
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Saude Mental - Revisar PTS
    [Documentation]    A revisão vem preenchida, pede a síntese e gera a versão 2; a 1 fica no histórico.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${caso}    ${unidade}    ${equipe}    ${matricula}    ${paciente}    ${programa}=    Seed Caso Saude Mental
    ${objetivo}=    Create Dictionary    descricao=Vínculo com o CAPS
    ${objetivos}=    Create List    ${objetivo}
    ${payload}=    Create Dictionary    necessidades=Vínculo e adesão.    objetivos=${objetivos}
    ${v1}=    POST PTS Saude Mental    ${caso}    ${payload}
    Should Be Equal As Integers    ${v1.status_code}    201
    ${detalhe}=    GET Caso Saude Mental    ${caso}
    ${nomePaciente}=    Set Variable    ${detalhe.json()}[pacienteNome]
    UI Saude Mental - Abrir Caso    ${caso}
    Wait Until Keyword Succeeds    10s    500ms    UI Saude Mental - Titulo Deve Ser    ${nomePaciente}
    Click    role=tab[name=/^PTS/]
    Clicar Botao    Revisar PTS
    ${valor}=    Get Property    id=f-objetivoDescricao-0    value
    Should Be Equal    ${valor}    Vínculo com o CAPS
    Enviar Gaveta
    ${erro}=    Ler Erro Do Campo Da Gaveta    sinteseRevisao
    Should Contain    ${erro}    síntese
    Escolher Opcao Da Gaveta    objetivoSituacao-0    ALCANCADO
    Preencher Campo Da Gaveta    sinteseRevisao    Vínculo alcançado; segue o cuidado.
    Enviar Gaveta
    ${aviso}=    Ler Aviso De Status
    Should Contain    ${aviso}    PTS revisado
    Wait For Elements State    css=[aria-label="Versões anteriores do PTS"] >> text=Versão 1    visible
    ${depois}=    GET Caso Saude Mental    ${caso}
    Should Be Equal As Integers    ${depois.json()}[ptsVersao]    2
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Saude Mental - Registrar Intervencao
    [Documentation]    Sem resumo, a mensagem; com ele, a intervenção aparece na aba.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${caso}    ${unidade}    ${equipe}    ${matricula}    ${paciente}    ${programa}=    Seed Caso Saude Mental
    ${tipo}=    Id Do Tipo De Intervencao    ATENDIMENTO_FAMILIAR
    ${detalhe}=    GET Caso Saude Mental    ${caso}
    ${nomePaciente}=    Set Variable    ${detalhe.json()}[pacienteNome]
    UI Saude Mental - Abrir Caso    ${caso}
    Wait Until Keyword Succeeds    10s    500ms    UI Saude Mental - Titulo Deve Ser    ${nomePaciente}
    Clicar Botao    Registrar intervenção
    Escolher Opcao Da Gaveta    tipoIntervencao    ${tipo}
    Enviar Gaveta
    ${erro}=    Ler Erro Do Campo Da Gaveta    resumoIntervencao
    Should Contain    ${erro}    resumo
    Preencher Campo Da Gaveta    resumoIntervencao    Conversa com a mãe sobre a rotina.
    Enviar Gaveta
    ${aviso}=    Ler Aviso De Status
    Should Contain    ${aviso}    Intervenção registrada
    Wait For Elements State    css=[aria-label="Intervenções do caso"] >> text=Atendimento familiar    visible
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Saude Mental - Rede De Apoio
    [Documentation]    Inclui uma pessoa e a tira da rede, com motivo; ela vai para o histórico.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${caso}    ${unidade}    ${equipe}    ${matricula}    ${paciente}    ${programa}=    Seed Caso Saude Mental
    ${detalhe}=    GET Caso Saude Mental    ${caso}
    ${nomePaciente}=    Set Variable    ${detalhe.json()}[pacienteNome]
    UI Saude Mental - Abrir Caso    ${caso}
    Wait Until Keyword Succeeds    10s    500ms    UI Saude Mental - Titulo Deve Ser    ${nomePaciente}
    Click    role=tab[name=/^Rede de apoio/]
    Clicar Botao    Incluir pessoa
    Preencher Campo Da Gaveta    nomeRede    Ana Souza
    Escolher Opcao Da Gaveta    papelRede    FAMILIAR
    Preencher Campo Da Gaveta    vinculoRede    irmã
    Enviar Gaveta
    ${aviso}=    Ler Aviso De Status
    Should Contain    ${aviso}    Incluído na rede de apoio
    Wait For Elements State    css=[aria-label="Rede de apoio"] >> text=Ana Souza    visible
    Clicar Botao    Tirar da rede: Ana Souza
    Preencher Campo Da Gaveta    motivoSaidaRede    Mudou de cidade.
    Enviar Gaveta
    Wait For Elements State    css=[aria-label="Rede de apoio anterior"] >> text=Ana Souza · Familiar    visible
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
