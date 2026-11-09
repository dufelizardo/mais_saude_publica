-- Gestão da Rede, fatia GR1 (ADR-0179): oferta assistencial por executante, procedimento e competência, as cotas da PPI e a cota
-- consumida (ou a extracota) na solicitação de regulação. Conferido contra o ddl-auto=validate (ADR-0090).

CREATE TABLE public.tb_oferta_assistencial (
    competencia date NOT NULL,
    quantidade integer NOT NULL,
    atualizada_em timestamp(6) with time zone NOT NULL,
    procedimento_id uuid NOT NULL,
    unidade_executante_id uuid NOT NULL,
    uuid uuid NOT NULL,
    observacao character varying(500),
    atualizada_por_cpf character varying(255)
);
ALTER TABLE ONLY public.tb_oferta_assistencial ADD CONSTRAINT tb_oferta_assistencial_pkey PRIMARY KEY (uuid);
ALTER TABLE ONLY public.tb_oferta_assistencial ADD CONSTRAINT uk_oferta_assistencial UNIQUE (unidade_executante_id, procedimento_id, competencia);
ALTER TABLE ONLY public.tb_oferta_assistencial ADD CONSTRAINT fk_oferta_executante FOREIGN KEY (unidade_executante_id) REFERENCES public.tb_unidade_de_saude(uuid);
ALTER TABLE ONLY public.tb_oferta_assistencial ADD CONSTRAINT fk_oferta_procedimento FOREIGN KEY (procedimento_id) REFERENCES public.tb_procedimento_regulado(uuid);

CREATE TABLE public.tb_cota_ppi (
    quantidade integer NOT NULL,
    atualizada_em timestamp(6) with time zone NOT NULL,
    beneficiaria_id uuid NOT NULL,
    oferta_id uuid NOT NULL,
    uuid uuid NOT NULL,
    atualizada_por_cpf character varying(255)
);
ALTER TABLE ONLY public.tb_cota_ppi ADD CONSTRAINT tb_cota_ppi_pkey PRIMARY KEY (uuid);
ALTER TABLE ONLY public.tb_cota_ppi ADD CONSTRAINT uk_cota_ppi UNIQUE (oferta_id, beneficiaria_id);
ALTER TABLE ONLY public.tb_cota_ppi ADD CONSTRAINT fk_cota_oferta FOREIGN KEY (oferta_id) REFERENCES public.tb_oferta_assistencial(uuid);
ALTER TABLE ONLY public.tb_cota_ppi ADD CONSTRAINT fk_cota_beneficiaria FOREIGN KEY (beneficiaria_id) REFERENCES public.tb_unidade_de_saude(uuid);

ALTER TABLE public.tb_solicitacao_regulacao ADD COLUMN cota_ppi_id uuid;
ALTER TABLE public.tb_solicitacao_regulacao ADD COLUMN extra_cota boolean NOT NULL DEFAULT false;
ALTER TABLE public.tb_solicitacao_regulacao ADD COLUMN justificativa_extra_cota character varying(500);
ALTER TABLE ONLY public.tb_solicitacao_regulacao ADD CONSTRAINT fk_solicitacao_cota_ppi FOREIGN KEY (cota_ppi_id) REFERENCES public.tb_cota_ppi(uuid);
CREATE INDEX ix_solicitacao_cota_ppi ON public.tb_solicitacao_regulacao USING btree (cota_ppi_id);
