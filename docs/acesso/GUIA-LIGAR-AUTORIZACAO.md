# Guia — ligar a exigência de permissão num ambiente

Passo a passo para passar um ambiente de "quem faz login vê tudo" para "cada pessoa só faz o que o perfil
e a unidade dela permitem". As decisões por trás deste guia estão nas ADRs
[0066](../adr/0066-papeis-permissoes-e-escopo-por-unidade.md) (perfis e escopo),
[0067](../adr/0067-autorizacao-aplicada-nas-rotas-e-no-escopo.md) (exigência nas rotas) e
[0068](../adr/0068-tela-usuarios-e-perfis.md) (tela Usuários & Perfis).

## Como está hoje

São três interruptores, e cada um só funciona com o anterior ligado:

| Variável (overlay do ambiente) | O que faz | `dev` | `qaa` / `homologacao` / `prod` |
|---|---|---|---|
| `APP_SECURITY_ENABLED` | Exige login (ADR-0055) | ligado | desligado |
| `APP_SECURITY_AUTHORIZATION_ENABLED` | Exige permissão por perfil e unidade | desligado | desligado |
| `APP_SECURITY_PRONTUARIO_POR_VINCULO_ENABLED` | Exige vínculo assistencial para abrir o prontuário (ADR-0076) | desligado | desligado |

Com o segundo desligado, nada do que está neste guia é obrigatório. A tela Usuários & Perfis já funciona
e os acessos concedidos ficam guardados, mas ainda não restringem nada.

## O que muda quando liga

- Cada rota da API passa a exigir a permissão correspondente. Sem ela, a API responde **403**, com uma
  mensagem que diz qual permissão ou unidade faltou.
- **Registrar** só é possível na unidade do acesso e nas que estão abaixo dela. Um acesso na Regional
  cobre as UBS dela.
- **Listas** de atendimentos, registros clínicos, lotes, dispensações e transferências mostram só as
  unidades do acesso. O **prontuário do paciente** continua visível na rede inteira, para quem tem
  permissão de consultar prontuário, por continuidade do cuidado — até ligar o vínculo (seção abaixo).
- **Retificar** um registro clínico: só quem registrou, ou a coordenação de enfermagem da unidade.
- **Receber** uma transferência: só quem tem acesso na unidade de destino.
- **O menu** mostra só o que as permissões de cada pessoa liberam.
- **Quem não tem nenhum perfil** entra no sistema, mas não vê nenhuma tela com dados.

Por isso a ordem é sempre esta: **conceder os acessos primeiro, ligar depois**.

## Antes de começar

- O ambiente precisa estar com o login ligado (`APP_SECURITY_ENABLED=true`) e com o `Secret`
  `app-secrets` provisionado (ADR-0055).
- A versão implantada precisa ter a tela Usuários & Perfis (PR #257 em diante).
- **Os acessos são dados do banco de cada ambiente.** O que foi concedido no `dev` não vai para o
  `qaa`, a `homologacao` ou a `prod`. Este guia é repetido em cada ambiente.

## Passo 0 — Trocar a senha do administrador inicial (recomendado)

O administrador inicial nasce do `Secret` (`bootstrap-cpf` e `bootstrap-senha`), mas **só na primeira
subida**: se o usuário já existe, o bootstrap não mexe nele. Por isso, **trocar a senha no `Secret` não
troca a senha de quem já existe.** Troque pela própria tela (ADR-0069):

1. Entre com o administrador inicial.
2. Na barra superior, clique no ícone de **chave** (Trocar senha).
3. Informe a senha atual e a nova. A senha antiga deixa de valer na hora.

Se preferir ter a sua conta pessoal separada da conta de instalação: cadastre-a em **Usuários & Perfis →
Novo usuário**, conceda **Administrador da plataforma** na **Rede inteira**, entre com ela (o sistema pede
para trocar a senha provisória) e desative a conta inicial em **Editar usuário**. Ninguém consegue
desativar o próprio usuário; por isso a desativação é feita a partir da conta nova.

> O perfil Administrador da plataforma **não dá acesso a dado de saúde** (ADR-0054). Se você também
> usa as telas clínicas para testar, conceda à sua conta outros perfis além dele (ex.: Coordenador de
> enfermagem ou Médico na unidade de teste).

## Passo 1 — Planejar quem recebe o quê

Perfis que já vêm prontos:

| Perfil | Para quem | Observação |
|---|---|---|
| Administrador da plataforma | quem administra usuários, acessos e unidades | sem dado de saúde |
| Gestor | gestão de RH, setor administrativo, estoque e acessos da unidade | concede acesso só na sua unidade |
| Recepção | cadastro de paciente, agenda, acolhimento, andamento da regulação | não lê triagem, prontuário nem a justificativa da regulação |
| Médico | consulta, prescrição, procedimento, solicitação de regulação | |
| Médico regulador | fila da Central de Regulação do Acesso: autoriza com vaga, devolve, nega | escopo no município ou na regional (ADR-0087) |
| Enfermeiro | classificação de risco, evolução, procedimento, medicação | |
| Coordenador de enfermagem | tudo do enfermeiro + retificar registro de outro profissional | |
| Técnico de enfermagem | procedimento e medicação | sem classificação de risco nem evolução (COFEN 661/2021) |
| Farmacêutico | estoque, dispensação, transferências | |

Para cada pessoa, decida:
- **O perfil.** Uma pessoa pode ter mais de um, por exemplo Enfermeiro + Coordenador de enfermagem.
- **O escopo.** Pode ser uma UBS, uma Regional (que cobre as unidades de baixo) ou a rede inteira.
  Prefira o escopo mais estreito que resolve.
- **O período, se for temporário:** plantão ou cobertura de férias. Sem fim, vale até ser revogado.

**Auditoria:** nenhum perfil pronto consulta a trilha de auditoria — nem o Administrador da plataforma
(ADR-0071). Para quem fiscaliza (ouvidoria, controle interno), crie um perfil "Auditor" com a permissão
**Consultar a trilha de auditoria** e conceda-o no escopo que essa pessoa fiscaliza.

Um perfil que não existe pode ser criado na aba **Perfis & permissões → Novo**. Para partir de um
parecido, use **Duplicar**. Só quem gerencia acesso na rede inteira cria ou altera perfis.

## Passo 2 — Conceder os acessos

Em **Usuários & Perfis**:

1. **A pessoa ainda não tem login:** use **Novo usuário**. Ela nasce *sem acesso* e com **senha
   provisória**: no primeiro acesso, o sistema pede que ela crie a própria senha antes de qualquer tela.
   Entregue a senha inicial por um canal próprio, nunca junto com o CPF.
2. Abra a pessoa na tabela → **+ Conceder acesso** → perfil, escopo e, se houver, período.
3. Repita para cada pessoa. O gestor de uma unidade também pode conceder na unidade dele.

Para conferir o que já foi feito:
- O filtro **Status → Sem acesso** mostra quem ainda não recebeu nenhum perfil.
- Clicar num perfil, na lista da direita, filtra a tabela por ele e mostra a matriz do que ele permite.

## Passo 3 — Conferir antes de ligar

Com a exigência ainda desligada, para cada tipo de perfil:

- Entre como uma pessoa daquele perfil. Uma conta de teste serve.
- Abra `GET /api/v1/auth/eu` pelo Swagger. Os campos `acessos` e `permissoes` precisam mostrar o
  esperado.
- Confira que ninguém que trabalha no dia a dia aparece como **Sem acesso**.

## Passo 4 — Ligar no overlay do ambiente

Acrescente a variável no `configMapGenerator` do overlay, por exemplo em
`k8s/overlays/dev/kustomization.yaml`:

```yaml
configMapGenerator:
  - name: app-config
    literals:
      # ...
      - APP_SECURITY_ENABLED=true
      # Exige permissão por perfil e unidade (ADR-0067). Acessos concedidos antes — ver
      # docs/acesso/GUIA-LIGAR-AUTORIZACAO.md.
      - APP_SECURITY_AUTHORIZATION_ENABLED=true
```

A mudança segue o fluxo normal: branch → PR → CI → merge. O ArgoCD aplica, e o ConfigMap novo reinicia
a aplicação. Para `qaa`, `homologacao` e `prod`, vale a regra de promoção de sempre: só com a
liberação explícita do responsável.

## Passo 5 — Verificar depois do deploy

- `GET /api/v1/auth/status` precisa responder `"authorizationEnabled": true`. Essa rota é pública.
- Entre com um perfil restrito (ex.: Recepção) e confira que o menu mostra só o que ele usa.
- Tente uma ação fora do perfil (ex.: registrar triagem como Recepção). A tela precisa mostrar o erro
  com a permissão que faltou.
- Entre com a sua conta de administrador e confira que a tela Usuários & Perfis abre.

## Depois: exigir vínculo assistencial no prontuário (ADR-0076)

É um passo separado, feito **depois** que a exigência de permissão estiver funcionando.

**O que muda:**
- Para abrir o prontuário completo de um paciente, é preciso ter vínculo com ele. Há vínculo quando:
  - o paciente tem atendimento em aberto, ou nos últimos 30 dias, numa unidade do acesso da pessoa; ou
  - a pessoa é a profissional de um atendimento recente dele; ou
  - a pessoa é a profissional de um agendamento próximo dele (até 30 dias antes ou depois); ou
  - o paciente tem uma solicitação de regulação em curso, ou realizada nos últimos 30 dias, em que a
    unidade solicitante ou a executante está no acesso da pessoa (ADR-0089).
- Sem vínculo, a tela mostra **"Sem vínculo assistencial com este paciente"** e o botão
  **Acessar com justificativa**. A pessoa informa o motivo e um texto, e o acesso vale por 4 horas, só
  para ela.
- Cada acesso justificado aparece na tela **Auditoria**, filtrando a ação por **Acesso justificado**. A
  supervisão deve revisar esses acessos periodicamente.
- Quem faz auditoria clínica ou regulação precisa de um perfil com a permissão
  `PRONTUARIO.CONSULTAR_SEM_VINCULO`, que nenhum perfil padrão tem. Crie um perfil próprio na aba
  Perfis & permissões.

**Como ligar:** acrescente `APP_SECURITY_PRONTUARIO_POR_VINCULO_ENABLED=true` no mesmo overlay do
Passo 4 e siga o mesmo fluxo de PR. Para desligar, remova a linha. Os acessos justificados continuam
guardados.

## Recuperação de senha por e-mail (ADR-0081)

Sem ela, quem esquece a senha depende da administração (senha provisória). Para oferecer o
**Esqueci minha senha** no login, o ambiente precisa de um provedor SMTP.

**Variáveis no overlay do ambiente:**
- `SPRING_MAIL_HOST` e `SPRING_MAIL_PORT`;
- `SPRING_MAIL_USERNAME` e `SPRING_MAIL_PASSWORD`, num Secret;
- `SPRING_MAIL_PROPERTIES_MAIL_SMTP_STARTTLS_ENABLE=true`, se o provedor exigir;
- `APP_SECURITY_RECUPERACAO_SENHA_URL_FRONTEND`, o endereço do front, usado no link (ex.:
  `http://frontend-dev.mais-saude.local`);
- `APP_EMAIL_REMETENTE`, o endereço "de".

**Para conferir:** `GET /api/v1/auth/status` deve trazer `"recuperacaoDeSenha": true`.

**Quem consegue recuperar:** só quem tem profissional cadastrado com e-mail, ligado pelo mesmo CPF. Os
outros continuam pela senha provisória. Os pedidos aparecem na tela Auditoria com a ação
**Recuperação de senha**.

## Como desligar (voltar atrás)

Remova a linha `APP_SECURITY_AUTHORIZATION_ENABLED=true` do overlay, ou troque o valor para `false`, e
siga o mesmo fluxo de PR. Nada se perde: os acessos concedidos continuam no banco e voltam a valer
quando a exigência for religada.

## Problemas comuns

| Sintoma | Causa provável | O que fazer |
|---|---|---|
| Tela ou botão responde "Seu acesso não inclui …" | o perfil da pessoa não tem aquela permissão | conceder o perfil certo, ou ajustar o perfil na aba Perfis & permissões |
| "… na unidade X" | a pessoa tem a permissão, mas em outra unidade | conceder na unidade X, ou numa unidade acima dela |
| Pessoa entra e não vê nada | status **Sem acesso** | conceder um perfil |
| Gestor não consegue conceder Administrador da plataforma | por desenho: perfil com administração do sistema só é concedido por quem tem acesso na rede inteira | um administrador da plataforma concede |
| "Você não pode desativar o seu próprio usuário" | proteção contra trancar a administração | desativar a partir de outra conta de administrador |
| "Conta temporariamente bloqueada" no login | 5 senhas erradas seguidas (ADR-0055) | abrir a pessoa → **Desbloquear**, ou esperar o prazo |
| Pessoa esqueceu a senha | — | com SMTP configurado, ela mesma usa **Esqueci minha senha** no login (ADR-0081); sem SMTP, ou sem e-mail no cadastro de profissional, abrir a pessoa → **Definir senha provisória** (ADR-0069) |
| "Troque a senha provisória antes de continuar" | a pessoa entrou com senha provisória | ela conclui a troca na tela que o sistema abre sozinho |
| "Sem vínculo assistencial com este paciente" | o vínculo está ligado e a pessoa não tem atendimento nem agendamento com o paciente | registrar o atendimento ou o agendamento, ou usar **Acessar com justificativa** |
| Computador esquecido logado, ou suspeita de uso indevido da conta | a sessão continua aberta | abrir a pessoa → **Encerrar sessões**; ela entra de novo com a mesma senha (ADR-0078) |
| Menu mostra tudo mesmo com a exigência ligada | a tela foi aberta antes do deploy | sair e entrar de novo, ou recarregar a página |

## Pendências conhecidas

- Recuperação de senha pelo gov.br (a por e-mail existe, ver seção acima, ADR-0081). As sessões ativas
  já caem na hora ao desativar, redefinir ou trocar a senha, e pelo botão **Encerrar sessões** (ADR-0078).
- MFA, sessões ativas e política de senha aparecem como "Em breve" na tela.
