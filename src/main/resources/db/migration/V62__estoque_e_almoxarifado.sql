-- Estoque e Almoxarifado, fatia ES1 (ADR-0177): catálogo de materiais, lotes por unidade, livro de movimentação e mínimo.
-- Conferido contra o ddl-auto=validate (ADR-0090).

CREATE TABLE public.tb_material_estoque (
    ativo boolean NOT NULL,
    controla_lote boolean NOT NULL,
    uuid uuid NOT NULL,
    catmat character varying(20),
    unidade_medida character varying(20) NOT NULL,
    categoria character varying(30) NOT NULL,
    codigo character varying(30) NOT NULL,
    nome character varying(150) NOT NULL,
    descricao character varying(500),
    CONSTRAINT tb_material_estoque_categoria_check CHECK (categoria IN ('MEDICO_HOSPITALAR', 'EPI', 'ODONTOLOGICO', 'LABORATORIO', 'LIMPEZA_HIGIENE',
        'EXPEDIENTE', 'ROUPARIA', 'OUTRO')),
    CONSTRAINT tb_material_estoque_unidade_medida_check CHECK (unidade_medida IN ('UNIDADE', 'CAIXA', 'PACOTE', 'PAR', 'ROLO', 'LITRO', 'FRASCO'))
);
ALTER TABLE ONLY public.tb_material_estoque ADD CONSTRAINT tb_material_estoque_pkey PRIMARY KEY (uuid);
ALTER TABLE ONLY public.tb_material_estoque ADD CONSTRAINT uk_material_estoque_codigo UNIQUE (codigo);
ALTER TABLE ONLY public.tb_material_estoque ADD CONSTRAINT uk_material_estoque_nome UNIQUE (nome);

CREATE TABLE public.tb_lote_material (
    quantidade integer NOT NULL,
    validade date,
    custo_unitario numeric(12,4),
    criado_em timestamp(6) with time zone NOT NULL,
    material_id uuid NOT NULL,
    unidade_id uuid NOT NULL,
    uuid uuid NOT NULL,
    numero_lote character varying(60)
);
ALTER TABLE ONLY public.tb_lote_material ADD CONSTRAINT tb_lote_material_pkey PRIMARY KEY (uuid);
CREATE INDEX ix_lote_material_unidade ON public.tb_lote_material USING btree (unidade_id, material_id);
ALTER TABLE ONLY public.tb_lote_material ADD CONSTRAINT fk_lote_material_material FOREIGN KEY (material_id) REFERENCES public.tb_material_estoque(uuid);
ALTER TABLE ONLY public.tb_lote_material ADD CONSTRAINT fk_lote_material_unidade FOREIGN KEY (unidade_id) REFERENCES public.tb_unidade_de_saude(uuid);

CREATE TABLE public.tb_movimentacao_estoque (
    quantidade integer NOT NULL,
    saldo_apos integer NOT NULL,
    registrado_em timestamp(6) with time zone NOT NULL,
    lote_id uuid NOT NULL,
    profissional_id uuid,
    setor_id uuid,
    uuid uuid NOT NULL,
    tipo character varying(20) NOT NULL,
    origem character varying(20),
    motivo_perda character varying(20),
    documento character varying(60),
    fornecedor character varying(150),
    justificativa character varying(500),
    registrado_por_cpf character varying(255),
    CONSTRAINT tb_movimentacao_estoque_tipo_check CHECK (tipo IN ('SALDO_INICIAL', 'ENTRADA', 'SAIDA_CONSUMO', 'PERDA', 'AJUSTE_INVENTARIO')),
    CONSTRAINT tb_movimentacao_estoque_origem_check CHECK (origem IN ('NOTA_FISCAL', 'DOACAO', 'SALDO_INICIAL', 'OUTRA')),
    CONSTRAINT tb_movimentacao_estoque_motivo_perda_check CHECK (motivo_perda IN ('VENCIMENTO', 'AVARIA', 'EXTRAVIO', 'OUTRO'))
);
ALTER TABLE ONLY public.tb_movimentacao_estoque ADD CONSTRAINT tb_movimentacao_estoque_pkey PRIMARY KEY (uuid);
CREATE INDEX ix_movimentacao_estoque_lote ON public.tb_movimentacao_estoque USING btree (lote_id, registrado_em);
ALTER TABLE ONLY public.tb_movimentacao_estoque ADD CONSTRAINT fk_movimentacao_estoque_lote FOREIGN KEY (lote_id) REFERENCES public.tb_lote_material(uuid);
ALTER TABLE ONLY public.tb_movimentacao_estoque ADD CONSTRAINT fk_movimentacao_estoque_setor FOREIGN KEY (setor_id) REFERENCES public.tb_setor(uuid);
ALTER TABLE ONLY public.tb_movimentacao_estoque ADD CONSTRAINT fk_movimentacao_estoque_profissional FOREIGN KEY (profissional_id) REFERENCES public.tb_profissional(uuid);

CREATE TABLE public.tb_estoque_minimo (
    quantidade_minima integer NOT NULL,
    atualizado_em timestamp(6) with time zone NOT NULL,
    material_id uuid NOT NULL,
    unidade_id uuid NOT NULL,
    uuid uuid NOT NULL,
    atualizado_por_cpf character varying(255)
);
ALTER TABLE ONLY public.tb_estoque_minimo ADD CONSTRAINT tb_estoque_minimo_pkey PRIMARY KEY (uuid);
ALTER TABLE ONLY public.tb_estoque_minimo ADD CONSTRAINT uk_estoque_minimo UNIQUE (material_id, unidade_id);
ALTER TABLE ONLY public.tb_estoque_minimo ADD CONSTRAINT fk_estoque_minimo_material FOREIGN KEY (material_id) REFERENCES public.tb_material_estoque(uuid);
ALTER TABLE ONLY public.tb_estoque_minimo ADD CONSTRAINT fk_estoque_minimo_unidade FOREIGN KEY (unidade_id) REFERENCES public.tb_unidade_de_saude(uuid);
