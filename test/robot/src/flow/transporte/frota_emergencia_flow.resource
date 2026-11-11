*** Settings ***
Library     String
Library     Collections
Resource    ../../pom/transporte/frota_emergencia_pom.resource
Resource    ../../pom/transporte/transporte_pom.resource
Resource    ../../pom/emergencias/coordenacao_pom.resource
Resource    ../../pom/emergencias/desmobilizacao_pom.resource
Resource    ../emergencias/emergencias_flow.resource
Resource    ./transporte_flow.resource
Resource    ../setor/create_setor/create_setor_flow.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
    Veículos nas emergências (ADR-0192): a coordenação pede pela mobilização e a frota cede; cedido, o veículo fica em serviço; a
    devolução volta a disponível com a quilometragem; concluir o pedido e a desmobilização devolvem o que restou.
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
Pedido De Veiculo
    [Documentation]    A vehicle mobilization of the emergency; returns its uuid.
    [Arguments]    ${emergencia}    ${destino}    ${recurso}    ${quantidade}=${None}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${p}=    Create Dictionary    tipo=VEICULO    destinoId=${destino}    recurso=${recurso}
    IF    $quantidade is not None    Set To Dictionary    ${p}    quantidade=${quantidade}
    ${m}=    POST Mobilizacao    ${emergencia}    ${p}
    Should Be Equal As Integers    ${m.status_code}    201
    ${id}=    Evaluate    [x['uuid'] for x in $m.json()['mobilizacoes'] if x['recurso'] == $recurso][0]
    RETURN    ${id}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Ceder
    [Documentation]    The fleet cedes the vehicle to the request; returns the response.
    [Arguments]    ${mobilizacao}    ${veiculo}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${p}=    Create Dictionary    veiculoId=${veiculo}
    ${r}=    POST Frota Emergencia    mobilizacao/${mobilizacao}/cessao    ${p}
    RETURN    ${r}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Situacao Do Veiculo
    [Documentation]    The current situation of the vehicle.
    [Arguments]    ${veiculo}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${v}=    GET Veiculo    ${veiculo}
    RETURN    ${v.json()}[situacao]
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Frota Emergencia - Ceder E Devolver
    [Documentation]    Ceded, the van is in service and the request moves; a negative mileage is 400; returned, it is available again and returning twice is 422.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${garagem}=    Seed A Unidade De Saude Uuid
    ${destino}=    Seed A Unidade De Saude Uuid
    ${van}    ${prefixo}=    Seed Viatura    VAN_SANITARIA    ${garagem}
    ${em}=    Seed Emergencia
    ${emergencia}=    Set Variable    ${em.json()}[uuid]
    ${pedido}=    Pedido De Veiculo    ${emergencia}    ${destino}    Van Robot    ${1}
    ${lista}=    GET Frota Emergencia    pedidos
    ${ids}=    Evaluate    [p['mobilizacaoId'] for p in $lista.json()]
    List Should Contain Value    ${ids}    ${pedido}
    ${r}=    Ceder    ${pedido}    ${van}
    Should Be Equal As Integers    ${r.status_code}    201
    ${cessao}=    Set Variable    ${r.json()}[cessao][uuid]
    ${s}=    Situacao Do Veiculo    ${van}
    Should Be Equal    ${s}    EM_SERVICO
    ${d}=    GET Emergencia    ${emergencia}
    ${linha}=    Evaluate    [x for x in $d.json()['mobilizacoes'] if x['uuid'] == $pedido][0]
    Should Be Equal    ${linha}[situacao]    EM_ANDAMENTO
    ${volta}=    Create Dictionary    quilometragem=${-1}
    ${x}=    POST Frota Emergencia    cessao/${cessao}/devolucao    ${volta}
    Should Be Equal As Integers    ${x.status_code}    400
    ${ok}=    Create Dictionary    quilometragem=${150}
    ${x}=    POST Frota Emergencia    cessao/${cessao}/devolucao    ${ok}
    Should Be Equal As Integers    ${x.status_code}    200
    ${s}=    Situacao Do Veiculo    ${van}
    Should Be Equal    ${s}    DISPONIVEL
    ${menos}=    Create Dictionary    quilometragem=${100}
    ${x}=    POST Frota Emergencia    cessao/${cessao}/devolucao    ${menos}
    Should Be Equal As Integers    ${x.status_code}    422
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Frota Emergencia - Limites E Desmobilizacao
    [Documentation]    The vehicle already ceded is 409; one in maintenance is 422; beyond the quantity is 422; the demobilization returns it.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${garagem}=    Seed A Unidade De Saude Uuid
    ${destino}=    Seed A Unidade De Saude Uuid
    ${amb}    ${p1}=    Seed Viatura    AMBULANCIA_TRANSPORTE    ${garagem}
    ${carro}    ${p2}=    Seed Viatura    CARRO_ADMINISTRATIVO    ${garagem}
    ${oficina}    ${p3}=    Seed Viatura    VAN_SANITARIA    ${garagem}
    ${m}=    Create Dictionary    situacao=MANUTENCAO    motivo=Freio
    ${x}=    POST Situacao Veiculo    ${oficina}    ${m}
    Should Be Equal As Integers    ${x.status_code}    200
    ${em}=    Seed Emergencia
    ${emergencia}=    Set Variable    ${em.json()}[uuid]
    ${um}=    Pedido De Veiculo    ${emergencia}    ${destino}    Uma ambulância Robot    ${1}
    ${outro}=    Pedido De Veiculo    ${emergencia}    ${destino}    Outro veículo Robot
    ${r}=    Ceder    ${um}    ${oficina}
    Should Be Equal As Integers    ${r.status_code}    422
    ${r}=    Ceder    ${um}    ${amb}
    Should Be Equal As Integers    ${r.status_code}    201
    ${r}=    Ceder    ${um}    ${carro}
    Should Be Equal As Integers    ${r.status_code}    422
    ${r}=    Ceder    ${outro}    ${amb}
    Should Be Equal As Integers    ${r.status_code}    409
    ${r}=    Ceder    ${outro}    ${carro}
    Should Be Equal As Integers    ${r.status_code}    201
    ${p}=    Create Dictionary    responsavel=Secretário Robot    justificativa=Águas baixando
    ${dm}=    POST Desmobilizacao    ${emergencia}    ${p}
    Should Be Equal As Integers    ${dm.status_code}    200
    ${s}=    Situacao Do Veiculo    ${amb}
    Should Be Equal    ${s}    DISPONIVEL
    ${s}=    Situacao Do Veiculo    ${carro}
    Should Be Equal    ${s}    DISPONIVEL
    ${rel}=    GET Relatorio Pos Evento    ${emergencia}
    Should Be Equal As Integers    ${rel.json()}[indicadores][veiculosCedidos]    2
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Frota Emergencia - By Cenario
    [Documentation]    Executes the vehicles-in-emergencies scenario for the given case.
    [Arguments]    ${caso}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    IF    '${caso}' == 'CEDER_DEVOLVER'
        Frota Emergencia - Ceder E Devolver
    ELSE IF    '${caso}' == 'LIMITES_DESMOBILIZACAO'
        Frota Emergencia - Limites E Desmobilizacao
    ELSE
        Fail    Caso '${caso}' não suportado no cenário Frota nas Emergências.
    END
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
