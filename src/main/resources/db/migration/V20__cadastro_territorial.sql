-- Cadastro territorial, fatia F3a (ADR-0127): imóvel na microárea com endereço e ponto, domicílio com as condições da
-- ficha domiciliar do e-SUS, família (núcleo) com início e fim no domicílio e os membros, que são os pacientes.
-- Conferido contra o ddl-auto=validate (ADR-0090). A permissão TERRITORIO.CADASTRAR e o papel de agente comunitário de
-- saúde nascem pelo catálogo de acesso.

CREATE TABLE public.tb_imovel (
    ativo boolean NOT NULL,
    latitude double precision,
    longitude double precision,
    microarea_id uuid NOT NULL,
    uuid uuid NOT NULL,
    bairro character varying(255),
    cep character varying(255),
    cidade character varying(255),
    complemento character varying(255),
    ddd character varying(255),
    estado character varying(255),
    logradouro character varying(255),
    numero_logradouro character varying(255),
    registrado_por_cpf character varying(255),
    tipo character varying(255) NOT NULL,
    CONSTRAINT tb_imovel_tipo_check CHECK (((tipo)::text = ANY ((ARRAY['DOMICILIO'::character varying, 'COMERCIO'::character varying, 'ESCOLA'::character varying, 'INSTITUICAO'::character varying, 'TERRENO_BALDIO'::character varying, 'PONTO_ESTRATEGICO'::character varying, 'OUTRO'::character varying])::text[])))
);

CREATE TABLE public.tb_domicilio (
    ativo boolean NOT NULL,
    comodos integer,
    energia_eletrica boolean,
    imovel_id uuid NOT NULL,
    uuid uuid NOT NULL,
    identificacao character varying(60),
    abastecimento_agua character varying(255),
    destino_lixo character varying(255),
    escoamento_sanitario character varying(255),
    localizacao character varying(255),
    situacao character varying(255),
    CONSTRAINT tb_domicilio_abastecimento_agua_check CHECK (((abastecimento_agua)::text = ANY ((ARRAY['REDE_ENCANADA'::character varying, 'POCO_OU_NASCENTE'::character varying, 'CISTERNA'::character varying, 'CARRO_PIPA'::character varying, 'OUTRO'::character varying])::text[]))),
    CONSTRAINT tb_domicilio_destino_lixo_check CHECK (((destino_lixo)::text = ANY ((ARRAY['COLETADO'::character varying, 'QUEIMADO_OU_ENTERRADO'::character varying, 'CEU_ABERTO'::character varying, 'OUTRO'::character varying])::text[]))),
    CONSTRAINT tb_domicilio_escoamento_sanitario_check CHECK (((escoamento_sanitario)::text = ANY ((ARRAY['REDE_COLETORA'::character varying, 'FOSSA_SEPTICA'::character varying, 'FOSSA_RUDIMENTAR'::character varying, 'DIRETO_RIO_LAGO_MAR'::character varying, 'CEU_ABERTO'::character varying, 'OUTRO'::character varying])::text[]))),
    CONSTRAINT tb_domicilio_localizacao_check CHECK (((localizacao)::text = ANY ((ARRAY['URBANA'::character varying, 'RURAL'::character varying])::text[]))),
    CONSTRAINT tb_domicilio_situacao_check CHECK (((situacao)::text = ANY ((ARRAY['PROPRIO'::character varying, 'FINANCIADO'::character varying, 'ALUGADO'::character varying, 'ARRENDADO'::character varying, 'CEDIDO'::character varying, 'OCUPACAO'::character varying, 'OUTRA'::character varying])::text[])))
);

CREATE TABLE public.tb_nucleo_familiar (
    fim date,
    inicio date NOT NULL,
    domicilio_id uuid NOT NULL,
    responsavel_id uuid NOT NULL,
    uuid uuid NOT NULL,
    motivo_fim character varying(500),
    registrado_por_cpf character varying(255),
    renda_familiar character varying(255),
    CONSTRAINT tb_nucleo_familiar_renda_familiar_check CHECK (((renda_familiar)::text = ANY ((ARRAY['ATE_UM_QUARTO_SM'::character varying, 'ATE_MEIO_SM'::character varying, 'ATE_UM_SM'::character varying, 'ATE_DOIS_SM'::character varying, 'ATE_TRES_SM'::character varying, 'ATE_QUATRO_SM'::character varying, 'ACIMA_DE_QUATRO_SM'::character varying])::text[])))
);

CREATE TABLE public.tb_membro_nucleo (
    fim date,
    inicio date NOT NULL,
    nucleo_id uuid NOT NULL,
    paciente_id uuid NOT NULL,
    uuid uuid NOT NULL,
    motivo_saida character varying(500),
    parentesco character varying(255) NOT NULL,
    registrado_por_cpf character varying(255),
    CONSTRAINT tb_membro_nucleo_parentesco_check CHECK (((parentesco)::text = ANY ((ARRAY['RESPONSAVEL'::character varying, 'CONJUGE'::character varying, 'FILHO'::character varying, 'ENTEADO'::character varying, 'NETO'::character varying, 'PAI_OU_MAE'::character varying, 'SOGRO'::character varying, 'IRMAO'::character varying, 'GENRO_OU_NORA'::character varying, 'OUTRO_PARENTE'::character varying, 'NAO_PARENTE'::character varying])::text[])))
);

ALTER TABLE ONLY public.tb_imovel ADD CONSTRAINT tb_imovel_pkey PRIMARY KEY (uuid);
ALTER TABLE ONLY public.tb_domicilio ADD CONSTRAINT tb_domicilio_pkey PRIMARY KEY (uuid);
ALTER TABLE ONLY public.tb_nucleo_familiar ADD CONSTRAINT tb_nucleo_familiar_pkey PRIMARY KEY (uuid);
ALTER TABLE ONLY public.tb_membro_nucleo ADD CONSTRAINT tb_membro_nucleo_pkey PRIMARY KEY (uuid);

CREATE INDEX ix_imovel_microarea ON public.tb_imovel USING btree (microarea_id);
CREATE INDEX ix_domicilio_imovel ON public.tb_domicilio USING btree (imovel_id);
CREATE INDEX ix_nucleo_domicilio ON public.tb_nucleo_familiar USING btree (domicilio_id, fim);
CREATE INDEX ix_membro_nucleo_nucleo ON public.tb_membro_nucleo USING btree (nucleo_id, fim);
CREATE INDEX ix_membro_nucleo_paciente ON public.tb_membro_nucleo USING btree (paciente_id, fim);

-- Um núcleo vigente por paciente.
CREATE UNIQUE INDEX uk_membro_nucleo_paciente_vigente ON public.tb_membro_nucleo USING btree (paciente_id) WHERE (fim IS NULL);

ALTER TABLE ONLY public.tb_imovel
    ADD CONSTRAINT fk_imovel_microarea FOREIGN KEY (microarea_id) REFERENCES public.tb_microarea(uuid);
ALTER TABLE ONLY public.tb_domicilio
    ADD CONSTRAINT fk_domicilio_imovel FOREIGN KEY (imovel_id) REFERENCES public.tb_imovel(uuid);
ALTER TABLE ONLY public.tb_nucleo_familiar
    ADD CONSTRAINT fk_nucleo_domicilio FOREIGN KEY (domicilio_id) REFERENCES public.tb_domicilio(uuid);
ALTER TABLE ONLY public.tb_nucleo_familiar
    ADD CONSTRAINT fk_nucleo_responsavel FOREIGN KEY (responsavel_id) REFERENCES public.tb_paciente(uuid);
ALTER TABLE ONLY public.tb_membro_nucleo
    ADD CONSTRAINT fk_membro_nucleo_nucleo FOREIGN KEY (nucleo_id) REFERENCES public.tb_nucleo_familiar(uuid);
ALTER TABLE ONLY public.tb_membro_nucleo
    ADD CONSTRAINT fk_membro_nucleo_paciente FOREIGN KEY (paciente_id) REFERENCES public.tb_paciente(uuid);
