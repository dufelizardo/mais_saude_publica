*** Settings ***
Library     String
Library     Collections
Library     DateTime
Resource    ../../pom/transporte/transporte_eletivo_pom.resource
Resource    ./transporte_flow.resource
Resource    ../paciente/create_paciente/create_paciente_flow.resource
Resource    ../setor/create_setor/create_setor_flow.resource
Resource    ../profissional/get_profissional_by_matricula/get_profissional_by_matricula_flow.resource
Resource    ../common/schema_validation_flow.resource
Resource    ../../resource/config/schema/schema.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
    Transporte eletivo e TFD (ADR-0190). O compromisso é amanhã às 10h; a janela da viagem (amanhã das 03h às 23h em UTC) o
    contém em qualquer fuso do servidor.
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
Amanha
    [Documentation]    Tomorrow as yyyy-mm-dd.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${d}=    Get Current Date    increment=1 day    result_format=%Y-%m-%d
    RETURN    ${d}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Pedido De Transporte
    [Documentation]    A request payload for tomorrow at the given time; extra keys go in ${extra}.
    [Arguments]    ${paciente}    ${unidade}    ${motivo}    ${hora}=10:00    &{extra}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${amanha}=    Amanha
    ${p}=    Create Dictionary    pacienteId=${paciente}    unidadeId=${unidade}    motivo=${motivo}    trecho=IDA_E_VOLTA
    ...    dataHora=${amanha}T${hora}    enderecoEmbarque=Rua do Robot, 100    destino=Hospital Regional Robot    destinoMunicipio=Cidade Robot Beta
    Set To Dictionary    ${p}    &{extra}
    RETURN    ${p}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Solicitacao Criada
    [Documentation]    Creates a simple request and returns its uuid.
    [Arguments]    ${paciente}    ${unidade}    ${motivo}=CONSULTA    &{extra}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${p}=    Pedido De Transporte    ${paciente}    ${unidade}    ${motivo}    &{extra}
    ${r}=    POST Transporte Eletivo    solicitacao    ${p}
    Should Be Equal As Integers    ${r.status_code}    201
    RETURN    ${r.json()}[uuid]
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Payload Viagem
    [Documentation]    A trip tomorrow from 03:00 to 23:00 UTC with the given requests.
    [Arguments]    ${veiculo}    ${condutor}    @{solicitacoes}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${amanha}=    Amanha
    ${lista}=    Create List    @{solicitacoes}
    ${p}=    Create Dictionary    veiculoId=${veiculo}    condutorMatricula=${condutor}    saidaPrevista=${amanha}T03:00:00Z
    ...    retornoPrevisto=${amanha}T23:00:00Z    solicitacoes=${lista}
    RETURN    ${p}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Situacao Da Solicitacao
    [Documentation]    The current status of a request.
    [Arguments]    ${id}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${r}=    GET Transporte Eletivo    solicitacao/${id}
    Should Be Equal As Integers    ${r.status_code}    200
    RETURN    ${r.json()}[situacao]
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Transporte Eletivo - Solicitacao E TFD - Status Code 201
    [Documentation]    Stretcher without a companion (400), TFD without the report (400), TFD awaiting the commission, authorized once (422 twice).
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${unidade}=    Seed A Unidade De Saude Uuid
    ${paciente}    ${cpf}    ${cns}    ${nome}=    Seed A Paciente
    ${medico}=    Seed A Profissional Matricula
    ${maca}=    Pedido De Transporte    ${paciente}    ${unidade}    EXAME    maca=${True}
    ${r400}=    POST Transporte Eletivo    solicitacao    ${maca}
    Should Be Equal As Integers    ${r400.status_code}    400
    ${sem_laudo}=    Pedido De Transporte    ${paciente}    ${unidade}    TFD
    ${r400b}=    POST Transporte Eletivo    solicitacao    ${sem_laudo}
    Should Be Equal As Integers    ${r400b.status_code}    400
    ${tfd}=    Pedido De Transporte    ${paciente}    ${unidade}    TFD    cid=C50    justificativa=Radioterapia sem oferta no município
    ...    profissionalMatricula=${medico}
    ${r}=    POST Transporte Eletivo    solicitacao    ${tfd}
    Should Be Equal As Integers    ${r.status_code}    201
    Validate Object Schema    ${r.json()}    ${SOLICITACAO_TRANSPORTE_SCHEMA}
    Should Be Equal    ${r.json()}[situacao]    AGUARDANDO_AUTORIZACAO
    ${id}=    Set Variable    ${r.json()}[uuid]
    ${ra}=    POST Transporte Eletivo    solicitacao/${id}/autorizacao
    Should Be Equal As Integers    ${ra.status_code}    200
    Should Be Equal    ${ra.json()}[situacao]    AUTORIZADA
    ${r422}=    POST Transporte Eletivo    solicitacao/${id}/autorizacao
    Should Be Equal As Integers    ${r422.status_code}    422
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Transporte Eletivo - Recorrencia - Status Code 201
    [Documentation]    Hemodialysis three times a week for two weeks: six requests; cancelling with the recurrence cancels the next ones.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${unidade}=    Seed A Unidade De Saude Uuid
    ${paciente}    ${cpf}    ${cns}    ${nome}=    Seed A Paciente
    ${dias}=    Create List    MONDAY    WEDNESDAY    FRIDAY
    ${fim}=    Get Current Date    increment=14 days    result_format=%Y-%m-%d
    ${consulta}=    Pedido De Transporte    ${paciente}    ${unidade}    CONSULTA    06:00    diasDaSemana=${dias}    recorrenteAte=${fim}
    ${r400}=    POST Transporte Eletivo    solicitacao/recorrente    ${consulta}
    Should Be Equal As Integers    ${r400.status_code}    400
    ${hd}=    Pedido De Transporte    ${paciente}    ${unidade}    HEMODIALISE    06:00    diasDaSemana=${dias}    recorrenteAte=${fim}
    ${r}=    POST Transporte Eletivo    solicitacao/recorrente    ${hd}
    Should Be Equal As Integers    ${r.status_code}    201
    ${n}=    Get Length    ${r.json()}
    Should Be True    ${n} >= 6
    ${primeira}=    Set Variable    ${r.json()}[0][uuid]
    ${segunda}=    Set Variable    ${r.json()}[1][uuid]
    ${motivo}=    Create Dictionary    motivo=Mudou de clínica    recorrencia=${True}
    ${rc}=    POST Transporte Eletivo    solicitacao/${segunda}/cancelamento    ${motivo}
    Should Be Equal As Integers    ${rc.status_code}    200
    ${canceladas}=    Get Length    ${rc.json()}
    Should Be Equal As Integers    ${canceladas}    ${n - 1}
    ${s}=    Situacao Da Solicitacao    ${primeira}
    Should Be Equal    ${s}    SOLICITADA
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Transporte Eletivo - Viagem - Status Code 200
    [Documentation]    Plans a trip (the same request again: 409), departs, registers a no-show and arrives with the mileage.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${garagem}=    Seed A Unidade De Saude Uuid
    ${unidade}=    Seed A Unidade De Saude Uuid
    ${placa}=    Placa Aleatoria
    ${dv}=    Create Dictionary    placa=${placa}    prefixo=RB-${placa}    tipo=VAN_SANITARIA    unidadeId=${garagem}    lugares=${8}
    ...    cadeiraDeRodas=${True}    quilometragem=${1000}
    ${rv}=    POST Veiculo    ${dv}
    Should Be Equal As Integers    ${rv.status_code}    201
    ${van}=    Set Variable    ${rv.json()}[uuid]
    ${condutor}=    Seed A Profissional Matricula
    ${p1}    ${c1}    ${s1}    ${n1}=    Seed A Paciente
    ${p2}    ${c2}    ${s2}    ${n2}=    Seed A Paciente
    ${a}=    Solicitacao Criada    ${p1}    ${unidade}
    ${b}=    Solicitacao Criada    ${p2}    ${unidade}    EXAME    acompanhanteNome=Filha
    ${dp}=    Payload Viagem    ${van}    ${condutor}    ${a}    ${b}
    ${r}=    POST Transporte Eletivo    viagem    ${dp}
    Should Be Equal As Integers    ${r.status_code}    201
    Validate Object Schema    ${r.json()}    ${VIAGEM_TRANSPORTE_SCHEMA}
    Should Be Equal As Integers    ${r.json()}[lugaresOcupados]    3
    ${viagem}=    Set Variable    ${r.json()}[uuid]
    ${r409}=    POST Transporte Eletivo    viagem    ${dp}
    Should Be Equal As Integers    ${r409.status_code}    409
    ${km_ruim}=    Create Dictionary    quilometragem=${999}
    ${r422}=    POST Transporte Eletivo    viagem/${viagem}/saida    ${km_ruim}
    Should Be Equal As Integers    ${r422.status_code}    422
    ${km_saida}=    Create Dictionary    quilometragem=${1010}
    ${rs}=    POST Transporte Eletivo    viagem/${viagem}/saida    ${km_saida}
    Should Be Equal As Integers    ${rs.status_code}    200
    ${veiculo}=    GET Veiculo    ${van}
    Should Be Equal    ${veiculo.json()}[situacao]    EM_SERVICO
    ${foi}=    Create Dictionary    solicitacaoId=${a}    embarcou=${True}
    ${faltou}=    Create Dictionary    solicitacaoId=${b}    embarcou=${False}    motivo=Não estava em casa
    ${itens}=    Create List    ${foi}    ${faltou}
    ${presencas}=    Create Dictionary    itens=${itens}
    ${re}=    POST Transporte Eletivo    viagem/${viagem}/embarque    ${presencas}
    Should Be Equal As Integers    ${re.status_code}    200
    ${km_chegada}=    Create Dictionary    quilometragem=${1062}
    ${rc}=    POST Transporte Eletivo    viagem/${viagem}/chegada    ${km_chegada}
    Should Be Equal As Integers    ${rc.status_code}    200
    Should Be Equal    ${rc.json()}[situacao]    CONCLUIDA
    Should Be Equal As Integers    ${rc.json()}[kmPercorridos]    52
    ${sa}=    Situacao Da Solicitacao    ${a}
    Should Be Equal    ${sa}    REALIZADA
    ${sb}=    Situacao Da Solicitacao    ${b}
    Should Be Equal    ${sb}    FALTOU
    ${veiculo}=    GET Veiculo    ${van}
    Should Be Equal    ${veiculo.json()}[situacao]    DISPONIVEL
    Should Be Equal As Integers    ${veiculo.json()}[quilometragem]    1062
    ${rk}=    GET Transporte Eletivo    quilometragem?unidadeId=${garagem}
    Should Be Equal As Integers    ${rk.status_code}    200
    Should Be Equal As Integers    ${rk.json()}[0][km]    52
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Transporte Eletivo - By Caso
    [Documentation]    Dispatches the scenario by case.
    [Arguments]    ${caso}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Run Keyword If    '${caso}' == 'SOLICITACAO_TFD'    Transporte Eletivo - Solicitacao E TFD - Status Code 201
    Run Keyword If    '${caso}' == 'RECORRENCIA'    Transporte Eletivo - Recorrencia - Status Code 201
    Run Keyword If    '${caso}' == 'VIAGEM'    Transporte Eletivo - Viagem - Status Code 200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
