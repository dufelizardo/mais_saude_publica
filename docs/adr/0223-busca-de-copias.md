# 0223 — Busca de cópias públicas do projeto por marcadores

## Status

Aceita e implementada.

## Contexto

- **O repositório é público, sob a GPL-3.0.** A licença permite copiar, modificar e redistribuir, mas não avisa quando alguém faz
  isso, nem garante que a cópia cumpra as condições (manter a licença e o aviso de autoria, publicar o código das versões
  distribuídas).
- **O GitHub mostra só uma parte:** a aba Insights traz os clones e as visitas dos últimos 14 dias e a lista de forks. Uma cópia
  feita por download, ou subida como repositório novo, não aparece ali.
- **O que dá para fazer:** espalhar marcadores únicos ("canários") no projeto e procurá-los periodicamente na busca de código.
  Isso só acha cópias **públicas**; a cópia em repositório privado continua invisível.

## Decisão

1. **Cinco marcadores**, um por camada, no formato `msp-` seguido de 10 dígitos hexadecimais:
   - no backend, uma propriedade da configuração;
   - no frontend, uma meta tag da página;
   - nos testes Robot, um comentário do recurso comum;
   - no manual do usuário e no índice das ADRs, um comentário HTML (não aparece na página).

   Cada camada tem o seu marcador, e o achado diz o que foi copiado.
2. **A lista dos marcadores não fica no código:** ela é a variável `CANARIOS` do repositório (Settings › Secrets and variables ›
   Actions › Variables), uma linha por marcador, no formato `marcador=onde está`. Nem esta ADR nem o código dizem quais são,
   para quem copia não ter a lista do que apagar.
3. **Busca semanal** (`.github/workflows/busca-de-copias.yml`, toda segunda às 10h UTC, e manual pelo *Run workflow*):
   - o script `.github/scripts/buscar_copias.py` procura cada marcador na busca de código do GitHub;
   - descarta os achados deste repositório;
   - escreve o resumo no job;
   - havendo achado, abre a issue **"Possível cópia do projeto detectada"** com o rótulo `copia-detectada`, ou comenta na que já
     estiver aberta.
4. **Fora da automação, feito à mão quando houver suspeita:**
   - **Sourcegraph** e **Google Alerts** com o nome do produto e os marcadores;
   - **copydetect** ou **ScanCode** para comparar um repositório suspeito com este, mesmo com nomes trocados;
   - a aba **Insights › Traffic** e **Forks** do GitHub.

## Alternativas descartadas

- **Marcador visível no texto ou na tela:** chama a atenção e é o primeiro a ser apagado.
- **A lista dos marcadores no repositório:** facilitaria limpar a cópia.
- **Busca diária:** a busca de código aceita poucas consultas por minuto, e cópia não exige reação no mesmo dia.
- **Trocar a licença:** é outra decisão, do titular do projeto; esta ADR não muda a licença.

## Consequências

- **Uma cópia pública com os marcadores vira issue** em até uma semana depois de indexada pelo GitHub.
- **A cópia que apagar os marcadores, ou a privada, não é achada.** Os marcadores são uma rede, não uma garantia.
- **Mudar ou remover um marcador** exige atualizar a variável `CANARIOS`, ou a busca deixa de procurá-lo.
- **Pendências:** o aviso de autoria (`Copyright (C) <ano> <nome>`) e o campo da licença no `pom.xml` e no `package.json` estão
  vazios, e a escolha entre continuar na GPL-3.0 ou passar à AGPL-3.0 é do titular.

## Testes

- **O script, local:** com um termo comum, lista os achados de fora e descarta este repositório; com a variável vazia, não busca
  nada.
- **O workflow:** rodado à mão depois do merge, com a variável `CANARIOS` preenchida.
