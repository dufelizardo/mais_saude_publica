*** Settings ***
Library    Collections
Library    ../../../../resource/libraries/cpf_library.py
Resource    ../../../../pom/ui/assistencia/pacientes/pacientes_ui_pom.resource
Resource    ../../../../pom/auth/login/login_pom.resource
Resource    ../../../../pom/auth/senha/senha_pom.resource
Resource    ../../../acesso/acesso/acesso_flow.resource
Resource    ../../../setor/create_setor/create_setor_flow.resource
Resource    ../../../profissional/get_profissional_by_matricula/get_profissional_by_matricula_flow.resource
Resource    ../../../paciente/create_paciente/create_paciente_flow.resource
Resource    ../../../atendimento/create_atendimento/create_atendimento_flow.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
    Prontuário por vínculo assistencial na interface (ADR-0076). Precisa de uma API com login, autorização e vínculo
    ligados, e do administrador inicial (bootstrap) em MSP_ADMIN_CPF/MSP_ADMIN_SENHA. Sem essas variáveis, a suíte é
    pulada (Skip).

    O cenário é montado pela API, autenticada:
    - o administrador cria as duas UBS, um usuário de apoio (Recepção + Gestor na rede inteira, para cadastrar
      profissional, paciente e atendimento) e a enfermeira (Enfermeiro só na UBS A);
    - cada usuário troca a senha provisória (ADR-0069);
    - o apoio cadastra dois pacientes: um com atendimento na UBS A (com vínculo para a enfermeira) e outro só na UBS B
      (sem vínculo).
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Variables ***
${VINCULO_ADMIN_CPF}            %{MSP_ADMIN_CPF=}
${VINCULO_ADMIN_SENHA}          %{MSP_ADMIN_SENHA=}
${VINCULO_SENHA_PROVISORIA}     Robot#Provisoria1
${VINCULO_SENHA}                Robot#Definitiva1
${VINCULO_TEXTO_VALIDO}         Paciente chegou desacordado, preciso do histórico de alergias e medicações
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
Entrar Na API Como
    [Documentation]    Faz login na API e passa a usar o token nas chamadas seguintes. Devolve o corpo do login.
    [Arguments]    ${cpf}    ${senha}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Definir Token Da Sessao De API    ${EMPTY}
    ${payload}=    Create Dictionary    tipo=CPF    identificador=${cpf}    senha=${senha}    manterConectado=${False}
    ${resposta}=    POST Login    ${payload}
    Should Be Equal As Integers    ${resposta.status_code}    200
    Definir Token Da Sessao De API    ${resposta.json()['token']}
    RETURN    ${resposta.json()}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Criar Usuario Pela Administracao
    [Documentation]    Cria um usuário com senha provisória (o token do administrador precisa estar ativo) e devolve
    ...    cpf e uuid.
    [Arguments]    ${nome}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${cpf}=    Gerar Cpf Valido
    ${payload}=    Create Dictionary    cpf=${cpf}    nome=${nome}    senha=${VINCULO_SENHA_PROVISORIA}
    ${criado}=    POST Create Usuario    ${payload}
    Should Be Equal As Integers    ${criado.status_code}    201
    ${lista}=    GET List Usuarios
    ${uuid}=    Evaluate    [u['uuid'] for u in $lista.json() if u['cpf'] == $cpf][0]
    RETURN    ${cpf}    ${uuid}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Conceder Papel
    [Documentation]    Concede um papel padrão a um usuário; sem unidade, na rede inteira.
    [Arguments]    ${usuarioId}    ${codigoPapel}    ${unidadeId}=${NONE}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${papelId}=    Buscar Papel Uuid Por Codigo    ${codigoPapel}
    ${payload}=    Create Dictionary    usuarioId=${usuarioId}    papelId=${papelId}
    IF    $unidadeId is not None
        Set To Dictionary    ${payload}    unidadeId=${unidadeId}
    END
    ${resposta}=    POST Create Atribuicao Acesso    ${payload}
    Should Be Equal As Integers    ${resposta.status_code}    201
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Trocar Senha Provisoria
    [Documentation]    Entra com a senha provisória e define a senha definitiva (ADR-0069). Deixa o token do usuário ativo.
    [Arguments]    ${cpf}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Entrar Na API Como    ${cpf}    ${VINCULO_SENHA_PROVISORIA}
    ${payload}=    Create Dictionary    senhaAtual=${VINCULO_SENHA_PROVISORIA}    novaSenha=${VINCULO_SENHA}
    ...    manterConectado=${False}
    ${resposta}=    POST Auth Senha    ${payload}
    Should Be Equal As Integers    ${resposta.status_code}    200
    Definir Token Da Sessao De API    ${resposta.json()['token']}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Cadastrar Paciente Com Atendimento Na Unidade
    [Documentation]    Cadastra um paciente com um atendimento em aberto na unidade (token de quem cadastra ativo).
    ...    Devolve o nome do paciente.
    [Arguments]    ${unidadeId}    ${matricula}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${pacienteId}    ${cpf}    ${cartaoSus}    ${nome}=    Seed A Paciente
    ${payload}=    Build Atendimento Payload    ${pacienteId}    ${matricula}    ${unidadeId}
    ${resposta}=    POST Create Atendimento    ${payload}
    Should Be Equal As Integers    ${resposta.status_code}    201
    RETURN    ${nome}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Preparar Cenario Do Prontuario Por Vinculo
    [Documentation]    Monta o cenário pela API e abre o navegador já logado como a enfermeira da UBS A. Sem as
    ...    credenciais do administrador, pula a suíte.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Skip If    '${VINCULO_ADMIN_CPF}' == '${EMPTY}'
    ...    MSP_ADMIN_CPF/MSP_ADMIN_SENHA não definidas: a suíte precisa de uma API com login, autorização e vínculo ligados.

    Entrar Na API Como    ${VINCULO_ADMIN_CPF}    ${VINCULO_ADMIN_SENHA}
    ${ubsA}=    Seed A Unidade De Saude Uuid
    ${ubsB}=    Seed A Unidade De Saude Uuid
    ${cpfApoio}    ${idApoio}=    Criar Usuario Pela Administracao    Robot Apoio Vinculo
    Conceder Papel    ${idApoio}    RECEPCAO
    Conceder Papel    ${idApoio}    GESTOR
    ${cpfEnfermeira}    ${idEnfermeira}=    Criar Usuario Pela Administracao    Robot Enfermeira Vinculo
    Conceder Papel    ${idEnfermeira}    ENFERMEIRO    ${ubsA}

    Trocar Senha Provisoria    ${cpfEnfermeira}
    Trocar Senha Provisoria    ${cpfApoio}
    ${matricula}=    Seed A Profissional Matricula
    ${comVinculo}=    Cadastrar Paciente Com Atendimento Na Unidade    ${ubsA}    ${matricula}
    ${semVinculo}=    Cadastrar Paciente Com Atendimento Na Unidade    ${ubsB}    ${matricula}
    Definir Token Da Sessao De API    ${EMPTY}

    Set Suite Variable    ${VINCULO_PACIENTE_COM_VINCULO}    ${comVinculo}
    Set Suite Variable    ${VINCULO_PACIENTE_SEM_VINCULO}    ${semVinculo}
    Abrir Navegador Do Mais Saude Publica
    Entrar No Sistema Pela Tela De Login    ${cpfEnfermeira}    ${VINCULO_SENHA}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Abrir Prontuario Do Paciente Em Pacientes
    [Documentation]    Abre a tela Pacientes, o paciente e a aba Histórico.
    [Arguments]    ${nome}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    UI Pacientes - Abrir Tela
    UI Pacientes - Buscar    ${nome}
    UI Pacientes - Abrir Paciente    ${nome}
    ${mostrado}=    UI Pacientes - Ler Nome No Detalhe
    Should Be Equal    ${mostrado}    ${nome}
    Clicar Aba    Histórico
    Wait For Load State    networkidle
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Vinculo - Com Vinculo Abre Sem Aviso
    [Documentation]    Paciente com atendimento na unidade da enfermeira: o prontuário abre, sem aviso nem bloqueio.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Abrir Prontuario Do Paciente Em Pacientes    ${VINCULO_PACIENTE_COM_VINCULO}
    ${avisos}=    Contar Elementos Com Texto    Sem vínculo assistencial com este paciente
    Should Be Equal As Integers    ${avisos}    0
    ${bloqueios}=    Contar Elementos Com Texto    Sem acesso ao prontuário deste paciente.
    Should Be Equal As Integers    ${bloqueios}    0
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Vinculo - Sem Vinculo Mostra Aviso E Bloqueia
    [Documentation]    Paciente só com atendimento em outra unidade: aparece o aviso e o histórico fica bloqueado.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Abrir Prontuario Do Paciente Em Pacientes    ${VINCULO_PACIENTE_SEM_VINCULO}
    Esperar Texto Aparecer    Sem vínculo assistencial com este paciente
    Esperar Texto Aparecer    Sem acesso ao prontuário deste paciente.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Vinculo - Justificativa Curta E Recusada
    [Documentation]    Abaixo do mínimo de caracteres, o formulário mostra o erro e não registra.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Abrir Prontuario Do Paciente Em Pacientes    ${VINCULO_PACIENTE_SEM_VINCULO}
    Clicar Botao    Acessar com justificativa
    UI Pacientes - Preencher Justificativa Do Acesso    curto demais
    Clicar Botao    Registrar e abrir prontuário
    ${erro}=    UI Pacientes - Ler Erro Da Justificativa
    Should Start With    ${erro}    Descreva com pelo menos 20 caracteres
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Vinculo - Justificativa Libera O Prontuario
    [Documentation]    Com a justificativa válida, o prontuário abre e aparece a faixa do acesso justificado.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Abrir Prontuario Do Paciente Em Pacientes    ${VINCULO_PACIENTE_SEM_VINCULO}
    Clicar Botao    Acessar com justificativa
    UI Pacientes - Preencher Justificativa Do Acesso    ${VINCULO_TEXTO_VALIDO}
    Clicar Botao    Registrar e abrir prontuário
    ${faixa}=    UI Pacientes - Ler Faixa Do Acesso Justificado
    Should Contain    ${faixa}    registrado na auditoria
    ${avisos}=    Contar Elementos Com Texto    Sem vínculo assistencial com este paciente
    Should Be Equal As Integers    ${avisos}    0
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
