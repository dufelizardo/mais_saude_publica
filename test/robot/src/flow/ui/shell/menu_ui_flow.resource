*** Settings ***
Resource    ../../../pom/ui/shell/menu_ui_pom.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
    Menu recolhido por padrão: só o grupo da página atual abre sozinho, e o que a pessoa abriu continua aberto.
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
Grupo Deve Estar
    [Documentation]    Confere se o grupo está expandido (true) ou recolhido (false).
    [Arguments]    ${grupo}    ${esperado}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${estado}=    UI Menu - Ler Estado Do Grupo    ${grupo}
    Should Be Equal    ${estado}    ${esperado}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Menu - So O Grupo Da Pagina Abre
    [Documentation]    Numa tela do Administrativo, só o grupo Administrativo está aberto.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    UI Menu - Abrir Rota    /administrativo/setores
    Grupo Deve Estar    Administrativo    true
    Grupo Deve Estar    Recursos Humanos    false
    Grupo Deve Estar    Assistência    false
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Menu - Grupo Aberto Pela Pessoa Continua Aberto
    [Documentation]    A pessoa abre Recursos Humanos; ao navegar dentro do Administrativo e depois para a Assistência,
    ...    Recursos Humanos continua aberto e o grupo da página nova abre sozinho.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    UI Menu - Abrir Rota    /administrativo/setores
    UI Menu - Clicar Grupo    Recursos Humanos
    Grupo Deve Estar    Recursos Humanos    true
    Clicar Item Do Menu    Modelo administrativo
    Grupo Deve Estar    Recursos Humanos    true
    Grupo Deve Estar    Administrativo    true
    UI Menu - Clicar Grupo    Assistência
    Clicar Item Do Menu    Pacientes
    Grupo Deve Estar    Assistência    true
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Menu - Recolher O Grupo Da Pagina
    [Documentation]    O grupo da página atual também pode ser recolhido.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    UI Menu - Abrir Rota    /assistencia/pacientes
    Grupo Deve Estar    Assistência    true
    UI Menu - Clicar Grupo    Assistência
    Grupo Deve Estar    Assistência    false
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
