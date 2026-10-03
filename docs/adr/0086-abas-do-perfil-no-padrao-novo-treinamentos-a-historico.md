# 0086 — Abas do perfil do profissional no padrão novo (Treinamentos a Histórico)

## Status

Aceita e implementada (parte 3 de 3). Fecha a série das ADRs
[0084](./0084-abas-do-perfil-no-padrao-novo-dados-a-ajustes.md) e
[0085](./0085-abas-do-perfil-no-padrao-novo-afastamentos-a-sst.md): as 13 abas do perfil ficam no padrão
novo.

## Contexto

- Treinamentos, Avaliações, Benefícios e Desligamento/Rescisão ainda tinham formulários soltos abaixo
  das tabelas.
- O encerramento de adesão a benefício ainda usava o último `app-modal` do perfil.
- O histórico funcional era uma tabela de três colunas, com o valor do ajuste sem formato de moeda.
- Com as gavetas novas, o componente passou do orçamento de 1 MB do bundle inicial, porque o perfil
  era carregado com a aplicação.

## Decisão

1. **Mesmo desenho das partes A e B:** seção com a ação, que só aparece com `RH.GERENCIAR`; tabela em
   `tbl-wrap` com badge e estado vazio; formulário em gaveta; toast ao salvar.
2. **Gavetas:**
   - participação em treinamento, avaliação, nova adesão e cálculo de rescisão;
   - **encerrar adesão** deixa de ser modal: a gaveta mostra o benefício e o início e já vem com a data
     de hoje. O botão da linha tem nome acessível "Encerrar adesão de <benefício>".
3. **Treinamentos:** validade vencida ganha a etiqueta "Vencido", como o ASO (ADR-0085).
4. **Desligamento:**
   - destaque com a situação (Ativo ou Desligado, com a data);
   - o botão "+ Registrar cálculo de rescisão" segue a regra de antes: só depois do desligamento e só
     se ainda não houver cálculo;
   - o tipo de desligamento passa a ter rótulo em português.
5. **Histórico funcional:** vira uma linha do tempo (data, tipo em badge colorido, descrição), com o
   valor do ajuste em reais.
6. **Perfil sob demanda:** a rota `/profissionais/perfil` passa a usar `loadComponent`. O bundle inicial
   cai de 1,01 MB para cerca de 860 kB, e o perfil vira um chunk próprio (cerca de 137 kB).
7. **O componente não usa mais `Modal`:** o import sai.
8. **Nenhuma regra muda:** mesmas validações, serviços e recargas.

## Consequências

- As 13 abas do perfil seguem o mesmo padrão das outras telas do RH e do Administrativo.
- O perfil carrega um pouco depois do primeiro clique, uma vez por sessão, em troca de uma abertura do
  sistema mais leve.

## Testes

- **Robot de interface** (`test/ui/rh/perfil/UI_perfil_abas_treinamentos_a_historico.robot`):
  - registrar participação num treinamento do catálogo;
  - aderir a um benefício e encerrar a adesão pelas gavetas;
  - registrar avaliação num ciclo;
  - profissional ativo não oferece o cálculo de rescisão.
- As suítes das partes A e B continuam verdes (11 casos no total).
- Sem backend novo.
