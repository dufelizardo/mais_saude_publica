*** Settings ***
Resource    ../../../../pom/ui/assistencia/pacientes/pacientes_ui_pom.resource
Resource    ../../../paciente/create_paciente/create_paciente_flow.resource
Library     OperatingSystem
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

UI Pacientes - Anexar E Retirar Documento
    [Documentation]    Pela aba Anexos, sem arquivo a mensagem; anexa, baixa a lista e retira com o motivo.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${pacienteId}    ${cpf}    ${cartaoSus}    ${nome}=    Seed A Paciente
    ${arquivo}=    Set Variable    ${OUTPUT_DIR}/exame-ui.pdf
    Create File    ${arquivo}    %PDF-1.4 teste
    UI Pacientes - Abrir Tela
    UI Pacientes - Buscar    ${nome}
    UI Pacientes - Abrir Paciente    ${nome}
    Clicar Aba    Anexos
    Clicar Botao    Anexar documento
    Wait For Elements State    css=#f-tipoDocumento option:has-text("Laudo")    attached
    Select Options By    id=f-tipoDocumento    label    Laudo
    Preencher Campo Da Gaveta    tituloDocumento    Hemograma de março
    Select Options By    id=f-unidadeDocumento    index    1
    Enviar Gaveta
    ${erro}=    Ler Erro Do Campo Da Gaveta    arquivo
    Should Contain    ${erro}    arquivo
    Upload File By Selector    id=f-arquivo    ${arquivo}
    Enviar Gaveta
    ${aviso}=    Ler Aviso De Status
    Should Contain    ${aviso}    Documento anexado
    Esperar Texto Aparecer    Hemograma de março
    Clicar Botao    Retirar Hemograma de março
    Preencher Campo Da Gaveta    motivoRetirada    Anexado no paciente errado
    Enviar Gaveta
    Esperar Texto Aparecer    Anexado no paciente errado
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

