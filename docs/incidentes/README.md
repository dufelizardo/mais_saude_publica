# Incidentes

Registro dos incidentes dos ambientes: o que aconteceu, a causa, como foi resolvido e o que ficou de acompanhamento. Cada
incidente tem também uma issue no GitHub com a etiqueta `bug`.

| Data | Incidente | Correção |
|---|---|---|
| 2026-11-14 | [API fora nos quatro ambientes depois da v1.13.0](./2026-11-14-api-fora-depois-da-v1-13-0.md): o backend não cabia mais em 512Mi e meia CPU, e o Postgres travou no `gosu` depois do reinício do servidor | v1.13.1 e v1.13.2 |
| 2026-11-17 | [Banco do dev travado em recuperação](./2026-11-17-banco-do-dev-travado-em-recuperacao.md) por 3 dias, sem reinício automático | [ADR-0210](../adr/0210-postgres-se-reinicia-quando-trava.md) |

## O vigia e o aviso de queda

O CronJob `vigia` de produção ([ADR-0209](../adr/0209-resiliencia-do-deploy.md)) olha, de 5 em 5 minutos, a API (`app`) e o banco
(`postgres`) dos quatro ambientes. O que ficar indisponível por mais de 10 minutos vira uma issue com as etiquetas `alerta` e `bug`,
com os comandos de diagnóstico; quando volta, o vigia comenta e fecha a issue. O GitHub avisa o dono do repositório por e-mail.

Para ligar o aviso, crie no servidor o secret com um token do GitHub com permissão de issues neste repositório (um token
*fine-grained* só com "Issues: Read and write"):

```bash
sudo kubectl -n mais-saude-prod create secret generic alerta-github --from-literal=token='<o token>'
```

Sem o secret, o vigia só escreve no log: `sudo kubectl -n mais-saude-prod logs job/<o último vigia>`.

