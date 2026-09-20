# Escopo do domínio de RH — roadmap e ideias

**Data:** 2026-09-19 (criado) · atualizado em 2026-09-19 após conclusão do backend
**Status:** Vivo. **Backend (Fases 0-9) implementado e mergeado em `developer`** — ver a tabela de
PRs em [MODELO-RH.md](./MODELO-RH.md#0-estado-de-implementação-backend). Frontend em andamento, ver
seção 6. Nenhuma fase foi promovida a `qaa`/`homologacao`/`main` ainda (regra do módulo completo,
seção 5).

## 1. Contexto e como ler este documento

Este documento nasceu de uma proposta gerada por outra IA (DeepSeek), trazida pelo usuário e
revisada em conversa nesta sessão. O material original propunha 9 documentos completos (um por
subdomínio de RH), cada um no formato de ADR, com regras de negócio, endpoints, entidades e
estimativas de prazo já "fechadas".

Na revisão em conjunto, ficou combinado que:

- O material original **não foi gerado com conhecimento do código real** deste projeto — chegou a
  descrever o endpoint de desligamento com um contrato errado (`{nome}` em vez de `{cpf}`,
  ignorando o `dataDesligamento` da [ADR-0017](../adr/0017-numero-de-matricula-automatico-e-cpf-nao-unico.md)).
  Serve como **checklist de vocabulário e lacunas**, não como especificação literal.
- **Os números não foram validados.** Percentuais de INSS/FGTS/IRRF, prazos de aviso prévio,
  regras de cálculo de rescisão — tudo isso é conhecimento genérico de CLT reescrito em tabela,
  sem revisão de um profissional de RH/contabilidade qualificado. Calcular folha/rescisão errado
  tem risco financeiro e trabalhista real. **Nada disso deve virar código sem essa revisão.**
  Alternativa mais segura, se algum desses cálculos for necessário antes de termos essa revisão: o
  sistema registra o valor já calculado por um profissional habilitado, em vez de calcular
  internamente.
- O projeto segue avançando por **incrementos pequenos e discutidos** (como foi feito com
  Profissional: matrícula, endereço, telefone, ViaCEP, desligar — cada um sua própria conversa e
  PR), não adotando o roadmap inteiro de uma vez.
- O endpoint `/api/v1/profissional/` **não será reestruturado** para baixo de um prefixo `/rh/`. Já
  está em produção (frontend, testes Robot, ADRs dependem dele) e REST convenciona nomear pelo
  recurso, não pelo departamento que o gerencia. "RH" fica como nome do domínio/documentação (esta
  pasta), não do path da API — se algum dia existirem outros recursos reais de RH (licença,
  afastamento, folha), decide-se o prefixo deles quando forem construídos de verdade.

## 2. Padrão de modelagem confirmado: dados temporais viram histórico

Qualquer dado do profissional que **muda ao longo do tempo** — salário, lotação (unidade em que
trabalha), cargo/promoção, jornada — deve ser modelado como um **registro histórico com data**, não
como um campo mutável que se sobrescreve.

Mesmo raciocínio já aplicado na ADR-0017: recontratação gera uma ficha nova (matrícula nova), não
sobrescreve a ficha antiga. A ideia é que perder a "foto anterior" desses dados quebra tanto
auditoria quanto cálculos futuros que dependem do valor vigente em cada período (ex.: 13º e férias
proporcionais dependem de quanto a pessoa ganhava em cada mês do ano, não só do valor atual).

## 3. Lacunas já identificadas no `Profissional` atual

Achadas nesta conversa (não no documento do DeepSeek):

- **Sem campo de salário.** Não existe nenhum registro de remuneração hoje.
- **Sem vínculo geral de lotação.** O único vínculo profissional↔unidade que existe é
  `UnidadeDeSaude.responsavel` ([ADR-0014](../adr/0014-modulo-profissional-rh-com-vinculo-fraco-por-reconciliacao.md)),
  que é especificamente o papel de *responsável pela unidade* — não "em qual unidade este
  profissional trabalha" no dia a dia.
- **Sem suporte a transferência, promoção ou reajuste** — consequência direta das duas lacunas
  acima: sem um histórico de lotação/cargo/salário, não há o que "transferir" ou "reajustar", só
  sobrescrever.
- **Sem registro de benefícios.** Vale transporte, vale alimentação/refeição, plano de saúde,
  plano odontológico, seguro de vida, cesta básica — nenhum desses é rastreado hoje, apesar de
  toda empresa oferecer ou compartilhar o custo de algum deles.

## 4. É um ecossistema, não itens isolados

Importante não perder de vista: os subdomínios abaixo se alimentam uns dos outros — Ponto abastece
Folha, Afastamentos e Licenças se sobrepõem, Desligamento fecha o ciclo que Recrutamento abre. Não
são features independentes que dá pra construir em qualquer ordem sem pensar nas dependências.

### Fases dos 9 subdomínios originais (ordem revisada com o usuário)

| Fase | Subdomínio | Justificativa |
|---|---|---|
| 1 | **Afastamentos** | Já iniciado (documento + discussão desta sessão), base pros demais |
| 2 | **Ponto** | Obrigatório por lei, base pra folha |
| 3 | **Licenças** | Complementa afastamentos |
| 4 | **Desligamento** (com cálculo de rescisão) | Necessário pro ciclo de vida do profissional |
| 5 | **Folha de pagamento** | Consolida tudo (ponto + afastamentos + licenças + salário vigente) |
| 6 | **Treinamento/Certificações** | Qualidade e conformidade |
| 7 | **SST** | Segurança do trabalho |
| 8 | **Recrutamento** | Início do ciclo (para os próximos profissionais) |
| 9 | **Avaliação de desempenho** | Desenvolvimento |

**Todas as 9 fases acima, mais a Fase 0 (Lotação/Salário/Benefícios), já foram implementadas no
backend** — ver a tabela de PRs em
[MODELO-RH.md](./MODELO-RH.md#0-estado-de-implementação-backend). Esta tabela documenta a ordem
*planejada*; os números/regras trabalhistas seguem sujeitos ao aviso da seção 1 (campos de registro,
não cálculo automático, onde aplicável).

### Pré-requisitos identificados nesta sessão (não estavam na lista original)

As lacunas da seção 3 (**Lotação**, **Salário** e **Benefícios**) não apareciam no documento
original, mas o ecossistema acima depende delas silenciosamente: Ponto (fase 2) precisa saber a
jornada associada a onde o profissional está lotado; Folha (fase 5) precisa do salário vigente e
dos benefícios ativos pra calcular qualquer coisa. Por isso ficam como **Fase 0 — pré-requisito**,
antes da Fase 1 valer a pena de verdade:

| Subdomínio | Ideia central | Alimenta |
|---|---|---|
| **Lotação** | Vínculo profissional↔unidade com histórico (data início/fim), suporta transferência | Ponto (fase 2), Escala (fora desta lista, mas do mesmo tipo) |
| **Salário** | Derivado de tabela por cargo/categoria (não um valor solto por profissional) — dissídio reajusta o cargo inteiro, mudança de cargo já traz o valor novo junto, anuênio é regra automática por tempo de serviço (ver [MODELO-RH.md](./MODELO-RH.md#2-fase-0--lotação-cargo-salário-e-benefícios-pré-requisitos)) | Folha (fase 5), Desligamento/rescisão (fase 4) |
| **Benefícios** | Remuneração indireta (VT, VR/VA, plano de saúde, odontológico, seguro de vida, cesta básica) — catálogo + tabela de valores por data + adesão do profissional, com custeio próprio (empresa/compartilhado/profissional) | Folha (fase 5) |

## 5. Próximo passo planejado

**Lotação primeiro** (Fase 0), pelos motivos da seção 4 acima — sem ela, nem Ponto nem Escala têm
onde ancorar a jornada. Depois **Salário e Benefícios** (também Fase 0), mesmo padrão de histórico,
validado com Lotação antes de mexer em algo com dinheiro envolvido. A partir daí, seguir a ordem de
fases 1-9 combinada nesta seção — cada fase, na sua vez, merece sua própria conversa e plano, não
uma adoção em bloco.

## 6. Estado do frontend

Backend completo (seção acima); frontend construído fatia por fatia, seguindo a ordem da
"Especificação Funcional das Telas do Domínio de RH" (documento trazido pelo usuário, não anexado
a este repositório — ver [ADR-0018](../adr/0018-app-shell-e-decisoes-de-frontend-do-modulo-rh.md)
pra como ele orientou a análise de lacunas e a ordem das fatias).

| Fatia | Escopo | PR(s) | Status |
|---|---|---|---|
| — | Correção: cadastro de profissional (pré-existente, fora das fatias de RH) passa a vincular cargo e unidade via `Lotacao` — sem isso não havia de onde derivar salário nenhum (ver [ADR-0019](../adr/0019-vincular-cargo-e-lotacao-no-cadastro-de-profissional.md)) | [#141](https://github.com/dufelizardo/mais_saude_publica/pull/141) | ✅ |
| App shell | Navegação (sidebar/topbar/breadcrumb) + componente de modal reutilizável | [#136](https://github.com/dufelizardo/mais_saude_publica/pull/136), [#137](https://github.com/dufelizardo/mais_saude_publica/pull/137) | ✅ |
| 1 | Cadastros estruturais: Categorias salariais, Cargos, Tabela salarial, Regras de anuênio | [#135](https://github.com/dufelizardo/mais_saude_publica/pull/135), [#136](https://github.com/dufelizardo/mais_saude_publica/pull/136) | ✅ |
| 2a | Tela central do Profissional — estrutura (busca por CPF, `.detail-card`/`.tabs`) + abas Dados e Lotação (só leitura) | [#142](https://github.com/dufelizardo/mais_saude_publica/pull/142) | ✅ |
| 2b | Tela central do Profissional — abas Ajustes individuais e Treinamentos (histórico + registro) | [#143](https://github.com/dufelizardo/mais_saude_publica/pull/143) | ✅ |
| 2c | Tela central do Profissional — abas Avaliações e Desligamento/Rescisão (histórico + registro) | [#144](https://github.com/dufelizardo/mais_saude_publica/pull/144) | ✅ |
| 3 | Lotações (fluxo de transferência) — form embutido na aba Lotação do perfil, sem tela própria (ver [ADR-0021](../adr/0021-fluxo-de-transferencia-na-aba-lotacao.md)) | [#145](https://github.com/dufelizardo/mais_saude_publica/pull/145) | ✅ |
| 4 | Composição remuneratória — endpoint de leitura calculada + aba nova no perfil (ver [ADR-0022](../adr/0022-composicao-remuneratoria-endpoint-calculado.md)) | [#146](https://github.com/dufelizardo/mais_saude_publica/pull/146) | ✅ |
| 5 | Afastamentos e Licenças — aba nova no perfil (ver [ADR-0023](../adr/0023-afastamentos-e-licencas-no-perfil.md)) | [#147](https://github.com/dufelizardo/mais_saude_publica/pull/147) | ✅ |
| 6a | Ponto com filtro de período — aba nova no perfil (ver [ADR-0024](../adr/0024-ponto-com-filtro-de-periodo.md)) | [#148](https://github.com/dufelizardo/mais_saude_publica/pull/148) | ✅ |
| 6b | Folha de pagamento — listagem por competência + aba no perfil + tela própria (ver [ADR-0025](../adr/0025-folha-de-pagamento-por-competencia.md)) | [#149](https://github.com/dufelizardo/mais_saude_publica/pull/149) | ✅ |
| 7 | Histórico funcional consolidado — aba nova no perfil, sem endpoint novo (ver [ADR-0026](../adr/0026-historico-funcional-consolidado.md)) | [#150](https://github.com/dufelizardo/mais_saude_publica/pull/150) | ✅ |
| 8a | Catálogo de treinamentos + Ciclos de avaliação (ver [ADR-0027](../adr/0027-fatia-8-treinamento-avaliacao-sst-recrutamento.md)) | [#151](https://github.com/dufelizardo/mais_saude_publica/pull/151) | ✅ |
| 8b | SST — aba "SST" no perfil, com 3 sub-abas (Exames ocupacionais, Acidentes de trabalho, EPIs) | [#152](https://github.com/dufelizardo/mais_saude_publica/pull/152) | ✅ |
| 8c | Recrutamento — Vagas + Candidatos | [#153](https://github.com/dufelizardo/mais_saude_publica/pull/153) | 🔶 PR aberta, bloqueada — CI não dispara para essa branch (causa ainda não diagnosticável por CLI, ver PR) |
| 8d | Menu agrupado + breadcrumb dinâmico | — | ⏳ Não iniciada |
| 9 | Benefícios — backend: `PATCH /adesao-beneficio/{uuid}/encerrar` (ver [ADR-0028](../adr/0028-fatia-9-beneficios.md)) | [#154](https://github.com/dufelizardo/mais_saude_publica/pull/154) | ✅ |
| 9 | Benefícios — frontend: `/rh/tipos-beneficio`, `/rh/tipos-beneficio/:tipoId/valores`, aba "Benefícios" no perfil | [#155](https://github.com/dufelizardo/mais_saude_publica/pull/155) | ✅ |
| — | Gap identificado no levantamento de endpoints de `Profissional`: lista de profissionais (`/profissionais`) + edição de contato no perfil (ver [ADR-0029](../adr/0029-listagem-e-edicao-de-contato-do-profissional.md)) | [#157](https://github.com/dufelizardo/mais_saude_publica/pull/157) | ✅ |

### Regra da fatia 8 (definida pelo usuário, vale para 8a-8d)

> Os endpoints da fatia 8 atualmente suportam criação e consulta. As interfaces não devem
> disponibilizar edição ou mudança de status onde não houver endpoint correspondente. A evolução
> dessas operações poderá ser incorporada posteriormente com a disponibilização dos respectivos
> endpoints.

Duas exceções derivadas (não são "status gravado", são calculadas a partir de datas já existentes
— não violam a regra acima): status do ciclo de avaliação (Agendado/Em andamento/Encerrado) e
situação do EPI (Em uso/Devolvido). Ver [ADR-0027](../adr/0027-fatia-8-treinamento-avaliacao-sst-recrutamento.md).

A fatia 9 (Benefícios) segue a mesma regra e o mesmo raciocínio de situação derivada — com uma
diferença: ali a regra motivou uma mudança real de backend (`PATCH /adesao-beneficio/{uuid}/encerrar`),
porque `AdesaoBeneficio.dataFim` já existia no model sem nenhum endpoint capaz de escrevê-lo. Ver
[ADR-0028](../adr/0028-fatia-9-beneficios.md).

## 7. Referências

- [MODELO-RH.md](./MODELO-RH.md) — desenho completo das entidades/relacionamentos de todas as fases
- [ADR-0014](../adr/0014-modulo-profissional-rh-com-vinculo-fraco-por-reconciliacao.md) — vínculo responsável↔unidade
- [ADR-0017](../adr/0017-numero-de-matricula-automatico-e-cpf-nao-unico.md) — matrícula, ficha e o
  precedente de "dado temporal vira histórico"
- O texto original gerado pelo DeepSeek não foi anexado a este repositório — este documento é a
  versão curada. Se for necessário preservar o texto bruto também, pedir explicitamente.
