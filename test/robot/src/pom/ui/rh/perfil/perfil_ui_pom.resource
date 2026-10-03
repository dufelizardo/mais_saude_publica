*** Settings ***
Resource    ../../../common/mais_saude_publica_ui_common.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
    Perfil do profissional no padrão novo (ADR-0084): cada aba tem cabeçalho com a ação e o formulário abre na gaveta.
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
UI Perfil - Abrir Perfil Pelo Cpf
    [Documentation]    Abre o perfil do profissional pelo CPF na URL.
    [Arguments]    ${cpf}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Ir Para Rota Do Frontend    /profissionais/perfil?cpf=${cpf}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Perfil - Clicar Acao Da Primeira Marcacao De Ponto
    [Documentation]    Clica na ação (Solicitar correção, Aprovar, Rejeitar) da primeira marcação. O nome acessível
    ...    leva a data e a hora da marcação, por isso o clique é pelo texto visível na célula de ações.
    [Arguments]    ${acao}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Click    css=td.perfil-acoes >> nth=0 >> text=${acao}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
