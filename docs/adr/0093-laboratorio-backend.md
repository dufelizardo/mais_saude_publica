# 0093 — Laboratório assistencial: backend

## Status

Aceita e implementada (parte 1 de 3). Abre o domínio #10 do [MAPA-DE-DOMINIOS](../MAPA-DE-DOMINIOS.md).
- A tela vem na ADR-0094.
- Os resultados no prontuário e no atendimento, o laudo imprimível e a recoleta pela tela vêm na ADR-0095.

## Contexto

- A consulta só tinha um texto livre, `examesSolicitados`: o pedido não seguia para lugar nenhum e o
  resultado não voltava.
- O DER esboça o fluxo atendimento → pedido → coleta → amostra → laboratório → resultado → laudo →
  prontuário.
- O levantamento de equipamentos separa o laboratório **assistencial** (`TipoUnidadeDeSaude.LABORATORIO`)
  do **LACEN**, que é vigilância laboratorial.

## Decisão

1. **Escopo:** o laboratório assistencial (análises clínicas), da coleta ao resultado liberado.
   - O LACEN fica de fora, como função de vigilância.
   - Exames de imagem e o que a rede não faz seguem pela Regulação (procedimento do tipo exame).
2. **Catálogo de exames** (`TB_EXAME_LABORATORIAL`):
   - campos: nome único, material, preparo, prazo em dias e ativo;
   - resultado **numérico**, com unidade e faixa de referência (mínima maior que a máxima dá 400), ou em
     **texto**, com uma referência textual;
   - não se apaga: sai de uso.
3. **Pedido** (`TB_PEDIDO_EXAME`):
   - registra paciente, atendimento de origem (opcional, do mesmo paciente), unidade e profissional
     solicitantes, indicação clínica, CID opcional e prioridade (rotina ou urgente);
   - o pedido não muda depois;
   - **cada exame é um item com situação própria:** Solicitado → Coletado → Resultado registrado →
     Liberado, ou Cancelado;
   - exame repetido no pedido dá 400; exame fora de uso, 422.
4. **Coleta e amostra** (`TB_AMOSTRA_EXAME`):
   - a coleta gera **uma amostra por material**, com código para a etiqueta (`AM` + data + 4
     caracteres);
   - a coleta diz **qual laboratório analisa** (por padrão, a própria unidade da coleta). É esse
     laboratório que decide o escopo de quem analisa e libera.
   - **Amostra rejeitada** (hemolisada, insuficiente, coagulada, identificação incorreta, outro) não se
     apaga. Os exames dela, se ainda não liberados, voltam a aguardar coleta. A recoleta gera amostra
     nova só para eles.
5. **Resultado** (`TB_RESULTADO_EXAME`), sempre como **registro novo**:
   - o item aponta para o resultado atual;
   - unidade e faixa são **copiadas do catálogo** no registro, então mudar o catálogo não muda resultado
     dado;
   - a **interpretação** (normal, acima, abaixo) é calculada;
   - antes da liberação, registrar de novo grava outro resultado e guarda o anterior.
6. **Liberação e retificação:**
   - quem responde tecnicamente libera, e o resultado não muda mais;
   - correção é **retificação** com motivo: grava outro resultado, ligado ao corrigido, já liberado por
     quem retificou;
   - a mesma pessoa pode analisar e liberar, para servir a laboratórios pequenos. Os dois nomes ficam
     registrados.
7. **Cancelamento:** de um exame ainda não liberado, por quem pediu ou pelo laboratório, com motivo.
8. **Eventos** (`TB_EVENTO_EXAME`): solicitação, coleta, rejeição, resultado, liberação, retificação e
   cancelamento, com quem e quando.
9. **Leitura:**
   - **pedidos**, sem valores, com a situação calculada (aguardando coleta, em andamento, concluído,
     cancelado);
   - **lista de trabalho** por exame (para coletar, em análise, para liberar), com urgente primeiro;
   - **detalhe** com indicação clínica, resultados, amostras e eventos. Leitura auditada; só no escopo da
     unidade solicitante, da coleta ou do laboratório.
10. **Acesso:**
    - `EXAME.SOLICITAR`: Médico e Enfermeiro (protocolos da atenção básica e do pré-natal).
    - `LABORATORIO.COLETAR`: Enfermeiro, Técnico de enfermagem e os papéis de laboratório.
    - `LABORATORIO.ANALISAR`: papel novo **Técnico de laboratório**.
    - `LABORATORIO.LIBERAR` e `LABORATORIO.GERENCIAR` (catálogo): papel novo **Responsável técnico do
      laboratório**.
    - As permissões novas entram nos papéis padrão que já existiam (ADR-0087).
11. **Migração V4** (ADR-0090), com índices da lista de trabalho e dos pedidos do paciente.
12. **Tela de Usuários & Perfis:** módulo Laboratório na matriz e cores dos papéis novos. A Auditoria ganha
    os rótulos dos recursos novos.

## Consequências

- O pedido de exame passa a ter dono, situação e resultado rastreável, da coleta à liberação.
- Até a ADR-0095, o resultado liberado está no detalhe do pedido, mas ainda não no prontuário.
- **Fora do escopo** (registrado em `PENDENCIAS.md`):
  - LACEN e vigilância;
  - integração com os equipamentos (LIS, HL7, ASTM);
  - controle de qualidade;
  - insumos de coleta (Estoque, #13);
  - exames de imagem;
  - laudo PDF assinado;
  - SIGTAP e faturamento (BPA);
  - valores de referência por sexo e idade.

## Testes

- **JUnit** (`PedidoExameControllerTest`):
  - catálogo;
  - pedido com exame repetido e fora de uso;
  - coleta por material;
  - resultado numérico com interpretação;
  - liberação que trava o resultado;
  - retificação que guarda o anterior, e leitura auditada;
  - resultado em texto, cancelamento e listagem sem valores;
  - rejeição e recoleta;
  - urgente primeiro.
- **JUnit com autorização ligada** (`LaboratorioAutorizacaoControllerTest`):
  - o técnico do laboratório analisa, e o de outro laboratório não;
  - só o responsável técnico libera;
  - a recepção não lê, e o médico não coleta.
- **Robot de API:** 19 casos em `test/laboratorio/`.
- **Achado durante os testes:** o resultado era salvo antes de receber o motivo da retificação, e as
  colunas imutáveis não eram gravadas no segundo `save`. Agora o resultado é gravado de uma vez.
