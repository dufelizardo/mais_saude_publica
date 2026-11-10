*** Settings ***
Library     String
Library     Collections
Library     DateTime
Resource    ../../../pom/internacao/eletiva_censo/eletiva_censo_pom.resource
Resource    ../leito_internacao/leito_internacao_flow.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
    Internação eletiva e censo diário (ADR-0185). O leito semeado é clínico, misto e adulto; a paciente semeada é adulta.
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
Data Daqui A
    [Documentation]    The ISO date N days from today (negative is the past).
    [Arguments]    ${dias}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${d}=    Get Current Date    result_format=%Y-%m-%d    increment=${dias} days
    RETURN    ${d}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Seed Uma Eletiva
    [Documentation]    A clinical bed in a fresh unit and an elective admission there N days from today; returns the elective, the bed, the unit and the registration.
    [Arguments]    ${dias}=0
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${leito}    ${unidade}    ${setor}=    Seed Um Leito
    ${pacienteId}    ${cpf}    ${cartaoSus}    ${nome}=    Seed A Paciente
    ${matricula}=    Seed A Profissional Matricula
    ${data}=    Data Daqui A    ${dias}
    ${payload}=    Create Dictionary    pacienteId=${pacienteId}    unidadeId=${unidade}    dataPrevista=${data}    tipoLeito=CLINICO
    ...    medicoMatricula=${matricula}    cid=K80.2    procedimento=Colecistectomia videolaparoscópica.    previsaoDias=${2}
    ${r}=    POST Agendar Eletiva    ${payload}
    Should Be Equal As Integers    ${r.status_code}    201
    RETURN    ${r.json()}[uuid]    ${leito}    ${unidade}    ${matricula}    ${payload}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Eletiva - Agendar - Status Code 201 E 409
    [Documentation]    The elective is scheduled and shows in the unit agenda; a second one for the same patient gives 409.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${eletiva}    ${leito}    ${unidade}    ${matricula}    ${payload}=    Seed Uma Eletiva    10
    ${d}=    GET Eletiva    ${eletiva}
    Should Be Equal As Integers    ${d.status_code}    200
    Should Be Equal    ${d.json()}[status]    AGENDADA
    Should Not Be True    ${d.json()}[podeReservar]
    ${ate}=    Data Daqui A    30
    ${params}=    Create Dictionary    unidadeId=${unidade}    ate=${ate}
    ${agenda}=    GET Agenda Eletivas    ${params}
    ${ids}=    Evaluate    [e['uuid'] for e in $agenda.json()]
    Should Contain    ${ids}    ${eletiva}
    ${r}=    POST Agendar Eletiva    ${payload}
    Should Be Equal As Integers    ${r.status_code}    409
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Eletiva - Reserva E Admissao - Status Code 200
    [Documentation]    On the day, the bed is reserved and the patient admitted; the bed becomes occupied.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${eletiva}    ${leito}    ${unidade}    ${matricula}    ${payload}=    Seed Uma Eletiva    0
    ${reserva}=    Create Dictionary    profissionalMatricula=${matricula}    leitoId=${leito}
    ${r}=    POST Acao Eletiva    ${eletiva}    reserva    ${reserva}
    Should Be Equal As Integers    ${r.status_code}    200
    Should Be Equal    ${r.json()}[status]    RESERVADA
    ${params}=    Create Dictionary    unidadeId=${unidade}
    ${mapa}=    GET Mapa Leitos    ${params}
    ${situacao}=    Evaluate    [l['situacao'] for l in $mapa.json() if l['uuid'] == $leito][0]
    Should Be Equal    ${situacao}    RESERVADO
    ${admissao}=    Create Dictionary    profissionalMatricula=${matricula}
    ${a}=    POST Acao Eletiva    ${eletiva}    admissao    ${admissao}
    Should Be Equal As Integers    ${a.status_code}    200
    Should Be Equal    ${a.json()}[status]    ADMITIDA
    Should Not Be Equal    ${a.json()}[internacaoId]    ${None}
    ${mapa}=    GET Mapa Leitos    ${params}
    ${situacao}=    Evaluate    [l['situacao'] for l in $mapa.json() if l['uuid'] == $leito][0]
    Should Be Equal    ${situacao}    OCUPADO
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Eletiva - Reserva - Status Code 422 - Fora Da Janela
    [Documentation]    The reservation opens two days before the date; ten days before gives 422.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${eletiva}    ${leito}    ${unidade}    ${matricula}    ${payload}=    Seed Uma Eletiva    10
    ${reserva}=    Create Dictionary    profissionalMatricula=${matricula}    leitoId=${leito}
    ${r}=    POST Acao Eletiva    ${eletiva}    reserva    ${reserva}
    Should Be Equal As Integers    ${r.status_code}    422
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Eletiva - Remarcar E Cancelar
    [Documentation]    Rescheduling without a reason gives 400; with a reason, the date changes; then it is cancelled.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${eletiva}    ${leito}    ${unidade}    ${matricula}    ${payload}=    Seed Uma Eletiva    10
    ${nova}=    Data Daqui A    15
    ${sem}=    Create Dictionary    profissionalMatricula=${matricula}    dataPrevista=${nova}
    ${r}=    POST Acao Eletiva    ${eletiva}    remarcacao    ${sem}
    Should Be Equal As Integers    ${r.status_code}    400
    ${com}=    Create Dictionary    profissionalMatricula=${matricula}    dataPrevista=${nova}    motivo=Centro cirúrgico em manutenção.
    ${r}=    POST Acao Eletiva    ${eletiva}    remarcacao    ${com}
    Should Be Equal As Integers    ${r.status_code}    200
    Should Be Equal    ${r.json()}[dataPrevista]    ${nova}
    ${cancela}=    Create Dictionary    profissionalMatricula=${matricula}    motivo=Paciente desistiu da cirurgia.
    ${c}=    POST Acao Eletiva    ${eletiva}    cancelamento    ${cancela}
    Should Be Equal As Integers    ${c.status_code}    200
    Should Be Equal    ${c.json()}[status]    CANCELADA
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Eletiva - Falta - Status Code 422 E 200
    [Documentation]    The absence is only recorded from the scheduled day on.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${futura}    ${leito}    ${unidade}    ${matricula}    ${payload}=    Seed Uma Eletiva    10
    ${falta}=    Create Dictionary    profissionalMatricula=${matricula}    motivo=Não compareceu.
    ${r}=    POST Acao Eletiva    ${futura}    falta    ${falta}
    Should Be Equal As Integers    ${r.status_code}    422
    ${hoje}    ${leito2}    ${unidade2}    ${matricula2}    ${payload2}=    Seed Uma Eletiva    0
    ${r}=    POST Acao Eletiva    ${hoje}    falta    ${falta}
    Should Be Equal As Integers    ${r.status_code}    200
    Should Be Equal    ${r.json()}[status]    FALTA
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Censo - Previa Fechar Reabrir
    [Documentation]    Preview of yesterday, closing (201), closing again (409), reopening without reason (400) and with reason (200); today cannot be closed (422).
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${leito}    ${unidade}    ${setor}=    Seed Um Leito
    ${ontem}=    Data Daqui A    -1
    ${p}=    GET Censo Do Dia    ${unidade}    ${ontem}
    Should Be Equal As Integers    ${p.status_code}    200
    Should Not Be True    ${p.json()}[fechado]
    Should Be Equal As Integers    ${p.json()}[leitosOperacionais]    1
    ${dia}=    Create Dictionary    unidadeId=${unidade}    data=${ontem}
    ${f}=    POST Fechar Censo    ${dia}
    Should Be Equal As Integers    ${f.status_code}    201
    Should Be True    ${f.json()}[fechado]
    Should Be Equal As Integers    ${f.json()}[versao]    1
    ${f2}=    POST Fechar Censo    ${dia}
    Should Be Equal As Integers    ${f2.status_code}    409
    ${r}=    POST Reabrir Censo    ${dia}
    Should Be Equal As Integers    ${r.status_code}    400
    ${motivo}=    Create Dictionary    unidadeId=${unidade}    data=${ontem}    motivo=Alta lançada com atraso.
    ${r}=    POST Reabrir Censo    ${motivo}
    Should Be Equal As Integers    ${r.status_code}    200
    ${f3}=    POST Fechar Censo    ${dia}
    Should Be Equal As Integers    ${f3.status_code}    201
    Should Be Equal As Integers    ${f3.json()}[versao]    2
    ${hoje}=    Data Daqui A    0
    ${h}=    Create Dictionary    unidadeId=${unidade}    data=${hoje}
    ${f4}=    POST Fechar Censo    ${h}
    Should Be Equal As Integers    ${f4.status_code}    422
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Eletiva Censo - By Cenario
    [Documentation]    Executes the Eletiva Censo scenario for the given case.
    [Arguments]    ${caso}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    IF    '${caso}' == 'AGENDAR'
        Eletiva - Agendar - Status Code 201 E 409
    ELSE IF    '${caso}' == 'RESERVA_ADMISSAO'
        Eletiva - Reserva E Admissao - Status Code 200
    ELSE IF    '${caso}' == 'RESERVA_422'
        Eletiva - Reserva - Status Code 422 - Fora Da Janela
    ELSE IF    '${caso}' == 'REMARCAR_CANCELAR'
        Eletiva - Remarcar E Cancelar
    ELSE IF    '${caso}' == 'FALTA'
        Eletiva - Falta - Status Code 422 E 200
    ELSE IF    '${caso}' == 'CENSO'
        Censo - Previa Fechar Reabrir
    ELSE
        Fail    Caso '${caso}' não suportado no cenário Eletiva Censo.
    END
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
