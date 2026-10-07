# Status dos domínios

**Documento vivo.** Mostra, domínio por domínio, o que já funciona e o que falta, para decidir e agir sobre os
próximos passos. **Atualize no mesmo PR** que entregar ou descobrir algo: marque a caixa, mova o item para
"Temos" e mude o estado na tabela.

- **Atualizado em:** 2026-10-07
- **Release em produção:** v1.6.0
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
| 19 | [Indicadores / BI](#19-indicadores--bi) | ⬜ | Sala de Situação e Relatórios |
| 20 | [Integrações / Identidade / Segurança / Governo](#20-integrações--identidade--segurança--governo) | 🟡 | ligar o login fora do `dev`; integrações |
| 21 | [Território e Adscrição](#21-território-e-adscrição) | 📐 | fatias F1 a F4 |
| 22 | [Programas, Ações e Linhas de Cuidado](#22-programas-ações-e-linhas-de-cuidado) | 📐 | fatias P1 a P6 |
| 23 | [Vigilância em Saúde](#23-vigilância-em-saúde) | 📐 | fatias V1 a V5 |
| 24 | [Imunização](#24-imunização) | ⬜ | desenhar em ADR própria |
| 25 | [Saúde Bucal](#25-saúde-bucal) | 📐 | fatias SB1 a SB5 |
| 26 | [Saúde Mental](#26-saúde-mental) | 📐 | fatias SM1 a SM6 |
| 27 | [Gestão de Emergências e Desastres](#27-gestão-de-emergências-e-desastres) | 📐 | fatias E1 a E6 |
| 28 | [Comunicação e Educação em Saúde](#28-comunicação-e-educação-em-saúde) | 📐 | futuro; fatias C1 a C6 |
| 29 | [Modelo Operacional dos Equipamentos](#29-modelo-operacional-dos-equipamentos) (camada) | 📐 | fatias MO1 a MO4 |
| 30 | [Gestão da Rede de Atenção](#30-gestão-da-rede-de-atenção) | ⬜ | desenhar em ADR própria |
| 31 | [Intersetorialidade e Proteção Social](#31-intersetorialidade-e-proteção-social) | ⬜ | desenhar em ADR própria |

**Resumo:** 31 itens mais Documentos: 9 ✅ · 5 🟡 · 8 📐 · 9 ⬜ (e Documentos ⬜). A Qualidade é a parte não
iniciada do domínio 18; as Integrações, a parte não iniciada do domínio 20.

### Ordem e dependências das fatias

**Prontas para começar** (não dependem de nada que falte):

| Fatia | Domínio | O que é |
|---|---|---|
| MO1 | 29 Modelo Operacional | perfis e capacidades da unidade |
| P1 | 22 Programas | catálogo de programas, tela em cartões |
| F1 | 21 Território | backend do território (área, microárea, GeoJSON) |
| V1 | 23 Vigilância | agravos, notificação, investigação |
| SB1 | 25 Saúde Bucal | especialidade no atendimento, avaliação odontológica |
| C1 | 28 Comunicação | comunicado, conteúdo, portal e e-mail |

**Dependências entre fatias** (A → B: B precisa de A):

```text
MO1 ──► SB4 (CEO) · SM5 (CAPS) · E3 (capacidade emergencial) · MO2 → MO4
MO3 ──► SAMU e unidade móvel (com Transporte, 16)

P1 ──► P2 ──► SM1 (acompanhamento em saúde mental) · P4 · P6
P1 ──► P3 ──► C2 (campanha de comunicação)
P4 ──► SB5 (saúde bucal coletiva) · C3 (educação em saúde)

F1 ──► F2 ──► V2 (mapa dos casos) · E1 (área afetada) · Equipamentos com mapa real
F1 ──► F3 ──► V4 (zoonoses) · E4 (vulneráveis) · P2 por microárea · F4

V1 ──► V2 · SM3 (notificação de autoagressão) · V5
SB1 ──► SB2 ──► SB3 ──► SB4
SM1 ──► SM2 · SM3 · SM4 · SM6
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

**Falta:**
- [ ] Mapa e coordenadas reais das unidades: vêm pelas fatias F1 e F2 do Território (ADR-0108).
- [ ] Sincronização com o CNES (depende de Integrações, domínio 20).
- [ ] Consultórios e capacidade.
- [ ] Vagas semanais da unidade.
- [ ] Acessibilidade.
- [ ] Exportação.
- [ ] Inspeção sanitária.
- [ ] Modalidades de CAPS (I, II, III, i, AD, AD III) e componentes da RAPS como capacidades da unidade (domínio 26, SM5;
  depende do catálogo de capacidades, item 29, MO1).
- [ ] Perfis operacionais e capacidades assistenciais da unidade (item 29, MO1); serviços (MO2); coordenadas e
  pontos operacionais (MO3).
- [ ] Tipo de unidade do CEO (Centro de Especialidades Odontológicas), pelo critério da ADR-0053 (domínio 25, SB4).

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
- A partir do atendimento: pedir exames (ADR-0094), encaminhar à Regulação (ADR-0088) e internar (ADR-0099).

**Falta:**
- [ ] Especialidade no atendimento (domínio 25, SB1; serve a todas as especialidades).
- [ ] Local de realização do atendimento (domicílio, via pública, local do evento, unidade móvel), item 29, MO3.
- [ ] Atividades coletivas: grupo, vacinação aberta, coleta com vários pacientes, reunião, capacitação, visita do
  ACS. Dependem das ações programáticas (domínio 22, P4) e do Território (domínio 21).

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
- [ ] Filtro de especialidade (a especialidade vem pelo domínio 25, SB1).
- [ ] Sala.
- [ ] Programas na marcação, como HiperDia e pré-natal (domínio 22, P4).
- [ ] Atividades coletivas (domínio 22, P4).
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

**Estado:** ⬜. Hoje só existem indicadores locais em cada tela.

**Falta:**
- [ ] Sala de Situação (protótipo `Painel.html`).
- [ ] Relatórios (protótipo `Relatorios.html`).
- [ ] Indicadores consolidados da rede, que vão receber também os indicadores programáticos (domínio 22, P6).
- [ ] Coordenações & Gerência (protótipo `Coordenacoes.html`).

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

**Estado:** 📐. Desenhado na [ADR-0108](./adr/0108-territorio-e-adscricao.md); detalhe em
[`territorio/MODELO-TERRITORIO.md`](./territorio/MODELO-TERRITORIO.md).

**Falta:**
- [ ] **F1 · Backend:** segmento, território, área e microárea com GeoJSON; responsabilidade equipe ↔ área e
  atribuição ACS ↔ microárea; coordenadas da unidade; migração das microáreas que hoje são texto em Equipe.
- [ ] **F2 · Tela Território:** mapa Leaflet com a área e as microáreas destacadas; mapa real em Equipamentos.
- [ ] **F3 · Cadastro territorial:** imóvel, domicílio, núcleo familiar e adscrição; famílias e cobertura em
  Equipes.
- [ ] **F4 · Escopo territorial de acesso:** o ACS vê a própria microárea e a equipe a própria área.

### 22. Programas, Ações e Linhas de Cuidado

**Estado:** 📐. Desenhado na [ADR-0109](./adr/0109-programas-acoes-e-linhas-de-cuidado.md); detalhe em
[`programas/MODELO-PROGRAMAS.md`](./programas/MODELO-PROGRAMAS.md).

**Falta:**
- [ ] **P1 · Catálogo:** programas e categorias cadastráveis, público-alvo e carga inicial; tela em cartões do
  protótipo.
- [ ] **P2 · Acompanhamento:** inscrever e encerrar; aba Programas e filtro em Pacientes; programas na equipe.
- [ ] **P3 · Campanhas:** cadastro, tela e consulta pública na landing page.
- [ ] **P4 · Ações programáticas:** ações pendentes, busca ativa, programa na marcação da Agenda.
- [ ] **P5 · Linhas de cuidado.**
- [ ] **P6 · Metas e indicadores programáticos.**

### 23. Vigilância em Saúde

**Estado:** 📐. Desenhado na [ADR-0110](./adr/0110-vigilancia-em-saude.md); detalhe em
[`vigilancia/MODELO-VIGILANCIA.md`](./vigilancia/MODELO-VIGILANCIA.md).

**Falta:**
- [ ] **V1 · Fundação:** agravo com a ficha específica como dado, notificação e notificação negativa,
  investigação e classificação; sugestão de notificação a partir de atendimento, internação e exame.
- [ ] **V2 · Epidemiologia avançada:** contato, surto, medida de controle, mapa e linha do tempo dos casos
  (o mapa depende das fatias F1 e F2 do Território).
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

**Estado:** 📐. Desenhado na [ADR-0111](./adr/0111-saude-bucal.md); detalhe em
[`saude-bucal/MODELO-SAUDE-BUCAL.md`](./saude-bucal/MODELO-SAUDE-BUCAL.md). A equipe eSB já existe (ADR-0103).

**Falta:**
- [ ] **SB1 · Base:** especialidade no atendimento e na Agenda; avaliação odontológica.
- [ ] **SB2 · Odontograma:** catálogo de condições, registros com histórico (notação FDI), projeção por data, tela.
- [ ] **SB3 · Tratamento:** catálogo geral de procedimentos, complemento odontológico (dente e faces), plano de
  tratamento e evolução.
- [ ] **SB4 · Regulação e prótese:** especialidades odontológicas no catálogo da Regulação, CEO como executante,
  tratamento protético.
- [ ] **SB5 · Saúde bucal coletiva:** detalhe odontológico das ações coletivas (depende de Programas P4 e do
  Território).

### 26. Saúde Mental

**Estado:** 📐. Desenhado na [ADR-0112](./adr/0112-saude-mental.md); detalhe em
[`saude-mental/MODELO-SAUDE-MENTAL.md`](./saude-mental/MODELO-SAUDE-MENTAL.md).

**Falta:**
- [ ] **SM1 · Base:** acolhimento, avaliação, acompanhamento ligado ao programa Saúde Mental (Programas P2), equipe
  e técnico de referência.
- [ ] **SM2 · PTS:** objetivos, metas, intervenções previstas, rede de apoio, versões e revisão; registro de
  intervenções.
- [ ] **SM3 · Risco e crise:** avaliação de risco com histórico, evento de crise, pós-crise, sugestão de notificação
  (Vigilância V1); modalidade da internação e comunicação ao Ministério Público (depende da regulação de urgência
  para acionar o SAMU).
- [ ] **SM4 · Rede:** referência e contrarreferência pela Regulação; cuidado compartilhado e matriciamento.
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

**Estado:** 📐 (camada transversal, não domínio de negócio). Desenhado na
[ADR-0115](./adr/0115-modelo-operacional-dos-equipamentos.md), que aceita e completa a ADR-0053; matriz e detalhe em
[`equipamentos/MODELO-OPERACIONAL.md`](./equipamentos/MODELO-OPERACIONAL.md).

**Falta:**
- [ ] **MO1 · Perfis e capacidades:** catálogos de perfil operacional e de capacidade assistencial, associação à
  unidade, aba "Capacidades" em Equipamentos (pré-requisito de SB4, SM5 e E3).
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
- [ ] Unificar os catálogos de Regulação (`ProcedimentoRegulado`), Laboratório (`ExameLaboratorial`) e o catálogo
  geral de procedimentos que vem com a ADR-0111.

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
