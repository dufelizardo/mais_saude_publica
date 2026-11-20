# Setores e modelo administrativo

> **Versão:** 1.15.0 · [English](../en/24-setores-e-modelo-administrativo.md)

Menu: **Administrativo › Setores**, **Administrativo › Modelo administrativo** e **Administrativo › Necessidades de pessoal**.

- **Setores:** os setores de cada unidade, com o responsável, e as responsabilidades administrativas de cada profissional.
- **Modelo administrativo:** o que cada tipo de unidade faz administrativamente: as capacidades, os processos e os perfis.
- **Necessidades de pessoal:** o que cada unidade precisa de gente, encaminhado ao RH para recrutamento.

**Quem usa:** a gestão administrativa (gestor). Os setores são consultados por quem está logado. Quem lê o administrativo consulta
o modelo.

## 1. Setores

**Os cartões do alto:**

- os **Setores ativos** e as **Unidades com setor;**
- os setores **Por tipo:** administrativo, assistencial, apoio ou técnico;
- os **Sem responsável:** os setores ativos sem responsável.

**Cadastrar um setor:**

1. Escolha a **Unidade de saúde**.
2. Informe:
   - o **Nome** (por exemplo, *Farmácia*);
   - o **Código** (por exemplo, *FARM*);
   - o tipo;
   - o **Responsável (matrícula)**, se houver.
3. Deixe **Ativo** marcado. O setor inativo deixa de aparecer para escolha nos atendimentos e nas necessidades.
4. Salve.

O setor é usado no atendimento, na lotação do profissional, nos leitos (o setor assistencial) e nos centros de custo.

## 2. Responsabilidades administrativas

São funções como **fiscal de contrato** ou **membro de comissão**, separadas da lotação. Um profissional pode ter várias ao mesmo
tempo.

1. Use **+ Atribuir responsabilidade** e escolha o **Profissional** (pelo nome ou pela matrícula).
2. Informe:
   - a **Responsabilidade** (por exemplo, *Fiscal de contrato*);
   - o **Setor;**
   - a **Descrição** (por exemplo, *contrato de limpeza nº 12/2026*);
   - o **Início.**
3. Salve. Para terminar, use **Encerrar** com a data. A responsabilidade continua no histórico do profissional.

Na lista de setores, **Responsáveis** mostra as responsabilidades do setor. **Trocar profissional** consulta outro profissional.

## 3. Modelo administrativo

**Os cartões:**

- as **Capacidades ativas** e os **Processos ativos;**
- os **Perfis ativos**, que são os modelos de estrutura administrativa;
- os **Tipos de unidade com perfil** (UBS, UPA, hospital, CAPS…).

**As seções:**

- **Capacidades administrativas:** o que uma unidade pode ter de estrutura administrativa (por exemplo, compras, estoque,
  faturamento). Cada uma tem o código, o nome e a descrição.
- **Processos administrativos:** o detalhamento opcional de cada capacidade. A capacidade vale mesmo sem eles.
- **Perfis administrativos:** os modelos de estrutura, atribuídos a tipos de unidade.
- **Perfil por tipo de unidade:** cada tipo de unidade de saúde usa um perfil. Os níveis de gestão (federal a regional) não
  entram. Use **Definir** ou **Trocar**.
- **Tipos de documento:** o que pode ser anexado ao paciente (módulo 01). O tipo inativo não entra em documento novo.

O item inativo continua no catálogo, mas não é oferecido para escolha.

## 4. Necessidades de pessoal

**Os cartões:**

- as **Necessidades registradas** e as **Posições pedidas;**
- as **Sem vaga vinculada;**
- as **Com vaga vinculada**, em recrutamento pelo RH.

**Registrar uma necessidade:**

1. **Onde:** a **Unidade de saúde** e, se quiser, o **Setor** (só os da unidade escolhida).
2. **Quem:**
   - o **Cargo**, a **Quantidade** e a **Jornada semanal (h);**
   - as **Competências necessárias.**
3. Escreva a **Justificativa** e salve.

**Vincular vaga:** quando o RH abre a vaga em **Recrutamento** (módulo 22), ligue a necessidade a ela. A necessidade passa a
**Vinculada**.

Busque pelo cargo, pela unidade, pelo setor ou pela competência, e filtre pela unidade ou por **Sem vaga**.

## 5. Mensagens comuns

| Situação | O que quer dizer | O que fazer |
|----------|------------------|-------------|
| **Sem responsável** | o setor ativo não tem responsável | informe a matrícula do responsável |
| *Nenhuma responsabilidade registrada* | o profissional ou o setor não tem responsabilidade | atribua, se for o caso |
| **Sem perfil** no tipo de unidade | o tipo de unidade não tem perfil administrativo | defina o perfil |
| **Sem vaga** na necessidade | o RH ainda não abriu a vaga | peça ao RH e vincule depois |
