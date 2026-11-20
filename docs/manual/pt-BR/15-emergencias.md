# Emergências e desastres

> **Versão:** 1.15.0 · [English](../en/15-emergencias.md)

Menu: **Vigilância › Emergências**. Cobre o ciclo de uma emergência em saúde pública ou de um desastre, como uma enchente, uma
epidemia ou um evento de massa:

1. o **evento** registrado;
2. a **emergência** ativa, com o nível de resposta;
3. o impacto na rede, as ações, as mobilizações e as pessoas afetadas;
4. a desmobilização, o encerramento e o relatório pós-evento.

**Quem usa:**

| Perfil | O que faz |
|--------|-----------|
| Gestor, coordenação de enfermagem, vigilância epidemiológica, ambiental e sanitária | registram eventos |
| Gestor | converte o evento em emergência, muda o nível, ativa planos e encerra |
| Gestor e coordenação de enfermagem | coordenam: ações, mobilizações, capacidade e pessoas |
| Gestor | mantém os planos de contingência, a capacidade emergencial e os catálogos |

A tela tem as abas:

- **Emergências ativas**, **Eventos** e **Encerradas;**
- **Planos de contingência**, **Capacidade emergencial** e **Catálogos.**

## 1. Registrar um evento

1. Clique em **Registrar evento**.
2. Escolha o **Tipo** e a **Fonte**: vigilância, unidade de saúde, Defesa Civil, gestor, profissional ou sistema externo.
3. Informe o **Local** (por exemplo, *Bairro Ribeirinho*) e **Ocorrido em** (padrão: agora).
4. Escreva a **Descrição**.
5. Se quiser, desenhe a área do evento: o polígono no mapa.
6. Salve.

O evento que veio de um surto da Vigilância aparece **Ligado ao surto da Vigilância**.

**Na aba Eventos**, cada evento tem:

- **Monitorar;**
- **Descartar**, com o motivo;
- **Converter em emergência.**

## 2. Converter em emergência (gestor)

1. No evento, clique em **Converter em emergência**.
2. Informe o **Nome da emergência** (por exemplo, *Enchente Região Norte*), o **Nível inicial**, a **Matrícula do responsável** e a
   **Justificativa**.
3. Para um evento que ainda não começou (por exemplo, um evento de massa), marque **Planejar**. A emergência fica planejada:
   preencha os dados e as estruturas, e use **Iniciar operação** na hora.
4. Confirme.

O território afetado começa pela área do evento e pelas microáreas que ela atinge.

## 3. A emergência ativa

Abra a emergência para ver o nível, o início e o último impacto. As ações principais são:

| Ação | O que faz |
|------|-----------|
| **Nova avaliação** | registra o impacto: a população afetada, o nível de impacto, as unidades afetadas, os leitos e os profissionais indisponíveis, os medicamentos críticos, os equipamentos, o transporte e a infraestrutura |
| **Território** | desenha ou ajusta o polígono; as microáreas que ele atinge entram no território afetado |
| **Mudar nível** | o novo nível de resposta, com a justificativa; fica no **Histórico de nível** |
| **ESPIN e decreto** | registra o número e a data da ESPIN e do decreto publicados |
| **Ativar plano** | o plano de contingência vigente, com a autoridade (por exemplo, *Secretário de Saúde*) e a justificativa |

> **ESPIN e decreto:** o sistema registra; declarar é ato do poder público e da Defesa Civil.

O plano ativado que não cobre o nível atual aparece com o aviso **Não cobre o nível atual**.

## 4. Coordenar a resposta

**Ações:**

- **Gerar ações do plano** traz as ações previstas para o nível;
- **Nova ação** cria uma ação avulsa, com a prioridade e o prazo.

Cada ação tem **Iniciar**, **Concluir** e **Cancelar**. A marca **Atrasada** avisa o que passou do prazo.

**Mobilização:**

1. Use **Mobilizar** para pôr profissionais, equipes ou veículos na emergência.
2. O profissional mobilizado tem o turno autorizado na Escala.
3. A transferência de paciente pode ser ligada à emergência (**Ligar transferência**).

**Capacidade emergencial:** **Ativar capacidade** ativa os leitos ou os recursos a mais que a unidade pode abrir (item 7).

**Estoque:** as reservas são feitas pelo estoque de cada unidade (Administrativo › Estoque). O consumo marcado com a emergência
aparece aqui.

## 5. Pessoas afetadas

- **Vulneráveis da área afetada:**
  - as pessoas do território afetado, com os marcadores: idoso (60+), criança (menos de 5), gestante, em programa, diálise ou
    terapia seriada e oxigênio;
  - filtre por marcador ou marque **Incluir quem não tem marcador;**
  - **Baixar CSV** gera a lista para as equipes de campo;
  - **Deslocar** registra que a pessoa saiu de casa.
- **Deslocados:**
  - **Registrar deslocado** guarda quem está fora de casa, com o local: domicílio, unidade de saúde ou abrigo;
  - **Realocar** muda o local;
  - o retorno encerra o deslocamento.
- **Compromissos nas unidades afetadas:** os agendamentos e as regulações dos próximos 30 dias nas unidades fora de operação. Quem
  redireciona é a Agenda ou a Regulação.
- **Regulações priorizadas:** a regulação de um deslocado pode ser priorizada. **Retirar** tira a prioridade.

## 6. Desmobilizar, encerrar e o relatório pós-evento

- **Iniciar desmobilização:**
  - conclui as mobilizações em aberto, e a Escala deixa de aceitar turnos novos sem lotação;
  - desativa a capacidade emergencial e os planos;
  - depois, só se conclui o que ficou aberto e se escreve o relatório.
- **Encerrar** (direto): conclui as mobilizações e desativa a capacidade e os planos, com o **Motivo do encerramento**. Para
  controlar o retorno passo a passo, use **Iniciar desmobilização**.
- **Cancelar evento:** a emergência planejada que não começou é cancelada, e as estruturas planejadas são desativadas.

**Relatório pós-evento** (emergência encerrada):

1. O relatório mostra os números:
   - a duração e o tempo do evento à ativação;
   - as ações concluídas no prazo e a capacidade ativada;
   - o público estimado do evento.
2. Escreva o **Resumo**, os **Problemas** e as **Lições aprendidas**.
3. Use **Adicionar recomendação**, ligada ou não a um plano (**Revisar plano**).
4. Use **Salvar rascunho** e, ao fim, **Finalizar**. **Exportar números (CSV)** baixa os números.

## 7. Planos, capacidade e catálogos (gestor)

**Planos de contingência:**

1. **Novo plano:** o **Código**, o **Nome**, o **Tipo de emergência**, a vigência, o objetivo e as unidades.
2. Para cada nível, os **Critérios e ações**, os **Recursos necessários** e os **Contatos**.
3. O plano passa por rascunho, **Publicar**, **Revisar** (nova versão) e **Revogar** (com o motivo).

O filtro **Em uso** / **Todas as versões** mostra as versões.

**Capacidade emergencial:** a capacidade que cada unidade pode ativar numa emergência.

1. **Nova capacidade:** a **Unidade**, o **Recurso**, a **Capacidade normal** e quanto é **Ativável a mais**.
2. A capacidade inativa não pode ser ativada em emergência.

**Ativar é na emergência, pela coordenação.**

**Catálogos:**

- **Tipos de emergência:** o código, o nome, a categoria (desastre ou saúde), o COBRADE e o perfil. O evento de massa é planejado
  antes de acontecer e tem os dados do evento.
- **Níveis de resposta:** o nível, a descrição e a ordem.

O inativo não entra em evento nem em mudança de nível novos.

## 8. Mensagens comuns

| Mensagem | O que quer dizer | O que fazer |
|----------|------------------|-------------|
| *Informe o tipo, a fonte e a descrição.* | o evento está incompleto | preencha os três campos |
| *Informe o motivo do descarte.* | o descarte está sem motivo | escreva o motivo |
| *Informe o nome, o nível e a justificativa.* | a conversão está incompleta | preencha os três campos |
| *Escolha o nível e justifique.* | a mudança de nível está incompleta | escolha o nível e justifique |
| *Justifique o início da operação.* | a emergência planejada está sem a justificativa | escreva a justificativa |
| *Escolha o plano e informe a autoridade e a justificativa.* | a ativação do plano está incompleta | preencha os três campos |
| *Escolha o nível de impacto.* | a avaliação está sem o nível | escolha o nível de impacto |
| *Informe a unidade, a capacidade normal e quanto se pode ativar.* | a capacidade está incompleta | preencha os campos |
| *Encerre a emergência antes* | o relatório só fecha depois do encerramento | encerre a emergência |
| *Seu acesso não inclui esta operação.* | o seu perfil não permite a ação | peça o perfil, se a sua função precisa |
