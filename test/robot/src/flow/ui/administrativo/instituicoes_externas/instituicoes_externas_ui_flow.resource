*** Settings ***
Library     String
Resource    ../../../../pom/ui/administrativo/instituicoes_externas/instituicoes_externas_ui_pom.resource
Resource    ../../../intersetorialidade/intersetorialidade_flow.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
    Jornadas da tela Instituições da rede intersetorial (ADR-0154).
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
UI Instituicoes Externas - Tela Abre
    [Documentation]    Pelo menu Administrativo (aberto na tela Setores), a tela abre com o título.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Ir Para Rota Do Frontend    /administrativo/setores
    Clicar Item Do Menu    Rede intersetorial
    ${titulo}=    Ler Titulo Da Pagina
    Should Be Equal    ${titulo}    Instituições da rede intersetorial
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
