# Users and profiles

> **Version:** 1.15.0 · [Português](../pt-BR/36-usuarios-e-perfis.md)

Menu: **Administração › Usuários & Perfis** (Administration › Users and profiles). This is where the administration looks after
accounts, access profiles (RBAC) and each person's access per unit.

**Who uses it:**

| Profile | What they do |
|---------|--------------|
| Platform administrator | registers users, sets temporary passwords, unlocks, ends sessions and maintains profiles |
| Manager | grants and revokes access within their scope |

> The platform administrator **does not read health data**. System administration and care work should use separate accounts.

**The cards at the top:**

- **Usuários ativos** (active users) and **Com acesso vigente** (with current access);
- **Acessos vigentes** (current access grants);
- **Bloqueados / inativos** (locked / inactive).

## 1. Registering a user

1. Click to create a user.
2. Under **Matrícula do profissional** (professional's staff number), type the number and click **Buscar** (search). CPF and name
   come from the staff list. For a technical account, leave it blank and fill in the **CPF** and **Nome**.
3. Enter the **Senha inicial** (initial password) and confirm it. At least 8 characters.
   - **The password is temporary:** on first access, the person creates their own.
   - **Deliver it through a separate channel, never together with the CPF.**
4. Save. **The user is created without access:** after registering, grant the profile and the unit (item 3).

## 2. The user's account

Search by name, CPF or staff number, and filter by unit and status: active, locked, inactive or without access.

When you open the user:

- **Editar usuário** (edit user): the name and **Ativo** (active). A deactivated user can no longer sign in; access grants remain
  recorded.
- **Definir senha provisória** (set temporary password): for someone who forgot the password or is locked out.
  - The current password stops working and open sessions are ended.
  - The lockout is lifted.
  - On next access, the person creates their own password before using the system.
- **Desbloquear** (unlock): lifts the lockout from failed attempts before the 15 minutes. The account shows **Bloqueado por
  tentativas até…** (locked by attempts until…).
- **Encerrar sessões** (end sessions): ends all the person's open sessions.
- **Ver na auditoria** (view in audit): opens the person's audit trail.

## 3. Granting and revoking access

An **access grant** is a profile at a unit.

**Granting:**

1. On the user, use **+ Conceder acesso** (grant access).
2. Choose the **Perfil** (profile) and the **Escopo** (scope). Access applies to the chosen unit and those below it (for example, a
   Region covers its primary care units).
3. Optionally, the **Início** (start) and **Fim** (end). Without dates, access lasts until revoked; with an end, it suits on-call
   duty or temporary cover.
4. Save.

> **Without any access, the user can sign in but sees no screen with data.**

**Revoking:** on the grant, use **Revogar** (revoke) and write the **Motivo** (reason, for example *transferido para outra
unidade* — transferred to another unit). The grant is not deleted: it stays in the history with the reason, who revoked it and
when.

## 4. Profiles (RBAC)

The profiles tab lists profiles, with:

- the scope (unit or network);
- the mark **Acessa dado de saúde** (accesses health data) or **Sem dado de saúde** (no health data);
- how many users have the profile.

**Creating or editing a profile:**

1. Enter the code (for example, *AGENTE_COMUNITARIO*). **The code cannot change after creation.**
2. Enter the name and the **Descrição** (what the profile does and where).
3. Tick the **Permissões** (permissions). Those giving access to health data are marked **dado de saúde**.
4. Leave **Ativo** ticked. An inactive profile no longer grants access and cannot be granted.

**Duplicar** (duplicate) creates a profile from another.

**The permission matrix** shows, per system module, what the profile can do: **Visualizar** (view), **Registrar** (record) and
**Gerenciar** (manage), marked **Permitido** (allowed), **Parcial** (partial) or **Negado** (denied). **Outras permissões** (other
permissions) are those without a module in the matrix yet.

**Default profiles** come ready, such as reception, physician, nurse, nursing technician, pharmacist, manager, the regulation,
laboratory, surveillance, oral health and mental health profiles, storekeeper, procurement, finance and assets.

## 5. Common messages

| Situation | What it means | What to do |
|-----------|---------------|------------|
| *Sem nenhum acesso…* — without any access | the user has no profile at any unit | grant access |
| **Bloqueado por tentativas até…** | the person got the password wrong 5 times | wait or use **Desbloquear** |
| Profile **inativo** (inactive) | the profile no longer grants access | reactivate it or grant another profile |
| *Nenhum usuário encontrado* — no user found | search or filters found nobody | adjust the search |
