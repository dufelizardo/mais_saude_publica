# 0110 — Vigilância em Saúde: domínio próprio

## Status

Aceita. **Desenho, sem implementação.** A implementação vem nas fatias V1 a V5, cada uma com ADR própria. O detalhe
está em [`vigilancia/MODELO-VIGILANCIA.md`](../vigilancia/MODELO-VIGILANCIA.md).

## Contexto

- A vigilância não existia no mapa de domínios. Ela aparecia só por partes:
  - como **finalidade** prevista no Território (ADR-0108);
  - nos equipamentos do Mapa de Equipamentos (LACEN, CEREST, Unidade de Vigilância de Zoonoses, Serviço de
    Verificação de Óbito);
  - no "LACEN e vigilância" pendente do Laboratório.
- O RH já registra `AcidenteTrabalho` do servidor. A internação já guarda CID.
- A análise trazida pelo usuário mostra que a vigilância é uma camada de inteligência e intervenção sobre riscos,
  agravos e eventos. Ela tem subdomínios muito diferentes entre si e precisa de território, população e rede. Por
  isso **vale modelar antes de implementar**: influencia Território (ADR-0108) e Programas (ADR-0109) antes de eles
  virarem código.

## Decisão

1. **Domínio novo de primeira classe: #23 Vigilância em Saúde**, no grupo Assistência do MAPA.
   - Não há uma entidade `Vigilancia`. São subdomínios sobre uma base compartilhada: evento, local e território de
     referência, ação de vigilância.
   - **Ciclo:** evento → detecção → notificação → investigação → classificação → ação → encerramento →
     indicadores.
2. **Subdomínios:**
   - **Epidemiológica:** agravo, notificação, caso, investigação, contato, surto, medida de controle;
   - **Sanitária:** estabelecimento regulado, inspeção, irregularidade, medida, licença;
   - **Ambiental,** com **Zoonoses e Vetores** com autonomia operacional;
   - **Saúde do Trabalhador.**
3. **Imunização vira o domínio #24, próprio,** a desenhar em ADR própria.
   - Atravessa vigilância, assistência e estoque.
   - A campanha de vacinação continua sendo `Campanha` de Programas (ADR-0109); a Imunização registra as doses.
4. **Agravo é catálogo, notificação é genérica.**
   - O `Agravo` é cadastrável e guarda:
     - CID-10;
     - se é compulsório (Lista Nacional, Portaria de Consolidação GM/MS nº 4/2017);
     - periodicidade: imediata (24h) ou semanal;
     - prazo de encerramento e critérios de classificação;
     - marca de sensível;
     - **ficha específica como dado, versionada**.
   - Uma única `Notificacao` serve a todos os agravos, com as respostas da ficha. **Não há tabela por doença.**
   - Prevê também a notificação negativa semanal.
5. **Notificação não é caso confirmado.**
   - A investigação leva à classificação: suspeito, provável, confirmado, descartado ou inconclusivo, com o
     critério.
   - O **contato** pode não ter cadastro e vira `Paciente` quando for atendido.
6. **Surto é entidade própria:** agravo, território, período, casos relacionados, investigação, medidas e situação.
7. **A rede sugere, não notifica.** Um CID de agravo notificável no atendimento, na internação ou no exame gera uma
   sugestão ao profissional. Notificar é ato profissional.
8. **Território:**
   - todo evento tem **local** (endereço e ponto opcional em GeoJSON);
   - território e microárea são **opcionais** e só entram quando fazem sentido;
   - usa a finalidade **vigilância** do Território;
   - zoonoses usa o **imóvel** do Território, sem ficar presa à microárea do ACS.
9. **Sanitária não se mistura com a epidemiologia:**
   - exerce poder de polícia administrativa: auto, processo sanitário, prazos de defesa (Lei 6.437/1977);
   - o **estabelecimento regulado não é a `UnidadeDeSaude`**: a unidade pública pode estar entre os regulados,
     ligada de forma opcional;
   - os autos dependem do domínio Documentos.
10. **Saúde do Trabalhador não depende do RH:**
    - o trabalhador pode ser externo à rede;
    - se for servidor, o evento se liga opcionalmente ao `AcidenteTrabalho`;
    - o RH cuida da gestão e da CAT; a vigilância cuida da notificação e da investigação.
11. **Dado sensível (LGPD, art. 11):**
    - notificação identificada é dado de saúde, com leitura auditada;
    - agravos sensíveis têm sigilo e visibilidade restrita: HIV/aids, sífilis, violência interpessoal e
      autoprovocada;
    - boletins mostram só agregados.
12. **Permissões previstas:**
    - `VIGILANCIA.NOTIFICAR`: profissional assistencial;
    - `VIGILANCIA.INVESTIGAR`: equipe de vigilância;
    - `VIGILANCIA.GERENCIAR`: catálogo e surtos;
    - `VISA.FISCALIZAR`: autoridade sanitária;
    - as dos demais subdomínios, definidas nas fatias.
13. **Domínio interno primeiro, adaptadores depois:**
    - SINAN, SIVEP, SI-PNI, SISAGUA, ANVISA, CNES e e-SUS entram como adaptadores do domínio 20 (Integrações);
    - o modelo deles não contamina o domínio.
14. **Fatias:**
    - **V1 · Fundação:** agravo e ficha, notificação, investigação e classificação, sugestão a partir da rede.
    - **V2 · Epidemiologia avançada:** contato, surto, medida de controle, mapa dos casos.
    - **V3 · Sanitária.**
    - **V4 · Ambiental e Zoonoses.**
    - **V5 · Saúde do Trabalhador e integrações** (SINAN primeiro).

## Consequências

- O mapa ganha dois domínios: #23 Vigilância em Saúde e #24 Imunização.
- **Território, Programas e Documentos passam a ter mais um consumidor:**
  - a finalidade vigilância e o imóvel do Território ganham uso real;
  - Programas recebe casos confirmados que viram acompanhamento (por exemplo, tuberculose);
  - Documentos passa a ser pré-requisito da sanitária.
- O "LACEN e vigilância" do Laboratório e a aba Vacinação de Pacientes passam a ter domínio de destino (#23 e #24).
- **Fora do escopo agora:**
  - reproduzir o SINAN ou o SIVEP;
  - vigilância de óbito;
  - LACEN;
  - a Imunização em si;
  - alertas automáticos de surto por regra estatística.
