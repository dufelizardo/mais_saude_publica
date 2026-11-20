-- Compras, fatia CP3 (ADR-0214, desenho do domínio na ADR-0212): a ordem de fornecimento contra o saldo da ata ou do contrato, o
-- recebimento provisório e definitivo (Lei 14.133/2021, art. 140) com a entrada no Estoque e na Farmácia, as ocorrências com o
-- fornecedor e o vínculo da movimentação do Estoque com o fornecedor cadastrado. Conferido contra o ddl-auto=validate (ADR-0090).

CREATE TABLE public.tb_ordem_fornecimento (
    emitida_em date NOT NULL,
    prazo_em date NOT NULL,
    prazo_dias integer NOT NULL,
    registrado_em timestamp(6) with time zone NOT NULL,
    instrumento_id uuid NOT NULL,
    unidade_id uuid NOT NULL,
    uuid uuid NOT NULL,
    numero character varying(20) NOT NULL,
    empenho character varying(60),
    motivo_cancelamento character varying(500),
    situacao character varying(255) NOT NULL,
    registrado_por_cpf character varying(255),
    CONSTRAINT tb_ordem_fornecimento_situacao_check CHECK (((situacao)::text = ANY ((ARRAY['EMITIDA'::character varying, 'PARCIAL'::character varying, 'RECEBIDA'::character varying, 'CANCELADA'::character varying])::text[])))
);
ALTER TABLE ONLY public.tb_ordem_fornecimento ADD CONSTRAINT tb_ordem_fornecimento_pkey PRIMARY KEY (uuid);
ALTER TABLE ONLY public.tb_ordem_fornecimento ADD CONSTRAINT uk_ordem_fornecimento_numero UNIQUE (numero);
CREATE INDEX ix_ordem_fornecimento_instrumento ON public.tb_ordem_fornecimento USING btree (instrumento_id, situacao);
ALTER TABLE ONLY public.tb_ordem_fornecimento ADD CONSTRAINT fk_ordem_fornecimento_instrumento FOREIGN KEY (instrumento_id) REFERENCES public.tb_instrumento_contratual(uuid);
ALTER TABLE ONLY public.tb_ordem_fornecimento ADD CONSTRAINT fk_ordem_fornecimento_unidade FOREIGN KEY (unidade_id) REFERENCES public.tb_unidade_de_saude(uuid);

CREATE TABLE public.tb_item_ordem_fornecimento (
    quantidade integer NOT NULL,
    quantidade_aceita integer NOT NULL,
    item_instrumento_id uuid NOT NULL,
    ordem_id uuid NOT NULL,
    uuid uuid NOT NULL
);
ALTER TABLE ONLY public.tb_item_ordem_fornecimento ADD CONSTRAINT tb_item_ordem_fornecimento_pkey PRIMARY KEY (uuid);
CREATE INDEX ix_item_ordem_fornecimento ON public.tb_item_ordem_fornecimento USING btree (ordem_id);
ALTER TABLE ONLY public.tb_item_ordem_fornecimento ADD CONSTRAINT fk_item_ordem_fornecimento FOREIGN KEY (ordem_id) REFERENCES public.tb_ordem_fornecimento(uuid);
ALTER TABLE ONLY public.tb_item_ordem_fornecimento ADD CONSTRAINT fk_item_ordem_item_instrumento FOREIGN KEY (item_instrumento_id) REFERENCES public.tb_item_instrumento(uuid);

CREATE TABLE public.tb_recebimento_compra (
    definitivo_em date,
    nota_emitida_em date NOT NULL,
    recebido_em date NOT NULL,
    registrado_em timestamp(6) with time zone NOT NULL,
    ordem_id uuid NOT NULL,
    uuid uuid NOT NULL,
    nota_fiscal character varying(60) NOT NULL,
    observacao character varying(1000),
    situacao character varying(255) NOT NULL,
    atestado_por_cpf character varying(255),
    registrado_por_cpf character varying(255),
    CONSTRAINT tb_recebimento_compra_situacao_check CHECK (((situacao)::text = ANY ((ARRAY['PROVISORIO'::character varying, 'DEFINITIVO'::character varying])::text[])))
);
ALTER TABLE ONLY public.tb_recebimento_compra ADD CONSTRAINT tb_recebimento_compra_pkey PRIMARY KEY (uuid);
CREATE INDEX ix_recebimento_compra_ordem ON public.tb_recebimento_compra USING btree (ordem_id);
ALTER TABLE ONLY public.tb_recebimento_compra ADD CONSTRAINT fk_recebimento_compra_ordem FOREIGN KEY (ordem_id) REFERENCES public.tb_ordem_fornecimento(uuid);

CREATE TABLE public.tb_item_recebimento_compra (
    quantidade_aceita integer,
    quantidade_entregue integer NOT NULL,
    validade date,
    item_ordem_id uuid NOT NULL,
    recebimento_id uuid NOT NULL,
    uuid uuid NOT NULL,
    numero_lote character varying(60),
    motivo_recusa character varying(500)
);
ALTER TABLE ONLY public.tb_item_recebimento_compra ADD CONSTRAINT tb_item_recebimento_compra_pkey PRIMARY KEY (uuid);
CREATE INDEX ix_item_recebimento_compra ON public.tb_item_recebimento_compra USING btree (recebimento_id);
ALTER TABLE ONLY public.tb_item_recebimento_compra ADD CONSTRAINT fk_item_recebimento_compra FOREIGN KEY (recebimento_id) REFERENCES public.tb_recebimento_compra(uuid);
ALTER TABLE ONLY public.tb_item_recebimento_compra ADD CONSTRAINT fk_item_recebimento_item_ordem FOREIGN KEY (item_ordem_id) REFERENCES public.tb_item_ordem_fornecimento(uuid);

CREATE TABLE public.tb_ocorrencia_fornecedor (
    encerrada_em date,
    registrada_em date NOT NULL,
    registrado_em timestamp(6) with time zone NOT NULL,
    fornecedor_id uuid NOT NULL,
    ordem_id uuid,
    sancao_id uuid,
    uuid uuid NOT NULL,
    resposta character varying(1000),
    descricao character varying(1000) NOT NULL,
    situacao character varying(255) NOT NULL,
    tipo character varying(255) NOT NULL,
    registrado_por_cpf character varying(255),
    CONSTRAINT tb_ocorrencia_fornecedor_situacao_check CHECK (((situacao)::text = ANY ((ARRAY['ABERTA'::character varying, 'ENCERRADA'::character varying, 'SANCIONADA'::character varying])::text[]))),
    CONSTRAINT tb_ocorrencia_fornecedor_tipo_check CHECK (((tipo)::text = ANY ((ARRAY['ATRASO'::character varying, 'ENTREGA_PARCIAL'::character varying, 'FORA_ESPECIFICACAO'::character varying, 'OUTRA'::character varying])::text[])))
);
ALTER TABLE ONLY public.tb_ocorrencia_fornecedor ADD CONSTRAINT tb_ocorrencia_fornecedor_pkey PRIMARY KEY (uuid);
CREATE INDEX ix_ocorrencia_fornecedor ON public.tb_ocorrencia_fornecedor USING btree (fornecedor_id, situacao);
ALTER TABLE ONLY public.tb_ocorrencia_fornecedor ADD CONSTRAINT fk_ocorrencia_fornecedor FOREIGN KEY (fornecedor_id) REFERENCES public.tb_fornecedor(uuid);
ALTER TABLE ONLY public.tb_ocorrencia_fornecedor ADD CONSTRAINT fk_ocorrencia_ordem FOREIGN KEY (ordem_id) REFERENCES public.tb_ordem_fornecimento(uuid);
ALTER TABLE ONLY public.tb_ocorrencia_fornecedor ADD CONSTRAINT fk_ocorrencia_sancao FOREIGN KEY (sancao_id) REFERENCES public.tb_sancao_fornecedor(uuid);

-- A entrada no Estoque pelo recebimento liga o fornecedor cadastrado; o texto livre continua para as entradas avulsas (ADR-0177).
ALTER TABLE public.tb_movimentacao_estoque ADD COLUMN fornecedor_id uuid;
ALTER TABLE ONLY public.tb_movimentacao_estoque ADD CONSTRAINT fk_movimentacao_estoque_fornecedor FOREIGN KEY (fornecedor_id) REFERENCES public.tb_fornecedor(uuid);
