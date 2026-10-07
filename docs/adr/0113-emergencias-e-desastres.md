# 0113 — Gestão de Emergências e Desastres: coordenação extraordinária

## Status

Aceita. **Desenho, sem implementação.** A implementação vem nas fatias E1 a E6, cada uma com ADR própria. O detalhe
está em [`emergencias/MODELO-EMERGENCIAS.md`](../emergencias/MODELO-EMERGENCIAS.md).

## Contexto

- A análise trazida pelo usuário propõe um domínio para emergências e desastres. É uma camada de **coordenação
  extraordinária**, ativada quando um evento ultrapassa a operação normal da rede, e não substitui Vigilância,
  Regulação, Estoque, Transporte nem a capacidade da rede.
- O que já existe pesa no desenho:
  - **Organização:** a situação operacional da unidade, que fecha a agenda (ADR-0101);
  - **Leitos:** com bloqueio (ADR-0098);
  - **Farmácia:** a transferência de medicamentos entre unidades (ADR-0059);
  - **Escala:** só aceita turno na unidade de lotação (ADR-0105);
  - **Vigilância:** o surto (ADR-0110);
  - **Território e Programas:** permitem localizar a população e os vulneráveis (ADRs 0108, 0109).
- **Ainda não existem:** Estoque (13) e Transporte (16). A regulação de urgência está pendente.
- **Referências que a análise não trazia:**
  - **ESPIN** (Decreto 7.616/2011);
  - **COE** e **CIEVS** na estrutura de resposta do SUS;
  - **COBRADE**;
  - a Lei 12.608/2012, pela qual a situação de emergência e a calamidade são declaradas pela Defesa Civil.

## Decisão

1. **Domínio novo e transversal: #27 Gestão de Emergências e Desastres.**
   - **É dono da coordenação extraordinária, não dos recursos.**
   - RH, Farmácia e Estoque, Transporte, Regulação, Organização e Leitos e Vigilância continuam donos do que já são.
   - Emergências registra o que coordenou.
2. **Evento, emergência e plano são separados:**
   - `EventoEmergencial` é o fenômeno, e nem todo evento vira emergência;
   - `Emergencia` é a situação de resposta coordenada;
   - `PlanoDeContingencia` é a preparação.
3. **Tipos e níveis são catálogo:**
   - o tipo tem o código COBRADE opcional para desastres;
   - os níveis de resposta são configuráveis;
   - a ESPIN e o decreto de emergência ou calamidade são **referências registradas**. O sistema não declara.
4. **O plano de contingência é configurável e versionado:** critérios de ativação, níveis, responsabilidades, ações
   previstas, recursos, unidades e contatos. Sem classes por tipo de plano.
5. **A ativação é explícita e tem histórico** (`AtivacaoPlano`): nível, autoridade e justificativa. Elevar, reduzir e
   desativar são registros novos e auditados.
6. **A avaliação de impacto é datada e repetível,** lendo dos domínios donos sempre que puder: leitos, situação das
   unidades, estoque da Farmácia, população do Território.
7. **Capacidade emergencial** por unidade e tipo de recurso: normal, emergencial, disponível e reservada. Os leitos
   extras usam o `Leito` que já existe.
8. **Mobilização registra, o dono executa:**
   - **profissional:** a mobilização **autoriza turnos fora da lotação** na Escala enquanto a emergência estiver ativa.
     É uma exceção controlada à ADR-0105, sem lotação falsa e com os alertas mantidos;
   - **medicamento:** vira transferência na Farmácia;
   - **insumos e veículos:** esperam Estoque e Transporte;
   - **unidade afetada:** usa a situação operacional que já existe.
9. **Ações emergenciais** com prioridade, prazo e situação, que podem nascer das ações previstas no plano.
10. **Vulneráveis e deslocados:**
    - a lista de vulneráveis vem de Território e Programas;
    - os pacientes deslocados são registrados com origem e destino;
    - abrigo é local, não domínio.
11. **Desmobilização e relatório pós-evento são obrigatórios:**
    - a desmobilização encerra as exceções de escala;
    - o relatório alimenta a revisão do plano.
12. **Território:** reaproveita o domínio 21. Para desastre, a área afetada pode ser um **polígono arbitrário**.
13. **A Vigilância detecta, Emergências coordena.** O surto pode originar o evento emergencial, sem duplicar a
    investigação.
14. **Eventos de massa são um tipo de emergência planejada** (fatia E6), não um domínio.
15. **Estrutura de resposta:**
    - o domínio dá suporte ao COE;
    - sem comando e controle militarizado;
    - não substitui a Defesa Civil.
16. **Permissões previstas:** `EMERGENCIA.REGISTRAR`, `EMERGENCIA.COORDENAR`, `EMERGENCIA.ATIVAR` e
    `CONTINGENCIA.GERENCIAR`.
17. **Fatias:**
    - **E1 · Núcleo.**
    - **E2 · Contingência.**
    - **E3 · Mobilização.**
    - **E4 · Integração da rede.**
    - **E5 · Pós-evento.**
    - **E6 · Eventos especializados.**

## Consequências

- **O sistema ganha uma camada de gestão de crise e resiliência da rede**, sem virar um segundo sistema de cada área.
- **A Escala ganha a exceção controlada** de turnos fora da lotação por mobilização em emergência.
- **Leitos ganha a marca de leito extra ativável.**
- **Estoque, Transporte e a regulação de urgência** ganham mais um consumidor quando forem implementados.
- **Fora do escopo agora:**
  - sistema da Defesa Civil;
  - meteorologia;
  - desastres ambientais completos;
  - abrigos como domínio;
  - hospital de campanha completo;
  - comando militarizado;
  - comunicação de risco;
  - qualquer duplicação dos domínios donos.
