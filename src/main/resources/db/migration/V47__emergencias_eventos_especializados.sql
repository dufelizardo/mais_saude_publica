-- Emergências e Desastres, fatia E6a (ADR-0160, desenho na ADR-0113): o perfil do tipo de emergência, a emergência
-- planejada (evento de massa), os dados do evento de massa e as estruturas temporárias de atendimento com os boletins.
-- Conferido contra o ddl-auto=validate (ADR-0090).

ALTER TABLE public.tb_tipo_emergencia ADD COLUMN perfil character varying(255) DEFAULT 'GERAL' NOT NULL;
ALTER TABLE public.tb_tipo_emergencia ALTER COLUMN perfil DROP DEFAULT;
ALTER TABLE public.tb_tipo_emergencia ADD CONSTRAINT tb_tipo_emergencia_perfil_check CHECK (((perfil)::text = ANY ((ARRAY['GERAL'::character varying, 'EVENTO_DE_MASSA'::character varying, 'EPIDEMIA'::character varying, 'DESASTRE_AMBIENTAL'::character varying])::text[])));
UPDATE public.tb_tipo_emergencia SET perfil = 'EVENTO_DE_MASSA' WHERE codigo = 'EVENTO_DE_MASSA';
UPDATE public.tb_tipo_emergencia SET perfil = 'EPIDEMIA' WHERE codigo = 'EPIDEMIA';
UPDATE public.tb_tipo_emergencia SET perfil = 'DESASTRE_AMBIENTAL' WHERE codigo IN ('PRODUTO_PERIGOSO', 'INCENDIO_FLORESTAL');

ALTER TABLE public.tb_emergencia DROP CONSTRAINT tb_emergencia_situacao_check;
ALTER TABLE public.tb_emergencia ADD CONSTRAINT tb_emergencia_situacao_check CHECK (((situacao)::text = ANY ((ARRAY['PLANEJADA'::character varying, 'ATIVA'::character varying, 'EM_DESMOBILIZACAO'::character varying, 'ENCERRADA'::character varying])::text[])));

ALTER TABLE public.tb_desmobilizacao ADD COLUMN estruturas_desativadas integer DEFAULT 0 NOT NULL;
ALTER TABLE public.tb_desmobilizacao ALTER COLUMN estruturas_desativadas DROP DEFAULT;

CREATE TABLE public.tb_evento_massa (
    publico_estimado integer NOT NULL,
    publico_maximo integer,
    atualizado_em timestamp(6) with time zone NOT NULL,
    fim_previsto timestamp(6) with time zone NOT NULL,
    inicio_previsto timestamp(6) with time zone NOT NULL,
    emergencia_id uuid NOT NULL,
    uuid uuid NOT NULL,
    natureza character varying(255) NOT NULL,
    risco character varying(255) NOT NULL,
    local character varying(300) NOT NULL,
    organizador character varying(200),
    contato character varying(200),
    observacao character varying(1000),
    registrado_por_cpf character varying(255),
    CONSTRAINT tb_evento_massa_natureza_check CHECK (((natureza)::text = ANY ((ARRAY['ESPORTIVO'::character varying, 'SHOW'::character varying, 'FESTA_POPULAR'::character varying, 'RELIGIOSO'::character varying, 'MANIFESTACAO'::character varying, 'OUTRO'::character varying])::text[]))),
    CONSTRAINT tb_evento_massa_risco_check CHECK (((risco)::text = ANY ((ARRAY['BAIXO'::character varying, 'MEDIO'::character varying, 'ALTO'::character varying])::text[])))
);
ALTER TABLE ONLY public.tb_evento_massa ADD CONSTRAINT tb_evento_massa_pkey PRIMARY KEY (uuid);
ALTER TABLE ONLY public.tb_evento_massa ADD CONSTRAINT uk_evento_massa_emergencia UNIQUE (emergencia_id);
ALTER TABLE ONLY public.tb_evento_massa ADD CONSTRAINT fk_evento_massa_emergencia FOREIGN KEY (emergencia_id) REFERENCES public.tb_emergencia(uuid);

CREATE TABLE public.tb_estrutura_temporaria (
    ambulancias integer NOT NULL,
    atendimentos_dia integer,
    leitos integer NOT NULL,
    latitude double precision,
    longitude double precision,
    ativada_em timestamp(6) with time zone,
    desativada_em timestamp(6) with time zone,
    emergencia_id uuid NOT NULL,
    unidade_referencia_id uuid NOT NULL,
    uuid uuid NOT NULL,
    tipo character varying(255) NOT NULL,
    situacao character varying(255) NOT NULL,
    nome character varying(200) NOT NULL,
    local character varying(300) NOT NULL,
    horario character varying(200),
    motivo_desativacao character varying(500),
    registrado_por_cpf character varying(255),
    CONSTRAINT tb_estrutura_temporaria_tipo_check CHECK (((tipo)::text = ANY ((ARRAY['POSTO_MEDICO'::character varying, 'TENDA_HIDRATACAO'::character varying, 'HOSPITAL_DE_CAMPANHA'::character varying, 'OUTRO'::character varying])::text[]))),
    CONSTRAINT tb_estrutura_temporaria_situacao_check CHECK (((situacao)::text = ANY ((ARRAY['PLANEJADA'::character varying, 'EM_OPERACAO'::character varying, 'DESATIVADA'::character varying])::text[])))
);
ALTER TABLE ONLY public.tb_estrutura_temporaria ADD CONSTRAINT tb_estrutura_temporaria_pkey PRIMARY KEY (uuid);
CREATE INDEX ix_estrutura_temporaria_emergencia ON public.tb_estrutura_temporaria USING btree (emergencia_id);
ALTER TABLE ONLY public.tb_estrutura_temporaria ADD CONSTRAINT fk_estrutura_temporaria_emergencia FOREIGN KEY (emergencia_id) REFERENCES public.tb_emergencia(uuid);
ALTER TABLE ONLY public.tb_estrutura_temporaria ADD CONSTRAINT fk_estrutura_temporaria_unidade FOREIGN KEY (unidade_referencia_id) REFERENCES public.tb_unidade_de_saude(uuid);

CREATE TABLE public.tb_boletim_estrutura (
    atendimentos integer NOT NULL,
    casos_graves integer NOT NULL,
    obitos integer NOT NULL,
    remocoes integer NOT NULL,
    data date NOT NULL,
    registrado_em timestamp(6) with time zone NOT NULL,
    estrutura_id uuid NOT NULL,
    uuid uuid NOT NULL,
    observacao character varying(1000),
    registrado_por_cpf character varying(255)
);
ALTER TABLE ONLY public.tb_boletim_estrutura ADD CONSTRAINT tb_boletim_estrutura_pkey PRIMARY KEY (uuid);
CREATE INDEX ix_boletim_estrutura_estrutura ON public.tb_boletim_estrutura USING btree (estrutura_id);
ALTER TABLE ONLY public.tb_boletim_estrutura ADD CONSTRAINT fk_boletim_estrutura_estrutura FOREIGN KEY (estrutura_id) REFERENCES public.tb_estrutura_temporaria(uuid);
