-- Saúde Bucal, fatia SB3b (ADR-0137): o plano de tratamento odontológico e os itens, executados pelos procedimentos.
-- Um plano aberto por paciente (índice único parcial). Conferido contra o ddl-auto=validate (ADR-0090).

CREATE TABLE public.tb_plano_tratamento (
    aberto_em timestamp(6) with time zone NOT NULL,
    encerrado_em timestamp(6) with time zone,
    atendimento_id uuid NOT NULL,
    avaliacao_id uuid,
    paciente_id uuid NOT NULL,
    profissional_id uuid NOT NULL,
    unidade_id uuid NOT NULL,
    uuid uuid NOT NULL,
    motivo_encerramento character varying(500),
    objetivo character varying(1000),
    diagnostico character varying(2000),
    aberto_por_cpf character varying(255),
    encerrado_por_cpf character varying(255),
    prioridade character varying(255) NOT NULL,
    situacao character varying(255) NOT NULL,
    CONSTRAINT tb_plano_tratamento_prioridade_check CHECK (((prioridade)::text = ANY ((ARRAY['SEM_NECESSIDADE'::character varying, 'ELETIVA'::character varying, 'PRIORITARIA'::character varying, 'URGENCIA'::character varying])::text[]))),
    CONSTRAINT tb_plano_tratamento_situacao_check CHECK (((situacao)::text = ANY ((ARRAY['ABERTO'::character varying, 'CONCLUIDO'::character varying, 'ABANDONADO'::character varying])::text[])))
);

ALTER TABLE ONLY public.tb_plano_tratamento ADD CONSTRAINT tb_plano_tratamento_pkey PRIMARY KEY (uuid);
CREATE INDEX ix_plano_tratamento_paciente ON public.tb_plano_tratamento USING btree (paciente_id);
CREATE UNIQUE INDEX uk_plano_tratamento_aberto_por_paciente ON public.tb_plano_tratamento (paciente_id) WHERE ((situacao)::text = 'ABERTO'::text);
ALTER TABLE ONLY public.tb_plano_tratamento ADD CONSTRAINT fk_plano_tratamento_paciente FOREIGN KEY (paciente_id) REFERENCES public.tb_paciente(uuid);
ALTER TABLE ONLY public.tb_plano_tratamento ADD CONSTRAINT fk_plano_tratamento_unidade FOREIGN KEY (unidade_id) REFERENCES public.tb_unidade_de_saude(uuid);
ALTER TABLE ONLY public.tb_plano_tratamento ADD CONSTRAINT fk_plano_tratamento_atendimento FOREIGN KEY (atendimento_id) REFERENCES public.tb_atendimento(uuid);
ALTER TABLE ONLY public.tb_plano_tratamento ADD CONSTRAINT fk_plano_tratamento_avaliacao FOREIGN KEY (avaliacao_id) REFERENCES public.tb_avaliacao_odontologica(uuid);
ALTER TABLE ONLY public.tb_plano_tratamento ADD CONSTRAINT fk_plano_tratamento_profissional FOREIGN KEY (profissional_id) REFERENCES public.tb_profissional(uuid);

CREATE TABLE public.tb_item_plano_tratamento (
    dente integer,
    ordem integer NOT NULL,
    criado_em timestamp(6) with time zone NOT NULL,
    situacao_alterada_em timestamp(6) with time zone,
    plano_id uuid NOT NULL,
    procedimento_catalogo_id uuid NOT NULL,
    procedimento_id uuid,
    solicitacao_regulacao_id uuid,
    uuid uuid NOT NULL,
    motivo_situacao character varying(500),
    observacao character varying(500),
    criado_por_cpf character varying(255),
    situacao character varying(255) NOT NULL,
    situacao_alterada_por_cpf character varying(255),
    CONSTRAINT tb_item_plano_tratamento_situacao_check CHECK (((situacao)::text = ANY ((ARRAY['PLANEJADO'::character varying, 'EM_ANDAMENTO'::character varying, 'CONCLUIDO'::character varying, 'ENCAMINHADO'::character varying, 'CANCELADO'::character varying])::text[])))
);

ALTER TABLE ONLY public.tb_item_plano_tratamento ADD CONSTRAINT tb_item_plano_tratamento_pkey PRIMARY KEY (uuid);
CREATE INDEX ix_item_plano_tratamento_plano ON public.tb_item_plano_tratamento USING btree (plano_id);
CREATE INDEX ix_item_plano_tratamento_procedimento ON public.tb_item_plano_tratamento USING btree (procedimento_id);
ALTER TABLE ONLY public.tb_item_plano_tratamento ADD CONSTRAINT fk_item_plano_tratamento_plano FOREIGN KEY (plano_id) REFERENCES public.tb_plano_tratamento(uuid);
ALTER TABLE ONLY public.tb_item_plano_tratamento ADD CONSTRAINT fk_item_plano_tratamento_catalogo FOREIGN KEY (procedimento_catalogo_id) REFERENCES public.tb_procedimento_catalogo(uuid);
ALTER TABLE ONLY public.tb_item_plano_tratamento ADD CONSTRAINT fk_item_plano_tratamento_procedimento FOREIGN KEY (procedimento_id) REFERENCES public.tb_procedimento(uuid);
ALTER TABLE ONLY public.tb_item_plano_tratamento ADD CONSTRAINT fk_item_plano_tratamento_solicitacao FOREIGN KEY (solicitacao_regulacao_id) REFERENCES public.tb_solicitacao_regulacao(uuid);

CREATE TABLE public.tb_item_plano_tratamento_face (
    item_id uuid NOT NULL,
    face character varying(255) NOT NULL,
    CONSTRAINT tb_item_plano_tratamento_face_face_check CHECK (((face)::text = ANY ((ARRAY['MESIAL'::character varying, 'DISTAL'::character varying, 'OCLUSAL'::character varying, 'INCISAL'::character varying, 'VESTIBULAR'::character varying, 'LINGUAL_PALATINA'::character varying])::text[])))
);

ALTER TABLE ONLY public.tb_item_plano_tratamento_face ADD CONSTRAINT tb_item_plano_tratamento_face_pkey PRIMARY KEY (item_id, face);
ALTER TABLE ONLY public.tb_item_plano_tratamento_face ADD CONSTRAINT fk_item_plano_tratamento_face FOREIGN KEY (item_id) REFERENCES public.tb_item_plano_tratamento(uuid);
