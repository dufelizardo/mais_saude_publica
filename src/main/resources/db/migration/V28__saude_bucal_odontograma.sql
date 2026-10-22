-- Saúde Bucal, fatia SB2 (ADR-0135): o catálogo de condições do odontograma (com a carga inicial) e os registros por
-- dente (FDI) e face, com a origem e a anulação. Conferido contra o ddl-auto=validate (ADR-0090).

CREATE TABLE public.tb_condicao_odontologica (
    ativa boolean NOT NULL,
    uuid uuid NOT NULL,
    sigla character varying(4) NOT NULL,
    cor character varying(7) NOT NULL,
    codigo character varying(40) NOT NULL,
    nome character varying(100) NOT NULL,
    alcance character varying(255) NOT NULL,
    CONSTRAINT tb_condicao_odontologica_alcance_check CHECK (((alcance)::text = ANY ((ARRAY['FACE'::character varying, 'DENTE'::character varying, 'AMBOS'::character varying])::text[])))
);

ALTER TABLE ONLY public.tb_condicao_odontologica ADD CONSTRAINT tb_condicao_odontologica_pkey PRIMARY KEY (uuid);
ALTER TABLE ONLY public.tb_condicao_odontologica ADD CONSTRAINT uk_condicao_odontologica_codigo UNIQUE (codigo);

-- Carga inicial editável (MODELO-SAUDE-BUCAL, seção 6).
INSERT INTO public.tb_condicao_odontologica (uuid, codigo, nome, alcance, cor, sigla, ativa) VALUES
    (gen_random_uuid(), 'HIGIDO', 'Hígido', 'DENTE', '#16A34A', 'H', true),
    (gen_random_uuid(), 'CARIE', 'Cárie', 'FACE', '#DC2626', 'C', true),
    (gen_random_uuid(), 'RESTAURADO', 'Restaurado', 'FACE', '#2563EB', 'R', true),
    (gen_random_uuid(), 'RESTAURACAO_INSATISFATORIA', 'Restauração insatisfatória', 'FACE', '#F97316', 'RI', true),
    (gen_random_uuid(), 'SELANTE', 'Selante', 'FACE', '#0D9488', 'S', true),
    (gen_random_uuid(), 'AUSENTE', 'Ausente', 'DENTE', '#6B7280', 'A', true),
    (gen_random_uuid(), 'EXTRACAO_INDICADA', 'Extração indicada', 'DENTE', '#B91C1C', 'EI', true),
    (gen_random_uuid(), 'PROTESE', 'Prótese', 'DENTE', '#7C3AED', 'P', true),
    (gen_random_uuid(), 'IMPLANTE', 'Implante', 'DENTE', '#4F46E5', 'I', true),
    (gen_random_uuid(), 'RAIZ_RESIDUAL', 'Raiz residual', 'DENTE', '#92400E', 'RR', true),
    (gen_random_uuid(), 'FRATURA', 'Fratura', 'AMBOS', '#DB2777', 'F', true);

CREATE TABLE public.tb_registro_odontograma (
    dente integer NOT NULL,
    data date NOT NULL,
    anulado_em timestamp(6) with time zone,
    registrado_em timestamp(6) with time zone NOT NULL,
    avaliacao_id uuid,
    condicao_id uuid NOT NULL,
    paciente_id uuid NOT NULL,
    uuid uuid NOT NULL,
    motivo_anulacao character varying(500),
    observacao character varying(500),
    anulado_por_cpf character varying(255),
    face character varying(255),
    registrado_por_cpf character varying(255),
    CONSTRAINT tb_registro_odontograma_face_check CHECK (((face)::text = ANY ((ARRAY['MESIAL'::character varying, 'DISTAL'::character varying, 'OCLUSAL'::character varying, 'INCISAL'::character varying, 'VESTIBULAR'::character varying, 'LINGUAL_PALATINA'::character varying])::text[])))
);

ALTER TABLE ONLY public.tb_registro_odontograma ADD CONSTRAINT tb_registro_odontograma_pkey PRIMARY KEY (uuid);
CREATE INDEX ix_registro_odontograma_paciente ON public.tb_registro_odontograma USING btree (paciente_id, dente);
ALTER TABLE ONLY public.tb_registro_odontograma ADD CONSTRAINT fk_registro_odontograma_paciente FOREIGN KEY (paciente_id) REFERENCES public.tb_paciente(uuid);
ALTER TABLE ONLY public.tb_registro_odontograma ADD CONSTRAINT fk_registro_odontograma_condicao FOREIGN KEY (condicao_id) REFERENCES public.tb_condicao_odontologica(uuid);
ALTER TABLE ONLY public.tb_registro_odontograma ADD CONSTRAINT fk_registro_odontograma_avaliacao FOREIGN KEY (avaliacao_id) REFERENCES public.tb_avaliacao_odontologica(uuid);
