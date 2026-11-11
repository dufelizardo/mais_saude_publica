# Status dos domínios

**Documento vivo.** Mostra, domínio por domínio, o que já funciona e o que falta, para decidir e agir sobre os
próximos passos. **Atualize no mesmo PR** que entregar ou descobrir algo: marque a caixa, mova o item para
"Temos" e mude o estado na tabela.

- **Atualizado em:** 2026-11-08 (release 1.12.0)
- **Release em produção:** v1.12.0 (2026-11-08)
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
| 1 | [Organização da Rede / Unidades](#1-organização-da-rede--unidades-de-saúde) | ✅ | CNES, consultórios e vagas da unidade |
| 2 | [RH](#2-rh) | ✅ | banco de horas e horas realizadas |
| 3 | [Administrativo](#3-administrativo) | ✅ | teste de tela de Necessidades de pessoal |
| 4 | [Atendimento](#4-atendimento) | ✅ | atividades coletivas |
| 5 | [Paciente / Cidadão](#5-paciente--cidadão) | ✅ | indicador de risco |
| 6 | [Prontuário](#6-prontuário--histórico-clínico) | ✅ | ligar o vínculo assistencial nos ambientes |
| 7 | [Agendamento / Agenda](#7-agendamento--agenda) | ✅ | visão Mês e exportação |
| 8 | [Enfermagem](#8-enfermagem) | ✅ | plano de cuidados (SAE); aprazamento feito na RL4 |
| 9 | [Farmácia](#9-farmácia) | ✅ | — |
| 10 | [Laboratório / Diagnóstico](#10-laboratório--diagnóstico) | ✅ | controle de qualidade interno; integrações (LIS, PACS, LACEN, BPA) no domínio 20 |
| 11 | [Regulação](#11-regulação) | ✅ | RL1 e RL5 feitas (central de leitos, ficha do APH); SISREG, SIGTAP e telefonia do 192 no domínio 20 |
| 12 | [Leitos / Internação](#12-leitos--internação) | ✅ | RL1 a RL4 feitas (central de leitos, eletiva e censo, laudo de AIH, prescrição e checagem); SIH e faturamento no domínio 20 |
| 13 | [Estoque / Almoxarifado](#13-estoque--almoxarifado) | 🟡 | ES1 e ES2 feitas (catálogo, lotes, livro, alertas; central, requisição, transferência, inventário); falta a ES3 (emergências) |
| 14 | [Compras / Contratos / Fornecedores](#14-compras--contratos--fornecedores) | ⬜ | domínio inteiro |
| 15 | [Patrimônio / Manutenção](#15-patrimônio--manutenção) | ⬜ | domínio inteiro |
| 16 | [Transporte Sanitário](#16-transporte-sanitário) | 🟡 | T1 e T2 feitas (frota, plantão, viatura no SAMU; transporte eletivo, TFD, viagens e quilometragem); falta a T3 (emergências) |
| 17 | [Financeiro](#17-financeiro) | ⬜ | decidir o escopo e implementar |
| 18 | [Qualidade / Segurança / Auditoria](#18-qualidade--segurança--auditoria) | 🟡 | Qualidade não iniciada |
| 19 | [Indicadores / BI](#19-indicadores--bi) | ✅ | BI1 a BI5 feitas (Sala de Situação, relatórios, coordenações e gerência) |
| 20 | [Integrações / Identidade / Segurança / Governo](#20-integrações--identidade--segurança--governo) | 🟡 | ligar a exigência de permissão; integrações |
| 21 | [Território e Adscrição](#21-território-e-adscrição) | ✅ | F1 a F4 feitas; ligar o escopo territorial por rede |
| 22 | [Programas, Ações e Linhas de Cuidado](#22-programas-ações-e-linhas-de-cuidado) | ✅ | P1 a P6 feitas; critérios clínicos dos elegíveis e linhas recorrentes |
| 23 | [Vigilância em Saúde](#23-vigilância-em-saúde) | 🟡 | V1, V2 e V4 (vetores, animais e ambiental) feitas; sanitária e trabalhador (V3, V5) |
| 24 | [Imunização](#24-imunização) | 🟡 | I1 a I4 feitas (caderneta, Sala de Vacina, estoque, cadeia de frio, cobertura, campanhas e ESAVI); I5 |
| 25 | [Saúde Bucal](#25-saúde-bucal) | ✅ | SB1 a SB5 feitas; CPO-D e indicadores pela P6 e pelo BI |
| 26 | [Saúde Mental](#26-saúde-mental) | ✅ | SM1 a SM6 feitas, com o SAMU acionado pela crise |
| 27 | [Gestão de Emergências e Desastres](#27-gestão-de-emergências-e-desastres) | 🟡 | E1 a E3, E5 e E6 feitas (núcleo; contingência; coordenação; desmobilização e relatório; eventos especializados); falta a E4 |
| 28 | [Comunicação e Educação em Saúde](#28-comunicação-e-educação-em-saúde) | 🟡 | C1, C2 e C4 feitas (comunicado, conteúdo, portal e e-mail; campanhas e materiais; educação permanente); fatias C3, C5 e C6 |
| 29 | [Modelo Operacional dos Equipamentos](#29-modelo-operacional-dos-equipamentos) (camada) | ✅ | MO1 a MO4 feitas |
| 30 | [Gestão da Rede de Atenção](#30-gestão-da-rede-de-atenção) | 🟡 | GR1 feita (oferta e cotas da PPI na Regulação); GR2 (redes temáticas) e GR3 (cobertura) |
| 31 | [Intersetorialidade e Proteção Social](#31-intersetorialidade-e-proteção-social) | 🟡 | IS1 feita (catálogo e encaminhamento); uso pela Vigilância e pelas Emergências |

**Resumo:** 31 itens mais Documentos: 15 ✅ · 13 🟡 · 3 ⬜ (e Documentos 🟡). A Qualidade é a parte não
iniciada do domínio 18; as Integrações, a parte não iniciada do domínio 20.

### Ordem e dependências das fatias

**Prontas para começar** (não dependem de nada que falte):

| Fatia | Domínio | O que é |
|---|---|---|

**Precisam de desenho (ADR) antes:** nenhum dos itens da ordem de 2026-11-08; os ⬜ restantes (14, 15, 17) ainda pedem ADR.

**Dependências entre fatias** (A → B: B precisa de A):

```text
MO1 ✅ ──► SB4a ✅ (CEO) · SM5 ✅ (CAPS e RAPS) · E3 ✅ (capacidade emergencial) · MO2 ✅ · MO4 ✅
MO3 ✅ ──► SAMU (com Transporte, 16)

P1 ✅ ──► P2 ✅ ──► SM1 ✅ · P4 ✅ · P6 ✅ ──► BI3 ✅ (indicadores da rede)
BI1 ✅ ──► BI2 ✅ (situação operacional) · BI3 ✅ ──► BI4 ✅ (relatórios) · BI5 ✅ (coordenações)
P1 ✅ ──► P3 ✅ ──► C2 ✅ (campanha de comunicação)
P4 ✅ ──► SB5 ✅ (saúde bucal coletiva) · C3 (educação em saúde)

F1 ✅ ──► F2 ✅ ──► V2a ✅ (mapa dos casos) · E1 ✅ (área afetada) · Equipamentos com mapa real ✅
F1 ✅ ──► F3a ✅ ──► F3b ✅ ──► V4a ✅ (vetores) ──► V4b ✅ (animais) ──► V4c ✅ (ambiental) · E4 (vulneráveis) · P2 por microárea ✅ · F4 ✅

V1 ✅ ──► V2a ✅ · SM3 (notificação de autoagressão ✅) · V5
V2a ✅ ──► V2b ✅ ──► E1 ✅ (surto como gatilho do evento emergencial)
SB1 ✅ ──► SB2 ✅ ──► SB3a ✅ ──► SB3b ✅ ──► SB4a ✅ ──► SB4b ✅ ──► SB5 ✅
SM1 ✅ ──► SM2 ✅ · SM3 ✅ (parcial) · SM4 ✅ ──► SM6 ✅
IS1 ✅ ──► SM6 ✅ · V3 (denúncia e violência) · E4 (abrigos)
E1 ✅ ──► E2 ✅ ──► E3 ✅ ──► E4 · E5 ✅ ──► E6a ✅ ──► E6b ✅
C1 ✅ ──► C2 ✅ ──► C3 · C6
C1 ✅ ──► C4 ✅ · C5
I1 ✅ ──► I2 ✅ · I3 ✅ (vacinação na Sala de Situação) · I4 ✅ (ESAVI na Vigilância) · I5 (domínio 20)
```

**Dependem de itens ainda não desenhados** (precisam de ADR de domínio antes):

| Pré-requisito ainda sem desenho | Fatias que espera |
|---|---|
| Documentos D2 | V3 (autos da VISA), C2 (arquivos dos materiais) |
| Integrações (20) | V5 (SINAN), C5 (SMS e push), sincronização com CNES |
| Transporte T3 (16), com aprovação | E4 (veículos) |
| Estoque ES3 (13), com aprovação | E4 (insumos) |

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
- [x] Componentes da RAPS no catálogo do modelo operacional e o acolhimento noturno do CAPS (domínio 26, SM5,
  [ADR-0153](./adr/0153-saude-mental-caps-e-raps.md)).
- [x] Perfis operacionais e capacidades assistenciais da unidade (item 29, MO1, [ADR-0117](./adr/0117-modelo-operacional-perfis-e-capacidades.md)).
- [x] Serviços da unidade, com setores e leitos por serviço (item 29, MO2, [ADR-0165](./adr/0165-modelo-operacional-servicos-da-unidade.md)).
- [x] Pontos operacionais, base, rotas e localização da unidade móvel (item 29, MO3, [ADR-0166](./adr/0166-modelo-operacional-ponto-operacional.md)).
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
- [x] Conteúdo e material didático ligados aos treinamentos, com competências (domínio 28, C4, [ADR-0173](./adr/0173-educacao-permanente.md)).
- [ ] Competências exigidas por cargo e o painel de quem não as tem; a Necessidade de pessoal passar a usar o catálogo.
- [x] Turnos fora da lotação por mobilização em emergência ativa, como exceção controlada (domínio 27, E3, [ADR-0158](./adr/0158-emergencias-coordenacao.md)).
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
- [x] Local de realização do atendimento (domicílio, via pública, local do evento, ponto da unidade), item 29, MO3 ([ADR-0166](./adr/0166-modelo-operacional-ponto-operacional.md)).
- [ ] Atividades coletivas: vacinação aberta, coleta com vários pacientes, reunião, capacitação. O grupo e a visita
  do programa já são ações programáticas (domínio 22, P4, ADR-0132); falta o registro do atendimento coletivo.

### 5. Paciente / Cidadão

**Estado:** ✅

**Temos:**
- Cadastro do paciente (ADR-0040).
- Tela Pacientes com lista, detalhe e prontuário (ADR-0052).

**Falta:**
- [x] Aba Programas, indicador "Em programas de saúde" e filtro por programa (domínio 22, P2).
- [x] Aba Vacinação, com a caderneta (domínio 24, Imunização).
- [x] Aba Anexos ([ADR-0172](./adr/0172-documentos-anexos.md)).
- [x] Vínculo do paciente com a unidade, pela adscrição (domínio 21, F3).
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
- [x] Prescrição com aprazamento: prescrição hospitalar de 24 horas, horários aprazados e checagem ([ADR-0187](./adr/0187-prescricao-hospitalar.md), domínio 12, fatia RL4).

### 9. Farmácia

**Estado:** ✅

**Temos:**
- Medicamento, lote e dispensação (ADRs 0049 a 0051).
- Livro de movimentação do estoque (ADR-0057) e tela própria (ADR-0058).
- Transferência entre unidades com recebimento conferido (ADRs 0059 e seguintes).

**Falta:**
- [ ] Os "Em breve" pontuais da tela (rever na próxima passada pela Farmácia).

### 10. Laboratório / Diagnóstico

**Estado:** ✅ (L1 a L4 da ADR-0180, fechadas em 2026-11-09)

**Temos:**
- Catálogo, pedido, coleta em amostras, resultado, liberação e retificação (ADR-0093).
- Listas de trabalho por etapa (ADR-0094).
- Resultado no prontuário e no atendimento, laudo imprimível e recoleta (ADR-0095).
- Faixas de referência por sexo e idade, valores críticos e a evolução do exame no tempo, com gráfico, no prontuário
  ([ADR-0180](./adr/0180-laboratorio-faixas-de-referencia.md), fatia L1).
- Ciência de quem pediu (lista "Para ciência", reaberta na retificação), comunicação do valor crítico pelo laboratório (lista "Críticos
  a comunicar" e e-mail ao solicitante) e entrega do resultado ao paciente, com terceiro identificado e segunda via
  ([ADR-0181](./adr/0181-laboratorio-ciencia-critico-entrega.md), fatia L2).
- Exames de imagem no mesmo catálogo e pedido: serviço de imagem com a capacidade `DIAGNOSTICO_POR_IMAGEM`, realização pelo técnico em
  radiologia, laudo assinado pelo radiologista, adendo e achado crítico na comunicação da L2 ([ADR-0182](./adr/0182-exames-de-imagem.md), fatia L3).
- Laudo em PDF gerado no servidor, com assinatura eletrônica interna em cada resultado (o profissional logado assina; hash do conteúdo),
  código de verificação, QR code, versões e a página pública "Verificar laudo"; insumos de coleta e de realização baixados do
  Estoque, com o alerta de consumo sem saldo ([ADR-0183](./adr/0183-laudo-pdf-assinado-e-insumos.md), fatia L4).

**Falta** (pendências pontuais; as integrações estão no domínio 20 e não impedem):
- [x] L2: aviso ao solicitante quando o resultado é liberado, com urgência no crítico, e entrega ao paciente ([ADR-0181](./adr/0181-laboratorio-ciencia-critico-entrega.md)).
  O envio eletrônico ao paciente, com consentimento, fica na C5 e no domínio 20.
- [x] L3: exames de imagem (diagnóstico além do laboratório), com o laudo do radiologista ([ADR-0182](./adr/0182-exames-de-imagem.md)).
- [x] L4: laudo PDF com assinatura eletrônica interna e os insumos de coleta baixados do Estoque ([ADR-0183](./adr/0183-laudo-pdf-assinado-e-insumos.md)).
- [x] Gráfico da evolução de um exame no tempo ([ADR-0180](./adr/0180-laboratorio-faixas-de-referencia.md)).
- [x] Valores de referência por sexo e idade, com os valores críticos ([ADR-0180](./adr/0180-laboratorio-faixas-de-referencia.md)).
- [ ] Controle de qualidade interno.
- Integrações, no domínio 20: equipamentos (LIS, HL7, ASTM), PACS e DICOM (com a lista de trabalho MWL), RIS, LACEN, SIGTAP e BPA, certificado ICP-Brasil; envio do resultado
  ao paciente pelo Meu SUS Digital ou RNDS.

### 11. Regulação

**Estado:** ✅

**Temos:**
- Central de Regulação do Acesso: catálogo, solicitação, fila e eventos (ADR-0087).
- Tela com "Encaminhar" no atendimento (ADR-0088).
- Agendamento na executante, desfecho com contrarreferência e vínculo assistencial (ADR-0089).
- Regulação de urgência ([ADR-0176](./adr/0176-regulacao-de-urgencia.md)): central do 192 pela capacidade do modelo operacional, ocorrência
  com ou sem paciente, regulação médica (orientação, envio de recurso, encaminhamento, transferência), destino com perfil Urgência e vaga
  zero, deslocamentos, recebimento no destino, tempos de resposta, acionamento pela crise em saúde mental e as ocorrências no prontuário.

**Falta:**
- [x] Regulação de urgência e SAMU, com o acionamento pela crise em saúde mental (domínio 26, SM3) ([ADR-0176](./adr/0176-regulacao-de-urgencia.md)).
- [x] Viaturas do SAMU com plantão e tripulação no lugar do recurso em texto ([ADR-0178](./adr/0178-transporte-sanitario.md)); o texto segue aceito
  para a central sem frota cadastrada.
- [x] Ficha clínica do atendimento pré-hospitalar ([ADR-0188](./adr/0188-ficha-aph.md), fatia RL5).
- [ ] Telefonia ou integração com o 192 (domínio 20).
- [x] Regulação de internação e de leitos entre unidades, com o domínio 12 ([ADR-0184](./adr/0184-regulacao-de-leitos.md), fatia RL1).
- [x] RL5: ficha clínica do atendimento pré-hospitalar na ocorrência de urgência, com sinais vitais em série, procedimentos, medicamentos,
  desfecho, finalização e adendo ([ADR-0188](./adr/0188-ficha-aph.md)).
- [x] Cotas por unidade e procedimento (PPI), consumidas na autorização, com extracota justificada (Gestão da Rede, item 30,
  [ADR-0179](./adr/0179-gestao-da-rede-de-atencao.md)).
- [ ] Integração com SISREG e SIGTAP.
- [ ] Conceder o papel Médico regulador quando a autorização for ligada.

### 12. Leitos / Internação

**Estado:** ✅

**Temos:**
- Leito em setor assistencial e internação com troca de leito e alta médica (ADR-0098).
- Higienização, bloqueio, mapa e indicadores.
- Tela com "Internar" no atendimento (ADR-0099).
- Internações no prontuário (ADR-0100).

**Falta:**
- [x] Central de regulação de leitos entre unidades, com reserva de leito com prazo e vaga zero ([ADR-0184](./adr/0184-regulacao-de-leitos.md), fatia RL1).
- [x] Laudo para solicitação de AIH e autorização interna, com o número da faixa do gestor ([ADR-0186](./adr/0186-laudo-aih.md), fatia RL3).
- [ ] SIH e faturamento da AIH (domínio 20).
- [x] Reserva de leito para cirurgia eletiva: internação eletiva agendada, reserva na janela de dois dias, admissão, falta,
  remarcação e cancelamento ([ADR-0185](./adr/0185-internacao-eletiva-e-censo.md), fatia RL2).
- [x] Censo diário formal: fechamento por unidade e dia, paciente-dia, taxa de ocupação, média de permanência, censo nominal e
  reabertura com motivo ([ADR-0185](./adr/0185-internacao-eletiva-e-censo.md), fatia RL2).
- [x] Prescrição e dieta hospitalar: medicamentos, dieta e cuidados, aprazamento, checagem pela enfermagem e pela nutrição, posto de
  enfermagem e prescrições no prontuário ([ADR-0187](./adr/0187-prescricao-hospitalar.md), fatia RL4).
- [x] Restrição de leito por idade: faixa etária adulto, pediátrico ou neonatal, com justificativa do médico ([ADR-0184](./adr/0184-regulacao-de-leitos.md)).
- [x] Limite de leitos autorizados por capacidade, mantido pela gestão ([ADR-0171](./adr/0171-limite-de-leitos-por-capacidade.md)).
- [x] Leitos extras ativáveis em emergência, como quantidade ativada (domínio 27, E3, [ADR-0158](./adr/0158-emergencias-coordenacao.md)).
- [x] Modalidade da internação (voluntária, involuntária, compulsória) e comunicação ao Ministério Público em 72h na
  involuntária, pela Lei 10.216/2001 (domínio 26, SM3).
- [ ] Sincronização com os leitos do CNES.

### 13. Estoque / Almoxarifado

**Estado:** 🟡. Desenho na [ADR-0177](./adr/0177-estoque-e-almoxarifado.md); detalhe em
[`estoque/MODELO-ESTOQUE.md`](./estoque/MODELO-ESTOQUE.md).

**Temos:**
- **ES1:** catálogo de materiais da rede (com ou sem lote e validade), estoque por unidade em lotes, livro de movimentação (entrada por
  nota fiscal, doação ou saldo inicial; saída por consumo para um setor, pelos lotes que vencem primeiro; perda; ajuste de inventário),
  estoque mínimo, alertas de mínimo e validade e o valor do estoque. Tela em Administrativo e o papel Almoxarife.

**Falta:**
- [x] **ES2:** almoxarifado central (capacidade Central de abastecimento), requisição das unidades, transferência em duas etapas com
  divergência e inventário com fechamento ([ADR-0189](./adr/0189-estoque-central-requisicao-inventario.md)).
- [ ] **ES3:** consumido pela mobilização em emergência: reserva, consumo extraordinário, redistribuição (domínio 27, E4).
- [ ] Fornecedor cadastrado e pedido de compra (domínio 14); custo médio e contabilidade (domínio 17); código de barras.

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

**Estado:** 🟡. Desenho na [ADR-0178](./adr/0178-transporte-sanitario.md); detalhe em
[`transporte/MODELO-TRANSPORTE.md`](./transporte/MODELO-TRANSPORTE.md).

**Temos:**
- **T1:** frota (placa, prefixo, tipo, lotação; viatura do SAMU só em unidade com o perfil Atenção móvel), situação com histórico
  (manutenção, em serviço, inativo), plantão das viaturas com a tripulação mínima das Portarias 2.048/2002 e 1.010/2012, e a viatura
  empenhada pela regulação de urgência, em serviço no envio e livre no fim. Tela Frota e transporte e o papel Coordenador de frota.
- **T2:** solicitação de transporte eletivo (consulta, exame, hemodiálise, terapia seriada, alta, TFD) ligada ao agendamento, à
  regulação ou à internação, com acompanhante e recorrência; TFD com laudo e a Comissão de TFD; viagem com veículo, condutor, lugares,
  janela, embarques, saída, falta e chegada com a quilometragem; relatório por veículo ([ADR-0190](./adr/0190-transporte-eletivo-e-tfd.md)).

**Falta:**
- [ ] **T3:** consumido pela mobilização em emergência: veículos e ambulâncias (domínio 27, E4).
- [ ] Plantão ligado à escala geral; combustível, custos, GPS; o veículo como bem patrimonial (domínio 15); ajuda de custo e diárias do
  TFD (domínio 17); roteirização.

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
- [x] **Vacinação na Sala de Situação** (fatia I3 da Imunização,
  [ADR-0149](./adr/0149-imunizacao-cobertura-e-campanhas.md)): doses do período por estratégia, cobertura das vacinas de
  referência, homogeneidade, abandono e campanhas de vacinação com o andamento, no lugar do bloco "Em breve".

**Falta:**
- [x] **BI4 · Relatórios** ([ADR-0167](./adr/0167-bi-relatorios.md)): catálogo de cinco relatórios sobre os números dos
  domínios, com período e unidade, prévia na tela, CSV na trilha de auditoria e impressão. Sem envio agendado.
- [x] **BI5 · Coordenações e gerência** ([ADR-0175](./adr/0175-coordenacoes-e-gerencia.md)): indicadores somados por regional, estrutura de gestão e quem responde, como histórico.
- [ ] Cargo gestor no RH, metas por coordenação e exportação do organograma.

### 20. Integrações / Identidade / Segurança / Governo

**Estado:** 🟡

**Temos:**
- Login com CPF ou matrícula, JWT e chave liga/desliga por ambiente (ADR-0055).
- Papéis, permissões e escopo por unidade (ADRs 0066, 0067); tela Usuários & Perfis (ADR-0068).
- Troca de senha e senha provisória (ADR-0069); recuperação por e-mail (ADR-0081).
- Encerramento de sessões (ADR-0078).

**Falta, na configuração dos ambientes** (passo a passo em
[`acesso/GUIA-LIGAR-AUTORIZACAO.md`](./acesso/GUIA-LIGAR-AUTORIZACAO.md)):
- [x] Ligar o login em `qaa`, `homologacao` e `prod`, com o Secret `app-secrets` em cada um (2026-11-07).
- [ ] Garantir o próprio acesso e conceder os perfis; depois, ligar a exigência de permissão.
- [ ] Ligar o vínculo do prontuário (domínio 6).
- [ ] Escolher e contratar um SMTP para a recuperação de senha.

**Falta, em código:**
- [ ] MFA, sessões ativas por dispositivo e política de senha configurável.
- [ ] Login e recuperação de senha pelo gov.br.
- [ ] Os botões sem permissão aparecem por um instante ao abrir as telas (ADR-0079).
- [ ] **Integrações** (não iniciadas): arquitetura de adapters e depois CNES, e-SUS APS e SISAB, SIGTAP, SISREG e
  DATASUS. Do laboratório (domínio 10): equipamentos (LIS, HL7, ASTM), PACS, LACEN, BPA e o certificado ICP-Brasil no laudo.
  Nenhuma integração impede fechar um domínio: cada fatia entrega o fluxo interno e deixa o encaixe previsto.
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

**Estado:** ✅. Desenhado na [ADR-0109](./adr/0109-programas-acoes-e-linhas-de-cuidado.md), com as seis fatias feitas; detalhe em
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
- [x] **P5 · Linhas de cuidado** ([ADR-0152](./adr/0152-programas-linhas-de-cuidado.md)): linha cadastrável com as
  etapas como dado (cumpridas por atendimento, exame liberado, regulação realizada, vacina, alta de internação com CID
  ou registro manual; quantidade e prazo do início ou da etapa anterior); percurso do paciente com a situação calculada
  (cumprida, no prazo, atrasada, aguardando a anterior, dispensada), registro manual, dispensa e encerramento; painel
  com atrasados e a sugestão de abrir a linha para quem está no programa; carga da gestante e da primeira semana da
  criança; aba Linhas de cuidado em Programas e as linhas na aba Programas do paciente.

**Falta:**
- [x] Lista de elegíveis pelo público-alvo entre os adscritos da equipe (ADR-0128); faltam os critérios clínicos (gestação, condição).
- [ ] Linhas recorrentes e indicadores de linha (P6 e Sala de Situação), depois da P5 (ADR-0152).

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
- [x] **V4a · Controle de vetores** ([ADR-0162](./adr/0162-vigilancia-controle-de-vetores.md)): papéis Agente de
  endemias e Vigilância ambiental; vistoria ao imóvel do Território no modelo do PNCD (resultado, depósitos por tipo A1 a
  E, tratamento focal, ciclo bimestral) com a pendência de revisita; focos com o laboratório (negativo não conta) e o
  tratamento; índices do ciclo (IIP e Breteau, com a classificação do Ministério da Saúde) por área; ações de controle
  (bloqueio, nebulização, mutirão, educativa) ligadas ao caso de arbovirose ou ao surto, com os casos sem bloqueio sem
  expor o paciente; tela Zoonoses e vetores; a área em risco na Sala de Situação.
- [x] **V4b · Animais** ([ADR-0163](./adr/0163-vigilancia-zoonoses-animais.md)): animal de interesse (o tutor só com
  nome e telefone); observação do cão ou gato agressor por 10 dias ligada ao atendimento antirrábico notificado, com o
  desfecho visto por quem investiga; exames de raiva e de leishmaniose visceral canina, com o desfecho do cão reagente;
  vacinação antirrábica animal por posto, com os totais do ano.
- [x] **V4c · Ambiental** ([ADR-0164](./adr/0164-vigilancia-ambiental-agua-e-risco.md)): pontos de coleta e amostras da
  qualidade da água com a conformidade calculada pela Portaria GM/MS 888/2021 (E. coli, coliformes, cloro residual,
  turbidez, fluoreto) e a medida da fora do padrão; risco ambiental permanente, ativo ou controlado; a água fora do padrão
  na Sala de Situação.

**Falta:**
- [ ] Sugestão de notificação por exame positivo (o catálogo de exames não liga exame a agravo).
- [ ] **V3 · Sanitária:** estabelecimento regulado, inspeção, irregularidade, medida, licença (os autos dependem
  do domínio Documentos).
- [ ] O plano de amostragem mínimo da água e os demais parâmetros da portaria (agrotóxicos, metais).
- [ ] A cobertura da vacinação antirrábica animal (precisa da população animal estimada).
- [ ] O quarteirão como unidade de trabalho do agente de endemias.
- [ ] **V5 · Saúde do Trabalhador e integrações:** evento do trabalho, risco ocupacional, investigação;
  adaptadores, com o SINAN primeiro (depende do domínio 20, Integrações).

### 24. Imunização

**Estado:** 🟡. Desenhado na [ADR-0147](./adr/0147-imunizacao.md) (reservado pela
[ADR-0110](./adr/0110-vigilancia-em-saude.md)): imunobiológico como catálogo próprio, calendário como dado, dose
aplicada como registro clínico com retificação, estoque de vacinas da própria Imunização.

**Temos:**
- [x] **I1 · Fundação** ([ADR-0147](./adr/0147-imunizacao.md)): catálogo de 20 imunobiológicos e o calendário do PNI
  (criança, adolescente, adulto, idoso e gestante) como dado editável; registro da dose aplicada (quem aplicou, lote)
  ou transcrita da caderneta (data); dose repetida 409, fora da idade ou do intervalo só com justificativa;
  retificação e anulação; situação vacinal calculada (aplicada, atrasada, próxima, futura, fora da idade);
  caderneta na aba Vacinação do paciente; tela Sala de Vacina com as doses do dia e os adscritos com dose atrasada.
- [x] **I2 · Sala de vacina e cadeia de frio** ([ADR-0148](./adr/0148-imunizacao-sala-de-vacina.md)): capacidade
  "Sala de vacina" (transversal) no modelo operacional; na unidade com ela, lote de imunobiológico com livro de
  movimentação em doses (entrada, aplicação, estorno, perdas física e técnica, ajuste) e a dose aplicada saindo do
  lote com baixa automática; frasco multidose aberto com prazo e perda técnica no vencimento; câmara de refrigeração
  com duas leituras por dia, pendência do turno sem leitura e excursão que põe os lotes sob suspeita até a coordenação
  liberar ou inutilizar; abas Estoque e Cadeia de frio na Sala de Vacina.
- [x] **I3 · Cobertura e campanhas** ([ADR-0149](./adr/0149-imunizacao-cobertura-e-campanhas.md)): a dose de
  referência e a meta no calendário (dado editável); cobertura pelo cadastro (coorte de crianças adscritas de 1 a 12
  meses depois da idade recomendada), homogeneidade entre vacinas e entre territórios e taxa de abandono, por unidade,
  equipe e microárea; busca ativa filtrada por equipe e microárea, com o agente comunitário, onde mora e a lista para
  imprimir; andamento da campanha de vacinação pelas pessoas vacinadas, com as vacinas da campanha; aba Cobertura na
  Sala de Vacina, bloco Vacinação na Sala de Situação e cobertura no detalhe da equipe.
- [x] **I4 · ESAVI e erro de imunização** ([ADR-0150](./adr/0150-imunizacao-esavi.md)): os dois agravos no catálogo da
  Vigilância, com ficha; a notificação ligada às doses (origem Vacinação), com a gravidade do ESAVI; o encerramento do
  ESAVI pela causalidade da OMS e pela conduta sobre o esquema; a contraindicação vacinal na caderneta e no registro da
  dose (só com justificativa); a dose fora do calendário como sugestão de erro de imunização; "Notificar ESAVI" na
  caderneta e o ESAVI no bloco Vacinação da Sala de Situação.

**Falta:**
- [ ] **Remanejamento de lote entre unidades,** pedido à rede de frio e termômetro automático (depois da I2;
  ADR-0148).
- [ ] **Cobertura sobre os nascidos vivos (SINASC)** e de adolescentes, adultos e gestantes; cobertura vacinal nas
  metas programáticas (depois da I3; ADR-0149).
- [ ] **Alerta de ESAVI por lote** e revogação da contraindicação pelo CRIE (depois da I4; ADR-0150).
- [ ] **I5 · Integração com o SI-PNI/RNDS** (depende do domínio 20).

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

**Estado:** ✅ (SM1 a SM6). Desenhado na [ADR-0112](./adr/0112-saude-mental.md); detalhe em
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
- [x] **SM5 · CAPS e RAPS** ([ADR-0153](./adr/0153-saude-mental-caps-e-raps.md)): o componente da RAPS como dado do
  modelo operacional (perfis e capacidades, com as capacidades novas de Consultório na Rua, Centro de convivência,
  Unidade de acolhimento, Serviço residencial terapêutico e Geração de trabalho e renda); painel da RAPS por componente
  com os casos, as intervenções dos últimos 90 dias e as lacunas da rede; acolhimento noturno do CAPS com vagas,
  permanência máxima, entrada, prorrogação e saída; telas Rede (RAPS) e Acolhimento noturno.
- [x] **SM6 · Intersetorialidade e indicadores** ([ADR-0155](./adr/0155-saude-mental-intersetorialidade-e-indicadores.md)):
  encaminhamento do caso a instituições de outros setores (pelo catálogo da IS1, ADR-0154), com recebimento, retorno
  (concluído ou recusado) e cancelamento; o sem retorno no prazo como pendência do caso e filtro da lista; cinco
  indicadores no conjunto da P6 (PTS em dia, avaliação em até 30 dias, pós-crise em até 7 dias, comunicação ao MP no
  prazo e retorno intersetorial), com metas, série e Sala de Situação.

**Falta:**
- [x] **SM3 · o que ficou:** o acionamento do SAMU, que abre a ocorrência na regulação de urgência e registra a etapa com o protocolo
  ([ADR-0176](./adr/0176-regulacao-de-urgencia.md)). A notificação de violência autoprovocada já sai da crise pela Vigilância
  ([ADR-0124](./adr/0124-vigilancia-epidemiologica-fundacao.md)).
- [ ] CAPS de referência por território e a produção do CAPS para o faturamento (RAAS, depende do domínio 20), depois da
  SM5 (ADR-0153).
- [ ] A guia de encaminhamento intersetorial para imprimir (depende de Documentos).

### 27. Gestão de Emergências e Desastres

**Estado:** 🟡. Desenhado na [ADR-0113](./adr/0113-emergencias-e-desastres.md); detalhe em
[`emergencias/MODELO-EMERGENCIAS.md`](./emergencias/MODELO-EMERGENCIAS.md).

**Temos:**
- [x] **E1 · Núcleo** ([ADR-0156](./adr/0156-emergencias-nucleo.md)): catálogos de tipo (COBRADE) e nível (0 a 3);
  evento emergencial com fonte, área e o surto de origem (registrado, monitorado, descartado, convertido); emergência
  com nível em histórico, território afetado (polígono e microáreas atingidas), ESPIN e decreto registrados e
  encerramento; avaliação de impacto datada com sugestões dos donos (pessoas das microáreas, unidades na área, leitos
  fora de uso); permissões `EMERGENCIA.REGISTRAR`, `EMERGENCIA.ATIVAR` e `CONTINGENCIA.GERENCIAR`; tela Emergências,
  aviso na Sala de Situação e atalho no surto.
- [x] **E2 · Contingência** ([ADR-0157](./adr/0157-emergencias-contingencia.md)): plano versionado (rascunho,
  vigente, substituída, revogada) com níveis, critérios e ações previstas por nível, recursos, contatos e unidades;
  ativação na emergência pela autoridade, com passos (ativado, nível alterado automático, desativado) e as ações
  previstas do nível atual com prazo; encerramento desativa os planos; telas de planos e de catálogos.
- [x] **E3 · Coordenação** ([ADR-0158](./adr/0158-emergencias-coordenacao.md)): quadro de ações (geradas das ações
  previstas do plano ou avulsas; atrasadas no alto, também na Sala de Situação); mobilização registrada e executada pelo
  dono (o profissional mobilizado ganha turno na Escala sem lotação na unidade de destino, rastreável; o medicamento segue
  a transferência da Farmácia; insumo, equipamento e veículo como necessidade); capacidade emergencial cadastrada por
  unidade e ativada na emergência até o limite; unidades afetadas com a situação operacional.
- [x] **E5 · Pós-evento** ([ADR-0159](./adr/0159-emergencias-desmobilizacao-e-relatorio.md)): situação "em
  desmobilização" (libera de uma vez mobilizações, capacidade e planos, bloqueia o que é novo, mostra as pendências);
  encerramento sem ação aberta; relatório pós-evento com os números calculados, o texto da equipe, as recomendações que
  abrem a revisão do plano (E2), finalização e CSV.
- [x] **E6a · Evento de massa e estruturas temporárias** ([ADR-0160](./adr/0160-emergencias-evento-de-massa-e-estruturas-temporarias.md)):
  perfil do tipo (geral, evento de massa, epidemia, desastre ambiental); emergência planejada para o evento de massa,
  com os dados do evento (natureza, local, período, público, organizador, risco) e o início da operação; estruturas
  temporárias (posto médico, tenda de hidratação, hospital de campanha como capacidade, ligadas a uma unidade de
  referência) com os boletins; desativadas na desmobilização; os números no relatório; o evento planejado na Sala de
  Situação.
- [x] **E6b · Epidemia e desastre ambiental** ([ADR-0161](./adr/0161-emergencias-epidemia-e-exposicao-ambiental.md)):
  agravo da epidemia (o escolhido ou o do surto; sensível não entra) e painel lido da Vigilância (notificados,
  confirmados, em investigação, óbitos, semana atual e anterior, curva por semana epidemiológica, na área afetada);
  exposição ambiental datada (meio, agente, população exposta, unidades na área, medidas, recomendação); os números no
  relatório.

**Falta:**
- [ ] A escolha manual das microáreas na tela (a API aceita a lista).
- [ ] **E4 · Integração da rede:** redistribuição pela Farmácia, Regulação, vulneráveis por Território e Programas,
  pacientes deslocados; Estoque e Transporte quando existirem.
- [ ] Ambulâncias da estrutura como veículos (depende de Transporte) e hospital de campanha com leitos próprios.
- [x] Comunicação de risco à população, pelo comunicado emergencial (domínio 28, [ADR-0168](./adr/0168-comunicacao-basica.md)).

### 28. Comunicação e Educação em Saúde

**Estado:** 🟡. Desenhado na [ADR-0114](./adr/0114-comunicacao-e-educacao-em-saude.md); detalhe em
[`comunicacao/MODELO-COMUNICACAO.md`](./comunicacao/MODELO-COMUNICACAO.md).

**Temos:**
- [x] **C1 · Comunicação básica** ([ADR-0168](./adr/0168-comunicacao-basica.md)): comunicado com prioridade e período, conteúdo educativo com revisão
  técnica, validade e versão, canais e publicação com resultado; avisos e orientações na página pública; e-mail só a profissionais.

**Falta:**
- [x] **C2 · Campanhas de comunicação** ([ADR-0174](./adr/0174-campanhas-de-comunicacao.md)): campanha com público, territórios, temas, canais, conteúdos revisados e comunicados, alcance por canal, portal; material educativo com link (arquivo com a D2).
- [ ] **C3 · Educação em saúde:** conteúdo e material nas ações educativas de Programas (P4).
- [x] **C4 · Educação permanente** ([ADR-0173](./adr/0173-educacao-permanente.md)): catálogo de competências, competências e conteúdos revisados no treinamento, competências do profissional pelas participações.
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

- [x] **MO2 · Serviços** ([ADR-0165](./adr/0165-modelo-operacional-servicos-da-unidade.md)): catálogo editável de tipos de serviço (11, com
  capacidade exigida opcional), serviços da unidade como histórico, setores por serviço, leitos por serviço calculados
  pelos setores, aba Serviços em Equipamentos e aba Tipos de serviço em Modelo operacional. A capacidade não encerra com
  serviço vigente que a exige.

- [x] **MO3 · Ponto operacional** ([ADR-0166](./adr/0166-modelo-operacional-ponto-operacional.md)): local de realização opcional no Atendimento
  (domicílio do cadastro ou descrito, via pública, evento, escola, ponto da unidade); pontos da unidade como histórico; base,
  rotas programadas e localização informada da unidade móvel, que é a unidade com o perfil Atenção móvel. Aba Pontos em
  Equipamentos e campo na gaveta do Atendimento. Sem GPS.
- [x] **MO4 · Domínios consultam capacidades** ([ADR-0170](./adr/0170-modelo-operacional-dominios-consultam-capacidades.md)): cada tipo de leito pede
  a sua capacidade, a capacidade não encerra com leito ativo, leito emergencial pede internação ou observação, e a sala de vacina
  exige `SALA_VACINA`. Saúde Mental, Saúde Bucal e Agenda ficam como estão, por decisão registrada.

### 30. Gestão da Rede de Atenção

**Estado:** 🟡. Desenho na [ADR-0179](./adr/0179-gestao-da-rede-de-atencao.md); detalhe em [`rede/MODELO-REDE.md`](./rede/MODELO-REDE.md).

**Temos:**
- **GR1:** oferta assistencial por executante, procedimento e mês, cotas da PPI para unidades e esferas, a Regulação consumindo a cota
  mais específica na autorização (extracota com justificativa; sem oferta cadastrada, sem controle), painel da PPI com usado e saldo, cópia
  de competência e o saldo na gaveta de autorização.

**Falta:**
- [ ] **GR2:** redes temáticas (Rede de Urgência, Rede Cegonha/Alyne, Pessoa com Deficiência, Doenças Crônicas) e referência estrutural, no
  padrão da RAPS (ADR-0153).
- [ ] **GR3:** cobertura da rede e indicadores de pactuação no BI.
- [ ] Teto financeiro (MAC) e programação físico-orçamentária (domínio 17); SISREG e SIGTAP (domínio 20).

### 31. Intersetorialidade e Proteção Social

**Estado:** 🟡. Reservado pela [ADR-0115](./adr/0115-modelo-operacional-dos-equipamentos.md); desenhado na
[ADR-0154](./adr/0154-intersetorialidade.md).

**Temos:**
- [x] **IS1 · Catálogo e encaminhamento** ([ADR-0154](./adr/0154-intersetorialidade.md)): catálogo único de
  instituições externas (setor e tipo, contato, ativa), lido por quem está logado e mantido com
  `INTERSETORIAL.GERENCIAR` (Gestor e Assistente social); encaminhamento com origem em qualquer domínio (enviado,
  recebido, concluído, recusado, cancelado; sem retorno derivado do prazo); tela Instituições da rede intersetorial.
  Primeiro uso: a Saúde Mental (SM6).

**Falta:**
- [ ] O encaminhamento pela Vigilância (violência: Conselho Tutelar, CREAS) e pelas Emergências (abrigos), com as
  próprias fatias.
- [ ] O painel de encaminhamentos por instituição, para a gestão da rede intersetorial.
- [ ] A guia de encaminhamento para imprimir (Documentos) e a integração com o CadÚnico e o prontuário SUAS
  (domínio 20, adiado).

## 3. Pendências transversais

**Catálogos de procedimento:**
- [x] Catálogo geral de procedimentos, com SIGTAP opcional (ADR-0136).
- [ ] Unificar os catálogos de Regulação (`ProcedimentoRegulado`), Laboratório (`ExameLaboratorial`) e o catálogo
  geral de procedimentos (ADR-0136).

**Domínio "Documentos"** (🟡): anexos e documentos formais reaproveitáveis por qualquer domínio, com versão,
aprovação e assinatura.
- [x] **D1 · Anexos do paciente** ([ADR-0172](./adr/0172-documentos-anexos.md)): tipos, versões com hash, retirada, volume próprio e aba Anexos.
- [ ] **D2:** outros donos (materiais da C2, autos da Vigilância). **D3:** guias para imprimir. **D4:** aprovação e assinatura digital.
- [ ] Backup do volume de documentos, antes de dado real.

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
