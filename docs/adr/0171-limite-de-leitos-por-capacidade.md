# 0171 — Limite de leitos por capacidade

## Status

Aceita e implementada. Complementa a [ADR-0170](./0170-modelo-operacional-dominios-consultam-capacidades.md): a capacidade diz quais tipos de leito a
unidade pode ter; o limite diz quantos.

## Contexto

- **A quantidade de leitos era livre:** uma unidade com a capacidade cadastrava quantos quisesse.
- **O número é responsabilidade da gestão:** gerente, diretor, coordenador e administrador o mantêm atualizado. Por isso, ele pode bloquear, sem
  precisar vir do CNES, que não está integrado.

## Decisão

1. **Leitos autorizados por capacidade da unidade** (`TB_LIMITE_LEITOS`), para as capacidades que têm leitos. Só inclusão: o registro mais recente vale,
   e os anteriores são o histórico, com o valor, a data e o motivo. O CPF de quem alterou fica guardado e na trilha, sem sair na resposta da API.
2. **Sem número, sem limite:** nenhuma unidade muda até a gestão informar.
3. **Bloqueio (422)** ao cadastrar um leito, reativar um fora de uso ou trocar o tipo para outra capacidade. Trocar entre tipos da mesma capacidade, como UTI
   adulto e pediátrica, não confere.
4. **Contam os leitos ativos,** inclusive bloqueados e em higienização. Pediátrico e isolamento contam em Internação clínica, como na ADR-0170.
5. **Reduzir abaixo dos ativos é recusado (422);** primeiro se tiram os leitos de uso. O mesmo valor também é recusado (422).
6. **Os leitos extras da capacidade emergencial ficam de fora** (ADR-0158).
7. **Permissão `LEITO.AUTORIZAR`,** separada de `LEITO.GERENCIAR`: quem cadastra leitos não muda o limite. Vai para Gestor e Administrador da plataforma, na
   unidade do acesso. A leitura acompanha quem vê leitos e Equipamentos.

### API

- `GET /api/v1/unidade-saude/id/{uuid}/limites-de-leitos`: por capacidade vigente, os ativos, os autorizados e o histórico;
- `POST .../limites-de-leitos`: capacidade, quantidade e motivo.

### Telas

- **Equipamentos, aba Capacidades:** bloco "Leitos autorizados", com "8 de 10 leito(s)", o botão Alterar limite e o histórico na gaveta.
- **Leitos, gaveta de cadastro:** mostra o limite da capacidade do tipo escolhido.

### Migração

`V56__limite_de_leitos.sql`: a tabela. Nenhuma unidade recebe limite.

## Consequências

- **O limite depende de a gestão mantê-lo:** ampliou, atualiza antes de cadastrar.
- **Enquanto a exigência de permissão estiver desligada,** qualquer pessoa logada altera o limite.
- **Dois cadastros simultâneos** podem passar do limite por um leito: o risco é baixo, e o número corrige na próxima conferência.
- **Ficam para depois:** o número vindo do CNES, limite por setor ou por serviço e limite de outros recursos.

## Testes

- **JUnit:** `LimiteLeitosControllerTest`: sem número, o cadastro para no limite, fora de uso e reativação, troca de tipo, redução, histórico, capacidade ausente ou
  sem leitos, motivo e quantidade; `LimiteLeitosAutorizacaoControllerTest`, com a segurança ligada: o gestor altera na sua unidade, o gestor de outra não, e o NIR só lê.
- **Robot:** caso 13 de `LeitoInternacao.robot` e caso 9 de `UI_leitos.robot`.
