-- Financeiro, fatia F2 (ADR-0219, desenho do domínio na ADR-0218): a liquidação a partir do atesto (recebimento definitivo, ordem de serviço
-- por contrato, diária do TFD) ou avulsa, com as retenções; o pagamento com a ordem bancária do SIAFIC e a ordem cronológica (Lei 14.133/2021,
-- art. 141); e a diária ou ajuda de custo do TFD. Conferido contra o ddl-auto=validate (ADR-0090).

CREATE TABLE public.tb_diaria_tfd (
    concedida_em date NOT NULL,
    dias integer NOT NULL,
    valor_diaria numeric(15,2) NOT NULL,
    valor_total numeric(15,2) NOT NULL,
    registrado_em timestamp(6) with time zone NOT NULL,
    credor_id uuid NOT NULL,
    solicitacao_id uuid NOT NULL,
    uuid uuid NOT NULL,
    observacao character varying(500),
    beneficiario character varying(255) NOT NULL,
    concedida_por_cpf character varying(255),
    CONSTRAINT tb_diaria_tfd_beneficiario_check CHECK (((beneficiario)::text = ANY ((ARRAY['PACIENTE'::character varying, 'ACOMPANHANTE'::character varying])::text[])))
);
ALTER TABLE ONLY public.tb_diaria_tfd ADD CONSTRAINT tb_diaria_tfd_pkey PRIMARY KEY (uuid);
ALTER TABLE ONLY public.tb_diaria_tfd ADD CONSTRAINT uk_diaria_tfd UNIQUE (solicitacao_id, beneficiario);
ALTER TABLE ONLY public.tb_diaria_tfd ADD CONSTRAINT fk_diaria_tfd_solicitacao FOREIGN KEY (solicitacao_id) REFERENCES public.tb_solicitacao_transporte(uuid);
ALTER TABLE ONLY public.tb_diaria_tfd ADD CONSTRAINT fk_diaria_tfd_credor FOREIGN KEY (credor_id) REFERENCES public.tb_fornecedor(uuid);

CREATE TABLE public.tb_liquidacao (
    liquidada_em date NOT NULL,
    nota_emitida_em date,
    paga_em date,
    vencimento date NOT NULL,
    inss numeric(15,2) NOT NULL,
    irrf numeric(15,2) NOT NULL,
    iss numeric(15,2) NOT NULL,
    outras_retencoes numeric(15,2) NOT NULL,
    valor_bruto numeric(15,2) NOT NULL,
    valor_liquido numeric(15,2) NOT NULL,
    registrado_em timestamp(6) with time zone NOT NULL,
    diaria_id uuid,
    empenho_id uuid NOT NULL,
    ordem_servico_id uuid,
    recebimento_id uuid,
    uuid uuid NOT NULL,
    nota_fiscal character varying(60),
    ordem_bancaria character varying(60),
    justificativa character varying(500),
    justificativa_fora_de_ordem character varying(500),
    atestado_por_cpf character varying(255),
    origem character varying(255) NOT NULL,
    pago_por_cpf character varying(255),
    registrado_por_cpf character varying(255),
    CONSTRAINT tb_liquidacao_origem_check CHECK (((origem)::text = ANY ((ARRAY['RECEBIMENTO'::character varying, 'ORDEM_SERVICO'::character varying, 'DIARIA_TFD'::character varying, 'AVULSA'::character varying])::text[])))
);
ALTER TABLE ONLY public.tb_liquidacao ADD CONSTRAINT tb_liquidacao_pkey PRIMARY KEY (uuid);
ALTER TABLE ONLY public.tb_liquidacao ADD CONSTRAINT uk_liquidacao_recebimento UNIQUE (recebimento_id);
ALTER TABLE ONLY public.tb_liquidacao ADD CONSTRAINT uk_liquidacao_ordem_servico UNIQUE (ordem_servico_id);
ALTER TABLE ONLY public.tb_liquidacao ADD CONSTRAINT uk_liquidacao_diaria UNIQUE (diaria_id);
CREATE INDEX ix_liquidacao_empenho ON public.tb_liquidacao USING btree (empenho_id);
ALTER TABLE ONLY public.tb_liquidacao ADD CONSTRAINT fk_liquidacao_empenho FOREIGN KEY (empenho_id) REFERENCES public.tb_empenho(uuid);
ALTER TABLE ONLY public.tb_liquidacao ADD CONSTRAINT fk_liquidacao_recebimento FOREIGN KEY (recebimento_id) REFERENCES public.tb_recebimento_compra(uuid);
ALTER TABLE ONLY public.tb_liquidacao ADD CONSTRAINT fk_liquidacao_ordem_servico FOREIGN KEY (ordem_servico_id) REFERENCES public.tb_ordem_servico_manutencao(uuid);
ALTER TABLE ONLY public.tb_liquidacao ADD CONSTRAINT fk_liquidacao_diaria FOREIGN KEY (diaria_id) REFERENCES public.tb_diaria_tfd(uuid);
