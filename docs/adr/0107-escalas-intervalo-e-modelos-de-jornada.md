# 0107 — Escalas: intervalo no turno e modelos de jornada

## Status

Aceita e implementada. Complementa as ADRs [0105](./0105-escalas-backend.md) (backend) e [0106](./0106-escalas-tela.md)
(tela).

## Contexto

- O caso pedido foi a jornada de **40h semanais, 8h por dia com 1h de intervalo**, por exemplo das 08:00 às 17:00.
- A escala da ADR-0105 não tinha intervalo: o turno das 08:00 às 17:00 contava 9h. Cinco dias somavam 45h e disparavam o
  alerta de jornada acima de 40h.
- Também não havia tipo de turno para o dia inteiro, e montar a semana exigia cinco turnos lançados um a um.
- Pela CLT (art. 71), o intervalo de repouso e alimentação **não conta na jornada**:
  - de 15 minutos quando o trabalho passa de 4h e vai até 6h;
  - de 1h a 2h quando passa de 6h;
  - no 12x36, o intervalo pode ser indenizado (art. 59-A).

## Decisão

1. **Intervalo no turno** (`intervaloMinutos`, migração V10):
   - de 0 a 120 minutos. Fora disso, ou igual ou maior que o turno, dá 400;
   - **as horas do turno são as trabalhadas:** a duração menos o intervalo. É o que entra na jornada semanal, nos
     indicadores e nos alertas;
   - a sobreposição e o descanso de 11h continuam olhando o turno inteiro, de ponta a ponta.
2. **Alerta de intervalo**, que avisa sem impedir, como os de jornada e descanso:
   - abaixo de 1h quando o trabalho passa de 6h;
   - abaixo de 15 minutos quando passa de 4h;
   - plantões de 12h e 24h e sobreaviso não geram esse alerta.
3. **Tipo novo `DIURNO`, "Diurno (8h + 1h)":**
   - sugere 08:00 às 17:00 com 1h de intervalo;
   - segue a regra de manhã e tarde: cabe no horário da unidade e termina no mesmo dia.
4. **Intervalo sugerido por tipo na tela:** manhã e tarde com 15 minutos, diurno e noite com 1h, plantão, sobreaviso e
   capacitação sem intervalo. A gaveta mostra as horas trabalhadas enquanto se edita.
5. **Modelos de jornada** (`POST /api/v1/escala/aplicar-modelo`), que geram a semana de um profissional:

   | Modelo | Turnos gerados |
   |---|---|
   | 40h — 8h + 1h | seg a sex, 08:00–17:00 (diurno) |
   | 44h — 6x1 | seg a sex 08:00–17:00 e sáb 08:00–12:00 |
   | 30h — 6h + 15min | seg a sex, 07:00–13:15 |
   | 20h — 4h | seg a sex, 08:00–12:00 |
   | 12x36 | seg, qua, sex e dom, plantões de 12h, só em unidade 24 horas |

   - O início e o intervalo podem ser mudados antes de aplicar. O fim de cada dia sai das horas do modelo mais o
     intervalo; dia de até 4h não tem intervalo.
   - Cada turno passa pelas regras do cadastro. O que cai em dia passado ou quebra uma regra (afastamento, lotação,
     horário da unidade, sobreposição) **fica de fora e volta com o motivo**, como na cópia de semana.
   - A auditoria registra o modelo, o profissional, a semana e quantos turnos entraram.
6. **Tela:**
   - **Aplicar modelo** no topo abre a gaveta: profissional, modelo, início, intervalo e equipe;
   - o modelo sugerido vem da **jornada contratada na lotação** (40h sugere o de 8h + 1h);
   - a gaveta mostra a **prévia dos turnos** e o total da semana, e depois o resultado com o que ficou de fora;
   - o bloco da grade mostra o intervalo, por exemplo "08h–17h · Diurno · 8h (1h int.)".

## Consequências

- A jornada de 40h com 8h + 1h é lançada em um passo e fecha em 40h, sem alerta.
- Turnos criados antes da V10 ficam com intervalo zero e continuam contando como antes.
- Fora do escopo: modelos cadastráveis pelo usuário, horário do intervalo dentro do turno (só a duração) e o ponto.

## Testes

- **JUnit** (`EscalaControllerTest`):
  - cinco dias diurnos de 08:00 às 17:00 com 1h somam 40h sem alerta;
  - sem intervalo, 9h e o alerta da CLT;
  - intervalo acima de 2h e maior que o turno recusados;
  - plantão de 12h sem intervalo não gera alerta;
  - modelo de 40h gera cinco turnos e 40h;
  - modelo deixa de fora o dia de férias, o 12x36 fora de unidade 24 horas e o sábado em que a unidade não abre.
- **Robot de API:** turno diurno com 1h contando 8h; modelo de 40h com cinco turnos.
- **Robot de interface:** modelo de 40h aplicado pela gaveta, com cinco turnos e 40h na API.
