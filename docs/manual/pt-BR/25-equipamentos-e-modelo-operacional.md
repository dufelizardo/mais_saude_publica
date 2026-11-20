# Equipamentos de Saúde e modelo operacional

> **Versão:** 1.15.0 · [English](../en/25-equipamentos-e-modelo-operacional.md)

Menu: **Administrativo › Equipamentos de Saúde** e **Administrativo › Modelo operacional**.

- **Equipamentos de Saúde:** as unidades da rede (UBS, UPA, hospital, CAPS…) e os níveis de gestão, com o mapa da rede e a ficha de
  cada unidade.
- **Modelo operacional:** o catálogo do que as unidades operam e conseguem fazer: os perfis, as capacidades e os tipos de
  serviço.

**Quem usa:** quem mantém a estrutura (o administrador da plataforma) e quem lê o administrativo (o gestor).

## 1. A rede de unidades

**Os cartões do alto:**

- as **Unidades ativas** e as **UBS;**
- as **UPA / Hospital / CAPS e especializadas;**
- as **Em manutenção / obras / inoperantes**, sem vagas na agenda.

**Buscar e filtrar:** pela unidade ou pelo CNES, e pela **Regional**, pelo **Tipo** (unidades de atendimento ou níveis de
gestão), pela **Situação** e pelo **Perfil operacional**.

**Ver no Mapa ou na Lista.** As unidades sem coordenadas ficam só na lista. Marque a localização na ficha, em **Dados gerais**.

**Exportar** baixa a lista.

## 2. A ficha da unidade

Clique na unidade para abrir a ficha, com as abas:

| Aba | O que tem |
|-----|-----------|
| **Dados gerais** | o nome, o CNES (7 dígitos), o tipo (não muda pela edição), o endereço, o contato, o responsável (pelo CPF) e a localização no mapa |
| **Operacional** | o horário de funcionamento, a capacidade (leitos e consultórios) e a situação |
| **Capacidades** | os perfis operacionais e as capacidades da unidade, e os **Leitos autorizados** |
| **Serviços** | como a unidade se organiza: UTI Adulto, Sala de vacina, Pronto-socorro… |
| **Pontos** | onde a unidade atua fora do endereço (escola, praça) e as **Rotas programadas**; e, na unidade móvel, **Onde está agora** |
| **Equipes** | os profissionais lotados e as equipes (cadastradas em Recursos Humanos › Equipes) |
| **Vinculações** | a hierarquia: a unidade superior e a supervisão regional; os setores e o mapa de leitos |
| **Histórico** | as mudanças de situação, com **Ver tudo na auditoria** |

**Cadastrar ou editar:**

1. Informe o **Nome**, o tipo, o **CNES** e o **Nível de gestão**. Só aparece o nível acima do tipo escolhido; para a unidade de
   atendimento, o municipal.
2. Preencha o endereço e o contato.
3. Em **CPF do responsável**, informe o CPF. Ele liga ao profissional de mesmo CPF; sem cadastro, fica pendente até ele existir.
4. Em **Localização**, clique no mapa ou digite as coordenadas (WGS84). Sem coordenadas, a unidade não aparece no mapa da rede nem
   no do território.

**Horário de funcionamento:**

1. Use **Editar horário** e, para cada dia, marque **Aberto** e informe o **1º turno** e, se quiser, o **2º turno**. Para um
   intervalo de almoço, use dois turnos no dia.
2. A agenda só oferece vagas dentro dos turnos.
3. **Funciona 24 horas** vale para hospital e UPA: aí o horário não fecha a agenda.

> Sem horário estruturado, a agenda não fecha pelo horário da unidade.

**Mudar situação:** use **Mudar situação**, com o **Status operacional** (por exemplo, em manutenção ou em obra), o **Motivo** e a
**Previsão de retorno**. A unidade fora de operação não oferece vagas, e os compromissos dela aparecem nas emergências.

**Perfis e capacidades da unidade:**

1. **Adicionar perfil:** o que a unidade opera (atenção primária, urgência, internação…), com a data **Desde**. Uma unidade pode
   ter vários perfis: um hospital é internação e urgência.
2. **Adicionar capacidade:** só as transversais (acolhimento, demanda espontânea) e as dos perfis que a unidade tem. Use a
   **Observação** para, por exemplo, a portaria de habilitação.
3. **Leitos autorizados** (gestão): o número de leitos autorizados por capacidade. Use **Alterar limite**. O cadastro de leitos
   acima dele é bloqueado.

**Pontos e posição:**

- **Adicionar ponto** e **Adicionar rota** mostram onde a unidade atua fora do endereço.
- Na unidade móvel, **Registrar posição** informa onde ela está. A posição é informada por quem opera; não há rastreamento.

## 3. O catálogo do modelo operacional

**Os cartões:**

- os **Perfis ativos** (atenção primária, urgência, internação…);
- as **Capacidades ativas;**
- os **Perfis em unidades:** as associações vigentes.

**As seções:**

- **Perfis operacionais:** o que uma unidade opera.
- **Capacidades assistenciais:** o que uma unidade consegue fazer. A de um perfil só entra na unidade que tem o perfil. Sem perfil
  (grupo), a capacidade vale em qualquer unidade de atendimento. O perfil da capacidade não muda enquanto ela estiver em alguma
  unidade.
- **Tipos de serviço:** como a capacidade se organiza na unidade. O tipo pode exigir uma capacidade: aí o serviço só entra na
  unidade que tem essa capacidade, e ela não muda enquanto houver serviço vigente do tipo.

Cada item tem o **Código** (guardado em maiúsculas), o **Nome** e a **Descrição**. O item inativo continua no catálogo e nas
unidades que já o têm, mas não entra em unidade nova.

> As capacidades decidem várias telas: a central do 192 precisa da *Regulação médica de urgência*, o laudo de imagem precisa de
> diagnóstico por imagem, e a Regulação confere a capacidade da unidade executante.

## 4. Mensagens comuns

| Situação | O que quer dizer | O que fazer |
|----------|------------------|-------------|
| *Sem coordenadas: a unidade não aparece no mapa…* | a unidade não tem localização | marque no mapa em Dados gerais |
| *Sem horário estruturado…* | a unidade não tem horário de funcionamento | cadastre o horário |
| *Nenhum perfil…* | a unidade não tem perfil operacional | adicione o perfil |
| *Não foi possível carregar as equipes (é preciso ter acesso ao RH ou às equipes).* | o seu perfil não lê o RH | peça o perfil, se a sua função precisa |
