-- Financeiro, fatia F1 (ADR-0218): a dotação orçamentária do Fundo Municipal de Saúde com as alterações (suplementação e anulação), e o
-- empenho com o número do SIAFIC (Decreto 10.540/2020), o reforço e a anulação; a ordem de fornecimento passa a apontar o empenho.
-- Conferido contra o ddl-auto=validate (ADR-0090).

CREATE TABLE public.tb_dotacao_orcamentaria (
    exercicio integer NOT NULL,
    valor_inicial numeric(15,2) NOT NULL,
    registrado_em timestamp(6) with time zone NOT NULL,
    uuid uuid NOT NULL,
    natureza_despesa character varying(20) NOT NULL,
    fonte_codigo character varying(20),
    programa_trabalho character varying(40) NOT NULL,
    descricao character varying(300) NOT NULL,
    fonte character varying(255) NOT NULL,
    registrado_por_cpf character varying(255),
    CONSTRAINT tb_dotacao_orcamentaria_fonte_check CHECK (((fonte)::text = ANY ((ARRAY['RECURSOS_PROPRIOS'::character varying, 'BLOCO_MANUTENCAO'::character varying, 'BLOCO_ESTRUTURACAO'::character varying, 'TRANSFERENCIA_ESTADUAL'::character varying, 'OUTRAS'::character varying])::text[])))
);
ALTER TABLE ONLY public.tb_dotacao_orcamentaria ADD CONSTRAINT tb_dotacao_orcamentaria_pkey PRIMARY KEY (uuid);
ALTER TABLE ONLY public.tb_dotacao_orcamentaria ADD CONSTRAINT uk_dotacao_orcamentaria UNIQUE (exercicio, programa_trabalho, natureza_despesa, fonte);

CREATE TABLE public.tb_alteracao_dotacao (
    data date NOT NULL,
    valor numeric(15,2) NOT NULL,
    registrado_em timestamp(6) with time zone NOT NULL,
    dotacao_id uuid NOT NULL,
    uuid uuid NOT NULL,
    ato character varying(60) NOT NULL,
    motivo character varying(500),
    registrado_por_cpf character varying(255),
    tipo character varying(255) NOT NULL,
    CONSTRAINT tb_alteracao_dotacao_tipo_check CHECK (((tipo)::text = ANY ((ARRAY['SUPLEMENTACAO'::character varying, 'ANULACAO'::character varying])::text[])))
);
ALTER TABLE ONLY public.tb_alteracao_dotacao ADD CONSTRAINT tb_alteracao_dotacao_pkey PRIMARY KEY (uuid);
CREATE INDEX ix_alteracao_dotacao ON public.tb_alteracao_dotacao USING btree (dotacao_id);
ALTER TABLE ONLY public.tb_alteracao_dotacao ADD CONSTRAINT fk_alteracao_dotacao FOREIGN KEY (dotacao_id) REFERENCES public.tb_dotacao_orcamentaria(uuid);

CREATE TABLE public.tb_empenho (
    data date NOT NULL,
    exercicio integer NOT NULL,
    valor numeric(15,2) NOT NULL,
    registrado_em timestamp(6) with time zone NOT NULL,
    dotacao_id uuid NOT NULL,
    fornecedor_id uuid NOT NULL,
    instrumento_id uuid,
    uuid uuid NOT NULL,
    numero character varying(30) NOT NULL,
    historico character varying(500),
    registrado_por_cpf character varying(255),
    tipo character varying(255) NOT NULL,
    CONSTRAINT tb_empenho_tipo_check CHECK (((tipo)::text = ANY ((ARRAY['ORDINARIO'::character varying, 'ESTIMATIVO'::character varying, 'GLOBAL'::character varying])::text[])))
);
ALTER TABLE ONLY public.tb_empenho ADD CONSTRAINT tb_empenho_pkey PRIMARY KEY (uuid);
ALTER TABLE ONLY public.tb_empenho ADD CONSTRAINT uk_empenho_numero UNIQUE (exercicio, numero);
CREATE INDEX ix_empenho_dotacao ON public.tb_empenho USING btree (dotacao_id);
CREATE INDEX ix_empenho_fornecedor ON public.tb_empenho USING btree (fornecedor_id);
ALTER TABLE ONLY public.tb_empenho ADD CONSTRAINT fk_empenho_dotacao FOREIGN KEY (dotacao_id) REFERENCES public.tb_dotacao_orcamentaria(uuid);
ALTER TABLE ONLY public.tb_empenho ADD CONSTRAINT fk_empenho_fornecedor FOREIGN KEY (fornecedor_id) REFERENCES public.tb_fornecedor(uuid);
ALTER TABLE ONLY public.tb_empenho ADD CONSTRAINT fk_empenho_instrumento FOREIGN KEY (instrumento_id) REFERENCES public.tb_instrumento_contratual(uuid);

CREATE TABLE public.tb_movimento_empenho (
    data date NOT NULL,
    valor numeric(15,2) NOT NULL,
    registrado_em timestamp(6) with time zone NOT NULL,
    empenho_id uuid NOT NULL,
    uuid uuid NOT NULL,
    motivo character varying(500) NOT NULL,
    registrado_por_cpf character varying(255),
    tipo character varying(255) NOT NULL,
    CONSTRAINT tb_movimento_empenho_tipo_check CHECK (((tipo)::text = ANY ((ARRAY['REFORCO'::character varying, 'ANULACAO'::character varying])::text[])))
);
ALTER TABLE ONLY public.tb_movimento_empenho ADD CONSTRAINT tb_movimento_empenho_pkey PRIMARY KEY (uuid);
CREATE INDEX ix_movimento_empenho ON public.tb_movimento_empenho USING btree (empenho_id);
ALTER TABLE ONLY public.tb_movimento_empenho ADD CONSTRAINT fk_movimento_empenho FOREIGN KEY (empenho_id) REFERENCES public.tb_empenho(uuid);

-- A ordem de fornecimento aponta o empenho (ADR-0218); o número em texto continua nas ordens anteriores e em quem ainda não lança o empenho.
ALTER TABLE public.tb_ordem_fornecimento ADD COLUMN empenho_id uuid;
CREATE INDEX ix_ordem_fornecimento_empenho ON public.tb_ordem_fornecimento USING btree (empenho_id);
ALTER TABLE ONLY public.tb_ordem_fornecimento ADD CONSTRAINT fk_ordem_fornecimento_empenho FOREIGN KEY (empenho_id) REFERENCES public.tb_empenho(uuid);
