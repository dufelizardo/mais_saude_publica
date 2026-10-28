-- Programas, fatia P5 (ADR-0152): linhas de cuidado. A linha é catálogo com as etapas como dado; o percurso é o paciente
-- na linha; a situação das etapas é calculada dos registros dos domínios, e só os registros manuais e as dispensas são
-- guardados.

CREATE TABLE public.tb_linha_cuidado (
    ativa boolean NOT NULL,
    criada_em timestamp(6) with time zone NOT NULL,
    programa_id uuid,
    uuid uuid NOT NULL,
    codigo character varying(60) NOT NULL,
    nome character varying(200) NOT NULL,
    descricao character varying(1000),
    registrado_por_cpf character varying(255)
);
ALTER TABLE ONLY public.tb_linha_cuidado ADD CONSTRAINT tb_linha_cuidado_pkey PRIMARY KEY (uuid);
ALTER TABLE ONLY public.tb_linha_cuidado ADD CONSTRAINT uk_linha_cuidado_codigo UNIQUE (codigo);
ALTER TABLE ONLY public.tb_linha_cuidado ADD CONSTRAINT fk_linha_cuidado_programa FOREIGN KEY (programa_id) REFERENCES public.tb_programa(uuid);

CREATE TABLE public.tb_etapa_linha_cuidado (
    ativa boolean NOT NULL,
    obrigatoria boolean NOT NULL,
    ordem integer NOT NULL,
    prazo_dias integer,
    quantidade integer NOT NULL,
    especialidade_id uuid,
    exame_id uuid,
    imunobiologico_id uuid,
    linha_id uuid NOT NULL,
    procedimento_id uuid,
    uuid uuid NOT NULL,
    cids character varying(200),
    nome character varying(200) NOT NULL,
    prazo_desde character varying(255) NOT NULL,
    tipo character varying(255) NOT NULL,
    CONSTRAINT tb_etapa_linha_cuidado_prazo_desde_check CHECK (((prazo_desde)::text = ANY ((ARRAY['INICIO'::character varying, 'ETAPA_ANTERIOR'::character varying])::text[]))),
    CONSTRAINT tb_etapa_linha_cuidado_tipo_check CHECK (((tipo)::text = ANY ((ARRAY['ATENDIMENTO'::character varying, 'EXAME'::character varying, 'REGULACAO'::character varying, 'VACINA'::character varying, 'INTERNACAO'::character varying, 'MANUAL'::character varying])::text[])))
);
ALTER TABLE ONLY public.tb_etapa_linha_cuidado ADD CONSTRAINT tb_etapa_linha_cuidado_pkey PRIMARY KEY (uuid);
ALTER TABLE ONLY public.tb_etapa_linha_cuidado ADD CONSTRAINT fk_etapa_linha_cuidado_linha FOREIGN KEY (linha_id) REFERENCES public.tb_linha_cuidado(uuid);
ALTER TABLE ONLY public.tb_etapa_linha_cuidado ADD CONSTRAINT fk_etapa_linha_cuidado_especialidade FOREIGN KEY (especialidade_id) REFERENCES public.tb_especialidade(uuid);
ALTER TABLE ONLY public.tb_etapa_linha_cuidado ADD CONSTRAINT fk_etapa_linha_cuidado_exame FOREIGN KEY (exame_id) REFERENCES public.tb_exame_laboratorial(uuid);
ALTER TABLE ONLY public.tb_etapa_linha_cuidado ADD CONSTRAINT fk_etapa_linha_cuidado_procedimento FOREIGN KEY (procedimento_id) REFERENCES public.tb_procedimento_regulado(uuid);
ALTER TABLE ONLY public.tb_etapa_linha_cuidado ADD CONSTRAINT fk_etapa_linha_cuidado_imunobiologico FOREIGN KEY (imunobiologico_id) REFERENCES public.tb_imunobiologico(uuid);
CREATE INDEX ix_etapa_linha_cuidado_linha ON public.tb_etapa_linha_cuidado USING btree (linha_id, ordem);

CREATE TABLE public.tb_percurso_linha_cuidado (
    encerrado_em date,
    inicio date NOT NULL,
    registrado_em timestamp(6) with time zone NOT NULL,
    acompanhamento_id uuid,
    equipe_id uuid,
    linha_id uuid NOT NULL,
    paciente_id uuid NOT NULL,
    unidade_id uuid NOT NULL,
    uuid uuid NOT NULL,
    motivo_encerramento character varying(500),
    registrado_por_cpf character varying(255),
    situacao character varying(255) NOT NULL,
    CONSTRAINT tb_percurso_linha_cuidado_situacao_check CHECK (((situacao)::text = ANY ((ARRAY['EM_CURSO'::character varying, 'CONCLUIDO'::character varying, 'INTERROMPIDO'::character varying])::text[])))
);
ALTER TABLE ONLY public.tb_percurso_linha_cuidado ADD CONSTRAINT tb_percurso_linha_cuidado_pkey PRIMARY KEY (uuid);
ALTER TABLE ONLY public.tb_percurso_linha_cuidado ADD CONSTRAINT fk_percurso_linha_cuidado_linha FOREIGN KEY (linha_id) REFERENCES public.tb_linha_cuidado(uuid);
ALTER TABLE ONLY public.tb_percurso_linha_cuidado ADD CONSTRAINT fk_percurso_linha_cuidado_paciente FOREIGN KEY (paciente_id) REFERENCES public.tb_paciente(uuid);
ALTER TABLE ONLY public.tb_percurso_linha_cuidado ADD CONSTRAINT fk_percurso_linha_cuidado_unidade FOREIGN KEY (unidade_id) REFERENCES public.tb_unidade_de_saude(uuid);
ALTER TABLE ONLY public.tb_percurso_linha_cuidado ADD CONSTRAINT fk_percurso_linha_cuidado_equipe FOREIGN KEY (equipe_id) REFERENCES public.tb_equipe(uuid);
ALTER TABLE ONLY public.tb_percurso_linha_cuidado ADD CONSTRAINT fk_percurso_linha_cuidado_acompanhamento FOREIGN KEY (acompanhamento_id) REFERENCES public.tb_acompanhamento_programatico(uuid);
CREATE INDEX ix_percurso_linha_cuidado_paciente ON public.tb_percurso_linha_cuidado USING btree (paciente_id, linha_id);

CREATE TABLE public.tb_registro_etapa_percurso (
    data date NOT NULL,
    registrado_em timestamp(6) with time zone NOT NULL,
    etapa_id uuid NOT NULL,
    percurso_id uuid NOT NULL,
    profissional_id uuid,
    uuid uuid NOT NULL,
    observacao character varying(500),
    registrado_por_cpf character varying(255),
    tipo character varying(255) NOT NULL,
    CONSTRAINT tb_registro_etapa_percurso_tipo_check CHECK (((tipo)::text = ANY ((ARRAY['CUMPRIDA'::character varying, 'DISPENSADA'::character varying])::text[])))
);
ALTER TABLE ONLY public.tb_registro_etapa_percurso ADD CONSTRAINT tb_registro_etapa_percurso_pkey PRIMARY KEY (uuid);
ALTER TABLE ONLY public.tb_registro_etapa_percurso ADD CONSTRAINT fk_registro_etapa_percurso_percurso FOREIGN KEY (percurso_id) REFERENCES public.tb_percurso_linha_cuidado(uuid);
ALTER TABLE ONLY public.tb_registro_etapa_percurso ADD CONSTRAINT fk_registro_etapa_percurso_etapa FOREIGN KEY (etapa_id) REFERENCES public.tb_etapa_linha_cuidado(uuid);
ALTER TABLE ONLY public.tb_registro_etapa_percurso ADD CONSTRAINT fk_registro_etapa_percurso_profissional FOREIGN KEY (profissional_id) REFERENCES public.tb_profissional(uuid);
CREATE INDEX ix_registro_etapa_percurso_percurso ON public.tb_registro_etapa_percurso USING btree (percurso_id);

-- Carga inicial editável: as linhas da gestante e da primeira semana da criança. Etapas de exame e de regulação
-- dependem do catálogo do município e entram pelo editor de etapas.
INSERT INTO public.tb_linha_cuidado (uuid, codigo, nome, descricao, programa_id, ativa, criada_em)
SELECT gen_random_uuid(), v.codigo, v.nome, v.descricao, p.uuid, true, now()
FROM (VALUES
    ('GESTANTE', 'Gestante: do pré-natal ao puerpério',
     'Percurso da gestação contado da data da última menstruação: captação até a 12ª semana, testes rápidos, dTpa, seis consultas, parto e consulta puerperal.',
     'PRE_NATAL'),
    ('PRIMEIRA_SEMANA_CRIANCA', 'Primeira semana de saúde da criança',
     'Percurso contado do nascimento: consulta do recém-nascido, teste do pezinho e as vacinas ao nascer.', 'SAUDE_DA_CRIANCA')
) AS v(codigo, nome, descricao, programa)
LEFT JOIN public.tb_programa p ON p.codigo = v.programa;

INSERT INTO public.tb_etapa_linha_cuidado (uuid, linha_id, ordem, nome, tipo, imunobiologico_id, cids, quantidade, prazo_dias, prazo_desde,
    obrigatoria, ativa)
SELECT gen_random_uuid(), l.uuid, v.ordem, v.nome, v.tipo, i.uuid, v.cids, v.quantidade, v.prazo, v.desde, true, true
FROM (VALUES
    ('GESTANTE', 1, '1ª consulta de pré-natal (até a 12ª semana)', 'ATENDIMENTO', NULL, NULL, 1, 84, 'INICIO'),
    ('GESTANTE', 2, 'Testes rápidos de sífilis, HIV e hepatites', 'MANUAL', NULL, NULL, 1, 84, 'INICIO'),
    ('GESTANTE', 3, 'Vacina dTpa (até a 36ª semana)', 'VACINA', 'DTPA', NULL, 1, 252, 'INICIO'),
    ('GESTANTE', 4, 'Seis consultas de pré-natal (até a 40ª semana)', 'ATENDIMENTO', NULL, NULL, 6, 280, 'INICIO'),
    ('GESTANTE', 5, 'Parto (até a 42ª semana)', 'INTERNACAO', NULL, 'O80,O81,O82,O83,O84', 1, 294, 'INICIO'),
    ('GESTANTE', 6, 'Consulta puerperal (até 42 dias do parto)', 'ATENDIMENTO', NULL, NULL, 1, 42, 'ETAPA_ANTERIOR'),
    ('PRIMEIRA_SEMANA_CRIANCA', 1, 'Consulta do recém-nascido (até 7 dias)', 'ATENDIMENTO', NULL, NULL, 1, 7, 'INICIO'),
    ('PRIMEIRA_SEMANA_CRIANCA', 2, 'Teste do pezinho (até 5 dias)', 'MANUAL', NULL, NULL, 1, 5, 'INICIO'),
    ('PRIMEIRA_SEMANA_CRIANCA', 3, 'BCG (até 30 dias)', 'VACINA', 'BCG', NULL, 1, 30, 'INICIO'),
    ('PRIMEIRA_SEMANA_CRIANCA', 4, 'Hepatite B ao nascer (até 30 dias)', 'VACINA', 'HEPATITE_B', NULL, 1, 30, 'INICIO')
) AS v(linha, ordem, nome, tipo, imuno, cids, quantidade, prazo, desde)
JOIN public.tb_linha_cuidado l ON l.codigo = v.linha
LEFT JOIN public.tb_imunobiologico i ON i.codigo = v.imuno;
