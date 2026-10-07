## O que entra

<!-- O que muda e por quê. Cite a ADR e a fatia (por exemplo: ADR-0115, MO1). -->

## Testes

- [ ] JUnit dos endpoints novos ou alterados
- [ ] Robot de API (`test/robot/test/...`) para os endpoints novos
- [ ] Robot de interface (`test/robot/test/ui/...`) para as telas novas ou refeitas
- [ ] `ng build` sem erros (quando há frontend)

## Documentação

- [ ] ADR nova ou atualizada, com linha no índice (`docs/adr/README.md`)
- [ ] `docs/STATUS-DOS-DOMINIOS.md` atualizado (caixa marcada, item em "Temos", estado do domínio)
- [ ] `docs/PENDENCIAS.md` e o modelo do domínio, quando houver mudança de escopo
- [ ] Migração Flyway nova, se mudou o esquema
