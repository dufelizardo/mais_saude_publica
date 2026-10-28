-- Imunização, fatia I2 (ADR-0148): a sala de vacina como capacidade da unidade, a câmara de refrigeração com as leituras
-- de temperatura, o lote de imunobiológico por unidade com o livro de movimentação (em doses), o frasco aberto
-- (multidose) e o lote do estoque ligado à dose. Conferido contra o ddl-auto=validate (ADR-0090).

-- A sala de vacina é capacidade da unidade (MO1, ADR-0117): com ela, a dose aplicada sai do estoque. Transversal (sem
-- perfil): além da UBS, vacinam a maternidade (BCG e hepatite B ao nascer), o CRIE e o ponto de vacinação avulso.
INSERT INTO public.tb_capacidade_assistencial (uuid, codigo, nome, descricao, ativo, perfil_operacional_id)
SELECT gen_random_uuid(), 'SALA_VACINA', 'Sala de vacina', 'Aplicação de imunobiológicos com estoque e cadeia de frio próprios.', true, NULL
WHERE NOT EXISTS (SELECT 1 FROM public.tb_capacidade_assistencial c WHERE c.codigo = 'SALA_VACINA');

CREATE TABLE public.tb_camara_refrigeracao (
    ativa boolean NOT NULL,
    temperatura_maxima numeric(4,1) NOT NULL,
    temperatura_minima numeric(4,1) NOT NULL,
    unidade_id uuid NOT NULL,
    uuid uuid NOT NULL,
    nome character varying(80) NOT NULL
);

ALTER TABLE ONLY public.tb_camara_refrigeracao ADD CONSTRAINT tb_camara_refrigeracao_pkey PRIMARY KEY (uuid);
ALTER TABLE ONLY public.tb_camara_refrigeracao ADD CONSTRAINT uk_camara_unidade_nome UNIQUE (unidade_id, nome);
ALTER TABLE ONLY public.tb_camara_refrigeracao ADD CONSTRAINT fk_camara_unidade FOREIGN KEY (unidade_id) REFERENCES public.tb_unidade_de_saude(uuid);

CREATE TABLE public.tb_leitura_temperatura (
    excursao boolean NOT NULL,
    temperatura_atual numeric(4,1) NOT NULL,
    temperatura_maxima numeric(4,1) NOT NULL,
    temperatura_minima numeric(4,1) NOT NULL,
    lida_em timestamp(6) without time zone NOT NULL,
    registrado_em timestamp(6) with time zone NOT NULL,
    camara_id uuid NOT NULL,
    profissional_id uuid,
    uuid uuid NOT NULL,
    observacao character varying(500),
    registrado_por_cpf character varying(255),
    turno character varying(255) NOT NULL,
    CONSTRAINT tb_leitura_temperatura_turno_check CHECK (((turno)::text = ANY ((ARRAY['MANHA'::character varying, 'TARDE'::character varying])::text[])))
);

ALTER TABLE ONLY public.tb_leitura_temperatura ADD CONSTRAINT tb_leitura_temperatura_pkey PRIMARY KEY (uuid);
CREATE INDEX ix_leitura_temperatura_camara ON public.tb_leitura_temperatura USING btree (camara_id, lida_em);
ALTER TABLE ONLY public.tb_leitura_temperatura ADD CONSTRAINT fk_leitura_camara FOREIGN KEY (camara_id) REFERENCES public.tb_camara_refrigeracao(uuid);
ALTER TABLE ONLY public.tb_leitura_temperatura ADD CONSTRAINT fk_leitura_profissional FOREIGN KEY (profissional_id) REFERENCES public.tb_profissional(uuid);

CREATE TABLE public.tb_lote_imunobiologico (
    doses integer NOT NULL,
    validade date NOT NULL,
    avaliado_em timestamp(6) with time zone,
    sob_suspeita_desde timestamp(6) with time zone,
    camara_id uuid,
    imunobiologico_id uuid NOT NULL,
    unidade_id uuid NOT NULL,
    uuid uuid NOT NULL,
    numero_lote character varying(40) NOT NULL,
    fabricante character varying(100),
    parecer character varying(500),
    avaliado_por_cpf character varying(255),
    situacao character varying(255) NOT NULL,
    CONSTRAINT tb_lote_imunobiologico_situacao_check CHECK (((situacao)::text = ANY ((ARRAY['DISPONIVEL'::character varying, 'SOB_SUSPEITA'::character varying, 'INUTILIZADO'::character varying])::text[])))
);

ALTER TABLE ONLY public.tb_lote_imunobiologico ADD CONSTRAINT tb_lote_imunobiologico_pkey PRIMARY KEY (uuid);
ALTER TABLE ONLY public.tb_lote_imunobiologico ADD CONSTRAINT uk_lote_imuno_unidade_numero UNIQUE (unidade_id, imunobiologico_id, numero_lote);
ALTER TABLE ONLY public.tb_lote_imunobiologico ADD CONSTRAINT fk_lote_imuno_unidade FOREIGN KEY (unidade_id) REFERENCES public.tb_unidade_de_saude(uuid);
ALTER TABLE ONLY public.tb_lote_imunobiologico ADD CONSTRAINT fk_lote_imuno_imunobiologico FOREIGN KEY (imunobiologico_id) REFERENCES public.tb_imunobiologico(uuid);
ALTER TABLE ONLY public.tb_lote_imunobiologico ADD CONSTRAINT fk_lote_imuno_camara FOREIGN KEY (camara_id) REFERENCES public.tb_camara_refrigeracao(uuid);

CREATE TABLE public.tb_frasco_aberto (
    doses_no_frasco integer NOT NULL,
    doses_usadas integer NOT NULL,
    aberto_em timestamp(6) without time zone NOT NULL,
    encerrado_em timestamp(6) without time zone,
    valido_ate timestamp(6) without time zone NOT NULL,
    lote_id uuid NOT NULL,
    uuid uuid NOT NULL,
    motivo_encerramento character varying(255),
    CONSTRAINT tb_frasco_aberto_motivo_encerramento_check CHECK (((motivo_encerramento)::text = ANY ((ARRAY['ESGOTADO'::character varying, 'VENCIDO'::character varying])::text[])))
);

ALTER TABLE ONLY public.tb_frasco_aberto ADD CONSTRAINT tb_frasco_aberto_pkey PRIMARY KEY (uuid);
CREATE INDEX ix_frasco_aberto_lote ON public.tb_frasco_aberto USING btree (lote_id);
ALTER TABLE ONLY public.tb_frasco_aberto ADD CONSTRAINT fk_frasco_lote FOREIGN KEY (lote_id) REFERENCES public.tb_lote_imunobiologico(uuid);

CREATE TABLE public.tb_movimentacao_imunobiologico (
    doses integer NOT NULL,
    saldo_apos integer NOT NULL,
    registrado_em timestamp(6) with time zone NOT NULL,
    frasco_id uuid,
    lote_id uuid NOT NULL,
    profissional_id uuid,
    registro_vacinacao_id uuid,
    uuid uuid NOT NULL,
    justificativa character varying(500),
    motivo_perda character varying(255),
    registrado_por_cpf character varying(255),
    tipo character varying(255) NOT NULL,
    CONSTRAINT tb_movimentacao_imunobiologico_motivo_perda_check CHECK (((motivo_perda)::text = ANY ((ARRAY['VENCIMENTO'::character varying, 'QUEBRA'::character varying, 'FALHA_EQUIPAMENTO'::character varying, 'FALTA_ENERGIA'::character varying, 'TRANSPORTE'::character varying, 'EXCURSAO_TEMPERATURA'::character varying, 'FRASCO_ABERTO_VENCIDO'::character varying, 'OUTRO'::character varying])::text[]))),
    CONSTRAINT tb_movimentacao_imunobiologico_tipo_check CHECK (((tipo)::text = ANY ((ARRAY['ENTRADA'::character varying, 'APLICACAO'::character varying, 'ESTORNO_APLICACAO'::character varying, 'PERDA_FISICA'::character varying, 'PERDA_TECNICA'::character varying, 'AJUSTE_INVENTARIO'::character varying])::text[])))
);

ALTER TABLE ONLY public.tb_movimentacao_imunobiologico ADD CONSTRAINT tb_movimentacao_imunobiologico_pkey PRIMARY KEY (uuid);
CREATE INDEX ix_movimentacao_imuno_lote ON public.tb_movimentacao_imunobiologico USING btree (lote_id);
ALTER TABLE ONLY public.tb_movimentacao_imunobiologico ADD CONSTRAINT fk_mov_imuno_lote FOREIGN KEY (lote_id) REFERENCES public.tb_lote_imunobiologico(uuid);
ALTER TABLE ONLY public.tb_movimentacao_imunobiologico ADD CONSTRAINT fk_mov_imuno_frasco FOREIGN KEY (frasco_id) REFERENCES public.tb_frasco_aberto(uuid);
ALTER TABLE ONLY public.tb_movimentacao_imunobiologico ADD CONSTRAINT fk_mov_imuno_profissional FOREIGN KEY (profissional_id) REFERENCES public.tb_profissional(uuid);
ALTER TABLE ONLY public.tb_movimentacao_imunobiologico ADD CONSTRAINT fk_mov_imuno_registro FOREIGN KEY (registro_vacinacao_id) REFERENCES public.tb_registro_vacinacao(uuid);

-- A dose aplicada numa unidade com sala de vacina sai de um lote do estoque.
ALTER TABLE public.tb_registro_vacinacao ADD COLUMN lote_estoque_id uuid;
ALTER TABLE public.tb_registro_vacinacao ADD COLUMN frasco_id uuid;
ALTER TABLE ONLY public.tb_registro_vacinacao ADD CONSTRAINT fk_registro_vacinacao_lote_estoque FOREIGN KEY (lote_estoque_id) REFERENCES public.tb_lote_imunobiologico(uuid);
ALTER TABLE ONLY public.tb_registro_vacinacao ADD CONSTRAINT fk_registro_vacinacao_frasco FOREIGN KEY (frasco_id) REFERENCES public.tb_frasco_aberto(uuid);
