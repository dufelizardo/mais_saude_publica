-- Modelo operacional, fatia MO1 (ADR-0117): catálogos de perfil operacional e de capacidade assistencial, e o que cada
-- unidade opera e consegue fazer, com início e fim. Tabelas conferidas contra o ddl-auto=validate (ADR-0090).
-- A carga inicial roda uma vez só; depois, os catálogos e as associações são editados pela tela.

CREATE TABLE public.tb_perfil_operacional (
    ativo boolean NOT NULL,
    uuid uuid NOT NULL,
    codigo character varying(60) NOT NULL,
    nome character varying(100) NOT NULL,
    descricao text
);

CREATE TABLE public.tb_capacidade_assistencial (
    ativo boolean NOT NULL,
    perfil_operacional_id uuid,
    uuid uuid NOT NULL,
    codigo character varying(60) NOT NULL,
    nome character varying(100) NOT NULL,
    descricao text
);

CREATE TABLE public.tb_unidade_perfil_operacional (
    fim date,
    inicio date NOT NULL,
    perfil_operacional_id uuid NOT NULL,
    unidade_id uuid NOT NULL,
    uuid uuid NOT NULL,
    motivo_fim character varying(500),
    registrado_por_cpf character varying(255)
);

CREATE TABLE public.tb_unidade_capacidade_assistencial (
    fim date,
    inicio date NOT NULL,
    capacidade_assistencial_id uuid NOT NULL,
    unidade_id uuid NOT NULL,
    uuid uuid NOT NULL,
    observacao character varying(300),
    motivo_fim character varying(500),
    registrado_por_cpf character varying(255)
);

ALTER TABLE ONLY public.tb_perfil_operacional
    ADD CONSTRAINT tb_perfil_operacional_pkey PRIMARY KEY (uuid);

ALTER TABLE ONLY public.tb_perfil_operacional
    ADD CONSTRAINT tb_perfil_operacional_codigo_key UNIQUE (codigo);

ALTER TABLE ONLY public.tb_capacidade_assistencial
    ADD CONSTRAINT tb_capacidade_assistencial_pkey PRIMARY KEY (uuid);

ALTER TABLE ONLY public.tb_capacidade_assistencial
    ADD CONSTRAINT tb_capacidade_assistencial_codigo_key UNIQUE (codigo);

ALTER TABLE ONLY public.tb_unidade_perfil_operacional
    ADD CONSTRAINT tb_unidade_perfil_operacional_pkey PRIMARY KEY (uuid);

ALTER TABLE ONLY public.tb_unidade_capacidade_assistencial
    ADD CONSTRAINT tb_unidade_capacidade_assistencial_pkey PRIMARY KEY (uuid);

CREATE INDEX ix_unidade_perfil_operacional ON public.tb_unidade_perfil_operacional USING btree (unidade_id, fim);

CREATE INDEX ix_unidade_capacidade_assistencial ON public.tb_unidade_capacidade_assistencial USING btree (unidade_id, fim);

ALTER TABLE ONLY public.tb_capacidade_assistencial
    ADD CONSTRAINT fk_capacidade_assistencial_perfil FOREIGN KEY (perfil_operacional_id) REFERENCES public.tb_perfil_operacional(uuid);

ALTER TABLE ONLY public.tb_unidade_perfil_operacional
    ADD CONSTRAINT fk_unidade_perfil_operacional_unidade FOREIGN KEY (unidade_id) REFERENCES public.tb_unidade_de_saude(uuid);

ALTER TABLE ONLY public.tb_unidade_perfil_operacional
    ADD CONSTRAINT fk_unidade_perfil_operacional_perfil FOREIGN KEY (perfil_operacional_id) REFERENCES public.tb_perfil_operacional(uuid);

ALTER TABLE ONLY public.tb_unidade_capacidade_assistencial
    ADD CONSTRAINT fk_unidade_capacidade_assistencial_unidade FOREIGN KEY (unidade_id) REFERENCES public.tb_unidade_de_saude(uuid);

ALTER TABLE ONLY public.tb_unidade_capacidade_assistencial
    ADD CONSTRAINT fk_unidade_capacidade_assistencial_capacidade FOREIGN KEY (capacidade_assistencial_id) REFERENCES public.tb_capacidade_assistencial(uuid);

-- ── Carga inicial: perfis operacionais (MODELO-OPERACIONAL.md, seção 6) ──────────────────────────────────────────────

INSERT INTO public.tb_perfil_operacional (uuid, codigo, nome, descricao, ativo) VALUES
    (gen_random_uuid(), 'ATENCAO_PRIMARIA', 'Atenção primária', 'Cuidado territorial e longitudinal, porta de entrada da rede (UBS, unidade com eSF).', true),
    (gen_random_uuid(), 'URGENCIA', 'Urgência', 'Demanda espontânea aguda, com classificação de risco (UPA, pronto-socorro).', true),
    (gen_random_uuid(), 'INTERNACAO', 'Internação', 'Cuidado em regime de internação, em leitos (hospital).', true),
    (gen_random_uuid(), 'ATENCAO_ESPECIALIZADA', 'Atenção especializada', 'Consultas e procedimentos especializados por referência (Centro de Especialidades, Policlínica, CEO).', true),
    (gen_random_uuid(), 'ATENCAO_PSICOSSOCIAL', 'Atenção psicossocial', 'Cuidado em saúde mental na Rede de Atenção Psicossocial (CAPS).', true),
    (gen_random_uuid(), 'DIAGNOSTICO', 'Diagnóstico', 'Exames laboratoriais e de imagem (Laboratório, serviço de imagem).', true),
    (gen_random_uuid(), 'ASSISTENCIA_FARMACEUTICA', 'Assistência farmacêutica', 'Dispensação e controle de medicamentos (Farmácia).', true),
    (gen_random_uuid(), 'VIGILANCIA', 'Vigilância', 'Vigilância sanitária, ambiental e de zoonoses.', true),
    (gen_random_uuid(), 'REABILITACAO', 'Reabilitação', 'Reabilitação física, intelectual, auditiva e visual (Centro de Reabilitação).', true),
    (gen_random_uuid(), 'ATENCAO_MOVEL', 'Atenção móvel', 'Atendimento pré-hospitalar e unidades móveis (central e base do SAMU, unidade móvel).', true);

-- ── Carga inicial: capacidades assistenciais; perfil nulo = transversal ──────────────────────────────────────────────

INSERT INTO public.tb_capacidade_assistencial (uuid, codigo, nome, descricao, ativo, perfil_operacional_id)
SELECT gen_random_uuid(), c.codigo, c.nome, c.descricao, true, p.uuid
FROM (VALUES
    ('ACOLHIMENTO', 'Acolhimento', 'Escuta qualificada de quem chega à unidade.', NULL),
    ('DEMANDA_ESPONTANEA', 'Atendimento à demanda espontânea', 'Atende sem agendamento prévio.', NULL),
    ('ATENDIMENTO_PROGRAMADO', 'Atendimento programado', 'Atende por agenda.', NULL),
    ('ACOMPANHAMENTO_LONGITUDINAL', 'Acompanhamento longitudinal', 'Acompanha a pessoa ao longo do tempo.', NULL),
    ('ADSCRICAO_TERRITORIAL', 'Adscrição territorial', 'População adscrita a território e equipe.', 'ATENCAO_PRIMARIA'),
    ('ATENCAO_DOMICILIAR', 'Atenção domiciliar', 'Visitas e cuidado no domicílio.', 'ATENCAO_PRIMARIA'),
    ('ATIVIDADE_COLETIVA', 'Atividades coletivas e grupos', 'Grupos, ações educativas e atividades coletivas.', 'ATENCAO_PRIMARIA'),
    ('CLASSIFICACAO_DE_RISCO', 'Classificação de risco', 'Prioriza o atendimento pela gravidade.', 'URGENCIA'),
    ('OBSERVACAO', 'Observação', 'Leitos de observação de curta permanência.', 'URGENCIA'),
    ('ESTABILIZACAO', 'Estabilização', 'Sala de emergência para estabilizar o paciente grave.', 'URGENCIA'),
    ('TRANSFERENCIA', 'Transferência para outro serviço', 'Encaminha o paciente estabilizado a outro ponto da rede.', 'URGENCIA'),
    ('INTERNACAO_CLINICA', 'Internação clínica', 'Leitos de clínica médica.', 'INTERNACAO'),
    ('CIRURGIA', 'Cirurgia', 'Centro cirúrgico e internação cirúrgica.', 'INTERNACAO'),
    ('UTI', 'Terapia intensiva (UTI)', 'Leitos de terapia intensiva.', 'INTERNACAO'),
    ('OBSTETRICIA', 'Parto e obstetrícia', 'Assistência ao parto e leitos obstétricos.', 'INTERNACAO'),
    ('LEITO_SAUDE_MENTAL', 'Leito de saúde mental em hospital geral', 'Internação psiquiátrica de curta permanência.', 'INTERNACAO'),
    ('CONSULTA_ESPECIALIZADA', 'Consultas especializadas', 'Consultas em especialidades por referência.', 'ATENCAO_ESPECIALIZADA'),
    ('ODONTO_DIAGNOSTICO_BUCAL', 'Diagnóstico bucal', 'Com ênfase na detecção do câncer de boca (CEO).', 'ATENCAO_ESPECIALIZADA'),
    ('ODONTO_PERIODONTIA', 'Periodontia', 'Periodontia especializada (CEO).', 'ATENCAO_ESPECIALIZADA'),
    ('ODONTO_ENDODONTIA', 'Endodontia', 'Tratamento de canal (CEO).', 'ATENCAO_ESPECIALIZADA'),
    ('ODONTO_CIRURGIA_ORAL_MENOR', 'Cirurgia oral menor', 'Cirurgias orais de pequeno porte (CEO).', 'ATENCAO_ESPECIALIZADA'),
    ('ODONTO_PNE', 'Atendimento a pacientes com necessidades especiais', 'Odontologia para pessoas com deficiência (CEO).', 'ATENCAO_ESPECIALIZADA'),
    ('ODONTO_PROTESE', 'Prótese dentária', 'Reabilitação protética.', 'ATENCAO_ESPECIALIZADA'),
    ('CAPS_I', 'CAPS I', 'Modalidade de CAPS para municípios de menor porte.', 'ATENCAO_PSICOSSOCIAL'),
    ('CAPS_II', 'CAPS II', 'Modalidade de CAPS para municípios de médio porte.', 'ATENCAO_PSICOSSOCIAL'),
    ('CAPS_III', 'CAPS III', 'CAPS com funcionamento 24 horas e acolhimento noturno.', 'ATENCAO_PSICOSSOCIAL'),
    ('CAPS_INFANTOJUVENIL', 'CAPS infantojuvenil (CAPSi)', 'Atenção psicossocial a crianças e adolescentes.', 'ATENCAO_PSICOSSOCIAL'),
    ('CAPS_AD', 'CAPS álcool e drogas (CAPS AD)', 'Atenção a pessoas com uso de álcool e outras drogas.', 'ATENCAO_PSICOSSOCIAL'),
    ('CAPS_AD_III', 'CAPS AD III', 'CAPS AD com funcionamento 24 horas.', 'ATENCAO_PSICOSSOCIAL'),
    ('ACOLHIMENTO_NOTURNO', 'Acolhimento noturno', 'Leitos de acolhimento noturno no CAPS.', 'ATENCAO_PSICOSSOCIAL'),
    ('COLETA_DE_AMOSTRAS', 'Coleta de amostras', 'Coleta de material para exame.', 'DIAGNOSTICO'),
    ('ANALISES_CLINICAS', 'Análises clínicas', 'Processamento e análise de amostras, com resultado.', 'DIAGNOSTICO'),
    ('DIAGNOSTICO_POR_IMAGEM', 'Diagnóstico por imagem', 'Raio-X, ultrassonografia, tomografia.', 'DIAGNOSTICO'),
    ('DISPENSACAO', 'Dispensação de medicamentos', 'Dispensação com controle de lote e validade.', 'ASSISTENCIA_FARMACEUTICA'),
    ('DISPENSACAO_CONTROLADOS', 'Dispensação de controlados', 'Medicamentos sujeitos a controle especial (Portaria SVS/MS 344/1998).', 'ASSISTENCIA_FARMACEUTICA'),
    ('INSPECAO_SANITARIA', 'Inspeção sanitária', 'Inspeção de estabelecimentos.', 'VIGILANCIA'),
    ('LICENCIAMENTO_SANITARIO', 'Licenciamento sanitário', 'Emissão de licença sanitária.', 'VIGILANCIA'),
    ('CONTROLE_DE_ZOONOSES', 'Controle de zoonoses', 'Controle de animais e zoonoses.', 'VIGILANCIA'),
    ('CONTROLE_DE_VETORES', 'Controle de vetores', 'Vistoria de imóveis e controle de focos.', 'VIGILANCIA'),
    ('REABILITACAO_FISICA', 'Reabilitação física', 'Modalidade física.', 'REABILITACAO'),
    ('REABILITACAO_INTELECTUAL', 'Reabilitação intelectual', 'Modalidade intelectual.', 'REABILITACAO'),
    ('REABILITACAO_AUDITIVA', 'Reabilitação auditiva', 'Modalidade auditiva.', 'REABILITACAO'),
    ('REABILITACAO_VISUAL', 'Reabilitação visual', 'Modalidade visual.', 'REABILITACAO'),
    ('DISPENSACAO_OPM', 'Concessão de órteses, próteses e meios de locomoção', 'OPM para pessoas com deficiência.', 'REABILITACAO'),
    ('ATENDIMENTO_PRE_HOSPITALAR', 'Atendimento pré-hospitalar', 'Atendimento móvel no local da ocorrência.', 'ATENCAO_MOVEL'),
    ('REGULACAO_MEDICA_URGENCIA', 'Regulação médica de urgência', 'Central de regulação das urgências (192).', 'ATENCAO_MOVEL')
) AS c(codigo, nome, descricao, perfil)
LEFT JOIN public.tb_perfil_operacional p ON p.codigo = c.perfil;

-- ── Unidades existentes: o perfil do tipo delas. Capacidades não, porque não dá para adivinhar (um CAPS é II ou AD?) ──

INSERT INTO public.tb_unidade_perfil_operacional (uuid, unidade_id, perfil_operacional_id, inicio)
SELECT gen_random_uuid(), u.uuid, p.uuid, CURRENT_DATE
FROM public.tb_unidade_de_saude u
JOIN (VALUES
    ('UBS', 'ATENCAO_PRIMARIA'),
    ('UPA', 'URGENCIA'),
    ('HOSPITAL', 'INTERNACAO'),
    ('HOSPITAL', 'URGENCIA'),
    ('CAPS', 'ATENCAO_PSICOSSOCIAL'),
    ('LABORATORIO', 'DIAGNOSTICO'),
    ('CENTRO_ESPECIALIDADES', 'ATENCAO_ESPECIALIZADA'),
    ('POLICLINICA', 'ATENCAO_ESPECIALIZADA'),
    ('POLICLINICA', 'DIAGNOSTICO'),
    ('CENTRO_REABILITACAO', 'REABILITACAO')
) AS m(tipo, perfil) ON u.tipo = m.tipo
JOIN public.tb_perfil_operacional p ON p.codigo = m.perfil;
