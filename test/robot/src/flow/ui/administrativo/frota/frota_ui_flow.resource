*** Settings ***
Library     Browser
Library     String
Resource    ../../../../pom/common/mais_saude_publica_ui_common.resource
Resource    ../../../transporte/transporte_flow.resource
Resource    ../../../transporte/transporte_eletivo_flow.resource
Resource    ../../../transporte/frota_emergencia_flow.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
    Jornadas da tela Frota e transporte (ADR-0178) e do transporte eletivo (ADR-0190).
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

UI Frota - Pedir E Cancelar Transporte
    [Documentation]    Pede o transporte de uma consulta pela tela (sem paciente, a validação) e cancela com motivo.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${ubs}=    Seed A Unidade De Saude Uuid
    ${paciente}    ${cpf}    ${cns}    ${nome}=    Seed A Paciente
    Ir Para Rota Do Frontend    /administrativo/frota
    Wait For Elements State    css=h1 >> text=Frota e transporte    visible
    Clicar Aba    Solicitações
    Clicar Botao    Pedir transporte
    Enviar Gaveta
    ${erro}=    Ler Erro Do Campo Da Gaveta    pacienteTransporte
    Should Contain    ${erro}    paciente
    Fill Text    id=f-buscaPacienteTransporte    ${cpf}
    Wait For Elements State    css=#f-pacienteTransporte option[value="${paciente}"]    attached
    Escolher Opcao Da Gaveta    pacienteTransporte    ${paciente}
    Wait For Elements State    css=#f-unidadeTransporte option[value="${ubs}"]    attached
    Escolher Opcao Da Gaveta    unidadeTransporte    ${ubs}
    Preencher Campo Da Gaveta    destinoTransporte    Policlínica Robot
    Preencher Campo Da Gaveta    embarqueTransporte    Rua do Robot, 10
    Enviar Gaveta
    ${aviso}=    Ler Aviso De Status
    Should Contain    ${aviso}    Transporte pedido
    Wait For Elements State    css=#pn-solicitacoes tr >> text=${nome}    visible
    Click    css=button[aria-label="Cancelar o transporte de ${nome}"]
    Preencher Campo Da Gaveta    motivoTransporte    Paciente remarcou a consulta.
    Enviar Gaveta
    ${aviso}=    Ler Aviso De Status
    Should Contain    ${aviso}    Transporte cancelado
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Frota - Programar Viagem Ate A Chegada
    [Documentation]    Programa a viagem com o paciente da fila, registra a saída, o embarque e a chegada com a quilometragem.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${garagem}=    Seed A Unidade De Saude Uuid
    ${ubs}=    Seed A Unidade De Saude Uuid
    ${placa}=    Placa Aleatoria
    ${dv}=    Create Dictionary    placa=${placa}    prefixo=VAN-${placa}    tipo=VAN_SANITARIA    unidadeId=${garagem}    lugares=${6}    quilometragem=${2000}
    ${rv}=    POST Veiculo    ${dv}
    Should Be Equal As Integers    ${rv.status_code}    201
    ${condutor}=    Seed A Profissional Matricula
    ${paciente}    ${cpf}    ${cns}    ${nome}=    Seed A Paciente
    ${sol}=    Solicitacao Criada    ${paciente}    ${ubs}
    Ir Para Rota Do Frontend    /administrativo/frota
    Wait For Elements State    css=h1 >> text=Frota e transporte    visible
    Clicar Botao    Programar viagem
    Wait For Elements State    css=#f-veiculoViagem option[value="${rv.json()}[uuid]"]    attached
    Escolher Opcao Da Gaveta    veiculoViagem    ${rv.json()}[uuid]
    Wait For Elements State    css=#f-condutorViagem option[value="${condutor}"]    attached
    Escolher Opcao Da Gaveta    condutorViagem    ${condutor}
    Check Checkbox    id=f-sol${sol}
    Enviar Gaveta
    ${aviso}=    Ler Aviso De Status
    Should Contain    ${aviso}    Viagem programada
    Clicar Aba    Viagens
    Click    css=button[aria-label="Registrar a saída de VAN-${placa}"]
    Enviar Gaveta
    ${aviso}=    Ler Aviso De Status
    Should Contain    ${aviso}    Saída registrada
    Click    css=button[aria-label="Registrar quem embarcou na viagem de VAN-${placa}"]
    Enviar Gaveta
    ${aviso}=    Ler Aviso De Status
    Should Contain    ${aviso}    Embarque registrado
    Click    css=button[aria-label="Registrar a chegada de VAN-${placa}"]
    Preencher Campo Da Gaveta    kmViagem    2045
    Enviar Gaveta
    ${aviso}=    Ler Aviso De Status
    Should Contain    ${aviso}    45 km
    ${s}=    Situacao Da Solicitacao    ${sol}
    Should Be Equal    ${s}    REALIZADA
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Frota - Ceder E Devolver A Emergencia
    [Documentation]    O pedido de veículo da emergência aparece na aba Frota; a frota cede a van, que fica em serviço, e devolve com a quilometragem (ADR-0192).
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${garagem}=    Seed A Unidade De Saude Uuid
    ${destino}=    Seed A Unidade De Saude Uuid
    ${van}    ${prefixo}=    Seed Viatura    VAN_SANITARIA    ${garagem}
    ${em}=    Seed Emergencia
    ${sufixo}=    Generate Random String    5    [UPPER][NUMBERS]
    ${recurso}=    Set Variable    Van para remoções ${sufixo}
    ${pedido}=    Pedido De Veiculo    ${em.json()}[uuid]    ${destino}    ${recurso}    ${1}
    Ir Para Rota Do Frontend    /administrativo/frota
    Wait For Elements State    css=h1 >> text=Frota e transporte    visible
    Click    id=tab-frota
    Wait For Elements State    css=ul[aria-label="Pedidos de veículo das emergências"] >> text=${recurso}    visible
    Clicar Botao    Ceder veículo: ${recurso}
    Wait For Elements State    css=#f-veiculoCessao option[value="${van}"]    attached
    Select Options By    id=f-veiculoCessao    value    ${van}
    Enviar Gaveta
    ${aviso}=    Ler Aviso De Status
    Should Contain    ${aviso}    Veículo cedido
    ${s}=    Situacao Do Veiculo    ${van}
    Should Be Equal    ${s}    EM_SERVICO
    Clicar Botao    Devolver ${prefixo}
    Preencher Campo Da Gaveta    kmCessao    120
    Enviar Gaveta
    ${aviso}=    Ler Aviso De Status
    Should Contain    ${aviso}    Veículo devolvido
    ${s}=    Situacao Do Veiculo    ${van}
    Should Be Equal    ${s}    DISPONIVEL
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
