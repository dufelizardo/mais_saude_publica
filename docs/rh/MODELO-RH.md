# Modelo completo do domínio de RH

**Data:** 2026-09-19
**Status:** Desenho completo do domínio — ainda não implementado (ver [ESCOPO-RH.md](./ESCOPO-RH.md)
para a ordem de fases e o que já é lacuna confirmada vs. ideia). Este documento é o detalhamento
técnico: entidades, campos, relacionamentos e como cada peça se conecta às outras. Serve pra ter
"todos os pontos" na mão antes de escolher por onde começar a implementar — não é uma proposta de
implementar tudo de uma vez.

**Aviso que continua valendo:** os campos de valor monetário/percentual em Folha e Rescisão (seção
7 e 8) são **campos de registro**, não cálculos automáticos — o sistema grava o que um profissional
de RH/contabilidade habilitado já calculou, não calcula sozinho. Isso é uma decisão deliberada, não
uma lacuna: calcular INSS/FGTS/IRRF errado tem risco financeiro e trabalhista real, e nenhuma
tabela de alíquota foi validada com um especialista. Se um dia quisermos que o sistema calcule de
verdade, isso é uma decisão à parte, tomada depois de validação humana — não implícita neste
desenho.

## 1. Visão geral — como as peças se conectam

```
        CategoriaSalarial ──1:N── Cargo ──1:N── TabelaSalarial (histórico de valor por cargo)
                │
                │ 1:N (percentual por ano de tempo de serviço, ver seção 2.5)
                ▼
          RegraAnuenio
                                     ▲
                                     │ N (FK Cargo, não mais texto livre)
                              ┌─────────────────────┐
                              │   UnidadeDeSaude     │  (já existe)
                              │   (uuid PK)          │
                              └──────────┬───────────┘
                                         │ 1
                          ┌──────────────┼───────────────────┐
                          │ N                                │ 0..1 (responsavel, ADR-0014)
                 ┌────────▼─────────┐                        │
                 │     Lotacao      │◄───────────────┐       │
                 │  (histórico;     │                 │       │
                 │   FK cargo acima)│                 │       │
                 └────────┬─────────┘                 │       │
                          │ N                          │ N     │ 0..1
                          │                             │       │
                 ┌────────▼──────────────────────────────▼───────▼───┐
                 │                  Profissional (já existe)          │
                 │         (matricula PK, cpf, ficha por contratação) │
                 └──┬───────┬───────┬───────┬────────┬────────┬──────┘
                    │ N     │ N     │ N      │ N      │ N      │ N
        ┌───────────▼─┐ ┌───▼────┐ ┌▼───────┐ ┌──────▼──┐ ┌───▼────┐ ┌▼──────────┐
        │AjusteIndiv.  │ │Afasta- │ │Registro│ │Treinam. │ │ SST    │ │Avaliação  │
        │ (só residual)│ │mento   │ │Ponto   │ │(partic.)│ │(exame/ │ │           │
        └──────────────┘ └───┬────┘ └────────┘ └─────────┘ │acident.│ └───────────┘
                              │ 0..1                        │ /EPI)  │
                       ┌──────▼─────┐                       └────────┘
                       │  Licenca   │
                       │(subtipo)   │
                       └────────────┘

        Profissional.dataDesligamento + ativo (já existe, ADR-0017)
                              │ 0..1
                       ┌──────▼──────────┐
                       │ CalculoRescisao │ (campos de registro, não cálculo automático)
                       └─────────────────┘

        Salário efetivo = TabelaSalarial vigente do Cargo (via Lotacao vigente) + anuênio calculado
        pela RegraAnuenio + AjusteIndividual vigente. FolhaPagamento (mensal) consome esse valor +
        RegistroPonto do mês + Afastamento/Licenca do mês. Campos de valor são registro, não
        cálculo automático.

        Vaga (FK UnidadeDeSaude) → Candidato → aprovado vira um Profissional novo (fecha o ciclo)
```

## 2. Fase 0 — Lotação, Cargo e Salário (pré-requisitos)

Ponto levantado na revisão: salário não é um número solto por profissional. Ele deriva de uma
**tabela salarial por cargo**, agrupada por categoria profissional (a mesma lógica de convenção
coletiva/dissídio). Isso muda a modelagem — em vez de gravar um valor direto pra cada profissional
a cada reajuste, o profissional referencia um **cargo**, e o cargo referencia sua **tabela de
valores por data**. Quatro motivos distintos de mudança, quatro formas diferentes de disparar:

- **Dissídio da categoria**: reajuste percentual que atinge todos os cargos daquela categoria de
  uma vez, proporcionalmente — um evento, poucos registros novos (um por cargo afetado, não um por
  profissional), propaga sozinho pra todo mundo lotado nesses cargos.
- **Promoção / mudança de cargo**: o profissional passa a ocupar outro `Cargo` — o valor muda
  automaticamente porque vem da tabela do cargo novo, sem precisar calcular nada à parte (mesmo que
  não haja dissídio nenhum acontecendo naquele momento).
- **Anuênio**: progressão automática por tempo de serviço — **regra**, não lançamento manual (seção
  2.5). Calculada a partir da `dataAdmissao`, não gravada por instância.
- **Ajuste individual**: o resíduo de verdade — só o que não é regra nem tabela (gratificação
  pessoal, equiparação salarial por decisão judicial), seção 2.6.

### 2.1 `CategoriaSalarial`

| Campo | Tipo | Observação |
|---|---|---|
| `id` | UUID (PK) | |
| `nome` | String | Ex.: "Enfermagem", "Medicina", "Administrativo" — o nível em que um dissídio se aplica |
| `convencaoColetiva` | String, opcional | Referência ao sindicato/convenção, se houver |

### 2.2 `Cargo`

| Campo | Tipo | Observação |
|---|---|---|
| `id` | UUID (PK) | |
| `categoria` | FK → `CategoriaSalarial` | |
| `nome` | String | Ex.: "Técnico de Enfermagem", "Enfermeiro", "Enfermeiro Chefe" — cada um é um cargo distinto, mesmo dentro da mesma categoria |

### 2.3 `TabelaSalarial` (histórico de valores por cargo)

| Campo | Tipo | Observação |
|---|---|---|
| `id` | UUID (PK) | |
| `cargo` | FK → `Cargo` | |
| `valorBase` | BigDecimal | |
| `dataVigencia` | LocalDate | |
| `motivo` | enum: `DISSIDIO`, `REVISAO_PLANO_CARGOS_SALARIOS` | |

**Regra**: valor vigente de um cargo = registro com maior `dataVigencia <= hoje`. Um dissídio gera
um novo registro **por cargo afetado** dentro da categoria (não por profissional) — o reajuste
proporcional é calculado uma vez por cargo e propaga automaticamente pra quem estiver lotado nele.

### 2.4 `Lotacao`

| Campo | Tipo | Observação |
|---|---|---|
| `id` | UUID (PK) | |
| `profissional` | FK → `Profissional` | Pela `matricula`, não pelo `cpf` — cada ficha tem sua própria lotação (ADR-0017: recontratação = ficha nova) |
| `unidade` | FK → `UnidadeDeSaude` | Onde o profissional efetivamente trabalha |
| `cargo` | FK → `Cargo` | Não é mais texto livre — é daqui que o salário vigente é derivado (via `TabelaSalarial`) |
| `jornadaSemanalHoras` | Integer, opcional | Alimenta `RegistroPonto` (fase 2) e validação de carga horária |
| `dataInicio` | LocalDate | |
| `dataFim` | LocalDate, nullable | `null` = lotação vigente |
| `motivo` | String | "Admissão", "Transferência", "Promoção", "Mudança de cargo" |

**Regra de integridade**: no máximo uma `Lotacao` com `dataFim = null` por `Profissional` (ficha) por
vez. Transferir unidade **ou** trocar de cargo (com ou sem trocar de unidade) = fechar a vigente
(`dataFim = hoje`) + criar uma nova. Não existe update de `unidade`/`cargo` numa `Lotacao`
existente — isso apagaria a história de onde/como a pessoa já trabalhou.

**Salário efetivo de um profissional numa data** = valor vigente em `TabelaSalarial` para o `cargo`
da sua `Lotacao` vigente naquela data, mais o anuênio calculado pela regra (seção 2.5), mais a soma
de quaisquer `AjusteIndividual` vigentes (seção 2.6). Não existe mais um "salário do profissional"
solto — ele é sempre derivado.

### 2.5 `RegraAnuenio` — regra automática, não lançamento manual

Anuênio é progressão por tempo de serviço: todo aniversário de tempo de casa, o valor deveria
aumentar sozinho, sem alguém precisar lançar um registro novo a cada ano. Por isso não é um
`AjusteIndividual` — é uma **regra**, aplicada no momento de calcular o salário efetivo (e, mais
adiante, na Folha), não um dado gravado por instância.

| Campo | Tipo | Observação |
|---|---|---|
| `id` | UUID (PK) | |
| `categoria` | FK → `CategoriaSalarial` | O percentual costuma vir de convenção coletiva, por categoria |
| `percentualPorAno` | BigDecimal | Ex.: 1% do valor base do cargo, por ano completo de tempo de serviço |
| `tetoAnos` | Integer, opcional | Algumas convenções limitam quantos anos contam (ex.: só até 25 anos) |

**Cálculo** (conceitual — não é código de Folha ainda, só a regra existindo no domínio): anos
completos = tempo entre `Profissional.dataAdmissao` **desta ficha** e a data de cálculo (não soma
tempo de fichas antigas — cada ficha é independente, ver ADR-0017: recontratação = ficha nova, então
o tempo de serviço reconta do zero); valor do anuênio = `min(anosCompletos, tetoAnos ?? ∞) ×
percentualPorAno × valorBase do cargo vigente`.

**Se um dia for necessário registrar formalmente cada concessão** (auditoria, exigência de ato
formal), isso vira uma entidade de histórico separada (ex.: `ConcessaoAnuenio`, gerada
automaticamente a cada aniversário, não digitada por alguém) — decisão adiada, não é regra deste
desenho ainda.

### 2.6 `AjusteIndividual`

Só para concessões que são de fato decisões pontuais e individuais — não regra automática (isso é
anuênio, seção 2.5), não tabela de cargo (isso é dissídio/mudança de cargo, seções 2.2-2.3).

| Campo | Tipo | Observação |
|---|---|---|
| `id` | UUID (PK) | |
| `profissional` | FK → `Profissional` | |
| `valor` | BigDecimal | Só acréscimo pessoal — desconto é outra coisa (folha, fase 7) |
| `dataInicio` | LocalDate | Obrigatório |
| `dataFim` | LocalDate, nullable | `null` = sem prazo definido; preenchido pra ajustes temporários (ex.: gratificação por projeto) |
| `motivo` | enum: `GRATIFICACAO_PESSOAL`, `EQUIPARACAO_JUDICIAL` | |
| `referencia` | String, opcional | Número do processo/fundamento legal, quando `motivo = EQUIPARACAO_JUDICIAL` — sem isso, daqui a um ano ninguém lembra por que aquele valor existe |

**Regra**: só existe pra casos que não são nem dissídio (que mexe na `TabelaSalarial` do cargo) nem
mudança de cargo (que já resolve sozinha via `Lotacao`) — é o resíduo, não o caminho principal de
reajuste.

## 3. Fase 1 — Afastamento

| Campo | Tipo | Observação |
|---|---|---|
| `id` | UUID (PK) | |
| `profissional` | FK → `Profissional` | |
| `tipo` | enum: `FERIAS`, `LICENCA_MEDICA`, `LICENCA_PESSOAL`, `OUTROS` | Tipos legais específicos (maternidade, acidente de trabalho, etc.) são `Licenca` (fase 3), não aqui |
| `dataInicio`, `dataFim` | LocalDate | |
| `status` | enum: `SOLICITADO`, `APROVADO`, `EM_ANDAMENTO`, `CONCLUIDO`, `CANCELADO` | |
| `observacao` | String, opcional | |

**Regra confirmada**: Afastamento **não** altera `Profissional.ativo`. `ativo`/`dataDesligamento`
continua reservado exclusivamente pro desligamento (ADR-0017) — afastado continua sendo profissional
ativo, só temporariamente ausente.

## 4. Fase 2 — Ponto

| Campo | Tipo | Observação |
|---|---|---|
| `id` | UUID (PK) | |
| `profissional` | FK → `Profissional` | |
| `dataHora` | LocalDateTime | |
| `tipo` | enum: `ENTRADA`, `SAIDA`, `INICIO_INTERVALO`, `FIM_INTERVALO` | |
| `origem` | String, opcional | manual / app / dispositivo |

**Depende de**: `Lotacao.jornadaSemanalHoras` (fase 0) pra saber a jornada esperada, e de
`Afastamento` (fase 1) pra não cobrar ponto de quem está afastado no período.

## 5. Fase 3 — Licença

Subtipo de `Afastamento` com regras legais próprias (duração, quem paga). Modelada como entidade
própria ligada 1:1 a um `Afastamento`, não como um novo campo nele — mantém `Afastamento` genérico
e simples pros casos (férias, licença pessoal) que não precisam dessa camada extra.

| Campo | Tipo | Observação |
|---|---|---|
| `id` | UUID (PK) | |
| `afastamento` | FK → `Afastamento` (1:1) | |
| `tipoLegal` | enum | maternidade, paternidade, doença, acidente de trabalho, falecimento, casamento, etc. |
| `responsavelPagamento` | enum: `EMPRESA`, `INSS`, `MISTO` | |
| `documentoUrl` | String, opcional | Comprovante |

## 6. Fase 4 — Desligamento e Rescisão

O desligamento em si **já existe** (`Profissional.ativo` + `dataDesligamento`, ver ADR-0017). O que
falta é o cálculo de verbas rescisórias — e aqui vale o aviso do topo do documento: campos de
registro, não cálculo automático.

### `CalculoRescisao`

| Campo | Tipo | Observação |
|---|---|---|
| `id` | UUID (PK) | |
| `profissional` | FK → `Profissional` | |
| `tipoDesligamento` | enum: sem justa causa, com justa causa, pedido de demissão, término de contrato, aposentadoria, falecimento | |
| `avisoPrevio`, `feriasVencidas`, `feriasProporcionais`, `decimoTerceiroProporcional`, `multaFgts`, `total` | BigDecimal | **Informados** por quem calculou fora do sistema (contador/DP), não calculados aqui |
| `documentoTrctUrl` | String, opcional | |

## 7. Fase 5 — Folha de pagamento

Fecha mensalmente, consumindo o salário efetivo (fase 0 — `TabelaSalarial` do `Cargo` vigente via
`Lotacao`, mais `AjusteIndividual`) + `RegistroPonto` do mês (fase 2) + `Afastamento`/`Licenca` do
mês (fases 1 e 3).

### `FolhaPagamento`

| Campo | Tipo | Observação |
|---|---|---|
| `id` | UUID (PK) | |
| `profissional` | FK → `Profissional` | |
| `competencia` | String (`MM/AAAA`) | |
| proventos, descontos, encargos, total | BigDecimal (vários campos) | Mesma ressalva: registro, não cálculo automático, a não ser que exista revisão/validação específica depois |

## 8. Fase 6 — Treinamento e certificações

- **`Treinamento`** (catálogo): `id`, `nome`, `cargaHoraria`, `validadeMeses`, `obrigatorio`.
- **`ParticipacaoTreinamento`**: `id`, `profissional` (FK), `treinamento` (FK), `dataConclusao`,
  `dataValidade` (= `dataConclusao` + `validadeMeses`), `certificadoUrl`.

**Uso pretendido**: bloquear escala/lotação em atividades que exigem certificação vencida — mas
isso depende de Escala existir, que está fora do escopo atual (nem chegou a ser levantado nesta
conversa; fica registrado aqui como consumidor futuro).

## 9. Fase 7 — SST (Saúde e Segurança do Trabalho)

- **`ExameOcupacional`**: `id`, `profissional` (FK), `tipo` (admissional/periódico/demissional),
  `dataRealizacao`, `dataValidade`, `resultado` (apto/inapto/apto com restrição), `asoUrl`.
- **`AcidenteTrabalho`**: `id`, `profissional` (FK), `dataHora`, `descricao`, `catEmitida`,
  `catUrl`, `diasAfastamento` — gera um `Afastamento` (fase 1) quando há dias de afastamento.
- **`EPI`**: `id`, `profissional` (FK), `tipo`, `numeroCA`, `dataEntrega`, `dataDevolucao`.

## 10. Fase 8 — Recrutamento

- **`Vaga`**: `id`, `unidade` (FK → `UnidadeDeSaude`), `cargo`, `quantidade`, `status`.
- **`Candidato`**: `id`, `vaga` (FK), `nome`, `cpf`, `curriculoUrl`, `status` (inscrito → triagem →
  entrevista → aprovado/reprovado).

**Fecha o ciclo**: candidato aprovado gera um `Profissional` novo via o `POST
/api/v1/profissional/` que já existe — não precisa de endpoint novo pra isso, só a orquestração de
"aprovar candidato" acionar a criação.

## 11. Fase 9 — Avaliação de desempenho

- **`CicloAvaliacao`**: `id`, `nome` (ex.: "2027-S1"), `dataInicio`, `dataFim`.
- **`Avaliacao`**: `id`, `profissional` (FK), `ciclo` (FK), `avaliador`, `nota`, `observacao`.

## 12. Regras transversais

| Regra | Onde se aplica |
|---|---|
| Dado que muda com o tempo vira histórico (nunca update destrutivo) | `Lotacao`, `TabelaSalarial`, `AjusteIndividual` — e por extensão qualquer campo que a Folha (fase 5) precise reconstruir retroativamente |
| Vigente = registro com maior data-de-início `<= hoje` | `Lotacao`, `TabelaSalarial`, `AjusteIndividual` |
| Salário nunca é um valor solto por profissional — é derivado do `Cargo` da `Lotacao` vigente (via `TabelaSalarial`), mais anuênio calculado, mais ajustes individuais | `Lotacao`, `Cargo`, `TabelaSalarial`, `RegraAnuenio`, `AjusteIndividual`, consumido por `FolhaPagamento` |
| Dissídio atualiza a `TabelaSalarial` uma vez por cargo afetado, nunca por profissional — propaga sozinho pra quem estiver lotado ali | `TabelaSalarial` |
| Progressão automática (tempo de serviço) é regra calculada, não lançamento gravado por instância — só vira registro se um dia precisar de auditoria formal da concessão | `RegraAnuenio` vs. `AjusteIndividual` |
| Afastamento/Licença não mexe em `Profissional.ativo` | `Afastamento`, `Licenca` — só `dataDesligamento` (ADR-0017) desliga de verdade |
| Valores monetários/percentuais são registrados, não calculados pelo sistema, até haver validação humana especializada | `CalculoRescisao`, `FolhaPagamento` |
| Toda entidade nova referencia `Profissional` pela `matricula` (a ficha), não pelo `cpf` sozinho | Todas — CPF não é mais único desde a ADR-0017 |

## 13. Referências

- [ESCOPO-RH.md](./ESCOPO-RH.md) — contexto, ordem das fases, aviso sobre a proposta original
- [ADR-0014](../adr/0014-modulo-profissional-rh-com-vinculo-fraco-por-reconciliacao.md) — vínculo responsável↔unidade (precedente pro FK opcional em `Vaga`/`Lotacao`)
- [ADR-0017](../adr/0017-numero-de-matricula-automatico-e-cpf-nao-unico.md) — matrícula como identidade da ficha, `dataDesligamento` como único gatilho de `ativo`
