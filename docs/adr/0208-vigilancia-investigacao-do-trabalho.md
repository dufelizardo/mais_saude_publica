# 0208 — Vigilância: a investigação do trabalho e o risco ocupacional (V5b)

## Status

Aceita e implementada. Segunda e última parte da fatia **V5** do domínio 23 ([ADR-0110](./0110-vigilancia-em-saude.md)), depois da V5a
([ADR-0207](./0207-vigilancia-saude-do-trabalhador.md)). Com ela, o domínio 23 fica completo; o SINAN e as demais integrações estão na
lista do domínio 20.

## Contexto

- **A V5a pôs o trabalho na notificação** (ocupação, empregador, local e acidente), mas a vigilância do trabalhador não termina na
  notificação: o CEREST e a VISAT vão ao local de trabalho, concluem se o agravo tem relação com o trabalho e recomendam mudanças no
  ambiente.
- **O nexo é o centro:** um caso descartado pela investigação não pode inflar os números do trabalho.
- **A vigilância do trabalhador não tem poder de polícia próprio** no município: quem fiscaliza o estabelecimento é a vigilância
  sanitária (ADR-0199/0200), e a fiscalização do trabalho é federal (Superintendência Regional do Trabalho) ou do Ministério Público do
  Trabalho.
- **O risco não é do caso, é do ambiente:** o mesmo ruído atinge todos os trabalhadores daquele empregador.

## Decisão

1. **A investigação do trabalho (`InvestigacaoTrabalho`) é 1:1 com a notificação que tem o bloco do trabalho** (sem o bloco, 422):
   - a visita ao local (data, não no futuro nem antes do evento, 400), o local visitado e os achados; o investigador é o profissional do
     login. Iniciada, a notificação passa a em investigação;
   - **o nexo:** em análise, até a conclusão como confirmado, descartado ou inconclusivo, sempre com a justificativa (400). Pode ser revisto
     enquanto a notificação está aberta; a trilha de auditoria guarda cada conclusão.
2. **As medidas (`MedidaTrabalho`)** recomendadas: adequação do ambiente, proteção coletiva, proteção individual, organização do
   trabalho, capacitação, afastamento da exposição, vigilância dos expostos ou outra; com a descrição e o prazo (no passado, 400).
   - **A verificação:** cumprida ou não cumprida (com a observação, 400), uma vez (422). Vale depois do encerramento da notificação,
     porque o prazo da medida pode passar dele.
   - **Vencida** é a recomendada com o prazo passado.
3. **A comunicação à vigilância sanitária** abre a denúncia sanitária do canal novo **Vigilância** contra o empregador (o estabelecimento
   ligado, ou o nome e o documento do bloco), com o agravo, a data do evento e o que verificar. **Não leva o nome nem os dados do
   trabalhador** (LGPD, art. 11): a VISA fiscaliza o ambiente, não o caso. Uma por investigação (409); sem empregador no bloco, 422. O
   canal Vigilância não aparece no cadastro manual de denúncias.
4. **O encaminhamento intersetorial** (ADR-0154) à Superintendência do Trabalho, ao MPT ou ao sindicato, com a investigação como origem
   (domínio `VIGILANCIA`), o motivo escrito por quem investiga e o retorno em 30 dias se não informado. As regras do encaminhamento
   continuam (inativa 422, em aberto para a mesma 409).
5. **O risco ocupacional (`RiscoOcupacional`) é do empregador:** tipo (físico, químico, biológico, ergonômico, de acidente,
   psicossocial), agente, descrição, local e trabalhadores expostos; ativo até ser **controlado** com o motivo (uma vez, 422).
   - Pela investigação, com a notificação, o empregador vem do bloco do trabalho se não informado, e o risco fica ligado a ela;
   - sem a investigação, é o cadastro da vigilância do trabalhador: o empregador é obrigatório (400), com o CPF ou CNPJ conferido e ligado
     ao estabelecimento da VISA pelo documento.
6. **Indicadores no relatório Vigilância do #19**, pela data da notificação e no escopo dos indicadores; **as de nexo descartado ficam fora
   das contagens** (aparecem só na nota):
   - relacionadas ao trabalho, nexo investigado (%), acidentes fatais, trabalho com menores de 18 anos (idade na data do evento) e material
     biológico em trabalhadores da saúde (empregador da seção Q do CNAE ou ocupação da saúde na CBO: 22, 322, 515);
   - as tabelas por agravo (com nexo confirmado, óbitos e menores), por seção do CNAE 2.0 (pela divisão) e por grande grupo da CBO.
7. **Na Sala de Situação,** o alerta da saúde do trabalhador: os acidentes de trabalho dos últimos 7 dias pela data do evento, os óbitos,
   as investigações abertas, as medidas vencidas e os riscos ativos, **sem o nome do trabalhador**.
8. **No painel da aba Saúde do trabalhador,** as medidas vencidas; na aba, os riscos ocupacionais com o cadastro e o controle.
9. **Acesso, sem permissão nova:** investigar, recomendar, verificar, comunicar, encaminhar e o risco, `VIGILANCIA.INVESTIGAR` (na unidade
   da notificação; o risco sem notificação e a lista, em qualquer unidade); a semana da Sala de Situação, `VIGILANCIA.NOTIFICAR` ou
   `VIGILANCIA.INVESTIGAR`.

### API

| Método | Caminho (`/api/v1/vigilancia/`) | Permissão |
|---|---|---|
| PUT | `notificacao/{id}/trabalho/investigacao` | `VIGILANCIA.INVESTIGAR` |
| POST | `notificacao/{id}/trabalho/investigacao/nexo`, `.../medida`, `.../comunicacao-visa`, `.../encaminhamento` | `VIGILANCIA.INVESTIGAR` |
| POST | `medida-trabalho/{id}/verificacao` | `VIGILANCIA.INVESTIGAR` |
| POST | `saude-trabalhador/risco`, `risco-ocupacional/{id}/controle` | `VIGILANCIA.INVESTIGAR` |
| GET | `saude-trabalhador/riscos` | `VIGILANCIA.INVESTIGAR` |
| GET | `saude-trabalhador/semana` | `VIGILANCIA.NOTIFICAR` ou `VIGILANCIA.INVESTIGAR` |

A investigação vem no detalhe da notificação, em `trabalho.investigacao`; o painel ganha `medidasVencidas`; o relatório `VIGILANCIA`
ganha os indicadores e as três tabelas.

### Migração

`V90__vigilancia_investigacao_trabalho.sql`: o canal `VIGILANCIA` no CHECK da denúncia sanitária; as tabelas `tb_investigacao_trabalho`
(única por notificação e por denúncia), `tb_medida_trabalho` e `tb_risco_ocupacional`.

## Alternativas descartadas

- **Reaproveitar a `MedidaControle` da V2b:** ela é da medida epidemiológica (bloqueio, isolamento, quimioprofilaxia), com situação de
  execução; a medida do trabalho é recomendação ao empregador com verificação de cumprimento.
- **O risco dentro da investigação só:** o risco é do ambiente e sobrevive ao caso; o cadastro aceita o risco sem notificação (a vigilância
  de ambientes).
- **Auto de infração pela vigilância do trabalhador:** o poder de polícia é da VISA (V3b) ou da fiscalização federal; a investigação
  comunica e encaminha.
- **Mandar o caso à VISA:** a fiscalização do ambiente não precisa do nome do trabalhador; mandar seria tratamento de dado de saúde sem
  necessidade.

## Consequências

- **STATUS-DOS-DOMINIOS:** a V5 fica completa e o domínio 23 passa a ✅ (as integrações estão no domínio 20).
- **O nexo descartado muda os números:** o relatório informa quantas ficaram fora.
- **Fora do escopo:** a vigilância programada de ambientes por ramo, o catálogo oficial do CNAE e da CBO, a população economicamente ativa
  para coeficientes e o envio ao SINAN (domínio 20).

## Testes

- **JUnit `InvestigacaoTrabalhoControllerTest`:** sem a investigação, 422; a dengue sem bloco, 422; a visita no futuro, 400; a investigação
  passa a notificação a em investigação; o nexo em análise, 400; confirmado; a medida com prazo no passado, 400; verificada não cumprida
  sem observação, 400; cumprida; de novo, 422; a VISA com a denúncia do canal Vigilância contra o empregador e sem o nome do trabalhador; de
  novo, 409; o encaminhamento à Superintendência com o retorno em 30 dias; de novo, 409; o risco pela investigação com o empregador do bloco;
  sem empregador, 400; controlado; de novo, 422. O painel com a medida vencida, a semana sem o nome do trabalhador e o relatório da unidade
  só com a de nexo confirmado (fatal, menor de 18 anos, Construção, grande grupo 7).
- **JUnit `InvestigacaoTrabalhoAutorizacaoControllerTest`** (segurança ligada): o enfermeiro não investiga nem registra risco (403), vê a
  semana; a vigilância da unidade investiga, recomenda e registra o risco; sem login, 401.
- **Robot API `test/vigilancia/InvestigacaoTrabalho.robot`:** CT-001 a CT-006.
- **Robot UI `test/ui/vigilancia/UI_vigilancia.robot`:** CT-014 (a investigação pela tela), CT-015 (o risco ocupacional na aba) e CT-016
  (o alerta na Sala de Situação).
