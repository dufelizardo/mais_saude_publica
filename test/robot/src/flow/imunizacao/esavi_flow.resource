*** Settings ***
Library     String
Library     Collections
Resource    ../../pom/imunizacao/vacinacao_pom.resource
Resource    ../../pom/vigilancia/vigilancia_pom.resource
Resource    vacinacao_flow.resource
Resource    ../vigilancia/vigilancia_flow.resource
Resource    ../common/schema_validation_flow.resource
Resource    ../../resource/config/schema/schema.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
    O adulto com uma dose de dT transcrita: o ESAVI parte da dose, na Vigilância (ADR-0150).
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
Transcrever Dose Do Adulto
    [Documentation]    Registers a dT booster transcribed from the card, 30 days ago; returns its uuid.
    [Arguments]    ${dados}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${imuno}=    Id Do Imunobiologico    DT
    ${quando}=    Evaluate    (datetime.date.today() - datetime.timedelta(days=30)).isoformat() + 'T09:00:00'    modules=datetime
    ${payload}=    Create Dictionary    pacienteId=${dados}[paciente]    imunobiologicoId=${imuno}    dose=REFORCO    unidadeId=${dados}[unidade]
    ...    origem=TRANSCRITA    aplicadaEm=${quando}
    ${r}=    POST Registro Vacinacao    ${payload}
    Should Be Equal As Integers    ${r.status_code}    201
    RETURN    ${r.json()}[uuid]
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Payload Esavi
    [Documentation]    An ESAVI notification payload for the dose, with the form answers.
    [Arguments]    ${dados}    ${dose}    ${gravidade}=GRAVE
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${esavi}=    Id Do Agravo    ESAVI
    ${manifestacoes}=    Create List    Reação local
    ${respostas}=    Create Dictionary    manifestacoes=${manifestacoes}
    ${doses}=    Create List    ${dose}
    ${payload}=    Create Dictionary    pacienteId=${dados}[paciente]    agravoId=${esavi}    unidadeId=${dados}[unidade]    origem=VACINACAO
    ...    respostas=${respostas}    doseIds=${doses}    gravidade=${gravidade}
    RETURN    ${payload}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Esavi Notificado
    [Documentation]    Seeds a patient with a dT dose and its ESAVI; returns the data, the dose and the notification.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${dados}=    Seed Vacinacao
    ${dose}=    Transcrever Dose Do Adulto    ${dados}
    ${payload}=    Payload Esavi    ${dados}    ${dose}
    ${n}=    POST Notificacao    ${payload}
    Should Be Equal As Integers    ${n.status_code}    201
    RETURN    ${dados}    ${dose}    ${n.json()}[uuid]
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Encerrar Com Contraindicacao
    [Documentation]    Closes the ESAVI as product reaction, contraindicating the next doses.
    [Arguments]    ${notificacao}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${payload}=    Create Dictionary    evolucao=CURA    causalidade=A1_REACAO_PRODUTO    conduta=CONTRAINDICAR
    ${r}=    POST Acao Notificacao    ${notificacao}    encerramento    ${payload}
    RETURN    ${r}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Esavi - Notificacao Pela Dose - Status Code 201
    [Documentation]    The ESAVI points to the dose, with the severity and the schema.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${dados}=    Seed Vacinacao
    ${dose}=    Transcrever Dose Do Adulto    ${dados}
    ${payload}=    Payload Esavi    ${dados}    ${dose}
    ${n}=    POST Notificacao    ${payload}
    Should Be Equal As Integers    ${n.status_code}    201
    Validate Object Schema    ${n.json()}    ${NOTIFICACAO_ESAVI_SCHEMA}
    Validate Array Schema    ${n.json()}[doses]    ${DOSE_NOTIFICADA_SCHEMA}
    Should Be Equal    ${n.json()}[gravidadeEsavi]    GRAVE
    Should Be Equal    ${n.json()}[doses][0][uuid]    ${dose}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Esavi - Sem Dose - Status Code 400
    [Documentation]    The ESAVI needs the dose.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${dados}=    Seed Vacinacao
    ${dose}=    Transcrever Dose Do Adulto    ${dados}
    ${payload}=    Payload Esavi    ${dados}    ${dose}
    ${vazia}=    Create List
    Set To Dictionary    ${payload}    doseIds=${vazia}
    ${n}=    POST Notificacao    ${payload}
    Should Be Equal As Integers    ${n.status_code}    400
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Esavi - Sem Gravidade - Status Code 400
    [Documentation]    The ESAVI needs the severity.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${dados}=    Seed Vacinacao
    ${dose}=    Transcrever Dose Do Adulto    ${dados}
    ${payload}=    Payload Esavi    ${dados}    ${dose}
    Remove From Dictionary    ${payload}    gravidade
    ${n}=    POST Notificacao    ${payload}
    Should Be Equal As Integers    ${n.status_code}    400
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Esavi - Dose De Outro Paciente - Status Code 422
    [Documentation]    The dose must belong to the notified patient.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${dados}=    Seed Vacinacao
    ${outros}=    Seed Vacinacao
    ${dose}=    Transcrever Dose Do Adulto    ${outros}
    ${payload}=    Payload Esavi    ${dados}    ${dose}
    ${n}=    POST Notificacao    ${payload}
    Should Be Equal As Integers    ${n.status_code}    422
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Esavi - Encerramento Sem Causalidade - Status Code 400
    [Documentation]    Closing the ESAVI needs the causality and the conduct.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${dados}    ${dose}    ${notificacao}=    Esavi Notificado
    ${payload}=    Create Dictionary    evolucao=CURA
    ${r}=    POST Acao Notificacao    ${notificacao}    encerramento    ${payload}
    Should Be Equal As Integers    ${r.status_code}    400
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Esavi - Encerramento Com Contraindicacao - Status Code 200
    [Documentation]    The causality gives the final classification; contraindicating shows on the card.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${dados}    ${dose}    ${notificacao}=    Esavi Notificado
    ${r}=    Encerrar Com Contraindicacao    ${notificacao}
    Should Be Equal As Integers    ${r.status_code}    200
    Validate Object Schema    ${r.json()}    ${NOTIFICACAO_ESAVI_SCHEMA}
    Should Be Equal    ${r.json()}[classificacaoFinal]    CONFIRMADO
    ${c}=    GET Caderneta Vacinacao    ${dados}[paciente]
    Should Be Equal    ${c.json()}[contraindicacoes][0][sigla]    dT
    List Should Contain Value    ${c.json()}[dosesNotificadas]    ${dose}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Esavi - Dose Contraindicada - Status Code 422
    [Documentation]    The contraindicated vaccine only with the justification.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${dados}    ${dose}    ${notificacao}=    Esavi Notificado
    Encerrar Com Contraindicacao    ${notificacao}
    ${payload}=    Dose Aplicada    ${dados}    DT    REFORCO
    ${r}=    POST Registro Vacinacao    ${payload}
    Should Be Equal As Integers    ${r.status_code}    422
    Set To Dictionary    ${payload}    justificativa=Avaliado no CRIE
    ${ok}=    POST Registro Vacinacao    ${payload}
    Should Be Equal As Integers    ${ok.status_code}    201
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Esavi - Sugestao De Erro De Imunizacao - Status Code 200
    [Documentation]    The dose out of the calendar becomes a suggestion of immunization error.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${dados}=    Seed Vacinacao
    ${payload}=    Dose Aplicada    ${dados}    BCG    UNICA
    Set To Dictionary    ${payload}    justificativa=Sem registro anterior
    ${r}=    POST Registro Vacinacao    ${payload}
    Should Be Equal As Integers    ${r.status_code}    201
    ${s}=    GET Sugestoes
    Should Be Equal As Integers    ${s.status_code}    200
    ${linha}=    Evaluate    [x for x in $s.json() if x["referenciaId"] == $r.json()["uuid"]]
    Length Should Be    ${linha}    1
    Should Be Equal    ${linha}[0][agravoCodigo]    ERRO_IMUNIZACAO
    Should Be Equal    ${linha}[0][origem]    VACINACAO
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Esavi - By HTTP Status Code
    [Documentation]    Executes the I4 scenario for the given HTTP status code.
    [Arguments]    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Run Keyword If    '${meuHTTP}' == '201-esavi'    Esavi - Notificacao Pela Dose - Status Code 201
    ...    ELSE IF    '${meuHTTP}' == '400-sem-dose'    Esavi - Sem Dose - Status Code 400
    ...    ELSE IF    '${meuHTTP}' == '400-sem-gravidade'    Esavi - Sem Gravidade - Status Code 400
    ...    ELSE IF    '${meuHTTP}' == '422-outro-paciente'    Esavi - Dose De Outro Paciente - Status Code 422
    ...    ELSE IF    '${meuHTTP}' == '400-sem-causalidade'    Esavi - Encerramento Sem Causalidade - Status Code 400
    ...    ELSE IF    '${meuHTTP}' == '200-contraindicacao'    Esavi - Encerramento Com Contraindicacao - Status Code 200
    ...    ELSE IF    '${meuHTTP}' == '422-contraindicada'    Esavi - Dose Contraindicada - Status Code 422
    ...    ELSE IF    '${meuHTTP}' == '200-sugestao-erro'    Esavi - Sugestao De Erro De Imunizacao - Status Code 200
    ...    ELSE    Fail    HTTP status code '${meuHTTP}' is not supported for Esavi test scenario.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
