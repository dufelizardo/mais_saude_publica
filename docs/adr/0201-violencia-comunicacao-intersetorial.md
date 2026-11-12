# 0201 — Vigilância: a violência notificada comunicada à rede intersetorial (V3c)

## Status

Aceita e implementada. Última parte da fatia **V3** do domínio 23 ([ADR-0110](./0110-vigilancia-em-saude.md)), depois da V3a
([ADR-0199](./0199-vigilancia-sanitaria-fundacao.md)) e da V3b ([ADR-0200](./0200-processo-sanitario-e-medidas.md)). Fecha a ligação IS1 →
V3 do STATUS: a Vigilância entra na rede intersetorial ([ADR-0154](./0154-intersetorialidade.md)) como domínio de origem.

## Contexto

- **A V1** ([ADR-0124](./0124-vigilancia-epidemiologica-fundacao.md)) notifica a violência interpessoal e autoprovocada como agravo
  sensível, com a ficha (tipo de violência, lesão autoprovocada).
- **A lei manda comunicar** além de notificar:
  - criança e adolescente: ao Conselho Tutelar (ECA, art. 13; o art. 245 pune o profissional que deixa de comunicar);
  - pessoa idosa: à autoridade policial, ao Ministério Público ou aos conselhos da pessoa idosa (Estatuto da Pessoa Idosa, art. 19);
  - mulher: à autoridade policial em 24 horas (Lei 10.778/2003, alterada pela Lei 13.931/2019).
- **Nada no sistema** lembrava a comunicação nem guardava que ela foi feita.

## Decisão

1. **A obrigação é calculada**, não guardada, pela notificação do agravo `VIOLENCIA_INTERPESSOAL_AUTOPROVOCADA`:
   - a idade é a da data dos primeiros sintomas (a ocorrência) ou, sem ela, a da notificação; sem a data de nascimento, não se calcula;
   - **menor de 18 anos**, inclusive a lesão autoprovocada: Conselho Tutelar;
   - **60 anos ou mais**, fora a autoprovocada: conselho de direitos, Ministério Público ou segurança pública;
   - **mulher de 18 a 59 anos**, fora a autoprovocada: segurança pública;
   - o homem adulto e a lesão autoprovocada do adulto não têm comunicação obrigatória (a Saúde Mental segue pela SM3);
   - a autoprovocada vem da ficha: o campo `lesaoAutoprovocada` ou o tipo "Autoprovocada".
2. **O prazo é de 24 horas** do registro da notificação. A situação: PENDENTE, VENCIDA (passou o prazo sem comunicação) ou FEITA (com a
   data e se foi no prazo); sem obrigação, NAO_SE_APLICA.
3. **A comunicação é um encaminhamento intersetorial** (ADR-0154), sem tabela nova:
   - o domínio de origem `VIGILANCIA` entra no CHECK; a origem é a notificação;
   - a instituição precisa ser de um tipo aceito pela obrigação (422); sem obrigação, 422; as regras do encaminhamento continuam (inativa
     422, duplicada em aberto 409); o envio não é antes da notificação (400);
   - o retorno esperado é de 30 dias, se não informado;
   - **o motivo é montado pelo sistema** com o mínimo: a base legal, o tipo de violência, a data e a unidade. Dado sensível vai a outro
     setor por obrigação legal (LGPD, art. 11, II, a), sem o resto da ficha;
   - a comunicação feita é o primeiro encaminhamento não cancelado a um tipo aceito; o cancelado (registro errado) devolve a pendência.
4. **O catálogo ganha o tipo `CONSELHO_DE_DIREITOS`** (garantia de direitos), para os conselhos da pessoa idosa, da mulher e da pessoa
   com deficiência.
5. **Onde aparece:**
   - no detalhe da notificação de violência, a seção **Comunicação obrigatória** (destino, base legal, prazo, situação, as comunicações
     registradas, a escolha da instituição entre as dos tipos aceitos e o cancelamento com motivo);
   - na Vigilância epidemiológica, a lista **Comunicações obrigatórias da violência pendentes**, vencidas primeiro, para quem investiga;
   - no relatório **Vigilância** do #19, a seção por destino (obrigatórias, feitas, no prazo) e o indicador "Comunicações de violência no
     prazo (%)", só com totais.
6. **Acesso, sem permissão nova:** comunicar e ver a comunicação seguem o detalhe da notificação sensível (quem notificou ou quem investiga
   na unidade); a lista de pendentes, só `VIGILANCIA.INVESTIGAR`, nas unidades do acesso.

### API

| Método | Caminho (`/api/v1/vigilancia/`) | Permissão |
|---|---|---|
| GET | `notificacao/{id}/comunicacao` | `VIGILANCIA.NOTIFICAR` ou `VIGILANCIA.INVESTIGAR`, com o sigilo do agravo |
| POST | `notificacao/{id}/comunicacao`, `notificacao/{id}/comunicacao/{encaminhamento}/cancelamento` | idem |
| GET | `comunicacoes-pendentes` | `VIGILANCIA.INVESTIGAR` |

### Migração

`V84__violencia_comunicacao_intersetorial.sql`: `VIGILANCIA` no CHECK do domínio de origem do encaminhamento e `CONSELHO_DE_DIREITOS` no
CHECK do tipo da instituição. Sem tabela nova.

## Alternativas descartadas

- **Enviar a comunicação sozinho:** a ADR-0110 manda que a rede sugira e o profissional registre; o envio eletrônico ao Conselho Tutelar ou
  à delegacia é integração (domínio 20).
- **Tabela própria de comunicação:** o encaminhamento intersetorial já tem a instituição, o envio, o recebimento, o retorno e o
  cancelamento.
- **Guardar a obrigação:** ela sai da notificação e do paciente; guardar duplicaria e poderia divergir se o cadastro for corrigido.

## Consequências

- **STATUS-DOS-DOMINIOS:** a V3 fica completa (V3a, V3b, V3c); o domínio 23 segue 🟡 pela V5 (saúde do trabalhador e integrações).
- **A idade depende do cadastro:** sem a data de nascimento, a obrigação não aparece.
- **Fora do escopo:** a pessoa com deficiência (Lei 13.146/2015, art. 26), porque o cadastro não tem o dado de forma confiável; o envio
  eletrônico (domínio 20).

## Testes

- **JUnit `ComunicacaoViolenciaControllerTest`:** a criança ao Conselho Tutelar (pendente, tipo errado 422, feita no prazo com o motivo
  mínimo, duplicada 409, cancelada volta a pendente, vencida depois de 24 horas e feita fora do prazo), a adolescente com lesão
  autoprovocada, o relatório; a pessoa idosa com os três tipos aceitos; a mulher (conselho 422, envio antes da notificação 400, delegacia
  201); sem obrigação o homem adulto (422 ao comunicar), a autoprovocada adulta e outro agravo.
- **JUnit `ComunicacaoViolenciaAutorizacaoControllerTest`,** com a segurança ligada: outro enfermeiro da unidade não vê nem comunica (403);
  quem notificou comunica; a vigilância vê as pendentes e o enfermeiro não; sem login, 401.
- **Robot:** `ComunicacaoViolencia.robot` (CT-001 a CT-004); CT-010 de `UI_vigilancia.robot`.
