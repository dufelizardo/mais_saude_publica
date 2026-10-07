# Modelo do domínio Programas, Ações e Linhas de Cuidado

**Data:** 2026-10-07 (criado)
**Status:** desenho aprovado na [ADR-0109](../adr/0109-programas-acoes-e-linhas-de-cuidado.md). **Nada implementado
ainda.** A implementação vem nas fatias P1 a P6 (seção 12), cada uma com ADR própria.

Este documento detalha o domínio: os conceitos e por que ficam separados, as entidades, o acompanhamento do cidadão,
o dado sensível, como o protótipo `Programas.html` se encaixa nas fatias e onde o domínio liga com o que já existe.

## 1. O problema: conceitos que se misturam

O sistema já tem, ou vai ter, conceitos vizinhos que não podem virar uma coisa só:

| Conceito | Exemplo | Onde mora |
|---|---|---|
| Equipamento | UBS, UPA, hospital, CAPS | Organização (ADR-0101) |
| Equipe | eSF, eAP, eMulti | Organização (ADR-0103) |
| Território | área, microárea, adscrição | #21 Território (ADR-0108) |
| Serviço | consulta, vacinação, dispensação, exame | domínios assistenciais |
| **Programa** | Saúde da Mulher, Hipertensão, Tuberculose | **#22 (este domínio)** |
| **Campanha** | vacinação contra uma doença, campanha de prevenção | **#22** |
| **Linha de cuidado** | gestante, da UBS à maternidade | **#22** |
| Política pública | diretriz ampla do SUS | só referência, depois |
| **Ação programática** | busca ativa, grupo terapêutico, aferição | **#22**, realizada pelos serviços |

Se tudo virar `Programa`, a arquitetura quebra rápido.

## 2. Programa

Organização **permanente ou semipermanente** de ações de saúde para um público, uma condição, um território ou um
objetivo. Exemplos:
- Saúde da Mulher, da Criança e do Idoso;
- Hipertensão, Diabetes (HiperDia), Tuberculose e Hanseníase;
- Saúde Mental, Saúde Bucal e Saúde do Trabalhador;
- Imunização, Pré-natal e Planejamento Reprodutivo;
- Tabagismo.

**Não é enum.** O município cadastra programas próprios sem mudar código. Os nacionais entram como carga inicial
editável (seção 10).

### Programa não é serviço

O programa **organiza e acompanha o uso** dos serviços, que continuam nos domínios assistenciais:

```text
Programa: Saúde da Mulher
     ↓ prevê
Ações programáticas
     ↓ realizadas por
Serviços: consulta médica, consulta de enfermagem, exame, vacina, procedimento, visita domiciliar, encaminhamento
```

E não `Programa = Serviço`.

### Público-alvo

| Programa | Público |
|---|---|
| Saúde da Criança | 0 a 9 anos |
| Saúde do Idoso | 60 anos ou mais |
| Pré-natal | gestantes |
| Hipertensão | diagnóstico ou acompanhamento |

O público-alvo é **dado do programa** (faixa etária, sexo, condição como gestante, condição clínica por CID ou CIAP),
não regra codificada. Ele **sugere elegíveis**, como gestantes fora do pré-natal ou idosos sem acompanhamento, e
**não inscreve sozinho**: a inscrição é ato de profissional.

## 3. Acompanhamento programático

O valor do domínio está aqui. Um cidadão pode estar em vários programas ao mesmo tempo:

```text
Paciente
 ├── Hipertensão
 ├── Diabetes
 ├── Saúde do Idoso
 └── Saúde Bucal
```

O nome é **acompanhamento** e não "inscrição", porque nem todo programa tem inscrição formal:

```text
AcompanhamentoProgramatico
 ├── cidadão (Paciente)
 ├── programa
 ├── início, fim e motivo de saída
 ├── situação (ativo, faltoso, concluído, encerrado)
 ├── unidade responsável
 ├── equipe responsável
 └── profissional responsável
```

É **histórico**: entrar, ficar faltoso, sair e voltar são registros com data, não um campo que se sobrescreve.

### Com o território

Com a adscrição do domínio Território (ADR-0108), o acompanhamento ganha o endereço assistencial:

```text
UBS Jardim X → eSF 03 → Área 02 → Microárea 07 → Famílias → João
                                                          │
                                       Hipertensão, Diabetes, Saúde do Idoso
```

E o sistema responde perguntas como:
- quantos hipertensos estão na microárea 07?
- quantos diabéticos da eSF 03 estão sem acompanhamento há mais de 6 meses?

Até a adscrição existir, o acompanhamento já guarda a unidade e a equipe responsáveis.

## 4. Campanha

Separada de programa, porque tem **início e fim**, público, objetivo, metas, locais e ações:

```text
Campanha Nacional de Vacinação
 ├── período
 ├── público e meta (ex.: 90% de cobertura)
 ├── locais: UBS, escolas, unidades móveis, postos temporários
 └── ações: vacinação, busca ativa, comunicação, mobilização, registro
```

A campanha **pode** estar ligada a um programa (por exemplo, Imunização), mas não é um programa. Na campanha de
vacinação, as doses aplicadas são registradas pelo domínio #24 Imunização ([ADR-0110](../adr/0110-vigilancia-em-saude.md)).

## 5. Linha de cuidado

É uma **organização do percurso assistencial** e atravessa vários domínios:

```text
Linha de cuidado da gestante
UBS → pré-natal → exames → regulação → maternidade → parto → puerpério
```

O programa é uma organização programática; a linha de cuidado é um percurso. A linha de cuidado referencia
programas, serviços, encaminhamentos, regulação e unidades de referência, e por isso vem por último (P5).

## 6. Por que não existe "Outros"

Uma entidade genérica "Outros" vira a gaveta de tudo que não foi modelado direito. Uma iniciativa nova entra como
programa, campanha ou linha de cuidado, com **categoria cadastrável** (seção 8). Se aparecer algo que não cabe em
nenhum dos três (projeto, estratégia), a decisão é registrar um tipo novo numa ADR, não jogar numa gaveta.

## 7. Relações

```text
                         ┌─────────────────┐
                         │     Programa    │
                         └────────┬────────┘
                    ┌─────────────┼─────────────┐
                    ▼             ▼             ▼
             Acompanhamento   Campanha    Ação programática
                    │                           │
                    ▼                           ▼
                Cidadão                  Serviços / Atendimento
        ┌───────────┼────────────┐
        ▼           ▼            ▼
     Equipe      Unidade      Território

Linha de cuidado ── Programa, serviços, encaminhamentos, regulação, unidades de referência
```

**Domínio transversal:** referencia Paciente, Equipe, Unidade, Território, Atendimento e Prontuário, mas não é dono
de nenhum deles, para não virar um "superdomínio" que conhece tudo.

Dependência conceitual:

```text
Território → Adscrição → Equipe → Programa → Acompanhamento → Ações → Atendimento
```

## 8. Entidades previstas

Os atributos são uma previsão; a ADR de cada fatia fecha o desenho.

| Entidade | Atributos previstos | Fatia |
|---|---|---|
| `CategoriaPrograma` | nome, ordem, ativa (carga: Atenção Básica, Crônicos, Ciclos de vida, Saúde Mental) | P1 |
| `Programa` | código, nome, descrição, categoria, abrangência (nacional, estadual, municipal), situação, início, fim, coordenação (profissional), periodicidade de acompanhamento, sensível | P1 |
| `PublicoAlvo` (do programa) | faixa etária, sexo, gestante, condições clínicas (CID ou CIAP) | P1 |
| `ParticipacaoPrograma` | programa, unidade ou equipe participante, início, fim | P1 |
| `AcompanhamentoProgramatico` | paciente, programa, início, fim, motivo de saída, situação, unidade, equipe e profissional responsáveis | P2 |
| `Campanha` | nome, descrição, programa (opcional), início, fim, situação (planejada, em andamento, encerrada), público, meta | P3 |
| `LocalCampanha` | campanha, unidade da rede ou local temporário (nome, endereço) | P3 |
| `AcaoProgramatica` | programa ou campanha, tipo (busca ativa, grupo, aferição, exame, vacina, visita), responsável, data prevista, situação, ligação com o registro assistencial que a cumpriu | P4 |
| `LinhaDeCuidado`, `EtapaLinha` | nome, programa, etapas com serviço e unidade de referência | P5 |
| `MetaProgramatica`, `IndicadorProgramatico` | programa ou campanha, indicador, meta, período, apuração | P6 |

## 9. Dado sensível e permissões

**Estar num programa é dado de saúde** (LGPD, art. 11): hipertensão, tuberculose, saúde mental ou IST revelam
diagnóstico.

- **Ler os programas de um paciente** segue as regras do prontuário: permissão clínica, auditoria de leitura
  (ADR-0070) e vínculo assistencial (ADR-0076).
- **Programa sensível** (por exemplo, IST/HIV ou saúde mental) restringe ainda mais quem vê o acompanhamento.
- **Agregados não identificam:** os indicadores e a consulta pública na landing page mostram números, nunca o
  paciente.

| Permissão | Para quê | Natureza |
|---|---|---|
| `PROGRAMA.GERENCIAR` | cadastrar programas, categorias e campanhas | operação |
| `PROGRAMA.ACOMPANHAR` | inscrever, mudar a situação e encerrar acompanhamento | dado de saúde |
| leitura do catálogo | ver programas e campanhas (sem pacientes) | qualquer usuário logado |

## 10. Carga inicial

Editável pelo município. Inclui os programas do protótipo e os nacionais comuns:

| Categoria | Programas |
|---|---|
| Atenção Básica | Saúde da Família, Saúde Bucal, Imunização, Planejamento Reprodutivo |
| Crônicos | HiperDia (hipertensão e diabetes), Tuberculose, Hanseníase, Tabagismo |
| Ciclos de vida | Pré-natal, Saúde da Mulher, Saúde da Criança, Saúde do Idoso |
| Saúde Mental | Saúde Mental (CAPS e acolhimento) |

## 11. O protótipo `Programas.html` e as fatias

| Bloco do protótipo | Usa | Sai de "Em breve" em |
|---|---|---|
| Abas por categoria e cartões dos programas | catálogo | P1 |
| Coordenação e "Ativo · N unidades" | catálogo e participação | P1 |
| Inscritos, faltosos e "+N · 30d" no cartão | acompanhamento | P2 |
| "Inscrever paciente" e "Ver pacientes" | acompanhamento | P2 |
| Equipe responsável | equipes participantes e acompanhamento | P2 |
| Pacientes em destaque e busca ativa | acompanhamento e última aferição | P2 e P4 |
| Ações pendentes da semana | ações programáticas | P4 |
| PA controlada, HbA1c, cobertura e evolução de 12 meses | indicadores programáticos | P6 |
| "Relatório clínico" | indicadores | P6 |

## 12. Fatias de implementação

| Fatia | Entrega | Depende de |
|---|---|---|
| **P1** · catálogo | categoria, programa, público-alvo, participação e carga inicial; tela em cartões pelo protótipo, com os indicadores "Em breve" | — |
| **P2** · acompanhamento | inscrever, mudar a situação e encerrar; aba Programas e filtro em Pacientes; inscritos e faltosos no programa; programas na seção da equipe | P1, ADR-0076 |
| **P3** · campanhas | cadastro com período, público, meta e locais; tela e consulta pública na landing page | P1 |
| **P4** · ações programáticas | ação prevista ligada ao registro assistencial; ações pendentes e busca ativa; programa na marcação da Agenda | P2 |
| **P5** · linhas de cuidado | percurso com etapas, serviços e unidades de referência | Atendimento, Regulação e Prontuário maduros |
| **P6** · metas e indicadores | cobertura, controle, pré-natal adequado, faltosos; alimenta o #19 Indicadores e BI | P2, P4 |

As ondas são as da análise original, com uma troca: **campanhas sobem para a P3**. São catálogo simples e foram
pedidas explicitamente.

## 13. Onde liga com o que já existe

| Tela ou domínio | "Em breve" hoje | Fatia |
|---|---|---|
| Pacientes | indicador "Em programas de saúde", filtro por programa, aba Programas | P2 |
| Agenda (ADR-0091) | programa na marcação (HiperDia, pré-natal) | P4 |
| Equipes (ADR-0104) | programas vinculados e indicadores do território | P2 e P6 |
| Landing page | consulta pública de programas e campanhas | P1 e P3 |
| Território (ADR-0108) | perguntas por microárea | P2, com a F3 do território |

**Fora do escopo agora:**
- política de saúde como entidade;
- estratificação de risco;
- integração com o e-SUS, SISAB e Previne;
- inscrição automática por regra.
