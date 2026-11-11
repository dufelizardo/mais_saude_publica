-- Estoque (ES3, ADR-0191) e Transporte (T3, ADR-0192) nas Emergências: o insumo mobilizado atendido pelo estoque (a requisição
-- urgente da emergência ou a transferência ligada), a reserva para a emergência, o consumo extraordinário e o veículo cedido pela
-- frota. Conferido contra o ddl-auto=validate (ADR-0090).

-- ES3: a mobilização de insumo aponta o material e, quando atendida pelo estoque, a requisição ou a transferência.
ALTER TABLE public.tb_mobilizacao ADD COLUMN material_id uuid;
ALTER TABLE public.tb_mobilizacao ADD COLUMN requisicao_estoque_id uuid;
ALTER TABLE public.tb_mobilizacao ADD COLUMN transferencia_estoque_id uuid;
ALTER TABLE ONLY public.tb_mobilizacao ADD CONSTRAINT fk_mobilizacao_material FOREIGN KEY (material_id) REFERENCES public.tb_material_estoque(uuid);
ALTER TABLE ONLY public.tb_mobilizacao ADD CONSTRAINT fk_mobilizacao_requisicao_estoque FOREIGN KEY (requisicao_estoque_id) REFERENCES public.tb_requisicao_estoque(uuid);
ALTER TABLE ONLY public.tb_mobilizacao ADD CONSTRAINT fk_mobilizacao_transferencia_estoque FOREIGN KEY (transferencia_estoque_id) REFERENCES public.tb_transferencia_estoque(uuid);

-- ES3: a requisição urgente gerada pela emergência (o solicitante pode ser a coordenação, sem profissional no login).
ALTER TABLE public.tb_requisicao_estoque ADD COLUMN emergencia_id uuid;
ALTER TABLE public.tb_requisicao_estoque ALTER COLUMN solicitante_id DROP NOT NULL;
ALTER TABLE ONLY public.tb_requisicao_estoque ADD CONSTRAINT fk_requisicao_estoque_emergencia FOREIGN KEY (emergencia_id) REFERENCES public.tb_emergencia(uuid);
CREATE INDEX ix_requisicao_estoque_emergencia ON public.tb_requisicao_estoque USING btree (emergencia_id);

-- ES3: o consumo extraordinário fica marcado com a emergência.
ALTER TABLE public.tb_movimentacao_estoque ADD COLUMN emergencia_id uuid;
ALTER TABLE ONLY public.tb_movimentacao_estoque ADD CONSTRAINT fk_movimentacao_estoque_emergencia FOREIGN KEY (emergencia_id) REFERENCES public.tb_emergencia(uuid);
CREATE INDEX ix_movimentacao_estoque_emergencia ON public.tb_movimentacao_estoque USING btree (emergencia_id);

-- ES3: a reserva de material para a emergência, na unidade.
CREATE TABLE public.tb_reserva_estoque (
    consumida integer NOT NULL,
    quantidade integer NOT NULL,
    criada_em timestamp(6) with time zone NOT NULL,
    liberada_em timestamp(6) with time zone,
    uuid uuid NOT NULL,
    emergencia_id uuid NOT NULL,
    material_id uuid NOT NULL,
    unidade_id uuid NOT NULL,
    situacao character varying(20) NOT NULL,
    justificativa character varying(500) NOT NULL,
    motivo_liberacao character varying(500),
    registrado_por_cpf character varying(255),
    liberado_por_cpf character varying(255),
    CONSTRAINT tb_reserva_estoque_situacao_check CHECK (situacao IN ('ATIVA', 'LIBERADA', 'CONSUMIDA'))
);
ALTER TABLE ONLY public.tb_reserva_estoque ADD CONSTRAINT tb_reserva_estoque_pkey PRIMARY KEY (uuid);
ALTER TABLE ONLY public.tb_reserva_estoque ADD CONSTRAINT fk_reserva_estoque_emergencia FOREIGN KEY (emergencia_id) REFERENCES public.tb_emergencia(uuid);
ALTER TABLE ONLY public.tb_reserva_estoque ADD CONSTRAINT fk_reserva_estoque_material FOREIGN KEY (material_id) REFERENCES public.tb_material_estoque(uuid);
ALTER TABLE ONLY public.tb_reserva_estoque ADD CONSTRAINT fk_reserva_estoque_unidade FOREIGN KEY (unidade_id) REFERENCES public.tb_unidade_de_saude(uuid);
CREATE INDEX ix_reserva_estoque_unidade ON public.tb_reserva_estoque USING btree (unidade_id, material_id, situacao);
CREATE INDEX ix_reserva_estoque_emergencia ON public.tb_reserva_estoque USING btree (emergencia_id);

-- T3: o veículo cedido pela frota a uma mobilização de veículo.
CREATE TABLE public.tb_cessao_veiculo (
    km_fim integer,
    km_inicio integer,
    cedida_em timestamp(6) with time zone NOT NULL,
    devolvida_em timestamp(6) with time zone,
    uuid uuid NOT NULL,
    mobilizacao_id uuid NOT NULL,
    veiculo_id uuid NOT NULL,
    observacao character varying(500),
    cedida_por_cpf character varying(255),
    devolvida_por_cpf character varying(255)
);
ALTER TABLE ONLY public.tb_cessao_veiculo ADD CONSTRAINT tb_cessao_veiculo_pkey PRIMARY KEY (uuid);
ALTER TABLE ONLY public.tb_cessao_veiculo ADD CONSTRAINT fk_cessao_veiculo_mobilizacao FOREIGN KEY (mobilizacao_id) REFERENCES public.tb_mobilizacao(uuid);
ALTER TABLE ONLY public.tb_cessao_veiculo ADD CONSTRAINT fk_cessao_veiculo_veiculo FOREIGN KEY (veiculo_id) REFERENCES public.tb_veiculo(uuid);
CREATE INDEX ix_cessao_veiculo_mobilizacao ON public.tb_cessao_veiculo USING btree (mobilizacao_id);
CREATE INDEX ix_cessao_veiculo_veiculo ON public.tb_cessao_veiculo USING btree (veiculo_id, devolvida_em);
