# 0114 — Comunicação e Educação em Saúde: domínio futuro

## Status

Aceita. **Desenho de domínio futuro, sem implementação.** A implementação vem nas fatias C1 a C6, cada uma com ADR
própria. O detalhe está em [`comunicacao/MODELO-COMUNICACAO.md`](../comunicacao/MODELO-COMUNICACAO.md).

## Contexto

- A análise trazida pelo usuário propõe um domínio que conecta comunicação, educação, campanhas e públicos da saúde,
  sem virar "CMS de notícias".
- Boa parte do que ele toca já foi construído ou decidido:
  - **Programas (ADR-0109)** já define a `Campanha` operacional (vacinação, meta, locais), com consulta pública na
    landing page, e as ações programáticas usadas também pela saúde bucal coletiva (ADR-0111);
  - **o RH já tem `Treinamento` e `ParticipacaoTreinamento`**, com a tela Desenvolvimento;
  - **Emergências (ADR-0113)** deixou a comunicação de risco à população fora do escopo dela;
  - **canais que já existem:** o envio de e-mail (ADR-0081) e a landing page.
- Material em arquivo depende do domínio Documentos, que ainda não existe.

## Decisão

1. **Domínio novo, transversal e futuro: #28 Comunicação e Educação em Saúde.**
   - Organiza conhecimento, campanhas de comunicação, ações educativas e comunicação.
   - **Não é dono** de programas, território, profissionais nem eventos assistenciais.
   - O portal é só um canal.
2. **Quatro finalidades:** comunicação, que informa; educação em saúde, para a comunidade; educação permanente, para
   os profissionais; comunicação de emergência, urgente.
3. **`CampanhaComunicacao`, com nome distinto,** para não colidir com a `Campanha` operacional de Programas. Pode se
   ligar a um programa ou a uma campanha operacional, sem substituí-los.
4. **Conteúdo é diferente de material:**
   - `ConteudoEducativo` é o conhecimento, versionado e com validade;
   - `MaterialEducativo` é cada formato, e formato é atributo, não classe;
   - os arquivos ficam no domínio Documentos.
5. **Validação técnica:**
   - rascunho → revisão técnica → aprovado → publicado → expirado;
   - nada de orientação vencida no ar;
   - acessibilidade prevista (Lei 13.146/2015).
6. **`Comunicado`** é o aviso operacional, com prioridade, público, período, território ou unidade e canais. Pode ser
   **sugerido por fatos de outros domínios**: situação operacional da unidade, campanha operacional, emergência.
7. **A comunicação de emergência é um comunicado com prioridade emergencial.**
   - Emergências sabe *que* precisa comunicar; este domínio sabe *como*.
   - Cobre o item que a ADR-0113 deixou fora do escopo.
8. **Canais são catálogo com adaptadores, sem fornecedor no domínio:**
   - o e-mail reaproveita o envio que já existe;
   - o portal é a landing page;
   - SMS, WhatsApp e push entram por Integrações (#20);
   - o disparo em massa é do adaptador.
9. **Público-alvo simples:** classificação mais território (domínio 21), unidade, equipe e faixa etária. Sem motor de
   segmentação.
10. **Privacidade (LGPD):**
    - segmentar por dado de saúde só para a finalidade de tutela da saúde;
    - **a mensagem nunca revela a condição de saúde;**
    - envio a pessoa identificada respeita consentimento e descadastro.
11. **Ação educativa comunitária é ação programática de Programas (P4).** Este domínio fornece o conteúdo, o material
    e a avaliação.
12. **Educação permanente reaproveita o `Treinamento` e a `ParticipacaoTreinamento` do RH.** Este domínio acrescenta só
    o conteúdo e o material didático e as competências. Sem `Capacitacao` paralela nem EAD.
13. **Avaliação de campanha:** o domínio produz os dados de alcance e participação, e o #19 consolida.
14. **Permissões previstas:** `COMUNICACAO.PUBLICAR`, `CONTEUDO.EDITAR`, `CONTEUDO.REVISAR`. O comunicado emergencial
    segue `EMERGENCIA.COORDENAR`.
15. **Fatias:**
    - **C1 · Comunicação básica:** comunicado, conteúdo com validação, publicação, canal; portal e e-mail.
    - **C2 · Campanhas de comunicação.**
    - **C3 · Educação em saúde.**
    - **C4 · Educação permanente.**
    - **C5 · Multicanal.**
    - **C6 · Avaliação.**
16. **Prioridade: futuro.**
    - Depois de Território, Programas, Vigilância, Saúde Mental e Emergências, porque o domínio consome o que eles
      produzem.
    - A C1 é a exceção possível, por ter valor sozinha.

## Consequências

- **A palavra "campanha" deixa de ser ambígua:** a operacional fica em Programas, a de comunicação neste domínio.
- **Emergências ganha o "como comunicar"** sem um segundo sistema.
- **O RH continua dono** da capacitação e do histórico funcional.
- **Documentos e Integrações (#20) viram pré-requisitos** dos materiais em arquivo e do multicanal.
- **Fora do escopo:**
  - rede social;
  - CMS completo;
  - editor multimídia;
  - marketing;
  - CRM de cidadãos;
  - disparo em massa no domínio;
  - LMS e EAD;
  - analytics próprio;
  - duplicação de Programas, Território ou RH.
