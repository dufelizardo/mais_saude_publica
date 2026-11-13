-- Laboratório, fatia L4 (ADR-0183): assinatura eletrônica interna do resultado (quem assinou e o hash do conteúdo), as emissões
-- do laudo em PDF com o código de verificação, os insumos de cada exame e o consumo sem saldo para o alerta do Estoque.
-- Conferido contra o ddl-auto=validate (ADR-0090).

ALTER TABLE public.tb_resultado_exame ADD COLUMN assinado_por_cpf character varying(255);
ALTER TABLE public.tb_resultado_exame ADD COLUMN hash_conteudo character varying(64);

CREATE TABLE public.tb_emissao_laudo_exame (
    versao integer NOT NULL,
    emitido_em timestamp(6) with time zone NOT NULL,
    pedido_id uuid NOT NULL,
    uuid uuid NOT NULL,
    codigo character varying(14) NOT NULL,
    hash_pdf character varying(64) NOT NULL,
    emitido_por_cpf character varying(255),
    pdf bytea NOT NULL
);
ALTER TABLE ONLY public.tb_emissao_laudo_exame ADD CONSTRAINT tb_emissao_laudo_exame_pkey PRIMARY KEY (uuid);
ALTER TABLE ONLY public.tb_emissao_laudo_exame ADD CONSTRAINT uk_emissao_laudo_exame_codigo UNIQUE (codigo);
ALTER TABLE ONLY public.tb_emissao_laudo_exame ADD CONSTRAINT fk_emissao_laudo_pedido FOREIGN KEY (pedido_id) REFERENCES public.tb_pedido_exame(uuid);
CREATE INDEX ix_emissao_laudo_pedido ON public.tb_emissao_laudo_exame USING btree (pedido_id);

CREATE TABLE public.tb_emissao_laudo_exame_resultado (
    emissao_id uuid NOT NULL,
    resultado_id uuid NOT NULL
);
ALTER TABLE ONLY public.tb_emissao_laudo_exame_resultado ADD CONSTRAINT tb_emissao_laudo_exame_resultado_pkey PRIMARY KEY (emissao_id, resultado_id);
ALTER TABLE ONLY public.tb_emissao_laudo_exame_resultado ADD CONSTRAINT fk_emissao_laudo_resultado_emissao FOREIGN KEY (emissao_id) REFERENCES public.tb_emissao_laudo_exame(uuid);
ALTER TABLE ONLY public.tb_emissao_laudo_exame_resultado ADD CONSTRAINT fk_emissao_laudo_resultado_resultado FOREIGN KEY (resultado_id) REFERENCES public.tb_resultado_exame(uuid);

CREATE TABLE public.tb_insumo_exame (
    quantidade integer NOT NULL,
    so_com_contraste boolean NOT NULL,
    exame_id uuid NOT NULL,
    material_id uuid NOT NULL,
    uuid uuid NOT NULL
);
ALTER TABLE ONLY public.tb_insumo_exame ADD CONSTRAINT tb_insumo_exame_pkey PRIMARY KEY (uuid);
ALTER TABLE ONLY public.tb_insumo_exame ADD CONSTRAINT uk_insumo_exame_material UNIQUE (exame_id, material_id);
ALTER TABLE ONLY public.tb_insumo_exame ADD CONSTRAINT fk_insumo_exame_exame FOREIGN KEY (exame_id) REFERENCES public.tb_exame_laboratorial(uuid);
ALTER TABLE ONLY public.tb_insumo_exame ADD CONSTRAINT fk_insumo_exame_material FOREIGN KEY (material_id) REFERENCES public.tb_material_estoque(uuid);

CREATE TABLE public.tb_consumo_sem_saldo (
    quantidade_faltante integer NOT NULL,
    ocorrido_em timestamp(6) with time zone NOT NULL,
    conferido_em timestamp(6) with time zone,
    material_id uuid NOT NULL,
    profissional_id uuid,
    unidade_id uuid NOT NULL,
    uuid uuid NOT NULL,
    origem character varying(300) NOT NULL,
    conferido_por_cpf character varying(255)
);
ALTER TABLE ONLY public.tb_consumo_sem_saldo ADD CONSTRAINT tb_consumo_sem_saldo_pkey PRIMARY KEY (uuid);
ALTER TABLE ONLY public.tb_consumo_sem_saldo ADD CONSTRAINT fk_consumo_sem_saldo_material FOREIGN KEY (material_id) REFERENCES public.tb_material_estoque(uuid);
ALTER TABLE ONLY public.tb_consumo_sem_saldo ADD CONSTRAINT fk_consumo_sem_saldo_profissional FOREIGN KEY (profissional_id) REFERENCES public.tb_profissional(uuid);
ALTER TABLE ONLY public.tb_consumo_sem_saldo ADD CONSTRAINT fk_consumo_sem_saldo_unidade FOREIGN KEY (unidade_id) REFERENCES public.tb_unidade_de_saude(uuid);
CREATE INDEX ix_consumo_sem_saldo_unidade ON public.tb_consumo_sem_saldo USING btree (unidade_id, conferido_em);
