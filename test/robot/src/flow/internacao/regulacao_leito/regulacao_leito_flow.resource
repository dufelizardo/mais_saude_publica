*** Settings ***
Library     String
Library     Collections
Resource    ../../../pom/internacao/regulacao_leito/regulacao_leito_pom.resource
Resource    ../leito_internacao/leito_internacao_flow.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
    Central de regulação de leitos (ADR-0184). A paciente semeada é mulher adulta; o leito semeado é clínico, misto e adulto.
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
Seed Um Pedido De Leito
    [Documentation]    A fresh adult patient asks a bed from a fresh origin unit; returns the request, the patient, the origin and the registration.
    [Arguments]    ${prioridade}=AMARELO
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${pacienteId}    ${cpf}    ${cartaoSus}    ${nome}=    Seed A Paciente
    ${origem}=    Seed A Unidade De Saude Uuid
    ${matricula}=    Seed A Profissional Matricula
    ${payload}=    Create Dictionary    pacienteId=${pacienteId}    unidadeOrigemId=${origem}    profissionalMatricula=${matricula}
    ...    cid=J18.9    quadroClinico=Pneumonia com hipoxemia, sem leito na UPA.    tipoLeito=CLINICO    prioridade=${prioridade}
    ${r}=    POST Pedir Leito    ${payload}
    Should Be Equal As Integers    ${r.status_code}    201
    RETURN    ${r.json()}[uuid]    ${pacienteId}    ${origem}    ${matricula}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Regulacao Leito - Pedido - Status Code 201 E 409
    [Documentation]    The request enters the queue; a second one for the same patient gives 409.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${pedido}    ${pacienteId}    ${origem}    ${matricula}=    Seed Um Pedido De Leito    VERMELHO
    ${d}=    GET Pedido De Leito    ${pedido}
    Should Be Equal    ${d.json()}[status]    SOLICITADA
    Validate Object Schema    ${d.json()}    ${SOLICITACAO_LEITO_SCHEMA}
    ${payload}=    Create Dictionary    pacienteId=${pacienteId}    unidadeOrigemId=${origem}    profissionalMatricula=${matricula}
    ...    cid=J18.9    quadroClinico=Outra vez.    tipoLeito=CLINICO    prioridade=VERDE
    ${r}=    POST Pedir Leito    ${payload}
    Should Be Equal As Integers    ${r.status_code}    409
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Regulacao Leito - Reserva E Admissao - Status Code 200
    [Documentation]    The bed of another unit is compatible, reserved and the destination admits.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${pedido}    ${pacienteId}    ${origem}    ${matricula}=    Seed Um Pedido De Leito
    ${leito}    ${unidade}    ${setor}=    Seed Um Leito
    ${vagas}=    GET Vagas Compativeis    ${pedido}
    ${ids}=    Evaluate    [v['leitoId'] for v in $vagas.json()]
    Should Contain    ${ids}    ${leito}
    ${fila}=    GET Fila De Leitos
    ${linha}=    Evaluate    [s for s in $fila.json() if s['uuid'] == $pedido][0]
    Should Not Be Equal    ${linha}[posicaoNaFila]    ${None}
    ${reserva}=    Create Dictionary    profissionalMatricula=${matricula}    leitoId=${leito}    horas=${4}
    ${r}=    POST Reservar Leito    ${pedido}    ${reserva}
    Should Be Equal As Integers    ${r.status_code}    200
    Should Be Equal    ${r.json()}[status]    RESERVADA
    ${admissao}=    Create Dictionary    medicoMatricula=${matricula}
    ${a}=    POST Admitir Regulado    ${pedido}    ${admissao}
    Should Be Equal As Integers    ${a.status_code}    200
    Should Be Equal    ${a.json()}[status]    ADMITIDA
    Should Not Be Equal    ${a.json()}[internacaoDestinoId]    ${None}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Regulacao Leito - Reserva - Status Code 422 - Mesma Unidade
    [Documentation]    A bed of the origin unit is not a regulated destination, expecting 422.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${leito}    ${unidade}    ${setor}=    Seed Um Leito
    ${pacienteId}    ${cpf}    ${cartaoSus}    ${nome}=    Seed A Paciente
    ${matricula}=    Seed A Profissional Matricula
    ${payload}=    Create Dictionary    pacienteId=${pacienteId}    unidadeOrigemId=${unidade}    profissionalMatricula=${matricula}
    ...    cid=J18.9    quadroClinico=Pneumonia.    tipoLeito=CLINICO    prioridade=AMARELO
    ${p}=    POST Pedir Leito    ${payload}
    ${reserva}=    Create Dictionary    profissionalMatricula=${matricula}    leitoId=${leito}
    ${r}=    POST Reservar Leito    ${p.json()}[uuid]    ${reserva}
    Should Be Equal As Integers    ${r.status_code}    422
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Regulacao Leito - Vaga Zero - Status Code 400
    [Documentation]    Vaga zero without justification, expecting 400.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${pedido}    ${pacienteId}    ${origem}    ${matricula}=    Seed Um Pedido De Leito
    ${destino}=    Seed A Unidade De Saude Uuid
    ${reserva}=    Create Dictionary    profissionalMatricula=${matricula}    unidadeDestinoId=${destino}
    ${r}=    POST Reservar Leito    ${pedido}    ${reserva}
    Should Be Equal As Integers    ${r.status_code}    400
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Regulacao Leito - Recusa Volta A Fila
    [Documentation]    The destination refuses: the request returns to the queue and the bed is free again.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${pedido}    ${pacienteId}    ${origem}    ${matricula}=    Seed Um Pedido De Leito
    ${leito}    ${unidade}    ${setor}=    Seed Um Leito
    ${reserva}=    Create Dictionary    profissionalMatricula=${matricula}    leitoId=${leito}
    POST Reservar Leito    ${pedido}    ${reserva}
    ${motivo}=    Create Dictionary    profissionalMatricula=${matricula}    motivo=Sem equipe hoje.
    ${r}=    POST Recusar Regulado    ${pedido}    ${motivo}
    Should Be Equal As Integers    ${r.status_code}    200
    Should Be Equal    ${r.json()}[status]    SOLICITADA
    ${params}=    Create Dictionary    unidadeId=${unidade}
    ${mapa}=    GET Mapa Leitos    ${params}
    ${situacao}=    Evaluate    [l['situacao'] for l in $mapa.json() if l['uuid'] == $leito][0]
    Should Be Equal    ${situacao}    LIVRE
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Regulacao Leito - Negar - Status Code 200
    [Documentation]    The regulator denies with a reason.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${pedido}    ${pacienteId}    ${origem}    ${matricula}=    Seed Um Pedido De Leito    AZUL
    ${motivo}=    Create Dictionary    profissionalMatricula=${matricula}    motivo=Quadro resolvido na unidade.
    ${r}=    POST Negar Pedido De Leito    ${pedido}    ${motivo}
    Should Be Equal As Integers    ${r.status_code}    200
    Should Be Equal    ${r.json()}[status]    NEGADA
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Regulacao Leito - By Cenario
    [Documentation]    Executes the Regulacao Leito scenario for the given case.
    [Arguments]    ${caso}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    IF    '${caso}' == 'PEDIDO'
        Regulacao Leito - Pedido - Status Code 201 E 409
    ELSE IF    '${caso}' == 'RESERVA_ADMISSAO'
        Regulacao Leito - Reserva E Admissao - Status Code 200
    ELSE IF    '${caso}' == 'RESERVA_422'
        Regulacao Leito - Reserva - Status Code 422 - Mesma Unidade
    ELSE IF    '${caso}' == 'VAGA_ZERO_400'
        Regulacao Leito - Vaga Zero - Status Code 400
    ELSE IF    '${caso}' == 'RECUSA'
        Regulacao Leito - Recusa Volta A Fila
    ELSE IF    '${caso}' == 'NEGAR'
        Regulacao Leito - Negar - Status Code 200
    ELSE
        Fail    Caso '${caso}' não suportado no cenário Regulacao Leito.
    END
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
