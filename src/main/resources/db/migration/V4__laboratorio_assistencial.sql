-- Laboratório assistencial (ADR-0093): catálogo de exames, pedido com itens, amostras, resultados (append-only,
-- com retificação) e eventos. Gerado com ddl-auto=create num banco descartável e conferido (ADR-0090).

CREATE TABLE public.tb_amostra_exame (
    coletada_em timestamp(6) without time zone NOT NULL,
    rejeitada_em timestamp(6) with time zone,
    coletada_por_id uuid NOT NULL,
    laboratorio_id uuid NOT NULL,
    pedido_id uuid NOT NULL,
    rejeitada_por_id uuid,
    unidade_coleta_id uuid NOT NULL,
    uuid uuid NOT NULL,
    codigo character varying(20) NOT NULL,
    observacao_rejeicao character varying(500),
    material character varying(255) NOT NULL,
    motivo_rejeicao character varying(255),
    registrado_por_cpf character varying(255),
    CONSTRAINT tb_amostra_exame_material_check CHECK (((material)::text = ANY ((ARRAY['SANGUE'::character varying, 'URINA'::character varying, 'FEZES'::character varying, 'SECRECAO'::character varying, 'OUTRO'::character varying])::text[]))),
    CONSTRAINT tb_amostra_exame_motivo_rejeicao_check CHECK (((motivo_rejeicao)::text = ANY ((ARRAY['HEMOLISADA'::character varying, 'INSUFICIENTE'::character varying, 'COAGULADA'::character varying, 'IDENTIFICACAO_INCORRETA'::character varying, 'OUTRO'::character varying])::text[])))
);

CREATE TABLE public.tb_evento_exame (
    ocorrido_em timestamp(6) with time zone NOT NULL,
    item_id uuid,
    pedido_id uuid NOT NULL,
    profissional_id uuid NOT NULL,
    uuid uuid NOT NULL,
    texto character varying(1000),
    registrado_por_cpf character varying(255),
    tipo character varying(255) NOT NULL,
    CONSTRAINT tb_evento_exame_tipo_check CHECK (((tipo)::text = ANY ((ARRAY['SOLICITACAO'::character varying, 'COLETA'::character varying, 'REJEICAO_AMOSTRA'::character varying, 'RESULTADO'::character varying, 'LIBERACAO'::character varying, 'RETIFICACAO'::character varying, 'CANCELAMENTO'::character varying])::text[])))
);

CREATE TABLE public.tb_exame_laboratorial (
    ativo boolean NOT NULL,
    prazo_dias integer,
    referencia_maxima numeric(14,4),
    referencia_minima numeric(14,4),
    uuid uuid NOT NULL,
    unidade_medida character varying(30),
    nome character varying(200) NOT NULL,
    referencia_texto character varying(200),
    preparo character varying(500),
    material character varying(255) NOT NULL,
    tipo_resultado character varying(255) NOT NULL,
    CONSTRAINT tb_exame_laboratorial_material_check CHECK (((material)::text = ANY ((ARRAY['SANGUE'::character varying, 'URINA'::character varying, 'FEZES'::character varying, 'SECRECAO'::character varying, 'OUTRO'::character varying])::text[]))),
    CONSTRAINT tb_exame_laboratorial_tipo_resultado_check CHECK (((tipo_resultado)::text = ANY ((ARRAY['NUMERICO'::character varying, 'TEXTO'::character varying])::text[])))
);

CREATE TABLE public.tb_item_pedido_exame (
    amostra_id uuid,
    exame_id uuid NOT NULL,
    pedido_id uuid NOT NULL,
    resultado_atual_id uuid,
    uuid uuid NOT NULL,
    motivo_cancelamento character varying(1000),
    status character varying(255) NOT NULL,
    CONSTRAINT tb_item_pedido_exame_status_check CHECK (((status)::text = ANY ((ARRAY['SOLICITADO'::character varying, 'COLETADO'::character varying, 'RESULTADO_REGISTRADO'::character varying, 'LIBERADO'::character varying, 'CANCELADO'::character varying])::text[])))
);

CREATE TABLE public.tb_pedido_exame (
    cid character varying(8),
    solicitado_em timestamp(6) with time zone NOT NULL,
    atendimento_id uuid,
    paciente_id uuid NOT NULL,
    profissional_solicitante_id uuid NOT NULL,
    unidade_solicitante_id uuid NOT NULL,
    uuid uuid NOT NULL,
    indicacao_clinica character varying(2000) NOT NULL,
    prioridade character varying(255) NOT NULL,
    solicitado_por_cpf character varying(255),
    CONSTRAINT tb_pedido_exame_prioridade_check CHECK (((prioridade)::text = ANY ((ARRAY['ROTINA'::character varying, 'URGENTE'::character varying])::text[])))
);

CREATE TABLE public.tb_resultado_exame (
    referencia_maxima numeric(14,4),
    referencia_minima numeric(14,4),
    valor_numerico numeric(14,4),
    liberado_em timestamp(6) with time zone,
    registrado_em timestamp(6) with time zone NOT NULL,
    analisado_por_id uuid NOT NULL,
    item_id uuid NOT NULL,
    liberado_por_id uuid,
    retificacao_de_id uuid,
    uuid uuid NOT NULL,
    unidade_medida character varying(30),
    referencia_texto character varying(200),
    motivo_retificacao character varying(1000),
    observacao character varying(1000),
    valor_texto character varying(2000),
    interpretacao character varying(255),
    registrado_por_cpf character varying(255),
    CONSTRAINT tb_resultado_exame_interpretacao_check CHECK (((interpretacao)::text = ANY ((ARRAY['NORMAL'::character varying, 'ACIMA'::character varying, 'ABAIXO'::character varying])::text[])))
);

ALTER TABLE ONLY public.tb_amostra_exame
    ADD CONSTRAINT tb_amostra_exame_codigo_key UNIQUE (codigo);

ALTER TABLE ONLY public.tb_amostra_exame
    ADD CONSTRAINT tb_amostra_exame_pkey PRIMARY KEY (uuid);

ALTER TABLE ONLY public.tb_evento_exame
    ADD CONSTRAINT tb_evento_exame_pkey PRIMARY KEY (uuid);

ALTER TABLE ONLY public.tb_exame_laboratorial
    ADD CONSTRAINT tb_exame_laboratorial_nome_key UNIQUE (nome);

ALTER TABLE ONLY public.tb_exame_laboratorial
    ADD CONSTRAINT tb_exame_laboratorial_pkey PRIMARY KEY (uuid);

ALTER TABLE ONLY public.tb_item_pedido_exame
    ADD CONSTRAINT tb_item_pedido_exame_pkey PRIMARY KEY (uuid);

ALTER TABLE ONLY public.tb_pedido_exame
    ADD CONSTRAINT tb_pedido_exame_pkey PRIMARY KEY (uuid);

ALTER TABLE ONLY public.tb_resultado_exame
    ADD CONSTRAINT tb_resultado_exame_pkey PRIMARY KEY (uuid);

ALTER TABLE ONLY public.tb_item_pedido_exame
    ADD CONSTRAINT fk2t6cx706rckoh14fcl4ea1x2b FOREIGN KEY (exame_id) REFERENCES public.tb_exame_laboratorial(uuid);

ALTER TABLE ONLY public.tb_amostra_exame
    ADD CONSTRAINT fk32dydnshniflknwpdmxtusfhp FOREIGN KEY (laboratorio_id) REFERENCES public.tb_unidade_de_saude(uuid);

ALTER TABLE ONLY public.tb_resultado_exame
    ADD CONSTRAINT fk40o2ju3cm18xvvqxrh553o1n9 FOREIGN KEY (retificacao_de_id) REFERENCES public.tb_resultado_exame(uuid);

ALTER TABLE ONLY public.tb_evento_exame
    ADD CONSTRAINT fk4km8k1q6ldxcxwal0ntys924d FOREIGN KEY (profissional_id) REFERENCES public.tb_profissional(uuid);

ALTER TABLE ONLY public.tb_evento_exame
    ADD CONSTRAINT fk64qogok5af3noemmm33bl3omi FOREIGN KEY (pedido_id) REFERENCES public.tb_pedido_exame(uuid);

ALTER TABLE ONLY public.tb_amostra_exame
    ADD CONSTRAINT fk73r5qbsjxaqjdutj5pqjkg82l FOREIGN KEY (pedido_id) REFERENCES public.tb_pedido_exame(uuid);

ALTER TABLE ONLY public.tb_pedido_exame
    ADD CONSTRAINT fk7bdwbwpccud61rc8a0ge77ryy FOREIGN KEY (paciente_id) REFERENCES public.tb_paciente(uuid);

ALTER TABLE ONLY public.tb_amostra_exame
    ADD CONSTRAINT fk81i0sarlrrm9d1ej2k9pp07hd FOREIGN KEY (coletada_por_id) REFERENCES public.tb_profissional(uuid);

ALTER TABLE ONLY public.tb_pedido_exame
    ADD CONSTRAINT fk92kqrkpbtiybmpibgdfqgftv3 FOREIGN KEY (profissional_solicitante_id) REFERENCES public.tb_profissional(uuid);

ALTER TABLE ONLY public.tb_amostra_exame
    ADD CONSTRAINT fkb9w2xmbu7fqyh2lqsh7mh85lc FOREIGN KEY (rejeitada_por_id) REFERENCES public.tb_profissional(uuid);

ALTER TABLE ONLY public.tb_pedido_exame
    ADD CONSTRAINT fkdu668bnpky8lb46vi8tbe0coy FOREIGN KEY (unidade_solicitante_id) REFERENCES public.tb_unidade_de_saude(uuid);

ALTER TABLE ONLY public.tb_resultado_exame
    ADD CONSTRAINT fkfoxsjkrffebvfmaaev1njvxmd FOREIGN KEY (analisado_por_id) REFERENCES public.tb_profissional(uuid);

ALTER TABLE ONLY public.tb_item_pedido_exame
    ADD CONSTRAINT fkh3x50ruqotsy8a9o9iaxx6cgg FOREIGN KEY (amostra_id) REFERENCES public.tb_amostra_exame(uuid);

ALTER TABLE ONLY public.tb_item_pedido_exame
    ADD CONSTRAINT fkk1funui92d0wec8y8to6wjkon FOREIGN KEY (pedido_id) REFERENCES public.tb_pedido_exame(uuid);

ALTER TABLE ONLY public.tb_pedido_exame
    ADD CONSTRAINT fkmh00e1a358urtpd2yuvux5jsb FOREIGN KEY (atendimento_id) REFERENCES public.tb_atendimento(uuid);

ALTER TABLE ONLY public.tb_evento_exame
    ADD CONSTRAINT fknf6oli0jrwyk8n6mixtlven1s FOREIGN KEY (item_id) REFERENCES public.tb_item_pedido_exame(uuid);

ALTER TABLE ONLY public.tb_item_pedido_exame
    ADD CONSTRAINT fknjy5x112qf191xoypxechie16 FOREIGN KEY (resultado_atual_id) REFERENCES public.tb_resultado_exame(uuid);

ALTER TABLE ONLY public.tb_resultado_exame
    ADD CONSTRAINT fkq5nto0m39plsdcviyiu676j7p FOREIGN KEY (liberado_por_id) REFERENCES public.tb_profissional(uuid);

ALTER TABLE ONLY public.tb_resultado_exame
    ADD CONSTRAINT fkqfjikru4c764i284eeon4785f FOREIGN KEY (item_id) REFERENCES public.tb_item_pedido_exame(uuid);

ALTER TABLE ONLY public.tb_amostra_exame
    ADD CONSTRAINT fkqm7a5y4eby00pbhdeeyo365sj FOREIGN KEY (unidade_coleta_id) REFERENCES public.tb_unidade_de_saude(uuid);

-- Leituras: lista de trabalho por situação, itens do pedido e pedidos do paciente.
CREATE INDEX ix_item_pedido_exame_status ON public.tb_item_pedido_exame USING btree (status);
CREATE INDEX ix_item_pedido_exame_pedido ON public.tb_item_pedido_exame USING btree (pedido_id);
CREATE INDEX ix_pedido_exame_paciente ON public.tb_pedido_exame USING btree (paciente_id, solicitado_em);
CREATE INDEX ix_evento_exame_pedido ON public.tb_evento_exame USING btree (pedido_id, ocorrido_em);
