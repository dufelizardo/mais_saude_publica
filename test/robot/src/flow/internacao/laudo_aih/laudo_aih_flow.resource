*** Settings ***
Library     String
Library     Collections
Library     DateTime
Resource    ../../../pom/internacao/laudo_aih/laudo_aih_pom.resource
Resource    ../leito_internacao/leito_internacao_flow.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
    Laudo de AIH (ADR-0186). As faixas são da rede inteira: cada caso cadastra uma faixa sorteada e confere o formato do número
    (13 dígitos, dígito verificador pelo módulo 11), sem depender de qual faixa a autorização usou.
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
Seed Um Procedimento Para AIH
    [Documentation]    A general catalog procedure with a SIGTAP code; returns its uuid and name.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${sufixo}=    Evaluate    uuid.uuid4().hex[:8].upper()    modules=uuid
    ${payload}=    Create Dictionary    codigo=AIH_${sufixo}    nome=Robot AIH ${sufixo}    sigtap=0303140151
    ${r}=    POST Item Do Catalogo Para AIH    ${payload}
    Should Be Equal As Integers    ${r.status_code}    201
    RETURN    ${r.json()}[uuid]    ${r.json()}[nome]
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Seed Uma Faixa AIH
    [Documentation]    A random range of the current year with ten numbers.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${ano}=    Get Current Date    result_format=%Y
    ${inicio}=    Evaluate    random.randint(10**10, 9*10**11)    modules=random
    ${fim}=    Evaluate    ${inicio} + 9
    ${payload}=    Create Dictionary    ano=${{int($ano)}}    inicio=${inicio}    fim=${fim}
    ${r}=    POST Cadastrar Faixa AIH    ${payload}
    Should Be Equal As Integers    ${r.status_code}    201
    RETURN    ${inicio}    ${fim}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Corpo Do Laudo AIH
    [Documentation]    The report body for an admission.
    [Arguments]    ${internacao}    ${matricula}    ${procedimento}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${payload}=    Create Dictionary    internacaoId=${internacao}    profissionalMatricula=${matricula}
    ...    sinaisSintomas=Febre, tosse produtiva e dispneia.    condicoesJustificativas=Saturação de 88 por cento em ar ambiente.
    ...    diagnosticoInicial=Pneumonia comunitária grave.    cidPrincipal=J18.9    procedimentoId=${procedimento}    clinica=CLINICA_MEDICA
    RETURN    ${payload}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Seed Um Laudo AIH
    [Documentation]    An emergency admission with its report; returns the report, the admission, the requester and the procedure.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${internacao}    ${leito}    ${unidade}    ${setor}    ${paciente}    ${matricula}=    Seed Uma Internacao
    ${procedimento}    ${nome}=    Seed Um Procedimento Para AIH
    ${payload}=    Corpo Do Laudo AIH    ${internacao}    ${matricula}    ${procedimento}
    ${r}=    POST Solicitar AIH    ${payload}
    Should Be Equal As Integers    ${r.status_code}    201
    RETURN    ${r.json()}[uuid]    ${internacao}    ${matricula}    ${procedimento}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Numero AIH Deve Ser Valido
    [Documentation]    13 digits; the last one is the remainder of the first 12 by 11 (10 becomes 0).
    [Arguments]    ${numero}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Should Match Regexp    ${numero}    ^\\d{13}$
    ${dv}=    Evaluate    (int($numero[:12]) % 11) % 10
    Should Be Equal As Integers    ${numero[12]}    ${dv}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Laudo AIH - Solicitar - Status Code 201 E 409
    [Documentation]    The report is requested and enters the queue; a second one for the same admission gives 409.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${laudo}    ${internacao}    ${matricula}    ${procedimento}=    Seed Um Laudo AIH
    ${d}=    GET Laudo AIH    ${laudo}
    Should Be Equal As Integers    ${d.status_code}    200
    Should Be Equal    ${d.json()}[status]    SOLICITADO
    Should Be Equal    ${d.json()}[carater]    URGENCIA
    ${fila}=    GET Fila AIH
    ${ids}=    Evaluate    [l['uuid'] for l in $fila.json()]
    Should Contain    ${ids}    ${laudo}
    ${payload}=    Corpo Do Laudo AIH    ${internacao}    ${matricula}    ${procedimento}
    ${r}=    POST Solicitar AIH    ${payload}
    Should Be Equal As Integers    ${r.status_code}    409
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Laudo AIH - Devolver E Corrigir
    [Documentation]    Returning needs a reason (400); the corrected report goes back to the queue.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${laudo}    ${internacao}    ${matricula}    ${procedimento}=    Seed Um Laudo AIH
    ${autorizador}=    Seed A Profissional Matricula
    ${sem}=    Create Dictionary    profissionalMatricula=${autorizador}
    ${r}=    POST Decidir AIH    ${laudo}    devolucao    ${sem}
    Should Be Equal As Integers    ${r.status_code}    400
    ${com}=    Create Dictionary    profissionalMatricula=${autorizador}    motivo=Descreva a gasometria.
    ${r}=    POST Decidir AIH    ${laudo}    devolucao    ${com}
    Should Be Equal As Integers    ${r.status_code}    200
    Should Be Equal    ${r.json()}[status]    DEVOLVIDO
    ${payload}=    Corpo Do Laudo AIH    ${internacao}    ${matricula}    ${procedimento}
    Set To Dictionary    ${payload}    resultadosExames=Gasometria com hipoxemia.
    ${c}=    PUT Corrigir AIH    ${laudo}    ${payload}
    Should Be Equal As Integers    ${c.status_code}    200
    Should Be Equal    ${c.json()}[status]    SOLICITADO
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Laudo AIH - Autorizar Com Numero
    [Documentation]    The requester cannot authorize (422); another doctor authorizes with a valid AIH number, copied to the admission.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Seed Uma Faixa AIH
    ${laudo}    ${internacao}    ${matricula}    ${procedimento}=    Seed Um Laudo AIH
    ${proprio}=    Create Dictionary    profissionalMatricula=${matricula}
    ${r}=    POST Decidir AIH    ${laudo}    autorizacao    ${proprio}
    Should Be Equal As Integers    ${r.status_code}    422
    ${autorizador}=    Seed A Profissional Matricula
    ${outro}=    Create Dictionary    profissionalMatricula=${autorizador}
    ${r}=    POST Decidir AIH    ${laudo}    autorizacao    ${outro}
    Should Be Equal As Integers    ${r.status_code}    200
    Should Be Equal    ${r.json()}[status]    AUTORIZADO
    Numero AIH Deve Ser Valido    ${r.json()}[numeroAih]
    Get Mais Saude Publica Session
    ${i}=    GET On Session    msp_session    ${BASE_URL}/internacao/${internacao}
    Should Be Equal    ${i.json()}[numeroAih]    ${r.json()}[numeroAih]
    ${pdf}=    GET Laudo AIH Pdf    ${laudo}
    Should Be Equal As Integers    ${pdf.status_code}    200
    Should Start With    ${pdf.headers}[Content-Type]    application/pdf
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Laudo AIH - Negar E Cancelar
    [Documentation]    Denying needs a reason; a denied report cannot be cancelled (422); a requested one is cancelled with a reason.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${laudo}    ${internacao}    ${matricula}    ${procedimento}=    Seed Um Laudo AIH
    ${autorizador}=    Seed A Profissional Matricula
    ${nega}=    Create Dictionary    profissionalMatricula=${autorizador}    motivo=Sem critério de internação.
    ${r}=    POST Decidir AIH    ${laudo}    negacao    ${nega}
    Should Be Equal As Integers    ${r.status_code}    200
    Should Be Equal    ${r.json()}[status]    NEGADO
    ${cancela}=    Create Dictionary    profissionalMatricula=${matricula}    motivo=Engano.
    ${r}=    POST Decidir AIH    ${laudo}    cancelamento    ${cancela}
    Should Be Equal As Integers    ${r.status_code}    422
    ${outro}    ${internacao2}    ${matricula2}    ${procedimento2}=    Seed Um Laudo AIH
    ${cancela2}=    Create Dictionary    profissionalMatricula=${matricula2}    motivo=Paciente errado.
    ${r}=    POST Decidir AIH    ${outro}    cancelamento    ${cancela2}
    Should Be Equal As Integers    ${r.status_code}    200
    Should Be Equal    ${r.json()}[status]    CANCELADO
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Laudo AIH - Faixa - Status Code 400 E 409
    [Documentation]    Start after the end gives 400; an overlapping range gives 409.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${inicio}    ${fim}=    Seed Uma Faixa AIH
    ${ano}=    Get Current Date    result_format=%Y
    ${sobreposta}=    Create Dictionary    ano=${{int($ano)}}    inicio=${fim}    fim=${{int($fim) + 5}}
    ${r}=    POST Cadastrar Faixa AIH    ${sobreposta}
    Should Be Equal As Integers    ${r.status_code}    409
    ${invertida}=    Create Dictionary    ano=${{int($ano)}}    inicio=${{int($fim) + 100}}    fim=${{int($fim) + 50}}
    ${r}=    POST Cadastrar Faixa AIH    ${invertida}
    Should Be Equal As Integers    ${r.status_code}    400
    ${p}=    GET Pendencias AIH
    Should Be Equal As Integers    ${p.status_code}    200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Laudo AIH - By Cenario
    [Documentation]    Executes the Laudo AIH scenario for the given case.
    [Arguments]    ${caso}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    IF    '${caso}' == 'SOLICITAR'
        Laudo AIH - Solicitar - Status Code 201 E 409
    ELSE IF    '${caso}' == 'DEVOLVER_CORRIGIR'
        Laudo AIH - Devolver E Corrigir
    ELSE IF    '${caso}' == 'AUTORIZAR'
        Laudo AIH - Autorizar Com Numero
    ELSE IF    '${caso}' == 'NEGAR_CANCELAR'
        Laudo AIH - Negar E Cancelar
    ELSE IF    '${caso}' == 'FAIXA'
        Laudo AIH - Faixa - Status Code 400 E 409
    ELSE
        Fail    Caso '${caso}' não suportado no cenário Laudo AIH.
    END
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
