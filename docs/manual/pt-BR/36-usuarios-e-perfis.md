# Usuários e perfis

> **Versão:** 1.15.0 · [English](../en/36-usuarios-e-perfis.md)

Menu: **Administração › Usuários & Perfis**. É onde a administração cuida das contas, dos perfis de acesso (RBAC) e dos acessos
de cada pessoa por unidade.

**Quem usa:**

| Perfil | O que faz |
|--------|-----------|
| Administrador da plataforma | cadastra usuários, define a senha provisória, desbloqueia, encerra sessões e mantém os perfis |
| Gestor | concede e revoga acessos no seu escopo |

> O administrador da plataforma **não lê dado de saúde**. Quem administra o sistema e quem atende devem ter contas separadas.

**Os cartões do alto:**

- os **Usuários ativos** e os **Com acesso vigente;**
- os **Acessos vigentes;**
- os **Bloqueados / inativos.**

## 1. Cadastrar um usuário

1. Clique em novo usuário.
2. Em **Matrícula do profissional**, digite a matrícula e clique em **Buscar**. O CPF e o nome vêm do quadro de profissionais. Para
   uma conta técnica, deixe em branco e preencha o **CPF** e o **Nome**.
3. Informe a **Senha inicial** e a confirmação. São no mínimo 8 caracteres.
   - **A senha é provisória:** no primeiro acesso, a pessoa cria a própria.
   - **Entregue-a por um canal próprio, nunca junto com o CPF.**
4. Salve. **O usuário nasce sem acesso:** depois de cadastrar, conceda o perfil e a unidade (item 3).

## 2. A conta do usuário

Busque pelo nome, pelo CPF ou pela matrícula, e filtre pela unidade e pela situação: ativos, bloqueados, inativos ou sem acesso.

Ao abrir o usuário:

- **Editar usuário:** o nome e **Ativo**. O usuário desativado não entra mais no sistema; os acessos continuam registrados.
- **Definir senha provisória:** para quem esqueceu a senha ou está bloqueado.
  - A senha atual deixa de valer e as sessões abertas são encerradas.
  - O bloqueio é liberado.
  - No próximo acesso, a pessoa cria a própria senha antes de usar o sistema.
- **Desbloquear:** libera o bloqueio por tentativas antes dos 15 minutos. A conta mostra **Bloqueado por tentativas até…**.
- **Encerrar sessões:** derruba todas as sessões abertas da pessoa.
- **Ver na auditoria:** abre a trilha da pessoa.

## 3. Conceder e revogar acessos

Um **acesso** é um perfil numa unidade.

**Conceder:**

1. No usuário, use **+ Conceder acesso**.
2. Escolha o **Perfil** e o **Escopo**. O acesso vale para a unidade escolhida e para as que estão abaixo dela (por exemplo, a
   Regional cobre as suas UBS).
3. Se quiser, o **Início** e o **Fim**. Sem datas, o acesso vale até ser revogado; com fim, serve para plantão ou cobertura
   temporária.
4. Salve.

> **Sem nenhum acesso, o usuário entra no sistema, mas não vê nenhuma tela com dados.**

**Revogar:** no acesso, use **Revogar** e escreva o **Motivo** (por exemplo, *transferido para outra unidade*). O acesso não é
apagado: fica no histórico com o motivo, quem revogou e quando.

## 4. Perfis (RBAC)

A aba de perfis lista os perfis, com:

- o escopo (unidade ou rede);
- a marca **Acessa dado de saúde** ou **Sem dado de saúde;**
- quantos usuários têm o perfil.

**Criar ou editar um perfil:**

1. Informe o código (por exemplo, *AGENTE_COMUNITARIO*). **O código não muda depois de criado.**
2. Informe o nome e a **Descrição** (o que o perfil faz e onde).
3. Marque as **Permissões**. As que dão acesso a dado de saúde aparecem marcadas como **dado de saúde**.
4. Deixe **Ativo** marcado. O perfil inativo deixa de dar acesso e não pode ser concedido.

**Duplicar** cria um perfil a partir de outro.

**A matriz de permissões** mostra, por módulo do sistema, o que o perfil pode: **Visualizar**, **Registrar** e **Gerenciar**,
com a marca **Permitido**, **Parcial** ou **Negado**. As **Outras permissões** são as que ainda não têm módulo na matriz.

**Os perfis padrão** já vêm prontos, como recepção, médico, enfermeiro, técnico de enfermagem, farmacêutico, gestor, os de
regulação, laboratório, vigilância, saúde bucal, saúde mental, almoxarife, compras, financeiro e patrimônio.

## 5. Mensagens comuns

| Situação | O que quer dizer | O que fazer |
|----------|------------------|-------------|
| *Sem nenhum acesso…* | o usuário não tem perfil em nenhuma unidade | conceda o acesso |
| **Bloqueado por tentativas até…** | a pessoa errou a senha 5 vezes | espere ou use **Desbloquear** |
| Perfil **inativo** | o perfil não dá mais acesso | reative ou conceda outro perfil |
| *Nenhum usuário encontrado* | a busca ou os filtros não acharam ninguém | ajuste a busca |
