# 0183 — Laboratório e Diagnóstico: laudo em PDF com assinatura eletrônica interna e insumos do Estoque

## Status

Aceita e implementada. Fatia **L4**, a última do plano para fechar o domínio 10 (Laboratório e Diagnóstico), descrito na
[ADR-0180](./0180-laboratorio-faixas-de-referencia.md):

- **L1:** faixas de referência por sexo e idade, valores críticos e evolução do exame ([ADR-0180](./0180-laboratorio-faixas-de-referencia.md));
- **L2:** ciência de quem pediu, comunicação do crítico e entrega ao paciente ([ADR-0181](./0181-laboratorio-ciencia-critico-entrega.md));
- **L3:** exames de imagem ([ADR-0182](./0182-exames-de-imagem.md));
- **L4 (esta):** laudo em PDF com assinatura eletrônica interna e os insumos de coleta e de realização baixados do Estoque.

Com a L4, o domínio 10 fica ✅. Seguem no domínio 20, sem impedir:

- certificado ICP-Brasil (assinatura qualificada);
- equipamentos (LIS, HL7, ASTM);
- PACS e RIS;
- LACEN;
- SIGTAP e BPA.

## Contexto

- **O laudo era uma página para imprimir pelo navegador**, com o aviso "sem assinatura digital". Nada ligava o papel ao registro no sistema.
- **A liberação aceitava qualquer matrícula:** com login, quem clicava podia liberar em nome de outro profissional.
- **O Estoque ([ADR-0177](./0177-estoque-e-almoxarifado.md)) não sabia o que o laboratório consumia:** tubo, agulha, contraste e seringa
  saíam sem registro.

## Decisão

1. **Assinatura eletrônica interna do resultado.** É uma assinatura avançada no sentido da Lei 14.063/2020: o profissional se autentica com
   a própria senha, e o sistema guarda a prova.
   - **Quando:** na liberação do laboratório, no laudo de imagem, na retificação e no adendo.
   - **Quem assina:** com login, a matrícula informada precisa ser a do profissional logado (403). Sem login, como na segurança desligada
     dos testes, vale a matrícula informada, como antes.
   - **O que se grava no resultado:**
     - `assinado_por_cpf`, o CPF de quem assinou;
     - `hash_conteudo`, o SHA-256 do texto canônico assinado: exame, paciente, valor ou laudo, referência, interpretação, quem liberou e
       quando, e o resultado corrigido, se houver.
   - **Assinatura qualificada:** o certificado ICP-Brasil é ponto de encaixe do domínio 20.
2. **Laudo em PDF gerado no servidor:** `GET /pedido-exame/{id}/laudo.pdf`.
   - **Bibliotecas:** OpenPDF (LGPL/MPL) para o documento e ZXing (Apache 2.0) só para a matriz do QR code. São as duas dependências novas
     do `pom`.
   - **Conteúdo:**
     - só os resultados liberados;
     - em cada um, quem assinou, quando e o início do hash;
     - o laudo de imagem com técnica, achados e conclusão.
   - **Rodapé:** o código de verificação, o endereço da verificação, o QR code com esse endereço e a versão.
   - **Acesso:** as mesmas regras do detalhe do pedido (403 fora do escopo), com leitura auditada. Nada liberado: 422.
3. **Emissões** (`TB_EMISSAO_LAUDO_EXAME`, com os resultados incluídos):
   - **O que cada emissão guarda:** o PDF como saiu, o hash dele, um **código de verificação** (`XXXX-XXXX-XXXX`, sem letras e números que
     se confundem), quem emitiu e quando.
   - **Baixar de novo sem resultado novo** devolve a **mesma** emissão e o mesmo arquivo, então o hash continua valendo.
   - **Com resultado novo, retificação ou adendo,** nasce outra **versão**, e a anterior fica **substituída**.
   - **Histórico:** `GET /pedido-exame/{id}/emissoes` traz versão, código, hash e qual é a vigente.
4. **Verificação pública, sem login:** `GET /api/v1/publico/laudo/{codigo}` e a página **"Verificar laudo"**, com link na página inicial.
   - **O que mostra:** se o laudo vale ou foi substituído (e qual é a versão vigente), a data, a unidade, os exames e o hash do PDF.
   - **Do paciente,** só as iniciais (LGPD).
   - **Código desconhecido:** 404.
5. **Insumos do exame** (`TB_INSUMO_EXAME`), mantidos com `LABORATORIO.GERENCIAR` em `PUT /exame-laboratorial/{id}/insumos`:
   - **O que se cadastra:** o material do Estoque e a quantidade.
   - **Só com contraste:** marcação que vale só para exame de imagem (422 no laboratorial).
   - **Validações:** material repetido dá 400; material fora de uso dá 422.
6. **Baixa automática dos insumos**, pelo sistema:
   - **Quando e onde:** na **coleta**, uma vez por amostra, no estoque da unidade da coleta; e na **realização** de imagem, no estoque do
     serviço de imagem. O contraste só sai quando a realização usou contraste.
   - **Na mesma amostra,** cada material conta uma vez, pela maior quantidade entre os exames: dois exames no mesmo tubo usam um tubo.
   - **Como sai:** pelo FEFO, como saída por consumo, com a origem na justificativa do livro. Não exige `ESTOQUE.MOVIMENTAR` de quem coleta.
   - **Recoleta** consome de novo, porque o consumo é real.
7. **Falta de saldo nunca bloqueia a coleta**, porque o paciente já foi puncionado:
   - sai o que houver;
   - o que faltou fica no evento do pedido e vira **consumo sem saldo** (`TB_CONSUMO_SEM_SALDO`);
   - o consumo sem saldo aparece nos alertas do Estoque até o almoxarife acertar a entrada ou o inventário e marcá-lo como **conferido**
     (`ESTOQUE.MOVIMENTAR`; já conferido dá 422).

### API

- `GET /api/v1/pedido-exame/{id}/laudo.pdf` (cabeçalho `X-Codigo-Verificacao`) e `GET /pedido-exame/{id}/emissoes`;
- `GET /api/v1/publico/laudo/{codigo}`, sem login;
- `PUT /api/v1/exame-laboratorial/{id}/insumos`; `insumos` no exame do catálogo;
- `consumosSemSaldo` no saldo do Estoque e `POST /api/v1/estoque/consumo-sem-saldo/{id}/conferencia`;
- `hashConteudo` no resultado.

### Telas

- **Laboratório e imagem:**
  - no detalhe do pedido, **"Baixar laudo (PDF)"**, ao lado da "Prévia para imprimir" (a página antiga), e a seção "Laudos emitidos", com
    versão, código, hash e a marca de vigente ou substituída;
  - na gaveta do exame, a seção "Insumos de coleta" (ou "de realização").
- **Estoque,** aba Alertas: "Consumo sem saldo", com o botão "Conferido".
- **Página pública "Verificar laudo"** (`/verificar-laudo`), aberta pelo QR code.

### Migração

`V68__laudo_pdf_assinado_e_insumos.sql`:

- a assinatura no resultado;
- as emissões e os resultados de cada uma;
- os insumos do exame;
- o consumo sem saldo.

## Alternativas descartadas

- **Gerar o PDF no navegador:** o arquivo mudaria a cada impressão, e o hash não provaria nada. Gerado e guardado no servidor, o PDF
  verificado é o mesmo que foi entregue.
- **Bloquear a coleta sem saldo:** inverte a prioridade, porque a coleta é ato assistencial e o estoque é registro. O alerta leva o problema
  a quem acerta o estoque.
- **Somar os insumos dos exames da mesma amostra:** contaria dois tubos para dois exames no mesmo tubo.
- **Guardar o resultado do laudo num conjunto:** o `hashCode` do resultado passa pelo item e volta ao resultado. A lista evita a recursão.

## Consequências

- **Laudos já emitidos** como página de impressão continuam válidos como estavam. A primeira emissão em PDF de cada pedido sai como versão 1.
- **Resultados liberados antes da V68** não têm hash e aparecem no PDF sem ele. Os novos já saem assinados.
- **O endereço do QR code** usa a URL do frontend configurada (`app.security.recuperacao-senha.url-frontend`). Sem ela, o PDF mostra só o
  caminho e o código.
- **Ficam para depois:**
  - código de barras do tubo;
  - custo do exame pelos insumos (Financeiro, #17);
  - assinatura qualificada com certificado (domínio 20).

## Testes

- **JUnit:**
  - `LaudoInsumosControllerTest`:
    - o PDF assinado com o código e a mesma emissão sem resultado novo;
    - a verificação pública com o hash e só as iniciais;
    - a versão substituída na retificação e o 404 do código desconhecido;
    - nada liberado dá 422;
    - a baixa de insumos uma vez por amostra, com o consumo sem saldo e a conferência;
    - o contraste só com contraste;
  - `LaboratorioAutorizacaoControllerTest`, com a segurança ligada:
    - a liberação com a matrícula de outro dá 403;
    - o PDF no escopo (a recepção e o outro laboratório recebem 403, e sem login 401);
    - a verificação pública sem login.
- **Robot:** `GET_laudo_pdf_insumos.robot` (API, CT-001 a CT-007) e os casos CT-019 a CT-021 de `UI_laboratorio.robot`.
