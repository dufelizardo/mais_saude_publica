*** Settings ***
Library     String
Resource    ../../../../pom/ui/vigilancia/sanitaria/vigilancia_sanitaria_ui_pom.resource
Resource    ../../../vigilancia/vigilancia_sanitaria_flow.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
    Jornadas da tela Vigilância sanitária (ADR-0199); as bases vêm pela API.
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
UI Vigilancia Sanitaria - Pelo Menu
    [Documentation]    O item do menu Vigilância abre a tela.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Ir Para Rota Do Frontend    /vigilancia
    ${expandido}=    Get Element Count    css=a.nav-item[href="/vigilancia/sanitaria"]
    IF    ${expandido} == 0
        Click    css=button.nav-group__label >> text=Vigilância
    END
    Click    css=a.nav-item[href="/vigilancia/sanitaria"]
    Wait For Elements State    css=h1 >> text=Vigilância sanitária    visible
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Vigilancia Sanitaria - Cadastrar Estabelecimento
    [Documentation]    Pela tela: cadastra o estabelecimento com a atividade e o vê na tabela com o risco.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${cnpj}=    Gerar Cnpj Valido
    ${sufixo}=    Generate Random String    6    [NUMBERS]
    ${nome}=    Set Variable    Robot UI Visa ${sufixo}
    UI Vigilancia Sanitaria - Abrir Tela
    Clicar Botao    Novo estabelecimento
    Fill Text    id=f-vs-doc    ${cnpj}
    Fill Text    id=f-vs-razao    ${nome}
    Fill Text    id=f-vs-endereco    Rua da Interface, 5
    Fill Text    id=f-vs-cnae-0    4771-7/01
    Fill Text    id=f-vs-ativ-0    Farmácia
    Select Options By    id=f-vs-risco-0    value    ALTO
    Enviar Gaveta
    Wait For Elements State    css=[aria-label="Estabelecimentos regulados"] tr:has-text("${nome}") >> text=III · alto    visible
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Vigilancia Sanitaria - Registrar Inspecao Com Roteiro
    [Documentation]    Pela tela: o item não conforme do roteiro abre a irregularidade, e a inspeção aparece com ressalvas.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${sufixo}=    Generate Random String    6    [NUMBERS]
    ${nome}=    Set Variable    Robot UI Lanchonete ${sufixo}
    ${id}=    Seed Estabelecimento Visa    MEDIO    ${nome}
    ${matricula}=    Seed A Profissional Matricula
    ${r}=    GET Roteiros Visa
    ${roteiro}=    Evaluate    [x['uuid'] for x in $r.json() if x['codigo'] == 'ALIMENTOS_BOAS_PRATICAS'][0]
    UI Vigilancia Sanitaria - Abrir Tela
    Click    css=button[aria-label="Inspecionar ${nome}"]
    Select Options By    id=f-vs-i-roteiro    value    ${roteiro}
    Select Options By    css=select[aria-label="Resposta do item A3"]    value    NAO_CONFORME
    Wait For Elements State    id=f-vs-ir-desc-0    visible
    Fill Text    id=f-vs-ir-prazo-0    2099-12-31
    Fill Text    id=f-vs-i-fiscais    ${matricula}
    Fill Text    id=f-vs-i-rel    Pragas na área de preparo.
    Enviar Gaveta
    Wait For Elements State    css=app-drawer    detached
    Click    id=tab-inspecoes
    Wait For Elements State    css=[aria-label="Inspeções sanitárias"] tr:has-text("${nome}") >> text=Com ressalvas    visible
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Vigilancia Sanitaria - Lavrar Auto E Ciencia
    [Documentation]    Pela tela: lavra o auto sobre a irregularidade aberta e registra a ciência; o processo passa a em defesa.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${sufixo}=    Generate Random String    6    [NUMBERS]
    ${nome}=    Set Variable    Robot UI Auto ${sufixo}
    ${id}=    Seed Estabelecimento Visa    MEDIO    ${nome}
    ${matricula}=    Seed A Profissional Matricula
    ${irreg}=    Create Dictionary    descricao=Lixo exposto    gravidade=LEVE
    ${irregs}=    Create List    ${irreg}
    ${fiscais}=    Create List    ${matricula}
    ${insp}=    Create Dictionary    estabelecimentoId=${id}    motivo=PROGRAMADA    resultado=COM_RESSALVAS    relatorio=Lixo exposto.    fiscais=${fiscais}    irregularidades=${irregs}
    ${r}=    POST Inspecao Visa    ${insp}
    Should Be Equal As Integers    ${r.status_code}    201
    UI Vigilancia Sanitaria - Abrir Tela
    Click    id=tab-processos
    Click    css=button >> text=Lavrar auto de infração
    Select Options By    id=f-ps-estab    value    ${id}
    Wait For Elements State    css=input[aria-label="Autuar: Lixo exposto"]    visible
    Fill Text    id=f-ps-disp    Lei 6.437/1977, art. 10
    Fill Text    id=f-ps-desc    Lixo exposto na área de preparo.
    Fill Text    id=f-ps-fiscais    ${matricula}
    Enviar Gaveta
    Wait For Elements State    css=app-drawer >> text=Ciência do auto    visible
    Enviar Gaveta
    Wait For Elements State    css=[aria-label="Andamento do processo"] >> text=/Ciência do auto em/    visible
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Vigilancia Sanitaria - Interdicao Cautelar
    [Documentation]    Pela tela: a interdição cautelar aparece vigente na aba Medidas.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${sufixo}=    Generate Random String    6    [NUMBERS]
    ${nome}=    Set Variable    Robot UI Interdito ${sufixo}
    ${id}=    Seed Estabelecimento Visa    MEDIO    ${nome}
    UI Vigilancia Sanitaria - Abrir Tela
    Click    id=tab-medidas
    Click    css=button >> text=Interdição cautelar
    Select Options By    id=f-ps-mestab    value    ${id}
    Fill Text    id=f-ps-mdesc    Câmara fria sem refrigeração.
    Enviar Gaveta
    Wait For Elements State    css=app-drawer    detached
    Wait For Elements State    css=[aria-label="Medidas sanitárias"] tr:has-text("${nome}") >> text=Vigente    visible
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Vigilancia Sanitaria - Verificar Licenca Publica
    [Documentation]    A página pública confere a licença pelo número, sem login.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${id}=    Seed Estabelecimento Visa    MEDIO
    ${vazio}=    Create Dictionary
    ${r}=    POST Licenca Visa    ${id}    ${vazio}
    Should Be Equal As Integers    ${r.status_code}    201
    Ir Para Rota Do Frontend    /verificar-visa?licenca=${r.json()}[numero]
    Wait For Elements State    css=section.verif >> text=/Licença sanitária: Vigente/    visible
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
