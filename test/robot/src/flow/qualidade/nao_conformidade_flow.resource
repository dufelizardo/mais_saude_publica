*** Settings ***
Library     String
Library     Collections
Library     DateTime
Resource    ../../pom/qualidade/nao_conformidade_pom.resource
Resource    ../setor/create_setor/create_setor_flow.resource
Resource    ../profissional/get_profissional_by_matricula/get_profissional_by_matricula_flow.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
    A não conformidade, o plano 5W2H e a verificação de eficácia (ADR-0204).
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
Seed Nao Conformidade
    [Documentation]    A nonconformity from an internal audit; returns the uuid, the unit and the responsible registration.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${unidade}=    Seed A Unidade De Saude Uuid
    ${matricula}=    Seed A Profissional Matricula
    ${payload}=    Create Dictionary    unidadeId=${unidade}    origem=AUDITORIA_INTERNA    descricao=Medicamento vencido no carrinho de emergência.
    ...    gravidade=MAIOR    responsavelMatricula=${matricula}
    ${r}=    POST Nao Conformidade    ${payload}
    Should Be Equal As Integers    ${r.status_code}    201
    RETURN    ${r.json()}[uuid]    ${unidade}    ${matricula}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Adicionar Acao
    [Documentation]    Adds an action of the plan; returns its uuid.
    [Arguments]    ${nc}    ${matricula}    ${oque}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${prazo}=    Get Current Date    increment=10 days    result_format=%Y-%m-%d
    ${payload}=    Create Dictionary    descricao=${oque}    justificativa=Eliminar a causa.    responsavelMatricula=${matricula}    prazo=${prazo}
    ${r}=    POST Acao Nao Conformidade    ${nc}    acao    ${payload}
    Should Be Equal As Integers    ${r.status_code}    201
    ${ids}=    Evaluate    [a['uuid'] for a in $r.json()['acoes'] if a['oQue'] == $oque]
    RETURN    ${ids}[-1]
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Concluir Acao
    [Documentation]    Concludes the action with the evidence.
    [Arguments]    ${acao}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${payload}=    Create Dictionary    situacao=CONCLUIDA    evidencia=Feito e conferido.
    ${r}=    POST Andamento Acao Qualidade    ${acao}    ${payload}
    Should Be Equal As Integers    ${r.status_code}    200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Nao Conformidade - Origem Indicador - Status Code 400
    [Documentation]    The indicator origin waits for Q3.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${unidade}=    Seed A Unidade De Saude Uuid
    ${matricula}=    Seed A Profissional Matricula
    ${payload}=    Create Dictionary    unidadeId=${unidade}    origem=INDICADOR    descricao=x    gravidade=MENOR    responsavelMatricula=${matricula}
    ${r}=    POST Nao Conformidade    ${payload}
    Should Be Equal As Integers    ${r.status_code}    400
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Nao Conformidade - Ciclo Eficaz - Status Code 200
    [Documentation]    Analysis, one action concluded, effective verification closes it.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${nc}    ${unidade}    ${matricula}=    Seed Nao Conformidade
    ${categorias}=    Create List    METODO
    ${analise}=    Create Dictionary    analiseCausa=Sem conferência diária.    categorias=${categorias}
    ${r}=    POST Acao Nao Conformidade    ${nc}    analise    ${analise}
    Should Be Equal As Integers    ${r.status_code}    200
    ${acao}=    Adicionar Acao    ${nc}    ${matricula}    Checklist diário do carrinho
    Concluir Acao    ${acao}
    ${verificacao}=    Create Dictionary    eficaz=${True}
    ${r}=    POST Acao Nao Conformidade    ${nc}    verificacao    ${verificacao}
    Should Be Equal As Integers    ${r.status_code}    200
    Should Be Equal    ${r.json()}[situacao]    ENCERRADA
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Nao Conformidade - Nao Eficaz Abre Ciclo - Status Code 200
    [Documentation]    An ineffective verification opens the next cycle.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${nc}    ${unidade}    ${matricula}=    Seed Nao Conformidade
    ${acao}=    Adicionar Acao    ${nc}    ${matricula}    Treinar a equipe
    Concluir Acao    ${acao}
    ${verificacao}=    Create Dictionary    eficaz=${False}
    ${r}=    POST Acao Nao Conformidade    ${nc}    verificacao    ${verificacao}
    Should Be Equal As Integers    ${r.status_code}    400
    Set To Dictionary    ${verificacao}    observacao=Voltou a ocorrer.
    ${r}=    POST Acao Nao Conformidade    ${nc}    verificacao    ${verificacao}
    Should Be Equal As Integers    ${r.status_code}    200
    Should Be Equal    ${r.json()}[situacao]    EM_TRATAMENTO
    Should Be Equal As Integers    ${r.json()}[ciclo]    2
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Nao Conformidade - Painel - Status Code 200
    [Documentation]    The panel counts the nonconformities.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${nc}    ${unidade}    ${matricula}=    Seed Nao Conformidade
    ${r}=    GET Painel Nao Conformidades
    Should Be Equal As Integers    ${r.status_code}    200
    Should Be True    ${r.json()}[total] >= 1
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Nao Conformidade - By Cenario
    [Documentation]    Executes the nonconformity scenario for the given case.
    [Arguments]    ${caso}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    IF    '${caso}' == 'INDICADOR'
        Nao Conformidade - Origem Indicador - Status Code 400
    ELSE IF    '${caso}' == 'EFICAZ'
        Nao Conformidade - Ciclo Eficaz - Status Code 200
    ELSE IF    '${caso}' == 'NAO_EFICAZ'
        Nao Conformidade - Nao Eficaz Abre Ciclo - Status Code 200
    ELSE IF    '${caso}' == 'PAINEL'
        Nao Conformidade - Painel - Status Code 200
    ELSE
        Fail    Caso '${caso}' não suportado no cenário Não Conformidade.
    END
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
