*** Settings ***
Library     String
Resource    ../../../../pom/ui/rh/perfil/perfil_ui_pom.resource
Resource    ../../../profissional/create_profissional/create_profissional_flow.resource
Resource    ../../../treinamento/update_treinamento/update_treinamento_flow.resource
Resource    ../../../tipo_beneficio/update_tipo_beneficio/update_tipo_beneficio_flow.resource
Resource    ../../../ciclo_avaliacao/update_ciclo_avaliacao/update_ciclo_avaliacao_flow.resource
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

UI Perfil - Registrar Afastamento Pela Gaveta
    [Documentation]    Registra férias na aba Afastamentos e vê o aviso e a linha nova.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${cpf}=    UI Perfil - Semear Profissional Com Cpf Formatado
    UI Perfil - Abrir Perfil Pelo Cpf    ${cpf}
    Clicar Aba    Afastamentos e Licenças
    Esperar Texto Aparecer    Nenhum afastamento registrado
    Clicar Botao    + Registrar afastamento
    ${titulo}=    Ler Titulo Da Gaveta
    Should Be Equal    ${titulo}    Registrar afastamento
    Escolher Opcao Da Gaveta    tipoAfastamento    FERIAS
    Preencher Campo Da Gaveta    dataFimAfastamento    2099-12-31
    Preencher Campo Da Gaveta    observacaoAfastamento    Férias Robot
    Enviar Gaveta
    ${aviso}=    Ler Aviso De Status
    Should Contain    ${aviso}    Afastamento registrado
    Esperar Texto Aparecer    Férias Robot
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Perfil - Solicitar Correcao De Ponto Pela Gaveta
    [Documentation]    Registra um ponto, pede a correção pela gaveta e vê a correção pendente.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${cpf}=    UI Perfil - Semear Profissional Com Cpf Formatado
    UI Perfil - Abrir Perfil Pelo Cpf    ${cpf}
    Clicar Aba    Ponto
    Clicar Botao    + Registrar ponto
    Escolher Opcao Da Gaveta    tipoPonto    ENTRADA
    Enviar Gaveta
    ${aviso}=    Ler Aviso De Status
    Should Contain    ${aviso}    Ponto registrado
    UI Perfil - Clicar Acao Da Primeira Marcacao De Ponto    Solicitar correção
    ${titulo}=    Ler Titulo Da Gaveta
    Should Be Equal    ${titulo}    Solicitar correção de ponto
    Preencher Campo Da Gaveta    justificativa    Relógio marcou errado
    Enviar Gaveta
    Esperar Texto Aparecer    Correção pendente
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Perfil - Registrar Exame Ocupacional Pela Gaveta
    [Documentation]    Registra um exame na aba SST e vê o aviso e o resultado.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${cpf}=    UI Perfil - Semear Profissional Com Cpf Formatado
    UI Perfil - Abrir Perfil Pelo Cpf    ${cpf}
    Clicar Aba    SST
    Clicar Aba    Exames ocupacionais
    Clicar Botao    + Registrar exame
    Escolher Opcao Da Gaveta    tipoExame    ADMISSIONAL
    Escolher Opcao Da Gaveta    resultadoExame    APTO_COM_RESTRICAO
    Enviar Gaveta
    ${aviso}=    Ler Aviso De Status
    Should Contain    ${aviso}    Exame registrado
    Esperar Texto Aparecer    Apto com restrição
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Perfil - Gaveta Da Folha Recusa Competencia Invalida
    [Documentation]    Envia a folha com competência fora do formato e vê a mensagem do campo.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${cpf}=    UI Perfil - Semear Profissional Com Cpf Formatado
    UI Perfil - Abrir Perfil Pelo Cpf    ${cpf}
    Clicar Aba    Folha de pagamento
    Clicar Botao    + Registrar folha
    Preencher Campo Da Gaveta    competencia    2026-09
    Enviar Gaveta
    ${erro}=    Ler Erro Do Campo Da Gaveta    competencia
    Should Contain    ${erro}    MM/AAAA
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Perfil - Registrar Participacao Em Treinamento Pela Gaveta
    [Documentation]    Registra a participação num treinamento do catálogo e vê o aviso e a linha nova.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${uuid}    ${nome}=    Seed A Treinamento
    ${cpf}=    UI Perfil - Semear Profissional Com Cpf Formatado
    UI Perfil - Abrir Perfil Pelo Cpf    ${cpf}
    Clicar Aba    Treinamentos
    Clicar Botao    + Registrar participação
    Escolher Opcao Da Gaveta    treinamentoId    ${uuid}
    Enviar Gaveta
    ${aviso}=    Ler Aviso De Status
    Should Contain    ${aviso}    Participação registrada
    Esperar Texto Aparecer    ${nome}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Perfil - Aderir E Encerrar Beneficio Pelas Gavetas
    [Documentation]    Registra a adesão e depois a encerra pela gaveta da linha.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${uuid}    ${nome}=    Seed A Tipo Beneficio
    ${cpf}=    UI Perfil - Semear Profissional Com Cpf Formatado
    UI Perfil - Abrir Perfil Pelo Cpf    ${cpf}
    Clicar Aba    Benefícios
    Clicar Botao    + Nova adesão
    Escolher Opcao Da Gaveta    tipoBeneficioId    ${uuid}
    Enviar Gaveta
    Esperar Texto Aparecer    Adesão registrada
    Clicar Botao    Encerrar adesão de ${nome}
    ${titulo}=    Ler Titulo Da Gaveta
    Should Be Equal    ${titulo}    Encerrar adesão
    Enviar Gaveta
    Esperar Texto Aparecer    Adesão encerrada
    Esperar Texto Aparecer    Encerrado
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Perfil - Registrar Avaliacao Pela Gaveta
    [Documentation]    Registra uma avaliação num ciclo e vê o aviso e o ciclo na tabela.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${uuid}    ${nome}=    Seed A Ciclo Avaliacao
    ${cpf}=    UI Perfil - Semear Profissional Com Cpf Formatado
    UI Perfil - Abrir Perfil Pelo Cpf    ${cpf}
    Clicar Aba    Avaliações
    Clicar Botao    + Registrar avaliação
    Escolher Opcao Da Gaveta    cicloId    ${uuid}
    Preencher Campo Da Gaveta    avaliador    Chefia Robot
    Preencher Campo Da Gaveta    nota    8.5
    Enviar Gaveta
    ${aviso}=    Ler Aviso De Status
    Should Contain    ${aviso}    Avaliação registrada
    Esperar Texto Aparecer    ${nome}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Perfil - Profissional Ativo Nao Oferece Calculo De Rescisao
    [Documentation]    Com o profissional ativo, a aba Desligamento não oferece o cálculo de rescisão.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${cpf}=    UI Perfil - Semear Profissional Com Cpf Formatado
    UI Perfil - Abrir Perfil Pelo Cpf    ${cpf}
    Clicar Aba    Desligamento/Rescisão
    Esperar Texto Aparecer    Nenhum cálculo de rescisão registrado
    ${n}=    Contar Botoes Com Nome    + Registrar cálculo de rescisão
    Should Be Equal As Integers    ${n}    0
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
