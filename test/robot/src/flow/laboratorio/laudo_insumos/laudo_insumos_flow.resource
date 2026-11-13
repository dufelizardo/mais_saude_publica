*** Settings ***
Library     String
Library     Collections
Resource    ../../../pom/laboratorio/laudo_insumos/laudo_insumos_pom.resource
Resource    ../../../pom/estoque/estoque_pom.resource
Resource    ../../estoque/estoque_flow.resource
Resource    ../faixa_referencia/faixa_referencia_flow.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
    Laudo em PDF com assinatura eletrônica interna, código de verificação e verificação pública, e os insumos de coleta
    baixados do Estoque (ADR-0183). O mesmo profissional semeado pede, coleta, analisa e libera.
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
Seed A Pedido Liberado
    [Documentation]    A released result of the adult female range exam; returns pedidoId, itemId and matricula.
    [Arguments]    ${liberar}=${True}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${exameId}    ${nome}    ${faixaId}=    Seed A Exame Com Faixa Feminina
    ${pacienteId}    ${cpf}    ${cartaoSus}    ${pacienteNome}=    Seed A Paciente
    ${unidadeId}=    Seed A Unidade De Saude Uuid
    ${matricula}=    Seed A Profissional Matricula
    ${pedidoId}    ${itemId}=    Registrar Resultado Do Exame    ${pacienteId}    ${exameId}    ${unidadeId}    ${matricula}    ${12.4}    liberar=${liberar}
    RETURN    ${pedidoId}    ${itemId}    ${matricula}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Laudo - PDF - Status Code 200
    [Documentation]    The PDF comes with the verification code, the public check is valid and the schema holds.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${pedidoId}    ${itemId}    ${matricula}=    Seed A Pedido Liberado
    ${pdf}=    GET Laudo Pdf    ${pedidoId}
    Should Be Equal As Integers    ${pdf.status_code}    200
    Should Start With    ${pdf.headers}[Content-Type]    application/pdf
    ${inicio}=    Evaluate    $pdf.content[:5].decode('latin-1')
    Should Be Equal    ${inicio}    %PDF-
    ${codigo}=    Set Variable    ${pdf.headers}[X-Codigo-Verificacao]
    Should Match Regexp    ${codigo}    ^[A-Z2-9]{4}-[A-Z2-9]{4}-[A-Z2-9]{4}$
    ${hash}=    Evaluate    hashlib.sha256($pdf.content).hexdigest()    modules=hashlib
    ${v}=    GET Verificar Laudo    ${codigo}
    Should Be Equal As Integers    ${v.status_code}    200
    Should Be True    ${v.json()}[valido]
    Should Be Equal    ${v.json()}[hashPdf]    ${hash}
    Validate Object Schema    ${v.json()}    ${VERIFICACAO_LAUDO_SCHEMA}
    ${emissoes}=    GET Emissoes Laudo    ${pedidoId}
    Length Should Be    ${emissoes.json()}    1
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Laudo - PDF - Status Code 422 - Sem Liberado
    [Documentation]    No released result, expecting HTTP 422.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${pedidoId}    ${itemId}    ${matricula}=    Seed A Pedido Liberado    liberar=${False}
    ${pdf}=    GET Laudo Pdf    ${pedidoId}
    Should Be Equal As Integers    ${pdf.status_code}    422
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Laudo - Versao Substituida
    [Documentation]    After a correction, a new version is emitted and the old code shows as replaced.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${pedidoId}    ${itemId}    ${matricula}=    Seed A Pedido Liberado
    ${primeiro}=    GET Laudo Pdf    ${pedidoId}
    ${codigo}=    Set Variable    ${primeiro.headers}[X-Codigo-Verificacao]
    ${ret}=    Create Dictionary    profissionalMatricula=${matricula}    valorNumerico=${12.8}    motivo=Diluição corrigida.
    ${r}=    POST Acao Item Exame    ${itemId}    retificacao    ${ret}
    Should Be Equal As Integers    ${r.status_code}    200
    ${segundo}=    GET Laudo Pdf    ${pedidoId}
    Should Not Be Equal    ${segundo.headers}[X-Codigo-Verificacao]    ${codigo}
    ${v}=    GET Verificar Laudo    ${codigo}
    Should Be True    ${v.json()}[substituido]
    Should Be Equal As Integers    ${v.json()}[versaoVigente]    2
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Laudo - Verificar - Status Code 404
    [Documentation]    Unknown code, expecting HTTP 404.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${v}=    GET Verificar Laudo    ZZZZ-ZZZZ-ZZZZ
    Should Be Equal As Integers    ${v.status_code}    404
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Laudo - Insumos - Baixa Na Coleta
    [Documentation]    The supplies of the exam leave the stock of the collection unit.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${material}    ${materialNome}=    Seed Material Estoque    ${False}
    ${exameId}    ${nome}    ${faixaId}=    Seed A Exame Com Faixa Feminina
    ${i}=    Create Dictionary    materialId=${material}    quantidade=${2}
    ${lista}=    Create List    ${i}
    ${r}=    PUT Insumos Exame    ${exameId}    ${lista}
    Should Be Equal As Integers    ${r.status_code}    200
    Should Be Equal    ${r.json()}[insumos][0][materialId]    ${material}
    ${pacienteId}    ${cpf}    ${cartaoSus}    ${pacienteNome}=    Seed A Paciente
    ${unidadeId}=    Seed A Unidade De Saude Uuid
    ${matricula}=    Seed A Profissional Matricula
    ${entrada}=    Create Dictionary    unidadeId=${unidadeId}    materialId=${material}    quantidade=${5}    origem=SALDO_INICIAL
    ${e}=    POST Entrada Estoque    ${entrada}
    Should Be Equal As Integers    ${e.status_code}    201
    Registrar Resultado Do Exame    ${pacienteId}    ${exameId}    ${unidadeId}    ${matricula}    ${12.4}    liberar=${False}
    ${params}=    Create Dictionary    unidadeId=${unidadeId}
    ${saldo}=    GET Saldo Estoque    ${params}
    ${total}=    Evaluate    [i['total'] for i in $saldo.json()['itens'] if i['materialId'] == $material][0]
    Should Be Equal As Integers    ${total}    3
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Laudo - Insumos - Status Code 400 - Repetido
    [Documentation]    The same material twice, expecting HTTP 400.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${material}    ${materialNome}=    Seed Material Estoque    ${False}
    ${exameId}    ${nome}=    Seed A Exame Laboratorial
    ${i}=    Create Dictionary    materialId=${material}    quantidade=${1}
    ${lista}=    Create List    ${i}    ${i}
    ${r}=    PUT Insumos Exame    ${exameId}    ${lista}
    Should Be Equal As Integers    ${r.status_code}    400
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Laudo - Insumos - Consumo Sem Saldo
    [Documentation]    Collection without stock becomes an alert, which the stock keeper checks.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${material}    ${materialNome}=    Seed Material Estoque    ${False}
    ${exameId}    ${nome}    ${faixaId}=    Seed A Exame Com Faixa Feminina
    ${i}=    Create Dictionary    materialId=${material}    quantidade=${1}
    ${lista}=    Create List    ${i}
    PUT Insumos Exame    ${exameId}    ${lista}
    ${pacienteId}    ${cpf}    ${cartaoSus}    ${pacienteNome}=    Seed A Paciente
    ${unidadeId}=    Seed A Unidade De Saude Uuid
    ${matricula}=    Seed A Profissional Matricula
    Registrar Resultado Do Exame    ${pacienteId}    ${exameId}    ${unidadeId}    ${matricula}    ${12.4}    liberar=${False}
    ${params}=    Create Dictionary    unidadeId=${unidadeId}
    ${saldo}=    GET Saldo Estoque    ${params}
    ${consumo}=    Evaluate    [c for c in $saldo.json()['consumosSemSaldo'] if c['materialId'] == $material][0]
    Should Be Equal As Integers    ${consumo}[quantidadeFaltante]    1
    ${ok}=    POST Conferir Consumo Sem Saldo    ${consumo}[uuid]
    Should Be Equal As Integers    ${ok.status_code}    204
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Laudo Insumos - By Cenario
    [Documentation]    Executes the Laudo e Insumos scenario for the given case.
    [Arguments]    ${caso}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    IF    '${caso}' == 'PDF'
        Laudo - PDF - Status Code 200
    ELSE IF    '${caso}' == 'PDF_422'
        Laudo - PDF - Status Code 422 - Sem Liberado
    ELSE IF    '${caso}' == 'SUBSTITUIDO'
        Laudo - Versao Substituida
    ELSE IF    '${caso}' == 'VERIFICAR_404'
        Laudo - Verificar - Status Code 404
    ELSE IF    '${caso}' == 'INSUMOS_COLETA'
        Laudo - Insumos - Baixa Na Coleta
    ELSE IF    '${caso}' == 'INSUMOS_400'
        Laudo - Insumos - Status Code 400 - Repetido
    ELSE IF    '${caso}' == 'CONSUMO_SEM_SALDO'
        Laudo - Insumos - Consumo Sem Saldo
    ELSE
        Fail    Caso '${caso}' não suportado no cenário Laudo e Insumos.
    END
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
