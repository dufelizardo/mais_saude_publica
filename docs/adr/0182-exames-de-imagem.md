# 0182 — Laboratório e Diagnóstico: exames de imagem

## Status

Aceita e implementada. Fatia **L3** do plano para fechar o domínio 10 (Laboratório e Diagnóstico), descrito na
[ADR-0180](./0180-laboratorio-faixas-de-referencia.md):

- **L1:** faixas de referência por sexo e idade, valores críticos e evolução do exame ([ADR-0180](./0180-laboratorio-faixas-de-referencia.md));
- **L2:** ciência de quem pediu, comunicação do crítico e entrega ao paciente ([ADR-0181](./0181-laboratorio-ciencia-critico-entrega.md));
- **L3 (esta):** exames de imagem, com a realização e o laudo do radiologista;
- **L4:** laudo em PDF com assinatura eletrônica interna e os insumos de coleta baixados do Estoque.

As integrações de imagem não impedem esta fatia e entram na lista do domínio 20:

- PACS e DICOM, com a lista de trabalho DICOM (MWL);
- RIS;
- ligação automática da autorização da Regulação ao pedido.

## Contexto

- **O sistema só tinha exame laboratorial.** Raio-X, ultrassonografia, tomografia e mamografia não tinham pedido nem laudo.
- **O modelo operacional já previa a capacidade** `DIAGNOSTICO_POR_IMAGEM` ([ADR-0117](./0117-modelo-operacional-perfis-e-capacidades.md)), mas
  nenhum domínio a consultava.
- **O fluxo de imagem é outro:** não há amostra. O exame é realizado no serviço de imagem e laudado pelo médico radiologista, que assina o
  laudo. Não existe a liberação técnica do laboratório.
- **O que vem depois do resultado é o mesmo:** a ciência de quem pediu, o valor crítico (na imagem, o achado crítico), a entrega ao
  paciente e a presença no prontuário e no laudo impresso.

## Decisão

1. **Um catálogo só, com categoria** (`categoria` e `modalidade` em `TB_EXAME_LABORATORIAL`):
   - **Laboratorial** (padrão) ou **imagem**. A categoria decide o fluxo e **não muda depois de criado** (422).
   - **Imagem:** tem **modalidade** obrigatória (400): raio-X, ultrassonografia, tomografia, ressonância magnética, mamografia,
     densitometria óssea ou outra.
     - Não tem material, unidade nem faixa de referência: o resultado é sempre laudo.
     - O material deixa de ser obrigatório na tabela.
   - **Laboratorial:** continua exigindo material e tipo de resultado (400).
   - **Preparo e prazo** valem para as duas categorias.
   - **Quem mantém:** `LABORATORIO.GERENCIAR`, que passa a ser a gestão do catálogo de diagnóstico.
2. **Pedido único**, como a guia SADT: laboratório e imagem no mesmo pedido.
   - Com exame de imagem, o pedido informa o **serviço de imagem** (`unidade_executante_imagem_id`; 400 se faltar).
   - A unidade precisa ter a capacidade `DIAGNOSTICO_POR_IMAGEM` vigente (422), no padrão da
     [ADR-0170](./0170-modelo-operacional-dominios-consultam-capacidades.md).
   - Exame regulado (tomografia e ressonância, na maioria das redes) continua passando antes pela Regulação. O pedido nasce com a unidade
     que ela autorizou.
3. **Fluxo do exame de imagem:** solicitado → **realizado** → laudo **liberado**.
   - A coleta e as listas de coleta, análise e liberação ignoram os exames de imagem.
4. **Realização** (`TB_REALIZACAO_EXAME_IMAGEM`, uma por exame), com `IMAGEM.REALIZAR` no serviço de imagem do pedido (403 fora dele):
   - registra quando, o técnico, o equipamento, o contraste (sim ou não, e qual) e uma observação;
   - o equipamento é texto livre, porque não há cadastro de aparelhos no sistema;
   - validações: exame laboratorial ou já realizado dá 422; data no futuro dá 422; contraste sem descrição dá 400.
5. **Laudo**, com `IMAGEM.LAUDAR` no serviço de imagem (403 fora dele), gravado no resultado (`laudo_tecnica`, `laudo_achados`,
   `laudo_conclusao`, `imagem_url`):
   - **Conteúdo:** técnica (opcional), achados e conclusão (obrigatórios).
   - **Classificação:** **sem alterações**, **com alterações** ou **achado crítico** (400 para outra). Fica na interpretação, que ganha
     `ALTERADO` e `ACHADO_CRITICO`.
   - **Link das imagens:** opcional, só `https` (400). É o ponto de encaixe do PACS.
   - **Assinatura:** o radiologista escreve e assina num passo só, e o laudo sai liberado. Só exame realizado (422).
   - **Exibição:** a conclusão também vai no valor em texto, para o prontuário, o laudo impresso e as listas mostrarem o laudo sem mudança.
6. **Adendo:** corrige o laudo liberado, com motivo obrigatório (400).
   - Usa a mesma mecânica da retificação: grava um laudo novo ligado ao anterior, já liberado.
   - A retificação de laboratório recusa exame de imagem (422).
7. **O que vem pronto da L2:**
   - **Achado crítico** é crítico. Avisa quem pediu por e-mail e entra em "Para ciência" e em "Críticos a comunicar".
   - **Comunicação do achado crítico:** quem registra é o radiologista do serviço de imagem, e não quem libera no laboratório.
   - **Ciência e entrega** valem para o laudo. A entrega espera também os exames realizados sem laudo.
   - **Cancelamento:** vale até o laudo, também para quem realiza no serviço de imagem.
8. **Papéis e permissões** (semeados pelo catálogo de acesso, [ADR-0066](./0066-papeis-permissoes-e-escopo-por-unidade.md)):
   - permissões `IMAGEM.REALIZAR` e `IMAGEM.LAUDAR`, de acesso a dado de saúde;
   - **Técnico em radiologia:** `PACIENTE.CONSULTAR` e `IMAGEM.REALIZAR`;
   - **Médico radiologista:** `PACIENTE.CONSULTAR`, `IMAGEM.LAUDAR` e `PRONTUARIO.CONSULTAR`;
   - o pedido, a lista de trabalho e o detalhe passam a valer também para essas permissões, no escopo do serviço de imagem.

### API

- `POST /api/v1/pedido-exame/item/{id}/realizacao` (`IMAGEM.REALIZAR`);
- `POST /pedido-exame/item/{id}/laudo` e `/adendo` (`IMAGEM.LAUDAR`);
- `GET /pedido-exame/trabalho?etapa=PARA_REALIZAR` e `PARA_LAUDAR`;
- `unidadeExecutanteImagemId` no pedido; `categoria` e `modalidade` no catálogo; `realizacoes` no detalhe e o laudo no resultado.

### Telas

- **"Laboratório e imagem"** (antes "Laboratório"):
  - **Abas novas:** "Para realizar" e "Para laudar", conforme a permissão.
  - **Gavetas novas:** a realização, com o preparo e a indicação clínica; e o laudo, com o aviso do achado crítico e o adendo.
  - **Gaveta do pedido:** pede o serviço de imagem quando há exame de imagem marcado.
  - **Catálogo:** a categoria e a modalidade na gaveta do exame; a coluna "Material ou modalidade".
  - **Detalhe do pedido:** o serviço de imagem, a realização e os laudos com técnica, achados, conclusão e o link das imagens.
- **Laudo impresso e exames do paciente:** mostram a modalidade e os achados.
- **Usuários e perfis:** o grupo "Diagnóstico por imagem".

### Migração

`V67__exames_de_imagem.sql`:

- a categoria e a modalidade no catálogo, e o material opcional;
- o serviço de imagem no pedido;
- a tabela da realização;
- o laudo no resultado;
- os CHECKs da situação (`REALIZADO`), da interpretação (`ALTERADO`, `ACHADO_CRITICO`) e dos eventos (`REALIZACAO`, `LAUDO`, `ADENDO`).

## Alternativas descartadas

- **Domínio e telas separados para imagem:** duplicaria pedido, ciência, comunicação de crítico, entrega, prontuário e laudo impresso. O
  item do pedido com a categoria reaproveita tudo isso.
- **Laudo em duas etapas (registrar e liberar), como no laboratório:** o laudo é ato médico e o radiologista o assina. Não há o técnico que
  registra e o responsável que libera.
- **Dar as permissões de imagem ao gestor e ao administrador**, como dizia o desenho: o administrador da plataforma não lê dado de saúde
  ([ADR-0054](./0054-modelo-de-identidade-autorizacao-e-auditoria.md)), e o gestor também não tem as do laboratório. As permissões ficam
  com os papéis novos.
- **Equipamento como cadastro:** os "equipamentos de saúde" do sistema são as unidades. Um cadastro de aparelhos fica para quando houver
  manutenção e rastreio de dose, junto com o RIS.

## Consequências

- **Implantação aos poucos:** sem exame de imagem no catálogo, nada muda. A unidade de imagem precisa da capacidade antes do primeiro pedido.
- **A situação "Aguardando coleta"** do pedido também cobre o exame de imagem ainda não realizado. Na tela, o exame aparece como "Aguardando".
- **Ficam para depois:** dose de radiação e rastreio por aparelho, que dependem do RIS; e o laudo comparativo com exames anteriores.

## Testes

- **JUnit:**
  - `ExameImagemControllerTest`:
    - o catálogo com modalidade, sem material e com a categoria fixa;
    - o serviço de imagem com a capacidade;
    - a realização, o laudo e o adendo sem passar pela coleta;
    - o achado crítico avisando e entrando nos críticos a comunicar;
    - o exame realizado segurando a entrega e o cancelamento;
  - `LaboratorioAutorizacaoControllerTest`, com a segurança ligada: o técnico em radiologia realiza, e só o radiologista lauda, no serviço
    de imagem.
- **Robot:** `POST_exame_imagem.robot` (API, CT-001 a CT-008) e os casos CT-017 e CT-018 de `UI_laboratorio.robot`.
