# 0122 — Saúde Mental: rede de atenção (SM4)

## Status

Aceita e implementada (fatia SM4 da [ADR-0112](./0112-saude-mental.md), sobre o caso das ADRs
[0120](./0120-saude-mental-base.md) e [0121](./0121-saude-mental-pts-intervencoes.md)).

## Contexto

- **Na RAPS, o cuidado anda pela rede:** UBS → CAPS → urgência → hospital → retorno ao CAPS ou à UBS. O caso da SM1
  ficava preso à unidade e à equipe de referência.
- **A rede já tem peças no sistema:** a Regulação do acesso, com referência e contrarreferência (ADRs 0087 a 0089),
  e as equipes com o apoio matricial da eMulti às eSF e eAP (ADR-0103).
- **A ADR-0112 manda reaproveitar:** não criar uma "regulação de saúde mental" nem um motor próprio de matriciamento.

## Decisão

1. **Encaminhamento pela Regulação** (`EncaminhamentoSaudeMental`, migração V16):
   - Uma ligação, no domínio de Saúde Mental, entre o caso e uma `SolicitacaoRegulacao`. A Regulação não ganha
     campo de saúde mental.
   - **Encaminhar daqui** cria a solicitação pelo `SolicitacaoRegulacaoService.solicitar`, com as regras e a
     permissão da Regulação (`REGULACAO.SOLICITAR` na unidade do caso). O médico encaminha; o psicólogo não, como
     hoje. Sem a matrícula, o solicitante é o profissional do usuário.
   - **Vincular** uma solicitação existente do mesmo paciente (de outro, 422; já ligada, 409).
   - **A contrarreferência é a da Regulação** (registrada no desfecho, ADR-0089). O caso mostra procedimento,
     situação, executante, data prevista e contrarreferência.
   - **Os procedimentos vêm do catálogo que a Regulação mantém:** sem semente nova.
2. **Cuidado compartilhado** (`CompartilhamentoCuidado`):
   - Com outra equipe, de qualquer unidade, tipicamente a eSF do paciente: objetivo e início/fim com motivo, como
     histórico.
   - **Compartilhar o cuidado é compartilhar o PTS.** Enquanto vigente, os membros da equipe parceira têm vínculo
     assistencial (regra 6 do `VinculoAssistencialService`) e o conteúdo do caso liberado; encerrar corta o acesso.
   - A própria equipe de referência: 422; compartilhamento vigente repetido: 409.
3. **Matriciamento:**
   - Tipo novo no catálogo de intervenções: "Discussão de caso / apoio matricial" (`MATRICIAMENTO`).
   - Nesse tipo, a equipe matriciada é obrigatória (400). O apoio da ADR-0103 precisa existir entre ela e a equipe
     de referência, em qualquer sentido (422). Nos outros tipos, não se informa equipe matriciada (400).
4. **Transferência do cuidado** (`TransferenciaCuidado`), em duas etapas:
   - **Proposta** pela origem, com a unidade de destino (outra unidade; a mesma: 422) e o resumo de contrarreferência
     obrigatório. Uma pendente por caso (409). Com transferência pendente, o caso não é encerrado (422).
   - **Aceite** pelo destino, com `SAUDE_MENTAL.ACOMPANHAR` lá:
     - o caso de origem sai com o motivo `TRANSFERENCIA`, fechando a referência e o programa;
     - o caso novo abre no destino, com a equipe e o técnico escolhidos, o programa inscrito lá (o destino precisa
       oferecer o programa Saúde Mental, 422) e o vínculo ao caso anterior (`casoAnterior`);
     - a rede de apoio vigente é copiada; o **PTS não é copiado**: o destino elabora o seu a partir do resumo;
     - **o caso anterior fica legível para a referência do caso novo.**
   - **Recusa** pelo destino, com motivo: o caso continua na origem. **Cancelamento** pela origem enquanto pendente.
   - Quem aceita no destino não tem a permissão na unidade de origem: o encerramento do programa pela Saúde Mental
     passa por um caminho interno do `AcompanhamentoService`, sem a conferência da unidade (quem chama já conferiu).
5. **Sem permissão nova.** Escrever na rede pede `SAUDE_MENTAL.ACOMPANHAR` e o conteúdo do caso liberado (como na
   SM2); aceitar e recusar pedem a permissão no destino.
6. **API** (`/api/v1/saude-mental/`):
   - `POST acompanhamento/{id}/encaminhamento`;
   - `POST acompanhamento/{id}/compartilhamento` e `POST acompanhamento/{id}/compartilhamento/{c}/encerramento`;
   - `POST acompanhamento/{id}/transferencia`;
   - `POST transferencia/{id}/aceite`, `/recusa` e `/cancelamento`;
   - `GET transferencias`: pendentes que chegam e que saem, no escopo (leitura auditada, com o resumo);
   - **o detalhe do caso** traz os encaminhamentos, os compartilhamentos, as transferências e o caso anterior.
7. **Tela:**
   - **Aba "Rede de atenção" no caso:** encaminhamentos (encaminhar ou vincular), cuidado compartilhado (incluir e
     encerrar), transferência (propor, cancelar, abrir o caso no destino) e o link para o caso anterior.
   - **Painel "Transferências do cuidado aguardando a sua unidade"** acima da lista, com Aceitar e Recusar.
   - **Na gaveta de intervenção,** a equipe matriciada quando o tipo é matriciamento, entre as equipes com apoio da
     referência.

## Consequências

- **O caso anda pela rede sem perder a história:** a transferência fecha a origem e abre o destino ligados, e a nova
  referência lê o caso anterior.
- **A eSF participa do cuidado sem virar referência:** o compartilhamento dá o acesso enquanto durar.
- **Fora do escopo:** regulação de urgência e SAMU (SM3 ou Regulação); instituições externas e encaminhamento
  intersetorial (SM6); cópia do PTS na transferência.

## Testes

- **JUnit:**
  - `RedeSaudeMentalControllerTest`: encaminhar (400 sem os dados) e vincular (outro paciente 422, repetido 409);
    compartilhamento como histórico (referência 422, repetido 409, encerrado 422); matriciamento (400 sem equipe, 422
    sem apoio, 201 com o apoio cadastrado, 400 em outro tipo); transferência com aceite (programa no destino, caso
    anterior, rede de apoio copiada, origem encerrada por transferência, encerrar com pendente 422); recusa e
    cancelamento.
  - `SaudeMentalAutorizacaoControllerTest`: o compartilhamento dá e corta o acesso da equipe parceira; aceitar sem a
    permissão no destino é 403; o caso anterior fica legível para a referência do caso novo.
- **Robot de API** (`test/saude_mental/SaudeMentalRede.robot`): 7 casos.
- **Robot de interface** (`UI_saude_mental.robot`): mais 4 casos — encaminhar, compartilhar, propor a transferência e
  aceitar pelo painel.
