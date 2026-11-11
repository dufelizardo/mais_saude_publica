*** Settings ***
Library     String
Library     Collections
Resource    ../../pom/comunicacao/educacao_em_saude_pom.resource
Resource    ./comunicacao_flow.resource
Resource    ../saude_bucal/saude_bucal_coletiva_flow.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
    Educação em saúde (ADR-0195): a ação coletiva de Programas ganha tema, conteúdos revisados, materiais e avaliação.
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
Seed Conteudo Com Material
    [Documentation]    An approved content with a booklet; returns the content group and the material.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${uuid}    ${titulo}=    Seed Conteudo Aprovado
    ${lista}=    GET Educacao Generico    educacao-permanente/conteudo
    ${grupo}=    Evaluate    [c['grupoId'] for c in $lista.json() if c['uuid'] == $uuid][0]
    ${m}=    Create Dictionary    conteudoGrupoId=${grupo}    formato=CARTILHA    titulo=Cartilha ${titulo}
    ${r}=    POST Educacao Generico    comunicacao/material    ${m}
    Should Be Equal As Integers    ${r.status_code}    201
    RETURN    ${grupo}    ${r.json()}[uuid]    ${titulo}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Educacao - Detalhe Da Acao
    [Documentation]    Without a theme, 400; the group with the approved content and the booklet; the evaluation only after done (422).
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${acao}    ${paciente}    ${unidade}    ${nome}=    Seed Acao Bucal Coletiva    SAUDE_DA_FAMILIA    ${False}    Praça Robot
    ${grupo}    ${material}    ${titulo}=    Seed Conteudo Com Material
    ${vazio}=    Create Dictionary    tema=${EMPTY}
    ${r}=    PUT Educacao Da Acao    ${acao}    ${vazio}
    Should Be Equal As Integers    ${r.status_code}    400
    ${conteudos}=    Create List    ${grupo}
    ${item}=    Create Dictionary    materialId=${material}    quantidade=${40}
    ${materiais}=    Create List    ${item}
    ${p}=    Create Dictionary    tema=Dengue    conteudoGrupoIds=${conteudos}    materiais=${materiais}
    ${r}=    PUT Educacao Da Acao    ${acao}    ${p}
    Should Be Equal As Integers    ${r.status_code}    200
    Should Be Equal    ${r.json()}[tema]    Dengue
    Should Be Equal As Integers    ${r.json()}[materiais][0][quantidade]    40
    ${av}=    Create Dictionary    participacao=ALTA
    ${r}=    POST Educacao Generico    acao-programatica/${acao}/educacao/avaliacao    ${av}
    Should Be Equal As Integers    ${r.status_code}    422
    ${uso}=    GET Educacao Generico    educacao-permanente/uso-dos-conteudos
    ${n}=    Evaluate    [u['acoesEducativas'] for u in $uso.json() if u['grupoId'] == $grupo][0]
    Should Be Equal As Integers    ${n}    1
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Educacao - By Cenario
    [Documentation]    Executes the health education scenario for the given case.
    [Arguments]    ${caso}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    IF    '${caso}' == 'DETALHE'
        Educacao - Detalhe Da Acao
    ELSE
        Fail    Caso '${caso}' não suportado no cenário Educação em Saúde.
    END
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
