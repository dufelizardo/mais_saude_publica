# Architecture Decision Records (ADR)

Este diretório guarda os registros de decisões de arquitetura do projeto — o "porquê" por trás de escolhas que têm trade-offs relevantes e não são óbvias a partir do código sozinho.

Cada ADR é um arquivo `NNNN-titulo-curto.md`, numerado sequencialmente, seguindo o formato leve de Michael Nygard:

- **Status** — Proposta, Aceita, Rejeitada, Substituída (por qual ADR) ou Obsoleta.
- **Contexto** — o problema, restrição ou situação que motivou a decisão.
- **Decisão** — o que foi decidido.
- **Trade-offs considerados** — as alternativas avaliadas e por que a escolhida venceu.
- **Consequências** — o que essa decisão traz de bom, e o que ela deixa como dívida ou pendência conhecida.

Crie um novo ADR quando uma decisão envolver trade-offs (não para escolhas triviais ou reversíveis a baixo custo), especialmente quando a decisão contraria a alternativa "óbvia" e alguém no futuro provavelmente vai perguntar "por que não fizeram X em vez disso?".

## Índice

| ADR | Título | Status |
|---|---|---|
| [0001](./0001-restaurar-e-refatorar-em-vez-de-reescrever.md) | Restaurar e refatorar o código apagado, em vez de reescrever do zero | Aceita |
| [0002](./0002-modelar-hierarquia-como-entidade-unica-autorreferenciada.md) | Modelar a hierarquia de 4 níveis como entidade única autorreferenciada | Aceita |
| [0003](./0003-adocao-clean-architecture.md) | Adoção de Clean Architecture | Proposto |
| [0004](./0004-adocao-padroes-projeto.md) | Adoção de padrões de projeto (Factory, Builder, Strategy, Repository) | Proposto |
| [0005](./0005-separacao-dominio-jpa.md) | Separação entre entidades de domínio e entidades JPA | Proposto |
| [0006](./0006-seguranca-jwt.md) | Estratégia de segurança e autenticação com JWT | Substituída (0054) |
| [0007](./0007-deploy-docker.md) | Estratégia de deploy com Docker e CI/CD | Proposto |
| [0008](./0008-frontend-angular.md) | Escolha de front-end: Angular + TypeScript | Aceita (parcialmente implementada) |
| [0009](./0009-renomear-hierarquia-para-esferas-de-gestao-do-sus.md) | Renomear a hierarquia genérica (Zero/Um/Dois/Três) para as esferas de gestão do SUS (Federal/Estadual/Municipal/Regional) + criar o nível Unidade de Saúde | Aceita (parcialmente implementada) |
| [0010](./0010-fluxo-de-branches-e-pipeline-de-promocao.md) | Fluxo de branches (developer → qaa → homologacao → main) com pipeline de gate no GitHub Actions + troca de MySQL para PostgreSQL | Aceita |
| [0011](./0011-fase-2-melhorias-de-pipeline-e-branching.md) | Backlog de melhorias de branching/pipeline (Fase 2): PAT dedicado, feature/fix/hotfix, aprovações obrigatórias, testes de performance | Proposta |
| [0012](./0012-infraestrutura-local-k3s-homelab.md) | Infraestrutura local (home-lab K3s + ArgoCD) para os 4 ambientes, resolvendo a pendência de URL persistente em QA/Homologação | Proposta |
| [0013](./0013-implementar-5-nivel-unidade-de-saude-e-supervisao-regional.md) | Implementar o 5º nível (Unidade de Saúde) e o vínculo de supervisão regional | Aceita |
| [0014](./0014-modulo-profissional-rh-com-vinculo-fraco-por-reconciliacao.md) | Módulo Profissional/RH com vínculo fraco resolvido por reconciliação | Aceita |
| [0015](./0015-pinar-imagens-por-sha-para-sincronizacao-automatica-do-argocd.md) | Pinar imagens por SHA para sincronização automática do ArgoCD (sem SSH/restart manual) | Aceita |
| [0016](./0016-deploy-do-frontend-angular-no-ambiente-dev.md) | Deploy do front-end Angular no ambiente dev, em host separado | Aceita (escopo: dev) |
| [0017](./0017-numero-de-matricula-automatico-e-cpf-nao-unico.md) | Número de matrícula automático e CPF deixa de ser único em Profissional | Aceita |
| [0018](./0018-app-shell-e-decisoes-de-frontend-do-modulo-rh.md) | App shell de navegação e decisões de frontend da fatia 1 do módulo de RH | Aceita |
| [0019](./0019-vincular-cargo-e-lotacao-no-cadastro-de-profissional.md) | Vincular cargo e lotação (e portanto salário) no cadastro de profissional | Aceita |
| [0020](./0020-tela-central-do-profissional.md) | Tela central do Profissional (fatia 2) | Aceita |
| [0021](./0021-fluxo-de-transferencia-na-aba-lotacao.md) | Fluxo de transferência de lotação embutido no perfil, sem tela própria (fatia 3) | Aceita |
| [0022](./0022-composicao-remuneratoria-endpoint-calculado.md) | Endpoint calculado de composição remuneratória (fatia 4) | Aceita |
| [0023](./0023-afastamentos-e-licencas-no-perfil.md) | Afastamentos e Licenças no perfil (fatia 5) | Aceita |
| [0024](./0024-ponto-com-filtro-de-periodo.md) | Ponto com filtro de período (fatia 6a) | Aceita |
| [0025](./0025-folha-de-pagamento-por-competencia.md) | Folha de pagamento por competência (fatia 6b, fecha a fatia 6) | Aceita |
| [0026](./0026-historico-funcional-consolidado.md) | Histórico funcional consolidado (fatia 7) | Aceita |
| [0027](./0027-fatia-8-treinamento-avaliacao-sst-recrutamento.md) | Fatia 8: Treinamento, Avaliação, SST, Recrutamento | Aceita |
| [0028](./0028-fatia-9-beneficios.md) | Fatia 9: Benefícios (+ endpoint `encerrar` de AdesaoBeneficio) | Aceita |
| [0029](./0029-listagem-e-edicao-de-contato-do-profissional.md) | Listagem de profissionais e edição de contato | Aceita |
| [0030](./0030-setor-administrativo-e-relacao-com-unidade-de-saude.md) | Setor Administrativo e relação com Unidade de Saúde | Aceita |
| [0031](./0031-perfil-administrativo-por-tipo-de-unidade.md) | Perfil Administrativo por tipo de unidade | Aceita |
| [0032](./0032-catalogo-de-capacidades-administrativas.md) | Catálogo de capacidades administrativas | Aceita |
| [0033](./0033-processos-administrativos-por-capacidade.md) | Processos administrativos por capacidade | Aceita |
| [0034](./0034-integracao-administrativo-rh-sem-duplicar-profissional.md) | Integração Administrativo ↔ RH sem duplicar Profissional | Aceita |
| [0035](./0035-responsabilidade-administrativa-separada-da-lotacao.md) | Responsabilidade administrativa separada da lotação | Aceita |
| [0036](./0036-necessidade-de-pessoal-encaminhada-ao-rh.md) | Necessidade de pessoal encaminhada ao RH | Aceita |
| [0037](./0037-criterio-para-especializacao-administrativa.md) | Critério para especialização administrativa | Proposta |
| [0038](./0038-app-shell-dinamico-e-telas-de-frontend-do-setor-administrativo.md) | App shell dinâmico e decisões de frontend do Setor Administrativo | Aceita |
| [0039](./0039-mapa-de-dominios-e-prioridades-de-arquitetura.md) | Mapa de domínios do Mais Saúde Pública e prioridades de arquitetura | Aceita |
| [0040](./0040-paciente-primeira-entidade-da-assistencia.md) | Paciente: primeira entidade da onda Assistência | Aceita |
| [0041](./0041-atendimento-registra-entrada-do-paciente-na-rede.md) | Atendimento registra a entrada do paciente na rede | Aceita |
| [0042](./0042-agendamento-independente-do-atendimento.md) | Agendamento, independente do Atendimento | Aceita |
| [0043](./0043-consulta-registrada-durante-o-atendimento.md) | Consulta registrada durante o Atendimento | Aceita |
| [0044](./0044-procedimento-realizado-durante-a-consulta.md) | Procedimento realizado durante a Consulta | Aceita |
| [0045](./0045-prontuario-agregacao-de-leitura.md) | Prontuário como agregação de leitura no backend | Aceita |
| [0046](./0046-telas-de-frontend-da-onda-assistencia.md) | Telas de frontend da onda Assistência | Aceita |
| [0047](./0047-triagem-primeira-entidade-da-enfermagem.md) | Triagem: primeira entidade do domínio Enfermagem | Aceita |
| [0048](./0048-evolucao-de-enfermagem-segunda-entidade-da-enfermagem.md) | Evolução de Enfermagem: segunda entidade do domínio Enfermagem | Aceita |
| [0049](./0049-medicamento-primeira-entidade-da-farmacia.md) | Medicamento: primeira entidade do domínio Farmácia | Aceita |
| [0050](./0050-lote-segunda-entidade-da-farmacia.md) | Lote: segunda entidade do domínio Farmácia | Aceita |
| [0051](./0051-dispensacao-terceira-entidade-da-farmacia.md) | Dispensação: terceira entidade do domínio Farmácia | Aceita |
| [0052](./0052-tela-de-pacientes-lista-mais-painel-de-detalhe.md) | Tela de Pacientes: lista + painel de detalhe, com estados "Em breve" | Aceita |
| [0053](./0053-criterio-de-governanca-para-equipamentos-de-saude.md) | Critério de governança para equipamentos de saúde (evitar lista plana em `TipoUnidadeDeSaude`) | Proposta |
| [0054](./0054-modelo-de-identidade-autorizacao-e-auditoria.md) | Modelo de Identidade, Autorização e Auditoria (substitui a ADR-0006) | Proposta |

As ADRs 0003 a 0008 formam um roadmap de arquitetura futura (não implementado) — ver notas de reconciliação em cada arquivo. A prioridade original (0003, 0006 e 0007 antes de novos domínios de negócio) nunca foi seguida na prática — RH e Administrativo foram construídos inteiros sem ela — e foi formalmente revisitada pela ADR-0039: convenção flat reafirmada (0003 não adotado), segurança conscientemente adiada mesmo para o domínio clínico (0006). 0008 (front-end) é a de menor prioridade e segue parcialmente implementada.

As ADRs 0030 a 0037 formam o roadmap do Setor Administrativo. 0030-0036 (Fases 1-6) já foram implementadas; 0037 (Fase 7+) ainda não — ver [`docs/administrativo/ESCOPO-ADMINISTRATIVO.md`](../administrativo/ESCOPO-ADMINISTRATIVO.md) para o contexto completo e a ordem de implementação planejada.

As ADRs 0040 a 0046 formam a onda Operação Assistencial (`Paciente`, `Atendimento`, `Agendamento`, `Consulta`, `Procedimento`, `Prontuário` e suas telas de frontend) — backend e frontend completos — ver [`docs/assistencia/ESCOPO-ASSISTENCIA.md`](../assistencia/ESCOPO-ASSISTENCIA.md) para o estado atual. As ADRs 0047 e 0048 formam o início do domínio Enfermagem (`Triagem`, `EvolucaoEnfermagem`), reaproveitando a mesma agregação de Prontuário — ver [`docs/enfermagem/ESCOPO-ENFERMAGEM.md`](../enfermagem/ESCOPO-ENFERMAGEM.md) para o estado atual. As ADRs 0049 a 0051 formam o início do domínio seguinte, Farmácia (`Medicamento`, `Lote`, `Dispensacao`), que desbloqueia `AdministracaoDeMedicamento` em Enfermagem — ver [`docs/farmacia/ESCOPO-FARMACIA.md`](../farmacia/ESCOPO-FARMACIA.md) para o estado atual. A ADR-0052 redesenha a tela de Pacientes (fase F7 da onda Assistência, ver `ESCOPO-ASSISTENCIA.md`) com o layout lista+painel de detalhe e o padrão "Em breve" para seções sem backend ainda. A ADR-0053 reconcilia o levantamento de ~30 equipamentos de saúde (base CNES) trazido pelo usuário — generaliza o critério de 4 pontos da ADR-0037 para toda a plataforma (quando um equipamento novo vira valor de `TipoUnidadeDeSaude`, capacidade catalogada ou especialização de domínio própria), sem implementar nenhum equipamento novo — ver [`../MAPA-DE-EQUIPAMENTOS-DE-SAUDE.md`](../MAPA-DE-EQUIPAMENTOS-DE-SAUDE.md). A ADR-0054 formaliza o modelo de Identidade, Autorização e Auditoria (RBAC + escopo organizacional + catálogo de permissões como dado + auditoria enriquecida), substituindo o modelo de 4 roles fixas da ADR-0006 — decisão de forma apenas, implementação continua adiada até revisitar antes de produção com dado real de paciente (ADR-0039).

Ver também [`DER-atual.md`](./DER-atual.md) (schema real implementado hoje, corrigido pela ADR-0053 para refletir os 12 valores reais de `TipoUnidadeDeSaude`), [`DER.md`](./DER.md) (modelo de dados clínico, revisado pela ADR-0039, com apêndice expandido pela ADR-0053), [`../MAPA-DE-DOMINIOS.md`](../MAPA-DE-DOMINIOS.md) (mapa dos 20 domínios da plataforma inteira e ordem de implementação) e [`../MAPA-DE-EQUIPAMENTOS-DE-SAUDE.md`](../MAPA-DE-EQUIPAMENTOS-DE-SAUDE.md) (levantamento completo de equipamentos de saúde).
