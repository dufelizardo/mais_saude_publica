# 0207 — Vigilância: a saúde do trabalhador na notificação (V5a)

## Status

Aceita e implementada. Primeira parte da fatia **V5** do domínio 23 ([ADR-0110](./0110-vigilancia-em-saude.md)). A V5b (investigação do
ambiente de trabalho com o nexo, as medidas, o risco ocupacional por empregador e os indicadores) vem em ADR própria. O adaptador do SINAN
sai da V5 e fica na lista do domínio 20 (Integrações), pela regra de que integração não impede fechar a fatia.

## Contexto

- **A ADR-0110 desenhou a Saúde do Trabalhador** como subdomínio da vigilância (trabalhador → evento → notificação → investigação → medidas),
  sem depender do RH: o trabalhador pode ser de qualquer empresa do município.
- **O catálogo tinha só o `ACIDENTE_TRABALHO_GRAVE`** (V18), sem CID e sem nada sobre a ocupação, o empregador ou o local do evento.
- **O cadastro do paciente não tem ocupação.** Ocupação muda com o tempo; o que importa à vigilância é a da data do evento.
- **O RH registra o `AcidenteTrabalho` do servidor** (CAT e afastamento), sem ligação com a vigilância: um servidor acidentado não era
  notificado, e nada mostrava isso.
- **O acidente com material biológico** é o acidente de trabalho mais comum entre os trabalhadores da própria rede e pede o
  acompanhamento sorológico por seis meses.

## Decisão

1. **O agravo ganha a marca `relacionadoAoTrabalho`** (editável no catálogo). Ficam marcados:
   - o `ACIDENTE_TRABALHO_GRAVE` que já existia, que ganha o CID Y96;
   - o novo `ACIDENTE_TRABALHO_MATERIAL_BIOLOGICO`, compulsório e semanal, com prazo de encerramento de 180 dias, CID Z20.9 e a ficha
     versão 1 (tipo de exposição, material orgânico, circunstância, agente, proteção em uso, vacina contra hepatite B, fonte conhecida e
     conduta);
   - os agravos da vigilância sentinela, todos sem compulsoriedade, semanais, com 180 dias e editáveis: LER/DORT (Z57.9), PAIR (H83.3),
     pneumoconioses (J60 a J65), dermatoses ocupacionais (L98.9), câncer relacionado ao trabalho (C80) e transtorno mental relacionado ao
     trabalho (F99).

   A ficha continua sendo dado: não há tabela por doença.
2. **O bloco do trabalho é um `EventoTrabalho`, 1:1 com a notificação:**
   - **obrigatório** no agravo marcado (400); **opcional** nos demais, pela opção "relacionado ao trabalho" (a intoxicação por agrotóxico
     na lavoura, por exemplo);
   - **a ocupação** na data do evento: o código da CBO, conferido no formato (seis dígitos, 400), e a descrição. Fica no evento, não no
     paciente, porque é dado temporal;
   - **a situação no mercado de trabalho:** registrado, sem registro, servidor estatutário ou celetista, autônomo, empregador, avulso,
     cooperado, estagiário, aposentado, desempregado ou outra;
   - **o empregador:** CPF ou CNPJ conferido (400), o nome e o CNAE (sete dígitos, 400). Quando o documento já é de um estabelecimento da
     vigilância sanitária (ADR-0199), o evento se liga a ele e o nome vem de lá;
   - **o local do evento,** separado da residência, com a coordenada opcional (as duas ou nenhuma, 400);
   - **só no acidente** (os dois agravos de acidente; fora deles, 400): o tipo (típico ou de trajeto, obrigatório no grave), as horas
     depois do início da jornada (0 a 24), a CAT (sim, não ou ignorado), os outros trabalhadores atingidos e o óbito. No acidente, a data
     dos primeiros sintomas é a data do acidente, e é obrigatória (400).
3. **A investigação completa ou corrige o bloco** (o notificante nem sempre sabe o empregador): `PUT notificacao/{id}/trabalho`, só
   `VIGILANCIA.INVESTIGAR` na unidade e antes do encerramento (422). Pela mesma chamada, a investigação inclui o bloco numa notificação que
   não tinha.
4. **O acompanhamento sorológico do material biológico** tem três etapas previstas a partir da data do acidente: 6 semanas, 3 meses e
   6 meses. A data realizada de cada uma é registrada por quem notifica ou investiga na unidade (não no futuro, nem antes do acidente,
   400; só no material biológico aberto, 422). A etapa sem registro depois da data prevista fica **vencida**.
5. **O servidor da rede, sem duplicar o RH:**
   - o evento se liga, opcionalmente, ao `AcidenteTrabalho` do RH: o acidente existe (404), não está em outra notificação (409) e é do
     mesmo CPF do paciente (422). Só no acidente;
   - **a CAT e o afastamento continuam com o RH;** o RH vê apenas a marca "Notificado à vigilância", nunca o conteúdo, que é dado de
     saúde;
   - **a lista "acidentes de servidores sem notificação"** mostra os acidentes do RH sem evento ligado, com o paciente de mesmo CPF, se
     houver um só. O servidor não tem unidade no RH, então a lista é da vigilância com acesso à rede inteira (como o CEREST municipal); com
     acesso restrito a unidades, 403.
6. **O painel da saúde do trabalhador** (`GET saude-trabalhador/painel`): as notificações relacionadas ao trabalho do escopo, com os totais
   (acidentes, material biológico, óbitos), e as etapas do acompanhamento sorológico vencidas ou nos próximos 7 dias. Segue o filtro de
   unidade e o sigilo do agravo da lista de notificações.
7. **Acesso, sem permissão nova:** notificar com o bloco, `VIGILANCIA.NOTIFICAR`; corrigir o bloco e ver os acidentes dos servidores,
   `VIGILANCIA.INVESTIGAR`; o painel e o acompanhamento, uma das duas.
8. **Telas:**
   - no formulário da notificação, o bloco **Trabalho** (obrigatório no agravo marcado; nos demais, pela opção "Relacionado ao
     trabalho"), e o rótulo "Data do acidente" no lugar dos primeiros sintomas no acidente;
   - no detalhe, a seção **Trabalho** com o bloco, a correção pela investigação e o acompanhamento sorológico com o registro de cada etapa;
   - na Vigilância epidemiológica, a aba **Saúde do trabalhador**: os totais, os acompanhamentos a fazer, as notificações e os acidentes
     dos servidores sem notificação, com o botão que abre a notificação do acidente grave já ligada ao acidente do RH;
   - no catálogo, a marca **Trabalho** e a opção no cadastro do agravo; no perfil do profissional (RH), a marca no acidente.

### API

| Método | Caminho (`/api/v1/vigilancia/`) | Permissão |
|---|---|---|
| POST | `notificacao` (campo `trabalho`) | `VIGILANCIA.NOTIFICAR` |
| PUT | `notificacao/{id}/trabalho` | `VIGILANCIA.INVESTIGAR` |
| POST | `notificacao/{id}/trabalho/acompanhamento` | `VIGILANCIA.NOTIFICAR` ou `VIGILANCIA.INVESTIGAR` |
| GET | `saude-trabalhador/painel` | `VIGILANCIA.NOTIFICAR` ou `VIGILANCIA.INVESTIGAR` |
| GET | `saude-trabalhador/acidentes-sem-notificacao` | `VIGILANCIA.INVESTIGAR` na rede inteira |

O `GET /api/v1/acidente-trabalho/profissional/{matricula}` do RH ganha `notificadoVigilancia`.

### Migração

`V89__vigilancia_saude_trabalhador.sql`: a coluna `relacionado_ao_trabalho` em `tb_agravo`; os sete agravos novos com CIDs, critérios e a
ficha do material biológico; o CID Y96 no acidente grave; a tabela `tb_evento_trabalho` (únicos por notificação e por acidente do RH).

## Alternativas descartadas

- **Ocupação no cadastro do paciente:** a ocupação muda; a vigilância precisa da que valia na data do evento, e o cadastro não guarda
  histórico (a regra do projeto é modelar dado temporal como histórico).
- **Catálogo oficial da CBO e do CNAE:** são grandes e mudam; entra o código conferido no formato, e o catálogo fica para quando houver
  a integração.
- **Notificar sozinho o acidente do RH:** notificar é ato profissional (ADR-0110). A rede mostra o acidente sem notificação; o
  profissional notifica.
- **Mostrar ao RH o conteúdo da notificação:** é dado de saúde do servidor; o RH precisa só saber que a vigilância foi avisada.

## Consequências

- **STATUS-DOS-DOMINIOS:** a V5a fica feita; o domínio 23 segue 🟡 até a V5b. O SINAN vai para a lista do domínio 20.
- **O acidente sem data não é aceito:** o acompanhamento e os indicadores dependem dela.
- **Fora do escopo:** a investigação do ambiente, o nexo, as medidas, o risco ocupacional e os indicadores (V5b); a CAT eletrônica e o
  INSS (RH e domínio 20); coeficientes por população economicamente ativa.

## Testes

- **JUnit `SaudeTrabalhadorControllerTest`:** o catálogo com a marca; o acidente grave sem o bloco, com CBO errado, com CNPJ inválido, sem
  a data e sem o tipo (400); o óbito na LER/DORT (400); o acidente com o CNPJ e o CNAE só com os dígitos; a dengue sem bloco; o painel com
  o óbito; a correção pela investigação; o acompanhamento fora do material biológico (422); o material biológico com a etapa de 6 semanas
  vencida, o registro no futuro e antes do acidente (400) e feito hoje, saindo das pendências; o acidente do RH na lista com o paciente de
  mesmo CPF (com pontuação), de outro trabalhador (422), inexistente (404), ligado (201), repetido (409), fora da lista e marcado no RH.
- **JUnit `SaudeTrabalhadorAutorizacaoControllerTest`** (segurança ligada): o enfermeiro notifica, vê o painel e registra o
  acompanhamento, mas não corrige o bloco (403); a vigilância da unidade corrige; a lista dos acidentes dos servidores é 403 para o
  enfermeiro e para a vigilância da unidade e 200 para a vigilância da rede; sem login, 401.
- **Robot API `test/vigilancia/SaudeTrabalhador.robot`:** CT-001 a CT-006.
- **Robot UI `test/ui/vigilancia/UI_vigilancia.robot`:** CT-011 (notificar o acidente com o bloco), CT-012 (acompanhamento sorológico) e
  CT-013 (acidente do servidor no RH).
