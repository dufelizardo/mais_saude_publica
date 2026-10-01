*** Settings ***
Library     String
Resource    ../../../../pom/ui/rh/perfil/perfil_ui_pom.resource
Resource    ../../../profissional/create_profissional/create_profissional_flow.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
    O profissional é criado pela API; o registro é feito pelas gavetas das abas (ADR-0084).
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
UI Perfil - Semear Profissional Com Cpf Formatado
    [Documentation]    Cria o profissional pela API com o CPF formatado, como o cadastro pela tela grava, e devolve o CPF.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${d}=    Generate Random String    11    [NUMBERS]
    ${cpf}=    Set Variable    ${d}[0:3].${d}[3:6].${d}[6:9]-${d}[9:11]
    ${sufixo}=    Generate Random String    8    [LETTERS][NUMBERS]
    POST Create Profissional - Status Code 201    ${cpf}    Robot Perfil ${sufixo}
    RETURN    ${cpf}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Perfil - Registrar Ajuste Pela Gaveta
    [Documentation]    Registra um ajuste individual na aba Ajustes e vê o aviso e a linha nova.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${cpf}=    UI Perfil - Semear Profissional Com Cpf Formatado
    UI Perfil - Abrir Perfil Pelo Cpf    ${cpf}
    Clicar Aba    Ajustes individuais
    Esperar Texto Aparecer    Nenhum ajuste individual registrado
    Clicar Botao    + Registrar ajuste
    ${titulo}=    Ler Titulo Da Gaveta
    Should Be Equal    ${titulo}    Registrar ajuste individual
    Preencher Campo Da Gaveta    valor    250.00
    Escolher Opcao Da Gaveta    motivoAjuste    GRATIFICACAO_PESSOAL
    Preencher Campo Da Gaveta    referencia    Processo Robot 123
    Enviar Gaveta
    ${aviso}=    Ler Aviso De Status
    Should Contain    ${aviso}    Ajuste registrado
    Esperar Texto Aparecer    Processo Robot 123
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Perfil - Editar Contato Pela Gaveta
    [Documentation]    Troca o e-mail pela gaveta da aba Dados e vê o aviso e o e-mail novo.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${cpf}=    UI Perfil - Semear Profissional Com Cpf Formatado
    ${sufixo}=    Generate Random String    6    [LOWER][NUMBERS]
    ${email}=    Set Variable    robot.${sufixo}@profissional.saude.sp.gov.br
    UI Perfil - Abrir Perfil Pelo Cpf    ${cpf}
    Clicar Aba    Dados
    Clicar Botao    Editar contato
    ${titulo}=    Ler Titulo Da Gaveta
    Should Be Equal    ${titulo}    Editar contato
    Preencher Campo Da Gaveta    email    ${email}
    Enviar Gaveta
    ${aviso}=    Ler Aviso De Status
    Should Contain    ${aviso}    Contato atualizado
    Esperar Texto Aparecer    ${email}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Perfil - Gaveta Recusa Ajuste Sem Valor
    [Documentation]    Envia a gaveta de ajuste vazia e vê a mensagem do campo, sem fechar a gaveta.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${cpf}=    UI Perfil - Semear Profissional Com Cpf Formatado
    UI Perfil - Abrir Perfil Pelo Cpf    ${cpf}
    Clicar Aba    Ajustes individuais
    Clicar Botao    + Registrar ajuste
    Enviar Gaveta
    Esperar Texto Aparecer    Valor é obrigatório.
    ${titulo}=    Ler Titulo Da Gaveta
    Should Be Equal    ${titulo}    Registrar ajuste individual
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
