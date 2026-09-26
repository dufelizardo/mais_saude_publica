# DER (Diagrama Entidade-Relacionamento) — Modelo ATUAL implementado

**Data:** 2026-09-06 (criado) — **corrigido em 2026-09-26**
**Versão:** 1.1 (reflete o código em `src/main/java/com/edufelizardo/maissaudepublica/models/`)

> **Correção 2026-09-26**: a versão 1.0 deste documento descrevia `tipo` como `int` solto (não
> `@Enumerated`) e `TipoUnidadeDeSaude` com apenas 6 valores (`ADMINISTRACAO1..4`, `UBS`,
> `HOSPITAL`). Isso ficou desatualizado a partir da ADR-0013 (5º nível hierárquico e supervisão
> regional) e da ADR-0031 (extensão do enum para os tipos de Unidade de Saúde do Administrativo).
> Corrigido para refletir o código real, como parte do levantamento de
> [`MAPA-DE-EQUIPAMENTOS-DE-SAUDE.md`](../MAPA-DE-EQUIPAMENTOS-DE-SAUDE.md) (ver
> [ADR-0053](./0053-criterio-de-governanca-para-equipamentos-de-saude.md)).

Este documento descreve o schema **realmente implementado** hoje, em contraste com o modelo de domínio clínico futuro/proposto em [`DER.md`](./DER.md). Ver também [`0002-modelar-hierarquia-como-entidade-unica-autorreferenciada.md`](./0002-modelar-hierarquia-como-entidade-unica-autorreferenciada.md) para a decisão arquitetural por trás deste modelo.

---

## Diagrama

```
┌────────────────────────────────────────────────────┐
│                 TB_UNIDADE_DE_SAUDE                 │
├────────────────────────────────────────────────────┤
│ uuid (PK)                    UUID                   │
│ nome                         VARCHAR   UNIQUE, NOT NULL │
│ tipo                         VARCHAR   @Enumerated(STRING) — ver TipoUnidadeDeSaude │
│ unidade_superior_id (FK)     UUID      NULL — autorreferência (mesma tabela), hierarquia de 5 níveis │
│ supervisao_regional_id (FK)  UUID      NULL — vínculo lateral de supervisão técnica (só UBS/HOSPITAL/demais tipos de Unidade de Saúde), sempre para uma unidade REGIONAL │
│ regiao                       VARCHAR   NULL — só preenchido no nível "Regional"  │
│ municipio                    VARCHAR   NULL — só preenchido no nível "Municipal" │
│ estado_administracao         VARCHAR   NULL — só preenchido no nível "Estadual"  │
│ responsavel_cpf              VARCHAR   NULL — CPF do responsável, aceito antes do Profissional existir (ADR-0014) │
│ responsavel_id (FK)          UUID      NULL — vínculo com Profissional, quando reconciliado │
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
| `tipo` | `TipoUnidadeDeSaude` | `@Enumerated(EnumType.STRING)` | Discriminador de nível/tipo — ver seção 3 abaixo |
| `unidadeSuperior` | `UnidadeDeSaude` | `@ManyToOne(LAZY)` `@JoinColumn(name="unidade_superior_id", referencedColumnName="uuid")` | Autorreferência — implementa a hierarquia de 5 níveis (ADR-0002, estendida pela ADR-0013); sem `@OneToMany` inverso (não há navegação pai→filhos) |
| `supervisaoRegional` | `UnidadeDeSaude` | `@ManyToOne(LAZY)` `@JoinColumn(name="supervisao_regional_id", referencedColumnName="uuid", nullable=true)` | Vínculo lateral, **não hierárquico**, de supervisão técnica — só preenchido para o 5º nível (Unidade de Saúde), sempre apontando para uma unidade `REGIONAL`; não substitui `unidadeSuperior` (ADR-0009/ADR-0013) |
| `regiao` | `String` | coluna simples, nullable | Só setado pelo construtor do nível `REGIONAL` |
| `municipio` | `String` | coluna simples, nullable | Só setado pelo construtor do nível `MUNICIPAL` |
| `estado` | `String` | `@Column(name="estado_administracao")`, nullable | Só setado pelo construtor do nível `ESTADUAL` |
| `endereco` | `Endereco` | `@Embedded` | Ver detalhamento abaixo — colunas ficam na própria `TB_UNIDADE_DE_SAUDE` |
| `saudeTelefones` | `Set<String>` | `@ElementCollection` `@CollectionTable(name="TB_TELEFONES_SAUDE")` | Tabela filha 1:N |
| `email` | `String` | coluna simples | |
| `horarioFuncionamento` | `Map<DayOfWeek, String>` | `@ElementCollection` `@CollectionTable(name="TB_HORARIO_FUNIONAMENTO")`, chave `DAY_OF_WEEK`, valor `HORARIO_FUNCIONAMENTO` | Tabela filha, mapa dia-da-semana → texto livre |
| `horarioAtendimento` | `Map<DayOfWeek, String>` | `@ElementCollection` `@CollectionTable(name="TB_HORARIO_ATENDIMENTO")`, chave `DAY_OF_WEEK`, valor `HORARIO_ATENDIMENTO` | Idem, para horário de atendimento ao público |
| `ativo` | `boolean` | coluna simples | |
| `responsavelCpf` | `String` | coluna simples, nullable | CPF do responsável pela unidade, aceito mesmo antes do `Profissional` correspondente existir — vínculo fraco resolvido por reconciliação (ADR-0014) |
| `responsavel` | `Profissional` | `@ManyToOne(LAZY)` `@JoinColumn(name="responsavel_id", referencedColumnName="uuid", nullable=true)` | Preenchido quando a reconciliação por CPF encontra o `Profissional` |

**Relacionamentos**: autorreferenciado (`unidadeSuperior` → `UnidadeDeSaude`, hierarquia de 5
níveis); lateral não-hierárquico (`supervisaoRegional` → `UnidadeDeSaude`, ADR-0013); `responsavel`
→ `Profissional` (RH, ADR-0014); 3 tabelas-filhas via `@ElementCollection` (telefones, horário de
funcionamento, horário de atendimento).

### 2. `Endereco` (`@Embeddable`, sem tabela própria)

| Campo | Tipo | Observação |
|---|---|---|
| `cep`, `logradouro`, `numeroLogradouro`, `complemento`, `bairro`, `cidade`, `estado`, `ddd` | `String` | Todos embutidos diretamente nas colunas de `TB_UNIDADE_DE_SAUDE` — não é uma tabela nem entidade separada |

### 3. `TipoUnidadeDeSaude` (enum, `models/enuns/`)

12 valores, `@Enumerated(EnumType.STRING)` (persistido pelo nome, não pelo ordinal):

```java
public enum TipoUnidadeDeSaude {
    FEDERAL, ESTADUAL, MUNICIPAL, REGIONAL, UBS,
    HOSPITAL, UPA, LABORATORIO, CAPS, CENTRO_ESPECIALIDADES, CENTRO_REABILITACAO, POLICLINICA
}
```

Os 4 primeiros (`FEDERAL, ESTADUAL, MUNICIPAL, REGIONAL`) são os 4 níveis hierárquicos originais
das esferas de gestão do SUS (renomeados de Zero/Um/Dois/Três pela ADR-0009). `UBS` foi adicionado
junto com o 5º nível — "Unidade de Saúde" — pela **ADR-0013**. Os 6 seguintes (`UPA, LABORATORIO,
CAPS, CENTRO_ESPECIALIDADES, CENTRO_REABILITACAO, POLICLINICA`) foram adicionados pela **ADR-0031**,
todos sob o mesmo 5º nível que `UBS`/`HOSPITAL`.

Esses 8 valores do 5º nível (`UBS, HOSPITAL, UPA, LABORATORIO, CAPS, CENTRO_ESPECIALIDADES,
CENTRO_REABILITACAO, POLICLINICA`) são exatamente a lista retornada por
`UnidadeSaudeService.getTiposAceitos()` — o mecanismo (criado pela ADR-0013, generalizado pela
ADR-0031) que permite um único controller/service aceitar todos os tipos de "Unidade de Saúde" sem
precisar de um controller por tipo. Ver [ADR-0053](./0053-criterio-de-governanca-para-equipamentos-de-saude.md)
para o critério de quando um novo tipo de equipamento deve (ou não) virar um 9º valor aqui.

## Lacunas de integridade conhecidas

- Nada no schema ou na camada de serviço garante que `unidadeSuperior` aponte para o nível
  imediatamente superior correto (ex.: uma unidade `MUNICIPAL` só deveria poder referenciar uma
  `ESTADUAL` como superior) — ver ADR-0002.
- Nenhuma validação garante que os 8 valores do 5º nível sejam usados exclusivamente por unidades
  cujo `unidadeSuperior` é `MUNICIPAL` — a regra é aplicada pelos controllers (`getTiposAceitos()`),
  não pelo schema.
