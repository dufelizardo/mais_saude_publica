# 0087 — Regulação do acesso: backend

## Status

Aceita e implementada (parte 1 de 3). Abre o domínio #11 do
[MAPA-DE-DOMINIOS](../MAPA-DE-DOMINIOS.md). A tela vem na ADR-0088, e o fechamento do ciclo
(agendamento, vínculo e contrarreferência) na ADR-0089.

## Contexto

- Quando a unidade de origem não oferece uma consulta especializada, um exame ou um procedimento, o
  paciente precisa ser encaminhado. Hoje isso não passa pelo sistema: não há fila, nem prioridade, nem
  registro de quem decidiu.
- O levantamento de equipamentos (`MAPA-DE-EQUIPAMENTOS-DE-SAUDE.md`) separa duas centrais:
  - a **Central de Regulação do Acesso**, que cuida da fila eletiva;
  - a **Central de Regulação Médica das Urgências**, que despacha o SAMU.
- A [ADR-0076](./0076-prontuario-por-vinculo-assistencial.md) deixou prevista a regulação como quarta
  condição de vínculo assistencial.

## Decisão

1. **Escopo: a Central de Regulação do Acesso.**
   - Encaminhamento eletivo para consulta especializada, exame e procedimento.
   - Ficam de fora: urgência e SAMU, internação (depende de Leitos, #12), integração com SISREG e
     SIGTAP, e cotas por unidade (PPI).
2. **Procedimento regulado:** catálogo da Central, com nome único, tipo e ativo.
   - Não se apaga; sai de uso com `ativo = false`, e as solicitações antigas continuam apontando para ele.
   - Procedimento de acesso direto não entra no catálogo: é agendado na própria unidade, sem passar pela
     Central.
3. **Solicitação de regulação:**
   - Registra paciente, procedimento, unidade e profissional solicitantes, CID-10, justificativa clínica
     e prioridade.
   - O que foi pedido não muda depois. O CID é guardado sem ponto e em maiúsculas (`e11.9` vira `E119`).
   - **Prioridade em quatro cores**, como nas centrais: vermelho (emergência), amarelo (urgente), verde
     (não urgente) e azul (eletivo).
   - Só uma solicitação em andamento por paciente e procedimento (409).
4. **Ciclo:** Solicitada → Autorizada (com unidade executante e data e hora da vaga), Devolvida (para
   complementar) ou Negada.
   - Cancelada vale até a autorização, pelo solicitante ou pela regulação.
   - Ao complementar, a devolvida volta à fila **na posição original**: a hora do pedido não muda.
   - Agendada, Realizada e Faltou já existem no enum, mas são da ADR-0089.
   - Cada passo grava um **evento imutável** com quem fez, quando, o status e a prioridade que
     resultaram, e o texto (motivo, complemento ou observação).
   - Devolver, negar, cancelar e reclassificar exigem motivo.
5. **Fila calculada, não armazenada:**
   - inclui as solicitações `SOLICITADA`, ordenadas por prioridade e depois pela hora do pedido;
   - a posição conta a fila do procedimento na rede inteira, porque a fila é uma só;
   - o regulador pode reclassificar a prioridade enquanto a solicitação está na fila.
6. **Quem decide não é quem pediu:** autorizar e negar recusam (422) o profissional que fez a
   solicitação. Devolver e reclassificar não têm essa trava, porque não decidem o acesso.
7. **Dado clínico separado do andamento:**
   - A listagem e a fila trazem o **resumo**, sem CID e sem justificativa. Ele serve à recepção para
     informar o paciente, inclusive a posição na fila.
   - O **detalhe** (`GET /solicitacao-regulacao/{uuid}`) traz CID, justificativa e eventos. Só quem
     solicita ou regula pode ler, e a leitura é auditada (`@AuditarLeitura`).
8. **Acesso:**
   - `REGULACAO.CONSULTAR`, só o andamento: entra em Recepção e Médico.
   - `REGULACAO.SOLICITAR`, dado de saúde: entra em Médico.
   - `REGULACAO.REGULAR`, dado de saúde: no papel padrão novo **Médico regulador**, com
     `PACIENTE.CONSULTAR` e `REGULACAO.CONSULTAR`.
   - O escopo é o da hierarquia que já existe (ADR-0066): o regulador lotado no município regula as
     unidades abaixo dele. O solicitante responde pela unidade solicitante.
9. **Permissão nova em papel que já existe:** o catálogo de acesso só criava o que faltava. Agora,
   quando uma permissão nasce, ela também entra nos papéis padrão que já existiam e a preveem. Isso vale
   **só na criação**: o que a administração tirar depois continua tirado.
10. **Endpoints:**
    - `/api/v1/procedimento-regulado/`: POST, PATCH `{uuid}`, GET (com `?ativos=true`) e GET `{uuid}`.
    - `/api/v1/solicitacao-regulacao/`:
      - POST, GET (filtros `status`, `pacienteId`, `unidadeSolicitanteId`, `procedimentoId`), GET
        `fila` e GET `{uuid}`;
      - POST `{uuid}/complemento`, `/reclassificacao`, `/autorizacao`, `/devolucao`, `/negativa` e
        `/cancelamento`.
11. **Tela de Usuários & Perfis:** a matriz de permissões ganha o módulo Regulação, e a Auditoria ganha
    os rótulos dos dois recursos novos.

## Consequências

- A rede passa a ter fila única por procedimento, com prioridade e histórico de cada decisão.
- Até a ADR-0089, a autorização registra a vaga, mas ainda não cria o agendamento na unidade executante,
  e a unidade executante ainda não ganha vínculo com o paciente.
- Ninguém recebe `REGULACAO.REGULAR` automaticamente: o papel Médico regulador precisa ser concedido a
  alguém, no escopo do município ou da regional.

## Testes

- **JUnit** (`SolicitacaoRegulacaoControllerTest`, segurança desligada):
  - catálogo: cria, edita, recusa nome repetido e filtra ativos;
  - solicitação com CID normalizado, primeiro evento, posição e leitura auditada;
  - recusa de CID inválido, de procedimento fora de uso e de segunda solicitação em aberto;
  - fila por prioridade e hora do pedido, listagem sem dado clínico e reclassificação;
  - devolução e complemento na posição original;
  - autorização por outro regulador, recusa do solicitante e de data passada, cancelamento;
  - negativa.
- **JUnit** (`RegulacaoAutorizacaoControllerTest`, login e autorização ligados):
  - médico solicita na própria unidade e não na de outro município;
  - regulador do município vê e autoriza, e o de outro município não;
  - recepção acompanha sem o dado clínico;
  - permissão nova entra no papel existente só na criação.
- **Robot de API:** 23 casos em `test/procedimento_regulado/` e `test/solicitacao_regulacao/`, uma
  suíte por endpoint, com os schemas em `resource/schema/regulacao/`.
