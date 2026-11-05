-- Vigilância em Saúde, fatia V4b (ADR-0163, desenho na ADR-0110): os animais das zoonoses. O animal de interesse, a
-- observação do agressor, os exames (raiva e leishmaniose visceral canina) e a vacinação antirrábica animal. Conferido
-- contra o ddl-auto=validate (ADR-0090).

CREATE TABLE public.tb_animal_zoonoses (
    ativo boolean NOT NULL,
    registrado_em timestamp(6) with time zone NOT NULL,
    imovel_id uuid,
    microarea_id uuid,
    uuid uuid NOT NULL,
    especie character varying(255) NOT NULL,
    nome character varying(200),
    descricao character varying(500),
    tutor_nome character varying(200),
    tutor_telefone character varying(40),
    endereco character varying(300),
    registrado_por_cpf character varying(255),
    CONSTRAINT tb_animal_zoonoses_especie_check CHECK (((especie)::text = ANY ((ARRAY['CAO'::character varying, 'GATO'::character varying, 'MORCEGO'::character varying, 'PRIMATA'::character varying, 'HERBIVORO'::character varying, 'OUTRA'::character varying])::text[])))
);
ALTER TABLE ONLY public.tb_animal_zoonoses ADD CONSTRAINT tb_animal_zoonoses_pkey PRIMARY KEY (uuid);
ALTER TABLE ONLY public.tb_animal_zoonoses ADD CONSTRAINT fk_animal_zoonoses_imovel FOREIGN KEY (imovel_id) REFERENCES public.tb_imovel(uuid);
ALTER TABLE ONLY public.tb_animal_zoonoses ADD CONSTRAINT fk_animal_zoonoses_microarea FOREIGN KEY (microarea_id) REFERENCES public.tb_microarea(uuid);

CREATE TABLE public.tb_observacao_animal (
    data_agressao date NOT NULL,
    desfecho_em date,
    fim_previsto date NOT NULL,
    inicio date NOT NULL,
    registrado_em timestamp(6) with time zone NOT NULL,
    animal_id uuid NOT NULL,
    notificacao_id uuid,
    uuid uuid NOT NULL,
    desfecho character varying(255),
    observacao character varying(1000),
    registrado_por_cpf character varying(255),
    CONSTRAINT tb_observacao_animal_desfecho_check CHECK (((desfecho)::text = ANY ((ARRAY['SADIO'::character varying, 'MORTO'::character varying, 'DESAPARECIDO'::character varying, 'SUSPEITO_RAIVA'::character varying])::text[])))
);
ALTER TABLE ONLY public.tb_observacao_animal ADD CONSTRAINT tb_observacao_animal_pkey PRIMARY KEY (uuid);
CREATE INDEX ix_observacao_animal_notificacao ON public.tb_observacao_animal USING btree (notificacao_id);
ALTER TABLE ONLY public.tb_observacao_animal ADD CONSTRAINT fk_observacao_animal_animal FOREIGN KEY (animal_id) REFERENCES public.tb_animal_zoonoses(uuid);
ALTER TABLE ONLY public.tb_observacao_animal ADD CONSTRAINT fk_observacao_animal_notificacao FOREIGN KEY (notificacao_id) REFERENCES public.tb_notificacao(uuid);

CREATE TABLE public.tb_exame_animal (
    data_coleta date NOT NULL,
    registrado_em timestamp(6) with time zone NOT NULL,
    resultado_em timestamp(6) with time zone,
    animal_id uuid NOT NULL,
    observacao_id uuid,
    uuid uuid NOT NULL,
    tipo character varying(255) NOT NULL,
    resultado character varying(255),
    desfecho_leishmaniose character varying(255),
    observacao character varying(1000),
    registrado_por_cpf character varying(255),
    CONSTRAINT tb_exame_animal_tipo_check CHECK (((tipo)::text = ANY ((ARRAY['RAIVA_IMUNOFLUORESCENCIA'::character varying, 'LEISHMANIOSE_TR_DPP'::character varying, 'LEISHMANIOSE_ELISA'::character varying, 'OUTRO'::character varying])::text[]))),
    CONSTRAINT tb_exame_animal_resultado_check CHECK (((resultado)::text = ANY ((ARRAY['REAGENTE'::character varying, 'NAO_REAGENTE'::character varying, 'INDETERMINADO'::character varying])::text[]))),
    CONSTRAINT tb_exame_animal_desfecho_leishmaniose_check CHECK (((desfecho_leishmaniose)::text = ANY ((ARRAY['EUTANASIA'::character varying, 'TRATAMENTO_VETERINARIO'::character varying, 'ACOMPANHAMENTO'::character varying, 'OBITO'::character varying])::text[])))
);
ALTER TABLE ONLY public.tb_exame_animal ADD CONSTRAINT tb_exame_animal_pkey PRIMARY KEY (uuid);
ALTER TABLE ONLY public.tb_exame_animal ADD CONSTRAINT fk_exame_animal_animal FOREIGN KEY (animal_id) REFERENCES public.tb_animal_zoonoses(uuid);
ALTER TABLE ONLY public.tb_exame_animal ADD CONSTRAINT fk_exame_animal_observacao FOREIGN KEY (observacao_id) REFERENCES public.tb_observacao_animal(uuid);

CREATE TABLE public.tb_vacinacao_antirrabica_animal (
    caes integer NOT NULL,
    gatos integer NOT NULL,
    data date NOT NULL,
    registrado_em timestamp(6) with time zone NOT NULL,
    microarea_id uuid NOT NULL,
    uuid uuid NOT NULL,
    posto character varying(200) NOT NULL,
    responsavel character varying(200) NOT NULL,
    registrado_por_cpf character varying(255)
);
ALTER TABLE ONLY public.tb_vacinacao_antirrabica_animal ADD CONSTRAINT tb_vacinacao_antirrabica_animal_pkey PRIMARY KEY (uuid);
CREATE INDEX ix_vacinacao_antirrabica_data ON public.tb_vacinacao_antirrabica_animal USING btree (data);
ALTER TABLE ONLY public.tb_vacinacao_antirrabica_animal ADD CONSTRAINT fk_vacinacao_antirrabica_animal_microarea FOREIGN KEY (microarea_id) REFERENCES public.tb_microarea(uuid);
