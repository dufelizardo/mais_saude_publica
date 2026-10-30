*** Settings ***
Resource    ../../src/scenario/emergencias/eventos_especializados_scenario.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
Metadata    Test Suite - Eventos Especializados
Metadata    Test Suite Description        This test suite validates the planned mass gathering event and the temporary care structures (ADR-0160) of the Mais Saúde Pública API.
Metadata    Test Suite Owner              Eduardo Felizardo
Metadata    Test Suite Version            1.0
Metadata    Test Suite Tags               EventosEspecializados    MaisSaudePublicaAPI
Metadata    Test Suite Created On         2026-10-30
Metadata    Test Suite Last Modified      XXXX-XX-XX
Metadata    Project                       Layered Keyword Driven Framework (LKDF)
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Variables ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Test Cases ***
CT-001 - Validate Eventos Especializados - HTTP 200 (Perfil Do Tipo)
    [Documentation]    Perfil Do Tipo.
    [Tags]    EventosEspecializados    HTTP200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    EVENTOS ESPECIALIZADOS    200-perfil-do-tipo
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-002 - Validate Eventos Especializados - HTTP 201 (Planejar)
    [Documentation]    Planejar.
    [Tags]    EventosEspecializados    HTTP201
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    EVENTOS ESPECIALIZADOS    201-planejar
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-003 - Validate Eventos Especializados - HTTP 422 (Planejar Outro Perfil)
    [Documentation]    Planejar Outro Perfil.
    [Tags]    EventosEspecializados    HTTP422
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    EVENTOS ESPECIALIZADOS    422-planejar-outro-perfil
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-004 - Validate Eventos Especializados - HTTP 422 (Operacao Sem Dados)
    [Documentation]    Operacao Sem Dados.
    [Tags]    EventosEspecializados    HTTP422
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    EVENTOS ESPECIALIZADOS    422-operacao-sem-dados
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-005 - Validate Eventos Especializados - HTTP 400 (Periodo Invalido)
    [Documentation]    Periodo Invalido.
    [Tags]    EventosEspecializados    HTTP400
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    EVENTOS ESPECIALIZADOS    400-periodo-invalido
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-006 - Validate Eventos Especializados - HTTP 200 (Dados Do Evento)
    [Documentation]    Dados Do Evento.
    [Tags]    EventosEspecializados    HTTP200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    EVENTOS ESPECIALIZADOS    200-dados-do-evento
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-007 - Validate Eventos Especializados - HTTP 201 (Estrutura)
    [Documentation]    Estrutura.
    [Tags]    EventosEspecializados    HTTP201
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    EVENTOS ESPECIALIZADOS    201-estrutura
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-008 - Validate Eventos Especializados - HTTP 422 (Abrir Antes Da Operacao)
    [Documentation]    Abrir Antes Da Operacao.
    [Tags]    EventosEspecializados    HTTP422
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    EVENTOS ESPECIALIZADOS    422-abrir-antes-da-operacao
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-009 - Validate Eventos Especializados - HTTP 200 (Iniciar Operacao)
    [Documentation]    Iniciar Operacao.
    [Tags]    EventosEspecializados    HTTP200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    EVENTOS ESPECIALIZADOS    200-iniciar-operacao
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-010 - Validate Eventos Especializados - HTTP 201 (Boletim)
    [Documentation]    Boletim.
    [Tags]    EventosEspecializados    HTTP201
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    EVENTOS ESPECIALIZADOS    201-boletim
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-011 - Validate Eventos Especializados - HTTP 200 (Desmobilizar E Relatorio)
    [Documentation]    Desmobilizar E Relatorio.
    [Tags]    EventosEspecializados    HTTP200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    EVENTOS ESPECIALIZADOS    200-desmobilizar-e-relatorio
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

CT-012 - Validate Eventos Especializados - HTTP 200 (Cancelar Planejada)
    [Documentation]    Cancelar Planejada.
    [Tags]    EventosEspecializados    HTTP200
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    EVENTOS ESPECIALIZADOS    200-cancelar-planejada
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
