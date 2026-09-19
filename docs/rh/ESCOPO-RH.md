# Escopo do domínio de RH — roadmap e ideias

**Data:** 2026-09-19
**Status:** Vivo (visão/roadmap, não uma decisão implementada — ver aviso na seção 1)

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

Cada um continua como 💡 ideia registrada, não desenhada — nenhum desses tem plano de implementação
ainda, e os números/regras trabalhistas seguem sujeitos ao aviso da seção 1.

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

## 6. Referências

- [MODELO-RH.md](./MODELO-RH.md) — desenho completo das entidades/relacionamentos de todas as fases
- [ADR-0014](../adr/0014-modulo-profissional-rh-com-vinculo-fraco-por-reconciliacao.md) — vínculo responsável↔unidade
- [ADR-0017](../adr/0017-numero-de-matricula-automatico-e-cpf-nao-unico.md) — matrícula, ficha e o
  precedente de "dado temporal vira histórico"
- O texto original gerado pelo DeepSeek não foi anexado a este repositório — este documento é a
  versão curada. Se for necessário preservar o texto bruto também, pedir explicitamente.
