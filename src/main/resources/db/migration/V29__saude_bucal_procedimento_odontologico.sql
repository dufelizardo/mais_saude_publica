-- Saúde Bucal, fatia SB3a (ADR-0136): o catálogo geral de procedimentos (com a carga inicial), o procedimento no
-- atendimento (a consulta passa a ser opcional), o complemento odontológico (dente e faces) e a origem procedimento
-- nos registros do odontograma. Conferido contra o ddl-auto=validate (ADR-0090).

CREATE TABLE public.tb_procedimento_catalogo (
    ativo boolean NOT NULL,
    exige_dentista boolean NOT NULL,
    condicao_resultante_id uuid,
    especialidade_id uuid,
    uuid uuid NOT NULL,
    sigtap character varying(10),
    codigo character varying(40) NOT NULL,
    nome character varying(150) NOT NULL,
    alcance character varying(255) NOT NULL,
    CONSTRAINT tb_procedimento_catalogo_alcance_check CHECK (((alcance)::text = ANY ((ARRAY['NENHUM'::character varying, 'DENTE'::character varying, 'FACE'::character varying])::text[])))
);

ALTER TABLE ONLY public.tb_procedimento_catalogo ADD CONSTRAINT tb_procedimento_catalogo_pkey PRIMARY KEY (uuid);
ALTER TABLE ONLY public.tb_procedimento_catalogo ADD CONSTRAINT uk_procedimento_catalogo_codigo UNIQUE (codigo);
ALTER TABLE ONLY public.tb_procedimento_catalogo ADD CONSTRAINT fk_procedimento_catalogo_especialidade FOREIGN KEY (especialidade_id) REFERENCES public.tb_especialidade(uuid);
ALTER TABLE ONLY public.tb_procedimento_catalogo ADD CONSTRAINT fk_procedimento_catalogo_condicao FOREIGN KEY (condicao_resultante_id) REFERENCES public.tb_condicao_odontologica(uuid);

-- Carga inicial editável. Gerais (sem especialidade) e odontológicos; o SIGTAP fica para quem cadastra, sem chute.
-- Exige dentista: o que a Lei 11.889/2008 não dá ao técnico em saúde bucal (exodontia, restauração, pulpotomia, drenagem).
INSERT INTO public.tb_procedimento_catalogo (uuid, codigo, nome, especialidade_id, alcance, condicao_resultante_id, exige_dentista, ativo) VALUES
    (gen_random_uuid(), 'CURATIVO', 'Curativo', null, 'NENHUM', null, false, true),
    (gen_random_uuid(), 'GLICEMIA_CAPILAR', 'Glicemia capilar', null, 'NENHUM', null, false, true),
    (gen_random_uuid(), 'AFERICAO_PRESSAO', 'Aferição de pressão arterial', null, 'NENHUM', null, false, true),
    (gen_random_uuid(), 'NEBULIZACAO', 'Nebulização / inalação', null, 'NENHUM', null, false, true),
    (gen_random_uuid(), 'RETIRADA_PONTOS', 'Retirada de pontos', null, 'NENHUM', null, false, true),
    (gen_random_uuid(), 'ELETROCARDIOGRAMA', 'Eletrocardiograma', null, 'NENHUM', null, false, true);

INSERT INTO public.tb_procedimento_catalogo (uuid, codigo, nome, especialidade_id, alcance, condicao_resultante_id, exige_dentista, ativo)
SELECT gen_random_uuid(), v.codigo, v.nome, e.uuid, v.alcance, c.uuid, v.exige_dentista, true
FROM (VALUES
    ('RESTAURACAO_RESINA', 'Restauração em resina composta', 'FACE', 'RESTAURADO', true),
    ('RESTAURACAO_AMALGAMA', 'Restauração em amálgama', 'FACE', 'RESTAURADO', true),
    ('RESTAURACAO_IONOMERO', 'Restauração com ionômero de vidro', 'FACE', 'RESTAURADO', true),
    ('SELANTE', 'Aplicação de selante', 'FACE', 'SELANTE', false),
    ('EXODONTIA', 'Exodontia', 'DENTE', 'AUSENTE', true),
    ('PULPOTOMIA', 'Pulpotomia', 'DENTE', null, true),
    ('DRENAGEM_ABSCESSO', 'Drenagem de abscesso dentoalveolar', 'DENTE', null, true),
    ('APLICACAO_FLUOR', 'Aplicação tópica de flúor (individual)', 'NENHUM', null, false),
    ('RASPAGEM_SUPRAGENGIVAL', 'Raspagem, alisamento e polimento supragengival', 'NENHUM', null, false),
    ('PROFILAXIA', 'Profilaxia e remoção da placa bacteriana', 'NENHUM', null, false),
    ('ORIENTACAO_HIGIENE_BUCAL', 'Orientação de higiene bucal', 'NENHUM', null, false)
) AS v(codigo, nome, alcance, condicao, exige_dentista)
JOIN public.tb_especialidade e ON e.codigo = 'ODONTOLOGIA'
LEFT JOIN public.tb_condicao_odontologica c ON c.codigo = v.condicao;

-- O procedimento fica no atendimento; a consulta, quando houver. Os registros antigos herdam o atendimento da consulta.
ALTER TABLE public.tb_procedimento ADD COLUMN atendimento_id uuid;
UPDATE public.tb_procedimento p SET atendimento_id = c.atendimento_id FROM public.tb_consulta c WHERE c.uuid = p.consulta_id;
ALTER TABLE public.tb_procedimento ALTER COLUMN atendimento_id SET NOT NULL;
ALTER TABLE public.tb_procedimento ALTER COLUMN consulta_id DROP NOT NULL;
ALTER TABLE public.tb_procedimento ADD COLUMN procedimento_catalogo_id uuid;
ALTER TABLE ONLY public.tb_procedimento ADD CONSTRAINT fk_procedimento_atendimento FOREIGN KEY (atendimento_id) REFERENCES public.tb_atendimento(uuid);
ALTER TABLE ONLY public.tb_procedimento ADD CONSTRAINT fk_procedimento_catalogo FOREIGN KEY (procedimento_catalogo_id) REFERENCES public.tb_procedimento_catalogo(uuid);
CREATE INDEX ix_procedimento_atendimento ON public.tb_procedimento USING btree (atendimento_id);

CREATE TABLE public.tb_complemento_odontologico (
    dente integer NOT NULL,
    procedimento_id uuid NOT NULL,
    uuid uuid NOT NULL
);

ALTER TABLE ONLY public.tb_complemento_odontologico ADD CONSTRAINT tb_complemento_odontologico_pkey PRIMARY KEY (uuid);
ALTER TABLE ONLY public.tb_complemento_odontologico ADD CONSTRAINT uk_complemento_odontologico_procedimento UNIQUE (procedimento_id);
ALTER TABLE ONLY public.tb_complemento_odontologico ADD CONSTRAINT fk_complemento_odontologico_procedimento FOREIGN KEY (procedimento_id) REFERENCES public.tb_procedimento(uuid);

CREATE TABLE public.tb_complemento_odontologico_face (
    complemento_id uuid NOT NULL,
    face character varying(255) NOT NULL,
    CONSTRAINT tb_complemento_odontologico_face_face_check CHECK (((face)::text = ANY ((ARRAY['MESIAL'::character varying, 'DISTAL'::character varying, 'OCLUSAL'::character varying, 'INCISAL'::character varying, 'VESTIBULAR'::character varying, 'LINGUAL_PALATINA'::character varying])::text[])))
);

ALTER TABLE ONLY public.tb_complemento_odontologico_face ADD CONSTRAINT tb_complemento_odontologico_face_pkey PRIMARY KEY (complemento_id, face);
ALTER TABLE ONLY public.tb_complemento_odontologico_face ADD CONSTRAINT fk_complemento_odontologico_face FOREIGN KEY (complemento_id) REFERENCES public.tb_complemento_odontologico(uuid);

-- A origem procedimento no odontograma: "qual procedimento mudou a condição?".
ALTER TABLE public.tb_registro_odontograma ADD COLUMN procedimento_id uuid;
ALTER TABLE ONLY public.tb_registro_odontograma ADD CONSTRAINT fk_registro_odontograma_procedimento FOREIGN KEY (procedimento_id) REFERENCES public.tb_procedimento(uuid);
