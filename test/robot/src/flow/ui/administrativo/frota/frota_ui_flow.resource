*** Settings ***
Library     Browser
Library     String
Resource    ../../../../pom/common/mais_saude_publica_ui_common.resource
Resource    ../../../transporte/transporte_flow.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
    Jornadas da tela Frota e transporte (ADR-0178).
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
UI Frota - Cadastrar Veiculo E Manutencao
    [Documentation]    Placa inválida, o cadastro, a ficha e a entrada em manutenção.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${ubs}=    Seed A Unidade De Saude Uuid
    ${placa}=    Placa Aleatoria
    Ir Para Rota Do Frontend    /administrativo/frota
    Wait For Elements State    css=h1 >> text=Frota e transporte    visible
    Clicar Botao    Novo veículo
    Preencher Campo Da Gaveta    placaVeiculo    AB1
    Enviar Gaveta
    ${erro}=    Ler Erro Do Campo Da Gaveta    placaVeiculo
    Should Contain    ${erro}    Mercosul
    Preencher Campo Da Gaveta    placaVeiculo    ${placa}
    Preencher Campo Da Gaveta    prefixoVeiculo    VAN-${placa}
    Select Options By    id=f-tipoVeiculo    value    VAN_SANITARIA
    Wait For Elements State    css=#f-unidadeVeiculo option[value="${ubs}"]    attached
    Select Options By    id=f-unidadeVeiculo    value    ${ubs}
    Enviar Gaveta
    ${aviso}=    Ler Aviso De Status
    Should Contain    ${aviso}    Veículo cadastrado
    Wait For Elements State    css=#h-veiculo >> text=VAN-${placa}    visible
    Clicar Botao    Mudar situação
    Preencher Campo Da Gaveta    motivoSituacao    Revisão programada.
    Enviar Gaveta
    Wait For Elements State    css=ul[aria-label="Histórico de situações"] >> text=Revisão programada.    visible
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
