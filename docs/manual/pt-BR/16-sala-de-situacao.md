# Sala de Situação

> **Versão:** 1.15.0 · [English](../en/16-sala-de-situacao.md)

Menu: **Sala de Situação** (no alto do menu). É o painel de gestão da rede, calculado na hora a partir dos registros:

- a produção assistencial e a situação operacional;
- os programas, a vigilância, a vacinação e o custo por unidade.

**Só agregados, sem paciente.**

**Quem usa:** quem consulta indicadores: o gestor, a coordenação de enfermagem e o Núcleo de Segurança do Paciente.

## 1. Escolher o recorte

1. Em **Unidade**, escolha uma unidade ou **Todas do meu acesso**.
2. Em **Até**, escolha o fim do período. Os números são do período que termina nessa data.
3. **Exportar** baixa a produção assistencial em CSV. A exportação entra na auditoria.

No alto, os **avisos** do momento levam à tela de origem, por exemplo:

- **Ver emergência** e **Ver evento:** as emergências com ações atrasadas e os eventos;
- **Ver índices** e **Ver amostras:** os índices do *Aedes* e a qualidade da água, em Zoonoses e vetores;
- **Ver medidas:** as interdições da Vigilância sanitária perto do fim;
- **Ver na vigilância:** os alertas da saúde do trabalhador;
- **Ver na manutenção** e **Ver no painel:** o Patrimônio e as Compras.

## 2. Produção

Os cartões da produção mostram:

- os **Atendimentos concluídos** e a **Média diária;**
- as **Marcações na agenda** e as **Faltas registradas;**
- o **Absenteísmo** (as faltas sobre as realizadas e as faltas);
- os **Encaixes realizados.**

Os gráficos mostram:

- os atendimentos por **Dia** ou por **Mês** e os **Atendimentos por hora;**
- o atendimento por faixa etária (a idade no dia do atendimento);
- os atendimentos por tipo e pelo conselho do profissional;
- as oito especialidades com mais atendimentos, por sexo.

## 3. Agora e tempo de espera

- **Agora:** os atendimentos em andamento nas últimas 24 horas:
  - quantos estão em atendimento;
  - quantos aguardam triagem e quantos aguardam consulta;
  - a maior espera.
- **Tempo de espera:** a média, a mediana e o percentil 90 da chegada registrada até o primeiro contato e até a consulta.

## 4. Escala e agendas

- **Ocorrências da escala:** nos próximos 7 dias, os turnos sem profissional ou com o profissional afastado. **Ver escala completa
  →** abre a Escala.
- **Ocupação das agendas:** as vagas ofertadas e ocupadas dos próximos 7 dias.

## 5. Programas, vigilância e vacinação

- **Programas de saúde:**
  - os pacientes acompanhados e os acompanhados em dia, por programa;
  - clique no programa para ver por unidade;
  - **Gerenciar →** abre Programas de Saúde.
- **Vigilância:** as notificações do período, as encerradas no prazo e o recorte por território. **Abrir →** abre a Vigilância.
- **Vacinação:**
  - as doses aplicadas no período e a cobertura de hoje pelo cadastro;
  - as vacinas com abandono alto e as campanhas de vacinação;
  - **Ver sala de vacina** abre a Sala de Vacina.

## 6. Produtividade e custo

- **Produtividade por unidade:** os atendimentos concluídos no período e a variação sobre o período anterior.
- **Custo por unidade:**
  - o custo por atendimento da competência anterior e, quando há, o custo por paciente-dia;
  - a marca **acima da média** avisa a unidade 30% ou mais acima da média do tipo de unidade.

## 7. Mensagens comuns

| Mensagem | O que quer dizer |
|----------|------------------|
| *Sem atendimentos no período.* | não há atendimento concluído no recorte escolhido |
| *Nenhuma ocorrência na escala dos próximos 7 dias.* | a escala está completa |
| *Nenhum programa oferecido pelas unidades do seu acesso.* | as unidades do recorte não oferecem programa |
| *Nenhum custo apurado nas unidades do seu acesso.* | a competência anterior ainda não foi apurada no Financeiro |
