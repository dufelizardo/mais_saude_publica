*** Settings ***
Library     String
Resource    ../../../../pom/common/mais_saude_publica_ui_common.resource
Resource    ../../../saude_bucal/odontograma_flow.resource
Resource    ../../../saude_bucal/procedimento_odontologico_flow.resource
Resource    ../../../saude_bucal/plano_tratamento_flow.resource
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

UI Saude Bucal - Marcar Odontograma
    [Documentation]    No atendimento de odontologia com a avaliação: marcar a cárie na oclusal do 36 e o 48 ausente, salvar e ver no histórico do dente.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${avaliacao}    ${paciente}=    Seed Avaliacao Odontologica
    Ir Para Rota Do Frontend    /assistencia/atendimentos?aba=pront&pacienteId=${paciente}
    Click    css=.pront-tl >> role=button[name="Abrir"] >> nth=0
    Wait For Elements State    css=[aria-label="Odontograma do paciente"]    visible
    Click    css=polygon[aria-label="Dente 36, face Oclusal"]
    Click    css=[aria-label="Condições"] >> role=button[name="Cárie"]
    Click    css=[aria-label="Odontograma"] >> role=button[name="Dente 48"]
    Click    css=[aria-label="Condições"] >> role=button[name="Ausente"]
    Wait For Elements State    css=[aria-label="Marcações a salvar"]    visible
    Clicar Botao    Salvar marcações
    Wait For Elements State    css=[aria-label="Marcações a salvar"]    detached
    Click    css=[aria-label="Odontograma"] >> role=button[name="Dente 36"]
    Wait For Elements State    css=[aria-label="Histórico do dente"] >> text=Cárie · Oclusal    visible
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Saude Bucal - Restauracao Pelo Procedimento
    [Documentation]    No atendimento de odontologia: registrar a restauração em resina na oclusal do 36 e ver o odontograma mudar (ADR-0136).
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${atendimento}    ${paciente}    ${matricula}=    Seed Atendimento Odontologico
    Ir Para Rota Do Frontend    /assistencia/atendimentos?aba=pront&pacienteId=${paciente}
    Click    css=.pront-tl >> role=button[name="Abrir"] >> nth=0
    Wait For Elements State    css=[aria-label="Procedimentos odontológicos"]    visible
    Clicar Botao    + Procedimento odontológico
    Select Options By    id=f-catalogoId    label    Restauração em resina composta (só dentista)
    Fill Text    id=f-profissionalMatricula    ${matricula}
    Enviar Gaveta
    Wait For Elements State    css=app-drawer >> text=Informe o dente (FDI).    visible
    Fill Text    id=f-dente    36
    Click    css=app-drawer label:has-text("Oclusal")
    Enviar Gaveta
    Wait For Elements State    css=[aria-label="Procedimentos odontológicos"] >> text=Dente 36 · Oclusal    visible
    Wait For Elements State    css=polygon[aria-label="Dente 36, face Oclusal: Restaurado"]    attached
    Click    css=[aria-label="Odontograma"] >> role=button[name="Dente 36"]
    Wait For Elements State    css=[aria-label="Histórico do dente"] >> text=Restauração em resina composta    visible
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Saude Bucal - Catalogo De Procedimentos
    [Documentation]    Na tela de Setores: incluir um procedimento odontológico no catálogo e desativá-lo (ADR-0136).
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${sufixo}=    Generate Random String    5    [UPPER]
    Ir Para Rota Do Frontend    /administrativo/setores
    Wait For Elements State    css=[aria-label="Catálogo de procedimentos"]    visible
    Fill Text    id=f-proc-codigo    UI_${sufixo}
    Fill Text    id=f-proc-nome    Procedimento UI ${sufixo}
    Select Options By    id=f-proc-especialidade    label    Odontologia
    Select Options By    id=f-proc-alcance    label    Dente inteiro
    Select Options By    id=f-proc-condicao    label    Ausente
    Clicar Botao    Incluir procedimento
    Wait For Elements State    css=[aria-label="Catálogo de procedimentos"] li:has-text("Procedimento UI ${sufixo}") >> text=grava Ausente    visible
    Clicar Botao    Desativar: Procedimento UI ${sufixo}
    Wait For Elements State    css=[aria-label="Catálogo de procedimentos"] li:has-text("Procedimento UI ${sufixo}") >> text=Inativo    visible
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Saude Bucal - Plano De Tratamento
    [Documentation]    No atendimento de odontologia com a avaliação: abrir o plano, incluir a restauração do 36 e o flúor, executar a restauração pela gaveta, cancelar o flúor e concluir o tratamento (ADR-0137).
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${atendimento}    ${paciente}    ${matricula}=    Seed Atendimento Odontologico
    ${avaliacao}=    Payload Da Avaliacao    ${atendimento}    ${matricula}
    POST Avaliacao Odontologica    ${avaliacao}
    Ir Para Rota Do Frontend    /assistencia/atendimentos?aba=pront&pacienteId=${paciente}
    Click    css=.pront-tl >> role=button[name="Abrir"] >> nth=0
    Wait For Elements State    css=[aria-label="Plano de tratamento"] >> text=Sem plano de tratamento aberto.    visible
    Fill Text    id=f-plano-matricula    ${matricula}
    Clicar Botao    Abrir plano de tratamento
    Wait For Elements State    css=[aria-label="Plano aberto"]    visible
    Select Options By    id=f-plano-item    label    Restauração em resina composta
    Fill Text    id=f-plano-dente    36
    Click    css=[aria-label="Novo item do plano"] label:has-text("Oclusal")
    Clicar Botao    Incluir item
    Wait For Elements State    css=[aria-label="Itens do plano"] >> text=Dente 36 · Oclusal    visible
    Select Options By    id=f-plano-item    label    Aplicação tópica de flúor (individual)
    Clicar Botao    Incluir item
    Wait For Elements State    css=[aria-label="Itens do plano"] >> text=2. Aplicação tópica de flúor (individual)    visible
    Clicar Botao    Executar: Restauração em resina composta
    Wait For Elements State    css=app-drawer [aria-label="Item do plano executado"]    visible
    Fill Text    id=f-profissionalMatricula    ${matricula}
    Enviar Gaveta
    Wait For Elements State    css=[aria-label="Itens do plano"] li:has-text("Restauração em resina composta") >> text=Concluído    visible
    Clicar Botao    Cancelar: Aplicação tópica de flúor (individual)
    Fill Text    css=[aria-label="Confirmar ação do plano"] input    Recusou
    Clicar Botao    Confirmar
    Wait For Elements State    css=[aria-label="Itens do plano"] li:has-text("flúor") >> text=Cancelado    visible
    Clicar Botao    Concluir tratamento
    Wait For Elements State    css=[aria-label="Plano de tratamento"] >> text=Planos anteriores (1)    visible
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
