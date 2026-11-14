# Incidentes

Registro dos incidentes dos ambientes: o que aconteceu, a causa, como foi resolvido e o que ficou de acompanhamento. Cada
incidente tem também uma issue no GitHub com a etiqueta `bug`.

| Data | Incidente | Correção |
|---|---|---|
| 2026-11-14 | [API fora nos quatro ambientes depois da v1.13.0](./2026-11-14-api-fora-depois-da-v1-13-0.md): o backend não cabia mais em 512Mi e meia CPU, e o Postgres travou no `gosu` depois do reinício do servidor | v1.13.1 e v1.13.2 |
