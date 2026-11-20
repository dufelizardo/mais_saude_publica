*** Settings ***
Library     String
Resource    ../../../../pom/ui/administrativo/instituicoes_externas/instituicoes_externas_ui_pom.resource
Resource    ../../../intersetorialidade/intersetorialidade_flow.resource
Resource    ../../../../pom/ui/indicadores/sala_de_situacao_ui_pom.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
    Jornadas da tela Instituições da rede intersetorial (ADR-0154) e do painel da rede (ADR-0211).
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
UI Instituicoes Externas - Tela Abre
    [Documentation]    Pelo menu Administrativo (aberto na tela Setores), a tela abre com o título.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Ir Para Rota Do Frontend    /administrativo/setores
    Clicar Item Do Menu    Rede intersetorial
    Wait For Elements State    role=heading[level=1] >> text=Instituições da rede intersetorial    visible
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Instituicoes Externas - Cadastrar
    [Documentation]    Sem nome, setor e tipo, a mensagem; com eles, a instituição aparece na tabela.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${sufixo}=    Generate Random String    6    [NUMBERS]
    ${nome}=    Set Variable    CREAS Tela ${sufixo}
    UI Instituicoes Externas - Abrir Tela
    Clicar Botao    Nova instituição
    Enviar Gaveta
    Wait For Elements State    css=app-drawer >> text=Informe o nome, o setor e o tipo.    visible
    Fill Text    id=f-ie-nome    ${nome}
    Select Options By    id=f-ie-setor    value    ASSISTENCIA_SOCIAL
    Select Options By    id=f-ie-tipo    value    CREAS
    Fill Text    id=f-ie-responsavel    Coordenação regional
    Enviar Gaveta
    Wait For Elements State    css=table[aria-label="Instituições externas"] >> text=${nome}    visible
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Instituicoes Externas - Inativar
    [Documentation]    Na edição, desligar "Ativa no catálogo" tira a instituição da lista das ativas.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${uuid}=    Seed Instituicao Externa    Escola Tela    EDUCACAO    ESCOLA
    ${i}=    GET Instituicoes Externas    ${None}
    ${nome}=    Evaluate    [x["nome"] for x in $i.json() if x["uuid"] == $uuid][0]
    UI Instituicoes Externas - Abrir Tela
    Click    role=button[name="Editar ${nome}"]
    Click    id=f-ie-ativa
    Enviar Gaveta
    Wait For Elements State    css=table[aria-label="Instituições externas"] >> text=${nome}    detached
    Click    role=button[name="Com as inativas"]
    Wait For Elements State    css=table[aria-label="Instituições externas"] >> tr:has-text("${nome}") >> text=Inativa    visible
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Seed Encaminhamento Sem Retorno
    [Documentation]    Um caso de saúde mental encaminhado há 20 dias, com o prazo vencido há 5; devolve a unidade.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    [Arguments]    ${instituicao}
    ${caso}    ${unidade}    ${equipe}    ${matricula}    ${paciente}    ${programa}=    Seed Caso Saude Mental
    ${r}=    Encaminhar Caso    ${caso}    ${instituicao}    20    -5
    Should Be Equal As Integers    ${r.status_code}    201
    RETURN    ${unidade}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Instituicoes Externas - Painel E Cobranca
    [Documentation]    A aba Painel mostra a instituição com o encaminhamento sem retorno; "Cobrar" abre a lista com o atraso.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${instituicao}=    Seed Instituicao Externa    CREAS Painel    ASSISTENCIA_SOCIAL    CREAS
    ${unidade}=    Seed Encaminhamento Sem Retorno    ${instituicao}
    ${i}=    GET Instituicoes Externas    ${None}
    ${nome}=    Evaluate    [x["nome"] for x in $i.json() if x["uuid"] == $instituicao][0]
    UI Instituicoes Externas - Abrir Tela
    Click    role=tab[name="Painel"]
    Select Options By    css=select[aria-label="Unidade que encaminhou"]    value    ${unidade}
    Wait For Elements State    css=table[aria-label="Encaminhamentos por instituição"] >> tr:has-text("${nome}")    visible
    Wait For Elements State    css=section[aria-label="Resumo da rede intersetorial"] >> text=Sem retorno    visible
    Click    role=button[name="Cobrar ${nome}"]
    Wait For Elements State    css=table[aria-label="Encaminhamentos sem retorno"] >> text=5 dias    visible
    Wait For Elements State    css=app-drawer >> text=Não confirmou o recebimento    visible
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Instituicoes Externas - Alerta Na Sala De Situacao
    [Documentation]    Com 3 encaminhamentos sem retorno, a instituição vira alerta na Sala de Situação, que leva ao painel.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${instituicao}=    Seed Instituicao Externa    CT Alerta    GARANTIA_DE_DIREITOS    CONSELHO_TUTELAR
    FOR    ${n}    IN RANGE    3
        Seed Encaminhamento Sem Retorno    ${instituicao}
    END
    ${i}=    GET Instituicoes Externas    ${None}
    ${nome}=    Evaluate    [x["nome"] for x in $i.json() if x["uuid"] == $instituicao][0]
    UI Sala - Abrir
    ${alerta}=    Set Variable    css=article[aria-label="Encaminhamentos sem retorno: ${nome}"]
    Wait For Elements State    ${alerta}    visible
    Click    ${alerta} >> text=Ver no painel
    Wait For Elements State    role=tab[name="Painel"][selected]    visible
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
