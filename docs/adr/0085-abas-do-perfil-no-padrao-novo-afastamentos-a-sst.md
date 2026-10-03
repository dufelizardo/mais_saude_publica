# 0085 — Abas do perfil do profissional no padrão novo (Afastamentos a SST)

## Status

Aceita e implementada (parte 2 de 3). Continua a [ADR-0084](./0084-abas-do-perfil-no-padrao-novo-dados-a-ajustes.md),
com o mesmo desenho de aba.

## Contexto

- As abas Afastamentos e Licenças, Ponto, Folha de pagamento e SST tinham:
  - oito formulários soltos abaixo das tabelas;
  - o modal de correção de ponto;
  - tabelas sem estado vazio.
- As tabelas mostravam o valor cru dos enums (`FERIAS`, `EM_ANDAMENTO`, `APTO_COM_RESTRICAO`), e o
  histórico funcional também.
- Um ASO com validade vencida não se distinguia de um válido.

## Decisão

1. **Mesmo desenho da ADR-0084:** cabeçalho de seção com a ação, que só aparece com `RH.GERENCIAR`;
   tabela em `tbl-wrap`; estado vazio; formulário em gaveta; toast ao salvar.
   - Afastamentos e Licenças viram duas seções na mesma aba, cada uma com o seu botão.
   - SST mantém as subabas (Exames, Acidentes, EPIs), com uma seção por subaba.
2. **Gavetas:**
   - afastamento, licença, ponto, correção de ponto (antes modal), folha, exame, acidente e EPI;
   - datas já vêm preenchidas (hoje, agora, competência atual);
   - o afastamento abre como "Aprovado", o caso mais comum quando é o RH que lança;
   - a licença só lista afastamentos sem licença: se houver um só, já vem escolhido; se não houver
     nenhum, a gaveta avisa e o envio fica desabilitado.
3. **Rótulos em português para os enums** (tabela `ROTULOS` no componente), também no histórico
   funcional.
   - Situação do afastamento em badge: Solicitado (azul), Aprovado (verde), Em andamento (amarelo),
     Concluído e Cancelado (cinza).
   - Resultado do exame: Apto (verde), Apto com restrição (amarelo), Inapto (vermelho).
4. **ASO vencido:** validade anterior a hoje ganha a etiqueta "Vencido". É só leitura da data, sem regra
   nova no backend.
5. **Ponto:**
   - o filtro de período fica em pílulas acima da tabela;
   - Aprovar, Rejeitar e Solicitar correção têm nome acessível com a data e a hora da marcação, para o
     leitor de tela distinguir as linhas;
   - aprovar e rejeitar também mostram toast.
6. **Nenhuma regra muda:** mesmas validações, serviços e recargas.

## Consequências

- Até a aba SST, o perfil está no padrão novo. Faltam Treinamentos, Avaliações, Benefícios,
  Desligamento e Histórico (parte C), com o modal de encerrar adesão.
- **Achado de teste:** o seletor `role=button[name="…"]` da Browser library compara o nome acessível
  inteiro. Botão cujo nome leva dado variável (data, nome) é clicado no POM por um seletor da célula.

## Testes

- **Robot de interface** (`test/ui/rh/perfil/UI_perfil_abas_afastamentos_a_sst.robot`):
  - registrar afastamento pela gaveta;
  - registrar ponto e solicitar a correção (correção pendente na linha);
  - registrar exame ocupacional (resultado na tabela);
  - a gaveta da folha recusa competência fora de MM/AAAA.
- Sem backend novo.
