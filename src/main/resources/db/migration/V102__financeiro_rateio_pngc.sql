-- Financeiro, fatia F4 (ADR-0221): o rateio do Programa Nacional de Gestão de Custos, em degraus, dos centros administrativos e de apoio para os
-- produtivos da mesma unidade. O critério e a área ficam no centro; a lotação do RH ganha o setor, para a folha ir ao centro do setor. A apuração
-- guarda o resumo por centro (direto, recebido, enviado, internação) e as linhas do rateio, e vira fotografia ao fechar com o resto. O não rateado
-- é nulo nas apurações feitas antes desta fatia. Conferido contra o ddl-auto=validate (ADR-0090).

ALTER TABLE public.tb_lotacao ADD COLUMN setor_id uuid;
ALTER TABLE ONLY public.tb_lotacao ADD CONSTRAINT fk_lotacao_setor FOREIGN KEY (setor_id) REFERENCES public.tb_setor(uuid);

ALTER TABLE public.tb_centro_custo ADD COLUMN criterio_rateio character varying(255);
ALTER TABLE public.tb_centro_custo ADD COLUMN area_m2 numeric(10,2);
ALTER TABLE public.tb_centro_custo ADD CONSTRAINT tb_centro_custo_criterio_rateio_check CHECK (((criterio_rateio)::text = ANY ((ARRAY['PESSOAS'::character varying,
    'CUSTO_DIRETO'::character varying, 'AREA'::character varying, 'REQUISICOES'::character varying])::text[])));

ALTER TABLE public.tb_apuracao_custo ADD COLUMN nao_rateado numeric(15,2);

CREATE TABLE public.tb_custo_centro_apuracao (
    direto numeric(15,2) NOT NULL,
    enviado numeric(15,2) NOT NULL,
    internacao boolean NOT NULL,
    recebido numeric(15,2) NOT NULL,
    apuracao_id uuid NOT NULL,
    centro_id uuid NOT NULL,
    uuid uuid NOT NULL
);
ALTER TABLE ONLY public.tb_custo_centro_apuracao ADD CONSTRAINT tb_custo_centro_apuracao_pkey PRIMARY KEY (uuid);
ALTER TABLE ONLY public.tb_custo_centro_apuracao ADD CONSTRAINT uk_custo_centro_apuracao UNIQUE (apuracao_id, centro_id);
ALTER TABLE ONLY public.tb_custo_centro_apuracao ADD CONSTRAINT fk_custo_centro_apuracao_apuracao FOREIGN KEY (apuracao_id) REFERENCES public.tb_apuracao_custo(uuid);
ALTER TABLE ONLY public.tb_custo_centro_apuracao ADD CONSTRAINT fk_custo_centro_apuracao_centro FOREIGN KEY (centro_id) REFERENCES public.tb_centro_custo(uuid) ON DELETE CASCADE;

CREATE TABLE public.tb_rateio_apuracao_custo (
    ordem integer NOT NULL,
    valor numeric(15,2) NOT NULL,
    apuracao_id uuid NOT NULL,
    destino_id uuid NOT NULL,
    origem_id uuid NOT NULL,
    uuid uuid NOT NULL,
    criterio character varying(255) NOT NULL,
    CONSTRAINT tb_rateio_apuracao_custo_criterio_check CHECK (((criterio)::text = ANY ((ARRAY['PESSOAS'::character varying, 'CUSTO_DIRETO'::character varying,
        'AREA'::character varying, 'REQUISICOES'::character varying])::text[])))
);
ALTER TABLE ONLY public.tb_rateio_apuracao_custo ADD CONSTRAINT tb_rateio_apuracao_custo_pkey PRIMARY KEY (uuid);
ALTER TABLE ONLY public.tb_rateio_apuracao_custo ADD CONSTRAINT uk_rateio_apuracao_custo UNIQUE (apuracao_id, origem_id, destino_id);
ALTER TABLE ONLY public.tb_rateio_apuracao_custo ADD CONSTRAINT fk_rateio_apuracao_custo_apuracao FOREIGN KEY (apuracao_id) REFERENCES public.tb_apuracao_custo(uuid);
ALTER TABLE ONLY public.tb_rateio_apuracao_custo ADD CONSTRAINT fk_rateio_apuracao_custo_origem FOREIGN KEY (origem_id) REFERENCES public.tb_centro_custo(uuid) ON DELETE CASCADE;
ALTER TABLE ONLY public.tb_rateio_apuracao_custo ADD CONSTRAINT fk_rateio_apuracao_custo_destino FOREIGN KEY (destino_id) REFERENCES public.tb_centro_custo(uuid) ON DELETE CASCADE;
