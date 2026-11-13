-- Estoque, fatia ES2 (ADR-0189): o almoxarifado central marcado pela capacidade CENTRAL_ABASTECIMENTO do modelo operacional, a
-- requisição das unidades, a transferência em duas etapas com divergência e o inventário com fechamento. Conferido contra o
-- ddl-auto=validate (ADR-0090). As permissões ESTOQUE.REQUISITAR e ESTOQUE.ATENDER vêm do CatalogoDeAcesso.

INSERT INTO public.tb_capacidade_assistencial (uuid, codigo, nome, descricao, ativo, perfil_operacional_id)
SELECT gen_random_uuid(), 'CENTRAL_ABASTECIMENTO', 'Central de abastecimento',
       'Almoxarifado central que atende as requisições de materiais das unidades da rede (ADR-0189).', true, NULL
WHERE NOT EXISTS (SELECT 1 FROM public.tb_capacidade_assistencial WHERE codigo = 'CENTRAL_ABASTECIMENTO');

ALTER TABLE public.tb_movimentacao_estoque ALTER COLUMN tipo TYPE character varying(30);
ALTER TABLE public.tb_movimentacao_estoque DROP CONSTRAINT tb_movimentacao_estoque_tipo_check;
ALTER TABLE public.tb_movimentacao_estoque ADD CONSTRAINT tb_movimentacao_estoque_tipo_check CHECK (tipo IN ('SALDO_INICIAL', 'ENTRADA',
    'SAIDA_CONSUMO', 'PERDA', 'AJUSTE_INVENTARIO', 'TRANSFERENCIA_SAIDA', 'TRANSFERENCIA_ENTRADA', 'TRANSFERENCIA_ESTORNO'));
ALTER TABLE public.tb_movimentacao_estoque ADD COLUMN transferencia_id uuid;

CREATE TABLE public.tb_requisicao_estoque (
    criada_em timestamp(6) with time zone NOT NULL,
    decidida_em timestamp(6) with time zone,
    enviada_em timestamp(6) with time zone,
    recebida_em timestamp(6) with time zone,
    atendente_id uuid,
    central_id uuid NOT NULL,
    solicitante_id uuid NOT NULL,
    transferencia_id uuid,
    unidade_id uuid NOT NULL,
    uuid uuid NOT NULL,
    prioridade character varying(20) NOT NULL,
    situacao character varying(20) NOT NULL,
    motivo character varying(1000),
    observacao character varying(1000),
    registrado_por_cpf character varying(255),
    CONSTRAINT tb_requisicao_estoque_prioridade_check CHECK (prioridade IN ('NORMAL', 'URGENTE')),
    CONSTRAINT tb_requisicao_estoque_situacao_check CHECK (situacao IN ('RASCUNHO', 'ENVIADA', 'ATENDIDA', 'RECEBIDA', 'NEGADA', 'CANCELADA'))
);
ALTER TABLE ONLY public.tb_requisicao_estoque ADD CONSTRAINT tb_requisicao_estoque_pkey PRIMARY KEY (uuid);
ALTER TABLE ONLY public.tb_requisicao_estoque ADD CONSTRAINT fk_requisicao_estoque_unidade FOREIGN KEY (unidade_id) REFERENCES public.tb_unidade_de_saude(uuid);
ALTER TABLE ONLY public.tb_requisicao_estoque ADD CONSTRAINT fk_requisicao_estoque_central FOREIGN KEY (central_id) REFERENCES public.tb_unidade_de_saude(uuid);
ALTER TABLE ONLY public.tb_requisicao_estoque ADD CONSTRAINT fk_requisicao_estoque_solicitante FOREIGN KEY (solicitante_id) REFERENCES public.tb_profissional(uuid);
ALTER TABLE ONLY public.tb_requisicao_estoque ADD CONSTRAINT fk_requisicao_estoque_atendente FOREIGN KEY (atendente_id) REFERENCES public.tb_profissional(uuid);
CREATE INDEX ix_requisicao_estoque_central ON public.tb_requisicao_estoque USING btree (central_id, situacao);
CREATE INDEX ix_requisicao_estoque_unidade ON public.tb_requisicao_estoque USING btree (unidade_id, criada_em);

CREATE TABLE public.tb_item_requisicao_estoque (
    quantidade_atendida integer,
    quantidade_pedida integer NOT NULL,
    material_id uuid NOT NULL,
    requisicao_id uuid NOT NULL,
    uuid uuid NOT NULL
);
ALTER TABLE ONLY public.tb_item_requisicao_estoque ADD CONSTRAINT tb_item_requisicao_estoque_pkey PRIMARY KEY (uuid);
ALTER TABLE ONLY public.tb_item_requisicao_estoque ADD CONSTRAINT fk_item_requisicao_estoque_requisicao FOREIGN KEY (requisicao_id) REFERENCES public.tb_requisicao_estoque(uuid);
ALTER TABLE ONLY public.tb_item_requisicao_estoque ADD CONSTRAINT fk_item_requisicao_estoque_material FOREIGN KEY (material_id) REFERENCES public.tb_material_estoque(uuid);
CREATE INDEX ix_item_requisicao_estoque ON public.tb_item_requisicao_estoque USING btree (requisicao_id);

CREATE TABLE public.tb_transferencia_estoque (
    enviada_em timestamp(6) with time zone NOT NULL,
    recebida_em timestamp(6) with time zone,
    destino_id uuid NOT NULL,
    enviado_por_id uuid NOT NULL,
    origem_id uuid NOT NULL,
    recebido_por_id uuid,
    requisicao_id uuid,
    uuid uuid NOT NULL,
    situacao character varying(20) NOT NULL,
    observacao character varying(1000),
    registrado_por_cpf character varying(255),
    CONSTRAINT tb_transferencia_estoque_situacao_check CHECK (situacao IN ('EM_TRANSITO', 'RECEBIDA'))
);
ALTER TABLE ONLY public.tb_transferencia_estoque ADD CONSTRAINT tb_transferencia_estoque_pkey PRIMARY KEY (uuid);
ALTER TABLE ONLY public.tb_transferencia_estoque ADD CONSTRAINT fk_transferencia_estoque_origem FOREIGN KEY (origem_id) REFERENCES public.tb_unidade_de_saude(uuid);
ALTER TABLE ONLY public.tb_transferencia_estoque ADD CONSTRAINT fk_transferencia_estoque_destino FOREIGN KEY (destino_id) REFERENCES public.tb_unidade_de_saude(uuid);
ALTER TABLE ONLY public.tb_transferencia_estoque ADD CONSTRAINT fk_transferencia_estoque_enviado_por FOREIGN KEY (enviado_por_id) REFERENCES public.tb_profissional(uuid);
ALTER TABLE ONLY public.tb_transferencia_estoque ADD CONSTRAINT fk_transferencia_estoque_recebido_por FOREIGN KEY (recebido_por_id) REFERENCES public.tb_profissional(uuid);
CREATE INDEX ix_transferencia_estoque_destino ON public.tb_transferencia_estoque USING btree (destino_id, situacao);
CREATE INDEX ix_transferencia_estoque_origem ON public.tb_transferencia_estoque USING btree (origem_id, enviada_em);

CREATE TABLE public.tb_item_transferencia_estoque (
    custo_unitario numeric(12,4),
    quantidade_enviada integer NOT NULL,
    quantidade_recebida integer,
    validade date,
    lote_destino_id uuid,
    lote_origem_id uuid NOT NULL,
    material_id uuid NOT NULL,
    transferencia_id uuid NOT NULL,
    uuid uuid NOT NULL,
    numero_lote character varying(60),
    motivo_divergencia character varying(20),
    observacao character varying(500),
    CONSTRAINT tb_item_transferencia_estoque_motivo_check CHECK (motivo_divergencia IN ('FALTA', 'AVARIA', 'OUTRO'))
);
ALTER TABLE ONLY public.tb_item_transferencia_estoque ADD CONSTRAINT tb_item_transferencia_estoque_pkey PRIMARY KEY (uuid);
ALTER TABLE ONLY public.tb_item_transferencia_estoque ADD CONSTRAINT fk_item_transferencia_estoque FOREIGN KEY (transferencia_id) REFERENCES public.tb_transferencia_estoque(uuid);
ALTER TABLE ONLY public.tb_item_transferencia_estoque ADD CONSTRAINT fk_item_transferencia_estoque_material FOREIGN KEY (material_id) REFERENCES public.tb_material_estoque(uuid);
ALTER TABLE ONLY public.tb_item_transferencia_estoque ADD CONSTRAINT fk_item_transferencia_estoque_lote_origem FOREIGN KEY (lote_origem_id) REFERENCES public.tb_lote_material(uuid);
ALTER TABLE ONLY public.tb_item_transferencia_estoque ADD CONSTRAINT fk_item_transferencia_estoque_lote_destino FOREIGN KEY (lote_destino_id) REFERENCES public.tb_lote_material(uuid);
CREATE INDEX ix_item_transferencia_estoque ON public.tb_item_transferencia_estoque USING btree (transferencia_id);

CREATE TABLE public.tb_inventario_estoque (
    aberto_em timestamp(6) with time zone NOT NULL,
    encerrado_em timestamp(6) with time zone,
    aberto_por_id uuid NOT NULL,
    encerrado_por_id uuid,
    unidade_id uuid NOT NULL,
    uuid uuid NOT NULL,
    situacao character varying(20) NOT NULL,
    observacao character varying(1000),
    registrado_por_cpf character varying(255),
    CONSTRAINT tb_inventario_estoque_situacao_check CHECK (situacao IN ('ABERTO', 'FECHADO', 'CANCELADO'))
);
ALTER TABLE ONLY public.tb_inventario_estoque ADD CONSTRAINT tb_inventario_estoque_pkey PRIMARY KEY (uuid);
ALTER TABLE ONLY public.tb_inventario_estoque ADD CONSTRAINT fk_inventario_estoque_unidade FOREIGN KEY (unidade_id) REFERENCES public.tb_unidade_de_saude(uuid);
ALTER TABLE ONLY public.tb_inventario_estoque ADD CONSTRAINT fk_inventario_estoque_aberto_por FOREIGN KEY (aberto_por_id) REFERENCES public.tb_profissional(uuid);
ALTER TABLE ONLY public.tb_inventario_estoque ADD CONSTRAINT fk_inventario_estoque_encerrado_por FOREIGN KEY (encerrado_por_id) REFERENCES public.tb_profissional(uuid);
CREATE UNIQUE INDEX uk_inventario_estoque_aberto ON public.tb_inventario_estoque USING btree (unidade_id) WHERE situacao = 'ABERTO';

CREATE TABLE public.tb_item_inventario_estoque (
    quantidade_contada integer,
    saldo_sistema integer NOT NULL,
    contado_em timestamp(6) with time zone,
    contado_por_id uuid,
    inventario_id uuid NOT NULL,
    lote_id uuid NOT NULL,
    uuid uuid NOT NULL
);
ALTER TABLE ONLY public.tb_item_inventario_estoque ADD CONSTRAINT tb_item_inventario_estoque_pkey PRIMARY KEY (uuid);
ALTER TABLE ONLY public.tb_item_inventario_estoque ADD CONSTRAINT fk_item_inventario_estoque FOREIGN KEY (inventario_id) REFERENCES public.tb_inventario_estoque(uuid);
ALTER TABLE ONLY public.tb_item_inventario_estoque ADD CONSTRAINT fk_item_inventario_estoque_lote FOREIGN KEY (lote_id) REFERENCES public.tb_lote_material(uuid);
ALTER TABLE ONLY public.tb_item_inventario_estoque ADD CONSTRAINT fk_item_inventario_estoque_contado_por FOREIGN KEY (contado_por_id) REFERENCES public.tb_profissional(uuid);
CREATE INDEX ix_item_inventario_estoque ON public.tb_item_inventario_estoque USING btree (inventario_id);
