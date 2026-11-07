-- Modelo Operacional, fatia MO3 (ADR-0166, desenho na ADR-0115): pontos operacionais da unidade, rotas e localização da
-- unidade móvel, e o local de realização do atendimento (sem valor, na unidade; os atendimentos existentes seguem válidos).
-- Conferido contra o ddl-auto=validate (ADR-0090).

CREATE TABLE public.tb_ponto_operacional (
    fim date,
    inicio date NOT NULL,
    latitude double precision,
    longitude double precision,
    unidade_id uuid NOT NULL,
    uuid uuid NOT NULL,
    tipo character varying(20) NOT NULL,
    nome character varying(150) NOT NULL,
    referencia character varying(300),
    motivo_fim character varying(500),
    registrado_por_cpf character varying(255),
    CONSTRAINT tb_ponto_operacional_tipo_check CHECK (tipo IN ('BASE', 'PONTO_FIXO', 'PONTO_PROGRAMADO'))
);
ALTER TABLE ONLY public.tb_ponto_operacional ADD CONSTRAINT tb_ponto_operacional_pkey PRIMARY KEY (uuid);
CREATE INDEX ix_ponto_operacional_unidade ON public.tb_ponto_operacional USING btree (unidade_id);
ALTER TABLE ONLY public.tb_ponto_operacional ADD CONSTRAINT fk_ponto_operacional_unidade FOREIGN KEY (unidade_id) REFERENCES public.tb_unidade_de_saude(uuid);

CREATE TABLE public.tb_rota_programada (
    fim date,
    hora_fim time(6) without time zone,
    hora_inicio time(6) without time zone,
    inicio date NOT NULL,
    unidade_id uuid NOT NULL,
    uuid uuid NOT NULL,
    nome character varying(150) NOT NULL,
    motivo_fim character varying(500),
    registrado_por_cpf character varying(255)
);
ALTER TABLE ONLY public.tb_rota_programada ADD CONSTRAINT tb_rota_programada_pkey PRIMARY KEY (uuid);
CREATE INDEX ix_rota_programada_unidade ON public.tb_rota_programada USING btree (unidade_id);
ALTER TABLE ONLY public.tb_rota_programada ADD CONSTRAINT fk_rota_programada_unidade FOREIGN KEY (unidade_id) REFERENCES public.tb_unidade_de_saude(uuid);

CREATE TABLE public.tb_rota_dia (
    rota_id uuid NOT NULL,
    dia character varying(10) NOT NULL,
    CONSTRAINT tb_rota_dia_dia_check CHECK (dia IN ('MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY', 'SUNDAY'))
);
ALTER TABLE ONLY public.tb_rota_dia ADD CONSTRAINT tb_rota_dia_pkey PRIMARY KEY (rota_id, dia);
ALTER TABLE ONLY public.tb_rota_dia ADD CONSTRAINT fk_rota_dia_rota FOREIGN KEY (rota_id) REFERENCES public.tb_rota_programada(uuid);

CREATE TABLE public.tb_rota_ponto (
    ordem integer NOT NULL,
    ponto_id uuid NOT NULL,
    rota_id uuid NOT NULL
);
ALTER TABLE ONLY public.tb_rota_ponto ADD CONSTRAINT tb_rota_ponto_pkey PRIMARY KEY (rota_id, ordem);
ALTER TABLE ONLY public.tb_rota_ponto ADD CONSTRAINT fk_rota_ponto_rota FOREIGN KEY (rota_id) REFERENCES public.tb_rota_programada(uuid);
ALTER TABLE ONLY public.tb_rota_ponto ADD CONSTRAINT fk_rota_ponto_ponto FOREIGN KEY (ponto_id) REFERENCES public.tb_ponto_operacional(uuid);

CREATE TABLE public.tb_posicao_unidade_movel (
    desde timestamp(6) without time zone NOT NULL,
    ponto_id uuid NOT NULL,
    unidade_id uuid NOT NULL,
    uuid uuid NOT NULL,
    observacao character varying(300),
    registrado_por_cpf character varying(255)
);
ALTER TABLE ONLY public.tb_posicao_unidade_movel ADD CONSTRAINT tb_posicao_unidade_movel_pkey PRIMARY KEY (uuid);
CREATE INDEX ix_posicao_movel_unidade ON public.tb_posicao_unidade_movel USING btree (unidade_id, desde);
ALTER TABLE ONLY public.tb_posicao_unidade_movel ADD CONSTRAINT fk_posicao_movel_unidade FOREIGN KEY (unidade_id) REFERENCES public.tb_unidade_de_saude(uuid);
ALTER TABLE ONLY public.tb_posicao_unidade_movel ADD CONSTRAINT fk_posicao_movel_ponto FOREIGN KEY (ponto_id) REFERENCES public.tb_ponto_operacional(uuid);

ALTER TABLE public.tb_atendimento ADD COLUMN local_realizacao character varying(20);
ALTER TABLE public.tb_atendimento ADD COLUMN local_descricao character varying(300);
ALTER TABLE public.tb_atendimento ADD COLUMN local_latitude double precision;
ALTER TABLE public.tb_atendimento ADD COLUMN local_longitude double precision;
ALTER TABLE public.tb_atendimento ADD COLUMN domicilio_id uuid;
ALTER TABLE public.tb_atendimento ADD COLUMN ponto_operacional_id uuid;
ALTER TABLE public.tb_atendimento ADD CONSTRAINT tb_atendimento_local_realizacao_check
    CHECK (local_realizacao IN ('DOMICILIO', 'VIA_PUBLICA', 'LOCAL_DO_EVENTO', 'ESCOLA', 'PONTO_DA_UNIDADE', 'OUTRO'));
ALTER TABLE ONLY public.tb_atendimento ADD CONSTRAINT fk_atendimento_domicilio FOREIGN KEY (domicilio_id) REFERENCES public.tb_domicilio(uuid);
ALTER TABLE ONLY public.tb_atendimento ADD CONSTRAINT fk_atendimento_ponto_operacional FOREIGN KEY (ponto_operacional_id) REFERENCES public.tb_ponto_operacional(uuid);
