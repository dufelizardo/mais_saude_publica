# 0181 — Laboratório: ciência do solicitante, comunicação do crítico e entrega ao paciente

## Status

Aceita. Fatia **L2** do plano para fechar o domínio 10 (Laboratório e Diagnóstico), descrito na
[ADR-0180](./0180-laboratorio-faixas-de-referencia.md):

- **L1:** faixas de referência por sexo e idade, valores críticos e evolução do exame ([ADR-0180](./0180-laboratorio-faixas-de-referencia.md));
- **L2 (esta):** aviso a quem pediu o exame, comunicação do valor crítico pelo laboratório e entrega do resultado ao paciente;
- **L3:** exames de imagem, com o laudo do radiologista;
- **L4:** laudo em PDF com assinatura eletrônica interna e os insumos de coleta baixados do Estoque.

O envio eletrônico ao paciente (e-mail, SMS, Meu SUS Digital ou RNDS) **não impede esta fatia**. Ele pede consentimento, porque resultado
de exame é dado sensível (LGPD), e fica previsto na C5 de Comunicação ([ADR-0114](./0114-comunicacao-e-educacao-em-saude.md)) e no
domínio 20.

## Contexto

- **Ninguém era avisado da liberação.** O médico só via o resultado se abrisse o prontuário ou o pedido. Com valor crítico era igual.
- **A L1 trouxe o valor crítico** (Crítico baixo e Crítico alto), mas nada acontecia quando ele aparecia.
- **A RDC 786/2023 da Anvisa** pede que o laboratório comunique o valor crítico a quem responde pelo paciente e registre quem foi avisado,
  quando e por quem.
- **Não havia registro de entrega** do resultado ao paciente: nem quem retirou nem se foi segunda via.

## Decisão

1. **Ciência de quem pediu**, gravada no resultado (`ciencia_por_id`, `ciencia_em` em `TB_RESULTADO_EXAME`):
   - Todo resultado liberado nasce **pendente de ciência** do profissional solicitante do pedido.
   - **Só quem pediu** registra a ciência (422 para outro profissional). Com login, é sempre o profissional logado: informar outra
     matrícula dá 403. Sem login (segurança desligada), a matrícula vem no pedido, como nas demais ações do laboratório.
   - **Uma vez por resultado** (409). A retificação grava outro resultado, então a ciência **volta a ficar pendente**: o médico precisa ver
     o valor corrigido.
   - **Pelo item ou pelo pedido inteiro.** Pelo pedido, entram só os liberados sem ciência; nenhum pendente dá 409.
   - **Lista "Para ciência":** críticos primeiro, depois do mais antigo ao mais novo. Mostra a interpretação, mas não o valor, que fica no
     detalhe do pedido, com leitura auditada (ADR-0093).
2. **Comunicação do valor crítico** (`TB_COMUNICACAO_CRITICO_EXAME`), com `LABORATORIO.LIBERAR` no laboratório da amostra:
   - **O que se registra:** quem recebeu a informação (nome e função), o meio (telefone, presencial ou mensagem no sistema), quando e quem
     comunicou.
   - **Regras:** só resultado crítico liberado (422); uma comunicação por resultado (409); nem no futuro nem antes da liberação (422).
   - **Lista "Críticos a comunicar":** críticos liberados sem comunicação e sem ciência, nos laboratórios do escopo. A ciência do
     solicitante também tira o crítico da lista, porque mostra que o aviso chegou.
   - **E-mail a quem pediu** quando um crítico é liberado ou retificado para crítico, no padrão do aviso da auditoria
     ([ADR-0096](./0096-alertas-da-auditoria.md)). O e-mail não traz nome do paciente, exame nem valor, só o aviso e o link da aba. Sem SMTP
     ou sem e-mail no cadastro, nada sai e a liberação não falha.
3. **Entrega do resultado ao paciente** (`TB_ENTREGA_RESULTADO_EXAME`):
   - **Quem pode:** quem coleta (`LABORATORIO.COLETAR`) ou quem pede exames (`EXAME.SOLICITAR`) **na unidade da entrega** (403).
   - **Quando:** sem exame em andamento e com ao menos um liberado (422). Cancelado não impede.
   - **O que se registra:** quem retirou (o próprio paciente ou outra pessoa, com nome e documento obrigatórios, 400, e parentesco
     opcional), a forma (impresso na unidade ou entregue na consulta), a unidade, quem entregou e quando.
   - **Segunda via** é outra entrega, marcada no histórico.
   - **Crítico sem ciência não bloqueia a entrega:** a tela avisa, para a recepção orientar o paciente a procurar a unidade.
4. **Histórico:** os eventos `COMUNICACAO_CRITICO`, `CIENCIA` e `ENTREGA` entram em `TB_EVENTO_EXAME`, imutáveis como os demais.
5. **Telas do Laboratório:**
   - **"Para ciência"** (quem pede exames): com login, a lista do profissional logado; sem login, pela matrícula informada. Ações "Ver
     pedido" e "Ciente". Crítico destacado na linha.
   - **"Críticos a comunicar"** (quem libera): ações "Ver pedido" e "Comunicar", que abre a gaveta do registro.
   - **Detalhe do pedido:** a ciência em cada resultado, as seções "Comunicação de críticos" e "Entregas ao paciente", e os botões
     "Registrar ciência" e "Registrar entrega".

### API

- `GET /api/v1/pedido-exame/para-ciencia?matricula=` e `POST /pedido-exame/item/{id}/ciencia`, `POST /pedido-exame/{id}/ciencia`
  (`EXAME.SOLICITAR`);
- `GET /pedido-exame/criticos-a-comunicar` e `POST /pedido-exame/item/{id}/comunicacao-critico` (`LABORATORIO.LIBERAR`);
- `POST /pedido-exame/{id}/entrega` (`LABORATORIO.COLETAR` ou `EXAME.SOLICITAR`);
- o detalhe do pedido ganha `comunicacoesCritico` e `entregas`, e o resultado ganha a ciência.

### Migração

`V66__laboratorio_ciencia_comunicacao_entrega.sql`: as colunas da ciência no resultado, as duas tabelas e o CHECK dos tipos de evento.

## Alternativas descartadas

- **Ciência no item do pedido**, como no primeiro desenho: o item continua o mesmo depois da retificação, então seria preciso apagar a
  ciência à mão. No resultado, a retificação já nasce sem ciência.
- **Central de notificações nova:** o aviso fica nas listas de trabalho que cada perfil já usa, mais o e-mail no crítico. Uma central
  geral é decisão de outro domínio.
- **Bloquear a entrega até a ciência do crítico:** deixaria o paciente sem o papel na mão enquanto o médico não abre o sistema. O aviso na
  tela e a lista de críticos a comunicar cuidam do risco.
- **Marcar a entrega no laudo impresso:** o laudo é o documento entregue; quem entregou e para quem fica no histórico do pedido.

## Consequências

- **Implantação aos poucos:** os resultados liberados antes da V66 aparecem como pendentes de ciência na lista de quem pediu.
- **Ficam para depois:** envio eletrônico ao paciente, com consentimento (C5 e domínio 20); prazo de liberação com alerta de atraso, que
  combina com a L4 ou com Indicadores e BI.

## Testes

- **JUnit:**
  - `CienciaEntregaExameControllerTest`: só quem pediu dá ciência, uma vez, e a retificação reabre; a ciência do pedido inteiro só nos
    liberados; crítico primeiro, o e-mail sem dado do paciente e a comunicação com as validações; a ciência tira o crítico da lista do
    laboratório; a entrega só com tudo liberado, o terceiro identificado e a segunda via; pedido sem liberado não tem entrega;
  - `LaboratorioAutorizacaoControllerTest`, com a segurança ligada: a lista e a ciência do profissional logado, a lista de críticos só
    para quem libera e a entrega só na unidade de quem coleta ou pede.
- **Robot:** `POST_ciencia_entrega_exame.robot` (API) e os casos CT-014 a CT-016 de `UI_laboratorio.robot`.
