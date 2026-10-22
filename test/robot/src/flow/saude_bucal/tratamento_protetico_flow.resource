*** Settings ***
Library     String
Library     Collections
Resource    ../../pom/saude_bucal/tratamento_protetico_pom.resource
Resource    ./odontograma_flow.resource
Resource    ../common/schema_validation_flow.resource
Resource    ../../resource/config/schema/schema.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
Abrir Protese
    [Documentation]    Opens a prosthesis in a new dental-care visit; returns the response, the patient and the dentist.
    [Arguments]    ${tipo}    ${arcada}=${None}    ${dente}=${None}    ${externo}=${None}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${atendimento}    ${paciente}    ${matricula}=    Seed Atendimento Odontologico
    ${payload}=    Create Dictionary    atendimentoId=${atendimento}    profissionalMatricula=${matricula}    tipo=${tipo}
    IF    $arcada is not None    Set To Dictionary    ${payload}    arcada=${arcada}
    IF    $dente is not None    Set To Dictionary    ${payload}    dente=${dente}
    IF    $externo is not None    Set To Dictionary    ${payload}    laboratorioExterno=${externo}
    ${r}=    POST Tratamento Protetico    ${payload}
    RETURN    ${r}    ${paciente}    ${matricula}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Etapa
    [Documentation]    Registers a stage by the dentist.
    [Arguments]    ${id}    ${etapa}    ${matricula}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${payload}=    Create Dictionary    etapa=${etapa}    profissionalMatricula=${matricula}
    ${r}=    POST Etapa Protetica    ${id}    ${payload}
    RETURN    ${r}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Tratamento Protetico - Abrir - Status Code 201
    [Documentation]    Opens an upper complete denture; the next stage is the impression.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${r}    ${paciente}    ${matricula}=    Abrir Protese    TOTAL    SUPERIOR
    Should Be Equal As Integers    ${r.status_code}    201
    Validate Object Schema    ${r.json()}    ${TRATAMENTO_PROTETICO_SCHEMA}
    Should Be Equal As Strings    ${r.json()}[proximasEtapas]    ['MOLDAGEM']
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Tratamento Protetico - Sem Arcada - Status Code 400
    [Documentation]    The complete denture needs the arch.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${r}    ${paciente}    ${matricula}=    Abrir Protese    TOTAL
    Should Be Equal As Integers    ${r.status_code}    400
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Tratamento Protetico - Fora Da Ordem - Status Code 422
    [Documentation]    The try-in comes after the impression.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${r}    ${paciente}    ${matricula}=    Abrir Protese    TOTAL    INFERIOR
    ${e}=    Etapa    ${r.json()}[uuid]    PROVA    ${matricula}
    Should Be Equal As Integers    ${e.status_code}    422
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Tratamento Protetico - Entrega Da Unitaria - Status Code 201
    [Documentation]    Impression, laboratory and delivery: the crown on 46 becomes Prosthesis in the odontogram.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${r}    ${paciente}    ${matricula}=    Abrir Protese    UNITARIA    ${None}    ${46}    Laboratório Sorriso
    ${id}=    Set Variable    ${r.json()}[uuid]
    Etapa    ${id}    MOLDAGEM    ${matricula}
    Etapa    ${id}    LABORATORIO    ${matricula}
    ${e}=    Etapa    ${id}    ENTREGA    ${matricula}
    Should Be Equal As Integers    ${e.status_code}    201
    Should Not Be Equal    ${e.json()}[dataEntrega]    ${None}
    ${o}=    GET Odontograma    ${paciente}
    Should Be Equal    ${o.json()}[dentes][0][condicaoDente][condicaoCodigo]    PROTESE
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Tratamento Protetico - Concluir Antes Da Entrega - Status Code 422
    [Documentation]    Concluding needs the delivery.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${r}    ${paciente}    ${matricula}=    Abrir Protese    PARCIAL_REMOVIVEL    SUPERIOR
    ${c}=    POST Conclusao Protese    ${r.json()}[uuid]
    Should Be Equal As Integers    ${c.status_code}    422
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Tratamento Protetico - Cancelar Sem Motivo - Status Code 400
    [Documentation]    Cancelling needs the reason.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${r}    ${paciente}    ${matricula}=    Abrir Protese    TOTAL    SUPERIOR
    ${vazio}=    Create Dictionary
    ${c}=    POST Cancelamento Protese    ${r.json()}[uuid]    ${vazio}
    Should Be Equal As Integers    ${c.status_code}    400
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Tratamento Protetico - By HTTP Status Code
    [Documentation]    Executes the SB4b scenario for the given HTTP status code.
    [Arguments]    ${meuHTTP}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Run Keyword If    '${meuHTTP}' == '201-abrir'    Tratamento Protetico - Abrir - Status Code 201
    ...    ELSE IF    '${meuHTTP}' == '400-arcada'    Tratamento Protetico - Sem Arcada - Status Code 400
    ...    ELSE IF    '${meuHTTP}' == '422-ordem'    Tratamento Protetico - Fora Da Ordem - Status Code 422
    ...    ELSE IF    '${meuHTTP}' == '201-entrega'    Tratamento Protetico - Entrega Da Unitaria - Status Code 201
    ...    ELSE IF    '${meuHTTP}' == '422-concluir'    Tratamento Protetico - Concluir Antes Da Entrega - Status Code 422
    ...    ELSE IF    '${meuHTTP}' == '400-cancelar'    Tratamento Protetico - Cancelar Sem Motivo - Status Code 400
    ...    ELSE    Fail    HTTP status code '${meuHTTP}' is not supported for Tratamento Protetico test scenario.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
