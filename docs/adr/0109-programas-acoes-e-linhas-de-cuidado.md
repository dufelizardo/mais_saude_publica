# 0109 — Programas, Ações e Linhas de Cuidado: domínio próprio

## Status

Aceita. **Desenho, sem implementação.** A implementação vem nas fatias P1 a P6, cada uma com ADR própria. O detalhe
está em [`programas/MODELO-PROGRAMAS.md`](../programas/MODELO-PROGRAMAS.md).

## Contexto

- Programas de saúde aparecem como "Em breve" em várias telas:
  - **Pacientes:** o indicador "Em programas de saúde", o filtro por programa e a aba Programas;
  - **Agenda (ADR-0091):** o programa na marcação;
  - **Equipes (ADR-0104):** os programas vinculados;
  - **landing page:** a consulta pública.
- O protótipo `Programas.html` mostra:
  - cartões por programa, em abas por categoria (Atenção Básica, Crônicos, Ciclos de vida, Saúde Mental);
  - o detalhe com inscritos, indicadores clínicos, busca ativa dos faltosos, ações pendentes e equipe responsável;
  - a ação **Inscrever paciente**.
- A análise trazida pelo usuário separa **programa, campanha, linha de cuidado, ação programática, serviço e
  política**. Tratar tudo como `Programa` quebraria a arquitetura.
- O usuário pediu que **novos programas e campanhas sejam cadastráveis**.

## Decisão

1. **Domínio novo e transversal: #22 Programas, Ações e Linhas de Cuidado** (MAPA-DE-DOMINIOS).
   - **Referencia** Paciente, Equipe, Unidade, Território, Atendimento e Prontuário, mas não é dono de nenhum deles.
   - Dependência conceitual: Território → Adscrição → Equipe → Programa → Acompanhamento → Ações → Atendimento.
2. **Conceitos separados:**
   - **programa:** organização permanente de ações para um público, uma condição ou um objetivo;
   - **campanha:** com início e fim, meta, público e locais; pode estar ligada a um programa;
   - **linha de cuidado:** percurso assistencial que atravessa domínios;
   - **ação programática:** atividade concreta prevista, realizada pelos serviços;
   - os **serviços** (consulta, vacina, exame, dispensação) continuam nos domínios assistenciais. **Programa não é
     serviço.**
   - **Não haverá entidade "Outros":** uma iniciativa que não cabe em programa, campanha ou linha pede um tipo novo
     numa ADR.
3. **Programas e campanhas são cadastráveis, sem enum.**
   - **`Programa`:**
     - categoria **cadastrável**;
     - abrangência: nacional, estadual ou municipal;
     - situação, período e coordenação;
     - unidades e equipes participantes;
     - periodicidade de acompanhamento;
     - marca de **sensível**;
     - **público-alvo como dado**: faixa etária, sexo, gestante, condição por CID ou CIAP.
   - **`Campanha`:**
     - programa opcional;
     - período e situação (planejada, em andamento, encerrada);
     - público e meta;
     - locais: unidades da rede ou locais temporários.
   - **Carga inicial editável:** os programas do protótipo e os nacionais comuns.
4. **O público-alvo sugere elegíveis e não inscreve sozinho.** A inscrição é ato de profissional.
5. **`AcompanhamentoProgramatico`**, e não "inscrição", porque nem todo programa tem inscrição formal.
   - Campos:
     - paciente e programa;
     - início, fim e motivo de saída;
     - situação: ativo, faltoso, concluído ou encerrado;
     - unidade, equipe e profissional responsáveis.
   - É **histórico**.
   - Com a adscrição do domínio Território (ADR-0108), permite perguntas por microárea e equipe.
6. **Estar num programa é dado de saúde (LGPD, art. 11):**
   - ler os programas de um paciente segue as regras do prontuário: permissão clínica, auditoria de leitura (ADR-0070)
     e vínculo (ADR-0076);
   - **programa sensível** restringe ainda mais a visibilidade;
   - indicadores e consulta pública mostram **só agregados**.
7. **Permissões:**
   - `PROGRAMA.GERENCIAR`: cadastro de programas, categorias e campanhas (operação);
   - `PROGRAMA.ACOMPANHAR`: inscrever, mudar a situação e encerrar (dado de saúde);
   - a leitura do catálogo é aberta a quem está logado.
8. **Fatias:**
   - **P1 · Catálogo:** categoria, programa, público-alvo, participação e carga inicial; tela em cartões pelo
     protótipo.
   - **P2 · Acompanhamento:** inscrever e encerrar; aba Programas e filtro em Pacientes; inscritos e faltosos;
     programas na equipe.
   - **P3 · Campanhas:** cadastro, tela e consulta pública. Sobe à frente das ações por ser catálogo simples e pedido
     explícito.
   - **P4 · Ações programáticas:** ações pendentes, busca ativa e programa na marcação da Agenda.
   - **P5 · Linhas de cuidado:** só quando Atendimento, Regulação e Prontuário estiverem maduros.
   - **P6 · Metas e indicadores programáticos:** alimentam o #19 Indicadores e BI.

## Consequências

- Programas e campanhas passam a ser mantidos pelo município, sem mudança de código.
- As telas de Pacientes, Agenda, Equipes e a landing page ganham de onde ler programas, conforme as fatias.
- Com o domínio Território, o sistema passa a responder perguntas de gestão da APS, como hipertensos por microárea ou
  diabéticos sem acompanhamento por equipe.
- **Fora do escopo agora:**
  - política de saúde como entidade;
  - estratificação de risco;
  - integração com o e-SUS, SISAB e Previne;
  - inscrição automática por regra.
