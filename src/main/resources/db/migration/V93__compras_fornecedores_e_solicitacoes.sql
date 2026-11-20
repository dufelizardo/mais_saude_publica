-- Compras, Contratos e Fornecedores, fatia CP1 (ADR-0212): o cadastro do fornecedor com as certidões de habilitação e as sanções
-- (Lei 14.133/2021, art. 156), a solicitação de compra (DFD) com os itens e a pesquisa de preços com as cotações. As permissões
-- COMPRAS.SOLICITAR e COMPRAS.GERENCIAR e o papel COMPRAS entram pelo CatalogoDeAcesso. Conferido contra o ddl-auto=validate (ADR-0090).

CREATE TABLE public.tb_fornecedor (
    ativo boolean NOT NULL,
    pessoa_juridica boolean NOT NULL,
    registrado_em timestamp(6) with time zone NOT NULL,
    uuid uuid NOT NULL,
    documento character varying(14) NOT NULL,
    telefone character varying(30),
    email character varying(150),
    razao_social character varying(200) NOT NULL,
    nome_fantasia character varying(200),
    endereco character varying(500),
    registrado_por_cpf character varying(255)
);
ALTER TABLE ONLY public.tb_fornecedor ADD CONSTRAINT tb_fornecedor_pkey PRIMARY KEY (uuid);
ALTER TABLE ONLY public.tb_fornecedor ADD CONSTRAINT uk_fornecedor_documento UNIQUE (documento);

CREATE TABLE public.tb_certidao_fornecedor (
    emitida_em date NOT NULL,
    valida_ate date NOT NULL,
    registrado_em timestamp(6) with time zone NOT NULL,
    fornecedor_id uuid NOT NULL,
    uuid uuid NOT NULL,
    numero character varying(60),
    tipo character varying(255) NOT NULL,
    registrado_por_cpf character varying(255),
    CONSTRAINT tb_certidao_fornecedor_tipo_check CHECK (((tipo)::text = ANY ((ARRAY['FEDERAL'::character varying, 'FGTS'::character varying, 'TRABALHISTA'::character varying, 'ESTADUAL'::character varying, 'MUNICIPAL'::character varying])::text[])))
);
ALTER TABLE ONLY public.tb_certidao_fornecedor ADD CONSTRAINT tb_certidao_fornecedor_pkey PRIMARY KEY (uuid);
CREATE INDEX ix_certidao_fornecedor ON public.tb_certidao_fornecedor USING btree (fornecedor_id);
ALTER TABLE ONLY public.tb_certidao_fornecedor ADD CONSTRAINT fk_certidao_fornecedor FOREIGN KEY (fornecedor_id) REFERENCES public.tb_fornecedor(uuid);

CREATE TABLE public.tb_sancao_fornecedor (
    fim date,
    inicio date NOT NULL,
    revogada_em date,
    valor numeric(14,2),
    registrado_em timestamp(6) with time zone NOT NULL,
    fornecedor_id uuid NOT NULL,
    uuid uuid NOT NULL,
    processo character varying(60),
    motivo_revogacao character varying(500),
    motivo character varying(1000) NOT NULL,
    tipo character varying(255) NOT NULL,
    registrado_por_cpf character varying(255),
    CONSTRAINT tb_sancao_fornecedor_tipo_check CHECK (((tipo)::text = ANY ((ARRAY['ADVERTENCIA'::character varying, 'MULTA'::character varying, 'IMPEDIMENTO'::character varying, 'INIDONEIDADE'::character varying])::text[])))
);
ALTER TABLE ONLY public.tb_sancao_fornecedor ADD CONSTRAINT tb_sancao_fornecedor_pkey PRIMARY KEY (uuid);
CREATE INDEX ix_sancao_fornecedor ON public.tb_sancao_fornecedor USING btree (fornecedor_id);
ALTER TABLE ONLY public.tb_sancao_fornecedor ADD CONSTRAINT fk_sancao_fornecedor FOREIGN KEY (fornecedor_id) REFERENCES public.tb_fornecedor(uuid);

CREATE TABLE public.tb_solicitacao_compra (
    analisada_em timestamp(6) with time zone,
    enviada_em timestamp(6) with time zone,
    pesquisa_concluida_em timestamp(6) with time zone,
    registrado_em timestamp(6) with time zone NOT NULL,
    setor_id uuid,
    unidade_id uuid NOT NULL,
    uuid uuid NOT NULL,
    numero character varying(20) NOT NULL,
    motivo character varying(1000),
    justificativa character varying(2000) NOT NULL,
    analisada_por_cpf character varying(255),
    prioridade character varying(255) NOT NULL,
    situacao character varying(255) NOT NULL,
    registrado_por_cpf character varying(255),
    CONSTRAINT tb_solicitacao_compra_prioridade_check CHECK (((prioridade)::text = ANY ((ARRAY['NORMAL'::character varying, 'URGENTE'::character varying])::text[]))),
    CONSTRAINT tb_solicitacao_compra_situacao_check CHECK (((situacao)::text = ANY ((ARRAY['RASCUNHO'::character varying, 'ENVIADA'::character varying, 'DEVOLVIDA'::character varying, 'APROVADA'::character varying, 'REJEITADA'::character varying, 'EM_COTACAO'::character varying, 'COTADA'::character varying, 'CANCELADA'::character varying])::text[])))
);
ALTER TABLE ONLY public.tb_solicitacao_compra ADD CONSTRAINT tb_solicitacao_compra_pkey PRIMARY KEY (uuid);
ALTER TABLE ONLY public.tb_solicitacao_compra ADD CONSTRAINT uk_solicitacao_compra_numero UNIQUE (numero);
CREATE INDEX ix_solicitacao_compra_unidade ON public.tb_solicitacao_compra USING btree (unidade_id, situacao);
ALTER TABLE ONLY public.tb_solicitacao_compra ADD CONSTRAINT fk_solicitacao_compra_unidade FOREIGN KEY (unidade_id) REFERENCES public.tb_unidade_de_saude(uuid);
ALTER TABLE ONLY public.tb_solicitacao_compra ADD CONSTRAINT fk_solicitacao_compra_setor FOREIGN KEY (setor_id) REFERENCES public.tb_setor(uuid);

CREATE TABLE public.tb_item_solicitacao_compra (
    ordem integer NOT NULL,
    quantidade integer NOT NULL,
    preco_referencia numeric(14,4),
    material_id uuid,
    medicamento_id uuid,
    solicitacao_id uuid NOT NULL,
    uuid uuid NOT NULL,
    unidade_medida character varying(30) NOT NULL,
    descricao character varying(500) NOT NULL,
    justificativa_cotacao character varying(1000),
    tipo character varying(255) NOT NULL,
    CONSTRAINT tb_item_solicitacao_compra_tipo_check CHECK (((tipo)::text = ANY ((ARRAY['MATERIAL'::character varying, 'MEDICAMENTO'::character varying, 'SERVICO'::character varying])::text[])))
);
ALTER TABLE ONLY public.tb_item_solicitacao_compra ADD CONSTRAINT tb_item_solicitacao_compra_pkey PRIMARY KEY (uuid);
CREATE INDEX ix_item_solicitacao_compra ON public.tb_item_solicitacao_compra USING btree (solicitacao_id);
ALTER TABLE ONLY public.tb_item_solicitacao_compra ADD CONSTRAINT fk_item_solicitacao_compra FOREIGN KEY (solicitacao_id) REFERENCES public.tb_solicitacao_compra(uuid);
ALTER TABLE ONLY public.tb_item_solicitacao_compra ADD CONSTRAINT fk_item_solicitacao_compra_material FOREIGN KEY (material_id) REFERENCES public.tb_material_estoque(uuid);
ALTER TABLE ONLY public.tb_item_solicitacao_compra ADD CONSTRAINT fk_item_solicitacao_compra_medicamento FOREIGN KEY (medicamento_id) REFERENCES public.tb_medicamento(uuid);

CREATE TABLE public.tb_cotacao_compra (
    cotado_em date NOT NULL,
    preco_unitario numeric(14,4) NOT NULL,
    registrado_em timestamp(6) with time zone NOT NULL,
    fornecedor_id uuid,
    item_id uuid NOT NULL,
    uuid uuid NOT NULL,
    referencia character varying(300),
    fonte character varying(255) NOT NULL,
    registrado_por_cpf character varying(255),
    CONSTRAINT tb_cotacao_compra_fonte_check CHECK (((fonte)::text = ANY ((ARRAY['FORNECEDOR'::character varying, 'CONTRATACAO_PUBLICA'::character varying, 'SITE'::character varying, 'OUTRA'::character varying])::text[])))
);
ALTER TABLE ONLY public.tb_cotacao_compra ADD CONSTRAINT tb_cotacao_compra_pkey PRIMARY KEY (uuid);
CREATE INDEX ix_cotacao_compra_item ON public.tb_cotacao_compra USING btree (item_id);
ALTER TABLE ONLY public.tb_cotacao_compra ADD CONSTRAINT fk_cotacao_compra_item FOREIGN KEY (item_id) REFERENCES public.tb_item_solicitacao_compra(uuid);
ALTER TABLE ONLY public.tb_cotacao_compra ADD CONSTRAINT fk_cotacao_compra_fornecedor FOREIGN KEY (fornecedor_id) REFERENCES public.tb_fornecedor(uuid);
