-- Equipamentos de Saúde (ADR-0101): CNES, situação operacional e 24 horas na unidade; horário estruturado em turnos;
-- histórico de situação. Tabelas novas geradas com ddl-auto=create num banco descartável e conferidas (ADR-0090).

ALTER TABLE public.tb_unidade_de_saude ADD COLUMN cnes character varying(7);

ALTER TABLE ONLY public.tb_unidade_de_saude
    ADD CONSTRAINT tb_unidade_de_saude_cnes_key UNIQUE (cnes);

ALTER TABLE public.tb_unidade_de_saude ADD COLUMN situacao_operacional character varying(255) DEFAULT 'EM_OPERACAO' NOT NULL;

ALTER TABLE public.tb_unidade_de_saude ALTER COLUMN situacao_operacional DROP DEFAULT;

ALTER TABLE public.tb_unidade_de_saude ADD CONSTRAINT tb_unidade_de_saude_situacao_operacional_check CHECK (((situacao_operacional)::text = ANY ((ARRAY['EM_OPERACAO'::character varying, 'EM_MANUTENCAO'::character varying, 'EM_OBRA'::character varying, 'INOPERANTE'::character varying])::text[])));

ALTER TABLE public.tb_unidade_de_saude ADD COLUMN motivo_situacao character varying(500);

ALTER TABLE public.tb_unidade_de_saude ADD COLUMN previsao_retorno date;

ALTER TABLE public.tb_unidade_de_saude ADD COLUMN funciona_24h boolean DEFAULT false NOT NULL;

ALTER TABLE public.tb_unidade_de_saude ALTER COLUMN funciona_24h DROP DEFAULT;

-- Hospital e UPA funcionam 24 horas.
UPDATE public.tb_unidade_de_saude SET funciona_24h = true WHERE tipo IN ('HOSPITAL', 'UPA');

CREATE TABLE public.tb_evento_situacao_unidade (
    previsao_retorno date,
    ocorrido_em timestamp(6) with time zone NOT NULL,
    unidade_id uuid NOT NULL,
    uuid uuid NOT NULL,
    motivo character varying(500),
    registrado_por_cpf character varying(255),
    situacao character varying(255) NOT NULL,
    situacao_anterior character varying(255) NOT NULL,
    CONSTRAINT tb_evento_situacao_unidade_situacao_anterior_check CHECK (((situacao_anterior)::text = ANY ((ARRAY['EM_OPERACAO'::character varying, 'EM_MANUTENCAO'::character varying, 'EM_OBRA'::character varying, 'INOPERANTE'::character varying])::text[]))),
    CONSTRAINT tb_evento_situacao_unidade_situacao_check CHECK (((situacao)::text = ANY ((ARRAY['EM_OPERACAO'::character varying, 'EM_MANUTENCAO'::character varying, 'EM_OBRA'::character varying, 'INOPERANTE'::character varying])::text[])))
);

CREATE TABLE public.tb_horario_unidade (
    abre time(0) without time zone NOT NULL,
    fecha time(0) without time zone NOT NULL,
    unidade_id uuid NOT NULL,
    uuid uuid NOT NULL,
    dia_semana character varying(255) NOT NULL,
    CONSTRAINT tb_horario_unidade_dia_semana_check CHECK (((dia_semana)::text = ANY ((ARRAY['MONDAY'::character varying, 'TUESDAY'::character varying, 'WEDNESDAY'::character varying, 'THURSDAY'::character varying, 'FRIDAY'::character varying, 'SATURDAY'::character varying, 'SUNDAY'::character varying])::text[])))
);

ALTER TABLE ONLY public.tb_evento_situacao_unidade
    ADD CONSTRAINT tb_evento_situacao_unidade_pkey PRIMARY KEY (uuid);

ALTER TABLE ONLY public.tb_horario_unidade
    ADD CONSTRAINT tb_horario_unidade_pkey PRIMARY KEY (uuid);

CREATE INDEX ix_evento_situacao_unidade ON public.tb_evento_situacao_unidade USING btree (unidade_id, ocorrido_em);

CREATE INDEX ix_horario_unidade ON public.tb_horario_unidade USING btree (unidade_id, dia_semana);

ALTER TABLE ONLY public.tb_horario_unidade
    ADD CONSTRAINT fkg2xple47d1t6kyere6nmmwqgk FOREIGN KEY (unidade_id) REFERENCES public.tb_unidade_de_saude(uuid);

ALTER TABLE ONLY public.tb_evento_situacao_unidade
    ADD CONSTRAINT fkilqyugnl3opjijg1c0a890vuv FOREIGN KEY (unidade_id) REFERENCES public.tb_unidade_de_saude(uuid);
