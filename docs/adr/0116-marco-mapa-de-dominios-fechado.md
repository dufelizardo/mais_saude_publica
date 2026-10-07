# 0116 — Marco: mapa de domínios fechado e regra para novos domínios

## Status

Aceita.

## Contexto

- Entre as ADRs 0108 e 0115, o mapa ganhou os domínios que faltavam:
  - Território, Programas, Vigilância, Imunização, Saúde Bucal, Saúde Mental, Emergências e Comunicação;
  - a camada do Modelo Operacional;
  - Gestão da Rede e Intersetorialidade.
- O usuário trouxe a avaliação de que, com isso, o mapa está **arquiteturalmente completo**. Propôs parar de criar
  domínios e passar a aprofundar os existentes, com uma regra: domínio novo só se uma necessidade real não couber nos
  existentes.
- A conferência do texto contra o repositório confirmou a tese e corrigiu pontos:
  - **"coberto" não é "pronto":** dos 31 itens, 9 funcionam, 5 funcionam em parte, 8 só estão desenhados e 9 só estão
    mapeados;
  - **a lista do texto omitia** o #24 Imunização e o Documentos;
  - **o SAMU do dia a dia não usa o domínio Emergências:** é urgência regular (Regulação, Atendimento, Transporte e
    Modelo Operacional). Emergências entra só em evento extraordinário;
  - **"eventos de domínio"** apareciam como etapa, mas o projeto não decidiu usar mensageria entre domínios.

## Decisão

1. **Marco: o mapa está completo em cobertura funcional macro.**
   - O conjunto fechado são os **31 itens numerados** do [`MAPA-DE-DOMINIOS.md`](../MAPA-DE-DOMINIOS.md) **mais
     Documentos** (transversal, sem número, no mapa desde a ADR-0039).
   - **Completo como mapa, não como desenho nem como produto.** O estado real fica no
     [`STATUS-DOS-DOMINIOS.md`](../STATUS-DOS-DOMINIOS.md):

     | Estado | Itens |
     |---|---|
     | ✅ funcionando | 1 a 9 |
     | 🟡 em parte | 10, 11, 12, 18, 20 |
     | 📐 só desenhado (ADR, sem código) | 21, 22, 23, 25 a 29 |
     | ⬜ só mapeado (sem ADR de domínio) | 13 a 17, 19, 24, 30, 31 e Documentos |

2. **Sinal de que o mapa fechou: o que é novo encaixa no que existe.**
   - **SAMU:** Regulação (urgência), Atendimento, Transporte e Modelo Operacional (perfil de atenção móvel, ponto
     operacional). Emergências só em evento extraordinário.
   - **CAPS:** Saúde Mental, Atendimento, Agenda, Prontuário, Equipe (CAPS multi), Rede, Território e Modelo
     Operacional (modalidade como capacidade).
   - **Hospital:** Modelo Operacional (serviços e capacidades), Rede, Atendimento, Leitos, Enfermagem, Farmácia,
     Laboratório, Regulação.
   - Nenhum vira domínio, e nenhum vira `UBSService` ou `HospitalService`.
3. **Regra para domínio novo.** Uma necessidade nova passa, em ordem, por:
   1. **É dado** (classificação, "tem ou não tem")? → catálogo ou capacidade (ADR-0053; ADR-0115, MO1).
   2. **É um processo dentro de um domínio existente?** → fatia ou especialização dele (ADR-0053, critério 2).
   3. **É configuração de equipamento?** → perfil, capacidade, serviço ou ponto operacional (ADR-0115).
   4. **É interface com outro setor ou sistema?** → Intersetorialidade (#31) ou Integrações (#20).
   5. **Só se nada disso couber,** um domínio novo, com ADR que diga por que **nenhum** dos existentes serve.
4. **Regra inversa:** nenhum domínio vira depósito. Uma fatia que não respeita a fronteira do domínio volta para o
   desenho.
5. **Documentos de fora com outra numeração** são reconciliados por tabela, sem renumerar, como na ADR-0115.
6. **Próxima etapa: aprofundar por fatias, na ordem das dependências.**
   - **Método de cada fatia:** desenho aprovado → ADR (entidades, regras, permissões, API, tela) → código → JUnit e
     Robot de API e de tela → STATUS.
   - **Itens só mapeados (⬜)** ganham ADR de domínio quando entram na fila: o desenho vem antes do código.
   - **A ordem e as dependências** ficam no STATUS (seção "Ordem e dependências das fatias").
7. **Eventos de domínio e mensageria não fazem parte do método.** As integrações entre domínios continuam síncronas e
   auditadas. Adotar eventos será uma decisão própria, com ADR, se houver necessidade.

## Consequências

- **O mapa para de crescer por inércia.** Domínio novo exige justificativa contra os cinco passos.
- **O trabalho passa a ser escolher a próxima fatia** pela ordem das dependências.
- **O STATUS é a fonte do estado real** (mapeado, desenhado, implementado). O mapa é a fonte das responsabilidades.
