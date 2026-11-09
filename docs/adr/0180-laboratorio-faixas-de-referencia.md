# 0180 — Laboratório: faixas de referência por sexo e idade, críticos e evolução do exame

## Status

Aceita. Fatia **L1** do plano para fechar o domínio 10 (Laboratório e Diagnóstico):

- **L1 (esta):** faixas de referência por sexo e idade, valores críticos e a evolução do exame;
- **L2:** aviso ao solicitante quando o resultado é liberado (com urgência no crítico) e envio ao paciente;
- **L3:** exames de imagem, com o laudo do radiologista;
- **L4:** laudo em PDF com assinatura eletrônica interna e os insumos de coleta baixados do Estoque.

As integrações (equipamentos LIS/HL7/ASTM, PACS, LACEN, SIGTAP/BPA e o certificado ICP-Brasil) **não impedem nenhuma fatia**: cada uma
fica com o ponto de encaixe previsto e entra na lista do domínio 20.

## Contexto

- **Faixa única no catálogo.** O catálogo de exames ([ADR-0093](./0093-laboratorio-backend.md)) tinha uma única faixa de referência, copiada
  para o resultado no registro. A hemoglobina normal de uma mulher adulta é baixa para um homem, e a de um recém-nascido é outra. Com faixa
  única, a interpretação (Normal, Acima, Abaixo) erra para boa parte dos pacientes.
- **Sem valor crítico.** Faltava o valor de pânico, aquele que pede aviso imediato ao médico. Ele é a base do aviso urgente da L2.
- **Sem evolução no tempo.** O prontuário mostrava cada resultado solto. Ver a curva de uma glicemia ou de uma hemoglobina era trabalho
  manual.

## Decisão

1. **Faixa por sexo e idade** (`TB_FAIXA_REFERENCIA_EXAME`), só para exame numérico (422 no de texto):
   - **Sexo:** masculino, feminino ou vazio (vale para os dois). `IGNORADO` não é sexo de faixa (400).
   - **Idade com unidade:** dias, meses ou anos. O mínimo entra na faixa e o máximo não; sem máximo, a faixa não tem limite.
   - **Limites:** mínimo e máximo de referência (ao menos um) e, opcionais, os críticos mínimo e máximo. Os críticos ficam fora da
     referência (400).
   - **Sem sobreposição:** duas faixas do mesmo sexo não podem ter idades em comum (409). Uma faixa de um sexo e outra sem sexo nas mesmas
     idades são aceitas, porque a do sexo vence.
   - **Descrição curta** opcional, por exemplo "Recém-nascido".
   - Um exame com faixas não pode mudar o resultado para texto sem antes removê-las (422).
2. **Faixa geral:** os limites que o catálogo já tinha continuam, agora com os críticos, e valem quando nenhuma faixa se aplica.
3. **Escolha da faixa no registro do resultado:**
   - **Idade:** contada na **data da coleta** da amostra, pelo calendário. "De 12 anos" vale a partir do 12º aniversário.
   - **Ordem:**
     1. a faixa do sexo do paciente que cubra a idade;
     2. a faixa sem sexo que cubra a idade;
     3. a faixa geral.
   - **Casos sem sexo ou sem idade:** sexo ignorado só usa faixas sem sexo; sem data de nascimento, vale a faixa geral. O sistema não supõe
     sexo nem idade.
4. **O resultado guarda a cópia:** referências, críticos e a descrição da faixa usada (por exemplo "Feminino, 12 a 59 anos" ou "Faixa
   geral"). Mudar ou remover a faixa não muda resultado já dado, a mesma regra da ADR-0093.
5. **Interpretação:** passa a ter **Crítico baixo** e **Crítico alto** além de Normal, Acima e Abaixo. O crítico vence o fora da faixa.
6. **Evolução do exame:** `GET /api/v1/prontuario/{paciente}/exame/{exame}/evolucao`.
   - **O que entra:** só resultados liberados e o valor atual de cada pedido; o que foi retificado sai marcado. A ordem é a data da coleta,
     e cada ponto traz a faixa que valeu nele.
   - **Acesso:** fica sob o prontuário porque segue as mesmas regras de acesso que ele: `PRONTUARIO.CONSULTAR`, vínculo assistencial ou
     acesso justificado ([ADR-0076](./0076-prontuario-por-vinculo-assistencial.md)), e leitura auditada.
   - **Tela:** nos exames do paciente, o botão "Evolução" abre uma gaveta com o gráfico em SVG, sem biblioteca nova. O gráfico sombreia a
     faixa de referência e destaca os pontos fora dela e os críticos. A mesma série aparece em tabela.
7. **Telas:**
   - **Catálogo do laboratório:** os críticos da faixa geral e a seção "Faixas por sexo e idade" na gaveta do exame, para incluir, editar e
     remover.
   - **Exames do paciente e laudo:** mostram a faixa usada e os críticos.

## Alternativas descartadas

- **Idade guardada em dias:** foi o primeiro desenho. Converter "60 anos" em dias erra o limite em alguns dias perto do aniversário; com a
  unidade guardada, a comparação usa o calendário. Para checar sobreposição entre faixas de unidades diferentes, a conversão aproximada
  basta.
- **Endpoint da evolução sob `pedido-exame`, com o filtro por unidade:** a evolução é leitura do histórico do paciente, como o prontuário,
  então segue as regras dele, e não as das listas de trabalho do laboratório.
- **Gestação e outras condições clínicas como critério da faixa:** ficam para um próximo critério, se a rede pedir.

## Consequências

- **Migração V65:** a tabela de faixas, os críticos no catálogo e no resultado, a descrição da faixa no resultado e o CHECK da
  interpretação com os dois valores críticos.
- **Implantação aos poucos:** sem faixas cadastradas, tudo funciona como antes, pela faixa geral.
- **Faixas de equipamento (LIS):** quando os equipamentos forem integrados, as faixas que eles mandam entram pela mesma escolha; o item fica
  no domínio 20.
