# Pendências e estado dos ambientes

Documento vivo, com **o que falta** no projeto (revisto na release **v1.12.0**, 2026-11-08). Cada item aponta
para a ADR ou o guia onde está o detalhe. Atualize este arquivo ao fechar ou abrir uma pendência.

## 1. Estado dos ambientes

O código é o mesmo nos quatro ambientes (v1.12.0, promovida em 2026-11-08). O que muda é o que cada um liga no próprio overlay
(`k8s/overlays/<ambiente>/kustomization.yaml`):

| Recurso | `dev` | `qaa` | `homologacao` | `prod` |
|---|---|---|---|---|
| Login (ADR-0055) | ✅ ligado | ✅ ligado | ✅ ligado | ✅ ligado |
| Exigência de permissão por perfil e unidade (ADR-0067) | ❌ | ❌ | ❌ | ❌ |
| Prontuário por vínculo assistencial (ADR-0076) | ❌ | ❌ | ❌ | ❌ |
| Recuperação de senha por e-mail (ADR-0081) | ❌ sem SMTP | ❌ | ❌ | ❌ |

O que isso significa:
- **Com login e sem exigência de permissão** (os quatro ambientes), todo usuário logado faz tudo. Os perfis concedidos
  ficam guardados e só restringem quando a exigência for ligada.
- **A auditoria grava em todos os ambientes.** Os eventos anteriores ao login ficaram sem a pessoa (CPF vazio).
- **Cada item depende do anterior:** o vínculo só age com a permissão ligada, e a permissão só com o
  login ligado.

Na rede de casa, cada ambiente abre sem configurar o aparelho em `http://maissaudepublica-dev.local`, `-qaa.local`, `-cert.local` e
`http://maissaudepublica.local` ([ADR-0169](./adr/0169-nomes-amigaveis-na-rede-local.md)). O nome chega a cada ambiente com a promoção.

## 2. Para ligar, nesta ordem

O passo a passo está em [`acesso/GUIA-LIGAR-AUTORIZACAO.md`](./acesso/GUIA-LIGAR-AUTORIZACAO.md).

1. ~~**Login em `qaa`, `homologacao` e `prod`.**~~ Ligado em 2026-11-07, com o Secret `app-secrets` em cada namespace.
2. **Antes de ligar a exigência de permissão, garantir o próprio acesso.** O administrador inicial só tem
   o perfil **Administrador da plataforma**, que não lê dado de saúde nem as áreas operacionais (ADR-0054).
   Há dois caminhos:
   - criar um perfil "Acesso total" (todas as permissões, inclusive `AUDITORIA.CONSULTAR` e
     `PRONTUARIO.CONSULTAR_SEM_VINCULO`) e concedê-lo à própria conta. Aceitável em `dev`;
   - **recomendado fora do `dev`:** uma segunda conta, com o perfil de trabalho, separada da conta de
     administração.
3. **Conceder os perfis às pessoas** e só então ligar `APP_SECURITY_AUTHORIZATION_ENABLED=true`.
4. **Vínculo do prontuário** (`APP_SECURITY_PRONTUARIO_POR_VINCULO_ENABLED=true`), depois que a permissão
   estiver estável.
5. **Recuperação de senha:** escolher um provedor SMTP e configurar as variáveis `SPRING_MAIL_*`,
   `APP_SECURITY_RECUPERACAO_SENHA_URL_FRONTEND` e `APP_EMAIL_REMETENTE` (ADR-0081).

## 3. Ações manuais fora do código

- [ ] Apagar o secret **`RENDER_DEPLOY_URL`** nas configurações do repositório. O deploy no Render foi
      removido e o workflow está desligado.
- [ ] Apagar o serviço antigo no painel do **Render**, se ainda existir.
- [x] Provisionar `app-secrets` nos namespaces de `qaa`, `homologacao` e `prod` (item 2.1).
- [ ] Escolher e contratar o provedor SMTP (item 2.5).

## 4. Infraestrutura do home-lab (ADR-0012)

- **`argocd-applicationset-controller` reiniciando sem parar:** mais de 4.000 reinícios em 21 dias. Não
  derruba nada, mas gasta CPU. Investigar ou desligar, se o ApplicationSet não for usado.
- **O projeto `lonewolf` divide o servidor** (CPU e disco mecânico) e não estava na conta de capacidade
  da ADR-0012.
- **Disco mecânico de 5400 RPM:** depois do reinício de 2026-10-01, as APIs levaram até 13 tentativas
  para subir. O `startupProbe` passou a 10 minutos. A troca por SSD continua recomendada.
- ~~**Migrações versionadas (Flyway)**~~ — feitas pela ADR-0090. Na próxima subida de cada ambiente, o
  Flyway marca o banco na V1 (esquema da v1.4.0) e aplica a V2. Mudança de esquema agora é migração.

## 5. Pendências por frente

### Acesso e segurança
- MFA, lista de **sessões ativas por dispositivo** e **política de senha** configurável: aparecem como
  "Em breve" na tela (ADRs 0068, 0069 e 0078).
- Recuperação de senha pelo **gov.br** (ADR-0069). A recuperação por e-mail já existe (ADR-0081).
- Na abertura da tela, os botões sem permissão aparecem por um instante, até o acesso carregar
  (ADR-0079).

### Auditoria
- ~~Exportação e retenção~~ — feitas pela ADR-0082 (CSV e 20 anos). PDF formatado só se houver pedido
  formal que o exija.
- ~~**Alertas**~~ — feitos pelas ADRs 0096 (detecção e análise) e 0097 (tela e contador no menu). Fora do escopo:
  detecção em tempo real, regras configuráveis pela tela, integração com SIEM e aviso ao titular dos dados.
  Os limites das regras precisam ser calibrados com o volume real de cada rede. O contador do menu não se
  atualiza sozinho (lê ao entrar no sistema e ao abrir a aba).

### Prontuário por vínculo
- ~~A regulação ainda não gera vínculo~~ — feito pela ADR-0089 (quarta condição).

### Regulação
- ~~Tela~~ e ~~fechamento do ciclo~~ — feitos pelas ADRs 0088 e 0089.
- ~~Vagas por unidade e procedimento (cotas e PPI)~~ — ADR-0179 (Gestão da Rede, GR1).
- Fora do escopo da ADR-0087: ~~regulação de urgência e SAMU~~ — ADR-0176; internação (depende de Leitos, #12),
  integração com SISREG e SIGTAP, e ~~cotas por unidade (PPI)~~ — ADR-0179.
- Regulação de urgência (ADR-0176): ~~viaturas no lugar do recurso em texto~~ — ADR-0178; ~~ficha clínica do
  atendimento pré-hospitalar~~ — ADR-0188 (RL5); telefonia e integração com o 192; reserva de leito na vaga zero; conceder os papéis **Atendente de regulação
  (TARM)** e **Médico regulador de urgência** na central quando a autorização for ligada.
- Conceder o papel **Médico regulador** a alguém no escopo do município ou da regional, quando a
  autorização for ligada.

### Equipamentos de Saúde
- ~~Tela pelo protótipo~~ — feita pela ADR-0102 (lista e painel da unidade). "Em breve" na tela: mapa e
  coordenadas, sincronização com o CNES, exportação, consultórios, vagas semanais da unidade, microáreas,
  acessibilidade. As equipes da unidade aparecem na aba Equipes desde a ADR-0104. Mapa, coordenadas e
  microáreas vêm pelo domínio Território (ADR-0108, fatias F1 e F2).
- Fora do escopo da ADR-0101: sincronização com o CNES; coordenadas e mapa; microáreas e famílias; equipes de
  Saúde da Família; inspeção sanitária; capacidade de consultórios; exportação; horário de atendimento
  estruturado.

### Leitos e internação
- ~~Internações no prontuário e vínculo de quem cuida do internado~~ — feitos pela ADR-0100. A tela (ADR-0099)
  não tem protótipo próprio.
- Fora do escopo da ADR-0098: ~~AIH interna~~ — ADR-0186 (RL3); SIH e faturamento no domínio 20; ~~central de regulação de leitos
  entre unidades~~ — ADR-0184 (RL1); ~~reserva de leito para cirurgia eletiva e censo diário formal~~ — ADR-0185 (RL2); ~~prescrição e dieta
  hospitalar~~ — ADR-0187 (RL4); sincronização com os leitos do CNES (domínio 20); ~~restrição de leito por idade~~ — ADR-0184.
- Fora do escopo da ADR-0184: rotina agendada para vencer reservas (o vencimento é visto na consulta); pedido de leito gerado
  sozinho pela transferência da urgência; faixa etária na troca de leito.
- Fora do escopo da ADR-0185: histórico do cadastro de leitos para o censo (os leitos contam como estão no fechamento); reserva
  da eletiva antes da janela de dois dias; fechamento automático do censo; envio do censo a sistemas externos (domínio 20).
- Fora do escopo da ADR-0186: laudo de mudança de procedimento e de prorrogação de permanência; espelho e faturamento da AIH;
  envio ao SIH e validação no SIGTAP (domínio 20); página pública de verificação do laudo de AIH.
- Fora do escopo da ADR-0187: dispensação por dose individualizada pela farmácia hospitalar; alerta de interação medicamentosa e
  alergia (base externa, domínio 20); balanço hídrico e sinais vitais estruturados; plano de cuidados da SAE; horários das
  refeições configuráveis por unidade.
- Fora do escopo da ADR-0188: telefonia e gravação do 192; estoque de medicamentos da viatura (ES3/T3); transmissão de
  eletrocardiograma; aplicativo off-line para tablet da viatura; conceder o papel **Equipe de intervenção do SAMU** quando a
  autorização for ligada.

### Laboratório
- Depois da ADR-0095: ~~laudo PDF assinado~~ — ADR-0183 (L4, com assinatura eletrônica interna; o ICP-Brasil é integração),
  ~~envio do resultado ao paciente e aviso ao solicitante quando o resultado é liberado~~ — ADR-0181 (L2; o envio
  eletrônico, com consentimento, fica na C5 e no domínio 20), ~~gráfico da evolução de um exame no tempo~~ — ADR-0180 (L1).
- A tela (ADR-0094) não tem protótipo próprio: segue o padrão de Farmácia e Regulação.
- Fora do escopo da ADR-0093: LACEN e vigilância, integração com equipamentos (LIS, HL7, ASTM), controle
  de qualidade, ~~insumos de coleta~~ — ADR-0183 (L4), ~~exames de imagem~~ — ADR-0182 (L3), ~~laudo PDF assinado~~ — ADR-0183 (L4), SIGTAP e BPA,
  e ~~valores de referência por sexo e idade~~ — ADR-0180 (L1).
- Fora do escopo da ADR-0180: gestação e outras condições clínicas como critério da faixa de referência.
- Fora do escopo da ADR-0181: central de notificações geral; prazo de liberação com alerta de atraso (L4 ou Indicadores).
- Fora do escopo da ADR-0183: código de barras do tubo; custo do exame pelos insumos (Financeiro, #17); assinatura
  qualificada com certificado ICP-Brasil (domínio 20).
- Fora do escopo da ADR-0182: cadastro de aparelhos com dose de radiação (com o RIS, domínio 20); ligação automática da autorização
  da Regulação ao pedido de imagem; laudo comparativo com exames anteriores.

### Agenda (diferenças com o protótipo `Agenda.html`, ADR-0092)
- Visão Mês e exportação ("Em breve"). ~~Filtro de especialidade~~ — feito pela ADR-0134 (SB1).
- Atividades coletivas (grupo, vacinação aberta, coleta com N pacientes, reunião, capacitação, visita do
  ACS) e sala: dependem de domínios que não existem. ~~Programas nas marcações (HiperDia, pré-natal)~~ — feito pela
  ADR-0132 (P4); o grupo e a visita do programa já são ações programáticas.
- Vaga liberada por falta oferecida à fila da Regulação ou a encaixe.
- ~~Horário de funcionamento da unidade fechando a agenda~~ — feito pela ADR-0101 (horário estruturado em
  turnos e unidade em obra ou inoperante sem vagas).
- Meta de ocupação configurável por unidade (está fixa em 85%); indicadores de encaixe e falta "de hoje".
- Mini-mês com marcações do mês inteiro; almoço desenhado como bloco; contador no menu; atualização ao
  vivo.

### Telas
- ~~**Perfil do profissional:** as 13 abas no padrão novo~~ — feito pelas ADRs 0084 a 0086.
- ~~Responsáveis por setor~~ e ~~edição de treinamento, ciclo e tipo de benefício~~ — feitos pela ADR-0083.
- Exportar e importar profissionais. ~~**Equipes**~~ — feitas pelas ADRs 0103 (backend) e 0104
  (tela, com "Atribuir equipe" em Profissionais e a aba Equipes em Equipamentos); ~~**Escalas**~~ — feitas pelas ADRs 0105 (backend) e 0106
  (tela, com "Ver escala" em Profissionais e em Equipes); "Em breve" na tela: banco de horas, realizado, exportação,
  cobertura de férias e filtro por função. Intervalo no turno e modelos de jornada (40h com 8h + 1h, 44h,
  30h, 20h, 12x36) pela ADR-0107; modelos cadastráveis pelo usuário ficam para depois. Fora do escopo das escalas: banco de horas e horas realizadas (ponto), confirmação do plantonista, troca
  pedida pelo profissional, recorrência, adicional noturno e a agenda lendo a escala. Território (famílias, microáreas georreferenciadas, cadastro domiciliar, cobertura) desenhado na ADR-0108, a
  implementar nas fatias F1 a F4. Fora do escopo das equipes: território (famílias, microáreas
  georreferenciadas, cadastro domiciliar), indicadores do Previne/PMAQ, programas e sincronização com o CNES. Programas vinculados à equipe vêm pela
  fatia P2 de Programas (ADR-0109).
- As telas refeitas não foram conferidas visualmente por quem implementou (ADRs 0074 a 0076).

### Testes
- **Cobertura Robot de interface das demais telas**, deixada para o final (ADR-0077): Necessidades de
  pessoal, RH (Cargos & salários, Benefícios, Desenvolvimento, Recrutamento, Folha; o perfil já tem cobertura pelas ADRs 0084 a 0086),
  Profissionais, Usuários & Perfis (além do encerramento de sessões) e Auditoria.
- **`SetorControllerTest` depende de banco limpo:** a limpeza dele apaga todos os setores e falha quando uma execução Robot
  deixou leitos (`Robot Leito ...`) ligados a um setor. Restringir a limpeza aos setores criados pelo próprio teste.

## 6. Roadmap de domínios

O estado de cada domínio, com o que já temos e o que falta, está em
[`STATUS-DOS-DOMINIOS.md`](./STATUS-DOS-DOMINIOS.md).

Detalhe em [`MAPA-DE-DOMINIOS.md`](./MAPA-DE-DOMINIOS.md), seção 4. Todos "só quando houver requisito
real":
- **Operação Assistencial:**
  - ~~#10 Laboratório e diagnóstico~~ — ADRs 0093 a 0095;
  - ~~#11 Regulação~~ — ADRs 0087 a 0089 (falta a regulação de urgência e de internação);
  - ~~#12 Leitos e internação~~ — ADRs 0098 a 0100;
  - #16 Transporte sanitário — ~~T1 (frota, plantão, viatura no SAMU)~~ — ADR-0178; ~~T2 (transporte eletivo e TFD)~~ — ADR-0190;
    ~~T3 (veículos nas emergências)~~ — ADR-0192; conceder o papel **Coordenador de frota**, o **Comissão de TFD** e `TRANSPORTE.SOLICITAR` às
    recepções quando a autorização for ligada; ajuda de custo e diárias do TFD (financeiro);
  - ~~agenda do profissional~~ — feita pelas ADRs 0091 e 0092. Faltam visão Mês, exportação, atividades
    coletivas e equipe (programas pela ADR-0132; especialidade pela ADR-0134);
  - ~~escalas~~ — ADRs 0105 a 0107 (com as equipes, ADRs 0103 e 0104);
  - na Enfermagem, `Cuidado` e prescrição com aprazamento.
- **Gestão e Inteligência:**
  - #13 Estoque — ~~ES1 (catálogo, lotes, livro, mínimo e alertas)~~ — ADR-0177; ~~ES2 (central, requisição, transferência,
    inventário)~~ — ADR-0189; ~~ES3 (reserva e consumo nas emergências)~~ — ADR-0191; ponto de reposição automático; incluir a capacidade Central de
    abastecimento no almoxarifado central; conceder o papel **Almoxarife** quando a autorização for ligada;
  - #14 Compras;
  - #15 Patrimônio;
  - #17 Financeiro;
  - #18 Qualidade;
  - #19 Indicadores e BI — desenhado na ADR-0142; ~~BI1 (produção assistencial e Sala de Situação)~~ — ADR-0143;
    ~~BI2 (situação operacional)~~ — ADR-0144; ~~BI3 (indicadores da rede)~~ — ADR-0146; ~~BI4 (relatórios)~~ — ADR-0167 (sem envio agendado); ~~BI5 (coordenações e gerência)~~ — ADR-0175.
- **#29 Modelo Operacional dos Equipamentos** (camada) — desenhado na ADR-0115, que aceita a ADR-0053. ~~MO1~~ —
  perfis e capacidades, ADR-0117; ~~MO2~~ — serviços da unidade, ADR-0165 (sem o leito apontar direto para o serviço). ~~MO3~~ — ponto operacional, ADR-0166 (sem GPS, sem agendar pelo ponto ou pela rota e sem
  indicadores por local). ~~MO4~~ — domínios consultam capacidades, ADR-0170. Camada completa.
- **#30 Gestão da Rede de Atenção** — desenhado na ADR-0179; ~~GR1 (oferta e cotas da PPI)~~ — ADR-0179; GR2 (redes temáticas) e GR3
  (cobertura) com aprovação.
- **#31 Intersetorialidade e Proteção Social** — desenhado na ADR-0154; ~~IS1 (catálogo e encaminhamento)~~ —
  ADR-0154. Faltam o uso pela Vigilância e pelas Emergências, o painel por instituição, a guia para imprimir
  (Documentos) e a integração com o CadÚnico e o prontuário SUAS (domínio 20).
- **#28 Comunicação e Educação em Saúde** — desenhado na ADR-0114; ~~C1 (comunicação básica)~~ — ADR-0168 (e-mail só a
  profissionais, sem consentimento para a população); faltam C2 a C6 (avaliação).
- **#27 Gestão de Emergências e Desastres** — desenhado na ADR-0113; ~~E1 (núcleo)~~ — ADR-0156 (sem a escolha
  manual das microáreas na tela); ~~E2 (contingência)~~ — ADR-0157; ~~E3 (coordenação)~~ — ADR-0158 (insumo, equipamento e veículo como necessidade até Estoque e Transporte); ~~E5 (pós-evento)~~ — ADR-0159 (sem o relatório em PDF, que depende de Documentos); ~~E6a (evento de massa e estruturas temporárias)~~ — ADR-0160 (ambulâncias como número até Transporte); ~~E6b (painel da epidemia e exposição ambiental)~~ — ADR-0161 (sem incidência por população); ~~E4a (vulneráveis, deslocados e abrigos)~~ — ADR-0193; falta a E4b (redistribuição e Regulação).
- **#26 Saúde Mental** — desenhado na ADR-0112; fatias ~~SM1 (base)~~ — ADR-0120; ~~SM2 (PTS)~~ — ADR-0121; ~~SM4 (rede)~~ — ADR-0122; ~~SM3 (risco e crise)~~ — ADR-0123 (a notificação pela V1; o SAMU pela
  regulação de urgência, ADR-0176); ~~SM5 (CAPS e RAPS)~~ — ADR-0153 (sem o CAPS de referência por território e a RAAS); ~~SM6
  (intersetorialidade e indicadores)~~ — ADR-0155. Domínio completo.
- **#25 Saúde Bucal** — desenhado na ADR-0111; fatias ~~SB1 (base)~~ — ADR-0134, ~~SB2 (odontograma)~~ — ADR-0135, ~~SB3a (catálogo e procedimento odontológico)~~ — ADR-0136, ~~SB3b (plano de tratamento)~~ — ADR-0137; ~~SB4a (encaminhamento pela Regulação)~~ — ADR-0138; ~~SB4b (prótese)~~ — ADR-0139; ~~SB5 (coletiva)~~ — ADR-0140. Domínio completo; CPO-D e indicadores ficam para a P6 e o BI.
- **#23 Vigilância em Saúde** — desenhado na ADR-0110; fatias ~~V1 (fundação)~~ — ADR-0124,
  ~~V2a (contatos, local e mapa)~~ — ADR-0130, ~~V2b (surto e medidas)~~ — ADR-0131, ~~V4a (controle de vetores)~~ —
  ADR-0162 (sem o quarteirão), ~~V4b (animais)~~ — ADR-0163 (sem a cobertura antirrábica), ~~V4c (água e risco)~~ — ADR-0164 (sem o plano de amostragem); faltam V3 (sanitária) e V5 (trabalhador e integrações).
- **#24 Imunização** — desenhado na ADR-0147; ~~I1 (fundação: catálogo, calendário, dose, caderneta e Sala de Vacina)~~
  — ADR-0147; ~~I2 (sala de vacina e cadeia de frio)~~ — ADR-0148 (sem o remanejamento entre unidades, o pedido à rede
  de frio e o termômetro automático); ~~I3 (cobertura e campanhas)~~ — ADR-0149 (sem o SINASC e sem a cobertura de
  adolescentes, adultos e gestantes); ~~I4 (ESAVI)~~ — ADR-0150 (sem o alerta por lote e a revogação da
  contraindicação); falta I5 (SI-PNI).
- **#22 Programas, Ações e Linhas de Cuidado** — desenhado na ADR-0109; programas e campanhas cadastráveis.
  Fatias: ~~P1 (catálogo)~~ — ADR-0118; ~~P2 (acompanhamento)~~ — ADR-0119 (elegíveis por idade e sexo na
  ADR-0128; os critérios clínicos ficam para depois); ~~P4 (ações e programa na Agenda)~~ — ADR-0132; ~~P3 (campanhas e consulta pública)~~ — ADR-0133; ~~P6 (metas e indicadores)~~ — ADR-0141; ~~P5 (linhas de cuidado)~~ — ADR-0152 (sem linhas recorrentes e
  indicadores de linha). Domínio completo.
- **#21 Território e Adscrição** — desenhado na ADR-0108; ~~F1 (fundação)~~ — ADR-0125; ~~F2 (mapa)~~ — ADR-0126;
  ~~F3a (imóvel, domicílio e família)~~ — ADR-0127; ~~F3b (adscrição e cobertura)~~ — ADR-0128; ~~F4
  (escopo de acesso e fim do texto das microáreas)~~ — ADR-0129. Falta ligar o escopo territorial quando a rede tiver o
  cadastro.
- **Integrações** (SUS, CNES, e-SUS, SIGTAP) e **Documentos** além da D1 ([ADR-0172](./adr/0172-documentos-anexos.md)): outros donos, guias para imprimir, aprovação e assinatura.
- **Backup do servidor,** agora também do volume `documentos-data`, antes de qualquer dado real.

## 7. Fluxo de promoção

Mergear na `developer` é rotina. **Promover para `qaa`, `homologacao` e `main` só quando o usuário pedir**
(ADR-0010). A remoção do deploy do Render (#281) foi na release 1.5.0. Antes de promover uma release, incremente o
`<version>` do `pom.xml` em `developer` (VERSIONING.md): sem isso, a tag e a release não são criadas.
