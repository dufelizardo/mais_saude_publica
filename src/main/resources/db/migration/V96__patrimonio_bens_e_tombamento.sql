-- Patrimônio e Manutenção, fatia PM1 (ADR-0215): o bem patrimonial com o tombamento, o termo de responsabilidade com histórico, a
-- transferência entre unidades em duas etapas e o bem permanente comprado, que nasce "a tombar" no recebimento definitivo da CP3.
-- Conferido contra o ddl-auto=validate (ADR-0090).

CREATE TABLE public.tb_bem_patrimonial (
    data_aquisicao date,
    tombado_em date,
    valor_aquisicao numeric(15,2),
    registrado_em timestamp(6) with time zone NOT NULL,
    fornecedor_id uuid,
    item_recebimento_id uuid,
    setor_id uuid,
    unidade_id uuid NOT NULL,
    uuid uuid NOT NULL,
    veiculo_id uuid,
    numero_tombamento character varying(20),
    marca character varying(80),
    modelo character varying(80),
    numero_serie character varying(80),
    documento character varying(60),
    descricao character varying(500) NOT NULL,
    observacao character varying(500),
    classe character varying(255),
    origem character varying(255) NOT NULL,
    situacao character varying(255) NOT NULL,
    registrado_por_cpf character varying(255),
    CONSTRAINT tb_bem_patrimonial_classe_check CHECK (((classe)::text = ANY ((ARRAY['EQUIPAMENTO_MEDICO_HOSPITALAR'::character varying, 'INFORMATICA'::character varying, 'MOBILIARIO'::character varying, 'VEICULO'::character varying, 'ELETRODOMESTICO'::character varying, 'OUTRO'::character varying])::text[]))),
    CONSTRAINT tb_bem_patrimonial_origem_check CHECK (((origem)::text = ANY ((ARRAY['COMPRA'::character varying, 'DOACAO'::character varying, 'CESSAO_COMODATO'::character varying, 'TRANSFERENCIA_ENTE'::character varying])::text[]))),
    CONSTRAINT tb_bem_patrimonial_situacao_check CHECK (((situacao)::text = ANY ((ARRAY['A_TOMBAR'::character varying, 'EM_USO'::character varying, 'OCIOSO'::character varying, 'EM_MANUTENCAO'::character varying, 'BAIXADO'::character varying])::text[])))
);
ALTER TABLE ONLY public.tb_bem_patrimonial ADD CONSTRAINT tb_bem_patrimonial_pkey PRIMARY KEY (uuid);
ALTER TABLE ONLY public.tb_bem_patrimonial ADD CONSTRAINT uk_bem_patrimonial_tombamento UNIQUE (numero_tombamento);
ALTER TABLE ONLY public.tb_bem_patrimonial ADD CONSTRAINT uk_bem_patrimonial_veiculo UNIQUE (veiculo_id);
CREATE INDEX ix_bem_patrimonial_unidade ON public.tb_bem_patrimonial USING btree (unidade_id, situacao);
ALTER TABLE ONLY public.tb_bem_patrimonial ADD CONSTRAINT fk_bem_patrimonial_unidade FOREIGN KEY (unidade_id) REFERENCES public.tb_unidade_de_saude(uuid);
ALTER TABLE ONLY public.tb_bem_patrimonial ADD CONSTRAINT fk_bem_patrimonial_setor FOREIGN KEY (setor_id) REFERENCES public.tb_setor(uuid);
ALTER TABLE ONLY public.tb_bem_patrimonial ADD CONSTRAINT fk_bem_patrimonial_fornecedor FOREIGN KEY (fornecedor_id) REFERENCES public.tb_fornecedor(uuid);
ALTER TABLE ONLY public.tb_bem_patrimonial ADD CONSTRAINT fk_bem_patrimonial_item_recebimento FOREIGN KEY (item_recebimento_id) REFERENCES public.tb_item_recebimento_compra(uuid);
ALTER TABLE ONLY public.tb_bem_patrimonial ADD CONSTRAINT fk_bem_patrimonial_veiculo FOREIGN KEY (veiculo_id) REFERENCES public.tb_veiculo(uuid);

CREATE TABLE public.tb_responsavel_bem (
    ate date,
    desde date NOT NULL,
    registrado_em timestamp(6) with time zone NOT NULL,
    bem_id uuid NOT NULL,
    profissional_id uuid NOT NULL,
    uuid uuid NOT NULL,
    registrado_por_cpf character varying(255)
);
ALTER TABLE ONLY public.tb_responsavel_bem ADD CONSTRAINT tb_responsavel_bem_pkey PRIMARY KEY (uuid);
CREATE INDEX ix_responsavel_bem ON public.tb_responsavel_bem USING btree (bem_id);
CREATE INDEX ix_responsavel_bem_profissional ON public.tb_responsavel_bem USING btree (profissional_id);
ALTER TABLE ONLY public.tb_responsavel_bem ADD CONSTRAINT fk_responsavel_bem FOREIGN KEY (bem_id) REFERENCES public.tb_bem_patrimonial(uuid);
ALTER TABLE ONLY public.tb_responsavel_bem ADD CONSTRAINT fk_responsavel_bem_profissional FOREIGN KEY (profissional_id) REFERENCES public.tb_profissional(uuid);

CREATE TABLE public.tb_transferencia_bem (
    enviada_em date NOT NULL,
    resolvida_em date,
    registrado_em timestamp(6) with time zone NOT NULL,
    bem_id uuid NOT NULL,
    destino_id uuid NOT NULL,
    origem_id uuid NOT NULL,
    uuid uuid NOT NULL,
    motivo character varying(500) NOT NULL,
    motivo_recusa character varying(500),
    situacao character varying(255) NOT NULL,
    enviada_por_cpf character varying(255),
    resolvida_por_cpf character varying(255),
    CONSTRAINT tb_transferencia_bem_situacao_check CHECK (((situacao)::text = ANY ((ARRAY['PENDENTE'::character varying, 'ACEITA'::character varying, 'RECUSADA'::character varying, 'CANCELADA'::character varying])::text[])))
);
ALTER TABLE ONLY public.tb_transferencia_bem ADD CONSTRAINT tb_transferencia_bem_pkey PRIMARY KEY (uuid);
CREATE INDEX ix_transferencia_bem ON public.tb_transferencia_bem USING btree (bem_id);
CREATE INDEX ix_transferencia_bem_situacao ON public.tb_transferencia_bem USING btree (situacao);
ALTER TABLE ONLY public.tb_transferencia_bem ADD CONSTRAINT fk_transferencia_bem FOREIGN KEY (bem_id) REFERENCES public.tb_bem_patrimonial(uuid);
ALTER TABLE ONLY public.tb_transferencia_bem ADD CONSTRAINT fk_transferencia_bem_origem FOREIGN KEY (origem_id) REFERENCES public.tb_unidade_de_saude(uuid);
ALTER TABLE ONLY public.tb_transferencia_bem ADD CONSTRAINT fk_transferencia_bem_destino FOREIGN KEY (destino_id) REFERENCES public.tb_unidade_de_saude(uuid);

-- O bem permanente passa a ser comprado (ADR-0215): o tipo novo nos itens da solicitação e da contratação.
ALTER TABLE public.tb_item_solicitacao_compra DROP CONSTRAINT tb_item_solicitacao_compra_tipo_check;
ALTER TABLE public.tb_item_solicitacao_compra ADD CONSTRAINT tb_item_solicitacao_compra_tipo_check CHECK (((tipo)::text = ANY ((ARRAY['MATERIAL'::character varying, 'MEDICAMENTO'::character varying, 'SERVICO'::character varying, 'BEM_PERMANENTE'::character varying])::text[])));
ALTER TABLE public.tb_item_contratacao DROP CONSTRAINT tb_item_contratacao_tipo_check;
ALTER TABLE public.tb_item_contratacao ADD CONSTRAINT tb_item_contratacao_tipo_check CHECK (((tipo)::text = ANY ((ARRAY['MATERIAL'::character varying, 'MEDICAMENTO'::character varying, 'SERVICO'::character varying, 'BEM_PERMANENTE'::character varying])::text[])));
