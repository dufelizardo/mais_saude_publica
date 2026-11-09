-- Transporte, fatia T1 (ADR-0178): frota, situação com histórico, plantão das viaturas com a tripulação e a viatura empenhada na
-- ocorrência de urgência. Conferido contra o ddl-auto=validate (ADR-0090).

CREATE TABLE public.tb_veiculo (
    ano integer,
    cadeira_de_rodas boolean NOT NULL,
    lugares integer,
    maca boolean NOT NULL,
    quilometragem integer,
    unidade_id uuid NOT NULL,
    uuid uuid NOT NULL,
    placa character varying(7) NOT NULL,
    prefixo character varying(20) NOT NULL,
    situacao character varying(20) NOT NULL,
    tipo character varying(30) NOT NULL,
    modelo character varying(80),
    observacao character varying(500),
    CONSTRAINT tb_veiculo_tipo_check CHECK (tipo IN ('USA', 'USB', 'MOTOLANCIA', 'AEROMEDICO', 'AMBULANCIA_TRANSPORTE', 'VAN_SANITARIA', 'MICRO_ONIBUS',
        'CARRO_ADMINISTRATIVO')),
    CONSTRAINT tb_veiculo_situacao_check CHECK (situacao IN ('DISPONIVEL', 'EM_SERVICO', 'MANUTENCAO', 'INATIVO'))
);
ALTER TABLE ONLY public.tb_veiculo ADD CONSTRAINT tb_veiculo_pkey PRIMARY KEY (uuid);
ALTER TABLE ONLY public.tb_veiculo ADD CONSTRAINT uk_veiculo_placa UNIQUE (placa);
ALTER TABLE ONLY public.tb_veiculo ADD CONSTRAINT uk_veiculo_prefixo UNIQUE (prefixo);
ALTER TABLE ONLY public.tb_veiculo ADD CONSTRAINT fk_veiculo_unidade FOREIGN KEY (unidade_id) REFERENCES public.tb_unidade_de_saude(uuid);

CREATE TABLE public.tb_situacao_veiculo (
    inicio timestamp(6) with time zone NOT NULL,
    uuid uuid NOT NULL,
    veiculo_id uuid NOT NULL,
    situacao character varying(20) NOT NULL,
    motivo character varying(500),
    registrado_por_cpf character varying(255),
    CONSTRAINT tb_situacao_veiculo_situacao_check CHECK (situacao IN ('DISPONIVEL', 'EM_SERVICO', 'MANUTENCAO', 'INATIVO'))
);
ALTER TABLE ONLY public.tb_situacao_veiculo ADD CONSTRAINT tb_situacao_veiculo_pkey PRIMARY KEY (uuid);
CREATE INDEX ix_situacao_veiculo ON public.tb_situacao_veiculo USING btree (veiculo_id, inicio);
ALTER TABLE ONLY public.tb_situacao_veiculo ADD CONSTRAINT fk_situacao_veiculo_veiculo FOREIGN KEY (veiculo_id) REFERENCES public.tb_veiculo(uuid);

CREATE TABLE public.tb_plantao_viatura (
    fim timestamp(6) with time zone NOT NULL,
    inicio timestamp(6) with time zone NOT NULL,
    uuid uuid NOT NULL,
    veiculo_id uuid NOT NULL,
    observacao character varying(500),
    registrado_por_cpf character varying(255)
);
ALTER TABLE ONLY public.tb_plantao_viatura ADD CONSTRAINT tb_plantao_viatura_pkey PRIMARY KEY (uuid);
CREATE INDEX ix_plantao_viatura ON public.tb_plantao_viatura USING btree (veiculo_id, inicio);
ALTER TABLE ONLY public.tb_plantao_viatura ADD CONSTRAINT fk_plantao_viatura_veiculo FOREIGN KEY (veiculo_id) REFERENCES public.tb_veiculo(uuid);

CREATE TABLE public.tb_tripulante_plantao (
    plantao_id uuid NOT NULL,
    profissional_id uuid NOT NULL,
    uuid uuid NOT NULL,
    funcao character varying(30) NOT NULL,
    CONSTRAINT tb_tripulante_plantao_funcao_check CHECK (funcao IN ('CONDUTOR', 'TECNICO_ENFERMAGEM', 'ENFERMEIRO', 'MEDICO'))
);
ALTER TABLE ONLY public.tb_tripulante_plantao ADD CONSTRAINT tb_tripulante_plantao_pkey PRIMARY KEY (uuid);
ALTER TABLE ONLY public.tb_tripulante_plantao ADD CONSTRAINT fk_tripulante_plantao FOREIGN KEY (plantao_id) REFERENCES public.tb_plantao_viatura(uuid);
ALTER TABLE ONLY public.tb_tripulante_plantao ADD CONSTRAINT fk_tripulante_profissional FOREIGN KEY (profissional_id) REFERENCES public.tb_profissional(uuid);

ALTER TABLE public.tb_ocorrencia_urgencia ADD COLUMN veiculo_id uuid;
ALTER TABLE ONLY public.tb_ocorrencia_urgencia ADD CONSTRAINT fk_ocorrencia_urgencia_veiculo FOREIGN KEY (veiculo_id) REFERENCES public.tb_veiculo(uuid);
