# 0101 — Equipamentos de Saúde: backend

## Status

Aceita e implementada (parte 1 de 2). A tela, pelo protótipo `Equipamentos.html`, vem na ADR-0102.

## Contexto

- A unidade (`UnidadeDeSaude`, ADRs 0002, 0009 e 0013) já tinha tipo, hierarquia, supervisão regional,
  endereço, contato, responsável e horário de funcionamento e de atendimento **em texto livre** por dia
  (ex.: "8:00 AM - 9:00 PM").
- O cadastro e a edição pela API identificam a unidade **pelo nome**, o que quebra quando ela é renomeada.
  Nenhuma tela usava essas rotas.
- O protótipo pede CNES, situação operacional (manutenção, obra), um resumo por unidade e uma ficha com
  horário, vínculos, equipes e histórico.
- A Agenda (ADR-0092) tinha a pendência de não respeitar o horário da unidade: era preciso lançar bloqueio
  à mão.

## Decisão

1. **CNES na unidade:**
   - 7 dígitos, opcional e único;
   - repetido dá 409; formato errado, 400.
2. **Situação operacional:**
   - em operação, em manutenção, em obra ou inoperante;
   - fora de operação exige motivo, e a previsão de retorno é opcional (não pode ser no passado);
   - voltar à operação limpa motivo e previsão;
   - cada mudança vira um evento só de inclusão (`TB_EVENTO_SITUACAO_UNIDADE`), com a situação anterior, a
     nova, o motivo, a previsão, quem e quando. A mesma situação de novo dá 422.
3. **Horário estruturado** (`TB_HORARIO_UNIDADE`):
   - turnos por dia da semana, com hora de abrir e de fechar no mesmo dia;
   - até 3 turnos por dia, sem sobrepor; dia sem turno é dia fechado;
   - o horário é trocado inteiro. Turno que fecha antes de abrir ou turnos sobrepostos dão 400.
   - **Funciona 24 horas** é um indicador próprio da unidade. Hospital e UPA nascem com ele, e a migração
     marca os que já existem.
   - O texto livre antigo continua guardado e aparece na ficha quando não há horário estruturado. Não houve
     conversão automática, porque os textos não têm formato único.
4. **A Agenda respeita a unidade** (`FuncionamentoUnidade`):

   | Situação da unidade | O que a Agenda faz |
   |---|---|
   | **Em obra ou inoperante** | não oferece vaga e recusa marcação e encaixe (422) |
   | **Com horário estruturado (e sem 24 horas)** | só oferece as vagas que cabem inteiras num turno do dia; recusa encaixe fora do horário (422) e bloco recorrente fora do horário (422) |
   | **Sem horário estruturado** | segue como antes |
   | **Em manutenção** | só avisa |

   A resposta da agenda ganha `avisoUnidade`, e a tela mostra o aviso.
5. **Rotas por id** em `/api/v1/unidade-saude/`, ao lado das antigas por nome, que continuam:
   - `GET rede`:
     - todas as unidades do escopo, com tipo, CNES, situação, endereço, telefone, e-mail, unidade
       superior, supervisão, profissionais lotados (lotação vigente do RH), setores, leitos, leitos
       ocupados e se tem horário;
     - os indicadores contam só unidades de atendimento (sem os níveis federal, estadual, municipal e
       regional): total, ativas, em operação, fora de operação e por tipo;
   - `GET id/{uuid}`: a ficha, com endereço completo, telefones, responsável, turnos, horário em texto,
     unidades abaixo (pela hierarquia e pela supervisão), setores, profissionais lotados e histórico;
   - `POST id`:
     - a unidade superior precisa ser do nível esperado (ADR-0009), senão 422;
     - nome ou CNES repetido dá 409;
   - `PATCH id/{uuid}`:
     - edita nome, CNES, endereço, contato, responsável e supervisão regional;
     - **o tipo não muda** (422), nem a unidade superior;
   - `PUT id/{uuid}/horarios`;
   - `POST id/{uuid}/situacao`.
6. **Acesso:**
   - ler com `ORGANIZACAO.GERENCIAR` ou `ADMINISTRATIVO.CONSULTAR`;
   - escrever com `ORGANIZACAO.GERENCIAR`;
   - sempre no escopo: cadastrar exige o escopo da unidade superior;
   - as alterações entram na trilha de auditoria.
7. **Migração V7:** colunas novas na unidade, as duas tabelas e a marcação de 24 horas para hospital e UPA.

## Consequências

- A tela de Equipamentos de Saúde pode ser feita sem as rotas por nome.
- Fechar uma unidade (obra, inoperante) fecha a agenda dela na hora.
- O horário estruturado passa a valer para a agenda nas unidades que o cadastrarem. As que não cadastrarem
  continuam como antes.
- **Fora do escopo** (em `PENDENCIAS.md`):
  - sincronização com o CNES;
  - coordenadas e mapa;
  - microáreas e famílias;
  - equipes de Saúde da Família;
  - inspeção sanitária;
  - capacidade de consultórios;
  - exportação;
  - o horário de atendimento estruturado (só o de funcionamento ficou estruturado).

## Testes

- **JUnit** (`RedeUnidadesControllerTest`):
  - cadastro por id com a regra de nível, 24 horas para a UPA, nome e CNES repetidos e CNES inválido;
  - rede com setores, leitos e ocupação; subordinadas na ficha do município;
  - edição sem mudar o tipo;
  - horário sobreposto ou invertido recusado;
  - bloco fora do horário recusado e 4 vagas no turno;
  - encaixe fora do horário recusado;
  - situação sem motivo recusada; em obra sem vagas, com aviso e sem marcação; volta à operação com o
    histórico.
  - `AgendaControllerTest`, `AgendamentoControllerTest` e `MigracoesDoEsquemaTest` também passaram.
- **Robot de API** (`test/equipamentos/rede_unidades`): 7 casos. As suítes de Agenda e de unidades
  continuam passando.
- **Robot de interface:** CT-007 de `UI_agenda.robot`, unidade em obra avisa e fecha a agenda.
