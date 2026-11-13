*** Settings ***
Library     String
Library     Collections
Resource    ../../pom/vigilancia/vigilancia_sanitaria_pom.resource
Resource    ../profissional/get_profissional_by_matricula/get_profissional_by_matricula_flow.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
    Vigilância Sanitária (ADR-0199): o estabelecimento e o risco, a licença e a dispensa, a denúncia, a inspeção com o roteiro e o
    retorno, o termo em PDF e as pendências.
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
Gerar Cnpj Valido
    [Documentation]    A random CNPJ with valid check digits.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${base}=    Evaluate    '%08d0001' % random.randint(0, 99999999)    modules=random
    ${d1}=    Evaluate    (lambda r: 0 if r < 2 else 11 - r)(sum(int(c) * p for c, p in zip($base, [5,4,3,2,9,8,7,6,5,4,3,2])) % 11)
    ${base}=    Set Variable    ${base}${d1}
    ${d2}=    Evaluate    (lambda r: 0 if r < 2 else 11 - r)(sum(int(c) * p for c, p in zip($base, [6,5,4,3,2,9,8,7,6,5,4,3,2])) % 11)
    ${cnpj}=    Set Variable    ${base}${d2}
    RETURN    ${cnpj}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Seed Estabelecimento Visa
    [Documentation]    An establishment with one activity of the given risk; returns the uuid.
    [Arguments]    ${risco}=MEDIO    ${nome}=${EMPTY}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${cnpj}=    Gerar Cnpj Valido
    ${sufixo}=    Generate Random String    6    [NUMBERS]
    ${nome}=    Set Variable If    '${nome}' == '${EMPTY}'    Robot Visa ${sufixo}    ${nome}
    ${atividade}=    Create Dictionary    cnae=5611201    descricao=Restaurante    risco=${risco}
    ${atividades}=    Create List    ${atividade}
    ${payload}=    Create Dictionary    documento=${cnpj}    razaoSocial=${nome}    endereco=Rua Robot, 10    atividades=${atividades}
    ${r}=    POST Estabelecimento Visa    ${payload}
    Should Be Equal As Integers    ${r.status_code}    201
    RETURN    ${r.json()}[uuid]
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Roteiro Alimentos Visa
    [Documentation]    The current version id of the food services checklist.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${r}=    GET Roteiros Visa
    ${ids}=    Evaluate    [x['versaoId'] for x in $r.json() if x['codigo'] == 'ALIMENTOS_BOAS_PRATICAS']
    RETURN    ${ids}[0]
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Respostas Alimentos Visa
    [Documentation]    All items conforming, except the given non-conforming one.
    [Arguments]    ${nao_conforme}=${EMPTY}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${respostas}=    Evaluate    {c: ('NAO_CONFORME' if c == $nao_conforme else 'CONFORME') for c in ['A1', 'A2', 'A3', 'A4', 'A5', 'A6', 'A7', 'A8']}
    RETURN    ${respostas}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Visa - Estabelecimento E Dispensa - Status Code 201
    [Documentation]    A risk I establishment receives the licensing exemption, without validity.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${id}=    Seed Estabelecimento Visa    BAIXO
    ${payload}=    Create Dictionary
    ${r}=    POST Licenca Visa    ${id}    ${payload}
    Should Be Equal As Integers    ${r.status_code}    201
    Should Be True    ${r.json()}[dispensa]
    Should Be Equal    ${r.json()}[situacao]    DISPENSADA
    Should Be Equal    ${r.json()}[validade]    ${None}
    ${r}=    POST Licenca Visa    ${id}    ${payload}
    Should Be Equal As Integers    ${r.status_code}    409
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Visa - Cnpj Invalido - Status Code 400
    [Documentation]    A CNPJ with a wrong check digit is refused.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${atividade}=    Create Dictionary    cnae=5611201    descricao=Restaurante    risco=MEDIO
    ${atividades}=    Create List    ${atividade}
    ${payload}=    Create Dictionary    documento=11.222.333/0001-80    razaoSocial=Robot Visa Invalido    endereco=Rua Robot    atividades=${atividades}
    ${r}=    POST Estabelecimento Visa    ${payload}
    Should Be Equal As Integers    ${r.status_code}    400
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Visa - Risco Alto Pede Inspecao - Status Code 422 E 201
    [Documentation]    Risk III: the license waits for a satisfactory inspection.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${id}=    Seed Estabelecimento Visa    ALTO
    ${matricula}=    Seed A Profissional Matricula
    ${vazio}=    Create Dictionary
    ${r}=    POST Licenca Visa    ${id}    ${vazio}
    Should Be Equal As Integers    ${r.status_code}    422
    ${roteiro}=    Roteiro Alimentos Visa
    ${respostas}=    Respostas Alimentos Visa
    ${fiscais}=    Create List    ${matricula}
    ${payload}=    Create Dictionary    estabelecimentoId=${id}    motivo=LICENCIAMENTO    roteiroVersaoId=${roteiro}    respostas=${respostas}
    ...    resultado=SATISFATORIA    relatorio=Tudo em ordem.    fiscais=${fiscais}
    ${r}=    POST Inspecao Visa    ${payload}
    Should Be Equal As Integers    ${r.status_code}    201
    Should Be Equal As Integers    ${{len($r.json()['hashConteudo'])}}    64
    ${r}=    POST Licenca Visa    ${id}    ${vazio}
    Should Be Equal As Integers    ${r.status_code}    201
    Should Be Equal    ${r.json()}[situacao]    VIGENTE
    Should Not Be Equal    ${r.json()}[inspecaoId]    ${None}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Visa - Denuncia Inspecao E Retorno - Status Code 201
    [Documentation]    The complaint is inspected, found valid, and the return inspection confirms the correction.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${id}=    Seed Estabelecimento Visa
    ${matricula}=    Seed A Profissional Matricula
    ${denuncia}=    Create Dictionary    canal=OUVIDORIA    descricao=Baratas na cozinha    estabelecimentoId=${id}    anonima=${True}
    ${r}=    POST Denuncia Visa    ${denuncia}
    Should Be Equal As Integers    ${r.status_code}    201
    ${den}=    Set Variable    ${r.json()}[uuid]
    ${roteiro}=    Roteiro Alimentos Visa
    ${respostas}=    Respostas Alimentos Visa    A3
    ${fiscais}=    Create List    ${matricula}
    ${irreg}=    Create Dictionary    itemCodigo=A3    descricao=Baratas na área de preparo    gravidade=GRAVE
    ${irregs}=    Create List    ${irreg}
    ${payload}=    Create Dictionary    estabelecimentoId=${id}    motivo=DENUNCIA    denunciaId=${den}    roteiroVersaoId=${roteiro}    respostas=${respostas}
    ...    resultado=INSATISFATORIA    relatorio=Pragas na cozinha.    fiscais=${fiscais}    irregularidades=${irregs}
    ${r}=    POST Inspecao Visa    ${payload}
    Should Be Equal As Integers    ${r.status_code}    201
    ${primeira}=    Set Variable    ${r.json()}[uuid]
    ${irregularidade}=    Set Variable    ${r.json()}[irregularidades][0][uuid]
    ${conclusao}=    Create Dictionary    situacao=PROCEDENTE    conclusao=Constatado.
    ${r}=    POST Conclusao Denuncia Visa    ${den}    ${conclusao}
    Should Be Equal As Integers    ${r.status_code}    200
    Should Be Equal    ${r.json()}[situacao]    PROCEDENTE
    ${corrigidas}=    Create List    ${irregularidade}
    ${retorno}=    Create Dictionary    estabelecimentoId=${id}    motivo=RETORNO    inspecaoOrigemId=${primeira}    resultado=SATISFATORIA
    ...    relatorio=Corrigido.    fiscais=${fiscais}    corrigidas=${corrigidas}
    ${r}=    POST Inspecao Visa    ${retorno}
    Should Be Equal As Integers    ${r.status_code}    201
    ${r}=    GET Estabelecimento Visa    ${id}
    Should Be Equal As Integers    ${r.json()}[estabelecimento][irregularidadesAbertas]    0
    Should Be Equal    ${r.json()}[irregularidades][0][situacao]    CORRIGIDA
    ${r}=    GET Termo Inspecao Visa    ${primeira}
    Should Be Equal As Integers    ${r.status_code}    200
    Should Start With    ${r.headers}[Content-Type]    application/pdf
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Visa - Pendencias - Status Code 200
    [Documentation]    A risk II establishment without license is pending.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${id}=    Seed Estabelecimento Visa    MEDIO
    ${r}=    GET Pendencias Visa
    Should Be Equal As Integers    ${r.status_code}    200
    ${motivos}=    Evaluate    [x['motivo'] for x in $r.json()['licencas'] if x['estabelecimentoId'] == $id]
    Should Be Equal    ${motivos}    ${{['SEM_LICENCA']}}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Visa - By Cenario
    [Documentation]    Executes the sanitary surveillance scenario for the given case.
    [Arguments]    ${caso}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    IF    '${caso}' == 'DISPENSA'
        Visa - Estabelecimento E Dispensa - Status Code 201
    ELSE IF    '${caso}' == 'CNPJ_INVALIDO'
        Visa - Cnpj Invalido - Status Code 400
    ELSE IF    '${caso}' == 'RISCO_ALTO'
        Visa - Risco Alto Pede Inspecao - Status Code 422 E 201
    ELSE IF    '${caso}' == 'DENUNCIA'
        Visa - Denuncia Inspecao E Retorno - Status Code 201
    ELSE IF    '${caso}' == 'PENDENCIAS'
        Visa - Pendencias - Status Code 200
    ELSE
        Fail    Caso '${caso}' não suportado no cenário Vigilância Sanitária.
    END
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
