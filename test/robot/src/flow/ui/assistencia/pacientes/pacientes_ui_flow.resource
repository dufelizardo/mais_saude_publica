*** Settings ***
Resource    ../../../../pom/ui/assistencia/pacientes/pacientes_ui_pom.resource
Resource    ../../../paciente/create_paciente/create_paciente_flow.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
    Jornadas e asserções do prontuário na tela Pacientes. Com o vínculo desligado (padrão), o prontuário abre sem aviso.
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
UI Pacientes - Prontuario Abre Sem Aviso De Vinculo
    [Documentation]    Com o prontuário por vínculo desligado, o histórico abre sem o aviso de vínculo nem bloqueio.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${pacienteId}    ${cpf}    ${cartaoSus}    ${nome}=    Seed A Paciente
    UI Pacientes - Abrir Tela
    UI Pacientes - Buscar    ${nome}
    UI Pacientes - Abrir Paciente    ${nome}
    ${mostrado}=    UI Pacientes - Ler Nome No Detalhe
    Should Be Equal    ${mostrado}    ${nome}
    Clicar Aba    Histórico
    Wait For Load State    networkidle
    ${avisos}=    Contar Elementos Com Texto    Sem vínculo assistencial com este paciente
    Should Be Equal As Integers    ${avisos}    0
    ${bloqueios}=    Contar Elementos Com Texto    Sem acesso ao prontuário deste paciente.
    Should Be Equal As Integers    ${bloqueios}    0
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
