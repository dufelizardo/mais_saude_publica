# 0142 — Indicadores, BI e Gestão: camada de leitura (domínio 19)

## Status

Aceita. **Desenho do domínio**; cada fatia tem ADR própria. Implementadas: BI1 (produção assistencial por unidade) na
[ADR-0143](./0143-bi-producao-assistencial.md) e BI2 (situação operacional) na [ADR-0144](./0144-bi-situacao-operacional.md).

## Contexto

- **O domínio 19 estava só mapeado** no [MAPA-DE-DOMINIOS](../MAPA-DE-DOMINIOS.md): "transforma fatos operacionais em
  informação de gestão, nunca o inverso". Cada tela tinha os próprios números locais.
- **O protótipo `Painel.html` (Sala de Situação)** mostra o que a gestão quer ver junto:
  - a produção do dia;
  - o tempo de espera;
  - a ocupação das agendas;
  - a cobertura vacinal;
  - a produtividade por unidade;
  - os programas;
  - as ausências da escala.

  Os protótipos `Relatorios.html` e `Coordenacoes.html` completam o domínio.
- **Os domínios operacionais já registram o bastante para começar:** Atendimento, Agenda, Programas (com os indicadores
  da [ADR-0141](./0141-programas-metas-e-indicadores.md)), Vigilância e Escalas.
- **A pergunta que abriu o domínio** foi a de atendimentos por dia e por mês na unidade. Ela não é de nenhum programa,
  e por isso não cabe na P6.

## Decisão

1. **Domínio de leitura.** O BI não tem dado próprio e nada é digitado nele. Ele agrega o que os outros domínios
   registram e não grava de volta em nenhum deles.
2. **Só agregados.** Nenhum indicador, série, ranking ou exportação traz paciente. As dimensões são as que os
   registros já têm: unidade, período, tipo, especialidade, categoria do profissional, faixa etária e sexo.
3. **Calculado na hora.** Na escala de um município pequeno ou médio, a consulta direta basta. Se pesar, uma fatia
   futura materializa (tabela agregada ou visão), em ADR própria e sem mudar a API.
4. **Acesso:**
   - a permissão é `INDICADOR.CONSULTAR`, na dimensão de operação, porque não traz conteúdo clínico;
   - vale o escopo por unidade da [ADR-0067](./0067-autorizacao-aplicada-nas-rotas-e-no-escopo.md): quem tem acesso a uma unidade
     vê aquela unidade, e a rede inteira pede acesso na rede;
   - **sem unidade, a consulta soma as unidades do acesso;**
   - **a exportação entra na trilha de auditoria** (como a da [ADR-0082](./0082-exportacao-e-retencao-da-auditoria.md));
   - a leitura de agregado não entra, pela regra das listagens da [ADR-0070](./0070-trilha-de-auditoria.md).
5. **Os números de cada domínio continuam no domínio.** Os indicadores de programa ficam em Programas, o boletim em
   Vigilância. O BI consome, consolida e compara, mas não recalcula com outra regra.

### Fatias

| Fatia | Entrega | Depende de |
|---|---|---|
| **BI1** · produção assistencial ✅ ([ADR-0143](./0143-bi-producao-assistencial.md)) | atendimentos por dia e por mês, distribuições, agenda (faltas, encaixes, absenteísmo), ranking de unidades; Sala de Situação | Atendimento, Agenda |
| **BI2** · situação operacional ✅ ([ADR-0144](./0144-bi-situacao-operacional.md)) | alertas (fila, espera, agenda, escala), tempo de espera pela chegada registrada, ocupação das agendas, ocorrências da escala | BI1, Escalas, chegada do atendimento |
| **BI3** · indicadores da rede | indicadores programáticos (P6) e de vigilância consolidados por unidade e território, com metas | BI1, P6, Vigilância |
| **BI4** · relatórios | catálogo de relatórios com filtros e exportação (`Relatorios.html`) | BI1 a BI3 |
| **BI5** · coordenações e gerência | painel por coordenação e regional (`Coordenacoes.html`) | BI3, estrutura organizacional |

## Consequências

- **O domínio 19 sai de ⬜ para 🟡** com a BI1.
- **A cobertura vacinal da Sala de Situação espera o domínio 24 (Imunização).** Até lá, o bloco fica como "Em breve".
- **Pela [ADR-0116](./0116-marco-mapa-de-dominios-fechado.md), nenhum domínio novo:** relatórios e painéis são fatias
  deste.
