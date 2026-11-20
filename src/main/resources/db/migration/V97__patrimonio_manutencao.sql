-- Patrimônio e Manutenção, fatia PM2 (ADR-0216, desenho do domínio na ADR-0215): o plano de manutenção do bem (preventiva, calibração e
-- segurança elétrica, RDC 509/2021), a ordem de serviço que nasce do chamado ou do plano vencido, executada pela equipe interna ou por um
-- contrato de serviço (ADR-0213), e as peças, que saem do Estoque da unidade. Conferido contra o ddl-auto=validate (ADR-0090).

CREATE TABLE public.tb_plano_manutencao (
    ativo boolean NOT NULL,
    periodicidade_dias integer NOT NULL,
    proxima_em date NOT NULL,
    ultima_execucao date,
    registrado_em timestamp(6) with time zone NOT NULL,
    bem_id uuid NOT NULL,
    uuid uuid NOT NULL,
    descricao character varying(500),
    tipo character varying(255) NOT NULL,
    registrado_por_cpf character varying(255),
    CONSTRAINT tb_plano_manutencao_tipo_check CHECK (((tipo)::text = ANY ((ARRAY['CORRETIVA'::character varying, 'PREVENTIVA'::character varying, 'CALIBRACAO'::character varying, 'SEGURANCA_ELETRICA'::character varying])::text[])))
);
ALTER TABLE ONLY public.tb_plano_manutencao ADD CONSTRAINT tb_plano_manutencao_pkey PRIMARY KEY (uuid);
CREATE INDEX ix_plano_manutencao_bem ON public.tb_plano_manutencao USING btree (bem_id);
ALTER TABLE ONLY public.tb_plano_manutencao ADD CONSTRAINT fk_plano_manutencao_bem FOREIGN KEY (bem_id) REFERENCES public.tb_bem_patrimonial(uuid);

CREATE TABLE public.tb_ordem_servico_manutencao (
    bem_parado boolean NOT NULL,
    aberta_em date NOT NULL,
    calibracao_valida_ate date,
    concluida_em date,
    iniciada_em date,
    parado_desde date,
    custo_mao_de_obra numeric(15,2),
    registrado_em timestamp(6) with time zone NOT NULL,
    bem_id uuid NOT NULL,
    instrumento_id uuid,
    plano_id uuid,
    uuid uuid NOT NULL,
    numero character varying(20) NOT NULL,
    certificado character varying(60),
    tecnico character varying(150),
    motivo_cancelamento character varying(500),
    defeito character varying(1000),
    servico_realizado character varying(1000),
    aberta_por_cpf character varying(255),
    execucao character varying(255),
    prioridade character varying(255),
    situacao character varying(255) NOT NULL,
    situacao_bem_anterior character varying(255),
    tipo character varying(255) NOT NULL,
    CONSTRAINT tb_ordem_servico_manutencao_execucao_check CHECK (((execucao)::text = ANY ((ARRAY['INTERNA'::character varying, 'CONTRATO'::character varying])::text[]))),
    CONSTRAINT tb_ordem_servico_manutencao_prioridade_check CHECK (((prioridade)::text = ANY ((ARRAY['BAIXA'::character varying, 'NORMAL'::character varying, 'ALTA'::character varying, 'URGENTE'::character varying])::text[]))),
    CONSTRAINT tb_ordem_servico_manutencao_situacao_check CHECK (((situacao)::text = ANY ((ARRAY['ABERTA'::character varying, 'EM_EXECUCAO'::character varying, 'CONCLUIDA'::character varying, 'CANCELADA'::character varying])::text[]))),
    CONSTRAINT tb_ordem_servico_manutencao_situacao_bem_anterior_check CHECK (((situacao_bem_anterior)::text = ANY ((ARRAY['A_TOMBAR'::character varying, 'EM_USO'::character varying, 'OCIOSO'::character varying, 'EM_MANUTENCAO'::character varying, 'BAIXADO'::character varying])::text[]))),
    CONSTRAINT tb_ordem_servico_manutencao_tipo_check CHECK (((tipo)::text = ANY ((ARRAY['CORRETIVA'::character varying, 'PREVENTIVA'::character varying, 'CALIBRACAO'::character varying, 'SEGURANCA_ELETRICA'::character varying])::text[])))
);
ALTER TABLE ONLY public.tb_ordem_servico_manutencao ADD CONSTRAINT tb_ordem_servico_manutencao_pkey PRIMARY KEY (uuid);
ALTER TABLE ONLY public.tb_ordem_servico_manutencao ADD CONSTRAINT uk_ordem_servico_manutencao_numero UNIQUE (numero);
CREATE INDEX ix_ordem_servico_bem ON public.tb_ordem_servico_manutencao USING btree (bem_id, situacao);
CREATE INDEX ix_ordem_servico_plano ON public.tb_ordem_servico_manutencao USING btree (plano_id);
ALTER TABLE ONLY public.tb_ordem_servico_manutencao ADD CONSTRAINT fk_ordem_servico_bem FOREIGN KEY (bem_id) REFERENCES public.tb_bem_patrimonial(uuid);
ALTER TABLE ONLY public.tb_ordem_servico_manutencao ADD CONSTRAINT fk_ordem_servico_plano FOREIGN KEY (plano_id) REFERENCES public.tb_plano_manutencao(uuid);
ALTER TABLE ONLY public.tb_ordem_servico_manutencao ADD CONSTRAINT fk_ordem_servico_instrumento FOREIGN KEY (instrumento_id) REFERENCES public.tb_instrumento_contratual(uuid);

CREATE TABLE public.tb_peca_ordem_servico (
    quantidade integer NOT NULL,
    custo_unitario numeric(15,2),
    material_id uuid,
    ordem_id uuid NOT NULL,
    uuid uuid NOT NULL,
    descricao character varying(300) NOT NULL
);
ALTER TABLE ONLY public.tb_peca_ordem_servico ADD CONSTRAINT tb_peca_ordem_servico_pkey PRIMARY KEY (uuid);
CREATE INDEX ix_peca_ordem_servico ON public.tb_peca_ordem_servico USING btree (ordem_id);
ALTER TABLE ONLY public.tb_peca_ordem_servico ADD CONSTRAINT fk_peca_ordem_servico FOREIGN KEY (ordem_id) REFERENCES public.tb_ordem_servico_manutencao(uuid);
ALTER TABLE ONLY public.tb_peca_ordem_servico ADD CONSTRAINT fk_peca_ordem_servico_material FOREIGN KEY (material_id) REFERENCES public.tb_material_estoque(uuid);
