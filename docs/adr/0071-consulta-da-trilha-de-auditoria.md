# 0071 — Consulta da trilha de auditoria

## Status

Aceita e implementada. Fatia 2 de 2 da auditoria; o registro é a
[ADR-0070](./0070-trilha-de-auditoria.md).

## Contexto

A ADR-0070 passou a registrar quem leu o detalhe de um dado de saúde, quem alterou o quê, quem teve
acesso negado e os logins. Um registro que ninguém consegue ler não responde às perguntas que motivaram
a trilha:
- quem abriu o prontuário deste paciente?
- o que esta pessoa acessou?
- houve tentativa indevida?

Falta decidir três coisas: **quem** pode ler a trilha, **de onde** ela é lida e **como** as consultas
ficam registradas.

## Decisão

1. **Permissão `AUDITORIA.CONSULTAR`** no catálogo (ADR-0066; agora são 23 permissões), na dimensão
   **administração do sistema**.
   - **Não está em nenhum perfil padrão**, nem no Administrador da plataforma: quem administra o sistema
     não audita a si mesmo.
   - A auditoria é concedida por um perfil próprio (ex.: "Auditor", criado em Usuários & Perfis) a quem
     tem essa função (ouvidoria, controle interno, coordenação).
   - Por ser da dimensão administração do sistema, só quem gerencia acesso na **rede inteira** consegue
     concedê-la (ADR-0067).
2. **`GET /api/v1/auditoria/`**:
   - filtros opcionais: CPF de quem fez, paciente, registro, unidade, ação, resultado e período em dias
     (fuso de Brasília);
   - ordem do mais recente ao mais antigo, paginado (até 100 por página);
   - devolve o **total** e um **resumo do filtro inteiro**: leituras, alterações, negados, logins e
     logins recusados;
   - traz os nomes de quem, do paciente e da unidade, buscados de uma vez para a página;
   - período invertido ou página inválida → **400**; filtro sem resultado → página vazia (**200**).
3. **Escopo.** Quem audita uma unidade vê os eventos dela e das unidades abaixo. Eventos sem unidade
   (login, cadastros de rede) só aparecem para quem audita a **rede inteira**.
4. **Consultar a auditoria também entra na trilha.** A própria rota é `@AuditarLeitura`, então fica
   registrado quem olhou a auditoria e quando.
5. **Onde ela aparece:**
   - **Tela Auditoria** (`/administracao/auditoria`, no grupo Administração):
     - indicadores do filtro;
     - filtros por CPF, paciente (busca por nome, CPF ou cartão SUS), ação, resultado e período;
     - tabela paginada;
     - gaveta do evento, com o motivo da recusa, IP, rota, registro e os atalhos "Tudo desta pessoa" e
       "Tudo deste paciente".
     - Aceita `?usuarioCpf=` e `?pacienteId=`, para chegar já filtrada.
   - **Usuários & Perfis:** a gaveta do usuário ganha "Ver na auditoria".
   - **Atendimento:** a gaveta ganha **"Quem acessou os dados deste paciente"**, com os 10 últimos eventos
     e um link para a trilha completa. Carrega só quando se clica em "Ver", para abrir o atendimento não
     virar uma consulta à auditoria.
   - A matriz de permissões ganha o módulo **Auditoria** (agora são 10 módulos, ADR-0068).
   - O menu mostra a tela só para quem tem `AUDITORIA.CONSULTAR`, quando a autorização está ligada.
   - **Exportar** fica "Em breve".

## Trade-offs considerados

**Consulta com Criteria (escolhida)** × **Specification do Spring Data**
- ✅ `JpaSpecificationExecutor` traz um `delete(Specification)`. A Criteria direta no serviço mantém o
  repositório da trilha sem nenhuma forma de apagar (ADR-0070).

**Auditoria fora dos perfis padrão (escolhida)** × **no Gestor ou no Administrador**
- ✅ Segregação de funções: quem concede acesso ou opera a unidade não é quem fiscaliza.
- ❌ Ninguém audita até alguém criar o perfil e concedê-lo. O
  [guia](../acesso/GUIA-LIGAR-AUTORIZACAO.md) mostra como.

**"Quem acessou" sob pedido (escolhida)** × **sempre carregado na gaveta**
- ✅ Abrir um atendimento não gera, a cada vez, uma leitura da auditoria na trilha.

## Consequências

**Positivas:**
- A trilha responde as perguntas da LGPD, com escopo e com registro de quem consultou.
- A decisão de deixar o prontuário na rede inteira (ADR-0067) passa a ter fiscalização efetiva.

**Negativas / pendências:**
- ~~Exportação~~ em CSV e ~~retenção~~ de 20 anos feitas pela [ADR-0082](./0082-exportacao-e-retencao-da-auditoria.md); PDF formatado fica para quando houver pedido formal que o exija.
- Alertas (ex.: muitas recusas seguidas, leituras fora do horário da escala).

## Referências

- [ADR-0070](./0070-trilha-de-auditoria.md): o registro.
- [ADR-0066](./0066-papeis-permissoes-e-escopo-por-unidade.md): catálogo de permissões e perfis.
- [ADR-0067](./0067-autorizacao-aplicada-nas-rotas-e-no-escopo.md): escopo por unidade.
- [ADR-0068](./0068-tela-usuarios-e-perfis.md): Usuários & Perfis e a matriz.
