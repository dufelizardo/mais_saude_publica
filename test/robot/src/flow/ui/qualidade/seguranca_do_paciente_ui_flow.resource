*** Settings ***
Library     String
Resource    ../../../pom/ui/qualidade/seguranca_do_paciente_ui_pom.resource
Resource    ../../qualidade/seguranca_do_paciente_flow.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
    Jornadas da tela Segurança do paciente (ADR-0203); as bases vêm pela API.
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
UI Seguranca - Pelo Menu
    [Documentation]    O item do menu abre a tela.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Ir Para Rota Do Frontend    /sala-de-situacao
    Click    css=a.nav-item[href="/qualidade/seguranca-do-paciente"]
    Wait For Elements State    css=h1 >> text=Segurança do paciente    visible
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Seguranca - Notificar
    [Documentation]    Pela tela: a notificação anônima mostra o protocolo.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${unidade}=    Seed A Unidade De Saude Uuid
    UI Seguranca - Abrir Tela
    Click    id=tab-notificar
    Select Options By    id=f-sp-unidade    value    ${unidade}
    Fill Text    id=f-sp-quando    2026-01-10T10:30
    Fill Text    id=f-sp-local    Enfermaria 3
    Select Options By    id=f-sp-tipo    value    QUEDA
    Fill Text    id=f-sp-desc    Paciente escorregou no banheiro.
    Check Checkbox    id=f-sp-anonima
    Click    css=form[aria-label="Notificar incidente"] button[type="submit"]
    Wait For Elements State    css=[role="status"] >> text=/Notificação registrada: protocolo \\d{4}\\/\\d{6} \\(anônima\\)/    visible
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Seguranca - Classificar
    [Documentation]    Pela tela: o núcleo classifica o incidente e ele entra em análise.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${id}    ${unidade}=    Seed Incidente    IDENTIFICACAO_PACIENTE
    UI Seguranca - Abrir Tela
    Click    id=tab-incidentes
    ${r}=    GET On Session    msp_session    ${BASE_URL}/qualidade/incidente/${id}
    Click    css=button[aria-label="Abrir incidente ${r.json()}[numero]"]
    Select Options By    id=f-sp-nat    value    QUASE_ERRO
    Select Options By    id=f-sp-cdano    value    NENHUM
    Click    css=app-drawer button >> text=Classificar
    Wait For Elements State    css=app-drawer dl >> text=Em análise    visible
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
