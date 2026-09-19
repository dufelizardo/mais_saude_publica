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

## 4. Subdomínios identificados (visão geral, não desenhados)

Lista curta — cada item é só o nome + a ideia central, sem fingir o nível de detalhe que o
documento original tinha. "Status" indica se já tem um plano de implementação real ou se é só
ideia registrada.

| Subdomínio | Ideia central | Status |
|---|---|---|
| **Lotação** | Vínculo profissional↔unidade com histórico (data início/fim), suporta transferência | 📝 Próximo a desenhar |
| **Salário** | Histórico de remuneração com data de vigência, suporta reajuste/promoção | 📝 Depois de Lotação |
| **Afastamentos** | Ausências (férias, licença médica/pessoal) sem alterar o status de `ativo` do profissional | 💡 Ideia registrada |
| **Licenças** | Subtipos legais de afastamento (maternidade, paternidade, acidente de trabalho) com regras próprias de duração/remuneração | 💡 Ideia registrada |
| **Ponto** | Registro de jornada (entrada/saída/intervalo) | 💡 Ideia registrada |
| **Desligamento com cálculo de rescisão** | Hoje o desligamento é só `ativo=false` + data (ADR-0017); cálculo de verbas rescisórias (aviso prévio, FGTS, 13º/férias proporcionais) é outra etapa, e cai direto no aviso da seção 1 sobre validação | 💡 Ideia registrada |
| **Folha de pagamento** | Fecha mensalmente a partir de ponto + afastamentos + salário vigente | 💡 Ideia registrada |
| **Recrutamento** | Vaga → candidato → processo seletivo → admissão, com rastreabilidade de como cada profissional foi contratado | 💡 Ideia registrada |
| **Treinamento/Certificações** | Educação continuada, certificações com validade (relevante pra saúde: NR-32, suporte básico de vida) | 💡 Ideia registrada |
| **SST** | Exames ocupacionais, acidentes de trabalho (CAT), controle de EPI | 💡 Ideia registrada |
| **Avaliação de desempenho** | Ciclos de avaliação, metas | 💡 Ideia registrada |

## 5. Próximo passo planejado

**Lotação primeiro.** Motivos: já existe uma base pra generalizar (`UnidadeDeSaude.responsavel`),
e o subdomínio é mais simples que Salário (não envolve cálculo, só "profissional X está na unidade
Y desde a data Z") — serve de prova de conceito pro padrão de histórico da seção 2 antes de aplicá-lo
a algo com dinheiro envolvido.

**Salário depois**, mesmo padrão de histórico, uma vez validado com Lotação.

Os demais subdomínios da seção 4 ficam como referência futura — cada um, quando for a vez de
desenhar, merece sua própria conversa e plano, não uma adoção em bloco.

## 6. Referências

- [ADR-0014](../adr/0014-modulo-profissional-rh-com-vinculo-fraco-por-reconciliacao.md) — vínculo responsável↔unidade
- [ADR-0017](../adr/0017-numero-de-matricula-automatico-e-cpf-nao-unico.md) — matrícula, ficha e o
  precedente de "dado temporal vira histórico"
- O texto original gerado pelo DeepSeek não foi anexado a este repositório — este documento é a
  versão curada. Se for necessário preservar o texto bruto também, pedir explicitamente.
