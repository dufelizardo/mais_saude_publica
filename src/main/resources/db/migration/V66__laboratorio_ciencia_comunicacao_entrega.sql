-- Laboratório, fatia L2 (ADR-0181): ciência do solicitante no resultado, comunicação de valor crítico pelo laboratório,
-- entrega do resultado ao paciente e os tipos de evento novos. Conferido contra o ddl-auto=validate (ADR-0090).

ALTER TABLE public.tb_resultado_exame ADD COLUMN ciencia_por_id uuid;
ALTER TABLE public.tb_resultado_exame ADD COLUMN ciencia_em timestamp(6) with time zone;
ALTER TABLE ONLY public.tb_resultado_exame ADD CONSTRAINT fk_resultado_exame_ciencia_por FOREIGN KEY (ciencia_por_id) REFERENCES public.tb_profissional(uuid);

CREATE TABLE public.tb_comunicacao_critico_exame (
    comunicado_em timestamp(6) with time zone NOT NULL,
    item_id uuid NOT NULL,
    profissional_id uuid NOT NULL,
    resultado_id uuid NOT NULL,
    uuid uuid NOT NULL,
    receptor_nome character varying(150) NOT NULL,
    meio character varying(255) NOT NULL,
    registrado_por_cpf character varying(255),
    CONSTRAINT tb_comunicacao_critico_exame_meio_check CHECK (((meio)::text = ANY ((ARRAY['TELEFONE'::character varying, 'PRESENCIAL'::character varying, 'MENSAGEM_NO_SISTEMA'::character varying])::text[])))
);
ALTER TABLE ONLY public.tb_comunicacao_critico_exame ADD CONSTRAINT tb_comunicacao_critico_exame_pkey PRIMARY KEY (uuid);
ALTER TABLE ONLY public.tb_comunicacao_critico_exame ADD CONSTRAINT fk_comunicacao_critico_item FOREIGN KEY (item_id) REFERENCES public.tb_item_pedido_exame(uuid);
ALTER TABLE ONLY public.tb_comunicacao_critico_exame ADD CONSTRAINT fk_comunicacao_critico_resultado FOREIGN KEY (resultado_id) REFERENCES public.tb_resultado_exame(uuid);
ALTER TABLE ONLY public.tb_comunicacao_critico_exame ADD CONSTRAINT fk_comunicacao_critico_profissional FOREIGN KEY (profissional_id) REFERENCES public.tb_profissional(uuid);
CREATE INDEX ix_comunicacao_critico_resultado ON public.tb_comunicacao_critico_exame USING btree (resultado_id);
CREATE INDEX ix_comunicacao_critico_item ON public.tb_comunicacao_critico_exame USING btree (item_id);

CREATE TABLE public.tb_entrega_resultado_exame (
    retirado_pelo_paciente boolean NOT NULL,
    entregue_em timestamp(6) with time zone NOT NULL,
    pedido_id uuid NOT NULL,
    profissional_id uuid NOT NULL,
    unidade_id uuid NOT NULL,
    uuid uuid NOT NULL,
    terceiro_documento character varying(30),
    terceiro_parentesco character varying(50),
    terceiro_nome character varying(150),
    forma character varying(255) NOT NULL,
    registrado_por_cpf character varying(255),
    CONSTRAINT tb_entrega_resultado_exame_forma_check CHECK (((forma)::text = ANY ((ARRAY['IMPRESSO_NA_UNIDADE'::character varying, 'NA_CONSULTA'::character varying])::text[])))
);
ALTER TABLE ONLY public.tb_entrega_resultado_exame ADD CONSTRAINT tb_entrega_resultado_exame_pkey PRIMARY KEY (uuid);
ALTER TABLE ONLY public.tb_entrega_resultado_exame ADD CONSTRAINT fk_entrega_resultado_pedido FOREIGN KEY (pedido_id) REFERENCES public.tb_pedido_exame(uuid);
ALTER TABLE ONLY public.tb_entrega_resultado_exame ADD CONSTRAINT fk_entrega_resultado_profissional FOREIGN KEY (profissional_id) REFERENCES public.tb_profissional(uuid);
ALTER TABLE ONLY public.tb_entrega_resultado_exame ADD CONSTRAINT fk_entrega_resultado_unidade FOREIGN KEY (unidade_id) REFERENCES public.tb_unidade_de_saude(uuid);
CREATE INDEX ix_entrega_resultado_pedido ON public.tb_entrega_resultado_exame USING btree (pedido_id);

ALTER TABLE public.tb_evento_exame DROP CONSTRAINT tb_evento_exame_tipo_check;
ALTER TABLE public.tb_evento_exame ADD CONSTRAINT tb_evento_exame_tipo_check CHECK (((tipo)::text = ANY ((ARRAY['SOLICITACAO'::character varying, 'COLETA'::character varying, 'REJEICAO_AMOSTRA'::character varying, 'RESULTADO'::character varying, 'LIBERACAO'::character varying, 'RETIFICACAO'::character varying, 'CANCELAMENTO'::character varying, 'COMUNICACAO_CRITICO'::character varying, 'CIENCIA'::character varying, 'ENTREGA'::character varying])::text[])));
