# 0168 — Comunicação e Educação em Saúde: comunicação básica (C1)

## Status

Aceita e implementada. Fatia C1 do domínio 28, desenhada na [ADR-0114](./0114-comunicacao-e-educacao-em-saude.md) e em
[`comunicacao/MODELO-COMUNICACAO.md`](../comunicacao/MODELO-COMUNICACAO.md).

## Contexto

- **A gestão não tinha como avisar:** UBS em obra, serviço suspenso, orientação epidemiológica.
- **Orientação de saúde vencida no ar é risco:** conteúdo pede revisão técnica, validade e versão.
- **Dois canais já existem:** a landing page e o envio de e-mail da ADR-0081.
- **Não há registro de consentimento nem de descadastro,** que a ADR-0114 exige para pessoa identificada.

## Decisão

1. **Canais** (`TB_CANAL_COMUNICACAO`): catálogo com tipo, nome e ativo. Carga: Portal e E-mail. Só esses dois têm envio; os demais
   registram a publicação como não enviada até a C5. O tipo não muda (422), porque decide como o canal envia.
2. **Comunicado** (`TB_COMUNICADO`, `COMUNICACAO.PUBLICAR`): título, mensagem, prioridade (normal, alta, emergencial), público,
   período, unidade opcional e canais.
   - Rascunho → publicado → encerrado, com o motivo. Publicado não se edita (422).
   - Fim antes do início (400); sem canal, não publica (400); período já terminado, não publica (422).
   - Fora do período, sai do portal sozinho: a consulta olha a data, sem rotina.
   - **O emergencial pede também `EMERGENCIA.COORDENAR`** para publicar (403), como previsto na ADR-0114.
3. **Conteúdo educativo** (`TB_CONTEUDO_EDUCATIVO`): título, resumo, texto, tema, público, faixa etária, validade e versão.
   - Rascunho → em revisão → aprovado → publicado. O revisor devolve com o motivo, e o conteúdo volta ao rascunho.
   - **Quem escreveu não aprova o próprio conteúdo** (422).
   - Só o rascunho se edita (422). Vencido não publica (422) e sai do portal sozinho; aparece como expirado.
   - **Versão:** a partir do publicado, cria-se a seguinte (uma em andamento por conteúdo, 409). Ao ser publicada, a anterior fica
     substituída. As versões dividem o `grupoId`.
4. **Publicação** (`TB_PUBLICACAO`, só inclusão): o que saiu, em qual canal, quando e com qual resultado (publicada, enviada,
   parcial, não enviada), com a contagem de enviados e de falhas.
5. **Público e canal:** a população só pelo portal (422 no e-mail); os profissionais fora do portal, que é público (422).
6. **E-mail só para profissionais,** pelo cadastro: os lotados na unidade ou, sem unidade, os das unidades do acesso de quem publica.
   Sem SMTP configurado ou sem destinatário, a publicação não falha: o resultado fica como não enviada, com o motivo.
7. **Privacidade:** não há destinatário por dado de saúde. A mensagem é texto livre; a tela lembra de não citar pessoa nem condição
   de saúde.
8. **Permissões:** `COMUNICACAO.PUBLICAR`, `CONTEUDO.EDITAR` e `CONTEUDO.REVISAR`, acrescentadas a Gestor e Administrador da
   plataforma.

### API

- `/api/v1/comunicacao/canal`, `/comunicado`, `/conteudo`, com as ações `publicacao`, `encerramento`, `envio-revisao`, `aprovacao`,
  `devolucao` e `nova-versao`;
- `GET /api/v1/publico/comunicacao/avisos` e `/orientacoes`: sem login e sem dado de pessoa.

### Telas

- **Comunicação**, em Administrativo: abas Comunicados, Conteúdos educativos e Canais, com o histórico de publicação de cada item.
- **Página pública:** seções Avisos e Orientações de saúde abaixo das campanhas; aviso emergencial em destaque no topo.

### Migração

`V54__comunicacao_basica.sql`: as tabelas e a carga dos dois canais.

## Consequências

- **A comunicação de risco à população,** que a ADR-0113 deixou de fora, tem onde acontecer: o comunicado emergencial.
- **Comunicado sem unidade vale para a rede,** e quem tem a permissão em uma só unidade também pode criá-lo. A trilha registra quem
  publicou; restringir por nível fica como decisão futura, se o uso pedir.
- **Ficam para depois:** comunicado sugerido por outros domínios; público por território, equipe ou faixa etária; materiais (C2);
  SMS, WhatsApp, notificação e e-mail à população, com consentimento e descadastro (C5).

## Testes

- **JUnit:**
  - `ComunicacaoControllerTest`: a carga; o canal repetido e o tipo que não muda; o comunicado no portal, publicado de novo,
    editado e encerrado; fora do período; o emergencial primeiro; o público e o canal; as datas, a falta de canal e o período
    terminado; sem SMTP; a unidade no aviso; o conteúdo só depois da revisão; a devolução; o vencido; a nova versão substituindo a
    anterior; o publicado sem edição;
  - `ComunicacaoAutorizacaoControllerTest`, com a segurança ligada: as permissões na unidade, o emergencial com a coordenação da
    emergência, quem escreveu sem aprovar e o portal sem login.
- **Robot:** `Comunicacao.robot` (API, seis casos) e `UI_comunicacao.robot` (comunicado até a página pública, revisão do conteúdo e
  encerramento).
