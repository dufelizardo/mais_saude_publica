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
| [0002](./0002-modelar-hierarquia-como-entidade-unica-autorreferenciada.md) | Modelar a hierarquia de 4 níveis como entidade única autorreferenciada | Aceita e implementada |
| [0003](./0003-adocao-clean-architecture.md) | Adoção de Clean Architecture | Proposto |
| [0004](./0004-adocao-padroes-projeto.md) | Adoção de padrões de projeto (Factory, Builder, Strategy, Repository) | Proposto |
| [0005](./0005-separacao-dominio-jpa.md) | Separação entre entidades de domínio e entidades JPA | Proposto |
| [0006](./0006-seguranca-jwt.md) | Estratégia de segurança e autenticação com JWT | Substituída (0054) |
| [0007](./0007-deploy-docker.md) | Estratégia de deploy com Docker e CI/CD | Proposto |
| [0008](./0008-frontend-angular.md) | Escolha de front-end: Angular + TypeScript | Aceita (parcialmente implementada; testes substituídos pela 0077) |
| [0009](./0009-renomear-hierarquia-para-esferas-de-gestao-do-sus.md) | Renomear a hierarquia genérica (Zero/Um/Dois/Três) para as esferas de gestão do SUS (Federal/Estadual/Municipal/Regional) + criar o nível Unidade de Saúde | Aceita (parcialmente implementada) |
| [0010](./0010-fluxo-de-branches-e-pipeline-de-promocao.md) | Fluxo de branches (developer → qaa → homologacao → main) com pipeline de gate no GitHub Actions + troca de MySQL para PostgreSQL | Aceita e implementada |
| [0011](./0011-fase-2-melhorias-de-pipeline-e-branching.md) | Backlog de melhorias de branching/pipeline (Fase 2): PAT dedicado, feature/fix/hotfix, aprovações obrigatórias, testes de performance | Proposta |
| [0012](./0012-infraestrutura-local-k3s-homelab.md) | Infraestrutura local (home-lab K3s + ArgoCD) para os 4 ambientes, resolvendo a pendência de URL persistente em QA/Homologação | Aceita e implementada (2026-09-10) |
| [0013](./0013-implementar-5-nivel-unidade-de-saude-e-supervisao-regional.md) | Implementar o 5º nível (Unidade de Saúde) e o vínculo de supervisão regional | Aceita e implementada |
| [0014](./0014-modulo-profissional-rh-com-vinculo-fraco-por-reconciliacao.md) | Módulo Profissional/RH com vínculo fraco resolvido por reconciliação | Aceita e implementada |
| [0015](./0015-pinar-imagens-por-sha-para-sincronizacao-automatica-do-argocd.md) | Pinar imagens por SHA para sincronização automática do ArgoCD (sem SSH/restart manual) | Aceita e implementada |
| [0016](./0016-deploy-do-frontend-angular-no-ambiente-dev.md) | Deploy do front-end Angular no ambiente dev, em host separado | Aceita e implementada (dev, qaa, homologacao e prod) |
| [0017](./0017-numero-de-matricula-automatico-e-cpf-nao-unico.md) | Número de matrícula automático e CPF deixa de ser único em Profissional | Aceita e implementada |
| [0018](./0018-app-shell-e-decisoes-de-frontend-do-modulo-rh.md) | App shell de navegação e decisões de frontend da fatia 1 do módulo de RH | Aceita e implementada |
| [0019](./0019-vincular-cargo-e-lotacao-no-cadastro-de-profissional.md) | Vincular cargo e lotação (e portanto salário) no cadastro de profissional | Aceita e implementada |
| [0020](./0020-tela-central-do-profissional.md) | Tela central do Profissional (fatia 2) | Aceita e implementada |
| [0021](./0021-fluxo-de-transferencia-na-aba-lotacao.md) | Fluxo de transferência de lotação embutido no perfil, sem tela própria (fatia 3) | Aceita e implementada |
| [0022](./0022-composicao-remuneratoria-endpoint-calculado.md) | Endpoint calculado de composição remuneratória (fatia 4) | Aceita e implementada |
| [0023](./0023-afastamentos-e-licencas-no-perfil.md) | Afastamentos e Licenças no perfil (fatia 5) | Aceita e implementada |
| [0024](./0024-ponto-com-filtro-de-periodo.md) | Ponto com filtro de período (fatia 6a) | Aceita e implementada |
| [0025](./0025-folha-de-pagamento-por-competencia.md) | Folha de pagamento por competência (fatia 6b, fecha a fatia 6) | Aceita e implementada |
| [0026](./0026-historico-funcional-consolidado.md) | Histórico funcional consolidado (fatia 7) | Aceita e implementada |
| [0027](./0027-fatia-8-treinamento-avaliacao-sst-recrutamento.md) | Fatia 8: Treinamento, Avaliação, SST, Recrutamento | Aceita e implementada |
| [0028](./0028-fatia-9-beneficios.md) | Fatia 9: Benefícios (+ endpoint `encerrar` de AdesaoBeneficio) | Aceita e implementada |
| [0029](./0029-listagem-e-edicao-de-contato-do-profissional.md) | Listagem de profissionais e edição de contato | Aceita e implementada |
| [0030](./0030-setor-administrativo-e-relacao-com-unidade-de-saude.md) | Setor Administrativo e relação com Unidade de Saúde | Aceita e implementada |
| [0031](./0031-perfil-administrativo-por-tipo-de-unidade.md) | Perfil Administrativo por tipo de unidade | Aceita e implementada |
| [0032](./0032-catalogo-de-capacidades-administrativas.md) | Catálogo de capacidades administrativas | Aceita e implementada |
| [0033](./0033-processos-administrativos-por-capacidade.md) | Processos administrativos por capacidade | Aceita e implementada |
| [0034](./0034-integracao-administrativo-rh-sem-duplicar-profissional.md) | Integração Administrativo ↔ RH sem duplicar Profissional | Aceita e implementada |
| [0035](./0035-responsabilidade-administrativa-separada-da-lotacao.md) | Responsabilidade administrativa separada da lotação | Aceita e implementada |
| [0036](./0036-necessidade-de-pessoal-encaminhada-ao-rh.md) | Necessidade de pessoal encaminhada ao RH | Aceita e implementada |
| [0037](./0037-criterio-para-especializacao-administrativa.md) | Critério para especialização administrativa | Proposta |
| [0038](./0038-app-shell-dinamico-e-telas-de-frontend-do-setor-administrativo.md) | App shell dinâmico e decisões de frontend do Setor Administrativo | Aceita e implementada |
| [0039](./0039-mapa-de-dominios-e-prioridades-de-arquitetura.md) | Mapa de domínios do Mais Saúde Pública e prioridades de arquitetura | Aceita |
| [0040](./0040-paciente-primeira-entidade-da-assistencia.md) | Paciente: primeira entidade da onda Assistência | Aceita e implementada |
| [0041](./0041-atendimento-registra-entrada-do-paciente-na-rede.md) | Atendimento registra a entrada do paciente na rede | Aceita e implementada |
| [0042](./0042-agendamento-independente-do-atendimento.md) | Agendamento, independente do Atendimento | Aceita e implementada |
| [0043](./0043-consulta-registrada-durante-o-atendimento.md) | Consulta registrada durante o Atendimento | Aceita e implementada |
| [0044](./0044-procedimento-realizado-durante-a-consulta.md) | Procedimento realizado durante a Consulta | Aceita (estendida pela ADR-0136: também direto no atendimento) |
| [0045](./0045-prontuario-agregacao-de-leitura.md) | Prontuário como agregação de leitura no backend | Aceita e implementada |
| [0046](./0046-telas-de-frontend-da-onda-assistencia.md) | Telas de frontend da onda Assistência | Aceita e implementada |
| [0047](./0047-triagem-primeira-entidade-da-enfermagem.md) | Triagem: primeira entidade do domínio Enfermagem | Aceita e implementada |
| [0048](./0048-evolucao-de-enfermagem-segunda-entidade-da-enfermagem.md) | Evolução de Enfermagem: segunda entidade do domínio Enfermagem | Aceita e implementada |
| [0049](./0049-medicamento-primeira-entidade-da-farmacia.md) | Medicamento: primeira entidade do domínio Farmácia | Aceita e implementada |
| [0050](./0050-lote-segunda-entidade-da-farmacia.md) | Lote: segunda entidade do domínio Farmácia | Aceita e implementada |
| [0051](./0051-dispensacao-terceira-entidade-da-farmacia.md) | Dispensação: terceira entidade do domínio Farmácia | Aceita e implementada |
| [0052](./0052-tela-de-pacientes-lista-mais-painel-de-detalhe.md) | Tela de Pacientes: lista + painel de detalhe, com estados "Em breve" | Aceita e implementada |
| [0053](./0053-criterio-de-governanca-para-equipamentos-de-saude.md) | Critério de governança para equipamentos de saúde (evitar lista plana em `TipoUnidadeDeSaude`) | Aceita (complementada pela 0115) |
| [0054](./0054-modelo-de-identidade-autorizacao-e-auditoria.md) | Modelo de Identidade, Autorização e Auditoria (substitui a ADR-0006) | Proposta |
| [0055](./0055-primeira-implementacao-de-login-usuario-jwt-e-toggle-por-ambiente.md) | Primeira implementação de login: `Usuario`, JWT e toggle por ambiente | Aceita e implementada (backend + frontend) |
| [0056](./0056-versao-da-release-vem-do-pom-e-release-automatica.md) | Versão da release vem do `pom.xml`, e a release no GitHub passa a ser automática | Aceita e implementada |
| [0057](./0057-livro-de-movimentacao-do-estoque-da-farmacia.md) | Livro de movimentação do estoque da Farmácia (auditoria de lotes) | Aceita e implementada |
| [0058](./0058-tela-da-farmacia-abas-gaveta-lateral-e-livro-de-estoque.md) | Tela da Farmácia: abas, gaveta lateral e livro de estoque | Aceita e implementada |
| [0059](./0059-transferencia-de-estoque-entre-unidades.md) | Transferência de estoque da Farmácia entre unidades | Revista pela 0061 |
| [0060](./0060-uma-remessa-um-lote-por-unidade.md) | Uma remessa, um lote por unidade | Aceita e implementada |
| [0061](./0061-transferencia-em-duas-etapas-envio-e-recebimento.md) | Transferência em duas etapas: envio, trânsito e recebimento conferido | Aceita e implementada |
| [0062](./0062-registros-clinicos-imutaveis-com-retificacao.md) | Registros clínicos imutáveis, com retificação | Aceita e implementada |
| [0063](./0063-tela-atendimentos-do-agendamento-ao-prontuario.md) | Tela Atendimentos: do agendamento ao prontuário, com acolhimento | Aceita e implementada |
| [0064](./0064-administracao-de-medicamento-enfermagem-e-farmacia.md) | Administração de medicamento: checagem de enfermagem ligada à Farmácia | Aceita e implementada |
| [0065](./0065-profissional-preenchido-a-partir-do-login.md) | Profissional preenchido a partir do login | Aceita e implementada |
| [0066](./0066-papeis-permissoes-e-escopo-por-unidade.md) | Papéis, permissões e escopo por unidade | Aceita e implementada (fatia 1 de 3) |
| [0067](./0067-autorizacao-aplicada-nas-rotas-e-no-escopo.md) | Autorização aplicada nas rotas e no escopo da unidade | Aceita e implementada (fatia 2 de 3; toggle desligado) |
| [0068](./0068-tela-usuarios-e-perfis.md) | Tela Usuários & Perfis e menu conforme as permissões | Aceita e implementada (fatia 3 de 3) |
| [0069](./0069-troca-de-senha-e-senha-provisoria.md) | Troca de senha e senha provisória | Aceita e implementada |
| [0070](./0070-trilha-de-auditoria.md) | Trilha de auditoria: registro | Aceita e implementada (fatia 1 de 2) |
| [0071](./0071-consulta-da-trilha-de-auditoria.md) | Consulta da trilha de auditoria | Aceita e implementada (fatia 2 de 2) |
| [0072](./0072-tela-profissionais-a-partir-do-prototipo.md) | Tela Profissionais a partir do protótipo | Aceita e implementada (padronização do RH, 1 de 3) |
| [0073](./0073-catalogos-de-rh-em-telas-com-abas.md) | Catálogos de RH agrupados em telas com o layout recente | Aceita e implementada (padronização do RH, 2 de 3) |
| [0074](./0074-telas-do-administrativo-agrupadas.md) | Telas do Administrativo agrupadas com o layout recente | Aceita e implementada |
| [0075](./0075-cabecalho-do-perfil-do-profissional.md) | Cabeçalho do perfil do profissional no estilo do cartão | Aceita e implementada (padronização do RH, 3 de 3) |
| [0076](./0076-prontuario-por-vinculo-assistencial.md) | Prontuário por vínculo assistencial e acesso justificado | Aceita e implementada (atrás de toggle, desligado) |
| [0077](./0077-testes-de-frontend-com-robot-framework.md) | Testes de frontend com Robot Framework | Aceita e implementada (base e primeiras telas) |
| [0078](./0078-sessoes-encerradas-na-hora.md) | Sessões encerradas na hora: versão de sessão no token | Aceita e implementada |
| [0079](./0079-acoes-escondidas-sem-permissao.md) | Telas só oferecem as ações que o perfil permite | Aceita e implementada |
| [0080](./0080-restricoes-de-enum-atualizadas-na-subida.md) | Restrições de enum atualizadas na subida | Substituída pela ADR-0090 |
| [0081](./0081-recuperacao-de-senha-por-email.md) | Recuperação de senha por e-mail | Aceita e implementada (desligada até haver SMTP) |
| [0082](./0082-exportacao-e-retencao-da-auditoria.md) | Exportação e retenção da trilha de auditoria | Aceita e implementada |
| [0083](./0083-edicao-de-catalogos-de-rh-e-responsaveis-por-setor.md) | Edição de catálogos de RH e responsáveis por setor | Aceita e implementada |
| [0084](./0084-abas-do-perfil-no-padrao-novo-dados-a-ajustes.md) | Abas do perfil do profissional no padrão novo (Dados a Ajustes) | Aceita e implementada |
| [0085](./0085-abas-do-perfil-no-padrao-novo-afastamentos-a-sst.md) | Abas do perfil do profissional no padrão novo (Afastamentos a SST) | Aceita e implementada |
| [0086](./0086-abas-do-perfil-no-padrao-novo-treinamentos-a-historico.md) | Abas do perfil do profissional no padrão novo (Treinamentos a Histórico) | Aceita e implementada |
| [0087](./0087-regulacao-do-acesso-backend.md) | Regulação do acesso: backend | Aceita e implementada |
| [0088](./0088-regulacao-do-acesso-tela.md) | Regulação do acesso: tela | Aceita e implementada |
| [0089](./0089-regulacao-agendamento-desfecho-e-vinculo.md) | Regulação do acesso: agendamento, desfecho e vínculo | Aceita e implementada |
| [0090](./0090-migracoes-versionadas-com-flyway.md) | Migrações versionadas com Flyway | Aceita e implementada |
| [0091](./0091-agenda-do-profissional.md) | Agenda do profissional | Aceita e implementada |
| [0092](./0092-agenda-do-profissional-tela.md) | Agenda do profissional: tela | Aceita e implementada |
| [0093](./0093-laboratorio-backend.md) | Laboratório assistencial: backend | Aceita e implementada |
| [0094](./0094-laboratorio-tela.md) | Laboratório assistencial: tela | Aceita e implementada |
| [0095](./0095-laboratorio-prontuario-laudo-recoleta.md) | Laboratório assistencial: resultados no prontuário, laudo e recoleta | Aceita e implementada |
| [0096](./0096-alertas-da-auditoria.md) | Alertas da auditoria: detecção e análise | Aceita e implementada |
| [0097](./0097-alertas-da-auditoria-tela.md) | Alertas da auditoria: tela | Aceita e implementada |
| [0098](./0098-leitos-e-internacao-backend.md) | Leitos e internação: backend | Aceita e implementada (parte 1 de 3) |
| [0099](./0099-leitos-e-internacao-tela.md) | Leitos e internação: tela | Aceita e implementada (parte 2 de 3) |
| [0100](./0100-leitos-e-internacao-prontuario.md) | Leitos e internação: prontuário e vínculo assistencial | Aceita e implementada |
| [0101](./0101-equipamentos-de-saude-backend.md) | Equipamentos de Saúde: backend | Aceita e implementada (parte 1 de 2) |
| [0102](./0102-equipamentos-de-saude-tela.md) | Equipamentos de Saúde: tela | Aceita e implementada |
| [0103](./0103-equipes-backend.md) | Equipes de saúde: backend | Aceita e implementada (parte 1 de 4) |
| [0104](./0104-equipes-tela.md) | Equipes de saúde: tela | Aceita e implementada (parte 2 de 4) |
| [0105](./0105-escalas-backend.md) | Escalas: backend | Aceita e implementada (parte 3 de 4) |
| [0106](./0106-escalas-tela.md) | Escalas: tela | Aceita e implementada (parte 4 de 4) |
| [0107](./0107-escalas-intervalo-e-modelos-de-jornada.md) | Escalas: intervalo no turno e modelos de jornada | Aceita e implementada |
| [0108](./0108-territorio-e-adscricao.md) | Território e Adscrição: domínio próprio | Aceita e implementada (F1 a F4 nas ADRs 0125 a 0129) |
| [0109](./0109-programas-acoes-e-linhas-de-cuidado.md) | Programas, Ações e Linhas de Cuidado: domínio próprio | Aceita (desenho; P1 a P4 e P6 nas ADRs 0118, 0119, 0133, 0132 e 0141; P5 a fazer) |
| [0110](./0110-vigilancia-em-saude.md) | Vigilância em Saúde: domínio próprio (e Imunização reservada como #24) | Aceita (desenho; V1 na ADR-0124, V2 nas ADRs 0130 e 0131, V3 a V5 a fazer) |
| [0111](./0111-saude-bucal.md) | Saúde Bucal: domínio assistencial especializado | Aceita e implementada (SB1 a SB5 nas ADRs 0134 a 0140) |
| [0112](./0112-saude-mental.md) | Saúde Mental: domínio de atenção psicossocial | Aceita (desenho; SM1 a SM4 nas ADRs 0120 a 0123; SM5 e SM6 a fazer) |
| [0113](./0113-emergencias-e-desastres.md) | Gestão de Emergências e Desastres: coordenação extraordinária | Aceita (desenho; implementação nas fatias E1 a E6) |
| [0114](./0114-comunicacao-e-educacao-em-saude.md) | Comunicação e Educação em Saúde: domínio futuro | Aceita (desenho; implementação nas fatias C1 a C6) |
| [0115](./0115-modelo-operacional-dos-equipamentos.md) | Modelo Operacional dos Equipamentos e reconciliação do mapa (#29 a #31) | Aceita (desenho; MO1 na ADR-0117, MO2 a MO4 a fazer) |
| [0116](./0116-marco-mapa-de-dominios-fechado.md) | Marco: mapa de domínios fechado e regra para novos domínios | Aceita |
| [0117](./0117-modelo-operacional-perfis-e-capacidades.md) | Modelo operacional: perfis e capacidades da unidade (MO1) | Aceita e implementada |
| [0118](./0118-programas-catalogo.md) | Programas de saúde: catálogo (P1) | Aceita e implementada |
| [0119](./0119-programas-acompanhamento.md) | Programas de saúde: acompanhamento programático (P2) | Aceita e implementada |
| [0120](./0120-saude-mental-base.md) | Saúde Mental: base (SM1) | Aceita e implementada |
| [0121](./0121-saude-mental-pts-intervencoes.md) | Saúde Mental: PTS, intervenções e rede de apoio (SM2) | Aceita e implementada |
| [0122](./0122-saude-mental-rede.md) | Saúde Mental: rede de atenção (SM4) | Aceita e implementada |
| [0123](./0123-saude-mental-risco-crise.md) | Saúde Mental: risco com histórico, crise e internação psiquiátrica (SM3) | Aceita e implementada |
| [0124](./0124-vigilancia-epidemiologica-fundacao.md) | Vigilância epidemiológica: fundação (V1) | Aceita e implementada |
| [0125](./0125-territorio-fundacao.md) | Território: fundação (F1) | Aceita e implementada |
| [0126](./0126-territorio-mapa.md) | Território: mapa (F2) | Aceita e implementada |
| [0127](./0127-cadastro-territorial.md) | Território: imóvel, domicílio e família (F3a) | Aceita e implementada |
| [0128](./0128-adscricao-e-cobertura.md) | Território: adscrição e cobertura (F3b) | Aceita e implementada |
| [0129](./0129-escopo-territorial.md) | Território: escopo territorial de acesso (F4) | Aceita e implementada (atrás de toggle, desligado) |
| [0130](./0130-vigilancia-contatos-local-e-mapa.md) | Vigilância: contatos, local do caso e mapa (V2a) | Aceita e implementada |
| [0131](./0131-vigilancia-surto-e-medidas.md) | Vigilância: surto e medidas de controle (V2b) | Aceita e implementada |
| [0132](./0132-programas-acoes-programaticas.md) | Programas: ações programáticas (P4) | Aceita e implementada |
| [0133](./0133-programas-campanhas.md) | Programas: campanhas (P3) | Aceita e implementada |
| [0134](./0134-saude-bucal-base.md) | Saúde Bucal: especialidade no atendimento e avaliação odontológica (SB1) | Aceita e implementada |
| [0135](./0135-saude-bucal-odontograma.md) | Saúde Bucal: odontograma com histórico (SB2) | Aceita e implementada |
| [0136](./0136-saude-bucal-procedimento-odontologico.md) | Saúde Bucal: catálogo de procedimentos e procedimento odontológico (SB3a) | Aceita e implementada |
| [0137](./0137-saude-bucal-plano-de-tratamento.md) | Saúde Bucal: plano de tratamento (SB3b) | Aceita e implementada |
| [0138](./0138-saude-bucal-encaminhamento-pela-regulacao.md) | Saúde Bucal: encaminhamento odontológico pela Regulação (SB4a) | Aceita e implementada |
| [0139](./0139-saude-bucal-tratamento-protetico.md) | Saúde Bucal: tratamento protético (SB4b) | Aceita e implementada |
| [0140](./0140-saude-bucal-coletiva.md) | Saúde Bucal: saúde bucal coletiva (SB5) | Aceita e implementada |
| [0141](./0141-programas-metas-e-indicadores.md) | Programas: metas e indicadores programáticos (P6) | Aceita e implementada |
| [0142](./0142-indicadores-bi-e-gestao.md) | Indicadores, BI e Gestão: camada de leitura (domínio 19) | Aceita (desenho; BI1 e BI2 nas ADRs 0143 e 0144; BI3 a BI5 a fazer) |
| [0143](./0143-bi-producao-assistencial.md) | Indicadores e BI: produção assistencial por unidade (BI1) | Aceita e implementada |
| [0144](./0144-bi-situacao-operacional.md) | Indicadores e BI: situação operacional (BI2) | Aceita e implementada |

As ADRs 0003 a 0008 formam um roadmap de arquitetura futura (não implementado) — ver notas de reconciliação em cada arquivo. A prioridade original (0003, 0006 e 0007 antes de novos domínios de negócio) nunca foi seguida na prática — RH e Administrativo foram construídos inteiros sem ela — e foi formalmente revisitada pela ADR-0039: convenção flat reafirmada (0003 não adotado), segurança conscientemente adiada mesmo para o domínio clínico (0006). 0008 (front-end) é a de menor prioridade e segue parcialmente implementada.

As ADRs 0030 a 0037 formam o roadmap do Setor Administrativo. 0030-0036 (Fases 1-6) já foram implementadas; 0037 (Fase 7+) ainda não — ver [`docs/administrativo/ESCOPO-ADMINISTRATIVO.md`](../administrativo/ESCOPO-ADMINISTRATIVO.md) para o contexto completo e a ordem de implementação planejada.

As ADRs 0040 a 0046 formam a onda Operação Assistencial (`Paciente`, `Atendimento`, `Agendamento`, `Consulta`, `Procedimento`, `Prontuário` e suas telas de frontend) — backend e frontend completos — ver [`docs/assistencia/ESCOPO-ASSISTENCIA.md`](../assistencia/ESCOPO-ASSISTENCIA.md) para o estado atual. As ADRs 0047 e 0048 formam o início do domínio Enfermagem (`Triagem`, `EvolucaoEnfermagem`), reaproveitando a mesma agregação de Prontuário — ver [`docs/enfermagem/ESCOPO-ENFERMAGEM.md`](../enfermagem/ESCOPO-ENFERMAGEM.md) para o estado atual. As ADRs 0049 a 0051 formam o início do domínio seguinte, Farmácia (`Medicamento`, `Lote`, `Dispensacao`), que desbloqueia `AdministracaoDeMedicamento` em Enfermagem — ver [`docs/farmacia/ESCOPO-FARMACIA.md`](../farmacia/ESCOPO-FARMACIA.md) para o estado atual; a ADR-0057 acrescenta o livro de movimentação do estoque (`MovimentacaoFarmacia`: entrada, dispensação, perda e ajuste de inventário imutáveis, com trava contra venda dupla), revendo o contador simples da ADR-0050; a ADR-0058 é a tela da Farmácia (abas de estoque, dispensações, medicamentos e livro de estoque, com gaveta lateral compartilhada); a ADR-0059 acrescenta a transferência de estoque entre unidades (saída e entrada ligadas no livro, lote da mesma remessa no destino); a ADR-0060 garante uma remessa por unidade (entrada soma no lote existente, trava da unidade, índice único e incorporação auditável de duplicados antigos); a ADR-0061 torna a transferência auditável em duas etapas (envio em trânsito, recebimento conferido por outro profissional com divergência registrada, ou cancelamento com estorno). A ADR-0062 torna imutáveis os registros clínicos (triagem, evolução, consulta e procedimento): correção vira retificação com motivo, o desfecho do procedimento vira evento próprio, e o atendimento ganha resumo clínico e fecha o agendamento de origem. A ADR-0063 junta Atendimentos, Agendamentos, Consultas, Procedimentos e Prontuário numa tela só (abas + atendimento em gaveta larga), com Acolhimento indo direto para a triagem. A ADR-0064 liga Enfermagem e Farmácia: a checagem do medicamento prescrito (administrado com lote, dose e via, ou não administrado com motivo) baixa o lote da unidade pelo livro, com estorno na retificação, e aparece na aba Medicação. A ADR-0065 abre a frente de identidade: `GET /auth/eu` informa quem está logado e o profissional de mesmo CPF, e as telas preenchem a matrícula dos registros com ele. A ADR-0066 implementa papéis, permissões e escopo por unidade (fatia 1 de 3): catálogo de 22 permissões e 8 papéis padrão semeados (técnico de enfermagem sem classificação de risco nem evolução; administrador da plataforma sem dado de saúde), atribuição com escopo herdado pela hierarquia e revogação auditável, e o cálculo de autorização — ainda sem exigir permissão nas rotas (fatia 2) e sem a tela Usuários & Perfis (fatia 3). A ADR-0067 é a fatia 2: permissão por rota (negado por padrão), escopo da unidade nos serviços e nas listagens, retificação só pelo autor ou supervisão, recebimento de transferência só no destino e concessão dentro do próprio escopo — atrás do toggle `app.security.authorization.enabled`, desligado em todos os ambientes até os acessos serem concedidos. A ADR-0068 fecha a frente com a tela Usuários & Perfis (protótipo `Usuarios.html`: usuários, perfis com a matriz de permissões por módulo, concessão e revogação por escopo), o cadastro e o desbloqueio de usuário, e o menu que só mostra o que as permissões liberam quando a autorização está ligada. A ADR-0070 abre a Auditoria: um evento imutável por alteração, por leitura do detalhe de dado de saúde ou pessoal, por recusa (403) e por login, com quem, quando, registro, paciente, unidade e origem — sem guardar conteúdo; a ADR-0071 dá a consulta (tela Auditoria com filtros, "Quem acessou" no atendimento, permissão `AUDITORIA.CONSULTAR` fora de todos os perfis padrão, escopo por unidade e a própria consulta registrada). A ADR-0069 acrescenta a troca da própria senha e a senha provisória (cadastro e redefinição pela administração exigem troca no primeiro acesso; até lá a API só atende /auth). A ADR-0072 começa a padronizar as telas de RH pelo protótipo `Profissionais.html`: quadro em cartões ou lista com categoria pelo conselho, lotação e afastamento vigentes (`GET /profissional/quadro`), e cadastro e desligamento em gavetas da mesma tela. A ADR-0073 agrupa os catálogos de RH em Cargos & salários, Benefícios, Desenvolvimento e Recrutamento (abas, indicadores, subpáginas e cadastros em gaveta) e refaz a Folha de pagamento no mesmo layout — o menu de RH cai para 6 itens. A ADR-0074 faz o mesmo no Administrativo: Setores ganha a aba Responsabilidades, Capacidades, Processos, Perfis e Perfil por tipo de unidade viram a tela Modelo administrativo, e Necessidades de pessoal é refeita — o menu cai de 7 para 3 itens. A ADR-0075 fecha a padronização do RH com o cabeçalho do perfil do profissional no estilo do cartão (categoria, situação, lotação vigente e jornada), sem mexer nas 13 abas. A ADR-0076 resolve a pendência da ADR-0054 sobre acesso contextual: o prontuário completo passa a exigir vínculo assistencial (atendimento na unidade, profissional do atendimento ou do agendamento), com "quebra de vidro" — acesso justificado por 4 horas, auditado como `ACESSO_JUSTIFICADO` — e a permissão `PRONTUARIO.CONSULTAR_SEM_VINCULO` fora dos papéis padrão, atrás do toggle `app.security.prontuario-por-vinculo.enabled`. Para ligar a exigência num ambiente, siga o guia [`docs/acesso/GUIA-LIGAR-AUTORIZACAO.md`](../acesso/GUIA-LIGAR-AUTORIZACAO.md). A ADR-0052 redesenha a tela de Pacientes (fase F7 da onda Assistência, ver `ESCOPO-ASSISTENCIA.md`) com o layout lista+painel de detalhe e o padrão "Em breve" para seções sem backend ainda. A ADR-0053 reconcilia o levantamento de ~30 equipamentos de saúde (base CNES) trazido pelo usuário — generaliza o critério de 4 pontos da ADR-0037 para toda a plataforma (quando um equipamento novo vira valor de `TipoUnidadeDeSaude`, capacidade catalogada ou especialização de domínio própria), sem implementar nenhum equipamento novo — ver [`../MAPA-DE-EQUIPAMENTOS-DE-SAUDE.md`](../MAPA-DE-EQUIPAMENTOS-DE-SAUDE.md). A ADR-0054 formaliza o modelo de Identidade, Autorização e Auditoria (RBAC + escopo organizacional + catálogo de permissões como dado + auditoria enriquecida), substituindo o modelo de 4 roles fixas da ADR-0006 — decisão de forma apenas, implementação continua adiada até revisitar antes de produção com dado real de paciente (ADR-0039). A ADR-0055 implementa `Usuario`, login por CPF/matrícula, JWT, o toggle `app.security.enabled` por ambiente e a tela de Login (backend + frontend) — sem RBAC granular ainda. O toggle já está ligado no overlay `dev` (requer o `Secret` `app-secrets` provisionado no cluster); `qaa`/`homologacao`/`prod` ligam quando o usuário decidir promover cada ambiente.

Ver também [`DER-atual.md`](./DER-atual.md) (schema real implementado hoje, corrigido pela ADR-0053 para refletir os 12 valores reais de `TipoUnidadeDeSaude`), [`DER.md`](./DER.md) (modelo de dados clínico, revisado pela ADR-0039, com apêndice expandido pela ADR-0053), [`../MAPA-DE-DOMINIOS.md`](../MAPA-DE-DOMINIOS.md) (mapa dos 31 itens da plataforma inteira e ordem de implementação) e [`../MAPA-DE-EQUIPAMENTOS-DE-SAUDE.md`](../MAPA-DE-EQUIPAMENTOS-DE-SAUDE.md) (levantamento completo de equipamentos de saúde).
