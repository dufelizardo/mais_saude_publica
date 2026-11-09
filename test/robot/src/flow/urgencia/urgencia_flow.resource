*** Settings ***
Library     String
Library     Collections
Resource    ../../pom/urgencia/urgencia_pom.resource
Resource    ../../pom/equipamentos/modelo_operacional/modelo_operacional_pom.resource
Resource    ../../pom/saude_mental/saude_mental_pom.resource
Resource    ../setor/create_setor/create_setor_flow.resource
Resource    ../paciente/create_paciente/create_paciente_flow.resource
Resource    ../profissional/get_profissional_by_matricula/get_profissional_by_matricula_flow.resource
Resource    ../common/schema_validation_flow.resource
Resource    ../../resource/config/schema/schema.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
Uuid Do Perfil Operacional
    [Documentation]    Returns the uuid of an operational profile by code.
    [Arguments]    ${codigo}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${lista}=    GET Perfis Operacionais
    ${uuid}=    Evaluate    [p['uuid'] for p in $lista.json() if p['codigo'] == $codigo][0]
    RETURN    ${uuid}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Uuid Da Capacidade Assistencial
    [Documentation]    Returns the uuid of a care capability by code.
    [Arguments]    ${codigo}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${lista}=    GET Capacidades Assistenciais
    ${uuid}=    Evaluate    [c['uuid'] for c in $lista.json() if c['codigo'] == $codigo][0]
    RETURN    ${uuid}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Seed Central De Urgencia
    [Documentation]    Seeds a unit with the Atenção móvel profile and the emergency regulation capability.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${central}=    Seed A Unidade De Saude Uuid
    ${perfil}=    Uuid Do Perfil Operacional    ATENCAO_MOVEL
    ${p}=    Create Dictionary    perfilId=${perfil}
    ${rp}=    POST Incluir Perfil Na Unidade    ${central}    ${p}
    Should Be Equal As Integers    ${rp.status_code}    200
    ${capacidade}=    Uuid Da Capacidade Assistencial    REGULACAO_MEDICA_URGENCIA
    ${c}=    Create Dictionary    capacidadeId=${capacidade}
    ${rc}=    POST Incluir Capacidade Na Unidade    ${central}    ${c}
    Should Be Equal As Integers    ${rc.status_code}    200
    RETURN    ${central}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Seed Destino De Urgencia
    [Documentation]    Seeds a unit with the Urgência profile.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${destino}=    Seed A Unidade De Saude Uuid
    ${perfil}=    Uuid Do Perfil Operacional    URGENCIA
    ${p}=    Create Dictionary    perfilId=${perfil}
    ${rp}=    POST Incluir Perfil Na Unidade    ${destino}    ${p}
    Should Be Equal As Integers    ${rp.status_code}    200
    RETURN    ${destino}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Abrir Chamado De Urgencia
    [Documentation]    Opens a 192 call at the center and returns its uuid.
    [Arguments]    ${central}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${payload}=    Create Dictionary    centralId=${central}    origem=CHAMADO_192    solicitanteTelefone=11977776666    queixa=Desmaio na rua.
    ...    localTipo=VIA_PUBLICA    endereco=Avenida Central, 500
    ${r}=    POST Ocorrencia Urgencia    ${payload}
    Should Be Equal As Integers    ${r.status_code}    201
    RETURN    ${r.json()}[uuid]
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Urgencia - Do Chamado Ao Destino - Status Code 200
    [Documentation]    Call, regulation with a resource, dispatch steps, destination with zero vacancy and receipt.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${central}=    Seed Central De Urgencia
    ${destino}=    Seed Destino De Urgencia
    ${regulador}=    Seed A Profissional Matricula
    ${id}=    Abrir Chamado De Urgencia    ${central}
    ${reg}=    Create Dictionary    profissionalMatricula=${regulador}    prioridade=VERMELHO    decisao=ENVIO_DE_RECURSO    conduta=Avaliar no local.    tipoRecurso=USA
    ${r}=    POST Regular Ocorrencia Urgencia    ${id}    ${reg}
    Should Be Equal As Integers    ${r.status_code}    200
    Should Be Equal    ${r.json()}[situacao]    REGULADA
    FOR    ${passo}    IN    ENVIO_DE_RECURSO    CHEGADA_AO_LOCAL    SAIDA_DO_LOCAL
        ${d}=    Create Dictionary    tipo=${passo}
        ${rd}=    POST Deslocamento Ocorrencia Urgencia    ${id}    ${d}
        Should Be Equal As Integers    ${rd.status_code}    200
    END
    ${dest}=    Create Dictionary    profissionalMatricula=${regulador}    destinoId=${destino}    vagaZero=${True}    justificativaVagaZero=Sala vermelha lotada.
    ${rdest}=    POST Destino Ocorrencia Urgencia    ${id}    ${dest}
    Should Be Equal As Integers    ${rdest.status_code}    200
    ${vazio}=    Create Dictionary
    ${rr}=    POST Recebimento Ocorrencia Urgencia    ${id}    ${vazio}
    Should Be Equal As Integers    ${rr.status_code}    200
    Validate Object Schema    ${rr.json()}    ${OCORRENCIA_URGENCIA_SCHEMA}
    Should Be Equal    ${rr.json()}[desfecho]    RECEBIDO_NO_DESTINO
    Should Be True    ${rr.json()}[vagaZero]
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Urgencia - Ordem Do Deslocamento - Status Code 422
    [Documentation]    Arrival before dispatch is refused; a repeated step conflicts.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${central}=    Seed Central De Urgencia
    ${regulador}=    Seed A Profissional Matricula
    ${id}=    Abrir Chamado De Urgencia    ${central}
    ${reg}=    Create Dictionary    profissionalMatricula=${regulador}    prioridade=AMARELO    decisao=ENVIO_DE_RECURSO    conduta=Avaliar.    tipoRecurso=USB
    ${r}=    POST Regular Ocorrencia Urgencia    ${id}    ${reg}
    Should Be Equal As Integers    ${r.status_code}    200
    ${chegada}=    Create Dictionary    tipo=CHEGADA_AO_LOCAL
    ${r422}=    POST Deslocamento Ocorrencia Urgencia    ${id}    ${chegada}
    Should Be Equal As Integers    ${r422.status_code}    422
    ${envio}=    Create Dictionary    tipo=ENVIO_DE_RECURSO
    ${r1}=    POST Deslocamento Ocorrencia Urgencia    ${id}    ${envio}
    Should Be Equal As Integers    ${r1.status_code}    200
    ${r409}=    POST Deslocamento Ocorrencia Urgencia    ${id}    ${envio}
    Should Be Equal As Integers    ${r409.status_code}    409
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Urgencia - Regras Da Decisao - Status Code 400
    [Documentation]    Resource without type, vacancy zero without justification, and a center without the capability.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${central}=    Seed Central De Urgencia
    ${destino}=    Seed Destino De Urgencia
    ${regulador}=    Seed A Profissional Matricula
    ${id}=    Abrir Chamado De Urgencia    ${central}
    ${sem}=    Create Dictionary    profissionalMatricula=${regulador}    prioridade=AMARELO    decisao=ENVIO_DE_RECURSO    conduta=Avaliar.
    ${r1}=    POST Regular Ocorrencia Urgencia    ${id}    ${sem}
    Should Be Equal As Integers    ${r1.status_code}    400
    ${vz}=    Create Dictionary    profissionalMatricula=${regulador}    prioridade=AMARELO    decisao=ENCAMINHAMENTO    conduta=Ir à UPA.    destinoId=${destino}    vagaZero=${True}
    ${r2}=    POST Regular Ocorrencia Urgencia    ${id}    ${vz}
    Should Be Equal As Integers    ${r2.status_code}    400
    ${ubs}=    Seed A Unidade De Saude Uuid
    ${payload}=    Create Dictionary    centralId=${ubs}    origem=CHAMADO_192    solicitanteTelefone=11977776666    queixa=Dor.
    ${r3}=    POST Ocorrencia Urgencia    ${payload}
    Should Be Equal As Integers    ${r3.status_code}    422
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Urgencia - Orientacao E Painel - Status Code 200
    [Documentation]    Phone guidance closes the occurrence; the panel puts awaiting first.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${central}=    Seed Central De Urgencia
    ${regulador}=    Seed A Profissional Matricula
    ${orientada}=    Abrir Chamado De Urgencia    ${central}
    ${aguardando}=    Abrir Chamado De Urgencia    ${central}
    ${reg}=    Create Dictionary    profissionalMatricula=${regulador}    prioridade=VERDE    decisao=ORIENTACAO    conduta=Hidratar e observar.
    ${r}=    POST Regular Ocorrencia Urgencia    ${orientada}    ${reg}
    Should Be Equal As Integers    ${r.status_code}    200
    Should Be Equal    ${r.json()}[desfecho]    ORIENTADO
    ${params}=    Create Dictionary    centralId=${central}
    ${p}=    GET Painel Urgencia    ${params}
    Should Be Equal As Integers    ${p.status_code}    200
    Validate Object Schema    ${p.json()}    ${PAINEL_URGENCIA_SCHEMA}
    ${ids}=    Evaluate    [o['uuid'] for o in $p.json()['abertas']]
    Should Be Equal    ${ids}    ${{[$aguardando]}}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Urgencia - Acionamento Pela Crise - Status Code 201
    [Documentation]    A mental health crisis calls the SAMU; a second open call conflicts.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${central}=    Seed Central De Urgencia
    ${unidade}=    Seed A Unidade De Saude Uuid
    ${paciente}    ${cpf}    ${cns}    ${nome}=    Seed A Paciente
    ${crise}=    Create Dictionary    pacienteId=${paciente}    unidadeId=${unidade}    tipo=AGITACAO_PSICOMOTORA    descricao=Agitação intensa.
    ${rc}=    POST Crise Saude Mental    ${crise}
    Should Be Equal As Integers    ${rc.status_code}    201
    ${acionar}=    Create Dictionary    centralId=${central}    motivo=Agitação com risco; precisa de remoção.
    ${r}=    POST Acionar Samu Pela Crise    ${rc.json()}[uuid]    ${acionar}
    Should Be Equal As Integers    ${r.status_code}    201
    Should Be Equal    ${r.json()}[origem]    CRISE_SAUDE_MENTAL
    Should Be Equal    ${r.json()}[pacienteId]    ${paciente}
    ${r409}=    POST Acionar Samu Pela Crise    ${rc.json()}[uuid]    ${acionar}
    Should Be Equal As Integers    ${r409.status_code}    409
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Urgencia - By HTTP Status Code
    [Documentation]    Dispatches the scenario by HTTP status code.
    [Arguments]    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Run Keyword If    '${meuHTTP}' == '200-ciclo'    Urgencia - Do Chamado Ao Destino - Status Code 200
    Run Keyword If    '${meuHTTP}' == '422-deslocamento'    Urgencia - Ordem Do Deslocamento - Status Code 422
    Run Keyword If    '${meuHTTP}' == '400-decisao'    Urgencia - Regras Da Decisao - Status Code 400
    Run Keyword If    '${meuHTTP}' == '200-orientacao'    Urgencia - Orientacao E Painel - Status Code 200
    Run Keyword If    '${meuHTTP}' == '201-crise'    Urgencia - Acionamento Pela Crise - Status Code 201
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
