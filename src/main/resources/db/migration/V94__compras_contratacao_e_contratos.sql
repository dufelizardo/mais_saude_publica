-- Compras, fatia CP2 (ADR-0213, desenho do domínio na ADR-0212): a contratação a partir das solicitações cotadas, o resultado por
-- item, a homologação que gera a ata de registro de preços ou o contrato por fornecedor, com o gestor e o fiscal, os itens com saldo e
-- os aditivos (Lei 14.133/2021, arts. 84, 105 a 107, 117 e 125). Conferido contra o ddl-auto=validate (ADR-0090).

CREATE TABLE public.tb_contratacao (
    homologada_em date,
    registrado_em timestamp(6) with time zone NOT NULL,
    uuid uuid NOT NULL,
    numero_processo character varying(60) NOT NULL,
    ata_origem character varying(200),
    fundamento_legal character varying(300),
    motivo_cancelamento character varying(500),
    objeto character varying(1000) NOT NULL,
    modalidade character varying(255) NOT NULL,
    situacao character varying(255) NOT NULL,
    registrado_por_cpf character varying(255),
    CONSTRAINT tb_contratacao_modalidade_check CHECK (((modalidade)::text = ANY ((ARRAY['PREGAO_ELETRONICO'::character varying, 'CONCORRENCIA'::character varying, 'DISPENSA'::character varying, 'INEXIGIBILIDADE'::character varying, 'ADESAO_ATA'::character varying])::text[]))),
    CONSTRAINT tb_contratacao_situacao_check CHECK (((situacao)::text = ANY ((ARRAY['EM_ANDAMENTO'::character varying, 'HOMOLOGADA'::character varying, 'CANCELADA'::character varying])::text[])))
);
ALTER TABLE ONLY public.tb_contratacao ADD CONSTRAINT tb_contratacao_pkey PRIMARY KEY (uuid);
ALTER TABLE ONLY public.tb_contratacao ADD CONSTRAINT uk_contratacao_processo UNIQUE (numero_processo);

CREATE TABLE public.tb_item_contratacao (
    ordem integer NOT NULL,
    quantidade integer NOT NULL,
    preco_referencia numeric(14,4),
    preco_unitario numeric(14,4),
    contratacao_id uuid NOT NULL,
    fornecedor_id uuid,
    material_id uuid,
    medicamento_id uuid,
    uuid uuid NOT NULL,
    unidade_medida character varying(30) NOT NULL,
    descricao character varying(500) NOT NULL,
    justificativa_preco character varying(1000),
    resultado character varying(255) NOT NULL,
    tipo character varying(255) NOT NULL,
    CONSTRAINT tb_item_contratacao_resultado_check CHECK (((resultado)::text = ANY ((ARRAY['PENDENTE'::character varying, 'ADJUDICADO'::character varying, 'FRACASSADO'::character varying, 'DESERTO'::character varying])::text[]))),
    CONSTRAINT tb_item_contratacao_tipo_check CHECK (((tipo)::text = ANY ((ARRAY['MATERIAL'::character varying, 'MEDICAMENTO'::character varying, 'SERVICO'::character varying])::text[])))
);
ALTER TABLE ONLY public.tb_item_contratacao ADD CONSTRAINT tb_item_contratacao_pkey PRIMARY KEY (uuid);
CREATE INDEX ix_item_contratacao ON public.tb_item_contratacao USING btree (contratacao_id);
ALTER TABLE ONLY public.tb_item_contratacao ADD CONSTRAINT fk_item_contratacao FOREIGN KEY (contratacao_id) REFERENCES public.tb_contratacao(uuid);
ALTER TABLE ONLY public.tb_item_contratacao ADD CONSTRAINT fk_item_contratacao_fornecedor FOREIGN KEY (fornecedor_id) REFERENCES public.tb_fornecedor(uuid);
ALTER TABLE ONLY public.tb_item_contratacao ADD CONSTRAINT fk_item_contratacao_material FOREIGN KEY (material_id) REFERENCES public.tb_material_estoque(uuid);
ALTER TABLE ONLY public.tb_item_contratacao ADD CONSTRAINT fk_item_contratacao_medicamento FOREIGN KEY (medicamento_id) REFERENCES public.tb_medicamento(uuid);

-- A solicitação cotada entra numa contratação; o item dela aponta o item agregado da contratação.
ALTER TABLE public.tb_solicitacao_compra ADD COLUMN contratacao_id uuid;
ALTER TABLE ONLY public.tb_solicitacao_compra ADD CONSTRAINT fk_solicitacao_compra_contratacao FOREIGN KEY (contratacao_id) REFERENCES public.tb_contratacao(uuid);
ALTER TABLE public.tb_item_solicitacao_compra ADD COLUMN item_contratacao_id uuid;
ALTER TABLE ONLY public.tb_item_solicitacao_compra ADD CONSTRAINT fk_item_solicitacao_item_contratacao FOREIGN KEY (item_contratacao_id) REFERENCES public.tb_item_contratacao(uuid);
ALTER TABLE public.tb_solicitacao_compra DROP CONSTRAINT tb_solicitacao_compra_situacao_check;
ALTER TABLE public.tb_solicitacao_compra ADD CONSTRAINT tb_solicitacao_compra_situacao_check CHECK (((situacao)::text = ANY ((ARRAY['RASCUNHO'::character varying, 'ENVIADA'::character varying, 'DEVOLVIDA'::character varying, 'APROVADA'::character varying, 'REJEITADA'::character varying, 'EM_COTACAO'::character varying, 'COTADA'::character varying, 'CONTRATADA'::character varying, 'CANCELADA'::character varying])::text[])));

CREATE TABLE public.tb_instrumento_contratual (
    fim date NOT NULL,
    inicio date NOT NULL,
    servico_continuo boolean NOT NULL,
    valor_inicial numeric(14,2) NOT NULL,
    registrado_em timestamp(6) with time zone NOT NULL,
    contratacao_id uuid NOT NULL,
    fiscal_id uuid NOT NULL,
    fornecedor_id uuid NOT NULL,
    gestor_id uuid NOT NULL,
    uuid uuid NOT NULL,
    numero character varying(60) NOT NULL,
    tipo character varying(255) NOT NULL,
    registrado_por_cpf character varying(255),
    CONSTRAINT tb_instrumento_contratual_tipo_check CHECK (((tipo)::text = ANY ((ARRAY['ATA_REGISTRO_PRECOS'::character varying, 'CONTRATO'::character varying])::text[])))
);
ALTER TABLE ONLY public.tb_instrumento_contratual ADD CONSTRAINT tb_instrumento_contratual_pkey PRIMARY KEY (uuid);
ALTER TABLE ONLY public.tb_instrumento_contratual ADD CONSTRAINT uk_instrumento_contratual_numero UNIQUE (numero);
ALTER TABLE ONLY public.tb_instrumento_contratual ADD CONSTRAINT fk_instrumento_contratacao FOREIGN KEY (contratacao_id) REFERENCES public.tb_contratacao(uuid);
ALTER TABLE ONLY public.tb_instrumento_contratual ADD CONSTRAINT fk_instrumento_fornecedor FOREIGN KEY (fornecedor_id) REFERENCES public.tb_fornecedor(uuid);
ALTER TABLE ONLY public.tb_instrumento_contratual ADD CONSTRAINT fk_instrumento_gestor FOREIGN KEY (gestor_id) REFERENCES public.tb_profissional(uuid);
ALTER TABLE ONLY public.tb_instrumento_contratual ADD CONSTRAINT fk_instrumento_fiscal FOREIGN KEY (fiscal_id) REFERENCES public.tb_profissional(uuid);

CREATE TABLE public.tb_item_instrumento (
    quantidade integer NOT NULL,
    quantidade_inicial integer NOT NULL,
    saldo integer NOT NULL,
    preco_unitario numeric(14,4) NOT NULL,
    instrumento_id uuid NOT NULL,
    item_contratacao_id uuid NOT NULL,
    uuid uuid NOT NULL
);
ALTER TABLE ONLY public.tb_item_instrumento ADD CONSTRAINT tb_item_instrumento_pkey PRIMARY KEY (uuid);
CREATE INDEX ix_item_instrumento ON public.tb_item_instrumento USING btree (instrumento_id);
ALTER TABLE ONLY public.tb_item_instrumento ADD CONSTRAINT fk_item_instrumento FOREIGN KEY (instrumento_id) REFERENCES public.tb_instrumento_contratual(uuid);
ALTER TABLE ONLY public.tb_item_instrumento ADD CONSTRAINT fk_item_instrumento_item FOREIGN KEY (item_contratacao_id) REFERENCES public.tb_item_contratacao(uuid);

CREATE TABLE public.tb_aditivo_contratual (
    assinado_em date NOT NULL,
    fim_anterior date,
    novo_fim date,
    numero integer NOT NULL,
    quantidade integer,
    novo_preco numeric(14,4),
    preco_anterior numeric(14,4),
    registrado_em timestamp(6) with time zone NOT NULL,
    instrumento_id uuid NOT NULL,
    item_instrumento_id uuid,
    uuid uuid NOT NULL,
    justificativa character varying(1000) NOT NULL,
    tipo character varying(255) NOT NULL,
    registrado_por_cpf character varying(255),
    CONSTRAINT tb_aditivo_contratual_tipo_check CHECK (((tipo)::text = ANY ((ARRAY['PRAZO'::character varying, 'ACRESCIMO'::character varying, 'SUPRESSAO'::character varying, 'REEQUILIBRIO'::character varying])::text[])))
);
ALTER TABLE ONLY public.tb_aditivo_contratual ADD CONSTRAINT tb_aditivo_contratual_pkey PRIMARY KEY (uuid);
CREATE INDEX ix_aditivo_contratual ON public.tb_aditivo_contratual USING btree (instrumento_id);
ALTER TABLE ONLY public.tb_aditivo_contratual ADD CONSTRAINT fk_aditivo_instrumento FOREIGN KEY (instrumento_id) REFERENCES public.tb_instrumento_contratual(uuid);
ALTER TABLE ONLY public.tb_aditivo_contratual ADD CONSTRAINT fk_aditivo_item_instrumento FOREIGN KEY (item_instrumento_id) REFERENCES public.tb_item_instrumento(uuid);
