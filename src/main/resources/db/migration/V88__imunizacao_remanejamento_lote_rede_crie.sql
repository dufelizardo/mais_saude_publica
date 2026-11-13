-- Itens internos da Imunização, I6 (ADR-0206): o remanejamento de lote entre salas de vacina, a suspensão do lote na rede pelo
-- alerta de ESAVI e a revogação da contraindicação pelo CRIE. Conferido contra o ddl-auto=validate (ADR-0090).

-- A suspensão do lote na rede (imunobiológico + número do lote), decidida pela coordenação da rede.
CREATE TABLE public.tb_suspensao_lote_rede (
    uuid uuid NOT NULL,
    imunobiologico_id uuid NOT NULL,
    numero_lote character varying(40) NOT NULL,
    sinal character varying(1000) NOT NULL,
    motivo character varying(1000) NOT NULL,
    notificacoes character varying(2000),
    situacao character varying(255) NOT NULL,
    suspensa_em timestamp(6) with time zone NOT NULL,
    suspensa_por_cpf character varying(255),
    decidida_em timestamp(6) with time zone,
    decidida_por_cpf character varying(255),
    parecer character varying(1000),
    CONSTRAINT tb_suspensao_lote_rede_situacao_check CHECK (((situacao)::text = ANY ((ARRAY['VIGENTE'::character varying, 'LIBERADA'::character varying, 'INUTILIZADA'::character varying])::text[])))
);
ALTER TABLE ONLY public.tb_suspensao_lote_rede ADD CONSTRAINT tb_suspensao_lote_rede_pkey PRIMARY KEY (uuid);
ALTER TABLE ONLY public.tb_suspensao_lote_rede ADD CONSTRAINT fk_suspensao_lote_imunobiologico FOREIGN KEY (imunobiologico_id) REFERENCES public.tb_imunobiologico(uuid);
CREATE UNIQUE INDEX uk_suspensao_lote_vigente ON public.tb_suspensao_lote_rede USING btree (imunobiologico_id, numero_lote) WHERE ((situacao)::text = 'VIGENTE'::text);

-- O lote sob suspeita guarda o motivo; o suspenso pela rede aponta a suspensão.
ALTER TABLE public.tb_lote_imunobiologico ADD COLUMN motivo_suspeita character varying(255);
UPDATE public.tb_lote_imunobiologico SET motivo_suspeita = 'EXCURSAO_TEMPERATURA' WHERE situacao = 'SOB_SUSPEITA';
ALTER TABLE public.tb_lote_imunobiologico ADD CONSTRAINT tb_lote_imunobiologico_motivo_suspeita_check CHECK (((motivo_suspeita)::text = ANY ((ARRAY['EXCURSAO_TEMPERATURA'::character varying, 'ESAVI_LOTE'::character varying])::text[])));
ALTER TABLE public.tb_lote_imunobiologico ADD COLUMN suspensao_rede_id uuid;
ALTER TABLE ONLY public.tb_lote_imunobiologico ADD CONSTRAINT fk_lote_imuno_suspensao FOREIGN KEY (suspensao_rede_id) REFERENCES public.tb_suspensao_lote_rede(uuid);

-- O remanejamento de doses de um lote para a sala de vacina de outra unidade, em duas etapas.
CREATE TABLE public.tb_remanejamento_imunobiologico (
    uuid uuid NOT NULL,
    lote_origem_id uuid NOT NULL,
    unidade_destino_id uuid NOT NULL,
    lote_destino_id uuid,
    camara_destino_id uuid,
    doses integer NOT NULL,
    motivo character varying(255) NOT NULL,
    observacao character varying(500),
    situacao character varying(255) NOT NULL,
    enviado_em timestamp(6) with time zone NOT NULL,
    enviado_por_cpf character varying(255),
    concluido_em timestamp(6) with time zone,
    concluido_por_cpf character varying(255),
    temperatura_chegada numeric(4,1),
    motivo_recusa character varying(500),
    CONSTRAINT tb_remanejamento_imunobiologico_motivo_check CHECK (((motivo)::text = ANY ((ARRAY['EXCESSO'::character varying, 'VALIDADE_PROXIMA'::character varying, 'DESABASTECIMENTO'::character varying, 'OUTRO'::character varying])::text[]))),
    CONSTRAINT tb_remanejamento_imunobiologico_situacao_check CHECK (((situacao)::text = ANY ((ARRAY['ENVIADO'::character varying, 'RECEBIDO'::character varying, 'RECUSADO'::character varying, 'CANCELADO'::character varying])::text[])))
);
ALTER TABLE ONLY public.tb_remanejamento_imunobiologico ADD CONSTRAINT tb_remanejamento_imunobiologico_pkey PRIMARY KEY (uuid);
ALTER TABLE ONLY public.tb_remanejamento_imunobiologico ADD CONSTRAINT fk_remanejamento_lote_origem FOREIGN KEY (lote_origem_id) REFERENCES public.tb_lote_imunobiologico(uuid);
ALTER TABLE ONLY public.tb_remanejamento_imunobiologico ADD CONSTRAINT fk_remanejamento_lote_destino FOREIGN KEY (lote_destino_id) REFERENCES public.tb_lote_imunobiologico(uuid);
ALTER TABLE ONLY public.tb_remanejamento_imunobiologico ADD CONSTRAINT fk_remanejamento_unidade_destino FOREIGN KEY (unidade_destino_id) REFERENCES public.tb_unidade_de_saude(uuid);
ALTER TABLE ONLY public.tb_remanejamento_imunobiologico ADD CONSTRAINT fk_remanejamento_camara_destino FOREIGN KEY (camara_destino_id) REFERENCES public.tb_camara_refrigeracao(uuid);
CREATE INDEX ix_remanejamento_destino ON public.tb_remanejamento_imunobiologico USING btree (unidade_destino_id, situacao);

-- O livro: as saídas e entradas do remanejamento e a perda pelo lote inutilizado na rede.
ALTER TABLE public.tb_movimentacao_imunobiologico DROP CONSTRAINT tb_movimentacao_imunobiologico_tipo_check;
ALTER TABLE public.tb_movimentacao_imunobiologico ADD CONSTRAINT tb_movimentacao_imunobiologico_tipo_check CHECK (((tipo)::text = ANY ((ARRAY['ENTRADA'::character varying, 'APLICACAO'::character varying, 'ESTORNO_APLICACAO'::character varying, 'PERDA_FISICA'::character varying, 'PERDA_TECNICA'::character varying, 'AJUSTE_INVENTARIO'::character varying, 'REMANEJAMENTO_SAIDA'::character varying, 'REMANEJAMENTO_ENTRADA'::character varying, 'ESTORNO_REMANEJAMENTO'::character varying])::text[])));
ALTER TABLE public.tb_movimentacao_imunobiologico DROP CONSTRAINT tb_movimentacao_imunobiologico_motivo_perda_check;
ALTER TABLE public.tb_movimentacao_imunobiologico ADD CONSTRAINT tb_movimentacao_imunobiologico_motivo_perda_check CHECK (((motivo_perda)::text = ANY ((ARRAY['VENCIMENTO'::character varying, 'QUEBRA'::character varying, 'FALHA_EQUIPAMENTO'::character varying, 'FALTA_ENERGIA'::character varying, 'TRANSPORTE'::character varying, 'EXCURSAO_TEMPERATURA'::character varying, 'FRASCO_ABERTO_VENCIDO'::character varying, 'ESAVI_LOTE'::character varying, 'OUTRO'::character varying])::text[])));
ALTER TABLE public.tb_movimentacao_imunobiologico ADD COLUMN remanejamento_id uuid;
ALTER TABLE ONLY public.tb_movimentacao_imunobiologico ADD CONSTRAINT fk_movimentacao_remanejamento FOREIGN KEY (remanejamento_id) REFERENCES public.tb_remanejamento_imunobiologico(uuid);

-- A contraindicação revogada pelo CRIE fica no histórico; só uma vigente por paciente e imunobiológico.
ALTER TABLE public.tb_contraindicacao_vacinal DROP CONSTRAINT uk_contraindicacao_paciente_imunobiologico;
ALTER TABLE public.tb_contraindicacao_vacinal ADD COLUMN revogada_em timestamp(6) with time zone;
ALTER TABLE public.tb_contraindicacao_vacinal ADD COLUMN revogada_por_cpf character varying(255);
ALTER TABLE public.tb_contraindicacao_vacinal ADD COLUMN parecer_revogacao character varying(1000);
CREATE UNIQUE INDEX uk_contraindicacao_vigente ON public.tb_contraindicacao_vacinal USING btree (paciente_id, imunobiologico_id) WHERE (revogada_em IS NULL);
