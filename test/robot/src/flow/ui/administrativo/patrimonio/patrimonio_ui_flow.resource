*** Settings ***
Library     String
Resource    ../../../../pom/ui/administrativo/patrimonio/patrimonio_ui_pom.resource
Resource    ../../../patrimonio/manutencao_flow.resource
Resource    ../../../patrimonio/inventario_flow.resource
Resource    ../../../patrimonio/patrimonio_flow.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
    Jornadas da tela Patrimônio (ADR-0215); as bases vêm pela API.
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
UI Patrimonio - Pelo Menu
    [Documentation]    O item do menu abre a tela.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Ir Para Rota Do Frontend    /sala-de-situacao
    ${expandido}=    Get Element Count    css=a.nav-item[href="/administrativo/patrimonio"]
    IF    ${expandido} == 0
        Click    css=button.nav-group__label >> text=Administrativo
    END
    Click    css=a.nav-item[href="/administrativo/patrimonio"]
    Wait For Elements State    css=h1 >> text=Patrimônio    visible
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Patrimonio - Cadastro
    [Documentation]    Pela tela: cadastra o bem doado, que entra tombado e aparece na lista da unidade.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${setor}    ${codigo}    ${unidade}=    Seed A Setor
    ${matricula}=    Seed A Profissional Matricula
    ${sufixo}=    Generate Random String    6    [UPPER]
    UI Patrimonio - Abrir Tela
    Clicar Botao    Cadastrar bem
    Fill Text    id=f-pt-descricao    Robot UI Mesa ${sufixo}
    Select Options By    id=f-pt-cad-classe    value    MOBILIARIO
    Select Options By    id=f-pt-cad-unidade    value    ${unidade}
    Select Options By    id=f-pt-cad-setor    value    ${setor}
    Fill Text    id=f-pt-responsavel    ${matricula}
    Enviar Gaveta
    Wait For Elements State    css=[role=status] >> text=/Bem cadastrado com o tombamento \\d{4}\\/\\d{6}/    visible
    UI Patrimonio - Filtrar Unidade    ${unidade}
    Wait For Elements State    css=table[aria-label="Bens patrimoniais"] >> text=Robot UI Mesa ${sufixo}    visible
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Patrimonio - Transferencia
    [Documentation]    Pela tela: a ficha envia o bem para outra unidade e a aba Transferências aceita.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${bem}    ${unidade}    ${matricula}=    Seed Bem Patrimonial
    ${setor2}    ${codigo}    ${destino}=    Seed A Setor
    ${r}=    GET Bem Patrimonial    ${bem}
    ${numero}=    Set Variable    ${r.json()}[bem][numeroTombamento]
    ${novo}=    Seed A Profissional Matricula
    UI Patrimonio - Abrir Tela
    UI Patrimonio - Filtrar Unidade    ${unidade}
    Click    css=button[aria-label="Abrir bem ${numero}"]
    Select Options By    id=f-pt-destino    value    ${destino}
    Fill Text    id=f-pt-motivo-envio    Remanejamento pela tela.
    Click    css=app-drawer button >> text=Enviar
    Wait For Elements State    css=app-drawer >> text=/à espera do aceite/    visible
    Click    css=app-drawer button[aria-label="Fechar"]
    Click    role=tab[name=/Transferências/]
    Click    css=button[aria-label="Aceitar ${numero}"]
    Select Options By    id=f-pt-ac-setor    value    ${setor2}
    Fill Text    id=f-pt-ac-responsavel    ${novo}
    Enviar Gaveta
    Wait For Elements State    css=[role=status] >> text=Bem recebido na unidade.    visible
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Patrimonio - Tombamento
    [Documentation]    Pela tela: o bem comprado aparece na aba A tombar e recebe o número.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${sufixo}=    Generate Random String    6    [UPPER]
    Seed Bem A Tombar    Robot UI Autoclave ${sufixo}
    ${matricula}=    Seed A Profissional Matricula
    UI Patrimonio - Abrir Tela
    Click    role=tab[name=/A tombar/]
    Click    css=button[aria-label="Tombar Robot UI Autoclave ${sufixo}"]
    Select Options By    id=f-pt-tb-classe    value    EQUIPAMENTO_MEDICO_HOSPITALAR
    Fill Text    id=f-pt-tb-serie    SN-${sufixo}
    Fill Text    id=f-pt-tb-responsavel    ${matricula}
    Enviar Gaveta
    Wait For Elements State    css=[role=status] >> text=/Bem tombado com o número \\d{4}\\/\\d{6}/    visible
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Patrimonio - Chamado Pela Ficha
    [Documentation]    Pela tela: a ficha do bem abre o chamado.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${bem}    ${unidade}    ${matricula}=    Seed Bem Patrimonial
    ${r}=    GET Bem Patrimonial    ${bem}
    ${numero}=    Set Variable    ${r.json()}[bem][numeroTombamento]
    UI Patrimonio - Abrir Tela
    UI Patrimonio - Filtrar Unidade    ${unidade}
    Click    css=button[aria-label="Abrir bem ${numero}"]
    Fill Text    id=f-mn-defeito    Tela piscando.
    Click    css=app-drawer button >> text=Abrir chamado
    Wait For Elements State    css=app-drawer [role=status] >> text=/Chamado \\d{4}\\/\\d{6} aberto/    visible
    Wait For Elements State    css=[aria-label="Ordens de serviço do bem"] >> text=Corretiva    visible
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Patrimonio - Ordem De Servico
    [Documentation]    Pela tela: a aba Manutenção inicia e conclui a ordem do chamado.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${os}    ${numero}    ${bem}    ${unidade}=    Seed Chamado
    UI Patrimonio - Abrir Tela
    Click    role=tab[name="Manutenção"]
    Click    css=button[aria-label="Abrir ordem ${numero}"]
    Fill Text    id=f-mn-tecnico    Equipe de eletrônica
    Click    css=app-drawer button >> text=Iniciar
    Wait For Elements State    css=app-drawer dl >> text=Em execução    visible
    Fill Text    id=f-mn-servico    Troca do capacitor.
    Fill Text    id=f-mn-mao    50
    Click    css=app-drawer button >> text=Concluir
    Wait For Elements State    css=app-drawer dl >> text=Concluída    visible
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Patrimonio - Inventario
    [Documentation]    Pela tela: abre o inventário com a comissão, confere o bem e fecha.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${bem}    ${unidade}    ${matricula}=    Seed Bem Patrimonial
    ${r}=    GET Bem Patrimonial    ${bem}
    ${numero}=    Set Variable    ${r.json()}[bem][numeroTombamento]
    ${comissao}=    Comissao Inventario
    UI Patrimonio - Abrir Tela
    Click    role=tab[name="Inventários"]
    Clicar Botao    Abrir inventário
    Select Options By    id=f-iv-unidade    value    ${unidade}
    Fill Text    id=f-iv-membro-1    ${comissao}[0]
    Fill Text    id=f-iv-membro-2    ${comissao}[1]
    Fill Text    id=f-iv-membro-3    ${comissao}[2]
    Enviar Gaveta
    Wait For Elements State    css=#drawer-titulo >> text=/Inventário \\d{4}\\/\\d{6}/    visible
    Click    css=button[aria-label="Conferir ${numero}"]
    Select Options By    id=f-iv-conservacao    value    BOM
    Click    css=app-drawer button >> text=Registrar
    Wait For Elements State    css=[aria-label="Bens do inventário"] >> text=/Encontrado · Bom/    visible
    Click    css=app-drawer button >> text=Fechar inventário
    Wait For Elements State    css=app-drawer dl >> text=/Fechado em/    visible
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Patrimonio - Baixa E Painel
    [Documentation]    Pela tela: a ficha baixa o bem e o painel abre.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${bem}    ${unidade}    ${matricula}=    Seed Bem Patrimonial
    ${r}=    GET Bem Patrimonial    ${bem}
    ${numero}=    Set Variable    ${r.json()}[bem][numeroTombamento]
    UI Patrimonio - Abrir Tela
    UI Patrimonio - Filtrar Unidade    ${unidade}
    Click    css=button[aria-label="Abrir bem ${numero}"]
    Select Options By    id=f-pt-bx-motivo    value    INSERVIVEL
    Fill Text    id=f-pt-bx-processo    PA 31/2026
    Click    css=app-drawer button >> text=Pedir baixa
    Wait For Elements State    css=app-drawer dl >> text=/processo PA 31\\/2026/    visible
    Click    css=app-drawer button[aria-label="Fechar"]
    Click    role=tab[name="Painel"]
    Wait For Elements State    css=[aria-label="Resumo do patrimônio"] >> text=bens tombados    visible
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

