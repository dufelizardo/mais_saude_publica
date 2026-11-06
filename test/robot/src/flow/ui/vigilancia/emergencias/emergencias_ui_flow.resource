*** Settings ***
Library     String
Resource    ../../../../pom/ui/vigilancia/emergencias/emergencias_ui_pom.resource
Resource    ../../../emergencias/desmobilizacao_flow.resource
Resource    ../../../emergencias/eventos_especializados_flow.resource
Resource    ../../../emergencias/epidemia_exposicao_flow.resource
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
    Jornadas da tela Emergências e desastres (ADR-0156); as bases vêm pela API.
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
UI Emergencias - Registrar E Converter
    [Documentation]    Registra o evento pela gaveta e converte em emergência no nível 1.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${sufixo}=    Generate Random String    6    [NUMBERS]
    ${descricao}=    Set Variable    Robot tela deslizamento ${sufixo}
    ${tipo}=    Id Do Tipo Emergencia    DESLIZAMENTO
    ${nivel}=    Id Do Nivel Emergencia    1
    UI Emergencias - Abrir Tela
    Clicar Botao    Registrar evento
    Enviar Gaveta
    Wait For Elements State    css=app-drawer >> text=Informe o tipo, a fonte e a descrição.    visible
    Wait For Elements State    css=#f-em-tipo option[value="${tipo}"]    attached
    Select Options By    id=f-em-tipo    value    ${tipo}
    Select Options By    id=f-em-fonte    value    DEFESA_CIVIL
    Fill Text    id=f-em-descricao    ${descricao}
    Enviar Gaveta
    Click    role=tab[name=/^Eventos/]
    Wait For Elements State    css=table[aria-label="Eventos emergenciais"] >> tr:has-text("${descricao}")    visible
    Click    css=table[aria-label="Eventos emergenciais"] >> tr:has-text("${descricao}") >> role=button[name^="Converter em emergência"]
    Fill Text    id=f-em-nome    Deslizamento Robot ${sufixo}
    Select Options By    id=f-em-nivel    value    ${nivel}
    Fill Text    id=f-em-justificativa    Casas atingidas na encosta
    Enviar Gaveta
    Wait For Elements State    css=[aria-label="Emergência Deslizamento Robot ${sufixo}"]    visible
    Wait For Elements State    css=[aria-label="Histórico de nível"] >> text=/Ativada no/    visible
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Emergencias - Nivel E Avaliacao
    [Documentation]    Na emergência: eleva o nível e registra a avaliação de impacto.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${em}=    Seed Emergencia
    ${nome}=    Set Variable    ${em.json()}[nome]
    ${nivel}=    Id Do Nivel Emergencia    2
    UI Emergencias - Abrir Tela    ${em.json()}[uuid]
    Wait For Elements State    css=section.em-detalhe    visible
    Clicar Botao    Mudar nível
    Select Options By    id=f-em-nivel    value    ${nivel}
    Fill Text    id=f-em-justificativa    Hospital atingido
    Enviar Gaveta
    Wait For Elements State    css=[aria-label="Histórico de nível"] >> text=/Nível 1 · Resposta local →/    visible
    Clicar Botao    Nova avaliação
    Select Options By    id=f-em-impacto    value    CRITICO
    Fill Text    id=f-em-populacao    4800
    Enviar Gaveta
    Wait For Elements State    css=[aria-label="Avaliações de impacto"] >> text=/4800 pessoas afetadas/    visible
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Emergencias - Ativar Plano
    [Documentation]    Na emergência, ativa o plano vigente do tipo; as ações previstas do nível aparecem.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${em}=    Seed Emergencia
    ${plano}=    Seed Plano Vigente    INUNDACAO    1
    UI Emergencias - Abrir Tela    ${em.json()}[uuid]
    Wait For Elements State    css=section.em-detalhe    visible
    Clicar Botao    Ativar plano
    Wait For Elements State    css=#f-em-plano option[value="${plano}"]    attached
    Select Options By    id=f-em-plano    value    ${plano}
    Fill Text    id=f-em-autoridade    Secretário de Saúde
    Fill Text    id=f-em-justificativa    Cota de alerta atingida
    Enviar Gaveta
    Wait For Elements State    css=[aria-label="Planos da emergência"] >> text=Ativo    visible
    Wait For Elements State    css=[aria-label^="Ações previstas para o nível"] >> text=Abrir o COE    visible
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Emergencias - Plano Pelo Editor
    [Documentation]    Na aba de planos: o rascunho pelo editor e a publicação.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${sufixo}=    Generate Random String    6    [NUMBERS]
    ${tipo}=    Id Do Tipo Emergencia    DESLIZAMENTO
    ${n1}=    Id Do Nivel Emergencia    1
    UI Emergencias - Abrir Tela
    Click    role=tab[name="Planos de contingência"]
    Clicar Botao    Novo plano
    Fill Text    id=f-pc-codigo    ROBOT_TELA_${sufixo}
    Fill Text    id=f-pc-nome    Plano de encostas ${sufixo}
    Select Options By    id=f-pc-tipo    value    ${tipo}
    Fill Text    id=f-pc-objetivo    Retirar famílias das áreas de risco
    Click    css=app-drawer >> label:has-text("Nível 1 · Resposta local") >> input
    Clicar Botao    Adicionar ação
    Fill Text    css=app-drawer >> [aria-label="Ação 1"]    Evacuar a encosta
    Fill Text    css=app-drawer >> [aria-label="Responsável pela ação 1"]    Defesa Civil e Atenção Básica
    Enviar Gaveta
    Wait For Elements State    css=app-drawer >> text=Rascunho    visible
    Click    css=app-drawer >> role=button[name="Publicar"]
    Wait For Elements State    css=app-drawer >> [aria-label="Versões do plano"] >> text=Vigente    visible
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Emergencias - Quadro De Acoes
    [Documentation]    Na emergência: nova ação pela gaveta, iniciar e concluir.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${em}=    Seed Emergencia
    UI Emergencias - Abrir Tela    ${em.json()}[uuid]
    Wait For Elements State    css=section.em-detalhe    visible
    Clicar Botao    Nova ação
    Fill Text    id=f-co-descricao    Abrir abrigo no ginásio
    Fill Text    id=f-co-responsavel    Assistência social
    Select Options By    id=f-co-prioridade    value    URGENTE
    Enviar Gaveta
    Wait For Elements State    css=table[aria-label="Ações emergenciais"] >> text=Abrir abrigo no ginásio    visible
    Click    role=button[name="Iniciar: Abrir abrigo no ginásio"]
    Enviar Gaveta
    Wait For Elements State    css=table[aria-label="Ações emergenciais"] >> text=Em execução    visible
    Click    role=button[name="Concluir: Abrir abrigo no ginásio"]
    Enviar Gaveta
    Wait For Elements State    css=table[aria-label="Ações emergenciais"] >> text=Concluída    visible
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Emergencias - Mobilizar Profissional
    [Documentation]    Na emergência: mobiliza o profissional, que passa a autorizar turno na Escala.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${em}=    Seed Emergencia
    ${matricula}=    Seed A Profissional Matricula
    ${destino}=    Seed A Unidade De Saude Uuid
    UI Emergencias - Abrir Tela    ${em.json()}[uuid]
    Wait For Elements State    css=section.em-detalhe    visible
    Clicar Botao    Mobilizar
    Wait For Elements State    css=#f-co-destino option[value="${destino}"]    attached
    Select Options By    id=f-co-destino    value    ${destino}
    Fill Text    id=f-co-matricula    ${matricula}
    Enviar Gaveta
    Wait For Elements State    css=[aria-label="Mobilizações"] >> text=Autoriza turno na Escala    visible
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Emergencias - Desmobilizar
    [Documentation]    Pela tela: inicia a desmobilização, vê a pendência, cancela as ações restantes e encerra.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${em}    ${plano}=    Seed Emergencia Com Plano Ativo
    UI Emergencias - Abrir Tela    ${em}
    Wait For Elements State    css=section.em-detalhe    visible
    Clicar Botao    Iniciar desmobilização
    Fill Text    id=f-em-responsavel-desmob    Secretário de Saúde
    Fill Text    id=f-em-justificativa    Águas baixando
    Enviar Gaveta
    Wait For Elements State    css=[aria-label="Desmobilização"]    visible
    Wait For Elements State    css=[aria-label="Pendências da desmobilização"] >> text=/em aberto/    visible
    Clicar Botao    Cancelar ações restantes
    Fill Text    id=f-em-motivo    Desnecessárias após a cheia
    Enviar Gaveta
    Wait For Elements State    css=[aria-label="Pendências da desmobilização"]    detached
    Click    css=section.em-detalhe >> role=button[name="Encerrar"]
    Fill Text    id=f-em-motivo    Rede normalizada
    Enviar Gaveta
    Wait For Elements State    css=section.em-detalhe >> text=Encerrada    visible
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Emergencias - Relatorio Pos Evento
    [Documentation]    Na desmobilização: os números, o rascunho com a recomendação e a revisão do plano.
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${em}    ${plano}=    Seed Emergencia Com Plano Ativo
    Desmobilizar    ${em}
    UI Emergencias - Abrir Tela    ${em}
    Wait For Elements State    css=[aria-label="Números do evento"]    visible
    Fill Text    id=f-rp-resumo    Cheia de três dias na região norte
    Click    role=button[name="Adicionar recomendação"]
    Fill Text    css=[aria-label="Recomendação 1"]    Abrir o abrigo já no nível 1
    Select Options By    css=[aria-label="Plano da recomendação 1"]    value    ${plano}
    Click    role=button[name="Salvar rascunho"]
    Wait For Elements State    css=[aria-label="Relatório pós-evento"] >> text=Rascunho    visible
    Click    css=[aria-label="Recomendações do relatório"] >> role=button[name^="Revisar plano"]
    Wait For Elements State    css=[aria-label="Recomendações do relatório"] >> text=/revisão aberta na versão 2/    visible
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Emergencias - Evento De Massa Planejado
    [Documentation]    Pela tela: o evento planejado ganha os dados do evento e inicia a operação (ADR-0160).
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${em}=    Seed Evento De Massa Planejado
    UI Emergencias - Abrir Tela    ${em}
    Wait For Elements State    css=section.em-detalhe .em-cab .tag >> text="Planejada"    visible
    Wait For Elements State    role=tab[name=/Planejadas/]    visible
    Clicar Botao    Dados do evento
    ${inicio}=    Evaluate    (datetime.datetime.now() + datetime.timedelta(days=2)).strftime('%Y-%m-%dT18:00')    modules=datetime
    ${fim}=    Evaluate    (datetime.datetime.now() + datetime.timedelta(days=3)).strftime('%Y-%m-%dT02:00')    modules=datetime
    Fill Text    id=f-es-local    Avenida Central
    Fill Text    id=f-es-inicio    ${inicio}
    Fill Text    id=f-es-fim    ${fim}
    Fill Text    id=f-es-publico    80000
    Select Options By    id=f-es-risco    value    ALTO
    Enviar Gaveta
    Wait For Elements State    css=[aria-label="Evento de massa"] >> text=Avenida Central    visible
    Wait For Elements State    css=[aria-label="Evento de massa"] >> text=Risco alto    visible
    Clicar Botao    Iniciar operação
    Fill Text    id=f-em-justificativa    Abertura dos portões
    Enviar Gaveta
    Wait For Elements State    css=section.em-detalhe .em-cab .tag >> text="Ativa"    visible
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Emergencias - Estrutura E Boletim
    [Documentation]    Pela tela: nova estrutura temporária e o boletim da que está em operação (ADR-0160).
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${em}    ${posto}=    Evento De Massa Em Operacao
    ${unidade}=    Seed A Unidade De Saude Uuid
    UI Emergencias - Abrir Tela    ${em}
    Wait For Elements State    css=[aria-label="Estruturas temporárias"] >> text=Em operação    visible
    Click    role=button[name="Boletim de Posto Robot"]
    Fill Text    id=f-es-atend    50
    Fill Text    id=f-es-remocoes    2
    Enviar Gaveta
    Wait For Elements State    css=[aria-label="Estruturas temporárias"] >> text=50 atendimento(s)    visible
    Clicar Botao    Nova estrutura
    Select Options By    id=f-es-tipo    value    TENDA_HIDRATACAO
    Fill Text    id=f-es-nome    Tenda Robot
    Fill Text    id=f-es-local-est    Praça da Matriz
    Select Options By    id=f-es-unidade    value    ${unidade}
    Enviar Gaveta
    Wait For Elements State    css=[aria-label="Estruturas temporárias"] >> text=Tenda Robot    visible
    Click    role=button[name="Abrir Tenda Robot"]
    Wait For Elements State    role=button[name="Abrir Tenda Robot"]    detached
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Emergencias - Painel Da Epidemia
    [Documentation]    Pela tela: escolhe o agravo da epidemia e vê os números e a curva (ADR-0161).
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${em}=    Seed Emergencia Do Tipo    EPIDEMIA
    Seed Caso De Sarampo
    ${sarampo}=    Id Do Agravo    SARAMPO
    UI Emergencias - Abrir Tela    ${em}
    Wait For Elements State    css=[aria-label="Situação epidemiológica"]    visible
    Clicar Botao    Escolher agravo
    Select Options By    id=f-es-agravo    value    ${sarampo}
    Enviar Gaveta
    Wait For Elements State    css=[aria-label="Números da epidemia"]    visible
    Wait For Elements State    css=app-curva-epidemica [role="list"]    visible
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

UI Emergencias - Exposicao Ambiental
    [Documentation]    Pela tela: registra a exposição ambiental do desastre (ADR-0161).
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    ${em}=    Seed Emergencia Do Tipo    PRODUTO_PERIGOSO
    UI Emergencias - Abrir Tela    ${em}
    Wait For Elements State    css=[aria-label="Exposição ambiental"]    visible
    Clicar Botao    Nova exposição
    Select Options By    id=f-es-meio    value    AGUA
    Fill Text    id=f-es-agente    Benzeno
    Fill Text    id=f-es-expostos    1200
    Fill Text    id=f-es-recomendacao    Não usar a água da torneira
    Enviar Gaveta
    Wait For Elements State    css=[aria-label="Exposições ambientais"] >> text=Benzeno    visible
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
