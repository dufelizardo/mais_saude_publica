# 0174 — Comunicação e Educação em Saúde: campanhas de comunicação (C2)

## Status

Aceita e implementada. Fatia C2 do domínio 28, desenhada na [ADR-0114](./0114-comunicacao-e-educacao-em-saude.md) e nas seções 4, 5 e 9 de
[`comunicacao/MODELO-COMUNICACAO.md`](../comunicacao/MODELO-COMUNICACAO.md).

## Contexto

- **Já existiam as peças soltas:** o comunicado, o conteúdo revisado, os canais e a publicação com resultado (C1,
  [ADR-0168](./0168-comunicacao-basica.md)); e a `Campanha` operacional de Programas ([ADR-0133](./0133-programas-campanhas.md)).
- **Faltava o que amarra tudo,** como o Outubro Rosa: objetivo, público, territórios, conteúdos, materiais e comunicados sob uma campanha.
- **Dois nomes:** a deste domínio é `CampanhaComunicacao`, para não confundir com a operacional (ADR-0114).

## Decisão

1. **Campanha de comunicação** (`TB_CAMPANHA_COMUNICACAO`): nome, objetivo, descrição, período, temas, público-alvo (uma classificação,
   faixa etária, territórios e unidade; sem território, a rede), canais planejados, e programa e campanha operacional opcionais.
   - Com só a campanha operacional, o programa vem dela; com os dois e de programas diferentes: 422.
   - Território ou canal inativo: 422. Portal para público de profissionais: 422.
2. **Ciclo:** planejada → em andamento → encerrada; planejada ou em andamento → cancelada, com o motivo.
   - Só a planejada se edita (422).
   - Iniciar pede ao menos um canal e o período aberto (422).
   - O encerramento leva o balanço, que alimenta a avaliação da C6.
3. **Conteúdos da campanha** (`TB_CAMPANHA_COMUNICACAO_CONTEUDO`), pelo grupo de versões, como na C4
   ([ADR-0173](./0173-educacao-permanente.md)): só os revisados (422), sempre na versão em vigor.
4. **Material educativo** (`TB_MATERIAL_EDUCATIVO`): conteúdo, formato (cartilha, folder, cartaz, infográfico, vídeo, áudio, peça para
   redes sociais, outro), título e descrição. **Sem arquivo até a D2 de Documentos;** por ora, link externo opcional, só `https` (400).
   Material retirado sai com o motivo.
5. **Comunicado da campanha:** o comunicado ganha a campanha de origem, opcional. A campanha precisa estar planejada ou em andamento, e o
   período do comunicado caber no dela (422). A campanha soma as publicações dos comunicados por canal: publicações, enviados e falhas.
6. **Portal:** a campanha em andamento, no período, com o Portal entre os canais e público que não é de profissionais aparece na página
   pública, com os conteúdos publicados e os materiais. Nenhum dado de pessoa.
7. **Sem permissão nova:** `COMUNICACAO.PUBLICAR` para as campanhas, na unidade quando houver; `CONTEUDO.EDITAR` para os materiais.

### API

- `GET, POST /api/v1/comunicacao/campanha`, `PATCH /campanha/{uuid}`, e `POST /campanha/{uuid}/inicio`, `/encerramento` e `/cancelamento`;
- `GET /api/v1/comunicacao/material?conteudoGrupoId=`, `POST /material` e `POST /material/{uuid}/retirada`;
- `campanhaId` no comunicado;
- `GET /api/v1/publico/comunicacao/campanhas`, sem login.

### Telas

- **Comunicação:** aba Campanhas (lista, gaveta de cadastro, detalhe com conteúdos, comunicados, alcance e as ações); a gaveta do comunicado
  ganha a campanha; cada conteúdo, a gaveta Materiais.
- **Página pública:** seção Campanhas de saúde em andamento.

### Migração

`V59__campanhas_de_comunicacao.sql`: as tabelas da campanha, o material educativo e a coluna da campanha no comunicado.

## Consequências

- **O alcance é o das publicações,** não o de pessoas: medir participação e cobertura é a C6, com Indicadores.
- **Ficam para depois:** arquivos dos materiais (D2); público por dado de saúde e envio a pessoa identificada, com consentimento (C5); ação
  educativa nas UBS (C3); a campanha mostrada na tela de Programas.

## Testes

- **JUnit:** `CampanhaComunicacaoControllerTest` (temas sem repetir e o conteúdo em vigor; validações; o ciclo; o comunicado da campanha no
  alcance; o portal com os materiais; o material com link https e a retirada) e `CampanhaComunicacaoAutorizacaoControllerTest`, com a
  segurança ligada (sem login, o médico, outra unidade, o gestor e o portal sem login).
- **Robot:** `CampanhasComunicacao.robot` (API, cinco casos) e o CT-004 de `UI_comunicacao.robot`.
