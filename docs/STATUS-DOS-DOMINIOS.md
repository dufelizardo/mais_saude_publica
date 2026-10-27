# Status dos domínios

**Documento vivo.** Mostra, domínio por domínio, o que já funciona e o que falta, para decidir e agir sobre os
próximos passos. **Atualize no mesmo PR** que entregar ou descobrir algo: marque a caixa, mova o item para
"Temos" e mude o estado na tabela.

- **Atualizado em:** 2026-10-26 (BI3, depois da release 1.7.0)
- **Release em produção:** v1.7.0 (2026-10-22)
- **Marco:** o mapa está fechado em cobertura funcional ([ADR-0116](./adr/0116-marco-mapa-de-dominios-fechado.md)): 31
  itens mais Documentos. Domínio novo só pelos cinco passos da ADR-0116. Completo como mapa, não como produto.
- **Fontes:**
  - [`MAPA-DE-DOMINIOS.md`](./MAPA-DE-DOMINIOS.md): responsabilidades e entidades;
  - [`PENDENCIAS.md`](./PENDENCIAS.md): ambientes e detalhes técnicos;
  - os "Em breve" das telas;
  - as ADRs.

**Legenda:**

| Sinal | Estado |
|---|---|
| ✅ | funcionando ponta a ponta, com pendências pontuais |
| 🟡 | funcionando em parte |
| 📐 | só desenhado (ADR), sem código |
| ⬜ | não iniciado |

## 1. Visão geral

| # | Domínio | Estado | Principal pendência |
|---|---|---|---|
| 1 | [Organização da Rede / Unidades](#1-organização-da-rede--unidades-de-saúde) | ✅ | mapa e coordenadas reais |
| 2 | [RH](#2-rh) | ✅ | banco de horas e horas realizadas |
| 3 | [Administrativo](#3-administrativo) | ✅ | teste de tela de Necessidades de pessoal |
| 4 | [Atendimento](#4-atendimento) | ✅ | atividades coletivas |
| 5 | [Paciente / Cidadão](#5-paciente--cidadão) | ✅ | abas Programas, Vacinação e Anexos |
| 6 | [Prontuário](#6-prontuário--histórico-clínico) | ✅ | ligar o vínculo assistencial nos ambientes |
| 7 | [Agendamento / Agenda](#7-agendamento--agenda) | ✅ | visão Mês e programas na marcação |
| 8 | [Enfermagem](#8-enfermagem) | ✅ | plano de cuidados e aprazamento |
| 9 | [Farmácia](#9-farmácia) | ✅ | — |
| 10 | [Laboratório / Diagnóstico](#10-laboratório--diagnóstico) | 🟡 | exames de imagem e laudo assinado |
| 11 | [Regulação](#11-regulação) | 🟡 | urgência e internação |
| 12 | [Leitos / Internação](#12-leitos--internação) | 🟡 | regulação de leitos e faturamento (AIH) |
| 13 | [Estoque / Almoxarifado](#13-estoque--almoxarifado) | ⬜ | domínio inteiro |
| 14 | [Compras / Contratos / Fornecedores](#14-compras--contratos--fornecedores) | ⬜ | domínio inteiro |
| 15 | [Patrimônio / Manutenção](#15-patrimônio--manutenção) | ⬜ | domínio inteiro |
| 16 | [Transporte Sanitário](#16-transporte-sanitário) | ⬜ | domínio inteiro |
| 17 | [Financeiro](#17-financeiro) | ⬜ | decidir o escopo e implementar |
| 18 | [Qualidade / Segurança / Auditoria](#18-qualidade--segurança--auditoria) | 🟡 | Qualidade não iniciada |
| 19 | [Indicadores / BI](#19-indicadores--bi) | 🟡 | BI1 a BI3 feitas (Sala de Situação: produção, situação operacional e indicadores da rede); BI4 e BI5 |
| 20 | [Integrações / Identidade / Segurança / Governo](#20-integrações--identidade--segurança--governo) | 🟡 | ligar o login fora do `dev`; integrações |
| 21 | [Território e Adscrição](#21-território-e-adscrição) | ✅ | F1 a F4 feitas; ligar o escopo territorial por rede |
| 22 | [Programas, Ações e Linhas de Cuidado](#22-programas-ações-e-linhas-de-cuidado) | 🟡 | P1 a P4 e P6 feitas; falta P5 |
| 23 | [Vigilância em Saúde](#23-vigilância-em-saúde) | 🟡 | V1 e V2 feitas; sanitária, ambiental e trabalhador (V3 a V5) |
| 24 | [Imunização](#24-imunização) | ⬜ | desenhar em ADR própria |
| 25 | [Saúde Bucal](#25-saúde-bucal) | ✅ | SB1 a SB5 feitas; CPO-D e indicadores pela P6 e pelo BI |
| 26 | [Saúde Mental](#26-saúde-mental) | 🟡 | SM1 a SM4 feitas (SM3 sem notificação no sistema e SAMU); CAPS e indicadores (SM5, SM6) |
| 27 | [Gestão de Emergências e Desastres](#27-gestão-de-emergências-e-desastres) | 📐 | fatias E1 a E6 |
| 28 | [Comunicação e Educação em Saúde](#28-comunicação-e-educação-em-saúde) | 📐 | futuro; fatias C1 a C6 |
| 29 | [Modelo Operacional dos Equipamentos](#29-modelo-operacional-dos-equipamentos) (camada) | 🟡 | fatias MO2 a MO4 |
| 30 | [Gestão da Rede de Atenção](#30-gestão-da-rede-de-atenção) | ⬜ | desenhar em ADR própria |
| 31 | [Intersetorialidade e Proteção Social](#31-intersetorialidade-e-proteção-social) | ⬜ | desenhar em ADR própria |

**Resumo:** 31 itens mais Documentos: 11 ✅ · 10 🟡 · 2 📐 · 8 ⬜ (e Documentos ⬜). A Qualidade é a parte não
iniciada do domínio 18; as Integrações, a parte não iniciada do domínio 20.

### Ordem e dependências das fatias

**Prontas para começar** (não dependem de nada que falte):

| Fatia | Domínio | O que é |
|---|---|---|
| C1 | 28 Comunicação | comunicado, conteúdo, portal e e-mail |
| SM5 | 26 Saúde Mental | componentes da RAPS e serviços do CAPS; as modalidades já estão no catálogo da MO1 |
| P5 | 22 Programas | linhas de cuidado |
| V4 | 23 Vigilância | ambiental e zoonoses: imóvel, foco, vistoria, animal (o imóvel veio na F3) |
| E1 | 27 Emergências | núcleo do evento emergencial: tipo (COBRADE), nível, território afetado |
| MO2 | 29 Modelo Operacional | serviços da unidade (pronto-socorro, UTI, centro cirúrgico, sala de vacina) |
| MO3 | 29 Modelo Operacional | ponto operacional: local de realização do atendimento e unidade móvel |
| BI4 | 19 Indicadores e BI | relatórios (protótipo `Relatorios.html`) |

**Dependências entre fatias** (A → B: B precisa de A):

```text
MO1 ✅ ──► SB4a ✅ (CEO) · SM5 (CAPS) · E3 (capacidade emergencial) · MO2 → MO4
MO3 ──► SAMU e unidade móvel (com Transporte, 16)

P1 ✅ ──► P2 ✅ ──► SM1 ✅ · P4 ✅ · P6 ✅ ──► BI3 ✅ (indicadores da rede)
BI1 ✅ ──► BI2 ✅ (situação operacional) · BI3 ✅ ──► BI4 (relatórios) · BI5 (coordenações)
P1 ✅ ──► P3 ✅ ──► C2 (campanha de comunicação)
P4 ✅ ──► SB5 ✅ (saúde bucal coletiva) · C3 (educação em saúde)

F1 ✅ ──► F2 ✅ ──► V2a ✅ (mapa dos casos) · E1 (área afetada) · Equipamentos com mapa real ✅
F1 ✅ ──► F3a ✅ ──► F3b ✅ ──► V4 (zoonoses) · E4 (vulneráveis) · P2 por microárea ✅ · F4 ✅

V1 ✅ ──► V2a ✅ · SM3 (notificação de autoagressão ✅) · V5
V2a ✅ ──► V2b ✅ ──► E1 (surto como gatilho do evento emergencial)
SB1 ✅ ──► SB2 ✅ ──► SB3a ✅ ──► SB3b ✅ ──► SB4a ✅ ──► SB4b ✅ ──► SB5 ✅
SM1 ✅ ──► SM2 ✅ · SM3 ✅ (parcial) · SM4 ✅ ──► SM6
E1 ──► E2 ──► E3 ──► E4 · E5 ──► E6
C1 ──► C2 · C4 · C5
```

**Dependem de itens ainda não desenhados** (precisam de ADR de domínio antes):

| Pré-requisito ainda sem desenho | Fatias que espera |
|---|---|
| Documentos | V3 (autos da VISA), C2 (arquivos dos materiais) |
| Integrações (20) | V5 (SINAN), C5 (SMS e push), sincronização com CNES |
| Estoque (13) e Transporte (16) | E4 (insumos e veículos), SAMU |
| Regulação de urgência (11) | SM3 (acionar SAMU), E4 |
| Gestão da Rede (30) | cotas PPI da Regulação |
| Intersetorialidade (31) | SM6, encaminhamentos da Vigilância e das Emergências |
| Imunização (24) | aba Vacinação de Pacientes, doses das campanhas |

## 2. Domínios

### 1. Organização da Rede / Unidades de Saúde

**Estado:** ✅

**Temos:**
- Hierarquia de unidades em 5 níveis (ADRs 0002, 0009, 0013) e setores (ADR-0030).
- Tela Equipamentos de Saúde, com lista, ficha, horário estruturado em turnos e situação operacional (em obra,
  inoperante, manutenção) (ADRs 0101, 0102).
- Equipes da unidade na ficha (ADR-0104).
- Visão Mapa da rede e localização da unidade marcada no mapa (ADR-0126).

**Falta:**
- [ ] Sincronização com o CNES (depende de Integrações, domínio 20).
- [ ] Consultórios e capacidade.
- [ ] Vagas semanais da unidade.
- [ ] Acessibilidade.
- [ ] Exportação.
- [ ] Inspeção sanitária.
- [ ] Componentes da RAPS e serviços do CAPS (domínio 26, SM5). As modalidades de CAPS (I, II, III, i, AD, AD III) já
  são capacidades no catálogo da MO1 ([ADR-0117](./adr/0117-modelo-operacional-perfis-e-capacidades.md)).
- [x] Perfis operacionais e capacidades assistenciais da unidade (item 29, MO1, [ADR-0117](./adr/0117-modelo-operacional-perfis-e-capacidades.md)).
- [ ] Serviços (MO2); coordenadas e pontos operacionais (MO3).
- [x] Tipo de unidade do CEO: sem tipo próprio; é a unidade de especialidades com as capacidades odontológicas (ADR-0138).

### 2. RH

**Estado:** ✅

**Temos:**
- Profissionais em cartões, com cadastro e desligamento em gaveta (ADR-0072).
- Cargos e salários, benefícios, desenvolvimento, recrutamento e folha (ADR-0073).
- Perfil com 13 abas (ADRs 0084 a 0086).
- Lotação com histórico; afastamentos, licenças e ponto (backend).
- Equipes de saúde com composição mínima e apoio matricial (ADRs 0103, 0104).
- Escalas com intervalo e modelos de jornada (ADRs 0105 a 0107).

**Falta:**
- [ ] Banco de horas e horas realizadas, ligando o ponto à escala.
- [ ] Exportar e importar profissionais.
- [ ] Modelos de jornada cadastráveis pelo usuário.
- [ ] Conteúdo e material didático ligados aos treinamentos (domínio 28, C4).
- [ ] Turnos fora da lotação por mobilização em emergência ativa, como exceção controlada (domínio 27, E3).
- [ ] Confirmação do plantonista e troca pedida pelo próprio profissional.
- [ ] Recorrência de escala e adicional noturno.
- [ ] Agenda lendo a escala.
- [ ] Cobertura de férias na escala; filtro por função na escala.
- [ ] Folha: cálculo real da composição remuneratória (os valores ainda não são calculados).
- [ ] Testes de tela de Cargos & salários, Benefícios, Desenvolvimento, Recrutamento, Folha e Profissionais.

### 3. Administrativo

**Estado:** ✅

**Temos:**
- Setores, modelo administrativo, capacidades, processos e responsabilidades.
- Responsáveis por setor (ADR-0083).
- Necessidades de pessoal encaminhadas ao RH.

**Falta:**
- [ ] Teste de tela de Necessidades de pessoal.

### 4. Atendimento

**Estado:** ✅

**Temos:**
- Tela Atendimentos (ADR-0063), com consulta, procedimento, triagem, evolução e administração de medicamento.
- Procedimento do catálogo geral, também direto no atendimento, sem consulta (ADR-0136).
- A partir do atendimento: pedir exames (ADR-0094), encaminhar à Regulação (ADR-0088) e internar (ADR-0099).

**Falta:**
- [x] Especialidade no atendimento, do catálogo, herdada da marcação (ADR-0134; serve a todas as especialidades).
- [ ] Local de realização do atendimento (domicílio, via pública, local do evento, unidade móvel), item 29, MO3.
- [ ] Atividades coletivas: vacinação aberta, coleta com vários pacientes, reunião, capacitação. O grupo e a visita
  do programa já são ações programáticas (domínio 22, P4, ADR-0132); falta o registro do atendimento coletivo.

### 5. Paciente / Cidadão

**Estado:** ✅

**Temos:**
- Cadastro do paciente (ADR-0040).
- Tela Pacientes com lista, detalhe e prontuário (ADR-0052).

**Falta:**
- [ ] Aba Programas, indicador "Em programas de saúde" e filtro por programa (domínio 22, P2).
- [ ] Aba Vacinação: depende do domínio 24, Imunização.
- [ ] Aba Anexos (domínio Documentos).
- [ ] Vínculo do paciente com a unidade: vem pela adscrição (domínio 21, F3).
- [ ] Indicador de risco: precisa de uma agregação sobre a triagem mais recente.

### 6. Prontuário / Histórico Clínico

**Estado:** ✅

**Temos:**
- Prontuário agregado com atendimentos, consultas, procedimentos, exames, internações e regulação (ADRs 0045, 0089,
  0095, 0100).
- Acesso por vínculo assistencial e acesso justificado (ADR-0076).

**Falta:**
- [ ] **Ligar o vínculo assistencial** nos ambientes. Está desligado em todos; depende de ligar antes o login e as
  permissões (domínio 20).

### 7. Agendamento / Agenda

**Estado:** ✅

**Temos:**
- Agendamento (ADR-0042).
- Agenda do profissional, com blocos por unidade, bloqueios, vagas, encaixe e falta (ADRs 0091, 0092).
- A agenda respeita o horário e a situação da unidade (ADR-0101).

**Falta:**
- [ ] Visão Mês e exportação.
- [x] Filtro de especialidade e especialidade do bloco, herdada pela marcação (ADR-0134).
- [ ] Sala.
- [x] Programas na marcação, como HiperDia e pré-natal, e a marcação ligada à consulta programada (ADR-0132).
- [ ] Atividades coletivas na grade da agenda (as ações coletivas existem nos Programas, ADR-0132).
- [ ] Vaga liberada por falta oferecida à fila da Regulação ou a encaixe.
- [ ] Meta de ocupação configurável por unidade (fixa em 85%); indicadores de encaixe e falta "de hoje".
- [ ] Mini-mês com as marcações do mês, almoço como bloco, contador no menu, atualização ao vivo.

### 8. Enfermagem

**Estado:** ✅

**Temos:**
- Triagem e classificação de risco (ADR-0047).
- Evolução de enfermagem (ADR-0048).
- Administração de medicamento ligada à Farmácia (ADR-0064).
- Escala, implementada no RH (ADR-0105).

**Falta:**
- [ ] `Cuidado` (plano de cuidados).
- [ ] Prescrição com aprazamento.

### 9. Farmácia

**Estado:** ✅

**Temos:**
- Medicamento, lote e dispensação (ADRs 0049 a 0051).
- Livro de movimentação do estoque (ADR-0057) e tela própria (ADR-0058).
- Transferência entre unidades com recebimento conferido (ADRs 0059 e seguintes).

**Falta:**
- [ ] Os "Em breve" pontuais da tela (rever na próxima passada pela Farmácia).

### 10. Laboratório / Diagnóstico

**Estado:** 🟡

**Temos:**
- Catálogo, pedido, coleta em amostras, resultado, liberação e retificação (ADR-0093).
- Listas de trabalho por etapa (ADR-0094).
- Resultado no prontuário e no atendimento, laudo imprimível e recoleta (ADR-0095).

**Falta:**
- [ ] Exames de imagem (diagnóstico além do laboratório).
- [ ] Laudo PDF com assinatura digital (ICP-Brasil).
- [ ] Envio do resultado ao paciente e aviso ao solicitante quando o resultado é liberado.
- [ ] Gráfico da evolução de um exame no tempo.
- [ ] Valores de referência por sexo e idade.
- [ ] Integração com equipamentos (LIS, HL7, ASTM) e controle de qualidade.
- [ ] Insumos de coleta (depende do Estoque, domínio 13).
- [ ] LACEN e vigilância (vigilância: domínio 23); SIGTAP e BPA.

### 11. Regulação

**Estado:** 🟡

**Temos:**
- Central de Regulação do Acesso: catálogo, solicitação, fila e eventos (ADR-0087).
- Tela com "Encaminhar" no atendimento (ADR-0088).
- Agendamento na executante, desfecho com contrarreferência e vínculo assistencial (ADR-0089).

**Falta:**
- [ ] Regulação de urgência e SAMU (também pedida pela crise em saúde mental, domínio 26, SM3).
- [ ] Regulação de internação e de leitos entre unidades (com o domínio 12).
- [ ] Cotas por unidade e procedimento (PPI); hoje o regulador informa a vaga. Pertencem à Gestão da Rede de Atenção
  (item 30).
- [ ] Integração com SISREG e SIGTAP.
- [ ] Conceder o papel Médico regulador quando a autorização for ligada.

### 12. Leitos / Internação

**Estado:** 🟡

**Temos:**
- Leito em setor assistencial e internação com troca de leito e alta médica (ADR-0098).
- Higienização, bloqueio, mapa e indicadores.
- Tela com "Internar" no atendimento (ADR-0099).
- Internações no prontuário (ADR-0100).

**Falta:**
- [ ] Central de regulação de leitos entre unidades.
- [ ] AIH, SIH e faturamento.
- [ ] Reserva de leito para cirurgia eletiva.
- [ ] Censo diário formal.
- [ ] Prescrição e dieta hospitalar.
- [ ] Restrição de leito por idade (pediátrico, neonatal).
- [ ] Leitos extras ativáveis em emergência (domínio 27, E3; sobre a capacidade do item 29, MO1).
- [ ] Modalidade da internação (voluntária, involuntária, compulsória) e comunicação ao Ministério Público em 72h na
  involuntária, pela Lei 10.216/2001 (domínio 26, SM3).
- [ ] Sincronização com os leitos do CNES.

### 13. Estoque / Almoxarifado

**Estado:** ⬜. Hoje só existe o rótulo `ESTOQUE` reservado no catálogo de capacidades (ADR-0032).

**Falta:**
- [ ] Consumido pela mobilização em emergência: reserva, consumo extraordinário, redistribuição (domínio 27, E4).
- [ ] Desenho (ADR): produto, lote, estoque, movimentação, inventário, almoxarifado. Fica separado da Farmácia,
  porque medicamento tem regra própria.
- [ ] Implementação.

### 14. Compras / Contratos / Fornecedores

**Estado:** ⬜. Hoje só existem os rótulos `COMPRAS` e `CONTRATOS` reservados (ADRs 0032 e 0037).

**Falta:**
- [ ] Desenho (ADR): fornecedor, solicitação de compra, cotação, contrato, item contratado, entrega.
- [ ] Implementação.

### 15. Patrimônio / Manutenção

**Estado:** ⬜. Hoje só existe o rótulo `PATRIMONIO` reservado.

**Falta:**
- [ ] Desenho (ADR): bem (equipamento, veículo, mobiliário), tombamento, localização, manutenção.
- [ ] Implementação.

### 16. Transporte Sanitário

**Estado:** ⬜

**Falta:**
- [ ] Consumido pela mobilização em emergência: veículos e ambulâncias (domínio 27, E4).
- [ ] Desenho (ADR): veículo, solicitação de transporte. Integra com Patrimônio (veículo), RH (motorista) e
  Regulação (necessidade).
- [ ] Implementação.

### 17. Financeiro

**Estado:** ⬜

**Falta:**
- [ ] **Decisão:** sistema financeiro completo, ou só registro e acompanhamento integrado a um ERP externo. A
  tendência registrada no MAPA é a segunda.
- [ ] Desenho (ADR): orçamento, empenho, despesa, centro de custo.
- [ ] Implementação.

### 18. Qualidade / Segurança / Auditoria

**Estado:** 🟡

**Temos:**
- Trilha de auditoria imutável: alterações, leitura de dado de saúde, recusas e login (ADR-0070).
- Consulta na tela Auditoria e "Quem acessou" no atendimento (ADR-0071).
- Exportação CSV e retenção de 20 anos (ADR-0082).
- Alertas com detecção, análise, tela e contador no menu (ADRs 0096, 0097).

**Falta:**
- [ ] **Qualidade** (não iniciada): indicadores de qualidade do serviço, não conformidades, planos de ação.
- [ ] Alertas em tempo real, regras configuráveis pela tela e integração com SIEM.
- [ ] Calibrar os limites dos alertas com o volume real de cada rede.
- [ ] O contador de alertas não se atualiza sozinho.
- [ ] Teste de tela da Auditoria.

### 19. Indicadores / BI

**Estado:** 🟡. Desenhado na [ADR-0142](./adr/0142-indicadores-bi-e-gestao.md): camada de leitura, só agregados,
calculada na hora, com `INDICADOR.CONSULTAR` no escopo da unidade.

**Temos:**
- [x] **BI1 · Produção assistencial por unidade** ([ADR-0143](./adr/0143-bi-producao-assistencial.md)): atendimentos
  concluídos no período e no anterior, média diária, por tipo, especialidade, categoria do profissional, faixa etária,
  sexo e hora; agenda (marcações, realizadas, faltas, canceladas, encaixes e absenteísmo); série por dia e por mês;
  ranking de unidades; exportação CSV auditada; tela Sala de Situação pelo protótipo `Painel.html`.
- [x] **BI2 · Situação operacional** ([ADR-0144](./adr/0144-bi-situacao-operacional.md)): a chegada do paciente
  gravada pelo sistema (V35); a situação agora (em atendimento, aguardando triagem e consulta, maior espera); o tempo de
  espera com média, mediana e p90; a ocupação das agendas por especialidade pela montagem da Agenda; as ocorrências da
  escala (sem cobertura e afastados, sem nomear licença médica); alertas de fila, espera, agenda e escala com limites
  na configuração; atualização a cada minuto na Sala de Situação.
- [x] **BI3 · Indicadores da rede** ([ADR-0146](./adr/0146-bi-indicadores-da-rede.md)): os programas ativos
  consolidados pela apuração da P6 (inscritos, novos, saídas, faltosos, em dia e situação das metas) e cada programa
  por unidade, com a meta da unidade ou da rede; a vigilância por agravo com a oportunidade de encerramento, as 12
  semanas epidemiológicas, a regularidade da notificação e o território; programa e agravo sensíveis escondidos sem a
  permissão; cartões na Sala de Situação.

**Falta:**
- [ ] **BI4 · Relatórios** (protótipo `Relatorios.html`).
- [ ] **BI5 · Coordenações & Gerência** (protótipo `Coordenacoes.html`).

### 20. Integrações / Identidade / Segurança / Governo

**Estado:** 🟡

**Temos:**
- Login com CPF ou matrícula, JWT e chave liga/desliga por ambiente (ADR-0055).
- Papéis, permissões e escopo por unidade (ADRs 0066, 0067); tela Usuários & Perfis (ADR-0068).
- Troca de senha e senha provisória (ADR-0069); recuperação por e-mail (ADR-0081).
- Encerramento de sessões (ADR-0078).

**Falta, na configuração dos ambientes** (passo a passo em
[`acesso/GUIA-LIGAR-AUTORIZACAO.md`](./acesso/GUIA-LIGAR-AUTORIZACAO.md)):
- [ ] Ligar o login em `qaa`, `homologacao` e `prod`, provisionando o Secret `app-secrets` em cada um.
- [ ] Garantir o próprio acesso e conceder os perfis; depois, ligar a exigência de permissão.
- [ ] Ligar o vínculo do prontuário (domínio 6).
- [ ] Escolher e contratar um SMTP para a recuperação de senha.

**Falta, em código:**
- [ ] MFA, sessões ativas por dispositivo e política de senha configurável.
- [ ] Login e recuperação de senha pelo gov.br.
- [ ] Os botões sem permissão aparecem por um instante ao abrir as telas (ADR-0079).
- [ ] **Integrações** (não iniciadas): arquitetura de adapters e depois CNES, e-SUS APS e SISAB, SIGTAP, SISREG e
  DATASUS.
- [ ] Teste de tela de Usuários & Perfis.

### 21. Território e Adscrição

**Estado:** ✅. Desenhado na [ADR-0108](./adr/0108-territorio-e-adscricao.md); detalhe em
[`territorio/MODELO-TERRITORIO.md`](./territorio/MODELO-TERRITORIO.md).

**Temos:**
- [x] **F1 · Fundação** ([ADR-0125](./adr/0125-territorio-fundacao.md)): segmento, território com finalidade, área e
  microárea com GeoJSON validado (JTS, sem PostGIS); microárea dentro da área e sem sobreposição; equipe da área e ACS
  da microárea como histórico; coordenadas da unidade; as microáreas em texto de Equipes migradas, com o Território
  como fonte; tela Território (árvore, detalhe e gavetas).
- [x] **F2 · Mapa** ([ADR-0126](./adr/0126-territorio-mapa.md)): Leaflet com OpenStreetMap (URL configurável) carregado
  sob demanda; a área, as microáreas (sem ACS em âmbar) e as unidades no mapa; desenho e edição de polígonos com o
  geoman, avisos de fora da área e sobreposição; importação de KML; visão Mapa e localização da unidade em
  Equipamentos.
- [x] **F3a · Imóvel, domicílio e família** ([ADR-0127](./adr/0127-cadastro-territorial.md)): imóvel com o ponto
  dentro da microárea, domicílio com as condições da ficha do e-SUS, família com responsável e histórico de moradia
  (mudança), membros que são os pacientes, endereço do cadastro só por ação explícita; papel de agente comunitário de
  saúde e permissão `TERRITORIO.CADASTRAR` com leitura auditada; imóveis no mapa e ficha do imóvel na tela.

- [x] **F3b · Adscrição e cobertura** ([ADR-0128](./adr/0128-adscricao-e-cobertura.md)): adscrição à equipe calculada
  pelo domicílio (entrada, mudança, saída, troca de equipe da área) e manual com motivo, que a calculada não troca;
  cobertura por equipe e microárea; equipe de referência na ficha do paciente; pessoas por microárea em Equipes;
  elegíveis dos programas (idade e sexo) com inscrição pela lista.
- [x] **F4 · Escopo territorial** ([ADR-0129](./adr/0129-escopo-territorial.md)): atrás de interruptor, o ACS na sua
  microárea e a equipe na sua área (cadastro, adscrição manual, elegíveis); quem gere sem restrição; a adscrição à
  equipe vira vínculo para o prontuário (ADR-0076); `meu-escopo` e a tela aberta no alcance; o texto livre das
  microáreas saiu (V22), com o que sobrava migrado para o Território.

**Falta:**
- [ ] Ligar o escopo territorial (`app.security.escopo-territorial.enabled`) quando a rede tiver equipes, áreas e ACS
  cadastrados (ADR-0129).
- [ ] Restringir a busca de pacientes por território (ficou fora: o ACS precisa achar quem ainda não está no cadastro).

### 22. Programas, Ações e Linhas de Cuidado

**Estado:** 🟡. Desenhado na [ADR-0109](./adr/0109-programas-acoes-e-linhas-de-cuidado.md); detalhe em
[`programas/MODELO-PROGRAMAS.md`](./programas/MODELO-PROGRAMAS.md).

**Temos:**
- [x] **P1 · Catálogo** ([ADR-0118](./adr/0118-programas-catalogo.md)): categorias e programas cadastráveis (carga de 4 categorias e 13
  programas), público-alvo como dado (idade, sexo, gestante, CID-10/CIAP-2), coordenação, marca de sensível,
  unidades e equipes participantes como histórico, permissão `PROGRAMA.GERENCIAR`; tela Programas de Saúde pelo
  protótipo e consulta pública na landing page (`/api/v1/publico/programa`, sem login).
- [x] **P2 · Acompanhamento** ([ADR-0119](./adr/0119-programas-acompanhamento.md)): inscrever, faltoso, saída e reinscrição como histórico; atrasado
  pela periodicidade; programas do paciente com vínculo assistencial e auditoria de leitura; programa sensível
  restrito; inscritos no escopo; aba Programas, indicador e filtro em Pacientes; programas na equipe; permissão
  `PROGRAMA.ACOMPANHAR`.
- [x] **P4 · Ações programáticas** ([ADR-0132](./adr/0132-programas-acoes-programaticas.md)): ação individual (pelo
  acompanhamento) ou coletiva (com os participantes), responsável, data prevista, vencida; realização com o
  atendimento que a cumpriu, não realizada e cancelada com motivo; busca ativa dos faltosos e atrasados com o ACS da
  microárea e o resultado; programa na marcação da Agenda, "Agendar" a consulta programada e a ação pronta para
  confirmar quando o atendimento é concluído; aba Ações programáticas e as ações na aba Programas do paciente.
- [x] **P3 · Campanhas** ([ADR-0133](./adr/0133-programas-campanhas.md)): campanha cadastrável com tipo, programa
  opcional, período (situação pelas datas), público como dado ou em palavras, meta com quantidade, publicação e
  cancelamento; locais da rede ou temporários sob a unidade responsável; ações da campanha (coletivas, no local, com as
  pessoas atendidas) dando o andamento da meta; aba Campanhas e a consulta pública na landing page
  (`/api/v1/publico/campanha`).
- [x] **P6 · Metas e indicadores** ([ADR-0141](./adr/0141-programas-metas-e-indicadores.md)): indicadores
  calculados dos registros, no período e na rede ou na unidade (acompanhados em dia, cobertura, ações realizadas, PA
  controlada, exame no alvo, pré-natal com 6 consultas, tratamento odontológico concluído, próteses entregues e
  escovação supervisionada); metas por indicador e unidade com a situação (atingida, em risco, abaixo); evolução de
  12 meses; relatório em CSV; só números, e o programa sensível só para quem acompanha.

**Falta:**
- [x] Lista de elegíveis pelo público-alvo entre os adscritos da equipe (ADR-0128); faltam os critérios clínicos (gestação, condição).
- [ ] **P5 · Linhas de cuidado.**

### 23. Vigilância em Saúde

**Estado:** 🟡. Desenhado na [ADR-0110](./adr/0110-vigilancia-em-saude.md); detalhe em
[`vigilancia/MODELO-VIGILANCIA.md`](./vigilancia/MODELO-VIGILANCIA.md).

**Temos:**
- [x] **V1 · Fundação** ([ADR-0124](./adr/0124-vigilancia-epidemiologica-fundacao.md)): catálogo de 22 agravos da
  Lista Nacional com a ficha específica como dado e versionada; notificação com as respostas validadas pela ficha,
  investigação, classificação com histórico e encerramento com prazo; notificação negativa por semana epidemiológica;
  sugestões de notificação a partir de internação, pedido de exame e crise em saúde mental (que fica notificada);
  agravos sensíveis restritos à vigilância; boletim só com agregados; tela Vigilância epidemiológica.
- [x] **V2a · Contatos, local e mapa** ([ADR-0130](./adr/0130-vigilancia-contatos-local-e-mapa.md)): contato do caso
  (com ou sem cadastro), período de monitoramento por agravo e contatos vencidos; o contato que adoece vira caso pela
  notificação, ligada ao caso índice; local do caso pelo domicílio da família ou marcado no mapa, com a microárea; mapa
  dos casos por microárea, pontos só para quem investiga e nunca de agravo sensível, curva por semana epidemiológica.
- [x] **V2b · Surto e medidas** ([ADR-0131](./adr/0131-vigilancia-surto-e-medidas.md)): surto comunitário ou
  institucional com as microáreas, a definição de caso e o ciclo até o encerramento (com a conclusão e sem medida
  aberta); um surto por caso, do mesmo agravo; medidas de controle do surto ou do caso, com responsável, prazo e
  resultado, e as vencidas nos indicadores; linha do tempo do caso e do surto (esta, pela trilha de auditoria); mapa
  filtrado pelo surto e "Abrir surto" pela microárea.

**Falta:**
- [ ] Sugestão de notificação por exame positivo (o catálogo de exames não liga exame a agravo).
- [ ] **V3 · Sanitária:** estabelecimento regulado, inspeção, irregularidade, medida, licença (os autos dependem
  do domínio Documentos).
- [ ] **V4 · Ambiental e Zoonoses:** evento e risco ambiental, monitoramento, vistoria, foco, animal, ação de
  controle (depende do imóvel, fatia F3 do Território).
- [ ] **V5 · Saúde do Trabalhador e integrações:** evento do trabalho, risco ocupacional, investigação;
  adaptadores, com o SINAN primeiro (depende do domínio 20, Integrações).

### 24. Imunização

**Estado:** ⬜. Reservado pela [ADR-0110](./adr/0110-vigilancia-em-saude.md).

**Falta:**
- [ ] Desenho (ADR): imunobiológico, calendário, dose aplicada, cobertura, eventos adversos, cadeia de frio.
  Integra com Vigilância (23), Programas (22, campanha de vacinação), Estoque (13) e Paciente (aba Vacinação).
- [ ] Implementação.
- [ ] Integração com o SI-PNI (depende do domínio 20).

### 25. Saúde Bucal

**Estado:** ✅. Desenhado na [ADR-0111](./adr/0111-saude-bucal.md) e implementado nas fatias SB1 a SB5 (ADRs 0134 a
0140); detalhe em [`saude-bucal/MODELO-SAUDE-BUCAL.md`](./saude-bucal/MODELO-SAUDE-BUCAL.md). A equipe eSB já existe
(ADR-0103).

**Temos:**
- [x] **SB1 · Base** ([ADR-0134](./adr/0134-saude-bucal-base.md)): catálogo de especialidades (com a marca de
  odontológica), especialidade no atendimento, no bloco da agenda e na marcação, filtro da Agenda; avaliação
  odontológica no atendimento de odontologia (queixa, anamnese, exame, diagnóstico, risco em saúde bucal,
  necessidade de tratamento, plano inicial), com retificação e no prontuário; permissão `ODONTOLOGIA.REGISTRAR` e os
  papéis Cirurgião-dentista e Técnico em saúde bucal.
- [x] **SB2 · Odontograma** ([ADR-0135](./adr/0135-saude-bucal-odontograma.md)): catálogo de condições (cor, sigla e
  alcance), registros por dente FDI e face feitos na avaliação, validação da face pelo dente e do alcance pela
  condição, anulação com motivo, projeção por data (o dente inteiro por cima das faces anteriores) e histórico do
  dente; seção Odontograma no atendimento de odontologia.
- [x] **SB3a · Procedimento odontológico** ([ADR-0136](./adr/0136-saude-bucal-procedimento-odontologico.md)):
  catálogo geral de procedimentos (todas as especialidades, SIGTAP opcional, alcance, condição resultante e exigência
  de dentista), procedimento direto no atendimento (a consulta ficou opcional), complemento odontológico (dente e
  faces) e o procedimento realizado gravando o odontograma, anulado na retificação; o técnico em saúde bucal registra
  o que a lei permite; catálogo na tela Setores.
- [x] **SB3b · Plano de tratamento** ([ADR-0137](./adr/0137-saude-bucal-plano-de-tratamento.md)): um plano aberto por
  paciente, com diagnóstico, objetivo e prioridade (da avaliação); itens do catálogo com dente e faces, executados
  pelos procedimentos (realizado conclui, agendado põe em andamento, cancelado devolve a planejado); em andamento,
  encaminhado (com a solicitação de regulação) e cancelado pelo dentista; tratamento concluído com data e abandono
  com motivo; permissão `ODONTOLOGIA.PLANEJAR`; seção Plano de tratamento no atendimento de odontologia.
- [x] **SB4a · Encaminhamento pela Regulação** ([ADR-0138](./adr/0138-saude-bucal-encaminhamento-pela-regulacao.md)):
  o CEO como unidade com as capacidades odontológicas (sem tipo próprio); o item regulado exige a capacidade da
  executante (vale para toda a Regulação); as especialidades odontológicas no catálogo; a solicitação a partir do item
  do plano, que fica encaminhado e volta a planejado na negativa, cancelamento ou falta; a contrarreferência no plano.
- [x] **SB4b · Tratamento protético** ([ADR-0139](./adr/0139-saude-bucal-tratamento-protetico.md)): total e parcial
  pela arcada, unitária pelo dente; etapas na ordem do ciclo (prova e laboratório se alternam), com data e
  profissional; laboratório da rede (capacidade nova LRPD) ou contratado; a entrega grava a unitária no odontograma;
  conclusão depois da entrega e cancelamento com motivo; seção Prótese no atendimento de odontologia.
- [x] **SB5 · Saúde bucal coletiva** ([ADR-0140](./adr/0140-saude-bucal-coletiva.md)): detalhe odontológico da ação
  coletiva dos programas Saúde Bucal e PSE (atividades, local, público, kits); avaliação de risco por participante na
  ação realizada com lista nominal; lista a convocar (necessidade de tratamento sem plano aberto); o PSE no catálogo de
  Programas.

**Falta:**
- [ ] Índice CPO-D do levantamento epidemiológico e indicadores odontológicos (tratamento concluído, primeira consulta
  programática), pela P6 e pelo BI.

### 26. Saúde Mental

**Estado:** 🟡. Desenhado na [ADR-0112](./adr/0112-saude-mental.md); detalhe em
[`saude-mental/MODELO-SAUDE-MENTAL.md`](./saude-mental/MODELO-SAUDE-MENTAL.md).

**Temos:**
- [x] **SM1 · Base** ([ADR-0120](./adr/0120-saude-mental-base.md)): acolhimento (risco percebido e desfecho), caso
  ligado ao acompanhamento do programa Saúde Mental (reaproveitado ou inscrito; a saída fecha o programa), equipe e
  técnico de referência como histórico, avaliação com CID-10 opcional; sigilo pela equipe de referência (que conta
  como vínculo assistencial), conteúdo só para a referência, os autores e o acesso justificado; permissões
  `SAUDE_MENTAL.ACOLHER` e `SAUDE_MENTAL.ACOMPANHAR`, papéis Psicólogo e Assistente social; tela Saúde Mental e
  atalho na aba Programas de Pacientes. Não depende da SB1: o atendimento é opcional.
- [x] **SM2 · PTS** ([ADR-0121](./adr/0121-saude-mental-pts-intervencoes.md)): PTS versionado (a revisão não
  sobrescreve; revisão concorrente recusada), com objetivos, situação, intervenções previstas, pactuação e "revisar
  em"; intervenções realizadas com tipo de catálogo, compartilhamento e faltas seguidas; rede de apoio como histórico;
  indicadores "Sem PTS" e "PTS a revisar". Grupo ligado à atividade coletiva fica para a P4.
- [x] **SM4 · Rede** ([ADR-0122](./adr/0122-saude-mental-rede.md)): encaminhamento pela Regulação (criar ou vincular;
  contrarreferência da Regulação no caso); cuidado compartilhado com outra equipe, que ganha vínculo e o conteúdo
  enquanto vigente; matriciamento como intervenção com a equipe matriciada e o apoio da ADR-0103; transferência do
  cuidado em duas etapas (proposta, aceite pelo destino com caso novo ligado ao anterior, recusa, cancelamento).
- [x] **SM3 · Risco e crise** ([ADR-0123](./adr/0123-saude-mental-risco-crise.md)): risco com histórico e vigente por
  tipo, medidas e reavaliação com prazo, indicadores; evento de crise com ou sem caso, etapas, pós-crise e desfecho,
  notificação de violência autoprovocada sugerida e registrada como feita; na Internação, a modalidade (Lei
  10.216/2001) e a comunicação da involuntária ao Ministério Público em 72 horas, com a pendência à vista.

**Falta:**
- [ ] **SM3 · o que ficou:** a notificação de violência autoprovocada no sistema (depende da Vigilância V1) e o
  acionamento do SAMU (depende da regulação de urgência).
- [ ] **SM5 · CAPS e RAPS:** modalidades de CAPS e componentes da RAPS como capacidades da unidade (ADR-0053).
- [ ] **SM6 · Intersetorialidade e indicadores:** instituições externas e encaminhamento intersetorial (pelo item 31),
  indicadores.

### 27. Gestão de Emergências e Desastres

**Estado:** 📐. Desenhado na [ADR-0113](./adr/0113-emergencias-e-desastres.md); detalhe em
[`emergencias/MODELO-EMERGENCIAS.md`](./emergencias/MODELO-EMERGENCIAS.md).

**Falta:**
- [ ] **E1 · Núcleo:** evento, emergência, tipo (COBRADE), nível, situação, avaliação de impacto, território afetado.
- [ ] **E2 · Contingência:** plano versionado, critérios de ativação, ações previstas, ativação com histórico.
- [ ] **E3 · Mobilização:** mobilização (com a exceção de turnos fora da lotação na Escala), capacidade emergencial,
  ações emergenciais, unidade afetada pela situação operacional.
- [ ] **E4 · Integração da rede:** redistribuição pela Farmácia, Regulação, vulneráveis por Território e Programas,
  pacientes deslocados; Estoque e Transporte quando existirem.
- [ ] **E5 · Pós-evento:** desmobilização, relatório, lições aprendidas, revisão do plano, indicadores.
- [ ] **E6 · Especializados:** eventos de massa, epidemias, desastres ambientais, hospital de campanha como capacidade.
- [ ] Comunicação de risco à população: feita pelo domínio 28 (comunicado emergencial).

### 28. Comunicação e Educação em Saúde

**Estado:** 📐 **futuro**. Desenhado na [ADR-0114](./adr/0114-comunicacao-e-educacao-em-saude.md); detalhe em
[`comunicacao/MODELO-COMUNICACAO.md`](./comunicacao/MODELO-COMUNICACAO.md).

**Falta:**
- [ ] **C1 · Comunicação básica:** comunicado, conteúdo educativo com validação técnica, publicação, canal; portal
  (landing page) e e-mail.
- [ ] **C2 · Campanhas de comunicação:** `CampanhaComunicacao` ligada a programa ou campanha operacional, público-alvo,
  material educativo (os arquivos dependem do domínio Documentos).
- [ ] **C3 · Educação em saúde:** conteúdo e material nas ações educativas de Programas (P4).
- [ ] **C4 · Educação permanente:** conteúdo e material ligados ao `Treinamento` do RH; competências.
- [ ] **C5 · Multicanal:** adaptadores de SMS, notificação, aplicativo e painel (Integrações, #20); consentimento e
  descadastro.
- [ ] **C6 · Avaliação:** alcance, participação e efetividade, para o #19.

### 29. Modelo Operacional dos Equipamentos

**Estado:** 🟡 (camada transversal, não domínio de negócio). Desenhado na
[ADR-0115](./adr/0115-modelo-operacional-dos-equipamentos.md), que aceita e completa a ADR-0053; matriz e detalhe em
[`equipamentos/MODELO-OPERACIONAL.md`](./equipamentos/MODELO-OPERACIONAL.md).

**Temos:**
- [x] **MO1 · Perfis e capacidades** ([ADR-0117](./adr/0117-modelo-operacional-perfis-e-capacidades.md)): catálogos editáveis de perfil operacional (10) e de
  capacidade assistencial (46, 4 transversais), associação à unidade como histórico, coerência entre capacidade e
  perfil, perfis na rede, aba Capacidades e filtro por perfil em Equipamentos, tela Modelo operacional. Destrava SB4,
  SM5 e E3.

**Falta:**
- [ ] **MO2 · Serviços:** serviços da unidade (pronto-socorro, UTI, centro cirúrgico, sala de vacina), ligados a
  setores.
- [ ] **MO3 · Ponto operacional:** local de realização no Atendimento; base, localização e rota da unidade móvel.
- [ ] **MO4 · Domínios consultam capacidades:** Leitos, Agenda, Saúde Mental, Saúde Bucal, Emergências.

### 30. Gestão da Rede de Atenção

**Estado:** ⬜. Reservado pela [ADR-0115](./adr/0115-modelo-operacional-dos-equipamentos.md).

**Falta:**
- [ ] Desenho (ADR): rede de atenção, participação de cada unidade, oferta e pactuação assistencial, cotas da PPI,
  redes temáticas (RAPS, Rede de Urgência, Rede Cegonha), referência estrutural, cobertura.
- [ ] Implementação. A Regulação (item 11) passa a ler cotas e ofertas daqui.

### 31. Intersetorialidade e Proteção Social

**Estado:** ⬜. Reservado pela [ADR-0115](./adr/0115-modelo-operacional-dos-equipamentos.md).

**Falta:**
- [ ] Desenho (ADR): catálogo único de instituições externas (CRAS, CREAS, escolas, Conselho Tutelar, Defensoria,
  Ministério Público, abrigos) e encaminhamento intersetorial com retorno.
- [ ] Implementação, reaproveitada por Saúde Mental (SM6), Vigilância (violência), Emergências (abrigos) e
  Comunicação.

## 3. Pendências transversais

**Catálogos de procedimento:**
- [x] Catálogo geral de procedimentos, com SIGTAP opcional (ADR-0136).
- [ ] Unificar os catálogos de Regulação (`ProcedimentoRegulado`), Laboratório (`ExameLaboratorial`) e o catálogo
  geral de procedimentos (ADR-0136).

**Domínio "Documentos"** (⬜): anexos e documentos formais reaproveitáveis por qualquer domínio, com versão,
aprovação e assinatura.
- [ ] Desenho e implementação. A aba Anexos de Pacientes depende disso.

**Testes:**
- [ ] Testes Robot de tela das telas antigas (adiados por decisão do usuário): Necessidades de pessoal, RH (Cargos
  & salários, Benefícios, Desenvolvimento, Recrutamento, Folha), Profissionais, Usuários & Perfis e Auditoria.
- [ ] Conferência visual das telas refeitas pelas ADRs 0074 a 0076.

**Infraestrutura e ações manuais** (detalhe em [`PENDENCIAS.md`](./PENDENCIAS.md)):
- [ ] Apagar o secret `RENDER_DEPLOY_URL` e o serviço antigo no painel do Render.
- [ ] Investigar o `argocd-applicationset-controller`, que reinicia sem parar.
- [ ] Trocar o disco mecânico do servidor por SSD.

## 4. Como manter este documento

- **Ao entregar algo:** no mesmo PR, marque a caixa, passe o item para "Temos" com a ADR e reavalie o estado do
  domínio e a linha da visão geral.
- **Ao descobrir uma pendência:** acrescente como caixa no domínio certo, com a dependência entre parênteses.
- **Ao desenhar um domínio novo:** inclua na visão geral e no MAPA-DE-DOMINIOS.
- **Ao promover uma release:** atualize "Release em produção" e a data no topo.
