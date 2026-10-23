# 0143 — Indicadores e BI: produção assistencial por unidade (BI1)

## Status

Aceita e implementada (fatia BI1 da [ADR-0142](./0142-indicadores-bi-e-gestao.md)).

## Contexto

- **A gestão pediu os atendimentos por dia e por mês de cada unidade.** O detalhe do programa (ADR-0141) não responde
  isso, porque produção não é de programa.
- **O Atendimento já registra** a unidade, a data e hora, o tipo, a especialidade, o profissional e o paciente (idade e
  sexo). **A Agenda já registra** a situação de cada marcação (realizada, falta, cancelada) e o encaixe.
- **O protótipo `Painel.html`** mostra a faixa de indicadores com a variação, os atendimentos por hora, a faixa etária
  e a produtividade por unidade.

## Decisão

1. **O que conta:**
   - **Atendimento:** só o **concluído**, pela data e hora do atendimento. O registro retificado continua sendo um
     atendimento só.
   - **Agenda:** cada marcação pela data e hora marcada; a marcação sem unidade (anterior à agenda por unidade, ADR-0091)
     não entra em nenhuma abrangência.
     - **marcações:** todas;
     - **realizadas, faltas e canceladas:** pela situação;
     - **encaixes:** os encaixes não cancelados;
     - **absenteísmo:** faltas sobre realizadas mais faltas, em %. As pendentes (agendada ou confirmada) não entram
       na conta.
2. **Período:**
   - o padrão vai do primeiro dia do mês até hoje, e o máximo é um ano (400);
   - o início depois do fim também dá 400;
   - o período anterior tem a mesma duração e termina na véspera do início;
   - a variação é em %, e fica nula quando o anterior é zero.
3. **Distribuições do período:**
   - **por tipo:** consulta, urgência, internação;
   - **por especialidade:** sem especialidade quando não há;
   - **por categoria do profissional:** pelo conselho de classe, nas categorias da tela Profissionais
     ([ADR-0072](./0072-tela-profissionais-a-partir-do-prototipo.md)): médicos, enfermagem, odontologia, farmácia,
     outros conselhos, apoio;
   - **por faixa etária:** a idade no dia do atendimento, nas faixas do protótipo (neonatal até 28 dias, 1ª infância
     até 5 anos, crianças de 6 a 12, adolescentes de 13 a 17, adultos de 18 a 59, idosos com 60 ou mais);
   - **por sexo;**
   - **por hora do dia:** as 24 horas.
4. **Série:**
   - por dia (padrão 30 pontos, até 90) ou por mês (padrão 12, até 24), terminando hoje (400 fora disso);
   - cada ponto traz os atendimentos concluídos e as faltas.
5. **Ranking:** as unidades do acesso com produção no período ou no anterior, da maior para a menor, com a variação.
6. **Abrangência:**
   - com a unidade, `INDICADOR.CONSULTAR` num escopo que a cubra (403);
   - sem unidade, as unidades do acesso: a rede municipal para quem tem acesso na rede, e "Unidades do seu acesso" para
     os demais.
7. **Exportação:** o resumo em CSV (ponto e vírgula, UTF-8 com BOM), na rota `producao/exportacao`, que a auditoria
   registra como exportação.
8. **Permissão:** `INDICADOR.CONSULTAR`, nova, vai para o gestor e o coordenador de enfermagem pelo catálogo de acesso.
   Nenhuma migração: o catálogo semeia a permissão na subida.

### API

| Método | Caminho | Permissão |
|---|---|---|
| GET | `producao?de&ate&unidadeId` | `INDICADOR.CONSULTAR` (escopo) |
| GET | `producao/serie?granularidade=DIA\|MES&unidadeId&pontos` | idem |
| GET | `producao/unidades?de&ate` | idem (unidades do acesso) |
| GET | `producao/exportacao?de&ate&unidadeId` | idem; auditada |

### Tela

**Sala de Situação** (`/sala-de-situacao`), o primeiro item do menu, para quem tem a permissão. Segue o protótipo
`Painel.html`, sobre o fundo `var(--ink-50)` das telas internas. Tem:
- **Filtros:** a unidade (ou todas do acesso), Hoje, Semana, Mês, Ano e período livre, e o botão "Exportar".
- **Seis indicadores com a variação:** atendimentos concluídos, média diária, marcações, faltas, absenteísmo e
  encaixes.
- **Gráficos:** atendimentos por dia ou por mês e por hora (com o pico).
- **Distribuições:** faixa etária, tipo e categoria, especialidade e sexo.
- **Ranking:** a produtividade por unidade.
- **Um bloco "Em breve"** com o que depende de outras fatias:
  - alertas e tempo de espera (BI2);
  - ocupação das agendas (BI2);
  - ausências da escala (BI2);
  - vacinação (domínio 24);
  - programas na rede (BI3).

## Consequências

- **O tempo de espera do protótipo** pede o registro dos tempos do atendimento (chegada, chamada e início). Fica para
  a BI2.
- **A consulta lê os atendimentos e as marcações do período e agrega na aplicação.** É o bastante para a escala
  municipal; a materialização fica para quando pesar (ADR-0142).

## Testes

- **JUnit:**
  - `ProducaoAssistencialControllerTest`: contagens, distribuições, período anterior, agenda, série, ranking, CSV e
    parâmetros inválidos;
  - `ProducaoAutorizacaoControllerTest`, com segurança ligada: 401, 403 sem a permissão, 403 fora do escopo, unidades
    do acesso e exportação auditada.
- **Robot de API:** `test/indicadores/ProducaoAssistencial.robot`.
- **Robot de tela:**
  - `test/ui/indicadores/UI_sala_de_situacao.robot`;
  - CT-003 de `test/ui/acesso/UI_botoes_por_permissao.robot`: o item do menu aparece só com a permissão.
