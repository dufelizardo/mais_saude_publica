*** Settings ***
Library     String
Resource    ../../../../pom/ui/administracao/auditoria/alertas_auditoria_ui_pom.resource
Resource    ../../../auditoria/alerta_auditoria/alerta_auditoria_flow.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
    O alerta de apoio nasce pela API: cinco logins recusados e a detecção na hora. A análise é feita pela tela.
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
UI Alertas - Seed Alerta
    [Documentation]    Cria um alerta de login recusado e devolve o uuid, o CPF e o CPF formatado.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${uuid}    ${cpf}=    Seed Um Alerta De Login Recusado
    ${formatado}=    Evaluate    f"{$cpf[:3]}.{$cpf[3:6]}.{$cpf[6:9]}-{$cpf[9:]}"
    RETURN    ${uuid}    ${cpf}    ${formatado}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Alertas - Analisar Pela Gaveta
    [Documentation]    Abre o alerta, conclui como procedente com parecer e vê o aviso.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${uuid}    ${cpf}    ${formatado}=    UI Alertas - Seed Alerta
    UI Alertas - Abrir Aba
    Clicar Botao    Analisar alerta Login recusado em sequência de ${formatado}
    ${titulo}=    Ler Titulo Da Gaveta
    Should Be Equal    ${titulo}    Login recusado em sequência
    UI Alertas - Escolher Conclusao    Procedente
    Preencher Campo Da Gaveta    parecer    Tentativa de adivinhar a senha; usuário orientado e senha trocada.
    Enviar Gaveta
    ${aviso}=    Ler Aviso De Status
    Should Contain    ${aviso}    Alerta analisado
    ${lido}=    GET Alerta Auditoria By Id    ${uuid}
    Should Be Equal    ${lido.json()}[status]    PROCEDENTE
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Alertas - Gaveta Exige Conclusao E Parecer
    [Documentation]    Envia a análise vazia e vê as mensagens dos campos.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${uuid}    ${cpf}    ${formatado}=    UI Alertas - Seed Alerta
    UI Alertas - Abrir Aba
    Clicar Botao    Analisar alerta Login recusado em sequência de ${formatado}
    Enviar Gaveta
    ${erro}=    Ler Erro Do Campo Da Gaveta    conclusao
    Should Contain    ${erro}    procedente ou improcedente
    ${erroParecer}=    Ler Erro Do Campo Da Gaveta    parecer
    Should Contain    ${erroParecer}    10 caracteres
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Alertas - Ver Eventos Do Alerta
    [Documentation]    "Ver eventos" leva à aba Eventos filtrada pelo CPF de quem foi alertado.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${uuid}    ${cpf}    ${formatado}=    UI Alertas - Seed Alerta
    UI Alertas - Abrir Aba
    Clicar Botao    Analisar alerta Login recusado em sequência de ${formatado}
    Clicar Botao    Ver eventos de ${formatado}
    Wait Until Keyword Succeeds    10s    500ms    UI Alertas - Aba Deve Ser    Eventos
    ${filtro}=    UI Alertas - Ler Filtro De Cpf
    Should Be Equal    ${filtro}    ${formatado}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Alertas - Aba Deve Ser
    [Documentation]    Confere a aba selecionada.
    [Arguments]    ${esperada}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${aba}=    Ler Aba Selecionada
    Should Start With    ${aba}    ${esperada}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Alertas - Contador No Menu
    [Documentation]    Com alerta aberto, o item Auditoria do menu mostra o contador.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    UI Alertas - Seed Alerta
    Ir Para Rota Do Frontend    /administracao/auditoria
    UI Alertas - Contador Do Menu Deve Aparecer
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
