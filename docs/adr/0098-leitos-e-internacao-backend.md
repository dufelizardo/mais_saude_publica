# 0098 — Leitos e internação: backend

## Status

Aceita e implementada (parte 1 de 3). Abre o domínio #12 do [MAPA-DE-DOMINIOS](../MAPA-DE-DOMINIOS.md).
- A tela vem na ADR-0099.
- As internações no prontuário e o vínculo assistencial de quem cuida do internado vêm na ADR-0100.

## Contexto

- O atendimento já tinha o tipo `INTERNACAO`, mas sem leito, sem ocupação e sem alta.
- Hospital e UPA (leitos de observação) precisam saber onde cada paciente está, quais leitos estão livres
  e quanto tempo cada um fica.
- O catálogo administrativo já reservava o rótulo `GESTAO_DE_LEITOS` (ADRs 0032 e 0037).
- Não há protótipo de leitos.

## Decisão

1. **Leito** (`TB_LEITO`):
   - fica num **setor assistencial da própria unidade**. Setor de outra unidade dá 400; setor
     administrativo, de apoio ou técnico dá 422;
   - tem uma identificação única na unidade (ex.: "Enf. 2 · Leito 03"); repetida dá 409;
   - **tipo**, numa versão simplificada da classificação do CNES:
     - clínico, cirúrgico, pediátrico, obstétrico;
     - UTI adulto, UTI pediátrica, UTI neonatal;
     - observação, isolamento;
   - **sexo da enfermaria**: masculino, feminino ou misto;
   - **situação**:
     - **livre**;
     - **ocupado**;
     - **em higienização**: depois da saída do paciente, até alguém registrar a limpeza;
     - **bloqueado**: com motivo, só a partir de livre ou em higienização;
   - não se apaga: sai de uso. Leito ocupado não sai de uso nem muda o sexo da enfermaria.
2. **Internação** (`TB_INTERNACAO`):
   - **dados**:
     - paciente, unidade e leito;
     - atendimento de origem, opcional e do mesmo paciente;
     - médico responsável;
     - CID-10 principal, obrigatório porque é o que a AIH pede;
     - motivo e caráter (eletiva ou urgência);
     - admissão e previsão de alta;
   - **travas**:
     - **uma internação ativa por paciente**: a segunda dá 409, e para mudar de leito se usa a troca;
     - **só em leito livre e em uso**, senão 422. O leito fica travado durante a operação;
     - **sexo compatível**: enfermaria masculina ou feminina só recebe paciente do mesmo sexo, e o
       paciente de sexo ignorado vai para leito misto (senão, 422);
     - no banco, dois índices únicos parciais garantem uma internação ativa por paciente e por leito.
3. **Troca de leito:**
   - só dentro da mesma unidade. Para outra unidade, a saída é a alta por transferência;
   - o leito de destino precisa estar livre e ser compatível;
   - o leito anterior vai para higienização.
4. **Alta**, ato médico:
   - permissão própria, `INTERNACAO.ALTA`;
   - **tipo**: melhorado, a pedido, transferência, evasão ou óbito;
   - **sumário** de 10 a 4000 caracteres;
   - **data**: não pode ser antes da admissão nem no futuro (400);
   - a internação fecha e não muda mais, e o leito vai para higienização.
5. **Movimentos** (`TB_EVENTO_LEITO`), só inclusão: admissão, troca de leito, alta, higienização concluída,
   bloqueio e desbloqueio, com quem e quando.
6. **Leituras:**
   - **mapa** dos leitos do escopo, com paciente, dias internado, previsão de alta e médico de cada leito
     ocupado;
   - **indicadores**:
     - leitos livres, ocupados, em higienização e bloqueados;
     - taxa de ocupação: ocupados sobre leitos em uso e não bloqueados;
     - média de permanência das altas dos últimos 30 dias;
   - **lista de internações**, sem motivo, sumário e movimentos;
   - **detalhe completo**, com leitura auditada.
7. **Acesso:**
   - as permissões novas:
     - `INTERNACAO.CONSULTAR`: ver o mapa e as internações;
     - `INTERNACAO.GERENCIAR`: internar e trocar de leito;
     - `INTERNACAO.ALTA`: dar alta;
     - `LEITO.GERENCIAR`: cadastrar, bloquear e liberar;
   - entram nos papéis padrão:
     - **Médico**: consultar, gerenciar e dar alta;
     - **Enfermeiro** e **Coordenador de enfermagem**: consultar, gerenciar e leito. A alta é do médico;
     - **Técnico de enfermagem**: consultar;
     - **Recepção**: consultar, para informar a família onde o paciente está;
   - papel novo: **Gestor de leitos (NIR)**, o Núcleo Interno de Regulação, com consultar, gerenciar e
     leito.
8. **Migração V6** (ADR-0090), gerada com `ddl-auto=create` num banco descartável.
9. A tela de Usuários & Perfis ganha o módulo **Leitos e internação** e a cor do papel novo. A Auditoria
   ganha os rótulos dos recursos novos.

## Consequências

- O hospital e a UPA passam a ter mapa de leitos, ocupação e permanência, com histórico de cada leito.
- A higienização vira etapa explícita: o leito só volta ao mapa como livre quando alguém registra a
  limpeza.
- **Fora do escopo** (em `PENDENCIAS.md`):
  - AIH, SIH e faturamento;
  - central de regulação de leitos entre unidades (a "regulação de internação" que está pendente na
    Regulação);
  - reserva de leito para cirurgia eletiva;
  - censo diário formal;
  - prescrição e dieta hospitalar;
  - sincronização com os leitos do CNES;
  - restrição de leito por idade (pediátrico e neonatal).

## Testes

- **JUnit** (`LeitoInternacaoControllerTest`):
  - cadastro de leito, setor administrativo e de outra unidade, e identificação repetida;
  - admissão: sexo incompatível, paciente já internado e leito ocupado;
  - troca de leito, mapa e indicadores;
  - alta antes da admissão e com sumário curto, e a alta em si;
  - troca de leito depois da alta;
  - higienização e liberação;
  - detalhe auditado;
  - bloqueio com motivo, e o leito ocupado que não sai de uso.
- **JUnit com autorização ligada** (`InternacaoAutorizacaoControllerTest`):
  - o enfermeiro interna e não dá alta; o médico dá alta;
  - a recepção vê o mapa e não interna;
  - o gestor de leitos de outro hospital não vê, não bloqueia e não lê.
- **Robot de API** (`test/internacao/leito_internacao`): 10 casos.
