# DER (Diagrama Entidade-Relacionamento) — Modelo ATUAL implementado

**Data:** 2026-09-06
**Versão:** 1.0 (reflete o código em `src/main/java/com/edufelizardo/maissaudepublica/models/`)

Este documento descreve o schema **realmente implementado** hoje, em contraste com o modelo de domínio clínico futuro/proposto em [`DER.md`](./DER.md). Ver também [`0002-modelar-hierarquia-como-entidade-unica-autorreferenciada.md`](./0002-modelar-hierarquia-como-entidade-unica-autorreferenciada.md) para a decisão arquitetural por trás deste modelo.

---

## Diagrama

```
┌────────────────────────────────────────────────────┐
│                 TB_UNIDADE_DE_SAUDE                 │
├────────────────────────────────────────────────────┤
│ uuid (PK)                    UUID                   │
│ nome                         VARCHAR   UNIQUE, NOT NULL │
│ tipo                         INT       (ver TipoUnidadeDeSaude — NÃO é FK/enum no schema) │
│ unidade_superior_id (FK)     UUID      NULL — autorreferência (mesma tabela) │
│ regiao                       VARCHAR   NULL — só preenchido no nível "Dois" │
│ municipio                    VARCHAR   NULL — só preenchido no nível "Um"   │
│ estados                      VARCHAR   NULL — só preenchido no nível "Um"   │
│ email                        VARCHAR                │
│ ativo                        BOOLEAN                │
│ -- colunas embutidas de Endereco (@Embedded) --     │
│ cep, logradouro, numeroLogradouro, complemento,     │
│ bairro, cidade, estado, ddd     (todas VARCHAR)     │
└───────────────┬──────────────────────┬──────────────┘
                │ 1                    │ 1
                │ (unidade_superior_id)│
                ▼ N (auto-relacionamento)
        [outra linha de TB_UNIDADE_DE_SAUDE]
                │
                │ 1 ── N (via @ElementCollection, FK implícita para a uuid da unidade)
   ┌────────────┼──────────────────────────────┐
   ▼                       ▼                    ▼
┌───────────────────┐ ┌──────────────────────────┐ ┌──────────────────────────┐
│ TB_TELEFONES_SAUDE │ │ TB_HORARIO_FUNIONAMENTO  │ │ TB_HORARIO_ATENDIMENTO   │
├────────────────────┤ ├──────────────────────────┤ ├──────────────────────────┤
│ (FK unidade)        │ │ (FK unidade)             │ │ (FK unidade)             │
│ saudeTelefones (str)│ │ DAY_OF_WEEK (map key)    │ │ DAY_OF_WEEK (map key)    │
│                      │ │ HORARIO_FUNCIONAMENTO   │ │ HORARIO_ATENDIMENTO      │
└────────────────────┘ └──────────────────────────┘ └──────────────────────────┘
```

> `TB_HORARIO_FUNIONAMENTO` (sem o "C") é o nome real da tabela no código-fonte (`@CollectionTable(name = "TB_HORARIO_FUNIONAMENTO")`) — mantido aqui de propósito, não é erro de digitação deste documento.

## Descrição das entidades

### 1. `UnidadeDeSaude` (`@Entity`, tabela `TB_UNIDADE_DE_SAUDE`)

| Campo | Tipo Java | Coluna/Anotação | Observação |
|---|---|---|---|
| `uuid` | `UUID` | `@Id @GeneratedValue(AUTO)` | PK |
| `nome` | `String` | `@NotBlank @Column(unique = true)` | Único em toda a tabela, independente do nível |
| `tipo` | `int` | coluna simples | Discriminador de nível — comparado contra os ordinais do enum `TipoUnidadeDeSaude`; **não é `@Enumerated`**, ver pendência na ADR 0001 |
| `unidadeSuperior` | `UnidadeDeSaude` | `@ManyToOne(LAZY)` `@JoinColumn(name="unidade_superior_id", referencedColumnName="uuid")` | Autorreferência — implementa a hierarquia de 4 níveis (ver ADR 0002); sem `@OneToMany` inverso (não há navegação pai→filhos) |
| `regiao` | `String` | coluna simples, nullable | Só setado pelo construtor do nível "Dois" |
| `municipio` | `String` | coluna simples, nullable | Só setado pelo construtor do nível "Um" |
| `estados` | `String` | coluna simples, nullable | Só setado pelo construtor do nível "Um" |
| `endereco` | `Endereco` | `@Embedded` | Ver detalhamento abaixo — colunas ficam na própria `TB_UNIDADE_DE_SAUDE` |
| `saudeTelefones` | `Set<String>` | `@ElementCollection` `@CollectionTable(name="TB_TELEFONES_SAUDE")` | Tabela filha 1:N |
| `email` | `String` | coluna simples | |
| `horarioFuncionamento` | `Map<DayOfWeek, String>` | `@ElementCollection` `@CollectionTable(name="TB_HORARIO_FUNIONAMENTO")`, chave `DAY_OF_WEEK`, valor `HORARIO_FUNCIONAMENTO` | Tabela filha, mapa dia-da-semana → texto livre |
| `horarioAtendimento` | `Map<DayOfWeek, String>` | `@ElementCollection` `@CollectionTable(name="TB_HORARIO_ATENDIMENTO")`, chave `DAY_OF_WEEK`, valor `HORARIO_ATENDIMENTO` | Idem, para horário de atendimento ao público |
| `ativo` | `boolean` | coluna simples | |

**Relacionamentos**: autorreferenciado (`unidadeSuperior` → `UnidadeDeSaude`); 3 tabelas-filhas via `@ElementCollection` (telefones, horário de funcionamento, horário de atendimento).

### 2. `Endereco` (`@Embeddable`, sem tabela própria)

| Campo | Tipo | Observação |
|---|---|---|
| `cep`, `logradouro`, `numeroLogradouro`, `complemento`, `bairro`, `cidade`, `estado`, `ddd` | `String` | Todos embutidos diretamente nas colunas de `TB_UNIDADE_DE_SAUDE` — não é uma tabela nem entidade separada |

### 3. `TipoUnidadeDeSaude` (enum, `models/enuns/`)

Valores e ordinal (`int tipo` associado): `ADMINISTRACAO1(0)`, `ADMINISTRACAO2(1)`, `ADMINISTRACAO3(2)`, `ADMINISTRACAO4(3)`, `UBS(4)`, `HOSPITAL(5)`.

Mapeamento para os 4 níveis hierárquicos da API (Zero/Um/Dois/Três, ver ADR 0001/0002): Zero=`ADMINISTRACAO1`, Um=`ADMINISTRACAO2`, Dois=`ADMINISTRACAO3`, Três=`ADMINISTRACAO4`. Os valores `UBS`(4) e `HOSPITAL`(5) existem no enum mas **não são usados por nenhum dos 4 controllers de hierarquia atuais** — possível código morto ou funcionalidade planejada e nunca conectada; não investigado além do escopo desta documentação.

**Importante**: este enum não é o tipo da coluna `tipo` na entidade (que é `int` solto) — é usado apenas como uma tabela de lookup em memória no código Java.

## Lacunas de integridade conhecidas

- Nada no schema ou na camada de serviço garante que `unidadeSuperior` aponte para o nível imediatamente superior correto (ex.: uma unidade "Três" só deveria poder referenciar uma "Dois" como superior) — ver ADR 0002.
- `tipo` aceita qualquer `int`, incluindo valores fora do intervalo 0–3 usado pelos 4 controllers de hierarquia (ex.: 4/`UBS`, 5/`HOSPITAL`), sem validação de que o valor corresponda a um dos 4 níveis esperados nesse contexto.
