*** Settings ***
Library    OperatingSystem
Resource    ../../../../pom/ui/administracao/auditoria/auditoria_ui_pom.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
    Com o login desligado, a tela Auditoria abre para qualquer um (com a autorização ligada, só com AUDITORIA.CONSULTAR).
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
UI Auditoria - Exportar Baixa O CSV
    [Documentation]    O botão baixa um CSV com o nome da auditoria e a linha de cabeçalho.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    UI Auditoria - Abrir Tela
    Esperar Texto Aparecer    Exportar CSV
    ${arquivo}=    UI Auditoria - Baixar CSV
    Should Match Regexp    ${arquivo}[suggestedFilename]    ^auditoria-\\d{8}-\\d{4}\\.csv$
    ${conteudo}=    Get File    ${arquivo}[saveAs]    encoding=UTF-8-SIG
    Should Start With    ${conteudo}    Data e hora (Brasília);
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
