# 0105 — Escalas: backend

## Status

Aceita e implementada (parte 3 de 4 de Equipes e Escalas). A tela vem na ADR-0106.

## Contexto

- O protótipo `Escalas.html` mostra uma grade semanal por profissional, com os seguintes elementos:
  - turnos de manhã, tarde, noite e 12h;
  - capacitação, folga, férias e licença;
  - plantões de 12h e 24h e sobreaviso;
  - vagas "sem médico" para designar;
  - troca de plantonista;
  - férias e licenças do RH;
  - indicadores de horas previstas, cobertura, plantões e afastados.
- Já existiam as peças que a escala precisa respeitar:
  - o horário estruturado e a situação da unidade (ADR-0101);
  - a lotação com jornada semanal e os afastamentos do RH;
  - as equipes (ADR-0103).
- A Enfermagem listava `Escala` como entidade ainda não iniciada (MAPA-DE-DOMINIOS).

## Decisão

1. **Turno com data, não padrão semanal.**
   - Cada `TurnoEscala` (`TB_TURNO_ESCALA`, migração V9) tem os seguintes campos:
     - unidade;
     - equipe (opcional);
     - profissional;
     - função;
     - tipo;
     - início e fim (data e hora);
     - descrição (atividade ou local, como "Imunização" ou "Sala de PA").
   - O fim antes do início cai no dia seguinte. No plantão de 24 horas, o fim é igual ao início.
   - A repetição da semana é feita pela **cópia de semana**, sem regra de recorrência. A escala real muda de semana a
     semana, e cada turno precisa ser um fato com data para auditar e contar horas.
2. **Tipos e regras de duração:**

   | Tipo | Duração | Onde |
   |---|---|---|
   | Manhã, tarde | até 12h, no mesmo dia | dentro do horário da unidade |
   | Noite | até 12h, pode virar o dia | só em unidade 24 horas |
   | Plantão 12h | exatamente 12h | só em unidade 24 horas |
   | Plantão 24h | exatamente 24h | só em unidade 24 horas |
   | Sobreaviso | até 24h | qualquer unidade; não soma na jornada (CLT, art. 244) |
   | Capacitação | até 12h, no mesmo dia | pode ser fora do horário da unidade |

   - Férias e licença não são turno: vêm do RH. Folga é o dia sem turno.
3. **Horário da unidade (ADR-0101):**
   - manhã e tarde precisam caber entre a primeira abertura e o último fechamento do dia. O intervalo de almoço da
     unidade não corta o turno de quem trabalha;
   - num dia sem horário cadastrado, a unidade não abre e o turno é recusado (422);
   - unidade sem nenhum horário estruturado não restringe;
   - unidade inativa, em obra ou inoperante não recebe escala (422).
4. **Profissional, com as mesmas regras de Equipes:**
   - **lotação** na unidade na data do turno (422);
   - **desligado:** 422;
   - **afastamento** aprovado ou em andamento no período (422). A mensagem diz "em férias", "de licença" ou
     "afastado", **sem dizer que a licença é médica**;
   - **sobreposição** com outro turno da pessoa, em qualquer unidade: 409.
5. **Vaga aberta:**
   - turno sem profissional, com a **função que falta** obrigatória (400);
   - a **designação** preenche a vaga ou **troca** quem está no turno, com as mesmas regras do cadastro;
   - o mesmo profissional de novo dá 409;
   - a auditoria registra "Vaga preenchida por" ou "Troca de A por B", com o motivo.
6. **Alertas, que avisam sem impedir:**
   - **jornada semanal** (segunda a domingo, em todas as unidades, sem sobreaviso) acima da contratada na lotação;
   - **descanso menor que 11 horas** entre dois turnos (CLT, art. 66);
   - turno de equipe com alguém que não é membro dela.
7. **Edição e remoção:**
   - a edição muda tipo, data, horário, equipe, função e descrição;
   - a unidade não muda, e trocar o profissional é pela designação (422);
   - turno já terminado não muda, e turno que já começou não é removido: os dois ficam como registro;
   - não se cria nem se move turno para dia passado.
8. **Cópia de semana:**
   - copia os turnos de uma semana da unidade para outra, no mesmo dia e horário, e as vagas continuam vagas;
   - o que cai em dia passado ou quebra uma regra fica de fora e volta na resposta com o motivo, por exemplo
     férias na semana de destino. A cópia não falha por um turno.
9. **Semana da unidade** (`GET /api/v1/escala/?unidadeId=&semana=&equipeId=`):
   - **linhas:** uma por pessoa lotada na unidade (ou membro da equipe filtrada) e por quem tem turno nela. Cada linha
     traz:
     - cargo, conselho e jornada contratada;
     - horas na semana e horas de sobreaviso;
     - equipes;
     - alertas;
     - ausências do RH na semana;
     - os turnos, inclusive os de outra unidade, com o nome dela;
   - **vagas abertas** da semana;
   - **férias e licenças dos próximos 30 dias**, sem a observação do afastamento, que pode ter motivo de saúde;
   - **indicadores:** horas previstas, profissionais, turnos, vagas abertas, plantões, afastados e pessoas com
     alerta.
10. **Rotas** em `/api/v1/escala/`:
    - `GET` (semana);
    - `POST turno`;
    - `PATCH turno/{uuid}`;
    - `POST turno/{uuid}/designar`;
    - `DELETE turno/{uuid}`;
    - `POST copiar-semana`.
11. **Permissões:**
    - **`ESCALA.GERENCIAR`** monta a escala. Fica nos papéis padrão Gestor e Coordenador de enfermagem, os mesmos de
      `EQUIPE.GERENCIAR`;
    - a leitura vale com `ESCALA.GERENCIAR`, `EQUIPE.GERENCIAR`, `RH.CONSULTAR`, `RH.GERENCIAR` ou
      `ADMINISTRATIVO.CONSULTAR`;
    - tudo dentro do escopo de unidades do usuário.

## Consequências

- A escala vira fato com data, base para a tela (ADR-0106) e, no futuro, para o ponto e o banco de horas.
- Escala e agenda continuam separadas: a agenda do profissional (ADR-0092) não lê a escala. Ligar as duas é pendência.
- **Fora do escopo:**
  - banco de horas e horas realizadas (ponto);
  - confirmação do plantonista;
  - troca pedida pelo próprio profissional;
  - regra de recorrência;
  - escala de sobreaviso remunerada;
  - adicional noturno.

## Testes

- **JUnit:**
  - `EscalaControllerTest` (8 casos):
    - turno no horário e visto na semana;
    - fora do horário, dia fechado e fim antes do início;
    - plantão só em unidade 24 horas, com a duração exata;
    - lotação, licença sem revelar o tipo e sobreposição;
    - vaga sem função, designação, mesma pessoa e troca;
    - alerta de jornada acima da contratada e de descanso de 6h;
    - edição, troca pela edição recusada, remoção e dia passado;
    - cópia de semana com um turno de fora por férias.
  - `EscalaAutorizacaoControllerTest` (autorização ligada): o gestor da unidade monta a escala, o enfermeiro não e o
    gestor de outra unidade não vê.
- **Robot de API** (`test/escalas/escala/Escala.robot`, 7 casos): turno e semana, vaga sem função, plantão fora de unidade
  24 horas, sobreposição, designação, cópia de semana e remoção.
