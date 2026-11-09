-- Documentos, fatia D1 (ADR-0172): catálogo de tipos, documentos com dono e versões. Os arquivos ficam num volume próprio.
-- Conferido contra o ddl-auto=validate (ADR-0090).

CREATE TABLE public.tb_tipo_documento (
    ativo boolean NOT NULL,
    uuid uuid NOT NULL,
    codigo character varying(60) NOT NULL,
    nome character varying(100) NOT NULL
);
ALTER TABLE ONLY public.tb_tipo_documento ADD CONSTRAINT tb_tipo_documento_pkey PRIMARY KEY (uuid);
ALTER TABLE ONLY public.tb_tipo_documento ADD CONSTRAINT uk_tipo_documento_codigo UNIQUE (codigo);

CREATE TABLE public.tb_documento (
    data_documento date NOT NULL,
    registrado_em timestamp(6) without time zone NOT NULL,
    retirado_em timestamp(6) without time zone,
    dono_id uuid NOT NULL,
    tipo_id uuid NOT NULL,
    unidade_id uuid NOT NULL,
    uuid uuid NOT NULL,
    dono_tipo character varying(20) NOT NULL,
    titulo character varying(150) NOT NULL,
    motivo_retirada character varying(500),
    registrado_por_cpf character varying(255),
    retirado_por_cpf character varying(255),
    CONSTRAINT tb_documento_dono_tipo_check CHECK (dono_tipo IN ('PACIENTE'))
);
ALTER TABLE ONLY public.tb_documento ADD CONSTRAINT tb_documento_pkey PRIMARY KEY (uuid);
CREATE INDEX ix_documento_dono ON public.tb_documento USING btree (dono_tipo, dono_id);
ALTER TABLE ONLY public.tb_documento ADD CONSTRAINT fk_documento_tipo FOREIGN KEY (tipo_id) REFERENCES public.tb_tipo_documento(uuid);
ALTER TABLE ONLY public.tb_documento ADD CONSTRAINT fk_documento_unidade FOREIGN KEY (unidade_id) REFERENCES public.tb_unidade_de_saude(uuid);

CREATE TABLE public.tb_versao_documento (
    numero integer NOT NULL,
    tamanho bigint NOT NULL,
    enviado_em timestamp(6) without time zone NOT NULL,
    documento_id uuid NOT NULL,
    uuid uuid NOT NULL,
    chave character varying(60) NOT NULL,
    sha256 character varying(64) NOT NULL,
    tipo_conteudo character varying(60) NOT NULL,
    nome_arquivo character varying(255) NOT NULL,
    enviado_por_cpf character varying(255)
);
ALTER TABLE ONLY public.tb_versao_documento ADD CONSTRAINT tb_versao_documento_pkey PRIMARY KEY (uuid);
ALTER TABLE ONLY public.tb_versao_documento ADD CONSTRAINT uk_versao_documento UNIQUE (documento_id, numero);
ALTER TABLE ONLY public.tb_versao_documento ADD CONSTRAINT fk_versao_documento FOREIGN KEY (documento_id) REFERENCES public.tb_documento(uuid);

INSERT INTO public.tb_tipo_documento (uuid, codigo, nome, ativo) VALUES
    (gen_random_uuid(), 'EXAME_EXTERNO', 'Exame externo', true),
    (gen_random_uuid(), 'LAUDO', 'Laudo', true),
    (gen_random_uuid(), 'RECEITA', 'Receita', true),
    (gen_random_uuid(), 'TERMO_CONSENTIMENTO', 'Termo de consentimento', true),
    (gen_random_uuid(), 'DOCUMENTO_PESSOAL', 'Documento pessoal', true),
    (gen_random_uuid(), 'OUTRO', 'Outro', true);
