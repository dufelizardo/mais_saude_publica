*** Settings ***
Resource    ../../../pom/common/mais_saude_publica_ui_common.resource
Resource    ../../common/ui_seguranca_flow.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
    Botões por permissão (ADR-0079): sem a permissão, a tela não oferece a ação. Dois usuários com perfis padrão na
    rede inteira — técnico de enfermagem (sem cadastrar paciente nem abrir atendimento) e recepção (com os dois).
    Precisa da API com login e autorização ligados (tag SEGURANCA).
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
Preparar Usuarios Dos Botoes
    [Documentation]    Cria o técnico de enfermagem e a recepção, troca as senhas provisórias e abre o navegador.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Pular Sem Administrador Configurado
    Entrar Na API Como    ${SEG_ADMIN_CPF}    ${SEG_ADMIN_SENHA}
    ${cpfTecnico}    ${idTecnico}=    Criar Usuario Pela Administracao    Robot Tecnico Botoes
    Conceder Papel    ${idTecnico}    TECNICO_DE_ENFERMAGEM
    ${cpfRecepcao}    ${idRecepcao}=    Criar Usuario Pela Administracao    Robot Recepcao Botoes
    Conceder Papel    ${idRecepcao}    RECEPCAO
    Trocar Senha Provisoria    ${cpfTecnico}
    Trocar Senha Provisoria    ${cpfRecepcao}
    Definir Token Da Sessao De API    ${EMPTY}
    Set Suite Variable    ${BOTOES_TECNICO}    ${cpfTecnico}
    Set Suite Variable    ${BOTOES_RECEPCAO}    ${cpfRecepcao}
    Abrir Navegador Do Mais Saude Publica
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Entrar Como
    [Documentation]    Sai de quem estiver logado (novo contexto do navegador) e entra com outro usuário.
    [Arguments]    ${cpf}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Fechar Navegador Do Mais Saude Publica
    Abrir Navegador Do Mais Saude Publica
    Entrar No Sistema Pela Tela De Login    ${cpf}    ${SEG_SENHA}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Botao Deve Aparecer
    [Documentation]    Abre a rota e confere quantos botões com o nome aparecem.
    [Arguments]    ${rota}    ${botao}    ${quantidade}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Ir Para Rota Do Frontend    ${rota}
    Ler Titulo Da Pagina
    # O acesso chega depois da tela abrir: confere por alguns segundos até estabilizar.
    Wait Until Keyword Succeeds    5s    250ms    Quantidade De Botoes Deve Ser    ${botao}    ${quantidade}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Quantidade De Botoes Deve Ser
    [Documentation]    Conta os botões com o nome uma vez e compara.
    [Arguments]    ${botao}    ${quantidade}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${n}=    Contar Botoes Com Nome    ${botao}
    Should Be Equal As Integers    ${n}    ${quantidade}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Botoes - Tecnico Nao Ve Cadastro Nem Abertura
    [Documentation]    Técnico de enfermagem: sem "Novo paciente", "Iniciar atendimento" e "Acolhimento".
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Entrar Como    ${BOTOES_TECNICO}
    Botao Deve Aparecer    /assistencia/pacientes    Novo paciente    0
    Botao Deve Aparecer    /assistencia/atendimentos    Iniciar atendimento    0
    Botao Deve Aparecer    /assistencia/atendimentos    Acolhimento    0
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Botoes - Recepcao Ve Cadastro E Abertura
    [Documentation]    Recepção: vê "Novo paciente", "Iniciar atendimento" e "Acolhimento".
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Entrar Como    ${BOTOES_RECEPCAO}
    Botao Deve Aparecer    /assistencia/pacientes    Novo paciente    1
    Botao Deve Aparecer    /assistencia/atendimentos    Iniciar atendimento    1
    Botao Deve Aparecer    /assistencia/atendimentos    Acolhimento    1
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
