*** Settings ***
Library     String
Library     Collections
Resource    ../../pom/urgencia/ficha_aph_pom.resource
Resource    ./urgencia_flow.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
    Ficha do APH (ADR-0188): a ocorrência com recurso enviado vem da central semeada pela regulação de urgência.
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
Seed Ocorrencia Com Recurso
    [Documentation]    A center and a call regulated with a USA dispatched; returns the occurrence, the center and a professional registration.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${central}=    Seed Central De Urgencia
    ${regulador}=    Seed A Profissional Matricula
    ${id}=    Abrir Chamado De Urgencia    ${central}
    ${reg}=    Create Dictionary    profissionalMatricula=${regulador}    prioridade=VERMELHO    decisao=ENVIO_DE_RECURSO    conduta=Avaliar no local.
    ...    tipoRecurso=USA    recursoIdentificacao=USA-01
    ${r}=    POST Regular Ocorrencia Urgencia    ${id}    ${reg}
    Should Be Equal As Integers    ${r.status_code}    200
    RETURN    ${id}    ${central}    ${regulador}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Seed Uma Ficha Aph
    [Documentation]    An occurrence with a dispatched resource and its record opened; returns the record, the occurrence, the center and a registration.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${id}    ${central}    ${matricula}=    Seed Ocorrencia Com Recurso
    ${payload}=    Create Dictionary    natureza=CLINICA    situacaoEncontrada=Consciente, pálido, sudoreico.
    ${r}=    PUT Ficha Aph    ${id}    ${payload}
    Should Be Equal As Integers    ${r.status_code}    200
    RETURN    ${r.json()}[uuid]    ${id}    ${central}    ${matricula}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Ficha Aph - So Com Recurso - Status Code 422 E 200
    [Documentation]    A call without dispatched resource has no record (422); with it, the record opens and the list shows it.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${central}=    Seed Central De Urgencia
    ${sem}=    Abrir Chamado De Urgencia    ${central}
    ${payload}=    Create Dictionary    natureza=CLINICA
    ${r}=    PUT Ficha Aph    ${sem}    ${payload}
    Should Be Equal As Integers    ${r.status_code}    422
    ${ficha}    ${id}    ${central2}    ${matricula}=    Seed Uma Ficha Aph
    ${lista}=    GET Atendimentos Aph    ${central2}
    Should Be Equal As Integers    ${lista.status_code}    200
    Should Be Equal    ${lista.json()}[0][fichaId]    ${ficha}
    ${d}=    GET Ficha Aph    ${id}
    Should Be Equal    ${d.json()}[situacaoEncontrada]    Consciente, pálido, sudoreico.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Ficha Aph - Glasgow E Sinais Vitais
    [Documentation]    Glasgow out of range gives 400; the total is computed; vital signs out of range give 400 and the series is kept.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${ficha}    ${id}    ${central}    ${matricula}=    Seed Uma Ficha Aph
    ${ruim}=    Create Dictionary    natureza=TRAUMA    glasgowOcular=${5}
    ${r}=    PUT Ficha Aph    ${id}    ${ruim}
    Should Be Equal As Integers    ${r.status_code}    400
    ${ok}=    Create Dictionary    natureza=TRAUMA    mecanismoTrauma=QUEDA    glasgowOcular=${3}    glasgowVerbal=${4}    glasgowMotor=${6}
    ${r}=    PUT Ficha Aph    ${id}    ${ok}
    Should Be Equal As Integers    ${r.status_code}    200
    Should Be Equal As Integers    ${r.json()}[glasgowTotal]    13
    ${fora}=    Create Dictionary    profissionalMatricula=${matricula}    saturacao=${150}
    ${s}=    POST Registro Aph    ${ficha}    sinais-vitais    ${fora}
    Should Be Equal As Integers    ${s.status_code}    400
    ${primeira}=    Create Dictionary    profissionalMatricula=${matricula}    paSistolica=${90}    paDiastolica=${60}    frequenciaCardiaca=${118}    saturacao=${91}
    ${s}=    POST Registro Aph    ${ficha}    sinais-vitais    ${primeira}
    Should Be Equal As Integers    ${s.status_code}    200
    ${segunda}=    Create Dictionary    profissionalMatricula=${matricula}    paSistolica=${110}    paDiastolica=${70}    saturacao=${96}
    ${s}=    POST Registro Aph    ${ficha}    sinais-vitais    ${segunda}
    Length Should Be    ${s.json()}[sinaisVitais]    2
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Ficha Aph - Finalizar E Adendo
    [Documentation]    Finalizing without the outcome gives 422; the removal needs who received; once finalized, only an addendum.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${ficha}    ${id}    ${central}    ${matricula}=    Seed Uma Ficha Aph
    ${med}=    Create Dictionary    profissionalMatricula=${matricula}    nome=Soro fisiológico    dose=500 ml    via=INTRAVENOSA
    ${m}=    POST Registro Aph    ${ficha}    medicamentos    ${med}
    Should Be Equal As Integers    ${m.status_code}    200
    ${fim}=    Create Dictionary    profissionalMatricula=${matricula}
    ${f}=    POST Registro Aph    ${ficha}    finalizacao    ${fim}
    Should Be Equal As Integers    ${f.status_code}    422
    ${remocao}=    Create Dictionary    natureza=CLINICA    desfecho=REMOVIDO_AO_DESTINO
    ${r}=    PUT Ficha Aph    ${id}    ${remocao}
    ${f}=    POST Registro Aph    ${ficha}    finalizacao    ${fim}
    Should Be Equal As Integers    ${f.status_code}    422
    ${entregue}=    Create Dictionary    natureza=CLINICA    desfecho=REMOVIDO_AO_DESTINO    recebidoPor=Enf. Clara
    ${r}=    PUT Ficha Aph    ${id}    ${entregue}
    ${f}=    POST Registro Aph    ${ficha}    finalizacao    ${fim}
    Should Be Equal As Integers    ${f.status_code}    200
    Should Be True    ${f.json()}[finalizada]
    ${r}=    PUT Ficha Aph    ${id}    ${entregue}
    Should Be Equal As Integers    ${r.status_code}    422
    ${adendo}=    Create Dictionary    profissionalMatricula=${matricula}    texto=Paciente relatou alergia a dipirona.
    ${a}=    POST Registro Aph    ${ficha}    adendo    ${adendo}
    Should Be Equal As Integers    ${a.status_code}    200
    Length Should Be    ${a.json()}[adendos]    1
    ${pdf}=    GET Ficha Aph Pdf    ${ficha}
    Should Be Equal As Integers    ${pdf.status_code}    200
    Should Start With    ${pdf.headers}[Content-Type]    application/pdf
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Ficha Aph - Obito Pede Constatacao
    [Documentation]    The death on scene needs the time and the doctor who confirmed it before finalizing.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${ficha}    ${id}    ${central}    ${matricula}=    Seed Uma Ficha Aph
    ${obito}=    Create Dictionary    natureza=CLINICA    desfecho=OBITO_NO_LOCAL
    ${r}=    PUT Ficha Aph    ${id}    ${obito}
    ${fim}=    Create Dictionary    profissionalMatricula=${matricula}
    ${f}=    POST Registro Aph    ${ficha}    finalizacao    ${fim}
    Should Be Equal As Integers    ${f.status_code}    422
    ${hora}=    Evaluate    datetime.datetime.now(datetime.timezone.utc).strftime('%Y-%m-%dT%H:%M:%SZ')    modules=datetime
    ${completo}=    Create Dictionary    natureza=CLINICA    desfecho=OBITO_NO_LOCAL    obitoEm=${hora}    obitoConstatadoPorMatricula=${matricula}
    ${r}=    PUT Ficha Aph    ${id}    ${completo}
    Should Be Equal As Integers    ${r.status_code}    200
    ${f}=    POST Registro Aph    ${ficha}    finalizacao    ${fim}
    Should Be Equal As Integers    ${f.status_code}    200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Ficha Aph - By Cenario
    [Documentation]    Executes the Ficha Aph scenario for the given case.
    [Arguments]    ${caso}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    IF    '${caso}' == 'SO_COM_RECURSO'
        Ficha Aph - So Com Recurso - Status Code 422 E 200
    ELSE IF    '${caso}' == 'GLASGOW_SINAIS'
        Ficha Aph - Glasgow E Sinais Vitais
    ELSE IF    '${caso}' == 'FINALIZAR_ADENDO'
        Ficha Aph - Finalizar E Adendo
    ELSE IF    '${caso}' == 'OBITO'
        Ficha Aph - Obito Pede Constatacao
    ELSE
        Fail    Caso '${caso}' não suportado no cenário Ficha Aph.
    END
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
