*** Settings ***
Library     String
Resource    ../../../../pom/common/mais_saude_publica_ui_common.resource
Resource    ../../../saude_bucal/saude_bucal_flow.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
    A base é criada pela API; a ação sob teste é feita pela tela.
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
UI Saude Bucal - Avaliacao Pela Tela
    [Documentation]    No atendimento de odontologia: registrar a avaliação odontológica e retificá-la.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${atendimento}    ${paciente}    ${matricula}=    Seed Atendimento Odontologico
    Ir Para Rota Do Frontend    /assistencia/atendimentos?aba=pront&pacienteId=${paciente}
    Click    css=.pront-tl >> role=button[name="Abrir"] >> nth=0
    Clicar Botao    + Avaliação odontológica
    Fill Text    id=f-profissionalMatricula    ${matricula}
    Fill Text    id=f-diagnosticoOdonto    Cárie no 36
    Enviar Gaveta
    Wait For Elements State    css=app-drawer >> text=Classifique o risco em saúde bucal.    visible
    Click    css=app-drawer label:has-text("Risco alto")
    Select Options By    id=f-necessidadeTratamento    value    URGENCIA
    Enviar Gaveta
    Wait For Elements State    css=[aria-label="Avaliação odontológica"] .tag >> text=Risco alto    visible
    Clicar Botao    Retificar avaliação odontológica
    Fill Text    id=f-motivoRetificacao    Dente errado
    Fill Text    id=f-diagnosticoOdonto    Cárie no 46
    Enviar Gaveta
    Wait For Elements State    css=[aria-label="Avaliação odontológica"] >> text=Cárie no 46    visible
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Saude Bucal - Catalogo De Especialidades
    [Documentation]    Na tela de Setores: incluir uma especialidade no catálogo e desativá-la.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${sufixo}=    Generate Random String    5    [UPPER]
    Ir Para Rota Do Frontend    /administrativo/setores
    Wait For Elements State    css=[aria-label="Catálogo de especialidades"]    visible
    Fill Text    id=f-especialidade-codigo    UI_${sufixo}
    Fill Text    id=f-especialidade-nome    Especialidade UI ${sufixo}
    Clicar Botao    Incluir especialidade
    Wait For Elements State    css=[aria-label="Catálogo de especialidades"] >> text=Especialidade UI ${sufixo}    visible
    Clicar Botao    Desativar: Especialidade UI ${sufixo}
    Wait For Elements State    css=[aria-label="Catálogo de especialidades"] li:has-text("Especialidade UI ${sufixo}") >> text=Inativa    visible
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
