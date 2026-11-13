*** Settings ***
Library     String
Resource    ../../../../pom/ui/assistencia/leitos/leitos_ui_pom.resource
Resource    ../../../internacao/leito_internacao/leito_internacao_flow.resource
Resource    ../../../atendimento/create_atendimento/create_atendimento_flow.resource
Resource    ../../../internacao/regulacao_leito/regulacao_leito_flow.resource
Resource    ../../../internacao/eletiva_censo/eletiva_censo_flow.resource
Resource    ../../../internacao/laudo_aih/laudo_aih_flow.resource
Resource    ../../../internacao/prescricao_hospitalar/prescricao_hospitalar_flow.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
    Os dados de apoio (setor, leito, paciente, internação) são criados pela API; a ação sob teste é feita pela tela.
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
UI Leitos - Seed Leito Com Nome
    [Documentation]    Cria um leito com identificação conhecida no setor e devolve uuid e identificação.
    [Arguments]    ${unidadeId}    ${setorId}    ${sexo}=MISTO
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${sufixo}=    Generate Random String    6    [LETTERS][NUMBERS]
    ${identificacao}=    Set Variable    UI Leito ${sufixo}
    Habilitar Internacao Na Unidade    ${unidadeId}
    ${payload}=    Create Dictionary    unidadeId=${unidadeId}    setorId=${setorId}    identificacao=${identificacao}    tipo=CLINICO    sexo=${sexo}
    ${response}=    POST Create Leito    ${payload}
    Should Be Equal As Integers    ${response.status_code}    201
    RETURN    ${response.json()}[uuid]    ${identificacao}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Leitos - Situacao Do Leito Deve Ser
    [Documentation]    Confere pela API a situação do leito na unidade.
    [Arguments]    ${unidadeId}    ${leitoId}    ${esperada}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${params}=    Create Dictionary    unidadeId=${unidadeId}
    ${mapa}=    GET Mapa Leitos    ${params}
    ${situacoes}=    Evaluate    {l["uuid"]: l["situacao"] for l in $mapa.json()}
    Should Be Equal    ${situacoes}[${leitoId}]    ${esperada}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Leitos - Internar Pelo Mapa
    [Documentation]    Interna um paciente clicando no leito livre do mapa.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${setorId}    ${codigo}    ${unidadeId}=    Seed A Setor    ASSISTENCIAL
    ${leitoId}    ${identificacao}=    UI Leitos - Seed Leito Com Nome    ${unidadeId}    ${setorId}
    ${pacienteId}    ${cpf}    ${cartaoSus}    ${nomePaciente}=    Seed A Paciente
    ${matricula}=    Seed A Profissional Matricula
    UI Leitos - Abrir Aba    mapa
    Clicar Botao    Internar no leito ${identificacao}
    ${titulo}=    Ler Titulo Da Gaveta
    Should Be Equal    ${titulo}    Internar no leito ${identificacao}
    Preencher Campo Da Gaveta    buscaPaciente    ${nomePaciente}
    Escolher Opcao Da Gaveta    pacienteId    ${pacienteId}
    Preencher Campo Da Gaveta    cid    J18.9
    Preencher Campo Da Gaveta    medicoMatricula    ${matricula}
    Preencher Campo Da Gaveta    motivo    Pneumonia com hipoxemia, sem resposta ao tratamento oral.
    Enviar Gaveta
    ${aviso}=    Ler Aviso De Status
    Should Contain    ${aviso}    Paciente internado
    UI Leitos - Situacao Do Leito Deve Ser    ${unidadeId}    ${leitoId}    OCUPADO
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Leitos - Gaveta Recusa Internacao Vazia
    [Documentation]    Envia a internação vazia e vê as mensagens do paciente e do leito.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    UI Leitos - Abrir Aba    mapa
    Clicar Botao    Internar paciente
    Preencher Campo Da Gaveta    medicoMatricula    ${EMPTY}
    Enviar Gaveta
    ${erro}=    Ler Erro Do Campo Da Gaveta    pacienteId
    Should Contain    ${erro}    Selecione o paciente
    ${erroLeito}=    Ler Erro Do Campo Da Gaveta    leitoId
    Should Contain    ${erroLeito}    leito livre
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Leitos - Alta E Liberacao
    [Documentation]    Abre a internação pelo mapa, dá alta e libera o leito depois da higienização.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${internacaoId}    ${leitoId}    ${unidadeId}    ${setorId}    ${pacienteId}    ${matricula}=    Seed Uma Internacao
    ${lida}=    GET Internacao By Id    ${internacaoId}
    ${identificacao}=    Set Variable    ${lida.json()}[leitoIdentificacao]
    UI Leitos - Abrir Aba    mapa
    Clicar Botao    Abrir a internação do leito ${identificacao}
    Esperar Texto Aparecer    Motivo da internação
    Clicar Botao    Dar alta
    Preencher Campo Da Gaveta    sumario    Pneumonia tratada com antibiótico venoso; alta com antibiótico oral.
    Preencher Campo Da Gaveta    matriculaAlta    ${matricula}
    Enviar Gaveta
    ${aviso}=    Ler Aviso De Status
    Should Contain    ${aviso}    Alta registrada
    UI Leitos - Situacao Do Leito Deve Ser    ${unidadeId}    ${leitoId}    HIGIENIZACAO
    Clicar Botao    Liberar o leito ${identificacao}
    Esperar Texto Aparecer    Leito liberado
    Wait Until Keyword Succeeds    10s    500ms    UI Leitos - Situacao Do Leito Deve Ser    ${unidadeId}    ${leitoId}    LIVRE
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Leitos - Trocar De Leito
    [Documentation]    Troca o paciente para outro leito livre do setor.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${internacaoId}    ${leitoId}    ${unidadeId}    ${setorId}    ${pacienteId}    ${matricula}=    Seed Uma Internacao
    ${destino}    ${identDestino}=    UI Leitos - Seed Leito Com Nome    ${unidadeId}    ${setorId}    FEMININO
    ${lida}=    GET Internacao By Id    ${internacaoId}
    ${identificacao}=    Set Variable    ${lida.json()}[leitoIdentificacao]
    UI Leitos - Abrir Aba    mapa
    Clicar Botao    Abrir a internação do leito ${identificacao}
    Esperar Texto Aparecer    Motivo da internação
    Clicar Botao    Trocar de leito
    Escolher Opcao Da Gaveta    leitoDestino    ${destino}
    Preencher Campo Da Gaveta    motivoTroca    Isolamento de contato
    Preencher Campo Da Gaveta    matriculaTroca    ${matricula}
    Enviar Gaveta
    ${aviso}=    Ler Aviso De Status
    Should Contain    ${aviso}    Leito trocado
    UI Leitos - Situacao Do Leito Deve Ser    ${unidadeId}    ${destino}    OCUPADO
    UI Leitos - Situacao Do Leito Deve Ser    ${unidadeId}    ${leitoId}    HIGIENIZACAO
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Leitos - Bloquear E Desbloquear
    [Documentation]    Bloqueia um leito livre com motivo e desbloqueia.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${setorId}    ${codigo}    ${unidadeId}=    Seed A Setor    ASSISTENCIAL
    ${leitoId}    ${identificacao}=    UI Leitos - Seed Leito Com Nome    ${unidadeId}    ${setorId}
    UI Leitos - Abrir Aba    mapa
    Clicar Botao    Bloquear o leito ${identificacao}
    Preencher Campo Da Gaveta    motivoBloqueio    Manutenção da cama
    Enviar Gaveta
    ${aviso}=    Ler Aviso De Status
    Should Contain    ${aviso}    Leito bloqueado
    Clicar Botao    Desbloquear o leito ${identificacao}
    Esperar Texto Aparecer    Leito desbloqueado
    Wait Until Keyword Succeeds    10s    500ms    UI Leitos - Situacao Do Leito Deve Ser    ${unidadeId}    ${leitoId}    LIVRE
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Leitos - Cadastrar Leito Pela Gaveta
    [Documentation]    Sem a capacidade, a mensagem na gaveta; com ela, cadastra o leito e vê o aviso.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${setorId}    ${codigo}    ${unidadeId}=    Seed A Setor    ASSISTENCIAL
    ${sufixo}=    Generate Random String    6    [LETTERS][NUMBERS]
    UI Leitos - Abrir Aba    leitos
    Clicar Botao    + Leito
    Escolher Opcao Da Gaveta    unidadeLeito    ${unidadeId}
    Escolher Opcao Da Gaveta    setorLeito    ${setorId}
    Preencher Campo Da Gaveta    identificacao    UI Novo ${sufixo}
    Escolher Opcao Da Gaveta    tipoLeito    UTI_ADULTO
    Enviar Gaveta
    Wait For Elements State    css=app-drawer [role="alert"] >> text=/UTI/    visible
    Incluir Perfil Por Codigo    ${unidadeId}    INTERNACAO
    Incluir Capacidade Por Codigo    ${unidadeId}    UTI
    Enviar Gaveta
    ${aviso}=    Ler Aviso De Status
    Should Contain    ${aviso}    Leito cadastrado
    Esperar Texto Aparecer    UI Novo ${sufixo}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Leitos - Link Do Atendimento Abre A Internacao
    [Documentation]    O link do "Internar" no atendimento abre a internação com o paciente.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${atendimentoId}    ${pacienteId}    ${matricula}    ${unidadeId}=    Seed Um Atendimento
    UI Leitos - Abrir Internar Pelo Link    ${pacienteId}    ${unidadeId}    ${atendimentoId}
    ${titulo}=    Ler Titulo Da Gaveta
    Should Be Equal    ${titulo}    Internar paciente
    Wait Until Keyword Succeeds    10s    500ms    UI Leitos - Campo Deve Valer    pacienteId    ${pacienteId}
    Esperar Texto Aparecer    ligada ao atendimento
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Leitos - Campo Deve Valer
    [Documentation]    Confere o valor de um campo da gaveta.
    [Arguments]    ${campo}    ${esperado}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${valor}=    UI Leitos - Ler Valor Do Campo    ${campo}
    Should Be Equal    ${valor}    ${esperado}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Leitos - Internacao No Prontuario
    [Documentation]    A internação aparece no prontuário do paciente, com o leito e o motivo (ADR-0100).
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${internacaoId}    ${leitoId}    ${unidadeId}    ${setorId}    ${pacienteId}    ${matricula}=    Seed Uma Internacao
    ${lida}=    GET Internacao By Id    ${internacaoId}
    Ir Para Rota Do Frontend    /assistencia/atendimentos?aba=pront&pacienteId=${pacienteId}
    Esperar Texto Aparecer    Internações
    Wait For Elements State    css=app-internacoes-paciente >> text=${lida.json()}[leitoIdentificacao]    visible    10s
    Wait For Elements State    css=app-internacoes-paciente >> text=Pneumonia com hipoxemia.    visible    10s
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Leitos - Limite Pela Ficha Da Unidade
    [Documentation]    Na aba Capacidades, define o limite e a gaveta do leito mostra os números; o cadastro acima dele mostra a mensagem.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${setorId}    ${codigo}    ${unidadeId}=    Seed A Setor    ASSISTENCIAL
    Incluir Perfil Por Codigo    ${unidadeId}    INTERNACAO
    Incluir Capacidade Por Codigo    ${unidadeId}    UTI
    Ir Para Rota Do Frontend    /administrativo/equipamentos?unidade=${unidadeId}
    Clicar Aba    Capacidades
    Clicar Botao    Alterar limite de Terapia intensiva (UTI)
    Enviar Gaveta
    ${erro}=    Ler Erro Do Campo Da Gaveta    quantidadeLeitos
    Should Contain    ${erro}    número
    Preencher Campo Da Gaveta    quantidadeLeitos    0
    Preencher Campo Da Gaveta    motivoLimite    Ainda sem habilitação
    Enviar Gaveta
    ${aviso}=    Ler Aviso De Status
    Should Contain    ${aviso}    Limite alterado
    ${sufixo}=    Generate Random String    6    [LETTERS][NUMBERS]
    UI Leitos - Abrir Aba    leitos
    Clicar Botao    + Leito
    Escolher Opcao Da Gaveta    unidadeLeito    ${unidadeId}
    Escolher Opcao Da Gaveta    setorLeito    ${setorId}
    Preencher Campo Da Gaveta    identificacao    UI Limite ${sufixo}
    Escolher Opcao Da Gaveta    tipoLeito    UTI_ADULTO
    Wait For Elements State    css=#h-tipoLeito >> text=0 de 0    visible
    Enviar Gaveta
    Wait For Elements State    css=app-drawer [role="alert"] >> text=/limite/    visible
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════


UI Leitos - Regular Pedido Pela Central
    [Documentation]    O regulador reserva um leito compatível de outra unidade pela aba Central de leitos (ADR-0184).
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${pedido}    ${pacienteId}    ${origem}    ${matricula}=    Seed Um Pedido De Leito    VERMELHO
    ${leito}    ${unidade}    ${setor}=    Seed Um Leito
    ${vagas}=    GET Vagas Compativeis    ${pedido}
    ${identificacao}=    Evaluate    [v['identificacao'] for v in $vagas.json() if v['leitoId'] == $leito][0]
    ${detalhe}=    GET Pedido De Leito    ${pedido}
    ${nome}=    Set Variable    ${detalhe.json()}[pacienteNome]
    UI Leitos - Abrir Aba    central
    Clicar Botao    Regular o pedido de ${nome}
    ${titulo}=    Ler Titulo Da Gaveta
    Should Be Equal    ${titulo}    Pedido de leito
    Click    css=app-drawer label:has-text("${identificacao}")
    Preencher Campo Da Gaveta    matriculaRegulador    ${matricula}
    Enviar Gaveta
    ${aviso}=    Ler Aviso De Status
    Should Contain    ${aviso}    Leito reservado
    ${depois}=    GET Pedido De Leito    ${pedido}
    Should Be Equal    ${depois.json()}[status]    RESERVADA
    Should Be Equal    ${depois.json()}[leitoReservadoId]    ${leito}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Leitos - Admitir Pela Aba A Receber
    [Documentation]    O destino admite o paciente regulado pela aba A receber (ADR-0184).
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${pedido}    ${pacienteId}    ${origem}    ${matricula}=    Seed Um Pedido De Leito
    ${leito}    ${unidade}    ${setor}=    Seed Um Leito
    ${reserva}=    Create Dictionary    profissionalMatricula=${matricula}    leitoId=${leito}
    ${r}=    POST Reservar Leito    ${pedido}    ${reserva}
    Should Be Equal As Integers    ${r.status_code}    200
    ${nome}=    Set Variable    ${r.json()}[pacienteNome]
    UI Leitos - Abrir Aba    receber
    Clicar Botao    Admitir ${nome}
    Preencher Campo Da Gaveta    medicoRegulado    ${matricula}
    Enviar Gaveta
    ${aviso}=    Ler Aviso De Status
    Should Contain    ${aviso}    Paciente admitido
    UI Leitos - Situacao Do Leito Deve Ser    ${unidade}    ${leito}    OCUPADO
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Leitos - Pedir Vaga Pela Gaveta
    [Documentation]    Pede vaga de internação em outra unidade pela gaveta (ADR-0184).
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${pacienteId}    ${cpf}    ${cartaoSus}    ${nomePaciente}=    Seed A Paciente
    ${origem}=    Seed A Unidade De Saude Uuid
    ${matricula}=    Seed A Profissional Matricula
    UI Leitos - Abrir Aba    mapa
    Clicar Botao    Pedir vaga em outra unidade
    Preencher Campo Da Gaveta    buscaPacienteVaga    ${nomePaciente}
    Escolher Opcao Da Gaveta    pacienteVaga    ${pacienteId}
    Escolher Opcao Da Gaveta    unidadeOrigem    ${origem}
    Escolher Opcao Da Gaveta    tipoLeitoVaga    UTI_ADULTO
    Escolher Opcao Da Gaveta    prioridadeVaga    VERMELHO
    Preencher Campo Da Gaveta    cidVaga    A41.9
    Preencher Campo Da Gaveta    quadroClinico    Choque séptico, em noradrenalina.
    Preencher Campo Da Gaveta    matriculaVaga    ${matricula}
    Enviar Gaveta
    ${aviso}=    Ler Aviso De Status
    Should Contain    ${aviso}    Vaga pedida à central de leitos
    Get Mais Saude Publica Session
    ${lista}=    GET On Session    msp_session    ${BASE_URL}/regulacao-leito/    params=pacienteId=${pacienteId}
    Should Be Equal    ${lista.json()}[0][tipoLeito]    UTI_ADULTO
    Should Be Equal    ${lista.json()}[0][prioridade]    VERMELHO
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Leitos - Agendar Eletiva Pela Gaveta
    [Documentation]    Agenda uma internação eletiva pela gaveta do cabeçalho (ADR-0185).
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${leito}    ${unidade}    ${setor}=    Seed Um Leito
    ${pacienteId}    ${cpf}    ${cartaoSus}    ${nomePaciente}=    Seed A Paciente
    ${matricula}=    Seed A Profissional Matricula
    ${data}=    Data Daqui A    7
    UI Leitos - Abrir Aba    mapa
    Clicar Botao    Agendar internação eletiva
    Preencher Campo Da Gaveta    buscaPacienteEletiva    ${nomePaciente}
    Escolher Opcao Da Gaveta    pacienteEletiva    ${pacienteId}
    Escolher Opcao Da Gaveta    unidadeEletiva    ${unidade}
    Preencher Campo Da Gaveta    dataPrevista    ${data}
    Escolher Opcao Da Gaveta    tipoLeitoEletiva    CLINICO
    Preencher Campo Da Gaveta    cidEletiva    K80.2
    Preencher Campo Da Gaveta    procedimento    Colecistectomia videolaparoscópica.
    Preencher Campo Da Gaveta    medicoEletiva    ${matricula}
    Enviar Gaveta
    ${aviso}=    Ler Aviso De Status
    Should Contain    ${aviso}    Internação eletiva agendada
    ${ate}=    Data Daqui A    30
    ${params}=    Create Dictionary    unidadeId=${unidade}    ate=${ate}
    ${agenda}=    GET Agenda Eletivas    ${params}
    Should Be Equal    ${agenda.json()}[0][pacienteId]    ${pacienteId}
    Should Be Equal    ${agenda.json()}[0][dataPrevista]    ${data}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Leitos - Admitir Eletiva Pela Aba
    [Documentation]    Admite a eletiva do dia pela aba Eletivas, escolhendo o leito livre (ADR-0185).
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${eletiva}    ${leito}    ${unidade}    ${matricula}    ${payload}=    Seed Uma Eletiva    0
    ${detalhe}=    GET Eletiva    ${eletiva}
    ${nome}=    Set Variable    ${detalhe.json()}[pacienteNome]
    UI Leitos - Abrir Aba    eletivas
    Clicar Botao    Admitir ${nome}
    Escolher Opcao Da Gaveta    leitoEletiva    ${leito}
    Preencher Campo Da Gaveta    matriculaEletiva    ${matricula}
    Enviar Gaveta
    ${aviso}=    Ler Aviso De Status
    Should Contain    ${aviso}    Paciente admitido
    UI Leitos - Situacao Do Leito Deve Ser    ${unidade}    ${leito}    OCUPADO
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Leitos - Fechar Censo Pela Aba
    [Documentation]    Vê a prévia do censo de ontem e fecha pela aba Censo diário (ADR-0185).
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${leito}    ${unidade}    ${setor}=    Seed Um Leito
    ${ontem}=    Data Daqui A    -1
    UI Leitos - Abrir Aba    censo
    Select Options By    id=f-unidadeCenso    value    ${unidade}
    Fill Text    id=f-dataCenso    ${ontem}
    Clicar Botao    Ver censo
    Get Text    css=#pn-censo    contains    Prévia (não fechado)
    Clicar Botao    Fechar censo do dia
    ${aviso}=    Ler Aviso De Status
    Should Contain    ${aviso}    Censo fechado
    ${c}=    GET Censo Do Dia    ${unidade}    ${ontem}
    Should Be True    ${c.json()}[fechado]
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Leitos - Solicitar AIH Pela Internacao
    [Documentation]    Preenche o laudo de AIH pela gaveta da internação (ADR-0186).
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${internacao}    ${leito}    ${unidade}    ${setor}    ${pacienteId}    ${matricula}=    Seed Uma Internacao
    ${procedimento}    ${nomeProcedimento}=    Seed Um Procedimento Para AIH
    Get Mais Saude Publica Session
    ${i}=    GET On Session    msp_session    ${BASE_URL}/internacao/${internacao}
    ${nome}=    Set Variable    ${i.json()}[pacienteNome]
    UI Leitos - Abrir Aba    internacoes
    Clicar Botao    Ver a internação de ${nome}
    Clicar Botao    Preencher laudo de AIH
    Preencher Campo Da Gaveta    sinaisSintomas    Febre, tosse produtiva e dispneia.
    Preencher Campo Da Gaveta    condicoesJustificativas    Saturação de 88 por cento em ar ambiente.
    Preencher Campo Da Gaveta    diagnosticoInicial    Pneumonia comunitária grave.
    Preencher Campo Da Gaveta    buscaProcedimentoAih    ${nomeProcedimento}
    Escolher Opcao Da Gaveta    procedimentoAih    ${procedimento}
    Escolher Opcao Da Gaveta    clinicaAih    CLINICA_MEDICA
    Preencher Campo Da Gaveta    matriculaAih    ${matricula}
    Enviar Gaveta
    ${aviso}=    Ler Aviso De Status
    Should Contain    ${aviso}    AIH solicitada
    ${lista}=    GET On Session    msp_session    ${BASE_URL}/laudo-aih/    params=internacaoId=${internacao}
    Should Be Equal    ${lista.json()}[0][status]    SOLICITADO
    Should Be Equal    ${lista.json()}[0][cidPrincipal]    J189
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Leitos - Autorizar AIH Pela Aba
    [Documentation]    Autoriza o laudo pela aba Autorização de AIH, com o número da faixa (ADR-0186).
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Seed Uma Faixa AIH
    ${laudo}    ${internacao}    ${matricula}    ${procedimento}=    Seed Um Laudo AIH
    ${autorizador}=    Seed A Profissional Matricula
    ${d}=    GET Laudo AIH    ${laudo}
    ${nome}=    Set Variable    ${d.json()}[pacienteNome]
    UI Leitos - Abrir Aba    aih
    Clicar Botao    Autorizar a AIH de ${nome}
    Preencher Campo Da Gaveta    matriculaDecisao    ${autorizador}
    Enviar Gaveta
    ${aviso}=    Ler Aviso De Status
    Should Contain    ${aviso}    AIH autorizada
    ${depois}=    GET Laudo AIH    ${laudo}
    Should Be Equal    ${depois.json()}[status]    AUTORIZADO
    Numero AIH Deve Ser Valido    ${depois.json()}[numeroAih]
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Leitos - Cadastrar Faixa AIH Pela Gaveta
    [Documentation]    Cadastra a faixa de numeração pela aba Autorização de AIH (ADR-0186).
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${inicio}=    Evaluate    random.randint(10**10, 9*10**11)    modules=random
    ${fim}=    Evaluate    ${inicio} + 4
    UI Leitos - Abrir Aba    aih
    Clicar Botao    Cadastrar faixa
    Preencher Campo Da Gaveta    inicioFaixa    ${inicio}
    Preencher Campo Da Gaveta    fimFaixa    ${fim}
    Enviar Gaveta
    ${aviso}=    Ler Aviso De Status
    Should Contain    ${aviso}    Faixa de AIH cadastrada
    Get Mais Saude Publica Session
    ${faixas}=    GET On Session    msp_session    ${BASE_URL}/laudo-aih/faixas
    ${achou}=    Evaluate    [f for f in $faixas.json() if f["inicio"] == ${inicio}]
    Length Should Be    ${achou}    1
    Should Be Equal As Integers    ${achou}[0][restantes]    5
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Leitos - Prescrever Pela Internacao
    [Documentation]    Prescreve medicamento e cuidado pela gaveta da internação (ADR-0187).
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${internacao}    ${leito}    ${unidade}    ${setor}    ${pacienteId}    ${matricula}=    Seed Uma Internacao
    ${medicamento}    ${nomeMedicamento}=    Seed Um Medicamento
    Get Mais Saude Publica Session
    ${i}=    GET On Session    msp_session    ${BASE_URL}/internacao/${internacao}
    ${nome}=    Set Variable    ${i.json()}[pacienteNome]
    UI Leitos - Abrir Aba    internacoes
    Clicar Botao    Ver a internação de ${nome}
    Clicar Botao    Nova prescrição
    Preencher Campo Da Gaveta    medicoPrescricao    ${matricula}
    Escolher Opcao Da Gaveta    medicamentoItem    ${medicamento}
    Preencher Campo Da Gaveta    doseItem    1 g
    Escolher Opcao Da Gaveta    viaItem    INTRAVENOSA
    Preencher Campo Da Gaveta    intervaloItem    6
    Clicar Botao    Incluir item
    Escolher Opcao Da Gaveta    tipoItem    CUIDADO
    Preencher Campo Da Gaveta    descricaoItem    Sinais vitais
    Preencher Campo Da Gaveta    intervaloItem    4
    Clicar Botao    Incluir item
    Enviar Gaveta
    ${aviso}=    Ler Aviso De Status
    Should Contain    ${aviso}    Prescrição registrada
    ${lista}=    GET On Session    msp_session    ${BASE_URL}/prescricao-hospitalar/    params=internacaoId=${internacao}
    Length Should Be    ${lista.json()}[0][itens]    2
    Should Be Equal    ${lista.json()}[0][itens][0][tipo]    MEDICAMENTO
    Should Be Equal As Integers    ${lista.json()}[0][itens][1][intervaloHoras]    4
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Leitos - Checar Pelo Posto
    [Documentation]    Checa um cuidado pela aba Horários, o posto de enfermagem (ADR-0187).
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${p}    ${internacao}    ${unidade}    ${medicamento}    ${matricula}=    Seed Uma Prescricao
    ${d}=    GET Prescricao    ${p}
    ${nome}=    Set Variable    ${d.json()}[pacienteNome]
    UI Leitos - Abrir Aba    horarios
    Clicar Botao    Checar Sinais vitais de 4/4 h de ${nome}
    Preencher Campo Da Gaveta    matriculaChecagem    ${matricula}
    Enviar Gaveta
    ${aviso}=    Ler Aviso De Status
    Should Contain    ${aviso}    Checagem registrada
    ${depois}=    GET Prescricao    ${p}
    Should Be Equal    ${depois.json()}[itens][2][horarios][0][situacao]    FEITO
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
