# Pendências e estado dos ambientes

Documento vivo, com **o que falta** no projeto depois da release **v1.4.0** (2026-10-01). Cada item aponta
para a ADR ou o guia onde está o detalhe. Atualize este arquivo ao fechar ou abrir uma pendência.

## 1. Estado dos ambientes

O código é o mesmo nos quatro ambientes (v1.4.0). O que muda é o que cada um liga no próprio overlay
(`k8s/overlays/<ambiente>/kustomization.yaml`):

| Recurso | `dev` | `qaa` | `homologacao` | `prod` |
|---|---|---|---|---|
| Login (ADR-0055) | ✅ ligado | ❌ | ❌ | ❌ |
| Exigência de permissão por perfil e unidade (ADR-0067) | ❌ | ❌ | ❌ | ❌ |
| Prontuário por vínculo assistencial (ADR-0076) | ❌ | ❌ | ❌ | ❌ |
| Recuperação de senha por e-mail (ADR-0081) | ❌ sem SMTP | ❌ | ❌ | ❌ |

O que isso significa:
- **Sem login** (`qaa`, `homologacao`, `prod`), qualquer pessoa entra e faz tudo. Usuários & Perfis,
  troca de senha, encerramento de sessões e botões por permissão existem, mas não têm efeito.
- **Com login e sem exigência de permissão** (`dev`), todo usuário logado faz tudo. Os perfis concedidos
  ficam guardados e só restringem quando a exigência for ligada.
- **A auditoria grava em todos os ambientes.** Sem login, os eventos ficam sem a pessoa (CPF vazio).
- **Cada item depende do anterior:** o vínculo só age com a permissão ligada, e a permissão só com o
  login ligado.

## 2. Para ligar, nesta ordem

O passo a passo está em [`acesso/GUIA-LIGAR-AUTORIZACAO.md`](./acesso/GUIA-LIGAR-AUTORIZACAO.md).

1. **Login em `qaa`, `homologacao` e `prod`.** Provisionar o Secret `app-secrets` (chave do JWT, CPF e senha
   do administrador inicial) em cada namespace e acrescentar `APP_SECURITY_ENABLED=true` no overlay.
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
- [ ] Provisionar `app-secrets` nos namespaces de `qaa`, `homologacao` e `prod` (item 2.1).
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
- Vagas por unidade e procedimento (cotas e PPI): hoje o regulador informa a vaga na autorização.
- Fora do escopo da ADR-0087: regulação de urgência e SAMU, internação (depende de Leitos, #12),
  integração com SISREG e SIGTAP, e cotas por unidade (PPI).
- Conceder o papel **Médico regulador** a alguém no escopo do município ou da regional, quando a
  autorização for ligada.

### Equipamentos de Saúde
- ~~Tela pelo protótipo~~ — feita pela ADR-0102 (lista e painel da unidade). "Em breve" na tela: mapa e
  coordenadas, sincronização com o CNES, exportação, consultórios, vagas semanais da unidade, microáreas,
  acessibilidade e equipes de Saúde da Família.
- Fora do escopo da ADR-0101: sincronização com o CNES; coordenadas e mapa; microáreas e famílias; equipes de
  Saúde da Família; inspeção sanitária; capacidade de consultórios; exportação; horário de atendimento
  estruturado.

### Leitos e internação
- ~~Internações no prontuário e vínculo de quem cuida do internado~~ — feitos pela ADR-0100. A tela (ADR-0099)
  não tem protótipo próprio.
- Fora do escopo da ADR-0098: AIH, SIH e faturamento; central de regulação de leitos entre unidades;
  reserva de leito para cirurgia eletiva; censo diário formal; prescrição e dieta hospitalar; sincronização
  com os leitos do CNES; restrição de leito por idade (pediátrico e neonatal).

### Laboratório
- Depois da ADR-0095: laudo PDF assinado digitalmente (ICP-Brasil), envio do resultado ao paciente, gráfico
  da evolução de um exame no tempo e aviso ao solicitante quando o resultado é liberado.
- A tela (ADR-0094) não tem protótipo próprio: segue o padrão de Farmácia e Regulação.
- Fora do escopo da ADR-0093: LACEN e vigilância, integração com equipamentos (LIS, HL7, ASTM), controle
  de qualidade, insumos de coleta (Estoque, #13), exames de imagem, laudo PDF assinado, SIGTAP e BPA,
  e valores de referência por sexo e idade.

### Agenda (diferenças com o protótipo `Agenda.html`, ADR-0092)
- Visão Mês, exportação e filtro de especialidade ("Em breve").
- Atividades coletivas (grupo, vacinação aberta, coleta com N pacientes, reunião, capacitação, visita do
  ACS), programas nas marcações (HiperDia, pré-natal) e sala: dependem de domínios que não existem.
- Vaga liberada por falta oferecida à fila da Regulação ou a encaixe.
- ~~Horário de funcionamento da unidade fechando a agenda~~ — feito pela ADR-0101 (horário estruturado em
  turnos e unidade em obra ou inoperante sem vagas).
- Meta de ocupação configurável por unidade (está fixa em 85%); indicadores de encaixe e falta "de hoje".
- Mini-mês com marcações do mês inteiro; almoço desenhado como bloco; contador no menu; atualização ao
  vivo.

### Telas
- ~~**Perfil do profissional:** as 13 abas no padrão novo~~ — feito pelas ADRs 0084 a 0086.
- ~~Responsáveis por setor~~ e ~~edição de treinamento, ciclo e tipo de benefício~~ — feitos pela ADR-0083.
- Exportar e importar profissionais; **Equipes** e **Escalas**, que são domínios novos (ADR-0072).
- As telas refeitas não foram conferidas visualmente por quem implementou (ADRs 0074 a 0076).

### Testes
- **Cobertura Robot de interface das demais telas**, deixada para o final (ADR-0077): Necessidades de
  pessoal, RH (Cargos & salários, Benefícios, Desenvolvimento, Recrutamento, Folha; o perfil já tem cobertura pelas ADRs 0084 a 0086),
  Profissionais, Usuários & Perfis (além do encerramento de sessões) e Auditoria.

## 6. Roadmap de domínios

Detalhe em [`MAPA-DE-DOMINIOS.md`](./MAPA-DE-DOMINIOS.md), seção 4. Todos "só quando houver requisito
real":
- **Operação Assistencial:**
  - #10 Laboratório e diagnóstico;
  - #11 Regulação (também completa o vínculo do prontuário);
  - #12 Leitos e internação;
  - #16 Transporte sanitário;
  - ~~agenda do profissional~~ — feita pelas ADRs 0091 e 0092. Faltam visão Mês, exportação, atividades
    coletivas, programas, especialidade e equipe;
  - na Enfermagem, `Cuidado`, `Escala` e prescrição com aprazamento.
- **Gestão e Inteligência:**
  - #13 Estoque;
  - #14 Compras;
  - #15 Patrimônio;
  - #17 Financeiro;
  - #18 Qualidade;
  - #19 Indicadores e BI.
- **Integrações** (SUS, CNES, e-SUS, SIGTAP) e **Documentos**.

## 7. Fluxo de promoção

Mergear na `developer` é rotina. **Promover para `qaa`, `homologacao` e `main` só quando o usuário pedir**
(ADR-0010). A próxima promoção leva a remoção do deploy do Render (#281), que já está na `developer`.
