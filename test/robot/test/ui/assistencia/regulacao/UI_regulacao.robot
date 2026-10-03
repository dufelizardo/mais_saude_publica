*** Settings ***
Resource    ../../../../src/scenario/ui/assistencia/regulacao/regulacao_ui_scenario.resource
Resource    ../../../../src/scenario/common/ui_sessao_scenario.resource
Suite Setup       UI - ABRIR SISTEMA
Suite Teardown    UI - FECHAR SISTEMA
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
Metadata    Test Suite - UI Assistência - Regulação
Metadata    Test Suite Description        Valida a tela Regulação: nova solicitação, análise pela fila, devolução e complemento, catálogo, o link do Encaminhar (ADR-0088), agendamento, atendimento com contrarreferência e falta (ADR-0089).
Metadata    Test Suite Owner              Eduardo Felizardo
Metadata    Test Suite Version            1.0
Metadata    Test Suite Tags               UI    UiRegulacao    MaisSaudePublicaFrontend
Metadata    Test Suite Created On         2026-10-01
Metadata    Test Suite Last Modified      XXXX-XX-XX
Metadata    Project                       Layered Keyword Driven Framework (LKDF)
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Variables ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Test Cases ***
CT-001 - Encaminhar paciente pela gaveta
    [Documentation]    Aviso e linha na aba Solicitações.
    [Tags]    UI    UiRegulacao
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    UI - REGULACAO - NOVA SOLICITACAO
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-002 - Regulador autoriza com vaga pela fila
    [Documentation]    Aviso e solicitação autorizada.
    [Tags]    UI    UiRegulacao
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    UI - REGULACAO - AUTORIZAR PELA FILA
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-003 - Regulador devolve e solicitante complementa
    [Documentation]    A solicitação volta à fila.
    [Tags]    UI    UiRegulacao
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    UI - REGULACAO - DEVOLVER E COMPLEMENTAR
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-004 - Gaveta recusa CID inválido
    [Documentation]    Mensagem do campo CID.
    [Tags]    UI    UiRegulacao
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    UI - REGULACAO - CID INVALIDO
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-005 - Cadastrar procedimento regulado
    [Documentation]    Aviso e linha no catálogo.
    [Tags]    UI    UiRegulacao
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    UI - REGULACAO - CADASTRAR PROCEDIMENTO
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-006 - Encaminhar abre a gaveta com paciente e unidade
    [Documentation]    Campos já preenchidos.
    [Tags]    UI    UiRegulacao
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    UI - REGULACAO - ENCAMINHAR PREENCHIDO
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-007 - Regulador autoriza informando quem vai atender
    [Documentation]    A solicitação sai agendada.
    [Tags]    UI    UiRegulacao
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    UI - REGULACAO - AUTORIZAR JA AGENDANDO
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-008 - Executante agenda a autorizada
    [Documentation]    Agendada com o profissional informado.
    [Tags]    UI    UiRegulacao
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    UI - REGULACAO - AGENDAR PELA LINHA
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-009 - Executante registra o atendimento com contrarreferência
    [Documentation]    Contrarreferência no detalhe.
    [Tags]    UI    UiRegulacao
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    UI - REGULACAO - ATENDIMENTO COM CONTRARREFERENCIA
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-010 - Executante registra a falta
    [Documentation]    Solicitação termina como falta.
    [Tags]    UI    UiRegulacao
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    UI - REGULACAO - REGISTRAR FALTA
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-011 - Regulador autoriza escolhendo a vaga livre da agenda
    [Documentation]    Agendamento na primeira vaga livre de quem vai atender.
    [Tags]    UI    UiRegulacao
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    UI - REGULACAO - AUTORIZAR NA VAGA DA AGENDA
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
